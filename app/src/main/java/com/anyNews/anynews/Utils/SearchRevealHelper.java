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
 * from a search icon (engagement row or News|Read toggle).
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

    /** Run enter circular reveal on the activity root after layout. */
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
        final int cx = intent.getIntExtra(EXTRA_REVEAL_CX, root.getWidth() / 2);
        final int cy = intent.getIntExtra(EXTRA_REVEAL_CY, 0);
        root.getViewTreeObserver()
                .addOnGlobalLayoutListener(
                        new ViewTreeObserver.OnGlobalLayoutListener() {
                            @Override
                            public void onGlobalLayout() {
                                root.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                int w = root.getWidth();
                                int h = root.getHeight();
                                float finalRadius =
                                        (float) Math.hypot(Math.max(cx, w - cx), Math.max(cy, h - cy));
                                try {
                                    Animator anim =
                                            ViewAnimationUtils.createCircularReveal(
                                                    root, cx, cy, 0f, finalRadius);
                                    anim.setDuration(420);
                                    root.setVisibility(View.VISIBLE);
                                    anim.start();
                                } catch (Exception e) {
                                    root.setVisibility(View.VISIBLE);
                                }
                            }
                        });
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
        float startRadius =
                (float)
                        Math.hypot(
                                Math.max(cx, root.getWidth() - cx),
                                Math.max(cy, root.getHeight() - cy));
        try {
            Animator anim =
                    ViewAnimationUtils.createCircularReveal(root, cx, cy, startRadius, 0f);
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
