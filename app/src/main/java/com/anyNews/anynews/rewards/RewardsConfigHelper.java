package com.anyNews.anynews.rewards;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Persists splash rewards chip copy/style + joined state for the main-feed slot.
 * Chip text is language-tagged so Telugu splash prefs never stick after Hindi/ML.
 */
public final class RewardsConfigHelper {

    public static final String PREF_GUEST_CHIP = "rewards_guest_chip";
    public static final String PREF_JOINED_CHIP = "rewards_joined_chip";
    public static final String PREF_RATE_LABEL = "rewards_rate_label";
    public static final String PREF_JOINED = "rewards_user_joined";
    public static final String PREF_CHIP_COLOR = "rewards_chip_color";
    public static final String PREF_CHIP_SIZE_SP = "rewards_chip_size_sp";
    public static final String PREF_CHIP_WEIGHT = "rewards_chip_weight";
    /** Language id that the saved guest/joined chip strings were written for. */
    public static final String PREF_CHIP_LANG = "rewards_chip_copy_lang";
    /** IST day the stored factor caps belong to (YYYY-MM-DD). */
    public static final String PREF_FACTOR_CAPS_DAY = "rewards_factor_caps_day";
    /** JSON: { day_key, factors: { ads_seen: { cap_per_day, remaining, capped, ... } } } */
    public static final String PREF_FACTOR_CAPS_JSON = "rewards_factor_caps_json";
    /** live | ended | upcoming — from splash, session poll, or rewards hub. */
    public static final String PREF_PROGRAM_STATUS = "rewards_program_status";
    public static final String PREF_SCORING_OPEN = "rewards_scoring_open";
    public static final String PREF_PERIOD_ENDED = "rewards_period_ended";
    public static final String PREF_PERIOD_UPCOMING = "rewards_period_upcoming";
    public static final String PREF_PERIOD_BANNER = "rewards_period_banner";

    private RewardsConfigHelper() {}

    /** IST calendar day — same window the server uses for cap/day. */
    public static String istDayKey() {
        try {
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            fmt.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
            return fmt.format(new Date());
        } catch (Exception e) {
            return "";
        }
    }

    /** Seconds until the next IST midnight, when cap/day resets. At least 1 while still today. */
    public static int secondsUntilIstReset() {
        try {
            java.util.Calendar cal =
                    java.util.Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata"));
            long now = System.currentTimeMillis();
            cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
            cal.set(java.util.Calendar.MINUTE, 0);
            cal.set(java.util.Calendar.SECOND, 0);
            cal.set(java.util.Calendar.MILLISECOND, 0);
            cal.add(java.util.Calendar.DAY_OF_MONTH, 1);
            long waitMs = cal.getTimeInMillis() - now;
            if (waitMs <= 0) return 1;
            long sec = (waitMs + 999L) / 1000L;
            if (sec > Integer.MAX_VALUE) return Integer.MAX_VALUE;
            return (int) sec;
        } catch (Exception e) {
            return 60;
        }
    }

