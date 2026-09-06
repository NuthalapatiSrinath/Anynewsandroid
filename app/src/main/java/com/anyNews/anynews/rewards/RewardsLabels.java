package com.anyNews.anynews.rewards;

import android.content.Context;

import androidx.annotation.Nullable;

import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.SharedPrefsData;

/**
 * Rewards hub chrome labels keyed by {@link Constants#LanguageID}
 * (same pattern as ProfileHubLabels).
 */
public final class RewardsLabels {

    private RewardsLabels() {}

    public static String guestHubTitle(@Nullable Context c) {
        switch (languageId(c)) {
            case 1: return "AnyNews റിവാർഡുകളിൽ ചേരുക";
            case 2: return "AnyNews रिवॉर्ड्स में शामिल हों";
            case 3: return "AnyNews రివార్డ్స్‌లో చేరండి";
            case 4: return "AnyNews ரிவார்டுகளில் சேரவும்";
            case 5: return "AnyNews ರಿವಾರ್ಡ್‌ಗಳಿಗೆ ಸೇರಿ";
            case 6: return "AnyNews ରିୱାର୍ଡସ୍‌ରେ ଯୋଗ ଦିଅନ୍ତୁ";
            default: return "Join AnyNews Rewards";
        }
    }

    public static String joinedHubTitle(@Nullable Context c) {
        switch (languageId(c)) {
            case 1: return "AnyNews നാണയങ്ങൾ";
            case 2: return "AnyNews कॉइन्स";
            case 3: return "AnyNews కాయిన్లు";
            case 4: return "AnyNews காய்கள்";
            case 5: return "AnyNews ನಾಣ್ಯಗಳು";
            case 6: return "AnyNews କଏନ୍ସ";
            default: return "AnyNews Coins";
        }
    }

    public static String guestHubSubtitle(@Nullable Context c) {
        switch (languageId(c)) {
            case 1: return "ലോഗിൻ ചെയ്ത് ഓരോ ആഴ്ചയും നാണയങ്ങൾ നേടുക.";
            case 2: return "लॉगिन करें और हर हफ्ते कॉइन्स कमाएँ.";
            case 3: return "లాగిన్ అయి ప్రతి వారం కాయిన్లు సంపాదించండి.";
            case 4: return "உள்நுழைந்து ஒவ்வொரு வாரமும் காய்கள் சம்பாதிக்கவும்.";
            case 5: return "ಲಾಗಿನ್ ಮಾಡಿ ಪ್ರತಿ ವಾರ ನಾಣ್ಯಗಳನ್ನು ಗಳಿಸಿ.";
            case 6: return "ଲଗଇନ୍ କରି ପ୍ରତି ସପ୍ତାହ କଏନ୍ ରୋଜଗାର କରନ୍ତୁ।";
            default: return "Login and keep reading to earn coins every week.";
        }
    }

    public static String joinedHubSubtitle(@Nullable Context c) {
        switch (languageId(c)) {
            case 1: return "വായിച്ച് നാണയങ്ങൾ നേടുക.";
            case 2: return "पढ़कर कॉइन्स कमाएँ.";
            case 3: return "చదివి కాయిన్లు సంపాదించండి.";
            case 4: return "படித்து காய்கள் சம்பாதிக்கவும்.";
            case 5: return "ಓದಿ ನಾಣ್ಯಗಳನ್ನು ಗಳಿಸಿ.";
            case 6: return "ପଢ଼ି କଏନ୍ ରୋଜଗାର କରନ୍ତୁ।";
            default: return "Read and earn coins for the leaderboard.";
        }
    }

    public static String coinsThisWeek(@Nullable Context c) {
        switch (languageId(c)) {
            case 1: return "ഈ ആഴ്ചയിലെ നാണയങ്ങൾ";
            case 2: return "इस सप्ताह के कॉइन्स";
            case 3: return "ఈ వారం కాయిన్లు";
            case 4: return "இந்த வாரம் காய்கள்";
            case 5: return "ಈ ವಾರದ ನಾಣ್ಯಗಳು";
            case 6: return "ଏହି ସପ୍ତାହର କଏନ୍ସ";
            default: return "coins this week";
        }
    }

