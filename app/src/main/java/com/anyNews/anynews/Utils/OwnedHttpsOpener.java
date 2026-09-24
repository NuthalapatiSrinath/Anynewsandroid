package com.anyNews.anynews.Utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.anyNews.anynews.Activities.StartingActivity;

/**
 * Opens AnyNews-owned https / custom-scheme URLs inside the app only.
 *
 * <p>Bare {@link Intent#ACTION_VIEW} on {@code https://share*.anynews.co.in/…} lets the
 * OS pick Chrome when App Links are unverified — that is the Chrome+blank-app
 * switcher bug. Package-scoped intents keep the hop in AnyNews.
 */
public final class OwnedHttpsOpener {

    private static final String TAG = "OwnedHttpsOpener";

    private OwnedHttpsOpener() {
    }

    /** True for hosts this app claims via App Links / share landing. */
    public static boolean isOwnedHost(@Nullable String host) {
        if (host == null || host.isEmpty()) {
            return false;
        }
        String h = host.toLowerCase(java.util.Locale.US);
        return h.equals("share.anynews.co.in")
                || h.equals("sharedev.anynews.co.in")
                || h.equals("api.anynews.co.in")
                || h.equals("anynewsdevelopmentapi.anynews.co.in")
                || h.equals("anynews.co.in")
                || h.equals("www.anynews.co.in")
                || h.equals("anws.in")
                || h.equals("www.anws.in")
                || h.equals("admin.anynews.co.in");
    }

    public static boolean isOwnedUri(@Nullable Uri uri) {
        if (uri == null) {
            return false;
        }
        String scheme = uri.getScheme();
        if (scheme == null) {
            return false;
        }
        if ("anynews".equalsIgnoreCase(scheme)) {
            return true;
        }
        if (!"https".equalsIgnoreCase(scheme) && !"http".equalsIgnoreCase(scheme)) {
            return false;
        }
        return isOwnedHost(uri.getHost());
    }

    public static boolean isOwnedUrl(@Nullable String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        try {
            return isOwnedUri(Uri.parse(url.trim()));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Open {@code url} in AnyNews when it is an owned link; otherwise normal
     * {@link Intent#ACTION_VIEW} (external browser / Play / WhatsApp, etc.).
     *
     * @return true if an activity was started
     */
    public static boolean open(@NonNull Context context, @Nullable String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        Uri uri;
        try {
            uri = Uri.parse(url.trim());
        } catch (Exception e) {
            Log.w(TAG, "bad url", e);
            return false;
        }
        return open(context, uri);
    }

    public static boolean open(@NonNull Context context, @Nullable Uri uri) {
        if (uri == null) {
            return false;
        }
        try {
            if (isOwnedUri(uri)) {
                return openInApp(context, uri);
            }
            Intent view = new Intent(Intent.ACTION_VIEW, uri);
            if (!(context instanceof android.app.Activity)) {
                view.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            context.startActivity(view);
            return true;
        } catch (Exception e) {
            Log.w(TAG, "open failed: " + uri, e);
            return false;
        }
    }

    /**
     * Force in-app open for an owned (or anynews://) URI — never Chrome.
     */
    public static boolean openInApp(@NonNull Context context, @NonNull Uri uri) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setClass(context, StartingActivity.class);
            intent.setPackage(context.getPackageName());
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            Log.w(TAG, "openInApp failed, retry package-only", e);
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.setPackage(ShareOutboundHelper.PACKAGE_NAME);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(intent);
                return true;
            } catch (Exception e2) {
                Log.e(TAG, "openInApp package retry failed", e2);
                return false;
            }
        }
    }
}
