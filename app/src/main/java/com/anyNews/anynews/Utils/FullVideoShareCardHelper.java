package com.anyNews.anynews.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.anyNews.anynews.Pojo.FullVideoShareCardConfig;
import com.anyNews.anynews.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.gson.Gson;

/**
 * Applies admin {@link FullVideoShareCardConfig} to the full-video share sheet
 * primary card. Cached from splash in SharedPrefs. Missing config → leave the
 * compiled layout untouched (current hardcoded look).
 */
public final class FullVideoShareCardHelper {

    private static final String TAG = "AnyNewsShareCard";
    private static final Gson GSON = new Gson();

    private FullVideoShareCardHelper() {
    }

    public static FullVideoShareCardConfig load(Context context) {
        if (context == null) {
            return null;
        }
        String json = SharedPrefsData.getString(context, Constants.FULL_VIDEO_SHARE_CARD, Constants.PREF_NAME);
        if (TextUtils.isEmpty(json)) {
            return null;
        }
        try {
            return GSON.fromJson(json, FullVideoShareCardConfig.class);
        } catch (Exception e) {
            Log.e(TAG, "load parse failed", e);
            return null;
        }
    }

    public static void save(Context context, FullVideoShareCardConfig cfg) {
        if (context == null || cfg == null) {
            return;
        }
        SharedPrefsData.putString(
                context,
                Constants.FULL_VIDEO_SHARE_CARD,
                GSON.toJson(cfg),
                Constants.PREF_NAME
        );
    }

    /**
     * Style the primary "Send to Contact" card. Safe no-op when cfg is null.
     *
     * @param cardRoot the {@code option_whatsapp_chat} LinearLayout
     */
    public static void apply(Context context, View cardRoot) {
        if (context == null || cardRoot == null) {
            return;
        }
        FullVideoShareCardConfig cfg = load(context);
        if (cfg == null) {
            return;
        }

        TextView title = cardRoot.findViewById(R.id.option_whatsapp_chat_title);
        TextView subtitle = cardRoot.findViewById(R.id.option_whatsapp_chat_subtitle);
        ImageView icon = cardRoot.findViewById(R.id.option_whatsapp_chat_icon);

        if (title != null) {
            title.setText(cfg.getTitle());
            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, cfg.getTitleSizeSp());
            try {
                title.setTextColor(Color.parseColor(cfg.getTitleColor()));
            } catch (Exception ignored) {
                title.setTextColor(Color.BLACK);
            }
        }
        if (subtitle != null) {
            subtitle.setText(cfg.getSubtitle());
            subtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, cfg.getSubtitleSizeSp());
            try {
                subtitle.setTextColor(Color.parseColor(cfg.getSubtitleColor()));
            } catch (Exception ignored) {
                subtitle.setTextColor(Color.parseColor("#A2A2A2"));
            }
        }

        String bgImage = cfg.getBackgroundImageUrl();
        if (!TextUtils.isEmpty(bgImage) && isValidGlideContext(context)) {
            Glide.with(context)
                    .load(bgImage)
                    .into(new CustomTarget<Drawable>() {
                        @Override
                        public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                            cardRoot.setBackground(resource);
                        }

                        @Override
                        public void onLoadCleared(Drawable placeholder) {
                        }
                    });
        } else {
            try {
                GradientDrawable bg = new GradientDrawable();
                bg.setColor(Color.parseColor(cfg.getBackgroundColor()));
                bg.setCornerRadius(dp(context, 12f));
                bg.setStroke(Math.max(1, dp(context, 1f)), Color.parseColor("#E0E0E0"));
                cardRoot.setBackground(bg);
            } catch (Exception e) {
                Log.w(TAG, "bg color apply failed", e);
            }
        }

        String iconUrl = cfg.getIconUrl();
        if (icon != null && !TextUtils.isEmpty(iconUrl) && isValidGlideContext(context)) {
            Glide.with(context)
                    .load(iconUrl)
                    .placeholder(R.drawable.ic_whatsapp_chat_share)
                    .error(R.drawable.ic_whatsapp_chat_share)
                    .into(icon);
        }
    }

    /** Open action_url if present; returns true when handled. */
    public static boolean openActionUrlIfPresent(Context context) {
        FullVideoShareCardConfig cfg = load(context);
        if (cfg == null || !cfg.hasCustomActionUrl()) {
            return false;
        }
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(cfg.getActionUrl()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "openActionUrl failed", e);
            Toast.makeText(context, "Could not open link", Toast.LENGTH_SHORT).show();
            return true; // still "handled" so we don't fall through to WhatsApp
        }
    }

    private static boolean isValidGlideContext(Context context) {
        if (context == null) {
            return false;
        }
        if (context instanceof Activity) {
            Activity a = (Activity) context;
            return !a.isFinishing() && !a.isDestroyed();
        }
        return true;
    }

    private static int dp(Context context, float value) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                context.getResources().getDisplayMetrics()
        ));
    }
}
