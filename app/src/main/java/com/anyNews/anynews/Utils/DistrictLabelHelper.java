package com.anyNews.anynews.Utils;

import android.content.Context;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.anyNews.anynews.models.DistricsModel;

import java.util.List;

/**
 * Single source of truth for the district / location pill label.
 *
 * <p>District display names must follow the selected in-app {@link Constants#LanguageID}.
 * Numeric district ids collide across languages — never display or keep a district that
 * belongs to another language (e.g. Hindi भिवानी under Telugu UI).
 *
 * <p>Active selection uses global prefs; each language also has a snapshot so switching
 * languages can restore the last valid district for that language.
 */
public final class DistrictLabelHelper {

    private DistrictLabelHelper() {}

    private static String keyDistId(int languageId) {
        return Constants.Selected_Distric_Id + "_lang_" + languageId;
    }

    private static String keyDistName(int languageId) {
        return Constants.Selected_Distric_Name1 + "_lang_" + languageId;
    }

    private static String keyStateId(int languageId) {
        return Constants.StateID + "_lang_" + languageId;
    }

    /** Persist district id + display name tagged with the current language id. */
    public static void saveSelected(
            @Nullable Context context, int distId, @Nullable String name) {
        if (context == null) {
            return;
        }
        Context app = context.getApplicationContext();
        int lang = SharedPrefsData.getInt(app, Constants.LanguageID, Constants.PREF_NAME);
        int stateId = SharedPrefsData.getInt(app, Constants.StateID, Constants.PREF_NAME);
        saveSelectedForLanguage(app, lang, stateId, distId, name);
    }

