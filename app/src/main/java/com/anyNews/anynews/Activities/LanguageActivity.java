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
import com.anyNews.anynews.Utils.FontSettingsHelper;
import com.anyNews.anynews.Utils.SelectionListCache;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.anyNews.anynews.models.LanguageModel;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.anyNews.anynews.Utils.AppHelper.showLog;
import static com.anyNews.anynews.Utils.MyUtils.decrypt;

public class LanguageActivity extends AppCompatActivity {


    RecyclerView recycleLayoutManager;
    LinearLayoutManager linearLayoutManager;
    LanguageAdopter chooseLaguageAdopter;
    ArrayList<LanguageModel.Datum> arrayList;
    TextView select_language;
    Allsharedpreference prefShare;
    LinearLayout layout_with_data, layout_with_out_data;

    LanguageModelResponse languageModelResponse;

    // firebase storage
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();
    String type_firebase_language = "language_selection";
    int draft_id_firebase_lang = 0;

    /** True when we already painted from cache; network only soft-refreshes. */
    private boolean paintedFromCache = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        recycleLayoutManager = findViewById(R.id.Language_Details);
        select_language = findViewById(R.id.select_language);
        layout_with_data = findViewById(R.id.layout_with_data);
        layout_with_out_data = findViewById(R.id.layout_with_out_data);

        linearLayoutManager = new LinearLayoutManager(this);
        recycleLayoutManager.setLayoutManager(linearLayoutManager);
        arrayList = new ArrayList<>();

        setStaticData();
        FontSettingsHelper.applySelectionHeader(getApplicationContext(), 1, select_language);

        // Instant paint from disk/memory; never block with Loading... when cache exists.
        paintFromCacheIfAvailable();
        getLangageDetails();

        draft_id_firebase_lang = SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME);
        long current_time_firebase_lang = System.currentTimeMillis();
        String device_id_firebase = SharedPrefsData.getString(getApplicationContext(), Constants.DeviceID, Constants.PREF_NAME);

        writeLanguageSelection("User", type_firebase_language, draft_id_firebase_lang, device_id_firebase, current_time_firebase_lang);
    }

    private void setStaticData() {
        prefShare = new Allsharedpreference(this);
        Gson gson = new Gson();
        String Translation = prefShare.getShared_String(Allsharedpreference.Translator);
        languageModelResponse = gson.fromJson(Translation, LanguageModelResponse.class);
    }

    private void paintFromCacheIfAvailable() {
        List<LanguageModel.Datum> cached = SelectionListCache.loadLanguages(this);
        if (cached == null || cached.isEmpty()) {
            // Cold: show shell (title) without full-screen spinner if possible.
            showContentShellWithoutSpinner();
            return;
        }
        arrayList.clear();
        arrayList.addAll(cached);
        bindLanguageList();
        paintedFromCache = true;
        showContentHideLoading();
    }

    private void showContentShellWithoutSpinner() {
        if (layout_with_out_data != null) {
            layout_with_out_data.setVisibility(View.GONE);
        }
        if (layout_with_data != null) {
            layout_with_data.setVisibility(View.VISIBLE);
        }
    }

    private void showContentHideLoading() {
        if (layout_with_out_data != null) {
            layout_with_out_data.setVisibility(View.GONE);
        }
        if (layout_with_data != null) {
            layout_with_data.setVisibility(View.VISIBLE);
        }
    }

    private void bindLanguageList() {
        if (chooseLaguageAdopter == null) {
            chooseLaguageAdopter = new LanguageAdopter(LanguageActivity.this, arrayList);
            recycleLayoutManager.setAdapter(chooseLaguageAdopter);
        } else {
            chooseLaguageAdopter.notifyDataSetChanged();
        }
    }

    private void getLangageDetails() {
        APIService apiService = RestApiClient.getClient().create(APIService.class);
        Call<EncreptionRequest> apiResponse = apiService.Languages_All_Languages("v3");

        apiResponse.enqueue(new Callback<EncreptionRequest>() {
            @Override
            public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {
                if (response.body() == null || response.body().getData() == null) {
                    if (!paintedFromCache) {
                        showContentShellWithoutSpinner();
                    }
                    return;
                }
                try {
                    EncreptionRequest encreptionRequest = response.body();
                    showLog("RES" + response.body().getData());
                    showLog("API REQUEST R" + decrypt(response.body().getData()));
                    String encrpt_data = decrypt(encreptionRequest.getData());

                    Gson gsonObj = new Gson();
                    LanguageModel languageModel = gsonObj.fromJson(encrpt_data, LanguageModel.class);

                    if (languageModel != null && Boolean.TRUE.equals(languageModel.getSuccess())
                            && languageModel.getData() != null && languageModel.getData().size() > 0) {

                        boolean unchanged = paintedFromCache
                                && SelectionListCache.languagesUnchanged(LanguageActivity.this, languageModel);
                        SelectionListCache.saveLanguages(LanguageActivity.this, languageModel);
                        if (unchanged) {
                            // Already painted same data — skip rebuild.
                            return;
                        }

                        arrayList.clear();
                        for (int i = 0; i < languageModel.getData().size(); i++) {
                            arrayList.add(new LanguageModel.Datum(
                                    languageModel.getData().get(i).getId(),
                                    languageModel.getData().get(i).getName()));
                        }
                        bindLanguageList();
                        paintedFromCache = true;
                        showContentHideLoading();
                    } else if (!paintedFromCache) {
                        showContentShellWithoutSpinner();
                    }
                } catch (Exception e) {
                    if (!paintedFromCache) {
                        showContentShellWithoutSpinner();
                    }
                }
            }

            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {
                if (!paintedFromCache) {
                    showContentShellWithoutSpinner();
                }
            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (chooseLaguageAdopter != null) {
            recycleLayoutManager.setAdapter(chooseLaguageAdopter);
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
