package com.anyNews.anynews.Utils;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.anyNews.anynews.Activities.StartingActivity;

/**
 * Clears OTP/auth prefs without wiping language pack, LanguageID, translator,
 * or geography. Used by Profile logout and remote FCM {@code force_logout}.
 */
public final class ForceLogoutHelper {

    private ForceLogoutHelper() {}

    /**
     * @param keepDraftId when true (remote force logout), keep {@link Constants#DraftID}
     *                    so splash reuses the server guest draft with language intact.
     *                    Voluntary Profile logout passes false (new anonymous draft).
     * @param restartToSplash when true, clears the task and opens StartingActivity.
     */
    public static void clearAuthSession(
            @NonNull Context context,
            boolean keepDraftId,
            boolean restartToSplash) {
        Context app = context.getApplicationContext();

        try {
            SessionAnalyticsTracker.get().onAppTerminate(app);
        } catch (Exception ignored) {
        }

        SharedPrefsData.putBoolImmediate(app, Constants.is_login, false, Constants.PREF_NAME);
        SharedPrefsData.putBoolImmediate(app, Constants.FORCE_LOGGED_OUT, true, Constants.PREF_NAME);
        SharedPrefsData.putBoolImmediate(app, Constants.is_reporter, false, Constants.PREF_NAME);
        SharedPrefsData.putInt(app, Constants.REPORTER_DESK_UI_STATE,
                Constants.REPORTER_DESK_HIDDEN, Constants.PREF_NAME);
        SharedPrefsData.putString(app, Constants.PROFILE_ACHIEVED_PCT, "", Constants.PREF_NAME);
        SharedPrefsData.putBool(app, Constants.IS_USER_ALREADY_LOGIN, false, Constants.PREF_NAME);
        SharedPrefsData.putBool(app, Constants.IS_ALREADY_LOGIN, false, Constants.PREF_NAME);
        SharedPrefsData.putString(app, Constants.Selected_Phone_Number, "", Constants.PREF_NAME);
        SharedPrefsData.putString(app, Constants.PhoneNumber, "", Constants.PREF_NAME);
        SharedPrefsData.putString(app, Constants.USER_NAME, "", Constants.PREF_NAME);
        SharedPrefsData.putString(app, Constants.PROFILE_PIC_URL, "", Constants.PREF_NAME);
        SharedPrefsData.putString(app, Constants.Email, "", Constants.PREF_NAME);
        SharedPrefsData.putString(app, Constants.AVAILABLEPOINTS, "", Constants.PREF_NAME);
        SharedPrefsData.putString(app, Constants.REDEEMPOINTS, "", Constants.PREF_NAME);
        SharedPrefsData.putString(app, Constants.USER_SESSION_ID, "", Constants.PREF_NAME);
        SharedPrefsData.putString(app, Constants.SessionID, "", Constants.PREF_NAME);
        SharedPrefsData.putString(app, Constants.REFERAL_LINK, "", Constants.PREF_NAME);
        SharedPrefsData.putString(app, Constants.REFERAL_LINK2, "", Constants.PREF_NAME);
        SharedPrefsData.putString(app, Constants.CUSST_ID, "", Constants.PREF_NAME);
        SharedPrefsData.putLong(app, Constants.USER_ID, 0L, Constants.PREF_NAME);
        SharedPrefsData.putBool(app, Constants.PAN_CARD_STATUS, false, Constants.PREF_NAME);
        SharedPrefsData.putInt(app, Constants.UserID, 0, Constants.PREF_NAME);
        SharedPrefsData.putInt(app, Constants.reporter_language_id, 0, Constants.PREF_NAME);

        if (!keepDraftId) {
            SharedPrefsData.putInt(app, Constants.DraftID, 0, Constants.PREF_NAME);
        }

        try {
            ReporterProfileCache.clear(app);
        } catch (Exception ignored) {
        }
        try {
            FollowListCache.clear(app);
        } catch (Exception ignored) {
        }

        try {
            Allsharedpreference prefShare = new Allsharedpreference(app);
            prefShare.setEmailID("");
            prefShare.setShared_String(Allsharedpreference.EMAIL_ID, "");
            prefShare.setShared_Booleen(Allsharedpreference.IS_USER_ALREADY_LOGIN, false);
            prefShare.setShared_Booleen(Allsharedpreference.IS_ALREADY_LOGIN, false);
        } catch (Exception ignored) {
        }

        // LanguageID / StateID / DistID / language pack / translator prefs intentionally untouched.

        if (restartToSplash) {
            Intent intent = new Intent(app, StartingActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("force_logout", true);
            app.startActivity(intent);
        }
    }
}
