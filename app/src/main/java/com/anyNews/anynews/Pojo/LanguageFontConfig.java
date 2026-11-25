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

    @SerializedName("district_selection_box_height")
    @Expose
    private Float districtSelectionBoxHeight;

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

    /**
     * District picker chip height in true dp (matches XML default 85dp).
     * Clamped to 48–120; missing/invalid values fall back to 85.
     */
    public float getDistrictSelectionBoxHeight() {
        if (districtSelectionBoxHeight == null || districtSelectionBoxHeight <= 0f) {
            return 85f;
        }
        float h = districtSelectionBoxHeight;
        if (h < 48f) return 48f;
        if (h > 120f) return 120f;
        return h;
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

    /**
     * Full production admin font row (feed + onboarding + other screens).
     * See {@link com.anyNews.anynews.Utils.AppUiDefaults}.
     */
    public static LanguageFontConfig compose(
            float titleSize,
            float bodySize,
            String titleFamily,
            String bodyFamily,
            float stripSize,
            Float programmaticBody,
            float lineSpacingMultiplier,
            int titleFontWeight,
            int bodyFontWeight,
            float stateSelectionFontSize,
            float districtSelectionFontSize,
            float districtSelectionBoxHeight,
            float areaSelectionFontSize,
            float languageSelectionFontSize,
            float selectionHeaderFontSize,
            float commentsFontSize,
            float profileFontSize,
            float loginFontSize,
            float postSubmitFontSize
    ) {
        LanguageFontConfig cfg = of(titleSize, bodySize, titleFamily, bodyFamily, stripSize, programmaticBody);
        cfg.lineSpacingMultiplier = lineSpacingMultiplier;
        cfg.titleFontWeight = titleFontWeight;
        cfg.bodyFontWeight = bodyFontWeight;
        cfg.stateSelectionFontSize = stateSelectionFontSize;
        cfg.districtSelectionFontSize = districtSelectionFontSize;
        cfg.districtSelectionBoxHeight = districtSelectionBoxHeight;
        cfg.areaSelectionFontSize = areaSelectionFontSize;
        cfg.languageSelectionFontSize = languageSelectionFontSize;
        cfg.selectionHeaderFontSize = selectionHeaderFontSize;
        cfg.commentsFontSize = commentsFontSize;
        cfg.profileFontSize = profileFontSize;
        cfg.loginFontSize = loginFontSize;
        cfg.postSubmitFontSize = postSubmitFontSize;
        return cfg;
    }

    /** Frozen production defaults — {@link com.anyNews.anynews.Utils.AppUiDefaults}. */
    public static LanguageFontConfig productionFor(int languageId) {
        return com.anyNews.anynews.Utils.AppUiDefaults.fontConfigFor(languageId);
    }
}
