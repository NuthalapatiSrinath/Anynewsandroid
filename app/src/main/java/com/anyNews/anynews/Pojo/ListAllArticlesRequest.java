package com.anyNews.anynews.Pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ListAllArticlesRequest implements Serializable {

    @SerializedName("actual_skip")
    @Expose
    private Long actualSkip;

    @SerializedName("draft_id")
    @Expose
    private Integer draftId;
    @SerializedName("category_id")
    @Expose
    private Integer categoryId;
    @SerializedName("language_id")
    @Expose
    private Integer languageId;
    @SerializedName("skip")
    @Expose
    private Long skip;
    @SerializedName("last_post_id")
    @Expose
    private Long lastPostId;
    @SerializedName("push_post_id")
    @Expose
    private Long pushPostId;

    public String getReferred_post_id() {
        return referred_post_id;
    }

    public void setReferred_post_id(String referred_post_id) {
        this.referred_post_id = referred_post_id;
    }

    @SerializedName("referred_post_id")
    @Expose
    private String referred_post_id;
    @SerializedName("share_post_id")
    @Expose
    private Long sharePostId;
    @SerializedName("app_version_name")
    @Expose
    private String app_version_name;

    public String getUser_session_id() {
        return user_session_id;
    }

    public void setUser_session_id(String user_session_id) {
        this.user_session_id = user_session_id;
    }

    @SerializedName("user_session_id")
    @Expose
    private String user_session_id;

    public String getAdvertiserID() {
        return advertiserID;
    }

    public void setAdvertiserID(String advertiserID) {
        this.advertiserID = advertiserID;
    }

    @SerializedName("advertiser_id")
    @Expose
    private String advertiserID;

    public String getDatafrom2g() {
        return datafrom2g;
    }

    public void setDatafrom2g(String datafrom2g) {
        this.datafrom2g = datafrom2g;
    }

    @SerializedName("data_from_two_g")
    @Expose
    private String datafrom2g;

    public boolean isSpecial_posts() {
        return special_posts;
    }

    public void setSpecial_posts(boolean special_posts) {
        this.special_posts = special_posts;
    }

    @SerializedName("special_posts")
    @Expose
    private boolean special_posts;

    /**
     * When true, server returns only {@code normal_video} + {@code full_video}
     * (mutually exclusive with {@link #special_posts} on the API path).
     * Legacy Video News tab — footer now uses {@link #push_posts} (Flash News).
     */
    @SerializedName("video_posts")
    @Expose
    private boolean video_posts;

    /**
     * Breaking / Flash News tab: articles/videos from push notification history
     * (main desk pushes + user's district local pushes only).
     * Same flip open UX as News.
     */
    @SerializedName("push_posts")
    @Expose
    private boolean push_posts;

    /**
     * Profile Status Videos hub: one Redis-backed pack of {@code full_video}
     * posts published in the last ~5 days for the language (up to ~30 items).
     * Prefer over multi-page special_posts crawl.
     */
    @SerializedName("status_videos")
    @Expose
    private boolean status_videos;

    /**
     * Optional (new clients only): recent local read post_ids so the server can
     * return a full page of unread main-feed cards. Old Play Store apps omit this
     * and keep the historic List_All_Articles behavior.
     * Comma-separated post_id list (capped client-side); server also truncates.
     */
    @SerializedName("exclude_post_ids")
    @Expose
    private String exclude_post_ids;

    public boolean isVideo_posts() {
        return video_posts;
    }

    public void setVideo_posts(boolean video_posts) {
        this.video_posts = video_posts;
    }

    public boolean isPush_posts() {
        return push_posts;
    }

    public void setPush_posts(boolean push_posts) {
        this.push_posts = push_posts;
    }

    public boolean isStatus_videos() {
        return status_videos;
    }

    public void setStatus_videos(boolean status_videos) {
        this.status_videos = status_videos;
    }

    public String getExclude_post_ids() {
        return exclude_post_ids;
    }

    public void setExclude_post_ids(String exclude_post_ids) {
        this.exclude_post_ids = exclude_post_ids;
    }

    public String getApp_version_name() {
        return app_version_name;
    }

    public void setApp_version_name(String app_version_name) {
        this.app_version_name = app_version_name;
    }

    public String getApp_version_number() {
        return app_version_number;
    }

    public void setApp_version_number(String app_version_number) {
        this.app_version_number = app_version_number;
    }

    @SerializedName("app_version_number")
    @Expose
    private String app_version_number;

    public int getNotif_perm_allowed() {
        return notif_perm_allowed;
    }

    public void setNotif_perm_allowed(int notif_perm_allowed) {
        this.notif_perm_allowed = notif_perm_allowed;
    }

    public int getSdk_version() {
        return sdk_version;
    }

    public void setSdk_version(int sdk_version) {
        this.sdk_version = sdk_version;
    }

    @SerializedName("notif_perm_allowed")
    @Expose
    private int notif_perm_allowed;

    @SerializedName("sdk_version")
    @Expose
    private int sdk_version;

    public Integer getScreenHeight() {
        return screenHeight;
    }

    public void setScreenHeight(Integer screenHeight) {
        this.screenHeight = screenHeight;
    }

    public Integer getScreenWidth() {
        return screenWidth;
    }

    public void setScreenWidth(Integer screenWidth) {
        this.screenWidth = screenWidth;
    }

    @SerializedName("screenHeight")
    @Expose
    private Integer screenHeight;
    @SerializedName("screenWidth")
    @Expose
    private Integer screenWidth;

    public Long getActualSkip() {
        return actualSkip;
    }

    public void setActualSkip(Long actualSkip) {
        this.actualSkip = actualSkip;
    }
    public Integer getDraftId() {
        return draftId;
    }

    public void setDraftId(Integer draftId) {
        this.draftId = draftId;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getLanguageId() {
        return languageId;
    }

    public void setLanguageId(Integer languageId) {
        this.languageId = languageId;
    }

    public Long getSkip() {
        return skip;
    }

    public void setSkip(Long skip) {
        this.skip = skip;
    }

    public Long getLastPostId() {
        return lastPostId;
    }

    public void setLastPostId(Long lastPostId) {
        this.lastPostId = lastPostId;
    }

    public Long getPushPostId() {
        return pushPostId;
    }

    public void setPushPostId(Long pushPostId) {
        this.pushPostId = pushPostId;
    }

    public Long getSharePostId() {
        return sharePostId;
    }

    public void setSharePostId(Long sharePostId) {
        this.sharePostId = sharePostId;
    }

   /* @SerializedName("draft_id")
    @Expose
    private Integer draftId;
    @SerializedName("category_id")
    @Expose
    private Integer categoryId;
    @SerializedName("language_id")
    @Expose
    private Integer languageId;
    @SerializedName("limit")
    @Expose
    private Integer limit;
    @SerializedName("skip")
    @Expose
    private Integer skip;
    @SerializedName("last_post_id")
    @Expose
    private Long lastPostId;
    @SerializedName("push_post_id")
    @Expose
    private long pushPostId;


    public Long getPushPostId() {
        return pushPostId;
    }

    public void setPushPostId(Long pushPostId) {
        this.pushPostId = pushPostId;
    }


    private final static long serialVersionUID = -7684223351166234806L;

    public Integer getDraftId() {
        return draftId;
    }

    public void setDraftId(Integer draftId) {
        this.draftId = draftId;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getLanguageId() {
        return languageId;
    }

    public void setLanguageId(Integer languageId) {
        this.languageId = languageId;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getSkip() {
        return skip;
    }

    public void setSkip(Integer skip) {
        this.skip = skip;
    }

    public Long getLastPostId() {
        return lastPostId;
    }

    public void setLastPostId(Long lastPostId) {
        this.lastPostId = lastPostId;

    }*/
}





















































































































































    /*

    // present new

    @SerializedName("draft_id")
    @Expose
    private Integer draftId;
    @SerializedName("category_id")
    @Expose
    private Integer categoryId;
    @SerializedName("language_id")
    @Expose
    private Integer languageId;
    @SerializedName("limit")
    @Expose
    private Integer limit;
    @SerializedName("skip")
    @Expose
    private Integer skip;
    private final static long serialVersionUID = -7098731439711398381L;

    public Integer getDraftId() {
        return draftId;
    }

    public void setDraftId(Integer draftId) {
        this.draftId = draftId;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getLanguageId() {
        return languageId;
    }

    public void setLanguageId(Integer languageId) {
        this.languageId = languageId;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getSkip() {
        return skip;
    }

    public void setSkip(Integer skip) {
        this.skip = skip;
    }

    //
        @SerializedName("ApiKey")
        @Expose
        private String apiKey;
        @SerializedName("USERID")
        @Expose
        private String uSERID;
        @SerializedName("SessionID")
        @Expose
        private String sessionID;
        @SerializedName("CategoryID")
        @Expose
        private String categoryID;


        //   list all articals



    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

  *//*  public String getDraftID() {
        return draftID;
    }

    public void setDraftID(String draftID) {
        this.draftID = draftID;
    }
*//*


        public String getUSERID() {
            return uSERID;
        }

        public void setUSERID(String uSERID) {
            this.uSERID = uSERID;
        }

        public String getSessionID() {
            return sessionID;
        }

        public void setSessionID(String sessionID) {
            this.sessionID = sessionID;
        }

        public String getCategoryID() {
            return categoryID;
        }

        public void setCategoryID(String categoryID) {
            this.categoryID = categoryID;
        }

       *//* public Integer getSkip() {
            return skip;
        }

        public void setSkip(Integer skip) {
            this.skip = skip;
        }

        public Integer getLimit() {
            return limit;
        }

        public void setLimit(Integer limit) {
            this.limit = limit;
        }*/
