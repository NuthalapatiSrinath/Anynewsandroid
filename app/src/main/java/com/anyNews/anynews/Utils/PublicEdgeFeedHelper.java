package com.anyNews.anynews.Utils;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.anyNews.anynews.BuildConfig;
import com.anyNews.anynews.Pojo.EncreptionRequest;
import com.anyNews.anynews.Pojo.PublicEdgeFeedResponse;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import android.os.Handler;
import android.os.Looper;

import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.HttpUrl;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.anyNews.anynews.Utils.MyUtils.encrypt;

/**
 * Cloudflare-first public feed reads (anonymous GET).
 *
 * Main News ({@code category_id=0}): anonymous GET uses language only (state/dist
 * omitted — one CF URL per language). Local ({@code category_id=-1}) sends draft geo.
 * Push/share pin prefer
 * {@code GET /app/v3/public/feed} (CF HIT → no origin). On CF miss/fail,
 * callers soft-fall to encrypted {@code List_All_Articles} so Node still
 * serves Redis→Mongo. CF is optional; origin must always work. Writes stay POST.
 *
 * Response is wrapped into {@link EncreptionRequest} so existing
 * {@code convertListOfArticles} decrypt path stays unchanged.
 */
public final class PublicEdgeFeedHelper {

    private static final String TAG = "PublicEdgeFeed";

