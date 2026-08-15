package com.anyNews.anynews.Utils;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Explicit offline disk files for article hero images (keyed by post_id).
 *
 * <p>JSON offline pack ({@link OfflineFeedStore}) only stores post payloads. Universal Image
 * Loader / Glide HTTP disk caches are best-effort and do not cover posts never scrolled into
 * view or that were evicted — which is why offline often falls through to the ANYNEWS splash
 * logo. This store downloads primary (and first gallery) images while online with a small
 * fixed concurrency so the CDN is not stormed.
 */
public final class OfflineImageStore {

    private static final String TAG = "OfflineImageStore";
    private static final String DIR_NAME = "offline_article_images";
    /** Cap concurrent downloads — thrifty against image CDN. */
    private static final int MAX_CONCURRENT = 2;
    /** Per-article images: primary + a couple of gallery/extras. */
    private static final int MAX_URLS_PER_POST = 3;
    private static final int CONNECT_TIMEOUT_MS = 15_000;
    private static final int READ_TIMEOUT_MS = 45_000;
    private static final long MIN_BYTES = 64L;

    private static final LinkedBlockingQueue<Job> QUEUE = new LinkedBlockingQueue<>();
    private static final Set<String> IN_FLIGHT_OR_QUEUED = ConcurrentHashMap.newKeySet();
    private static final AtomicInteger ACTIVE = new AtomicInteger(0);
    private static final Object PUMP_LOCK = new Object();

    private static final ExecutorService WORKERS =
            Executors.newFixedThreadPool(
                    MAX_CONCURRENT,
                    new ThreadFactory() {
                        private final AtomicInteger n = new AtomicInteger(0);

                        @Override
                        public Thread newThread(@NonNull Runnable r) {
                            Thread t = new Thread(r, "OfflineImage-" + n.incrementAndGet());
                            t.setPriority(Thread.NORM_PRIORITY - 1);
                            return t;
                        }
                    });

    private OfflineImageStore() {}

    /**
     * After offline pack merge: download missing hero images for retained posts and drop
     * files for posts no longer in the pack.
     */
    public static void syncPackImages(
            @Nullable Context context, @Nullable List<ModelClass.Datum> pack) {
        if (context == null || pack == null) return;
        final Context app = context.getApplicationContext();
        Set<Long> keep = new HashSet<>();
        for (ModelClass.Datum d : pack) {
            if (d == null || d.getPostId() == null || d.getPostId() <= 0) continue;
            if (isExcludedVideoPost(d)) continue;
            long id = d.getPostId();
            keep.add(id);
            enqueueArticle(app, d);
        }
        pruneExcept(app, keep);
    }

    /** Enqueue thrifty downloads for one article (idempotent if file already exists). */
    public static void enqueueArticle(@Nullable Context context, @Nullable ModelClass.Datum article) {
        if (context == null || article == null || article.getPostId() == null) return;
        long postId = article.getPostId();
        if (postId <= 0) return;
        // Half/full video + vast — not stored in offline pack; skip poster download traffic.
        if (isExcludedVideoPost(article)) return;
        if (!BookmarkStore.isNetworkAvailable(context)) return;

        List<String> urls = collectImageUrls(article, MAX_URLS_PER_POST);
        if (urls.isEmpty()) return;
        Context app = context.getApplicationContext();
        for (int i = 0; i < urls.size(); i++) {
            enqueue(app, postId, i, urls.get(i));
        }
    }

    /** Posts that never enter the offline news pack (see OfflineFeedStore). */
    private static boolean isExcludedVideoPost(@Nullable ModelClass.Datum d) {
        if (d == null) return true;
        String pt = d.getPostType();
        if (pt == null) return false;
        return pt.equalsIgnoreCase("normal_video")
                || pt.equalsIgnoreCase("full_video")
                || pt.equalsIgnoreCase("google_vasttag");
    }

    /**
     * Preferred display URI for UIL / Glide.
     * Prefers explicit offline file; when online falls back to remote; when offline
     * never returns a remote URL (avoids failed HTTP → ANYNEWS logo placeholder).
     */
    @Nullable
    public static String resolveDisplayUri(
            @Nullable Context context, @Nullable Long postId, @Nullable String remoteUrl) {
        return resolveDisplayUri(context, postId, 0, remoteUrl);
    }

