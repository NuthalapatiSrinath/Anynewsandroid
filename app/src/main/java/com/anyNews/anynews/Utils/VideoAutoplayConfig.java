package com.anyNews.anynews.Utils;

import android.content.Context;

import androidx.annotation.Nullable;

/**
 * Admin FlipView autoplay for full-video cards and half-article (half-video) cards.
 *
 * <p>New APK reads {@code video_autoplay_full_enabled} and
 * {@code video_autoplay_half_enabled}. If a splash/feed payload only has the
 * legacy {@code video_autoplay_enabled}, both sides follow it.
 *
 * <p>The in-app Settings key {@code settings_video_autoplay} used to be AND-ed
 * with the admin flag and defaults to false. Nothing in the current Settings
 * screen writes it (the column was removed), so that AND made admin ON never
 * start playback. Admin card-type ON is what starts the visible card. The
 * leftover pref is not an opt-out until a Settings switch exists again.
 */
public final class VideoAutoplayConfig {

    private VideoAutoplayConfig() {
    }

    /**
     * Persist splash / feed-poll flags. Null fields are omitted by the server
     * and must not wipe a cached value. A legacy-only payload copies that
     * boolean onto both card types.
     */
    public static void saveFromServer(
            @Nullable Context context,
            @Nullable Boolean legacy,
            @Nullable Boolean full,
            @Nullable Boolean half) {
        if (context == null) {
            return;
        }
        if (legacy == null && full == null && half == null) {
            return;
        }
        boolean legacyOn = legacy != null
                ? Boolean.TRUE.equals(legacy)
                : isLegacyEnabled(context);
        if (legacy != null) {
            SharedPrefsData.putBool(
                    context, Constants.VIDEO_AUTOPLAY_ENABLED, legacyOn, Constants.PREF_NAME);
        }
        if (full != null) {
            SharedPrefsData.putBool(
                    context,
                    Constants.VIDEO_AUTOPLAY_FULL_ENABLED,
                    Boolean.TRUE.equals(full),
                    Constants.PREF_NAME);
        } else if (legacy != null) {
            SharedPrefsData.putBool(
                    context, Constants.VIDEO_AUTOPLAY_FULL_ENABLED, legacyOn, Constants.PREF_NAME);
        }
        if (half != null) {
            SharedPrefsData.putBool(
                    context,
                    Constants.VIDEO_AUTOPLAY_HALF_ENABLED,
                    Boolean.TRUE.equals(half),
                    Constants.PREF_NAME);
        } else if (legacy != null) {
            SharedPrefsData.putBool(
                    context, Constants.VIDEO_AUTOPLAY_HALF_ENABLED, legacyOn, Constants.PREF_NAME);
        }
    }

    /** Legacy global. Used when a split flag has never been stored on device. */
    public static boolean isLegacyEnabled(@Nullable Context context) {
        if (context == null) {
            return false;
        }
        return SharedPrefsData.getBool(
                context, Constants.VIDEO_AUTOPLAY_ENABLED, Constants.PREF_NAME, false);
    }

    public static boolean isFullEnabled(@Nullable Context context) {
        return readSplitOrLegacy(context, Constants.VIDEO_AUTOPLAY_FULL_ENABLED);
    }

    public static boolean isHalfEnabled(@Nullable Context context) {
        return readSplitOrLegacy(context, Constants.VIDEO_AUTOPLAY_HALF_ENABLED);
    }

    /**
     * Not an opt-out today. The Settings switch that wrote
     * {@code settings_video_autoplay} was removed, and that pref defaults to
     * false, so treating it as an override blocked every admin ON.
     */
    /** Settings pref is not an opt-out (see class note). A null context cannot play. */
    public static boolean isUserOptOut(@Nullable Context context) {
        return context == null;
    }

    /**
     * Admin master for this card type.
     * {@code full_video} uses the full toggle; {@code normal_video} (half article)
     * uses the half toggle.
     */
    public static boolean shouldAutoplay(@Nullable Context context, @Nullable String postType) {
        if (context == null || postType == null || isUserOptOut(context)) {
            return false;
        }
        if ("full_video".equalsIgnoreCase(postType)) {
            return isFullEnabled(context);
        }
        if ("normal_video".equalsIgnoreCase(postType)) {
            return isHalfEnabled(context);
        }
        return false;
    }

    private static boolean prefsContain(@Nullable Context context, String key) {
        if (context == null || key == null) {
            return false;
        }
        try {
            return context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
                    .contains(key);
        } catch (Exception ignored) {
            return false;
        }
    }

    private static boolean readSplitOrLegacy(@Nullable Context context, String splitKey) {
        if (context == null) {
            return false;
        }
        if (prefsContain(context, splitKey)) {
            return SharedPrefsData.getBool(context, splitKey, Constants.PREF_NAME, false);
        }
        return isLegacyEnabled(context);
    }
}
