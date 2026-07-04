 package com.anyNews.anynews.Activities;

import androidx.annotation.Keep;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.anyNews.anynews.Adapters.LanguageAdopter;
import com.anyNews.anynews.Pojo.EncreptionRequest;
import com.anyNews.anynews.Pojo.LanguageModelResponse;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Utils.Allsharedpreference;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.anyNews.anynews.models.LanguageModel;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.gson.Gson;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.anyNews.anynews.Utils.AppHelper.showLog;
import static com.anyNews.anynews.Utils.MyUtils.decrypt;

public class LanguageActivity extends AppCompatActivity {


    RecyclerView recycleLayoutManager;
    LinearLayoutManager linearLayoutManager;
    LanguageAdopter chooseLaguageAdopter;
    ArrayList<LanguageModel.Datum>arrayList;
    TextView select_language;
    Allsharedpreference prefShare;
    LinearLayout layout_with_data,layout_with_out_data;

    LanguageModelResponse languageModelResponse;

    // firebase storage
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();
    String type_firebase_language = "language_selection";
    int draft_id_firebase_lang = 0 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        recycleLayoutManager=findViewById(R.id.Language_Details);
        select_language=findViewById(R.id.select_language);
        layout_with_data=findViewById(R.id.layout_with_data);
        layout_with_out_data=findViewById(R.id.layout_with_out_data);
        layout_with_out_data.setVisibility(View.VISIBLE);
        layout_with_data.setVisibility(View.GONE);


        setStaticData();

        getLangageDetails();

        draft_id_firebase_lang = SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME);
        long current_time_firebase_lang = System.currentTimeMillis();
        String device_id_firebase = SharedPrefsData.getString(getApplicationContext(), Constants.DeviceID, Constants.PREF_NAME);

        writeLanguageSelection("User",type_firebase_language ,draft_id_firebase_lang ,device_id_firebase , current_time_firebase_lang);


    }

    private void setStaticData(){

        prefShare = new Allsharedpreference(this);
        Gson gson = new Gson();
        String Translation = prefShare.getShared_String(Allsharedpreference.Translator);
        languageModelResponse = gson.fromJson(Translation, LanguageModelResponse.class);

    }

    private void getLangageDetails() {

        linearLayoutManager=new LinearLayoutManager(this);
        recycleLayoutManager.setLayoutManager(linearLayoutManager);
        arrayList=new ArrayList<>();

        APIService apiService = RestApiClient.getClient().create(APIService.class);
        Call<EncreptionRequest> apiResponse = apiService.Languages_All_Languages("v3");

        apiResponse.enqueue(new Callback<EncreptionRequest>() {
            //@RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {

                if(response.body()!=null){
                    EncreptionRequest encreptionRequest=response.body();
                    showLog("RES"+response.body().getData());
                    showLog("API REQUEST R"+decrypt(response.body().getData()));
                    String encrpt_data =  decrypt(encreptionRequest.getData());


                    Gson gsonObj = new Gson();
                    LanguageModel languageModel = gsonObj.fromJson(encrpt_data, LanguageModel.class);

                    arrayList.clear();


                    if (languageModel.getSuccess()) {

                        if(languageModel.getData().size()>0){
                            for(int i=0;i<languageModel.getData().size();i++){

                                arrayList.add(new LanguageModel.Datum(languageModel.getData().get(i).getId(),languageModel.getData().get(i).getName()));
                            }
                            chooseLaguageAdopter = new LanguageAdopter(LanguageActivity.this,arrayList);
                            recycleLayoutManager.setAdapter(chooseLaguageAdopter);

                            layout_with_out_data.setVisibility(View.GONE);
                            layout_with_data.setVisibility(View.VISIBLE);

                        }else {

                        }


                    }
                    }
            }

            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {


            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        recycleLayoutManager.setAdapter(chooseLaguageAdopter);
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