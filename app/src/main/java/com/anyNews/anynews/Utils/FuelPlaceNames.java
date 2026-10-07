package com.anyNews.anynews.Utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Fuel place names in the app language (1 ML, 2 HI, 3 TE, 4 TA, 5 KN, 6 OR).
 * States use a fixed name in that script. Cities use the same script: a known
 * spelling when we have one, otherwise a transliteration of the English source.
 * Latin and another language's script are not returned for languages 1–6.
 */
public final class FuelPlaceNames {

    private static final String[] CONS_KEYS = {
            "k", "kh", "g", "gh", "ng",
            "ch", "chh", "j", "jh", "ny",
            "T", "Th", "D", "Dh", "N",
            "t", "th", "d", "dh", "n",
            "p", "ph", "b", "bh", "m",
            "y", "r", "l", "v", "sh", "s", "h"
    };

    /** Index 0 unused. 1–6 match {@link Constants#LanguageID}. */
    private static final Script[] SCRIPTS = new Script[7];

    /** Normalized English state → names[lang]. */
    private static final Map<String, String[]> STATES = new HashMap<>();
    /** Normalized English city → names[lang], null slot means transliterate. */
    private static final Map<String, String[]> CITIES = new HashMap<>();

    static {
        SCRIPTS[1] = script(
                "അ|ആ|ഇ|ഈ|ഉ|ഊ|എ|ഐ|ഒ|ഔ",
                "|ാ|ി|ീ|ു|ൂ|െ|ൈ|ൊ|ൌ",
                "്", "ം",
                "ക|ഖ|ഗ|ഘ|ങ|ച|ഛ|ജ|ഝ|ഞ|ട|ഠ|ഡ|ഢ|ണ|ത|ഥ|ദ|ധ|ന|പ|ഫ|ബ|ഭ|മ|യ|ര|ല|വ|ശ|സ|ഹ");
        SCRIPTS[2] = script(
                "अ|आ|इ|ई|उ|ऊ|ए|ऐ|ओ|औ",
                "|ा|ि|ी|ु|ू|े|ै|ो|ौ",
                "्", "ं",
                "क|ख|ग|घ|ङ|च|छ|ज|झ|ञ|ट|ठ|ड|ढ|ण|त|थ|द|ध|न|प|फ|ब|भ|म|य|र|ल|व|श|स|ह");
        SCRIPTS[3] = script(
                "అ|ఆ|ఇ|ఈ|ఉ|ఊ|ఎ|ఐ|ఒ|ఔ",
                "|ా|ి|ీ|ు|ూ|ె|ై|ొ|ౌ",
                "్", "ం",
                "క|ఖ|గ|ఘ|ఙ|చ|ఛ|జ|ఝ|ఞ|ట|ఠ|డ|ఢ|ణ|త|థ|ద|ధ|న|ప|ఫ|బ|భ|మ|య|ర|ల|వ|శ|స|హ");
        SCRIPTS[4] = script(
                "அ|ஆ|இ|ஈ|உ|ஊ|எ|ஐ|ஒ|ஔ",
                "|ா|ி|ீ|ு|ூ|ெ|ை|ொ|ௌ",
                "்", "\u0B82",
                "க|க|க|க|ங|ச|ச|ஜ|ஜ|ஞ|ட|ட|ட|ட|ண|த|த|த|த|ந|ப|ப|ப|ப|ம|ய|ர|ல|வ|ஷ|ஸ|ஹ");
        SCRIPTS[5] = script(
                "ಅ|ಆ|ಇ|ಈ|ಉ|ಊ|ಎ|ಐ|ಒ|ಔ",
                "|ಾ|ಿ|ೀ|ು|ೂ|ೆ|ೈ|ೊ|ೌ",
                "್", "ಂ",
                "ಕ|ಖ|ಗ|ಘ|ಙ|ಚ|ಛ|ಜ|ಝ|ಞ|ಟ|ಠ|ಡ|ಢ|ಣ|ತ|ಥ|ದ|ಧ|ನ|ಪ|ಫ|ಬ|ಭ|ಮ|ಯ|ರ|ಲ|ವ|ಶ|ಸ|ಹ");
        SCRIPTS[6] = script(
                "ଅ|ଆ|ଇ|ଈ|ଉ|ଊ|ଏ|ଐ|ଓ|ଔ",
                "|ା|ି|ୀ|ୁ|ୂ|େ|ୈ|ୋ|ୌ",
                "୍", "ଂ",
                "କ|ଖ|ଗ|ଘ|ଙ|ଚ|ଛ|ଜ|ଝ|ଞ|ଟ|ଠ|ଡ|ଢ|ଣ|ତ|ଥ|ଦ|ଧ|ନ|ପ|ଫ|ବ|ଭ|ମ|ଯ|ର|ଲ|ଵ|ଶ|ସ|ହ");

        state("andhra pradesh",
                "ആന്ധ്രപ്രദേശ്", "आंध्र प्रदेश", "ఆంధ్రప్రదేశ్",
                "ஆந்திரப் பிரதேசம்", "ಆಂಧ್ರಪ್ರದೇಶ", "ଆନ୍ଧ୍ର ପ୍ରଦେଶ");
        state("telangana",
                "തെലങ്കാന", "तेलंगाना", "తెలంగాణ",
                "தெலங்கானா", "ತೆಲಂಗಾಣ", "ତେଲଙ୍ଗାନା");
        state("andaman and nicobar islands",
                "ആൻഡമാൻ നിക്കോബാർ ദ്വീപുകൾ", "अंडमान और निकोबार द्वीप समूह", "అండమాన్ మరియు నికోబార్ దీవులు",
                "அந்தமான் நிக்கோபார் தீவுகள்", "ಅಂಡಮಾನ್ ಮತ್ತು ನಿಕೋಬಾರ್ ದ್ವೀಪಗಳು", "ଆଣ୍ଡାମାନ ଓ ନିକୋବର ଦ୍ୱୀପପୁଞ୍ଜ");
        state("andaman and nicobar", STATES.get("andaman and nicobar islands"));
        state("arunachal pradesh",
                "അരുണാചൽ പ്രദേശ്", "अरुणाचल प्रदेश", "అరుణాచల్ ప్రదేశ్",
                "அருணாச்சலப் பிரதேசம்", "ಅರುಣಾಚಲ ಪ್ರದೇಶ", "ଅରୁଣାଚଳ ପ୍ରଦେଶ");
        state("assam",
                "അസം", "असम", "అసోం",
                "அசாம்", "ಅಸ್ಸಾಂ", "ଆସାମ");
        state("bihar",
                "ബീഹാർ", "बिहार", "బీహార్",
                "பீகார்", "ಬಿಹಾರ", "ବିହାର");
        state("chandigarh",
                "ചണ്ഡീഗഡ്", "चंडीगढ़", "చండీగఢ్",
                "சண்டிகர்", "ಚಂಡೀಗಢ", "ଚଣ୍ଡୀଗଡ଼");
        state("chhattisgarh",
                "ഛത്തീസ്ഗഡ്", "छत्तीसगढ़", "ఛత్తీస్‌గఢ్",
                "சத்தீஸ்கர்", "ಛತ್ತೀಸ್‌ಗಢ", "ଛତ୍ତିଶଗଡ଼");
        state("chattisgarh", STATES.get("chhattisgarh"));
        state("dadra and nagar haveli",
                "ദാദ്ര നഗർ ഹവേലി", "दादरा और नगर हवेली", "దాద్రా మరియు నగర్ హవేలీ",
                "தாத்ரா மற்றும் நகர் ஹவேலி", "ದಾದ್ರಾ ಮತ್ತು ನಗರ್ ಹವೇಲಿ", "ଦାଦ୍ରା ଓ ନଗର ହାଭେଳୀ");
        state("daman and diu",
                "ദമൻ ദിയു", "दमन और दीव", "దమన్ మరియు దీవ్",
                "தமன் மற்றும் தியு", "ದಮನ್ ಮತ್ತು ದಿಯು", "ଦମନ ଓ ଦୀଉ");
        state("dadra and nagar haveli and daman and diu",
                "ദാദ്ര നഗർ ഹവേലി ദമൻ ദിയു", "दादरा और नगर हवेली और दमन और दीव",
                "దాద్రా నగర్ హవేలీ మరియు దమన్ దీవ్",
                "தாத்ரா நகர் ஹவேலி தமன் தியு", "ದಾದ್ರಾ ನಗರ್ ಹವೇಲಿ ಮತ್ತು ದಮನ್ ದಿಯು",
                "ଦାଦ୍ରା ନଗର ହାଭେଳୀ ଓ ଦମନ ଦୀଉ");
        state("delhi",
                "ഡൽഹി", "दिल्ली", "ఢిల్లీ",
                "டெல்லி", "ದೆಹಲಿ", "ଦିଲ୍ଲୀ");
        state("new delhi", STATES.get("delhi"));
        state("goa",
                "ഗോവ", "गोवा", "గోవా",
                "கோவா", "ಗೋವಾ", "ଗୋଆ");
        state("gujarat",
                "ഗുജറാത്ത്", "गुजरात", "గుజరాత్",
                "குஜராத்", "ಗುಜರಾತ್", "ଗୁଜରାଟ");
        state("haryana",
                "ഹരിയാണ", "हरियाणा", "హర్యానా",
                "ஹரியானா", "ಹರಿಯಾಣ", "ହରିଆଣା");
        state("himachal pradesh",
                "ഹിമാചൽ പ്രദേശ്", "हिमाचल प्रदेश", "హిమాచల్ ప్రదేశ్",
                "இமாச்சலப் பிரதேசம்", "ಹಿಮಾಚಲ ಪ್ರದೇಶ", "ହିମାଚଳ ପ୍ରଦେଶ");
        state("jammu and kashmir",
                "ജമ്മു കശ്മീർ", "जम्मू और कश्मीर", "జమ్మూ మరియు కాశ్మీర్",
                "ஜம்மு காஷ்மீர்", "ಜಮ್ಮು ಮತ್ತು ಕಾಶ್ಮೀರ", "ଜମ୍ମୁ ଓ କଶ୍ମୀର");
        state("jharkhand",
                "ഝാർഖണ്ഡ്", "झारखंड", "ఝార్ఖండ్",
                "ஜார்கண்ட்", "ಜಾರ್ಖಂಡ್", "ଝାଡ଼ଖଣ୍ଡ");
        state("karnataka",
                "കർണാടക", "कर्नाटक", "కర్ణాటక",
                "கர்நாடகா", "ಕರ್ನಾಟಕ", "କର୍ଣ୍ଣାଟକ");
        state("kerala",
                "കേരളം", "केरल", "కేరళ",
                "கேரளா", "ಕೇರಳ", "କେରଳ");
        state("madhya pradesh",
                "മധ്യപ്രദേശ്", "मध्य प्रदेश", "మధ్య ప్రదేశ్",
                "மத்தியப் பிரதேசம்", "ಮಧ್ಯ ಪ್ರದೇಶ", "ମଧ୍ୟ ପ୍ରଦେଶ");
        state("maharashtra",
                "മഹാരാഷ്ട്ര", "महाराष्ट्र", "మహారాష్ట్ర",
                "மகாராஷ்டிரா", "ಮಹಾರಾಷ್ಟ್ರ", "ମହାରାଷ୍ଟ୍ର");
        state("manipur",
                "മണിപ്പൂർ", "मणिपुर", "మణిపూర్",
                "மணிப்பூர்", "ಮಣಿಪುರ", "ମଣିପୁର");
        state("meghalaya",
                "മേഘാലയ", "मेघालय", "మేఘాలయ",
                "மேகாலயா", "ಮೇಘಾಲಯ", "ମେଘାଳୟ");
        state("mizoram",
                "മിസോറം", "मिजोरम", "మిజోరం",
                "மிசோரம்", "ಮಿಜೋರಾಂ", "ମିଜୋରାମ");
        state("nagaland",
                "നാഗാലാൻഡ്", "नागालैंड", "నాగాలాండ్",
                "நாகாலாந்து", "ನಾಗಾಲ್ಯಾಂಡ್", "ନାଗାଲାଣ୍ଡ");
        state("odisha",
                "ഒഡിഷ", "ओडिशा", "ఒడిశా",
                "ஒடிசா", "ಒಡಿಶಾ", "ଓଡ଼ିଶା");
        state("orissa", STATES.get("odisha"));
        state("punjab",
                "പഞ്ചാബ്", "पंजाब", "పంజాబ్",
                "பஞ்சாப்", "ಪಂಜಾಬ್", "ପଞ୍ଜାବ");
        state("rajasthan",
                "രാജസ്ഥാൻ", "राजस्थान", "రాజస్థాన్",
                "ராஜஸ்தான்", "ರಾಜಸ್ಥಾನ", "ରାଜସ୍ଥାନ");
        state("sikkim",
                "സിക്കിം", "सिक्किम", "సిక్కిం",
                "சிக்கிம்", "ಸಿಕ್ಕಿಂ", "ସିକ୍କିମ");
        state("tamil nadu",
                "തമിഴ്നാട്", "तमिलनाडु", "తమిళనాడు",
                "தமிழ்நாடு", "ತಮಿಳುನಾಡು", "ତାମିଲନାଡ଼ୁ");
        state("tamilnadu", STATES.get("tamil nadu"));
        state("tripura",
                "ത്രിപുര", "त्रिपुरा", "త్రిపుర",
                "திரிபுரா", "ತ್ರಿಪುರ", "ତ୍ରିପୁରା");
        state("uttar pradesh",
                "ഉത്തർ പ്രദേശ്", "उत्तर प्रदेश", "ఉత్తర ప్రదేశ్",
                "உத்தரப் பிரதேசம்", "ಉತ್ತರ ಪ್ರದೇಶ", "ଉତ୍ତର ପ୍ରଦେଶ");
        state("uttarakhand",
                "ഉത്തരാഖണ്ഡ്", "उत्तराखंड", "ఉత్తరాఖండ్",
                "உத்தராகண்ட்", "ಉತ್ತರಾಖಂಡ", "ଉତ୍ତରାଖଣ୍ଡ");
        state("west bengal",
                "പശ്ചിമ ബംഗാൾ", "पश्चिम बंगाल", "పశ్చిమ బెంగాల్",
                "மேற்கு வங்காளம்", "ಪಶ್ಚಿಮ ಬಂಗಾಳ", "ପଶ୍ଚିମ ବଙ୍ଗ");
        state("puducherry",
                "പുതുച്ചേരി", "पुदुच्चेरी", "పుదుచ్చేరి",
                "புதுச்சேரி", "ಪುದುಚೇರಿ", "ପୁଡୁଚେରୀ");
        state("pondicherry", STATES.get("puducherry"));
        state("ladakh",
                "ലഡാക്ക്", "लद्दाख", "లడఖ్",
                "லடாக்", "ಲಡಾಖ್", "ଲଦାଖ");
        state("lakshadweep",
                "ലക്ഷദ്വീപ്", "लक्षद्वीप", "లక్షద్వీప్",
                "லட்சத்தீவு", "ಲಕ್ಷದ್ವೀಪ", "ଲକ୍ଷଦ୍ୱୀପ");

        cityTe("visakhapatnam", "విశాఖపట్నం");
        cityTe("vizag", "విశాఖపట్నం");
        cityTe("vijayawada", "విజయవాడ");
        cityTe("tirupati", "తిరుపతి");
        cityTe("kakinada", "కాకినాడ");
        cityTe("rajahmundry", "రాజమండ్రి");
        cityTe("kurnool", "కర్నూలు");
        cityTe("guntur", "గుంటూరు");
        cityTe("nellore", "నెల్లూరు");
        cityTe("warangal", "వరంగల్");
        cityTe("karimnagar", "కరీంనగర్");
        cityTe("nizamabad", "నిజామాబాద్");
        cityTe("khammam", "ఖమ్మం");
        cityTe("adilabad", "ఆదిలాబాద్");
        cityTe("nalgonda", "నల్గొండ");
        cityTe("sangareddy", "సంగారెడ్డి");
        cityTe("mahabubnagar", "మహబూబ్‌నగర్");
        cityTe("mahbubnagar", "మహబూబ్‌నగర్");
        cityTe("srikakulam", "శ్రీకాకుళం");
        cityTe("vizianagaram", "విజయనగరం");
        cityTe("eluru", "ఏలూరు");
        cityTe("ongole", "ఒంగోలు");
        cityTe("chittoor", "చిత్తూరు");
        cityTe("anantapur", "అనంతపురం");
        cityTe("kadapa", "కడప");
        cityTe("machilipatnam", "మచిలీపట్నం");
        city("hyderabad",
                "ഹൈദരാബാദ്", "हैदराबाद", "హైదరాబాద్",
                "ஹைதராபாத்", "ಹೈದರಾಬಾದ್", "ହାଇଦ୍ରାବାଦ");
        city("visakhapatnam", fillLang(3, "విశాఖపట్నం",
                "വിശാഖപട്ടണം", "विशाखापट्टनम", "விசாகப்பட்டினம்", "ವಿಶಾಖಪಟ್ಟಣಂ", "ବିଶାଖପଟ୍ଟନମ୍"));
        city("vijayawada", fillLang(3, "విజయవాడ",
                "വിജയവാഡ", "विजयवाड़ा", "விஜயவாடா", "ವಿಜಯವಾಡ", "ବିଜୟୱାଡା"));
        city("tirupati", fillLang(3, "తిరుపతి",
                "തിരുപ്പതി", "तिरुपति", "திருப்பதி", "ತಿರುಪತಿ", "ତିରୁପତି"));
        city("warangal", fillLang(3, "వరంగల్",
                "വാറങ്കൽ", "वारंगल", "வாரங்கல்", "ವಾರಂಗಲ್", "ୱାରାଙ୍ଗଲ୍"));
    }

