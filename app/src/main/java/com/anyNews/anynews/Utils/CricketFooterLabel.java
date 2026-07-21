package com.anyNews.anynews.Utils;

import android.content.Context;

import androidx.annotation.Nullable;

import com.anyNews.anynews.R;

/**
 * Localized label for the bottom-footer cricket tab.
 *
 * The app selects its content language by an in-app {@code language_id} (stored
 * as {@link Constants#LanguageID}), NOT by device Locale, so footer labels
 * cannot come from per-locale {@code strings.xml}. Like the other footer labels
 * (Reload / Events / Category / Profile / Share, which come from the server
 * language pack), the cricket label is resolved from the selected language id.
 *
 * Language ids (server): 1 Malayalam · 2 Hindi · 3 Telugu · 4 Tamil ·
 * 5 Kannada · 6 Odia. Anything else falls back to English "Cricket"
 * ({@code R.string.cricket}).
 */
public final class CricketFooterLabel {

    private CricketFooterLabel() {
    }

    /** Reads the selected in-app language id and returns the matching label. */
    public static String forCurrentLanguage(@Nullable Context context) {
        int languageId = 0;
        if (context != null) {
            try {
                languageId = SharedPrefsData.getInt(
                        context.getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME);
            } catch (Exception ignored) {
            }
        }
        return forLanguageId(context, languageId);
    }

    public static String forLanguageId(@Nullable Context context, int languageId) {
        switch (languageId) {
            case 1: // Malayalam
                return "\u0D15\u0D4D\u0D30\u0D3F\u0D15\u0D4D\u0D15\u0D31\u0D4D\u0D31\u0D4D"; // ക്രിക്കറ്റ്
            case 2: // Hindi
                return "\u0915\u094D\u0930\u093F\u0915\u0947\u091F"; // क्रिकेट
            case 3: // Telugu
                return "\u0C15\u0C4D\u0C30\u0C3F\u0C15\u0C46\u0C1F\u0C4D"; // క్రికెట్
            case 4: // Tamil
                return "\u0B95\u0BBF\u0BB0\u0BBF\u0B95\u0BCD\u0B95\u0BC6\u0B9F\u0BCD"; // கிரிக்கெட்
            case 5: // Kannada
                return "\u0C95\u0CCD\u0CB0\u0CBF\u0C95\u0C46\u0C9F\u0CCD"; // ಕ್ರಿಕೆಟ್
            case 6: // Odia
                return "\u0B15\u0B4D\u0B30\u0B3F\u0B15\u0B47\u0B1F\u0B4D"; // କ୍ରିକେଟ୍
            default:
                if (context != null) {
                    try {
                        return context.getString(R.string.cricket);
                    } catch (Exception ignored) {
                    }
                }
                return "Cricket";
        }
    }
}
