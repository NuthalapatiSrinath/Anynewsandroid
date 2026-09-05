package com.anyNews.anynews.Utils;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Admin-tunable main News FlipView content-tap modes.
 * Values arrive via splash / session:
 * {@code news_tap_mode}, {@code news_tap_half_upper_pct},
 * {@code news_tap_full_upper_pct}, {@code news_tap_full_right_rail_pct},
 * {@code news_tap_half_hide_engagement}.
 *
 * <ul>
 *   <li>{@link #MODE_CLASSIC} — hide reporter/district footer (half) or likes/comments rail (full)</li>
 *   <li>{@link #MODE_READER} — News|Read (Chadivinavi) chrome primary (previous default)</li>
 *   <li>{@link #MODE_HYBRID} — upper Y-band → Reader; lower / right-rail → Classic (default)</li>
 * </ul>
 */
public final class NewsTapModeConfig {

    public static final String MODE_CLASSIC = "classic";
    public static final String MODE_READER = "reader";
    public static final String MODE_HYBRID = "hybrid";

    public static final String DEFAULT_MODE = MODE_HYBRID;
    public static final int DEFAULT_HALF_UPPER_PCT = 50;
    public static final int DEFAULT_FULL_UPPER_PCT = 50;
    public static final int DEFAULT_FULL_RIGHT_RAIL_PCT = 28;
    /** Default OFF: Classic hide on half only toggles writers row. */
    public static final boolean DEFAULT_HALF_HIDE_ENGAGEMENT = false;
    public static final int MIN_PCT = 10;
    public static final int MAX_PCT = 90;

    private NewsTapModeConfig() {}

    /** Persist splash/session values when present (null = old server; leave cache). */
    public static void saveFromServer(
            @Nullable Context context,
            @Nullable String mode,
            @Nullable Integer halfUpperPct,
            @Nullable Integer fullUpperPct,
            @Nullable Integer fullRightRailPct) {
        saveFromServer(context, mode, halfUpperPct, fullUpperPct, fullRightRailPct, null);
    }

    /** Persist splash/session values when present (null = old server; leave cache). */
    public static void saveFromServer(
            @Nullable Context context,
            @Nullable String mode,
            @Nullable Integer halfUpperPct,
            @Nullable Integer fullUpperPct,
            @Nullable Integer fullRightRailPct,
            @Nullable Boolean halfHideEngagement) {
        if (context == null) return;
        Context app = context.getApplicationContext();
        if (mode != null) {
            SharedPrefsData.putString(
                    app, Constants.NEWS_TAP_MODE, normalizeMode(mode), Constants.PREF_NAME);
        }
        if (halfUpperPct != null) {
            SharedPrefsData.putString(
                    app,
                    Constants.NEWS_TAP_HALF_UPPER_PCT,
                    String.valueOf(clampPct(halfUpperPct, DEFAULT_HALF_UPPER_PCT)),
                    Constants.PREF_NAME);
        }
        if (fullUpperPct != null) {
            SharedPrefsData.putString(
                    app,
                    Constants.NEWS_TAP_FULL_UPPER_PCT,
                    String.valueOf(clampPct(fullUpperPct, DEFAULT_FULL_UPPER_PCT)),
                    Constants.PREF_NAME);
        }
        if (fullRightRailPct != null) {
            SharedPrefsData.putString(
                    app,
                    Constants.NEWS_TAP_FULL_RIGHT_RAIL_PCT,
                    String.valueOf(clampPct(fullRightRailPct, DEFAULT_FULL_RIGHT_RAIL_PCT)),
                    Constants.PREF_NAME);
        }
        if (halfHideEngagement != null) {
            SharedPrefsData.putString(
                    app,
                    Constants.NEWS_TAP_HALF_HIDE_ENGAGEMENT,
                    halfHideEngagement ? "true" : "false",
                    Constants.PREF_NAME);
        }
    }

    @NonNull
    public static String getMode(@Nullable Context context) {
        if (context == null) return DEFAULT_MODE;
        try {
            String v =
                    SharedPrefsData.getString(
                            context.getApplicationContext(),
                            Constants.NEWS_TAP_MODE,
                            Constants.PREF_NAME);
            if (TextUtils.isEmpty(v)) return DEFAULT_MODE;
            return normalizeMode(v);
        } catch (Exception ignored) {
            return DEFAULT_MODE;
        }
    }

    public static int getHalfUpperPct(@Nullable Context context) {
        return readPct(context, Constants.NEWS_TAP_HALF_UPPER_PCT, DEFAULT_HALF_UPPER_PCT);
    }

    public static int getFullUpperPct(@Nullable Context context) {
        return readPct(context, Constants.NEWS_TAP_FULL_UPPER_PCT, DEFAULT_FULL_UPPER_PCT);
    }

    public static int getFullRightRailPct(@Nullable Context context) {
        return readPct(
                context, Constants.NEWS_TAP_FULL_RIGHT_RAIL_PCT, DEFAULT_FULL_RIGHT_RAIL_PCT);
    }

    /**
     * When true, Classic hide on half articles also hides likes/comments/share/flag
     * together with the writers/reporter row. Default false.
     */
    public static boolean halfHideEngagementWithWriters(@Nullable Context context) {
        if (context == null) return DEFAULT_HALF_HIDE_ENGAGEMENT;
        try {
            String v =
                    SharedPrefsData.getString(
                            context.getApplicationContext(),
                            Constants.NEWS_TAP_HALF_HIDE_ENGAGEMENT,
                            Constants.PREF_NAME);
            if (TextUtils.isEmpty(v)) return DEFAULT_HALF_HIDE_ENGAGEMENT;
            return "true".equalsIgnoreCase(v.trim()) || "1".equals(v.trim());
        } catch (Exception ignored) {
            return DEFAULT_HALF_HIDE_ENGAGEMENT;
        }
    }

    public static boolean isClassic(@Nullable Context context) {
        return MODE_CLASSIC.equals(getMode(context));
    }

    public static boolean isReader(@Nullable Context context) {
        return MODE_READER.equals(getMode(context));
    }

    public static boolean isHybrid(@Nullable Context context) {
        return MODE_HYBRID.equals(getMode(context));
    }

    @NonNull
    private static String normalizeMode(@Nullable String raw) {
        String m = raw == null ? "" : raw.trim().toLowerCase();
        if (MODE_CLASSIC.equals(m) || MODE_READER.equals(m) || MODE_HYBRID.equals(m)) {
            return m;
        }
        return DEFAULT_MODE;
    }

    private static int readPct(@Nullable Context context, String key, int fallback) {
        if (context == null) return fallback;
        try {
            String v =
                    SharedPrefsData.getString(
                            context.getApplicationContext(), key, Constants.PREF_NAME);
            if (TextUtils.isEmpty(v)) return fallback;
            return clampPct(Integer.parseInt(v.trim()), fallback);
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static int clampPct(int n, int fallback) {
        if (n < MIN_PCT || n > MAX_PCT) {
            if (n < 0 || n > 100) return fallback;
            if (n < MIN_PCT) return MIN_PCT;
            return MAX_PCT;
        }
        return n;
    }
}
