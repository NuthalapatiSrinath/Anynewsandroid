package com.anyNews.anynews.Utils;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.Nullable;

/**
 * How long to keep the news-card the user was on after the app leaves the
 * foreground (share to WhatsApp, Home, recents).
 *
 * <p>The minutes value is cached from splash / session
 * ({@code feed_position_keep_minutes}). The check itself is a local timestamp
 * written when the process goes to the background. Coming back inside the
 * window must not call the server.
 *
 * <p>Missing / old server → {@link #DEFAULT_MINUTES} (15). This is not
 * {@link OpenPathCache#SPLASH_TTL_MS}.
 */
public final class FeedPositionKeepConfig {

    public static final int DEFAULT_MINUTES = 15;
    public static final int MIN_MINUTES = 1;
    public static final int MAX_MINUTES = 180;

    private FeedPositionKeepConfig() {}

    /** Persist admin value when present (null = old server; leave cache / default). */
    public static void saveMinutesFromServer(@Nullable Context context, @Nullable Integer minutes) {
        if (context == null || minutes == null) {
            return;
        }
        SharedPrefsData.putString(
                context.getApplicationContext(),
                Constants.FEED_POSITION_KEEP_MINUTES,
                String.valueOf(clamp(minutes)),
                Constants.PREF_NAME);
    }

    public static int getMinutes(@Nullable Context context) {
        if (context == null) {
            return DEFAULT_MINUTES;
        }
        try {
            String raw = SharedPrefsData.getString(
                    context.getApplicationContext(),
                    Constants.FEED_POSITION_KEEP_MINUTES,
                    Constants.PREF_NAME);
            if (TextUtils.isEmpty(raw)) {
                return DEFAULT_MINUTES;
            }
            return clamp(Integer.parseInt(raw.trim()));
        } catch (Exception ignored) {
            return DEFAULT_MINUTES;
        }
    }

    /**
     * Call when the whole process leaves the foreground. Wall clock, so a
     * later activity recreate can still see it. Does not touch the network.
     */
    public static void noteLeftForeground(
            @Nullable Context context, int flipIndex, long postId) {
        if (context == null) {
            return;
        }
        try {
            context.getApplicationContext()
                    .getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putString(
                            Constants.FEED_POSITION_LEFT_AT,
                            String.valueOf(System.currentTimeMillis()))
                    .putInt(Constants.FEED_POSITION_INDEX, Math.max(0, flipIndex))
                    .putString(Constants.FEED_POSITION_POST_ID, String.valueOf(postId))
                    .commit();
        } catch (Exception ignored) {
        }
    }

    /** True when the user left recently enough that the same card should stay. */
    public static boolean shouldKeepPosition(@Nullable Context context) {
        if (context == null) {
            return false;
        }
        long leftAt = readLeftAt(context);
        if (leftAt <= 0L) {
            return false;
        }
        long windowMs = getMinutes(context) * 60_000L;
        long elapsed = System.currentTimeMillis() - leftAt;
        return elapsed >= 0L && elapsed < windowMs;
    }

    public static int savedIndex(@Nullable Context context) {
        if (context == null) {
            return 0;
        }
        try {
            return Math.max(
                    0,
                    SharedPrefsData.getInt(
                            context.getApplicationContext(),
                            Constants.FEED_POSITION_INDEX,
                            Constants.PREF_NAME));
        } catch (Exception ignored) {
            return 0;
        }
    }

    public static long savedPostId(@Nullable Context context) {
        if (context == null) {
            return -1L;
        }
        try {
            String raw = SharedPrefsData.getString(
                    context.getApplicationContext(),
                    Constants.FEED_POSITION_POST_ID,
                    Constants.PREF_NAME);
            if (TextUtils.isEmpty(raw)) {
                return -1L;
            }
            return Long.parseLong(raw.trim());
        } catch (Exception ignored) {
            return -1L;
        }
    }

    /** Explicit reload, push open, or a return after the window. */
    public static void clear(@Nullable Context context) {
        if (context == null) {
            return;
        }
        try {
            context.getApplicationContext()
                    .getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putString(Constants.FEED_POSITION_LEFT_AT, "0")
                    .putInt(Constants.FEED_POSITION_INDEX, 0)
                    .putString(Constants.FEED_POSITION_POST_ID, "-1")
                    .commit();
        } catch (Exception ignored) {
        }
    }

    private static long readLeftAt(@Nullable Context context) {
        try {
            String raw = SharedPrefsData.getString(
                    context.getApplicationContext(),
                    Constants.FEED_POSITION_LEFT_AT,
                    Constants.PREF_NAME);
            if (TextUtils.isEmpty(raw)) {
                return 0L;
            }
            return Long.parseLong(raw.trim());
        } catch (Exception ignored) {
            return 0L;
        }
    }

    private static int clamp(int n) {
        if (n < MIN_MINUTES) {
            return DEFAULT_MINUTES;
        }
        if (n > MAX_MINUTES) {
            return MAX_MINUTES;
        }
        return n;
    }
}
