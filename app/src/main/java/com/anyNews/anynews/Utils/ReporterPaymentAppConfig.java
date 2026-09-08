package com.anyNews.anynews.Utils;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.Nullable;

/**
 * Admin My Articles From floor + Profile payment banner from splash/session/profile.
 */
public final class ReporterPaymentAppConfig {

    public static final String PREF_ARTICLES_FROM_DATE = "reporter_articles_from_date";
    public static final String PREF_BANNER_ACTIVE = "reporter_payment_banner_active";
    public static final String PREF_BANNER_TEXT = "reporter_payment_banner_text";

    private ReporterPaymentAppConfig() {}

    public static void saveFromServer(
            @Nullable Context context,
            @Nullable String fromDate,
            @Nullable Boolean bannerActive,
            @Nullable String bannerText) {
        if (context == null) return;
        Context app = context.getApplicationContext();
        if (fromDate != null) {
            String d = fromDate.trim();
            if (d.length() >= 10) d = d.substring(0, 10);
            else if (TextUtils.isEmpty(d)) d = "";
            SharedPrefsData.putString(app, PREF_ARTICLES_FROM_DATE, d, Constants.PREF_NAME);
        }
        if (bannerActive != null) {
            SharedPrefsData.putBool(app, PREF_BANNER_ACTIVE, bannerActive, Constants.PREF_NAME);
        }
        if (bannerText != null) {
            SharedPrefsData.putString(
                    app,
                    PREF_BANNER_TEXT,
                    bannerText.trim(),
                    Constants.PREF_NAME);
        }
    }

    /** YYYY-MM-DD or empty. */
    @Nullable
    public static String articlesFromDate(@Nullable Context context) {
        try {
            String v =
                    SharedPrefsData.getString(
                            context != null ? context.getApplicationContext() : null,
                            PREF_ARTICLES_FROM_DATE,
                            Constants.PREF_NAME);
            if (TextUtils.isEmpty(v)) return "";
            v = v.trim();
            return v.length() >= 10 ? v.substring(0, 10) : v;
        } catch (Exception ignored) {
            return "";
        }
    }

    public static boolean bannerActive(@Nullable Context context) {
        try {
            return SharedPrefsData.getBool(
                    context != null ? context.getApplicationContext() : null,
                    PREF_BANNER_ACTIVE,
                    Constants.PREF_NAME);
        } catch (Exception ignored) {
            return false;
        }
    }

    @Nullable
    public static String bannerText(@Nullable Context context) {
        try {
            String v =
                    SharedPrefsData.getString(
                            context != null ? context.getApplicationContext() : null,
                            PREF_BANNER_TEXT,
                            Constants.PREF_NAME);
            return v != null ? v.trim() : "";
        } catch (Exception ignored) {
            return "";
        }
    }

    /** Parse YYYY-MM-DD to epoch millis at local midnight, or Long.MIN_VALUE. */
    public static long fromDateMillis(@Nullable String iso) {
        if (TextUtils.isEmpty(iso) || iso.length() < 10) return Long.MIN_VALUE;
        try {
            String[] p = iso.substring(0, 10).split("-");
            if (p.length != 3) return Long.MIN_VALUE;
            java.util.Calendar c = java.util.Calendar.getInstance();
            c.clear();
            c.set(Integer.parseInt(p[0]), Integer.parseInt(p[1]) - 1, Integer.parseInt(p[2]));
            return c.getTimeInMillis();
        } catch (Exception ignored) {
            return Long.MIN_VALUE;
        }
    }
}
