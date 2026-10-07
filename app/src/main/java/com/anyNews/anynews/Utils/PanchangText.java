package com.anyNews.anynews.Utils;

import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Display translations for Telugu panchang pack strings.
 * Language id 3 never reaches here — callers return the Telugu source unchanged.
 * Times stay numeric; only labels, names, and day-part words change.
 */
final class PanchangText {

    private PanchangText() {}

    /** ml, hi, ta, kn, or, en, bn, mr, gu */
    private static final Map<String, String[]> NAMES = new HashMap<>();

    private static final String[] SAM_TE = {
            "ప్రభవ", "విభవ", "శుక్ల", "ప్రమోదూత", "ప్రజోత్పత్తి", "ఆంగీరస", "శ్రీముఖ", "భావ",
            "యువ", "ధాత", "ఈశ్వర", "బహుధాన్య", "ప్రమాధి", "విక్రమ", "వృష", "చిత్రభాను",
            "స్వభాను", "తారణ", "పార్థివ", "వ్యయ", "సర్వజిత్", "సర్వధారి", "విరోధి", "వికృతి",
            "ఖర", "నందన", "విజయ", "జయ", "మన్మథ", "దుర్ముఖి", "హేవిళంబి", "విళంబి",
            "వికారి", "శార్వరి", "ప్లవ", "శుభకృత్", "శోభకృత్", "క్రోధి", "విశ్వావసు", "పరాభవ",
            "ప్లవంగ", "కీలక", "సౌమ్య", "సాధారణ", "విరోధికృత్", "పరీధావి", "ప్రమాదీచ", "ఆనంద",
            "రాక్షస", "నల", "పింగళ", "కాళయుక్తి", "సిద్ధార్థి", "రౌద్రి", "దుర్మతి", "దుందుభి",
            "రుధిరోద్గారి", "రక్తాక్షి", "క్రోధన", "అక్షయ"
    };
    private static final String[] SAM_HI = {
            "प्रभव", "विभव", "शुक्ल", "प्रमोदूत", "प्रजोत्पत्ति", "आंगीरस", "श्रीमुख", "भाव",
            "युव", "धाता", "ईश्वर", "बहुधान्य", "प्रमाथी", "विक्रम", "वृष", "चित्रभानु",
            "स्वभानु", "तारण", "पार्थिव", "व्यय", "सर्वजित्", "सर्वधारी", "विरोधी", "विकृति",
            "खर", "नंदन", "विजय", "जय", "मन्मथ", "दुर्मुखी", "हेविलम्बी", "विलम्बी",
            "विकारी", "शार्वरी", "प्लव", "शुभकृत्", "शोभकृत्", "क्रोधी", "विश्वावसु", "पराभव",
            "प्लवंग", "कीलक", "सौम्य", "साधारण", "विरोधिकृत्", "परीधावी", "प्रमादीच", "आनंद",
            "राक्षस", "नल", "पिंगल", "कालयुक्ति", "सिद्धार्थी", "रौद्री", "दुर्मति", "दुंदुभि",
            "रुधिरोद्गारी", "रक्ताक्षी", "क्रोधन", "अक्षय"
    };
    private static final String[] SAM_EN = {
            "Prabhava", "Vibhava", "Shukla", "Pramodoota", "Prajotpatti", "Angirasa", "Shrimukha", "Bhava",
            "Yuva", "Dhata", "Ishvara", "Bahudhanya", "Pramathi", "Vikrama", "Vrisha", "Chitrabhanu",
            "Svabhanu", "Tarana", "Parthiva", "Vyaya", "Sarvajit", "Sarvadhari", "Virodhi", "Vikriti",
            "Khara", "Nandana", "Vijaya", "Jaya", "Manmatha", "Durmukhi", "Hevilambi", "Vilambi",
            "Vikari", "Sharvari", "Plava", "Shubhakrit", "Shobhakrit", "Krodhi", "Vishvavasu", "Parabhava",
            "Plavanga", "Kilaka", "Saumya", "Sadharana", "Virodhikrit", "Paridhavi", "Pramadicha", "Ananda",
            "Rakshasa", "Nala", "Pingala", "Kalayukti", "Siddharthi", "Raudri", "Durmati", "Dundubhi",
            "Rudhirodgari", "Raktakshi", "Krodhana", "Akshaya"
    };
    private static final String[] SAM_ML = {
            "പ്രഭവ", "വിഭവ", "ശുക്ല", "പ്രമോദൂത", "പ്രജോത്പത്തി", "ആംഗീരസ", "ശ്രീമുഖ", "ഭാവ",
            "യുവ", "ധാതാ", "ഈശ്വര", "ബഹുധാന്യ", "പ്രമാഥി", "വിക്രമ", "വൃഷ", "ചിത്രഭാനു",
            "സ്വഭാനു", "താരണ", "പാർഥിവ", "വ്യയ", "സർവജിത്", "സർവധാരി", "വിരോധി", "വികൃതി",
            "ഖര", "നന്ദന", "വിജയ", "ജയ", "മന്മഥ", "ദുർമുഖി", "ഹേവിളംബി", "വിളംബി",
            "വികാരി", "ശാർവരി", "പ്ലവ", "ശുഭകൃത്", "ശോഭകൃത്", "ക്രോധി", "വിശ്വാവസു", "പരാഭവ",
            "പ്ലവംഗ", "കീലക", "സൗമ്യ", "സാധാരണ", "വിരോധികൃത്", "പരീധാവി", "പ്രമാദീച", "ആനന്ദ",
            "രാക്ഷസ", "നള", "പിംഗള", "കാളയുക്തി", "സിദ്ധാർഥി", "രൗദ്രി", "ദുർമതി", "ദുന്ദുഭി",
            "രുധിരോദ്ഗാരി", "രക്താക്ഷി", "ക്രോധന", "അക്ഷയ"
    };
    private static final String[] SAM_TA = {
            "பிரபவ", "விபவ", "சுக்ல", "பிரமோதூத", "பிரசோற்பத்தி", "ஆங்கீரச", "ஸ்ரீமுக", "பாவ",
            "யுவ", "தாது", "ஈஸ்வர", "பகுதான்ய", "பிரமாதி", "விக்கிரம", "விருஷ", "சித்திரபானு",
            "சுபானு", "தாரண", "பார்த்திவ", "விய", "சர்வஜித்", "சர்வதாரி", "விரோதி", "விக்ருதி",
            "கர", "நந்தன", "விஜய", "ஜய", "மன்மத", "துர்முகி", "ஹேவிளம்பி", "விளம்பி",
            "விகாரி", "சார்வரி", "ப்லவ", "சுபகிருது", "சோபகிருது", "குரோதி", "விசுவாவசு", "பராபவ",
            "ப்லவங்க", "கீலக", "சௌமிய", "சாதாரண", "விரோதிகிருது", "பரீதாபி", "பிரமாதீச", "ஆனந்த",
            "ராக்ஷச", "நள", "பிங்கள", "காளயுக்தி", "சித்தார்த்தி", "ரௌத்திரி", "துர்மதி", "துந்துபி",
            "ருத்ரோத்காரி", "ரக்தாக்ஷி", "குரோதன", "அட்சய"
    };
    private static final String[] SAM_KN = {
            "ಪ್ರಭವ", "ವಿಭವ", "ಶುಕ್ಲ", "ಪ್ರಮೋದೂತ", "ಪ್ರಜೋತ್ಪತ್ತಿ", "ಆಂಗೀರಸ", "ಶ್ರೀಮುಖ", "ಭಾವ",
            "ಯುವ", "ಧಾತ", "ಈಶ್ವರ", "ಬಹುಧಾನ್ಯ", "ಪ್ರಮಾಥಿ", "ವಿಕ್ರಮ", "ವೃಷ", "ಚಿತ್ರಭಾನು",
            "ಸ್ವಭಾನು", "ತಾರಣ", "ಪಾರ್ಥಿವ", "ವ್ಯಯ", "ಸರ್ವಜಿತ್", "ಸರ್ವಧಾರಿ", "ವಿರೋಧಿ", "ವಿಕೃತಿ",
            "ಖರ", "ನಂದನ", "ವಿಜಯ", "ಜಯ", "ಮನ್ಮಥ", "ದುರ್ಮುಖಿ", "ಹೇವಿಳಂಬಿ", "ವಿಳಂಬಿ",
            "ವಿಕಾರಿ", "ಶಾರ್ವರಿ", "ಪ್ಲವ", "ಶುಭಕೃತ್", "ಶೋಭಕೃತ್", "ಕ್ರೋಧಿ", "ವಿಶ್ವಾವಸು", "ಪರಾಭವ",
            "ಪ್ಲವಂಗ", "ಕೀಲಕ", "ಸೌಮ್ಯ", "ಸಾಧಾರಣ", "ವಿರೋಧಿಕೃತ್", "ಪರೀಧಾವಿ", "ಪ್ರಮಾದೀಚ", "ಆನಂದ",
            "ರಾಕ್ಷಸ", "ನಲ", "ಪಿಂಗಳ", "ಕಾಳಯುಕ್ತಿ", "ಸಿದ್ಧಾರ್ಥಿ", "ರೌದ್ರಿ", "ದುರ್ಮತಿ", "ದುಂದುಭಿ",
            "ರುಧಿರೋದ್ಗಾರಿ", "ರಕ್ತಾಕ್ಷಿ", "ಕ್ರೋಧನ", "ಅಕ್ಷಯ"
    };
    private static final String[] SAM_OR = {
            "ପ୍ରଭବ", "ବିଭବ", "ଶୁକ୍ଳ", "ପ୍ରମୋଦୂତ", "ପ୍ରଜୋତ୍ପତ୍ତି", "ଆଙ୍ଗୀରସ", "ଶ୍ରୀମୁଖ", "ଭାବ",
            "ଯୁବ", "ଧାତା", "ଈଶ୍ୱର", "ବହୁଧାନ୍ୟ", "ପ୍ରମାଥୀ", "ବିକ୍ରମ", "ବୃଷ", "ଚିତ୍ରଭାନୁ",
            "ସ୍ୱଭାନୁ", "ତାରଣ", "ପାର୍ଥିବ", "ବ୍ୟୟ", "ସର୍ବଜିତ୍", "ସର୍ବଧାରୀ", "ବିରୋଧୀ", "ବିକୃତି",
            "ଖର", "ନନ୍ଦନ", "ବିଜୟ", "ଜୟ", "ମନ୍ମଥ", "ଦୁର୍ମୁଖୀ", "ହେବିଳମ୍ବି", "ବିଳମ୍ବି",
            "ବିକାରୀ", "ଶାର୍ବରୀ", "ପ୍ଳବ", "ଶୁଭକୃତ୍", "ଶୋଭକୃତ୍", "କ୍ରୋଧୀ", "ବିଶ୍ୱାବସୁ", "ପରାଭବ",
            "ପ୍ଳବଙ୍ଗ", "କୀଳକ", "ସୌମ୍ୟ", "ସାଧାରଣ", "ବିରୋଧିକୃତ୍", "ପରୀଧାବୀ", "ପ୍ରମାଦୀଚ", "ଆନନ୍ଦ",
            "ରାକ୍ଷସ", "ନଳ", "ପିଙ୍ଗଳ", "କାଳଯୁକ୍ତି", "ସିଦ୍ଧାର୍ଥୀ", "ରୌଦ୍ରୀ", "ଦୁର୍ମତି", "ଦୁନ୍ଦୁଭି",
            "ରୁଧିରୋଦ୍ଗାରୀ", "ରକ୍ତାକ୍ଷୀ", "କ୍ରୋଧନ", "ଅକ୍ଷୟ"
    };
    private static final String[] SAM_BN = {
            "প্রভব", "বিভব", "শুক্ল", "প্রমোদূত", "প্রজোৎপত্তি", "আঙ্গীরস", "শ্রীমুখ", "ভাব",
            "যুব", "ধাতা", "ঈশ্বর", "বহুধান্য", "প্রমাথী", "বিক্রম", "বৃষ", "চিত্রভানু",
            "স্বভানু", "তারণ", "পার্থিব", "ব্যয়", "সর্বজিৎ", "সর্বধারী", "বিরোধী", "বিকৃতি",
            "খর", "নন্দন", "বিজয়", "জয়", "মন্মথ", "দুর্মুখী", "হেবিলম্বী", "বিলম্বী",
            "বিকারী", "শার্বরী", "প্লব", "শুভকৃত্", "শোভকৃত্", "ক্রোধী", "বিশ্বাবসু", "পরাভব",
            "প্লবঙ্গ", "কীলক", "সৌম্য", "সাধারণ", "বিরোধিকৃত্", "পরীধাবী", "প্রমাদীচ", "আনন্দ",
            "রাক্ষস", "নল", "পিঙ্গল", "কালযুক্তি", "সিদ্ধার্থী", "রৌদ্রী", "দুর্মতি", "দুন্দুভি",
            "রুধিরোদ্গারী", "রক্তাক্ষী", "ক্রোধন", "অক্ষয়"
    };
    private static final String[] SAM_MR = {
            "प्रभव", "विभव", "शुक्ल", "प्रमोदूत", "प्रजोत्पत्ती", "आंगीरस", "श्रीमुख", "भाव",
            "युव", "धाता", "ईश्वर", "बहुधान्य", "प्रमाथी", "विक्रम", "वृष", "चित्रभानू",
            "स्वभानू", "तारण", "पार्थिव", "व्यय", "सर्वजित्", "सर्वधारी", "विरोधी", "विकृती",
            "खर", "नंदन", "विजय", "जय", "मन्मथ", "दुर्मुखी", "हेविळंबी", "विळंबी",
            "विकारी", "शार्वरी", "प्लव", "शुभकृत", "शोभकृत", "क्रोधी", "विश्वावसु", "पराभव",
            "प्लवंग", "कीलक", "सौम्य", "साधारण", "विरोधिकृत", "परीधावी", "प्रमादीच", "आनंद",
            "राक्षस", "नल", "पिंगळ", "काळयुक्ती", "सिद्धार्थी", "रौद्री", "दुर्मती", "दुंदुभी",
            "रुधिरोद्गारी", "रक्ताक्षी", "क्रोधन", "अक्षय"
    };
    private static final String[] SAM_GU = {
            "પ્રભવ", "વિભવ", "શુક્લ", "પ્રમોદૂત", "પ્રજોત્પત્તિ", "આંગીરસ", "શ્રીમુખ", "ભાવ",
            "યુવ", "ધાતા", "ઈશ્વર", "બહુધાન્ય", "પ્રમાથી", "વિક્રમ", "વૃષ", "ચિત્રભાનુ",
            "સ્વભાનુ", "તારણ", "પાર્થિવ", "વ્યય", "સર્વજિત્", "સર્વધારી", "વિરોધી", "વિકૃતિ",
            "ખર", "નંદન", "વિજય", "જય", "મન્મથ", "દુર્મુખી", "હેવિળંબી", "વિળંબી",
            "વિકારી", "શાર્વરી", "પ્લવ", "શુભકૃત્", "શોભકૃત્", "ક્રોધી", "વિશ્વાવસુ", "પરાભવ",
            "પ્લવંગ", "કીલક", "સૌમ્ય", "સાધારણ", "વિરોધિકૃત્", "પરીધાવી", "પ્રમાદીચ", "આનંદ",
            "રાક્ષસ", "નલ", "પિંગળ", "કાળયુક્તિ", "સિદ્ધાર્થી", "રૌદ્રી", "દુર્મતિ", "દુંદુભિ",
            "રુધિરોદ્ગારી", "રક્તાક્ષી", "ક્રોધન", "અક્ષય"
    };

