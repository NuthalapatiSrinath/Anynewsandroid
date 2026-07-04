package com.anyNews.anynews.Pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
public class LoginDetailesResponse {

    public class Data {

        @SerializedName("login_status")
        @Expose
        private Boolean loginStatus;

        public Boolean getLoginStatus() {
            return loginStatus;
        }

        public void setLoginStatus(Boolean loginStatus) {
            this.loginStatus = loginStatus;
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
