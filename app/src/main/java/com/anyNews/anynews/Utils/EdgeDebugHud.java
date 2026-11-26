package com.anyNews.anynews.Utils;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.anyNews.anynews.BuildConfig;
import com.anyNews.anynews.Pojo.PublicEdgeFeedResponse;

import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.Locale;

import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;

/**
 * Dev-only overlay (debug APK) — short API / feed-origin flashes. No-op in release.
 */
public final class EdgeDebugHud {

    /** Debug APK only; release sets {@link BuildConfig#EDGE_DEBUG_HUD} false. */
    public static final boolean EDGE_DEBUG_HUD = BuildConfig.EDGE_DEBUG_HUD;

    private static final long DISMISS_MS = 2500L;
    private static final int MAX_LINES = 3;

    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static final ArrayDeque<String> LINES = new ArrayDeque<>(MAX_LINES);

    @Nullable private static WeakReference<Activity> hostRef;
    @Nullable private static FrameLayout overlayHost;
    @Nullable private static TextView hudText;
    @Nullable private static Runnable dismissRunnable;
    @Nullable private static String stagedFeedSource;

    private EdgeDebugHud() {
    }

    public static boolean isEnabled() {
        return EDGE_DEBUG_HUD;
    }

    public static void attach(@NonNull Activity activity) {
        if (!isEnabled()) {
            return;
        }
        MAIN.post(
                () -> {
                    hostRef = new WeakReference<>(activity);
                    ensureOverlay(activity);
                    render();
                });
    }

    public static void detach(@Nullable Activity activity) {
        if (!isEnabled()) {
            return;
        }
        MAIN.post(
                () -> {
                    Activity host = hostRef != null ? hostRef.get() : null;
                    if (activity != null && host != null && host != activity) {
                        return;
                    }
                    cancelDismiss();
                    if (overlayHost != null && overlayHost.getParent() instanceof ViewGroup) {
                        ((ViewGroup) overlayHost.getParent()).removeView(overlayHost);
                    }
                    overlayHost = null;
                    hudText = null;
                    hostRef = null;
                });
    }

    /** OkHttp completed call — path + CF-Cache-Status (GET) or method label (POST). */
    public static void notifyHttp(
            @Nullable Request request, @Nullable Response response) {
        if (!isEnabled() || request == null) {
            return;
        }
        try {
            HttpUrl url = request.url();
            if (url == null) {
                return;
            }
            String path = url.encodedPath();
            if (TextUtils.isEmpty(path)
                    || path.toLowerCase(Locale.US).contains("edge_telemetry")) {
                return;
            }
            if (!isSignificantPath(path)) {
                return;
            }
            Headers headers = response != null ? response.headers() : request.headers();
            String method = request.method();
            String suffix;
            if ("GET".equalsIgnoreCase(method)) {
                String cf = firstHeader(headers, "CF-Cache-Status", "cf-cache-status");
                suffix = TextUtils.isEmpty(cf) ? "GET" : cf.trim().toUpperCase(Locale.US);
            } else {
                suffix = method.toUpperCase(Locale.US);
            }
            enqueue(shortenPath(path) + " · " + suffix);
        } catch (Exception ignored) {
        }
    }

  /** Remember feed origin until {@link #flushStagedFeedSource()} on paint. */
    public static void stageFeedSource(@Nullable String label) {
        if (!isEnabled() || TextUtils.isEmpty(label)) {
            return;
        }
        stagedFeedSource = label;
    }

    public static void flushStagedFeedSource() {
        if (!isEnabled()) {
            return;
        }
        String line = stagedFeedSource;
        stagedFeedSource = null;
        if (!TextUtils.isEmpty(line)) {
            enqueue(line);
        }
    }

    @NonNull
    public static String buildFeedSourceLabel(
            @Nullable retrofit2.Response<PublicEdgeFeedResponse> response,
            @Nullable PublicEdgeFeedResponse body,
            boolean listPost) {
        if (listPost) {
            return "Feed: List POST";
        }
        Headers headers = null;
        if (response != null && response.headers() != null) {
            Headers.Builder b = new Headers.Builder();
            for (String name : response.headers().names()) {
                String v = response.headers().get(name);
                if (v != null) {
                    b.add(name, v);
                }
            }
            headers = b.build();
        }
        String cf = firstHeader(headers, "CF-Cache-Status", "cf-cache-status");
        if ("HIT".equalsIgnoreCase(cf)) {
            return "Feed: CF HIT";
        }
        String serving = servingFromBody(body);
        if (!TextUtils.isEmpty(serving)) {
            return "Feed: " + serving;
        }
        String mongo = firstHeader(headers, "X-AnyNews-Origin-Mongo", "x-anynews-origin-mongo");
        if ("USED".equalsIgnoreCase(mongo)) {
            return "Feed: Mongo";
        }
        String redis = firstHeader(headers, "X-AnyNews-Origin-Redis", "x-anynews-origin-redis");
        if ("HIT".equalsIgnoreCase(redis)) {
            return "Feed: Redis";
        }
        if (!TextUtils.isEmpty(cf)) {
            return "Feed: CF " + cf.trim().toUpperCase(Locale.US);
        }
        return "Feed: CF MISS";
    }

