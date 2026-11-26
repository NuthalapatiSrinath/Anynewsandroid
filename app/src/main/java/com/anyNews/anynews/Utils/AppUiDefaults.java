package com.anyNews.anynews.Utils;

import com.anyNews.anynews.Pojo.LanguageFontConfig;

/**
 * Single source of truth for production admin UI settings baked into the APK.
 * <p>
 * <b>Frozen typography:</b> while {@link FontSettingsHelper#APPLY_REMOTE_FONT_SETTINGS} is
 * {@code false}, splash / edge {@code language_font_settings} and SharedPreferences are ignored
 * for feed and onboarding typography — values here always win.
 * <p>
 * Sourced from production admin ({@code admin.anynews.co.in/configuration}) and aligned with
 * {@code anynewsserver/helpers/fontSettingsDefaults.js} ({@code PRODUCTION_FONT_SETTINGS}),
 * with Telugu numeric sizes calibrated for the app payload ({@code toAppClientMap}: ×300/360).
 * Malayalam (1) and Telugu (3) match live production admin DB ({@code admin.anynews.co.in}).
 * Languages 2, 4, 5, 6 match {@code PRODUCTION_FONT_SETTINGS} (no Telugu-style scaling).
 */
public final class AppUiDefaults {

  /** Brand accent from admin "Brand accent color (theme)". */
  public static final String BRAND_ACCENT_HEX = "#EF4923";

  /** Advertise on AnyNews — sales WhatsApp (digits, incl. country code for wa.me). */
  public static final String ADVERTISE_ON_ANYNEWS_WHATSAPP = "917993531666";

  /**
   * Production admin Configuration ({@code action_icon_size_dp} / {@code like_circle_size_dp}).
   * Used when splash prefs must not resize feed engagement icons.
   */
  public static final int HALF_ACTION_ICON_GLYPH_DP = 21;
  public static final int HALF_ACTION_ICON_CIRCLE_DP = 35;
  public static final int HALF_ACTION_ICON_SLOT_DP = 42;
  public static final int HALF_ACTION_ICON_GAP_DP = 2;

  /**
   * When false, {@link ThemeColorHelper} ignores splash-stored theme_color and always uses
   * {@link #BRAND_ACCENT_HEX}.
   */
  public static final boolean APPLY_REMOTE_THEME_COLOR = false;

  private static final int DEFAULT_FONT_WEIGHT = 400;
  private static final float DEFAULT_LINE_SPACING = 1.0f;
  private static final float DISTRICT_BOX_HEIGHT_DP = 85f;

  private static final float SCREEN_STATE = 21f;
  private static final float SCREEN_DISTRICT = 16f;
  private static final float SCREEN_AREA = 16f;
  private static final float SCREEN_LANGUAGE = 16f;
  private static final float SCREEN_HEADER = 20f;
  private static final float SCREEN_COMMENTS = 14f;
  private static final float SCREEN_PROFILE = 14f;
  private static final float SCREEN_LOGIN = 16f;
  private static final float SCREEN_POST_SUBMIT = 14f;

  private static final int TELUGU_LANGUAGE_ID = 3;
  private static final float SDP_DESIGN_WIDTH = 300f;
  private static final float SDP_REF_DEVICE_WIDTH = 360f;

  private AppUiDefaults() {}

  public static LanguageFontConfig fontConfigFor(int languageId) {
    switch (languageId) {
      case 1:
        return malayalam();
      case 2:
        return hindi();
      case 3:
        return telugu();
      case 4:
        return tamil();
      case 5:
        return kannada();
      case 6:
        return odia();
      default:
        return hindi();
    }
  }

  /**
   * Live production admin — Malayalam section (2026-11-25).
   */
  private static LanguageFontConfig malayalam() {
    return screenRow(13, 13, "roboto_bold", "mallannaregular", 14, 12f, 0.85f);
  }

  private static LanguageFontConfig hindi() {
    return screenRow(16, 13, "robotolighttelugu", "robotolighttelugu", 13, 14f, DEFAULT_LINE_SPACING);
  }

  /**
   * Production admin DB: title 20, body 23 (Mandali), strip 13, line 0.96, weights 500;
   * sizes below are {@code toAppClientMap} sdp indices (admin × 300/360).
   */
  private static LanguageFontConfig telugu() {
    return LanguageFontConfig.compose(
        teluguAppSize(20),
        teluguAppSize(23),
        "sreekrushnadevaraya_regular",
        "mallannaregular",
        teluguAppSize(13),
        null,
        0.96f,
        500,
        500,
        teluguAppSize(23),
        teluguAppSize(18),
        DISTRICT_BOX_HEIGHT_DP,
        teluguAppSize(16),
        teluguAppSize(22),
        teluguAppSize(20),
        teluguAppSize(14),
        teluguAppSize(14),
        teluguAppSize(16),
        teluguAppSize(14)
    );
  }

  private static LanguageFontConfig tamil() {
    return screenRow(13, 13, "mallannaregular", "mallannaregular", 12, 12f, DEFAULT_LINE_SPACING);
  }

  private static LanguageFontConfig kannada() {
    return screenRow(16, 13, "robotolighttelugu", "robotolighttelugu", 13, null, DEFAULT_LINE_SPACING);
  }

  private static LanguageFontConfig odia() {
    return kannada();
  }

  private static LanguageFontConfig screenRow(
      float title,
      float body,
      String titleFont,
      String bodyFont,
      float strip,
      Float programmatic,
      float lineSpacing
  ) {
    return LanguageFontConfig.compose(
        title,
        body,
        titleFont,
        bodyFont,
        strip,
        programmatic,
        lineSpacing,
        DEFAULT_FONT_WEIGHT,
        DEFAULT_FONT_WEIGHT,
        SCREEN_STATE,
        SCREEN_DISTRICT,
        DISTRICT_BOX_HEIGHT_DP,
        SCREEN_AREA,
        SCREEN_LANGUAGE,
        SCREEN_HEADER,
        SCREEN_COMMENTS,
        SCREEN_PROFILE,
        SCREEN_LOGIN,
        SCREEN_POST_SUBMIT
    );
  }

  private static float teluguAppSize(float adminSize) {
    return Math.max(8, Math.round((adminSize * SDP_DESIGN_WIDTH) / SDP_REF_DEVICE_WIDTH));
  }

  /** @return language id when Telugu app calibration applies */
  public static int teluguLanguageId() {
    return TELUGU_LANGUAGE_ID;
  }
}