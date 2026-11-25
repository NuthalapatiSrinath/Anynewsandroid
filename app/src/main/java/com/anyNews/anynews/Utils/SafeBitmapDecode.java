package com.anyNews.anynews.Utils;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Subsampled bitmap decode that never lets {@link OutOfMemoryError} kill the process.
 * Prefer this for splash / logo / full-bleed images on low-RAM devices.
 */
public final class SafeBitmapDecode {
    private SafeBitmapDecode() {}

    public static boolean isLowRam(@Nullable Context ctx) {
        if (ctx == null) return false;
        try {
            ActivityManager am =
                    (ActivityManager) ctx.getApplicationContext()
                            .getSystemService(Context.ACTIVITY_SERVICE);
            return am != null && am.isLowRamDevice();
        } catch (Throwable ignored) {
            return false;
        }
    }

    /** Soft memory-class hint (MB). Falls back to heap-based estimate. */
    public static int memoryClassMb(@Nullable Context ctx) {
        if (ctx != null) {
            try {
                ActivityManager am =
                        (ActivityManager) ctx.getApplicationContext()
                                .getSystemService(Context.ACTIVITY_SERVICE);
                if (am != null) {
                    return Math.max(16, am.getMemoryClass());
                }
            } catch (Throwable ignored) {
            }
        }
        long max = Runtime.getRuntime().maxMemory() / (1024L * 1024L);
        return (int) Math.max(16L, Math.min(512L, max));
    }

    /**
     * Decode a local file with RGB_565 + inSampleSize so the longest side is
     * roughly at most {@code maxSidePx} (clamped for low-RAM).
     */
    @Nullable
    public static Bitmap decodeFile(@Nullable Context ctx, @Nullable String path, int maxSidePx) {
        if (path == null || path.isEmpty()) return null;
        int cap = clampMaxSide(ctx, maxSidePx);
        try {
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, bounds);
            int w = Math.max(1, bounds.outWidth);
            int h = Math.max(1, bounds.outHeight);
            int sample = 1;
            int longest = Math.max(w, h);
            while (longest / sample > cap && sample < 64) {
                sample *= 2;
            }
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = sample;
            opts.inPreferredConfig = Bitmap.Config.RGB_565;
            opts.inDither = true;
            Bitmap bmp = BitmapFactory.decodeFile(path, opts);
            if (bmp != null) return bmp;
            // Retry once at a coarser sample if decode returned null under pressure.
            if (sample < 16) {
                opts.inSampleSize = sample * 2;
                return BitmapFactory.decodeFile(path, opts);
            }
            return null;
        } catch (OutOfMemoryError oom) {
            try {
                System.gc();
            } catch (Throwable ignored) {
            }
            try {
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inSampleSize = 16;
                opts.inPreferredConfig = Bitmap.Config.RGB_565;
                return BitmapFactory.decodeFile(path, opts);
            } catch (Throwable ignored) {
                return null;
            }
        } catch (Throwable t) {
            return null;
        }
    }

    /** Public clamp used by splash, FlipView Glide, and gallery adapters. */
    public static int clampMaxSide(@Nullable Context ctx, int requested) {
        int base = requested > 0 ? requested : 2048;
        if (isLowRam(ctx)) {
            return Math.min(base, 1280);
        }
        int mem = memoryClassMb(ctx);
        if (mem <= 64) return Math.min(base, 1280);
        if (mem <= 128) return Math.min(base, 1600);
        return Math.min(base, 2048);
    }

    /**
     * Longest side for FlipView / magazine / gallery Glide overrides.
     * Tighter than splash — Camera flip + adjacent scrap must not hold 1080×1920 ARGB.
     */
    public static int flipDecodeMaxSidePx(@Nullable Context ctx) {
        if (isLowRam(ctx)) {
            return 960;
        }
        int mem = memoryClassMb(ctx);
        if (mem <= 64) return 960;
        if (mem <= 128) return 1280;
        return 1600;
    }

    /**
     * Scale {@code w}/{@code h} so the longest side is at most
     * {@link #flipDecodeMaxSidePx(Context)}. Never returns 0.
     */
    @NonNull
    public static int[] clampFlipOverride(@Nullable Context ctx, int w, int h) {
        int rw = Math.max(1, w);
        int rh = Math.max(1, h);
        int max = flipDecodeMaxSidePx(ctx);
        int longest = Math.max(rw, rh);
        if (longest <= max) {
            return new int[]{rw, rh};
        }
        float scale = max / (float) longest;
        return new int[]{
                Math.max(1, Math.round(rw * scale)),
                Math.max(1, Math.round(rh * scale))
        };
    }

    /** Longest side for a full-screen splash given display metrics. */
    public static int splashMaxSidePx(@Nullable Context ctx) {
        if (ctx == null) return 1600;
        try {
            DisplayMetrics dm = ctx.getResources().getDisplayMetrics();
            int longest = Math.max(dm.widthPixels, dm.heightPixels);
            // 1.25× display is enough for FIT_XY splash; avoid decoding 4K banners.
            return clampMaxSide(ctx, Math.max(720, (int) (longest * 1.25f)));
        } catch (Throwable ignored) {
            return clampMaxSide(ctx, 1600);
        }
    }
}
