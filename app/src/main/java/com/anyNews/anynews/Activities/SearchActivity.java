package com.anyNews.anynews.Activities;

import androidx.annotation.Keep;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.anyNews.anynews.Adapters.FlexboxAdapter;
import com.anyNews.anynews.BuildConfig;
import com.anyNews.anynews.Pojo.CategoriesRequest;
import com.anyNews.anynews.Pojo.EncreptionRequest;
import com.anyNews.anynews.Pojo.EncritonRequestData;
import com.anyNews.anynews.Pojo.LanguageModelResponse;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Utils.Allsharedpreference;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.anyNews.anynews.models.SearchModel;
import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
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


public class SearchActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    ArrayList<SearchModel.Datum> arrayList ;
    FlexboxAdapter adapter;
    TextView chooseCategories;
    Allsharedpreference prefShare;
    LanguageModelResponse languageModelResponse;
    ImageView backButtonSearch;
    // firebase storage
    /*FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();
    String type_firebase_category = "category_page_selection";
    int draft_id_firebase_category = 0;*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        backButtonSearch = findViewById(R.id.back__button_category);

        recyclerView = findViewById(R.id.recyclerView);
        chooseCategories = findViewById(R.id.choose_categories);
        recyclerView.setHasFixedSize(true);
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(this);
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setJustifyContent(JustifyContent.CENTER);
        layoutManager.setAlignItems(AlignItems.CENTER);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        setStaticdata();

        getDataFromServer();

        backButtonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SearchActivity.this, HomePage.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                if (getApplicationContext() != null && getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)){
                    startActivity(intent);
                }
                finish();
            }
        });

        // firebase events
        /*draft_id_firebase_category = SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME);
        long current_time_firebase_category = System.currentTimeMillis();
        String device_id_firebase = SharedPrefsData.getString(getApplicationContext(), Constants.DeviceID, Constants.PREF_NAME);

        writeCategorySelection("User",type_firebase_category ,draft_id_firebase_category ,device_id_firebase, current_time_firebase_category);*/

    }

    private void setStaticdata() {
        prefShare = new Allsharedpreference(this);
        Gson gson = new Gson();
        String Translation = prefShare.getShared_String(Allsharedpreference.Translator);
        languageModelResponse = gson.fromJson(Translation, LanguageModelResponse.class);
        try {
            chooseCategories.setText(" "+languageModelResponse.getData().getChooseThreeOrMoreCategories());
        } catch (Exception e) {
            chooseCategories.setText(" Choose categories");
        }
    }
    public void getDataFromServer(){

        arrayList=new ArrayList<SearchModel.Datum>();
        final int langId =
                SharedPrefsData.getInt(
                        getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME);
        try {
            String cached =
                    com.anyNews.anynews.Utils.CategoriesDiskCache.loadIfMatches(this, langId);
            if (cached != null && !cached.isEmpty()) {
                SearchModel fromDisk = new Gson().fromJson(cached, SearchModel.class);
                if (fromDisk != null
                        && fromDisk.getSuccess()
                        && fromDisk.getData() != null) {
                    arrayList.addAll(fromDisk.getData());
                    adapter = new FlexboxAdapter(SearchActivity.this, arrayList);
                    recyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    return;
                }
            }
        } catch (Exception ignored) {
        }

        JsonObject otp = sendRequest();

        showLog("API REQ   "+otp.toString());
        APIService apiService = RestApiClient.getClient().create(APIService.class);

        Call<EncreptionRequest> otp_generation = apiService.List_All_Categories("v3",otp);

        try {
            otp_generation.enqueue(new Callback<EncreptionRequest>() {

                @Override
                public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {


                    EncreptionRequest encreptionRequest=response.body();
                    showLog("API RES   "+response.body().getData());
                    String encrpt_data =  decrypt(encreptionRequest.getData());

                    showLog("ENCRYPT"+encrpt_data);
                    try {
                        com.anyNews.anynews.Utils.CategoriesDiskCache.save(
                                SearchActivity.this, langId, encrpt_data);
                    } catch (Exception ignored) {
                    }

                    Gson gsonObj = new Gson();
                    SearchModel validateUserOtp  = gsonObj.fromJson(encrpt_data, SearchModel.class);

                   //     Toast.makeText(SearchActivity.this, ""+validateUserOtp.getSuccess(), Toast.LENGTH_SHORT).show();

                        if (validateUserOtp.getSuccess()){
                            arrayList.addAll(validateUserOtp.getData());
                            adapter = new FlexboxAdapter(SearchActivity.this, arrayList);
                            recyclerView.setAdapter(adapter);
                            adapter.notifyDataSetChanged();

                        }  else {


                        }



                }

                @Override
                public void onFailure(Call<EncreptionRequest> call, Throwable t) {
                }
            });

        } catch (Exception e){
         //   Toast.makeText(SearchActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    private JsonObject sendRequest(){

        CategoriesRequest requestModel = new CategoriesRequest();

        requestModel.setDraftId(SharedPrefsData.getInt(SearchActivity.this, Constants.DraftID, Constants.PREF_NAME));
        requestModel.setLanguageId(SharedPrefsData.getInt(getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME));
        requestModel.setScreenType(0);

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
    public void onResume() {
        super.onResume();
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        //  super.onBackPressed();
        Intent intent=new Intent(SearchActivity.this,HomePage.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        if (getApplicationContext() != null && getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)){
            startActivity(intent);
        }

        finish();

    }

    public void writeCategorySelection(String userId,String type_firebase ,int draft_id ,String device_id,long current_time) {
        CategorySelection appOpen = new CategorySelection(type_firebase ,draft_id ,device_id, current_time);

        //myRef.child("Events").push().setValue(appOpen);
    }
    @IgnoreExtraProperties
    @Keep
    public class CategorySelection {

        public String type;
        public int draft_id;
        public long created_at;
        public String device_id;


        public CategorySelection() {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }

        public CategorySelection(String type_firebase,  int draft_id,String device_id, long current_time) {
            this.type = type_firebase;
            this.draft_id = draft_id;
            this.device_id = device_id;
            this.created_at = current_time;
        }

    }

}