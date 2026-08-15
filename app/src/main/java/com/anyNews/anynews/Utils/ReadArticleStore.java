package com.anyNews.anynews.Utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Client-local read/seen store for online News | Read News.
 *
 * <p><b>Session vs committed:</b> flip settle only appends to a durable
 * <i>session viewed</i> (pending) set. On process start,
 * {@link #commitSessionViewedToReadOncePerProcess} merges pending into the
 * committed read set used by the Read tab.
 *
 * <p><b>News:</b> main online News is a full chronological List_All_Articles feed
 * (Play Store-era UX). Already-read posts stay in News; they are not stripped
 * and the server is not asked to densify via exclude_post_ids.
 *
 * <p><b>Read tab window:</b> list + badge only include last-read times within
 * {@link ReadHistoryConfig} (admin {@code read_history_hours}, default 24h).
 * Read paints local postcard cache only — no densify / fill-unread API.
 *
 * <p>No per-mark network or DB calls. Also updates {@link ArticleOpenLog} for offline
 * ranking when the user settles on a card. Caps growth with max ID count and
 * optional TTL. Lightweight postcard JSON is cached when marking so Read tab does
 * not re-fetch the feed.
 *
 * <p><b>Does not</b> soft-import the entire {@link ArticleOpenLog} open set — that blanked
 * News by treating every offline open as online-read without postcard bodies.
 */
public final class ReadArticleStore {

    private static final String TAG = "ReadArticleStore";
    private static final String PREF_READ_META = "read_article_meta_v1";
    /** Session viewed this process (and recovered after kill) — not yet used for News strip. */
    private static final String PREF_PENDING_META = "read_article_pending_v1";
    /** One-shot repair after removing ArticleOpenLog soft-migrate. */
    private static final String PREF_ORPHAN_SWEEP_DONE = "read_article_orphan_sweep_v2";
    private static final String DIR_NAME = "read_articles_v1";
    private static final String INDEX_FILE = "index.json";

    /** Hard cap — oldest read IDs drop first. */
    public static final int MAX_IDS = 300;
    /**
     * Cap for {@code exclude_post_ids} on List_All_Articles (new clients only).
     * Newest-read first so server densify can skip every locally committed read
     * (must match {@link #MAX_IDS} — a smaller cap left older committed ids off
     * the wire, server re-served them, and client strip emptied the page).
     */
    public static final int EXCLUDE_IDS_SEND_CAP = MAX_IDS;
    /** Drop entries older than this when pruning (30 days). */
    public static final long TTL_MS = 30L * 24L * 60L * 60L * 1000L;

    private static final Gson GSON = new Gson();
    private static final Type LIST_LONG = new TypeToken<List<Long>>() {}.getType();

    private static final Object LOCK = new Object();
    private static final ExecutorService IO =
            Executors.newSingleThreadExecutor(
                    r -> {
                        Thread t = new Thread(r, "ReadArticleStore-IO");
                        t.setPriority(Thread.NORM_PRIORITY - 1);
                        return t;
                    });
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    /** Committed postId → lastReadAtMs (insertion-ordered; newest at end). */
    @Nullable private static LinkedHashMap<Long, Long> memoryMeta;
    /** Session-viewed (pending) postId → atMs — not applied to News strip until commit. */
    @Nullable private static LinkedHashMap<Long, Long> memoryPending;
    /** True after first process-lifecycle commit of previous-session pending. */
    private static boolean processCommitDone;

    private ReadArticleStore() {}

    /**
     * Quiet session view: pending id + optional postcard. Does <b>not</b> join the
     * committed read set used by {@link #stripReadFromFeed} / exclude_post_ids.
     * Call {@link #commitSessionViewedToReadOncePerProcess} on next process start.
     */
    public static void markSessionViewed(
            @Nullable Context context, long postId, @Nullable ModelClass.Datum snapshot) {
        if (context == null || postId <= 0) return;
        // Stickies are position-only pins — still session-viewed / committed like normal posts.
        if (snapshot != null && isSyntheticFeedSlot(snapshot)) {
            return;
        }
        final Context app = context.getApplicationContext();
        final ModelClass.Datum copy = snapshot != null ? lightweightCopy(snapshot) : null;
        // Shared open log for offline ranking — still prefs-only, no network.
        try {
            ArticleOpenLog.markOpened(app, postId);
        } catch (Exception ignored) {
        }

        final long now = System.currentTimeMillis();
        synchronized (LOCK) {
            // Already committed from a prior session — keep meta fresh, no pending churn.
            LinkedHashMap<Long, Long> committed = loadMetaLocked(app);
            if (committed.containsKey(postId)) {
                committed.remove(postId);
                committed.put(postId, now);
                pruneLocked(app, committed, now);
                persistMetaLocked(app, committed);
            } else {
                LinkedHashMap<Long, Long> pending = loadPendingLocked(app);
                pending.remove(postId);
                pending.put(postId, now);
                pruneLocked(app, pending, now);
                persistPendingLocked(app, pending);
            }
        }
        if (copy != null) {
            IO.execute(() -> writePostFile(app, postId, copy));
        }
    }

    public static void markSessionViewed(@Nullable Context context, long postId) {
        markSessionViewed(context, postId, null);
    }

    /**
     * Permanently mark read (committed set). Prefer {@link #markSessionViewed} for flip
     * settle so News is not filtered mid-session. Kept for explicit permanent writers.
     */
    public static void markRead(
            @Nullable Context context, long postId, @Nullable ModelClass.Datum snapshot) {
        if (context == null || postId <= 0) return;
        // Stickies are position-only pins — still join committed read like normal posts.
        if (snapshot != null && isSyntheticFeedSlot(snapshot)) {
            return;
        }
        final Context app = context.getApplicationContext();
        final ModelClass.Datum copy = snapshot != null ? lightweightCopy(snapshot) : null;
        // Shared open log for offline ranking — still prefs-only, no network.
        try {
            ArticleOpenLog.markOpened(app, postId);
        } catch (Exception ignored) {
        }

        final long now = System.currentTimeMillis();
        synchronized (LOCK) {
            LinkedHashMap<Long, Long> pending = loadPendingLocked(app);
            pending.remove(postId);
            persistPendingLocked(app, pending);

            LinkedHashMap<Long, Long> meta = loadMetaLocked(app);
            meta.remove(postId);
            meta.put(postId, now);
            pruneLocked(app, meta, now);
            persistMetaLocked(app, meta);
        }
        if (copy != null) {
            IO.execute(() -> writePostFile(app, postId, copy));
        }
    }

    public static void markRead(@Nullable Context context, long postId) {
        markRead(context, postId, null);
    }

    /**
     * Once per process: merge durable session-pending into the committed read set, then
     * clear pending so this session starts clean for News strip/exclude.
     *
     * @return number of ids promoted into the committed set
     */
    public static int commitSessionViewedToReadOncePerProcess(@Nullable Context context) {
        if (context == null) return 0;
        synchronized (LOCK) {
            if (processCommitDone) return 0;
            int n = commitSessionViewedToReadLocked(context.getApplicationContext());
            processCommitDone = true;
            return n;
        }
    }

    /**
     * Merge all session-pending into committed and clear pending. Prefer
     * {@link #commitSessionViewedToReadOncePerProcess} so mid-session callers do not
     * re-filter News while the user is still flipping.
     */
    public static int commitSessionViewedToRead(@Nullable Context context) {
        if (context == null) return 0;
        synchronized (LOCK) {
            return commitSessionViewedToReadLocked(context.getApplicationContext());
        }
    }

    private static int commitSessionViewedToReadLocked(@NonNull Context app) {
        LinkedHashMap<Long, Long> pending = loadPendingLocked(app);
        if (pending.isEmpty()) {
            return 0;
        }
        LinkedHashMap<Long, Long> meta = loadMetaLocked(app);
        int promoted = 0;
        final long now = System.currentTimeMillis();
        // Preserve pending order (oldest first in LinkedHashMap) when merging.
        for (Map.Entry<Long, Long> e : pending.entrySet()) {
            Long id = e.getKey();
            if (id == null || id <= 0) continue;
            long at = e.getValue() != null && e.getValue() > 0 ? e.getValue() : now;
            meta.remove(id);
            meta.put(id, at);
            promoted++;
        }
        pending.clear();
        pruneLocked(app, meta, now);
        persistMetaLocked(app, meta);
        persistPendingLocked(app, pending);
        if (promoted > 0) {
            Logone.d(TAG + " commitSessionViewed promoted=" + promoted);
        }
        return promoted;
    }

    /** Committed permanent read only (not session pending). */
    public static boolean isRead(@Nullable Context context, long postId) {
        if (context == null || postId <= 0) return false;
        synchronized (LOCK) {
            return loadMetaLocked(context).containsKey(postId);
        }
    }

    /** True if viewed this session (or recovered pending) and not yet committed. */
    public static boolean isSessionPending(@Nullable Context context, long postId) {
        if (context == null || postId <= 0) return false;
        synchronized (LOCK) {
            return loadPendingLocked(context).containsKey(postId);
        }
    }

    @NonNull
    public static Set<Long> snapshotReadIds(@Nullable Context context) {
        if (context == null) return new LinkedHashSet<>();
        synchronized (LOCK) {
            return new LinkedHashSet<>(loadMetaLocked(context).keySet());
        }
    }

    /**
     * Newest-first read ids for server {@code exclude_post_ids} (capped).
     * Empty when store is empty — caller should omit the request field.
     */
    @NonNull
    public static List<Long> recentReadIdsNewestFirst(
            @Nullable Context context, int max) {
        if (context == null || max <= 0) return new ArrayList<>();
        List<Long> order;
        synchronized (LOCK) {
            LinkedHashMap<Long, Long> meta = loadMetaLocked(context);
            order = new ArrayList<>(meta.keySet());
        }
        List<Long> out = new ArrayList<>();
        for (int i = order.size() - 1; i >= 0 && out.size() < max; i--) {
            Long id = order.get(i);
            if (id != null && id > 0) {
                out.add(id);
            }
        }
        return out;
    }

    /**
     * Comma-separated post ids for List_All_Articles (or empty string to omit).
     */
    @NonNull
    public static String excludePostIdsCsv(
            @Nullable Context context, int max) {
        List<Long> ids = recentReadIdsNewestFirst(context, max);
        if (ids.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(ids.get(i));
        }
        return sb.toString();
    }

    /**
     * Read-tab / badge count: committed + session-pending postcards within the
     * admin read-history window (matches the Read flip UI). Does not force News strip.
     */
    public static int count(@Nullable Context context) {
        if (context == null) return 0;
        return listCached(context).size();
    }

    /** Committed + pending id count (may include id-only before postcard write). Prefer {@link #count}. */
    public static int countIds(@Nullable Context context) {
        if (context == null) return 0;
        synchronized (LOCK) {
            LinkedHashMap<Long, Long> c = loadMetaLocked(context);
            LinkedHashMap<Long, Long> p = loadPendingLocked(context);
            LinkedHashSet<Long> keys = new LinkedHashSet<>(c.keySet());
            keys.addAll(p.keySet());
            return keys.size();
        }
    }

    public static void countAsync(
            @Nullable Context context, @NonNull CountCallback callback) {
        if (context == null) {
            MAIN.post(() -> callback.onCounted(0));
            return;
        }
        final Context app = context.getApplicationContext();
        IO.execute(
                () -> {
                    final int n = count(app);
                    MAIN.post(() -> callback.onCounted(n));
                });
    }

    /**
     * Newest-first cached postcards for Read tab: session-pending first, then committed.
     * Skip ids with no cache body. Pending never strips News mid-session.
     * Only includes last-read timestamps within {@link ReadHistoryConfig} window
     * (default 24h; admin-tunable). Older committed reads still strip from News.
     */
    @NonNull
    public static List<ModelClass.Datum> listCached(@Nullable Context context) {
        if (context == null) return new ArrayList<>();
        final Context app = context.getApplicationContext();
        final long now = System.currentTimeMillis();
        final long cutoff = now - ReadHistoryConfig.getWindowMs(app);
        List<Long> order = new ArrayList<>();
        synchronized (LOCK) {
            LinkedHashMap<Long, Long> pending = loadPendingLocked(app);
            LinkedHashMap<Long, Long> meta = loadMetaLocked(app);
            // Pending newest first, then committed newest first (dedupe by skip).
            List<Long> pOrder = new ArrayList<>(pending.keySet());
            for (int i = pOrder.size() - 1; i >= 0; i--) {
                Long id = pOrder.get(i);
                if (id == null || id <= 0) continue;
                if (!isWithinReadWindow(pending.get(id), cutoff, now)) continue;
                order.add(id);
            }
            List<Long> cOrder = new ArrayList<>(meta.keySet());
            LinkedHashSet<Long> seen = new LinkedHashSet<>(order);
            for (int i = cOrder.size() - 1; i >= 0; i--) {
                Long id = cOrder.get(i);
                if (id == null || id <= 0 || seen.contains(id)) continue;
                if (!isWithinReadWindow(meta.get(id), cutoff, now)) continue;
                order.add(id);
                seen.add(id);
            }
        }
        List<ModelClass.Datum> out = new ArrayList<>();
        for (Long id : order) {
            if (id == null || id <= 0) continue;
            ModelClass.Datum d = readPostFile(app, id);
            if (d != null) {
                out.add(d);
            }
        }
        return out;
    }

    /**
     * True when {@code lastReadAtMs} is inside the Read-tab window ending at {@code now}.
     * Missing/zero timestamps count as "now" so pre-migration entries still appear once.
     */
    private static boolean isWithinReadWindow(
            @Nullable Long lastReadAtMs, long cutoffMs, long nowMs) {
        long at = lastReadAtMs != null && lastReadAtMs > 0 ? lastReadAtMs : nowMs;
        return at >= cutoffMs;
    }

    public static void listCachedAsync(
            @Nullable Context context, @NonNull ListCallback callback) {
        if (context == null) {
            MAIN.post(() -> callback.onListed(new ArrayList<>()));
            return;
        }
        final Context app = context.getApplicationContext();
        IO.execute(
                () -> {
                    final List<ModelClass.Datum> list = listCached(app);
                    MAIN.post(() -> callback.onListed(list));
                });
    }

    @Nullable
    public static ModelClass.Datum getCached(@Nullable Context context, long postId) {
        if (context == null || postId <= 0) return null;
        return readPostFile(context.getApplicationContext(), postId);
    }

    /**
     * True when this post should never be treated as a normal news card (ads, share shell, etc.).
     * Admin stickies are real cards — use {@link #isStickyArticle}, not this.
     */
    public static boolean isSyntheticFeedSlot(@Nullable ModelClass.Datum d) {
        if (d == null) return true;
        return shouldNeverStrip(d);
    }

    /**
     * Admin sticky / pinned feed rows ({@code sticky_position >= 0}). Server injects these
     * at fixed indices. Sticky is only a placement rule — they still follow normal
     * session-viewed / commit / News-strip / Read flow like any other post.
     */
    public static boolean isStickyArticle(@Nullable ModelClass.Datum d) {
        if (d == null || isSyntheticFeedSlot(d)) return false;
        Integer sp = d.getStickyPosition();
        return sp != null && sp >= 0;
    }

    /**
     * Count normal (non-sticky, non-synthetic) news cards — used for min-unread / skip-ahead.
     * Stickies do not satisfy "enough unread" alone (user may still need more pages).
     */
    public static int countRealArticles(@Nullable List<ModelClass.Datum> items) {
        if (items == null || items.isEmpty()) return 0;
        int n = 0;
        for (ModelClass.Datum d : items) {
            if (d == null || isSyntheticFeedSlot(d) || isStickyArticle(d)) continue;
            if (d.getPostId() == null || d.getPostId() <= 0) continue;
            n++;
        }
        return n;
    }

    /** Count admin stickies in a list. */
    public static int countStickyArticles(@Nullable List<ModelClass.Datum> items) {
        if (items == null || items.isEmpty()) return 0;
        int n = 0;
        for (ModelClass.Datum d : items) {
            if (isStickyArticle(d)) n++;
        }
        return n;
    }

    /**
     * True when list still has user-visible posts (normal OR sticky). Ads/share alone are not
     * enough to paint News — those trigger skip-ahead instead.
     */
    public static int countPaintableArticles(@Nullable List<ModelClass.Datum> items) {
        if (items == null || items.isEmpty()) return 0;
        int n = 0;
        for (ModelClass.Datum d : items) {
            if (d == null || isSyntheticFeedSlot(d)) continue;
            if (d.getPostId() == null || d.getPostId() <= 0) continue;
            n++;
        }
        return n;
    }

    /**
     * Real-article cadence for client-side ad re-placement after unread strip.
     * Matches server {@code feed_ad_every_n} (default 4 → seats at 4, 9, 14, …).
     * Prefer {@link #getAdEveryNReal()} — admin may override via splash/session.
     */
    public static final int DEFAULT_AD_EVERY_N_REAL = FeedAdConfig.DEFAULT_EVERY_N;

    /**
     * @deprecated Use {@link #getAdEveryNReal()} so admin overrides apply.
     * Kept as compile-time default (=4) for javadoc / legacy references.
     */
    @Deprecated
    public static final int AD_EVERY_N_REAL = DEFAULT_AD_EVERY_N_REAL;

    /** Admin-driven cadence (default 4). Safe without Context after splash. */
    public static int getAdEveryNReal() {
        return FeedAdConfig.getEveryN();
    }

    public static int getAdEveryNReal(@Nullable Context context) {
        return FeedAdConfig.getEveryN(context);
    }

    /**
     * Max one {@code share_app} shell per this many real articles after unread strip.
     * Unread filtering densifies server injects + failed-ad share fallbacks — cap spam.
     */
    public static final int SHARE_EVERY_N_REAL = 12;

    /**
     * In-feed "Rate AnyNews" stars card: place after this many real articles.
     * With ads every {@link #getAdEveryNReal()}, that lands around flip index ~10
     * (still early in the feed). Server historic inject used brittle
     * {@code overallSkip + postIndex == 23} and often never surfaced early.
     */
    public static final int RATING_AFTER_N_REAL = 8;

    /** Synthetic id from server {@code ratings-data.json} — stable across pages. */
    public static final long RATING_POST_ID = 901009L;

    /**
     * Strip articles in the <b>committed</b> read set only (main online News load /
     * pagination / reopen). Session-pending views are not stripped — they stay in News
     * until the next process start commits them.
     *
     * <p>Mutates list in place. Ads / share shells are kept. Admin stickies follow the same
     * committed-read strip as normal posts (unread stickies keep server positions).
     * Does not re-place ads — callers should use {@link #rebalanceFeedSlots} after
     * strip when list length / order changed.
     */
    public static void stripReadFromFeed(
            @Nullable Context context, @Nullable List<ModelClass.Datum> items) {
        stripReadFromFeed(context, items, null);
    }

    /**
     * Same as {@link #stripReadFromFeed(Context, List)}. Ids in {@code holdVisibleIds}
     * remain in the list even when committed-read (one-shot share / push deep-links when
     * the product explicitly wants them on News; Read-tab routing usually handles history).
     */
    public static void stripReadFromFeed(
            @Nullable Context context,
            @Nullable List<ModelClass.Datum> items,
            @Nullable Collection<Long> holdVisibleIds) {
        if (context == null || items == null || items.isEmpty()) return;
        Set<Long> read;
        synchronized (LOCK) {
            // Committed only — never session pending.
            read = new LinkedHashSet<>(loadMetaLocked(context).keySet());
        }
        if (read.isEmpty()) return;
        final Set<Long> hold = (holdVisibleIds == null || holdVisibleIds.isEmpty())
                ? null
                : new LinkedHashSet<>(holdVisibleIds);
        for (int i = items.size() - 1; i >= 0; i--) {
            ModelClass.Datum d = items.get(i);
            if (d == null || d.getPostId() == null) continue;
            long id = d.getPostId();
            if (id <= 0) continue;
            if (shouldNeverStrip(d)) continue;
            if (hold != null && hold.contains(id)) continue;
            // Stickies strip when committed read (same as normal posts).
            if (read.contains(id)) {
                items.remove(i);
            }
        }
    }

    /**
     * Collect admin stickies from a feed list (stable by post id, keep latest row).
     */
    @NonNull
    public static ArrayList<ModelClass.Datum> extractStickyArticles(
            @Nullable List<ModelClass.Datum> items) {
        ArrayList<ModelClass.Datum> out = new ArrayList<>();
        if (items == null || items.isEmpty()) return out;
        LinkedHashMap<Long, ModelClass.Datum> byId = new LinkedHashMap<>();
        for (ModelClass.Datum d : items) {
            if (!isStickyArticle(d) || d.getPostId() == null) continue;
            byId.put(d.getPostId(), d);
        }
        out.addAll(byId.values());
        return out;
    }

    /**
     * Place <b>read</b> sticky postcards at admin {@code sticky_position} among the Read list.
     * Unread stickies are never injected — only postcards already in the local read store
     * (session-pending + committed) that are sticky (or match remembered sticky ids).
     * Remembered feed stickies supply/refresh {@code sticky_position} so Read matches News pins.
     *
     * @param readNewestFirst newest-first read postcards
     * @param stickies        remembered stickies from main feed (server positions)
     */
    @NonNull
    public static ArrayList<ModelClass.Datum> buildReadListWithStickies(
            @Nullable List<ModelClass.Datum> readNewestFirst,
            @Nullable List<ModelClass.Datum> stickies) {
        // Admin position metadata by id (may outlive a single fetch).
        LinkedHashMap<Long, ModelClass.Datum> stickyMeta = new LinkedHashMap<>();
        for (ModelClass.Datum s : extractStickyArticles(stickies)) {
            if (s != null && s.getPostId() != null && s.getPostId() > 0) {
                stickyMeta.put(s.getPostId(), s);
            }
        }

        ArrayList<ModelClass.Datum> normals = new ArrayList<>();
        ArrayList<ModelClass.Datum> pins = new ArrayList<>();
        if (readNewestFirst != null) {
            for (ModelClass.Datum d : readNewestFirst) {
                if (d == null || d.getPostId() == null || d.getPostId() <= 0) continue;
                long id = d.getPostId();
                boolean sticky =
                        isStickyArticle(d) || stickyMeta.containsKey(id);
                if (!sticky) {
                    normals.add(d);
                    continue;
                }
                // Prefer postcard body; apply latest admin pin index when remembered.
                ModelClass.Datum pin = d;
                ModelClass.Datum meta = stickyMeta.get(id);
                if (meta != null && meta.getStickyPosition() != null) {
                    if (pin.getStickyPosition() == null
                            || !pin.getStickyPosition().equals(meta.getStickyPosition())) {
                        try {
                            pin = lightweightCopy(d);
                            if (pin == null) pin = d;
                            pin.setStickyPosition(meta.getStickyPosition());
                        } catch (Exception ignored) {
                            pin = d;
                            try {
                                pin.setStickyPosition(meta.getStickyPosition());
                            } catch (Exception ignored2) {
                            }
                        }
                    }
                }
                pins.add(pin);
            }
        }
        if (pins.isEmpty()) {
            return normals;
        }
        // Sort by sticky_position ascending so inserts don't scramble each other.
        pins.sort(
                (a, b) -> {
                    int pa = a.getStickyPosition() != null ? a.getStickyPosition() : 0;
                    int pb = b.getStickyPosition() != null ? b.getStickyPosition() : 0;
                    return Integer.compare(pa, pb);
                });
        ArrayList<ModelClass.Datum> out = new ArrayList<>(normals);
        for (ModelClass.Datum sticky : pins) {
            if (sticky == null || sticky.getPostId() == null) continue;
            long sid = sticky.getPostId();
            // Avoid double if same id already present among normals.
            boolean exists = false;
            for (ModelClass.Datum d : out) {
                if (d != null && d.getPostId() != null && d.getPostId() == sid) {
                    exists = true;
                    break;
                }
            }
            if (exists) continue;
            int pos = sticky.getStickyPosition() != null ? sticky.getStickyPosition() : 0;
            if (pos < 0) pos = 0;
            if (pos > out.size()) pos = out.size();
            out.add(pos, sticky);
        }
        return out;
    }

    /** True when this row is a synthetic native/web/vast ad slot (not share/ratings). */
    public static boolean isAdFeedSlot(@Nullable ModelClass.Datum d) {
        if (d == null) return false;
        String type = d.getPostType();
        if (type == null) return false;
        return type.equalsIgnoreCase("ad")
                || type.equalsIgnoreCase("web_ads")
                || type.equalsIgnoreCase("google_vasttag");
    }

    /** True when this row is the "Share AnyNews with friends" shell. */
    public static boolean isShareFeedSlot(@Nullable ModelClass.Datum d) {
        if (d == null) return false;
        String type = d.getPostType();
        return type != null && type.equalsIgnoreCase("share_app");
    }

    /**
     * Soft-timeout demotion keeps {@code ad_code} / {@code ad_req_position} on the
     * share_app row so a late fill can restore it. Organic share promos have no unit.
     */
    public static boolean isDemotedAdShareShell(@Nullable ModelClass.Datum d) {
        if (!isShareFeedSlot(d)) return false;
        try {
            String code = d.getAd_code();
            if (code == null || code.trim().isEmpty()) {
                code = d.getNext_req_ad_code();
            }
            return code != null && !code.trim().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /** True when this row is the in-feed Rate AnyNews (stars) card. */
    public static boolean isRatingsFeedSlot(@Nullable ModelClass.Datum d) {
        if (d == null) return false;
        String type = d.getPostType();
        return type != null && type.equalsIgnoreCase("ratings");
    }

    /**
     * Ads at every {@link #getAdEveryNReal()} reals + share_app throttle in one pass.
     * Prefer this after unread strip / soft-remove / tab restore.
     * Does not place ratings — callers should use {@link #ensureRatingsSlot} after
     * (needs Context for rated / daily-seen prefs).
     */
    public static boolean rebalanceFeedSlots(
            @Nullable List<ModelClass.Datum> items,
            @Nullable ModelClass.Datum adTemplate) {
        boolean adChanged = rebalanceAdSlots(items, adTemplate);
        boolean shareChanged = throttleShareSlots(items);
        return adChanged || shareChanged;
    }

    /**
     * Ensure one {@code post_type=ratings} card sits after {@link #RATING_AFTER_N_REAL}
     * real articles. Moves a server-injected late ratings row up; invents a shell
     * when the API never sent one.
     *
     * <p>Skips when the user already submitted a rating, or already saw the card
     * today (local day key — mirrors server ViewsModel same-day gate). Removes
     * leftover ratings rows when ineligible.
     *
     * @return true if list mutated
     */
    public static boolean ensureRatingsSlot(
            @Nullable Context context, @Nullable List<ModelClass.Datum> items) {
        if (items == null || items.isEmpty()) return false;

        final boolean eligible = context != null && isRatingsPromptEligible(context);

        ArrayList<ModelClass.Datum> existing = new ArrayList<>(2);
        for (int i = items.size() - 1; i >= 0; i--) {
            ModelClass.Datum d = items.get(i);
            if (isRatingsFeedSlot(d)) {
                existing.add(0, d);
                items.remove(i);
            }
        }

        if (!eligible) {
            // Drop any server/late ratings when user already rated or saw today.
            return !existing.isEmpty();
        }

        int paintableTotal = countPaintableArticles(items);
        // Need at least one full ad-block of stories before showing the rate card.
        final int adEveryN = getAdEveryNReal(context);
        final int minPaintables = Math.min(RATING_AFTER_N_REAL, adEveryN);
        if (paintableTotal < minPaintables) {
            // Not enough content yet — put any server row back (before feed_end)
            // so pagination can move it to the target once paintables grow.
            if (existing.isEmpty()) {
                return false;
            }
            int at = items.size();
            for (int i = 0; i < items.size(); i++) {
                if (FeedEndHelper.isFeedEnd(items.get(i))) {
                    at = i;
                    break;
                }
            }
            items.add(Math.min(at, items.size()), existing.get(0));
            return true;
        }

        ModelClass.Datum shell = !existing.isEmpty() ? existing.get(0) : newRatingsShell();
        if (shell == null) return !existing.isEmpty();

        // Prefer after the 8th story. On a short first page, place after the last
        // paintable; later pages move it up to N.
        final int targetAfter = Math.min(RATING_AFTER_N_REAL, paintableTotal);

        int insertAt = items.size();
        int paintablesSeen = 0;
        for (int i = 0; i < items.size(); i++) {
            ModelClass.Datum d = items.get(i);
            if (d == null) continue;
            if (FeedEndHelper.isFeedEnd(d)) {
                insertAt = i;
                break;
            }
            // Count normal + sticky stories (same as user-visible news cards).
            // Ads/share/ratings are synthetic and do not advance the cadence.
            if (!isSyntheticFeedSlot(d)
                    && d.getPostId() != null
                    && d.getPostId() > 0) {
                paintablesSeen++;
                if (paintablesSeen >= targetAfter) {
                    insertAt = i + 1;
                    break;
                }
            }
        }
        if (insertAt > items.size()) {
            insertAt = items.size();
        }
        items.add(Math.min(insertAt, items.size()), shell);
        return true;
    }

    /** Local eligibility: not submitted, and not shown earlier today. */
    public static boolean isRatingsPromptEligible(@NonNull Context context) {
        try {
            if (SharedPrefsData.getBool(
                    context, Constants.APP_RATING_SUBMITTED, Constants.PREF_NAME)) {
                return false;
            }
            String seenDay = SharedPrefsData.getString(
                    context, Constants.APP_RATING_SEEN_DAY, Constants.PREF_NAME);
            String today = currentLocalDayKey();
            return seenDay == null || seenDay.isEmpty() || !today.equals(seenDay);
        } catch (Exception e) {
            return true;
        }
    }

    /** Mark in-feed ratings card as shown for the local calendar day. */
    public static void markRatingsPromptSeen(@Nullable Context context) {
        if (context == null) return;
        try {
            SharedPrefsData.putString(
                    context,
                    Constants.APP_RATING_SEEN_DAY,
                    currentLocalDayKey(),
                    Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
    }

    @NonNull
    private static String currentLocalDayKey() {
        try {
            java.text.SimpleDateFormat fmt =
                    new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US);
            return fmt.format(new java.util.Date());
        } catch (Exception e) {
            return String.valueOf(System.currentTimeMillis() / 86_400_000L);
        }
    }

    @NonNull
    private static ModelClass.Datum newRatingsShell() {
        ModelClass.Datum d = new ModelClass.Datum();
        d.setPostId(RATING_POST_ID);
        d.setSubPostId(1L);
        d.setPostType("ratings");
        d.setTitle("Rate AnyNews");
        d.setDescription("");
        d.setMediaType("1");
        d.setStickyPosition(-1);
        return d;
    }

    /**
     * Keep at most one share_app per {@link #SHARE_EVERY_N_REAL} real articles.
     * Drops clustered shares left after unread strip. Always keeps at most one when
     * the list has zero real articles (avoids share-only spam trains).
     *
     * @return true if list mutated
     */
    public static boolean throttleShareSlots(@Nullable List<ModelClass.Datum> items) {
        if (items == null || items.isEmpty()) return false;
        int shareCount = 0;
        for (ModelClass.Datum d : items) {
            if (isShareFeedSlot(d)) shareCount++;
        }
        if (shareCount == 0) return false;

        final int realTotal = countRealArticles(items);
        // Cap allowed shares by real count; never more than one when no reals.
        final int maxShares =
                realTotal <= 0 ? 1 : Math.max(1, realTotal / SHARE_EVERY_N_REAL);
        if (shareCount <= maxShares && shareCount <= 1 && realTotal > 0) {
            // Single share among content — leave placement as server chose.
            return false;
        }
        if (shareCount <= maxShares && realTotal >= SHARE_EVERY_N_REAL) {
            // Already within budget with multiple only if density is ok — still
            // re-walk if consecutive shares exist.
            boolean consecutive = false;
            for (int i = 1; i < items.size(); i++) {
                if (isShareFeedSlot(items.get(i)) && isShareFeedSlot(items.get(i - 1))) {
                    consecutive = true;
                    break;
                }
            }
            if (!consecutive && shareCount <= maxShares) return false;
        }

        ArrayList<ModelClass.Datum> shares = new ArrayList<>();
        ArrayList<ModelClass.Datum> content = new ArrayList<>(items.size());
        for (ModelClass.Datum d : items) {
            if (d == null) continue;
            if (isShareFeedSlot(d)) {
                shares.add(d);
            } else {
                content.add(d);
            }
        }
        if (shares.isEmpty()) return false;

        ArrayList<ModelClass.Datum> rebuilt = new ArrayList<>(content.size() + maxShares + 1);
        int realsSeen = 0;
        int sharesPlaced = 0;
        for (ModelClass.Datum d : content) {
            rebuilt.add(d);
            // Ad/share cadence counts normal stories only — stickies keep relative placement.
            if (d != null
                    && !isSyntheticFeedSlot(d)
                    && !isStickyArticle(d)
                    && d.getPostId() != null
                    && d.getPostId() > 0) {
                realsSeen++;
                if (sharesPlaced < maxShares
                        && sharesPlaced < shares.size()
                        && realsSeen > 0
                        && realsSeen % SHARE_EVERY_N_REAL == 0) {
                    rebuilt.add(shares.get(sharesPlaced));
                    sharesPlaced++;
                }
            }
        }
        // Ensure one promo when we had shares but never hit the window yet.
        final int adEveryN = getAdEveryNReal();
        if (sharesPlaced == 0 && !shares.isEmpty() && realTotal >= adEveryN) {
            int r = 0;
            int insertAt = rebuilt.size();
            for (int i = 0; i < rebuilt.size(); i++) {
                ModelClass.Datum d = rebuilt.get(i);
                if (d != null
                        && !isSyntheticFeedSlot(d)
                        && !isStickyArticle(d)
                        && d.getPostId() != null
                        && d.getPostId() > 0) {
                    r++;
                    if (r >= adEveryN) {
                        insertAt = i + 1;
                        break;
                    }
                }
            }
            rebuilt.add(Math.min(insertAt, rebuilt.size()), shares.get(0));
            sharesPlaced = 1;
        } else if (sharesPlaced == 0 && !shares.isEmpty() && realTotal == 0) {
            // Share-only page mid skip-ahead: keep a single shell (caller should keep paging).
            rebuilt.add(shares.get(0));
            sharesPlaced = 1;
        }

        if (rebuilt.size() == items.size()) {
            boolean same = true;
            for (int i = 0; i < rebuilt.size(); i++) {
                if (rebuilt.get(i) != items.get(i)) {
                    same = false;
                    break;
                }
            }
            if (same) return false;
        }
        items.clear();
        items.addAll(rebuilt);
        return true;
    }

    /**
     * After unread filter / soft strip collapses the list, re-insert ad slots at the
     * product cadence (every {@link #getAdEveryNReal()} real cards → first ad at
     * index N).
     * Keeps original ad metadata ({@code ad_req_position}, codes) so already-loaded
     * native ads still match. Clones the template when needed so skip-ahead pages
     * that discarded later ads still get a full cadence.
     * Used by online News and Video News flip feeds.
     *
     * @param items       feed list to mutate in place
     * @param adTemplate  optional fallback ad shell when the list lost all ad rows
     * @return true if the list order/size changed
     */
    public static boolean rebalanceAdSlots(
            @Nullable List<ModelClass.Datum> items,
            @Nullable ModelClass.Datum adTemplate) {
        if (items == null || items.isEmpty()) return false;

        ArrayList<ModelClass.Datum> adPool = new ArrayList<>();
        ArrayList<ModelClass.Datum> content = new ArrayList<>(items.size());
        for (ModelClass.Datum d : items) {
            if (d == null) continue;
            if (isAdFeedSlot(d)) {
                adPool.add(d);
            } else if (isDemotedAdShareShell(d)) {
                // Soft fill-timeout left share_app with unit meta — reclaim as ad
                // so pagination rebalance restores income seats instead of keeping Share.
                try {
                    d.setPostType("ad");
                } catch (Exception ignored) {
                }
                adPool.add(d);
            } else {
                content.add(d);
            }
        }

        ModelClass.Datum template = adTemplate;
        if (template == null && !adPool.isEmpty()) {
            template = adPool.get(0);
        }
        // No ad shell and none in list → cannot invent creative/code; leave as-is.
        if (template == null || !isAdFeedSlot(template)) {
            return false;
        }

        int realCount = countRealArticles(content);
        final int adEveryN = getAdEveryNReal();
        // Need at least one full block of reals to place an ad (every-N pattern).
        if (realCount < adEveryN) {
            // Still drop stacked leading/trailing ads if strip left only clumped ads.
            if (adPool.isEmpty() && items.size() == content.size()) {
                return false;
            }
            // Fewer than N reals: keep content only (no ad first on a tiny feed).
            if (content.size() == items.size() && adPool.isEmpty()) {
                return false;
            }
            items.clear();
            items.addAll(content);
            return true;
        }

        ArrayList<ModelClass.Datum> rebuilt = new ArrayList<>(content.size() + realCount / adEveryN + 2);
        int realsSeen = 0;
        int adUsed = 0;
        for (ModelClass.Datum d : content) {
            rebuilt.add(d);
            // Every N normal non-sticky articles — do not let stickies displace ad cadence.
            if (d != null && !isSyntheticFeedSlot(d) && !isStickyArticle(d)
                    && d.getPostId() != null && d.getPostId() > 0) {
                realsSeen++;
                if (realsSeen % adEveryN == 0) {
                    ModelClass.Datum slot;
                    if (adUsed < adPool.size()) {
                        slot = adPool.get(adUsed);
                    } else {
                        slot = cloneAdShell(template);
                        if (slot == null) slot = template;
                    }
                    if (slot != null) {
                        // Align position metadata with list index so native preload
                        // and Flipper matching hit this shell (not stale 6/14 seeds).
                        try {
                            int at = rebuilt.size();
                            slot.setAd_req_position(at);
                            slot.setNext_ad_req_position(at + adEveryN + 1);
                        } catch (Exception ignored) {
                        }
                        rebuilt.add(slot);
                        adUsed++;
                    }
                }
            }
        }

        // Unchanged length+order of non-ad content and same ad count/placement → skip churn.
        if (rebuilt.size() == items.size()) {
            boolean same = true;
            for (int i = 0; i < rebuilt.size(); i++) {
                if (rebuilt.get(i) != items.get(i)) {
                    same = false;
                    break;
                }
            }
            if (same) return false;
        }

        items.clear();
        items.addAll(rebuilt);
        return true;
    }

    /**
     * First ad slot in a page/list usable as a rebalance template, or null.
     */
    @Nullable
    public static ModelClass.Datum firstAdTemplate(
            @Nullable List<ModelClass.Datum> items) {
        if (items == null) return null;
        for (ModelClass.Datum d : items) {
            if (isAdFeedSlot(d)) {
                ModelClass.Datum copy = cloneAdShell(d);
                return copy != null ? copy : d;
            }
        }
        return null;
    }

    @Nullable
    private static ModelClass.Datum cloneAdShell(@Nullable ModelClass.Datum src) {
        if (src == null) return null;
        try {
            String json = GSON.toJson(src);
            if (TextUtils.isEmpty(json)) return null;
            return GSON.fromJson(json, ModelClass.Datum.class);
        } catch (Exception e) {
            return null;
        }
    }

    /** True when any of the given post ids is already read. */
    public static boolean anyRead(
            @Nullable Context context, @Nullable Collection<Long> postIds) {
        if (context == null || postIds == null || postIds.isEmpty()) return false;
        synchronized (LOCK) {
            LinkedHashMap<Long, Long> meta = loadMetaLocked(context);
            for (Long id : postIds) {
                if (id != null && id > 0 && meta.containsKey(id)) return true;
            }
        }
        return false;
    }

    public interface ListCallback {
        void onListed(@NonNull List<ModelClass.Datum> items);
    }

    public interface CountCallback {
        void onCounted(int count);
    }

    // --- internals ---

    private static boolean shouldNeverStrip(@NonNull ModelClass.Datum d) {
        String type = d.getPostType();
        if (type == null) return false;
        return type.equalsIgnoreCase("ad")
                || type.equalsIgnoreCase("share_app")
                || type.equalsIgnoreCase("ratings")
                || type.equalsIgnoreCase("web_ads")
                || type.equalsIgnoreCase("web")
                || type.equalsIgnoreCase("google_vasttag")
                || type.equalsIgnoreCase(FeedEndHelper.POST_TYPE);
    }

    @NonNull
    private static LinkedHashMap<Long, Long> loadMetaLocked(@NonNull Context context) {
        if (memoryMeta != null) return memoryMeta;
        LinkedHashMap<Long, Long> map =
                loadIdAtMapFromPref(context, PREF_READ_META);
        try {
            // Soft-migrate from ArticleOpenLog intentionally removed.
            // Importing all offline open IDs blanked online News and inflated badges
            // without postcard bodies for the Read tab.
            maybeSweepOrphanIdsLocked(context, map);
        } catch (Exception e) {
            Logone.w(e, TAG + " load meta failed");
        }
        pruneLocked(context, map, System.currentTimeMillis());
        memoryMeta = map;
        return map;
    }

    @NonNull
    private static LinkedHashMap<Long, Long> loadPendingLocked(@NonNull Context context) {
        if (memoryPending != null) return memoryPending;
        LinkedHashMap<Long, Long> map =
                loadIdAtMapFromPref(context, PREF_PENDING_META);
        pruneLocked(context, map, System.currentTimeMillis());
        memoryPending = map;
        return map;
    }

    @NonNull
    private static LinkedHashMap<Long, Long> loadIdAtMapFromPref(
            @NonNull Context context, @NonNull String prefKey) {
        LinkedHashMap<Long, Long> map = new LinkedHashMap<>();
        try {
            String raw =
                    SharedPrefsData.getString(
                            context.getApplicationContext(), prefKey, Constants.PREF_NAME);
            if (!TextUtils.isEmpty(raw)) {
                JSONArray arr = new JSONArray(raw);
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject o = arr.optJSONObject(i);
                    if (o == null) continue;
                    long id = o.optLong("id", 0L);
                    long at = o.optLong("at", 0L);
                    if (id > 0) {
                        if (at <= 0) at = System.currentTimeMillis();
                        map.put(id, at);
                    }
                }
            }
        } catch (Exception e) {
            Logone.w(e, TAG + " load " + prefKey + " failed");
        }
        return map;
    }

    private static void persistPendingLocked(
            @NonNull Context context, @NonNull LinkedHashMap<Long, Long> map) {
        try {
            JSONArray arr = new JSONArray();
            for (Map.Entry<Long, Long> e : map.entrySet()) {
                if (e.getKey() == null || e.getKey() <= 0) continue;
                JSONObject o = new JSONObject();
                o.put("id", e.getKey());
                o.put("at", e.getValue() != null ? e.getValue() : 0L);
                arr.put(o);
            }
            SharedPrefsData.putString(
                    context.getApplicationContext(),
                    PREF_PENDING_META,
                    arr.toString(),
                    Constants.PREF_NAME);
        } catch (Exception e) {
            Logone.w(e, TAG + " persist pending failed");
        }
    }

    /**
     * One-shot: drop id-only entries that have no postcard file (typical soft-migrate damage).
     * Keeps true settled reads that wrote {@code <id>.json}.
     */
    private static void maybeSweepOrphanIdsLocked(
            @NonNull Context context, @NonNull LinkedHashMap<Long, Long> map) {
        if (map.isEmpty()) {
            try {
                SharedPrefsData.putBool(
                        context.getApplicationContext(),
                        PREF_ORPHAN_SWEEP_DONE,
                        true,
                        Constants.PREF_NAME);
            } catch (Exception ignored) {
            }
            return;
        }
        boolean done = false;
        try {
            done =
                    SharedPrefsData.getBool(
                            context.getApplicationContext(),
                            PREF_ORPHAN_SWEEP_DONE,
                            Constants.PREF_NAME,
                            false);
        } catch (Exception ignored) {
        }
        if (done) return;

        Context app = context.getApplicationContext();
        List<Long> orphans = new ArrayList<>();
        for (Long id : map.keySet()) {
            if (id == null || id <= 0) continue;
            File f = new File(dir(app), id + ".json");
            if (!f.exists() || f.length() <= 0) {
                orphans.add(id);
            }
        }
        if (!orphans.isEmpty()) {
            for (Long id : orphans) {
                map.remove(id);
            }
            persistMetaLocked(app, map);
            Logone.d(TAG + " orphan sweep removed " + orphans.size() + " id-only reads");
        }
        try {
            SharedPrefsData.putBool(app, PREF_ORPHAN_SWEEP_DONE, true, Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
    }

    private static void pruneLocked(
            @NonNull Context context,
            @NonNull LinkedHashMap<Long, Long> map,
            long now) {
        // TTL
        Iterator<Map.Entry<Long, Long>> it = map.entrySet().iterator();
        List<Long> dropped = new ArrayList<>();
        while (it.hasNext()) {
            Map.Entry<Long, Long> e = it.next();
            Long at = e.getValue();
            if (at != null && at > 0 && (now - at) > TTL_MS) {
                dropped.add(e.getKey());
                it.remove();
            }
        }
        // Max size — drop oldest keys first
        while (map.size() > MAX_IDS) {
            Iterator<Long> keys = map.keySet().iterator();
            if (!keys.hasNext()) break;
            Long first = keys.next();
            keys.remove();
            if (first != null) dropped.add(first);
        }
        if (!dropped.isEmpty()) {
            IO.execute(() -> {
                for (Long id : dropped) {
                    if (id != null) deletePostFile(context.getApplicationContext(), id);
                }
            });
        }
    }

    private static void persistMetaLocked(
            @NonNull Context context, @NonNull LinkedHashMap<Long, Long> map) {
        try {
            JSONArray arr = new JSONArray();
            for (Map.Entry<Long, Long> e : map.entrySet()) {
                if (e.getKey() == null || e.getKey() <= 0) continue;
                JSONObject o = new JSONObject();
                o.put("id", e.getKey());
                o.put("at", e.getValue() != null ? e.getValue() : 0L);
                arr.put(o);
            }
            SharedPrefsData.putString(
                    context.getApplicationContext(),
                    PREF_READ_META,
                    arr.toString(),
                    Constants.PREF_NAME);
        } catch (Exception e) {
            Logone.w(e, TAG + " persist meta failed");
        }
    }

    /** Compact postcard — fields needed for Read list + BookmarkReader. */
    @Nullable
    private static ModelClass.Datum lightweightCopy(@Nullable ModelClass.Datum src) {
        if (src == null || src.getPostId() == null || src.getPostId() <= 0) return null;
        try {
            // Gson round-trip is safest given ModelClass size; still local-only.
            String json = GSON.toJson(src);
            if (TextUtils.isEmpty(json)) return null;
            return GSON.fromJson(json, ModelClass.Datum.class);
        } catch (Exception e) {
            return null;
        }
    }

    @NonNull
    private static File dir(@NonNull Context app) {
        File d = new File(app.getFilesDir(), DIR_NAME);
        if (!d.exists()) {
            //noinspection ResultOfMethodCallIgnored
            d.mkdirs();
        }
        return d;
    }

    private static void writePostFile(
            @NonNull Context app, long postId, @NonNull ModelClass.Datum post) {
        try {
            File f = new File(dir(app), postId + ".json");
            File tmp = new File(dir(app), postId + ".json.tmp");
            try (OutputStreamWriter w =
                    new OutputStreamWriter(new FileOutputStream(tmp), StandardCharsets.UTF_8)) {
                GSON.toJson(post, w);
            }
            if (f.exists()) {
                //noinspection ResultOfMethodCallIgnored
                f.delete();
            }
            //noinspection ResultOfMethodCallIgnored
            tmp.renameTo(f);
            touchIndex(app, postId);
        } catch (Exception e) {
            Logone.w(e, TAG + " write post failed id=" + postId);
        }
    }

    @Nullable
    private static ModelClass.Datum readPostFile(@NonNull Context app, long postId) {
        File f = new File(dir(app), postId + ".json");
        if (!f.exists() || f.length() <= 0) return null;
        try (InputStreamReader r =
                new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8)) {
            return GSON.fromJson(r, ModelClass.Datum.class);
        } catch (Exception e) {
            return null;
        }
    }

    private static void deletePostFile(@NonNull Context app, long postId) {
        try {
            File f = new File(dir(app), postId + ".json");
            if (f.exists()) {
                //noinspection ResultOfMethodCallIgnored
                f.delete();
            }
            dropFromIndex(app, postId);
        } catch (Exception ignored) {
        }
    }

    private static void touchIndex(@NonNull Context app, long postId) {
        try {
            File idx = new File(dir(app), INDEX_FILE);
            List<Long> order = new ArrayList<>();
            if (idx.exists() && idx.length() > 0) {
                try (InputStreamReader r =
                        new InputStreamReader(new FileInputStream(idx), StandardCharsets.UTF_8)) {
                    List<Long> existing = GSON.fromJson(r, LIST_LONG);
                    if (existing != null) order.addAll(existing);
                }
            }
            order.remove(postId);
            order.add(postId);
            while (order.size() > MAX_IDS) {
                order.remove(0);
            }
            File tmp = new File(dir(app), INDEX_FILE + ".tmp");
            try (OutputStreamWriter w =
                    new OutputStreamWriter(new FileOutputStream(tmp), StandardCharsets.UTF_8)) {
                GSON.toJson(order, w);
            }
            if (idx.exists()) {
                //noinspection ResultOfMethodCallIgnored
                idx.delete();
            }
            //noinspection ResultOfMethodCallIgnored
            tmp.renameTo(idx);
        } catch (Exception ignored) {
        }
    }

    private static void dropFromIndex(@NonNull Context app, long postId) {
        try {
            File idx = new File(dir(app), INDEX_FILE);
            if (!idx.exists()) return;
            List<Long> order = new ArrayList<>();
            try (InputStreamReader r =
                    new InputStreamReader(new FileInputStream(idx), StandardCharsets.UTF_8)) {
                List<Long> existing = GSON.fromJson(r, LIST_LONG);
                if (existing != null) order.addAll(existing);
            }
            order.remove(postId);
            try (OutputStreamWriter w =
                    new OutputStreamWriter(new FileOutputStream(idx), StandardCharsets.UTF_8)) {
                GSON.toJson(order, w);
            }
        } catch (Exception ignored) {
        }
    }
}
