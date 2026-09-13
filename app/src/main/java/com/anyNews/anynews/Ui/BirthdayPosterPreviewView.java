package com.anyNews.anynews.Ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.anyNews.anynews.Utils.BirthdayFonts;

import java.io.InputStream;

/**
 * Live Birthday Special poster preview matching admin/server geometry
 * ({@code birthday_template_orange}, 977×1610).
 *
 * <p>Draw order matches admin: template → photo (ellipse clip) → plaque band
 * repaint → name / address / special note. Pan is a fraction of the cover box.
 */
public class BirthdayPosterPreviewView extends View {

    private static final float TEMPLATE_W = 977f;
    private static final float TEMPLATE_H = 1610f;
    private static final float CIRCLE_CX = 493f;
    private static final float CIRCLE_CY = 698f;
    private static final float CIRCLE_RX = 297f;
    private static final float CIRCLE_RY = 315f;
    private static final float PLAQUE_TOP_Y = 953f;
    private static final float NAME_CENTER_X = 488f;
    private static final float NAME_CENTER_Y = 1026f;
    private static final float NAME_MAX_WIDTH = 490f;
    private static final float ADDRESS_START_X = 357f;
    private static final float ADDRESS_CENTER_Y = 1128f;
    private static final float ADDRESS_MAX_WIDTH = 390f;
    /** Admin special-note dotted lines (template px). */
    private static final float SPECIAL_LEFT_X = 285f;
    private static final float SPECIAL_MAX_WIDTH = 416f;
    private static final float[] SPECIAL_BASELINES = {1437f, 1460.5f, 1483.5f};
    private static final int TEXT_COLOR = 0xFFE63B19;
    private static final int SPECIAL_COLOR = 0xFF1F2937;

    private final Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final TextPaint namePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final TextPaint addressPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final TextPaint specialPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final Path clipPath = new Path();
    private final RectF destRect = new RectF();
    private final Matrix photoMatrix = new Matrix();
    private final Rect plaqueSrc = new Rect();
    private final RectF plaqueDst = new RectF();

    @Nullable private Bitmap template;
    @Nullable private Bitmap photo;
    private String nameText = "";
    private String addressText = "";
    private String specialText = "";
    private boolean nameBold;
    private boolean addressBold;
    private String nameFontId = BirthdayFonts.DEFAULT_NAME_FONT_ID;
    private String addressFontId = BirthdayFonts.DEFAULT_ADDRESS_FONT_ID;
    private int nameSizePx = BirthdayFonts.NAME_SIZE_MEDIUM;
    private int addressSizePx = BirthdayFonts.ADDR_SIZE_MEDIUM;
    private float zoom = 1f;
    private float panX = 0f;
    private float panY = 0f;
    /** Manual clockwise degrees: 0 / 90 / 180 / 270 */
    private int photoRotation = 0;
    /** Optional cap so compose controls (zoom/rotate) stay on-screen. */
    private int maxHeightPx = 0;

    private float lastTouchX;
    private float lastTouchY;
    private boolean draggingPhoto;
    @Nullable private PanListener panListener;

    public interface PanListener {
        void onPanChanged(float panX, float panY);
    }

    public BirthdayPosterPreviewView(Context context) {
        super(context);
        init();
    }

