
package com.anyNews.anynews.Pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Map;

public class StartActivityReqModel implements Serializable {

    public class Data {

        @SerializedName("draft_id")
        @Expose
        private Integer draftId;
        @SerializedName("font_size")
        @Expose
        private Integer fontSize;
        @SerializedName("next_screen")
        @Expose
        private String nextScreen;
        @SerializedName("show_animation")
        @Expose
        private Integer showAnimation;
        @SerializedName("app_update")
        @Expose
        private Boolean appUpdate;
        @SerializedName("language_id")
        @Expose
        private Integer languageId;
        @SerializedName("static_data_api")
        @Expose
        private Integer staticDataApi;
        @SerializedName("dist_name")
        @Expose
        private String distName;
        @SerializedName("dist_id")
        @Expose
        private Integer distId;
        @SerializedName("state_id")
        @Expose
        private Integer stateId;
        @SerializedName("is_login")
        @Expose
        private Boolean isLogin;
        @SerializedName("update_menu_status")
        @Expose
        private Integer updateMenuStatus;

        /**
         * Admin Home update-banner subtitle. Empty/null → use
         * R.string.app_update_available_subtitle.
         */
        @SerializedName("update_banner_subtitle")
        @Expose
        private String updateBannerSubtitle;

        public String getPost_id() {
            return post_id;
        }

        public void setPost_id(String post_id) {
            this.post_id = post_id;
        }

        @SerializedName("post_id")
        @Expose
        private String post_id;


        @SerializedName("event_status")
        @Expose
        private Boolean eventStatus;

        @SerializedName("admob_app_id")
        @Expose
        private String admobAppId;

        @SerializedName("engagement_badge_color")
        @Expose
        private String engagementBadgeColor;

        @SerializedName("theme_color")
        @Expose
        private String themeColor;

        @SerializedName("allow_screen_capture")
        @Expose
        private boolean allowScreenCapture = false;

        public boolean getAllowScreenCapture() {
            return allowScreenCapture;
        }

        public void setAllowScreenCapture(boolean allowScreenCapture) {
            this.allowScreenCapture = allowScreenCapture;
        }

        @SerializedName("action_icon_row_offset")
        @Expose
        private Integer actionIconRowOffset = 0;

        public int getActionIconRowOffset() {
            return actionIconRowOffset != null ? actionIconRowOffset : 0;
        }

        public void setActionIconRowOffset(Integer actionIconRowOffset) {
            this.actionIconRowOffset = actionIconRowOffset;
        }

        @SerializedName("action_icon_size_dp")
        @Expose
        private Integer actionIconSizeDp = 21;

        public int getActionIconSizeDp() {
            return actionIconSizeDp != null ? actionIconSizeDp : 21;
        }

        public void setActionIconSizeDp(Integer actionIconSizeDp) {
            this.actionIconSizeDp = actionIconSizeDp;
        }

        @SerializedName("like_circle_size_dp")
        @Expose
        private Integer likeCircleSizeDp = 35;

        public int getLikeCircleSizeDp() {
            return likeCircleSizeDp != null ? likeCircleSizeDp : 35;
        }

        public void setLikeCircleSizeDp(Integer likeCircleSizeDp) {
            this.likeCircleSizeDp = likeCircleSizeDp;
        }

        @SerializedName("search_icon_on_engagement")
        @Expose
        private Boolean searchIconOnEngagement;

        @SerializedName("search_icon_on_toggle")
        @Expose
        private Boolean searchIconOnToggle;

        @SerializedName("search_icon_size_dp")
        @Expose
        private Integer searchIconSizeDp;

        public Boolean getSearchIconOnEngagement() {
            return searchIconOnEngagement;
        }

        public Boolean getSearchIconOnToggle() {
            return searchIconOnToggle;
        }

        public Integer getSearchIconSizeDp() {
            return searchIconSizeDp;
        }

        @SerializedName("magazine_share_icon_size_dp")
        @Expose
        private Integer magazineShareIconSizeDp = 48;

        public int getMagazineShareIconSizeDp() {
            return magazineShareIconSizeDp != null ? magazineShareIconSizeDp : 48;
        }

        public void setMagazineShareIconSizeDp(Integer magazineShareIconSizeDp) {
            this.magazineShareIconSizeDp = magazineShareIconSizeDp;
        }

        @SerializedName("magazine_share_margin_end_dp")
        @Expose
        private Integer magazineShareMarginEndDp = 20;

        public int getMagazineShareMarginEndDp() {
            return magazineShareMarginEndDp != null ? magazineShareMarginEndDp : 20;
        }

        public void setMagazineShareMarginEndDp(Integer magazineShareMarginEndDp) {
            this.magazineShareMarginEndDp = magazineShareMarginEndDp;
        }

        @SerializedName("magazine_share_margin_bottom_dp")
        @Expose
        private Integer magazineShareMarginBottomDp = 64;

        public int getMagazineShareMarginBottomDp() {
            return magazineShareMarginBottomDp != null ? magazineShareMarginBottomDp : 64;
        }

        public void setMagazineShareMarginBottomDp(Integer magazineShareMarginBottomDp) {
            this.magazineShareMarginBottomDp = magazineShareMarginBottomDp;
        }

        @SerializedName("magazine_share_margin_start_dp")
        @Expose
        private Integer magazineShareMarginStartDp = 0;

        public int getMagazineShareMarginStartDp() {
            return magazineShareMarginStartDp != null ? magazineShareMarginStartDp : 0;
        }

        public void setMagazineShareMarginStartDp(Integer magazineShareMarginStartDp) {
            this.magazineShareMarginStartDp = magazineShareMarginStartDp;
        }

        @SerializedName("magazine_share_margin_top_dp")
        @Expose
        private Integer magazineShareMarginTopDp = 0;

