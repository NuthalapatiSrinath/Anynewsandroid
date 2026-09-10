package com.anyNews.anynews.rewards;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.SharedPrefsData;

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

    private RewardsConfigHelper() {}

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
