package com.anyNews.anynews.Utils;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.TextViewCompat;

import com.anyNews.anynews.Pojo.LanguageFontConfig;
import com.anyNews.anynews.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public final class FontSettingsHelper {

    public static final String TAG = "AnyNewsFont";
    private static final Gson GSON = new Gson();
    private static final Type MAP_TYPE = new TypeToken<Map<String, LanguageFontConfig>>() {}.getType();

    private FontSettingsHelper() {
    }

    public static void saveLanguageFontSettings(Context context, Map<String, LanguageFontConfig> settings) {
        if (context == null || settings == null || settings.isEmpty()) {
            Log.w(TAG, "saveLanguageFontSettings skipped: context/settings empty");
            return;
        }
        String json = GSON.toJson(settings);
        SharedPrefsData.putString(
                context,
                Constants.LANGUAGE_FONT_SETTINGS,
                json,
                Constants.PREF_NAME
        );
        Log.d(TAG, "saveLanguageFontSettings saved " + settings.size() + " languages, jsonLen=" + json.length());
        for (Map.Entry<String, LanguageFontConfig> entry : settings.entrySet()) {
            Log.d(TAG, "saved lang=" + entry.getKey() + " config=" + describeConfig(entry.getValue()));
        }
    }

    public static LanguageFontConfig getForLanguage(Context context, int languageId) {
        Map<String, LanguageFontConfig> all = loadAll(context);
        LanguageFontConfig stored = all.get(String.valueOf(languageId));
        LanguageFontConfig resolved = stored != null ? sanitizeConfig(stored, languageId) : productionDefault(languageId);
        String source = stored != null ? "prefs" : "productionDefault";
        Log.d(TAG, "getForLanguage lang=" + languageId + " source=" + source + " config=" + describeConfig(resolved));
        return resolved;
    }

    public static float getStripSize(Context context, int languageId) {
        float size = getForLanguage(context, languageId).getStripFontSize();
        Log.d(TAG, "getStripSize lang=" + languageId + " size=" + size);
        return size;
    }

    public static float getProgrammaticBodySize(Context context, int languageId) {
        LanguageFontConfig cfg = getForLanguage(context, languageId);
        float size = resolveBodySize(cfg);
        Log.d(TAG, "getProgrammaticBodySize lang=" + languageId + " size=" + size);
        return size;
    }

    public static float getPostSubmitSize(Context context, int languageId) {
        return getForLanguage(context, languageId).getPostSubmitFontSize();
    }

    public static void applyTextSize(Context context, TextView view, float sizeSdp) {
        setTextSizeSdp(context, view, sizeSdp);
    }

    public static void applySelectionHeader(Context context, int languageId, TextView headerView) {
        if (context == null || headerView == null) {
            return;
        }
        LanguageFontConfig cfg = getForLanguage(context, languageId);
        setTextSizeSdp(context, headerView, cfg.getSelectionHeaderFontSize());
        applyTypeface(context, headerView, cfg.getTitleFontFamily());
    }

    public static void applyStateSelectionTypography(Context context, int languageId, TextView... labels) {
        applySelectionTypography(context, languageId, getForLanguage(context, languageId).getStateSelectionFontSize(), labels);
    }

    public static void applyDistrictSelectionTypography(Context context, int languageId, TextView... labels) {
        applySelectionTypography(context, languageId, getForLanguage(context, languageId).getDistrictSelectionFontSize(), labels);
    }

    public static void applyAreaSelectionTypography(Context context, int languageId, TextView... labels) {
        applySelectionTypography(context, languageId, getForLanguage(context, languageId).getAreaSelectionFontSize(), labels);
    }

    public static void applyLanguageSelectionTypography(Context context, int languageId, TextView... labels) {
        applySelectionTypography(context, languageId, getForLanguage(context, languageId).getLanguageSelectionFontSize(), labels);
    }

    private static void applySelectionTypography(Context context, int languageId, float sizeSdp, TextView... labels) {
        if (context == null || labels == null) {
            return;
        }
        LanguageFontConfig cfg = getForLanguage(context, languageId);
        for (TextView label : labels) {
            if (label == null) {
                continue;
            }
            setTextSizeSdp(context, label, sizeSdp);
            applyTypeface(context, label, cfg.getBodyFontFamily());
        }
    }

    public static void applyArticleTypography(Context context, int languageId, TextView titleView, TextView bodyView) {
        applyArticleTypography(context, languageId, titleView, bodyView, "card");
    }

    public static void applyArticleTypography(
            Context context,
            int languageId,
            TextView titleView,
            TextView bodyView,
            String source
    ) {
        if (context == null) {
            Log.e(TAG, "applyArticleTypography skipped: null context source=" + source);
            return;
        }
        LanguageFontConfig cfg = getForLanguage(context, languageId);
        float titleSize = cfg.getTitleFontSize();
        float bodySize = resolveBodySize(cfg);
        String titleFamily = cfg.getTitleFontFamily();
        String bodyFamily = cfg.getBodyFontFamily();

        Log.d(
                TAG,
                "applyArticleTypography source=" + source
                        + " lang=" + languageId
                        + " titleSize=" + titleSize
                        + " bodySize=" + bodySize
                        + " titleFont=" + titleFamily
                        + " bodyFont=" + bodyFamily
                        + " titleView=" + viewLabel(titleView)
                        + " bodyView=" + viewLabel(bodyView)
        );

        if (titleView != null) {
            setTextSizeSdp(context, titleView, titleSize);
            applyTypeface(context, titleView, titleFamily);
            Log.d(TAG, "applied title pxSize=" + titleView.getTextSize() + " on " + viewLabel(titleView));
        }
        if (bodyView != null) {
            setTextSizeSdp(context, bodyView, bodySize);
            applyTypeface(context, bodyView, bodyFamily);
            Log.d(TAG, "applied body pxSize=" + bodyView.getTextSize() + " on " + viewLabel(bodyView));
        }
    }

    public static void applyTypeface(Context context, TextView view, String fontFamilyKey) {
        if (context == null || view == null) {
            return;
        }
        if ("sans-serif-medium".equals(fontFamilyKey)) {
            view.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
            Log.d(TAG, "typeface sans-serif-medium on " + viewLabel(view));
            return;
        }
        Integer fontRes = resolveFontRes(fontFamilyKey);
        if (fontRes != null) {
            Typeface typeface = ResourcesCompat.getFont(context, fontRes);
            if (typeface != null) {
                view.setTypeface(typeface);
                Log.d(TAG, "typeface " + fontFamilyKey + " res=" + fontRes + " on " + viewLabel(view));
            } else {
                Log.e(TAG, "typeface load failed key=" + fontFamilyKey + " res=" + fontRes);
            }
        } else {
            Log.w(TAG, "typeface unknown key=" + fontFamilyKey + " on " + viewLabel(view));
        }
    }

    /** Admin "Body size" drives article body; programmatic_body_size is legacy-only. */
    private static float resolveBodySize(LanguageFontConfig cfg) {
        float body = cfg.getBodyFontSize();
        if (body > 0f) {
            return body;
        }
        if (cfg.hasProgrammaticBodySize()) {
            return cfg.getProgrammaticBodySize();
        }
        return 14f;
    }

    private static LanguageFontConfig sanitizeConfig(LanguageFontConfig raw, int languageId) {
        return raw != null ? raw : productionDefault(languageId);
    }

    private static void setTextSizeSdp(Context context, TextView view, float sizeSdp) {
        if (view == null || sizeSdp <= 0f) {
            return;
        }
        TextViewCompat.setAutoSizeTextTypeWithDefaults(view, TextViewCompat.AUTO_SIZE_TEXT_TYPE_NONE);
        float px = sdpToPx(context, sizeSdp);
        view.setTextSize(TypedValue.COMPLEX_UNIT_PX, px);
        Log.d(TAG, "setTextSizeSdp sdp=" + sizeSdp + " px=" + px + " on " + viewLabel(view));
    }

    private static float sdpToPx(Context context, float sizeSdp) {
        int intSize = Math.round(sizeSdp);
        String resName = "_" + intSize + "sdp";
        int resId = context.getResources().getIdentifier(resName, "dimen", context.getPackageName());
        if (resId != 0) {
            return context.getResources().getDimension(resId);
        }
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        float swDp = Math.min(dm.widthPixels, dm.heightPixels) / dm.density;
        float px = sizeSdp * (swDp / 360f) * dm.density;
        Log.w(TAG, "sdp dimen missing for " + resName + ", approx px=" + px);
        return px;
    }

    private static Map<String, LanguageFontConfig> loadAll(Context context) {
        String json = SharedPrefsData.getString(context, Constants.LANGUAGE_FONT_SETTINGS, Constants.PREF_NAME);
        if (TextUtils.isEmpty(json)) {
            Log.d(TAG, "loadAll: no saved language_font_settings in prefs");
            return new HashMap<>();
        }
        try {
            Map<String, LanguageFontConfig> parsed = GSON.fromJson(json, MAP_TYPE);
            int count = parsed != null ? parsed.size() : 0;
            Log.d(TAG, "loadAll: parsed " + count + " languages from prefs, jsonLen=" + json.length());
            return parsed != null ? parsed : new HashMap<>();
        } catch (Exception e) {
            Log.e(TAG, "loadAll: parse failed, using empty map", e);
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

    private static String describeConfig(LanguageFontConfig cfg) {
        if (cfg == null) {
            return "null";
        }
        return "{title=" + cfg.getTitleFontSize()
                + ",body=" + cfg.getBodyFontSize()
                + ",strip=" + cfg.getStripFontSize()
                + ",prog=" + (cfg.hasProgrammaticBodySize() ? cfg.getProgrammaticBodySize() : "null")
                + ",titleFont=" + cfg.getTitleFontFamily()
                + ",bodyFont=" + cfg.getBodyFontFamily()
                + "}";
    }

    private static String viewLabel(TextView view) {
        if (view == null) {
            return "null";
        }
        if (view.getId() != View.NO_ID) {
            try {
                return view.getResources().getResourceEntryName(view.getId());
            } catch (Exception ignored) {
                return "id@" + view.getId();
            }
        }
        return "TextView@" + Integer.toHexString(System.identityHashCode(view));
    }

    private static LanguageFontConfig productionDefault(int languageId) {
        return LanguageFontConfig.productionFor(languageId);
    }
}
