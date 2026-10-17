package com.anyNews.anynews.Adapters;


import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.anyNews.anynews.R;
import com.bumptech.glide.Glide;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


import static com.facebook.FacebookSdk.getApplicationContext;

public class SamplePagerAdapter extends PagerAdapter {

    private final Random random = new Random();
    private int mSize;
    DisplayMetrics displayMetrics = new DisplayMetrics();
    DisplayImageOptions options;
    ImageLoader imageLoader;

    ArrayList<String>arrayList;
    Context context;
    boolean fromHalfGallery;
    int layoutHeight=0;
    /** When true, use FIT_CENTER (Upload Original); else FIT_XY. */
    boolean useContainFit = false;
    /** Cover ImageView — re-kick Glide if first open cancelled before paint. */
    @Nullable
    private ImageView page0View;
    public SamplePagerAdapter(Context context ,ArrayList<String> arrayList,int height,boolean value) {
        this(context, arrayList, height, value, false);
    }

    public SamplePagerAdapter(
            Context context,
            ArrayList<String> arrayList,
            int height,
            boolean value,
            boolean useContainFit) {
        mSize = arrayList.size();
        this.arrayList=arrayList;
        this.context=context;
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(ImageLoaderConfiguration.createDefault(getApplicationContext()));
        this.fromHalfGallery=value;
        this.layoutHeight=height;
        this.useContainFit = useContainFit;
    }

    public SamplePagerAdapter(int count) {
        mSize = count;
    }

    @Override public int getCount() {
        return mSize;
    }