    /** Matches server {@code EDGE_FEED_PAGE_SIZE} / CF public feed page. */
    public static final int EDGE_FEED_PAGE_SIZE = 50;

    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());
    private static final ExecutorService EDGE_WRAP_EXEC = Executors.newSingleThreadExecutor();

    private PublicEdgeFeedHelper() {
    }

    public static int clampFeedPage(int page) {
        return Math.max(0, Math.min(page, 4));
    }

    /** Flash feed only — server page index from legacy skip stride (8). */
    public static int flashPageFromSkip(long skip) {
        return (int) Math.min(Math.max(0L, skip) / 8L, 4L);
    }

    /**
     * True for feeds that can use anonymous GET {@code /public/feed} (CF-backed).
     * Search, video, and category browse stay on encrypted POST.
     */
    public static boolean canUsePublicEdge(
            boolean searchMode,
            boolean specialMode,
            boolean videoMode,
            boolean flashMode,
            int categoryId) {
        if (searchMode || videoMode) return false;
        if (flashMode) return true;
        if (specialMode) return categoryId == 0 || categoryId == -1;
        return categoryId == 0 || categoryId == -1;
    }

    /**
     * Coalesces concurrent identical public feed GETs (Starting prefetch + Home page-0).
     */
    @Nullable
    public static Call<PublicEdgeFeedResponse> enqueuePublicFeedCoalesced(
            @NonNull Context context,
            int categoryId,
            long skip,
            long pinPostId,
            boolean specialMode,
            boolean flashMode,
            @NonNull Callback<PublicEdgeFeedResponse> callback) {
        int page = flashMode ? flashPageFromSkip(skip) : 0;
        return enqueuePublicFeedCoalesced(
                context,
                categoryId,
                skip,
                pinPostId,
                specialMode,
                flashMode,
                page,
                callback);
    }

    @Nullable
    public static Call<PublicEdgeFeedResponse> enqueuePublicFeedCoalesced(
            @NonNull Context context,
            int categoryId,
            long skip,
            long pinPostId,
            boolean specialMode,
            boolean flashMode,
            int feedPage,
            @NonNull Callback<PublicEdgeFeedResponse> callback) {
        Call<PublicEdgeFeedResponse> call =
                buildPublicFeedCall(
                        context,
                        categoryId,
                        skip,
                        pinPostId,
                        specialMode,
                        flashMode,
                        feedPage);
        if (call == null) {
            callback.onFailure(
                    null, new IOException("public feed call unavailable"));
            return null;
        }
        int page = clampFeedPage(feedPage);
        NetworkCallDeduper.enqueueUnique(
                publicFeedDedupeKey(
                        context,
                        categoryId,
                        page,
                        pinPostId,
                        specialMode,
                        flashMode),
                call,
                callback);
        return call;
    }

    @NonNull
    static String publicFeedDedupeKey(
            @NonNull Context context,
            int categoryId,
            int feedPage,
            long pinPostId,
            boolean specialMode,
            boolean flashMode) {
        int languageId = SharedPrefsData.getInt(
                context, Constants.LanguageID, Constants.PREF_NAME);
        int stateId = 0;
        int distId = 0;
        if (categoryId == -1) {
            stateId = SharedPrefsData.getInt(
                    context, Constants.StateID, Constants.PREF_NAME);
            distId = SharedPrefsData.getInt(
                    context, Constants.Selected_Distric_Id, Constants.PREF_NAME);
        }
        long pin = feedPage <= 0 ? Math.max(0L, pinPostId) : 0L;
        String kind = flashMode ? "flash" : (specialMode ? "special" : "main");
        return String.format(
                Locale.US,
                "public_feed_v3_%s_%d_%d_%d_%d_%d",
                kind,
                languageId,
                stateId,
                distId,
                clampFeedPage(feedPage),
                pin);
    }

    @Nullable
    public static Call<PublicEdgeFeedResponse> buildPublicFeedCall(
            @NonNull Context context,
            int categoryId,
            long skip,
            long pinPostId,
            boolean specialMode,
            boolean flashMode) {
        int page = flashMode ? flashPageFromSkip(skip) : 0;
        return buildPublicFeedCall(
                context, categoryId, skip, pinPostId, specialMode, flashMode, page);
    }

    @Nullable
    public static Call<PublicEdgeFeedResponse> buildPublicFeedCall(
            @NonNull Context context,
            int categoryId,
            long skip,
            long pinPostId,
            boolean specialMode,
            boolean flashMode,
            int feedPage) {
        try {
            int languageId = SharedPrefsData.getInt(
                    context, Constants.LanguageID, Constants.PREF_NAME);
            if (languageId <= 0) return null;

            int stateId = 0;
            int distId = 0;
            if (!flashMode && categoryId == -1) {
                stateId = SharedPrefsData.getInt(
                        context, Constants.StateID, Constants.PREF_NAME);
                distId = SharedPrefsData.getInt(
                        context, Constants.Selected_Distric_Id, Constants.PREF_NAME);
                if (stateId <= 0 || distId <= 0) {
                    return null;
                }
            }

            APIService api = RestApiClient.getClient().create(APIService.class);
            int page = clampFeedPage(feedPage);
            Integer pin =
                    !specialMode
                            && !flashMode
                            && page <= 0
                            && skip <= 0L
                            && pinPostId > 0
                            ? (int) Math.min(pinPostId, Integer.MAX_VALUE)
                            : null;
            if (flashMode) {
                return api.Public_Feed_Flash("v3", languageId, page);
            }
            if (specialMode) {
                return api.Public_Feed_Special("v3", languageId, page);
            }
            if (categoryId == -1 && stateId > 0 && distId > 0) {
                return api.Public_Feed_Local(
                        "v3", languageId, stateId, distId, page);
            }
            return api.Public_Feed_Main("v3", languageId, page, pin);
        } catch (Exception e) {
            Logone.w(e, TAG + " build call failed");
            return null;
        }
    }

    /** Debug: request URL + CF-Cache-Status when present. */
    public static void logPublicFeedResponse(
            @Nullable Call<PublicEdgeFeedResponse> call,
            @Nullable Response<PublicEdgeFeedResponse> response) {
        if (!BuildConfig.DEBUG) {
            return;
        }
        try {
            String url = "?";
            if (call != null && call.request() != null) {
                HttpUrl httpUrl = call.request().url();
                if (httpUrl != null) {
                    url = httpUrl.toString();
                }
            }
            String cf = "none";
            if (response != null && response.headers() != null) {
                String h = response.headers().get("CF-Cache-Status");
                if (TextUtils.isEmpty(h)) {
                    h = response.headers().get("cf-cache-status");
                }
                if (!TextUtils.isEmpty(h)) {
                    cf = h.trim();
                }
            }
            Logone.d(TAG, "public feed GET url=" + url + " cf-cache=" + cf);
        } catch (Exception ignored) {
        }
    }

    public interface PlainEdgeDelivery {
        void onReady(@Nullable EncreptionRequest enc);
    }

    /** Build plain JSON envelope off UI thread; deliver on main. */
    public static void deliverPlainEdgeOnMain(
            @Nullable Context context,
            @Nullable PublicEdgeFeedResponse body,
            long pinPostId,
            long skip,
            @Nullable Set<Long> seenPostIds,
            @NonNull PlainEdgeDelivery delivery) {
        EDGE_WRAP_EXEC.execute(
                () -> {
                    EncreptionRequest enc =
                            toEncreptionRequestPlain(
                                    context, body, pinPostId, skip, seenPostIds);
                    MAIN_HANDLER.post(() -> delivery.onReady(enc));
                });
    }

    /**
     * True when the public edge body is usable for painting page-0.
     * Empty {@code data[]} / {@code success:false} / null → soft-fallback to
     * {@code List_All_Articles} (Node → Redis → Mongo). Never treat an empty
     * CF HIT as a real feed — that left FlipView on skeleton / "no information".
     */
    public static boolean hasUsablePosts(@Nullable PublicEdgeFeedResponse body) {
        if (body == null || !Boolean.TRUE.equals(body.getSuccess())) {
            return false;
        }
        JsonArray posts = body.getData();
        return posts != null && posts.size() > 0;
    }

    /**
     * Accept edge JSON for enqueue. Requires non-empty {@code data[]} so broken
     * CF/Redis twins soft-fall to {@code List_All_Articles} (true EOF still works
     * via List on the last page). Non-200 / null body handled by caller.
     */
    public static boolean isAcceptableEdgeResponse(
            @Nullable PublicEdgeFeedResponse body,
            long skip) {
        if (body == null || !Boolean.TRUE.equals(body.getSuccess())) {
            return false;
        }
        if (skip <= 0L) {
            return hasUsablePosts(body);
        }
        // Page N+: empty only when the twin is healthy. Broken national twins on prod
        // returned data:[] for every skip while List_All_Articles still had rows —
        // treating that as EOF caused premature "ఇక చదవడానికి వార్తలు లేవు".
        return hasUsablePosts(body);
    }

    /**
     * Human-readable reason when {@link #isAcceptableEdgeResponse} is false (logs / beacons).
     * Distinguishes HTTP errors from empty CF HIT so page-0 can retry transient 5xx once.
     */
    @NonNull
    public static String describeEdgeReject(
            @Nullable Response<PublicEdgeFeedResponse> response, long skip) {
        if (response == null) {
            return "no_response";
        }
        if (!response.isSuccessful()) {
            return "http_" + response.code();
        }
        PublicEdgeFeedResponse body = response.body();
        if (body == null) {
            return "null_body";
        }
        if (!Boolean.TRUE.equals(body.getSuccess())) {
            return "success_false";
        }
        JsonArray posts = body.getData();
        int n = posts != null ? posts.size() : 0;
        if (skip <= 0L && n == 0) {
            return "page0_empty_data";
        }
        if (n == 0) {
            return "pageN_empty_data";
        }
        return "unknown";
    }

    /** True for one-shot page-0 edge re-enqueue (network / origin 5xx), not empty CF twin. */
    public static boolean isTransientEdgeFailure(
            @Nullable Response<PublicEdgeFeedResponse> response,
            @Nullable Throwable error,
            long skip) {
        if (skip > 0L) return false;
        if (error != null) return true;
        if (response == null) return true;
        int code = response.code();
        return !response.isSuccessful() && code >= 500 && code != 501;
    }

    /**
     * Map public JSON into an encrypted {@link EncreptionRequest} envelope.
     * Dedupes pin post if it appears twice. Soft-fail → null.
     * Does <b>not</b> reject empty posts — callers use
     * {@link #isAcceptableEdgeResponse} for page-0 miss → List fallback.
     */
    @Nullable
    public static EncreptionRequest toEncreptionRequest(
            @Nullable Context context,
            @Nullable PublicEdgeFeedResponse body,
            long pinPostId) {
        return toEncreptionRequest(context, body, pinPostId, 0L, null);
    }

    /**
     * Share page-0: pin first + dedupe. Page N+: drop rows already in {@code seenPostIds}
     * so shared posts do not reappear on CF HITs (no {@code post_id} on URL).
     */
    @Nullable
    public static EncreptionRequest toEncreptionRequest(
            @Nullable Context context,
            @Nullable PublicEdgeFeedResponse body,
            long pinPostId,
            long skip,
            @Nullable Set<Long> seenPostIds) {
        if (body == null || !Boolean.TRUE.equals(body.getSuccess())) {
            return null;
        }
        JsonArray posts = body.getData();
        if (posts == null) {
            posts = new JsonArray();
        }

        if (skip <= 0L && pinPostId > 0) {
            posts = dedupePinFirst(posts, pinPostId);
        } else if (skip > 0L && seenPostIds != null && !seenPostIds.isEmpty()) {
            posts = omitSeenPostIds(posts, seenPostIds);
        }

        JsonObject model = new JsonObject();
        model.addProperty("success", true);
        model.add("data", posts);
        if (body.getSkip() != null) {
            model.addProperty("skip", body.getSkip());
        }
        if (body.getLast_post_id() != null) {
            model.addProperty("last_post_id", body.getLast_post_id());
        } else if (posts.size() > 0) {
            try {
                JsonObject last = posts.get(posts.size() - 1).getAsJsonObject();
                if (last.has("post_id")) {
                    model.addProperty("last_post_id", last.get("post_id").getAsLong());
                }
            } catch (Exception ignored) {
            }
        }
        if (body.getActual_skip() != null) {
            model.addProperty("actual_skip", body.getActual_skip());
        } else if (body.getSkip() != null) {
            model.addProperty(
                    "actual_skip",
                    body.getSkip() + posts.size());
        }

        return wrapModelJson(model.toString(), true);
    }

    /**
     * Public edge fast path: plain JSON in {@link EncreptionRequest#getData()} (no encrypt).
     */
    @Nullable
    public static EncreptionRequest toEncreptionRequestPlain(
            @Nullable Context context,
            @Nullable PublicEdgeFeedResponse body,
            long pinPostId,
            long skip,
            @Nullable Set<Long> seenPostIds) {
        if (body == null || !Boolean.TRUE.equals(body.getSuccess())) {
            return null;
        }
        JsonArray posts = body.getData();
        if (posts == null) {
            posts = new JsonArray();
        }

        if (skip <= 0L && pinPostId > 0) {
            posts = dedupePinFirst(posts, pinPostId);
        } else if (skip > 0L && seenPostIds != null && !seenPostIds.isEmpty()) {
            posts = omitSeenPostIds(posts, seenPostIds);
        }

        JsonObject model = new JsonObject();
        model.addProperty("success", true);
        model.add("data", posts);
        if (body.getSkip() != null) {
            model.addProperty("skip", body.getSkip());
        }
        if (body.getLast_post_id() != null) {
            model.addProperty("last_post_id", body.getLast_post_id());
        } else if (posts.size() > 0) {
            try {
                JsonObject last = posts.get(posts.size() - 1).getAsJsonObject();
                if (last.has("post_id")) {
                    model.addProperty("last_post_id", last.get("post_id").getAsLong());
                }
            } catch (Exception ignored) {
            }
        }
        if (body.getActual_skip() != null) {
            model.addProperty("actual_skip", body.getActual_skip());
        } else if (body.getSkip() != null) {
            model.addProperty(
                    "actual_skip",
                    body.getSkip() + posts.size());
        }
        return wrapModelJson(model.toString(), false);
    }

    @Nullable
    private static EncreptionRequest wrapModelJson(String modelJson, boolean encryptPayload) {
        try {
            String payload =
                    encryptPayload ? encrypt(modelJson) : modelJson;
            if (TextUtils.isEmpty(payload)) return null;
            EncreptionRequest enc = new EncreptionRequest();
            enc.setData(payload);
            enc.setPublicEdgePlainPayload(!encryptPayload);
            enc.setShow_ads(true);
            return enc;
        } catch (Exception e) {
            Logone.w(e, TAG + " wrap payload failed");
            return null;
        }
    }

    @NonNull
    private static JsonArray dedupePinFirst(@NonNull JsonArray posts, long pinPostId) {
        JsonArray out = new JsonArray();
        JsonElement pinned = null;
        Set<Long> seen = new HashSet<>();
        for (JsonElement el : posts) {
            if (el == null || !el.isJsonObject()) continue;
            JsonObject obj = el.getAsJsonObject();
            long id = 0L;
            try {
                if (obj.has("post_id") && !obj.get("post_id").isJsonNull()) {
                    id = obj.get("post_id").getAsLong();
                }
            } catch (Exception ignored) {
            }
            if (id == pinPostId) {
                if (pinned == null) pinned = obj;
                continue;
            }
            if (id > 0 && !seen.add(id)) continue;
            out.add(obj);
        }
        JsonArray merged = new JsonArray();
        if (pinned != null) {
            merged.add(pinned);
            seen.add(pinPostId);
        }
        for (JsonElement el : out) {
            merged.add(el);
        }
        return merged;
    }

    @NonNull
    private static JsonArray omitSeenPostIds(
            @NonNull JsonArray posts, @NonNull Set<Long> seenPostIds) {
        JsonArray out = new JsonArray();
        for (JsonElement el : posts) {
            if (el == null || !el.isJsonObject()) continue;
            JsonObject obj = el.getAsJsonObject();
            long id = 0L;
            try {
                if (obj.has("post_id") && !obj.get("post_id").isJsonNull()) {
                    id = obj.get("post_id").getAsLong();
                }
            } catch (Exception ignored) {
            }
            if (id > 0 && seenPostIds.contains(id)) {
                continue;
            }
            out.add(obj);
        }
        return out;
    }

    /**
     * Execute public feed synchronously on a background thread caller.
     * Prefer enqueue via Retrofit in UI code; this is for simple prefetch.
     */
    @Nullable
    public static EncreptionRequest fetchBlocking(
            @NonNull Context context,
            int categoryId,
            long skip,
            long pinPostId) {
        Call<PublicEdgeFeedResponse> call =
                buildPublicFeedCall(context, categoryId, skip, pinPostId, false, false);
        if (call == null) return null;
        try {
            Response<PublicEdgeFeedResponse> response = call.execute();
            if (response == null || !response.isSuccessful()) return null;
            PublicEdgeFeedResponse body = response.body();
            // Page-0 blocking prefetch: empty CF body is a miss.
            if (!isAcceptableEdgeResponse(body, skip)) return null;
            return toEncreptionRequest(context, body, pinPostId, skip, null);
        } catch (Exception e) {
            Logone.w(e, TAG + " fetchBlocking failed");
            return null;
        }
    }

    /**
     * Fire-and-forget Admin Edge/DB watch beacon (new APK only). Never blocks UI.
     *
     * @param reason {@code client_main_local_list_fallback} (Main/Local/push List
     *               after CF miss) or {@code client_push_cf_fail_no_list} (push CF
     *               miss beacon — monitoring only; caller still falls to List)
     */
    /** Monitoring beacons: Main uses 0,0; Local uses saved state/district. */
    public static int edgeBeaconStateId(@NonNull Context context, int categoryId) {
        if (categoryId == -1) {
            return SharedPrefsData.getInt(
                    context, Constants.StateID, Constants.PREF_NAME);
        }
        return 0;
    }

    public static int edgeBeaconDistId(@NonNull Context context, int categoryId) {
        if (categoryId == -1) {
            return SharedPrefsData.getInt(
                    context, Constants.Selected_Distric_Id, Constants.PREF_NAME);
        }
        return 0;
    }

    public static void reportEdgeOriginBeacon(
            @Nullable Context context,
            @NonNull String reason,
            long postId,
            int languageId,
            int stateId,
            int distId) {
        if (context == null) return;
        try {
            String detail;
            String endpoint;
            if ("client_main_local_list_fallback".equals(reason)) {
                detail = "list_fallback_after_cf_miss";
                endpoint = "/app/v3/List_All_Articles";
                // Coalesce with OkHttp interceptor observing the soft List POST.
                EdgeOriginCallWatch.annotateNextUnexpected(
                        "list_all_articles", detail);
            } else if ("client_push_cf_fail_no_list".equals(reason)) {
                // Keep reason key for Admin Edge/DB watch; origin List still runs.
                detail = "push_cf_miss_list_fallback";
                endpoint = "/app/v3/public/feed";
            } else {
                detail = reason;
                endpoint = "/app/v3/public/feed";
            }
            EdgeOriginCallWatch.report(
                    reason,
                    endpoint,
                    postId,
                    languageId,
                    stateId,
                    distId,
                    detail);
        } catch (Exception e) {
            Logone.w(e, TAG + " edge beacon failed");
        }
    }
}
