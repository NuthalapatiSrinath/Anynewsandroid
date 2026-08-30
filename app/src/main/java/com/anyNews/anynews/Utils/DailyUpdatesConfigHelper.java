package com.anyNews.anynews.Utils;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Splash {@code daily_updates} — hub kill switches, card art offset,
 * Mana Zilla per-language enable + headline typography, Stock Market.
 */
public final class DailyUpdatesConfigHelper {

    private static final int DEFAULT_CARD_OFFSET_DP = -12;
    private static final int DEFAULT_MANA_X_DP = 28;
    private static final int DEFAULT_MANA_Y_DP = 0;
    private static final int DEFAULT_MANA_SIZE_SP = 11;
    private static final int DEFAULT_STOCK_OFFSET_Y_DP = 0;
    /** Telugu language id — Mana Zilla default ON only for this language. */
    public static final int TELUGU_LANGUAGE_ID = 3;
    /** Telugu desk state ids (autoNewsLocalGeo defaults). */
    public static final int AP_STATE_ID = 2;
    public static final int TG_STATE_ID = 33;

    private DailyUpdatesConfigHelper() {}

    public static void saveFromSplash(@Nullable Context context, @Nullable Object raw) {
        if (context == null || raw == null) return;
        try {
            JSONObject o = toJsonObject(raw);
            if (o == null) return;
            Context app = context.getApplicationContext();
            SharedPrefsData.putString(
                    app, Constants.DAILY_UPDATES_CONFIG_JSON, o.toString(), Constants.PREF_NAME);
            putBool(app, Constants.DU_PANCHANGAM_ENABLED, o.optBoolean("panchangam_enabled", true));
            putBool(app, Constants.DU_MANA_ZILLA_ENABLED, o.optBoolean("mana_zilla_enabled", true));
            putBool(app, Constants.DU_FLASH_NEWS_ENABLED, o.optBoolean("flash_news_enabled", true));
            putBool(app, Constants.DU_STOCK_ENABLED, o.optBoolean("stock_enabled", true));
            SharedPrefsData.putString(
                    app,
                    Constants.DU_STOCK_TITLE,
                    o.optString("stock_title", ""),
                    Constants.PREF_NAME);
            SharedPrefsData.putString(
                    app,
                    Constants.DU_STOCK_SUBTITLE,
                    stripThirdPartyBrand(o.optString("stock_subtitle", "")),
                    Constants.PREF_NAME);
            putIntClamped(app, Constants.DU_STOCK_OFFSET_Y_DP,
                    o.optInt("stock_offset_y_dp", DEFAULT_STOCK_OFFSET_Y_DP), -24, 24);
            putIntClamped(app, Constants.DU_CARD_IMAGE_OFFSET_DP,
                    o.optInt("card_image_offset_dp", DEFAULT_CARD_OFFSET_DP), -40, 40);
            putIntClamped(app, Constants.DU_MANA_HEADLINE_OFFSET_X_DP,
                    o.optInt("mana_headline_offset_x_dp", DEFAULT_MANA_X_DP), -20, 80);
            putIntClamped(app, Constants.DU_MANA_HEADLINE_OFFSET_Y_DP,
                    o.optInt("mana_headline_offset_y_dp", DEFAULT_MANA_Y_DP), -24, 24);
            putIntClamped(app, Constants.DU_MANA_HEADLINE_TEXT_SIZE_SP,
                    o.optInt("mana_headline_text_size_sp", DEFAULT_MANA_SIZE_SP), 9, 16);
            JSONArray langs = o.optJSONArray("mana_zilla_language_ids");
            StringBuilder csv = new StringBuilder();
            if (langs != null && langs.length() > 0) {
                for (int i = 0; i < langs.length(); i++) {
                    int id = langs.optInt(i, 0);
                    if (id <= 0) continue;
                    if (csv.length() > 0) csv.append(',');
                    csv.append(id);
                }
            }
            if (csv.length() == 0) csv.append(TELUGU_LANGUAGE_ID);
            SharedPrefsData.putString(
                    app, Constants.DU_MANA_ZILLA_LANGUAGE_IDS, csv.toString(), Constants.PREF_NAME);
            SharedPrefsData.putBool(app, Constants.DAILY_UPDATES_CONFIG_SET, true, Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
    }

    /** Splash {@code stock_market} summary — real Yahoo prices only. */
    public static void saveStockSummaryFromSplash(@Nullable Context context, @Nullable Object raw) {
        if (context == null) return;
        try {
            Context app = context.getApplicationContext();
            JSONObject o = toJsonObject(raw);
            if (o == null) {
                SharedPrefsData.putString(app, Constants.DU_STOCK_SUMMARY_JSON, "", Constants.PREF_NAME);
                return;
            }
            SharedPrefsData.putString(
                    app, Constants.DU_STOCK_SUMMARY_JSON, o.toString(), Constants.PREF_NAME);
            if (o.has("enabled")) {
                putBool(app, Constants.DU_STOCK_ENABLED, o.optBoolean("enabled", true));
            }
        } catch (Exception ignored) {
        }
    }

    /** Splash {@code flash_news} headlines — Redis/memory pack. */
    public static void saveFlashHeadlinesFromSplash(@Nullable Context context, @Nullable Object raw) {
        if (context == null) return;
        try {
            Context app = context.getApplicationContext();
            JSONObject o = toJsonObject(raw);
            if (o == null) return;
            if (o.has("enabled")) {
                putBool(app, Constants.DU_FLASH_NEWS_ENABLED, o.optBoolean("enabled", true));
            }
            JSONArray arr = o.optJSONArray("headlines");
            SharedPrefsData.putString(
                    app,
                    Constants.DU_FLASH_HEADLINES_JSON,
                    arr != null ? arr.toString() : "[]",
                    Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
    }

    /** Splash {@code local_headlines} — Mana Zilla 2 titles from Redis/memory. */
    public static void saveLocalHeadlinesFromSplash(@Nullable Context context, @Nullable Object raw) {
        if (context == null) return;
        try {
            Context app = context.getApplicationContext();
            JSONObject o = toJsonObject(raw);
            if (o == null) return;
            JSONArray arr = o.optJSONArray("titles");
            SharedPrefsData.putString(
                    app,
                    Constants.DU_LOCAL_HEADLINES_JSON,
                    arr != null ? arr.toString() : "[]",
                    Constants.PREF_NAME);
            putIntClamped(app, Constants.DU_LOCAL_HEADLINES_DIST, o.optInt("dist_id", 0), 0, 999999);
            // Also seed mana title cache for instant DU paint.
            String t1 = "";
            String t2 = "";
            if (arr != null) {
                if (arr.length() > 0) t1 = arr.optString(0, "").trim();
                if (arr.length() > 1) t2 = arr.optString(1, "").trim();
            }
            SharedPrefsData.putString(app, "du_mana_title_1", t1, Constants.PREF_NAME);
            SharedPrefsData.putString(app, "du_mana_title_2", t2, Constants.PREF_NAME);
            int dist = o.optInt("dist_id", 0);
            if (dist > 0) {
                SharedPrefsData.putInt(app, "du_mana_cache_dist", dist, Constants.PREF_NAME);
            }
            SharedPrefsData.putString(
                    app, "du_mana_cache_at", String.valueOf(System.currentTimeMillis()), Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
    }

    @NonNull
    public static String[] flashHeadlines(@Nullable Context context) {
        if (context == null) return new String[0];
        try {
            String s = SharedPrefsData.getString(
                    context.getApplicationContext(),
                    Constants.DU_FLASH_HEADLINES_JSON,
                    Constants.PREF_NAME);
            if (TextUtils.isEmpty(s)) return new String[0];
            JSONArray arr = new JSONArray(s);
            java.util.ArrayList<String> out = new java.util.ArrayList<>();
            for (int i = 0; i < arr.length(); i++) {
                String t = arr.optString(i, "").trim();
                if (!TextUtils.isEmpty(t)) out.add(t);
            }
            return out.toArray(new String[0]);
        } catch (Exception e) {
            return new String[0];
        }
    }

    @NonNull
    public static String[] localHeadlines(@Nullable Context context) {
        if (context == null) return new String[0];
        try {
            String s = SharedPrefsData.getString(
                    context.getApplicationContext(),
                    Constants.DU_LOCAL_HEADLINES_JSON,
                    Constants.PREF_NAME);
            if (TextUtils.isEmpty(s)) return new String[0];
            JSONArray arr = new JSONArray(s);
            java.util.ArrayList<String> out = new java.util.ArrayList<>();
            for (int i = 0; i < arr.length() && out.size() < 2; i++) {
                String t = arr.optString(i, "").trim();
                if (!TextUtils.isEmpty(t)) out.add(t);
            }
            return out.toArray(new String[0]);
        } catch (Exception e) {
            return new String[0];
        }
    }

    public static boolean isPanchangamEnabled(@Nullable Context context) {
        return readBool(context, Constants.DU_PANCHANGAM_ENABLED, true);
    }

    public static boolean isFlashNewsEnabled(@Nullable Context context) {
        return readBool(context, Constants.DU_FLASH_NEWS_ENABLED, true);
    }

    public static boolean isStockEnabled(@Nullable Context context) {
        return readBool(context, Constants.DU_STOCK_ENABLED, true);
    }

    @NonNull
    public static String stockTitle(@Nullable Context context) {
        String t = readString(context, Constants.DU_STOCK_TITLE);
        if (!TextUtils.isEmpty(t)) return t;
        int lang = langId(context);
        switch (lang) {
            case 1: return "സ്റ്റോക്ക് മാർക്കറ്റ്";
            case 2: return "स्टॉक मार्केट";
            case 3: return "స్టాక్ మార్కెట్";
            case 4: return "ஸ்டாக் மார்க்கெட்";
            case 5: return "ಸ್ಟಾಕ್ ಮಾರ್ಕೆಟ್";
            case 6: return "ଷ୍ଟକ୍ ମାର୍କେଟ୍";
            default: return "Stock Market";
        }
    }

    @NonNull
    public static String stockSubtitle(@Nullable Context context) {
        String t = stripThirdPartyBrand(readString(context, Constants.DU_STOCK_SUBTITLE));
        if (!TextUtils.isEmpty(t)) return t;
        int lang = langId(context);
        switch (lang) {
            case 1: return "നിഫ്റ്റി · സെൻസെക്സ് · ഷെയറുകൾ";
            case 2: return "निफ्टी · सेंसेक्स · शेयर";
            case 3: return "నిఫ్టీ · సెన్సెక్స్ · షేర్లు";
            case 4: return "நிஃப்டி · சென்செக்ஸ் · பங்குகள்";
            case 5: return "ನಿಫ್ಟಿ · ಸೆನ್ಸೆಕ್ಸ್ · ಷೇರುಗಳು";
            case 6: return "ନିଫ୍ଟି · ସେନସେକ୍ସ · ସେୟାର";
            default: return "Nifty · Sensex · Shares";
        }
    }

    /**
     * Stock detail screen subtitle — live prices, ANYNEWS branding only.
     * Never shows Yahoo / third-party source labels.
     */
    @NonNull
    public static String stockLiveLabel(@Nullable Context context) {
        String t = stripThirdPartyBrand(readString(context, Constants.DU_STOCK_SUBTITLE));
        int lang = langId(context);
        String liveDefault;
        switch (lang) {
            case 1: liveDefault = "തത്സമയ വിലകൾ"; break;
            case 2: liveDefault = "लाइव कीमतें"; break;
            case 3: liveDefault = "ప్రత్యక్ష ధరలు"; break;
            case 4: liveDefault = "நேரடி விலைகள்"; break;
            case 5: liveDefault = "ನೇರ ಬೆಲೆಗಳು"; break;
            case 6: liveDefault = "ପ୍ରତ୍ୟକ୍ଷ ମୂଲ୍ୟ"; break;
            default: liveDefault = "Live prices"; break;
        }
        if (TextUtils.isEmpty(t)) return liveDefault;
        // Hub row default chrome → prefer live-prices label on detail.
        String hubDefault = stockSubtitle(context);
        if (hubDefault.equals(t) || t.contains("నిఫ్టీ") || t.toLowerCase().contains("nifty")) {
            return liveDefault;
        }
        return t;
    }

    private static int langId(@Nullable Context context) {
        if (context == null) return 0;
        try {
            return SharedPrefsData.getInt(
                    context.getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME);
        } catch (Exception e) {
            return 0;
        }
    }

    /** Strip Yahoo / third-party source prefixes from splash/admin subtitle overrides. */
    @NonNull
    static String stripThirdPartyBrand(@Nullable String raw) {
        if (raw == null) return "";
        String t = raw.trim();
        if (t.isEmpty()) return "";
        t = t.replaceAll("(?i)yahoo\\s*finance\\s*[·•|\\-–—:/]*\\s*", "");
        t = t.replaceAll("(?i)\\byahoo\\s*finance\\b", "");
        t = t.replaceAll("(?i)\\byahoo\\b", "");
        t = t.replaceAll("(?i)\\bvia\\s+yahoo\\b", "");
        t = t.replaceAll("^[\\s·•|\\-–—:/]+", "");
        t = t.replaceAll("[\\s·•|\\-–—:/]+$", "");
        t = t.replaceAll("\\s{2,}", " ").trim();
        return t;
    }

    public static int stockOffsetYDp(@Nullable Context context) {
        return readInt(context, Constants.DU_STOCK_OFFSET_Y_DP, DEFAULT_STOCK_OFFSET_Y_DP, -24, 24);
    }

    @Nullable
    public static JSONObject stockSummaryJson(@Nullable Context context) {
        if (context == null) return null;
        try {
            String s = SharedPrefsData.getString(
                    context.getApplicationContext(),
                    Constants.DU_STOCK_SUMMARY_JSON,
                    Constants.PREF_NAME);
            if (TextUtils.isEmpty(s)) return null;
            return new JSONObject(s);
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isManaZillaEnabled(@Nullable Context context) {
        if (!readBool(context, Constants.DU_MANA_ZILLA_ENABLED, true)) return false;
        int lang = 0;
        try {
            lang = SharedPrefsData.getInt(
                    context.getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
        return isManaZillaLanguageAllowed(context, lang);
    }

    public static boolean isManaZillaLanguageAllowed(@Nullable Context context, int languageId) {
        if (languageId <= 0) return languageId == TELUGU_LANGUAGE_ID;
        String csv = "";
        try {
            csv = SharedPrefsData.getString(
                    context.getApplicationContext(),
                    Constants.DU_MANA_ZILLA_LANGUAGE_IDS,
                    Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
        if (TextUtils.isEmpty(csv)) {
            return languageId == TELUGU_LANGUAGE_ID;
        }
        String[] parts = csv.split(",");
        for (String p : parts) {
            try {
                if (Integer.parseInt(p.trim()) == languageId) return true;
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    public static int cardImageOffsetDp(@Nullable Context context) {
        return readInt(context, Constants.DU_CARD_IMAGE_OFFSET_DP, DEFAULT_CARD_OFFSET_DP, -40, 40);
    }

    public static int manaHeadlineOffsetXDp(@Nullable Context context) {
        return readInt(context, Constants.DU_MANA_HEADLINE_OFFSET_X_DP, DEFAULT_MANA_X_DP, -20, 80);
    }

    public static int manaHeadlineOffsetYDp(@Nullable Context context) {
        return readInt(context, Constants.DU_MANA_HEADLINE_OFFSET_Y_DP, DEFAULT_MANA_Y_DP, -24, 24);
    }

    public static int manaHeadlineTextSizeSp(@Nullable Context context) {
        return readInt(context, Constants.DU_MANA_HEADLINE_TEXT_SIZE_SP, DEFAULT_MANA_SIZE_SP, 9, 16);
    }

    public static int manaZillaBgResId(@Nullable Context context) {
        int stateId = 0;
        try {
            if (context != null) {
                stateId = SharedPrefsData.getInt(
                        context.getApplicationContext(), Constants.StateID, Constants.PREF_NAME);
            }
        } catch (Exception ignored) {
        }
        if (stateId == TG_STATE_ID) {
            return com.anyNews.anynews.R.drawable.du_bg_mana_zilla_tg;
        }
        return com.anyNews.anynews.R.drawable.du_bg_mana_zilla_ap;
    }

    @Nullable
    private static JSONObject toJsonObject(@NonNull Object raw) {
        try {
            if (raw instanceof JSONObject) return (JSONObject) raw;
            if (raw instanceof String) {
                String s = ((String) raw).trim();
                if (s.isEmpty() || "null".equalsIgnoreCase(s)) return null;
                return new JSONObject(s);
            }
            return new JSONObject(String.valueOf(new com.google.gson.Gson().toJson(raw)));
        } catch (Exception e) {
            return null;
        }
    }

    private static void putBool(Context app, String key, boolean v) {
        SharedPrefsData.putString(app, key, v ? "true" : "false", Constants.PREF_NAME);
    }

    private static void putIntClamped(Context app, String key, int v, int min, int max) {
        if (v < min) v = min;
        if (v > max) v = max;
        SharedPrefsData.putInt(app, key, v, Constants.PREF_NAME);
    }

    private static boolean readBool(@Nullable Context context, String key, boolean def) {
        if (context == null) return def;
        try {
            String v = SharedPrefsData.getString(
                    context.getApplicationContext(), key, Constants.PREF_NAME);
            if (TextUtils.isEmpty(v)) return def;
            return "true".equalsIgnoreCase(v) || "1".equals(v);
        } catch (Exception ignored) {
            return def;
        }
    }

    @NonNull
    private static String readString(@Nullable Context context, String key) {
        if (context == null) return "";
        try {
            String v = SharedPrefsData.getString(
                    context.getApplicationContext(), key, Constants.PREF_NAME);
            return v != null ? v : "";
        } catch (Exception e) {
            return "";
        }
    }

    private static int readInt(@Nullable Context context, String key, int def, int min, int max) {
        if (context == null) return def;
        try {
            if (!SharedPrefsData.getBool(
                    context.getApplicationContext(),
                    Constants.DAILY_UPDATES_CONFIG_SET,
                    Constants.PREF_NAME)) {
                return def;
            }
            int v = SharedPrefsData.getInt(
                    context.getApplicationContext(), key, Constants.PREF_NAME);
            if (v < min) return min;
            if (v > max) return max;
            return v;
        } catch (Exception e) {
            return def;
        }
    }
}
