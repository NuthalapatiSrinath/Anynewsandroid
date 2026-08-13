package com.anyNews.anynews.panchang;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * On-device Telugu panchang year generator (mirrors server {@code panchangEngine.js}).
 * <p>
 * Deterministic for (year, lat, lng, Asia/Kolkata) — no randomness.
 * <p>
 * Data source (honest, not temple-grade):
 * <ul>
 *   <li>Sunrise/sunset: NOAA-style solar equation of time + altitude formula</li>
 *   <li>Tithi / nakshatra: low-precision sun & moon ecliptic longitudes → elongation / 27-fold</li>
 *   <li>Rahu / Yama / Gulika: classical weekday day-eighth slots between sunup–sundown</li>
 *   <li>Abhijit / Amrita / Brahma / Varjya / vehicle: fixed fractional day approximations</li>
 *   <li>Festivals: fixed national dates + curated seeded movable religious dates per year</li>
 * </ul>
 * Not claimed to match every printed panchang or competitor to the minute.
 */
public final class PanchangEngine {

    /** Bump when festival seeds or day-structure rules change (invalidates disk cache). */
    public static final int ENGINE_VERSION = 3;
    public static final String DATA_SOURCE =
            "astronomical_approx+seeded_festivals_v3";
    /** Drop auspicious fragments shorter than this after trimming Rahu / Yama / Gulika. */
    private static final double MIN_SHUBHA_FRAGMENT_MIN = 5.0;

    public static final double DEFAULT_LAT = 14.6819;
    public static final double DEFAULT_LNG = 77.6006;
    public static final String DEFAULT_TZ = "Asia/Kolkata";
    private static final int TZ_OFFSET_MIN = 330;

    private static final String[] TITHI_NAMES = {
            "పాడ్యమి", "విదియ", "తదియ", "చవితి", "పంచమి", "షష్ఠి", "సప్తమి",
            "అష్టమి", "నవమి", "దశమి", "ఏకాదశి", "ద్వాదశి", "త్రయోదశి", "చతుర్దశి",
            "పూర్ణిమ"
    };

    private static final String[] NAKSHATRA_NAMES = {
            "అశ్విని", "భరణి", "కృత్తిక", "రోహిణి", "మృగశిర", "ఆర్ద్ర", "పునర్వసు",
            "పుష్యమి", "ఆశ్లేష", "మఖ", "పుబ్బ", "ఉత్తర", "హస్త", "చిత్త", "స్వాతి",
            "విశాఖ", "అనురాధ", "జ్యేష్ఠ", "మూల", "పూ.షా", "ఉ.షా", "శ్రవణం",
            "ధనిష్ఠ", "శతభిష", "పూ.భా", "ఉ.భా", "రేవతి"
    };

    private static final String[] NAKSHATRA_SHORT = {
            "అశ్వి", "భరణి", "కృత్తి", "రోహి", "మృగ", "ఆర్ద్ర", "పున",
            "పుష్య", "ఆశ్లే", "మఖ", "పుబ్బ", "ఉత్తర", "హస్త", "చిత్త", "స్వాతి",
            "విశా", "అను", "జ్యేష్ఠ", "మూల", "పూ.షా", "ఉ.షా", "శ్రవ",
            "ధని", "శత", "పూ.భా", "ఉ.భా", "రేవ"
    };

    private static final String[] SAMVATSARAM = {
            "ప్రభవ", "విభవ", "శుక్ల", "ప్రమోదూత", "ప్రజోత్పత్తి", "ఆంగీరస", "శ్రీముఖ", "భావ",
            "యువ", "ధాత", "ఈశ్వర", "బహుధాన్య", "ప్రమాధి", "విక్రమ", "వృష", "చిత్రభాను",
            "స్వభాను", "తారణ", "పార్థివ", "వ్యయ", "సర్వజిత్", "సర్వధారి", "విరోధి", "వికృతి",
            "ఖర", "నందన", "విజయ", "జయ", "మన్మథ", "దుర్ముఖి", "హేవిళంబి", "విళంబి",
            "వికారి", "శార్వరి", "ప్లవ", "శుభకృత్", "శోభకృత్", "క్రోధి", "విశ్వావసు", "పరాభవ",
            "ప్లవంగ", "కీలక", "సౌమ్య", "సాధారణ", "విరోధికృత్", "పరీధావి", "ప్రమాదీచ", "ఆనంద",
            "రాక్షస", "నల", "పింగళ", "కాళయుక్తి", "సిద్ధార్థి", "రౌద్రి", "దుర్మతి", "దుందుభి",
            "రుధిరోద్గారి", "రక్తాక్షి", "క్రోధన", "అక్షయ"
    };

