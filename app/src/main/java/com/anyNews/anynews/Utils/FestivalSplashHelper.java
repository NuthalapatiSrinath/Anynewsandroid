package com.anyNews.anynews.Utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

/**
 * Opening Banner disabled app-side — logo splash only ({@code AppThemeSplash}).
 * Kept as a compile-stable stub; server may still send festival fields (ignored).
 */
public final class FestivalSplashHelper {
    private static final int DEFAULT_DURATION_SEC = 2;

    /** Legacy constant; no download waits on open. */
    public static final long ENSURE_TIMEOUT_MS = 0L;

    private FestivalSplashHelper() {}

    public static boolean isEnabled(Context ctx) {
        return false;
    }

    public static boolean shouldShowOnPush(Context ctx) {
        return false;
    }

    public static boolean shouldShowOnShare(Context ctx) {
        return false;
    }

    public static boolean isAnyShowPathEnabled(Context ctx) {
        return false;
    }

    public static boolean shouldSuppressLogoSplash(Context ctx, Intent launchIntent) {
        return false;
    }

    public static boolean shouldShowForLaunchIntent(Context ctx, Intent intent) {
        return false;
    }

    public static boolean isPushLaunchIntent(Intent intent) {
        return false;
    }

    public static boolean isShareOrDeepLinkLaunchIntent(Intent intent) {
        return false;
    }

    public static int getGapFillColor(Context ctx) {
        return 0xFFEF4923;
    }

    public static void storeGapFillColor(Context ctx, int color) {
    }

    public static int sampleAndStoreGapColor(Context ctx, Bitmap bmp) {
        return getGapFillColor(ctx);
    }

    public static int sampleEdgeAverage(Bitmap bmp) {
        return 0;
    }

    public static int getDurationMs(Context ctx) {
        return DEFAULT_DURATION_SEC * 1000;
    }

    public static String getImageUrl(Context ctx) {
        return "";
    }

    public static long getUpdatedAt(Context ctx) {
        return 0L;
    }

    public static java.io.File getLocalCacheFile(Context ctx) {
        return null;
    }

    public static String extractUrlVersion(String url) {
        return "";
    }

    public static boolean hasReadyLocalImage(Context ctx) {
        return false;
    }

    public static Bitmap decodeLocalBitmap(Context ctx) {
        return null;
    }

    public static void invalidateLocalCache(Context ctx) {
    }

    public static void saveFromServer(
            Context ctx,
            Boolean enabled,
            Integer durationSeconds,
            String imageUrl) {
    }

    public static void saveFromServer(
            Context ctx,
            Boolean enabled,
            Boolean showOnPush,
            Boolean showOnShare,
            Integer durationSeconds,
            String imageUrl) {
    }

    public static void saveFromServer(
            Context ctx,
            Boolean enabled,
            Boolean showOnPush,
            Boolean showOnShare,
            Integer durationSeconds,
            String imageUrl,
            Long updatedAt) {
    }

    public static void prefetchInBackground(Context ctx) {
    }

    public static boolean ensureLocalImageBlocking(Context ctx, long timeoutMs) {
        return false;
    }

    public static void ensureLocalImageAsync(Context ctx, long timeoutMs, final Runnable onMain) {
        if (onMain != null) {
            onMain.run();
        }
    }
}