package com.anyNews.anynews.Utils;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.Nullable;

/**
 * Admin-tunable Status Videos lookback window (days).
 * Value arrives via splash / session ({@code status_videos_days}).
 *
 * <ul>
 *   <li>Default / missing / invalid → {@link #DEFAULT_DAYS} (5)</li>
 *   <li>Range {@link #MIN_DAYS}–{@link #MAX_DAYS}</li>
 *   <li>Used for disk-cache / Special-fallback client filter; pack is server-gated</li>
 * </ul>
 */
public final class StatusVideosDaysConfig {

    public static final int DEFAULT_DAYS = 5;
    public static final int MIN_DAYS = 1;
    public static final int MAX_DAYS = 30;

    private StatusVideosDaysConfig() {}

    /** Persist admin value when present (null = old server; leave cache / default). */
    public static void saveDaysFromServer(
            @Nullable Context context, @Nullable Integer days) {
        if (context == null || days == null) return;
        int safe = clamp(days);
        SharedPrefsData.putString(
                context.getApplicationContext(),
                Constants.STATUS_VIDEOS_DAYS,
                String.valueOf(safe),
                Constants.PREF_NAME);
    }

    /**
     * Lookback days for Status Videos client filter. Always ≥ 1.
     * When unset falls back to {@link #DEFAULT_DAYS}.
     */
    public static int getDays(@Nullable Context context) {
        if (context == null) return DEFAULT_DAYS;
        try {
            String v =
                    SharedPrefsData.getString(
                            context.getApplicationContext(),
                            Constants.STATUS_VIDEOS_DAYS,
                            Constants.PREF_NAME);
            if (TextUtils.isEmpty(v)) return DEFAULT_DAYS;
            return clamp(Integer.parseInt(v.trim()));
        } catch (Exception ignored) {
            return DEFAULT_DAYS;
        }
    }

    /** Window length in milliseconds for published_time filtering. */
    public static long getWindowMs(@Nullable Context context) {
        return getDays(context) * 24L * 60L * 60L * 1000L;
    }

    private static int clamp(int n) {
        if (n < MIN_DAYS) {
            if (n <= 0) return DEFAULT_DAYS;
            return MIN_DAYS;
        }
        if (n > MAX_DAYS) return MAX_DAYS;
        return n;
    }
}
