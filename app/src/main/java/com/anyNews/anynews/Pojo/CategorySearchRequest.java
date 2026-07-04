package com.anyNews.anynews.Pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class CategorySearchRequest implements Serializable {

        @SerializedName("ApiKey")
        @Expose
        private String apiKey;
        @SerializedName("USERID")
        @Expose
        private String uSERID;
        @SerializedName("SessionID")
        @Expose
        private String sessionID;
        @SerializedName("Category_Name")
        @Expose
        private String categoryName;
        private final static long serialVersionUID = -8875476279964242690L;

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

        public String getCategoryName() {
            return categoryName;
        }

        public void setCategoryName(String categoryName) {
            this.categoryName = categoryName;
        }

}
