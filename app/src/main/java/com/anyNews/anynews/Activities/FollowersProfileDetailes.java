package com.anyNews.anynews.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.anyNews.anynews.Adapters.FollowersProfileAdopter;
import com.anyNews.anynews.Pojo.EncreptionRequest;
import com.anyNews.anynews.Pojo.EncritonRequestData;
import com.anyNews.anynews.Pojo.LanguageModelResponse;
import com.anyNews.anynews.Pojo.PaymentHistoryRequest;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Utils.Allsharedpreference;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.MediaUrl;
import com.anyNews.anynews.Utils.SelfFollowGuard;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.anyNews.anynews.models.FollowersProfileModel;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.anyNews.anynews.Utils.AppHelper.showLog;
import static com.anyNews.anynews.Utils.MyUtils.decrypt;
import static com.anyNews.anynews.Utils.MyUtils.encrypt;

public class FollowersProfileDetailes extends AppCompatActivity {

    RecyclerView follower_profile_post;
    GridLayoutManager linearLayoutManager;
    FollowersProfileAdopter followersProfileAdopter;
    ArrayList<FollowersProfileModel.Datum> followersProfileModels;
    ImageView back;
    LinearLayout linear_no_recyclerView, follow_layout_with_out_data;

    Allsharedpreference prefShare;
    LanguageModelResponse languageModelResponse;
    TextView discover;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followers_profile_detailes);
        linear_no_recyclerView = findViewById(R.id.linear_no_recyclerView);
        follower_profile_post = findViewById(R.id.follower_profile_post);
        follow_layout_with_out_data = findViewById(R.id.follow_layout_with_out_data);
        back = findViewById(R.id.back);
        discover = findViewById(R.id.discover);
        back.setOnClickListener(view -> finish());

        setStaticdata();
        showLoading();

        LocalBroadcastManager.getInstance(this).registerReceiver(FOLLOWERS_PAGE_ACTIVE,
                new IntentFilter("FOLLOWERS_PAGE_ACTIVE"));

        getPaymentHistory();
    }

    private void setStaticdata() {
        try {
            prefShare = new Allsharedpreference(FollowersProfileDetailes.this);
            Gson gson = new Gson();
            String translation = prefShare.getShared_String(Allsharedpreference.Translator);
            if (translation != null) {
                languageModelResponse = gson.fromJson(translation, LanguageModelResponse.class);
            }
            if (languageModelResponse != null && languageModelResponse.getData() != null
                    && languageModelResponse.getData().getDiscover() != null) {
                discover.setText(" " + languageModelResponse.getData().getDiscover());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showLoading() {
        follow_layout_with_out_data.setVisibility(View.VISIBLE);
        follower_profile_post.setVisibility(View.GONE);
        linear_no_recyclerView.setVisibility(View.GONE);
    }

    private void showContent() {
        follow_layout_with_out_data.setVisibility(View.GONE);
        follower_profile_post.setVisibility(View.VISIBLE);
        linear_no_recyclerView.setVisibility(View.GONE);
    }

    private void showEmpty() {
        follow_layout_with_out_data.setVisibility(View.GONE);
        follower_profile_post.setVisibility(View.GONE);
        linear_no_recyclerView.setVisibility(View.VISIBLE);
    }

    public BroadcastReceiver FOLLOWERS_PAGE_ACTIVE = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            getPaymentHistory();
        }
    };

    private JsonObject ReadOTP() {
        PaymentHistoryRequest requestModel = new PaymentHistoryRequest();
        requestModel.setDraftId(SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME));
        String jsonString = new Gson().toJson(requestModel);
        showLog("API REQUEST E" + jsonString);
        String encrpt_data = encrypt(jsonString);
        EncritonRequestData Encriton_Request = new EncritonRequestData();
        Encriton_Request.setData(encrpt_data);
        return new Gson().toJsonTree(Encriton_Request).getAsJsonObject();
    }

    public void getPaymentHistory() {
        showLoading();
        followersProfileModels = new ArrayList<>();

        JsonObject otp = ReadOTP();
        showLog("API REQ   " + otp.toString());
        APIService apiService = RestApiClient.getClient().create(APIService.class);
        apiService.follow_more("v3", otp).enqueue(new Callback<EncreptionRequest>() {
            @Override
            public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {
                if (response.body() == null || response.body().getData() == null) {
                    showLog("follow_more empty response body");
                    showEmpty();
                    return;
                }
                try {
                    String encrpt_data = decrypt(response.body().getData());
                    showLog("API REQUEST R" + encrpt_data);
                    FollowersProfileModel languageModel = new Gson().fromJson(encrpt_data, FollowersProfileModel.class);

                    if (languageModel != null && Boolean.TRUE.equals(languageModel.getSuccess())
                            && languageModel.getData() != null && !languageModel.getData().isEmpty()) {

                        followersProfileModels.addAll(SelfFollowGuard.withoutSelfSuggestions(
                                FollowersProfileDetailes.this, languageModel.getData()));
                        if (followersProfileModels.isEmpty()) {
                            showEmpty();
                            return;
                        }
                        for (FollowersProfileModel.Datum item : followersProfileModels) {
                            MediaUrl.normalizeFollowProfile(item);
                        }
                        if (linearLayoutManager == null) {
                            linearLayoutManager = new GridLayoutManager(FollowersProfileDetailes.this, 3);
                            follower_profile_post.setLayoutManager(linearLayoutManager);
                        }
                        followersProfileAdopter = new FollowersProfileAdopter(
                                FollowersProfileDetailes.this, followersProfileModels);
                        follower_profile_post.setAdapter(followersProfileAdopter);
                        showContent();
                    } else {
                        showEmpty();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showEmpty();
                }
            }

            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {
                showLog("follow_more failed: " + t.getMessage());
                showEmpty();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(FOLLOWERS_PAGE_ACTIVE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
