package com.anyNews.anynews.Utils;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.Nullable;

/**
 * Admin-tunable in-feed native ad cadence / load knobs + impression flush.
 * Values arrive via splash / session ({@code feed_ad_every_n},
 * {@code ad_prefetch_lookahead}, {@code max_native_ad_loads_in_flight},
 * {@code ad_impr_flush_every_n}).
 *
 * <ul>
 *   <li>Ad every N articles — default {@link #DEFAULT_EVERY_N} (4), range 2–10</li>
 *   <li>Prefetch lookahead — default {@link #DEFAULT_PREFETCH_LOOKAHEAD} (3), range 2–4</li>
 *   <li>Max concurrent loads — default {@link #DEFAULT_MAX_IN_FLIGHT} (2), range 1–2</li>
 *   <li>Impression flush every N — default {@link #DEFAULT_FLUSH_EVERY_N} (3), range 1–50</li>
 * </ul>
 *
 * Process-level caches let list rebalance run without a Context after splash.
 * Caps are intentionally tight so AdMob pool work does not jank flip FPS.
 */
public final class FeedAdConfig {

    public static final int DEFAULT_EVERY_N = 4;
    public static final int MIN_EVERY_N = 2;
    public static final int MAX_EVERY_N = 10;

    /** Keep near the next seat only — deep lookahead storms the main thread. */
    public static final int DEFAULT_PREFETCH_LOOKAHEAD = 3;
    public static final int MIN_PREFETCH_LOOKAHEAD = 2;
    /** Hard cap: old prefs of 6+ clamp down so flip stays light. */
    public static final int MAX_PREFETCH_LOOKAHEAD = 4;

    /** Two concurrent natives is enough for every-4 cadence without flip hitch. */
    public static final int DEFAULT_MAX_IN_FLIGHT = 2;
    public static final int MIN_MAX_IN_FLIGHT = 1;
    /** Cap concurrent natives during rapid flips even if admin set higher. */
    public static final int MAX_MAX_IN_FLIGHT = 2;

    /** Batched {@code report_ad_impressions} after every N painted real ads. */
    public static final int DEFAULT_FLUSH_EVERY_N = 3;
    public static final int MIN_FLUSH_EVERY_N = 1;
    public static final int MAX_FLUSH_EVERY_N = 50;

    private static volatile int cachedEveryN = DEFAULT_EVERY_N;
    private static volatile int cachedPrefetchLookahead = DEFAULT_PREFETCH_LOOKAHEAD;
    private static volatile int cachedMaxInFlight = DEFAULT_MAX_IN_FLIGHT;
    private static volatile int cachedFlushEveryN = DEFAULT_FLUSH_EVERY_N;

    private FeedAdConfig() {}

    /** Load prefs into process cache (call early on splash / Home). */
    public static void warmFromPrefs(@Nullable Context context) {
        if (context == null) return;
        getEveryN(context);
        getPrefetchLookahead(context);
        getMaxInFlight(context);
        getFlushEveryN(context);
    }

    /** Persist when present (null fields = old server; leave cache). */
    public static void saveFromServer(
            @Nullable Context context,
            @Nullable Integer everyN,
            @Nullable Integer prefetchLookahead,
            @Nullable Integer maxInFlight) {
        saveFromServer(context, everyN, prefetchLookahead, maxInFlight, null);
    }

    public static void saveFromServer(
            @Nullable Context context,
            @Nullable Integer everyN,
            @Nullable Integer prefetchLookahead,
            @Nullable Integer maxInFlight,
            @Nullable Integer flushEveryN) {
        if (everyN != null) {
            saveEveryNFromServer(context, everyN);
        }
        if (prefetchLookahead != null) {
            savePrefetchLookaheadFromServer(context, prefetchLookahead);
        }
        if (maxInFlight != null) {
            saveMaxInFlightFromServer(context, maxInFlight);
        }
        if (flushEveryN != null) {
            saveFlushEveryNFromServer(context, flushEveryN);
        }
    }

    public static void saveEveryNFromServer(
            @Nullable Context context, @Nullable Integer everyN) {
        if (everyN == null) return;
        int safe = clampEveryN(everyN);
        cachedEveryN = safe;
        if (context == null) return;
        SharedPrefsData.putString(
                context.getApplicationContext(),
                Constants.FEED_AD_EVERY_N,
                String.valueOf(safe),
                Constants.PREF_NAME);
    }

    public static void savePrefetchLookaheadFromServer(
            @Nullable Context context, @Nullable Integer lookahead) {
        if (lookahead == null) return;
        int safe = clampPrefetch(lookahead);
        cachedPrefetchLookahead = safe;
        if (context == null) return;
        SharedPrefsData.putString(
                context.getApplicationContext(),
                Constants.AD_PREFETCH_LOOKAHEAD,
                String.valueOf(safe),
                Constants.PREF_NAME);
    }

    public static void saveMaxInFlightFromServer(
            @Nullable Context context, @Nullable Integer maxInFlight) {
        if (maxInFlight == null) return;
        int safe = clampMaxInFlight(maxInFlight);
        cachedMaxInFlight = safe;
        if (context == null) return;
        SharedPrefsData.putString(
                context.getApplicationContext(),
                Constants.MAX_NATIVE_AD_LOADS_IN_FLIGHT,
                String.valueOf(safe),
                Constants.PREF_NAME);
    }

