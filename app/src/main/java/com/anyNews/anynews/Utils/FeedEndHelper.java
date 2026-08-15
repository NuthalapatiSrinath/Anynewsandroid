package com.anyNews.anynews.Utils;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.anyNews.anynews.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Sentinel FlipView page shown when a feed has no further articles
 * (online News exhausted, offline pack end, Chadivinavi / Read, Special, etc.).
 * Prevents a blank white FlipView child past the last real item.
 */
public final class FeedEndHelper {

    /** FlipperAdapter / ModelClass post_type for the end card. */
    public static final String POST_TYPE = "feed_end";

    /** Stable synthetic id — never a real server post. */
    public static final long POST_ID = -991001L;

    private FeedEndHelper() {}

    public static boolean isFeedEnd(@Nullable ModelClass.Datum d) {
        if (d == null) return false;
        String pt = d.getPostType();
        if (pt != null && POST_TYPE.equalsIgnoreCase(pt.trim())) {
            return true;
        }
        Long id = d.getPostId();
        return id != null && id == POST_ID;
    }

    public static boolean isFeedEndType(@Nullable String postType) {
        return postType != null && POST_TYPE.equalsIgnoreCase(postType.trim());
    }

    @NonNull
    public static ModelClass.Datum createDatum(@Nullable Context context) {
        ModelClass.Datum d = new ModelClass.Datum();
        d.setPostType(POST_TYPE);
        d.setPostId(POST_ID);
        d.setTitle(resolveMessage(context));
        d.setDescription("");
        return d;
    }

    @NonNull
    public static String resolveMessage(@Nullable Context context) {
        if (context != null) {
            try {
                if (AppHelper.isTeluguLanguageSelected()) {
                    return context.getString(R.string.feed_end_no_articles_te);
                }
                return context.getString(R.string.feed_end_no_articles);
            } catch (Exception ignored) {
            }
        }
        return "No articles to read further";
    }

    @NonNull
    public static String resolveHint(@Nullable Context context) {
        if (context != null) {
            try {
                if (AppHelper.isTeluguLanguageSelected()) {
                    return context.getString(R.string.feed_end_hint_te);
                }
                return context.getString(R.string.feed_end_hint);
            } catch (Exception ignored) {
            }
        }
        return "You've reached the end";
    }

    /** Remove every feed_end sentinel from {@code items}. */
    public static boolean stripAll(@Nullable List<ModelClass.Datum> items) {
        if (items == null || items.isEmpty()) return false;
        boolean changed = false;
        for (int i = items.size() - 1; i >= 0; i--) {
            if (isFeedEnd(items.get(i))) {
                items.remove(i);
                changed = true;
            }
        }
        return changed;
    }

    /**
     * Ensure exactly one feed_end datum is the last list item.
     *
     * @return true if the list was mutated
     */
    public static boolean ensureAtEnd(@Nullable List<ModelClass.Datum> items,
            @Nullable Context context) {
        if (items == null) return false;
        boolean changed = stripAll(items);
        items.add(createDatum(context));
        return true;
    }

    /**
     * True when the last item is already a feed_end sentinel.
     */
    public static boolean hasAtEnd(@Nullable List<ModelClass.Datum> items) {
        if (items == null || items.isEmpty()) return false;
        return isFeedEnd(items.get(items.size() - 1));
    }

    /**
     * Copy list without end sentinels (for capture / rebalance inputs).
     */
    @NonNull
    public static ArrayList<ModelClass.Datum> withoutEnd(
            @Nullable List<ModelClass.Datum> items) {
        ArrayList<ModelClass.Datum> out = new ArrayList<>();
        if (items == null) return out;
        for (ModelClass.Datum d : items) {
            if (!isFeedEnd(d)) {
                out.add(d);
            }
        }
        return out;
    }

    public static boolean messageLooksLikeEnd(@Nullable String message) {
        if (TextUtils.isEmpty(message)) return false;
        String m = message.trim();
        return "No articles to read further".equalsIgnoreCase(m)
                || m.contains("ఇక చదవడానికి");
    }
}
