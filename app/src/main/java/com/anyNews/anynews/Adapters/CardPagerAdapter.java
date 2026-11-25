package com.anyNews.anynews.Adapters;



import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.anyNews.anynews.BuildConfig;
import com.anyNews.anynews.Pojo.CardItem;
import com.anyNews.anynews.Pojo.EncreptionRequest;
import com.anyNews.anynews.Pojo.LikeDownloadRespouse;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.CardAdapter;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.SafeBitmapDecode;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.anyNews.anynews.Services.CardAdapter.MAX_ELEVATION_FACTOR;
import static com.anyNews.anynews.Utils.AppHelper.showLog;
import static com.anyNews.anynews.Utils.MyUtils.decrypt;
import static com.facebook.FacebookSdk.getApplicationContext;

public class CardPagerAdapter extends PagerAdapter implements CardAdapter {

    private List<CardView> mViews;
    private List<CardItem> mData;
    private float mBaseElevation;
    Context context;
    CountDownTimer countDownTimer;
    String strik_name;
    static shareMagazine shareMagazine;
    ImageView imageView1;
    /** Inflated cover page root — used to re-kick Glide if first bind was cancelled. */
    @Nullable
    private View page0Root;
    int LanguageId= SharedPrefsData.getInt(getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME);
    public CardPagerAdapter(Context context,String strik_name,shareMagazine shareMagazine) {
        this.context=context;
        this.strik_name =strik_name;
        mData = new ArrayList<>();
        mViews = new ArrayList<>();
        this.shareMagazine=shareMagazine;
    }

    public void addCardItem(CardItem item) {
        mViews.add(null);
        mData.add(item);
    }

    public float getBaseElevation() {
        return mBaseElevation;
    }

    @Override
    public CardView getCardViewAt(int position) {
        if (mViews == null || position < 0 || position >= mViews.size()) {
            android.util.Log.e("MagazineCrash", "getCardViewAt OOB pos=" + position
                    + " size=" + (mViews == null ? -1 : mViews.size()));
            return null;
        }
        return mViews.get(position);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        if (mData == null || position < 0 || position >= mData.size()) {
            android.util.Log.e("MagazineCrash", "instantiateItem bad pos=" + position
                    + " dataSize=" + (mData == null ? -1 : mData.size()));
            View empty = new View(container.getContext());
            container.addView(empty);
            return empty;
        }
        View view = LayoutInflater.from(container.getContext())
                .inflate(R.layout.adapter, container, false);
        container.addView(view);

        bind(mData.get(position), view, position);
        CardView cardView = (CardView) view.findViewById(R.id.cardView);
        ImageView images=(ImageView)view.findViewById(R.id.images);
        if (position == 0) {
            page0Root = view;
        }
        imageView1=(ImageView)view.findViewById(R.id.imageView1);
        imageView1.setVisibility(View.GONE);
        TextView strip_text_name=(TextView)view.findViewById(R.id.strip_text_name);
        LinearLayout ic_magzine_stirp=(LinearLayout) view.findViewById(R.id.with_strip_layout);

        if (mBaseElevation == 0) {
            mBaseElevation = cardView.getCardElevation();
        }


        imageView1.setImageResource(R.drawable.ic_share_black_24dp);
        images.setEnabled(false);


        if(LanguageId==4){

            strip_text_name.setTextSize(10);
            strip_text_name.setText(""+strik_name);

        }else if(LanguageId==1) {

            strip_text_name.setTextSize(12);
            strip_text_name.setText(""+strik_name);

        }else {

            strip_text_name.setTextSize(11);
            strip_text_name.setText(""+strik_name);


        }

        // check null
        if(strip_text_name.getText().toString().matches("")){
            strip_text_name.setText("" +" The Best Regional News App ");
        }

        cardView.setFocusableInTouchMode(false);

        cardView.setMaxCardElevation(mBaseElevation * MAX_ELEVATION_FACTOR);
        if (position >= 0 && position < mViews.size()) {
            mViews.set(position, cardView);
        } else {
            android.util.Log.e("MagazineCrash", "instantiateItem mViews OOB pos=" + position
                    + " size=" + mViews.size());
        }
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        try {
            if (object instanceof View) {
                container.removeView((View) object);
            }
        } catch (Exception e) {
            android.util.Log.e("MagazineCrash", "destroyItem remove failed: " + e.getMessage());
        }
        if (mViews != null && position >= 0 && position < mViews.size()) {
            mViews.set(position, null);
        }
    }

    /** True after Glide finished for magazine cover (not the transparent placeholder). */
    public boolean isPage0Loaded() {
        if (page0Root == null) {
            return false;
        }
        ImageView images = page0Root.findViewById(R.id.images);
        if (images == null) {
            return false;
        }
        Object loaded = images.getTag(R.id.tag_gallery_image_loaded);
        return Boolean.TRUE.equals(loaded) && images.getDrawable() != null;
    }

