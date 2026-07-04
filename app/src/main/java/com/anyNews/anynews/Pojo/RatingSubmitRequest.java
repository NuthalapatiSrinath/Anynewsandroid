package com.anyNews.anynews.Pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RatingSubmitRequest {


    public Long getDraft_id() {
        return draft_id;
    }

    public void setDraft_id(Long draft_id) {
        this.draft_id = draft_id;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }



    @SerializedName("draft_id")
    @Expose
    private Long draft_id;

    @SerializedName("rating")
    @Expose
    private float rating;

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    @SerializedName("comments")
    @Expose
    private String comments;


}
