package com.anyNews.anynews.panchang;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.SharedPrefsData;

import java.util.Calendar;
import java.util.Locale;

/**
 * Daily raasi (zodiac) text — deterministic curated/templated content per app language.
 * Language IDs: 1 ML, 2 HI, 3 TE, 4 TA, 5 KN, 6 OR, else EN.
 * Same (rashiId, yyyy-MM-dd, lang) always yields the same phalalu / luck labels.
 */
public final class RaasiContent {

    public static final int COUNT = 12;

    /** Telugu display names (index 0 = Mesha). */
    public static final String[] NAMES_TE = {
            "మేషం", "వృషభం", "మిథునం", "కర్కాటకం", "సింహం", "కన్య",
            "తుల", "వృశ్చికం", "ధనుస్సు", "మకరం", "కుంభం", "మీనం"
    };

    /** English zodiac names (index 0 = Aries / Mesha). */
    public static final String[] NAMES_EN = {
            "Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo",
            "Libra", "Scorpio", "Sagittarius", "Capricorn", "Aquarius", "Pisces"
    };

    /** Stable keys for admin default_rashi_id enum / splash. */
    public static final String[] KEYS = {
            "mesha", "vrishabha", "mithuna", "karkataka", "simha", "kanya",
            "tula", "vrischika", "dhanu", "makara", "kumbha", "meena"
    };

    /** Drawable resource name stems (ic_raasi_*). Index matches rashi id. */
    public static final String[] ICON_KEYS = KEYS;

    /** Short glyph / symbol for tile fallback. */
    public static final String[] GLYPHS = {
            "♈", "♉", "♊", "♋", "♌", "♍", "♎", "♏", "♐", "♑", "♒", "♓"
    };

    // Colors & directions per language — no Telugu fallback for HI/EN/etc.
    private static final String[] COLORS_EN = {
            "Red", "White", "Yellow", "Blue", "Orange", "Green",
            "Pink", "Purple", "Black", "Gold", "Silver", "Violet"
    };
    private static final String[] COLORS_HI = {
            "लाल", "सफेद", "पीला", "नीला", "नारंगी", "हरा",
            "गुलाबी", "बैंगनी", "काला", "सुनहरा", "चांदी", "जामुनी"
    };
    private static final String[] COLORS_TE = {
            "ఎరుపు", "తెలుపు", "పసుపు", "నీలం", "నారింజ", "ఆకుపచ్చ",
            "గులాబీ", "ఊదా", "నలుపు", "బంగారు", "వెండి", "వైలెట్"
    };
    private static final String[] COLORS_TA = {
            "சிவப்பு", "வெள்ளை", "மஞ்சள்", "நீலம்", "ஆரஞ்சு", "பச்சை",
            "இளஞ்சிவப்பு", "ஊதா", "கருப்பு", "தங்கம்", "வெள்ளி", "ஊதா நிறம்"
    };
    private static final String[] COLORS_KN = {
            "ಕೆಂಪು", "ಬಿಳಿ", "ಹಳದಿ", "ನೀಲಿ", "ಕಿತ್ತಳೆ", "ಹಸಿರು",
            "ಗುಲಾಬಿ", "ನೇರಳೆ", "ಕಪ್ಪು", "ಚಿನ್ನ", "ಬೆಳ್ಳಿ", "ಊದಾ"
    };
    private static final String[] COLORS_ML = {
            "ചുവപ്പ്", "വെളുപ്പ്", "മഞ്ഞ", "നീല", "ഓറഞ്ച്", "പച്ച",
            "പിങ്ക്", "പർപ്പിൾ", "കറുപ്പ്", "സ്വർണം", "വെള്ളി", "വയലറ്റ്"
    };
    private static final String[] COLORS_OR = {
            "ଲାଲ", "ଧଳା", "ହଳଦିଆ", "ନୀଳ", "କମଳା", "ସବୁଜ",
            "ଗୋଲାପୀ", "ବାଇଗଣୀ", "କଳା", "ସୁନା", "ରୂପା", "ବାଇଗଣି"
    };

    private static final String[] DIRS_EN = {
            "East", "West", "North", "South",
            "Northeast", "Northwest", "Southeast", "Southwest"
    };
    private static final String[] DIRS_HI = {
            "पूर्व", "पश्चिम", "उत्तर", "दक्षिण",
            "उत्तर-पूर्व", "उत्तर-पश्चिम", "दक्षिण-पूर्व", "दक्षिण-पश्चिम"
    };
    private static final String[] DIRS_TE = {
            "తూర్పు", "పశ్చిమం", "ఉత్తరం", "దక్షిణం",
            "ఈశాన్యం", "వాయువ్యం", "ఆగ్నేయం", "నైరుతి"
    };
    private static final String[] DIRS_TA = {
            "கிழக்கு", "மேற்கு", "வடக்கு", "தெற்கு",
            "வடகிழக்கு", "வடமேற்கு", "தென்கிழக்கு", "தென்மேற்கு"
    };
    private static final String[] DIRS_KN = {
            "ಪೂರ್ವ", "ಪಶ್ಚಿಮ", "ಉತ್ತರ", "ದಕ್ಷಿಣ",
            "ಈಶಾನ್ಯ", "ವಾಯುವ್ಯ", "ಆಗ್ನೇಯ", "ನೈಋತ್ಯ"
    };
    private static final String[] DIRS_ML = {
            "കിഴക്ക്", "പടിഞ്ഞാറ്", "വടക്ക്", "തെക്ക്",
            "വടക്കുകിഴക്ക്", "വടക്കുപടിഞ്ഞാറ്", "തെക്കുകിഴക്ക്", "തെക്കുപടിഞ്ഞാറ്"
    };
    private static final String[] DIRS_OR = {
            "ପୂର୍ବ", "ପଶ୍ଚିମ", "ଉତ୍ତର", "ଦକ୍ଷିଣ",
            "ଉତ୍ତର-ପୂର୍ବ", "ଉତ୍ତର-ପଶ୍ଚିମ", "ଦକ୍ଷିଣ-ପୂର୍ବ", "ଦକ୍ଷିଣ-ପଶ୍ଚିମ"
    };

    // Long multi-sentence bodies (A+B+C) so list truncates and detail modal shows full text.
    private static final String[] TEMPLATES_A_EN = {
            "Today favors family warmth and honest conversations at home. Plan a calm discussion if something needs clearing, and avoid raising your voice over small issues. Shared meals or a short call to elders can strengthen bonds more than expensive gifts.",
            "Work and business move better with patience than with rushed decisions. Finish one high-priority task fully before starting the next, and double-check emails or invoices before sending. A colleague's quiet support may matter more today than loud praise.",
            "Health needs steady attention: hydrate early, walk if you sit long hours, and do not ignore mild discomfort. Light meals and an earlier bedtime will restore energy faster than late-night screens. Mental rest is as important as physical rest now.",
            "Money can improve with careful choices—review recurring costs and skip impulse buys. A long-pending bill or insurance matter deserves a calm look today. If investing or lending, wait until paperwork is crystal clear.",
            "Students and exam aspirants gain from focused blocks with short breaks, not all-night cramming. Revise weak topics first, then practice under a timer. A clear desk and muted notifications raise concentration more than extra coffee.",
            "Travel or new meetings can open doors if you stay polite and observant. Keep documents and chargers ready, leave a buffer for delays, and listen more than you speak in first conversations. One introduction may become useful later this week.",
            "Emotional calm protects household peace. When stress rises, pause before replying to messages and choose kind wording. Helping a family member without expectation brings quieter joy than winning a debate.",
            "Generosity shows good results: help someone who asks without overextending yourself. A small favor returned later may ease your own load. Stay ready for unexpected support from an unlikely place."
    };
    private static final String[] TEMPLATES_B_EN = {
            "In love and friendship, small sincere gestures rebuild trust better than long explanations. Share appreciation out loud and schedule time without phones. Avoid comparing relationships with others online today.",
            "Morning hours are your best window for tough calls, applications, or creative work. Block distractions until midday, then handle meetings. Evening is better for light social plans than heavy decisions.",
            "New projects and contracts need a second pair of eyes. Read terms thrice, ask one practical question, and do not sign under pressure. What feels slightly unclear now can cost more later.",
            "Balance chores and rest at home so nobody feels overburdened. Delegate if possible, and accept imperfect rooms over exhausted moods. A short nap or walk resets the atmosphere for the household.",
            "Quiet prayer, mantra, or five mindful breaths settle restless thoughts. Spiritual rhythm need not be long—consistency beats intensity. Journaling one gratitude line anchors the day.",
            "Life goals look sharper when written as next-step actions, not vague hopes. A trusted friend may give useful feedback if you ask specifically. Discard one outdated plan that no longer fits you.",
            "Unexpected expenses may appear—keep a small buffer and avoid reactive spending. Maintain sleep, water, and medicine routines even when the day is busy. Stability in habits steadies mood and money.",
            "Evening suits friendly gatherings if you keep conversations light and positive. Limit gossip and late caffeine. End the night with a brief review of what went well so tomorrow starts lighter."
    };
    private static final String[] TEMPLATES_C_EN = {
            "Lucky focus: act with fairness, keep commitments, and leave room for rest. Your words carry weight—choose accuracy over drama. Progress today is quiet but lasting if you stay disciplined.",
            "Overall outlook: mixed energy rewards careful timing more than speed. Protect privacy on digital apps and avoid oversharing plans. One completed promise raises confidence for everyone around you.",
            "Remember that patience with seniors or juniors prevents needless friction. Say thank you freely. Even on a pressured day, a short smile or message of support elevates the atmosphere.",
            "If obstacles appear, treat them as feedback, not failure. Adjust the plan, seek one expert view, and try again tomorrow. Consistency through the week brings the bigger result you want."
    };

