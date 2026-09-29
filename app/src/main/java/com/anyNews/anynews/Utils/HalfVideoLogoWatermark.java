package com.anyNews.anynews.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.media.MediaMuxer;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Surface;

import com.anyNews.anynews.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stamps the share logo onto a half-article video file. The default artwork is
 * {@code R.drawable.app_share_logo}. An admin upload
 * ({@link HalfVideoShareLogo}) replaces it when that image is already cached.
 * In-app playback is untouched; only the file produced for download or share
 * is encoded.
 *
 * <p>The logo sits in the bottom-left of the frame the viewer sees, about 16%
 * of the short side, with a small margin. Container rotation is preserved so
 * the picture stays the same and the mark stays upright.
 *
 * <p>A finished file is cached per source URL. A second share reuses it.
 */
public final class HalfVideoLogoWatermark {

    private static final String TAG = "HalfVideoLogo";
    private static final String CACHE_PREFIX = "halfwm_v1_";
    private static final ConcurrentHashMap<String, Object> LOCKS = new ConcurrentHashMap<>();

    private static final int EGL_RECORDABLE_ANDROID = 0x3142;
    private static final long DEQUEUE_TIMEOUT_US = 10_000L;
    private static final int STALL_LIMIT = 800;

    public static final class Outcome {
        public final File file;
        public final boolean branded;
        /** User-facing reason when {@link #branded} is false. */
        public final String fallback;

        Outcome(File file, boolean branded, String fallback) {
            this.file = file;
            this.branded = branded;
            this.fallback = fallback;
        }
    }

    private HalfVideoLogoWatermark() {
    }

    /** Cached branded file for this video URL, or null. */
    public static File peek(Context context, String videoKey) {
        if (context == null || videoKey == null || videoKey.trim().isEmpty()) {
            return null;
        }
        File cached = cacheFile(context, videoKey);
        if (cached.exists() && cached.length() > 4096L) {
            return cached;
        }
        return null;
    }

    /**
     * @return branded cache file, or {@code source} with a fallback reason.
     *         Never returns a silent unbranded file without {@code fallback} set.
     */
    public static Outcome apply(Context context, File source, String videoKey) throws InterruptedException {
        if (context == null || source == null || !source.exists() || source.length() <= 0) {
            return new Outcome(source, false, "Original video kept because the file could not be read.");
        }
        File cached = cacheFile(context, videoKey);
        Object lock = LOCKS.computeIfAbsent(cacheKey(videoKey), key -> new Object());
        synchronized (lock) {
            if (cached.exists() && cached.length() > 4096L) {
                cached.setReadable(true, false);
                return new Outcome(cached, true, null);
            }
            File part = new File(cached.getParentFile(), cached.getName() + ".part");
            //noinspection ResultOfMethodCallIgnored
            part.delete();
            try {
                encode(context, source, part);
                if (Thread.currentThread().isInterrupted()) {
                    //noinspection ResultOfMethodCallIgnored
                    part.delete();
                    throw new InterruptedException();
                }
                if (!part.exists() || part.length() <= 4096L) {
                    //noinspection ResultOfMethodCallIgnored
                    part.delete();
                    return new Outcome(source, false,
                            "Original video kept because the ANYNEWS logo could not be added.");
                }
                if (cached.exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    cached.delete();
                }
                if (!part.renameTo(cached)) {
                    copyFile(part, cached);
                    //noinspection ResultOfMethodCallIgnored
                    part.delete();
                }
                cached.setReadable(true, false);
                Log.i(TAG, "cached branded video " + cached.getName() + " bytes=" + cached.length());
                return new Outcome(cached, true, null);
            } catch (InterruptedException e) {
                //noinspection ResultOfMethodCallIgnored
                part.delete();
                throw e;
            } catch (Exception e) {
                Log.w(TAG, "watermark failed, caller must keep the original", e);
                //noinspection ResultOfMethodCallIgnored
                part.delete();
                //noinspection ResultOfMethodCallIgnored
                cached.delete();
                return new Outcome(source, false,
                        "Original video kept because the ANYNEWS logo could not be added.");
            }
        }
    }

