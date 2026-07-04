package com.anyNews.anynews.Pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class UserProfileResponse  implements Serializable {


    public class Data implements Serializable
    {

        @SerializedName("email_id")
        @Expose
        private String emailId;
        @SerializedName("language_id")
        @Expose
        private Integer languageId;
        @SerializedName("location_flag")
        @Expose
        private Boolean locationFlag;
        @SerializedName("notification_flag")
        @Expose
        private Boolean notificationFlag;
        @SerializedName("profile_pic")
        @Expose
        private String profilePic;
        @SerializedName("user_id")
        @Expose
        private Integer userId;
        @SerializedName("user_name")
        @Expose
        private String userName;
        @SerializedName("country_code")
        @Expose
        private String countryCode;
        @SerializedName("mobile_number")
        @Expose
        private String mobileNumber;
        private final static long serialVersionUID = -8677736124340991333L;

        public String getEmailId() {
            return emailId;
        }

        public void setEmailId(String emailId) {
            this.emailId = emailId;
        }

        public Integer getLanguageId() {
            return languageId;
        }

        public void setLanguageId(Integer languageId) {
            this.languageId = languageId;
        }

        public Boolean getLocationFlag() {
            return locationFlag;
        }

        public void setLocationFlag(Boolean locationFlag) {
            this.locationFlag = locationFlag;
        }

        public Boolean getNotificationFlag() {
            return notificationFlag;
        }

        public void setNotificationFlag(Boolean notificationFlag) {
            this.notificationFlag = notificationFlag;
        }

        public String getProfilePic() {
            return profilePic;
        }

        public void setProfilePic(String profilePic) {
            this.profilePic = profilePic;
        }

        public Integer getUserId() {
            return userId;
        }

        public void setUserId(Integer userId) {
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

    }

    public class Extras implements Serializable {
        @SerializedName("Data")
        @Expose
        private Data data;
        private final static long serialVersionUID = -4816148093015487722L;

        public Data getData() {
            return data;
        }

        public void setData(Data data) {
            this.data = data;
        }
    }

    @SerializedName("success")
    @Expose
    private Boolean success;
    @SerializedName("extras")
    @Expose
    private Extras extras;
    private final static long serialVersionUID = -8677736124340991333L;

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