    private static final String[] TEMPLATES_A_HI = {
            "आज परिवार में गर्मजोशी और सच्ची बातचीत फलदायी रहेगी। यदि कोई बात सुलझानी हो तो शांत स्वर में चर्चा करें और छोटी बातों पर आवाज़ ऊँची न करें। बुज़ुर्गों को फोन या साथ भोजन संबंधों को खर्च से अधिक मज़बूत बना सकता है।",
            "काम/व्यवसाय में जल्दबाज़ी से बेहतर धैर्य काम करेगा। एक महत्वपूर्ण काम पूरा कर फिर अगला शुरू करें; मेल या बिल भेजने से पहले दोबारा जाँचें। आज शोर भरी तारीफ़ से चुप समर्थन ज़्यादा काम आएगा।",
            "स्वास्थ्य पर नियमित ध्यान दें: सुबह पानी, बैठक में बीच-बीच में चलना, और हल्के असुविधा को नज़रअंदाज़ न करें। हलका भोजन व जल्दी सोना देर रात स्क्रीन से बेहतर ऊर्जा लौटाते हैं। मानसिक विश्राम भी ज़रूरी है।",
            "पैसे में सावधानी फायदेमंद: बार-बार के खर्च देखें, आवेग खरीदी टालें। लंबित बिल/इंश्योरेंस आज शांति से देखें। निवेश या उधार तभी जब कागज़ साफ हों।",
            "विद्यार्थियों के लिए छोटे ब्रेक के साथ फोकस्ड पठन बेहतर; रातभर रटना नहीं। कमज़ोर विषय पहले, फिर टाइमर से अभ्यास। साफ मेज़ और नोटिफिकेशन बंद एकाग्रता बढ़ाते हैं।",
            "यात्रा या नए परिचय सम्मान और ध्यान से फायदामंद रह सकते हैं। दस्तावेज़/चार्जर तैयार रखें, देर का बफ़र रखें, पहली बात में अधिक सुनें। एक परिचय सप्ताह बाद काम आ सकता है।",
            "भावनात्मक शांति घर का सुख बचाती है। तनाव में संदेश भेजने से पहले रुकें, नरम शब्द चुनें। बिना अपेक्षा मदद विवाद जीतने से बेहतर खुशी देती है।",
            "उदारता के अच्छे फल: माँगने वाले की मदद करें पर खुद को थकाएँ नहीं। छोटा उपकार बाद में वापस आ सकता है। अप्रत्याशित जगह से सहायता के लिए खुले रहें।"
    };
    private static final String[] TEMPLATES_B_HI = {
            "प्रेम/मित्रता में छोटे सच्चे इशारे लंबी सफ़ाई से बेहतर भरोसा लाते हैं। खुली तारीफ़ करें, फोन-मुक्त समय रखें। आज ऑनलाइन तुलना से बचें।",
            "सुबह मुश्किल कॉल, आवेदन या रचनात्मक काम के लिए श्रेष्ठ है। दोपहर तक बाधाएँ रोकें; शाम हल्की मुलाकात के लिए। भारी निर्णय शाम पर न लें।",
            "नए प्रोजेक्ट/अनुबंध को दूसरी नज़र चाहिए। शर्तें तीन बार पढ़ें, एक व्यावहारिक प्रश्न पूछें, दबाव में साइन न करें। अस्पष्ट बिंदु बाद में महँगा पड़ सकता है।",
            "घर के काम और आराम में संतुलन रखें ताकि कोई अतिभारित न हो। संभव हो तो बाँटें; थकान से बदतर कमरे नहीं। छोटा आराम घर का माहौल सुधारता है।",
            "शांत प्रार्थना, मंत्र या पाँच साँस बेचैन विचारों को ठहराते हैं। लंबा अनुष्ठान ज़रूरी नहीं—नियम से तीव्रता बेहतर। एक कृतज्ञता पंक्ति दिन जकड़ती है।",
            "जीवन लक्ष्य अगले कदम जैसे लिखने पर साफ दिखते हैं। भरोसेमंद मित्र से विशेष सलाह लें। जो योजना अब फिट नहीं उसे छोड़ें।",
            "अनचाहे खर्च आ सकते हैं—छोटा बफ़र रखें, प्रतिक्रिया खर्च से बचें। व्यस्त दिन भी नींद-पानी-दवाई जारी रखें। आदतें मूड और पैसे दोनों स्थिर करती हैं।",
            "शाम हल्की और सकारात्मक मुलाकातों के लिए अच्छी है। गपशप और देर कैफीन कम करें। रात को क्या अच्छा रहा इसकी संक्षिप्त समीक्षा कल हल्का शुरू करेगी।"
    };
    private static final String[] TEMPLATES_C_HI = {
            "शुभ ध्यान: निष्पक्ष रहें, वादे पूरे करें, विश्राम बचाएँ। शब्द वजनदार हैं—नाटक से सटीकता चुनें। आज की चुप प्रगति अनुशासन से टिकाऊ है।",
            "कुल दृष्टि: मिश्रित ऊर्जा में समय का ध्यान स्पीड से बेहतर। डिजिटल गोपनीयता रखें, योजनाएँ ज़्यादा न बताएँ। एक पूरा वादा आसपास आत्मविश्वास बढ़ाता है।",
            "बड़े-छोटे दोनों के प्रति धैर्य अनावश्यक खिंचाव रोकता है। धन्यवाद खुले दिल से कहें। दबाव वाले दिन भी मुस्कान/सहायक संदेश माहौल सुधारता है।",
            "रुकावट आए तो असफलता नहीं, फीडबैक समझें। योजना बदलें, एक जानकार से पूछें, कल फिर कोशिश करें। हफ़्ते भर की निरंतरता बड़ा फल लाती है।"
    };

    private static final String[] TEMPLATES_A_TE = {
            "ఈరోజు కుటుంబంలో వెచ్చదనం, నిజాయితీ సంభాషణలు మంచి ఫలితం ఇస్తాయి. పరిష్కరించాల్సిన విషయం ఉంటే ప్రశాంతంగా మాట్లాడండి; చిన్న విషయాలపై గట్టిగా అరవకండి. పెద్దలతో ఫోన్ లేదా కలిసి భోజనం ఖరీదైన బహుమతుల కంటే సంబంధాలను బలపరుస్తాయి.",
            "పని/వ్యాపారంలో తొందర కంటే ఓపిక మెరుగు తెస్తుంది. ఒక ముఖ్య పని పూర్తి చేసి తర్వాత తదుపరి పని మొదలుపెట్టండి; మెయిల్/బిల్లు పంపే ముందు మళ్లీ చూడండి. ఈరోజు శబ్దపు ప్రశంస కంటే నిశ్శబ్ద మద్దతు ఎక్కువ పనికి వస్తుంది.",
            "ఆరోగ్యం పై నిరంతర శ్రద్ధ అవసరం: ఉదయం నీరు, ఎక్కువగా కూర్చుంటే మధ్యలో నడక, తేలికపాటి అసౌకర్యాన్ని నిర్లక్ష్యం చేయకండి. తేలిక ఆహారం, ముందుగా నిద్ర ఆలస్య స్క్రీన్ కంటే శక్తి తిరిగి ఇస్తాయి. మానసిక విశ్రాంతి కూడా అంతే ముఖ్యం.",
            "డబ్బు విషయంలో జాగ్రత్త మెరుగు: పునరావృత ఖర్చులు చూడండి, ఆవేశపూరిత కొనుగోలు ఆపండి. పెండింగ్ బిల్లు/ఇన్సూరెన్స్ నేడు ప్రశాంతంగా చూడండి. పెట్టుబడి/అప్పు కాగితాలు స్పష్టం అయ్యాకే.",
            "విద్యార్థులకు చిన్న విరామాలతో ఫోకస్ చదువు మేలు; రాత్రంతా కష్టపడటం కాదు. బలహీన అంశాలు ముందు, తర్వాత టైమర్ సాధన. శుభ్రమైన బల్ల, నోటిఫికేషన్లు ఆఫ్ దృష్టి పెంచుతాయి.",
            "ప్రయాణం/కొత్త పరిచయాలు మర్యాద, గమనంతో లాభం ఇవ్వవచ్చు. డాక్యుమెంట్లు/ఛార్జర్ సిద్ధంగా, ఆలస్యానికి సమయం వదిలి, మొదటి సంభాషణలో ఎక్కువ వినండి. ఒక పరిచయం ఈ వారం తర్వాత పనికి రావచ్చు.",
            "భావోద్వేగ ప్రశాంతత ఇంటి సుఖాన్ని కాపాడుతుంది. ఒత్తిడిలో సందేశం పంపే ముందు ఆగండి, మృదువైన మాటలు ఎంచుకోండి. అపేక్ష లేకుండా సహాయం వాదం గెలవడం కంటే నిశ్శబ్ద సంతోషం ఇస్తుంది.",
            "దాతృత్వం మంచి ఫలం ఇస్తుంది: అడిగిన వారికి సహాయం చేయండి కాని మిమ్మల్ని అతిగా ఒత్తిడి పెట్టుకోకండి. చిన్న ఉపకారం తర్వాత తిరిగి రావచ్చు. అనూహ్య చోటు నుంచి సహాయానికి సిద్ధంగా ఉండండి."
    };
    private static final String[] TEMPLATES_B_TE = {
            "ప్రేమ/స్నేహంలో చిన్న నిజమైన సైన్లు పొడుగు వివరణల కంటే నమ్మకం పునర్నిర్మిస్తాయి. ప్రశంస బిగ్గరగా చెప్పండి, ఫోన్ లేని సమయం పెట్టుకోండి. ఈరోజు ఆన్‌లైన్ పోలికలు మానండి.",
            "ఉదయం కష్టమైన కాల్స్, అప్లికేషన్లు, సృజనాత్మక పనికి మంచి సమయం. మధ్యాహ్నం వరకు అంతరాయాలు తగ్గించండి; సాయంత్రం తేలిక సామాజిక ప్రణాళికకు. భారీ నిర్ణయాలు సాయంత్రం మానండి.",
            "కొత్త ప్రాజెక్టులు/ఒప్పందాలకు రెండో చూపు అవసరం. షరతులు మూడుసార్లు చదవండి, ఒక ఆచరణ ప్రశ్న అడగండి, ఒత్తిడిలో సైన్ చేయకండి. అస్పష్టం ఇప్పుడు అయితే తర్వాత ఖరీదవుతుంది.",
            "ఇంటి పనులు, విశ్రాంతిలో సమతుల్యత ఉంచండి; ఎవరూ అధిక భారం పడకూడదు. సాధ్యమైతే పంచుకోండి; అలసట కంటే అసంపూర్ణ గదులు మేలు. చిన్న విరామం ఇంటి వాతావరణం మారుస్తుంది.",
            "నిశ్శబ్ద ప్రార్థన, మంత్రం లేదా ఐదు శ్వాసలు అలజడి ఆలోచనలను నిలిపివేస్తాయి. దీర్ఘమైన ఆచారం అవసరం లేదు—నిబద్ధత తీవ్రత కంటే ముఖ్యం. ఒక కృతజ్ఞత పంక్తి రోజును స్థిరపరుస్తుంది.",
            "జీవన లక్ష్యాలు తదుపరి చర్యలుగా రాస్తే స్పష్టమవుతాయి. నమ్మకమైన స్నేహితుడి నుంచి నిర్దిష్ట సలహా తీసుకోండి. ఇక సరిపోని పాత ప్రణాళికను వదలండి.",
            "అనుకోని ఖర్చు రావచ్చు—చిన్న బఫర్ ఉంచండి, తక్షణ ఖర్చు ఆపండి. బిజీ రోజుల్లో కూడా నిద్ర, నీరు, మందులు కొనసాగించండి. అలవాట్లు మూడ్, డబ్బు రెండింటినీ స్థిరం చేస్తాయి.",
            "సాయంత్రం తేలిక, సానుకూల కలయికలకు అనుకూలం. గాసిప్, ఆలస్య కెఫీన్ తగ్గించండి. రాత్రి ఏమి బాగా జరిగిందో చిన్న సమీక్ష రేపటిని తేలికగా మొదలుపెట్టిస్తుంది."
    };
    private static final String[] TEMPLATES_C_TE = {
            "శుభ దృష్టి: న్యాయంగా ఉండండి, మాట నిలబెట్టుకోండి, విశ్రాంతికి చోటు ఇవ్వండి. మీ మాటలు భారం కలిగి ఉంటాయి—నాటకం కంటే ఖచ్చితత్వం. ఈరోజు నిశ్శబ్ద పురోగతి క్రమశిక్షణతో నిలుస్తుంది.",
            "మొత్తం ఫలం: మిశ్రమ శక్తిలో సమయం స్పీడ్ కంటే ముఖ్యం. డిజిటల్ గోప్యత కాపాడండి, ప్రణాళికలు అతిగా చెప్పకండి. ఒక నిలబడ్డ వాగ్దానం చుట్టూ ఉన్నవారికి ఆత్మవిశ్వాసం పెంచుతుంది.",
            "పెద్దలు/చిన్నల పట్ల ఓపిక అనవసర ఘర్షణను నివారిస్తుంది. ధన్యవాదాలు స్వేచ్ఛగా చెప్పండి. ఒత్తిడి రోజులో కూడా చిరునవ్వు లేదా సహాయ సందేశం వాతావరణం మెరుగుపరుస్తుంది.",
            "అడ్డంకులు వస్తే వైఫల్యం కాదు, అభిప్రాయంగా తీసుకోండి. ప్రణాళిక సవరించండి, ఒక నిపుణుడిని అడగండి, రేపు మళ్లీ ప్రయత్నించండి. వారం పాటు నిలకడ పెద్ద ఫలం ఇస్తుంది."
    };

