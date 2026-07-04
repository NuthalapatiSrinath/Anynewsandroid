package com.anyNews.anynews.Activities;

import androidx.annotation.Keep;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.anyNews.anynews.Adapters.StatesAdopter;
import com.anyNews.anynews.Pojo.EncreptionRequest;
import com.anyNews.anynews.Pojo.EncritonRequestData;
import com.anyNews.anynews.Pojo.LanguageModelResponse;
import com.anyNews.anynews.Pojo.LanguageSelectionRequest;
import com.anyNews.anynews.Pojo.LanguageSelectionResponse;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Utils.Allsharedpreference;
import com.anyNews.anynews.Utils.AppLanguagesFonts;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.SharedPrefsData;
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
import static com.facebook.FacebookSdk.getApplicationContext;

public class States extends AppCompatActivity {

    RecyclerView recycleLayoutManager;
    GridLayoutManager linearLayoutManager;
    StatesAdopter statesAdopter;
    ArrayList<StatesModel.Datum> arrayList;
    Allsharedpreference prefShare;
    LanguageSelectionResponse languageSelectionResponse;
    TextView select_state;
    int LANGUAGE_ID;
    LinearLayout layout_with_data,layout_with_out_data;

    LanguageModelResponse languageModelResponse;
    ImageView img_back;
    // firebase storage
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();
    String type_firebase_state = "state_selection";
    int draft_id_firebase_state = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_states);
        recycleLayoutManager=findViewById(R.id.States_Details);
        layout_with_out_data=findViewById(R.id.layout_with_out_data);
        layout_with_data=findViewById(R.id.layout_with_data);
        img_back=findViewById(R.id.img_back);
        layout_with_data.setVisibility(View.GONE);
        layout_with_out_data.setVisibility(View.VISIBLE);

        prefShare = new Allsharedpreference(States.this);
        select_state=findViewById(R.id.select_state);
        arrayList=new ArrayList<>();
        arrayList.clear();

        linearLayoutManager = new GridLayoutManager(States.this, 2, GridLayoutManager.VERTICAL, false);
        recycleLayoutManager.setLayoutManager(linearLayoutManager);


        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent=new Intent(States.this,LanguageActivity.class);
                startActivity(intent);
            }
        });

        setStaticdata();

        getStates();

        draft_id_firebase_state = SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME);
        long current_time_firebase_lang = System.currentTimeMillis();
        String device_id_firebase = SharedPrefsData.getString(getApplicationContext(), Constants.DeviceID, Constants.PREF_NAME);

        writeStateSelection("User",type_firebase_state ,draft_id_firebase_state ,device_id_firebase, current_time_firebase_lang);
    }

    private void setStaticdata() {

        Gson gson = new Gson();
        String Translation = prefShare.getShared_String(Allsharedpreference.Translator);
        languageModelResponse = gson.fromJson(Translation, LanguageModelResponse.class);

        select_state.setText(" "+languageModelResponse.getData().getSelectState());
        AppLanguagesFonts.setTypeFace(select_state);
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
    public void getStates(){

        JsonObject otp = set_states_request();
        showLog("API REQ   "+otp.toString());
        APIService apiService = RestApiClient.getClient().create(APIService.class);
        Call<EncreptionRequest> apiResponse = apiService.Get_states_requiest("v3",otp);

        apiResponse.enqueue(new Callback<EncreptionRequest>() {
            //@RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {

                    EncreptionRequest encreptionRequest=response.body();
                showLog("API RES   "+response.body().getData());
                    String encrpt_data =  decrypt(encreptionRequest.getData());

                    Gson gsonObj = new Gson();
                    StatesModel languageModel  = gsonObj.fromJson(encrpt_data, StatesModel.class);

                    arrayList.clear();




                    if (languageModel.getSuccess()) {

                        SharedPrefsData.putString(States.this, Constants.Distric_Name, languageModel.getNextScreen(), Constants.PREF_NAME);

                        if(languageModel.getData().size()>0){
                            arrayList.addAll(languageModel.getData());

                            statesAdopter = new StatesAdopter(States.this,arrayList);
                            recycleLayoutManager.setAdapter(statesAdopter);
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

    @Override
    protected void onStart() {
        super.onStart();
    showLog(" ONE1 ");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent=new Intent(States.this,LanguageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
    @Override
    protected void onStop() {
        super.onStop();
        showLog(" ONE2 ");
    }

    @Override
    protected void onPause() {
        super.onPause();



    }

    @Override
    protected void onResume() {
        super.onResume();


    }

    @Override
    protected void onRestart() {
        super.onRestart();
        getStates();
    }

    public void writeStateSelection(String userId,String type_firebase ,int draft_id ,String device_id,long current_time) {
        StateSelection appOpen = new StateSelection(type_firebase ,draft_id ,device_id, current_time);

        myRef.child("Events").push().setValue(appOpen);
    }
    @IgnoreExtraProperties
    @Keep
    public class StateSelection {

        public String type;
        public int draft_id;
        public long created_at;
        public String device_id;


        public StateSelection() {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }

        public StateSelection(String type_firebase,  int draft_id,String device_id, long current_time) {
            this.type = type_firebase;
            this.draft_id = draft_id;
            this.device_id = device_id;
            this.created_at = current_time;
        }

    }
}