package com.anyNews.anynews.Utils;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Localize panchang / rashi display strings that ship as Telugu in the engine pack.
 * Uses {@link Constants#LanguageID}. Telugu (3) returns the source string unchanged.
 * Other languages (including 8 Bengali, 9 Marathi, 10 Gujarati) are translated
 * at display time; the year-pack payload stays Telugu.
 */
public final class PanchangLabels {

    private PanchangLabels() {}

    private static int lang(@Nullable Context c) {
        if (c == null) return 0;
        try {
            return SharedPrefsData.getInt(
                    c.getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME);
        } catch (Exception e) {
            return 0;
        }
    }

    private static final String[] NAK_TE = {
            "అశ్విని", "భరణి", "కృత్తిక", "రోహిణి", "మృగశిర", "ఆర్ద్ర", "పునర్వసు",
            "పుష్యమి", "ఆశ్లేష", "మఖ", "పుబ్బ", "ఉత్తర", "హస్త", "చిత్త", "స్వాతి",
            "విశాఖ", "అనురాధ", "జ్యేష్ఠ", "మూల", "పూ.షా", "ఉ.షా", "శ్రవణం",
            "ధనిష్ఠ", "శతభిష", "పూ.భా", "ఉ.భా", "రేవతి"
    };
    private static final String[] NAK_HI = {
            "अश्विनी", "भरणी", "कृत्तिका", "रोहिणी", "मृगशिरा", "आर्द्रा", "पुनर्वसु",
            "पुष्य", "आश्लेषा", "मघा", "पूर्वा फाल्गुनी", "उत्तरा फाल्गुनी", "हस्त", "चित्रा", "स्वाती",
            "विशाखा", "अनुराधा", "ज्येष्ठा", "मूल", "पूर्वाषाढ़ा", "उत्तराषाढ़ा", "श्रवण",
            "धनिष्ठा", "शतभिषा", "पूर्वा भाद्रपद", "उत्तरा भाद्रपद", "रेवती"
    };
    private static final String[] NAK_EN = {
            "Ashwini", "Bharani", "Krittika", "Rohini", "Mrigashira", "Ardra", "Punarvasu",
            "Pushya", "Ashlesha", "Magha", "Purva Phalguni", "Uttara Phalguni", "Hasta", "Chitra", "Swati",
            "Vishakha", "Anuradha", "Jyeshtha", "Mula", "Purva Ashadha", "Uttara Ashadha", "Shravana",
            "Dhanishta", "Shatabhisha", "Purva Bhadrapada", "Uttara Bhadrapada", "Revati"
    };
    private static final String[] NAK_ML = {
            "അശ്വതി", "ഭരണി", "കാർത്തിക", "രോഹിണി", "മകയിരം", "തിരുവാതിര", "പുണർതം",
            "പൂയം", "ആയില്യം", "മകം", "പൂരം", "ഉത്രം", "അത്തം", "ചിത്തിര", "ചോതി",
            "വിശാഖം", "അനിഴം", "തൃക്കേട്ട", "മൂലം", "പൂരാടം", "ഉത്രാടം", "തിരുവോണം",
            "അവിട്ടം", "ചതയം", "പൂരുരുട്ടാതി", "ഉത്രട്ടാതി", "രേവതി"
    };
    private static final String[] NAK_TA = {
            "அசுவினி", "பரணி", "கார்த்திகை", "ரோகிணி", "மிருகசீரிடம்", "திருவாதிரை", "புனர்பூசம்",
            "பூசம்", "ஆயில்யம்", "மகம்", "பூரம்", "உத்திரம்", "ஹஸ்தம்", "சித்திரை", "சுவாதி",
            "விசாகம்", "அனுஷம்", "கேட்டை", "மூலம்", "பூராடம்", "உத்திராடம்", "திருவோணம்",
            "அவிட்டம்", "சதயம்", "பூரட்டாதி", "உத்திரட்டாதி", "ரேவதி"
    };
    private static final String[] NAK_KN = {
            "ಅಶ್ವಿನಿ", "ಭರಣಿ", "ಕೃತ್ತಿಕಾ", "ರೋಹಿಣಿ", "ಮೃಗಶಿರ", "ಆರ್ದ್ರಾ", "ಪುನರ್ವಸು",
            "ಪುಷ್ಯ", "ಆಶ್ಲೇಷಾ", "ಮಘಾ", "ಪೂರ್ವ ಫಾಲ್ಗುನಿ", "ಉತ್ತರ ಫಾಲ್ಗುನಿ", "ಹಸ್ತ", "ಚಿತ್ರಾ", "ಸ್ವಾತಿ",
            "ವಿಶಾಖಾ", "ಅನುರಾಧಾ", "ಜ್ಯೇಷ್ಠಾ", "ಮೂಲ", "ಪೂರ್ವಾಷಾಢಾ", "ಉತ್ತರಾಷಾಢಾ", "ಶ್ರವಣ",
            "ಧನಿಷ್ಠಾ", "ಶತಭಿಷಾ", "ಪೂರ್ವ ಭಾದ್ರಪದ", "ಉತ್ತರ ಭಾದ್ರಪದ", "ರೇವತಿ"
    };
    private static final String[] NAK_OR = {
            "ଅଶ୍ୱିନୀ", "ଭରଣୀ", "କୃତ୍ତିକା", "ରୋହିଣୀ", "ମୃଗଶିରା", "ଆର୍ଦ୍ରା", "ପୁନର୍ବସୁ",
            "ପୁଷ୍ୟା", "ଆଶ୍ଲେଷା", "ମଘା", "ପୂର୍ବା ଫାଲ୍ଗୁନୀ", "ଉତ୍ତରା ଫାଲ୍ଗୁନୀ", "ହସ୍ତା", "ଚିତ୍ରା", "ସ୍ୱାତୀ",
            "ବିଶାଖା", "ଅନୁରାଧା", "ଜ୍ୟେଷ୍ଠା", "ମୂଳା", "ପୂର୍ବାଷାଢ଼ା", "ଉତ୍ତରାଷାଢ଼ା", "ଶ୍ରବଣା",
            "ଧନିଷ୍ଠା", "ଶତଭିଷା", "ପୂର୍ବ ଭାଦ୍ରପଦ", "ଉତ୍ତର ଭାଦ୍ରପଦ", "ରେବତୀ"
    };
    private static final String[] NAK_BN = {
            "অশ্বিনী", "ভরণী", "কৃত্তিকা", "রোহিণী", "মৃগশিরা", "আর্দ্রা", "পুনর্বসু",
            "পুষ্যা", "অশ্লেষা", "মঘা", "পূর্ব ফল্গুনী", "উত্তর ফল্গুনী", "হস্তা", "চিত্রা", "স্বাতী",
            "বিশাখা", "অনুরাধা", "জ্যেষ্ঠা", "মূলা", "পূর্বাষাঢ়া", "উত্তরাষাঢ়া", "শ্রবণা",
            "ধনিষ্ঠা", "শতভিষা", "পূর্ব ভাদ্রপদ", "উত্তর ভাদ্রপদ", "রেবতী"
    };
    private static final String[] NAK_MR = {
            "अश्विनी", "भरणी", "कृत्तिका", "रोहिणी", "मृगशिरा", "आर्द्रा", "पुनर्वसु",
            "पुष्य", "आश्लेषा", "मघा", "पूर्वा फाल्गुनी", "उत्तरा फाल्गुनी", "हस्त", "चित्रा", "स्वाती",
            "विशाखा", "अनुराधा", "ज्येष्ठा", "मूळ", "पूर्वाषाढा", "उत्तराषाढा", "श्रवण",
            "धनिष्ठा", "शततारका", "पूर्वा भाद्रपदा", "उत्तरा भाद्रपदा", "रेवती"
    };
    private static final String[] NAK_GU = {
            "અશ્વિની", "ભરણી", "કૃત્તિકા", "રોહિણી", "મૃગશિર", "આર્દ્રા", "પુનર્વસુ",
            "પુષ્ય", "આશ્લેષા", "મઘા", "પૂર્વ ફાલ્ગુની", "ઉત્તર ફાલ્ગુની", "હસ્ત", "ચિત્રા", "સ્વાતિ",
            "વિશાખા", "અનુરાધા", "જ્યેષ્ઠા", "મૂળ", "પૂર્વાષાઢા", "ઉત્તરાષાઢા", "શ્રવણ",
            "ધનિષ્ઠા", "શતભિષા", "પૂર્વ ભાદ્રપદ", "ઉત્તર ભાદ્રપદ", "રેવતી"
    };

    private static final String[] TITHI_TE = {
            "పాడ్యమి", "విదియ", "తదియ", "చవితి", "పంచమి", "షష్ఠి", "సప్తమి",
            "అష్టమి", "నవమి", "దశమి", "ఏకాదశి", "ద్వాదశి", "త్రయోదశి", "చతుర్దశి",
            "పూర్ణిమ", "అమావాస్య"
    };
    private static final String[] TITHI_HI = {
            "प्रतिपदा", "द्वितीया", "तृतीया", "चतुर्थी", "पंचमी", "षष्ठी", "सप्तमी",
            "अष्टमी", "नवमी", "दशमी", "एकादशी", "द्वादशी", "त्रयोदशी", "चतुर्दशी",
            "पूर्णिमा", "अमावस्या"
    };
    private static final String[] TITHI_EN = {
            "Pratipada", "Dwitiya", "Tritiya", "Chaturthi", "Panchami", "Shashthi", "Saptami",
            "Ashtami", "Navami", "Dashami", "Ekadashi", "Dwadashi", "Trayodashi", "Chaturdashi",
            "Purnima", "Amavasya"
    };

    private static final String[] MONTH_EN = {
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
    };
    private static final String[] MONTH_HI = {
            "जनवरी", "फरवरी", "मार्च", "अप्रैल", "मई", "जून",
            "जुलाई", "अगस्त", "सितंबर", "अक्टूबर", "नवंबर", "दिसंबर"
    };
    private static final String[] MONTH_TE = {
            "జనవరి", "ఫిబ్రవరి", "మార్చి", "ఏప్రిల్", "మే", "జూన్",
            "జూలై", "ఆగష్టు", "సెప్టెంబర్", "అక్టోబర్", "నవంబర్", "డిసెంబర్"
    };
    private static final String[] MONTH_ML = {
            "ജനുവരി", "ഫെബ്രുവരി", "മാർച്ച്", "ഏപ്രിൽ", "മേയ്", "ജൂൺ",
            "ജൂലൈ", "ഓഗസ്റ്റ്", "സെപ്റ്റംബർ", "ഒക്ടോബർ", "നവംബർ", "ഡിസംബർ"
    };
    private static final String[] MONTH_TA = {
            "ஜனவரி", "பிப்ரவரி", "மார்ச்", "ஏப்ரல்", "மே", "ஜூன்",
            "ஜூலை", "ஆகஸ்ட்", "செப்டம்பர்", "அக்டோபர்", "நவம்பர்", "டிசம்பர்"
    };
    private static final String[] MONTH_KN = {
            "ಜನವರಿ", "ಫೆಬ್ರವರಿ", "ಮಾರ್ಚ್", "ಏಪ್ರಿಲ್", "ಮೇ", "ಜೂನ್",
            "ಜುಲೈ", "ಆಗಸ್ಟ್", "ಸೆಪ್ಟೆಂಬರ್", "ಅಕ್ಟೋಬರ್", "ನವೆಂಬರ್", "ಡಿಸೆಂಬರ್"
    };
    private static final String[] MONTH_OR = {
            "ଜାନୁଆରୀ", "ଫେବୃଆରୀ", "ମାର୍ଚ୍ଚ", "ଏପ୍ରିଲ୍", "ମଇ", "ଜୁନ୍",
            "ଜୁଲାଇ", "ଅଗଷ୍ଟ", "ସେପ୍ଟେମ୍ବର", "ଅକ୍ଟୋବର", "ନଭେମ୍ବର", "ଡିସେମ୍ବର"
    };

    @NonNull
    public static String nakshatra(@Nullable Context c, @Nullable String teName) {
        if (TextUtils.isEmpty(teName)) return "—";
        int id = lang(c);
        if (id == 3) return teName;
        int idx = indexOfIgnoreSpace(NAK_TE, teName);
        if (idx < 0) return teName;
        switch (id) {
            case 1: return NAK_ML[idx];
            case 2: return NAK_HI[idx];
            case 4: return NAK_TA[idx];
            case 5: return NAK_KN[idx];
            case 6: return NAK_OR[idx];
            case 8: return NAK_BN[idx];
            case 9: return NAK_MR[idx];
            case 10: return NAK_GU[idx];
            default: return NAK_EN[idx];
        }
    }

    @NonNull
    public static String tithi(@Nullable Context c, @Nullable String teName) {
        if (TextUtils.isEmpty(teName)) return "—";
        int id = lang(c);
        if (id == 3) return teName;
        int idx = indexOfIgnoreSpace(TITHI_TE, teName);
        if (idx < 0) {
            // Engine uses పూర్ణిమ for full moon; amavasya may appear as text.
            if (teName.contains("అమావాస్య") || teName.toLowerCase().contains("amavasya")) {
                idx = 15;
            } else {
                return teName;
            }
        }
        switch (id) {
            case 1:
                return new String[]{
                        "പ്രതിപദ", "ദ്വിതീയ", "തൃതീയ", "ചതുർഥി", "പഞ്ചമി", "ഷഷ്ഠി", "സപ്തമി",
                        "അഷ്ടമി", "നവമി", "ദശമി", "ഏകാദശി", "ദ്വാദശി", "ത്രയോദശി", "ചതുർദശി",
                        "പൂർണിമ", "അമാവാസ്യ"
                }[idx];
            case 2: return TITHI_HI[idx];
            case 4:
                return new String[]{
                        "பிரதமை", "துவிதியை", "திருதியை", "சதுர்த்தி", "பஞ்சமி", "சஷ்டி", "சப்தமி",
                        "அஷ்டமி", "நவமி", "தசமி", "ஏகாதசி", "துவாதசி", "திரயோதசி", "சதுர்த்தசி",
                        "பௌர்ணமி", "அமாவாசை"
                }[idx];
            case 5:
                return new String[]{
                        "ಪಾಡ್ಯ", "ದ್ವಿತೀಯ", "ತೃತೀಯ", "ಚತುರ್ಥಿ", "ಪಂಚಮಿ", "ಷಷ್ಠಿ", "ಸಪ್ತಮಿ",
                        "ಅಷ್ಟಮಿ", "ನವಮಿ", "ದಶಮಿ", "ಏಕಾದಶಿ", "ದ್ವಾದಶಿ", "ತ್ರಯೋದಶಿ", "ಚತುರ್ದಶಿ",
                        "ಪೂರ್ಣಿಮಾ", "ಅಮಾವಾಸ್ಯೆ"
                }[idx];
            case 6:
                return new String[]{
                        "ପ୍ରତିପଦ", "ଦ୍ୱିତୀୟା", "ତୃତୀୟା", "ଚତୁର୍ଥୀ", "ପଞ୍ଚମୀ", "ଷଷ୍ଠୀ", "ସପ୍ତମୀ",
                        "ଅଷ୍ଟମୀ", "ନବମୀ", "ଦଶମୀ", "ଏକାଦଶୀ", "ଦ୍ୱାଦଶୀ", "ତ୍ରୟୋଦଶୀ", "ଚତୁର୍ଦଶୀ",
                        "ପୂର୍ଣ୍ଣିମା", "ଅମାବାସ୍ୟା"
                }[idx];
            case 8:
                return new String[]{
                        "প্রতিপদ", "দ্বিতীয়া", "তৃতীয়া", "চতুর্থী", "পঞ্চমী", "ষষ্ঠী", "সপ্তমী",
                        "অষ্টমী", "নবমী", "দশমী", "একাদশী", "দ্বাদশী", "ত্রয়োদশী", "চতুর্দশী",
                        "পূর্ণিমা", "অমাবস্যা"
                }[idx];
            case 9:
                return new String[]{
                        "प्रतिपदा", "द्वितीया", "तृतीया", "चतुर्थी", "पंचमी", "षष्ठी", "सप्तमी",
                        "अष्टमी", "नवमी", "दशमी", "एकादशी", "द्वादशी", "त्रयोदशी", "चतुर्दशी",
                        "पौर्णिमा", "अमावस्या"
                }[idx];
            case 10:
                return new String[]{
                        "પ્રતિપદા", "દ્વિતીયા", "તૃતીયા", "ચતુર્થી", "પંચમી", "ષષ્ઠી", "સપ્તમી",
                        "અષ્ટમી", "નવમી", "દશમી", "એકાદશી", "દ્વાદશી", "ત્રયોદશી", "ચતુર્દશી",
                        "પૂર્ણિમા", "અમાસ"
                }[idx];
            default: return TITHI_EN[idx];
        }
    }

    @NonNull
    public static String monthName(@Nullable Context c, int month0) {
        int m = Math.max(0, Math.min(11, month0));
        switch (lang(c)) {
            case 1: return MONTH_ML[m];
            case 2: return MONTH_HI[m];
            case 3: return MONTH_TE[m];
            case 4: return MONTH_TA[m];
            case 5: return MONTH_KN[m];
            case 6: return MONTH_OR[m];
            case 8: return new String[]{
                    "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
                    "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"
            }[m];
            case 9: return new String[]{
                    "जानेवारी", "फेब्रुवारी", "मार्च", "एप्रिल", "मे", "जून",
                    "जुलै", "ऑगस्ट", "सप्टेंबर", "ऑक्टोबर", "नोव्हेंबर", "डिसेंबर"
            }[m];
            case 10: return new String[]{
                    "જાન્યુઆરી", "ફેબ્રુઆરી", "માર્ચ", "એપ્રિલ", "મે", "જૂન",
                    "જુલાઈ", "ઑગસ્ટ", "સપ્ટેમ્બર", "ઑક્ટોબર", "નવેમ્બર", "ડિસેમ્બર"
            }[m];
            default: return MONTH_EN[m];
        }
    }

    @NonNull
    public static String weekday(@Nullable Context c, int calendarDayOfWeek) {
        switch (lang(c)) {
            case 1:
                switch (calendarDayOfWeek) {
                    case java.util.Calendar.SUNDAY: return "ഞായർ";
                    case java.util.Calendar.MONDAY: return "തിങ്കൾ";
                    case java.util.Calendar.TUESDAY: return "ചൊവ്വ";
                    case java.util.Calendar.WEDNESDAY: return "ബുധൻ";
                    case java.util.Calendar.THURSDAY: return "വ്യാഴം";
                    case java.util.Calendar.FRIDAY: return "വെള്ളി";
                    case java.util.Calendar.SATURDAY: return "ശനി";
                    default: return "";
                }
            case 2:
                switch (calendarDayOfWeek) {
                    case java.util.Calendar.SUNDAY: return "रविवार";
                    case java.util.Calendar.MONDAY: return "सोमवार";
                    case java.util.Calendar.TUESDAY: return "मंगलवार";
                    case java.util.Calendar.WEDNESDAY: return "बुधवार";
                    case java.util.Calendar.THURSDAY: return "गुरुवार";
                    case java.util.Calendar.FRIDAY: return "शुक्रवार";
                    case java.util.Calendar.SATURDAY: return "शनिवार";
                    default: return "";
                }
            case 3:
                switch (calendarDayOfWeek) {
                    case java.util.Calendar.SUNDAY: return "ఆదివారం";
                    case java.util.Calendar.MONDAY: return "సోమవారం";
                    case java.util.Calendar.TUESDAY: return "మంగళవారం";
                    case java.util.Calendar.WEDNESDAY: return "బుధవారం";
                    case java.util.Calendar.THURSDAY: return "గురువారం";
                    case java.util.Calendar.FRIDAY: return "శుక్రవారం";
                    case java.util.Calendar.SATURDAY: return "శనివారం";
                    default: return "";
                }
            case 4:
                switch (calendarDayOfWeek) {
                    case java.util.Calendar.SUNDAY: return "ஞாயிறு";
                    case java.util.Calendar.MONDAY: return "திங்கள்";
                    case java.util.Calendar.TUESDAY: return "செவ்வாய்";
                    case java.util.Calendar.WEDNESDAY: return "புதன்";
                    case java.util.Calendar.THURSDAY: return "வியாழன்";
                    case java.util.Calendar.FRIDAY: return "வெள்ளி";
                    case java.util.Calendar.SATURDAY: return "சனி";
                    default: return "";
                }
            case 5:
                switch (calendarDayOfWeek) {
                    case java.util.Calendar.SUNDAY: return "ಭಾನುವಾರ";
                    case java.util.Calendar.MONDAY: return "ಸೋಮವಾರ";
                    case java.util.Calendar.TUESDAY: return "ಮಂಗಳವಾರ";
                    case java.util.Calendar.WEDNESDAY: return "ಬುಧವಾರ";
                    case java.util.Calendar.THURSDAY: return "ಗುರುವಾರ";
                    case java.util.Calendar.FRIDAY: return "ಶುಕ್ರವಾರ";
                    case java.util.Calendar.SATURDAY: return "ಶನಿವಾರ";
                    default: return "";
                }
            case 6:
                switch (calendarDayOfWeek) {
                    case java.util.Calendar.SUNDAY: return "ରବିବାର";
                    case java.util.Calendar.MONDAY: return "ସୋମବାର";
                    case java.util.Calendar.TUESDAY: return "ମଙ୍ଗଳବାର";
                    case java.util.Calendar.WEDNESDAY: return "ବୁଧବାର";
                    case java.util.Calendar.THURSDAY: return "ଗୁରୁବାର";
                    case java.util.Calendar.FRIDAY: return "ଶୁକ୍ରବାର";
                    case java.util.Calendar.SATURDAY: return "ଶନିବାର";
                    default: return "";
                }
            default:
                switch (calendarDayOfWeek) {
                    case java.util.Calendar.SUNDAY: return "Sunday";
                    case java.util.Calendar.MONDAY: return "Monday";
                    case java.util.Calendar.TUESDAY: return "Tuesday";
                    case java.util.Calendar.WEDNESDAY: return "Wednesday";
                    case java.util.Calendar.THURSDAY: return "Thursday";
                    case java.util.Calendar.FRIDAY: return "Friday";
                    case java.util.Calendar.SATURDAY: return "Saturday";
                    default: return "";
                }
        }
    }

    @NonNull
    public static String endTimePrefix(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "അവസാനം: ";
            case 2: return "समाप्ति: ";
            case 3: return "ముగింపు: ";
            case 4: return "முடிவு: ";
            case 5: return "ಅಂತ್ಯ: ";
            case 6: return "ଶେଷ: ";
            default: return "Ends: ";
        }
    }

    @NonNull
    public static String sunrise(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "സൂര്യോദയം";
            case 2: return "सूर्योदय";
            case 3: return "సూర్యోదయం";
            case 4: return "சூரிய उदयம்";
            case 5: return "ಸೂರ್ಯೋದಯ";
            case 6: return "ସୂର୍ଯ୍ୟୋଦୟ";
            default: return "Sunrise";
        }
    }

    @NonNull
    public static String sunset(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "സൂര്യാസ്തമയം";
            case 2: return "सूर्यास्त";
            case 3: return "సూర్యాస్తమయం";
            case 4: return "சூரிய அஸ்தமனம்";
            case 5: return "ಸೂರ್ಯಾಸ್ತ";
            case 6: return "ସୂର୍ଯ୍ୟାସ୍ତ";
            default: return "Sunset";
        }
    }

    @NonNull
    public static String financeBody(@Nullable Context c, @NonNull String numbers) {
        switch (lang(c)) {
            case 1:
                return "ഇന്നത്തെ അദൃഷ്ട സംഖ്യകൾ " + numbers
                        + ". ചെലവിൽ ജാഗ്രത പാലിക്കുക; ആവശ്യമുള്ള കാര്യങ്ങൾക്ക് മാത്രം പണം മാറ്റിവയ്ക്കുക.";
            case 2:
                return "आज के भाग्यशाली अंक " + numbers
                        + "। खर्च में सावधानी रखें; ज़रूरी कामों के लिए ही पैसे रखें।";
            case 3:
                return "ఈరోజు అదృష్ట సంఖ్యలు " + numbers
                        + ". ఖర్చుల్లో జాగ్రత్త పాటించండి; అవసరమైన పనులకే డబ్బు కేటాయించండి.";
            case 4:
                return "இன்றைய அதிர்ஷ்ட எண்கள் " + numbers
                        + ". செலவில் கவனம்; தேவையான பணிகளுக்கு மட்டும் பணம் ஒதுக்குங்கள்.";
            case 5:
                return "ಇಂದಿನ ಅದೃಷ್ಟ ಸಂಖ್ಯೆಗಳು " + numbers
                        + ". ಖರ್ಚಿನಲ್ಲಿ ಜಾಗ್ರತೆ; ಅಗತ್ಯ ಕೆಲಸಗಳಿಗೆ ಮಾತ್ರ ಹಣ ಮೀಸಲಿಡಿ.";
            case 6:
                return "ଆଜିର ଭାଗ୍ୟ ସଂଖ୍ୟା " + numbers
                        + "। ଖର୍ଚ୍ଚରେ ସାବଧାନ; ଆବଶ୍ୟକ କାମ ପାଇଁ ମାତ୍ର ଟଙ୍କା ରଖନ୍ତୁ।";
            default:
                return "Lucky numbers today: " + numbers
                        + ". Be careful with spending; allocate money only for necessary tasks.";
        }
    }

    @NonNull
    public static String healthBody(@Nullable Context c, @NonNull String dir) {
        switch (lang(c)) {
            case 1:
                return "ആരോഗ്യത്തിന് ലഘു വ്യായാമം, വെള്ളം, സമയത്തുള്ള വിശ്രമം ആവശ്യം. അദൃഷ്ട ദിശ: "
                        + dir + ".";
            case 2:
                return "स्वास्थ्य के लिए हल्का व्यायाम, पानी और समय पर आराम ज़रूरी। शुभ दिशा: "
                        + dir + "।";
            case 3:
                return "ఆరోగ్యం కోసం తేలిక వ్యాయామం, నీరు, సమయానికి విశ్రాంతి అవసరం. అదృష్ట దిక్కు: "
                        + dir + ".";
            case 4:
                return "உடல்நலனுக்கு லேசான உடற்பயிற்சி, நீர், சரியான நேரத்தில் ஓய்வு தேவை. அதிர்ஷ்ட திசை: "
                        + dir + ".";
            case 5:
                return "ಆರೋಗ್ಯಕ್ಕೆ ಹಗುರ ವ್ಯಾಯಾಮ, ನೀರು, ಸಮಯಕ್ಕೆ ವಿಶ್ರಾಂತಿ ಅಗತ್ಯ. ಅದೃಷ್ಟ ದಿಕ್ಕು: "
                        + dir + ".";
            case 6:
                return "ସ୍ୱାସ୍ଥ୍ୟ ପାଇଁ ହାଲୁକା ବ୍ୟାୟାମ, ପାଣି, ସମୟରେ ବିଶ୍ରାମ ଦରକାର। ଭାଗ୍ୟ ଦିଗ: "
                        + dir + "।";
            default:
                return "For health: light exercise, water, and rest on time. Lucky direction: "
                        + dir + ".";
        }
    }

    @NonNull
    public static String remedyBody(@Nullable Context c, @NonNull String color) {
        switch (lang(c)) {
            case 1:
                return "ശുഭ നിറം " + color
                        + " ധരിക്കുക; നിശ്ശബ്ദ പ്രാർത്ഥനയോ നന്ദിയോടെ ദിവസം അവസാനിപ്പിക്കുക.";
            case 2:
                return "शुभ रंग " + color
                        + " पहनें; शांत प्रार्थना या कृतज्ञता के साथ दिन समाप्त करें।";
            case 3:
                return "శుభ రంగు " + color
                        + "ను ధరించండి; నిశ్శబ్ద ప్రార్థన లేదా కృతజ్ఞతతో రోజును ముగించండి.";
            case 4:
                return "சுப நிறம் " + color
                        + " அணியுங்கள்; அமைதியான பிரார்த்தை அல்லது நன்றியுடன் நாளை முடியுங்கள்.";
            case 5:
                return "ಶುಭ ಬಣ್ಣ " + color
                        + " ಧರಿಸಿ; ನಿಶ್ಶಬ್ದ ಪ್ರಾರ್ಥನೆ ಅಥವಾ ಕೃತಜ್ಞತೆಯೊಂದಿಗೆ ದಿನ ಮುಗಿಸಿ.";
            case 6:
                return "ଶୁଭ ରଙ୍ଗ " + color
                        + " ପିନ୍ଧନ୍ତୁ; ଶାନ୍ତ ପ୍ରାର୍ଥନା କିମ୍ବା କୃତଜ୍ଞତା ସହ ଦିନ ଶେଷ କରନ୍ତୁ।";
            default:
                return "Wear auspicious color " + color
                        + "; end the day with quiet prayer or gratitude.";
        }
    }

    /**
     * Year line under the month title. Telugu (3) is returned unchanged.
     */
    @NonNull
    public static String localizeHeader(@Nullable Context c, @Nullable String te) {
        if (te == null) return "";
        if (lang(c) == 3 || te.isEmpty()) return te;
        return PanchangText.header(lang(c), te);
    }

    /**
     * One calendar day-cell line (tithi or nakshatra plus its end time).
     */
    @NonNull
    public static String localizeCellLine(@Nullable Context c, @Nullable String line) {
        if (line == null) return "";
        int id = lang(c);
        if (id == 3 || line.isEmpty()) return line;
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(
                "(రా\\.|ఉ\\.|మ\\.|సా\\.)\\s*(\\d{1,2}\\.\\d{2})\\s*$").matcher(line);
        if (!m.find()) return localizeName(c, line.trim());
        String name = line.substring(0, m.start()).trim();
        String time = line.substring(m.start()).trim();
        String shown = PanchangText.nakshatraShort(id, name);
        if (shown == null) shown = localizeName(c, name);
        return shown + " " + localizeTimePhrase(c, time);
    }

    /**
     * Muhurta label, tithi, nakshatra, or festival name from the Telugu pack.
     */
    @NonNull
    public static String localizeName(@Nullable Context c, @Nullable String te) {
        if (te == null) return "";
        int id = lang(c);
        if (id == 3 || te.isEmpty()) return te;
        String named = PanchangText.name(id, te);
        if (!named.equals(te)) return named;
        String ti = tithi(c, te);
        if (!ti.equals(te)) return ti;
        String nk = nakshatra(c, te);
        if (!"—".equals(nk) && !nk.equals(te)) return nk;
        return te;
    }

    /**
     * Clock phrase. Numbers stay; Telugu day-part words and from/until connectors change.
     */
    @NonNull
    public static String localizeTimePhrase(@Nullable Context c, @Nullable String te) {
        if (te == null) return "";
        if (lang(c) == 3 || te.isEmpty()) return te;
        return PanchangText.timePhrase(lang(c), te);
    }

    private static int indexOfIgnoreSpace(@NonNull String[] arr, @NonNull String name) {
        String n = name.trim();
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].equals(n) || arr[i].replace(" ", "").equals(n.replace(" ", ""))) {
                return i;
            }
        }
        // Prefix match for short forms like పూ.షా
        for (int i = 0; i < arr.length; i++) {
            if (n.startsWith(arr[i]) || arr[i].startsWith(n)) return i;
        }
        return -1;
    }
}
