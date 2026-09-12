package com.anyNews.anynews.rewards;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Utils.AnalyticsBatchConfig;
import com.anyNews.anynews.Utils.BookmarkStore;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.DistrictPinVisibilityConfig;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.anyNews.anynews.Utils.WalletUserId;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Batches engagement events (opens, reads, scroll, ads…) and flushes on an
 * admin-tunable interval ({@code rewards_engagement_flush_interval_seconds},
 * default 45s) only while Home is resumed and the process is foreground.
 *
 * <p>The delayed tick may run, but {@code rewards_events_batch} is posted only
 * when the local queue is non-empty and the device is online. An idle window
 * does not hit the network. Offline keeps the queue and retries on the next
 * tick while the user is still in the app. Background / leave-Home sends at
 * most one flush of pending events, then stops — an empty queue does not call.
 *
 * <p>Hard gate: rewards master OFF or user not joined → never enqueue and never
 * call {@code rewards_events_batch}.
 */
public final class RewardsEngagementBatcher {

    private static final String TAG = "RewardsBatcher";
    private static final int MAX_BUFFER = 36;

    private static final RewardsEngagementBatcher INSTANCE = new RewardsEngagementBatcher();

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final List<JsonObject> buffer = new ArrayList<>();
    private final Runnable flushRunnable = this::flushInternal;
    private final AtomicBoolean lifecycleHooked = new AtomicBoolean(false);
    private boolean flushScheduled;
    private boolean homeActive;
    private boolean processForeground = true;
    @Nullable private Context appContext;

    public static RewardsEngagementBatcher get() {
        return INSTANCE;
    }

    public void ensure(@Nullable Context context) {
        if (context != null) {
            appContext = context.getApplicationContext();
            AnalyticsBatchConfig.warmFromPrefs(appContext);
            hookProcessLifecycleOnce();
        }
    }

    /** Home onResume → true; Home onPause → false (one flush if the queue changed). */
    public void setHomeFlushActive(boolean active) {
        homeActive = active;
        if (!active) {
            // Leave Home: one delivery of pending events, then stop. Empty = no HTTP.
            if (pendingCount() > 0) {
                flushInternal();
            }
            cancelScheduledFlush();
            return;
        }
        if (!processForeground || pendingCount() == 0) return;
        // Back on Home with a held queue (offline / off-Home). Send only if online.
        if (isOnline(appContext)) {
            cancelScheduledFlush();
            flushInternal();
        } else {
            scheduleFlushIfUsingApp();
        }
    }

