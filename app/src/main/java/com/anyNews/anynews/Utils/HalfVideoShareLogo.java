package com.anyNews.anynews.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Admin half-video share logo. The URL rides splash and the article feed
 * config the app already stores. The image is downloaded once when that
 * config arrives, not on each share. Empty or missing URL keeps
 * {@code R.drawable.app_share_logo}.
 */
public final class HalfVideoShareLogo {

    private static final OkHttpClient CLIENT = new OkHttpClient.Builder().build();
    private static final Object LOCK = new Object();

    private HalfVideoShareLogo() {
    }

    /**
     * @param url null when the payload omitted the field (keep the last value).
     *            Empty string means the admin has no upload, so use the built-in logo.
     */
    public static void saveFromServer(Context context, String url) {
        if (context == null || url == null) {
            return;
        }
        final Context app = context.getApplicationContext();
        final String next = sanitize(url);
        String prev = SharedPrefsData.getString(app, Constants.HALF_VIDEO_SHARE_LOGO_URL, Constants.PREF_NAME);
        if (prev == null) {
            prev = "";
        }
        if (next.equals(prev) && (next.isEmpty() || readyFile(app) != null)) {
            return;
        }
        SharedPrefsData.putStringImmediate(app, Constants.HALF_VIDEO_SHARE_LOGO_URL, next, Constants.PREF_NAME);
        if (next.isEmpty()) {
            SharedPrefsData.putStringImmediate(app, Constants.HALF_VIDEO_SHARE_LOGO_READY, "", Constants.PREF_NAME);
            return;
        }
        new Thread(() -> download(app, next), "half-share-logo").start();
    }

    /** Downloaded logo for the current admin URL, or null to use the built-in mark. */
    public static File readyFile(Context context) {
        if (context == null) {
            return null;
        }
        String url = SharedPrefsData.getString(context, Constants.HALF_VIDEO_SHARE_LOGO_URL, Constants.PREF_NAME);
        String ready = SharedPrefsData.getString(context, Constants.HALF_VIDEO_SHARE_LOGO_READY, Constants.PREF_NAME);
        if (url == null || url.trim().isEmpty() || ready == null || !url.equals(ready)) {
            return null;
        }
        File file = cacheFile(context, url);
        if (file.exists() && file.length() > 32L) {
            return file;
        }
        return null;
    }

    /**
     * {@code builtin} keeps the existing branded-video cache. Any other token
     * means a downloaded admin logo and a separate cache file.
     */
    public static String cacheToken(Context context) {
        File file = readyFile(context);
        if (file == null) {
            return "builtin";
        }
        String url = SharedPrefsData.getString(context, Constants.HALF_VIDEO_SHARE_LOGO_URL, Constants.PREF_NAME);
        if (url == null || url.isEmpty()) {
            return "builtin";
        }
        return Integer.toHexString(url.hashCode());
    }

    private static void download(Context context, String url) {
        File dest = cacheFile(context, url);
        File part = new File(dest.getAbsolutePath() + ".part");
        synchronized (LOCK) {
            String current = SharedPrefsData.getString(
                    context, Constants.HALF_VIDEO_SHARE_LOGO_URL, Constants.PREF_NAME);
            if (current == null || !current.equals(url)) {
                return;
            }
            if (dest.exists() && dest.length() > 32L
                    && url.equals(SharedPrefsData.getString(
                    context, Constants.HALF_VIDEO_SHARE_LOGO_READY, Constants.PREF_NAME))) {
                return;
            }
            //noinspection ResultOfMethodCallIgnored
            part.delete();
            try {
                Request request = new Request.Builder().url(url).build();
                try (Response response = CLIENT.newCall(request).execute()) {
                    if (!response.isSuccessful() || response.body() == null) {
                        return;
                    }
                    try (InputStream in = response.body().byteStream();
                         FileOutputStream out = new FileOutputStream(part)) {
                        byte[] buffer = new byte[8192];
                        int read;
                        while ((read = in.read(buffer)) != -1) {
                            out.write(buffer, 0, read);
                        }
                        out.flush();
                    }
                }
                if (!part.exists() || part.length() <= 32L) {
                    //noinspection ResultOfMethodCallIgnored
                    part.delete();
                    return;
                }
                if (dest.exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    dest.delete();
                }
                if (!part.renameTo(dest)) {
                    //noinspection ResultOfMethodCallIgnored
                    part.delete();
                    return;
                }
                Bitmap probe = BitmapFactory.decodeFile(dest.getAbsolutePath());
                if (probe == null) {
                    //noinspection ResultOfMethodCallIgnored
                    dest.delete();
                    return;
                }
                probe.recycle();
                current = SharedPrefsData.getString(
                        context, Constants.HALF_VIDEO_SHARE_LOGO_URL, Constants.PREF_NAME);
                if (url.equals(current)) {
                    SharedPrefsData.putStringImmediate(
                            context, Constants.HALF_VIDEO_SHARE_LOGO_READY, url, Constants.PREF_NAME);
                }
            } catch (Exception ignored) {
                //noinspection ResultOfMethodCallIgnored
                part.delete();
            }
        }
    }

    private static File cacheFile(Context context, String url) {
        File dir = context.getExternalCacheDir();
        if (dir == null) {
            dir = context.getCacheDir();
        }
        File folder = new File(dir, "half_video_wm");
        //noinspection ResultOfMethodCallIgnored
        folder.mkdirs();
        return new File(folder, "share_logo_" + Integer.toHexString(url.hashCode()) + ".img");
    }

    private static String sanitize(String raw) {
        String value = raw == null ? "" : raw.trim();
        if (value.isEmpty() || value.length() > 800) {
            return "";
        }
        String lower = value.toLowerCase();
        if (!lower.startsWith("https://") && !lower.startsWith("http://")) {
            return "";
        }
        return value;
    }
}
