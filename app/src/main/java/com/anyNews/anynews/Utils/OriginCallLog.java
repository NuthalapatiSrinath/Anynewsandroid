package com.anyNews.anynews.Utils;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

import okhttp3.HttpUrl;

/**
 * In-memory log of outbound API calls that hit the API host.
 * Public-edge GETs ({@code /public/feed}, splash, daily_updates) are marked
 * {@code edge}; everything else is {@code origin} so the footer debug page
 * can show what still touches Node.
 */
public final class OriginCallLog {

    public static final int MAX_ENTRIES = 200;

    public static final class Entry {
        public long atMs;
        @NonNull public final String method;
        @NonNull public final String path;
        @NonNull public final String kind; // edge | origin
        public int count;

        Entry(long atMs, @NonNull String method, @NonNull String path, @NonNull String kind) {
            this.atMs = atMs;
            this.method = method;
            this.path = path;
            this.kind = kind;
            this.count = 1;
        }

        @NonNull
        public String whenLabel() {
            return new SimpleDateFormat("HH:mm:ss", Locale.US).format(new Date(atMs));
        }

        @NonNull
        public String line() {
            String c = count > 1 ? (" ×" + count) : "";
            return whenLabel() + "  " + method + "  " + path + "  [" + kind + "]" + c;
        }
    }

    private static final CopyOnWriteArrayList<Entry> ENTRIES = new CopyOnWriteArrayList<>();

    private OriginCallLog() {
    }

    public static void record(
            @Nullable HttpUrl url,
            @Nullable String method) {
        if (url == null) return;
        try {
            String path = url.encodedPath();
            if (TextUtils.isEmpty(path)) return;
            // Skip noisy beacon that this page itself would trigger.
            if (path.toLowerCase(Locale.US).contains("edge_origin_event")) return;

            String m = TextUtils.isEmpty(method) ? "?" : method.toUpperCase(Locale.US);
            String kind = isPublicEdgePath(path) ? "edge" : "origin";
            String query = url.encodedQuery();
            String full = TextUtils.isEmpty(query) ? path : (path + "?" + query);
            if (full.length() > 160) full = full.substring(0, 160);

            // Coalesce consecutive identical method+path (ignore query for key).
            String keyPath = normalizeKey(path);
            if (!ENTRIES.isEmpty()) {
                Entry last = ENTRIES.get(ENTRIES.size() - 1);
                if (last.method.equals(m)
                        && normalizeKey(last.path).equals(keyPath)
                        && last.kind.equals(kind)) {
                    last.count += 1;
                    last.atMs = System.currentTimeMillis();
                    // path may include query on first; keep first
                    return;
                }
            }

            ENTRIES.add(new Entry(System.currentTimeMillis(), m, full, kind));
            while (ENTRIES.size() > MAX_ENTRIES) {
                ENTRIES.remove(0);
            }
        } catch (Exception ignored) {
            /* never break traffic */
        }
    }

    @NonNull
    public static ArrayList<Entry> snapshot() {
        return new ArrayList<>(ENTRIES);
    }

    public static void clear() {
        ENTRIES.clear();
    }

    public static int originCount() {
        int n = 0;
        for (Entry e : ENTRIES) {
            if ("origin".equals(e.kind)) n += e.count;
        }
        return n;
    }

    public static int edgeCount() {
        int n = 0;
        for (Entry e : ENTRIES) {
            if ("edge".equals(e.kind)) n += e.count;
        }
        return n;
    }

    static boolean isPublicEdgePath(@NonNull String path) {
        String lower = path.toLowerCase(Locale.US);
        return lower.contains("/public/feed")
                || lower.contains("/public/splash_config")
                || lower.contains("/public/daily_updates");
    }

    @NonNull
    private static String normalizeKey(@NonNull String pathOrFull) {
        int q = pathOrFull.indexOf('?');
        String p = q >= 0 ? pathOrFull.substring(0, q) : pathOrFull;
        return p.toLowerCase(Locale.US);
    }
}
