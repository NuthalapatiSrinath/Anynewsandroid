package com.anyNews.anynews.Utils;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Persistent offline main-feed pack for cold open with Wi‑Fi/data off.
 *
 * <p><b>Hard caps (never unbounded growth):</b>
 * <ul>
 *   <li>Main pack: max {@link #getMainCap} articles (admin {@code offline_news_pack_max},
 *       default {@link #DEFAULT_PACK_MAX}=24; absolute hard ceiling {@link #HARD_CAP_ABS}=50).
 *       Over-cap → evict already-opened first, then oldest by published/saved time.</li>
 *   <li>Stale TTL: articles older than {@link #STALE_AGE_MS} dropped on load/capture.</li>
 *   <li>Special: offline paint blocked; writes disabled ({@link #SPECIAL_CAP} = 0).
 *       Special stays online-only.</li>
 * </ul>
 *
 * <p><b>Media-gated serving set:</b> disk JSON may hold candidates while images download,
 * but {@link #load}, {@link #loadForOfflinePaint}, and {@link #mainPackCount} only expose
 * posts with a successfully cached offline hero image ({@link OfflineImageStore#hasPrimary}).
 * Posts with no downloadable image URL never enter the pack. Offline News badge counts only
 * stories that paint with real media (no ANYNEWS placeholders).
 *
 * Image bitmaps: JSON only here; hero/gallery frames go through {@link OfflineImageStore}
 * (explicit files under filesDir, thrifty 1–2 concurrent downloads). Library HTTP caches
 * are not enough when posts were never scrolled into view.
 *
 * <p>Half ({@code normal_video}) / full ({@code full_video}) video posts and
 * {@code google_vasttag} are never stored — they cannot play usefully offline. Filter is
 * client-side only at merge/load (no substitute List_All_Articles queries).
 */
public final class OfflineFeedStore {

    private static final String TAG = "OfflineFeedStore";
    private static final Gson GSON = new Gson();

    /** Default when splash never delivered a value (matches server runtimeTuning). */
    public static final int DEFAULT_PACK_MAX = 24;
    /** Admin / server min when a value is present. */
    public static final int MIN_PACK_MAX = 8;
    /**
     * Absolute hard safety ceiling — never exceed even if prefs misconfigured.
     * Matches server runtimeTuning max.
     */
    public static final int HARD_CAP_ABS = 50;
    /**
     * Kept as the code default (=24) for references that lack a Context;
     * runtime value always comes from {@link #getMainCap(Context)}.
     */
    public static final int MAIN_CAP = DEFAULT_PACK_MAX;
    /**
     * Same as main cap default; prefer {@link #getTargetPack(Context)}.
     */
    public static final int TARGET_PACK = DEFAULT_PACK_MAX;
    /** Special: no offline writes / paint (online-only). */
    public static final int SPECIAL_CAP = 0;
    /** Drop articles whose published/saved age exceeds this on load + capture. */
    public static final long STALE_AGE_MS = 7L * 24L * 60L * 60L * 1000L;

    private static final String MAIN_FILE = "offline_feed_main_v1.json";
    private static final String SPECIAL_FILE = "offline_feed_special_v1.json";

    /** UI badge / Offline News list refresh when media-ready pack size changes. */
    public interface PackChangeListener {
        void onOfflinePackChanged();
    }

    private static final AtomicReference<PackChangeListener> PACK_LISTENER =
            new AtomicReference<>();

    private OfflineFeedStore() {}

    public static void setPackChangeListener(@Nullable PackChangeListener listener) {
        PACK_LISTENER.set(listener);
    }

    public static void notifyPackChanged() {
        PackChangeListener l = PACK_LISTENER.get();
        if (l != null) {
            try {
                l.onOfflinePackChanged();
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Clamp a raw pack size to the hard safety range [MIN_PACK_MAX, HARD_CAP_ABS].
     * Null / invalid → {@link #DEFAULT_PACK_MAX}.
     */
    public static int clampPackMax(int raw) {
        if (raw < MIN_PACK_MAX) {
            // Allow 0/negative as “missing” → default rather than min-flooring junk.
            if (raw <= 0) return DEFAULT_PACK_MAX;
            return MIN_PACK_MAX;
        }
        return Math.min(HARD_CAP_ABS, raw);
    }

    /**
     * Persist admin Offline News pack max from splash / session.
     * Values outside 8–50 are clamped; {@code null} leaves prefs unchanged.
     */
    public static void savePackMaxFromServer(@Nullable Context context, @Nullable Integer max) {
        if (context == null || max == null) return;
        int safe = clampPackMax(max);
        SharedPrefsData.putString(
                context.getApplicationContext(),
                Constants.OFFLINE_NEWS_PACK_MAX,
                String.valueOf(safe),
                Constants.PREF_NAME);
    }

    /**
     * Hard max JSON articles on disk for main offline pack.
     * Admin value after splash (default 24); always ≤ {@link #HARD_CAP_ABS}.
     */
    public static int getMainCap(@Nullable Context context) {
        return resolvePackMax(context);
    }

    /**
     * Thrifty prefetch target (= {@link #getMainCap}). Stop filling once pack
     * reaches this many <b>media-ready</b> articles.
     */
    public static int getTargetPack(@Nullable Context context) {
        return resolvePackMax(context);
    }

    private static int resolvePackMax(@Nullable Context context) {
        if (context == null) return DEFAULT_PACK_MAX;
        try {
            String v =
                    SharedPrefsData.getString(
                            context.getApplicationContext(),
                            Constants.OFFLINE_NEWS_PACK_MAX,
                            Constants.PREF_NAME);
            if (TextUtils.isEmpty(v)) return DEFAULT_PACK_MAX;
            int n = Integer.parseInt(v.trim());
            return clampPackMax(n);
        } catch (Exception ignored) {
            return DEFAULT_PACK_MAX;
        }
    }

    public static final class Snapshot {
        public long savedAt;
        public int languageId;
        public int draftId;
        public String district;
        /** Selected district id at capture time (0 if unknown). */
        public int districtId;
        public boolean special;
        public List<ModelClass.Datum> articles;
        /**
         * Parallel to {@link #articles} when present: millis when each article was
         * first merged into the pack (used for eviction age if published_time missing).
         */
        public List<Long> articleSavedAt;
    }

    public static boolean hasAny(@Nullable Context context) {
        return hasUsable(context, false);
    }

    public static boolean hasUsable(@Nullable Context context, boolean special) {
        if (special) return false;
        List<ModelClass.Datum> list = load(context, false);
        return list != null && !list.isEmpty();
    }

    /**
     * Offline News badge + pack size: only stories with a local hero image.
     * Matches {@link #loadForOfflinePaint}.
     */
    public static int mainPackCount(@Nullable Context context) {
        List<ModelClass.Datum> list = load(context, false);
        return list == null ? 0 : list.size();
    }

    /**
     * Disk candidates (eligible JSON, not yet media-gated). Used for image sync and to
     * know whether thrifty fill should still chase more pages when ready count is low.
     */
    public static int candidatePackCount(@Nullable Context context) {
        List<ModelClass.Datum> list = loadCandidates(context);
        return list == null ? 0 : list.size();
    }

    /** Pack {@code savedAt}, or 0. */
    public static long loadSavedAt(@Nullable Context context) {
        if (context == null) return 0L;
        try {
            Snapshot snap = readSnapshot(context, false);
            return snap != null ? snap.savedAt : 0L;
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * Drop the offline main pack (language switch). Special file cleared as well.
     * Does not touch Daily Updates / gold / petrol / jobs caches.
     */
    public static void clearMain(@Nullable Context context) {
        if (context == null) return;
        try {
            File f = fileFor(context, false);
            if (f.exists()) {
                //noinspection ResultOfMethodCallIgnored
                f.delete();
            }
        } catch (Exception ignored) {
        }
        clearSpecialQuietly(context);
    }

    /**
     * Overwrite/merge offline snapshot from the current in-memory feed list.
     * Main only — special is a no-op (online-only; may delete legacy special file).
     * Feed list is treated as freshest-first (page-0 / cumulative display list).
     */
    public static void capture(
            @Nullable Context context,
            boolean special,
            @Nullable List<ModelClass.Datum> source) {
        if (special) {
            // Stop growing special offline cache; drop legacy file if present.
            clearSpecialQuietly(context);
            return;
        }
        mergeCapture(context, source, false);
    }

    /**
     * Merge articles into the main offline pack (dedupe by post id), then enforce
     * TTL + {@link #getMainCap}. Never grows unbounded.
     *
     * @param appendOnly when true, new posts from {@code source} are appended after
     *                   the existing pack (pagination / prefetch pages). When false,
     *                   {@code source} is treated as freshest-first primary order
     *                   (in-memory main list).
     */
    public static void mergeCapture(
            @Nullable Context context, @Nullable List<ModelClass.Datum> source) {
        mergeCapture(context, source, false);
    }

    public static void mergeCapture(
            @Nullable Context context,
            @Nullable List<ModelClass.Datum> source,
            boolean appendOnly) {
        if (context == null || source == null || source.isEmpty()) return;
        try {
            Context app = context.getApplicationContext();
            int draft = SharedPrefsData.getInt(app, Constants.DraftID, Constants.PREF_NAME);
            int lang = SharedPrefsData.getInt(app, Constants.LanguageID, Constants.PREF_NAME);
            String dist =
                    SharedPrefsData.getString(
                            app, Constants.Selected_Distric_Name1, Constants.PREF_NAME);
            int districtId =
                    SharedPrefsData.getInt(app, Constants.Selected_Distric_Id, Constants.PREF_NAME);

            Snapshot existing = null;
            try {
                existing = readSnapshot(app, false);
            } catch (Exception ignored) {
            }
            boolean identityOk =
                    existing != null
                            && existing.articles != null
                            && (existing.draftId <= 0 || draft <= 0 || existing.draftId == draft)
                            && (existing.languageId <= 0 || lang <= 0 || existing.languageId == lang);

            LinkedHashMap<Long, ModelClass.Datum> byId = new LinkedHashMap<>();
            Map<Long, Long> savedAtById = new HashMap<>();
            long now = System.currentTimeMillis();

            if (appendOnly && identityOk) {
                // Keep fresher pack order; append only unseen page items.
                List<ModelClass.Datum> prev = existing.articles;
                List<Long> prevSaved =
                        existing.articleSavedAt != null
                                ? existing.articleSavedAt
                                : Collections.emptyList();
                for (int i = 0; i < prev.size(); i++) {
                    ModelClass.Datum d = prev.get(i);
                    if (!isOfflineEligible(d)) continue;
                    long id = d.getPostId();
                    byId.put(id, d);
                    long sa = existing.savedAt > 0 ? existing.savedAt : now;
                    if (i < prevSaved.size() && prevSaved.get(i) != null && prevSaved.get(i) > 0) {
                        sa = prevSaved.get(i);
                    }
                    savedAtById.put(id, sa);
                }
                for (ModelClass.Datum d : source) {
                    if (!isOfflineEligible(d)) continue;
                    long id = d.getPostId();
                    if (byId.containsKey(id)) {
                        byId.put(id, d); // refresh body, keep position
                    } else {
                        byId.put(id, d);
                        savedAtById.put(id, now);
                    }
                }
            } else {
                // Freshest-first: source (display list) first, then leftover pack items.
                for (ModelClass.Datum d : source) {
                    if (!isOfflineEligible(d)) continue;
                    long id = d.getPostId();
                    byId.put(id, d);
                    savedAtById.put(id, now);
                }
                if (identityOk) {
                    List<ModelClass.Datum> prev = existing.articles;
                    List<Long> prevSaved =
                            existing.articleSavedAt != null
                                    ? existing.articleSavedAt
                                    : Collections.emptyList();
                    for (int i = 0; i < prev.size(); i++) {
                        ModelClass.Datum d = prev.get(i);
                        if (!isOfflineEligible(d)) continue;
                        long id = d.getPostId();
                        if (!byId.containsKey(id)) {
                            byId.put(id, d);
                        }
                        long sa = existing.savedAt > 0 ? existing.savedAt : now;
                        if (i < prevSaved.size()
                                && prevSaved.get(i) != null
                                && prevSaved.get(i) > 0) {
                            sa = prevSaved.get(i);
                        }
                        if (!savedAtById.containsKey(id)) {
                            savedAtById.put(id, sa);
                        }
                    }
                }
            }

            List<ModelClass.Datum> slim = new ArrayList<>(byId.size());
            List<Long> slimSaved = new ArrayList<>(byId.size());
            for (Map.Entry<Long, ModelClass.Datum> e : byId.entrySet()) {
                ModelClass.Datum d = e.getValue();
                long sa = savedAtById.containsKey(e.getKey()) ? savedAtById.get(e.getKey()) : now;
                if (isStale(d, sa, now)) continue;
                slim.add(d);
                slimSaved.add(sa);
            }
            if (slim.isEmpty()) return;

            enforceCap(app, slim, slimSaved, now);

            Snapshot snap = new Snapshot();
            snap.savedAt = now;
            snap.special = false;
            snap.draftId = draft;
            snap.languageId = lang;
            snap.district = dist != null ? dist : "";
            snap.districtId = districtId;
            snap.articles = slim;
            snap.articleSavedAt = slimSaved;

            writeUtf8(fileFor(app, false), GSON.toJson(snap));
            // Warm / maintain explicit offline hero images for retained pack posts.
            try {
                OfflineImageStore.syncPackImages(app, slim);
            } catch (Exception imgEx) {
                Logone.w(imgEx, TAG + " syncPackImages failed");
            }
            Logone.v(
                    TAG,
                    "mergeCapture candidates="
                            + slim.size()
                            + " ready="
                            + filterMediaReady(app, slim).size()
                            + " cap="
                            + getMainCap(app)
                            + " appendOnly="
                            + appendOnly);
            notifyPackChanged();
        } catch (Exception e) {
            Logone.w(e, TAG + " mergeCapture failed");
        }
    }

    /**
     * Drop a post from the offline pack (e.g. primary image download permanently failed).
     */
    public static void removePost(@Nullable Context context, long postId) {
        if (context == null || postId <= 0) return;
        try {
            Context app = context.getApplicationContext();
            Snapshot snap = readSnapshot(app, false);
            if (snap == null || snap.articles == null || snap.articles.isEmpty()) return;
            List<ModelClass.Datum> next = new ArrayList<>();
            List<Long> nextSaved = new ArrayList<>();
            List<Long> savedAts =
                    snap.articleSavedAt != null ? snap.articleSavedAt : Collections.emptyList();
            boolean removed = false;
            for (int i = 0; i < snap.articles.size(); i++) {
                ModelClass.Datum d = snap.articles.get(i);
                if (d != null && d.getPostId() != null && d.getPostId() == postId) {
                    removed = true;
                    continue;
                }
                next.add(d);
                if (i < savedAts.size() && savedAts.get(i) != null) {
                    nextSaved.add(savedAts.get(i));
                } else {
                    nextSaved.add(snap.savedAt > 0 ? snap.savedAt : System.currentTimeMillis());
                }
            }
            if (!removed) return;
            if (next.isEmpty()) {
                File f = fileFor(app, false);
                //noinspection ResultOfMethodCallIgnored
                f.delete();
                notifyPackChanged();
                return;
            }
            snap.articles = next;
            snap.articleSavedAt = nextSaved;
            writeUtf8(fileFor(app, false), GSON.toJson(snap));
            notifyPackChanged();
            Logone.v(TAG, "removePost id=" + postId + " remainingCandidates=" + next.size());
        } catch (Exception e) {
            Logone.w(e, TAG + " removePost failed");
        }
    }

    /**
     * Load main pack for Offline News (identity + age + <b>local hero image</b>).
     * Prefer {@link #loadForOfflinePaint} for unread-first order.
     */
    @Nullable
    public static List<ModelClass.Datum> load(@Nullable Context context, boolean special) {
        if (special) return null;
        List<ModelClass.Datum> candidates = loadCandidates(context);
        if (candidates == null || candidates.isEmpty()) return null;
        List<ModelClass.Datum> ready = filterMediaReady(context, candidates);
        return ready.isEmpty() ? null : ready;
    }

    /**
     * Offline News paint order: media-ready only; unread/not-opened first, then remaining.
     */
    @Nullable
    public static List<ModelClass.Datum> loadForOfflinePaint(@Nullable Context context) {
        List<ModelClass.Datum> raw = load(context, false);
        if (raw == null || raw.isEmpty()) return null;
        try {
            Set<Long> opened = ArticleOpenLog.snapshotOpened(context);
            if (opened.isEmpty()) {
                return new ArrayList<>(raw);
            }
            List<ModelClass.Datum> unread = new ArrayList<>();
            List<ModelClass.Datum> read = new ArrayList<>();
            for (ModelClass.Datum d : raw) {
                if (d == null || d.getPostId() == null) continue;
                if (opened.contains(d.getPostId())) {
                    read.add(d);
                } else {
                    unread.add(d);
                }
            }
            List<ModelClass.Datum> out = new ArrayList<>(raw.size());
            out.addAll(unread);
            out.addAll(read);
            return out;
        } catch (Exception e) {
            return new ArrayList<>(raw);
        }
    }

    /**
     * Offline-eligible candidates on disk (may lack local images — not for UI paint).
     */
    @Nullable
    public static List<ModelClass.Datum> loadCandidates(@Nullable Context context) {
        return loadRaw(context);
    }

    @Nullable
    private static List<ModelClass.Datum> loadRaw(@Nullable Context context) {
        if (context == null) return null;
        try {
            Snapshot snap = readSnapshot(context, false);
            if (snap == null || snap.articles == null || snap.articles.isEmpty()) return null;

            int draft = SharedPrefsData.getInt(context, Constants.DraftID, Constants.PREF_NAME);
            int lang = SharedPrefsData.getInt(context, Constants.LanguageID, Constants.PREF_NAME);
            if (snap.draftId > 0 && draft > 0 && snap.draftId != draft) return null;
            if (snap.languageId > 0 && lang > 0 && snap.languageId != lang) return null;

            long now = System.currentTimeMillis();
            List<ModelClass.Datum> out = new ArrayList<>();
            List<Long> savedAts =
                    snap.articleSavedAt != null ? snap.articleSavedAt : Collections.emptyList();
            boolean grewStale = false;
            for (int i = 0; i < snap.articles.size(); i++) {
                ModelClass.Datum d = snap.articles.get(i);
                if (!isOfflineEligible(d)) continue;
                long sa =
                        (i < savedAts.size() && savedAts.get(i) != null && savedAts.get(i) > 0)
                                ? savedAts.get(i)
                                : (snap.savedAt > 0 ? snap.savedAt : now);
                if (isStale(d, sa, now)) {
                    grewStale = true;
                    continue;
                }
                out.add(d);
            }
            if (out.isEmpty()) {
                try {
                    File f = fileFor(context, false);
                    //noinspection ResultOfMethodCallIgnored
                    f.delete();
                } catch (Exception ignored) {
                }
                return null;
            }
            if (grewStale || out.size() < snap.articles.size()) {
                try {
                    List<Long> slimSaved = new ArrayList<>(out.size());
                    long base = snap.savedAt > 0 ? snap.savedAt : now;
                    for (int i = 0; i < out.size(); i++) {
                        slimSaved.add(base);
                    }
                    snap.articles = out;
                    snap.articleSavedAt = slimSaved;
                    snap.savedAt = base;
                    writeUtf8(fileFor(context, false), GSON.toJson(snap));
                } catch (Exception ignored) {
                }
            }
            return new ArrayList<>(out);
        } catch (Exception e) {
            Logone.w(e, TAG + " load failed");
            return null;
        }
    }

    @NonNull
    private static List<ModelClass.Datum> filterMediaReady(
            @Nullable Context context, @Nullable List<ModelClass.Datum> source) {
        List<ModelClass.Datum> out = new ArrayList<>();
        if (context == null || source == null) return out;
        for (ModelClass.Datum d : source) {
            if (isServingReady(context, d)) {
                out.add(d);
            }
        }
        return out;
    }

    /**
     * True when Offline News should show this post: offline-eligible + local hero on disk.
     */
    public static boolean isServingReady(
            @Nullable Context context, @Nullable ModelClass.Datum d) {
        if (context == null || !isOfflineEligible(d)) return false;
        return OfflineImageStore.hasPrimary(context, d.getPostId());
    }

    /**
     * District label saved with the offline main-feed snapshot. Used when painting
     * the location pill offline after theme recreate if SharedPrefs is empty.
     * Prefer {@link #loadDistrictLabelForLanguage} so a Telugu snapshot cannot
     * repaint a Hindi UI.
     */
    @Nullable
    public static String loadDistrictLabel(@Nullable Context context) {
        if (context == null) return null;
        int lang = SharedPrefsData.getInt(context, Constants.LanguageID, Constants.PREF_NAME);
        return loadDistrictLabelForLanguage(context, lang);
    }

    /**
     * District label from offline main snapshot only when {@code languageId} matches
     * the language the snapshot was captured under (or snapshot has no language tag).
     */
    @Nullable
    public static String loadDistrictLabelForLanguage(@Nullable Context context, int languageId) {
        if (context == null) return null;
        try {
            Snapshot snap = readSnapshot(context, false);
            if (snap == null) return null;
            if (languageId > 0 && snap.languageId > 0 && snap.languageId != languageId) {
                return null;
            }
            if (snap.districtId > 0) {
                int storedId =
                        SharedPrefsData.getInt(
                                context, Constants.Selected_Distric_Id, Constants.PREF_NAME);
                if (storedId <= 0) {
                    SharedPrefsData.putInt(
                            context,
                            Constants.Selected_Distric_Id,
                            snap.districtId,
                            Constants.PREF_NAME);
                }
            }
            String d = snap.district;
            if (TextUtils.isEmpty(d)) return null;
            d = d.trim();
            if (d.isEmpty() || "null".equalsIgnoreCase(d) || "N/A".equalsIgnoreCase(d)) {
                return null;
            }
            return d;
        } catch (Exception e) {
            Logone.w(e, TAG + " loadDistrictLabelForLanguage failed");
            return null;
        }
    }

    /**
     * Language-safe district label for the current {@link Constants#LanguageID}.
     *
     * @return usable district label, or empty string
     */
    @NonNull
    public static String ensureDistrictPrefs(@Nullable Context context) {
        return DistrictLabelHelper.ensurePrefsForCurrentLanguage(context);
    }

    /**
     * Human label for Offline News pill, e.g. {@code Saved for offline · updated 2h ago}.
     * Empty if no pack.
     */
    @NonNull
    public static String formatUpdatedLabel(@Nullable Context context) {
        long at = loadSavedAt(context);
        if (at <= 0 || context == null) return "";
        long age = Math.max(0L, System.currentTimeMillis() - at);
        String when;
        if (age < 60_000L) {
            when = "just now";
        } else if (age < 3_600_000L) {
            when = (age / 60_000L) + "m ago";
        } else if (age < 86_400_000L) {
            when = (age / 3_600_000L) + "h ago";
        } else {
            when = (age / 86_400_000L) + "d ago";
        }
        try {
            return context.getApplicationContext()
                    .getString(com.anyNews.anynews.R.string.offline_saved_updated, when);
        } catch (Exception e) {
            return "Saved for offline · updated " + when;
        }
    }

    private static void enforceCap(
            @NonNull Context context,
            @NonNull List<ModelClass.Datum> slim,
            @NonNull List<Long> slimSaved,
            long now) {
        final int mainCap = getMainCap(context);
        if (slim.size() <= mainCap) return;
        Set<Long> opened = ArticleOpenLog.snapshotOpened(context);

        // Prefer keeping media-ready posts; evict non-ready first, then opened, then oldest.
        List<Integer> order = new ArrayList<>(slim.size());
        for (int i = 0; i < slim.size(); i++) order.add(i);

        Collections.sort(
                order,
                new Comparator<Integer>() {
                    @Override
                    public int compare(Integer a, Integer b) {
                        ModelClass.Datum da = slim.get(a);
                        ModelClass.Datum db = slim.get(b);
                        long ida = da.getPostId() != null ? da.getPostId() : 0L;
                        long idb = db.getPostId() != null ? db.getPostId() : 0L;
                        boolean readyA = OfflineImageStore.hasPrimary(context, ida);
                        boolean readyB = OfflineImageStore.hasPrimary(context, idb);
                        if (readyA != readyB) {
                            // Evict non-ready first so the cap is used by paint-ready stories.
                            return readyA ? 1 : -1;
                        }
                        boolean oa = ida > 0 && opened.contains(ida);
                        boolean ob = idb > 0 && opened.contains(idb);
                        if (oa != ob) {
                            return oa ? -1 : 1;
                        }
                        long ta = ageKey(da, slimSaved.get(a));
                        long tb = ageKey(db, slimSaved.get(b));
                        int c = Long.compare(ta, tb);
                        if (c != 0) return c;
                        return Integer.compare(b, a);
                    }
                });

        boolean[] drop = new boolean[slim.size()];
        int need = slim.size() - mainCap;
        for (int k = 0; k < order.size() && need > 0; k++) {
            drop[order.get(k)] = true;
            need--;
        }
        List<ModelClass.Datum> next = new ArrayList<>(mainCap);
        List<Long> nextSaved = new ArrayList<>(mainCap);
        for (int i = 0; i < slim.size(); i++) {
            if (drop[i]) continue;
            next.add(slim.get(i));
            nextSaved.add(slimSaved.get(i));
        }
        slim.clear();
        slim.addAll(next);
        slimSaved.clear();
        slimSaved.addAll(nextSaved);
        Logone.v(TAG, "evicted to cap=" + mainCap + " remaining=" + slim.size());
    }

    private static long ageKey(@Nullable ModelClass.Datum d, long savedAt) {
        if (d != null && d.getPublishedTime() != null && d.getPublishedTime() > 0) {
            long p = d.getPublishedTime();
            // Server may send seconds.
            if (p < 1_000_000_000_000L) p *= 1000L;
            return p;
        }
        return savedAt > 0 ? savedAt : 0L;
    }

    private static boolean isStale(
            @Nullable ModelClass.Datum d, long savedAt, long now) {
        long ref = ageKey(d, savedAt);
        if (ref <= 0) return false;
        return (now - ref) > STALE_AGE_MS;
    }

    /**
     * Posts kept as offline pack candidates.
     * Drop ads and playables that need network media ({@code normal_video},
     * {@code full_video}, {@code google_vasttag}). Require at least one downloadable
     * hero/thumbnail URL so Offline News never includes placeholder-only cards.
     */
    private static boolean isOfflineEligible(@Nullable ModelClass.Datum d) {
        if (d == null || d.getPostId() == null || d.getPostId() <= 0) return false;
        String pt = d.getPostType();
        if (pt != null) {
            if (pt.equalsIgnoreCase("normal_video")
                    || pt.equalsIgnoreCase("full_video")
                    || pt.equalsIgnoreCase("google_vasttag")
                    || pt.equalsIgnoreCase("ad")
                    || pt.equalsIgnoreCase("web_ads")
                    || pt.equalsIgnoreCase("web")
                    || pt.equalsIgnoreCase("ratings")
                    || pt.equalsIgnoreCase("share_app")) {
                return false;
            }
        }
        // Must have a hero URL we can cache offline (no logo-only placeholders).
        String hero = OfflineImageStore.primaryImageUrl(d);
        return !TextUtils.isEmpty(hero);
    }

    private static void clearSpecialQuietly(@Nullable Context context) {
        if (context == null) return;
        try {
            File f = fileFor(context, true);
            if (f.exists()) {
                //noinspection ResultOfMethodCallIgnored
                f.delete();
            }
        } catch (Exception ignored) {
        }
    }

    @Nullable
    private static Snapshot readSnapshot(@NonNull Context context, boolean special) throws Exception {
        File file = fileFor(context, special);
        if (!file.exists() || file.length() < 32) return null;
        String json = readUtf8(file);
        if (TextUtils.isEmpty(json)) return null;
        return GSON.fromJson(json, Snapshot.class);
    }

    @NonNull
    private static File fileFor(@NonNull Context context, boolean special) {
        File dir = context.getApplicationContext().getFilesDir();
        return new File(dir, special ? SPECIAL_FILE : MAIN_FILE);
    }

    private static String readUtf8(File file) throws Exception {
        StringBuilder sb = new StringBuilder((int) Math.min(file.length(), 4_000_000));
        try (BufferedReader br =
                new BufferedReader(
                        new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            char[] buf = new char[8192];
            int n;
            while ((n = br.read(buf)) >= 0) {
                sb.append(buf, 0, n);
            }
        }
        return sb.toString();
    }

    private static void writeUtf8(File file, String content) throws Exception {
        File tmp = new File(file.getAbsolutePath() + ".tmp");
        try (FileOutputStream fos = new FileOutputStream(tmp, false)) {
            fos.write(content.getBytes(StandardCharsets.UTF_8));
            fos.flush();
        }
        if (file.exists() && !file.delete()) {
            // best-effort
        }
        if (!tmp.renameTo(file)) {
            try (FileOutputStream fos = new FileOutputStream(file, false)) {
                fos.write(content.getBytes(StandardCharsets.UTF_8));
            }
            //noinspection ResultOfMethodCallIgnored
            tmp.delete();
        }
    }
}