    @Nullable
    public static String resolveDisplayUri(
            @Nullable Context context,
            @Nullable Long postId,
            int index,
            @Nullable String remoteUrl) {
        File local = fileIfReady(context, postId, index);
        if (local != null) {
            return UriFile.asFileUri(local);
        }
        // Secondary index miss: try primary slot (half-image style).
        if (index > 0) {
            File primary = fileIfReady(context, postId, 0);
            if (primary != null) return UriFile.asFileUri(primary);
        }
        final boolean online =
                context != null && BookmarkStore.isNetworkAvailable(context);
        if (!online) {
            // Offline: remote would fail and show ANYNEWS branding placeholder.
            return null;
        }
        if (!TextUtils.isEmpty(remoteUrl)) {
            String trimmed = remoteUrl.trim();
            try {
                MediaAccessReporter.get().recordUrl(trimmed);
            } catch (Exception ignored) {
            }
            return trimmed;
        }
        return null;
    }

    /**
     * Map a remote gallery list to display URIs preferring local offline files when present.
     * Keeps list size; empty remote slots stay empty.
     */
    @NonNull
    public static ArrayList<String> resolveGalleryUris(
            @Nullable Context context,
            @Nullable Long postId,
            @Nullable List<String> remoteImages) {
        ArrayList<String> out = new ArrayList<>();
        if (remoteImages == null || remoteImages.isEmpty()) return out;
        for (int i = 0; i < remoteImages.size(); i++) {
            String remote = remoteImages.get(i);
            String uri = resolveDisplayUri(context, postId, i, remote);
            out.add(uri != null ? uri : (remote != null ? remote : ""));
        }
        return out;
    }

    @Nullable
    public static File fileIfReady(
            @Nullable Context context, @Nullable Long postId, int index) {
        if (context == null || postId == null || postId <= 0) return null;
        File f = fileFor(context, postId, index);
        if (f.exists() && f.length() >= MIN_BYTES) return f;
        return null;
    }

    public static boolean hasPrimary(@Nullable Context context, @Nullable Long postId) {
        return fileIfReady(context, postId, 0) != null;
    }

    /**
     * First usable hero URL for normal_image / full_image / video thumb / poll background.
     * Order: images[0], thumbnail, background_image.
     */
    @Nullable
    public static String primaryImageUrl(@Nullable ModelClass.Datum d) {
        List<String> urls = collectImageUrls(d, 1);
        return urls.isEmpty() ? null : urls.get(0);
    }

    @NonNull
    public static List<String> collectImageUrls(@Nullable ModelClass.Datum d, int max) {
        LinkedHashSet<String> out = new LinkedHashSet<>();
        if (d == null || max <= 0) return new ArrayList<>();
        if (d.getImages() != null) {
            for (String u : d.getImages()) {
                if (isHttpUrl(u)) {
                    out.add(u.trim());
                    if (out.size() >= max) break;
                }
            }
        }
        if (out.size() < max && isHttpUrl(d.getThumbnail())) {
            out.add(d.getThumbnail().trim());
        }
        if (out.size() < max && isHttpUrl(d.getBackgroundImage())) {
            out.add(d.getBackgroundImage().trim());
        }
        return new ArrayList<>(out);
    }

    private static boolean isHttpUrl(@Nullable String u) {
        if (TextUtils.isEmpty(u)) return false;
        String t = u.trim();
        if (t.isEmpty() || "null".equalsIgnoreCase(t)) return false;
        String lower = t.toLowerCase(Locale.US);
        return lower.startsWith("http://") || lower.startsWith("https://");
    }

    private static void enqueue(
            @NonNull Context app, long postId, int index, @NonNull String url) {
        File dest = fileFor(app, postId, index);
        if (dest.exists() && dest.length() >= MIN_BYTES) return;
        String key = postId + "#" + index + "#" + url;
        if (!IN_FLIGHT_OR_QUEUED.add(key)) return;
        QUEUE.offer(new Job(app, postId, index, url, key));
        pump();
    }

    private static void pump() {
        synchronized (PUMP_LOCK) {
            while (ACTIVE.get() < MAX_CONCURRENT) {
                final Job job = QUEUE.poll();
                if (job == null) return;
                ACTIVE.incrementAndGet();
                WORKERS.execute(
                        () -> {
                            try {
                                download(job);
                            } finally {
                                IN_FLIGHT_OR_QUEUED.remove(job.key);
                                ACTIVE.decrementAndGet();
                                pump();
                            }
                        });
            }
        }
    }

