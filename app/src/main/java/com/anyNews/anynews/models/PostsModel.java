package com.anyNews.anynews.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Collection;
import java.util.List;

public class PostsModel {


    public static class Datum {

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

        /** Optional engagement from live post (get_user_posts). */
        @SerializedName("likes")
        @Expose
        private Integer likes;
        @SerializedName("comments")
        @Expose
        private Integer comments;
        @SerializedName(value = "views", alternate = {"total_views", "totalViews"})
        @Expose
        private Integer views;
        @SerializedName("live_post_id")
        @Expose
        private Integer livePostId;

        /** Week metadata from get_user_posts (optional). */
        @SerializedName("week_key")
        @Expose
        private String weekKey;
        @SerializedName("week_label")
        @Expose
        private String weekLabel;
        @SerializedName("week_start")
        @Expose
        private String weekStart;
        @SerializedName("week_end")
        @Expose
        private String weekEnd;

        /** Client-only: week section header row (not from API). */
        public transient boolean isWeekHeader;
        public transient String headerLabel;
        public transient int headerCount;

        public Long getPost_status() {
            return post_status;
        }

        public void setPost_status(Long post_status) {
            this.post_status = post_status;
        }

        public Integer getLikes() {
            return likes != null ? likes : 0;
        }

        public void setLikes(Integer likes) {
            this.likes = likes;
        }

        public Integer getComments() {
            return comments != null ? comments : 0;
        }

        public void setComments(Integer comments) {
            this.comments = comments;
        }

        public Integer getViews() {
            return views != null ? views : 0;
        }

        public void setViews(Integer views) {
            this.views = views;
        }

        public Integer getLivePostId() {
            return livePostId;
        }

        public void setLivePostId(Integer livePostId) {
            this.livePostId = livePostId;
        }

        public String getWeekKey() {
            return weekKey;
        }

        public void setWeekKey(String weekKey) {
            this.weekKey = weekKey;
        }

        public String getWeekLabel() {
            return weekLabel;
        }

        public void setWeekLabel(String weekLabel) {
            this.weekLabel = weekLabel;
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

    /** Totals across all of the reporter's posts (not just this page). */
    @SerializedName("approved_count")
    @Expose
    private Integer approvedCount;
    @SerializedName("pending_count")
    @Expose
    private Integer pendingCount;
    @SerializedName("rejected_count")
    @Expose
    private Integer rejectedCount;

    @SerializedName("has_more")
    @Expose
    private Boolean hasMore;

    public Integer getApprovedCount() {
        return approvedCount;
    }

    public void setApprovedCount(Integer approvedCount) {
        this.approvedCount = approvedCount;
    }

    public Integer getPendingCount() {
        return pendingCount;
    }

    public void setPendingCount(Integer pendingCount) {
        this.pendingCount = pendingCount;
    }

    public Integer getRejectedCount() {
        return rejectedCount;
    }

    public void setRejectedCount(Integer rejectedCount) {
        this.rejectedCount = rejectedCount;
    }

    public Boolean getHasMore() {
        return hasMore;
    }

    public void setHasMore(Boolean hasMore) {
        this.hasMore = hasMore;
    }



}
