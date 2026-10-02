package com.anyNews.anynews.Utils;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.anyNews.anynews.Activities.FuelDetailActivity;
import com.anyNews.anynews.Activities.GoldDetailActivity;
import com.anyNews.anynews.Activities.HomePage;
import com.anyNews.anynews.Activities.JobsPageActivity;
import com.anyNews.anynews.Activities.StockMarketActivity;
import com.anyNews.anynews.Activities.TtdDetailActivity;
import com.anyNews.anynews.Activities.WeatherDetailActivity;
import com.anyNews.anynews.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Match typed search text to active Daily Updates categories for the user's
 * language so InAppSearch can suggest Jobs / Weather / etc. and open that feed.
 */
public final class DailyUpdatesSearchSuggest {

    public static final String EXTRA_DU_OPEN = "extra_du_open";
    public static final String DU_OPEN_PANCHANGAM = "panchangam";
    public static final String DU_OPEN_RASHI = "rashi";
    public static final String DU_OPEN_CRICKET = "cricket";
    public static final String DU_OPEN_FLASH = "flash";
    public static final String DU_OPEN_MANA = "mana";
    public static final String DU_OPEN_HUB = "hub";

    private DailyUpdatesSearchSuggest() {}

    public static final class Match {
        @NonNull public final String title;
        @NonNull public final String key;
        @Nullable public final Class<?> activityClass;
        @Nullable public final String homeExtra;

        Match(
                @NonNull String title,
                @NonNull String key,
                @Nullable Class<?> activityClass,
                @Nullable String homeExtra) {
            this.title = title;
            this.key = key;
            this.activityClass = activityClass;
            this.homeExtra = homeExtra;
        }
    }

    /**
     * Active Daily Updates categories whose title / aliases contain {@code query}
     * (case-insensitive). Empty query → empty list.
     */
    @NonNull
    public static List<Match> matches(@Nullable Context context, @Nullable String query) {
        List<Match> out = new ArrayList<>();
        if (context == null) return out;
        String q = query != null ? query.trim().toLowerCase(Locale.ROOT) : "";
        if (q.isEmpty()) return out;

        for (Match m : allActive(context)) {
            if (aliasesMatch(m, q)) {
                out.add(m);
            }
        }
        return out;
    }

