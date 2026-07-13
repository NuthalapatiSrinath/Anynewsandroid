package com.anyNews.anynews.Utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.graphics.ColorUtils;

import com.anyNews.anynews.R;
import com.anyNews.anynews.Ui.NeomorphFrameLayout;

public final class ThemeColorHelper {

    public static final String DEFAULT_COLOR = "#EF4923";

    private ThemeColorHelper() {
    }

    public static void saveColor(Context context, String color) {
        if (context == null) {
            return;
        }
        SharedPrefsData.putString(
                context,
                Constants.THEME_COLOR,
                normalizeColor(color),
                Constants.PREF_NAME
        );
    }

    public static int getThemeColor(Context context) {
        String stored = SharedPrefsData.getString(context, Constants.THEME_COLOR, Constants.PREF_NAME);
        if (TextUtils.isEmpty(stored)) {
            return context.getColor(R.color.brand_orange);
        }
        try {
            return Color.parseColor(stored);
        } catch (IllegalArgumentException ignored) {
            return context.getColor(R.color.brand_orange);
        }
    }

    public static int getThemeShadowColor(Context context) {
        return ColorUtils.setAlphaComponent(getThemeColor(context), 0x16);
    }

    public static void applySelectedChip(Context context, NeomorphFrameLayout frame, TextView label) {
        if (context == null) {
            return;
        }
        int themeColor = getThemeColor(context);
        applySelectedNeomorphTree(frame, themeColor, getThemeShadowColor(context));
        if (label != null) {
            label.setBackgroundColor(themeColor);
        }
    }

    public static void applySelectedNeomorphTree(Context context, NeomorphFrameLayout root) {
        if (context == null || root == null) {
            return;
        }
        applySelectedNeomorphTree(root, getThemeColor(context), getThemeShadowColor(context));
    }

    private static void applySelectedNeomorphTree(NeomorphFrameLayout frame, int themeColor, int shadowColor) {
        if (frame == null) {
            return;
        }
        frame.setNeomorphBackgroundColor(themeColor);
        frame.setNeomorphShadowColor(shadowColor);
        for (int i = 0; i < frame.getChildCount(); i++) {
            View child = frame.getChildAt(i);
            if (child instanceof NeomorphFrameLayout) {
                applySelectedNeomorphTree((NeomorphFrameLayout) child, themeColor, shadowColor);
            } else if (child instanceof TextView) {
                ((TextView) child).setBackgroundColor(themeColor);
            }
        }
    }

    public static void applyAccentTint(Context context, ImageView imageView) {
        if (context == null || imageView == null) {
            return;
        }
        imageView.setColorFilter(getThemeColor(context), PorterDuff.Mode.SRC_IN);
    }

    public static void applyAccentBackground(Context context, View view) {
        if (context == null || view == null) {
            return;
        }
        view.setBackgroundColor(getThemeColor(context));
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