    private FuelPlaceNames() {
    }

    /**
     * Name in {@code lang}. Empty when {@code raw} is another script and this
     * language has no mapping — callers pass the English source instead.
     */
    @NonNull
    public static String inLanguage(@Nullable String raw, int lang) {
        if (raw == null) return "";
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) return "";
        int script = dominantScript(trimmed);
        if (lang >= 1 && lang <= 6) {
            if (script == lang) return trimmed;
            if (script != 0) return "";
            String hit = lookup(norm(trimmed), lang);
            if (hit != null && !hit.isEmpty()) return hit;
            return transliterate(trimmed, lang);
        }
        if (script == 0) return trimmed;
        return "";
    }

    /** True when {@code name} is written in {@code lang}'s script (Latin when lang is not 1–6). */
    public static boolean matchesLanguage(@Nullable String name, int lang) {
        if (name == null || name.trim().isEmpty()) return false;
        int script = dominantScript(name);
        if (lang >= 1 && lang <= 6) return script == lang;
        return script == 0;
    }

    @Nullable
    private static String lookup(@NonNull String key, int lang) {
        if (lang < 1 || lang > 6 || key.isEmpty()) return null;
        String[] state = STATES.get(key);
        if (state != null && state[lang] != null && !state[lang].isEmpty()) return state[lang];
        String[] city = CITIES.get(key);
        if (city != null && city[lang] != null && !city[lang].isEmpty()) return city[lang];
        return null;
    }

    @NonNull
    private static String norm(@NonNull String raw) {
        return raw.toLowerCase(Locale.US)
                .replace('&', ' ')
                .replaceAll("[^a-z0-9]+", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static void state(@NonNull String key, @NonNull String ml, @NonNull String hi,
                              @NonNull String te, @NonNull String ta, @NonNull String kn,
                              @NonNull String or) {
        STATES.put(key, new String[]{"", ml, hi, te, ta, kn, or});
    }

    private static void state(@NonNull String key, @Nullable String[] names) {
        if (names != null) STATES.put(key, names);
    }

    private static void city(@NonNull String key, @NonNull String ml, @NonNull String hi,
                             @NonNull String te, @NonNull String ta, @NonNull String kn,
                             @NonNull String or) {
        CITIES.put(key, new String[]{"", ml, hi, te, ta, kn, or});
    }

    private static void city(@NonNull String key, @NonNull String[] names) {
        CITIES.put(key, names);
    }

    /** Telugu spelling for an Andhra / Telangana fuel city. Other languages transliterate. */
    private static void cityTe(@NonNull String key, @NonNull String te) {
        String[] row = new String[]{"", null, null, te, null, null, null};
        CITIES.put(key, row);
    }

    @NonNull
    private static String[] fillLang(int keep, @NonNull String kept,
                                     @NonNull String ml, @NonNull String hi,
                                     @NonNull String ta, @NonNull String kn, @NonNull String or) {
        String[] row = new String[]{"", ml, hi, null, ta, kn, or};
        row[keep] = kept;
        return row;
    }

    @NonNull
    private static String transliterate(@NonNull String raw, int lang) {
        Script sc = SCRIPTS[lang];
        if (sc == null) return "";
        String prepared = prep(raw);
        if (prepared.isEmpty()) return "";
        StringBuilder out = new StringBuilder();
        String[] words = prepared.split(" ");
        for (int w = 0; w < words.length; w++) {
            if (w > 0) out.append(' ');
            String word = words[w];
            if ("and".equals(word)) {
                out.append(conjunction(lang));
            } else {
                out.append(trWord(word, sc));
            }
        }
        return out.toString().trim();
    }

    @NonNull
    private static String conjunction(int lang) {
        switch (lang) {
            case 1: return "ഒപ്പം";
            case 2: return "और";
            case 3: return "మరియు";
            case 4: return "மற்றும்";
            case 5: return "ಮತ್ತು";
            case 6: return "ଓ";
            default: return "and";
        }
    }

    @NonNull
    private static String prep(@NonNull String raw) {
        String[] words = norm(raw).split(" ");
        StringBuilder out = new StringBuilder();
        for (String w : words) {
            if (w.isEmpty()) continue;
            if (w.endsWith("gaon")) w = w.substring(0, w.length() - 4) + "gaav";
            else if (w.endsWith("oa")) w = w.substring(0, w.length() - 2) + "ovaa";
            if (w.endsWith("pur") && !w.endsWith("puram")) {
                w = w.substring(0, w.length() - 3) + "puur";
            }
            if (w.endsWith("garh")) w = w.substring(0, w.length() - 4) + "ga#";
            if (out.length() > 0) out.append(' ');
            out.append(w);
        }
        return out.toString();
    }

    @NonNull
    private static String trWord(@NonNull String s, @NonNull Script sc) {
        StringBuilder out = new StringBuilder();
        String pending = null;
        boolean gem = false;
        int i = 0;
        while (i < s.length()) {
            if (s.charAt(i) == '#') {
                flushVirama(out, sc, pending, gem);
                pending = "Dh";
                gem = false;
                i++;
                continue;
            }
            Vowel v = readVowel(s, i);
            if (v != null) {
                if (pending != null) {
                    if (gem) out.append(sc.cons.get(pending)).append(sc.virama);
                    out.append(sc.cons.get(pending)).append(sc.sign[v.index]);
                    pending = null;
                    gem = false;
                } else {
                    out.append(sc.indep[v.index]);
                }
                i += v.len;
                continue;
            }
            Cons c = readCons(s, i);
            if (c == null) {
                i++;
                continue;
            }
            if ("y".equals(c.key) && pending != null && isConsAt(s, i + c.len)) {
                if (gem) out.append(sc.cons.get(pending)).append(sc.virama);
                out.append(sc.cons.get(pending)).append(sc.sign[7]);
                pending = null;
                gem = false;
                i += c.len;
                continue;
            }
            if (("m".equals(c.key) || "n".equals(c.key)) && isConsAt(s, i + c.len)) {
                flushVirama(out, sc, pending, gem);
                pending = null;
                gem = false;
                out.append(sc.anusvara);
                i += c.len;
                continue;
            }
            flushVirama(out, sc, pending, gem);
            pending = c.key;
            gem = c.gem;
            i += c.len;
        }
        flushVirama(out, sc, pending, gem);
        return out.toString();
    }

    private static void flushVirama(StringBuilder out, Script sc, @Nullable String pending, boolean gem) {
        if (pending == null) return;
        String glyph = sc.cons.get(pending);
        if (glyph == null) return;
        if (gem) out.append(glyph).append(sc.virama);
        out.append(glyph).append(sc.virama);
    }

    private static boolean isConsAt(@NonNull String s, int i) {
        if (i >= s.length()) return false;
        if (s.charAt(i) == '#') return true;
        if (readVowel(s, i) != null) return false;
        return readCons(s, i) != null;
    }

    @Nullable
    private static Vowel readVowel(@NonNull String s, int i) {
        if (i >= s.length()) return null;
        if (i + 1 < s.length()) {
            String two = s.substring(i, i + 2);
            if ("aa".equals(two)) return new Vowel(1, 2);
            if ("ai".equals(two)) return new Vowel(7, 2);
            if ("au".equals(two)) return new Vowel(9, 2);
            if ("ee".equals(two) || "ii".equals(two)) return new Vowel(3, 2);
            if ("oo".equals(two) || "uu".equals(two)) return new Vowel(5, 2);
        }
        switch (s.charAt(i)) {
            case 'a': return new Vowel(0, 1);
            case 'e': return new Vowel(6, 1);
            case 'i': return new Vowel(2, 1);
            case 'o': return new Vowel(8, 1);
            case 'u': return new Vowel(4, 1);
            default: return null;
        }
    }

    @Nullable
    private static Cons readCons(@NonNull String s, int i) {
        if (s.startsWith("tt", i)) return new Cons("T", 2, true);
        if (s.startsWith("dd", i)) return new Cons("D", 2, true);
        String[] order = {
                "ksh", "chh", "kh", "gh", "jh", "th", "dh", "ph", "bh", "sh", "ng", "ny",
                "ch", "j", "t", "d", "n", "p", "b", "m", "y", "r", "l", "v", "w", "s", "h",
                "k", "g", "c", "f", "z", "q", "x"
        };
        for (String k : order) {
            if (!s.startsWith(k, i)) continue;
            String key = k;
            if ("w".equals(key)) key = "v";
            else if ("c".equals(key)) key = "ch";
            else if ("f".equals(key)) key = "ph";
            else if ("z".equals(key)) key = "j";
            else if ("q".equals(key)) key = "k";
            else if ("x".equals(key) || "ksh".equals(key)) key = "sh".equals(k) ? "sh" : ("ksh".equals(k) || "x".equals(k) ? "k" : key);
            if ("ksh".equals(k) || "x".equals(k)) {
                return new Cons("ksh", k.length(), false);
            }
            boolean geminate = k.length() == 1 && i + 1 < s.length() && s.charAt(i) == s.charAt(i + 1);
            return new Cons(key, geminate ? 2 : k.length(), geminate);
        }
        return null;
    }

    private static int dominantScript(@NonNull String s) {
        int ml = 0, hi = 0, te = 0, ta = 0, kn = 0, or = 0;
        for (int i = 0; i < s.length(); ) {
            int cp = s.codePointAt(i);
            i += Character.charCount(cp);
            if (cp >= 0x0D00 && cp <= 0x0D7F) ml++;
            else if (cp >= 0x0900 && cp <= 0x097F) hi++;
            else if (cp >= 0x0C00 && cp <= 0x0C7F) te++;
            else if (cp >= 0x0B80 && cp <= 0x0BFF) ta++;
            else if (cp >= 0x0C80 && cp <= 0x0CFF) kn++;
            else if (cp >= 0x0B00 && cp <= 0x0B7F) or++;
        }
        int best = 0;
        int script = 0;
        if (ml > best) { best = ml; script = 1; }
        if (hi > best) { best = hi; script = 2; }
        if (te > best) { best = te; script = 3; }
        if (ta > best) { best = ta; script = 4; }
        if (kn > best) { best = kn; script = 5; }
        if (or > best) { best = or; script = 6; }
        return script;
    }

    private static Script script(String indep, String sign, String virama, String anusvara, String cons) {
        String[] iv = indep.split("\\|", -1);
        String[] sv = sign.split("\\|", -1);
        String[] cv = cons.split("\\|", -1);
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < CONS_KEYS.length && i < cv.length; i++) {
            map.put(CONS_KEYS[i], cv[i]);
        }
        map.put("ksh", map.get("k") + virama + map.get("sh"));
        return new Script(iv, sv, virama, anusvara, map);
    }

    private static final class Script {
        final String[] indep;
        final String[] sign;
        final String virama;
        final String anusvara;
        final Map<String, String> cons;

        Script(String[] indep, String[] sign, String virama, String anusvara, Map<String, String> cons) {
            this.indep = indep;
            this.sign = sign;
            this.virama = virama;
            this.anusvara = anusvara;
            this.cons = cons;
        }
    }

    private static final class Vowel {
        final int index;
        final int len;

        Vowel(int index, int len) {
            this.index = index;
            this.len = len;
        }
    }

    private static final class Cons {
        final String key;
        final int len;
        final boolean gem;

        Cons(String key, int len, boolean gem) {
            this.key = key;
            this.len = len;
            this.gem = gem;
        }
    }
}
