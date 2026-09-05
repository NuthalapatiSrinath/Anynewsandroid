package com.anyNews.anynews.Utils;

import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.annotation.Nullable;

/**
 * Full-bleed Daily Updates card backgrounds: cover-scale, left-biased crop,
 * plus admin {@code card_image_offset_dp} (negative = more art on the left).
 * Strong overscale clips baked-in neon borders / right-edge art strokes so the
 * XML foreground stroke is the only visible 4-sided border.
 */
public final class DailyUpdatesArtHelper {

    /** Extra scale beyond cover — clips ~10% of art edges (baked borders). */
    private static final float EDGE_CLIP_OVERSCALE = 1.14f;

    private DailyUpdatesArtHelper() {}

    public static void applyCoverLeftBias(@Nullable ImageView iv, int offsetDp) {
        if (iv == null) return;
        iv.setScaleType(ImageView.ScaleType.MATRIX);
        Runnable apply = () -> {
            try {
                Drawable d = iv.getDrawable();
                int vw = iv.getWidth();
                int vh = iv.getHeight();
                if (d == null || vw <= 0 || vh <= 0) return;
                int dw = d.getIntrinsicWidth();
                int dh = d.getIntrinsicHeight();
                if (dw <= 0 || dh <= 0) return;
                float scale = Math.max((float) vw / (float) dw, (float) vh / (float) dh)
                        * EDGE_CLIP_OVERSCALE;
                float density = iv.getResources().getDisplayMetrics().density;
                float dx = offsetDp * density;
                float dy = (vh - dh * scale) * 0.5f;
                // Crop both left and right baked borders via overscale bias.
                float overflowX = dw * scale - vw;
                if (overflowX > 0) {
                    dx -= overflowX * 0.35f;
                }
                Matrix m = new Matrix();
                m.setScale(scale, scale);
                m.postTranslate(dx, dy);
                iv.setImageMatrix(m);
            } catch (Exception ignored) {
            }
        };
        if (iv.getWidth() > 0 && iv.getHeight() > 0) {
            apply.run();
        } else {
            iv.post(apply);
        }
    }

    public static void applyCoverLeftBias(@Nullable ImageView iv) {
        if (iv == null) return;
        applyCoverLeftBias(iv, DailyUpdatesConfigHelper.cardImageOffsetDp(iv.getContext()));
    }
}
