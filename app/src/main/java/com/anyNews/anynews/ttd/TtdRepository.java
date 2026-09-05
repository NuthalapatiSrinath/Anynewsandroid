package com.anyNews.anynews.ttd;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Loads TTD day content for the app.
 *
 * <p><b>Source status:</b> ANYNEWS has no {@code /ttd} API yet. Official TTD does
 * not publish a public JSON API. This repository serves a structured day pack
 * shaped like tirumala.org so the in-app page is useful without opening dead links.
 *
 * <p>External links are allowlisted to known-working official hosts only
 * ({@link #OFFICIAL_URL}, {@link #BOOKING_URL}). Legacy ttdevasthanams.ap.gov.in
 * 404s and must never be opened.
 */
public final class TtdRepository {

    /** Official TTD public site (schedule / flash / slotted darshan). */
    public static final String OFFICIAL_URL = "https://www.tirumala.org/";
    /** Official booking portal (replaces dead ttdevasthanams.ap.gov.in). */
    public static final String BOOKING_URL = "https://tirupatibalaji.ap.gov.in/";

    private static final Set<String> ALLOWED_HOSTS = new HashSet<>(Arrays.asList(
            "www.tirumala.org",
            "tirumala.org",
            "tirupatibalaji.ap.gov.in",
            "www.tirupatibalaji.ap.gov.in",
            "services.tirupatibalaji.ap.gov.in"
    ));

    private TtdRepository() {}

    /** True when the URL host is a known-working official TTD host. */
    public static boolean isAllowedExternalUrl(@NonNull String url) {
        try {
            String host = android.net.Uri.parse(url).getHost();
            if (host == null) return false;
            host = host.toLowerCase(Locale.US);
            return ALLOWED_HOSTS.contains(host);
        } catch (Exception e) {
            return false;
        }
    }

    @NonNull
    public static TtdModels.DayPack loadToday() {
        return loadToday(null);
    }

    @NonNull
    public static TtdModels.DayPack loadToday(@Nullable android.content.Context context) {
        Calendar cal = Calendar.getInstance();
        String dateLabel = new SimpleDateFormat("d MMM yyyy, EEEE", Locale.ENGLISH)
                .format(cal.getTime());

        String official = context != null
                ? com.anyNews.anynews.Utils.TtdConfigHelper.officialUrl(context)
                : OFFICIAL_URL;
        String booking = context != null
                ? com.anyNews.anynews.Utils.TtdConfigHelper.bookingUrl(context)
                : BOOKING_URL;
        String heroTitle = context != null
                ? com.anyNews.anynews.Utils.TtdConfigHelper.heroTitle(context)
                : "Tirumala Tirupati Devasthanams";
        String heroSub = context != null
                ? com.anyNews.anynews.Utils.TtdConfigHelper.heroSubtitle(context)
                : "Sri Venkateswara Swamy Temple · Tirumala";

        // Day schedule shaped like tirumala.org “Day Schedules” (typical weekday).
        List<TtdModels.ScheduleItem> schedule = new ArrayList<>();
        schedule.add(new TtdModels.ScheduleItem(
                "02:30 – 03:00", "Suprabhatam", "Morning wake-up seva"));
        schedule.add(new TtdModels.ScheduleItem(
                "03:30 – 04:00", "Thomala Seva", "Ekantam"));
        schedule.add(new TtdModels.ScheduleItem(
                "04:00 – 04:15", "Koluvu and Panchanga Sravanam", "Ekantam"));
        schedule.add(new TtdModels.ScheduleItem(
                "04:00 – 04:30", "First Archana / Sahasranama Archana", "Ekantam"));
        schedule.add(new TtdModels.ScheduleItem(
                "06:30 – 07:00", "First Bell, Bali and Sattumura", null));
        schedule.add(new TtdModels.ScheduleItem(
                "07:00 – 07:30", "Suddhi / Second Archana / Second Bell", "Ekantam"));
        schedule.add(new TtdModels.ScheduleItem(
                "07:30 – 19:00", "Darshanam", "General darshan window"));
        schedule.add(new TtdModels.ScheduleItem(
                "12:00 – 17:00", "Kalyanotsavam / Brahmotsavam / Unjal",
                "When scheduled for the day"));
        schedule.add(new TtdModels.ScheduleItem(
                "17:30 – 18:30", "Sahasra Deepalankarana Seva", null));
        schedule.add(new TtdModels.ScheduleItem(
                "19:00 – 20:00", "Night kainkaryams / Night Bell", "Ekantam"));
        schedule.add(new TtdModels.ScheduleItem(
                "20:00 – 00:30", "Evening / Night Darshanam", null));
        schedule.add(new TtdModels.ScheduleItem(
                "00:45", "Ekanta Seva", "Day closes"));

        List<TtdModels.ServiceItem> darshan = new ArrayList<>();
        darshan.add(new TtdModels.ServiceItem(
                "Sarva Darshan",
                "General queue darshan for all devotees (subject to day’s queue).",
                "Open"));
        darshan.add(new TtdModels.ServiceItem(
                "Special Entry Darshan",
                "Paid / special-entry corridor — book via tirupatibalaji.ap.gov.in.",
                "Book online"));
        darshan.add(new TtdModels.ServiceItem(
                "Divya Darshan",
                "For footpath / pedestrian pilgrims climbing Tirumala.",
                "Pilgrim path"));
        darshan.add(new TtdModels.ServiceItem(
                "Senior / Differently-abled Darshan",
                "Priority corridor as per TTD guidelines with valid ID.",
                "Priority"));

        List<TtdModels.ServiceItem> other = new ArrayList<>();
        other.add(new TtdModels.ServiceItem(
                "Srivari Sevas",
                "Arjitha sevas (Thomala, Archana, Kalyanotsavam, etc.) — advance booking.",
                null));
        other.add(new TtdModels.ServiceItem(
                "Accommodation",
                "TTD cottages & rooms — reserve on the official booking portal.",
                null));
        other.add(new TtdModels.ServiceItem(
                "Annadanam",
                "Free meals for pilgrims at Tirumala / Tirupati TTD centres.",
                null));
        other.add(new TtdModels.ServiceItem(
                "Laddu Prasadam",
                "Srivari laddu available at counters after darshan (limits apply).",
                null));
        other.add(new TtdModels.ServiceItem(
                "Hundi & Donations",
                "Offerings online or at temple hundi — official channels only.",
                null));

        List<String> fields = Arrays.asList(
                "templeName",
                "subtitle",
                "dateLabel",
                "greeting",
                "flashNews",
                "slottedDarshanStatus",
                "daySchedule[].timeRange",
                "daySchedule[].title",
                "daySchedule[].note",
                "darshanServices[].name",
                "darshanServices[].description",
                "darshanServices[].status",
                "otherServices[].name",
                "otherServices[].description",
                "officialUrl",
                "bookingUrl",
                "dataSource"
        );

        return new TtdModels.DayPack(
                heroTitle,
                heroSub,
                dateLabel,
                "శ్రీ వేంకటేశ్వర స్వామి వారికి నమః",
                "Advance-booking arjitha seva holders (Sahasrakalasabhishekam, "
                        + "Vishesha Pooja, Vasantotsavam) may be guided to Break Darshan "
                        + "or a refund when those sevas are deferred to once-a-year. "
                        + "Confirm live notices on tirumala.org before travel.",
                "Slotted Sarva Darshan: live slot & balance tickets are published on "
                        + "tirumala.org (first-come). Status here is informational — "
                        + "not a live ticket counter. Prefer the in-app schedule above; "
                        + "use Official / Booking only for confirmed live portals.",
                schedule,
                darshan,
                other,
                official,
                booking,
                "static_pack_v3+admin_urls (tirumala.org-shaped)",
                fields);
    }

    @NonNull
    public static String buildShareText(@NonNull TtdModels.DayPack pack) {
        StringBuilder sb = new StringBuilder();
        sb.append(pack.templeName).append('\n');
        sb.append(pack.subtitle).append('\n');
        sb.append(pack.dateLabel).append('\n');
        sb.append(pack.greeting).append("\n\n");
        sb.append("Day schedule:\n");
        int n = Math.min(6, pack.daySchedule.size());
        for (int i = 0; i < n; i++) {
            TtdModels.ScheduleItem s = pack.daySchedule.get(i);
            sb.append("• ").append(s.timeRange).append(" — ").append(s.title).append('\n');
        }
        sb.append("\nOfficial: ").append(pack.officialUrl);
        sb.append("\nBooking: ").append(pack.bookingUrl);
        sb.append("\n\n— AnyNews");
        return sb.toString().trim();
    }

    /** ISO-ish stamp for debugging UI (not shown as primary copy). */
    @NonNull
    public static String generatedAtLabel() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH).format(new Date());
    }
}
