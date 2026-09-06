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

/**
 * Holds at most one preloaded Google native for feed ad seats that show the
 * share-with-friends / house placeholder while waiting for fill.
 *
 * <p>Prefetch early (splash / home idle / profile). On seat open, callers
 * {@link #takeReady()} if available; otherwise the existing HomePage load path
 * runs after arrival. Does not show ads on unrelated screens.
 */
public final class AdPreloader {
    private static final String TAG = "ANYNEWS_ADS";
    private static final long FAIL_BACKOFF_MS = 12_000L;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Nullable
    private NativeAd readyAd;
    @Nullable
    private String readyAdCode;
    private boolean loading;
    private long lastFailUptimeMs;

    private AdPreloader() {}

    private static final class Holder {
        private static final AdPreloader INSTANCE = new AdPreloader();
    }

    public static AdPreloader get() {
        return Holder.INSTANCE;
    }

    /** True when a creative is waiting to be claimed by the next share/ad seat. */
    public synchronized boolean isReady() {
        return readyAd != null;
    }

    public synchronized boolean isLoading() {
        return loading;
    }

    /**
     * Warm the next share-screen native if empty. Safe to call often; no-ops when
     * already ready, in-flight, or in failure backoff.
     */
    public void prefetch(@Nullable Context context) {
        if (context == null) {
            return;
        }
        final Context app = context.getApplicationContext();
        AdMobInitializer.runWhenInitialized(app, () -> startLoadIfNeeded(app));
    }

    /**
     * Claim the preloaded creative for a feed seat. Returns null when not ready
     * (caller keeps load-after-arrive). Ownership transfers to the caller.
     */
    @Nullable
    public synchronized NativeAd takeReady() {
        NativeAd ad = readyAd;
        readyAd = null;
        String code = readyAdCode;
        readyAdCode = null;
        if (ad != null) {
            Log.d(TAG, "AdPreloader TAKE ready unit=" + safe(code));
        }
        return ad;
    }

    @Nullable
    public synchronized String peekAdCode() {
        return readyAdCode;
    }

    /** Drop a held creative (e.g. process teardown). Does not cancel in-flight SDK work. */
    public synchronized void clear() {
        if (readyAd != null) {
            try {
                readyAd.destroy();
            } catch (Exception ignored) {
            }
            readyAd = null;
            readyAdCode = null;
        }
    }

    private void startLoadIfNeeded(@Nullable Context appContext) {
        if (appContext == null) {
            return;
        }
        final String unit;
        synchronized (this) {
            if (readyAd != null || loading) {
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
            loading = true;
        }
        mainHandler.post(() -> {
            try {
                Log.d(TAG, "AdPreloader LOAD START unit=" + unit);
                AdLoader adLoader = new AdLoader.Builder(appContext, unit)
                        .forNativeAd(nativeAd -> {
                            synchronized (AdPreloader.this) {
                                loading = false;
                                if (readyAd != null) {
                                    try {
                                        nativeAd.destroy();
                                    } catch (Exception ignored) {
                                    }
                                    Log.d(TAG, "AdPreloader LOAD discard (already ready)");
                                    return;
                                }
                                readyAd = nativeAd;
                                readyAdCode = unit;
                                Log.d(TAG, "AdPreloader LOAD SUCCESS unit=" + unit);
                            }
                        })
                        .withAdListener(new AdListener() {
                            @Override
                            public void onAdFailedToLoad(LoadAdError loadAdError) {
                                synchronized (AdPreloader.this) {
                                    loading = false;
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
                    loading = false;
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

    private static String safe(@Nullable String s) {
        return s == null ? "" : s;
    }
}
