package com.anyNews.anynews.Utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.google.android.gms.ads.MobileAds;

import java.util.ArrayList;
import java.util.List;

public final class AdMobInitializer {

    private static volatile boolean initialized = false;
    private static volatile boolean initializing = false;
    private static volatile String initializedAppId = null;
    private static final List<Runnable> pendingTasks = new ArrayList<>();

    private AdMobInitializer() {
    }

    public static void initialize(Context context, String appId) {
        if (context == null) {
            return;
        }

        String manifestAppId = getManifestAppId(context.getApplicationContext());
        String trimmedAppId = TextUtils.isEmpty(appId) ? manifestAppId : appId.trim();
        if (TextUtils.isEmpty(trimmedAppId)) {
            return;
        }

        if (initialized) {
            return;
        }

        if (initializing) {
            if (!TextUtils.isEmpty(trimmedAppId) && !trimmedAppId.equals(initializedAppId)) {
                android.util.Log.w("AdMobInitializer", "MobileAds init already in progress.");
            }
            return;
        }

        Context appContext = context.getApplicationContext();
        if (!TextUtils.isEmpty(manifestAppId) && !trimmedAppId.equals(manifestAppId)) {
            android.util.Log.w(
                    "AdMobInitializer",
                    "Server App ID differs from manifest; manifest App ID is used by this SDK version."
            );
        }

        initializing = true;
        MobileAds.initialize(appContext, initializationStatus -> {
            initialized = true;
            initializing = false;
            initializedAppId = manifestAppId != null ? manifestAppId : trimmedAppId;
            android.util.Log.i("AdMobInitializer", "MobileAds initialized");
            flushPendingTasks();
        });
    }

    public static void runWhenInitialized(Context context, Runnable task) {
        if (task == null) {
            return;
        }
        if (initialized) {
            task.run();
            return;
        }
        synchronized (pendingTasks) {
            pendingTasks.add(task);
        }
        initializeFromManifestIfNeeded(context);
    }

    public static boolean isInitialized() {
        return initialized;
    }

    private static void flushPendingTasks() {
        List<Runnable> tasks;
        synchronized (pendingTasks) {
            if (pendingTasks.isEmpty()) {
                return;
            }
            tasks = new ArrayList<>(pendingTasks);
            pendingTasks.clear();
        }
        for (Runnable task : tasks) {
            try {
                task.run();
            } catch (Exception e) {
                android.util.Log.e("AdMobInitializer", "Pending ad task failed", e);
            }
        }
    }

    public static String getManifestAppId(Context context) {
        if (context == null) {
            return null;
        }
        try {
            ApplicationInfo applicationInfo = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            if (applicationInfo.metaData != null) {
                return applicationInfo.metaData.getString("com.google.android.gms.ads.APPLICATION_ID");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void initializeFromManifestIfNeeded(Context context) {
        if (initialized || initializing) {
            return;
        }
        initialize(context, getManifestAppId(context));
    }
}
