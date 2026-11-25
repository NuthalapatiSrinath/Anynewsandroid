package com.anyNews.anynews.Utils;

import android.app.Activity;
import android.content.Context;
import android.view.WindowManager;

/**
 * Screenshots / screen recording are allowed app-wide (FLAG_SECURE is never applied).
 * Splash may still persist {@code allow_screen_capture} for analytics; it does not gate capture.
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

    public static boolean isCaptureAllowed(Context context) {
        return true;
    }

    public static void applySecureFlag(Activity activity) {
        if (activity == null || activity.getWindow() == null) {
            return;
        }
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
    }
}
