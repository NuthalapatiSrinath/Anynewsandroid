package com.anyNews.anynews.Activities;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.android.installreferrer.api.ReferrerDetails;
import com.anyNews.anynews.BuildConfig;
import com.anyNews.anynews.Pojo.EncreptionRequest;
import com.anyNews.anynews.Pojo.EncritonRequestData;
import com.anyNews.anynews.Pojo.LanguageModelResponse;
import com.anyNews.anynews.Pojo.LanguageSelectionRequest;
import com.anyNews.anynews.Pojo.StartActivityReqModel;
import com.anyNews.anynews.Pojo.StartingAppRequest;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Utils.Allsharedpreference;
import com.anyNews.anynews.Utils.AdMobInitializer;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.EngagementCountHelper;
import com.anyNews.anynews.Utils.ThemeColorHelper;
import com.anyNews.anynews.Pojo.LanguageFontConfig;
import com.anyNews.anynews.Utils.FontSettingsHelper;
import com.anyNews.anynews.Utils.Logone;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.facebook.FacebookSdk;
import com.facebook.ads.AdSettings;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.applinks.AppLinkData;
import com.facebook.applinks.AppLinks;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.analytics.CampaignTrackingReceiver;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.anyNews.anynews.Utils.AppHelper.showLog;
import static com.anyNews.anynews.Utils.MyUtils.decrypt;
import static com.anyNews.anynews.Utils.MyUtils.encrypt;
import static com.facebook.FacebookSdk.setAutoLogAppEventsEnabled;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class StartingActivity extends AppCompatActivity {

    private Allsharedpreference prefManager;

    public String OSName, DeviceName, DeviceID, DeviceType, version_name;
    public int versionid;
    String token = "";
    String articleId;
    LinearLayout net_avilable, net_not_avilable, net_try;
    String TITLE, DATAFROM2G,OBJECT,TITLE_REWARDS;
    public static final String SCOPES = "https://www.googleapis.com/auth/plus.login "
            + "https://www.googleapis.com/auth/drive.file";

    CountDownTimer countDownTimer;
    JsonObject splash;
    boolean isForceUpdate = true;


    private static final String LOG_TAG = "CheckNetworkStatus";
    private NetworkChangeReceiver receiver;
    private boolean isConnected = false;
    private TextView networkStatus, welcome;

    ImageView logo_image_server, logo_image;

    String TAG = "TESTING_VALUE";
    String referlink;
    String referrerUid;
    String articleId1;
    String scrren;
    // firebase storage
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();
    String type_firebase = "app_open";
    String type_firebase_open = "push_open";
    String type_firebase_share = "share_open";
    private final Executor backgroundExecutor = Executors.newSingleThreadExecutor();
    private final String prefKey = "checkedInstallReferrer";
    private  String facebookReferrelKey="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
//        showLog("android.os.Build.VERSION.SDK_INT::::"+android.os.Build.VERSION.SDK_INT);
//        showLog("Build.VERSION_CODES.M::::::::"+Build.VERSION_CODES.M);
//        if(android.os.Build.VERSION.SDK_INT<Build.VERSION_CODES.S){
//            showLog("Theme:::::::AppStarting");
//            getTheme().applyStyle(R.style.AppStarting,true);
//            SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
//        }else{
//            showLog("Theme:::::::AppThemeSplash");
//            getTheme().applyStyle(R.style.AppThemeSplash,true);
//        }



        showLog("CHECK POINT 1*");
        //Toast.makeText(this, "Hello", Toast.LENGTH_LONG).show();
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_starting);
        net_not_avilable = findViewById(R.id.net_not_avilable);
        net_avilable = findViewById(R.id.net_avilable);
        net_try = findViewById(R.id.net_try);
        logo_image_server = findViewById(R.id.logo_image_server);
        welcome = findViewById(R.id.welcome);
        logo_image = findViewById(R.id.logo_image);
        prefManager = new Allsharedpreference(getApplicationContext());
        showLog("EMAIL-ID:::::"+prefManager.getEmailId());
        showLog("CHECK POINT 1");
        Intent intent = getIntent();
        TITLE = "" + intent.getStringExtra("TITILE");
        SharedPrefsData.putString(getApplicationContext(), Constants.REFERRER_POST, "0", Constants.PREF_NAME);

        OBJECT = "" + intent.getStringExtra("OBJECT");
        TITLE_REWARDS = "" +intent.getStringExtra("TITLE_REWARDS");
        logo_image_server.setVisibility(View.VISIBLE);
        logo_image.setVisibility(View.GONE);
        receiver = new NetworkChangeReceiver();
        networkStatus = (TextView) findViewById(R.id.networkStatus);
        if(intent.getAction()!=null){
            String action = intent.getAction();
                showLog("action::"+action);
        }
        //extractData(intent.getData());
        if (intent.getData()!=null){
            Uri data = intent.getData();
            showLog("data%%::"+data.toString());
            extractData(data);

        }


        int draft_id_firebase = SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME);
        String device_id_firebase =  Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        long current_time_firebase = System.currentTimeMillis();

        writeAppOpen("User",type_firebase ,current_time_firebase,draft_id_firebase,device_id_firebase);
               showLog("CHECK POINT 2");
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        // Get deep link from result (may be null if no link is found)
                        showLog("CHECK POINT 3");
                        Uri deepLink = null;
                        if (pendingDynamicLinkData != null) {
                            deepLink = pendingDynamicLinkData.getLink();
                            referrerUid = deepLink.getQueryParameter("user_id");
                            showLog("CHECK POINT referrerUid:"+referrerUid);
                            Uri finalDeepLink = deepLink;
                            try {
                                articleId1= finalDeepLink.getQueryParameter("articleId");
                                showLog("CHECK POINT articleId1 %%%:"+articleId1);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                            referlink = deepLink.toString();

                            SharedPrefsData.putString(getApplicationContext(), Constants.REFERAL_LINK, referlink, Constants.PREF_NAME);

                            try {
                                String referLinkValue = referlink;
                                int firstEq = referLinkValue.indexOf("=");
                                if (firstEq < 0 || firstEq >= referLinkValue.length() - 1) {
                                    return;
                                }
                                referlink = referLinkValue.substring(firstEq + 1);
                                showLog("REFER_LINK " + referlink);

                                int secondEq = referlink.indexOf("=");
                                if (secondEq < 0 || secondEq >= referlink.length() - 1) {
                                    return;
                                }
                                String ddd = referlink.substring(secondEq + 1);
                                showLog("REFER_LINKDDD" + ddd);

                                String[] parts = ddd.split("&");
                                if (parts.length < 2) {
                                    return;
                                }
                                String part1 = parts[0].trim();
                                String part2 = parts[1];

                                showLog("SHARE TYPE " + part1 + "  ,  " + part2);
                                long share_id = Long.parseLong(part1);

                                SharedPrefsData.putLong(getApplicationContext(), Constants.CUSST_ID, share_id, Constants.PREF_NAME);
                                try {
                                    String[] custid = part2.split("=");
                                    if (custid.length >= 2) {
                                        String custid2 = custid[1];
                                        showLog("REFERAL COMPLETE DATA " + part1 + "  ,  " + custid2);
                                        welcome.setText(" USER ID :" + part1 + "  TYPE :" + custid2);
                                        welcome.setVisibility(View.GONE);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

//2023-06-22 16:07:28.330 24979-24979/com.anyNews.anynews I/System.out: App Message<-->REFER_LINKcom.anyNews.anynews&articleId=914766&user_id0&type=share&medium=app

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            //   https://anynews.co.in/myrefer.php?custid="+custid123-"+proid456
                        }

                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(StartingActivity.this, "Failed to connect ", Toast.LENGTH_SHORT).show();
                        Logone.w(TAG, "getDynamicLink:onFailure", e);
                    }
                });



                try {
                    checkInstallReferrer();
                }catch (Exception e){
                    e.printStackTrace();
                }

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isNetworkAvailable(StartingActivity.this);
                        getUIDs();
                        setAutoLogAppEventsEnabled(true);
                        UUID uuid = UUID.randomUUID();
                        String uuidAsString = uuid.toString();
                        SharedPrefsData.putString(getApplicationContext(), Constants.USER_SESSION_ID, uuidAsString, Constants.PREF_NAME);
                    }
                },1000);


        try {
            FacebookSdk.setAutoInitEnabled(true);
            FacebookSdk.fullyInitialize();
            AppLinkData.fetchDeferredAppLinkData(this,
                    new AppLinkData.CompletionHandler() {
                        @Override
                        public void onDeferredAppLinkDataFetched(AppLinkData appLinkData) {
                            try{
                                showLog("onDeferredAppLinkDataFetched::"+appLinkData);
                                if (appLinkData != null && !appLinkData.equals("null")
                                        && !appLinkData.equals("")) {
                                    facebookReferrelKey=appLinkData.getTargetUri().toString();
                                    showLog("onDeferredAppLinkDataFetched::"+appLinkData.getTargetUri().toString());
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }

                            // Process app link data
                        }
                    }
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
//        {
//        net_not_avilable = findViewById(R.id.net_not_avilable);
//        net_avilable = findViewById(R.id.net_avilable);
//        net_try = findViewById(R.id.net_try);
//        logo_image_server = findViewById(R.id.logo_image_server);
//        welcome = findViewById(R.id.welcome);
//        logo_image = findViewById(R.id.logo_image);
//        prefManager = new Allsharedpreference(getApplicationContext());
//        showLog("EMAIL-ID:::::"+prefManager.getEmailId());
//        showLog("CHECK POINT 1");
//        Intent intent = getIntent();
//        TITLE = "" + intent.getStringExtra("TITILE");
//        OBJECT = "" + intent.getStringExtra("OBJECT");
//        TITLE_REWARDS = "" +intent.getStringExtra("TITLE_REWARDS");
//        logo_image_server.setVisibility(View.VISIBLE);
//        logo_image.setVisibility(View.GONE);
//        receiver = new NetworkChangeReceiver();
//        networkStatus = (TextView) findViewById(R.id.networkStatus);
//        int draft_id_firebase = SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME);
//        String device_id_firebase =  Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
//        long current_time_firebase = System.currentTimeMillis();
//
//        writeAppOpen("User",type_firebase ,current_time_firebase,draft_id_firebase,device_id_firebase);
//               showLog("CHECK POINT 2");
//        FirebaseDynamicLinks.getInstance()
//                .getDynamicLink(getIntent())
//                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
//                    @Override
//                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
//                        // Get deep link from result (may be null if no link is found)
//                        showLog("CHECK POINT 3");
//                        Uri deepLink = null;
//                        if (pendingDynamicLinkData != null) {
//                            deepLink = pendingDynamicLinkData.getLink();
//                            referrerUid = deepLink.getQueryParameter("user_id");
//                            showLog("CHECK POINT referrerUid:"+referrerUid);
//                            Uri finalDeepLink = deepLink;
//                            try {
//                                articleId1= finalDeepLink.getQueryParameter("articleId");
//                                showLog("CHECK POINT articleId1:"+articleId1);
//                            }catch (Exception e){
//                                e.printStackTrace();
//                            }
//                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//                                @Override
//                                public void run() {
//
//                                }
//                            }, 0);
//
//
//                            referlink = deepLink.toString();
//
//                            SharedPrefsData.putString(getApplicationContext(), Constants.REFERAL_LINK, referlink, Constants.PREF_NAME);
//
//                            try {
//
//                                referlink = referlink.substring(referlink.indexOf("=") + 1);
//                                showLog("REFER_LINK " + referlink);
//
//                                //   String ddd=referlink.substring(28);
//                                String ddd = referlink.substring(referlink.indexOf("=") + 1);
//                                showLog("REFER_LINKDDD" + ddd);
//
//                                String[] parts = ddd.split("&");
//                                String part1 = parts[0]; // 004
//                                String part2 = parts[1];
//
//                                showLog("SHARE TYPE " + part1 + "  ,  " + part2);
//                                long share_id = Long.parseLong(part1);
//
//                                SharedPrefsData.putLong(getApplicationContext(), Constants.CUSST_ID, share_id, Constants.PREF_NAME);
//                                try {
//                                    String[] custid = part2.split("=");
//                                    String custid1 = custid[0]; // 004
//                                    String custid2 = custid[1];
//                                    showLog("REFERAL COMPLETE DATA " + part1 + "  ,  " + custid2);
//                                    welcome.setText(" USER ID :" + part1 + "  TYPE :" + custid2);
//                                    welcome.setVisibility(View.GONE);
//                                }catch (Exception e){
//                                    e.printStackTrace();
//                                }
//
////2023-06-22 16:07:28.330 24979-24979/com.anyNews.anynews I/System.out: App Message<-->REFER_LINKcom.anyNews.anynews&articleId=914766&user_id0&type=share&medium=app
//
//
//
//
//
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//
//                            //   https://anynews.co.in/myrefer.php?custid="+custid123-"+proid456
//                        }
//
//                    }
//                })
//                .addOnFailureListener(this, new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//
//                        Toast.makeText(StartingActivity.this, "Failed to connect ", Toast.LENGTH_SHORT).show();
//                        Logone.w(TAG, "getDynamicLink:onFailure", e);
//                    }
//                });
//
//
//
//                try {
//                    checkInstallReferrer();
//                }catch (Exception e){
//                    e.printStackTrace();
//                }
//
//        isNetworkAvailable(this);
//                getUIDs();
//    }
    public void extractData(Uri uri){
        try {
            //String url1="https://play.google.com/store/apps/details?id=com.anyNews.anynews&articleId=1108939&type=share&medium=app&utm_content=1108939";
            //Uri uri=Uri.parse(url1);
            String articleId2= uri.getQueryParameter("articleId");
            showLog("CHECK POINT ArticleId1 @@@:"+articleId2);
            if(articleId2!=null){
                TITLE=articleId1;
            }else{
                DATAFROM2G=uri.toString();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        Logone.v(LOG_TAG, "onDestory");
        super.onDestroy();

      //  unregisterReceiver(receiver);

    }

    public class NetworkChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, final Intent intent) {

            Logone.v(LOG_TAG, "Receieved notification about network status");
            isNetworkAvailable(context);

        }


    }
    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        if (!isConnected) {
                            Logone.v(LOG_TAG, "Now you are connected to Internet!");
                            //    networkStatus.setText("Now you are connected to Internet!");
                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    // Your Code
                                    // articleId1= finalDeepLink.getQueryParameter("articleId");
                                    getOSandDeviceId();
                                }
                            }, 1000);

                            net_avilable.setVisibility(View.VISIBLE);
                            net_not_avilable.setVisibility(View.GONE);
                            isConnected = true;
                            //do your processing here ---
                            //if you need to post any data to the server or get status
                            //update from the server
                        }
                        return true;
                    }
                }
            }
        }
        Logone.v(LOG_TAG, "You are not connected to Internet!");
        net_avilable.setVisibility(View.GONE);
        net_not_avilable.setVisibility(View.VISIBLE);

        //   one.setText("You are not connected to Internet!");
        isConnected = false;
        return false;
    }

    private void getOSandDeviceId() {
        showLog("CHECK POINT 4");
        PackageManager manager = getApplicationContext().getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(
                    getApplicationContext().getPackageName(), 0);
            double versionhf = info.versionCode;

            version_name = info.versionName;


            Logone.v("sanjayred", String.valueOf(versionhf));

            versionid = Integer.valueOf((int) versionhf);


        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        int version = Build.VERSION.SDK_INT;
        String versionRelease = Build.VERSION.RELEASE;
        OSName = "ANDROID";
        DeviceType = "1";
        DeviceName = manufacturer + model;
        DeviceID = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);
        SharedPrefsData.putString(getApplicationContext(), Constants.DeviceID, DeviceID, Constants.PREF_NAME);
        String App_version_id_sending = "" + version_name;
        String version_id = getVersionIdPrefix();
        SharedPrefsData.putString(getApplicationContext(), Constants.Version_ID, version_id, Constants.PREF_NAME);

        String token = SharedPrefsData.getString(getApplicationContext(), Constants.FCM_TOKEN, Constants.PREF_NAME);
        showLog("CHECK POINT 11::" + token);

        /* Always refresh FCM token on splash — cached tokens go stale after
           reinstall, clear-data, or Firebase rotation and inflate push Failed %. */
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                String resolvedToken = token;
                if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                    resolvedToken = task.getResult();
                    SharedPrefsData.putString(getApplicationContext(), Constants.FCM_TOKEN, resolvedToken, Constants.PREF_NAME);
                } else if (resolvedToken == null || resolvedToken.equalsIgnoreCase("")) {
                    Logone.w("FCM Token", "getToken failed", task.getException());
                    if (BuildConfig.DEBUG) {
                        resolvedToken = "debug-" + DeviceID;
                        SharedPrefsData.putString(getApplicationContext(), Constants.FCM_TOKEN, resolvedToken, Constants.PREF_NAME);
                    } else {
                        return;
                    }
                }
                showLog("CHECK POINT token :::" + resolvedToken);
                sendDeviceDetails(resolvedToken);
                int draft_id = SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME);
                String device_id = SharedPrefsData.getString(getApplicationContext(), Constants.DeviceID, Constants.PREF_NAME);
                int lang_id = SharedPrefsData.getInt(getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME);
                if (draft_id != 0 && !TITLE.equals("null")) {
                    writePushOpen("User", type_firebase, TITLE, draft_id, System.currentTimeMillis(), lang_id, device_id);
                }
                Logone.d("FCM MSg", resolvedToken);
            }
        });

        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        Uri deepLink = null;
                        if (pendingDynamicLinkData != null) {
                            deepLink = pendingDynamicLinkData.getLink();
                            articleId = deepLink.getQueryParameter("articleId");
                            if (articleId != null) {
                                Logone.v("NUmber", articleId);
                                showLog("CHECK POINT SECOND REQUEST::" + articleId);
                                int draft_id_firebase_share = SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME);
                                long current_time_firebase_share = System.currentTimeMillis();
                                writeShareOpen("User", type_firebase_share, articleId, draft_id_firebase_share, current_time_firebase_share);
                            } else {
                                Logone.v("NUmber", "Empty VAlue");
                            }
                        }
                    }
                });


    }


    private String getVersionIdPrefix() {
        String version = version_name != null ? version_name : "";
        if (version.isEmpty()) {
            return "v0";
        }
        return "v" + version.substring(0, 1);
    }

    private void sendDeviceDetails(final String token) {


        showLog("CHECK POINT 13 REQUEST::");
        // sending app version id through url  and saveing inside database
        String App_version_id_sending = "" + version_name;
        String version_id = getVersionIdPrefix();
        SharedPrefsData.putString(getApplicationContext(), Constants.Version_ID, version_id, Constants.PREF_NAME);
        String DRAFT_ID = "" + SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME);

        if (DRAFT_ID.equals("") || DRAFT_ID.equals("null") || DRAFT_ID.equals("0")) {
            splash = Splash_Scrren_First_Time(token);
        } else {
            splash = Splash_Scrren_Second_Time();
        }

        int languageId = SharedPrefsData.getInt(getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME);
        showLog("REFERLINK:"+referlink);
        showLog("CHECK POINT 11::"+token);
        showLog("languageId:"+languageId);
        showLog("stored link:"+SharedPrefsData.getString(getApplicationContext(), Constants.REFERRER_POST,  Constants.PREF_NAME));
        // Always call splash so remote config (typography, ads, engagement color) stays fresh.
        // Returning users previously skipped this and never picked up admin font changes.
        callSplashRequest();
    }

    private void routeReturningUser() {
        String nextScreen = SharedPrefsData.getString(this, Constants.MOVE_NEXT_SCREEN, Constants.PREF_NAME);
        if (nextScreen == null) {
            nextScreen = "";
        }
        showLog("CHECK POINT returning user nextScreen::" + nextScreen);

        if ("main".equalsIgnoreCase(nextScreen)) {
            openHomeFromSplash();
            return;
        }
        if ("state".equalsIgnoreCase(nextScreen)) {
            startActivity(new Intent(StartingActivity.this, States.class));
            finish();
            return;
        }
        if ("district".equalsIgnoreCase(nextScreen)) {
            startActivity(new Intent(StartingActivity.this, Districs.class));
            finish();
            return;
        }
        if ("language".equalsIgnoreCase(nextScreen)) {
            startActivity(new Intent(StartingActivity.this, LanguageActivity.class));
            finish();
            return;
        }

        // Completed onboarding from older builds may only have the flag set.
        if (SharedPrefsData.getBool(this, Constants.IS_LANGUAGE_SELECTED, Constants.PREF_NAME)) {
            openHomeFromSplash();
            return;
        }

        // Unknown local state — ask server where this device should go.
        callSplashRequest();
    }
    public void callSplashRequest(){
        APIService apiService = RestApiClient.getClient().create(APIService.class);
        Call<EncreptionRequest> apiResponse = apiService.Splash_Screen("v3", splash);
        showLog("CHECK POINT SPLASH REQUEST::");
        apiResponse.enqueue(new Callback<EncreptionRequest>() {
            //@RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {

                if (response.isSuccessful()) {
                    showLog("CHECK POINT 14 REQUEST SUCCESS::");
                    try {
                    EncreptionRequest encreptionRequest = response.body();
                    if (encreptionRequest == null || encreptionRequest.getData() == null) {
                        Toast.makeText(StartingActivity.this, "Unable to start app. Please try again.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    String encrpt_data = decrypt(encreptionRequest.getData());
                    showLog(" NARASIMHA API Response"+encrpt_data.toString());
                    Gson gsonObj = new Gson();
                    StartActivityReqModel startActivityReqModel = gsonObj.fromJson(encrpt_data, StartActivityReqModel.class);
                    showLog("CHECK POINT 6");

                    if (Boolean.TRUE.equals(startActivityReqModel.getSuccess())) {

                        if (startActivityReqModel.getData().getAdmobAppId() != null
                                && !startActivityReqModel.getData().getAdmobAppId().isEmpty()) {
                            AdMobInitializer.initialize(StartingActivity.this, startActivityReqModel.getData().getAdmobAppId());
                        }

                        // if getting null then uninstall app

                        SharedPrefsData.putInt(getApplicationContext(), Constants.DraftID, startActivityReqModel.getData().getDraftId(), Constants.PREF_NAME);
                        SharedPrefsData.putInt(getApplicationContext(), Constants.LanguageID, startActivityReqModel.getData().getLanguageId(), Constants.PREF_NAME);
                        SharedPrefsData.putInt(getApplicationContext(), Constants.Show_Animation, startActivityReqModel.getData().getShowAnimation(), Constants.PREF_NAME);
                        SharedPrefsData.putInt(getApplicationContext(), Constants.StaticDataApi, startActivityReqModel.getData().getStaticDataApi(), Constants.PREF_NAME);
                        SharedPrefsData.putInt(getApplicationContext(), Constants.StaticDataApi, startActivityReqModel.getData().getStaticDataApi(), Constants.PREF_NAME);

                        SharedPrefsData.putString(getApplicationContext(), Constants.Selected_Distric_Name, startActivityReqModel.getData().getDistName(), Constants.PREF_NAME);
                        SharedPrefsData.putString(getApplicationContext(), Constants.Selected_Distric_Name1, startActivityReqModel.getData().getDistName(), Constants.PREF_NAME);
                        SharedPrefsData.putString(getApplicationContext(), Constants.MOVE_NEXT_SCREEN, startActivityReqModel.getData().getNextScreen(), Constants.PREF_NAME);
                        markOnboardingCompleteIfNeeded(startActivityReqModel.getData().getNextScreen());

                        SharedPrefsData.putBool(getApplicationContext(), Constants.is_login, startActivityReqModel.getData().getIsLogin(), Constants.PREF_NAME);

                        SharedPrefsData.putBool(getApplicationContext(), Constants.Event_Status, startActivityReqModel.getData().getEventStatus(), Constants.PREF_NAME);

                        if (startActivityReqModel.getData().getEngagementBadgeColor() != null
                                && !startActivityReqModel.getData().getEngagementBadgeColor().isEmpty()) {
                            EngagementCountHelper.saveColor(
                                    getApplicationContext(),
                                    startActivityReqModel.getData().getEngagementBadgeColor()
                            );
                        }
                        if (startActivityReqModel.getData().getThemeColor() != null
                                && !startActivityReqModel.getData().getThemeColor().isEmpty()) {
                            ThemeColorHelper.saveColor(
                                    getApplicationContext(),
                                    startActivityReqModel.getData().getThemeColor()
                            );
                        }

                        LanguageFontConfig splashFontConfig = startActivityReqModel.getData().getFontConfig();
                        Map<String, LanguageFontConfig> splashFontSettings =
                                startActivityReqModel.getData().getLanguageFontSettings();
                        int splashLanguageId = startActivityReqModel.getData().getLanguageId() != null
                                ? startActivityReqModel.getData().getLanguageId()
                                : 0;
                        android.util.Log.d(
                                FontSettingsHelper.TAG,
                                "splash response font_config=" + new Gson().toJson(splashFontConfig)
                                        + " legacy_font_size=" + startActivityReqModel.getData().getFontSize()
                                        + " language_id=" + splashLanguageId
                        );
                        if (splashFontSettings != null && !splashFontSettings.isEmpty()) {
                            android.util.Log.d(
                                    FontSettingsHelper.TAG,
                                    "splash language_font_settings count=" + splashFontSettings.size()
                                            + " payload=" + new Gson().toJson(splashFontSettings)
                            );
                            FontSettingsHelper.saveLanguageFontSettings(
                                    getApplicationContext(),
                                    splashFontSettings
                            );
                        } else {
                            android.util.Log.w(
                                    FontSettingsHelper.TAG,
                                    "splash language_font_settings missing/empty; font_config only="
                                            + new Gson().toJson(splashFontConfig)
                            );
                            if (splashFontConfig != null && splashLanguageId > 0) {
                                Map<String, LanguageFontConfig> fallback = new HashMap<>();
                                fallback.put(String.valueOf(splashLanguageId), splashFontConfig);
                                FontSettingsHelper.saveLanguageFontSettings(getApplicationContext(), fallback);
                                android.util.Log.d(
                                        FontSettingsHelper.TAG,
                                        "saved font_config fallback for language_id=" + splashLanguageId
                                );
                            }
                        }

                        showLog("SplashUpdateMenu::::"+ startActivityReqModel.getData().getUpdateMenuStatus());

                        if(startActivityReqModel.getData().getPost_id()!=null){
                            showLog("POST_ID::"+startActivityReqModel.getData().getPost_id());
                            SharedPrefsData.putString(getApplicationContext(), Constants.REFERRER_POST, startActivityReqModel.getData().getPost_id(), Constants.PREF_NAME);

                        }


                        if (startActivityReqModel.getData().getUpdateMenuStatus() == 1 || startActivityReqModel.getData().getUpdateMenuStatus() == 0) {

                            int static_data_value = SharedPrefsData.getInt(getApplicationContext(), Constants.StaticDataApi, Constants.PREF_NAME);
                            int language_id = SharedPrefsData.getInt(getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME);

                            if (language_id==0) {
                                try {

                                    scrren = startActivityReqModel.getData().getNextScreen();
                                    if (scrren.equals("language")) {
                                        Intent intent = new Intent(StartingActivity.this, LanguageActivity.class);
                                        startActivity(intent);
                                        finish();

                                    } else if (scrren.equals("state")) {

                                        Intent intent = new Intent(StartingActivity.this, States.class);
                                        startActivity(intent);
                                        finish();

                                    } else if (scrren.equals("district")) {

                                        Intent intent = new Intent(StartingActivity.this, Districs.class);
                                        startActivity(intent);
                                        finish();

                                    } else if (scrren.equals("region")) {

                                        Intent intent = new Intent(StartingActivity.this, AreaSelection.class);
                                        startActivity(intent);
                                        finish();

                                    }else {
                                        showLog("CHECK POINT HOMEPAGE 10");
                                        Intent intent = new Intent(StartingActivity.this, HomePage.class);
                                        intent.putExtra("EXTRA_SESSION_ID", "0");
                                        intent.putExtra("TITLE", TITLE);
                                        intent.putExtra("TITLE_REWARDS",TITLE_REWARDS);
                                        intent.putExtra("SHARE_REDIRECT_ID",articleId1);
                                        intent.putExtra("DATAFROM2G",DATAFROM2G);
                                        if (getApplicationContext() != null && getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)){
                                            startActivity(intent);
                                        }
                                        finish();
                                    }

                                } catch (Exception e) {
                                    //do things with exception
                                }
                            } else {


                                JsonObject otp = set_static_data();


                                APIService apiService = RestApiClient.getClient().create(APIService.class);

                                Call<JsonObject> otp_generation = apiService.get_static_data("v3", otp);

                                otp_generation.enqueue(new Callback<JsonObject>() {

                                    @Override
                                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                                        if (response.isSuccessful()) {

                                            JsonObject jsonObject = response.body().getAsJsonObject();

                                            Gson gson = new Gson();

                                            LanguageModelResponse languageSelectionResponse = gson.fromJson(jsonObject, LanguageModelResponse.class);

                                            if (languageSelectionResponse.getSuccess()) {

                                                prefManager.setShared_Booleen(Allsharedpreference.Language, true);

                                                String json = gson.toJson(languageSelectionResponse); // myObject - instance of MyObject
                                                prefManager.setShared_String(Allsharedpreference.Translator, json);


                                                SharedPrefsData.putString(StartingActivity.this, Constants.State_Name, languageSelectionResponse.getNextScreen(), Constants.PREF_NAME);



                                                try {

                                                    String scrren = startActivityReqModel.getData().getNextScreen();

                                                    if (scrren.equals("language")) {

                                                        Intent intent = new Intent(StartingActivity.this, LanguageActivity.class);
                                                        startActivity(intent);
                                                        finish();

                                                    } else if (scrren.equals("state")) {

                                                        Intent intent = new Intent(StartingActivity.this, States.class);
                                                        startActivity(intent);
                                                        finish();

                                                    } else if (scrren.equals("district")) {

                                                        Intent intent = new Intent(StartingActivity.this, Districs.class);
                                                        startActivity(intent);
                                                        finish();

                                                    } else if (scrren.equals("region")) {

                                                        Intent intent = new Intent(StartingActivity.this, AreaSelection.class);
                                                        startActivity(intent);
                                                        finish();

                                                    } else {
                                                        showLog("CHECK POINT HOMEPAGE 7");
                                                        Intent intent = new Intent(StartingActivity.this, HomePage.class);
                                                        intent.putExtra("EXTRA_SESSION_ID", "0");
                                                        intent.putExtra("TITLE_REWARDS",TITLE_REWARDS);
                                                        if (getApplicationContext() != null && getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)){
                                                            startActivity(intent);
                                                            finish();
                                                        }

                                                    }

                                                } catch (Exception e) {
                                                    //do things with exception
                                                }

                                            } else {

                                                //     Toast.makeText(context, languageSelectionResponse.getExtras().getMsg(), Toast.LENGTH_SHORT).show();
                                                if ("main".equals(startActivityReqModel.getData().getNextScreen())) {
                                                    openHomeFromSplash();
                                                }

                                            }

                                        } else if ("main".equals(startActivityReqModel.getData().getNextScreen())) {
                                            openHomeFromSplash();
                                        }

                                    }

                                    @Override
                                    public void onFailure(Call<JsonObject> call, Throwable t) {
                                        if ("main".equals(startActivityReqModel.getData().getNextScreen())) {
                                            openHomeFromSplash();
                                        }
                                    }
                                });

                            }


                        } else {
                            if (startActivityReqModel.getData().getUpdateMenuStatus() == 2) {

                                PlaystoreUpdateDialog();

                            } else if (startActivityReqModel.getData().getUpdateMenuStatus() == 3) {
                                movetoappStore();
                            }
                            //
                        }

                    } else {

                        Toast.makeText(StartingActivity.this, "Something went wrong..please try again after some time  ", Toast.LENGTH_LONG).show();
                    }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(StartingActivity.this, "Unable to start app. Please try again.", Toast.LENGTH_LONG).show();
                    }

                } else {
                    showLog("Splash response unsuccessful, using cached route");
                    routeReturningUser();
                }

            }

            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {
                showLog("Splash request failed, using cached route::" + t);
                routeReturningUser();
            }
        });


    }

    private JsonObject Splash_Scrren_Second_Time() {


        /*Logone.i("TAG", "SERIAL: " + Build.SERIAL);
        Logone.i("TAG","MODEL: " + Build.MODEL);
        Logone.i("TAG","ID: " + Build.ID);
        Logone.i("TAG","Manufacture: " + Build.MANUFACTURER);
        Logone.i("TAG","brand: " + Build.BRAND);
        Logone.i("TAG","type: " + Build.TYPE);
        Logone.i("TAG","user: " + Build.USER);
        Logone.i("TAG","BASE: " + Build.VERSION_CODES.BASE);
        Logone.i("TAG","INCREMENTAL " + Build.VERSION.INCREMENTAL);
        Logone.i("TAG","SDK  " + Build.VERSION.SDK);
        Logone.i("TAG","BOARD: " + Build.BOARD);
        Logone.i("TAG","BRAND " + Build.BRAND);
        Logone.i("TAG","HOST " + Build.HOST);
        Logone.i("TAG","FINGERPRINT: "+Build.FINGERPRINT);*/

      /*  showLog("SERIAL: " + Build.SERIAL+"MODEL: " + Build.MODEL+"ID: " + Build.ID+"Manufacture: " + Build.MANUFACTURER+
                "brand: " + Build.BRAND+ "type: " + Build.TYPE+"user: " + Build.USER+"BASE: " + Build.VERSION_CODES.BASE+
                "INCREMENTAL " + Build.VERSION.INCREMENTAL+"SDK  " + Build.VERSION.SDK+ "BOARD: " + Build.BOARD+"BRAND " + Build.BRAND+"HOST " + Build.HOST
                +"FINGERPRINT: "+Build.FINGERPRINT);*/


        StartingAppRequest requestModel = new StartingAppRequest();
        requestModel.setAppVersion((Integer.valueOf(versionid)));
        requestModel.setDraftId(SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME));
        requestModel.setReferralLink("" + SharedPrefsData.getString(getApplicationContext(), Constants.REFERAL_LINK, Constants.PREF_NAME));  //
        requestModel.setReferralLink2("" + SharedPrefsData.getString(getApplicationContext(), Constants.REFERAL_LINK2, Constants.PREF_NAME));  //
        requestModel.setReferredBy(SharedPrefsData.getLong(getApplicationContext(), Constants.CUSST_ID, Constants.PREF_NAME));  //
        requestModel.setFcmToken("" +  SharedPrefsData.getString(getApplicationContext(), Constants.FCM_TOKEN, Constants.PREF_NAME));
        showLog("TOKEN_VALUE::: "+SharedPrefsData.getString(getApplicationContext(), Constants.FCM_TOKEN, Constants.PREF_NAME));
        requestModel.setLanguage_id(""+ SharedPrefsData.getInt(getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME));
        requestModel.setApp_version_name(""+BuildConfig.VERSION_NAME);
        requestModel.setApp_version_number(""+BuildConfig.VERSION_CODE);
        requestModel.setFb_ref_key(""+facebookReferrelKey);
       /* requestModel.setSERIAL(Build.SERIAL);
        requestModel.setMODEL(Build.MODEL);
        requestModel.setID(Build.ID);
        requestModel.setManufacture(Build.MANUFACTURER);
        requestModel.setBOARD(Build.BRAND);
        requestModel.setType(Build.TYPE);
        requestModel.setUser(Build.USER);
        requestModel.setBASE(""+Build.VERSION_CODES.BASE);
        requestModel.setINCREMENTAL(Build.VERSION.INCREMENTAL);
        requestModel.setSDK(""+Build.VERSION.SDK_INT);
        requestModel.setBOARD(Build.BOARD);
        requestModel.setBrand(Build.BRAND);
        requestModel.setHOST(Build.HOST);
        requestModel.setFINGERPRINT(Build.FINGERPRINT);*/

        String jsonString = new com.google.gson.Gson().toJson(requestModel);

        String encrpt_data = null;
        /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            encrpt_data = encrypt(jsonString);
        }*/
        showLog("JSON DATA::: "+jsonString);

        encrpt_data = encrypt(jsonString);
        EncritonRequestData Encriton_Request = new EncritonRequestData();
        Encriton_Request.setData(encrpt_data);
        return new Gson().toJsonTree(Encriton_Request).getAsJsonObject();
    }


    private JsonObject Splash_Scrren_First_Time(String token) {


        StartingAppRequest requestModel = new StartingAppRequest();

        requestModel.setDeviceID(DeviceID);
        requestModel.setLanguage_id(""+ SharedPrefsData.getInt(getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME));
        requestModel.setDeviceType(Integer.valueOf(DeviceType));
        requestModel.setDeviceName(DeviceName);
        requestModel.setAppVersion((Integer.valueOf(versionid)));
        requestModel.setFcmToken("" + token);
        requestModel.setAdId("");
        requestModel.setReferralLink("" + SharedPrefsData.getString(getApplicationContext(), Constants.REFERAL_LINK, Constants.PREF_NAME));  //
        requestModel.setReferralLink2("" + SharedPrefsData.getString(getApplicationContext(), Constants.REFERAL_LINK2, Constants.PREF_NAME));  //
        requestModel.setReferredBy(SharedPrefsData.getLong(getApplicationContext(), Constants.CUSST_ID, Constants.PREF_NAME));  //
        requestModel.setDraftId((0));
        requestModel.setApp_version_name(""+BuildConfig.VERSION_NAME);
        requestModel.setApp_version_number(""+BuildConfig.VERSION_CODE);
        requestModel.setFb_ref_key(""+facebookReferrelKey);


        String jsonString = new com.google.gson.Gson().toJson(requestModel);
        String encrpt_data = null;
        /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            encrpt_data = encrypt(jsonString);
        }*/
        showLog("JSON DATA::: "+jsonString);
        encrpt_data = encrypt(jsonString);

        EncritonRequestData Encriton_Request = new EncritonRequestData();
        Encriton_Request.setData(encrpt_data);
        return new Gson().toJsonTree(Encriton_Request).getAsJsonObject();

    }

    private void markOnboardingCompleteIfNeeded(String nextScreen) {
        if ("main".equalsIgnoreCase(nextScreen)) {
            SharedPrefsData.putBool(getApplicationContext(), Constants.IS_LANGUAGE_SELECTED, true, Constants.PREF_NAME);
        }
    }

    private void openHomeFromSplash() {
        markOnboardingCompleteIfNeeded("main");
        showLog("CHECK POINT HOMEPAGE fallback");
        Intent intent = new Intent(StartingActivity.this, HomePage.class);
        intent.putExtra("EXTRA_SESSION_ID", "0");
        intent.putExtra("TITLE", TITLE);
        intent.putExtra("TITLE_REWARDS", TITLE_REWARDS);
        intent.putExtra("SHARE_REDIRECT_ID", articleId1);
        intent.putExtra("DATAFROM2G", DATAFROM2G);
        if (getApplicationContext() != null && getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)) {
            startActivity(intent);
            finish();
        }
    }

    private void movetoappStore() {

        final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
        try {
            if (getApplicationContext() != null && getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)){
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
            }

        } catch (android.content.ActivityNotFoundException anfe) {
            if (getApplicationContext() != null && getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)){
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
            }
        }
    }

    private void PlaystoreUpdateDialog() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(StartingActivity.this);
        alertDialogBuilder.setTitle(StartingActivity.this.getString(R.string.app_name));
        alertDialogBuilder.setMessage(StartingActivity.this.getString(R.string.update_message));
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton(R.string.update_now, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                final String appPackageName = getPackageName();   // getPackageName() from Context or Activity object
                try {
                    if (getApplicationContext() != null && getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)){
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                    }

                } catch (android.content.ActivityNotFoundException anfe) {
                    if (getApplicationContext() != null && getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)){
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                    }

                }
            }
        });
        alertDialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (isForceUpdate) {

                    ForeUpdateCencel();
                }
                dialog.dismiss();
            }
        });
        if (!isFinishing()){
            alertDialogBuilder.show();
        }
        //alertDialogBuilder.show();


    }


    private JsonObject set_static_data() {

        LanguageSelectionRequest requestModel = new LanguageSelectionRequest();
        requestModel.setDraftId(SharedPrefsData.getInt(StartingActivity.this, Constants.DraftID, Constants.PREF_NAME));   //
        requestModel.setLanguageId(SharedPrefsData.getInt(getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME));

        /*String jsonString = new com.google.gson.Gson().toJson(requestModel);
        String encrpt_data = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            encrpt_data = encrypt(jsonString);
        }


        EncritonRequestData encriton_request=new EncritonRequestData();
        encriton_request.setData(encrpt_data);*/

        return new Gson().toJsonTree(requestModel).getAsJsonObject();
    }

    private JsonObject foreupdatecencel() {

        LanguageSelectionRequest requestModel = new LanguageSelectionRequest();
        requestModel.setDraftId(SharedPrefsData.getInt(StartingActivity.this, Constants.DraftID, Constants.PREF_NAME));   //

        String jsonString = new com.google.gson.Gson().toJson(requestModel);
        String encrpt_data = null;
        /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            encrpt_data = encrypt(jsonString);
        }*/
        encrpt_data = encrypt(jsonString);

        EncritonRequestData Encriton_Request = new EncritonRequestData();
        Encriton_Request.setData(encrpt_data);
        return new Gson().toJsonTree(Encriton_Request).getAsJsonObject();
    }


    public void ForeUpdateCencel() {

        JsonObject splash = foreupdatecencel();
        APIService apiService = RestApiClient.getClient().create(APIService.class);
        Call<EncreptionRequest> apiResponse = apiService.disable_update_menu("v3", splash);

        apiResponse.enqueue(new Callback<EncreptionRequest>() {
            //@RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {

                if (response.isSuccessful()) {


                    EncreptionRequest encreptionRequest = response.body();
                    String encrpt_data = decrypt(encreptionRequest.getData());
                    Gson gsonObj = new Gson();
                    StartActivityReqModel startActivityReqModel = gsonObj.fromJson(encrpt_data, StartActivityReqModel.class);


                    int static_data_value = SharedPrefsData.getInt(getApplicationContext(), Constants.StaticDataApi, Constants.PREF_NAME);

                    if (static_data_value == 0) {
                        try {



                            scrren=  SharedPrefsData.getString(getApplicationContext(), Constants.MOVE_NEXT_SCREEN, Constants.PREF_NAME);

                            if (scrren.equals("language")) {

                                Intent intent = new Intent(StartingActivity.this, LanguageActivity.class);
                                startActivity(intent);
                                finish();

                            } else if (scrren.equals("state")) {

                                Intent intent = new Intent(StartingActivity.this, States.class);
                                startActivity(intent);
                                finish();

                            } else if (scrren.equals("district")) {

                                Intent intent = new Intent(StartingActivity.this, Districs.class);
                                startActivity(intent);
                                finish();

                            } else if (scrren.equals("region")) {

                                Intent intent = new Intent(StartingActivity.this, AreaSelection.class);
                                startActivity(intent);
                                finish();

                            } else {
                                showLog("CHECK POINT HOMEPAGE 8");
                                Intent intent = new Intent(StartingActivity.this, HomePage.class);
                                intent.putExtra("EXTRA_SESSION_ID", "0");
                                intent.putExtra("TITLE", TITLE);
                                intent.putExtra("TITLE_REWARDS",TITLE_REWARDS);
                                intent.putExtra("SHARE_REDIRECT_ID",articleId1);
                                intent.putExtra("DATAFROM2G",DATAFROM2G);
                                if (getApplicationContext() != null && getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)){
                                    startActivity(intent);
                                    finish();
                                }

                            }

                        } catch (Exception e) {
                            //do things with exception
                        }
                    } else {


                        JsonObject otp = set_static_data();


                        APIService apiService = RestApiClient.getClient().create(APIService.class);

                        Call<JsonObject> otp_generation = apiService.get_static_data("v3", otp);

                        otp_generation.enqueue(new Callback<JsonObject>() {

                            @Override
                            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                                if (response.isSuccessful()) {

                                    JsonObject jsonObject = response.body().getAsJsonObject();

                                    Gson gson = new Gson();

                                    LanguageModelResponse languageSelectionResponse = gson.fromJson(jsonObject, LanguageModelResponse.class);

                                    if (languageSelectionResponse.getSuccess()) {

                                        prefManager.setShared_Booleen(Allsharedpreference.Language, true);

                                        String json = gson.toJson(languageSelectionResponse); // myObject - instance of MyObject
                                        prefManager.setShared_String(Allsharedpreference.Translator, json);


                                        SharedPrefsData.putString(StartingActivity.this, Constants.State_Name, languageSelectionResponse.getNextScreen(), Constants.PREF_NAME);


                                        try {

                                            scrren=  SharedPrefsData.getString(getApplicationContext(), Constants.MOVE_NEXT_SCREEN, Constants.PREF_NAME);

                                            if (scrren.equals("language")) {

                                                Intent intent = new Intent(StartingActivity.this, LanguageActivity.class);
                                                startActivity(intent);
                                                finish();

                                            } else if (scrren.equals("state")) {

                                                Intent intent = new Intent(StartingActivity.this, States.class);
                                                startActivity(intent);
                                                finish();

                                            } else if (scrren.equals("district")) {

                                                Intent intent = new Intent(StartingActivity.this, Districs.class);
                                                startActivity(intent);
                                                finish();

                                            } else if (scrren.equals("region")) {

                                                Intent intent = new Intent(StartingActivity.this, AreaSelection.class);
                                                startActivity(intent);
                                                finish();

                                            } else {
                                                showLog("CHECK POINT HOMEPAGE 9");
                                                Intent intent = new Intent(StartingActivity.this, HomePage.class);
                                                intent.putExtra("EXTRA_SESSION_ID", "0");
                                                intent.putExtra("TITLE_REWARDS",TITLE_REWARDS);
                                                if (getApplicationContext() != null && getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)){
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            }

                                        } catch (Exception e) {
                                            //do things with exception
                                        }

                                    } else {

                                        //     Toast.makeText(context, languageSelectionResponse.getExtras().getMsg(), Toast.LENGTH_SHORT).show();

                                    }

                                }

                            }

                            @Override
                            public void onFailure(Call<JsonObject> call, Throwable t) {

                                //    Toast.makeText(context, t.getMessage(), Toast.LENGTH_LONG).show();


                            }
                        });

                    }


                }

            }

            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {


            }
        });
    }

    /* firebase */

    public void writeAppOpen(String userId,String type_firebase ,long current_time, int draft_id, String device_id) {
        AppOpen appOpen = new AppOpen(type_firebase , current_time , draft_id,device_id);

        myRef.child("Events").push().setValue(appOpen);
    }

    @IgnoreExtraProperties
    @Keep
    public class AppOpen {

        public String type;
        public long created_at;
        public int draft_id;
        public String device_id;


        public AppOpen() {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }

        public AppOpen(String type_firebase ,long current_time ,int draft_id, String device_id ) {
            this.type = type_firebase;
            this.created_at = current_time;
            this.draft_id = draft_id;
            this.device_id = device_id;
        }

    }

    // fcm open
    public void writePushOpen(String userId,String type_firebase ,String post_id ,int draft_id ,long current_time ,int language_id , String deviceID ) {
        PushOpen appOpen = new PushOpen(type_firebase ,post_id , draft_id ,current_time , language_id , deviceID);

        myRef.child("Events").push().setValue(appOpen);
    }
    @IgnoreExtraProperties
    @Keep
    public class PushOpen {

        public String type;
        public String post_id;
        public long created_at;
        public int draft_id;
        public int language_id;
        public String device_id;


        public PushOpen() {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }

        public PushOpen(String type_firebase,  String post_id,int draft_id, long current_time , int language_id , String device_id ) {
            this.type = type_firebase_open;
            this.post_id = post_id;
            this.draft_id = draft_id;
            this.created_at = current_time;
            this.language_id = language_id;
            this.device_id = device_id;
        }

    }

    // share open
    public void writeShareOpen(String userId,String type_firebase ,String post_id ,int draft_id ,long current_time) {
        ShareOpen appOpen = new ShareOpen(type_firebase ,post_id ,draft_id, current_time);

        myRef.child("Events").push().setValue(appOpen);
    }
    @IgnoreExtraProperties
    @Keep
    public class ShareOpen {

        public String type;
        public String post_id;
        public int draft_id;
        public long created_at;


        public ShareOpen() {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }

        public ShareOpen(String type_firebase,  String post_id,int draft_id, long current_time  ) {
            this.type = type_firebase;
            this.post_id = post_id;
            this.draft_id = draft_id;
            this.created_at = current_time;
        }


    }

    void checkInstallReferrer() {
        if (getPreferences(MODE_PRIVATE).getBoolean(prefKey, false)) {
            return;
        }
        showLog("checkInstallReferrer");

        InstallReferrerClient referrerClient = InstallReferrerClient.newBuilder(this).build();
        backgroundExecutor.execute(() -> getInstallReferrerFromClient(referrerClient));
    }


    void getInstallReferrerFromClient(InstallReferrerClient referrerClient) {

        referrerClient.startConnection(new InstallReferrerStateListener() {
            @Override
            public void onInstallReferrerSetupFinished(int responseCode) {
                showLog("onInstallReferrerSetupFinished responseCode:"+responseCode);
                switch (responseCode) {
                    case InstallReferrerClient.InstallReferrerResponse.OK:
                        ReferrerDetails response = null;
                        try {
                            response = referrerClient.getInstallReferrer();

                        } catch (RemoteException e) {
                            e.printStackTrace();
                            return;
                        }
                        final String referrerUrl = response.getInstallReferrer();
                        showLog("referrerUrl:"+referrerUrl);
                        SharedPrefsData.putString(getApplicationContext(), Constants.REFERAL_LINK2, referrerUrl, Constants.PREF_NAME);
                        // TODO: If you're using GTM, call trackInstallReferrerforGTM instead.
                        trackInstallReferrer(referrerUrl);
                        trackInstallReferrer(referrerUrl);
                        // Only check this once.
                        getPreferences(MODE_PRIVATE).edit().putBoolean(prefKey, true).commit();

                        // End the connection
                        referrerClient.endConnection();

                        break;
                    case InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED:
                        // API not available on the current Play Store app.
                        break;
                    case InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE:
                        // Connection couldn't be established.
                        break;
                }
            }

            @Override
            public void onInstallReferrerServiceDisconnected() {

            }
        });
    }

    // Tracker for Classic GA (call this if you are using Classic GA only)
    private void trackInstallReferrer(final String referrerUrl) {
        new Handler(getMainLooper()).post(new Runnable() {
            @Override
            public void run() {

                CampaignTrackingReceiver receiver = new CampaignTrackingReceiver();
                Intent intent = new Intent("com.android.vending.INSTALL_REFERRER");
                intent.putExtra("referrer", referrerUrl);
                receiver.onReceive(getApplicationContext(), intent);
            }
        });
    }

    void getUIDs()
    {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    AdvertisingIdClient.Info adInfo = AdvertisingIdClient.getAdvertisingIdInfo(StartingActivity.this);
                    String myId = adInfo != null ? adInfo.getId() : null;
                    prefManager.setAdvertiserId(Allsharedpreference.ADVERTISER_ID, myId);

                    showLog("UIDMY"+ myId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }



}
