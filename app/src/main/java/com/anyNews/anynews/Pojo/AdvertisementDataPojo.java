package com.anyNews.anynews.Pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class AdvertisementDataPojo implements Serializable {
    @SerializedName("_id")
    @Expose
    private String id;
    @SerializedName("Advertisement_Image_Data")
    @Expose
    private AdvertisementImageData advertisementImageData;

    @SerializedName("Advertisement_Image_Array")
    @Expose
    private List<AdvertisementImageData> advertisementImageDataList;

    @SerializedName("AdvertisementID")
    @Expose
    private String advertisementID;
    @SerializedName("Company_Name")
    @Expose
    private String companyName;
    @SerializedName("Advertisement_Type")
    @Expose
    private Integer advertisementType;
    @SerializedName("Advertisement_Name")
    @Expose
    private String advertisementName;
    @SerializedName("Advertisement_Link")
    @Expose
    private String advertisementLink;
    @SerializedName("SNo")
    @Expose
    private Integer sNo;
    @SerializedName("AreaCode_Array")
    @Expose
    private List<Object> areaCodeArray = null;
    @SerializedName("Advertisement_Image_Available")
    @Expose
    private Boolean advertisementImageAvailable;
    @SerializedName("Status")
    @Expose
    private Boolean status;
    @SerializedName("Description")
    @Expose
    private String description;
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("updated_at")
    @Expose
    private String updatedAt;
    @SerializedName("ViewedData")
    @Expose
    private List<AdvertisementViewedData> viewedData = null;
    @SerializedName("__v")
    @Expose
    private Integer v;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<AdvertisementImageData> getAdvertisementImageDataList() {
        return advertisementImageDataList;
    }

    public void setAdvertisementImageDataList(List<AdvertisementImageData> advertisementImageDataList) {
        this.advertisementImageDataList = advertisementImageDataList;
    }

    public AdvertisementImageData getAdvertisementImageData() {
        return advertisementImageData;
    }

    public void setAdvertisementImageData(AdvertisementImageData advertisementImageData) {
        this.advertisementImageData = advertisementImageData;
    }

    public String getAdvertisementID() {
        return advertisementID;
    }

    public void setAdvertisementID(String advertisementID) {
        this.advertisementID = advertisementID;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public Integer getAdvertisementType() {
        return advertisementType;
    }

    public void setAdvertisementType(Integer advertisementType) {
        this.advertisementType = advertisementType;
    }

    public String getAdvertisementName() {
        return advertisementName;
    }

    public void setAdvertisementName(String advertisementName) {
        this.advertisementName = advertisementName;
    }

    public String getAdvertisementLink() {
        return advertisementLink;
    }

    public void setAdvertisementLink(String advertisementLink) {
        this.advertisementLink = advertisementLink;
    }

    public Integer getsNo() {
        return sNo;
    }

    public void setsNo(Integer sNo) {
        this.sNo = sNo;
    }

    public List<Object> getAreaCodeArray() {
        return areaCodeArray;
    }

    public void setAreaCodeArray(List<Object> areaCodeArray) {
        this.areaCodeArray = areaCodeArray;
    }

    public Boolean getAdvertisementImageAvailable() {
        return advertisementImageAvailable;
    }

    public void setAdvertisementImageAvailable(Boolean advertisementImageAvailable) {
        this.advertisementImageAvailable = advertisementImageAvailable;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<AdvertisementViewedData> getViewedData() {
        return viewedData;
    }

    public void setViewedData(List<AdvertisementViewedData> viewedData) {
        this.viewedData = viewedData;
    }

    public Integer getV() {
        return v;
    }

    public void setV(Integer v) {
        this.v = v;
    }
}
