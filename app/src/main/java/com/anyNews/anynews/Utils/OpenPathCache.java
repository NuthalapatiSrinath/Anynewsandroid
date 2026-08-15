package com.anyNews.anynews.Utils;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.anyNews.anynews.Pojo.EncreptionRequest;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Client-side cache for cold/warm open under push stampede.
 * <p>
 * Goals: thrash-reduce open-path network via short TTLs + dedupe, and allow
 * a same-session warm page-0 paint only when the last online fetch is
 * minutes-old and matches identity/mode. Never paint multi-hour stale feed
 * or offline packs as an online first frame — show shimmer until network
 * (or true offline mode) instead.
 * <p>
 * MUST NOT alter Reload / Special / push-share sticky semantics: those paths
 * always hit the feed network and never paint page-0 disk as “done”.
 * <p>
 * Main vs Special vs Video page-0 snapshots are stored under separate feedMode
 * keys so a main payload can never paint Special (and vice versa).
 */
public final class OpenPathCache {

    private static final String TAG = "OpenPathCache";
    private static final Gson GSON = new Gson();

    /** Feed mode for page-0 disk snapshots — never collide main/special/video. */
    public static final String FEED_MODE_MAIN = "main";
    public static final String FEED_MODE_SPECIAL = "special";
    public static final String FEED_MODE_VIDEO = "video";

    /** Splash / identity refresh (draft, fonts, update flags). */
    public static final long SPLASH_TTL_MS = 15 * 60 * 1000L;
    /** Translator JSON from get_static_data — rarely changes. */
    public static final long STATIC_TTL_MS = 24 * 60 * 60 * 1000L;
    /**
     * Online page-0 warm paint only (same session / soft-refresh continuity).
     * Max 5 minutes after last successful online save for matching
     * lang/geo/draft/mode. Older disk is still usable for true offline via
     * {@link #loadFeedForOffline} — never as online first paint.
     */
    public static final long FEED_DISK_TTL_MS = 5 * 60 * 1000L;
    /**
     * Skip network feed refresh if last successful page-0 fetch was this recent
     * and there is no push/share sticky / special / reload. Aligns with warm paint.
     * Reload / special / sticky always force network (never throttled).
     */
    public static final long FEED_NETWORK_MIN_INTERVAL_MS = 3 * 60 * 1000L;

    private static final String FEED_FILE_MAIN = "open_path_feed_page0_main.json";
    private static final String FEED_FILE_SPECIAL = "open_path_feed_page0_special.json";
    private static final String FEED_FILE_VIDEO = "open_path_feed_page0_video.json";
    /** Legacy unscoped file (pre mode split) — treated as main only. */
    private static final String FEED_FILE_LEGACY = "open_path_feed_page0.json";

    /** One-shot force: set by Reload; cleared when the forced network call is enqueued. */
    private static volatile boolean pendingForceFeedNetwork = false;

    private OpenPathCache() {
    }

    private static String feedFileName(String feedMode) {
        if (FEED_MODE_SPECIAL.equals(feedMode)) return FEED_FILE_SPECIAL;
        if (FEED_MODE_VIDEO.equals(feedMode)) return FEED_FILE_VIDEO;
        return FEED_FILE_MAIN;
    }

    // ── Identity / splash / static ──────────────────────────────────────

    public static boolean hasUsableIdentity(Context context) {
        if (context == null) return false;
        int draft = SharedPrefsData.getInt(context, Constants.DraftID, Constants.PREF_NAME);
        int lang = SharedPrefsData.getInt(context, Constants.LanguageID, Constants.PREF_NAME);
        return draft > 0 && lang > 0;
    }

