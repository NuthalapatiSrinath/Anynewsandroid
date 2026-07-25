package com.anyNews.anynews.Utils;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public final class FontSettingsHelper {

    public static final String TAG = "AnyNewsFont";
    /** CSS Regular — matches historical applyTypeface (Typeface.NORMAL) behavior. */
    public static final int DEFAULT_FONT_WEIGHT = 400;
    private static final Gson GSON = new Gson();
    private static final Type MAP_TYPE = new TypeToken<Map<String, LanguageFontConfig>>() {}.getType();
    private static final Type URL_MAP_TYPE = new TypeToken<Map<String, String>>() {}.getType();
    private static final OkHttpClient FONT_HTTP = new OkHttpClient.Builder().build();
    private static final ConcurrentHashMap<String, Typeface> CUSTOM_TYPEFACE_CACHE = new ConcurrentHashMap<>();

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

    /** Persist admin-uploaded font key → URL map from splash. */
    public static void saveCustomFontUrls(Context context, Map<String, String> urls) {
        if (context == null) {
            return;
        }
        if (urls == null || urls.isEmpty()) {
            SharedPrefsData.putString(context, Constants.CUSTOM_FONT_URLS, "{}", Constants.PREF_NAME);
            CUSTOM_TYPEFACE_CACHE.clear();
            Log.d(TAG, "saveCustomFontUrls cleared");
            return;
        }
        SharedPrefsData.putString(
                context,
                Constants.CUSTOM_FONT_URLS,
                GSON.toJson(urls),
                Constants.PREF_NAME
        );
        CUSTOM_TYPEFACE_CACHE.clear();
        Log.d(TAG, "saveCustomFontUrls saved " + urls.size() + " fonts");
    }

    public static Map<String, String> loadCustomFontUrls(Context context) {
        if (context == null) {
            return new HashMap<>();
        }
        String json = SharedPrefsData.getString(context, Constants.CUSTOM_FONT_URLS, Constants.PREF_NAME);
        if (TextUtils.isEmpty(json)) {
            return new HashMap<>();
        }
        try {
            Map<String, String> parsed = GSON.fromJson(json, URL_MAP_TYPE);
            return parsed != null ? parsed : new HashMap<>();
        } catch (Exception e) {
            Log.e(TAG, "loadCustomFontUrls parse failed", e);
            return new HashMap<>();
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
        applyTypeface(context, headerView, cfg.getTitleFontFamily(), cfg.getTitleFontWeight());
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
            applyTypeface(context, label, cfg.getBodyFontFamily(), cfg.getBodyFontWeight());
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
        int titleWeight = cfg.getTitleFontWeight();
        int bodyWeight = cfg.getBodyFontWeight();

        Log.d(
                TAG,
                "applyArticleTypography source=" + source
                        + " lang=" + languageId
                        + " titleSize=" + titleSize
                        + " bodySize=" + bodySize
                        + " titleFont=" + titleFamily
                        + " bodyFont=" + bodyFamily
                        + " titleWeight=" + titleWeight
                        + " bodyWeight=" + bodyWeight
                        + " titleView=" + viewLabel(titleView)
                        + " bodyView=" + viewLabel(bodyView)
        );

        if (titleView != null) {
            setTextSizeSdp(context, titleView, titleSize);
            applyTypeface(context, titleView, titleFamily, titleWeight);
            Log.d(TAG, "applied title pxSize=" + titleView.getTextSize() + " on " + viewLabel(titleView));
        }
        if (bodyView != null) {
            setTextSizeSdp(context, bodyView, bodySize);
            applyTypeface(context, bodyView, bodyFamily, bodyWeight);
            applyLineSpacing(bodyView, cfg.getLineSpacingMultiplier());
            Log.d(TAG, "applied body pxSize=" + bodyView.getTextSize() + " on " + viewLabel(bodyView));
        }
    }

    /**
     * Applies the admin-configured per-language line-spacing multiplier to a body
     * TextView while preserving whatever lineSpacingExtra the layout XML defined
     * (so a multiplier of 1.0 keeps the exact current look). Lower values tighten
     * the lines (e.g. Malayalam), higher values open them up.
     */
    public static void applyLineSpacing(TextView view, float multiplier) {
        if (view == null || multiplier <= 0f) {
            return;
        }
        float extraPx = view.getLineSpacingExtra();
        view.setLineSpacing(extraPx, multiplier);
        Log.d(TAG, "applyLineSpacing mult=" + multiplier + " extraPx=" + extraPx + " on " + viewLabel(view));
    }

    public static void applyTypeface(Context context, TextView view, String fontFamilyKey) {
        applyTypeface(context, view, fontFamilyKey, DEFAULT_FONT_WEIGHT);
    }

    /**
     * Apply a font family and optional CSS-style weight (100..900).
     * Weight {@link #DEFAULT_FONT_WEIGHT} (400) keeps the historical look:
     * family applied with Typeface.NORMAL / no fake-bold overlay.
     */
    public static void applyTypeface(Context context, TextView view, String fontFamilyKey, int fontWeight) {
        if (context == null || view == null) {
            return;
        }
        if (TextUtils.isEmpty(fontFamilyKey)) {
            return;
        }
        int weight = clampFontWeight(fontWeight);
        // Clear any prior fake-bold so resetting to 400 restores the current look.
        view.getPaint().setFakeBoldText(false);

        if ("sans-serif-medium".equals(fontFamilyKey)) {
            Typeface base = Typeface.create("sans-serif-medium", Typeface.NORMAL);
            setTypefaceWithWeight(view, base, weight);
            Log.d(TAG, "typeface sans-serif-medium weight=" + weight + " on " + viewLabel(view));
            return;
        }
        Integer fontRes = resolveFontRes(fontFamilyKey);
        if (fontRes != null) {
            Typeface typeface = ResourcesCompat.getFont(context, fontRes);
            if (typeface != null) {
                setTypefaceWithWeight(view, typeface, weight);
                Log.d(TAG, "typeface " + fontFamilyKey + " res=" + fontRes
                        + " weight=" + weight + " on " + viewLabel(view));
            } else {
                Log.e(TAG, "typeface load failed key=" + fontFamilyKey + " res=" + fontRes);
            }
            return;
        }
        Typeface custom = resolveCustomTypeface(context, fontFamilyKey);
        if (custom != null) {
            setTypefaceWithWeight(view, custom, weight);
            Log.d(TAG, "typeface custom " + fontFamilyKey + " weight=" + weight + " on " + viewLabel(view));
        } else {
            Log.w(TAG, "typeface unknown key=" + fontFamilyKey + " on " + viewLabel(view));
        }
    }

    private static int clampFontWeight(int weight) {
        if (weight <= 0) return DEFAULT_FONT_WEIGHT;
        int w = weight;
        if (w < 100) w = 100;
        if (w > 900) w = 900;
        return Math.round(w / 100f) * 100;
    }

    /**
     * Weight 400 = exact historical path (setTypeface(base) only).
     * Other weights use API 28 Typeface.create(base, weight), fontVariationSettings
     * when helpful, BOLD/NORMAL on older APIs, and fake-bold as a last resort.
     */
    private static void setTypefaceWithWeight(TextView view, Typeface base, int weight) {
        if (view == null || base == null) {
            return;
        }
        if (weight == DEFAULT_FONT_WEIGHT) {
            view.setTypeface(base);
            clearFontVariationSettings(view);
            return;
        }

        Typeface weighted = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                weighted = Typeface.create(base, weight, false);
            } catch (Exception e) {
                Log.w(TAG, "Typeface.create(weight) failed weight=" + weight, e);
            }
        }

        if (weighted != null) {
            view.setTypeface(weighted);
        } else if (weight >= 600) {
            Typeface bold = Typeface.create(base, Typeface.BOLD);
            view.setTypeface(bold != null ? bold : base);
            // Single-weight fonts often ignore BOLD — synthesize when needed.
            if (bold == null || bold.equals(base) || styleOf(bold) == Typeface.NORMAL) {
                view.getPaint().setFakeBoldText(true);
            }
        } else {
            view.setTypeface(base);
        }

        applyFontVariationWeight(view, weight);

        // If API 28+ returned a face that is still too light for a bold request, fake-bold.
        if (weight >= 600 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Typeface current = view.getTypeface();
            if (current != null && current.getWeight() < 600) {
                view.getPaint().setFakeBoldText(true);
            }
        }
        view.invalidate();
    }

    private static int styleOf(Typeface tf) {
        return tf != null ? tf.getStyle() : Typeface.NORMAL;
    }

    /** Prefer Typeface.create(weight) on P+; use variation settings only below P. */
    private static void applyFontVariationWeight(TextView view, int weight) {
        if (view == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // Weight already applied via Typeface.create(base, weight, false).
            return;
        }
        if (weight == DEFAULT_FONT_WEIGHT) {
            clearFontVariationSettings(view);
            return;
        }
        try {
            view.setFontVariationSettings("'wght' " + weight);
        } catch (Exception e) {
            Log.d(TAG, "fontVariationSettings unsupported weight=" + weight);
        }
    }

    private static void clearFontVariationSettings(TextView view) {
        if (view == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        try {
            view.setFontVariationSettings(null);
        } catch (Exception ignored) {
        }
    }

    /**
     * Load an admin-uploaded font from disk cache, downloading from the splash URL if needed.
     * Safe to call on the UI thread for cached hits; download runs synchronously on first miss
     * (splash already warm-starts commonly used keys).
     */
    private static Typeface resolveCustomTypeface(Context context, String fontFamilyKey) {
        if (TextUtils.isEmpty(fontFamilyKey)) {
            return null;
        }
        Typeface cached = CUSTOM_TYPEFACE_CACHE.get(fontFamilyKey);
        if (cached != null) {
            return cached;
        }
        File fontFile = customFontFile(context, fontFamilyKey);
        if (fontFile != null && fontFile.exists() && fontFile.length() > 0) {
            try {
                Typeface tf = Typeface.createFromFile(fontFile);
                if (tf != null) {
                    CUSTOM_TYPEFACE_CACHE.put(fontFamilyKey, tf);
                    return tf;
                }
            } catch (Exception e) {
                Log.e(TAG, "createFromFile failed key=" + fontFamilyKey, e);
            }
        }
        Map<String, String> urls = loadCustomFontUrls(context);
        String url = urls.get(fontFamilyKey);
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        if (!downloadFontToFile(url, fontFile)) {
            return null;
        }
        try {
            Typeface tf = Typeface.createFromFile(fontFile);
            if (tf != null) {
                CUSTOM_TYPEFACE_CACHE.put(fontFamilyKey, tf);
            }
            return tf;
        } catch (Exception e) {
            Log.e(TAG, "createFromFile after download failed key=" + fontFamilyKey, e);
            return null;
        }
    }

    private static File customFontFile(Context context, String fontFamilyKey) {
        File dir = new File(context.getFilesDir(), "custom_fonts");
        if (!dir.exists() && !dir.mkdirs()) {
            Log.e(TAG, "custom_fonts dir create failed");
            return null;
        }
        String safe = fontFamilyKey.replaceAll("[^a-zA-Z0-9._-]", "_");
        return new File(dir, safe + ".font");
    }

    private static boolean downloadFontToFile(String url, File dest) {
        if (dest == null || TextUtils.isEmpty(url)) {
            return false;
        }
        Request request = new Request.Builder().url(url).get().build();
        try (Response response = FONT_HTTP.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                Log.e(TAG, "font download HTTP " + response.code() + " url=" + url);
                return false;
            }
            ResponseBody body = response.body();
            if (body == null) {
                return false;
            }
            File tmp = new File(dest.getAbsolutePath() + ".tmp");
            try (InputStream in = body.byteStream();
                 FileOutputStream out = new FileOutputStream(tmp)) {
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) >= 0) {
                    out.write(buf, 0, n);
                }
            }
            if (dest.exists() && !dest.delete()) {
                Log.w(TAG, "could not replace existing font file " + dest.getName());
            }
            if (!tmp.renameTo(dest)) {
                Log.e(TAG, "rename font tmp failed");
                //noinspection ResultOfMethodCallIgnored
                tmp.delete();
                return false;
            }
            Log.d(TAG, "font downloaded bytes=" + dest.length() + " -> " + dest.getName());
            return true;
        } catch (Exception e) {
            Log.e(TAG, "font download failed url=" + url, e);
            return false;
        }
    }

    /** Best-effort prefetch so the first feed paint does not block on network. */
    public static void prefetchCustomFonts(Context context) {
        if (context == null) {
            return;
        }
        final Context app = context.getApplicationContext();
        new Thread(() -> {
            Map<String, String> urls = loadCustomFontUrls(app);
            for (String key : urls.keySet()) {
                try {
                    resolveCustomTypeface(app, key);
                } catch (Exception e) {
                    Log.w(TAG, "prefetch failed key=" + key, e);
                }
            }
        }, "AnyNewsFontPrefetch").start();
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
            case "subhadra_regular":
                return R.font.subhadra_regular;
            case "balo_tammudu_reg":
                return R.font.balo_tammudu_reg;
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
                + ",titleWt=" + cfg.getTitleFontWeight()
                + ",bodyWt=" + cfg.getBodyFontWeight()
                + ",lineSp=" + cfg.getLineSpacingMultiplier()
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
