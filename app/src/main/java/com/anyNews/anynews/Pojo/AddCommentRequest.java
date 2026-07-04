package com.anyNews.anynews.Pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class AddCommentRequest implements Serializable {

        @SerializedName("ApiKey")
        @Expose
        private String apiKey;
        @SerializedName("USERID")
        @Expose
        private String uSERID;
        @SerializedName("SessionID")
        @Expose
        private String sessionID;
        @SerializedName("Article_NewsID")
        @Expose
        private String articleNewsID;
        @SerializedName("Comment")
        @Expose
        private String comment;
        private final static long serialVersionUID = -7292508070503817849L;

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

        public String getArticleNewsID() {
            return articleNewsID;
        }

        public void setArticleNewsID(String articleNewsID) {
            this.articleNewsID = articleNewsID;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

}
