package com.anyNews.anynews.Pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class AddArticleRequest implements Serializable {
    @SerializedName("draft_id")
    @Expose
    private Integer draftId;
    @SerializedName("user_id")
    @Expose
    private Integer userId;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("language_id")
    @Expose
    private Integer languageId;
    @SerializedName("language_name")
    @Expose
    private String languageName;
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
    private String mediaType;
    @SerializedName("image_urls")
    @Expose
    private List<String> imageUrls = null;
    @SerializedName("video_url")
    @Expose
    private String videoUrl;

    public Integer getDraftId() {
        return draftId;
    }

    public void setDraftId(Integer draftId) {
        this.draftId = draftId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
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

    public String getLanguageName() {
        return languageName;
    }

    public void setLanguageName(String languageName) {
        this.languageName = languageName;
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

    public void setPostType(String postType) {
        this.postType = postType;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

//        @SerializedName("ApiKey")
//        @Expose
//        private String apiKey;
//        @SerializedName("USERID")
//        @Expose
//        private String uSERID;
//        @SerializedName("SessionID")
//        @Expose
//        private String sessionID;
//        @SerializedName("Title")
//        @Expose
//        private String title;
//        @SerializedName("Description")
//        @Expose
//        private String description;
//        @SerializedName("ImageID")
//        @Expose
//        private String imageID;
//        @SerializedName("Article_News_YouTube_Link")
//        @Expose
//        private String articleNewsYouTubeLink;
//        @SerializedName("LanguageID")
//        @Expose
//        private String languageID;
//        @SerializedName("CategoryID")
//        @Expose
//        private String categoryID;
//        @SerializedName("Article_News_Image_Available")
//        @Expose
//        private Boolean articleNewsImageAvailable;
//        @SerializedName("Article_News_Video_Available")
//        @Expose
//        private Boolean articleNewsVideoAvailable;
//        @SerializedName("Article_Type")
//        @Expose
//        private Integer articleType;
//        @SerializedName("Time")
//        @Expose
//        private String time;
//        private final static long serialVersionUID = -6092588703573802222L;
//
//        public String getApiKey() {
//            return apiKey;
//        }
//
//        public void setApiKey(String apiKey) {
//            this.apiKey = apiKey;
//        }
//
//        public String getUSERID() {
//            return uSERID;
//        }
//
//        public void setUSERID(String uSERID) {
//            this.uSERID = uSERID;
//        }
//
//        public String getSessionID() {
//            return sessionID;
//        }
//
//        public void setSessionID(String sessionID) {
//            this.sessionID = sessionID;
//        }
//
//        public String getTitle() {
//            return title;
//        }
//
//        public void setTitle(String title) {
//            this.title = title;
//        }
//
//        public String getDescription() {
//            return description;
//        }
//
//        public void setDescription(String description) {
//            this.description = description;
//        }
//
//        public String getImageID() {
//            return imageID;
//        }
//
//        public void setImageID(String imageID) {
//            this.imageID = imageID;
//        }
//
//        public String getArticleNewsYouTubeLink() {
//            return articleNewsYouTubeLink;
//        }
//
//        public void setArticleNewsYouTubeLink(String articleNewsYouTubeLink) {
//            this.articleNewsYouTubeLink = articleNewsYouTubeLink;
//        }
//
//        public String getLanguageID() {
//            return languageID;
//        }
//
//        public void setLanguageID(String languageID) {
//            this.languageID = languageID;
//        }
//
//        public String getCategoryID() {
//            return categoryID;
//        }
//
//        public void setCategoryID(String categoryID) {
//            this.categoryID = categoryID;
//        }
//
//        public Boolean getArticleNewsImageAvailable() {
//            return articleNewsImageAvailable;
//        }
//
//        public void setArticleNewsImageAvailable(Boolean articleNewsImageAvailable) {
//            this.articleNewsImageAvailable = articleNewsImageAvailable;
//        }
//
//        public Boolean getArticleNewsVideoAvailable() {
//            return articleNewsVideoAvailable;
//        }
//
//        public void setArticleNewsVideoAvailable(Boolean articleNewsVideoAvailable) {
//            this.articleNewsVideoAvailable = articleNewsVideoAvailable;
//        }
//
//        public Integer getArticleType() {
//            return articleType;
//        }
//
//        public void setArticleType(Integer articleType) {
//            this.articleType = articleType;
//        }
//
//        public String getTime() {
//            return time;
//        }
//
//        public void setTime(String time) {
//            this.time = time;
//        }

}
