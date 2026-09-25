package com.anyNews.anynews.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.anyNews.anynews.R;
import com.anyNews.anynews.Utils.GalleryImageUrls;
import com.anyNews.anynews.flipview.FlipView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;
import java.util.List;

/**
 * Image pages for Special-tab nested horizontal {@link com.anyNews.anynews.flipview.FlipView}
 * (book page-turn instead of ViewPager slide).
 *
 * <p>Page 0 uses Glide {@link Priority#IMMEDIATE} and a sticky ImageView so FlipView
 * recycling / neighbor loads cannot cancel the cover request (orange splash logo flash).
 *
 * <p>FlipView draws children in {@code dispatchDraw}; after Glide completes we must
 * invalidate the FlipView or a cancelled first paint stays blank until leave/return.
 */
public final class GalleryImageFlipAdapter extends BaseAdapter {

    private static final int TAG_URL = R.id.tag_gallery_image_url;
    private static final int TAG_LOADED = R.id.tag_gallery_image_loaded;

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
        this.context = context;
        this.useContainFit = useContainFit;
        images.addAll(GalleryImageUrls.sanitize(urls));
        if (images.isEmpty()) {
            // Keep FlipView non-empty; transparent — never splash logo as a fake page.
            images.add("");
        }
    }

    /** Stable key for skip-rebind (same poster set already on the nested FlipView). */
    @NonNull
    public String urlFingerprint() {
        return GalleryImageUrls.fingerprint(images);
    }

    /** True after Glide {@code onResourceReady} for the cover (not the transparent stub). */
    public boolean isPage0Loaded() {
        if (page0View == null) {
            return false;
        }
        Object loaded = page0View.getTag(TAG_LOADED);
        return Boolean.TRUE.equals(loaded) && page0View.getDrawable() != null;
    }

    /**
     * Re-kick page-0 Glide once the nested FlipView has a real size (first open often
     * bound at 0×0, then Glide wait-for-layout was cancelled by FlipView scrap churn).
     */
    public void ensurePage0Bound(@Nullable ViewGroup flipHost) {
        if (images.isEmpty()) {
            return;
        }
        ViewGroup parent = flipHost;
        if (parent == null && page0View != null && page0View.getParent() instanceof ViewGroup) {
            parent = (ViewGroup) page0View.getParent();
        }
        if (parent == null) {
            return;
        }
        if (page0View != null) {
            if (isPage0Loaded()) {
                invalidateFlipAncestors(page0View);
                return;
            }
            // Clear sticky skip so getView reloads.
            page0View.setTag(TAG_LOADED, null);
            page0View.setTag(TAG_URL, null);
        }
        getView(0, page0View, parent);
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

        // Only skip when Glide actually finished — transparent placeholder counts as
        // drawable != null and used to permanently skip after a cancelled first load.
        Object prior = imageView.getTag(TAG_URL);
        Object loaded = imageView.getTag(TAG_LOADED);
        if (url.equals(prior)
                && Boolean.TRUE.equals(loaded)
                && imageView.getDrawable() != null) {
            return imageView;
        }

        Priority priority = position == 0 ? Priority.IMMEDIATE : Priority.NORMAL;
        int[] size = resolveLoadSize(imageView, parent);
        RequestOptions opts = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(priority)
                .dontAnimate()
                .override(size[0], size[1])
                // Transparent while loading — never paint splash orange-N as the poster.
                .placeholder(R.drawable.trasparent_background)
                .error(R.drawable.trasparent_background)
                .fallback(R.drawable.trasparent_background);

        try {
            if (url.isEmpty()) {
                imageView.setImageResource(R.drawable.trasparent_background);
                imageView.setTag(TAG_URL, "");
                imageView.setTag(TAG_LOADED, Boolean.TRUE);
            } else {
                imageView.setTag(TAG_URL, url);
                imageView.setTag(TAG_LOADED, null);
                final ImageView targetView = imageView;
                final String loadUrl = url;
                Glide.with(glideContext(parent))
                        .load(url)
                        .apply(opts)
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(
                                    @Nullable GlideException e,
                                    Object model,
                                    Target<Drawable> target,
                                    boolean isFirstResource) {
                                if (loadUrl.equals(targetView.getTag(TAG_URL))) {
                                    targetView.setTag(TAG_LOADED, null);
                                }
                                invalidateFlipAncestors(targetView);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(
                                    Drawable resource,
                                    Object model,
                                    Target<Drawable> target,
                                    DataSource dataSource,
                                    boolean isFirstResource) {
                                if (loadUrl.equals(targetView.getTag(TAG_URL))) {
                                    targetView.setTag(TAG_LOADED, Boolean.TRUE);
                                }
                                // FlipView custom dispatchDraw — force redraw now.
                                invalidateFlipAncestors(targetView);
                                return false;
                            }
                        })
                        .into(imageView);
            }
        } catch (Exception e) {
            imageView.setImageResource(R.drawable.trasparent_background);
            imageView.setTag(TAG_URL, url);
            imageView.setTag(TAG_LOADED, null);
        }
        return imageView;
    }

    @NonNull
    private Context glideContext(@Nullable ViewGroup parent) {
        if (parent != null && parent.getContext() != null) {
            return parent.getContext();
        }
        return context;
    }

    @NonNull
    private static int[] resolveLoadSize(@NonNull ImageView imageView, @Nullable ViewGroup parent) {
        int w = imageView.getWidth();
        int h = imageView.getHeight();
        if (w <= 0 || h <= 0) {
            if (parent != null) {
                w = parent.getWidth();
                h = parent.getHeight();
            }
        }
        if (w <= 0 || h <= 0) {
            try {
                DisplayMetrics dm = imageView.getResources().getDisplayMetrics();
                w = Math.max(1, dm.widthPixels);
                h = Math.max(1, dm.heightPixels);
            } catch (Exception e) {
                w = 1080;
                h = 1920;
            }
        }
        return new int[]{w, h};
    }

    static void invalidateFlipAncestors(@Nullable View start) {
        if (start == null) {
            return;
        }
        try {
            start.invalidate();
        } catch (Exception ignored) {
        }
        ViewParent parent = start.getParent();
        while (parent instanceof View) {
            View v = (View) parent;
            try {
                v.invalidate();
            } catch (Exception ignored) {
            }
            if (parent instanceof FlipView) {
                break;
            }
            parent = parent.getParent();
        }
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
