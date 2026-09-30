package com.anyNews.anynews.Utils;

import androidx.annotation.NonNull;

import java.util.Locale;

/**
 * App {@code LanguageID} → TTS locale used for read-aloud, including how digits
 * are spoken. Ids match profile labels and font catalogs:
 * 1 Malayalam, 2 Hindi, 3 Telugu, 4 Tamil, 5 Kannada, 6 Odia,
 * 7 English, 8 Bengali, 9 Marathi, 10 Gujarati.
 *
 * <p>Unknown ids use {@code en-IN}. They must not fall through to Telugu, or
 * Hindi and Kannada numbers are read in Telugu.
 */
public final class TtsLanguageLocales {

    private TtsLanguageLocales() {}

    @NonNull
    public static Locale forLanguageId(int langId) {
        switch (langId) {
            case 1:
                return new Locale("ml", "IN");
            case 2:
                return new Locale("hi", "IN");
            case 3:
                return new Locale("te", "IN");
            case 4:
                return new Locale("ta", "IN");
            case 5:
                return new Locale("kn", "IN");
            case 6:
                return new Locale("or", "IN");
            case 7:
                return new Locale("en", "IN");
            case 8:
                return new Locale("bn", "IN");
            case 9:
                return new Locale("mr", "IN");
            case 10:
                return new Locale("gu", "IN");
            default:
                return new Locale("en", "IN");
        }
    }
}
