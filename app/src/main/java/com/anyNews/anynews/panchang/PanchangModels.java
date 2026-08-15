package com.anyNews.anynews.panchang;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Models for Telugu panchang year packs (server JSON + on-device cache).
 */
public final class PanchangModels {

    private PanchangModels() {
    }

    public static final class TimeItem {
        public String label;
        public String range;

        public TimeItem() {
        }

        public TimeItem(String label, String range) {
            this.label = label;
            this.range = range;
        }
    }

    public static final class Festival {
        public String date;
        public int day;
        public int month;
        public String name;
        public String icon;
        public String dateDisplayTe;
        public String dateDisplayEn;

        public Festival() {
        }
    }

    public static final class DayData {
        public String date; // yyyy-MM-dd
        public int year;
        public int month; // 1-12
        public int day;
        public int weekday; // 0=Sun
        public List<String> cellLines;
        public Tithi tithi;
        public Nakshatra nakshatra;
        public String sunrise;
        public String sunset;
        public int sunriseMinutes;
        public int sunsetMinutes;
        public boolean isAmavasya;
        public boolean isPournami;
        public List<Festival> festivals;
        public String teluguHeader;
        public String samvatsaram;
        public String masa;
        public List<TimeItem> shubha;
        public List<TimeItem> ashubha;
        public List<TimeItem> tithiCard;
        public String dateDisplayEn;
        public String dateDisplayTe;

        @NonNull
        public List<String> safeCellLines() {
            return cellLines != null ? cellLines : new ArrayList<>();
        }

        @NonNull
        public List<Festival> safeFestivals() {
            return festivals != null ? festivals : new ArrayList<>();
        }

        @NonNull
        public List<TimeItem> safeShubha() {
            return shubha != null ? shubha : new ArrayList<>();
        }

        @NonNull
        public List<TimeItem> safeAshubha() {
            return ashubha != null ? ashubha : new ArrayList<>();
        }

        @NonNull
        public List<TimeItem> safeTithiCard() {
            return tithiCard != null ? tithiCard : new ArrayList<>();
        }

        /**
         * Icon for the grid cell. Prefer cultural festival icons over pure lunar markers,
         * then amavasya/pournami, then any remaining festival icon. Never random.
         */
        @Nullable
        public String primaryIcon() {
            String cultural = firstFestivalIcon(
                    "flag", "eid", "rakhi", "sankranti", "festival", "newyear", "star");
            if (cultural != null) return cultural;
            if (isAmavasya) return "amavasya";
            if (isPournami) return "pournami";
            String lunarFest = firstFestivalIcon("amavasya", "pournami");
            if (lunarFest != null) return lunarFest;
            if (festivals != null) {
                for (Festival f : festivals) {
                    if (f == null) continue;
                    String ic = normalizeIcon(f.icon);
                    if (ic != null) return ic;
                    if (f.name != null && !f.name.isEmpty()) return "festival";
                }
            }
            return null;
        }

        @Nullable
        private String firstFestivalIcon(String... preferred) {
            if (festivals == null || preferred == null) return null;
            for (String want : preferred) {
                for (Festival f : festivals) {
                    if (f == null) continue;
                    String ic = normalizeIcon(f.icon);
                    if (want.equals(ic)) return ic;
                }
            }
            return null;
        }

        @Nullable
        private static String normalizeIcon(@Nullable String icon) {
            if (icon == null) return null;
            String t = icon.trim().toLowerCase();
            return t.isEmpty() ? null : t;
        }

        public boolean hasFestivalHighlight() {
            if (primaryIcon() != null) return true;
            if (isAmavasya || isPournami) return true;
            if (festivals == null) return false;
            for (Festival f : festivals) {
                if (f == null) continue;
                if (f.name != null && !f.name.isEmpty()) return true;
            }
            return false;
        }
    }

    public static final class Tithi {
        public String name;
        public String endTime;
        public int endMinutes;
        public String nextName;
    }

    public static final class Nakshatra {
        public String name;
        public String shortName;
        public String endTime;
        public int endMinutes;
    }

    public static final class YearPack {
        public int statusCode;
        public String message;
        public int year;
        public double lat;
        public double lng;
        public String timezone;
        public String locationName;
        public String generatedAt;
        public String cacheKey;
        /** Bumps when festival seeds / icon rules change so devices regenerate disk cache. */
        public int engineVersion;
        public String dataSource;
        public int dayCount;
        public List<DayData> days;
        public List<Festival> festivals;
        /** month (1-12) → telugu header string from JSON object map */
        public java.util.Map<String, String> monthHeaders;

        @Nullable
        public DayData day(int month1, int dayOfMonth) {
            if (days == null) return null;
            for (DayData d : days) {
                if (d != null && d.month == month1 && d.day == dayOfMonth) return d;
            }
            return null;
        }

        @Nullable
        public DayData dayByKey(String ymd) {
            if (days == null || ymd == null) return null;
            for (DayData d : days) {
                if (d != null && ymd.equals(d.date)) return d;
            }
            return null;
        }

        @NonNull
        public List<Festival> festivalsForMonth(int month1) {
            List<Festival> out = new ArrayList<>();
            if (festivals == null) return out;
            for (Festival f : festivals) {
                if (f != null && f.month == month1) {
                    // Skip pure lunar phase labels in festival sheet if also real festivals exist —
                    // still include amavasya/pournami as list items like competitors sometimes do.
                    out.add(f);
                }
            }
            return out;
        }

        @NonNull
        public List<Festival> namedFestivalsForMonth(int month1) {
            List<Festival> out = new ArrayList<>();
            if (festivals == null) return out;
            for (Festival f : festivals) {
                if (f == null || f.month != month1) continue;
                if ("amavasya".equals(f.icon) || "pournami".equals(f.icon)) {
                    // Only include if it's a pure lunar marker without a better cultural name overlap
                    if ("అమావాస్య".equals(f.name) || "పూర్ణిమ".equals(f.name)) {
                        // Prefer not cluttering sheet with only phase — skip unless sole interest
                        continue;
                    }
                }
                out.add(f);
            }
            // If empty, fall back to all including amavasya
            if (out.isEmpty()) {
                return festivalsForMonth(month1);
            }
            return out;
        }

        @Nullable
        public String monthHeader(int month1) {
            if (monthHeaders != null) {
                String h = monthHeaders.get(String.valueOf(month1));
                if (h != null) return h;
            }
            DayData mid = day(month1, 15);
            if (mid != null && mid.teluguHeader != null) return mid.teluguHeader;
            DayData first = day(month1, 1);
            return first != null ? first.teluguHeader : null;
        }
    }
}
