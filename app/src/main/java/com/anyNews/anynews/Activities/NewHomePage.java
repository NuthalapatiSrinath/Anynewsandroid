package com.anyNews.anynews.Activities;

import static com.anyNews.anynews.Utils.AppHelper.showLog;
import static com.anyNews.anynews.Utils.MyUtils.decrypt;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Display;
import android.view.View;

import com.anyNews.anynews.Pojo.EncreptionRequest;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.FlipperAdapter;
import com.anyNews.anynews.Utils.ModelClass;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.anyNews.anynews.flipview.FlipView;
import com.anyNews.anynews.flipview.OverFlipMode;
import com.google.gson.Gson;

import java.util.ArrayList;

public class NewHomePage extends AppCompatActivity implements FlipperAdapter.Callbacks, FlipView.OnFlipListener, FlipView.OnOverFlipListener, FlipView.OnFlipAppeared {
    FlipView mFlipView;
    private FlipperAdapter adapter;
    ArrayList<ModelClass.Datum> list=new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_home_page);
        mFlipView = (FlipView) findViewById(R.id.flip_view);
       // mFlipView.setmIsFlippingEnabled(false);
        Display display = getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();
        int distance;

        int resourceId = getResources().getIdentifier(
                "status_bar_height", "dimen", "android");
        int screenheight = (height - getResources().getDimensionPixelSize(
                resourceId));
        if (getResources().getDisplayMetrics().density <= 1.0) {
            distance = (int) (screenheight / 2.0);
        } else {
            distance = (int) (screenheight / getResources()
                    .getDisplayMetrics().density);
        }

        mFlipView.setFlipDistanceperpage(distance, getResources()
                .getDisplayMetrics().density);
        mFlipView.setOverFlipMode(OverFlipMode.RUBBER_BAND);

        adapter = new FlipperAdapter(this, NewHomePage.this, getIntent().getStringExtra("type"), 0, getLifecycle());
        adapter.setCallback(this);
        mFlipView.setAdapter(adapter);
        mFlipView.setOnFlipListener(this);
        mFlipView.setOverFlipMode(OverFlipMode.GLOW);
        mFlipView.peakNext(false);
        mFlipView.setOnOverFlipListener(this);
        mFlipView.setOnFlipAppear(this);
        EncreptionRequest response= (EncreptionRequest) getIntent().getSerializableExtra("data");
        convertListOfArticles(response);


    }
    public void convertListOfArticles(EncreptionRequest encreptionRequest){

        try {

            if (encreptionRequest!= null) {

                SharedPrefsData.putBool(getApplicationContext(), Constants.HOME_SCREEN, true, Constants.PREF_NAME);

                String encrpt_data = decrypt(encreptionRequest.getData());
                if(encrpt_data!=null && encrpt_data.length()>1){
                    int maxLogSize = 2000;
                    for(int i = 0; i <= encrpt_data.length() / maxLogSize; i++) {
                        int start = i * maxLogSize;
                        int end = (i+1) * maxLogSize;
                        end = end > encrpt_data.length() ? encrpt_data.length() : end;
                        android.util.Log.d("ALL ARTICLES DATA:", encrpt_data.substring(start, end));
                    }
                }


                Gson gsonObj = new Gson();
                ModelClass validateUserOtp = gsonObj.fromJson(encrpt_data, ModelClass.class);


                if (validateUserOtp.getSuccess()) {
                    if (validateUserOtp.getData().size() > 0) {
                        list.addAll(validateUserOtp.getData());
                        adapter.hasStableIds();
                        adapter.notifyDataSetChanged();
                        mFlipView.setVisibility(View.VISIBLE);


                    }
                } else {


                }
            }

        } catch (NullPointerException e) {

        }

    }

    @Override
    public void onPageRequested(int page) {
    }

    @Override
    public void onRequestedPermission(int page) {
    }

    @Override
    public void callLocationRequest(int from) {
    }

    @Override
    public void openFlashNewsFromFeedChip() {
    }

    @Override
    public void onVideoAutoplayEndedRequestNext(int position) {
    }

    @Override
    public void onFlippedToPage(FlipView v, int position, long id) {
        showLog("onFlippedToPage POSITION::"+position);
    }

    @Override
    public void onOverFlip(FlipView v, OverFlipMode mode, boolean overFlippingPrevious, float overFlipDistance, float flipDistancePerPage) {
    }

    @Override
    public void flipfinsh() {
    }

    @Override
    public void onFlippedToAppear(FlipView v) {
    }
}