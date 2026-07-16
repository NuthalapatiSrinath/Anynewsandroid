package com.anyNews.anynews.Utils;

import android.app.Activity;
import android.content.Context;
import android.view.WindowManager;

/**
 * Remote-config gate for screenshots / screen recording.
 *
 * Default (and fallback when the flag can't be fetched) is SECURE: FLAG_SECURE
 * is applied and capture is blocked — the app's normal behavior. When the admin
 * turns on {@code allow_screen_capture}, the app caches it here and stops
 * applying FLAG_SECURE so advertisers can screenshot / screen-record. Flipping
 * the toggle back OFF returns to the secure default on the next config fetch,
 * with no app rebuild required.
 */
public final class ScreenCaptureHelper {

    private ScreenCaptureHelper() {
    }

    public static void saveAllowed(Context context, boolean allowed) {
        if (context == null) {
            return;
        }
        SharedPrefsData.putBool(
                context,
                Constants.ALLOW_SCREEN_CAPTURE,
                allowed,
                Constants.PREF_NAME
        );
    }

    /** True only when capture has been explicitly enabled from remote config. Default false = secure. */
    public static boolean isCaptureAllowed(Context context) {
        if (context == null) {
            return false;
        }
        return SharedPrefsData.getBool(context, Constants.ALLOW_SCREEN_CAPTURE, Constants.PREF_NAME);
    }

    /**
     * Applies FLAG_SECURE unless capture is allowed. Safe to call any time (e.g.
     * after a config fetch) so the toggle takes effect without an app restart.
     */
    public static void applySecureFlag(Activity activity) {
        if (activity == null || activity.getWindow() == null) {
            return;
        }
        if (isCaptureAllowed(activity)) {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
        } else {
            activity.getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_SECURE,
                    WindowManager.LayoutParams.FLAG_SECURE
            );
        }
    }
}
