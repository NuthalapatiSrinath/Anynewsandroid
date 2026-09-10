package com.anyNews.anynews.Utils;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/** Fuel detail chrome keyed by {@link Constants#LanguageID}. */
public final class FuelLabels {

    private FuelLabels() {}

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
            case 1: return "ഇന്നത്തെ പെട്രോൾ & ഡീസൽ വിലകൾ";
            case 2: return "आज के पेट्रोल और डीज़ल भाव";
            case 3: return "నేటి పెట్రోల్ & డీజిల్ ధరలు";
            case 4: return "இன்றைய பெட்ரோல் & டீசல் விலைகள்";
            case 5: return "ಇಂದಿನ ಪೆಟ್ರೋಲ್ & ಡೀಸೆಲ್ ಬೆಲೆಗಳು";
            case 6: return "ଆଜିର ପେଟ୍ରୋଲ୍ & ଡିଜେଲ୍ ମୂଲ୍ୟ";
            default: return "Today's Petrol & Diesel Rates";
        }
    }

    @NonNull
    public static String majorCities(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "പ്രധാന നഗരങ്ങളിലെ വിലകൾ";
            case 2: return "प्रमुख शहरों की कीमतें";
            case 3: return "ప్రధాన నగరాల ధరలు";
            case 4: return "முக்கிய நகர விலைகள்";
            case 5: return "ಪ್ರಮುಖ ನಗರಗಳ ಬೆಲೆಗಳು";
            case 6: return "ମୁଖ୍ୟ ସହର ମୂଲ୍ୟ";
            default: return "Major city rates";
        }
    }

    @NonNull
    public static String seeOtherCities(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "മറ്റ് നഗരങ്ങൾ കാണുക >";
            case 2: return "अन्य शहर देखें >";
            case 3: return "ఇతర నగరాలను చూడండి >";
            case 4: return "பிற நகரங்களைப் பார்க்க >";
            case 5: return "ಇತರ ನಗರಗಳನ್ನು ನೋಡಿ >";
            case 6: return "ଅନ୍ୟ ସହର ଦେଖନ୍ତୁ >";
            default: return "See other cities >";
        }
    }

    @NonNull
    public static String cityColumn(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "നഗരം";
            case 2: return "शहर";
            case 3: return "నగరం";
            case 4: return "நகரம்";
            case 5: return "ನಗರ";
            case 6: return "ସହର";
            default: return "City";
        }
    }

    @NonNull
    public static String pickCity(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "നഗരം തിരഞ്ഞെടുക്കുക";
            case 2: return "शहर चुनें";
            case 3: return "నగరం ఎంచుకోండి";
            case 4: return "நகரத்தைத் தேர்ந்தெடுக்கவும்";
            case 5: return "ನಗರವನ್ನು ಆಯ್ಕೆಮಾಡಿ";
            case 6: return "ସହର ବାଛନ୍ତୁ";
            default: return "Choose a city";
        }
    }

    @NonNull
    public static String noCityRates(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "നഗര വിലകൾ ലഭ്യമല്ല";
            case 2: return "शहर की कीमतें उपलब्ध नहीं";
            case 3: return "నగర ధరలు లేవు";
            case 4: return "நகர விலைகள் இல்லை";
            case 5: return "ನಗರ ಬೆಲೆಗಳು ಲಭ್ಯವಿಲ್ಲ";
            case 6: return "ସହର ମୂଲ୍ୟ ନାହିଁ";
            default: return "No city rates";
        }
    }

    @NonNull
    public static String subtitle(@Nullable Context c) {
        return title(c);
    }

    @NonNull
    public static String perLiter(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return " / ലീറ്റർ";
            case 2: return " / लीटर";
            case 3: return " / లీటర్";
            case 4: return " / லிட்டர்";
            case 5: return " / ಲೀಟರ್";
            case 6: return " / ଲିଟର";
            default: return " / liter";
        }
    }

    @NonNull
    public static String historyTitle(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "വില മാറ്റം (ഇന്നലെ → ഇന്ന്)";
            case 2: return "कीमत परिवर्तन (कल → आज)";
            case 3: return "ధరల మార్పు (నిన్న → ఈరోజు)";
            case 4: return "விலை மாற்றம் (நேற்று → இன்று)";
            case 5: return "ಬೆಲೆ ಬದಲಾವಣೆ (ನಿನ್ನೆ → ಇಂದು)";
            case 6: return "ମୂଲ୍ୟ ପରିବର୍ତ୍ତନ (ଗତକାଲି → ଆଜି)";
            default: return "Price change (yesterday → today)";
        }
    }

    @NonNull
    public static String historyNote(@Nullable Context c) {
        switch (lang(c)) {
            case 1:
                return "പൂർണ്ണ 7 ദിവസത്തെ ചരിത്രം ലഭ്യമല്ല — ഇന്നലെ vs ഇന്ന് മാത്രം.";
            case 2:
                return "पूर्ण 7 दिन का इतिहास उपलब्ध नहीं — कल बनाम आज ही।";
            case 3:
                return "పూర్తి 7 రోజుల చరిత్ర అందుబాటులో లేదు — నిన్న vs ఈరోజు మాత్రమే.";
            case 4:
                return "முழு 7 நாள் வரலாறு இல்லை — நேற்று vs இன்று மட்டும்.";
            case 5:
                return "ಪೂರ್ಣ 7 ದಿನಗಳ ಇತಿಹಾಸ ಲಭ್ಯವಿಲ್ಲ — ನಿನ್ನೆ vs ಇಂದು ಮಾತ್ರ.";
            case 6:
                return "ସମ୍ପୂର୍ଣ 7 ଦିନର ଇତିହାସ ନାହିଁ — ଗତକାଲି vs ଆଜି ମାତ୍ର।";
            default:
                return "Full 7-day history unavailable — yesterday vs today only.";
        }
    }

    @NonNull
    public static String pickState(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "സംസ്ഥാനം തിരഞ്ഞെടുക്കുക";
            case 2: return "राज्य चुनें";
            case 3: return "రాష్ట్రం ఎంచుకోండి";
            case 4: return "மாநிலத்தைத் தேர்ந்தெடுக்கவும்";
            case 5: return "ರಾಜ್ಯವನ್ನು ಆಯ್ಕೆಮಾಡಿ";
            case 6: return "ରାଜ୍ୟ ବାଛନ୍ତୁ";
            default: return "Choose state";
        }
    }

    @NonNull
    public static String cityPickerSub(@Nullable Context c, @NonNull String stateLabel) {
        switch (lang(c)) {
            case 1:
                return stateLabel + " — ഹീറോ വിലകൾ പുതുക്കും";
            case 2:
                return stateLabel + " — हीरो कीमतें अपडेट होंगी";
            case 3:
                return stateLabel + " — హీరో ధరలు నవీకరించబడతాయి";
            case 4:
                return stateLabel + " — ஹீரோ விலைகள் புதுப்பிக்கப்படும்";
            case 5:
                return stateLabel + " — ಹೀರೋ ಬೆಲೆಗಳು ನವೀಕರಿಸಲಾಗುತ್ತದೆ";
            case 6:
                return stateLabel + " — ହିରୋ ମୂଲ୍ୟ ଅପଡେଟ୍ ହେବ";
            default:
                return stateLabel + " — hero rates will refresh";
        }
    }

    @NonNull
    public static String stateAveragePrimary(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "സംസ്ഥാന ശരാശരി / പ്രാഥമിക";
            case 2: return "राज्य औसत / प्राथमिक";
            case 3: return "రాష్ట్ర సగటు / ప్రాథమిక";
            case 4: return "மாநில சராசரி / முதன்மை";
            case 5: return "ರಾಜ್ಯ ಸರಾಸರಿ / ಪ್ರಾಥಮಿಕ";
            case 6: return "ରାଜ୍ୟ ହାରାହାରି / ପ୍ରାଥମିକ";
            default: return "State average / primary";
        }
    }

    @NonNull
    public static String stateAverageSelected(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "സംസ്ഥാന ശരാശരി";
            case 2: return "राज्य औसत";
            case 3: return "రాష్ట్ర సగటు";
            case 4: return "மாநில சராசரி";
            case 5: return "ರಾಜ್ಯ ಸರಾಸರಿ";
            case 6: return "ରାଜ୍ୟ ହାରାହାରି";
            default: return "State average";
        }
    }

    @NonNull
    public static String stateAndhra(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "ആന്ധ്രപ്രദേശ്";
            case 2: return "आंध्र प्रदेश";
            case 3: return "ఆంధ్రప్రదేశ్";
            case 4: return "ஆந்திரப் பிரதேசம்";
            case 5: return "ಆಂಧ್ರಪ್ರದೇಶ";
            case 6: return "ଆନ୍ଧ୍ର ପ୍ରଦେଶ";
            default: return "Andhra Pradesh";
        }
    }

    @NonNull
    public static String stateTelangana(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "തെലങ്കാന";
            case 2: return "तेलंगाना";
            case 3: return "తెలంగాణ";
            case 4: return "தெலங்கானா";
            case 5: return "ತೆಲಂಗಾಣ";
            case 6: return "ତେଲଙ୍ଗାନା";
            default: return "Telangana";
        }
    }

    @NonNull
    public static String shareBody(
            @Nullable Context c,
            @NonNull String where,
            @NonNull String petrolLine,
            @NonNull String dieselLine) {
        switch (lang(c)) {
            case 1:
                return "ഇന്ധന വില — "
                        + (where.isEmpty() ? "" : (where + " — "))
                        + DailyUpdatesLabels.petrol(c) + " ₹" + petrolLine
                        + ", " + DailyUpdatesLabels.diesel(c) + " ₹" + dieselLine;
            case 2:
                return "ईंधन कीमतें — "
                        + (where.isEmpty() ? "" : (where + " — "))
                        + DailyUpdatesLabels.petrol(c) + " ₹" + petrolLine
                        + ", " + DailyUpdatesLabels.diesel(c) + " ₹" + dieselLine;
            case 3:
                return "ఇంధన ధరలు — "
                        + (where.isEmpty() ? "" : (where + " — "))
                        + DailyUpdatesLabels.petrol(c) + " ₹" + petrolLine
                        + ", " + DailyUpdatesLabels.diesel(c) + " ₹" + dieselLine;
            case 4:
                return "எரிபொருள் விலை — "
                        + (where.isEmpty() ? "" : (where + " — "))
                        + DailyUpdatesLabels.petrol(c) + " ₹" + petrolLine
                        + ", " + DailyUpdatesLabels.diesel(c) + " ₹" + dieselLine;
            case 5:
                return " ಇಂಧನ ಬೆಲೆ — "
                        + (where.isEmpty() ? "" : (where + " — "))
                        + DailyUpdatesLabels.petrol(c) + " ₹" + petrolLine
                        + ", " + DailyUpdatesLabels.diesel(c) + " ₹" + dieselLine;
            case 6:
                return "ଇଞ୍ଧନ ମୂଲ୍ୟ — "
                        + (where.isEmpty() ? "" : (where + " — "))
                        + DailyUpdatesLabels.petrol(c) + " ₹" + petrolLine
                        + ", " + DailyUpdatesLabels.diesel(c) + " ₹" + dieselLine;
            default:
                return "Fuel rates — "
                        + (where.isEmpty() ? "" : (where + " — "))
                        + "Petrol ₹" + petrolLine + ", Diesel ₹" + dieselLine;
        }
    }

    @NonNull
    public static String promoBothDownTitle(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "വിലകൾ കുറഞ്ഞു!";
            case 2: return "कीमतें घटीं!";
            case 3: return "ధరలు తగ్గాయి!";
            case 4: return "விலைகள் குறைந்தன!";
            case 5: return "ಬೆಲೆಗಳು ಕುಸಿದವು!";
            case 6: return "ମୂଲ୍ୟ କମିଲା!";
            default: return "Prices dropped!";
        }
    }

    @NonNull
    public static String promoBothDownText(
            @Nullable Context c, @NonNull String petrolCh, @NonNull String dieselCh) {
        switch (lang(c)) {
            case 1:
                return "ഇന്ന് പെട്രോൾ (▼" + petrolCh + ") & ഡീസൽ (▼" + dieselCh
                        + ") കുറഞ്ഞു. ലാഭം നേടൂ 🙂";
            case 2:
                return "आज पेट्रोल (▼" + petrolCh + ") और डीज़ल (▼" + dieselCh
                        + ") सस्ता हुआ। फायदा उठाएँ 🙂";
            case 3:
                return "ఈరోజు పెట్రోల్ (▼" + petrolCh + ") & డీజిల్ (▼" + dieselCh
                        + ") తగ్గాయి. లాభం పొందండి 🙂";
            case 4:
                return "இன்று பெட்ரோல் (▼" + petrolCh + ") & டீசல் (▼" + dieselCh
                        + ") குறைந்தது. நன்மை பெறுங்கள் 🙂";
            case 5:
                return "ಇಂದು ಪೆಟ್ರೋಲ್ (▼" + petrolCh + ") & ಡೀಸೆಲ್ (▼" + dieselCh
                        + ") ಕುಸಿದಿದೆ. ಲಾಭ ಪಡೆಯಿರಿ 🙂";
            case 6:
                return "ଆଜି ପେଟ୍ରୋଲ୍ (▼" + petrolCh + ") & ଡିଜେଲ୍ (▼" + dieselCh
                        + ") କମିଲା। ଲାଭ ନିଅନ୍ତୁ 🙂";
            default:
                return "Today petrol (▼" + petrolCh + ") & diesel (▼" + dieselCh
                        + ") are cheaper. Save at the pump 🙂";
        }
    }

    @NonNull
    public static String promoBothUpTitle(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "വിലകൾ കൂടി!";
            case 2: return "कीमतें बढ़ीं!";
            case 3: return "ధరలు పెరిగాయి!";
            case 4: return "விலைகள் உயர்ந்தன!";
            case 5: return "ಬೆಲೆಗಳು ಏರಿವೆ!";
            case 6: return "ମୂଲ୍ୟ ବଢ଼ିଲା!";
            default: return "Prices increased!";
        }
    }

    @NonNull
    public static String promoBothUpText(
            @Nullable Context c, @NonNull String petrolCh, @NonNull String dieselCh) {
        switch (lang(c)) {
            case 1:
                return "ഇന്ന് പെട്രോൾ (▲" + petrolCh + ") & ഡീസൽ (▲" + dieselCh + ") കൂടി.";
            case 2:
                return "आज पेट्रोल (▲" + petrolCh + ") और डीज़ल (▲" + dieselCh + ") महँगा हुआ।";
            case 3:
                return "ఈరోజు పెట్రోల్ (▲" + petrolCh + ") & డీజిల్ (▲" + dieselCh + ") పెరిగాయి.";
            case 4:
                return "இன்று பெட்ரோல் (▲" + petrolCh + ") & டீசல் (▲" + dieselCh + ") உயர்ந்தது.";
            case 5:
                return "ಇಂದು ಪೆಟ್ರೋಲ್ (▲" + petrolCh + ") & ಡೀಸೆಲ್ (▲" + dieselCh + ") ಏರಿದೆ.";
            case 6:
                return "ଆଜି ପେଟ୍ରୋଲ୍ (▲" + petrolCh + ") & ଡିଜେଲ୍ (▲" + dieselCh + ") ବଢ଼ିଲା।";
            default:
                return "Today petrol (▲" + petrolCh + ") & diesel (▲" + dieselCh + ") went up.";
        }
    }

    @NonNull
    public static String promoMixedTitle(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "നീരൂപമായ മാറ്റം";
            case 2: return "मिश्रित बदलाव";
            case 3: return "మిశ్రమ మార్పు";
            case 4: return "கலப்பு மாற்றம்";
            case 5: return "ಮಿಶ್ರ ಬದಲಾವಣೆ";
            case 6: return "ମିଶ୍ର ପରିବର୍ତ୍ତନ";
            default: return "Mixed change";
        }
    }

    @NonNull
    public static String promoPetrolDownDieselUp(
            @Nullable Context c, @NonNull String petrolCh, @NonNull String dieselCh) {
        switch (lang(c)) {
            case 1:
                return DailyUpdatesLabels.petrol(c) + " കുറഞ്ഞു (▼" + petrolCh + "), "
                        + DailyUpdatesLabels.diesel(c) + " കൂടി (▲" + dieselCh + ").";
            case 2:
                return "पेट्रोल घटा (▼" + petrolCh + "), डीज़ल बढ़ा (▲" + dieselCh + ")।";
            case 3:
                return "పెట్రోల్ తగ్గింది (▼" + petrolCh + "), డీజిల్ పెరిగింది (▲" + dieselCh + ").";
            case 4:
                return "பெட்ரோல் குறைந்தது (▼" + petrolCh + "), டீசல் உயர்ந்தது (▲" + dieselCh + ").";
            case 5:
                return "ಪೆಟ್ರೋಲ್ ಕುಸಿತ (▼" + petrolCh + "), ಡೀಸೆಲ್ ಏರಿಕೆ (▲" + dieselCh + ").";
            case 6:
                return "ପେଟ୍ରୋଲ୍ କମିଲା (▼" + petrolCh + "), ଡିଜେଲ୍ ବଢ଼ିଲା (▲" + dieselCh + ")।";
            default:
                return "Petrol down (▼" + petrolCh + "), diesel up (▲" + dieselCh + ").";
        }
    }

    @NonNull
    public static String promoPetrolUpDieselDown(
            @Nullable Context c, @NonNull String petrolCh, @NonNull String dieselCh) {
        switch (lang(c)) {
            case 1:
                return DailyUpdatesLabels.petrol(c) + " കൂടി (▲" + petrolCh + "), "
                        + DailyUpdatesLabels.diesel(c) + " കുറഞ്ഞു (▼" + dieselCh + ").";
            case 2:
                return "पेट्रोल बढ़ा (▲" + petrolCh + "), डीज़ल घटा (▼" + dieselCh + ")।";
            case 3:
                return "పెట్రోల్ పెరిగింది (▲" + petrolCh + "), డీజిల్ తగ్గింది (▼" + dieselCh + ").";
            case 4:
                return "பெட்ரோல் உயர்ந்தது (▲" + petrolCh + "), டீசல் குறைந்தது (▼" + dieselCh + ").";
            case 5:
                return "ಪೆಟ್ರೋಲ್ ಏರಿಕೆ (▲" + petrolCh + "), ಡೀಸೆಲ್ ಕುಸಿತ (▼" + dieselCh + ").";
            case 6:
                return "ପେଟ୍ରୋଲ୍ ବଢ଼ିଲା (▲" + petrolCh + "), ଡିଜେଲ୍ କମିଲା (▼" + dieselCh + ")।";
            default:
                return "Petrol up (▲" + petrolCh + "), diesel down (▼" + dieselCh + ").";
        }
    }

    @NonNull
    public static String promoPetrolDownTitle(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return DailyUpdatesLabels.petrol(c) + " കുറഞ്ഞു";
            case 2: return "पेट्रोल सस्ता";
            case 3: return "పెట్రోల్ తగ్గింది";
            case 4: return "பெட்ரோல் குறைந்தது";
            case 5: return "ಪೆಟ್ರೋಲ್ ಕುಸಿತ";
            case 6: return "ପେଟ୍ରୋଲ୍ କମିଲା";
            default: return "Petrol down";
        }
    }

    @NonNull
    public static String promoPetrolDownText(@Nullable Context c, @NonNull String petrolCh) {
        switch (lang(c)) {
            case 1:
                return "ഇന്ന് " + DailyUpdatesLabels.petrol(c) + " കുറഞ്ഞു (▼" + petrolCh
                        + "). " + DailyUpdatesLabels.diesel(c) + " മാറിയില്ല.";
            case 2:
                return "आज पेट्रोल सस्ता (▼" + petrolCh + ")। डीज़ल वही।";
            case 3:
                return "ఈరోజు పెట్రోల్ తగ్గింది (▼" + petrolCh + "). డీజిల్ మారలేదు.";
            case 4:
                return "இன்று பெட்ரோல் குறைந்தது (▼" + petrolCh + "). டீசல் மாறவில்லை.";
            case 5:
                return "ಇಂದು ಪೆಟ್ರೋಲ್ ಕುಸಿತ (▼" + petrolCh + "). ಡೀಸೆಲ್ ಬದಲಾಗಿಲ್ಲ.";
            case 6:
                return "ଆଜି ପେଟ୍ରୋଲ୍ କମିଲା (▼" + petrolCh + "). ଡିଜେଲ୍ ବଦଳିଲା ନାହିଁ।";
            default:
                return "Today petrol down (▼" + petrolCh + "). Diesel unchanged.";
        }
    }

    @NonNull
    public static String promoPetrolUpTitle(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return DailyUpdatesLabels.petrol(c) + " കൂടി";
            case 2: return "पेट्रोल महँगा";
            case 3: return "పెట్రోల్ పెరిగింది";
            case 4: return "பெட்ரோல் உயர்ந்தது";
            case 5: return "ಪೆಟ್ರೋಲ್ ಏರಿಕೆ";
            case 6: return "ପେଟ୍ରୋଲ୍ ବଢ଼ିଲା";
            default: return "Petrol up";
        }
    }

    @NonNull
    public static String promoPetrolUpText(@Nullable Context c, @NonNull String petrolCh) {
        switch (lang(c)) {
            case 1:
                return "ഇന്ന് " + DailyUpdatesLabels.petrol(c) + " കൂടി (▲" + petrolCh
                        + "). " + DailyUpdatesLabels.diesel(c) + " മാറിയില്ല.";
            case 2:
                return "आज पेट्रोल महँगा (▲" + petrolCh + ")। डीज़ल वही।";
            case 3:
                return "ఈరోజు పెట్రోల్ పెరిగింది (▲" + petrolCh + "). డీజిల్ మారలేదు.";
            case 4:
                return "இன்று பெட்ரோல் உயர்ந்தது (▲" + petrolCh + "). டீசல் மாறவில்லை.";
            case 5:
                return "ಇಂದು ಪೆಟ್ರೋಲ್ ಏರಿಕೆ (▲" + petrolCh + "). ಡೀಸೆಲ್ ಬದಲಾಗಿಲ್ಲ.";
            case 6:
                return "ଆଜି ପେଟ୍ରୋଲ୍ ବଢ଼ିଲା (▲" + petrolCh + "). ଡିଜେଲ୍ ବଦଳିଲା ନାହିଁ।";
            default:
                return "Today petrol up (▲" + petrolCh + "). Diesel unchanged.";
        }
    }

    @NonNull
    public static String promoDieselDownTitle(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return DailyUpdatesLabels.diesel(c) + " കുറഞ്ഞു";
            case 2: return "डीज़ल सस्ता";
            case 3: return "డీజిల్ తగ్గింది";
            case 4: return "டீசல் குறைந்தது";
            case 5: return "ಡೀಸೆಲ್ ಕುಸಿತ";
            case 6: return "ଡିଜେଲ୍ କମିଲା";
            default: return "Diesel down";
        }
    }

    @NonNull
    public static String promoDieselDownText(@Nullable Context c, @NonNull String dieselCh) {
        switch (lang(c)) {
            case 1:
                return "ഇന്ന് " + DailyUpdatesLabels.diesel(c) + " കുറഞ്ഞു (▼" + dieselCh
                        + "). " + DailyUpdatesLabels.petrol(c) + " മാറിയില്ല.";
            case 2:
                return "आज डीज़ल सस्ता (▼" + dieselCh + ")। पेट्रोल वही।";
            case 3:
                return "ఈరోజు డీజిల్ తగ్గింది (▼" + dieselCh + "). పెట్రోల్ మారలేదు.";
            case 4:
                return "இன்று டீசல் குறைந்தது (▼" + dieselCh + "). பெட்ரோல் மாறவில்லை.";
            case 5:
                return "ಇಂದು ಡೀಸೆಲ್ ಕುಸಿತ (▼" + dieselCh + "). ಪೆಟ್ರೋಲ್ ಬದಲಾಗಿಲ್ಲ.";
            case 6:
                return "ଆଜି ଡିଜେଲ୍ କମିଲା (▼" + dieselCh + "). ପେଟ୍ରୋଲ୍ ବଦଳିଲା ନାହିଁ।";
            default:
                return "Today diesel down (▼" + dieselCh + "). Petrol unchanged.";
        }
    }

    @NonNull
    public static String promoDieselUpTitle(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return DailyUpdatesLabels.diesel(c) + " കൂടി";
            case 2: return "डीज़ल महँगा";
            case 3: return "డీజిల్ పెరిగింది";
            case 4: return "டீசல் உயர்ந்தது";
            case 5: return "ಡೀಸೆಲ್ ಏರಿಕೆ";
            case 6: return "ଡିଜେଲ୍ ବଢ଼ିଲା";
            default: return "Diesel up";
        }
    }

    @NonNull
    public static String promoDieselUpText(@Nullable Context c, @NonNull String dieselCh) {
        switch (lang(c)) {
            case 1:
                return "ഇന്ന് " + DailyUpdatesLabels.diesel(c) + " കൂടി (▲" + dieselCh
                        + "). " + DailyUpdatesLabels.petrol(c) + " മാറിയില്ല.";
            case 2:
                return "आज डीज़ल महँगा (▲" + dieselCh + ")। पेट्रोल वही।";
            case 3:
                return "ఈరోజు డీజిల్ పెరిగింది (▲" + dieselCh + "). పెట్రోల్ మారలేదు.";
            case 4:
                return "இன்று டீசல் உயர்ந்தது (▲" + dieselCh + "). பெட்ரோல் மாறவில்லை.";
            case 5:
                return "ಇಂದು ಡೀಸೆಲ್ ಏರಿಕೆ (▲" + dieselCh + "). ಪೆಟ್ರೋಲ್ ಬದಲಾಗಿಲ್ಲ.";
            case 6:
                return "ଆଜି ଡିଜେଲ୍ ବଢ଼ିଲା (▲" + dieselCh + "). ପେଟ୍ରୋଲ୍ ବଦଳିଲା ନାହିଁ।";
            default:
                return "Today diesel up (▲" + dieselCh + "). Petrol unchanged.";
        }
    }

    @NonNull
    public static String promoUnchangedTitle(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "വില മാറിയില്ല";
            case 2: return "कीमतें वही";
            case 3: return "ధరలు మారలేదు";
            case 4: return "விலை மாறவில்லை";
            case 5: return "ಬೆಲೆ ಬದಲಾಗಿಲ್ಲ";
            case 6: return "ମୂଲ୍ୟ ବଦଳିଲା ନାହିଁ";
            default: return "No price change";
        }
    }

    @NonNull
    public static String promoUnchangedText(@Nullable Context c) {
        switch (lang(c)) {
            case 1:
                return "ഇന്ന് " + DailyUpdatesLabels.petrol(c) + " & "
                        + DailyUpdatesLabels.diesel(c) + " ഇന്നലെ പോലെ.";
            case 2:
                return "आज पेट्रोल और डीज़ल कल जैसी ही कीमत।";
            case 3:
                return "ఈరోజు పెట్రోల్ & డీజిల్ ధరలు నిన్నటితో సమానం.";
            case 4:
                return "இன்று பெட்ரோல் & டீசல் நேற்றைய விலையில்.";
            case 5:
                return "ಇಂದು ಪೆಟ್ರೋಲ್ & ಡೀಸೆಲ್ ನಿನ್ನೆಯಂತೆ.";
            case 6:
                return "ଆଜି ପେଟ୍ରୋଲ୍ & ଡିଜେଲ୍ ଗତକାଲି ପରି।";
            default:
                return "Today petrol & diesel match yesterday's rates.";
        }
    }

    public static boolean looksLikeTeluguChrome(@Nullable String s) {
        if (s == null || s.isEmpty()) return false;
        return s.indexOf('\u0C00') >= 0;
    }
}
