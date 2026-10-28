package com.anyNews.anynews.Utils;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.anyNews.anynews.Services.Config;
import com.anyNews.anynews.Ui.BirthdayPosterPreviewView;

/**
 * Caches and reads Profile hub / More / Home footer / Settings panel admin flags from splash.
 *
 * <p>Defaults match product intent (categories in profile only, header icon OFF —
 * bottom footer Profile tab is the entry; Status Videos ON; footer slots all ON
 * except legacy Categories-in-footer). Settings local stories column defaults OFF
 * until admin enables. Null server fields leave the cache untouched so old
 * servers never wipe user session prefs mid-flight.
 */
public final class ProfileHubConfigHelper {

    /** Compile-time / pre-splash default for Contact Us. */
    public static final String DEFAULT_CONTACT_EMAIL = Config.LINK_CONTACT_EMAIL;

    private ProfileHubConfigHelper() {
    }

    public static void saveFromSplash(
            @Nullable Context context,
            @Nullable Boolean categoriesInFooter,
            @Nullable Boolean showHeaderProfileIcon,
            @Nullable Boolean enableStatusVideos,
            @Nullable Boolean showBookmarks,
            @Nullable Boolean showReferral,
            @Nullable Boolean showSettingsRow,
            @Nullable Integer headerIconSizeDp,
            @Nullable Integer categoryIconSizeDp,
            @Nullable Integer menuTextSizeSp) {
        saveFromSplash(context, categoriesInFooter, showHeaderProfileIcon, enableStatusVideos,
                showBookmarks, showReferral, showSettingsRow, headerIconSizeDp,
                categoryIconSizeDp, menuTextSizeSp, null,
                null, null, null, null, null,
                null, null, null, null);
    }

    public static void saveFromSplash(
            @Nullable Context context,
            @Nullable Boolean categoriesInFooter,
            @Nullable Boolean showHeaderProfileIcon,
            @Nullable Boolean enableStatusVideos,
            @Nullable Boolean showBookmarks,
            @Nullable Boolean showReferral,
            @Nullable Boolean showSettingsRow,
            @Nullable Integer headerIconSizeDp,
            @Nullable Integer categoryIconSizeDp,
            @Nullable Integer menuTextSizeSp,
            @Nullable Integer footerPlusIconSizeDp) {
        saveFromSplash(context, categoriesInFooter, showHeaderProfileIcon, enableStatusVideos,
                showBookmarks, showReferral, showSettingsRow, headerIconSizeDp,
                categoryIconSizeDp, menuTextSizeSp, footerPlusIconSizeDp,
                null, null, null, null, null,
                null, null, null, null);
    }

    public static void saveFromSplash(
            @Nullable Context context,
            @Nullable Boolean categoriesInFooter,
            @Nullable Boolean showHeaderProfileIcon,
            @Nullable Boolean enableStatusVideos,
            @Nullable Boolean showBookmarks,
            @Nullable Boolean showReferral,
            @Nullable Boolean showSettingsRow,
            @Nullable Integer headerIconSizeDp,
            @Nullable Integer categoryIconSizeDp,
            @Nullable Integer menuTextSizeSp,
            @Nullable Integer footerPlusIconSizeDp,
            @Nullable Boolean footerShowReload,
            @Nullable Boolean footerShowSpecial,
            @Nullable Boolean footerShowPlus,
            @Nullable Boolean footerShowProfile,
            @Nullable Boolean footerShowBookmarks) {
        saveFromSplash(context, categoriesInFooter, showHeaderProfileIcon, enableStatusVideos,
                showBookmarks, showReferral, showSettingsRow, headerIconSizeDp,
                categoryIconSizeDp, menuTextSizeSp, footerPlusIconSizeDp,
                footerShowReload, footerShowSpecial, footerShowPlus,
                footerShowProfile, footerShowBookmarks,
                null, null, null, null);
    }

