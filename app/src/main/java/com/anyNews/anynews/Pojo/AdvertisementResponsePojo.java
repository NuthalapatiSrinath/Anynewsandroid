package com.anyNews.anynews.Pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class AdvertisementResponsePojo implements Serializable {
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

    public class Extras implements Serializable {
        @SerializedName("msg")
        @Expose
        private String msg;

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public String getMsg() {
            return msg;
        }

        @SerializedName("Count")
        @Expose
        private Integer count;
        @SerializedName("Data")
        @Expose
        private AdvertisementDataPojo data = null;

        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }

        public AdvertisementDataPojo getData() {
            return data;
        }

        public void setData(AdvertisementDataPojo data) {
            this.data = data;
        }
    }
}
