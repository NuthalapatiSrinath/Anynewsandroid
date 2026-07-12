package com.anyNews.anynews.Utils;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.TypedValue;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.anyNews.anynews.Pojo.LanguageFontConfig;
import com.anyNews.anynews.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public final class FontSettingsHelper {

    private static final Gson GSON = new Gson();
    private static final Type MAP_TYPE = new TypeToken<Map<String, LanguageFontConfig>>() {}.getType();

    private FontSettingsHelper() {
    }

    public static void saveLanguageFontSettings(Context context, Map<String, LanguageFontConfig> settings) {
        if (context == null || settings == null || settings.isEmpty()) {
            return;
        }
        SharedPrefsData.putString(
                context,
                Constants.LANGUAGE_FONT_SETTINGS,
                GSON.toJson(settings),
                Constants.PREF_NAME
        );
    }

    public static LanguageFontConfig getForLanguage(Context context, int languageId) {
        Map<String, LanguageFontConfig> all = loadAll(context);
        LanguageFontConfig stored = all.get(String.valueOf(languageId));
        if (stored != null) {
            return stored;
        }
        return productionDefault(languageId);
    }

    public static float getStripSize(Context context, int languageId) {
        return getForLanguage(context, languageId).getStripFontSize();
    }

    public static float getProgrammaticBodySize(Context context, int languageId) {
        LanguageFontConfig cfg = getForLanguage(context, languageId);
        if (cfg.hasProgrammaticBodySize()) {
            return cfg.getProgrammaticBodySize();
        }
        return cfg.getBodyFontSize();
    }

    public static void applyArticleTypography(Context context, int languageId, TextView titleView, TextView bodyView) {
        if (context == null) {
            return;
        }
        LanguageFontConfig cfg = getForLanguage(context, languageId);
        if (titleView != null) {
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, cfg.getTitleFontSize());
            applyTypeface(context, titleView, cfg.getTitleFontFamily());
        }
        if (bodyView != null) {
            float bodySize = cfg.hasProgrammaticBodySize()
                    ? cfg.getProgrammaticBodySize()
                    : cfg.getBodyFontSize();
            bodyView.setTextSize(TypedValue.COMPLEX_UNIT_SP, bodySize);
            applyTypeface(context, bodyView, cfg.getBodyFontFamily());
        }
    }

    public static void applyTypeface(Context context, TextView view, String fontFamilyKey) {
        if (context == null || view == null) {
            return;
        }
        if ("sans-serif-medium".equals(fontFamilyKey)) {
            view.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
            return;
        }
        Integer fontRes = resolveFontRes(fontFamilyKey);
        if (fontRes != null) {
            Typeface typeface = ResourcesCompat.getFont(context, fontRes);
            if (typeface != null) {
                view.setTypeface(typeface);
            }
        }
    }

    private static Map<String, LanguageFontConfig> loadAll(Context context) {
        String json = SharedPrefsData.getString(context, Constants.LANGUAGE_FONT_SETTINGS, Constants.PREF_NAME);
        if (TextUtils.isEmpty(json)) {
            return new HashMap<>();
        }
        try {
            Map<String, LanguageFontConfig> parsed = GSON.fromJson(json, MAP_TYPE);
            return parsed != null ? parsed : new HashMap<>();
        } catch (Exception ignored) {
            return new HashMap<>();
        }
    }

    private static Integer resolveFontRes(String fontFamilyKey) {
        if (TextUtils.isEmpty(fontFamilyKey)) {
            return null;
        }
        switch (fontFamilyKey) {
            case "sreekrushnadevaraya_regular":
                return R.font.sreekrushnadevaraya_regular;
            case "mallannaregular":
                return R.font.mallannaregular;
            case "robotolighttelugu":
                return R.font.robotolighttelugu;
            case "roboto_light":
                return R.font.roboto_light;
            case "roboto_medium":
                return R.font.roboto_medium;
            case "roboto_bold":
                return R.font.roboto_bold;
            case "roboto_regular":
                return R.font.roboto_regular;
            case "inter_black":
                return R.font.inter_black;
            case "inter_medium":
                return R.font.inter_medium;
            default:
                return null;
        }
    }

    private static LanguageFontConfig productionDefault(int languageId) {
        switch (languageId) {
            case 1:
                return LanguageFontConfig.of(14, 14, "sans-serif-medium", "mallannaregular", 14, 13f);
            case 2:
                return LanguageFontConfig.of(16, 13, "robotolighttelugu", "robotolighttelugu", 13, 14f);
            case 3:
                return LanguageFontConfig.of(18, 16, "sreekrushnadevaraya_regular", "sreekrushnadevaraya_regular", 13, null);
            case 4:
                return LanguageFontConfig.of(13, 13, "mallannaregular", "mallannaregular", 12, 12f);
            case 5:
            case 6:
                return LanguageFontConfig.of(16, 13, "robotolighttelugu", "robotolighttelugu", 13, null);
            default:
                return LanguageFontConfig.of(14, 14, "robotolighttelugu", "robotolighttelugu", 13, null);
        }
    }
}
