package com.anyNews.anynews.Utils;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.Nullable;

/**
 * District pin / Rewards / Flash News chip visibility on half-article cards.
 * Values arrive via splash / session:
 * {@code hide_district_on_main_feed}, {@code rewards_master},
 * {@code rewards_in_main_feed_slot}, {@code flash_news_in_main_feed_slot},
 * {@code flash_news_chip_label}.
 *
 * <p>Visibility matrix:
 * <ul>
 *   <li>Local feed ({@code categoryId == -1}) — always show district; never promo chips.</li>
 *   <li>Main + district/local post ({@code dist_id > 0} or post category 11) — always
 *       show that district (even when hide-district is ON).</li>
 *   <li>Main + non-district — hide when hide-district ON; otherwise show as before.</li>
 *   <li>Rewards chip — hide ON, not district, master ON, slot ON, user not joined.</li>
 *   <li>Flash News chip — hide ON, not district, flash slot ON, Rewards not taking slot
 *       (prefer Flash after join).</li>
 * </ul>
 */
public final class DistrictPinVisibilityConfig {

    /** Default ON: preferred product behavior. */
    public static final boolean DEFAULT_HIDE_ON_MAIN_FEED = true;
    /** Rewards feature master — default OFF until product enables it. */
    public static final boolean DEFAULT_REWARDS_MASTER = false;
    /** Show rewards promo in main-feed district slot when district would be hidden. */
    public static final boolean DEFAULT_REWARDS_IN_MAIN_FEED_SLOT = false;
    /** Show Flash News chip in the same slot when Rewards is not taking it. */
    public static final boolean DEFAULT_FLASH_IN_MAIN_FEED_SLOT = true;
    /** Default ON: reporters excluded from rewards / rewards wallet. */
    public static final boolean DEFAULT_EXCLUDE_REPORTERS = true;

    /** Local desk category id (server {@code categoryMap.local}). */
    public static final int LOCAL_CATEGORY_ID = 11;

    private DistrictPinVisibilityConfig() {}

    /** Persist splash/session values when present (null = old server; leave cache). */
    public static void saveFromServer(
            @Nullable Context context,
            @Nullable Boolean hideOnMainFeed,
            @Nullable Boolean rewardsMaster,
            @Nullable Boolean rewardsInMainFeedSlot) {
        saveFromServer(
                context, hideOnMainFeed, rewardsMaster, rewardsInMainFeedSlot, null, null);
    }

    public static void saveFromServer(
            @Nullable Context context,
            @Nullable Boolean hideOnMainFeed,
            @Nullable Boolean rewardsMaster,
            @Nullable Boolean rewardsInMainFeedSlot,
            @Nullable Boolean excludeReporters) {
        saveFromServer(
                context,
                hideOnMainFeed,
                rewardsMaster,
                rewardsInMainFeedSlot,
                excludeReporters,
                null);
    }

    public static void saveFromServer(
            @Nullable Context context,
            @Nullable Boolean hideOnMainFeed,
            @Nullable Boolean rewardsMaster,
            @Nullable Boolean rewardsInMainFeedSlot,
            @Nullable Boolean excludeReporters,
            @Nullable Boolean flashInMainFeedSlot) {
        if (context == null) return;
        Context app = context.getApplicationContext();
        if (hideOnMainFeed != null) {
            SharedPrefsData.putString(
                    app,
                    Constants.HIDE_DISTRICT_ON_MAIN_FEED,
                    hideOnMainFeed ? "true" : "false",
                    Constants.PREF_NAME);
        }
        if (rewardsMaster != null) {
            SharedPrefsData.putString(
                    app,
                    Constants.REWARDS_MASTER,
                    rewardsMaster ? "true" : "false",
                    Constants.PREF_NAME);
        }
        if (rewardsInMainFeedSlot != null) {
            SharedPrefsData.putString(
                    app,
                    Constants.REWARDS_IN_MAIN_FEED_SLOT,
                    rewardsInMainFeedSlot ? "true" : "false",
                    Constants.PREF_NAME);
        }
        if (flashInMainFeedSlot != null) {
            SharedPrefsData.putString(
                    app,
                    Constants.FLASH_NEWS_IN_MAIN_FEED_SLOT,
                    flashInMainFeedSlot ? "true" : "false",
                    Constants.PREF_NAME);
        }
        if (excludeReporters != null) {
            // commit() so Profile / feed gates see the new value immediately
            // (apply() can race with onResume after session heartbeat).
            SharedPrefsData.putStringImmediate(
                    app,
                    Constants.REWARDS_EXCLUDE_REPORTERS,
                    excludeReporters ? "true" : "false",
                    Constants.PREF_NAME);
        }
    }

    /** Backward-compatible overload used by older call sites. */
    public static void saveFromServer(@Nullable Context context, @Nullable Boolean hideOnMainFeed) {
        saveFromServer(context, hideOnMainFeed, null, null);
    }

    /** True when admin wants district hidden on main News (default true). */
    public static boolean hideOnMainFeed(@Nullable Context context) {
        return readBool(context, Constants.HIDE_DISTRICT_ON_MAIN_FEED, DEFAULT_HIDE_ON_MAIN_FEED);
    }

    /** Master switch for rewards UI (Profile row + optional main-feed slot). */
    public static boolean rewardsMasterEnabled(@Nullable Context context) {
        return readBool(context, Constants.REWARDS_MASTER, DEFAULT_REWARDS_MASTER);
    }