    public static String loginToRank(@Nullable Context c) {
        switch (languageId(c)) {
            case 1: return "റാങ്കിന് ലോഗിൻ ചെയ്യുക";
            case 2: return "रैंक के लिए लॉगिन करें";
            case 3: return "ర్యాంక్ కోసం లాగిన్ అవ్వండి";
            case 4: return "ரேங்கிற்கு உள்நுழைக";
            case 5: return "ರ್ಯಾಂಕ್‌ಗಾಗಿ ಲಾಗಿನ್ ಮಾಡಿ";
            case 6: return "ର୍ୟାଙ୍କ ପାଇଁ ଲଗଇନ୍ କରନ୍ତୁ";
            default: return "Login to rank";
        }
    }

    public static String rankPrefix(@Nullable Context c, int rank) {
        switch (languageId(c)) {
            case 1: return "റാങ്ക് #" + rank;
            case 2: return "रैंक #" + rank;
            case 3: return "ర్యాంక్ #" + rank;
            case 4: return "ரேங்க் #" + rank;
            case 5: return "ರ್ಯಾಂಕ್ #" + rank;
            case 6: return "ର୍ୟାଙ୍କ #" + rank;
            default: return "Rank #" + rank;
        }
    }

    public static String loginToJoin(@Nullable Context c) {
        switch (languageId(c)) {
            case 1: return "ചേരാൻ ലോഗിൻ ചെയ്യുക";
            case 2: return "शामिल होने के लिए लॉगिन करें";
            case 3: return "చేరడానికి లాగిన్ అవ్వండి";
            case 4: return "சேர உள்நுழைக";
            case 5: return "ಸೇರಲು ಲಾಗಿನ್ ಮಾಡಿ";
            case 6: return "ଯୋଗ ଦେବାକୁ ଲଗଇନ୍ କରନ୍ତୁ";
            default: return "Login to join";
        }
    }

    public static String joinRewards(@Nullable Context c) {
        switch (languageId(c)) {
            case 1: return "റിവാർഡുകളിൽ ചേരുക";
            case 2: return "रिवॉर्ड्स में शामिल हों";
            case 3: return "రివార్డ్స్‌లో చేరండి";
            case 4: return "ரிவார்டுகளில் சேரவும்";
            case 5: return "ರಿವಾರ್ಡ್‌ಗಳಿಗೆ ಸೇರಿ";
            case 6: return "ରିୱାର୍ଡସ୍‌ରେ ଯୋଗ ଦିଅନ୍ତୁ";
            default: return "Join Rewards";
        }
    }

    public static String earnMore(@Nullable Context c) {
        switch (languageId(c)) {
            case 1: return "കൂടുതൽ നേടുക";
            case 2: return "और कमाएँ";
            case 3: return "మరిన్ని సంపాదించండి";
            case 4: return "மேலும் சம்பாதிக்கவும்";
            case 5: return "ಇನ್ನಷ್ಟು ಗಳಿಸಿ";
            case 6: return "ଅଧିକ ରୋଜଗାର କରନ୍ତୁ";
            default: return "Earn more";
        }
    }

    public static String leaderboard(@Nullable Context c) {
        switch (languageId(c)) {
            case 1: return "ലീഡർബോർഡ്";
            case 2: return "लीडरबोर्ड";
            case 3: return "లీడర్‌బోర్డ్";
            case 4: return "லீடர்போர்டு";
            case 5: return "ಲೀಡರ್‌ಬೋರ್ಡ್";
            case 6: return "ଲିଡରବୋର୍ଡ";
            default: return "Leaderboard";
        }
    }

    public static String leaderboardHint(@Nullable Context c) {
        switch (languageId(c)) {
            case 1: return "ഈ ആഴ്ചയിലെ മികച്ച നേട്ടക്കാർ";
            case 2: return "इस सप्ताह के टॉप अर्नर्स";
            case 3: return "ఈ వారం టాప్ సంపాదనదారులు";
            case 4: return "இந்த வாரத்தின் சிறந்த சம்பாதிப்பாளர்கள்";
            case 5: return "ಈ ವಾರದ ಟಾಪ್ ಗಳಿಕೆದಾರರು";
            case 6: return "ଏହି ସପ୍ତାହର ଶୀର୍ଷ ରୋଜଗାରୀ";
            default: return "Top earning performers this week";
        }
    }

