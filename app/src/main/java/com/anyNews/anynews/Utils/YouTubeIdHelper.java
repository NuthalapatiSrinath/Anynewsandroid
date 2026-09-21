package com.anyNews.anynews.Utils;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Shared YouTube video-id extraction for Flipper feed players and Status Videos.
 * YouTube iframe / Android YouTube player APIs require an 11-char id — passing a
 * watch/shorts/embed URL yields YouTube's in-player "Video unavailable".
 */
public final class YouTubeIdHelper {

    private static final Pattern YOUTUBE_ID = Pattern.compile(
            "(?:youtu\\.be/|youtube(?:-nocookie)?\\.com/(?:watch\\?(?:.*&)?v=|embed/|v/|shorts/|live/|e/)|youtube\\.com/(?:shorts/|live/))([\\w-]{11})",
            Pattern.CASE_INSENSITIVE);

    private YouTubeIdHelper() {
    }

    /**
     * @return 11-char YouTube id, or null when {@code url} is empty / not YouTube
     */
    @Nullable
    public static String extract(@Nullable String url) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        String trimmed = url.trim();
        if (trimmed.length() == 11
                && !trimmed.contains("/")
                && !trimmed.contains(".")
                && trimmed.matches("[\\w-]{11}")) {
            return trimmed;
        }
        Matcher m = YOUTUBE_ID.matcher(trimmed);
        if (m.find()) {
            return m.group(1);
        }
        int vi = trimmed.toLowerCase(Locale.US).indexOf("/vi/");
        if (vi >= 0 && trimmed.length() >= vi + 15) {
            String id = trimmed.substring(vi + 4, vi + 15);
            if (id.matches("[\\w-]{11}")) {
                return id;
            }
        }
        return null;
    }

    /** True when the string looks like a YouTube URL or bare video id. */
    public static boolean isYouTubeish(@Nullable String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        if (extract(url) != null) {
            return true;
        }
        String u = url.trim().toLowerCase(Locale.US);
        return u.contains("youtube.com") || u.contains("youtu.be") || u.contains("img.youtube");
    }

    /**
     * Resolve an id from common feed fields (video_url first, then thumbnail /
     * background / nested video strings). Never uses CMS orange poster art as a
     * source of truth for display — only for id extraction when the URL embeds
     * {@code /vi/{id}/}.
     */
    @Nullable
    public static String extractFromItem(
            @Nullable String videoUrl,
            @Nullable String thumbnail,
            @Nullable String backgroundImage,
            @Nullable List<Object> videos,
            @Nullable List<String> images) {
        String id = extract(videoUrl);
        if (!TextUtils.isEmpty(id)) {
            return id;
        }
        id = extract(thumbnail);
        if (!TextUtils.isEmpty(id)) {
            return id;
        }
        id = extract(backgroundImage);
        if (!TextUtils.isEmpty(id)) {
            return id;
        }
        if (videos != null) {
            for (Object o : videos) {
                if (o == null) {
                    continue;
                }
                id = extract(String.valueOf(o));
                if (!TextUtils.isEmpty(id)) {
                    return id;
                }
            }
        }
        if (images != null) {
            for (String s : images) {
                id = extract(s);
                if (!TextUtils.isEmpty(id)) {
                    return id;
                }
            }
        }
        return null;
    }

    /**
     * Official YouTube poster frame for feed cards. Prefer this over CMS/splash
     * branding art (orange "Click Here to Play Video" / app_splash_logo).
     *
     * @return {@code https://i.ytimg.com/vi/{id}/hqdefault.jpg}, or null
     */
    @Nullable
    public static String hqDefaultThumbnailUrl(@Nullable String videoUrlOrId) {
        String id = extract(videoUrlOrId);
        if (TextUtils.isEmpty(id)) {
            return null;
        }
        return hqDefaultThumbnailUrlForId(id);
    }

    @Nullable
    public static String hqDefaultThumbnailUrlForId(@Nullable String id) {
        if (TextUtils.isEmpty(id)) {
            return null;
        }
        return "https://i.ytimg.com/vi/" + id + "/hqdefault.jpg";
    }

    /**
     * Prefer maxres when available; Glide callers should fall back to
     * {@link #hqDefaultThumbnailUrlForId} on error (maxres 404s for some videos).
     */
    @Nullable
    public static String maxresThumbnailUrlForId(@Nullable String id) {
        if (TextUtils.isEmpty(id)) {
            return null;
        }
        return "https://i.ytimg.com/vi/" + id + "/maxresdefault.jpg";
    }

    /**
     * Primary feed poster URL: maxresdefault for sharper cards; loaders must
     * error-fallback to hqdefault so a 404 never leaves a black plate.
     */
    @Nullable
    public static String feedPosterUrlForId(@Nullable String id) {
        return maxresThumbnailUrlForId(id);
    }
}
