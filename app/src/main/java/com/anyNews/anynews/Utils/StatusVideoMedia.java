package com.anyNews.anynews.Utils;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Shared Status Videos media resolution — mirrors FlipperAdapter / Special card fields
 * ({@code video_url}, {@code thumbnail}, {@code images[0]}, videos/thumbnails arrays).
 */
public final class StatusVideoMedia {

    private static final Pattern YOUTUBE_ID = Pattern.compile(
            "(?:youtu\\.be/|youtube(?:-nocookie)?\\.com/(?:watch\\?(?:.*&)?v=|embed/|v/|shorts/))([\\w-]{11})",
            Pattern.CASE_INSENSITIVE);

    private StatusVideoMedia() {
    }

    /**
     * Same priority as Flipper nearby strip / full video poster:
     * thumbnail → thumbnails[0] → images[0] → background_image → YouTube hqdefault.
     */
    @Nullable
    public static String resolveThumbnailUrl(@Nullable ModelClass.Datum item) {
        if (item == null) {
            return null;
        }

        String direct = cleanUrl(item.getThumbnail());
        if (!TextUtils.isEmpty(direct) && !isStreamOnlyUrl(direct)) {
            return ensureHttps(resolveStatic(direct));
        }

        String fromThumbs = firstListUrl(item.getThumbnails(), true);
        if (!TextUtils.isEmpty(fromThumbs) && !isStreamOnlyUrl(fromThumbs)) {
            return ensureHttps(resolveStatic(fromThumbs));
        }

        String img0 = firstImage(item);
        if (!TextUtils.isEmpty(img0) && !isStreamOnlyUrl(img0)) {
            return ensureHttps(resolveStatic(img0));
        }

        String bg = cleanUrl(item.getBackgroundImage());
        if (!TextUtils.isEmpty(bg) && !isStreamOnlyUrl(bg)) {
            return ensureHttps(resolveStatic(bg));
        }

        // Nested video maps may carry poster / thumbnail only.
        String nested = firstListUrl(item.getVideos(), true);
        if (!TextUtils.isEmpty(nested) && !isStreamOnlyUrl(nested)) {
            return ensureHttps(resolveStatic(nested));
        }

        String ytId = resolveYoutubeId(item);
        if (!TextUtils.isEmpty(ytId)) {
            return "https://img.youtube.com/vi/" + ytId + "/hqdefault.jpg";
        }
        return null;
    }

    /**
     * Playable file/HLS URL for Exo — Flipper uses {@code getVideoUrl()} directly with no
     * extension gate; OCI object keys often lack {@code .mp4} in the path.
     */
    @Nullable
    public static String resolveStreamUrl(@Nullable ModelClass.Datum item) {
        if (item == null) {
            return null;
        }

        // media_type 4/6 = YouTube in Special/Flipper — do not hand bare ids to Exo.
        String media = normalize(item.getMediaType());
        if ("4".equals(media) || "6".equals(media)) {
            // Still allow an explicit non-youtube stream if present.
            String v = cleanUrl(item.getVideoUrl());
            if (!TextUtils.isEmpty(v) && isHttpUrl(v) && !isYoutubeish(v)) {
                return ensureHttps(v);
            }
            return null;
        }

        String[] candidates = {
                cleanUrl(item.getVideoUrl()),
                cleanUrl(item.getDownloadUrl()),
                firstListUrl(item.getVideos(), false)
        };
        for (String c : candidates) {
            if (TextUtils.isEmpty(c)) {
                continue;
            }
            if (isYoutubeish(c) && !isHttpUrl(c)) {
                continue; // bare YouTube id
            }
            if (isYoutubeish(c)) {
                continue;
            }
            if (isHttpUrl(c) || c.startsWith("file")) {
                return ensureHttps(c);
            }
        }
        return null;
    }

    @Nullable
    public static String resolveYoutubeId(@Nullable ModelClass.Datum item) {
        if (item == null) {
            return null;
        }

        String media = normalize(item.getMediaType());
        boolean youtubeMedia = "4".equals(media) || "6".equals(media);

        String fromMain = youtubeIdFromUrl(item.getVideoUrl(), youtubeMedia);
        if (fromMain != null) {
            return fromMain;
        }
        fromMain = youtubeIdFromUrl(item.getDownloadUrl(), false);
        if (fromMain != null) {
            return fromMain;
        }
        if (item.getVideos() != null) {
            for (Object o : item.getVideos()) {
                if (o == null) {
                    continue;
                }
                String id;
                if (o instanceof Map) {
                    Object url = firstMapValue((Map<?, ?>) o, "url", "video_url", "videoUrl", "src", "id");
                    id = youtubeIdFromUrl(url != null ? String.valueOf(url) : null, youtubeMedia);
                } else {
                    id = youtubeIdFromUrl(String.valueOf(o), youtubeMedia);
                }
                if (id != null) {
                    return id;
                }
            }
        }
        return null;
    }

