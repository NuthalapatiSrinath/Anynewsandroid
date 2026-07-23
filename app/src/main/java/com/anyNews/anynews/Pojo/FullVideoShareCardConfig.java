package com.anyNews.anynews.Pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Admin-configurable primary card on the full-video share sheet
 * ("Send to Contact"). Every getter falls back to the EXACT current
 * hardcoded look from {@code bottom_sheet_video_share.xml} / colors.xml.
 *
 * action_url empty → WhatsApp Send-to-Contact video share (default).
 * action_url set → primary tap opens that http(s) URL instead.
 */
public class FullVideoShareCardConfig implements Serializable {

    @SerializedName("title")
    @Expose
    private String title;

    @SerializedName("subtitle")
    @Expose
    private String subtitle;

    @SerializedName("title_color")
    @Expose
    private String titleColor;

    @SerializedName("subtitle_color")
    @Expose
    private String subtitleColor;

    @SerializedName("title_size_sp")
    @Expose
    private Float titleSizeSp;

    @SerializedName("subtitle_size_sp")
    @Expose
    private Float subtitleSizeSp;

    @SerializedName("icon_file")
    @Expose
    private String iconFile;

    @SerializedName("icon_url")
    @Expose
    private String iconUrl;

    @SerializedName("background_color")
    @Expose
    private String backgroundColor;

    @SerializedName("background_image")
    @Expose
    private String backgroundImage;

    @SerializedName("background_image_url")
    @Expose
    private String backgroundImageUrl;

    @SerializedName("action_url")
    @Expose
    private String actionUrl;

    public String getTitle() {
        return title != null && !title.trim().isEmpty() ? title.trim() : "Send to Contact";
    }

    public String getSubtitle() {
        return subtitle != null && !subtitle.trim().isEmpty() ? subtitle.trim() : "Send video to a chat";
    }

    public String getTitleColor() {
        return titleColor != null && !titleColor.trim().isEmpty() ? titleColor.trim() : "#000000";
    }

    public String getSubtitleColor() {
        return subtitleColor != null && !subtitleColor.trim().isEmpty() ? subtitleColor.trim() : "#A2A2A2";
    }

    public float getTitleSizeSp() {
        float v = titleSizeSp != null && titleSizeSp > 0 ? titleSizeSp : 14f;
        if (v < 8f) return 8f;
        if (v > 28f) return 28f;
        return v;
    }

    public float getSubtitleSizeSp() {
        float v = subtitleSizeSp != null && subtitleSizeSp > 0 ? subtitleSizeSp : 11f;
        if (v < 8f) return 8f;
        if (v > 24f) return 24f;
        return v;
    }

    public String getIconUrl() {
        return iconUrl == null ? "" : iconUrl.trim();
    }

    public String getBackgroundColor() {
        return backgroundColor != null && !backgroundColor.trim().isEmpty()
                ? backgroundColor.trim()
                : "#F5F7FB";
    }

    public String getBackgroundImageUrl() {
        return backgroundImageUrl == null ? "" : backgroundImageUrl.trim();
    }

    /** Empty → default WhatsApp Send-to-Contact; non-empty → open URL on tap. */
    public String getActionUrl() {
        return actionUrl == null ? "" : actionUrl.trim();
    }

    public boolean hasCustomActionUrl() {
        String u = getActionUrl();
        return u.startsWith("http://") || u.startsWith("https://");
    }
}