    private static final String[] MASA_TE = {
            "చైత్రం", "వైశాఖం", "జ్యేష్ఠం", "ఆషాఢం", "శ్రావణం", "భాద్రపదం",
            "ఆశ్వయుజం", "కార్తీకం", "మార్గశిరం", "పుష్యం", "మాఘం", "ఫాల్గుణం"
    };
    private static final String[] MASA_HI = {
            "चैत्र", "वैशाख", "ज्येष्ठ", "आषाढ़", "श्रावण", "भाद्रपद",
            "आश्वयुज", "कार्तिक", "मार्गशीर्ष", "पुष्य", "माघ", "फाल्गुन"
    };
    private static final String[] MASA_EN = {
            "Chaitra", "Vaisakha", "Jyeshtha", "Ashadha", "Shravana", "Bhadrapada",
            "Ashwayuja", "Kartika", "Margashira", "Pushya", "Magha", "Phalguna"
    };
    private static final String[] MASA_ML = {
            "ചൈത്രം", "വൈശാഖം", "ജ്യേഷ്ഠം", "ആഷാഢം", "ശ്രാവണം", "ഭാദ്രപദം",
            "ആശ്വയുജം", "കാർത്തികം", "മാർഗശിരം", "പുഷ്യം", "മാഘം", "ഫാൽഗുണം"
    };
    private static final String[] MASA_TA = {
            "சைத்திரம்", "வைசாகம்", "ஜ்யேஷ்டம்", "ஆஷாடம்", "ஸ்ராவணம்", "பாத்ரபதம்",
            "ஆஸ்வயுஜம்", "கார்த்திகம்", "மார்கசிரம்", "புஷ்யம்", "மாகம்", "பால்குணம்"
    };
    private static final String[] MASA_KN = {
            "ಚೈತ್ರ", "ವೈಶಾಖ", "ಜ್ಯೇಷ್ಠ", "ಆಷಾಢ", "ಶ್ರಾವಣ", "ಭಾದ್ರಪದ",
            "ಆಶ್ವಯುಜ", "ಕಾರ್ತೀಕ", "ಮಾರ್ಗಶಿರ", "ಪುಷ್ಯ", "ಮಾಘ", "ಫಾಲ್ಗುಣ"
    };
    private static final String[] MASA_OR = {
            "ଚୈତ୍ର", "ବୈଶାଖ", "ଜ୍ୟେଷ୍ଠ", "ଆଷାଢ଼", "ଶ୍ରାବଣ", "ଭାଦ୍ରପଦ",
            "ଆଶ୍ୱୟୁଜ", "କାର୍ତ୍ତିକ", "ମାର୍ଗଶିର", "ପୁଷ୍ୟ", "ମାଘ", "ଫାଲ୍ଗୁନ"
    };
    private static final String[] MASA_BN = {
            "চৈত্র", "বৈশাখ", "জ্যেষ্ঠ", "আষাঢ়", "শ্রাবণ", "ভাদ্র",
            "আশ্বিন", "কার্তিক", "অগ্রহায়ণ", "পৌষ", "মাঘ", "ফাল্গুন"
    };
    private static final String[] MASA_MR = {
            "चैत्र", "वैशाख", "ज्येष्ठ", "आषाढ", "श्रावण", "भाद्रपद",
            "आश्विन", "कार्तिक", "मार्गशीर्ष", "पुष्य", "माघ", "फाल्गुन"
    };
    private static final String[] MASA_GU = {
            "ચૈત્ર", "વૈશાખ", "જ્યેષ્ઠ", "આષાઢ", "શ્રાવણ", "ભાદરવો",
            "આસો", "કારતક", "માગશર", "પોષ", "મહા", "ફાગણ"
    };

