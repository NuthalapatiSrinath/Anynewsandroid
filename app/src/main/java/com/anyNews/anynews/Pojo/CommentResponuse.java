package com.anyNews.anynews.Pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class CommentResponuse implements Serializable
{


    public static class Extras implements Serializable
    {

        @SerializedName("Status")
        @Expose
        private String status;
        private final static long serialVersionUID = 4688394357016798198L;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

    }
    @SerializedName("success")
    @Expose
    private Boolean success;
    @SerializedName("saved")
    @Expose
    private Boolean saved;
    @SerializedName("comment_id")
    @Expose
    private Long commentId;
    @SerializedName("msg")
    @Expose
    private String msg;
    @SerializedName("comments")
    @Expose
    private Integer comments;
    @SerializedName("extras")
    @Expose
    private Extras extras;
    private final static long serialVersionUID = 7838793227372617409L;

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    /** True when the row was stored, even if a later side effect reported failure. */
    public boolean wasSaved() {
        if (Boolean.TRUE.equals(success) || Boolean.TRUE.equals(saved)) {
            return true;
        }
        return commentId != null && commentId > 0;
    }

    public Boolean getSaved() {
        return saved;
    }

    public void setSaved(Boolean saved) {
        this.saved = saved;
    }

    public Long getCommentId() {
        return commentId;
    }

    public void setCommentId(Long commentId) {
        this.commentId = commentId;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Extras getExtras() {
        return extras;
    }

    public void setExtras(Extras extras) {
        this.extras = extras;
    }

    public Integer getComments() {
        return comments;
    }

    public void setComments(Integer comments) {
        this.comments = comments;
    }

}
