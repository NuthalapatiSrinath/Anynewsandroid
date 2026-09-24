package com.anyNews.anynews.Utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.CRC32;

/**
 * Opening Banner / festival splash — admin toggles + duration + banner.
 * Paths: normal ({@link #isEnabled}), push ({@link #shouldShowOnPush}),
 * share/deep-link ({@link #shouldShowOnShare}). All default OFF.
 * Prefers a local file under filesDir so open never waits on the network.
 * Display uses FIT_XY stretch fill (entire image visible, all four edges meet
 * the screen — no centerCrop cutting, no letterbox bars).
 * When ON + image URL: suppress the default orange logo splash entirely.
 * Cache: server sends ?v=&lt;updated_at&gt;; URL/version/content-hash change
 * clears local file. Downloads use HttpURLConnection with caches disabled —
 * OCI serves festival_splash.png with max-age=604800 so Glide HTTP cache was
 * poisoning replaces when the path stayed the same.
 */
public final class FestivalSplashHelper {
    private static final String TAG = "FestivalSplash";
    private static final String LOCAL_NAME = "festival_splash_cache.bin";
    private static final int DEFAULT_DURATION_SEC = 2;
    private static final int MIN_DURATION_SEC = 1;
    private static final int MAX_DURATION_SEC = 8;
    /** Max wait on OpenPath when banner ON but cache cold (better than logo flash). */
    public static final long ENSURE_TIMEOUT_MS = 8_000L;
    private static final int CONNECT_TIMEOUT_MS = 6_000;
    private static final int READ_TIMEOUT_MS = 8_000;

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

    /**
     * Admin wants Opening Banner for this cold start (path toggle ON + image URL).
     * Used to keep a blank black system splash so the orange logo never paints.
     */
    public static boolean shouldSuppressLogoSplash(Context ctx, Intent launchIntent) {
        if (ctx == null) return false;
        if (getImageUrl(ctx).isEmpty()) return false;
        return shouldShowForLaunchIntent(ctx, launchIntent);
    }

    /** Path toggle for this Intent (push / share / normal) without Activity fields. */
    public static boolean shouldShowForLaunchIntent(Context ctx, Intent intent) {
        if (ctx == null) return false;
        if (isPushLaunchIntent(intent)) {
            return shouldShowOnPush(ctx);
        }
        if (isShareOrDeepLinkLaunchIntent(intent)) {
            return shouldShowOnShare(ctx);
        }
        return isEnabled(ctx);
    }

