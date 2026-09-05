package com.anyNews.anynews.Utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
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
        void onShareStarted();
    }

    private static final OkHttpClient CLIENT = new OkHttpClient.Builder().build();
    private static final ConcurrentHashMap<String, Object> DOWNLOAD_LOCKS = new ConcurrentHashMap<>();
    private static ProgressDialog progressDialog;
    private static volatile Thread downloadThread;

    public static void shareVideo(Context context, Activity activity, String videoUrl, String title,
                                    String shareLink, ShareMode mode, OnShareCompleteListener listener) {
        shareVideo(context, activity, videoUrl, title, shareLink, mode,
                context.getString(R.string.video_status_sharing), listener);
    }

    public static void shareVideo(Context context, Activity activity, String videoUrl, String title,
                                    String shareLink, ShareMode mode, String statusMessage,
                                    OnShareCompleteListener listener) {
        if (videoUrl == null || videoUrl.trim().isEmpty()) {
            Toast.makeText(context, "Video is not available to share", Toast.LENGTH_SHORT).show();
            if (listener != null) {
                listener.onShareStarted();
            }
            return;
        }

        final String sourceUrl = videoUrl.trim();
        final Handler mainHandler = new Handler(Looper.getMainLooper());

        // Instant path: local/cache already ready — skip worker thread + dialog.
        PreparedShare ready = tryResolveReadyShare(context, sourceUrl);
        if (ready != null) {
            launchShareIntent(context, activity, ready.uri, ready.mimeType, title, shareLink, mode);
            if (listener != null) {
                listener.onShareStarted();
            }
            return;
        }

        downloadThread = new Thread(() -> {
            try {
                PreparedShare prepared = prepareShareableVideo(context, sourceUrl, statusMessage, mainHandler, true);
                if (prepared == null) {
                    mainHandler.post(() -> {
                        dismissProgress();
                        if (listener != null) {
                            listener.onShareStarted();
                        }
                    });
                    return;
                }
                mainHandler.post(() -> {
                    dismissProgress();
                    launchShareIntent(context, activity, prepared.uri, prepared.mimeType, title, shareLink, mode);
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
        try {
            Uri parsed = Uri.parse(sourceUrl);
            String scheme = parsed.getScheme();
            if (scheme != null && scheme.equalsIgnoreCase("content")) {
                return new PreparedShare(parsed, resolveMimeType(null, sourceUrl));
            }
            File localFile = resolveLocalFile(sourceUrl, parsed, scheme);
            if (localFile != null && localFile.exists() && localFile.length() > 0) {
                localFile.setReadable(true, false);
                Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", localFile);
                return new PreparedShare(uri, resolveMimeType(localFile, sourceUrl));
            }
            File cachedFile = getCacheFile(context, sourceUrl);
            if (cachedFile.exists() && cachedFile.length() > 0) {
                cachedFile.setReadable(true, false);
                Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", cachedFile);
                return new PreparedShare(uri, resolveMimeType(cachedFile, sourceUrl));
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * Warm the share cache while the bottom sheet is open so Status/Contact can skip the network wait.
     */
    public static void prefetchVideo(Context context, String videoUrl) {
        if (context == null || videoUrl == null || videoUrl.trim().isEmpty()) {
            return;
        }
        final String sourceUrl = videoUrl.trim();
        Uri parsed = Uri.parse(sourceUrl);
        String scheme = parsed.getScheme();
        if (scheme != null && (scheme.equalsIgnoreCase("content") || scheme.equalsIgnoreCase("file"))) {
            return;
        }
        if (scheme == null && sourceUrl.startsWith("/")) {
            return;
        }

        final Context appContext = context.getApplicationContext();
        File cachedFile = getCacheFile(appContext, sourceUrl);
        if (cachedFile.exists() && cachedFile.length() > 0) {
            return;
        }

        new Thread(() -> {
            try {
                prepareShareableVideo(appContext, sourceUrl, null, new Handler(Looper.getMainLooper()), false);
            } catch (Exception ignored) {
                // Prefetch is best-effort; share path will retry with UI if needed.
            }
        }, "video-share-prefetch").start();
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
            Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", localFile);
            return new PreparedShare(uri, resolveMimeType(localFile, sourceUrl));
        }

        File cachedFile = getCacheFile(context, sourceUrl);

        // Cache hit — share immediately, no progress UI
        if (cachedFile.exists() && cachedFile.length() > 0) {
            cachedFile.setReadable(true, false);
            Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", cachedFile);
            return new PreparedShare(uri, resolveMimeType(cachedFile, sourceUrl));
        }

        Object lock = DOWNLOAD_LOCKS.computeIfAbsent(cacheKey(sourceUrl), key -> new Object());
        synchronized (lock) {
            // Another thread may have finished while we waited
            if (cachedFile.exists() && cachedFile.length() > 0) {
                cachedFile.setReadable(true, false);
                Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", cachedFile);
                return new PreparedShare(uri, resolveMimeType(cachedFile, sourceUrl));
            }

            if (allowProgress && statusMessage != null) {
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

        cachedFile.setReadable(true, false);
        Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", cachedFile);
        return new PreparedShare(uri, resolveMimeType(cachedFile, sourceUrl));
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

    private static String buildShareMessage(String title, String shareLink) {
        String safeTitle = title != null ? title.trim() : "";
        String safeLink = shareLink != null ? shareLink.trim() : "";
        if (!safeTitle.isEmpty() && !safeLink.isEmpty()) {
            return safeTitle + "\n" + safeLink;
        }
        if (!safeTitle.isEmpty()) {
            return safeTitle;
        }
        return safeLink;
    }

    private static String resolveMimeType(File videoFile, String videoUrl) {
        String extension = getExtension(videoUrl != null ? videoUrl
                : (videoFile != null ? videoFile.getName() : "mp4"));
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        return mimeType != null ? mimeType : "video/*";
    }

    private static void launchShareIntent(Context context, Activity activity, Uri uri,
                                            String mimeType, String title, String shareLink, ShareMode mode) {
        try {
            String shareMessage = buildShareMessage(title, shareLink);

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType(mimeType != null ? mimeType : "video/*");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            if (!shareMessage.isEmpty()) {
                intent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                intent.putExtra(Intent.EXTRA_SUBJECT, shareMessage);
            }
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setClipData(ClipData.newUri(context.getContentResolver(), "shared_video", uri));

            if (mode == ShareMode.WHATSAPP_CHAT || mode == ShareMode.WHATSAPP_STATUS) {
                if (!isAppInstalled(context, "com.whatsapp")) {
                    Toast.makeText(context, "WhatsApp is not installed", Toast.LENGTH_SHORT).show();
                    return;
                }
                intent.setPackage("com.whatsapp");
                if (mode == ShareMode.WHATSAPP_STATUS) {
                    intent.putExtra("jid", "status@broadcast");
                }
                // Skip scanning all share targets — grant WhatsApp only and launch ASAP.
                context.grantUriPermission("com.whatsapp", uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                if (activity != null) {
                    activity.grantUriPermission("com.whatsapp", uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
                context.startActivity(intent);
                return;
            }

            List<ResolveInfo> resInfoList = context.getPackageManager()
                    .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                context.grantUriPermission(packageName, uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
            context.startActivity(Intent.createChooser(intent, "Share Video"));
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Unable to share video", Toast.LENGTH_SHORT).show();
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

        PreparedShare(Uri uri, String mimeType) {
            this.uri = uri;
            this.mimeType = mimeType;
        }
    }
}
