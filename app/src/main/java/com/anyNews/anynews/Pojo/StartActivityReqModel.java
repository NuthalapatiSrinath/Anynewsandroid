
package com.anyNews.anynews.Pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

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