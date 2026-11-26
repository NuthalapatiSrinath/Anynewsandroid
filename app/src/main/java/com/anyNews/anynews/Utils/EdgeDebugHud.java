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
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
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
 * Dev / legacy-flip-test release overlay — rolling API call log (top-end). No-op when disabled.
 */
public final class EdgeDebugHud {

    /**
     * {@link BuildConfig#EDGE_DEBUG_HUD} or {@link BuildConfig#DEV_EDGE_HUD} (release APK → dev API).
     */
    public static final boolean EDGE_DEBUG_HUD =
            BuildConfig.EDGE_DEBUG_HUD || BuildConfig.DEV_EDGE_HUD;

    private static final int MAX_LINES = 8;
    private static final int COLOR_NORMAL = Color.parseColor("#E8F4FF");
    private static final int COLOR_WARNING = Color.parseColor("#FFB020");
    private static final int COLOR_ORIGIN = Color.parseColor("#FF5555");

    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static final ArrayDeque<HudLine> LINES = new ArrayDeque<>(MAX_LINES);

    @Nullable private static WeakReference<Activity> hostRef;
    @Nullable private static FrameLayout overlayHost;
    @Nullable private static LinearLayout linesContainer;
    @Nullable private static String stagedFeedSource;
    @Nullable private static HudTone stagedFeedTone;

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
                    if (overlayHost != null && overlayHost.getParent() instanceof ViewGroup) {
                        ((ViewGroup) overlayHost.getParent()).removeView(overlayHost);
                    }
                    overlayHost = null;
                    linesContainer = null;
                    hostRef = null;
                    LINES.clear();
                });
    }

    /** OkHttp completed call — path + served-by label for every app API path. */
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
            Headers headers = response != null ? response.headers() : request.headers();
            String method = request.method();
            HttpLine built = buildHttpLine(path, method, headers);
            enqueue(built.text, built.tone);
        } catch (Exception ignored) {
        }
    }

    /** Remember feed origin until {@link #flushStagedFeedSource()} on paint. */
    public static void stageFeedSource(@Nullable String label) {
        if (!isEnabled() || TextUtils.isEmpty(label)) {
            return;
        }
        stagedFeedSource = label;
        stagedFeedTone = toneForFeedLabel(label);
    }

    public static void flushStagedFeedSource() {
        if (!isEnabled()) {
            return;
        }
        String line = stagedFeedSource;
        HudTone tone = stagedFeedTone != null ? stagedFeedTone : HudTone.NORMAL;
        stagedFeedSource = null;
        stagedFeedTone = null;
        if (!TextUtils.isEmpty(line)) {
            enqueue(formatFeedStageLine(line), tone);
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
            return "Feed: Cloudflare (HIT)";
        }
        String serving = servingFromBody(body);
        if (!TextUtils.isEmpty(serving)) {
            if ("Mongo".equalsIgnoreCase(serving)) {
                return "Feed: Mongo";
            }
            if ("Redis".equalsIgnoreCase(serving)) {
                return "Feed: Redis";
            }
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
            return "Feed: Cloudflare (" + cf.trim().toUpperCase(Locale.US) + ")";
        }
        return "Feed: Cloudflare (MISS)";
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

    private static void enqueue(@NonNull String line, @NonNull HudTone tone) {
        MAIN.post(
                () -> {
                    while (LINES.size() >= MAX_LINES) {
                        LINES.pollFirst();
                    }
                    LINES.addLast(new HudLine(line, tone));
                    Activity host = hostRef != null ? hostRef.get() : null;
                    if (host != null && !host.isFinishing()) {
                        ensureOverlay(host);
                    }
                    render();
                });
    }

    private static void render() {
        if (linesContainer == null) {
            return;
        }
        linesContainer.removeAllViews();
        int pad = linesContainer.getResources().getDisplayMetrics().density > 0
                ? Math.round(4 * linesContainer.getResources().getDisplayMetrics().density)
                : 4;
        for (HudLine entry : LINES) {
            TextView row = new TextView(linesContainer.getContext());
            row.setText(entry.text);
            row.setTextColor(colorFor(entry.tone));
            row.setTextSize(10f);
            row.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
            row.setGravity(Gravity.END);
            row.setPadding(0, pad, 0, pad);
            linesContainer.addView(row);
        }
        if (overlayHost != null) {
            boolean show = !LINES.isEmpty();
            overlayHost.setVisibility(show ? View.VISIBLE : View.GONE);
            if (show) {
                overlayHost.bringToFront();
                overlayHost.setElevation(dpFromView(overlayHost, 24));
            }
        }
    }

    private static void ensureOverlay(@NonNull Activity activity) {
        if (overlayHost != null && linesContainer != null) {
            ViewParent parent = overlayHost.getParent();
            if (parent == null) {
                overlayHost = null;
                linesContainer = null;
            } else {
                return;
            }
        }
        View content = activity.findViewById(android.R.id.content);
        if (!(content instanceof ViewGroup)) {
            return;
        }
        ViewGroup root = (ViewGroup) content;

        FrameLayout wrap = new FrameLayout(activity);
        FrameLayout.LayoutParams lp =
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        Gravity.TOP | Gravity.END);
        int m = dp(activity, 8);
        lp.setMargins(m, dp(activity, 36), m, 0);
        wrap.setLayoutParams(lp);
        wrap.setClickable(false);
        wrap.setFocusable(false);
        wrap.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        wrap.setElevation(dp(activity, 24));

        LinearLayout stack = new LinearLayout(activity);
        stack.setOrientation(LinearLayout.VERTICAL);
        stack.setGravity(Gravity.END);
        int pad = dp(activity, 8);
        stack.setPadding(pad, pad, pad, pad);
        stack.setBackgroundColor(0xCC1A2332);
        FrameLayout.LayoutParams slp =
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        Gravity.END);
        wrap.addView(stack, slp);

        root.addView(wrap);
        overlayHost = wrap;
        linesContainer = stack;
    }

    private static int dp(@NonNull Activity activity, int dp) {
        float d = activity.getResources().getDisplayMetrics().density;
        return Math.round(dp * d);
    }

    private static int dpFromView(@NonNull View view, int dp) {
        float d = view.getResources().getDisplayMetrics().density;
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
        if (p.length() > 42) {
            return "…" + p.substring(p.length() - 39);
        }
        return p;
    }

    @NonNull
    private static HttpLine buildHttpLine(
            @NonNull String path, @NonNull String method, @Nullable Headers headers) {
        String shortPath = shortenPath(path);
        String served = resolveServedBy(path, method, headers);
        String text = shortPath + " · served by: " + served;
        HudTone tone = toneForHttp(path, method, headers, served);
        return new HttpLine(text, tone);
    }

    @NonNull
    private static String resolveServedBy(
            @NonNull String path, @NonNull String method, @Nullable Headers headers) {
        String lower = path.toLowerCase(Locale.US);
        if ("POST".equalsIgnoreCase(method) && lower.contains("list_all_articles")) {
            return "List POST";
        }
        String mongo = firstHeader(headers, "X-AnyNews-Origin-Mongo", "x-anynews-origin-mongo");
        if ("USED".equalsIgnoreCase(mongo)) {
            return "Mongo";
        }
        String redis = firstHeader(headers, "X-AnyNews-Origin-Redis", "x-anynews-origin-redis");
        if ("HIT".equalsIgnoreCase(redis)) {
            return "Redis";
        }
        String cf = firstHeader(headers, "CF-Cache-Status", "cf-cache-status");
        if (!TextUtils.isEmpty(cf)) {
            return "Cloudflare (" + cf.trim().toUpperCase(Locale.US) + ")";
        }
        if ("GET".equalsIgnoreCase(method)) {
            return "Cloudflare (MISS)";
        }
        return method.toUpperCase(Locale.US);
    }

    @NonNull
    private static HudTone toneForHttp(
            @NonNull String path,
            @NonNull String method,
            @Nullable Headers headers,
            @NonNull String served) {
        String lower = path.toLowerCase(Locale.US);
        if ("POST".equalsIgnoreCase(method) && lower.contains("list_all_articles")) {
            return HudTone.ORIGIN;
        }
        if (served.contains("Redis") || served.contains("Mongo") || served.contains("List POST")) {
            return HudTone.ORIGIN;
        }
        String cf = firstHeader(headers, "CF-Cache-Status", "cf-cache-status");
        if ("HIT".equalsIgnoreCase(cf)) {
            return HudTone.NORMAL;
        }
        if ("MISS".equalsIgnoreCase(cf) || served.contains("(MISS)")) {
            return HudTone.WARNING;
        }
        if ("POST".equalsIgnoreCase(method) && lower.contains("list")) {
            return HudTone.ORIGIN;
        }
        return HudTone.NORMAL;
    }

    @NonNull
    private static HudTone toneForFeedLabel(@NonNull String label) {
        String l = label.toLowerCase(Locale.US);
        if (l.contains("list post") || l.contains("redis") || l.contains("mongo")) {
            return HudTone.ORIGIN;
        }
        if (l.contains("miss")) {
            return HudTone.WARNING;
        }
        return HudTone.NORMAL;
    }

    @NonNull
    private static String formatFeedStageLine(@NonNull String staged) {
        if (staged.startsWith("Feed: ")) {
            String rest = staged.substring("Feed: ".length());
            return "public/feed · served by: " + rest;
        }
        return staged;
    }

    private static int colorFor(@NonNull HudTone tone) {
        switch (tone) {
            case ORIGIN:
                return COLOR_ORIGIN;
            case WARNING:
                return COLOR_WARNING;
            case NORMAL:
            default:
                return COLOR_NORMAL;
        }
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

    private enum HudTone {
        NORMAL,
        WARNING,
        ORIGIN
    }

    private static final class HudLine {
        @NonNull final String text;
        @NonNull final HudTone tone;

        HudLine(@NonNull String text, @NonNull HudTone tone) {
            this.text = text;
            this.tone = tone;
        }
    }

    private static final class HttpLine {
        @NonNull final String text;
        @NonNull final HudTone tone;

        HttpLine(@NonNull String text, @NonNull HudTone tone) {
            this.text = text;
            this.tone = tone;
        }
    }
}