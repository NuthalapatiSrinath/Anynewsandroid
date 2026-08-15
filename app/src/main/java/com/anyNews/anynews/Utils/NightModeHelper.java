package com.anyNews.anynews.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;

import com.anyNews.anynews.R;

/**
 * Manual-only app night mode from Settings ({@link Constants#NIGHT_MODE}).
 *
 * <p>Always uses {@link AppCompatDelegate#MODE_NIGHT_YES} / {@link AppCompatDelegate#MODE_NIGHT_NO}.
 * Never {@link AppCompatDelegate#MODE_NIGHT_FOLLOW_SYSTEM} or
 * {@link AppCompatDelegate#MODE_NIGHT_AUTO_BATTERY} so device battery saver / system dark
 * do not flip the app theme.
 */
public final class NightModeHelper {

    /** Target flash-fill while activities recreate (matches settings chrome). */
    private static final int FLASH_DARK = Color.parseColor("#0B0F19");
    private static final int FLASH_LIGHT = Color.parseColor("#F6F7F9");

    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    /** Guards concurrent toggle / lifecycle apply loops. */
    private static volatile boolean applying;

    private NightModeHelper() {}

    public static boolean isEnabled(@Nullable Context context) {
        if (context == null) {
            return false;
        }
        return SharedPrefsData.getBool(
                context, Constants.NIGHT_MODE, Constants.PREF_NAME, false);
    }

    public static int preferredNightMode(@Nullable Context context) {
        return isEnabled(context)
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO;
    }

    public static boolean isConfigurationNight(@Nullable Context context) {
        if (context == null) {
            return false;
        }
        int night =
                context.getResources().getConfiguration().uiMode
                        & Configuration.UI_MODE_NIGHT_MASK;
        return night == Configuration.UI_MODE_NIGHT_YES;
    }

    /**
     * Application cold start — bind YES/NO from stored pref before activities paint.
     * No-op if already on the preferred mode (avoids extra work / recreate storms).
     */
    public static void applyStoredPreference(@Nullable Context context) {
        if (context == null || applying) {
            return;
        }
        int mode = preferredNightMode(context);
        if (AppCompatDelegate.getDefaultNightMode() == mode) {
            return;
        }
        AppCompatDelegate.setDefaultNightMode(mode);
    }

    /**
     * Settings night switch: persist preference (sync), then apply manual night mode once.
     * Switch stays interactive; mode change is posted so the thumb animation can finish.
     */
    public static void setUserNightModeFromSettings(
            @NonNull final Activity activity, final boolean night) {
        // Durable write before any recreate (apply() can lose to a crash mid-toggle).
        activity.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(Constants.NIGHT_MODE, night)
                .commit();

        final int mode =
                night ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
        if (AppCompatDelegate.getDefaultNightMode() == mode) {
            return;
        }
        if (applying) {
            return;
        }

        // Let the SwitchCompat thumb settle, then apply once on the next frames.
        MAIN.post(
                () -> {
                    if (activity.isFinishing()) {
                        return;
                    }
                    applyDefaultNightMode(activity, mode, night);
                });
    }

    private static void applyDefaultNightMode(
            @NonNull Activity activity, int mode, boolean night) {
        if (AppCompatDelegate.getDefaultNightMode() == mode) {
            return;
        }
        if (applying) {
            return;
        }
        applying = true;
        try {
            softenWindowForThemeChange(activity, night);
            try {
                activity.getWindow().setWindowAnimations(R.style.NightModeWindowAnimation);
            } catch (Exception ignored) {
            }
            // Single global apply — AppCompat recreates activities that do not handle uiMode.
            // HomePage opts into uiMode configChanges and defers recreate until onResume.
            AppCompatDelegate.setDefaultNightMode(mode);
        } finally {
            MAIN.postDelayed(
                    () -> {
                        applying = false;
                    },
                    400L);
        }
    }

    /**
     * Paint status/nav/window with the destination chrome color to reduce white flash
     * during DayNight recreate of the foreground activity.
     */
    private static void softenWindowForThemeChange(@NonNull Activity activity, boolean night) {
        try {
            Window window = activity.getWindow();
            if (window == null) {
                return;
            }
            int fill = night ? FLASH_DARK : FLASH_LIGHT;
            window.setBackgroundDrawable(new ColorDrawable(fill));
            window.setStatusBarColor(fill);
            window.setNavigationBarColor(fill);
        } catch (Exception ignored) {
        }
    }

    /** Optional fade helper for deferred recreate (e.g. HomePage onResume). */
    public static void recreateWithFade(@NonNull Activity activity) {
        try {
            softenWindowForThemeChange(activity, isEnabled(activity));
            activity.getWindow().setWindowAnimations(R.style.NightModeWindowAnimation);
        } catch (Exception ignored) {
        }
        activity.recreate();
    }
}
