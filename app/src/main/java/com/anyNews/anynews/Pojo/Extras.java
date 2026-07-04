
package com.anyNews.anynews.Pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public  class Extras implements Serializable {


    @SerializedName("Status")
    @Expose
    private String status;
    @SerializedName("draft_id")
    @Expose
    private Integer draftId;
    @SerializedName("Whether_Latest_Version")
    @Expose
    private Boolean whetherLatestVersion;
    private final static long serialVersionUID = 6454616189832658279L;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getDraftId() {
        return draftId;
    }

    public void setDraftId(Integer draftId) {
        this.draftId = draftId;
    }

    public Boolean getWhetherLatestVersion() {
        return whetherLatestVersion;
    }

    public void setWhetherLatestVersion(Boolean whetherLatestVersion) {
        this.whetherLatestVersion = whetherLatestVersion;
    }


    //  list of all languages url
    @SerializedName("Data")
    @Expose
    private List<Datum> data = null;

    public List<Datum> getData() {
        return data;
    }

    public void setData(List<Datum> data) {
        this.data = data;
    }
}