    @Override public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup view, int position, @NonNull Object object) {
        view.removeView((View) object);
    }

    @NonNull @Override public Object instantiateItem(@NonNull ViewGroup view, int position) {
        ImageView imageView = new ImageView(view.getContext());
        imageView.setScaleType(
                useContainFit ? ImageView.ScaleType.FIT_CENTER : ImageView.ScaleType.FIT_XY);
        if (useContainFit) {
            imageView.setBackgroundColor(Color.BLACK);
        } else {
            imageView.setBackgroundColor(Color.TRANSPARENT);
        }
        if(fromHalfGallery){
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, layoutHeight);
            imageView.setLayoutParams(params);
         imageView.getLayoutParams().height=layoutHeight;
        }


        try {


            // Transparent stubs — never FIT_XY the orange splash logo as a fake
            // first magazine/gallery page (Play Store regression after animations).
            options = new DisplayImageOptions.Builder()
                    .showImageForEmptyUri(R.drawable.trasparent_background)
                    .showImageOnLoading(R.drawable.trasparent_background)
                    .showImageOnFail(R.drawable.trasparent_background)
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .build();

            String pageUrl = "";
            if (arrayList != null && position >= 0 && position < arrayList.size()
                    && arrayList.get(position) != null) {
                pageUrl = arrayList.get(position).trim();
            }
            if (pageUrl.isEmpty() || "null".equalsIgnoreCase(pageUrl)) {
                imageView.setImageResource(R.drawable.trasparent_background);
                if (position == 0) {
                    page0View = imageView;
                    imageView.setTag(R.id.tag_gallery_image_url, "");
                    imageView.setTag(R.id.tag_gallery_image_loaded, Boolean.TRUE);
                }
            } else if (position == 0) {
                // Cover wins the Glide pool over neighbor UIL loads.
                page0View = imageView;
                try {
                    int ow = imageView.getWidth();
                    int oh = imageView.getHeight();
                    if (ow <= 0 || oh <= 0) {
                        try {
                            DisplayMetrics dm = context.getResources().getDisplayMetrics();
                            ow = Math.max(1, dm.widthPixels);
                            oh = Math.max(1, dm.heightPixels);
                        } catch (Exception ignored) {
                            ow = 1080;
                            oh = 1920;
                        }
                    }
                    final ImageView targetView = imageView;
                    final String loadUrl = pageUrl;
                    imageView.setTag(R.id.tag_gallery_image_url, loadUrl);
                    imageView.setTag(R.id.tag_gallery_image_loaded, null);
                    Glide.with(context)
                            .load(pageUrl)
                            .priority(com.bumptech.glide.Priority.IMMEDIATE)
                            .dontAnimate()
                            .override(ow, oh)
                            .placeholder(R.drawable.trasparent_background)
                            .error(R.drawable.trasparent_background)
                            .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                                @Override
                                public boolean onLoadFailed(
                                        @androidx.annotation.Nullable com.bumptech.glide.load.engine.GlideException e,
                                        Object model,
                                        com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target,
                                        boolean isFirstResource) {
                                    if (loadUrl.equals(targetView.getTag(R.id.tag_gallery_image_url))) {
                                        targetView.setTag(R.id.tag_gallery_image_loaded, null);
                                    }
                                    targetView.postInvalidate();
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(
                                        android.graphics.drawable.Drawable resource,
                                        Object model,
                                        com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target,
                                        com.bumptech.glide.load.DataSource dataSource,
                                        boolean isFirstResource) {
                                    if (loadUrl.equals(targetView.getTag(R.id.tag_gallery_image_url))) {
                                        targetView.setTag(R.id.tag_gallery_image_loaded, Boolean.TRUE);
                                    }
                                    targetView.postInvalidate();
                                    return false;
                                }
                            })
                            .into(imageView);
                } catch (Exception glideEx) {
                    imageLoader.displayImage(pageUrl, imageView, options, null);
                    imageView.setTag(R.id.tag_gallery_image_loaded, Boolean.TRUE);
                }
            } else {
                imageLoader.displayImage(pageUrl, imageView, options, null);
            }
            /*Glide
                    .with(textView.getContext())
                    .load(arrayList.get(position).toString())
                    .centerCrop()
                    .placeholder(R.drawable.progress_icon)
                    .into(textView);*/

            /*textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String msg =" ";
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_SUBJECT, "");
                    intent.putExtra(Intent.EXTRA_TEXT, msg);
                    Bitmap b = ScreenShott.getInstance().takeScreenShotOfView(textView);

                    try{

                        File file = new File(context.getExternalCacheDir(),"sample.png");
                        FileOutputStream fOut = new FileOutputStream(file);
                        b.compress(Bitmap.CompressFormat.PNG,100,fOut);
                        fOut.flush();
                        fOut.close();
                        file.setReadable(true,true);
                        Uri photoURI = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
                        intent.putExtra(Intent.EXTRA_STREAM, photoURI);
                        List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(Intent.createChooser(intent,"Share Link" ), PackageManager.MATCH_DEFAULT_ONLY);
                        for (ResolveInfo resolveInfo : resInfoList) {
                            String packageName = resolveInfo.activityInfo.packageName;
                            context.grantUriPermission(packageName, photoURI, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        }
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                    intent.setType("image/*");
                    context.startActivity(Intent.createChooser(intent,"Share Link" ));
                }
            });*/

        }catch (IndexOutOfBoundsException e){
            e.printStackTrace();
        }



        view.addView(imageView, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);


        return imageView;
    }

    public boolean isPage0Loaded() {
        if (page0View == null) {
            return false;
        }
        Object loaded = page0View.getTag(R.id.tag_gallery_image_loaded);
        return Boolean.TRUE.equals(loaded) && page0View.getDrawable() != null;
    }

    /** Re-fire cover Glide if the first open left only the transparent stub. */
    public void ensurePage0Bound(@Nullable ViewGroup pager) {
        if (arrayList == null || arrayList.isEmpty() || page0View == null) {
            return;
        }
        if (isPage0Loaded()) {
            page0View.invalidate();
            return;
        }
        String pageUrl = arrayList.get(0);
        if (pageUrl == null) {
            pageUrl = "";
        }
        pageUrl = pageUrl.trim();
        if (pageUrl.isEmpty() || "null".equalsIgnoreCase(pageUrl)) {
            page0View.setImageResource(R.drawable.trasparent_background);
            page0View.setTag(R.id.tag_gallery_image_url, "");
            page0View.setTag(R.id.tag_gallery_image_loaded, Boolean.TRUE);
            return;
        }
        page0View.setTag(R.id.tag_gallery_image_url, null);
        page0View.setTag(R.id.tag_gallery_image_loaded, null);
        ViewGroup parent = pager;
        if (parent == null && page0View.getParent() instanceof ViewGroup) {
            parent = (ViewGroup) page0View.getParent();
        }
        if (parent == null) {
            return;
        }
        // Re-run position-0 bind path via a fresh Glide call (same as instantiateItem).
        try {
            int ow = page0View.getWidth();
            int oh = page0View.getHeight();
            if (ow <= 0 || oh <= 0) {
                ow = parent.getWidth() > 0 ? parent.getWidth() : 1080;
                oh = parent.getHeight() > 0 ? parent.getHeight() : 1920;
            }
            final ImageView targetView = page0View;
            final String loadUrl = pageUrl;
            page0View.setTag(R.id.tag_gallery_image_url, loadUrl);
            Glide.with(context)
                    .load(pageUrl)
                    .priority(com.bumptech.glide.Priority.IMMEDIATE)
                    .dontAnimate()
                    .override(Math.max(1, ow), Math.max(1, oh))
                    .placeholder(R.drawable.trasparent_background)
                    .error(R.drawable.trasparent_background)
                    .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                        @Override
                        public boolean onLoadFailed(
                                @Nullable com.bumptech.glide.load.engine.GlideException e,
                                Object model,
                                com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target,
                                boolean isFirstResource) {
                            if (loadUrl.equals(targetView.getTag(R.id.tag_gallery_image_url))) {
                                targetView.setTag(R.id.tag_gallery_image_loaded, null);
                            }
                            targetView.postInvalidate();
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(
                                android.graphics.drawable.Drawable resource,
                                Object model,
                                com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target,
                                com.bumptech.glide.load.DataSource dataSource,
                                boolean isFirstResource) {
                            if (loadUrl.equals(targetView.getTag(R.id.tag_gallery_image_url))) {
                                targetView.setTag(R.id.tag_gallery_image_loaded, Boolean.TRUE);
                            }
                            targetView.postInvalidate();
                            return false;
                        }
                    })
                    .into(page0View);
        } catch (Exception e) {
            android.util.Log.e("MagazineCrash", "SamplePager ensurePage0: " + e.getMessage());
        }
    }

    public static void schedulePage0Ensure(
            @Nullable ViewPager pager, @Nullable SamplePagerAdapter adapter) {
        if (pager == null || adapter == null) {
            return;
        }
        final Handler handler = new Handler(Looper.getMainLooper());
        final int[] attempts = {0};
        final int maxAttempts = 6;
        final Runnable[] ensureHolder = new Runnable[1];
        ensureHolder[0] = () -> {
            if (adapter.isPage0Loaded()) {
                return;
            }
            if (pager.getWindowToken() == null) {
                if (attempts[0] < maxAttempts) {
                    attempts[0]++;
                    handler.postDelayed(ensureHolder[0], 50L);
                }
                return;
            }
            try {
                adapter.ensurePage0Bound(pager);
            } catch (Exception e) {
                android.util.Log.e("MagazineCrash", "SamplePager schedulePage0: " + e.getMessage());
            }
            if (!adapter.isPage0Loaded() && attempts[0] < maxAttempts) {
                attempts[0]++;
                long delay = attempts[0] <= 2 ? 50L : (attempts[0] <= 4 ? 200L : 400L);
                handler.postDelayed(ensureHolder[0], delay);
            }
        };
        final Runnable ensure = ensureHolder[0];
        if (pager.getWidth() > 0 && pager.getHeight() > 0 && pager.getChildCount() > 0) {
            pager.post(ensure);
            return;
        }
        final ViewTreeObserver.OnGlobalLayoutListener[] layoutListener =
                new ViewTreeObserver.OnGlobalLayoutListener[1];
        layoutListener[0] = () -> {
            if (pager.getWidth() <= 0 || pager.getHeight() <= 0) {
                return;
            }
            try {
                pager.getViewTreeObserver().removeOnGlobalLayoutListener(layoutListener[0]);
            } catch (Exception ignored) {
            }
            ensure.run();
        };
        try {
            pager.getViewTreeObserver().addOnGlobalLayoutListener(layoutListener[0]);
        } catch (Exception e) {
            handler.postDelayed(ensure, 100L);
            return;
        }
        handler.postDelayed(() -> {
            try {
                pager.getViewTreeObserver().removeOnGlobalLayoutListener(layoutListener[0]);
            } catch (Exception ignored) {
            }
            if (!adapter.isPage0Loaded()) {
                ensure.run();
            }
        }, 300L);
    }

    public void addItem() {
        mSize++;
        notifyDataSetChanged();
    }

    public void removeItem() {
        mSize--;
        mSize = mSize < 0 ? 0 : mSize;

        notifyDataSetChanged();
    }
}
