package com.anyNews.anynews.Utils;

import android.content.Context;

import androidx.annotation.Nullable;

/**
 * Admin-tunable in-app Search icon placement.
 * Values arrive via splash / session / feed poll:
 * {@code search_icon_on_engagement}, {@code search_icon_on_toggle},
 * {@code search_icon_size_dp}.
 *
 * <p>Null from server = leave cache (never wipe on partial poll).
 */
public final class SearchIconConfig {

    public static final boolean DEFAULT_ON_ENGAGEMENT = true;
    public static final boolean DEFAULT_ON_TOGGLE = false;
    public static final int DEFAULT_SIZE_DP = 22;
    public static final int MIN_SIZE_DP = 14;
    public static final int MAX_SIZE_DP = 40;

    private SearchIconConfig() {}

    public static void saveFromServer(
            @Nullable Context context,
            @Nullable Boolean onEngagement,
            @Nullable Boolean onToggle,
            @Nullable Integer sizeDp) {
        if (context == null) return;
        Context app = context.getApplicationContext();
        if (onEngagement != null) {
            SharedPrefsData.putBool(
                    app,
                    Constants.SEARCH_ICON_ON_ENGAGEMENT,
                    onEngagement,
                    Constants.PREF_NAME);
        }
        if (onToggle != null) {
            SharedPrefsData.putBool(
                    app,
                    Constants.SEARCH_ICON_ON_TOGGLE,
                    onToggle,
                    Constants.PREF_NAME);
        }
        if (sizeDp != null) {
            SharedPrefsData.putInt(
                    app,
                    Constants.SEARCH_ICON_SIZE_DP,
                    clampSize(sizeDp),
                    Constants.PREF_NAME);
        }
    }

    public static boolean isOnEngagement(@Nullable Context context) {
        if (context == null) return DEFAULT_ON_ENGAGEMENT;
        try {
            return SharedPrefsData.getBool(
                    context,
                    Constants.SEARCH_ICON_ON_ENGAGEMENT,
                    Constants.PREF_NAME,
                    DEFAULT_ON_ENGAGEMENT);
        } catch (Exception e) {
            return DEFAULT_ON_ENGAGEMENT;
        }
    }

    public static boolean isOnToggle(@Nullable Context context) {
        if (context == null) return DEFAULT_ON_TOGGLE;
        try {
            return SharedPrefsData.getBool(
                    context,
                    Constants.SEARCH_ICON_ON_TOGGLE,
                    Constants.PREF_NAME,
                    DEFAULT_ON_TOGGLE);
        } catch (Exception e) {
            return DEFAULT_ON_TOGGLE;
        }
    }

    public static int sizeDp(@Nullable Context context) {
        if (context == null) return DEFAULT_SIZE_DP;
        try {
            int stored =
                    SharedPrefsData.getInt(
                            context, Constants.SEARCH_ICON_SIZE_DP, Constants.PREF_NAME);
            if (stored <= 0) return DEFAULT_SIZE_DP;
            return clampSize(stored);
        } catch (Exception e) {
            return DEFAULT_SIZE_DP;
        }
    }

    private static int clampSize(int dp) {
        return Math.max(MIN_SIZE_DP, Math.min(MAX_SIZE_DP, dp));
    }
}
