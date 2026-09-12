package com.anyNews.anynews.Utils;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.Nullable;

/**
 * District pin / Rewards / Flash News chip visibility on half-article cards.
 * Values arrive via splash / session:
 * {@code hide_district_on_main_feed}, {@code rewards_master},
 * {@code show_wallet_in_profile}, {@code rewards_in_main_feed_slot},
 * {@code keep_rewards_after_join},
 * {@code flash_news_in_main_feed_slot}, {@code show_flash_and_rewards_together},
 * {@code flash_news_chip_label}, {@code dual_chip_size_dp},
 * {@code dual_chip_shift_left_dp}.
 *
 * <p>{@code rewards_master} gates Profile Rewards + feed chips only — not Wallet.
 * {@code show_wallet_in_profile} gates Profile Wallet (coins + convert); default ON.
 * <p>Visibility matrix:
 * <ul>
 *   <li>Local feed ({@code categoryId == -1}) — always show district; never promo chips.</li>
 *   <li>Main + district/local post ({@code dist_id > 0} or post category 11) — always
 *       show that district (even when hide-district is ON).</li>
 *   <li>Main + non-district — hide when hide-district ON; otherwise show as before.</li>
 *   <li>Show both ON (and Rewards master ON) — Flash (if Flash News is enabled
 *       for the selected language) and Rewards (join text, or Rewards chip after
 *       join) at the same time. Does not use the single-chip matrix below.</li>
 *   <li>Rewards chip — hide ON, not district, master ON, slot ON; until join, or after
 *       join when keep_rewards_after_join ON. Show-both OFF only.</li>
 *   <li>Flash News chip — hide ON, not district, flash slot ON, Rewards not taking slot
 *       (prefer Flash after join unless keep_rewards_after_join). Show-both OFF only.</li>
 * </ul>
 */
public final class DistrictPinVisibilityConfig {