    private static final String[] TELUGU_MASA = {
            "చైత్రం", "వైశాఖం", "జ్యేష్ఠం", "ఆషాఢం", "శ్రావణం", "భాద్రపదం",
            "ఆశ్వయుజం", "కార్తీకం", "మార్గశిరం", "పుష్యం", "మాఘం", "ఫాల్గుణం"
    };

    private static final String[] EN_SHORT = {
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    };
    private static final String[] EN_FULL = {
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
    };
    private static final String[] TE_MONTH = {
            "జనవరి", "ఫిబ్రవరి", "మార్చి", "ఏప్రిల్", "మే", "జూన్",
            "జూలై", "ఆగస్టు", "సెప్టెంబర్", "అక్టోబర్", "నవంబర్", "డిసెంబర్"
    };

    private static final int[] RAHU_SLOT = {7, 1, 6, 4, 5, 3, 2};
    private static final int[] YAMA_SLOT = {4, 3, 2, 1, 0, 6, 5};
    private static final int[] GULIKA_SLOT = {6, 5, 4, 3, 2, 1, 0};

    private static final double[][][] DURMU = {
            {{10.5 / 15, 11.5 / 15}},
            {{6.5 / 15, 7.5 / 15}, {12.0 / 15, 1.0}},
            {{3.0 / 15, 4.0 / 15}},
            {{7.5 / 15, 8.5 / 15}},
            {{5.5 / 15, 6.5 / 15}, {11.0 / 15, 12.0 / 15}},
            {{3.0 / 15, 4.0 / 15}, {8.0 / 15, 9.0 / 15}},
            {{1.5 / 15, 2.5 / 15}}
    };

    private PanchangEngine() {
    }

