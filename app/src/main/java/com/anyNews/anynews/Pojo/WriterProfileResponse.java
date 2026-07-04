package com.anyNews.anynews.Pojo;

import com.anyNews.anynews.models.FollowersProfileModel;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class WriterProfileResponse {
    public class Data {

        @SerializedName("name")
        @Expose
        private String name;
        @SerializedName("role")
        @Expose
        private String role;
        @SerializedName("articles")
        @Expose
        private Integer articles;
        @SerializedName("followers")
        @Expose
        private Integer followers;
        @SerializedName("following")
        @Expose
        private Integer following;
        @SerializedName("likes")
        @Expose
        private Integer likes;
        @SerializedName("profile_pic")
        @Expose
        private String profilePic;
        @SerializedName("is_follow")
        @Expose
        private Integer isFollow;
        @SerializedName("location")
        @Expose
        private String location;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public Integer getArticles() {
            return articles;
        }

        public void setArticles(Integer articles) {
            this.articles = articles;
        }

        public Integer getFollowers() {
            return followers;
        }

        public void setFollowers(Integer followers) {
            this.followers = followers;
        }

        public Integer getFollowing() {
            return following;
        }

        public void setFollowing(Integer following) {
            this.following = following;
        }

        public Integer getLikes() {
            return likes;
        }

        public void setLikes(Integer likes) {
            this.likes = likes;
        }

        public String getProfilePic() {
            return profilePic;
        }

        public void setProfilePic(String profilePic) {
            this.profilePic = profilePic;
        }

        public Integer getIsFollow() {
            return isFollow;
        }

        public void setIsFollow(Integer isFollow) {
            this.isFollow = isFollow;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

}
    @SerializedName("success")
    @Expose
    private Boolean success;
    @SerializedName("data")
    @Expose
    private Data data;

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

}
