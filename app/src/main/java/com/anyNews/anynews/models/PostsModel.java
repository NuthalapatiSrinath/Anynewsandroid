package com.anyNews.anynews.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Collection;
import java.util.List;

public class PostsModel {


    public class Datum {

        @SerializedName("title")
        @Expose
        private String title;
        @SerializedName("description")
        @Expose
        private String description;
        @SerializedName("image_url")
        @Expose
        private String imageUrl;
        @SerializedName("post_type")
        @Expose
        private String postType;
        @SerializedName("media_type")
        @Expose
        private String mediaType;
        @SerializedName("user_post_id")
        @Expose
        private Integer userPostId;
        @SerializedName("reason")
        @Expose
        private String reason;
        @SerializedName("createdAt")
        @Expose
        private Long createdAt;

        @SerializedName("post_status")
        @Expose
        private Long post_status;

        public Long getPost_status() {
            return post_status;
        }

        public void setPost_status(Long post_status) {
            this.post_status = post_status;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public String getPostType() {
            return postType;
        }

        public void setPostType(String postType) {
            this.postType = postType;
        }

        public String getMediaType() {
            return mediaType;
        }

        public void setMediaType(String mediaType) {
            this.mediaType = mediaType;
        }

        public Integer getUserPostId() {
            return userPostId;
        }

        public void setUserPostId(Integer userPostId) {
            this.userPostId = userPostId;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }

        public Long getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(Long createdAt) {
            this.createdAt = createdAt;
        }

    }


    @SerializedName("success")
    @Expose
    private Boolean success;
    @SerializedName("data")
    @Expose
    private List<Datum> data = null;
    @SerializedName("skip")
    @Expose
    private Integer skip;
    @SerializedName("first_post_id")
    @Expose
    private Long firstPostId;

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public List<Datum> getData() {
        return data;
    }

    public void setData(List<Datum> data) {
        this.data = data;
    }

    public Integer getSkip() {
        return skip;
    }

    public void setSkip(Integer skip) {
        this.skip = skip;
    }

    public Long getFirstPostId() {
        return firstPostId;
    }

    public void setFirstPostId(Long firstPostId) {
        this.firstPostId = firstPostId;
    }



}
