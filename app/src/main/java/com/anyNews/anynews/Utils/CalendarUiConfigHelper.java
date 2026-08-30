package com.anyNews.anynews.Utils;

import android.content.Context;

import androidx.annotation.Nullable;

/**
 * Splash-delivered Telugu calendar UI sizing (Root_Settings → splash).
 * Missing / invalid values fall back to product defaults matching the tall-card design.
 */
public final class CalendarUiConfigHelper {

    public static final int DEFAULT_CARD_MIN_HEIGHT_DP = 220;
    public static final int DEFAULT_TITLE_SIZE_SP = 17;
    public static final int DEFAULT_ITEM_SIZE_SP = 15;
    public static final int DEFAULT_ITEM_PAD_DP = 5;
    /** Month block share of calendar page; lower → taller shubha/ashubha cards. */
    public static final float DEFAULT_MONTH_HEIGHT_FRACTION = 0.42f;
    /**
     * Selected-day blue circle diameter as % of {@link #SELECTED_CIRCLE_BASE_DP}.
     * Default 80 (~26dp) surrounds 14sp day digits with light padding and stays
     * above tithi/nakshatra lines.
     */
    public static final int DEFAULT_SELECTED_CIRCLE_RADIUS_PERCENT = 80;
    /** Base diameter (dp) when circle diameter percent is 100. */
    public static final int SELECTED_CIRCLE_BASE_DP = 32;

    private CalendarUiConfigHelper() {}

    /**
     * Persist optional size knobs from splash. Null leaves each cache entry untouched.
     */
    public static void saveFromSplash(
            @Nullable Context context,
            @Nullable Integer cardMinHeightDp,
            @Nullable Integer titleSizeSp,
            @Nullable Integer itemSizeSp,
            @Nullable Integer itemPadDp) {
        saveFromSplash(context, cardMinHeightDp, titleSizeSp, itemSizeSp, itemPadDp, null, null);
    }

    public static void saveFromSplash(
            @Nullable Context context,
            @Nullable Integer cardMinHeightDp,
            @Nullable Integer titleSizeSp,
            @Nullable Integer itemSizeSp,
            @Nullable Integer itemPadDp,
            @Nullable Float monthHeightFraction,
            @Nullable Integer selectedCircleRadiusPercent) {
        if (context == null) return;
        Context app = context.getApplicationContext();
        if (cardMinHeightDp != null) {
            SharedPrefsData.putInt(
                    app,
                    Constants.CALENDAR_DETAIL_CARD_MIN_HEIGHT_DP,
                    clamp(cardMinHeightDp, 160, 360, DEFAULT_CARD_MIN_HEIGHT_DP),
                    Constants.PREF_NAME);
        }
        if (titleSizeSp != null) {
            SharedPrefsData.putInt(
                    app,
                    Constants.CALENDAR_DETAIL_TITLE_SIZE_SP,
                    clamp(titleSizeSp, 12, 24, DEFAULT_TITLE_SIZE_SP),
                    Constants.PREF_NAME);
        }
        if (itemSizeSp != null) {
            SharedPrefsData.putInt(
                    app,
                    Constants.CALENDAR_DETAIL_ITEM_SIZE_SP,
                    clamp(itemSizeSp, 11, 22, DEFAULT_ITEM_SIZE_SP),
                    Constants.PREF_NAME);
        }
        if (itemPadDp != null) {
            SharedPrefsData.putInt(
                    app,
                    Constants.CALENDAR_DETAIL_ITEM_PAD_DP,
                    clamp(itemPadDp, 2, 16, DEFAULT_ITEM_PAD_DP),
                    Constants.PREF_NAME);
        }
        if (monthHeightFraction != null) {
            float f = monthHeightFraction;
            if (f > 1f) f = f / 100f;
            f = clampFloat(f, 0.35f, 0.70f, DEFAULT_MONTH_HEIGHT_FRACTION);
            // Store as percent int so 0 means “unset” in SharedPreferences.
            SharedPrefsData.putInt(
                    app,
                    Constants.CALENDAR_MONTH_HEIGHT_FRACTION,
                    Math.round(f * 100f),
                    Constants.PREF_NAME);
        }
        if (selectedCircleRadiusPercent != null) {
            SharedPrefsData.putInt(
                    app,
                    Constants.CALENDAR_SELECTED_CIRCLE_RADIUS_PERCENT,
                    clamp(
                            selectedCircleRadiusPercent,
                            40,
                            100,
                            DEFAULT_SELECTED_CIRCLE_RADIUS_PERCENT),
                    Constants.PREF_NAME);
        }
    }

