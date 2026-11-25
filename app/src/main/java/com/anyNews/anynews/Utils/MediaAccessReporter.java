package com.anyNews.anynews.Utils;

import com.anyNews.anynews.Utils.MyUtils;

import android.app.Application;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.anyNews.anynews.Pojo.EncritonRequestData;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Soft-accounting beacon for public OCI / object media opens.
 *
 * <p>Devices load object URLs directly (they never hit Node {@code /oci-media}). This class
 * records host + path prefix counters, batch-flushes to {@code /app/v3/report_media_access},
 * and dedupes the same URL for a short TTL so scroll rebinds do not thrash.
 *
 * <p>Flush interval from admin {@code media_access_flush_interval_seconds} (default 60).
 * Periodic timer only while Home is resumed; background / leave-Home still flush.
 *
 * <p>Approximate only — not OCI billing access logs.
 */
public final class MediaAccessReporter {

    private static final long DEDUP_TTL_MS = 90_000L;
    private static final int MAX_DEDUP = 400;
    private static final int MAX_AGG_KEYS = 80;
    private static final int FLUSH_AT_KEYS = 24;

    private static final MediaAccessReporter INSTANCE = new MediaAccessReporter();
    private static final ExecutorService NET_EXEC =
            Executors.newSingleThreadExecutor(
                    r -> {
                        Thread t = new Thread(r, "media-access-beacon");
                        t.setPriority(Thread.NORM_PRIORITY - 1);
                        return t;
                    });

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final Object lock = new Object();
    /** key → count in current aggregate window */
    private final Map<String, Agg> pending = new LinkedHashMap<>();
    /** full URL → last recorded elapsedRealtime-ish wall ms */
    private final ConcurrentHashMap<String, Long> recentUrls = new ConcurrentHashMap<>();

    @Nullable private Context appContext;
    @Nullable private Runnable flushRunnable;
    private boolean foreground = false;
    private boolean homeActive = false;
    private boolean flushInFlight = false;

    private MediaAccessReporter() {}

    public static MediaAccessReporter get() {
        return INSTANCE;
    }

    public void setHomeFlushActive(boolean active) {
        homeActive = active;
        if (active && foreground) {
            startFlushTimer();
        } else {
            stopFlushTimer();
        }
    }

    public void init(Application application) {
        if (!initialized.compareAndSet(false, true)) return;
        appContext = application.getApplicationContext();
        AnalyticsBatchConfig.warmFromPrefs(appContext);

        ProcessLifecycleOwner.get()
                .getLifecycle()
                .addObserver(
                        new LifecycleObserver() {
                            @OnLifecycleEvent(Lifecycle.Event.ON_START)
                            public void onAppForeground() {
                                foreground = true;
                                if (homeActive) {
                                    startFlushTimer();
                                }
                                flush(appContext, false);
                            }

                            @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
                            public void onAppBackground() {
                                foreground = false;
                                stopFlushTimer();
                                flush(appContext, true);
                            }
                        });

        application.registerComponentCallbacks(
                new ComponentCallbacks2() {
                    @Override
                    public void onTrimMemory(int level) {
                        if (level >= ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
                            flush(appContext, true);
                        }
                    }

                    @Override
                    public void onConfigurationChanged(Configuration newConfig) {}

                    @Override
                    public void onLowMemory() {
                        flush(appContext, true);
                    }
                });
    }