    /** When true, logged-in reporters must not see rewards / rewards wallet. */
    public static boolean excludeReporters(@Nullable Context context) {
        return readBool(
                context, Constants.REWARDS_EXCLUDE_REPORTERS, DEFAULT_EXCLUDE_REPORTERS);
    }

    /** True when this device user may use rewards (master ON and not an excluded reporter). */
    public static boolean rewardsVisibleForUser(@Nullable Context context) {
        if (!rewardsMasterEnabled(context)) return false;
        if (!excludeReporters(context)) return true;
        try {
            boolean isReporter =
                    SharedPrefsData.getBool(
                            context.getApplicationContext(),
                            Constants.is_reporter,
                            Constants.PREF_NAME);
            return !isReporter;
        } catch (Exception ignored) {
            return true;
        }
    }

    /**
     * When true (and rewards master ON), main feed may show a rewards promo in the
     * district slot for non-district posts while hide-district is ON — only if the
     * user has not joined Rewards yet.
     */
    public static boolean rewardsInMainFeedSlotEnabled(@Nullable Context context) {
        return readBool(
                context, Constants.REWARDS_IN_MAIN_FEED_SLOT, DEFAULT_REWARDS_IN_MAIN_FEED_SLOT);
    }

    /** Admin toggle: Flash News chip in the main-feed right slot. */
    public static boolean flashInMainFeedSlotEnabled(@Nullable Context context) {
        return readBool(
                context, Constants.FLASH_NEWS_IN_MAIN_FEED_SLOT, DEFAULT_FLASH_IN_MAIN_FEED_SLOT);
    }

    /** Local / Mana Zilla feed surface. */
    public static boolean isLocalFeed(int feedCategoryId) {
        return feedCategoryId == -1;
    }

    /**
     * Post is district/local news that must keep its district pin even on main feed
     * when hide-district is ON.
     */
    public static boolean isDistrictOrLocalPost(@Nullable ModelClass.Datum post) {
        if (post == null) return false;
        try {
            Integer distId = post.getDist_id();
            if (distId != null && distId > 0) return true;
        } catch (Exception ignored) {
        }
        try {
            Integer cat = post.getCategoryId();
            if (cat != null && cat == LOCAL_CATEGORY_ID) return true;
        } catch (Exception ignored) {
        }
        return false;
    }

    /**
     * Whether the half-card district pill should show the district label.
     * Prefer {@link #shouldShowDistrict(Context, int, ModelClass.Datum)}.
     */
    public static boolean shouldShowDistrict(@Nullable Context context, int categoryId) {
        return shouldShowDistrict(context, categoryId, null);
    }

    /**
     * District label visibility for this feed + post.
     * Local feed always; district/local posts always; otherwise respect hide toggle.
     */
    public static boolean shouldShowDistrict(
            @Nullable Context context, int feedCategoryId, @Nullable ModelClass.Datum post) {
        if (isLocalFeed(feedCategoryId)) {
            return true;
        }
        if (isDistrictOrLocalPost(post)) {
            return true;
        }
        return !hideOnMainFeed(context);
    }

    /** Logged-in user who has already joined Rewards (Profile owns Rewards UI). */
    public static boolean rewardsJoined(@Nullable Context context) {
        if (context == null) return false;
        boolean loggedIn = false;
        try {
            loggedIn =
                    SharedPrefsData.getBool(
                            context.getApplicationContext(),
                            Constants.is_login,
                            Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
        if (!loggedIn) return false;
        try {
            return com.anyNews.anynews.rewards.RewardsConfigHelper.isJoined(context);
        } catch (Exception ignored) {
            return false;
        }
    }

    /**
     * Show Rewards promo in the district slot (main feed only).
     * Never on Local; never when the post keeps its district; never after join.
     */
    public static boolean shouldShowRewardsInDistrictSlot(
            @Nullable Context context, int feedCategoryId, @Nullable ModelClass.Datum post) {
        if (isLocalFeed(feedCategoryId)) return false;
        if (isDistrictOrLocalPost(post)) return false;
        if (!hideOnMainFeed(context)) return false;
        if (!rewardsMasterEnabled(context)) return false;
        if (!rewardsVisibleForUser(context)) return false;
        if (!rewardsInMainFeedSlotEnabled(context)) return false;
        // Joined users use Profile — do not keep showing Rewards in the News slot.
        if (rewardsJoined(context)) return false;
        return true;
    }

    /**
     * Show Flash News chip in the same right slot (main feed only).
     * Prefer Flash when Rewards is not taking the slot (including after join).
     */
    public static boolean shouldShowFlashInDistrictSlot(
            @Nullable Context context, int feedCategoryId, @Nullable ModelClass.Datum post) {
        if (isLocalFeed(feedCategoryId)) return false;
        if (isDistrictOrLocalPost(post)) return false;
        if (!hideOnMainFeed(context)) return false;
        if (!flashInMainFeedSlotEnabled(context)) return false;
        // Rewards promo wins while the user has not joined yet.
        if (shouldShowRewardsInDistrictSlot(context, feedCategoryId, post)) return false;
        return true;
    }

    private static boolean readBool(
            @Nullable Context context, String key, boolean defaultValue) {
        if (context == null) return defaultValue;
        try {
            String v =
                    SharedPrefsData.getString(
                            context.getApplicationContext(), key, Constants.PREF_NAME);
            if (TextUtils.isEmpty(v)) return defaultValue;
            return "true".equalsIgnoreCase(v.trim()) || "1".equals(v.trim());
        } catch (Exception ignored) {
            return defaultValue;
        }
    }
}
