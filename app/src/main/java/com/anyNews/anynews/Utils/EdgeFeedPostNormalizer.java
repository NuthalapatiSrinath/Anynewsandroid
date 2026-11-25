package com.anyNews.anynews.Utils;

import android.text.TextUtils;

import androidx.annotation.Nullable;

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
        final String writer = clean(d.getWriterProfile());
        if (TextUtils.isEmpty(clean(d.getThumbnail()))) {
            String thumb = clean(d.getVideoThumb());
            if (StatusVideoMedia.isUsableVideoPosterForNormalize(thumb, writer)) {
                d.setThumbnail(thumb);
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
        if (TextUtils.isEmpty(clean(d.getWriterName()))) {
            d.setWriterName("Journalist");
        } else {
            d.setWriterName(AutoNewsBylineHelper.displayWriterName(d.getWriterName()));
        }
        if (TextUtils.isEmpty(clean(d.getRole()))) {
            d.setRole("Reporter");
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