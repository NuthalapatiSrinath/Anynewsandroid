package com.anyNews.anynews.ttd;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * TTD (Tirumala Tirupati Devasthanams) detail model.
 *
 * <p>No dedicated TTD API exists in ANYNEWS backend today. Fields mirror the
 * official tirumala.org day schedule / darshan sections so a future API can
 * drop in with the same shape. Source: {@link TtdRepository}.
 */
public final class TtdModels {

    private TtdModels() {}

    /** One timed temple schedule row (Suprabhatam, Darshanam, Seva, …). */
    public static final class ScheduleItem {
        @NonNull public final String timeRange;
        @NonNull public final String title;
        @Nullable public final String note;

        public ScheduleItem(@NonNull String timeRange, @NonNull String title,
                            @Nullable String note) {
            this.timeRange = timeRange;
            this.title = title;
            this.note = note;
        }
    }

    /** Darshan / seva service card. */
    public static final class ServiceItem {
        @NonNull public final String name;
        @NonNull public final String description;
        @Nullable public final String status;

        public ServiceItem(@NonNull String name, @NonNull String description,
                           @Nullable String status) {
            this.name = name;
            this.description = description;
            this.status = status;
        }
    }

    /** Full day pack shown on {@code TtdDetailActivity}. */
    public static final class DayPack {
        /** e.g. "Tirumala Tirupati Devasthanams" */
        @NonNull public final String templeName;
        /** e.g. "Sri Venkateswara Swamy Temple, Tirumala" */
        @NonNull public final String subtitle;
        /** Display date, e.g. "29 Aug 2026, Saturday" */
        @NonNull public final String dateLabel;
        /** Short Telugu / English greeting line */
        @NonNull public final String greeting;
        /** Flash / notice text from temple */
        @NonNull public final String flashNews;
        /** Slotted Sarva Darshan status summary */
        @NonNull public final String slottedDarshanStatus;
        @NonNull public final List<ScheduleItem> daySchedule;
        @NonNull public final List<ServiceItem> darshanServices;
        @NonNull public final List<ServiceItem> otherServices;
        /** Official site / booking URLs for open-in-browser + share */
        @NonNull public final String officialUrl;
        @NonNull public final String bookingUrl;
        /** Where this pack came from (static / api) */
        @NonNull public final String dataSource;
        /** Field inventory for docs / share footer */
        @NonNull public final List<String> fieldNames;

        public DayPack(
                @NonNull String templeName,
                @NonNull String subtitle,
                @NonNull String dateLabel,
                @NonNull String greeting,
                @NonNull String flashNews,
                @NonNull String slottedDarshanStatus,
                @NonNull List<ScheduleItem> daySchedule,
                @NonNull List<ServiceItem> darshanServices,
                @NonNull List<ServiceItem> otherServices,
                @NonNull String officialUrl,
                @NonNull String bookingUrl,
                @NonNull String dataSource,
                @NonNull List<String> fieldNames) {
            this.templeName = templeName;
            this.subtitle = subtitle;
            this.dateLabel = dateLabel;
            this.greeting = greeting;
            this.flashNews = flashNews;
            this.slottedDarshanStatus = slottedDarshanStatus;
            this.daySchedule = Collections.unmodifiableList(new ArrayList<>(daySchedule));
            this.darshanServices = Collections.unmodifiableList(new ArrayList<>(darshanServices));
            this.otherServices = Collections.unmodifiableList(new ArrayList<>(otherServices));
            this.officialUrl = officialUrl;
            this.bookingUrl = bookingUrl;
            this.dataSource = dataSource;
            this.fieldNames = Collections.unmodifiableList(new ArrayList<>(fieldNames));
        }
    }
}
