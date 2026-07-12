package com.anyNews.anynews.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FlipItems {

    public String getPOST_TYPE() {
        return POST_TYPE;
    }

    public void setPOST_TYPE(String POST_TYPE) {
        this.POST_TYPE = POST_TYPE;
    }

    public String getPOSTID() {
        return POSTID;
    }

    public void setPOSTID(String POSTID) {
        this.POSTID = POSTID;
    }

    public String getPOST_POS() {
        return POST_POS;
    }

    public void setPOST_POS(String POST_POS) {
        this.POST_POS = POST_POS;
    }

    public String getVERSION_CODE() {
        return VERSION_CODE;
    }

    public void setVERSION_CODE(String VERSION_CODE) {
        this.VERSION_CODE = VERSION_CODE;
    }

    public String getVERSION_NAME() {
        return VERSION_NAME;
    }

    public void setVERSION_NAME(String VERSION_NAME) {
        this.VERSION_NAME = VERSION_NAME;
    }

    public String getPLATFORM() {
        return PLATFORM;
    }

    public void setPLATFORM(String PLATFORM) {
        this.PLATFORM = PLATFORM;
    }

    public String getAD_Type() {
        return AD_Type;
    }

    public void setAD_Type(String AD_Type) {
        this.AD_Type = AD_Type;
    }

    public String getADImps() {
        return ADImps;
    }

    public void setADImps(String ADImps) {
        this.ADImps = ADImps;
    }

    public String getADERROR() {
        return ADERROR;
    }

    public void setADERROR(String ADERROR) {
        this.ADERROR = ADERROR;
    }

    public String getDATE_TIME() {
        return DATE_TIME;
    }

    public void setDATE_TIME(String DATE_TIME) {
        this.DATE_TIME = DATE_TIME;
    }

    @SerializedName("POST_TYPE")
    @Expose
    private String POST_TYPE;
    @SerializedName("POSTID")
    @Expose
    private String POSTID = null;

    @SerializedName("POST_POS")
    @Expose
    private String POST_POS = null;
    @SerializedName("VERSION_CODE")
    @Expose
    private String VERSION_CODE = null;
    @SerializedName("VERSION_NAME")
    @Expose
    private String VERSION_NAME = null;
    @SerializedName("PLATFORM")
    @Expose
    private String PLATFORM = null;
    @SerializedName("AD_Type")
    @Expose
    private String AD_Type = null;
    @SerializedName("ADImps")
    @Expose
    private String ADImps = null;
    @SerializedName("ADERROR")
    @Expose
    private String ADERROR = null;
    @SerializedName("DATE_TIME")
    @Expose
    private String DATE_TIME = null;
    @SerializedName("DATE_TIME_1")
    @Expose
    private String DATE_TIME_1 = null;

    public String getDATE_TIME_2() {
        return DATE_TIME_2;
    }

    public void setDATE_TIME_2(String DATE_TIME_2) {
        this.DATE_TIME_2 = DATE_TIME_2;
    }

    public String getDATE_TIME_1() {
        return DATE_TIME_1;
    }

    public void setDATE_TIME_1(String DATE_TIME_1) {
        this.DATE_TIME_1 = DATE_TIME_1;
    }

    @SerializedName("DATE_TIME_2")
    @Expose
    private String DATE_TIME_2 = null;
    @SerializedName("EVENT_NAME")
    @Expose
    private String EVENT_NAME = null;
    @SerializedName("DRAFT_ID")
    @Expose
    private int draft_id;


    public String getEVENT_NAME() {
        return EVENT_NAME;
    }

    public void setEVENT_NAME(String EVENT_NAME) {
        this.EVENT_NAME = EVENT_NAME;
    }

    public int getDraft_id() {
        return draft_id;
    }

    public void setDraft_id(int draft_id) {
        this.draft_id = draft_id;
    }
}
