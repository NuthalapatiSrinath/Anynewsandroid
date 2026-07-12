package com.anyNews.anynews.Adapters;



import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;
import androidx.viewpager.widget.PagerAdapter;

import com.anyNews.anynews.BuildConfig;
import com.anyNews.anynews.Pojo.CardItem;
import com.anyNews.anynews.Pojo.EncreptionRequest;
import com.anyNews.anynews.Pojo.LikeDownloadRespouse;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.CardAdapter;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.bumptech.glide.Glide;
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
        View view = LayoutInflater.from(container.getContext())
                .inflate(R.layout.adapter, container, false);
        container.addView(view);

        bind(mData.get(position), view);
        CardView cardView = (CardView) view.findViewById(R.id.cardView);
        ImageView images=(ImageView)view.findViewById(R.id.images);
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
        mViews.set(position,cardView);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
        mViews.set(position, null);
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
    private void bind(CardItem item, View view) {

        try {

            ImageView images=(ImageView)view.findViewById(R.id.images);
            Glide.with(context).load(item.getText()).into(images);

        }catch (RuntimeException e){
            e.printStackTrace();
        }
    }
    public interface shareMagazine{
        void onClickShareMagzine(CardView cardView,ImageView imageView1);
    }

}
