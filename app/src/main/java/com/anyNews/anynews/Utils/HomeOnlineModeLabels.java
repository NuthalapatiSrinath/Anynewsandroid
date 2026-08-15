package com.anyNews.anynews.Utils;

import android.content.Context;

import androidx.annotation.Nullable;

import com.anyNews.anynews.R;

/**
 * Localized labels for the online-only top pill:
 * {@code News | Read News}.
 *
 * <p>In-app language is {@link Constants#LanguageID}, not device Locale — same
 * pattern as {@link HomeOfflineModeLabels} / {@link NewsFooterLabel}.
 *
 * Language ids (server): 1 Malayalam · 2 Hindi · 3 Telugu · 4 Tamil ·
 * 5 Kannada · 6 Odia. Anything else falls back to English string resources.
 */
public final class HomeOnlineModeLabels {

    private HomeOnlineModeLabels() {
    }

    /** Left segment: News (full chronological feed). No count badge in product UI. */
    public static String news(@Nullable Context context) {
        return newsForLanguageId(context, currentLanguageId(context));
    }

    /** Right segment: Read News (local already-viewed). Badge shows local read count. */
    public static String readNews(@Nullable Context context) {
        return readNewsForLanguageId(context, currentLanguageId(context));
    }

    public static String newsForLanguageId(@Nullable Context context, int languageId) {
        // Align with footer News wording per language.
        return NewsFooterLabel.forLanguageId(context, languageId);
    }

    public static String readNewsForLanguageId(@Nullable Context context, int languageId) {
        switch (languageId) {
            case 1: // Malayalam — വായിച്ച വാർത്തകൾ
                return "വായിച്ചവ";
            case 2: // Hindi — पढ़ी हुई खबरें
                return "पढ़े हुए";
            case 3: // Telugu — చదివినవి (chandivinavi)
                return "చదివినవి";
            case 4: // Tamil — படித்தவை
                return "படித்தவை";
            case 5: // Kannada — ಓದಿದವು
                return "ಓದಿದವು";
            case 6: // Odia — ପଢ଼ାଯାଇଥିବା
                return "ପଢ଼ାଯାଇଥିବା";
            default:
                if (context != null) {
                    try {
                        return context.getString(R.string.home_mode_read_news);
                    } catch (Exception ignored) {
                    }
                }
                return "Read News";
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