    /**
     * Re-kick cover Glide after ViewPager layout — first instantiate often races with
     * neighbor loads / scrap and leaves page-0 on the transparent stub while pages 1+
     * already sit in memory (same class of bug as Special FlipView page-0).
     */
    public void ensurePage0Bound() {
        if (page0Root == null || mData == null || mData.isEmpty()) {
            return;
        }
        ImageView images = page0Root.findViewById(R.id.images);
        if (images == null) {
            return;
        }
        if (isPage0Loaded()) {
            images.invalidate();
            return;
        }
        images.setTag(R.id.tag_gallery_image_loaded, null);
        images.setTag(R.id.tag_gallery_image_url, null);
        bind(mData.get(0), page0Root, 0);
    }

    /**
     * Schedule cover re-bind after magazine ViewPager is attached/laid out.
     * Retries until {@link #isPage0Loaded()} or attempts are exhausted.
     */
    public static void schedulePage0Ensure(
            @Nullable ViewPager pager, @Nullable CardPagerAdapter adapter) {
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
                adapter.ensurePage0Bound();
            } catch (Exception e) {
                android.util.Log.e("MagazineCrash", "CardPager ensurePage0: " + e.getMessage());
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

    private void Share_Count_Value(String share,long post_id){

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("draft_id", SharedPrefsData.getInt(context, Constants.DraftID,Constants.PREF_NAME));
        jsonObject.addProperty("post_id", post_id);
        jsonObject.addProperty("property", share);

        showLog("API REQ   "+jsonObject.toString());
        APIService apiService = RestApiClient.getClient().create(APIService.class);
        Call<EncreptionRequest> apiResponse = apiService.update_post_count( "v3",jsonObject);
        apiResponse.enqueue(new Callback<EncreptionRequest>() {
            @Override

            public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {

                EncreptionRequest encreptionRequest=response.body();
                showLog("API RES   "+response.body().getData());
                String encrpt_data = null;
                /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    encrpt_data = decrypt(encreptionRequest.getData());
                }*/
                encrpt_data = decrypt(encreptionRequest.getData());

                Gson gsonObj = new Gson();
                LikeDownloadRespouse likeDownloadRespouse  = gsonObj.fromJson(encrpt_data, LikeDownloadRespouse.class);




                if(likeDownloadRespouse.getSuccess()){


                }



            }

            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {

                Toast.makeText(context, t.getMessage(), Toast.LENGTH_LONG).show();

            }
        });

    }
    private void bind(CardItem item, View view, int position) {
        try {
            ImageView images = (ImageView) view.findViewById(R.id.images);
            String url = item != null ? item.getText() : null;
            if (url == null) {
                url = "";
            }
            url = url.trim();
            // Only skip when Glide finished — transparent placeholder is a drawable and
            // used to permanently skip after a cancelled first page-0 load.
            Object prior = images.getTag(R.id.tag_gallery_image_url);
            Object loaded = images.getTag(R.id.tag_gallery_image_loaded);
            if (url.equals(prior)
                    && Boolean.TRUE.equals(loaded)
                    && images.getDrawable() != null
                    && !url.isEmpty()) {
                return;
            }
            images.setTag(R.id.tag_gallery_image_url, url);
            images.setTag(R.id.tag_gallery_image_loaded, null);
            // Page 0 is on-screen first; neighbors also instantiate (offscreen=1) at the
            // same time as a background Glide load — without IMMEDIATE, cover can lose
            // the race to page 1 and look empty until leave/return hits cache.
            com.bumptech.glide.Priority priority = position == 0
                    ? com.bumptech.glide.Priority.IMMEDIATE
                    : com.bumptech.glide.Priority.NORMAL;
            if (url.isEmpty()) {
                // Never paint splash orange-N as a magazine page.
                images.setImageResource(R.drawable.trasparent_background);
                images.setTag(R.id.tag_gallery_image_loaded, Boolean.TRUE);
                return;
            }
            final ImageView targetView = images;
            final String loadUrl = url;
            int ow = images.getWidth();
            int oh = images.getHeight();
            if (ow <= 0 || oh <= 0) {
                try {
                    android.util.DisplayMetrics dm = context.getResources().getDisplayMetrics();
                    ow = Math.max(1, dm.widthPixels);
                    oh = Math.max(1, dm.heightPixels);
                } catch (Exception ignored) {
                    ow = 1080;
                    oh = 1920;
                }
            }
            int[] capped = SafeBitmapDecode.clampFlipOverride(context, ow, oh);
            Glide.with(context)
                    .load(url)
                    .priority(priority)
                    .dontAnimate()
                    .format(DecodeFormat.PREFER_RGB_565)
                    .override(capped[0], capped[1])
                    .placeholder(R.drawable.trasparent_background)
                    .error(R.drawable.trasparent_background)
                    .fallback(R.drawable.trasparent_background)
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
                            ViewGroup p = (ViewGroup) targetView.getParent();
                            while (p != null) {
                                p.postInvalidate();
                                if (p.getParent() instanceof ViewGroup) {
                                    p = (ViewGroup) p.getParent();
                                } else {
                                    break;
                                }
                            }
                            return false;
                        }
                    })
                    .into(images);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }
    public interface shareMagazine{
        void onClickShareMagzine(CardView cardView,ImageView imageView1);
    }

}
