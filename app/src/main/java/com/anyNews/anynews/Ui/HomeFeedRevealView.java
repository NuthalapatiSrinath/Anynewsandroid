package com.anyNews.anynews.Ui;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.anyNews.anynews.R;

/**
 * Home feed first-paint loader: article-shaped skeleton (top media bone +
 * body text letter-bars). L→R shimmer is clipped to the bones region only —
 * never a full-screen white column. Theme-aware soft bones for light/dark.
 */
public class HomeFeedRevealView extends FrameLayout {

    private static final long SHIMMER_MS = 1200L;
    private static final long OUT_MS = 180L;
    /** Soft beam width as fraction of bones clip width. */
    private static final float BAND_FRACTION = 0.32f;

    private View wipeBand;
    private View bonesClip;
    private ValueAnimator shimmerAnimator;
    private boolean running;
    private boolean attached;

    public HomeFeedRevealView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public HomeFeedRevealView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public HomeFeedRevealView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.layout_home_feed_reveal, this, true);
        wipeBand = findViewById(R.id.feed_reveal_wipe_band);
        bonesClip = findViewById(R.id.feed_reveal_text_clip);
        setClickable(true);
        setFocusable(true);
        setVisibility(GONE);
        setClipChildren(true);
        setClipToPadding(true);
        if (bonesClip instanceof ViewGroup) {
            ((ViewGroup) bonesClip).setClipChildren(true);
            ((ViewGroup) bonesClip).setClipToPadding(true);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        attached = true;
        if (running) {
            startShimmerInternal();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        attached = false;
        cancelInternal();
        super.onDetachedFromWindow();
    }

    /** Start looping L→R shimmer across media + text bones until {@link #stop(boolean, Runnable)}. */
    public void start() {
        running = true;
        animate().cancel();
        setAlpha(1f);
        setTranslationX(0f);
        setVisibility(VISIBLE);
        if (bonesClip != null && bonesClip.getWidth() > 0) {
            startShimmerInternal();
        } else {
            post(this::startShimmerInternal);
        }
    }

    /**
     * @param instant true for disk-cache paint (no fade that can flash)
     * @param end      run after hidden (may be null)
     */
    public void stop(boolean instant, @Nullable Runnable end) {
        running = false;
        cancelInternal();
        if (instant || getVisibility() != VISIBLE) {
            setVisibility(GONE);
            setAlpha(1f);
            setTranslationX(0f);
            if (wipeBand != null) {
                wipeBand.setTranslationX(0f);
            }
            if (end != null) {
                end.run();
            }
            return;
        }
        animate()
                .alpha(0f)
                .setDuration(OUT_MS)
                .withEndAction(() -> {
                    setVisibility(GONE);
                    setAlpha(1f);
                    setTranslationX(0f);
                    if (wipeBand != null) {
                        wipeBand.setTranslationX(0f);
                    }
                    if (end != null) {
                        end.run();
                    }
                })
                .start();
    }

    public boolean isRunning() {
        return running && getVisibility() == VISIBLE;
    }

    private void startShimmerInternal() {
        if (!running || !attached) {
            return;
        }
        cancelInternal();
        if (wipeBand == null) {
            return;
        }

        int clipW = 0;
        int clipH = 0;
        if (bonesClip != null) {
            if (bonesClip.getWidth() > 0) {
                clipW = bonesClip.getWidth();
            }
            if (bonesClip.getHeight() > 0) {
                clipH = bonesClip.getHeight();
            }
        }
        if (clipW <= 0) {
            clipW = Math.max(getWidth(), getResources().getDisplayMetrics().widthPixels);
        }
        if (clipH <= 0) {
            clipH = Math.max(getHeight(), 0);
        }

        // Soft beam across bones; clipped so it never becomes a full-viewport white slab.
        int desiredBand = Math.max((int) (clipW * BAND_FRACTION), (int) dp(64));
        desiredBand = Math.min(desiredBand, (int) dp(120));
        ViewGroup.LayoutParams lp = wipeBand.getLayoutParams();
        if (lp != null) {
            boolean changed = false;
            if (lp.width != desiredBand) {
                lp.width = desiredBand;
                changed = true;
            }
            if (clipH > 0 && lp.height != clipH) {
                lp.height = clipH;
                changed = true;
            } else if (clipH <= 0 && lp.height != ViewGroup.LayoutParams.MATCH_PARENT) {
                lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
                changed = true;
            }
            if (changed) {
                wipeBand.setLayoutParams(lp);
            }
        }

        final float bandW = desiredBand;
        final float startX = -bandW;
        final float endX = clipW + bandW;

        wipeBand.setVisibility(VISIBLE);
        wipeBand.setTranslationX(startX);
        wipeBand.setAlpha(1f);

        ObjectAnimator sweep = ObjectAnimator.ofFloat(wipeBand, View.TRANSLATION_X, startX, endX);
        sweep.setDuration(SHIMMER_MS);
        sweep.setRepeatCount(ValueAnimator.INFINITE);
        sweep.setRepeatMode(ValueAnimator.RESTART);
        sweep.setInterpolator(new LinearInterpolator());
        shimmerAnimator = sweep;
        sweep.start();
    }

    private void cancelInternal() {
        if (shimmerAnimator != null) {
            try {
                shimmerAnimator.cancel();
            } catch (Exception ignored) {
            }
            shimmerAnimator = null;
        }
        if (wipeBand != null) {
            wipeBand.animate().cancel();
        }
    }

    private float dp(float v) {
        return v * getResources().getDisplayMetrics().density;
    }
}
