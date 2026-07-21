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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Tracks app foreground sessions and posts open/close to /app/v3/session.
 * Also exposes the active session_id for per-post dwell events.
 *
 * Close must survive process pause: persist pending close, flush with a
 * blocking call on a background thread, and retry on next open (before open).
 */
public final class SessionAnalyticsTracker {

    private static final String PREF_APP_SESSION_ID = "app_session_analytics_id";
    private static final String PREF_PENDING_CLOSE = "app_session_pending_close_json";
    private static final long CLOSE_DELAY_MS = 400L;

    private static final SessionAnalyticsTracker INSTANCE = new SessionAnalyticsTracker();
    private static final ExecutorService NET_EXEC = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "session-analytics-net");
        t.setPriority(Thread.NORM_PRIORITY);
        return t;
    });

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
                        try {
                            PostDwellTracker.get().flush(appCtx);
                        } catch (Exception ignored) {
                        }
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
        try {
            PostDwellTracker.get().flush(context.getApplicationContext());
        } catch (Exception ignored) {
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

        final String sid = sessionId;
        final long opened = openAtMs;
        // Serialize: pending close (if any) then open, on one worker thread.
        NET_EXEC.execute(() -> {
            flushPendingCloseBlocking(context);
            JSONObject body = buildSessionBody(context, "open", sid, opened, 0L, 0L);
            if (body != null) {
                executeSessionCall(context, body, false);
            }
        });
    }

    private void closeSession(Context context) {
        if (!sessionOpen || sessionId == null) return;
        long closeAt = System.currentTimeMillis();
        long duration = Math.max(0L, closeAt - openAtMs);
        String sid = sessionId;
        long opened = openAtMs;
        sessionOpen = false;
        sessionId = null;
        openAtMs = 0L;
        SharedPrefsData.putString(context, PREF_APP_SESSION_ID, "", Constants.PREF_NAME);
        // Persist first so a killed process can still close on next launch.
        persistPendingClose(context, sid, opened, closeAt, duration);
        final JSONObject body = buildSessionBody(context, "close", sid, opened, closeAt, duration);
        if (body == null) return;
        NET_EXEC.execute(() -> executeSessionCall(context, body, true));
    }

    private void persistPendingClose(Context context, String sid, long openAt, long closeAt, long durationMs) {
        try {
            JSONObject body = buildSessionBody(context, "close", sid, openAt, closeAt, durationMs);
            if (body == null) return;
            SharedPrefsData.putString(context, PREF_PENDING_CLOSE, body.toString(), Constants.PREF_NAME);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearPendingClose(Context context) {
        SharedPrefsData.putString(context, PREF_PENDING_CLOSE, "", Constants.PREF_NAME);
    }

    /** Must run on NET_EXEC. */
    private void flushPendingCloseBlocking(Context context) {
        try {
            String raw = SharedPrefsData.getString(context, PREF_PENDING_CLOSE, Constants.PREF_NAME);
            if (raw == null || raw.trim().isEmpty()) return;
            JSONObject body = new JSONObject(raw);
            String sid = body.optString("session_id", "");
            if (sid.isEmpty()) {
                clearPendingClose(context);
                return;
            }
            executeSessionCall(context, body, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Nullable
    private JSONObject buildSessionBody(
            Context context, String action, String sid, long openAt, long closeAt, long durationMs) {
        try {
            int draftId = SharedPrefsData.getInt(context, Constants.DraftID, Constants.PREF_NAME);
            if (draftId <= 0) return null;

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
            return body;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /** Must run on NET_EXEC for critical paths. */
    private void executeSessionCall(Context context, JSONObject body, boolean clearPendingOnSuccess) {
        try {
            String encrypted = MyUtils.encrypt(body.toString());
            EncritonRequestData enc = new EncritonRequestData();
            enc.setData(encrypted);
            JsonObject payload = new Gson().toJsonTree(enc).getAsJsonObject();

            APIService api = RestApiClient.getClient().create(APIService.class);
            Call<JsonObject> call = api.post_session("v3", payload);
            Response<JsonObject> response = call.execute();
            if (response.isSuccessful() && clearPendingOnSuccess) {
                clearPendingClose(context);
            }
        } catch (Exception e) {
            // Pending close kept in prefs for retry on next open.
            e.printStackTrace();
        }
    }
}
