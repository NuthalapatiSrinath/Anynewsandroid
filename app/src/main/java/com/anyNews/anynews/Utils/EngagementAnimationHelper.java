package com.anyNews.anynews.Utils;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import java.util.Locale;

/**
 * Drives the "Circular Action Bar" motion for the feed action row:
 *   - a continuous slow orange glow-ring rotation around the Like (idle presence)
 *   - a tap "pop" (scale overshoot) + ring flash + expanding shockwave burst
 *   - a rolling count-up on the number below the icon
 * Kept lightweight because the reload feed flips one post at a time.
 */
public final class EngagementAnimationHelper {

    private EngagementAnimationHelper() {
    }

    /** Slow, always-on orange halo spin so the Like reads as "alive" at rest. */
    public static void startLikeGlow(View glowRing) {
        if (glowRing == null) {
            return;
        }
        if (glowRing.getTag() instanceof ObjectAnimator) {
            return; // already spinning on this recycled view
        }
        glowRing.setAlpha(0.6f);
        ObjectAnimator spin = ObjectAnimator.ofFloat(glowRing, View.ROTATION, 0f, 360f);
        spin.setDuration(5200);
        spin.setRepeatCount(ValueAnimator.INFINITE);
        spin.setInterpolator(new LinearInterpolator());
        spin.start();
        glowRing.setTag(spin);
    }

    /** The full "like" celebration: icon pop, ring flash, and an expanding shockwave. */
    public static void playLikeBurst(final View icon, final View glowRing, final View shockwave) {
        if (icon != null) {
            icon.animate().cancel();
            icon.setScaleX(1f);
            icon.setScaleY(1f);
            ObjectAnimator sx = ObjectAnimator.ofFloat(icon, View.SCALE_X, 1f, 0.8f, 1.25f, 1f);
            ObjectAnimator sy = ObjectAnimator.ofFloat(icon, View.SCALE_Y, 1f, 0.8f, 1.25f, 1f);
            AnimatorSet pop = new AnimatorSet();
            pop.playTogether(sx, sy);
            pop.setDuration(420);
            pop.setInterpolator(new OvershootInterpolator());
            pop.start();
        }

        if (glowRing != null) {
            float from = glowRing.getRotation();
            ObjectAnimator sweep = ObjectAnimator.ofFloat(glowRing, View.ROTATION, from, from + 260f);
            sweep.setDuration(520);
            sweep.setInterpolator(new DecelerateInterpolator());
            sweep.start();
            glowRing.animate().alpha(1f).setDuration(150)
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            glowRing.animate().alpha(0.6f).setDuration(420).start();
                        }
                    }).start();
        }

        if (shockwave != null) {
            shockwave.animate().cancel();
            shockwave.setVisibility(View.VISIBLE);
            shockwave.setAlpha(0.9f);
            shockwave.setScaleX(0.5f);
            shockwave.setScaleY(0.5f);
            shockwave.animate()
                    .scaleX(2.2f).scaleY(2.2f).alpha(0f)
                    .setDuration(520)
                    .setInterpolator(new DecelerateInterpolator())
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            shockwave.setVisibility(View.INVISIBLE);
                            shockwave.setScaleX(1f);
                            shockwave.setScaleY(1f);
                        }
                    }).start();
        }
    }

    /** Rolls the number from -> to with a small upward lift, matching the mockup's count-up. */
    public static void animateCount(final TextView tv, int from, int to) {
        if (tv == null) {
            return;
        }
        if (from == to) {
            tv.setText(formatCount(to));
            return;
        }
        tv.setTranslationY(7f);
        tv.animate().translationY(0f).setDuration(320).setInterpolator(new DecelerateInterpolator()).start();
        ValueAnimator va = ValueAnimator.ofInt(from, to);
        va.setDuration(600);
        va.setInterpolator(new DecelerateInterpolator());
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                tv.setText(formatCount((int) animation.getAnimatedValue()));
            }
        });
        va.start();
    }

    public static String formatCount(int c) {
        if (c >= 1000000) {
            double v = c / 1000000.0;
            return (v >= 10 || v == Math.floor(v))
                    ? String.format(Locale.US, "%.0fM", v)
                    : String.format(Locale.US, "%.1fM", v);
        }
        if (c >= 1000) {
            double v = c / 1000.0;
            return (v >= 10 || v == Math.floor(v))
                    ? String.format(Locale.US, "%.0fK", v)
                    : String.format(Locale.US, "%.1fK", v);
        }
        return String.valueOf(c);
    }
}
