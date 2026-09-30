package com.anyNews.anynews.Utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.anyNews.anynews.BuildConfig;
import com.anyNews.anynews.Pojo.EncreptionRequest;
import com.anyNews.anynews.Pojo.EncritonRequestData;
import com.anyNews.anynews.Pojo.ListAllArticlesRequest;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.anyNews.anynews.Utils.MyUtils.decrypt;
import static com.anyNews.anynews.Utils.MyUtils.encrypt;

/**
 * Thrifty offline-pack page filler while the user reads the main feed online.
 *
 * <ul>
 *   <li>After page-0 success: capture base; schedule at most one delayed next page.</li>
 *   <li>Max 1 in-flight prefetch; cooldown between pages.</li>
 *   <li>Never starts when offline / special / sticky / bad network / app backgrounded /
 *       main feed already loading ({@link Host#isMainFeedBusy()}).</li>
 *   <li>Prefers reusing pages already bound into the list (capture/merge) and only
 *       issues gentle {@code List_All_Articles} pages until media-ready pack reaches
 *       {@link OfflineFeedStore#getTargetPack}.</li>
 *   <li>Merge only keeps offline-eligible posts (no half/full video, must have image
 *       URL). Under-target after video/image filter does not chase aggressively — at most
 *       {@link #maxExtraPages} extra APIs; an all-video page stops further fill.</li>
 * </ul>
 */
public final class OfflinePackPrefetcher {

    private static final String TAG = "OfflinePackPrefetcher";
    private static final OfflinePackPrefetcher INSTANCE = new OfflinePackPrefetcher();

    /** Soft idle delay band after page bind / flip milestones (stay off flip path). */
    private static final long IDLE_DELAY_MIN_MS = 6_000L;
    private static final long IDLE_DELAY_MAX_MS = 14_000L;
    /** Cooldown between successful prefetch pages. */
    private static final long PAGE_COOLDOWN_MS = 12_000L;
    /** Flips after which we allow a pending pack fill. */
    private static final int FLIPS_TO_NUDGE = 5;
    /**
     * Floor / ceiling for thrifty extra List_All_Articles pages beyond the display feed.
     * Actual budget scales with {@link OfflineFeedStore#getTargetPack} so a pack max of
     * 50 can fill from page-0 alone (page size ~8), not stop around one page + 3.
     */
    private static final int MIN_EXTRA_PAGES = 3;
    private static final int MAX_EXTRA_PAGES_ABS = 10;

    public interface Host {
        boolean isDeviceOnline();
        boolean isSpecialMode();
        boolean isStickyFeedOpen();
        /** True while HomePage has List_All_Articles in-flight for the visible feed. */
        boolean isMainFeedBusy();
        /** Home activity currently resumed. */
        boolean isHostResumed();
        int getCategoryId();
        @Nullable String getDataFrom2g();
        int getScreenWidth();
        int getScreenHeight();
        /**
         * Optional thrift: if held next page is empty and cursors match Home pagination,
         * store body as dummyResponse so load-more does not re-hit the network.
         */
        void offerHeldNextPageIfUseful(
                long skip, long lastPostId, long actualSkip, @NonNull EncreptionRequest body);
    }

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();
    private final AtomicBoolean inFlight = new AtomicBoolean(false);

    private long packSkip;
    private long packLastPostId;
    private long packActualSkip;
    private boolean cursorsReady;
    private int extraPagesFetched;
    private int flipCountSinceBind;
    private long lastPrefetchAtMs;
    private long lastScheduleToken;

    @Nullable private Host host;
    @Nullable private Runnable pending;

    private OfflinePackPrefetcher() {}

    public static OfflinePackPrefetcher get() {
        return INSTANCE;
    }

    public void bindHost(@Nullable Host host) {
        this.host = host;
    }

    public void resetSession() {
        cancelPending();
        inFlight.set(false);
        cursorsReady = false;
        packSkip = 0;
        packLastPostId = 0;
        packActualSkip = 0;
        extraPagesFetched = 0;
        flipCountSinceBind = 0;
        lastPrefetchAtMs = 0;
    }

    /**
     * Call after successful online main feed convert (page-0 replace or append/soft merge).
     * Captures the current list and schedules thrifty fill if under target.
     */
    public void onMainFeedCaptured(
            @Nullable Context context,
            @Nullable List<ModelClass.Datum> feedList,
            @Nullable ModelClass pageModel,
            boolean page0) {
        if (context == null) return;
        try {
            OfflineFeedStore.mergeCapture(context, feedList, false);
        } catch (Exception ignored) {
        }
        if (page0) {
            extraPagesFetched = 0;
            flipCountSinceBind = 0;
        }
        if (pageModel != null) {
            applyCursorsFromModel(pageModel);
        }
        scheduleIfNeeded(context, false);
    }