    private static final String[] TEMPLATES_A_TA = {
            "இன்று குடும்பத்தில் அரவணைப்பும் நேர்மையான பேச்சும் நல்ல பலன் தரும். தீர்க்க வேண்டிய விஷயம் இருந்தால் அமைதியாக பேசுங்கள்; சிறு விஷயங்களில் குரல் உயர்த்த வேண்டாம். பெரியவர்களுடன் அழைப்பு அல்லது சேர்ந்து உணவு விலை உயர்ந்த பரிசுகளை விட உறவை வலுப்படுத்தும்.",
            "வேலை/வியாபாரத்தில் அவசரத்தை விட பொறுமை மேன்மை தரும். ஒரு முக்கிய பணியை முடித்து பின்னர் அடுத்ததை தொடங்குங்கள்; மின்னஞ்சல்/ரசீது அனுப்பும் முன் மீண்டும் சரிபாருங்கள். இன்று சத்தமான பாராட்டை விட அமைதியான ஆதரவு அதிகம் பயன்படும்.",
            "ஆரோக்கியத்திற்கு தொடர் கவனம்: காலையில் நீர், நீண்ட நேரம் உட்கார்ந்தால் நடை, சிறு அசௌகரியத்தைப் புறக்கணியாதீர்கள். இலகு உணவும் முன்னதாக தூக்கமும் இரவுத் திரையை விட சக்தியை மீட்கும். மன ஓய்வும் அவசியம்.",
            "பண விஷயத்தில் கவனம்: மீண்டும் வரும் செலவுகளை பாருங்கள், உணர்ச்சி வாங்குதல் நிறுத்துங்கள். நிலுவை பில்/காப்பீடு இன்று அமைதியாக பாருங்கள். முதலீடு/கடன் ஆவணங்கள் தெளிவான பிறகே.",
            "மாணவர்களுக்கு சிறு இடைவேளையுடன் கவனக் கற்றல் நலம்; இரவு முழு துவக்கம் வேண்டாம். பலவீன பாடங்கள் முதலில், பிறகு டைமர் பயிற்சி. சுத்தமான மேசை, அறிவிப்பு ஆஃப் கவனத்தை உயர்த்தும்.",
            "பயணம்/புதிய அறிமுகம் மரியாதை, கவனிப்புடன் பயனளிக்கலாம். ஆவணங்கள்/சார்ஜர் தயார், தாமத இடைவெளி, முதல் உரையாடலில் அதிகம் கேளுங்கள். ஒரு அறிமுகம் இந்த வாரம் பிறகு பயன்படலாம்.",
            "உணர்ச்சி அமைதி வீட்டு இன்பத்தை காக்கும். அழுத்தத்தில் செய்தி அனுப்புமுன் நிறுத்துங்கள், மென்மையான சொற்கள் தேர்வு செய்யுங்கள். எதிர்பார்ப்பின்றி உதவி வாக்குவாதம் வெல்வதை விட அமைதி மகிழ்ச்சி தரும்.",
            "தாராள மனம் நல்ல பலன்: கேட்பவர்க்கு உதவுங்கள் ஆனால் உங்களை அதிகம் சோர்வடையாதீர்கள். சிறு உதவி பிறகு திரும்பலாம். எதிர்பாராத இடத்திலிருந்து உதவிக்கு தயாராக இருங்கள்."
    };
    private static final String[] TEMPLATES_B_TA = {
            "காதல்/நட்பில் சிறிய உண்மையான அடையாளங்கள் நீண்ட விளக்கங்களை விட நம்பிக்கை கட்டும். பாராட்டை வெளிப்படுத்தவும், போன் இல்லா நேரம் ஒதுக்கவும். இன்று ஆன்லைன் ஒப்பீடு தவிர்க்கவும்.",
            "காலை கடின அழைப்புகள், விண்ணப்பங்கள், படைப்பு பணிக்கு உகந்தது. மதியம் வரை தடைகளை குறைக்கவும்; மாலை இலகு சமுதாய திட்டத்திற்கு. கனமான தீர்மானம் மாலையில் வேண்டாம்.",
            "புதிய திட்டம்/ஒப்பந்தத்திற்கு இரண்டாம் பார்வை தேவை. நிபந்தனைகளை மூன்று முறை வாசிக்கவும், ஒரு நடைமுறை கேள்வி கேட்கவும், அழுத்தத்தில் கையொப்பமிட வேண்டாம். தெளிவின்மை இப்போது பின்னர் விலையாகும்.",
            "வீட்டு வேலை, ஓய்வில் சமநிலை காக்கவும்; யாரும் அதிக சுமை உணரக் கூடாது. முடிந்தால் பகிருங்கள்; சோர்வை விட சரியற்ற அறைகள் நலம். சிறு ஓய்வு வீட்டு சூழலை மாற்றும்.",
            "அமைதியான பிரார்த்தனை, மந்திரம் அல்லது ஐந்து மூச்சுகள் அலை யோசனைகளை நிறுத்தும். நீண்ட சடங்கு தேவையில்லை—தொடர்ச்சி தீவிரத்தை விட முக்கியம். ஒரு நன்றி வரி நாளை நிலைப்படுத்தும்.",
            "வாழ்க்கை இலக்குகள் அடுத்த நடவடிக்கையாக எழுதினால் தெளிவாகும். நம்பக நண்பரிடம் குறிப்பிட்ட ஆலோசனை கேளுங்கள். பொருந்தாத பழைய திட்டத்தை விடுங்கள்.",
            "எதிர்பாராத செலவு வரலாம்—சிறு இருப்பு வைக்கவும், எதிர்வினை செலவு தவிர்க்கவும். பிஸி நாளிலும் தூக்கம், நீர், மருந்து தொடரவும். பழக்கங்கள் மனநிலை, பணம் இரண்டையும் நிலைப்படுத்தும்.",
            "மாலை இலகு, நேர்மறை சந்திப்புகளுக்கு ஏற்றது. வதந்தி, தாமத காஃபின் குறைக்கவும். இரவு என்ன நன்றாக நடந்தது என சிறு மதிப்பாய்வு நாளையை எளிதாக்கும்."
    };
    private static final String[] TEMPLATES_C_TA = {
            "சுப கவனம்: நியாயமாக இருங்கள், வாக்குறுதி நிறைவேற்றுங்கள், ஓய்வுக்கு இடம் கொடுங்கள். உங்கள் சொற்கள் எடை உடையவை—நாடகத்தை விட துல்லியம். இன்றைய அமைதி முன்னேற்றம் ஒழுக்கத்தால் நிலைக்கும்.",
            "மொத்தப் பார்வை: கலவை ஆற்றலில் நேரம் வேகத்தை விட முக்கியம். டிஜிட்டல் தனியுரிமை காக்கவும், திட்டங்களை அதிகம் பகிர வேண்டாம். ஒரு நிறைவேறிய வாக்குறுதி சுற்றிலும் தன்னம்பிக்கை உயர்த்தும்.",
            "பெரியோர்/இளையோரிடம் பொறுமை தேவையற்ற மோதலைத் தடுக்கும். நன்றி தாராளமாக சொல்லுங்கள். அழுத்த நாளிலும் புன்னகை அல்லது உதவி செய்தி சூழலை உயர்த்தும்.",
            "தடை வந்தால் தோல்வி அல்ல, பின்னூட்டம் என எடுங்கள். திட்டம் சீரமைக்கவும், ஒரு நிபுணரை கேட்கவும், நாளை மீண்டும் முயலவும். வார நீடிப்பு பெரிய பயனைத் தரும்."
    };