    /** Default ON: preferred product behavior. */
    public static final boolean DEFAULT_HIDE_ON_MAIN_FEED = true;
    /** Rewards feature master — default OFF until product enables it. */
    public static final boolean DEFAULT_REWARDS_MASTER = false;
    /**
     * Profile wallet (coins + convert). Default ON so mid-program users keep
     * convert when Rewards master is turned OFF. Missing key → ON.
     */
    public static final boolean DEFAULT_SHOW_WALLET_IN_PROFILE = true;
    /** Show rewards promo in main-feed district slot when district would be hidden. */
    public static final boolean DEFAULT_REWARDS_IN_MAIN_FEED_SLOT = false;
    /** Keep Rewards chip after join (instead of Flash / empty). Default OFF. */
    public static final boolean DEFAULT_KEEP_REWARDS_AFTER_JOIN = false;
    /** Show Flash News chip in the same slot when Rewards is not taking it. */
    public static final boolean DEFAULT_FLASH_IN_MAIN_FEED_SLOT = true;
    /**
     * Show Flash News and Rewards together. Default OFF.
     * Forced off when Rewards master is OFF.
     */
    public static final boolean DEFAULT_SHOW_FLASH_AND_REWARDS_TOGETHER = false;
    /**
     * Pair text size (dp). Old dual layout hardcoded 9; 13 stays readable
     * without stacking. Clamped 8–24.
     */
    public static final int DEFAULT_PAIR_SIZE_DP = 13;
    /**
     * Extra end margin (dp) that shifts the pair left only. 0 keeps the
     * layout end margin. Never negative. Clamped 0–96.
     */
    public static final int DEFAULT_PAIR_SHIFT_LEFT_DP = 0;
    /**
     * Extra lift (dp) that moves the pair up. 0 keeps the writers-row
     * baseline. Never negative. Clamped 0–48.
     */
    public static final int DEFAULT_PAIR_SHIFT_UP_DP = 0;
    /**
     * Once both + Rewards master are ON this process, keep both chips until a
     * fresh config explicitly turns both off (or master off). A later bind must
     * not drop Rewards because a poll omitted the flag or a recycle took the
     * single-slot path against a stale pref.
     */
    private static volatile boolean sessionBothLatched = false;
    /** Process latch: a fresh config said Rewards master is ON. */
    private static volatile boolean sessionMasterLatched = false;
    /**
     * Only hide reporters when splash/session explicitly saved exclude=true.
     * A missing key must not hide the whole feature (Allow reporters / unset).
     */
    public static final boolean DEFAULT_EXCLUDE_REPORTERS = false;

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
                context, hideOnMainFeed, rewardsMaster, rewardsInMainFeedSlot, null, null, null);
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
                null,
                null);
    }

    public static void saveFromServer(
            @Nullable Context context,
            @Nullable Boolean hideOnMainFeed,
            @Nullable Boolean rewardsMaster,
            @Nullable Boolean rewardsInMainFeedSlot,
            @Nullable Boolean excludeReporters,
            @Nullable Boolean flashInMainFeedSlot) {
        saveFromServer(
                context,
                hideOnMainFeed,
                rewardsMaster,
                rewardsInMainFeedSlot,
                excludeReporters,
                flashInMainFeedSlot,
                null);
    }

    public static void saveFromServer(
            @Nullable Context context,
            @Nullable Boolean hideOnMainFeed,
            @Nullable Boolean rewardsMaster,
            @Nullable Boolean rewardsInMainFeedSlot,
            @Nullable Boolean excludeReporters,
            @Nullable Boolean flashInMainFeedSlot,
            @Nullable Boolean keepRewardsAfterJoin) {
        saveFromServer(
                context,
                hideOnMainFeed,
                rewardsMaster,
                rewardsInMainFeedSlot,
                excludeReporters,
                flashInMainFeedSlot,
                keepRewardsAfterJoin,
                null);
    }

    public static void saveFromServer(
            @Nullable Context context,
            @Nullable Boolean hideOnMainFeed,
            @Nullable Boolean rewardsMaster,
            @Nullable Boolean rewardsInMainFeedSlot,
            @Nullable Boolean excludeReporters,
            @Nullable Boolean flashInMainFeedSlot,
            @Nullable Boolean keepRewardsAfterJoin,
            @Nullable Boolean showFlashAndRewardsTogether) {
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
            // commit() so Profile / feed gates see master before the next paint.
            // apply() can lose the race and leave the missing-key default (OFF).
            SharedPrefsData.putStringImmediate(
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
        if (keepRewardsAfterJoin != null) {
            SharedPrefsData.putString(
                    app,
                    Constants.KEEP_REWARDS_AFTER_JOIN,
                    keepRewardsAfterJoin ? "true" : "false",
                    Constants.PREF_NAME);
        }
        if (flashInMainFeedSlot != null) {
            SharedPrefsData.putString(
                    app,
                    Constants.FLASH_NEWS_IN_MAIN_FEED_SLOT,
                    flashInMainFeedSlot ? "true" : "false",
                    Constants.PREF_NAME);
        }
        if (showFlashAndRewardsTogether != null) {
            SharedPrefsData.putStringImmediate(
                    app,
                    Constants.SHOW_FLASH_AND_REWARDS_TOGETHER,
                    showFlashAndRewardsTogether ? "true" : "false",
                    Constants.PREF_NAME);
        }
        // Server only serves show-both as true when Rewards master is ON.
        // If that flag arrived and master was omitted, persist master so Profile
        // does not keep trusting a missing key.
        if ((Boolean.TRUE.equals(showFlashAndRewardsTogether)
                        || Boolean.TRUE.equals(rewardsInMainFeedSlot))
                && rewardsMaster == null) {
            SharedPrefsData.putStringImmediate(
                    app,
                    Constants.REWARDS_MASTER,
                    "true",
                    Constants.PREF_NAME);
            rewardsMaster = Boolean.TRUE;
        }
        applySessionLatch(context, rewardsMaster, showFlashAndRewardsTogether);
        // Settings switch defaults ON. If it was never written, persist that so
        // Profile and the feed chip read the same ON the switch shows.
        if ((Boolean.TRUE.equals(rewardsMaster) || sessionMasterLatched)
                && userRewardsPrefMissing(app)) {
            setUserRewardsEnabled(app, true);
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

    /**
     * Persist Profile wallet visibility. Null leaves the cached value (poll must
     * not wipe to OFF). Missing key reads as ON ({@link #DEFAULT_SHOW_WALLET_IN_PROFILE}).
     */
    public static void saveWalletVisibilityFromServer(
            @Nullable Context context, @Nullable Boolean showWalletInProfile) {
        if (context == null || showWalletInProfile == null) return;
        SharedPrefsData.putStringImmediate(
                context.getApplicationContext(),
                Constants.SHOW_WALLET_IN_PROFILE,
                showWalletInProfile ? "true" : "false",
                Constants.PREF_NAME);
    }

    /** Backward-compatible overload used by older call sites. */
    public static void saveFromServer(@Nullable Context context, @Nullable Boolean hideOnMainFeed) {
        saveFromServer(context, hideOnMainFeed, null, null);
    }

    /**
     * Pair size (dp) and left-only shift (dp). Null leaves the cached value.
     * A later poll that omits these must not reset size or shift.
     */
    public static void savePairLayoutFromServer(
            @Nullable Context context,
            @Nullable Integer sizeDp,
            @Nullable Integer shiftLeftDp) {
        savePairLayoutFromServer(context, sizeDp, shiftLeftDp, null);
    }

    /**
     * Pair size, left shift, and up shift. Null leaves the cached value.
     * A later poll that omits these must not reset size or shift.
     */
    public static void savePairLayoutFromServer(
            @Nullable Context context,
            @Nullable Integer sizeDp,
            @Nullable Integer shiftLeftDp,
            @Nullable Integer shiftUpDp) {
        if (context == null) return;
        Context app = context.getApplicationContext();
        if (sizeDp != null) {
            SharedPrefsData.putString(
                    app,
                    Constants.DUAL_CHIP_SIZE_DP,
                    String.valueOf(clampPairSizeDp(sizeDp)),
                    Constants.PREF_NAME);
        }
        if (shiftLeftDp != null) {
            SharedPrefsData.putString(
                    app,
                    Constants.DUAL_CHIP_SHIFT_LEFT_DP,
                    String.valueOf(clampPairShiftLeftDp(shiftLeftDp)),
                    Constants.PREF_NAME);
        }
        if (shiftUpDp != null) {
            SharedPrefsData.putString(
                    app,
                    Constants.DUAL_CHIP_SHIFT_UP_DP,
                    String.valueOf(clampPairShiftUpDp(shiftUpDp)),
                    Constants.PREF_NAME);
        }
    }

    /**
     * Latch both-on for this process when a fresh config says both + master ON.
     * Clear only when that config explicitly turns both off, or turns master off.
     * Null fields do not clear the latch (omitted poll must not drop Rewards).
     */
    private static void applySessionLatch(
            @Nullable Context context,
            @Nullable Boolean rewardsMaster,
            @Nullable Boolean showBoth) {
        if (rewardsMaster != null && !rewardsMaster) {
            sessionBothLatched = false;
            sessionMasterLatched = false;
            return;
        }
        if (Boolean.TRUE.equals(rewardsMaster) || Boolean.TRUE.equals(showBoth)) {
            sessionMasterLatched = true;
        }
        if (showBoth == null) return;
        boolean masterOn =
                rewardsMaster != null ? rewardsMaster : rewardsMasterEnabled(context);
        if (!masterOn) {
            sessionBothLatched = false;
            return;
        }
        if (showBoth) {
            sessionBothLatched = true;
        } else {
            sessionBothLatched = false;
        }
    }

    /**
     * Settings “Allow reporters” and splash {@code rewards_exclude_reporters}
     * write the same gate the hub reads. Allow ON wins if both arrive.
     */
    public static void saveReporterGate(
            @Nullable Context context,
            @Nullable Boolean excludeReporters,
            @Nullable Boolean allowReporters) {
        if (context == null) return;
        Boolean resolved = excludeReporters;
        if (allowReporters != null) {
            resolved = !allowReporters;
        }
        if (resolved == null) return;
        SharedPrefsData.putStringImmediate(
                context.getApplicationContext(),
                Constants.REWARDS_EXCLUDE_REPORTERS,
                resolved ? "true" : "false",
                Constants.PREF_NAME);
    }

    /** In-app Settings Rewards switch. Writes the flag Profile and the feed chip read. */
    public static void setUserRewardsEnabled(@Nullable Context context, boolean on) {
        if (context == null) return;
        SharedPrefsData.putStringImmediate(
                context.getApplicationContext(),
                Constants.USER_REWARDS_ENABLED,
                on ? "true" : "false",
                Constants.PREF_NAME);
    }

    /** True when admin wants district hidden on main News (default true). */
    public static boolean hideOnMainFeed(@Nullable Context context) {
        return readBool(context, Constants.HIDE_DISTRICT_ON_MAIN_FEED, DEFAULT_HIDE_ON_MAIN_FEED);
    }

    /**
     * Master switch for rewards UI (Profile rewards row + optional main-feed slot).
     * Does not control Profile wallet — see {@link #walletInProfileEnabled}.
     * Missing key is not a hard OFF when a sibling flag only the server sends
     * with master ON is already true (show-both / feed slot). A boolean stored
     * by the Settings switch is honored — not only the splash string.
     */
    public static boolean rewardsMasterEnabled(@Nullable Context context) {
        if (sessionMasterLatched) return true;
        Boolean stored = readBoolOrMissing(context, Constants.REWARDS_MASTER);
        if (stored != null) return stored;
        if (sessionBothLatched) return true;
        if (readBool(context, Constants.SHOW_FLASH_AND_REWARDS_TOGETHER, false)) return true;
        if (readBool(context, Constants.REWARDS_IN_MAIN_FEED_SLOT, false)) return true;
        return false;
    }

    /**
     * Admin Profile wallet toggle. Missing key → ON (safe for mid-program convert).
     * Independent of {@link #rewardsMasterEnabled}.
     */
    public static boolean walletInProfileEnabled(@Nullable Context context) {
        return readBool(
                context, Constants.SHOW_WALLET_IN_PROFILE, DEFAULT_SHOW_WALLET_IN_PROFILE);
    }

    /**
     * True when this device user may see Profile wallet (admin wallet ON and not
     * an excluded reporter). Does not require rewards_master or the Settings
     * Rewards switch.
     */
    public static boolean walletVisibleForUser(@Nullable Context context) {
        if (!walletInProfileEnabled(context)) return false;
        if (!excludeReporters(context)) return true;
        if (!isLoggedIn(context)) return true;
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
     * In-app Settings Rewards switch. Missing key means ON so the hub does not
     * hide when the switch was never written, or only painted UI.
     */
    public static boolean userRewardsEnabled(@Nullable Context context) {
        return readBool(context, Constants.USER_REWARDS_ENABLED, true);
    }

    /** True when Settings never wrote the Rewards switch (string or boolean). */
    public static boolean userRewardsPrefMissing(@Nullable Context context) {
        return readBoolOrMissing(context, Constants.USER_REWARDS_ENABLED) == null;
    }

    /** When true, logged-in reporters must not see rewards / rewards wallet. */
    public static boolean excludeReporters(@Nullable Context context) {
        return readBool(
                context, Constants.REWARDS_EXCLUDE_REPORTERS, DEFAULT_EXCLUDE_REPORTERS);
    }

    /**
     * True when this device user may use rewards (master ON and not an excluded
     * reporter). Guests are never treated as reporters — a stale
     * {@code is_reporter} flag after logout used to hide Profile + the chip.
     * Ended / upcoming periods do not hide the entry; scoring pause is banner-only.
     */
    public static boolean rewardsVisibleForUser(@Nullable Context context) {
        if (!rewardsMasterEnabled(context)) return false;
        // Default Settings switch is ON. Persist it on first read so Profile
        // and the feed chip do not stay hidden on a missing pref.
        if (userRewardsPrefMissing(context)) {
            setUserRewardsEnabled(context, true);
            return true;
        }
        Boolean userPref = readBoolOrMissing(context, Constants.USER_REWARDS_ENABLED);
        // Settings switch OFF hides Profile + chip even when admin master is ON.
        if (Boolean.FALSE.equals(userPref)) return false;
        // Settings switch ON is the flag the hub reads — show even if a stale
        // reporter-exclude pref is still true.
        if (Boolean.TRUE.equals(userPref)) return true;
        if (!excludeReporters(context)) return true;
        if (!isLoggedIn(context)) return true;
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
     * district slot for non-district posts while hide-district is ON — until join,
     * or after join when {@link #keepRewardsAfterJoinEnabled} is ON.
     */
    public static boolean rewardsInMainFeedSlotEnabled(@Nullable Context context) {
        return readBool(
                context, Constants.REWARDS_IN_MAIN_FEED_SLOT, DEFAULT_REWARDS_IN_MAIN_FEED_SLOT);
    }

    /**
     * Admin toggle: after join, keep Rewards chip in the main-feed slot instead of
     * Flash / empty. Default OFF.
     */
    public static boolean keepRewardsAfterJoinEnabled(@Nullable Context context) {
        return readBool(
                context, Constants.KEEP_REWARDS_AFTER_JOIN, DEFAULT_KEEP_REWARDS_AFTER_JOIN);
    }

    /** Admin toggle: Flash News chip in the main-feed right slot. */
    public static boolean flashInMainFeedSlotEnabled(@Nullable Context context) {
        return readBool(
                context, Constants.FLASH_NEWS_IN_MAIN_FEED_SLOT, DEFAULT_FLASH_IN_MAIN_FEED_SLOT);
    }

    /**
     * Admin toggle: show Flash News and Rewards together.
     * Always false when Rewards master is OFF, even if a stale true was saved.
     */
    public static boolean showFlashAndRewardsTogetherEnabled(@Nullable Context context) {
        if (!rewardsMasterEnabled(context)) return false;
        // Session latch wins over a stale pref until a fresh config turns both off.
        if (sessionBothLatched) return true;
        return readBool(
                context,
                Constants.SHOW_FLASH_AND_REWARDS_TOGETHER,
                DEFAULT_SHOW_FLASH_AND_REWARDS_TOGETHER);
    }

    /** Text size (dp) for the Flash + Rewards pair. Default 13. */
    public static int pairSizeDp(@Nullable Context context) {
        return readInt(
                context,
                Constants.DUAL_CHIP_SIZE_DP,
                DEFAULT_PAIR_SIZE_DP,
                8,
                24);
    }

    /** Extra end margin (dp) that shifts the pair left only. Default 0. */
    public static int pairShiftLeftDp(@Nullable Context context) {
        return readInt(
                context,
                Constants.DUAL_CHIP_SHIFT_LEFT_DP,
                DEFAULT_PAIR_SHIFT_LEFT_DP,
                0,
                96);
    }

    /** Extra lift (dp) that moves the pair up. Default 0. */
    public static int pairShiftUpDp(@Nullable Context context) {
        return readInt(
                context,
                Constants.DUAL_CHIP_SHIFT_UP_DP,
                DEFAULT_PAIR_SHIFT_UP_DP,
                0,
                48);
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
     * Never on Local. District posts and hide-district OFF used to hide the
     * chip even when the Rewards chip toggle was ON — that made Rewards vanish
     * on most cards. Slot ON keeps the chip after join; keep_after_join still
     * forces it when the slot toggle is off. Ended period is not a gate.
     * Single-chip matrix only — show-both uses {@link #shouldShowRewardsWhenBoth}.
     */
    public static boolean shouldShowRewardsInDistrictSlot(
            @Nullable Context context, int feedCategoryId, @Nullable ModelClass.Datum post) {
        if (showFlashAndRewardsTogetherEnabled(context)) return false;
        if (isLocalFeed(feedCategoryId)) return false;
        if (!rewardsMasterEnabled(context)) return false;
        if (!rewardsVisibleForUser(context)) return false;
        boolean slotOn = rewardsInMainFeedSlotEnabled(context);
        boolean keep = keepRewardsAfterJoinEnabled(context);
        if (!slotOn && !keep) return false;
        if (!slotOn && isDistrictOrLocalPost(post)) return false;
        if (!slotOn && !hideOnMainFeed(context)) return false;
        // Slot ON means the chip stays after join. keep_after_join no longer
        // hides a chip the admin already turned on.
        return true;
    }

    /**
     * Show Flash News chip in the same right slot (main feed only).
     * Prefer Flash when Rewards is not taking the slot (including after join,
     * unless keep_rewards_after_join keeps Rewards).
     * Single-chip matrix only — show-both uses {@link #shouldShowFlashWhenBoth}.
     */
    public static boolean shouldShowFlashInDistrictSlot(
            @Nullable Context context, int feedCategoryId, @Nullable ModelClass.Datum post) {
        if (showFlashAndRewardsTogetherEnabled(context)) return false;
        if (isLocalFeed(feedCategoryId)) return false;
        if (isDistrictOrLocalPost(post)) return false;
        if (!hideOnMainFeed(context)) return false;
        if (!flashInMainFeedSlotEnabled(context)) return false;
        // Rewards promo wins while eligible (pre-join, or post-join with keep ON).
        if (shouldShowRewardsInDistrictSlot(context, feedCategoryId, post)) return false;
        return true;
    }

    /**
     * Slot where a promo chip may replace district.
     * Local feed always keeps district. Show-both or the Rewards chip toggle
     * wins on main feed, including district posts — otherwise Rewards never
     * appeared on cards that carry a dist_id.
     */
    public static boolean isPromoSlotEligible(
            @Nullable Context context, int feedCategoryId, @Nullable ModelClass.Datum post) {
        if (isLocalFeed(feedCategoryId)) return false;
        if (showFlashAndRewardsTogetherEnabled(context)
                || rewardsInMainFeedSlotEnabled(context)) {
            return true;
        }
        if (isDistrictOrLocalPost(post)) return false;
        return hideOnMainFeed(context);
    }

    /**
     * Flash chip while show-both is ON. Language gate matches Daily Updates Flash
     * (enabled + allowed language). The single-slot Flash toggle does not hide it.
     */
    public static boolean shouldShowFlashWhenBoth(
            @Nullable Context context, int feedCategoryId, @Nullable ModelClass.Datum post) {
        if (!showFlashAndRewardsTogetherEnabled(context)) return false;
        if (!isPromoSlotEligible(context, feedCategoryId, post)) return false;
        return DailyUpdatesConfigHelper.isFlashNewsEnabled(context);
    }

    /**
     * Rewards chip while show-both is ON. Stays after join (join text vs Rewards
     * chip is copy only). Does not require the Rewards slot toggle.
     */
    public static boolean shouldShowRewardsWhenBoth(
            @Nullable Context context, int feedCategoryId, @Nullable ModelClass.Datum post) {
        if (!showFlashAndRewardsTogetherEnabled(context)) return false;
        if (!isPromoSlotEligible(context, feedCategoryId, post)) return false;
        if (!rewardsMasterEnabled(context)) return false;
        return rewardsVisibleForUser(context);
    }

    /** True when the writers row should bind the dual-chip path (one or both). */
    public static boolean shouldBindDualFeedChips(
            @Nullable Context context, int feedCategoryId, @Nullable ModelClass.Datum post) {
        return shouldShowFlashWhenBoth(context, feedCategoryId, post)
                || shouldShowRewardsWhenBoth(context, feedCategoryId, post);
    }

    private static int clampPairSizeDp(int raw) {
        return Math.max(8, Math.min(24, raw));
    }

    private static int clampPairShiftLeftDp(int raw) {
        return Math.max(0, Math.min(96, raw));
    }

    private static int clampPairShiftUpDp(int raw) {
        return Math.max(0, Math.min(48, raw));
    }

    private static boolean isLoggedIn(@Nullable Context context) {
        if (context == null) return false;
        try {
            return SharedPrefsData.getBool(
                    context.getApplicationContext(), Constants.is_login, Constants.PREF_NAME);
        } catch (Exception ignored) {
            return false;
        }
    }

    private static int readInt(
            @Nullable Context context,
            String key,
            int defaultValue,
            int min,
            int max) {
        if (context == null) return defaultValue;
        try {
            String v =
                    SharedPrefsData.getString(
                            context.getApplicationContext(), key, Constants.PREF_NAME);
            if (TextUtils.isEmpty(v)) return defaultValue;
            int n = Integer.parseInt(v.trim());
            if (n <= 0 && min > 0) return defaultValue;
            return Math.max(min, Math.min(max, n));
        } catch (Exception ignored) {
            return defaultValue;
        }
    }

    private static boolean readBool(
            @Nullable Context context, String key, boolean defaultValue) {
        Boolean stored = readBoolOrMissing(context, key);
        return stored != null ? stored : defaultValue;
    }

    /**
     * null = key missing. Accepts splash strings ("true"/"1") and a boolean
     * written by the Settings switch under the same name.
     */
    @Nullable
    private static Boolean readBoolOrMissing(@Nullable Context context, String key) {
        if (context == null || key == null) return null;
        try {
            android.content.SharedPreferences sp =
                    context.getApplicationContext()
                            .getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
            if (!sp.contains(key)) return null;
            try {
                String v = sp.getString(key, null);
                if (TextUtils.isEmpty(v)) return null;
                return "true".equalsIgnoreCase(v.trim()) || "1".equals(v.trim());
            } catch (ClassCastException ignored) {
                try {
                    return sp.getBoolean(key, false);
                } catch (Exception inner) {
                    return null;
                }
            }
        } catch (Exception ignored) {
            return null;
        }
    }
}
