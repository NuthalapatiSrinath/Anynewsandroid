package com.anyNews.anynews.rewards;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.anyNews.anynews.Utils.WalletUserId;
import com.google.gson.JsonObject;

/**
 * Shared auth payload for Rewards APIs — uses the same OTP identity as Profile/BAR
 * ({@code user_id} + {@code draft_id}), not the legacy USERID/SessionID prefs.
 */
public final class RewardsAuth {

    private RewardsAuth() {}

    @NonNull
    public static JsonObject authBody(@Nullable Context context) {
        JsonObject body = new JsonObject();
        if (context == null) return body;
        Context app = context.getApplicationContext();

        long userId = 0L;
        try {
            userId = WalletUserId.resolve(app);
        } catch (Exception ignored) {
        }
        if (userId <= 0) {
            try {
                userId = SharedPrefsData.getLong(app, Constants.USER_ID, Constants.PREF_NAME);
            } catch (Exception ignored) {
            }
        }

        int draftId = 0;
        try {
            draftId = SharedPrefsData.getInt(app, Constants.DraftID, Constants.PREF_NAME);
        } catch (Exception ignored) {
        }

        int languageId = 0;
        try {
            languageId = SharedPrefsData.getInt(app, Constants.LanguageID, Constants.PREF_NAME);
        } catch (Exception ignored) {
        }

        if (userId > 0) {
            body.addProperty("user_id", userId);
            // Legacy field some server paths still read.
            body.addProperty("USERID", String.valueOf(userId));
        }
        if (draftId > 0) {
            body.addProperty("draft_id", draftId);
        }
        if (languageId >= 0) {
            body.addProperty("language_id", languageId);
        }

        // Soft session token from splash (not Users.SessionID).
        try {
            String session =
                    SharedPrefsData.getString(app, Constants.USER_SESSION_ID, Constants.PREF_NAME);
            if (TextUtils.isEmpty(session)) {
                session = SharedPrefsData.getString(app, Constants.SessionID, Constants.PREF_NAME);
            }
            if (!TextUtils.isEmpty(session)) {
                body.addProperty("SessionID", session);
                body.addProperty("user_session_id", session);
            }
        } catch (Exception ignored) {
        }

        return body;
    }

    public static boolean isLoggedIn(@Nullable Context context) {
        if (context == null) return false;
        try {
            if (!SharedPrefsData.getBool(
                    context.getApplicationContext(), Constants.is_login, Constants.PREF_NAME)) {
                return false;
            }
        } catch (Exception ignored) {
            return false;
        }
        return WalletUserId.resolve(context) > 0;
    }
}