    private static final String[] TEMPLATES_A_KN = {
            "ಇಂದು ಕುಟುಂಬದಲ್ಲಿ ಬೆಚ್ಚನೆ ಮತ್ತು ಪ್ರಾಮಾಣಿಕ ಸಂವಾದ ಒಳ್ಳೆಯ ಫಲ ನೀಡುತ್ತದೆ. ಪರಿಹರಿಸಬೇಕಾದ ವಿಷಯವಿದ್ದರೆ ಶಾಂತವಾಗಿ ಮಾತನಾಡಿ; ಸಣ್ಣ ವಿಷಯಗಳಲ್ಲಿ ಧ್ವನಿ ಏರಿಸಬೇಡಿ. ಹಿರಿಯರಿಗೆ ಫೋನ್ ಅಥವಾ ಜೊತೆ ಊಟ ದುಬಾರಿ ಉಡುಗೊರೆಗಿಂತ ಸಂಬಂಧ ಬಲಪಡಿಸುತ್ತದೆ.",
            "ಕೆಲಸ/ವ್ಯಾಪಾರದಲ್ಲಿ ಆತುರಕ್ಕಿಂತ ತಾಳ್ಮೆ ಉತ್ತಮ. ಒಂದು ಮುಖ್ಯ ಕೆಲಸ ಮುಗಿಸಿ ನಂತರ ಮುಂದಿನದು; ಮೇಲ್/ಬಿಲ್ ಕಳುಹಿಸುವ ಮೊದಲು ಪರಿಶೀಲಿಸಿ. ಇಂದು ಗಟ್ಟಿ ಹೊಗಳಿಕೆಗಿಂತ ನಿಶ್ಯಬ್ದ ಬೆಂಬಲ ಹೆಚ್ಚು ಕೆಲಸಕ್ಕೆ ಬರುತ್ತದೆ.",
            "ಆರೋಗ್ಯಕ್ಕೆ ನಿರಂತರ ಗಮನ: ಬೆಳಿಗ್ಗೆ ನೀರು, ಹೆಚ್ಚು ಕುಳಿತಿದ್ದರೆ ನಡಿಗೆ, ಸಣ್ಣ ಅಸೌಖ್ಯ ನಿರ್ಲಕ್ಷಿಸಬೇಡಿ. ಹಗುರ ಆಹಾರ ಮತ್ತು ಮುಂಚಿತ ನಿದ್ರೆ ತಡವಾದ ಸ್ಕ್ರೀನ್‌ಗಿಂತ ಶಕ್ತಿ ಮರಳಿಸುತ್ತವೆ. ಮಾನಸಿಕ ವಿಶ್ರಾಂತಿಯೂ ಅಷ್ಟೇ ಮುಖ್ಯ.",
            "ಹಣ ವಿಷಯದಲ್ಲಿ ಜಾಗರೂಕತೆ: ಮರುಕಳಿಸುವ ಖರ್ಚು ನೋಡಿ, ಆವೇಶ ಖರೀದಿ ನಿಲ್ಲಿಸಿ. ಬಾಕಿ ಬಿಲ್/ವಿಮೆ ಇಂದು ಶಾಂತವಾಗಿ ನೋಡಿ. ಹೂಡಿಕೆ/ಸಾಲ ಕಾಗದ ಸ್ಪಷ್ಟವಾದ ನಂತರವೇ.",
            "ವಿದ್ಯಾರ್ಥಿಗಳಿಗೆ ಸಣ್ಣ ವಿರಾಮದೊಂದಿಗೆ ಫೋಕಸ್ ಅಧ್ಯಯನ ಉತ್ತಮ; ರಾತ್ರಿಯೆಲ್ಲಾ ಓದು ಅಲ್ಲ. ದುರ್ಬಲ ವಿಷಯ ಮೊದಲು, ನಂತರ ಟೈಮರ್ ಅಭ್ಯಾಸ. ಸ್ವಚ್ಛ ಮೇಜು, ಅಧಿಸೂಚನೆ ಆಫ್ ಏಕಾಗ್ರತೆ ಹೆಚ್ಚಿಸುತ್ತವೆ.",
            "ಪ್ರಯಾಣ/ಹೊಸ ಪರಿಚಯ ಗೌರವ ಮತ್ತು ಗಮನದಿಂದ ಲಾಭಕರ. ದಾಖಲೆ/ಚಾರ್ಜರ್ ತಯಾರು, ವಿಳಂಬ ಬಫರ್, ಮೊದಲ ಸಂಭಾಷಣೆಯಲ್ಲಿ ಹೆಚ್ಚು ಕೇಳಿ. ಒಂದು ಪರಿಚಯ ಈ ವಾರ ನಂತರ ಉಪಯೋಗವಾಗಬಹುದು.",
            "ಭಾವನಾತ್ಮಕ ಶಾಂತಿ ಮನೆಯ ಸುಖ ಕಾಪಾಡುತ್ತದೆ. ಒತ್ತಡದಲ್ಲಿ ಸಂದೇಶ ಕಳುಹಿಸುವ ಮೊದಲು ನಿಲ್ಲಿ, ಮೃದು ಪದ ಆಯ್ಕೆಮಾಡಿ. ನಿರೀಕ್ಷೆಯಿಲ್ಲದೆ ಸಹಾಯ ವಾದ ಗೆಲ್ಲುವುದಕ್ಕಿಂತ ನಿಶ್ಯಬ್ದ ಸಂತೋಷ ನೀಡುತ್ತದೆ.",
            "ದಾನಶೀಲತೆ ಒಳ್ಳೆಯ ಫಲ: ಕೇಳುವವರಿಗೆ ಸಹಾಯ ಮಾಡಿ ಆದರೆ ನಿಮ್ಮನ್ನು ಅತಿಯಾಗಿ ದಣಿಸಬೇಡಿ. ಸಣ್ಣ ಉಪಕಾರ ನಂತರ ಹಿಂದಿರುಗಬಹುದು. ಅನಿರೀಕ್ಷಿತ ಸ್ಥಳದಿಂದ ಸಹಾಯಕ್ಕೆ ಸಿದ್ಧರಿರಿ."
    };
    private static final String[] TEMPLATES_B_KN = {
            "ಪ್ರೀತಿ/ಸ್ನೇಹದಲ್ಲಿ ಸಣ್ಣ ಪ್ರಾಮಾಣಿಕ ಸನ್ನೆಗಳು ಉದ್ದದ ವಿವರಣೆಗಿಂತ ನಂಬಿಕೆ ಕಟ್ಟುತ್ತವೆ. ಮೆಚ್ಚುಗೆ ಹೇಳಿ, ಫೋನ್ ಇಲ್ಲದ ಸಮಯ ಇಡಿ. ಇಂದು ಆನ್‌ಲೈನ್ ಹೋಲಿಕೆ ತಪ್ಪಿಸಿ.",
            "ಬೆಳಿಗ್ಗೆ ಕಷ್ಟದ ಕಾಲ್, ಅರ್ಜಿ ಅಥವಾ ಸೃಜನಾತ್ಮಕ ಕೆಲಸಕ್ಕೆ ಉತ್ತಮ. ಮಧ್ಯಾಹ್ನದವರೆಗೆ ಅಡಚಣೆ ಕಡಿಮೆ; ಸಂಜೆ ಹಗುರ ಸಾಮಾಜಿಕ ಯೋಜನೆ. ಭಾರಿ ನಿರ್ಣಯ ಸಂಜೆ ತಪ್ಪಿಸಿ.",
            "ಹೊಸ ಯೋಜನೆ/ಒಪ್ಪಂದಕ್ಕೆ ಎರಡನೇ ನೋಟ ಬೇಕು. ನಿಬಂಧನೆಗಳನ್ನು ಮೂರು ಬಾರಿ ಓದಿ, ಒಂದು ಪ್ರಾಯೋಗಿಕ ಪ್ರಶ್ನೆ ಕೇಳಿ, ಒತ್ತಡದಲ್ಲಿ ಸಹಿ ಮಾಡಬೇಡಿ. ಅಸ್ಪಷ್ಟತೆ ಈಗಾದರೆ ನಂತರ ದುಬಾರಿ.",
            "ಮನೆಯ ಕೆಲಸ ಮತ್ತು ವಿಶ್ರಾಂತಿಯಲ್ಲಿ ಸಮತೋಲನ; ಯಾರೂ ಅತಿ ಭಾರ ಅನುಭವಿಸಬಾರದು. ಸಾಧ್ಯವಾದರೆ ಹಂಚಿ; ಆಯಾಸಕ್ಕಿಂತ ಅಪೂರ್ಣ ಕೋಣೆ ಉತ್ತಮ. ಸಣ್ಣ ವಿರಾಮ ಮನೆ ವಾತಾವರಣ ಬದಲಾಯಿಸುತ್ತದೆ.",
            "ಶಾಂತ ಪ್ರಾರ್ಥನೆ, ಮಂತ್ರ ಅಥವಾ ಐದು ಉಸಿರು ಅಲೆದಾಡುವ ಆಲೋಚನೆ ನಿಲ್ಲಿಸುತ್ತವೆ. ದೀರ್ಘ ಆಚರಣೆ ಅಗತ್ಯವಿಲ್ಲ—ನಿರಂತರತೆ ತೀವ್ರತೆಗಿಂತ ಮುಖ್ಯ. ಒಂದು ಕೃತಜ್ಞತೆ ಸಾಲು ದಿನ ನೆಲೆಗೊಳಿಸುತ್ತದೆ.",
            "ಜೀವನ ಗುರಿಗಳು ಮುಂದಿನ ಕ್ರಮಗಳಾಗಿ ಬರೆದರೆ ಸ್ಪಷ್ಟ. ವಿಶ್ವಾಸಾರ್ಹ ಸ್ನೇಹಿತರಿಂದ ನಿರ್ದಿಷ್ಟ ಸಲಹೆ ಪಡೆಯಿರಿ. ಹೊಂದಿಕೆಯಾಗದ ಹಳೆಯ ಯೋಜನೆ ಬಿಡಿ.",
            "ಅನಿರೀಕ್ಷಿತ ಖರ್ಚು ಬರಬಹುದು—ಸಣ್ಣ ಬಫರ್ ಇರಿಸಿ, ಪ್ರತಿಕ್ರಿಯಾ ಖರ್ಚು ತಪ್ಪಿಸಿ. ಬಿಜಿ ದಿನದಲ್ಲೂ ನಿದ್ರೆ, ನೀರು, ಔಷಧಿ ಮುಂದುವರಿಸಿ. ಅಭ್ಯಾಸ ಮನೋಭಾವ ಮತ್ತು ಹಣ ಎರಡನ್ನೂ ಸ್ಥಿರಗೊಳಿಸುತ್ತವೆ.",
            "ಸಂಜೆ ಹಗುರ, ಧನಾತ್ಮಕ ಭೇಟಿಗೆ ಅನುಕೂಲ. ಹರಟೆ, ತಡವಾದ ಕೆಫೀನ್ ಕಡಿಮೆ. ರಾತ್ರಿ ಏನು ಚೆನ್ನಾಗಿತ್ತು ಎಂಬ ಸಣ್ಣ ಪರಿಶೀಲನೆ ನಾಳೆಯನ್ನು ಹಗುರವಾಗಿ ಪ್ರಾರಂಭಿಸುತ್ತದೆ."
    };
    private static final String[] TEMPLATES_C_KN = {
            "ಶುಭ ಗಮನ: ನ್ಯಾಯವಾಗಿರಿ, ಮಾತು ನಿಲ್ಲಿಸಿ, ವಿಶ್ರಾಂತಿಗೆ ಜಾಗ ಕೊಡಿ. ನಿಮ್ಮ ಮಾತುಗಳು ತೂಕ ಹೊಂದಿವೆ—ನಾಟಕಕ್ಕಿಂತ ನಿಖರತೆ. ಇಂದಿನ ನಿಶ್ಯಬ್ದ ಪ್ರಗತಿ ಶಿಸ್ತಿನಿಂದ ನಿಲ್ಲುತ್ತದೆ.",
            "ಒಟ್ಟು ನೋಟ: ಮಿಶ್ರ ಶಕ್ತಿಯಲ್ಲಿ ಸಮಯ ವೇಗಕ್ಕಿಂತ ಮುಖ್ಯ. ಡಿಜಿಟಲ್ ಗೌಪ್ಯತೆ ಕಾಪಾಡಿ, ಯೋಜನೆಗಳನ್ನು ಮಿತವಾಗಿ ಹಂಚಿ. ಒಂದು ನೆರವೇರಿದ ವಾಗ್ದಾನ ಸುತ್ತಮುತ್ತಲಿನ ಆತ್ಮವಿಶ್ವಾಸ ಹೆಚ್ಚಿಸುತ್ತದೆ.",
            "ಹಿರಿಯ/ಕಿರಿಯರಿಗೆ ತಾಳ್ಮೆ ಅನಗತ್ಯ ಘರ್ಷಣೆ ತಡೆಯುತ್ತದೆ. ಧನ್ಯವಾದಗಳನ್ನು ಮುಕ್ತವಾಗಿ ಹೇಳಿ. ಒತ್ತಡದ ದಿನದಲ್ಲೂ ನಗು ಅಥವಾ ಸಹಾಯ ಸಂದೇಶ ವಾತಾವರಣ ಏರಿಸುತ್ತದೆ.",
            "ಅಡ್ಡಿಗಳು ಬಂದರೆ ವೈಫಲ್ಯವಲ್ಲ, ಪ್ರತಿಕ್ರಿಯೆ ಎಂದು ತಿಳಿಯಿರಿ. ಯೋಜನೆ ಸರಿಪಡಿಸಿ, ಒಬ್ಬ ತಜ್ಞರನ್ನು ಕೇಳಿ, ನಾಳೆ ಮತ್ತೆ ಪ್ರಯತ್ನಿಸಿ. ವಾರದ ನಿರಂತರತೆ ದೊಡ್ಡ ಫಲ ನೀಡುತ್ತದೆ."
    };

