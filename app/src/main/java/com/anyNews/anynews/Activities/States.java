package com.anyNews.anynews.Activities;

import androidx.annotation.Keep;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.anyNews.anynews.Utils.SelectionListCache;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.anyNews.anynews.Utils.StateLandmarkPrefetcher;
import com.anyNews.anynews.models.StatesModel;
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
import static com.facebook.FacebookSdk.getApplicationContext;

public class States extends AppCompatActivity {

    RecyclerView recycleLayoutManager;
    GridLayoutManager linearLayoutManager;
    StatesAdopter statesAdopter;
    ArrayList<StatesModel.Datum> arrayList;
    Allsharedpreference prefShare;
    TextView select_state;
    LinearLayout layout_with_data, layout_with_out_data;

    LanguageModelResponse languageModelResponse;
    ImageView img_back;
    // firebase storage
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();
    String type_firebase_state = "state_selection";
    int draft_id_firebase_state = 0;

    private boolean paintedFromCache = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_states);
        recycleLayoutManager = findViewById(R.id.States_Details);
        layout_with_out_data = findViewById(R.id.layout_with_out_data);
        layout_with_data = findViewById(R.id.layout_with_data);
        img_back = findViewById(R.id.img_back);

        prefShare = new Allsharedpreference(States.this);
        select_state = findViewById(R.id.select_state);
        arrayList = new ArrayList<>();

        linearLayoutManager = new GridLayoutManager(States.this, 2, GridLayoutManager.VERTICAL, false);
        recycleLayoutManager.setLayoutManager(linearLayoutManager);

        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(LanguageActivity.createChangeLanguageIntent(States.this));
            }
        });

        setStaticdata();

        paintFromCacheIfAvailable();
        getStates();

        draft_id_firebase_state = SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME);
        long current_time_firebase_lang = System.currentTimeMillis();
        String device_id_firebase = SharedPrefsData.getString(getApplicationContext(), Constants.DeviceID, Constants.PREF_NAME);

        writeStateSelection("User", type_firebase_state, draft_id_firebase_state, device_id_firebase, current_time_firebase_lang);
    }

    private void setStaticdata() {
        Gson gson = new Gson();
        String Translation = prefShare.getShared_String(Allsharedpreference.Translator);
        languageModelResponse = gson.fromJson(Translation, LanguageModelResponse.class);

        String title = null;
        if (languageModelResponse != null && languageModelResponse.getData() != null) {
            title = languageModelResponse.getData().getSelectState();
        }
        if (title == null || title.isEmpty()) {
            title = getIntent().getStringExtra("LANGUAGE_NAME");
        }
        if (title == null || title.isEmpty()) {
            title = "Select State";
        }
        select_state.setText(" " + title);
        AppLanguagesFonts.setTypeFace(select_state);
        int languageId = SharedPrefsData.getInt(getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME);
        FontSettingsHelper.applySelectionHeader(getApplicationContext(), languageId > 0 ? languageId : 1, select_state);
    }

    private void paintFromCacheIfAvailable() {
        int languageId = SharedPrefsData.getInt(getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME);
        List<StatesModel.Datum> cached = SelectionListCache.loadStates(this, languageId);
        if (cached == null || cached.isEmpty()) {
            showContentShellWithoutSpinner();
            return;
        }
        String nextScreen = SelectionListCache.loadStatesNextScreen(this, languageId);
        if (!TextUtils.isEmpty(nextScreen)) {
            SharedPrefsData.putString(States.this, Constants.Distric_Name, nextScreen, Constants.PREF_NAME);
        }
        arrayList.clear();
        arrayList.addAll(cached);
        paintStateGrid(false);
        paintedFromCache = true;
        // Icons may still tick in via Picasso; never block on prefetch.
        StateLandmarkPrefetcher.prefetch(this, arrayList, null);
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
        showContentShellWithoutSpinner();
    }

    private void paintStateGrid(boolean forceNewAdapter) {
        applyStateGridLayout(arrayList.size());
        if (statesAdopter == null || forceNewAdapter) {
            statesAdopter = new StatesAdopter(States.this, arrayList);
            recycleLayoutManager.setAdapter(statesAdopter);
        } else {
            statesAdopter.notifyDataSetChanged();
        }
        showContentHideLoading();
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

    public void getStates() {
        JsonObject otp = set_states_request();
        showLog("API REQ   " + otp.toString());
        APIService apiService = RestApiClient.getClient().create(APIService.class);
        Call<EncreptionRequest> apiResponse = apiService.Get_states_requiest("v3", otp);

        apiResponse.enqueue(new Callback<EncreptionRequest>() {
            @Override
            public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {
                if (response == null || !response.isSuccessful() || response.body() == null
                        || response.body().getData() == null) {
                    if (!paintedFromCache) {
                        recoverToLanguagePicker("Unable to load states. Please select another language.");
                    }
                    return;
                }

                try {
                    EncreptionRequest encreptionRequest = response.body();
                    showLog("API RES   " + encreptionRequest.getData());
                    String encrpt_data = decrypt(encreptionRequest.getData());

                    Gson gsonObj = new Gson();
                    StatesModel languageModel = gsonObj.fromJson(encrpt_data, StatesModel.class);

                    boolean success = languageModel != null && Boolean.TRUE.equals(languageModel.getSuccess());
                    boolean hasData = languageModel != null
                            && languageModel.getData() != null
                            && languageModel.getData().size() > 0;
                    String nextScreen = languageModel != null ? languageModel.getNextScreen() : null;
                    int languageId = SharedPrefsData.getInt(getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME);

                    if (success && hasData) {
                        SharedPrefsData.putString(States.this, Constants.Distric_Name, nextScreen, Constants.PREF_NAME);
                        boolean unchanged = paintedFromCache
                                && SelectionListCache.statesUnchanged(States.this, languageId, languageModel);
                        SelectionListCache.saveStates(States.this, languageId, languageModel);
                        if (unchanged) {
                            // Still warm icons in background without repainting.
                            StateLandmarkPrefetcher.prefetch(States.this, languageModel.getData(), null);
                            return;
                        }

                        arrayList.clear();
                        arrayList.addAll(languageModel.getData());
                        paintedFromCache = true;
                        // Paint names + rows immediately; Picasso fills icons async (no full-screen wait).
                        paintStateGrid(true);
                        StateLandmarkPrefetcher.prefetch(States.this, arrayList, null);
                        return;
                    }

                    if (paintedFromCache) {
                        return;
                    }
                    String msg = languageModel != null ? languageModel.getMsg() : null;
                    if (TextUtils.isEmpty(msg)) {
                        msg = "No states available for this language. Please select another language.";
                    }
                    recoverToLanguagePicker(msg);
                } catch (Exception e) {
                    if (!paintedFromCache) {
                        recoverToLanguagePicker("Unable to load states. Please select another language.");
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

    /**
     * Multi-state: 2-column grid (unchanged). Single state: 1-column with side padding
     * so the card keeps half-width and sits centered instead of left-aligned.
     */
    private void applyStateGridLayout(int stateCount) {
        if (recycleLayoutManager == null) {
            return;
        }
        int sidePad = 0;
        int span = 2;
        if (stateCount == 1) {
            span = 1;
            int width = recycleLayoutManager.getWidth();
            if (width <= 0) {
                width = getResources().getDisplayMetrics().widthPixels;
            }
            // Match one cell of the 2-column grid: 25% pad each side → 50% card width.
            sidePad = Math.max(0, width / 4);
        }
        recycleLayoutManager.setPadding(sidePad, recycleLayoutManager.getPaddingTop(),
                sidePad, recycleLayoutManager.getPaddingBottom());
        recycleLayoutManager.setClipToPadding(false);
        if (linearLayoutManager == null || linearLayoutManager.getSpanCount() != span) {
            linearLayoutManager = new GridLayoutManager(States.this, span, GridLayoutManager.VERTICAL, false);
            recycleLayoutManager.setLayoutManager(linearLayoutManager);
        }
    }

    private void recoverToLanguagePicker(String message) {
        layout_with_out_data.setVisibility(View.GONE);
        layout_with_data.setVisibility(View.GONE);
        SharedPrefsData.putInt(States.this, Constants.LanguageID, 0, Constants.PREF_NAME);
        SharedPrefsData.putInt(States.this, Constants.StateID, 0, Constants.PREF_NAME);
        SharedPrefsData.putBool(States.this, Constants.IS_LANGUAGE_SELECTED, false, Constants.PREF_NAME);
        SharedPrefsData.putString(States.this, Constants.MOVE_NEXT_SCREEN, "language", Constants.PREF_NAME);
        if (!TextUtils.isEmpty(message)) {
            Toast.makeText(States.this, message, Toast.LENGTH_LONG).show();
        }
        Intent intent = LanguageActivity.createChangeLanguageIntent(States.this);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        showLog(" ONE1 ");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = LanguageActivity.createChangeLanguageIntent(States.this);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        showLog(" ONE2 ");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        // Soft-refresh only; cache paints instantly above.
        paintFromCacheIfAvailable();
        getStates();
    }

    public void writeStateSelection(String userId, String type_firebase, int draft_id, String device_id, long current_time) {
        StateSelection appOpen = new StateSelection(type_firebase, draft_id, device_id, current_time);

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

        public StateSelection(String type_firebase, int draft_id, String device_id, long current_time) {
            this.type = type_firebase;
            this.draft_id = draft_id;
            this.device_id = device_id;
            this.created_at = current_time;
        }

    }
}
