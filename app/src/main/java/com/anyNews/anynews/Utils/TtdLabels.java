package com.anyNews.anynews.Utils;

import android.content.Context;

import androidx.annotation.Nullable;

/**
 * TTD page labels by in-app {@link Constants#LanguageID}
 * (1 ML · 2 HI · 3 TE · 4 TA · 5 KN · 6 OR · else EN).
 * Same pattern as {@link FooterTabLabels} / {@link HomeChromeActionLabels}.
 */
public final class TtdLabels {

    private TtdLabels() {}

    public static String topSubtitle(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "ശ്രീ വെങ്കടേശ്വര സ്വാമി, തിരുമല";
            case 2: return "श्री वेंकटेश्वर स्वामी, तिरुमला";
            case 3: return "శ్రీ వేంకటేశ్వర స్వామి, తిరుమల";
            case 4: return "ஸ்ரீ வெங்கடேஸ்வர சுவாமி, திருமலை";
            case 5: return "ಶ್ರೀ ವೆಂಕಟೇಶ್ವರ ಸ್ವಾಮಿ, ತಿರುಮಲ";
            case 6: return "ଶ୍ରୀ ଭେଙ୍କଟେଶ୍ୱର ସ୍ୱାମୀ, ତିରୁମଲା";
            default: return "Sri Venkateswara Swamy, Tirumala";
        }
    }

    public static String heroTitle(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "ശ്രീ വെങ്കടേശ്വര സ്വാമി";
            case 2: return "श्री वेंकटेश्वर स्वामी";
            case 3: return "శ్రీ వేంకటేశ్వర స్వామి";
            case 4: return "ஸ்ரீ வெங்கடேஸ்வர சுவாமி";
            case 5: return "ಶ್ರೀ ವೆಂಕಟೇಶ್ವರ ಸ್ವಾಮಿ";
            case 6: return "ଶ୍ରୀ ଭେଙ୍କଟେଶ୍ୱର ସ୍ୱାମୀ";
            default: return "Sri Venkateswara Swamy";
        }
    }

    public static String heroSubtitle(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "തിരുമല തിരുപതി ദേവസ്ഥാനം";
            case 2: return "तिरुमला तिरुपति देवस्थानम्";
            case 3: return "తిరుమల తిరుపతి దేవస్థానం";
            case 4: return "திருமலை திருப்பதி தேவஸ்தானம்";
            case 5: return "ತಿರುಮಲ ತಿರುಪತಿ ದೇವಸ್ಥಾನಂ";
            case 6: return "ତିରୁମଲା ତିରୁପତି ଦେବସ୍ଥାନମ୍";
            default: return "Tirumala Tirupati Devasthanams";
        }
    }

    public static String heroBody(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "ഭക്തർക്ക് ദർശനം, സേവകൾ, വസതി സൗകര്യങ്ങൾ.";
            case 2: return "भक्तों के लिए दर्शन, सेवाएँ और आवास सुविधाएँ।";
            case 3: return "భక్తులకు దర్శనం, సేవలు, వసతి సౌకర్యాలు.";
            case 4: return "பக்தர்களுக்கு தரிசனம், சேவைகள், தங்குமிட வசதிகள்.";
            case 5: return "ಭಕ್ತರಿಗೆ ದರ್ಶನ, ಸೇವೆಗಳು, ವಸತಿ ಸೌಕರ್ಯಗಳು.";
            case 6: return "ଭକ୍ତଙ୍କ ପାଇଁ ଦର୍ଶନ, ସେବା ଓ ବାସସ୍ଥାନ ସୁବିଧା।";
            default: return "Darshan, sevas and lodging for pilgrims.";
        }
    }

    public static String quickDarshan(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "ദർശനം";
            case 2: return "दर्शन";
            case 3: return "దర్శనము";
            case 4: return "தரிசனம்";
            case 5: return "ದರ್ಶನ";
            case 6: return "ଦର୍ଶନ";
            default: return "Darshanam";
        }
    }

    public static String quickDarshanSub(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "ടിക്കറ്റ് ബുക്കിംഗ്";
            case 2: return "टिकट बुकिंग";
            case 3: return "టికెట్ బుకింగ్";
            case 4: return "டிக்கெட் முன்பதிவு";
            case 5: return "ಟಿಕೆಟ್ ಬುಕಿಂಗ್";
            case 6: return "ଟିକେଟ୍ ବୁକିଂ";
            default: return "Ticket Booking";
        }
    }

    public static String quickRoom(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "താമസം";
            case 2: return "आवास";
            case 3: return "వసతి";
            case 4: return "தங்குமிடம்";
            case 5: return "ವಸತಿ";
            case 6: return "ବାସସ୍ଥାନ";
            default: return "Accommodation";
        }
    }

    public static String quickRoomSub(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "മുറി ബുക്കിംഗ്";
            case 2: return "कमरा बुकिंग";
            case 3: return "గది బుకింగ్";
            case 4: return "அறை முன்பதிவு";
            case 5: return "ಕೋಣೆ ಬುಕಿಂಗ್";
            case 6: return "ରୁମ୍ ବୁକିଂ";
            default: return "Room Booking";
        }
    }

    public static String quickAnna(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "അന്നപ്രസാദം";
            case 2: return "अन्नप्रसादम";
            case 3: return "అన్నప్రసాదం";
            case 4: return "அன்னப்பிரசாதம்";
            case 5: return "ಅನ್ನಪ್ರಸಾದ";
            case 6: return "ଅନ୍ନପ୍ରସାଦ";
            default: return "Annaprasadam";
        }
    }

    public static String quickAnnaSub(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "വിവരങ്ങൾ";
            case 2: return "जानकारी";
            case 3: return "సమాచారం";
            case 4: return "தகவல்";
            case 5: return "ಮಾಹಿತಿ";
            case 6: return "ସୂଚନା";
            default: return "Information";
        }
    }

    public static String quickHundi(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "ഹുണ്ടി";
            case 2: return "हुंडी";
            case 3: return "హుండీ";
            case 4: return "ஹுண்டி";
            case 5: return "ಹುಂಡಿ";
            case 6: return "ହୁଣ୍ଡି";
            default: return "Hundi";
        }
    }

    public static String quickHundiSub(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "ഓൺലൈൻ സേവനം";
            case 2: return "ऑनलाइन सेवा";
            case 3: return "ఆన్‌లైన్ సేవ";
            case 4: return "ஆன்லைன் சேவை";
            case 5: return "ಆನ್‌ಲೈನ್ ಸೇವೆ";
            case 6: return "ଅନଲାଇନ୍ ସେବା";
            default: return "Online Service";
        }
    }

    public static String darshanHeading(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "ദർശന വിവരങ്ങൾ";
            case 2: return "दर्शन जानकारी";
            case 3: return "దర్శన సమాచారం";
            case 4: return "தரிசன தகவல்";
            case 5: return "ದರ್ಶನ ಮಾಹಿತಿ";
            case 6: return "ଦର୍ଶନ ସୂଚନା";
            default: return "Darshanam Information";
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

    public static String sarvaLabel(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "സർവ ദർശനം";
            case 2: return "सर्व दर्शन";
            case 3: return "సర్వ దర్శనం";
            case 4: return "சர்வ தரிசனம்";
            case 5: return "ಸರ್ವ ದರ್ಶನ";
            case 6: return "ସର୍ବ ଦର୍ଶନ";
            default: return "Sarva Darshanam";
        }
    }

    public static String sarvaValue(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "24 മണിക്കൂർ";
            case 2: return "24 घंटे";
            case 3: return "24 గంటలు";
            case 4: return "24 மணி";
            case 5: return "24 ಗಂಟೆಗಳು";
            case 6: return "24 ଘଣ୍ଟା";
            default: return "24 Hours";
        }
    }

    /** Hour count from the saved Tirumala pack, in the selected script. */
    public static String hoursCount(int hours, @Nullable Context c) {
        if (hours <= 0) return "";
        String n = String.valueOf(hours);
        switch (lang(c)) {
            case 1: return n + " മണിക്കൂർ";
            case 2: return n + " घंटे";
            case 3: return n + " గంటలు";
            case 4: return n + " மணி";
            case 5: return n + " ಗಂಟೆಗಳು";
            case 6: return n + " ଘଣ୍ଟା";
            default: return n + " Hours";
        }
    }

    /** {@code yyyy-MM-dd} from the official update date, month in the selected script. */
    public static String newsDate(@Nullable String iso, @Nullable Context c) {
        if (iso == null || iso.length() < 10) return "";
        try {
            int year = Integer.parseInt(iso.substring(0, 4));
            int month = Integer.parseInt(iso.substring(5, 7));
            int day = Integer.parseInt(iso.substring(8, 10));
            if (month < 1 || month > 12 || day < 1 || day > 31) return "";
            String mon = MONTHS[monthIndex(c)][month - 1];
            return String.format(java.util.Locale.US, "%02d %s, %d", day, mon, year);
        } catch (Exception e) {
            return "";
        }
    }

    private static int monthIndex(@Nullable Context c) {
        int id = lang(c);
        if (id < 0 || id > 6) return 0;
        return id;
    }

    private static final String[][] MONTHS = {
            {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"},
            {"ജനു", "ഫെബ്രു", "മാർ", "ഏപ്രി", "മേയ്", "ജൂൺ", "ജൂലൈ", "ഓഗ", "സെപ്റ്റ", "ഒക്ടോ", "നവം", "ഡിസം"},
            {"जन", "फर", "मार्च", "अप्रै", "मई", "जून", "जुल", "अग", "सित", "अक्टू", "नव", "दिस"},
            {"జన", "ఫిబ్ర", "మార్చి", "ఏప్రి", "మే", "జూన్", "జూలై", "ఆగ", "సెప్టె", "అక్టో", "నవం", "డిసె"},
            {"ஜன", "பிப்", "மார்", "ஏப்", "மே", "ஜூன்", "ஜூலை", "ஆக", "செப்", "அக்", "நவ", "டிச"},
            {"ಜನ", "ಫೆಬ್ರ", "ಮಾರ್ಚ್", "ಏಪ್ರಿ", "ಮೇ", "ಜೂನ್", "ಜುಲೈ", "ಆಗ", "ಸೆಪ್ಟೆ", "ಅಕ್ಟೋ", "ನವಂ", "ಡಿಸೆ"},
            {"ଜାନ", "ଫେବ", "ମାର୍ଚ୍ଚ", "ଅପ୍ରେ", "ମଇ", "ଜୁନ", "ଜୁଲା", "ଅଗ", "ସେପ୍ଟ", "ଅକ୍ଟୋ", "ନଭ", "ଡିସ"},
    };

    public static String specialLabel(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "പ്രത്യേക ദർശനം";
            case 2: return "विशेष दर्शन";
            case 3: return "ప్రత్యేక దర్శనం";
            case 4: return "சிறப்பு தரிசனம்";
            case 5: return "ವಿಶೇಷ ದರ್ಶನ";
            case 6: return "ବିଶେଷ ଦର୍ଶନ";
            default: return "Special Darshanam";
        }
    }

    public static String timingsLabel(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "ദർശന സമയം";
            case 2: return "दर्शन समय";
            case 3: return "దర్శనం సమయం";
            case 4: return "தரிசன நேரம்";
            case 5: return "ದರ್ಶನ ಸಮಯ";
            case 6: return "ଦର୍ଶନ ସମୟ";
            default: return "Darshanam Timings";
        }
    }

    public static String bookDarshan(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "ദർശന ടിക്കറ്റ് ബുക്ക് ചെയ്യുക ›";
            case 2: return "दर्शन टिकट बुक करें ›";
            case 3: return "దర్శన టికెట్ బుకింగ్ చేయండి ›";
            case 4: return "தரிசன டிக்கெட் முன்பதிவு ›";
            case 5: return "ದರ್ಶನ ಟಿಕೆಟ್ ಬುಕ್ ಮಾಡಿ ›";
            case 6: return "ଦର୍ଶନ ଟିକେଟ୍ ବୁକ୍ କରନ୍ତୁ ›";
            default: return "Book Darshanam Ticket ›";
        }
    }

    public static String sevasHeading(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "സേവകളും കാഴ്ചകളും";
            case 2: return "सेवाएँ एवं अर्पण";
            case 3: return "సేవలు & కానుకలు";
            case 4: return "சேவைகள் & காணிக்கைகள்";
            case 5: return "ಸೇವೆಗಳು & ಕಾಣಿಕೆಗಳು";
            case 6: return "ସେବା ଓ ଉତ୍ସର୍ଗ";
            default: return "Services & Offerings";
        }
    }

    public static String viewAll(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "എല്ലാ സേവകളും ›";
            case 2: return "सभी सेवाएँ ›";
            case 3: return "అన్ని సేవలు ›";
            case 4: return "அனைத்து சேவைகள் ›";
            case 5: return "ಎಲ್ಲಾ ಸೇವೆಗಳು ›";
            case 6: return "ସମସ୍ତ ସେବା ›";
            default: return "View All ›";
        }
    }

    public static String newsHeading(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "TTD ഏറ്റവും പുതിയ വാർത്തകൾ";
            case 2: return "TTD ताज़ा खबरें";
            case 3: return "TTD తాజా వార్తలు";
            case 4: return "TTD சமீபத்திய செய்திகள்";
            case 5: return "TTD ಇತ್ತೀಚಿನ ಸುದ್ದಿ";
            case 6: return "TTD ନୂତନ ଖବର";
            default: return "TTD Latest News";
        }
    }

    public static String more(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "കൂടുതൽ ›";
            case 2: return "और ›";
            case 3: return "మరిన్ని ›";
            case 4: return "மேலும் ›";
            case 5: return "ಇನ್ನಷ್ಟು ›";
            case 6: return "ଅଧିକ ›";
            default: return "More ›";
        }
    }

    public static String linksHeading(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "പ്രധാന ലിങ്കുകൾ";
            case 2: return "महत्वपूर्ण लिंक";
            case 3: return "ముఖ్యమైన లింకులు";
            case 4: return "முக்கிய இணைப்புகள்";
            case 5: return "ಪ್ರಮುಖ ಲಿಂಕ್‌ಗಳು";
            case 6: return "ଗୁରୁତ୍ୱପୂର୍ଣ୍ଣ ଲିଙ୍କ୍";
            default: return "Important Links";
        }
    }

    public static String linkOfficial(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "TTD ഔദ്യോഗിക സൈറ്റ്";
            case 2: return "TTD आधिकारिक साइट";
            case 3: return "TTD అధికారిక వెబ్‌సైట్";
            case 4: return "TTD அதிகாரப்பூர்வ தளம்";
            case 5: return "TTD ಅಧಿಕೃತ ತಾಣ";
            case 6: return "TTD ସରକାରୀ ସାଇଟ୍";
            default: return "TTD Official Website";
        }
    }

    public static String linkEdarshan(@Nullable Context c) {
        return "e-Darshan";
    }

    public static String linkApp(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "TTD മൊബൈൽ ആപ്പ്";
            case 2: return "TTD मोबाइल ऐप";
            case 3: return "TTD Mobile App";
            case 4: return "TTD மொபைல் ஆப்";
            case 5: return "TTD ಮೊಬೈಲ್ ಆಪ್";
            case 6: return "TTD ମୋବାଇଲ୍ ଆପ୍";
            default: return "TTD Mobile App";
        }
    }

    public static String blessingLine1(@Nullable Context c) {
        switch (lang(c)) {
            case 1: return "ശ്രീ വാരെ സേവിക്കുക, ഭക്തിയോടെ ജീവിക്കുക";
            case 2: return "श्री वार की सेवा करें, भक्ति से जिएँ";
            case 3: return "శ్రీ వారిని సేవించండి, భక్తితో జీవించండి";
            case 4: return "ஸ்ரீ வாரரை சேவியுங்கள், பக்தியுடன் வாழுங்கள்";
            case 5: return "ಶ್ರೀ ವಾರರನ್ನು ಸೇವಿಸಿ, ಭಕ್ತಿಯಿಂದ ಬದುಕಿ";
            case 6: return "ଶ୍ରୀ ବାରଙ୍କୁ ସେବା କରନ୍ତୁ, ଭକ୍ତିରେ ବଞ୍ଚନ୍ତୁ";
            default: return "Serve Sri Vari, live with devotion";
        }
    }

    public static String blessingLine2(@Nullable Context c) {
        return "|| ఓం నమో వేంకటేశాయ ||";
    }

    public static String sevaName(int index, @Nullable Context c) {
        switch (index) {
            case 0:
                switch (lang(c)) {
                    case 1: return "അർച്ചന";
                    case 2: return "अर्चना";
                    case 3: return "అర్చన";
                    case 4: return "அர்ச்சனை";
                    case 5: return "ಅರ್ಚನೆ";
                    case 6: return "ଅର୍ଚନା";
                    default: return "Archana";
                }
            case 1:
                switch (lang(c)) {
                    case 1: return "തോമല";
                    case 2: return "थोमला";
                    case 3: return "తోమాల";
                    case 4: return "தோமலை";
                    case 5: return "ತೋಮಲ";
                    case 6: return "ଥୋମଲା";
                    default: return "Thomala";
                }
            case 2:
                switch (lang(c)) {
                    case 1: return "അഭിഷേകം";
                    case 2: return "अभिषेकम्";
                    case 3: return "అభిషేకం";
                    case 4: return "அபிஷேகம்";
                    case 5: return "ಅಭಿಷೇಕ";
                    case 6: return "ଅଭିଷେକ";
                    default: return "Abhishekam";
                }
            case 3:
                switch (lang(c)) {
                    case 1: return "കല്യാണോത്സവം";
                    case 2: return "कल्याणोत्सवम्";
                    case 3: return "కల్యాణోత్సవం";
                    case 4: return "கல்யாணோத்ஸவம்";
                    case 5: return "ಕಲ್ಯಾಣೋತ್ಸವ";
                    case 6: return "କଲ୍ୟାଣୋତ୍ସବ";
                    default: return "Kalyanotsavam";
                }
            default:
                switch (lang(c)) {
                    case 1: return "ബ്രഹ്മോത്സവം";
                    case 2: return "ब्रह्मोत्सवम्";
                    case 3: return "బ్రహ్మోత్సవం";
                    case 4: return "பிரஹ்மோத்ஸவம்";
                    case 5: return "ಬ್ರಹ್ಮೋತ್ಸವ";
                    case 6: return "ବ୍ରହ୍ମୋତ୍ସବ";
                    default: return "Brahmotsavam";
                }
        }
    }

    public static String newsLine(int index, @Nullable Context c) {
        switch (index) {
            case 0:
                switch (lang(c)) {
                    case 1: return "അർജിത സേവ ടോക്കണുകൾ ഓൺലൈനിൽ റിലീസ്.";
                    case 2: return "अर्जित सेवा टोकन ऑनलाइन जारी।";
                    case 3: return "అర్జిత సేవ టోకెన్లు ఆన్‌లైన్‌లో విడుదల.";
                    case 4: return "அர்ஜித சேவை டோக்கன்கள் ஆன்லைனில் வெளியீடு.";
                    case 5: return "ಅರ್ಜಿತ ಸೇವಾ ಟೋಕನ್‌ಗಳು ಆನ್‌ಲೈನ್‌ನಲ್ಲಿ ಬಿಡುಗಡೆ.";
                    case 6: return "ଅର୍ଜିତ ସେବା ଟୋକେନ୍ ଅନଲାଇନ୍ ରିଲିଜ୍।";
                    default: return "Arjitha seva tokens released online.";
                }
            case 1:
                switch (lang(c)) {
                    case 1: return "ബ്രഹ്മോത്സവം 2026 തീയതികൾ പ്രസിദ്ധീകരിച്ചു.";
                    case 2: return "ब्रह्मोत्सव 2026 तिथियाँ प्रकाशित।";
                    case 3: return "బ్రహ్మోత్సవం 2026 తేదీలు ప్రకటన.";
                    case 4: return "பிரஹ்மோத்ஸவம் 2026 தேதிகள் வெளியீடு.";
                    case 5: return "ಬ್ರಹ್ಮೋತ್ಸವ 2026 ದಿನಾಂಕಗಳು ಪ್ರಕಟ.";
                    case 6: return "ବ୍ରହ୍ମୋତ୍ସବ 2026 ତାରିଖ ପ୍ରକାଶିତ।";
                    default: return "Brahmotsavam 2026 dates announced.";
                }
            default:
                switch (lang(c)) {
                    case 1: return "ഹുണ്ടി സംഭാവനകൾക്ക് നികുതി ഇളവ് വിവരങ്ങൾ.";
                    case 2: return "हुंडी दान पर कर छूट की जानकारी।";
                    case 3: return "హుండీ విరాళాలపై పన్ను మినహాయింపు సమాచారం.";
                    case 4: return "ஹுண்டி நன்கொடைக்கு வரி சலுகை தகவல்.";
                    case 5: return "ಹುಂಡಿ ದಾನಕ್ಕೆ ತೆರಿಗೆ ವಿನಾಯಿತಿ ಮಾಹಿತಿ.";
                    case 6: return "ହୁଣ୍ଡି ଦାନରେ କର ଛାଡ଼ ସୂଚନା।";
                    default: return "Tax exemption info for hundi donations.";
                }
        }
    }

    private static int lang(@Nullable Context context) {
        if (context == null) return 0;
        return DailyUpdatesShareLanguage.effective(context);
    }
}
