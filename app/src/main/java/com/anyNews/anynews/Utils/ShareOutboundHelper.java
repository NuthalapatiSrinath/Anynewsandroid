package com.anyNews.anynews.Utils;

import android.net.Uri;
import android.text.TextUtils;

/**
 * Formats outbound share captions so recipients open AnyNews when possible.
 *
 * <p>WhatsApp / most messengers only reliably linkify {@code https://} (needed for OG
 * preview). Captions use <strong>title + https share URL only</strong> — custom-scheme
 * {@code anynews://} lines look broken in WhatsApp and are unnecessary when the share
 * host serves a package-scoped intent handoff + verified App Links.
 *
 * <p>True skip-Chrome for raw https still requires verified Digital Asset Links
 * (colon SHA-256 including Play App Signing cert) — see server
 * {@code docs/APP_LINKS_SHARE.md}. Landing uses an opaque package-scoped
 * {@code intent://…;package=} handoff when Chrome does load the https URL.
 * In-app opens of owned https hosts use {@link OwnedHttpsOpener} (never Chrome).
 * Admin {@code share_public_base_url} (splash) drives which https host is preferred;
 * see {@link SharePublicBaseHelper}.
 */
public final class ShareOutboundHelper {

    public static final String PACKAGE_NAME = "com.anyNews.anynews";

    private static final String PLAY_STORE_BASE =
            "https://play.google.com/store/apps/details?id=" + PACKAGE_NAME + "&hl=en_IN";

    private ShareOutboundHelper() {
    }

    /** Custom-scheme deep link — opens StartingActivity when AnyNews is installed. */
    public static String customSchemePostUrl(String postId) {
        return customSchemePostUrl(postId, "share");
    }

    public static String customSchemePostUrl(String postId, String type) {
        if (TextUtils.isEmpty(postId)) {
            return "";
        }
        String t = TextUtils.isEmpty(type) ? "share" : type.trim();
        return "anynews://post/" + postId.trim() + "?type=" + Uri.encode(t);
    }

    /**
     * Package-scoped Chrome Intent URL — opens AnyNews only (no disambiguation)
     * when the package is installed; otherwise Play Store via browser_fallback.
     */
    public static String customSchemeIntentUrl(String postId) {
        return customSchemeIntentUrl(postId, "share");
    }

    public static String customSchemeIntentUrl(String postId, String type) {
        if (TextUtils.isEmpty(postId)) {
            return "";
        }
        String t = TextUtils.isEmpty(type) ? "share" : type.trim();
        String play = PLAY_STORE_BASE + "&articleId=" + Uri.encode(postId.trim());
        return "intent://post/" + postId.trim()
                + "?type=" + Uri.encode(t)
                + "#Intent;scheme=anynews;package=" + PACKAGE_NAME
                + ";action=android.intent.action.VIEW"
                + ";S.browser_fallback_url=" + Uri.encode(play)
                + ";end";
    }

    /**
     * Prefer explicit post id; else parse {@code /p/{id}} from an https share URL.
     * Short codes without a numeric id return empty (caller keeps https only).
     */
    public static String extractPostId(String httpsShareLink, Long postId) {
        if (postId != null && postId > 0L) {
            return String.valueOf(postId);
        }
        if (TextUtils.isEmpty(httpsShareLink)) {
            return "";
        }
        try {
            Uri uri = Uri.parse(httpsShareLink.trim());
            String path = uri.getPath();
            if (path == null || path.isEmpty()) {
                return "";
            }
            String[] segments = path.split("/");
            for (int i = 0; i < segments.length; i++) {
                String seg = segments[i];
                if (seg == null || seg.isEmpty()) {
                    continue;
                }
                if (("p".equals(seg) || "post".equals(seg)) && i + 1 < segments.length) {
                    String next = segments[i + 1];
                    if (next != null && next.matches("\\d+")) {
                        return next;
                    }
                }
            }
            // Bare host/{digits} (anws.in / share host numeric path)
            for (String seg : segments) {
                if (seg != null && seg.matches("\\d+")) {
                    return seg;
                }
            }
        } catch (Exception ignored) {
        }
        return "";
    }

    /**
     * Caption for article shares: title, optional body, then https only
     * (WhatsApp OG preview). No {@code anynews://} line in outbound text.
     *
     * @param postId unused in caption text; kept so call sites can pass the id
     *               (landing / App Links use the https URL to open the post).
     */
    public static String formatCaption(String title, String description, String httpsShareLink,
                                       Long postId) {
        String safeTitle = title != null ? title.trim() : "";
        String safeDesc = description != null ? description.trim() : "";
        if (safeDesc.length() > 3500) {
            safeDesc = safeDesc.substring(0, 3500).trim() + "…";
        }
        String https = httpsShareLink != null ? httpsShareLink.trim() : "";

        StringBuilder sb = new StringBuilder();
        if (!safeTitle.isEmpty()) {
            sb.append(safeTitle);
        }
        if (!safeDesc.isEmpty()) {
            if (sb.length() > 0) {
                sb.append("\n\n");
            }
            sb.append(safeDesc);
        }
        if (!https.isEmpty()) {
            if (sb.length() > 0) {
                sb.append("\n\n");
            }
            sb.append(https);
        }
        return sb.toString();
    }

    /** Title + https share link (no body, no custom-scheme line). */
    public static String formatCaption(String title, String httpsShareLink, Long postId) {
        return formatCaption(title, null, httpsShareLink, postId);
    }

    /**
     * Prefer server {@code share_link}; if empty, build from Admin splash base.
     */
    public static String resolveHttpsShareLink(
            android.content.Context context, String shareLink, Long postId) {
        if (!TextUtils.isEmpty(shareLink)) {
            return shareLink.trim();
        }
        return SharePublicBaseHelper.buildFallbackShareUrl(context, postId, null);
    }
}