    /**
     * Persist geography for {@code languageId}. Rejects clear cross-script names
     * so a Hindi Devanagari label cannot be stored under Telugu.
     */
    public static void saveSelectedForLanguage(
            @Nullable Context context,
            int languageId,
            int stateId,
            int distId,
            @Nullable String name) {
        if (context == null || languageId <= 0) {
            return;
        }
        if (isUsable(name) && isClearScriptConflict(name, languageId)) {
            // Never stamp a foreign-script label as belonging to this language.
            clearActiveSelection(context);
            return;
        }
        Context app = context.getApplicationContext();
        if (distId > 0) {
            SharedPrefsData.putInt(app, Constants.Selected_Distric_Id, distId, Constants.PREF_NAME);
            SharedPrefsData.putInt(app, keyDistId(languageId), distId, Constants.PREF_NAME);
        }
        if (stateId > 0) {
            SharedPrefsData.putInt(app, Constants.StateID, stateId, Constants.PREF_NAME);
            SharedPrefsData.putInt(app, keyStateId(languageId), stateId, Constants.PREF_NAME);
        }
        if (!isUsable(name)) {
            if (distId > 0) {
                SharedPrefsData.putInt(
                        app, Constants.Selected_Distric_Name_LangId, languageId, Constants.PREF_NAME);
            }
            return;
        }
        String n = name.trim();
        SharedPrefsData.putString(app, Constants.Selected_Distric_Name, n, Constants.PREF_NAME);
        SharedPrefsData.putString(app, Constants.Selected_Distric_Name1, n, Constants.PREF_NAME);
        SharedPrefsData.putInt(app, Constants.Selected_Distric_Name_LangId, languageId, Constants.PREF_NAME);
        SharedPrefsData.putString(app, keyDistName(languageId), n, Constants.PREF_NAME);
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
     * Splash / server write: store name for the language id the server just applied.
     * Rejects cross-language / wrong-script labels; clears active selection instead.
     */
    public static void saveFromServer(
            @Nullable Context context,
            int languageId,
            int distId,
            @Nullable String name) {
        if (context == null) {
            return;
        }
        Context app = context.getApplicationContext();
        int tagLang = languageId > 0
                ? languageId
                : SharedPrefsData.getInt(app, Constants.LanguageID, Constants.PREF_NAME);
        if (tagLang <= 0) {
            return;
        }
        if (isUsable(name) && isClearScriptConflict(name, tagLang)) {
            // Server returned e.g. Hindi भिवानी while language is Telugu — do not apply.
            // Only clear active prefs when they are also wrong for this language.
            int nameLang = SharedPrefsData.getInt(
                    app, Constants.Selected_Distric_Name_LangId, Constants.PREF_NAME);
            String d1 = SharedPrefsData.getString(
                    app, Constants.Selected_Distric_Name1, Constants.PREF_NAME);
            String d0 = SharedPrefsData.getString(
                    app, Constants.Selected_Distric_Name, Constants.PREF_NAME);
            String active = isUsable(d1) ? d1 : d0;
            boolean activeBad = (nameLang > 0 && nameLang != tagLang)
                    || (isUsable(active) && isClearScriptConflict(active, tagLang));
            if (activeBad || !isUsable(active)) {
                clearActiveSelection(app);
            }
            return;
        }
        if (!isUsable(name) && distId <= 0) {
            return;
        }
        int stateId = SharedPrefsData.getInt(app, Constants.StateID, Constants.PREF_NAME);
        if (distId > 0 && !isUsable(name)) {
            // Id-only write: still require it to exist under this language when cache is warm.
            if (!districtBelongsToLanguage(app, tagLang, distId, stateId)) {
                // Cache miss is common on cold start — keep id but do not invent a label.
                SharedPrefsData.putInt(app, Constants.Selected_Distric_Id, distId, Constants.PREF_NAME);
                SharedPrefsData.putInt(app, keyDistId(tagLang), distId, Constants.PREF_NAME);
                SharedPrefsData.putInt(
                        app, Constants.Selected_Distric_Name_LangId, tagLang, Constants.PREF_NAME);
                return;
            }
        }
        saveSelectedForLanguage(app, tagLang, stateId, distId, name);
    }

    public static int getStoredDistId(@Nullable Context context) {
        if (context == null) {
            return 0;
        }
        return SharedPrefsData.getInt(
                context.getApplicationContext(), Constants.Selected_Distric_Id, Constants.PREF_NAME);
    }

    /** Snapshot current active geography into the per-language slot (before language switch). */
    public static void snapshotActiveToLanguageSlot(@Nullable Context context) {
        if (context == null) {
            return;
        }
        Context app = context.getApplicationContext();
        // Prefer the language the label was tagged with — LanguageID may already
        // have been switched by the caller before onLanguageChanged runs.
        int nameLang = SharedPrefsData.getInt(
                app, Constants.Selected_Distric_Name_LangId, Constants.PREF_NAME);
        int uiLang = SharedPrefsData.getInt(app, Constants.LanguageID, Constants.PREF_NAME);
        int lang = nameLang > 0 ? nameLang : uiLang;
        if (lang <= 0) {
            return;
        }
        int distId = getStoredDistId(app);
        int stateId = SharedPrefsData.getInt(app, Constants.StateID, Constants.PREF_NAME);
        String name = SharedPrefsData.getString(
                app, Constants.Selected_Distric_Name1, Constants.PREF_NAME);
        if (!isUsable(name)) {
            name = SharedPrefsData.getString(
                    app, Constants.Selected_Distric_Name, Constants.PREF_NAME);
        }
        if (distId <= 0 && !isUsable(name)) {
            return;
        }
        if (isUsable(name) && isClearScriptConflict(name, lang)) {
            return;
        }
        if (distId > 0) {
            SharedPrefsData.putInt(app, keyDistId(lang), distId, Constants.PREF_NAME);
        }
        if (stateId > 0) {
            SharedPrefsData.putInt(app, keyStateId(lang), stateId, Constants.PREF_NAME);
        }
        if (isUsable(name)) {
            SharedPrefsData.putString(app, keyDistName(lang), name.trim(), Constants.PREF_NAME);
        }
    }

    /** Clear active (global) district/state selection without wiping per-language slots. */
    public static void clearActiveSelection(@Nullable Context context) {
        if (context == null) {
            return;
        }
        Context app = context.getApplicationContext();
        try {
            SharedPrefsData.putInt(app, Constants.StateID, 0, Constants.PREF_NAME);
            SharedPrefsData.putInt(app, Constants.Selected_Distric_Id, 0, Constants.PREF_NAME);
            SharedPrefsData.putString(app, Constants.Selected_Distric_Name, "", Constants.PREF_NAME);
            SharedPrefsData.putString(app, Constants.Selected_Distric_Name1, "", Constants.PREF_NAME);
            SharedPrefsData.putInt(app, Constants.Selected_Distric_Name_LangId, 0, Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
    }

    /**
     * After language change / login / profile: drop cross-language district, or restore
     * the last valid district stored for the current language.
     *
     * @return true when active selection was cleared or replaced
     */
    public static boolean ensureSelectedMatchesLanguage(@Nullable Context context) {
        if (context == null) {
            return false;
        }
        Context app = context.getApplicationContext();
        int lang = SharedPrefsData.getInt(app, Constants.LanguageID, Constants.PREF_NAME);
        if (lang <= 0) {
            return false;
        }
        int distId = getStoredDistId(app);
        int stateId = SharedPrefsData.getInt(app, Constants.StateID, Constants.PREF_NAME);
        int nameLang = SharedPrefsData.getInt(
                app, Constants.Selected_Distric_Name_LangId, Constants.PREF_NAME);
        String d1 = SharedPrefsData.getString(app, Constants.Selected_Distric_Name1, Constants.PREF_NAME);
        String d0 = SharedPrefsData.getString(app, Constants.Selected_Distric_Name, Constants.PREF_NAME);
        String activeName = isUsable(d1) ? d1 : d0;

        boolean scriptBad = isUsable(activeName) && isClearScriptConflict(activeName, lang);
        boolean tagBad = nameLang > 0 && nameLang != lang;
        boolean idBad = false;
        if (distId > 0) {
            // Only treat as bad when cache positively proves the id is not in this language.
            Boolean belongs = districtBelongsToLanguageStrict(app, lang, distId, stateId);
            if (Boolean.FALSE.equals(belongs)) {
                idBad = true;
            }
        }

        if (!scriptBad && !tagBad && !idBad) {
            // Soft-upgrade Latin → native when possible; keep selection.
            if (distId > 0 && needsNativeScriptUpgrade(activeName, lang)) {
                String fromCache = lookupLocalizedNameFromCache(app, lang);
                if (isUsable(fromCache)) {
                    saveSelectedForLanguage(app, lang, stateId, distId, fromCache);
                    return true;
                }
            }
            // Keep per-language snapshot fresh.
            if (distId > 0 || isUsable(activeName)) {
                snapshotActiveToLanguageSlot(app);
            }
            return false;
        }

        // Invalid for current language — try restore from per-language slot.
        int savedDist = SharedPrefsData.getInt(app, keyDistId(lang), Constants.PREF_NAME);
        int savedState = SharedPrefsData.getInt(app, keyStateId(lang), Constants.PREF_NAME);
        String savedName = SharedPrefsData.getString(app, keyDistName(lang), Constants.PREF_NAME);
        if (savedDist > 0
                && isUsable(savedName)
                && !isClearScriptConflict(savedName, lang)) {
            clearActiveSelection(app);
            saveSelectedForLanguage(app, lang, savedState, savedDist, savedName);
            return true;
        }
        if (isUsable(savedName)
                && !isClearScriptConflict(savedName, lang)
                && !needsNativeScriptUpgrade(savedName, lang)) {
            clearActiveSelection(app);
            saveSelectedForLanguage(app, lang, savedState, savedDist, savedName);
            return true;
        }

        clearActiveSelection(app);
        return true;
    }

    /**
     * Resolve the district pill / header label for the <b>current</b> app language.
     * Never returns a clear Indic-script conflict (e.g. Devanagari under Telugu).
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

        // Auto-heal once when prefs clearly conflict with LanguageID.
        if ((isUsable(d1) && isClearScriptConflict(d1, lang))
                || (isUsable(d0) && isClearScriptConflict(d0, lang))
                || (nameLang > 0 && nameLang != lang && (isUsable(d1) || isUsable(d0)))) {
            ensureSelectedMatchesLanguage(app);
            d1 = SharedPrefsData.getString(app, Constants.Selected_Distric_Name1, Constants.PREF_NAME);
            d0 = SharedPrefsData.getString(app, Constants.Selected_Distric_Name, Constants.PREF_NAME);
            nameLang = SharedPrefsData.getInt(
                    app, Constants.Selected_Distric_Name_LangId, Constants.PREF_NAME);
        }

        String match = firstCompatible(lang, nameLang, d1, d0);
        if (isUsable(match) && !needsNativeScriptUpgrade(match, lang)
                && !isClearScriptConflict(match, lang)) {
            return match.trim();
        }

        // Prefs are Latin-only under an Indic language — look up localized name by id.
        String fromCache = lookupLocalizedNameFromCache(app, lang);
        if (isUsable(fromCache) && !isClearScriptConflict(fromCache, lang)) {
            String priorLatin = null;
            if (isLatinOrDigitsOnlySafe(match)) {
                priorLatin = match.trim();
            } else if (isLatinOrDigitsOnlySafe(d1)) {
                priorLatin = d1.trim();
            } else if (isLatinOrDigitsOnlySafe(d0)) {
                priorLatin = d0.trim();
            }
            int distId = getStoredDistId(app);
            int stateId = SharedPrefsData.getInt(app, Constants.StateID, Constants.PREF_NAME);
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
            SharedPrefsData.putString(app, keyDistName(lang), fromCache.trim(), Constants.PREF_NAME);
            if (distId > 0) {
                SharedPrefsData.putInt(app, keyDistId(lang), distId, Constants.PREF_NAME);
            }
            if (stateId > 0) {
                SharedPrefsData.putInt(app, keyStateId(lang), stateId, Constants.PREF_NAME);
            }
            return fromCache.trim();
        }

        if (isUsable(d1) && !isClearScriptConflict(d1, lang)
                && !needsNativeScriptUpgrade(d1, lang)) {
            return d1.trim();
        }
        if (isUsable(d0) && !isClearScriptConflict(d0, lang)
                && !needsNativeScriptUpgrade(d0, lang)) {
            return d0.trim();
        }
        // Latin-only fallback (never foreign Indic script).
        if (isUsable(match) && !isClearScriptConflict(match, lang)) {
            return match.trim();
        }
        if (isUsable(d1) && isLatinOrDigitsOnlySafe(d1)) {
            return d1.trim();
        }
        if (isUsable(d0) && isLatinOrDigitsOnlySafe(d0)) {
            return d0.trim();
        }
        // Prefer per-language saved name over blank.
        String saved = SharedPrefsData.getString(app, keyDistName(lang), Constants.PREF_NAME);
        if (isUsable(saved) && !isClearScriptConflict(saved, lang)) {
            return saved.trim();
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
     * Look up a district display name for {@code distId} in the current language
     * cache (optionally scoped by {@code stateId}). Prefers native-script names.
     */
    @Nullable
    public static String lookupLocalizedNameForDistrict(
            @Nullable Context context, int languageId, int distId, int stateId) {
        if (context == null || languageId <= 0 || distId <= 0) {
            return null;
        }
        Context app = context.getApplicationContext();
        int sid = stateId;
        if (sid <= 0) {
            sid = SelectionListCache.findStateIdForDistrict(app, languageId, distId);
        }
        if (sid <= 0) {
            sid = SharedPrefsData.getInt(app, Constants.StateID, Constants.PREF_NAME);
        }
        if (sid <= 0) {
            return null;
        }
        try {
            List<DistricsModel.Datum> list =
                    SelectionListCache.loadDistricts(app, languageId, sid);
            if (list == null || list.isEmpty()) {
                return null;
            }
            String latinFallback = null;
            for (DistricsModel.Datum d : list) {
                if (d == null || d.getId() == null || d.getId() != distId) {
                    continue;
                }
                if (!isUsable(d.getName())) {
                    continue;
                }
                String name = d.getName().trim();
                if (isClearScriptConflict(name, languageId)) {
                    continue;
                }
                if (!needsNativeScriptUpgrade(name, languageId)) {
                    return name;
                }
                if (latinFallback == null) {
                    latinFallback = name;
                }
            }
            return latinFallback;
        } catch (Exception ignored) {
        }
        return null;
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
        return lookupLocalizedNameForDistrict(context, languageId, distId, stateId);
    }

    /**
     * @return true if cache lists the district under language; false if cache is warm
     *     and does not contain it; true on cache-miss (unknown) to avoid false clears.
     */
    private static boolean districtBelongsToLanguage(
            @Nullable Context context, int languageId, int distId, int stateId) {
        Boolean strict = districtBelongsToLanguageStrict(context, languageId, distId, stateId);
        return strict == null || strict;
    }

    /**
     * @return {@link Boolean#TRUE} if found, {@link Boolean#FALSE} if warm cache proves
     *     absence, {@code null} if caches are empty / unknown.
     */
    @Nullable
    private static Boolean districtBelongsToLanguageStrict(
            @Nullable Context context, int languageId, int distId, int stateId) {
        if (context == null || languageId <= 0 || distId <= 0) {
            return null;
        }
        Context app = context.getApplicationContext();
        int found = SelectionListCache.findStateIdForDistrict(app, languageId, distId);
        if (found > 0) {
            return Boolean.TRUE;
        }
        // Warm states list but no district → definitely not in this language.
        try {
            List<?> states = SelectionListCache.loadStates(app, languageId);
            if (states != null && !states.isEmpty()) {
                if (stateId > 0) {
                    List<DistricsModel.Datum> list =
                            SelectionListCache.loadDistricts(app, languageId, stateId);
                    if (list != null && !list.isEmpty()) {
                        return Boolean.FALSE;
                    }
                }
                // States warm but district lists not all loaded — unknown.
                return null;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /** True when label is clearly another Indic script than the selected language. */
    public static boolean isClearScriptConflict(@Nullable String label, int languageId) {
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
        ensureSelectedMatchesLanguage(context);
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
        if (t.isEmpty() || "null".equalsIgnoreCase(t) || "N/A".equalsIgnoreCase(t)) {
            return false;
        }
        // Country placeholders are not districts — never keep/display as selection.
        if ("India".equalsIgnoreCase(t)
                || "Bharat".equalsIgnoreCase(t)
                || "Bharath".equalsIgnoreCase(t)
                || "भारत".equals(t)) {
            return false;
        }
        return true;
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
