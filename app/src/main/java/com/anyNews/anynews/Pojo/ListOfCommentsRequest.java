package com.anyNews.anynews.Pojo;

import android.content.Intent;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ListOfCommentsRequest implements Serializable {


    @SerializedName("draft_id")
    @Expose
    private Integer draftId;
    @SerializedName("post_id")
    @Expose
    private Long postId;
    @SerializedName("sub_post_id")
    @Expose
    private Long subPostId;
    @SerializedName("comment")
    @Expose
    private String comment;

    @SerializedName("parent_comment_id")
    @Expose
    private Long parentCommentId;

    public Long getParentCommentId() {
        return parentCommentId;
    }

    public void setParentCommentId(Long parentCommentId) {
        this.parentCommentId = parentCommentId;
    }

    public String getUserid() {
        return email;
    }

    public void setUserid(String userid) {
        this.email = userid;
    }

    @SerializedName("email")
    @Expose
    private String email;

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

    public Long getSubPostId() {
        return subPostId;
    }

    public void setSubPostId(Long subPostId) {
        this.subPostId = subPostId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }



    @SerializedName("first_comment_id")
    @Expose
    private Long firstCommentId;
    @SerializedName("skip")
    @Expose
    private Long skip;





    public Long getFirstCommentId() {
        return firstCommentId;
    }

    public void setFirstCommentId(Long firstCommentId) {
        this.firstCommentId = firstCommentId;
    }

    public Long getSkip() {
        return skip;
    }

    public void setSkip(Long skip) {
        this.skip = skip;
    }


}
