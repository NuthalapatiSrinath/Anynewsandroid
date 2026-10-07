package com.anyNews.anynews.Utils;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.anyNews.anynews.Pojo.EncreptionRequest;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.google.gson.JsonObject;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Reports one completed Daily Updates share. Called only after
 * {@link android.app.Activity#RESULT_OK} (WhatsApp Send) or, when WhatsApp
 * is not installed, after the user picks a target from the sheet.
 * Opening the sheet does not call this.
 */
public final class DailyUpdatesShareReporter {

    private DailyUpdatesShareReporter() {
    }

    public static void report(@Nullable Context context, @Nullable String box) {
        if (context == null) {
            return;
        }
        String key = canonicalBox(box);
        if (key == null) {
            return;
        }
        final Context app = context.getApplicationContext();
        JsonObject body = new JsonObject();
        body.addProperty("box", key);
        int draft = SharedPrefsData.getInt(app, Constants.DraftID, Constants.PREF_NAME);
        if (draft > 0) {
            body.addProperty("draft_id", draft);
        }
        try {
            APIService api = RestApiClient.getClient().create(APIService.class);
            api.daily_updates_share("v3", body).enqueue(new Callback<EncreptionRequest>() {
                @Override
                public void onResponse(
                        @NonNull Call<EncreptionRequest> call,
                        @NonNull Response<EncreptionRequest> response) {
                }

                @Override
                public void onFailure(@NonNull Call<EncreptionRequest> call, @NonNull Throwable t) {
                }
            });
        } catch (Exception ignored) {
        }
    }

    /**
     * Boxes that have a share action. Jobs are not a {@code /du/} page, so
     * they are not in {@link DailyUpdatesShareLinks#canonicalKey}.
     */
    @Nullable
    static String canonicalBox(@Nullable String raw) {
        if (raw == null) {
            return null;
        }
        String key = raw.trim().toLowerCase(Locale.US);
        if ("jobs".equals(key) || "job".equals(key)) {
            return "jobs";
        }
        return DailyUpdatesShareLinks.canonicalKey(key);
    }
}