    private void hookProcessLifecycleOnce() {
        if (!lifecycleHooked.compareAndSet(false, true)) return;
        try {
            ProcessLifecycleOwner.get()
                    .getLifecycle()
                    .addObserver(
                            new LifecycleObserver() {
                                @OnLifecycleEvent(Lifecycle.Event.ON_START)
                                public void onAppForeground() {
                                    processForeground = true;
                                    // One delivery of a held queue. Idle return = no HTTP, no timer.
                                    if (pendingCount() == 0) return;
                                    if (isOnline(appContext)) {
                                        flushInternal();
                                    } else if (homeActive) {
                                        scheduleFlushIfUsingApp();
                                    }
                                }

                                @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
                                public void onAppBackground() {
                                    processForeground = false;
                                    // One flush of pending events, then stop. Empty queue = no call.
                                    if (pendingCount() > 0) {
                                        flushInternal();
                                    }
                                    cancelScheduledFlush();
                                }
                            });
        } catch (Exception ignored) {
        }
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

    /**
     * No-op. Feed impressions must not enqueue {@code ads_seen}. Rewarded
     * video completions are counted on the existing watch-ad claim call.
     */
    public void trackAdsSeen(@Nullable Context ctx, int count) {}

    public void flushNow(@Nullable Context ctx) {
        ensure(ctx);
        flushInternal();
    }

    /** True when rewards master ON, logged in, and joined. */
    private boolean isEngagementEligible(@Nullable Context ctx) {
        Context c = ctx != null ? ctx.getApplicationContext() : appContext;
        if (c == null) return false;
        if (!DistrictPinVisibilityConfig.rewardsMasterEnabled(c)) return false;
        if (!SharedPrefsData.getBool(c, Constants.is_login, Constants.PREF_NAME)) return false;
        return RewardsConfigHelper.isJoined(c);
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
        if (!isEngagementEligible(appContext)) {
            // Drop any stale buffer if feature toggled off / user left rewards.
            clearBufferQuietly();
            return;
        }
        // Daily cap already reached — do not post another scored event.
        if (RewardsConfigHelper.isFactorCapped(appContext, factor)) {
            return;
        }

        JsonObject evt = new JsonObject();
        evt.addProperty("factor", factor);
        if (!TextUtils.isEmpty(postId)) evt.addProperty("post_id", postId);
        if (readSeconds > 0) evt.addProperty("read_seconds", readSeconds);
        if (scrollPct > 0) evt.addProperty("scroll_pct", scrollPct);
        if (count > 1) evt.addProperty("count", count);
        RewardsConfigHelper.noteFactorQueued(appContext, factor, Math.max(1, count));

        synchronized (buffer) {
            buffer.add(evt);
            if (buffer.size() >= MAX_BUFFER) {
                cancelScheduledFlush();
                flushInternal();
                return;
            }
            // Delayed flush only while the user is on Home in the foreground.
            // Off Home / background: leave-Home and ON_STOP deliver once if queued.
            scheduleFlushIfUsingApp();
        }
    }

    private int pendingCount() {
        synchronized (buffer) {
            return buffer.size();
        }
    }

    private boolean isOnline(@Nullable Context ctx) {
        Context c = ctx != null ? ctx : appContext;
        if (c == null) return false;
        try {
            return BookmarkStore.isNetworkAvailable(c);
        } catch (Exception ignored) {
            return false;
        }
    }

    /** Interval tick only while Home is resumed and the process is foreground. */
    private void scheduleFlushIfUsingApp() {
        if (!homeActive || !processForeground || flushScheduled) return;
        if (appContext == null || pendingCount() == 0) return;
        flushScheduled = true;
        long delay = AnalyticsBatchConfig.getRewardsFlushIntervalMs(appContext);
        handler.postDelayed(flushRunnable, delay);
    }

    private void requeue(@Nullable List<JsonObject> batch) {
        if (batch == null || batch.isEmpty()) return;
        synchronized (buffer) {
            buffer.addAll(0, batch);
            while (buffer.size() > MAX_BUFFER) {
                buffer.remove(buffer.size() - 1);
            }
        }
    }

    private void cancelScheduledFlush() {
        handler.removeCallbacks(flushRunnable);
        flushScheduled = false;
    }

    private void clearBufferQuietly() {
        synchronized (buffer) {
            buffer.clear();
        }
        cancelScheduledFlush();
    }

    private void flushInternal() {
        flushScheduled = false;
        if (appContext == null) return;
        // Hard stop: never POST when rewards OFF / not joined.
        if (!isEngagementEligible(appContext)) {
            clearBufferQuietly();
            return;
        }
        // Score/queue unchanged — no HTTP, and do not keep a heartbeat running.
        if (pendingCount() == 0) return;
        // Offline: keep the queue. Retry only while the user is still in the app.
        if (!isOnline(appContext)) {
            scheduleFlushIfUsingApp();
            return;
        }
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
        if (numericId <= 0) {
            // Cannot address the user yet — keep the queue, do not POST.
            requeue(batch);
            scheduleFlushIfUsingApp();
            return;
        }
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

        if (events.size() == 0) return;
        try {
            APIService api = RestApiClient.getClient().create(APIService.class);
            api.rewards_events_batch("v3", body).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    try {
                        RewardsConfigHelper.applyFromRewardsPayload(
                                appContext, response != null ? response.body() : null);
                    } catch (Exception ignored) {
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Log.d(TAG, "flush failed soft: " + (t != null ? t.getMessage() : ""));
                }
            });
        } catch (Exception e) {
            // Request never started — keep the queue for the next online window.
            requeue(batch);
            scheduleFlushIfUsingApp();
            Log.d(TAG, "flush error: " + e.getMessage());
        }
    }
}