        public int getMagazineShareMarginTopDp() {
            return magazineShareMarginTopDp != null ? magazineShareMarginTopDp : 0;
        }

        public void setMagazineShareMarginTopDp(Integer magazineShareMarginTopDp) {
            this.magazineShareMarginTopDp = magazineShareMarginTopDp;
        }

        @SerializedName("enable_video_post")
        @Expose
        private Boolean enableVideoPost = Boolean.TRUE;

        public boolean getEnableVideoPost() {
            return enableVideoPost == null || enableVideoPost;
        }

        public void setEnableVideoPost(Boolean enableVideoPost) {
            this.enableVideoPost = enableVideoPost;
        }

        @SerializedName("enable_article_post")
        @Expose
        private Boolean enableArticlePost = Boolean.TRUE;

        public boolean getEnableArticlePost() {
            return enableArticlePost == null || enableArticlePost;
        }

        public void setEnableArticlePost(Boolean enableArticlePost) {
            this.enableArticlePost = enableArticlePost;
        }

        @SerializedName("enable_full_post")
        @Expose
        private Boolean enableFullPost = Boolean.TRUE;

        public boolean getEnableFullPost() {
            return enableFullPost == null || enableFullPost;
        }

        public void setEnableFullPost(Boolean enableFullPost) {
            this.enableFullPost = enableFullPost;
        }

        /**
         * Nullable on purpose — default FALSE used to wipe a splash/poll-delivered
         * {@code true} when a partial envelope omitted the key (same class of bug as
         * allow_screen_capture). Only persist when non-null.
         */
        @SerializedName("require_full_profile_to_post")
        @Expose
        private Boolean requireFullProfileToPost;

        /** Null when splash omitted the field — do not overwrite prefs. */
        public Boolean getRequireFullProfileToPost() {
            return requireFullProfileToPost;
        }

        public boolean hasRequireFullProfileToPost() {
            return requireFullProfileToPost != null;
        }

        public void setRequireFullProfileToPost(Boolean requireFullProfileToPost) {
            this.requireFullProfileToPost = requireFullProfileToPost;
        }

        @SerializedName("show_writer_photo_on_cards")
        @Expose
        private Boolean showWriterPhotoOnCards;

        /** Null when splash omitted the field — do not overwrite prefs. */
        public Boolean getShowWriterPhotoOnCards() {
            return showWriterPhotoOnCards;
        }

        public boolean hasShowWriterPhotoOnCards() {
            return showWriterPhotoOnCards != null;
        }

        public void setShowWriterPhotoOnCards(Boolean showWriterPhotoOnCards) {
            this.showWriterPhotoOnCards = showWriterPhotoOnCards;
        }

        @SerializedName("title_body_gap_dp")
        @Expose
        private Integer titleBodyGapDp = 0;

        public int getTitleBodyGapDp() {
            return titleBodyGapDp != null ? titleBodyGapDp : 0;
        }

        public void setTitleBodyGapDp(Integer titleBodyGapDp) {
            this.titleBodyGapDp = titleBodyGapDp;
        }

        @SerializedName("content_block_offset_dp")
        @Expose
        private Integer contentBlockOffsetDp = 0;

        public int getContentBlockOffsetDp() {
            return contentBlockOffsetDp != null ? contentBlockOffsetDp : 0;
        }

        public void setContentBlockOffsetDp(Integer contentBlockOffsetDp) {
            this.contentBlockOffsetDp = contentBlockOffsetDp;
        }

        /** Admin push tray: show article image (default true). Null = not sent / keep prefs. */
        @SerializedName("pn_show_article_image")
        @Expose
        private Boolean pnShowArticleImage;

        @SerializedName("pn_style_mode")
        @Expose
        private String pnStyleMode;

        @SerializedName("pn_large_icon_url")
        @Expose
        private String pnLargeIconUrl;

        @SerializedName("push_include_logo_default")
        @Expose
        private Boolean pushIncludeLogoDefault;

        @SerializedName("pn_left_icon_size_dp")
        @Expose
        private Integer pnLeftIconSizeDp;

        @SerializedName("pn_thumb_size_dp")
        @Expose
        private Integer pnThumbSizeDp;

        @SerializedName("pn_title_size_sp")
        @Expose
        private Integer pnTitleSizeSp;

        public Boolean getPnShowArticleImage() {
            return pnShowArticleImage;
        }

        public String getPnStyleMode() {
            return pnStyleMode;
        }

        public String getPnLargeIconUrl() {
            return pnLargeIconUrl;
        }

        public Boolean getPushIncludeLogoDefault() {
            return pushIncludeLogoDefault;
        }

        public Integer getPnLeftIconSizeDp() {
            return pnLeftIconSizeDp;
        }

        public Integer getPnThumbSizeDp() {
            return pnThumbSizeDp;
        }

        public Integer getPnTitleSizeSp() {
            return pnTitleSizeSp;
        }

        @SerializedName("font_config")
        @Expose
        private LanguageFontConfig fontConfig;

        @SerializedName("language_font_settings")
        @Expose
        private Map<String, LanguageFontConfig> languageFontSettings;

        /** Admin-uploaded fonts: key → public download URL. */
        @SerializedName("custom_font_urls")
        @Expose
        private Map<String, String> customFontUrls;

        @SerializedName("footer_style")
        @Expose
        private FooterStyleConfig footerStyle;

        @SerializedName("writer_strip_style")
        @Expose
        private WriterStripStyleConfig writerStripStyle;

        @SerializedName("full_video_share_card")
        @Expose
        private FullVideoShareCardConfig fullVideoShareCard;

        @SerializedName("poll_survey_colors")
        @Expose
        private PollSurveyColorsConfig pollSurveyColors;

        @SerializedName("cricket_mini_player_position")
        @Expose
        private CricketMiniPlayerPosition cricketMiniPlayerPosition;

