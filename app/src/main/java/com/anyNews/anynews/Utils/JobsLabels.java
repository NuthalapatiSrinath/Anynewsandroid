package com.anyNews.anynews.Utils;

import android.content.Context;

import androidx.annotation.Nullable;

/**
 * Jobs page chrome labels by {@link Constants#LanguageID}
 * (1 ML · 2 HI · 3 TE · 4 TA · 5 KN · 6 OR · else EN).
 */
public final class JobsLabels {

    private JobsLabels() {}

    private static int lang(@Nullable Context c) {
        if (c == null) return 0;
        try {
            return SharedPrefsData.getInt(
                    c.getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME);
        } catch (Exception e) {
            return 0;
        }
    }

    public static String pageTitle(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "ജോലികൾ";
            case 2: return "नौकरियाँ";
            case 3: return "ఉద్యోగాలు";
            case 4: return "வேலைகள்";
            case 5: return "ಉದ್ಯೋಗಗಳು";
            case 6: return "ଚାକିରି";
            default: return "Jobs";
        }
    }

    public static String bannerTitle(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "നിങ്ങളുടെ സ്വപ്ന ജോലി";
            case 2: return "आपकी सपनों की नौकरी";
            case 3: return "మీ కలల ఉద్యోగం";
            case 4: return "உங்கள் கனவு வேலை";
            case 5: return "ನಿಮ್ಮ ಕನಸಿನ ಉದ್ಯೋಗ";
            case 6: return "ଆପଣଙ୍କ ସ୍ୱପ୍ନର ଚାକିରି";
            default: return "Your dream job";
        }
    }

    public static String bannerSubtitle(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "തിരയുക.. അപേക്ഷിക്കുക.. നേടുക!";
            case 2: return "खोजें.. आवेदन करें.. पाएँ!";
            case 3: return "వెతకండి.. అప్లై చేయండి.. సాధించండి!";
            case 4: return "தேடுங்கள்.. விண்ணப்பிக்கவும்.. அடையுங்கள்!";
            case 5: return "ಹುಡುಕಿ.. ಅರ್ಜಿ ಸಲ್ಲಿಸಿ.. ಸಾಧಿಸಿ!";
            case 6: return "ଖୋଜନ୍ତୁ.. ଆବେଦନ କରନ୍ତୁ.. ସାଧନ କରନ୍ତୁ!";
            default: return "Search.. Apply.. Achieve!";
        }
    }

    public static String searchHint(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "ജോലി, കമ്പനി അല്ലെങ്കിൽ സ്കിൽ തിരയുക...";
            case 2: return "नौकरी, कंपनी या स्किल खोजें...";
            case 3: return "ఉద్యోగం పేరు, కంపెనీ లేదా నైపుణ్యం కోసం వెతకండి...";
            case 4: return "வேலை, நிறுவனம் அல்லது திறன் தேடுங்கள்...";
            case 5: return "ಉದ್ಯೋಗ, ಕಂಪನಿ ಅಥವಾ ಕೌಶಲ ಹುಡುಕಿ...";
            case 6: return "ଚାକିରି, କମ୍ପାନୀ କିମ୍ବା ଦକ୍ଷତା ଖୋଜନ୍ତୁ...";
            default: return "Search job, company or skill...";
        }
    }

    public static String filter(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "ഫിൽട്ടർ";
            case 2: return "फ़िल्टर";
            case 3: return "ఫిల్టర్";
            case 4: return "வடிகட்டி";
            case 5: return "ಫಿಲ್ಟರ್";
            case 6: return "ଫିଲ୍ଟର";
            default: return "Filter";
        }
    }

    public static String catAll(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "എല്ലാം";
            case 2: return "सभी";
            case 3: return "అన్నీ";
            case 4: return "அனைத்தும்";
            case 5: return "ಎಲ್ಲಾ";
            case 6: return "ସବୁ";
            default: return "All";
        }
    }

    public static String catIt(@Nullable Context c) {
        return "IT / " + (lang(c) == 3 ? "సాఫ్ట్‌వేర్" : lang(c) == 0 ? "Software" : "Software");
    }

    public static String catBanking(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "ബാങ്കിങ്";
            case 2: return "बैंकिंग";
            case 3: return "బ్యాంకింగ్";
            case 4: return "வங்கி";
            case 5: return "ಬ್ಯಾಂಕಿಂಗ್";
            case 6: return "ବ୍ୟାଙ୍କିଂ";
            default: return "Banking";
        }
    }

    public static String catTeaching(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "ടീച്ചിങ്";
            case 2: return "शिक्षण";
            case 3: return "టీచింగ్";
            case 4: return "கற்பித்தல்";
            case 5: return "ಬೋಧನೆ";
            case 6: return "ଶିକ୍ଷା";
            default: return "Teaching";
        }
    }

    public static String catHealthcare(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "ഹെൽത്ത്‌കെയർ";
            case 2: return "हेल्थकेयर";
            case 3: return "హెల్త్‌కేర్";
            case 4: return "சுகாதாரம்";
            case 5: return "ಆರೋಗ್ಯ";
            case 6: return "ସ୍ୱାସ୍ଥ୍ୟ";
            default: return "Healthcare";
        }
    }

    public static String catMore(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "കൂടുതൽ";
            case 2: return "और";
            case 3: return "మరిన్ని";
            case 4: return "மேலும்";
            case 5: return "ಇನ್ನಷ್ಟು";
            case 6: return "ଅଧିକ";
            default: return "More";
        }
    }

    public static String catGovernment(@Nullable Context c) {
        switch (lang(c)) {
            case 3: return "గవర్నమెంట్";
            default: return "Government";
        }
    }

    public static String catEngineering(@Nullable Context c) {
        switch (lang(c)) {
            case 3: return "ఇంజనీరింగ్";
            default: return "Engineering";
        }
    }

    public static String catSales(@Nullable Context c) {
        switch (lang(c)) {
            case 3: return "సేల్స్";
            default: return "Sales";
        }
    }

    public static String catSupport(@Nullable Context c) {
        switch (lang(c)) {
            case 3: return "సపోర్ట్";
            default: return "Support";
        }
    }

    public static String catManufacturing(@Nullable Context c) {
        switch (lang(c)) {
            case 3: return "మాన్యుఫాక్చరింగ్";
            default: return "Manufacturing";
        }
    }

    public static String catLogistics(@Nullable Context c) {
        switch (lang(c)) {
            case 3: return "లాజిస్టిక్స్";
            default: return "Logistics";
        }
    }

    public static String catHospitality(@Nullable Context c) {
        switch (lang(c)) {
            case 3: return "హాస్పిటాలిటీ";
            default: return "Hospitality";
        }
    }

    public static String catRemote(@Nullable Context c) {
        switch (lang(c)) {
            case 3: return "రిమోట్";
            default: return "Remote";
        }
    }

    public static String statTotal(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "മൊത്തം ജോലികൾ";
            case 2: return "कुल नौकरियाँ";
            case 3: return "మొత్తం ఉద్యోగాలు";
            case 4: return "மொத்த வேலைகள்";
            case 5: return "ಒಟ್ಟು ಉದ್ಯೋಗಗಳು";
            case 6: return "ମୋଟ ଚାକିରି";
            default: return "Total Jobs";
        }
    }

    public static String statCompanies(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "കമ്പനികൾ";
            case 2: return "कंपनियाँ";
            case 3: return "కంపెనీలు";
            case 4: return "நிறுவனங்கள்";
            case 5: return "ಕಂಪನಿಗಳು";
            case 6: return "କମ୍ପାନୀ";
            default: return "Companies";
        }
    }

    public static String statLocations(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "സ്ഥലങ്ങൾ";
            case 2: return "स्थान";
            case 3: return "స్థలాలు";
            case 4: return "இடங்கள்";
            case 5: return "ಸ್ಥಳಗಳು";
            case 6: return "ସ୍ଥାନ";
            default: return "Locations";
        }
    }

    public static String statNew(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "പുതിയ ജോലികൾ";
            case 2: return "नई नौकरियाँ";
            case 3: return "కొత్త ఉద్యోగాలు";
            case 4: return "புதிய வேலைகள்";
            case 5: return "ಹೊಸ ಉದ್ಯೋಗಗಳು";
            case 6: return "ନୂଆ ଚାକିରି";
            default: return "New Jobs";
        }
    }

    public static String today(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "ഇന്ന്";
            case 2: return "आज";
            case 3: return "ఈ రోజు";
            case 4: return "இன்று";
            case 5: return "ಇಂದು";
            case 6: return "ଆଜି";
            default: return "Today";
        }
    }

    public static String latestJobs(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "പുതിയ ജോലികൾ";
            case 2: return "ताज़ा नौकरियाँ";
            case 3: return "తాజా ఉద్యోగాలు";
            case 4: return "சமீப வேலைகள்";
            case 5: return "ಇತ್ತೀಚಿನ ಉದ್ಯೋಗಗಳು";
            case 6: return "ନବୀନ ଚାକିରି";
            default: return "Latest Jobs";
        }
    }

    public static String sortLatest(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "പുതിയത്";
            case 2: return "नवीनतम";
            case 3: return "తాజాగా";
            case 4: return "சமீபத்திய";
            case 5: return "ಇತ್ತೀಚಿನ";
            case 6: return "ନବୀନ";
            default: return "Latest";
        }
    }

    public static String seeMore(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "ജോലികൾ കാണുക";
            case 2: return "नौकरियाँ देखें";
            case 3: return "ఉద్యోగాలు చూడండి";
            case 4: return "வேலைகள் காண்க";
            case 5: return "ಉದ್ಯೋಗಗಳನ್ನು ನೋಡಿ";
            case 6: return "ଚାକିରି ଦେଖନ୍ତୁ";
            default: return "See jobs";
        }
    }

    public static String empty(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "ഇതുവരെ ജോലികളൊന്നുമില്ല";
            case 2: return "अभी कोई नौकरी पोस्ट नहीं हुई";
            case 3: return "ఇంకా ఉద్యోగాలు పోస్ట్ కాలేదు";
            case 4: return "இன்னும் வேலைகள் இல்லை";
            case 5: return "ಇನ್ನೂ ಉದ್ಯೋಗಗಳಿಲ್ಲ";
            case 6: return "ଏପର୍ଯ୍ୟନ୍ତ କୌଣସି ଚାକିରି ନାହିଁ";
            default: return "No jobs posted yet";
        }
    }

    public static String timeAgo(@Nullable Context c, long publishedMs) {
        long diff = Math.max(0, System.currentTimeMillis() - publishedMs);
        long mins = diff / 60000L;
        long hours = diff / 3600000L;
        long days = diff / 86400000L;
        String unit;
        long n;
        if (mins < 60) {
            n = Math.max(1, mins);
            unit = "m";
        } else if (hours < 24) {
            n = hours;
            unit = "h";
        } else {
            n = Math.max(1, days);
            unit = "d";
        }
        switch (lang(c)) {
            case 3: return n + unit + " క్రితం";
            case 2: return n + unit + " पहले";
            case 1: return n + unit + " മുമ്പ്";
            case 4: return n + unit + " முன்";
            case 5: return n + unit + " ಹಿಂದೆ";
            case 6: return n + unit + " ପୂର୍ବେ";
            default: return n + unit + " ago";
        }
    }
}