    public static String historyShowingLast(@Nullable Context c) {
        switch (languageId(c)) {
            case 1: return "അവസാന 10 കാണിക്കുന്നു";
            case 2: return "पिछले 10 दिखा रहे हैं";
            case 3: return "చివరి 10 చూపిస్తున్నాం";
            case 4: return "கடைசி 10 காட்டப்படுகிறது";
            case 5: return "ಕೊನೆಯ 10 ತೋರಿಸಲಾಗುತ್ತಿದೆ";
            case 6: return "ଶେଷ 10 ଦେଖାଯାଉଛି";
            default: return "Showing last 10";
        }
    }

    public static String redeemedThisWeek(@Nullable Context c, int redeemed) {
        switch (languageId(c)) {
            case 1: return "(" + redeemed + " ഈ ആഴ്ച റിഡീം ചെയ്തു)";
            case 2: return "(" + redeemed + " इस सप्ताह रिडीम)";
            case 3: return "(" + redeemed + " ఈ వారం రిడీమ్)";
            case 4: return "(" + redeemed + " இந்த வாரம் ரிடீம்)";
            case 5: return "(" + redeemed + " ಈ ವಾರ ರಿಡೀಮ್)";
            case 6: return "(" + redeemed + " ଏହି ସପ୍ତାହ ରିଡିମ୍)";
            default: return "(" + redeemed + " redeemed this week)";
        }
    }

    public static String watchAdsWait(@Nullable Context c, int totalSec) {
        int sec = Math.max(0, totalSec);
        int m = sec / 60;
        int s = sec % 60;
        String clock = String.format(java.util.Locale.US, "%d:%02d", m, s);
        switch (languageId(c)) {
            case 1: return "പരസ്യങ്ങൾ — " + clock + " കാത്തിരിക്കുക";
            case 2: return "विज्ञापन — " + clock + " प्रतीक्षा";
            case 3: return "యాడ్స్ — " + clock + " వేచి ఉండండి";
            case 4: return "விளம்பரங்கள் — " + clock + " காத்திருங்கள்";
            case 5: return "ಜಾಹೀರಾತು — " + clock + " ನಿರೀಕ್ಷಿಸಿ";
            case 6: return "ବିଜ୍ଞାପନ — " + clock + " ଅପେକ୍ଷା";
            default: return "Watch ads — wait " + clock;
        }
    }

    public static String ratingPending(@Nullable Context c) {
        switch (languageId(c)) {
            case 1: return "റേറ്റിംഗ് സ്ക്രീൻഷോട്ട് അവലോകനത്തിലാണ്";
            case 2: return "रेटिंग स्क्रीनशॉट समीक्षा में";
            case 3: return "రేటింగ్ స్క్రీన్‌షాట్ సమీక్షలో";
            case 4: return "மதிப்பீட்டு ஸ்கிரீன்ஷாட் மதிப்பாய்வில்";
            case 5: return "ರೇಟಿಂಗ್ ಸ್ಕ್ರೀನ್‌ಶಾಟ್ ಪರಿಶೀಲನೆಯಲ್ಲಿ";
            case 6: return "ରେଟିଂ ସ୍କ୍ରିନସଟ୍ ସମୀକ୍ଷାରେ";
            default: return "Rating screenshot pending review";
        }
    }

    public static String ratingSubmitting(@Nullable Context c) {
        switch (languageId(c)) {
            case 1: return "സമർപ്പിച്ചു — അംഗീകാരം കാത്തിരിക്കുന്നു";
            case 2: return "भेजा गया — स्वीकृति की प्रतीक्षा";
            case 3: return "సమర్పించబడింది — ఆమోదం కోసం వేచి";
            case 4: return "சமர்ப்பிக்கப்பட்டது — ஒப்புதலுக்காக காத்திருக்கிறது";
            case 5: return "ಸಲ್ಲಿಸಲಾಗಿದೆ — ಅನುಮೋದನೆಗಾಗಿ ನಿರೀಕ್ಷೆ";
            case 6: return "ଦାଖଲ — ଅନୁମୋଦନ ଅପେକ୍ଷାରେ";
            default: return "Submitted — pending approval";
        }
    }