    public static void saveFromSplash(
            @Nullable Context context,
            @Nullable Boolean categoriesInFooter,
            @Nullable Boolean showHeaderProfileIcon,
            @Nullable Boolean enableStatusVideos,
            @Nullable Boolean showBookmarks,
            @Nullable Boolean showReferral,
            @Nullable Boolean showSettingsRow,
            @Nullable Integer headerIconSizeDp,
            @Nullable Integer categoryIconSizeDp,
            @Nullable Integer menuTextSizeSp,
            @Nullable Integer footerPlusIconSizeDp,
            @Nullable Boolean footerShowReload,
            @Nullable Boolean footerShowSpecial,
            @Nullable Boolean footerShowPlus,
            @Nullable Boolean footerShowProfile,
            @Nullable Boolean footerShowBookmarks,
            @Nullable Boolean settingsShowNotifications,
            @Nullable Boolean settingsShowVideoAutoplay,
            @Nullable Boolean settingsShowNightMode,
            @Nullable Boolean settingsShowLocalStories) {
        if (context == null) return;
        Context app = context.getApplicationContext();
        saveBool(app, Constants.CATEGORIES_IN_FOOTER, categoriesInFooter);
        saveBool(app, Constants.SHOW_HEADER_PROFILE_ICON, showHeaderProfileIcon);
        saveBool(app, Constants.ENABLE_STATUS_VIDEOS, enableStatusVideos);
        saveBool(app, Constants.PROFILE_SHOW_BOOKMARKS, showBookmarks);
        saveBool(app, Constants.PROFILE_SHOW_REFERRAL, showReferral);
        saveBool(app, Constants.PROFILE_SHOW_SETTINGS_ROW, showSettingsRow);
        saveBool(app, Constants.FOOTER_SHOW_RELOAD, footerShowReload);
        saveBool(app, Constants.FOOTER_SHOW_SPECIAL, footerShowSpecial);
        saveBool(app, Constants.FOOTER_SHOW_PLUS, footerShowPlus);
        saveBool(app, Constants.FOOTER_SHOW_PROFILE, footerShowProfile);
        saveBool(app, Constants.FOOTER_SHOW_BOOKMARKS, footerShowBookmarks);
        saveBool(app, Constants.SETTINGS_SHOW_NOTIFICATIONS, settingsShowNotifications);
        saveBool(app, Constants.SETTINGS_SHOW_VIDEO_AUTOPLAY, settingsShowVideoAutoplay);
        saveBool(app, Constants.SETTINGS_SHOW_NIGHT_MODE, settingsShowNightMode);
        saveBool(app, Constants.SETTINGS_SHOW_LOCAL_STORIES, settingsShowLocalStories);
        if (headerIconSizeDp != null) {
            saveInt(app, Constants.PROFILE_HEADER_ICON_SIZE_DP, clamp(headerIconSizeDp, 24, 56));
        }
        if (categoryIconSizeDp != null) {
            saveInt(app, Constants.PROFILE_CATEGORY_ICON_SIZE_DP, clamp(categoryIconSizeDp, 28, 72));
        }
        if (menuTextSizeSp != null) {
            saveInt(app, Constants.PROFILE_MENU_TEXT_SIZE_SP, clamp(menuTextSizeSp, 11, 22));
        }
        if (footerPlusIconSizeDp != null) {
            // Default compile size is 42dp; admin may raise up to 72 (e.g. 56).
            saveInt(app, Constants.FOOTER_PLUS_ICON_SIZE_DP, clamp(footerPlusIconSizeDp, 32, 72));
        }
    }

    /** Default false — Categories on profile hub only. */
    public static boolean isCategoriesInFooter(@Nullable Context context) {
        return readBool(context, Constants.CATEGORIES_IN_FOOTER, false);
    }

    /** Default false — profile opens from bottom footer tab, not a top-left header chip. */
    public static boolean isHeaderProfileIconVisible(@Nullable Context context) {
        return readBool(context, Constants.SHOW_HEADER_PROFILE_ICON, false);
    }

    /** Default true. */
    public static boolean isStatusVideosEnabled(@Nullable Context context) {
        return readBool(context, Constants.ENABLE_STATUS_VIDEOS, true);
    }

    /**
     * Become a Reporter hiring flag from splash ({@code enable_become_a_reporter}).
     * When false the Profile row still shows for matching audience (except reporters);
     * tap shows the localized not-hiring message after login instead of the form.
     */
    public static void saveBecomeAReporterEnabled(
            @Nullable Context context, @Nullable Boolean enabled) {
        if (context == null || enabled == null) return;
        saveBool(context.getApplicationContext(),
                Constants.ENABLE_BECOME_A_REPORTER, enabled);
    }

