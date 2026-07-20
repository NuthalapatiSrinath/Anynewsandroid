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
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

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
            return;
        }

        Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.post(() -> {
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
        });

        downloadThread = new Thread(() -> {
            File videoFile = null;
            File partFile = null;
            try {
                String extension = getExtension(videoUrl);
                File cacheDir = context.getExternalCacheDir();
                if (cacheDir == null) {
                    cacheDir = context.getCacheDir();
                }
                videoFile = new File(cacheDir, "share_" + cacheKey(videoUrl) + "." + extension);

                if (!(videoFile.exists() && videoFile.length() > 0)) {
                    partFile = new File(cacheDir, videoFile.getName() + ".part");
                    Request request = new Request.Builder().url(videoUrl).build();
                    try (Response response = CLIENT.newCall(request).execute()) {
                        if (!response.isSuccessful() || response.body() == null) {
                            throw new Exception("Download failed");
                        }
                        InputStream input = response.body().byteStream();
                        OutputStream output = new BufferedOutputStream(new FileOutputStream(partFile), 1 << 16);
                        byte[] buffer = new byte[1 << 16];
                        int read;
                        while ((read = input.read(buffer)) != -1) {
                            if (Thread.currentThread().isInterrupted()) {
                                output.close();
                                input.close();
                                partFile.delete();
                                mainHandler.post(VideoShareHelper::dismissProgress);
                                return;
                            }
                            output.write(buffer, 0, read);
                        }
                        output.flush();
                        output.close();
                        input.close();
                    }
                    if (!partFile.renameTo(videoFile)) {
                        throw new Exception("Unable to finalize download");
                    }
                }

                videoFile.setReadable(true, false);
                File finalVideoFile = videoFile;
                mainHandler.post(() -> {
                    dismissProgress();
                    launchShareIntent(context, activity, finalVideoFile, title, shareLink, mode);
                    if (listener != null) {
                        listener.onShareStarted();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                if (partFile != null && partFile.exists()) {
                    partFile.delete();
                }
                mainHandler.post(() -> {
                    dismissProgress();
                    Toast.makeText(context, "Unable to prepare video for sharing", Toast.LENGTH_SHORT).show();
                });
            }
        });
        downloadThread.start();
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
        String extension = getExtension(videoUrl != null ? videoUrl : videoFile.getName());
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        return mimeType != null ? mimeType : "video/*";
    }

    private static void launchShareIntent(Context context, Activity activity, File videoFile,
                                            String title, String shareLink, ShareMode mode) {
        try {
            Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", videoFile);
            String mimeType = resolveMimeType(videoFile, videoFile.getName());
            String shareMessage = buildShareMessage(title, shareLink);

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType(mimeType);
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
            }

            List<ResolveInfo> resInfoList = context.getPackageManager()
                    .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                context.grantUriPermission(packageName, uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
            if (activity != null && (mode == ShareMode.WHATSAPP_CHAT || mode == ShareMode.WHATSAPP_STATUS)) {
                activity.grantUriPermission("com.whatsapp", uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }

            if (mode == ShareMode.GENERAL) {
                context.startActivity(Intent.createChooser(intent, "Share Video"));
            } else {
                context.startActivity(intent);
            }
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
            return "mp4";
        }
        return extension;
    }
}
