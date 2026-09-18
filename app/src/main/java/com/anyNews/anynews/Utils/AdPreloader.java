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
 * {@link #takeReady(String)} if available; otherwise HomePage settle
 * {@code ensureFeedNativeAdLoaded} always requests. Prefetch never blocks or
 * cancels the on-settle seat load.
 */
public final class AdPreloader {
    private static final String TAG = "ANYNEWS_ADS";
    /** Backoff after no-fill — keep short enough to recover before the next seat. */
    private static final long FAIL_BACKOFF_MS = 8_000L;
    /**
     * Shared warm depth across all non-reward feeds (not one-per-mode).
     * Target 3–4 ready unimpressed natives for near-always seat fill.
     */
    private static final int MAX_READY = 4;
    /** Parallel AdLoader starts while topping up toward {@link #MAX_READY}. */
    private static final int MAX_PARALLEL_LOADS = 2;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

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
            // Kick up to MAX_PARALLEL_LOADS while below capacity.
            startLoadIfNeeded(app);
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
    }

    private void startLoadIfNeeded(@Nullable Context appContext) {
        if (appContext == null) {
            return;
        }
        final String unit;
        synchronized (this) {
            int room = MAX_READY - readySlots.size() - loadsInFlight;
            if (room <= 0 || loadsInFlight >= MAX_PARALLEL_LOADS) {
                return;
            }
            long now = SystemClock.uptimeMillis();
            if (lastFailUptimeMs > 0L && (now - lastFailUptimeMs) < FAIL_BACKOFF_MS) {
                return;
            }
            unit = resolveUnit(appContext);
            if (TextUtils.isEmpty(unit)) {
                return;
            }
            loadsInFlight++;
        }
        mainHandler.post(() -> {
            try {
                Log.d(TAG, "AdPreloader LOAD START unit=" + unit
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
                            // Top up toward MAX_READY while warm.
                            prefetch(appContext);
                        })
                        .withAdListener(new AdListener() {
                            @Override
                            public void onAdFailedToLoad(LoadAdError loadAdError) {
                                synchronized (AdPreloader.this) {
                                    if (loadsInFlight > 0) {
                                        loadsInFlight--;
                                    }
                                    lastFailUptimeMs = SystemClock.uptimeMillis();
                                }
                                String detail = loadAdError != null
                                        ? loadAdError.getMessage() : "unknown";
                                Log.d(TAG, "AdPreloader LOAD FAIL " + detail);
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
            }
        });
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