    @NonNull
    public static Intent launchIntent(@NonNull Context context, @NonNull Match match) {
        if (match.activityClass != null) {
            return new Intent(context, match.activityClass);
        }
        Intent intent = new Intent(context, HomePage.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if (DU_OPEN_HUB.equals(match.homeExtra)) {
            intent.putExtra(HomePage.EXTRA_FOOTER_TAB, R.id.navigation_status);
        } else if (match.homeExtra != null) {
            intent.putExtra(EXTRA_DU_OPEN, match.homeExtra);
            intent.putExtra(HomePage.EXTRA_FOOTER_TAB, R.id.navigation_status);
        }
        return intent;
    }

    @NonNull
    private static List<Match> allActive(@NonNull Context context) {
        List<Match> all = new ArrayList<>();
        int lang = langId(context);

        if (LocalUpdatesConfigHelper.isWeatherEnabled(context)) {
            all.add(new Match(titleWeather(lang), "weather", WeatherDetailActivity.class, null));
        }
        if (LocalUpdatesConfigHelper.isFuelEnabled(context)) {
            all.add(new Match(titleFuel(lang), "fuel", FuelDetailActivity.class, null));
        }
        if (LocalUpdatesConfigHelper.isGoldEnabled(context)) {
            all.add(new Match(titleGold(lang), "gold", GoldDetailActivity.class, null));
        }
        if (TtdConfigHelper.isEnabled(context)) {
            all.add(new Match(titleTtd(lang), "ttd", TtdDetailActivity.class, null));
        }
        if (JobsConfigHelper.isEnabled(context)) {
            all.add(new Match(titleJobs(lang), "jobs", JobsPageActivity.class, null));
        }
        if (DailyUpdatesConfigHelper.isPanchangamEnabled(context)) {
            all.add(new Match(titlePanchangam(lang), "panchangam", null, DU_OPEN_PANCHANGAM));
        }
        if (DailyUpdatesConfigHelper.isRashiEnabled(context)) {
            all.add(new Match(titleRashi(lang), "rashi", null, DU_OPEN_RASHI));
        }
        if (DailyUpdatesConfigHelper.isCricketEnabled(context)) {
            all.add(new Match(titleCricket(lang), "cricket", null, DU_OPEN_CRICKET));
        }
        if (DailyUpdatesConfigHelper.isStockEnabled(context)) {
            all.add(new Match(
                    DailyUpdatesConfigHelper.stockTitle(context),
                    "stock",
                    StockMarketActivity.class,
                    null));
        }
        if (DailyUpdatesConfigHelper.isFlashNewsEnabled(context)) {
            all.add(new Match(titleFlash(lang), "flash", null, DU_OPEN_FLASH));
        }
        if (DailyUpdatesConfigHelper.isManaZillaEnabled(context)) {
            all.add(new Match(titleMana(lang), "mana", null, DU_OPEN_MANA));
        }
        // Always offer the Daily Updates hub when the typed text matches.
        all.add(new Match(titleDailyHub(lang), "daily_updates", null, DU_OPEN_HUB));
        return all;
    }

    private static boolean aliasesMatch(@NonNull Match m, @NonNull String q) {
        if (containsFold(m.title, q) || containsFold(m.key, q)) {
            return true;
        }
        for (String a : aliasesFor(m.key)) {
            if (containsFold(a, q)) return true;
        }
        return false;
    }

    private static boolean containsFold(@Nullable String hay, @NonNull String needle) {
        if (hay == null || hay.isEmpty()) return false;
        return hay.toLowerCase(Locale.ROOT).contains(needle);
    }

    @NonNull
    private static String[] aliasesFor(@NonNull String key) {
        switch (key) {
            case "jobs":
                return new String[] {
                    "jobs", "job", "udyog", "udyoga", "నౌకరీ", "ఉద్యోగ", "नौकरी", "வேலை", "ಉದ್ಯೋಗ"
                };
            case "weather":
                return new String[] {
                    "weather", "climate", "వాతావరణ", "मौसम", "வானிலை", "ಹವಾಮಾನ"
                };
            case "fuel":
                return new String[] {
                    "fuel", "petrol", "diesel", "పెట్రోల్", "డీజిల్", "पेट्रोल", "डीज़ल"
                };
            case "gold":
                return new String[] {
                    "gold", "silver", "బంగారం", "వెండి", "सोना", "चांदी", "தங்கம்"
                };
            case "ttd":
                return new String[] {"ttd", "tirupati", "టీటీడీ", "तिरुपति"};
            case "panchangam":
                return new String[] {"panchangam", "panchang", "పంచాంగం", "पंचांग", "பஞ்சாங்கம்"};
            case "rashi":
                return new String[] {
                    "rashi", "horoscope", "రాశి", "राशिफल", "ராசி", "ರಾಶಿ"
                };
            case "stock":
                return new String[] {
                    "stock", "market", "share", "nifty", "sensex", "స్టాక్", "स्टॉक"
                };
            case "flash":
                return new String[] {"flash", "breaking", "ఫ్లాష్", "फ्लैश"};
            case "mana":
                return new String[] {
                    "mana", "zilla", "local", "మన జిల్లా", "जिला", "district"
                };
            case "daily_updates":
                return new String[] {
                    "daily", "updates", "డైలీ", "అప్డేట్", "दैनिक"
                };
            default:
                return new String[0];
        }
    }

    private static int langId(@Nullable Context c) {
        if (c == null) return 0;
        try {
            return SharedPrefsData.getInt(c, Constants.LanguageID, Constants.PREF_NAME);
        } catch (Exception e) {
            return 0;
        }
    }

    @NonNull
    private static String titleWeather(int lang) {
        switch (lang) {
            case 1: return "കാലാവസ്ഥ";
            case 2: return "मौसम";
            case 3: return "వాతావరణం";
            case 4: return "வானிலை";
            case 5: return "ಹವಾಮಾನ";
            case 6: return "ପାଣିପାଗ";
            default: return "Weather";
        }
    }

    @NonNull
    private static String titleFuel(int lang) {
        switch (lang) {
            case 1: return "പെട്രോൾ & ഡീസൽ";
            case 2: return "पेट्रोल और डीज़ल";
            case 3: return "పెట్రోల్ & డీజిల్";
            case 4: return "பெட்ரோல் & டீசல்";
            case 5: return "ಪೆಟ್ರೋಲ್ & ಡೀಸೆಲ್";
            case 6: return "ପେଟ୍ରୋଲ୍ ଓ ଡିଜେଲ୍";
            default: return "Petrol & Diesel";
        }
    }

    @NonNull
    private static String titleGold(int lang) {
        switch (lang) {
            case 1: return "സ്വർണം & വെള്ളി";
            case 2: return "सोना और चांदी";
            case 3: return "బంగారం & వెండి";
            case 4: return "தங்கம் & வெள்ளி";
            case 5: return "ಚಿನ್ನ & ಬೆಳ್ಳಿ";
            case 6: return "ସୁନା ଓ ରୂପା";
            default: return "Gold & Silver";
        }
    }

    @NonNull
    private static String titleTtd(int lang) {
        switch (lang) {
            case 1: return "ടിടിഡി വിവരം";
            case 2: return "टीटीडी जानकारी";
            case 3: return "టీటీడీ సమాచారం";
            case 4: return "டிடிடி தகவல்";
            case 5: return "ಟಿಟಿಡಿ ಮಾಹಿತಿ";
            case 6: return "ଟିଟିଡି ସୂଚନା";
            default: return "TTD Info";
        }
    }

    @NonNull
    private static String titleJobs(int lang) {
        switch (lang) {
            case 1: return "ജോലികൾ";
            case 2: return "नौकरियां";
            case 3: return "ఉద్యోగాలు";
            case 4: return "வேலைகள்";
            case 5: return "ಉದ್ಯೋಗಗಳು";
            case 6: return "ଚାକିରି";
            default: return "Jobs";
        }
    }

    @NonNull
    private static String titlePanchangam(int lang) {
        switch (lang) {
            case 1: return "പഞ്ചാംഗം";
            case 2: return "पंचांग";
            case 3: return "పంచాంగం";
            case 4: return "பஞ்சாங்கம்";
            case 5: return "ಪಂಚಾಂಗ";
            case 6: return "ପଞ୍ଚାଙ୍ଗ";
            default: return "Panchangam";
        }
    }

    @NonNull
    private static String titleCricket(int lang) {
        switch (lang) {
            case 1: return "ക്രിക്കറ്റ്";
            case 2: return "क्रिकेट";
            case 3: return "క్రికెట్";
            case 4: return "கிரிக்கெட்";
            case 5: return "ಕ್ರಿಕೆಟ್";
            case 6: return "କ୍ରିକେଟ";
            default: return "Cricket";
        }
    }

    @NonNull
    private static String titleRashi(int lang) {
        switch (lang) {
            case 1: return "രാശിഫലം";
            case 2: return "राशिफल";
            case 3: return "రాశిఫలాలు";
            case 4: return "ராசிபலன்";
            case 5: return "ರಾಶಿಫಲ";
            case 6: return "ରାଶିଫଳ";
            default: return "Rashi Phalalu";
        }
    }

    @NonNull
    private static String titleFlash(int lang) {
        switch (lang) {
            case 3: return "ఫ్లాష్ న్యూస్";
            case 2: return "फ्लैश न्यूज़";
            default: return "Flash News";
        }
    }

    @NonNull
    private static String titleMana(int lang) {
        switch (lang) {
            case 3: return "మన జిల్లా";
            case 2: return "मेरा जिला";
            default: return "Local News";
        }
    }

    @NonNull
    private static String titleDailyHub(int lang) {
        switch (lang) {
            case 3: return "డైలీ అప్డేట్స్";
            case 2: return "डेली अपडेट्स";
            default: return "Daily Updates";
        }
    }
}
