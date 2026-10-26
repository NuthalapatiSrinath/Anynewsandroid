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
 * {@code ad_impr_flush_every_n}, {@code destroy_native_ads_after_impression},
 * {@code feed_native_ad_unit_mode}, {@code use_second_native_ad_unit},
 * {@code ad_code_secondary}).
 *
 * <p>After a multi-format APK, admin alone switches native ↔ interstitial + unit;
 * no rebuild for format/unit changes. Cadence ({@link #getEveryN}) remains the
 * frequency cap for both formats.
 *
 * <p>Native unit mode: {@code primary_only} | {@code primary_then_secondary}
 * (default) | {@code secondary_only}. Legacy bool maps when mode is absent.
 */
public final class FeedAdConfig {

    public static final String FORMAT_NATIVE = "native";
    public static final String FORMAT_INTERSTITIAL = "interstitial";

    /** Feed native unit selection (admin {@code feed_native_ad_unit_mode}). */
    public static final String MODE_PRIMARY_ONLY = "primary_only";
    public static final String MODE_PRIMARY_THEN_SECONDARY = "primary_then_secondary";
    public static final String MODE_SECONDARY_ONLY = "secondary_only";

    /** Production feed native unit (code / strings fallback). */
    public static final String DEFAULT_UNIT_ID =
            "ca-app-pub-8562336752221105/4901277505";

    /** Second native unit for no-fill waterfall (admin {@code ad_code_secondary}). */
    public static final String DEFAULT_SECOND_UNIT_ID =
            "ca-app-pub-8562336752221105/8909042175";

    public static final int DEFAULT_EVERY_N = 4;
    public static final int MIN_EVERY_N = 2;
    public static final int MAX_EVERY_N = 10;

    /**
     * Match admin Performance range (3–12). Default {@link #DEFAULT_EVERY_N}
     * ≈ next one ad seat only. Deeper windows stockpile matched ads that never
     * impress and depress match rate. App hard-caps seat AdLoader starts to 1.
     */
    public static final int DEFAULT_PREFETCH_LOOKAHEAD = DEFAULT_EVERY_N;
    public static final int MIN_PREFETCH_LOOKAHEAD = 3;
    public static final int MAX_PREFETCH_LOOKAHEAD = 12;

    /**
     * Match admin Performance range (1–5). Default 1 = one seat AdLoader at a
     * time (primary; secondary only after that seat's primary no-fill). Higher
     * values raced AdPreloader and over-requested without impressions.
     */
    public static final int DEFAULT_MAX_IN_FLIGHT = 1;
    public static final int MIN_MAX_IN_FLIGHT = 1;
    public static final int MAX_MAX_IN_FLIGHT = 5;

    /** Batched {@code report_ad_impressions} after every N painted real ads. */
    public static final int DEFAULT_FLUSH_EVERY_N = 3;
    public static final int MIN_FLUSH_EVERY_N = 1;
    public static final int MAX_FLUSH_EVERY_N = 50;

    /**
     * Default OFF (Play-smooth): keep claimed/impressed natives on flip-away so
     * return can seat-sticky the same creative without destroy/recreate jank.
     * ON: destroy after settle when the user leaves the seat (fresh fill on return).
     * Cross-seat unpaid reuse stays blocked via reqPos + claim-once regardless.
     */
    public static final boolean DEFAULT_DESTROY_AFTER_IMPRESSION = false;

    /**
     * Default: primary then secondary on no-fill.
     * @deprecated Prefer {@link #DEFAULT_NATIVE_AD_UNIT_MODE}.
     */
    public static final boolean DEFAULT_USE_SECOND_NATIVE = true;

    /** Default feed native unit mode (matches prior waterfall ON). */
    public static final String DEFAULT_NATIVE_AD_UNIT_MODE = MODE_PRIMARY_THEN_SECONDARY;

    private static volatile int cachedEveryN = DEFAULT_EVERY_N;
    private static volatile int cachedPrefetchLookahead = DEFAULT_PREFETCH_LOOKAHEAD;
    private static volatile int cachedMaxInFlight = DEFAULT_MAX_IN_FLIGHT;
    private static volatile int cachedFlushEveryN = DEFAULT_FLUSH_EVERY_N;
    private static volatile boolean cachedDestroyAfterImpression =
            DEFAULT_DESTROY_AFTER_IMPRESSION;
    private static volatile String cachedNativeAdUnitMode = DEFAULT_NATIVE_AD_UNIT_MODE;
    private static volatile String cachedFormat = FORMAT_NATIVE;
    private static volatile String cachedUnitId = DEFAULT_UNIT_ID;
    private static volatile String cachedSecondUnitId = DEFAULT_SECOND_UNIT_ID;

    private FeedAdConfig() {}

    /** Load prefs into process cache (call early on splash / Home). */
    public static void warmFromPrefs(@Nullable Context context) {
        if (context == null) return;
        getEveryN(context);
        getPrefetchLookahead(context);
        getMaxInFlight(context);
        getFlushEveryN(context);
        isDestroyAfterImpression(context);
        getNativeAdUnitMode(context);
        getFormat(context);
        getUnitId(context);
        getSecondUnitId(context);
    }

    /** Persist when present (null fields = old server; leave cache). */
    public static void saveFromServer(
            @Nullable Context context,
            @Nullable Integer everyN,
            @Nullable Integer prefetchLookahead,
            @Nullable Integer maxInFlight) {
        saveFromServer(context, everyN, prefetchLookahead, maxInFlight, null, null, null, null);
    }

    public static void saveFromServer(
            @Nullable Context context,
            @Nullable Integer everyN,
            @Nullable Integer prefetchLookahead,
            @Nullable Integer maxInFlight,
            @Nullable Integer flushEveryN) {
        saveFromServer(
                context, everyN, prefetchLookahead, maxInFlight, flushEveryN, null, null, null);
    }

    public static void saveFromServer(
            @Nullable Context context,
            @Nullable Integer everyN,
            @Nullable Integer prefetchLookahead,
            @Nullable Integer maxInFlight,
            @Nullable Integer flushEveryN,
            @Nullable String feedAdFormat,
            @Nullable String feedAdUnitId) {
        saveFromServer(
                context,
                everyN,
                prefetchLookahead,
                maxInFlight,
                flushEveryN,
                feedAdFormat,
                feedAdUnitId,
                null);
    }

    public static void saveFromServer(
            @Nullable Context context,
            @Nullable Integer everyN,
            @Nullable Integer prefetchLookahead,
            @Nullable Integer maxInFlight,
            @Nullable Integer flushEveryN,
            @Nullable String feedAdFormat,
            @Nullable String feedAdUnitId,
            @Nullable Boolean destroyAfterImpression) {
        saveFromServer(
                context,
                everyN,
                prefetchLookahead,
                maxInFlight,
                flushEveryN,
                feedAdFormat,
                feedAdUnitId,
                destroyAfterImpression,
                null,
                null);
    }

    public static void saveFromServer(
            @Nullable Context context,
            @Nullable Integer everyN,
            @Nullable Integer prefetchLookahead,
            @Nullable Integer maxInFlight,
            @Nullable Integer flushEveryN,
            @Nullable String feedAdFormat,
            @Nullable String feedAdUnitId,
            @Nullable Boolean destroyAfterImpression,
            @Nullable Boolean useSecondNative,
            @Nullable String secondUnitId) {
        saveFromServer(
                context,
                everyN,
                prefetchLookahead,
                maxInFlight,
                flushEveryN,
                feedAdFormat,
                feedAdUnitId,
                destroyAfterImpression,
                useSecondNative,
                secondUnitId,
                null);
    }

    public static void saveFromServer(
            @Nullable Context context,
            @Nullable Integer everyN,
            @Nullable Integer prefetchLookahead,
            @Nullable Integer maxInFlight,
            @Nullable Integer flushEveryN,
            @Nullable String feedAdFormat,
            @Nullable String feedAdUnitId,
            @Nullable Boolean destroyAfterImpression,
            @Nullable Boolean useSecondNative,
            @Nullable String secondUnitId,
            @Nullable String nativeAdUnitMode) {
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
        if (destroyAfterImpression != null) {
            saveDestroyAfterImpressionFromServer(context, destroyAfterImpression);
        }
        if (nativeAdUnitMode != null && !TextUtils.isEmpty(nativeAdUnitMode.trim())) {
            saveNativeAdUnitModeFromServer(context, nativeAdUnitMode);
        } else if (useSecondNative != null) {
            saveUseSecondNativeFromServer(context, useSecondNative);
        }
        if (secondUnitId != null) {
            saveSecondUnitIdFromServer(context, secondUnitId);
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

    public static void saveDestroyAfterImpressionFromServer(
            @Nullable Context context, @Nullable Boolean destroyAfter) {
        if (destroyAfter == null) return;
        boolean safe = Boolean.TRUE.equals(destroyAfter);
        cachedDestroyAfterImpression = safe;
        if (context == null) return;
        SharedPrefsData.putBool(
                context.getApplicationContext(),
                Constants.DESTROY_NATIVE_ADS_AFTER_IMPRESSION,
                safe,
                Constants.PREF_NAME);
    }

    public static void saveUseSecondNativeFromServer(
            @Nullable Context context, @Nullable Boolean useSecond) {
        if (useSecond == null) return;
        // Legacy bool → mode (cannot express secondary_only).
        saveNativeAdUnitModeFromServer(
                context,
                Boolean.TRUE.equals(useSecond)
                        ? MODE_PRIMARY_THEN_SECONDARY
                        : MODE_PRIMARY_ONLY);
    }

    public static void saveNativeAdUnitModeFromServer(
            @Nullable Context context, @Nullable String mode) {
        if (mode == null) return;
        String safe = normalizeNativeAdUnitMode(mode);
        cachedNativeAdUnitMode = safe;
        if (context == null) return;
        Context app = context.getApplicationContext();
        SharedPrefsData.putString(
                app, Constants.FEED_NATIVE_AD_UNIT_MODE, safe, Constants.PREF_NAME);
        SharedPrefsData.putBool(
                app,
                Constants.USE_SECOND_NATIVE_AD_UNIT,
                MODE_PRIMARY_THEN_SECONDARY.equals(safe),
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

    public static void saveSecondUnitIdFromServer(
            @Nullable Context context, @Nullable String unitId) {
        if (unitId == null) return;
        String trimmed = unitId.trim();
        if (trimmed.isEmpty() || trimmed.contains("3940256099942544")) {
            trimmed = DEFAULT_SECOND_UNIT_ID;
        }
        cachedSecondUnitId = trimmed;
        if (context == null) return;
        SharedPrefsData.putString(
                context.getApplicationContext(),
                Constants.FEED_AD_SECOND_UNIT_ID,
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

    /**
     * When true: destroy claimed/impressed natives after flip-away settle (fresh
     * fill on return). When false (default): seat-sticky keep on the same
     * {@code ad_req_position}; impressed creatives never transfer to another seat.
     * Mode-switch / activity wipe still prune used creatives (money-safe).
     */
    public static boolean isDestroyAfterImpression(@Nullable Context context) {
        if (context != null) {
            try {
                boolean v =
                        SharedPrefsData.getBool(
                                context.getApplicationContext(),
                                Constants.DESTROY_NATIVE_ADS_AFTER_IMPRESSION,
                                Constants.PREF_NAME,
                                DEFAULT_DESTROY_AFTER_IMPRESSION);
                cachedDestroyAfterImpression = v;
                return v;
            } catch (Exception ignored) {
            }
        }
        return cachedDestroyAfterImpression;
    }

    public static boolean isDestroyAfterImpression() {
        return cachedDestroyAfterImpression;
    }

    /**
     * When true: on primary native no-fill, try the secondary unit for the same
     * seat ({@link #MODE_PRIMARY_THEN_SECONDARY}). False for primary-only and
     * secondary-only (no waterfall).
     */
    public static boolean isUseSecondNativeAdUnit(@Nullable Context context) {
        return MODE_PRIMARY_THEN_SECONDARY.equals(getNativeAdUnitMode(context));
    }

    public static boolean isUseSecondNativeAdUnit() {
        return MODE_PRIMARY_THEN_SECONDARY.equals(normalizeNativeAdUnitMode(cachedNativeAdUnitMode));
    }

    /**
     * Admin feed native unit mode: primary_only | primary_then_secondary |
     * secondary_only.
     */
    @NonNull
    public static String getNativeAdUnitMode(@Nullable Context context) {
        if (context != null) {
            try {
                Context app = context.getApplicationContext();
                String modeRaw =
                        SharedPrefsData.getString(
                                app, Constants.FEED_NATIVE_AD_UNIT_MODE, Constants.PREF_NAME);
                if (!TextUtils.isEmpty(modeRaw)) {
                    String n = normalizeNativeAdUnitMode(modeRaw);
                    cachedNativeAdUnitMode = n;
                    return n;
                }
                // Legacy prefs: derive from boolean when mode key absent.
                boolean useSecond =
                        SharedPrefsData.getBool(
                                app,
                                Constants.USE_SECOND_NATIVE_AD_UNIT,
                                Constants.PREF_NAME,
                                DEFAULT_USE_SECOND_NATIVE);
                String derived =
                        useSecond ? MODE_PRIMARY_THEN_SECONDARY : MODE_PRIMARY_ONLY;
                cachedNativeAdUnitMode = derived;
                return derived;
            } catch (Exception ignored) {
            }
        }
        return normalizeNativeAdUnitMode(cachedNativeAdUnitMode);
    }

    @NonNull
    public static String getNativeAdUnitMode() {
        return normalizeNativeAdUnitMode(cachedNativeAdUnitMode);
    }

    public static boolean isSecondaryOnly(@Nullable Context context) {
        return MODE_SECONDARY_ONLY.equals(getNativeAdUnitMode(context));
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

    /** True when admin between-news format is interstitial (splash/session). */
    public static boolean isInterstitial(@Nullable Context context) {
        return FORMAT_INTERSTITIAL.equals(getFormat(context));
    }

    public static boolean isInterstitial() {
        return FORMAT_INTERSTITIAL.equals(getFormat());
    }

    /**
     * Effective unit for feed native loads. In {@link #MODE_SECONDARY_ONLY}
     * (native format only), returns the secondary unit; otherwise the primary
     * admin unit. Interstitial format always uses the primary {@code ad_code}.
     */
    @NonNull
    public static String getUnitId(@Nullable Context context) {
        if (MODE_SECONDARY_ONLY.equals(getNativeAdUnitMode(context))
                && !isInterstitial(context)) {
            return getSecondUnitId(context);
        }
        return getPrimaryUnitId(context);
    }

    @NonNull
    public static String getUnitId() {
        if (MODE_SECONDARY_ONLY.equals(getNativeAdUnitMode()) && !isInterstitial()) {
            return getSecondUnitId();
        }
        return getPrimaryUnitId();
    }

    /** Stored primary unit (never remapped by secondary-only mode). */
    @NonNull
    public static String getPrimaryUnitId(@Nullable Context context) {
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
    public static String getPrimaryUnitId() {
        String cached = cachedUnitId == null ? "" : cachedUnitId.trim();
        return TextUtils.isEmpty(cached) ? DEFAULT_UNIT_ID : cached;
    }

    @NonNull
    public static String getSecondUnitId(@Nullable Context context) {
        if (context != null) {
            try {
                String v =
                        SharedPrefsData.getString(
                                context.getApplicationContext(),
                                Constants.FEED_AD_SECOND_UNIT_ID,
                                Constants.PREF_NAME);
                if (!TextUtils.isEmpty(v)) {
                    String n = v.trim();
                    if (!n.contains("3940256099942544")) {
                        cachedSecondUnitId = n;
                        return n;
                    }
                }
            } catch (Exception ignored) {
            }
        }
        String cached = cachedSecondUnitId == null ? "" : cachedSecondUnitId.trim();
        return TextUtils.isEmpty(cached) ? DEFAULT_SECOND_UNIT_ID : cached;
    }

    @NonNull
    public static String getSecondUnitId() {
        String cached = cachedSecondUnitId == null ? "" : cachedSecondUnitId.trim();
        return TextUtils.isEmpty(cached) ? DEFAULT_SECOND_UNIT_ID : cached;
    }

    /**
     * When mode is {@link #MODE_PRIMARY_THEN_SECONDARY} and {@code primaryUnit}
     * is not already the secondary id, return the secondary unit for a no-fill
     * waterfall; otherwise null (primary-only / secondary-only / same unit).
     */
    @Nullable
    public static String resolveFallbackUnitAfterNoFill(
            @Nullable Context context, @Nullable String primaryUnit) {
        if (!MODE_PRIMARY_THEN_SECONDARY.equals(getNativeAdUnitMode(context))) {
            return null;
        }
        String secondary = getSecondUnitId(context);
        if (TextUtils.isEmpty(secondary)) {
            return null;
        }
        String primary = primaryUnit == null ? "" : primaryUnit.trim();
        if (!TextUtils.isEmpty(primary) && primary.equals(secondary)) {
            return null;
        }
        return secondary;
    }

    @NonNull
    private static String normalizeNativeAdUnitMode(@Nullable String raw) {
        if (raw == null) return DEFAULT_NATIVE_AD_UNIT_MODE;
        String v = raw.trim().toLowerCase();
        if (MODE_PRIMARY_ONLY.equals(v)
                || MODE_PRIMARY_THEN_SECONDARY.equals(v)
                || MODE_SECONDARY_ONLY.equals(v)) {
            return v;
        }
        return DEFAULT_NATIVE_AD_UNIT_MODE;
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