        // Independent cricket surface toggles delivered at splash. Null (old
        // server) → mini player enabled / footer tab disabled = current behavior.
        @SerializedName("cricket_mini_player_enabled")
        @Expose
        private Boolean cricketMiniPlayerEnabled;

        @SerializedName("cricket_footer_tab_enabled")
        @Expose
        private Boolean cricketFooterTabEnabled;

        /** Seconds between mini-player polls (only used when mini is ON). */
        @SerializedName("cricket_mini_player_poll_interval_seconds")
        @Expose
        private Integer cricketMiniPlayerPollIntervalSeconds;

        /** Idle Home poll when mini ON but no live match (admin). */
        @SerializedName("cricket_mini_player_idle_poll_interval_seconds")
        @Expose
        private Integer cricketMiniPlayerIdlePollIntervalSeconds;

        @SerializedName("session_heartbeat_interval_seconds")
        @Expose
        private Integer sessionHeartbeatIntervalSeconds;

        /** True-live presence monitoring (admin). Null on old servers. */
        @SerializedName("session_live_enabled")
        @Expose
        private Boolean sessionLiveEnabled;

        /** Presence window seconds (typically interval × 2). */
        @SerializedName("session_live_window_seconds")
        @Expose
        private Integer sessionLiveWindowSeconds;

        /**
         * Offline News pack max articles (admin; default 24, range 8–50).
         * Null on old servers → Android keeps cached / default 24.
         */
        @SerializedName("offline_news_pack_max")
        @Expose
        private Integer offlineNewsPackMax;

        /**
         * First-open auto-hide for top News|Read chrome (admin; default 3, range 0–30).
         * 0 = off. Null on old servers → Android default 3.
         */
        @SerializedName("home_mode_chrome_auto_hide_seconds")
        @Expose
        private Integer homeModeChromeAutoHideSeconds;

        /**
         * Main News FlipView tap mode: classic | reader | hybrid (default hybrid).
         * Null on old servers → Android keeps cached / default hybrid.
         */
        @SerializedName("news_tap_mode")
        @Expose
        private String newsTapMode;

        @SerializedName("news_tap_half_upper_pct")
        @Expose
        private Integer newsTapHalfUpperPct;

        @SerializedName("news_tap_full_upper_pct")
        @Expose
        private Integer newsTapFullUpperPct;

        @SerializedName("news_tap_full_right_rail_pct")
        @Expose
        private Integer newsTapFullRightRailPct;

        /**
         * Half articles: also hide likes/comments/share/flag with writers row.
         * Null on old servers → Android keeps cached / default false.
         */
        @SerializedName("news_tap_half_hide_engagement")
        @Expose
        private Boolean newsTapHalfHideEngagement;

        /**
         * Hide district on main News for non-district posts. Null on old servers.
         * Default ON when missing from prefs. District/local posts still show.
         */
        @SerializedName("hide_district_on_main_feed")
        @Expose
        private Boolean hideDistrictOnMainFeed;

        /** Rewards feature master (Profile rewards + optional feed slot). Null on old servers. */
        @SerializedName("rewards_master")
        @Expose
        private Boolean rewardsMaster;

        /**
         * Profile wallet (coins + convert). Independent of rewards_master.
         * Null on old servers → app default ON.
         */
        @SerializedName("show_wallet_in_profile")
        @Expose
        private Boolean showWalletInProfile;

        /**
         * Show rewards promo in main-feed district slot when district would be hidden.
         * Null on old servers.
         */
        @SerializedName("rewards_in_main_feed_slot")
        @Expose
        private Boolean rewardsInMainFeedSlot;

        /**
         * Keep Rewards chip after join (instead of Flash / empty). Null on old servers.
         */
        @SerializedName("keep_rewards_after_join")
        @Expose
        private Boolean keepRewardsAfterJoin;

        /**
         * Show Flash News chip in main-feed right slot when district would be hidden
         * and Rewards is not taking the slot. Null on old servers.
         */
        @SerializedName("flash_news_in_main_feed_slot")
        @Expose
        private Boolean flashNewsInMainFeedSlot;

        /**
         * Show Flash News and Rewards chips together. Null on old servers.
         * App treats as off when rewards_master is OFF.
         */
        @SerializedName("show_flash_and_rewards_together")
        @Expose
        private Boolean showFlashAndRewardsTogether;

        /** Admin Flash News chip label for this splash language. */
        @SerializedName("flash_news_chip_label")
        @Expose
        private String flashNewsChipLabel;

        /** Dual-chip pair text size (dp). Null on old servers. */
        @SerializedName("dual_chip_size_dp")
        @Expose
        private Integer dualChipSizeDp;

        /** Extra end margin (dp) shifting the pair left only. Null on old servers. */
        @SerializedName("dual_chip_shift_left_dp")
        @Expose
        private Integer dualChipShiftLeftDp;

        /** Extra lift (dp) moving the pair up. Null on old servers. */
        @SerializedName("dual_chip_shift_up_dp")
        @Expose
        private Integer dualChipShiftUpDp;

        /** My Articles date filter From floor (YYYY-MM-DD IST). Empty = none. */
        @SerializedName("reporter_articles_from_date")
        @Expose
        private String reporterArticlesFromDate;

        /** Admin payment banner currently visible (On + within time limit). */
        @SerializedName("reporter_payment_banner_active")
        @Expose
        private Boolean reporterPaymentBannerActive;

        /** Admin payment banner text for this splash language. */
        @SerializedName("reporter_payment_banner_text")
        @Expose
        private String reporterPaymentBannerText;

        /** Permanent notice inside Payment History (admin On + text). */
        @SerializedName("reporter_payment_history_notice_active")
        @Expose
        private Boolean reporterPaymentHistoryNoticeActive;

        @SerializedName("reporter_payment_history_notice_text")
        @Expose
        private String reporterPaymentHistoryNoticeText;

