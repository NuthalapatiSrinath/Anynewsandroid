package com.anyNews.anynews.Utils;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.anyNews.anynews.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Birthday Special poster fonts — ids match admin {@code birthdayComposite.js}.
 * Dropdown rows render the sample word in each typeface (not plain family names).
 *
 * <p>APK size: decorative TTFs are <strong>not</strong> bundled (~5.5MB removed).
 * Live preview uses system faces; final poster uses server-side composite fonts.
 * Indic (Telugu etc.) uses the existing bundled Telugu UI font so place text
 * never paints tofu boxes.
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
        /** Formerly assets/birthday/fonts — kept null to avoid fat APK. */
        public final String assetFile;

        Spec(String id, String label, @Nullable String assetFile) {
            this.id = id;
            this.label = label;
            this.assetFile = assetFile;
        }
    }

    /** Curated catalog of visually distinct font styles (no duplicate fallbacks). */
    private static final Spec[] CATALOG =
            new Spec[] {
                new Spec("great-vibes", "Cursive Script", null),
                new Spec("caveat", "Casual Handwriting", null),
                new Spec("georgia", "Classic Serif", null),
                new Spec("roboto", "Clean Sans-Serif", null),
                new Spec("bebas", "Bold Condensed", null),
                new Spec("monospace", "Monospace", null),
            };

    private static final Map<String, Typeface> CACHE = new HashMap<>();

    private BirthdayFonts() {}

    @NonNull
    public static Spec[] catalog() {
        return CATALOG;
    }

    public static int indexOf(@Nullable String fontId) {
        String id = fontId == null ? DEFAULT_NAME_FONT_ID : fontId;
        for (int i = 0; i < CATALOG.length; i++) {
            if (CATALOG[i].id.equals(id)) return i;
        }
        if ("pacifico".equals(id) || "dancing-script".equals(id) || "satisfy".equals(id)
                || "lobster".equals(id) || "cookie".equals(id) || "sacramento".equals(id)
                || "allura".equals(id) || "alex-brush".equals(id) || "courgette".equals(id)
                || "kaushan".equals(id)) {
            return 0; // Cursive Script
        }
        if ("indie-flower".equals(id) || "shadows".equals(id) || "amatic".equals(id)) {
            return 1; // Casual Handwriting
        }
        if ("merriweather".equals(id) || "playfair".equals(id) || "lora".equals(id)
                || "cinzel".equals(id)) {
            return 2; // Classic Serif
        }
        if ("open-sans".equals(id) || "montserrat".equals(id) || "poppins".equals(id)
                || "raleway".equals(id) || "nunito".equals(id) || "quicksand".equals(id)
                || "comfortaa".equals(id) || "titillium".equals(id) || "source-sans".equals(id)) {
            return 3; // Clean Sans-Serif
        }
        if ("permanent-marker".equals(id) || "anton".equals(id) || "bangers".equals(id)
                || "russo".equals(id) || "oswald".equals(id) || "impact".equals(id)) {
            return 4; // Bold Condensed
        }
        return 0;
    }

    @NonNull
    public static String idAt(int index) {
        if (index < 0 || index >= CATALOG.length) return DEFAULT_NAME_FONT_ID;
        return CATALOG[index].id;
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
     * Use the app's bundled Telugu UI font (already in res/font) so Canvas paints
     * real glyphs — {@link Typeface#SANS_SERIF} alone often still tofus on OEM builds
     * when a Latin face was previously selected.
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
        // Preserve fallback chain (create by family name > SANS_SERIF constant).
        Typeface named = Typeface.create("sans-serif", Typeface.NORMAL);
        return named != null ? named : Typeface.DEFAULT;
    }

    /** True when text contains Indic blocks (Telugu, Devanagari, Kannada, …). */
    public static boolean needsIndicFallback(@Nullable String text) {
        if (text == null || text.isEmpty()) return false;
        for (int i = 0; i < text.length(); ) {
            int cp = text.codePointAt(i);
            if ((cp >= 0x0900 && cp <= 0x097F) // Devanagari
                    || (cp >= 0x0980 && cp <= 0x09FF) // Bengali
                    || (cp >= 0x0A80 && cp <= 0x0AFF) // Gujarati
                    || (cp >= 0x0B00 && cp <= 0x0B7F) // Oriya
                    || (cp >= 0x0B80 && cp <= 0x0BFF) // Tamil
                    || (cp >= 0x0C00 && cp <= 0x0C7F) // Telugu
                    || (cp >= 0x0C80 && cp <= 0x0CFF) // Kannada
                    || (cp >= 0x0D00 && cp <= 0x0D7F)) { // Malayalam
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
        Typeface tf = Typeface.SERIF;
        Spec spec = null;
        for (Spec s : CATALOG) {
            if (s.id.equals(id)) {
                spec = s;
                break;
            }
        }
        if (spec != null && spec.assetFile != null && context != null) {
            try {
                tf = Typeface.createFromAsset(
                        context.getAssets(), "birthday/fonts/" + spec.assetFile);
            } catch (Exception ignored) {
                tf = fallbackSystem(id);
            }
        } else {
            tf = fallbackSystem(id);
        }
        synchronized (CACHE) {
            CACHE.put(id, tf);
        }
        return tf;
    }

    @NonNull
    private static Typeface fallbackSystem(String id) {
        if ("great-vibes".equals(id) || "pacifico".equals(id) || "dancing-script".equals(id)
                || "satisfy".equals(id) || "lobster".equals(id) || "cookie".equals(id)
                || "sacramento".equals(id) || "allura".equals(id) || "alex-brush".equals(id)
                || "courgette".equals(id) || "kaushan".equals(id)) {
            Typeface t = Typeface.create("cursive", Typeface.NORMAL);
            return t != null ? t : Typeface.SERIF;
        }
        if ("indie-flower".equals(id) || "shadows".equals(id) || "caveat".equals(id)
                || "amatic".equals(id)) {
            Typeface t = Typeface.create("casual", Typeface.NORMAL);
            return t != null ? t : Typeface.SANS_SERIF;
        }
        if ("permanent-marker".equals(id) || "bebas".equals(id) || "anton".equals(id)
                || "bangers".equals(id) || "russo".equals(id) || "oswald".equals(id)) {
            Typeface t = Typeface.create("sans-serif-condensed", Typeface.BOLD);
            return t != null ? t : Typeface.SANS_SERIF;
        }
        if ("impact".equals(id)) {
            Typeface t = Typeface.create("sans-serif-black", Typeface.BOLD);
            return t != null ? t : Typeface.DEFAULT_BOLD;
        }
        if ("georgia".equals(id) || "merriweather".equals(id) || "playfair".equals(id)
                || "lora".equals(id) || "cinzel".equals(id)) {
            return Typeface.SERIF;
        }
        if ("quicksand".equals(id) || "comfortaa".equals(id) || "josefin".equals(id)
                || "nunito".equals(id) || "raleway".equals(id)) {
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
        for (Spec s : CATALOG) list.add(s);
        return new ArrayAdapter<Spec>(context, android.R.layout.simple_spinner_dropdown_item, list) {
            @NonNull
            @Override
            public android.view.View getView(
                    int position, @Nullable android.view.View convertView, @NonNull android.view.ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                bindSample(context, tv, getItem(position));
                return tv;
            }

            @Override
            public android.view.View getDropDownView(
                    int position, @Nullable android.view.View convertView, @NonNull android.view.ViewGroup parent) {
                TextView tv = (TextView) super.getDropDownView(position, convertView, parent);
                bindSample(context, tv, getItem(position));
                return tv;
            }

            private void bindSample(Context ctx, TextView tv, @Nullable Spec spec) {
                if (tv == null) return;
                String id = spec != null ? spec.id : DEFAULT_NAME_FONT_ID;
                String sample = provider != null && spec != null ? provider.getSampleText(spec) : SAMPLE_WORD;
                if (android.text.TextUtils.isEmpty(sample)) sample = SAMPLE_WORD;
                tv.setText(sample);
                tv.setTypeface(typefaceForText(ctx, id, false, sample));
                tv.setTextSize(18f);
                tv.setPadding(24, 18, 24, 18);
                tv.setContentDescription(spec != null ? spec.label : sample);
            }
        };
    }
}
