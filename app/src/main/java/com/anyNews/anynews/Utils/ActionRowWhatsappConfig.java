package com.anyNews.anynews.Utils;

import android.content.Context;

import androidx.annotation.Nullable;

/**
 * Likes-row and full-post side WhatsApp (icon and count). Not the footer icon.
 *
 * <p>New APK reads {@code action_row_whatsapp_half_enabled} (half videos and
 * half articles) and {@code action_row_whatsapp_full_enabled} (full image,
 * full video, and any other card that uses that same side icon). If a
 * splash or feed payload only has the legacy {@code action_row_whatsapp_enabled},
 * both sides follow it. Default ON.
 */
public final class ActionRowWhatsappConfig {

    private ActionRowWhatsappConfig() {
    }

    /**
     * Persist splash / session / feed-poll flags. Null fields are omitted by
     * an older server and must not wipe a cached value. A legacy-only payload
     * copies that boolean onto both card types.
     */
    public static void saveFromServer(
            @Nullable Context context,
            @Nullable Boolean legacy,
            @Nullable Boolean full,
            @Nullable Boolean half) {
        if (context == null) {
            return;
        }
        if (legacy == null && full == null && half == null) {
            return;
        }
        boolean legacyOn = legacy != null
                ? Boolean.TRUE.equals(legacy)
                : isLegacyEnabled(context);
        if (legacy != null) {
            SharedPrefsData.putBool(
                    context, Constants.ACTION_ROW_WHATSAPP_ENABLED, legacyOn, Constants.PREF_NAME);
        }
        if (full != null) {
            SharedPrefsData.putBool(
                    context,
                    Constants.ACTION_ROW_WHATSAPP_FULL_ENABLED,
                    Boolean.TRUE.equals(full),
                    Constants.PREF_NAME);
        } else if (legacy != null) {
            SharedPrefsData.putBool(
                    context,
                    Constants.ACTION_ROW_WHATSAPP_FULL_ENABLED,
                    legacyOn,
                    Constants.PREF_NAME);
        }
        if (half != null) {
            SharedPrefsData.putBool(
                    context,
                    Constants.ACTION_ROW_WHATSAPP_HALF_ENABLED,
                    Boolean.TRUE.equals(half),
                    Constants.PREF_NAME);
        } else if (legacy != null) {
            SharedPrefsData.putBool(
                    context,
                    Constants.ACTION_ROW_WHATSAPP_HALF_ENABLED,
                    legacyOn,
                    Constants.PREF_NAME);
        }
    }

    /** Legacy global. Used when a split flag has never been stored on device. Default ON. */
    public static boolean isLegacyEnabled(@Nullable Context context) {
        if (context == null) {
            return true;
        }
        return SharedPrefsData.getBool(
                context, Constants.ACTION_ROW_WHATSAPP_ENABLED, Constants.PREF_NAME, true);
    }

    /** Full image, full video, and other full cards that use the side WhatsApp icon. */
    public static boolean isFullEnabled(@Nullable Context context) {
        return readSplitOrLegacy(context, Constants.ACTION_ROW_WHATSAPP_FULL_ENABLED);
    }

    /** Half videos and half articles — likes-row WhatsApp icon and count. */
    public static boolean isHalfEnabled(@Nullable Context context) {
        return readSplitOrLegacy(context, Constants.ACTION_ROW_WHATSAPP_HALF_ENABLED);
    }

    private static boolean prefsContain(@Nullable Context context, String key) {
        if (context == null || key == null) {
            return false;
        }
        try {
            return context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
                    .contains(key);
        } catch (Exception ignored) {
            return false;
        }
    }

    private static boolean readSplitOrLegacy(@Nullable Context context, String splitKey) {
        if (context == null) {
            return true;
        }
        if (prefsContain(context, splitKey)) {
            return SharedPrefsData.getBool(context, splitKey, Constants.PREF_NAME, true);
        }
        return isLegacyEnabled(context);
    }
}
