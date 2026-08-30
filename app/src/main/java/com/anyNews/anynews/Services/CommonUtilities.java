package com.anyNews.anynews.Services;

/**
 * API host for the Android app.
 *
 * Dual env (match admin / server pattern):
 * - Development (2nd Oracle): {@code https://anynewsdevelopmentapi.anynews.co.in/}
 * - Production: {@code https://api.anynews.co.in/}
 *
 * Development APK / development branch uses the development API only —
 * do not ship production URL until explicitly switching for a Play release.
 */
public final class CommonUtilities {

    /** Development / 2nd Oracle API (active for development APK). */
    public static final String URL = "https://anynewsdevelopmentapi.anynews.co.in/";

    // Production (Play Store) — keep commented on development builds:
    // public static final String URL = "https://api.anynews.co.in/";

}
