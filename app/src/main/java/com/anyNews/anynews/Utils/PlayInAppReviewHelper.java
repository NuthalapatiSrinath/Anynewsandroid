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
 * Google Play In-App Review. Google does not allow pre-selecting stars via
 * {@code market://} or Play Store listing URLs — ReviewManager is the supported
 * flow. The dialog (and whether it appears) is controlled by Play. Callers should
 * mark the in-feed prompt submitted ({@code APP_RATING_SUBMITTED}) when the user
 * taps the review CTA so the card does not nag again — do not collect stars in-app.
 */
public final class PlayInAppReviewHelper {

    private static final String TAG = "PlayInAppReview";

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

    /** Open the store listing only — cannot pre-select a star rating (Google restriction). */
    public static boolean openPlayStoreListing(@NonNull Activity activity) {
        final String appPackageName = activity.getPackageName();
        try {
            activity.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + appPackageName)));
            return true;
        } catch (ActivityNotFoundException anfe) {
            try {
                activity.startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id="
                                + appPackageName)));
                return true;
            } catch (Exception e) {
                Log.w(TAG, "openPlayStoreListing failed", e);
                return false;
            }
        }
    }
}
