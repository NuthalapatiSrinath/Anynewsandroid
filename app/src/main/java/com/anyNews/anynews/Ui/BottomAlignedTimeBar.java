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
 * That leaves a few transparent pixels of video visible below the red line.
 * This subclass shifts drawing so the track sits on the absolute bottom edge while
 * keeping a taller touch target for scrubbing.
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
        int defaultBar = (int) (2f * context.getResources().getDisplayMetrics().density + 0.5f);
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
        int shift = Math.max(0, (getHeight() - barHeightPx) / 2);
        if (shift == 0) {
            super.onDraw(canvas);
            return;
        }
        canvas.save();
        // Keep strokes inside the view; shift centers the track onto the bottom edge.
        canvas.clipRect(0, 0, getWidth(), getHeight());
        canvas.translate(0, shift);
        super.onDraw(canvas);
        canvas.restore();
    }
}
