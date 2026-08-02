package com.anyNews.anynews.Utils;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.Nullable;

/**
 * Caches and reads the two independent admin cricket surface toggles delivered
 * via splash + {@code /app/v3/cricket_active_match}:
 *
 * <ul>
 *   <li>{@link Constants#CRICKET_MINI_PLAYER_ENABLED} — floating mini player.
 *       Unset → {@code true} (current behavior, mini player shown).</li>
 *   <li>{@link Constants#CRICKET_FOOTER_TAB_ENABLED} — bottom-footer cricket tab.
 *       Unset → {@code false} (footer stays exactly as it is today).</li>
 * </ul>
 *
 * Stored as "true"/"false" strings so an unset value falls back to the correct
 * default (a plain boolean pref could not tell "off" from "never received").
 */
public final class CricketSurfaceConfigHelper {

    private CricketSurfaceConfigHelper() {
    }

    /** @param enabled null → leave the cached value untouched (server omitted the flag). */
    public static void saveMiniPlayerEnabled(@Nullable Context context, @Nullable Boolean enabled) {
        if (context == null || enabled == null) return;
        SharedPrefsData.putString(
                context.getApplicationContext(),
                Constants.CRICKET_MINI_PLAYER_ENABLED,
                enabled ? "true" : "false",
                Constants.PREF_NAME);
    }

    /** @param enabled null → leave the cached value untouched (server omitted the flag). */
    public static void saveFooterTabEnabled(@Nullable Context context, @Nullable Boolean enabled) {
        if (context == null || enabled == null) return;
        SharedPrefsData.putString(
                context.getApplicationContext(),
                Constants.CRICKET_FOOTER_TAB_ENABLED,
                enabled ? "true" : "false",
                Constants.PREF_NAME);
    }

    public static void savePollIntervalSeconds(@Nullable Context context, int seconds) {
        if (context == null) return;
        SharedPrefsData.putString(
                context.getApplicationContext(),
                Constants.CRICKET_MINI_PLAYER_POLL_INTERVAL_SECONDS,
                String.valueOf(seconds),
                Constants.PREF_NAME);
    }

    public static void saveHeartbeatIntervalSeconds(@Nullable Context context, int seconds) {
        if (context == null) return;
        SharedPrefsData.putString(
                context.getApplicationContext(),
                Constants.SESSION_HEARTBEAT_INTERVAL_SECONDS,
                String.valueOf(seconds),
                Constants.PREF_NAME);
    }

    /** Mini player defaults ON when nothing has been cached yet. */
    public static boolean isMiniPlayerEnabled(@Nullable Context context) {
        return readBool(context, Constants.CRICKET_MINI_PLAYER_ENABLED, true);
    }

    /** Footer tab defaults OFF when nothing has been cached yet. */
    public static boolean isFooterTabEnabled(@Nullable Context context) {
        return readBool(context, Constants.CRICKET_FOOTER_TAB_ENABLED, false);
    }

    public static int getPollIntervalSeconds(@Nullable Context context) {
        return readInt(context, Constants.CRICKET_MINI_PLAYER_POLL_INTERVAL_SECONDS, 10);
    }

    public static int getHeartbeatIntervalSeconds(@Nullable Context context) {
        return readInt(context, Constants.SESSION_HEARTBEAT_INTERVAL_SECONDS, 45);
    }

    private static boolean readBool(@Nullable Context context, String key, boolean fallback) {
        if (context == null) return fallback;
        try {
            String v = SharedPrefsData.getString(context.getApplicationContext(), key, Constants.PREF_NAME);
            if (TextUtils.isEmpty(v)) return fallback;
            return "true".equalsIgnoreCase(v.trim());
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static int readInt(@Nullable Context context, String key, int fallback) {
        if (context == null) return fallback;
        try {
            String v = SharedPrefsData.getString(context.getApplicationContext(), key, Constants.PREF_NAME);
            if (TextUtils.isEmpty(v)) return fallback;
            return Integer.parseInt(v.trim());
        } catch (Exception ignored) {
            return fallback;
        }
    }
}
