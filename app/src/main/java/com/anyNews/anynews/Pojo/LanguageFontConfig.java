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

    @SerializedName("line_spacing_multiplier")
    @Expose
    private Float lineSpacingMultiplier;

    @SerializedName("title_font_weight")
    @Expose
    private Integer titleFontWeight;

    @SerializedName("body_font_weight")
    @Expose
    private Integer bodyFontWeight;

    @SerializedName("state_selection_font_size")
    @Expose
    private Float stateSelectionFontSize;

    @SerializedName("district_selection_font_size")
    @Expose
    private Float districtSelectionFontSize;

    @SerializedName("area_selection_font_size")
    @Expose
    private Float areaSelectionFontSize;

    @SerializedName("language_selection_font_size")
    @Expose
    private Float languageSelectionFontSize;

    @SerializedName("selection_header_font_size")
    @Expose
    private Float selectionHeaderFontSize;

    @SerializedName("comments_font_size")
    @Expose
    private Float commentsFontSize;

    @SerializedName("profile_font_size")
    @Expose
    private Float profileFontSize;

    @SerializedName("login_font_size")
    @Expose
    private Float loginFontSize;

    @SerializedName("post_submit_font_size")
    @Expose
    private Float postSubmitFontSize;

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

    /** Feed body line-spacing multiplier. Defaults to 1.0 (neutral) when unset. */
    public float getLineSpacingMultiplier() {
        if (lineSpacingMultiplier == null || lineSpacingMultiplier <= 0f) {
            return 1.0f;
        }
        // Clamp to the same range the admin/backend enforce.
        if (lineSpacingMultiplier < 0.5f) return 0.5f;
        if (lineSpacingMultiplier > 3.0f) return 3.0f;
        return lineSpacingMultiplier;
    }

    /**
     * CSS-style font weight for article titles (100..900).
     * Default 400 matches FontSettingsHelper's historical Typeface.NORMAL apply path.
     */
    public int getTitleFontWeight() {
        return clampFontWeight(titleFontWeight);
    }

    /**
     * CSS-style font weight for article body (100..900).
     * Default 400 matches FontSettingsHelper's historical Typeface.NORMAL apply path.
     */
    public int getBodyFontWeight() {
        return clampFontWeight(bodyFontWeight);
    }

    private static int clampFontWeight(Integer raw) {
        if (raw == null || raw <= 0) {
            return 400;
        }
        int w = raw;
        if (w < 100) w = 100;
        if (w > 900) w = 900;
        // Snap to nearest 100 to match admin slider steps.
        return Math.round(w / 100f) * 100;
    }

    public float getStateSelectionFontSize() {
        return stateSelectionFontSize != null ? stateSelectionFontSize : 21f;
    }

    public float getDistrictSelectionFontSize() {
        return districtSelectionFontSize != null ? districtSelectionFontSize : 16f;
    }

    public float getAreaSelectionFontSize() {
        return areaSelectionFontSize != null ? areaSelectionFontSize : 16f;
    }

    public float getLanguageSelectionFontSize() {
        return languageSelectionFontSize != null ? languageSelectionFontSize : 16f;
    }

    public float getSelectionHeaderFontSize() {
        return selectionHeaderFontSize != null ? selectionHeaderFontSize : 20f;
    }

    public float getCommentsFontSize() {
        return commentsFontSize != null ? commentsFontSize : 14f;
    }

    public float getProfileFontSize() {
        return profileFontSize != null ? profileFontSize : 14f;
    }

    public float getLoginFontSize() {
        return loginFontSize != null ? loginFontSize : 16f;
    }

    public float getPostSubmitFontSize() {
        return postSubmitFontSize != null ? postSubmitFontSize : 14f;
    }

    /** Production defaults including onboarding/screen-specific sizes. */
    public static LanguageFontConfig productionFor(int languageId) {
        LanguageFontConfig cfg;
        switch (languageId) {
            case 1:
                cfg = of(14, 14, "sans-serif-medium", "mallannaregular", 14, 13f);
                break;
            case 2:
                cfg = of(16, 13, "robotolighttelugu", "robotolighttelugu", 13, 14f);
                break;
            case 3:
                cfg = of(18, 16, "sreekrushnadevaraya_regular", "sreekrushnadevaraya_regular", 13, null);
                cfg.stateSelectionFontSize = 22f;
                cfg.districtSelectionFontSize = 18f;
                break;
            case 4:
                cfg = of(13, 13, "mallannaregular", "mallannaregular", 12, 12f);
                break;
            case 5:
            case 6:
                cfg = of(16, 13, "robotolighttelugu", "robotolighttelugu", 13, null);
                break;
            default:
                cfg = of(14, 14, "robotolighttelugu", "robotolighttelugu", 13, null);
                break;
        }
        if (cfg.stateSelectionFontSize == null) {
            cfg.stateSelectionFontSize = 21f;
        }
        if (cfg.districtSelectionFontSize == null) {
            cfg.districtSelectionFontSize = 16f;
        }
        cfg.areaSelectionFontSize = 16f;
        cfg.languageSelectionFontSize = 16f;
        cfg.selectionHeaderFontSize = 20f;
        cfg.commentsFontSize = 14f;
        cfg.profileFontSize = 14f;
        cfg.loginFontSize = 16f;
        cfg.postSubmitFontSize = 14f;
        // Explicit defaults so offline productionDefault matches current look
        // (applyTypeface with Typeface.NORMAL / no weight overlay).
        cfg.titleFontWeight = 400;
        cfg.bodyFontWeight = 400;
        return cfg;
    }
}
