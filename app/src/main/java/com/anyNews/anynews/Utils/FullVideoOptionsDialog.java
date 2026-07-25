package com.anyNews.anynews.Utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.anyNews.anynews.R;

/**
 * Full-video "Download Video / Share / Dismiss" chooser shown before the share
 * sheet. Only full_video posts reach this; every other post type keeps its
 * existing one-tap share flow.
 *
 * UI mirrors the iOS UIAlertController actionSheet: frosted bottom card,
 * centered title/message, blue text rows with hairline dividers.
 */
public final class FullVideoOptionsDialog {

    public interface Listener {
        void onDownloadVideo();

        void onShare();
    }

    private FullVideoOptionsDialog() {
    }

    public static void show(Context context, Listener listener) {
        if (context == null || listener == null) {
            return;
        }
        if (context instanceof Activity && ((Activity) context).isFinishing()) {
            return;
        }
        try {
            View view = LayoutInflater.from(context).inflate(R.layout.dialog_full_video_options, null);
            Dialog dialog = new Dialog(context);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(view);
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(true);

            view.findViewById(R.id.full_video_option_download).setOnClickListener(v -> {
                dialog.dismiss();
                listener.onDownloadVideo();
            });
            view.findViewById(R.id.full_video_option_share).setOnClickListener(v -> {
                dialog.dismiss();
                listener.onShare();
            });
            view.findViewById(R.id.full_video_option_dismiss).setOnClickListener(v -> dialog.dismiss());

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
