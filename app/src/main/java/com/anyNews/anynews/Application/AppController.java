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
import com.anyNews.anynews.Activities.HomePage;
import com.anyNews.anynews.Activities.StartingActivity;
import com.anyNews.anynews.BuildConfig;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.ConnectivityReceiver;
import com.anyNews.anynews.Utils.AppHelper;
import com.anyNews.anynews.Utils.AppLanguagesFonts;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.ImageLoaderInit;
import com.anyNews.anynews.Utils.Logone;
import com.anyNews.anynews.Utils.ScreenCaptureHelper;
import com.anyNews.anynews.Utils.SessionAnalyticsTracker;
import com.anyNews.anynews.Utils.ViewsBatchTracker;
import com.google.firebase.FirebaseApp;
import com.rollbar.android.Rollbar;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class AppController extends MultiDexApplication {

    public static final String TAG = AppController.class.getSimpleName();
    private static AppController mInstance;
    private static final ExecutorService startupExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "app-startup");
        t.setPriority(Thread.NORM_PRIORITY - 1);
        return t;
    });

    LruBitmapCache mLruBitmapCache;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;

    public static synchronized AppController getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        AppHelper.showLog("onCreate for AppController");
        super.onCreate();
        // Manual night mode only (pref) — never follow system / battery-saver auto DayNight.
        try {
            com.anyNews.anynews.Utils.NightModeHelper.applyStoredPreference(this);
        } catch (Exception ignored) {
        }
        FirebaseApp.initializeApp(this);
        SessionAnalyticsTracker.get().init(this);
        ViewsBatchTracker.get().init(this);
        mInstance = this;
        // Must run on main thread before any activity loads images (fast splash → Home).
        ImageLoaderInit.ensureInitialized(this);
        createNotificationChannel(getApplicationContext());

        startupExecutor.execute(() -> {
            try {
                Rollbar.init(AppController.this);
            } catch (Exception e) {
                Log.w(TAG, "Rollbar init skipped", e);
            }
            try {
                AppLanguagesFonts.getInstance(getApplicationContext());
            } catch (Exception ignored) {
            }
        });

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
                // Splash keeps its own full-screen look; HomePage / Login re-apply after this.
                if (!(activity instanceof StartingActivity)) {
                    applyThemeSystemBars(activity);
                }

                if (activity instanceof HomePage && !ScreenCaptureHelper.isCaptureAllowed(activity)) {
                    activity.getWindow().setFlags(
                            WindowManager.LayoutParams.FLAG_SECURE,
                            WindowManager.LayoutParams.FLAG_SECURE);
                } else {
                    activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
                }
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {
            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {
            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {
            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
            }
        });
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        // Drop image caches under pressure so low-end devices recover instead of OOM-kill.
        if (level >= TRIM_MEMORY_RUNNING_MODERATE) {
            try {
                if (mLruBitmapCache != null) {
                    mLruBitmapCache.evictAll();
                }
            } catch (Throwable ignored) {
            }
            try {
                com.nostra13.universalimageloader.core.ImageLoader uil =
                        com.nostra13.universalimageloader.core.ImageLoader.getInstance();
                if (uil.isInited()) {
                    uil.clearMemoryCache();
                }
            } catch (Throwable ignored) {
            }
            try {
                com.bumptech.glide.Glide.get(getApplicationContext()).clearMemory();
            } catch (Throwable ignored) {
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

    public void createNotificationChannel(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    Constants.Channel_ID, "Any News", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            if (BuildConfig.DEBUG) {
                SSLSocketFactory sslSocketFactory = createSslSocketFactory();
                if (sslSocketFactory != null) {
                    mRequestQueue = Volley.newRequestQueue(getApplicationContext(),
                            new HurlStack(null, sslSocketFactory));
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
        if (mLruBitmapCache == null) {
            mLruBitmapCache = new LruBitmapCache();
        }
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