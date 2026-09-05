package com.anyNews.anynews.Utils;

import android.content.Context;
import android.text.TextUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.anyNews.anynews.models.DistricsModel;

import java.util.List;

/**
 * Single source of truth for the district / location pill label.
 *
 * <p>District display names must follow the selected in-app {@link Constants#LanguageID},
 * not a frozen ctor string or an offline snapshot from another language. Store district id
 * (stable) and always re-resolve the label for the current language when binding or resuming.
 */
public final class DistrictLabelHelper {

    private DistrictLabelHelper() {}

    /** Persist district id + display name tagged with the current language id. */
    public static void saveSelected(
            @Nullable Context context, int distId, @Nullable String name) {
        if (context == null) {
            return;
        }
        Context app = context.getApplicationContext();
        int lang = SharedPrefsData.getInt(app, Constants.LanguageID, Constants.PREF_NAME);
        if (distId > 0) {
            SharedPrefsData.putInt(app, Constants.Selected_Distric_Id, distId, Constants.PREF_NAME);
        }
        if (!isUsable(name)) {
            return;
        }
        String n = name.trim();
        SharedPrefsData.putString(app, Constants.Selected_Distric_Name, n, Constants.PREF_NAME);
        SharedPrefsData.putString(app, Constants.Selected_Distric_Name1, n, Constants.PREF_NAME);
        SharedPrefsData.putInt(app, Constants.Selected_Distric_Name_LangId, lang, Constants.PREF_NAME);
    }

    /** Update display name only (keeps stored id); tags with current language. */
    public static void saveName(@Nullable Context context, @Nullable String name) {
        if (context == null) {
            return;
        }
        int id = SharedPrefsData.getInt(
                context.getApplicationContext(), Constants.Selected_Distric_Id, Constants.PREF_NAME);
        saveSelected(context, id, name);
    }

    /**
     * Splash / server write: store name for the language id the server just applied
     * (may differ mid-session only when splash updates LanguageID).
     */
    public static void saveFromServer(
            @Nullable Context context,
            int languageId,
            int distId,
            @Nullable String name) {
        if (context == null || !isUsable(name)) {
            return;
        }
        Context app = context.getApplicationContext();
        if (distId > 0) {
            SharedPrefsData.putInt(app, Constants.Selected_Distric_Id, distId, Constants.PREF_NAME);
        }
        String n = name.trim();
        SharedPrefsData.putString(app, Constants.Selected_Distric_Name, n, Constants.PREF_NAME);
        SharedPrefsData.putString(app, Constants.Selected_Distric_Name1, n, Constants.PREF_NAME);
        int tagLang = languageId > 0
                ? languageId
                : SharedPrefsData.getInt(app, Constants.LanguageID, Constants.PREF_NAME);
        SharedPrefsData.putInt(app, Constants.Selected_Distric_Name_LangId, tagLang, Constants.PREF_NAME);
    }

    public static int getStoredDistId(@Nullable Context context) {
        if (context == null) {
            return 0;
        }
        return SharedPrefsData.getInt(
                context.getApplicationContext(), Constants.Selected_Distric_Id, Constants.PREF_NAME);
    }

