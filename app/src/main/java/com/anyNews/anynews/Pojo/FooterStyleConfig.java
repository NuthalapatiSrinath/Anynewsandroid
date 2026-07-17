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

    /** Center "+" FAB diameter (activity_home_page.xml fabCustomSize). Default 42. */
    @SerializedName("fab_size_dp")
    @Expose
    private Float fabSizeDp;

    /** Plus logo drawable max size inside the FAB (maxImageSize). Default 30. */
    @SerializedName("fab_icon_size_dp")
    @Expose
    private Float fabIconSizeDp;

    public float getIconSizeDp() {
        return iconSizeDp != null && iconSizeDp > 0 ? iconSizeDp : 24f;
    }

    public float getFabSizeDp() {
        float v = fabSizeDp != null && fabSizeDp > 0 ? fabSizeDp : 42f;
        if (v < 32f) return 32f;
        if (v > 64f) return 64f;
        return v;
    }

    public float getFabIconSizeDp() {
        float v = fabIconSizeDp != null && fabIconSizeDp > 0 ? fabIconSizeDp : 30f;
        if (v < 20f) return 20f;
        if (v > 40f) return 40f;
        return v;
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
