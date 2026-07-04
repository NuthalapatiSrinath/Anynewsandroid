package com.anyNews.anynews.Pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class AdvertisementViewedData implements Serializable {
    @SerializedName("USERID")
    @Expose
    private String uSERID;
    @SerializedName("Name")
    @Expose
    private String name;
    @SerializedName("PhoneNumber")
    @Expose
    private Integer phoneNumber;
    @SerializedName("created_at")
    @Expose
    private String createdAt;

    public String getuSERID() {
        return uSERID;
    }

    public void setuSERID(String uSERID) {
        this.uSERID = uSERID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(Integer phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
