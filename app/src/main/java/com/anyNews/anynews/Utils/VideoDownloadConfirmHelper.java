package com.anyNews.anynews.Utils;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.anyNews.anynews.R;

import java.io.File;

/**
 * After a video finishes downloading via {@link FlipperAdapter#downloadPDFWithProgressBar}:
 * <ul>
 *   <li>AnyNews-styled confirmation dialog on the main thread</li>
 *   <li>System notification the user can tap to open the file ({@link Intent#ACTION_VIEW})</li>
 * </ul>
 */
public final class VideoDownloadConfirmHelper {

    private static final String TAG = "VideoDownloadConfirm";
    private static final String DOWNLOAD_CHANNEL_ID = "AnyNewsDownloads";
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static final int REQ_POST_NOTIFICATIONS_DOWNLOAD = 2911;

    private VideoDownloadConfirmHelper() {
    }

    /**
     * Request POST_NOTIFICATIONS on Android 13+ so the open-file notification can show.
     * Safe to call when starting a download; no-ops if already granted or pre-Tiramisu.
     */
    public static void ensureNotificationPermission(Activity activity) {
        if (activity == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return;
        }
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            return;
        }
        try {
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    REQ_POST_NOTIFICATIONS_DOWNLOAD);
        } catch (Exception e) {
            Log.w(TAG, "request POST_NOTIFICATIONS failed: " + e.getMessage());
        }
    }

    /**
     * Dialog + tap-to-open notification for a completed DownloadManager video download.
     */
    public static void showDownloaded(Context context, long downloadId, String fileName) {
        if (context == null) {
            return;
        }
        Runnable work = () -> {
            showDialogNow(context);
            postOpenNotification(context, downloadId, fileName);
        };
        if (Looper.myLooper() == Looper.getMainLooper()) {
            work.run();
        } else {
            MAIN.post(work);
        }
    }

    /** Dialog only (legacy call sites). Prefer {@link #showDownloaded(Context, long, String)}. */
    public static void showDownloaded(Context context) {
        showDownloaded(context, -1L, null);
    }

    /**
     * Same confirmation as a DownloadManager save, for a file this app wrote
     * (watermarked half-article video).
     */
    public static void showSavedVideo(Context context, Uri openUri, String fileName) {
        if (context == null) {
            return;
        }
        Runnable work = () -> {
            showDialogNow(context);
            postOpenNotificationUri(context, openUri, fileName);
        };
        if (Looper.myLooper() == Looper.getMainLooper()) {
            work.run();
        } else {
            MAIN.post(work);
        }
    }

    private static void showDialogNow(Context context) {
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            if (activity.isFinishing() || activity.isDestroyed()) {
                return;
            }
        }
        try {
            Context dialogCtx = context;
            if (!(context instanceof Activity) && context.getApplicationContext() != null) {
                // Dialog needs a window token — prefer Activity when available.
                dialogCtx = context;
            }
            View view = LayoutInflater.from(dialogCtx).inflate(R.layout.dialog_video_download_confirm, null);
            Dialog dialog = new Dialog(dialogCtx);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(view);
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(true);

            view.findViewById(R.id.video_download_confirm_ok).setOnClickListener(v -> dialog.dismiss());

            Window window = dialog.getWindow();
            if (window != null) {
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                window.setDimAmount(0.45f);
                window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

                DisplayMetrics metrics = dialogCtx.getResources().getDisplayMetrics();
                int sideMargin = Math.round(10f * metrics.density);
                int bottomMargin = Math.round(10f * metrics.density);

                WindowManager.LayoutParams params = window.getAttributes();
                params.width = metrics.widthPixels - (sideMargin * 2);
                params.height = WindowManager.LayoutParams.WRAP_CONTENT;
                params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
                params.y = bottomMargin;
                params.dimAmount = 0.45f;
                window.setAttributes(params);
            }

            dialog.show();
        } catch (Exception e) {
            Log.e(TAG, "show dialog failed", e);
        }
    }

    private static void postOpenNotification(Context context, long downloadId, String fileName) {
        try {
            ensureDownloadChannel(context);

            Uri openUri = resolveOpenUri(context, downloadId, fileName);
            postOpenNotificationUri(context, openUri, fileName);
        } catch (Exception e) {
            Log.e(TAG, "post notification failed", e);
        }
    }

    private static void postOpenNotificationUri(Context context, Uri openUri, String fileName) {
        if (openUri == null) {
            Log.w(TAG, "no open URI for file=" + fileName);
            return;
        }
        try {
            ensureDownloadChannel(context);
            String mime = resolveMime(context, openUri, fileName);
            Intent viewIntent = new Intent(Intent.ACTION_VIEW);
            viewIntent.setDataAndType(openUri, mime);
            viewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                context.grantUriPermission(
                        context.getPackageName(),
                        openUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (Exception ignored) {
            }

            int requestCode = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
            if (requestCode == 0) {
                requestCode = 44001;
            }
            int piFlags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                piFlags |= PendingIntent.FLAG_IMMUTABLE;
            }
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context.getApplicationContext(),
                    requestCode,
                    viewIntent,
                    piFlags);

            String title = context.getString(R.string.your_video_downloaded);
            String body = context.getString(R.string.video_download_confirm_message);
            if (fileName != null && !fileName.trim().isEmpty()) {
                body = fileName;
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(
                    context.getApplicationContext(), DOWNLOAD_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_arman_notificatio)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_STATUS)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder.setColor(ContextCompat.getColor(context, R.color.colorPrimary));
            }

            NotificationManagerCompat nm =
                    NotificationManagerCompat.from(context.getApplicationContext());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                    && ContextCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "POST_NOTIFICATIONS not granted — dialog still shown");
                return;
            }
            int notifyId = requestCode == 0 ? 44001 : requestCode;
            nm.notify(notifyId, builder.build());
        } catch (Exception e) {
            Log.e(TAG, "post notification failed", e);
        }
    }

    private static Uri resolveOpenUri(Context context, long downloadId, String fileName) {
        if (downloadId > 0) {
            try {
                DownloadManager dm =
                        (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                if (dm != null) {
                    Uri dmUri = dm.getUriForDownloadedFile(downloadId);
                    if (dmUri != null) {
                        return dmUri;
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "getUriForDownloadedFile failed: " + e.getMessage());
            }
        }
        if (fileName == null || fileName.trim().isEmpty()) {
            return null;
        }
        try {
            File file = new File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    fileName);
            if (file.exists() && file.length() > 0) {
                return FileProvider.getUriForFile(
                        context, context.getPackageName() + ".provider", file);
            }
        } catch (Exception e) {
            Log.w(TAG, "FileProvider URI failed: " + e.getMessage());
        }
        return null;
    }

    private static String resolveMime(Context context, Uri uri, String fileName) {
        String mime = null;
        try {
            mime = context.getContentResolver().getType(uri);
        } catch (Exception ignored) {
        }
        if (mime == null || mime.trim().isEmpty()) {
            String ext = null;
            if (fileName != null && fileName.contains(".")) {
                ext = fileName.substring(fileName.lastIndexOf('.') + 1);
            }
            if (ext != null) {
                mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext.toLowerCase());
            }
        }
        if (mime == null || mime.trim().isEmpty()) {
            mime = "video/*";
        }
        return mime;
    }

    private static void ensureDownloadChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) {
            return;
        }
        if (nm.getNotificationChannel(DOWNLOAD_CHANNEL_ID) != null) {
            return;
        }
        NotificationChannel channel = new NotificationChannel(
                DOWNLOAD_CHANNEL_ID,
                "Downloads",
                NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription("Video and file download alerts");
        channel.enableVibration(true);
        nm.createNotificationChannel(channel);
    }
}
