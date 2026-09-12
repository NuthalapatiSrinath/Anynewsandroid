package com.anyNews.anynews.Utils;

import android.app.Application;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.anyNews.anynews.BuildConfig;
import com.anyNews.anynews.Pojo.EncritonRequestData;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Batches real native-ad display impressions (Google / FB painted on screen) and
 * POSTs one flush to {@code /app/v3/report_ad_impressions}.
 *
 * <p>No per-ad network. Counts survive process death via SharedPreferences.
 * Flush when pending unacked impressions reach admin {@code ad_impr_flush_every_n}
 * (default 3), and also on background, trim, or next cold-start foreground.
 * After successful ack, only the flushed days are cleared.
 */
public final class AdImpressionReporter {

    private static final String PREF_PENDING = "app_ad_impr_pending_json";
    private static final String NETWORK_GOOGLE = "google";
    private static final String NETWORK_FB = "fb";
    private static final long DEDUP_TTL_MS = 30 * 60_000L;
    private static final int MAX_DEDUP = 400;
    private static final int MAX_DAYS = 14;

    private static final AdImpressionReporter INSTANCE = new AdImpressionReporter();
    private static final ExecutorService NET_EXEC =
            Executors.newSingleThreadExecutor(
                    r -> {
                        Thread t = new Thread(r, "ad-impr-batch");
                        t.setPriority(Thread.NORM_PRIORITY - 1);
                        return t;
                    });

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final Object lock = new Object();
    /** IST date YYYY-MM-DD → {google, fb} */
    private final Map<String, DayCount> pending = new LinkedHashMap<>();
    /** identityHashCode(network|obj) → last counted wall ms (avoid double-count on rebind) */
    private final ConcurrentHashMap<String, Long> countedAds = new ConcurrentHashMap<>();

    @Nullable private Context appContext;
    private boolean flushInFlight = false;

    /** Coalesced flush — never run on flip/paint frames. */
    private final Runnable deferredFlushRunnable =
            new Runnable() {
                @Override
                public void run() {
                    if (FlipAdWorkGate.shouldDeferAdWork()) {
                        mainHandler.postDelayed(
                                this, Math.max(200L, FlipAdWorkGate.millisUntilAdWorkSafe()));
                        return;
                    }
                    flush(appContext, false);
                }
            };

    private AdImpressionReporter() {}

    public static AdImpressionReporter get() {
        return INSTANCE;
    }

    public void init(Application application) {
        if (!initialized.compareAndSet(false, true)) return;
        appContext = application.getApplicationContext();
        restoreFromPrefs(appContext);

        ProcessLifecycleOwner.get()
                .getLifecycle()
                .addObserver(
                        new LifecycleObserver() {
                            @OnLifecycleEvent(Lifecycle.Event.ON_START)
                            public void onAppForeground() {
                                // Next cold start / resume: deliver anything restored after kill.
                                flush(appContext, false);
                            }

                            @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
                            public void onAppBackground() {
                                synchronized (lock) {
                                    persistLocked(appContext);
                                }
                                flush(appContext, true);
                            }
                        });

        application.registerComponentCallbacks(
                new ComponentCallbacks2() {
                    @Override
                    public void onTrimMemory(int level) {
                        if (level >= ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
                            synchronized (lock) {
                                persistLocked(appContext);
                            }
                            flush(appContext, true);
                        }
                    }

                    @Override
                    public void onConfigurationChanged(Configuration newConfig) {}

                    @Override
                    public void onLowMemory() {
                        synchronized (lock) {
                            persistLocked(appContext);
                        }
                        flush(appContext, true);
                    }
                });
    }

    /** Google native painted via {@code NativeAdView.setNativeAd}. */
    public void recordGoogleDisplay(@Nullable Object nativeAd) {
        recordDisplay(NETWORK_GOOGLE, nativeAd);
    }

    /** FB native painted via {@code registerViewForInteraction}. */
    public void recordFbDisplay(@Nullable Object nativeAd) {
        recordDisplay(NETWORK_FB, nativeAd);
    }

