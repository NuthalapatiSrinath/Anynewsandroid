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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

/**
 * Applies the admin-configured {@link FooterStyleConfig} (bottom-nav footer icon
 * size + label typography + icon/label colors + center "+" FAB size) at render
 * time. Reads the JSON cached in SharedPrefs from the splash config. If nothing
 * is cached the footer is left untouched (compiled selector + sizes). Defaults
 * on the server match compiled values (icon 24dp, labels 11/12sp bold, FAB
 * 42/30) so an unedited admin panel reproduces the current look.
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

        applyIconAndLabelColors(nav, cfg);

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

    /**
     * Resolves the ColorStateList used for footer icon + label active/inactive colors
     * (admin footer_style label colors, else the compiled selector). Useful after
     * programmatically rebinding menu ImageViews so checked orange still applies.
     */
    public static ColorStateList resolveIconTextTint(Context context) {
        if (context == null) {
            return null;
        }
        FooterStyleConfig cfg = load(context);
        ColorStateList fromAdmin = buildColorStateList(cfg);
        if (fromAdmin != null) {
            return fromAdmin;
        }
        try {
            return androidx.core.content.ContextCompat.getColorStateList(
                    context, com.anyNews.anynews.R.color.selector_item_primary_color);
        } catch (Exception e) {
            Log.e(TAG, "resolveIconTextTint failed", e);
            return null;
        }
    }

    /**
     * Applies admin-configured center "+" FAB diameter and plus-logo max size.
     * Prefers {@code footer_plus_icon_size_dp} from ProfileHub prefs; falls back to
     * footer_style fab_size_dp / fab_icon_size_dp. Defaults (42dp / 30dp) match
     * activity_home_page.xml when nothing is cached.
     */
    public static void applyFab(Context context, FloatingActionButton fab) {
        if (context == null || fab == null) {
            return;
        }
        try {
            float density = context.getResources().getDisplayMetrics().density;
            FooterStyleConfig cfg = load(context);
            // Top-level splash key takes priority over nested footer_style JSON.
            float fabDp = ProfileHubConfigHelper.getFooterPlusIconSizeDp(context);
            float iconDp = cfg != null ? cfg.getFabIconSizeDp() : 30f;
            // If plus size was never persisted, honor footer_style.fab_size_dp when present.
            String plusRaw = SharedPrefsData.getString(
                    context, Constants.FOOTER_PLUS_ICON_SIZE_DP, Constants.PREF_NAME);
            if (TextUtils.isEmpty(plusRaw) && cfg != null) {
                fabDp = cfg.getFabSizeDp();
            }
            if (fabDp > 0) {
                if (fabDp < 32f) fabDp = 32f;
                if (fabDp > 72f) fabDp = 72f;
                int fabPx = Math.round(fabDp * density);
                fab.setCustomSize(fabPx);
                ViewGroup.LayoutParams lp = fab.getLayoutParams();
                if (lp != null) {
                    lp.width = fabPx;
                    lp.height = fabPx;
                    fab.setLayoutParams(lp);
                }
            }
            if (iconDp > 0) {
                // Scale plus glyph roughly to FAB diameter when plus size is large.
                float cappedIcon = Math.min(iconDp, fabDp * 0.72f);
                if (cappedIcon < 20f) cappedIcon = 20f;
                int iconPx = Math.round(cappedIcon * density);
                try {
                    fab.getClass()
                            .getMethod("setMaxImageSize", int.class)
                            .invoke(fab, iconPx);
                } catch (ReflectiveOperationException ignored) {
                    android.graphics.drawable.Drawable d = fab.getDrawable();
                    if (d != null) {
                        d = d.mutate();
                        d.setBounds(0, 0, iconPx, iconPx);
                        fab.setImageDrawable(d);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "fab size failed", e);
        }
    }

    /**
     * Admin label_color / label_active_color drive both text and icon tints so
     * Special + Profile match Reload when selected (brand orange). Empty colors
     * leave the compiled {@code selector_item_primary_color} in place.
     */
    private static void applyIconAndLabelColors(BottomNavigationView nav, FooterStyleConfig cfg) {
        ColorStateList colors = buildColorStateList(cfg);
        if (colors == null) {
            return;
        }
        try {
            nav.setItemTextColor(colors);
            nav.setItemIconTintList(colors);
        } catch (Exception e) {
            Log.e(TAG, "icon/text color failed", e);
        }
    }

    private static ColorStateList buildColorStateList(FooterStyleConfig cfg) {
        if (cfg == null) {
            return null;
        }
        String normal = cfg.getLabelColor();
        String active = cfg.getLabelActiveColor();
        // Both empty → keep the compiled selector (current look).
        if (TextUtils.isEmpty(normal) && TextUtils.isEmpty(active)) {
            return null;
        }
        Integer normalColor = parseColor(!TextUtils.isEmpty(normal) ? normal : active);
        Integer activeColor = parseColor(!TextUtils.isEmpty(active) ? active : normal);
        if (normalColor == null || activeColor == null) {
            return null;
        }
        int[][] states = {
                {android.R.attr.state_checked},
                {-android.R.attr.state_checked}
        };
        int[] colors = {activeColor, normalColor};
        return new ColorStateList(states, colors);
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

    /**
     * Center Share glyph on magazine / special cards (the middle footer button).
     * Compiled layout was 24dp inside a 50dp bar. Missing prefs use 32dp.
     * This is not the article action-row share icon.
     */
    public static final int MAGAZINE_SHARE_ICON_DEFAULT_DP = 32;
    public static final int MAGAZINE_SHARE_ICON_MIN_DP = 20;
    public static final int MAGAZINE_SHARE_ICON_MAX_DP = 36;
    /** Current compiled bar when the share icon is the old 24dp size. */
    private static final int FOOTER_BAR_BASE_DP = 50;
    /**
     * Padding + label under the center Share icon. Bar grows with the icon
     * so the Share label and the other tab labels stay inside the bar.
     */
    private static final int FOOTER_SHARE_CHROME_DP = 20;
    /** Cap so a large Share icon cannot climb over the magazine page dots. */
    private static final int FOOTER_BAR_MAX_DP = 56;

    public static int clampMagazineShareIconDp(int dp) {
        if (dp < MAGAZINE_SHARE_ICON_MIN_DP) return MAGAZINE_SHARE_ICON_MIN_DP;
        if (dp > MAGAZINE_SHARE_ICON_MAX_DP) return MAGAZINE_SHARE_ICON_MAX_DP;
        return dp;
    }

    /** Pref missing or unset → {@link #MAGAZINE_SHARE_ICON_DEFAULT_DP}. */
    public static int readMagazineShareIconDp(Context context) {
        if (context == null) {
            return MAGAZINE_SHARE_ICON_DEFAULT_DP;
        }
        try {
            android.content.SharedPreferences sp = context.getSharedPreferences(
                    Constants.PREF_NAME, Context.MODE_PRIVATE);
            if (!sp.contains(Constants.MAGAZINE_SHARE_ICON_SIZE_DP)) {
                return MAGAZINE_SHARE_ICON_DEFAULT_DP;
            }
            int stored = sp.getInt(
                    Constants.MAGAZINE_SHARE_ICON_SIZE_DP, MAGAZINE_SHARE_ICON_DEFAULT_DP);
            if (stored <= 0) {
                return MAGAZINE_SHARE_ICON_DEFAULT_DP;
            }
            return clampMagazineShareIconDp(stored);
        } catch (Exception e) {
            return MAGAZINE_SHARE_ICON_DEFAULT_DP;
        }
    }

    /** Footer bar height that fits this Share icon plus its label. */
    public static int footerBarHeightDpForShareIcon(int iconDp) {
        int needed = clampMagazineShareIconDp(iconDp) + FOOTER_SHARE_CHROME_DP;
        if (needed < FOOTER_BAR_BASE_DP) needed = FOOTER_BAR_BASE_DP;
        if (needed > FOOTER_BAR_MAX_DP) needed = FOOTER_BAR_MAX_DP;
        return needed;
    }

    /**
     * Sizes the center magazine Share icon and grows the footer bar just enough
     * for that icon and the News / Special / Daily / Profile labels. The Share
     * control stays inside the bar (it does not draw over the page dots).
     * {@code shareColumn} and {@code shareIcon} may be null (profile footer).
     */
    public static void applyMagazineShareChrome(
            Context context,
            View footerBar,
            BottomNavigationView nav,
            View shareColumn,
            android.widget.ImageView shareIcon) {
        if (context == null) {
            return;
        }
        try {
            float density = context.getResources().getDisplayMetrics().density;
            int iconDp = readMagazineShareIconDp(context);
            int barPx = Math.round(footerBarHeightDpForShareIcon(iconDp) * density);
            int iconPx = Math.round(iconDp * density);
            setViewHeightPx(footerBar, barPx);
            setViewHeightPx(nav, barPx);
            setViewHeightPx(shareColumn, barPx);
            if (shareIcon != null) {
                ViewGroup.LayoutParams lp = shareIcon.getLayoutParams();
                if (lp != null && (lp.width != iconPx || lp.height != iconPx)) {
                    lp.width = iconPx;
                    lp.height = iconPx;
                    shareIcon.setLayoutParams(lp);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "magazine share size failed", e);
        }
    }

    /**
     * Footer WhatsApp in place of the center + (28 July 2026: 48dp,
     * {@code ic_whatsapp_home}, no tint, no count). Does not change footer
     * bar height or the magazine Share icon.
     */
    public static final int FOOTER_WHATSAPP_ICON_DEFAULT_DP = 48;
    public static final int FOOTER_WHATSAPP_ICON_MIN_DP = 24;
    public static final int FOOTER_WHATSAPP_ICON_MAX_DP = 48;

    public static int clampFooterWhatsappIconDp(int dp) {
        if (dp < FOOTER_WHATSAPP_ICON_MIN_DP) return FOOTER_WHATSAPP_ICON_MIN_DP;
        if (dp > FOOTER_WHATSAPP_ICON_MAX_DP) return FOOTER_WHATSAPP_ICON_MAX_DP;
        return dp;
    }

    public static int readFooterWhatsappIconDp(Context context) {
        if (context == null) {
            return FOOTER_WHATSAPP_ICON_DEFAULT_DP;
        }
        try {
            android.content.SharedPreferences sp = context.getSharedPreferences(
                    Constants.PREF_NAME, Context.MODE_PRIVATE);
            if (!sp.contains(Constants.FOOTER_WHATSAPP_ICON_SIZE_DP)) {
                return FOOTER_WHATSAPP_ICON_DEFAULT_DP;
            }
            int stored = sp.getInt(
                    Constants.FOOTER_WHATSAPP_ICON_SIZE_DP, FOOTER_WHATSAPP_ICON_DEFAULT_DP);
            if (stored <= 0) {
                return FOOTER_WHATSAPP_ICON_DEFAULT_DP;
            }
            return clampFooterWhatsappIconDp(stored);
        } catch (Exception e) {
            return FOOTER_WHATSAPP_ICON_DEFAULT_DP;
        }
    }

    /** Sizes only the footer WhatsApp ImageView. Bar height stays with magazine Share. */
    public static void applyFooterWhatsappIcon(Context context, android.widget.ImageView icon) {
        if (context == null || icon == null) {
            return;
        }
        try {
            float density = context.getResources().getDisplayMetrics().density;
            int px = Math.round(readFooterWhatsappIconDp(context) * density);
            ViewGroup.LayoutParams lp = icon.getLayoutParams();
            if (lp != null && (lp.width != px || lp.height != px)) {
                lp.width = px;
                lp.height = px;
                icon.setLayoutParams(lp);
            }
        } catch (Exception e) {
            Log.e(TAG, "footer whatsapp size failed", e);
        }
    }

    private static void setViewHeightPx(View view, int heightPx) {
        if (view == null || heightPx <= 0) {
            return;
        }
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (lp == null || lp.height == heightPx) {
            return;
        }
        lp.height = heightPx;
        view.setLayoutParams(lp);
    }
}
