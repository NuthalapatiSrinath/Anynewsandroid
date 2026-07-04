package com.anyNews.anynews.Pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ValidateOtpResponse implements Serializable {


    public class Data implements Serializable
    {


        @SerializedName("user_id")
        @Expose
        private Integer userId;
        @SerializedName("country_code")
        @Expose
        private String countryCode;
        @SerializedName("mobile_number")
        @Expose
        private String mobileNumber;
        @SerializedName("user_name")
        @Expose
        private String userName;
        @SerializedName("email_id")
        @Expose
        private String emailId;
        @SerializedName("profile_pic")
        @Expose
        private String profilePic;
        private final static long serialVersionUID = -5867418139714542685L;

        public Integer getUserId() {
            return userId;
        }

        public void setUserId(Integer userId) {
            this.userId = userId;
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

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getEmailId() {
            return emailId;
        }

        public void setEmailId(String emailId) {
            this.emailId = emailId;
        }

        public String getProfilePic() {
            return profilePic;
        }

        public void setProfilePic(String profilePic) {
            this.profilePic = profilePic;
        }
    }


    public class Extras implements Serializable
    {

        @SerializedName("Status")
        @Expose
        private String status;
        @SerializedName("Data")
        @Expose
        private Data data;
        private final static long serialVersionUID = -351983868329942769L;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Data getData() {
            return data;
        }

        public void setData(Data data) {
            this.data = data;
        }
    }


    public class Language implements Serializable
    {

        @SerializedName("Language_Name")
        @Expose
        private String languageName;
        @SerializedName("LanguageID")
        @Expose
        private String languageID;
        private final static long serialVersionUID = -3832078895264856547L;

        public String getLanguageName() {
            return languageName;
        }

        public void setLanguageName(String languageName) {
            this.languageName = languageName;
        }

        public String getLanguageID() {
            return languageID;
        }

        public void setLanguageID(String languageID) {
            this.languageID = languageID;
        }

    }





    @SerializedName("success")
    @Expose
    private Boolean success;
    @SerializedName("extras")
    @Expose
    private Extras extras;
    private final static long serialVersionUID = 2882389038047626877L;

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













    public class UserImageData implements Serializable
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
        private final static long serialVersionUID = 958762078557228572L;

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


}