    public static boolean hasTranslator(Context context) {
        if (context == null) return false;
        try {
            Allsharedpreference prefs = new Allsharedpreference(context.getApplicationContext());
            String translation = prefs.getShared_String(Allsharedpreference.Translator);
            return translation != null && translation.length() > 20;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returning user can leave splash without blocking on Splash_Screen + static.
     * Push/share deep links still open home immediately but force a feed network call.
     */
    public static boolean canFastOpenHome(Context context) {
        if (!hasUsableIdentity(context)) return false;
        // Prefer translator, but page-0 disk alone is enough to leave splash (~2s target)
        // and paint Home immediately; static/translator can soft-refresh after.
        // OfflineFeedStore main pack (capped, no special offline) also counts for cold open with data off.
        if (!hasTranslator(context)
                && !hasFeedDisk(context)
                && !OfflineFeedStore.hasAny(context)) {
            return false;
        }
        String next = SharedPrefsData.getString(context, Constants.MOVE_NEXT_SCREEN, Constants.PREF_NAME);
        if (next == null || next.isEmpty()) {
            return SharedPrefsData.getBool(context, Constants.IS_LANGUAGE_SELECTED, Constants.PREF_NAME);
        }
        return "main".equalsIgnoreCase(next)
                || SharedPrefsData.getBool(context, Constants.IS_LANGUAGE_SELECTED, Constants.PREF_NAME);
    }

    public static boolean shouldBlockOnSplash(Context context) {
        if (!hasUsableIdentity(context)) return true;
        long last = SharedPrefsData.getLong(context, Constants.OPEN_PATH_SPLASH_AT, Constants.PREF_NAME);
        if (last <= 0) return true;
        return (System.currentTimeMillis() - last) > SPLASH_TTL_MS;
    }

    public static void markSplashSuccess(Context context) {
        if (context == null) return;
        SharedPrefsData.putLong(context, Constants.OPEN_PATH_SPLASH_AT,
                System.currentTimeMillis(), Constants.PREF_NAME);
    }

    public static boolean shouldRefreshStatic(Context context) {
        if (!hasTranslator(context)) return true;
        long last = SharedPrefsData.getLong(context, Constants.OPEN_PATH_STATIC_AT, Constants.PREF_NAME);
        if (last <= 0) return true;
        return (System.currentTimeMillis() - last) > STATIC_TTL_MS;
    }

    public static void markStaticSuccess(Context context) {
        if (context == null) return;
        SharedPrefsData.putLong(context, Constants.OPEN_PATH_STATIC_AT,
                System.currentTimeMillis(), Constants.PREF_NAME);
    }

    /**
     * @param forceAlways when true (push/share sticky, special, explicit reload), never skip.
     */
    public static boolean shouldNetworkRefreshFeed(Context context, boolean forceAlways) {
        if (forceAlways || pendingForceFeedNetwork) return true;
        if (context == null) return true;
        if (!hasFeedDisk(context)) return true;
        long last = SharedPrefsData.getLong(context, Constants.OPEN_PATH_FEED_NET_AT, Constants.PREF_NAME);
        if (last <= 0) return true;
        return (System.currentTimeMillis() - last) > FEED_NETWORK_MIN_INTERVAL_MS;
    }

    /** Reload / intentional soft restart: next page-0 must hit network. */
    public static void forceNextFeedNetwork(Context context) {
        pendingForceFeedNetwork = true;
        if (context == null) return;
        SharedPrefsData.putLong(context, Constants.OPEN_PATH_FEED_NET_AT, 0L, Constants.PREF_NAME);
    }

    /** True while an explicit reload/force is pending (do not paint stale disk as “done”). */
    public static boolean isForceFeedNetworkPending() {
        return pendingForceFeedNetwork;
    }

    /** Clear one-shot force after the forced request has been enqueued. */
    public static void clearForceFeedNetwork() {
        pendingForceFeedNetwork = false;
    }

    public static void markFeedNetworkSuccess(Context context) {
        if (context == null) return;
        SharedPrefsData.putLong(context, Constants.OPEN_PATH_FEED_NET_AT,
                System.currentTimeMillis(), Constants.PREF_NAME);
    }

    // ── Feed disk cache (page 0 only) ───────────────────────────────────

    public static final class FeedSnapshot {
        public long savedAt;
        public int languageId;
        public int categoryId;
        public int draftId;
        public String district;
        /** {@link #FEED_MODE_MAIN}, {@link #FEED_MODE_SPECIAL}, or {@link #FEED_MODE_VIDEO}. */
        public String feedMode;
        public EncreptionRequest payload;
    }

    public static String feedKey(Context context, int categoryId) {
        return feedKey(context, categoryId, FEED_MODE_MAIN);
    }

    public static String feedKey(Context context, int categoryId, String feedMode) {
        int draft = SharedPrefsData.getInt(context, Constants.DraftID, Constants.PREF_NAME);
        int lang = SharedPrefsData.getInt(context, Constants.LanguageID, Constants.PREF_NAME);
        String dist = SharedPrefsData.getString(context, Constants.Selected_Distric_Name1, Constants.PREF_NAME);
        if (dist == null) dist = "";
        String mode = feedMode != null ? feedMode : FEED_MODE_MAIN;
        return draft + "|" + lang + "|" + categoryId + "|" + dist + "|" + mode;
    }

    public static boolean hasFeedDisk(Context context) {
        // Offline cold open / mid-session drop: any matching main identity payload counts,
        // even past FEED_DISK_TTL (paint-only; network refresh still TTL-gated).
        // Special/video are online-only and never count as Offline News pack identity.
        return loadFeed(context, 0, FEED_MODE_MAIN) != null
                || loadFeedForOffline(context, 0, FEED_MODE_MAIN) != null;
    }

    /**
     * Load page-0 feed if present, not expired, and matches language/category/draft/district.
     * Defaults to {@link #FEED_MODE_MAIN}.
     */
    @Nullable
    public static EncreptionRequest loadFeed(Context context, int categoryId) {
        return loadFeed(context, categoryId, FEED_MODE_MAIN);
    }

    @Nullable
    public static EncreptionRequest loadFeed(Context context, int categoryId, String feedMode) {
        return loadFeedInternal(context, categoryId, feedMode, /*maxAgeMs*/ FEED_DISK_TTL_MS);
    }

    /**
     * Online first-paint / soft-refresh continuity: only the short warm window
     * ({@link #FEED_DISK_TTL_MS}). No multi-hour stale dump — callers keep shimmer
     * until a live network response when this returns null.
     */
    @Nullable
    public static EncreptionRequest loadFeedForInstantPaint(Context context, int categoryId) {
        return loadFeedForInstantPaint(context, categoryId, FEED_MODE_MAIN);
    }

    @Nullable
    public static EncreptionRequest loadFeedForInstantPaint(
            Context context, int categoryId, String feedMode) {
        return loadFeed(context, categoryId, feedMode);
    }

    /**
     * Offline-only paint fallback: identity match without FEED_DISK_TTL so a saved
     * page-0 JSON can still open Offline News when Wi‑Fi/data is off past the warm TTL.
     */
    @Nullable
    public static EncreptionRequest loadFeedForOffline(Context context, int categoryId) {
        return loadFeedForOffline(context, categoryId, FEED_MODE_MAIN);
    }

    @Nullable
    public static EncreptionRequest loadFeedForOffline(
            Context context, int categoryId, String feedMode) {
        // Long.MAX_VALUE: any age, identity match only (true offline policy).
        return loadFeedInternal(context, categoryId, feedMode, Long.MAX_VALUE);
    }

    /**
     * @param maxAgeMs max age of snapshot; {@link Long#MAX_VALUE} = any age (offline only).
     *                 Online warm paint uses {@link #FEED_DISK_TTL_MS} only.
     */
    @Nullable
    private static EncreptionRequest loadFeedInternal(
            Context context, int categoryId, String feedMode, long maxAgeMs) {
        if (context == null) return null;
        final String mode = TextUtils.isEmpty(feedMode) ? FEED_MODE_MAIN : feedMode;
        try {
            File prefer = new File(
                    context.getApplicationContext().getCacheDir(), feedFileName(mode));
            EncreptionRequest hit = readFeedFile(
                    prefer, context, categoryId, mode, maxAgeMs);
            if (hit != null) return hit;
            // Legacy main-only file (no feedMode field) — never reuse for special/video.
            if (FEED_MODE_MAIN.equals(mode)) {
                File legacy = new File(
                        context.getApplicationContext().getCacheDir(), FEED_FILE_LEGACY);
                return readFeedFile(
                        legacy, context, categoryId, FEED_MODE_MAIN, maxAgeMs);
            }
            return null;
        } catch (Exception e) {
            Logone.w(e, TAG + " loadFeed failed");
            return null;
        }
    }

    @Nullable
    private static EncreptionRequest readFeedFile(
            File file,
            Context context,
            int categoryId,
            String expectedMode,
            long maxAgeMs) {
        if (file == null || !file.exists() || file.length() < 32) return null;
        try {
            String json = readUtf8(file);
            if (TextUtils.isEmpty(json)) return null;
            FeedSnapshot snap = GSON.fromJson(json, FeedSnapshot.class);
            if (snap == null || snap.payload == null || TextUtils.isEmpty(snap.payload.getData())) {
                return null;
            }
            if (maxAgeMs < Long.MAX_VALUE
                    && (System.currentTimeMillis() - snap.savedAt) > maxAgeMs) {
                return null;
            }
            String snapMode = snap.feedMode;
            if (TextUtils.isEmpty(snapMode)) {
                // Legacy unscoped snapshot → treat as main only (never special/video).
                if (!FEED_MODE_MAIN.equals(expectedMode)) {
                    return null;
                }
                snapMode = FEED_MODE_MAIN;
            }
            if (!expectedMode.equals(snapMode)) {
                return null;
            }
            int draft = SharedPrefsData.getInt(context, Constants.DraftID, Constants.PREF_NAME);
            int lang = SharedPrefsData.getInt(context, Constants.LanguageID, Constants.PREF_NAME);
            String dist = SharedPrefsData.getString(context, Constants.Selected_Distric_Name1, Constants.PREF_NAME);
            if (dist == null) dist = "";
            if (snap.draftId != draft || snap.languageId != lang || snap.categoryId != categoryId) {
                return null;
            }
            String snapDist = snap.district != null ? snap.district : "";
            if (!snapDist.equals(dist)) {
                return null;
            }
            return snap.payload;
        } catch (Exception e) {
            Logone.w(e, TAG + " readFeedFile failed");
            return null;
        }
    }

    /** Persist first-page feed response for instant reopen paint (main only by default). */
    public static void saveFeed(Context context, int categoryId, EncreptionRequest payload) {
        saveFeed(context, categoryId, payload, FEED_MODE_MAIN);
    }

    /**
     * Persist first-page feed for a mode. Callers must pass the active feed mode so
     * Special/Video never overwrite main page-0 JSON.
     */
    public static void saveFeed(
            Context context, int categoryId, EncreptionRequest payload, String feedMode) {
        if (context == null || payload == null || TextUtils.isEmpty(payload.getData())) return;
        // Only cache non-push-specific first pages (caller should pass page-0 only).
        try {
            final String mode = TextUtils.isEmpty(feedMode) ? FEED_MODE_MAIN : feedMode;
            // Product: Special is online-only — never write special disk paint.
            if (FEED_MODE_SPECIAL.equals(mode)) {
                markFeedNetworkSuccess(context);
                return;
            }
            FeedSnapshot snap = new FeedSnapshot();
            snap.savedAt = System.currentTimeMillis();
            snap.draftId = SharedPrefsData.getInt(context, Constants.DraftID, Constants.PREF_NAME);
            snap.languageId = SharedPrefsData.getInt(context, Constants.LanguageID, Constants.PREF_NAME);
            snap.categoryId = categoryId;
            String dist = SharedPrefsData.getString(context, Constants.Selected_Distric_Name1, Constants.PREF_NAME);
            snap.district = dist != null ? dist : "";
            snap.feedMode = mode;
            snap.payload = payload;
            File file = new File(
                    context.getApplicationContext().getCacheDir(), feedFileName(mode));
            writeUtf8(file, GSON.toJson(snap));
            markFeedNetworkSuccess(context);
        } catch (Exception e) {
            Logone.w(e, TAG + " saveFeed failed");
        }
    }

    public static void clearFeed(Context context) {
        if (context == null) return;
        deleteQuiet(context, FEED_FILE_MAIN);
        deleteQuiet(context, FEED_FILE_SPECIAL);
        deleteQuiet(context, FEED_FILE_VIDEO);
        deleteQuiet(context, FEED_FILE_LEGACY);
    }

    private static void deleteQuiet(Context context, String name) {
        try {
            File file = new File(context.getApplicationContext().getCacheDir(), name);
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        } catch (Exception ignored) {
        }
    }

    private static String readUtf8(File file) throws Exception {
        StringBuilder sb = new StringBuilder((int) Math.min(file.length(), 2_000_000));
        try (BufferedReader br = new BufferedReader(
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
            //best-effort
        }
        if (!tmp.renameTo(file)) {
            // fallback copy
            try (FileOutputStream fos = new FileOutputStream(file, false)) {
                fos.write(content.getBytes(StandardCharsets.UTF_8));
            }
            //noinspection ResultOfMethodCallIgnored
            tmp.delete();
        }
    }
}
