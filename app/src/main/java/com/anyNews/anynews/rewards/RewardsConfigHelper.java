package com.anyNews.anynews.rewards;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.SharedPrefsData;

/**
 * Persists splash rewards chip copy + joined state for the main-feed slot.
 */
public final class RewardsConfigHelper {

    public static final String PREF_GUEST_CHIP = "rewards_guest_chip";
    public static final String PREF_JOINED_CHIP = "rewards_joined_chip";
    public static final String PREF_RATE_LABEL = "rewards_rate_label";
    public static final String PREF_JOINED = "rewards_user_joined";

    private RewardsConfigHelper() {}

    public static void saveChipCopyFromServer(
            @Nullable Context context,
            @Nullable String guestChip,
            @Nullable String joinedChip,
            @Nullable String rateLabel) {
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
