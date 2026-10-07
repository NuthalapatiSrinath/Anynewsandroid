package com.anyNews.anynews.Utils;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Locale;

/**
 * Share https origin used when the phone builds a link itself.
 *
 * <p>A normal news caption does not build a host. It copies {@code share_link}
 * from the article the server already rewrote to the Admin share domain.
 * Splash {@code share_public_base_url} is a second copy and can still be the
 * production default while those article links are already on another host.
 * {@link #rememberFromNewsShareLink} stores the origin of a news {@code share_link}
 * already on the phone. {@link #getBaseUrl} prefers that origin, then splash,
 * then the production fallback. No extra request.
 */
public final class SharePublicBaseHelper {

    /** Safe fallback only when splash has never delivered a base. */
    private static final String FALLBACK_BASE =
            "https://share.anynews.co.in";

    private SharePublicBaseHelper() {
    }

    public static void saveFromServer(@Nullable Context context, @Nullable String url) {
        if (context == null) {
            return;
        }
        String normalized = normalize(url);
        if (normalized.isEmpty()) {
            return;
        }
        SharedPrefsData.putString(
                context, Constants.SHARE_PUBLIC_BASE_URL, normalized, Constants.PREF_NAME);
    }

    /**
     * Learn the https origin from a news article {@code share_link} already
     * on the phone (the URL a normal news share would put in the message).
     *
     * @return true when {@code shareLink} is an article share URL and its
     *         origin is now the one {@link #getBaseUrl} will return
     */
    public static boolean rememberFromNewsShareLink(
            @Nullable Context context, @Nullable String shareLink) {
        if (context == null) {
            return false;
        }
        String origin = newsShareOrigin(shareLink);
        if (origin.isEmpty()) {
            return false;
        }
        storeObservedBase(context, origin);
        return true;
    }

    /**
     * Learn from a feed page. If this page contains both the production
     * fallback host and another host, keep the other host — that is the
     * Admin domain a current news share is using.
     */
    public static void rememberFromNewsShareLinks(
            @Nullable Context context, @Nullable Iterable<String> shareLinks) {
        if (context == null || shareLinks == null) {
            return;
        }
        String first = "";
        String other = "";
        int seen = 0;
        for (String link : shareLinks) {
            if (seen++ >= 80) {
                break;
            }
            String origin = newsShareOrigin(link);
            if (origin.isEmpty()) {
                continue;
            }
            if (first.isEmpty()) {
                first = origin;
            }
            if (!FALLBACK_BASE.equals(origin)) {
                other = origin;
                break;
            }
        }
        String chosen = !other.isEmpty() ? other : first;
        if (!chosen.isEmpty()) {
            storeObservedBase(context, chosen);
        }
    }

    private static void storeObservedBase(@NonNull Context context, @NonNull String origin) {
        String stored = normalize(SharedPrefsData.getString(
                context, Constants.NEWS_SHARE_OBSERVED_BASE, Constants.PREF_NAME));
        if (!origin.equals(stored)) {
            SharedPrefsData.putString(
                    context, Constants.NEWS_SHARE_OBSERVED_BASE, origin, Constants.PREF_NAME);
        }
    }

    @NonNull
    public static String getBaseUrl(@Nullable Context context) {
        if (context == null) {
            return FALLBACK_BASE;
        }
        String observed = normalize(SharedPrefsData.getString(
                context, Constants.NEWS_SHARE_OBSERVED_BASE, Constants.PREF_NAME));
        if (!observed.isEmpty()) {
            return observed;
        }
        String stored = SharedPrefsData.getString(
                context, Constants.SHARE_PUBLIC_BASE_URL, Constants.PREF_NAME);
        String normalized = normalize(stored);
        return normalized.isEmpty() ? FALLBACK_BASE : normalized;
    }

