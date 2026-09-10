package com.anyNews.anynews.Utils;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Daily Updates card body chrome keyed by {@link Constants#LanguageID}.
 * Titles already live on DailyUpdatesController; body blurbs / fuel-gold labels
 * used to stay as Telugu XML defaults.
 */
public final class DailyUpdatesLabels {

    private DailyUpdatesLabels() {}

    private static int lang(@Nullable Context c) {
        if (c == null) return 0;
        try {
            return SharedPrefsData.getInt(
                    c.getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME);
        } catch (Exception e) {
            return 0;
        }
    }

    @NonNull
    public static String petrol(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "പെട്രോൾ";
            case 2: return "पेट्रोल";
            case 3: return "పెట్రోల్";
            case 4: return "பெட்ரோல்";
            case 5: return "ಪೆಟ್ರೋಲ್";
            case 6: return "ପେଟ୍ରୋଲ୍";
            default: return "Petrol";
        }
    }

    @NonNull
    public static String diesel(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "ഡീസൽ";
            case 2: return "डीज़ल";
            case 3: return "డీజిల్";
            case 4: return "டீசல்";
            case 5: return "ಡೀಸೆಲ್";
            case 6: return "ଡିଜେଲ୍";
            default: return "Diesel";
        }
    }

    @NonNull
    public static String silver(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "വെള്ളി";
            case 2: return "चांदी";
            case 3: return "వెండి";
            case 4: return "வெள்ளி";
            case 5: return "ಬೆಳ್ಳಿ";
            case 6: return "ରୂପା";
            default: return "Silver";
        }
    }

    @NonNull
    public static String newJobs(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "പുതിയ ജോലികൾ";
            case 2: return "नई नौकरियां";
            case 3: return "కొత్త ఉద్యోగాలు";
            case 4: return "புதிய வேலைகள்";
            case 5: return "ಹೊಸ ಉದ್ಯೋಗಗಳು";
            case 6: return "ନୂଆ ଚାକିରି";
            default: return "New jobs";
        }
    }

    @NonNull
    public static String panchangamCopy(@Nullable Context c) {
        switch (lang(c)) {
            case 1:
                return "തിഥി, നക്ഷത്രം,\nയോഗം, കരണം &\nശുഭ സമയങ്ങൾ";
            case 2:
                return "तिथि, नक्षत्र,\nयोग, करण &\nशुभ समय";
            case 3:
                return "తిథి, నక్షత్రం,\nయోగం, కరణం &\nశుభ సమయాలు";
            case 4:
                return "திதி, நட்சத்திரம்,\nயோகம், கரணம் &\nசுப நேரங்கள்";
            case 5:
                return "ತಿಥಿ, ನಕ್ಷತ್ರ,\nಯೋಗ, ಕರಣ &\nಶುಭ ಸಮಯಗಳು";
            case 6:
                return "ତିଥି, ନକ୍ଷତ୍ର,\nଯୋଗ, କରଣ &\nଶୁଭ ସମୟ";
            default:
                return "Tithi, nakshatra,\nyoga, karana &\nauspicious times";
        }
    }

    @NonNull
    public static String rashiCopy(@Nullable Context c) {
        switch (lang(c)) {
            case 1:
                return "മേഷം മുതൽ മീനം\nഇന്നത്തെ രാശിഫലം\n& ശുഭ സൂചനകൾ";
            case 2:
                return "मेष से मीन तक\nआज का राशिफल\n& शुभ संकेत";
            case 3:
                return "మేషం నుంచి మీనం\nఈరోజు రాశి ఫలాలు\n& శుభ సూచనలు";
            case 4:
                return "மேஷம் முதல் மீனம்\nஇன்றைய ராசிபலன்\n& சுப குறிப்புகள்";
            case 5:
                return "ಮೇಷದಿಂದ ಮೀನ\nಇಂದಿನ ರಾಶಿಫಲ\n& ಶುಭ ಸೂಚನೆಗಳು";
            case 6:
                return "ମେଷରୁ ମୀନ\nଆଜିର ରାଶିଫଳ\n& ଶୁଭ ସୂଚନା";
            default:
                return "Aries to Pisces\nToday's horoscope\n& auspicious tips";
        }
    }

    /**
     * Map weather condition_icon (or legacy Telugu condition text) → LanguageID label.
     */
    @NonNull
    public static String weatherCondition(
            @Nullable Context c, @Nullable String icon, @Nullable String fallback) {
        String key = icon == null ? "" : icon.trim().toLowerCase();
        if (key.isEmpty() && fallback != null) {
            key = inferIconFromTelugu(fallback);
        }
        switch (key) {
            case "sunny":
            case "clear":
                switch (lang(c)) {
                    case 1: return "വെയിൽ";
                    case 2: return "धूप";
                    case 3: return "ఎండగా ఉంది";
                    case 4: return "வெயில்";
                    case 5: return "ಬಿಸಿಲು";
                    case 6: return "ରବି";
                    default: return "Sunny";
                }
            case "cloudy":
            case "overcast":
                switch (lang(c)) {
                    case 1: return "മേഘാവൃതം";
                    case 2: return "बादल छाए";
                    case 3: return "మేఘావృతం";
                    case 4: return "மேகமூட்டம்";
                    case 5: return "ಮೋಡ ಕವಿದಿದೆ";
                    case 6: return "ମେଘାଚ୍ଛନ୍ନ";
                    default: return "Cloudy";
                }
            case "rainy":
            case "rain":
                switch (lang(c)) {
                    case 1: return "മഴ";
                    case 2: return "बारिश";
                    case 3: return "వర్షం";
                    case 4: return "மழை";
                    case 5: return "ಮಳೆ";
                    case 6: return "ବର୍ଷା";
                    default: return "Rain";
                }
            case "storm":
            case "thunder":
                switch (lang(c)) {
                    case 1: return "ഇടിമിന്നലുള്ള മഴ";
                    case 2: return "आंधी-बारिश";
                    case 3: return "ఉరుములతో వర్షం";
                    case 4: return "இடியுடன் மழை";
                    case 5: return "ಗುಡುಗು ಸಹಿತ ಮಳೆ";
                    case 6: return "ଗର୍ଜନ ସହ ବର୍ଷା";
                    default: return "Thunderstorm";
                }
            case "fog":
            case "mist":
                switch (lang(c)) {
                    case 1: return "മൂടൽമഞ്ഞ്";
                    case 2: return "कोहरा";
                    case 3: return "పొగమంచు";
                    case 4: return "மூடுபனி";
                    case 5: return "ಮಂಜು";
                    case 6: return "କୁହୁଡ଼ି";
                    default: return "Fog";
                }
            case "snow":
                switch (lang(c)) {
                    case 1: return "മഞ്ഞ്";
                    case 2: return "बर्फ";
                    case 3: return "మంచు";
                    case 4: return "பனி";
                    case 5: return "ಹಿಮ";
                    case 6: return "ହିମ";
                    default: return "Snow";
                }
            case "partly_cloudy":
            default:
                switch (lang(c)) {
                    case 1: return "ഭാഗികമായി മേഘാവൃതം";
                    case 2: return "आंशिक बादल";
                    case 3: return "పాక్షిక మేఘావృతం";
                    case 4: return "பகுதி மேகமூட்டம்";
                    case 5: return "ಭಾಗಶಃ ಮೋಡ";
                    case 6: return "ଆଂଶିକ ମେଘାଚ୍ଛନ୍ନ";
                    default: return "Partly cloudy";
                }
        }
    }

    @NonNull
    private static String inferIconFromTelugu(@NonNull String t) {
        if (t.contains("ఉరుము") || t.contains("ఇടി")) return "storm";
        if (t.contains("వర్షం") || t.contains("వర్ష")) return "rainy";
        if (t.contains("పొగమంచు")) return "fog";
        if (t.contains("మంచు")) return "snow";
        if (t.contains("ఎండ")) return "sunny";
        if (t.contains("పాక్షిక")) return "partly_cloudy";
        if (t.contains("మేఘ")) return "cloudy";
        return "partly_cloudy";
    }

    @NonNull
    public static String nifty50(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "നിഫ്റ്റി 50";
            case 2: return "निफ्टी 50";
            case 3: return "నిఫ్టీ 50";
            case 4: return "நிஃப்டி 50";
            case 5: return "ನಿಫ್ಟಿ 50";
            case 6: return "ନିଫ୍ଟି 50";
            default: return "Nifty 50";
        }
    }

    @NonNull
    public static String sensex(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "സെൻസെക്സ്";
            case 2: return "सेंसेक्स";
            case 3: return "సెన్సెక్స్";
            case 4: return "சென்செக்ஸ்";
            case 5: return "ಸೆನ್ಸೆಕ್ಸ್";
            case 6: return "ସେନ୍ସେକ୍ସ";
            default: return "Sensex";
        }
    }

    /** Stock search box hint ("Share Shodana" / find shares). */
    @NonNull
    public static String stockSearchHint(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "ഷെയർ തിരയുക";
            case 2: return "शेयर खोजें";
            case 3: return "షేర్ శోధన";
            case 4: return "பங்கு தேடல்";
            case 5: return "ಷೇರ್ ಹುಡುಕಿ";
            case 6: return "ସେୟାର ଖୋଜନ୍ତୁ";
            default: return "Search shares";
        }
    }

    @NonNull
    public static String stockSearchResults(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "തിരച്ചിൽ ഫലങ്ങൾ";
            case 2: return "खोज परिणाम";
            case 3: return "శోధన ఫలితాలు";
            case 4: return "தேடல் முடிவுகள்";
            case 5: return "ಹುಡುಕಾಟ ಫಲಿತಾಂಶಗಳು";
            case 6: return "ଖୋଜ ଫଳାଫଳ";
            default: return "Search results";
        }
    }

    @NonNull
    public static String stockDisabled(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "സ്റ്റോക്ക് മാർക്കറ്റ് ലഭ്യമല്ല";
            case 2: return "स्टॉक मार्केट उपलब्ध नहीं";
            case 3: return "స్టాక్ మార్కెట్ అందుబాటులో లేదు";
            case 4: return "பங்குச் சந்தை கிடைக்கவில்லை";
            case 5: return "ಸ್ಟಾಕ್ ಮಾರ್ಕೆಟ್ ಲಭ್ಯವಿಲ್ಲ";
            case 6: return "ଷ୍ଟକ୍ ମାର୍କେଟ୍ ଉପଲବ୍ଧ ନାହିଁ";
            default: return "Stock market unavailable";
        }
    }
}
