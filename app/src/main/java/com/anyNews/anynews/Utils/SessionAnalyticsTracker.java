package com.anyNews.anynews.Utils;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;

import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.anyNews.anynews.BuildConfig;
import com.anyNews.anynews.Pojo.EncritonRequestData;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.CommonUtilities;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Services.SessionFlushService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Tracks app foreground sessions and posts open/close to /app/v3/session.
 *
 * Time counts only while the process is in the foreground AND the screen is on
 * (interactive). Sources:
 * - ProcessLifecycleOwner ON_START / ON_STOP
 * - ActivityLifecycleCallbacks (start/stop/destroy + userLeaveHint)
 * - ACTION_SCREEN_OFF → close immediately
 * - ACTION_SCREEN_ON / USER_PRESENT → reopen only if still foreground + interactive
 * - SessionFlushService.onTaskRemoved → Recents swipe last-chance flush
 *
 * Close persists to SharedPrefs and flushes on a worker thread (never blocks the
 * main thread — Lifecycle/SCREEN_OFF would ANR). Critical close paths use a short
 * OkHttp timeout + SessionFlushService so swipe-kill can still POST. Heartbeats
 * stop on background. Pending prefs + server stale auto-close cover residual loss.
 */
public final class SessionAnalyticsTracker {

    private static final String PREF_APP_SESSION_ID = "app_session_analytics_id";
    private static final String PREF_PENDING_CLOSE = "app_session_pending_close_json";
    /** Short grace only for activity-to-activity transitions (not screen/process). */
    private static final long ACTIVITY_CLOSE_DELAY_MS = 300L;
    private static final long HEARTBEAT_INTERVAL_MS = 45_000L;
    private static final long CLOSE_FLUSH_WAIT_MS = 2_500L;
    private static final long CLOSE_HTTP_TIMEOUT_SEC = 3L;

