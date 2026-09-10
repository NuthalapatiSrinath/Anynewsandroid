package com.anyNews.anynews.Utils;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Gold &amp; Silver detail chrome keyed by {@link Constants#LanguageID}.
 * Never fall back to Telugu XML defaults for Hindi / ML / etc.
 */
public final class GoldLabels {

    private GoldLabels() {}

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
    public static String title(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "സ്വർണം & വെള്ളി വിലകൾ";
            case 2: return "सोना & चांदी की कीमतें";
            case 3: return "బంగారం & వెండి ధరలు";
            case 4: return "தங்கம் & வெள்ளி விலைகள்";
            case 5: return "ಚಿನ್ನ & ಬೆಳ್ಳಿ ಬೆಲೆಗಳು";
            case 6: return "ସୁନା & ରୂପା ମୂଲ୍ୟ";
            default: return "Gold & Silver Rates";
        }
    }

    @NonNull
    public static String dateLabel(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "തീയതി";
            case 2: return "तारीख";
            case 3: return "తేది";
            case 4: return "தேதி";
            case 5: return "ದಿನಾಂಕ";
            case 6: return "ତାରିଖ";
            default: return "Date";
        }
    }

    @NonNull
    public static String cardTitle(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "സ്വർണ വിലകൾ";
            case 2: return "सोने की कीमतें";
            case 3: return "బంగారం ధరలు";
            case 4: return "தங்க விலைகள்";
            case 5: return "ಚಿನ್ನದ ಬೆಲೆಗಳು";
            case 6: return "ସୁନା ମୂଲ୍ୟ";
            default: return "Gold Rates";
        }
    }

    @NonNull
    public static String silverCardTitle(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "വെള്ളി വിലകൾ";
            case 2: return "चांदी की कीमतें";
            case 3: return "వెండి ధరలు";
            case 4: return "வெள்ளி விலைகள்";
            case 5: return "ಬೆಳ್ಳಿ ಬೆಲೆಗಳು";
            case 6: return "ରୂପା ମୂଲ୍ୟ";
            default: return "Silver Rates";
        }
    }

    @NonNull
    public static String colDetails(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "വിവരങ്ങൾ";
            case 2: return "विवरण";
            case 3: return "వివరాలు";
            case 4: return "விவரங்கள்";
            case 5: return "ವಿವರಗಳು";
            case 6: return "ବିବରଣୀ";
            default: return "Details";
        }
    }

    @NonNull
    public static String colPrice(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "വില (₹)";
            case 2: return "कीमत (₹)";
            case 3: return "ధర (₹)";
            case 4: return "விலை (₹)";
            case 5: return "ಬೆಲೆ (₹)";
            case 6: return "ମୂଲ୍ୟ (₹)";
            default: return "Price (₹)";
        }
    }

    @NonNull
    public static String colChange(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "മാറ്റം";
            case 2: return "बदलाव";
            case 3: return "మార్పు";
            case 4: return "மாற்றம்";
            case 5: return "ಬದಲಾವಣೆ";
            case 6: return "ପରିବର୍ତ୍ତନ";
            default: return "Change";
        }
    }

    @NonNull
    public static String chartCta(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "ചരിത്ര വില ചാർട്ട് കാണുക";
            case 2: return "ऐतिहासिक मूल्य चार्ट देखें";
            case 3: return "చరిత్రాత్మక ధరల చార్ట్ చూడండి";
            case 4: return "வரலாற்று விலை விளக்கப்படத்தைப் பார்க்க";
            case 5: return "ಐತಿಹಾಸಿಕ ಬೆಲೆ ಚಾರ್ಟ್ ನೋಡಿ";
            case 6: return "ଐତିହାସିକ ମୂଲ୍ୟ ଚାର୍ଟ ଦେଖନ୍ତୁ";
            default: return "View historical price chart";
        }
    }

    @NonNull
    public static String chartCtaHide(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "ചാർട്ട് മറയ്ക്കുക";
            case 2: return "चार्ट छिपाएँ";
            case 3: return "చార్ట్ దాచండి";
            case 4: return "விளக்கப்படத்தை மறை";
            case 5: return "ಚಾರ್ಟ್ ಮರೆಮಾಡಿ";
            case 6: return "ଚାର୍ଟ ଲୁଚାନ୍ତୁ";
            default: return "Hide chart";
        }
    }

    @NonNull
    public static String chartEmpty(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "ചാർട്ട് ഡാറ്റ ലഭ്യമല്ല";
            case 2: return "चार्ट डेटा उपलब्ध नहीं";
            case 3: return "చార్ట్ డేటా అందుబాటులో లేదు";
            case 4: return "விளக்கப்படத் தரவு இல்லை";
            case 5: return "ಚಾರ್ಟ್ ಡೇಟಾ ಲಭ್ಯವಿಲ್ಲ";
            case 6: return "ଚାର୍ଟ ତଥ୍ୟ ଉପଲବ୍ଧ ନାହିଁ";
            default: return "Chart data unavailable";
        }
    }

    @NonNull
    public static String dateNoData(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "ഈ തീയതിക്ക് വിലകൾ ലഭ്യമല്ല";
            case 2: return "इस तारीख के लिए कीमतें उपलब्ध नहीं";
            case 3: return "ఈ తేదీకి ధరలు అందుబాటులో లేవు";
            case 4: return "இந்த தேதிக்கு விலைகள் இல்லை";
            case 5: return "ಈ ದಿನಾಂಕಕ್ಕೆ ಬೆಲೆಗಳು ಲಭ್ಯವಿಲ್ಲ";
            case 6: return "ଏହି ତାରିଖ ପାଇଁ ମୂଲ୍ୟ ଉପଲବ୍ଧ ନାହିଁ";
            default: return "No rates for this date";
        }
    }

    @NonNull
    public static String historyTitleGold(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "7 ദിവസ വില ട്രെൻഡ് (24K)";
            case 2: return "7 दिन की कीमत प्रवृत्ति (24K)";
            case 3: return "7 రోజుల ధరల ట్రెండ్ (24K)";
            case 4: return "7 நாள் விலை போக்கு (24K)";
            case 5: return "7 ದಿನದ ಬೆಲೆ ಪ್ರವೃತ್ತಿ (24K)";
            case 6: return "7 ଦିନର ମୂଲ୍ୟ ଟ୍ରେଣ୍ଡ (24K)";
            default: return "7-day price trend (24K)";
        }
    }

    @NonNull
    public static String historyTitleSilver(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "7 ദിവസ വില ട്രെൻഡ് (വെള്ളി 999)";
            case 2: return "7 दिन की कीमत प्रवृत्ति (चांदी 999)";
            case 3: return "7 రోజుల ధరల ట్రెండ్ (వెండి 999)";
            case 4: return "7 நாள் விலை போக்கு (வெள்ளி 999)";
            case 5: return "7 ದಿನದ ಬೆಲೆ ಪ್ರವೃತ್ತಿ (ಬೆಳ್ಳಿ 999)";
            case 6: return "7 ଦିନର ମୂଲ୍ୟ ଟ୍ରେଣ୍ଡ (ରୂପା 999)";
            default: return "7-day price trend (Silver 999)";
        }
    }

    @NonNull
    public static String weight(@Nullable Context c, int grams) {
        if (grams >= 1000) {
            switch (lang(c)) {
                case 1: return "1 കിലോ";
                case 2: return "1 किलो";
                case 3: return "1 కిలో";
                case 4: return "1 கிலோ";
                case 5: return "1 ಕಿಲೋ";
                case 6: return "1 କିଲୋ";
                default: return "1 kg";
            }
        }
        switch (lang(c)) {
            case 1: return grams + " ഗ്രാം";
            case 2: return grams + " ग्राम";
            case 3: return grams == 1 ? "1 గ్రాము" : grams + " గ్రాములు";
            case 4: return grams + " கிராம்";
            case 5: return grams + " ಗ್ರಾಂ";
            case 6: return grams + " ଗ୍ରାମ";
            default: return grams + (grams == 1 ? " gram" : " grams");
        }
    }

    @NonNull
    public static String disclaimer(@Nullable Context c) {
        switch (lang(c)) {
            case 1:
                return "ശ്രദ്ധിക്കുക: ഈ വിലകൾ മാർക്കറ്റ് അനുസരിച്ച് മാറാം. കൃത്യമായ വിലയ്ക്ക് പ്രാദേശിക ജ്വല്ലറിയെ സമീപിക്കുക.";
            case 2:
                return "नोट: ये कीमतें बाज़ार के अनुसार बदल सकती हैं। सटीक दरों के लिए स्थानीय जौहरी से संपर्क करें।";
            case 3:
                return "గమనిక: ఈ ధరలు మార్కెట్ పరిస్థితులకు అనుసరించి మారవచ్చు. ఖచ్చితమైన ధరల కోసం స్థానిక ఆభరణాల దుకాణాలను సంప్రదించండి.";
            case 4:
                return "குறிப்பு: இந்த விலைகள் சந்தைக்கு ஏற்ப மாறலாம். துல்லியமான விலைக்கு உள்ளூர் நகையகத்தை அணுகவும்.";
            case 5:
                return "ಗಮನಿಸಿ: ಈ ಬೆಲೆಗಳು ಮಾರುಕಟ್ಟೆಗೆ ಅನುಗುಣವಾಗಿ ಬದಲಾಗಬಹುದು. ನಿಖರ ಬೆಲೆಗೆ ಸ್ಥಳೀಯ ಜ್ವೆಲ್ಲರ್ ಅನ್ನು ಸಂಪರ್ಕಿಸಿ.";
            case 6:
                return "ନୋଟ୍: ଏହି ମୂଲ୍ୟ ବଜାର ଅନୁଯାୟୀ ବଦଳିପାରେ। ସଠିକ୍ ମୂଲ୍ୟ ପାଇଁ ସ୍ଥାନୀୟ ଜୁଏଲର୍ ସହ ଯୋଗାଯୋଗ କରନ୍ତୁ।";
            default:
                return "Note: Rates may change with market conditions. Contact a local jeweller for exact prices.";
        }
    }

    @NonNull
    public static String ratesUnavailable(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "വിലകൾ ലഭ്യമല്ല";
            case 2: return "कीमतें उपलब्ध नहीं";
            case 3: return "ధరలు అందుబాటులో లేవు";
            case 4: return "விலைகள் இல்லை";
            case 5: return "ಬೆಲೆಗಳು ಲಭ್ಯವಿಲ್ಲ";
            case 6: return "ମୂଲ୍ୟ ଉପଲବ୍ଧ ନାହିଁ";
            default: return "Rates unavailable";
        }
    }

    /** True if splash chrome still looks like Telugu defaults while LanguageID is not Telugu. */
    public static boolean looksLikeTeluguChrome(@Nullable String s) {
        if (s == null || s.isEmpty()) return false;
        return s.indexOf('\u0C00') >= 0; // Telugu block
    }
}
