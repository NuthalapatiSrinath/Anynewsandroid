package com.anyNews.anynews.Utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.google.android.gms.ads.MobileAds;

public final class AdMobInitializer {

    private static volatile boolean initialized = false;
    private static volatile String initializedAppId = null;

    private AdMobInitializer() {
    }

    public static void initialize(Context context, String appId) {
        if (context == null || TextUtils.isEmpty(appId)) {
            return;
        }

        String trimmedAppId = appId.trim();
        if (initialized && trimmedAppId.equals(initializedAppId)) {
            return;
        }

        Context appContext = context.getApplicationContext();
        MobileAds.initialize(appContext, initializationStatus -> {
            initialized = true;
            initializedAppId = trimmedAppId;
        });
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
        if (initialized) {
            return;
        }
        initialize(context, getManifestAppId(context));
    }
}
