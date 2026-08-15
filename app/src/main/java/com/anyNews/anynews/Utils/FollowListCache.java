package com.anyNews.anynews.Utils;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.anyNews.anynews.models.FollowingMenderModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Instant paint cache for following / followers lists (memory + prefs).
 */
public final class FollowListCache {

    public static final String TYPE_FOLLOWING = "following";
    public static final String TYPE_FOLLOWERS = "followers";

    private static final Gson GSON = new Gson();
    private static final Type LIST_TYPE =
            new TypeToken<List<FollowingMenderModel.Datum>>() {}.getType();

    private static volatile String memoryKey;
    private static volatile String memoryJson;

    private FollowListCache() {
    }

    @NonNull
    private static String key(@Nullable Context context, @NonNull String type) {
        int draft = 0;
        if (context != null) {
            draft = SharedPrefsData.getInt(
                    context.getApplicationContext(), Constants.DraftID, Constants.PREF_NAME);
        }
        return "follow_list_" + type + "_" + draft;
    }

    public static void save(
            @Nullable Context context,
            @NonNull String type,
            @Nullable List<FollowingMenderModel.Datum> data) {
        if (context == null) return;
        Context app = context.getApplicationContext();
        List<FollowingMenderModel.Datum> copy =
                data != null ? new ArrayList<>(data) : new ArrayList<FollowingMenderModel.Datum>();
        String json = GSON.toJson(copy);
        String k = key(app, type);
        memoryKey = k;
        memoryJson = json;
        try {
            SharedPrefsData.putString(app, k, json, Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
    }

    @Nullable
    public static List<FollowingMenderModel.Datum> load(
            @Nullable Context context, @NonNull String type) {
        if (context == null) return null;
        Context app = context.getApplicationContext();
        String k = key(app, type);
        String json = null;
        if (k.equals(memoryKey) && !TextUtils.isEmpty(memoryJson)) {
            json = memoryJson;
        } else {
            try {
                json = SharedPrefsData.getString(app, k, Constants.PREF_NAME);
                if (!TextUtils.isEmpty(json)) {
                    memoryKey = k;
                    memoryJson = json;
                }
            } catch (Exception ignored) {
            }
        }
        if (TextUtils.isEmpty(json)) return null;
        try {
            List<FollowingMenderModel.Datum> list = GSON.fromJson(json, LIST_TYPE);
            return list != null ? list : new ArrayList<FollowingMenderModel.Datum>();
        } catch (Exception e) {
            return null;
        }
    }

    public static void clear(@Nullable Context context) {
        memoryKey = null;
        memoryJson = null;
        if (context == null) return;
        Context app = context.getApplicationContext();
        try {
            SharedPrefsData.putString(app, key(app, TYPE_FOLLOWING), "", Constants.PREF_NAME);
            SharedPrefsData.putString(app, key(app, TYPE_FOLLOWERS), "", Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
    }
}