    private static final SessionAnalyticsTracker INSTANCE = new SessionAnalyticsTracker();
    private static final ExecutorService NET_EXEC = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "session-analytics-net");
        t.setPriority(Thread.NORM_PRIORITY + 1);
        return t;
    });

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final AtomicBoolean watchServiceStarted = new AtomicBoolean(false);

    private int startedCount = 0;
    private boolean processForeground = false;
    private boolean screenOn = true;
    /** After SCREEN_OFF, wait for USER_PRESENT (or fresh ON_START) before counting again. */
    private boolean awaitingUnlock = false;
    private long openAtMs = 0L;
    @Nullable private String sessionId;
    private boolean sessionOpen = false;
    @Nullable private Runnable pendingActivityClose;
    @Nullable private Runnable heartbeatRunnable;
    @Nullable private BroadcastReceiver screenReceiver;
    @Nullable private Context appContext;
    @Nullable private volatile Retrofit fastCloseRetrofit;

    private SessionAnalyticsTracker() {}

    public static SessionAnalyticsTracker get() {
        return INSTANCE;
    }

    /**
     * Call once from Application.onCreate. Registers ProcessLifecycleOwner + screen receiver.
     */
    public void init(Application application) {
        if (!initialized.compareAndSet(false, true)) return;
        appContext = application.getApplicationContext();
        screenOn = isDeviceInteractive(appContext);

        ProcessLifecycleOwner.get().getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_START)
            public void onAppForeground() {
                onMoveToForeground(appContext);
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
            public void onAppBackground() {
                onMoveToBackground(appContext);
            }
        });

        screenReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent == null || intent.getAction() == null) return;
                String action = intent.getAction();
                Context ctx = context.getApplicationContext();
                if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                    onScreenOff(ctx);
                } else if (Intent.ACTION_SCREEN_ON.equals(action)) {
                    onScreenOn(ctx);
                } else if (Intent.ACTION_USER_PRESENT.equals(action)) {
                    onUserPresent(ctx);
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            application.registerReceiver(screenReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            application.registerReceiver(screenReceiver, filter);
        }

        // Flush any close that never reached the server on a prior kill.
        NET_EXEC.execute(() -> flushPendingCloseBlocking(appContext));
    }

    @Nullable
    public String getActiveSessionId() {
        return sessionOpen ? sessionId : null;
    }

    /** ProcessLifecycleOwner ON_START — app moved to foreground. */
    public synchronized void onMoveToForeground(Context context) {
        processForeground = true;
        awaitingUnlock = false;
        cancelActivityClose();
        refreshScreenFromDevice(context);
        evaluateTracking(context.getApplicationContext(), false);
    }

    /** ProcessLifecycleOwner ON_STOP — app fully backgrounded; close immediately. */
    public synchronized void onMoveToBackground(Context context) {
        processForeground = false;
        awaitingUnlock = false;
        cancelActivityClose();
        evaluateTracking(context.getApplicationContext(), true);
    }

    /** Power button / screen off while (possibly) still "started". */
    public synchronized void onScreenOff(Context context) {
        screenOn = false;
        awaitingUnlock = true;
        cancelActivityClose();
        evaluateTracking(context.getApplicationContext(), true);
    }

    /**
     * Screen on — do not reopen yet (lock screen must not accrue time).
     * Reopen on USER_PRESENT or ProcessLifecycle ON_START.
     */
    public synchronized void onScreenOn(Context context) {
        // Keep awaitingUnlock; USER_PRESENT / ON_START clears it.
        if (!awaitingUnlock) {
            screenOn = isDeviceInteractive(context);
            evaluateTracking(context.getApplicationContext(), false);
        }
    }

    /** User unlocked — safe to count again if still foreground. */
    public synchronized void onUserPresent(Context context) {
        awaitingUnlock = false;
        screenOn = true;
        evaluateTracking(context.getApplicationContext(), false);
    }

    /** ActivityLifecycleCallbacks backup (config-change grace via short delay). */
    public synchronized void onActivityStarted(Context context) {
        cancelActivityClose();
        startedCount++;
        if (startedCount == 1) {
            processForeground = true;
            refreshScreenFromDevice(context);
            evaluateTracking(context.getApplicationContext(), false);
        }
    }

    public synchronized void onActivityStopped(Context context) {
        startedCount = Math.max(0, startedCount - 1);
        if (startedCount == 0) {
            final Context appCtx = context.getApplicationContext();
            // Short delay so activity→activity transitions do not flicker close/open.
            // ProcessLifecycle / SCREEN_OFF already close immediately and cancel this.
            pendingActivityClose = () -> {
                synchronized (SessionAnalyticsTracker.this) {
                    pendingActivityClose = null;
                    if (startedCount == 0) {
                        processForeground = false;
                        evaluateTracking(appCtx, true);
                    }
                }
            };
            mainHandler.postDelayed(pendingActivityClose, ACTIVITY_CLOSE_DELAY_MS);
        }
    }

    /**
     * Last activity finishing (not config change) — force close without the 300ms grace.
     * Covers Recents swipe when ProcessLifecycle ON_STOP races process death.
     */
    public synchronized void onActivityFinishingDestroyed(Activity activity) {
        if (activity == null) return;
        if (activity.isChangingConfigurations()) return;
        if (!activity.isFinishing()) return;
        cancelActivityClose();
        if (startedCount <= 0 && sessionOpen) {
            processForeground = false;
            stopHeartbeat();
            evaluateTracking(activity.getApplicationContext(), true);
        }
    }

    /**
     * Activity.onUserLeaveHint — user leaving to Recents / another app.
     * Stop heartbeats immediately (so admin cannot climb) and snapshot a pending close
     * for kill recovery. Do NOT POST close yet — user may return from Recents.
     * Real close: ProcessLifecycle ON_STOP / onTaskRemoved / finishing destroy.
     */
    public synchronized void onUserLeaveHint(Context context) {
        if (!sessionOpen || sessionId == null) return;
        if (startedCount > 1) return;
        stopHeartbeat();
        // Snapshot only — flush happens on actual background / task removed.
        long closeAt = System.currentTimeMillis();
        long duration = Math.max(0L, closeAt - openAtMs);
        persistPendingClose(context.getApplicationContext(), sessionId, openAtMs, closeAt, duration);
    }

    /** Recents swipe — Service.onTaskRemoved. Close + blocking flush on worker. */
    public void onTaskRemoved(Context context) {
        final Context appCtx = context.getApplicationContext();
        synchronized (this) {
            cancelActivityClose();
            processForeground = false;
            stopHeartbeat();
            if (sessionOpen) {
                try {
                    PostDwellTracker.get().onScreenOff(appCtx);
                } catch (Exception ignored) {
                }
                try {
                    ViewsBatchTracker.get().flush(appCtx, true);
                } catch (Exception ignored) {
                }
                closeSession(appCtx, false);
            }
        }
        // Block briefly on a non-main thread so the POST can land before kill.
        CountDownLatch done = new CountDownLatch(1);
        NET_EXEC.execute(() -> {
            try {
                flushPendingCloseBlocking(appCtx);
            } finally {
                done.countDown();
            }
        });
        try {
            done.await(CLOSE_FLUSH_WAIT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
        stopWatchService(appCtx);
    }

    /** Called from SessionFlushService ACTION_FLUSH. */
    public void flushPendingCloseFromService(Context context, @Nullable Runnable onDone) {
        NET_EXEC.execute(() -> {
            try {
                flushPendingCloseBlocking(context.getApplicationContext());
            } finally {
                if (onDone != null) {
                    try {
                        onDone.run();
                    } catch (Exception ignored) {
                    }
                }
            }
        });
    }

    /** Activity resumed — reopen only if not waiting for unlock after SCREEN_OFF. */
    public synchronized void onActivityResumed(Context context) {
        if (awaitingUnlock) return;
        refreshScreenFromDevice(context);
        if (processForeground || startedCount > 0) {
            processForeground = true;
            evaluateTracking(context.getApplicationContext(), false);
        }
    }

    /**
     * Call after DraftID is first written (splash). openSession no-ops until then;
     * without this nudge, a cold install would wait until the next lifecycle event.
     */
    public synchronized void onIdentityReady(Context context) {
        if (sessionOpen) return;
        if (!processForeground && startedCount <= 0) return;
        refreshScreenFromDevice(context);
        evaluateTracking(context.getApplicationContext(), false);
    }

    /**
     * Home resume / foreground nudge: if a session is open and GPS throttle allows,
     * POST a heartbeat with last-known lat/lng (non-blocking).
     */
    public void maybeRefreshDeviceLocation(Context context) {
        if (context == null) return;
        final Context appCtx = context.getApplicationContext();
        final String sid;
        final long opened;
        synchronized (this) {
            if (!sessionOpen || sessionId == null) return;
            if (!DeviceLocationHelper.shouldAttachGpsOnHeartbeat(appCtx)) return;
            sid = sessionId;
            opened = openAtMs;
        }
        NET_EXEC.execute(() -> {
            // Force-include GPS by using "open"-style attach via a heartbeat that
            // bypasses throttle only when shouldAttachGpsOnHeartbeat already passed.
            JSONObject body = buildSessionBody(appCtx, "heartbeat", sid, opened, 0L, 0L);
            if (body != null && body.has("latitude")) {
                executeSessionCall(appCtx, body, false, false);
            }
        });
    }

    /** Flush dwell + session when process is about to die. */
    public synchronized void onAppTerminate(Context context) {
        cancelActivityClose();
        stopHeartbeat();
        try {
            PostDwellTracker.get().flush(context.getApplicationContext());
        } catch (Exception ignored) {
        }
        try {
            ViewsBatchTracker.get().flush(context.getApplicationContext(), true);
        } catch (Exception ignored) {
        }
        if (sessionOpen) {
            closeSession(context.getApplicationContext(), true);
        }
        stopWatchService(context.getApplicationContext());
    }

    private void evaluateTracking(Context context, boolean awaitCloseFlush) {
        boolean shouldTrack = processForeground
                && screenOn
                && !awaitingUnlock
                && isDeviceInteractive(context);
        if (shouldTrack) {
            if (!sessionOpen) {
                openSession(context);
            }
            startHeartbeat(context);
        } else {
            stopHeartbeat();
            if (sessionOpen) {
                try {
                    PostDwellTracker.get().onScreenOff(context);
                } catch (Exception ignored) {
                }
                try {
                    ViewsBatchTracker.get().flush(context, true);
                } catch (Exception ignored) {
                }
                closeSession(context, awaitCloseFlush);
            }
        }
    }

    private void cancelActivityClose() {
        if (pendingActivityClose != null) {
            mainHandler.removeCallbacks(pendingActivityClose);
            pendingActivityClose = null;
        }
    }

    private void refreshScreenFromDevice(Context context) {
        screenOn = isDeviceInteractive(context);
    }

    private static boolean isDeviceInteractive(Context context) {
        try {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (pm != null) return pm.isInteractive();
        } catch (Exception ignored) {
        }
        return true;
    }

    private void openSession(Context context) {
        if (sessionOpen) return;
        // Prefer not to mark a local session open until draft_id exists — otherwise
        // fresh installs open during splash (draft still 0), never POST /session/open,
        // and later resumes skip open because sessionOpen is already true.
        int draftId = SharedPrefsData.getInt(context, Constants.DraftID, Constants.PREF_NAME);
        if (draftId <= 0) return;

        sessionId = UUID.randomUUID().toString();
        openAtMs = System.currentTimeMillis();
        sessionOpen = true;
        SharedPrefsData.putString(context, PREF_APP_SESSION_ID, sessionId, Constants.PREF_NAME);
        startWatchService(context);

        final String sid = sessionId;
        final long opened = openAtMs;
        NET_EXEC.execute(() -> {
            flushPendingCloseBlocking(context);
            JSONObject body = buildSessionBody(context, "open", sid, opened, 0L, 0L);
            if (body != null) {
                executeSessionCall(context, body, false, false);
            }
        });
    }

    private void closeSession(Context context, boolean awaitFlush) {
        if (!sessionOpen || sessionId == null) return;
        long closeAt = System.currentTimeMillis();
        long duration = Math.max(0L, closeAt - openAtMs);
        String sid = sessionId;
        long opened = openAtMs;
        sessionOpen = false;
        sessionId = null;
        openAtMs = 0L;
        SharedPrefsData.putString(context, PREF_APP_SESSION_ID, "", Constants.PREF_NAME);
        persistPendingClose(context, sid, opened, closeAt, duration);
        final JSONObject body = buildSessionBody(context, "close", sid, opened, closeAt, duration);
        stopWatchService(context);
        if (body == null) {
            SessionFlushService.enqueueFlush(context);
            return;
        }

        Future<?> future = NET_EXEC.submit(() -> executeSessionCall(context, body, true, true));
        // Also kick the service flush path — survives a bit longer after Recents swipe.
        SessionFlushService.enqueueFlush(context);
        if (awaitFlush) {
            // Never block the main thread: ProcessLifecycle / SCREEN_OFF / onTerminate
            // all run there; future.get(2.5s) is an ANR risk. Pending prefs + next
            // open flush + server stale auto-close cover process death.
            if (Looper.myLooper() == Looper.getMainLooper()) {
                return;
            }
            try {
                future.get(CLOSE_FLUSH_WAIT_MS, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                // Pending close remains in prefs for retry on next open.
            }
        }
    }

    private void startWatchService(Context context) {
        if (watchServiceStarted.compareAndSet(false, true)) {
            SessionFlushService.startWatch(context);
        } else {
            SessionFlushService.startWatch(context);
        }
    }

    private void stopWatchService(Context context) {
        watchServiceStarted.set(false);
        SessionFlushService.stopWatch(context);
    }

    private void startHeartbeat(Context context) {
        if (heartbeatRunnable != null) return;
        final Context appCtx = context.getApplicationContext();
        heartbeatRunnable = new Runnable() {
            @Override
            public void run() {
                synchronized (SessionAnalyticsTracker.this) {
                    if (!sessionOpen || sessionId == null || !processForeground || !screenOn) {
                        heartbeatRunnable = null;
                        return;
                    }
                    final String sid = sessionId;
                    final long opened = openAtMs;
                    NET_EXEC.execute(() -> {
                        JSONObject body = buildSessionBody(appCtx, "heartbeat", sid, opened, 0L, 0L);
                        if (body != null) {
                            executeSessionCall(appCtx, body, false, false);
                        }
                    });
                    mainHandler.postDelayed(this, HEARTBEAT_INTERVAL_MS);
                }
            }
        };
        mainHandler.postDelayed(heartbeatRunnable, HEARTBEAT_INTERVAL_MS);
    }

    private void stopHeartbeat() {
        if (heartbeatRunnable != null) {
            mainHandler.removeCallbacks(heartbeatRunnable);
            heartbeatRunnable = null;
        }
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
            executeSessionCall(context, body, true, true);
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
            if ("close".equals(action)) {
                body.put("duration_ms", Math.max(0L, durationMs));
            } else if (durationMs > 0) {
                body.put("duration_ms", durationMs);
            }
            attachDeviceGps(context, body, action);
            return body;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Attach quiet last-known GPS for device_drafts.
     * Always on open/close; throttled on heartbeat (~3 min).
     */
    private void attachDeviceGps(Context context, JSONObject body, String action) {
        try {
            boolean always = "open".equals(action) || "close".equals(action);
            if (!always && !DeviceLocationHelper.shouldAttachGpsOnHeartbeat(context)) {
                return;
            }
            DeviceLocationHelper.Coords coords = DeviceLocationHelper.readIfPermitted(context);
            if (coords == null) return;
            body.put("latitude", String.valueOf(coords.latitude));
            body.put("longitude", String.valueOf(coords.longitude));
            DeviceLocationHelper.markGpsSent(context);
        } catch (Exception ignored) {
        }
    }

    private APIService sessionApi(boolean fastClose) {
        if (!fastClose) {
            return RestApiClient.getClient().create(APIService.class);
        }
        Retrofit retrofit = fastCloseRetrofit;
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(CLOSE_HTTP_TIMEOUT_SEC, TimeUnit.SECONDS)
                    .readTimeout(CLOSE_HTTP_TIMEOUT_SEC, TimeUnit.SECONDS)
                    .writeTimeout(CLOSE_HTTP_TIMEOUT_SEC, TimeUnit.SECONDS)
                    .callTimeout(CLOSE_HTTP_TIMEOUT_SEC + 1, TimeUnit.SECONDS)
                    .build();
            retrofit = new Retrofit.Builder()
                    .baseUrl(CommonUtilities.URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(new Gson()))
                    .build();
            fastCloseRetrofit = retrofit;
        }
        return retrofit.create(APIService.class);
    }

    /** Must run on NET_EXEC for critical paths. */
    private void executeSessionCall(
            Context context, JSONObject body, boolean clearPendingOnSuccess, boolean fastClose) {
        try {
            String encrypted = MyUtils.encrypt(body.toString());
            EncritonRequestData enc = new EncritonRequestData();
            enc.setData(encrypted);
            JsonObject payload = new Gson().toJsonTree(enc).getAsJsonObject();

            APIService api = sessionApi(fastClose);
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
