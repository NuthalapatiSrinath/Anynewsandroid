package com.anyNews.anynews.Utils;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.Nullable;

/**
 * Subtle infinite alpha pulse for cricket LIVE pills (mini player + Match Center).
 * Call {@link #stop} when the match is no longer live or the badge is detached.
 */
public final class LiveBadgeBlinkHelper {

    private static final long DURATION_MS = 780;
    /** Floor alpha — soft pulse, not a harsh flash. */
    private static final float MIN_ALPHA = 0.42f;

    private LiveBadgeBlinkHelper() {
    }

    /**
     * Show the badge and start (or restart) a whole-pill opacity blink when
     * {@code live} is true; stop and hide when false.
     */
    @Nullable
    public static ObjectAnimator sync(@Nullable View badge, boolean live,
                                      @Nullable ObjectAnimator existing) {
        if (badge == null) {
            return stop(null, existing);
        }
        if (live) {
            badge.setVisibility(View.VISIBLE);
            return start(badge, existing);
        }
        ObjectAnimator cleared = stop(badge, existing);
        badge.setVisibility(View.GONE);
        return cleared;
    }

    /** Start an infinite reverse alpha pulse on the whole badge. */
    @Nullable
    public static ObjectAnimator start(@Nullable View badge,
                                       @Nullable ObjectAnimator existing) {
        if (badge == null) {
            return stop(null, existing);
        }
        stop(badge, existing);
        badge.setAlpha(1f);
        ObjectAnimator anim = ObjectAnimator.ofFloat(badge, View.ALPHA, 1f, MIN_ALPHA);
        anim.setDuration(DURATION_MS);
        anim.setRepeatCount(ValueAnimator.INFINITE);
        anim.setRepeatMode(ValueAnimator.REVERSE);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        anim.start();
        return anim;
    }

    /** Cancel any running pulse and restore full opacity. */
    @Nullable
    public static ObjectAnimator stop(@Nullable View badge,
                                      @Nullable ObjectAnimator existing) {
        if (existing != null) {
            existing.cancel();
        }
        if (badge != null) {
            badge.clearAnimation();
            badge.setAlpha(1f);
        }
        return null;
    }
}
