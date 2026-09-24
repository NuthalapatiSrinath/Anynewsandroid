package com.anyNews.anynews.Utils;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Admin {@code share_public_base_url} from splash (Root_Settings).
 *
 * <p>One stable production share domain for admin/testing and live
 * ({@code share.anynews.co.in}). Outbound {@code share_link} on each post is
 * already built server-side from Admin; this helper caches that value for
 * fallbacks and owned-host checks.
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

    @NonNull
    public static String getBaseUrl(@Nullable Context context) {
        if (context == null) {
            return FALLBACK_BASE;
        }
        String stored = SharedPrefsData.getString(
                context, Constants.SHARE_PUBLIC_BASE_URL, Constants.PREF_NAME);
        String normalized = normalize(stored);
        return normalized.isEmpty() ? FALLBACK_BASE : normalized;
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
