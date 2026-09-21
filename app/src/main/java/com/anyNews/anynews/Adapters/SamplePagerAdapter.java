package com.anyNews.anynews.Adapters;


import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.viewpager.widget.PagerAdapter;

import android.net.Uri;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
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
            } else if (position == 0) {
                // Cover wins the Glide pool over neighbor UIL loads.
                try {
                    Glide.with(context)
                            .load(pageUrl)
                            .priority(com.bumptech.glide.Priority.IMMEDIATE)
                            .dontAnimate()
                            .placeholder(R.drawable.trasparent_background)
                            .error(R.drawable.trasparent_background)
                            .into(imageView);
                } catch (Exception glideEx) {
                    imageLoader.displayImage(pageUrl, imageView, options, null);
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
