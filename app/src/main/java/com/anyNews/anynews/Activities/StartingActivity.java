package com.anyNews.anynews.Activities;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import com.anyNews.anynews.Services.MyFirebaseMessagingService;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Utils.Allsharedpreference;
import com.anyNews.anynews.Utils.AdMobInitializer;
import com.anyNews.anynews.Utils.AdPreloader;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.CricketMiniPlayerPositionHelper;
import com.anyNews.anynews.Utils.CricketSurfaceConfigHelper;
import com.anyNews.anynews.Utils.OfflineFeedStore;
import com.anyNews.anynews.Utils.HomeModeChromeConfig;
import com.anyNews.anynews.Utils.NewsTapModeConfig;
import com.anyNews.anynews.Utils.DistrictPinVisibilityConfig;
import com.anyNews.anynews.Utils.FlashNewsChipHelper;
import com.anyNews.anynews.Utils.ReporterPaymentAppConfig;
import com.anyNews.anynews.Utils.ReadHistoryConfig;
import com.anyNews.anynews.Utils.StatusVideosDaysConfig;
import com.anyNews.anynews.Utils.FeedAdConfig;
import com.anyNews.anynews.Utils.FestivalSplashHelper;
import com.anyNews.anynews.Utils.FlipperAdapter;
import com.anyNews.anynews.Utils.ArticleTtsConfigHelper;
import com.anyNews.anynews.Utils.ArticleTtsHelper;
import com.anyNews.anynews.Utils.AnalyticsBatchConfig;
import com.anyNews.anynews.Utils.CategoriesDiskCache;
import com.anyNews.anynews.Utils.CalendarUiConfigHelper;
import com.anyNews.anynews.Utils.ProfileHubConfigHelper;
import com.anyNews.anynews.Utils.ReporterPostGateHelper;
import com.anyNews.anynews.Utils.TtdConfigHelper;
import com.anyNews.anynews.Utils.LocalUpdatesConfigHelper;
import com.anyNews.anynews.Utils.DailyUpdatesConfigHelper;
import com.anyNews.anynews.Utils.ReadArticleStore;
import com.anyNews.anynews.Utils.DeviceLocationHelper;
import com.anyNews.anynews.Utils.DistrictLabelHelper;
import com.anyNews.anynews.Utils.EngagementCountHelper;
import com.anyNews.anynews.Utils.ScreenCaptureHelper;
import com.anyNews.anynews.Utils.SessionAnalyticsTracker;
import com.anyNews.anynews.Utils.ThemeColorHelper;
import com.anyNews.anynews.Pojo.LanguageFontConfig;
import com.anyNews.anynews.Utils.FontSettingsHelper;
import com.anyNews.anynews.Utils.FullVideoShareCardHelper;
import com.anyNews.anynews.Utils.PollOptionStyleHelper;
import com.anyNews.anynews.Utils.ShareOutboundHelper;
import com.anyNews.anynews.Utils.SharePublicBaseHelper;
import com.anyNews.anynews.Utils.Logone;
import com.anyNews.anynews.Utils.NetworkCallDeduper;
import com.anyNews.anynews.Utils.OpenPathCache;
import com.anyNews.anynews.Utils.TranslatorPackHelper;
import com.anyNews.anynews.Utils.OpenPathFeedPrefetcher;
import com.anyNews.anynews.Utils.SelectionListCache;
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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
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


    private static final String LOG_TAG = "CheckNetworkStatus";
    private NetworkChangeReceiver receiver;
    private boolean isConnected = false;
    private TextView networkStatus, welcome;

        ImageView logo_image_server, logo_image, splashCenterLogo;

    String TAG = "TESTING_VALUE";
    String referlink;
    String referrerUid;
    String articleId1;
    /** Job id from /job/{id} or legacy /job-{base64} deep links. */
    String jobId1;
    /**
     * 0-based nested gallery/magazine page from share {@code ?page=} / {@code ?slide=}.
     * Forwarded to Home as {@code SHARE_GALLERY_PAGE}; 0 = first page (legacy links).
     */
    int shareGalleryPage = 0;
    /**
     * Short code last resolved in this process. A later tap of the same
     * {@code /{code}?page=N} can jump the open magazine without waiting on JSON.
     */
    private String resolvedShareShortCode;
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
    /** Prevents double Home launches (fast path + splash callback race). */
    private boolean homeOpenedFromSplash = false;
    /**
     * True while {@link #maybeResolveShareShortCode} is in flight for a bare
     * {@code /{code}} share URL. Blocks OpenPath fast-home until resolved so
     * magazines/articles do not land on feed page-0 without SHARE_REDIRECT_ID.
     */
    private boolean shareShortCodeResolvePending = false;
    /** One splash network attempt per process open (dedupe + throttle). */
    private boolean splashNetworkInFlight = false;
    /** Splash_Screen failures (prevents infinite re-enqueue on offline first install). */
    private int splashNetworkFailCount = 0;
    /** Avoid re-running getOSandDeviceId on every CONNECTIVITY_ACTION. */
    private boolean startupDeviceStarted = false;
    /**
     * Festival splash gate: when true, {@link #openHomeFromSplash} defers until
     * the admin duration elapses. OFF / no cache → false (zero OpenPath cost).
     */
    private boolean festivalHoldHome = false;
    private boolean pendingHomeAfterFestival = false;
    /** True while downloading banner (theme-color splash kept; never show logo). */
    private boolean festivalAwaitingDownload = false;
    /**
     * Keep AndroidX splash only while waiting for a cold banner download.
     * When the image is already cached, dismiss ASAP and paint the ImageView.
     */
    private boolean splashKeepUntilReady = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Before super + installSplashScreen: Opening Banner ON → theme-color
        // + blank icon (never logo, never black). Banner OFF keeps Manifest
        // centered logo (splash_circle_logo / splash_logo_center_plate).
        // icon_preferred is on the splash theme so share/VIEW cold starts
        // draw that mark; Android 13+ otherwise leaves only splash_logo_bg.
        boolean suppressLogoSplash =
                FestivalSplashHelper.shouldSuppressLogoSplash(this, getIntent());
        final boolean bannerCached =
                suppressLogoSplash && FestivalSplashHelper.hasReadyLocalImage(this);
        if (suppressLogoSplash) {
            setTheme(R.style.AppThemeSplashOpeningBanner);
        }
        // AndroidX requires installSplashScreen before super.onCreate.
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        if (suppressLogoSplash) {
            // Cached → do not hold system splash (theme-color already set; banner paints next).
            // Cold cache → hold theme-color splash until download/paint or timeout.
            splashKeepUntilReady = !bannerCached;
            if (splashKeepUntilReady) {
                splashScreen.setKeepOnScreenCondition(() -> splashKeepUntilReady);
            }
            splashScreen.setOnExitAnimationListener(provider -> {
                try {
                    provider.remove();
                } catch (Exception ignored) {
                }
            });
        } else {
            splashKeepUntilReady = false;
        }
        super.onCreate(savedInstanceState);
        // Ensure previous session's viewed cards are committed before Home News loads
        // (also runs from AppController; once-per-process is a no-op if already done).
        try {
            ReadArticleStore.commitSessionViewedToReadOncePerProcess(getApplicationContext());
        } catch (Exception ignored) {
        }
        try {
            FeedAdConfig.warmFromPrefs(getApplicationContext());
        } catch (Exception ignored) {
        }
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
        splashCenterLogo = findViewById(R.id.splash_center_logo);
        prefManager = new Allsharedpreference(getApplicationContext());
        showLog("EMAIL-ID:::::"+prefManager.getEmailId());
        showLog("CHECK POINT 1");
        Intent intent = getIntent();
        absorbLaunchPushId(intent);
        if (isIncomingPushTap(intent)) {
            MyFirebaseMessagingService.dismissOpenedPush(this, intent);
        }
        SharedPrefsData.putString(getApplicationContext(), Constants.REFERRER_POST, "0", Constants.PREF_NAME);
        // Clear any stale pending share post before parsing this launch's intent;
        // extractData() re-sets it only when this launch is an actual share link.
        SharedPrefsData.putString(getApplicationContext(), Constants.PENDING_SHARE_POST, "", Constants.PREF_NAME);
        SharedPrefsData.putString(getApplicationContext(), Constants.PENDING_SHARE_JOB, "", Constants.PREF_NAME);
        SharedPrefsData.putInt(getApplicationContext(), Constants.PENDING_SHARE_PAGE, 0, Constants.PREF_NAME);
        // Cold start via splash (incl. Recents swipe-away → reopen): do NOT restore
        // mid-feed from HomePage's theme-recreate flip checkpoint. Theme/uiMode
        // recreate never goes through StartingActivity, so that path still works.
        try {
            getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE).edit()
                    .putInt(Constants.FLIP_RESTORE_INDEX, 0)
                    .putString(Constants.FLIP_RESTORE_POST_ID, "-1")
                    .putBoolean(Constants.FLIP_RESTORE_PENDING, false)
                    .putString(Constants.FLIP_RESTORE_AT, "0")
                    .apply();
        } catch (Exception ignored) {
        }

        OBJECT = "" + intent.getStringExtra("OBJECT");
        TITLE_REWARDS = "" +intent.getStringExtra("TITLE_REWARDS");
        if (suppressLogoSplash) {
            // Theme-color (or last sampled banner edge) until banner paints — never logo/black.
            final int gapColor = FestivalSplashHelper.getGapFillColor(this);
            try {
                getWindow().setBackgroundDrawable(new ColorDrawable(gapColor));
            } catch (Exception ignored) {
            }
            try {
                View root = findViewById(android.R.id.content);
                if (root != null) {
                    root.setBackgroundColor(gapColor);
                }
                // Waiting plate only — paintFestivalBannerAndHold clears this so
                // the topmost banner ImageView is not covered.
                if (net_avilable != null) {
                    net_avilable.setBackgroundColor(gapColor);
                }
            } catch (Exception ignored) {
            }
            logo_image_server.setImageDrawable(null);
            logo_image_server.setVisibility(View.INVISIBLE);
            logo_image.setVisibility(View.GONE);
            if (splashCenterLogo != null) {
                splashCenterLogo.setVisibility(View.GONE);
            }
            if (welcome != null) {
                welcome.setVisibility(View.GONE);
            }
        } else {
            // Classic logo (banner OFF). Plate matches the system splash so a
            // share/job/notification cold start is not a flat gray window while
            // the link resolves. new_bg_sp sat the mark at the top and left the
            // center empty (#353535 at night).
            try {
                getWindow().setBackgroundDrawableResource(R.drawable.splash_logo_center_plate);
            } catch (Exception ignored) {
            }
            try {
                View root = findViewById(android.R.id.content);
                if (root != null) {
                    root.setBackgroundResource(R.drawable.splash_logo_center_plate);
                }
            } catch (Exception ignored) {
            }
            logo_image_server.setImageDrawable(null);
            logo_image_server.setVisibility(View.INVISIBLE);
            logo_image.setVisibility(View.GONE);
            if (splashCenterLogo != null) {
                splashCenterLogo.setVisibility(View.VISIBLE);
            }
        }
        receiver = new NetworkChangeReceiver();
        networkStatus = (TextView) findViewById(R.id.networkStatus);
        if(intent.getAction()!=null){
            String action = intent.getAction();
                showLog("action::"+action);
        }
        if (intent.getData() != null) {
            Uri data = intent.getData();
            showLog("data%%::" + data.toString());
            // FCM PendingIntent uses anynews://push/{id} only for Intent uniqueness —
            // never treat it as a share / DATAFROM2G deep link.
            if (!isFcmPushIntentUri(data)) {
                extractData(data);
                absorbSharePageExtra(intent);
                maybeResolveLegacyPageLink(data);
                maybeResolveShareShortCode(data);
            }
        }

        // Opening banner when admin ON for this path (+ wait download if needed).
        // After intent parse so push/share can apply their own toggles.
        maybeArmFestivalSplash();
        // Keep stays true while festivalAwaitingDownload; OFF path already released.

        // Instant open for returning users (after intent/deep-link parse so share ids pass through).
        // Config/FCM soft-refresh continues via getOSandDeviceId on the next tick.
        // Wait for short-code HTTP resolve so SHARE_REDIRECT_ID is set before Home
        // (otherwise magazines land on feed page-0 / first unrelated news).
        try {
            if (shareShortCodeResolvePending
                    && OpenPathCache.canFastOpenHome(getApplicationContext())) {
                showLog("OpenPath: defer fast home until share short-code resolves");
                new Handler(Looper.getMainLooper()).post(() -> {
                    try {
                        if (!startupDeviceStarted) {
                            getOSandDeviceId();
                        }
                    } catch (Exception ignored) {
                    }
                });
                // Fail-open: if resolve hangs, still enter Home (PENDING may arrive later).
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    try {
                        if (!homeOpenedFromSplash) {
                            OpenPathFeedPrefetcher.maybePrefetchMainPage0(this, TITLE, articleId1);
                            openHomeFromSplash();
                        }
                    } catch (Exception ignored) {
                    }
                }, 2500);
                return;
            } else if (OpenPathCache.canFastOpenHome(getApplicationContext())) {
                showLog("OpenPath: immediate home after intent parse");
                // Kick List_All_Articles before Home binds — coalesce with Home page-0.
                OpenPathFeedPrefetcher.maybePrefetchMainPage0(this, TITLE, articleId1);
                openHomeFromSplash();
                new Handler(Looper.getMainLooper()).post(() -> {
                    try {
                        if (!startupDeviceStarted) {
                            getOSandDeviceId();
                        }
                    } catch (Exception ignored) {
                    }
                });
                return;
            }
        } catch (Exception e) {
            showLog("OpenPath: early home skip " + e.getMessage());
        }

        int draft_id_firebase = SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME);
        String device_id_firebase =  Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        long current_time_firebase = System.currentTimeMillis();

        writeAppOpen("User",type_firebase ,current_time_firebase,draft_id_firebase,device_id_firebase);
               showLog("CHECK POINT 2");
        // FDL shut down 2025-08-25 — keep as best-effort only; App Links + extractData are primary
        try {
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
                            if (deepLink == null) {
                                return;
                            }
                            referrerUid = deepLink.getQueryParameter("user_id");
                            showLog("CHECK POINT referrerUid:"+referrerUid);
                            Uri finalDeepLink = deepLink;
                            try {
                                String fromFdl = finalDeepLink.getQueryParameter("articleId");
                                if (fromFdl == null || fromFdl.isEmpty()) {
                                    fromFdl = finalDeepLink.getQueryParameter("post_id");
                                }
                                if (fromFdl == null || fromFdl.isEmpty()) {
                                    fromFdl = articleIdFromPath(finalDeepLink);
                                }
                                if (fromFdl != null && !fromFdl.isEmpty()) {
                                    articleId1 = fromFdl;
                                    // Share sticky only — never TITLE (FCM push id).
                                    SharedPrefsData.putString(getApplicationContext(),
                                            Constants.PENDING_SHARE_POST, fromFdl, Constants.PREF_NAME);
                                }
                                showLog("CHECK POINT articleId1 %%%:"+articleId1);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                            applyDeepLinkParams(deepLink);
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
                        // Expected after FDL shutdown — App Links path parsing handles shares
                        Logone.w(TAG, "getDynamicLink:onFailure (ignored; using App Links)", e);
                    }
                });
        } catch (Exception e) {
            Logone.w(TAG, "FirebaseDynamicLinks unavailable", e);
        }



                try {
                    checkInstallReferrer();
                }catch (Exception e){
                    e.printStackTrace();
                }

                // No artificial 1s splash wait — network/FCM work starts immediately.
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        isNetworkAvailable(StartingActivity.this);
                        getUIDs();
                        setAutoLogAppEventsEnabled(true);
                        UUID uuid = UUID.randomUUID();
                        String uuidAsString = uuid.toString();
                        SharedPrefsData.putString(getApplicationContext(), Constants.USER_SESSION_ID, uuidAsString, Constants.PREF_NAME);
                    }
                });

                // Absolute fail-open: if splash/API never finishes, leave the logo.
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (isFinishing() || isDestroyed() || homeOpenedFromSplash) {
                            return;
                        }
                        showLog("OpenPath: splash watchdog fail-open");
                        splashNetworkFailCount = Math.max(splashNetworkFailCount, 1);
                        routeReturningUser();
                    }
                }, 25000);


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
    public void extractData(Uri uri) {
        try {
            if (uri == null) {
                return;
            }
            applyDeepLinkParams(uri);

            // Job shares first — /job/{id} must not be mistaken for article paths.
            String jobId2 = uri.getQueryParameter("jobId");
            if (jobId2 == null || jobId2.isEmpty()) {
                jobId2 = uri.getQueryParameter("job_id");
            }
            if (jobId2 == null || jobId2.isEmpty()) {
                jobId2 = jobIdFromPath(uri);
            }
            if (jobId2 != null && !jobId2.isEmpty()) {
                jobId1 = jobId2;
                SharedPrefsData.putString(getApplicationContext(),
                        Constants.PENDING_SHARE_JOB, jobId2, Constants.PREF_NAME);
                showLog("CHECK POINT JobId @@@:" + jobId2);
                return;
            }

            String articleId2 = uri.getQueryParameter("articleId");
            if (articleId2 == null || articleId2.isEmpty()) {
                articleId2 = uri.getQueryParameter("post_id");
            }
            if (articleId2 == null || articleId2.isEmpty()) {
                articleId2 = articleIdFromPath(uri);
            }
            showLog("CHECK POINT ArticleId1 @@@:" + articleId2);
            if (articleId2 != null && !articleId2.isEmpty()) {
                articleId1 = articleId2;
                // Share / App Link opens must use SHARE_REDIRECT_ID + share_post_id only.
                // Do NOT set TITLE=articleId — TITLE is the FCM push sticky (push_post_id).
                // Dual-assigning made server take the push_open branch, skip share pin, and
                // left already-read shares stripped on News without pinning the
                // exact card. SHARE_REDIRECT_ID + share_post_id is the share path.
                // Persist as a one-shot so the exact post still opens even when a
                // splash routing branch launches HomePage without the
                // SHARE_REDIRECT_ID extra (e.g. returning-user static-data path).
                // Cross-language: do not change LanguageID from the shared article's
                // language — Home sends user LanguageID; server pins any-language post.
                SharedPrefsData.putString(getApplicationContext(),
                        Constants.PENDING_SHARE_POST, articleId2, Constants.PREF_NAME);
            } else {
                DATAFROM2G = uri.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Parse job id from /job/{id}, anynews://job/{id}, or legacy /job-{base64}
     * (e.g. https://anynews.co.in/job-MQ== → job 1).
     */
    private String jobIdFromPath(Uri uri) {
        if (uri == null) {
            return null;
        }
        String scheme = uri.getScheme();
        String host = uri.getHost();
        if (scheme != null && "anynews".equalsIgnoreCase(scheme)
                && host != null && "job".equalsIgnoreCase(host)) {
            String path = uri.getPath();
            if (path != null) {
                String trimmed = path.startsWith("/") ? path.substring(1) : path;
                int slash = trimmed.indexOf('/');
                String idPart = slash >= 0 ? trimmed.substring(0, slash) : trimmed;
                String resolved = decodeJobIdToken(idPart);
                if (resolved != null) {
                    return resolved;
                }
            }
        }
        String path = uri.getPath();
        if (path == null || path.isEmpty() || "/".equals(path)) {
            return null;
        }
        String[] segments = path.split("/");
        for (int i = 0; i < segments.length; i++) {
            String seg = segments[i];
            if (seg == null || seg.isEmpty()) {
                continue;
            }
            // /job/{id} — same shape as /p/{id} for articles
            if ("job".equalsIgnoreCase(seg) && i + 1 < segments.length) {
                String next = segments[i + 1];
                String resolved = decodeJobIdToken(next);
                if (resolved != null) {
                    return resolved;
                }
            }
            // Legacy single-segment /job-{base64}
            if (seg.length() > 4 && seg.regionMatches(true, 0, "job-", 0, 4)) {
                String resolved = decodeJobIdToken(seg.substring(4));
                if (resolved != null) {
                    return resolved;
                }
            }
        }
        return null;
    }

    /** Digits as-is, or base64-decoded digits (legacy job-MQ==). */
    private String decodeJobIdToken(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        String t = token.trim();
        if (t.matches("\\d+")) {
            return t;
        }
        try {
            byte[] raw = android.util.Base64.decode(t, android.util.Base64.DEFAULT);
            if (raw != null && raw.length > 0) {
                String decoded = new String(raw, java.nio.charset.StandardCharsets.UTF_8).trim();
                if (decoded.matches("\\d+")) {
                    return decoded;
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /** Parse post id from /p/{id}, /post/{id}, anynews://post/{id}, /{id} (anws.in), or /p/{id} on share host.
     * Alphanumeric short codes on share hosts are resolved async via maybeResolveShareShortCode. */
    private String articleIdFromPath(Uri uri) {
        if (uri == null) {
            return null;
        }
        // anynews://post/{id}
        String scheme = uri.getScheme();
        String host = uri.getHost();
        if (scheme != null && "anynews".equalsIgnoreCase(scheme)
                && host != null && "post".equalsIgnoreCase(host)) {
            String path = uri.getPath();
            if (path != null) {
                String trimmed = path.startsWith("/") ? path.substring(1) : path;
                int slash = trimmed.indexOf('/');
                String idPart = slash >= 0 ? trimmed.substring(0, slash) : trimmed;
                if (idPart.matches("\\d+")) {
                    return idPart;
                }
            }
        }
        String path = uri.getPath();
        if (path == null || path.isEmpty() || "/".equals(path)) {
            return null;
        }
        String[] segments = path.split("/");
        for (int i = 0; i < segments.length; i++) {
            String seg = segments[i];
            if (seg == null || seg.isEmpty()) {
                continue;
            }
            if (("p".equals(seg) || "post".equals(seg)) && i + 1 < segments.length) {
                String next = segments[i + 1];
                if (next != null && next.matches("\\d+")) {
                    return next;
                }
            }
        }
        // anws.in/{postId}
        if (host != null && host.contains("anws.in")) {
            for (String seg : segments) {
                if (seg != null && seg.matches("\\d+")) {
                    return seg;
                }
            }
        }
        // share.anynews.co.in|sharedev…/{digits} only — alphanumeric codes need HTTP resolve
        if (isShareShortHost(host)) {
            for (String seg : segments) {
                if (seg != null && seg.matches("\\d+")) {
                    return seg;
                }
            }
        }
        return null;
    }

    /** Production share host + legacy sharedev (old inbound links only). */
    private static boolean isShareShortHost(@Nullable String host) {
        if (host == null || host.isEmpty()) {
            return false;
        }
        String h = host.toLowerCase(java.util.Locale.US);
        return h.equals("share.anynews.co.in")
                || h.equals("sharedev.anynews.co.in");
    }

    /**
     * Alphanumeric {@code https://share*.anynews.co.in/{code}} segment, or null
     * when this URI is already a numeric /p/{id} (or not a share host).
     */
    @Nullable
    private String shareShortCodeFromUri(@Nullable Uri data) {
        if (data == null || !isShareShortHost(data.getHost())) {
            return null;
        }
        String path = data.getPath();
        if (path == null || path.isEmpty() || "/".equals(path)) {
            return null;
        }
        String code = path.startsWith("/") ? path.substring(1) : path;
        int slash = code.indexOf('/');
        if (slash >= 0) {
            code = code.substring(0, slash);
        }
        if (code.isEmpty() || "p".equals(code) || code.matches("\\d+")) {
            return null;
        }
        if (!code.matches("[A-Za-z0-9]{5,12}")) {
            return null;
        }
        return code;
    }

    /**
     * Resolve https://share*.anynews.co.in/{code} → numeric post_id via ?format=json.
     * App Links open the short URL directly; landing HTML is not loaded.
     * A previous {@link #articleId1} must not skip this — warm taps of
     * {@code /{code}?page=N} still need a resolve when the code changed.
     */
    private void maybeResolveShareShortCode(Uri data) {
        final String shortCode = shareShortCodeFromUri(data);
        if (shortCode == null || data == null || data.getHost() == null) {
            return;
        }
        final String resolveHost = data.getHost().toLowerCase(java.util.Locale.US);
        // Forward ?page= so the JSON echo can restore it if the VIEW uri lost the query.
        final int pageAtResolve = Math.max(0, shareGalleryPage);
        final String resolveUrl = "https://" + resolveHost + "/" + shortCode + "?format=json"
                + (pageAtResolve > 0 ? "&page=" + pageAtResolve : "");
        shareShortCodeResolvePending = true;
        backgroundExecutor.execute(() -> {
            String resolvedId = null;
            int resolvedPage = 0;
            HttpURLConnection conn = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(resolveUrl);
                conn = (HttpURLConnection) url.openConnection();
                conn.setInstanceFollowRedirects(true);
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
                int status = conn.getResponseCode();
                if (status >= 200 && status < 300) {
                    reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    JsonObject obj = new Gson().fromJson(sb.toString(), JsonObject.class);
                    if (obj != null && obj.has("post_id") && !obj.get("post_id").isJsonNull()) {
                        resolvedId = String.valueOf(obj.get("post_id").getAsLong());
                    }
                    if (obj != null && obj.has("page") && !obj.get("page").isJsonNull()) {
                        try {
                            resolvedPage = Math.max(0, obj.get("page").getAsInt());
                        } catch (Exception ignored) {
                        }
                    }
                }
            } catch (Exception e) {
                Logone.w(TAG, "share short code resolve failed", e);
            } finally {
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (Exception ignored) {
                }
                if (conn != null) {
                    conn.disconnect();
                }
            }
            final String id = resolvedId;
            final int pageFromJson = resolvedPage;
            new Handler(Looper.getMainLooper()).post(() -> {
                shareShortCodeResolvePending = false;
                if (pageFromJson > 0) {
                    shareGalleryPage = pageFromJson;
                    SharedPrefsData.putInt(getApplicationContext(), Constants.PENDING_SHARE_PAGE,
                            shareGalleryPage, Constants.PREF_NAME);
                }
                if (id != null && !id.isEmpty()) {
                    resolvedShareShortCode = shortCode;
                    articleId1 = id;
                    SharedPrefsData.putString(getApplicationContext(),
                            Constants.PENDING_SHARE_POST, id, Constants.PREF_NAME);
                    showLog("CHECK POINT share short-code resolved articleId:" + id
                            + " page=" + shareGalleryPage
                            + " host=" + resolveHost);
                    deliverShareRedirectToHome(id);
                }
            });
        });
    }

    /**
     * After short-code / warm-start resolve: open or update Home with
     * SHARE_REDIRECT_ID so magazines pin via share_post_id (never page-0 alone).
     */
    private void deliverShareRedirectToHome(@Nullable String shareId) {
        if (shareId == null || shareId.trim().isEmpty()
                || "0".equals(shareId.trim())
                || "null".equalsIgnoreCase(shareId.trim())) {
            return;
        }
        try {
            if (!homeOpenedFromSplash) {
                OpenPathFeedPrefetcher.maybePrefetchMainPage0(this, TITLE, shareId);
                openHomeFromSplash();
                return;
            }
            // Home already showing — push sticky via onNewIntent (CLEAR_TOP|SINGLE_TOP).
            Intent intent = new Intent(StartingActivity.this, HomePage.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("SHARE_REDIRECT_ID", shareId.trim());
            intent.putExtra("SHARE_GALLERY_PAGE", shareGalleryPage);
            intent.putExtra("EXTRA_SESSION_ID", "0");
            // Share path must not re-forward a stale splash TITLE as push_post_id.
            intent.putExtra("TITLE", "0");
            intent.putExtra("TITLE_REWARDS", TITLE_REWARDS);
            intent.putExtra("JOB_REDIRECT_ID", jobId1);
            intent.putExtra("DATAFROM2G", DATAFROM2G);
            startActivity(intent);
        } catch (Exception e) {
            Logone.w(TAG, "deliverShareRedirectToHome failed", e);
        }
    }

    /**
     * Intent string extra {@code page} / {@code slide} from the landing
     * {@code S.page=} handoff. Used only when the VIEW uri has no query —
     * a uri query always wins. 0-based, same as {@code ?page=}.
     */
    private void absorbSharePageExtra(@Nullable Intent intent) {
        if (intent == null || shareGalleryPage > 0) {
            return;
        }
        int page = 0;
        try {
            page = pageExtraValue(intent, "page");
            if (page <= 0) {
                page = pageExtraValue(intent, "slide");
            }
        } catch (Exception ignored) {
        }
        if (page <= 0) {
            return;
        }
        shareGalleryPage = page;
        SharedPrefsData.putInt(getApplicationContext(), Constants.PENDING_SHARE_PAGE,
                shareGalleryPage, Constants.PREF_NAME);
    }

    private static int pageExtraValue(@NonNull Intent intent, @NonNull String key) {
        if (!intent.hasExtra(key) || intent.getExtras() == null) {
            return 0;
        }
        Object raw = intent.getExtras().get(key);
        if (raw == null) {
            return 0;
        }
        try {
            if (raw instanceof Number) {
                return Math.max(0, ((Number) raw).intValue());
            }
            String text = String.valueOf(raw).trim().replaceAll("[^0-9]", "");
            if (text.isEmpty()) {
                return 0;
            }
            return Math.max(0, Integer.parseInt(text));
        } catch (Exception ignored) {
            return 0;
        }
    }

    private void applyDeepLinkParams(Uri uri) {
        if (uri == null) {
            return;
        }
        try {
            String uid = uri.getQueryParameter("user_id");
            if (uid != null && !uid.isEmpty()) {
                referrerUid = uid;
                try {
                    SharedPrefsData.putLong(getApplicationContext(), Constants.CUSST_ID,
                            Long.parseLong(uid.replaceAll("[^0-9]", "")), Constants.PREF_NAME);
                } catch (Exception ignored) { /* non-numeric referrer */ }
            }
            int page = ShareOutboundHelper.parseGalleryPage(uri);
            shareGalleryPage = Math.max(0, page);
            SharedPrefsData.putInt(getApplicationContext(), Constants.PENDING_SHARE_PAGE,
                    shareGalleryPage, Constants.PREF_NAME);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Legacy anynews0.page.link short links still HTTP-302 to Play Store with articleId.
     * Resolve without the deprecated FDL SDK when the short URL is opened in-app.
     */
    private void maybeResolveLegacyPageLink(Uri data) {
        if (data == null) {
            return;
        }
        String host = data.getHost();
        if (host == null || !host.contains("page.link")) {
            return;
        }
        if (articleId1 != null && !articleId1.isEmpty()) {
            return;
        }
        final String shortUrl = data.toString();
        backgroundExecutor.execute(() -> {
            String resolvedId = null;
            HttpURLConnection conn = null;
            try {
                URL url = new URL(shortUrl);
                conn = (HttpURLConnection) url.openConnection();
                conn.setInstanceFollowRedirects(false);
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);
                conn.setRequestMethod("HEAD");
                int code = conn.getResponseCode();
                String location = conn.getHeaderField("Location");
                if ((code == 301 || code == 302 || code == 303 || code == 307 || code == 308)
                        && location != null) {
                    Uri loc = Uri.parse(location);
                    resolvedId = loc.getQueryParameter("articleId");
                    if (resolvedId == null || resolvedId.isEmpty()) {
                        resolvedId = loc.getQueryParameter("post_id");
                    }
                    if (resolvedId == null || resolvedId.isEmpty()) {
                        resolvedId = articleIdFromPath(loc);
                    }
                }
            } catch (Exception e) {
                Logone.w(TAG, "page.link resolve failed", e);
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
            if (resolvedId != null && !resolvedId.isEmpty()) {
                final String id = resolvedId;
                new Handler(Looper.getMainLooper()).post(() -> {
                    articleId1 = id;
                    // Share sticky only — never TITLE (push). See extractData.
                    SharedPrefsData.putString(getApplicationContext(),
                            Constants.PENDING_SHARE_POST, id, Constants.PREF_NAME);
                    showLog("CHECK POINT page.link resolved articleId:" + id);
                    deliverShareRedirectToHome(id);
                });
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (intent == null) {
            return;
        }
        // FCM tray / notification-history tap while splash singleTask is still
        // alive: refresh TITLE and deliver to Home (share path below is URI-only).
        if (deliverWarmPushOpen(intent)) {
            return;
        }
        if (intent.getData() != null) {
            Uri data = intent.getData();
            if (!isFcmPushIntentUri(data)) {
                extractData(data);
                absorbSharePageExtra(intent);
                maybeResolveLegacyPageLink(data);
                // Same magazine already resolved: deliver now so ?page= moves the
                // open pager. A different code must not reuse the previous post id.
                String incomingCode = shareShortCodeFromUri(data);
                boolean sameShort = incomingCode != null
                        && incomingCode.equals(resolvedShareShortCode)
                        && articleId1 != null
                        && !articleId1.isEmpty();
                if (incomingCode != null && !sameShort) {
                    articleId1 = null;
                    maybeResolveShareShortCode(data);
                } else if (incomingCode == null) {
                    maybeResolveShareShortCode(data);
                }
            }
            // Warm start: open Jobs hub immediately for job share App Links.
            if (jobId1 != null && !jobId1.isEmpty()) {
                try {
                    int id = Integer.parseInt(jobId1.replaceAll("[^0-9]", ""));
                    if (id > 0) {
                        Intent jobs = new Intent(this, JobsPageActivity.class);
                        jobs.putExtra(JobsPageActivity.EXTRA_FOCUS_JOB_ID, id);
                        jobs.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(jobs);
                    }
                } catch (Exception ignored) {
                }
            } else if (articleId1 != null && !articleId1.isEmpty()) {
                // Warm share (app already on Home): pin the exact post / magazine.
                deliverShareRedirectToHome(articleId1);
            }
        }
    }

    /** True for FCM tray PendingIntent data ({@code anynews://push/{postId}}). */
    private static boolean isFcmPushIntentUri(@Nullable Uri uri) {
        if (uri == null) {
            return false;
        }
        return "anynews".equalsIgnoreCase(uri.getScheme())
                && "push".equalsIgnoreCase(uri.getHost());
    }

    /** Tray tap, not a launcher or share splash. TITLE alone is too broad. */
    private static boolean isIncomingPushTap(@Nullable Intent intent) {
        if (intent == null) {
            return false;
        }
        if (isFcmPushIntentUri(intent.getData())
                || intent.getBooleanExtra("notification", false)
                || MyFirebaseMessagingService.isOpenPushAction(intent.getAction())) {
            return true;
        }
        String titile = intent.getStringExtra("TITILE");
        if (titile == null || titile.isEmpty() || "null".equalsIgnoreCase(titile)) {
            return false;
        }
        try {
            return Long.parseLong(titile.trim()) > 0L;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Copy a numeric push post id onto {@link #TITLE}. Notification history
     * sometimes launches the launcher activity with the visible headline and
     * no numeric id. A plain icon launch does not pin a tray post.
     */
    private void absorbLaunchPushId(@Nullable Intent intent) {
        String resolved = MyFirebaseMessagingService.resolvePushPostId(this, intent);
        if (!isNumericPushId(resolved)
                && MyFirebaseMessagingService.isHistoryLauncherFallback(this, intent)) {
            resolved = MyFirebaseMessagingService.resolveHistoryDisplayedTitle(this, intent);
        }
        if (isNumericPushId(resolved)) {
            TITLE = resolved.trim();
            try {
                SharedPrefsData.putString(
                        getApplicationContext(),
                        Constants.PENDING_PUSH_POST,
                        TITLE,
                        Constants.PREF_NAME);
            } catch (Exception ignored) {
            }
        } else {
            TITLE = "0";
            // Launcher / non-push splash: drop stale sticky so HomePage cannot pin
            // a previously received (but untapped) notification article.
            try {
                SharedPrefsData.putString(
                        getApplicationContext(),
                        Constants.PENDING_PUSH_POST,
                        "",
                        Constants.PREF_NAME);
            } catch (Exception ignored) {
            }
        }
    }

    /** @return true when this intent was a push open and Home was asked to pin it. */
    private boolean deliverWarmPushOpen(@Nullable Intent intent) {
        if (intent == null) {
            return false;
        }
        String pushTitle = MyFirebaseMessagingService.resolvePushPostId(this, intent);
        boolean historyFallback = false;
        if (!isNumericPushId(pushTitle)
                && MyFirebaseMessagingService.isHistoryLauncherFallback(this, intent)) {
            pushTitle = MyFirebaseMessagingService.resolveHistoryDisplayedTitle(this, intent);
            historyFallback = isNumericPushId(pushTitle);
        }
        if (!isNumericPushId(pushTitle)) {
            return false;
        }
        if (!isIncomingPushTap(intent) && !historyFallback) {
            return false;
        }
        if (isIncomingPushTap(intent)) {
            MyFirebaseMessagingService.dismissOpenedPush(this, intent);
        }
        TITLE = pushTitle.trim();
        TITLE_REWARDS = "" + intent.getStringExtra("TITLE_REWARDS");
        OBJECT = "" + intent.getStringExtra("OBJECT");
        try {
            SharedPrefsData.putString(
                    getApplicationContext(),
                    Constants.PENDING_PUSH_POST,
                    TITLE,
                    Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
        deliverPushRedirectToHome(TITLE);
        return true;
    }

    private static boolean isNumericPushId(@Nullable String raw) {
        if (raw == null) {
            return false;
        }
        try {
            return Long.parseLong(raw.trim()) > 0L;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Forward the {@code notify()} id. Do not invent {@code hashCode(postId)} —
     * that is not the tray id from {@code safeNotificationId}.
     */
    private void attachPushTrayId(@NonNull Intent homeIntent, @Nullable String postId) {
        if (postId == null || postId.trim().isEmpty()
                || "0".equals(postId.trim())
                || "null".equalsIgnoreCase(postId.trim())) {
            return;
        }
        String id = postId.trim();
        int nid = MyFirebaseMessagingService.pushNotificationIdFromIntent(getIntent());
        if (nid == 0) {
            try {
                long v = Long.parseLong(id);
                if (v > 0 && v <= Integer.MAX_VALUE) {
                    nid = (int) v;
                }
            } catch (Exception ignored) {
            }
        }
        if (nid != 0) {
            homeIntent.putExtra(MyFirebaseMessagingService.EXTRA_PUSH_NOTIFICATION_ID, nid);
        }
        try {
            String uri = nid != 0
                    ? "anynews://push/" + id + "/" + nid
                    : "anynews://push/" + id;
            homeIntent.setData(Uri.parse(uri));
        } catch (Exception ignored) {
        }
    }

    /**
     * Warm FCM open: Home already showing — pin via TITLE / push_post_id onNewIntent.
     * Cold path still uses {@link #openHomeFromSplash()}.
     */
    private void deliverPushRedirectToHome(@Nullable String pushPostId) {
        if (pushPostId == null || pushPostId.trim().isEmpty()
                || "0".equals(pushPostId.trim())
                || "null".equalsIgnoreCase(pushPostId.trim())) {
            return;
        }
        try {
            if (!homeOpenedFromSplash) {
                OpenPathFeedPrefetcher.maybePrefetchMainPage0(this, pushPostId, articleId1);
                openHomeFromSplash();
                return;
            }
            Intent intent = new Intent(StartingActivity.this, HomePage.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("notification", true);
            intent.putExtra("TITLE", pushPostId.trim());
            intent.putExtra("TITILE", pushPostId.trim());
            intent.putExtra("EXTRA_SESSION_ID", "0");
            intent.putExtra("TITLE_REWARDS", TITLE_REWARDS);
            intent.putExtra("DATAFROM2G", DATAFROM2G);
            // Keep the notify() id. hashCode(postId) diverged from the tray id and
            // a later cancel removed nothing.
            attachPushTrayId(intent, pushPostId.trim());
            // Do not pass SHARE_REDIRECT_ID — push must use push_post_id only.
            startActivity(intent);
        } catch (Exception e) {
            Logone.w(TAG, "deliverPushRedirectToHome failed", e);
        }
    }

    @Override
    protected void onUserLeaveHint() {
        try {
            SessionAnalyticsTracker.get().onUserLeaveHint(this);
        } catch (Exception ignored) {
        }
        super.onUserLeaveHint();
    }

    @Override
    protected void onDestroy() {
        Logone.v(LOG_TAG, "onDestory");
        super.onDestroy();

      //  unregisterReceiver(receiver);

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Power-button lock can leave splash on "no network" even after internet returns.
        // Re-check on foreground and continue startup if we recovered.
        try {
            if (net_avilable != null && net_not_avilable != null
                    && net_not_avilable.getVisibility() == View.VISIBLE) {
                isNetworkAvailable(StartingActivity.this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                        boolean recovering = !isConnected
                                || (net_not_avilable != null
                                && net_not_avilable.getVisibility() == View.VISIBLE);
                        if (recovering) {
                            Logone.v(LOG_TAG, "Now you are connected to Internet!");
                            //    networkStatus.setText("Now you are connected to Internet!");
                            // Start device/identity immediately (was +1s) to shorten splash.
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    getOSandDeviceId();
                                }
                            });

                            if (net_avilable != null) {
                                net_avilable.setVisibility(View.VISIBLE);
                            }
                            if (net_not_avilable != null) {
                                net_not_avilable.setVisibility(View.GONE);
                            }
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
        // Offline with local identity + OfflineFeedStore / OpenPath / translator:
        // open Home on the same path as “saved for offline” — do not trap the user
        // on splash full-screen “No internet!”.
        if (OpenPathCache.canFastOpenHome(context)) {
            showLog("OpenPath: offline fast home from local cache");
            if (net_avilable != null) {
                net_avilable.setVisibility(View.VISIBLE);
            }
            if (net_not_avilable != null) {
                net_not_avilable.setVisibility(View.GONE);
            }
            isConnected = false;
            openHomeFromSplash();
            return false;
        }

        if (net_avilable != null) {
            net_avilable.setVisibility(View.GONE);
        }
        if (net_not_avilable != null) {
            net_not_avilable.setVisibility(View.VISIBLE);
            try {
                net_not_avilable.bringToFront();
            } catch (Exception ignored) {
            }
        }
        if (splashCenterLogo != null) {
            splashCenterLogo.setVisibility(View.GONE);
        }

        //   one.setText("You are not connected to Internet!");
        isConnected = false;
        return false;
    }

    private void getOSandDeviceId() {
        if (startupDeviceStarted) {
            showLog("OpenPath: getOSandDeviceId already started");
            return;
        }
        startupDeviceStarted = true;
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
        DeviceID = resolveStableDeviceId();
        String App_version_id_sending = "" + version_name;
        String version_id = getVersionIdPrefix();
        SharedPrefsData.putString(getApplicationContext(), Constants.Version_ID, version_id, Constants.PREF_NAME);

        String token = SharedPrefsData.getString(getApplicationContext(), Constants.FCM_TOKEN, Constants.PREF_NAME);
        showLog("CHECK POINT 11::" + token);

        int draftForPush = SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME);
        String device_id = SharedPrefsData.getString(getApplicationContext(), Constants.DeviceID, Constants.PREF_NAME);
        int lang_id = SharedPrefsData.getInt(getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME);
        if (draftForPush != 0 && TITLE != null && !TITLE.equals("null") && !TITLE.isEmpty() && !TITLE.equals("0")) {
            writePushOpen("User", type_firebase, TITLE, draftForPush, System.currentTimeMillis(), lang_id, device_id);
        }

        /*
         * Open-path optimization:
         * - Returning users with draft+language+translator: leave splash immediately (local),
         *   refresh splash in background only when TTL expired (does not re-block UI).
         * - Never block splash on FirebaseMessaging.getToken() — on emulators / broken Play
         *   Services the Task can hang indefinitely (release users saw an infinite splash).
         * - Use cached token, else a stable ANDROID_ID placeholder; refresh real FCM in bg.
         */
        if (OpenPathCache.canFastOpenHome(getApplicationContext())) {
            showLog("OpenPath: fast home (identity+translator local)");
            OpenPathFeedPrefetcher.maybePrefetchMainPage0(this, TITLE, articleId1);
            openHomeFromSplash();
            final String cachedToken = ensureSplashFcmToken(token);
            refreshFcmTokenInBackground(cachedToken);
            // Identity/config soft refresh only if splash TTL expired.
            if (OpenPathCache.shouldBlockOnSplash(getApplicationContext())) {
                sendDeviceDetailsBackground(cachedToken);
            }
        } else {
            String resolvedToken = ensureSplashFcmToken(token);
            showLog("OpenPath: splash with token (non-blocking FCM)::" + resolvedToken);
            sendDeviceDetails(resolvedToken);
            refreshFcmTokenInBackground(resolvedToken);
        }

        try {
            FirebaseDynamicLinks.getInstance()
                    .getDynamicLink(getIntent())
                    .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                        @Override
                        public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                            Uri deepLink = null;
                            if (pendingDynamicLinkData != null) {
                                deepLink = pendingDynamicLinkData.getLink();
                                if (deepLink == null) return;
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
        } catch (Exception ignored) {
        }

    }

    /**
     * Stable device identity for splash upsert.
     * Prefer ANDROID_ID (stable across opens of the same signing-key install).
     * If missing, reuse a prefs-backed INSTALL_UUID — never mint a new UUID per open.
     */
    private String resolveStableDeviceId() {
        String androidId = null;
        try {
            androidId = Settings.Secure.getString(
                    getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        } catch (Exception ignored) {
        }
        if (androidId != null && !androidId.isEmpty() && !"null".equalsIgnoreCase(androidId)) {
            SharedPrefsData.putString(
                    getApplicationContext(), Constants.DeviceID, androidId, Constants.PREF_NAME);
            // Keep install UUID around as secondary key for server rebind.
            ensureInstallUuid();
            return androidId;
        }
        String stored = SharedPrefsData.getString(
                getApplicationContext(), Constants.DeviceID, Constants.PREF_NAME);
        if (stored != null && !stored.isEmpty() && !"null".equalsIgnoreCase(stored)) {
            return stored;
        }
        String installUuid = ensureInstallUuid();
        String fallback = "install_" + installUuid;
        SharedPrefsData.putString(
                getApplicationContext(), Constants.DeviceID, fallback, Constants.PREF_NAME);
        Logone.w("ANDROID_ID missing; using stable install UUID for DeviceID");
        return fallback;
    }

    private String ensureInstallUuid() {
        String existing = SharedPrefsData.getString(
                getApplicationContext(), Constants.INSTALL_UUID, Constants.PREF_NAME);
        if (existing != null && !existing.isEmpty() && !"null".equalsIgnoreCase(existing)) {
            return existing;
        }
        String created = UUID.randomUUID().toString();
        SharedPrefsData.putString(
                getApplicationContext(), Constants.INSTALL_UUID, created, Constants.PREF_NAME);
        return created;
    }

    private String resolveFcmToken(Task<String> task, String fallback) {
        if (task != null && task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
            String resolved = task.getResult();
            SharedPrefsData.putString(getApplicationContext(), Constants.FCM_TOKEN, resolved, Constants.PREF_NAME);
            return resolved;
        }
        return fallback != null ? fallback : "";
    }

    /**
     * Token for splash/device registration without blocking the UI thread on FCM.
     * Prefer cached value; otherwise a stable ANDROID_ID placeholder so first open
     * can call Splash_Screen even when Play Services / getToken hangs or fails.
     */
    private String ensureSplashFcmToken(String cachedToken) {
        if (cachedToken != null && !cachedToken.isEmpty() && !"null".equalsIgnoreCase(cachedToken)) {
            return cachedToken;
        }
        String existing = SharedPrefsData.getString(getApplicationContext(), Constants.FCM_TOKEN, Constants.PREF_NAME);
        if (existing != null && !existing.isEmpty() && !"null".equalsIgnoreCase(existing)) {
            return existing;
        }
        String id = DeviceID != null && !DeviceID.isEmpty()
                ? DeviceID
                : resolveStableDeviceId();
        String placeholder = (BuildConfig.DEBUG ? "debug-" : "pending-") + id;
        SharedPrefsData.putString(getApplicationContext(), Constants.FCM_TOKEN, placeholder, Constants.PREF_NAME);
        Logone.w("FCM token missing; using placeholder for splash device registration");
        return placeholder;
    }

    private void refreshFcmTokenInBackground(final String currentToken) {
        try {
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                String resolved = resolveFcmToken(task, currentToken);
                if (resolved != null && !resolved.isEmpty()
                        && (currentToken == null || !resolved.equals(currentToken))) {
                    showLog("OpenPath: FCM token updated in background");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Splash for config only — home already open; never blocks UI. */
    private void sendDeviceDetailsBackground(final String token) {
        showLog("OpenPath: background splash refresh");
        if (token != null && !token.isEmpty()) {
            SharedPrefsData.putString(getApplicationContext(), Constants.FCM_TOKEN, token, Constants.PREF_NAME);
        }
        String DRAFT_ID = "" + SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME);
        if (DRAFT_ID.equals("") || DRAFT_ID.equals("null") || DRAFT_ID.equals("0")) {
            splash = Splash_Scrren_First_Time(token != null ? token : "");
        } else {
            splash = Splash_Scrren_Second_Time();
        }
        callSplashRequest(true);
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
        // Fresh splash for first install / missing translator. Returning path may
        // already have opened home (background=true).
        callSplashRequest(false);
    }

    private void routeReturningUser() {
        // Existing Play / reporter devices with language already chosen: never interrupt.
        if (OpenPathCache.shouldSkipLanguageOnboarding(this)) {
            showLog("OpenPath: returning user skip language → home");
            openHomeFromSplash();
            return;
        }

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
            // Stale MOVE_NEXT_SCREEN=language must not override completed onboarding.
            if (OpenPathCache.shouldSkipLanguageOnboarding(this)) {
                openHomeFromSplash();
                return;
            }
            startActivity(new Intent(StartingActivity.this, LanguageActivity.class));
            finish();
            return;
        }

        // Completed onboarding from older builds may only have the flag set.
        if (SharedPrefsData.getBool(this, Constants.IS_LANGUAGE_SELECTED, Constants.PREF_NAME)) {
            openHomeFromSplash();
            return;
        }

        // Unknown local state: one recovery splash only. Never re-enqueue forever —
        // dead/fake network + empty first-run prefs used to leave the logo splash stuck.
        if (!splashNetworkInFlight && splashNetworkFailCount == 0) {
            callSplashRequest(false);
            return;
        }
        // Prefer home when device already has an ID + any prior identity; only true
        // first-run cold devices go to language after splash failure.
        String storedDeviceId = SharedPrefsData.getString(this, Constants.DeviceID, Constants.PREF_NAME);
        int draftId = SharedPrefsData.getInt(this, Constants.DraftID, Constants.PREF_NAME);
        if (storedDeviceId != null && !storedDeviceId.isEmpty() && draftId > 0) {
            showLog("OpenPath: splash failure with stored device+draft → home");
            openHomeFromSplash();
            return;
        }
        showLog("OpenPath: no cached route after splash failure → Language");
        try {
            startActivity(new Intent(StartingActivity.this, LanguageActivity.class));
            finish();
        } catch (Exception e) {
            openHomeFromSplash();
        }
    }
    public void callSplashRequest() {
        callSplashRequest(false);
    }

    /**
     * @param configOnly when true (home already shown), apply splash prefs only —
     *                   never re-open Home / onboarding screens unless force-critical.
     */
    public void callSplashRequest(final boolean configOnly){
        if (splashNetworkInFlight) {
            showLog("OpenPath: splash already in flight");
            return;
        }
        if (splash == null) {
            String token = SharedPrefsData.getString(getApplicationContext(), Constants.FCM_TOKEN, Constants.PREF_NAME);
            String DRAFT_ID = "" + SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME);
            if (DRAFT_ID.equals("") || DRAFT_ID.equals("null") || DRAFT_ID.equals("0")) {
                splash = Splash_Scrren_First_Time(token != null ? token : "");
            } else {
                splash = Splash_Scrren_Second_Time();
            }
        }
        splashNetworkInFlight = true;
        APIService apiService = RestApiClient.getClient().create(APIService.class);
        Call<EncreptionRequest> apiResponse = apiService.Splash_Screen("v3", splash);
        showLog("CHECK POINT SPLASH REQUEST:: configOnly=" + configOnly);
        NetworkCallDeduper.enqueueUnique("splash_v3", apiResponse, new Callback<EncreptionRequest>() {
            //@RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {
                splashNetworkInFlight = false;
                if (response.isSuccessful()) {
                    showLog("CHECK POINT 14 REQUEST SUCCESS::");
                    try {
                    EncreptionRequest encreptionRequest = response.body();
                    if (encreptionRequest == null || encreptionRequest.getData() == null) {
                        if (!configOnly) {
                            splashNetworkFailCount++;
                            Toast.makeText(StartingActivity.this, "Unable to start app. Please try again.", Toast.LENGTH_LONG).show();
                            routeReturningUser();
                        }
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
                        // Warm next share/house-ad seat before the user flips onto it.
                        try {
                            AdPreloader.get().prefetch(getApplicationContext());
                        } catch (Exception ignored) {
                        }

                        // if getting null then uninstall app

                        SharedPrefsData.putInt(getApplicationContext(), Constants.DraftID, startActivityReqModel.getData().getDraftId(), Constants.PREF_NAME);
                        // Never wipe a returning user's remembered language with server 0 —
                        // that re-forced "Select Content Language" for Play Store devices.
                        int previousLangId = SharedPrefsData.getInt(
                                getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME);
                        int splashLangId = startActivityReqModel.getData().getLanguageId() != null
                                ? startActivityReqModel.getData().getLanguageId()
                                : 0;
                        if (splashLangId <= 0 && previousLangId > 0) {
                            splashLangId = previousLangId;
                            showLog("OpenPath: preserve local LanguageID=" + previousLangId
                                    + " (server returned 0)");
                        }
                        SharedPrefsData.putInt(getApplicationContext(), Constants.LanguageID, splashLangId, Constants.PREF_NAME);
                        try {
                            CategoriesDiskCache.invalidateIfLanguageChanged(
                                    getApplicationContext(), splashLangId);
                        } catch (Exception ignored) {
                        }
                        SessionAnalyticsTracker.get().onIdentityReady(getApplicationContext());
                        SharedPrefsData.putInt(getApplicationContext(), Constants.Show_Animation, startActivityReqModel.getData().getShowAnimation(), Constants.PREF_NAME);
                        SharedPrefsData.putInt(getApplicationContext(), Constants.StaticDataApi, startActivityReqModel.getData().getStaticDataApi(), Constants.PREF_NAME);
                        SharedPrefsData.putInt(getApplicationContext(), Constants.StaticDataApi, startActivityReqModel.getData().getStaticDataApi(), Constants.PREF_NAME);

                        int splashDistId = 0;
                        try {
                            if (startActivityReqModel.getData().getDistId() != null) {
                                splashDistId = startActivityReqModel.getData().getDistId();
                            }
                        } catch (Exception ignored) {
                        }
                        try {
                            Integer splashStateId = startActivityReqModel.getData().getStateId();
                            if (splashStateId != null && splashStateId > 0) {
                                SharedPrefsData.putInt(
                                        getApplicationContext(),
                                        Constants.StateID,
                                        splashStateId,
                                        Constants.PREF_NAME);
                            } else if (splashDistId > 0) {
                                int found = SelectionListCache.findStateIdForDistrict(
                                        getApplicationContext(), splashLangId, splashDistId);
                                if (found > 0) {
                                    SharedPrefsData.putInt(
                                            getApplicationContext(),
                                            Constants.StateID,
                                            found,
                                            Constants.PREF_NAME);
                                }
                            }
                        } catch (Exception ignored) {
                        }
                        DistrictLabelHelper.saveFromServer(
                                getApplicationContext(),
                                splashLangId,
                                splashDistId,
                                startActivityReqModel.getData().getDistName());
                        try {
                            DistrictLabelHelper.ensureSelectedMatchesLanguage(
                                    getApplicationContext());
                        } catch (Exception ignored) {
                        }
                        String splashNext = startActivityReqModel.getData().getNextScreen();
                        if ("language".equalsIgnoreCase(splashNext)
                                && OpenPathCache.shouldSkipLanguageOnboarding(getApplicationContext())) {
                            splashNext = "main";
                            showLog("OpenPath: coerce next_screen language → main for returning user");
                        }
                        SharedPrefsData.putString(getApplicationContext(), Constants.MOVE_NEXT_SCREEN, splashNext, Constants.PREF_NAME);
                        markOnboardingCompleteIfNeeded(splashNext);

                        boolean splashLoggedIn =
                                !SharedPrefsData.getBool(getApplicationContext(), Constants.FORCE_LOGGED_OUT, Constants.PREF_NAME)
                                        && Boolean.TRUE.equals(startActivityReqModel.getData().getIsLogin());
                        SharedPrefsData.putBool(getApplicationContext(), Constants.is_login,
                                splashLoggedIn,
                                Constants.PREF_NAME);
                        // Guest / forced logout: drop leftover display name so comments never say "Srinath".
                        if (!splashLoggedIn) {
                            SharedPrefsData.putString(getApplicationContext(), Constants.USER_NAME, "", Constants.PREF_NAME);
                            SharedPrefsData.putString(getApplicationContext(), Constants.PROFILE_PIC_URL, "", Constants.PREF_NAME);
                        }

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

                        ScreenCaptureHelper.saveAllowed(
                                getApplicationContext(),
                                startActivityReqModel.getData().getAllowScreenCapture()
                        );

                        // Admin-controlled vertical offset (dp) for the half-post action icon row.
                        SharedPrefsData.putInt(
                                getApplicationContext(),
                                Constants.ACTION_ICON_ROW_OFFSET,
                                startActivityReqModel.getData().getActionIconRowOffset(),
                                Constants.PREF_NAME
                        );

                        // Admin-controlled half-post action icon glyph + white-circle sizes (dp).
                        SharedPrefsData.putInt(
                                getApplicationContext(),
                                Constants.ACTION_ICON_SIZE_DP,
                                startActivityReqModel.getData().getActionIconSizeDp(),
                                Constants.PREF_NAME
                        );
                        SharedPrefsData.putInt(
                                getApplicationContext(),
                                Constants.LIKE_CIRCLE_SIZE_DP,
                                startActivityReqModel.getData().getLikeCircleSizeDp(),
                                Constants.PREF_NAME
                        );

                        // Center magazine / special Share icon. Omitted field keeps a
                        // saved value; first launch with no key uses 32dp.
                        Integer magazineShareIconDp =
                                startActivityReqModel.getData().getMagazineShareIconSizeDp();
                        if (magazineShareIconDp != null) {
                            SharedPrefsData.putInt(
                                    getApplicationContext(),
                                    Constants.MAGAZINE_SHARE_ICON_SIZE_DP,
                                    com.anyNews.anynews.Utils.FooterStyleHelper
                                            .clampMagazineShareIconDp(magazineShareIconDp),
                                    Constants.PREF_NAME
                            );
                        } else if (!getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE)
                                .contains(Constants.MAGAZINE_SHARE_ICON_SIZE_DP)) {
                            SharedPrefsData.putInt(
                                    getApplicationContext(),
                                    Constants.MAGAZINE_SHARE_ICON_SIZE_DP,
                                    com.anyNews.anynews.Utils.FooterStyleHelper
                                            .MAGAZINE_SHARE_ICON_DEFAULT_DP,
                                    Constants.PREF_NAME
                            );
                        }

                        // Horizontal gap between article action icons. Omitted field
                        // keeps a saved value; first launch with no key uses 2dp.
                        Integer actionIconGapDp = startActivityReqModel.getData().getActionIconGapDp();
                        if (actionIconGapDp != null) {
                            SharedPrefsData.putInt(
                                    getApplicationContext(),
                                    Constants.ACTION_ICON_GAP_DP,
                                    FlipperAdapter.clampActionIconGapDp(actionIconGapDp),
                                    Constants.PREF_NAME
                            );
                        } else if (!getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE)
                                .contains(Constants.ACTION_ICON_GAP_DP)) {
                            SharedPrefsData.putInt(
                                    getApplicationContext(),
                                    Constants.ACTION_ICON_GAP_DP,
                                    FlipperAdapter.DEFAULT_ACTION_ICON_GAP_DP,
                                    Constants.PREF_NAME
                            );
                        }

                        com.anyNews.anynews.Utils.SearchIconConfig.saveFromServer(
                                getApplicationContext(),
                                startActivityReqModel.getData().getSearchIconOnEngagement(),
                                startActivityReqModel.getData().getSearchIconOnToggle(),
                                startActivityReqModel.getData().getSearchIconSizeDp()
                        );

                        // Admin-controlled reporter create-menu post-type toggles.
                        SharedPrefsData.putBool(
                                getApplicationContext(),
                                Constants.ENABLE_VIDEO_POST,
                                startActivityReqModel.getData().getEnableVideoPost(),
                                Constants.PREF_NAME
                        );
                        SharedPrefsData.putBool(
                                getApplicationContext(),
                                Constants.ENABLE_ARTICLE_POST,
                                startActivityReqModel.getData().getEnableArticlePost(),
                                Constants.PREF_NAME
                        );
                        SharedPrefsData.putBool(
                                getApplicationContext(),
                                Constants.ENABLE_FULL_POST,
                                startActivityReqModel.getData().getEnableFullPost(),
                                Constants.PREF_NAME
                        );

                        // Admin full/half autoplay (null fields keep cache; legacy-only
                        // payloads copy the global onto both card types).
                        if (startActivityReqModel.getData().hasVideoAutoplayEnabled()
                                || startActivityReqModel.getData().hasVideoAutoplayFullEnabled()
                                || startActivityReqModel.getData().hasVideoAutoplayHalfEnabled()) {
                            com.anyNews.anynews.Utils.VideoAutoplayConfig.saveFromServer(
                                    getApplicationContext(),
                                    startActivityReqModel.getData().hasVideoAutoplayEnabled()
                                            ? startActivityReqModel.getData().getVideoAutoplayEnabled()
                                            : null,
                                    startActivityReqModel.getData().hasVideoAutoplayFullEnabled()
                                            ? startActivityReqModel.getData().getVideoAutoplayFullEnabled()
                                            : null,
                                    startActivityReqModel.getData().hasVideoAutoplayHalfEnabled()
                                            ? startActivityReqModel.getData().getVideoAutoplayHalfEnabled()
                                            : null
                            );
                        }

                        // Article TTS / read-aloud (null fields = old server; leave cache).
                        ArticleTtsConfigHelper.saveFromServer(
                                getApplicationContext(),
                                startActivityReqModel.getData().getTtsEnabled(),
                                startActivityReqModel.getData().getTtsSpeakerOnImage(),
                                startActivityReqModel.getData().getTtsSpeakerOnEngagement(),
                                startActivityReqModel.getData().getTtsLocale(),
                                startActivityReqModel.getData().getTtsVoice(),
                                startActivityReqModel.getData().getTtsEngine());
                        // Init engine + voices now so first Home speaker tap is instant.
                        try {
                            ArticleTtsHelper.warmUp(getApplicationContext());
                        } catch (Exception ignored) {
                        }

                        // Feed Exo chrome A/B (null = keep cache).
                        if (startActivityReqModel.getData().hasFeedVideoPlayer()) {
                            com.anyNews.anynews.Utils.FeedVideoPlayerConfig.saveFromServer(
                                    getApplicationContext(),
                                    startActivityReqModel.getData().getFeedVideoPlayer());
                        }

                        ReporterPostGateHelper.saveFromSplash(
                                getApplicationContext(),
                                startActivityReqModel.getData().hasRequireFullProfileToPost()
                                        ? startActivityReqModel.getData().getRequireFullProfileToPost()
                                        : null,
                                startActivityReqModel.getData().hasShowWriterPhotoOnCards()
                                        ? startActivityReqModel.getData().getShowWriterPhotoOnCards()
                                        : null
                        );

                        // Admin-controlled gap (dp) between the half-post title and body.
                        SharedPrefsData.putInt(
                                getApplicationContext(),
                                Constants.TITLE_BODY_GAP_DP,
                                startActivityReqModel.getData().getTitleBodyGapDp(),
                                Constants.PREF_NAME
                        );

                        // Admin-controlled vertical offset (dp) of the half-post content block.
                        SharedPrefsData.putInt(
                                getApplicationContext(),
                                Constants.CONTENT_BLOCK_OFFSET_DP,
                                startActivityReqModel.getData().getContentBlockOffsetDp(),
                                Constants.PREF_NAME
                        );

                        // Admin push tray appearance (logo / small image / text-only / sizes).
                        if (startActivityReqModel.getData().getPnShowArticleImage() != null) {
                            SharedPrefsData.putBool(
                                    getApplicationContext(),
                                    Constants.PN_SHOW_ARTICLE_IMAGE,
                                    startActivityReqModel.getData().getPnShowArticleImage(),
                                    Constants.PREF_NAME
                            );
                        }
                        if (startActivityReqModel.getData().getPnStyleMode() != null
                                && !startActivityReqModel.getData().getPnStyleMode().isEmpty()) {
                            SharedPrefsData.putString(
                                    getApplicationContext(),
                                    Constants.PN_STYLE_MODE,
                                    startActivityReqModel.getData().getPnStyleMode(),
                                    Constants.PREF_NAME
                            );
                        }
                        if (startActivityReqModel.getData().getPnLargeIconUrl() != null) {
                            SharedPrefsData.putString(
                                    getApplicationContext(),
                                    Constants.PN_LARGE_ICON_URL,
                                    startActivityReqModel.getData().getPnLargeIconUrl(),
                                    Constants.PREF_NAME
                            );
                        }
                        if (startActivityReqModel.getData().getPushIncludeLogoDefault() != null) {
                            SharedPrefsData.putBool(
                                    getApplicationContext(),
                                    Constants.PN_INCLUDE_LOGO_DEFAULT,
                                    startActivityReqModel.getData().getPushIncludeLogoDefault(),
                                    Constants.PREF_NAME
                            );
                        }
                        if (startActivityReqModel.getData().getPnLeftIconSizeDp() != null) {
                            SharedPrefsData.putInt(
                                    getApplicationContext(),
                                    Constants.PN_LEFT_ICON_SIZE_DP,
                                    startActivityReqModel.getData().getPnLeftIconSizeDp(),
                                    Constants.PREF_NAME
                            );
                        }
                        if (startActivityReqModel.getData().getPnThumbSizeDp() != null) {
                            SharedPrefsData.putInt(
                                    getApplicationContext(),
                                    Constants.PN_THUMB_SIZE_DP,
                                    startActivityReqModel.getData().getPnThumbSizeDp(),
                                    Constants.PREF_NAME
                            );
                        }
                        if (startActivityReqModel.getData().getPnTitleSizeSp() != null) {
                            SharedPrefsData.putInt(
                                    getApplicationContext(),
                                    Constants.PN_TITLE_SIZE_SP,
                                    startActivityReqModel.getData().getPnTitleSizeSp(),
                                    Constants.PREF_NAME
                            );
                        }

                        // Admin-controlled footer + writer-strip styling. Cached as JSON and
                        // applied at render time; unset → current hardcoded look.
                        if (startActivityReqModel.getData().getFooterStyle() != null) {
                            SharedPrefsData.putString(
                                    getApplicationContext(),
                                    Constants.FOOTER_STYLE,
                                    new Gson().toJson(startActivityReqModel.getData().getFooterStyle()),
                                    Constants.PREF_NAME
                            );
                        }
                        if (startActivityReqModel.getData().getWriterStripStyle() != null) {
                            SharedPrefsData.putString(
                                    getApplicationContext(),
                                    Constants.WRITER_STRIP_STYLE,
                                    new Gson().toJson(startActivityReqModel.getData().getWriterStripStyle()),
                                    Constants.PREF_NAME
                            );
                        }
                        if (startActivityReqModel.getData().getFullVideoShareCard() != null) {
                            FullVideoShareCardHelper.save(
                                    getApplicationContext(),
                                    startActivityReqModel.getData().getFullVideoShareCard()
                            );
                        }
                        // Admin share domain (share.anynews.co.in).
                        SharePublicBaseHelper.saveFromServer(
                                getApplicationContext(),
                                startActivityReqModel.getData().getSharePublicBaseUrl());
                        if (startActivityReqModel.getData().getPollSurveyColors() != null) {
                            PollOptionStyleHelper.save(
                                    getApplicationContext(),
                                    startActivityReqModel.getData().getPollSurveyColors()
                            );
                        }

                        if (startActivityReqModel.getData().getCricketMiniPlayerPosition() != null) {
                            CricketMiniPlayerPositionHelper.save(
                                    getApplicationContext(),
                                    startActivityReqModel.getData().getCricketMiniPlayerPosition()
                            );
                        }

                        // Independent cricket surface toggles (null flags are left
                        // untouched → defaults: mini player OFF until server ON,
                        // footer tab OFF).
                        Boolean prevMini = null;
                        try {
                            // Detect flip so Home can stop/start cricket HTTP without reopen.
                            String raw = SharedPrefsData.getString(
                                    getApplicationContext(),
                                    Constants.CRICKET_MINI_PLAYER_ENABLED,
                                    Constants.PREF_NAME);
                            if (raw != null && !raw.isEmpty()) {
                                prevMini = "true".equalsIgnoreCase(raw.trim());
                            }
                        } catch (Exception ignored) {
                        }
                        Boolean prevFooter = null;
                        try {
                            String rawFooter = SharedPrefsData.getString(
                                    getApplicationContext(),
                                    Constants.CRICKET_FOOTER_TAB_ENABLED,
                                    Constants.PREF_NAME);
                            if (rawFooter != null && !rawFooter.isEmpty()) {
                                prevFooter = "true".equalsIgnoreCase(rawFooter.trim());
                            }
                        } catch (Exception ignored) {
                        }
                        CricketSurfaceConfigHelper.saveMiniPlayerEnabled(
                                getApplicationContext(),
                                startActivityReqModel.getData().getCricketMiniPlayerEnabled()
                        );
                        CricketSurfaceConfigHelper.saveFooterTabEnabled(
                                getApplicationContext(),
                                startActivityReqModel.getData().getCricketFooterTabEnabled()
                        );
                        // Profile hub / More + home footer slot flags (null = leave cache / defaults).
                        ProfileHubConfigHelper.saveFromSplash(
                                getApplicationContext(),
                                startActivityReqModel.getData().getCategoriesInFooter(),
                                startActivityReqModel.getData().getShowHeaderProfileIcon(),
                                startActivityReqModel.getData().getEnableStatusVideos(),
                                startActivityReqModel.getData().getProfileShowBookmarks(),
                                false,
                                startActivityReqModel.getData().getProfileShowSettingsRow(),
                                startActivityReqModel.getData().getProfileHeaderIconSizeDp(),
                                startActivityReqModel.getData().getProfileCategoryIconSizeDp(),
                                startActivityReqModel.getData().getProfileMenuTextSizeSp(),
                                startActivityReqModel.getData().getFooterPlusIconSizeDp(),
                                startActivityReqModel.getData().getFooterShowReload(),
                                startActivityReqModel.getData().getFooterShowSpecial(),
                                startActivityReqModel.getData().getFooterShowPlus(),
                                startActivityReqModel.getData().getFooterShowProfile(),
                                startActivityReqModel.getData().getFooterShowBookmarks(),
                                startActivityReqModel.getData().getSettingsShowNotifications(),
                                startActivityReqModel.getData().getSettingsShowVideoAutoplay(),
                                startActivityReqModel.getData().getSettingsShowNightMode(),
                                startActivityReqModel.getData().getSettingsShowLocalStories()
                        );
                        ProfileHubConfigHelper.saveBecomeAReporterEnabled(
                                getApplicationContext(),
                                startActivityReqModel.getData().getEnableBecomeAReporter());
                        ProfileHubConfigHelper.saveBirthdaySpecialEnabled(
                                getApplicationContext(),
                                startActivityReqModel.getData().getEnableBirthdaySpecial());
                        ProfileHubConfigHelper.saveNotHiringMessages(
                                getApplicationContext(),
                                startActivityReqModel.getData()
                                        .getBecomeAReporterNotHiringMessages());
                        ProfileHubConfigHelper.saveHiringMessages(
                                getApplicationContext(),
                                startActivityReqModel.getData()
                                        .getBecomeAReporterHiringMessages());
                        ProfileHubConfigHelper.saveBecomeReporterTargeting(
                                getApplicationContext(),
                                startActivityReqModel.getData()
                                        .getBecomeAReporterLanguageIds(),
                                startActivityReqModel.getData()
                                        .getBecomeAReporterStateIds(),
                                startActivityReqModel.getData()
                                        .getBecomeAReporterDistIds());
                        // Contact Us mailto target from Root_Settings (null/blank keeps cache).
                        ProfileHubConfigHelper.saveContactEmail(
                                getApplicationContext(),
                                startActivityReqModel.getData().getContactEmail());
                        ProfileHubConfigHelper.saveFullJatakamWhatsapp(
                                getApplicationContext(),
                                startActivityReqModel.getData().getFullJatakamWhatsapp());
                        ProfileHubConfigHelper.saveDefaultRashiId(
                                getApplicationContext(),
                                startActivityReqModel.getData().getDefaultRashiId());
                        ProfileHubConfigHelper.saveCalendarPanchangSource(
                                getApplicationContext(),
                                startActivityReqModel.getData().getCalendarPanchangSource());
                        CalendarUiConfigHelper.saveFromSplash(
                                getApplicationContext(),
                                startActivityReqModel.getData().getCalendarDetailCardMinHeightDp(),
                                startActivityReqModel.getData().getCalendarDetailTitleSizeSp(),
                                startActivityReqModel.getData().getCalendarDetailItemSizeSp(),
                                startActivityReqModel.getData().getCalendarDetailItemPadDp(),
                                startActivityReqModel.getData().getCalendarMonthHeightFraction(),
                                startActivityReqModel.getData()
                                        .getCalendarSelectedCircleRadiusPercent());
                        TtdConfigHelper.saveFromSplash(
                                getApplicationContext(),
                                startActivityReqModel.getData().getTtd());
                        LocalUpdatesConfigHelper.saveWeatherFromSplash(
                                getApplicationContext(),
                                startActivityReqModel.getData().getWeather());
                        LocalUpdatesConfigHelper.saveFuelFromSplash(
                                getApplicationContext(),
                                startActivityReqModel.getData().getFuel());
                        LocalUpdatesConfigHelper.saveGoldFromSplash(
                                getApplicationContext(),
                                startActivityReqModel.getData().getGold());
                        LocalUpdatesConfigHelper.saveJobsFromSplash(
                                getApplicationContext(),
                                startActivityReqModel.getData().getJobs());
                        DailyUpdatesConfigHelper.saveFromSplash(
                                getApplicationContext(),
                                startActivityReqModel.getData().getDailyUpdates());
                        DailyUpdatesConfigHelper.saveStockSummaryFromSplash(
                                getApplicationContext(),
                                startActivityReqModel.getData().getStockMarket());
                        DailyUpdatesConfigHelper.saveFlashHeadlinesFromSplash(
                                getApplicationContext(),
                                startActivityReqModel.getData().getFlashNews());
                        DailyUpdatesConfigHelper.saveLocalHeadlinesFromSplash(
                                getApplicationContext(),
                                startActivityReqModel.getData().getLocalHeadlines());
                        // Admin notification re-prompt (one OS ask next open when due).
                        if (startActivityReqModel.getData().getNotificationRepromptEnabled() != null) {
                            SharedPrefsData.putBool(
                                    getApplicationContext(),
                                    Constants.NOTIFICATION_REPROMPT_ENABLED,
                                    Boolean.TRUE.equals(
                                            startActivityReqModel.getData().getNotificationRepromptEnabled()),
                                    Constants.PREF_NAME);
                        }
                        // Always persist (incl. false) so a prior ON cannot stick after admin turns OFF.
                        SharedPrefsData.putBool(
                                getApplicationContext(),
                                Constants.PROMPT_NOTIFICATIONS_ON_OPEN,
                                Boolean.TRUE.equals(
                                        startActivityReqModel.getData().getPromptNotificationsOnOpen()),
                                Constants.PREF_NAME);
                        if (startActivityReqModel.getData().getNotificationRepromptDue() != null) {
                            boolean serverDue = Boolean.TRUE.equals(
                                    startActivityReqModel.getData().getNotificationRepromptDue());
                            // Prefer splash due; isAdminNotificationRepromptDue also
                            // gates on local shown_wave + OS off.
                            SharedPrefsData.putBool(
                                    getApplicationContext(),
                                    Constants.NOTIFICATION_REPROMPT_DUE,
                                    serverDue,
                                    Constants.PREF_NAME);
                        }
                        if (startActivityReqModel.getData().getNotificationRepromptWave() != null) {
                            SharedPrefsData.putInt(
                                    getApplicationContext(),
                                    Constants.NOTIFICATION_REPROMPT_WAVE,
                                    startActivityReqModel.getData().getNotificationRepromptWave(),
                                    Constants.PREF_NAME);
                        }
                        // Both admin modes OFF, or server due=false → drop sticky
                        // local due so Home cannot ask from a prior one-time window.
                        boolean splashAskOnOpen = Boolean.TRUE.equals(
                                startActivityReqModel.getData().getPromptNotificationsOnOpen());
                        boolean splashCampaignOn = Boolean.TRUE.equals(
                                startActivityReqModel.getData().getNotificationRepromptEnabled());
                        if (!splashAskOnOpen && !splashCampaignOn) {
                            SharedPrefsData.putBool(
                                    getApplicationContext(),
                                    Constants.NOTIFICATION_REPROMPT_DUE,
                                    false,
                                    Constants.PREF_NAME);
                        } else if (startActivityReqModel.getData().getNotificationRepromptDue() != null
                                && !Boolean.TRUE.equals(
                                        startActivityReqModel.getData().getNotificationRepromptDue())) {
                            SharedPrefsData.putBool(
                                    getApplicationContext(),
                                    Constants.NOTIFICATION_REPROMPT_DUE,
                                    false,
                                    Constants.PREF_NAME);
                        }
                        // Notification opt-in: server true → local true. Server false only when
                        // user previously opted out / re-prompt path — do not force OFF on first
                        // open before OS permission was ever offered (legacy empty-FCM drafts).
                        if (startActivityReqModel.getData().getNotificationFlag() != null) {
                            boolean serverFlag = Boolean.TRUE.equals(
                                    startActivityReqModel.getData().getNotificationFlag());
                            if (serverFlag) {
                                SharedPrefsData.putBool(
                                        getApplicationContext(),
                                        Constants.NOTIFICATION_ENABLED,
                                        true,
                                        Constants.PREF_NAME);
                            } else {
                                // Keep false only if we already asked OS or user had offline pref false.
                                int askedVersion = SharedPrefsData.getInt(
                                        getApplicationContext(),
                                        Constants.NOTIFICATION_RUNTIME_ASK_VERSION,
                                        Constants.PREF_NAME);
                                boolean everAsked = askedVersion > 0;
                                boolean existingPref = SharedPrefsData.getBool(
                                        getApplicationContext(),
                                        Constants.NOTIFICATION_ENABLED,
                                        Constants.PREF_NAME,
                                        true);
                                if (everAsked || !existingPref) {
                                    SharedPrefsData.putBool(
                                            getApplicationContext(),
                                            Constants.NOTIFICATION_ENABLED,
                                            false,
                                            Constants.PREF_NAME);
                                } else {
                                    // Fresh / never-asked: keep opt-in true until user denies OS.
                                    SharedPrefsData.putBool(
                                            getApplicationContext(),
                                            Constants.NOTIFICATION_ENABLED,
                                            true,
                                            Constants.PREF_NAME);
                                }
                            }
                        } else {
                            // Missing key: default ON until user turns off.
                            boolean hasKey;
                            try {
                                hasKey = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE)
                                        .contains(Constants.NOTIFICATION_ENABLED);
                            } catch (Exception e) {
                                hasKey = false;
                            }
                            if (!hasKey) {
                                SharedPrefsData.putBool(
                                        getApplicationContext(),
                                        Constants.NOTIFICATION_ENABLED,
                                        true,
                                        Constants.PREF_NAME);
                            }
                        }
                        Integer pollSec = startActivityReqModel.getData()
                                .getCricketMiniPlayerPollIntervalSeconds();
                        if (pollSec != null && pollSec > 0) {
                            CricketSurfaceConfigHelper.savePollIntervalSeconds(
                                    getApplicationContext(), pollSec);
                        }
                        Integer idleSec = startActivityReqModel.getData()
                                .getCricketMiniPlayerIdlePollIntervalSeconds();
                        if (idleSec != null && idleSec > 0) {
                            CricketSurfaceConfigHelper.saveIdlePollIntervalSeconds(
                                    getApplicationContext(), idleSec);
                        }
                        Integer hbSec = startActivityReqModel.getData()
                                .getSessionHeartbeatIntervalSeconds();
                        Boolean liveEnabled = startActivityReqModel.getData().getSessionLiveEnabled();
                        Integer liveWindow = startActivityReqModel.getData()
                                .getSessionLiveWindowSeconds();
                        // True-live triad from splash. Interval 0 / enabled false = no heartbeats.
                        CricketSurfaceConfigHelper.applyPresenceFromServer(
                                getApplicationContext(),
                                liveEnabled,
                                hbSec != null ? Math.max(0, hbSec) : null,
                                liveWindow);
                        // Offline News pack max (default 24; hard 8–50).
                        OfflineFeedStore.savePackMaxFromServer(
                                getApplicationContext(),
                                startActivityReqModel.getData().getOfflineNewsPackMax());
                        // News|Read bar first-open auto-hide seconds (default 3; 0 = off).
                        HomeModeChromeConfig.saveAutoHideSecondsFromServer(
                                getApplicationContext(),
                                startActivityReqModel.getData()
                                        .getHomeModeChromeAutoHideSeconds());
                        // Main News FlipView tap modes (default hybrid).
                        NewsTapModeConfig.saveFromServer(
                                getApplicationContext(),
                                startActivityReqModel.getData().getNewsTapMode(),
                                startActivityReqModel.getData().getNewsTapHalfUpperPct(),
                                startActivityReqModel.getData().getNewsTapFullUpperPct(),
                                startActivityReqModel.getData().getNewsTapFullRightRailPct(),
                                startActivityReqModel.getData().getNewsTapHalfHideEngagement());
                        DistrictPinVisibilityConfig.saveFromServer(
                                getApplicationContext(),
                                startActivityReqModel.getData().getHideDistrictOnMainFeed(),
                                startActivityReqModel.getData().getRewardsMaster(),
                                startActivityReqModel.getData().getRewardsInMainFeedSlot(),
                                startActivityReqModel.getData().getRewardsExcludeReporters(),
                                startActivityReqModel.getData().getFlashNewsInMainFeedSlot(),
                                startActivityReqModel.getData().getKeepRewardsAfterJoin(),
                                startActivityReqModel.getData().getShowFlashAndRewardsTogether());
                        DistrictPinVisibilityConfig.saveLocationPillOpensLocalFromServer(
                                getApplicationContext(),
                                startActivityReqModel.getData().getLocationPillOpensLocalFeed());
                        DistrictPinVisibilityConfig.saveWalletVisibilityFromServer(
                                getApplicationContext(),
                                startActivityReqModel.getData().getShowWalletInProfile());
                        DistrictPinVisibilityConfig.saveReporterGate(
                                getApplicationContext(),
                                startActivityReqModel.getData().getRewardsExcludeReporters(),
                                startActivityReqModel.getData().getRewardsAllowReporters());
                        FlashNewsChipHelper.saveLabelFromServer(
                                getApplicationContext(),
                                startActivityReqModel.getData().getFlashNewsChipLabel());
                        DistrictPinVisibilityConfig.savePairLayoutFromServer(
                                getApplicationContext(),
                                startActivityReqModel.getData().getDualChipSizeDp(),
                                startActivityReqModel.getData().getDualChipShiftLeftDp(),
                                startActivityReqModel.getData().getDualChipShiftUpDp());
                        ReporterPaymentAppConfig.saveFromServer(
                                getApplicationContext(),
                                startActivityReqModel.getData().getReporterArticlesFromDate(),
                                startActivityReqModel.getData().getReporterPaymentBannerActive(),
                                startActivityReqModel.getData().getReporterPaymentBannerText(),
                                startActivityReqModel.getData().getReporterPaymentHistoryNoticeActive(),
                                startActivityReqModel.getData().getReporterPaymentHistoryNoticeText());
                        FestivalSplashHelper.saveFromServer(
                                getApplicationContext(),
                                startActivityReqModel.getData().getFestivalSplashEnabled(),
                                startActivityReqModel.getData().getFestivalSplashShowOnPush(),
                                startActivityReqModel.getData().getFestivalSplashShowOnShare(),
                                startActivityReqModel.getData().getFestivalSplashDurationSeconds(),
                                startActivityReqModel.getData().getFestivalSplashImageUrl(),
                                startActivityReqModel.getData().getFestivalSplashUpdatedAt());
                        FestivalSplashHelper.prefetchInBackground(getApplicationContext());
                        com.anyNews.anynews.rewards.RewardsConfigHelper.saveProgramWindow(
                                getApplicationContext(),
                                startActivityReqModel.getData().getRewardsProgramStatus(),
                                startActivityReqModel.getData().getRewardsScoringOpen(),
                                startActivityReqModel.getData().getRewardsPeriodEnded(),
                                startActivityReqModel.getData().getRewardsPeriodUpcoming(),
                                startActivityReqModel.getData().getRewardsPeriodBanner());
                        com.anyNews.anynews.rewards.RewardsConfigHelper.saveChipCopyFromServer(
                                getApplicationContext(),
                                startActivityReqModel.getData().getRewardsGuestChip(),
                                startActivityReqModel.getData().getRewardsJoinedChip(),
                                startActivityReqModel.getData().getRewardsRateLabel(),
                                startActivityReqModel.getData().getRewardsChipColor(),
                                startActivityReqModel.getData().getRewardsChipSizeSp(),
                                startActivityReqModel.getData().getRewardsChipWeight());
                        try {
                            String capsJson =
                                    startActivityReqModel.getData().getRewardsFactorCaps();
                            if (capsJson != null && !capsJson.trim().isEmpty()) {
                                com.anyNews.anynews.rewards.RewardsConfigHelper
                                        .applyFactorCapsJson(
                                                getApplicationContext(), capsJson);
                            } else if (startActivityReqModel.getData().getRewardsAdsSeenCap()
                                            != null
                                    || startActivityReqModel.getData().getRewardsAdsSeenCapped()
                                            != null) {
                                com.google.gson.JsonObject ads = new com.google.gson.JsonObject();
                                Integer cap =
                                        startActivityReqModel.getData().getRewardsAdsSeenCap();
                                Integer rem =
                                        startActivityReqModel
                                                .getData()
                                                .getRewardsAdsSeenRemaining();
                                Boolean capped =
                                        startActivityReqModel.getData().getRewardsAdsSeenCapped();
                                if (cap != null) ads.addProperty("cap_per_day", cap);
                                if (rem != null) ads.addProperty("remaining", rem);
                                if (capped != null) ads.addProperty("capped", capped);
                                com.google.gson.JsonObject wrap = new com.google.gson.JsonObject();
                                wrap.add("ads_seen", ads);
                                com.anyNews.anynews.rewards.RewardsConfigHelper.applyFactorCaps(
                                        getApplicationContext(),
                                        wrap,
                                        com.anyNews.anynews.rewards.RewardsConfigHelper.istDayKey());
                            }
                        } catch (Exception ignored) {
                        }
                        // Read / చదివినవి recency window (default 24h; 1–168).
                        ReadHistoryConfig.saveHoursFromServer(
                                getApplicationContext(),
                                startActivityReqModel.getData().getReadHistoryHours());
                        // Recent Searches TTL days (default 7; 1–90) — local prune only.
                        com.anyNews.anynews.Utils.RecentSearchStore.saveTtlDays(
                                getApplicationContext(),
                                startActivityReqModel.getData().getSearchRecentTtlDays());
                        // Status Videos lookback days (default 5; 1–30).
                        StatusVideosDaysConfig.saveDaysFromServer(
                                getApplicationContext(),
                                startActivityReqModel.getData().getStatusVideosDays());
                        // Feed ad cadence + format/unit + Android prefetch/concurrency + impression flush
                        // + destroy on flip-away (fresh fill on return; default ON).
                        FeedAdConfig.saveFromServer(
                                getApplicationContext(),
                                startActivityReqModel.getData().getFeedAdEveryN(),
                                startActivityReqModel.getData().getAdPrefetchLookahead(),
                                startActivityReqModel.getData().getMaxNativeAdLoadsInFlight(),
                                startActivityReqModel.getData().getAdImprFlushEveryN(),
                                startActivityReqModel.getData().getFeedAdFormat(),
                                startActivityReqModel.getData().getAdCode(),
                                startActivityReqModel.getData()
                                        .getDestroyNativeAdsAfterImpression(),
                                startActivityReqModel.getData().getUseSecondNativeAdUnit(),
                                startActivityReqModel.getData().getAdCodeSecondary(),
                                startActivityReqModel.getData().getFeedNativeAdUnitMode());
                        FeedAdConfig.warmFromPrefs(getApplicationContext());
                        AnalyticsBatchConfig.saveFromServer(
                                getApplicationContext(),
                                startActivityReqModel.getData().getViewsBatchFlushIntervalSeconds(),
                                startActivityReqModel.getData().getViewsBatchFlushAtCount(),
                                startActivityReqModel.getData().getMediaAccessFlushIntervalSeconds(),
                                startActivityReqModel.getData()
                                        .getRewardsEngagementFlushIntervalSeconds());
                        AnalyticsBatchConfig.warmFromPrefs(getApplicationContext());
                        Boolean newMini = startActivityReqModel.getData()
                                .getCricketMiniPlayerEnabled();
                        Boolean newFooter = startActivityReqModel.getData()
                                .getCricketFooterTabEnabled();
                        boolean miniFlipped = newMini != null && prevMini != null
                                && prevMini != newMini;
                        boolean footerFlipped = newFooter != null && prevFooter != null
                                && prevFooter != newFooter;
                        if (miniFlipped || footerFlipped) {
                            try {
                                androidx.localbroadcastmanager.content.LocalBroadcastManager
                                        .getInstance(getApplicationContext())
                                        .sendBroadcast(new android.content.Intent(
                                                SessionAnalyticsTracker.ACTION_CRICKET_STATE_CHANGED));
                            } catch (Exception ignored) {
                            }
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
                        Map<String, String> splashCustomFonts =
                                startActivityReqModel.getData().getCustomFontUrls();
                        if (splashCustomFonts != null) {
                            FontSettingsHelper.saveCustomFontUrls(
                                    getApplicationContext(),
                                    splashCustomFonts
                            );
                            FontSettingsHelper.prefetchCustomFonts(getApplicationContext());
                        }

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
                        // Admin update-banner subtitle (empty → Home uses hardcoded fallback).
                        String splashUpdateSubtitle =
                                startActivityReqModel.getData().getUpdateBannerSubtitle();
                        SharedPrefsData.putString(
                                getApplicationContext(),
                                Constants.UPDATE_BANNER_SUBTITLE,
                                splashUpdateSubtitle != null ? splashUpdateSubtitle.trim() : "",
                                Constants.PREF_NAME);

                        if(startActivityReqModel.getData().getPost_id()!=null){
                            showLog("POST_ID::"+startActivityReqModel.getData().getPost_id());
                            SharedPrefsData.putString(getApplicationContext(), Constants.REFERRER_POST, startActivityReqModel.getData().getPost_id(), Constants.PREF_NAME);

                        }

                        OpenPathCache.markSplashSuccess(getApplicationContext());

                        if (configOnly) {
                            // Soft config refresh only — do not re-navigate.
                            maybeBackgroundRefreshStatic(startActivityReqModel);
                            return;
                        }

                        // 0=silenced, 1=up-to-date/disabled, 2=soft, 3=force.
                        // Soft/force no longer block splash with a center dialog — proceed to
                        // onboarding/Home; Home shows the sticky bottom update banner.
                        int updateMenuStatus = startActivityReqModel.getData().getUpdateMenuStatus();
                        if (updateMenuStatus == 1 || updateMenuStatus == 0
                                || updateMenuStatus == 2 || updateMenuStatus == 3) {

                            int language_id = SharedPrefsData.getInt(getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME);

                            if (language_id == 0 && OpenPathCache.shouldSkipLanguageOnboarding(getApplicationContext())) {
                                // Returning user with IS_LANGUAGE_SELECTED / main route but lang wiped — home.
                                openHomeFromSplash();
                            } else if (language_id == 0) {
                                navigateAfterSplash(startActivityReqModel);
                            } else {
                                // Prefer cached translator; only fetch static when stale or missing.
                                if (!OpenPathCache.shouldRefreshStatic(getApplicationContext())) {
                                    showLog("OpenPath: skip get_static_data (fresh cache)");
                                    navigateAfterSplash(startActivityReqModel);
                                } else {
                                    fetchStaticThenNavigate(startActivityReqModel);
                                }
                            }


                        }

                    } else {
                        // Server returned success:false (e.g. device_drafts id E11000).
                        // Never leave the user stuck on the logo splash — fail-open.
                        showLog("Splash success=false, fail-open route");
                        splashNetworkFailCount++;
                        if (!configOnly) {
                            Toast.makeText(StartingActivity.this,
                                    "Something went wrong..please try again after some time  ",
                                    Toast.LENGTH_LONG).show();
                            routeReturningUser();
                        }
                    }
                    } catch (Exception e) {
                        e.printStackTrace();
                        splashNetworkFailCount++;
                        if (!configOnly) {
                            Toast.makeText(StartingActivity.this, "Unable to start app. Please try again.", Toast.LENGTH_LONG).show();
                            routeReturningUser();
                        }
                    }

                } else {
                    showLog("Splash response unsuccessful, using cached route");
                    if (!configOnly) {
                        routeReturningUser();
                    }
                }

            }

            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {
                splashNetworkInFlight = false;
                splashNetworkFailCount++;
                showLog("Splash request failed, using cached route::" + t);
                if (!configOnly) {
                    routeReturningUser();
                }
            }
        });


    }

    private void maybeBackgroundRefreshStatic(final StartActivityReqModel startActivityReqModel) {
        int language_id = SharedPrefsData.getInt(getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME);
        if (language_id == 0 || !OpenPathCache.shouldRefreshStatic(getApplicationContext())) {
            return;
        }
        fetchStaticThenNavigate(startActivityReqModel, true);
    }

    private void fetchStaticThenNavigate(final StartActivityReqModel startActivityReqModel) {
        fetchStaticThenNavigate(startActivityReqModel, false);
    }

    private void fetchStaticThenNavigate(final StartActivityReqModel startActivityReqModel, final boolean configOnly) {
        JsonObject otp = set_static_data();
        APIService apiService = RestApiClient.getClient().create(APIService.class);
        Call<JsonObject> otp_generation = apiService.get_static_data("v3", otp);

        NetworkCallDeduper.enqueueUnique("static_v3_" + SharedPrefsData.getInt(getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME),
                otp_generation, new Callback<JsonObject>() {

            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                if (response.isSuccessful() && response.body() != null) {

                    JsonObject jsonObject = response.body().getAsJsonObject();

                    Gson gson = new Gson();

                    LanguageModelResponse languageSelectionResponse = gson.fromJson(jsonObject, LanguageModelResponse.class);

                    if (languageSelectionResponse.getSuccess()) {

                        prefManager.setShared_Booleen(Allsharedpreference.Language, true);

                        String json = gson.toJson(languageSelectionResponse); // myObject - instance of MyObject
                        int langId = SharedPrefsData.getInt(
                                getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME);
                        TranslatorPackHelper.savePack(
                                getApplicationContext(), langId, languageSelectionResponse);
                        prefManager.setShared_String(Allsharedpreference.Translator, json);
                        OpenPathCache.markStaticSuccess(getApplicationContext());

                        SharedPrefsData.putString(StartingActivity.this, Constants.State_Name, languageSelectionResponse.getNextScreen(), Constants.PREF_NAME);
                    }
                }
                if (!configOnly) {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            if ("main".equals(startActivityReqModel.getData().getNextScreen())
                                    || (startActivityReqModel.getData().getNextScreen() != null
                                    && !startActivityReqModel.getData().getNextScreen().isEmpty())) {
                                navigateAfterSplash(startActivityReqModel);
                                return;
                            }
                        } catch (Exception ignored) {
                        }
                    }
                    if ("main".equals(startActivityReqModel.getData().getNextScreen())) {
                        openHomeFromSplash();
                    } else {
                        navigateAfterSplash(startActivityReqModel);
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                if (!configOnly) {
                    if ("main".equals(startActivityReqModel.getData().getNextScreen())
                            || OpenPathCache.hasTranslator(getApplicationContext())) {
                        openHomeFromSplash();
                    }
                }
            }
        });
    }

    private void navigateAfterSplash(StartActivityReqModel startActivityReqModel) {
        try {
            if (OpenPathCache.shouldSkipLanguageOnboarding(getApplicationContext())) {
                openHomeFromSplash();
                return;
            }

            scrren = startActivityReqModel.getData().getNextScreen();
            if (scrren == null) {
                scrren = "main";
            }
            // Prefer coerced prefs (may have forced language → main for returning users).
            String prefsNext = SharedPrefsData.getString(
                    getApplicationContext(), Constants.MOVE_NEXT_SCREEN, Constants.PREF_NAME);
            if (prefsNext != null && !prefsNext.isEmpty()) {
                scrren = prefsNext;
            }

            if (scrren.equals("language")) {
                if (OpenPathCache.shouldSkipLanguageOnboarding(getApplicationContext())) {
                    openHomeFromSplash();
                    return;
                }
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
                openHomeFromSplash();
            }

        } catch (Exception e) {
            openHomeFromSplash();
        }
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
        // Always send DeviceID on returning splash — Play second-open used to omit it,
        // which broke orphan/Mongo rebind and forced language select for existing devices.
        String returningDeviceId = DeviceID;
        if (returningDeviceId == null || returningDeviceId.isEmpty()) {
            returningDeviceId = SharedPrefsData.getString(
                    getApplicationContext(), Constants.DeviceID, Constants.PREF_NAME);
        }
        if (returningDeviceId == null || returningDeviceId.isEmpty()) {
            returningDeviceId = resolveStableDeviceId();
        }
        requestModel.setDeviceID(returningDeviceId);
        requestModel.setInstall_uuid(ensureInstallUuid());
        requestModel.setReferralLink("" + SharedPrefsData.getString(getApplicationContext(), Constants.REFERAL_LINK, Constants.PREF_NAME));  //
        requestModel.setReferralLink2("" + SharedPrefsData.getString(getApplicationContext(), Constants.REFERAL_LINK2, Constants.PREF_NAME));  //
        requestModel.setReferredBy(SharedPrefsData.getLong(getApplicationContext(), Constants.CUSST_ID, Constants.PREF_NAME));  //
        requestModel.setFcmToken("" +  SharedPrefsData.getString(getApplicationContext(), Constants.FCM_TOKEN, Constants.PREF_NAME));
        showLog("TOKEN_VALUE::: "+SharedPrefsData.getString(getApplicationContext(), Constants.FCM_TOKEN, Constants.PREF_NAME));
        requestModel.setLanguage_id(""+ SharedPrefsData.getInt(getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME));
        requestModel.setApp_version_name(""+BuildConfig.VERSION_NAME);
        requestModel.setApp_version_number(""+BuildConfig.VERSION_CODE);
        requestModel.setFb_ref_key(""+facebookReferrelKey);
        attachDeviceGpsIfAvailable(requestModel);
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

        requestModel.setDeviceID(DeviceID != null && !DeviceID.isEmpty()
                ? DeviceID
                : resolveStableDeviceId());
        requestModel.setInstall_uuid(ensureInstallUuid());
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
        attachDeviceGpsIfAvailable(requestModel);


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

    /** Quiet last-known GPS for device_drafts — no permission prompts / no UI block. */
    private void attachDeviceGpsIfAvailable(StartingAppRequest requestModel) {
        try {
            DeviceLocationHelper.Coords coords = DeviceLocationHelper.readIfPermitted(getApplicationContext());
            if (coords == null) return;
            requestModel.setLatitude(String.valueOf(coords.latitude));
            requestModel.setLongitude(String.valueOf(coords.longitude));
            DeviceLocationHelper.markGpsSent(getApplicationContext());
        } catch (Exception ignored) {
        }
    }

    private void markOnboardingCompleteIfNeeded(String nextScreen) {
        if ("main".equalsIgnoreCase(nextScreen)) {
            SharedPrefsData.putBool(getApplicationContext(), Constants.IS_LANGUAGE_SELECTED, true, Constants.PREF_NAME);
        }
    }

    private void openHomeFromSplash() {
        if (homeOpenedFromSplash) {
            showLog("OpenPath: home already launched");
            return;
        }
        if (festivalHoldHome || festivalAwaitingDownload) {
            pendingHomeAfterFestival = true;
            showLog("OpenPath: festival splash hold");
            return;
        }
        homeOpenedFromSplash = true;
        markOnboardingCompleteIfNeeded("main");
        showLog("CHECK POINT HOMEPAGE fallback");
        // Prefetch pickers as soon as user reaches main (also runs from HomePage).
        SelectionListCache.prefetchInBackground(getApplicationContext());
        // Race List_All_Articles while Home inflates (no offline-pack first paint).
        OpenPathFeedPrefetcher.maybePrefetchMainPage0(this, TITLE, articleId1);
        Intent intent = new Intent(StartingActivity.this, HomePage.class);
        // Avoid stacking a second HomePage on top of an existing one when a
        // WhatsApp share re-enters via singleTask splash — that made magazine
        // open then “automatically back” to the prior Home (or WhatsApp on crash).
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("EXTRA_SESSION_ID", "0");
        boolean pushTitle = TITLE != null && !TITLE.isEmpty()
                && !"null".equalsIgnoreCase(TITLE) && !"0".equals(TITLE.trim());
        if (pushTitle) {
            intent.putExtra("notification", true);
            intent.putExtra("TITLE", TITLE);
            intent.putExtra("TITILE", TITLE);
            attachPushTrayId(intent, TITLE.trim());
        } else {
            intent.putExtra("TITLE", "0");
        }
        intent.putExtra("TITLE_REWARDS", TITLE_REWARDS);
        intent.putExtra("SHARE_REDIRECT_ID", articleId1);
        intent.putExtra("SHARE_GALLERY_PAGE", shareGalleryPage);
        intent.putExtra("JOB_REDIRECT_ID", jobId1);
        intent.putExtra("DATAFROM2G", DATAFROM2G);
        if (getApplicationContext() != null && getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)) {
            startActivity(intent);
            finish();
        }
    }

    /**
     * When admin Opening Banner is ON for this launch path and image URL is set,
     * show ONLY the full-bleed banner stretched FIT_XY (never the orange logo theme splash).
     * If local cache is cold, keep theme-color splash, download, then paint.
     * OFF → immediate return (no OpenPath delay).
     */
    private void maybeArmFestivalSplash() {
        try {
            if (!shouldShowFestivalForThisOpen()) {
                if (FestivalSplashHelper.isAnyShowPathEnabled(this)
                        && !FestivalSplashHelper.hasReadyLocalImage(this)) {
                    FestivalSplashHelper.prefetchInBackground(getApplicationContext());
                }
                releaseFestivalSplashKeep();
                return;
            }
            if (FestivalSplashHelper.getImageUrl(this).isEmpty()) {
                showLog("OpenPath: opening banner ON but empty image url — skip");
                releaseFestivalSplashKeep();
                return;
            }
            if (FestivalSplashHelper.hasReadyLocalImage(this)) {
                if (paintFestivalBannerAndHold()) {
                    releaseFestivalSplashKeep();
                } else {
                    // Decode failed — try download once more rather than logo.
                    beginFestivalDownloadThenArm();
                }
                return;
            }
            beginFestivalDownloadThenArm();
        } catch (Exception e) {
            festivalHoldHome = false;
            festivalAwaitingDownload = false;
            releaseFestivalSplashKeep();
            Logone.w("festival splash arm failed: " + (e.getMessage() != null ? e.getMessage() : e));
        }
    }

    private void beginFestivalDownloadThenArm() {
        festivalAwaitingDownload = true;
        showLog("OpenPath: opening banner ON — waiting for image (theme-color hold, no logo)");
        FestivalSplashHelper.ensureLocalImageAsync(
                this,
                FestivalSplashHelper.ENSURE_TIMEOUT_MS,
                this::onFestivalDownloadAttemptFinished);
    }

    private void onFestivalDownloadAttemptFinished() {
        try {
            if (isFinishing() || isDestroyed()) {
                festivalAwaitingDownload = false;
                releaseFestivalSplashKeep();
                return;
            }
            festivalAwaitingDownload = false;
            if (FestivalSplashHelper.hasReadyLocalImage(this)
                    && paintFestivalBannerAndHold()) {
                releaseFestivalSplashKeep();
                return;
            }
            showLog("OpenPath: opening banner download/decode failed — theme-color open, no logo");
            releaseFestivalSplashKeep();
            if (pendingHomeAfterFestival && !homeOpenedFromSplash) {
                openHomeFromSplash();
            }
        } catch (Exception e) {
            festivalAwaitingDownload = false;
            festivalHoldHome = false;
            releaseFestivalSplashKeep();
            Logone.w("festival splash after download: "
                    + (e.getMessage() != null ? e.getMessage() : e));
            if (pendingHomeAfterFestival && !homeOpenedFromSplash
                    && !isFinishing() && !isDestroyed()) {
                openHomeFromSplash();
            }
        }
    }

    /** @return true when banner painted and Home hold armed */
    private boolean paintFestivalBannerAndHold() {
        Bitmap banner = FestivalSplashHelper.decodeLocalBitmap(this);
        if (banner == null || banner.getWidth() < 1 || banner.getHeight() < 1) {
            showLog("OpenPath: opening banner decode failed");
            return false;
        }
        final int gapColor = FestivalSplashHelper.sampleAndStoreGapColor(this, banner);
        try {
            getWindow().setBackgroundDrawable(new ColorDrawable(gapColor));
        } catch (Exception ignored) {
        }
        try {
            View root = findViewById(android.R.id.content);
            if (root != null) {
                root.setBackgroundColor(gapColor);
            }
            // Never paint a solid plate over the banner ImageView (z-order).
            if (net_avilable != null) {
                net_avilable.setBackgroundColor(Color.TRANSPARENT);
                net_avilable.setVisibility(View.GONE);
            }
        } catch (Exception ignored) {
        }
        logo_image_server.setScaleType(ImageView.ScaleType.FIT_XY);
        logo_image_server.setAdjustViewBounds(false);
        logo_image_server.setBackgroundColor(gapColor);
        logo_image_server.setImageBitmap(banner);
        logo_image_server.setVisibility(View.VISIBLE);
        try {
            logo_image_server.bringToFront();
        } catch (Exception ignored) {
        }
        logo_image.setVisibility(View.GONE);
        if (splashCenterLogo != null) {
            splashCenterLogo.setVisibility(View.GONE);
        }
        if (welcome != null) {
            welcome.setVisibility(View.GONE);
        }
        festivalHoldHome = true;
        final int durationMs = FestivalSplashHelper.getDurationMs(this);
        showLog("OpenPath: festival splash armed ms=" + durationMs);
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                festivalHoldHome = false;
                if (pendingHomeAfterFestival && !homeOpenedFromSplash
                        && !isFinishing() && !isDestroyed()) {
                    openHomeFromSplash();
                }
            }
        }, durationMs);
        return true;
    }

    private void releaseFestivalSplashKeep() {
        splashKeepUntilReady = false;
    }

    /**
     * Admin path toggles: normal ({@link FestivalSplashHelper#isEnabled}),
     * push ({@link FestivalSplashHelper#shouldShowOnPush}),
     * share/deep-link ({@link FestivalSplashHelper#shouldShowOnShare}).
     */
    private boolean shouldShowFestivalForThisOpen() {
        if (isPushOpenForFestival()) {
            boolean on = FestivalSplashHelper.shouldShowOnPush(this);
            if (!on) {
                showLog("OpenPath: festival skip (push; admin show_on_push OFF)");
            }
            return on;
        }
        if (isShareOrDeepLinkOpenForFestival()) {
            boolean on = FestivalSplashHelper.shouldShowOnShare(this);
            if (!on) {
                showLog("OpenPath: festival skip (share/deep link; admin show_on_share OFF)");
            }
            return on;
        }
        boolean on = FestivalSplashHelper.isEnabled(this);
        return on;
    }

    /** FCM / rewards push sticky (TITLE / TITLE_REWARDS / anynews://push/...). */
    private boolean isPushOpenForFestival() {
        if (TITLE != null && !TITLE.isEmpty() && !"null".equalsIgnoreCase(TITLE)
                && !"0".equals(TITLE)) {
            return true;
        }
        if (TITLE_REWARDS != null && !TITLE_REWARDS.isEmpty()
                && !"null".equalsIgnoreCase(TITLE_REWARDS)
                && !"0".equals(TITLE_REWARDS)) {
            return true;
        }
        try {
            Intent i = getIntent();
            if (i != null && i.getData() != null) {
                String host = i.getData().getHost();
                if (host != null && "push".equalsIgnoreCase(host)) {
                    return true;
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    /** Share / App Link / job / DATAFROM2G deep links (not push). */
    private boolean isShareOrDeepLinkOpenForFestival() {
        if (isPushOpenForFestival()) {
            return false;
        }
        if (articleId1 != null && !articleId1.isEmpty() && !"null".equalsIgnoreCase(articleId1)
                && !"0".equals(articleId1)) {
            return true;
        }
        if (jobId1 != null && !jobId1.isEmpty() && !"null".equalsIgnoreCase(jobId1)
                && !"0".equals(jobId1)) {
            return true;
        }
        if (DATAFROM2G != null && !DATAFROM2G.isEmpty() && !"null".equalsIgnoreCase(DATAFROM2G)) {
            return true;
        }
        try {
            Intent i = getIntent();
            if (i != null && i.getData() != null) {
                String host = i.getData().getHost();
                String path = i.getData().getPath();
                if (host != null && "push".equalsIgnoreCase(host)) {
                    return false;
                }
                if (path != null && path.length() > 1) {
                    return true;
                }
            }
        } catch (Exception ignored) {
        }
        return false;
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
                                if (OpenPathCache.shouldSkipLanguageOnboarding(getApplicationContext())) {
                                    showLog("CHECK POINT HOMEPAGE 8 skip language");
                                    openHomeFromSplash();
                                } else {
                                Intent intent = new Intent(StartingActivity.this, LanguageActivity.class);
                                startActivity(intent);
                                finish();
                                }

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
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                intent.putExtra("EXTRA_SESSION_ID", "0");
                                boolean pushTitle8 = TITLE != null && !TITLE.isEmpty()
                                        && !"null".equalsIgnoreCase(TITLE) && !"0".equals(TITLE.trim());
                                if (pushTitle8) {
                                    intent.putExtra("notification", true);
                                    intent.putExtra("TITLE", TITLE);
                                    intent.putExtra("TITILE", TITLE);
                                    attachPushTrayId(intent, TITLE.trim());
                                } else {
                                    intent.putExtra("TITLE", "0");
                                }
                                intent.putExtra("TITLE_REWARDS",TITLE_REWARDS);
                                intent.putExtra("SHARE_REDIRECT_ID",articleId1);
                                intent.putExtra("SHARE_GALLERY_PAGE", shareGalleryPage);
                                intent.putExtra("JOB_REDIRECT_ID", jobId1);
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
                                        int langId = SharedPrefsData.getInt(
                                                getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME);
                                        TranslatorPackHelper.savePack(
                                                getApplicationContext(), langId, languageSelectionResponse);
                                        prefManager.setShared_String(Allsharedpreference.Translator, json);


                                        SharedPrefsData.putString(StartingActivity.this, Constants.State_Name, languageSelectionResponse.getNextScreen(), Constants.PREF_NAME);


                                        try {

                                            scrren=  SharedPrefsData.getString(getApplicationContext(), Constants.MOVE_NEXT_SCREEN, Constants.PREF_NAME);

                                            if (scrren.equals("language")) {
                                                if (OpenPathCache.shouldSkipLanguageOnboarding(getApplicationContext())) {
                                                    showLog("CHECK POINT HOMEPAGE 9 skip language");
                                                    openHomeFromSplash();
                                                } else {
                                                Intent intent = new Intent(StartingActivity.this, LanguageActivity.class);
                                                startActivity(intent);
                                                finish();
                                                }

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
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                                intent.putExtra("EXTRA_SESSION_ID", "0");
                                                boolean pushTitle9 = TITLE != null && !TITLE.isEmpty()
                                                        && !"null".equalsIgnoreCase(TITLE) && !"0".equals(TITLE.trim());
                                                if (pushTitle9) {
                                                    intent.putExtra("notification", true);
                                                    intent.putExtra("TITLE", TITLE);
                                                    intent.putExtra("TITILE", TITLE);
                                                    attachPushTrayId(intent, TITLE.trim());
                                                } else {
                                                    intent.putExtra("TITLE", "0");
                                                }
                                                intent.putExtra("TITLE_REWARDS",TITLE_REWARDS);
                                                intent.putExtra("SHARE_REDIRECT_ID",articleId1);
                                                intent.putExtra("SHARE_GALLERY_PAGE", shareGalleryPage);
                                                intent.putExtra("JOB_REDIRECT_ID", jobId1);
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
