package com.anyNews.anynews.Utils;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.anyNews.anynews.BuildConfig;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.google.gson.JsonObject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.HttpUrl;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Debug APK only ({@link BuildConfig#DEBUG}) fire-and-forget reporting when the
 * client hits Node/Mongo for routes that should use Cloudflare public edge — or
 * other unexpected origin calls. Play Store / release builds send no beacons.
 *
 * <p>Wired via {@link EdgeOriginWatchInterceptor} on {@link RestApiClient}. Manual beacons
 * (Main/Local List fallback, push CF fail) also go through {@link #report}.
 */
public final class EdgeOriginCallWatch {
    public static final String HEADER_SKIP = "X-Edge-Origin-Watch-Skip";
    public static final String HEADER_DETAIL = "X-Edge-Origin-Watch-Detail";

    private static final String TAG = "EdgeOriginCallWatch";
    private static final long THROTTLE_MS = 2500L;

    /** Routes that should not hit origin for news body reads on the new APK. */
    private static final Set<String> UNEXPECTED_ROUTE_SUFFIXES = new HashSet<>(Arrays.asList(
            "list_all_articles",
            "flash_news",
            "get_single_article"
    ));

    /**
     * Intentional origin traffic (writes, session, rewards, profile, splash draft,
     * search helpers that still POST by product design, CF public GETs, beacon).
     */
    private static final Set<String> ALLOWED_ROUTE_SUFFIXES = new HashSet<>(Arrays.asList(
            // Public edge (correct CF path) + this beacon
            "public/feed",
            "public/splash_config",
            "public/daily_updates",
            "public/edge_origin_event",
            "public/edge_telemetry",
            // Splash draft still needed for first open
            "splash_screen",
            // Likes / comments
            "update_like",
            "add_like",
            "add_comment",
            "list_all_comment_for_article",
            "comments/like",
            "comments/report",
            "comments/report-problems/list",
            // Share / open counts (intentional writes)
            "update_post_count",
            "daily_updates_share",
            "views",
            "views/batch",
            // Login / session / OTP
            "session",
            "generate_user_otp",
            "validate_user_otp",
            "get_login_status",
            "delete-account",
            // Profile / settings / language / geo
            "get_user_profile",
            "update_user_name",
            "update_reporter_profile",
            "update_profile_pic",
            "user_profile_update",
            "get_settings",
            "update_settings",
            "settings",
            "update_user_language",
            "list_all_languages",
            "get_states",
            "update_state",
            "get_districts",
            "update_district",
            "regions",
            "onboarding/region",
            "list_all_categories",
            "list_all_categories_with_language",
            "category_search",
            "get_static_data",
            "enc_dec",
            // FCM / notifications / ratings / survey
            "update_device_fcm_token",
            "notifications",
            "ack_notification_reprompt",
            "log_notification_permission_ask",
            "ratings",
            "survey_response",
            "survey/poll",
            "events",
            "event_link",
            "disable_update_menu",
            // Reporter / follow / posts
            "get_writer_profile",
            "update_follow",
            "get_follow_list",
            "list_followers",
            "follow_more",
            "add_article_news",
            "edit_user_post",
            "get_user_posts",
            "list_all_user_articles",
            "get_article_transactions",
            "reporter_applicant_get",
            "reporter_applicant_upsert_profile",
            "reporter_applicant_create_post",
            "get_report_issues",
            "add_issue",
            "add_report",
            "get_jobs",
            "report_job",
            // Rewards / wallet / scratch
            "rewards_hub",
            "rewards_leaderboard",
            "rewards_events_batch",
            "rewards_join",
            "rewards_update_upi",
            "rewards_withdraw",
            "rewards_convert",
            "rewards_history",
            "rewards_withdrawal_proof",
            "rewards_checkin",
            "rewards_watch_ads_claim",
            "rewards_rating_submit",
            "rewards_earnings_status",
            "rewards_set_enabled",
            "get_redeem_transactions",
            "redeem_wallet",
            "get_scratch_cards",
            "update_scratch_cards",
            // Birthday
            "birthday_config",
            "birthday_schedule",
            "birthday_preview",
            "birthday_my",
            "birthday_update",
            "birthday_cancel",
            // Advertise / referral / help / ads accounting
            "advertise_offer",
            "advertise_start",
            "advertise_messages",
            "advertise_send",
            "referral/integrity-nonce",
            "referral/claim-install",
            "referral/create-link",
            "list_all_help_data",
            "fetch_advertisement",
            "report_media_access",
            "report_ad_impressions",
            // Search still intentional POST (not CF yet)
            "search_suggestions",
            "search_stories",
            // Non-feed datasets (not CF public feed)
            "cricket_active_match",
            "cricket_match_detail",
            "fuel_dataset",
            "ttd_dataset",
            "stock_market",
            // Uploads
            "upload_image",
            "upload_file"
    ));

    private static final ExecutorService BEACON_EXEC =
            Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "edge-origin-watch");
                t.setDaemon(true);
                return t;
            });

    private static final ConcurrentHashMap<String, Long> LAST_FIRE = new ConcurrentHashMap<>();
    /** Optional detail override for the next matching unexpected route (cross-thread). */
    private static final ConcurrentHashMap<String, String> PENDING_DETAIL =
            new ConcurrentHashMap<>();

    private EdgeOriginCallWatch() {
    }

    /**
     * Prefer this detail when the interceptor next sees {@code routeKey}
     * (e.g. {@code list_all_articles} after a Main/Local CF miss). Collapses with
     * manual {@link #report} via throttle when detail+endpoint match.
     */
    public static void annotateNextUnexpected(
            @Nullable String routeKey, @Nullable String detail) {
        if (!isInstrumentedBuild()) return;
        if (TextUtils.isEmpty(routeKey) || TextUtils.isEmpty(detail)) return;
        PENDING_DETAIL.put(routeKey.trim().toLowerCase(Locale.US), detail.trim());
    }

    public static boolean isInstrumentedBuild() {
        return BuildConfig.DEBUG;
    }

    /**
     * Inspect an outbound API URL. Fire-and-forget beacon when unexpected.
     * Never throws; never blocks the caller beyond trivial map lookups.
     */
    public static void observeRequest(
            @Nullable HttpUrl url,
            @Nullable String method,
            @Nullable String detailHint) {
        if (!isInstrumentedBuild() || url == null) return;
        try {
            String path = url.encodedPath();
            if (TextUtils.isEmpty(path)) return;
            String lower = path.toLowerCase(Locale.US);
            if (lower.contains("edge_origin_event")) return;
            if (lower.contains("/upload/")) return;

            String route = normalizeRoute(lower);
            if (TextUtils.isEmpty(route)) return;

            // Correct CF attempts — server records A/B/C/E on Node miss; do not beacon.
            if ("public/feed".equals(route)
                    || "public/splash_config".equals(route)
                    || "public/daily_updates".equals(route)) {
                return;
            }

            String detail;
            String reason;
            if (isUnexpectedRoute(route)) {
                String pending = PENDING_DETAIL.remove(route);
                if (!TextUtils.isEmpty(detailHint)) {
                    detail = detailHint;
                } else if (!TextUtils.isEmpty(pending)) {
                    detail = pending;
                } else {
                    detail = defaultUnexpectedDetail(route);
                }
                // Special / video / search / flash List_All_Articles are intentional origin.
                if (isExpectedIntentionalListDetail(detail)) {
                    return;
                }
                reason = reasonForDetail(detail);
            } else if (isAllowedRoute(route)) {
                return;
            } else {
                // Unknown /app route on new APK — surface so team can allowlist or move to CF.
                detail = !TextUtils.isEmpty(detailHint)
                        ? detailHint
                        : "unknown_origin_call";
                reason = "unexpected_origin";
            }

            String endpoint = path.length() > 120 ? path.substring(0, 120) : path;
            report(
                    reason,
                    endpoint,
                    0L,
                    0,
                    0,
                    0,
                    detail,
                    url.toString());
        } catch (Exception e) {
            Logone.w(e, TAG + " observe failed");
        }
    }

    /** Manual beacon (Main/Local fallback, push CF fail, etc.). */
    public static void report(
            @Nullable String reason,
            @Nullable String endpoint,
            long postId,
            int languageId,
            int stateId,
            int distId,
            @Nullable String detail) {
        report(reason, endpoint, postId, languageId, stateId, distId, detail, null);
    }

    public static void report(
            @Nullable String reason,
            @Nullable String endpoint,
            long postId,
            int languageId,
            int stateId,
            int distId,
            @Nullable String detail,
            @Nullable String url) {
        if (!isInstrumentedBuild()) return;
        final String safeReason = TextUtils.isEmpty(reason) ? "unexpected_origin" : reason.trim();
        final String safeEndpoint = TextUtils.isEmpty(endpoint)
                ? "/app/v3/List_All_Articles"
                : endpoint.trim();
        final String safeDetail = TextUtils.isEmpty(detail) ? safeReason : detail.trim();
        final String throttleKey = safeEndpoint + "|" + safeDetail;
        long now = System.currentTimeMillis();
        Long prev = LAST_FIRE.put(throttleKey, now);
        if (prev != null && (now - prev) < THROTTLE_MS) {
            return;
        }

        BEACON_EXEC.execute(() -> fireBeacon(
                safeReason,
                safeEndpoint,
                postId,
                languageId,
                stateId,
                distId,
                safeDetail,
                url,
                now));
    }

    private static void fireBeacon(
            String reason,
            String endpoint,
            long postId,
            int languageId,
            int stateId,
            int distId,
            String detail,
            @Nullable String url,
            long atMs) {
        try {
            JsonObject body = new JsonObject();
            body.addProperty("reason", reason);
            body.addProperty("endpoint", endpoint);
            if (postId > 0) body.addProperty("post_id", postId);
            if (languageId > 0) body.addProperty("language_id", languageId);
            if (stateId > 0) body.addProperty("state_id", stateId);
            if (distId > 0) body.addProperty("dist_id", distId);
            body.addProperty("cf_status", "miss");
            body.addProperty(
                    "detail",
                    detail.length() > 160 ? detail.substring(0, 160) : detail);
            body.addProperty("version_code", BuildConfig.VERSION_CODE);
            body.addProperty("version_name", BuildConfig.VERSION_NAME);
            body.addProperty("app_version_code", BuildConfig.VERSION_CODE);
            body.addProperty("ts", atMs);
            if (!TextUtils.isEmpty(url)) {
                String u = url.length() > 160 ? url.substring(0, 160) : url;
                body.addProperty("url", u);
            }
            body.addProperty("source", "apk_client");

            APIService api = RestApiClient.getClient().create(APIService.class);
            Call<Void> call = api.Public_Edge_Origin_Event("v3", body);
            if (call == null) return;
            // Path contains edge_origin_event → interceptor skips observe.
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> c, Response<Void> response) {
                    /* best-effort */
                }

                @Override
                public void onFailure(Call<Void> c, Throwable t) {
                    /* best-effort */
                }
            });
        } catch (Exception e) {
            Logone.w(e, TAG + " beacon failed");
        }
    }

    /** Normalize {@code /app/v3/Foo_Bar} → {@code foo_bar}; keeps nested paths. */
    @NonNull
    static String normalizeRoute(@NonNull String lowerPath) {
        String p = lowerPath;
        int appIdx = p.indexOf("/app/");
        if (appIdx >= 0) {
            p = p.substring(appIdx + "/app/".length());
        } else if (p.startsWith("app/")) {
            p = p.substring(4);
        }
        // Drop version segment v1/v2/v3
        if (p.startsWith("v1/") || p.startsWith("v2/") || p.startsWith("v3/")) {
            p = p.substring(3);
        } else if (p.equals("v1") || p.equals("v2") || p.equals("v3")) {
            return "";
        }
        // Dynamic "{langId}/articles" → treat as articles feed
        if (p.matches("\\d+/articles.*")) {
            return "list_all_articles";
        }
        while (p.endsWith("/")) {
            p = p.substring(0, p.length() - 1);
        }
        return p;
    }

    private static boolean isUnexpectedRoute(@NonNull String route) {
        if (UNEXPECTED_ROUTE_SUFFIXES.contains(route)) return true;
        for (String s : UNEXPECTED_ROUTE_SUFFIXES) {
            if (route.endsWith("/" + s)) return true;
        }
        return false;
    }

    private static boolean isAllowedRoute(@NonNull String route) {
        if (ALLOWED_ROUTE_SUFFIXES.contains(route)) return true;
        for (String s : ALLOWED_ROUTE_SUFFIXES) {
            if (route.equals(s) || route.startsWith(s + "/") || route.endsWith("/" + s)) {
                return true;
            }
        }
        // calendar year packs
        if (route.startsWith("calendar/")) return true;
        return false;
    }

    /** Product-intentional List/Flash reads — do not spam edge_origin_event. */
    static boolean isExpectedIntentionalListDetail(@Nullable String detail) {
        if (TextUtils.isEmpty(detail)) return false;
        switch (detail.trim()) {
            case "expected_special_posts":
            case "expected_video_posts":
            case "expected_search_feed":
            case "expected_flash_feed":
            case "expected_category_feed":
                return true;
            default:
                return false;
        }
    }

    /**
     * Call before an intentional {@code List_All_Articles} POST (not CF soft-fallback).
     */
    public static void annotateExpectedListOrigin(@Nullable String detail) {
        if (!isInstrumentedBuild()) return;
        if (TextUtils.isEmpty(detail)) return;
        annotateNextUnexpected("list_all_articles", detail.trim());
    }

    @NonNull
    private static String defaultUnexpectedDetail(@NonNull String route) {
        if ("list_all_articles".equals(route)) return "direct_list_call";
        if ("flash_news".equals(route)) return "flash_news_origin";
        if ("get_single_article".equals(route)) return "direct_single_article";
        return "unexpected_origin_call";
    }

    @NonNull
    private static String reasonForDetail(@NonNull String detail) {
        if ("list_fallback_after_cf_miss".equals(detail)
                || "client_main_local_list_fallback".equals(detail)
                || "apk_main_local_list_fallback".equals(detail)) {
            return "client_main_local_list_fallback";
        }
        if ("push_cf_fail_no_list".equals(detail)
                || "push_cf_miss_list_fallback".equals(detail)
                || "client_push_cf_fail_no_list".equals(detail)
                || "apk_push_cf_fail_no_list".equals(detail)) {
            return "client_push_cf_fail_no_list";
        }
        if ("direct_list_call".equals(detail)) return "client_direct_list_call";
        if ("flash_news_origin".equals(detail)) return "client_flash_news_origin";
        return "unexpected_origin";
    }
}