    /**
     * When Home holds a next page (reqPos=3) for the visible feed, also merge that
     * page into the offline pack without waiting for end-of-list apply.
     */
    public void onHeldPageForDisplay(
            @Nullable Context context, @Nullable EncreptionRequest body) {
        if (context == null || body == null) return;
        try {
            ModelClass model = parseModel(body);
            if (model == null || model.getData() == null || model.getData().isEmpty()) return;
            OfflineFeedStore.mergeCapture(context, model.getData(), true);
            applyCursorsFromModel(model);
            scheduleIfNeeded(context, false);
        } catch (Exception e) {
            Logone.w(e, TAG + " onHeldPageForDisplay");
        }
    }

    /** Flip settle on main online feed — may nudge pack fill after a few cards. */
    public void onMainFlipSettled(@Nullable Context context) {
        flipCountSinceBind++;
        if (flipCountSinceBind >= FLIPS_TO_NUDGE) {
            scheduleIfNeeded(context, true);
        }
    }

    public void onHostPaused() {
        cancelPending();
    }

    private void scheduleIfNeeded(@Nullable Context context, boolean fromFlip) {
        if (context == null) return;
        cancelPending();
        if (!shouldWork(context)) return;
        // Target is media-ready count (badge / Offline News list), not raw JSON candidates.
        if (OfflineFeedStore.mainPackCount(context) >= OfflineFeedStore.getTargetPack(context)) {
            return;
        }
        if (extraPagesFetched >= maxExtraPages(context)) return;
        if (!cursorsReady) return;

        long delay;
        if (fromFlip && flipCountSinceBind >= FLIPS_TO_NUDGE) {
            delay = IDLE_DELAY_MIN_MS + random.nextInt(1_500);
        } else {
            delay =
                    IDLE_DELAY_MIN_MS
                            + random.nextInt(
                                    (int) Math.max(1L, IDLE_DELAY_MAX_MS - IDLE_DELAY_MIN_MS + 1));
        }
        final long token = ++lastScheduleToken;
        final Context app = context.getApplicationContext();
        pending =
                new Runnable() {
                    @Override
                    public void run() {
                        if (token != lastScheduleToken) return;
                        pending = null;
                        maybeFirePrefetch(app);
                    }
                };
        mainHandler.postDelayed(pending, delay);
        Logone.v(TAG, "schedule pack fill in " + delay + "ms fromFlip=" + fromFlip);
    }

    /**
     * How many thrifty pages we may fetch beyond what the visible feed already loaded.
     * Scales with pack target (~8 articles/page) so Offline News can reach admin max.
     */
    private static int maxExtraPages(@Nullable Context context) {
        int target = OfflineFeedStore.getTargetPack(context);
        // ceil(target / 8) pages to approach target from an empty display cursor,
        // minus the page-0 already captured by the main feed.
        int pages = Math.max(1, (target + 7) / 8);
        int extra = Math.max(0, pages - 1);
        if (extra < MIN_EXTRA_PAGES) return MIN_EXTRA_PAGES;
        return Math.min(MAX_EXTRA_PAGES_ABS, extra);
    }

