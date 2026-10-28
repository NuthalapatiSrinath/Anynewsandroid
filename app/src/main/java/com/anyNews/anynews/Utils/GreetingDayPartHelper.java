package com.anyNews.anynews.Utils;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Mirrors server {@code helpers/greetingSlotWindows.js} default IST windows so the
 * client can detect when a long-lived session crossed a slot (or into a gap) and
 * needs a forced network page-0 (same greetings post_id, different image — or
 * no greeting row in gaps).
 * <p>
 * Defaults (end exclusive): morning 00:00–11:00, afternoon 12:00–14:00,
 * evening 16:00–18:00, night 20:00–24:00; gaps → {@link #SLOT_NONE}.
 */
public final class GreetingDayPartHelper {

    private static final TimeZone IST = TimeZone.getTimeZone("Asia/Kolkata");

    public static final int SLOT_NONE = 0;
    public static final int SLOT_MORNING = 1;
    public static final int SLOT_AFTERNOON = 2;
    public static final int SLOT_EVENING = 3;
    public static final int SLOT_NIGHT = 4;

    /** Morning end / afternoon start gap begins (minutes from midnight). */
    private static final int MORNING_END_MIN = 11 * 60;
    private static final int AFTERNOON_START_MIN = 12 * 60;
    private static final int AFTERNOON_END_MIN = 14 * 60;
    private static final int EVENING_START_MIN = 16 * 60;
    private static final int EVENING_END_MIN = 18 * 60;
    private static final int NIGHT_START_MIN = 20 * 60;
    private static final int NIGHT_END_MIN = 24 * 60;

    private GreetingDayPartHelper() {
    }

    /** IST hour 0–23. */
    public static int istHourNow() {
        Calendar cal = Calendar.getInstance(IST);
        return cal.get(Calendar.HOUR_OF_DAY);
    }

    /** IST minutes from midnight 0–1439. */
    public static int istMinutesNow() {
        Calendar cal = Calendar.getInstance(IST);
        return cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
    }

    /**
     * Default IST day-parts matching server {@code greetingSlotWindows} defaults
     * (end exclusive). Gaps between windows return {@link #SLOT_NONE}.
     */
    public static int slotForIstMinutes(int mins) {
        if (mins < 0 || mins >= NIGHT_END_MIN) {
            return SLOT_NONE;
        }
        if (mins < MORNING_END_MIN) return SLOT_MORNING;
        if (mins >= AFTERNOON_START_MIN && mins < AFTERNOON_END_MIN) return SLOT_AFTERNOON;
        if (mins >= EVENING_START_MIN && mins < EVENING_END_MIN) return SLOT_EVENING;
        if (mins >= NIGHT_START_MIN) return SLOT_NIGHT;
        return SLOT_NONE;
    }

    /**
     * Hour-granularity helper (start of that IST hour). Prefer
     * {@link #slotForIstMinutes(int)} / {@link #currentSlot()} at boundaries.
     */
    public static int slotForHour(int hour) {
        if (hour < 0 || hour > 23) return SLOT_NONE;
        return slotForIstMinutes(hour * 60);
    }

    public static int currentSlot() {
        return slotForIstMinutes(istMinutesNow());
    }

    /** Stable key including calendar day so midnight resets cleanly. */
    public static String currentSlotKey() {
        Calendar cal = Calendar.getInstance(IST);
        int y = cal.get(Calendar.YEAR);
        int m = cal.get(Calendar.MONTH) + 1;
        int d = cal.get(Calendar.DAY_OF_MONTH);
        return y + "-" + m + "-" + d + "|" + currentSlot();
    }

    public static String slotLabel(int slot) {
        switch (slot) {
            case SLOT_MORNING:
                return "morning";
            case SLOT_AFTERNOON:
                return "afternoon";
            case SLOT_EVENING:
                return "evening";
            case SLOT_NIGHT:
                return "night";
            default:
                return "none";
        }
    }
}
