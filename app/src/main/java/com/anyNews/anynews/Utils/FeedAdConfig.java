package com.anyNews.anynews.Utils;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * In-feed ad seats / format / unit + load knobs.
 *
 * <p>Seat placement is hardcoded at {@link #FIXED_AD_POSITIONS} (0-based FlipView
 * list indices: index 0 = first article, 1 = first ad, …). Admin/splash
 * {@code feed_ad_every_n} is inert for seats — still
 * persisted for compat but never used to place shells.
 *
 * <p>Other knobs arrive via splash / session ({@code feed_ad_format},
 * {@code ad_code}, {@code ad_prefetch_lookahead},
 * {@code max_native_ad_loads_in_flight}, {@code ad_impr_flush_every_n},
 * {@code destroy_native_ads_after_impression}).
 *
 * <p>July 28 parity: <b>one</b> native unit only
 * ({@link #DEFAULT_UNIT_ID} / admin {@code ad_code}). Dual-unit waterfall
 * (secondary / primary_then_secondary) is disabled in-app — same as July Play.
 *
 * <p>No-fill UX: skip unfilled seats (no Share / fake ad banner / black house).
 * Admin {@code default_ad_position} is retained for prefs compat; the app always
 * skips empty shells so users never park on a black ads page.
 *
 * <p>After a multi-format APK, admin alone switches native ↔ interstitial + unit;
 * no rebuild for format/unit changes.
 */
public final class FeedAdConfig {

    public static final String FORMAT_NATIVE = "native";
    public static final String FORMAT_INTERSTITIAL = "interstitial";

    /** Feed native unit selection — July: primary only. */
    public static final String MODE_PRIMARY_ONLY = "primary_only";
    /** @deprecated Dual-unit removed; kept for compile/compat only. */
    public static final String MODE_PRIMARY_THEN_SECONDARY = "primary_then_secondary";
    /** @deprecated Dual-unit removed; kept for compile/compat only. */
    public static final String MODE_SECONDARY_ONLY = "secondary_only";

    /** Production feed native unit (July / code / strings fallback). */
    public static final String DEFAULT_UNIT_ID =
            "ca-app-pub-8562336752221105/4901277505";

    /**
     * Unused (July one-unit). Kept so older prefs / call sites compile;
     * {@link #resolveFallbackUnitAfterNoFill} never returns it.
     */
    public static final String DEFAULT_SECOND_UNIT_ID =
            "ca-app-pub-8562336752221105/8909042175";

    /**
     * Hardcoded AdMob seats — 0-based flip list indices (position 0 = first
     * article, 1 = first ad seat, then 5/9/14/19). Exactly five seats; not every-N.
     */
    public static final int[] FIXED_AD_POSITIONS = {1, 5, 9, 14, 19};

    /**
     * @deprecated Seats use {@link #FIXED_AD_POSITIONS}. Kept for splash/prefs
     * compat and non-seat callers (share/ratings spacing hints).
     */
    @Deprecated
    public static final int DEFAULT_EVERY_N = 4;
    /** @deprecated Seats use {@link #FIXED_AD_POSITIONS}. */
    @Deprecated
    public static final int MIN_EVERY_N = 2;
    /** @deprecated Seats use {@link #FIXED_AD_POSITIONS}. */
    @Deprecated
    public static final int MAX_EVERY_N = 10;

    /**
     * 0 = never show a quiet default on no-fill (skip). Typical once-seat values
     * match feed {@code ad_req_position} (e.g. 1, 5, 9, 14, 19).
     */
    public static final int DEFAULT_DEFAULT_AD_POSITION = 0;
    public static final int MIN_DEFAULT_AD_POSITION = 0;
    public static final int MAX_DEFAULT_AD_POSITION = 50;

    /**
     * Match admin Performance range (3–12). Default covers the typical gap
     * between fixed seats (~4). Deeper windows stockpile matched ads that never
     * impress and depress match rate. App hard-caps seat AdLoader starts to 1.
     */
    public static final int DEFAULT_PREFETCH_LOOKAHEAD = 4;
    public static final int MIN_PREFETCH_LOOKAHEAD = 3;
    public static final int MAX_PREFETCH_LOOKAHEAD = 12;

    /**
     * Match admin Performance range (1–5). Default 1 = one seat AdLoader at a
     * time (July: single primary unit). Higher values raced AdPreloader and
     * over-requested without impressions.
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
     * July: one unit only (no secondary waterfall).
     * @deprecated Prefer {@link #DEFAULT_NATIVE_AD_UNIT_MODE}.
     */
    public static final boolean DEFAULT_USE_SECOND_NATIVE = false;

    /** July feed native unit mode: primary only. */
    public static final String DEFAULT_NATIVE_AD_UNIT_MODE = MODE_PRIMARY_ONLY;

    private static volatile int cachedEveryN = DEFAULT_EVERY_N;
    private static volatile int cachedPrefetchLookahead = DEFAULT_PREFETCH_LOOKAHEAD;
    private static volatile int cachedMaxInFlight = DEFAULT_MAX_IN_FLIGHT;
    private static volatile int cachedFlushEveryN = DEFAULT_FLUSH_EVERY_N;
    private static volatile int cachedDefaultAdPosition = DEFAULT_DEFAULT_AD_POSITION;
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
        // every-N warmed for prefs compat only — seats use FIXED_AD_POSITIONS.
        getEveryN(context);
        getPrefetchLookahead(context);
        getMaxInFlight(context);
        getFlushEveryN(context);
        getDefaultAdPosition(context);
        isDestroyAfterImpression(context);
        getNativeAdUnitMode(context);
        getFormat(context);
        getUnitId(context);
        getSecondUnitId(context);
    }

    /** Copy of hardcoded 0-based seat list indices. */
    @NonNull
    public static int[] fixedAdPositions() {
        return FIXED_AD_POSITIONS.clone();
    }

    /** True when {@code listIndex} is a fixed AdMob seat in the FlipView list. */
    public static boolean isFixedAdPosition(int listIndex) {
        for (int p : FIXED_AD_POSITIONS) {
            if (p == listIndex) return true;
        }
        return false;
    }

    /** Next fixed seat after {@code oneBasedPos}, or the same if last/unknown. */
    public static int nextFixedAdPosition(int oneBasedPos) {
        for (int i = 0; i < FIXED_AD_POSITIONS.length; i++) {
            if (FIXED_AD_POSITIONS[i] == oneBasedPos
                    && i + 1 < FIXED_AD_POSITIONS.length) {
                return FIXED_AD_POSITIONS[i + 1];
            }
        }
        return oneBasedPos > 0 ? oneBasedPos : FIXED_AD_POSITIONS[0];
    }

    /** First fixed seat (template ads_data / empty-page fallback). */
    public static int firstFixedAdPosition() {
        return FIXED_AD_POSITIONS[0];
    }

    /**
     * Persist optional single quiet-default seat from splash / feed envelope.
     * Null = old server; leave cache. 0 = skip all no-fills (recommended).
     */
    public static void saveDefaultAdPositionFromServer(
            @Nullable Context context, @Nullable Integer position) {
        if (position == null) return;
        int safe = clampDefaultAdPosition(position);
        cachedDefaultAdPosition = safe;
        if (context == null) return;
        SharedPrefsData.putString(
                context.getApplicationContext(),
                Constants.DEFAULT_AD_POSITION,
                String.valueOf(safe),
                Constants.PREF_NAME);
    }

    /** Admin quiet-default seat ({@code ad_req_position}); 0 = never / skip all. */
    public static int getDefaultAdPosition(@Nullable Context context) {
        if (context != null) {
            try {
                String v =
                        SharedPrefsData.getString(
                                context.getApplicationContext(),
                                Constants.DEFAULT_AD_POSITION,
                                Constants.PREF_NAME);
                if (!TextUtils.isEmpty(v)) {
                    int n = clampDefaultAdPosition(Integer.parseInt(v.trim()));
                    cachedDefaultAdPosition = n;
                    return n;
                }
            } catch (Exception ignored) {
            }
        }
        return clampDefaultAdPosition(cachedDefaultAdPosition);
    }

    public static int getDefaultAdPosition() {
        return clampDefaultAdPosition(cachedDefaultAdPosition);
    }

    /**
     * True when Admin enabled a quiet default at this feed {@code ad_req_position}.
     * When false, no-fill seats must be skipped (no Share / fake ad card).
     */
    public static boolean allowsDefaultAtReqPos(
            @Nullable Context context, @Nullable Integer reqPos) {
        if (reqPos == null) return false;
        int seat = getDefaultAdPosition(context);
        return seat > 0 && seat == reqPos;
    }

    private static int clampDefaultAdPosition(int n) {
        if (n < MIN_DEFAULT_AD_POSITION) return DEFAULT_DEFAULT_AD_POSITION;
        if (n > MAX_DEFAULT_AD_POSITION) return MAX_DEFAULT_AD_POSITION;
        return n;
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

    /**
     * Persist splash every-N for compat only — does <b>not</b> change seats
     * ({@link #FIXED_AD_POSITIONS}).
     */
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
        // July one-unit: ignore admin dual toggle; always primary_only.
        if (useSecond == null) return;
        saveNativeAdUnitModeFromServer(context, MODE_PRIMARY_ONLY);
    }

    public static void saveNativeAdUnitModeFromServer(
            @Nullable Context context, @Nullable String mode) {
        if (mode == null) return;
        // July one-unit: never persist dual / secondary_only.
        String safe = MODE_PRIMARY_ONLY;
        cachedNativeAdUnitMode = safe;
        if (context == null) return;
        Context app = context.getApplicationContext();
        SharedPrefsData.putString(
                app, Constants.FEED_NATIVE_AD_UNIT_MODE, safe, Constants.PREF_NAME);
        SharedPrefsData.putBool(
                app, Constants.USE_SECOND_NATIVE_AD_UNIT, false, Constants.PREF_NAME);
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
        // July one-unit: ignore secondary id from server (never requested).
    }

    /**
     * @deprecated Inert for seat placement — seats use {@link #FIXED_AD_POSITIONS}.
     * Still reads splash/prefs so older callers (share/ratings spacing) compile.
     */
    @Deprecated
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

    /** @deprecated Seats use {@link #FIXED_AD_POSITIONS}. */
    @Deprecated
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
     * July one-unit: always false (no secondary waterfall).
     */
    public static boolean isUseSecondNativeAdUnit(@Nullable Context context) {
        return false;
    }

    public static boolean isUseSecondNativeAdUnit() {
        return false;
    }

    /**
     * July one-unit: always {@link #MODE_PRIMARY_ONLY}.
     */
    @NonNull
    public static String getNativeAdUnitMode(@Nullable Context context) {
        cachedNativeAdUnitMode = MODE_PRIMARY_ONLY;
        return MODE_PRIMARY_ONLY;
    }

    @NonNull
    public static String getNativeAdUnitMode() {
        cachedNativeAdUnitMode = MODE_PRIMARY_ONLY;
        return MODE_PRIMARY_ONLY;
    }

    public static boolean isSecondaryOnly(@Nullable Context context) {
        return false;
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
     * Effective unit for feed native loads — July: always primary {@code ad_code}.
     */
    @NonNull
    public static String getUnitId(@Nullable Context context) {
        return getPrimaryUnitId(context);
    }

    @NonNull
    public static String getUnitId() {
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
     * July one-unit: never waterfall to a second AdMob unit.
     */
    @Nullable
    public static String resolveFallbackUnitAfterNoFill(
            @Nullable Context context, @Nullable String primaryUnit) {
        return null;
    }

    @NonNull
    private static String normalizeNativeAdUnitMode(@Nullable String raw) {
        return MODE_PRIMARY_ONLY;
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

