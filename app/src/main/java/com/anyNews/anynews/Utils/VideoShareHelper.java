package com.anyNews.anynews.Utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.anyNews.anynews.R;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class VideoShareHelper {

    public enum ShareMode {
        GENERAL,
        WHATSAPP_CHAT,
        WHATSAPP_STATUS
    }

    public interface OnShareCompleteListener {
        /** Intent launched / chooser opened — unlock UI only (not a counted share). */
        void onShareStarted();

        /**
         * User picked a target from the share sheet, or WhatsApp returned RESULT_OK
         * after Send. Opening WhatsApp does not call this. Default no-op for
         * callers that only unlock UI.
         */
        default void onShareConfirmed() {
        }
    }

    private static final OkHttpClient CLIENT = new OkHttpClient.Builder().build();
    private static final ConcurrentHashMap<String, Object> DOWNLOAD_LOCKS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Boolean> STAMP_RUNNING = new ConcurrentHashMap<>();
    /** How long a share will wait for a logo that is already being stamped. */
    private static final long HALF_STAMP_WAIT_MS = 700L;
    private static final String[] WHATSAPP_PACKAGES = {
            "com.whatsapp",
            "com.whatsapp.w4b"
    };
    private static ProgressDialog progressDialog;
    private static volatile Thread downloadThread;

    public static void shareVideo(Context context, Activity activity, String videoUrl, String title,
                                    String shareLink, ShareMode mode, OnShareCompleteListener listener) {
        shareVideo(context, activity, videoUrl, null, title, null, shareLink, mode,
                context.getString(R.string.video_status_sharing), listener);
    }

    public static void shareVideo(Context context, Activity activity, String videoUrl, String title,
                                    String description, String shareLink, ShareMode mode,
                                    OnShareCompleteListener listener) {
        shareVideo(context, activity, videoUrl, null, title, description, shareLink, mode,
                context.getString(R.string.video_status_sharing), listener);
    }

    public static void shareVideo(Context context, Activity activity, String videoUrl, String title,
                                    String shareLink, ShareMode mode, String statusMessage,
                                    OnShareCompleteListener listener) {
        shareVideo(context, activity, videoUrl, null, title, null, shareLink, mode, statusMessage, listener);
    }

    /**
     * Prefer any already-cached candidate ({@code primaryUrl} usually downloadUrl,
     * {@code alternateUrl} the play URL) so WhatsApp opens without a second download.
     */
    public static void shareVideo(Context context, Activity activity, String primaryUrl,
                                    String alternateUrl, String title, String description,
                                    String shareLink, ShareMode mode,
                                    OnShareCompleteListener listener) {
        shareVideo(context, activity, primaryUrl, alternateUrl, title, description, shareLink, mode,
                context.getString(R.string.video_status_sharing), listener);
    }

    public static void shareVideo(Context context, Activity activity, String primaryUrl,
                                    String alternateUrl, String title, String description,
                                    String shareLink, ShareMode mode, String statusMessage,
                                    OnShareCompleteListener listener) {
        sharePrepared(context, activity, primaryUrl, alternateUrl, title, description, shareLink,
                mode, statusMessage, listener, false);
    }

    /**
     * Half-article video share. The file sent to WhatsApp / other apps carries
     * {@code app_share_logo}. Full-screen videos keep {@link #shareVideo}.
     */
    public static void shareHalfArticleVideo(Context context, Activity activity, String primaryUrl,
                                              String alternateUrl, String title, String description,
                                              String shareLink, ShareMode mode,
                                              OnShareCompleteListener listener) {
        sharePrepared(context, activity, primaryUrl, alternateUrl, title, description, shareLink,
                mode, context.getString(R.string.video_status_sharing), listener, true);
    }

    /**
     * Saves a half-article video into Downloads with the share logo.
     * If the logo cannot be added, the original file is still saved.
     * No logo-status dialog or toast is shown.
     */
    public static void saveHalfArticleVideo(Context context, Activity activity, String primaryUrl,
                                             String alternateUrl, String displayName) {
        if (context == null) {
            return;
        }
        final String[] candidates = uniqueUrls(primaryUrl, alternateUrl);
        if (candidates.length == 0) {
            Toast.makeText(context, "Video is not available to download", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(context, "Your video is downloading", Toast.LENGTH_LONG).show();
        final Handler mainHandler = new Handler(Looper.getMainLooper());
        final String logoKey = candidates[0];
        mainHandler.post(() -> showProgress(context, context.getString(R.string.video_status_downloading)));
        downloadThread = new Thread(() -> {
            try {
                BrandedFile branded = resolveHalfVideoFile(context, candidates, logoKey,
                        context.getString(R.string.video_status_downloading), mainHandler, true, true);
                if (branded == null || branded.file == null) {
                    mainHandler.post(() -> {
                        dismissProgress();
                        Toast.makeText(context, "Unable to download video", Toast.LENGTH_LONG).show();
                    });
                    return;
                }
                String savedName = displayName;
                if (!branded.branded) {
                    String ext = getExtension(branded.file.getName());
                    if (savedName != null && !savedName.toLowerCase().endsWith("." + ext)) {
                        int dot = savedName.lastIndexOf('.');
                        savedName = (dot > 0 ? savedName.substring(0, dot) : savedName) + "." + ext;
                    }
                } else if (savedName != null && !savedName.toLowerCase().endsWith(".mp4")) {
                    int dot = savedName.lastIndexOf('.');
                    savedName = (dot > 0 ? savedName.substring(0, dot) : savedName) + ".mp4";
                }
                Uri saved = exportToDownloads(context.getApplicationContext(), branded.file, savedName);
                final String finalName = savedName;
                mainHandler.post(() -> {
                    dismissProgress();
                    VideoDownloadConfirmHelper.showSavedVideo(context, saved, finalName);
                });
            } catch (InterruptedException e) {
                mainHandler.post(VideoShareHelper::dismissProgress);
            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> {
                    dismissProgress();
                    Toast.makeText(context, "Unable to download video", Toast.LENGTH_LONG).show();
                });
            }
        }, "half-video-download");
        downloadThread.start();
    }

    private static void sharePrepared(Context context, Activity activity, String primaryUrl,
                                      String alternateUrl, String title, String description,
                                      String shareLink, ShareMode mode, String statusMessage,
                                      OnShareCompleteListener listener, boolean halfArticleLogo) {
        final String[] candidates = uniqueUrls(primaryUrl, alternateUrl);
        if (candidates.length == 0) {
            Toast.makeText(context, "Video is not available to share", Toast.LENGTH_SHORT).show();
            if (listener != null) {
                listener.onShareStarted();
            }
            return;
        }

        final Handler mainHandler = new Handler(Looper.getMainLooper());
        final String logoKey = candidates[0];

        if (halfArticleLogo) {
            // Branded file if the stamp already finished; otherwise the local
            // video right now. Do not wait on a stamp that is still running.
            if (launchReadyHalfShare(context, activity, candidates, logoKey, title, description,
                    shareLink, mode, listener)) {
                return;
            }
        } else {
            // Instant path: any local/cache hit — skip worker thread + dialog.
            PreparedShare ready = tryResolveReadyShare(context, candidates);
            if (ready != null) {
                launchShareIntent(context, activity, ready.uri,
                        videoShareMime(ready.file, ready.mimeType), title, description,
                        shareLink, mode, listener);
                if (listener != null) {
                    listener.onShareStarted();
                }
                return;
            }
        }

        downloadThread = new Thread(() -> {
            try {
                Uri shareUri;
                String mime;
                if (halfArticleLogo) {
                    BrandedFile branded = resolveHalfVideoFile(context, candidates, logoKey,
                            statusMessage, mainHandler, true, false);
                    if (branded == null || branded.file == null || branded.file.length() <= 0) {
                        if (Thread.currentThread().isInterrupted()) {
                            throw new InterruptedException();
                        }
                        mainHandler.post(() -> {
                            dismissProgress();
                            Toast.makeText(context, "Unable to prepare video for sharing", Toast.LENGTH_SHORT).show();
                            if (listener != null) {
                                listener.onShareStarted();
                            }
                        });
                        return;
                    }
                    shareUri = uriForShareFile(context, branded.file);
                    mime = branded.branded ? "video/mp4"
                            : videoShareMime(branded.file, resolveMimeType(branded.file, branded.file.getName()));
                } else {
                    PreparedShare prepared = null;
                    for (String candidate : candidates) {
                        prepared = prepareShareableVideo(context, candidate, statusMessage, mainHandler, true);
                        if (prepared != null) {
                            break;
                        }
                    }
                    if (prepared == null || prepared.uri == null) {
                        if (Thread.currentThread().isInterrupted()) {
                            throw new InterruptedException();
                        }
                        mainHandler.post(() -> {
                            dismissProgress();
                            Toast.makeText(context, "Unable to prepare video for sharing", Toast.LENGTH_SHORT).show();
                            if (listener != null) {
                                listener.onShareStarted();
                            }
                        });
                        return;
                    }
                    shareUri = prepared.uri;
                    mime = videoShareMime(prepared.file, prepared.mimeType);
                }
                final Uri doneUri = shareUri;
                final String doneMime = mime;
                mainHandler.post(() -> {
                    dismissProgress();
                    launchShareIntent(context, activity, doneUri, doneMime, title, description,
                            shareLink, mode, listener);
                    if (listener != null) {
                        listener.onShareStarted();
                    }
                });
            } catch (InterruptedException e) {
                mainHandler.post(() -> {
                    dismissProgress();
                    if (listener != null) {
                        listener.onShareStarted();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> {
                    dismissProgress();
                    Toast.makeText(context, "Unable to prepare video for sharing", Toast.LENGTH_SHORT).show();
                    if (listener != null) {
                        listener.onShareStarted();
                    }
                });
            }
        }, "video-share");
        downloadThread.start();
    }

    /** Non-blocking resolve for content/file/cache hits only (no network). */
    private static PreparedShare tryResolveReadyShare(Context context, String sourceUrl) {
        return tryResolveReadyShare(context, new String[]{sourceUrl});
    }

    private static PreparedShare tryResolveReadyShare(Context context, String[] candidates) {
        if (candidates == null) {
            return null;
        }
        for (String sourceUrl : candidates) {
            if (sourceUrl == null || sourceUrl.trim().isEmpty()) {
                continue;
            }
            PreparedShare hit = tryResolveReadyShareOne(context, sourceUrl.trim());
            if (hit != null) {
                return hit;
            }
        }
        return null;
    }

    private static PreparedShare tryResolveReadyShareOne(Context context, String sourceUrl) {
        try {
            Uri parsed = Uri.parse(sourceUrl);
            String scheme = parsed.getScheme();
            if (scheme != null && scheme.equalsIgnoreCase("content")) {
                return new PreparedShare(parsed, resolveMimeType(null, sourceUrl));
            }
            File localFile = resolveLocalFile(sourceUrl, parsed, scheme);
            if (localFile != null && localFile.exists() && localFile.length() > 0) {
                localFile.setReadable(true, false);
                Uri uri = uriForShareFile(context, localFile);
                return new PreparedShare(uri, resolveMimeType(localFile, sourceUrl), localFile);
            }
            File cachedFile = getCacheFile(context, sourceUrl);
            if (cachedFile.exists() && cachedFile.length() > 0) {
                cachedFile.setReadable(true, false);
                Uri uri = uriForShareFile(context, cachedFile);
                return new PreparedShare(uri, resolveMimeType(cachedFile, sourceUrl), cachedFile);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * Warm the share cache while the bottom sheet is open / video is playing so Contact is instant.
     */
    public static void prefetchVideo(Context context, String videoUrl) {
        prefetchVideoCandidates(context, videoUrl, null);
    }

    public static void prefetchVideoCandidates(Context context, String primaryUrl, String alternateUrl) {
        if (context == null) {
            return;
        }
        final String[] candidates = uniqueUrls(primaryUrl, alternateUrl);
        if (candidates.length == 0) {
            return;
        }
        final Context appContext = context.getApplicationContext();
        for (String sourceUrl : candidates) {
            Uri parsed = Uri.parse(sourceUrl);
            String scheme = parsed.getScheme();
            if (scheme != null && (scheme.equalsIgnoreCase("content") || scheme.equalsIgnoreCase("file"))) {
                continue;
            }
            if (scheme == null && sourceUrl.startsWith("/")) {
                continue;
            }
            File cachedFile = getCacheFile(appContext, sourceUrl);
            if (cachedFile.exists() && cachedFile.length() > 0) {
                continue;
            }
            final String url = sourceUrl;
            new Thread(() -> {
                try {
                    prepareShareableVideo(appContext, url, null, new Handler(Looper.getMainLooper()), false);
                } catch (Exception ignored) {
                    // Prefetch is best-effort; share path will retry with UI if needed.
                }
            }, "video-share-prefetch").start();
        }
    }

    /**
     * Download and stamp a half-article video ahead of Share / Download so the
     * second tap reuses the cached file.
     */
    public static void prefetchHalfArticleVideo(Context context, String primaryUrl, String alternateUrl) {
        if (context == null) {
            return;
        }
        final String[] candidates = uniqueUrls(primaryUrl, alternateUrl);
        if (candidates.length == 0) {
            return;
        }
        final Context appContext = context.getApplicationContext();
        final String logoKey = candidates[0];
        if (HalfVideoLogoWatermark.peek(appContext, logoKey) != null) {
            return;
        }
        new Thread(() -> {
            try {
                resolveHalfVideoFile(appContext, candidates, logoKey, null,
                        new Handler(Looper.getMainLooper()), false, true);
            } catch (Exception ignored) {
                // Share and download retry without a logo-status message.
            }
        }, "half-video-prefetch").start();
    }

    private static final class BrandedFile {
        final File file;
        final boolean branded;
        final String fallback;

        BrandedFile(File file, boolean branded, String fallback) {
            this.file = file;
            this.branded = branded;
            this.fallback = fallback;
        }
    }

    /**
     * Local raw file, then the cached logo composite. Watermarking does not
     * show a progress dialog or any “adding logo” status.
     *
     * @param blockForLogo download/prefetch wait for the stamp. Share does not:
     *                     a slow or stuck stamp must not hold the send.
     */
    private static BrandedFile resolveHalfVideoFile(Context context, String[] candidates, String logoKey,
                                                     String downloadMessage,
                                                     Handler mainHandler, boolean allowProgress,
                                                     boolean blockForLogo) throws Exception {
        PreparedShare prepared = null;
        for (String candidate : candidates) {
            prepared = prepareShareableVideo(context, candidate, downloadMessage, mainHandler, allowProgress);
            if (prepared != null) {
                break;
            }
        }
        if (prepared == null) {
            return null;
        }
        File source = prepared.file;
        if (source == null) {
            source = copyUriToCache(context, prepared.uri, logoKey);
        }
        if (source == null || !source.exists() || source.length() <= 0) {
            return new BrandedFile(null, false,
                    "Original video kept because the file could not be read.");
        }
        if (!blockForLogo) {
            ensureHalfStamp(context, source, logoKey);
            File quick = awaitBranded(context, logoKey, HALF_STAMP_WAIT_MS);
            if (quick != null) {
                return new BrandedFile(quick, true, null);
            }
            return new BrandedFile(source, false,
                    "Original video kept because the ANYNEWS logo could not be added.");
        }
        HalfVideoLogoWatermark.Outcome outcome = HalfVideoLogoWatermark.apply(context, source, logoKey);
        if (outcome.branded && outcome.file != null) {
            return new BrandedFile(outcome.file, true, null);
        }
        return new BrandedFile(source, false, outcome.fallback);
    }

    private static File copyUriToCache(Context context, Uri uri, String logoKey) {
        if (uri == null) {
            return null;
        }
        File dest = new File(cacheParent(context), "half_src_" + Integer.toHexString(logoKey.hashCode()) + ".mp4");
        try (java.io.InputStream in = context.getContentResolver().openInputStream(uri);
             java.io.OutputStream out = new java.io.BufferedOutputStream(new java.io.FileOutputStream(dest))) {
            if (in == null) {
                return null;
            }
            byte[] buffer = new byte[1 << 16];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            out.flush();
            return dest.length() > 0 ? dest : null;
        } catch (Exception e) {
            //noinspection ResultOfMethodCallIgnored
            dest.delete();
            return null;
        }
    }

    private static File cacheParent(Context context) {
        File dir = context.getExternalCacheDir();
        if (dir == null) {
            dir = context.getCacheDir();
        }
        return dir;
    }

    private static Uri exportToDownloads(Context context, File source, String displayName) throws Exception {
        String name = (displayName == null || displayName.trim().isEmpty())
                ? "anyNews_" + System.currentTimeMillis() + ".mp4"
                : displayName.trim();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = context.getContentResolver();
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
            values.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
            values.put(MediaStore.MediaColumns.IS_PENDING, 1);
            Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            if (uri == null) {
                throw new java.io.IOException("Downloads insert failed");
            }
            try (java.io.InputStream in = new java.io.FileInputStream(source);
                 java.io.OutputStream out = resolver.openOutputStream(uri)) {
                if (out == null) {
                    throw new java.io.IOException("Downloads stream failed");
                }
                byte[] buffer = new byte[1 << 16];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                out.flush();
            } catch (Exception e) {
                resolver.delete(uri, null, null);
                throw e;
            }
            values.clear();
            values.put(MediaStore.MediaColumns.IS_PENDING, 0);
            resolver.update(uri, values, null, null);
            return uri;
        }
        File destDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        //noinspection ResultOfMethodCallIgnored
        destDir.mkdirs();
        File dest = new File(destDir, name);
        try (java.io.InputStream in = new java.io.FileInputStream(source);
             java.io.OutputStream out = new java.io.BufferedOutputStream(new java.io.FileOutputStream(dest))) {
            byte[] buffer = new byte[1 << 16];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            out.flush();
        }
        return FileProvider.getUriForFile(context, context.getPackageName() + ".provider", dest);
    }

    private static String[] uniqueUrls(String primary, String alternate) {
        java.util.LinkedHashSet<String> set = new java.util.LinkedHashSet<>();
        if (primary != null) {
            String p = primary.trim();
            if (!p.isEmpty()) {
                set.add(p);
            }
        }
        if (alternate != null) {
            String a = alternate.trim();
            if (!a.isEmpty()) {
                set.add(a);
            }
        }
        return set.toArray(new String[0]);
    }

    /**
     * Resolve a shareable video without re-downloading when a local/cached file already exists.
     * Shows a progress dialog only while a network download is in progress (when allowProgress is true).
     */
    private static PreparedShare prepareShareableVideo(Context context, String sourceUrl,
                                                         String statusMessage, Handler mainHandler,
                                                         boolean allowProgress)
            throws Exception {
        // content:// — share directly (no copy/download)
        Uri parsed = Uri.parse(sourceUrl);
        String scheme = parsed.getScheme();
        if (scheme != null && scheme.equalsIgnoreCase("content")) {
            return new PreparedShare(parsed, resolveMimeType(null, sourceUrl));
        }

        // file:// or absolute filesystem path
        File localFile = resolveLocalFile(sourceUrl, parsed, scheme);
        if (localFile != null && localFile.exists() && localFile.length() > 0) {
            localFile.setReadable(true, false);
            Uri uri = uriForShareFile(context, localFile);
            return new PreparedShare(uri, resolveMimeType(localFile, sourceUrl), localFile);
        }

        File cachedFile = getCacheFile(context, sourceUrl);

        // Cache hit — share immediately, no progress UI
        if (cachedFile.exists() && cachedFile.length() > 0) {
            cachedFile.setReadable(true, false);
            Uri uri = uriForShareFile(context, cachedFile);
            return new PreparedShare(uri, resolveMimeType(cachedFile, sourceUrl), cachedFile);
        }

        // Show Sharing… before waiting on a prefetch download, or the tap looks dead.
        if (allowProgress && statusMessage != null && mainHandler != null) {
            mainHandler.post(() -> showProgress(context, statusMessage));
        }

        Object lock = DOWNLOAD_LOCKS.computeIfAbsent(cacheKey(sourceUrl), key -> new Object());
        synchronized (lock) {
            // Another thread may have finished while we waited
            if (cachedFile.exists() && cachedFile.length() > 0) {
                cachedFile.setReadable(true, false);
                Uri uri = uriForShareFile(context, cachedFile);
                return new PreparedShare(uri, resolveMimeType(cachedFile, sourceUrl), cachedFile);
            }

            if (allowProgress && statusMessage != null && mainHandler != null) {
                mainHandler.post(() -> showProgress(context, statusMessage));
            }

            downloadToCache(sourceUrl, cachedFile);

            if (Thread.currentThread().isInterrupted()) {
                if (allowProgress) {
                    mainHandler.post(VideoShareHelper::dismissProgress);
                }
                return null;
            }
        }

        if (!cachedFile.exists() || cachedFile.length() <= 0) {
            return null;
        }
        cachedFile.setReadable(true, false);
        Uri uri = uriForShareFile(context, cachedFile);
        return new PreparedShare(uri, resolveMimeType(cachedFile, sourceUrl), cachedFile);
    }

    private static void downloadToCache(String sourceUrl, File cachedFile) throws Exception {
        File partFile = new File(cachedFile.getParentFile(), cachedFile.getName() + ".part");
        try {
            Request request = new Request.Builder().url(sourceUrl).build();
            try (Response response = CLIENT.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    throw new Exception("Download failed");
                }
                try (InputStream input = response.body().byteStream();
                     OutputStream output = new BufferedOutputStream(new FileOutputStream(partFile), 1 << 16)) {
                    byte[] buffer = new byte[1 << 16];
                    int read;
                    while ((read = input.read(buffer)) != -1) {
                        if (Thread.currentThread().isInterrupted()) {
                            //noinspection ResultOfMethodCallIgnored
                            partFile.delete();
                            return;
                        }
                        output.write(buffer, 0, read);
                    }
                    output.flush();
                }
            }
            if (cachedFile.exists()) {
                //noinspection ResultOfMethodCallIgnored
                cachedFile.delete();
            }
            if (!partFile.renameTo(cachedFile)) {
                try (InputStream in = new FileInputStream(partFile);
                     OutputStream out = new BufferedOutputStream(new FileOutputStream(cachedFile), 1 << 16)) {
                    byte[] buffer = new byte[1 << 16];
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                    }
                    out.flush();
                }
                //noinspection ResultOfMethodCallIgnored
                partFile.delete();
            }
        } catch (Exception e) {
            if (partFile.exists()) {
                //noinspection ResultOfMethodCallIgnored
                partFile.delete();
            }
            throw e;
        }
    }

    private static File getCacheFile(Context context, String sourceUrl) {
        File cacheDir = context.getExternalCacheDir();
        if (cacheDir == null) {
            cacheDir = context.getCacheDir();
        }
        return new File(cacheDir, "share_" + cacheKey(sourceUrl) + "." + getExtension(sourceUrl));
    }

    private static File resolveLocalFile(String sourceUrl, Uri parsed, String scheme) {
        if (scheme != null && scheme.equalsIgnoreCase("file")) {
            String path = parsed.getPath();
            if (path != null && !path.isEmpty()) {
                return new File(path);
            }
        }
        if (scheme == null && sourceUrl.startsWith("/")) {
            return new File(sourceUrl);
        }
        return null;
    }

    private static void showProgress(Context context, String statusMessage) {
        dismissProgress();
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(statusMessage);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(true);
        progressDialog.setOnCancelListener(dialog -> {
            if (downloadThread != null) {
                downloadThread.interrupt();
            }
        });
        progressDialog.show();
    }

    private static String cacheKey(String url) {
        return Integer.toHexString(url.hashCode());
    }

    private static void dismissProgress() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        progressDialog = null;
    }

    /**
     * Title + optional article body + https share link (no {@code anynews://} in caption).
     * Prefer {@link #buildShareMessage(String, String, String, Long)} when post id is known.
     */
    public static String buildShareMessage(String title, String description, String shareLink) {
        return buildShareMessage(title, description, shareLink, null);
    }

    public static String buildShareMessage(String title, String description, String shareLink,
                                           Long postId) {
        return ShareOutboundHelper.formatCaption(title, description, shareLink, postId);
    }

    private static String buildShareMessage(String title, String shareLink) {
        return buildShareMessage(title, null, shareLink, null);
    }

    private static String resolveMimeType(File videoFile, String videoUrl) {
        String extension = getExtension(videoUrl != null ? videoUrl
                : (videoFile != null ? videoFile.getName() : "mp4"));
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        return mimeType != null ? mimeType : "video/*";
    }

    private static void launchShareIntent(Context context, Activity activity, Uri uri,
                                            String mimeType, String title, String shareLink, ShareMode mode) {
        launchShareIntent(context, activity, uri, mimeType, title, null, shareLink, mode, null);
    }

    /**
     * Opens a half-video that is already on disk. A finished stamp is preferred.
     * A stamp that is still running does not delay the send; the original file
     * goes out and the stamp keeps going for the next tap.
     *
     * @return true when a share intent was started
     */
    private static boolean launchReadyHalfShare(Context context, Activity activity, String[] candidates,
                                                String logoKey, String title, String description,
                                                String shareLink, ShareMode mode,
                                                OnShareCompleteListener listener) {
        try {
            File branded = HalfVideoLogoWatermark.peek(context, logoKey);
            if (branded != null && branded.length() > 0) {
                launchShareIntent(context, activity, uriForShareFile(context, branded), "video/mp4",
                        title, description, shareLink, mode, listener);
                if (listener != null) {
                    listener.onShareStarted();
                }
                return true;
            }
            PreparedShare ready = tryResolveReadyShare(context, candidates);
            if (ready == null || ready.uri == null) {
                return false;
            }
            if (ready.file != null && ready.file.length() > 0) {
                ensureHalfStamp(context, ready.file, logoKey);
                File justStamped = HalfVideoLogoWatermark.peek(context, logoKey);
                if (justStamped != null && justStamped.length() > 0) {
                    launchShareIntent(context, activity, uriForShareFile(context, justStamped),
                            "video/mp4", title, description, shareLink, mode, listener);
                } else {
                    launchShareIntent(context, activity, ready.uri,
                            videoShareMime(ready.file, ready.mimeType), title, description,
                            shareLink, mode, listener);
                }
            } else {
                launchShareIntent(context, activity, ready.uri,
                        videoShareMime(null, ready.mimeType), title, description,
                        shareLink, mode, listener);
            }
            if (listener != null) {
                listener.onShareStarted();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Starts a stamp if one is not already running. Never blocks the caller. */
    private static void ensureHalfStamp(Context context, File source, String logoKey) {
        if (context == null || source == null || !source.exists() || source.length() <= 0
                || logoKey == null || logoKey.isEmpty()) {
            return;
        }
        if (HalfVideoLogoWatermark.peek(context, logoKey) != null) {
            return;
        }
        if (STAMP_RUNNING.putIfAbsent(logoKey, Boolean.TRUE) != null) {
            return;
        }
        final Context app = context.getApplicationContext();
        new Thread(() -> {
            try {
                HalfVideoLogoWatermark.apply(app, source, logoKey);
            } catch (Exception ignored) {
                // The share already left with the original file.
            } finally {
                STAMP_RUNNING.remove(logoKey);
            }
        }, "half-video-stamp").start();
    }

    private static File awaitBranded(Context context, String logoKey, long budgetMs)
            throws InterruptedException {
        long deadline = SystemClock.elapsedRealtime() + Math.max(0L, budgetMs);
        while (true) {
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }
            File branded = HalfVideoLogoWatermark.peek(context, logoKey);
            if (branded != null && branded.length() > 0) {
                return branded;
            }
            long left = deadline - SystemClock.elapsedRealtime();
            if (left <= 0L) {
                return null;
            }
            Thread.sleep(Math.min(40L, left));
        }
    }

    /**
     * FileProvider URI. If this device rejects the external-cache path, copy
     * into internal cache (always in provider_paths) and share that.
     */
    private static Uri uriForShareFile(Context context, File file) throws Exception {
        String authority = context.getPackageName() + ".provider";
        try {
            return FileProvider.getUriForFile(context, authority, file);
        } catch (IllegalArgumentException first) {
            File safe = new File(context.getCacheDir(),
                    "share_safe_" + Integer.toHexString(file.getAbsolutePath().hashCode()) + ".mp4");
            copyShareFile(file, safe);
            safe.setReadable(true, false);
            return FileProvider.getUriForFile(context, authority, safe);
        }
    }

    private static void copyShareFile(File source, File dest) throws Exception {
        File parent = dest.getParentFile();
        if (parent != null) {
            //noinspection ResultOfMethodCallIgnored
            parent.mkdirs();
        }
        try (InputStream in = new FileInputStream(source);
             OutputStream out = new BufferedOutputStream(new FileOutputStream(dest))) {
            byte[] buffer = new byte[1 << 16];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            out.flush();
        }
        if (!dest.exists() || dest.length() <= 0) {
            throw new java.io.IOException("share copy empty");
        }
    }

    /** Never hand WhatsApp image/jpeg for a video file. Sniff the container. */
    private static String videoShareMime(File file, String declared) {
        String sniffed = sniffContainerMime(file);
        if (sniffed != null) {
            return sniffed;
        }
        if (declared != null && declared.startsWith("video/")) {
            return declared;
        }
        return "video/*";
    }

    private static String sniffContainerMime(File file) {
        if (file == null || !file.exists() || file.length() < 12) {
            return null;
        }
        try (InputStream in = new FileInputStream(file)) {
            byte[] header = new byte[12];
            int n = in.read(header);
            if (n >= 8 && header[4] == 'f' && header[5] == 't' && header[6] == 'y' && header[7] == 'p') {
                return "video/mp4";
            }
            if (n >= 4 && (header[0] & 0xFF) == 0x1A && (header[1] & 0xFF) == 0x45
                    && (header[2] & 0xFF) == 0xDF && (header[3] & 0xFF) == 0xA3) {
                return "video/webm";
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static void launchShareIntent(Context context, Activity activity, Uri uri,
                                            String mimeType, String title, String description,
                                            String shareLink, ShareMode mode,
                                            OnShareCompleteListener listener) {
        if (uri == null) {
            Toast.makeText(context, "Unable to share video", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            String shareMessage = buildShareMessage(title, description, shareLink);
            String shareMime = videoShareMime(null, mimeType);

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType(shareMime);
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            if (!shareMessage.isEmpty()) {
                intent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                intent.putExtra(Intent.EXTRA_SUBJECT,
                        title != null && !title.trim().isEmpty() ? title.trim() : shareMessage);
            }
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setClipData(ClipData.newUri(context.getContentResolver(), "shared_video", uri));

            Runnable onConfirmed = () -> {
                if (listener != null) {
                    listener.onShareConfirmed();
                }
            };
            Context launchCtx = activity != null ? activity : context;

            if (mode == ShareMode.WHATSAPP_CHAT || mode == ShareMode.WHATSAPP_STATUS) {
                String pkg = resolveWhatsAppPackage(context, shareMime);
                if (pkg == null && !"video/*".equals(shareMime)) {
                    pkg = resolveWhatsAppPackage(context, "video/*");
                    if (pkg != null) {
                        intent.setType("video/*");
                    }
                }
                if (pkg != null) {
                    intent.setPackage(pkg);
                    if (mode == ShareMode.WHATSAPP_STATUS) {
                        intent.putExtra("jid", "status@broadcast");
                    }
                    // Count only when WhatsApp returns RESULT_OK after Send.
                    grantRead(context, activity, uri, pkg);
                    ShareOutboundHelper.launchChooserOnTargetChosen(
                            launchCtx, intent, "Share Video", onConfirmed);
                    return;
                }
                // No enabled WhatsApp or Business that can take the video.
                // The system sheet still lets the tap reach some other app.
                intent.setPackage(null);
                intent.setType("video/*");
            }

            grantReadToResolvedTargets(context, intent, uri);
            ShareOutboundHelper.launchChooserOnTargetChosen(
                    launchCtx, intent, "Share Video", onConfirmed);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Unable to share video", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Enabled WhatsApp or WhatsApp Business that actually exports a SEND
     * activity for this mime. A disabled stub of {@code com.whatsapp} is skipped
     * so the tap is not aimed at a dead component.
     */
    private static String resolveWhatsAppPackage(Context context, String mimeType) {
        if (context == null) {
            return null;
        }
        PackageManager pm = context.getPackageManager();
        String type = mimeType != null ? mimeType : "video/*";
        for (String pkg : WHATSAPP_PACKAGES) {
            if (!isPackageEnabled(pm, pkg)) {
                continue;
            }
            Intent probe = new Intent(Intent.ACTION_SEND);
            probe.setType(type);
            probe.setPackage(pkg);
            try {
                if (pm.resolveActivity(probe, 0) != null) {
                    return pkg;
                }
                List<ResolveInfo> handlers = pm.queryIntentActivities(probe, 0);
                if (handlers != null && !handlers.isEmpty()) {
                    return pkg;
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private static boolean isPackageEnabled(PackageManager pm, String packageName) {
        try {
            ApplicationInfo info = pm.getApplicationInfo(packageName, 0);
            if (!info.enabled) {
                return false;
            }
            return (info.flags & ApplicationInfo.FLAG_SUSPENDED) == 0;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private static void grantRead(Context context, Activity activity, Uri uri, String packageName) {
        if (packageName == null || uri == null) {
            return;
        }
        try {
            context.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (Exception ignored) {
        }
        if (activity != null) {
            try {
                activity.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (Exception ignored) {
            }
        }
    }

    private static void grantReadToResolvedTargets(Context context, Intent intent, Uri uri) {
        try {
            List<ResolveInfo> resInfoList = context.getPackageManager()
                    .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            if (resInfoList == null) {
                return;
            }
            for (ResolveInfo resolveInfo : resInfoList) {
                if (resolveInfo.activityInfo == null) {
                    continue;
                }
                grantRead(context, null, uri, resolveInfo.activityInfo.packageName);
            }
        } catch (Exception ignored) {
        }
    }

    private static boolean isAppInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static String getExtension(String url) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension == null || extension.isEmpty()) {
            int q = url != null ? url.lastIndexOf('?') : -1;
            String path = q > 0 ? url.substring(0, q) : url;
            int dot = path != null ? path.lastIndexOf('.') : -1;
            if (dot >= 0 && dot < path.length() - 1) {
                String ext = path.substring(dot + 1).toLowerCase();
                if (ext.length() <= 5 && ext.matches("[a-z0-9]+")) {
                    return ext;
                }
            }
            return "mp4";
        }
        return extension;
    }

    private static final class PreparedShare {
        final Uri uri;
        final String mimeType;
        final File file;

        PreparedShare(Uri uri, String mimeType) {
            this(uri, mimeType, null);
        }

        PreparedShare(Uri uri, String mimeType, File file) {
            this.uri = uri;
            this.mimeType = mimeType;
            this.file = file;
        }
    }
}
