package com.anyNews.anynews.Activities;

import androidx.annotation.Keep;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.anyNews.anynews.Adapters.DistricsAdopter;
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
import com.anyNews.anynews.Utils.SelectionListCache;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.anyNews.anynews.models.DistricsModel;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

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
    LinearLayout layout_with_data, layout_with_out_data;

    LanguageModelResponse languageModelResponse;
    TextView select_distic;
    Allsharedpreference prefShare;
    ImageView img_back;

    // firebase storage
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();
    String type_firebase_district = "district_selection";
    int draft_id_firebase_district = 0;

    private boolean paintedFromCache = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_districs);
        layout_with_out_data = findViewById(R.id.layout_with_out_data);
        layout_with_data = findViewById(R.id.layout_with_data);
        select_distic = findViewById(R.id.select_distic);
        img_back = findViewById(R.id.img_back);

        recycleLayoutManager = findViewById(R.id.Districs_Details);
        arrayList = new ArrayList<>();
        linearLayoutManager = new LinearLayoutManager(Districs.this);
        recycleLayoutManager.setLayoutManager(linearLayoutManager);

        setStaticdata();

        paintFromCacheIfAvailable();
        getDisrics();

        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int back_value = SharedPrefsData.getInt(getApplicationContext(), Constants.Back_Button_Move, Constants.PREF_NAME);

                if (back_value == 1) {


                } else if (back_value == 2) {
                    Intent intent = new Intent(Districs.this, States.class);
                    startActivity(intent);
                } else {
                    startActivity(LanguageActivity.createChangeLanguageIntent(Districs.this));
                }

            }
        });

        draft_id_firebase_district = SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME);
        long current_time_firebase_lang = System.currentTimeMillis();
        String device_id_firebase = SharedPrefsData.getString(getApplicationContext(), Constants.DeviceID, Constants.PREF_NAME);

        writeDistrictSelection("User", type_firebase_district, draft_id_firebase_district, device_id_firebase, current_time_firebase_lang);
    }

    private void setStaticdata() {
        prefShare = new Allsharedpreference(Districs.this);
        Gson gson = new Gson();
        String Translation = prefShare.getShared_String(Allsharedpreference.Translator);
        languageModelResponse = gson.fromJson(Translation, LanguageModelResponse.class);

        String title = null;
        if (languageModelResponse != null && languageModelResponse.getData() != null) {
            title = languageModelResponse.getData().getSelectDistrict();
        }
        if (title == null || title.isEmpty()) {
            title = getIntent().getStringExtra("LANGUAGE_NAME");
        }
        if (title == null || title.isEmpty()) {
            title = "Select District";
        }
        select_distic.setText(" " + title);
        AppLanguagesFonts.setTypeFace(select_distic);
        int languageId = SharedPrefsData.getInt(getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME);
        FontSettingsHelper.applySelectionHeader(getApplicationContext(), languageId > 0 ? languageId : 1, select_distic);
    }

    private void paintFromCacheIfAvailable() {
        int languageId = SharedPrefsData.getInt(getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME);
        int stateId = SharedPrefsData.getInt(getApplicationContext(), Constants.StateID, Constants.PREF_NAME);
        List<DistricsModel.Datum> cached = SelectionListCache.loadDistricts(this, languageId, stateId);
        if (cached == null || cached.isEmpty()) {
            showContentShellWithoutSpinner();
            return;
        }
        // Drop cache rows whose script conflicts with LanguageID (Hindi list
        // previously saved under a Telugu session before server language-scope).
        java.util.ArrayList<DistricsModel.Datum> filtered = new java.util.ArrayList<>();
        for (DistricsModel.Datum d : cached) {
            if (d == null || d.getName() == null) continue;
            if (com.anyNews.anynews.Utils.DistrictLabelHelper.isClearScriptConflict(
                    d.getName(), languageId)) {
                continue;
            }
            filtered.add(d);
        }
        if (filtered.isEmpty()) {
            showContentShellWithoutSpinner();
            return;
        }
        arrayList.clear();
        arrayList.addAll(filtered);
        bindDistrictList(false);
        paintedFromCache = true;
    }

    private void showContentShellWithoutSpinner() {
        if (layout_with_out_data != null) {
            layout_with_out_data.setVisibility(View.GONE);
        }
        if (layout_with_data != null) {
            layout_with_data.setVisibility(View.VISIBLE);
        }
    }

    private void bindDistrictList(boolean forceNewAdapter) {
        if (districsAdopter == null || forceNewAdapter) {
            districsAdopter = new DistricsAdopter(Districs.this, arrayList);
            recycleLayoutManager.setAdapter(districsAdopter);
        } else {
            districsAdopter.notifyDataSetChanged();
        }
        showContentShellWithoutSpinner();
    }

    private JsonObject set_districs() {
        LanguageSelectionRequest requestModel = new LanguageSelectionRequest();

        requestModel.setDraftId(SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME));
        int languageId = SharedPrefsData.getInt(getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME);
        int stateId = SharedPrefsData.getInt(getApplicationContext(), Constants.StateID, Constants.PREF_NAME);
        // Stale StateID from another language (e.g. Haryana 41 under Telugu)
        // must not drive the list — clear when warm states cache proves it is foreign.
        if (languageId > 0 && stateId > 0) {
            try {
                java.util.List<com.anyNews.anynews.models.StatesModel.Datum> states =
                        SelectionListCache.loadStates(getApplicationContext(), languageId);
                if (states != null && !states.isEmpty()) {
                    boolean ok = false;
                    for (com.anyNews.anynews.models.StatesModel.Datum st : states) {
                        if (st != null && st.getId() != null && st.getId() == stateId) {
                            ok = true;
                            break;
                        }
                    }
                    if (!ok) {
                        stateId = 0;
                        SharedPrefsData.putInt(getApplicationContext(), Constants.StateID, 0, Constants.PREF_NAME);
                    }
                }
            } catch (Exception ignored) {
            }
        }
        requestModel.setLanguageId(languageId);
        requestModel.setStateId(stateId);
        String jsonString = new Gson().toJson(requestModel);
        String encrpt_data = encrypt(jsonString);
        showLog("API REQUEST E" + jsonString);
        EncritonRequestData Encriton_Request = new EncritonRequestData();
        Encriton_Request.setData(encrpt_data);
        return new Gson().toJsonTree(Encriton_Request).getAsJsonObject();
    }

    public void getDisrics() {
        JsonObject otp = set_districs();

        showLog("API REQ   " + otp.toString());

        APIService apiService = RestApiClient.getClient().create(APIService.class);
        Call<EncreptionRequest> apiResponse = apiService.Get_districts("v3", otp);

        apiResponse.enqueue(new Callback<EncreptionRequest>() {
            @Override
            public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {
                if (response == null || !response.isSuccessful() || response.body() == null
                        || response.body().getData() == null) {
                    if (!paintedFromCache) {
                        recoverToLanguagePicker("Unable to load districts. Please select another language.");
                    }
                    return;
                }
                try {
                    EncreptionRequest encreptionRequest = response.body();
                    showLog("API RES   " + encreptionRequest.getData());
                    String encrpt_data = decrypt(encreptionRequest.getData());

                    Gson gsonObj = new Gson();
                    DistricsModel languageModel = gsonObj.fromJson(encrpt_data, DistricsModel.class);

                    boolean success = languageModel != null && Boolean.TRUE.equals(languageModel.getSuccess());
                    boolean hasData = languageModel != null
                            && languageModel.getData() != null
                            && languageModel.getData().size() > 0;
                    int languageId = SharedPrefsData.getInt(getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME);
                    int stateId = SharedPrefsData.getInt(getApplicationContext(), Constants.StateID, Constants.PREF_NAME);

                    if (success && hasData) {
                        boolean unchanged = paintedFromCache
                                && SelectionListCache.districtsUnchanged(Districs.this, languageId, stateId, languageModel);
                        SelectionListCache.saveDistricts(Districs.this, languageId, stateId, languageModel);
                        if (unchanged) {
                            return;
                        }

                        arrayList.clear();
                        arrayList.addAll(languageModel.getData());
                        paintedFromCache = true;
                        bindDistrictList(true);
                        return;
                    }

                    if (paintedFromCache) {
                        return;
                    }
                    String msg = languageModel != null ? languageModel.getMsg() : null;
                    if (TextUtils.isEmpty(msg)) {
                        msg = "No districts available for this language. Please select another language.";
                    }
                    recoverToLanguagePicker(msg);
                } catch (Exception e) {
                    if (!paintedFromCache) {
                        recoverToLanguagePicker("Unable to load districts. Please select another language.");
                    }
                }
            }

            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {
                if (paintedFromCache) {
                    return;
                }
                recoverToLanguagePicker(
                        t != null && t.getMessage() != null
                                ? t.getMessage()
                                : "Network error. Please select another language."
                );
            }
        });
    }

    private void recoverToLanguagePicker(String message) {
        layout_with_out_data.setVisibility(View.GONE);
        layout_with_data.setVisibility(View.GONE);
        SharedPrefsData.putInt(Districs.this, Constants.LanguageID, 0, Constants.PREF_NAME);
        SharedPrefsData.putInt(Districs.this, Constants.StateID, 0, Constants.PREF_NAME);
        SharedPrefsData.putBool(Districs.this, Constants.IS_LANGUAGE_SELECTED, false, Constants.PREF_NAME);
        SharedPrefsData.putString(Districs.this, Constants.MOVE_NEXT_SCREEN, "language", Constants.PREF_NAME);
        if (!TextUtils.isEmpty(message)) {
            Toast.makeText(Districs.this, message, Toast.LENGTH_LONG).show();
        }
        Intent intent = LanguageActivity.createChangeLanguageIntent(Districs.this);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    public void writeDistrictSelection(String userId, String type_firebase, int draft_id, String device_id, long current_time) {
        DistrictSelection appOpen = new DistrictSelection(type_firebase, draft_id, device_id, current_time);

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

        public DistrictSelection(String type_firebase, int draft_id, String device_id, long current_time) {
            this.type = type_firebase;
            this.draft_id = draft_id;
            this.device_id = device_id;
            this.created_at = current_time;
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        int back_value = SharedPrefsData.getInt(getApplicationContext(), Constants.Back_Button_Move, Constants.PREF_NAME);

        if (back_value == 1) {

        } else if (back_value == 2) {
            Intent intent = new Intent(Districs.this, States.class);
            startActivity(intent);
        } else {
            startActivity(LanguageActivity.createChangeLanguageIntent(Districs.this));
        }
    }
}
