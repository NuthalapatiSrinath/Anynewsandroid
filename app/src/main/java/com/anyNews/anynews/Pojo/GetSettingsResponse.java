package com.anyNews.anynews.Pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GetSettingsResponse {
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
    public class Data {

        @SerializedName("notification_flag")
        @Expose
        private Boolean notificationFlag;

        public Boolean getNotificationFlag() {
            return notificationFlag;
        }

        public void setNotificationFlag(Boolean notificationFlag) {
            this.notificationFlag = notificationFlag;
        }

    }

}
