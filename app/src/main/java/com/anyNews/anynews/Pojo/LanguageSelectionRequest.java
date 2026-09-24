package com.anyNews.anynews.Pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class LanguageSelectionRequest implements Serializable {


    @SerializedName("data")
    @Expose
    private String data;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
    @SerializedName("draft_id")
    @Expose
    private Integer draftId;

    @SerializedName("region_id")
    @Expose
    private Integer region_id;
    @SerializedName("language_id")
    @Expose
    private Integer languageId;
    @SerializedName("state_id")
    @Expose
    private Integer stateId;



    @SerializedName("dist_id")
    @Expose
    private Integer distId;



    @SerializedName("property")
    @Expose
    private String property;
    @SerializedName("value")
    @Expose
    private Boolean value;

    /** When true, server marks notification_reprompt_converted on enable. */
    @SerializedName("via_reprompt")
    @Expose
    private Boolean viaReprompt;

    @SerializedName("converted")
    @Expose
    private Boolean converted;

    /** true when user tapped Don't allow on POST_NOTIFICATIONS. */
    @SerializedName("denied")
    @Expose
    private Boolean denied;

    /** Alias for converted; false means Don't allow for one-time send stats. */
    @SerializedName("accepted")
    @Expose
    private Boolean accepted;

    @SerializedName("already_enabled")
    @Expose
    private Boolean alreadyEnabled;

    @SerializedName("notification_flag")
    @Expose
    private Boolean notificationFlag;

    /** UI surface where the permission ask was shown (e.g. HomePage). */
    @SerializedName("screen")
    @Expose
    private String screen;

    /** campaign | ask_on_open | install | settings | other */
    @SerializedName("reason")
    @Expose
    private String reason;

    @SerializedName("wave")
    @Expose
    private Integer wave;

    public Integer getRegion_id() {
        return region_id;
    }

    public void setRegion_id(Integer region_id) {
        this.region_id = region_id;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public Boolean getValue() {
        return value;
    }

    public void setValue(Boolean value) {
        this.value = value;
    }

    public Boolean getViaReprompt() {
        return viaReprompt;
    }

    public void setViaReprompt(Boolean viaReprompt) {
        this.viaReprompt = viaReprompt;
    }

    public Boolean getConverted() {
        return converted;
    }

    public void setConverted(Boolean converted) {
        this.converted = converted;
    }

    public Boolean getDenied() {
        return denied;
    }

    public void setDenied(Boolean denied) {
        this.denied = denied;
    }

    public Boolean getAccepted() {
        return accepted;
    }

    public void setAccepted(Boolean accepted) {
        this.accepted = accepted;
    }

    public Boolean getAlreadyEnabled() {
        return alreadyEnabled;
    }

    public void setAlreadyEnabled(Boolean alreadyEnabled) {
        this.alreadyEnabled = alreadyEnabled;
    }

    public Boolean getNotificationFlag() {
        return notificationFlag;
    }

    public void setNotificationFlag(Boolean notificationFlag) {
        this.notificationFlag = notificationFlag;
    }

    public String getScreen() {
        return screen;
    }

    public void setScreen(String screen) {
        this.screen = screen;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Integer getWave() {
        return wave;
    }

    public void setWave(Integer wave) {
        this.wave = wave;
    }

    public Integer getDistId() {
        return distId;
    }

    public void setDistId(Integer distId) {
        this.distId = distId;
    }
    public Integer getDraftId() {
        return draftId;
    }

    public void setDraftId(Integer draftId) {
        this.draftId = draftId;
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

}