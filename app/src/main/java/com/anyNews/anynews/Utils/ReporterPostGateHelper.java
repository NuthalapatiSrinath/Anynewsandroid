package com.anyNews.anynews.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import com.anyNews.anynews.Activities.newProfileDetails;
import com.anyNews.anynews.R;

/**
 * Admin gate: when {@link Constants#REQUIRE_FULL_PROFILE_TO_POST} is ON,
 * reporters must reach 100% profile (name/email/mobile/photo) before compose.
 * Completeness matches {@link ProfileAchievementHelper}.
 */
public final class ReporterPostGateHelper {

    private ReporterPostGateHelper() {
    }

    public static boolean isRequireFullProfileToPost(@Nullable Context context) {
        if (context == null) return false;
        try {
            SharedPreferences sp =
                    context.getApplicationContext()
                            .getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
            return sp.getBoolean(Constants.REQUIRE_FULL_PROFILE_TO_POST, false);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isShowWriterPhotoOnCards(@Nullable Context context) {
        if (context == null) return true;
        try {
            SharedPreferences sp =
                    context.getApplicationContext()
                            .getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
            return sp.getBoolean(Constants.SHOW_WRITER_PHOTO_ON_CARDS, true);
        } catch (Exception e) {
            return true;
        }
    }

    public static void saveFromSplash(
            @Nullable Context context,
            @Nullable Boolean requireFullProfile,
            @Nullable Boolean showWriterPhoto) {
        if (context == null) return;
        Context app = context.getApplicationContext();
        try {
            if (requireFullProfile != null) {
                SharedPrefsData.putBool(
                        app,
                        Constants.REQUIRE_FULL_PROFILE_TO_POST,
                        requireFullProfile,
                        Constants.PREF_NAME);
            }
            if (showWriterPhoto != null) {
                SharedPrefsData.putBool(
                        app,
                        Constants.SHOW_WRITER_PHOTO_ON_CARDS,
                        showWriterPhoto,
                        Constants.PREF_NAME);
            }
        } catch (Exception ignored) {
        }
    }

    /** True when gate is ON and local/prefs completeness is below 100. */
    public static boolean isBlockedByIncompleteProfile(@Nullable Context context) {
        if (!isRequireFullProfileToPost(context)) return false;
        int pct = ProfileAchievementHelper.computePctFromPrefs(context);
        return pct < 100;
    }

    /**
     * If gate blocks, show dialog (Complete profile) and return true.
     * Caller should not open compose when this returns true.
     */
    public static boolean blockComposeIfIncomplete(@Nullable Activity activity) {
        if (activity == null || activity.isFinishing()) return false;
        if (!isBlockedByIncompleteProfile(activity)) return false;
        try {
            new AlertDialog.Builder(activity)
                    .setTitle(R.string.reporter_profile_gate_title)
                    .setMessage(R.string.reporter_profile_gate_message)
                    .setCancelable(true)
                    .setPositiveButton(R.string.reporter_profile_gate_complete, (d, w) -> {
                        try {
                            Intent intent = new Intent(activity, newProfileDetails.class);
                            intent.putExtra("open_photo_picker", true);
                            activity.startActivity(intent);
                        } catch (Exception ignored) {
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        } catch (Exception e) {
            try {
                android.widget.Toast.makeText(
                                activity,
                                R.string.reporter_profile_gate_message,
                                android.widget.Toast.LENGTH_LONG)
                        .show();
            } catch (Exception ignored) {
            }
        }
        return true;
    }
}
