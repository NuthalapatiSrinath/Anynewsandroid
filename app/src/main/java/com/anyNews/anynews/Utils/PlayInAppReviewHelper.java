package com.anyNews.anynews.Utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Task;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;

/**
 * Google Play In-App Review / store listing helper.
 * The explicit "Rate on Google Play" CTA must open only the ANYNEWS Play listing
 * ({@link #PLAY_STORE_URL}) — never a market chooser for other stores when avoidable.
 */
public final class PlayInAppReviewHelper {

    private static final String TAG = "PlayInAppReview";
    public static final String PLAY_PACKAGE = "com.anyNews.anynews";
    /** Exact listing URL required by product. */
    public static final String PLAY_STORE_URL =
            "https://play.google.com/store/apps/details?id=com.anyNews.anynews&hl=en_IN";
    private static final String PLAY_MARKET_URI =
            "market://details?id=com.anyNews.anynews&hl=en_IN";

    public interface Listener {
        /** Review UI finished (or Play dismissed / quota). */
        void onReviewFlowFinished(boolean launched);

        /** In-app review unavailable — listing fallback may have been opened. */
        void onReviewUnavailable(@Nullable Exception error, boolean openedPlayStore);
    }

    private PlayInAppReviewHelper() {
    }

    /**
     * Request Google's in-app review UI. Falls back to the Play Store listing
     * when ReviewManager cannot start (sideload / no Play / API failure).
     */
    public static void launch(
            @NonNull Activity activity,
            @Nullable Listener listener) {
        try {
            ReviewManager manager = ReviewManagerFactory.create(activity);
            Task<ReviewInfo> request = manager.requestReviewFlow();
            request.addOnCompleteListener(task -> {
                if (!task.isSuccessful() || task.getResult() == null) {
                    Exception err = task.getException();
                    Log.w(TAG, "requestReviewFlow failed", err);
                    boolean opened = openPlayStoreListing(activity);
                    if (listener != null) {
                        listener.onReviewUnavailable(err, opened);
                    }
                    return;
                }
                Task<Void> flow = manager.launchReviewFlow(activity, task.getResult());
                flow.addOnCompleteListener(launchTask -> {
                    if (listener != null) {
                        listener.onReviewFlowFinished(launchTask.isSuccessful());
                    }
                });
            });
        } catch (Exception e) {
            Log.w(TAG, "launch review crashed", e);
            boolean opened = openPlayStoreListing(activity);
            if (listener != null) {
                listener.onReviewUnavailable(e, opened);
            }
        }
    }

    /** Open the Play Store listing only — pin to Play Store package when possible. */
    public static boolean openPlayStoreListing(@NonNull Activity activity) {
        // Prefer https with explicit Play Store package — avoids OEM market choosers.
        try {
            Intent https = new Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_STORE_URL));
            https.setPackage("com.android.vending");
            https.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(https);
            return true;
        } catch (Exception ignored) {
        }
        try {
            Intent market = new Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_MARKET_URI));
            market.setPackage("com.android.vending");
            market.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(market);
            return true;
        } catch (ActivityNotFoundException anfe) {
            try {
                Intent fallback = new Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_STORE_URL));
                fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(fallback);
                return true;
            } catch (Exception e) {
                Log.w(TAG, "openPlayStoreListing failed", e);
                return false;
            }
        } catch (Exception e) {
            Log.w(TAG, "openPlayStoreListing failed", e);
            return false;
        }
    }

    /** Same as {@link #openPlayStoreListing(Activity)} for non-Activity contexts. */
    public static boolean openPlayStoreListing(@NonNull android.content.Context context) {
        if (context instanceof Activity) {
            return openPlayStoreListing((Activity) context);
        }
        try {
            Intent https = new Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_STORE_URL));
            https.setPackage("com.android.vending");
            https.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(https);
            return true;
        } catch (Exception ignored) {
        }
        try {
            Intent fallback = new Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_STORE_URL));
            fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(fallback);
            return true;
        } catch (Exception e) {
            Log.w(TAG, "openPlayStoreListing(context) failed", e);
            return false;
        }
    }
}
