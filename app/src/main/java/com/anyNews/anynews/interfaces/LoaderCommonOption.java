package com.anyNews.anynews.interfaces;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.ImageView;

import com.anyNews.anynews.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;



public interface LoaderCommonOption {
    /** Theme-aware placeholder; resolved via color resource at load time. */
    int IMAGE_LOADING_PLACEHOLDER = R.color.image_placeholder;
    ColorDrawable d1 = new ColorDrawable(Color.parseColor("#0baedc"));
    ColorDrawable d2 = new ColorDrawable(Color.parseColor("#f77a52"));
    ColorDrawable d3 = new ColorDrawable(Color.parseColor("#604b9f"));
    ColorDrawable d4 = new ColorDrawable(Color.parseColor("#db404d"));
    ColorDrawable d5 = new ColorDrawable(Color.parseColor("#27ad60"));
    ColorDrawable d6 = new ColorDrawable(Color.parseColor("#d8d8d8"));
    /** Shown when feed/sticky article images fail (dead S3 URLs, network, etc.). */
    int IMAGE_FAIL_PLACEHOLDER = R.drawable.app_splash_logo;
    DisplayImageOptions options = new DisplayImageOptions.Builder()
            .showImageOnLoading(IMAGE_LOADING_PLACEHOLDER)
            .showImageOnFail(IMAGE_FAIL_PLACEHOLDER)
            .showImageForEmptyUri(IMAGE_FAIL_PLACEHOLDER)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            // Must clear scrap FlipView heroes before the next URL paints — otherwise a
            // prior birthday poster can stick on an unrelated rain/news card mid-bind.
            .resetViewBeforeLoading()
            .imageScaleType(ImageScaleType.EXACTLY)
            .build();
    DisplayImageOptions optionsAds = new DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .resetViewBeforeLoading()
            .imageScaleType(ImageScaleType.EXACTLY)
            .build();

    DisplayImageOptions options_audio = new DisplayImageOptions.Builder()
            .displayer(new RoundedBitmapDisplayer(10)) //rounded corner bitmap
            .cacheInMemory(true)
            .cacheOnDisc(true)
            .build();

    DisplayImageOptions options1 = new DisplayImageOptions.Builder()
            .showImageOnLoading(d1)
            .showImageOnFail(d1)
            .cacheInMemory(false)
            .cacheOnDisk(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .resetViewBeforeLoading()
//            .imageScaleType(ImageScaleType.)
            .build();

    DisplayImageOptions options2 = new DisplayImageOptions.Builder()
            .showImageOnLoading(d2)
            .showImageOnFail(d2)
            .cacheInMemory(false)
            .cacheOnDisk(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .resetViewBeforeLoading()
//            .imageScaleType(ImageScaleType.)
            .build();
    DisplayImageOptions options3 = new DisplayImageOptions.Builder()
            .showImageOnLoading(d3)
            .showImageOnFail(d3)
            .cacheInMemory(false)
            .cacheOnDisk(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .resetViewBeforeLoading()
//            .imageScaleType(ImageScaleType.)
            .build();

    DisplayImageOptions options4 = new DisplayImageOptions.Builder()
            .showImageOnLoading(d4)
            .showImageOnFail(d4)
            .cacheInMemory(false)
            .cacheOnDisk(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .resetViewBeforeLoading()
//            .imageScaleType(ImageScaleType.)
            .build();

    DisplayImageOptions options5 = new DisplayImageOptions.Builder()
            .showImageOnLoading(d5)
            .showImageOnFail(d5)
            .cacheInMemory(false)
            .cacheOnDisk(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .resetViewBeforeLoading()
//            .imageScaleType(ImageScaleType.)
            .build();

    /*DisplayImageOptions splash = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.hands)
            .showImageOnFail(R.drawable.hands)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .imageScaleType(ImageScaleType.NONE)
            .resetViewBeforeLoading()

            .build();*/

    DisplayImageOptions optionsImpressions = new DisplayImageOptions.Builder()
            .cacheInMemory(false)
            .cacheOnDisk(false)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .resetViewBeforeLoading()
            .imageScaleType(ImageScaleType.EXACTLY)
            .build();


    // Image Loader
    ImageLoader imageLoader = ImageLoader.getInstance();

    AnimateFirstDisplayListener animationListener = new AnimateFirstDisplayListener();

    static class AnimateFirstDisplayListener extends SimpleImageLoadingListener {

        static final List<String> displayedImages = Collections
                .synchronizedList(new LinkedList<String>());

        @Override
        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
            super.onLoadingFailed(imageUri, view, failReason);
            if (view instanceof ImageView) {
                ((ImageView) view).setImageResource(IMAGE_FAIL_PLACEHOLDER);
            }
        }

        @Override
        public void onLoadingComplete(String imageUri, View view,
                                      Bitmap loadedImage) {
            if (loadedImage != null) {
//                ImageView imageView = (ImageView) view;
//                boolean firstDisplay = !displayedImages.contains(imageUri);
//                if (firstDisplay) {
//                    FadeInBitmapDisplayer.animate(imageView, 500);
//                    displayedImages.add(imageUri);
//                }

                    if (loadedImage != null) {
                        ImageView imageView = (ImageView) view;
                        boolean firstDisplay = !displayedImages.contains(imageUri);
                        if (firstDisplay) {
                            FadeInBitmapDisplayer.animate(imageView, 500);
                            displayedImages.add(imageUri);
                        }
                    }

            }
        }
    }

}
