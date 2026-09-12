package com.anyNews.anynews.Ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.ui.DefaultTimeBar;

/**
 * {@link DefaultTimeBar} draws its progress track vertically centered in the view.
 * Shift drawing so the track sits on the absolute bottom edge while keeping a taller
 * touch target for scrubbing — without clipping the red bar off-screen.
 */
@UnstableApi
public class BottomAlignedTimeBar extends DefaultTimeBar {

    private final int barHeightPx;

    public BottomAlignedTimeBar(Context context) {
        this(context, null);
    }

    public BottomAlignedTimeBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BottomAlignedTimeBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        int defaultBar = (int) (3f * context.getResources().getDisplayMetrics().density + 0.5f);
        int resolved = defaultBar;
        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs, androidx.media3.ui.R.styleable.DefaultTimeBar, defStyleAttr, 0);
            try {
                resolved = a.getDimensionPixelSize(
                        androidx.media3.ui.R.styleable.DefaultTimeBar_bar_height, defaultBar);
            } finally {
                a.recycle();
            }
        }
        barHeightPx = Math.max(1, resolved);
    }

    @Override
    public void onDraw(Canvas canvas) {
        int h = getHeight();
        if (h <= 0) {
            super.onDraw(canvas);
            return;
        }
        // DefaultTimeBar centers the track. Move it down so the track's bottom
        // coincides with this view's bottom, keeping strokes inside the clip.
        int shift = Math.max(0, (h - barHeightPx) / 2);
        if (shift == 0) {
            super.onDraw(canvas);
            return;
        }
        canvas.save();
        // Expand clip slightly upward so anti-aliased strokes are not cut.
        canvas.clipRect(0, 0, getWidth(), h);
        canvas.translate(0, shift);
        // Prevent DefaultTimeBar from painting past the bottom after translate.
        canvas.clipRect(0, 0, getWidth(), h - shift);
        super.onDraw(canvas);
        canvas.restore();
    }
}
