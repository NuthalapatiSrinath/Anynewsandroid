package com.anyNews.anynews.Services;

import com.anyNews.anynews.BuildConfig;

/**
 * API host for the Android app.
 *
 * Release / Play Store uses https://api.anynews.co.in/.
 * Debug uses the development API. The active value is BuildConfig.API_BASE_URL.
 */
public final class CommonUtilities {

    public static final String URL = BuildConfig.API_BASE_URL;

}
