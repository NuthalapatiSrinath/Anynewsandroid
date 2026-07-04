package com.anyNews.anynews.Pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class UserProfileUpdateResponse implements Serializable {

    public class Extras implements Serializable
    {

        @SerializedName("Status")
        @Expose
        private String status;
        private final static long serialVersionUID = 4688394357016798198L;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

    }

    @SerializedName("success")
    @Expose
    private Boolean success;
    @SerializedName("extras")
    @Expose
    private Extras extras;
    private final static long serialVersionUID = 7838793227372617409L;

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public Extras getExtras() {
        return extras;
    }

    public void setExtras(Extras extras) {
        this.extras = extras;
    }

}