    private static final String[] TEMPLATES_A_ML = {
            "ഇന്ന് കുടുംബത്തിൽ ചൂടും സത്യസന്ധ സംഭാഷണവും നല്ല ഫലം നൽകും. പരിഹരിക്കേണ്ട കാര്യമുണ്ടെങ്കിൽ ശാന്തമായി സംസാരിക്കൂ; ചെറിയ കാര്യങ്ങളിൽ ശബ്ദം ഉയർത്തരുത്. മുതിർന്നവരോട് ഫോൺ അല്ലെങ്കിൽ ഒരുമിച്ച് ഭക്ഷണം ചെലവേറിയ സമ്മാനത്തേക്കാൾ ബന്ധം ശക്തമാക്കും.",
            "ജോലി/ബിസിനസ്സിൽ തിടുക്കത്തേക്കാൾ ക്ഷമ മെച്ചം. ഒരു പ്രധാന ജോലി തീർത്തശേഷം അടുത്തത്; മെയിൽ/ബിൽ അയയ്ക്കും മുമ്പ് വീണ്ടും പരിശോധിക്കൂ. ഇന്ന് ഉച്ചത്തിലുള്ള പ്രശംസയേക്കാൾ നിശ്ശബ്ദ പിന്തുണ കൂടുതൽ ഉപകാരം.",
            "ആരോഗ്യത്തിന് തുടർശ്രദ്ധ: രാവിലെ വെള്ളം, ദീർഘനേരം ഇരുന്നാൽ നടത്തം, ചെറിയ അസ്വാസ്ഥ്യം അവഗണിക്കരുത്. ലഘു ഭക്ഷണവും നേരത്തെ ഉറക്കവും വൈകിയ സ്ക്രീനിനേക്കാൾ ഊർജ്ജം തിരികെ നൽകും. മാനസിക വിശ്രമവും അത്രയും പ്രധാനം.",
            "പണക്കാര്യത്തിൽ ജാഗ്രത: ആവർത്തിക്കുന്ന ചെലവ് നോക്കൂ, ആവേശ വാങ്ങൽ നിർത്തൂ. ബാക്കി ബിൽ/ഇൻഷുറൻസ് ഇന്ന് ശാന്തമായി നോക്കൂ. നിക്ഷേപം/കടം രേഖകൾ വ്യക്തമായതിനു ശേഷം മാത്രം.",
            "വിദ്യാർത്ഥികൾക്ക് ചെറിയ ഇടവേളയോടെ ഫോക്കസ് പഠനം നല്ലത്; രാത്രി മുഴുവൻ പഠനമല്ല. ദുർബല വിഷയം ആദ്യം, പിന്നെ ടൈമർ അഭ്യാസം. വൃത്തിയായ മേശ, അറിയിപ്പ് ഓഫ് ഏകാഗ്രത വർധിപ്പിക്കും.",
            "യാത്ര/പുതിയ പരിചയം മര്യാദയും ശ്രദ്ധയും ഉണ്ടെങ്കിൽ ലാഭകരം. ഡോക്യുമെന്റ്/ചാർജർ തയ്യാർ, കാലതാമസ ബഫർ, ആദ്യ സംഭാഷണത്തിൽ കൂടുതൽ കേൾക്കൂ. ഒരു പരിചയം ഈ ആഴ്ചയ്ക്ക് ശേഷം ഉപയോഗപ്പെടാം.",
            "വൈകാരിക ശാന്തി വീട്ടിലെ സുഖം കാത്തുസൂക്ഷിക്കും. സമ്മർദത്തിൽ സന്ദേശം അയയ്ക്കും മുമ്പ് നില്ക്കൂ, മൃദുവായ വാക്കുകൾ തിരഞ്ഞെടുക്കൂ. പ്രതീക്ഷയില്ലാതെ സഹായം തർക്കം ജയിക്കുന്നതിനേക്കാൾ നിശ്ശബ്ദ സന്തോഷം തരും.",
            "ഉദാരത നല്ല ഫലം: ചോദിക്കുന്നവർക്ക് സഹായിക്കൂ എന്നാൽ സ്വയം അമിതമായി തളരരുത്. ചെറിയ ഉപകാരം പിന്നീട് മടങ്ങിവരാം. അപ്രതീക്ഷിത സ്ഥലത്തുനിന്ന് സഹായത്തിന് തയ്യാറായിരിക്കൂ."
    };
    private static final String[] TEMPLATES_B_ML = {
            "പ്രണയ/സൗഹൃദത്തിൽ ചെറിയ ആത്മാർത്ഥ സൂചനകൾ നീണ്ട വിശദീകരണത്തേക്കാൾ വിശ്വാസം പുനർനിർമ്മിക്കും. പ്രശംസ പറയൂ, ഫോൺ ഇല്ലാത്ത സമയം മാറ്റിവയ്ക്കൂ. ഇന്ന് ഓൺലൈൻ താരതമ്യം ഒഴിവാക്കൂ.",
            "രാവിലെ ബുദ്ധിമുട്ടുള്ള കോൾ, അപേക്ഷ, ക്രിയാത്മക ജോലിക്ക് നല്ല സമയം. ഉച്ചയ്ക്ക് വരെ തടസ്സം കുറയ്ക്കൂ; വൈകുന്നേരം ലഘു സാമൂഹിക പദ്ധതിക്ക്. കനത്ത തീരുമാനം വൈകുന്നേരം ഒഴിവാക്കൂ.",
            "പുതിയ പ്രോജക്ട്/കരാറിന് രണ്ടാം നോട്ട് വേണം. വ്യവസ്ഥകൾ മൂന്നുതവണ വായിക്കൂ, ഒരു പ്രായോഗിക ചോദ്യം ചോദിക്കൂ, സമ്മർദത്തിൽ ഒപ്പിടരുത്. അവ്യക്തത ഇപ്പോൾ പിന്നീട് ചെലവാകും.",
            "വീട്ടുജോലിയും വിശ്രമവും സന്തുലിതമാക്കൂ; ആരും അധികഭാരം അനുഭവിക്കരുത്. കഴിയുമെങ്കിൽ വിഭജിക്കൂ; ക്ഷീണത്തേക്കാൾ അപൂർണ്ണ മുറികൾ നല്ലത്. ചെറിയ വിശ്രമം വീട്ടുഅന്തരീക്ഷം മാറ്റും.",
            "ശാന്ത പ്രാർത്ഥന, മന്ത്രം അല്ലെങ്കിൽ അഞ്ച് ശ്വാസം ചഞ്ചല ചിന്തകൾ നിർത്തും. ദൈർഘ്യമേറിയ ആചാരം വേണ്ട—സ്ഥിരത തീവ്രതയേക്കാൾ മുഖ്യം. ഒരു നന്ദി വരി ദിവസം ഉറപ്പിക്കും.",
            "ജീവിതലക്ഷ്യങ്ങൾ അടുത്ത നടപടികളായി എഴുതിയാൽ വ്യക്തമാകും. വിശ്വസ്ത സുഹൃത്തിൽ നിന്ന് നിർദ്ദിഷ്ട ഉപദേശം വാങ്ങൂ. ഇനി യോജിക്കാത്ത പഴയ പദ്ധതി ഉപേക്ഷിക്കൂ.",
            "അപ്രതീക്ഷിത ചെലവ് വരാം—ചെറിയ ബഫർ വയ്ക്കൂ, പ്രതികരണ ചെലവ് ഒഴിവാക്കൂ. തിരക്കുള്ള ദിവസത്തിലും ഉറക്കം, വെള്ളം, മരുന്ന് തുടരൂ. ശീലങ്ങൾ മാനസികാവസ്ഥയും പണവും സ്ഥിരപ്പെടുത്തും.",
            "വൈകുന്നേരം ലഘു, പോസിറ്റീവ് കൂടിക്കാഴ്ചകൾക്ക് അനുകൂലം. ഗോസിപ്പ്, വൈകിയ കഫീൻ കുറയ്ക്കൂ. രാത്രി എന്താണ് നന്നായത് എന്ന ചെറിയ അവലോകനം നാളെ ലഘുവാക്കും."
    };
    private static final String[] TEMPLATES_C_ML = {
            "ശുഭ ശ്രദ്ധ: നീതിപൂർവ്വം ഇരിക്കൂ, വാക്ക് നിറവേറ്റൂ, വിശ്രമത്തിന് ഇടം കൊടുക്കൂ. നിങ്ങളുടെ വാക്കുകൾക്ക് ഭാരമുണ്ട്—നാടകത്തേക്കാൾ കൃത്യത. ഇന്നത്തെ നിശ്ശബ്ദ പുരോഗതി അച്ചടക്കത്തോടെ നിലനിൽക്കും.",
            "മൊത്തം കാഴ്ച: മിശ്ര ഊർജ്ജത്തിൽ സമയം സ്പീഡിനേക്കാൾ പ്രധാനം. ഡിജിറ്റൽ സ്വകാര്യത കാത്തുസൂക്ഷിക്കൂ, പദ്ധതികൾ അമിതമായി പങ്കിടരുത്. ഒരു നിറവേറ്റിയ വാഗ്ദാനം ചുറ്റുമുള്ള ആത്മവിശ്വാസം ഉയർത്തും.",
            "മുതിർന്നവർ/കുഞ്ഞുങ്ങളോട് ക്ഷമ അനാവശ്യ ഘർഷണം തടയും. നന്ദി സ്വതന്ത്രമായി പറയൂ. സമ്മർദ ദിവസത്തിലും പുഞ്ചിരി അല്ലെങ്കിൽ സഹായ സന്ദേശം അന്തരീക്ഷം ഉയർത്തും.",
            "തടസ്സം വന്നാൽ പരാജയമല്ല, ഫീഡ്‌ബാക്ക് ആയി കാണൂ. പദ്ധതി ക്രമീകരിക്കൂ, ഒരു വിദഗ്ധനോട് ചോദിക്കൂ, നാളെ വീണ്ടും ശ്രമിക്കൂ. ആഴ്ചയുടെ സ്ഥിരത വലിയ ഫലം തരും."
    };

