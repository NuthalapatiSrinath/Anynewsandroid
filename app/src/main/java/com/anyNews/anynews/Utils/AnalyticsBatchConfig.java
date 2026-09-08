package com.anyNews.anynews.Utils;

import android.content.Context;

import androidx.annotation.Nullable;

/**
 * Admin-tunable analytics / engagement flush knobs delivered via splash + session
 * ({@code views_batch_flush_interval_seconds}, {@code views_batch_flush_at_count},
 * {@code media_access_flush_interval_seconds},
 * {@code rewards_engagement_flush_interval_seconds}).
 *
 * <p>Timers apply while Home is resumed; leave-Home / background still flush.
 * Rewards engagement interval is ignored when rewards master is OFF or the user
 * has not joined — those paths never call {@code rewards_events_batch}.
 */
public final class AnalyticsBatchConfig {

    public static final int DEFAULT_VIEWS_INTERVAL_SEC = 60;
    public static final int MIN_VIEWS_INTERVAL_SEC = 15;
    public static final int MAX_VIEWS_INTERVAL_SEC = 300;

    public static final int DEFAULT_VIEWS_FLUSH_AT = 12;
    public static final int MIN_VIEWS_FLUSH_AT = 3;
    public static final int MAX_VIEWS_FLUSH_AT = 100;

    public static final int DEFAULT_MEDIA_INTERVAL_SEC = 60;
    public static final int MIN_MEDIA_INTERVAL_SEC = 15;
    public static final int MAX_MEDIA_INTERVAL_SEC = 300;

    public static final int DEFAULT_REWARDS_INTERVAL_SEC = 45;
    public static final int MIN_REWARDS_INTERVAL_SEC = 15;
    public static final int MAX_REWARDS_INTERVAL_SEC = 120;

    private static volatile int cachedViewsIntervalSec = DEFAULT_VIEWS_INTERVAL_SEC;
    private static volatile int cachedViewsFlushAt = DEFAULT_VIEWS_FLUSH_AT;
    private static volatile int cachedMediaIntervalSec = DEFAULT_MEDIA_INTERVAL_SEC;
    private static volatile int cachedRewardsIntervalSec = DEFAULT_REWARDS_INTERVAL_SEC;

    private AnalyticsBatchConfig() {}

    public static void warmFromPrefs(@Nullable Context context) {
        if (context == null) return;
        getViewsFlushIntervalMs(context);
        getViewsFlushAtCount(context);
        getMediaFlushIntervalMs(context);
        getRewardsFlushIntervalMs(context);
    }

    public static void saveFromServer(
            @Nullable Context context,
            @Nullable Integer viewsIntervalSec,
            @Nullable Integer viewsFlushAt,
            @Nullable Integer mediaIntervalSec,
            @Nullable Integer rewardsIntervalSec) {
        if (viewsIntervalSec != null) {
            saveViewsIntervalFromServer(context, viewsIntervalSec);
        }
        if (viewsFlushAt != null) {
            saveViewsFlushAtFromServer(context, viewsFlushAt);
        }
        if (mediaIntervalSec != null) {
            saveMediaIntervalFromServer(context, mediaIntervalSec);
        }
        if (rewardsIntervalSec != null) {
            saveRewardsIntervalFromServer(context, rewardsIntervalSec);
        }
    }

    public static long getViewsFlushIntervalMs(@Nullable Context context) {
        return getViewsFlushIntervalSec(context) * 1000L;
    }

    public static int getViewsFlushIntervalSec(@Nullable Context context) {
        int n = cachedViewsIntervalSec;
        if (context != null) {
            try {
                int stored =
                        SharedPrefsData.getInt(
                                context.getApplicationContext(),
                                Constants.VIEWS_BATCH_FLUSH_INTERVAL_SECONDS,
                                Constants.PREF_NAME);
                if (stored >= MIN_VIEWS_INTERVAL_SEC && stored <= MAX_VIEWS_INTERVAL_SEC) {
                    n = stored;
                    cachedViewsIntervalSec = n;
                }
            } catch (Exception ignored) {
            }
        }
        return clamp(n, MIN_VIEWS_INTERVAL_SEC, MAX_VIEWS_INTERVAL_SEC, DEFAULT_VIEWS_INTERVAL_SEC);
    }

    public static int getViewsFlushAtCount(@Nullable Context context) {
        int n = cachedViewsFlushAt;
        if (context != null) {
            try {
                int stored =
                        SharedPrefsData.getInt(
                                context.getApplicationContext(),
                                Constants.VIEWS_BATCH_FLUSH_AT_COUNT,
                                Constants.PREF_NAME);
                if (stored >= MIN_VIEWS_FLUSH_AT && stored <= MAX_VIEWS_FLUSH_AT) {
                    n = stored;
                    cachedViewsFlushAt = n;
                }
            } catch (Exception ignored) {
            }
        }
        return clamp(n, MIN_VIEWS_FLUSH_AT, MAX_VIEWS_FLUSH_AT, DEFAULT_VIEWS_FLUSH_AT);
    }

