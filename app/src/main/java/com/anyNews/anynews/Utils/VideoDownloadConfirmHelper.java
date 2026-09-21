package com.anyNews.anynews.Utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.anyNews.anynews.R;

/**
 * Shared success popup after a video file finishes downloading via
 * {@link FlipperAdapter#downloadPDFWithProgressBar}. Matches the
 * Full Video options sheet style so every download entry point shows
 * the same confirmation.
 */
public final class VideoDownloadConfirmHelper {

    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private VideoDownloadConfirmHelper() {
    }

    public static void showDownloaded(Context context) {
        if (context == null) {
            return;
        }
        if (Looper.myLooper() == Looper.getMainLooper()) {
            showNow(context);
        } else {
            MAIN.post(() -> showNow(context));
        }
    }

    private static void showNow(Context context) {
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            if (activity.isFinishing() || activity.isDestroyed()) {
                return;
            }
        }
        try {
            View view = LayoutInflater.from(context).inflate(R.layout.dialog_video_download_confirm, null);
            Dialog dialog = new Dialog(context);
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

                DisplayMetrics metrics = context.getResources().getDisplayMetrics();
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
            e.printStackTrace();
        }
    }
}