    /** Telugu cell abbreviations, same order as the 27 nakshatras. */
    private static final String[] NAK_SHORT_TE = {
            "అశ్వి", "భరణి", "కృత్తి", "రోహి", "మృగ", "ఆర్ద్ర", "పున",
            "పుష్య", "ఆశ్లే", "మఖ", "పుబ్బ", "ఉత్తర", "హస్త", "చిత్త", "స్వాతి",
            "విశా", "అను", "జ్యేష్ఠ", "మూల", "పూ.షా", "ఉ.షా", "శ్రవ",
            "ధని", "శత", "పూ.భా", "ఉ.భా", "రేవ"
    };
    private static final String[] NAK_SHORT_HI = {
            "अश्वि", "भरणी", "कृत्ति", "रोहि", "मृग", "आर्द्रा", "पुनर्",
            "पुष्य", "आश्ले", "मघा", "पू.फा", "उ.फा", "हस्त", "चित्रा", "स्वाति",
            "विशा", "अनु", "ज्येष्ठ", "मूल", "पू.षा", "उ.षा", "श्रव",
            "धनि", "शत", "पू.भा", "उ.भा", "रेव"
    };
    private static final String[] NAK_SHORT_EN = {
            "Ashwini", "Bharani", "Krittika", "Rohini", "Mrig", "Ardra", "Punar",
            "Pushya", "Ashlesha", "Magha", "P.Phalgu", "U.Phalgu", "Hasta", "Chitra", "Swati",
            "Vishakha", "Anuradha", "Jyeshtha", "Mula", "P.Ashad", "U.Ashad", "Shravana",
            "Dhanish", "Shatabhi", "P.Bhadra", "U.Bhadra", "Revati"
    };
    private static final String[] NAK_SHORT_ML = {
            "അശ്വതി", "ഭരണി", "കാർത്തിക", "രോഹിണി", "മകയിരം", "തിരുവാതിര", "പുണർതം",
            "പൂയം", "ആയില്യം", "മകം", "പൂരം", "ഉത്രം", "അത്തം", "ചിത്തിര", "ചോതി",
            "വിശാഖം", "അനിഴം", "തൃക്കേട്ട", "മൂലം", "പൂരാടം", "ഉത്രാടം", "തിരുവോണം",
            "അവിട്ടം", "ചതയം", "പൂരുരുട്ടാ", "ഉത്രട്ടാതി", "രേവതി"
    };
    private static final String[] NAK_SHORT_TA = {
            "அசுவினி", "பரணி", "கார்த்திகை", "ரோகிணி", "மிருகசீரிடம்", "திருவாதிரை", "புனர்பூசம்",
            "பூசம்", "ஆயில்யம்", "மகம்", "பூரம்", "உத்திரம்", "ஹஸ்தம்", "சித்திரை", "சுவாதி",
            "விசாகம்", "அனுஷம்", "கேட்டை", "மூலம்", "பூராடம்", "உத்திராடம்", "திருவோணம்",
            "அவிட்டம்", "சதயம்", "பூரட்டாதி", "உத்திரட்டாதி", "ரேவதி"
    };
    private static final String[] NAK_SHORT_KN = {
            "ಅಶ್ವಿ", "ಭರಣಿ", "ಕೃತ್ತಿ", "ರೋಹಿ", "ಮೃಗ", "ಆರ್ದ್ರ", "ಪುನರ್",
            "ಪುಷ್ಯ", "ಆಶ್ಲೇ", "ಮಘ", "ಪೂ.ಫಾ", "ಉ.ಫಾ", "ಹಸ್ತ", "ಚಿತ್ರ", "ಸ್ವಾತಿ",
            "ವಿಶಾ", "ಅನು", "ಜ್ಯೇಷ್ಠ", "ಮೂಲ", "ಪೂ.ಷಾ", "ಉ.ಷಾ", "ಶ್ರವ",
            "ಧನಿ", "ಶತ", "ಪೂ.ಭಾ", "ಉ.ಭಾ", "ರೇವ"
    };
    private static final String[] NAK_SHORT_OR = {
            "ଅଶ୍ୱି", "ଭରଣୀ", "କୃତ୍ତି", "ରୋହି", "ମୃଗ", "ଆର୍ଦ୍ରା", "ପୁନର",
            "ପୁଷ୍ୟ", "ଆଶ୍ଲେ", "ମଘା", "ପୂ.ଫା", "ଉ.ଫା", "ହସ୍ତ", "ଚିତ୍ରା", "ସ୍ୱାତୀ",
            "ବିଶା", "ଅନୁ", "ଜ୍ୟେଷ୍ଠ", "ମୂଳ", "ପୂ.ଷା", "ଉ.ଷା", "ଶ୍ରବ",
            "ଧନି", "ଶତ", "ପୂ.ଭା", "ଉ.ଭା", "ରେବ"
    };
    private static final String[] NAK_SHORT_BN = {
            "অশ্বি", "ভরণী", "কৃত্তি", "রোহি", "মৃগ", "আর্দ্রা", "পুনর",
            "পুষ্য", "অশ্লে", "মঘা", "পূ.ফা", "উ.ফা", "হস্ত", "চিত্রা", "স্বাতী",
            "বিশা", "অনু", "জ্যেষ্ঠ", "মূল", "পূ.ষা", "উ.ষা", "শ্রব",
            "ধনি", "শত", "পূ.ভা", "উ.ভা", "রেব"
    };
    private static final String[] NAK_SHORT_MR = {
            "अश्वि", "भरणी", "कृत्ति", "रोहि", "मृग", "आर्द्रा", "पुनर्",
            "पुष्य", "आश्ले", "मघा", "पू.फा", "उ.फा", "हस्त", "चित्रा", "स्वाती",
            "विशा", "अनु", "ज्येष्ठ", "मूळ", "पू.षा", "उ.षा", "श्रव",
            "धनि", "शत", "पू.भा", "उ.भा", "रेव"
    };
    private static final String[] NAK_SHORT_GU = {
            "અશ્વિ", "ભરણી", "કૃત્તિ", "રોહિ", "મૃગ", "આર્દ્રા", "પુનર",
            "પુષ્ય", "આશ્લે", "મઘા", "પૂ.ફા", "ઉ.ફા", "હસ્ત", "ચિત્રા", "સ્વાતિ",
            "વિશા", "અનુ", "જ્યેષ્ઠ", "મૂળ", "પૂ.ષા", "ઉ.ષા", "શ્રવ",
            "ધનિ", "શત", "પૂ.ભા", "ઉ.ભા", "રેવ"
    };