    public static boolean isPushLaunchIntent(Intent intent) {
        if (intent == null) return false;
        try {
            String title = intent.getStringExtra("TITILE");
            if (title == null || title.isEmpty() || "null".equalsIgnoreCase(title)) {
                title = intent.getStringExtra("TITLE");
            }
            if (title != null && !title.isEmpty() && !"null".equalsIgnoreCase(title)
                    && !"0".equals(title)) {
                return true;
            }
            String rewards = intent.getStringExtra("TITLE_REWARDS");
            if (rewards != null && !rewards.isEmpty() && !"null".equalsIgnoreCase(rewards)
                    && !"0".equals(rewards)) {
                return true;
            }
            Uri data = intent.getData();
            if (data != null) {
                String host = data.getHost();
                if (host != null && "push".equalsIgnoreCase(host)) {
                    return true;
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    public static boolean isShareOrDeepLinkLaunchIntent(Intent intent) {
        if (intent == null || isPushLaunchIntent(intent)) return false;
        try {
            Uri data = intent.getData();
            if (data != null) {
                String scheme = data.getScheme();
                String host = data.getHost();
                if (host != null && "push".equalsIgnoreCase(host)) {
                    return false;
                }
                if (scheme != null && ("http".equalsIgnoreCase(scheme)
                        || "https".equalsIgnoreCase(scheme)
                        || "anynews".equalsIgnoreCase(scheme))) {
                    return true;
                }
            }
            String action = intent.getAction();
            if (Intent.ACTION_VIEW.equals(action) && intent.getData() != null) {
                return true;
            }
        } catch (Exception ignored) {
        }
        return false;
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

    public static long getUpdatedAt(Context ctx) {
        if (ctx == null) return 0L;
        return SharedPrefsData.getLong(
                ctx.getApplicationContext(),
                Constants.FESTIVAL_SPLASH_UPDATED_AT,
                Constants.PREF_NAME);
    }

    public static File getLocalCacheFile(Context ctx) {
        if (ctx == null) return null;
        return new File(ctx.getApplicationContext().getFilesDir(), LOCAL_NAME);
    }

    /** Extract cache-bust token from URL (?v= / &amp;v= / t= / ver=). */
    public static String extractUrlVersion(String url) {
        if (url == null || url.isEmpty()) return "";
        try {
            Uri u = Uri.parse(url);
            String v = u.getQueryParameter("v");
            if (v == null || v.isEmpty()) v = u.getQueryParameter("t");
            if (v == null || v.isEmpty()) v = u.getQueryParameter("ver");
            return v != null ? v.trim() : "";
        } catch (Exception e) {
            return "";
        }
    }

    /** True when a non-empty local cache exists for the current URL (+ version). */
    public static boolean hasReadyLocalImage(Context ctx) {
        File f = getLocalCacheFile(ctx);
        if (f == null || !f.exists() || f.length() < 64) return false;
        String url = getImageUrl(ctx);
        if (url.isEmpty()) return false;
        Context app = ctx.getApplicationContext();
        String cachedUrl = SharedPrefsData.getString(
                app, Constants.FESTIVAL_SPLASH_CACHED_URL, Constants.PREF_NAME);
        if (cachedUrl == null || !url.equals(cachedUrl)) return false;
        long updatedAt = getUpdatedAt(app);
        long cachedAt = SharedPrefsData.getLong(
                app, Constants.FESTIVAL_SPLASH_CACHED_UPDATED_AT, Constants.PREF_NAME);
        // Server version must match local arm — including legacy caches with
        // cachedAt=0 after admin replace (same path, new updated_at / ?v=).
        if (updatedAt > 0 && updatedAt != cachedAt) {
            return false;
        }
        // URL ?v= changed even when updated_at pref lagged / was 0.
        String urlVer = extractUrlVersion(url);
        String cachedVer = SharedPrefsData.getString(
                app, Constants.FESTIVAL_SPLASH_CACHED_URL_VERSION, Constants.PREF_NAME);
        if (urlVer.length() > 0) {
            if (cachedVer == null || !urlVer.equals(cachedVer)) {
                return false;
            }
        }
        return true;
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

    /** Delete local banner file + clear cached URL arm (forces re-download). */
    public static void invalidateLocalCache(Context ctx) {
        if (ctx == null) return;
        Context app = ctx.getApplicationContext();
        try {
            File f = getLocalCacheFile(app);
            if (f != null && f.exists()) {
                //noinspection ResultOfMethodCallIgnored
                f.delete();
            }
            File part = new File(app.getFilesDir(), LOCAL_NAME + ".part");
            if (part.exists()) {
                //noinspection ResultOfMethodCallIgnored
                part.delete();
            }
        } catch (Exception ignored) {
        }
        SharedPrefsData.putString(
                app, Constants.FESTIVAL_SPLASH_CACHED_URL, "", Constants.PREF_NAME);
        SharedPrefsData.putLong(
                app, Constants.FESTIVAL_SPLASH_CACHED_UPDATED_AT, 0L, Constants.PREF_NAME);
        SharedPrefsData.putString(
                app, Constants.FESTIVAL_SPLASH_CACHED_URL_VERSION, "", Constants.PREF_NAME);
        SharedPrefsData.putString(
                app, Constants.FESTIVAL_SPLASH_CACHED_CONTENT_HASH, "", Constants.PREF_NAME);
    }

    /**
     * Persist splash/session fields. Always updates prefs (including OFF)
     * so the next cold start respects admin without blocking OpenPath.
     * When image URL, ?v=, or updated_at changes, clears stale local arm.
     */
    public static void saveFromServer(
            Context ctx,
            Boolean enabled,
            Integer durationSeconds,
            String imageUrl) {
        saveFromServer(ctx, enabled, null, null, durationSeconds, imageUrl, null);
    }

    public static void saveFromServer(
            Context ctx,
            Boolean enabled,
            Boolean showOnPush,
            Boolean showOnShare,
            Integer durationSeconds,
            String imageUrl) {
        saveFromServer(ctx, enabled, showOnPush, showOnShare, durationSeconds, imageUrl, null);
    }

    public static void saveFromServer(
            Context ctx,
            Boolean enabled,
            Boolean showOnPush,
            Boolean showOnShare,
            Integer durationSeconds,
            String imageUrl,
            Long updatedAt) {
        if (ctx == null) return;
        Context app = ctx.getApplicationContext();
        final String prevUrl = getImageUrl(app);
        final long prevUpdated = getUpdatedAt(app);
        final String prevVer = extractUrlVersion(prevUrl);
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
        if (updatedAt != null && updatedAt >= 0L) {
            SharedPrefsData.putLong(
                    app, Constants.FESTIVAL_SPLASH_UPDATED_AT, updatedAt, Constants.PREF_NAME);
        }
        final String nextUrl = getImageUrl(app);
        final long nextUpdated = getUpdatedAt(app);
        final String nextVer = extractUrlVersion(nextUrl);
        boolean urlChanged = imageUrl != null && !nextUrl.equals(prevUrl);
        boolean versionChanged =
                updatedAt != null && updatedAt > 0L && nextUpdated != prevUpdated;
        boolean queryVerChanged =
                nextVer.length() > 0 && !nextVer.equals(prevVer);
        // Server healed from updated_at=0 → positive while URL path unchanged.
        boolean healedFromZero =
                prevUpdated <= 0L && nextUpdated > 0L && !nextUrl.isEmpty();
        if (urlChanged || versionChanged || queryVerChanged || healedFromZero) {
            Log.i(TAG, "settings changed — invalidate local cache"
                    + " urlChanged=" + urlChanged
                    + " versionChanged=" + versionChanged
                    + " queryVerChanged=" + queryVerChanged
                    + " healedFromZero=" + healedFromZero
                    + " nextVer=" + nextVer
                    + " nextUpdated=" + nextUpdated);
            invalidateLocalCache(app);
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
        new Thread(() -> downloadToCache(app, url), "festival-splash-prefetch").start();
    }

    /**
     * Ensure local cache exists for the current URL (blocking, off-main).
     * Call from a worker thread. Returns true when {@link #hasReadyLocalImage}.
     */
    public static boolean ensureLocalImageBlocking(Context ctx, long timeoutMs) {
        if (ctx == null) return false;
        final Context app = ctx.getApplicationContext();
        if (hasReadyLocalImage(app)) return true;
        final String url = getImageUrl(app);
        if (url.isEmpty()) return false;
        final CountDownLatch done = new CountDownLatch(1);
        final AtomicBoolean ok = new AtomicBoolean(false);
        Thread t = new Thread(() -> {
            try {
                ok.set(downloadToCache(app, url));
            } finally {
                done.countDown();
            }
        }, "festival-splash-ensure");
        t.start();
        try {
            long wait = timeoutMs > 0 ? timeoutMs : ENSURE_TIMEOUT_MS;
            if (!done.await(wait, TimeUnit.MILLISECONDS)) {
                Log.w(TAG, "ensureLocalImageBlocking timeout ms=" + wait);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return ok.get() || hasReadyLocalImage(app);
    }

    /**
     * Download on a worker then invoke {@code onMain} on the main thread.
     * Caller should check {@link #hasReadyLocalImage}; fail-open after timeout.
     */
    public static void ensureLocalImageAsync(
            Context ctx, long timeoutMs, final Runnable onMain) {
        if (onMain == null) return;
        if (ctx == null) {
            onMain.run();
            return;
        }
        final Context app = ctx.getApplicationContext();
        final android.os.Handler main = new android.os.Handler(android.os.Looper.getMainLooper());
        if (hasReadyLocalImage(app) || getImageUrl(app).isEmpty()) {
            main.post(onMain);
            return;
        }
        final AtomicBoolean posted = new AtomicBoolean(false);
        final Runnable once = () -> {
            if (posted.compareAndSet(false, true)) {
                onMain.run();
            }
        };
        final long wait = timeoutMs > 0 ? timeoutMs : ENSURE_TIMEOUT_MS;
        new Thread(() -> {
            downloadToCache(app, getImageUrl(app));
            main.post(once);
        }, "festival-splash-ensure-async").start();
        main.postDelayed(once, wait);
    }

    /**
     * Direct HTTP download — never Glide/OkHttp disk cache. OCI max-age=7d would
     * otherwise keep serving the previous festival_splash.png bytes after replace.
     */
    private static boolean downloadToCache(Context app, String url) {
        HttpURLConnection conn = null;
        InputStream in = null;
        FileOutputStream out = null;
        try {
            if (url == null || url.trim().isEmpty()) return false;
            if (hasReadyLocalImage(app)) return true;
            // Prefer busted URL; if server omitted ?v=, append updated_at ourselves.
            String fetchUrl = url.trim();
            long updatedAt = getUpdatedAt(app);
            if (extractUrlVersion(fetchUrl).isEmpty() && updatedAt > 0L) {
                fetchUrl = fetchUrl
                        + (fetchUrl.contains("?") ? "&" : "?")
                        + "v=" + updatedAt;
            }
            Log.i(TAG, "download start url=" + fetchUrl);
            URL u = new URL(fetchUrl);
            conn = (HttpURLConnection) u.openConnection();
            conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
            conn.setReadTimeout(READ_TIMEOUT_MS);
            conn.setInstanceFollowRedirects(true);
            conn.setUseCaches(false);
            conn.setRequestProperty("Cache-Control", "no-cache, no-store");
            conn.setRequestProperty("Pragma", "no-cache");
            conn.connect();
            int code = conn.getResponseCode();
            if (code < 200 || code >= 300) {
                Log.w(TAG, "download HTTP " + code);
                return false;
            }
            File dest = getLocalCacheFile(app);
            if (dest == null) return false;
            File part = new File(dest.getAbsolutePath() + ".part");
            if (part.exists()) {
                //noinspection ResultOfMethodCallIgnored
                part.delete();
            }
            CRC32 crc = new CRC32();
            long bytes = 0L;
            in = new BufferedInputStream(conn.getInputStream());
            out = new FileOutputStream(part);
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) >= 0) {
                out.write(buf, 0, n);
                crc.update(buf, 0, n);
                bytes += n;
            }
            out.getFD().sync();
            out.close();
            out = null;
            if (bytes < 64) {
                //noinspection ResultOfMethodCallIgnored
                part.delete();
                Log.w(TAG, "download too small bytes=" + bytes);
                return false;
            }
            if (dest.exists()) {
                //noinspection ResultOfMethodCallIgnored
                dest.delete();
            }
            if (!part.renameTo(dest)) {
                copyFile(part, dest);
                //noinspection ResultOfMethodCallIgnored
                part.delete();
            }
            String contentHash = Long.toHexString(crc.getValue()) + ":" + bytes;
            SharedPrefsData.putString(
                    app, Constants.FESTIVAL_SPLASH_CACHED_URL, getImageUrl(app), Constants.PREF_NAME);
            SharedPrefsData.putLong(
                    app,
                    Constants.FESTIVAL_SPLASH_CACHED_UPDATED_AT,
                    getUpdatedAt(app),
                    Constants.PREF_NAME);
            SharedPrefsData.putString(
                    app,
                    Constants.FESTIVAL_SPLASH_CACHED_URL_VERSION,
                    extractUrlVersion(getImageUrl(app)),
                    Constants.PREF_NAME);
            SharedPrefsData.putString(
                    app,
                    Constants.FESTIVAL_SPLASH_CACHED_CONTENT_HASH,
                    contentHash,
                    Constants.PREF_NAME);
            Log.i(TAG, "prefetch ok bytes=" + bytes
                    + " hash=" + contentHash
                    + " ver=" + extractUrlVersion(getImageUrl(app))
                    + " updatedAt=" + getUpdatedAt(app));
            return true;
        } catch (Exception e) {
            Log.w(TAG, "prefetch failed: " + (e.getMessage() != null ? e.getMessage() : e));
            return false;
        } finally {
            try {
                if (out != null) out.close();
            } catch (Exception ignored) {
            }
            try {
                if (in != null) in.close();
            } catch (Exception ignored) {
            }
            if (conn != null) {
                try {
                    conn.disconnect();
                } catch (Exception ignored) {
                }
            }
        }
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
