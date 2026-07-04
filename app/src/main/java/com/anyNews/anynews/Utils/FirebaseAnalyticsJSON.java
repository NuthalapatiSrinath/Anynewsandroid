package com.anyNews.anynews.Utils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class FirebaseAnalyticsJSON implements Serializable {

    @SerializedName("DeviceID")
    @Expose
    private String deviceID;

    @SerializedName("PostID")
    @Expose
    private String postID;

    @SerializedName("AppVersion")
    @Expose
    private Integer appVersion;

    @SerializedName("draft_id")
    @Expose
    private Integer draftId;

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public Integer getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(Integer appVersion) {
        this.appVersion = appVersion;
    }

    public Integer getDraftId() {
        return draftId;
    }

    public void setDraftId(Integer draftId) {
        this.draftId = draftId;
    }

    public String getPostID() {
        return postID;
    }

    public void setPostID(String postID) {
        this.postID = postID;
    }
}
