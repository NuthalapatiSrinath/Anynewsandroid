package com.anyNews.anynews.Pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class UserProfileRequest implements Serializable {


    @SerializedName("user_name")
    @Expose
    private String userName;


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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


        @SerializedName("draft_id")
        @Expose
        private Integer draftId;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @SerializedName("email")
        @Expose
        private String email;
        private final static long serialVersionUID = -4886032557244791067L;

        public Integer getDraftId() {
            return draftId;
        }

    public void setDraftId(Integer draftId) {
        this.draftId = draftId;
    }
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

}
