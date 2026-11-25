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
        Context app = context.getApplicationContext();
        DisplayImageOptions defaultOpts = new DisplayImageOptions.Builder()
                .bitmapConfig(Bitmap.Config.RGB_565)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .build();
        // Cap UIL memory cache — default can balloon on feed scroll and OOM low-end.
        int memMb = SafeBitmapDecode.memoryClassMb(app);
        int memoryCachePercent = SafeBitmapDecode.isLowRam(app) || memMb <= 64
                ? 8 : (memMb <= 128 ? 10 : 12);
        ImageLoaderConfiguration.Builder builder =
                new ImageLoaderConfiguration.Builder(app)
                        .defaultDisplayImageOptions(defaultOpts)
                        .memoryCacheSizePercentage(memoryCachePercent)
                        .diskCacheSize(24 * 1024 * 1024)
                        .threadPoolSize(SafeBitmapDecode.isLowRam(app) ? 2 : 3);
        if (BuildConfig.DEBUG) {
            builder.imageDownloader(new DebugSslImageDownloader(app));
        }
        loader.init(builder.build());
    }
}
