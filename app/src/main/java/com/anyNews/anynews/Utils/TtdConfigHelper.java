package com.anyNews.anynews.Utils;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Splash-delivered TTD (Tirumala) feature flags + URLs.
 * When {@link #isEnabled} is false, Calendar|TTD chrome must stay hidden.
 */
public final class TtdConfigHelper {

    public static final String DEFAULT_OFFICIAL = "https://www.tirumala.org/";
    public static final String DEFAULT_BOOKING = "https://tirupatibalaji.ap.gov.in/";
    public static final String DEFAULT_EDARSHAN = "https://tirupatibalaji.ap.gov.in/";
    public static final String DEFAULT_APP = "https://www.tirumala.org/";

    private static final TimeZone IST = TimeZone.getTimeZone("Asia/Kolkata");

    private TtdConfigHelper() {}

    /**
     * Persist splash {@code ttd} object. Null leaves cache untouched.
     * Accepts Map/JSONObject-like via toString JSON or Gson LinkedTreeMap.
     */
    public static void saveFromSplash(@Nullable Context context, @Nullable Object raw) {
        if (context == null || raw == null) return;
        try {
            JSONObject o;
            if (raw instanceof JSONObject) {
                o = (JSONObject) raw;
            } else if (raw instanceof String) {
                String s = ((String) raw).trim();
                if (s.isEmpty() || "null".equalsIgnoreCase(s)) return;
                o = new JSONObject(s);
            } else {
                // Gson LinkedTreeMap / Map → JSON via toString is unreliable; use JSONObject wrap.
                o = new JSONObject(String.valueOf(
                        new com.google.gson.Gson().toJson(raw)));
            }
            Context app = context.getApplicationContext();
            putBool(app, Constants.TTD_ENABLED, o.optBoolean("enabled", true));
            putBool(app, Constants.TTD_BOOKING_ENABLED, o.optBoolean("booking_enabled", true));
            putBool(app, Constants.TTD_SHOW_QUICK, o.optBoolean("show_quick_services", true));
            putBool(app, Constants.TTD_SHOW_DARSHAN, o.optBoolean("show_darshan_info", true));
            putBool(app, Constants.TTD_SHOW_SEVAS, o.optBoolean("show_sevas", true));
            putBool(app, Constants.TTD_SHOW_NEWS, o.optBoolean("show_news", true));
            putBool(app, Constants.TTD_SHOW_LINKS, o.optBoolean("show_links", true));
            putUrl(app, Constants.TTD_OFFICIAL_URL, o.optString("official_url"), DEFAULT_OFFICIAL);
            putUrl(app, Constants.TTD_BOOKING_URL, o.optString("booking_url"), DEFAULT_BOOKING);
            putUrl(app, Constants.TTD_EDARSHAN_URL, o.optString("edarshan_url"), DEFAULT_EDARSHAN);
            putUrl(app, Constants.TTD_APP_URL, o.optString("app_url"), DEFAULT_APP);
            String title = o.optString("hero_title", "").trim();
            SharedPrefsData.putString(app, Constants.TTD_HERO_TITLE,
                    title.isEmpty() ? "Tirumala Tirupati Devasthanams" : title,
                    Constants.PREF_NAME);
            String sub = o.optString("hero_subtitle", "").trim();
            SharedPrefsData.putString(app, Constants.TTD_HERO_SUBTITLE,
                    sub.isEmpty() ? "శ్రీ వేంకటేశ్వర స్వామి వారి సేవలో" : sub,
                    Constants.PREF_NAME);
            String bless = o.optString("blessing_text", "").trim();
            SharedPrefsData.putString(app, Constants.TTD_BLESSING,
                    bless.isEmpty() ? "|| ఓం నమో వేంకటేశాయ ||" : bless,
                    Constants.PREF_NAME);
            JSONArray days = o.optJSONArray("booking_days_of_month");
            StringBuilder sb = new StringBuilder();
            if (days != null) {
                for (int i = 0; i < days.length(); i++) {
                    int d = days.optInt(i, -1);
                    if (d < 1 || d > 31) continue;
                    if (sb.length() > 0) sb.append(',');
                    sb.append(d);
                }
            }
            SharedPrefsData.putString(app, Constants.TTD_BOOKING_DAYS, sb.toString(),
                    Constants.PREF_NAME);
            SharedPrefsData.putString(app, Constants.TTD_BOOKING_OPENS_AT,
                    normalizeScheduleRaw(o.optString("booking_opens_at", "")),
                    Constants.PREF_NAME);
            SharedPrefsData.putString(app, Constants.TTD_BOOKING_CLOSES_AT,
                    normalizeScheduleRaw(o.optString("booking_closes_at", "")),
                    Constants.PREF_NAME);
            SharedPrefsData.putBool(app, Constants.TTD_CONFIG_SET, true, Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
    }

    public static boolean isEnabled(@Nullable Context context) {
        // Default ON until splash says otherwise (matches server default).
        return readBool(context, Constants.TTD_ENABLED, true);
    }

    /**
     * Booking CTA visibility: master flag + optional IST schedule window + optional days-of-month.
     * Empty schedule + empty days = show whenever {@code booking_enabled}.
     */
    public static boolean isBookingCtaVisibleToday(@Nullable Context context) {
        if (!isEnabled(context)) return false;
        if (!readBool(context, Constants.TTD_BOOKING_ENABLED, true)) return false;

        long now = System.currentTimeMillis();
        Long opens = parseScheduleMillis(bookingOpensAt(context));
        if (opens != null && now < opens) return false;
        Long closes = parseScheduleMillis(bookingClosesAt(context));
        if (closes != null && now > closes) return false;

        List<Integer> days = bookingDays(context);
        if (days.isEmpty()) return true;
        Calendar cal = Calendar.getInstance(IST);
        int today = cal.get(Calendar.DAY_OF_MONTH);
        return days.contains(today);
    }

    @NonNull
    public static String bookingOpensAt(@Nullable Context context) {
        return readStr(context, Constants.TTD_BOOKING_OPENS_AT, "");
    }

    @NonNull
    public static String bookingClosesAt(@Nullable Context context) {
        return readStr(context, Constants.TTD_BOOKING_CLOSES_AT, "");
    }

    @NonNull
    public static List<Integer> bookingDays(@Nullable Context context) {
        List<Integer> out = new ArrayList<>();
        if (context == null) return out;
        try {
            String raw = SharedPrefsData.getString(
                    context.getApplicationContext(),
                    Constants.TTD_BOOKING_DAYS,
                    Constants.PREF_NAME);
            if (TextUtils.isEmpty(raw)) return out;
            for (String part : raw.split("[,\\s]+")) {
                try {
                    int d = Integer.parseInt(part.trim());
                    if (d >= 1 && d <= 31 && !out.contains(d)) out.add(d);
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
        }
        return out;
    }

    public static boolean showQuick(@Nullable Context c) {
        return readBool(c, Constants.TTD_SHOW_QUICK, true);
    }

    public static boolean showDarshan(@Nullable Context c) {
        return readBool(c, Constants.TTD_SHOW_DARSHAN, true);
    }

    public static boolean showSevas(@Nullable Context c) {
        return readBool(c, Constants.TTD_SHOW_SEVAS, true);
    }

    public static boolean showNews(@Nullable Context c) {
        return readBool(c, Constants.TTD_SHOW_NEWS, true);
    }

    public static boolean showLinks(@Nullable Context c) {
        return readBool(c, Constants.TTD_SHOW_LINKS, true);
    }

    @NonNull
    public static String officialUrl(@Nullable Context c) {
        return readUrl(c, Constants.TTD_OFFICIAL_URL, DEFAULT_OFFICIAL);
    }

    @NonNull
    public static String bookingUrl(@Nullable Context c) {
        return readUrl(c, Constants.TTD_BOOKING_URL, DEFAULT_BOOKING);
    }

    @NonNull
    public static String edarshanUrl(@Nullable Context c) {
        return readUrl(c, Constants.TTD_EDARSHAN_URL, DEFAULT_EDARSHAN);
    }

    @NonNull
    public static String appUrl(@Nullable Context c) {
        return readUrl(c, Constants.TTD_APP_URL, DEFAULT_APP);
    }

    @NonNull
    public static String heroTitle(@Nullable Context c) {
        return readStr(c, Constants.TTD_HERO_TITLE, "Tirumala Tirupati Devasthanams");
    }

    @NonNull
    public static String heroSubtitle(@Nullable Context c) {
        return readStr(c, Constants.TTD_HERO_SUBTITLE, "శ్రీ వేంకటేశ్వర స్వామి వారి సేవలో");
    }

    @NonNull
    public static String blessing(@Nullable Context c) {
        return readStr(c, Constants.TTD_BLESSING, "|| ఓం నమో వేంకటేశాయ ||");
    }

    /**
     * Short outside bullets for Daily Updates TTD card (no district).
     * Honors section flags; always returns 2–3 honest lines from config.
     */
    @NonNull
    public static String outsideCardBullets(@Nullable Context c) {
        List<String> lines = new ArrayList<>(4);
        if (showDarshan(c)) {
            lines.add("• " + TtdLabels.darshanHeading(c));
        }
        if (showSevas(c)) {
            lines.add("• " + TtdLabels.sevasHeading(c));
        }
        if (readBool(c, Constants.TTD_BOOKING_ENABLED, true)) {
            lines.add("• " + TtdLabels.quickDarshanSub(c));
        }
        if (showQuick(c) && lines.size() < 3) {
            lines.add("• " + TtdLabels.quickRoom(c));
        }
        if (showNews(c) && lines.size() < 3) {
            lines.add("• " + TtdLabels.newsHeading(c));
        }
        // Disabled sections still need 2–3 honest lines from config defaults.
        if (lines.size() < 2) {
            String[] fallback = ttdFallbackBullets(c);
            for (String fb : fallback) {
                if (lines.size() >= 3) break;
                if (!lines.contains(fb)) lines.add(fb);
            }
        }
        while (lines.size() > 3) {
            lines.remove(lines.size() - 1);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) sb.append('\n');
            sb.append(lines.get(i));
        }
        return sb.toString();
    }

    @NonNull
    private static String[] ttdFallbackBullets(@Nullable Context c) {
        return new String[]{
                "• " + TtdLabels.darshanHeading(c),
                "• " + TtdLabels.sevasHeading(c),
                "• " + TtdLabels.quickDarshanSub(c),
        };
    }

    /** Persist raw schedule string; empty clears. Does not reformat — server already normalizes. */
    @NonNull
    private static String normalizeScheduleRaw(@Nullable String raw) {
        if (raw == null) return "";
        return raw.trim();
    }

    /**
     * Parse schedule datetime. Bare local times (no zone) are treated as Asia/Kolkata (IST).
     * @return epoch millis, or null if empty/unparseable
     */
    @Nullable
    static Long parseScheduleMillis(@Nullable String raw) {
        if (raw == null) return null;
        String s = raw.trim();
        if (s.isEmpty()) return null;
        // ISO with Z or numeric offset
        if (s.endsWith("Z") || s.endsWith("z")
                || s.matches(".*[+-]\\d{2}:?\\d{2}$")) {
            try {
                // SimpleDateFormat ISO-8601 with offset
                String norm = s;
                if (norm.endsWith("Z") || norm.endsWith("z")) {
                    SimpleDateFormat sdf = new SimpleDateFormat(
                            "yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
                    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                    // try with millis
                    try {
                        return sdf.parse(norm.replaceAll("\\.\\d+", "")).getTime();
                    } catch (ParseException e) {
                        SimpleDateFormat sdf2 = new SimpleDateFormat(
                                "yyyy-MM-dd'T'HH:mm'Z'", Locale.US);
                        sdf2.setTimeZone(TimeZone.getTimeZone("UTC"));
                        return sdf2.parse(norm).getTime();
                    }
                }
                // +05:30 or +0530
                String[] patterns = {
                        "yyyy-MM-dd'T'HH:mm:ssXXX",
                        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                        "yyyy-MM-dd'T'HH:mmXXX",
                        "yyyy-MM-dd'T'HH:mm:ssZ",
                        "yyyy-MM-dd'T'HH:mmZ",
                };
                for (String p : patterns) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat(p, Locale.US);
                        Date d = sdf.parse(norm);
                        if (d != null) return d.getTime();
                    } catch (Exception ignored) {
                    }
                }
            } catch (Exception ignored) {
            }
        }
        // Bare local → IST
        String[] localPatterns = {
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd HH:mm",
        };
        for (String p : localPatterns) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(p, Locale.US);
                sdf.setTimeZone(IST);
                Date d = sdf.parse(s);
                if (d != null) return d.getTime();
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private static void putBool(Context app, String key, boolean v) {
        SharedPrefsData.putString(app, key, v ? "true" : "false", Constants.PREF_NAME);
    }

    private static void putUrl(Context app, String key, String raw, String fallback) {
        String s = raw != null ? raw.trim() : "";
        if (s.isEmpty() || (!s.startsWith("http://") && !s.startsWith("https://"))) {
            s = fallback;
        }
        SharedPrefsData.putString(app, key, s, Constants.PREF_NAME);
    }

    private static boolean readBool(@Nullable Context context, String key, boolean def) {
        if (context == null) return def;
        try {
            String v = SharedPrefsData.getString(
                    context.getApplicationContext(), key, Constants.PREF_NAME);
            if (TextUtils.isEmpty(v)) return def;
            return "true".equalsIgnoreCase(v) || "1".equals(v);
        } catch (Exception ignored) {
            return def;
        }
    }

    @NonNull
    private static String readUrl(@Nullable Context context, String key, String fallback) {
        String s = readStr(context, key, fallback);
        if (!s.startsWith("http://") && !s.startsWith("https://")) return fallback;
        return s;
    }

    @NonNull
    private static String readStr(@Nullable Context context, String key, String fallback) {
        if (context == null) return fallback;
        try {
            String v = SharedPrefsData.getString(
                    context.getApplicationContext(), key, Constants.PREF_NAME);
            if (TextUtils.isEmpty(v)) return fallback;
            return v;
        } catch (Exception ignored) {
            return fallback;
        }
    }
}
