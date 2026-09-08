package com.anyNews.anynews.Utils;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.Nullable;

/**
 * Disk cache for {@code List_All_Categories} keyed by language_id.
 * Invalidates automatically when the stored language no longer matches.
 */
public final class CategoriesDiskCache {

    private CategoriesDiskCache() {}

    public static void save(@Nullable Context context, int languageId, @Nullable String plainJson) {
        if (context == null || TextUtils.isEmpty(plainJson) || languageId <= 0) return;
        Context app = context.getApplicationContext();
        try {
            SharedPrefsData.putString(
                    app, Constants.CATEGORIES_DISK_CACHE_JSON, plainJson, Constants.PREF_NAME);
            SharedPrefsData.putInt(
                    app, Constants.CATEGORIES_DISK_CACHE_LANG, languageId, Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
    }

    @Nullable
    public static String loadIfMatches(@Nullable Context context, int languageId) {
        if (context == null || languageId <= 0) return null;
        Context app = context.getApplicationContext();
        try {
            int cachedLang =
                    SharedPrefsData.getInt(
                            app, Constants.CATEGORIES_DISK_CACHE_LANG, Constants.PREF_NAME);
            if (cachedLang != languageId) return null;
            String json =
                    SharedPrefsData.getString(
                            app, Constants.CATEGORIES_DISK_CACHE_JSON, Constants.PREF_NAME);
            if (TextUtils.isEmpty(json)) return null;
            return json;
        } catch (Exception e) {
            return null;
        }
    }

    public static void invalidate(@Nullable Context context) {
        if (context == null) return;
        Context app = context.getApplicationContext();
        try {
            SharedPrefsData.putString(
                    app, Constants.CATEGORIES_DISK_CACHE_JSON, "", Constants.PREF_NAME);
            SharedPrefsData.putInt(
                    app, Constants.CATEGORIES_DISK_CACHE_LANG, 0, Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
    }

    /** Call when language_id changes so the next open re-fetches. */
    public static void invalidateIfLanguageChanged(@Nullable Context context, int newLanguageId) {
        if (context == null || newLanguageId <= 0) return;
        Context app = context.getApplicationContext();
        try {
            int cachedLang =
                    SharedPrefsData.getInt(
                            app, Constants.CATEGORIES_DISK_CACHE_LANG, Constants.PREF_NAME);
            if (cachedLang > 0 && cachedLang != newLanguageId) {
                invalidate(app);
            }
        } catch (Exception ignored) {
        }
    }
}
