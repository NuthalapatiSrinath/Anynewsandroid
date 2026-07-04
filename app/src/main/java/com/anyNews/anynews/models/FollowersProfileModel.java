package com.anyNews.anynews.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FollowersProfileModel {

    @SerializedName("success")
    @Expose
    private Boolean success;
    @SerializedName("data")
    @Expose
    private List<Datum> data = null;

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

    public static class Datum {

        @SerializedName("writer_id")
        @Expose
        private Integer writerId;
        @SerializedName("writer_name")
        @Expose
        private String writerName;
        @SerializedName("profile_pic")
        @Expose
        private String profilePic;
        @SerializedName("is_follow")
        @Expose
        private Integer isFollow;
        @SerializedName("role")
        @Expose
        private String role;
        @SerializedName("location")
        @Expose
        private String location;
        @SerializedName("followers")
        @Expose
        private Object followers;

        public Integer getWriterId() {
            return writerId;
        }

        public void setWriterId(Integer writerId) {
            this.writerId = writerId;
        }

        public String getWriterName() {
            return writerName;
        }

        public void setWriterName(String writerName) {
            this.writerName = writerName;
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

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getFollowers() {
            return followers == null ? null : String.valueOf(followers);
        }

        public void setFollowers(Object followers) {
            this.followers = followers;
        }

    }


}
