package com.anyNews.anynews.Utils;

import android.content.Context;

import androidx.annotation.Nullable;

import com.anyNews.anynews.R;

/**
 * Advertise / Birthday Special chat chrome by {@link Constants#LanguageID}.
 */
public final class AdvertiseChatLabels {

    private AdvertiseChatLabels() {}

    private static int languageId(@Nullable Context c) {
        if (c == null) return 0;
        try {
            return SharedPrefsData.getInt(c, Constants.LanguageID, Constants.PREF_NAME);
        } catch (Exception e) {
            return 0;
        }
    }

    public static String advertiseTitle(@Nullable Context c) {
        switch (languageId(c)) {
            case 1: return "AnyNews-ൽ പരസ്യം ചെയ്യുക";
            case 2: return "AnyNews में विज्ञापन करें";
            case 3: return "ఎనీన్యూస్‌లో యాడ్స్ ఇవ్వండి";
            case 4: return "AnyNews-இல் விளம்பரம் செய்யுங்கள்";
            case 5: return "AnyNewsನಲ್ಲಿ ಜಾಹೀರಾತು ಮಾಡಿ";
            case 6: return "AnyNewsରେ ବିଜ୍ଞାପନ କରନ୍ତୁ";
            default: return str(c, R.string.advertise_on_anynews, "Advertise in Anynews");
        }
    }

    public static String birthdayTitle(@Nullable Context c) {
        switch (languageId(c)) {
            case 1: return "ജന്മദിന സ്പെഷ്യൽ";
            case 2: return "जन्मदिन स्पेशल";
            case 3: return "పుట్టినరోజు స్పెషల్";
            case 4: return "பிறந்தநாள் சிறப்பு";
            case 5: return "ಜನ್ಮದಿನ ಸ್ಪೆಷಲ್";
            case 6: return "ଜନ୍ମଦିନ ସ୍ପେସିଆଲ୍";
            default: return str(c, R.string.birthday_special, "Birthday Special");
        }
    }

    public static String birthdayOffer(@Nullable Context c) {
        switch (languageId(c)) {
            case 1:
                return "വ്യക്തമായ ഫോട്ടോ, പേര്, സ്ഥലം അയയ്ക്കുക. ഞങ്ങളുടെ ടീം Local Birthday Special ഉണ്ടാക്കി ഇവിടെ മറുപടി നൽകും.";
            case 2:
                return "साफ़ फ़ोटो, नाम और स्थान भेजें। हमारी टीम Local Birthday Special बनाकर यहाँ जवाब देगी।";
            case 3:
                return "స్పష్టమైన ఫోటో, పేరు, స్థలం పంపండి. మా టీమ్ Local Birthday Special తయారుచేసి ఇక్కడే సమాధానం ఇస్తుంది.";
            case 4:
                return "தெளிவான புகைப்படம், பெயர், இடம் அனுப்புங்கள். எங்கள் குழு Local Birthday Special உருவாக்கி இங்கே பதிலளிக்கும்.";
            case 5:
                return "ಸ್ಪಷ್ಟ ಫೋಟೋ, ಹೆಸರು, ಸ್ಥಳ ಕಳುಹಿಸಿ. ನಮ್ಮ ತಂಡ Local Birthday Special ತಯಾರಿಸಿ ಇಲ್ಲಿ ಉತ್ತರಿಸುತ್ತದೆ.";
            case 6:
                return "ସ୍ପଷ୍ଟ ଫଟୋ, ନାମ ଓ ସ୍ଥାନ ପଠାନ୍ତୁ। ଆମ ଟିମ୍ Local Birthday Special ତିଆରି କରି ଏଠାରେ ଉତ୍ତର ଦେବ।";
            default:
                return str(
                        c,
                        R.string.birthday_offer_default,
                        "Share a clear photo, name, and location. Our team will create your Local Birthday Special and reply here.");
        }
    }

    public static String hintLocation(@Nullable Context c) {
        switch (languageId(c)) {
            case 1: return "ഗ്രാമം / പട്ടണം / സ്ഥലം";
            case 2: return "गाँव / शहर / स्थान";
            case 3: return "గ్రామం / పట్టణం / స్థలం";
            case 4: return "கிராமம் / ஊர் / இடம்";
            case 5: return "ಗ್ರಾಮ / ಪಟ್ಟಣ / ಸ್ಥಳ";
            case 6: return "ଗାଁ / ସହର / ସ୍ଥାନ";
            default: return str(c, R.string.birthday_hint_location, "Village / town / location");
        }
    }

