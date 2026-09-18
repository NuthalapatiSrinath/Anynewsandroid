package com.anyNews.anynews.Utils;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.CommonUtilities;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Birthday Special poster fonts — ids match admin {@code birthdayComposite.js}
 * and server {@code helpers/birthday/fontsCatalog.js}.
 *
 * <p>Decorative TTFs are downloaded from the birthday config API into app cache
 * (not bundled — keeps APK lean). Live preview + dropdown use the same faces as
 * the server poster when files are available; Indic text always uses the bundled
 * Telugu UI font.
 */
public final class BirthdayFonts {

    public static final String SAMPLE_WORD = "Birthday";
    public static final String DEFAULT_NAME_FONT_ID = "great-vibes";
    public static final String DEFAULT_ADDRESS_FONT_ID = "great-vibes";
    public static final String DEFAULT_SPECIAL_FONT_ID = "merriweather";

    public static final int NAME_SIZE_SMALL = 36;
    public static final int NAME_SIZE_MEDIUM = 52;
    public static final int NAME_SIZE_LARGE = 68;
    public static final int ADDR_SIZE_SMALL = 28;
    public static final int ADDR_SIZE_MEDIUM = 40;
    public static final int ADDR_SIZE_LARGE = 52;

    public static final class Spec {
        public final String id;
        public final String label;
        /** Server file name when known (e.g. GreatVibes-Regular.ttf). */
        @Nullable public final String fileName;
        /** Absolute or relative download URL from birthday_config. */
        @Nullable public final String url;

        Spec(String id, String label, @Nullable String fileName, @Nullable String url) {
            this.id = id;
            this.label = label;
            this.fileName = fileName;
            this.url = url;
        }
    }

    /** Built-in defaults when API has not configured fonts yet. */
    private static final Spec[] DEFAULT_CATALOG =
            new Spec[] {
                new Spec("great-vibes", "Cursive Script", "GreatVibes-Regular.ttf", null),
                new Spec("pacifico", "Pacifico Script", "Pacifico-Regular.ttf", null),
                new Spec("dancing-script", "Dancing Script", "DancingScript-Regular.ttf", null),
                new Spec("caveat", "Casual Handwriting", "Caveat-Regular.ttf", null),
                new Spec("playfair", "Elegant Serif", "PlayfairDisplay-Regular.ttf", null),
                new Spec("georgia", "Classic Serif", null, null),
                new Spec("roboto", "Clean Sans-Serif", "Roboto-Regular.ttf", null),
                new Spec("bebas", "Bold Condensed", "BebasNeue-Regular.ttf", null),
            };

    private static Spec[] activeCatalog = DEFAULT_CATALOG;
    private static final Map<String, Typeface> CACHE = new HashMap<>();
    private static final ExecutorService DOWNLOAD_EXEC = Executors.newSingleThreadExecutor();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static volatile boolean downloadScheduled;

    private BirthdayFonts() {}

    @NonNull
    public static Spec[] catalog() {
        return activeCatalog;
    }

    /**
     * Apply admin-configured font list from {@code birthday_config} / {@code fonts} array.
     * Triggers background download of missing TTF files.
     */
    public static void applyServerFonts(@Nullable Context context, @Nullable JSONArray fonts) {
        if (fonts == null || fonts.length() == 0) {
            activeCatalog = DEFAULT_CATALOG;
            if (context != null) ensureFontsDownloaded(context, null);
            return;
        }
        List<Spec> list = new ArrayList<>();
        for (int i = 0; i < fonts.length(); i++) {
            try {
                JSONObject o = fonts.optJSONObject(i);
                if (o == null) continue;
                String id = o.optString("id", "").trim();
                if (id.isEmpty()) continue;
                String label = o.optString("label", id);
                String file = o.has("file") && !o.isNull("file") ? o.optString("file", null) : null;
                if (file != null && file.isEmpty()) file = null;
                String url = o.has("url") && !o.isNull("url") ? o.optString("url", null) : null;
                if (url != null && url.isEmpty()) url = null;
                list.add(new Spec(id, label, file, url));
            } catch (Exception ignored) {
            }
        }
        if (list.isEmpty()) {
            activeCatalog = DEFAULT_CATALOG;
        } else {
            activeCatalog = list.toArray(new Spec[0]);
        }
        synchronized (CACHE) {
            CACHE.clear();
        }
        if (context != null) ensureFontsDownloaded(context, null);
    }

