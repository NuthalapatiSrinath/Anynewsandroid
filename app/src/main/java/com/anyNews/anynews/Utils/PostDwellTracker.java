package com.anyNews.anynews.Utils;

import android.content.Context;

import androidx.annotation.Nullable;

/**
 * Per-post dwell timer. Start when an article is shown; enqueue seconds when
 * leaving (flip away / pause / background) via {@link ViewsBatchTracker}.
 *
 * Critical paths ({@code critical=true}) also flush the batch immediately so
 * admin analytics stay current on background / screen-off / process death.
 */
public final class PostDwellTracker {

    private static final PostDwellTracker INSTANCE = new PostDwellTracker();

    private long postId = 0L;
    private long startedAtMs = 0L;
    @Nullable private String postType;

    private PostDwellTracker() {}

    public static PostDwellTracker get() {
        return INSTANCE;
    }

    public synchronized void onPostShown(Context context, long newPostId, @Nullable String type) {
        // Skip non-article surfaces first so ad/web landings still flush prior dwell.
        if (type != null) {
            String t = type.toLowerCase();
            if (t.equals("ad") || t.equals("share_app") || t.equals("ratings")
                    || t.equals("web_ads") || t.equals("web")) {
                flush(context, false);
                postId = 0L;
                startedAtMs = 0L;
                postType = null;
                return;
            }
        }
        if (newPostId <= 0) return;
        if (postId > 0 && postId != newPostId) {
            flush(context, false);
        }
        if (postId != newPostId || startedAtMs <= 0) {
            postId = newPostId;
            postType = type;
            startedAtMs = System.currentTimeMillis();
        }
    }

    public synchronized void onPaused(Context context) {
        // Enqueue only — activity pauses mid-app should not force a network flush.
        // Background / screen-off / trim flush the batch via ViewsBatchTracker.
        flush(context, false);
        // Keep postId so resume can restart the same article timer
        startedAtMs = 0L;
    }

    /** Screen off / session close — flush dwell + batch immediately. */
    public synchronized void onScreenOff(Context context) {
        flush(context, true);
        startedAtMs = 0L;
    }

    public synchronized void onResumed(Context context) {
        // Only resume dwell while a session is active (foreground + screen on).
        if (SessionAnalyticsTracker.get().getActiveSessionId() == null) return;
        if (postId > 0 && startedAtMs <= 0) {
            startedAtMs = System.currentTimeMillis();
        }
    }

    public synchronized void flush(Context context) {
        flush(context, true);
    }

    public synchronized void flush(Context context, boolean critical) {
        if (postId <= 0 || startedAtMs <= 0) {
            if (critical) {
                ViewsBatchTracker.get().flush(context, true);
            }
            return;
        }
        long elapsedSec = Math.max(0L, (System.currentTimeMillis() - startedAtMs) / 1000L);
        long pid = postId;
        String type = postType;
        startedAtMs = 0L;
        // Minimum 1s if they actually saw it briefly (avoids noise of 0)
        if (elapsedSec < 1) elapsedSec = 1;
        enqueueDwell(context.getApplicationContext(), pid, elapsedSec, type);
        if (critical) {
            ViewsBatchTracker.get().flush(context.getApplicationContext(), true);
        }
    }

    private void enqueueDwell(
            Context context, long pid, long durationSeconds, @Nullable String type) {
        try {
            Context appCtx = context.getApplicationContext();
            int draftId = SharedPrefsData.getInt(appCtx, Constants.DraftID, Constants.PREF_NAME);
            if (draftId <= 0) return;
            String sessionId = SessionAnalyticsTracker.get().getActiveSessionId();
            ViewsBatchTracker.get().enqueueDwell(
                    appCtx, pid, durationSeconds, draftId, sessionId, type);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
