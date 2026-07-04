package com.anyNews.anynews.Pojo;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DialogSendRequest {

    @SerializedName("draft_id")
    @Expose
    private Integer draftId;
    @SerializedName("language_id")
    @Expose
    private Integer languageId;
    @SerializedName("post_id")
    @Expose
    private Long postId;
    @SerializedName("reason")
    @Expose
    private String reason;
    @SerializedName("option")
    @Expose
    private String option;

    public Integer getDraftId() {
        return draftId;
    }

    public void setDraftId(Integer draftId) {
        this.draftId = draftId;
    }

    public Integer getLanguageId() {
        return languageId;
    }

    public void setLanguageId(Integer languageId) {
        this.languageId = languageId;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getOption() {
        return option;
    }

    public void setOption(String option) {
        this.option = option;
    }

}
