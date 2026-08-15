package com.anyNews.anynews.Utils;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.Nullable;

/**
 * Admin-tunable Read / చదివినవి visibility window (hours).
 * Value arrives via splash / session ({@code read_history_hours}).
 *
 * <ul>
 *   <li>Default / missing / invalid → {@link #DEFAULT_HOURS} (24)</li>
 *   <li>Range {@link #MIN_HOURS}–{@link #MAX_HOURS} (1 week)</li>
 *   <li>Only the Read tab list + badge use this; News still strips all committed reads</li>
 * </ul>
 */
public final class ReadHistoryConfig {

    public static final int DEFAULT_HOURS = 24;
    public static final int MIN_HOURS = 1;
    public static final int MAX_HOURS = 168;

    private ReadHistoryConfig() {}

    /** Persist admin value when present (null = old server; leave cache / default). */
    public static void saveHoursFromServer(
            @Nullable Context context, @Nullable Integer hours) {
        if (context == null || hours == null) return;
        int safe = clamp(hours);
        SharedPrefsData.putString(
                context.getApplicationContext(),
                Constants.READ_HISTORY_HOURS,
                String.valueOf(safe),
                Constants.PREF_NAME);
    }

    /**
     * Hours of recently-read articles to show under Read. Always ≥ 1.
     * When unset falls back to {@link #DEFAULT_HOURS}.
     */
    public static int getHours(@Nullable Context context) {
        if (context == null) return DEFAULT_HOURS;
        try {
            String v =
                    SharedPrefsData.getString(
                            context.getApplicationContext(),
                            Constants.READ_HISTORY_HOURS,
                            Constants.PREF_NAME);
            if (TextUtils.isEmpty(v)) return DEFAULT_HOURS;
            return clamp(Integer.parseInt(v.trim()));
        } catch (Exception ignored) {
            return DEFAULT_HOURS;
        }
    }

    /** Window length in milliseconds for timestamp filtering. */
    public static long getWindowMs(@Nullable Context context) {
        return getHours(context) * 60L * 60L * 1000L;
    }

    private static int clamp(int n) {
        if (n < MIN_HOURS) {
            // 0 / negative / junk → default rather than floor-to-1 guessing intent.
            if (n <= 0) return DEFAULT_HOURS;
            return MIN_HOURS;
        }
        if (n > MAX_HOURS) return MAX_HOURS;
        return n;
    }
}
