package com.anyNews.anynews.Pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Admin-configurable styling for the bottom-nav footer (icons + labels).
 * Every getter falls back to the EXACT current hardcoded value so an unset /
 * missing config reproduces the current look. Sizes: icon in dp, labels in sp.
 */
public class FooterStyleConfig implements Serializable {

    @SerializedName("icon_size_dp")
    @Expose
    private Float iconSizeDp;

    @SerializedName("label_size_sp")
    @Expose
    private Float labelSizeSp;

    @SerializedName("label_active_size_sp")
    @Expose
    private Float labelActiveSizeSp;

    @SerializedName("label_bold")
    @Expose
    private Boolean labelBold;

    @SerializedName("label_font_family")
    @Expose
    private String labelFontFamily;

    @SerializedName("label_color")
    @Expose
    private String labelColor;

    @SerializedName("label_active_color")
    @Expose
    private String labelActiveColor;

    public float getIconSizeDp() {
        return iconSizeDp != null && iconSizeDp > 0 ? iconSizeDp : 24f;
    }

    public float getLabelSizeSp() {
        return labelSizeSp != null && labelSizeSp > 0 ? labelSizeSp : 11f;
    }

    public float getLabelActiveSizeSp() {
        return labelActiveSizeSp != null && labelActiveSizeSp > 0 ? labelActiveSizeSp : 12f;
    }

    public boolean isLabelBold() {
        return labelBold == null || labelBold;
    }

    public String getLabelFontFamily() {
        return labelFontFamily == null ? "" : labelFontFamily.trim();
    }

    public String getLabelColor() {
        return labelColor == null ? "" : labelColor.trim();
    }

    public String getLabelActiveColor() {
        return labelActiveColor == null ? "" : labelActiveColor.trim();
    }
}
