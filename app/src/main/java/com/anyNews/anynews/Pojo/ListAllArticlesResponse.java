package com.anyNews.anynews.Pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class ListAllArticlesResponse implements Serializable {




    public class Datum implements Serializable
    {

        @SerializedName("thumbnail")
        @Expose
        private String thumbnail;
        @SerializedName("title")
        @Expose
        private String title;
        @SerializedName("description")
        @Expose
        private String description;
        @SerializedName("category_id")
        @Expose
        private Integer categoryId;
        @SerializedName("category_name")
        @Expose
        private String categoryName;
        @SerializedName("post_type")
        @Expose
        private String postType;
        @SerializedName("media_type")
        @Expose
        private String MediaType;
        @SerializedName("images")
        @Expose
        private List<String> images = null;
        @SerializedName("video_url")
        @Expose
        private String videoUrl;
        @SerializedName("published_time")
        @Expose
        private Long publishedTime;
        @SerializedName("shares")
        @Expose
        private Integer shares;
        @SerializedName("downloads")
        @Expose
        private Integer downloads;
        @SerializedName("comments_flag")
        @Expose
        private Boolean commentsFlag;
        @SerializedName("is_ad")
        @Expose
        private Boolean isAd;
        @SerializedName("is_breaking")
        @Expose
        private Boolean isBreaking;
        @SerializedName("post_id")
        @Expose
        private Integer postId;
        @SerializedName("language_id")
        @Expose
        private Integer languageId;
        @SerializedName("share_link")
        @Expose
        private String shareLink;

        @SerializedName("background_image")
        @Expose
        private String backgroundImage;



        private final static long serialVersionUID = -1001532379296520495L;


        public String getBackgroundImage() {
            return backgroundImage;
        }

        public void setBackgroundImage(String backgroundImage) {
            this.backgroundImage = backgroundImage;
        }
        public String getShareLink() {
            return shareLink;
        }

        public void setShareLink(String shareLink) {
            this.shareLink = shareLink;
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

        public Integer getCategoryId() {
            return categoryId;
        }

        public void setCategoryId(Integer categoryId) {
            this.categoryId = categoryId;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public void setCategoryName(String categoryName) {
            this.categoryName = categoryName;
        }

        public String getPostType() {
            return postType;
        }
        public String getMediaType() {
            return MediaType;
        }

        public void setPostType(String postType) {
            this.postType = postType;
        }

        public String getThumbnail() {
            return thumbnail;
        }

        public void setThumbnail(String thumbnail) {
            this.thumbnail = thumbnail;
        }
        public List<String> getImages() {
            return images;
        }

        public void setImages(List<String> images) {
            this.images = images;
        }

        public String getVideoUrl() {
            return videoUrl;
        }

        public void setVideoUrl(String videoUrl) {
            this.videoUrl = videoUrl;
        }

        public Long getPublishedTime() {
            return publishedTime;
        }

        public void setPublishedTime(Long publishedTime) {
            this.publishedTime = publishedTime;
        }

        public Integer getShares() {
            return shares;
        }

        public void setShares(Integer shares) {
            this.shares = shares;
        }

        public Integer getDownloads() {
            return downloads;
        }

        public void setDownloads(Integer downloads) {
            this.downloads = downloads;
        }

        public Boolean getCommentsFlag() {
            return commentsFlag;
        }

        public void setCommentsFlag(Boolean commentsFlag) {
            this.commentsFlag = commentsFlag;
        }

        public Boolean getIsAd() {
            return isAd;
        }

        public void setIsAd(Boolean isAd) {
            this.isAd = isAd;
        }

        public Boolean getIsBreaking() {
            return isBreaking;
        }

        public void setIsBreaking(Boolean isBreaking) {
            this.isBreaking = isBreaking;
        }

        public Integer getPostId() {
            return postId;
        }

        public void setPostId(Integer postId) {
            this.postId = postId;
        }

        public Integer getLanguageId() {
            return languageId;
        }

        public void setLanguageId(Integer languageId) {
            this.languageId = languageId;
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
        @SerializedName("last_post_id")
        @Expose
        private Integer lastPostId;
        private final static long serialVersionUID = 5038545514147607982L;

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

        public Integer getLastPostId() {
            return lastPostId;
        }

        public void setLastPostId(Integer lastPostId) {
            this.lastPostId = lastPostId;
        }

    }




    @SerializedName("success")
    @Expose
    private Boolean success;
    @SerializedName("extras")
    @Expose
    private Extras extras;
    private final static long serialVersionUID = -9019652905466300122L;

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public Extras getExtras() {
        return extras;
    }

    public void setExtras(Extras extras) {
        this.extras = extras;
    }

}