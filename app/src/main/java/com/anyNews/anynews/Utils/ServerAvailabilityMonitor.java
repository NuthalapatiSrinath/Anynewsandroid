package com.anyNews.anynews.Utils;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.anyNews.anynews.Application.AppController;
import com.anyNews.anynews.Services.CommonUtilities;

import java.io.IOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Detects sustained API unavailability (deploy / Bad Gateway / connection refused)
 * so Home can show the same Offline News chrome as device offline.
 *
 * <p><b>Thresholds (hysteresis — brief lag must NOT enter offline-server):</b>
 * <ul>
 *   <li>Enter after {@link #CONSECUTIVE_FAILURES_TO_ENTER} (3) consecutive hard/soft
 *       failures, OR {@link #HARD_FAILURES_IN_WINDOW} (2) hard failures within
 *       {@link #FAILURE_WINDOW_MS} (12s).</li>
 *   <li>Hard: HTTP 502/503/504, or ConnectException / UnknownHostException /
 *       NoRouteToHostException while the device still has a network path.</li>
 *   <li>Soft: SocketTimeoutException / call timeout — counts toward the consecutive
 *       streak only (one timeout then success clears; never enters alone).</li>
 *   <li>Ignored: normal 4xx, other 5xx, slow-but-successful responses, device-offline
 *       transport errors.</li>
 *   <li>Leave only after a successful 2xx API response or a healthy base-URL ping.</li>
     *   <li>Health ping: optional; while unavailable every
     *       {@link #UNAVAILABLE_HEALTH_INTERVAL_MS} (10s); otherwise at most every
     *       {@link #HEALTH_MIN_INTERVAL_MS} (20s) after first hard failure.</li>
 * </ul>
 */
public final class ServerAvailabilityMonitor {

    public static final String ACTION_SERVER_AVAILABILITY =
            "com.anyNews.anynews.SERVER_AVAILABILITY";
    public static final String EXTRA_SERVER_UNAVAILABLE = "server_unavailable";

    /** Header on probe requests so the interceptor does not re-count them. */
    public static final String HEALTH_HEADER = "X-AnyNews-Health";

    // --- hysteresis thresholds (documented for product / QA) ---
    private static final int CONSECUTIVE_FAILURES_TO_ENTER = 3;
    private static final int HARD_FAILURES_IN_WINDOW = 2;
    private static final long FAILURE_WINDOW_MS = 12_000L;
    /** While still available — avoid spam after a single hard failure. */
    private static final long HEALTH_MIN_INTERVAL_MS = 20_000L;
    private static final long SUSPECT_HEALTH_DELAY_MS = 2_500L;
    /**
     * While already unavailable (deploy / gateway): poll often so online feed
     * recovers soon after API returns — do not wait ~45s.
     */
    private static final long UNAVAILABLE_HEALTH_INTERVAL_MS = 10_000L;
    /** Min gap between probes while unavailable (allows resume/force to fire sooner). */
    private static final long UNAVAILABLE_HEALTH_MIN_INTERVAL_MS = 5_000L;

    private static final ServerAvailabilityMonitor INSTANCE = new ServerAvailabilityMonitor();

    private final Object lock = new Object();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final AtomicBoolean healthInFlight = new AtomicBoolean(false);

    private int consecutiveFailures = 0;
    private int hardFailuresInWindow = 0;
    private long windowStartElapsedMs = 0L;
    private boolean unavailable = false;
    private long lastHealthAttemptElapsedMs = 0L;
    @Nullable private OkHttpClient healthClient;
    @Nullable private Runnable pendingSuspectHealth;
    @Nullable private Runnable periodicHealthRunnable;

    private ServerAvailabilityMonitor() {}

    @NonNull
    public static ServerAvailabilityMonitor get() {
        return INSTANCE;
    }

    /** True when hysteresis decided the API is down / deploying / unreachable. */
    public boolean isUnavailable() {
        synchronized (lock) {
            return unavailable;
        }
    }

    /** Device has a usable network path (Wi‑Fi / cell / ethernet / VPN). */
    public static boolean isDeviceNetworkUp(@Nullable Context context) {
        Context app = context != null ? context.getApplicationContext() : null;
        if (app == null) {
            try {
                app = AppController.getInstance();
            } catch (Exception ignored) {
                return false;
            }
        }
        if (app == null) return false;
        try {
            ConnectivityManager cm =
                    (ConnectivityManager) app.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) return false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Network network = cm.getActiveNetwork();
                if (network == null) return false;
                NetworkCapabilities caps = cm.getNetworkCapabilities(network);
                return caps != null
                        && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        && (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                        || caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                        || caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                        || caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN));
            }
            android.net.NetworkInfo active = cm.getActiveNetworkInfo();
            return active != null && active.isConnected();
        } catch (Exception e) {
            return false;
        }
    }

    /** Successful API response — clear streak and leave offline-server if needed. */
    public void onHttpSuccess(int code) {
        if (code < 200 || code >= 300) {
            return;
        }
        boolean left;
        synchronized (lock) {
            consecutiveFailures = 0;
            hardFailuresInWindow = 0;
            windowStartElapsedMs = 0L;
            left = unavailable;
            unavailable = false;
        }
        cancelSuspectHealth();
        stopPeriodicHealth();
        if (left) {
            Logone.v("ServerAvail: server recovered (HTTP " + code + ")");
            broadcast(false);
        }
    }

    /**
     * HTTP status from OkHttp. Only 502/503/504 count as hard gateway failures.
     * 4xx and other 5xx are ignored for enter hysteresis.
     */
    public void onHttpResponse(int code) {
        if (code >= 200 && code < 300) {
            onHttpSuccess(code);
            return;
        }
        if (code == 502 || code == 503 || code == 504) {
            recordFailure(true, "HTTP " + code);
        }
        // Other statuses: do not enter offline-server.
    }

    /**
     * Transport / connect failure. Device-offline errors are ignored; timeouts are soft.
     * Cancelled Retrofit/OkHttp calls (typeahead debounce) must NEVER count — rapid
     * Search_Suggestions cancel was falsely entering offline-server and toasting
     * "You're offline — showing saved news" while Wi‑Fi/4G were up.
     */
    public void onTransportFailure(@Nullable IOException error) {
        if (error == null) return;
        if (isCancelledFailure(error)) {
            return;
        }
        if (!isDeviceNetworkUp(null)) {
            // Airplane / no Wi‑Fi — Home connectivity path owns offline UI.
            synchronized (lock) {
                consecutiveFailures = 0;
                hardFailuresInWindow = 0;
                windowStartElapsedMs = 0L;
            }
            return;
        }
        if (isSoftTimeout(error)) {
            recordFailure(false, "timeout");
            return;
        }
        if (isHardConnectFailure(error)) {
            recordFailure(true, error.getClass().getSimpleName());
            return;
        }
        // Other IOExceptions (e.g. unexpected EOF): treat as soft so one blip is ignored.
        recordFailure(false, "io:" + error.getClass().getSimpleName());
    }

    /** True for deliberate call cancel (debounce / activity leave) — not an outage. */
    private static boolean isCancelledFailure(@Nullable Throwable error) {
        Throwable t = error;
        int depth = 0;
        while (t != null && depth < 6) {
            if (t instanceof java.util.concurrent.CancellationException) {
                return true;
            }
            String msg = t.getMessage();
            if (msg != null) {
                String lower = msg.trim().toLowerCase();
                // OkHttp/Retrofit: "Canceled" / "Cancelled" when Call.cancel() runs.
                if (lower.equals("canceled")
                        || lower.equals("cancelled")
                        || lower.startsWith("canceled:")
                        || lower.startsWith("cancelled:")) {
                    return true;
                }
            }
            String simple = t.getClass().getSimpleName();
            if (simple != null) {
                String n = simple.toLowerCase();
                if (n.contains("cancel")) {
                    return true;
                }
            }
            t = t.getCause();
            depth++;
        }
        return false;
    }

    private static boolean isSoftTimeout(@NonNull IOException error) {
        if (error instanceof SocketTimeoutException) return true;
        String msg = error.getMessage();
        if (msg == null) return false;
        String lower = msg.toLowerCase();
        return lower.contains("timeout") || lower.contains("timed out");
    }

    private static boolean isHardConnectFailure(@NonNull IOException error) {
        if (error instanceof ConnectException
                || error instanceof UnknownHostException
                || error instanceof NoRouteToHostException) {
            return true;
        }
        String msg = error.getMessage();
        if (msg == null) return false;
        String lower = msg.toLowerCase();
        return lower.contains("failed to connect")
                || lower.contains("connection refused")
                || lower.contains("unable to resolve host")
                || lower.contains("no address associated");
    }

    private void recordFailure(boolean hard, @NonNull String reason) {
        boolean entered = false;
        boolean scheduleSuspect = false;
        synchronized (lock) {
            consecutiveFailures++;
            long now = SystemClock.elapsedRealtime();
            if (hard) {
                if (windowStartElapsedMs == 0L
                        || (now - windowStartElapsedMs) > FAILURE_WINDOW_MS) {
                    windowStartElapsedMs = now;
                    hardFailuresInWindow = 1;
                } else {
                    hardFailuresInWindow++;
                }
            }
            boolean hitConsecutive = consecutiveFailures >= CONSECUTIVE_FAILURES_TO_ENTER;
            boolean hitWindow = hard && hardFailuresInWindow >= HARD_FAILURES_IN_WINDOW;
            if (!unavailable && (hitConsecutive || hitWindow)) {
                unavailable = true;
                entered = true;
            } else if (!unavailable && hard && consecutiveFailures == 1) {
                scheduleSuspect = true;
            }
            Logone.v("ServerAvail: failure hard=" + hard
                    + " consec=" + consecutiveFailures
                    + " hardWin=" + hardFailuresInWindow
                    + " unavailable=" + unavailable
                    + " reason=" + reason);
        }
        if (entered) {
            Logone.v("ServerAvail: ENTER offline-server mode");
            cancelSuspectHealth();
            broadcast(true);
            startPeriodicHealth();
        } else if (scheduleSuspect) {
            scheduleSuspectHealth();
        }
    }

    private void scheduleSuspectHealth() {
        cancelSuspectHealth();
        pendingSuspectHealth =
                () -> {
                    pendingSuspectHealth = null;
                    if (!isUnavailable()) {
                        maybeHealthCheck("suspect");
                    }
                };
        mainHandler.postDelayed(pendingSuspectHealth, SUSPECT_HEALTH_DELAY_MS);
    }

    private void cancelSuspectHealth() {
        if (pendingSuspectHealth != null) {
            mainHandler.removeCallbacks(pendingSuspectHealth);
            pendingSuspectHealth = null;
        }
    }

    private void startPeriodicHealth() {
        stopPeriodicHealth();
        periodicHealthRunnable =
                new Runnable() {
                    @Override
                    public void run() {
                        if (!isUnavailable()) {
                            stopPeriodicHealth();
                            return;
                        }
                        maybeHealthCheck("periodic", true);
                        if (periodicHealthRunnable != null) {
                            mainHandler.postDelayed(
                                    periodicHealthRunnable, UNAVAILABLE_HEALTH_INTERVAL_MS);
                        }
                    }
                };
        mainHandler.postDelayed(periodicHealthRunnable, Math.min(
                UNAVAILABLE_HEALTH_INTERVAL_MS, 3_000L));
    }

    private void stopPeriodicHealth() {
        if (periodicHealthRunnable != null) {
            mainHandler.removeCallbacks(periodicHealthRunnable);
            periodicHealthRunnable = null;
        }
    }

    /** Lightweight GET to API base URL — never spam. */
    public void maybeHealthCheck(@NonNull String reason) {
        maybeHealthCheck(reason, false);
    }

    /**
     * Health probe. When {@code force} and already unavailable, use the shorter
     * unavailable min-interval (resume / chrome tap) so recover is not stuck behind
     * the longer "still available" throttle.
     */
    public void maybeHealthCheck(@NonNull String reason, boolean force) {
        if (!isDeviceNetworkUp(null)) return;
        long now = SystemClock.elapsedRealtime();
        boolean unavailableNow;
        synchronized (lock) {
            unavailableNow = unavailable;
            long minInterval = unavailableNow
                    ? UNAVAILABLE_HEALTH_MIN_INTERVAL_MS
                    : HEALTH_MIN_INTERVAL_MS;
            if (!force && lastHealthAttemptElapsedMs > 0
                    && (now - lastHealthAttemptElapsedMs) < minInterval) {
                return;
            }
            if (force && unavailableNow && lastHealthAttemptElapsedMs > 0
                    && (now - lastHealthAttemptElapsedMs) < UNAVAILABLE_HEALTH_MIN_INTERVAL_MS) {
                return;
            }
            lastHealthAttemptElapsedMs = now;
        }
        if (!healthInFlight.compareAndSet(false, true)) return;

        Logone.v("ServerAvail: health check reason=" + reason + " force=" + force);
        try {
            OkHttpClient client = healthClient();
            Request request =
                    new Request.Builder()
                            .url(CommonUtilities.URL)
                            .get()
                            .header(HEALTH_HEADER, "1")
                            .build();
            client.newCall(request)
                    .enqueue(
                            new Callback() {
                                @Override
                                public void onFailure(
                                        @NonNull Call call, @NonNull IOException e) {
                                    healthInFlight.set(false);
                                    // Probe failure only counts when network is still up.
                                    onTransportFailure(e);
                                }

                                @Override
                                public void onResponse(
                                        @NonNull Call call, @NonNull Response response) {
                                    healthInFlight.set(false);
                                    try {
                                        int code = response.code();
                                        // Gateway errors still mean API is down/deploying.
                                        if (code == 502 || code == 503 || code == 504) {
                                            onHttpResponse(code);
                                        } else if (code > 0) {
                                            // Host reachable (incl. 404 on bare base URL).
                                            onHttpSuccess(200);
                                        }
                                    } finally {
                                        response.close();
                                    }
                                }
                            });
        } catch (Exception e) {
            healthInFlight.set(false);
            Logone.w(e, "ServerAvail health check");
        }
    }

    @NonNull
    private OkHttpClient healthClient() {
        OkHttpClient existing = healthClient;
        if (existing != null) return existing;
        existing =
                new OkHttpClient.Builder()
                        .connectTimeout(5, TimeUnit.SECONDS)
                        .readTimeout(5, TimeUnit.SECONDS)
                        .writeTimeout(5, TimeUnit.SECONDS)
                        .callTimeout(8, TimeUnit.SECONDS)
                        .build();
        healthClient = existing;
        return existing;
    }

    private void broadcast(boolean nowUnavailable) {
        try {
            Context app = AppController.getInstance();
            if (app == null) return;
            Intent intent = new Intent(ACTION_SERVER_AVAILABILITY);
            intent.putExtra(EXTRA_SERVER_UNAVAILABLE, nowUnavailable);
            LocalBroadcastManager.getInstance(app).sendBroadcast(intent);
        } catch (Exception e) {
            Logone.w(e, "ServerAvail broadcast");
        }
    }
}
