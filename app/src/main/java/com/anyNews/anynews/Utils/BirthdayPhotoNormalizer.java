package com.anyNews.anynews.Utils;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.webkit.MimeTypeMap;

import android.media.ExifInterface;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Decode birthday photos (including WhatsApp HEIC/HEIF) to oriented JPEG for
 * preview + upload so server sharp never sees unsupported HEIF.
 */
public final class BirthdayPhotoNormalizer {

    public static final class Result {
        @NonNull public final Uri jpegUri;
        @NonNull public final File jpegFile;
        @NonNull public final Bitmap previewBitmap;

        Result(@NonNull Uri jpegUri, @NonNull File jpegFile, @NonNull Bitmap previewBitmap) {
            this.jpegUri = jpegUri;
            this.jpegFile = jpegFile;
            this.previewBitmap = previewBitmap;
        }
    }

    private BirthdayPhotoNormalizer() {}

    /**
     * Convert any picked image URI to a cached JPEG. Runs decoding on the calling
     * thread — invoke from a background executor.
     */
    @NonNull
    public static Result toJpeg(@NonNull Context context, @NonNull Uri sourceUri) throws Exception {
        Context app = context.getApplicationContext();
        Bitmap decoded = decodeBitmap(app, sourceUri);
        if (decoded == null) {
            throw new IllegalStateException("Could not decode photo (HEIF/HEIC unsupported on device)");
        }
        Bitmap oriented = applyExifOrientation(app, sourceUri, decoded);
        if (oriented != decoded && !decoded.isRecycled()) {
            decoded.recycle();
        }
        // Cap memory for preview/upload
        Bitmap scaled = downscaleIfNeeded(oriented, 2048);
        if (scaled != oriented && oriented != null && !oriented.isRecycled()) {
            oriented.recycle();
        }

        File dir = new File(app.getCacheDir(), "birthday_photos");
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IllegalStateException("Cannot create birthday cache");
        }
        File out = new File(dir, "bday_" + System.currentTimeMillis() + ".jpg");
        try (FileOutputStream fos = new FileOutputStream(out)) {
            if (!scaled.compress(Bitmap.CompressFormat.JPEG, 90, fos)) {
                throw new IllegalStateException("JPEG encode failed");
            }
            fos.flush();
        }
        return new Result(Uri.fromFile(out), out, scaled);
    }

    @Nullable
    private static Bitmap decodeBitmap(@NonNull Context context, @NonNull Uri uri) {
        // 1) ImageDecoder (API 28+) — handles many HEIF variants
        if (Build.VERSION.SDK_INT >= 28) {
            try {
                android.graphics.ImageDecoder.Source src =
                        android.graphics.ImageDecoder.createSource(context.getContentResolver(), uri);
                return android.graphics.ImageDecoder.decodeBitmap(
                        src,
                        (decoder, info, source) -> {
                            decoder.setAllocator(android.graphics.ImageDecoder.ALLOCATOR_SOFTWARE);
                        });
            } catch (Exception ignored) {
            }
        }

        // 2) Byte-buffer BitmapFactory (some providers stream poorly)
        try {
            byte[] bytes = readAll(context.getContentResolver(), uri);
            if (bytes != null && bytes.length > 0) {
                BitmapFactory.Options bounds = new BitmapFactory.Options();
                bounds.inJustDecodeBounds = true;
                BitmapFactory.decodeByteArray(bytes, 0, bytes.length, bounds);
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inSampleSize = sampleForMax(bounds.outWidth, bounds.outHeight, 2048);
                opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);
                if (bmp != null) return bmp;
            }
        } catch (Exception ignored) {
        }

        // 3) Glide — often succeeds where BitmapFactory fails on WhatsApp HEIC
        try {
            return Glide.with(context)
                    .asBitmap()
                    .load(uri)
                    .submit(2048, 2048)
                    .get();
        } catch (Exception ignored) {
        }
        return null;
    }

    private static int sampleForMax(int w, int h, int maxSide) {
        int max = Math.max(w, h);
        int sample = 1;
        while (max / sample > maxSide) sample *= 2;
        return Math.max(1, sample);
    }

    @NonNull
    private static Bitmap downscaleIfNeeded(@NonNull Bitmap src, int maxSide) {
        int w = src.getWidth();
        int h = src.getHeight();
        int max = Math.max(w, h);
        if (max <= maxSide) return src;
        float scale = maxSide / (float) max;
        int nw = Math.max(1, Math.round(w * scale));
        int nh = Math.max(1, Math.round(h * scale));
        return Bitmap.createScaledBitmap(src, nw, nh, true);
    }

    @NonNull
    private static Bitmap applyExifOrientation(
            @NonNull Context context, @NonNull Uri uri, @NonNull Bitmap bitmap) {
        int orientation = ExifInterface.ORIENTATION_UNDEFINED;
        try (InputStream is = context.getContentResolver().openInputStream(uri)) {
            if (is != null) {
                ExifInterface exif = new ExifInterface(is);
                orientation =
                        exif.getAttributeInt(
                                ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            }
        } catch (Exception ignored) {
        }
        // Also try from JPEG bytes already oriented via ImageDecoder (often already applied)
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.postRotate(90f);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.postRotate(180f);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.postRotate(270f);
                break;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.preScale(-1f, 1f);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.preScale(1f, -1f);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap rotated =
                    Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            return rotated != null ? rotated : bitmap;
        } catch (Exception e) {
            return bitmap;
        }
    }

    @Nullable
    private static byte[] readAll(@NonNull ContentResolver cr, @NonNull Uri uri) {
        try (InputStream is = cr.openInputStream(uri)) {
            if (is == null) return null;
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[16384];
            int n;
            while ((n = is.read(buf)) > 0) bos.write(buf, 0, n);
            return bos.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean looksLikeHeif(@NonNull Context context, @NonNull Uri uri) {
        String mime = null;
        try {
            mime = context.getContentResolver().getType(uri);
        } catch (Exception ignored) {
        }
        if (mime != null) {
            String m = mime.toLowerCase();
            if (m.contains("heic") || m.contains("heif")) return true;
        }
        String path = uri.getLastPathSegment();
        if (path != null) {
            String p = path.toLowerCase();
            if (p.endsWith(".heic") || p.endsWith(".heif")) return true;
        }
        String ext = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
        if (ext != null) {
            String e = ext.toLowerCase();
            return "heic".equals(e) || "heif".equals(e);
        }
        return false;
    }
}