    public BirthdayPosterPreviewView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BirthdayPosterPreviewView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setWillNotDraw(false);
        setClickable(true);
        circlePaint.setColor(Color.WHITE);
        namePaint.setColor(TEXT_COLOR);
        namePaint.setTextAlign(Paint.Align.CENTER);
        addressPaint.setColor(TEXT_COLOR);
        addressPaint.setTextAlign(Paint.Align.LEFT);
        specialPaint.setColor(SPECIAL_COLOR);
        specialPaint.setTextAlign(Paint.Align.LEFT);
        loadTemplateAsync();
    }

    private void loadTemplateAsync() {
        post(() -> {
            try (InputStream is = getContext().getAssets().open(
                    "birthday/birthday_template_orange.jpg")) {
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
                template = BitmapFactory.decodeStream(is, null, opts);
                invalidate();
            } catch (Exception ignored) {
            }
        });
    }

    public void setPanListener(@Nullable PanListener listener) {
        this.panListener = listener;
    }

    /** Prefer a pre-normalized JPEG bitmap (HEIF already converted). */
    public void setPhotoBitmap(@Nullable Bitmap bitmap) {
        recyclePhoto();
        photo = bitmap;
        invalidate();
    }

    public void setBannerTexts(
            @Nullable String name,
            @Nullable String address,
            @Nullable String special,
            boolean boldName,
            boolean boldAddress) {
        nameText = name == null ? "" : name.trim();
        addressText = address == null ? "" : address.trim();
        specialText = special == null ? "" : special.trim();
        nameBold = boldName;
        addressBold = boldAddress;
        invalidate();
    }

    public void setNameStyle(@Nullable String fontId, int sizePx, boolean bold) {
        nameFontId = TextUtils.isEmpty(fontId) ? BirthdayFonts.DEFAULT_NAME_FONT_ID : fontId;
        nameSizePx = Math.max(12, sizePx);
        nameBold = bold;
        invalidate();
    }

    public void setAddressStyle(@Nullable String fontId, int sizePx, boolean bold) {
        addressFontId = TextUtils.isEmpty(fontId) ? BirthdayFonts.DEFAULT_ADDRESS_FONT_ID : fontId;
        addressSizePx = Math.max(12, sizePx);
        addressBold = bold;
        invalidate();
    }

    public void setZoom(float zoom) {
        this.zoom = Math.max(1f, Math.min(3f, zoom));
        invalidate();
    }

    /** Clockwise 0 / 90 / 180 / 270 — applied after EXIF-normalized bitmap. */
    public void setPhotoRotation(int degrees) {
        int r = ((degrees % 360) + 360) % 360;
        r = (Math.round(r / 90f) * 90) % 360;
        if (r != 0 && r != 90 && r != 180 && r != 270) r = 0;
        if (this.photoRotation == r) return;
        this.photoRotation = r;
        invalidate();
    }

    public int getPhotoRotation() {
        return photoRotation;
    }

    public void setPan(float panX, float panY) {
        this.panX = clampPan(panX);
        this.panY = clampPan(panY);
        invalidate();
    }

    /** Cap measured height (keeps zoom/rotate visible under a tall poster). */
    public void setMaxHeightPx(int maxHeightPx) {
        this.maxHeightPx = Math.max(0, maxHeightPx);
        requestLayout();
    }

    public float getPanX() {
        return panX;
    }

    public float getPanY() {
        return panY;
    }

    private static float clampPan(float v) {
        return Math.max(-1f, Math.min(1f, v));
    }

    private void recyclePhoto() {
        // Caller owns external bitmaps; only clear reference.
        photo = null;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        if (width <= 0) {
            width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9f);
        }
        int height = Math.round(width * (TEMPLATE_H / TEMPLATE_W));
        if (maxHeightPx > 0 && height > maxHeightPx) {
            height = maxHeightPx;
            width = Math.round(height * (TEMPLATE_W / TEMPLATE_H));
        }
        setMeasuredDimension(width, height);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (photo == null) return super.onTouchEvent(event);
        float w = getWidth();
        float h = getHeight();
        if (w <= 0 || h <= 0) return super.onTouchEvent(event);
        float sx = w / TEMPLATE_W;
        float sy = h / TEMPLATE_H;
        float cx = CIRCLE_CX * sx;
        float cy = CIRCLE_CY * sy;
        float rx = CIRCLE_RX * sx;
        float ry = CIRCLE_RY * sy;
        float box = Math.max(rx, ry) * 2f;

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                float dx = (event.getX() - cx) / rx;
                float dy = (event.getY() - cy) / ry;
                if (dx * dx + dy * dy <= 1.05f) {
                    draggingPhoto = true;
                    lastTouchX = event.getX();
                    lastTouchY = event.getY();
                    getParent().requestDisallowInterceptTouchEvent(true);
                    return true;
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (!draggingPhoto) break;
                float dx = event.getX() - lastTouchX;
                float dy = event.getY() - lastTouchY;
                lastTouchX = event.getX();
                lastTouchY = event.getY();
                // pan is fraction of cover box (same as admin fitCover)
                panX = clampPan(panX + dx / box);
                panY = clampPan(panY + dy / box);
                invalidate();
                if (panListener != null) panListener.onPanChanged(panX, panY);
                return true;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (draggingPhoto) {
                    draggingPhoto = false;
                    getParent().requestDisallowInterceptTouchEvent(false);
                    if (panListener != null) panListener.onPanChanged(panX, panY);
                    return true;
                }
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth();
        float h = getHeight();
        if (w <= 0 || h <= 0) return;
        float sx = w / TEMPLATE_W;
        float sy = h / TEMPLATE_H;

        if (template != null && !template.isRecycled()) {
            destRect.set(0, 0, w, h);
            canvas.drawBitmap(template, null, destRect, bitmapPaint);
        } else {
            canvas.drawColor(0xFFFFF1E6);
        }

        if (photo != null && !photo.isRecycled()) {
            float cx = CIRCLE_CX * sx;
            float cy = CIRCLE_CY * sy;
            float rx = CIRCLE_RX * sx;
            float ry = CIRCLE_RY * sy;
            float box = Math.max(rx, ry) * 2f;
            clipPath.reset();
            clipPath.addOval(cx - rx, cy - ry, cx + rx, cy + ry, Path.Direction.CW);
            int save = canvas.save();
            canvas.clipPath(clipPath);
            canvas.drawRect(cx - box / 2f, cy - box / 2f, cx + box / 2f, cy + box / 2f, circlePaint);

            float srcW = photo.getWidth();
            float srcH = photo.getHeight();
            // After 90/270 the cover-fit uses swapped visual bounds.
            float orientW = (photoRotation == 90 || photoRotation == 270) ? srcH : srcW;
            float orientH = (photoRotation == 90 || photoRotation == 270) ? srcW : srcH;
            float base = Math.max(box / orientW, box / orientH);
            float scale = base * zoom;
            float drawW = orientW * scale;
            float drawH = orientH * scale;
            float left = cx - box / 2f + (box - drawW) / 2f + panX * box;
            float top = cy - box / 2f + (box - drawH) / 2f + panY * box;
            photoMatrix.reset();
            // Rotate about photo center, then scale + translate into cover box.
            photoMatrix.postTranslate(-srcW / 2f, -srcH / 2f);
            if (photoRotation != 0) {
                photoMatrix.postRotate(photoRotation);
            }
            photoMatrix.postScale(scale, scale);
            photoMatrix.postTranslate(left + drawW / 2f, top + drawH / 2f);
            canvas.drawBitmap(photo, photoMatrix, bitmapPaint);
            canvas.restoreToCount(save);

            // Repaint plaque band so name plaque sits above the photo (admin order).
            if (template != null && !template.isRecycled()) {
                int natW = template.getWidth();
                int natH = template.getHeight();
                int srcY = Math.round((PLAQUE_TOP_Y / TEMPLATE_H) * natH);
                plaqueSrc.set(0, srcY, natW, natH);
                plaqueDst.set(0, PLAQUE_TOP_Y * sy, w, h);
                canvas.drawBitmap(template, plaqueSrc, plaqueDst, bitmapPaint);
            }
        }

        Typeface nameTf = BirthdayFonts.typefaceForText(getContext(), nameFontId, nameBold, nameText);
        Typeface addrTf =
                BirthdayFonts.typefaceForText(getContext(), addressFontId, addressBold, addressText);
        namePaint.setTypeface(nameTf);
        addressPaint.setTypeface(addrTf);
        specialPaint.setTypeface(
                BirthdayFonts.typefaceForText(
                        getContext(), BirthdayFonts.DEFAULT_SPECIAL_FONT_ID, false, specialText));

        float nameStart = nameSizePx * sy;
        namePaint.setTextSize(fitTextSize(namePaint, nameText, NAME_MAX_WIDTH * sx, nameStart, 16f * sy));
        float addrStart = addressSizePx * sy;
        addressPaint.setTextSize(
                fitTextSize(addressPaint, addressText, ADDRESS_MAX_WIDTH * sx, addrStart, 14f * sy));
        specialPaint.setTextSize(15f * sy);

        if (!TextUtils.isEmpty(nameText)) {
            namePaint.setTextAlign(Paint.Align.CENTER);
            // Middle baseline like admin textBaseline middle
            Paint.FontMetrics fm = namePaint.getFontMetrics();
            float midAdjust = (fm.ascent + fm.descent) / 2f;
            canvas.drawText(nameText, NAME_CENTER_X * sx, NAME_CENTER_Y * sy - midAdjust, namePaint);
        }
        if (!TextUtils.isEmpty(addressText)) {
            addressPaint.setTextAlign(Paint.Align.LEFT);
            Paint.FontMetrics fm = addressPaint.getFontMetrics();
            float midAdjust = (fm.ascent + fm.descent) / 2f;
            String clipped =
                    TextUtils.ellipsize(
                                    addressText,
                                    addressPaint,
                                    ADDRESS_MAX_WIDTH * sx,
                                    TextUtils.TruncateAt.END)
                            .toString();
            canvas.drawText(
                    clipped, ADDRESS_START_X * sx, ADDRESS_CENTER_Y * sy - midAdjust, addressPaint);
        }
        if (!TextUtils.isEmpty(specialText)) {
            float maxW = SPECIAL_MAX_WIDTH * sx;
            StaticLayout layout =
                    StaticLayout.Builder.obtain(
                                    specialText, 0, specialText.length(), specialPaint, (int) maxW)
                            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                            .setMaxLines(3)
                            .setEllipsize(TextUtils.TruncateAt.END)
                            .setLineSpacing(0f, 1f)
                            .setIncludePad(false)
                            .build();
            // Draw each line on the measured dotted-line baselines (admin).
            int lineCount = Math.min(3, layout.getLineCount());
            for (int i = 0; i < lineCount; i++) {
                int start = layout.getLineStart(i);
                int end = layout.getLineEnd(i);
                String line = specialText.substring(start, end).replace("\n", "");
                if (line.isEmpty() && i >= SPECIAL_BASELINES.length) continue;
                float y = SPECIAL_BASELINES[Math.min(i, SPECIAL_BASELINES.length - 1)] * sy;
                canvas.drawText(line, SPECIAL_LEFT_X * sx, y, specialPaint);
            }
        }
    }

    private static float fitTextSize(
            TextPaint paint, String text, float maxWidth, float start, float min) {
        if (TextUtils.isEmpty(text)) return start;
        float size = start;
        while (size > min) {
            paint.setTextSize(size);
            if (paint.measureText(text) <= maxWidth) return size;
            size -= 1f;
        }
        return min;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        photo = null;
    }
}
