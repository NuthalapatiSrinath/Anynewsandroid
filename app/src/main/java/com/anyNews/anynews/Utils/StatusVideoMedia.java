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
            "(?:youtu\\.be/|youtube(?:-nocookie)?\\.com/(?:watch\\?(?:.*&)?v=|embed/|v/|shorts/|live/|e/)|youtube\\.com/(?:shorts/|live/))([\\w-]{11})",
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
        final String writerAvatar = cleanUrl(item.getWriterProfile());

        String direct = cleanUrl(item.getThumbnail());
        if (isUsableVideoPoster(direct, writerAvatar)
                && !isStreamOnlyUrl(direct)
                && !isGenericVideoPlaceholderUrl(direct)) {
            return ensureHttps(resolveStatic(direct));
        }

        String videoThumb = cleanUrl(item.getVideoThumb());
        if (isUsableVideoPoster(videoThumb, writerAvatar)
                && !isStreamOnlyUrl(videoThumb)
                && !isGenericVideoPlaceholderUrl(videoThumb)) {
            return ensureHttps(resolveStatic(videoThumb));
        }

        String thumbImage = cleanUrl(item.getThumbImage());
        if (isUsableVideoPoster(thumbImage, writerAvatar)
                && !isStreamOnlyUrl(thumbImage)
                && !isGenericVideoPlaceholderUrl(thumbImage)) {
            return ensureHttps(resolveStatic(thumbImage));
        }

        String loneImage = cleanUrl(item.getImage());
        if (isUsableVideoPoster(loneImage, writerAvatar)
                && !isStreamOnlyUrl(loneImage)
                && !isGenericVideoPlaceholderUrl(loneImage)) {
            return ensureHttps(resolveStatic(loneImage));
        }

        String fromThumbs = firstListUrl(item.getThumbnails(), true);
        if (isUsableVideoPoster(fromThumbs, writerAvatar)
                && !isStreamOnlyUrl(fromThumbs)
                && !isGenericVideoPlaceholderUrl(fromThumbs)) {
            return ensureHttps(resolveStatic(fromThumbs));
        }

        String img0 = firstImage(item);
        if (isUsableVideoPoster(img0, writerAvatar)
                && !isStreamOnlyUrl(img0)
                && !isGenericVideoPlaceholderUrl(img0)) {
            return ensureHttps(resolveStatic(img0));
        }

        String bg = cleanUrl(item.getBackgroundImage());
        if (isUsableVideoPoster(bg, writerAvatar)
                && !isStreamOnlyUrl(bg)
                && !isGenericVideoPlaceholderUrl(bg)) {
            return ensureHttps(resolveStatic(bg));
        }

        // Nested video maps may carry poster / thumbnail only.
        String nested = firstListUrl(item.getVideos(), true);
        if (isUsableVideoPoster(nested, writerAvatar)
                && !isStreamOnlyUrl(nested)
                && !isGenericVideoPlaceholderUrl(nested)) {
            return ensureHttps(resolveStatic(nested));
        }

        String ytId = resolveYoutubeId(item);
        if (!TextUtils.isEmpty(ytId)) {
            return "https://img.youtube.com/vi/" + ytId + "/hqdefault.jpg";
        }
        return null;
    }

    /**
     * Feed half/full Exo idle poster — {@link #resolveThumbnailUrl} plus YouTube hqdefault
     * when CMS thumbnail is empty (avoids Universal Image Loader {@code app_splash_logo} empty-uri art).
     */
    /**
     * YouTube half cards (media_type 4): official frame first; never CMS splash branding.
     */
    @Nullable
    public static String resolveYoutubeHalfPosterUrl(@Nullable ModelClass.Datum item) {
        if (item == null) {
            return null;
        }
        String ytId = resolveYoutubeId(item);
        if (!TextUtils.isEmpty(ytId)) {
            String max = YouTubeIdHelper.maxresThumbnailUrlForId(ytId);
            if (!TextUtils.isEmpty(max)) {
                return max;
            }
            return YouTubeIdHelper.hqDefaultThumbnailUrlForId(ytId);
        }
        String hq = YouTubeIdHelper.hqDefaultThumbnailUrl(item.getVideoUrl());
        if (!TextUtils.isEmpty(hq)) {
            return hq;
        }
        hq = YouTubeIdHelper.hqDefaultThumbnailUrl(item.getVideo());
        if (!TextUtils.isEmpty(hq)) {
            return hq;
        }
        String thumb = resolveThumbnailUrl(item);
        return isGenericVideoPlaceholderUrl(thumb) ? null : thumb;
    }

    /** Orange "Click Here to Play" / splash logo art must not cover video cards. */
    public static boolean isGenericVideoPlaceholderUrl(@Nullable String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        String u = url.trim().toLowerCase(Locale.US);
        return u.contains("app_splash")
                || u.contains("splash_logo")
                || u.contains("splash_screen")
                || u.contains("click_here")
                || u.contains("click-here")
                || u.contains("play_video")
                || u.contains("default_video")
                || u.contains("/anynews_logo")
                || u.contains("placeholder_video");
    }

    @Nullable
    public static String resolveFeedVideoPosterUrl(@Nullable ModelClass.Datum item) {
        if (item != null) {
            String media = normalize(item.getMediaType());
            if ("4".equals(media) || "6".equals(media)) {
                String ytPoster = asGlidePosterUrl(resolveYoutubeHalfPosterUrl(item));
                if (!TextUtils.isEmpty(ytPoster)) {
                    return ytPoster;
                }
            }
        }
        String thumb = asGlidePosterUrl(resolveThumbnailUrl(item));
        if (!TextUtils.isEmpty(thumb)) {
            return thumb;
        }
        if (item == null) {
            return null;
        }
        // HLS / stream-only play URL: never Glide the manifest — use server still.
        final String writerAvatar = cleanUrl(item.getWriterProfile());
        String stream = resolveStreamUrl(item);
        if (!TextUtils.isEmpty(stream) && isStreamOnlyUrl(stream)) {
            String img0 = firstImage(item);
            String fromImg = asGlidePosterUrl(
                    isUsableVideoPoster(img0, writerAvatar) ? resolveStatic(img0) : null);
            if (!TextUtils.isEmpty(fromImg)) {
                return fromImg;
            }
            String serverThumb = asGlidePosterUrl(cleanUrl(item.getVideoThumb()));
            if (!TextUtils.isEmpty(serverThumb)) {
                return serverThumb;
            }
        }
        String yt = asGlidePosterUrl(YouTubeIdHelper.hqDefaultThumbnailUrl(item.getVideoUrl()));
        if (!TextUtils.isEmpty(yt)) {
            return yt;
        }
        yt = asGlidePosterUrl(YouTubeIdHelper.hqDefaultThumbnailUrl(item.getVideo()));
        if (!TextUtils.isEmpty(yt)) {
            return yt;
        }
        if (item.getVideos() != null) {
            for (Object o : item.getVideos()) {
                if (o == null) {
                    continue;
                }
                yt = asGlidePosterUrl(YouTubeIdHelper.hqDefaultThumbnailUrl(String.valueOf(o)));
                if (!TextUtils.isEmpty(yt)) {
                    return yt;
                }
            }
        }
        return null;
    }

    /** Bitmap posters only — Glide must never receive HLS/mp4 play URLs. */
    @Nullable
    public static String asGlidePosterUrl(@Nullable String url) {
        if (TextUtils.isEmpty(url) || isStreamOnlyUrl(url) || isGenericVideoPlaceholderUrl(url)) {
            return null;
        }
        return ensureHttps(url.startsWith("http") ? url : resolveStatic(url));
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
                cleanUrl(item.getVideo()),
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

    /**
     * Exo tap-to-play on feed cards: {@link #resolveStreamUrl} then a guarded {@code video_url}
     * fallback (HTTP/HLS only — never bare YouTube ids / watch pages).
     */
    @Nullable
    public static String resolveFeedPlayUrl(@Nullable ModelClass.Datum item) {
        String stream = resolveStreamUrl(item);
        if (!TextUtils.isEmpty(stream)) {
            return stream;
        }
        if (item == null) {
            return null;
        }
        String raw = cleanUrl(item.getVideoUrl());
        if (TextUtils.isEmpty(raw)) {
            raw = cleanUrl(item.getVideo());
        }
        if (TextUtils.isEmpty(raw)) {
            return null;
        }
        if (isYoutubeish(raw)) {
            return null;
        }
        if (isHttpUrl(raw) || raw.startsWith("file")) {
            return ensureHttps(raw);
        }
        return null;
    }

    /** Primary + fallback URLs for {@link VideoShareHelper} OkHttp downloads. */
    public static final class ShareUrlPair {
        @Nullable public final String primary;
        @Nullable public final String alternate;

        public ShareUrlPair(@Nullable String primary, @Nullable String alternate) {
            this.primary = primary;
            this.alternate = alternate;
        }
    }

    /**
     * Prefer direct progressive files ({@code downloadUrl}, mp4 in {@code videos[]}) over
     * HLS {@code video_url} play URLs that Exo can stream but cannot be shared as one file.
     */
    public static ShareUrlPair resolveShareFileUrls(@Nullable ModelClass.Datum item) {
        if (item == null) {
            return new ShareUrlPair(null, null);
        }
        java.util.ArrayList<String> direct = new java.util.ArrayList<>();
        java.util.ArrayList<String> streamOnly = new java.util.ArrayList<>();
        maybeAddShareCandidate(direct, streamOnly, item.getDownloadUrl());
        maybeAddShareCandidate(direct, streamOnly, item.getVideoUrl());
        if (item.getVideos() != null) {
            for (Object o : item.getVideos()) {
                if (o instanceof String) {
                    maybeAddShareCandidate(direct, streamOnly, (String) o);
                } else if (o != null) {
                    maybeAddShareCandidate(direct, streamOnly, String.valueOf(o));
                }
            }
        }
        String primary = !direct.isEmpty() ? direct.get(0)
                : (!streamOnly.isEmpty() ? streamOnly.get(0) : null);
        String alternate = null;
        if (!direct.isEmpty() && direct.size() > 1) {
            alternate = direct.get(1);
        } else if (!direct.isEmpty() && !streamOnly.isEmpty()) {
            alternate = streamOnly.get(0);
        } else if (direct.isEmpty() && streamOnly.size() > 1) {
            alternate = streamOnly.get(1);
        }
        return new ShareUrlPair(primary, alternate);
    }

    private static void maybeAddShareCandidate(java.util.List<String> direct,
                                               java.util.List<String> streamOnly,
                                               @Nullable String url) {
        String c = cleanUrl(url);
        if (c == null || !isHttpUrl(c) || isYoutubeish(c)) {
            return;
        }
        c = ensureHttps(c);
        java.util.List<String> bucket = isHlsOrManifest(c) ? streamOnly : direct;
        if (!bucket.contains(c)) {
            bucket.add(c);
        }
    }

    private static boolean isHlsOrManifest(String url) {
        String u = url.toLowerCase(Locale.US);
        return u.contains(".m3u8") || u.contains(".m3u") || u.contains("manifest.mpd");
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

    /** Exposed for {@link EdgeFeedPostNormalizer} — never treat avatars as video posters. */
    public static boolean isUsableVideoPosterForNormalize(
            @Nullable String url, @Nullable String writerAvatar) {
        return isUsableVideoPoster(url, writerAvatar);
    }

    private static boolean isUsableVideoPoster(
      @Nullable String url, @Nullable String writerAvatar) {
    if (TextUtils.isEmpty(url)) {
      return false;
    }
    if (AvatarImageHelper.isUnusablePhoto(url)) {
      return false;
    }
    if (!TextUtils.isEmpty(writerAvatar)) {
      String a = writerAvatar.trim();
      String u = url.trim();
      if (a.equalsIgnoreCase(u)
          || u.contains(a)
          || (a.length() > 12 && u.contains(a.substring(a.length() - 12)))) {
        return false;
      }
    }
    String lower = url.trim().toLowerCase(Locale.US);
    return !lower.contains("writer_profile")
        && !lower.contains("user_profile.")
        && !lower.contains("/profile_pic")
        && !lower.contains("profile_pic/");
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
        // Prefer shared helper (shorts / live / embed / watch / bare id).
        String viaHelper = YouTubeIdHelper.extract(url);
        if (viaHelper != null) {
            return viaHelper;
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
