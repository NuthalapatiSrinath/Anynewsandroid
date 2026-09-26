package com.anyNews.anynews.Utils;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

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

    private static final AtomicInteger CHOOSER_REQ = new AtomicInteger(1);
    private static final long CHOOSER_RECEIVER_TTL_MS = 120_000L;

    private ShareOutboundHelper() {
    }

    /**
     * Opens the system share sheet. {@code onTargetChosen} runs only when the user
     * picks an app — dismiss / back without choosing does <strong>not</strong> count.
     * Android cannot confirm the user pressed Send inside the target app.
     */
    public static void launchChooserOnTargetChosen(
            @NonNull Context context,
            @NonNull Intent sendIntent,
            @Nullable CharSequence title,
            @Nullable Runnable onTargetChosen) {
        final Context app = context.getApplicationContext();
        final int req = CHOOSER_REQ.getAndIncrement() & 0x7fffffff;
        final String action = app.getPackageName() + ".SHARE_TARGET_CHOSEN." + req;
        final BroadcastReceiver[] holder = new BroadcastReceiver[1];
        final Handler main = new Handler(Looper.getMainLooper());
        final Runnable cleanup = () -> {
            BroadcastReceiver r = holder[0];
            if (r == null) {
                return;
            }
            holder[0] = null;
            try {
                app.unregisterReceiver(r);
            } catch (Exception ignored) {
            }
        };

        holder[0] = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                cleanup.run();
                if (onTargetChosen != null) {
                    try {
                        onTargetChosen.run();
                    } catch (Exception ignored) {
                    }
                }
            }
        };

        IntentFilter filter = new IntentFilter(action);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                app.registerReceiver(holder[0], filter, Context.RECEIVER_NOT_EXPORTED);
            } else {
                app.registerReceiver(holder[0], filter);
            }
        } catch (Exception e) {
            // Fallback: open chooser without callback rather than blocking share.
            startChooserPlain(context, sendIntent, title);
            return;
        }

        main.postDelayed(cleanup, CHOOSER_RECEIVER_TTL_MS);

        Intent callback = new Intent(action).setPackage(app.getPackageName());
        int piFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // System fills EXTRA_CHOSEN_COMPONENT — must be mutable on API 31+.
            piFlags |= PendingIntent.FLAG_MUTABLE;
        }
        PendingIntent pending = PendingIntent.getBroadcast(app, req, callback, piFlags);
        Intent chooser = Intent.createChooser(
                sendIntent,
                title != null ? title : "Share",
                pending.getIntentSender());
        if (!(context instanceof Activity)) {
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        try {
            context.startActivity(chooser);
        } catch (Exception e) {
            cleanup.run();
            throw e;
        }
    }

    private static void startChooserPlain(
            Context context, Intent sendIntent, CharSequence title) {
        Intent chooser = Intent.createChooser(sendIntent, title != null ? title : "Share");
        if (!(context instanceof Activity)) {
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(chooser);
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

    /**
     * Append 0-based gallery/magazine page to an https share URL.
     * {@code pageIndex <= 0} leaves the link unchanged (legacy links = page 0).
     * Replaces any existing {@code page} / {@code slide} query param.
     */
    @NonNull
    public static String appendGalleryPage(@Nullable String httpsShareLink, int pageIndex) {
        if (TextUtils.isEmpty(httpsShareLink) || pageIndex <= 0) {
            return httpsShareLink != null ? httpsShareLink.trim() : "";
        }
        try {
            Uri uri = Uri.parse(httpsShareLink.trim());
            Uri.Builder b = uri.buildUpon().clearQuery();
            Set<String> names = uri.getQueryParameterNames();
            if (names != null) {
                for (String name : names) {
                    if (name == null) {
                        continue;
                    }
                    if ("page".equalsIgnoreCase(name) || "slide".equalsIgnoreCase(name)) {
                        continue;
                    }
                    for (String v : uri.getQueryParameters(name)) {
                        b.appendQueryParameter(name, v);
                    }
                }
            }
            b.appendQueryParameter("page", String.valueOf(pageIndex));
            return b.build().toString();
        } catch (Exception e) {
            String base = httpsShareLink.trim();
            String sep = base.contains("?") ? "&" : "?";
            return base + sep + "page=" + pageIndex;
        }
    }

    /**
     * Parse 0-based gallery page from a deep-link / share URI.
     * Accepts {@code page} or {@code slide}; missing / invalid → 0.
     */
    public static int parseGalleryPage(@Nullable Uri uri) {
        if (uri == null) {
            return 0;
        }
        try {
            String raw = uri.getQueryParameter("page");
            if (TextUtils.isEmpty(raw)) {
                raw = uri.getQueryParameter("slide");
            }
            if (TextUtils.isEmpty(raw)) {
                return 0;
            }
            int page = Integer.parseInt(raw.trim().replaceAll("[^0-9]", ""));
            return Math.max(0, page);
        } catch (Exception e) {
            return 0;
        }
    }

    /** @see #parseGalleryPage(Uri) */
    public static int parseGalleryPage(@Nullable String urlOrUri) {
        if (TextUtils.isEmpty(urlOrUri)) {
            return 0;
        }
        try {
            return parseGalleryPage(Uri.parse(urlOrUri.trim()));
        } catch (Exception e) {
            return 0;
        }
    }
}
