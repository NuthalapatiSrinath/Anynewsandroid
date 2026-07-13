package com.anyNews.anynews.Activities;

import androidx.annotation.Keep;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.anyNews.anynews.Adapters.DistricsAdopter;
import com.anyNews.anynews.Adapters.LanguageAdopter;
import com.anyNews.anynews.Adapters.StatesAdopter;
import com.anyNews.anynews.Pojo.EncreptionRequest;
import com.anyNews.anynews.Pojo.EncritonRequestData;
import com.anyNews.anynews.Pojo.LanguageModelResponse;
import com.anyNews.anynews.Pojo.LanguageSelectionRequest;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Utils.Allsharedpreference;
import com.anyNews.anynews.Utils.AppLanguagesFonts;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.FontSettingsHelper;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.anyNews.anynews.models.DistricsModel;
import com.anyNews.anynews.models.LanguageModel;
import com.anyNews.anynews.models.StatesModel;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.anyNews.anynews.Utils.AppHelper.showLog;
import static com.anyNews.anynews.Utils.MyUtils.decrypt;
import static com.anyNews.anynews.Utils.MyUtils.encrypt;

public class Districs extends AppCompatActivity {

    RecyclerView recycleLayoutManager;
    LinearLayoutManager linearLayoutManager;
    DistricsAdopter districsAdopter;
    ArrayList<DistricsModel.Datum> arrayList;
    LinearLayout layout_with_data,layout_with_out_data;

    LanguageModelResponse languageModelResponse;
    TextView select_distic;
    Allsharedpreference prefShare;
    ImageView img_back;

    // firebase storage
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();
    String type_firebase_district = "district_selection";
    int draft_id_firebase_district = 0 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_districs);
        layout_with_out_data=findViewById(R.id.layout_with_out_data);
        layout_with_data=findViewById(R.id.layout_with_data);
        select_distic=findViewById(R.id.select_distic);
        img_back=findViewById(R.id.img_back);

        layout_with_out_data.setVisibility(View.VISIBLE);
        layout_with_data.setVisibility(View.GONE);
        recycleLayoutManager=findViewById(R.id.Districs_Details);
        arrayList=new ArrayList<>();
        linearLayoutManager = new LinearLayoutManager(Districs.this);
        recycleLayoutManager.setLayoutManager(linearLayoutManager);

        setStaticdata();

        getDisrics();

        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


              int back_value=  SharedPrefsData.getInt(getApplicationContext(), Constants.Back_Button_Move, Constants.PREF_NAME);

              if(back_value==1){


              }else if(back_value==2){
                  Intent intent=new Intent(Districs.this,States.class);
                  startActivity(intent);
              }else {
                  Intent intent=new Intent(Districs.this,LanguageActivity.class);
                  startActivity(intent);
              }

            }
        });

        draft_id_firebase_district = SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME);
        long current_time_firebase_lang = System.currentTimeMillis();
        String device_id_firebase = SharedPrefsData.getString(getApplicationContext(), Constants.DeviceID, Constants.PREF_NAME);

        writeDistrictSelection("User",type_firebase_district ,draft_id_firebase_district , device_id_firebase, current_time_firebase_lang);

    }

    private void setStaticdata() {
        prefShare = new Allsharedpreference(Districs.this);
        Gson gson = new Gson();
        String Translation = prefShare.getShared_String(Allsharedpreference.Translator);
        languageModelResponse = gson.fromJson(Translation, LanguageModelResponse.class);

        select_distic.setText(" "+languageModelResponse.getData().getSelectDistrict());
        AppLanguagesFonts.setTypeFace(select_distic);
        int languageId = SharedPrefsData.getInt(getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME);
        FontSettingsHelper.applySelectionHeader(getApplicationContext(), languageId > 0 ? languageId : 1, select_distic);


    }
    private JsonObject set_districs() {

        LanguageSelectionRequest requestModel = new LanguageSelectionRequest();

        requestModel.setDraftId(SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME));
        requestModel.setLanguageId(SharedPrefsData.getInt(getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME));
        requestModel.setStateId(SharedPrefsData.getInt(getApplicationContext(), Constants.StateID, Constants.PREF_NAME));
        String jsonString = new com.google.gson.Gson().toJson(requestModel);
        String encrpt_data = null;
        encrpt_data = encrypt(jsonString);
        showLog("API REQUEST E" + jsonString.toString());
        EncritonRequestData Encriton_Request=new EncritonRequestData();
        Encriton_Request.setData(encrpt_data);
        return new Gson().toJsonTree(Encriton_Request).getAsJsonObject();
    }
    public void getDisrics(){

        JsonObject otp = set_districs();

        showLog("API REQ   "+otp.toString());


        APIService apiService = RestApiClient.getClient().create(APIService.class);
        Call<EncreptionRequest> apiResponse = apiService.Get_districts("v3",otp);

        apiResponse.enqueue(new Callback<EncreptionRequest>() {
            //@RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {


                EncreptionRequest encreptionRequest=response.body();
                showLog("API RES   "+response.body().getData());
                String encrpt_data =  decrypt(encreptionRequest.getData());


                Gson gsonObj = new Gson();
                DistricsModel languageModel  = gsonObj.fromJson(encrpt_data, DistricsModel.class);

                arrayList.clear();


                if (languageModel.getSuccess()) {

                        if(languageModel.getData().size()>0){

                            arrayList.addAll(languageModel.getData());
                            districsAdopter = new DistricsAdopter(Districs.this,arrayList);
                            recycleLayoutManager.setAdapter(districsAdopter);


                        }else {

                        }

                    }

                layout_with_out_data.setVisibility(View.GONE);
                layout_with_data.setVisibility(View.VISIBLE);

            }

            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {

            }
        });
    }

    public void writeDistrictSelection(String userId,String type_firebase ,int draft_id ,String device_id ,long current_time) {
        DistrictSelection appOpen = new DistrictSelection(type_firebase ,draft_id ,device_id, current_time);

        myRef.child("Events").push().setValue(appOpen);
    }
    @IgnoreExtraProperties
    @Keep
    public class DistrictSelection {

        public String type;
        public int draft_id;
        public long created_at;
        public String device_id;


        public DistrictSelection() {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }

        public DistrictSelection(String type_firebase,  int draft_id,String device_id, long current_time) {
            this.type = type_firebase;
            this.draft_id = draft_id;
            this.device_id = device_id;
            this.created_at = current_time;
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        int back_value=  SharedPrefsData.getInt(getApplicationContext(), Constants.Back_Button_Move, Constants.PREF_NAME);

        if(back_value==1){

        }else if(back_value==2){
            Intent intent=new Intent(Districs.this,States.class);
            startActivity(intent);
        }else {
            Intent intent=new Intent(Districs.this,LanguageActivity.class);
            startActivity(intent);
        }
    }
}