package com.anyNews.anynews.Pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class OtpValidationRequest implements Serializable {

        @SerializedName("PhoneNumber")
        @Expose
        private String phoneNumber;
        @SerializedName("OTP")
        @Expose
        private String oTP;
        @SerializedName("ApiKey")
        @Expose
        private String apiKey;
        @SerializedName("CountryCode")
        @Expose
        private String countryCode;
        private final static long serialVersionUID = -3211377327833715308L;

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public String getOTP() {
            return oTP;
        }

        public void setOTP(String oTP) {
            this.oTP = oTP;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getCountryCode() {
            return countryCode;
        }

        public void setCountryCode(String countryCode) {
            this.countryCode = countryCode;
        }

    }