        @SerializedName("rewards_guest_chip")
        @Expose
        private String rewardsGuestChip;

        @SerializedName("rewards_joined_chip")
        @Expose
        private String rewardsJoinedChip;

        @SerializedName("rewards_rate_label")
        @Expose
        private String rewardsRateLabel;

        @SerializedName("rewards_chip_color")
        @Expose
        private String rewardsChipColor;

        @SerializedName("rewards_chip_size_sp")
        @Expose
        private Integer rewardsChipSizeSp;

        @SerializedName("rewards_chip_weight")
        @Expose
        private String rewardsChipWeight;

        @SerializedName("rewards_exclude_reporters")
        @Expose
        private Boolean rewardsExcludeReporters;

        /** Settings Allow reporters. When present, wins over exclude. */
        @SerializedName("allow_reporters")
        @Expose
        private Boolean rewardsAllowReporters;

        @SerializedName("rewards_period_unit")
        @Expose
        private String rewardsPeriodUnit;

        /** JSON string of IST-day factor caps (ads_seen and siblings). */
        @SerializedName("rewards_factor_caps")
        @Expose
        private String rewardsFactorCaps;

        @SerializedName("rewards_ads_seen_cap")
        @Expose
        private Integer rewardsAdsSeenCap;

        @SerializedName("rewards_ads_seen_remaining")
        @Expose
        private Integer rewardsAdsSeenRemaining;

        @SerializedName("rewards_ads_seen_capped")
        @Expose
        private Boolean rewardsAdsSeenCapped;

        @SerializedName("rewards_program_status")
        @Expose
        private String rewardsProgramStatus;

        @SerializedName("rewards_scoring_open")
        @Expose
        private Boolean rewardsScoringOpen;

        @SerializedName("rewards_period_ended")
        @Expose
        private String rewardsPeriodEnded;

        @SerializedName("rewards_period_upcoming")
        @Expose
        private String rewardsPeriodUpcoming;

        @SerializedName("rewards_period_banner")
        @Expose
        private String rewardsPeriodBanner;

        @SerializedName("play_store_package")
        @Expose
        private String playStorePackage;

        /**
         * Read / చదివినవి recency window in hours (admin; default 24, range 1–168).
         * Null on old servers → Android keeps cached / default 24.
         */
        @SerializedName("read_history_hours")
        @Expose
        private Integer readHistoryHours;

        /**
         * Status Videos lookback days (admin; default 5, range 1–30).
         * Null on old servers → Android keeps cached / default 5.
         */
        @SerializedName("status_videos_days")
        @Expose
        private Integer statusVideosDays;

        /**
         * In-feed native ad after every N real articles (admin; default 4, range 2–10).
         * Null on old servers → Android keeps cached / default 4.
         */
        @SerializedName("feed_ad_every_n")
        @Expose
        private Integer feedAdEveryN;

        /**
         * Flip positions ahead to prefetch next native ad (admin; default 6, range 3–12).
         */
        @SerializedName("ad_prefetch_lookahead")
        @Expose
        private Integer adPrefetchLookahead;

        /**
         * Max simultaneous native ad loads (admin; default 3, range 1–5).
         */
        @SerializedName("max_native_ad_loads_in_flight")
        @Expose
        private Integer maxNativeAdLoadsInFlight;

        /**
         * Flush batched real-ad impressions after every N painted ads
         * (admin; default 3, range 1–50). Null on old servers → keep cached / default 3.
         */
        @SerializedName("ad_impr_flush_every_n")
        @Expose
        private Integer adImprFlushEveryN;

        /**
         * Destroy impressed natives on leave (admin; default true / money-safe).
         * Null on old servers → Android keeps cached / default ON.
         */
        @SerializedName("destroy_native_ads_after_impression")
        @Expose
        private Boolean destroyNativeAdsAfterImpression;

        /**
         * Try secondary native after primary no-fill (admin; default true).
         * Null on old servers → Android keeps cached / default ON.
         * Prefer {@link #feedNativeAdUnitMode} when present.
         */
        @SerializedName("use_second_native_ad_unit")
        @Expose
        private Boolean useSecondNativeAdUnit;

        /**
         * primary_only | primary_then_secondary | secondary_only.
         * Null on old servers → derive from {@link #useSecondNativeAdUnit}.
         */
        @SerializedName("feed_native_ad_unit_mode")
        @Expose
        private String feedNativeAdUnitMode;

        /** ViewsBatchTracker flush interval seconds (admin; default 60). */
        @SerializedName("views_batch_flush_interval_seconds")
        @Expose
        private Integer viewsBatchFlushIntervalSeconds;

        /** ViewsBatchTracker early flush at N events (admin; default 12). */
        @SerializedName("views_batch_flush_at_count")
        @Expose
        private Integer viewsBatchFlushAtCount;

        /** MediaAccessReporter flush interval seconds (admin; default 60). */
        @SerializedName("media_access_flush_interval_seconds")
        @Expose
        private Integer mediaAccessFlushIntervalSeconds;

        /**
         * RewardsEngagementBatcher flush interval seconds (admin; default 45).
         * Only applies when rewards ON + joined.
         */
        @SerializedName("rewards_engagement_flush_interval_seconds")
        @Expose
        private Integer rewardsEngagementFlushIntervalSeconds;

        /**
         * Between-news format: native | interstitial (admin; default native).
         * Null on old servers → Android keeps cached / native.
         */
        @SerializedName("feed_ad_format")
        @Expose
        private String feedAdFormat;

        /**
         * Between-news AdMob unit (Root_Settings.ad_code). Null → keep cached / default native unit.
         */
        @SerializedName("ad_code")
        @Expose
        private String adCode;

        /**
         * Secondary native unit for no-fill waterfall (Root_Settings.ad_code_secondary).
         * Null → keep cached / default secondary unit.
         */
        @SerializedName("ad_code_secondary")
        @Expose
        private String adCodeSecondary;

