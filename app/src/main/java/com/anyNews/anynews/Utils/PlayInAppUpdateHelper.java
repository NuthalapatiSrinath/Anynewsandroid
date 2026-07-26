package com.anyNews.anynews.Utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.anyNews.anynews.BuildConfig;
import com.google.android.gms.tasks.Task;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.appupdate.AppUpdateOptions;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;

/**
 * Google Play In-App Updates (flexible): downloads Play's incremental/delta update
 * in the background, then completes install. Falls back to the Play Store listing
 * when in-app updates are unavailable (sideload / tip APK / no Play).
 */
public final class PlayInAppUpdateHelper {

    private static final String TAG = "PlayInAppUpdate";

    public interface Listener {
        /** Flexible package finished downloading — UI should show Install. */
        void onFlexibleUpdateDownloaded();

        /** Download progress (0–100). Optional for UI. */
        void onFlexibleUpdateProgress(int percent);

        /** In-app flow could not start; caller may open Play Store. */
        void onFlexibleUpdateUnavailable(@Nullable Exception error);
    }

    private final Activity activity;
    private final AppUpdateManager appUpdateManager;
    @Nullable
    private Listener listener;
    @Nullable
    private ActivityResultLauncher<IntentSenderRequest> updateLauncher;
    @Nullable
    private InstallStateUpdatedListener installListener;
    private boolean listenerRegistered = false;

    public PlayInAppUpdateHelper(@NonNull Activity activity) {
        this.activity = activity;
        this.appUpdateManager = AppUpdateManagerFactory.create(activity.getApplicationContext());
    }

    public void setListener(@Nullable Listener listener) {
        this.listener = listener;
    }

    public void setUpdateLauncher(@Nullable ActivityResultLauncher<IntentSenderRequest> launcher) {
        this.updateLauncher = launcher;
    }

    /** Start flexible in-app update, or notify unavailable for Play Store fallback. */
    public void startFlexibleUpdate() {
        ensureInstallListener();
        Task<AppUpdateInfo> task = appUpdateManager.getAppUpdateInfo();
        task.addOnSuccessListener(info -> {
            try {
                if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                        && info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                    if (updateLauncher == null) {
                        notifyUnavailable(new IllegalStateException("update launcher not bound"));
                        return;
                    }
                    appUpdateManager.startUpdateFlowForResult(
                            info,
                            updateLauncher,
                            AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
                    );
                    return;
                }
                if (info.installStatus() == InstallStatus.DOWNLOADED) {
                    if (listener != null) {
                        listener.onFlexibleUpdateDownloaded();
                    }
                    return;
                }
                notifyUnavailable(null);
            } catch (Exception e) {
                Log.w(TAG, "startFlexibleUpdate failed", e);
                notifyUnavailable(e);
            }
        });
        task.addOnFailureListener(e -> {
            Log.w(TAG, "getAppUpdateInfo failed", e);
            notifyUnavailable(e);
        });
    }

    /** If a flexible update already downloaded, notify UI (e.g. onResume). */
    public void resumeIfDownloaded() {
        appUpdateManager.getAppUpdateInfo().addOnSuccessListener(info -> {
            if (info != null && info.installStatus() == InstallStatus.DOWNLOADED) {
                if (listener != null) {
                    listener.onFlexibleUpdateDownloaded();
                }
            }
        });
    }

    /** Apply downloaded flexible update (restarts the app). */
    public void completeFlexibleUpdate() {
        try {
            appUpdateManager.completeUpdate();
        } catch (Exception e) {
            Log.w(TAG, "completeUpdate failed", e);
            openPlayStoreListing(activity);
        }
    }

    public void unregister() {
        if (listenerRegistered && installListener != null) {
            try {
                appUpdateManager.unregisterListener(installListener);
            } catch (Exception ignored) {
            }
            listenerRegistered = false;
        }
        installListener = null;
        listener = null;
    }

    public static void openPlayStoreListing(@NonNull Activity activity) {
        final String appPackageName = activity.getPackageName();
        try {
            if (activity.getApplicationContext() != null
                    && activity.getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)) {
                activity.startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=" + appPackageName)));
            }
        } catch (ActivityNotFoundException anfe) {
            if (activity.getApplicationContext() != null
                    && activity.getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)) {
                activity.startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
            }
        }
    }

    private void ensureInstallListener() {
        if (installListener != null) {
            return;
        }
        installListener = state -> {
            if (state == null) {
                return;
            }
            if (state.installStatus() == InstallStatus.DOWNLOADING) {
                long total = state.totalBytesToDownload();
                long done = state.bytesDownloaded();
                int percent = total > 0 ? (int) ((done * 100L) / total) : 0;
                if (listener != null) {
                    listener.onFlexibleUpdateProgress(percent);
                }
            } else if (state.installStatus() == InstallStatus.DOWNLOADED) {
                if (listener != null) {
                    listener.onFlexibleUpdateDownloaded();
                }
            }
        };
        try {
            appUpdateManager.registerListener(installListener);
            listenerRegistered = true;
        } catch (Exception e) {
            Log.w(TAG, "registerListener failed", e);
        }
    }

    private void notifyUnavailable(@Nullable Exception error) {
        if (listener != null) {
            listener.onFlexibleUpdateUnavailable(error);
        }
    }
}
