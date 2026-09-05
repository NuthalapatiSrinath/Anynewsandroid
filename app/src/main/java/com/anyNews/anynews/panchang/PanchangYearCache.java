package com.anyNews.anynews.panchang;



import android.content.Context;

import android.content.res.AssetManager;

import android.util.Log;



import androidx.annotation.Nullable;



import com.google.gson.Gson;

import com.anyNews.anynews.Utils.ProfileHubConfigHelper;



import java.io.BufferedReader;

import java.io.File;

import java.io.FileInputStream;

import java.io.FileOutputStream;

import java.io.InputStream;

import java.io.InputStreamReader;

import java.nio.charset.StandardCharsets;

import java.util.Locale;



/**

 * Aggressive on-device year pack cache for the Telugu calendar.

 * <p>

 * Product rule: always the fixed Hyderabad ProKerala Telugu Panchangam pack

 * under {@code assets/panchang/hyderabad_YYYY.json}. Device/news GPS is not used

 * for panchang timings (Way2News parity).

 */

public final class PanchangYearCache {



    private static final String TAG = "PanchangYearCache";

    private static final Gson GSON = new Gson();



    private static volatile String memoryKey;

    @Nullable

    private static volatile PanchangModels.YearPack memoryPack;



    private PanchangYearCache() {

    }



    public static String cacheKey(int year, double lat, double lng, String tz) {

        // Calendar is always Hyderabad — normalize key so GPS never shards the cache.

        return String.format(Locale.US, "panchang_v%d_%d_%.2f_%.2f_%s",

                PanchangEngine.ENGINE_VERSION,

                year,

                PanchangEngine.DEFAULT_LAT,

                PanchangEngine.DEFAULT_LNG,

                tz != null ? tz : PanchangEngine.DEFAULT_TZ);

    }



    private static File file(Context context, String key) {

        File dir = new File(context.getApplicationContext().getFilesDir(), "panchang");

        //noinspection ResultOfMethodCallIgnored

        dir.mkdirs();

        String safe = key.replaceAll("[^a-zA-Z0-9._-]", "_");

        return new File(dir, safe + ".json");

    }



    /** Public for repository when switching admin calendar source. */
    @Nullable
    public static PanchangModels.YearPack loadFromAssetsPublic(Context context, int year) {
        return loadFromAssets(context, year);
    }

    @Nullable
    private static PanchangModels.YearPack loadFromAssets(Context context, int year) {

        String assetPath = "panchang/hyderabad_" + year + ".json";

        AssetManager am = context.getApplicationContext().getAssets();

        try (InputStream in = am.open(assetPath);

             BufferedReader br = new BufferedReader(

                     new InputStreamReader(in, StandardCharsets.UTF_8))) {

            StringBuilder sb = new StringBuilder(512 * 1024);

            char[] buf = new char[8192];

            int n;

            while ((n = br.read(buf)) >= 0) {

                sb.append(buf, 0, n);

            }

            PanchangModels.YearPack pack =

                    GSON.fromJson(sb.toString(), PanchangModels.YearPack.class);

            if (pack == null || pack.days == null || pack.days.size() < 365) return null;

            if (pack.engineVersion < 3) return null;

            // Stamp Hyd reference coords regardless of what the JSON stored.

            pack.lat = PanchangEngine.DEFAULT_LAT;

            pack.lng = PanchangEngine.DEFAULT_LNG;

            if (pack.timezone == null || pack.timezone.isEmpty()) {

                pack.timezone = PanchangEngine.DEFAULT_TZ;

            }

            if (pack.locationName == null || pack.locationName.isEmpty()) {

                pack.locationName = "Hyderabad, Telangana";

            }

            Log.i(TAG, "loaded ProKerala asset " + assetPath + " days=" + pack.days.size());

            return pack;

        } catch (Exception e) {

            Log.d(TAG, "no asset " + assetPath + ": " + e.getMessage());

            return null;

        }

    }



    /**

     * Load year pack. {@code lat}/{@code lng} are ignored — always Hyderabad vendor/cache.

     */

    @Nullable

    public static PanchangModels.YearPack load(Context context, int year, double lat, double lng) {

        if (context == null) return null;

        String key = cacheKey(year, PanchangEngine.DEFAULT_LAT, PanchangEngine.DEFAULT_LNG,

                PanchangEngine.DEFAULT_TZ);

        if (key.equals(memoryKey) && memoryPack != null

                && memoryPack.days != null && !memoryPack.days.isEmpty()) {

            return memoryPack;

        }

        File f = file(context, key);

        if (f.exists() && f.length() >= 100) {

            try (BufferedReader br = new BufferedReader(

                    new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {

                StringBuilder sb = new StringBuilder((int) Math.min(f.length(), Integer.MAX_VALUE));

                char[] buf = new char[8192];

                int n;

                while ((n = br.read(buf)) >= 0) {

                    sb.append(buf, 0, n);

                }

                PanchangModels.YearPack pack =

                        GSON.fromJson(sb.toString(), PanchangModels.YearPack.class);

                if (pack != null && pack.days != null && !pack.days.isEmpty()) {

                    boolean vendorHyd = pack.engineVersion >= 3

                            && pack.dataSource != null

                            && pack.dataSource.contains("prokerala_telugu_panchangam_hyderabad");

                    if (pack.engineVersion == PanchangEngine.ENGINE_VERSION || vendorHyd) {

                        memoryKey = key;

                        memoryPack = pack;

                        return pack;

                    }

                    Log.i(TAG, "stale engineVersion=" + pack.engineVersion

                            + " need " + PanchangEngine.ENGINE_VERSION + "; regenerating");

                    //noinspection ResultOfMethodCallIgnored

                    f.delete();

                }

            } catch (Exception e) {

                Log.w(TAG, "load failed: " + e.getMessage());

            }

        }



        // Bundled ProKerala pack (Way2-like). Skip when admin selected astronomical.
        if (!ProfileHubConfigHelper.prefersAstronomicalCalendar(context)) {
            PanchangModels.YearPack asset = loadFromAssets(context, year);
            if (asset != null) {
                if (asset.cacheKey == null) {
                    asset.cacheKey = key;
                }
                memoryKey = key;
                memoryPack = asset;
                save(context, asset);
                return asset;
            }
        }

        return null;

    }



    public static void save(Context context, PanchangModels.YearPack pack) {

        if (context == null || pack == null || pack.days == null || pack.days.isEmpty()) return;

        String key = pack.cacheKey != null

                ? pack.cacheKey

                : cacheKey(pack.year, PanchangEngine.DEFAULT_LAT, PanchangEngine.DEFAULT_LNG,

                pack.timezone);

        try {

            String json = GSON.toJson(pack);

            File f = file(context, key);

            File tmp = new File(f.getAbsolutePath() + ".tmp");

            try (FileOutputStream fos = new FileOutputStream(tmp)) {

                fos.write(json.getBytes(StandardCharsets.UTF_8));

                fos.flush();

            }

            if (f.exists()) {

                //noinspection ResultOfMethodCallIgnored

                f.delete();

            }

            //noinspection ResultOfMethodCallIgnored

            tmp.renameTo(f);

            memoryKey = key;

            memoryPack = pack;

        } catch (Exception e) {

            Log.w(TAG, "save failed: " + e.getMessage());

        }

    }



    public static void putMemory(PanchangModels.YearPack pack) {

        if (pack == null) return;

        memoryKey = pack.cacheKey != null

                ? pack.cacheKey

                : cacheKey(pack.year, PanchangEngine.DEFAULT_LAT, PanchangEngine.DEFAULT_LNG,

                pack.timezone);

        memoryPack = pack;

    }

}


