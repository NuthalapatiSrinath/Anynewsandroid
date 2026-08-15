package com.anyNews.anynews.Utils;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.Nullable;

/**
 * Admin-tunable auto-hide delay for home mode chrome (top News|Read bar
 * and bottom Calendar|Reload overlay).
 * Value arrives via splash / session ({@code home_mode_chrome_auto_hide_seconds}).
 *
 * <ul>
 *   <li>Default / missing / invalid → {@link #DEFAULT_AUTO_HIDE_SECONDS} (3)</li>
 *   <li>0 → disable auto-hide</li>
 *   <li>Range 0–{@link #MAX_AUTO_HIDE_SECONDS}</li>
 * </ul>
 *
 * Applied whenever chrome is shown (app start and every tap-to-reveal), not only
 * on first open.
 */
public final class HomeModeChromeConfig {

    public static final int DEFAULT_AUTO_HIDE_SECONDS = 3;
    public static final int MAX_AUTO_HIDE_SECONDS = 30;

    private HomeModeChromeConfig() {}

    /** Persist admin value when present (null = old server; leave cache / default). */
    public static void saveAutoHideSecondsFromServer(
            @Nullable Context context, @Nullable Integer seconds) {
        if (context == null || seconds == null) return;
        int safe = clamp(seconds);
        SharedPrefsData.putString(
                context.getApplicationContext(),
                Constants.HOME_MODE_CHROME_AUTO_HIDE_SECONDS,
                String.valueOf(safe),
                Constants.PREF_NAME);
    }

    /**
     * Seconds until chrome auto-hide after show. Never negative; may be 0 (disabled).
     * When unset falls back to {@link #DEFAULT_AUTO_HIDE_SECONDS}.
     */
    public static int getAutoHideSeconds(@Nullable Context context) {
        if (context == null) return DEFAULT_AUTO_HIDE_SECONDS;
        try {
            String v =
                    SharedPrefsData.getString(
                            context.getApplicationContext(),
                            Constants.HOME_MODE_CHROME_AUTO_HIDE_SECONDS,
                            Constants.PREF_NAME);
            if (TextUtils.isEmpty(v)) return DEFAULT_AUTO_HIDE_SECONDS;
            return clamp(Integer.parseInt(v.trim()));
        } catch (Exception ignored) {
            return DEFAULT_AUTO_HIDE_SECONDS;
        }
    }

    private static int clamp(int n) {
        if (n < 0) return DEFAULT_AUTO_HIDE_SECONDS;
        if (n > MAX_AUTO_HIDE_SECONDS) return MAX_AUTO_HIDE_SECONDS;
        return n;
    }
}
