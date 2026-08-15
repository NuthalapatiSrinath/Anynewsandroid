package com.anyNews.anynews.Utils;



import android.content.Context;

import android.text.TextUtils;



import androidx.annotation.Nullable;



import com.anyNews.anynews.testing.PostItem;

import com.google.gson.Gson;



import java.io.BufferedReader;

import java.io.File;

import java.io.FileInputStream;

import java.io.FileOutputStream;

import java.io.InputStreamReader;

import java.nio.charset.StandardCharsets;

import java.util.concurrent.ConcurrentHashMap;



/**

 * Disk + memory cache for wallet history first pages so Total Cash / Earnings

 * (article transactions + payment history) can paint immediately on open.

 * Does not change amounts or server business logic — UI snapshot only.

 */

public final class WalletHistoryCache {



    private static final String TAG = "WalletHistoryCache";

    private static final Gson GSON = new Gson();



    /** Keep page-0 usable long enough for instant reopens; network soft-refresh updates it. */

    public static final long TTL_MS = 24 * 60 * 60 * 1000L;



    /** Aligns roughly with server reporterWallet TTL (~300s). */

    public static final long STALE_MS = 180 * 1000L;



    public static final String TYPE_ARTICLE = "article";

    public static final String TYPE_REDEEM = "redeem";



    private static final String FILE_ARTICLE = "wallet_history_article_page0.json";

    private static final String FILE_REDEEM = "wallet_history_redeem_page0.json";

    private static final String PREF_TOTAL_POINTS = "wallet_history_total_points_display";



    /** Hot path: avoid disk IO on Profile → Earnings open. */

    private static final ConcurrentHashMap<String, Snapshot> MEMORY = new ConcurrentHashMap<>();



    private WalletHistoryCache() {

    }



    public static final class Snapshot {

        public long savedAt;

        public long userId;

        public int draftId;

        public PostItem payload;

    }



    @Nullable

    public static PostItem load(Context context, String type) {

        Snapshot snap = loadSnapshot(context, type);

        return snap != null ? snap.payload : null;

    }



    /**

     * Age of cache in ms, or -1 if none. Used to decide silent prefetch refresh.

     */

    public static long ageMs(Context context, String type) {

        Snapshot snap = loadSnapshot(context, type);

        if (snap == null) return -1L;

        return Math.max(0L, System.currentTimeMillis() - snap.savedAt);

    }



    public static boolean isMissingOrStale(Context context, String type) {

        long age = ageMs(context, type);

        if (age < 0) return true;

        return age >= STALE_MS;

    }



    @Nullable

    public static Snapshot loadSnapshot(Context context, String type) {

        if (context == null || TextUtils.isEmpty(type)) return null;

        try {

            long userId = SharedPrefsData.getLong(context, Constants.USER_ID, Constants.PREF_NAME);

            int draftId = SharedPrefsData.getInt(context, Constants.DraftID, Constants.PREF_NAME);

            String memKey = memKey(type, userId, draftId);



            Snapshot mem = MEMORY.get(memKey);

            if (mem != null && isSnapshotUsable(mem, userId, draftId)) {

                return mem;

            }



            File file = fileFor(context, type);

            if (!file.exists() || file.length() < 8) return null;

            String json = readUtf8(file);

            if (TextUtils.isEmpty(json)) return null;

            Snapshot snap = GSON.fromJson(json, Snapshot.class);

            if (!isSnapshotUsable(snap, userId, draftId)) return null;

            MEMORY.put(memKey, snap);

            return snap;

        } catch (Exception e) {

            Logone.w(e, TAG + " load failed type=" + type);

            return null;

        }

    }



    private static boolean isSnapshotUsable(Snapshot snap, long userId, int draftId) {

        if (snap == null || snap.payload == null) return false;

        if ((System.currentTimeMillis() - snap.savedAt) > TTL_MS) return false;

        if (snap.userId != 0 && userId != 0 && snap.userId != userId) return false;

        if (snap.draftId != 0 && draftId != 0 && snap.draftId != draftId) return false;

        return true;

    }



    public static void save(Context context, String type, PostItem payload) {

        if (context == null || payload == null || TextUtils.isEmpty(type)) return;

        try {

            Snapshot snap = new Snapshot();

            snap.savedAt = System.currentTimeMillis();

            snap.userId = SharedPrefsData.getLong(context, Constants.USER_ID, Constants.PREF_NAME);

            snap.draftId = SharedPrefsData.getInt(context, Constants.DraftID, Constants.PREF_NAME);

            snap.payload = payload;



            String memKey = memKey(type, snap.userId, snap.draftId);

            MEMORY.put(memKey, snap);



            writeUtf8(fileFor(context, type), GSON.toJson(snap));

            if (payload.getTotalPoints() != null) {

                SharedPrefsData.putString(context, PREF_TOTAL_POINTS,

                        String.valueOf(payload.getTotalPoints()), Constants.PREF_NAME);

            }

        } catch (Exception e) {

            Logone.w(e, TAG + " save failed type=" + type);

        }

    }



    /** Last known total points string for instant header paint (no currency symbol). */

    @Nullable

    public static String loadTotalPointsDisplay(Context context) {

        if (context == null) return null;

        try {

            return SharedPrefsData.getString(context, PREF_TOTAL_POINTS, Constants.PREF_NAME);

        } catch (Exception e) {

            return null;

        }

    }



    public static void saveTotalPointsDisplay(Context context, String display) {

        if (context == null || display == null) return;

        try {

            String digits = display.replace("\u20B9", "").replace("₹", "").trim();

            if (!digits.isEmpty()) {

                SharedPrefsData.putString(context, PREF_TOTAL_POINTS, digits, Constants.PREF_NAME);

            }

        } catch (Exception ignored) {

        }

    }



    private static String memKey(String type, long userId, int draftId) {

        return type + ":" + userId + ":" + draftId;

    }



    /**

     * Prefer filesDir so OS doesn't wipe under storage pressure (still not forever).

     * Migrates legacy cacheDir file once if present.

     */

    private static File fileFor(Context context, String type) {

        String name = TYPE_REDEEM.equals(type) ? FILE_REDEEM : FILE_ARTICLE;

        File primary = new File(context.getApplicationContext().getFilesDir(), name);

        if (primary.exists()) return primary;

        File legacy = new File(context.getApplicationContext().getCacheDir(), name);

        if (legacy.exists() && legacy.length() >= 8) {

            // Best-effort migrate for next open

            try {

                String json = readUtf8(legacy);

                if (!TextUtils.isEmpty(json)) {

                    writeUtf8(primary, json);

                }

            } catch (Exception ignored) {

            }

            if (primary.exists()) return primary;

            return legacy;

        }

        return primary;

    }



    private static String readUtf8(File file) throws Exception {

        StringBuilder sb = new StringBuilder((int) Math.min(file.length(), 1_000_000));

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


