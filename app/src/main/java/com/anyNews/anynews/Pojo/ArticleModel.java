package com.anyNews.anynews.Pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ArticleModel implements Serializable {


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
        @SerializedName("Like_Status")
        @Expose
        private Boolean likeStatus;
        private final static long serialVersionUID = -4900474440129774753L;



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

        public Boolean getLikeStatus() {
            return likeStatus;
        }

        public void setLikeStatus(Boolean likeStatus) {
            this.likeStatus = likeStatus;
        }


}
