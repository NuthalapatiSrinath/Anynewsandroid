package com.anyNews.anynews.Utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.anyNews.anynews.Pojo.WriterStripStyleConfig;
import com.anyNews.anynews.R;
import com.google.gson.Gson;

/**
 * Applies the admin-configured {@link WriterStripStyleConfig} (writer strip
 * name / designation / time typography and the right-side location pill) at
 * render time. Reads the JSON cached in SharedPrefs from the splash config. If
 * nothing is cached the strip is left untouched, preserving the current
 * compiled look. Sizes are treated as sdp to match the layouts' @dimen/_Nsdp
 * values; the location offset is in dp. Because the backend serves defaults
 * that equal the current hardcoded values, applying them reproduces the
 * current look exactly.
 */
public final class WriterStripStyleHelper {

    private static final String TAG = "AnyNewsWriterStyle";
    private static final Gson GSON = new Gson();

    private WriterStripStyleHelper() {
    }

    public static WriterStripStyleConfig load(Context context) {
        if (context == null) {
            return null;
        }
        String json = SharedPrefsData.getString(context, Constants.WRITER_STRIP_STYLE, Constants.PREF_NAME);
        if (TextUtils.isEmpty(json)) {
            return null;
        }
        try {
            return GSON.fromJson(json, WriterStripStyleConfig.class);
        } catch (Exception e) {
            Log.e(TAG, "load parse failed", e);
            return null;
        }
    }

    public static void apply(
            Context context,
            TextView name,
            TextView designation,
            TextView time,
            TextView location,
            View locationPill
    ) {
        if (context == null) {
            return;
        }
        WriterStripStyleConfig cfg = load(context);
        if (cfg == null) {
            // No admin config cached: the compiled layouts use @color/black,
            // which day/night remaps to white in dark mode. Re-assert the
            // theme-aware colour anyway so the strip is guaranteed readable in
            // BOTH themes even if a stale literal colour was applied earlier.
            applyThemeColor(context, name);
            applyThemeColor(context, designation);
            applyThemeColor(context, time);
            applyThemeColor(context, location);
            return;
        }
        styleText(context, name, cfg.getNameSizeSdp(), cfg.isNameBold(), cfg.getNameFontFamily(), cfg.getNameColor());
        styleText(context, designation, cfg.getDesignationSizeSdp(), cfg.isDesignationBold(), cfg.getDesignationFontFamily(), cfg.getDesignationColor());
        styleText(context, time, cfg.getTimeSizeSdp(), cfg.isTimeBold(), cfg.getTimeFontFamily(), cfg.getTimeColor());
        // Location pill: no bold flag (the my_feed TextView is not bold by default).
        styleText(context, location, cfg.getLocationSizeSdp(), null, cfg.getLocationFontFamily(), cfg.getLocationColor());

        if (locationPill != null) {
            try {
                float px = cfg.getLocationRightOffsetDp() * context.getResources().getDisplayMetrics().density;
                locationPill.setTranslationX(px);
            } catch (Exception e) {
                Log.e(TAG, "location offset failed", e);
            }
        }
    }

    private static void styleText(
            Context context,
            TextView tv,
            float sizeSdp,
            Boolean bold,
            String fontFamily,
            String color
    ) {
        if (tv == null) {
            return;
        }
        if (sizeSdp > 0) {
            FontSettingsHelper.applyTextSize(context, tv, sizeSdp);
        }
        if (!TextUtils.isEmpty(fontFamily)) {
            FontSettingsHelper.applyTypeface(context, tv, fontFamily);
        }
        if (bold != null) {
            tv.setTypeface(tv.getTypeface(), bold ? Typeface.BOLD : Typeface.NORMAL);
        }
        if (isThemeDefaultColor(color)) {
            // No admin colour, OR the admin colour is plain black (which is the
            // backend/admin DEFAULT, e.g. "#000000"). Plain black is NOT a real
            // choice here: it is invisible on the dark card in night mode, so we
            // treat it as "use the theme default" and fall back to the
            // day/night-aware text_color_primary (black in light, white in dark).
            applyThemeColor(context, tv);
        } else {
            // Admin explicitly picked a non-black colour -> honour it verbatim.
            try {
                tv.setTextColor(Color.parseColor(color.trim()));
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    /**
     * Applies the day/night-aware {@code text_color_primary} (black in light
     * mode, white in dark mode) so writer-strip text is always readable.
     */
    private static void applyThemeColor(Context context, TextView tv) {
        if (context == null || tv == null) {
            return;
        }
        try {
            tv.setTextColor(ContextCompat.getColor(context, R.color.text_color_primary));
        } catch (Exception ignored) {
        }
    }

    /**
     * Returns {@code true} when the colour should be ignored in favour of the
     * theme-aware default: either unset/blank, unparseable, or plain black
     * (the shipped default) which would be invisible in dark mode.
     */
    private static boolean isThemeDefaultColor(String color) {
        if (TextUtils.isEmpty(color)) {
            return true;
        }
        try {
            int c = Color.parseColor(color.trim());
            // Ignore alpha; treat any fully-opaque/!opaque pure black as default.
            return (c & 0x00FFFFFF) == 0x000000;
        } catch (IllegalArgumentException e) {
            return true;
        }
    }
}
