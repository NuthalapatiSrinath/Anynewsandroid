package com.anyNews.anynews.Utils;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Mirrors server {@code helpers/greetingDayPart.js} IST windows so the client
 * can detect when a long-lived session crossed morning→afternoon (etc.) and
 * needs a fresh page-0 (same greetings post_id, different image).
 */
public final class GreetingDayPartHelper {

    private static final TimeZone IST = TimeZone.getTimeZone("Asia/Kolkata");

    public static final int SLOT_NONE = 0;
    public static final int SLOT_MORNING = 1;
    public static final int SLOT_AFTERNOON = 2;
    public static final int SLOT_EVENING = 3;
    public static final int SLOT_NIGHT = 4;

    private GreetingDayPartHelper() {
    }

    /** IST hour 0–23. */
    public static int istHourNow() {
        Calendar cal = Calendar.getInstance(IST);
        return cal.get(Calendar.HOUR_OF_DAY);
    }

    /**
     * Contiguous IST day-parts (same as server):
     * morning 4–12, afternoon 12–16, evening 16–20, night 20–24; none 0–4.
     */
    public static int slotForHour(int hour) {
        if (hour >= 4 && hour < 12) return SLOT_MORNING;
        if (hour >= 12 && hour < 16) return SLOT_AFTERNOON;
        if (hour >= 16 && hour < 20) return SLOT_EVENING;
        if (hour >= 20) return SLOT_NIGHT;
        return SLOT_NONE;
    }

    public static int currentSlot() {
        return slotForHour(istHourNow());
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
