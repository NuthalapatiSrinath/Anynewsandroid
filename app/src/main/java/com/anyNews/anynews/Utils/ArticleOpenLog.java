package com.anyNews.anynews.Utils;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Local open/seen log for main-feed articles (flip settle / view open).
 * Used to rank Offline News unread-first; capped so prefs never grow unbounded.
 */
public final class ArticleOpenLog {

    private static final String TAG = "ArticleOpenLog";
    private static final String PREF_KEY = "offline_article_open_ids_v1";
    /** Hard cap — eviction of oldest first (LinkedHashSet insertion order). */
    public static final int MAX_IDS = 300;

    private static final Object LOCK = new Object();
    @Nullable private static LinkedHashSet<Long> memory;

    private ArticleOpenLog() {}

    public static void markOpened(@Nullable Context context, long postId) {
        if (context == null || postId <= 0) return;
        synchronized (LOCK) {
            LinkedHashSet<Long> set = loadLocked(context);
            if (set.contains(postId)) {
                // Move to newest end.
                set.remove(postId);
            }
            set.add(postId);
            while (set.size() > MAX_IDS) {
                Long first = set.iterator().next();
                set.remove(first);
            }
            persistLocked(context, set);
        }
    }

    public static boolean isOpened(@Nullable Context context, long postId) {
        if (context == null || postId <= 0) return false;
        synchronized (LOCK) {
            return loadLocked(context).contains(postId);
        }
    }

    @NonNull
    public static Set<Long> snapshotOpened(@Nullable Context context) {
        if (context == null) return new LinkedHashSet<>();
        synchronized (LOCK) {
            return new LinkedHashSet<>(loadLocked(context));
        }
    }

    public static boolean anyOpened(@Nullable Context context, @Nullable Collection<Long> postIds) {
        if (context == null || postIds == null || postIds.isEmpty()) return false;
        synchronized (LOCK) {
            Set<Long> opened = loadLocked(context);
            for (Long id : postIds) {
                if (id != null && id > 0 && opened.contains(id)) return true;
            }
        }
        return false;
    }

    @NonNull
    private static LinkedHashSet<Long> loadLocked(@NonNull Context context) {
        if (memory != null) return memory;
        LinkedHashSet<Long> set = new LinkedHashSet<>();
        try {
            String raw =
                    SharedPrefsData.getString(
                            context.getApplicationContext(), PREF_KEY, Constants.PREF_NAME);
            if (!TextUtils.isEmpty(raw)) {
                JSONArray arr = new JSONArray(raw);
                for (int i = 0; i < arr.length(); i++) {
                    long id = arr.optLong(i, 0L);
                    if (id > 0) set.add(id);
                }
            }
        } catch (Exception e) {
            Logone.w(e, TAG + " load failed");
        }
        while (set.size() > MAX_IDS) {
            Long first = set.iterator().next();
            set.remove(first);
        }
        memory = set;
        return set;
    }

    private static void persistLocked(@NonNull Context context, @NonNull LinkedHashSet<Long> set) {
        try {
            JSONArray arr = new JSONArray();
            for (Long id : set) {
                if (id != null && id > 0) arr.put(id);
            }
            SharedPrefsData.putString(
                    context.getApplicationContext(),
                    PREF_KEY,
                    arr.toString(),
                    Constants.PREF_NAME);
        } catch (Exception e) {
            Logone.w(e, TAG + " persist failed");
        }
    }
}