    /**
     * Record a media URL about to be loaded (or successfully requested). No-op for non-object
     * hosts, file://, empty, and within short-TTL dedup.
     */
    public void recordUrl(@Nullable String url) {
        if (TextUtils.isEmpty(url)) return;
        String trimmed = url.trim();
        if (trimmed.isEmpty()) return;
        String lower = trimmed.toLowerCase(Locale.US);
        if (lower.startsWith("file://")
                || lower.startsWith("content://")
                || lower.startsWith("android.resource://")
                || lower.startsWith("data:")) {
            return;
        }
        if (!(lower.startsWith("http://") || lower.startsWith("https://"))) {
            return;
        }

        Parsed p = parseObjectUrl(trimmed);
        if (p == null) return;

        long now = System.currentTimeMillis();
        String dedupKey = p.host + p.fullPath;
        Long prev = recentUrls.get(dedupKey);
        if (prev != null && now - prev < DEDUP_TTL_MS) {
            return;
        }
        recentUrls.put(dedupKey, now);
        pruneDedup(now);

        boolean flushSoon = false;
        synchronized (lock) {
            Agg a = pending.get(p.aggKey);
            if (a == null) {
                if (pending.size() >= MAX_AGG_KEYS) {
                    // Drop oldest insertion (LinkedHashMap order)
                    Iterator<String> it = pending.keySet().iterator();
                    if (it.hasNext()) {
                        it.next();
                        it.remove();
                    }
                }
                a = new Agg(p.host, p.pathPrefix);
                pending.put(p.aggKey, a);
            }
            a.count += 1;
            if (foreground && homeActive && pending.size() >= FLUSH_AT_KEYS) {
                flushSoon = true;
            }
        }
        if (flushSoon && appContext != null) {
            mainHandler.post(() -> flush(appContext, false));
        }
    }

    public void flush(@Nullable Context context, boolean critical) {
        final Context appCtx =
                context != null ? context.getApplicationContext() : appContext;
        if (appCtx == null) return;
        try {
            if (!MyUtils.isNetworkAvailable(appCtx)) return;
        } catch (Exception ignored) {
        }

        final List<JSONObject> events;
        synchronized (lock) {
            if (pending.isEmpty()) return;
            if (flushInFlight && !critical) return;
            events = new ArrayList<>(pending.size());
            for (Agg a : pending.values()) {
                if (a == null || a.count <= 0) continue;
                try {
                    JSONObject e = new JSONObject();
                    e.put("url_host", a.host);
                    e.put("path_prefix", a.pathPrefix);
                    e.put("count", a.count);
                    events.add(e);
                } catch (Exception ignored) {
                }
            }
            pending.clear();
            flushInFlight = true;
        }
        if (events.isEmpty()) {
            synchronized (lock) {
                flushInFlight = false;
            }
            return;
        }

        NET_EXEC.execute(
                () -> {
                    try {
                        postBatch(appCtx, events);
                    } finally {
                        synchronized (lock) {
                            flushInFlight = false;
                        }
                    }
                });
    }

    private void postBatch(Context appCtx, List<JSONObject> events) {
        try {
            JSONArray arr = new JSONArray();
            for (JSONObject o : events) arr.put(o);
            JSONObject body = new JSONObject();
            body.put("events", arr);
            String encrypted = MyUtils.encrypt(body.toString());
            EncritonRequestData enc = new EncritonRequestData();
            enc.setData(encrypted);
            JsonObject payload = new Gson().toJsonTree(enc).getAsJsonObject();
            APIService api = RestApiClient.getClient().create(APIService.class);
            Call<JsonObject> call = api.report_media_access("v3", payload);
            call.enqueue(
                    new Callback<JsonObject>() {
                        @Override
                        public void onResponse(
                                Call<JsonObject> call, Response<JsonObject> response) {
                            // Best-effort — drop on success or soft-fail (do not requeue thrash).
                        }

                        @Override
                        public void onFailure(Call<JsonObject> call, Throwable t) {
                            // Re-merge so a flaky network still lands eventually.
                            requeue(events);
                        }
                    });
        } catch (Exception e) {
            requeue(events);
        }
    }

    private void requeue(List<JSONObject> events) {
        if (events == null || events.isEmpty()) return;
        synchronized (lock) {
            for (JSONObject e : events) {
                if (e == null) continue;
                String host = e.optString("url_host", "");
                String prefix = e.optString("path_prefix", "/");
                int count = e.optInt("count", 1);
                if (host.isEmpty() || count <= 0) continue;
                String key = host + "|" + prefix;
                Agg a = pending.get(key);
                if (a == null) {
                    if (pending.size() >= MAX_AGG_KEYS) continue;
                    a = new Agg(host, prefix);
                    pending.put(key, a);
                }
                a.count += count;
            }
        }
    }

    private void startFlushTimer() {
        stopFlushTimer();
        if (!foreground || !homeActive) return;
        final long intervalMs = AnalyticsBatchConfig.getMediaFlushIntervalMs(appContext);
        flushRunnable =
                new Runnable() {
                    @Override
                    public void run() {
                        if (!foreground || !homeActive) return;
                        flush(appContext, false);
                        if (foreground && homeActive) {
                            long next = AnalyticsBatchConfig.getMediaFlushIntervalMs(appContext);
                            mainHandler.postDelayed(this, next);
                        }
                    }
                };
        mainHandler.postDelayed(flushRunnable, intervalMs);
    }

