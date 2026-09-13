package com.anyNews.anynews.Ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.io.InputStream;

/**
 * Live Birthday Special poster preview matching admin/server geometry
 * ({@code birthday_template_orange}, 977×1610).
 */
public class BirthdayPosterPreviewView extends View {

    private static final float TEMPLATE_W = 977f;
    private static final float TEMPLATE_H = 1610f;
    private static final float CIRCLE_CX = 493f;
    private static final float CIRCLE_CY = 698f;
    private static final float CIRCLE_RX = 297f;
    private static final float CIRCLE_RY = 315f;
    private static final float NAME_CENTER_X = 488f;
    private static final float NAME_CENTER_Y = 1026f;
    private static final float NAME_MAX_WIDTH = 490f;
    private static final float ADDRESS_START_X = 357f;
    private static final float ADDRESS_CENTER_Y = 1128f;
    private static final float ADDRESS_MAX_WIDTH = 390f;
    private static final float SPECIAL_TOP_Y = 1288f;
    private static final float SPECIAL_LEFT_X = 120f;
    private static final float SPECIAL_MAX_WIDTH = 740f;
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

    @Nullable private Bitmap template;
    @Nullable private Bitmap photo;
    private String nameText = "";
    private String addressText = "";
    private String specialText = "";
    private boolean nameBold;
    private boolean addressBold;
    private float zoom = 1f;

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
                opts.inPreferredConfig = Bitmap.Config.RGB_565;
                template = BitmapFactory.decodeStream(is, null, opts);
                invalidate();
            } catch (Exception ignored) {
            }
        });
    }

    public void setPhotoUri(@Nullable Uri uri) {
        if (uri == null) {
            recyclePhoto();
            invalidate();
            return;
        }
        post(() -> {
            Bitmap decoded = null;
            try (InputStream is = getContext().getContentResolver().openInputStream(uri)) {
                if (is == null) return;
                BitmapFactory.Options bounds = new BitmapFactory.Options();
                bounds.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(is, null, bounds);
            } catch (Exception ignored) {
            }
            try (InputStream is = getContext().getContentResolver().openInputStream(uri)) {
                if (is == null) return;
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inSampleSize = computeInSample(uri);
                opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
                decoded = BitmapFactory.decodeStream(is, null, opts);
            } catch (Exception ignored) {
            }
            recyclePhoto();
            photo = decoded;
            invalidate();
        });
    }

    private int computeInSample(Uri uri) {
        try (InputStream is = getContext().getContentResolver().openInputStream(uri)) {
            if (is == null) return 2;
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, null, bounds);
            int maxSide = Math.max(bounds.outWidth, bounds.outHeight);
            int sample = 1;
            while (maxSide / sample > 1600) sample *= 2;
            return Math.max(1, sample);
        } catch (Exception e) {
            return 2;
        }
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

    public void setZoom(float zoom) {
        this.zoom = Math.max(1f, Math.min(3f, zoom));
        invalidate();
    }

    private void recyclePhoto() {
        if (photo != null && !photo.isRecycled()) {
            photo.recycle();
        }
        photo = null;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        if (width <= 0) {
            width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9f);
        }
        int height = Math.round(width * (TEMPLATE_H / TEMPLATE_W));
        setMeasuredDimension(width, height);
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
            clipPath.reset();
            clipPath.addOval(cx - rx, cy - ry, cx + rx, cy + ry, Path.Direction.CW);
            int save = canvas.save();
            canvas.clipPath(clipPath);
            float destW = rx * 2f;
            float destH = ry * 2f;
            float base = Math.max(destW / photo.getWidth(), destH / photo.getHeight());
            float scale = base * zoom;
            float drawW = photo.getWidth() * scale;
            float drawH = photo.getHeight() * scale;
            float left = cx - drawW / 2f;
            float top = cy - drawH / 2f;
            photoMatrix.reset();
            photoMatrix.postScale(scale, scale);
            photoMatrix.postTranslate(left, top);
            canvas.drawBitmap(photo, photoMatrix, bitmapPaint);
            canvas.restoreToCount(save);
        }

        namePaint.setTextSize(52f * sy);
        namePaint.setTypeface(nameBold ? Typeface.DEFAULT_BOLD : Typeface.create(Typeface.SERIF, Typeface.NORMAL));
        addressPaint.setTextSize(40f * sy);
        addressPaint.setTypeface(
                addressBold ? Typeface.DEFAULT_BOLD : Typeface.create(Typeface.SERIF, Typeface.NORMAL));
        specialPaint.setTextSize(15f * sy);
        specialPaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.NORMAL));

        if (!TextUtils.isEmpty(nameText)) {
            String clipped = TextUtils.ellipsize(
                    nameText, namePaint, NAME_MAX_WIDTH * sx, TextUtils.TruncateAt.END).toString();
            canvas.drawText(clipped, NAME_CENTER_X * sx, NAME_CENTER_Y * sy, namePaint);
        }
        if (!TextUtils.isEmpty(addressText)) {
            String clipped = TextUtils.ellipsize(
                    addressText, addressPaint, ADDRESS_MAX_WIDTH * sx, TextUtils.TruncateAt.END).toString();
            canvas.drawText(
                    clipped, ADDRESS_START_X * sx, ADDRESS_CENTER_Y * sy, addressPaint);
        }
        if (!TextUtils.isEmpty(specialText)) {
            float maxW = SPECIAL_MAX_WIDTH * sx;
            StaticLayout layout =
                    StaticLayout.Builder.obtain(specialText, 0, specialText.length(), specialPaint, (int) maxW)
                            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                            .setMaxLines(3)
                            .setEllipsize(TextUtils.TruncateAt.END)
                            .setLineSpacing(0f, 1.15f)
                            .build();
            canvas.save();
            canvas.translate(SPECIAL_LEFT_X * sx, SPECIAL_TOP_Y * sy - specialPaint.getTextSize());
            layout.draw(canvas);
            canvas.restore();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        recyclePhoto();
    }
}
