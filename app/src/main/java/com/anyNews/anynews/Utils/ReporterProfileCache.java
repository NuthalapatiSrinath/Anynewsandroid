package com.anyNews.anynews.Utils;

import android.content.Context;
import android.text.TextUtils;

import com.anyNews.anynews.Pojo.NewUserProfileResponse;
import com.google.gson.Gson;

/**
 * Instant reporter profile: memory + disk cache keyed by draftId + phone/email.
 * Open profile paints from cache; network soft-refreshes in background when TTL expired.
 */
public final class ReporterProfileCache {

    /** Soft-refresh TTL for get_user_profile (2–5 min band; pick 3). */
    public static final long SOFT_REFRESH_TTL_MS = 3L * 60L * 1000L;

    private static final Gson GSON = new Gson();
    private static volatile String memoryKey;
    private static volatile String memoryJson;
    private static volatile NewUserProfileResponse memoryParsed;

    private ReporterProfileCache() {
    }

    public static String cacheKey(Context context) {
        if (context == null) return "profile_cache_anon";
        Context app = context.getApplicationContext();
        int draft = SharedPrefsData.getInt(app, Constants.DraftID, Constants.PREF_NAME);
        String phone = SharedPrefsData.getString(app, Constants.Selected_Phone_Number, Constants.PREF_NAME);
        if (TextUtils.isEmpty(phone) || "null".equalsIgnoreCase(phone)) {
            phone = SharedPrefsData.getString(app, Constants.PhoneNumber, Constants.PREF_NAME);
        }
        String email = "";
        try {
            Allsharedpreference pref = new Allsharedpreference(app);
            email = pref.getEmailId() != null ? pref.getEmailId() : "";
        } catch (Exception ignored) {
        }
        String id = !TextUtils.isEmpty(phone) && !"null".equalsIgnoreCase(phone)
                ? phone.trim()
                : (!TextUtils.isEmpty(email) ? email.trim() : "anon");
        return "profile_cache_" + draft + "_" + id;
    }

    public static void save(Context context, String decryptedJson) {
        if (context == null || TextUtils.isEmpty(decryptedJson)) return;
        Context app = context.getApplicationContext();
        String key = cacheKey(app);
        memoryKey = key;
        memoryJson = decryptedJson;
        try {
            memoryParsed = GSON.fromJson(decryptedJson, NewUserProfileResponse.class);
        } catch (Exception e) {
            memoryParsed = null;
        }
        try {
            SharedPrefsData.putString(app, key, decryptedJson, Constants.PREF_NAME);
            // Legacy single-key used by wallet / article status screens.
            SharedPrefsData.putString(app, Constants.PROFILE_CACHE, decryptedJson, Constants.PREF_NAME);
            SharedPrefsData.putString(app, Constants.PROFILE_CACHE_KEY, key, Constants.PREF_NAME);
            SharedPrefsData.putLong(
                    app,
                    Constants.PROFILE_CACHE_FETCHED_AT,
                    System.currentTimeMillis(),
                    Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
        try {
            ReporterProfilePrefetcher.applyIdentityPrefsFromProfileJson(app, decryptedJson);
        } catch (Exception ignored) {
        }
    }

    /** True when a non-empty cache exists and was fetched within {@link #SOFT_REFRESH_TTL_MS}. */
    public static boolean isFresh(Context context) {
        if (context == null) return false;
        Context app = context.getApplicationContext();
        try {
            String json = loadJson(app);
            if (TextUtils.isEmpty(json)) return false;
            long at =
                    SharedPrefsData.getLong(
                            app, Constants.PROFILE_CACHE_FETCHED_AT, Constants.PREF_NAME);
            if (at <= 0L) return false;
            return System.currentTimeMillis() - at < SOFT_REFRESH_TTL_MS;
        } catch (Exception e) {
            return false;
        }
    }

    public static String loadJson(Context context) {
        if (context == null) return null;
        Context app = context.getApplicationContext();
        String key = cacheKey(app);
        if (key.equals(memoryKey) && !TextUtils.isEmpty(memoryJson)) {
            return memoryJson;
        }
        String json = null;
        try {
            json = SharedPrefsData.getString(app, key, Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
        if (TextUtils.isEmpty(json)) {
            try {
                json = SharedPrefsData.getString(app, Constants.PROFILE_CACHE, Constants.PREF_NAME);
            } catch (Exception ignored) {
            }
        }
        if (!TextUtils.isEmpty(json)) {
            memoryKey = key;
            memoryJson = json;
            try {
                memoryParsed = GSON.fromJson(json, NewUserProfileResponse.class);
            } catch (Exception e) {
                memoryParsed = null;
            }
        }
        return json;
    }

    public static NewUserProfileResponse load(Context context) {
        if (context == null) return null;
        String key = cacheKey(context);
        if (key.equals(memoryKey) && memoryParsed != null) {
            return memoryParsed;
        }
        String json = loadJson(context);
        if (TextUtils.isEmpty(json)) return null;
        try {
            NewUserProfileResponse resp = GSON.fromJson(json, NewUserProfileResponse.class);
            memoryParsed = resp;
            return resp;
        } catch (Exception e) {
            return null;
        }
    }

    public static void clear(Context context) {
        memoryKey = null;
        memoryJson = null;
        memoryParsed = null;
        if (context == null) return;
        Context app = context.getApplicationContext();
        try {
            String key = SharedPrefsData.getString(app, Constants.PROFILE_CACHE_KEY, Constants.PREF_NAME);
            if (!TextUtils.isEmpty(key)) {
                SharedPrefsData.putString(app, key, "", Constants.PREF_NAME);
            }
            SharedPrefsData.putString(app, Constants.PROFILE_CACHE, "", Constants.PREF_NAME);
            SharedPrefsData.putString(app, Constants.PROFILE_CACHE_KEY, "", Constants.PREF_NAME);
            SharedPrefsData.putLong(app, Constants.PROFILE_CACHE_FETCHED_AT, 0L, Constants.PREF_NAME);
            // Also clear current identity key.
            SharedPrefsData.putString(app, cacheKey(app), "", Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
    }
}