    private static final String[] TEMPLATES_A_OR = {
            "ଆଜି ପରିବାରରେ ଉଷ୍ଣତା ଓ ସତ୍ୟ କଥା ଭଲ ଫଳ ଦେବ। ସମାଧାନ ଦରକାର ହେଲେ ଶାନ୍ତରେ କୁହନ୍ତୁ; ଛୋଟ କଥାରେ ଚିତ୍କାର କରନ୍ତୁ ନାହିଁ। ବଡ଼ମାନଙ୍କ ସହ ଫୋନ୍ କିମ୍ବା ଏକାଠି ଖାଇବା ମହଙ୍ଗା ଉପହାର ଠାରୁ ସମ୍ପର୍କ ମଜବୁତ କରେ।",
            "କାମ/ବ୍ୟବସାୟରେ ତ୍ୱରା ଠାରୁ ଧୈର୍ଯ୍ୟ ଭଲ। ଗୋଟିଏ ମୁଖ୍ୟ କାମ ସାରି ତା’ପରେ ପରବର୍ତ୍ତୀ; ମେଲ୍/ବିଲ୍ ପଠାଇବା ପୂର୍ବରୁ ଦୁଇଥର ଯାଞ୍ଚ କରନ୍ତୁ। ଆଜି ଉଚ୍ଚ ପ୍ରଶଂସା ଠାରୁ ନୀରବ ସମର୍ଥନ ଅଧିକ କାମ ଆସିବ।",
            "ସ୍ୱାସ୍ଥ୍ୟରେ ନିୟମିତ ଧ୍ୟାନ: ସକାଳେ ପାଣି, ବହୁତ ବସିଲେ ଚାଲିବା, ଛୋଟ ଅସୁବିଧା ଅଣଦେଖା କରନ୍ତୁ ନାହିଁ। ହାଲୁକା ଖାଦ୍ୟ ଓ ଶୀଘ୍ର ନିଦ ବିଳମ୍ବ ସ୍କ୍ରିନ୍ ଠାରୁ ଶକ୍ତି ଫେରାଏ। ମାନସିକ ବିଶ୍ରାମ ମଧ୍ୟ ଗୁରୁତ୍ୱପୂର୍ଣ୍ଣ।",
            "ଟଙ୍କା ବିଷୟରେ ସତର୍କତା: ପୁନରାବୃତ୍ତ ଖର୍ଚ୍ଚ ଦେଖନ୍ତୁ, ଆବେଗ କିଣା ବନ୍ଦ କରନ୍ତୁ। ବକେୟା ବିଲ୍/ଇନସ୍ୟୁରେନ୍ସ ଆଜି ଶାନ୍ତରେ ଦେଖନ୍ତୁ। ନିବେଶ/ଋଣ କାଗଜ ସ୍ପଷ୍ଟ ହେଲା ପରେ।",
            "ଛାତ୍ରମାନଙ୍କ ପାଇଁ ଛୋଟ ବିରତି ସହ ଫୋକସ୍ ପଢ଼ା ଭଲ; ରାତିସାରା ପଢ଼ିବା ନୁହେଁ। ଦୁର୍ବଳ ବିଷୟ ପ୍ରଥମେ, ତା’ପରେ ଟାଇମର୍ ଅଭ୍ୟାସ। ସଫା ଟେବୁଲ୍, ନୋଟିଫିକେସନ୍ ବନ୍ଦ ଏକାଗ୍ରତା ବଢ଼ାଏ।",
            "ଯାତ୍ରା/ନୂଆ ପରିଚୟ ସମ୍ମାନ ଓ ଧ୍ୟାନ ସହ ଲାଭଦାୟକ। ଡକ୍ୟୁମେଣ୍ଟ/ଚାର୍ଜର୍ ପ୍ରସ୍ତୁତ, ବିଳମ୍ବ ବଫର୍, ପ୍ରଥମ କଥାରେ ଅଧିକ ଶୁଣନ୍ତୁ। ଗୋଟିଏ ପରିଚୟ ଏହି ସପ୍ତାହ ପରେ କାମ ଆସିପାରେ।",
            "ଭାବନାତ୍ମକ ଶାନ୍ତି ଘରର ସୁଖ ରକ୍ଷା କରେ। ଚାପରେ ମେସେଜ୍ ପଠାଇବା ପୂର୍ବରୁ ରୁହନ୍ତୁ, କୋମଳ ଶବ୍ଦ ବାଛନ୍ତୁ। ଅପେକ୍ଷା ବିନା ସାହାଯ୍ୟ ବିବାଦ ଜିତିବା ଠାରୁ ନୀରବ ଖୁସି ଦିଏ।",
            "ଉଦାରତା ଭଲ ଫଳ: ମାଗୁଥିବାଙ୍କୁ ସାହାଯ୍ୟ କରନ୍ତୁ କିନ୍ତୁ ନିଜକୁ ଅତ୍ୟଧିକ କ୍ଳାନ୍ତ କରନ୍ତୁ ନାହିଁ। ଛୋଟ ଉପକାର ପରେ ଫେରିଆସିପାରେ। ଅପ୍ରତ୍ୟାଶିତ ସ୍ଥାନରୁ ସାହାଯ୍ୟ ପାଇଁ ପ୍ରସ୍ତୁତ ରୁହନ୍ତୁ।"
    };
    private static final String[] TEMPLATES_B_OR = {
            "ପ୍ରେମ/ବନ୍ଧୁତାରେ ଛୋଟ ସତ୍ୟ ଇଙ୍ଗିତ ଲମ୍ବା ବ୍ୟାଖ୍ୟା ଠାରୁ ବିଶ୍ୱାସ ଗଢ଼େ। ପ୍ରଶଂସା କୁହନ୍ତୁ, ଫୋନ୍ ବିନା ସମୟ ରଖନ୍ତୁ। ଆଜି ଅନଲାଇନ୍ ତୁଳନା ଏଡ଼ାନ୍ତୁ।",
            "ସକାଳ କଠିନ କଲ୍, ଆବେଦନ କିମ୍ବା ସୃଜନାତ୍ମକ କାମ ପାଇଁ ଭଲ। ମଧ୍ୟାହ୍ନ ପର୍ଯ୍ୟନ୍ତ ବାଧା କମାନ୍ତୁ; ସନ୍ଧ୍ୟା ହାଲୁକା ସାମାଜିକ ଯୋଜନା। ଭାରୀ ନିଷ୍ପତ୍ତି ସନ୍ଧ୍ୟାରେ ନିଅନ୍ତୁ ନାହିଁ।",
            "ନୂଆ ପ୍ରକଳ୍ପ/ଚୁକ୍ତିକୁ ଦ୍ୱିତୀୟ ନଜର ଦରକାର। ସର୍ତ୍ତ ତିନିଥର ପଢ଼ନ୍ତୁ, ଗୋଟିଏ ବ୍ୟବହାରିକ ପ୍ରଶ୍ନ ପଚାରନ୍ତୁ, ଚାପରେ ସାଇନ୍ କରନ୍ତୁ ନାହିଁ। ଅସ୍ପଷ୍ଟତା ବର୍ତ୍ତମାନ ପରେ ମହଙ୍ଗା।",
            "ଘର କାମ ଓ ବିଶ୍ରାମରେ ସନ୍ତୁଳନ; କେହି ଅଧିକ ଭାର ଅନୁଭବ କରିବେ ନାହିଁ। ସମ୍ଭବ ହେଲେ ବାଣ୍ଟନ୍ତୁ; କ୍ଳାନ୍ତି ଠାରୁ ଅସମ୍ପୂର୍ଣ୍ଣ କୋଠରୀ ଭଲ। ଛୋଟ ବିରାମ ଘର ପରିବେଶ ବଦଳାଏ।",
            "ଶାନ୍ତ ପ୍ରାର୍ଥନା, ମନ୍ତ୍ର କିମ୍ବା ପାଞ୍ଚ ଶ୍ୱାସ ଚଞ୍ଚଳ ଚିନ୍ତା ରୋକେ। ଦୀର୍ଘ ଆଚାର ଦରକାର ନାହିଁ—ନିୟମିତତା ତୀବ୍ରତା ଠାରୁ ମୁଖ୍ୟ। ଗୋଟିଏ କୃତଜ୍ଞତା ଧାଡ଼ି ଦିନ ସ୍ଥିର କରେ।",
            "ଜୀବନ ଲକ୍ଷ୍ୟ ପରବର୍ତ୍ତୀ ପଦକ୍ଷେପ ଭାବେ ଲେଖିଲେ ସ୍ପଷ୍ଟ। ବିଶ୍ୱସ୍ତ ବନ୍ଧୁଙ୍କଠାରୁ ନିର୍ଦ୍ଦିଷ୍ଟ ପରାମର୍ଶ ନିଅନ୍ତୁ। ଆଉ ମେଳ ନଖାଉଥିବା ପୁରୁଣା ଯୋଜନା ଛାଡ଼ନ୍ତୁ।",
            "ଅପ୍ରତ୍ୟାଶିତ ଖର୍ଚ୍ଚ ଆସିପାରେ—ଛୋଟ ବଫର୍ ରଖନ୍ତୁ, ପ୍ରତିକ୍ରିୟା ଖର୍ଚ୍ଚ ଏଡ଼ାନ୍ତୁ। ବ୍ୟସ୍ତ ଦିନରେ ମଧ୍ୟ ନିଦ, ପାଣି, ଔଷଧ ଜାରି ରଖନ୍ତୁ। ଅଭ୍ୟାସ ମୂଡ୍ ଓ ଟଙ୍କା ଉଭୟ ସ୍ଥିର କରେ।",
            "ସନ୍ଧ୍ୟା ହାଲୁକା, ସକାରାତ୍ମକ ଭେଟ ପାଇଁ ଅନୁକୂଳ। ଗସିପ୍, ବିଳମ୍ବ କ୍ୟାଫିନ୍ କମାନ୍ତୁ। ରାତିରେ କ’ଣ ଭଲ ହେଲା ସେ ସଂକ୍ଷିପ୍ତ ସମୀକ୍ଷା କାଲି ହାଲୁକା ଆରମ୍ଭ କରାଏ।"
    };
    private static final String[] TEMPLATES_C_OR = {
            "ଶୁଭ ଧ୍ୟାନ: ନ୍ୟାୟ ରୁହନ୍ତୁ, କଥା ରଖନ୍ତୁ, ବିଶ୍ରାମକୁ ସ୍ଥାନ ଦିଅନ୍ତୁ। ଆପଣଙ୍କ ଶବ୍ଦର ଓଜନ ଅଛି—ନାଟକ ଠାରୁ ସଠିକତା। ଆଜିର ନୀରବ ପ୍ରଗତି ଶୃଙ୍ଖଳା ସହ ଟିକେ।",
            "ମୋଟ ଦୃଷ୍ଟି: ମିଶ୍ର ଶକ୍ତିରେ ସମୟ ଗତି ଠାରୁ ମୁଖ୍ୟ। ଡିଜିଟାଲ୍ ଗୋପନୀୟତା ରଖନ୍ତୁ, ଯୋଜନା ଅଧିକ କୁହନ୍ତୁ ନାହିଁ। ଗୋଟିଏ ପୂରଣ ହୋଇଥିବା ପ୍ରତିଜ୍ଞା ଚାରିପାଖେ ଆତ୍ମବିଶ୍ୱାସ ବଢ଼ାଏ।",
            "ବଡ଼/ଛୋଟ ପ୍ରତି ଧୈର୍ଯ୍ୟ ଅନାବଶ୍ୟକ ଘର୍ଷଣ ରୋକେ। ଧନ୍ୟବାଦ ଖୋଲାଖୋଲି କୁହନ୍ତୁ। ଚାପିଲା ଦିନରେ ମଧ୍ୟ ହସ କିମ୍ବା ସାହାଯ୍ୟ ମେସେଜ୍ ପରିବେଶ ଉନ୍ନତ କରେ।",
            "ବାଧା ଆସିଲେ ବିଫଳତା ନୁହେଁ, ମତାମତ ବୁଝନ୍ତୁ। ଯୋଜନା ସଜାଡ଼ନ୍ତୁ, ଗୋଟିଏ ବିଶେଷଜ୍ଞ ପଚାରନ୍ତୁ, କାଲି ପୁଣି ଚେଷ୍ଟା କରନ୍ତୁ। ସପ୍ତାହ ଧରି ନିୟମିତତା ବଡ଼ ଫଳ ଦିଏ।"
    };

