package com.anyNews.anynews.Ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * Compact sparkline from real price points (no invented values).
 * Hidden when fewer than 2 points.
 */
public class StockSparklineView extends View {

    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path linePath = new Path();
    private final Path fillPath = new Path();
    private float[] points = new float[0];
    private int lineColor = 0xFF4ADE80;

    public StockSparklineView(Context context) {
        super(context);
        init();
    }

    public StockSparklineView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public StockSparklineView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        float d = getResources().getDisplayMetrics().density;
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(1.8f * d);
        linePaint.setStrokeJoin(Paint.Join.ROUND);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        fillPaint.setStyle(Paint.Style.FILL);
        setWillNotDraw(false);
        // Avoid zero-size measure when parent gives AT_MOST with no intrinsic size.
        setMinimumWidth((int) (48 * d));
        setMinimumHeight((int) (24 * d));
    }

    /** Set closes; color green if last ≥ first, else red. */
    public void setPoints(@Nullable float[] values) {
        if (values == null || values.length < 2) {
            points = new float[0];
            setVisibility(GONE);
            invalidate();
            return;
        }
        points = values;
        boolean up = values[values.length - 1] >= values[0];
        lineColor = up ? 0xFF4ADE80 : 0xFFF87171;
        linePaint.setColor(lineColor);
        fillPaint.setColor((lineColor & 0x00FFFFFF) | 0x33000000);
        setVisibility(VISIBLE);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (points.length < 2) return;
        float w = getWidth() - getPaddingLeft() - getPaddingRight();
        float h = getHeight() - getPaddingTop() - getPaddingBottom();
        if (w <= 0 || h <= 0) return;
        float min = points[0];
        float max = points[0];
        for (float p : points) {
            if (p < min) min = p;
            if (p > max) max = p;
        }
        float range = max - min;
        if (range < 1e-6f) range = 1f;
        float left = getPaddingLeft();
        float top = getPaddingTop();
        float step = w / (points.length - 1);
        linePath.reset();
        fillPath.reset();
        for (int i = 0; i < points.length; i++) {
            float x = left + step * i;
            float y = top + h - ((points[i] - min) / range) * h;
            if (i == 0) {
                linePath.moveTo(x, y);
                fillPath.moveTo(x, top + h);
                fillPath.lineTo(x, y);
            } else {
                linePath.lineTo(x, y);
                fillPath.lineTo(x, y);
            }
        }
        fillPath.lineTo(left + w, top + h);
        fillPath.close();
        canvas.drawPath(fillPath, fillPaint);
        canvas.drawPath(linePath, linePaint);
    }
}
