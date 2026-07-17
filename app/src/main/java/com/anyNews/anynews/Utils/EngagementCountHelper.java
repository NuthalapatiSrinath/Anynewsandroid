package com.anyNews.anynews.Utils;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.anyNews.anynews.R;

public final class EngagementCountHelper {

    public static final String DEFAULT_COLOR = "#FF6300";

    private EngagementCountHelper() {
    }

    public static void saveColor(Context context, String color) {
        if (context == null) {
            return;
        }
        SharedPrefsData.putString(
                context,
                Constants.ENGAGEMENT_BADGE_COLOR,
                normalizeColor(color),
                Constants.PREF_NAME
        );
    }

    public static int getCountColor(Context context) {
        String stored = SharedPrefsData.getString(context, Constants.ENGAGEMENT_BADGE_COLOR, Constants.PREF_NAME);
        if (TextUtils.isEmpty(stored)) {
            return context.getColor(R.color.engagement_badge_bg);
        }
        try {
            return Color.parseColor(stored);
        } catch (IllegalArgumentException ignored) {
            return context.getColor(R.color.engagement_badge_bg);
        }
    }

    public static void applyHalfBadgeStyle(Context context, TextView tv) {
        if (context == null || tv == null) {
            return;
        }
        GradientDrawable badge = new GradientDrawable();
        badge.setShape(GradientDrawable.OVAL);
        badge.setColor(getCountColor(context));
        badge.setStroke(
                (int) (context.getResources().getDisplayMetrics().density + 0.5f),
                context.getColor(R.color.engagement_badge_stroke)
        );
        tv.setBackground(badge);
        tv.setTextColor(context.getColor(R.color.engagement_badge_text));
        tv.setTypeface(tv.getTypeface(), Typeface.BOLD);
    }

    /** Tints half-post action icons with the same admin engagement color as counts. */
    public static void applyIconTint(Context context, ImageView icon) {
        if (context == null || icon == null) {
            return;
        }
        icon.setImageTintList(ColorStateList.valueOf(getCountColor(context)));
    }

    public static String normalizeColor(String color) {
        if (TextUtils.isEmpty(color)) {
            return DEFAULT_COLOR;
        }
        String trimmed = color.trim();
        if (!trimmed.startsWith("#")) {
            trimmed = "#" + trimmed;
        }
        if (trimmed.length() == 4) {
            trimmed = "#"
                    + trimmed.charAt(1) + trimmed.charAt(1)
                    + trimmed.charAt(2) + trimmed.charAt(2)
                    + trimmed.charAt(3) + trimmed.charAt(3);
        }
        try {
            Color.parseColor(trimmed);
            return trimmed.toUpperCase();
        } catch (IllegalArgumentException ignored) {
            return DEFAULT_COLOR;
        }
    }
}
