package com.anyNews.anynews.Pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class EncreptionRequest {

    public Boolean getShow_push_perm_alert() {
        return show_push_perm_alert;
    }

    public void setShow_push_perm_alert(Boolean show_push_perm_alert) {
        this.show_push_perm_alert = show_push_perm_alert;
    }

    @SerializedName("show_push_perm_alert")
    @Expose
    private Boolean show_push_perm_alert;
    @SerializedName("data")
    @Expose
    private String data;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public CardsDisplay getCards_display() {
        return cards_display;
    }

    public void setCards_display(CardsDisplay cards_display) {
        this.cards_display = cards_display;
    }

    @SerializedName("cards_display")
    @Expose
    private CardsDisplay cards_display;

    @SerializedName("update_menu_status")
    @Expose
    private Integer updateMenuStatus;

    @SerializedName("callAppEvents")
    @Expose
    private boolean callAppEvents;

    public boolean isShow_ads() {
        return show_ads;
    }

    public void setShow_ads(boolean show_ads) {
        this.show_ads = show_ads;
    }

    @SerializedName("show_ads")
    @Expose
    private boolean show_ads;

    public boolean isUpdate_user_language() {
        return update_user_language;
    }

    public void setUpdate_user_language(boolean update_user_language) {
        this.update_user_language = update_user_language;
    }

    @SerializedName("update_user_language")
    @Expose
    private boolean update_user_language;

    public boolean isShow_reporter_login() {
        return show_reporter_login;
    }

    public void setShow_reporter_login(boolean show_reporter_login) {
        this.show_reporter_login = show_reporter_login;
    }

    @SerializedName("show_reporter_login")
    @Expose
    private boolean show_reporter_login;

    public List<AdsPojo> getAd_positions() {
        return ad_positions;
    }

    public void setAd_positions(List<AdsPojo> ad_positions) {
        this.ad_positions = ad_positions;
    }

    @SerializedName("ads_data")
    @Expose
    private List<AdsPojo> ad_positions;

    @SerializedName("admob_app_id")
    @Expose
    private String admob_app_id;

    public String getAdmob_app_id() {
        return admob_app_id;
    }

    public void setAdmob_app_id(String admob_app_id) {
        this.admob_app_id = admob_app_id;
    }

    @SerializedName("engagement_badge_color")
    @Expose
    private String engagement_badge_color;

    @SerializedName("theme_color")
    @Expose
    private String theme_color;

    public String getEngagement_badge_color() {
        return engagement_badge_color;
    }

    public void setEngagement_badge_color(String engagement_badge_color) {
        this.engagement_badge_color = engagement_badge_color;
    }

    public String getTheme_color() {
        return theme_color;
    }

    public void setTheme_color(String theme_color) {
        this.theme_color = theme_color;
    }

    public Integer getUpdateMenuStatus() {
        return updateMenuStatus;
    }

    public void setUpdateMenuStatus(Integer updateMenuStatus) {
        this.updateMenuStatus = updateMenuStatus;
    }

    public boolean isCallAppEvents() {
        return callAppEvents;
    }

    public void setCallAppEvents(boolean callAppEvents) {
        this.callAppEvents = callAppEvents;
    }

}