        // Profile hub / More (null = old server; keep cached defaults).
        @SerializedName("categories_in_footer")
        @Expose
        private Boolean categoriesInFooter;

        @SerializedName("show_header_profile_icon")
        @Expose
        private Boolean showHeaderProfileIcon;

        @SerializedName("enable_status_videos")
        @Expose
        private Boolean enableStatusVideos;

        @SerializedName("enable_become_a_reporter")
        @Expose
        private Boolean enableBecomeAReporter;

        @SerializedName("enable_birthday_special")
        @Expose
        private Boolean enableBirthdaySpecial;

        @SerializedName("become_a_reporter_not_hiring_messages")
        @Expose
        private Object becomeAReporterNotHiringMessages;

        @SerializedName("profile_show_bookmarks")
        @Expose
        private Boolean profileShowBookmarks;

        @SerializedName("profile_show_referral")
        @Expose
        private Boolean profileShowReferral;

        /** Isolated referral-programme master switch; null means an older server. */
        @SerializedName("referral_enabled")
        @Expose
        private Boolean referralEnabled;

        @SerializedName("profile_show_settings_row")
        @Expose
        private Boolean profileShowSettingsRow;

        @SerializedName("profile_header_icon_size_dp")
        @Expose
        private Integer profileHeaderIconSizeDp;

        @SerializedName("profile_category_icon_size_dp")
        @Expose
        private Integer profileCategoryIconSizeDp;

        @SerializedName("profile_menu_text_size_sp")
        @Expose
        private Integer profileMenuTextSizeSp;

        /** Home footer center "+" size (dp). Null = old server / keep cache. */
        @SerializedName("footer_plus_icon_size_dp")
        @Expose
        private Integer footerPlusIconSizeDp;

        // Home bottom-nav slot visibility (null = old server / keep cache). Defaults true.
        @SerializedName("footer_show_reload")
        @Expose
        private Boolean footerShowReload;

        @SerializedName("footer_show_special")
        @Expose
        private Boolean footerShowSpecial;

        @SerializedName("footer_show_plus")
        @Expose
        private Boolean footerShowPlus;

        @SerializedName("footer_show_profile")
        @Expose
        private Boolean footerShowProfile;

        @SerializedName("footer_show_bookmarks")
        @Expose
        private Boolean footerShowBookmarks;

        // Settings panel toggle columns (null = old server / keep cache).
        @SerializedName("settings_show_notifications")
        @Expose
        private Boolean settingsShowNotifications;

        @SerializedName("settings_show_video_autoplay")
        @Expose
        private Boolean settingsShowVideoAutoplay;

        @SerializedName("settings_show_night_mode")
        @Expose
        private Boolean settingsShowNightMode;

        @SerializedName("settings_show_local_stories")
        @Expose
        private Boolean settingsShowLocalStories;

        /**
         * Admin global FlipView video autoplay master.
         * Null = old server / keep cache. Default ON when unset on client.
         */
        @SerializedName("video_autoplay_enabled")
        @Expose
        private Boolean videoAutoplayEnabled;

        /**
         * Admin feed Exo chrome A/B: {@code current} | {@code playstore}.
         * Null = old server / keep cache. Default {@code current}.
         */
        @SerializedName("feed_video_player")
        @Expose
        private String feedVideoPlayer;

        /** Contact Us mailto from admin Root_Settings. Null = keep cache / compile default. */
        @SerializedName("contact_email")
        @Expose
        private String contactEmail;

        /** Full jatakam WhatsApp (country+digits) for calendar CTA. Null/blank = keep cache. */
        @SerializedName("full_jatakam_whatsapp")
        @Expose
        private String fullJatakamWhatsapp;

        /**
         * Default selected rashi on calendar raasi flip.
         * 1–12 (Mesha=1), 0–11, or string key mesha…meena. Null = keep cache.
         */
        @SerializedName("default_rashi_id")
        @Expose
        private Object defaultRashiId; // Integer or String from JSON

        /**
         * Admin calendar year-pack source:
         * prokerala_hyderabad | astronomical_hyderabad. Null = keep cache / default.
         */
        @SerializedName("calendar_panchang_source")
        @Expose
        private String calendarPanchangSource;

        /** Detail card min height (dp). Null = keep cache / app default. */
        @SerializedName("calendar_detail_card_min_height_dp")
        @Expose
        private Integer calendarDetailCardMinHeightDp;

        /** Detail card title text size (sp). Null = keep cache / app default. */
        @SerializedName("calendar_detail_title_size_sp")
        @Expose
        private Integer calendarDetailTitleSizeSp;

        /** Detail card list line text size (sp). Null = keep cache / app default. */
        @SerializedName("calendar_detail_item_size_sp")
        @Expose
        private Integer calendarDetailItemSizeSp;

        /** Vertical pad between detail list lines (dp). Null = keep cache / app default. */
        @SerializedName("calendar_detail_item_pad_dp")
        @Expose
        private Integer calendarDetailItemPadDp;

        /** Month grid height fraction (0.35–0.70). Null = keep cache / app default. */
        @SerializedName("calendar_month_height_fraction")
        @Expose
        private Float calendarMonthHeightFraction;

        /** Selected-day circle diameter % of base 32dp (40–100). Null = keep cache. */
        @SerializedName("calendar_selected_circle_radius_percent")
        @Expose
        private Integer calendarSelectedCircleRadiusPercent;

        /** TTD feature config object from Root_Settings. Null = keep cache. */
        @SerializedName("ttd")
        @Expose
        private Object ttd;

        /** Weather Special config from Root_Settings. Null = keep cache. */
        @SerializedName("weather")
        @Expose
        private Object weather;

        /** Fuel Prices Special config from Root_Settings. Null = keep cache. */
        @SerializedName("fuel")
        @Expose
        private Object fuel;

