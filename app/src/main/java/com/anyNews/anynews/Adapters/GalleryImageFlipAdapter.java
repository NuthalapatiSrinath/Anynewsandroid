package com.anyNews.anynews.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.anyNews.anynews.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * Image pages for Special-tab nested horizontal {@link com.anyNews.anynews.flipview.FlipView}
 * (book page-turn instead of ViewPager slide).
 */
public final class GalleryImageFlipAdapter extends BaseAdapter {

    private final Context context;
    private final ArrayList<String> images = new ArrayList<>();
    private final boolean useContainFit;
    private final ImageLoader imageLoader;
    private final DisplayImageOptions options;

    public GalleryImageFlipAdapter(
            @NonNull Context context,
            @Nullable List<String> urls,
            boolean useContainFit) {
        this.context = context.getApplicationContext() != null
                ? context.getApplicationContext()
                : context;
        this.useContainFit = useContainFit;
        if (urls != null) {
            images.addAll(urls);
        }
        if (images.isEmpty()) {
            images.add("");
        }
        imageLoader = ImageLoader.getInstance();
        try {
            if (!imageLoader.isInited()) {
                imageLoader.init(ImageLoaderConfiguration.createDefault(this.context));
            }
        } catch (Exception ignored) {
        }
        options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.app_splash_logo)
                .showImageOnLoading(R.drawable.trasparent_background)
                .showImageOnFail(R.drawable.app_splash_logo)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public Object getItem(int position) {
        return images.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView instanceof ImageView) {
            imageView = (ImageView) convertView;
        } else {
            imageView = new ImageView(parent.getContext());
            imageView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
        }
        imageView.setScaleType(
                useContainFit ? ImageView.ScaleType.FIT_CENTER : ImageView.ScaleType.FIT_XY);
        if (useContainFit) {
            imageView.setBackgroundColor(Color.BLACK);
        }
        String url = images.get(position);
        try {
            imageLoader.displayImage(url != null ? url : "", imageView, options, null);
        } catch (Exception e) {
            imageView.setImageResource(R.drawable.app_splash_logo);
        }
        return imageView;
    }
}
