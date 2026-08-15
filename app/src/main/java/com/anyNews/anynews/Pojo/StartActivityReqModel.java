
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
         * Read / చదివినవి recency window in hours (admin; default 24, range 1–168).
         * Null on old servers → Android keeps cached / default 24.
         */
        @SerializedName("read_history_hours")
        @Expose
        private Integer readHistoryHours;

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

        public Integer getReadHistoryHours() {
            return readHistoryHours;
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

        public Boolean getCategoriesInFooter() {
            return categoriesInFooter;
        }

        public Boolean getShowHeaderProfileIcon() {
            return showHeaderProfileIcon;
        }

        public Boolean getEnableStatusVideos() {
            return enableStatusVideos;
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

        public String getContactEmail() {
            return contactEmail;
        }

        public String getFullJatakamWhatsapp() {
            return fullJatakamWhatsapp;
        }

        public Object getDefaultRashiId() {
            return defaultRashiId;
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