package com.anyNews.anynews.Pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Plain JSON from {@code GET /app/v3/public/splash_config} (Cloudflare edge).
 * Shared shell only — draft/login still require POST Splash_Screen.
 * Includes Opening Banner fields so open can apply banner URL from CF when
 * the edge responds before (or without waiting on) origin Splash.
 */
public class PublicEdgeSplashResponse {

    @SerializedName("success")
    @Expose
    private Boolean success;

    @SerializedName("edge")
    @Expose
    private Boolean edge;

    @SerializedName("language_id")
    @Expose
    private Integer language_id;

    @SerializedName("admob_app_id")
    @Expose
    private String admob_app_id;

    @SerializedName("show_ads")
    @Expose
    private Boolean show_ads;

    @SerializedName("default_ad_position")
    @Expose
    private Integer default_ad_position;

    @SerializedName("festival_splash_enabled")
    @Expose
    private Boolean festival_splash_enabled;

    @SerializedName("festival_splash_show_on_push")
    @Expose
    private Boolean festival_splash_show_on_push;

    @SerializedName("festival_splash_show_on_share")
    @Expose
    private Boolean festival_splash_show_on_share;

    @SerializedName("festival_splash_duration_seconds")
    @Expose
    private Integer festival_splash_duration_seconds;

    @SerializedName("festival_splash_image_url")
    @Expose
    private String festival_splash_image_url;

    @SerializedName("festival_splash_updated_at")
    @Expose
    private Long festival_splash_updated_at;

    public Boolean getSuccess() {
        return success;
    }

    public Boolean getEdge() {
        return edge;
    }

    public Integer getLanguage_id() {
        return language_id;
    }

    public String getAdmob_app_id() {
        return admob_app_id;
    }

    public Boolean getShow_ads() {
        return show_ads;
    }

    public Integer getDefault_ad_position() {
        return default_ad_position;
    }

    public Boolean getFestival_splash_enabled() {
        return festival_splash_enabled;
    }

    public Boolean getFestival_splash_show_on_push() {
        return festival_splash_show_on_push;
    }

    public Boolean getFestival_splash_show_on_share() {
        return festival_splash_show_on_share;
    }

    public Integer getFestival_splash_duration_seconds() {
        return festival_splash_duration_seconds;
    }

    public String getFestival_splash_image_url() {
        return festival_splash_image_url;
    }

    public Long getFestival_splash_updated_at() {
        return festival_splash_updated_at;
    }
}
