package com.anyNews.anynews.Activities;

import androidx.annotation.Keep;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.anyNews.anynews.Utils.OpenPathCache;
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

    /** True once any non-empty list is on screen (cache or bundled). */
    private boolean paintedWithItems = false;

    /** True after List_All_Languages returned a non-empty list this session. */
    private boolean paintedFromLiveApi = false;

    /** Fresh-install soft-refresh retries — never block UI with a spinner. */
    private int languageFetchAttempts = 0;
    private static final int MAX_LANGUAGE_FETCH_ATTEMPTS = 4;
    private final Handler retryHandler = new Handler(Looper.getMainLooper());
    private boolean destroyed = false;
    private boolean fetchInFlight = false;

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

        // Safety net: existing Play / reporter devices with language already chosen
        // must never sit on this screen (stale MOVE_NEXT_SCREEN=language paths).
        if (OpenPathCache.shouldSkipLanguageOnboarding(this)) {
            showLog("LanguageActivity: skip for returning device → HomePage");
            Intent home = new Intent(LanguageActivity.this, HomePage.class);
            home.putExtra("EXTRA_SESSION_ID", "0");
            startActivity(home);
            finish();
            return;
        }

        // Instant paint: cache if present, else bundled ML/HI/TE/TA/KN/OD.
        // NEVER blank title-only shell. NEVER blocking Loading spinner.
        paintInstantLanguageList();
        getLangageDetails();

        draft_id_firebase_lang = SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME);
        long current_time_firebase_lang = System.currentTimeMillis();
        String device_id_firebase = SharedPrefsData.getString(getApplicationContext(), Constants.DeviceID, Constants.PREF_NAME);

        writeLanguageSelection("User", type_firebase_language, draft_id_firebase_lang, device_id_firebase, current_time_firebase_lang);
    }

    @Override
    protected void onDestroy() {
        destroyed = true;
        retryHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private void setStaticData() {
        prefShare = new Allsharedpreference(this);
        Gson gson = new Gson();
        String Translation = prefShare.getShared_String(Allsharedpreference.Translator);
        languageModelResponse = gson.fromJson(Translation, LanguageModelResponse.class);
    }

    /**
     * Paint languages on the first frame — SelectionListCache when non-empty,
     * otherwise the bundled production language set. Content shell stays visible;
     * loading layout stays gone.
     */
    private void paintInstantLanguageList() {
        ensureContentVisible();
        List<LanguageModel.Datum> cached = SelectionListCache.loadLanguages(this);
        if (cached != null && !cached.isEmpty()) {
            arrayList.clear();
            arrayList.addAll(cached);
            bindLanguageList();
            paintedWithItems = true;
            showLog("LanguageActivity: instant paint from SelectionListCache size=" + arrayList.size());
            return;
        }
        paintBundledFallbackLanguages();
        showLog("LanguageActivity: instant paint from bundled fallback size=" + arrayList.size());
    }

    /** Keep the picker content visible; never flip to the old Loading include. */
    private void ensureContentVisible() {
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

    /**
     * Known-good production language IDs (ML=1 … OD=6). Used for fresh install
     * first paint and as the lasting UI if the network never returns a list.
     */
    private void paintBundledFallbackLanguages() {
        arrayList.clear();
        arrayList.add(new LanguageModel.Datum(1, "മലയാളം"));
        arrayList.add(new LanguageModel.Datum(2, "हिन्दी"));
        arrayList.add(new LanguageModel.Datum(3, "తెలుగు"));
        arrayList.add(new LanguageModel.Datum(4, "தமிழ்"));
        arrayList.add(new LanguageModel.Datum(5, "ಕನ್ನಡ"));
        arrayList.add(new LanguageModel.Datum(6, "ଓଡ଼ିଆ"));
        bindLanguageList();
        paintedWithItems = true;
        ensureContentVisible();
        // Seed disk so a cold reopen is also instant even before API succeeds.
        SelectionListCache.saveLanguages(this, arrayList);
    }

    private void handleLanguagesLoadFailure(String reason) {
        if (destroyed) {
            return;
        }
        showLog("LanguageActivity soft-refresh failed: " + reason
                + " attempt=" + languageFetchAttempts
                + " itemsOnScreen=" + (arrayList != null ? arrayList.size() : 0));
        // List already on screen (cache or bundled). Never show Loading.
        ensureContentVisible();
        if (paintedFromLiveApi) {
            return;
        }
        if (languageFetchAttempts < MAX_LANGUAGE_FETCH_ATTEMPTS) {
            long delayMs = Math.min(800L * Math.max(1, languageFetchAttempts), 4000L);
            retryHandler.postDelayed(() -> {
                if (!destroyed && !paintedFromLiveApi) {
                    getLangageDetails();
                }
            }, delayMs);
        }
    }

    private void getLangageDetails() {
        if (destroyed || fetchInFlight) {
            return;
        }
        languageFetchAttempts++;
        fetchInFlight = true;
        // Soft-refresh only — do not hide items or show a spinner.

        APIService apiService = RestApiClient.getClient().create(APIService.class);
        Call<EncreptionRequest> apiResponse = apiService.Languages_All_Languages("v3");

        apiResponse.enqueue(new Callback<EncreptionRequest>() {
            @Override
            public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {
                fetchInFlight = false;
                if (destroyed) {
                    return;
                }
                if (response.body() == null || response.body().getData() == null) {
                    handleLanguagesLoadFailure("null body");
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

                        boolean unchanged = paintedWithItems
                                && SelectionListCache.languagesUnchanged(LanguageActivity.this, languageModel);
                        SelectionListCache.saveLanguages(LanguageActivity.this, languageModel);
                        paintedFromLiveApi = true;
                        languageFetchAttempts = 0;
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
                        paintedWithItems = true;
                        ensureContentVisible();
                    } else {
                        handleLanguagesLoadFailure("empty or unsuccessful list");
                    }
                } catch (Exception e) {
                    handleLanguagesLoadFailure("parse: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {
                fetchInFlight = false;
                if (destroyed) {
                    return;
                }
                handleLanguagesLoadFailure(t != null ? t.getMessage() : "network");
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