    private void recordDisplay(String network, @Nullable Object nativeAd) {
        if (nativeAd == null) return;
        String dedupKey = network + "|" + System.identityHashCode(nativeAd);
        long now = System.currentTimeMillis();
        Long prev = countedAds.get(dedupKey);
        if (prev != null && now - prev < DEDUP_TTL_MS) {
            return;
        }
        countedAds.put(dedupKey, now);
        pruneDedup(now);

        String day = todayIst();
        boolean shouldFlush = false;
        synchronized (lock) {
            DayCount dc = pending.get(day);
            if (dc == null) {
                while (pending.size() >= MAX_DAYS) {
                    Iterator<String> it = pending.keySet().iterator();
                    if (!it.hasNext()) break;
                    it.next();
                    it.remove();
                }
                dc = new DayCount();
                pending.put(day, dc);
            }
            if (NETWORK_FB.equals(network)) {
                dc.fb += 1;
            } else {
                dc.google += 1;
            }
            // Do not SharedPreferences-write on every paint (can hitch flip).
            // Persist on flush / background / trim; memory holds counts until then.
            int threshold = FeedAdConfig.getFlushEveryN(appContext);
            shouldFlush = pendingTotalLocked() >= threshold;
        }
        // Feed paints are not rewarded videos. Do not write ads_seen (that
        // flooded the server and counted toward the daily watch-ad cap).
        if (shouldFlush) {
            // Never SharedPreferences + network flush on the paint / flip path.
            // Coalesce until the main queue is idle after settle grace.
            mainHandler.removeCallbacks(deferredFlushRunnable);
            long delay = FlipAdWorkGate.shouldDeferAdWork()
                    ? Math.max(400L, FlipAdWorkGate.millisUntilAdWorkSafe() + 180L)
                    : 400L;
            mainHandler.postDelayed(deferredFlushRunnable, delay);
        }
    }

    /** Sum of all unacked google + fb across pending days (must hold {@link #lock}). */
    private int pendingTotalLocked() {
        int total = 0;
        for (DayCount dc : pending.values()) {
            if (dc == null) continue;
            total += Math.max(0, dc.google) + Math.max(0, dc.fb);
        }
        return total;
    }

    public void flush(@Nullable Context context, boolean critical) {
        final Context appCtx =
                context != null ? context.getApplicationContext() : appContext;
        if (appCtx == null) return;
        try {
            if (!BookmarkStore.isNetworkAvailable(appCtx)) {
                synchronized (lock) {
                    persistLocked(appCtx);
                }
                return;
            }
        } catch (Exception ignored) {
        }

        final List<JSONObject> days;
        final String flushId;
        synchronized (lock) {
            if (pending.isEmpty()) return;
            if (flushInFlight && !critical) return;
            days = snapshotDaysLocked();
            if (days.isEmpty()) return;
            flushId = UUID.randomUUID().toString();
            flushInFlight = true;
            // Keep prefs until ack so a kill mid-POST can restore.
            persistLocked(appCtx);
        }

        NET_EXEC.execute(
                () -> {
                    try {
                        postBatch(appCtx, flushId, days);
                    } finally {
                        synchronized (lock) {
                            flushInFlight = false;
                        }
                    }
                });
    }

    private List<JSONObject> snapshotDaysLocked() {
        List<JSONObject> out = new ArrayList<>(pending.size());
        for (Map.Entry<String, DayCount> e : pending.entrySet()) {
            DayCount dc = e.getValue();
            if (dc == null) continue;
            int g = Math.max(0, dc.google);
            int f = Math.max(0, dc.fb);
            if (g <= 0 && f <= 0) continue;
            try {
                JSONObject o = new JSONObject();
                o.put("date", e.getKey());
                o.put("google", g);
                o.put("fb", f);
                out.add(o);
            } catch (Exception ignored) {
            }
        }
        return out;
    }

