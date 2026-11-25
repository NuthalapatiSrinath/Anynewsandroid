package com.anyNews.anynews.Utils;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * Post impressions for dev / debug builds when {@link com.anyNews.anynews.BuildConfig#IMPRESSIONS_SERVER_BATCH_ENABLED}
 * is false. Use Firebase Console (DebugView with debug APK) — no server {@code POST /views/batch}.
 *
 * Server cron / BigQuery aggregation is intentionally not implemented (cost). Optional future:
 * Cloudflare Worker bulk ingest from GA4 export.
 */
public final class PostViewAnalytics {

    public static final String EVENT_POST_VIEW = "post_view";

    private static final String[] SKIPPED_TYPES = {
            "ad", "share_app", "ratings", "web_ads", "web", FeedEndHelper.POST_TYPE,
    };

    private PostViewAnalytics() {}

    public static boolean isArticleType(@Nullable String postType) {
        if (postType == null || postType.isEmpty()) return true;
        String t = postType.toLowerCase();
        for (String skip : SKIPPED_TYPES) {
            if (t.equals(skip)) return false;
        }
        return true;
    }

    /** Log one settled flip impression (local SDK only — no AnyNews API). */
    public static void logPostView(
            Context context,
            long postId,
            int draftId,
            @Nullable String postType,
            int position) {
        if (context == null || postId <= 0 || draftId <= 0) return;
        if (!isArticleType(postType)) return;
        try {
            Context appCtx = context.getApplicationContext();
            FirebaseAnalytics analytics = FirebaseAnalytics.getInstance(appCtx);
            Bundle bundle = new Bundle();
            bundle.putLong(FirebaseAnalytics.Param.ITEM_ID, postId);
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, postType != null ? postType : "article");
            bundle.putInt("draft_id", draftId);
            bundle.putInt("feed_position", position);
            bundle.putString("source", "flip_settle");
            analytics.logEvent(EVENT_POST_VIEW, bundle);
        } catch (Exception ignored) {
        }
    }
}