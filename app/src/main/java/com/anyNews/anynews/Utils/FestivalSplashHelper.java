package com.anyNews.anynews.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.concurrent.Future;

/**
 * Opening Banner / festival splash — admin toggles + duration + banner.
 * Paths: normal ({@link #isEnabled}), push ({@link #shouldShowOnPush}),
 * share/deep-link ({@link #shouldShowOnShare}). All default OFF.
 * Prefers a local file under filesDir so open never waits on the network.
 * Display uses CENTER_CROP full-bleed (no letterbox bars).
 */
public final class FestivalSplashHelper {
    private static final String TAG = "FestivalSplash";
    private static final String LOCAL_NAME = "festival_splash_cache.bin";
    private static final int DEFAULT_DURATION_SEC = 2;
    private static final int MIN_DURATION_SEC = 1;
    private static final int MAX_DURATION_SEC = 8;

    private FestivalSplashHelper() {}

    /** Show on normal launcher / Recents open (no push or share target). */
    public static boolean isEnabled(Context ctx) {
        if (ctx == null) return false;
        return SharedPrefsData.getBool(
                ctx.getApplicationContext(),
                Constants.FESTIVAL_SPLASH_ENABLED,
                Constants.PREF_NAME,
                false);
    }

    /** Show when opening from a push notification. Default OFF. */
    public static boolean shouldShowOnPush(Context ctx) {
        if (ctx == null) return false;
        return SharedPrefsData.getBool(
                ctx.getApplicationContext(),
                Constants.FESTIVAL_SPLASH_SHOW_ON_PUSH,
                Constants.PREF_NAME,
                false);
    }

    /** Show when opening from share / App Link / job deep link. Default OFF. */
    public static boolean shouldShowOnShare(Context ctx) {
        if (ctx == null) return false;
        return SharedPrefsData.getBool(
                ctx.getApplicationContext(),
                Constants.FESTIVAL_SPLASH_SHOW_ON_SHARE,
                Constants.PREF_NAME,
                false);
    }

    /** True when any launch path may show the banner (prefetch gate). */
    public static boolean isAnyShowPathEnabled(Context ctx) {
        return isEnabled(ctx) || shouldShowOnPush(ctx) || shouldShowOnShare(ctx);
    }

    public static int getDurationMs(Context ctx) {
        if (ctx == null) return DEFAULT_DURATION_SEC * 1000;
        int sec = SharedPrefsData.getInt(
                ctx.getApplicationContext(),
                Constants.FESTIVAL_SPLASH_DURATION_SECONDS,
                Constants.PREF_NAME);
        if (sec < MIN_DURATION_SEC) sec = DEFAULT_DURATION_SEC;
        if (sec > MAX_DURATION_SEC) sec = MAX_DURATION_SEC;
        return sec * 1000;
    }

    public static String getImageUrl(Context ctx) {
        if (ctx == null) return "";
        String url = SharedPrefsData.getString(
                ctx.getApplicationContext(),
                Constants.FESTIVAL_SPLASH_IMAGE_URL,
                Constants.PREF_NAME);
        if (url == null || "null".equalsIgnoreCase(url)) return "";
        return url.trim();
    }

    public static File getLocalCacheFile(Context ctx) {
        if (ctx == null) return null;
        return new File(ctx.getApplicationContext().getFilesDir(), LOCAL_NAME);
    }

    /** True when a non-empty local cache exists for the current URL. */
    public static boolean hasReadyLocalImage(Context ctx) {
        File f = getLocalCacheFile(ctx);
        if (f == null || !f.exists() || f.length() < 64) return false;
        String url = getImageUrl(ctx);
        if (url.isEmpty()) return false;
        String cachedUrl = SharedPrefsData.getString(
                ctx.getApplicationContext(),
                Constants.FESTIVAL_SPLASH_CACHED_URL,
                Constants.PREF_NAME);
        return url.equals(cachedUrl);
    }

