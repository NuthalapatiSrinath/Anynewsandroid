package com.anyNews.anynews.Utils;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Local recent search terms with optional post_id + timestamp.
 * TTL comes from admin {@code search_recent_ttl_days} (splash/runtime; default 7).
 * Expiry is pruned locally on open — no network call.
 */
public final class RecentSearchStore {

    private static final String KEY = "recent_story_searches_v2";
    /** Legacy string-array key — migrated once on first load. */
    private static final String KEY_LEGACY = "recent_story_searches_v1";
    private static final int MAX = 12;
    private static final int DEFAULT_TTL_DAYS = 7;

    private RecentSearchStore() {}

    public static int ttlDays(@Nullable Context context) {
        int days = DEFAULT_TTL_DAYS;
        if (context != null) {
            try {
                days = SharedPrefsData.getInt(
                        context, Constants.SEARCH_RECENT_TTL_DAYS, Constants.PREF_NAME);
            } catch (Exception ignored) {
            }
        }
        if (days <= 0) days = DEFAULT_TTL_DAYS;
        if (days > 90) days = 90;
        return days;
    }

    public static void saveTtlDays(@Nullable Context context, @Nullable Integer days) {
        if (context == null || days == null) return;
        int d = days;
        if (d <= 0) d = DEFAULT_TTL_DAYS;
        if (d > 90) d = 90;
        SharedPrefsData.putInt(context, Constants.SEARCH_RECENT_TTL_DAYS, d, Constants.PREF_NAME);
    }

    @NonNull
    public static List<String> load(@Nullable Context context) {
        ArrayList<String> out = new ArrayList<>();
        for (Entry e : loadEntries(context)) {
            if (!TextUtils.isEmpty(e.term)) out.add(e.term);
        }
        return out;
    }

    @NonNull
    public static List<Entry> loadEntries(@Nullable Context context) {
        ArrayList<Entry> out = new ArrayList<>();
        if (context == null) return out;
        long now = System.currentTimeMillis();
        long ttlMs = TimeUnit.DAYS.toMillis(ttlDays(context));
        try {
            migrateLegacyIfNeeded(context);
            String raw = SharedPrefsData.getString(context, KEY, Constants.PREF_NAME);
            if (TextUtils.isEmpty(raw)) return out;
            JSONArray arr = new JSONArray(raw);
            boolean pruned = false;
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.optJSONObject(i);
                if (o == null) continue;
                String term = o.optString("t", "").trim();
                if (TextUtils.isEmpty(term)) continue;
                long ts = o.optLong("ts", 0L);
                if (ts <= 0L) ts = now;
                if (now - ts > ttlMs) {
                    pruned = true;
                    continue;
                }
                long postId = o.optLong("pid", 0L);
                out.add(new Entry(term, ts, postId));
            }
            if (pruned) {
                saveEntries(context, out);
            }
        } catch (Exception ignored) {
        }
        return out;
    }

    public static void add(@Nullable Context context, @Nullable String term) {
        add(context, term, 0L);
    }

    public static void add(
            @Nullable Context context, @Nullable String term, long postId) {
        if (context == null || TextUtils.isEmpty(term)) return;
        String clean = term.trim();
        if (clean.isEmpty()) return;
        List<Entry> cur = loadEntries(context);
        ArrayList<Entry> next = new ArrayList<>();
        next.add(new Entry(clean, System.currentTimeMillis(), postId > 0L ? postId : 0L));
        for (Entry e : cur) {
            if (e.term.equalsIgnoreCase(clean)) continue;
            next.add(e);
            if (next.size() >= MAX) break;
        }
        saveEntries(context, next);
    }

    public static void remove(@Nullable Context context, @Nullable String term) {
        if (context == null || TextUtils.isEmpty(term)) return;
        List<Entry> cur = loadEntries(context);
        ArrayList<Entry> next = new ArrayList<>();
        for (Entry e : cur) {
            if (e.term.equalsIgnoreCase(term.trim())) continue;
            next.add(e);
        }
        saveEntries(context, next);
    }

    public static void clear(@Nullable Context context) {
        if (context == null) return;
        saveEntries(context, new ArrayList<Entry>());
    }

    private static void migrateLegacyIfNeeded(Context context) {
        try {
            String v2 = SharedPrefsData.getString(context, KEY, Constants.PREF_NAME);
            if (!TextUtils.isEmpty(v2)) return;
            String legacy = SharedPrefsData.getString(context, KEY_LEGACY, Constants.PREF_NAME);
            if (TextUtils.isEmpty(legacy)) return;
            JSONArray arr = new JSONArray(legacy);
            ArrayList<Entry> next = new ArrayList<>();
            long now = System.currentTimeMillis();
            for (int i = 0; i < arr.length() && next.size() < MAX; i++) {
                String s = arr.optString(i, "").trim();
                if (!TextUtils.isEmpty(s)) {
                    next.add(new Entry(s, now, 0L));
                }
            }
            saveEntries(context, next);
            SharedPrefsData.putString(context, KEY_LEGACY, "", Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
    }

    private static void saveEntries(Context context, List<Entry> items) {
        try {
            JSONArray arr = new JSONArray();
            for (Entry e : items) {
                if (e == null || TextUtils.isEmpty(e.term)) continue;
                JSONObject o = new JSONObject();
                o.put("t", e.term);
                o.put("ts", e.ts > 0L ? e.ts : System.currentTimeMillis());
                if (e.postId > 0L) o.put("pid", e.postId);
                arr.put(o);
            }
            SharedPrefsData.putString(context, KEY, arr.toString(), Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
    }

    public static final class Entry {
        public final String term;
        public final long ts;
        public final long postId;

        public Entry(String term, long ts, long postId) {
            this.term = term;
            this.ts = ts;
            this.postId = postId;
        }
    }
}
