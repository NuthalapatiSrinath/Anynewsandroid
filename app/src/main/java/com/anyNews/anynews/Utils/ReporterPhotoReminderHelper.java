package com.anyNews.anynews.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.anyNews.anynews.Activities.newProfileDetails;
import com.anyNews.anynews.R;

/**
 * Soft (dismissible) reminder for approved reporters who still use the default /
 * missing profile photo. Not a hard block — cooldown prevents nagging.
 */
public final class ReporterPhotoReminderHelper {

    private static boolean sShowing;

    private ReporterPhotoReminderHelper() {
    }

    public static boolean shouldRemind(Context context) {
        if (context == null) return false;
        try {
            boolean loggedIn = SharedPrefsData.getBool(
                    context, Constants.is_login, Constants.PREF_NAME);
            if (!loggedIn) return false;
            boolean isReporter = SharedPrefsData.getBool(
                    context, Constants.is_reporter, Constants.PREF_NAME);
            if (!isReporter) return false;

            String pic = SharedPrefsData.getString(
                    context, Constants.PROFILE_PIC_URL, Constants.PREF_NAME);
            if (!AvatarImageHelper.isUnusablePhoto(pic)) return false;

            String dismissedRaw = SharedPrefsData.getString(
                    context, Constants.PROFILE_PHOTO_REMINDER_DISMISSED_AT, Constants.PREF_NAME);
            if (!TextUtils.isEmpty(dismissedRaw)) {
                try {
                    long at = Long.parseLong(dismissedRaw.trim());
                    if (at > 0
                            && (System.currentTimeMillis() - at)
                            < Constants.PROFILE_PHOTO_REMINDER_COOLDOWN_MS) {
                        return false;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void markDismissed(Context context) {
        if (context == null) return;
        try {
            SharedPrefsData.putString(
                    context,
                    Constants.PROFILE_PHOTO_REMINDER_DISMISSED_AT,
                    String.valueOf(System.currentTimeMillis()),
                    Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
    }

    /**
     * Show a soft dialog when appropriate. Safe to call from onResume — no-ops when
     * already showing, activity finishing, or cooldown active.
     */
    public static void maybeShow(Activity activity) {
        if (activity == null || activity.isFinishing()) return;
        if (sShowing) return;
        if (!shouldRemind(activity)) return;

        sShowing = true;
        try {
            new AlertDialog.Builder(activity)
                    .setTitle(R.string.reporter_photo_reminder_title)
                    .setMessage(R.string.reporter_photo_reminder_message)
                    .setCancelable(true)
                    .setPositiveButton(R.string.reporter_photo_reminder_add, (d, w) -> {
                        markDismissed(activity);
                        openProfile(activity);
                    })
                    .setNegativeButton(R.string.reporter_photo_reminder_later, (d, w) ->
                            markDismissed(activity))
                    .setOnCancelListener(d -> markDismissed(activity))
                    .setOnDismissListener(d -> sShowing = false)
                    .show();
        } catch (Exception e) {
            sShowing = false;
        }
    }

    private static void openProfile(Activity activity) {
        try {
            Intent intent = new Intent(activity, newProfileDetails.class);
            intent.putExtra("open_photo_picker", true);
            activity.startActivity(intent);
        } catch (Exception ignored) {
        }
    }

    /** Clear dismiss cooldown after a successful photo upload so future gaps can remind again. */
    public static void clearDismissAfterUpload(@Nullable Context context) {
        if (context == null) return;
        try {
            SharedPrefsData.putString(
                    context,
                    Constants.PROFILE_PHOTO_REMINDER_DISMISSED_AT,
                    "",
                    Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
    }
}
