package com.anyNews.anynews.Utils;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.anyNews.anynews.Pojo.EncreptionRequest;
import com.anyNews.anynews.Pojo.PublicEdgeFeedResponse;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.anyNews.anynews.Utils.MyUtils.encrypt;

/**
 * Cloudflare-first public feed reads (anonymous GET).
 *
 * Normal national/local flip feeds and push/share pin prefer
 * {@code GET /app/v3/public/feed} (CF HIT → no origin). On CF miss/fail,
 * callers soft-fall to encrypted {@code List_All_Articles} so Node still
 * serves Redis→Mongo. CF is optional; origin must always work. Writes stay POST.
 *
 * Response is wrapped into {@link EncreptionRequest} so existing
 * {@code convertListOfArticles} decrypt path stays unchanged.
 */
public final class PublicEdgeFeedHelper {

    private static final String TAG = "PublicEdgeFeed";

    private PublicEdgeFeedHelper() {
    }

    /**
     * True for main/local chronological feeds that can use the public edge.
     * Search / special / flash / video / category browse stay on POST.
     */
    public static boolean canUsePublicEdge(
            boolean searchMode,
            boolean specialMode,
            boolean videoMode,
            int categoryId) {
        if (searchMode || specialMode || videoMode) return false;
        // category 0 = main national; -1 = local district; other categories need POST.
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
            @NonNull Callback<PublicEdgeFeedResponse> callback) {
        Call<PublicEdgeFeedResponse> call =
                buildPublicFeedCall(context, categoryId, skip, pinPostId);
        if (call == null) {
            callback.onFailure(
                    null, new IOException("public feed call unavailable"));
            return null;
        }
        NetworkCallDeduper.enqueueUnique(
                publicFeedDedupeKey(context, categoryId, skip, pinPostId),
                call,
                callback);
        return call;
    }

    @NonNull
    static String publicFeedDedupeKey(
            @NonNull Context context,
            int categoryId,
            long skip,
            long pinPostId) {
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
        long pin = Math.max(0L, pinPostId);
        return String.format(
                Locale.US,
                "public_feed_v3_%d_%d_%d_%d_%d",
                languageId,
                stateId,
                distId,
                skip,
                pin);
    }

    @Nullable
    public static Call<PublicEdgeFeedResponse> buildPublicFeedCall(
            @NonNull Context context,
            int categoryId,
            long skip,
            long pinPostId) {
        try {
            int languageId = SharedPrefsData.getInt(
                    context, Constants.LanguageID, Constants.PREF_NAME);
            if (languageId <= 0) return null;

            int stateId = 0;
            int distId = 0;
            if (categoryId == -1) {
                stateId = SharedPrefsData.getInt(
                        context, Constants.StateID, Constants.PREF_NAME);
                distId = SharedPrefsData.getInt(
                        context, Constants.Selected_Distric_Id, Constants.PREF_NAME);
                // Local needs geo on the public URL; otherwise soft-fallback to POST.
                if (stateId <= 0 || distId <= 0) {
                    return null;
                }
            }

            APIService api = RestApiClient.getClient().create(APIService.class);
            Integer pin = pinPostId > 0 ? (int) Math.min(pinPostId, Integer.MAX_VALUE) : null;
            return api.Public_Feed(
                    "v3",
                    languageId,
                    stateId > 0 ? stateId : null,
                    distId > 0 ? distId : null,
                    (int) Math.max(0, Math.min(skip, Integer.MAX_VALUE)),
                    pin);
        } catch (Exception e) {
            Logone.w(e, TAG + " build call failed");
            return null;
        }
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
        if (body == null || !Boolean.TRUE.equals(body.getSuccess())) {
            return null;
        }
        JsonArray posts = body.getData();
        if (posts == null) {
            posts = new JsonArray();
        }

        // Share/push pin: ensure first, strip duplicates below.
        if (pinPostId > 0) {
            posts = dedupePinFirst(posts, pinPostId);
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

        try {
            String encrypted = encrypt(model.toString());
            if (TextUtils.isEmpty(encrypted)) return null;
            EncreptionRequest enc = new EncreptionRequest();
            enc.setData(encrypted);
            // Ad seats: client rebalances to FeedAdConfig.FIXED_AD_POSITIONS
            // when show_ads is on (same path as List_All_Articles without ads_data).
            enc.setShow_ads(true);
            return enc;
        } catch (Exception e) {
            Logone.w(e, TAG + " wrap encrypt failed");
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
                buildPublicFeedCall(context, categoryId, skip, pinPostId);
        if (call == null) return null;
        try {
            Response<PublicEdgeFeedResponse> response = call.execute();
            if (response == null || !response.isSuccessful()) return null;
            PublicEdgeFeedResponse body = response.body();
            // Page-0 blocking prefetch: empty CF body is a miss.
            if (!isAcceptableEdgeResponse(body, skip)) return null;
            return toEncreptionRequest(context, body, pinPostId);
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
