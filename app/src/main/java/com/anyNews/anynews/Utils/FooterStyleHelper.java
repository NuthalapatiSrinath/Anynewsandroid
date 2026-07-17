package com.anyNews.anynews.Utils;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.anyNews.anynews.Pojo.FooterStyleConfig;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;

/**
 * Applies the admin-configured {@link FooterStyleConfig} (bottom-nav footer icon
 * size + label typography) at render time. Reads the JSON cached in SharedPrefs
 * from the splash config. If nothing is cached the footer is left untouched, so
 * the current compiled look is preserved. Because the backend serves defaults
 * that equal the current hardcoded values (icon 24dp, labels 11/12sp bold,
 * selector colors kept), applying them reproduces the current look exactly.
 */
public final class FooterStyleHelper {

    private static final String TAG = "AnyNewsFooterStyle";
    private static final Gson GSON = new Gson();

    private FooterStyleHelper() {
    }

    public static FooterStyleConfig load(Context context) {
        if (context == null) {
            return null;
        }
        String json = SharedPrefsData.getString(context, Constants.FOOTER_STYLE, Constants.PREF_NAME);
        if (TextUtils.isEmpty(json)) {
            return null;
        }
        try {
            return GSON.fromJson(json, FooterStyleConfig.class);
        } catch (Exception e) {
            Log.e(TAG, "load parse failed", e);
            return null;
        }
    }

    public static void apply(Context context, BottomNavigationView nav) {
        if (context == null || nav == null) {
            return;
        }
        FooterStyleConfig cfg = load(context);
        if (cfg == null) {
            return;
        }
        try {
            float iconDp = cfg.getIconSizeDp();
            if (iconDp > 0) {
                int px = Math.round(iconDp * context.getResources().getDisplayMetrics().density);
                nav.setItemIconSize(px);
            }
        } catch (Exception e) {
            Log.e(TAG, "icon size failed", e);
        }

        applyLabelColors(nav, cfg);

        try {
            View menuView = nav.getChildAt(0);
            if (menuView instanceof ViewGroup) {
                ViewGroup mv = (ViewGroup) menuView;
                for (int i = 0; i < mv.getChildCount(); i++) {
                    applyItemLabels(context, mv.getChildAt(i), cfg);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "label styling failed", e);
        }
    }

    private static void applyLabelColors(BottomNavigationView nav, FooterStyleConfig cfg) {
        String normal = cfg.getLabelColor();
        String active = cfg.getLabelActiveColor();
        // Both empty → keep the compiled selector (current look).
        if (TextUtils.isEmpty(normal) && TextUtils.isEmpty(active)) {
            return;
        }
        Integer normalColor = parseColor(!TextUtils.isEmpty(normal) ? normal : active);
        Integer activeColor = parseColor(!TextUtils.isEmpty(active) ? active : normal);
        if (normalColor == null || activeColor == null) {
            return;
        }
        int[][] states = {
                {android.R.attr.state_checked},
                {}
        };
        int[] colors = {activeColor, normalColor};
        try {
            nav.setItemTextColor(new ColorStateList(states, colors));
        } catch (Exception e) {
            Log.e(TAG, "text color failed", e);
        }
    }

    private static void applyItemLabels(Context context, View item, FooterStyleConfig cfg) {
        if (item == null) {
            return;
        }
        String pkg = context.getPackageName();
        int smallId = context.getResources().getIdentifier("navigation_bar_item_small_label_view", "id", pkg);
        int largeId = context.getResources().getIdentifier("navigation_bar_item_large_label_view", "id", pkg);
        TextView small = smallId != 0 ? item.findViewById(smallId) : null;
        TextView large = largeId != 0 ? item.findViewById(largeId) : null;
        if (small != null || large != null) {
            if (small != null) styleLabel(context, small, cfg.getLabelSizeSp(), cfg);
            if (large != null) styleLabel(context, large, cfg.getLabelActiveSizeSp(), cfg);
        } else if (item instanceof ViewGroup) {
            // Fallback: style every TextView with the inactive size.
            styleTextViewsRecursive(context, (ViewGroup) item, cfg);
        }
    }

    private static void styleTextViewsRecursive(Context context, ViewGroup group, FooterStyleConfig cfg) {
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            if (child instanceof TextView) {
                styleLabel(context, (TextView) child, cfg.getLabelSizeSp(), cfg);
            } else if (child instanceof ViewGroup) {
                styleTextViewsRecursive(context, (ViewGroup) child, cfg);
            }
        }
    }

    private static void styleLabel(Context context, TextView tv, float sizeSp, FooterStyleConfig cfg) {
        if (tv == null) {
            return;
        }
        if (sizeSp > 0) {
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp);
        }
        String family = cfg.getLabelFontFamily();
        if (!TextUtils.isEmpty(family)) {
            FontSettingsHelper.applyTypeface(context, tv, family);
        }
        tv.setTypeface(tv.getTypeface(), cfg.isLabelBold() ? Typeface.BOLD : Typeface.NORMAL);
    }

    private static Integer parseColor(String color) {
        if (TextUtils.isEmpty(color)) {
            return null;
        }
        try {
            return Color.parseColor(color.trim());
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
