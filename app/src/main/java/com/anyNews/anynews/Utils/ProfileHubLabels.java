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
            case 3: return "ఎనీన్యూస్‌లో ప్రకటన ఇవ్వండి";
            case 4: return "AnyNews-இல் விளம்பரம் செய்யுங்கள்";
            case 5: return "AnyNewsನಲ್ಲಿ ಜಾಹೀರಾತು ಮಾಡಿ";
            case 6: return "AnyNewsରେ ବିଜ୍ଞାପନ କରନ୍ତୁ";
            default: return str(context, R.string.advertise_on_anynews, "Advertise in Anynews");
        }
    }

    /** Caption above the district name on the location row. */
    public static String selectedLocation(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "നിങ്ങളുടെ സ്ഥലം തിരഞ്ഞെടുക്കുക";
            case 2: return "अपना स्थान चुनें";
            case 3: return "మీ లొకేషన్ ఎంచుకోండి";
            case 4: return "உங்கள் இடத்தைத் தேர்ந்தெடுக்கவும்";
            case 5: return "ನಿಮ್ಮ ಸ್ಥಳವನ್ನು ಆಯ್ಕೆಮಾಡಿ";
            case 6: return "ଆପଣଙ୍କ ଅବସ୍ଥାନ ବାଛନ୍ତୁ";
            default:
                return str(context, R.string.profile_selected_location, "Choose Your Location");
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

    /** Caption for the language row. */
    public static String selectLanguage(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "ഭാഷ തിരഞ്ഞെടുക്കുക";
            case 2: return "भाषा चुनें";
            case 3: return "భాష ఎంచుకోండి";
            case 4: return "மொழியைத் தேர்ந்தெடுக்கவும்";
            case 5: return "ಭಾಷೆ ಆಯ್ಕೆಮಾಡಿ";
            case 6: return "ଭାଷା ବାଛନ୍ତୁ";
            default:
                return str(context, R.string.profile_choose_language, "Choose Language");
        }
    }

    public static String categories(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "വിഭാഗങ്ങൾ";
            case 2: return "श्रेणियाँ";
            case 3: return "కేటగిరీలు";
            case 4: return "வகைகள்";
            case 5: return "ವರ್ಗಗಳು";
            case 6: return "ବର୍ଗଗୁଡ଼ିକ";
            default: return str(context, R.string.profile_categories, "Categories");
        }
    }

    public static String categoriesViewAll(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "എല്ലാം കാണുക >";
            case 2: return "सभी देखें >";
            case 3: return "అన్నీ చూడండి >";
            case 4: return "அனைத்தையும் பார்க்க >";
            case 5: return "ಎಲ್ಲವನ್ನೂ ನೋಡಿ >";
            case 6: return "ସବୁ ଦେଖନ୍ତୁ >";
            default:
                return str(context, R.string.profile_categories_view_all, "View All >");
        }
    }

    public static String contactUs(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "ഞങ്ങളെ ബന്ധപ്പെടുക";
            case 2: return "संपर्क करें";
            case 3: return "మమ్మల్ని సంప్రదించండి";
            case 4: return "எங்களை தொடர்பு கொள்ளுங்கள்";
            case 5: return "ನಮ್ಮನ್ನು ಸಂಪರ್ಕಿಸಿ";
            case 6: return "ଆମକୁ ଯୋଗାଯୋଗ କରନ୍ତୁ";
            default: return str(context, R.string.profile_contact_us, "Contact Us");
        }
    }

    public static String logout(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "ലോഗൗട്ട്";
            case 2: return "लॉग आउट";
            case 3: return "లాగ్ అవుట్";
            case 4: return "வெளியேறு";
            case 5: return "ಲಾಗ್ ಔಟ್";
            case 6: return "ଲଗଆଉଟ୍";
            default: return str(context, R.string.profile_logout, "Logout");
        }
    }

    public static String logoutConfirm(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "ലോഗൗട്ട് ചെയ്യണോ?";
            case 2: return "लॉग आउट करें?";
            case 3: return "లాగ్ అవుట్ చేయాలా?";
            case 4: return "வெளியேறவா?";
            case 5: return "ಲಾಗ್ ಔಟ್ ಮಾಡಬೇಕೇ?";
            case 6: return "ଲଗଆଉଟ୍ କରିବେ କି?";
            default: return str(context, R.string.profile_logout_confirm, "Logout?");
        }
    }

    public static String logoutYes(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "അതെ";
            case 2: return "हाँ";
            case 3: return "అవును";
            case 4: return "ஆம்";
            case 5: return "ಹೌದು";
            case 6: return "ହଁ";
            default: return str(context, R.string.profile_logout_yes, "Yes");
        }
    }

    public static String logoutNo(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "അല്ല";
            case 2: return "नहीं";
            case 3: return "కాదు";
            case 4: return "இல்லை";
            case 5: return "ಇಲ್ಲ";
            case 6: return "ନା";
            default: return str(context, R.string.profile_logout_no, "No");
        }
    }

    public static String tagline(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "മുന്നിലിരിക്കൂ, അറിഞ്ഞിരിക്കൂ";
            case 2: return "आगे रहें, जानकार रहें";
            case 3: return "ముందుండండి, సమాచారంతో ఉండండి";
            case 4: return "முன்னிலையில் இருங்கள், தெரிந்திருங்கள்";
            case 5: return "ಮುಂದಿರಿ, ತಿಳಿದಿರಿ";
            case 6: return "ଆଗକୁ ରୁହନ୍ତୁ, ଜାଣି ରୁହନ୍ତୁ";
            default: return str(context, R.string.profile_tagline, "Stay Ahead, Stay Informed");
        }
    }

    public static String verified(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "സ്ഥിരീകരിച്ചത്";
            case 2: return "सत्यापित";
            case 3: return "వెరిఫైడ్";
            case 4: return "சரிபார்க்கப்பட்டது";
            case 5: return "ಪರಿಶೀಲಿಸಲಾಗಿದೆ";
            case 6: return "ଯାଞ୍ଚିତ";
            default: return str(context, R.string.profile_verified, "Verified");
        }
    }

    public static String totalEarnings(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "മൊത്തം വരുമാനം";
            case 2: return "कुल कमाई";
            case 3: return "మొత్తం సంపాదన";
            case 4: return "மொத்த வருவாய்";
            case 5: return "ಒಟ್ಟು ಗಳಿಕೆ";
            case 6: return "ମୋଟ ଆୟ";
            default: return str(context, R.string.profile_total_earnings, "Total earnings");
        }
    }

    public static String earnings(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "വരുമാനം";
            case 2: return "कमाई";
            case 3: return "సంపాదన";
            case 4: return "வருவாய்";
            case 5: return "ಗಳಿಕೆ";
            case 6: return "ଆୟ";
            default: return str(context, R.string.profile_earnings_details, "Earnings");
        }
    }

    public static String reporterShortcuts(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "റിപ്പോർട്ടർ ഷോർട്ട്കട്ടുകൾ";
            case 2: return "रिपोर्टर शॉर्टकट";
            case 3: return "రిపోర్టర్ షార్ట్‌కట్లు";
            case 4: return "ரிப்போர்ட்டர் குறுக்குவழிகள்";
            case 5: return "ರಿಪೋರ್ಟರ್ ಶಾರ್ಟ್‌ಕಟ್‌ಗಳು";
            case 6: return "ରିପୋର୍ଟର୍ ସର୍ଟକଟ୍";
            default:
                return str(context, R.string.profile_reporter_shortcuts, "Reporter Shortcuts");
        }
    }

    public static String completeProfile(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "പ്രൊഫൈൽ പൂർത്തിയാക്കുക";
            case 2: return "प्रोफ़ाइल पूरा करें";
            case 3: return "ప్రొఫైల్ పూర్తి చేయండి";
            case 4: return "சுயவிவரத்தை முடிக்கவும்";
            case 5: return "ಪ್ರೊಫೈಲ್ ಪೂರ್ಣಗೊಳಿಸಿ";
            case 6: return "ପ୍ରୋଫାଇଲ୍ ସମ୍ପୂର୍ଣ୍ଣ କରନ୍ତୁ";
            default:
                return str(context, R.string.profile_complete_profile, "Complete Profile");
        }
    }

    public static String reporterRole(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "റിപ്പോർട്ടർ";
            case 2: return "रिपोर्टर";
            case 3: return "రిపోర్టర్";
            case 4: return "ரிப்போர்ட்டர்";
            case 5: return "ರಿಪೋರ್ಟರ್";
            case 6: return "ରିପୋର୍ଟର୍";
            default: return str(context, R.string.profile_role_reporter, "Reporter");
        }
    }

    public static String reporterDeskPending(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "ലോഗിൻ ചെയ്തു — റിപ്പോർട്ടർ അംഗീകാരം കാത്തിരിക്കുന്നു";
            case 2: return "लॉगिन हो चुका — रिपोर्टर स्वीकृति की प्रतीक्षा";
            case 3: return "లాగిన్ అయ్యారు — రిపోర్టర్ ఆమోదం కోసం వేచి";
            case 4: return "உள்நுழைந்துள்ளீர்கள் — ரிப்போர்ட்டர் ஒப்புதல் காத்திருக்கிறது";
            case 5: return "ಲಾಗಿನ್ ಆಗಿದೆ — ರಿಪೋರ್ಟರ್ ಅನುಮೋದನೆಗಾಗಿ ಕಾಯುತ್ತಿದೆ";
            case 6: return "ଲଗଇନ୍ ହୋଇଛି — ରିପୋର୍ଟର୍ ଅନୁମୋଦନ ଅପେକ୍ଷାରେ";
            default:
                return str(context, R.string.profile_reporter_desk_pending,
                        "Logged in — awaiting reporter approval");
        }
    }

    public static String reporterDeskRejected(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "റിപ്പോർട്ടർ അപേക്ഷ നിരസിച്ചു";
            case 2: return "रिपोर्टर आवेदन अस्वीकृत";
            case 3: return "రిపోర్టర్ దరఖాస్తు తిరస్కరించబడింది";
            case 4: return "ரிப்போர்ட்டர் விண்ணப்பம் நிராகரிக்கப்பட்டது";
            case 5: return "ರಿಪೋರ್ಟರ್ ಅರ್ಜಿ ತಿರಸ್ಕರಿಸಲಾಗಿದೆ";
            case 6: return "ରିପୋର୍ଟର୍ ଆବେଦନ ପ୍ରତ୍ୟାଖ୍ୟାନ";
            default:
                return str(context, R.string.profile_reporter_desk_rejected,
                        "Reporter application rejected");
        }
    }

    public static String deleteAccount(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "അക്കൗണ്ട് ഇല്ലാതാക്കുക";
            case 2: return "खाता हटाएँ";
            case 3: return "ఖాతా తొలగించండి";
            case 4: return "கணக்கை நீக்கு";
            case 5: return "ಖಾತೆ ಅಳಿಸಿ";
            case 6: return "ଖାତା ବିଲୋପ କରନ୍ତୁ";
            default: return "Delete Account";
        }
    }

    public static String followMore(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "കൂടുതൽ ഫോളോ ചെയ്യുക";
            case 2: return "और फॉलो करें";
            case 3: return "మరిన్ని ఫాలో అవ్వండి";
            case 4: return "மேலும் பின்தொடரவும்";
            case 5: return "ಇನ್ನಷ್ಟು ಫಾಲೋ ಮಾಡಿ";
            case 6: return "ଅଧିକ ଫଲୋ କରନ୍ତୁ";
            default: return "Follow More";
        }
    }

    public static String likes(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "ലൈക്കുകൾ";
            case 2: return "लाइक्स";
            case 3: return "లైక్స్";
            case 4: return "லைக்ஸ்";
            case 5: return "ಲೈಕ್‌ಗಳು";
            case 6: return "ଲାଇକ୍";
            default: return "Likes";
        }
    }

    public static String wallet(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "വാലറ്റ്";
            case 2: return "वॉलेट";
            case 3: return "వాలెట్";
            case 4: return "வாலட்";
            case 5: return "ವಾಲೆಟ್";
            case 6: return "ଓୟାଲେଟ୍";
            default: return str(context, R.string.profile_wallet, "Wallet");
        }
    }

    public static String rewards(@Nullable Context context) {
        switch (languageId(context)) {
            case 1: return "റിവാർഡുകൾ";
            case 2: return "रिवॉर्ड्स";
            case 3: return "రివార్డ్స్";
            case 4: return "ரிவார்டுகள்";
            case 5: return "ರಿವಾರ್ಡ್‌ಗಳು";
            case 6: return "ରିୱାର୍ଡସ୍";
            default: return "Rewards";
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
