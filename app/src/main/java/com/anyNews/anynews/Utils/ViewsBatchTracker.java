package com.anyNews.anynews.Utils;

import android.app.Application;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.anyNews.anynews.Pojo.EncritonRequestData;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Buffers post view impressions + dwell segments in memory (with light prefs
 * backup) and POSTs them as one batch to {@code /app/v3/views/batch}.
 *
 * <ul>
 *   <li>Flip settle only enqueues — no per-flip network.</li>
 *   <li>Foreground flush timer every {@link #FLUSH_INTERVAL_MS} (product durability).</li>
 *   <li>Early flush when buffer reaches {@link #FLUSH_AT_COUNT} (admin freshness).</li>
 *   <li>Immediate flush on background / screen-off / trim / process death paths.</li>
 *   <li>Session close can attach a rollup via {@link #snapshotActivitySummary()} —
 *       mid-session heartbeats are not used for view analytics.</li>
 * </ul>
 *
 * Session presence is open + close on {@link SessionAnalyticsTracker};
 * mid-session heartbeats only when admin true-live is ON.
 */
public final class ViewsBatchTracker {

    private static final String PREF_PENDING = "app_views_batch_pending_json";
    private static final long FLUSH_INTERVAL_MS = 60_000L;
    /** Flush sooner so admin impressions are not stuck until the 60s timer. */
    private static final int FLUSH_AT_COUNT = 12;
    private static final long CRITICAL_FLUSH_WAIT_MS = 2_500L;
    private static final int MAX_BUFFER = 250;

    private static final ViewsBatchTracker INSTANCE = new ViewsBatchTracker();
    private static final ExecutorService NET_EXEC = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "views-batch-net");
        t.setPriority(Thread.NORM_PRIORITY);
        return t;
    });

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final Object lock = new Object();
    private final List<JSONObject> pending = new ArrayList<>();

    @Nullable private Context appContext;
    @Nullable private Runnable flushRunnable;
    @Nullable private Runnable persistRunnable;
    private boolean foreground = false;
    private boolean flushInFlight = false;

    private ViewsBatchTracker() {}

    public static ViewsBatchTracker get() {
        return INSTANCE;
    }

    public void init(Application application) {
        if (!initialized.compareAndSet(false, true)) return;
        appContext = application.getApplicationContext();
        restoreFromPrefs(appContext);

        ProcessLifecycleOwner.get().getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_START)
            public void onAppForeground() {
                foreground = true;
                startFlushTimer();
                // Deliver anything restored after a kill.
                flush(appContext, false);
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
            public void onAppBackground() {
                foreground = false;
                stopFlushTimer();
                // End in-progress dwell into the queue, then POST the batch.
                try {
                    PostDwellTracker.get().flush(appContext, false);
                } catch (Exception ignored) {
                }
                // Sync prefs before best-effort network so a kill mid-POST can restore.
                synchronized (lock) {
                    persistBackupLocked(appContext);
                }
                flush(appContext, true);
            }
        });

        application.registerComponentCallbacks(new ComponentCallbacks2() {
            @Override
            public void onTrimMemory(int level) {
                if (level >= ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
                    flush(appContext, true);
                }
            }

            @Override
            public void onConfigurationChanged(Configuration newConfig) {}

            @Override
            public void onLowMemory() {
                flush(appContext, true);
            }
        });
    }

    /**
     * Non-destructive rollup of buffered events for session close.
     * Does not remove events — {@link #flush} still owns delivery to /views/batch.
     */
    @Nullable
    public JSONObject snapshotActivitySummary() {
        synchronized (lock) {
            if (pending.isEmpty()) return null;
            try {
                int impressions = 0;
                int dwellEvents = 0;
                long dwellSeconds = 0L;
                java.util.HashSet<Long> unique = new java.util.HashSet<>();
                for (JSONObject e : pending) {
                    if (e == null) continue;
                    long postId = e.optLong("post_id", 0L);
                    if (postId > 0) unique.add(postId);
                    if (e.has("duration_seconds")) {
                        dwellEvents++;
                        dwellSeconds += Math.max(0L, e.optLong("duration_seconds", 0L));
                    } else {
                        impressions++;
                    }
                }
                JSONObject summary = new JSONObject();
                summary.put("impressions", impressions);
                summary.put("dwell_events", dwellEvents);
                summary.put("dwell_seconds_total", dwellSeconds);
                summary.put("unique_posts", unique.size());
                // Rollup only on close — product view/dwell rows go via /views/batch.
                // Avoid attaching events[] here to prevent double-count when batch also lands.
                return summary;
            } catch (Exception ignored) {
                return null;
            }
        }
    }

    /** View open (impression) — local queue only. */
    public void enqueueImpression(
            Context context,
            long postId,
            int draftId,
            @Nullable String sessionId,
            @Nullable String postType) {
        if (postId <= 0 || draftId <= 0) return;
        try {
            JSONObject e = new JSONObject();
            e.put("draft_id", draftId);
            e.put("post_id", postId);
            long openedAt = System.currentTimeMillis();
            e.put("opened_at", openedAt);
            if (sessionId != null) e.put("session_id", sessionId);
            if (postType != null) e.put("post_type", postType);
            boolean offline = false;
            try {
                offline = context != null && !BookmarkStore.isNetworkAvailable(context);
            } catch (Exception ignored) {
            }
            if (offline) {
                e.put("offline", true);
                e.put("offline_at", openedAt);
            }
            addEvent(context, e);
            try {
                com.anyNews.anynews.rewards.RewardsEngagementBatcher.get()
                        .trackOpen(context, String.valueOf(postId));
            } catch (Exception ignored) {
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /** Dwell segment — local queue only (caller may {@link #flush} if critical). */
    public void enqueueDwell(
            Context context,
            long postId,
            long durationSeconds,
            int draftId,
            @Nullable String sessionId,
            @Nullable String postType) {
        if (postId <= 0 || draftId <= 0 || durationSeconds < 0) return;
        try {
            JSONObject e = new JSONObject();
            e.put("draft_id", draftId);
            e.put("post_id", postId);
            e.put("duration_seconds", Math.max(1L, durationSeconds));
            if (sessionId != null) e.put("session_id", sessionId);
            if (postType != null) e.put("post_type", postType);
            try {
                if (context != null && !BookmarkStore.isNetworkAvailable(context)) {
                    e.put("offline", true);
                    e.put("offline_at", System.currentTimeMillis());
                }
            } catch (Exception ignored) {
            }
            addEvent(context, e);
            try {
                int secs = (int) Math.max(1L, durationSeconds);
                com.anyNews.anynews.rewards.RewardsEngagementBatcher.get()
                        .trackQualityRead(context, String.valueOf(postId), secs);
                if (secs >= 8) {
                    com.anyNews.anynews.rewards.RewardsEngagementBatcher.get()
                            .trackScrollDepth(context, String.valueOf(postId), 55);
                }
            } catch (Exception ignored) {
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Send buffered events. {@code critical=true} uses a blocking execute on the
     * worker (awaits only off the main thread) so background / kill paths can land.
     */
    public void flush(@Nullable Context context, boolean critical) {
        final Context appCtx = context != null
                ? context.getApplicationContext()
                : appContext;
        if (appCtx == null) return;
        // Keep offline views on disk until online — avoid spinning failed POSTs.
        try {
            if (!BookmarkStore.isNetworkAvailable(appCtx)) {
                synchronized (lock) {
                    persistBackupLocked(appCtx);
                }
                return;
            }
        } catch (Exception ignored) {
        }

        final List<JSONObject> batch;
        synchronized (lock) {
            if (pending.isEmpty()) return;
            if (flushInFlight && !critical) return;
            batch = new ArrayList<>(pending);
            pending.clear();
            flushInFlight = true;
            // Keep prefs as this batch until POST succeeds (process-kill safety).
            persistBackupListLocked(appCtx, batch);
        }

        if (critical) {
            CountDownLatch done = new CountDownLatch(1);
            NET_EXEC.execute(() -> {
                try {
                    postBatchBlocking(appCtx, batch);
                } finally {
                    synchronized (lock) {
                        flushInFlight = false;
                    }
                    done.countDown();
                }
            });
            // Never block the main thread (ProcessLifecycle / trim / SCREEN_OFF).
            if (Looper.myLooper() == Looper.getMainLooper()) {
                return;
            }
            try {
                done.await(CRITICAL_FLUSH_WAIT_MS, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        } else {
            NET_EXEC.execute(() -> {
                try {
                    postBatchAsync(appCtx, batch);
                } finally {
                    synchronized (lock) {
                        flushInFlight = false;
                    }
                }
            });
        }
    }

    private void addEvent(Context context, JSONObject event) {
        Context appCtx = context.getApplicationContext();
        boolean flushSoon = false;
        synchronized (lock) {
            pending.add(event);
            while (pending.size() > MAX_BUFFER) {
                pending.remove(0);
            }
            if (foreground && pending.size() >= FLUSH_AT_COUNT) {
                flushSoon = true;
            }
        }
        // Debounced prefs backup (process-kill safety) — not on the flip hot path.
        schedulePersistBackup(appCtx);
        if (flushSoon) {
            mainHandler.post(() -> flush(appCtx, false));
        }
    }

    private void schedulePersistBackup(Context appCtx) {
        if (persistRunnable != null) {
            mainHandler.removeCallbacks(persistRunnable);
        }
        persistRunnable = () -> {
            synchronized (lock) {
                persistBackupLocked(appCtx);
            }
        };
        mainHandler.postDelayed(persistRunnable, 2_000L);
    }

    private void startFlushTimer() {
        stopFlushTimer();
        flushRunnable = new Runnable() {
            @Override
            public void run() {
                if (!foreground) return;
                flush(appContext, false);
                if (foreground) {
                    mainHandler.postDelayed(this, FLUSH_INTERVAL_MS);
                }
            }
        };
        mainHandler.postDelayed(flushRunnable, FLUSH_INTERVAL_MS);
    }

    private void stopFlushTimer() {
        if (flushRunnable != null) {
            mainHandler.removeCallbacks(flushRunnable);
            flushRunnable = null;
        }
    }

    private void persistBackupLocked(Context appCtx) {
        persistBackupListLocked(appCtx, pending);
    }

    private void persistBackupListLocked(Context appCtx, List<JSONObject> events) {
        try {
            JSONArray arr = new JSONArray();
            if (events != null) {
                for (JSONObject o : events) {
                    arr.put(o);
                }
            }
            // Also include any events that arrived after this flush snapshot was taken.
            if (events != pending) {
                for (JSONObject o : pending) {
                    arr.put(o);
                }
            }
            SharedPrefsData.putString(appCtx, PREF_PENDING, arr.toString(), Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
    }

    private void restoreFromPrefs(Context appCtx) {
        try {
            String raw = SharedPrefsData.getString(appCtx, PREF_PENDING, Constants.PREF_NAME);
            if (raw == null || raw.isEmpty() || "[]".equals(raw)) return;
            JSONArray arr = new JSONArray(raw);
            synchronized (lock) {
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject o = arr.optJSONObject(i);
                    if (o != null) pending.add(o);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void requeue(Context appCtx, List<JSONObject> batch) {
        if (batch == null || batch.isEmpty()) return;
        synchronized (lock) {
            pending.addAll(0, batch);
            while (pending.size() > MAX_BUFFER) {
                pending.remove(pending.size() - 1);
            }
            persistBackupLocked(appCtx);
        }
    }

    private void postBatchBlocking(Context appCtx, List<JSONObject> batch) {
        try {
            Call<JsonObject> call = buildCall(appCtx, batch);
            if (call == null) {
                requeue(appCtx, batch);
                return;
            }
            Response<JsonObject> response = call.execute();
            if (response.isSuccessful()) {
                synchronized (lock) {
                    persistBackupLocked(appCtx);
                }
            } else {
                requeue(appCtx, batch);
            }
        } catch (Exception e) {
            e.printStackTrace();
            requeue(appCtx, batch);
        }
    }

    private void postBatchAsync(Context appCtx, List<JSONObject> batch) {
        try {
            Call<JsonObject> call = buildCall(appCtx, batch);
            if (call == null) {
                requeue(appCtx, batch);
                return;
            }
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful()) {
                        synchronized (lock) {
                            persistBackupLocked(appCtx);
                        }
                    } else {
                        requeue(appCtx, batch);
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    requeue(appCtx, batch);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            requeue(appCtx, batch);
        }
    }

    @Nullable
    private Call<JsonObject> buildCall(Context appCtx, List<JSONObject> batch) throws Exception {
        if (batch == null || batch.isEmpty()) return null;
        JSONArray events = new JSONArray();
        for (JSONObject o : batch) {
            events.put(o);
        }
        JSONObject body = new JSONObject();
        body.put("events", events);
        String encrypted = MyUtils.encrypt(body.toString());
        EncritonRequestData enc = new EncritonRequestData();
        enc.setData(encrypted);
        JsonObject payload = new Gson().toJsonTree(enc).getAsJsonObject();
        APIService api = RestApiClient.getClient().create(APIService.class);
        return api.post_views_batch("v3", payload);
    }
}
