package com.anyNews.anynews.Ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Single-series sparkline for gold (24K ₹/10g base) or silver (₹/kg base) 7-day history.
 * Supports Y-axis labels, point values, touch tooltip, and {@link #setPriceScale} for
 * weight tabs. Skips non-positive points. Custom canvas chart (not MPAndroidChart).
 */
public class GoldHistoryChartView extends View {

    public static final String PRICE_KEY_GOLD = "gold_24k";
    public static final String PRICE_KEY_SILVER = "silver_kg";

    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint selectedDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tooltipBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tooltipTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint markerLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path linePath = new Path();
    private final Path fillPath = new Path();
    private final RectF tooltipRect = new RectF();

    /** Raw series from payload (gold ₹/10g or silver ₹/kg). */
    private float[] basePrices = new float[0];
    private float[] prices = new float[0];
    private String[] labels = new String[0];
    private float[] pointX = new float[0];
    private float[] pointY = new float[0];
    private int lineColor = 0xFFD4AF37;
    /** Multiplier applied to basePrices for weight tabs (default 1 = as stored). */
    private float priceScale = 1f;
    private int selectedIndex = -1;

    public GoldHistoryChartView(Context context) {
        super(context);
        init();
    }

    public GoldHistoryChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GoldHistoryChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        float d = getResources().getDisplayMetrics().density;
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(2.2f * d);
        linePaint.setStrokeJoin(Paint.Join.ROUND);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        fillPaint.setStyle(Paint.Style.FILL);
        dotPaint.setStyle(Paint.Style.FILL);
        selectedDotPaint.setStyle(Paint.Style.FILL);
        labelPaint.setColor(0xFF94A3B8);
        labelPaint.setTextSize(9f * d);
        labelPaint.setTextAlign(Paint.Align.CENTER);
        valuePaint.setTextSize(8f * d);
        valuePaint.setTextAlign(Paint.Align.CENTER);
        axisPaint.setColor(0xFF64748B);
        axisPaint.setTextSize(8.5f * d);
        axisPaint.setTextAlign(Paint.Align.RIGHT);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(1f);
        gridPaint.setColor(0x22FFFFFF);
        tooltipBgPaint.setStyle(Paint.Style.FILL);
        tooltipBgPaint.setColor(0xE6121824);
        tooltipTextPaint.setColor(0xFFFFFFFF);
        tooltipTextPaint.setTextSize(10f * d);
        tooltipTextPaint.setTextAlign(Paint.Align.CENTER);
        tooltipTextPaint.setFakeBoldText(true);
        markerLinePaint.setStyle(Paint.Style.STROKE);
        markerLinePaint.setStrokeWidth(1.2f * d);
        markerLinePaint.setColor(0x66FFFFFF);
        setClickable(true);
        applyColors();
    }

    public void setAccentColor(int color) {
        lineColor = color;
        applyColors();
        invalidate();
    }

    /**
     * Scale displayed prices for weight tabs.
     * Gold base is ₹/10g → use {@code grams / 10f}. Silver base is ₹/kg → {@code grams / 1000f}.
     */
    public void setPriceScale(float scale) {
        float s = scale > 0f && Float.isFinite(scale) ? scale : 1f;
        if (Math.abs(s - priceScale) < 1e-9f) return;
        priceScale = s;
        recomputeScaled();
        invalidate();
    }

    public float getPriceScale() {
        return priceScale;
    }

    private void applyColors() {
        linePaint.setColor(lineColor);
        dotPaint.setColor(lineColor);
        selectedDotPaint.setColor(lineColor);
        int a = (lineColor >> 24) & 0xFF;
        if (a == 0) a = 0xFF;
        fillPaint.setColor((lineColor & 0x00FFFFFF) | 0x28000000);
        valuePaint.setColor(lineColor);
    }

    /**
     * @param history JSONArray of { date_label, gold_24k?, silver_kg? }
     * @param priceKey {@link #PRICE_KEY_GOLD} or {@link #PRICE_KEY_SILVER}
     */
    public void setHistory(@Nullable JSONArray history, String priceKey) {
        selectedIndex = -1;
        if (history == null || history.length() == 0) {
            basePrices = new float[0];
            prices = new float[0];
            labels = new String[0];
            pointX = new float[0];
            pointY = new float[0];
            invalidate();
            return;
        }
        String key = priceKey != null ? priceKey : PRICE_KEY_GOLD;
        int n = history.length();
        float[] p = new float[n];
        String[] l = new String[n];
        int count = 0;
        // Prefer last ≤14 rows then trim to 7 positive points.
        int start = Math.max(0, n - 14);
        for (int i = start; i < n; i++) {
            JSONObject row = history.optJSONObject(i);
            if (row == null) continue;
            float v = (float) row.optDouble(key, 0);
            if (!(v > 0)) continue;
            p[count] = v;
            String raw = row.optString("date_label", "");
            if (raw.isEmpty()) raw = row.optString("date", "");
            l[count] = shortenLabel(raw);
            count++;
        }
        if (count > 7) {
            float[] p2 = new float[7];
            String[] l2 = new String[7];
            System.arraycopy(p, count - 7, p2, 0, 7);
            System.arraycopy(l, count - 7, l2, 0, 7);
            basePrices = p2;
            labels = l2;
        } else {
            basePrices = new float[count];
            labels = new String[count];
            System.arraycopy(p, 0, basePrices, 0, count);
            System.arraycopy(l, 0, labels, 0, count);
        }
        recomputeScaled();
        // Default focus: latest day so a price is visible before first tap.
        selectedIndex = prices.length >= 2 ? prices.length - 1 : -1;
        invalidate();
    }

    private void recomputeScaled() {
        int n = basePrices.length;
        prices = new float[n];
        for (int i = 0; i < n; i++) {
            prices[i] = basePrices[i] * priceScale;
        }
        pointX = new float[n];
        pointY = new float[n];
        if (selectedIndex >= n) selectedIndex = n > 0 ? n - 1 : -1;
    }

    public boolean hasEnoughPoints() {
        return prices.length >= 2;
    }

    private static String shortenLabel(String raw) {
        if (raw == null) return "";
        // "Aug 22" → "22 Aug"; "2026-08-22" → "22"
        String[] parts = raw.trim().split("\\s+");
        if (parts.length == 2) return parts[1] + " " + parts[0];
        if (raw.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return String.valueOf(Integer.parseInt(raw.substring(8, 10)));
        }
        return raw;
    }

    private static String formatPrice(float v) {
        try {
            return "₹" + NumberFormat.getNumberInstance(new Locale("en", "IN"))
                    .format(Math.round(v));
        } catch (Exception e) {
            return "₹" + Math.round(v);
        }
    }

    private static String formatAxis(float v) {
        float abs = Math.abs(v);
        if (abs >= 100_000f) {
            return "₹" + String.format(Locale.US, "%.1fL", v / 100_000f);
        }
        if (abs >= 10_000f) {
            return "₹" + String.format(Locale.US, "%.1fk", v / 1000f);
        }
        try {
            return "₹" + NumberFormat.getNumberInstance(new Locale("en", "IN"))
                    .format(Math.round(v));
        } catch (Exception e) {
            return "₹" + Math.round(v);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int n = prices.length;
        if (n < 2 || pointX.length != n) return super.onTouchEvent(event);
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN
                || action == MotionEvent.ACTION_MOVE
                || action == MotionEvent.ACTION_UP) {
            float x = event.getX();
            int best = 0;
            float bestDist = Math.abs(x - pointX[0]);
            for (int i = 1; i < n; i++) {
                float dist = Math.abs(x - pointX[i]);
                if (dist < bestDist) {
                    bestDist = dist;
                    best = i;
                }
            }
            float dens = getResources().getDisplayMetrics().density;
            if (bestDist <= 28f * dens || action != MotionEvent.ACTION_DOWN) {
                if (selectedIndex != best) {
                    selectedIndex = best;
                    invalidate();
                }
            }
            if (action == MotionEvent.ACTION_DOWN) {
                getParent().requestDisallowInterceptTouchEvent(true);
            }
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int n = prices.length;
        if (n < 2) return;

        float w = getWidth();
        float h = getHeight();
        float dens = getResources().getDisplayMetrics().density;
        float padL = 42f * dens;
        float padR = 10f * dens;
        float padT = 22f * dens;
        float padB = 26f * dens;

        float min = Float.MAX_VALUE;
        float max = Float.MIN_VALUE;
        for (float price : prices) {
            min = Math.min(min, price);
            max = Math.max(max, price);
        }
        float pad = Math.max(1f, (max - min) * 0.12f);
        min -= pad;
        max += pad;
        float range = Math.max(1f, max - min);

        // Y-axis grid + labels (3 ticks).
        for (int t = 0; t < 3; t++) {
            float frac = t / 2f;
            float val = max - frac * range;
            float y = padT + frac * (h - padT - padB);
            canvas.drawLine(padL, y, w - padR, y, gridPaint);
            canvas.drawText(formatAxis(val), padL - 4f * dens, y + 3f * dens, axisPaint);
        }

        linePath.reset();
        fillPath.reset();
        float baseY = h - padB;
        for (int i = 0; i < n; i++) {
            float x = padL + (i / (float) (n - 1)) * (w - padL - padR);
            float y = padT + ((max - prices[i]) / range) * (h - padT - padB);
            pointX[i] = x;
            pointY[i] = y;
            if (i == 0) {
                linePath.moveTo(x, y);
                fillPath.moveTo(x, baseY);
                fillPath.lineTo(x, y);
            } else {
                linePath.lineTo(x, y);
                fillPath.lineTo(x, y);
            }
        }
        float lastX = pointX[n - 1];
        fillPath.lineTo(lastX, baseY);
        fillPath.close();
        canvas.drawPath(fillPath, fillPaint);
        canvas.drawPath(linePath, linePaint);

        int focus = selectedIndex >= 0 ? selectedIndex : n - 1;

        for (int i = 0; i < n; i++) {
            float x = pointX[i];
            float y = pointY[i];
            boolean sel = i == focus && selectedIndex >= 0;
            canvas.drawCircle(x, y, (sel ? 4.2f : 2.8f) * dens, sel ? selectedDotPaint : dotPaint);
            canvas.drawText(labels[i] != null ? labels[i] : "", x, h - 5f * dens, labelPaint);

            // Point price labels (compact when large); selected day uses tooltip.
            if (selectedIndex < 0 || i != selectedIndex) {
                float textY = y - 6f * dens;
                if (textY < 10f * dens) textY = y + 14f * dens;
                String pointLabel = prices[i] >= 10_000f
                        ? formatAxis(prices[i])
                        : formatPrice(prices[i]);
                canvas.drawText(pointLabel, x, textY, valuePaint);
            }
        }

        if (selectedIndex >= 0 && selectedIndex < n) {
            float x = pointX[selectedIndex];
            float y = pointY[selectedIndex];
            canvas.drawLine(x, padT, x, baseY, markerLinePaint);
            canvas.drawCircle(x, y, 5f * dens, selectedDotPaint);

            String tip = (labels[selectedIndex] != null ? labels[selectedIndex] + " · " : "")
                    + formatPrice(prices[selectedIndex]);
            float tipW = tooltipTextPaint.measureText(tip) + 16f * dens;
            float tipH = 20f * dens;
            float tipX = Math.max(padL, Math.min(x - tipW / 2f, w - padR - tipW));
            float tipY = Math.max(2f * dens, y - tipH - 10f * dens);
            tooltipRect.set(tipX, tipY, tipX + tipW, tipY + tipH);
            canvas.drawRoundRect(tooltipRect, 6f * dens, 6f * dens, tooltipBgPaint);
            canvas.drawText(tip, tipX + tipW / 2f, tipY + tipH * 0.7f, tooltipTextPaint);
        }
    }
}
