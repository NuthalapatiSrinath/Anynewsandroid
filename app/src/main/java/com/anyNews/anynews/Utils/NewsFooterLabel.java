package com.anyNews.anynews.Utils;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.anyNews.anynews.R;

/**
 * Localized user-facing label for the main feed tab (footer first item) and
 * profile top-right "News" affordance.
 *
 * <p>In-app language is {@link Constants#LanguageID}, not device Locale. The
 * server pack key remains {@code reload} / {@code home} for backwards
 * compatibility; this helper forces the display word "News" (and native
 * equivalents) so admin packs that still say Reload / రీలోడ్ / Home never
 * leak into the UI.
 */
public final class NewsFooterLabel {

    private NewsFooterLabel() {
    }

    /** Selected in-app language → News label. */
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
                return "ന്യൂസ്";
            case 2: // Hindi
                return "न्यूज़";
            case 3: // Telugu
                return "న్యూస్";
            case 4: // Tamil
                return "நியூஸ்";
            case 5: // Kannada
                return "ನ್ಯೂಸ್";
            case 6: // Odia
                return "ନ୍ୟୁଜ୍";
            default:
                if (context != null) {
                    try {
                        return context.getString(R.string.news_back);
                    } catch (Exception ignored) {
                    }
                }
                return "News";
        }
    }

    /**
     * Prefer a pack string only when it already means News; otherwise fall back
     * to {@link #forCurrentLanguage}. Maps Reload / Home synonyms away.
     */
    public static String resolveDisplay(
            @Nullable Context context,
            @Nullable String packPrimary,
            @Nullable String packSecondary) {
        if (isUsableNewsLabel(packPrimary)) {
            return packPrimary.trim();
        }
        if (isUsableNewsLabel(packSecondary)) {
            return packSecondary.trim();
        }
        return forCurrentLanguage(context);
    }

    /** True when the pack value should be shown (not empty, not Reload/Home wording). */
    public static boolean isUsableNewsLabel(@Nullable String raw) {
        if (raw == null) return false;
        String t = raw.trim();
        if (t.isEmpty() || "null".equalsIgnoreCase(t)) return false;
        if (isReloadOrHomeWording(t)) return false;
        return true;
    }

    /**
     * Detects Reload / Home labels across English + common Indic spellings so
     * stale Static_Data packs never paint those words in the footer/profile.
     */
    public static boolean isReloadOrHomeWording(@Nullable String raw) {
        if (raw == null) return false;
        String t = raw.trim();
        if (t.isEmpty()) return false;
        // Latin
        String lower = t.toLowerCase(java.util.Locale.ROOT);
        if ("reload".equals(lower) || "re-load".equals(lower) || "re load".equals(lower)
                || "home".equals(lower) || "homes".equals(lower)) {
            return true;
        }
        // Telugu రీలోడ్ / హోమ్
        if (t.contains("రీలోడ్") || t.contains("రిలోడ్") || t.equals("హోమ్") || t.equals("హొం")) {
            return true;
        }
        // Hindi रीलोड / होम
        if (t.contains("रीलोड") || t.contains("री लोड") || t.equals("होम") || t.equals("होमपेज")) {
            return true;
        }
        // Tamil ரீலோட்
        if (t.contains("ரீலோட்") || t.contains("ரீலோடு") || t.equals("ஹோம்")) {
            return true;
        }
        // Kannada
        if (t.contains("ರೀಲೋಡ್") || t.equals("ಹೋಮ್")) {
            return true;
        }
        // Malayalam
        if (t.contains("റീലോഡ്") || t.equals("ഹോം")) {
            return true;
        }
        // Odia
        if (t.contains("ରିଲୋଡ୍") || t.equals("ହୋମ୍")) {
            return true;
        }
        return false;
    }

    /** Strip leading/trailing punctuation sometimes appended (“News >”). */
    public static String stripNavDecoration(@Nullable String label) {
        if (TextUtils.isEmpty(label)) return label;
        String t = label.trim();
        while (t.endsWith(">") || t.endsWith("›") || t.endsWith("»")) {
            t = t.substring(0, t.length() - 1).trim();
        }
        return t;
    }
}
