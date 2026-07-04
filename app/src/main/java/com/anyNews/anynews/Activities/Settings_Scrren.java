package com.anyNews.anynews.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.anyNews.anynews.Adapters.SettingAdopterData;
import com.anyNews.anynews.Pojo.EncreptionRequest;
import com.anyNews.anynews.Pojo.EncritonRequestData;
import com.anyNews.anynews.Pojo.GetSettingsResponse;
import com.anyNews.anynews.Pojo.LanguageModelResponse;
import com.anyNews.anynews.Pojo.LanguageSelectionRequest;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Utils.Allsharedpreference;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.anyNews.anynews.models.SettingsModel;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.anyNews.anynews.Utils.AppHelper.showLog;
import static com.anyNews.anynews.Utils.MyUtils.decrypt;
import static com.anyNews.anynews.Utils.MyUtils.encrypt;

public class Settings_Scrren extends AppCompatActivity {

    ArrayList<SettingsModel>List_name;
    LinearLayoutManager linearLayoutManager;
    SettingAdopterData settingsAdopter;
    RecyclerView recyclerView;
    ImageView go_back;
    CountDownTimer countDownTimer;

    LanguageModelResponse languageModelResponse;
    Allsharedpreference prefShare;

    String version_name;
    TextView app_version_id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings__scrren);
        go_back=findViewById(R.id.go_back);
        recyclerView=findViewById(R.id.settings_details);
        app_version_id=findViewById(R.id.app_version_id);
        List_name=new ArrayList<>();

        go_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        setStaticdata();

        getSettingsApi();

    }

    private void setStaticdata() {
        prefShare = new Allsharedpreference(Settings_Scrren.this);
        Gson gson = new Gson();
        String Translation = prefShare.getShared_String(Allsharedpreference.Translator);
        languageModelResponse = gson.fromJson(Translation, LanguageModelResponse.class);

        PackageManager manager = getApplicationContext().getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(
                    getApplicationContext().getPackageName(), 0);


            version_name = info.versionName;

            app_version_id.setText(" "+languageModelResponse.getData().getVersion()+"  "+version_name);


        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
       // Toast.makeText(Settings_Scrren.this, ""+languageModelResponse.getData().getVersion(), Toast.LENGTH_SHORT).show();

    }


    private JsonObject set_setting_detailes() {

        LanguageSelectionRequest requestModel = new LanguageSelectionRequest();

        requestModel.setDraftId(SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME));

        String jsonString = new com.google.gson.Gson().toJson(requestModel);
        String encrpt_data = null;
        /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            encrpt_data = encrypt(jsonString);
        }*/
        encrpt_data = encrypt(jsonString);
        showLog("API REQUEST E" + jsonString.toString());
        EncritonRequestData Encriton_Request=new EncritonRequestData();
        Encriton_Request.setData(encrpt_data);
        return new Gson().toJsonTree(Encriton_Request).getAsJsonObject();

    }
    public void getSettingsApi(){

        JsonObject otp = set_setting_detailes();
        showLog("API REQ   "+otp.toString());
        APIService apiService = RestApiClient.getClient().create(APIService.class);
        Call<EncreptionRequest> apiResponse = apiService.get_settings("v3",otp);

        apiResponse.enqueue(new Callback<EncreptionRequest>() {
            @Override
            public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {

                EncreptionRequest encreptionRequest=response.body();
                showLog("RES"+response.body().getData());
                showLog("API REQUEST R"+decrypt(response.body().getData()));
                String encrpt_data = null;
                /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    encrpt_data = decrypt(encreptionRequest.getData());
                }*/
                encrpt_data = decrypt(encreptionRequest.getData());

                showLog("ENCRYPT"+encrpt_data);

                Gson gsonObj = new Gson();
                GetSettingsResponse languageModel= gsonObj.fromJson(encrpt_data, GetSettingsResponse.class);


                    if (languageModel.getSuccess()) {


                            linearLayoutManager=new LinearLayoutManager(Settings_Scrren.this);

                        //    List_name.add(new SettingsModel(languageModelResponse.getData().getLanguage(),1));
                            List_name.add(new SettingsModel(languageModelResponse.getData().getNotifications(),2));
                            List_name.add(new SettingsModel(languageModelResponse.getData().getRateThisApp(),3));
                            List_name.add(new SettingsModel(languageModelResponse.getData().getTermsAndConditions(),4));
                            List_name.add(new SettingsModel(languageModelResponse.getData().getPrivacy(),5));
                            List_name.add(new SettingsModel(languageModelResponse.getData().getContactUs(),6));

                            settingsAdopter=new SettingAdopterData(Settings_Scrren.this,List_name,languageModel.getData().getNotificationFlag());
                            recyclerView.setLayoutManager(linearLayoutManager);
                            recyclerView.setAdapter(settingsAdopter);


                    }


                }


            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {


            }
        });
    }

}