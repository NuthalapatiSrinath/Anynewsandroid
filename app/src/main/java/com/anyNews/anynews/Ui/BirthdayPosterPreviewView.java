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
import android.os.Looper;
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
import java.net.HttpURLConnection;
import java.net.URL;

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

    /**
     * Template B (celebration poster, 1003×1568). Photo window is the white
     * rounded rectangle, not A's circle. Name sits on the plaque under it.
     *
     * <p>The window is the inner edge of the orange stroke. The top-left and
     * bottom-right ribbons are decorations painted on top of that photo. They
     * are not holes and the photo is not inset to dodge them.
     */
    private static final float B_W = 1003f;
    private static final float B_H = 1568f;
    private static final float B_PHOTO_L = 243f;
    private static final float B_PHOTO_T = 360f;
    private static final float B_PHOTO_R = 869f;
    private static final float B_PHOTO_B = 831f;
    private static final float B_PHOTO_RADIUS = 40f;
    private static final float B_NAME_CX = 564f;
    private static final float B_NAME_CY = 936f;
    private static final float B_NAME_MAX_W = 440f;
    private static final float B_NAME_MAX_SIZE = 46f;
    private static final float B_SPECIAL_LEFT = 336f;
    private static final float B_SPECIAL_MAX_W = 400f;
    private static final float[] B_SPECIAL_BASELINES = {1324f, 1354f, 1383f, 1413f};
    private static final String ASSET_A = "birthday/birthday_template_orange.jpg";

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
    /**
     * Template B artwork with the white photo hole transparent, so the ribbons
     * and orange border paint over the photo. Null for template A.
     */
    @Nullable private Bitmap frameOverlay;
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
    /**
     * False = drawing template A (circle). True only after celebration artwork
     * has actually decoded. A missing or in-flight URL stays on A.
     */
    private boolean templateB;
    private int templateLoadGen;
    /** Generation whose celebration bitmap is installed. -1 until a download wins. */
    private int celebrationGen = -1;
    @Nullable private String loadedCelebrationUrl = "";
    @Nullable private ArtworkListener artworkListener;

    private float lastTouchX;
    private float lastTouchY;
    private boolean draggingPhoto;
    @Nullable private PanListener panListener;

    public interface PanListener {
        void onPanChanged(float panX, float panY);
    }

    /** Fires when the preview switches between the circle poster and celebration art. */
    public interface ArtworkListener {
        void onArtworkChanged(boolean celebration);
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
        showBundledCircle(0);
    }

    public void setArtworkListener(@Nullable ArtworkListener listener) {
        artworkListener = listener;
    }

    /**
     * Switch artwork. Template A keeps the circle asset and geometry.
     * Template B is not packaged. Until {@code birthday_template_image_url}
     * decodes, and whenever that URL is empty or the download fails, the
     * preview stays on the bundled circle poster — never an empty canvas.
     */
    public void applyTemplate(@Nullable String templateId, @Nullable String imageUrl) {
        final boolean wantB = "b".equalsIgnoreCase(templateId == null ? "" : templateId.trim());
        final String url = imageUrl == null ? "" : imageUrl.trim();
        final int gen = ++templateLoadGen;
        final boolean http = url.startsWith("http://") || url.startsWith("https://");
        if (wantB && http && TextUtils.equals(url, loadedCelebrationUrl) && showingCelebration()) {
            notifyArtwork();
            requestLayout();
            invalidate();
            return;
        }
        // Circle immediately. Do not flip into B geometry with a null bitmap.
        showBundledCircle(gen);
        if (wantB && http) {
            final String fetchUrl = url;
            new Thread(
                    () -> {
                        Bitmap bmp = decodeUrl(fetchUrl);
                        Bitmap overlay = bmp != null ? punchCelebrationHole(bmp) : null;
                        post(
                                () -> {
                                    if (gen != templateLoadGen) {
                                        recycleBitmap(bmp);
                                        recycleBitmap(overlay);
                                        return;
                                    }
                                    if (bmp == null) {
                                        recycleBitmap(overlay);
                                        showBundledCircle(gen);
                                        return;
                                    }
                                    loadedCelebrationUrl = fetchUrl;
                                    celebrationGen = gen;
                                    templateB = true;
                                    replaceArtwork(bmp, overlay);
                                    requestLayout();
                                    invalidate();
                                    notifyArtwork();
                                });
                    })
                    .start();
        }
        requestLayout();
        invalidate();
    }

    /** True only while the celebration bitmap is what this view is drawing. */
    public boolean isTemplateB() {
        return showingCelebration();
    }

    private boolean showingCelebration() {
        return templateB && template != null && !template.isRecycled();
    }

    /**
     * Bundled circle banner. Runs on the UI thread so the first draw already
     * has artwork. Never clears the bitmap to paint an empty poster.
     */
    private void showBundledCircle(int gen) {
        Runnable job =
                () -> {
                    if (gen != templateLoadGen && gen != 0) return;
                    if (gen == 0 && templateLoadGen != 0) return;
                    // This generation already installed celebration art (download won).
                    if (celebrationGen == gen && showingCelebration()) return;
                    if (!templateB && template != null && !template.isRecycled()) {
                        notifyArtwork();
                        return;
                    }
                    try (InputStream is = getContext().getAssets().open(ASSET_A)) {
                        BitmapFactory.Options opts = new BitmapFactory.Options();
                        opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
                        Bitmap bmp = BitmapFactory.decodeStream(is, null, opts);
                        if (gen != templateLoadGen && gen != 0) {
                            recycleBitmap(bmp);
                            return;
                        }
                        if (gen == 0 && templateLoadGen != 0) {
                            recycleBitmap(bmp);
                            return;
                        }
                        if (celebrationGen == gen && showingCelebration()) {
                            recycleBitmap(bmp);
                            return;
                        }
                        if (bmp == null) return;
                        templateB = false;
                        celebrationGen = -1;
                        loadedCelebrationUrl = "";
                        replaceArtwork(bmp, null);
                        requestLayout();
                        invalidate();
                        notifyArtwork();
                    } catch (Exception ignored) {
                    }
                };
        if (Looper.myLooper() == Looper.getMainLooper()) job.run();
        else post(job);
    }

    private void notifyArtwork() {
        if (artworkListener != null) {
            artworkListener.onArtworkChanged(showingCelebration());
        }
    }

    private void replaceArtwork(@Nullable Bitmap nextTemplate, @Nullable Bitmap nextOverlay) {
        Bitmap oldTemplate = template;
        Bitmap oldOverlay = frameOverlay;
        template = nextTemplate;
        frameOverlay = nextOverlay;
        if (oldTemplate != nextTemplate && oldTemplate != nextOverlay) recycleBitmap(oldTemplate);
        if (oldOverlay != nextTemplate && oldOverlay != nextOverlay) recycleBitmap(oldOverlay);
    }

    private static void recycleBitmap(@Nullable Bitmap bmp) {
        if (bmp != null && !bmp.isRecycled()) bmp.recycle();
    }

    /**
     * Copy of the celebration artwork with the white photo window transparent.
     * Orange ribbons and the border stay opaque so they sit on the photo.
     */
    @Nullable
    private static Bitmap punchCelebrationHole(@Nullable Bitmap src) {
        if (src == null || src.isRecycled()) return null;
        Bitmap out = src.copy(Bitmap.Config.ARGB_8888, true);
        if (out == null) return null;
        int bw = out.getWidth();
        int bh = out.getHeight();
        if (bw <= 0 || bh <= 0) {
            recycleBitmap(out);
            return null;
        }
        int[] px = new int[bw * bh];
        out.getPixels(px, 0, bw, 0, 0, bw, bh);
        float sx = bw / B_W;
        float sy = bh / B_H;
        float left = B_PHOTO_L * sx;
        float top = B_PHOTO_T * sy;
        float right = B_PHOTO_R * sx;
        float bottom = B_PHOTO_B * sy;
        float rad = B_PHOTO_RADIUS * Math.min(sx, sy);
        int x0 = Math.max(0, (int) Math.floor(left));
        int y0 = Math.max(0, (int) Math.floor(top));
        int x1 = Math.min(bw - 1, (int) Math.ceil(right));
        int y1 = Math.min(bh - 1, (int) Math.ceil(bottom));
        for (int y = y0; y <= y1; y++) {
            int row = y * bw;
            for (int x = x0; x <= x1; x++) {
                if (!hitRoundRect(x + 0.5f, y + 0.5f, left, top, right, bottom, rad)) continue;
                int c = px[row + x];
                if (!isPhotoHoleWhite(c) || nearRibbonOrange(px, bw, bh, x, y)) continue;
                px[row + x] = 0;
            }
        }
        out.setPixels(px, 0, bw, 0, 0, bw, bh);
        return out;
    }

    /** Flat white placeholder inside the frame. Ribbon orange is not this. */
    private static boolean isPhotoHoleWhite(int c) {
        int r = (c >> 16) & 0xff;
        int g = (c >> 8) & 0xff;
        int b = c & 0xff;
        int max = Math.max(r, Math.max(g, b));
        int min = Math.min(r, Math.min(g, b));
        return (max - min) <= 18 && max >= 230;
    }

    private static boolean isRibbonOrange(int c) {
        int r = (c >> 16) & 0xff;
        int g = (c >> 8) & 0xff;
        int b = c & 0xff;
        return r > 200 && g < 180 && b < 110 && (r - g) > 50 && (r - b) > 80;
    }

    /** Keep bright folds on the ribbon; they sit on orange, not in the white hole. */
    private static boolean nearRibbonOrange(int[] px, int bw, int bh, int x, int y) {
        int yStart = Math.max(0, y - 2);
        int yEnd = Math.min(bh - 1, y + 2);
        int xStart = Math.max(0, x - 2);
        int xEnd = Math.min(bw - 1, x + 2);
        for (int yy = yStart; yy <= yEnd; yy++) {
            int row = yy * bw;
            for (int xx = xStart; xx <= xEnd; xx++) {
                if (isRibbonOrange(px[row + xx])) return true;
            }
        }
        return false;
    }

    @Nullable
    private static Bitmap decodeUrl(String url) {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(25000);
            conn.setInstanceFollowRedirects(true);
            conn.connect();
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) return null;
            try (InputStream is = conn.getInputStream()) {
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
                return BitmapFactory.decodeStream(is, null, opts);
            }
        } catch (Exception ignored) {
            return null;
        } finally {
            if (conn != null) conn.disconnect();
        }
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
        // Always the full card width. Shrinking width to honor maxHeightPx left
        // the poster start-aligned with an empty gap on the right.
        float aspect = showingCelebration() ? (B_H / B_W) : (TEMPLATE_H / TEMPLATE_W);
        int height = Math.max(1, Math.round(width * aspect));
        setMeasuredDimension(width, height);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (photo == null) return super.onTouchEvent(event);
        float w = getWidth();
        float h = getHeight();
        if (w <= 0 || h <= 0) return super.onTouchEvent(event);
        if (showingCelebration()) return onTouchTemplateB(event, w, h);
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
        if (showingCelebration()) {
            drawTemplateB(canvas, w, h);
            return;
        }
        float sx = w / TEMPLATE_W;
        float sy = h / TEMPLATE_H;

        if (template != null && !template.isRecycled()) {
            destRect.set(0, 0, w, h);
            canvas.drawBitmap(template, null, destRect, bitmapPaint);
        } else {
            return;
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

    private boolean onTouchTemplateB(MotionEvent event, float w, float h) {
        float sx = w / B_W;
        float sy = h / B_H;
        float left = B_PHOTO_L * sx;
        float top = B_PHOTO_T * sy;
        float right = B_PHOTO_R * sx;
        float bottom = B_PHOTO_B * sy;
        float rad = B_PHOTO_RADIUS * Math.min(sx, sy);
        float destW = right - left;
        float destH = bottom - top;

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                if (hitRoundRect(event.getX(), event.getY(), left, top, right, bottom, rad)) {
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
                panX = clampPan(panX + dx / destW);
                panY = clampPan(panY + dy / destH);
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

    private static boolean hitRoundRect(
            float x, float y, float left, float top, float right, float bottom, float rad) {
        if (x < left || x > right || y < top || y > bottom) return false;
        float rr = Math.min(rad, Math.min((right - left) / 2f, (bottom - top) / 2f));
        if (x < left + rr && y < top + rr) {
            float dx = x - (left + rr);
            float dy = y - (top + rr);
            return dx * dx + dy * dy <= rr * rr;
        }
        if (x > right - rr && y < top + rr) {
            float dx = x - (right - rr);
            float dy = y - (top + rr);
            return dx * dx + dy * dy <= rr * rr;
        }
        if (x < left + rr && y > bottom - rr) {
            float dx = x - (left + rr);
            float dy = y - (bottom - rr);
            return dx * dx + dy * dy <= rr * rr;
        }
        if (x > right - rr && y > bottom - rr) {
            float dx = x - (right - rr);
            float dy = y - (bottom - rr);
            return dx * dx + dy * dy <= rr * rr;
        }
        return true;
    }

    /**
     * Rounded-rectangle poster. Does not use A's circle coordinates.
     * Photo fills the inner frame; the ribbon overlay is painted after it.
     */
    private void drawTemplateB(Canvas canvas, float w, float h) {
        float sx = w / B_W;
        float sy = h / B_H;
        float left = B_PHOTO_L * sx;
        float top = B_PHOTO_T * sy;
        float right = B_PHOTO_R * sx;
        float bottom = B_PHOTO_B * sy;
        float destW = right - left;
        float destH = bottom - top;
        float rad = B_PHOTO_RADIUS * Math.min(sx, sy);
        boolean hasPhoto = photo != null && !photo.isRecycled();
        boolean ribbonsOnTop =
                hasPhoto && frameOverlay != null && !frameOverlay.isRecycled();
        if (!ribbonsOnTop && template != null && !template.isRecycled()) {
            destRect.set(0, 0, w, h);
            canvas.drawBitmap(template, null, destRect, bitmapPaint);
        }
        if (hasPhoto) {
            clipPath.reset();
            clipPath.addRoundRect(left, top, right, bottom, rad, rad, Path.Direction.CW);
            int save = canvas.save();
            canvas.clipPath(clipPath);
            canvas.drawRect(left, top, right, bottom, circlePaint);
            float srcW = photo.getWidth();
            float srcH = photo.getHeight();
            float orientW = (photoRotation == 90 || photoRotation == 270) ? srcH : srcW;
            float orientH = (photoRotation == 90 || photoRotation == 270) ? srcW : srcH;
            float base = Math.max(destW / orientW, destH / orientH);
            float scale = base * zoom;
            float drawW = orientW * scale;
            float drawH = orientH * scale;
            float drawLeft = left + (destW - drawW) / 2f + panX * destW;
            float drawTop = top + (destH - drawH) / 2f + panY * destH;
            photoMatrix.reset();
            photoMatrix.postTranslate(-srcW / 2f, -srcH / 2f);
            if (photoRotation != 0) photoMatrix.postRotate(photoRotation);
            photoMatrix.postScale(scale, scale);
            photoMatrix.postTranslate(drawLeft + drawW / 2f, drawTop + drawH / 2f);
            canvas.drawBitmap(photo, photoMatrix, bitmapPaint);
            canvas.restoreToCount(save);
        }
        if (ribbonsOnTop) {
            destRect.set(0, 0, w, h);
            canvas.drawBitmap(frameOverlay, null, destRect, bitmapPaint);
        }

        Typeface nameTf = BirthdayFonts.typefaceForText(getContext(), nameFontId, nameBold, nameText);
        namePaint.setTypeface(nameTf);
        float nameCap = Math.min(nameSizePx * sy, B_NAME_MAX_SIZE * sy);
        namePaint.setTextSize(fitTextSize(namePaint, nameText, B_NAME_MAX_W * sx, nameCap, 14f * sy));
        if (!TextUtils.isEmpty(nameText)) {
            namePaint.setTextAlign(Paint.Align.CENTER);
            Paint.FontMetrics fm = namePaint.getFontMetrics();
            float midAdjust = (fm.ascent + fm.descent) / 2f;
            canvas.drawText(nameText, B_NAME_CX * sx, B_NAME_CY * sy - midAdjust, namePaint);
        }
        if (!TextUtils.isEmpty(specialText)) {
            specialPaint.setTypeface(
                    BirthdayFonts.typefaceForText(
                            getContext(), BirthdayFonts.DEFAULT_SPECIAL_FONT_ID, false, specialText));
            specialPaint.setTextSize(15f * sy);
            specialPaint.setTextAlign(Paint.Align.LEFT);
            float maxW = B_SPECIAL_MAX_W * sx;
            StaticLayout layout =
                    StaticLayout.Builder.obtain(
                                    specialText, 0, specialText.length(), specialPaint, (int) maxW)
                            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                            .setMaxLines(B_SPECIAL_BASELINES.length)
                            .setEllipsize(TextUtils.TruncateAt.END)
                            .setIncludePad(false)
                            .build();
            int lineCount = Math.min(B_SPECIAL_BASELINES.length, layout.getLineCount());
            for (int i = 0; i < lineCount; i++) {
                int start = layout.getLineStart(i);
                int end = layout.getLineEnd(i);
                String line = specialText.substring(start, end).replace("\n", "");
                canvas.drawText(line, B_SPECIAL_LEFT * sx, B_SPECIAL_BASELINES[i] * sy, specialPaint);
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
