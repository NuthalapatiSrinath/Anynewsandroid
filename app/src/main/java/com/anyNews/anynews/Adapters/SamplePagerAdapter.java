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
    public SamplePagerAdapter(Context context ,ArrayList<String> arrayList,int height,boolean value) {
        mSize = arrayList.size();
        this.arrayList=arrayList;
        this.context=context;
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(ImageLoaderConfiguration.createDefault(getApplicationContext()));
        this.fromHalfGallery=value;
        this.layoutHeight=height;


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
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        if(fromHalfGallery){
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, layoutHeight);
            imageView.setLayoutParams(params);
         imageView.getLayoutParams().height=layoutHeight;
        }


        try {


            //Picasso.with(context).load(arrayList.get(position).toString()).skipMemoryCache().into(textView);
            options = new DisplayImageOptions.Builder()
                    // below image will be displayed when the image url is empty
                    .showImageForEmptyUri(R.drawable.app_splash_logo)
                    // stub image will display when your image is loading
                    .showImageOnLoading(R.drawable.trasparent_background)
                    .showImageOnFail(R.drawable.app_splash_logo)
                    // cachememory method will caches the image in users external storage
                    .cacheInMemory(true)
                    // cache on disc will caches the image in users internal storage
                    .cacheOnDisk(true)
                    // build will build the view for displaying image..
                    .build();

            // below method will display image inside our image view..
            imageLoader.displayImage(arrayList.get(position), imageView, options, null);
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