    public static String rulesSummary(@Nullable Context c) {
        switch (languageId(c)) {
            case 1:
                return "നിലവാരമുള്ള വായനയ്ക്ക് AnyNews നാണയങ്ങൾ നേടുക. യോഗ്യമായ നാണയങ്ങൾ വാലറ്റിലേക്ക് മാറ്റി UPI-യിലേക്ക് പിൻവലിക്കാം.";
            case 2:
                return "क्वालिटी रीडिंग पर AnyNews कॉइन्स कमाएँ. योग्य कॉइन्स वॉलेट में बदलें और UPI पर निकालें.";
            case 3:
                return "నాణ్యమైన చదవడానికి AnyNews కాయిన్లు సంపాదించండి. అర్హత ఉన్న కాయిన్లను వాలెట్‌కు మార్చి UPIకి విత్‌డ్రా చేయండి.";
            case 4:
                return "தரமான வாசிப்பிற்கு AnyNews காய்கள் சம்பாதியுங்கள். தகுதியான காய்களை வாலட்டிற்கு மாற்றி UPI-க்கு எடுக்கவும்.";
            case 5:
                return "ಗುಣಮಟ್ಟದ ಓದಿಗೆ AnyNews ನಾಣ್ಯಗಳನ್ನು ಗಳಿಸಿ. ಅರ್ಹ ನಾಣ್ಯಗಳನ್ನು ವಾಲೆಟ್‌ಗೆ ಪರಿವರ್ತಿಸಿ UPIಗೆ ಹಿಂಪಡೆಯಿರಿ.";
            case 6:
                return "ଗୁଣବତ୍ତା ପଢ଼ିବା ପାଇଁ AnyNews କଏନ୍ ରୋଜଗାର କରନ୍ତୁ। ଯୋଗ୍ୟ କଏନ୍ ଓୟାଲେଟ୍‌କୁ ରୂପାନ୍ତର କରି UPIରେ ଉଠାନ୍ତୁ।";
            default:
                return "Earn AnyNews Coins for quality reading. Convert eligible coins to your Rewards wallet, then withdraw to UPI when ready.";
        }
    }

    public static String convertCoins(@Nullable Context c) {
        switch (languageId(c)) {
            case 1: return "നാണയങ്ങൾ → ₹";
            case 2: return "कॉइन्स → ₹";
            case 3: return "కాయిన్లు → ₹";
            case 4: return "காய்கள் → ₹";
            case 5: return "ನಾಣ್ಯಗಳು → ₹";
            case 6: return "କଏନ୍ → ₹";
            default: return "Convert coins → ₹";
        }
    }

    public static String coinEarns(@Nullable Context c) {
        switch (languageId(c)) {
            case 1: return "നാണയ ലാഭങ്ങൾ";
            case 2: return "कॉइन कमाई";
            case 3: return "కాయిన్ సంపాదన";
            case 4: return "காய் வருவாய்";
            case 5: return "ನಾಣ್ಯ ಗಳಿಕೆ";
            case 6: return "କଏନ୍ ରୋଜଗାର";
            default: return "Coin earnings";
        }
    }

    public static String walletTitle(@Nullable Context c) {
        switch (languageId(c)) {
            case 1: return "റിവാർഡ് വാലറ്റ്";
            case 2: return "रिवॉर्ड वॉलेट";
            case 3: return "రివార్డ్స్ వాలెట్";
            case 4: return "ரிவார்டு வாலட்";
            case 5: return "ರಿವಾರ್ಡ್ ವಾಲೆಟ್";
            case 6: return "ରିୱାର୍ଡ ଓୟାଲେଟ୍";
            default: return "Rewards wallet";
        }
    }

    public static String saveUpi(@Nullable Context c) {
        switch (languageId(c)) {
            case 1: return "UPI സേവ് ചെയ്യുക";
            case 2: return "UPI सेव करें";
            case 3: return "UPI సేవ్ చేయండి";
            case 4: return "UPI சேமிக்கவும்";
            case 5: return "UPI ಉಳಿಸಿ";
            case 6: return "UPI ସେଭ୍ କରନ୍ତୁ";
            default: return "Save UPI";
        }
    }

    public static String withdraw(@Nullable Context c) {
        switch (languageId(c)) {
            case 1: return "പിൻവലിക്കുക";
            case 2: return "निकालें";
            case 3: return "విత్‌డ్రా";
            case 4: return "எடுக்கவும்";
            case 5: return "ಹಿಂಪಡೆಯಿರಿ";
            case 6: return "ଉଠାନ୍ତୁ";
            default: return "Withdraw";
        }
    }

