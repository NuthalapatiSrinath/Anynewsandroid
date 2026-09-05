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

import java.util.Locale;

/**
 * Dual-series sparkline for petrol (green) / diesel (blue) history.
 * Matches {@link GoldHistoryChartView} UX: Y-axis price labels, point values,
 * and tap tooltips. Skips non-positive price points.
 */
public class FuelHistoryChartView extends View {

    private final Paint petrolPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dieselPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint petrolDot = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dieselDot = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint petrolValuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dieselValuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tooltipBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tooltipTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint markerLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path petrolPath = new Path();
    private final Path dieselPath = new Path();
    private final RectF tooltipRect = new RectF();

    private float[] petrol = new float[0];
    private float[] diesel = new float[0];
    private String[] labels = new String[0];
    private float[] pointX = new float[0];
    private float[] petrolY = new float[0];
    private float[] dieselY = new float[0];
    private int selectedIndex = -1;

    public FuelHistoryChartView(Context context) {
        super(context);
        init();
    }

    public FuelHistoryChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FuelHistoryChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        float d = getResources().getDisplayMetrics().density;
        petrolPaint.setColor(0xFF34D399);
        petrolPaint.setStyle(Paint.Style.STROKE);
        petrolPaint.setStrokeWidth(2.2f * d);
        petrolPaint.setStrokeJoin(Paint.Join.ROUND);
        petrolPaint.setStrokeCap(Paint.Cap.ROUND);
        dieselPaint.setColor(0xFF60A5FA);
        dieselPaint.setStyle(Paint.Style.STROKE);
        dieselPaint.setStrokeWidth(2.2f * d);
        dieselPaint.setStrokeJoin(Paint.Join.ROUND);
        dieselPaint.setStrokeCap(Paint.Cap.ROUND);
        petrolDot.setColor(0xFF34D399);
        petrolDot.setStyle(Paint.Style.FILL);
        dieselDot.setColor(0xFF60A5FA);
        dieselDot.setStyle(Paint.Style.FILL);
        petrolValuePaint.setColor(0xFF34D399);
        petrolValuePaint.setTextSize(8f * d);
        petrolValuePaint.setTextAlign(Paint.Align.CENTER);
        dieselValuePaint.setColor(0xFF60A5FA);
        dieselValuePaint.setTextSize(8f * d);
        dieselValuePaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setColor(0xFF94A3B8);
        labelPaint.setTextSize(9f * d);
        labelPaint.setTextAlign(Paint.Align.CENTER);
        axisPaint.setColor(0xFF94A3B8);
        axisPaint.setTextSize(8.5f * d);
        axisPaint.setTextAlign(Paint.Align.RIGHT);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(1f);
        gridPaint.setColor(0x22FFFFFF);
        tooltipBgPaint.setStyle(Paint.Style.FILL);
        tooltipBgPaint.setColor(0xE6121824);
        tooltipTextPaint.setColor(0xFFFFFFFF);
        tooltipTextPaint.setTextSize(9.5f * d);
        tooltipTextPaint.setTextAlign(Paint.Align.CENTER);
        tooltipTextPaint.setFakeBoldText(true);
        markerLinePaint.setStyle(Paint.Style.STROKE);
        markerLinePaint.setStrokeWidth(1.2f * d);
        markerLinePaint.setColor(0x66FFFFFF);
        setClickable(true);
    }

    public void setHistory(@Nullable JSONArray history) {
        selectedIndex = -1;
        if (history == null || history.length() == 0) {
            petrol = new float[0];
            diesel = new float[0];
            labels = new String[0];
            pointX = new float[0];
            petrolY = new float[0];
            dieselY = new float[0];
            invalidate();
            return;
        }
        int n = history.length();
        float[] p = new float[n];
        float[] d = new float[n];
        String[] l = new String[n];
        int count = 0;
        for (int i = 0; i < n; i++) {
            JSONObject row = history.optJSONObject(i);
            if (row == null) continue;
            float pv = (float) row.optDouble("petrol", 0);
            float dv = (float) row.optDouble("diesel", 0);
            if (!(pv > 0) || !(dv > 0)) continue;
            p[count] = pv;
            d[count] = dv;
            String raw = row.optString("date_label", "");
            if (raw.isEmpty()) raw = row.optString("date", "");
            l[count] = shortenLabel(raw);
            count++;
        }
        petrol = new float[count];
        diesel = new float[count];
        labels = new String[count];
        pointX = new float[count];
        petrolY = new float[count];
        dieselY = new float[count];
        System.arraycopy(p, 0, petrol, 0, count);
        System.arraycopy(d, 0, diesel, 0, count);
        System.arraycopy(l, 0, labels, 0, count);
        // Default focus: latest day so prices are visible before first tap.
        selectedIndex = count >= 2 ? count - 1 : -1;
        invalidate();
    }

    public boolean hasEnoughPoints() {
        return petrol.length >= 2;
    }

    private static String shortenLabel(String raw) {
        if (raw == null) return "";
        String[] parts = raw.trim().split("\\s+");
        if (parts.length == 2) return parts[1] + " " + parts[0];
        if (raw.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return String.valueOf(Integer.parseInt(raw.substring(8, 10)));
        }
        return raw;
    }

    private static String formatPrice(float v) {
        return "₹" + String.format(Locale.US, "%.2f", v);
    }

    private static String formatAxis(float v) {
        // Compact left-axis labels for ~₹100 fuel prices.
        return "₹" + String.format(Locale.US, "%.1f", v);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int n = petrol.length;
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
        int n = petrol.length;
        if (n < 2) return;

        float w = getWidth();
        float h = getHeight();
        float dens = getResources().getDisplayMetrics().density;
        float padL = 44f * dens;
        float padR = 10f * dens;
        float padT = 22f * dens;
        float padB = 26f * dens;

        float min = Float.MAX_VALUE;
        float max = Float.MIN_VALUE;
        for (int i = 0; i < n; i++) {
            min = Math.min(min, Math.min(petrol[i], diesel[i]));
            max = Math.max(max, Math.max(petrol[i], diesel[i]));
        }
        float pad = Math.max(0.5f, (max - min) * 0.12f);
        min -= pad;
        max += pad;
        float range = Math.max(0.5f, max - min);

        // Y-axis grid + labels (3 ticks).
        for (int t = 0; t < 3; t++) {
            float frac = t / 2f;
            float val = max - frac * range;
            float y = padT + frac * (h - padT - padB);
            canvas.drawLine(padL, y, w - padR, y, gridPaint);
            canvas.drawText(formatAxis(val), padL - 4f * dens, y + 3f * dens, axisPaint);
        }

        petrolPath.reset();
        dieselPath.reset();
        float baseY = h - padB;
        for (int i = 0; i < n; i++) {
            float x = padL + (i / (float) (n - 1)) * (w - padL - padR);
            float yP = padT + ((max - petrol[i]) / range) * (h - padT - padB);
            float yD = padT + ((max - diesel[i]) / range) * (h - padT - padB);
            pointX[i] = x;
            petrolY[i] = yP;
            dieselY[i] = yD;
            if (i == 0) {
                petrolPath.moveTo(x, yP);
                dieselPath.moveTo(x, yD);
            } else {
                petrolPath.lineTo(x, yP);
                dieselPath.lineTo(x, yD);
            }
        }
        canvas.drawPath(petrolPath, petrolPaint);
        canvas.drawPath(dieselPath, dieselPaint);

        int focus = selectedIndex >= 0 ? selectedIndex : n - 1;

        for (int i = 0; i < n; i++) {
            float x = pointX[i];
            float yP = petrolY[i];
            float yD = dieselY[i];
            boolean sel = i == focus && selectedIndex >= 0;
            float r = (sel ? 4.2f : 2.8f) * dens;
            canvas.drawCircle(x, yP, r, petrolDot);
            canvas.drawCircle(x, yD, r, dieselDot);
            canvas.drawText(labels[i] != null ? labels[i] : "", x, h - 5f * dens, labelPaint);

            // Point price labels; selected day uses the dual-series tooltip.
            if (selectedIndex < 0 || i != selectedIndex) {
                float petrolTextY = yP - 6f * dens;
                if (petrolTextY < 10f * dens) petrolTextY = yP + 14f * dens;
                canvas.drawText(formatPrice(petrol[i]), x, petrolTextY, petrolValuePaint);

                float dieselTextY = yD + 12f * dens;
                if (dieselTextY > baseY - 2f * dens) dieselTextY = yD - 6f * dens;
                canvas.drawText(formatPrice(diesel[i]), x, dieselTextY, dieselValuePaint);
            }
        }

        if (selectedIndex >= 0 && selectedIndex < n) {
            float x = pointX[selectedIndex];
            float yP = petrolY[selectedIndex];
            float yD = dieselY[selectedIndex];
            canvas.drawLine(x, padT, x, baseY, markerLinePaint);
            canvas.drawCircle(x, yP, 5f * dens, petrolDot);
            canvas.drawCircle(x, yD, 5f * dens, dieselDot);

            String day = labels[selectedIndex] != null ? labels[selectedIndex] : "";
            String line1 = (day.isEmpty() ? "" : (day + " · "))
                    + "P " + formatPrice(petrol[selectedIndex]);
            String line2 = "D " + formatPrice(diesel[selectedIndex]);
            float tipW = Math.max(tooltipTextPaint.measureText(line1),
                    tooltipTextPaint.measureText(line2)) + 16f * dens;
            float tipH = 34f * dens;
            float midY = (yP + yD) / 2f;
            float tipX = Math.max(padL, Math.min(x - tipW / 2f, w - padR - tipW));
            float tipY = Math.max(2f * dens, midY - tipH - 8f * dens);
            if (tipY + tipH > baseY) tipY = Math.max(2f * dens, padT);
            tooltipRect.set(tipX, tipY, tipX + tipW, tipY + tipH);
            canvas.drawRoundRect(tooltipRect, 6f * dens, 6f * dens, tooltipBgPaint);
            canvas.drawText(line1, tipX + tipW / 2f, tipY + tipH * 0.38f, tooltipTextPaint);
            canvas.drawText(line2, tipX + tipW / 2f, tipY + tipH * 0.78f, tooltipTextPaint);
        }
    }
}
