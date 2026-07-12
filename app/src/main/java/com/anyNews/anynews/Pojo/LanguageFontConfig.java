package com.anyNews.anynews.Pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class LanguageFontConfig implements Serializable {

    @SerializedName("title_font_size")
    @Expose
    private Float titleFontSize;

    @SerializedName("body_font_size")
    @Expose
    private Float bodyFontSize;

    @SerializedName("title_font_family")
    @Expose
    private String titleFontFamily;

    @SerializedName("body_font_family")
    @Expose
    private String bodyFontFamily;

    @SerializedName("strip_font_size")
    @Expose
    private Float stripFontSize;

    @SerializedName("programmatic_body_size")
    @Expose
    private Float programmaticBodySize;

    public static LanguageFontConfig of(
            float titleSize,
            float bodySize,
            String titleFamily,
            String bodyFamily,
            float stripSize,
            Float programmaticBody
    ) {
        LanguageFontConfig cfg = new LanguageFontConfig();
        cfg.titleFontSize = titleSize;
        cfg.bodyFontSize = bodySize;
        cfg.titleFontFamily = titleFamily;
        cfg.bodyFontFamily = bodyFamily;
        cfg.stripFontSize = stripSize;
        cfg.programmaticBodySize = programmaticBody;
        return cfg;
    }

    public float getTitleFontSize() {
        return titleFontSize != null ? titleFontSize : 14f;
    }

    public float getBodyFontSize() {
        return bodyFontSize != null ? bodyFontSize : 14f;
    }

    public String getTitleFontFamily() {
        return titleFontFamily;
    }

    public String getBodyFontFamily() {
        return bodyFontFamily;
    }

    public float getStripFontSize() {
        return stripFontSize != null ? stripFontSize : 13f;
    }

    public float getProgrammaticBodySize() {
        return programmaticBodySize != null ? programmaticBodySize : -1f;
    }

    public boolean hasProgrammaticBodySize() {
        return programmaticBodySize != null && programmaticBodySize > 0;
    }
}
