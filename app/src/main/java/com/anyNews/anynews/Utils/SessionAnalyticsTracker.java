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
 * True-live presence (admin controlled via splash):
 * - session_live_enabled OFF / heartbeat 0 → open once + close; no mid-session pings
 * - ON → pulse every session_heartbeat_interval_seconds (min 5, default 30) while FG
 *
 * Time counts only while foreground AND screen on (interactive).
 * Close prefs + server stale auto-close cover kill loss without sticky 45-min “live”.
 */
public final class SessionAnalyticsTracker {

    private static final String PREF_APP_SESSION_ID = "app_session_analytics_id";
    private static final String PREF_PENDING_CLOSE = "app_session_pending_close_json";
    private static final long ACTIVITY_CLOSE_DELAY_MS = 300L;
    /** Used only when admin opts into mid-session heartbeat (interval &gt; 0). */
    private static long HEARTBEAT_INTERVAL_MS = 0L;
    private static final long CLOSE_FLUSH_WAIT_MS = 2_500L;
    private static final long CLOSE_HTTP_TIMEOUT_SEC = 3L;
    public static final String ACTION_CRICKET_STATE_CHANGED = "com.anyNews.anynews.ACTION_CRICKET_STATE_CHANGED";

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
     * Snapshot a pending close (with activity rollup) for kill recovery.
     * Do NOT POST close yet — user may return from Recents.
     */
    public synchronized void onUserLeaveHint(Context context) {
        if (!sessionOpen || sessionId == null) return;
        if (startedCount > 1) return;
        stopHeartbeat();
        long closeAt = System.currentTimeMillis();
        long duration = Math.max(0L, closeAt - openAtMs);
        JSONObject activity = peekActivitySummary(context);
        persistPendingClose(context.getApplicationContext(), sessionId, openAtMs, closeAt, duration, activity);
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
                JSONObject activity = peekActivitySummary(appCtx);
                try {
                    ViewsBatchTracker.get().flush(appCtx, true);
                } catch (Exception ignored) {
                }
                closeSession(appCtx, false, activity);
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
     * Home resume nudge: only posts GPS when mid-session heartbeat is admin opt-in.
     * Default open/close path attaches GPS on open/close only.
     */
    public void maybeRefreshDeviceLocation(Context context) {
        if (!CricketSurfaceConfigHelper.isHeartbeatEnabled(context)) return;
        refreshDeviceLocation(context, false);
    }

    /**
     * After soft LOCATION grant: one-shot lat/lng (not a recurring timer).
     * When heartbeat is off, still allows a single force attach if GPS available.
     */
    public void forceRefreshDeviceLocation(Context context) {
        if (context == null) return;
        final Context appCtx = context.getApplicationContext();
        refreshDeviceLocation(appCtx, true);
        if (!CricketSurfaceConfigHelper.isHeartbeatEnabled(appCtx)) return;
        final long[] delaysMs = {2_000L, 6_000L, 15_000L};
        for (long delayMs : delaysMs) {
            new Handler(Looper.getMainLooper()).postDelayed(
                    () -> refreshDeviceLocation(appCtx, true),
                    delayMs);
        }
    }

    private void refreshDeviceLocation(Context context, boolean force) {
        if (context == null) return;
        final Context appCtx = context.getApplicationContext();
        final String sid;
        final long opened;
        synchronized (this) {
            if (!sessionOpen || sessionId == null) return;
            if (!force && !DeviceLocationHelper.shouldAttachGpsOnHeartbeat(appCtx)) return;
            if (!force && !CricketSurfaceConfigHelper.isHeartbeatEnabled(appCtx)) return;
            sid = sessionId;
            opened = openAtMs;
        }
        NET_EXEC.execute(() -> {
            JSONObject body = buildSessionBody(
                    appCtx, force ? "open" : "heartbeat", sid, opened, 0L, 0L, null);
            if (body == null || !body.has("latitude")) return;
            try {
                body.put("action", "heartbeat");
            } catch (Exception ignored) {
            }
            executeSessionCall(appCtx, body, false, false);
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
        JSONObject activity = peekActivitySummary(context);
        try {
            ViewsBatchTracker.get().flush(context.getApplicationContext(), true);
        } catch (Exception ignored) {
        }
        if (sessionOpen) {
            closeSession(context.getApplicationContext(), true, activity);
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
            // Periodic heartbeat only when admin live monitoring ON + interval > 0.
            if (CricketSurfaceConfigHelper.isHeartbeatEnabled(context)) {
                startHeartbeat(context);
            } else {
                stopHeartbeat();
            }
        } else {
            stopHeartbeat();
            if (sessionOpen) {
                try {
                    PostDwellTracker.get().onScreenOff(context);
                } catch (Exception ignored) {
                }
                // Snapshot before views flush drains the buffer.
                JSONObject activity = peekActivitySummary(context);
                try {
                    ViewsBatchTracker.get().flush(context, true);
                } catch (Exception ignored) {
                }
                closeSession(context, awaitCloseFlush, activity);
            }
        }
    }

    @Nullable
    private static JSONObject peekActivitySummary(Context context) {
        try {
            return ViewsBatchTracker.get().snapshotActivitySummary();
        } catch (Exception ignored) {
            return null;
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
            JSONObject body = buildSessionBody(context, "open", sid, opened, 0L, 0L, null);
            if (body != null) {
                executeSessionCall(context, body, false, false);
            }
        });
    }

    private void closeSession(Context context, boolean awaitFlush, @Nullable JSONObject activitySummary) {
        if (!sessionOpen || sessionId == null) return;
        long closeAt = System.currentTimeMillis();
        long duration = Math.max(0L, closeAt - openAtMs);
        String sid = sessionId;
        long opened = openAtMs;
        sessionOpen = false;
        sessionId = null;
        openAtMs = 0L;
        SharedPrefsData.putString(context, PREF_APP_SESSION_ID, "", Constants.PREF_NAME);
        persistPendingClose(context, sid, opened, closeAt, duration, activitySummary);
        final JSONObject body = buildSessionBody(
                context, "close", sid, opened, closeAt, duration, activitySummary);
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
        if (!CricketSurfaceConfigHelper.isHeartbeatEnabled(context)) return;
        final Context appCtx = context.getApplicationContext();
        try {
            int hbSec = CricketSurfaceConfigHelper.getHeartbeatIntervalSeconds(appCtx);
            if (hbSec <= 0) return;
            HEARTBEAT_INTERVAL_MS = Math.max(
                    CricketSurfaceConfigHelper.HEARTBEAT_MIN_SECONDS * 1000L,
                    (long) hbSec * 1000L);
        } catch (Exception ignored) {
            return;
        }
        heartbeatRunnable = new Runnable() {
            @Override
            public void run() {
                synchronized (SessionAnalyticsTracker.this) {
                    if (!sessionOpen || sessionId == null || !processForeground || !screenOn) {
                        heartbeatRunnable = null;
                        return;
                    }
                    if (!CricketSurfaceConfigHelper.isHeartbeatEnabled(appCtx)) {
                        heartbeatRunnable = null;
                        return;
                    }
                    final String sid = sessionId;
                    final long opened = openAtMs;
                    NET_EXEC.execute(() -> {
                        JSONObject body = buildSessionBody(
                                appCtx, "heartbeat", sid, opened, 0L, 0L, null);
                        if (body != null) {
                            executeSessionCall(appCtx, body, false, false);
                        }
                    });
                    long next = HEARTBEAT_INTERVAL_MS;
                    try {
                        int hbSec = CricketSurfaceConfigHelper.getHeartbeatIntervalSeconds(appCtx);
                        if (hbSec <= 0) {
                            heartbeatRunnable = null;
                            return;
                        }
                        next = Math.max(
                                CricketSurfaceConfigHelper.HEARTBEAT_MIN_SECONDS * 1000L,
                                (long) hbSec * 1000L);
                        HEARTBEAT_INTERVAL_MS = next;
                    } catch (Exception ignored) {
                    }
                    mainHandler.postDelayed(this, next);
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

    private void persistPendingClose(
            Context context,
            String sid,
            long openAt,
            long closeAt,
            long durationMs,
            @Nullable JSONObject activitySummary) {
        try {
            JSONObject body = buildSessionBody(
                    context, "close", sid, openAt, closeAt, durationMs, activitySummary);
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
            Context context,
            String action,
            String sid,
            long openAt,
            long closeAt,
            long durationMs,
            @Nullable JSONObject activitySummary) {
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
                if (activitySummary != null && activitySummary.length() > 0) {
                    body.put("activity_summary", activitySummary);
                }
            } else if (durationMs > 0) {
                body.put("duration_ms", durationMs);
            }
            try {
                body.put("cricket_visible", CricketMiniPlayerManager.isCricketVisible());
                body.put("cricket_viewing", CricketMiniPlayerManager.isCricketViewing());
                String matchId = CricketMiniPlayerManager.getActiveMatchId();
                if (matchId != null) {
                    body.put("cricket_match_id", matchId);
                }
                int opens = CricketMiniPlayerManager.getAndResetOpensSinceLastSync();
                if (opens > 0) {
                    body.put("cricket_opens_since_last_sync", opens);
                }
            } catch (Exception ignored) {
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
     * Always on open/close; throttled on optional heartbeat.
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
            if (response.isSuccessful()) {
                if (clearPendingOnSuccess) {
                    clearPendingClose(context);
                }
                JsonObject resBody = response.body();
                if (resBody != null) {
                    boolean cricketSurfaceFlipped = false;
                    if (resBody.has("cricket_mini_player_enabled")) {
                        boolean newVal = resBody.get("cricket_mini_player_enabled").getAsBoolean();
                        boolean oldVal = CricketSurfaceConfigHelper.isMiniPlayerEnabled(context);
                        CricketSurfaceConfigHelper.saveMiniPlayerEnabled(context, newVal);
                        cricketSurfaceFlipped = oldVal != newVal;
                    }
                    if (resBody.has("cricket_footer_tab_enabled")
                            && !resBody.get("cricket_footer_tab_enabled").isJsonNull()) {
                        boolean newFooter = resBody.get("cricket_footer_tab_enabled").getAsBoolean();
                        boolean oldFooter = CricketSurfaceConfigHelper.isFooterTabEnabled(context);
                        CricketSurfaceConfigHelper.saveFooterTabEnabled(context, newFooter);
                        if (oldFooter != newFooter) {
                            cricketSurfaceFlipped = true;
                        }
                    }
                    // Notify on surface flip so Home updates mini/footer UI.
                    // Background polls only start when mini is ON and an active
                    // mini-player match is seeded (footer alone never polls).
                    if (cricketSurfaceFlipped) {
                        androidx.localbroadcastmanager.content.LocalBroadcastManager
                                .getInstance(context)
                                .sendBroadcast(new Intent(ACTION_CRICKET_STATE_CHANGED));
                    }
                    if (resBody.has("cricket_mini_player_poll_interval_seconds")) {
                        CricketSurfaceConfigHelper.savePollIntervalSeconds(context, 
                                resBody.get("cricket_mini_player_poll_interval_seconds").getAsInt());
                    }
                    if (resBody.has("cricket_mini_player_idle_poll_interval_seconds")
                            && !resBody.get("cricket_mini_player_idle_poll_interval_seconds").isJsonNull()) {
                        try {
                            CricketSurfaceConfigHelper.saveIdlePollIntervalSeconds(
                                    context,
                                    resBody.get("cricket_mini_player_idle_poll_interval_seconds").getAsInt());
                        } catch (Exception ignored) {
                        }
                    }
                    if (resBody.has("offline_news_pack_max")
                            && !resBody.get("offline_news_pack_max").isJsonNull()) {
                        try {
                            OfflineFeedStore.savePackMaxFromServer(
                                    context, resBody.get("offline_news_pack_max").getAsInt());
                        } catch (Exception ignored) {
                        }
                    }
                    if (resBody.has("home_mode_chrome_auto_hide_seconds")
                            && !resBody.get("home_mode_chrome_auto_hide_seconds").isJsonNull()) {
                        try {
                            HomeModeChromeConfig.saveAutoHideSecondsFromServer(
                                    context,
                                    resBody.get("home_mode_chrome_auto_hide_seconds").getAsInt());
                        } catch (Exception ignored) {
                        }
                    }
                    try {
                        String tapMode = null;
                        Integer halfPct = null;
                        Integer fullPct = null;
                        Integer railPct = null;
                        Boolean halfHideEng = null;
                        if (resBody.has("news_tap_mode")
                                && !resBody.get("news_tap_mode").isJsonNull()) {
                            tapMode = resBody.get("news_tap_mode").getAsString();
                        }
                        if (resBody.has("news_tap_half_upper_pct")
                                && !resBody.get("news_tap_half_upper_pct").isJsonNull()) {
                            halfPct = resBody.get("news_tap_half_upper_pct").getAsInt();
                        }
                        if (resBody.has("news_tap_full_upper_pct")
                                && !resBody.get("news_tap_full_upper_pct").isJsonNull()) {
                            fullPct = resBody.get("news_tap_full_upper_pct").getAsInt();
                        }
                        if (resBody.has("news_tap_full_right_rail_pct")
                                && !resBody.get("news_tap_full_right_rail_pct").isJsonNull()) {
                            railPct = resBody.get("news_tap_full_right_rail_pct").getAsInt();
                        }
                        if (resBody.has("news_tap_half_hide_engagement")
                                && !resBody.get("news_tap_half_hide_engagement").isJsonNull()) {
                            halfHideEng =
                                    resBody.get("news_tap_half_hide_engagement").getAsBoolean();
                        }
                        if (tapMode != null
                                || halfPct != null
                                || fullPct != null
                                || railPct != null
                                || halfHideEng != null) {
                            NewsTapModeConfig.saveFromServer(
                                    context,
                                    tapMode,
                                    halfPct,
                                    fullPct,
                                    railPct,
                                    halfHideEng);
                        }
                    } catch (Exception ignored) {
                    }
                    try {
                        Boolean hideDistrict = null;
                        Boolean rewardsMaster = null;
                        Boolean rewardsSlot = null;
                        Boolean flashSlot = null;
                        Boolean excludeReporters = null;
                        if (resBody.has("hide_district_on_main_feed")
                                && !resBody.get("hide_district_on_main_feed").isJsonNull()) {
                            hideDistrict =
                                    resBody.get("hide_district_on_main_feed").getAsBoolean();
                        }
                        if (resBody.has("rewards_master")
                                && !resBody.get("rewards_master").isJsonNull()) {
                            rewardsMaster = resBody.get("rewards_master").getAsBoolean();
                        }
                        if (resBody.has("rewards_in_main_feed_slot")
                                && !resBody.get("rewards_in_main_feed_slot").isJsonNull()) {
                            rewardsSlot =
                                    resBody.get("rewards_in_main_feed_slot").getAsBoolean();
                        }
                        if (resBody.has("flash_news_in_main_feed_slot")
                                && !resBody.get("flash_news_in_main_feed_slot").isJsonNull()) {
                            flashSlot =
                                    resBody.get("flash_news_in_main_feed_slot").getAsBoolean();
                        }
                        if (resBody.has("rewards_exclude_reporters")
                                && !resBody.get("rewards_exclude_reporters").isJsonNull()) {
                            excludeReporters =
                                    resBody.get("rewards_exclude_reporters").getAsBoolean();
                        }
                        if (hideDistrict != null
                                || rewardsMaster != null
                                || rewardsSlot != null
                                || flashSlot != null
                                || excludeReporters != null) {
                            DistrictPinVisibilityConfig.saveFromServer(
                                    context,
                                    hideDistrict,
                                    rewardsMaster,
                                    rewardsSlot,
                                    excludeReporters,
                                    flashSlot);
                        }
                        if (resBody.has("flash_news_chip_label")
                                && !resBody.get("flash_news_chip_label").isJsonNull()) {
                            FlashNewsChipHelper.saveLabelFromServer(
                                    context,
                                    resBody.get("flash_news_chip_label").getAsString());
                        }
                    } catch (Exception ignored) {
                    }
                    try {
                        Boolean lrEn = null;
                        Integer lrWin = null;
                        Integer lrShow = null;
                        Integer lrMin = null;
                        if (resBody.has("live_readers_enabled")
                                && !resBody.get("live_readers_enabled").isJsonNull()) {
                            lrEn = resBody.get("live_readers_enabled").getAsBoolean();
                        }
                        if (resBody.has("live_readers_window_minutes")
                                && !resBody.get("live_readers_window_minutes").isJsonNull()) {
                            lrWin = resBody.get("live_readers_window_minutes").getAsInt();
                        }
                        if (resBody.has("live_readers_show_seconds")
                                && !resBody.get("live_readers_show_seconds").isJsonNull()) {
                            lrShow = resBody.get("live_readers_show_seconds").getAsInt();
                        }
                        if (resBody.has("live_readers_min_count")
                                && !resBody.get("live_readers_min_count").isJsonNull()) {
                            lrMin = resBody.get("live_readers_min_count").getAsInt();
                        }
                        if (lrEn != null || lrWin != null || lrShow != null || lrMin != null) {
                            LiveReadersConfig.saveFromServer(context, lrEn, lrWin, lrShow, lrMin);
                        }
                    } catch (Exception ignored) {
                    }
                    if (resBody.has("read_history_hours")
                            && !resBody.get("read_history_hours").isJsonNull()) {
                        try {
                            ReadHistoryConfig.saveHoursFromServer(
                                    context, resBody.get("read_history_hours").getAsInt());
                        } catch (Exception ignored) {
                        }
                    }
                    Integer feedEveryN = null;
                    Integer adPrefetch = null;
                    Integer maxInFlight = null;
                    Integer flushEveryN = null;
                    String feedAdFormat = null;
                    String feedAdUnit = null;
                    if (resBody.has("feed_ad_every_n")
                            && !resBody.get("feed_ad_every_n").isJsonNull()) {
                        try {
                            feedEveryN = resBody.get("feed_ad_every_n").getAsInt();
                        } catch (Exception ignored) {
                        }
                    }
                    if (resBody.has("ad_prefetch_lookahead")
                            && !resBody.get("ad_prefetch_lookahead").isJsonNull()) {
                        try {
                            adPrefetch = resBody.get("ad_prefetch_lookahead").getAsInt();
                        } catch (Exception ignored) {
                        }
                    }
                    if (resBody.has("max_native_ad_loads_in_flight")
                            && !resBody.get("max_native_ad_loads_in_flight").isJsonNull()) {
                        try {
                            maxInFlight = resBody.get("max_native_ad_loads_in_flight").getAsInt();
                        } catch (Exception ignored) {
                        }
                    }
                    if (resBody.has("ad_impr_flush_every_n")
                            && !resBody.get("ad_impr_flush_every_n").isJsonNull()) {
                        try {
                            flushEveryN = resBody.get("ad_impr_flush_every_n").getAsInt();
                        } catch (Exception ignored) {
                        }
                    }
                    if (resBody.has("feed_ad_format")
                            && !resBody.get("feed_ad_format").isJsonNull()) {
                        try {
                            feedAdFormat = resBody.get("feed_ad_format").getAsString();
                        } catch (Exception ignored) {
                        }
                    }
                    if (resBody.has("ad_code") && !resBody.get("ad_code").isJsonNull()) {
                        try {
                            feedAdUnit = resBody.get("ad_code").getAsString();
                        } catch (Exception ignored) {
                        }
                    }
                    if (feedEveryN != null
                            || adPrefetch != null
                            || maxInFlight != null
                            || flushEveryN != null
                            || feedAdFormat != null
                            || feedAdUnit != null) {
                        FeedAdConfig.saveFromServer(
                                context,
                                feedEveryN,
                                adPrefetch,
                                maxInFlight,
                                flushEveryN,
                                feedAdFormat,
                                feedAdUnit);
                    }
                    if (resBody.has("session_live_enabled")
                            || resBody.has("session_heartbeat_interval_seconds")
                            || resBody.has("session_live_window_seconds")) {
                        Boolean liveEn = null;
                        Integer hb = null;
                        Integer win = null;
                        if (resBody.has("session_live_enabled")
                                && !resBody.get("session_live_enabled").isJsonNull()) {
                            liveEn = resBody.get("session_live_enabled").getAsBoolean();
                        }
                        if (resBody.has("session_heartbeat_interval_seconds")
                                && !resBody.get("session_heartbeat_interval_seconds").isJsonNull()) {
                            hb = Math.max(0, resBody.get("session_heartbeat_interval_seconds").getAsInt());
                        }
                        if (resBody.has("session_live_window_seconds")
                                && !resBody.get("session_live_window_seconds").isJsonNull()) {
                            win = Math.max(0, resBody.get("session_live_window_seconds").getAsInt());
                        }
                        CricketSurfaceConfigHelper.applyPresenceFromServer(context, liveEn, hb, win);
                        int effective =
                                CricketSurfaceConfigHelper.getHeartbeatIntervalSeconds(context);
                        HEARTBEAT_INTERVAL_MS = effective > 0 ? (long) effective * 1000L : 0L;
                        if (effective <= 0) {
                            mainHandler.post(this::stopHeartbeat);
                        } else if (sessionOpen && processForeground && screenOn) {
                            // Interval may have changed while session open — restart schedule.
                            mainHandler.post(() -> {
                                stopHeartbeat();
                                startHeartbeat(context);
                            });
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Pending close kept in prefs for retry on next open.
            e.printStackTrace();
        }
    }
}