    private static File cacheFile(Context context, String videoKey) {
        File dir = context.getExternalCacheDir();
        if (dir == null) {
            dir = context.getCacheDir();
        }
        File folder = new File(dir, "half_video_wm");
        //noinspection ResultOfMethodCallIgnored
        folder.mkdirs();
        String token = HalfVideoShareLogo.cacheToken(context);
        String name = "builtin".equals(token)
                ? CACHE_PREFIX + cacheKey(videoKey) + ".mp4"
                : "halfwm_v2_" + token + "_" + cacheKey(videoKey) + ".mp4";
        return new File(folder, name);
    }

    private static String cacheKey(String videoKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] bytes = digest.digest(videoKey.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(Character.forDigit((b >> 4) & 0xF, 16));
                sb.append(Character.forDigit(b & 0xF, 16));
            }
            return sb.toString();
        } catch (Exception e) {
            return Integer.toHexString(videoKey.hashCode());
        }
    }

    private static void encode(Context context, File source, File output) throws Exception {
        MediaExtractor extractor = new MediaExtractor();
        MediaCodec decoder = null;
        MediaCodec encoder = null;
        MediaMuxer muxer = null;
        EglBridge egl = null;
        Surface decoderSurface = null;
        SurfaceTexture surfaceTexture = null;
        boolean muxerStarted = false;
        boolean muxerStopFailed = false;
        try {
            extractor.setDataSource(source.getAbsolutePath());
            int videoTrack = selectTrack(extractor, "video/");
            if (videoTrack < 0) {
                throw new IOException("no video track");
            }
            int audioTrack = selectTrack(extractor, "audio/");
            extractor.selectTrack(videoTrack);
            MediaFormat inFormat = extractor.getTrackFormat(videoTrack);
            int width = inFormat.getInteger(MediaFormat.KEY_WIDTH);
            int height = inFormat.getInteger(MediaFormat.KEY_HEIGHT);
            int rotation = readRotation(inFormat, source);
            int encW = width & ~1;
            int encH = height & ~1;
            if (encW < 2 || encH < 2) {
                throw new IOException("video frame too small");
            }

            String mime = inFormat.getString(MediaFormat.KEY_MIME);
            if (mime == null) {
                throw new IOException("missing video mime");
            }

            MediaFormat outFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, encW, encH);
            outFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                    MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            outFormat.setInteger(MediaFormat.KEY_BIT_RATE, pickBitrate(inFormat, encW, encH));
            outFormat.setInteger(MediaFormat.KEY_FRAME_RATE, pickFrameRate(inFormat));
            outFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2);

            encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
            encoder.configure(outFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            Surface encoderSurface = encoder.createInputSurface();
            encoder.start();

            egl = new EglBridge(encoderSurface);
            int videoTex = egl.createExternalTexture();
            surfaceTexture = new SurfaceTexture(videoTex);
            surfaceTexture.setDefaultBufferSize(width, height);
            final Object frameLock = new Object();
            final boolean[] frameAvailable = new boolean[]{false};
            // Listener must have a Looper. The encode thread has none, so use main.
            surfaceTexture.setOnFrameAvailableListener(st -> {
                synchronized (frameLock) {
                    frameAvailable[0] = true;
                    frameLock.notifyAll();
                }
            }, new Handler(Looper.getMainLooper()));
            decoderSurface = new Surface(surfaceTexture);

            decoder = MediaCodec.createDecoderByType(mime);
            decoder.configure(inFormat, decoderSurface, null, 0);
            decoder.start();

            Bitmap logo = loadLogo(context, encW, encH, rotation);
            int[] logoTopLeft = logoTopLeft(encW, encH, rotation, logo.getWidth(), logo.getHeight(),
                    logoMargin(encW, encH));
            egl.uploadLogo(logo);
            logo.recycle();
            Log.i(TAG, "logo at x=" + logoTopLeft[0] + " y=" + logoTopLeft[1]
                    + " frame=" + encW + "x" + encH + " rotation=" + rotation);

            muxer = new MediaMuxer(output.getAbsolutePath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            int muxerVideoTrack = -1;
            int muxerAudioTrack = -1;
            MediaFormat audioFormat = audioTrack >= 0 ? extractor.getTrackFormat(audioTrack) : null;
            if (audioFormat != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                audioFormat.removeKey(MediaFormat.KEY_MAX_INPUT_SIZE);
            }

            MediaCodec.BufferInfo decInfo = new MediaCodec.BufferInfo();
            MediaCodec.BufferInfo encInfo = new MediaCodec.BufferInfo();
            boolean decoderInputDone = false;
            boolean decoderOutputDone = false;
            boolean encoderDone = false;
            int stalls = 0;
            long lastVideoPts = -1L;
            int videoSamples = 0;
            boolean signaledEos = false;

            while (!encoderDone) {
                if (Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException();
                }
                boolean progressed = false;

                if (!decoderInputDone) {
                    int inIndex = decoder.dequeueInputBuffer(DEQUEUE_TIMEOUT_US);
                    if (inIndex >= 0) {
                        ByteBuffer inBuf = decoder.getInputBuffer(inIndex);
                        if (inBuf == null) {
                            decoder.queueInputBuffer(inIndex, 0, 0, 0L, 0);
                        } else {
                            int sampleSize = extractor.readSampleData(inBuf, 0);
                            if (sampleSize < 0) {
                                decoder.queueInputBuffer(inIndex, 0, 0, 0L,
                                        MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                                decoderInputDone = true;
                            } else {
                                long pts = extractor.getSampleTime();
                                decoder.queueInputBuffer(inIndex, 0, sampleSize, pts, 0);
                                extractor.advance();
                            }
                        }
                        progressed = true;
                    }
                }

                if (!decoderOutputDone) {
                    int outIndex = decoder.dequeueOutputBuffer(decInfo, DEQUEUE_TIMEOUT_US);
                    if (outIndex >= 0) {
                        boolean eos = (decInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0;
                        if (decInfo.size > 0) {
                            decoder.releaseOutputBuffer(outIndex, true);
                            awaitFrame(frameLock, frameAvailable);
                            surfaceTexture.updateTexImage();
                            float[] stMatrix = new float[16];
                            surfaceTexture.getTransformMatrix(stMatrix);
                            long pts = decInfo.presentationTimeUs;
                            if (pts < 0) {
                                pts = 0;
                            }
                            if (pts <= lastVideoPts) {
                                pts = lastVideoPts + 1;
                            }
                            lastVideoPts = pts;
                            egl.draw(videoTex, stMatrix, encW, encH, logoTopLeft[0], logoTopLeft[1]);
                            EGLExt.eglPresentationTimeANDROID(egl.display, egl.surface, pts * 1000L);
                            if (!EGL14.eglSwapBuffers(egl.display, egl.surface)) {
                                throw new RuntimeException("eglSwapBuffers failed");
                            }
                        } else {
                            decoder.releaseOutputBuffer(outIndex, false);
                        }
                        if (eos) {
                            decoderOutputDone = true;
                            if (!signaledEos) {
                                encoder.signalEndOfInputStream();
                                signaledEos = true;
                            }
                        }
                        progressed = true;
                    }
                }

                if (!signaledEos && decoderOutputDone) {
                    encoder.signalEndOfInputStream();
                    signaledEos = true;
                }

                int encIndex = encoder.dequeueOutputBuffer(encInfo, DEQUEUE_TIMEOUT_US);
                if (encIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    if (muxerStarted) {
                        throw new RuntimeException("encoder format changed twice");
                    }
                    MediaFormat encFormat = encoder.getOutputFormat();
                    muxerVideoTrack = muxer.addTrack(encFormat);
                    if (audioFormat != null) {
                        muxerAudioTrack = muxer.addTrack(audioFormat);
                    }
                    muxer.setOrientationHint(rotation);
                    muxer.start();
                    muxerStarted = true;
                    progressed = true;
                } else if (encIndex >= 0) {
                    if ((encInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                        encoder.releaseOutputBuffer(encIndex, false);
                    } else {
                        ByteBuffer encoded = encoder.getOutputBuffer(encIndex);
                        if (encoded != null && encInfo.size > 0 && muxerStarted) {
                            encoded.position(encInfo.offset);
                            encoded.limit(encInfo.offset + encInfo.size);
                            muxer.writeSampleData(muxerVideoTrack, encoded, encInfo);
                            videoSamples++;
                        }
                        encoder.releaseOutputBuffer(encIndex, false);
                        if ((encInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                            encoderDone = true;
                        }
                    }
                    progressed = true;
                }

                if (progressed) {
                    stalls = 0;
                } else if (++stalls > STALL_LIMIT) {
                    throw new RuntimeException("video codec stalled");
                }
            }

            if (!muxerStarted || videoSamples == 0) {
                throw new IOException("encoder produced no video");
            }
            if (audioTrack >= 0 && muxerAudioTrack >= 0) {
                copyAudio(extractor, videoTrack, audioTrack, muxer, muxerAudioTrack);
            }
        } finally {
            try {
                if (decoder != null) {
                    decoder.stop();
                    decoder.release();
                }
            } catch (Exception ignored) {
            }
            try {
                if (encoder != null) {
                    encoder.stop();
                    encoder.release();
                }
            } catch (Exception ignored) {
            }
            if (decoderSurface != null) {
                decoderSurface.release();
            }
            if (surfaceTexture != null) {
                surfaceTexture.release();
            }
            if (egl != null) {
                egl.release();
            }
            try {
                if (muxer != null) {
                    if (muxerStarted) {
                        muxer.stop();
                    }
                    muxer.release();
                }
            } catch (Exception e) {
                Log.w(TAG, "muxer stop failed", e);
                muxerStopFailed = true;
                //noinspection ResultOfMethodCallIgnored
                output.delete();
            }
            extractor.release();
        }
        if (muxerStopFailed) {
            throw new IOException("muxer stop failed");
        }
    }

    private static void copyAudio(MediaExtractor extractor, int videoTrack, int audioTrack,
                                  MediaMuxer muxer, int muxerAudioTrack) {
        extractor.unselectTrack(videoTrack);
        extractor.selectTrack(audioTrack);
        extractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
        ByteBuffer buffer = ByteBuffer.allocate(256 * 1024);
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        while (true) {
            int size = extractor.readSampleData(buffer, 0);
            if (size < 0) {
                break;
            }
            if (size > buffer.capacity()) {
                buffer = ByteBuffer.allocate(size);
                size = extractor.readSampleData(buffer, 0);
                if (size < 0) {
                    break;
                }
            }
            info.offset = 0;
            info.size = size;
            info.presentationTimeUs = Math.max(0L, extractor.getSampleTime());
            int flags = extractor.getSampleFlags();
            if ((flags & MediaExtractor.SAMPLE_FLAG_SYNC) != 0) {
                info.flags = MediaCodec.BUFFER_FLAG_KEY_FRAME;
            } else {
                info.flags = 0;
            }
            muxer.writeSampleData(muxerAudioTrack, buffer, info);
            extractor.advance();
        }
    }

    private static void awaitFrame(Object frameLock, boolean[] frameAvailable) throws InterruptedException {
        synchronized (frameLock) {
            long deadline = System.currentTimeMillis() + 2500L;
            while (!frameAvailable[0]) {
                long wait = deadline - System.currentTimeMillis();
                if (wait <= 0) {
                    throw new RuntimeException("decoder frame timeout");
                }
                frameLock.wait(wait);
            }
            frameAvailable[0] = false;
        }
    }

    private static int selectTrack(MediaExtractor extractor, String mimePrefix) {
        int count = extractor.getTrackCount();
        for (int i = 0; i < count; i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime != null && mime.startsWith(mimePrefix)) {
                return i;
            }
        }
        return -1;
    }

    private static int readRotation(MediaFormat format, File source) {
        if (format.containsKey(MediaFormat.KEY_ROTATION)) {
            return normalizeRotation(format.getInteger(MediaFormat.KEY_ROTATION));
        }
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(source.getAbsolutePath());
            String value = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
            if (value == null) {
                return 0;
            }
            return normalizeRotation(Integer.parseInt(value));
        } catch (Exception ignored) {
            return 0;
        } finally {
            try {
                retriever.release();
            } catch (Exception ignored) {
            }
        }
    }

    private static int normalizeRotation(int rotation) {
        int r = ((rotation % 360) + 360) % 360;
        if (r == 90 || r == 180 || r == 270) {
            return r;
        }
        return 0;
    }

    private static int pickBitrate(MediaFormat format, int width, int height) {
        int bitrate = width * height * 4;
        if (format.containsKey(MediaFormat.KEY_BIT_RATE)) {
            try {
                bitrate = format.getInteger(MediaFormat.KEY_BIT_RATE);
            } catch (Exception ignored) {
            }
        }
        if (bitrate < 800_000) {
            bitrate = 800_000;
        }
        if (bitrate > 8_000_000) {
            bitrate = 8_000_000;
        }
        return bitrate;
    }

    private static int pickFrameRate(MediaFormat format) {
        int fps = 30;
        if (format.containsKey(MediaFormat.KEY_FRAME_RATE)) {
            try {
                fps = format.getInteger(MediaFormat.KEY_FRAME_RATE);
            } catch (Exception ignored) {
            }
        }
        if (fps < 1 || fps > 60) {
            return 30;
        }
        return fps;
    }

    /**
     * Bottom-left of the frame the viewer sees, in stored-frame pixels
     * (origin top-left, y down). The player applies {@code rotation} clockwise.
     */
    static int[] logoTopLeft(int frameW, int frameH, int rotation, int logoW, int logoH, int margin) {
        int x;
        int y;
        switch (rotation) {
            case 90:
                // Visual bottom-left is the stored bottom-right.
                x = frameW - margin - logoW;
                y = frameH - margin - logoH;
                break;
            case 180:
                // Visual bottom-left is the stored top-right.
                x = frameW - margin - logoW;
                y = margin;
                break;
            case 270:
                // Visual bottom-left is the stored top-left.
                x = margin;
                y = margin;
                break;
            default:
                x = margin;
                y = frameH - margin - logoH;
                break;
        }
        if (x < 0) {
            x = 0;
        }
        if (y < 0) {
            y = 0;
        }
        return new int[]{x, y};
    }

    private static int logoMargin(int frameW, int frameH) {
        int margin = Math.round(Math.min(frameW, frameH) * 0.035f);
        return Math.max(12, margin);
    }

    private static Bitmap decodeConfiguredLogo(Context context) {
        File file = HalfVideoShareLogo.readyFile(context);
        if (file == null) {
            return null;
        }
        try {
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(file.getAbsolutePath(), bounds);
            int sample = 1;
            int max = Math.max(bounds.outWidth, bounds.outHeight);
            while (max / sample > 1024 && sample < 16) {
                sample *= 2;
            }
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = sample;
            return BitmapFactory.decodeFile(file.getAbsolutePath(), opts);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static Bitmap loadLogo(Context context, int frameW, int frameH, int rotation) {
        Bitmap raw = decodeConfiguredLogo(context);
        if (raw == null) {
            raw = BitmapFactory.decodeResource(context.getResources(), R.drawable.app_share_logo);
        }
        if (raw == null) {
            throw new IllegalStateException("app_share_logo missing");
        }
        int shortSide = Math.min(frameW, frameH);
        int target = Math.round(shortSide * 0.16f);
        target = Math.max(48, Math.min(target, Math.max(48, shortSide / 3)));
        int longSide = Math.max(raw.getWidth(), raw.getHeight());
        float scale = target / (float) Math.max(1, longSide);
        int w = Math.max(1, Math.round(raw.getWidth() * scale));
        int h = Math.max(1, Math.round(raw.getHeight() * scale));
        Bitmap scaled = Bitmap.createScaledBitmap(raw, w, h, true);
        if (scaled != raw) {
            raw.recycle();
        }
        if (rotation == 0) {
            return scaled;
        }
        Matrix matrix = new Matrix();
        // Player rotates the frame clockwise; pre-rotate the mark the other way.
        matrix.postRotate(-rotation);
        Bitmap rotated = Bitmap.createBitmap(scaled, 0, 0, scaled.getWidth(), scaled.getHeight(), matrix, true);
        if (rotated != scaled) {
            scaled.recycle();
        }
        return rotated;
    }

    private static void copyFile(File from, File to) throws IOException {
        try (FileInputStream in = new FileInputStream(from);
             FileOutputStream out = new FileOutputStream(to)) {
            byte[] buffer = new byte[1 << 16];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            out.flush();
        }
    }

    /**
     * Draws each decoded frame into the encoder surface, then the share logo.
     */
    private static final class EglBridge {
        final EGLDisplay display;
        final EGLSurface surface;
        private final EGLContext context;
        private final int videoProgram;
        private final int logoProgram;
        private int logoTexture;
        private int logoW;
        private int logoH;
        private final FloatBuffer videoPos;
        private final FloatBuffer videoTex;
        private FloatBuffer logoPos;
        private final FloatBuffer logoTexCoords;
        private final int videoPosAttr;
        private final int videoTexAttr;
        private final int videoStHandle;
        private final int videoSampler;
        private final int logoPosAttr;
        private final int logoTexAttr;
        private final int logoSampler;

        EglBridge(Surface encoderSurface) {
            display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
            if (display == EGL14.EGL_NO_DISPLAY) {
                throw new RuntimeException("no EGL display");
            }
            int[] version = new int[2];
            if (!EGL14.eglInitialize(display, version, 0, version, 1)) {
                throw new RuntimeException("eglInitialize failed");
            }
            int[] attribs = {
                    EGL14.EGL_RED_SIZE, 8,
                    EGL14.EGL_GREEN_SIZE, 8,
                    EGL14.EGL_BLUE_SIZE, 8,
                    EGL14.EGL_ALPHA_SIZE, 8,
                    EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                    EGL_RECORDABLE_ANDROID, 1,
                    EGL14.EGL_NONE
            };
            EGLConfig[] configs = new EGLConfig[1];
            int[] num = new int[1];
            if (!EGL14.eglChooseConfig(display, attribs, 0, configs, 0, 1, num, 0)) {
                throw new RuntimeException("eglChooseConfig failed");
            }
            int[] ctxAttribs = {
                    EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                    EGL14.EGL_NONE
            };
            context = EGL14.eglCreateContext(display, configs[0], EGL14.EGL_NO_CONTEXT, ctxAttribs, 0);
            if (context == null || context == EGL14.EGL_NO_CONTEXT) {
                throw new RuntimeException("eglCreateContext failed");
            }
            int[] surfaceAttribs = {EGL14.EGL_NONE};
            surface = EGL14.eglCreateWindowSurface(display, configs[0], encoderSurface, surfaceAttribs, 0);
            if (surface == null || surface == EGL14.EGL_NO_SURFACE) {
                throw new RuntimeException("eglCreateWindowSurface failed");
            }
            if (!EGL14.eglMakeCurrent(display, surface, surface, context)) {
                throw new RuntimeException("eglMakeCurrent failed");
            }

            videoProgram = link(
                    "attribute vec4 aPosition;\n"
                            + "attribute vec2 aTextureCoord;\n"
                            + "uniform mat4 uSTMatrix;\n"
                            + "varying vec2 vTex;\n"
                            + "void main() {\n"
                            + "  gl_Position = aPosition;\n"
                            + "  vTex = (uSTMatrix * vec4(aTextureCoord, 0.0, 1.0)).xy;\n"
                            + "}\n",
                    "#extension GL_OES_EGL_image_external : require\n"
                            + "precision mediump float;\n"
                            + "varying vec2 vTex;\n"
                            + "uniform samplerExternalOES sTex;\n"
                            + "void main() {\n"
                            + "  gl_FragColor = texture2D(sTex, vTex);\n"
                            + "}\n");
            logoProgram = link(
                    "attribute vec4 aPosition;\n"
                            + "attribute vec2 aTexCoord;\n"
                            + "varying vec2 vTex;\n"
                            + "void main() {\n"
                            + "  gl_Position = aPosition;\n"
                            + "  vTex = aTexCoord;\n"
                            + "}\n",
                    "precision mediump float;\n"
                            + "varying vec2 vTex;\n"
                            + "uniform sampler2D sTex;\n"
                            + "void main() {\n"
                            + "  gl_FragColor = texture2D(sTex, vTex);\n"
                            + "}\n");
            videoPosAttr = GLES20.glGetAttribLocation(videoProgram, "aPosition");
            videoTexAttr = GLES20.glGetAttribLocation(videoProgram, "aTextureCoord");
            videoStHandle = GLES20.glGetUniformLocation(videoProgram, "uSTMatrix");
            videoSampler = GLES20.glGetUniformLocation(videoProgram, "sTex");
            logoPosAttr = GLES20.glGetAttribLocation(logoProgram, "aPosition");
            logoTexAttr = GLES20.glGetAttribLocation(logoProgram, "aTexCoord");
            logoSampler = GLES20.glGetUniformLocation(logoProgram, "sTex");

            videoPos = asBuffer(new float[]{
                    -1f, -1f,
                    1f, -1f,
                    -1f, 1f,
                    1f, 1f
            });
            videoTex = asBuffer(new float[]{
                    0f, 0f,
                    1f, 0f,
                    0f, 1f,
                    1f, 1f
            });
            // GLUtils uploads bitmap top at texture t=0, so the quad top samples t=0.
            logoTexCoords = asBuffer(new float[]{
                    0f, 1f,
                    1f, 1f,
                    0f, 0f,
                    1f, 0f
            });
        }

        int createExternalTexture() {
            int[] textures = new int[1];
            GLES20.glGenTextures(1, textures, 0);
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0]);
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            return textures[0];
        }

        void uploadLogo(Bitmap bitmap) {
            logoW = bitmap.getWidth();
            logoH = bitmap.getHeight();
            int[] textures = new int[1];
            GLES20.glGenTextures(1, textures, 0);
            logoTexture = textures[0];
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, logoTexture);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        }

        void draw(int videoTexture, float[] stMatrix, int frameW, int frameH, int logoX, int logoY) {
            GLES20.glViewport(0, 0, frameW, frameH);
            GLES20.glDisable(GLES20.GL_BLEND);
            GLES20.glClearColor(0f, 0f, 0f, 1f);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

            GLES20.glUseProgram(videoProgram);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, videoTexture);
            GLES20.glUniform1i(videoSampler, 0);
            GLES20.glUniformMatrix4fv(videoStHandle, 1, false, stMatrix, 0);
            videoPos.position(0);
            videoTex.position(0);
            GLES20.glEnableVertexAttribArray(videoPosAttr);
            GLES20.glVertexAttribPointer(videoPosAttr, 2, GLES20.GL_FLOAT, false, 0, videoPos);
            GLES20.glEnableVertexAttribArray(videoTexAttr);
            GLES20.glVertexAttribPointer(videoTexAttr, 2, GLES20.GL_FLOAT, false, 0, videoTex);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
            GLES20.glDisableVertexAttribArray(videoPosAttr);
            GLES20.glDisableVertexAttribArray(videoTexAttr);

            if (logoPos == null) {
                logoPos = logoQuad(frameW, frameH, logoX, logoY, logoW, logoH);
            }
            GLES20.glEnable(GLES20.GL_BLEND);
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
            GLES20.glUseProgram(logoProgram);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, logoTexture);
            GLES20.glUniform1i(logoSampler, 0);
            logoPos.position(0);
            logoTexCoords.position(0);
            GLES20.glEnableVertexAttribArray(logoPosAttr);
            GLES20.glVertexAttribPointer(logoPosAttr, 2, GLES20.GL_FLOAT, false, 0, logoPos);
            GLES20.glEnableVertexAttribArray(logoTexAttr);
            GLES20.glVertexAttribPointer(logoTexAttr, 2, GLES20.GL_FLOAT, false, 0, logoTexCoords);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
            GLES20.glDisableVertexAttribArray(logoPosAttr);
            GLES20.glDisableVertexAttribArray(logoTexAttr);
            GLES20.glDisable(GLES20.GL_BLEND);
        }

        void release() {
            if (logoTexture != 0) {
                int[] textures = new int[]{logoTexture};
                GLES20.glDeleteTextures(1, textures, 0);
                logoTexture = 0;
            }
            EGL14.eglMakeCurrent(display, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
            EGL14.eglDestroySurface(display, surface);
            EGL14.eglDestroyContext(display, context);
            EGL14.eglTerminate(display);
        }

        private static FloatBuffer logoQuad(int frameW, int frameH, int x, int y, int logoW, int logoH) {
            float left = (x / (float) frameW) * 2f - 1f;
            float right = ((x + logoW) / (float) frameW) * 2f - 1f;
            float top = 1f - (y / (float) frameH) * 2f;
            float bottom = 1f - ((y + logoH) / (float) frameH) * 2f;
            return asBuffer(new float[]{
                    left, bottom,
                    right, bottom,
                    left, top,
                    right, top
            });
        }

        private static int link(String vertexSrc, String fragmentSrc) {
            int program = GLES20.glCreateProgram();
            int vertex = compile(GLES20.GL_VERTEX_SHADER, vertexSrc);
            int fragment = compile(GLES20.GL_FRAGMENT_SHADER, fragmentSrc);
            GLES20.glAttachShader(program, vertex);
            GLES20.glAttachShader(program, fragment);
            GLES20.glLinkProgram(program);
            int[] linked = new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linked, 0);
            if (linked[0] == 0) {
                String log = GLES20.glGetProgramInfoLog(program);
                GLES20.glDeleteProgram(program);
                throw new RuntimeException("program link failed: " + log);
            }
            return program;
        }

        private static int compile(int type, String src) {
            int shader = GLES20.glCreateShader(type);
            GLES20.glShaderSource(shader, src);
            GLES20.glCompileShader(shader);
            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {
                String log = GLES20.glGetShaderInfoLog(shader);
                GLES20.glDeleteShader(shader);
                throw new RuntimeException("shader compile failed: " + log);
            }
            return shader;
        }

        private static FloatBuffer asBuffer(float[] data) {
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(data.length * 4);
            byteBuffer.order(ByteOrder.nativeOrder());
            FloatBuffer buffer = byteBuffer.asFloatBuffer();
            buffer.put(data);
            buffer.position(0);
            return buffer;
        }
    }
}
