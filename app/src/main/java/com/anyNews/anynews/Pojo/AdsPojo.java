package com.anyNews.anynews.Pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class AdsPojo implements Serializable {

    @SerializedName("ad_type")
    @Expose
    private String ad_type;
    @SerializedName("ad_req_position")
    @Expose
    private Integer ad_req_position;

    public String getAd_type() {
        return ad_type;
    }

    public void setAd_type(String ad_type) {
        this.ad_type = ad_type;
    }

    public Integer getAd_req_position() {
        return ad_req_position;
    }

    public void setAd_req_position(Integer ad_req_position) {
        this.ad_req_position = ad_req_position;
    }

    public String getAd_code() {
        return ad_code;
    }

    public void setAd_code(String ad_code) {
        this.ad_code = ad_code;
    }

    @SerializedName("ad_code")
    @Expose
    private String ad_code;

}
