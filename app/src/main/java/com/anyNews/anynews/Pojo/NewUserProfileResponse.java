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
        private Object approval_rates;

        /** 0–100 profile field completion; preferred for "% Achieved" UI. */
        @SerializedName("profile_completion")
        @Expose
        private Object profileCompletion;

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
        @SerializedName("language_id")
        @Expose
        private Integer languageId;
        @SerializedName("state_id")
        @Expose
        private Integer stateId;
        @SerializedName("dist_id")
        @Expose
        private Integer distId;
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

        /** Optional; server may omit — client shows 0. */
        @SerializedName("comments")
        @Expose
        private Long comments;

        /** Optional; server may omit — client shows 0. Accepts total_views or views. */
        @SerializedName(value = "total_views", alternate = {"views", "totalViews"})
        @Expose
        private Long totalViews;

        /** Soft Profile payment banner (admin-controlled). */
        @SerializedName("upcoming_payment_message")
        @Expose
        private String upcomingPaymentMessage;

        /** Explicit banner active flag from profile (preferred over text-as-show). */
        @SerializedName("reporter_payment_banner_active")
        @Expose
        private Boolean reporterPaymentBannerActive;

        /** My Articles From floor (YYYY-MM-DD) from admin. */
        @SerializedName("reporter_articles_from_date")
        @Expose
        private String reporterArticlesFromDate;

        @SerializedName("reporter_payment_history_notice_active")
        @Expose
        private Boolean reporterPaymentHistoryNoticeActive;

        @SerializedName("reporter_payment_history_notice_text")
        @Expose
        private String reporterPaymentHistoryNoticeText;

        public Long getComments() {
            return comments != null ? comments : 0L;
        }

        public void setComments(Long comments) {
            this.comments = comments;
        }

        public Long getTotalViews() {
            return totalViews != null ? totalViews : 0L;
        }

        public void setTotalViews(Long totalViews) {
            this.totalViews = totalViews;
        }

        public String getUpcomingPaymentMessage() {
            return upcomingPaymentMessage;
        }

        public void setUpcomingPaymentMessage(String upcomingPaymentMessage) {
            this.upcomingPaymentMessage = upcomingPaymentMessage;
        }

        public Boolean getReporterPaymentBannerActive() {
            return reporterPaymentBannerActive;
        }

        public String getReporterArticlesFromDate() {
            return reporterArticlesFromDate;
        }

        public Boolean getReporterPaymentHistoryNoticeActive() {
            return reporterPaymentHistoryNoticeActive;
        }

        public String getReporterPaymentHistoryNoticeText() {
            return reporterPaymentHistoryNoticeText;
        }

        public void setReporterArticlesFromDate(String reporterArticlesFromDate) {
            this.reporterArticlesFromDate = reporterArticlesFromDate;
        }

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

        public Integer getLanguageId() {
            return languageId;
        }

        public void setLanguageId(Integer languageId) {
            this.languageId = languageId;
        }

        public Integer getStateId() {
            return stateId;
        }

        public void setStateId(Integer stateId) {
            this.stateId = stateId;
        }

        public Integer getDistId() {
            return distId;
        }

        public void setDistId(Integer distId) {
            this.distId = distId;
        }

        public Boolean getPanStatus() {
            return panStatus;
        }

        public void setPanStatus(Boolean panStatus) {
            this.panStatus = panStatus;
        }

        public String getApproval_rates() {
            return objectToPctString(approval_rates);
        }

        public void setApproval_rates(String approval_rates) {
            this.approval_rates = approval_rates;
        }

        public String getProfileCompletion() {
            return objectToPctString(profileCompletion);
        }

        public void setProfileCompletion(Object profileCompletion) {
            this.profileCompletion = profileCompletion;
        }

        private static String objectToPctString(Object raw) {
            if (raw == null) return null;
            String s = String.valueOf(raw).replace("%", "").trim();
            return s.isEmpty() || "null".equalsIgnoreCase(s) ? null : s;
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
