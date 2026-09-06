package com.anyNews.anynews.Utils;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Way2News-style live readers badge (half articles only).
 * Values arrive via splash / session / articles / get_settings:
 * {@code live_readers_enabled}, {@code live_readers_window_minutes},
 * {@code live_readers_show_seconds}, {@code live_readers_min_count}.
 *
 * Lookback window max is 360 minutes (6 hours) to match admin + Redis.
 */
public final class LiveReadersConfig {

    public static final boolean DEFAULT_ENABLED = false;
    public static final int DEFAULT_WINDOW_MINUTES = 120;
    public static final int MAX_WINDOW_MINUTES = 360;
    public static final int DEFAULT_SHOW_SECONDS = 4;
    public static final int DEFAULT_MIN_COUNT = 1;

    private LiveReadersConfig() {}

    public static void saveFromServer(
            @Nullable Context context,
            @Nullable Boolean enabled,
            @Nullable Integer windowMinutes,
            @Nullable Integer showSeconds,
            @Nullable Integer minCount) {
        if (context == null) return;
        Context app = context.getApplicationContext();
        if (enabled != null) {
            SharedPrefsData.putString(
                    app,
                    Constants.LIVE_READERS_ENABLED,
                    enabled ? "true" : "false",
                    Constants.PREF_NAME);
        }
        if (windowMinutes != null) {
            SharedPrefsData.putString(
                    app,
                    Constants.LIVE_READERS_WINDOW_MINUTES,
                    String.valueOf(clamp(windowMinutes, 1, MAX_WINDOW_MINUTES, DEFAULT_WINDOW_MINUTES)),
                    Constants.PREF_NAME);
        }
        if (showSeconds != null) {
            SharedPrefsData.putString(
                    app,
                    Constants.LIVE_READERS_SHOW_SECONDS,
                    String.valueOf(clamp(showSeconds, 1, 30, DEFAULT_SHOW_SECONDS)),
                    Constants.PREF_NAME);
        }
        if (minCount != null) {
            SharedPrefsData.putString(
                    app,
                    Constants.LIVE_READERS_MIN_COUNT,
                    String.valueOf(clamp(minCount, 0, 100000, DEFAULT_MIN_COUNT)),
                    Constants.PREF_NAME);
        }
    }

    public static boolean isEnabled(@Nullable Context context) {
        if (context == null) return DEFAULT_ENABLED;
        try {
            String v =
                    SharedPrefsData.getString(
                            context.getApplicationContext(),
                            Constants.LIVE_READERS_ENABLED,
                            Constants.PREF_NAME);
            if (TextUtils.isEmpty(v)) return DEFAULT_ENABLED;
            return "true".equalsIgnoreCase(v.trim()) || "1".equals(v.trim());
        } catch (Exception ignored) {
            return DEFAULT_ENABLED;
        }
    }

    public static int getWindowMinutes(@Nullable Context context) {
        return readInt(
                context,
                Constants.LIVE_READERS_WINDOW_MINUTES,
                1,
                MAX_WINDOW_MINUTES,
                DEFAULT_WINDOW_MINUTES);
    }

    public static int getShowSeconds(@Nullable Context context) {
        return readInt(context, Constants.LIVE_READERS_SHOW_SECONDS, 1, 30, DEFAULT_SHOW_SECONDS);
    }

    public static int getMinCount(@Nullable Context context) {
        return readInt(context, Constants.LIVE_READERS_MIN_COUNT, 0, 100000, DEFAULT_MIN_COUNT);
    }

    /**
     * Format like Way2News: "1.3 Lakh readers now" / "12.5K readers now" / "842 readers now".
     */
    @NonNull
    public static String formatLabel(int count) {
        int n = Math.max(0, count);
        if (n >= 100000) {
            double lakhs = n / 100000.0;
            String num =
                    lakhs >= 10
                            ? String.valueOf(Math.round(lakhs))
                            : trimOneDecimal(lakhs);
            return num + " Lakh readers now";
        }
        if (n >= 1000) {
            double k = n / 1000.0;
            String num = k >= 100 ? String.valueOf(Math.round(k)) : trimOneDecimal(k);
            return num + "K readers now";
        }
        return n + " readers now";
    }

    @NonNull
    private static String trimOneDecimal(double v) {
        double rounded = Math.round(v * 10.0) / 10.0;
        if (Math.abs(rounded - Math.rint(rounded)) < 0.05) {
            return String.valueOf((long) Math.rint(rounded));
        }
        return String.valueOf(rounded);
    }

    private static int readInt(
            @Nullable Context context, String key, int min, int max, int fallback) {
        if (context == null) return fallback;
        try {
            String v =
                    SharedPrefsData.getString(
                            context.getApplicationContext(), key, Constants.PREF_NAME);
            if (TextUtils.isEmpty(v)) return fallback;
            return clamp(Integer.parseInt(v.trim()), min, max, fallback);
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static int clamp(int n, int min, int max, int fallback) {
        if (n < min || n > max) {
            if (n < 0) return fallback;
            if (n < min) return min;
            if (n > max) return max;
        }
        return n;
    }
}
