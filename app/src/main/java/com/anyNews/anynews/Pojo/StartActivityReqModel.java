
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
        @SerializedName("is_login")
        @Expose
        private Boolean isLogin;
        @SerializedName("update_menu_status")
        @Expose
        private Integer updateMenuStatus;

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