    public interface FontsReadyCallback {
        void onFontsReady();
    }

    /** Download missing decorative TTFs into {@code context.getCacheDir()/birthday_fonts}. */
    public static void ensureFontsDownloaded(
            @Nullable Context context, @Nullable FontsReadyCallback callback) {
        if (context == null) {
            if (callback != null) MAIN.post(callback::onFontsReady);
            return;
        }
        final Context app = context.getApplicationContext();
        DOWNLOAD_EXEC.execute(
                () -> {
                    for (Spec s : activeCatalog) {
                        if (s.fileName == null && s.url == null) continue;
                        File dest = fontCacheFile(app, s);
                        if (dest != null && dest.exists() && dest.length() > 1000) continue;
                        String url = resolveDownloadUrl(s);
                        if (TextUtils.isEmpty(url)) continue;
                        downloadTo(url, dest);
                    }
                    synchronized (CACHE) {
                        CACHE.clear();
                    }
                    downloadScheduled = true;
                    if (callback != null) MAIN.post(callback::onFontsReady);
                });
    }

    @Nullable
    private static File fontCacheFile(Context context, Spec s) {
        String name = s.fileName;
        if (TextUtils.isEmpty(name) && !TextUtils.isEmpty(s.url)) {
            int slash = s.url.lastIndexOf('/');
            name = slash >= 0 ? s.url.substring(slash + 1) : s.id + ".ttf";
            int q = name.indexOf('?');
            if (q > 0) name = name.substring(0, q);
        }
        if (TextUtils.isEmpty(name)) name = s.id + ".ttf";
        File dir = new File(context.getCacheDir(), "birthday_fonts");
        //noinspection ResultOfMethodCallIgnored
        dir.mkdirs();
        return new File(dir, name);
    }

    @Nullable
    private static String resolveDownloadUrl(Spec s) {
        if (!TextUtils.isEmpty(s.url)) {
            if (s.url.startsWith("http://") || s.url.startsWith("https://")) return s.url;
            String base = CommonUtilities.URL;
            if (base == null) return null;
            if (s.url.startsWith("/")) {
                // strip trailing path after host
                try {
                    URL u = new URL(base);
                    return u.getProtocol() + "://" + u.getAuthority() + s.url;
                } catch (Exception e) {
                    return base.replaceAll("/+$", "") + s.url;
                }
            }
            return base.replaceAll("/+$", "") + "/" + s.url;
        }
        if (!TextUtils.isEmpty(s.fileName)) {
            String base = CommonUtilities.URL;
            if (base == null) return null;
            try {
                URL u = new URL(base);
                return u.getProtocol()
                        + "://"
                        + u.getAuthority()
                        + "/public/birthday/fonts/"
                        + s.fileName;
            } catch (Exception e) {
                return base.replaceAll("/+$", "") + "/public/birthday/fonts/" + s.fileName;
            }
        }
        return null;
    }