    private static void download(@NonNull Job job) {
        File dest = fileFor(job.app, job.postId, job.index);
        if (dest.exists() && dest.length() >= MIN_BYTES) {
            if (job.index == 0) {
                OfflineFeedStore.notifyPackChanged();
            }
            return;
        }
        if (!BookmarkStore.isNetworkAvailable(job.app)) return;
        File tmp = new File(dest.getAbsolutePath() + ".tmp");
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(job.url).openConnection();
            conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
            conn.setReadTimeout(READ_TIMEOUT_MS);
            conn.setInstanceFollowRedirects(true);
            conn.setRequestProperty("User-Agent", "anyNews-Android-OfflineImage");
            conn.connect();
            int code = conn.getResponseCode();
            if (code >= 400) {
                Logone.v(TAG, "http " + code + " post=" + job.postId);
                // Primary hero failed hard — drop article from offline serving pack.
                if (job.index == 0 && code >= 400) {
                    OfflineFeedStore.removePost(job.app, job.postId);
                }
                return;
            }
            //noinspection ResultOfMethodCallIgnored
            dest.getParentFile().mkdirs();
            try (InputStream in = conn.getInputStream();
                    FileOutputStream out = new FileOutputStream(tmp, false)) {
                byte[] buf = new byte[8192];
                int n;
                long total = 0;
                while ((n = in.read(buf)) >= 0) {
                    out.write(buf, 0, n);
                    total += n;
                }
                out.flush();
                if (total < MIN_BYTES) {
                    //noinspection ResultOfMethodCallIgnored
                    tmp.delete();
                    if (job.index == 0) {
                        OfflineFeedStore.removePost(job.app, job.postId);
                    }
                    return;
                }
            }
            if (dest.exists()) {
                //noinspection ResultOfMethodCallIgnored
                dest.delete();
            }
            if (!tmp.renameTo(dest)) {
                try (InputStream in = new java.io.FileInputStream(tmp);
                        FileOutputStream out = new FileOutputStream(dest, false)) {
                    byte[] buf = new byte[8192];
                    int n;
                    while ((n = in.read(buf)) >= 0) out.write(buf, 0, n);
                }
                //noinspection ResultOfMethodCallIgnored
                tmp.delete();
            }
            Logone.v(TAG, "saved post=" + job.postId + " i=" + job.index + " bytes=" + dest.length());
            try {
                MediaAccessReporter.get().recordUrl(job.url);
            } catch (Exception ignored) {
            }
            // Primary ready → Offline News badge / serving count can include this story.
            if (job.index == 0 && dest.exists() && dest.length() >= MIN_BYTES) {
                OfflineFeedStore.notifyPackChanged();
            }
        } catch (Exception e) {
            Logone.v(TAG, "download fail post=" + job.postId + " " + e.getMessage());
            //noinspection ResultOfMethodCallIgnored
            if (tmp.exists()) tmp.delete();
            // Network blips should not permanently remove — leave candidate for retry next online.
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private static void pruneExcept(@NonNull Context app, @NonNull Set<Long> keepIds) {
        try {
            File dir = dir(app);
            File[] files = dir.listFiles();
            if (files == null) return;
            for (File f : files) {
                if (f == null || !f.isFile()) continue;
                String name = f.getName();
                if (name.endsWith(".tmp")) {
                    //noinspection ResultOfMethodCallIgnored
                    f.delete();
                    continue;
                }
                long id = parsePostId(name);
                if (id > 0 && !keepIds.contains(id)) {
                    //noinspection ResultOfMethodCallIgnored
                    f.delete();
                }
            }
        } catch (Exception e) {
            Logone.w(e, TAG + " pruneExcept");
        }
    }

    /** Files: {@code p123.img}, {@code p123_i1.img} */
    private static long parsePostId(@NonNull String name) {
        try {
            if (!name.startsWith("p")) return -1;
            int us = name.indexOf('_');
            int dot = name.lastIndexOf('.');
            String mid = name.substring(1, us > 0 ? us : (dot > 0 ? dot : name.length()));
            return Long.parseLong(mid);
        } catch (Exception e) {
            return -1;
        }
    }

    @NonNull
    private static File dir(@NonNull Context context) {
        File d = new File(context.getApplicationContext().getFilesDir(), DIR_NAME);
        if (!d.exists()) {
            //noinspection ResultOfMethodCallIgnored
            d.mkdirs();
        }
        return d;
    }

    @NonNull
    private static File fileFor(@NonNull Context context, long postId, int index) {
        String name = index <= 0 ? ("p" + postId + ".img") : ("p" + postId + "_i" + index + ".img");
        return new File(dir(context), name);
    }

    private static final class Job {
        final Context app;
        final long postId;
        final int index;
        final String url;
        final String key;

        Job(Context app, long postId, int index, String url, String key) {
            this.app = app;
            this.postId = postId;
            this.index = index;
            this.url = url;
            this.key = key;
        }
    }

    /** Tiny helper so callers do not need android.net.Uri import for absolute paths. */
    private static final class UriFile {
        static String asFileUri(@NonNull File f) {
            String path = f.getAbsolutePath();
            if (path.startsWith("/")) return "file://" + path;
            return "file:///" + path;
        }
    }
}
