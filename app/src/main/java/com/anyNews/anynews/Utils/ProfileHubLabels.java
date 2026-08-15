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
