package com.anyNews.anynews.Utils;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Flash News chip label from splash/session ({@code flash_news_chip_label}).
 * Falls back to per-language defaults matching Daily Updates Flash strip.
 */
public final class FlashNewsChipHelper {

    public static final String PREF_CHIP_LABEL = "flash_news_chip_label";

    private FlashNewsChipHelper() {}

    public static void saveLabelFromServer(
            @Nullable Context context, @Nullable String label) {
        if (context == null || TextUtils.isEmpty(label)) return;
        SharedPrefsData.putString(
                context.getApplicationContext(),
                PREF_CHIP_LABEL,
                label.trim(),
                Constants.PREF_NAME);
    }

    @NonNull
    public static String chipLabel(@Nullable Context context) {
        try {
            String v =
                    SharedPrefsData.getString(
                            context != null ? context.getApplicationContext() : null,
                            PREF_CHIP_LABEL,
                            Constants.PREF_NAME);
            if (!TextUtils.isEmpty(v)) return v.trim();
        } catch (Exception ignored) {
        }
        return fallbackLabel(context);
    }

    @NonNull
    public static String fallbackLabel(@Nullable Context context) {
        int lang = 0;
        try {
            if (context != null) {
                lang =
                        SharedPrefsData.getInt(
                                context.getApplicationContext(),
                                Constants.LanguageID,
                                Constants.PREF_NAME);
            }
        } catch (Exception ignored) {
        }
        switch (lang) {
            case 1:
                return "ഫ്ലാഷ് ന്യൂസ്";
            case 2:
                return "फ्लैश न्यूज़";
            case 3:
                return "ఫ్లాష్ న్యూస్";
            case 4:
                return "ஃபிளாஷ் நியூஸ்";
            case 5:
                return "ಫ್ಲಾಶ್ ನ್ಯೂಸ್";
            case 6:
                return "ଫ୍ଲାଶ୍ ନ୍ୟୁଜ୍";
            default:
                return "Flash News";
        }
    }
}