    public static String ensureHttps(String url) {
        if (TextUtils.isEmpty(url)) {
            return url;
        }
        String t = url.trim();
        if (t.startsWith("//")) {
            return "https:" + t;
        }
        if (t.regionMatches(true, 0, "http://", 0, 7)) {
            return "https://" + t.substring(7);
        }
        return t;
    }

    public static String summarize(@Nullable String url) {
        if (TextUtils.isEmpty(url)) {
            return "empty";
        }
        String t = url.trim();
        return t.length() > 64 ? t.substring(0, 64) + "…" : t;
    }

    @Nullable
    private static String firstImage(ModelClass.Datum item) {
        if (item.getImages() == null || item.getImages().isEmpty()) {
            return null;
        }
        return cleanUrl(item.getImages().get(0));
    }

    /**
     * @param preferPosterKeys when true, prefer thumbnail/poster keys inside maps
     */
    @Nullable
    private static String firstListUrl(List<Object> list, boolean preferPosterKeys) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        for (Object o : list) {
            if (o == null) {
                continue;
            }
            if (o instanceof String) {
                String s = cleanUrl((String) o);
                if (!TextUtils.isEmpty(s)) {
                    return s;
                }
            } else if (o instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) o;
                Object url;
                if (preferPosterKeys) {
                    url = firstMapValue(map,
                            "thumbnail", "thumb", "poster", "image", "cover",
                            "background_image", "backgroundImage", "url", "src");
                } else {
                    url = firstMapValue(map, "url", "video_url", "videoUrl", "src", "link", "file");
                }
                if (url != null) {
                    String s = cleanUrl(String.valueOf(url));
                    if (!TextUtils.isEmpty(s)) {
                        return s;
                    }
                }
            } else {
                String s = cleanUrl(String.valueOf(o));
                if (!TextUtils.isEmpty(s) && (s.startsWith("http") || s.startsWith("//"))) {
                    return s;
                }
            }
        }
        return null;
    }

    @Nullable
    private static Object firstMapValue(Map<?, ?> map, String... keys) {
        if (map == null) {
            return null;
        }
        for (String key : keys) {
            if (map.containsKey(key) && map.get(key) != null) {
                return map.get(key);
            }
            for (Map.Entry<?, ?> e : map.entrySet()) {
                if (e.getKey() != null
                        && key.equalsIgnoreCase(String.valueOf(e.getKey()))
                        && e.getValue() != null) {
                    return e.getValue();
                }
            }
        }
        return null;
    }

    @Nullable
    private static String cleanUrl(@Nullable String url) {
        if (TextUtils.isEmpty(url) || "null".equalsIgnoreCase(url.trim())) {
            return null;
        }
        return url.trim();
    }

    private static String resolveStatic(String url) {
        try {
            String resolved = MediaUrl.resolve(url);
            if (!TextUtils.isEmpty(resolved) && !resolved.equals(MediaUrl.DEFAULT_PROFILE)) {
                return resolved;
            }
        } catch (Exception ignored) {
        }
        return url;
    }

    private static boolean isHttpUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        String u = url.trim().toLowerCase(Locale.US);
        return u.startsWith("http://") || u.startsWith("https://") || u.startsWith("//");
    }

    private static boolean isYoutubeish(String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        String u = url.trim().toLowerCase(Locale.US);
        if (u.contains("youtube.com") || u.contains("youtu.be") || u.contains("img.youtube")) {
            return true;
        }
        // Bare 11-char YouTube id (Special youtube media_type stores this in video_url)
        String raw = url.trim();
        return raw.length() == 11 && !raw.contains("/") && !raw.contains(".")
                && raw.matches("[\\w-]{11}");
    }

    /**
     * True when Glide should not be handed this URL as a bitmap (HLS/mp4 streams).
     */
    public static boolean isStreamOnlyUrl(@Nullable String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        String u = url.trim().toLowerCase(Locale.US);
        if (u.contains(".jpg") || u.contains(".jpeg") || u.contains(".png")
                || u.contains(".webp") || u.contains(".gif") || u.contains("thumb")
                || u.contains("poster") || u.contains("hqdefault") || u.contains("mqdefault")
                || u.contains("maxresdefault") || u.contains("sddefault")
                || u.contains("img.youtube")) {
            return false;
        }
        return u.contains(".mp4")
                || u.contains(".m3u8")
                || u.contains(".webm")
                || u.contains(".mov")
                || u.contains("manifest");
    }

    @Nullable
    private static String youtubeIdFromUrl(@Nullable String url, boolean allowBareId) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        String trimmed = url.trim();
        if (allowBareId || isYoutubeish(trimmed)) {
            if (trimmed.length() == 11 && !trimmed.contains("/") && !trimmed.contains(".")
                    && trimmed.matches("[\\w-]{11}")) {
                // Avoid treating random 11-char tokens as YT when URL is actually a stream.
                if (isHttpUrl(trimmed)) {
                    // fall through
                } else if (isYoutubeish(trimmed) || allowBareId) {
                    return trimmed;
                }
            }
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

    private static String normalize(String value) {
        return value != null ? value.trim().toLowerCase(Locale.US) : "";
    }
}
