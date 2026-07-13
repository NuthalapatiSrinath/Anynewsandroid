package com.anyNews.anynews.Pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class ListOfCommentsResponse implements Serializable {


    public static class Datum implements Serializable
    {

        @SerializedName("user_name")
        @Expose
        private String userName;
        @SerializedName("comment")
        @Expose
        private String comment;
        @SerializedName("comment_time")
        @Expose
        private Long commentTime;
        @SerializedName("id")
        @Expose
        private Long id;
        @SerializedName("post_id")
        @Expose
        private Long postId;
        @SerializedName("draft_id")
        @Expose
        private Long draftId;
        @SerializedName("parent_comment_id")
        @Expose
        private Long parentCommentId;
        @SerializedName("reply_count")
        @Expose
        private Integer replyCount;
        @SerializedName("createdAt")
        @Expose
        private String createdAt;

        @SerializedName("user_profile_pic")
        @Expose
        private String user_profile_pic;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        @SerializedName("email")
        @Expose
        private String email;


        public String getUser_profile_pic() {
            return user_profile_pic;
        }

        public void setUser_profile_pic(String user_profile_pic) {
            this.user_profile_pic = user_profile_pic;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public Long getCommentTime() {
            return commentTime;
        }

        public void setCommentTime(Long commentTime) {
            this.commentTime = commentTime;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Long getPostId() {
            return postId;
        }

        public void setPostId(Long postId) {
            this.postId = postId;
        }

        public Long getDraftId() {
            return draftId;
        }

        public void setDraftId(Long draftId) {
            this.draftId = draftId;
        }

        public Long getParentCommentId() {
            return parentCommentId;
        }

        public void setParentCommentId(Long parentCommentId) {
            this.parentCommentId = parentCommentId;
        }

        public Integer getReplyCount() {
            return replyCount;
        }

        public void setReplyCount(Integer replyCount) {
            this.replyCount = replyCount;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }


    }
    public class Extras implements Serializable
    {
        @SerializedName("Count")
        @Expose
        private Integer count;
        @SerializedName("Data")
        @Expose
        private List<Datum> data = null;
        private final static long serialVersionUID = -2557662919545807446L;

        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }

        public List<Datum> getData() {
            return data;
        }

        public void setData(List<Datum> data) {
            this.data = data;
        }

    }



    @SerializedName("success")
    @Expose
    private Boolean success;

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    @SerializedName("statusCode")
    @Expose
    private int statusCode;
    @SerializedName("count")
    @Expose
    private Integer count;
    @SerializedName("data")
    @Expose
    private List<Datum> data = null;

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public List<Datum> getData() {
        return data;
    }

    public void setData(List<Datum> data) {
        this.data = data;
    }











/*

    public class UserImageData implements Serializable
    {

        @SerializedName("ImageID")
        @Expose
        private String imageID;
        @SerializedName("Image50")
        @Expose
        private String image50;
        @SerializedName("Image100")
        @Expose
        private String image100;
        @SerializedName("Image250")
        @Expose
        private String image250;
        @SerializedName("Image550")
        @Expose
        private String image550;
        @SerializedName("Image900")
        @Expose
        private String image900;
        @SerializedName("ImageOriginal")
        @Expose
        private String imageOriginal;
        private final static long serialVersionUID = 958762078557228572L;

        public String getImageID() {
            return imageID;
        }

        public void setImageID(String imageID) {
            this.imageID = imageID;
        }

        public String getImage50() {
            return image50;
        }

        public void setImage50(String image50) {
            this.image50 = image50;
        }

        public String getImage100() {
            return image100;
        }

        public void setImage100(String image100) {
            this.image100 = image100;
        }

        public String getImage250() {
            return image250;
        }

        public void setImage250(String image250) {
            this.image250 = image250;
        }

        public String getImage550() {
            return image550;
        }

        public void setImage550(String image550) {
            this.image550 = image550;
        }

        public String getImage900() {
            return image900;
        }

        public void setImage900(String image900) {
            this.image900 = image900;
        }

        public String getImageOriginal() {
            return imageOriginal;
        }

        public void setImageOriginal(String imageOriginal) {
            this.imageOriginal = imageOriginal;
        }

    }*/
}
