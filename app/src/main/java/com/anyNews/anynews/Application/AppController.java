package com.anyNews.anynews.Application;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.multidex.MultiDexApplication;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.anyNews.anynews.Activities.BookmarkReaderActivity;
import com.anyNews.anynews.Activities.BookmarksActivity;
import com.anyNews.anynews.Activities.HomePage;
import com.anyNews.anynews.Activities.StartingActivity;
import com.anyNews.anynews.BuildConfig;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.ConnectivityReceiver;
import com.anyNews.anynews.Utils.AppHelper;
import com.anyNews.anynews.Utils.AppLanguagesFonts;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.ImageLoaderInit;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.anyNews.anynews.Utils.Logone;
import com.anyNews.anynews.Utils.PostDwellTracker;
import com.anyNews.anynews.Utils.ScreenCaptureHelper;
import com.anyNews.anynews.Utils.MediaAccessReporter;
import com.anyNews.anynews.Utils.AdImpressionReporter;
import com.anyNews.anynews.Utils.SessionAnalyticsTracker;
import com.anyNews.anynews.Utils.ViewsBatchTracker;
import com.facebook.ads.AdSettings;
import com.facebook.ads.AudienceNetworkAds;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.AdapterStatus;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.firebase.FirebaseApp;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class AppController extends MultiDexApplication {

    public static final String TAG = AppController.class.getSimpleName();
    private static AppController mInstance;
    LruBitmapCache mLruBitmapCache;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private AppLanguagesFonts appLanguagesFonts;

    public static synchronized AppController getInstance()
    {
        return mInstance;
    }

    @Override
    public void onCreate() {
        AppHelper.showLog("onCreate for AppController");
        super.onCreate();
        // Manual night mode only (pref) — never follow system / battery-saver auto DayNight.
        try {
            com.anyNews.anynews.Utils.NightModeHelper.applyStoredPreference(this);
        } catch (Exception ignored) { }
        FirebaseApp.initializeApp(this);
        mInstance = this;
        ImageLoaderInit.ensureInitialized(this);
        createNotificationChannel(getApplicationContext());
        appLanguagesFonts=AppLanguagesFonts.getInstance(getApplicationContext());

       // MobileAds.initialize(this, new adsLoadingInit());
        //AudienceNetworkAds.initialize(this);
        MobileAds.initialize(this, initializationStatus -> {
        });

        // Session time: ProcessLifecycle + screen off/on + activity callbacks.
        SessionAnalyticsTracker.get().init(this);
        // Batched views/dwell — flip settle enqueues; flush ~60s or on background.
        ViewsBatchTracker.get().init(this);
        // Soft-accounting for public OCI / object URL opens (admin App→OCI dashboard).
        MediaAccessReporter.get().init(this);
        // Real native-ad display counts (Google/FB painted) — one batched flush.
        AdImpressionReporter.get().init(this);

        // One-shot per process: previous session's flip views → permanent Read before News loads.
        try {
            com.anyNews.anynews.Utils.ReadArticleStore.commitSessionViewedToReadOncePerProcess(this);
        } catch (Exception ignored) {
        }

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
                // Splash keeps its own full-screen look; HomePage / Login re-apply after this.
                // Bookmarks / reader paint brand-orange status bars and keep content below them.
                if (!(activity instanceof StartingActivity)
                        && !(activity instanceof BookmarksActivity)
                        && !(activity instanceof BookmarkReaderActivity)) {
                    applyThemeSystemBars(activity);
                }

                if(activity instanceof HomePage && !ScreenCaptureHelper.isCaptureAllowed(activity)){
                    activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
                }else{
                    activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
                }
            }
            @Override
            public void onActivityStarted(@NonNull Activity activity) {
                SessionAnalyticsTracker.get().onActivityStarted(activity);
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                SessionAnalyticsTracker.get().onActivityResumed(activity);
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {
            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {
                SessionAnalyticsTracker.get().onActivityStopped(activity);
            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {
            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
                SessionAnalyticsTracker.get().onActivityFinishingDestroyed(activity);
            }
        });



    }

    @Override
    public void onTerminate() {
        try {
            SessionAnalyticsTracker.get().onAppTerminate(this);
        } catch (Exception ignored) {
        }
        try {
            ViewsBatchTracker.get().flush(this, true);
        } catch (Exception ignored) {
        }
        try {
            AdImpressionReporter.get().flush(this, true);
        } catch (Exception ignored) {
        }
        super.onTerminate();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (level >= TRIM_MEMORY_UI_HIDDEN) {
            try {
                PostDwellTracker.get().flush(this, true);
            } catch (Exception ignored) {
            }
            try {
                ViewsBatchTracker.get().flush(this, true);
            } catch (Exception ignored) {
            }
            try {
                AdImpressionReporter.get().flush(this, true);
            } catch (Exception ignored) {
            }
        }
    }

    class adsLoadingInit implements OnInitializationCompleteListener {
        adsLoadingInit() {
        }

        public void onInitializationComplete(InitializationStatus initializationStatus) {
            Map<String, AdapterStatus> statusMap = initializationStatus.getAdapterStatusMap();
            for (String adapterClass : statusMap.keySet()) {
                AdapterStatus status = statusMap.get(adapterClass);
                AppHelper.showLog("MyApp" +String.format(
                        "Adapter name: %s, Description: %s, Latency: %d",
                        adapterClass, status.getDescription(), status.getLatency()));
            }
        }
    }

    private static SSLSocketFactory createSslSocketFactory() {
        if (!BuildConfig.DEBUG) {
            try {
                return SSLContext.getDefault().getSocketFactory();
            } catch (NoSuchAlgorithmException e) {
                Logone.e("sanjay", e.toString());
                return null;
            }
        }

        TrustManager[] byPassTrustManagers = new TrustManager[]{new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }

            public void checkClientTrusted(X509Certificate[] chain, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] chain, String authType) {
            }
        }};

        SSLContext sslContext = null;
        SSLSocketFactory sslSocketFactory = null;
        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, byPassTrustManagers, new SecureRandom());
            sslSocketFactory = sslContext.getSocketFactory();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            Logone.e("sanjay", e.toString());
        } catch (KeyManagementException e) {
            Logone.e("sanjay", e.toString());
        }

        return sslSocketFactory;
    }


    public  void createNotificationChannel(@NonNull Context context) {
        if (Build.VERSION.SDK_INT  >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(Constants.Channel_ID, "Any News", NotificationManager.IMPORTANCE_HIGH);
//            channel.setLightColor(R.color.colorAccent);
//            channel.enableVibration(true);
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }
    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            if (BuildConfig.DEBUG) {
                SSLSocketFactory sslSocketFactory = createSslSocketFactory();
                if (sslSocketFactory != null) {
                    mRequestQueue = Volley.newRequestQueue(getApplicationContext(), new HurlStack(null, sslSocketFactory));
                } else {
                    mRequestQueue = Volley.newRequestQueue(getApplicationContext());
                }
            } else {
                mRequestQueue = Volley.newRequestQueue(getApplicationContext());
            }
        }

        return mRequestQueue;
    }

    public ImageLoader getImageLoader() {
        getRequestQueue();
        if (mImageLoader == null) {
            getLruBitmapCache();
            mImageLoader = new ImageLoader(this.mRequestQueue, mLruBitmapCache);
        }

        return this.mImageLoader;
    }

    public LruBitmapCache getLruBitmapCache() {
        if (mLruBitmapCache == null)
            mLruBitmapCache = new LruBitmapCache();
        return this.mLruBitmapCache;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag, HurlStack mHurlStack) {
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    public void setConnectivityListener(ConnectivityReceiver.ConnectivityReceiverListener listener) {
        ConnectivityReceiver.connectivityReceiverListener = listener;
    }

    /**
     * Theme-aware status + nav bars for all activities.
     * Light mode: light bar bg + dark icons. Dark mode: dark bar bg + light icons.
     * HomePage / LoginActivityDetails may re-apply after this for screen-specific bars.
     */
    public static void applyThemeSystemBars(@NonNull Activity activity) {
        Window window = activity.getWindow();
        WindowCompat.setDecorFitsSystemWindows(window, true);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        int bg = ContextCompat.getColor(activity, R.color.background_color);
        window.setStatusBarColor(bg);
        window.setNavigationBarColor(bg);

        boolean lightMode = (activity.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES;

        WindowInsetsControllerCompat insetsController =
                WindowCompat.getInsetsController(window, window.getDecorView());
        if (insetsController != null) {
            insetsController.setAppearanceLightStatusBars(lightMode);
            insetsController.setAppearanceLightNavigationBars(lightMode);
        }
    }
}