package com.anyNews.anynews.Utils;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Patches public edge / lean Mongo rows into the shape {@link FlipperAdapter}
 * expects (same aliases as {@code List_All_Articles} on the server).
 */
public final class EdgeFeedPostNormalizer {

    private EdgeFeedPostNormalizer() {
    }

    public static void normalizeAll(@Nullable List<ModelClass.Datum> items) {
        if (items == null) return;
        for (ModelClass.Datum d : items) {
            normalize(d);
        }
    }

    public static void normalize(@Nullable ModelClass.Datum d) {
        if (d == null) return;

        if (TextUtils.isEmpty(clean(d.getVideoUrl())) && !TextUtils.isEmpty(clean(d.getVideo()))) {
            d.setVideoUrl(clean(d.getVideo()));
        }
        if (d.getImages() == null || d.getImages().isEmpty()) {
            String hero = clean(d.getImage());
            if (TextUtils.isEmpty(hero)) {
                hero = clean(d.getFullImage());
            }
            if (!TextUtils.isEmpty(hero)) {
                ArrayList<String> imgs = new ArrayList<>(1);
                imgs.add(hero);
                d.setImages(imgs);
            }
        }
        final String writer = clean(d.getWriterProfile());
        final String videoUrl = clean(d.getVideoUrl());
        String thumb = clean(d.getThumbnail());
        if (!TextUtils.isEmpty(thumb)) {
            if (StatusVideoMedia.isStreamOnlyUrl(thumb)
                    || StatusVideoMedia.isGenericVideoPlaceholderUrl(thumb)
                    || (!TextUtils.isEmpty(videoUrl)
                    && thumb.equalsIgnoreCase(videoUrl))) {
                d.setThumbnail(null);
            }
        }
        if (TextUtils.isEmpty(clean(d.getThumbnail()))) {
            String thumbImage = clean(d.getThumbImage());
            if (StatusVideoMedia.isUsableVideoPosterForNormalize(thumbImage, writer)
                    && !StatusVideoMedia.isStreamOnlyUrl(thumbImage)) {
                d.setThumbnail(thumbImage);
            }
        }
        if (TextUtils.isEmpty(clean(d.getThumbnail()))) {
            String videoThumb = clean(d.getVideoThumb());
            if (StatusVideoMedia.isUsableVideoPosterForNormalize(videoThumb, writer)) {
                d.setThumbnail(videoThumb);
            }
        }
        if (TextUtils.isEmpty(clean(d.getThumbnail()))
                && d.getImages() != null
                && !d.getImages().isEmpty()) {
            String img0 = clean(d.getImages().get(0));
            if (StatusVideoMedia.isUsableVideoPosterForNormalize(img0, writer)
                    && !StatusVideoMedia.isStreamOnlyUrl(img0)) {
                d.setThumbnail(img0);
            }
        }
        if (TextUtils.isEmpty(clean(d.getVideoThumb())) && !TextUtils.isEmpty(clean(d.getThumbnail()))) {
            d.setVideoThumb(clean(d.getThumbnail()));
        }
        if (TextUtils.isEmpty(clean(d.getThumbnail()))) {
            String poster = StatusVideoMedia.resolveFeedVideoPosterUrl(d);
            if (!TextUtils.isEmpty(poster)) {
                d.setThumbnail(poster);
                if (TextUtils.isEmpty(clean(d.getVideoThumb()))) {
                    d.setVideoThumb(poster);
                }
            }
        }
        if (TextUtils.isEmpty(clean(d.getWriterName()))) {
            d.setWriterName("Journalist");
        } else {
            d.setWriterName(AutoNewsBylineHelper.displayWriterName(d.getWriterName()));
        }
        if (TextUtils.isEmpty(clean(d.getRole()))) {
            d.setRole("Reporter");
        }
        // Keep server writer_profile when present; only fill generic avatar when still empty.
        if (TextUtils.isEmpty(clean(d.getWriterProfile()))) {
            d.setWriterProfile(MediaUrl.DEFAULT_PROFILE);
        }
    }

    @Nullable
    private static String clean(@Nullable String raw) {
        if (raw == null) return null;
        String t = raw.trim();
        if (t.isEmpty() || "null".equalsIgnoreCase(t) || "undefined".equalsIgnoreCase(t)) {
            return null;
        }
        return t;
    }
}