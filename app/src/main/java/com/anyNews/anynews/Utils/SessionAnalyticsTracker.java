package com.anyNews.anynews.Utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;

import com.anyNews.anynews.BuildConfig;
import com.anyNews.anynews.Pojo.EncritonRequestData;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Tracks app foreground sessions and posts open/close to /app/v3/session.
 * Also exposes the active session_id for per-post dwell events.
 */
public final class SessionAnalyticsTracker {

    private static final String PREF_APP_SESSION_ID = "app_session_analytics_id";
    private static final long CLOSE_DELAY_MS = 700L;

    private static final SessionAnalyticsTracker INSTANCE = new SessionAnalyticsTracker();

    private int startedCount = 0;
    private long openAtMs = 0L;
    @Nullable private String sessionId;
    private boolean sessionOpen = false;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    @Nullable private Runnable pendingClose;

    private SessionAnalyticsTracker() {}

    public static SessionAnalyticsTracker get() {
        return INSTANCE;
    }

    @Nullable
    public String getActiveSessionId() {
        return sessionOpen ? sessionId : null;
    }

    public synchronized void onActivityStarted(Context context) {
        if (pendingClose != null) {
            mainHandler.removeCallbacks(pendingClose);
            pendingClose = null;
        }
        startedCount++;
        if (startedCount == 1 && !sessionOpen) {
            openSession(context.getApplicationContext());
        }
    }

    public synchronized void onActivityStopped(Context context) {
        startedCount = Math.max(0, startedCount - 1);
        if (startedCount == 0 && sessionOpen) {
            final Context appCtx = context.getApplicationContext();
            pendingClose = () -> {
                synchronized (SessionAnalyticsTracker.this) {
                    if (startedCount == 0 && sessionOpen) {
                        closeSession(appCtx);
                    }
                    pendingClose = null;
                }
            };
            mainHandler.postDelayed(pendingClose, CLOSE_DELAY_MS);
        }
    }

    /** Flush dwell + session when process is about to die. */
    public synchronized void onAppTerminate(Context context) {
        if (pendingClose != null) {
            mainHandler.removeCallbacks(pendingClose);
            pendingClose = null;
        }
        if (sessionOpen) {
            closeSession(context.getApplicationContext());
        }
    }

    private void openSession(Context context) {
        sessionId = UUID.randomUUID().toString();
        openAtMs = System.currentTimeMillis();
        sessionOpen = true;
        SharedPrefsData.putString(context, PREF_APP_SESSION_ID, sessionId, Constants.PREF_NAME);
        postSession(context, "open", sessionId, openAtMs, 0L, 0L);
    }

    private void closeSession(Context context) {
        if (!sessionOpen || sessionId == null) return;
        long closeAt = System.currentTimeMillis();
        long duration = Math.max(0L, closeAt - openAtMs);
        String sid = sessionId;
        long opened = openAtMs;
        sessionOpen = false;
        postSession(context, "close", sid, opened, closeAt, duration);
        sessionId = null;
        openAtMs = 0L;
    }

    private void postSession(Context context, String action, String sid, long openAt, long closeAt, long durationMs) {
        try {
            int draftId = SharedPrefsData.getInt(context, Constants.DraftID, Constants.PREF_NAME);
            if (draftId <= 0) return;

            JSONObject body = new JSONObject();
            body.put("action", action);
            body.put("session_id", sid);
            body.put("draft_id", draftId);
            body.put("device_id", SharedPrefsData.getString(context, Constants.DeviceID, Constants.PREF_NAME));
            body.put("language_id", SharedPrefsData.getInt(context, Constants.LanguageID, Constants.PREF_NAME));
            body.put("app_version", BuildConfig.VERSION_NAME);
            body.put("open_at", openAt);
            if (closeAt > 0) body.put("close_at", closeAt);
            if (durationMs > 0) body.put("duration_ms", durationMs);

            String encrypted = MyUtils.encrypt(body.toString());
            EncritonRequestData enc = new EncritonRequestData();
            enc.setData(encrypted);
            JsonObject payload = new Gson().toJsonTree(enc).getAsJsonObject();

            APIService api = RestApiClient.getClient().create(APIService.class);
            api.post_session("v3", payload).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    // fire-and-forget
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    // best-effort; next open will start a new session
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
