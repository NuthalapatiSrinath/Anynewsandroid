package com.anyNews.anynews.panchang;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Device memory + disk cache for daily raasi phalalu packs.
 * <p>
 * Key: {@code raasi_v{CONTENT_VERSION}_lang{id}_{yyyy-MM-dd}} where date is Asia/Kolkata (IST).
 * Value: pack of all 12 rashis for that language + date.
 * TTL: until the IST calendar date changes (auto-invalidate on key mismatch); no multi-day keep.
 */
public final class RaasiDailyCache {

    private static final String TAG = "RaasiDailyCache";
    /** Bump when template sets / schema change so stale packs regenerate. */
    public static final int CONTENT_VERSION = 3;
    public static final String DATE_ZONE_ID = "Asia/Kolkata";

    private static final Gson GSON = new Gson();

    @Nullable private static volatile String memoryKey;
    @Nullable private static volatile Pack memoryPack;

    private RaasiDailyCache() {
    }

    @NonNull
    public static TimeZone dateZone() {
        return TimeZone.getTimeZone(DATE_ZONE_ID);
    }

    /** Local/IST calendar date key yyyy-MM-dd. */
    @NonNull
    public static String todayDateKey() {
        java.util.Calendar c = java.util.Calendar.getInstance(dateZone());
        return String.format(Locale.US, "%04d-%02d-%02d",
                c.get(java.util.Calendar.YEAR),
                c.get(java.util.Calendar.MONTH) + 1,
                c.get(java.util.Calendar.DAY_OF_MONTH));
    }

    @NonNull
    public static String dateKeyFrom(@NonNull java.util.Calendar day) {
        // Prefer fields as-provided; callers should use IST calendar when possible.
        return String.format(Locale.US, "%04d-%02d-%02d",
                day.get(java.util.Calendar.YEAR),
                day.get(java.util.Calendar.MONTH) + 1,
                day.get(java.util.Calendar.DAY_OF_MONTH));
    }

    @NonNull
    public static String packKey(int languageId, @NonNull String dateKey) {
        return String.format(Locale.US, "raasi_v%d_lang%d_%s",
                CONTENT_VERSION, languageId, dateKey);
    }

    @Nullable
    public static Pack load(@Nullable Context context, int languageId, @NonNull String dateKey) {
        String key = packKey(languageId, dateKey);
        Pack mem = memoryPack;
        if (key.equals(memoryKey) && mem != null && mem.isValidFor(key, dateKey, languageId)) {
            return mem;
        }
        if (context == null) return null;
        File f = file(context, key);
        if (!f.exists() || f.length() < 32) return null;
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder((int) Math.min(f.length(), Integer.MAX_VALUE));
            char[] buf = new char[4096];
            int n;
            while ((n = br.read(buf)) >= 0) {
                sb.append(buf, 0, n);
            }
            Pack pack = GSON.fromJson(sb.toString(), Pack.class);
            if (pack == null || !pack.isValidFor(key, dateKey, languageId)) {
                //noinspection ResultOfMethodCallIgnored
                f.delete();
                return null;
            }
            memoryKey = key;
            memoryPack = pack;
            return pack;
        } catch (Exception e) {
            Log.w(TAG, "load failed: " + e.getMessage());
            return null;
        }
    }

    public static void save(@Nullable Context context, @NonNull Pack pack) {
        if (context == null || pack.items == null || pack.items.length != RaasiContent.COUNT) {
            return;
        }
        String key = packKey(pack.languageId, pack.dateKey);
        pack.cacheKey = key;
        pack.contentVersion = CONTENT_VERSION;
        try {
            String json = GSON.toJson(pack);
            File f = file(context, key);
            File tmp = new File(f.getAbsolutePath() + ".tmp");
            try (FileOutputStream fos = new FileOutputStream(tmp)) {
                fos.write(json.getBytes(StandardCharsets.UTF_8));
                fos.flush();
            }
            if (f.exists()) {
                //noinspection ResultOfMethodCallIgnored
                f.delete();
            }
            if (!tmp.renameTo(f)) {
                try (FileOutputStream fos = new FileOutputStream(f)) {
                    fos.write(json.getBytes(StandardCharsets.UTF_8));
                }
                //noinspection ResultOfMethodCallIgnored
                tmp.delete();
            }
            memoryKey = key;
            memoryPack = pack;
            // Drop other-day files to keep store small (best-effort).
            pruneStale(context, pack.dateKey);
        } catch (Exception e) {
            Log.w(TAG, "save failed: " + e.getMessage());
        }
    }

    /** Drop packs whose dateKey is not today (IST). */
    public static void pruneStale(@Nullable Context context, @NonNull String keepDateKey) {
        if (context == null) return;
        File dir = dir(context);
        File[] files = dir.listFiles();
        if (files == null) return;
        String needle = "_" + keepDateKey + ".json";
        for (File f : files) {
            String n = f.getName();
            if (n.startsWith("raasi_v") && n.endsWith(".json") && !n.contains(needle)) {
                //noinspection ResultOfMethodCallIgnored
                f.delete();
            }
        }
        String mem = memoryKey;
        if (mem != null && !mem.endsWith("_" + keepDateKey) && !mem.contains("_" + keepDateKey)) {
            // key format ends with date
            if (!mem.endsWith(keepDateKey)) {
                memoryKey = null;
                memoryPack = null;
            }
        }
    }

    public static void clearMemory() {
        memoryKey = null;
        memoryPack = null;
    }

    @NonNull
    private static File dir(@NonNull Context context) {
        File dir = new File(context.getApplicationContext().getFilesDir(), "raasi");
        //noinspection ResultOfMethodCallIgnored
        dir.mkdirs();
        return dir;
    }

    @NonNull
    private static File file(@NonNull Context context, @NonNull String key) {
        String safe = key.replaceAll("[^a-zA-Z0-9._-]", "_");
        return new File(dir(context), safe + ".json");
    }

    /** On-disk / memory pack for one language + day (all 12 rashis). */
    public static final class Pack {
        @SerializedName("contentVersion")
        public int contentVersion;
        @SerializedName("languageId")
        public int languageId;
        @SerializedName("dateKey")
        public String dateKey;
        @SerializedName("cacheKey")
        public String cacheKey;
        @SerializedName("items")
        public Item[] items;

        boolean isValidFor(@NonNull String expectKey, @NonNull String dateKey, int languageId) {
            if (contentVersion != CONTENT_VERSION) return false;
            if (this.languageId != languageId) return false;
            if (this.dateKey == null || !this.dateKey.equals(dateKey)) return false;
            if (items == null || items.length != RaasiContent.COUNT) return false;
            for (Item it : items) {
                if (it == null || it.body == null) return false;
            }
            return expectKey.equals(packKey(languageId, dateKey));
        }

        @Nullable
        public Item get(int rashiId0) {
            int id = RaasiContent.clampId(rashiId0);
            if (items == null || id >= items.length) return null;
            return items[id];
        }
    }

    public static final class Item {
        @SerializedName("rashiId")
        public int rashiId;
        @SerializedName("body")
        public String body;
        @SerializedName("color")
        public String color;
        @SerializedName("numbers")
        public String numbers;
        @SerializedName("direction")
        public String direction;
    }
}
