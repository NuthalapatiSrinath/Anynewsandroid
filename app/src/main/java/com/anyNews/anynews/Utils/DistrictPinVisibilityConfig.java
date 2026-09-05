package com.anyNews.anynews.Utils;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.Nullable;

/**
 * Admin toggle: hide district pin/label on main News feed; show for Local only.
 * Values arrive via splash / session: {@code hide_district_on_main_feed}.
 *
 * <ul>
 *   <li>ON (default) — main feed ({@code category_id != -1}) hides pin+label;
 *       Local feed ({@code category_id == -1}) still shows them.</li>
 *   <li>OFF — previous behavior: show district on main feed too.</li>
 * </ul>
 */
public final class DistrictPinVisibilityConfig {

    /** Default ON: preferred product behavior. */
    public static final boolean DEFAULT_HIDE_ON_MAIN_FEED = true;

    private DistrictPinVisibilityConfig() {}

    /** Persist splash/session value when present (null = old server; leave cache). */
    public static void saveFromServer(@Nullable Context context, @Nullable Boolean hideOnMainFeed) {
        if (context == null || hideOnMainFeed == null) return;
        SharedPrefsData.putString(
                context.getApplicationContext(),
                Constants.HIDE_DISTRICT_ON_MAIN_FEED,
                hideOnMainFeed ? "true" : "false",
                Constants.PREF_NAME);
    }

    /** True when admin wants district hidden on main News (default true). */
    public static boolean hideOnMainFeed(@Nullable Context context) {
        if (context == null) return DEFAULT_HIDE_ON_MAIN_FEED;
        try {
            String v =
                    SharedPrefsData.getString(
                            context.getApplicationContext(),
                            Constants.HIDE_DISTRICT_ON_MAIN_FEED,
                            Constants.PREF_NAME);
            if (TextUtils.isEmpty(v)) return DEFAULT_HIDE_ON_MAIN_FEED;
            return "true".equalsIgnoreCase(v.trim()) || "1".equals(v.trim());
        } catch (Exception ignored) {
            return DEFAULT_HIDE_ON_MAIN_FEED;
        }
    }

    /**
     * Whether the half-card district pill should be shown for this feed context.
     * Local feed = {@code categoryId == -1} (Mana Zilla / Local Varthalu).
     */
    public static boolean shouldShowDistrict(@Nullable Context context, int categoryId) {
        if (!hideOnMainFeed(context)) {
            return true;
        }
        return categoryId == -1;
    }
}
