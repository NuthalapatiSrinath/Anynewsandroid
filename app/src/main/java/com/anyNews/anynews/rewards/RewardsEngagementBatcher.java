package com.anyNews.anynews.rewards;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.DistrictPinVisibilityConfig;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.anyNews.anynews.Utils.WalletUserId;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Batches engagement events (opens, reads, scroll, ads…) and flushes every ~45s
 * or when the buffer is full — avoids per-scroll API spam.
 */
public final class RewardsEngagementBatcher {

    private static final String TAG = "RewardsBatcher";
    private static final long FLUSH_MS = 12_000L;
    private static final int MAX_BUFFER = 36;

    private static final RewardsEngagementBatcher INSTANCE = new RewardsEngagementBatcher();

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final List<JsonObject> buffer = new ArrayList<>();
    private final Runnable flushRunnable = this::flushInternal;
    private boolean flushScheduled;
    @Nullable private Context appContext;

    public static RewardsEngagementBatcher get() {
        return INSTANCE;
    }

    public void ensure(@Nullable Context context) {
        if (context != null) appContext = context.getApplicationContext();
    }

    public void trackOpen(@Nullable Context ctx, @Nullable String postId) {
        enqueue(ctx, "unique_open", postId, 0, 0, 1);
    }

    public void trackQualityRead(
            @Nullable Context ctx, @Nullable String postId, int readSeconds) {
        if (readSeconds < 10) return;
        enqueue(ctx, "quality_read", postId, readSeconds, 0, 1);
    }

    public void trackScrollDepth(
            @Nullable Context ctx, @Nullable String postId, int scrollPct) {
        if (scrollPct < 40) return;
        enqueue(ctx, "scroll_depth", postId, 0, scrollPct, 1);
    }

    public void trackActiveDay(@Nullable Context ctx) {
        enqueue(ctx, "active_day", null, 0, 0, 1);
    }

    public void trackComment(@Nullable Context ctx, @Nullable String postId) {
        enqueue(ctx, "comment", postId, 0, 0, 1);
    }

    public void trackShare(@Nullable Context ctx, @Nullable String postId) {
        enqueue(ctx, "share", postId, 0, 0, 1);
    }

    public void trackFollow(@Nullable Context ctx) {
        enqueue(ctx, "follow", null, 0, 0, 1);
    }

    public void trackAdsSeen(@Nullable Context ctx, int count) {
        if (count <= 0) return;
        enqueue(ctx, "ads_seen", null, 0, 0, Math.min(5, count));
    }

    public void flushNow(@Nullable Context ctx) {
        ensure(ctx);
        flushInternal();
    }

    private void enqueue(
            @Nullable Context ctx,
            String factor,
            @Nullable String postId,
            int readSeconds,
            int scrollPct,
            int count) {
        ensure(ctx);
        if (appContext == null) return;
        if (!DistrictPinVisibilityConfig.rewardsMasterEnabled(appContext)) return;
        if (!SharedPrefsData.getBool(appContext, Constants.is_login, Constants.PREF_NAME)) {
            return;
        }
        if (!RewardsConfigHelper.isJoined(appContext)) return;

        JsonObject evt = new JsonObject();
        evt.addProperty("factor", factor);
        if (!TextUtils.isEmpty(postId)) evt.addProperty("post_id", postId);
        if (readSeconds > 0) evt.addProperty("read_seconds", readSeconds);
        if (scrollPct > 0) evt.addProperty("scroll_pct", scrollPct);
        if (count > 1) evt.addProperty("count", count);

        synchronized (buffer) {
            buffer.add(evt);
            if (buffer.size() >= MAX_BUFFER) {
                handler.removeCallbacks(flushRunnable);
                flushScheduled = false;
                flushInternal();
                return;
            }
            if (!flushScheduled) {
                flushScheduled = true;
                handler.postDelayed(flushRunnable, FLUSH_MS);
            }
        }
    }

    private void flushInternal() {
        flushScheduled = false;
        if (appContext == null) return;
        final List<JsonObject> batch;
        synchronized (buffer) {
            if (buffer.isEmpty()) return;
            batch = new ArrayList<>(buffer);
            buffer.clear();
        }

        String userId;
        String session;
        int draftId;
        long numericId = 0L;
        try {
            numericId = WalletUserId.resolve(appContext);
        } catch (Exception ignored) {
        }
        if (numericId <= 0) {
            try {
                numericId =
                        SharedPrefsData.getLong(appContext, Constants.USER_ID, Constants.PREF_NAME);
            } catch (Exception ignored) {
            }
        }
        if (numericId <= 0) return;
        userId = String.valueOf(numericId);
        try {
            draftId = SharedPrefsData.getInt(appContext, Constants.DraftID, Constants.PREF_NAME);
        } catch (Exception e) {
            draftId = 0;
        }
        try {
            session =
                    SharedPrefsData.getString(
                            appContext, Constants.USER_SESSION_ID, Constants.PREF_NAME);
            if (TextUtils.isEmpty(session)) {
                session =
                        SharedPrefsData.getString(
                                appContext, Constants.SessionID, Constants.PREF_NAME);
            }
        } catch (Exception e) {
            session = "";
        }

        JsonObject body = new JsonObject();
        body.addProperty("USERID", userId);
        body.addProperty("user_id", numericId);
        if (draftId > 0) body.addProperty("draft_id", draftId);
        if (!TextUtils.isEmpty(session)) {
            body.addProperty("SessionID", session);
            body.addProperty("user_session_id", session);
        }
        JsonArray events = new JsonArray();
        for (JsonObject e : batch) events.add(e);
        body.add("events", events);

        try {
            APIService api = RestApiClient.getClient().create(APIService.class);
            api.rewards_events_batch("v3", body).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    // soft — ignore body
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Log.d(TAG, "flush failed soft: " + (t != null ? t.getMessage() : ""));
                }
            });
        } catch (Exception e) {
            Log.d(TAG, "flush error: " + e.getMessage());
        }
    }
}
