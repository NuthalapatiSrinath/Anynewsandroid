package com.anyNews.anynews.Utils;

import android.content.Context;

import androidx.annotation.Nullable;

import com.anyNews.anynews.R;

/**
 * Localized labels for the offline-only top pill:
 * {@code Offline News | Bookmarks}.
 *
 * <p>In-app language is {@link Constants#LanguageID}, not device Locale — same
 * pattern as {@link NewsFooterLabel} / {@link CricketFooterLabel}. Layout
 * {@code @string/…} defaults remain English for inflation; {@link HomePage}
 * rebinds both segment labels from these helpers whenever toggle chrome paints.
 *
 * Language ids (server): 1 Malayalam · 2 Hindi · 3 Telugu · 4 Tamil ·
 * 5 Kannada · 6 Odia. Anything else falls back to English string resources.
 */
public final class HomeOfflineModeLabels {

    private HomeOfflineModeLabels() {
    }

    /** Left segment: Offline News. */
    public static String offlineNews(@Nullable Context context) {
        return offlineNewsForLanguageId(context, currentLanguageId(context));
    }

    /** Right segment: Bookmarks. */
    public static String bookmarks(@Nullable Context context) {
        return bookmarksForLanguageId(context, currentLanguageId(context));
    }

    public static String offlineNewsForLanguageId(@Nullable Context context, int languageId) {
        switch (languageId) {
            case 1: // Malayalam — ഓഫ്‌ലൈൻ ന്യൂസ്
                return "ഓഫ്‌ലൈൻ ന്യൂസ്";
            case 2: // Hindi — ऑफलाइन न्यूज़
                return "ऑफलाइन न्यूज़";
            case 3: // Telugu — ఆఫ్‌లైన్ న్యూస్
                return "ఆఫ్‌లైన్ న్యూస్";
            case 4: // Tamil — ஆஃப்லைன் நியூஸ்
                return "ஆஃப்லைன் நியூஸ்";
            case 5: // Kannada — ಆಫ್‌ಲೈನ್ ನ್ಯೂಸ್
                return "ಆಫ್‌ಲೈನ್ ನ್ಯೂಸ್";
            case 6: // Odia — ଅଫଲାଇନ୍ ନ୍ୟୁଜ୍
                return "ଅଫଲାଇନ୍ ନ୍ୟୁଜ୍";
            default:
                if (context != null) {
                    try {
                        return context.getString(R.string.home_mode_offline_news);
                    } catch (Exception ignored) {
                    }
                }
                return "Offline News";
        }
    }

    public static String bookmarksForLanguageId(@Nullable Context context, int languageId) {
        switch (languageId) {
            case 1: // Malayalam — ബുക്ക്മാർക്കുകൾ
                return "ബുക്ക്മാർക്കുകൾ";
            case 2: // Hindi — बुकमार्क
                return "बुकमार्क";
            case 3: // Telugu — బుక్‌మార్కులు
                return "బుక్‌మార్కులు";
            case 4: // Tamil — புக்மார்க்குகள்
                return "புக்மார்க்குகள்";
            case 5: // Kannada — ಬುಕ್‌ಮಾರ್ಕ್‌ಗಳು
                return "ಬುಕ್‌ಮಾರ್ಕ್‌ಗಳು";
            case 6: // Odia — ବୁକମାର୍କ
                return "ବୁକମାର୍କ";
            default:
                if (context != null) {
                    try {
                        return context.getString(R.string.bookmarks);
                    } catch (Exception ignored) {
                    }
                }
                return "Bookmarks";
        }
    }

    private static int currentLanguageId(@Nullable Context context) {
        if (context == null) return 0;
        try {
            return SharedPrefsData.getInt(
                    context.getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME);
        } catch (Exception ignored) {
            return 0;
        }
    }
}
