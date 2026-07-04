package com.anyNews.anynews.Pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class SurveyRequestFeedback implements Serializable {


    @SerializedName("draft_id")
    @Expose
    private Integer draftId;

    @SerializedName("post_id")
    @Expose
    private Long postId;

    @SerializedName("response")
    @Expose
    private String response;

    public Integer getDraftId() {
        return draftId;
    }

    public void setDraftId(Integer draftId) {
        this.draftId = draftId;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }


    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
