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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * When enabled: POST CF-Cache-Status for public feed GETs → Admin public edge stats.
 */
public final class PublicEdgeTelemetry {
    private static final String TAG = "PublicEdgeTelemetry";
    private static final long THROTTLE_MS = 4000L;
    private static final ExecutorService EXEC = Executors.newSingleThreadExecutor();
    private static final ConcurrentHashMap<String, Long> LAST = new ConcurrentHashMap<>();

    private PublicEdgeTelemetry() {
    }

    public static boolean isEnabled() {
        return BuildConfig.PUBLIC_EDGE_TELEMETRY;
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
            EXEC.execute(() -> fire(feedKind, languageId, skip, cfFinal, age));
        } catch (Exception e) {
            Logone.w(e, TAG + " report skipped");
        }
    }

    private static void fire(
            String feedKind,
            int languageId,
            long skip,
            String cfStatus,
            int ageSec) {
        try {
            JsonObject body = new JsonObject();
            body.addProperty("cf_cache_status", cfStatus);
            body.addProperty("feed_kind", feedKind);
            if (languageId > 0) body.addProperty("language_id", languageId);
            if (skip > 0) body.addProperty("skip", skip);
            if (ageSec >= 0) body.addProperty("age", ageSec);
            body.addProperty("version_code", BuildConfig.VERSION_CODE);

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

    private static int parseAge(@Nullable String raw) {
        if (TextUtils.isEmpty(raw)) return -1;
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}