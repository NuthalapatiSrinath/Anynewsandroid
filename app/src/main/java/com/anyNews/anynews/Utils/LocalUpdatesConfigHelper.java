package com.anyNews.anynews.Utils;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Splash-delivered Weather / Fuel / Gold configs (root_settings → DeviceController).
 * Stores each payload as one JSON string plus an enabled flag.
 * Phones never call OpenWeather / fuel / metal APIs — they only read this cache.
 */
public final class LocalUpdatesConfigHelper {

    private LocalUpdatesConfigHelper() {}

    public static void saveWeatherFromSplash(@Nullable Context context, @Nullable Object raw) {
        saveBlob(context, raw, Constants.WEATHER_CONFIG_JSON, Constants.WEATHER_ENABLED,
                Constants.WEATHER_CONFIG_SET);
    }

    public static void saveFuelFromSplash(@Nullable Context context, @Nullable Object raw) {
        if (context == null || raw == null) return;
        try {
            JSONObject o = toJsonObject(raw);
            if (o == null) return;
            JSONObject existing = fuelJson(context);
            if (existing != null && isOlderFuelSnapshot(o, existing)) {
                return;
            }
            if (existing != null) {
                copyFuelHistoryIfMissing(existing, o);
            }
            // Normalize alias keys → petrol_price / diesel_price before persist.
            double petrol = resolveFuelPrice(o, true);
            double diesel = resolveFuelPrice(o, false);
            if (petrol > 0 && optDouble(o, "petrol_price", 0) <= 0) {
                o.put("petrol_price", petrol);
            }
            if (diesel > 0 && optDouble(o, "diesel_price", 0) <= 0) {
                o.put("diesel_price", diesel);
            }
            Context app = context.getApplicationContext();
            SharedPrefsData.putString(app, Constants.FUEL_CONFIG_JSON, o.toString(), Constants.PREF_NAME);
            putBool(app, Constants.FUEL_ENABLED, o.optBoolean("enabled", true));
            SharedPrefsData.putBool(app, Constants.FUEL_CONFIG_SET, true, Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
    }

    /** ISO timestamps compare in order. Missing times are not treated as stale. */
    private static boolean isOlderFuelSnapshot(@NonNull JSONObject incoming, @NonNull JSONObject existing) {
        String inAt = incoming.optString("fetched_at", "");
        String exAt = existing.optString("fetched_at", "");
        if (inAt.isEmpty() || exAt.isEmpty()) return false;
        return inAt.compareTo(exAt) < 0;
    }

    /** Keep real per-district points when a feed pack has prices but no history. */
    private static void copyFuelHistoryIfMissing(@NonNull JSONObject from, @NonNull JSONObject to)
            throws Exception {
        JSONArray src = from.optJSONArray("places");
        JSONArray dst = to.optJSONArray("places");
        if (src == null || dst == null) return;
        java.util.HashMap<String, JSONArray> history = new java.util.HashMap<>();
        for (int i = 0; i < src.length(); i++) {
            JSONObject row = src.optJSONObject(i);
            if (row == null) continue;
            JSONArray points = row.optJSONArray("history");
            if (points == null || points.length() == 0) continue;
            history.put(fuelPlaceKey(row), points);
        }
        if (history.isEmpty()) return;
        for (int i = 0; i < dst.length(); i++) {
            JSONObject row = dst.optJSONObject(i);
            if (row == null) continue;
            JSONArray points = row.optJSONArray("history");
            if (points != null && points.length() > 0) continue;
            JSONArray kept = history.get(fuelPlaceKey(row));
            if (kept != null) row.put("history", kept);
        }
    }

    @NonNull
    private static String fuelPlaceKey(@NonNull JSONObject row) {
        String state = row.optString("state_key", "").trim().toLowerCase(java.util.Locale.US);
        String city = row.optString("city_en", "");
        if (city.isEmpty()) city = row.optString("city", "");
        return state + "|" + city.trim().toLowerCase(java.util.Locale.US);
    }

    public static void saveGoldFromSplash(@Nullable Context context, @Nullable Object raw) {
        saveBlob(context, raw, Constants.GOLD_CONFIG_JSON, Constants.GOLD_ENABLED,
                Constants.GOLD_CONFIG_SET);
    }

    public static void saveJobsFromSplash(@Nullable Context context, @Nullable Object raw) {
        saveBlob(context, raw, Constants.JOBS_CONFIG_JSON, Constants.JOBS_ENABLED,
                Constants.JOBS_CONFIG_SET);
    }

    /**
     * Feed-poll master kill switches (no full blob). Null leaves cache.
     * Updates the same prefs keys splash uses so DU cards hide immediately.
     */
    public static void saveMasterEnabledFromPoll(
            @Nullable Context context,
            @Nullable Boolean weatherEnabled,
            @Nullable Boolean fuelEnabled,
            @Nullable Boolean goldEnabled,
            @Nullable Boolean jobsEnabled) {
        if (context == null) return;
        Context app = context.getApplicationContext();
        if (weatherEnabled != null) {
            putBool(app, Constants.WEATHER_ENABLED, weatherEnabled);
        }
        if (fuelEnabled != null) {
            putBool(app, Constants.FUEL_ENABLED, fuelEnabled);
        }
        if (goldEnabled != null) {
            putBool(app, Constants.GOLD_ENABLED, goldEnabled);
        }
        if (jobsEnabled != null) {
            putBool(app, Constants.JOBS_ENABLED, jobsEnabled);
        }
    }

    public static boolean isWeatherEnabled(@Nullable Context context) {
        return readBool(context, Constants.WEATHER_ENABLED, true)
                && DailyUpdatesConfigHelper.isWeatherLanguageAllowed(context);
    }

    public static boolean isFuelEnabled(@Nullable Context context) {
        return readBool(context, Constants.FUEL_ENABLED, true)
                && DailyUpdatesConfigHelper.isFuelLanguageAllowed(context);
    }

    public static boolean isGoldEnabled(@Nullable Context context) {
        // National IBJA rates: language matrix must not hide Gold when splash
        // already delivered numbers (chrome is localized client-side).
        if (hasRealGoldRates(context)) {
            return readBool(context, Constants.GOLD_ENABLED, true);
        }
        return readBool(context, Constants.GOLD_ENABLED, true)
                && DailyUpdatesConfigHelper.isGoldLanguageAllowed(context);
    }

    public static boolean isJobsEnabled(@Nullable Context context) {
        return readBool(context, Constants.JOBS_ENABLED, true)
                && DailyUpdatesConfigHelper.isJobsLanguageAllowed(context);
    }

    @Nullable
    public static JSONObject weatherJson(@Nullable Context context) {
        return readJson(context, Constants.WEATHER_CONFIG_JSON);
    }

    @Nullable
    public static JSONObject fuelJson(@Nullable Context context) {
        return readJson(context, Constants.FUEL_CONFIG_JSON);
    }

    @Nullable
    public static JSONObject goldJson(@Nullable Context context) {
        return readJson(context, Constants.GOLD_CONFIG_JSON);
    }

    @Nullable
    public static JSONObject jobsJson(@Nullable Context context) {
        return readJson(context, Constants.JOBS_CONFIG_JSON);
    }

    /** True when splash weather blob is present (same source as WeatherDetailActivity). */
    public static boolean hasWeatherData(@Nullable Context context) {
        return weatherJson(context) != null;
    }

    /** True when splash fuel blob is present (same source as FuelDetailActivity). */
    public static boolean hasFuelData(@Nullable Context context) {
        return fuelJson(context) != null;
    }

    public static int weatherTempC(@Nullable Context context) {
        return optInt(weatherJson(context), "temp_c", 0);
    }

    public static int weatherHighC(@Nullable Context context) {
        return optInt(weatherJson(context), "high_c", 0);
    }

    public static int weatherLowC(@Nullable Context context) {
        return optInt(weatherJson(context), "low_c", 0);
    }

    public static int weatherHumidity(@Nullable Context context) {
        return optInt(weatherJson(context), "humidity", 0);
    }

    @NonNull
    public static String weatherCondition(@Nullable Context context) {
        JSONObject w = weatherJson(context);
        String icon = optStr(w, "condition_icon", "");
        if (TextUtils.isEmpty(icon)) {
            icon = optStr(w, "icon", "");
        }
        String raw = optStr(w, "condition", "");
        // Prefer LanguageID pack from icon / Telugu fallback — never leave
        // splash Telugu condition stuck on Hindi/Malayalam hubs.
        return DailyUpdatesLabels.weatherCondition(context, icon, raw);
    }

    @NonNull
    public static String weatherConditionIcon(@Nullable Context context) {
        JSONObject w = weatherJson(context);
        String icon = optStr(w, "condition_icon", "");
        if (TextUtils.isEmpty(icon)) {
            icon = optStr(w, "icon", "");
        }
        return icon == null ? "" : icon;
    }

    /**
     * Petrol ₹/L from splash fuel blob. Tries common key aliases, then cities[0].
     * Returns 0 when absent (UI should show — not invent prices).
     */
    public static double fuelPetrol(@Nullable Context context) {
        return resolveFuelPrice(fuelJson(context), true);
    }

    public static double fuelDiesel(@Nullable Context context) {
        return resolveFuelPrice(fuelJson(context), false);
    }

    public static double fuelPetrolChange(@Nullable Context context) {
        return resolveFuelChange(fuelJson(context), true);
    }

    public static double fuelDieselChange(@Nullable Context context) {
        return resolveFuelChange(fuelJson(context), false);
    }

    /**
     * State label for fuel chrome — never district / primary-city name.
     * Prefers {@code state_filter}, then {@code location_region}.
     */
    @NonNull
    public static String fuelStateLabel(@Nullable Context context) {
        JSONObject f = fuelJson(context);
        String state = optStr(f, "state_filter", "");
        if (!TextUtils.isEmpty(state)) return state.trim();
        String region = optStr(f, "location_region", "");
        if (!TextUtils.isEmpty(region)) {
            // "ఆంధ్రప్రదేశ్, భారత్" → first segment
            int comma = region.indexOf(',');
            return (comma > 0 ? region.substring(0, comma) : region).trim();
        }
        return "";
    }

    private static double resolveFuelPrice(@Nullable JSONObject f, boolean petrol) {
        if (f == null) return 0;
        String[] keys = petrol
                ? new String[]{"petrol_price", "petrol", "petrolPrice", "petrol_rs", "price_petrol"}
                : new String[]{"diesel_price", "diesel", "dieselPrice", "diesel_rs", "price_diesel"};
        for (String k : keys) {
            double v = optDouble(f, k, 0);
            if (v > 0) return v;
        }
        // Nested prices object
        JSONObject prices = f.optJSONObject("prices");
        if (prices != null) {
            for (String k : keys) {
                double v = optDouble(prices, k, 0);
                if (v > 0) return v;
            }
        }
        // cities[0].petrol / diesel (The Core rows)
        JSONArray cities = optArray(f, "cities");
        for (int i = 0; i < cities.length(); i++) {
            JSONObject row = cities.optJSONObject(i);
            if (row == null) continue;
            double v = optDouble(row, petrol ? "petrol" : "diesel", 0);
            if (v <= 0) {
                v = optDouble(row, petrol ? "petrol_price" : "diesel_price", 0);
            }
            if (v > 0) return v;
        }
        return 0;
    }

    private static double resolveFuelChange(@Nullable JSONObject f, boolean petrol) {
        if (f == null) return 0;
        String[] keys = petrol
                ? new String[]{"petrol_change", "petrol_delta", "petrolChange"}
                : new String[]{"diesel_change", "diesel_delta", "dieselChange"};
        for (String k : keys) {
            if (f.has(k) && !f.isNull(k)) {
                return optDouble(f, k, 0);
            }
        }
        JSONArray cities = optArray(f, "cities");
        if (cities.length() > 0) {
            JSONObject row = cities.optJSONObject(0);
            if (row != null) {
                if (petrol) {
                    if (row.has("change") && !row.isNull("change")) {
                        return optDouble(row, "change", 0);
                    }
                } else if (row.has("diesel_change") && !row.isNull("diesel_change")) {
                    return optDouble(row, "diesel_change", 0);
                } else if (row.has("change") && !row.isNull("change")) {
                    return optDouble(row, "change", 0);
                }
            }
        }
        return 0;
    }

    /**
     * Real job count from splash {@code jobs.stats.total_jobs} (or items length).
     * Never invent thousands — returns 0 when none posted / not cached.
     */
    public static int jobsTotalCount(@Nullable Context context) {
        JSONObject j = jobsJson(context);
        if (j == null) return 0;
        JSONObject stats = j.optJSONObject("stats");
        if (stats != null) {
            int n = optInt(stats, "total_jobs", -1);
            if (n >= 0) return n;
        }
        JSONArray items = optArray(j, "items");
        return items.length();
    }

    /**
     * True when splash gold payload has real IBJA (or admin-corrected) rates —
     * never invent ₹74,200-style stubs for the UI.
     */
    public static boolean hasRealGoldRates(@Nullable Context context) {
        JSONObject g = goldJson(context);
        if (g == null) return false;
        String src = optStr(g, "fetch_source", "");
        if ("unavailable".equalsIgnoreCase(src) || "stub".equalsIgnoreCase(src)) {
            // Still allow last good saved rates if numbers are present.
        }
        double g10 = optDouble(g, "gold_24k_10g", 0);
        if (g10 > 0) return true;
        double g1 = optDouble(g, "gold_24k_1g", 0);
        if (g1 > 0) return true;
        JSONArray rows = optArray(g, "purity_rows");
        for (int i = 0; i < rows.length(); i++) {
            JSONObject row = rows.optJSONObject(i);
            if (row == null) continue;
            if (optDouble(row, "price_1g", 0) > 0 || optDouble(row, "price_10g", 0) > 0) {
                return true;
            }
        }
        return false;
    }

    public static double gold24k10g(@Nullable Context context) {
        return optDouble(goldJson(context), "gold_24k_10g", 0);
    }

    public static double gold24kChange(@Nullable Context context) {
        JSONObject g = goldJson(context);
        if (g == null || !g.has("gold_24k_change") || g.isNull("gold_24k_change")) {
            return Double.NaN;
        }
        return optDouble(g, "gold_24k_change", 0);
    }

    public static double silver10g(@Nullable Context context) {
        return optDouble(goldJson(context), "silver_10g", 0);
    }

    public static double silverChange(@Nullable Context context) {
        return optDouble(goldJson(context), "silver_change", 0);
    }

    /** Silver per kg for Daily Updates card (mock shows 1kg). */
    public static double silverKg(@Nullable Context context) {
        JSONObject g = goldJson(context);
        double kg = optDouble(g, "silver_kg", 0);
        if (kg > 0) return kg;
        double per10 = silver10g(context);
        return per10 > 0 ? per10 * 100 : 0;
    }

    public static double silverKgChange(@Nullable Context context) {
        JSONObject g = goldJson(context);
        if (g != null && g.has("silver_kg_change") && !g.isNull("silver_kg_change")) {
            return optDouble(g, "silver_kg_change", 0);
        }
        if (g != null && g.has("silver_change") && !g.isNull("silver_change")) {
            return silverChange(context) * 100;
        }
        return Double.NaN;
    }

    @NonNull
    public static String formatMoney2(double v) {
        return String.format(java.util.Locale.US, "%.2f", v);
    }

    @NonNull
    public static String formatInr(double v) {
        try {
            return java.text.NumberFormat.getNumberInstance(new java.util.Locale("en", "IN"))
                    .format(Math.round(v));
        } catch (Exception e) {
            return String.valueOf(Math.round(v));
        }
    }

    private static void saveBlob(@Nullable Context context, @Nullable Object raw,
                                 @NonNull String jsonKey, @NonNull String enabledKey,
                                 @NonNull String setKey) {
        if (context == null || raw == null) return;
        try {
            JSONObject o = toJsonObject(raw);
            if (o == null) return;
            Context app = context.getApplicationContext();
            int lang = SharedPrefsData.getInt(app, Constants.LanguageID, Constants.PREF_NAME);
            SharedPrefsData.putString(app, jsonKey, o.toString(), Constants.PREF_NAME);
            putBool(app, enabledKey, o.optBoolean("enabled", true));
            SharedPrefsData.putBool(app, setKey, true, Constants.PREF_NAME);
            if (lang > 0) {
                SharedPrefsData.putInt(app, Constants.LOCAL_UPDATES_SPLASH_LANG, lang, Constants.PREF_NAME);
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * After LanguageID flip: do <b>not</b> wipe weather/fuel/gold/jobs numeric splash
     * blobs — those rates are language-agnostic and chrome is localized client-side
     * ({@code DailyUpdatesLabels} / {@code GoldLabels} / {@code FuelLabels}).
     * Clearing them caused hub 0°C / petrol-gold dashes until the next splash.
     * Stamp is reset so a later splash can re-tag the pack language.
     */
    public static void clearLanguageSensitiveCaches(@Nullable Context context) {
        if (context == null) return;
        Context app = context.getApplicationContext();
        try {
            // Keep WEATHER/FUEL/GOLD/JOBS JSON + enabled flags intact.
            SharedPrefsData.putInt(app, Constants.LOCAL_UPDATES_SPLASH_LANG, 0, Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
    }

    /**
     * True when cached splash blobs were saved for a different LanguageID.
     * Informational only — never reject valid numeric payloads on mismatch.
     */
    public static boolean splashLangMismatch(@Nullable Context context) {
        if (context == null) return false;
        try {
            Context app = context.getApplicationContext();
            int lang = SharedPrefsData.getInt(app, Constants.LanguageID, Constants.PREF_NAME);
            int stamped = SharedPrefsData.getInt(app, Constants.LOCAL_UPDATES_SPLASH_LANG, Constants.PREF_NAME);
            return lang > 0 && stamped > 0 && stamped != lang;
        } catch (Exception e) {
            return false;
        }
    }

    @Nullable
    private static JSONObject toJsonObject(@NonNull Object raw) {
        try {
            if (raw instanceof JSONObject) return (JSONObject) raw;
            if (raw instanceof String) {
                String s = ((String) raw).trim();
                if (s.isEmpty() || "null".equalsIgnoreCase(s)) return null;
                return new JSONObject(s);
            }
            return new JSONObject(String.valueOf(new com.google.gson.Gson().toJson(raw)));
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    private static JSONObject readJson(@Nullable Context context, @NonNull String key) {
        if (context == null) return null;
        try {
            String v = SharedPrefsData.getString(
                    context.getApplicationContext(), key, Constants.PREF_NAME);
            if (TextUtils.isEmpty(v)) return null;
            return new JSONObject(v);
        } catch (Exception e) {
            return null;
        }
    }

    private static void putBool(Context app, String key, boolean v) {
        // commit() so Daily Updates open / feed-poll apply cannot race stale read.
        SharedPrefsData.putStringImmediate(app, key, v ? "true" : "false", Constants.PREF_NAME);
    }

    private static boolean readBool(@Nullable Context context, String key, boolean def) {
        if (context == null) return def;
        try {
            android.content.SharedPreferences sp =
                    context.getApplicationContext()
                            .getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
            if (sp != null && sp.contains(key)) {
                Object raw = sp.getAll().get(key);
                if (raw instanceof Boolean) return (Boolean) raw;
                if (raw instanceof String) {
                    String s = ((String) raw).trim();
                    if (s.isEmpty()) return def;
                    return "true".equalsIgnoreCase(s) || "1".equals(s);
                }
                if (raw instanceof Number) return ((Number) raw).intValue() != 0;
            }
            return def;
        } catch (Exception ignored) {
            return def;
        }
    }

    private static int optInt(@Nullable JSONObject o, String key, int def) {
        if (o == null) return def;
        try {
            if (!o.has(key) || o.isNull(key)) return def;
            return (int) Math.round(o.optDouble(key, def));
        } catch (Exception e) {
            return def;
        }
    }

    private static double optDouble(@Nullable JSONObject o, String key, double def) {
        if (o == null) return def;
        try {
            if (!o.has(key) || o.isNull(key)) return def;
            double v = o.optDouble(key, def);
            return Double.isNaN(v) ? def : v;
        } catch (Exception e) {
            return def;
        }
    }

    @NonNull
    private static String optStr(@Nullable JSONObject o, String key, String def) {
        if (o == null) return def;
        try {
            String s = o.optString(key, def);
            return TextUtils.isEmpty(s) ? def : s;
        } catch (Exception e) {
            return def;
        }
    }

    /** Safe array getter for detail screens. */
    @NonNull
    public static JSONArray optArray(@Nullable JSONObject o, @NonNull String key) {
        if (o == null) return new JSONArray();
        JSONArray a = o.optJSONArray(key);
        return a != null ? a : new JSONArray();
    }

    @NonNull
    public static String optString(@Nullable JSONObject o, @NonNull String key, @NonNull String def) {
        return optStr(o, key, def);
    }

    public static double optNumber(@Nullable JSONObject o, @NonNull String key, double def) {
        return optDouble(o, key, def);
    }

    public static int optNumberInt(@Nullable JSONObject o, @NonNull String key, int def) {
        return optInt(o, key, def);
    }
}
