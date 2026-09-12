package com.anyNews.anynews.Ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Full-bleed host for the feed Exo {@code PlayerView} (+ lifted engagement).
 * Touches outside positioned children pass through to FlipView.
 */
public class FeedExoOverlayFrameLayout extends FrameLayout {

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

    /** @deprecated touch targets are derived from visible children */
    public void setTouchTarget(@Nullable View target) {
        // no-op — hit-test uses children
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getVisibility() != VISIBLE || getChildCount() == 0) {
            return false;
        }
        final float x = ev.getRawX();
        final float y = ev.getRawY();
        final int[] loc = new int[2];
        for (int i = getChildCount() - 1; i >= 0; i--) {
            View child = getChildAt(i);
            if (child.getVisibility() != VISIBLE
                    || child.getWidth() <= 0
                    || child.getHeight() <= 0) {
                continue;
            }
            child.getLocationOnScreen(loc);
            if (x >= loc[0]
                    && y >= loc[1]
                    && x < loc[0] + child.getWidth()
                    && y < loc[1] + child.getHeight()) {
                return super.dispatchTouchEvent(ev);
            }
        }
        return false;
    }
}
