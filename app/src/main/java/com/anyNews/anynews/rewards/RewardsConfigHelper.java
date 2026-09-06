package com.anyNews.anynews.rewards;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.SharedPrefsData;

/**
 * Persists splash rewards chip copy/style + joined state for the main-feed slot.
 */
public final class RewardsConfigHelper {

    public static final String PREF_GUEST_CHIP = "rewards_guest_chip";
    public static final String PREF_JOINED_CHIP = "rewards_joined_chip";
    public static final String PREF_RATE_LABEL = "rewards_rate_label";
    public static final String PREF_JOINED = "rewards_user_joined";
    public static final String PREF_CHIP_COLOR = "rewards_chip_color";
    public static final String PREF_CHIP_SIZE_SP = "rewards_chip_size_sp";
    public static final String PREF_CHIP_WEIGHT = "rewards_chip_weight";

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
        return read(context, PREF_GUEST_CHIP, "Join rewards");
    }

    public static String joinedChip(@Nullable Context context) {
        return read(context, PREF_JOINED_CHIP, "Rewards");
    }

    public static String rateLabel(@Nullable Context context) {
        return read(context, PREF_RATE_LABEL, "100 coins = ₹1");
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
