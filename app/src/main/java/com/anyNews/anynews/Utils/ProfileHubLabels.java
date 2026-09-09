package com.anyNews.anynews.Utils;

import android.content.Context;

import androidx.annotation.Nullable;

import com.anyNews.anynews.R;

/**
 * Profile hub chrome labels keyed by {@link Constants#LanguageID}
 * (same pattern as {@link ProfilePhotoLabels} / {@link HomeOfflineModeLabels}).
 *
 * <p>Layout {@code @string/…} defaults stay English for inflation; Profile rebinds
 * Guest / login prompt / Status Videos / location caption from these helpers.
 */
public final class ProfileHubLabels {

    private ProfileHubLabels() {
    }

    public static String guest(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "അതിഥി";
            case 2: return "अतिथि";
            case 3: return "అతిథి";
            case 4: return "விருந்தினர்";
            case 5: return "ಅತಿಥಿ";
            case 6: return "ଅତିଥି";
            default: return str(context, R.string.profile_guest_name, "Guest");
        }
    }

    public static String guestSubtitle(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "വാർത്ത അനുഭവം വ്യക്തിഗതമാക്കാൻ ലോഗിൻ ചെയ്യുക";
            case 2: return "अपने समाचार अनुभव को निजीकृत करने के लिए लॉगिन करें";
            case 3: return "మీ వార్తల అనుభవాన్ని వ్యక్తిగతం చేయడానికి లాగిన్ అవ్వండి";
            case 4: return "உங்கள் செய்தி அனுபவத்தை தனிப்பயனாக்க உள்நுழையவும்";
            case 5: return "ನಿಮ್ಮ ಸುದ್ದಿ ಅನುಭವವನ್ನು ವೈಯಕ್ತಿಕಗೊಳಿಸಲು ಲಾಗಿನ್ ಮಾಡಿ";
            case 6: return "ଆପଣଙ୍କ ଖବର ଅନୁଭବ ବ୍ୟକ୍ତିଗତ କରିବାକୁ ଲଗଇନ୍ କରନ୍ତୁ";
            default:
                return str(context, R.string.profile_guest_subtitle,
                        "Login to personalize your news experience");
        }
    }

    /** Fallback Login → when translator pack has no login key. */
    public static String loginCta(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "ലോഗിൻ →";
            case 2: return "लॉगिन →";
            case 3: return "లాగిన్ →";
            case 4: return "உள்நுழை →";
            case 5: return "ಲಾಗಿನ್ →";
            case 6: return "ଲଗଇନ୍ →";
            default: return str(context, R.string.profile_login_cta, "Login →");
        }
    }

    public static String statusVideos(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "സ്റ്റാറ്റസ് വീഡിയോകൾ";
            case 2: return "स्टेटस वीडियो";
            case 3: return "స్టేటస్ వీడియోలు";
            case 4: return "ஸ்டேட்டஸ் வீடியோக்கள்";
            case 5: return "ಸ್ಟೇಟಸ್ ವೀಡಿಯೊಗಳು";
            case 6: return "ଷ୍ଟାଟସ୍ ଭିଡିଓ";
            default: return str(context, R.string.status_videos, "Status Videos");
        }
    }

    public static String becomeAReporter(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "റിപ്പോർട്ടറാകുക";
            case 2: return "रिपोर्टर बनें";
            case 3: return "రిపోర్టర్ అవ్వండి";
            case 4: return "ரிப்போர்ட்டர் ஆகுங்கள்";
            case 5: return "ರಿಪೋರ್ಟರ್ ಆಗಿ";
            case 6: return "ରିପୋର୍ଟର୍ ହୁଅନ୍ତୁ";
            default: return str(context, R.string.become_a_reporter, "Become a Reporter");
        }
    }

    public static String birthdaySpecial(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "ജന്മദിന സ്പെഷ്യൽ";
            case 2: return "जन्मदिन स्पेशल";
            case 3: return "పుట్టినరోజు స్పెషల్";
            case 4: return "பிறந்தநாள் சிறப்பு";
            case 5: return "ಜನ್ಮದಿನ ಸ್ಪೆಷಲ್";
            case 6: return "ଜନ୍ମଦିନ ସ୍ପେସିଆଲ୍";
            default: return str(context, R.string.birthday_special, "Birthday Special");
        }
    }

    /** Profile hub Share App row (Play Store link). */
    public static String shareApp(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "ആപ്പ് ഷെയർ ചെയ്യുക";
            case 2: return "ऐप शेयर करें";
            case 3: return "యాప్ ని షేర్ చేయండి";
            case 4: return "ஆப்பைப் பகிரவும்";
            case 5: return "ಆ್ಯಪ್ ಹಂಚಿಕೊಳ್ಳಿ";
            case 6: return "ଆପ୍ ସେୟାର୍ କରନ୍ତୁ";
            default: return str(context, R.string.profile_share_app, "Share app");
        }
    }

    public static String advertiseOnAnyNews(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "AnyNews-ൽ പരസ്യം ചെയ്യുക";
            case 2: return "AnyNews में विज्ञापन करें";
            case 3: return "ఎనీన్యూస్‌లో Advertise చేయండి";
            case 4: return "AnyNews-இல் விளம்பரம் செய்யுங்கள்";
            case 5: return "AnyNewsನಲ್ಲಿ ಜಾಹೀರಾತು ಮಾಡಿ";
            case 6: return "AnyNewsରେ ବିଜ୍ଞାପନ କରନ୍ତୁ";
            default: return str(context, R.string.advertise_on_anynews, "Advertise in Anynews");
        }
    }

    /** Small caption above the district name on the location row. */
    public static String selectedLocation(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "തിരഞ്ഞെടുത്ത സ്ഥലം";
            case 2: return "चयनित स्थान";
            case 3: return "ఎంచుకున్న లొకేషన్";
            case 4: return "தேர்ந்தெடுத்த இடம்";
            case 5: return "ಆಯ್ಕೆಮಾಡಿದ ಸ್ಥಳ";
            case 6: return "ବଛా ଯାଇଥିବା ସ୍ଥାନ";
            default:
                return str(context, R.string.profile_selected_location, "selected location");
        }
    }

    /** "Edit profile" CTA on the completion bar. */
    public static String editProfile(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "പ്രൊഫൈൽ എഡിറ്റ് ചെയ്യുക";
            case 2: return "प्रोफ़ाइल संपादित करें";
            case 3: return "ప్రొఫైల్ ఎడిట్ చేయండి";
            case 4: return "சுயவிவரத்தைத் திருத்து";
            case 5: return "ಪ್ರೊಫೈಲ್ ಸಂಪಾದಿಸಿ";
            case 6: return "ପ୍ରୋଫାଇଲ୍ ସମ୍ପାଦନ କରନ୍ତୁ";
            default: return str(context, R.string.profile_edit_profile, "Edit profile");
        }
    }

    /** Orange " (Edit)" / change suffix on the location tile. */
    public static String editSuffix(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return " (മാറ്റുക)";
            case 2: return " (बदलें)";
            case 3: return " (ఎడిట్)";
            case 4: return " (மாற்று)";
            case 5: return " (ಬದಲಿಸಿ)";
            case 6: return " (ପରିବର୍ତ୍ତନ)";
            default: return str(context, R.string.profile_edit_suffix, " (Edit)");
        }
    }

    public static String feedback(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "ഫീഡ്‌ബാക്ക്";
            case 2: return "फीडबैक";
            case 3: return "ఫీడ్‌బ్యాక్";
            case 4: return "கருத்து";
            case 5: return "ಪ್ರತಿಕ್ರಿಯೆ";
            case 6: return "ମତାମତ";
            default: return str(context, R.string.profile_feedback, "Feedback");
        }
    }

    public static String following(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "ഫോളോയിംഗ്";
            case 2: return "फॉलोइंग";
            case 3: return "ఫాలోయింగ్";
            case 4: return "பின்தொடர்தல்";
            case 5: return "ಫಾಲೋಯಿಂಗ್";
            case 6: return "ଫଲୋଇଂ";
            default: return "Following";
        }
    }

    public static String followers(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "ഫോളോവേഴ്സ്";
            case 2: return "फॉलोअर्स";
            case 3: return "ఫాలోవర్లు";
            case 4: return "பின்தொடர்பவர்கள்";
            case 5: return "ಅನುಯಾಯಿಗಳು";
            case 6: return "ଅନୁଗାମୀ";
            default: return str(context, R.string.profile_followers_label, "Followers");
        }
    }

    public static String articles(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "ലേഖനങ്ങൾ";
            case 2: return "लेख";
            case 3: return "ఆర్టికల్స్";
            case 4: return "கட்டுரைகள்";
            case 5: return "ಲೇಖನಗಳು";
            case 6: return "ପ୍ରବନ୍ଧ";
            default: return "Articles";
        }
    }

    public static String selectLanguage(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "ഭാഷ തിരഞ്ഞെടുക്കുക";
            case 2: return "भाषा चुनें";
            case 3: return "భాష ఎంచుకోండి";
            case 4: return "மொழியைத் தேர்ந்தெடு";
            case 5: return "ಭಾಷೆ ಆಯ್ಕೆಮಾಡಿ";
            case 6: return "ଭାଷା ବାଛନ୍ତୁ";
            default: return str(context, R.string.profile_change_language_menu, "Change language");
        }
    }

    public static String fabArticle(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "ലേഖനം";
            case 2: return "लेख";
            case 3: return "ఆర్టికల్";
            case 4: return "கட்டுரை";
            case 5: return "ಲೇಖನ";
            case 6: return "ପ୍ରବନ୍ଧ";
            default: return str(context, R.string.profile_reporter_post_article, "Article");
        }
    }

    public static String fabVideoPost(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "വീഡിയോ പോസ്റ്റ്";
            case 2: return "वीडियो पोस्ट";
            case 3: return "వీడియో పోస్ట్";
            case 4: return "வீடியோ இடுகை";
            case 5: return "ವೀಡಿಯೊ ಪೋಸ್ಟ್";
            case 6: return "ଭିଡିଓ ପୋସ୍ଟ";
            default: return str(context, R.string.profile_reporter_post_video, "Video post");
        }
    }

    public static String fabFullPost(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "ഫുൾ പോസ്റ്റ്";
            case 2: return "फुल पोस्ट";
            case 3: return "ఫుల్ పోస్ట్";
            case 4: return "முழு இடுகை";
            case 5: return "ಪೂರ್ಣ ಪೋಸ್ಟ್";
            case 6: return "ପୂର୍ଣ୍ଣ ପୋସ୍ଟ";
            default: return str(context, R.string.profile_reporter_post_full, "Full post");
        }
    }

    public static String settings(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "ക്രമീകരണങ്ങൾ";
            case 2: return "सेटिंग्स";
            case 3: return "సెట్టింగ్స్";
            case 4: return "அமைப்புகள்";
            case 5: return "ಸೆಟ್ಟಿಂಗ್‌ಗಳು";
            case 6: return "ସେଟିଂସ୍";
            default: return str(context, R.string.profile_settings, "Settings");
        }
    }

    /**
     * Profile completion bar label (name/email/mobile/photo %), not earnings.
     * Format: {@code 75% profile completed} / language-id equivalents.
     */
    public static String profileCompleted(@Nullable Context context, int pct) {
        int safe = Math.max(0, Math.min(100, pct));
        switch (languageId(context)) {
            case 1: return safe + "% പ്രൊഫൈൽ പൂർത്തിയായി";
            case 2: return safe + "% प्रोफ़ाइल पूर्ण";
            case 3: return safe + "% ప్రొఫైల్ పూర్తయింది";
            case 4: return safe + "% சுயவிவரம் முடிந்தது";
            case 5: return safe + "% ಪ್ರೊಫೈಲ್ ಪೂರ್ಣಗೊಂಡಿದೆ";
            case 6: return safe + "% ପ୍ରୋଫାଇଲ୍ ସମ୍ପୂର୍ଣ୍ଣ";
            default:
                if (context != null) {
                    try {
                        return context.getString(R.string.profile_achieved_fmt, safe);
                    } catch (Exception ignored) {
                    }
                }
                return safe + "% profile completed";
        }
    }

    private static int languageId(@Nullable Context context) {
        if (context == null) return 0;
        try {
            return SharedPrefsData.getInt(
                    context.getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME);
        } catch (Exception ignored) {
            return 0;
        }
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