    @NonNull
    public static PanchangModels.YearPack generateYear(int year, double latIn, double lngIn) {
        int y = Math.max(2000, Math.min(2100, year));
        double lat = clamp(latIn, -90, 90);
        double lng = clamp(lngIn, -180, 180);
        Map<String, List<SeedFest>> festivalMap = buildFestivalMap(y);

        PanchangModels.YearPack pack = new PanchangModels.YearPack();
        pack.statusCode = 200;
        pack.message = "success";
        pack.year = y;
        pack.lat = lat;
        pack.lng = lng;
        pack.timezone = DEFAULT_TZ;
        pack.locationName = "Anantapur / default AP";
        pack.generatedAt = isoNow();
        pack.engineVersion = ENGINE_VERSION;
        pack.dataSource = DATA_SOURCE;
        pack.cacheKey = cacheKeyFor(y, lat, lng);
        pack.days = new ArrayList<>(366);
        pack.festivals = new ArrayList<>();
        pack.monthHeaders = new HashMap<>();

        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.set(y, Calendar.JANUARY, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        while (cal.get(Calendar.YEAR) == y) {
            int m0 = cal.get(Calendar.MONTH);
            int d = cal.get(Calendar.DAY_OF_MONTH);
            PanchangModels.DayData day = computeDay(y, m0, d, lat, lng, festivalMap);
            pack.days.add(day);
            if (d == 15 || !pack.monthHeaders.containsKey(String.valueOf(m0 + 1))) {
                pack.monthHeaders.put(String.valueOf(m0 + 1), day.teluguHeader);
            }
            if (day.festivals != null) {
                for (PanchangModels.Festival f : day.festivals) {
                    if (f == null) continue;
                    PanchangModels.Festival row = new PanchangModels.Festival();
                    row.date = day.date;
                    row.day = day.day;
                    row.month = day.month;
                    row.name = f.name;
                    row.icon = f.icon;
                    row.dateDisplayTe = day.dateDisplayTe;
                    row.dateDisplayEn = EN_FULL[m0].toUpperCase(Locale.US) + " " + d;
                    pack.festivals.add(row);
                }
            }
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
        pack.dayCount = pack.days.size();
        return pack;
    }

    private static PanchangModels.DayData computeDay(
            int y, int m0, int d, double lat, double lng,
            Map<String, List<SeedFest>> festivalMap) {
        String key = dateKey(y, m0, d);
        Calendar utc = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        utc.set(y, m0, d, 0, 0, 0);
        int weekday = utc.get(Calendar.DAY_OF_WEEK) - 1; // Calendar.SUNDAY=1 → 0

        double[] sun = sunTimesLocal(y, m0, d, lat, lng);
        double sunrise = sun[0];
        double sunset = sun[1];

        double noonHourUtc = (12 * 60 - TZ_OFFSET_MIN) / 60.0;
        double jdNoon = julianDay(y, m0 + 1, d, noonHourUtc);
        double sunLon = sunLongitude(jdNoon);
        double moonLon = moonLongitude(jdNoon);
        int tithiMid = tithiIndex(moonLon, sunLon);

        double midnightUtc = (0 - TZ_OFFSET_MIN) / 60.0;
        double jdMid = julianDay(y, m0 + 1, d, midnightUtc);
        int tithiStart = tithiIndex(moonLongitude(jdMid), sunLongitude(jdMid));
        int nakStart = ((int) Math.floor(moonLongitude(jdMid) / (360.0 / 27.0))) % 27;
        if (nakStart < 0) nakStart += 27;

        Trans tithiTrans = findTransition(y, m0, d, "tithi", tithiStart);
        Trans nakTrans = findTransition(y, m0, d, "nak", nakStart);

        String primaryTithi = tithiName(tithiStart);
        String nextTithi = tithiName(tithiTrans.nextIndex);
        String primaryNak = NAKSHATRA_NAMES[nakStart];
        String primaryNakShort = NAKSHATRA_SHORT[nakStart];

        boolean isPournami = tithiMid == 14 || tithiStart == 14 || tithiTrans.nextIndex == 14;
        boolean isAmavasya = tithiMid == 29 || tithiStart == 29 || tithiTrans.nextIndex == 29;

        double[] rahu = slotRange(sunrise, sunset, RAHU_SLOT[weekday]);
        double[] yama = slotRange(sunrise, sunset, YAMA_SLOT[weekday]);
        double[] gulika = slotRange(sunrise, sunset, GULIKA_SLOT[weekday]);
        double[] abhijit = abhijitRange(sunrise, sunset);
        double[] amrita = amritaRange(sunrise, sunset, weekday);
        double[] brahma = new double[]{sunrise - 96, sunrise - 48};
        double[] varjya = varjyaRange(sunrise, sunset, weekday);
        double[] vehicle = vehicleMuhurta(sunrise, sunset, weekday);

        List<PanchangModels.Festival> festivals = new ArrayList<>();
        List<SeedFest> seeded = festivalMap.get(key);
        if (seeded != null) {
            for (SeedFest s : seeded) {
                PanchangModels.Festival f = new PanchangModels.Festival();
                f.date = key;
                f.name = s.name;
                f.icon = s.icon != null ? s.icon : "festival";
                festivals.add(f);
            }
        }
        if (isAmavasya && !hasIcon(festivals, "amavasya")) {
            PanchangModels.Festival f = new PanchangModels.Festival();
            f.date = key;
            f.name = "అమావాస్య";
            f.icon = "amavasya";
            festivals.add(f);
        }
        if (isPournami && !hasIcon(festivals, "pournami")) {
            PanchangModels.Festival f = new PanchangModels.Festival();
            f.date = key;
            f.name = "పూర్ణిమ";
            f.icon = "pournami";
            festivals.add(f);
        }

        String sam = samvatsaramForDate(y, m0, d);
        String masa = TELUGU_MASA[masaIndexForDate(y, m0, d)];

        PanchangModels.DayData day = new PanchangModels.DayData();
        day.date = key;
        day.year = y;
        day.month = m0 + 1;
        day.day = d;
        day.weekday = weekday;
        day.cellLines = new ArrayList<>();
        day.cellLines.add(primaryTithi + " " + formatTeluguTime(tithiTrans.endMin));
        day.cellLines.add(primaryNakShort + " " + formatTeluguTime(nakTrans.endMin));

        day.tithi = new PanchangModels.Tithi();
        day.tithi.name = primaryTithi;
        day.tithi.endTime = formatTeluguTime(tithiTrans.endMin);
        day.tithi.endMinutes = (int) Math.round(tithiTrans.endMin);
        day.tithi.nextName = nextTithi;

        day.nakshatra = new PanchangModels.Nakshatra();
        day.nakshatra.name = primaryNak;
        day.nakshatra.shortName = primaryNakShort;
        day.nakshatra.endTime = formatTeluguTime(nakTrans.endMin);
        day.nakshatra.endMinutes = (int) Math.round(nakTrans.endMin);

        day.sunrise = formatTeluguTime(sunrise);
        day.sunset = formatTeluguTime(sunset);
        day.sunriseMinutes = (int) Math.round(sunrise);
        day.sunsetMinutes = (int) Math.round(sunset);
        day.isAmavasya = isAmavasya;
        day.isPournami = isPournami;
        day.festivals = festivals;
        day.samvatsaram = sam;
        day.masa = masa;
        day.teluguHeader = "శ్రీ " + sam + " నామ సంవత్సరం, " + masa + " మాసం";
        day.dateDisplayEn = EN_SHORT[m0] + " " + d + ", " + y;
        day.dateDisplayTe = TE_MONTH[m0] + " " + d;

        // Major inauspicious day-slots: trim these out of shubha so we never label
        // Rahu (etc.) time as auspicious when the raw windows overlap.
        List<double[]> majorAshubha = new ArrayList<>(3);
        majorAshubha.add(rahu);
        majorAshubha.add(yama);
        majorAshubha.add(gulika);

        day.shubha = new ArrayList<>();
        addShubhaTrimmed(day.shubha, "అభిజిత్ ముహూర్తాలు", abhijit[0], abhijit[1], majorAshubha);
        addShubhaTrimmed(day.shubha, "అమృతకాలము", amrita[0], amrita[1], majorAshubha);
        addShubhaTrimmed(day.shubha, "బ్రహ్మ ముహూర్తం", brahma[0], brahma[1], majorAshubha);
        if (vehicle != null) {
            addShubhaTrimmed(day.shubha, "వాహన కొనుగోలు", vehicle[0], vehicle[1], majorAshubha);
        }

        day.ashubha = new ArrayList<>();
        day.ashubha.add(item("రాహు", rahu[0], rahu[1]));
        day.ashubha.add(item("యమగండం", yama[0], yama[1]));
        day.ashubha.add(item("గుళిక", gulika[0], gulika[1]));
        double[][] durs = DURMU[weekday];
        for (double[] dr : durs) {
            double dayLen = sunset - sunrise;
            day.ashubha.add(item("దుర్ముహూర్తం", sunrise + dayLen * dr[0], sunrise + dayLen * dr[1]));
        }
        day.ashubha.add(item("వర్జ్యం", varjya[0], varjya[1]));

        day.tithiCard = new ArrayList<>();
        day.tithiCard.add(item(primaryTithi, 0, tithiTrans.endMin));
        day.tithiCard.add(item(nextTithi, tithiTrans.endMin, tithiTrans.endMin + 1440.0 / 30.0));

        return day;
    }

    private static PanchangModels.TimeItem item(String label, double a, double b) {
        return new PanchangModels.TimeItem(label, formatRange(a, b));
    }

    /**
     * Subtract major inauspicious intervals from an auspicious window and append
     * remaining fragment(s). Splits when Rahu cuts through the middle.
     */
    private static void addShubhaTrimmed(
            List<PanchangModels.TimeItem> out,
            String label,
            double start,
            double end,
            List<double[]> blockers) {
        List<double[]> segs = new ArrayList<>();
        segs.add(new double[]{start, end});
        if (blockers != null) {
            for (double[] bad : blockers) {
                if (bad == null || bad.length < 2) continue;
                segs = subtractIntervalList(segs, bad[0], bad[1]);
            }
        }
        for (double[] seg : segs) {
            if (seg[1] - seg[0] >= MIN_SHUBHA_FRAGMENT_MIN) {
                out.add(item(label, seg[0], seg[1]));
            }
        }
    }

    @NonNull
    private static List<double[]> subtractIntervalList(
            List<double[]> segs, double badStart, double badEnd) {
        if (badEnd <= badStart) return segs;
        List<double[]> next = new ArrayList<>();
        for (double[] seg : segs) {
            double a = seg[0];
            double b = seg[1];
            if (b <= badStart || a >= badEnd) {
                next.add(seg);
                continue;
            }
            if (a < badStart) {
                next.add(new double[]{a, Math.min(b, badStart)});
            }
            if (b > badEnd) {
                next.add(new double[]{Math.max(a, badEnd), b});
            }
        }
        return next;
    }

    private static boolean hasIcon(List<PanchangModels.Festival> list, String icon) {
        for (PanchangModels.Festival f : list) {
            if (f != null && icon.equals(f.icon)) return true;
        }
        return false;
    }

    private static class Trans {
        double endMin;
        int nextIndex;
    }

    private static Trans findTransition(int y, int m0, int d, String kind, int startIndex) {
        Trans t = new Trans();
        t.endMin = 24 * 60 - 1;
        t.nextIndex = startIndex;
        int prev = startIndex;
        for (int min = 15; min < 24 * 60; min += 15) {
            double hourUtc = (min - TZ_OFFSET_MIN) / 60.0;
            double jd = julianDay(y, m0 + 1, d, hourUtc);
            int idx;
            if ("tithi".equals(kind)) {
                idx = tithiIndex(moonLongitude(jd), sunLongitude(jd));
            } else {
                idx = ((int) Math.floor(moonLongitude(jd) / (360.0 / 27.0))) % 27;
                if (idx < 0) idx += 27;
            }
            if (idx != prev) {
                t.endMin = min;
                t.nextIndex = idx;
                return t;
            }
            prev = idx;
        }
        return t;
    }

    private static String tithiName(int index0) {
        int i = ((index0 % 30) + 30) % 30;
        if (i == 14) return "పూర్ణిమ";
        if (i == 29) return "అమావాస్య";
        if (i < 15) return TITHI_NAMES[i];
        return TITHI_NAMES[i - 15];
    }

    private static int tithiIndex(double moonLon, double sunLon) {
        double diff = norm360(moonLon - sunLon);
        return (int) Math.floor(diff / 12.0);
    }

    private static double[] slotRange(double sunrise, double sunset, int slotIndex) {
        double dayLen = Math.max(1, sunset - sunrise);
        double part = dayLen / 8.0;
        double start = sunrise + slotIndex * part;
        return new double[]{start, start + part};
    }

    private static double[] abhijitRange(double sunrise, double sunset) {
        double noon = (sunrise + sunset) / 2.0;
        double muhurta = Math.max(24, (sunset - sunrise) / 15.0);
        return new double[]{noon - muhurta / 2, noon + muhurta / 2};
    }

    private static double[] amritaRange(double sunrise, double sunset, int weekday) {
        double dayLen = sunset - sunrise;
        double[][] offsets = {
                {0.55, 0.68}, {0.18, 0.30}, {0.72, 0.85}, {0.40, 0.52},
                {0.48, 0.60}, {0.25, 0.38}, {0.62, 0.75}
        };
        double[] o = offsets[weekday % 7];
        return new double[]{sunrise + dayLen * o[0], sunrise + dayLen * o[1]};
    }

    private static double[] varjyaRange(double sunrise, double sunset, int weekday) {
        double dayLen = sunset - sunrise;
        double[][] offsets = {
                {0.35, 0.45}, {0.60, 0.70}, {0.22, 0.32}, {0.75, 0.88},
                {0.10, 0.20}, {0.50, 0.60}, {0.42, 0.52}
        };
        double[] o = offsets[weekday % 7];
        return new double[]{sunrise + dayLen * o[0], sunrise + dayLen * o[1]};
    }

    @Nullable
    private static double[] vehicleMuhurta(double sunrise, double sunset, int weekday) {
        if (weekday != 3 && weekday != 5) return null;
        double dayLen = sunset - sunrise;
        return new double[]{sunrise + dayLen * 0.42, sunrise + dayLen * 0.58};
    }

    private static String samvatsaramForDate(int y, int m0, int d) {
        int cycleYear = y;
        if (m0 < 2 || (m0 == 2 && d < 20)) cycleYear = y - 1;
        int idx = ((cycleYear - 1987) % 60 + 60) % 60;
        return SAMVATSARAM[idx];
    }

    private static int masaIndexForDate(int y, int m0, int d) {
        int days = dayOfYear(y, m0, d);
        int chaitraStart = dayOfYear(y, 2, 14);
        int offset = days - chaitraStart;
        if (offset < 0) {
            offset += (isLeap(y) ? 366 : 365);
        }
        return (int) Math.floor(offset / 30.5) % 12;
    }

    private static boolean isLeap(int y) {
        return (y % 4 == 0 && y % 100 != 0) || (y % 400 == 0);
    }

    private static double[] sunTimesLocal(int y, int m0, int d, double lat, double lng) {
        int N = dayOfYear(y, m0, d);
        double jd0 = julianDay(y, m0 + 1, d, 0);
        double declR = sunDeclination(jd0 + 0.25);
        double declS = sunDeclination(jd0 + 0.75);
        double latR = Math.toRadians(lat);
        double cosHA = (Math.cos(Math.toRadians(90.833)) / (Math.cos(latR) * Math.cos(declR)))
                - Math.tan(latR) * Math.tan(declR);
        double cosHAS = (Math.cos(Math.toRadians(90.833)) / (Math.cos(latR) * Math.cos(declS)))
                - Math.tan(latR) * Math.tan(declS);
        cosHA = clamp(cosHA, -1, 1);
        cosHAS = clamp(cosHAS, -1, 1);
        double haR = Math.toDegrees(Math.acos(cosHA));
        double haS = Math.toDegrees(Math.acos(cosHAS));
        double eotR = equationOfTimeMinutes(jd0 + 0.25);
        double eotS = equationOfTimeMinutes(jd0 + 0.75);
        double riseUtc = 720 - 4 * (lng + haR) - eotR;
        double setUtc = 720 - 4 * (lng - haS) - eotS;
        double rise = riseUtc + TZ_OFFSET_MIN;
        double set = setUtc + TZ_OFFSET_MIN;
        while (rise < 0) rise += 1440;
        while (set < 0) set += 1440;
        while (rise >= 1440) rise -= 1440;
        while (set >= 1440) set -= 1440;
        if (set <= rise) set = rise + 12 * 60;
        return new double[]{rise, set};
    }

    private static int dayOfYear(int y, int m0, int d) {
        Calendar c = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        c.set(y, m0, d);
        return c.get(Calendar.DAY_OF_YEAR);
    }

    private static double julianDay(int y, int m, int d, double hourUtc) {
        int year = y;
        int month = m;
        if (month <= 2) {
            year -= 1;
            month += 12;
        }
        int A = year / 100;
        int B = 2 - A + A / 4;
        return Math.floor(365.25 * (year + 4716))
                + Math.floor(30.6001 * (month + 1))
                + d + B - 1524.5 + hourUtc / 24.0;
    }

    private static double norm360(double x) {
        double v = x % 360.0;
        if (v < 0) v += 360.0;
        return v;
    }

    private static double sunLongitude(double jd) {
        double T = (jd - 2451545.0) / 36525.0;
        double L0 = norm360(280.46646 + 36000.76983 * T + 0.0003032 * T * T);
        double M = Math.toRadians(357.52911 + 35999.05029 * T - 0.0001537 * T * T);
        double C = (1.914602 - 0.004817 * T - 0.000014 * T * T) * Math.sin(M)
                + (0.019993 - 0.000101 * T) * Math.sin(2 * M)
                + 0.000289 * Math.sin(3 * M);
        return norm360(L0 + C);
    }

    private static double moonLongitude(double jd) {
        double T = (jd - 2451545.0) / 36525.0;
        double Lp = norm360(218.3164477 + 481267.88123421 * T - 0.0015786 * T * T);
        double D = Math.toRadians(297.8501921 + 445267.1114034 * T - 0.0018819 * T * T);
        double M = Math.toRadians(357.5291092 + 35999.0502909 * T - 0.0001536 * T * T);
        double Mp = Math.toRadians(134.9633964 + 477198.8675055 * T + 0.0087414 * T * T);
        double F = Math.toRadians(93.272095 + 483202.0175233 * T - 0.0036539 * T * T);
        double lon = Lp
                + 6.289 * Math.sin(Mp)
                + 1.274 * Math.sin(2 * D - Mp)
                + 0.658 * Math.sin(2 * D)
                + 0.214 * Math.sin(2 * Mp)
                - 0.186 * Math.sin(M)
                - 0.114 * Math.sin(2 * F);
        return norm360(lon);
    }

    private static double sunDeclination(double jd) {
        double L = Math.toRadians(sunLongitude(jd));
        double eps = Math.toRadians(23.439);
        return Math.asin(Math.sin(eps) * Math.sin(L));
    }

    private static double equationOfTimeMinutes(double jd) {
        double T = (jd - 2451545.0) / 36525.0;
        double M = Math.toRadians(357.52911 + 35999.05029 * T);
        double L0 = Math.toRadians(280.46646 + 36000.76983 * T);
        double e = 0.016708634 - 0.000042037 * T;
        double y = Math.tan(Math.toRadians(23.4393) / 2.0);
        double y2 = y * y;
        double E = y2 * Math.sin(2 * L0) - 2 * e * Math.sin(M)
                + 4 * e * y2 * Math.sin(M) * Math.cos(2 * L0)
                - 0.5 * y2 * y2 * Math.sin(4 * L0)
                - 1.25 * e * e * Math.sin(2 * M);
        return 4 * Math.toDegrees(E);
    }

    static String formatTeluguTime(double minutes) {
        int m = ((int) Math.round(minutes)) % (24 * 60);
        if (m < 0) m += 24 * 60;
        int h24 = m / 60;
        int mm = m % 60;
        String period;
        int h12;
        if (h24 == 0) {
            period = "రా.";
            h12 = 12;
        } else if (h24 < 5) {
            period = "రా.";
            h12 = h24;
        } else if (h24 < 12) {
            period = "ఉ.";
            h12 = h24;
        } else if (h24 == 12) {
            period = "మ.";
            h12 = 12;
        } else if (h24 < 16) {
            period = "మ.";
            h12 = h24 - 12;
        } else if (h24 < 20) {
            period = "సా.";
            h12 = h24 - 12;
        } else {
            period = "రా.";
            h12 = h24 - 12;
        }
        return period + " " + h12 + "." + String.format(Locale.US, "%02d", mm);
    }

    private static String formatRange(double startMin, double endMin) {
        return formatTeluguTime(startMin) + " నుండి " + formatTeluguTime(endMin) + " వరకు";
    }

    private static String dateKey(int y, int m0, int d) {
        return String.format(Locale.US, "%04d-%02d-%02d", y, m0 + 1, d);
    }

    @NonNull
    public static String cacheKeyFor(int year, double lat, double lng) {
        return String.format(Locale.US, "panchang_v%d_%d_%.2f_%.2f_%s",
                ENGINE_VERSION, year, lat, lng, DEFAULT_TZ);
    }

    private static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    private static String isoNow() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                .format(new java.util.Date());
    }

    private static final class SeedFest {
        final String name;
        final String icon;

        SeedFest(String name, String icon) {
            this.name = name;
            this.icon = icon;
        }
    }

    private static Map<String, List<SeedFest>> buildFestivalMap(int year) {
        Map<String, List<SeedFest>> map = new HashMap<>();
        addFixed(map, year, 1, 1, "నూతన సంవత్సరాది", "newyear");
        addFixed(map, year, 1, 14, "మకర సంక్రాంతి", "sankranti");
        addFixed(map, year, 1, 15, "కనుమ", "sankranti");
        addFixed(map, year, 1, 26, "గణతంత్ర దినోత్సవం", "flag");
        addFixed(map, year, 3, 8, "అంతర్జాతీయ మహిళా దినోత్సవం", "festival");
        addFixed(map, year, 5, 1, "కార్మిక దినోత్సవం", "festival");
        addFixed(map, year, 8, 15, "స్వాతంత్ర్య దినోత్సవం", "flag");
        addFixed(map, year, 10, 2, "గాంధీ జయంతి", "festival");
        addFixed(map, year, 11, 1, "ఆంధ్రప్రదేశ్ నిర్మాణ దినోత్సవం", "festival");
        addFixed(map, year, 12, 25, "క్రిస్మస్", "festival");

        String[][] movable = movableSeeds(year);
        if (movable != null) {
            for (String[] row : movable) {
                addKey(map, row[0], row[1], row[2]);
            }
        }
        return map;
    }

    @Nullable
    private static String[][] movableSeeds(int year) {
        if (year == 2025) {
            return new String[][]{
                    {"2025-02-26", "మహా శివరాత్రి", "amavasya"},
                    {"2025-03-14", "హోలీ", "festival"},
                    {"2025-03-30", "ఉగాది", "festival"},
                    {"2025-04-06", "శ్రీ రామ నవమి", "festival"},
                    {"2025-08-09", "రక్షా బంధన్", "rakhi"},
                    {"2025-08-16", "శ్రీకృష్ణ జన్మాష్టమి", "festival"},
                    {"2025-08-27", "గణేష్ చతుర్థి", "festival"},
                    {"2025-09-05", "మిలాద్ ఉన్-నబి", "eid"},
                    {"2025-10-02", "దసరా", "festival"},
                    {"2025-10-20", "దీపావళి", "festival"},
            };
        }
        if (year == 2026) {
            return new String[][]{
                    {"2026-01-14", "భోగి", "sankranti"},
                    {"2026-02-15", "మహా శివరాత్రి", "amavasya"},
                    {"2026-03-03", "హోలీ", "festival"},
                    {"2026-03-19", "ఉగాది", "festival"},
                    {"2026-03-26", "శ్రీ రామ నవమి", "festival"},
                    {"2026-08-21", "వరలక్ష్మీ వ్రతం", "festival"},
                    {"2026-08-26", "మిలాద్ ఉన్-నబి", "eid"},
                    {"2026-08-28", "రక్షా బంధన్", "rakhi"},
                    {"2026-09-04", "శ్రీకృష్ణ జన్మాష్టమి", "festival"},
                    {"2026-09-14", "గణేష్ చతుర్థి", "festival"},
                    {"2026-09-25", "వినాయక నిమజ్జనం", "festival"},
                    {"2026-10-20", "దసరా", "festival"},
                    {"2026-11-08", "దీపావళి", "festival"},
            };
        }
        if (year == 2027) {
            return new String[][]{
                    {"2027-03-06", "మహా శివరాత్రి", "amavasya"},
                    {"2027-03-22", "హోలీ", "festival"},
                    {"2027-04-07", "ఉగాది", "festival"},
                    {"2027-04-15", "శ్రీ రామ నవమి", "festival"},
                    {"2027-08-17", "రక్షా బంధన్", "rakhi"},
                    {"2027-08-25", "శ్రీకృష్ణ జన్మాష్టమి", "festival"},
                    {"2027-09-04", "గణేష్ చతుర్థి", "festival"},
                    {"2027-10-09", "దసరా", "festival"},
                    {"2027-10-29", "దీపావళి", "festival"},
            };
        }
        return null;
    }

    private static void addFixed(
            Map<String, List<SeedFest>> map, int year, int month1, int day,
            String name, String icon) {
        addKey(map, dateKey(year, month1 - 1, day), name, icon);
    }

    private static void addKey(Map<String, List<SeedFest>> map, String key, String name, String icon) {
        List<SeedFest> list = map.get(key);
        if (list == null) {
            list = new ArrayList<>();
            map.put(key, list);
        }
        for (SeedFest s : list) {
            if (s.name.equals(name)) return;
        }
        list.add(new SeedFest(name, icon));
    }
}
