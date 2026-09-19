package com.anyNews.anynews.Utils;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Lightweight client-side typo / intent matching (edit distance ≤ 1–2).
 * Used against recent searches + last suggestion texts — no extra network.
 */
public final class SearchFuzzyHelper {

    private SearchFuzzyHelper() {}

    /**
     * Ranked close matches for {@code query} from {@code candidates}.
     * Prefer distance 1 (nofty→nifty), then 2 for longer tokens.
     */
    @NonNull
    public static List<String> closeMatches(
            @Nullable String query, @Nullable List<String> candidates, int max) {
        ArrayList<String> out = new ArrayList<>();
        if (TextUtils.isEmpty(query) || candidates == null || candidates.isEmpty() || max <= 0) {
            return out;
        }
        String q = query.trim().toLowerCase(Locale.US);
        if (q.length() < 3) return out;
        int maxDist = q.length() <= 5 ? 1 : 2;

        ArrayList<Scored> scored = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (String raw : candidates) {
            if (TextUtils.isEmpty(raw)) continue;
            String c = raw.trim();
            if (c.isEmpty()) continue;
            String cl = c.toLowerCase(Locale.US);
            if (cl.equals(q)) continue;
            if (!seen.add(cl)) continue;
            // Prefer token-level distance for multi-word candidates.
            int dist = bestTokenDistance(q, cl);
            if (dist <= 0 || dist > maxDist) continue;
            // Same first letter boosts confidence for short typos.
            int boost = (q.charAt(0) == cl.charAt(0)) ? 0 : 1;
            scored.add(new Scored(c, dist + boost));
        }
        Collections.sort(
                scored,
                new Comparator<Scored>() {
                    @Override
                    public int compare(Scored a, Scored b) {
                        if (a.score != b.score) return a.score - b.score;
                        return a.text.compareToIgnoreCase(b.text);
                    }
                });
        for (Scored s : scored) {
            out.add(s.text);
            if (out.size() >= max) break;
        }
        return out;
    }

    private static int bestTokenDistance(String q, String candidate) {
        int best = levenshtein(q, candidate, 3);
        String[] tokens = candidate.split("\\s+");
        for (String t : tokens) {
            if (t.length() < 3) continue;
            int d = levenshtein(q, t, 3);
            if (d >= 0 && (best < 0 || d < best)) best = d;
        }
        return best;
    }

    /** Bounded Levenshtein; returns -1 when distance would exceed maxDist. */
    static int levenshtein(@NonNull String a, @NonNull String b, int maxDist) {
        int n = a.length();
        int m = b.length();
        if (Math.abs(n - m) > maxDist) return -1;
        if (n == 0) return m <= maxDist ? m : -1;
        if (m == 0) return n <= maxDist ? n : -1;
        int[] prev = new int[m + 1];
        int[] cur = new int[m + 1];
        for (int j = 0; j <= m; j++) prev[j] = j;
        for (int i = 1; i <= n; i++) {
            cur[0] = i;
            int rowMin = cur[0];
            char ca = a.charAt(i - 1);
            for (int j = 1; j <= m; j++) {
                int cost = ca == b.charAt(j - 1) ? 0 : 1;
                int del = prev[j] + 1;
                int ins = cur[j - 1] + 1;
                int sub = prev[j - 1] + cost;
                int v = Math.min(del, Math.min(ins, sub));
                cur[j] = v;
                if (v < rowMin) rowMin = v;
            }
            if (rowMin > maxDist) return -1;
            int[] tmp = prev;
            prev = cur;
            cur = tmp;
        }
        int dist = prev[m];
        return dist <= maxDist ? dist : -1;
    }

    private static final class Scored {
        final String text;
        final int score;

        Scored(String text, int score) {
            this.text = text;
            this.score = score;
        }
    }
}
