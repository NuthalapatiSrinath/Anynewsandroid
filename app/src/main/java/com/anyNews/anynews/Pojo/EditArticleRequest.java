package com.anyNews.anynews.Pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class EditArticleRequest implements Serializable {

        @SerializedName("ApiKey")
        @Expose
        private String apiKey;
        @SerializedName("USERID")
        @Expose
        private String uSERID;
        @SerializedName("SessionID")
        @Expose
        private String sessionID;
        @SerializedName("Title")
        @Expose
        private String title;
        @SerializedName("Description")
        @Expose
        private String description;
        @SerializedName("ImageID")
        @Expose
        private String imageID;
        @SerializedName("LanguageID")
        @Expose
        private String languageID;
        @SerializedName("CategoryID")
        @Expose
        private String categoryID;
        @SerializedName("Time")
        @Expose
        private String time;
        @SerializedName("Article_NewsID")
        @Expose
        private String articleNewsID;
        private final static long serialVersionUID = 1711684303346617428L;

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

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getImageID() {
            return imageID;
        }

        public void setImageID(String imageID) {
            this.imageID = imageID;
        }

        public String getLanguageID() {
            return languageID;
        }

        public void setLanguageID(String languageID) {
            this.languageID = languageID;
        }

        public String getCategoryID() {
            return categoryID;
        }

        public void setCategoryID(String categoryID) {
            this.categoryID = categoryID;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getArticleNewsID() {
            return articleNewsID;
        }

        public void setArticleNewsID(String articleNewsID) {
            this.articleNewsID = articleNewsID;
        }

}