    private void maybeFirePrefetch(@NonNull Context app) {
        if (!shouldWork(app)) return;
        // Never start a pack network page during an active flip / settle grace.
        if (FlipAdWorkGate.shouldDeferAdWork()) {
            scheduleIfNeeded(app, false);
            return;
        }
        if (OfflineFeedStore.mainPackCount(app) >= OfflineFeedStore.getTargetPack(app)) return;
        if (extraPagesFetched >= maxExtraPages(app)) return;
        if (!cursorsReady) return;
        long now = System.currentTimeMillis();
        if (lastPrefetchAtMs > 0 && (now - lastPrefetchAtMs) < PAGE_COOLDOWN_MS) return;
        if (!inFlight.compareAndSet(false, true)) return;

        Host h = host;
        if (h == null || h.isMainFeedBusy()) {
            inFlight.set(false);
            // Retry gently when feed busy (do not fight load-more).
            scheduleIfNeeded(app, false);
            return;
        }

        final long reqSkip = packSkip;
        final long reqLast = packLastPostId;
        final long reqActual = packActualSkip;
        JsonObject body;
        try {
            body = buildArticlesRequest(app, h, reqSkip, reqLast, reqActual);
        } catch (Exception e) {
            inFlight.set(false);
            return;
        }
        if (body == null) {
            inFlight.set(false);
            return;
        }

        APIService api = RestApiClient.getClient().create(APIService.class);
        Call<EncreptionRequest> call = api.List_All_Articles("v3", body);
        String dedupeKey =
                "articles_v3_"
                        + h.getCategoryId()
                        + "_"
                        + reqSkip
                        + "_"
                        + reqLast
                        + "_0_0_false";
        Logone.v(TAG, "fire prefetch skip=" + reqSkip + " last=" + reqLast);

        Callback<EncreptionRequest> cb =
                new Callback<EncreptionRequest>() {
                    @Override
                    public void onResponse(
                            Call<EncreptionRequest> call, Response<EncreptionRequest> response) {
                        try {
                            if (response != null && response.body() != null) {
                                EncreptionRequest enc = response.body();
                                ModelClass model = parseModel(enc);
                                if (model != null
                                        && Boolean.TRUE.equals(model.getSuccess())
                                        && model.getData() != null
                                        && !model.getData().isEmpty()) {
                                    int beforeReady = OfflineFeedStore.mainPackCount(app);
                                    int beforeCand = OfflineFeedStore.candidatePackCount(app);
                                    OfflineFeedStore.mergeCapture(app, model.getData(), true);
                                    int afterReady = OfflineFeedStore.mainPackCount(app);
                                    int afterCand = OfflineFeedStore.candidatePackCount(app);
                                    applyCursorsFromModel(model);
                                    extraPagesFetched++;
                                    lastPrefetchAtMs = System.currentTimeMillis();
                                    // No new candidates at all → stop (all video/ads or empty filter).
                                    if (afterCand <= beforeCand) {
                                        extraPagesFetched = maxExtraPages(app);
                                        Logone.v(
                                                TAG,
                                                "prefetch no candidate growth; stop fill ready="
                                                        + afterReady
                                                        + " cand="
                                                        + afterCand);
                                    }
                                    Host hh = host;
                                    if (hh != null) {
                                        try {
                                            hh.offerHeldNextPageIfUseful(
                                                    reqSkip, reqLast, reqActual, enc);
                                        } catch (Exception ignored) {
                                        }
                                    }
                                    Logone.v(
                                            TAG,
                                            "prefetch ok ready="
                                                    + afterReady
                                                    + " cand="
                                                    + afterCand
                                                    + " extraPages="
                                                    + extraPagesFetched
                                                    + " maxExtra="
                                                    + maxExtraPages(app));
                                }
                            }
                        } catch (Exception e) {
                            Logone.w(e, TAG + " prefetch response");
                        } finally {
                            inFlight.set(false);
                            if (OfflineFeedStore.mainPackCount(app)
                                            < OfflineFeedStore.getTargetPack(app)
                                    && extraPagesFetched < maxExtraPages(app)) {
                                scheduleIfNeeded(app, false);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<EncreptionRequest> call, Throwable t) {
                        inFlight.set(false);
                        Logone.v(
                                TAG,
                                "prefetch fail: "
                                        + (t != null ? t.getMessage() : "null"));
                    }
                };
        try {
            NetworkCallDeduper.enqueueUnique(dedupeKey, call, cb);
        } catch (Exception e) {
            inFlight.set(false);
        }
    }

    private boolean shouldWork(@Nullable Context context) {
        if (context == null) return false;
        Host h = host;
        if (h == null) return false;
        if (!h.isHostResumed()) return false;
        if (!h.isDeviceOnline()) return false;
        if (h.isSpecialMode()) return false;
        if (h.isStickyFeedOpen()) return false;
        if (!isAppForeground()) return false;
        if (!isNetworkGoodEnough(context, h.getDataFrom2g())) return false;
        return true;
    }

    private static boolean isAppForeground() {
        try {
            return ProcessLifecycleOwner.get()
                    .getLifecycle()
                    .getCurrentState()
                    .isAtLeast(Lifecycle.State.STARTED);
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Skip thrifty pack fill on offline/poor links (no internet capability, or explicit 2G flag).
     */
    static boolean isNetworkGoodEnough(@Nullable Context context, @Nullable String dataFrom2g) {
        if (context == null) return false;
        if (dataFrom2g != null) {
            String v = dataFrom2g.trim();
            if (!v.isEmpty()
                    && !"0".equals(v)
                    && !"null".equalsIgnoreCase(v)
                    && !"false".equalsIgnoreCase(v)) {
                return false;
            }
        }
        try {
            ConnectivityManager cm =
                    (ConnectivityManager)
                            context.getApplicationContext()
                                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) return false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                android.net.Network network = cm.getActiveNetwork();
                if (network == null) return false;
                NetworkCapabilities caps = cm.getNetworkCapabilities(network);
                if (caps == null
                        || !caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                    return false;
                }
                // Prefer validated links when the OS reports them.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        && !caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                        && !caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                        && !caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    // Cellular without VALIDATED is often flaky — still allow if INTERNET present
                    // unless marked restricted.
                }
                return caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                        || caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                        || caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                        || caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN);
            }
            return BookmarkStore.isNetworkAvailable(context);
        } catch (Exception e) {
            return BookmarkStore.isNetworkAvailable(context);
        }
    }