    public static boolean isBecomeAReporterEnabled(@Nullable Context context) {
        return readBool(context, Constants.ENABLE_BECOME_A_REPORTER, true);
    }

    /**
     * Persist hiring-ON banner messages from splash JSON object/map.
     * Null leaves cache; empty object still overwrites so admin clears work.
     */
    public static void saveHiringMessages(
            @Nullable Context context, @Nullable Object raw) {
        saveJsonMapPref(context, Constants.BECOME_A_REPORTER_HIRING_MESSAGES, raw);
    }

    /**
     * Persist audience id lists (language / state / district) as CSV.
     * Null leaves cache; empty list clears to "" (= all).
     */
    public static void saveBecomeReporterTargeting(
            @Nullable Context context,
            @Nullable Object languageIds,
            @Nullable Object stateIds,
            @Nullable Object distIds) {
        if (context == null) return;
        Context app = context.getApplicationContext();
        if (languageIds != null) {
            SharedPrefsData.putString(app, Constants.BECOME_A_REPORTER_LANGUAGE_IDS,
                    idListToCsv(languageIds), Constants.PREF_NAME);
        }
        if (stateIds != null) {
            SharedPrefsData.putString(app, Constants.BECOME_A_REPORTER_STATE_IDS,
                    idListToCsv(stateIds), Constants.PREF_NAME);
        }
        if (distIds != null) {
            SharedPrefsData.putString(app, Constants.BECOME_A_REPORTER_DIST_IDS,
                    idListToCsv(distIds), Constants.PREF_NAME);
        }
    }

