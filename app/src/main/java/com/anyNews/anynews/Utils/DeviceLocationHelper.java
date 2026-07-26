package com.anyNews.anynews.Utils;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.anyNews.anynews.holder.GpsTracker;

/**
 * Device GPS for draft/session analytics.
 * Read path stays quiet (no GPS-settings dialogs).
 * Soft runtime permission may be requested once from Home (see shouldSoftRequestOnce).
 */
public final class DeviceLocationHelper {

    private static final String PREF_LAST_GPS_SENT_MS = "device_location_last_gps_sent_ms";
    /** One soft LOCATION prompt per install (Home first session). */
    private static final String PREF_LOCATION_SOFT_ASKED = "device_location_soft_asked_v1";
    /** Throttle GPS on session heartbeats (still send on open / close / splash). */
    public static final long HEARTBEAT_GPS_MIN_INTERVAL_MS = 3L * 60L * 1000L;

    private DeviceLocationHelper() {}

    public static final class Coords {
        public final double latitude;
        public final double longitude;

        public Coords(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public boolean isValid() {
            if (latitude == 0.0 && longitude == 0.0) return false;
            if (Math.abs(latitude) > 90.0 || Math.abs(longitude) > 180.0) return false;
            return true;
        }
    }

    public static boolean hasLocationPermission(Context context) {
        if (context == null) return false;
        Context appCtx = context.getApplicationContext();
        return ContextCompat.checkSelfPermission(appCtx, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(appCtx, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Soft-ask once per install when FINE/COARSE not yet granted.
     * Guests/readers never hit create-post permission prompts otherwise.
     */
    public static boolean shouldSoftRequestOnce(Context context) {
        if (context == null) return false;
        if (hasLocationPermission(context)) return false;
        try {
            SharedPreferences prefs = context.getApplicationContext()
                    .getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
            return !prefs.getBoolean(PREF_LOCATION_SOFT_ASKED, false);
        } catch (Exception e) {
            return false;
        }
    }

    public static void markSoftAsked(Context context) {
        if (context == null) return;
        try {
            SharedPreferences prefs = context.getApplicationContext()
                    .getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
            prefs.edit().putBoolean(PREF_LOCATION_SOFT_ASKED, true).apply();
        } catch (Exception ignored) {
        }
    }

    /**
     * Read last-known location if FINE or COARSE already granted.
     * Returns null when denied, providers off, or coords invalid / 0,0.
     */
    @Nullable
    public static Coords readIfPermitted(Context context) {
        if (context == null) return null;
        try {
            Context appCtx = context.getApplicationContext();
            GpsTracker tracker = new GpsTracker(appCtx);
            if (!tracker.hasLocationPermission()) {
                tracker.stopUsingGPS();
                return null;
            }
            // Prefer a fresh last-known read; constructor already called getLocation().
            if (!tracker.canGetLocation()) {
                tracker.stopUsingGPS();
                return null;
            }
            double lat = tracker.getLatitude();
            double lng = tracker.getLongitude();
            tracker.stopUsingGPS();
            Coords coords = new Coords(lat, lng);
            return coords.isValid() ? coords : null;
        } catch (Exception e) {
            return null;
        }
    }

    /** Whether enough time has passed to attach GPS on a heartbeat. */
    public static boolean shouldAttachGpsOnHeartbeat(Context context) {
        if (context == null) return false;
        try {
            SharedPreferences prefs = context.getApplicationContext()
                    .getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
            long last = prefs.getLong(PREF_LAST_GPS_SENT_MS, 0L);
            return System.currentTimeMillis() - last >= HEARTBEAT_GPS_MIN_INTERVAL_MS;
        } catch (Exception e) {
            return true;
        }
    }

    public static void markGpsSent(Context context) {
        if (context == null) return;
        try {
            SharedPreferences prefs = context.getApplicationContext()
                    .getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
            prefs.edit().putLong(PREF_LAST_GPS_SENT_MS, System.currentTimeMillis()).apply();
        } catch (Exception ignored) {
        }
    }
}
