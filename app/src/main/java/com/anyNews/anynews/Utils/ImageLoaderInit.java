package com.anyNews.anynews.Utils;

import android.content.Context;
import android.graphics.Bitmap;

import com.anyNews.anynews.BuildConfig;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

public final class ImageLoaderInit {
    private ImageLoaderInit() { }

    public static void ensureInitialized(Context context) {
        ImageLoader loader = ImageLoader.getInstance();
        if (loader.isInited()) {
            return;
        }
        DisplayImageOptions defaultOpts = new DisplayImageOptions.Builder()
                .bitmapConfig(Bitmap.Config.RGB_565)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .build();
        ImageLoaderConfiguration config;
        if (BuildConfig.DEBUG) {
            config = new ImageLoaderConfiguration.Builder(context.getApplicationContext())
                    .defaultDisplayImageOptions(defaultOpts)
                    .imageDownloader(new DebugSslImageDownloader(context.getApplicationContext()))
                    .build();
        } else {
            config = new ImageLoaderConfiguration.Builder(context.getApplicationContext())
                    .defaultDisplayImageOptions(defaultOpts)
                    .build();
        }
        loader.init(config);
    }
}