        /** Gold & Silver Special config from Root_Settings. Null = keep cache. */
        @SerializedName("gold")
        @Expose
        private Object gold;

        /** Jobs hub payload (stats + items) from splash. Null = keep cache. */
        @SerializedName("jobs")
        @Expose
        private Object jobs;

        /** Daily Updates hub flags + art offset. Null = keep cache. */
        @SerializedName("daily_updates")
        @Expose
        private Object dailyUpdates;

        /** Stock Market splash summary (Yahoo indices) — real prices only. */
        @SerializedName("stock_market")
        @Expose
        private Object stockMarket;

        /** Flash News headlines pack (Redis/memory) for Daily Updates ticker. */
        @SerializedName("flash_news")
        @Expose
        private Object flashNews;

        /** Mana Zilla local district headlines (Redis/memory). */
        @SerializedName("local_headlines")
        @Expose
        private Object localHeadlines;

        // Admin one-time notification re-prompt (opt-in recovery).
        @SerializedName("notification_reprompt_enabled")
        @Expose
        private Boolean notificationRepromptEnabled;

        @SerializedName("notification_reprompt_due")
        @Expose
        private Boolean notificationRepromptDue;

        @SerializedName("notification_reprompt_wave")
        @Expose
        private Integer notificationRepromptWave;

        @SerializedName("notification_reprompt_converted")
        @Expose
        private Boolean notificationRepromptConverted;

        /** Continuous: ask OS notif permission on every app open while admin flag ON. */
        @SerializedName("prompt_notifications_on_open")
        @Expose
        private Boolean promptNotificationsOnOpen;

        @SerializedName("notification_flag")
        @Expose
        private Boolean notificationFlag;

        public LanguageFontConfig getFontConfig() {
            return fontConfig;
        }

        public Map<String, LanguageFontConfig> getLanguageFontSettings() {
            return languageFontSettings;
        }

        public Map<String, String> getCustomFontUrls() {
            return customFontUrls;
        }

        public FooterStyleConfig getFooterStyle() {
            return footerStyle;
        }

        public WriterStripStyleConfig getWriterStripStyle() {
            return writerStripStyle;
        }

        public FullVideoShareCardConfig getFullVideoShareCard() {
            return fullVideoShareCard;
        }

        public PollSurveyColorsConfig getPollSurveyColors() {
            return pollSurveyColors;
        }

        public CricketMiniPlayerPosition getCricketMiniPlayerPosition() {
            return cricketMiniPlayerPosition;
        }

        /** Null flag → not sent by server; caller keeps whatever is already cached. */
        public Boolean getCricketMiniPlayerEnabled() {
            return cricketMiniPlayerEnabled;
        }

        public Boolean getCricketFooterTabEnabled() {
            return cricketFooterTabEnabled;
        }

        public Integer getCricketMiniPlayerPollIntervalSeconds() {
            return cricketMiniPlayerPollIntervalSeconds;
        }

        public Integer getCricketMiniPlayerIdlePollIntervalSeconds() {
            return cricketMiniPlayerIdlePollIntervalSeconds;
        }

        public Integer getSessionHeartbeatIntervalSeconds() {
            return sessionHeartbeatIntervalSeconds;
        }

        public Boolean getSessionLiveEnabled() {
            return sessionLiveEnabled;
        }

        public Integer getSessionLiveWindowSeconds() {
            return sessionLiveWindowSeconds;
        }

        /** Null → old server; caller keeps cached / default 24. */
        public Integer getOfflineNewsPackMax() {
            return offlineNewsPackMax;
        }

        /** Null → old server; caller keeps cached / default 3. */
        public Integer getHomeModeChromeAutoHideSeconds() {
            return homeModeChromeAutoHideSeconds;
        }

        /** Null → old server; caller keeps cached / default hybrid. */
        public String getNewsTapMode() {
            return newsTapMode;
        }

        public Integer getNewsTapHalfUpperPct() {
            return newsTapHalfUpperPct;
        }

        public Integer getNewsTapFullUpperPct() {
            return newsTapFullUpperPct;
        }

        public Integer getNewsTapFullRightRailPct() {
            return newsTapFullRightRailPct;
        }

        public Boolean getNewsTapHalfHideEngagement() {
            return newsTapHalfHideEngagement;
        }

        public Boolean getHideDistrictOnMainFeed() {
            return hideDistrictOnMainFeed;
        }

        public Boolean getRewardsMaster() {
            return rewardsMaster;
        }

        public Boolean getShowWalletInProfile() {
            return showWalletInProfile;
        }

        public Boolean getRewardsInMainFeedSlot() {
            return rewardsInMainFeedSlot;
        }

        public Boolean getKeepRewardsAfterJoin() {
            return keepRewardsAfterJoin;
        }

        public Boolean getFlashNewsInMainFeedSlot() {
            return flashNewsInMainFeedSlot;
        }

        public Boolean getShowFlashAndRewardsTogether() {
            return showFlashAndRewardsTogether;
        }

        public String getFlashNewsChipLabel() {
            return flashNewsChipLabel;
        }

        public Integer getDualChipSizeDp() {
            return dualChipSizeDp;
        }

        public Integer getDualChipShiftLeftDp() {
            return dualChipShiftLeftDp;
        }

        public Integer getDualChipShiftUpDp() {
            return dualChipShiftUpDp;
        }

        public String getReporterArticlesFromDate() {
            return reporterArticlesFromDate;
        }

        public Boolean getReporterPaymentBannerActive() {
            return reporterPaymentBannerActive;
        }

        public String getReporterPaymentBannerText() {
            return reporterPaymentBannerText;
        }

        public Boolean getReporterPaymentHistoryNoticeActive() {
            return reporterPaymentHistoryNoticeActive;
        }

        public String getReporterPaymentHistoryNoticeText() {
            return reporterPaymentHistoryNoticeText;
        }