    private void applyCursorsFromModel(@NonNull ModelClass model) {
        if (model.getLastPostId() != null) {
            packLastPostId = model.getLastPostId();
        }
        if (model.getSkip() != null) {
            packSkip = model.getSkip();
        }
        if (model.getActualSkip() != null) {
            packActualSkip = model.getActualSkip();
        }
        cursorsReady = packLastPostId != 0 || packSkip != 0 || packActualSkip != 0
                || (model.getData() != null && !model.getData().isEmpty());
        // Even page-0 with zero cursors mark ready if we had data + server advanced skip.
        if (model.getData() != null && !model.getData().isEmpty()) {
            cursorsReady = true;
        }
    }

    @Nullable
    private static ModelClass parseModel(@NonNull EncreptionRequest body) {
        try {
            if (body.getData() == null) return null;
            String plain = decrypt(body.getData());
            if (plain == null || plain.length() < 2) return null;
            return new Gson().fromJson(plain, ModelClass.class);
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    private JsonObject buildArticlesRequest(
            @NonNull Context app,
            @NonNull Host h,
            long skip,
            long lastPostId,
            long actualSkip) {
        ListAllArticlesRequest requestModel = new ListAllArticlesRequest();
        requestModel.setDraftId(
                SharedPrefsData.getInt(app, Constants.DraftID, Constants.PREF_NAME));
        requestModel.setCategoryId(h.getCategoryId());
        requestModel.setLanguageId(
                SharedPrefsData.getInt(app, Constants.LanguageID, Constants.PREF_NAME));
        requestModel.setSkip(skip);
        requestModel.setLastPostId(lastPostId);
        requestModel.setPushPostId(0L);
        requestModel.setReferred_post_id(
                SharedPrefsData.getString(app, Constants.REFERRER_POST, Constants.PREF_NAME));
        requestModel.setSharePostId(0L);
        requestModel.setActualSkip(actualSkip);
        try {
            Allsharedpreference prefs = new Allsharedpreference(app);
            requestModel.setAdvertiserID(prefs.getAdvertiserId(Allsharedpreference.ADVERTISER_ID));
        } catch (Exception ignored) {
            requestModel.setAdvertiserID("");
        }
        String d2g = h.getDataFrom2g();
        requestModel.setDatafrom2g(d2g != null ? d2g : "0");
        requestModel.setSpecial_posts(false);
        int w = h.getScreenWidth();
        int ht = h.getScreenHeight();
        if (w <= 0 || ht <= 0) {
            DisplayMetrics dm = app.getResources().getDisplayMetrics();
            w = dm.widthPixels;
            ht = dm.heightPixels;
        }
        requestModel.setScreenHeight(ht);
        requestModel.setScreenWidth(w);
        requestModel.setUser_session_id(
                SharedPrefsData.getString(app, Constants.USER_SESSION_ID, Constants.PREF_NAME));
        requestModel.setApp_version_name("" + BuildConfig.VERSION_NAME);
        requestModel.setApp_version_number("" + BuildConfig.VERSION_CODE);
        requestModel.setSdk_version(Build.VERSION.SDK_INT);
        requestModel.setNotif_perm_allowed(1);

        String jsonString = new Gson().toJson(requestModel);
        EncritonRequestData enc = new EncritonRequestData();
        enc.setData(encrypt(jsonString));
        return new Gson().toJsonTree(enc).getAsJsonObject();
    }

    private void cancelPending() {
        if (pending != null) {
            mainHandler.removeCallbacks(pending);
            pending = null;
        }
        lastScheduleToken++;
    }
}
