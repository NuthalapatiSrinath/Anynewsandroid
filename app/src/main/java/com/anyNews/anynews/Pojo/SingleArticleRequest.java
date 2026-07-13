package com.anyNews.anynews.Pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class SingleArticleRequest implements Serializable {

    @SerializedName("draft_id")
    @Expose
    private Integer draftId;
    @SerializedName("user_post_id")
    @Expose
    private Integer userPostId;
    @SerializedName("image_changed")
    @Expose
    private Boolean imageChanged;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("language_id")
    @Expose
    private Integer languageId;
    @SerializedName("category_id")
    @Expose
    private Integer categoryId;
    @SerializedName("post_type")
    @Expose
    private String postType;
    @SerializedName("media_type")
    @Expose
    private String mediaType;
    @SerializedName("images")
    @Expose
    private String images = null;

    @SerializedName("videos")
    @Expose
    private String video = null;

    @SerializedName("longitude")
    @Expose
    private String longitude;
    @SerializedName("latitude")
    @Expose
    private String latitude;

    public Integer getDraftId() {
        return draftId;
    }

    public void setDraftId(Integer draftId) {
        this.draftId = draftId;
    }

    public Integer getUserPostId() {
        return userPostId;
    }

    public void setUserPostId(Integer userPostId) {
        this.userPostId = userPostId;
    }

    public Boolean getImageChanged() {
        return imageChanged;
    }

    public void setImageChanged(Boolean imageChanged) {
        this.imageChanged = imageChanged;
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

    public Integer getLanguageId() {
        return languageId;
    }

    public void setLanguageId(Integer languageId) {
        this.languageId = languageId;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
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

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }


    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

}