    /**
     * Resolve the district pill / header label for the <b>current</b> app language.
     * Always re-reads prefs (never relies on FlipperAdapter ctor freeze alone).
     *
     * <p>When LanguageID expects an Indic script and prefs only have a Latin name
     * (e.g. English "Anakapalli" under TE), re-resolves from the language-keyed
     * district list cache so Mana Zilla / location pills show native script.
     */
    @NonNull
    public static String resolveDisplayLabel(@Nullable Context context) {
        if (context == null) {
            return "";
        }
        Context app = context.getApplicationContext();
        int lang = SharedPrefsData.getInt(app, Constants.LanguageID, Constants.PREF_NAME);
        int nameLang = SharedPrefsData.getInt(
                app, Constants.Selected_Distric_Name_LangId, Constants.PREF_NAME);

        String d1 = SharedPrefsData.getString(app, Constants.Selected_Distric_Name1, Constants.PREF_NAME);
        String d0 = SharedPrefsData.getString(app, Constants.Selected_Distric_Name, Constants.PREF_NAME);

        String match = firstCompatible(lang, nameLang, d1, d0);
        if (isUsable(match) && !needsNativeScriptUpgrade(match, lang)) {
            return match.trim();
        }

        // Prefs are Latin-only under an Indic language — look up localized name by id.
        String fromCache = lookupLocalizedNameFromCache(app, lang);
        if (isUsable(fromCache)) {
            // Soft-upgrade: keep any prior Latin name in Selected_Distric_Name so
            // English server location_label can still match the selected district.
            String priorLatin = null;
            if (isLatinOrDigitsOnlySafe(match)) {
                priorLatin = match.trim();
            } else if (isLatinOrDigitsOnlySafe(d1)) {
                priorLatin = d1.trim();
            } else if (isLatinOrDigitsOnlySafe(d0)) {
                priorLatin = d0.trim();
            }
            int distId = getStoredDistId(app);
            if (distId > 0) {
                SharedPrefsData.putInt(app, Constants.Selected_Distric_Id, distId, Constants.PREF_NAME);
            }
            if (isUsable(priorLatin)) {
                SharedPrefsData.putString(
                        app, Constants.Selected_Distric_Name, priorLatin, Constants.PREF_NAME);
            }
            SharedPrefsData.putString(
                    app, Constants.Selected_Distric_Name1, fromCache.trim(), Constants.PREF_NAME);
            SharedPrefsData.putInt(
                    app, Constants.Selected_Distric_Name_LangId, lang, Constants.PREF_NAME);
            return fromCache.trim();
        }

        // Offline snapshot: only when it was captured for this language.
        String offline = OfflineFeedStore.loadDistrictLabelForLanguage(app, lang);
        if (isUsable(offline) && labelCompatibleWithLanguage(offline, lang)
                && !needsNativeScriptUpgrade(offline, lang)) {
            // Soft-fill empty prefs so pin has text; tag for current language.
            if (!isUsable(d1) && !isUsable(d0)) {
                saveFromServer(app, lang, getStoredDistId(app), offline);
            }
            return offline.trim();
        }

        // Prefer not to mix languages. When the only cached label is Telugu script
        // while Language_Id is Hindi (or similar), return empty — splash/district
        // reselect supplies the correct name. Never keep a foreign-script pill on
        // the main feed chrome.
        if (isUsable(offline) && !isClearScriptConflict(offline, lang)
                && !needsNativeScriptUpgrade(offline, lang)) {
            return offline.trim();
        }
        if (isUsable(d1) && !isClearScriptConflict(d1, lang)
                && !needsNativeScriptUpgrade(d1, lang)) {
            return d1.trim();
        }
        if (isUsable(d0) && !isClearScriptConflict(d0, lang)
                && !needsNativeScriptUpgrade(d0, lang)) {
            return d0.trim();
        }
        // Last resort: Latin name still better than blank (until cache warms).
        if (isUsable(match)) {
            return match.trim();
        }
        if (isUsable(d1)) {
            return d1.trim();
        }
        if (isUsable(d0)) {
            return d0.trim();
        }
        return "";
    }

    /**
     * True when {@code candidate} is Latin-only while {@code languageId} expects
     * an Indic script — UI should prefer a native-script district name.
     */
    public static boolean needsNativeScriptUpgrade(@Nullable String candidate, int languageId) {
        return expectsIndicScript(languageId) && isLatinOrDigitsOnlySafe(candidate);
    }

    /** Language ids 1–6 use Indic scripts (ML/HI/TE/TA/KN/OR). */
    public static boolean expectsIndicScript(int languageId) {
        return expectedScriptForLanguage(languageId) > 0;
    }

    public static boolean isLatinOnly(@Nullable String s) {
        return isLatinOrDigitsOnlySafe(s);
    }