    /**
     * Sync-decode the cached banner so the first activity frame can show it
     * (avoids a flash of the default logo splash before {@code setImageURI} finishes).
     */
    public static Bitmap decodeLocalBitmap(Context ctx) {
        if (!hasReadyLocalImage(ctx)) return null;
        File f = getLocalCacheFile(ctx);
        if (f == null || !f.exists()) return null;
        try {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
            return BitmapFactory.decodeFile(f.getAbsolutePath(), opts);
        } catch (Exception e) {
            Log.w(TAG, "decodeLocalBitmap failed: "
                    + (e.getMessage() != null ? e.getMessage() : e));
            return null;
        }
    }

    /**
     * Persist splash/session fields. Always updates prefs (including OFF)
     * so the next cold start respects admin without blocking OpenPath.
     */
    public static void saveFromServer(
            Context ctx,
            Boolean enabled,
            Integer durationSeconds,
            String imageUrl) {
        saveFromServer(ctx, enabled, null, null, durationSeconds, imageUrl);
    }

    public static void saveFromServer(
            Context ctx,
            Boolean enabled,
            Boolean showOnPush,
            Boolean showOnShare,
            Integer durationSeconds,
            String imageUrl) {
        if (ctx == null) return;
        Context app = ctx.getApplicationContext();
        if (enabled != null) {
            SharedPrefsData.putBool(
                    app, Constants.FESTIVAL_SPLASH_ENABLED, enabled, Constants.PREF_NAME);
        }
        if (showOnPush != null) {
            SharedPrefsData.putBool(
                    app, Constants.FESTIVAL_SPLASH_SHOW_ON_PUSH, showOnPush, Constants.PREF_NAME);
        }
        if (showOnShare != null) {
            SharedPrefsData.putBool(
                    app, Constants.FESTIVAL_SPLASH_SHOW_ON_SHARE, showOnShare, Constants.PREF_NAME);
        }
        if (durationSeconds != null && durationSeconds > 0) {
            int sec = Math.max(MIN_DURATION_SEC, Math.min(MAX_DURATION_SEC, durationSeconds));
            SharedPrefsData.putInt(
                    app, Constants.FESTIVAL_SPLASH_DURATION_SECONDS, sec, Constants.PREF_NAME);
        }
        if (imageUrl != null) {
            String url = imageUrl.trim();
            SharedPrefsData.putString(
                    app, Constants.FESTIVAL_SPLASH_IMAGE_URL, url, Constants.PREF_NAME);
        }
    }

    /** Background download into local filesDir when any show path + URL present. */
    public static void prefetchInBackground(Context ctx) {
        if (ctx == null) return;
        final Context app = ctx.getApplicationContext();
        if (!isAnyShowPathEnabled(app)) return;
        final String url = getImageUrl(app);
        if (url.isEmpty()) return;
        if (hasReadyLocalImage(app)) return;
        new Thread(() -> {
            try {
                Future<File> future = Glide.with(app)
                        .downloadOnly()
                        .load(url)
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                        .submit();
                File tmp = future.get();
                if (tmp == null || !tmp.exists()) return;
                File dest = getLocalCacheFile(app);
                if (dest == null) return;
                copyFile(tmp, dest);
                SharedPrefsData.putString(
                        app, Constants.FESTIVAL_SPLASH_CACHED_URL, url, Constants.PREF_NAME);
                Log.i(TAG, "prefetch ok bytes=" + dest.length());
            } catch (Exception e) {
                Log.w(TAG, "prefetch failed: " + (e.getMessage() != null ? e.getMessage() : e));
            }
        }, "festival-splash-prefetch").start();
    }

    private static void copyFile(File src, File dest) throws Exception {
        try (InputStream in = new java.io.FileInputStream(src);
             FileOutputStream out = new FileOutputStream(dest)) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) >= 0) {
                out.write(buf, 0, n);
            }
            out.getFD().sync();
        }
    }
}