    /**
     * Apply splash/session/hub/batch cap status so the client stops posting
     * factors that are already at cap/day. Feed ads are not hidden here.
     */
    public static void applyFactorCaps(
            @Nullable Context context, @Nullable JsonObject caps, @Nullable String dayKey) {
        if (context == null || caps == null) return;
        Context app = context.getApplicationContext();
        JsonObject factors = caps;
        String day = dayKey;
        if (caps.has("factors") && caps.get("factors").isJsonObject()) {
            factors = caps.getAsJsonObject("factors");
            if (TextUtils.isEmpty(day) && caps.has("day_key") && !caps.get("day_key").isJsonNull()) {
                day = caps.get("day_key").getAsString();
            }
        } else if (caps.has("day_key") && !caps.get("day_key").isJsonNull()) {
            day = caps.get("day_key").getAsString();
        }
        if (TextUtils.isEmpty(day)) day = istDayKey();
        JsonObject stored = new JsonObject();
        stored.addProperty("day_key", day);
        stored.add("factors", factors);
        try {
            SharedPrefsData.putString(
                    app, PREF_FACTOR_CAPS_DAY, day, Constants.PREF_NAME);
            SharedPrefsData.putString(
                    app, PREF_FACTOR_CAPS_JSON, stored.toString(), Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
    }

    public static void applyFactorCapsJson(
            @Nullable Context context, @Nullable String json) {
        if (context == null || TextUtils.isEmpty(json)) return;
        try {
            JsonElement parsed = new JsonParser().parse(json);
            if (parsed != null && parsed.isJsonObject()) {
                applyFactorCaps(context, parsed.getAsJsonObject(), null);
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Persist ended/upcoming copy from splash, session poll, or rewards hub.
     * Missing fields are left alone so an older payload cannot wipe a live message.
     */
    public static void applyProgramWindow(
            @Nullable Context context, @Nullable JsonObject body) {
        if (context == null || body == null) return;
        Context app = context.getApplicationContext();
        try {
            JsonObject program =
                    body.has("program") && body.get("program").isJsonObject()
                            ? body.getAsJsonObject("program")
                            : null;
            String status = firstString(body, program, "program_status", "rewards_program_status", "status");
            if (!TextUtils.isEmpty(status)) {
                SharedPrefsData.putString(
                        app, PREF_PROGRAM_STATUS, status.trim().toLowerCase(Locale.US), Constants.PREF_NAME);
            }
            if (body.has("scoring_open") && !body.get("scoring_open").isJsonNull()) {
                SharedPrefsData.putString(
                        app,
                        PREF_SCORING_OPEN,
                        body.get("scoring_open").getAsBoolean() ? "1" : "0",
                        Constants.PREF_NAME);
            } else if (body.has("rewards_scoring_open") && !body.get("rewards_scoring_open").isJsonNull()) {
                SharedPrefsData.putString(
                        app,
                        PREF_SCORING_OPEN,
                        body.get("rewards_scoring_open").getAsBoolean() ? "1" : "0",
                        Constants.PREF_NAME);
            } else if (program != null && program.has("scoring_open") && !program.get("scoring_open").isJsonNull()) {
                SharedPrefsData.putString(
                        app,
                        PREF_SCORING_OPEN,
                        program.get("scoring_open").getAsBoolean() ? "1" : "0",
                        Constants.PREF_NAME);
            }
            putIfPresent(app, PREF_PERIOD_ENDED, firstString(body, program, "period_ended", "rewards_period_ended"));
            putIfPresent(app, PREF_PERIOD_UPCOMING, firstString(body, program, "period_upcoming", "rewards_period_upcoming"));
            putIfPresent(app, PREF_PERIOD_BANNER, firstString(body, program, "period_banner", "rewards_period_banner", "banner"));
        } catch (Exception ignored) {
        }
    }

    public static void saveProgramWindow(
            @Nullable Context context,
            @Nullable String status,
            @Nullable Boolean scoringOpen,
            @Nullable String ended,
            @Nullable String upcoming,
            @Nullable String banner) {
        if (context == null) return;
        Context app = context.getApplicationContext();
        try {
            if (!TextUtils.isEmpty(status)) {
                SharedPrefsData.putString(
                        app, PREF_PROGRAM_STATUS, status.trim().toLowerCase(Locale.US), Constants.PREF_NAME);
            }
            if (scoringOpen != null) {
                SharedPrefsData.putString(
                        app, PREF_SCORING_OPEN, scoringOpen ? "1" : "0", Constants.PREF_NAME);
            }
            putIfPresent(app, PREF_PERIOD_ENDED, ended);
            putIfPresent(app, PREF_PERIOD_UPCOMING, upcoming);
            putIfPresent(app, PREF_PERIOD_BANNER, banner);
        } catch (Exception ignored) {
        }
    }

    /** Unknown (old server) stays open so we do not block scoring before the first poll. */
    public static boolean scoringOpen(@Nullable Context context) {
        String v = read(context, PREF_SCORING_OPEN, "");
        if (TextUtils.isEmpty(v)) return true;
        return !"0".equals(v) && !"false".equalsIgnoreCase(v);
    }

    public static String programStatus(@Nullable Context context) {
        String v = read(context, PREF_PROGRAM_STATUS, "live");
        return TextUtils.isEmpty(v) ? "live" : v.trim().toLowerCase(Locale.US);
    }

    public static String periodBannerText(@Nullable Context context) {
        String status = programStatus(context);
        String banner = read(context, PREF_PERIOD_BANNER, "");
        if (!TextUtils.isEmpty(banner) && !"live".equals(status)) return banner;
        if ("ended".equals(status)) {
            String ended = read(context, PREF_PERIOD_ENDED, "");
            return TextUtils.isEmpty(ended)
                    ? "The current week has ended. Next week’s rewards will start shortly."
                    : ended;
        }
        if ("upcoming".equals(status)) {
            String upcoming = read(context, PREF_PERIOD_UPCOMING, "");
            return TextUtils.isEmpty(upcoming)
                    ? "Next week’s rewards will start shortly."
                    : upcoming;
        }
        return "";
    }

    private static void putIfPresent(Context app, String key, @Nullable String value) {
        if (TextUtils.isEmpty(value)) return;
        SharedPrefsData.putString(app, key, value.trim(), Constants.PREF_NAME);
    }

    @Nullable
    private static String firstString(
            JsonObject body, @Nullable JsonObject nested, String... keys) {
        for (String key : keys) {
            String fromBody = jsonString(body, key);
            if (!TextUtils.isEmpty(fromBody)) return fromBody;
            String fromNested = jsonString(nested, key);
            if (!TextUtils.isEmpty(fromNested)) return fromNested;
        }
        return "";
    }

    @Nullable
    private static String jsonString(@Nullable JsonObject obj, String key) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) return "";
        try {
            if (!obj.get(key).isJsonPrimitive()) return "";
            return obj.get(key).getAsString();
        } catch (Exception e) {
            return "";
        }
    }

    /** Pull {@code factor_caps} or the splash/session ads_seen scalars from a payload. */
    public static void applyFromRewardsPayload(
            @Nullable Context context, @Nullable JsonObject body) {
        if (context == null || body == null) return;
        applyProgramWindow(context, body);
        if (body.has("factor_caps") && body.get("factor_caps").isJsonObject()) {
            applyFactorCaps(context, body.getAsJsonObject("factor_caps"), null);
            return;
        }
        if (body.has("rewards_factor_caps") && body.get("rewards_factor_caps").isJsonObject()) {
            applyFactorCaps(context, body.getAsJsonObject("rewards_factor_caps"), null);
            return;
        }
        if (body.has("rewards_factor_caps")
                && body.get("rewards_factor_caps").isJsonPrimitive()) {
            applyFactorCapsJson(context, body.get("rewards_factor_caps").getAsString());
            return;
        }
        if (!body.has("rewards_ads_seen_cap") && !body.has("rewards_ads_seen_capped")) return;
        JsonObject ads = new JsonObject();
        if (body.has("rewards_ads_seen_cap") && !body.get("rewards_ads_seen_cap").isJsonNull()) {
            ads.addProperty("cap_per_day", body.get("rewards_ads_seen_cap").getAsInt());
        }
        if (body.has("rewards_ads_seen_remaining")
                && !body.get("rewards_ads_seen_remaining").isJsonNull()) {
            ads.addProperty("remaining", body.get("rewards_ads_seen_remaining").getAsInt());
        }
        if (body.has("rewards_ads_seen_capped")
                && !body.get("rewards_ads_seen_capped").isJsonNull()) {
            ads.addProperty("capped", body.get("rewards_ads_seen_capped").getAsBoolean());
        }
        JsonObject wrap = new JsonObject();
        wrap.add("ads_seen", ads);
        applyFactorCaps(context, wrap, istDayKey());
    }

    public static boolean isFactorCapped(@Nullable Context context, @Nullable String factor) {
        JsonObject row = factorRow(context, factor);
        if (row == null) return false;
        if (row.has("enabled") && !row.get("enabled").isJsonNull() && !row.get("enabled").getAsBoolean()) {
            return true;
        }
        if (row.has("capped") && !row.get("capped").isJsonNull() && row.get("capped").getAsBoolean()) {
            return true;
        }
        if (row.has("remaining") && !row.get("remaining").isJsonNull()) {
            try {
                return row.get("remaining").getAsInt() <= 0
                        && row.has("cap_per_day")
                        && !row.get("cap_per_day").isJsonNull()
                        && row.get("cap_per_day").getAsInt() > 0;
            } catch (Exception ignored) {
                return false;
            }
        }
        return false;
    }

    /**
     * Admin "Coins" for a factor. For ads_seen this is always unused for pay
     * (Earn actions owns watch-ad coins). Returns -1 if unknown.
     */
    public static int factorCoinsPer(@Nullable Context context, @Nullable String factor) {
        if ("ads_seen".equals(factor)) return 0;
        JsonObject row = factorRow(context, factor);
        if (row == null || !row.has("coins_per") || row.get("coins_per").isJsonNull()) return -1;
        try {
            return Math.max(0, row.get("coins_per").getAsInt());
        } catch (Exception e) {
            return -1;
        }
    }

    /** Admin cap/day. For ads_seen this is the daily rewarded-ad count only. -1 if unknown. */
    public static int factorCapPerDay(@Nullable Context context, @Nullable String factor) {
        JsonObject row = factorRow(context, factor);
        if (row == null || !row.has("cap_per_day") || row.get("cap_per_day").isJsonNull()) {
            return -1;
        }
        try {
            return Math.max(0, row.get("cap_per_day").getAsInt());
        } catch (Exception e) {
            return -1;
        }
    }

    /** Remaining qualifying events today, or -1 if unlimited / unknown. */
    public static int factorRemaining(@Nullable Context context, @Nullable String factor) {
        JsonObject row = factorRow(context, factor);
        if (row == null || !row.has("remaining") || row.get("remaining").isJsonNull()) return -1;
        try {
            return Math.max(0, row.get("remaining").getAsInt());
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Write remaining for one factor without dropping sibling caps.
     * remaining &lt;= 0 marks the factor capped for the current IST day.
     */
    public static void setFactorRemaining(
            @Nullable Context context, @Nullable String factor, int remaining, int capPerDay) {
        if (context == null || TextUtils.isEmpty(factor)) return;
        JsonObject stored = storedCaps(context);
        JsonObject factors;
        String day = istDayKey();
        if (stored == null) {
            stored = new JsonObject();
            factors = new JsonObject();
        } else {
            factors =
                    stored.has("factors") && stored.get("factors").isJsonObject()
                            ? stored.getAsJsonObject("factors")
                            : new JsonObject();
            if (stored.has("day_key") && !stored.get("day_key").isJsonNull()) {
                String stamped = stored.get("day_key").getAsString();
                if (!TextUtils.isEmpty(stamped)) day = stamped;
            }
        }
        JsonObject row =
                factors.has(factor) && factors.get(factor).isJsonObject()
                        ? factors.getAsJsonObject(factor)
                        : new JsonObject();
        int cap = capPerDay;
        if (cap <= 0 && row.has("cap_per_day") && !row.get("cap_per_day").isJsonNull()) {
            try {
                cap = row.get("cap_per_day").getAsInt();
            } catch (Exception ignored) {
            }
        }
        if (cap <= 0) cap = Math.max(1, remaining);
        int next = Math.max(0, remaining);
        row.addProperty("enabled", true);
        row.addProperty("cap_per_day", cap);
        row.addProperty("remaining", next);
        row.addProperty("capped", next <= 0);
        factors.add(factor, row);
        stored.add("factors", factors);
        stored.addProperty("day_key", day);
        applyFactorCaps(context, stored, day);
    }

    public static void noteFactorQueued(
            @Nullable Context context, @Nullable String factor, int units) {
        if (context == null || TextUtils.isEmpty(factor) || units <= 0) return;
        JsonObject stored = storedCaps(context);
        if (stored == null) return;
        JsonObject factors = stored.getAsJsonObject("factors");
        if (factors == null || !factors.has(factor) || !factors.get(factor).isJsonObject()) return;
        JsonObject row = factors.getAsJsonObject(factor);
        if (!row.has("remaining") || row.get("remaining").isJsonNull()) return;
        int cap = 0;
        try {
            if (row.has("cap_per_day") && !row.get("cap_per_day").isJsonNull()) {
                cap = row.get("cap_per_day").getAsInt();
            }
        } catch (Exception ignored) {
        }
        if (cap <= 0) return;
        int remaining;
        try {
            remaining = row.get("remaining").getAsInt();
        } catch (Exception e) {
            return;
        }
        int next = Math.max(0, remaining - units);
        row.addProperty("remaining", next);
        if (next <= 0) row.addProperty("capped", true);
        applyFactorCaps(context, stored, stored.has("day_key") ? stored.get("day_key").getAsString() : istDayKey());
    }

    @Nullable
    private static JsonObject factorRow(@Nullable Context context, @Nullable String factor) {
        if (TextUtils.isEmpty(factor)) return null;
        JsonObject stored = storedCaps(context);
        if (stored == null) return null;
        JsonObject factors = stored.getAsJsonObject("factors");
        if (factors == null || !factors.has(factor) || !factors.get(factor).isJsonObject()) {
            return null;
        }
        return factors.getAsJsonObject(factor);
    }

    @Nullable
    private static JsonObject storedCaps(@Nullable Context context) {
        if (context == null) return null;
        try {
            Context app = context.getApplicationContext();
            String day = SharedPrefsData.getString(app, PREF_FACTOR_CAPS_DAY, Constants.PREF_NAME);
            if (TextUtils.isEmpty(day) || !day.equals(istDayKey())) return null;
            String raw = SharedPrefsData.getString(app, PREF_FACTOR_CAPS_JSON, Constants.PREF_NAME);
            if (TextUtils.isEmpty(raw)) return null;
            JsonElement parsed = new JsonParser().parse(raw);
            if (parsed == null || !parsed.isJsonObject()) return null;
            JsonObject obj = parsed.getAsJsonObject();
            if (obj.has("day_key") && !obj.get("day_key").isJsonNull()) {
                String stamped = obj.get("day_key").getAsString();
                if (!TextUtils.isEmpty(stamped) && !stamped.equals(istDayKey())) return null;
            }
            if (!obj.has("factors") || !obj.get("factors").isJsonObject()) return null;
            return obj;
        } catch (Exception e) {
            return null;
        }
    }

    public static void saveChipCopyFromServer(
            @Nullable Context context,
            @Nullable String guestChip,
            @Nullable String joinedChip,
            @Nullable String rateLabel) {
        saveChipCopyFromServer(context, guestChip, joinedChip, rateLabel, null, null, null);
    }

    public static void saveChipCopyFromServer(
            @Nullable Context context,
            @Nullable String guestChip,
            @Nullable String joinedChip,
            @Nullable String rateLabel,
            @Nullable String chipColor,
            @Nullable Integer chipSizeSp,
            @Nullable String chipWeight) {
        if (context == null) return;
        Context app = context.getApplicationContext();
        int lang = currentLanguageId(app);
        if (!TextUtils.isEmpty(guestChip)) {
            SharedPrefsData.putString(app, PREF_GUEST_CHIP, guestChip.trim(), Constants.PREF_NAME);
        }
        if (!TextUtils.isEmpty(joinedChip)) {
            SharedPrefsData.putString(app, PREF_JOINED_CHIP, joinedChip.trim(), Constants.PREF_NAME);
        }
        if (!TextUtils.isEmpty(rateLabel)) {
            SharedPrefsData.putString(app, PREF_RATE_LABEL, rateLabel.trim(), Constants.PREF_NAME);
        }
        if (!TextUtils.isEmpty(chipColor)) {
            SharedPrefsData.putString(app, PREF_CHIP_COLOR, chipColor.trim(), Constants.PREF_NAME);
        }
        if (chipSizeSp != null && chipSizeSp > 0) {
            SharedPrefsData.putString(
                    app, PREF_CHIP_SIZE_SP, String.valueOf(chipSizeSp), Constants.PREF_NAME);
        }
        if (!TextUtils.isEmpty(chipWeight)) {
            SharedPrefsData.putString(
                    app, PREF_CHIP_WEIGHT, chipWeight.trim().toLowerCase(), Constants.PREF_NAME);
        }
        // Stamp language whenever we persist chip strings so a later LanguageID
        // change falls through to RewardsLabels instead of stale Telugu.
        if (!TextUtils.isEmpty(guestChip) || !TextUtils.isEmpty(joinedChip)) {
            SharedPrefsData.putInt(app, PREF_CHIP_LANG, lang, Constants.PREF_NAME);
        }
    }

    /** Drop language-bound chip strings after an in-app language switch. */
    public static void clearChipCopyForLanguageChange(@Nullable Context context) {
        if (context == null) return;
        Context app = context.getApplicationContext();
        try {
            SharedPrefsData.putString(app, PREF_GUEST_CHIP, "", Constants.PREF_NAME);
            SharedPrefsData.putString(app, PREF_JOINED_CHIP, "", Constants.PREF_NAME);
            SharedPrefsData.putString(app, PREF_RATE_LABEL, "", Constants.PREF_NAME);
            SharedPrefsData.putInt(app, PREF_CHIP_LANG, 0, Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
    }

    public static void setJoined(@Nullable Context context, boolean joined) {
        if (context == null) return;
        SharedPrefsData.putBool(
                context.getApplicationContext(), PREF_JOINED, joined, Constants.PREF_NAME);
    }

    public static boolean isJoined(@Nullable Context context) {
        if (context == null) return false;
        return SharedPrefsData.getBool(
                context.getApplicationContext(), PREF_JOINED, Constants.PREF_NAME);
    }

    public static String guestChip(@Nullable Context context) {
        if (chipPrefsMatchLanguage(context)) {
            String v = read(context, PREF_GUEST_CHIP, "");
            if (!TextUtils.isEmpty(v) && !"Join rewards".equalsIgnoreCase(v.trim())) {
                return v;
            }
        }
        return RewardsLabels.guestChip(context);
    }

    public static String joinedChip(@Nullable Context context) {
        if (chipPrefsMatchLanguage(context)) {
            String v = read(context, PREF_JOINED_CHIP, "");
            if (!TextUtils.isEmpty(v) && !"Rewards".equalsIgnoreCase(v.trim())) {
                return v;
            }
        }
        return RewardsLabels.joinedChip(context);
    }

    public static String rateLabel(@Nullable Context context) {
        if (chipPrefsMatchLanguage(context)) {
            String v = read(context, PREF_RATE_LABEL, "");
            if (!TextUtils.isEmpty(v)) return v;
        }
        return "";
    }

    public static String chipColor(@Nullable Context context) {
        String v = read(context, PREF_CHIP_COLOR, "#FFFF6B00");
        if (TextUtils.isEmpty(v)) return "#FFFF6B00";
        String t = v.trim();
        if (!t.startsWith("#")) t = "#" + t;
        return t;
    }

    public static float chipSizeSp(@Nullable Context context) {
        try {
            String v = read(context, PREF_CHIP_SIZE_SP, "12");
            float n = Float.parseFloat(v);
            if (n < 8f) return 8f;
            if (n > 22f) return 22f;
            return n;
        } catch (Exception e) {
            return 12f;
        }
    }

    public static boolean chipBold(@Nullable Context context) {
        String w = read(context, PREF_CHIP_WEIGHT, "normal");
        return "bold".equalsIgnoreCase(w) || "700".equals(w) || "thick".equalsIgnoreCase(w);
    }

    private static boolean chipPrefsMatchLanguage(@Nullable Context context) {
        if (context == null) return false;
        try {
            Context app = context.getApplicationContext();
            int saved = SharedPrefsData.getInt(app, PREF_CHIP_LANG, Constants.PREF_NAME);
            int live = currentLanguageId(app);
            // Legacy installs: no stamp (0) but chip prefs filled — treat as mismatch
            // so we never keep Telugu after Hindi/ML until the next splash for live lang.
            if (saved <= 0) return false;
            return saved == live;
        } catch (Exception e) {
            return false;
        }
    }

    private static int currentLanguageId(@Nullable Context context) {
        if (context == null) return 0;
        try {
            return SharedPrefsData.getInt(
                    context.getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME);
        } catch (Exception e) {
            return 0;
        }
    }

    private static String read(@Nullable Context context, String key, String fallback) {
        if (context == null) return fallback;
        try {
            String v = SharedPrefsData.getString(
                    context.getApplicationContext(), key, Constants.PREF_NAME);
            if (TextUtils.isEmpty(v)) return fallback;
            return v;
        } catch (Exception e) {
            return fallback;
        }
    }
}
