package com.anyNews.anynews.Utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.anyNews.anynews.R;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared warm pool of ready-but-<b>unimpressed</b> Google natives for all non-reward
 * in-feed seats (News, Local, Flash/Daily Updates, Special, search).
 *
 * <p>Money-safe rule: each {@link NativeAd} object may be claimed at most once. After a
 * seat takes a creative, it leaves this pool forever — never re-park an impressed
 * object. A <b>new</b> network response with the same creative content / responseId is
 * fine (new impression). Rewards inventory stays separate and must not call here.
 *
 * <p>Best-effort early warm (splash / home idle / mid-flip). Safe during flip because
 * this path only runs AdLoader — no FlipView bind. On seat open, callers
 * {@link #takeReady(String)} if a creative is ready <b>or</b> HomePage settle
 * {@code ensureFeedNativeAdLoaded} starts a seat load — never both for the same
 * seat moment. Each warm/replenish uses primary then optional secondary on
 * no-fill only (max 2 AdMob requests).
 */
public final class AdPreloader {
    private static final String TAG = "ANYNEWS_ADS";
    /**
     * Backoff after no-fill. Short enough to recover mid-session, long enough to
     * avoid hammering AdMob when inventory is thin (hurts match rate).
     */
    private static final long FAIL_BACKOFF_MS = 18_000L;
    /**
     * Shared warm depth across all non-reward feeds (not one-per-mode).
     * Keep exactly one ready creative — deeper pools request ads that never impress.
     */
    private static final int MAX_READY = 1;
    /** At most one AdLoader while topping up toward {@link #MAX_READY}. */
    private static final int MAX_PARALLEL_LOADS = 1;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    @Nullable private Runnable delayedRecoverPrefetch;

    private static final class ReadySlot {
        final NativeAd ad;
        final String adCode;
        @Nullable final String responseId;

        ReadySlot(NativeAd ad, String adCode, @Nullable String responseId) {
            this.ad = ad;
            this.adCode = adCode;
            this.responseId = responseId;
        }
    }

    private final List<ReadySlot> readySlots = new ArrayList<>(MAX_READY);
    private int loadsInFlight;
    private long lastFailUptimeMs;
    @Nullable
    private volatile ReadyListener readyListener;

    /** Notified on the main thread when a house-seat creative becomes claimable. */
    public interface ReadyListener {
        void onPreloadedNativeReady();
    }

    private AdPreloader() {}

    private static final class Holder {
        private static final AdPreloader INSTANCE = new AdPreloader();
    }

    public static AdPreloader get() {
        return Holder.INSTANCE;
    }

    /**
     * HomePage registers so a parked share/house seat can in-place paint when this
     * warm creative arrives after land (no FlipView recycle / blink).
     */
    public void setReadyListener(@Nullable ReadyListener listener) {
        readyListener = listener;
    }

    /** True when at least one unimpressed creative is waiting to be claimed. */
    public synchronized boolean isReady() {
        return !readySlots.isEmpty();
    }

    public synchronized int readyCount() {
        return readySlots.size();
    }

    public synchronized boolean isLoading() {
        return loadsInFlight > 0;
    }

    /** Warm-pool capacity (ready unimpressed target). */
    public static int maxReady() {
        return MAX_READY;
    }

    private void notifyReadyListener() {
        final ReadyListener listener = readyListener;
        if (listener == null) {
            return;
        }
        mainHandler.post(() -> {
            try {
                listener.onPreloadedNativeReady();
            } catch (Exception ignored) {
            }
        });
    }

    /**
     * Warm the shared pool if below {@link #MAX_READY}. Safe to call often; no-ops when
     * full, at parallel-load cap, or in failure backoff. Does not touch HomePage seat
     * in-flight markers. Survives News/Local/Flash/Special switches (do not
     * {@link #clear()} there).
     */
    public void prefetch(@Nullable Context context) {
        if (context == null) {
            return;
        }
        final Context app = context.getApplicationContext();
        AdMobInitializer.runWhenInitialized(app, () -> {
            // One start per call; MAX_PARALLEL_LOADS gates concurrency.
            startLoadIfNeeded(app);
        });
    }

    /**
     * Claim one preloaded creative for a feed seat. Ownership transfers to the caller.
     * Prefer a different AdMob {@code responseId} than {@code preferExcludeResponseId},
     * but still hand out a same-responseId slot when that is the only warm inventory —
     * it is a new {@link NativeAd} object from a new request (paid impression OK).
     * Never returns an already-impressed object (pool only holds unimpressed).
     */
    @Nullable
    public synchronized NativeAd takeReady(@Nullable String preferExcludeResponseId) {
        if (readySlots.isEmpty()) {
            return null;
        }
        int pickPreferred = -1;
        int pickAny = -1;
        for (int i = 0; i < readySlots.size(); i++) {
            ReadySlot slot = readySlots.get(i);
            if (slot == null || slot.ad == null) {
                continue;
            }
            if (pickAny < 0) {
                pickAny = i;
            }
            if (!TextUtils.isEmpty(preferExcludeResponseId)
                    && preferExcludeResponseId.equals(slot.responseId)) {
                continue;
            }
            pickPreferred = i;
            break;
        }
        int pick = pickPreferred >= 0 ? pickPreferred : pickAny;
        if (pick < 0) {
            return null;
        }
        ReadySlot taken = readySlots.remove(pick);
        Log.d(TAG, "AdPreloader TAKE ready unit=" + safe(taken.adCode)
                + " resp=" + safe(taken.responseId)
                + (pickPreferred < 0 && !TextUtils.isEmpty(preferExcludeResponseId)
                ? " (same-respId ok — new object)" : "")
                + " remaining=" + readySlots.size());
        return taken.ad;
    }

    /** Claim any ready creative (no responseId preference). */
    @Nullable
    public synchronized NativeAd takeReady() {
        return takeReady(null);
    }

    @Nullable
    public synchronized NativeAd takeReadyAndReplenish(@Nullable Context context) {
        return takeReadyAndReplenish(context, null);
    }

    @Nullable
    public synchronized NativeAd takeReadyAndReplenish(
            @Nullable Context context, @Nullable String preferExcludeResponseId) {
        NativeAd ad = takeReady(preferExcludeResponseId);
        if (context != null) {
            prefetch(context);
        }
        return ad;
    }

    @Nullable
    public synchronized String peekAdCode() {
        if (readySlots.isEmpty()) {
            return null;
        }
        ReadySlot first = readySlots.get(0);
        return first != null ? first.adCode : null;
    }

    /**
     * Drop all held creatives (Activity destroy / process teardown only).
     * Do <b>not</b> call on ordinary News↔Local↔Flash↔Special switches — that wastes
     * warm unimpressed inventory.
     */
    public synchronized void clear() {
        for (ReadySlot slot : readySlots) {
            if (slot != null) {
                destroyQuietly(slot.ad);
            }
        }
        readySlots.clear();
        loadsInFlight = 0;
        if (delayedRecoverPrefetch != null) {
            mainHandler.removeCallbacks(delayedRecoverPrefetch);
            delayedRecoverPrefetch = null;
        }
    }

    private void startLoadIfNeeded(@Nullable Context appContext) {
        startLoadIfNeeded(appContext, null);
    }

    private void startLoadIfNeeded(
            @Nullable Context appContext, @Nullable String unitOverride) {
        if (appContext == null) {
            return;
        }
        final String unit;
        final boolean isSecondaryAttempt;
        synchronized (this) {
            int room = MAX_READY - readySlots.size() - loadsInFlight;
            if (room <= 0 || loadsInFlight >= MAX_PARALLEL_LOADS) {
                return;
            }
            long now = SystemClock.uptimeMillis();
            if (TextUtils.isEmpty(unitOverride)
                    && lastFailUptimeMs > 0L
                    && (now - lastFailUptimeMs) < FAIL_BACKOFF_MS) {
                return;
            }
            if (!TextUtils.isEmpty(unitOverride)) {
                unit = unitOverride.trim();
                isSecondaryAttempt = true;
            } else {
                unit = resolveUnit(appContext);
                isSecondaryAttempt = false;
            }
            if (TextUtils.isEmpty(unit)) {
                return;
            }
            loadsInFlight++;
        }
        mainHandler.post(() -> {
            try {
                Log.d(TAG, "AdPreloader LOAD START unit=" + unit
                        + (isSecondaryAttempt ? " (secondary)" : "")
                        + " inFlight=" + loadsInFlight
                        + " pool=" + readySlots.size());
                AdLoader adLoader = new AdLoader.Builder(appContext, unit)
                        .forNativeAd(nativeAd -> {
                            boolean notify = false;
                            synchronized (AdPreloader.this) {
                                if (loadsInFlight > 0) {
                                    loadsInFlight--;
                                }
                                if (readySlots.size() >= MAX_READY) {
                                    destroyQuietly(nativeAd);
                                    Log.d(TAG, "AdPreloader LOAD discard (pool full)");
                                    return;
                                }
                                String rid = responseIdOf(nativeAd);
                                readySlots.add(new ReadySlot(nativeAd, unit, rid));
                                notify = true;
                                Log.d(TAG, "AdPreloader LOAD SUCCESS unit=" + unit
                                        + " resp=" + safe(rid)
                                        + " pool=" + readySlots.size());
                            }
                            if (notify) {
                                // Parked house/share seat: paint without waiting for FlipView recycle.
                                notifyReadyListener();
                            }
                            // Do not chain another load here. Refill happens after
                            // takeReadyAndReplenish / HomePage settle — one load → show → refill.
                        })
                        .withAdListener(new AdListener() {
                            @Override
                            public void onAdFailedToLoad(LoadAdError loadAdError) {
                                synchronized (AdPreloader.this) {
                                    if (loadsInFlight > 0) {
                                        loadsInFlight--;
                                    }
                                }
                                boolean noFill = loadAdError != null
                                        && loadAdError.getCode() == AdRequest.ERROR_CODE_NO_FILL;
                                String detail = loadAdError != null
                                        ? loadAdError.getMessage() : "unknown";
                                Log.d(TAG, "AdPreloader LOAD FAIL " + detail
                                        + " noFill=" + noFill
                                        + " unit=" + unit);
                                // Primary no-fill → try secondary once (admin toggle).
                                if (noFill && !isSecondaryAttempt) {
                                    String secondary =
                                            FeedAdConfig.resolveFallbackUnitAfterNoFill(
                                                    appContext, unit);
                                    if (!TextUtils.isEmpty(secondary)) {
                                        Log.d(TAG, "AdPreloader waterfall → secondary="
                                                + secondary);
                                        startLoadIfNeeded(appContext, secondary);
                                        return;
                                    }
                                }
                                synchronized (AdPreloader.this) {
                                    lastFailUptimeMs = SystemClock.uptimeMillis();
                                }
                                // Never stop for the session — resume warm after backoff.
                                scheduleRecoverPrefetch(appContext);
                            }
                        })
                        .withNativeAdOptions(new NativeAdOptions.Builder()
                                .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
                                .build())
                        .build();
                adLoader.loadAd(new AdRequest.Builder().build());
            } catch (Exception e) {
                synchronized (AdPreloader.this) {
                    if (loadsInFlight > 0) {
                        loadsInFlight--;
                    }
                    lastFailUptimeMs = SystemClock.uptimeMillis();
                }
                Log.d(TAG, "AdPreloader LOAD exception: " + e.getMessage());
                scheduleRecoverPrefetch(appContext);
            }
        });
    }

    /**
     * After a fail streak the warm pool used to sit empty until some other HomePage
     * path happened to call {@link #prefetch}. Always re-arm after backoff so inventory
     * can return mid-session without an app kill.
     */
    private void scheduleRecoverPrefetch(@Nullable Context appContext) {
        if (appContext == null) {
            return;
        }
        final Context app = appContext.getApplicationContext();
        synchronized (this) {
            if (delayedRecoverPrefetch != null) {
                mainHandler.removeCallbacks(delayedRecoverPrefetch);
            }
            delayedRecoverPrefetch = () -> {
                delayedRecoverPrefetch = null;
                Log.d(TAG, "AdPreloader recover prefetch after fail backoff");
                prefetch(app);
            };
            mainHandler.postDelayed(delayedRecoverPrefetch, FAIL_BACKOFF_MS + 80L);
        }
    }

    @Nullable
    private static String resolveUnit(Context context) {
        try {
            if (FeedAdConfig.isInterstitial(context)) {
                // Native preloader is unused in interstitial mode.
                return null;
            }
            String unit = FeedAdConfig.getUnitId(context);
            if (!TextUtils.isEmpty(unit)) {
                return unit.trim();
            }
            unit = context.getString(R.string.admob_native_ad_unit_fallback);
            return unit == null ? null : unit.trim();
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    private static String responseIdOf(@Nullable NativeAd ad) {
        if (ad == null) {
            return null;
        }
        try {
            if (ad.getResponseInfo() != null) {
                return ad.getResponseInfo().getResponseId();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static void destroyQuietly(@Nullable NativeAd ad) {
        if (ad == null) {
            return;
        }
        try {
            ad.destroy();
        } catch (Exception ignored) {
        }
    }

    private static String safe(@Nullable String s) {
        return s == null ? "" : s;
    }
}
