package com.anyNews.anynews.Utils;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Jobs hub cache — splash {@code jobs} payload and/or {@code /get_jobs} refresh.
 * Empty items when admin has not posted; never invent fake counts.
 */
public final class JobsConfigHelper {

    private JobsConfigHelper() {}

    public static void saveFromSplash(@Nullable Context context, @Nullable Object raw) {
        if (context == null || raw == null) return;
        try {
            JSONObject o = toJsonObject(raw);
            if (o == null) return;
            Context app = context.getApplicationContext();
            SharedPrefsData.putString(
                    app, Constants.JOBS_CONFIG_JSON, o.toString(), Constants.PREF_NAME);
            boolean enabled = o.optBoolean("enabled", true);
            SharedPrefsData.putString(
                    app, Constants.JOBS_ENABLED, enabled ? "true" : "false", Constants.PREF_NAME);
            SharedPrefsData.putBool(app, Constants.JOBS_CONFIG_SET, true, Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
    }

    public static void savePayloadJson(@Nullable Context context, @Nullable JSONObject o) {
        if (context == null || o == null) return;
        try {
            Context app = context.getApplicationContext();
            SharedPrefsData.putString(
                    app, Constants.JOBS_CONFIG_JSON, o.toString(), Constants.PREF_NAME);
            SharedPrefsData.putString(
                    app,
                    Constants.JOBS_ENABLED,
                    o.optBoolean("enabled", true) ? "true" : "false",
                    Constants.PREF_NAME);
            SharedPrefsData.putBool(app, Constants.JOBS_CONFIG_SET, true, Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
    }

    public static boolean isEnabled(@Nullable Context context) {
        if (context == null) return true;
        try {
            String v = SharedPrefsData.getString(
                    context.getApplicationContext(), Constants.JOBS_ENABLED, Constants.PREF_NAME);
            if (TextUtils.isEmpty(v)) return true;
            return "true".equalsIgnoreCase(v) || "1".equals(v);
        } catch (Exception e) {
            return true;
        }
    }

    @Nullable
    public static JSONObject jobsJson(@Nullable Context context) {
        if (context == null) return null;
        try {
            String v = SharedPrefsData.getString(
                    context.getApplicationContext(),
                    Constants.JOBS_CONFIG_JSON,
                    Constants.PREF_NAME);
            if (TextUtils.isEmpty(v)) return null;
            return new JSONObject(v);
        } catch (Exception e) {
            return null;
        }
    }

    @NonNull
    public static JSONArray items(@Nullable Context context) {
        JSONObject o = jobsJson(context);
        if (o == null) return new JSONArray();
        JSONArray a = o.optJSONArray("items");
        return a != null ? a : new JSONArray();
    }

    @NonNull
    public static JSONObject stats(@Nullable Context context) {
        JSONObject o = jobsJson(context);
        if (o == null) return emptyStats();
        JSONObject s = o.optJSONObject("stats");
        return s != null ? s : emptyStats();
    }

    @NonNull
    public static JSONObject emptyStats() {
        try {
            return new JSONObject()
                    .put("total_jobs", 0)
                    .put("total_companies", 0)
                    .put("total_locations", 0)
                    .put("new_today", 0);
        } catch (Exception e) {
            return new JSONObject();
        }
    }

    @Nullable
    private static JSONObject toJsonObject(@NonNull Object raw) {
        try {
            if (raw instanceof JSONObject) return (JSONObject) raw;
            if (raw instanceof String) {
                String s = ((String) raw).trim();
                if (s.isEmpty() || "null".equalsIgnoreCase(s)) return null;
                return new JSONObject(s);
            }
            return new JSONObject(String.valueOf(new com.google.gson.Gson().toJson(raw)));
        } catch (Exception e) {
            return null;
        }
    }

    /** Prefer TE when LanguageID=3, else EN with TE fallback. */
    @NonNull
    public static String localized(
            @Nullable Context context,
            @Nullable JSONObject job,
            @NonNull String teKey,
            @NonNull String enKey) {
        if (job == null) return "";
        int lang = 0;
        try {
            if (context != null) {
                lang = SharedPrefsData.getInt(
                        context.getApplicationContext(),
                        Constants.LanguageID,
                        Constants.PREF_NAME);
            }
        } catch (Exception ignored) {
        }
        String te = job.optString(teKey, "");
        String en = job.optString(enKey, "");
        if (lang == 3) {
            return !TextUtils.isEmpty(te) ? te : en;
        }
        return !TextUtils.isEmpty(en) ? en : te;
    }
}
