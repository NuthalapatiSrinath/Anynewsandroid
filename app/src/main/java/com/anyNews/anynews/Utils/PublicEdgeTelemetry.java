package com.anyNews.anynews.Utils;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.anyNews.anynews.BuildConfig;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.google.gson.JsonObject;

import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * When enabled: POST CF-Cache-Status for public feed GETs → Admin public edge stats.
 */
public final class PublicEdgeTelemetry {
    private static final String TAG = "PublicEdgeTelemetry";
    private static final long THROTTLE_MS = 4000L;
    private static final long TIMELINE_THROTTLE_MS = 1500L;
    private static final ExecutorService EXEC = Executors.newSingleThreadExecutor();
    private static final ConcurrentHashMap<String, Long> LAST = new ConcurrentHashMap<>();

    private PublicEdgeTelemetry() {
    }

    public static boolean isEnabled() {
        return BuildConfig.PUBLIC_EDGE_TELEMETRY;
    }

  /**
   * Debug APK: POST every outbound API call (throttled) for Admin session timeline.
   */
    public static void reportOutboundCall(
            @Nullable Request request, @Nullable okhttp3.Response response) {
        if (!EdgeOriginCallWatch.isInstrumentedBuild() || request == null) return;
        try {
            HttpUrl url = request.url();
            if (url == null) return;
            String path = url.encodedPath();
            if (TextUtils.isEmpty(path) || path.toLowerCase().contains("edge_telemetry")) {
                return;
            }
            Headers headers = response != null ? response.headers() : request.headers();
            String cf = firstHeader(headers, "CF-Cache-Status", "cf-cache-status");
            if (TextUtils.isEmpty(cf)) {
                cf = guessCfForRoute(path);
            }
            String sessionId = EdgeOriginCallWatch.resolveAppSessionId();
            String throttleKey =
                    String.format(
                            Locale.US,
                            "%s|%s|%s",
                            sessionId != null ? sessionId : "-",
                            path,
                            request.method());
            long now = System.currentTimeMillis();
            Long prev = LAST.put(throttleKey, now);
            if (prev != null && (now - prev) < TIMELINE_THROTTLE_MS) {
                return;
            }
            final String cfFinal = cf;
            final String feedKind = feedKindFromPath(path);
            EXEC.execute(
                    () ->
                            fire(
                                    feedKind,
                                    0,
                                    0,
                                    cfFinal,
                                    parseAge(headers != null ? headers.get("Age") : null),
                                    path,
                                    request.method(),
                                    url.toString(),
                                    sessionId));
        } catch (Exception e) {
            Logone.w(e, TAG + " timeline skipped");
        }
    }

    /**
     * @param feedKind main | special | flash | splash | daily_updates
     */
    public static void reportFeedResponse(
            @Nullable Response<?> response,
            @NonNull String feedKind,
            int languageId,
            long skip) {
        if (!isEnabled() || response == null) return;
        try {
            Headers headers = response.headers();
            String cf =
                    firstHeader(headers, "CF-Cache-Status", "cf-cache-status");
            if (TextUtils.isEmpty(cf)) {
                cf = "MISS";
            }
            int age = parseAge(headers.get("Age"));
            String throttleKey =
                    String.format(
                            Locale.US,
                            "%s|%d|%d|%s",
                            feedKind,
                            languageId,
                            skip,
                            cf.toUpperCase(Locale.US));
            long now = System.currentTimeMillis();
            Long prev = LAST.put(throttleKey, now);
            if (prev != null && (now - prev) < THROTTLE_MS) {
                return;
            }
            final String cfFinal = cf;
            final String sessionId = EdgeOriginCallWatch.resolveAppSessionId();
            EXEC.execute(
                    () ->
                            fire(
                                    feedKind,
                                    languageId,
                                    skip,
                                    cfFinal,
                                    age,
                                    null,
                                    "GET",
                                    null,
                                    sessionId));
        } catch (Exception e) {
            Logone.w(e, TAG + " report skipped");
        }
    }

    private static void fire(
            String feedKind,
            int languageId,
            long skip,
            String cfStatus,
            int ageSec,
            @Nullable String endpoint,
            @Nullable String method,
            @Nullable String url,
            @Nullable String sessionId) {
        try {
            JsonObject body = new JsonObject();
            body.addProperty("cf_cache_status", cfStatus);
            body.addProperty("feed_kind", feedKind);
            if (languageId > 0) body.addProperty("language_id", languageId);
            if (skip > 0) body.addProperty("skip", skip);
            if (ageSec >= 0) body.addProperty("age", ageSec);
            body.addProperty("version_code", BuildConfig.VERSION_CODE);
            if (!TextUtils.isEmpty(endpoint)) {
                body.addProperty(
                        "endpoint",
                        endpoint.length() > 120 ? endpoint.substring(0, 120) : endpoint);
            }
            if (!TextUtils.isEmpty(method)) {
                body.addProperty("method", method);
            }
            if (!TextUtils.isEmpty(url)) {
                body.addProperty("url", url.length() > 160 ? url.substring(0, 160) : url);
            }
            if (!TextUtils.isEmpty(sessionId)) {
                body.addProperty("session_id", sessionId);
            }

            APIService api = RestApiClient.getClient().create(APIService.class);
            Call<Void> call = api.Public_Edge_Telemetry("v3", body);
            if (call == null) return;
            call.enqueue(
                    new Callback<Void>() {
                        @Override
                        public void onResponse(@NonNull Call<Void> c, @NonNull Response<Void> r) {
                            /* best-effort */
                        }

                        @Override
                        public void onFailure(@NonNull Call<Void> c, @NonNull Throwable t) {
                            /* best-effort */
                        }
                    });
        } catch (Exception e) {
            Logone.w(e, TAG + " fire failed");
        }
    }

    @NonNull
    private static String firstHeader(Headers headers, String... names) {
        for (String n : names) {
            String v = headers.get(n);
            if (!TextUtils.isEmpty(v)) return v.trim();
        }
        return "";
    }

    @NonNull
    private static String guessCfForRoute(@NonNull String path) {
        String route = EdgeOriginCallWatch.normalizeRoute(path.toLowerCase(Locale.US));
        if (EdgeOriginCallWatch.isPublicEdgeRoute(route)) {
            return "MISS";
        }
        return "ORIGIN";
    }

    @NonNull
    private static String feedKindFromPath(@NonNull String path) {
        String lower = path.toLowerCase(Locale.US);
        if (lower.contains("/public/splash")) return "splash";
        if (lower.contains("/public/daily_updates")
                || lower.contains("/public/daily/lang")) return "daily_updates";
        if (lower.contains("/public/flash/")) return "flash";
        if (lower.contains("/public/special/")) return "special";
        if (lower.contains("/public/local/")) return "main";
        if (lower.contains("/public/main/")) return "main";
        if (lower.contains("/public/feed")) return "main";
        return "main";
    }

    private static int parseAge(@Nullable String raw) {
        if (TextUtils.isEmpty(raw)) return -1;
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}