package com.anyNews.anynews.Utils;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Admin-tunable in-feed ad cadence / format / unit + load knobs.
 * Values arrive via splash / session ({@code feed_ad_every_n},
 * {@code feed_ad_format}, {@code ad_code},
 * {@code ad_prefetch_lookahead}, {@code max_native_ad_loads_in_flight},
 * {@code ad_impr_flush_every_n}).
 *
 * <p>After a multi-format APK, admin alone switches native ↔ interstitial + unit;
 * no rebuild for format/unit changes. Cadence ({@link #getEveryN}) remains the
 * frequency cap for both formats.
 */
public final class FeedAdConfig {

    public static final String FORMAT_NATIVE = "native";
    public static final String FORMAT_INTERSTITIAL = "interstitial";

    /** Production feed native unit (code / strings fallback). */
    public static final String DEFAULT_UNIT_ID =
            "ca-app-pub-8562336752221105/4901277505";

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
    private static volatile String cachedFormat = FORMAT_NATIVE;
    private static volatile String cachedUnitId = DEFAULT_UNIT_ID;

    private FeedAdConfig() {}

    /** Load prefs into process cache (call early on splash / Home). */
    public static void warmFromPrefs(@Nullable Context context) {
        if (context == null) return;
        getEveryN(context);
        getPrefetchLookahead(context);
        getMaxInFlight(context);
        getFlushEveryN(context);
        getFormat(context);
        getUnitId(context);
    }

    /** Persist when present (null fields = old server; leave cache). */
    public static void saveFromServer(
            @Nullable Context context,
            @Nullable Integer everyN,
            @Nullable Integer prefetchLookahead,
            @Nullable Integer maxInFlight) {
        saveFromServer(context, everyN, prefetchLookahead, maxInFlight, null, null, null);
    }

    public static void saveFromServer(
            @Nullable Context context,
            @Nullable Integer everyN,
            @Nullable Integer prefetchLookahead,
            @Nullable Integer maxInFlight,
            @Nullable Integer flushEveryN) {
        saveFromServer(
                context, everyN, prefetchLookahead, maxInFlight, flushEveryN, null, null);
    }

    public static void saveFromServer(
            @Nullable Context context,
            @Nullable Integer everyN,
            @Nullable Integer prefetchLookahead,
            @Nullable Integer maxInFlight,
            @Nullable Integer flushEveryN,
            @Nullable String feedAdFormat,
            @Nullable String feedAdUnitId) {
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
        if (feedAdFormat != null) {
            saveFormatFromServer(context, feedAdFormat);
        }
        if (feedAdUnitId != null) {
            saveUnitIdFromServer(context, feedAdUnitId);
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

    public static void saveFormatFromServer(
            @Nullable Context context, @Nullable String format) {
        if (format == null) return;
        String safe = normalizeFormat(format);
        cachedFormat = safe;
        if (context == null) return;
        SharedPrefsData.putString(
                context.getApplicationContext(),
                Constants.FEED_AD_FORMAT,
                safe,
                Constants.PREF_NAME);
    }

    public static void saveUnitIdFromServer(
            @Nullable Context context, @Nullable String unitId) {
        if (unitId == null) return;
        String trimmed = unitId.trim();
        if (trimmed.isEmpty() || trimmed.contains("3940256099942544")) {
            trimmed = DEFAULT_UNIT_ID;
        }
        cachedUnitId = trimmed;
        if (context == null) return;
        SharedPrefsData.putString(
                context.getApplicationContext(),
                Constants.FEED_AD_UNIT_ID,
                trimmed,
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

    @NonNull
    public static String getFormat(@Nullable Context context) {
        if (context != null) {
            try {
                String v =
                        SharedPrefsData.getString(
                                context.getApplicationContext(),
                                Constants.FEED_AD_FORMAT,
                                Constants.PREF_NAME);
                if (!TextUtils.isEmpty(v)) {
                    String n = normalizeFormat(v);
                    cachedFormat = n;
                    return n;
                }
            } catch (Exception ignored) {
            }
        }
        return normalizeFormat(cachedFormat);
    }

    @NonNull
    public static String getFormat() {
        return normalizeFormat(cachedFormat);
    }

    public static boolean isInterstitial(@Nullable Context context) {
        return FORMAT_INTERSTITIAL.equals(getFormat(context));
    }

    public static boolean isInterstitial() {
        return FORMAT_INTERSTITIAL.equals(getFormat());
    }

    @NonNull
    public static String getUnitId(@Nullable Context context) {
        if (context != null) {
            try {
                String v =
                        SharedPrefsData.getString(
                                context.getApplicationContext(),
                                Constants.FEED_AD_UNIT_ID,
                                Constants.PREF_NAME);
                if (!TextUtils.isEmpty(v)) {
                    String n = v.trim();
                    if (!n.contains("3940256099942544")) {
                        cachedUnitId = n;
                        return n;
                    }
                }
            } catch (Exception ignored) {
            }
        }
        String cached = cachedUnitId == null ? "" : cachedUnitId.trim();
        return TextUtils.isEmpty(cached) ? DEFAULT_UNIT_ID : cached;
    }

    @NonNull
    public static String getUnitId() {
        String cached = cachedUnitId == null ? "" : cachedUnitId.trim();
        return TextUtils.isEmpty(cached) ? DEFAULT_UNIT_ID : cached;
    }

    @NonNull
    private static String normalizeFormat(@Nullable String raw) {
        if (raw == null) return FORMAT_NATIVE;
        String v = raw.trim().toLowerCase();
        return FORMAT_INTERSTITIAL.equals(v) ? FORMAT_INTERSTITIAL : FORMAT_NATIVE;
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
