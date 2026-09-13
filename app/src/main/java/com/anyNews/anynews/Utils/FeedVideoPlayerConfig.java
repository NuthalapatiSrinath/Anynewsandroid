package com.anyNews.anynews.Utils;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.anyNews.anynews.R;

/**
 * Admin A/B for in-card feed Exo chrome.
 * Values arrive via splash / session / feed poll as {@code feed_video_player}.
 *
 * <ul>
 *   <li>{@link #MODE_CURRENT} — development Way2 chrome (no ±10, scrub-only time,
 *       mute/fs bottom while playing)</li>
 *   <li>{@link #MODE_PLAYSTORE} — Play Store classic from e87a084-era
 *       {@code player_custom_ui} (±10 / center pause / mute / red seek)</li>
 * </ul>
 *
 * Null / omitted from server must not wipe the cached preference.
 * Mode changes return {@code true} from {@link #saveFromServer} so HomePage can
 * invalidate FlipView scraps (layout XML differs per mode).
 */
public final class FeedVideoPlayerConfig {

    public static final String MODE_CURRENT = "current";
    /** Play Store classic; {@code classic} is accepted as an alias. */
    public static final String MODE_PLAYSTORE = "playstore";
    public static final String MODE_CLASSIC_ALIAS = "classic";

    public static final String DEFAULT_MODE = MODE_CURRENT;

    private FeedVideoPlayerConfig() {}

    /**
     * Persist when present; null = old server / omit — leave cache.
     *
     * @return true when the stored mode actually changed
     */
    public static boolean saveFromServer(@Nullable Context context, @Nullable String mode) {
        if (context == null || mode == null) {
            return false;
        }
        String normalized = normalizeMode(mode);
        String prev = getMode(context);
        if (normalized.equals(prev)) {
            return false;
        }
        SharedPrefsData.putString(
                context.getApplicationContext(),
                Constants.FEED_VIDEO_PLAYER,
                normalized,
                Constants.PREF_NAME);
        return true;
    }

    @NonNull
    public static String getMode(@Nullable Context context) {
        if (context == null) {
            return DEFAULT_MODE;
        }
        try {
            String v =
                    SharedPrefsData.getString(
                            context.getApplicationContext(),
                            Constants.FEED_VIDEO_PLAYER,
                            Constants.PREF_NAME);
            if (TextUtils.isEmpty(v)) {
                return DEFAULT_MODE;
            }
            return normalizeMode(v);
        } catch (Exception ignored) {
            return DEFAULT_MODE;
        }
    }

    public static boolean isPlayStoreClassic(@Nullable Context context) {
        return MODE_PLAYSTORE.equals(getMode(context));
    }

    public static boolean isCurrent(@Nullable Context context) {
        return !isPlayStoreClassic(context);
    }

    @NonNull
    public static String normalizeMode(@Nullable String raw) {
        String m = raw == null ? "" : raw.trim().toLowerCase();
        if (MODE_PLAYSTORE.equals(m) || MODE_CLASSIC_ALIAS.equals(m)) {
            return MODE_PLAYSTORE;
        }
        return MODE_CURRENT;
    }

    /** FlipView scrap key suffix so classic/current cards are not cross-reused. */
    @NonNull
    public static String layoutModeKey(@Nullable Context context) {
        return isPlayStoreClassic(context) ? "playstore" : "current";
    }

    @LayoutRes
    public static int fullVideoLayout(@Nullable Context context) {
        return isPlayStoreClassic(context)
                ? R.layout.activity_full_video_classic
                : R.layout.activity_full_video;
    }

    @LayoutRes
    public static int halfExoLayout(@Nullable Context context, boolean telugu) {
        if (isPlayStoreClassic(context)) {
            return telugu
                    ? R.layout.activity_half_exo_video_detailes_telugu_classic
                    : R.layout.activity_half_exo_video_detailes_classic;
        }
        return telugu
                ? R.layout.activity_half_exo_video_detailes_telugu
                : R.layout.activity_half_exo_video_detailes;
    }
}