    public static int getCardMinHeightDp(@Nullable Context context) {
        return readInt(context, Constants.CALENDAR_DETAIL_CARD_MIN_HEIGHT_DP,
                DEFAULT_CARD_MIN_HEIGHT_DP, 160, 360);
    }

    public static int getTitleSizeSp(@Nullable Context context) {
        return readInt(context, Constants.CALENDAR_DETAIL_TITLE_SIZE_SP,
                DEFAULT_TITLE_SIZE_SP, 12, 24);
    }

    public static int getItemSizeSp(@Nullable Context context) {
        return readInt(context, Constants.CALENDAR_DETAIL_ITEM_SIZE_SP,
                DEFAULT_ITEM_SIZE_SP, 11, 22);
    }

    public static int getItemPadDp(@Nullable Context context) {
        return readInt(context, Constants.CALENDAR_DETAIL_ITEM_PAD_DP,
                DEFAULT_ITEM_PAD_DP, 2, 16);
    }

    public static float getMonthHeightFraction(@Nullable Context context) {
        if (context == null) return DEFAULT_MONTH_HEIGHT_FRACTION;
        try {
            int pct = SharedPrefsData.getInt(
                    context.getApplicationContext(),
                    Constants.CALENDAR_MONTH_HEIGHT_FRACTION,
                    Constants.PREF_NAME);
            if (pct <= 0) return DEFAULT_MONTH_HEIGHT_FRACTION;
            return clampFloat(pct / 100f, 0.35f, 0.70f, DEFAULT_MONTH_HEIGHT_FRACTION);
        } catch (Exception ignored) {
            return DEFAULT_MONTH_HEIGHT_FRACTION;
        }
    }

    public static int getSelectedCircleRadiusPercent(@Nullable Context context) {
        return readInt(
                context,
                Constants.CALENDAR_SELECTED_CIRCLE_RADIUS_PERCENT,
                DEFAULT_SELECTED_CIRCLE_RADIUS_PERCENT,
                40,
                100);
    }

    /**
     * Resolved circle diameter in px for the selected day number.
     * Always a true circle: same width and height, centered on the date digits.
     */
    public static int getSelectedCircleDiameterPx(@Nullable Context context) {
        float density = 1f;
        if (context != null) {
            density = context.getResources().getDisplayMetrics().density;
        }
        int percent = getSelectedCircleRadiusPercent(context);
        int dp = Math.round(SELECTED_CIRCLE_BASE_DP * (percent / 100f));
        // Floor keeps two-digit day numbers readable inside the ring.
        dp = Math.max(18, Math.min(SELECTED_CIRCLE_BASE_DP, dp));
        return Math.round(dp * density);
    }

    private static int readInt(
            @Nullable Context context,
            String key,
            int def,
            int min,
            int max) {
        if (context == null) return def;
        try {
            int v = SharedPrefsData.getInt(
                    context.getApplicationContext(), key, Constants.PREF_NAME);
            if (v <= 0) return def;
            return clamp(v, min, max, def);
        } catch (Exception ignored) {
            return def;
        }
    }

    private static int clamp(int v, int min, int max, int def) {
        if (v < min || v > max) return def;
        return v;
    }

    private static float clampFloat(float v, float min, float max, float def) {
        if (v < min || v > max) return def;
        return v;
    }
}
