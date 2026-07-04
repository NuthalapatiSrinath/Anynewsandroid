package com.anyNews.anynews.Pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class NewUserProfileResponse {

    public class Data {

        @SerializedName("user_id")
        @Expose
        private Long userId;
        @SerializedName("user_name")
        @Expose
        private String userName;
        @SerializedName("country_code")
        @Expose
        private String countryCode;
        @SerializedName("mobile_number")
        @Expose
        private String mobileNumber;

        @SerializedName("approval_rates")
        @Expose
        private String approval_rates;

        @SerializedName("profile_pic")
        @Expose
        private String profilePic;
        @SerializedName("total_points")
        @Expose
        private Long totalPoints;
        @SerializedName("available_points")
        @Expose
        private Long availablePoints;
        @SerializedName("following")
        @Expose
        private Long following;
        @SerializedName("ref_link")
        @Expose
        private String refLink;
        @SerializedName("role")
        @Expose
        private String role;
        @SerializedName("required_points")
        @Expose
        private Long required_points;
        @SerializedName("is_reporter")
        @Expose
        private Boolean isReporter;
        @SerializedName("location")
        @Expose
        private String location;
        @SerializedName("pan_status")
        @Expose
        private Boolean panStatus;
        @SerializedName("articles")
        @Expose
        private Long articles;

        @SerializedName("followers")
        @Expose
        private Long followers;
        @SerializedName("likes")
        @Expose
        private Long likes;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        @SerializedName("email")
        @Expose
        private String email;

        public Long getArticles() {
            return articles;
        }

        public void setArticles(Long articles) {
            this.articles = articles;
        }


        public Long getFollowers() {
            return followers;
        }

        public void setFollowers(Long followers) {
            this.followers = followers;
        }

        public Long getLikes() {
            return likes;
        }

        public void setLikes(Long likes) {
            this.likes = likes;
        }
        public Long getUserId() {
            return userId;
        }

        public void setUserId(long userId) {
            this.userId = userId;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getCountryCode() {
            return countryCode;
        }

        public void setCountryCode(String countryCode) {
            this.countryCode = countryCode;
        }

        public String getMobileNumber() {
            return mobileNumber;
        }

        public void setMobileNumber(String mobileNumber) {
            this.mobileNumber = mobileNumber;
        }

        public String getProfilePic() {
            return profilePic;
        }

        public void setProfilePic(String profilePic) {
            this.profilePic = profilePic;
        }

        public long getTotalPoints() {
            return totalPoints;
        }

        public void setTotalPoints(Long totalPoints) {
            this.totalPoints = totalPoints;
        }

        public Long getAvailablePoints() {
            return availablePoints;
        }

        public void setAvailablePoints(Long availablePoints) {
            this.availablePoints = availablePoints;
        }

        public Long getFollowing() {
            return following;
        }

        public void setFollowing(Long following) {
            this.following = following;
        }

        public String getRefLink() {
            return refLink;
        }

        public void setRefLink(String refLink)
        {
            this.refLink = refLink;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public Long getRedeemValue() {
            return required_points;
        }

        public void setRedeemValue(Long required_points) {
            this.required_points = required_points;
        }

        public Boolean getIsReporter() {
            return isReporter;
        }

        public void setIsReporter(Boolean isReporter) {
            this.isReporter = isReporter;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public Boolean getPanStatus() {
            return panStatus;
        }

        public void setPanStatus(Boolean panStatus) {
            this.panStatus = panStatus;
        }

        public String getApproval_rates() {
            return approval_rates;
        }

        public void setApproval_rates(String approval_rates) {
            this.approval_rates = approval_rates;
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
