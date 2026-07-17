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
        if (!TextUtils.isEmpty(color)) {
            // Admin explicitly set a color -> honor it verbatim (override wins).
            try {
                tv.setTextColor(Color.parseColor(color.trim()));
            } catch (IllegalArgumentException ignored) {
            }
        } else {
            // No admin color -> use a theme-aware default so the writer strip is
            // readable in BOTH light and dark mode (text_color_primary flips to
            // white via values-night). Previously the default resolved to black
            // (#000000), which was invisible on the dark card.
            try {
                tv.setTextColor(ContextCompat.getColor(context, R.color.text_color_primary));
            } catch (Exception ignored) {
            }
        }
    }
}