    private static final Pattern HEADER = Pattern.compile(
            "శ్రీ\\s+(.+?)\\s+నామ సంవత్సరం,\\s+(.+?)\\s+మాసం");
    private static final Pattern CLOCK = Pattern.compile(
            "(రా\\.|ఉ\\.|మ\\.|సా\\.)\\s*(\\d{1,2}\\.\\d{2})");

    static {
        put("రాహుకాలం", "രാഹുകാലം", "राहुकाल", "ராகுகாலம்", "ರಾಹುಕಾಲ", "ରାହୁକାଳ",
                "Rahukalam", "রাহুকাল", "राहुकाळ", "રાહુકાળ");
        put("యమగండం", "യമഗണ്ഡം", "यमगंड", "எமகண்டம்", "ಯಮಗಂಡ", "ଯମଗଣ୍ଡ",
                "Yamagandam", "যমগণ্ড", "यमगंड", "યમગંડ");
        put("గుళికా", "ഗുലിക", "गुलिक", "குளிகை", "ಗುಳಿಕ", "ଗୁଳିକ",
                "Gulika", "গুলিকা", "गुळिक", "ગુલિક");
        put("గులికా", "ഗുലിക", "गुलिक", "குளிகை", "ಗುಳಿಕ", "ଗୁଳିକ",
                "Gulika", "গুলিকা", "गुळिक", "ગુલિક");
        put("దుర్ముహూర్తం", "ദുർമുഹൂർത്തം", "दुर्मुहूर्त", "துர்முகூர்த்தம்", "ದುರ್ಮುಹೂರ್ತ", "ଦୁର୍ମୁହୂର୍ତ୍ତ",
                "Durmuhurtam", "দুর্মুহূর্ত", "दुर्मुहूर्त", "દુર્મુહૂર્ત");
        put("వర్జ్యం", "വർജ്യം", "वर्ज्य", "வர்ஜ்யம்", "ವರ್ಜ್ಯ", "ବର୍ଜ୍ୟ",
                "Varjyam", "বর্জ্য", "वर्ज्य", "વર્જ્ય");
        put("అభిజిత్ ముహూర్తాలు", "അഭിജിത് മുഹൂർത്തം", "अभिजित मुहूर्त", "அபிஜித் முகூர்த்தம்",
                "ಅಭಿಜಿತ್ ಮುಹೂರ್ತ", "ଅଭିଜିତ ମୁହୂର୍ତ୍ତ", "Abhijit muhurta", "অভিজিত মুহূর্ত",
                "अभिजित मुहूर्त", "અભિજિત મુહૂર્ત");
        put("అమృతకాలము", "അമൃതകാലം", "अमृत काल", "அமிர்தகாலம்", "ಅಮೃತಕಾಲ", "ଅମୃତକାଳ",
                "Amrit kalam", "অমৃতকাল", "अमृतकाळ", "અમૃતકાળ");
        put("బ్రహ్మ ముహూర్తం", "ബ്രഹ്മമുഹൂർത്തം", "ब्रह्म मुहूर्त", "பிரம்ம முகூர்த்தம்",
                "ಬ್ರಹ್ಮ ಮುಹೂರ್ತ", "ବ୍ରହ୍ମ ମୁହୂର୍ତ୍ତ", "Brahma muhurta", "ব্রহ্ম মুহূর্ত",
                "ब्रह्म मुहूर्त", "બ્રહ્મ મુહૂર્ત");
        put("వాహన కొనుగోలు", "വാഹന വാങ്ങൽ", "वाहन खरीद", "வாகன வாங்குதல்", "ವಾಹನ ಖರೀದಿ", "ଯାନ କ୍ରୟ",
                "Vehicle purchase", "যানবাহন কেনা", "वाहन खरेदी", "વાહન ખરીદી");
        put("అంతర్జాతీయ మహిళా దినోత్సవం", "അന്താരാഷ്ട്ര വനിതാ ദിനം", "अंतरराष्ट्रीय महिला दिवस",
                "சர்வதேச மகளிர் தினம்", "ಅಂತರರಾಷ್ಟ್ರೀಯ ಮಹಿಳಾ ದಿನ", "ଆନ୍ତର୍ଜାତିକ ମହିଳା ଦିବସ",
                "International Women's Day", "আন্তর্জাতিক নারী দিবস", "आंतरराष्ट्रीय महिला दिन",
                "આંતરરાષ્ટ્રીય મહિલા દિન");
        put("ఆంధ్రప్రదేశ్ నిర్మాణ దినోత్సవం", "ആന്ധ്രാപ്രദേശ് രൂപവത്കരണ ദിനം", "आंध्र प्रदेश स्थापना दिवस",
                "ஆந்திரப் பிரதேச உருவாக்க தினம்", "ಆಂಧ್ರಪ್ರದೇಶ ಸ್ಥಾಪನಾ ದಿನ", "ଆନ୍ଧ୍ରପ୍ରଦେଶ ଗଠନ ଦିବସ",
                "Andhra Pradesh Formation Day", "অন্ধ্রপ্রদেশ প্রতিষ্ঠা দিবস", "आंध्र प्रदेश स्थापना दिन",
                "આંધ્ર પ્રદેશ સ્થાપના દિન");
        put("ఉగాది", "ഉഗാദി", "उगादि", "உகாதி", "ಯುಗಾದಿ", "ଉଗାଦି",
                "Ugadi", "উগাদি", "उगादी", "ઉગાડી");
        put("కనుమ", "കനുമ", "कनुमा", "கனுமா", "ಕನುಮ", "କନୁମ",
                "Kanuma", "কনুমা", "कनुमा", "કનુમા");
        put("కార్మిక దినోత్సవం", "തൊഴിലാളി ദിനം", "मजदूर दिवस", "தொழிலாளர் தினம்", "ಕಾರ್ಮಿಕರ ದಿನ",
                "ଶ୍ରମିକ ଦିବସ", "Labour Day", "মে দিবস", "कामगार दिन", "મજૂર દિન");
        put("క్రిస్మస్", "ക്രിസ്മസ്", "क्रिसमस", "கிறிஸ்துமஸ்", "ಕ್ರಿಸ್ಮಸ್", "ଖ୍ରୀଷ୍ଟମାସ",
                "Christmas", "ক্রিসমাস", "ख्रिसमस", "નાતાલ");
        put("గణతంత్ర దినోత్సవం", "റിപ്പബ്ലിക് ദിനം", "गणतंत्र दिवस", "குடியரசு தினம்", "ಗಣರಾಜ್ಯೋತ್ಸವ",
                "ଗଣତନ୍ତ୍ର ଦିବସ", "Republic Day", "প্রজাতন্ত্র দিবস", "प्रजासत्ताक दिन", "પ્રજાસત્તાક દિન");
        put("గణేష్ చతుర్థి", "ഗണേശ ചതുർത്ഥി", "गणेश चतुर्थी", "விநாயகர் சதுர்த்தி", "ಗಣೇಶ ಚತುರ್ಥಿ",
                "ଗଣେଶ ଚତୁର୍ଥୀ", "Ganesh Chaturthi", "গণেশ চতুর্থী", "गणेश चतुर्थी", "ગણેશ ચતુર્થી");
        put("గాంధీ జయంతి", "ഗാന്ധി ജയന്തി", "गांधी जयंती", "காந்தி ஜயந்தி", "ಗಾಂಧಿ ಜಯಂತಿ",
                "ଗାନ୍ଧୀ ଜୟନ୍ତୀ", "Gandhi Jayanti", "গান্ধী জয়ন্তী", "गांधी जयंती", "ગાંધી જયંતિ");
        put("దసరా", "വിജയദശമി", "दशहरा", "விஜயதசமி", "ದಸರಾ", "ଦଶହରା",
                "Dussehra", "বিজয়াদশমী", "दसरा", "દશેરા");
        put("దీపావళి", "ദീപാവലി", "दीपावली", "தீபாவளி", "ದೀಪಾವಳಿ", "ଦୀପାବଳି",
                "Diwali", "দীপাবলি", "दिवाळी", "દિવાળી");
        put("నూతన సంవత్సరాది", "നവവർഷം", "नव वर्ष", "புத்தாண்டு", "ಹೊಸ ವರ್ಷ", "ନୂଆବର୍ଷ",
                "New Year", "নববর্ষ", "नववर्ष", "નૂતન વર્ષ");
        put("భోగి", "ഭോഗി", "भोगी", "போகி", "ಭೋಗಿ", "ଭୋଗି",
                "Bhogi", "ভোগি", "भोगी", "ભોગી");
        put("మకర సంక్రాంతి", "മകരസംക്രാന്തി", "मकर संक्रांति", "மகர சங்கராந்தி", "ಮಕರ ಸಂಕ್ರಾಂತಿ",
                "ମକର ସଂକ୍ରାନ୍ତି", "Makar Sankranti", "মকর সংক্রান্তি", "मकर संक्रांत", "મકર સંક્રાંતિ");
        put("మహా శివరాత్రి", "മഹാശിവരാത്രി", "महाशिवरात्रि", "மகா சிவராத்திரி", "ಮಹಾ ಶಿವರಾತ್ರಿ",
                "ମହା ଶିବରାତ୍ରି", "Maha Shivaratri", "মহাশিবরাত্রি", "महाशिवरात्र", "મહા શિવરાત્રિ");
        put("మిలాద్ ఉన్-నబి", "മിലാദ് ഉൻ നബി", "मिलाद-उन-नबी", "மிலாதுன் நபி", "ಮಿಲಾದ್-ಉನ್-ನಬಿ",
                "ମିଲାଦ-ଉନ୍-ନବୀ", "Milad-un-Nabi", "ঈদে মিলাদুন্নবী", "ईद-ए-मिलाद", "મિલાદ-ઉન-નબી");
        put("రక్షా బంధన్", "രക്ഷാബന്ധൻ", "रक्षाबंधन", "ராக்கி", "ರಕ್ಷಾ ಬಂಧನ", "ରାଖୀ",
                "Raksha Bandhan", "রাখীবন্ধন", "रक्षाबंधन", "રક્ષાબંધન");
        put("వరలక్ష్మీ వ్రతం", "വരലക്ഷ്മി വ്രതം", "वरलक्ष्मी व्रत", "வரலட்சுமி விரதம்", "ವರಮಹಾಲಕ್ಷ್ಮಿ ವ್ರತ",
                "ବରଲକ୍ଷ୍ମୀ ବ୍ରତ", "Varalakshmi Vratam", "বরলক্ষ্মী ব্রত", "वरलक्ष्मी व्रत", "વરલક્ષ્મી વ્રત");
        put("వినాయక నిమజ్జనం", "ഗണേശ വിസർജൻ", "गणेश विसर्जन", "விநாயகர் நிமஜ்ஜனம்", "ಗಣೇಶ ವಿಸರ್ಜನೆ",
                "ଗଣେଶ ବିସର୍ଜନ", "Ganesh Visarjan", "গণেশ বিসর্জন", "गणेश विसर्जन", "ગણેશ વિસર્જન");
        put("శ్రీ రామ నవమి", "ശ്രീരാമ നവമി", "श्री राम नवमी", "ஸ்ரீ ராம நவமி", "ಶ್ರೀ ರಾಮ ನವಮಿ",
                "ଶ୍ରୀରାମ ନବମୀ", "Sri Rama Navami", "শ্রীরাম নবমী", "श्रीराम नवमी", "રામ નવમી");
        put("శ్రీకృష్ణ జన్మాష్టమి", "ശ്രീകൃഷ്ണ ജന്മാഷ്ടമി", "श्रीकृष्ण जन्माष्टमी", "கிருஷ்ண ஜயந்தி",
                "ಕೃಷ್ಣ ಜನ್ಮಾಷ್ಟಮಿ", "ଶ୍ରୀକୃଷ୍ଣ ଜନ୍ମାଷ୍ଟମୀ", "Krishna Janmashtami", "জন্মাষ্টমী",
                "जन्माष्टमी", "જન્માષ્ટમી");
        put("స్వాతంత్ర్య దినోత్సవం", "സ്വാതന്ത്ര്യദിനം", "स्वतंत्रता दिवस", "சுதந்திர தினம்", "ಸ್ವಾತಂತ್ರ್ಯ ದಿನ",
                "ସ୍ୱାଧୀନତା ଦିବସ", "Independence Day", "স্বাধীনতা দিবস", "स्वातंत्र्य दिन", "સ્વતંત્રતા દિન");
        put("హోలీ", "ഹോളി", "होली", "ஹோலி", "ಹೋಳಿ", "ହୋଲି",
                "Holi", "হোলি", "होळी", "હોળી");
        put("అమావాస్య", "അമാവാസി", "अमावस्या", "அமாவாசை", "ಅಮಾವಾಸ್ಯೆ", "ଅମାବାସ୍ୟା",
                "Amavasya", "অমাবস্যা", "अमावस्या", "અમાસ");
        put("పూర్ణిమ", "പൗർണമി", "पूर्णिमा", "பௌர்ணமி", "ಹುಣ್ಣಿಮೆ", "ପୂର୍ଣ୍ଣିମା",
                "Purnima", "পূর্ণিমা", "पौर्णिमा", "પૂનમ");
    }