        public String getRewardsGuestChip() {
            return rewardsGuestChip;
        }

        public String getRewardsJoinedChip() {
            return rewardsJoinedChip;
        }

        public String getRewardsRateLabel() {
            return rewardsRateLabel;
        }

        public String getRewardsChipColor() {
            return rewardsChipColor;
        }

        public Integer getRewardsChipSizeSp() {
            return rewardsChipSizeSp;
        }

        public String getRewardsChipWeight() {
            return rewardsChipWeight;
        }

        public Boolean getRewardsExcludeReporters() {
            return rewardsExcludeReporters;
        }

        public Boolean getRewardsAllowReporters() {
            return rewardsAllowReporters;
        }

        public String getRewardsPeriodUnit() {
            return rewardsPeriodUnit;
        }

        public String getRewardsFactorCaps() {
            return rewardsFactorCaps;
        }

        public Integer getRewardsAdsSeenCap() {
            return rewardsAdsSeenCap;
        }

        public Integer getRewardsAdsSeenRemaining() {
            return rewardsAdsSeenRemaining;
        }

        public Boolean getRewardsAdsSeenCapped() {
            return rewardsAdsSeenCapped;
        }

        public String getRewardsProgramStatus() {
            return rewardsProgramStatus;
        }

        public Boolean getRewardsScoringOpen() {
            return rewardsScoringOpen;
        }

        public String getRewardsPeriodEnded() {
            return rewardsPeriodEnded;
        }

        public String getRewardsPeriodUpcoming() {
            return rewardsPeriodUpcoming;
        }

        public String getRewardsPeriodBanner() {
            return rewardsPeriodBanner;
        }

        public String getPlayStorePackage() {
            return playStorePackage;
        }

        public Integer getReadHistoryHours() {
            return readHistoryHours;
        }

        /** Null → old server; caller keeps cached / default 5. */
        public Integer getStatusVideosDays() {
            return statusVideosDays;
        }

        /** Null → old server; caller keeps cached / default 4. */
        public Integer getFeedAdEveryN() {
            return feedAdEveryN;
        }

        public Integer getAdPrefetchLookahead() {
            return adPrefetchLookahead;
        }

        public Integer getMaxNativeAdLoadsInFlight() {
            return maxNativeAdLoadsInFlight;
        }

        /** Null → old server; caller keeps cached / default 3. */
        public Integer getAdImprFlushEveryN() {
            return adImprFlushEveryN;
        }

        /** Null → old server; caller keeps cached / default ON (money-safe). */
        public Boolean getDestroyNativeAdsAfterImpression() {
            return destroyNativeAdsAfterImpression;
        }

        /** Null → old server; caller keeps cached / default ON (waterfall). */
        public Boolean getUseSecondNativeAdUnit() {
            return useSecondNativeAdUnit;
        }

        /** Null → old server; derive from {@link #getUseSecondNativeAdUnit()}. */
        public String getFeedNativeAdUnitMode() {
            return feedNativeAdUnitMode;
        }

        public Integer getViewsBatchFlushIntervalSeconds() {
            return viewsBatchFlushIntervalSeconds;
        }

        public Integer getViewsBatchFlushAtCount() {
            return viewsBatchFlushAtCount;
        }

        public Integer getMediaAccessFlushIntervalSeconds() {
            return mediaAccessFlushIntervalSeconds;
        }

        public Integer getRewardsEngagementFlushIntervalSeconds() {
            return rewardsEngagementFlushIntervalSeconds;
        }

        /** Null → old server; caller keeps cached / native. */
        public String getFeedAdFormat() {
            return feedAdFormat;
        }

        /** Null → old server; caller keeps cached / default native unit. */
        public String getAdCode() {
            return adCode;
        }

        /** Null → old server; caller keeps cached / default secondary unit. */
        public String getAdCodeSecondary() {
            return adCodeSecondary;
        }

        public Boolean getCategoriesInFooter() {
            return categoriesInFooter;
        }

        public Boolean getShowHeaderProfileIcon() {
            return showHeaderProfileIcon;
        }

        public Boolean getEnableStatusVideos() {
            return enableStatusVideos;
        }

        public Boolean getEnableBecomeAReporter() {
            return enableBecomeAReporter;
        }

        public Boolean getEnableBirthdaySpecial() {
            return enableBirthdaySpecial;
        }

        public Object getBecomeAReporterNotHiringMessages() {
            return becomeAReporterNotHiringMessages;
        }

        public Boolean getProfileShowBookmarks() {
            return profileShowBookmarks;
        }

        public Boolean getProfileShowReferral() {
            return profileShowReferral;
        }

        public Boolean getReferralEnabled() {
            return referralEnabled;
        }

        public Boolean getProfileShowSettingsRow() {
            return profileShowSettingsRow;
        }

        public Integer getProfileHeaderIconSizeDp() {
            return profileHeaderIconSizeDp;
        }

        public Integer getProfileCategoryIconSizeDp() {
            return profileCategoryIconSizeDp;
        }

        public Integer getProfileMenuTextSizeSp() {
            return profileMenuTextSizeSp;
        }

        public Integer getFooterPlusIconSizeDp() {
            return footerPlusIconSizeDp;
        }

        public Boolean getFooterShowReload() {
            return footerShowReload;
        }

        public Boolean getFooterShowSpecial() {
            return footerShowSpecial;
        }

        public Boolean getFooterShowPlus() {
            return footerShowPlus;
        }

        public Boolean getFooterShowProfile() {
            return footerShowProfile;
        }

        public Boolean getFooterShowBookmarks() {
            return footerShowBookmarks;
        }

        public Boolean getSettingsShowNotifications() {
            return settingsShowNotifications;
        }

        public Boolean getSettingsShowVideoAutoplay() {
            return settingsShowVideoAutoplay;
        }

