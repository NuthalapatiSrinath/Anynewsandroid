package com.anyNews.anynews.Utils;

import android.content.Context;
import android.text.TextUtils;

import com.anyNews.anynews.Pojo.EncreptionRequest;
import com.anyNews.anynews.Pojo.EncritonRequestData;
import com.anyNews.anynews.Pojo.NewUserProfileResponse;
import com.anyNews.anynews.Pojo.UserProfileRequest;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Background soft-refresh of get_user_profile after login / home open so profile
 * opens with a warm disk+memory cache (no spinner).
 */
public final class ReporterProfilePrefetcher {

    private static volatile boolean inFlight;
    private static final Gson GSON = new Gson();

    private ReporterProfilePrefetcher() {
    }

    public static void prefetchIfPossible(Context context) {
        if (context == null || inFlight) return;
        final Context app = context.getApplicationContext();
        if (ReporterProfileCache.isFresh(app)) return;
        boolean loggedIn = SharedPrefsData.getBool(app, Constants.is_login, Constants.PREF_NAME);
        boolean reporter = SharedPrefsData.getBool(app, Constants.is_reporter, Constants.PREF_NAME);
        if (!loggedIn && !reporter) {
            // Still prefetch for any linked account so normal→reporter paint is ready.
            String email = "";
            try {
                email = new Allsharedpreference(app).getEmailId();
            } catch (Exception ignored) {
            }
            if (TextUtils.isEmpty(email)) return;
        }
        inFlight = true;
        try {
            UserProfileRequest requestModel = new UserProfileRequest();
            try {
                Allsharedpreference pref = new Allsharedpreference(app);
                if (pref.getEmailId() != null) {
                    requestModel.setEmail(pref.getEmailId());
                }
            } catch (Exception ignored) {
            }
            requestModel.setDraftId(SharedPrefsData.getInt(app, Constants.DraftID, Constants.PREF_NAME));
            String jsonString = GSON.toJson(requestModel);
            String encrpt = MyUtils.encrypt(jsonString);
            EncritonRequestData body = new EncritonRequestData();
            body.setData(encrpt);
            JsonObject otp = GSON.toJsonTree(body).getAsJsonObject();

            APIService api = RestApiClient.getClient().create(APIService.class);
            api.get_user_profile("v3", otp).enqueue(new Callback<EncreptionRequest>() {
                @Override
                public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {
                    inFlight = false;
                    try {
                        if (response.body() == null || response.body().getData() == null) return;
                        String plain = MyUtils.decrypt(response.body().getData());
                        if (!TextUtils.isEmpty(plain)) {
                            ReporterProfileCache.save(app, plain);
                        }
                    } catch (Exception e) {
                        Logone.w(e, "ReporterProfilePrefetcher parse failed");
                    }
                }

                @Override
                public void onFailure(Call<EncreptionRequest> call, Throwable t) {
                    inFlight = false;
                }
            });
        } catch (Exception e) {
            inFlight = false;
            Logone.w(e, "ReporterProfilePrefetcher enqueue failed");
        }
    }

    /** Persist reporter flag + achieved % so Profile first frame never waits. */
    public static void applyIdentityPrefsFromProfileJson(Context app, String plain) {
        if (app == null || TextUtils.isEmpty(plain)) return;
        try {
            NewUserProfileResponse resp = GSON.fromJson(plain, NewUserProfileResponse.class);
            if (resp == null || resp.getData() == null) return;
            NewUserProfileResponse.Data d = resp.getData();
            if (d.getIsReporter() != null) {
                SharedPrefsData.putBoolImmediate(app, Constants.is_reporter,
                        Boolean.TRUE.equals(d.getIsReporter()), Constants.PREF_NAME);
            }
            if (d.getUserId() != null && d.getUserId() > 0) {
                SharedPrefsData.putLong(app, Constants.USER_ID, d.getUserId(), Constants.PREF_NAME);
            }
            // Merge login prefs first so a sparse API payload cannot store 0%.
            int pct = ProfileAchievementHelper.resolveAchievedPctWithPrefs(app, d);
            SharedPrefsData.putString(app, Constants.PROFILE_ACHIEVED_PCT,
                    String.valueOf(pct), Constants.PREF_NAME);
            if (d.getUserName() != null && !d.getUserName().trim().isEmpty()
                    && !"guest".equalsIgnoreCase(d.getUserName().trim())) {
                SharedPrefsData.putString(app, Constants.USER_NAME,
                        d.getUserName().trim(), Constants.PREF_NAME);
            }
            if (d.getEmail() != null && d.getEmail().contains("@")) {
                SharedPrefsData.putString(app, Constants.Email, d.getEmail().trim(), Constants.PREF_NAME);
                try {
                    new Allsharedpreference(app).setEmailID(d.getEmail().trim());
                } catch (Exception ignored) {
                }
            }
            if (d.getProfilePic() != null && !d.getProfilePic().trim().isEmpty()) {
                SharedPrefsData.putString(app, Constants.PROFILE_PIC_URL,
                        d.getProfilePic().trim(), Constants.PREF_NAME);
            }
            if (d.getMobileNumber() != null && !d.getMobileNumber().trim().isEmpty()
                    && !"null".equalsIgnoreCase(d.getMobileNumber().trim())) {
                SharedPrefsData.putString(app, Constants.Selected_Phone_Number,
                        d.getMobileNumber().trim(), Constants.PREF_NAME);
            }
        } catch (Exception ignored) {
        }
    }
}
