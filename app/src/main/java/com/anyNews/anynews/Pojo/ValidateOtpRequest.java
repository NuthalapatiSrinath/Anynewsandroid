package com.anyNews.anynews.Pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ValidateOtpRequest implements Serializable {
    @SerializedName("draft_id")
    @Expose
    private Integer draftId;
    @SerializedName("country_code")
    @Expose
    private String countryCode;
    @SerializedName("mobile_number")
    @Expose
    private String mobileNumber;
    @SerializedName("otp")
    @Expose
    private String otp;
    private final static long serialVersionUID = -8323032816463788386L;

    public Integer getDraftId() {
        return draftId;
    }

    public void setDraftId(Integer draftId) {
        this.draftId = draftId;
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

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

}
