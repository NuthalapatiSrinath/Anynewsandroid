package com.anyNews.anynews.Pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class EventPageLinkResponse implements Serializable {

    @SerializedName("draft_id")
    @Expose
    private Integer draftId;

    @SerializedName("success")
    @Expose
    private Boolean success;

    @SerializedName("link")
    @Expose
    private String link;

    @SerializedName("login_status")
    @Expose
    private Boolean login_status;

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Boolean getLogin_status() {
        return login_status;
    }

    public void setLogin_status(Boolean login_status) {
        this.login_status = login_status;
    }

    public Integer getDraftId() {
        return draftId;
    }

    public void setDraftId(Integer draftId) {
        this.draftId = draftId;
    }

}