    private RaasiContent() {
    }

    public static final class Daily {
        public final int rashiId;
        public final String nameTe;
        public final String dateKey;
        public final String title;
        public final String body;
        public final String color;
        public final String numbers;
        public final String direction;

        Daily(int rashiId, String nameTe, String dateKey, String title,
              String body, String color, String numbers, String direction) {
            this.rashiId = rashiId;
            this.nameTe = nameTe;
            this.dateKey = dateKey;
            this.title = title;
            this.body = body;
            this.color = color;
            this.numbers = numbers;
            this.direction = direction;
        }
    }

    @NonNull
    public static Daily forToday(int rashiId) {
        return forToday(null, rashiId);
    }

    @NonNull
    public static Daily forToday(@Nullable Context context, int rashiId) {
        // Always use IST calendar-day boundary so packs are shared for the India day.
        Calendar c = Calendar.getInstance(RaasiDailyCache.dateZone());
        return forDate(context, rashiId, c);
    }

    @NonNull
    public static Daily forDate(int rashiId, @NonNull Calendar day) {
        return forDate(null, rashiId, day);
    }

    /**
     * Returns daily phalalu for rashi. With a non-null context, loads or builds a
     * language+date pack of all 12 rashis (memory + disk, TTL until next IST day).
     */
    @NonNull
    public static Daily forDate(@Nullable Context context, int rashiId, @NonNull Calendar day) {
        int id = clampId(rashiId);
        int lang = languageId(context);
        String dateKey = RaasiDailyCache.dateKeyFrom(day);

        if (context != null) {
            Context app = context.getApplicationContext();
            RaasiDailyCache.Pack pack = RaasiDailyCache.load(app, lang, dateKey);
            if (pack == null) {
                pack = buildAndStorePack(app, lang, day, dateKey);
            }
            if (pack != null) {
                RaasiDailyCache.Item item = pack.get(id);
                if (item != null && item.body != null) {
                    return toDaily(id, dateKey, day, item);
                }
            }
        }
        return generateUncached(id, lang, day, dateKey);
    }

