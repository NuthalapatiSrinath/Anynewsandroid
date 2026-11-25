package com.anyNews.anynews.Application;


import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.toolbox.ImageLoader.ImageCache;

public class LruBitmapCache extends LruCache<String, Bitmap> implements
        ImageCache {

    Context mTCx;
    public LruBitmapCache() {
        this(getDefaultLruCacheSize());
    }

    public LruBitmapCache(int sizeInKiloBytes) {
        super(sizeInKiloBytes);
    }



    public static int getDefaultLruCacheSize() {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        // /8 is aggressive on 64–128MB heaps; keep a smaller share to avoid OOM.
        int divisor = 8;
        try {
            if (com.anyNews.anynews.Utils.SafeBitmapDecode.isLowRam(
                    com.anyNews.anynews.Application.AppController.getInstance())) {
                divisor = 16;
            } else if (maxMemory < 96 * 1024) {
                divisor = 12;
            }
        } catch (Throwable ignored) {
            if (maxMemory < 96 * 1024) {
                divisor = 12;
            }
        }
        return Math.max(1024, maxMemory / divisor);
    }

    @Override
    protected int sizeOf(String key, Bitmap value) {
        return value.getRowBytes() * value.getHeight() / 1024;
    }

    @Override
    public Bitmap getBitmap(String url) {
        return get(url);
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        put(url, bitmap);
    }
}