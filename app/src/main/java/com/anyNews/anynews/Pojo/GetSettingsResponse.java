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

        @SerializedName("notification_reprompt_due")
        @Expose
        private Boolean notificationRepromptDue;

        @SerializedName("notification_reprompt_wave")
        @Expose
        private Integer notificationRepromptWave;

        @SerializedName("notification_reprompt_converted")
        @Expose
        private Boolean notificationRepromptConverted;

        @SerializedName("notification_reprompt_enabled")
        @Expose
        private Boolean notificationRepromptEnabled;

        @SerializedName("prompt_notifications_on_open")
        @Expose
        private Boolean promptNotificationsOnOpen;

        public Boolean getNotificationFlag() {
            return notificationFlag;
        }

        public void setNotificationFlag(Boolean notificationFlag) {
            this.notificationFlag = notificationFlag;
        }

        public Boolean getNotificationRepromptDue() {
            return notificationRepromptDue;
        }

        public Integer getNotificationRepromptWave() {
            return notificationRepromptWave;
        }

        public Boolean getNotificationRepromptConverted() {
            return notificationRepromptConverted;
        }

        public Boolean getNotificationRepromptEnabled() {
            return notificationRepromptEnabled;
        }

        public Boolean getPromptNotificationsOnOpen() {
            return promptNotificationsOnOpen;
        }

    }

}
