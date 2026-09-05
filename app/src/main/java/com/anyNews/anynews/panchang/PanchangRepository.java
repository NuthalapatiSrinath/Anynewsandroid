package com.anyNews.anynews.panchang;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Utils.ProfileHubConfigHelper;

import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Response;

/**
 * Loads full-year panchang packs for the in-app Telugu calendar.
 *
 * <p>Hyderabad-only timings. Admin {@code calendar_panchang_source} (splash)
 * chooses ProKerala vendor pack (Way2-like default) vs astronomical engine.
 */
public final class PanchangRepository {

    private static final String TAG = "PanchangRepository";
    /** Year generate / cache I/O — must stay free of network so UI loadYear callbacks are not stalled. */
    private static final ExecutorService EXEC = Executors.newSingleThreadExecutor();
    /** Background soft-refresh only; never share EXEC with first-paint load. */
    private static final ExecutorService SOFT_FETCH_EXEC = Executors.newSingleThreadExecutor();
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
     * @param lat ignored — calendar always uses Hyderabad
     * @param lng ignored — calendar always uses Hyderabad
     */
    public static void loadYear(
            @NonNull Context context,
            int year,
            double lat,
            double lng,
            @Nullable Callback callback) {
        Context app = context.getApplicationContext();
        final double hydLat = PanchangEngine.DEFAULT_LAT;
        final double hydLng = PanchangEngine.DEFAULT_LNG;
        final boolean wantAstro = ProfileHubConfigHelper.prefersAstronomicalCalendar(app);

        EXEC.execute(() -> {
            PanchangModels.YearPack cached = PanchangYearCache.load(app, year, hydLat, hydLng);
            if (cached != null && cached.days != null && !cached.days.isEmpty()) {
                // If admin wants astronomical but cache is still ProKerala (or vice versa),
                // still paint immediately then soft-refresh from server.
                if (packMatchesPreferredSource(cached, wantAstro)) {
                    deliver(callback, cached, true);
                    enqueueSoftFetch(app, year, hydLat, hydLng, callback);
                    return;
                }
                // Wrong source in cache — paint local fallback immediately, soft-refresh off-thread.
                if (wantAstro) {
                    PanchangModels.YearPack pack = PanchangEngine.generateYear(year, hydLat, hydLng);
                    if (pack != null) {
                        if (pack.dataSource == null || pack.dataSource.isEmpty()) {
                            pack.dataSource = "astronomical_hyderabad_v3";
                        }
                        PanchangYearCache.save(app, pack);
                        deliver(callback, pack, false);
                    } else {
                        deliver(callback, cached, true);
                    }
                } else {
                    // Prefer bundled ProKerala asset over stale astronomical cache.
                    PanchangModels.YearPack asset =
                            PanchangYearCache.loadFromAssetsPublic(app, year);
                    if (asset != null) {
                        PanchangYearCache.save(app, asset);
                        deliver(callback, asset, true);
                    } else {
                        deliver(callback, cached, true);
                    }
                }
                enqueueSoftFetch(app, year, hydLat, hydLng, callback);
                return;
            }

            if (wantAstro) {
                PanchangModels.YearPack pack = PanchangEngine.generateYear(year, hydLat, hydLng);
                if (pack != null && (pack.dataSource == null || pack.dataSource.isEmpty())) {
                    pack.dataSource = "astronomical_hyderabad_v3";
                }
                PanchangYearCache.save(app, pack);
                deliver(callback, pack, false);
                enqueueSoftFetch(app, year, hydLat, hydLng, null);
                return;
            }

            // Default: assets via load(), else generate on device.
            PanchangModels.YearPack pack = PanchangEngine.generateYear(year, hydLat, hydLng);
            PanchangYearCache.save(app, pack);
            deliver(callback, pack, false);
            enqueueSoftFetch(app, year, hydLat, hydLng, null);
        });
    }

    /** Soft server refresh must not block EXEC (warmCurrentYear + UI loadYear share it). */
    private static void enqueueSoftFetch(
            Context app, int year, double lat, double lng, @Nullable Callback callback) {
        SOFT_FETCH_EXEC.execute(() -> softFetchServer(app, year, lat, lng, callback));
    }

    private static boolean isProkeralaVendor(@Nullable PanchangModels.YearPack pack) {
        return pack != null
                && pack.dataSource != null
                && pack.dataSource.contains("prokerala_telugu_panchangam_hyderabad");
    }

    private static boolean isAstronomical(@Nullable PanchangModels.YearPack pack) {
        if (pack == null || pack.dataSource == null) return false;
        String ds = pack.dataSource;
        return ds.contains("astronomical") && !isProkeralaVendor(pack);
    }

    private static boolean packMatchesPreferredSource(
            @Nullable PanchangModels.YearPack pack, boolean wantAstro) {
        if (pack == null) return false;
        if (wantAstro) return isAstronomical(pack) || !isProkeralaVendor(pack);
        return isProkeralaVendor(pack) || !isAstronomical(pack);
    }

    private static void softFetchServer(
            Context app, int year, double lat, double lng, @Nullable Callback callback) {
        try {
            boolean wantAstro = ProfileHubConfigHelper.prefersAstronomicalCalendar(app);
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

                // Honor admin source: if we want ProKerala, keep local vendor over astronomical remote.
                // If we want astronomical, allow remote to replace local ProKerala.
                if (!wantAstro) {
                    PanchangModels.YearPack local = PanchangYearCache.load(app, year, lat, lng);
                    if (isProkeralaVendor(local) && !isProkeralaVendor(remote)) {
                        Log.d(TAG, "server year pack skip: keep local ProKerala vendor");
                        return;
                    }
                } else if (isProkeralaVendor(remote) && !isAstronomical(remote)) {
                    // Server still on ProKerala while admin wants astronomical — keep generating locally.
                    Log.d(TAG, "server year pack skip: admin wants astronomical");
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
        if (n.month0 >= 10) {
            loadYear(context, n.year + 1, null);
        } else if (n.month0 <= 1) {
            loadYear(context, n.year - 1, null);
        }
    }

    private static final class CalendarYearNow {
        final int year;
        final int month0;

        CalendarYearNow(int year, int month0) {
            this.year = year;
            this.month0 = month0;
        }
    }

    private static CalendarYearNow nowParts() {
        Calendar c = Calendar.getInstance();
        return new CalendarYearNow(c.get(Calendar.YEAR), c.get(Calendar.MONTH));
    }
}