    public static String history(@Nullable Context c) {
        switch (languageId(c)) {
            case 1: return "ചരിത്രം";
            case 2: return "इतिहास";
            case 3: return "చరిత్ర";
            case 4: return "வரலாறு";
            case 5: return "ಇತಿಹಾಸ";
            case 6: return "ଇତିହାସ";
            default: return "History";
        }
    }

    public static String dailyCheckin(@Nullable Context c, int coins) {
        switch (languageId(c)) {
            case 1: return "ദൈനംദിന ചെക്ക്-ഇൻ (+" + coins + ")";
            case 2: return "दैनिक चेक-इन (+" + coins + ")";
            case 3: return "రోజువారీ చెక్-ఇన్ (+" + coins + ")";
            case 4: return "தினசரி செக்-இன் (+" + coins + ")";
            case 5: return "ದೈನಂದಿನ ಚೆಕ್-ಇನ್ (+" + coins + ")";
            case 6: return "ଦୈନିକ ଚେକ୍-ଇନ୍ (+" + coins + ")";
            default: return "Daily check-in (+" + coins + ")";
        }
    }

    public static String checkedInToday(@Nullable Context c) {
        switch (languageId(c)) {
            case 1: return "ഇന്ന് ചെക്ക്-ഇൻ ചെയ്തു";
            case 2: return "आज चेक-इन हो चुका है";
            case 3: return "ఈరోజు చెక్-ఇన్ అయ్యింది";
            case 4: return "இன்று செக்-இன் முடிந்தது";
            case 5: return "ಇಂದು ಚೆಕ್-ಇನ್ ಆಗಿದೆ";
            case 6: return "ଆଜି ଚେକ୍-ଇନ୍ ହୋଇଛି";
            default: return "Checked in today";
        }
    }

    public static String watchAds(@Nullable Context c, int required, int coins) {
        switch (languageId(c)) {
            case 1: return required + " പരസ്യങ്ങൾ കാണുക (+" + coins + ")";
            case 2: return required + " विज्ञापन देखें (+" + coins + ")";
            case 3: return required + " యాడ్స్ చూడండి (+" + coins + ")";
            case 4: return required + " விளம்பரங்கள் பார்க்க (+" + coins + ")";
            case 5: return required + " ಜಾಹೀರಾತುಗಳನ್ನು ನೋಡಿ (+" + coins + ")";
            case 6: return required + " ବିଜ୍ଞାପନ ଦେଖନ୍ତୁ (+" + coins + ")";
            default: return "Watch " + required + " ads (+" + coins + ")";
        }
    }

    public static String ratingAction(@Nullable Context c, int coins) {
        switch (languageId(c)) {
            case 1: return "Play Store 5★ + സ്ക്രീൻഷോട്ട് (+" + coins + ")";
            case 2: return "Play Store 5★ + स्क्रीनशॉट (+" + coins + ")";
            case 3: return "Play Store 5★ + స్క్రీన్‌షాట్ (+" + coins + ")";
            case 4: return "Play Store 5★ + ஸ்கிரீன்ஷாட் (+" + coins + ")";
            case 5: return "Play Store 5★ + ಸ್ಕ್ರೀನ್‌ಶಾಟ್ (+" + coins + ")";
            case 6: return "Play Store 5★ + ସ୍କ୍ରିନସଟ୍ (+" + coins + ")";
            default: return "Play Store 5★ + screenshot (+" + coins + ")";
        }
    }

    public static String joinFirst(@Nullable Context c) {
        switch (languageId(c)) {
            case 1: return "ആദ്യം റിവാർഡുകളിൽ ചേരുക";
            case 2: return "पहले रिवॉर्ड्स में शामिल हों";
            case 3: return "ముందుగా రివార్డ్స్‌లో చేరండి";
            case 4: return "முதலில் ரிவார்டுகளில் சேரவும்";
            case 5: return "ಮೊದಲು ರಿವಾರ್ಡ್‌ಗಳಿಗೆ ಸೇರಿ";
            case 6: return "ପ୍ରଥମେ ରିୱାର୍ଡସ୍‌ରେ ଯୋଗ ଦିଅନ୍ତୁ";
            default: return "Join rewards first";
        }
    }

