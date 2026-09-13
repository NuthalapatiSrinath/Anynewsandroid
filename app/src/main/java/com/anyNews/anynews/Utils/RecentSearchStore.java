package com.anyNews.anynews.Utils;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/** Local recent search terms (max 12). No server call. */
public final class RecentSearchStore {

    private static final String KEY = "recent_story_searches_v1";
    private static final int MAX = 12;

    private RecentSearchStore() {}

    @NonNull
    public static List<String> load(@Nullable Context context) {
        ArrayList<String> out = new ArrayList<>();
        if (context == null) return out;
        try {
            String raw = SharedPrefsData.getString(context, KEY, Constants.PREF_NAME);
            if (TextUtils.isEmpty(raw)) return out;
            JSONArray arr = new JSONArray(raw);
            for (int i = 0; i < arr.length(); i++) {
                String s = arr.optString(i, "").trim();
                if (!TextUtils.isEmpty(s)) out.add(s);
            }
        } catch (Exception ignored) {
        }
        return out;
    }

    public static void add(@Nullable Context context, @Nullable String term) {
        if (context == null || TextUtils.isEmpty(term)) return;
        String clean = term.trim();
        if (clean.isEmpty()) return;
        List<String> cur = load(context);
        ArrayList<String> next = new ArrayList<>();
        next.add(clean);
        for (String s : cur) {
            if (s.equalsIgnoreCase(clean)) continue;
            next.add(s);
            if (next.size() >= MAX) break;
        }
        save(context, next);
    }

    public static void remove(@Nullable Context context, @Nullable String term) {
        if (context == null || TextUtils.isEmpty(term)) return;
        List<String> cur = load(context);
        ArrayList<String> next = new ArrayList<>();
        for (String s : cur) {
            if (s.equalsIgnoreCase(term.trim())) continue;
            next.add(s);
        }
        save(context, next);
    }

    public static void clear(@Nullable Context context) {
        if (context == null) return;
        save(context, new ArrayList<String>());
    }

    private static void save(Context context, List<String> items) {
        try {
            JSONArray arr = new JSONArray();
            for (String s : items) {
                arr.put(s);
            }
            SharedPrefsData.putString(context, KEY, arr.toString(), Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
    }
}
