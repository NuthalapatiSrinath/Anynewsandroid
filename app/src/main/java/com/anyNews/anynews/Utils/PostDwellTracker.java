package com.anyNews.anynews.Utils;

import android.content.Context;

import androidx.annotation.Nullable;

import com.anyNews.anynews.Pojo.EncritonRequestData;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Per-post dwell timer. Start when an article is shown; flush seconds when
 * leaving (flip away / pause / background). Sends duration via POST /app/v3/views.
 *
 * Background flushes use a blocking call on a worker thread so the request is
 * not dropped when the process is paused after fire-and-forget enqueue.
 */
public final class PostDwellTracker {

    private static final PostDwellTracker INSTANCE = new PostDwellTracker();
    private static final ExecutorService NET_EXEC = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "post-dwell-net");
        t.setPriority(Thread.NORM_PRIORITY);
        return t;
    });

    private long postId = 0L;
    private long startedAtMs = 0L;
    @Nullable private String postType;

    private PostDwellTracker() {}

    public static PostDwellTracker get() {
        return INSTANCE;
    }

    public synchronized void onPostShown(Context context, long newPostId, @Nullable String type) {
        if (newPostId <= 0) return;
        // Skip non-article surfaces
        if (type != null) {
            String t = type.toLowerCase();
            if (t.equals("ad") || t.equals("share_app") || t.equals("ratings")
                    || t.equals("web_ads") || t.equals("web")) {
                flush(context, false);
                postId = 0L;
                startedAtMs = 0L;
                postType = null;
                return;
            }
        }
        if (postId > 0 && postId != newPostId) {
            flush(context, false);
        }
        if (postId != newPostId || startedAtMs <= 0) {
            postId = newPostId;
            postType = type;
            startedAtMs = System.currentTimeMillis();
        }
    }

    public synchronized void onPaused(Context context) {
        flush(context, true);
        // Keep postId so resume can restart the same article timer
        startedAtMs = 0L;
    }

    /** Screen off / session close — flush critical and pause timer (same as pause). */
    public synchronized void onScreenOff(Context context) {
        flush(context, true);
        startedAtMs = 0L;
    }

    public synchronized void onResumed(Context context) {
        // Only resume dwell while a session is active (foreground + screen on).
        if (SessionAnalyticsTracker.get().getActiveSessionId() == null) return;
        if (postId > 0 && startedAtMs <= 0) {
            startedAtMs = System.currentTimeMillis();
        }
    }

    public synchronized void flush(Context context) {
        flush(context, true);
    }

    public synchronized void flush(Context context, boolean critical) {
        if (postId <= 0 || startedAtMs <= 0) return;
        long elapsedSec = Math.max(0L, (System.currentTimeMillis() - startedAtMs) / 1000L);
        long pid = postId;
        String type = postType;
        startedAtMs = 0L;
        // Minimum 1s if they actually saw it briefly (avoids noise of 0)
        if (elapsedSec < 1) elapsedSec = 1;
        sendDwell(context.getApplicationContext(), pid, elapsedSec, type, critical);
    }

    private void sendDwell(
            Context context, long pid, long durationSeconds, @Nullable String type, boolean critical) {
        try {
            int draftId = SharedPrefsData.getInt(context, Constants.DraftID, Constants.PREF_NAME);
            if (draftId <= 0) return;

            JSONObject body = new JSONObject();
            body.put("draft_id", draftId);
            body.put("post_id", pid);
            body.put("duration_seconds", durationSeconds);
            if (type != null) body.put("post_type", type);
            String sessionId = SessionAnalyticsTracker.get().getActiveSessionId();
            if (sessionId != null) body.put("session_id", sessionId);

            String encrypted = MyUtils.encrypt(body.toString());
            EncritonRequestData enc = new EncritonRequestData();
            enc.setData(encrypted);
            JsonObject payload = new Gson().toJsonTree(enc).getAsJsonObject();

            APIService api = RestApiClient.getClient().create(APIService.class);
            Call<JsonObject> call = api.get_views("v3", payload);

            if (critical) {
                NET_EXEC.execute(() -> {
                    try {
                        call.execute();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } else {
                call.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {}

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {}
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