    public static String pickPhoto(@Nullable Context c) {
        switch (languageId(c)) {
            case 1: return "ജന്മദിന ഫോട്ടോ ചേർക്കുക";
            case 2: return "जन्मदिन फ़ोटो जोड़ें";
            case 3: return "పుట్టినరోజు ఫోటో జోడించండి";
            case 4: return "பிறந்தநாள் புகைப்படம் சேர்க்கவும்";
            case 5: return "ಜನ್ಮದಿನ ಫೋಟೋ ಸೇರಿಸಿ";
            case 6: return "ଜନ୍ମଦିନ ଫଟୋ ଯୋଗ କରନ୍ତୁ";
            default: return str(c, R.string.birthday_pick_photo, "Tap to add birthday photo");
        }
    }

    public static String startBirthday(@Nullable Context c) {
        switch (languageId(c)) {
            case 1: return "സമർപ്പിച്ച് ചാറ്റ് തുറക്കുക";
            case 2: return "सबमिट करें और चैट खोलें";
            case 3: return "సమర్పించి చాట్ తెరవండి";
            case 4: return "சமர்ப்பித்து அரட்டையைத் திறக்கவும்";
            case 5: return "ಸಲ್ಲಿಸಿ ಚಾಟ್ ತೆರೆಯಿರಿ";
            case 6: return "ଦାଖଲ କରି ଚାଟ୍ ଖୋଲନ୍ତୁ";
            default: return str(c, R.string.birthday_start_chat, "Submit & chat");
        }
    }

    public static String needPhoto(@Nullable Context c) {
        switch (languageId(c)) {
            case 1: return "ജന്മദിന ഫോട്ടോ ചേർക്കുക";
            case 2: return "जन्मदिन फ़ोटो जोड़ें";
            case 3: return "పుట్టినరోజు ఫోటో జోడించండి";
            case 4: return "பிறந்தநாள் புகைப்படம் சேர்க்கவும்";
            case 5: return "ಜನ್ಮದಿನ ಫೋಟೋ ಸೇರಿಸಿ";
            case 6: return "ଜନ୍ମଦିନ ଫଟୋ ଯୋଗ କରନ୍ତୁ";
            default: return str(c, R.string.birthday_need_photo, "Add a birthday photo");
        }
    }

    public static String needLocation(@Nullable Context c) {
        switch (languageId(c)) {
            case 1: return "ഗ്രാമം അല്ലെങ്കിൽ പട്ടണം നൽകുക";
            case 2: return "गाँव या शहर दर्ज करें";
            case 3: return "గ్రామం లేదా పట్టణం ఇవ్వండి";
            case 4: return "கிராமம் அல்லது ஊரை உள்ளிடவும்";
            case 5: return "ಗ್ರಾಮ ಅಥವಾ ಪಟ್ಟಣ ನಮೂದಿಸಿ";
            case 6: return "ଗାଁ କିମ୍ବା ସହର ଲେଖନ୍ତୁ";
            default: return str(c, R.string.birthday_need_location, "Enter village or town");
        }
    }

    public static String pickImage(@Nullable Context c) {
        switch (languageId(c)) {
            case 1: return "ചിത്രം";
            case 2: return "छवि";
            case 3: return "చిత్రం";
            case 4: return "படம்";
            case 5: return "ಚಿತ್ರ";
            case 6: return "ଛବି";
            default: return str(c, R.string.advertise_pick_image, "Image");
        }
    }

    public static String pickVideo(@Nullable Context c) {
        switch (languageId(c)) {
            case 1: return "വീഡിയോ";
            case 2: return "वीडियो";
            case 3: return "వీడియో";
            case 4: return "வீடியோ";
            case 5: return "ವೀಡಿಯೊ";
            case 6: return "ଭିଡିଓ";
            default: return str(c, R.string.advertise_pick_video, "Video");
        }
    }

    public static String tapToPlay(@Nullable Context c) {
        switch (languageId(c)) {
            case 1: return "പ്ലേ ചെയ്യാൻ ടാപ്പ് ചെയ്യുക";
            case 2: return "चलाने के लिए टैप करें";
            case 3: return "ప్లే చేయడానికి టాప్ చేయండి";
            case 4: return "இயக்க தட்டவும்";
            case 5: return "ಪ್ಲೇ ಮಾಡಲು ಟ್ಯಾಪ್ ಮಾಡಿ";
            case 6: return "ଚଲାଇବାକୁ ଟ୍ୟାପ୍ କରନ୍ତୁ";
            default: return str(c, R.string.advertise_tap_play, "Tap to play");
        }
    }

    private static String str(@Nullable Context c, int res, String fallback) {
        try {
            if (c != null) return c.getString(res);
        } catch (Exception ignored) {
        }
        return fallback;
    }
}
