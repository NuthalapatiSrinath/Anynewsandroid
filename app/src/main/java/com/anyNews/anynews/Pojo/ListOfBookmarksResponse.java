package com.anyNews.anynews.Pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class ListOfBookmarksResponse implements Serializable {

    public class ArticleNewsImageData implements Serializable
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
        private final static long serialVersionUID = 4555206375094601719L;

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

    }

    public class Datum implements Serializable
    {

        @SerializedName("_id")
        @Expose
        private String id;
        @SerializedName("Language")
        @Expose
        private Language language;
        @SerializedName("Article_News_Image_Data")
        @Expose
        private ArticleNewsImageData articleNewsImageData;
        @SerializedName("Article_NewsID")
        @Expose
        private String articleNewsID;
        @SerializedName("USERID")
        @Expose
        private String uSERID;
        @SerializedName("Title")
        @Expose
        private String title;
        @SerializedName("Description")
        @Expose
        private String description;
        @SerializedName("Status")
        @Expose
        private Integer status;
        @SerializedName("Category_Name")
        @Expose
        private String categoryName;
        @SerializedName("CategoryID")
        @Expose
        private String categoryID;
        @SerializedName("Article_News_Image_Available")
        @Expose
        private Boolean articleNewsImageAvailable;
        @SerializedName("Time")
        @Expose
        private String time;
        @SerializedName("created_at")
        @Expose
        private String createdAt;
        @SerializedName("updated_at")
        @Expose
        private String updatedAt;
        @SerializedName("Likes")
        @Expose
        private Integer likes;
        @SerializedName("Report")
        @Expose
        private Integer report;
        @SerializedName("Comment")
        @Expose
        private Integer comment;
        @SerializedName("Shares")
        @Expose
        private Integer shares;
        @SerializedName("__v")
        @Expose
        private Integer v;
        @SerializedName("Article_News_Video_Available")
        @Expose
        private Boolean articleNewsVideoAvailable;
        @SerializedName("Article_Type")
        @Expose
        private Integer articleType;
        @SerializedName("Article_News_YouTube_Link")
        @Expose
        private String articleNewsYouTubeLink;
        @SerializedName("Like_Status")
        @Expose
        private Boolean likeStatus;
        private final static long serialVersionUID = -6541108869812686238L;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Language getLanguage() {
            return language;
        }

        public void setLanguage(Language language) {
            this.language = language;
        }

        public ArticleNewsImageData getArticleNewsImageData() {
            return articleNewsImageData;
        }

        public void setArticleNewsImageData(ArticleNewsImageData articleNewsImageData) {
            this.articleNewsImageData = articleNewsImageData;
        }

        public String getArticleNewsID() {
            return articleNewsID;
        }

        public void setArticleNewsID(String articleNewsID) {
            this.articleNewsID = articleNewsID;
        }

        public String getUSERID() {
            return uSERID;
        }

        public void setUSERID(String uSERID) {
            this.uSERID = uSERID;
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

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public void setCategoryName(String categoryName) {
            this.categoryName = categoryName;
        }

        public String getCategoryID() {
            return categoryID;
        }

        public void setCategoryID(String categoryID) {
            this.categoryID = categoryID;
        }

        public Boolean getArticleNewsImageAvailable() {
            return articleNewsImageAvailable;
        }

        public void setArticleNewsImageAvailable(Boolean articleNewsImageAvailable) {
            this.articleNewsImageAvailable = articleNewsImageAvailable;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(String updatedAt) {
            this.updatedAt = updatedAt;
        }

        public Integer getLikes() {
            return likes;
        }

        public void setLikes(Integer likes) {
            this.likes = likes;
        }

        public Integer getReport() {
            return report;
        }

        public void setReport(Integer report) {
            this.report = report;
        }

        public Integer getComment() {
            return comment;
        }

        public void setComment(Integer comment) {
            this.comment = comment;
        }

        public Integer getShares() {
            return shares;
        }

        public void setShares(Integer shares) {
            this.shares = shares;
        }

        public Integer getV() {
            return v;
        }

        public void setV(Integer v) {
            this.v = v;
        }

        public Boolean getArticleNewsVideoAvailable() {
            return articleNewsVideoAvailable;
        }

        public void setArticleNewsVideoAvailable(Boolean articleNewsVideoAvailable) {
            this.articleNewsVideoAvailable = articleNewsVideoAvailable;
        }

        public Integer getArticleType() {
            return articleType;
        }

        public void setArticleType(Integer articleType) {
            this.articleType = articleType;
        }

        public String getArticleNewsYouTubeLink() {
            return articleNewsYouTubeLink;
        }

        public void setArticleNewsYouTubeLink(String articleNewsYouTubeLink) {
            this.articleNewsYouTubeLink = articleNewsYouTubeLink;
        }

        public Boolean getLikeStatus() {
            return likeStatus;
        }

        public void setLikeStatus(Boolean likeStatus) {
            this.likeStatus = likeStatus;
        }

    }

    public class Extras implements Serializable
    {
        @SerializedName("msg")
        @Expose
        private String msg;

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public String getMsg() {
            return msg;
        }

        @SerializedName("Data")
        @Expose
        private List<Datum> data = null;
        private final static long serialVersionUID = -8306209157239107398L;

        public List<Datum> getData() {
            return data;
        }

        public void setData(List<Datum> data) {
            this.data = data;
        }

    }

    public class Language implements Serializable
    {

        @SerializedName("LanguageID")
        @Expose
        private String languageID;
        @SerializedName("Language_Name")
        @Expose
        private String languageName;
        private final static long serialVersionUID = -3832078895264856547L;

        public String getLanguageID() {
            return languageID;
        }

        public void setLanguageID(String languageID) {
            this.languageID = languageID;
        }

        public String getLanguageName() {
            return languageName;
        }

        public void setLanguageName(String languageName) {
            this.languageName = languageName;
        }

    }

        @SerializedName("success")
        @Expose
        private Boolean success;
        @SerializedName("extras")
        @Expose
        private Extras extras;
        private final static long serialVersionUID = -5843050117614797004L;

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
