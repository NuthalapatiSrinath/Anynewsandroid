package com.anyNews.anynews.Utils;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.Nullable;

/**
 * How long to keep the news card after the user leaves to share (WhatsApp).
 *
 * <p>The minutes value is cached from splash / session
 * ({@code feed_position_keep_minutes}). The seat is written when a share
 * starts, and the clock starts when that share actually puts the process in
 * the background. Coming back inside the window must not call the server.
 * Closing the app, the launcher, and a background that was not a share do
 * not keep the card — those opens start at the newest card.
 *
 * <p>The “waiting to come back from this share” flag lives only in this
 * process. A swipe-away or a new process cannot replay the seat.
 *
 * <p>Missing / old server → {@link #DEFAULT_MINUTES} (15). This is not
 * {@link OpenPathCache#SPLASH_TTL_MS}.
 */
public final class FeedPositionKeepConfig {

    public static final int DEFAULT_MINUTES = 15;
    public static final int MIN_MINUTES = 1;
    public static final int MAX_MINUTES = 180;

    /**
     * Set only after this process backgrounds because of a share. Dies with
     * the process, so a later cold open cannot treat a saved seat as a return.
     */
    private static boolean awaitingShareReturn = false;

    private FeedPositionKeepConfig() {}

    /** True only until the next foreground of the process that left to share. */
    public static boolean isAwaitingShareReturn() {
        return awaitingShareReturn;
    }

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
        int languageId = 0;
        if (context != null) {
            try {
                languageId = SharedPrefsData.getInt(
                        context.getApplicationContext(),
                        Constants.LanguageID,
                        Constants.PREF_NAME);
            } catch (Exception ignored) {
            }
        }
        noteLeftForeground(context, flipIndex, postId, languageId);
    }

    /**
     * Same as {@link #noteLeftForeground(Context, int, long)} but the language
     * is the feed on screen, not a pref that a language tap may already have
     * changed. {@code fromShare} is false for a Home / recents departure.
     */
    public static void noteLeftForeground(
            @Nullable Context context, int flipIndex, long postId, int languageId) {
        noteLeftForeground(context, flipIndex, postId, languageId, false);
    }

    /**
     * @param fromShare true only when this background is a share sheet. A later
     *                  open keeps the card only for that case, and only inside
     *                  the admin window.
     */
    public static void noteLeftForeground(
            @Nullable Context context,
            int flipIndex,
            long postId,
            int languageId,
            boolean fromShare) {
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
                    .putInt(Constants.FEED_POSITION_LANGUAGE_ID, Math.max(0, languageId))
                    .putBoolean(Constants.FEED_POSITION_FROM_SHARE, fromShare)
                    .putBoolean(Constants.FEED_POSITION_SHARE_DEPARTURE, false)
                    .commit();
        } catch (Exception ignored) {
        }
    }

    /**
     * Call when a share sheet or WhatsApp send actually starts. This is the
     * only place that records the card. A later Home / recents / close must
     * not write a new seat.
     */
    public static void noteShareStarted(
            @Nullable Context context, int flipIndex, long postId, int languageId) {
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
                    .putInt(Constants.FEED_POSITION_LANGUAGE_ID, Math.max(0, languageId))
                    .putBoolean(Constants.FEED_POSITION_FROM_SHARE, true)
                    .putBoolean(Constants.FEED_POSITION_SHARE_DEPARTURE, true)
                    .commit();
        } catch (Exception ignored) {
        }
    }

    /**
     * The process left the foreground while a share was in progress. Starts
     * the admin window from now and arms a one-shot return for this process.
     * Does not move the saved card — the share-start seat is the one to keep.
     */
    public static void confirmShareBackground(@Nullable Context context) {
        awaitingShareReturn = true;
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
                    .putBoolean(Constants.FEED_POSITION_FROM_SHARE, true)
                    .putBoolean(Constants.FEED_POSITION_SHARE_DEPARTURE, false)
                    .commit();
        } catch (Exception ignored) {
        }
    }

    /** Call when the share sheet is launched, before the process backgrounds. */
    public static void noteShareDeparture(@Nullable Context context) {
        if (context == null) {
            return;
        }
        try {
            context.getApplicationContext()
                    .getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean(Constants.FEED_POSITION_SHARE_DEPARTURE, true)
                    .commit();
        } catch (Exception ignored) {
        }
    }

    /**
     * True once, when a share sheet was opened and the process then left the
     * foreground. A dismissed sheet never reaches this.
     */
    public static boolean consumeShareDeparture(@Nullable Context context) {
        if (context == null || !isShareDeparturePending(context)) {
            return false;
        }
        clearShareDeparture(context);
        return true;
    }

    /** Sheet dismissed without leaving the app. Does not clear a saved seat. */
    public static void clearShareDeparture(@Nullable Context context) {
        if (context == null) {
            return;
        }
        try {
            context.getApplicationContext()
                    .getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean(Constants.FEED_POSITION_SHARE_DEPARTURE, false)
                    .commit();
        } catch (Exception ignored) {
        }
    }

    /** True when the user left to share recently enough that the same card should stay. */
    public static boolean shouldKeepPosition(@Nullable Context context) {
        if (context == null) {
            return false;
        }
        if (!savedFromShare(context)) {
            return false;
        }
        long leftAt = readLeftAt(context);
        if (leftAt <= 0L) {
            return false;
        }
        long windowMs = getMinutes(context) * 60_000L;
        long elapsed = System.currentTimeMillis() - leftAt;
        if (elapsed < 0L || elapsed >= windowMs) {
            return false;
        }
        // Share-return keeps the card only for the language that was on screen.
        // A leftover seat from language A must not open language B.
        int savedLang = savedLanguageId(context);
        if (savedLang > 0) {
            int current = 0;
            try {
                current = SharedPrefsData.getInt(
                        context.getApplicationContext(),
                        Constants.LanguageID,
                        Constants.PREF_NAME);
            } catch (Exception ignored) {
            }
            if (current > 0 && current != savedLang) {
                return false;
            }
        }
        return true;
    }

    public static int savedLanguageId(@Nullable Context context) {
        if (context == null) {
            return 0;
        }
        try {
            return Math.max(
                    0,
                    SharedPrefsData.getInt(
                            context.getApplicationContext(),
                            Constants.FEED_POSITION_LANGUAGE_ID,
                            Constants.PREF_NAME));
        } catch (Exception ignored) {
            return 0;
        }
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

    /** Explicit reload, push open, close, or any open that is not a share return. */
    public static void clear(@Nullable Context context) {
        awaitingShareReturn = false;
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
                    .putInt(Constants.FEED_POSITION_LANGUAGE_ID, 0)
                    .putBoolean(Constants.FEED_POSITION_FROM_SHARE, false)
                    .putBoolean(Constants.FEED_POSITION_SHARE_DEPARTURE, false)
                    .commit();
        } catch (Exception ignored) {
        }
    }

    private static boolean savedFromShare(@Nullable Context context) {
        if (context == null) {
            return false;
        }
        try {
            return context.getApplicationContext()
                    .getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
                    .getBoolean(Constants.FEED_POSITION_FROM_SHARE, false);
        } catch (Exception ignored) {
            return false;
        }
    }

    private static boolean isShareDeparturePending(@Nullable Context context) {
        if (context == null) {
            return false;
        }
        try {
            return context.getApplicationContext()
                    .getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
                    .getBoolean(Constants.FEED_POSITION_SHARE_DEPARTURE, false);
        } catch (Exception ignored) {
            return false;
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