        public Boolean getSettingsShowNightMode() {
            return settingsShowNightMode;
        }

        public Boolean getSettingsShowLocalStories() {
            return settingsShowLocalStories;
        }

        /**
         * Admin master: FlipView autoplay for half/full video feeds.
         * Null when omitted (old server) — do not wipe prefs.
         */
        public Boolean getVideoAutoplayEnabled() {
            return videoAutoplayEnabled;
        }

        public boolean hasVideoAutoplayEnabled() {
            return videoAutoplayEnabled != null;
        }

        /**
         * Admin feed Exo chrome: {@code current} | {@code playstore}.
         * Null when omitted — do not wipe prefs.
         */
        public String getFeedVideoPlayer() {
            return feedVideoPlayer;
        }

        public boolean hasFeedVideoPlayer() {
            return feedVideoPlayer != null && feedVideoPlayer.trim().length() > 0;
        }

        public String getContactEmail() {
            return contactEmail;
        }

        public String getFullJatakamWhatsapp() {
            return fullJatakamWhatsapp;
        }

        public Object getDefaultRashiId() {
            return defaultRashiId;
        }

        public String getCalendarPanchangSource() {
            return calendarPanchangSource;
        }

        public Integer getCalendarDetailCardMinHeightDp() {
            return calendarDetailCardMinHeightDp;
        }

        public Integer getCalendarDetailTitleSizeSp() {
            return calendarDetailTitleSizeSp;
        }

        public Integer getCalendarDetailItemSizeSp() {
            return calendarDetailItemSizeSp;
        }

        public Integer getCalendarDetailItemPadDp() {
            return calendarDetailItemPadDp;
        }

        public Float getCalendarMonthHeightFraction() {
            return calendarMonthHeightFraction;
        }

        public Integer getCalendarSelectedCircleRadiusPercent() {
            return calendarSelectedCircleRadiusPercent;
        }

        public Object getTtd() {
            return ttd;
        }

        public Object getWeather() {
            return weather;
        }

        public Object getFuel() {
            return fuel;
        }

        public Object getGold() {
            return gold;
        }

        public Object getJobs() {
            return jobs;
        }

        public Object getDailyUpdates() {
            return dailyUpdates;
        }

        public Object getStockMarket() {
            return stockMarket;
        }

        public Object getFlashNews() {
            return flashNews;
        }

        public Object getLocalHeadlines() {
            return localHeadlines;
        }

        public Boolean getNotificationRepromptEnabled() {
            return notificationRepromptEnabled;
        }

        public Boolean getNotificationRepromptDue() {
            return notificationRepromptDue;
        }

        public Integer getNotificationRepromptWave() {
            return notificationRepromptWave;
        }

        public Boolean getNotificationRepromptConverted() {
            return notificationRepromptConverted;
        }

        public Boolean getPromptNotificationsOnOpen() {
            return promptNotificationsOnOpen;
        }

        public Boolean getNotificationFlag() {
            return notificationFlag;
        }

        public String getEngagementBadgeColor() {
            return engagementBadgeColor;
        }

        public String getThemeColor() {
            return themeColor;
        }

        public void setEngagementBadgeColor(String engagementBadgeColor) {
            this.engagementBadgeColor = engagementBadgeColor;
        }

        public String getAdmobAppId() {
            return admobAppId;
        }

        public void setAdmobAppId(String admobAppId) {
            this.admobAppId = admobAppId;
        }

        public Integer getDraftId() {
            return draftId;
        }

        public void setDraftId(Integer draftId) {
            this.draftId = draftId;
        }

        public Integer getFontSize() {
            return fontSize;
        }

        public void setFontSize(Integer fontSize) {
            this.fontSize = fontSize;
        }

        public String getNextScreen() {
            return nextScreen;
        }

        public void setNextScreen(String nextScreen) {
            this.nextScreen = nextScreen;
        }

        public Integer getShowAnimation() {
            return showAnimation;
        }

        public void setShowAnimation(Integer showAnimation) {
            this.showAnimation = showAnimation;
        }

        public Boolean getAppUpdate() {
            return appUpdate;
        }

        public void setAppUpdate(Boolean appUpdate) {
            this.appUpdate = appUpdate;
        }

        public Integer getLanguageId() {
            return languageId;
        }

        public void setLanguageId(Integer languageId) {
            this.languageId = languageId;
        }

        public Integer getStaticDataApi() {
            return staticDataApi;
        }

        public void setStaticDataApi(Integer staticDataApi) {
            this.staticDataApi = staticDataApi;
        }

        public String getDistName() {
            return distName;
        }

        public void setDistName(String distName) {
            this.distName = distName;
        }

        public Integer getDistId() {
            return distId;
        }

        public void setDistId(Integer distId) {
            this.distId = distId;
        }

        public Integer getStateId() {
            return stateId;
        }

        public void setStateId(Integer stateId) {
            this.stateId = stateId;
        }

        public Boolean getIsLogin() {
            return isLogin;
        }

        public void setIsLogin(Boolean isLogin) {
            this.isLogin = isLogin;
        }

        public Integer getUpdateMenuStatus() {
            return updateMenuStatus;
        }

        public void setUpdateMenuStatus(Integer updateMenuStatus) {
            this.updateMenuStatus = updateMenuStatus;
        }

        public String getUpdateBannerSubtitle() {
            return updateBannerSubtitle;
        }

        public void setUpdateBannerSubtitle(String updateBannerSubtitle) {
            this.updateBannerSubtitle = updateBannerSubtitle;
        }

        public Boolean getEventStatus() {
            return eventStatus;
        }

        public void setEventStatus(Boolean eventStatus) {
            this.eventStatus = eventStatus;
        }
    }

    @SerializedName("success")
    @Expose
    private Boolean success;
    @SerializedName("data")
    @Expose
    private Data data;

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }




}