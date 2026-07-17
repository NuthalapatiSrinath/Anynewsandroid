
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

        @SerializedName("font_config")
        @Expose
        private LanguageFontConfig fontConfig;

        @SerializedName("language_font_settings")
        @Expose
        private Map<String, LanguageFontConfig> languageFontSettings;

        @SerializedName("footer_style")
        @Expose
        private FooterStyleConfig footerStyle;

        @SerializedName("writer_strip_style")
        @Expose
        private WriterStripStyleConfig writerStripStyle;

        public LanguageFontConfig getFontConfig() {
            return fontConfig;
        }

        public Map<String, LanguageFontConfig> getLanguageFontSettings() {
            return languageFontSettings;
        }

        public FooterStyleConfig getFooterStyle() {
            return footerStyle;
        }

        public WriterStripStyleConfig getWriterStripStyle() {
            return writerStripStyle;
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