    public static long getMediaFlushIntervalMs(@Nullable Context context) {
        return getMediaFlushIntervalSec(context) * 1000L;
    }

    public static int getMediaFlushIntervalSec(@Nullable Context context) {
        int n = cachedMediaIntervalSec;
        if (context != null) {
            try {
                int stored =
                        SharedPrefsData.getInt(
                                context.getApplicationContext(),
                                Constants.MEDIA_ACCESS_FLUSH_INTERVAL_SECONDS,
                                Constants.PREF_NAME);
                if (stored >= MIN_MEDIA_INTERVAL_SEC && stored <= MAX_MEDIA_INTERVAL_SEC) {
                    n = stored;
                    cachedMediaIntervalSec = n;
                }
            } catch (Exception ignored) {
            }
        }
        return clamp(n, MIN_MEDIA_INTERVAL_SEC, MAX_MEDIA_INTERVAL_SEC, DEFAULT_MEDIA_INTERVAL_SEC);
    }

    public static long getRewardsFlushIntervalMs(@Nullable Context context) {
        return getRewardsFlushIntervalSec(context) * 1000L;
    }

    public static int getRewardsFlushIntervalSec(@Nullable Context context) {
        int n = cachedRewardsIntervalSec;
        if (context != null) {
            try {
                int stored =
                        SharedPrefsData.getInt(
                                context.getApplicationContext(),
                                Constants.REWARDS_ENGAGEMENT_FLUSH_INTERVAL_SECONDS,
                                Constants.PREF_NAME);
                if (stored >= MIN_REWARDS_INTERVAL_SEC && stored <= MAX_REWARDS_INTERVAL_SEC) {
                    n = stored;
                    cachedRewardsIntervalSec = n;
                }
            } catch (Exception ignored) {
            }
        }
        return clamp(
                n,
                MIN_REWARDS_INTERVAL_SEC,
                MAX_REWARDS_INTERVAL_SEC,
                DEFAULT_REWARDS_INTERVAL_SEC);
    }

    private static void saveViewsIntervalFromServer(@Nullable Context context, int value) {
        int n =
                clamp(
                        value,
                        MIN_VIEWS_INTERVAL_SEC,
                        MAX_VIEWS_INTERVAL_SEC,
                        DEFAULT_VIEWS_INTERVAL_SEC);
        cachedViewsIntervalSec = n;
        if (context == null) return;
        try {
            SharedPrefsData.putInt(
                    context.getApplicationContext(),
                    Constants.VIEWS_BATCH_FLUSH_INTERVAL_SECONDS,
                    n,
                    Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
    }

    private static void saveViewsFlushAtFromServer(@Nullable Context context, int value) {
        int n = clamp(value, MIN_VIEWS_FLUSH_AT, MAX_VIEWS_FLUSH_AT, DEFAULT_VIEWS_FLUSH_AT);
        cachedViewsFlushAt = n;
        if (context == null) return;
        try {
            SharedPrefsData.putInt(
                    context.getApplicationContext(),
                    Constants.VIEWS_BATCH_FLUSH_AT_COUNT,
                    n,
                    Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
    }

    private static void saveMediaIntervalFromServer(@Nullable Context context, int value) {
        int n =
                clamp(
                        value,
                        MIN_MEDIA_INTERVAL_SEC,
                        MAX_MEDIA_INTERVAL_SEC,
                        DEFAULT_MEDIA_INTERVAL_SEC);
        cachedMediaIntervalSec = n;
        if (context == null) return;
        try {
            SharedPrefsData.putInt(
                    context.getApplicationContext(),
                    Constants.MEDIA_ACCESS_FLUSH_INTERVAL_SECONDS,
                    n,
                    Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
    }

    private static void saveRewardsIntervalFromServer(@Nullable Context context, int value) {
        int n =
                clamp(
                        value,
                        MIN_REWARDS_INTERVAL_SEC,
                        MAX_REWARDS_INTERVAL_SEC,
                        DEFAULT_REWARDS_INTERVAL_SEC);
        cachedRewardsIntervalSec = n;
        if (context == null) return;
        try {
            SharedPrefsData.putInt(
                    context.getApplicationContext(),
                    Constants.REWARDS_ENGAGEMENT_FLUSH_INTERVAL_SECONDS,
                    n,
                    Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
    }

    private static int clamp(int value, int min, int max, int fallback) {
        if (value < min || value > max) return fallback;
        return value;
    }
}