    private void stopFlushTimer() {
        if (flushRunnable != null) {
            mainHandler.removeCallbacks(flushRunnable);
            flushRunnable = null;
        }
    }

    private void pruneDedup(long now) {
        if (recentUrls.size() <= MAX_DEDUP) return;
        try {
            for (Map.Entry<String, Long> e : recentUrls.entrySet()) {
                Long t = e.getValue();
                if (t == null || now - t > DEDUP_TTL_MS) {
                    recentUrls.remove(e.getKey(), t);
                }
            }
            // Hard cap: clear oldest-ish by dumping half if still huge
            if (recentUrls.size() > MAX_DEDUP) {
                int drop = recentUrls.size() - MAX_DEDUP / 2;
                Iterator<Map.Entry<String, Long>> it = recentUrls.entrySet().iterator();
                while (it.hasNext() && drop > 0) {
                    it.next();
                    it.remove();
                    drop--;
                }
            }
        } catch (Exception ignored) {
        }
    }

    @Nullable
    static Parsed parseObjectUrl(String url) {
        try {
            Uri uri = Uri.parse(url);
            String host = uri.getHost();
            if (host == null || host.isEmpty()) return null;
            host = host.toLowerCase(Locale.US);
            if (!isObjectMediaHost(host)) return null;

            String path = uri.getPath();
            if (path == null || path.isEmpty()) path = "/";
            String pathPrefix = pathPrefixFromPath(path);
            String fullPath = path.split("\\?")[0];
            if (fullPath.length() > 240) fullPath = fullPath.substring(0, 240);
            String aggKey = host + "|" + pathPrefix;
            return new Parsed(host, pathPrefix, fullPath, aggKey);
        } catch (Exception e) {
            return null;
        }
    }

    static boolean isObjectMediaHost(String host) {
        if (host == null) return false;
        String h = host.toLowerCase(Locale.US);
        if (h.contains("objectstorage") && h.contains("oraclecloud")) return true;
        if (h.contains("compat.objectstorage") && h.contains("oraclecloud")) return true;
        if (h.contains("cloudfront.net")) return true;
        if (h.contains("amazonaws.com") && h.contains("s3")) return true;
        if (h.contains("anynews")
                && (h.contains("cdn") || h.contains("media") || h.contains("img"))) {
            return true;
        }
        return false;
    }

    static String pathPrefixFromPath(String path) {
        if (path == null || path.isEmpty()) return "/";
        String p = path;
        int q = p.indexOf('?');
        if (q >= 0) p = p.substring(0, q);
        // OCI: /n/{ns}/b/{bucket}/o/{objectKey...}
        int oIdx = p.indexOf("/o/");
        if (oIdx >= 0) {
            String key = p.substring(oIdx + 3);
            String[] segs = key.split("/");
            StringBuilder sb = new StringBuilder("/o");
            int n = 0;
            for (String s : segs) {
                if (s == null || s.isEmpty()) continue;
                sb.append('/').append(s);
                n++;
                if (n >= 2) break;
            }
            return sb.toString();
        }
        String[] segs = p.split("/");
        StringBuilder sb = new StringBuilder();
        int n = 0;
        for (String s : segs) {
            if (s == null || s.isEmpty()) continue;
            sb.append('/').append(s);
            n++;
            if (n >= 3) break;
        }
        return sb.length() == 0 ? "/" : sb.toString();
    }

    private static final class Agg {
        final String host;
        final String pathPrefix;
        int count;

        Agg(String host, String pathPrefix) {
            this.host = host;
            this.pathPrefix = pathPrefix != null ? pathPrefix : "/";
            this.count = 0;
        }
    }

    static final class Parsed {
        final String host;
        final String pathPrefix;
        final String fullPath;
        final String aggKey;

        Parsed(String host, String pathPrefix, String fullPath, String aggKey) {
            this.host = host;
            this.pathPrefix = pathPrefix;
            this.fullPath = fullPath;
            this.aggKey = aggKey;
        }
    }
}
