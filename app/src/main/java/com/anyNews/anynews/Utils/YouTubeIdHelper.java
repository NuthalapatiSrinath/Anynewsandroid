package com.anyNews.anynews.Utils;

import android.text.TextUtils;

import androidx.annotation.Nullable;

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
}
