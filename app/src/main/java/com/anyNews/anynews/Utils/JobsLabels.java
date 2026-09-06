package com.anyNews.anynews.Utils;

import android.content.Context;

import androidx.annotation.Nullable;

/**
 * Jobs / Udyogalu page chrome labels by {@link Constants#LanguageID}
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

    /** Page title only — "Udyogalu" / ఉద్యోగాలు (no district, no “in your area”). */
    public static String pageTitle(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "ജോലികൾ";
            case 2: return "नौकरियाँ";
            case 3: return "ఉద్యోగాలు";
            case 4: return "வேலைகள்";
            case 5: return "ಉದ್ಯೋಗಗಳು";
            case 6: return "ଚାକିରି";
            default: return "Udyogalu";
        }
    }

    public static String searchHint(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "ജോലി തിരയുക";
            case 2: return "नौकरी खोजें";
            case 3: return "జాబ్ కోసం ఇక్కడ వెతకండి";
            case 4: return "வேலை தேடுங்கள்";
            case 5: return "ಉದ್ಯೋಗ ಹುಡುಕಿ";
            case 6: return "ଚାକିରି ଖୋଜନ୍ତୁ";
            default: return "Search for job here";
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

    public static String feeWarning(@Nullable Context c) {
        switch (lang(c)) {
            case 3:
                return "హెచ్చరిక: ఉద్యోగం కోసం రిజిస్ట్రేషన్ ఫీజు లేదా డిపాజిట్ డబ్బు ఎవరికీ చెల్లించవద్దు. "
                        + "నిజమైన కంపెనీలు డబ్బు అడగవు. అలా అడిగితే రిపోర్ట్ చేయండి.";
            case 2:
                return "चेतावनी: नौकरी के लिए रजिस्ट्रेशन फीस या डिपॉजिट न दें। "
                        + "असली कंपनियाँ पैसे नहीं माँगतीं। रिपोर्ट करें।";
            case 1:
                return "മുന്നറിയിപ്പ്: ജോലിക്ക് രജിസ്ട്രേഷൻ ഫീസോ ഡെപ്പോസിറ്റോ നൽകരുത്. "
                        + "യഥാർത്ഥ കമ്പനികൾ പണം ചോദിക്കില്ല.";
            default:
                return "Warning: Never pay registration fees or deposits for jobs. "
                        + "Genuine companies do not ask for money. Report such requests.";
        }
    }

    public static String reportTitle(@Nullable Context c) {
        return "Report Job";
    }

    public static String reportReasonLabel(@Nullable Context c) {
        return "Please provide a reason for reporting this job:";
    }

    public static String reportReasonHint(@Nullable Context c) {
        return "Enter your reason here...";
    }

    public static String reportMobileLabel(@Nullable Context c) {
        return "Mobile number:";
    }

    public static String reportMobileHint(@Nullable Context c) {
        return "Enter your mobile number";
    }

    public static String reportClose(@Nullable Context c) {
        return "Close";
    }

    public static String reportSubmit(@Nullable Context c) {
        return "Submit";
    }

    public static String reportThanks(@Nullable Context c) {
        switch (lang(c)) {
            case 3: return "రిపోర్ట్ సమర్పించబడింది";
            case 2: return "रिपोर्ट भेज दी गई";
            default: return "Report submitted";
        }
    }

    public static String reportNeedReason(@Nullable Context c) {
        return "Please enter a reason";
    }

    public static String noPhone(@Nullable Context c) {
        switch (lang(c)) {
            case 3: return "ఫోన్ నంబర్ లేదు";
            default: return "No phone number";
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

    public static String shareMessage(
            @Nullable Context c,
            @Nullable String title,
            @Nullable String location,
            @Nullable String url) {
        String t = title != null && !title.isEmpty() ? title : "a role";
        String loc = location != null && !location.isEmpty() ? location : "NA";
        String link = url != null ? url : "https://anynews.co.in/";
        return "Hi! I just saw this job opening for a " + t + " at " + loc
                + ". It could be a fantastic opportunity for you! Interested? Apply here: "
                + link;
    }

    public static String timeAgo(@Nullable Context c, long publishedMs) {
        long diff = Math.max(0, System.currentTimeMillis() - publishedMs);
        long mins = diff / 60000L;
        long hours = diff / 3600000L;
        long days = diff / 86400000L;
        if (mins < 60) {
            long n = Math.max(1, mins);
            switch (lang(c)) {
                case 3: return n + " నిమి. క్రితం";
                case 2: return n + " मिनट पहले";
                default: return n + "m ago";
            }
        }
        if (hours < 24) {
            switch (lang(c)) {
                case 3: return hours + " గం. క్రితం";
                case 2: return hours + " घंटे पहले";
                default: return hours + "h ago";
            }
        }
        if (days == 1) {
            switch (lang(c)) {
                case 3: return "నిన్న";
                case 2: return "कल";
                default: return "Yesterday";
            }
        }
        switch (lang(c)) {
            case 3: return days + " రోజుల క్రితం";
            case 2: return days + " दिन पहले";
            default: return days + "d ago";
        }
    }

    // ---- retained for any leftover call sites / Daily Updates chrome ----

    public static String bannerTitle(@Nullable Context c) {
        return pageTitle(c);
    }

    public static String bannerSubtitle(@Nullable Context c) {
        switch (lang(c)) {
            case 3: return "వెతకండి.. అప్లై చేయండి.. సాధించండి!";
            default: return "Search.. Apply.. Achieve!";
        }
    }

    public static String filter(@Nullable Context c) {
        return lang(c) == 3 ? "ఫిల్టర్" : "Filter";
    }

    public static String seeMore(@Nullable Context c) {
        return pageTitle(c);
    }
}