    /** Feed GET succeeded — stage paint label (HTTP line comes from OkHttp interceptor). */
    public static void notifyPublicFeedResponse(
            @Nullable Call<PublicEdgeFeedResponse> call,
            @Nullable retrofit2.Response<PublicEdgeFeedResponse> response) {
        if (!isEnabled()) {
            return;
        }
        PublicEdgeFeedHelper.logPublicFeedResponse(call, response);
        try {
            stageFeedSource(
                    buildFeedSourceLabel(
                            response,
                            response != null ? response.body() : null,
                            false));
        } catch (Exception ignored) {
        }
    }

    @Nullable
    private static String servingFromBody(@Nullable PublicEdgeFeedResponse body) {
        if (body == null) {
            return null;
        }
        String serving = body.getServing();
        if (!TextUtils.isEmpty(serving)) {
            String s = serving.trim().toLowerCase(Locale.US);
            if (s.contains("mongo")) {
                return "Mongo";
            }
            if (s.contains("redis")) {
                return "Redis";
            }
            return serving;
        }
        if (Boolean.TRUE.equals(body.getOrigin_mongo())) {
            return "Mongo";
        }
        if (Boolean.TRUE.equals(body.getOrigin_redis())) {
            return "Redis";
        }
        return null;
    }

    private static void enqueue(@NonNull String line) {
        MAIN.post(
                () -> {
                    while (LINES.size() >= MAX_LINES) {
                        LINES.pollFirst();
                    }
                    LINES.addLast(line);
                    Activity host = hostRef != null ? hostRef.get() : null;
                    if (host != null && !host.isFinishing()) {
                        ensureOverlay(host);
                    }
                    render();
                    scheduleDismiss();
                });
    }

    private static void render() {
        if (hudText == null) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (String s : LINES) {
            if (sb.length() > 0) {
                sb.append('\n');
            }
            sb.append(s);
        }
        hudText.setText(sb.toString());
        if (overlayHost != null) {
            overlayHost.setVisibility(sb.length() > 0 ? View.VISIBLE : View.GONE);
        }
    }

    private static void scheduleDismiss() {
        cancelDismiss();
        dismissRunnable =
                () -> {
                    LINES.clear();
                    stagedFeedSource = null;
                    render();
                };
        MAIN.postDelayed(dismissRunnable, DISMISS_MS);
    }

    private static void cancelDismiss() {
        if (dismissRunnable != null) {
            MAIN.removeCallbacks(dismissRunnable);
            dismissRunnable = null;
        }
    }

    private static void ensureOverlay(@NonNull Activity activity) {
        if (overlayHost != null && hudText != null) {
            return;
        }
        View content = activity.findViewById(android.R.id.content);
        if (!(content instanceof FrameLayout)) {
            return;
        }
        FrameLayout root = (FrameLayout) content;
        FrameLayout wrap = new FrameLayout(activity);
        FrameLayout.LayoutParams lp =
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        Gravity.BOTTOM);
        lp.setMargins(0, 0, 0, dp(activity, 72));
        wrap.setLayoutParams(lp);
        wrap.setClickable(false);
        wrap.setFocusable(false);
        wrap.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);

        TextView tv = new TextView(activity);
        FrameLayout.LayoutParams tlp =
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
        int pad = dp(activity, 8);
        tv.setLayoutParams(tlp);
        tv.setPadding(pad, pad, pad, pad);
        tv.setBackgroundColor(0xCC1A2332);
        tv.setTextColor(Color.parseColor("#E8F4FF"));
        tv.setTextSize(11f);
        tv.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        tv.setMaxLines(MAX_LINES);
        wrap.addView(tv);

        root.addView(wrap);
        overlayHost = wrap;
        hudText = tv;
    }

    private static int dp(@NonNull Activity activity, int dp) {
        float d = activity.getResources().getDisplayMetrics().density;
        return Math.round(dp * d);
    }

    @NonNull
    static String shortenPath(@NonNull String path) {
        String p = path;
        if (p.startsWith("/app/v3/")) {
            p = p.substring("/app/v3/".length());
        } else if (p.startsWith("/app/")) {
            p = p.substring("/app/".length());
        }
        if (p.length() > 48) {
            return "…" + p.substring(p.length() - 45);
        }
        return p;
    }

    private static boolean isSignificantPath(@NonNull String path) {
        String lower = path.toLowerCase(Locale.US);
        if (lower.contains("/public/")) {
            return true;
        }
        if (lower.contains("list_all_articles")) {
            return true;
        }
        if (lower.contains("splash")) {
            return true;
        }
        if (lower.contains("daily")) {
            return true;
        }
        if (lower.contains("flash")) {
            return true;
        }
        if (lower.contains("special")) {
            return true;
        }
        if (lower.contains("stock_market")) {
            return true;
        }
        return false;
    }

    @NonNull
    private static String firstHeader(@Nullable Headers headers, String... names) {
        if (headers == null) {
            return "";
        }
        for (String n : names) {
            String v = headers.get(n);
            if (!TextUtils.isEmpty(v)) {
                return v.trim();
            }
        }
        return "";
    }
}