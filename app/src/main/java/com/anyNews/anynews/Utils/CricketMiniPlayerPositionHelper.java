package com.anyNews.anynews.Utils;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.anyNews.anynews.Pojo.CricketMiniPlayerPosition;
import com.google.gson.Gson;

/**
 * Caches and reads the admin cricket mini-player position from SharedPrefs
 * (splash + /cricket_active_match poll). Missing / use_default → null so the
 * manager keeps the hard-coded bottom-right placement.
 */
public final class CricketMiniPlayerPositionHelper {

    private static final Gson GSON = new Gson();

    private CricketMiniPlayerPositionHelper() {
    }

    public static void save(@Nullable Context context, @Nullable CricketMiniPlayerPosition position) {
        if (context == null || position == null) return;
        try {
            SharedPrefsData.putString(
                    context.getApplicationContext(),
                    Constants.CRICKET_MINI_PLAYER_POSITION,
                    GSON.toJson(position),
                    Constants.PREF_NAME
            );
        } catch (Exception ignored) {
        }
    }

    /**
     * @return custom position, or null when unset / use_default (caller uses hard-coded).
     */
    @Nullable
    public static CricketMiniPlayerPosition loadCustom(@Nullable Context context) {
        if (context == null) return null;
        try {
            String json = SharedPrefsData.getString(
                    context.getApplicationContext(),
                    Constants.CRICKET_MINI_PLAYER_POSITION,
                    Constants.PREF_NAME
            );
            if (TextUtils.isEmpty(json)) return null;
            CricketMiniPlayerPosition pos = GSON.fromJson(json, CricketMiniPlayerPosition.class);
            if (pos == null || pos.isUseDefault()) return null;
            return pos;
        } catch (Exception ignored) {
            return null;
        }
    }
}