    public static void saveFlushEveryNFromServer(
            @Nullable Context context, @Nullable Integer flushEveryN) {
        if (flushEveryN == null) return;
        int safe = clampFlushEveryN(flushEveryN);
        cachedFlushEveryN = safe;
        if (context == null) return;
        SharedPrefsData.putString(
                context.getApplicationContext(),
                Constants.AD_IMPR_FLUSH_EVERY_N,
                String.valueOf(safe),
                Constants.PREF_NAME);
    }

    /** Ad after every N real cards. Default 4 when unset. */
    public static int getEveryN(@Nullable Context context) {
        if (context != null) {
            try {
                String v =
                        SharedPrefsData.getString(
                                context.getApplicationContext(),
                                Constants.FEED_AD_EVERY_N,
                                Constants.PREF_NAME);
                if (!TextUtils.isEmpty(v)) {
                    int n = clampEveryN(Integer.parseInt(v.trim()));
                    cachedEveryN = n;
                    return n;
                }
            } catch (Exception ignored) {
            }
        }
        return clampEveryN(cachedEveryN);
    }

    /** No-context path for list rebalance after splash has warmed the cache. */
    public static int getEveryN() {
        return clampEveryN(cachedEveryN);
    }

    public static int getPrefetchLookahead(@Nullable Context context) {
        if (context != null) {
            try {
                String v =
                        SharedPrefsData.getString(
                                context.getApplicationContext(),
                                Constants.AD_PREFETCH_LOOKAHEAD,
                                Constants.PREF_NAME);
                if (!TextUtils.isEmpty(v)) {
                    int n = clampPrefetch(Integer.parseInt(v.trim()));
                    cachedPrefetchLookahead = n;
                    return n;
                }
            } catch (Exception ignored) {
            }
        }
        return clampPrefetch(cachedPrefetchLookahead);
    }

    public static int getPrefetchLookahead() {
        return clampPrefetch(cachedPrefetchLookahead);
    }

    public static int getMaxInFlight(@Nullable Context context) {
        if (context != null) {
            try {
                String v =
                        SharedPrefsData.getString(
                                context.getApplicationContext(),
                                Constants.MAX_NATIVE_AD_LOADS_IN_FLIGHT,
                                Constants.PREF_NAME);
                if (!TextUtils.isEmpty(v)) {
                    int n = clampMaxInFlight(Integer.parseInt(v.trim()));
                    cachedMaxInFlight = n;
                    return n;
                }
            } catch (Exception ignored) {
            }
        }
        return clampMaxInFlight(cachedMaxInFlight);
    }

    public static int getMaxInFlight() {
        return clampMaxInFlight(cachedMaxInFlight);
    }

    /** Flush pending real-ad impression batch after every N painted ads. */
    public static int getFlushEveryN(@Nullable Context context) {
        if (context != null) {
            try {
                String v =
                        SharedPrefsData.getString(
                                context.getApplicationContext(),
                                Constants.AD_IMPR_FLUSH_EVERY_N,
                                Constants.PREF_NAME);
                if (!TextUtils.isEmpty(v)) {
                    int n = clampFlushEveryN(Integer.parseInt(v.trim()));
                    cachedFlushEveryN = n;
                    return n;
                }
            } catch (Exception ignored) {
            }
        }
        return clampFlushEveryN(cachedFlushEveryN);
    }

    public static int getFlushEveryN() {
        return clampFlushEveryN(cachedFlushEveryN);
    }

    private static int clampEveryN(int n) {
        if (n < MIN_EVERY_N) {
            if (n <= 0) return DEFAULT_EVERY_N;
            return MIN_EVERY_N;
        }
        if (n > MAX_EVERY_N) return MAX_EVERY_N;
        return n;
    }

    private static int clampPrefetch(int n) {
        if (n < MIN_PREFETCH_LOOKAHEAD) {
            if (n <= 0) return DEFAULT_PREFETCH_LOOKAHEAD;
            return MIN_PREFETCH_LOOKAHEAD;
        }
        if (n > MAX_PREFETCH_LOOKAHEAD) return MAX_PREFETCH_LOOKAHEAD;
        return n;
    }

    private static int clampMaxInFlight(int n) {
        if (n < MIN_MAX_IN_FLIGHT) {
            if (n <= 0) return DEFAULT_MAX_IN_FLIGHT;
            return MIN_MAX_IN_FLIGHT;
        }
        if (n > MAX_MAX_IN_FLIGHT) return MAX_MAX_IN_FLIGHT;
        return n;
    }

    private static int clampFlushEveryN(int n) {
        if (n < MIN_FLUSH_EVERY_N) {
            if (n <= 0) return DEFAULT_FLUSH_EVERY_N;
            return MIN_FLUSH_EVERY_N;
        }
        if (n > MAX_FLUSH_EVERY_N) return MAX_FLUSH_EVERY_N;
        return n;
    }
}