    @NonNull
    private static RaasiDailyCache.Pack buildAndStorePack(
            @NonNull Context app, int lang, @NonNull Calendar day, @NonNull String dateKey) {
        RaasiDailyCache.Pack pack = new RaasiDailyCache.Pack();
        pack.contentVersion = RaasiDailyCache.CONTENT_VERSION;
        pack.languageId = lang;
        pack.dateKey = dateKey;
        pack.cacheKey = RaasiDailyCache.packKey(lang, dateKey);
        pack.items = new RaasiDailyCache.Item[COUNT];
        for (int i = 0; i < COUNT; i++) {
            Daily d = generateUncached(i, lang, day, dateKey);
            RaasiDailyCache.Item it = new RaasiDailyCache.Item();
            it.rashiId = i;
            it.body = d.body;
            it.color = d.color;
            it.numbers = d.numbers;
            it.direction = d.direction;
            pack.items[i] = it;
        }
        RaasiDailyCache.save(app, pack);
        return pack;
    }

    @NonNull
    private static Daily toDaily(int id, @NonNull String dateKey, @NonNull Calendar day,
                                 @NonNull RaasiDailyCache.Item item) {
        int y = day.get(Calendar.YEAR);
        int m0 = day.get(Calendar.MONTH);
        int d = day.get(Calendar.DAY_OF_MONTH);
        String dateDisp = String.format(Locale.US, "%02d %s %d", d, EN_MONTHS[m0], y);
        String title = NAMES_TE[id] + " రాశి ఫలితాలు (" + dateDisp + ")";
        return new Daily(id, NAMES_TE[id], dateKey, title,
                item.body != null ? item.body : "",
                item.color != null ? item.color : "",
                item.numbers != null ? item.numbers : "",
                item.direction != null ? item.direction : "");
    }

    /** Deterministic generate (no cache read/write). */
    @NonNull
    private static Daily generateUncached(int id, int lang, @NonNull Calendar day,
                                          @NonNull String dateKey) {
        id = clampId(id);
        int y = day.get(Calendar.YEAR);
        int m0 = day.get(Calendar.MONTH);
        int d = day.get(Calendar.DAY_OF_MONTH);
        int doy = day.get(Calendar.DAY_OF_YEAR);
        int seed = stableHash(id, dateKey);

        String[] ta = templatesA(lang);
        String[] tb = templatesB(lang);
        String[] tc = templatesC(lang);
        String a = ta[Math.floorMod(seed, ta.length)];
        String b = tb[Math.floorMod(seed / 7 + doy, tb.length)];
        String c = tc[Math.floorMod(seed / 13 + id + doy, tc.length)];
        // Long multi-paragraph style body for list truncate + full detail modal.
        String body = a + "\n\n" + b + "\n\n" + c;

        String[] colors = colorsFor(lang);
        String[] dirs = dirsFor(lang);
        String color = colors[Math.floorMod(seed + id * 3, colors.length)];
        int n1 = 1 + Math.floorMod(seed, 9);
        int n2 = 1 + Math.floorMod(seed / 11 + id, 9);
        if (n2 == n1) n2 = 1 + (n2 % 9);
        String numbers = n1 + ", " + n2;
        String dir = dirs[Math.floorMod(seed / 5 + doy, dirs.length)];

        String dateDisp = String.format(Locale.US, "%02d %s %d", d, EN_MONTHS[m0], y);
        String title = NAMES_TE[id] + " రాశి ఫలితాలు (" + dateDisp + ")";
        return new Daily(id, NAMES_TE[id], dateKey, title, body, color, numbers, dir);
    }

    /** Drawable res id for monochrome relief icon, or 0 if missing. */
    public static int iconResId(@NonNull Context context, int rashiId0) {
        int id = clampId(rashiId0);
        String name = "ic_raasi_" + ICON_KEYS[id];
        return context.getResources().getIdentifier(name, "drawable", context.getPackageName());
    }

    private static String[] templatesA(int lang) {
        switch (lang) {
            case 1: return TEMPLATES_A_ML;
            case 2: return TEMPLATES_A_HI;
            case 3: return TEMPLATES_A_TE;
            case 4: return TEMPLATES_A_TA;
            case 5: return TEMPLATES_A_KN;
            case 6: return TEMPLATES_A_OR;
            default: return TEMPLATES_A_EN;
        }
    }

    private static String[] templatesB(int lang) {
        switch (lang) {
            case 1: return TEMPLATES_B_ML;
            case 2: return TEMPLATES_B_HI;
            case 3: return TEMPLATES_B_TE;
            case 4: return TEMPLATES_B_TA;
            case 5: return TEMPLATES_B_KN;
            case 6: return TEMPLATES_B_OR;
            default: return TEMPLATES_B_EN;
        }
    }

    private static String[] templatesC(int lang) {
        switch (lang) {
            case 1: return TEMPLATES_C_ML;
            case 2: return TEMPLATES_C_HI;
            case 3: return TEMPLATES_C_TE;
            case 4: return TEMPLATES_C_TA;
            case 5: return TEMPLATES_C_KN;
            case 6: return TEMPLATES_C_OR;
            default: return TEMPLATES_C_EN;
        }
    }

    private static String[] colorsFor(int lang) {
        switch (lang) {
            case 1: return COLORS_ML;
            case 2: return COLORS_HI;
            case 3: return COLORS_TE;
            case 4: return COLORS_TA;
            case 5: return COLORS_KN;
            case 6: return COLORS_OR;
            default: return COLORS_EN;
        }
    }

    private static String[] dirsFor(int lang) {
        switch (lang) {
            case 1: return DIRS_ML;
            case 2: return DIRS_HI;
            case 3: return DIRS_TE;
            case 4: return DIRS_TA;
            case 5: return DIRS_KN;
            case 6: return DIRS_OR;
            default: return DIRS_EN;
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

    private static final String[] EN_MONTHS = {
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
    };

    public static int clampId(int id) {
        if (id < 0 || id >= COUNT) return 0;
        return id;
    }

    /**
     * Parse admin/splash default rashi: 1–12 (1=Mesha), 0–11, or key (mesha…meena / scorpio).
     * Returns 0-based index. Unparseable → {@code fallback0} clamped.
     */
    public static int parseDefaultId(@Nullable String raw, int fallback0) {
        if (raw == null) return clampId(fallback0);
        String s = raw.trim().toLowerCase(Locale.US);
        if (s.isEmpty()) return clampId(fallback0);
        try {
            int n = Integer.parseInt(s);
            if (n >= 1 && n <= 12) return n - 1;
            if (n >= 0 && n <= 11) return n;
        } catch (NumberFormatException ignored) {
        }
        for (int i = 0; i < KEYS.length; i++) {
            if (KEYS[i].equals(s)) return i;
        }
        if ("aries".equals(s) || "mesh".equals(s)) return 0;
        if ("scorpio".equals(s) || "vruschika".equals(s) || "vrishchika".equals(s)) return 7;
        return clampId(fallback0);
    }

    public static int parseDefaultId(@Nullable Object raw, int fallback0) {
        if (raw == null) return clampId(fallback0);
        if (raw instanceof Number) {
            int n = ((Number) raw).intValue();
            if (n >= 1 && n <= 12) return n - 1;
            if (n >= 0 && n <= 11) return n;
            return clampId(fallback0);
        }
        return parseDefaultId(String.valueOf(raw), fallback0);
    }

    /**
     * Very rough sidereal-ish sun sign from DOB (Western tropical range, shifted by ~23 days).
     * Good enough for “know your rashi” helper — not a kundali calculation.
     */
    public static int roughRashiFromDob(int year, int month1, int day) {
        int md = month1 * 100 + day;
        if (md >= 414 && md <= 514) return 0;
        if (md >= 515 && md <= 614) return 1;
        if (md >= 615 && md <= 716) return 2;
        if (md >= 717 && md <= 816) return 3;
        if (md >= 817 && md <= 916) return 4;
        if (md >= 917 && md <= 1016) return 5;
        if (md >= 1017 && md <= 1115) return 6;
        if (md >= 1116 && md <= 1215) return 7;
        if (md >= 1216 || md <= 114) return 8;
        if (md >= 115 && md <= 212) return 9;
        if (md >= 213 && md <= 314) return 10;
        return 11;
    }

    private static int stableHash(int rashiId, @Nullable String dateKey) {
        String s = rashiId + "|" + (dateKey != null ? dateKey : "");
        int h = 0x811c9dc5;
        for (int i = 0; i < s.length(); i++) {
            h ^= s.charAt(i);
            h *= 0x01000193;
        }
        return h & 0x7fffffff;
    }
}