    private static void put(String te, String ml, String hi, String ta, String kn, String or,
                            String en, String bn, String mr, String gu) {
        NAMES.put(te, new String[]{ml, hi, ta, kn, or, en, bn, mr, gu});
    }

    static String name(int langId, @Nullable String te) {
        if (te == null) return "";
        String key = te.trim();
        String[] row = NAMES.get(key);
        if (row == null) return te;
        return row[slot(langId)];
    }

    @Nullable
    static String nakshatraShort(int langId, @Nullable String teShort) {
        if (teShort == null) return null;
        String key = teShort.trim();
        int idx = exact(NAK_SHORT_TE, key);
        if (idx < 0) return null;
        return nakShortTable(langId)[idx];
    }

    static String header(int langId, @Nullable String te) {
        if (te == null) return "";
        Matcher m = HEADER.matcher(te.trim());
        if (!m.matches()) return te;
        String sam = pick(langId, SAM_TE, samTable(langId), m.group(1));
        String masa = pick(langId, MASA_TE, masaTable(langId), m.group(2));
        switch (langId) {
            case 1: return "ശ്രീ " + sam + " നാമ സംവത്സരം, " + masa + " മാസം";
            case 2: return "श्री " + sam + " नाम संवत्सर, " + masa + " मास";
            case 4: return "ஸ்ரீ " + sam + " நாம சம்வத்சரம், " + masa + " மாதம்";
            case 5: return "ಶ್ರೀ " + sam + " ನಾಮ ಸಂವತ್ಸರ, " + masa + " ಮಾಸ";
            case 6: return "ଶ୍ରୀ " + sam + " ନାମ ସଂବତ୍ସର, " + masa + " ମାସ";
            case 8: return "শ্রী " + sam + " নাম সংবৎসর, " + masa + " মাস";
            case 9: return "श्री " + sam + " नाम संवत्सर, " + masa + " मास";
            case 10: return "શ્રી " + sam + " નામ સંવત્સર, " + masa + " માસ";
            default: return "Sri " + sam + " Samvatsara, " + masa + " masa";
        }
    }

