package com.anyNews.anynews.Activities;

import static com.anyNews.anynews.Utils.AppHelper.showLog;
import static com.anyNews.anynews.Utils.MyUtils.decrypt;
import static com.anyNews.anynews.Utils.MyUtils.encrypt;

import androidx.annotation.Keep;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.anyNews.anynews.Adapters.LanguageAdopter;
import com.anyNews.anynews.Adapters.StatesAdopter;
import com.anyNews.anynews.Pojo.Datum;
import com.anyNews.anynews.Pojo.EncreptionRequest;
import com.anyNews.anynews.Pojo.EncritonRequestData;
import com.anyNews.anynews.Pojo.LanguageModelResponse;
import com.anyNews.anynews.Pojo.LanguageSelectionRequest;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Utils.Allsharedpreference;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.anyNews.anynews.models.LanguageModel;
import com.anyNews.anynews.models.RegionModel;
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

public class AreaSelection extends AppCompatActivity {
    RecyclerView recycleLayoutManager;
    LinearLayoutManager linearLayoutManager;
    AreaSelectionAdopter chooseAreaAdopter;
    ArrayList<RegionModel.Datum> arrayList;
    TextView select_language;
    Allsharedpreference prefShare;
    LinearLayout layout_with_data,layout_with_out_data;

    LanguageModelResponse languageModelResponse;

    // firebase storage
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();
    String type_firebase_language = "language_selection";
    int draft_id_firebase_lang = 0 ;
    ImageView img_back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_area_selection);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        recycleLayoutManager=findViewById(R.id.area_details);
        select_language=findViewById(R.id.select_language);
        layout_with_data=findViewById(R.id.layout_with_data);
        layout_with_out_data=findViewById(R.id.layout_with_out_data);
        layout_with_out_data.setVisibility(View.VISIBLE);
        img_back=findViewById(R.id.img_back);
        layout_with_data.setVisibility(View.GONE);
        setStaticData();

        getAreasList();

        draft_id_firebase_lang = SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME);
        long current_time_firebase_lang = System.currentTimeMillis();
        String device_id_firebase = SharedPrefsData.getString(getApplicationContext(), Constants.DeviceID, Constants.PREF_NAME);

        writeLanguageSelection("User",type_firebase_language ,draft_id_firebase_lang ,device_id_firebase , current_time_firebase_lang);
        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int back_value=  SharedPrefsData.getInt(getApplicationContext(), Constants.Back_Button_Move, Constants.PREF_NAME);

                if(back_value==1){
                    Intent intent=new Intent(AreaSelection.this,LanguageActivity.class);
                    startActivity(intent);
                    finish();

                }else{
                    finish();
                }

            }
        });

    }

    public void getAreasList(){

        linearLayoutManager=new LinearLayoutManager(this);
        recycleLayoutManager.setLayoutManager(linearLayoutManager);
        arrayList=new ArrayList<>();

        JsonObject otp = set_states_request();

        showLog("API REQ   "+otp.toString());
        APIService apiService = RestApiClient.getClient().create(APIService.class);
        Call<EncreptionRequest> apiResponse = apiService.get_regions_requiest("v3",otp);

        apiResponse.enqueue(new Callback<EncreptionRequest>() {
            //@RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {
                showLog("RES"+response.body().getData());
                showLog("API REQUEST R"+decrypt(response.body().getData()));
                EncreptionRequest encreptionRequest=response.body();
                String encrpt_data =  decrypt(encreptionRequest.getData());

                showLog(" AREAS LIST "+encrpt_data);
                Gson gsonObj = new Gson();
                RegionModel languageModel  = gsonObj.fromJson(encrpt_data, RegionModel.class);

                arrayList.clear();

                if (languageModel.getSuccess()) {

                //    SharedPrefsData.putString(AreaSelection.this, Constants.Distric_Name, languageModel.getNextScreen(), Constants.PREF_NAME);

                    if(languageModel.getData().size()>0){
                        arrayList.addAll(languageModel.getData());
                        chooseAreaAdopter = new AreaSelectionAdopter(AreaSelection.this,arrayList);
                        recycleLayoutManager.setAdapter(chooseAreaAdopter);
                        layout_with_data.setVisibility(View.VISIBLE);
                        layout_with_out_data.setVisibility(View.GONE);

                    }else {

                    }


                }

            }

            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {


            }
        });

    }

    private JsonObject set_states_request() {

        LanguageSelectionRequest requestModel = new LanguageSelectionRequest();

        requestModel.setDraftId(SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME));
        requestModel.setLanguageId(SharedPrefsData.getInt(getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME));

        String jsonString = new com.google.gson.Gson().toJson(requestModel);
        String encrpt_data = null;
        encrpt_data = encrypt(jsonString);

        showLog("API REQUEST E" + jsonString.toString());
        EncritonRequestData Encriton_Request=new EncritonRequestData();
        Encriton_Request.setData(encrpt_data);

        return new Gson().toJsonTree(Encriton_Request).getAsJsonObject();
    }

    private void setStaticData(){

        prefShare = new Allsharedpreference(this);
        Gson gson = new Gson();
        String Translation = prefShare.getShared_String(Allsharedpreference.Translator);
        languageModelResponse = gson.fromJson(Translation, LanguageModelResponse.class);

    }

    @Override
    public void onBackPressed() {
        int back_value=  SharedPrefsData.getInt(getApplicationContext(), Constants.Back_Button_Move, Constants.PREF_NAME);

        if(back_value==1){
            Intent intent=new Intent(AreaSelection.this,LanguageActivity.class);
            startActivity(intent);
            finish();

        }else{
            finish();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        recycleLayoutManager.setAdapter(chooseAreaAdopter);
    }

    public void writeLanguageSelection(String userId,String type_firebase ,int draft_id ,String device_id ,long current_time) {
     LanguageSelection appOpen = new LanguageSelection(type_firebase ,draft_id , device_id ,current_time);

        myRef.child("Events").push().setValue(appOpen);
    }
    @IgnoreExtraProperties
    @Keep
    public class LanguageSelection {

        public String type;
        public int draft_id;
        public long created_at;
        public String device_id;


        public LanguageSelection() {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }

        public LanguageSelection(String type_firebase,  int draft_id,String device_id, long current_time) {
            this.type = type_firebase;
            this.draft_id = draft_id;
            this.device_id = device_id;
            this.created_at = current_time;
        }

    }
}