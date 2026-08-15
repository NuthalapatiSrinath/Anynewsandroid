package com.anyNews.anynews.Utils;

import android.content.Context;

import androidx.annotation.Nullable;

import com.anyNews.anynews.R;

/**
 * Labels for secondary home chrome: Calendar | Reload.
 * Uses {@link Constants#LanguageID} (same pattern as {@link HomeOnlineModeLabels}).
 */
public final class HomeChromeActionLabels {

    private HomeChromeActionLabels() {}

    public static String calendar(@Nullable Context context) {
        return calendarForLanguageId(context, languageId(context));
    }

    public static String reload(@Nullable Context context) {
        return reloadForLanguageId(context, languageId(context));
    }

    public static String calendarForLanguageId(@Nullable Context context, int languageId) {
        switch (languageId) {
            case 1: return "കലണ്ടർ";
            case 2: return "कैलेंडर";
            case 3: return "క్యాలెండర్";
            case 4: return "நாட்காட்டி";
            case 5: return "ಕ್ಯಾಲೆಂಡರ್";
            case 6: return "କ୍ୟାଲେଣ୍ଡର";
            default:
                return str(context, R.string.home_chrome_calendar, "Calendar");
        }
    }

    public static String reloadForLanguageId(@Nullable Context context, int languageId) {
        switch (languageId) {
            case 1: return "റീലോഡ്";
            case 2: return "रीलोड";
            case 3: return "రీలోడ్";
            case 4: return "மீண்டும்";
            case 5: return "ರೀಲೋಡ್";
            case 6: return "ରିଲୋଡ୍";
            default:
                return str(context, R.string.home_chrome_reload, "Reload");
        }
    }

    private static int languageId(@Nullable Context context) {
        if (context == null) return 0;
        try {
            return SharedPrefsData.getInt(
                    context.getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME);
        } catch (Exception ignored) {
            return 0;
        }
    }

    private static String str(@Nullable Context context, int resId, String fallback) {
        if (context != null) {
            try {
                return context.getString(resId);
            } catch (Exception ignored) {
            }
        }
        return fallback;
    }
}
