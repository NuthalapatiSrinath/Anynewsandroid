package com.anyNews.anynews.Utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;

/**
 * Circular reveal when opening {@link com.anyNews.anynews.Activities.InAppSearchActivity}
 * from a search icon, and when opening search results into HomePage FlipView.
 */
public final class SearchRevealHelper {

    public static final String EXTRA_REVEAL_CX = "search_reveal_cx";
    public static final String EXTRA_REVEAL_CY = "search_reveal_cy";
    public static final String EXTRA_HAS_REVEAL = "search_has_reveal";

    private SearchRevealHelper() {}

    /** Launch search with optional circular-reveal origin from {@code source}. */
    public static void openFrom(@NonNull Activity activity, @Nullable View source) {
        Intent intent =
                new Intent(activity, com.anyNews.anynews.Activities.InAppSearchActivity.class);
        if (source != null) {
            int[] loc = new int[2];
            source.getLocationOnScreen(loc);
            intent.putExtra(EXTRA_HAS_REVEAL, true);
            intent.putExtra(EXTRA_REVEAL_CX, loc[0] + source.getWidth() / 2);
            intent.putExtra(EXTRA_REVEAL_CY, loc[1] + source.getHeight() / 2);
        }
        try {
            ActivityOptionsCompat opts =
                    ActivityOptionsCompat.makeCustomAnimation(activity, 0, 0);
            ActivityCompat.startActivity(activity, intent, opts.toBundle());
        } catch (Exception e) {
            activity.startActivity(intent);
        }
    }

    /**
     * Run enter circular reveal on {@code root} after layout.
     * Safe for search-icon open and search-result open into HomePage FlipView.
     */
    public static void playEnterReveal(
            @NonNull final Activity activity,
            @NonNull final View root,
            @Nullable Intent intent) {
        if (intent == null || !intent.getBooleanExtra(EXTRA_HAS_REVEAL, false)) {
            root.setVisibility(View.VISIBLE);
            return;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            root.setVisibility(View.VISIBLE);
            return;
        }
        root.setVisibility(View.INVISIBLE);
        final int cxExtra = intent.getIntExtra(EXTRA_REVEAL_CX, -1);
        final int cyExtra = intent.getIntExtra(EXTRA_REVEAL_CY, -1);
        final Runnable start =
                () -> startCircularEnter(root, cxExtra, cyExtra);
        if (root.isLaidOut() && root.getWidth() > 0 && root.getHeight() > 0) {
            root.post(start);
            return;
        }
        root.getViewTreeObserver()
                .addOnGlobalLayoutListener(
                        new ViewTreeObserver.OnGlobalLayoutListener() {
                            @Override
                            public void onGlobalLayout() {
                                root.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                start.run();
                            }
                        });
    }

    private static void startCircularEnter(@NonNull View root, int cxExtra, int cyExtra) {
        int w = Math.max(1, root.getWidth());
        int h = Math.max(1, root.getHeight());
        int[] rootLoc = new int[2];
        root.getLocationOnScreen(rootLoc);
        int cx = cxExtra >= 0 ? Math.max(0, Math.min(w, cxExtra - rootLoc[0])) : w / 2;
        int cy = cyExtra >= 0 ? Math.max(0, Math.min(h, cyExtra - rootLoc[1])) : h / 4;
        float finalRadius =
                (float) Math.hypot(Math.max(cx, w - cx), Math.max(cy, h - cy));
        try {
            Animator anim =
                    ViewAnimationUtils.createCircularReveal(root, cx, cy, 0f, finalRadius);
            anim.setDuration(420);
            root.setVisibility(View.VISIBLE);
            anim.start();
        } catch (Exception e) {
            root.setVisibility(View.VISIBLE);
        }
    }

    /** Reverse circular reveal then finish. */
    public static void playExitAndFinish(
            @NonNull final Activity activity,
            @NonNull final View root,
            @Nullable Intent intent) {
        if (intent == null
                || !intent.getBooleanExtra(EXTRA_HAS_REVEAL, false)
                || Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            activity.finish();
            activity.overridePendingTransition(0, 0);
            return;
        }
        int cx = intent.getIntExtra(EXTRA_REVEAL_CX, root.getWidth() / 2);
        int cy = intent.getIntExtra(EXTRA_REVEAL_CY, 0);
        int[] rootLoc = new int[2];
        root.getLocationOnScreen(rootLoc);
        int localCx = Math.max(0, Math.min(root.getWidth(), cx - rootLoc[0]));
        int localCy = Math.max(0, Math.min(root.getHeight(), cy - rootLoc[1]));
        float startRadius =
                (float)
                        Math.hypot(
                                Math.max(localCx, root.getWidth() - localCx),
                                Math.max(localCy, root.getHeight() - localCy));
        try {
            Animator anim =
                    ViewAnimationUtils.createCircularReveal(
                            root, localCx, localCy, startRadius, 0f);
            anim.setDuration(320);
            anim.addListener(
                    new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            root.setVisibility(View.INVISIBLE);
                            activity.finish();
                            activity.overridePendingTransition(0, 0);
                        }
                    });
            anim.start();
        } catch (Exception e) {
            activity.finish();
            activity.overridePendingTransition(0, 0);
        }
    }
}
