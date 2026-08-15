package com.anyNews.anynews.Utils;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.anyNews.anynews.models.PostsModel;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-tab cache for reporter post-status lists (approved / pending / rejected).
 * Memory map for instant tab switches; disk files for cold open / offline.
 * Status codes match API: 0=pending, 1=approved, 2=rejected.
 */
public final class PostStatusListCache {

    private static final String TAG = "PostStatusListCache";
    private static final Gson GSON = new Gson();

    /** In-memory lists keyed by identity + status. */
    private static final Map<String, List<PostsModel.Datum>> MEMORY = new ConcurrentHashMap<>();
    /** Status keys we successfully loaded (incl. empty) this process or from disk. */
    private static final Map<String, Boolean> LOADED = new ConcurrentHashMap<>();

    private PostStatusListCache() {}

    public static final class Snapshot {
        public long savedAt;
        public int draftId;
        public String mobile;
        public int status;
        public List<PostsModel.Datum> posts;
    }

    @NonNull
    private static String key(int draftId, @Nullable String mobile, int status) {
        String m = mobile != null ? mobile.trim() : "";
        return draftId + "|" + m + "|" + status;
    }

    @NonNull
    private static File fileFor(@NonNull Context context, int draftId, @Nullable String mobile, int status) {
        String safeMobile = (mobile != null ? mobile.replaceAll("\\D", "") : "0");
        if (safeMobile.isEmpty()) safeMobile = "0";
        String name = "post_status_" + draftId + "_" + safeMobile + "_s" + status + "_v1.json";
        return new File(context.getApplicationContext().getFilesDir(), name);
    }

    /**
     * True if we have a successful load recorded for this tab (list may be empty).
     */
    public static boolean hasLoaded(
            @Nullable Context context, int draftId, @Nullable String mobile, int status) {
        String k = key(draftId, mobile, status);
        if (Boolean.TRUE.equals(LOADED.get(k))) {
            return true;
        }
        return loadFromDisk(context, draftId, mobile, status) != null;
    }

    /**
     * Returns a defensive copy of cached posts, or null if never loaded.
     * Empty list means last known result was empty (not "unknown").
     */
    @Nullable
    public static List<PostsModel.Datum> load(
            @Nullable Context context, int draftId, @Nullable String mobile, int status) {
        String k = key(draftId, mobile, status);
        List<PostsModel.Datum> mem = MEMORY.get(k);
        if (mem != null) {
            return new ArrayList<>(mem);
        }
        List<PostsModel.Datum> disk = loadFromDisk(context, draftId, mobile, status);
        if (disk != null) {
            MEMORY.put(k, new ArrayList<>(disk));
            LOADED.put(k, Boolean.TRUE);
            return new ArrayList<>(disk);
        }
        return null;
    }

    /**
     * Persist a successful network (or empty) result for a tab.
     * Null {@code posts} is treated as empty list.
     */
    public static void save(
            @Nullable Context context,
            int draftId,
            @Nullable String mobile,
            int status,
            @Nullable List<PostsModel.Datum> posts) {
        List<PostsModel.Datum> copy =
                posts != null ? new ArrayList<>(posts) : new ArrayList<PostsModel.Datum>();
        String k = key(draftId, mobile, status);
        MEMORY.put(k, new ArrayList<>(copy));
        LOADED.put(k, Boolean.TRUE);

        if (context == null) return;
        try {
            Snapshot snap = new Snapshot();
            snap.savedAt = System.currentTimeMillis();
            snap.draftId = draftId;
            snap.mobile = mobile != null ? mobile : "";
            snap.status = status;
            snap.posts = copy;
            File file = fileFor(context, draftId, mobile, status);
            writeUtf8(file, GSON.toJson(snap));
            showLog("save status=" + status + " n=" + copy.size());
        } catch (Exception e) {
            showLog("save failed: " + e.getMessage());
        }
    }

    @Nullable
    private static List<PostsModel.Datum> loadFromDisk(
            @Nullable Context context, int draftId, @Nullable String mobile, int status) {
        if (context == null) return null;
        try {
            File file = fileFor(context, draftId, mobile, status);
            if (!file.exists() || file.length() < 8) return null;
            String json = readUtf8(file);
            if (TextUtils.isEmpty(json)) return null;
            Snapshot snap = GSON.fromJson(json, Snapshot.class);
            if (snap == null) return null;
            if (snap.draftId > 0 && draftId > 0 && snap.draftId != draftId) return null;
            String snapMobile = snap.mobile != null ? snap.mobile.replaceAll("\\D", "") : "";
            String curMobile = mobile != null ? mobile.replaceAll("\\D", "") : "";
            if (!snapMobile.isEmpty()
                    && !curMobile.isEmpty()
                    && !snapMobile.equals(curMobile)) {
                return null;
            }
            if (snap.posts == null) {
                return new ArrayList<>();
            }
            return new ArrayList<>(snap.posts);
        } catch (Exception e) {
            showLog("load disk failed: " + e.getMessage());
            return null;
        }
    }

    private static String readUtf8(File file) throws Exception {
        StringBuilder sb = new StringBuilder((int) Math.min(file.length(), 2_000_000));
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
        if (file.exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }
        if (!tmp.renameTo(file)) {
            try (FileOutputStream fos = new FileOutputStream(file, false)) {
                fos.write(content.getBytes(StandardCharsets.UTF_8));
            }
            //noinspection ResultOfMethodCallIgnored
            tmp.delete();
        }
    }

    private static void showLog(String msg) {
        try {
            AppHelper.showLog(TAG + " " + msg);
        } catch (Exception ignored) {
        }
    }
}
