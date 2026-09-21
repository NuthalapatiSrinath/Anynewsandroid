package com.anyNews.anynews.Utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared gallery URL cleanup for magazine / Special / News image galleries.
 * Drops blank slots that became empty WormDots pages and splash-logo placeholders.
 */
public final class GalleryImageUrls {

    private GalleryImageUrls() {}

    /**
     * Keep real URLs only: trim, drop null/empty/"null", drop exact consecutive
     * duplicates (cover repeated as page 0). Preserves order.
     */
    @NonNull
    public static ArrayList<String> sanitize(@Nullable List<String> raw) {
        ArrayList<String> out = new ArrayList<>();
        if (raw == null || raw.isEmpty()) {
            return out;
        }
        String prev = null;
        for (String u : raw) {
            if (u == null) {
                continue;
            }
            String t = u.trim();
            if (t.isEmpty() || "null".equalsIgnoreCase(t) || "undefined".equalsIgnoreCase(t)) {
                continue;
            }
            if (prev != null && prev.equals(t)) {
                continue;
            }
            out.add(t);
            prev = t;
        }
        return out;
    }

    @NonNull
    public static String fingerprint(@Nullable List<String> urls) {
        if (urls == null || urls.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder(urls.size() * 24);
        for (int i = 0; i < urls.size(); i++) {
            if (i > 0) {
                sb.append('\u0001');
            }
            String u = urls.get(i);
            sb.append(u != null ? u : "");
        }
        return sb.toString();
    }
}