    /**
     * Origin of an article share URL ({@code https://host/{code}} or
     * {@code https://host/p/{id}}). Daily Updates paths and job paths are
     * ignored so this phone does not learn a host from its own /du/ links.
     */
    @NonNull
    static String newsShareOrigin(@Nullable String shareLink) {
        if (TextUtils.isEmpty(shareLink)) {
            return "";
        }
        try {
            Uri uri = Uri.parse(shareLink.trim());
            String scheme = uri.getScheme();
            String host = uri.getHost();
            if (scheme == null || host == null) {
                return "";
            }
            if (!"https".equalsIgnoreCase(scheme) && !"http".equalsIgnoreCase(scheme)) {
                return "";
            }
            if (!isArticleSharePath(uri.getPath())) {
                return "";
            }
            String h = host.toLowerCase(Locale.US);
            if (h.contains("cloudfront") || h.contains("amazonaws")
                    || h.contains("googleusercontent") || h.contains("ytimg")) {
                return "";
            }
            return "https://" + h;
        } catch (Exception e) {
            return "";
        }
    }

    /** True for {@code /{code}}, {@code /{id}}, and {@code /p/{id}} — not {@code /du/} or {@code /job/}. */
    private static boolean isArticleSharePath(@Nullable String path) {
        if (path == null || path.isEmpty() || "/".equals(path)) {
            return false;
        }
        String p = path.startsWith("/") ? path.substring(1) : path;
        int slash = p.indexOf('/');
        if (slash < 0) {
            return p.matches("[A-Za-z0-9]{4,16}") || p.matches("\\d+");
        }
        String first = p.substring(0, slash);
        String rest = p.substring(slash + 1);
        int slash2 = rest.indexOf('/');
        if (slash2 >= 0) {
            rest = rest.substring(0, slash2);
        }
        if (rest.isEmpty()) {
            return false;
        }
        if ("p".equalsIgnoreCase(first) || "post".equalsIgnoreCase(first)) {
            return rest.matches("\\d+");
        }
        return false;
    }

    @Nullable
    public static String getHostname(@Nullable Context context) {
        try {
            return Uri.parse(getBaseUrl(context)).getHost();
        } catch (Exception e) {
            return null;
        }
    }

    /** True for Admin-configured Firebase-style short share hosts. */
    public static boolean isShortShareHost(@Nullable String host) {
        if (host == null || host.isEmpty()) {
            return false;
        }
        String h = host.toLowerCase(java.util.Locale.US);
        return h.equals("share.anynews.co.in") || h.equals("sharedev.anynews.co.in");
    }

    /**
     * Fallback https share URL when a post has no {@code share_link}.
     * Prefers short code path when Admin base is a short host.
     */
    @NonNull
    public static String buildFallbackShareUrl(
            @Nullable Context context, @Nullable Long postId, @Nullable String shortCode) {
        String base = getBaseUrl(context);
        String id = postId != null && postId > 0L ? String.valueOf(postId) : "";
        String code = shortCode != null ? shortCode.trim() : "";
        String host;
        try {
            host = Uri.parse(base).getHost();
        } catch (Exception e) {
            host = null;
        }
        if (!TextUtils.isEmpty(code) && isShortShareHost(host)) {
            return base + "/" + code;
        }
        if (!TextUtils.isEmpty(id) && host != null
                && (host.equalsIgnoreCase("anws.in") || host.equalsIgnoreCase("www.anws.in"))) {
            return base + "/" + id;
        }
        if (!TextUtils.isEmpty(id)) {
            return base + "/p/" + id;
        }
        return base;
    }

    @NonNull
    private static String normalize(@Nullable String url) {
        if (url == null) {
            return "";
        }
        String u = url.trim();
        if (u.isEmpty() || "null".equalsIgnoreCase(u)) {
            return "";
        }
        if (!u.startsWith("http://") && !u.startsWith("https://")) {
            u = "https://" + u;
        }
        while (u.endsWith("/")) {
            u = u.substring(0, u.length() - 1);
        }
        return u;
    }
}
