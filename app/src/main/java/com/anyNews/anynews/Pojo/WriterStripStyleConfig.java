package com.anyNews.anynews.Pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Admin-configurable styling for the half-post writer strip (writer name /
 * designation / time) and the right-side location pill. Every getter falls
 * back to the EXACT current hardcoded value from the half-feed layouts so an
 * unset / missing config reproduces the current look. Text sizes are in sdp
 * (the layouts use @dimen/_Nsdp); the location offset is in dp.
 */
public class WriterStripStyleConfig implements Serializable {

    @SerializedName("name_size_sdp")
    @Expose
    private Float nameSizeSdp;

    @SerializedName("name_bold")
    @Expose
    private Boolean nameBold;

    @SerializedName("name_font_family")
    @Expose
    private String nameFontFamily;

    @SerializedName("name_color")
    @Expose
    private String nameColor;

    @SerializedName("designation_size_sdp")
    @Expose
    private Float designationSizeSdp;

    @SerializedName("designation_bold")
    @Expose
    private Boolean designationBold;

    @SerializedName("designation_font_family")
    @Expose
    private String designationFontFamily;

    @SerializedName("designation_color")
    @Expose
    private String designationColor;

    @SerializedName("time_size_sdp")
    @Expose
    private Float timeSizeSdp;

    @SerializedName("time_bold")
    @Expose
    private Boolean timeBold;

    @SerializedName("time_font_family")
    @Expose
    private String timeFontFamily;

    @SerializedName("time_color")
    @Expose
    private String timeColor;

    @SerializedName("location_size_sdp")
    @Expose
    private Float locationSizeSdp;

    @SerializedName("location_color")
    @Expose
    private String locationColor;

    @SerializedName("location_font_family")
    @Expose
    private String locationFontFamily;

    @SerializedName("location_right_offset_dp")
    @Expose
    private Float locationRightOffsetDp;

    public float getNameSizeSdp() {
        return nameSizeSdp != null && nameSizeSdp > 0 ? nameSizeSdp : 8f;
    }

    public boolean isNameBold() {
        return nameBold == null || nameBold;
    }

    public String getNameFontFamily() {
        return nameFontFamily == null ? "" : nameFontFamily.trim();
    }

    /**
     * Returns the admin-set name color, or {@code null} when unset so the
     * renderer can fall back to a theme-aware default (readable in light AND
     * dark mode). An explicit admin value still wins.
     */
    public String getNameColor() {
        return isBlank(nameColor) ? null : nameColor.trim();
    }

    public float getDesignationSizeSdp() {
        return designationSizeSdp != null && designationSizeSdp > 0 ? designationSizeSdp : 4f;
    }

    public boolean isDesignationBold() {
        return designationBold == null || designationBold;
    }

    public String getDesignationFontFamily() {
        return designationFontFamily == null ? "" : designationFontFamily.trim();
    }

    public String getDesignationColor() {
        return isBlank(designationColor) ? null : designationColor.trim();
    }

    public float getTimeSizeSdp() {
        return timeSizeSdp != null && timeSizeSdp > 0 ? timeSizeSdp : 4f;
    }

    public boolean isTimeBold() {
        return timeBold == null || timeBold;
    }

    public String getTimeFontFamily() {
        return timeFontFamily == null ? "" : timeFontFamily.trim();
    }

    public String getTimeColor() {
        return isBlank(timeColor) ? null : timeColor.trim();
    }

    public float getLocationSizeSdp() {
        return locationSizeSdp != null && locationSizeSdp > 0 ? locationSizeSdp : 12f;
    }

    public String getLocationColor() {
        return isBlank(locationColor) ? null : locationColor.trim();
    }

    public String getLocationFontFamily() {
        return locationFontFamily == null ? "" : locationFontFamily.trim();
    }

    public float getLocationRightOffsetDp() {
        return locationRightOffsetDp != null ? locationRightOffsetDp : 0f;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
