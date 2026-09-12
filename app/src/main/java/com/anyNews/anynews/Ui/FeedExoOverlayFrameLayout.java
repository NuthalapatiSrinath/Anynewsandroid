package com.anyNews.anynews.Ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Full-bleed host for the feed Exo {@code PlayerView}. Touches outside the
 * positioned player pass through to FlipView (likes / share / writer row).
 */
public class FeedExoOverlayFrameLayout extends FrameLayout {

    @Nullable
    private View touchTarget;

    public FeedExoOverlayFrameLayout(@NonNull Context context) {
        super(context);
    }

    public FeedExoOverlayFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FeedExoOverlayFrameLayout(
            @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setTouchTarget(@Nullable View target) {
        touchTarget = target;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getVisibility() != VISIBLE || touchTarget == null
                || touchTarget.getVisibility() != VISIBLE
                || touchTarget.getWidth() <= 0
                || touchTarget.getHeight() <= 0) {
            return false;
        }
        final float x = ev.getRawX();
        final float y = ev.getRawY();
        final int[] loc = new int[2];
        touchTarget.getLocationOnScreen(loc);
        final boolean inside = x >= loc[0]
                && y >= loc[1]
                && x < loc[0] + touchTarget.getWidth()
                && y < loc[1] + touchTarget.getHeight();
        if (!inside) {
            return false;
        }
        return super.dispatchTouchEvent(ev);
    }
}
