package com.anyNews.anynews.Activities;

import androidx.annotation.Keep;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import com.anyNews.anynews.Adapters.FollowingMemberAdopter;
import com.anyNews.anynews.BuildConfig;
import com.anyNews.anynews.Pojo.EncreptionRequest;
import com.anyNews.anynews.Pojo.EncritonRequestData;
import com.anyNews.anynews.Pojo.EventPageLinkResponse;
import com.anyNews.anynews.Pojo.LanguageModelResponse;
import com.anyNews.anynews.Pojo.PaymentHistoryRequest;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Utils.Allsharedpreference;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.Logone;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.anyNews.anynews.models.FollowingMenderModel;
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

public class QuizPageActivity extends AppCompatActivity {

    private static final String TAG ="QuizPageActivity";
    WebView webView;
    ImageView backButtonProfile;
    LanguageModelResponse languageModelResponse;
    Allsharedpreference prefShare;
    ArrayList<EventPageLinkResponse> arrayList;
    String eventLink;

    // firebase storage
    /*FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();
    String type_firebase_events = "events_page_selection";
    int draft_id_firebase_events = 0;*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_page);
        backButtonProfile = findViewById(R.id.back__button_profile);
        webView = findViewById(R.id.webView);

        //setStaticdata();
       // getEventPage();

        backButtonProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(QuizPageActivity.this, HomePage.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                if (getApplicationContext() != null && getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)){
                    startActivity(intent);
                }
                finish();
            }
        });

        // firebase events
        /*draft_id_firebase_events = SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME);
        long current_time_firebase_events = System.currentTimeMillis();
        String device_id_firebase = SharedPrefsData.getString(getApplicationContext(), Constants.DeviceID, Constants.PREF_NAME);

        writeEventsPageSelection("User",type_firebase_events ,draft_id_firebase_events ,device_id_firebase, current_time_firebase_events);*/

    }


    class SSLTolerentWebViewClient extends WebViewClient {
        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            if (error.toString() == "piglet")
                handler.cancel();
            else
                handler.proceed(); // Ignore SSL certificate errors
        }
    }

    @Override
    public void onBackPressed() {
        //  super.onBackPressed();
        Intent intent=new Intent(QuizPageActivity.this,HomePage.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        if (getApplicationContext() != null && getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)){
            startActivity(intent);
        }

    }

    private void setStaticdata() {
        prefShare = new Allsharedpreference(QuizPageActivity.this);
        Gson gson = new Gson();
        String Translation = prefShare.getShared_String(Allsharedpreference.Translator);
        languageModelResponse = gson.fromJson(Translation, LanguageModelResponse.class);



    }
    private JsonObject set_payment_history() {

        EventPageLinkResponse requestModel = new EventPageLinkResponse();

        requestModel.setDraftId(SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME));


        String jsonString = new com.google.gson.Gson().toJson(requestModel);
        String encrpt_data = null;
        showLog("API REQUEST E" + jsonString.toString());
        encrpt_data = encrypt(jsonString);
        EncritonRequestData Encriton_Request=new EncritonRequestData();
        Encriton_Request.setData(encrpt_data);
        return new Gson().toJsonTree(Encriton_Request).getAsJsonObject();
    }



    public void getEventPage() {

        arrayList=new ArrayList<>();

        JsonObject otp = set_payment_history();
        showLog("API REQ   "+otp.toString());
        /*EventPageLinkResponse requestModel = new EventPageLinkResponse();
        requestModel.setDraftId((int) SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME));*/
        /*JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("draft_id", SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME));*/

        APIService apiService = RestApiClient.getClient().create(APIService.class);
        Call<EncreptionRequest> apiResponse = apiService.EventPageLink("v3" ,otp);

        apiResponse.enqueue(new Callback<EncreptionRequest>() {
            @Override
            public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {


                EncreptionRequest encreptionRequest=response.body();
                showLog("API RES   "+response.body().getData());
                String encrpt_data = null;
                /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    encrpt_data = decrypt(encreptionRequest.getData());
                }*/
                encrpt_data = decrypt(encreptionRequest.getData());

                Gson gsonObj = new Gson();
                EventPageLinkResponse languageModel = gsonObj.fromJson(encrpt_data, EventPageLinkResponse.class);



                if (languageModel.getSuccess()) {
                    eventLink = languageModel.getLink();
                    Logone.d(TAG,eventLink);
                    if ((eventLink != null) && (languageModel.getLogin_status())){
                        webView.setWebViewClient(new SSLTolerentWebViewClient());
                        webView.getSettings().setLoadsImagesAutomatically(true);
                        webView.getSettings().setJavaScriptEnabled(true);
                        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

                        webView.getSettings().setLoadWithOverviewMode(true);
                        webView.getSettings().setUseWideViewPort(true);

                        webView.getSettings().setBuiltInZoomControls(true);
                        webView.getSettings().setDisplayZoomControls(false);

                        webView.loadUrl(eventLink);
                    }

                    if ((eventLink != null) && (!languageModel.getLogin_status())){
                        Intent intent = new Intent(getApplicationContext(), LoginActivityDetails.class);
                        // intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }

                }
            }

            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {


            }
        });

    }

    public void writeEventsPageSelection(String userId,String type_firebase ,int draft_id ,String device_id,long current_time) {
        EventsPage appOpen = new EventsPage(type_firebase ,draft_id ,device_id, current_time);

        //myRef.child("Events").push().setValue(appOpen);
    }
    @IgnoreExtraProperties
    @Keep
    public class EventsPage {

        public String type;
        public int draft_id;
        public long created_at;
        public String device_id;


        public EventsPage() {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }

        public EventsPage(String type_firebase,  int draft_id,String device_id, long current_time) {
            this.type = type_firebase;
            this.draft_id = draft_id;
            this.device_id = device_id;
            this.created_at = current_time;
        }

    }
}