    public static String guestChip(@Nullable Context c) {
        switch (languageId(c)) {
            case 1: return "റിവാർഡുകളിൽ ചേരുക";
            case 2: return "रिवॉर्ड्स में शामिल हों";
            case 3: return "రివార్డ్స్‌లో చేరండి";
            case 4: return "ரிவார்டுகளில் சேரவும்";
            case 5: return "ರಿವಾರ್ಡ್‌ಗಳಿಗೆ ಸೇರಿ";
            case 6: return "ରିୱାର୍ଡସ୍‌ରେ ଯୋଗ ଦିଅନ୍ତୁ";
            default: return "Join rewards";
        }
    }

    public static String joinedChip(@Nullable Context c) {
        switch (languageId(c)) {
            case 1: return "റിവാർഡുകൾ";
            case 2: return "रिवॉर्ड्स";
            case 3: return "రివార్డ్స్";
            case 4: return "ரிவார்டுகள்";
            case 5: return "ರಿವಾರ್ಡ್‌ಗಳು";
            case 6: return "ରିୱାର୍ଡସ୍";
            default: return "Rewards";
        }
    }

    public static String goToWallet(@Nullable Context c) {
        switch (languageId(c)) {
            case 1: return "വാലറ്റിലേക്ക് പോകുക";
            case 2: return "वॉलेट पर जाएँ";
            case 3: return "వాలెట్‌కు వెళ్లండి";
            case 4: return "வாலட்டிற்குச் செல்லவும்";
            case 5: return "ವಾಲೆಟ್‌ಗೆ ಹೋಗಿ";
            case 6: return "ଓୟାଲେଟ୍‌କୁ ଯାଆନ୍ତୁ";
            default: return "Go to Wallet";
        }
    }

    public static String walletScreenTitle(@Nullable Context c) {
        switch (languageId(c)) {
            case 1: return "വാലറ്റ്";
            case 2: return "वॉलेट";
            case 3: return "వాలెట్";
            case 4: return "வாலட்";
            case 5: return "ವಾಲೆಟ್";
            case 6: return "ଓୟାଲେଟ୍";
            default: return "Wallet";
        }
    }

    public static String balanceLabel(@Nullable Context c) {
        switch (languageId(c)) {
            case 1: return "ബാലൻസ്";
            case 2: return "बैलेंस";
            case 3: return "బ్యాలెన్స్";
            case 4: return "இருப்பு";
            case 5: return "ಬ್ಯಾಲೆನ್ಸ್";
            case 6: return "ବାଲାନ୍ସ";
            default: return "Balance";
        }
    }

    public static String coinsWord(@Nullable Context c) {
        switch (languageId(c)) {
            case 1: return "നാണയങ്ങൾ";
            case 2: return "कॉइन्स";
            case 3: return "కాయిన్లు";
            case 4: return "காய்கள்";
            case 5: return "ನಾಣ್ಯಗಳು";
            case 6: return "କଏନ୍ସ";
            default: return "coins";
        }
    }

    public static String upiHint(@Nullable Context c) {
        return "UPI ID";
    }

    public static String enterUpi(@Nullable Context c) {
        switch (languageId(c)) {
            case 1: return "UPI ID നൽകുക";
            case 2: return "UPI ID दर्ज करें";
            case 3: return "UPI ID నమోదు చేయండి";
            case 4: return "UPI ID உள்ளிடவும்";
            case 5: return "UPI ID ನಮೂದಿಸಿ";
            case 6: return "UPI ID ଲେଖନ୍ତୁ";
            default: return "Enter UPI ID";
        }
    }

    public static String noHistory(@Nullable Context c) {
        switch (languageId(c)) {
            case 1: return "ചരിത്രമില്ല";
            case 2: return "अभी कोई इतिहास नहीं";
            case 3: return "ఇంకా చరిత్ర లేదు";
            case 4: return "இன்னும் வரலாறு இல்லை";
            case 5: return "ಇನ್ನೂ ಇತಿಹಾಸವಿಲ್ಲ";
            case 6: return "ଏପର୍ଯ୍ୟନ୍ତ ଇତିହାସ ନାହିଁ";
            default: return "No history yet";
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
}
