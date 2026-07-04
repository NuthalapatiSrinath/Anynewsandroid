package com.anyNews.anynews.Pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class AddCommentResponse implements Serializable {

    public class Extras implements Serializable
    {

        @SerializedName("msg")
        @Expose
        private int msg;

        public void setMsg(int msg) {
            this.msg = msg;
        }

        public int getMsg() {
            return msg;
        }

        @SerializedName("Status")
        @Expose
        private String status;
        private final static long serialVersionUID = -5709241024738367498L;

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
        private final static long serialVersionUID = -5843050117614797004L;

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