    private void postBatch(Context appCtx, String flushId, List<JSONObject> days) {
        try {
            JSONArray arr = new JSONArray();
            for (JSONObject o : days) arr.put(o);
            JSONObject body = new JSONObject();
            body.put("flush_id", flushId);
            body.put("days", arr);
            body.put(
                    "device_id",
                    SharedPrefsData.getString(appCtx, Constants.DeviceID, Constants.PREF_NAME));
            body.put("app_version", BuildConfig.VERSION_NAME);
            int draftId = SharedPrefsData.getInt(appCtx, Constants.DraftID, Constants.PREF_NAME);
            if (draftId > 0) body.put("draft_id", draftId);

            String encrypted = MyUtils.encrypt(body.toString());
            EncritonRequestData enc = new EncritonRequestData();
            enc.setData(encrypted);
            JsonObject payload = new Gson().toJsonTree(enc).getAsJsonObject();
            APIService api = RestApiClient.getClient().create(APIService.class);
            Call<JsonObject> call = api.report_ad_impressions("v3", payload);
            call.enqueue(
                    new Callback<JsonObject>() {
                        @Override
                        public void onResponse(
                                Call<JsonObject> call, Response<JsonObject> response) {
                            if (response != null && response.isSuccessful()) {
                                clearFlushedDays(days);
                            }
                            // Soft-fail: keep pending; next flush / cold start retries.
                        }

                        @Override
                        public void onFailure(Call<JsonObject> call, Throwable t) {
                            // Leave pending + prefs intact.
                        }
                    });
        } catch (Exception e) {
            // Leave pending for next attempt.
        }
    }

    /** Subtract only the amounts we just flushed (new impressions may have arrived). */
    private void clearFlushedDays(List<JSONObject> flushed) {
        if (flushed == null || flushed.isEmpty()) return;
        boolean again = false;
        synchronized (lock) {
            for (JSONObject o : flushed) {
                if (o == null) continue;
                String date = o.optString("date", "");
                if (TextUtils.isEmpty(date)) continue;
                DayCount dc = pending.get(date);
                if (dc == null) continue;
                dc.google = Math.max(0, dc.google - Math.max(0, o.optInt("google", 0)));
                dc.fb = Math.max(0, dc.fb - Math.max(0, o.optInt("fb", 0)));
                if (dc.google <= 0 && dc.fb <= 0) {
                    pending.remove(date);
                }
            }
            persistLocked(appContext);
            int threshold = FeedAdConfig.getFlushEveryN(appContext);
            again = pendingTotalLocked() >= threshold;
        }
        if (again) {
            flush(appContext, false);
        }
    }

    private void restoreFromPrefs(@Nullable Context context) {
        if (context == null) return;
        try {
            String raw =
                    SharedPrefsData.getString(context, PREF_PENDING, Constants.PREF_NAME);
            if (TextUtils.isEmpty(raw)) return;
            JSONObject root = new JSONObject(raw);
            JSONArray days = root.optJSONArray("days");
            if (days == null) return;
            synchronized (lock) {
                pending.clear();
                for (int i = 0; i < days.length(); i++) {
                    JSONObject o = days.optJSONObject(i);
                    if (o == null) continue;
                    String date = o.optString("date", "");
                    if (TextUtils.isEmpty(date)) continue;
                    DayCount dc = new DayCount();
                    dc.google = Math.max(0, o.optInt("google", 0));
                    dc.fb = Math.max(0, o.optInt("fb", 0));
                    if (dc.google > 0 || dc.fb > 0) {
                        pending.put(date, dc);
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void persistLocked(@Nullable Context context) {
        if (context == null) return;
        try {
            JSONArray arr = new JSONArray();
            for (Map.Entry<String, DayCount> e : pending.entrySet()) {
                DayCount dc = e.getValue();
                if (dc == null || (dc.google <= 0 && dc.fb <= 0)) continue;
                JSONObject o = new JSONObject();
                o.put("date", e.getKey());
                o.put("google", Math.max(0, dc.google));
                o.put("fb", Math.max(0, dc.fb));
                arr.put(o);
            }
            JSONObject root = new JSONObject();
            root.put("days", arr);
            SharedPrefsData.putString(
                    context, PREF_PENDING, root.toString(), Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
    }

    private void pruneDedup(long now) {
        if (countedAds.size() <= MAX_DEDUP) return;
        try {
            for (Map.Entry<String, Long> e : countedAds.entrySet()) {
                Long t = e.getValue();
                if (t == null || now - t > DEDUP_TTL_MS) {
                    countedAds.remove(e.getKey(), t);
                }
            }
            if (countedAds.size() > MAX_DEDUP) {
                int drop = countedAds.size() - MAX_DEDUP / 2;
                Iterator<Map.Entry<String, Long>> it = countedAds.entrySet().iterator();
                while (it.hasNext() && drop > 0) {
                    it.next();
                    it.remove();
                    drop--;
                }
            }
        } catch (Exception ignored) {
        }
    }

    static String todayIst() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
        return sdf.format(System.currentTimeMillis());
    }

    private static final class DayCount {
        int google;
        int fb;
    }
}
