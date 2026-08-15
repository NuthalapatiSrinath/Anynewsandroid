package com.anyNews.anynews.Utils;

import android.content.Context;

import com.anyNews.anynews.Pojo.NewUserProfileResponse;

/**
 * Resolve reporter user_id for wallet APIs. Prefs can lag behind profile cache
 * right after OTP / cached profile paint — never send 0 if profile knows the id.
 */
public final class WalletUserId {

    private WalletUserId() {
    }

    public static long resolve(Context context) {
        if (context == null) return 0L;
        Context app = context.getApplicationContext();
        long fromPrefs = 0L;
        try {
            fromPrefs = SharedPrefsData.getLong(app, Constants.USER_ID, Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
        if (fromPrefs > 0) return fromPrefs;

        try {
            NewUserProfileResponse resp = ReporterProfileCache.load(app);
            if (resp != null && resp.getData() != null && resp.getData().getUserId() != null
                    && resp.getData().getUserId() > 0) {
                long id = resp.getData().getUserId();
                SharedPrefsData.putLong(app, Constants.USER_ID, id, Constants.PREF_NAME);
                return id;
            }
        } catch (Exception ignored) {
        }
        return 0L;
    }
}
