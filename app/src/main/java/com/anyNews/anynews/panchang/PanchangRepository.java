package com.anyNews.anynews.panchang;



import android.content.Context;

import android.os.Handler;

import android.os.Looper;

import android.util.Log;



import androidx.annotation.NonNull;

import androidx.annotation.Nullable;



import com.anyNews.anynews.Services.APIService;

import com.anyNews.anynews.Services.RestApiClient;



import java.util.concurrent.ExecutorService;

import java.util.concurrent.Executors;



import retrofit2.Response;



/**

 * Loads full-year panchang packs for the in-app Telugu calendar.

 * <p>

 * Product rule (Way2News parity): calendar timings are always the fixed

 * Hyderabad ProKerala Telugu Panchangam pack — device/news GPS is ignored.

 * Soft server refresh must not replace that vendor pack with astronomical data

 * for other coordinates.

 */

public final class PanchangRepository {



    private static final String TAG = "PanchangRepository";

    private static final ExecutorService EXEC = Executors.newSingleThreadExecutor();

    private static final Handler MAIN = new Handler(Looper.getMainLooper());



    public interface Callback {

        void onPack(@NonNull PanchangModels.YearPack pack, boolean fromCache);

    }



    private PanchangRepository() {

    }



    /** Calendar reference city — always Hyderabad (not device GPS). */

    public static double defaultLat(@Nullable Context context) {

        return PanchangEngine.DEFAULT_LAT;

    }



    /** Calendar reference city — always Hyderabad (not device GPS). */

    public static double defaultLng(@Nullable Context context) {

        return PanchangEngine.DEFAULT_LNG;

    }



    /**

     * Ensure year pack is available. Invokes callback on main thread as soon as a pack is ready

     * (cache or local generate). Server may silently upgrade cache in background.

     */

    public static void loadYear(

            @NonNull Context context,

            int year,

            @Nullable Callback callback) {

        Context app = context.getApplicationContext();

        loadYear(app, year, PanchangEngine.DEFAULT_LAT, PanchangEngine.DEFAULT_LNG, callback);

    }



    /**

     * @param lat ignored — calendar always uses Hyderabad ProKerala pack

     * @param lng ignored — calendar always uses Hyderabad ProKerala pack

     */

    public static void loadYear(

            @NonNull Context context,

            int year,

            double lat,

            double lng,

            @Nullable Callback callback) {

        Context app = context.getApplicationContext();

        // Fixed Hyderabad for all users (Way2News calendar parity).

        final double hydLat = PanchangEngine.DEFAULT_LAT;

        final double hydLng = PanchangEngine.DEFAULT_LNG;

        EXEC.execute(() -> {

            PanchangModels.YearPack cached = PanchangYearCache.load(app, year, hydLat, hydLng);

            if (cached != null && cached.days != null && !cached.days.isEmpty()) {

                deliver(callback, cached, true);

                // Soft server refresh (does not block UI); still Hyd-only.

                softFetchServer(app, year, hydLat, hydLng, callback);

                return;

            }



            // Generate on device (offline capable) at Hyderabad coords only.

            PanchangModels.YearPack pack = PanchangEngine.generateYear(year, hydLat, hydLng);

            PanchangYearCache.save(app, pack);

            deliver(callback, pack, false);



            softFetchServer(app, year, hydLat, hydLng, null);

        });

    }



    private static boolean isProkeralaVendor(@Nullable PanchangModels.YearPack pack) {

        return pack != null

                && pack.dataSource != null

                && pack.dataSource.contains("prokerala_telugu_panchangam_hyderabad");

    }



    private static void softFetchServer(

            Context app, int year, double lat, double lng, @Nullable Callback callback) {

        try {

            APIService api = RestApiClient.getClient().create(APIService.class);

            Response<PanchangModels.YearPack> resp = api.calendarYear(

                    "v3", year, lat, lng, PanchangEngine.DEFAULT_TZ).execute();

            if (resp.isSuccessful() && resp.body() != null

                    && resp.body().days != null && !resp.body().days.isEmpty()) {

                PanchangModels.YearPack remote = resp.body();

                if (remote.engineVersion != PanchangEngine.ENGINE_VERSION) {

                    Log.d(TAG, "server year pack skip: engineVersion="

                            + remote.engineVersion + " want " + PanchangEngine.ENGINE_VERSION);

                    return;

                }

                PanchangModels.YearPack local = PanchangYearCache.load(app, year, lat, lng);

                // Never replace a local ProKerala vendor pack with astronomical fallback.

                if (isProkeralaVendor(local) && !isProkeralaVendor(remote)) {

                    Log.d(TAG, "server year pack skip: keep local ProKerala vendor");

                    return;

                }

                if (remote.cacheKey == null) {

                    remote.cacheKey = PanchangYearCache.cacheKey(

                            year, lat, lng, PanchangEngine.DEFAULT_TZ);

                }

                PanchangYearCache.save(app, remote);

                if (callback != null) {

                    deliver(callback, remote, false);

                }

            }

        } catch (Exception e) {

            Log.d(TAG, "server year pack skip: " + e.getMessage());

        }

    }



    private static void deliver(

            @Nullable Callback callback,

            @NonNull PanchangModels.YearPack pack,

            boolean fromCache) {

        if (callback == null) return;

        MAIN.post(() -> callback.onPack(pack, fromCache));

    }



    /** Precompute + store current year for faster first open. */

    public static void warmCurrentYear(@NonNull Context context) {

        CalendarYearNow n = nowParts();

        loadYear(context, n.year, null);

        // Also warm adjacent year near Dec/Jan

        if (n.month0 >= 10) {

            loadYear(context, n.year + 1, null);

        } else if (n.month0 <= 1) {

            loadYear(context, n.year - 1, null);

        }

    }



    public static CalendarYearNow nowParts() {

        java.util.Calendar c = java.util.Calendar.getInstance();

        CalendarYearNow n = new CalendarYearNow();

        n.year = c.get(java.util.Calendar.YEAR);

        n.month0 = c.get(java.util.Calendar.MONTH);

        n.day = c.get(java.util.Calendar.DAY_OF_MONTH);

        return n;

    }



    public static final class CalendarYearNow {

        public int year;

        public int month0;

        public int day;

    }

}


