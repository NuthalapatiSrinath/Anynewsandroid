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
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Image pages for Special-tab nested horizontal {@link com.anyNews.anynews.flipview.FlipView}
 * (book page-turn instead of ViewPager slide).
 *
 * <p>Page 0 uses Glide {@link Priority#IMMEDIATE} and a sticky ImageView so FlipView
 * recycling / neighbor loads cannot cancel the cover request (orange splash logo flash).
 */
public final class GalleryImageFlipAdapter extends BaseAdapter {

    private final Context context;
    private final ArrayList<String> images = new ArrayList<>();
    private final boolean useContainFit;
    /** Sticky cover view — never recycled into another page index. */
    @Nullable
    private ImageView page0View;

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
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public Object getItem(int position) {
        if (position < 0 || position >= images.size()) {
            return "";
        }
        return images.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (position == 0) {
            if (page0View == null) {
                page0View = newImageView(parent);
            }
            imageView = page0View;
        } else if (convertView instanceof ImageView
                && convertView != page0View) {
            imageView = (ImageView) convertView;
        } else {
            imageView = newImageView(parent);
        }
        imageView.setScaleType(
                useContainFit ? ImageView.ScaleType.FIT_CENTER : ImageView.ScaleType.FIT_XY);
        if (useContainFit) {
            imageView.setBackgroundColor(Color.BLACK);
        } else {
            imageView.setBackgroundColor(Color.TRANSPARENT);
        }

        String url = "";
        if (position >= 0 && position < images.size()) {
            url = images.get(position);
        }
        if (url == null) {
            url = "";
        }

        Priority priority = position == 0 ? Priority.IMMEDIATE : Priority.NORMAL;
        RequestOptions opts = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(priority)
                // Transparent while loading — never paint splash orange-N as the poster.
                .placeholder(R.drawable.trasparent_background)
                .error(R.drawable.trasparent_background)
                .fallback(R.drawable.trasparent_background);

        try {
            if (url.isEmpty()) {
                imageView.setImageResource(R.drawable.trasparent_background);
            } else {
                Glide.with(context)
                        .load(url)
                        .apply(opts)
                        .into(imageView);
            }
        } catch (Exception e) {
            imageView.setImageResource(R.drawable.trasparent_background);
        }
        return imageView;
    }

    @NonNull
    private static ImageView newImageView(@NonNull ViewGroup parent) {
        ImageView imageView = new ImageView(parent.getContext());
        imageView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        return imageView;
    }
}
