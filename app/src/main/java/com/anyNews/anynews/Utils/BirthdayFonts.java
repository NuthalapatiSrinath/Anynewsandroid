package com.anyNews.anynews.Utils;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Birthday Special poster fonts — ids match admin {@code birthdayComposite.js}.
 * Dropdown rows render the sample word in each typeface (not plain family names).
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
        public final String assetFile; // under assets/birthday/fonts/, nullable

        Spec(String id, String label, @Nullable String assetFile) {
            this.id = id;
            this.label = label;
            this.assetFile = assetFile;
        }
    }

    /** Default (Great Vibes) first, then rest matching admin catalog. */
    private static final Spec[] CATALOG =
            new Spec[] {
                new Spec("great-vibes", "Great Vibes", "great-vibes.ttf"),
                new Spec("pacifico", "Pacifico", "pacifico.ttf"),
                new Spec("dancing-script", "Dancing Script", "dancing-script.ttf"),
                new Spec("satisfy", "Satisfy", "satisfy.ttf"),
                new Spec("lobster", "Lobster", "lobster.ttf"),
                new Spec("cookie", "Cookie", "cookie.ttf"),
                new Spec("sacramento", "Sacramento", "sacramento.ttf"),
                new Spec("allura", "Allura", "allura.ttf"),
                new Spec("alex-brush", "Alex Brush", "alex-brush.ttf"),
                new Spec("courgette", "Courgette", "courgette.ttf"),
                new Spec("kaushan", "Kaushan Script", "kaushan.ttf"),
                new Spec("indie-flower", "Indie Flower", "indie-flower.ttf"),
                new Spec("shadows", "Shadows Into Light", "shadows.ttf"),
                new Spec("caveat", "Caveat", "caveat.ttf"),
                new Spec("amatic", "Amatic SC", "amatic.ttf"),
                new Spec("permanent-marker", "Permanent Marker", "permanent-marker.ttf"),
                new Spec("playfair", "Playfair Display", "playfair.ttf"),
                // Merriweather TTF was ~4.5MB (variable/full CJK dump) — use system serif.
                new Spec("merriweather", "Merriweather", null),
                new Spec("lora", "Lora", "lora.ttf"),
                new Spec("cinzel", "Cinzel", "cinzel.ttf"),
                new Spec("roboto", "Roboto", "roboto.ttf"),
                new Spec("open-sans", "Open Sans", "open-sans.ttf"),
                new Spec("montserrat", "Montserrat", "montserrat.ttf"),
                new Spec("poppins", "Poppins", "poppins.ttf"),
                new Spec("raleway", "Raleway", "raleway.ttf"),
                new Spec("oswald", "Oswald", "oswald.ttf"),
                new Spec("josefin", "Josefin Sans", "josefin.ttf"),
                new Spec("nunito", "Nunito", "nunito.ttf"),
                new Spec("quicksand", "Quicksand", "quicksand.ttf"),
                new Spec("comfortaa", "Comfortaa", "comfortaa.ttf"),
                new Spec("titillium", "Titillium Web", "titillium.ttf"),
                new Spec("source-sans", "Source Sans 3", "source-sans.ttf"),
                new Spec("bebas", "Bebas Neue", "bebas.ttf"),
                new Spec("anton", "Anton", "anton.ttf"),
                new Spec("bangers", "Bangers", "bangers.ttf"),
                new Spec("russo", "Russo One", "russo.ttf"),
                new Spec("georgia", "Georgia", null),
                new Spec("impact", "Impact", null),
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
     * Prefer a system face with proper fallback when the label needs Indic script.
     */
    @NonNull
    public static Typeface typefaceForText(
            @Nullable Context context,
            @Nullable String fontId,
            boolean bold,
            @Nullable String text) {
        if (needsIndicFallback(text)) {
            Typeface base = Typeface.SANS_SERIF;
            if (bold) {
                return Typeface.create(base, Typeface.BOLD);
            }
            return base != null ? base : Typeface.DEFAULT;
        }
        return typeface(context, fontId, bold);
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
        if ("georgia".equals(id) || "merriweather".equals(id) || "playfair".equals(id)
                || "lora".equals(id) || "cinzel".equals(id)) {
            return Typeface.SERIF;
        }
        if ("impact".equals(id)) {
            Typeface t = Typeface.create("sans-serif-black", Typeface.BOLD);
            return t != null ? t : Typeface.DEFAULT_BOLD;
        }
        if ("roboto".equals(id) || "open-sans".equals(id) || "montserrat".equals(id)
                || "poppins".equals(id) || "raleway".equals(id) || "nunito".equals(id)
                || "josefin".equals(id) || "source-sans".equals(id) || "titillium".equals(id)
                || "quicksand".equals(id) || "comfortaa".equals(id) || "oswald".equals(id)
                || "bebas".equals(id) || "anton".equals(id) || "bangers".equals(id)
                || "russo".equals(id) || "permanent-marker".equals(id)) {
            return Typeface.SANS_SERIF;
        }
        return Typeface.SERIF;
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

    @NonNull
    public static ArrayAdapter<Spec> fontSampleAdapter(@NonNull Context context) {
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
                tv.setText(SAMPLE_WORD);
                tv.setTypeface(typeface(ctx, id, false));
                tv.setTextSize(20f);
                tv.setPadding(24, 18, 24, 18);
                tv.setContentDescription(spec != null ? spec.label : SAMPLE_WORD);
            }
        };
    }
}
