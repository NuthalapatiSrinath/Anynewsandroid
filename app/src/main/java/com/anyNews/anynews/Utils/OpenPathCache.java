package com.anyNews.anynews.Utils;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.anyNews.anynews.Pojo.EncreptionRequest;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Client-side cache for splash / identity / static open-path.
 * <p>
 * <b>Article feed disk is permanently disabled</b> ({@link #FEED_DISK_DISABLED}):
 * no page-0 save, no warm instant paint, no offline feed paint. Cold/reopen
 * always shimmer until live List/page-0. Splash draft / static TTLs stay.
 * <p>
 * MUST NOT alter Reload / Special / push-share sticky semantics: those paths
 * always hit the feed network.
 */
public final class OpenPathCache {

    private static final String TAG = "OpenPathCache";
    private static final Gson GSON = new Gson();

    /**
     * Kill switch: never pack or paint article list from OpenPath feed disk.
     * Splash / translator / identity caches are unaffected.
     */
    public static final boolean FEED_DISK_DISABLED = true;

    /** Feed mode for page-0 disk snapshots — never collide main/special/video/local. */
    public static final String FEED_MODE_MAIN = "main";
    public static final String FEED_MODE_SPECIAL = "special";
    public static final String FEED_MODE_VIDEO = "video";
    /** District Local tab ({@code category_id=-1}) — separate file from Main. */
    public static final String FEED_MODE_LOCAL = "local";

    /** Splash / identity refresh (draft, fonts, update flags). */
    public static final long SPLASH_TTL_MS = 15 * 60 * 1000L;
    /**
     * Way2-style lean open: refresh Splash_Screen config (fonts / admin prefs)
     * in the background about once per day — not on every cold/push open.
     * Does not block first paint; see {@link #shouldRefreshSplashConfig}.
     */
    public static final long SPLASH_CONFIG_REFRESH_TTL_MS = 24 * 60 * 60 * 1000L;
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
     * and there is no push/share sticky / special / reload / warm-disk paint.
     * Aligns with warm paint TTL. Home always reconciles after painting warm disk
     * so delete/unpublish cannot linger for this full window.
     */
    public static final long FEED_NETWORK_MIN_INTERVAL_MS = 3 * 60 * 1000L;

    private static final String FEED_FILE_MAIN = "open_path_feed_page0_main.json";
    private static final String FEED_FILE_SPECIAL = "open_path_feed_page0_special.json";
    private static final String FEED_FILE_VIDEO = "open_path_feed_page0_video.json";
    private static final String FEED_FILE_LOCAL = "open_path_feed_page0_local.json";
    /** Legacy unscoped file (pre mode split) — treated as main only. */
    private static final String FEED_FILE_LEGACY = "open_path_feed_page0.json";

    /** One-shot force: set by Reload; cleared when the forced network call is enqueued. */
    private static volatile boolean pendingForceFeedNetwork = false;
    /** One-shot purge of legacy page-0 feed files after FEED_DISK_DISABLED. */
    private static volatile boolean legacyFeedDiskPurged = false;

    private OpenPathCache() {
    }

    private static String feedFileName(String feedMode) {
        if (FEED_MODE_SPECIAL.equals(feedMode)) return FEED_FILE_SPECIAL;
        if (FEED_MODE_VIDEO.equals(feedMode)) return FEED_FILE_VIDEO;
        if (FEED_MODE_LOCAL.equals(feedMode)) return FEED_FILE_LOCAL;
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
        // Feed disk / OfflineFeedStore no longer count — leave splash on identity
        // + translator (or language-selected route). Home always waits on network
        // for the article list.
        if (!hasTranslator(context)
                && !SharedPrefsData.getBool(
                        context, Constants.IS_LANGUAGE_SELECTED, Constants.PREF_NAME)) {
            // Still allow when MOVE_NEXT_SCREEN already says main (checked below).
            String peek = SharedPrefsData.getString(
                    context, Constants.MOVE_NEXT_SCREEN, Constants.PREF_NAME);
            if (peek == null || !"main".equalsIgnoreCase(peek)) {
                return false;
            }
        }
        String next = SharedPrefsData.getString(context, Constants.MOVE_NEXT_SCREEN, Constants.PREF_NAME);
        if (next == null || next.isEmpty()) {
            return SharedPrefsData.getBool(context, Constants.IS_LANGUAGE_SELECTED, Constants.PREF_NAME);
        }
        return "main".equalsIgnoreCase(next)
                || SharedPrefsData.getBool(context, Constants.IS_LANGUAGE_SELECTED, Constants.PREF_NAME);
    }

    /**
     * Play Store / returning users must not be forced through "Select Content Language".
     * True when local prefs already prove onboarding completed (language chosen, main route,
     * or draft+language identity). Does NOT treat "draft just created with language_id=0"
     * as complete — those users still see language pick.
     */
    public static boolean shouldSkipLanguageOnboarding(Context context) {
        if (context == null) return false;
        int lang = SharedPrefsData.getInt(context, Constants.LanguageID, Constants.PREF_NAME);
        if (lang > 0) return true;
        if (SharedPrefsData.getBool(context, Constants.IS_LANGUAGE_SELECTED, Constants.PREF_NAME)) {
            return true;
        }
        String next = SharedPrefsData.getString(context, Constants.MOVE_NEXT_SCREEN, Constants.PREF_NAME);
        if ("main".equalsIgnoreCase(next)) return true;
        return canFastOpenHome(context);
    }

    public static boolean shouldBlockOnSplash(Context context) {
        if (!hasUsableIdentity(context)) return true;
        long last = SharedPrefsData.getLong(context, Constants.OPEN_PATH_SPLASH_AT, Constants.PREF_NAME);
        if (last <= 0) return true;
        return (System.currentTimeMillis() - last) > SPLASH_TTL_MS;
    }

    /**
     * Returning user with identity whose last successful splash is inside {@link #SPLASH_TTL_MS}.
     * {@link com.anyNews.anynews.Activities.StartingActivity} uses {@link #canFastOpenHome}
     * for zero Retrofit on lean opens; this flag only documents TTL freshness.
     */
    public static boolean isLeanReturningOpen(Context context) {
        return canFastOpenHome(context) && !shouldBlockOnSplash(context);
    }

    public static void markSplashSuccess(Context context) {
        if (context == null) return;
        SharedPrefsData.putLong(context, Constants.OPEN_PATH_SPLASH_AT,
                System.currentTimeMillis(), Constants.PREF_NAME);
    }

    /**
     * True when a returning lean user should soft-refresh Splash_Screen for
     * fonts / admin config. Uses {@link #SPLASH_CONFIG_REFRESH_TTL_MS} (~24h).
     * First install / incomplete identity must still use the blocking splash path.
     */
    public static boolean shouldRefreshSplashConfig(Context context) {
        if (context == null) return false;
        if (!hasUsableIdentity(context)) return false;
        long last = SharedPrefsData.getLong(context, Constants.OPEN_PATH_SPLASH_AT, Constants.PREF_NAME);
        if (last <= 0) return true;
        return (System.currentTimeMillis() - last) > SPLASH_CONFIG_REFRESH_TTL_MS;
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
        if (FEED_DISK_DISABLED) return true;
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
        /** {@link #FEED_MODE_MAIN}, {@link #FEED_MODE_SPECIAL}, {@link #FEED_MODE_VIDEO},
         * or {@link #FEED_MODE_LOCAL}. */
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
        if (FEED_DISK_DISABLED) return false;
        // Offline cold open / mid-session drop: any matching main identity payload counts,
        // even past FEED_DISK_TTL (paint-only; network refresh still TTL-gated).
        // Special/video are online-only and never count as Offline News pack identity.
        return loadFeed(context, 0, FEED_MODE_MAIN) != null;
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
        if (FEED_DISK_DISABLED) return null;
        return loadFeedInternal(context, categoryId, feedMode, /*maxAgeMs*/ FEED_DISK_TTL_MS);
    }

    /**
     * Online first-paint — permanently disabled ({@link #FEED_DISK_DISABLED}).
     * Always null so Home keeps shimmer until live page-0.
     */
    @Nullable
    public static EncreptionRequest loadFeedForInstantPaint(Context context, int categoryId) {
        return loadFeedForInstantPaint(context, categoryId, FEED_MODE_MAIN);
    }

    @Nullable
    public static EncreptionRequest loadFeedForInstantPaint(
            Context context, int categoryId, String feedMode) {
        if (FEED_DISK_DISABLED) return null;
        // Warm online paint only — strip greeting so stale morning cannot seat.
        return payloadWithoutGreeting(loadFeed(context, categoryId, feedMode));
    }

    /** Offline feed paint removed — always null. */
    @Nullable
    public static EncreptionRequest loadFeedForOffline(Context context, int categoryId) {
        return null;
    }

    @Nullable
    public static EncreptionRequest loadFeedForOffline(
            Context context, int categoryId, String feedMode) {
        return null;
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
            // Legacy main-only file (no feedMode field) — never reuse for special/video/local.
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
                // Legacy unscoped snapshot → treat as main only (never special/video/local).
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
     * <p>
     * Main/Local disk never stores greeting rows (clone + strip). The live
     * {@code payload} passed by callers is not mutated — network convert still
     * receives the greeting.
     */
    public static void saveFeed(
            Context context, int categoryId, EncreptionRequest payload, String feedMode) {
        if (context == null || payload == null || TextUtils.isEmpty(payload.getData())) return;
        if (FEED_DISK_DISABLED) {
            // Never pack article list; purge legacy page-0 files once.
            if (!legacyFeedDiskPurged) {
                legacyFeedDiskPurged = true;
                clearFeed(context);
            }
            markFeedNetworkSuccess(context);
            return;
        }
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
            // Video Flash has no IST greeting inject — leave body as-is.
            snap.payload = FEED_MODE_VIDEO.equals(mode)
                    ? payload
                    : payloadWithoutGreeting(payload);
            if (snap.payload == null || TextUtils.isEmpty(snap.payload.getData())) {
                return;
            }
            File file = new File(
                    context.getApplicationContext().getCacheDir(), feedFileName(mode));
            writeUtf8(file, GSON.toJson(snap));
            markFeedNetworkSuccess(context);
        } catch (Exception e) {
            Logone.w(e, TAG + " saveFeed failed");
        }
    }

    /**
     * July-dumb OpenPath: drop sealed / typed greeting cards from a page-0 body.
     * Returns {@code payload} unchanged when nothing to strip; otherwise a Gson
     * clone with re-encrypted {@code data} so the live network object is untouched.
     */
    @Nullable
    static EncreptionRequest payloadWithoutGreeting(@Nullable EncreptionRequest payload) {
        if (payload == null || TextUtils.isEmpty(payload.getData())) {
            return payload;
        }
        try {
            String plain = MyUtils.decrypt(payload.getData());
            if (TextUtils.isEmpty(plain) || plain.length() < 8) {
                return payload;
            }
            JsonElement rootEl = new JsonParser().parse(plain);
            if (rootEl == null || !rootEl.isJsonObject()) {
                return payload;
            }
            JsonObject root = rootEl.getAsJsonObject();
            JsonElement dataEl = root.get("data");
            if (dataEl == null || !dataEl.isJsonArray()) {
                return payload;
            }
            JsonArray data = dataEl.getAsJsonArray();
            JsonArray kept = new JsonArray();
            boolean stripped = false;
            for (int i = 0; i < data.size(); i++) {
                JsonElement row = data.get(i);
                if (row != null && row.isJsonObject() && isGreetingJsonRow(row.getAsJsonObject())) {
                    stripped = true;
                    continue;
                }
                kept.add(row);
            }
            if (!stripped) {
                return payload;
            }
            root.add("data", kept);
            EncreptionRequest copy = GSON.fromJson(GSON.toJson(payload), EncreptionRequest.class);
            if (copy == null) {
                return payload;
            }
            copy.setData(MyUtils.encrypt(GSON.toJson(root)));
            return copy;
        } catch (Exception e) {
            Logone.w(e, TAG + " payloadWithoutGreeting failed");
            return payload;
        }
    }

    /** Match HomePage greeting identity without depending on applied* memory. */
    private static boolean isGreetingJsonRow(@Nullable JsonObject o) {
        if (o == null) {
            return false;
        }
        try {
            JsonElement seal = o.get("greeting_seal_slot");
            if (seal != null && seal.isJsonPrimitive() && !seal.isJsonNull()) {
                String s = seal.getAsString();
                if (s != null && !s.trim().isEmpty()) {
                    return true;
                }
            }
        } catch (Exception ignored) {
        }
        String pt = null;
        try {
            JsonElement ptEl = o.get("post_type");
            if (ptEl != null && ptEl.isJsonPrimitive() && !ptEl.isJsonNull()) {
                pt = ptEl.getAsString();
            }
        } catch (Exception ignored) {
        }
        if (pt != null && ("greetings".equalsIgnoreCase(pt)
                || "greeting".equalsIgnoreCase(pt))) {
            return true;
        }
        try {
            JsonElement end = o.get("greeting_slot_end");
            if (end != null && end.isJsonPrimitive() && !end.isJsonNull()
                    && end.getAsLong() > 0L
                    && pt != null
                    && ("full_image".equalsIgnoreCase(pt)
                    || "greetings".equalsIgnoreCase(pt)
                    || "greeting".equalsIgnoreCase(pt))) {
                return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    public static void clearFeed(Context context) {
        if (context == null) return;
        deleteQuiet(context, FEED_FILE_MAIN);
        deleteQuiet(context, FEED_FILE_SPECIAL);
        deleteQuiet(context, FEED_FILE_VIDEO);
        deleteQuiet(context, FEED_FILE_LOCAL);
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