    private static boolean isLatinOrDigitsOnlySafe(@Nullable String s) {
        return isUsable(s) && isLatinOrDigitsOnly(s.trim());
    }

    /**
     * True when {@code label} matches a stored district name (any script / case).
     * Used so an English post {@code location_label} can upgrade to the localized pill.
     */
    public static boolean matchesStoredName(@Nullable Context context, @Nullable String label) {
        if (context == null || !isUsable(label)) {
            return false;
        }
        Context app = context.getApplicationContext();
        String needle = label.trim();
        String d1 = SharedPrefsData.getString(app, Constants.Selected_Distric_Name1, Constants.PREF_NAME);
        String d0 = SharedPrefsData.getString(app, Constants.Selected_Distric_Name, Constants.PREF_NAME);
        if (namesEqualLoose(needle, d1) || namesEqualLoose(needle, d0)) {
            return true;
        }
        String resolved = resolveDisplayLabel(context);
        return namesEqualLoose(needle, resolved);
    }

    private static boolean namesEqualLoose(@Nullable String a, @Nullable String b) {
        if (!isUsable(a) || !isUsable(b)) {
            return false;
        }
        return a.trim().equalsIgnoreCase(b.trim());
    }

    /**
     * Look up the selected district's display name from the language+state district
     * list cache (same source as the change-district picker).
     */
    @Nullable
    public static String lookupLocalizedNameFromCache(@Nullable Context context, int languageId) {
        if (context == null || languageId <= 0) {
            return null;
        }
        Context app = context.getApplicationContext();
        int distId = getStoredDistId(app);
        int stateId = SharedPrefsData.getInt(app, Constants.StateID, Constants.PREF_NAME);
        if (distId <= 0 || stateId <= 0) {
            return null;
        }
        try {
            List<DistricsModel.Datum> list =
                    SelectionListCache.loadDistricts(app, languageId, stateId);
            if (list == null || list.isEmpty()) {
                return null;
            }
            for (DistricsModel.Datum d : list) {
                if (d == null || d.getId() == null) {
                    continue;
                }
                if (d.getId() == distId && isUsable(d.getName())) {
                    String name = d.getName().trim();
                    // Prefer native-script names; skip Latin entries if present.
                    if (!needsNativeScriptUpgrade(name, languageId)) {
                        return name;
                    }
                    // Keep as weak fallback if list only has Latin.
                    return name;
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /** True when label is clearly another Indic script than the selected language. */
    private static boolean isClearScriptConflict(@Nullable String label, int languageId) {
        int expected = expectedScriptForLanguage(languageId);
        if (expected == 0 || !isUsable(label)) {
            return false;
        }
        int dominant = dominantIndicScript(label);
        return dominant > 0 && dominant != expected;
    }

    /**
     * If prefs were empty, soft-fill from language-matched offline only.
     * Never copies a Telugu offline label into prefs while language is Hindi.
     */
    @NonNull
    public static String ensurePrefsForCurrentLanguage(@Nullable Context context) {
        if (context == null) {
            return "";
        }
        String resolved = resolveDisplayLabel(context);
        return isUsable(resolved) ? resolved.trim() : "";
    }

    /** Apply resolved district text to one or more TextViews (header, etc.). */
    public static void applyTo(@Nullable Context context, @Nullable TextView... views) {
        if (views == null || views.length == 0) {
            return;
        }
        String label = resolveDisplayLabel(context);
        for (TextView tv : views) {
            if (tv == null) {
                continue;
            }
            try {
                tv.setText(label);
            } catch (Exception ignored) {
            }
        }
    }

    public static boolean isUsable(@Nullable String s) {
        if (s == null) {
            return false;
        }
        String t = s.trim();
        return !t.isEmpty() && !"null".equalsIgnoreCase(t) && !"N/A".equalsIgnoreCase(t);
    }

    /**
     * Prefer labels tagged for {@code languageId}, then native-script matches,
     * then Latin/neutral names (caller may still upgrade Latin via district cache).
     */
    @Nullable
    private static String firstCompatible(
            int languageId, int nameLangId, @Nullable String... candidates) {
        if (candidates == null) {
            return null;
        }
        // 1) Tag match + native script for Indic languages (skip Latin "Anakapalli" under TE).
        if (nameLangId > 0 && nameLangId == languageId) {
            for (String c : candidates) {
                if (isUsable(c)
                        && !isClearScriptConflict(c, languageId)
                        && !needsNativeScriptUpgrade(c, languageId)) {
                    return c;
                }
            }
        }
        // 2) Script matches current language (Indic), not Latin.
        for (String c : candidates) {
            if (isUsable(c)
                    && labelCompatibleWithLanguage(c, languageId)
                    && !needsNativeScriptUpgrade(c, languageId)) {
                return c;
            }
        }
        // 3) Latin / neutral fallback (may be upgraded by cache lookup).
        for (String c : candidates) {
            if (isUsable(c) && labelCompatibleWithLanguage(c, languageId)) {
                return c;
            }
        }
        return null;
    }

    /**
     * True when {@code label} is Latin/neutral or uses the Unicode range for
     * {@code languageId}. Tags with wrong script for the selected language return false
     * so we do not prefer e.g. Telugu "అనకాపల్లి" while Language_Id is Hindi.
     *
     * <p>Language ids (server): 1 ML · 2 HI · 3 TE · 4 TA · 5 KN · 6 OR.
     */
    public static boolean labelCompatibleWithLanguage(@Nullable String label, int languageId) {
        if (!isUsable(label)) {
            return false;
        }
        if (isLatinOrDigitsOnly(label)) {
            return true;
        }
        int dominant = dominantIndicScript(label);
        if (dominant == 0) {
            // Mixed / unknown — allow rather than blank the pill.
            return true;
        }
        int expected = expectedScriptForLanguage(languageId);
        if (expected == 0) {
            return true;
        }
        return dominant == expected;
    }

    private static boolean isLatinOrDigitsOnly(@NonNull String s) {
        for (int i = 0; i < s.length(); ) {
            int cp = s.codePointAt(i);
            i += Character.charCount(cp);
            if (Character.isWhitespace(cp) || cp == '-' || cp == '\'' || cp == '.') {
                continue;
            }
            if (cp < 0x80) {
                continue;
            }
            // Common punctuation outside ASCII
            if (cp == 0x2013 || cp == 0x2014) {
                continue;
            }
            return false;
        }
        return true;
    }

    /** @return script family constant, or 0 if none dominant */
    private static int dominantIndicScript(@NonNull String s) {
        int ml = 0, hi = 0, te = 0, ta = 0, kn = 0, or = 0;
        for (int i = 0; i < s.length(); ) {
            int cp = s.codePointAt(i);
            i += Character.charCount(cp);
            if (cp >= 0x0D00 && cp <= 0x0D7F) {
                ml++;
            } else if (cp >= 0x0900 && cp <= 0x097F) {
                hi++;
            } else if (cp >= 0x0C00 && cp <= 0x0C7F) {
                te++;
            } else if (cp >= 0x0B80 && cp <= 0x0BFF) {
                ta++;
            } else if (cp >= 0x0C80 && cp <= 0x0CFF) {
                kn++;
            } else if (cp >= 0x0B00 && cp <= 0x0B7F) {
                or++;
            }
        }
        int best = 0;
        int script = 0;
        if (ml > best) {
            best = ml;
            script = 1;
        }
        if (hi > best) {
            best = hi;
            script = 2;
        }
        if (te > best) {
            best = te;
            script = 3;
        }
        if (ta > best) {
            best = ta;
            script = 4;
        }
        if (kn > best) {
            best = kn;
            script = 5;
        }
        if (or > best) {
            script = 6;
        }
        return script;
    }

    private static int expectedScriptForLanguage(int languageId) {
        switch (languageId) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
                return languageId;
            default:
                return 0;
        }
    }
}
