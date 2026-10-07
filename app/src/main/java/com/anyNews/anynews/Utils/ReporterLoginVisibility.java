package com.anyNews.anynews.Utils;

import android.content.Context;

import androidx.annotation.Nullable;

import org.json.JSONArray;

import java.util.List;

/**
 * Profile "+" for reporters. Visible only when the current app language is in
 * the admin Reporter login (per language) ON list. {@code show_reporter_login}
 * remains the boolean for one language so older APKs keep reading that field.
 */
public final class ReporterLoginVisibility {

    private ReporterLoginVisibility() {
    }

    /**
     * @param showReporterLogin boolean for {@code languageIdForFlag}; null when the payload omitted it
     * @param enabledLanguageIds ON language ids; null when the payload omitted the list.
     *                           An empty list means every language is off.
     */
    public static void save(
            @Nullable Context context,
            @Nullable Boolean showReporterLogin,
            @Nullable List<Integer> enabledLanguageIds,
            int languageIdForFlag) {
        if (context == null) {
            return;
        }
        Context app = context.getApplicationContext();
        if (enabledLanguageIds != null) {
            JSONArray arr = new JSONArray();
            for (Integer id : enabledLanguageIds) {
                if (id != null && id > 0) {
                    arr.put(id);
                }
            }
            SharedPrefsData.putString(
                    app, Constants.REPORTER_LOGIN_LANGUAGE_IDS, arr.toString(), Constants.PREF_NAME);
        }
        if (showReporterLogin != null && languageIdForFlag > 0) {
            SharedPrefsData.putBool(
                    app, Constants.SHOW_REPORTER_LOGIN, showReporterLogin, Constants.PREF_NAME);
            SharedPrefsData.putInt(
                    app, Constants.SHOW_REPORTER_LOGIN_LANG, languageIdForFlag, Constants.PREF_NAME);
        }
    }

    /** True only when this session's language has reporter login turned on. */
    public static boolean isEnabledForCurrentLanguage(@Nullable Context context) {
        if (context == null) {
            return false;
        }
        int lang = SharedPrefsData.getInt(context, Constants.LanguageID, Constants.PREF_NAME);
        if (lang <= 0) {
            return false;
        }
        String raw = SharedPrefsData.getString(
                context, Constants.REPORTER_LOGIN_LANGUAGE_IDS, Constants.PREF_NAME);
        if (raw != null) {
            try {
                JSONArray arr = new JSONArray(raw);
                for (int i = 0; i < arr.length(); i++) {
                    if (arr.optInt(i, -1) == lang) {
                        return true;
                    }
                }
                return false;
            } catch (Exception ignored) {
                return false;
            }
        }
        int storedLang = SharedPrefsData.getInt(
                context, Constants.SHOW_REPORTER_LOGIN_LANG, Constants.PREF_NAME);
        if (storedLang != lang) {
            return false;
        }
        return SharedPrefsData.getBool(context, Constants.SHOW_REPORTER_LOGIN, Constants.PREF_NAME);
    }
}
