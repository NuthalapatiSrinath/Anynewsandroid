package com.anyNews.anynews.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.anyNews.anynews.Adapters.FollowingMemberAdopter;
import com.anyNews.anynews.Pojo.EncreptionRequest;
import com.anyNews.anynews.Pojo.EncritonRequestData;
import com.anyNews.anynews.Pojo.LanguageModelResponse;
import com.anyNews.anynews.Pojo.PaymentHistoryRequest;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Utils.Allsharedpreference;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.anyNews.anynews.models.FollowingMenderModel;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.anyNews.anynews.Utils.AppHelper.showLog;
import static com.anyNews.anynews.Utils.MyUtils.decrypt;
import static com.anyNews.anynews.Utils.MyUtils.encrypt;

public class FolloweingMenders extends AppCompatActivity {

    RecyclerView followeing_menbers;
    ImageView back;
    LinearLayoutManager linearLayoutManager;
    FollowingMemberAdopter districsAdopter;
    ArrayList<FollowingMenderModel.Datum> paymentHistoryModel;
    LinearLayout layout_with_out_data,layout_with_data,linear_no_recyclerView,no_data_scrren;
    TextView menbers_list;

    Allsharedpreference prefShare;
    LanguageModelResponse languageModelResponse;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followeing_menders);
        layout_with_out_data=findViewById(R.id.follow_layout_with_out_data);
        layout_with_data=findViewById(R.id.follow_layout_with_data);
        linear_no_recyclerView=findViewById(R.id.linear_no_recyclerView);
        followeing_menbers=findViewById(R.id.followeing_menbers);
        no_data_scrren=findViewById(R.id.no_data_scrren);
        menbers_list=findViewById(R.id.menbers_list);
        back=findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        setStaticdata();

        getPaymentHistory();
    }

    private void setStaticdata() {
        prefShare = new Allsharedpreference(FolloweingMenders.this);
        Gson gson = new Gson();
        String Translation = prefShare.getShared_String(Allsharedpreference.Translator);
        languageModelResponse = gson.fromJson(Translation, LanguageModelResponse.class);

        menbers_list.setText(" "+languageModelResponse.getData().getMembers());

    }

    private JsonObject set_payment_history() {

        PaymentHistoryRequest requestModel = new PaymentHistoryRequest();
        requestModel.setDraftId(SharedPrefsData.getInt(FolloweingMenders.this, Constants.DraftID, Constants.PREF_NAME));


        String jsonString = new com.google.gson.Gson().toJson(requestModel);
        String encrpt_data = null;
        showLog("API REQUEST E" + jsonString.toString());
        encrpt_data = encrypt(jsonString);
        EncritonRequestData Encriton_Request=new EncritonRequestData();
        Encriton_Request.setData(encrpt_data);

        return new Gson().toJsonTree(Encriton_Request).getAsJsonObject();
    }


    public void getPaymentHistory() {

        paymentHistoryModel = new ArrayList<FollowingMenderModel.Datum>();

        JsonObject otp = set_payment_history();
        showLog("API REQ   "+otp.toString());
        APIService apiService = RestApiClient.getClient().create(APIService.class);
        Call<EncreptionRequest> apiResponse = apiService.get_follow_list("v3", otp);

        apiResponse.enqueue(new Callback<EncreptionRequest>() {
            @Override
            public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {

                if(response.body()!=null){
                    EncreptionRequest encreptionRequest=response.body();
                    showLog("RES"+response.body().getData());
                    showLog("API REQUEST R"+decrypt(response.body().getData()));
                    String encrpt_data = null;
                    encrpt_data = decrypt(encreptionRequest.getData());


                    Gson gsonObj = new Gson();
                    FollowingMenderModel languageModel = gsonObj.fromJson(encrpt_data, FollowingMenderModel.class);



                    if (languageModel.getSuccess()) {


                        if (languageModel.getData().size() > 0) {
                            paymentHistoryModel.addAll(languageModel.getData());
                            linearLayoutManager = new LinearLayoutManager(FolloweingMenders.this);
                            followeing_menbers.setLayoutManager(linearLayoutManager);

                            districsAdopter = new FollowingMemberAdopter(FolloweingMenders.this,paymentHistoryModel);
                            followeing_menbers.setAdapter(districsAdopter);

                            no_data_scrren.setVisibility(View.GONE);
                            layout_with_out_data.setVisibility(View.GONE);
                            layout_with_data.setVisibility(View.VISIBLE);

                        }else {

                            layout_with_out_data.setVisibility(View.GONE);
                            followeing_menbers.setVisibility(View.GONE);
                            linear_no_recyclerView.setVisibility(View.GONE);
                            no_data_scrren.setVisibility(View.VISIBLE);

                        }
                    }

                }

                }

            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {


            }
        });

    }
}
