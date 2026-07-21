package com.anyNews.anynews.Pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ListOfUserArticlesRequest implements Serializable {

    @SerializedName("draft_id")
    @Expose
    private Integer draftId;
    @SerializedName("post_id")
    @Expose
    private Long postId;

    @SerializedName("session_id")
    @Expose
    private String analyticsSessionId;

    @SerializedName("language_id")
    @Expose
    private Integer languageId;

    public Integer getLanguageId() {
        return languageId;
    }

    public void setLanguageId(Integer languageId) {
        this.languageId = languageId;
    }

    @SerializedName("ApiKey")
        @Expose
        private String apiKey;
        @SerializedName("USERID")
        @Expose
        private String uSERID;
        @SerializedName("SessionID")
        @Expose
        private String sessionID;
        @SerializedName("skip")
        @Expose
        private Integer skip;
        @SerializedName("limit")
        @Expose
        private Integer limit;
        private final static long serialVersionUID = 4742021313344456396L;

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

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

        public Integer getSkip() {
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
        }

    public Integer getDraftId() {
        return draftId;
    }

    public void setDraftId(Integer draftId) {
        this.draftId = draftId;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public String getAnalyticsSessionId() {
        return analyticsSessionId;
    }

    public void setAnalyticsSessionId(String analyticsSessionId) {
        this.analyticsSessionId = analyticsSessionId;
    }

    public String getuSERID() {
        return uSERID;
    }

    public void setuSERID(String uSERID) {
        this.uSERID = uSERID;
    }
}
