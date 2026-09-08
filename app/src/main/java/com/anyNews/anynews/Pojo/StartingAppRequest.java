package com.anyNews.anynews.Pojo;

import androidx.annotation.Keep;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
@Keep
public class StartingAppRequest implements Serializable {
    @SerializedName("INCREMENTAL")
    @Expose
    private String INCREMENTAL;
    @SerializedName("SDK")
    @Expose
    private String SDK;
    @SerializedName("BOARD")
    @Expose
    private String BOARD;
    @SerializedName("HOST")
    @Expose
    private String HOST;
    @SerializedName("FINGERPRINT")
    @Expose
    private String FINGERPRINT;
    @SerializedName("SERIAL")
    @Expose
    private String SERIAL;
    @SerializedName("MODEL")
    @Expose
    private String MODEL;
    @SerializedName("ID")
    @Expose
    private String ID;
    @SerializedName("Manufacture")
    @Expose
    private String Manufacture;
    @SerializedName("brand")
    @Expose
    private String brand;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("user")
    @Expose
    private String user;
    @SerializedName("BASE")
    @Expose
    private String BASE;
    @SerializedName("DeviceID")
    @Expose
    private String deviceID;

    @SerializedName("install_uuid")
    @Expose
    private String install_uuid;

    public String getInstall_uuid() {
        return install_uuid;
    }

    public void setInstall_uuid(String install_uuid) {
        this.install_uuid = install_uuid;
    }

    public String getLanguage_id() {
        return language_id;
    }

    public void setLanguage_id(String language_id) {
        this.language_id = language_id;
    }

    @SerializedName("language_id")
    @Expose
    private String language_id;
    @SerializedName("DeviceType")
    @Expose
    private Integer deviceType;
    @SerializedName("AppVersion")
    @Expose
    private Integer appVersion;
    @SerializedName("DeviceName")
    @Expose
    private String deviceName;
    @SerializedName("draft_id")
    @Expose
    private Integer draftId;
    @SerializedName("ad_id")
    @Expose
    private String adId;
    @SerializedName("fcm_token")
    @Expose
    private String fcmToken;
    @SerializedName("referred_by")
    @Expose
    private Long referredBy;
    @SerializedName("referral_link")
    @Expose
    private String referralLink;

    public String getReferralLink2() {
        return referralLink2;
    }

    public void setReferralLink2(String referralLink2) {
        this.referralLink2 = referralLink2;
    }

    @SerializedName("referral_link2")
    @Expose
    private String referralLink2;



    @SerializedName("app_version_name")
    @Expose
    private String app_version_name;

    public String getApp_version_name() {
        return app_version_name;
    }

    public void setApp_version_name(String app_version_name) {
        this.app_version_name = app_version_name;
    }

    public String getApp_version_number() {
        return app_version_number;
    }

    public void setApp_version_number(String app_version_number) {
        this.app_version_number = app_version_number;
    }

    @SerializedName("app_version_number")
    @Expose
    private String app_version_number;

    @SerializedName("fb_ref_key")
    @Expose
    private String fb_ref_key;

    @SerializedName("latitude")
    @Expose
    private String latitude;

    @SerializedName("longitude")
    @Expose
    private String longitude;

    public String getFb_ref_key() {
        return fb_ref_key;
    }

    public void setFb_ref_key(String fb_ref_key) {
        this.fb_ref_key = fb_ref_key;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }


    private final static long serialVersionUID = 6613519385545259750L;

    public String getINCREMENTAL() {
        return INCREMENTAL;
    }

    public void setINCREMENTAL(String INCREMENTAL) {
        this.INCREMENTAL = INCREMENTAL;
    }

    public String getSDK() {
        return SDK;
    }

    public void setSDK(String SDK) {
        this.SDK = SDK;
    }

    public String getBOARD() {
        return BOARD;
    }

    public void setBOARD(String BOARD) {
        this.BOARD = BOARD;
    }

    public String getHOST() {
        return HOST;
    }

    public void setHOST(String HOST) {
        this.HOST = HOST;
    }

    public String getFINGERPRINT() {
        return FINGERPRINT;
    }

    public void setFINGERPRINT(String FINGERPRINT) {
        this.FINGERPRINT = FINGERPRINT;
    }

    public String getSERIAL() {
        return SERIAL;
    }

    public void setSERIAL(String SERIAL) {
        this.SERIAL = SERIAL;
    }

    public String getMODEL() {
        return MODEL;
    }

    public void setMODEL(String MODEL) {
        this.MODEL = MODEL;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getManufacture() {
        return Manufacture;
    }

    public void setManufacture(String manufacture) {
        Manufacture = manufacture;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getBASE() {
        return BASE;
    }

    public void setBASE(String BASE) {
        this.BASE = BASE;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Long getReferredBy() {
        return referredBy;
    }

    public void setReferredBy(Long referredBy) {
        this.referredBy = referredBy;
    }

    public String getReferralLink() {
        return referralLink;
    }

    public void setReferralLink(String referralLink) {
        this.referralLink = referralLink;
    }


    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public Integer getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(Integer deviceType) {
        this.deviceType = deviceType;
    }

    public Integer getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(Integer appVersion) {
        this.appVersion = appVersion;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public Integer getDraftId() {
        return draftId;
    }

    public void setDraftId(Integer draftId) {
        this.draftId = draftId;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public String getAdId() {
        return adId;
    }

    public void setAdId(String adId) {
        this.adId = adId;
    }

}
