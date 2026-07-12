package com.anyNews.anynews.Utils;

import android.content.Context;

import com.anyNews.anynews.BuildConfig;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public final class ImageLoaderInit {
    private ImageLoaderInit() { }

    public static void ensureInitialized(Context context) {
        ImageLoader loader = ImageLoader.getInstance();
        if (loader.isInited()) {
            return;
        }
        ImageLoaderConfiguration config;
        if (BuildConfig.DEBUG) {
            config = new ImageLoaderConfiguration.Builder(context.getApplicationContext())
                    .imageDownloader(new DebugSslImageDownloader(context.getApplicationContext()))
                    .build();
        } else {
            config = ImageLoaderConfiguration.createDefault(context.getApplicationContext());
        }
        loader.init(config);
    }
}
