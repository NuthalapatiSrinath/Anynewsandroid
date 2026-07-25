package com.anyNews.anynews.Pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Admin-configured poll / survey option colors from splash
 * ({@code poll_survey_colors} on root_settings).
 */
public class PollSurveyColorsConfig {

    @SerializedName("light_fill")
    @Expose
    private String lightFill;

    @SerializedName("light_text")
    @Expose
    private String lightText;

    @SerializedName("dark_fill")
    @Expose
    private String darkFill;

    @SerializedName("dark_text")
    @Expose
    private String darkText;

    public String getLightFill() {
        return lightFill;
    }

    public void setLightFill(String lightFill) {
        this.lightFill = lightFill;
    }

    public String getLightText() {
        return lightText;
    }

    public void setLightText(String lightText) {
        this.lightText = lightText;
    }

    public String getDarkFill() {
        return darkFill;
    }

    public void setDarkFill(String darkFill) {
        this.darkFill = darkFill;
    }

    public String getDarkText() {
        return darkText;
    }

    public void setDarkText(String darkText) {
        this.darkText = darkText;
    }
}