    private static void downloadTo(String urlStr, @Nullable File dest) {
        if (dest == null) return;
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(12000);
            conn.setReadTimeout(30000);
            conn.setInstanceFollowRedirects(true);
            int code = conn.getResponseCode();
            if (code != 200) return;
            File tmp = new File(dest.getAbsolutePath() + ".part");
            try (InputStream in = conn.getInputStream();
                    FileOutputStream out = new FileOutputStream(tmp)) {
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) >= 0) {
                    out.write(buf, 0, n);
                }
            }
            if (tmp.length() > 1000) {
                //noinspection ResultOfMethodCallIgnored
                dest.delete();
                //noinspection ResultOfMethodCallIgnored
                tmp.renameTo(dest);
            } else {
                //noinspection ResultOfMethodCallIgnored
                tmp.delete();
            }
        } catch (Exception ignored) {
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    public static int indexOf(@Nullable String fontId) {
        String id = fontId == null ? DEFAULT_NAME_FONT_ID : fontId;
        Spec[] cat = activeCatalog;
        for (int i = 0; i < cat.length; i++) {
            if (cat[i].id.equals(id)) return i;
        }
        // Legacy aliases → closest face in active catalog
        String mapped = mapLegacyId(id);
        for (int i = 0; i < cat.length; i++) {
            if (cat[i].id.equals(mapped)) return i;
        }
        return 0;
    }

    @NonNull
    private static String mapLegacyId(String id) {
        if ("pacifico".equals(id)
                || "dancing-script".equals(id)
                || "satisfy".equals(id)
                || "lobster".equals(id)
                || "cookie".equals(id)
                || "sacramento".equals(id)
                || "allura".equals(id)
                || "alex-brush".equals(id)
                || "courgette".equals(id)
                || "kaushan".equals(id)
                || "great-vibes".equals(id)) {
            return "great-vibes";
        }
        if ("indie-flower".equals(id) || "shadows".equals(id) || "amatic".equals(id) || "caveat".equals(id)) {
            return "caveat";
        }
        if ("merriweather".equals(id)
                || "playfair".equals(id)
                || "lora".equals(id)
                || "cinzel".equals(id)
                || "georgia".equals(id)) {
            return "georgia";
        }
        if ("open-sans".equals(id)
                || "montserrat".equals(id)
                || "poppins".equals(id)
                || "raleway".equals(id)
                || "nunito".equals(id)
                || "quicksand".equals(id)
                || "comfortaa".equals(id)
                || "titillium".equals(id)
                || "source-sans".equals(id)
                || "roboto".equals(id)) {
            return "roboto";
        }
        if ("permanent-marker".equals(id)
                || "anton".equals(id)
                || "bangers".equals(id)
                || "russo".equals(id)
                || "oswald".equals(id)
                || "impact".equals(id)
                || "bebas".equals(id)) {
            return "bebas";
        }
        return DEFAULT_NAME_FONT_ID;
    }

    @NonNull
    public static String idAt(int index) {
        Spec[] cat = activeCatalog;
        if (index < 0 || index >= cat.length) return DEFAULT_NAME_FONT_ID;
        return cat[index].id;
    }

    @NonNull
    public static Typeface typeface(@Nullable Context context, @Nullable String fontId, boolean bold) {
        Typeface base = loadBase(context, fontId);
        if (bold) {
            return Typeface.create(base, Typeface.BOLD);
        }
        return base;
    }

    /**
     * Script fonts (Great Vibes, etc.) lack Telugu/Indic glyphs and draw tofu boxes.
     * Use the app's bundled Telugu UI font so Canvas paints real glyphs.
     */
    @NonNull
    public static Typeface typefaceForText(
            @Nullable Context context,
            @Nullable String fontId,
            boolean bold,
            @Nullable String text) {
        if (needsIndicFallback(text)) {
            Typeface base = indicTypeface(context);
            if (bold) {
                Typeface b = Typeface.create(base, Typeface.BOLD);
                return b != null ? b : base;
            }
            return base;
        }
        return typeface(context, fontId, bold);
    }

    @NonNull
    private static Typeface indicTypeface(@Nullable Context context) {
        if (context != null) {
            try {
                Typeface tf = ResourcesCompat.getFont(context, R.font.robotolighttelugu);
                if (tf != null) return tf;
            } catch (Exception ignored) {
            }
            try {
                Typeface tf = ResourcesCompat.getFont(context, R.font.sreekrushnadevaraya_regular);
                if (tf != null) return tf;
            } catch (Exception ignored) {
            }
        }
        Typeface named = Typeface.create("sans-serif", Typeface.NORMAL);
        return named != null ? named : Typeface.DEFAULT;
    }

    /** True when text contains Indic blocks (Telugu, Devanagari, Kannada, …). */
    public static boolean needsIndicFallback(@Nullable String text) {
        if (text == null || text.isEmpty()) return false;
        for (int i = 0; i < text.length(); ) {
            int cp = text.codePointAt(i);
            if ((cp >= 0x0900 && cp <= 0x097F)
                    || (cp >= 0x0980 && cp <= 0x09FF)
                    || (cp >= 0x0A80 && cp <= 0x0AFF)
                    || (cp >= 0x0B00 && cp <= 0x0B7F)
                    || (cp >= 0x0B80 && cp <= 0x0BFF)
                    || (cp >= 0x0C00 && cp <= 0x0C7F)
                    || (cp >= 0x0C80 && cp <= 0x0CFF)
                    || (cp >= 0x0D00 && cp <= 0x0D7F)) {
                return true;
            }
            i += Character.charCount(cp);
        }
        return false;
    }

    @NonNull
    private static Typeface loadBase(@Nullable Context context, @Nullable String fontId) {
        String id = fontId == null || fontId.isEmpty() ? DEFAULT_NAME_FONT_ID : fontId;
        synchronized (CACHE) {
            Typeface cached = CACHE.get(id);
            if (cached != null) return cached;
        }
        Typeface tf = null;
        Spec spec = findSpec(id);
        if (spec != null && context != null) {
            File cached = fontCacheFile(context, spec);
            if (cached != null && cached.exists() && cached.length() > 1000) {
                try {
                    tf = Typeface.createFromFile(cached);
                } catch (Exception ignored) {
                }
            }
            if (tf == null && !downloadScheduled) {
                ensureFontsDownloaded(context, null);
            }
        }
        if (tf == null) {
            tf = fallbackSystem(id);
        }
        synchronized (CACHE) {
            CACHE.put(id, tf);
        }
        return tf;
    }

    @Nullable
    private static Spec findSpec(String id) {
        for (Spec s : activeCatalog) {
            if (s.id.equals(id)) return s;
        }
        for (Spec s : DEFAULT_CATALOG) {
            if (s.id.equals(id)) return s;
        }
        return null;
    }

    @NonNull
    private static Typeface fallbackSystem(String id) {
        if ("great-vibes".equals(id)
                || "pacifico".equals(id)
                || "dancing-script".equals(id)
                || "satisfy".equals(id)
                || "lobster".equals(id)
                || "cookie".equals(id)
                || "sacramento".equals(id)
                || "allura".equals(id)
                || "alex-brush".equals(id)
                || "courgette".equals(id)
                || "kaushan".equals(id)) {
            Typeface t = Typeface.create("cursive", Typeface.NORMAL);
            return t != null ? t : Typeface.SERIF;
        }
        if ("indie-flower".equals(id)
                || "shadows".equals(id)
                || "caveat".equals(id)
                || "amatic".equals(id)) {
            Typeface t = Typeface.create("casual", Typeface.NORMAL);
            return t != null ? t : Typeface.SANS_SERIF;
        }
        if ("permanent-marker".equals(id)
                || "bebas".equals(id)
                || "anton".equals(id)
                || "bangers".equals(id)
                || "russo".equals(id)
                || "oswald".equals(id)) {
            Typeface t = Typeface.create("sans-serif-condensed", Typeface.BOLD);
            return t != null ? t : Typeface.SANS_SERIF;
        }
        if ("impact".equals(id)) {
            Typeface t = Typeface.create("sans-serif-black", Typeface.BOLD);
            return t != null ? t : Typeface.DEFAULT_BOLD;
        }
        if ("georgia".equals(id)
                || "merriweather".equals(id)
                || "playfair".equals(id)
                || "lora".equals(id)
                || "cinzel".equals(id)) {
            return Typeface.SERIF;
        }
        if ("monospace".equals(id)) {
            return Typeface.MONOSPACE;
        }
        if ("quicksand".equals(id)
                || "comfortaa".equals(id)
                || "josefin".equals(id)
                || "nunito".equals(id)
                || "raleway".equals(id)) {
            Typeface t = Typeface.create("sans-serif-light", Typeface.NORMAL);
            return t != null ? t : Typeface.SANS_SERIF;
        }
        return Typeface.SANS_SERIF;
    }

    public static int nameSizePx(int tierIndex) {
        if (tierIndex <= 0) return NAME_SIZE_SMALL;
        if (tierIndex == 1) return NAME_SIZE_MEDIUM;
        return NAME_SIZE_LARGE;
    }

    public static int addressSizePx(int tierIndex) {
        if (tierIndex <= 0) return ADDR_SIZE_SMALL;
        if (tierIndex == 1) return ADDR_SIZE_MEDIUM;
        return ADDR_SIZE_LARGE;
    }

    /** Medium is the default (matches admin defaults 52 / 40). */
    public static int defaultSizeTierIndex() {
        return 1;
    }

    /** Map a pixel size back to Small/Medium/Large spinner index. */
    public static int tierIndexForSize(boolean forName, int sizePx) {
        if (forName) {
            if (sizePx <= (NAME_SIZE_SMALL + NAME_SIZE_MEDIUM) / 2) return 0;
            if (sizePx >= (NAME_SIZE_MEDIUM + NAME_SIZE_LARGE) / 2) return 2;
            return 1;
        }
        if (sizePx <= (ADDR_SIZE_SMALL + ADDR_SIZE_MEDIUM) / 2) return 0;
        if (sizePx >= (ADDR_SIZE_MEDIUM + ADDR_SIZE_LARGE) / 2) return 2;
        return 1;
    }

    @NonNull
    public static ArrayAdapter<CharSequence> sizeAdapter(@NonNull Context context) {
        return new ArrayAdapter<>(
                context,
                android.R.layout.simple_spinner_dropdown_item,
                new String[] {"Small", "Medium", "Large"});
    }

    public interface SampleTextProvider {
        @NonNull
        String getSampleText(@NonNull Spec spec);
    }

    @NonNull
    public static ArrayAdapter<Spec> fontSampleAdapter(@NonNull Context context) {
        return fontSampleAdapter(context, null);
    }

    @NonNull
    public static ArrayAdapter<Spec> fontSampleAdapter(
            @NonNull Context context, @Nullable SampleTextProvider provider) {
        List<Spec> list = new ArrayList<>();
        for (Spec s : activeCatalog) list.add(s);
        return new ArrayAdapter<Spec>(context, android.R.layout.simple_spinner_dropdown_item, list) {
            @NonNull
            @Override
            public android.view.View getView(
                    int position,
                    @Nullable android.view.View convertView,
                    @NonNull android.view.ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                bindSample(context, tv, getItem(position));
                return tv;
            }

            @Override
            public android.view.View getDropDownView(
                    int position,
                    @Nullable android.view.View convertView,
                    @NonNull android.view.ViewGroup parent) {
                TextView tv = (TextView) super.getDropDownView(position, convertView, parent);
                bindSample(context, tv, getItem(position));
                return tv;
            }

            private void bindSample(Context ctx, TextView tv, @Nullable Spec spec) {
                if (tv == null) return;
                String id = spec != null ? spec.id : DEFAULT_NAME_FONT_ID;
                String sample =
                        provider != null && spec != null ? provider.getSampleText(spec) : SAMPLE_WORD;
                if (TextUtils.isEmpty(sample)) sample = SAMPLE_WORD;
                tv.setText(sample);
                tv.setTypeface(typefaceForText(ctx, id, false, sample));
                tv.setTextSize(18f);
                tv.setPadding(24, 18, 24, 18);
                tv.setContentDescription(spec != null ? spec.label : sample);
            }
        };
    }
}