    /**
     * Local audience filter using splash-cached targeting + current lang/state/district.
     * Empty lists = unrestricted. AND across non-empty dimensions.
     * Does not check hiring ON/OFF — callers combine with {@link #isBecomeAReporterEnabled}.
     */
    public static boolean matchesBecomeReporterAudience(@Nullable Context context) {
        if (context == null) return true;
        Context app = context.getApplicationContext();
        int langId = 0;
        int stateId = 0;
        int distId = 0;
        try {
            langId = SharedPrefsData.getInt(app, Constants.LanguageID, Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
        try {
            stateId = SharedPrefsData.getInt(app, Constants.StateID, Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
        try {
            distId = SharedPrefsData.getInt(app, Constants.Selected_Distric_Id, Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
        if (!csvContainsOrEmpty(app, Constants.BECOME_A_REPORTER_LANGUAGE_IDS, langId)) {
            return false;
        }
        if (!csvContainsOrEmpty(app, Constants.BECOME_A_REPORTER_STATE_IDS, stateId)) {
            return false;
        }
        if (!csvContainsOrEmpty(app, Constants.BECOME_A_REPORTER_DIST_IDS, distId)) {
            return false;
        }
        return true;
    }

    /**
     * Show Become a Reporter Profile row for non-reporters in the targeted audience.
     * Hiring ON/OFF only changes form vs not-hiring copy — not row visibility.
     */
    public static boolean shouldShowBecomeAReporterEntry(
            @Nullable Context context, boolean isReporter) {
        if (isReporter) return false;
        return matchesBecomeReporterAudience(context);
    }

    @NonNull
    public static String getHiringMessage(@Nullable Context context) {
        if (context == null) return "";
        int langId = 0;
        try {
            langId = SharedPrefsData.getInt(
                    context.getApplicationContext(),
                    Constants.LanguageID,
                    Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
        return getHiringMessage(context, langId);
    }

    @NonNull
    public static String getHiringMessage(@Nullable Context context, int languageId) {
        if (context == null) return "";
        try {
            String raw = SharedPrefsData.getString(
                    context.getApplicationContext(),
                    Constants.BECOME_A_REPORTER_HIRING_MESSAGES,
                    Constants.PREF_NAME);
            if (TextUtils.isEmpty(raw)) return "";
            org.json.JSONObject map = new org.json.JSONObject(raw);
            String byId = map.optString(String.valueOf(languageId), "").trim();
            if (!byId.isEmpty()) return byId;
            String en = map.optString("en", "").trim();
            if (!en.isEmpty()) return en;
            String zero = map.optString("0", "").trim();
            if (!zero.isEmpty()) return zero;
        } catch (Exception ignored) {
        }
        return "";
    }

    /**
     * Birthday Special Profile hub row from splash ({@code enable_birthday_special}).
     * When false the row is hidden entirely.
     */
    public static void saveBirthdaySpecialEnabled(
            @Nullable Context context, @Nullable Boolean enabled) {
        if (context == null || enabled == null) return;
        SharedPrefsData.putStringImmediate(
                context.getApplicationContext(),
                Constants.ENABLE_BIRTHDAY_SPECIAL,
                enabled ? "true" : "false",
                Constants.PREF_NAME);
    }

    /**
     * Selected birthday artwork from splash, session, feed poll, or birthday_config.
     * Null id leaves the cache. Empty image URL is stored (built-in artwork).
     */
    public static void saveBirthdayTemplate(
            @Nullable Context context, @Nullable String templateId, @Nullable String imageUrl) {
        if (context == null || templateId == null) return;
        String id = "b".equalsIgnoreCase(templateId.trim()) ? "b" : "a";
        Context app = context.getApplicationContext();
        SharedPrefsData.putStringImmediate(app, Constants.BIRTHDAY_TEMPLATE_ID, id, Constants.PREF_NAME);
        String url = imageUrl == null ? null : imageUrl.trim();
        if (imageUrl != null) {
            SharedPrefsData.putStringImmediate(
                    app, Constants.BIRTHDAY_TEMPLATE_IMAGE_URL, url, Constants.PREF_NAME);
        }
        // Prefetch celebration art so Birthday Special does not flash bundled A.
        if ("b".equals(id) && url != null && !url.isEmpty()) {
            try {
                BirthdayPosterPreviewView.prefetchCelebrationTemplate(app, id, url);
            } catch (Exception ignored) {
            }
        }
    }

    /** Missing or unknown values stay on template A. */
    public static String getBirthdayTemplateId(@Nullable Context context) {
        if (context == null) return "a";
        String v =
                SharedPrefsData.getString(
                        context.getApplicationContext(),
                        Constants.BIRTHDAY_TEMPLATE_ID,
                        Constants.PREF_NAME);
        return "b".equalsIgnoreCase(v == null ? "" : v.trim()) ? "b" : "a";
    }

    public static String getBirthdayTemplateImageUrl(@Nullable Context context) {
        if (context == null) return "";
        String v =
                SharedPrefsData.getString(
                        context.getApplicationContext(),
                        Constants.BIRTHDAY_TEMPLATE_IMAGE_URL,
                        Constants.PREF_NAME);
        return v == null ? "" : v.trim();
    }

    /** Default true — show Birthday Special on Profile hub. */
    public static boolean isBirthdaySpecialEnabled(@Nullable Context context) {
        return readBool(context, Constants.ENABLE_BIRTHDAY_SPECIAL, true);
    }

    /**
     * Persist per-language not-hiring messages from splash JSON object/map.
     * Null leaves cache; empty object still overwrites with "{}" so admin clears work.
     */
    public static void saveNotHiringMessages(
            @Nullable Context context, @Nullable Object raw) {
        saveJsonMapPref(context, Constants.BECOME_A_REPORTER_NOT_HIRING_MESSAGES, raw);
    }

    private static void saveJsonMapPref(
            @Nullable Context context, @NonNull String key, @Nullable Object raw) {
        if (context == null || raw == null) return;
        try {
            String json;
            if (raw instanceof String) {
                json = ((String) raw).trim();
                if (json.isEmpty() || "null".equalsIgnoreCase(json)) return;
            } else if (raw instanceof org.json.JSONObject) {
                json = raw.toString();
            } else if (raw instanceof java.util.Map) {
                json = new org.json.JSONObject((java.util.Map<?, ?>) raw).toString();
            } else {
                json = String.valueOf(raw);
                if (json.isEmpty() || "null".equalsIgnoreCase(json)) return;
                new org.json.JSONObject(json);
            }
            SharedPrefsData.putString(
                    context.getApplicationContext(),
                    key,
                    json,
                    Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
    }

    @NonNull
    private static String idListToCsv(@Nullable Object raw) {
        if (raw == null) return "";
        StringBuilder csv = new StringBuilder();
        try {
            if (raw instanceof org.json.JSONArray) {
                org.json.JSONArray arr = (org.json.JSONArray) raw;
                for (int i = 0; i < arr.length(); i++) {
                    int id = splashNumberFromArray(arr, i);
                    if (id <= 0) continue;
                    if (csv.length() > 0) csv.append(',');
                    csv.append(id);
                }
                return csv.toString();
            }
            if (raw instanceof java.util.Collection) {
                for (Object o : (java.util.Collection<?>) raw) {
                    int id = splashNumber(o);
                    if (id <= 0) continue;
                    if (csv.length() > 0) csv.append(',');
                    csv.append(id);
                }
                return csv.toString();
            }
            if (raw instanceof String) {
                String s = ((String) raw).trim();
                if (s.isEmpty() || "null".equalsIgnoreCase(s)) return "";
                if (s.startsWith("[")) {
                    return idListToCsv(new org.json.JSONArray(s));
                }
                // Already CSV
                return s.replaceAll("[^0-9,]", "");
            }
            // Gson may deliver ArrayList via Object field
            String as = String.valueOf(raw).trim();
            if (as.startsWith("[")) {
                return idListToCsv(new org.json.JSONArray(as));
            }
        } catch (Exception ignored) {
        }
        return csv.toString();
    }

    private static int splashNumber(@Nullable Object o) {
        if (o == null) return 0;
        if (o instanceof Number) return ((Number) o).intValue();
        try {
            return (int) Double.parseDouble(String.valueOf(o).trim());
        } catch (Exception ignored) {
            return 0;
        }
    }

    private static int splashNumberFromArray(@NonNull org.json.JSONArray arr, int index) {
        try {
            return splashNumber(arr.opt(index));
        } catch (Exception ignored) {
            return 0;
        }
    }

    /** Empty CSV = unrestricted; otherwise require positive id membership. */
    private static boolean csvContainsOrEmpty(
            @NonNull Context app, @NonNull String key, int id) {
        try {
            String csv = SharedPrefsData.getString(app, key, Constants.PREF_NAME);
            if (TextUtils.isEmpty(csv)) return true;
            String needle = String.valueOf(id);
            for (String part : csv.split(",")) {
                if (needle.equals(part.trim())) return true;
            }
            return false;
        } catch (Exception ignored) {
            return true;
        }
    }

    /** Default English not-hiring copy when splash/admin has nothing for the language. */
    public static final String DEFAULT_NOT_HIRING_MESSAGE =
            "Currently we are not hiring any reporters, please come back later";

    @NonNull
    public static String getNotHiringMessage(@Nullable Context context) {
        if (context == null) return DEFAULT_NOT_HIRING_MESSAGE;
        int langId = 0;
        try {
            langId = SharedPrefsData.getInt(
                    context.getApplicationContext(),
                    Constants.LanguageID,
                    Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
        return getNotHiringMessage(context, langId);
    }

    @NonNull
    public static String getNotHiringMessage(@Nullable Context context, int languageId) {
        String fallback = builtInNotHiringFallback(languageId);
        if (context == null) return fallback;
        try {
            String raw = SharedPrefsData.getString(
                    context.getApplicationContext(),
                    Constants.BECOME_A_REPORTER_NOT_HIRING_MESSAGES,
                    Constants.PREF_NAME);
            if (TextUtils.isEmpty(raw)) return fallback;
            org.json.JSONObject map = new org.json.JSONObject(raw);
            String byId = map.optString(String.valueOf(languageId), "").trim();
            if (!byId.isEmpty()) return byId;
            String en = map.optString("en", "").trim();
            if (!en.isEmpty()) return en;
            String zero = map.optString("0", "").trim();
            if (!zero.isEmpty()) return zero;
        } catch (Exception ignored) {
        }
        return fallback;
    }

    @NonNull
    private static String builtInNotHiringFallback(int languageId) {
        switch (languageId) {
            case 1:
                return "നിലവിൽ ഞങ്ങൾ റിപ്പോർട്ടർമാരെ നിയമിക്കുന്നില്ല, ദയവായി പിന്നീട് വരുക";
            case 2:
                return "वर्तमान में हम कोई रिपोर्टर नहीं रख रहे हैं, कृपया बाद में आएं";
            case 3:
                return "ప్రస్తుతం మేము రిపోర్టర్లను నియమించడం లేదు, దయచేసి తర్వాత రండి";
            case 4:
                return "தற்போது நாங்கள் ரிப்போர்ட்டர்களை நியமிக்கவில்லை, தயவுசெய்து பின்னர் வாருங்கள்";
            case 5:
                return "ಪ್ರಸ್ತುತ ನಾವು ಯಾವುದೇ ರಿಪೋರ್ಟರ್‌ಗಳನ್ನು ನೇಮಿಸುತ್ತಿಲ್ಲ, ದಯವಿಟ್ಟು ನಂತರ ಬನ್ನಿ";
            case 6:
                return "ବର୍ତ୍ତମାନ ଆମେ କୌଣସି ରିପୋର୍ଟର୍ ନିଯୁକ୍ତ କରୁନାହୁଁ, ଦୟାକରି ପରେ ଆସନ୍ତୁ";
            default:
                return DEFAULT_NOT_HIRING_MESSAGE;
        }
    }

    public static boolean isBookmarksVisible(@Nullable Context context) {
        return readBool(context, Constants.PROFILE_SHOW_BOOKMARKS, true);
    }

    public static boolean isReferralVisible(@Nullable Context context) {
        return readBool(context, Constants.PROFILE_SHOW_REFERRAL, true);
    }

    public static boolean isSettingsRowVisible(@Nullable Context context) {
        return readBool(context, Constants.PROFILE_SHOW_SETTINGS_ROW, true);
    }

    /** Home footer Reload tab. Default true. */
    public static boolean isFooterShowReload(@Nullable Context context) {
        return readBool(context, Constants.FOOTER_SHOW_RELOAD, true);
    }

    /** Home footer Special tab. Default true. */
    public static boolean isFooterShowSpecial(@Nullable Context context) {
        return readBool(context, Constants.FOOTER_SHOW_SPECIAL, true);
    }

    /**
     * Home footer + / WhatsApp FAB overlays.
     * Product: always hidden — create-post lives in Profile reporter desk.
     * Panchangam tab uses {@link #isFooterShowCalendar} (product-hidden); Daily Updates uses
     * {@link #isFooterShowVideos}. Admin {@code footer_show_plus} is ignored.
     */
    public static boolean isFooterShowPlus(@Nullable Context context) {
        return false;
    }

    /**
     * Home footer Panchangam tab ({@code navigation_stricker}).
     * Product: always hidden — Panchangam opens from Daily Updates only.
     * Menu id stays for EXTRA_FOOTER_TAB / legacy deep links; slot is GONE + weight 0.
     */
    public static boolean isFooterShowCalendar(@Nullable Context context) {
        return false;
    }

    /** Home footer Profile tab. Default true. */
    public static boolean isFooterShowProfile(@Nullable Context context) {
        return readBool(context, Constants.FOOTER_SHOW_PROFILE, true);
    }

    /**
     * Home footer Daily Updates tab ({@code navigation_status}; legacy videos flag).
     * Default true. Optional pref {@code footer_show_videos}; unread keys keep default on.
     */
    public static boolean isFooterShowVideos(@Nullable Context context) {
        return readBool(context, Constants.FOOTER_SHOW_VIDEOS, true);
    }

    /**
     * Home footer Bookmarks tab.
     * Product: always hidden — Bookmarks live in the top News|Bookmarks pill
     * (online + offline). Admin footer_show_bookmarks is ignored.
     */
    public static boolean isFooterShowBookmarks(@Nullable Context context) {
        return false;
    }

    /** Settings column: Notifications. Default true. */
    public static boolean isSettingsShowNotifications(@Nullable Context context) {
        return readBool(context, Constants.SETTINGS_SHOW_NOTIFICATIONS, true);
    }

    /**
     * Legacy splash flag for Settings Video Autoplay column (UI removed; footer Video
     * tab replaced by Breaking). Still cached from splash for old server payloads.
     */
    public static boolean isSettingsShowVideoAutoplay(@Nullable Context context) {
        return readBool(context, Constants.SETTINGS_SHOW_VIDEO_AUTOPLAY, false);
    }

    /** Settings column: Night Mode. Default true. */
    public static boolean isSettingsShowNightMode(@Nullable Context context) {
        return readBool(context, Constants.SETTINGS_SHOW_NIGHT_MODE, true);
    }

    /** Settings column: Local Stories. Default false (feature incomplete). */
    public static boolean isSettingsShowLocalStories(@Nullable Context context) {
        return readBool(context, Constants.SETTINGS_SHOW_LOCAL_STORIES, false);
    }

    public static int getHeaderIconSizeDp(@Nullable Context context) {
        return readInt(context, Constants.PROFILE_HEADER_ICON_SIZE_DP, 36);
    }

    public static int getCategoryIconSizeDp(@Nullable Context context) {
        return readInt(context, Constants.PROFILE_CATEGORY_ICON_SIZE_DP, 48);
    }

    public static int getMenuTextSizeSp(@Nullable Context context) {
        return readInt(context, Constants.PROFILE_MENU_TEXT_SIZE_SP, 15);
    }

    /**
     * Home footer center "+" FAB diameter in dp.
     * Default 42 matches {@code activity_home_page.xml} fabCustomSize.
     */
    public static int getFooterPlusIconSizeDp(@Nullable Context context) {
        return readInt(context, Constants.FOOTER_PLUS_ICON_SIZE_DP, 42);
    }

    /**
     * Persists admin contact email from splash when present and non-empty.
     * Null/blank from old servers leaves the cached value (or compile default).
     */
    public static void saveContactEmail(@Nullable Context context, @Nullable String email) {
        if (context == null || TextUtils.isEmpty(email)) return;
        String trimmed = email.trim();
        if (!isPlausibleEmail(trimmed)) return;
        SharedPrefsData.putString(
                context.getApplicationContext(),
                Constants.CONTACT_EMAIL,
                trimmed,
                Constants.PREF_NAME);
    }

    /**
     * Full jatakam WhatsApp digits from splash. Blank/null leaves cache.
     * Passing non-empty invalid sets empty (disables CTA).
     */
    public static void saveFullJatakamWhatsapp(@Nullable Context context, @Nullable String raw) {
        if (context == null || raw == null) return;
        String digits = raw.replaceAll("[^0-9]", "");
        SharedPrefsData.putString(
                context.getApplicationContext(),
                Constants.FULL_JATAKAM_WHATSAPP,
                digits.length() >= 10 ? digits : "",
                Constants.PREF_NAME);
    }

    @NonNull
    public static String getFullJatakamWhatsapp(@Nullable Context context) {
        if (context == null) return "";
        try {
            String v = SharedPrefsData.getString(
                    context.getApplicationContext(),
                    Constants.FULL_JATAKAM_WHATSAPP,
                    Constants.PREF_NAME);
            if (TextUtils.isEmpty(v)) return "";
            String digits = v.replaceAll("[^0-9]", "");
            return digits.length() >= 10 ? digits : "";
        } catch (Exception ignored) {
            return "";
        }
    }

    /**
     * Default rashi (0-based) from splash/admin. Blank leaves cache.
     * Accepts 1–12, 0–11, or mesha…meena / scorpio aliases.
     */
    public static void saveDefaultRashiId(@Nullable Context context, @Nullable Object raw) {
        if (context == null || raw == null) return;
        String s = String.valueOf(raw).trim();
        if (s.isEmpty() || "null".equalsIgnoreCase(s)) return;
        int id = com.anyNews.anynews.panchang.RaasiContent.parseDefaultId(raw, 7);
        SharedPrefsData.putInt(
                context.getApplicationContext(),
                Constants.DEFAULT_RASHI_ID,
                id,
                Constants.PREF_NAME);
        SharedPrefsData.putBool(
                context.getApplicationContext(),
                Constants.DEFAULT_RASHI_ID_SET,
                true,
                Constants.PREF_NAME);
    }

    /** 0-based rashi id; {@code fallback0} when admin never sent a value. */
    public static int getDefaultRashiId(@Nullable Context context, int fallback0) {
        if (context == null) {
            return com.anyNews.anynews.panchang.RaasiContent.clampId(fallback0);
        }
        try {
            if (!SharedPrefsData.getBool(
                    context.getApplicationContext(),
                    Constants.DEFAULT_RASHI_ID_SET,
                    Constants.PREF_NAME)) {
                return com.anyNews.anynews.panchang.RaasiContent.clampId(fallback0);
            }
            return com.anyNews.anynews.panchang.RaasiContent.clampId(
                    SharedPrefsData.getInt(
                            context.getApplicationContext(),
                            Constants.DEFAULT_RASHI_ID,
                            Constants.PREF_NAME));
        } catch (Exception ignored) {
            return com.anyNews.anynews.panchang.RaasiContent.clampId(fallback0);
        }
    }

    /**
     * Persist admin calendar engine selection from splash.
     * Null/blank leaves cache (defaults to ProKerala / Way2-like).
     */
    public static void saveCalendarPanchangSource(@Nullable Context context,
                                                    @Nullable String raw) {
        if (context == null || raw == null) return;
        String s = raw.trim().toLowerCase(java.util.Locale.US).replace('-', '_');
        if (s.isEmpty() || "null".equals(s)) return;
        if ("astronomical".equals(s)
                || "astronomical_hyderabad".equals(s)
                || "engine".equals(s)
                || "formula".equals(s)
                || "local".equals(s)) {
            s = Constants.CALENDAR_SOURCE_ASTRONOMICAL;
        } else {
            s = Constants.CALENDAR_SOURCE_PROKERALA;
        }
        SharedPrefsData.putString(
                context.getApplicationContext(),
                Constants.CALENDAR_PANCHANG_SOURCE,
                s,
                Constants.PREF_NAME);
    }

    /** Prefer ProKerala (Way2-like) unless admin selected astronomical. */
    @NonNull
    public static String getCalendarPanchangSource(@Nullable Context context) {
        if (context == null) return Constants.CALENDAR_SOURCE_PROKERALA;
        try {
            String v = SharedPrefsData.getString(
                    context.getApplicationContext(),
                    Constants.CALENDAR_PANCHANG_SOURCE,
                    Constants.PREF_NAME);
            if (TextUtils.isEmpty(v)) return Constants.CALENDAR_SOURCE_PROKERALA;
            String s = v.trim().toLowerCase(java.util.Locale.US).replace('-', '_');
            if (Constants.CALENDAR_SOURCE_ASTRONOMICAL.equals(s)) {
                return Constants.CALENDAR_SOURCE_ASTRONOMICAL;
            }
            return Constants.CALENDAR_SOURCE_PROKERALA;
        } catch (Exception ignored) {
            return Constants.CALENDAR_SOURCE_PROKERALA;
        }
    }

    public static boolean prefersAstronomicalCalendar(@Nullable Context context) {
        return Constants.CALENDAR_SOURCE_ASTRONOMICAL.equals(
                getCalendarPanchangSource(context));
    }

    /**
     * Contact Us address: splash-delivered pref, else {@link #DEFAULT_CONTACT_EMAIL}.
     */
    @NonNull
    public static String getContactEmail(@Nullable Context context) {
        if (context == null) return DEFAULT_CONTACT_EMAIL;
        try {
            String v = SharedPrefsData.getString(
                    context.getApplicationContext(), Constants.CONTACT_EMAIL, Constants.PREF_NAME);
            if (!TextUtils.isEmpty(v) && isPlausibleEmail(v.trim())) {
                return v.trim();
            }
        } catch (Exception ignored) {
        }
        return DEFAULT_CONTACT_EMAIL;
    }

    private static boolean isPlausibleEmail(@Nullable String email) {
        if (email == null) return false;
        String e = email.trim();
        int at = e.indexOf('@');
        return at > 0 && at < e.length() - 1 && e.indexOf(' ') < 0;
    }

    private static void saveBool(Context app, String key, @Nullable Boolean value) {
        if (value == null) return;
        SharedPrefsData.putString(app, key, value ? "true" : "false", Constants.PREF_NAME);
    }

    private static void saveInt(Context app, String key, int value) {
        SharedPrefsData.putString(app, key, String.valueOf(value), Constants.PREF_NAME);
    }

    private static boolean readBool(@Nullable Context context, String key, boolean fallback) {
        if (context == null) return fallback;
        try {
            String v = SharedPrefsData.getString(context.getApplicationContext(), key, Constants.PREF_NAME);
            if (TextUtils.isEmpty(v)) return fallback;
            return "true".equalsIgnoreCase(v.trim());
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static int readInt(@Nullable Context context, String key, int fallback) {
        if (context == null) return fallback;
        try {
            String v = SharedPrefsData.getString(context.getApplicationContext(), key, Constants.PREF_NAME);
            if (TextUtils.isEmpty(v)) return fallback;
            return Integer.parseInt(v.trim());
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}
