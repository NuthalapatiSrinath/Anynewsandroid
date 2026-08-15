package com.anyNews.anynews.Utils;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Disk + memory snapshot of the Status Videos pack for instant reopen paint.
 * Network soft-refresh still runs when Status Videos opens (unless recent).
 */
public final class StatusVideosCache {

    private static final String TAG = "StatusVideosCache";
    private static final Gson GSON = new Gson();

    /** Prefer warm snapshot for this long before forcing network-only paint. */
    public static final long DISK_TTL_MS = 60 * 60 * 1000L; // 1h match server pack
    /** Still paint (stale) while refreshing up to 24h. */
    public static final long STALE_PAINT_TTL_MS = 24 * 60 * 60 * 1000L;

    private static final String FILE_NAME = "status_videos_pack.json";

    private static volatile String memoryKey;
    @Nullable private static volatile ArrayList<ModelClass.Datum> memoryList;
    private static volatile long memorySavedAt;

    private StatusVideosCache() {
    }

    private static String identityKey(Context context) {
        int draft = SharedPrefsData.getInt(context, Constants.DraftID, Constants.PREF_NAME);
        int lang = SharedPrefsData.getInt(context, Constants.LanguageID, Constants.PREF_NAME);
        return draft + "|" + lang;
    }

    private static File file(Context context) {
        return new File(context.getApplicationContext().getCacheDir(), FILE_NAME);
    }

    public static final class Snapshot {
        public long savedAt;
        public int languageId;
        public int draftId;
        public ArrayList<ModelClass.Datum> items;
    }

    @Nullable
    public static ArrayList<ModelClass.Datum> loadForInstantPaint(Context context) {
        if (context == null) return null;
        Context app = context.getApplicationContext();
        String key = identityKey(app);
        if (key.equals(memoryKey) && memoryList != null && !memoryList.isEmpty()) {
            if ((System.currentTimeMillis() - memorySavedAt) <= STALE_PAINT_TTL_MS) {
                return new ArrayList<>(memoryList);
            }
        }
        Snapshot snap = readFile(app);
        if (snap == null || snap.items == null || snap.items.isEmpty()) return null;
        if (snap.draftId != SharedPrefsData.getInt(app, Constants.DraftID, Constants.PREF_NAME)
                || snap.languageId != SharedPrefsData.getInt(
                app, Constants.LanguageID, Constants.PREF_NAME)) {
            return null;
        }
        if ((System.currentTimeMillis() - snap.savedAt) > STALE_PAINT_TTL_MS) {
            return null;
        }
        memoryKey = key;
        memoryList = new ArrayList<>(snap.items);
        memorySavedAt = snap.savedAt;
        return new ArrayList<>(snap.items);
    }

    /** True when snapshot is warm enough that network can soft-refresh only. */
    public static boolean isWarm(Context context) {
        if (context == null) return false;
        Context app = context.getApplicationContext();
        if (identityKey(app).equals(memoryKey) && memoryList != null && !memoryList.isEmpty()) {
            return (System.currentTimeMillis() - memorySavedAt) <= DISK_TTL_MS;
        }
        Snapshot snap = readFile(app);
        if (snap == null || snap.items == null || snap.items.isEmpty()) return false;
        if (snap.draftId != SharedPrefsData.getInt(app, Constants.DraftID, Constants.PREF_NAME)
                || snap.languageId != SharedPrefsData.getInt(
                app, Constants.LanguageID, Constants.PREF_NAME)) {
            return false;
        }
        return (System.currentTimeMillis() - snap.savedAt) <= DISK_TTL_MS;
    }

    public static void save(Context context, List<ModelClass.Datum> items) {
        if (context == null || items == null || items.isEmpty()) return;
        Context app = context.getApplicationContext();
        try {
            Snapshot snap = new Snapshot();
            snap.savedAt = System.currentTimeMillis();
            snap.draftId = SharedPrefsData.getInt(app, Constants.DraftID, Constants.PREF_NAME);
            snap.languageId = SharedPrefsData.getInt(app, Constants.LanguageID, Constants.PREF_NAME);
            snap.items = new ArrayList<>(items);
            writeUtf8(file(app), GSON.toJson(snap));
            memoryKey = identityKey(app);
            memoryList = new ArrayList<>(items);
            memorySavedAt = snap.savedAt;
        } catch (Exception e) {
            try {
                Logone.w(e, TAG + " save failed");
            } catch (Exception ignored) {
            }
        }
    }

    @Nullable
    private static Snapshot readFile(Context app) {
        File f = file(app);
        if (!f.exists() || f.length() < 16) return null;
        try {
            String json = readUtf8(f);
            if (TextUtils.isEmpty(json)) return null;
            Snapshot snap = GSON.fromJson(json, Snapshot.class);
            if (snap == null || snap.items == null) return null;
            // Ensure list type after Gson (items may be rehydrated correctly via ArrayList).
            return snap;
        } catch (Exception e) {
            return null;
        }
    }

    private static String readUtf8(File file) throws Exception {
        StringBuilder sb = new StringBuilder((int) Math.min(file.length(), 4_000_000));
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