    static String timePhrase(int langId, @Nullable String te) {
        if (te == null) return "";
        if (te.isEmpty()) return te;
        String s = te.replace(" మరలా ", againWord(langId));
        s = s.replace(" నుండి ", fromWord(langId));
        s = s.replace("వరకు", toWord(langId));
        Matcher m = CLOCK.matcher(s);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, Matcher.quoteReplacement(
                    formatClock(langId, m.group(1), m.group(2))));
        }
        m.appendTail(sb);
        return sb.toString().replaceAll(" {2,}", " ").trim();
    }

    private static String formatClock(int langId, String period, String num) {
        if (isEnglish(langId)) {
            return num + " " + amPm(period, num);
        }
        return dayPart(langId, period) + " " + num;
    }

    private static boolean isEnglish(int langId) {
        return langId != 1 && langId != 2 && langId != 4 && langId != 5
                && langId != 6 && langId != 8 && langId != 9 && langId != 10;
    }

    /** రా. before 5am and at midnight is AM; 8pm–11pm is PM. */
    private static String amPm(String period, String num) {
        if ("ఉ.".equals(period)) return "AM";
        if ("మ.".equals(period) || "సా.".equals(period)) return "PM";
        int hour = 0;
        int dot = num.indexOf('.');
        try {
            hour = Integer.parseInt(dot > 0 ? num.substring(0, dot) : num);
        } catch (NumberFormatException ignored) {
            hour = 0;
        }
        if (hour >= 8 && hour <= 11) return "PM";
        return "AM";
    }

    private static String dayPart(int langId, String period) {
        boolean morning = "ఉ.".equals(period);
        boolean noon = "మ.".equals(period);
        boolean evening = "సా.".equals(period);
        switch (langId) {
            case 1:
                if (morning) return "രാവിലെ";
                if (noon) return "ഉച്ച";
                if (evening) return "വൈകിട്ട്";
                return "രാത്രി";
            case 2:
                if (morning) return "सु.";
                if (noon) return "दो.";
                if (evening) return "सा.";
                return "रा.";
            case 4:
                if (morning) return "கா.";
                if (noon) return "ம.";
                if (evening) return "மா.";
                return "இ.";
            case 5:
                if (morning) return "ಬೆ.";
                if (noon) return "ಮ.";
                if (evening) return "ಸಂ.";
                return "ರಾ.";
            case 6:
                if (morning) return "ସ.";
                if (noon) return "ଅ.";
                if (evening) return "ସଂ.";
                return "ରା.";
            case 8:
                if (morning) return "স.";
                if (noon) return "দু.";
                if (evening) return "সা.";
                return "রা.";
            case 9:
                if (morning) return "स.";
                if (noon) return "दु.";
                if (evening) return "सं.";
                return "रा.";
            case 10:
                if (morning) return "સ.";
                if (noon) return "બ.";
                if (evening) return "સાં.";
                return "રા.";
            default:
                return "";
        }
    }

    private static String fromWord(int langId) {
        switch (langId) {
            case 1: return " മുതൽ ";
            case 2: return " से ";
            case 4: return " முதல் ";
            case 5: return " ರಿಂದ ";
            case 6: return " ଠାରୁ ";
            case 8: return " থেকে ";
            case 9: return " पासून ";
            case 10: return " થી ";
            default: return " to ";
        }
    }

    private static String toWord(int langId) {
        switch (langId) {
            case 1: return " വരെ";
            case 2: return " तक";
            case 4: return " வரை";
            case 5: return " ವರೆಗೆ";
            case 6: return " ପର୍ଯ୍ୟନ୍ତ";
            case 8: return " পর্যন্ত";
            case 9: return " पर्यंत";
            case 10: return " સુધી";
            default: return "";
        }
    }

    private static String againWord(int langId) {
        switch (langId) {
            case 1: return " വീണ്ടും ";
            case 2: return " फिर ";
            case 4: return " மீண்டும் ";
            case 5: return " ಮತ್ತೆ ";
            case 6: return " ପୁଣି ";
            case 8: return " আবার ";
            case 9: return " पुन्हा ";
            case 10: return " ફરી ";
            default: return " again ";
        }
    }

    private static int slot(int langId) {
        switch (langId) {
            case 1: return 0;
            case 2: return 1;
            case 4: return 2;
            case 5: return 3;
            case 6: return 4;
            case 8: return 6;
            case 9: return 7;
            case 10: return 8;
            default: return 5;
        }
    }

    private static String[] nakShortTable(int langId) {
        switch (langId) {
            case 1: return NAK_SHORT_ML;
            case 2: return NAK_SHORT_HI;
            case 4: return NAK_SHORT_TA;
            case 5: return NAK_SHORT_KN;
            case 6: return NAK_SHORT_OR;
            case 8: return NAK_SHORT_BN;
            case 9: return NAK_SHORT_MR;
            case 10: return NAK_SHORT_GU;
            default: return NAK_SHORT_EN;
        }
    }

    private static String[] samTable(int langId) {
        switch (langId) {
            case 1: return SAM_ML;
            case 2: return SAM_HI;
            case 4: return SAM_TA;
            case 5: return SAM_KN;
            case 6: return SAM_OR;
            case 8: return SAM_BN;
            case 9: return SAM_MR;
            case 10: return SAM_GU;
            default: return SAM_EN;
        }
    }

    private static String[] masaTable(int langId) {
        switch (langId) {
            case 1: return MASA_ML;
            case 2: return MASA_HI;
            case 4: return MASA_TA;
            case 5: return MASA_KN;
            case 6: return MASA_OR;
            case 8: return MASA_BN;
            case 9: return MASA_MR;
            case 10: return MASA_GU;
            default: return MASA_EN;
        }
    }

    private static String pick(int langId, String[] te, String[] loc, String raw) {
        int idx = exact(te, raw == null ? "" : raw.trim());
        if (idx < 0 || loc == null || idx >= loc.length) return raw == null ? "" : raw.trim();
        return loc[idx];
    }

    private static int exact(String[] arr, String name) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].equals(name)) return i;
        }
        return -1;
    }
}
