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
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.anyNews.anynews.Pojo.EncreptionRequest;
import com.anyNews.anynews.Pojo.EncritonRequestData;
import com.anyNews.anynews.Pojo.LanguageModelResponse;
import com.anyNews.anynews.Pojo.LanguageSelectionRequest;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Utils.Allsharedpreference;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.FontSettingsHelper;
import com.anyNews.anynews.Utils.SelectionListCache;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.anyNews.anynews.models.RegionModel;
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

public class AreaSelection extends AppCompatActivity {
    RecyclerView recycleLayoutManager;
    LinearLayoutManager linearLayoutManager;
    AreaSelectionAdopter chooseAreaAdopter;
    ArrayList<RegionModel.Datum> arrayList;
    TextView select_language;
    Allsharedpreference prefShare;
    LinearLayout layout_with_data, layout_with_out_data;

    LanguageModelResponse languageModelResponse;

    // firebase storage
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();
    String type_firebase_language = "language_selection";
    int draft_id_firebase_lang = 0;
    ImageView img_back;

    private boolean paintedFromCache = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_area_selection);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        recycleLayoutManager = findViewById(R.id.area_details);
        select_language = findViewById(R.id.select_language);
        layout_with_data = findViewById(R.id.layout_with_data);
        layout_with_out_data = findViewById(R.id.layout_with_out_data);
        img_back = findViewById(R.id.img_back);

        linearLayoutManager = new LinearLayoutManager(this);
        recycleLayoutManager.setLayoutManager(linearLayoutManager);
        arrayList = new ArrayList<>();

        setStaticData();
        paintFromCacheIfAvailable();
        getAreasList();

        draft_id_firebase_lang = SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME);
        long current_time_firebase_lang = System.currentTimeMillis();
        String device_id_firebase = SharedPrefsData.getString(getApplicationContext(), Constants.DeviceID, Constants.PREF_NAME);

        writeLanguageSelection("User", type_firebase_language, draft_id_firebase_lang, device_id_firebase, current_time_firebase_lang);
        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int back_value = SharedPrefsData.getInt(getApplicationContext(), Constants.Back_Button_Move, Constants.PREF_NAME);

                if (back_value == 1) {
                    startActivity(LanguageActivity.createChangeLanguageIntent(AreaSelection.this));
                    finish();

                } else {
                    finish();
                }

            }
        });

    }

    private void paintFromCacheIfAvailable() {
        int languageId = SharedPrefsData.getInt(getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME);
        List<RegionModel.Datum> cached = SelectionListCache.loadRegions(this, languageId);
        if (cached == null || cached.isEmpty()) {
            showContentShellWithoutSpinner();
            return;
        }
        arrayList.clear();
        arrayList.addAll(cached);
        bindAreaList(false);
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

    private void bindAreaList(boolean forceNewAdapter) {
        if (chooseAreaAdopter == null || forceNewAdapter) {
            chooseAreaAdopter = new AreaSelectionAdopter(AreaSelection.this, arrayList);
            recycleLayoutManager.setAdapter(chooseAreaAdopter);
        } else {
            chooseAreaAdopter.notifyDataSetChanged();
        }
        showContentShellWithoutSpinner();
    }

    public void getAreasList() {
        JsonObject otp = set_states_request();

        showLog("API REQ   " + otp.toString());
        APIService apiService = RestApiClient.getClient().create(APIService.class);
        Call<EncreptionRequest> apiResponse = apiService.get_regions_requiest("v3", otp);

        apiResponse.enqueue(new Callback<EncreptionRequest>() {
            @Override
            public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {
                if (response == null || !response.isSuccessful() || response.body() == null
                        || response.body().getData() == null) {
                    if (!paintedFromCache) {
                        recoverToLanguagePicker("Unable to load regions. Please select another language.");
                    }
                    return;
                }
                try {
                    EncreptionRequest encreptionRequest = response.body();
                    showLog("RES" + encreptionRequest.getData());
                    showLog("API REQUEST R" + decrypt(encreptionRequest.getData()));
                    String encrpt_data = decrypt(encreptionRequest.getData());

                    showLog(" AREAS LIST " + encrpt_data);
                    Gson gsonObj = new Gson();
                    RegionModel languageModel = gsonObj.fromJson(encrpt_data, RegionModel.class);

                    boolean success = languageModel != null && Boolean.TRUE.equals(languageModel.getSuccess());
                    boolean hasData = languageModel != null
                            && languageModel.getData() != null
                            && languageModel.getData().size() > 0;
                    int languageId = SharedPrefsData.getInt(getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME);

                    if (success && hasData) {
                        boolean unchanged = paintedFromCache
                                && SelectionListCache.regionsUnchanged(AreaSelection.this, languageId, languageModel);
                        SelectionListCache.saveRegions(AreaSelection.this, languageId, languageModel);
                        if (unchanged) {
                            return;
                        }

                        arrayList.clear();
                        arrayList.addAll(languageModel.getData());
                        paintedFromCache = true;
                        bindAreaList(true);
                        return;
                    }

                    if (paintedFromCache) {
                        return;
                    }
                    recoverToLanguagePicker(
                            "No regions available for this language. Please select another language."
                    );
                } catch (Exception e) {
                    if (!paintedFromCache) {
                        recoverToLanguagePicker("Unable to load regions. Please select another language.");
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
        SharedPrefsData.putInt(AreaSelection.this, Constants.LanguageID, 0, Constants.PREF_NAME);
        SharedPrefsData.putBool(AreaSelection.this, Constants.IS_LANGUAGE_SELECTED, false, Constants.PREF_NAME);
        SharedPrefsData.putString(AreaSelection.this, Constants.MOVE_NEXT_SCREEN, "language", Constants.PREF_NAME);
        if (!TextUtils.isEmpty(message)) {
            Toast.makeText(AreaSelection.this, message, Toast.LENGTH_LONG).show();
        }
        Intent intent = LanguageActivity.createChangeLanguageIntent(AreaSelection.this);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private JsonObject set_states_request() {
        LanguageSelectionRequest requestModel = new LanguageSelectionRequest();

        requestModel.setDraftId(SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME));
        requestModel.setLanguageId(SharedPrefsData.getInt(getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME));

        String jsonString = new Gson().toJson(requestModel);
        String encrpt_data = encrypt(jsonString);

        showLog("API REQUEST E" + jsonString);
        EncritonRequestData Encriton_Request = new EncritonRequestData();
        Encriton_Request.setData(encrpt_data);

        return new Gson().toJsonTree(Encriton_Request).getAsJsonObject();
    }

    private void setStaticData() {
        prefShare = new Allsharedpreference(this);
        Gson gson = new Gson();
        String Translation = prefShare.getShared_String(Allsharedpreference.Translator);
        languageModelResponse = gson.fromJson(Translation, LanguageModelResponse.class);
        if (languageModelResponse != null && languageModelResponse.getData() != null) {
            select_language.setText(" " + languageModelResponse.getData().getSelectLanguage());
        }
        int languageId = SharedPrefsData.getInt(getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME);
        FontSettingsHelper.applySelectionHeader(getApplicationContext(), languageId > 0 ? languageId : 1, select_language);
    }

    @Override
    public void onBackPressed() {
        int back_value = SharedPrefsData.getInt(getApplicationContext(), Constants.Back_Button_Move, Constants.PREF_NAME);

        if (back_value == 1) {
            startActivity(LanguageActivity.createChangeLanguageIntent(AreaSelection.this));
            finish();

        } else {
            finish();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (chooseAreaAdopter != null) {
            recycleLayoutManager.setAdapter(chooseAreaAdopter);
        }
    }

    public void writeLanguageSelection(String userId, String type_firebase, int draft_id, String device_id, long current_time) {
        LanguageSelection appOpen = new LanguageSelection(type_firebase, draft_id, device_id, current_time);

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

        public LanguageSelection(String type_firebase, int draft_id, String device_id, long current_time) {
            this.type = type_firebase;
            this.draft_id = draft_id;
            this.device_id = device_id;
            this.created_at = current_time;
        }

    }
}
