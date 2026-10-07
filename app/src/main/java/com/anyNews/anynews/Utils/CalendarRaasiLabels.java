package com.anyNews.anynews.Utils;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.anyNews.anynews.R;
import com.anyNews.anynews.panchang.RaasiContent;

/**
 * Localized chrome labels for Telugu Calendar + Raasi flip (not device Locale).
 * Uses {@link Constants#LanguageID}: 1 ML, 2 HI, 3 TE, 4 TA, 5 KN, 6 OR,
 * 7 EN, 8 BN, 9 MR, 10 GU.
 * Panchang pack text is translated in {@link PanchangLabels}; Telugu stays the source.
 */
public final class CalendarRaasiLabels {

    private CalendarRaasiLabels() {}

    public static String swipeHint(@Nullable Context context) {
        // Legacy swipe affordance retired — Rashi is a top-segment option.
        return segRaasiPhalalu(context);
    }

    /** Top segment left: Panchangam (calendar day page). */
    public static String segPanchangam(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "പഞ്ചാംഗം";
            case 2: return "पंचांग";
            case 3: return "పంచాంగం";
            case 4: return "பஞ்சாங்கம்";
            case 5: return "ಪಂಚಾಂಗ";
            case 6: return "ପଞ୍ଚାଙ୍ଗ";
            default: return str(context, R.string.calendar_seg_panchangam, "Panchangam");
        }
    }

    /** Top segment right: Raasi Phalalu (replaces former TTD segment). */
    public static String segRaasiPhalalu(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "രാശിഫലം";
            case 2: return "राशिफल";
            case 3: return "రాశి ఫలాలు";
            case 4: return "ராசிபலன்";
            case 5: return "ರಾಶಿಫಲ";
            case 6: return "ରାଶିଫଳ";
            default: return str(context, R.string.calendar_seg_raasi_phalalu, "Raasi Phalalu");
        }
    }

    public static String pickRaasi(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "നിങ്ങളുടെ രാശിഫലം";
            case 2: return "आपकी राशिफल";
            case 3: return "మీ రాశి ఫలితాలు";
            case 4: return "உங்கள் ராசிபலன்";
            case 5: return "ನಿಮ್ಮ ರಾಶಿಫಲ";
            case 6: return "ଆପଣଙ୍କ ରାଶିଫଳ";
            default: return str(context, R.string.telugu_calendar_pick_raasi, "Your Rashi Phalalu");
        }
    }

    public static String dateLabel(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "തീയതി";
            case 2: return "तारीख";
            case 3: return "తేది";
            case 4: return "தேதி";
            case 5: return "ದಿನಾಂಕ";
            case 6: return "ତାରିଖ";
            default: return "Date";
        }
    }

    public static String yogaLabel(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "യോഗം";
            case 2: return "योग";
            case 3: return "యోగం";
            case 4: return "யோகம்";
            case 5: return "ಯೋಗ";
            case 6: return "ଯୋଗ";
            default: return "Yoga";
        }
    }

    public static String shubhaYogam(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "ശുഭ യോഗം";
            case 2: return "शुभ योग";
            case 3: return "శుభ యోగం";
            case 4: return "சுப யோகம்";
            case 5: return "ಶುಭ ಯೋಗ";
            case 6: return "ଶୁଭ ଯୋଗ";
            default: return "Shubha Yogam";
        }
    }

    public static String mishraYogam(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "മിശ്ര യോഗം";
            case 2: return "मिश्र योग";
            case 3: return "మిశ్ర యోగం";
            case 4: return "மிஸ்ர யோகம்";
            case 5: return "ಮಿಶ್ರ ಯೋಗ";
            case 6: return "ମିଶ୍ର ଯୋଗ";
            default: return "Mishra Yogam";
        }
    }

    public static String shubhamBhavatu(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "\"ശുഭം ഭവതു\"";
            case 2: return "\"शुभं भवतु\"";
            case 3: return "\"శుభం భవతు\"";
            case 4: return "\"சுபம் பவது\"";
            case 5: return "\"ಶುಭಂ ಭವತು\"";
            case 6: return "\"ଶୁଭଂ ଭବତୁ\"";
            default: return "\"Shubham Bhavatu\"";
        }
    }

    public static String lordLabel(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "അധിപൻ";
            case 2: return "अधिपति";
            case 3: return "అధిపతి";
            case 4: return "அதிபதி";
            case 5: return "ಅಧಿಪತಿ";
            case 6: return "ଅଧିପତି";
            default: return "Lord";
        }
    }

    public static String elementLabel(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "തത്ത്വം";
            case 2: return "तत्त्व";
            case 3: return "తత్త్వం";
            case 4: return "தத்துவம்";
            case 5: return "ತತ್ತ್ವ";
            case 6: return "ତତ୍ତ୍ୱ";
            default: return "Element";
        }
    }

    public static String gemLabel(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "രത്നം";
            case 2: return "रत्न";
            case 3: return "శుభ రత్నం";
            case 4: return "ரத்னம்";
            case 5: return "ರತ್ನ";
            case 6: return "ରତ୍ନ";
            default: return "Gem";
        }
    }

    public static String colorAttrLabel(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "നിറം";
            case 2: return "रंग";
            case 3: return "శుభ వర్ణం";
            case 4: return "நிறம்";
            case 5: return "ಬಣ್ಣ";
            case 6: return "ରଙ୍ଗ";
            default: return "Color";
        }
    }

    public static String sectionGeneral(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "സാധാരണ ഫലം";
            case 2: return "सामान्य फल";
            case 3: return "సాధారణ ఫలితాలు";
            case 4: return "பொது பலன்";
            case 5: return "ಸಾಮಾನ್ಯ ಫಲ";
            case 6: return "ସାଧାରଣ ଫଳ";
            default: return "General";
        }
    }

    public static String sectionFinance(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "സാമ്പത്തികം";
            case 2: return "वित्त";
            case 3: return "ఆర్థికం";
            case 4: return "நிதி";
            case 5: return "ಆರ್ಥಿಕ";
            case 6: return "ଆର୍ଥିକ";
            default: return "Finance";
        }
    }

    public static String sectionCareer(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "കരിയർ";
            case 2: return "करियर";
            case 3: return "కెరీర్";
            case 4: return "தொழில்";
            case 5: return "ವೃತ್ತಿ";
            case 6: return "କ୍ୟାରିଅର୍";
            default: return "Career";
        }
    }

    public static String sectionHealth(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "ആരോഗ്യം";
            case 2: return "स्वास्थ्य";
            case 3: return "ఆరోగ్యం";
            case 4: return "ஆரோக்கியம்";
            case 5: return "ಆರೋಗ್ಯ";
            case 6: return "ସ୍ୱାସ୍ଥ୍ୟ";
            default: return "Health";
        }
    }

    public static String sectionLove(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "സ്നേഹം & ബന്ധങ്ങൾ";
            case 2: return "प्रेम व संबंध";
            case 3: return "ప్రేమ & సంబంధాలు";
            case 4: return "காதல் & உறவுகள்";
            case 5: return "ಪ್ರೇಮ & ಸಂಬಂಧ";
            case 6: return "ପ୍ରେମ & ସମ୍ପର୍କ";
            default: return "Love & Relations";
        }
    }

    public static String sectionRemedy(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "പരിഹാരം";
            case 2: return "उपाय";
            case 3: return "పరిహారం";
            case 4: return "பரிகாரம்";
            case 5: return "ಪರಿಹಾರ";
            case 6: return "ପରିହାର";
            default: return "Remedy";
        }
    }

    /** Short title without date for detail card header. */
    public static String phalaluShortTitle(@Nullable Context context, int rashiId0) {
        String name = rashiName(context, rashiId0);
        switch (languageId(context)) {
            case 1: return name + " രാശിഫലം";
            case 2: return name + " राशिफल";
            case 3: return name + " రాశి ఫలితాలు";
            case 4: return name + " ராசிபலன்";
            case 5: return name + " ರಾಶಿಫಲ";
            case 6: return name + " ରାଶିଫଳ";
            default: return name + " horoscope";
        }
    }

    public static String knowRaasi(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "നിങ്ങളുടെ രാശി അറിയുക >";
            case 2: return "अपनी राशि जानें >";
            case 3: return "మీ రాశి తెలుసుకోండి >";
            case 4: return "உங்கள் ராசியை அறிய >";
            case 5: return "ನಿಮ್ಮ ರಾಶಿ ತಿಳಿಯಿರಿ >";
            case 6: return "ଆପଣଙ୍କ ରାଶି ଜାଣନ୍ତୁ >";
            default: return str(context, R.string.telugu_calendar_know_raasi, "Find your rashi >");
        }
    }

    public static String knowRaasiTitle(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "നിങ്ങളുടെ രാശി അറിയുക";
            case 2: return "अपनी राशि जानें";
            case 3: return "మీ రాశి తెలుసుకోండి";
            case 4: return "உங்கள் ராசியை அறியுங்கள்";
            case 5: return "ನಿಮ್ಮ ರಾಶಿ ತಿಳಿಯಿರಿ";
            case 6: return "ଆପଣଙ୍କ ରାଶି ଜାଣନ୍ତୁ";
            default: return str(context, R.string.telugu_calendar_know_raasi_title, "Find your rashi");
        }
    }

    public static String fullName(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "പൂർണ്ണ നാമം";
            case 2: return "पूरा नाम";
            case 3: return "పూర్తి పేరు";
            case 4: return "முழுப் பெயர்";
            case 5: return "ಪೂರ್ಣ ಹೆಸರು";
            case 6: return "ପୂର୍ଣ୍ଣ ନାମ";
            default: return str(context, R.string.telugu_calendar_full_name, "Full name");
        }
    }

    public static String dobTime(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "ജനന തിയതി (DD/MM/YYYY) / സമയം";
            case 2: return "जन्म तिथि (DD/MM/YYYY) / समय";
            case 3: return "పుట్టిన తేదీ (DD/MM/YYYY) / సమయం";
            case 4: return "பிறந்த தேதி (DD/MM/YYYY) / நேரம்";
            case 5: return "ಜನ್ಮ ದಿನಾಂಕ (DD/MM/YYYY) / ಸಮಯ";
            case 6: return "ଜନ୍ମ ତାରିଖ (DD/MM/YYYY) / ସମୟ";
            default: return str(context, R.string.telugu_calendar_dob_time, "DOB (DD/MM/YYYY) / time");
        }
    }

    public static String birthPlace(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "ജനന സ്ഥലം";
            case 2: return "जन्म स्थान";
            case 3: return "పుట్టిన ప్రదేశం";
            case 4: return "பிறந்த இடம்";
            case 5: return "ಜನ್ಮ ಸ್ಥಳ";
            case 6: return "ଜନ୍ମ ସ୍ଥାନ";
            default: return str(context, R.string.telugu_calendar_birth_place, "Place of birth");
        }
    }

    public static String submit(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "സമർപ്പിക്കുക";
            case 2: return "जमा करें";
            case 3: return "సమర్పించు";
            case 4: return "சமர்ப்பி";
            case 5: return "ಸಲ್ಲಿಸಿ";
            case 6: return "ଦାଖଲ କରନ୍ତୁ";
            default: return str(context, R.string.telugu_calendar_submit, "Submit");
        }
    }

    public static String more(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "…കൂടുതൽ";
            case 2: return "…और";
            case 3: return "…మరిన్ని";
            case 4: return "…மேலும்";
            case 5: return "…ಇನ್ನಷ್ಟು";
            case 6: return "…ଅଧିକ";
            default: return str(context, R.string.telugu_calendar_more, "…more");
        }
    }

    public static String less(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "കുറവ്";
            case 2: return "कम";
            case 3: return "తక్కువ";
            case 4: return "குறைவு";
            case 5: return "ಕಡಿಮೆ";
            case 6: return "କମ୍";
            default: return str(context, R.string.telugu_calendar_less, "Less");
        }
    }

    public static String luckyHeading(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "ഭാഗ്യ സൂചകങ്ങൾ";
            case 2: return "भाग्य सूचक";
            case 3: return "అదృష్ట సూచక అంశాలు";
            case 4: return "அதிர்ஷ்ட அம்சங்கள்";
            case 5: return "ಅದೃಷ್ಟ ಸೂಚಕಗಳು";
            case 6: return "ଭାଗ୍ୟ ସୂଚକ";
            default: return str(context, R.string.telugu_calendar_lucky_heading, "Lucky factors");
        }
    }

    public static String luckyColor(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "നിറം";
            case 2: return "रंग";
            case 3: return "రంగు";
            case 4: return "நிறம்";
            case 5: return "ಬಣ್ಣ";
            case 6: return "ରଙ୍ଗ";
            default: return str(context, R.string.telugu_calendar_lucky_color, "Color");
        }
    }

    public static String luckyNumbers(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "അക്കങ്ങൾ";
            case 2: return "अंक";
            case 3: return "సంఖ్యలు";
            case 4: return "எண்கள்";
            case 5: return "ಸಂಖ್ಯೆಗಳು";
            case 6: return "ସଂଖ୍ୟା";
            default: return str(context, R.string.telugu_calendar_lucky_numbers, "Numbers");
        }
    }

    public static String luckyDirection(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "ദിശ";
            case 2: return "दिशा";
            case 3: return "దిక్కు";
            case 4: return "திசை";
            case 5: return "ದಿಕ್ಕು";
            case 6: return "ଦିଗ";
            default: return str(context, R.string.telugu_calendar_lucky_direction, "Direction");
        }
    }

    public static String fullJatakam(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "പൂർണ്ണ ജാതകം അറിയുക";
            case 2: return "पूर्ण कुंडली जानें";
            case 3: return "పూర్తి జాతకం తెలుసుకోండి";
            case 4: return "முழு ஜாதகம் அறிய";
            case 5: return "ಪೂರ್ಣ ಜಾತಕ ತಿಳಿಯಿರಿ";
            case 6: return "ପୂର୍ଣ୍ଣ ଜାତକ ଜାଣନ୍ତୁ";
            default: return str(context, R.string.telugu_calendar_full_jatakam, "Know full jatakam");
        }
    }

    public static String festivalsTitle(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "ഉത്സവങ്ങൾ";
            case 2: return "त्योहार";
            case 3: return "పండుగలు";
            case 4: return "விழாக்கள்";
            case 5: return "ಹಬ್ಬಗಳು";
            case 6: return "ପର୍ବ";
            default: return str(context, R.string.telugu_calendar_festivals_title, "Festivals");
        }
    }

    public static String noFestivals(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "ഈ മാസം ഉത്സവങ്ങളില്ല";
            case 2: return "इस महीने कोई त्योहार नहीं";
            case 3: return "ఈ నెలలో పండుగలు లేవు";
            case 4: return "இம்மாதம் விழாக்கள் இல்லை";
            case 5: return "ಈ ತಿಂಗಳು ಹಬ್ಬಗಳಿಲ್ಲ";
            case 6: return "ଏହି ମାସରେ ପର୍ବ ନାହିଁ";
            default: return str(context, R.string.telugu_calendar_no_festivals, "No festivals this month");
        }
    }

    public static String raasiSaved(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "രാശി സംരക്ഷിച്ചു";
            case 2: return "राशि सहेजी गई";
            case 3: return "రాశి సేవ్ అయింది";
            case 4: return "ராசி சேமிக்கப்பட்டது";
            case 5: return "ರಾಶಿ ಉಳಿಸಲಾಗಿದೆ";
            case 6: return "ରାଶି ସେଭ୍ ହେଲା";
            default: return str(context, R.string.telugu_calendar_raasi_saved, "Rashi saved");
        }
    }

    /** Shown when Find-your-rashi submit has empty/unparseable DOB. */
    public static String dobRequired(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "ദയവായി ജനന തിയതി തിരഞ്ഞെടുക്കുക (DD/MM/YYYY)";
            case 2: return "कृपया जन्म तिथि चुनें (DD/MM/YYYY)";
            case 3: return "దయచేసి పుట్టిన తేదీ ఎంచుకోండి (DD/MM/YYYY)";
            case 4: return "பிறந்த தேதியைத் தேர்ந்தெடுக்கவும் (DD/MM/YYYY)";
            case 5: return "ದಯವಿಟ್ಟು ಜನ್ಮ ದಿನಾಂಕ ಆಯ್ಕೆಮಾಡಿ (DD/MM/YYYY)";
            case 6: return "ଦୟାକରି ଜନ୍ମ ତାରିଖ ବାଛନ୍ତୁ (DD/MM/YYYY)";
            default: return "Please select date of birth (DD/MM/YYYY)";
        }
    }

    public static String whatsappMissing(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "WhatsApp നമ്പർ അഡ്മിനിൽ സജ്ജമാക്കിയിട്ടില്ല";
            case 2: return "WhatsApp नंबर एडमिन में सेट नहीं है";
            case 3: return "WhatsApp నంబర్ అడ్మిన్‌లో సెట్ చేయలేదు";
            case 4: return "WhatsApp எண் அட்மினில் அமைக்கப்படவில்லை";
            case 5: return "WhatsApp ಸಂಖ್ಯೆ ನಿರ್ವಾಹಕರಲ್ಲಿ ಹೊಂದಿಸಿಲ್ಲ";
            case 6: return "WhatsApp ନମ୍ବର ଆଡମିନରେ ସେଟ୍ ହୋଇନାହିଁ";
            default: return str(context, R.string.telugu_calendar_whatsapp_missing,
                    "WhatsApp number not configured in admin");
        }
    }

    /** e.g. "1 of 2 Pages" — kept for completeness; UI hides these counters. */
    public static String pagesOf(@Nullable Context context, int current1Based, int total) {
        switch (languageId(context)) {
            case 1: return current1Based + " / " + total + " പേജുകൾ";
            case 2: return current1Based + " / " + total + " पृष्ठ";
            case 3: return current1Based + " / " + total + " పేజీలు";
            case 4: return current1Based + " / " + total + " பக்கங்கள்";
            case 5: return current1Based + " / " + total + " ಪುಟಗಳು";
            case 6: return current1Based + " / " + total + " ପୃଷ୍ଠା";
            default: return current1Based + " of " + total + " Pages";
        }
    }

    public static String pagesOf(int current1Based, int total) {
        return pagesOf(null, current1Based, total);
    }

    public static String shareDay(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "ദിവസ സമയങ്ങൾ പങ്കിടുക";
            case 2: return "दिन का समय साझा करें";
            case 3: return "రోజు సమయాలు షేర్ చేయండి";
            case 4: return "நாள் நேரங்களைப் பகிரவும்";
            case 5: return "ದಿನದ ಸಮಯಗಳನ್ನು ಹಂಚಿಕೊಳ್ಳಿ";
            case 6: return "ଦିନର ସମୟ ସେଆର୍ କରନ୍ତୁ";
            default: return str(context, R.string.telugu_calendar_share_day, "Share day timings");
        }
    }

    public static String shareRaasi(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "രാശിഫലം പങ്കിടുക";
            case 2: return "राशिफल साझा करें";
            case 3: return "రాశి ఫలాలు షేర్ చేయండి";
            case 4: return "ராசிபலன் பகிரவும்";
            case 5: return "ರಾಶಿಫಲ ಹಂಚಿಕೊಳ್ಳಿ";
            case 6: return "ରାଶିଫଳ ସେଆର୍ କରନ୍ତୁ";
            default: return str(context, R.string.telugu_calendar_share_raasi, "Share rashi phalalu");
        }
    }

    public static String shareChooser(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "പങ്കിടുക";
            case 2: return "साझा करें";
            case 3: return "షేర్";
            case 4: return "பகிர்";
            case 5: return "ಹಂಚಿಕೊಳ್ಳಿ";
            case 6: return "ସେଆର୍";
            default: return str(context, R.string.telugu_calendar_share_chooser, "Share");
        }
    }

    /** Joins dual Durmuhurtham / Varjyam ranges (Way2News “మరలా”). */
    public static String againConnector(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return " വീണ്ടും ";
            case 2: return " फिर ";
            case 3: return " మరలా ";
            case 4: return " மீண்டும் ";
            case 5: return " ಮತ್ತೆ ";
            case 6: return " ପୁଣି ";
            case 8: return " আবার ";
            case 9: return " पुन्हा ";
            case 10: return " ફરી ";
            default: return " again ";
        }
    }

    /** Gregorian month name for calendar header (0 = January). */
    @NonNull
    public static String monthName(@Nullable Context context, int month0) {
        int m = Math.max(0, Math.min(11, month0));
        switch (languageId(context)) {
            case 1: {
                String[] n = {"ജനുവരി", "ഫെബ്രുവരി", "മാർച്ച്", "ഏപ്രിൽ", "മേയ്", "ജൂൺ",
                        "ജൂലൈ", "ഓഗസ്റ്റ്", "സെപ്റ്റംബർ", "ഒക്ടോബർ", "നവംബർ", "ഡിസംബർ"};
                return n[m];
            }
            case 2: {
                String[] n = {"जनवरी", "फ़रवरी", "मार्च", "अप्रैल", "मई", "जून",
                        "जुलाई", "अगस्त", "सितंबर", "अक्टूबर", "नवंबर", "दिसंबर"};
                return n[m];
            }
            case 3: {
                String[] n = {"జనవరి", "ఫిబ్రవరి", "మార్చి", "ఏప్రిల్", "మే", "జూన్",
                        "జూలై", "ఆగస్టు", "సెప్టెంబర్", "అక్టోబర్", "నవంబర్", "డిసెంబర్"};
                return n[m];
            }
            case 4: {
                String[] n = {"ஜனவரி", "பிப்ரவரி", "மார்ச்", "ஏப்ரல்", "மே", "ஜூன்",
                        "ஜூலை", "ஆகஸ்ட்", "செப்டம்பர்", "அக்டோபர்", "நவம்பர்", "டிசம்பர்"};
                return n[m];
            }
            case 5: {
                String[] n = {"ಜನವರಿ", "ಫೆಬ್ರವರಿ", "ಮಾರ್ಚ್", "ಏಪ್ರಿಲ್", "ಮೇ", "ಜೂನ್",
                        "ಜುಲೈ", "ಆಗಸ್ಟ್", "ಸೆಪ್ಟೆಂಬರ್", "ಅಕ್ಟೋಬರ್", "ನವೆಂಬರ್", "ಡಿಸೆಂಬರ್"};
                return n[m];
            }
            case 6: {
                String[] n = {"ଜାନୁଆରୀ", "ଫେବୃଆରୀ", "ମାର୍ଚ୍ଚ", "ଅପ୍ରେଲ", "ମଇ", "ଜୁନ",
                        "ଜୁଲାଇ", "ଅଗଷ୍ଟ", "ସେପ୍ଟେମ୍ବର", "ଅକ୍ଟୋବର", "ନଭେମ୍ବର", "ଡିସେମ୍ବର"};
                return n[m];
            }
            case 8: {
                String[] n = {"জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
                        "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"};
                return n[m];
            }
            case 9: {
                String[] n = {"जानेवारी", "फेब्रुवारी", "मार्च", "एप्रिल", "मे", "जून",
                        "जुलै", "ऑगस्ट", "सप्टेंबर", "ऑक्टोबर", "नोव्हेंबर", "डिसेंबर"};
                return n[m];
            }
            case 10: {
                String[] n = {"જાન્યુઆરી", "ફેબ્રુઆરી", "માર્ચ", "એપ્રિલ", "મે", "જૂન",
                        "જુલાઈ", "ઑગસ્ટ", "સપ્ટેમ્બર", "ઑક્ટોબર", "નવેમ્બર", "ડિસેમ્બર"};
                return n[m];
            }
            default: {
                String[] n = {"January", "February", "March", "April", "May", "June",
                        "July", "August", "September", "October", "November", "December"};
                return n[m];
            }
        }
    }

    /** Weekday short labels Sun→Sat for calendar grid header. */
    @NonNull
    public static String[] weekdayShorts(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return new String[]{"ഞായർ", "തിങ്കൾ", "ചൊവ്വ", "ബുധൻ", "വ്യാഴം", "വെള്ളി", "ശനി"};
            case 2: return new String[]{"रवि", "सोम", "मंगल", "बुध", "गुरु", "शुक्र", "शनि"};
            case 3: return new String[]{"ఆది", "సోమ", "మంగళ", "బుధ", "గురు", "శుక్ర", "శని"};
            case 4: return new String[]{"ஞாயி", "திங்", "செவ்", "புத", "வியா", "வெள்", "சனி"};
            case 5: return new String[]{"ಭಾನು", "ಸೋಮ", "ಮಂಗಳ", "ಬುಧ", "ಗುರು", "ಶುಕ್ರ", "ಶನಿ"};
            case 6: return new String[]{"ରବି", "ସୋମ", "ମଙ୍ଗଳ", "ବୁଧ", "ଗୁରୁ", "ଶୁକ୍ର", "ଶନି"};
            case 8: return new String[]{"রবি", "সোম", "মঙ্গল", "বুধ", "বৃহ", "শুক্র", "শনি"};
            case 9: return new String[]{"रवि", "सोम", "मंगळ", "बुध", "गुरु", "शुक्र", "शनि"};
            case 10: return new String[]{"રવિ", "સોમ", "મંગળ", "બુધ", "ગુરુ", "શુક્ર", "શનિ"};
            default: return new String[]{"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        }
    }

    public static String shubhaTimes(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "ശുഭ സമയങ്ങൾ";
            case 2: return "शुभ समय";
            case 3: return "శుభ సమయాలు";
            case 4: return "சுப நேரங்கள்";
            case 5: return "ಶುಭ ಸಮಯಗಳು";
            case 6: return "ଶୁଭ ସମୟ";
            case 8: return "শুভ সময়";
            case 9: return "शुभ काळ";
            case 10: return "શુભ સમય";
            default: return "Auspicious times";
        }
    }

    public static String shubhaMuhurthams(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "ശുഭ മുഹൂർത്തങ്ങൾ";
            case 2: return "शुभ मुहूर्त";
            case 3: return "శుభ ముహూర్తాలు";
            case 4: return "சுப முகூர்த்தங்கள்";
            case 5: return "ಶುಭ ಮುಹೂರ್ತಗಳು";
            case 6: return "ଶୁଭ ମୁହୂର୍ତ୍ତ";
            case 8: return "শুভ মুহূর্ত";
            case 9: return "शुभ मुहूर्त";
            case 10: return "શુભ મુહૂર્ત";
            default: return "Auspicious muhurthams";
        }
    }

    public static String ashubhaTimes(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "അശുഭങ്ങൾ";
            case 2: return "अशुभ";
            case 3: return "అశుభాలు";
            case 4: return "அசுபங்கள்";
            case 5: return "ಅಶುಭಗಳು";
            case 6: return "ଅଶୁଭ";
            case 8: return "অশুভ";
            case 9: return "अशुभ";
            case 10: return "અશુભ";
            default: return "Inauspicious";
        }
    }

    public static String tithiLabel(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "തിഥി";
            case 2: return "तिथि";
            case 3: return "తిథి";
            case 4: return "திதி";
            case 5: return "ತಿಥಿ";
            case 6: return "ତିଥି";
            case 8: return "তিথি";
            case 9: return "तिथी";
            case 10: return "તિથિ";
            default: return "Tithi";
        }
    }

    public static String nakshatraLabel(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "നക്ഷത്രം";
            case 2: return "नक्षत्र";
            case 3: return "నక్షత్రం";
            case 4: return "நட்சத்திரம்";
            case 5: return "ನಕ್ಷತ್ರ";
            case 6: return "ନକ୍ଷତ୍ର";
            case 8: return "নক্ষত্র";
            case 9: return "नक्षत्र";
            case 10: return "નક્ષત્ર";
            default: return "Nakshatra";
        }
    }

    @NonNull
    public static String rashiName(@Nullable Context context, int rashiId0) {
        int id = RaasiContent.clampId(rashiId0);
        switch (languageId(context)) {
            case 1: {
                String[] n = {"മേഷം", "വൃഷഭം", "മിഥുനം", "കർക്കടകം", "സിംഹം", "കന്യ",
                        "തുലാം", "വൃശ്ചികം", "ധനു", "മകരം", "കുംഭം", "മീനം"};
                return n[id];
            }
            case 2: {
                String[] n = {"मेष", "वृषभ", "मिथुन", "कर्क", "सिंह", "कन्या",
                        "तुला", "वृश्चिक", "धनु", "मकर", "कुंभ", "मीन"};
                return n[id];
            }
            case 3:
                return RaasiContent.NAMES_TE[id];
            case 4: {
                String[] n = {"மேஷம்", "ரிஷபம்", "மிதுனம்", "கடகம்", "சிம்மம்", "கன்னி",
                        "துலாம்", "விருச்சிகம்", "தனுசு", "மகரம்", "கும்பம்", "மீனம்"};
                return n[id];
            }
            case 5: {
                String[] n = {"ಮೇಷ", "ವೃಷಭ", "ಮಿಥುನ", "ಕರ್ಕಾಟಕ", "ಸಿಂಹ", "ಕನ್ಯಾ",
                        "ತುಲಾ", "ವೃಶ್ಚಿಕ", "ಧನು", "ಮಕರ", "ಕುಂಭ", "ಮೀನ"};
                return n[id];
            }
            case 6: {
                String[] n = {"ମେଷ", "ବୃଷ", "ମିଥୁନ", "କର୍କଟ", "ସିଂହ", "କନ୍ୟା",
                        "ତୁଳା", "ବିଛା", "ଧନୁ", "ମକର", "କୁମ୍ଭ", "ମୀନ"};
                return n[id];
            }
            default: {
                String[] n = {"Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo",
                        "Libra", "Scorpio", "Sagittarius", "Capricorn", "Aquarius", "Pisces"};
                return n[id];
            }
        }
    }

    public static String phalaluTitle(@Nullable Context context, int rashiId0, String dateDisp) {
        String name = rashiName(context, rashiId0);
        switch (languageId(context)) {
            case 1: return name + " രാശിഫലം (" + dateDisp + ")";
            case 2: return name + " राशिफल (" + dateDisp + ")";
            case 3: return name + " రాశి ఫలితాలు (" + dateDisp + ")";
            case 4: return name + " ராசிபலன் (" + dateDisp + ")";
            case 5: return name + " ರಾಶಿಫಲ (" + dateDisp + ")";
            case 6: return name + " ରାଶିଫଳ (" + dateDisp + ")";
            default: return name + " horoscope (" + dateDisp + ")";
        }
    }

    public static String signLineWithEnglish(@Nullable Context context, int rashiId0) {
        String name = rashiName(context, rashiId0);
        String en = RaasiContent.NAMES_EN[RaasiContent.clampId(rashiId0)];
        return name + " (" + en + ")";
    }

    private static int languageId(@Nullable Context context) {
        if (context == null) return 0;
        return DailyUpdatesShareLanguage.effective(context);
    }

    private static String str(@Nullable Context context, int resId, String fallback) {
        if (context != null) {
            try {
                return context.getString(resId);
            } catch (Exception ignored) {
            }
        }
        return fallback;
    }
}
