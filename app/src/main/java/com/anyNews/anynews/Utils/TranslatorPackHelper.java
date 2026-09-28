package com.anyNews.anynews.Utils;

import android.content.Context;

import androidx.annotation.Nullable;

import com.anyNews.anynews.Pojo.LanguageModelResponse;
import com.anyNews.anynews.Pojo.LanguageSelectionRequest;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Keeps {@link Allsharedpreference#Translator} aligned with {@link Constants#LanguageID}.
 *
 * <p>Root cause of mixed Hindi/Telugu Profile chrome: LanguageID flips to Telugu while the
 * SessionPreferences translator JSON stays on the previous (often Hindi) pack. Profile then
 * prefers pack strings ({@code edit}, {@code article}, {@code feedback}, FAB labels, …).
 */
public final class TranslatorPackHelper {

    public interface RefreshCallback {
        void onPackUpdated(@Nullable LanguageModelResponse pack);
    }

    private TranslatorPackHelper() {
    }

    public static void savePack(
            @Nullable Context context, int languageId, @Nullable LanguageModelResponse pack) {
        if (context == null || pack == null || !Boolean.TRUE.equals(pack.getSuccess())) {
            return;
        }
        Context app = context.getApplicationContext();
        try {
            Allsharedpreference pref = new Allsharedpreference(app);
            pref.setShared_Booleen(Allsharedpreference.Language, true);
            pref.setShared_String(Allsharedpreference.Translator, new Gson().toJson(pack));
            if (languageId > 0) {
                SharedPrefsData.putInt(
                        app, Constants.TRANSLATOR_LANGUAGE_ID, languageId, Constants.PREF_NAME);
            }
            try {
                OpenPathCache.markStaticSuccess(app);
            } catch (Exception ignored) {
            }
        } catch (Exception ignored) {
        }
    }

    public static void savePackJson(
            @Nullable Context context, int languageId, @Nullable String json) {
        if (context == null || json == null || json.trim().isEmpty()) {
            return;
        }
        try {
            LanguageModelResponse pack = new Gson().fromJson(json, LanguageModelResponse.class);
            savePack(context, languageId, pack);
        } catch (Exception ignored) {
        }
    }

    public static int getPackLanguageId(@Nullable Context context) {
        if (context == null) return 0;
        try {
            return SharedPrefsData.getInt(
                    context.getApplicationContext(),
                    Constants.TRANSLATOR_LANGUAGE_ID,
                    Constants.PREF_NAME);
        } catch (Exception e) {
            return 0;
        }
    }

    public static int selectedLanguageId(@Nullable Context context) {
        if (context == null) return 0;
        try {
            return SharedPrefsData.getInt(
                    context.getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME);
        } catch (Exception e) {
            return 0;
        }
    }

    /** True when translator pack is safe to apply for the selected LanguageID. */
    public static boolean packMatchesLanguage(@Nullable Context context) {
        int want = selectedLanguageId(context);
        if (want <= 0) return true;
        int have = getPackLanguageId(context);
        // Unstamped legacy packs are not trusted — Profile would paint Hindi leftovers
        // while LanguageID is Telugu. Force LanguageID chrome + get_static_data stamp.
        if (have <= 0) {
            return false;
        }
        return have == want;
    }

    /**
     * Prefer pack string only when pack matches LanguageID; otherwise use LanguageID fallback.
     *
     * <p>Malayalam (LanguageID 1) static_data still stores English for some menu keys
     * (Settings, Contact Us, Feedback on Anynews). Those are missing translations, not
     * real Malayalam copy — use the same LanguageID fallback the other languages use
     * when their pack value is absent. Pack strings that already contain Malayalam
     * stay as authored.
     */
    @Nullable
    public static String packOrFallback(
            @Nullable Context context, @Nullable String packValue, @Nullable String fallback) {
        if (packMatchesLanguage(context)
                && notEmpty(packValue)
                && !isUntranslatedMalayalam(context, packValue)) {
            return packValue.trim();
        }
        return fallback;
    }

    /** True when LanguageID is Malayalam and the pack value has no Malayalam letters. */
    private static boolean isUntranslatedMalayalam(
            @Nullable Context context, @Nullable String packValue) {
        if (selectedLanguageId(context) != 1 || packValue == null) {
            return false;
        }
        return !containsMalayalam(packValue);
    }

    private static boolean containsMalayalam(@Nullable String s) {
        if (s == null || s.isEmpty()) return false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= 0x0D00 && c <= 0x0D7F) {
                return true;
            }
        }
        return false;
    }

    public static boolean notEmpty(@Nullable String s) {
        return s != null && !s.trim().isEmpty() && !"null".equalsIgnoreCase(s.trim());
    }

    /**
     * If pack LanguageID mismatches selected language, force {@code get_static_data} refresh.
     *
     * @return true when a network refresh was started
     */
    public static boolean ensureMatchesSelectedLanguage(
            @Nullable Context context, @Nullable RefreshCallback callback) {
        if (context == null) return false;
        int want = selectedLanguageId(context);
        if (want <= 0) return false;
        if (packMatchesLanguage(context)) {
            return false;
        }
        try {
            SharedPrefsData.putLong(
                    context.getApplicationContext(),
                    Constants.OPEN_PATH_STATIC_AT,
                    0L,
                    Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
        refreshAsync(context, want, callback);
        return true;
    }

    public static void refreshAsync(
            @Nullable Context context, int languageId, @Nullable RefreshCallback callback) {
        if (context == null || languageId <= 0) return;
        final Context app = context.getApplicationContext();
        try {
            LanguageSelectionRequest requestModel = new LanguageSelectionRequest();
            requestModel.setDraftId(SharedPrefsData.getInt(app, Constants.DraftID, Constants.PREF_NAME));
            requestModel.setLanguageId(languageId);
            JsonObject body = new Gson().toJsonTree(requestModel).getAsJsonObject();
            APIService apiService = RestApiClient.getClient().create(APIService.class);
            Call<JsonObject> call = apiService.get_static_data("v3", body);
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    LanguageModelResponse pack = null;
                    try {
                        if (response.isSuccessful() && response.body() != null) {
                            pack = new Gson().fromJson(response.body(), LanguageModelResponse.class);
                            if (pack != null && Boolean.TRUE.equals(pack.getSuccess())) {
                                savePack(app, languageId, pack);
                            } else {
                                pack = null;
                            }
                        }
                    } catch (Exception ignored) {
                        pack = null;
                    }
                    if (callback != null) {
                        try {
                            callback.onPackUpdated(pack);
                        } catch (Exception ignored) {
                        }
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    if (callback != null) {
                        try {
                            callback.onPackUpdated(null);
                        } catch (Exception ignored) {
                        }
                    }
                }
            });
        } catch (Exception e) {
            if (callback != null) {
                try {
                    callback.onPackUpdated(null);
                } catch (Exception ignored) {
                }
            }
        }
    }

    /** Devanagari leftovers while LanguageID is not Hindi (2). */
    private static boolean packLooksHindi(@Nullable Context context) {
        if (context == null) return false;
        try {
            Allsharedpreference pref = new Allsharedpreference(context.getApplicationContext());
            String raw = pref.getShared_String(Allsharedpreference.Translator);
            if (raw == null || raw.isEmpty()) return false;
            LanguageModelResponse pack = new Gson().fromJson(raw, LanguageModelResponse.class);
            if (pack == null || pack.getData() == null) return false;
            LanguageModelResponse.Data d = pack.getData();
            return containsDevanagari(d.getSelectLanguage())
                    || containsDevanagari(d.getEdit())
                    || containsDevanagari(d.getArticle())
                    || containsDevanagari(d.getArticles())
                    || containsDevanagari(d.getFeedback())
                    || containsDevanagari(d.getFollowing())
                    || containsDevanagari(d.getVideoPost())
                    || containsDevanagari(d.getFollowers());
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean containsDevanagari(@Nullable String s) {
        if (s == null || s.isEmpty()) return false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= 0x0900 && c <= 0x097F) {
                return true;
            }
        }
        return false;
    }
}
