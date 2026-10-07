package com.anyNews.anynews.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Outline;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.widget.NestedScrollView;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.anyNews.anynews.Pojo.EncreptionRequest;
import com.anyNews.anynews.Pojo.EncritonRequestData;
import com.anyNews.anynews.Pojo.LanguageSelectionRequest;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.DailyUpdatesLabels;
import com.anyNews.anynews.Utils.DailyUpdatesShareLanguage;
import com.anyNews.anynews.Utils.DailyUpdatesShareLinks;
import com.anyNews.anynews.Utils.DistrictLabelHelper;
import com.anyNews.anynews.Utils.FuelLabels;
import com.anyNews.anynews.Utils.FuelPlaceNames;
import com.anyNews.anynews.Utils.LocalUpdatesConfigHelper;
import com.anyNews.anynews.Utils.SelectionListCache;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.anyNews.anynews.models.DistricsModel;
import com.anyNews.anynews.models.StatesModel;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.anyNews.anynews.Utils.MyUtils.decrypt;
import static com.anyNews.anynews.Utils.MyUtils.encrypt;

/**
 * Fuel prices detail — splash {@code root_settings.fuel}.
 * State / city pickers refresh hero + city table from stored {@code places}
 * (or legacy {@code by_state} when that array is absent). No third-party call.
 * Promo copy is derived from today-vs-yesterday deltas (not static admin strings).
 * Place names come from the location master for the selected language.
 */
public class FuelDetailActivity extends AppCompatActivity {

    private static final String KEY_AP = "ap";
    private static final String KEY_TG = "tg";

    @Nullable private static WeakReference<FuelDetailActivity> openRef;

    private static final Pattern CLOCK = Pattern.compile("(\\d{1,2}:\\d{2}\\s*[APap][Mm])");

    /** Working view of fuel JSON for the selected state (mutated on switch). */
    @Nullable private JSONObject activeFuel;
    @NonNull private String selectedStateKey = KEY_AP;
    @Nullable private String selectedCity;
    private int selectedDistrictId;
    @Nullable private String selectedCityEn;

    /** Location-master names for {@link Constants#LanguageID}, keyed by state id. */
    private final Map<Integer, String> stateNamesById = new HashMap<>();
    /** {@code stateId:districtId} → name in the selected language. */
    private final Map<String, String> districtNamesByKey = new HashMap<>();
    private final Set<Integer> districtFetchStarted = new HashSet<>();
    private boolean statesFetchStarted;
    /** True after the user taps a state in this screen. Otherwise prefs win. */
    private boolean statePickedInSession;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!LocalUpdatesConfigHelper.isFuelEnabled(this)) {
            Toast.makeText(this, R.string.fuel_disabled, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        DailyUpdatesShareLinks.beginLanguage(this);
        setContentView(R.layout.activity_fuel_detail);
        applySystemBars(R.id.fuel_detail_root);
        clipHero();
        JSONObject root = LocalUpdatesConfigHelper.fuelJson(this);
        if (root == null) root = new JSONObject();
        applyNameMaps();
        selectedStateKey = resolveInitialStateKey(root);
        selectedCity = resolveInitialCity(root, selectedStateKey);
        if (TextUtils.isEmpty(selectedCity)) {
            selectedCity = firstCityInState(root, selectedStateKey);
        }
        activeFuel = overlayState(root, selectedStateKey);
        bindUi(activeFuel);
        wireClicks();
        ensureLocationNames();
    }

    @Override
    protected void onStart() {
        super.onStart();
        openRef = new WeakReference<>(this);
        refreshFromRedis();
    }

    @Override
    protected void onStop() {
        FuelDetailActivity cur = openRef != null ? openRef.get() : null;
        if (cur == this) openRef = null;
        super.onStop();
    }

    /** Feed replaced the saved fuel pack. Keep the state tab the user picked. */
    public static void onFuelPackUpdated() {
        FuelDetailActivity activity = openRef != null ? openRef.get() : null;
        if (activity == null || activity.isFinishing()) return;
        activity.runOnUiThread(() -> {
            if (activity.isFinishing()) return;
            JSONObject root = LocalUpdatesConfigHelper.fuelJson(activity);
            if (root == null) return;
            activity.activeFuel = activity.overlayState(root, activity.selectedStateKey);
            activity.bindUi(activity.activeFuel);
        });
    }

    private void applySystemBars(int rootId) {
        Window window = getWindow();
        WindowCompat.setDecorFitsSystemWindows(window, false);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.BLACK);
        window.setNavigationBarColor(Color.BLACK);
        WindowInsetsControllerCompat c =
                WindowCompat.getInsetsController(window, window.getDecorView());
        if (c != null) {
            c.setAppearanceLightStatusBars(false);
            c.setAppearanceLightNavigationBars(false);
        }
        View root = findViewById(rootId);
        if (root == null) return;
        final int padL = root.getPaddingLeft();
        final int padT = root.getPaddingTop();
        final int padR = root.getPaddingRight();
        final int padB = root.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(padL + bars.left, padT + bars.top, padR + bars.right, padB + bars.bottom);
            return insets;
        });
        ViewCompat.requestApplyInsets(root);
    }

    private void clipHero() {
        View hero = findViewById(R.id.fuel_hero_image);
        if (hero == null || hero.getParent() == null) return;
        View parent = (View) hero.getParent();
        // The station photo must not use its own height. The price block sets
        // the card size; the city list sits under that card.
        if (hero instanceof android.widget.ImageView) {
            android.widget.ImageView iv = (android.widget.ImageView) hero;
            iv.setAdjustViewBounds(false);
            iv.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
        }
        ViewGroup.LayoutParams heroLp = hero.getLayoutParams();
        if (heroLp != null) {
            heroLp.height = 0;
            hero.setLayoutParams(heroLp);
        }
        parent.addOnLayoutChangeListener(
                (v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
                    int h = v.getHeight();
                    ViewGroup.LayoutParams lp = hero.getLayoutParams();
                    if (h > 0 && lp != null && lp.height != h) {
                        lp.height = h;
                        hero.setLayoutParams(lp);
                    }
                });
        final float radius = 16f * getResources().getDisplayMetrics().density;
        parent.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radius);
            }
        });
        parent.setClipToOutline(true);
    }

    private void bindUi(@Nullable JSONObject f) {
        if (f == null) f = new JSONObject();
        bindChrome();
        String stateLabel = stateLabelForKey(selectedStateKey, f);
        setText(R.id.fuel_location, stateLabel);
        View regionView = findViewById(R.id.fuel_region);
        if (regionView != null) regionView.setVisibility(View.GONE);

        setText(R.id.fuel_updated, localizedUpdated(f));

        double petrol = positiveOr(LocalUpdatesConfigHelper.optNumber(f, "petrol_price", 0), 0);
        double diesel = positiveOr(LocalUpdatesConfigHelper.optNumber(f, "diesel_price", 0), 0);
        if (petrol <= 0) petrol = positiveOr(resolvePrice(f, true), 0);
        if (diesel <= 0) diesel = positiveOr(resolvePrice(f, false), 0);

        double petrolCh = LocalUpdatesConfigHelper.optNumber(f, "petrol_change", 0);
        double dieselCh = LocalUpdatesConfigHelper.optNumber(f, "diesel_change", 0);
        if (!f.has("petrol_change")) petrolCh = resolveChange(f, true);
        if (!f.has("diesel_change")) dieselCh = resolveChange(f, false);

        // City override for hero when user picked a city
        if (!TextUtils.isEmpty(selectedCity)) {
            JSONObject cityRow = findCity(f, selectedCity);
            if (cityRow != null) {
                double cp = LocalUpdatesConfigHelper.optNumber(cityRow, "petrol", 0);
                double cd = LocalUpdatesConfigHelper.optNumber(cityRow, "diesel", 0);
                if (cp > 0) petrol = cp;
                if (cd > 0) diesel = cd;
                if (cityRow.has("change")) {
                    petrolCh = LocalUpdatesConfigHelper.optNumber(cityRow, "change", petrolCh);
                }
                if (cityRow.has("diesel_change")) {
                    dieselCh = LocalUpdatesConfigHelper.optNumber(cityRow, "diesel_change", dieselCh);
                } else if (cityRow.has("change")) {
                    dieselCh = LocalUpdatesConfigHelper.optNumber(cityRow, "change", dieselCh);
                }
            }
        }

        setText(R.id.fuel_petrol_price, petrol > 0
                ? ("₹" + LocalUpdatesConfigHelper.formatMoney2(petrol)) : "—");
        setText(R.id.fuel_diesel_price, diesel > 0
                ? ("₹" + LocalUpdatesConfigHelper.formatMoney2(diesel)) : "—");
        setText(R.id.fuel_petrol_change, changeLine(petrolCh) + FuelLabels.perLiter(this));
        setText(R.id.fuel_diesel_change, changeLine(dieselCh) + FuelLabels.perLiter(this));
        tintChange(R.id.fuel_petrol_change, petrolCh);
        tintChange(R.id.fuel_diesel_change, dieselCh);

        View bannerRow = findViewById(R.id.fuel_banner_row);
        if (bannerRow != null) bannerRow.setVisibility(View.GONE);
        View banner = findViewById(R.id.fuel_banner);
        if (banner != null) banner.setVisibility(View.GONE);
        setText(R.id.fuel_cities_title, FuelLabels.majorCities(this));
        updateStateFilterUi(stateLabel);
        bindFootnote(LocalUpdatesConfigHelper.optString(f, "cities_footnote", ""));

        String districtLabel = TextUtils.isEmpty(selectedCity)
                ? FuelLabels.pickCity(this)
                : (selectedCity + " ▾");
        setText(R.id.fuel_district_filter, districtLabel);

        bindPromoFromDeltas(petrolCh, dieselCh);
        setText(R.id.fuel_promo_cta, FuelLabels.seeOtherCities(this));

        View fuelSource = findViewById(R.id.fuel_source);
        if (fuelSource != null) fuelSource.setVisibility(View.GONE);

        String heroUrl = LocalUpdatesConfigHelper.optString(f, "hero_image_url", "");
        View heroIv = findViewById(R.id.fuel_hero_image);
        if (heroIv instanceof android.widget.ImageView) {
            if (heroUrl.startsWith("http://") || heroUrl.startsWith("https://")) {
                Glide.with(this).load(heroUrl).centerCrop()
                        .placeholder(R.drawable.fuel_hero_station)
                        .into((android.widget.ImageView) heroIv);
            } else {
                ((android.widget.ImageView) heroIv).setImageResource(R.drawable.fuel_hero_station);
            }
        }

        bindCities(LocalUpdatesConfigHelper.optArray(f, "cities"));
    }

    /** Chrome follows {@link Constants#LanguageID}. Payload copy is often Telugu. */
    private void bindChrome() {
        setText(R.id.fuel_top_title, FuelLabels.title(this));
        View sub = findViewById(R.id.fuel_top_subtitle);
        if (sub != null) sub.setVisibility(View.GONE);
        setText(R.id.fuel_updated_caption, FuelLabels.lastUpdated(this));
        setText(R.id.fuel_petrol_caption, DailyUpdatesLabels.petrol(this));
        setText(R.id.fuel_diesel_caption, DailyUpdatesLabels.diesel(this));
        setText(R.id.fuel_col_city, FuelLabels.cityColumn(this));
        setText(R.id.fuel_col_petrol, FuelLabels.petrolColumn(this));
        setText(R.id.fuel_col_diesel, FuelLabels.dieselColumn(this));
        setText(R.id.fuel_col_change, FuelLabels.changeColumn(this));
    }

    private void bindFootnote(@Nullable String note) {
        TextView tv = findViewById(R.id.fuel_cities_footnote);
        if (tv == null) return;
        if (!nameOk(note)) {
            tv.setText("");
            tv.setVisibility(View.GONE);
            return;
        }
        tv.setVisibility(View.VISIBLE);
        tv.setText(note);
    }

    @NonNull
    private String localizedUpdated(@NonNull JSONObject f) {
        String clock = clockFrom(f.optString("last_updated", ""));
        if (clock.isEmpty()) clock = clockFromFetchedAt(f.optString("fetched_at", ""));
        if (clock.isEmpty()) return "—";
        return FuelLabels.today(this) + " " + clock;
    }

    @NonNull
    private static String clockFrom(@Nullable String raw) {
        if (raw == null || raw.isEmpty()) return "";
        Matcher m = CLOCK.matcher(raw);
        if (!m.find()) return "";
        return m.group(1).toUpperCase(java.util.Locale.US);
    }

    @NonNull
    private static String clockFromFetchedAt(@Nullable String iso) {
        if (iso == null || iso.isEmpty()) return "";
        try {
            String trimmed = iso.trim();
            if (trimmed.endsWith("Z") || trimmed.endsWith("z")) {
                trimmed = trimmed.substring(0, trimmed.length() - 1);
            }
            int dot = trimmed.indexOf('.');
            if (dot > 0) trimmed = trimmed.substring(0, dot);
            java.text.SimpleDateFormat in = new java.text.SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US);
            in.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            java.util.Date date = in.parse(trimmed);
            if (date == null) return "";
            java.text.SimpleDateFormat out = new java.text.SimpleDateFormat(
                    "hh:mm a", java.util.Locale.US);
            out.setTimeZone(java.util.TimeZone.getTimeZone("Asia/Kolkata"));
            return out.format(date).toUpperCase(java.util.Locale.US);
        } catch (Exception e) {
            return "";
        }
    }

    private void bindPromoFromDeltas(double petrolCh, double dieselCh) {
        boolean pDown = petrolCh < -0.001;
        boolean pUp = petrolCh > 0.001;
        boolean dDown = dieselCh < -0.001;
        boolean dUp = dieselCh > 0.001;
        String pAmt = LocalUpdatesConfigHelper.formatMoney2(Math.abs(petrolCh));
        String dAmt = LocalUpdatesConfigHelper.formatMoney2(Math.abs(dieselCh));
        String title;
        String text;
        if (pDown && dDown) {
            title = FuelLabels.promoBothDownTitle(this);
            text = FuelLabels.promoBothDownText(this, pAmt, dAmt);
        } else if (pUp && dUp) {
            title = FuelLabels.promoBothUpTitle(this);
            text = FuelLabels.promoBothUpText(this, pAmt, dAmt);
        } else if (pDown && dUp) {
            title = FuelLabels.promoMixedTitle(this);
            text = FuelLabels.promoPetrolDownDieselUp(this, pAmt, dAmt);
        } else if (pUp && dDown) {
            title = FuelLabels.promoMixedTitle(this);
            text = FuelLabels.promoPetrolUpDieselDown(this, pAmt, dAmt);
        } else if (pDown) {
            title = FuelLabels.promoPetrolDownTitle(this);
            text = FuelLabels.promoPetrolDownText(this, pAmt);
        } else if (pUp) {
            title = FuelLabels.promoPetrolUpTitle(this);
            text = FuelLabels.promoPetrolUpText(this, pAmt);
        } else if (dDown) {
            title = FuelLabels.promoDieselDownTitle(this);
            text = FuelLabels.promoDieselDownText(this, dAmt);
        } else if (dUp) {
            title = FuelLabels.promoDieselUpTitle(this);
            text = FuelLabels.promoDieselUpText(this, dAmt);
        } else {
            title = FuelLabels.promoUnchangedTitle(this);
            text = FuelLabels.promoUnchangedText(this);
        }
        setText(R.id.fuel_promo_title, title);
        setText(R.id.fuel_promo_text, text);
    }

    private void bindCities(JSONArray cities) {
        LinearLayout list = findViewById(R.id.fuel_cities_list);
        if (list == null) return;
        list.removeAllViews();
        float d = getResources().getDisplayMetrics().density;
        for (int i = 0; i < cities.length(); i++) {
            JSONObject row = cities.optJSONObject(i);
            if (row == null) continue;
            double p = LocalUpdatesConfigHelper.optNumber(row, "petrol", 0);
            double di = LocalUpdatesConfigHelper.optNumber(row, "diesel", 0);
            if (!(p > 0) || !(di > 0)) continue;
            final String cityName = LocalUpdatesConfigHelper.optString(row, "city", "");
            final String cityEn = LocalUpdatesConfigHelper.optString(row, "city_en", "");
            final int distId = row.optInt("district_id", 0);
            LinearLayout line = new LinearLayout(this);
            line.setOrientation(LinearLayout.HORIZONTAL);
            line.setPadding(0, (int) (6 * d), 0, (int) (6 * d));
            line.setGravity(Gravity.CENTER_VERTICAL);
            boolean selected = cityRowSelected(cityName, cityEn, distId);
            if (selected) {
                line.setBackgroundColor(0x22FF6A1A);
            }
            TextView city = makeTv(cityName, 0xFFFFFFFF, 11, true);
            LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 1.2f);
            city.setLayoutParams(clp);
            line.addView(city);
            TextView petrolTv = makeTv(LocalUpdatesConfigHelper.formatMoney2(p), 0xFF34D399, 11, false);
            TextView dieselTv = makeTv(LocalUpdatesConfigHelper.formatMoney2(di), 0xFF60A5FA, 11, false);
            double ch = LocalUpdatesConfigHelper.optNumber(row, "change", 0);
            TextView changeTv = makeTv(changeLine(ch), ch < 0 ? 0xFF34D399
                    : (ch > 0 ? 0xFFF87171 : 0xFF94A3B8), 10, false);
            line.addView(petrolTv);
            line.addView(dieselTv);
            line.addView(changeTv);
            line.setClickable(true);
            line.setFocusable(true);
            line.setOnClickListener(v -> selectCity(cityName, cityEn, distId));
            list.addView(line);
        }
    }

    private void selectCity(@Nullable String cityName, @Nullable String cityEn, int distId) {
        selectedCity = cityName;
        selectedCityEn = cityEn;
        selectedDistrictId = distId;
        if (activeFuel != null) bindUi(activeFuel);
        Toast.makeText(this,
                TextUtils.isEmpty(cityName)
                        ? FuelLabels.stateAverageSelected(this) : cityName,
                Toast.LENGTH_SHORT).show();
    }

    private boolean cityRowSelected(@Nullable String cityName, @Nullable String cityEn, int distId) {
        if (selectedDistrictId > 0 && distId > 0 && selectedDistrictId == distId) return true;
        if (!TextUtils.isEmpty(selectedCityEn) && !TextUtils.isEmpty(cityEn)
                && selectedCityEn.equalsIgnoreCase(cityEn)) {
            return true;
        }
        return !TextUtils.isEmpty(selectedCity) && selectedCity.equals(cityName);
    }

    private TextView makeTv(String text, int color, int sp, boolean bold) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(color);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        tv.setPadding((int) (4 * getResources().getDisplayMetrics().density), 0,
                (int) (4 * getResources().getDisplayMetrics().density), 0);
        if (bold) tv.setTypeface(tv.getTypeface(), android.graphics.Typeface.BOLD);
        return tv;
    }

    private void tintChange(int id, double v) {
        TextView tv = findViewById(id);
        if (tv == null) return;
        if (v < -0.001) tv.setTextColor(0xFF34D399);
        else if (v > 0.001) tv.setTextColor(0xFFF87171);
        else tv.setTextColor(0xFF94A3B8);
    }

    private static String changeLine(double v) {
        if (!Double.isFinite(v) || Math.abs(v) < 0.001) return "—";
        String arrow = v < 0 ? "▼" : "▲";
        return arrow + " " + LocalUpdatesConfigHelper.formatMoney2(Math.abs(v));
    }

    private static double positiveOr(double v, double fallback) {
        return (Double.isFinite(v) && v > 0) ? v : fallback;
    }

    @Override
    protected void onDestroy() {
        DailyUpdatesShareLanguage.end();
        super.onDestroy();
    }

    private void wireClicks() {
        View back = findViewById(R.id.fuel_back);
        if (back != null) back.setOnClickListener(v -> finish());
        View share = findViewById(R.id.fuel_share);
        if (share != null) {
            share.setOnClickListener(v -> {
                JSONObject f = activeFuel != null ? activeFuel : new JSONObject();
                double petrol = positiveOr(LocalUpdatesConfigHelper.optNumber(f, "petrol_price", 0), 0);
                double diesel = positiveOr(LocalUpdatesConfigHelper.optNumber(f, "diesel_price", 0), 0);
                String where = stateLabelForKey(selectedStateKey, f);
                String body = DailyUpdatesShareLinks.withLink(this, FuelLabels.shareBody(
                        this,
                        where,
                        petrol > 0 ? LocalUpdatesConfigHelper.formatMoney2(petrol) : "—",
                        diesel > 0 ? LocalUpdatesConfigHelper.formatMoney2(diesel) : "—"),
                        "fuel");
                Intent send = new Intent(Intent.ACTION_SEND);
                send.setType("text/plain");
                send.putExtra(Intent.EXTRA_TEXT, body);
                startActivity(Intent.createChooser(send, getString(R.string.local_update_share)));
            });
        }

        View.OnClickListener openState = v -> {
            if (countStatesWithData() >= 2) showStatePicker();
        };
        View stateFilter = findViewById(R.id.fuel_state_filter);
        if (stateFilter != null) stateFilter.setOnClickListener(openState);
        View location = findViewById(R.id.fuel_location);
        if (location != null) location.setOnClickListener(openState);
        View region = findViewById(R.id.fuel_region);
        if (region != null) region.setOnClickListener(openState);

        View.OnClickListener openCities = v -> showCityPicker();
        View promoCta = findViewById(R.id.fuel_promo_cta);
        if (promoCta != null) promoCta.setOnClickListener(openCities);
        View districtFilter = findViewById(R.id.fuel_district_filter);
        if (districtFilter != null) districtFilter.setOnClickListener(openCities);
    }

    /** Hide state chip / chevron when only one splash state has real prices. */
    private void updateStateFilterUi(@NonNull String stateLabel) {
        View stateFilter = findViewById(R.id.fuel_state_filter);
        if (stateFilter == null) return;
        int available = countStatesWithData();
        if (available <= 1) {
            // Single state: show label without picker, or hide chip entirely.
            stateFilter.setVisibility(View.GONE);
            return;
        }
        stateFilter.setVisibility(View.VISIBLE);
        setText(R.id.fuel_state_filter, stateLabel + " ▾");
        stateFilter.setClickable(true);
        stateFilter.setFocusable(true);
    }

    /** States in stored {@code places} (legacy: splash {@code by_state}). */
    private int countStatesWithData() {
        return statesWithData().size();
    }

    @NonNull
    private java.util.List<String[]> statesWithData() {
        JSONObject root = LocalUpdatesConfigHelper.fuelJson(this);
        if (root == null) root = new JSONObject();
        if (hasPlaces(root)) return statesFromPlaces(root);
        JSONObject byState = root.optJSONObject("by_state");
        if (byState == null) byState = new JSONObject();

        String[] keys = {KEY_AP, KEY_TG};
        String[] labels = {
                FuelLabels.stateAndhra(this),
                FuelLabels.stateTelangana(this)
        };
        java.util.List<String[]> out = new java.util.ArrayList<>();
        String rootKey = resolveStateKey(root);
        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];
            JSONObject snap = byState.optJSONObject(key);
            boolean hasData = snapHasPrices(snap);
            if (!hasData && key.equals(rootKey)) {
                hasData = LocalUpdatesConfigHelper.optNumber(root, "petrol_price", 0) > 0
                        && LocalUpdatesConfigHelper.optNumber(root, "diesel_price", 0) > 0;
            }
            if (hasData) out.add(new String[]{key, labels[i]});
        }
        return out;
    }

    private static boolean snapHasPrices(@Nullable JSONObject snap) {
        return snap != null
                && LocalUpdatesConfigHelper.optNumber(snap, "petrol_price", 0) > 0
                && LocalUpdatesConfigHelper.optNumber(snap, "diesel_price", 0) > 0;
    }

    private void showStatePicker() {
        JSONObject root = LocalUpdatesConfigHelper.fuelJson(this);
        if (root == null) root = new JSONObject();
        JSONObject byState = root.optJSONObject("by_state");
        if (byState == null) byState = new JSONObject();

        java.util.List<String[]> available = statesWithData();
        if (available.size() < 2) return; // No multi-state picker

        BottomSheetDialog sheet = new BottomSheetDialog(this);
        View content = getLayoutInflater().inflate(R.layout.bottom_sheet_fuel_picker, null);
        TextView title = content.findViewById(R.id.fuel_picker_title);
        if (title != null) title.setText(FuelLabels.pickState(this));
        TextView sub = content.findViewById(R.id.fuel_picker_subtitle);
        if (sub != null) sub.setVisibility(View.GONE);
        LinearLayout list = content.findViewById(R.id.fuel_picker_list);
        float dens = getResources().getDisplayMetrics().density;
        View selectedRow = null;
        for (int i = 0; i < available.size(); i++) {
            final String key = available.get(i)[0];
            final String label = available.get(i)[1];
            JSONObject snap = byState.optJSONObject(key);
            boolean selected = key.equals(selectedStateKey);

            LinearLayout row = new LinearLayout(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            if (i > 0) lp.topMargin = (int) (8 * dens);
            row.setLayoutParams(lp);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setBackgroundResource(R.drawable.bg_local_update_card);
            if (selected) row.setBackgroundColor(0x33FF6A1A);
            row.setPadding((int) (14 * dens), (int) (12 * dens), (int) (14 * dens), (int) (12 * dens));

            TextView name = new TextView(this);
            name.setLayoutParams(new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            name.setText(label + (selected ? "  ✓" : ""));
            name.setTextColor(selected ? 0xFFFF6A1A : 0xFFFFFFFF);
            name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            name.setTypeface(name.getTypeface(), android.graphics.Typeface.BOLD);
            row.addView(name);

            TextView meta = new TextView(this);
            double p = priceForState(root, key, snap);
            meta.setText(p > 0 ? ("₹" + LocalUpdatesConfigHelper.formatMoney2(p)) : "");
            meta.setTextColor(0xFF94A3B8);
            meta.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            row.addView(meta);

            row.setOnClickListener(v -> {
                sheet.dismiss();
                applyStateSelection(key);
            });
            list.addView(row);
            if (selected) selectedRow = row;
        }
        sheet.setContentView(content);
        revealPickerRow(sheet, content, selectedRow);
        sheet.show();
    }

    private void showCityPicker() {
        JSONObject f = activeFuel != null ? activeFuel : new JSONObject();
        JSONArray cities = LocalUpdatesConfigHelper.optArray(f, "cities");
        if (cities.length() == 0) {
            Toast.makeText(this, FuelLabels.noCityRates(this), Toast.LENGTH_SHORT).show();
            return;
        }
        BottomSheetDialog sheet = new BottomSheetDialog(this);
        View content = getLayoutInflater().inflate(R.layout.bottom_sheet_fuel_picker, null);
        TextView title = content.findViewById(R.id.fuel_picker_title);
        if (title != null) title.setText(FuelLabels.pickCity(this));
        TextView sub = content.findViewById(R.id.fuel_picker_subtitle);
        if (sub != null) {
            sub.setVisibility(View.VISIBLE);
            sub.setText(FuelLabels.cityPickerSub(
                    this, stateLabelForKey(selectedStateKey, f)));
        }
        LinearLayout list = content.findViewById(R.id.fuel_picker_list);
        float dens = getResources().getDisplayMetrics().density;
        View selectedRow = null;

        boolean avg = TextUtils.isEmpty(selectedCity) && selectedDistrictId <= 0;
        View avgRow = pickerRow(FuelLabels.stateAveragePrimary(this),
                heroPriceLabel(f), avg, dens, () -> {
                    sheet.dismiss();
                    selectedCity = null;
                    selectedCityEn = null;
                    selectedDistrictId = 0;
                    bindUi(activeFuel);
                    scrollToCities();
                });
        list.addView(avgRow);
        if (avg) selectedRow = avgRow;

        for (int i = 0; i < cities.length(); i++) {
            JSONObject row = cities.optJSONObject(i);
            if (row == null) continue;
            double p = LocalUpdatesConfigHelper.optNumber(row, "petrol", 0);
            double di = LocalUpdatesConfigHelper.optNumber(row, "diesel", 0);
            if (!(p > 0) || !(di > 0)) continue;
            final String cityName = LocalUpdatesConfigHelper.optString(row, "city", "");
            final String cityEn = LocalUpdatesConfigHelper.optString(row, "city_en", "");
            final int distId = row.optInt("district_id", 0);
            String price = DailyUpdatesLabels.petrol(this) + " ₹"
                    + LocalUpdatesConfigHelper.formatMoney2(p)
                    + "  " + DailyUpdatesLabels.diesel(this) + " ₹"
                    + LocalUpdatesConfigHelper.formatMoney2(di);
            boolean sel = cityRowSelected(cityName, cityEn, distId);
            View cityRow = pickerRow(cityName, price, sel, dens, () -> {
                sheet.dismiss();
                selectCity(cityName, cityEn, distId);
                scrollToCities();
            });
            list.addView(cityRow);
            if (sel) selectedRow = cityRow;
        }
        sheet.setContentView(content);
        revealPickerRow(sheet, content, selectedRow);
        sheet.show();
    }

    private View pickerRow(String name, String meta, boolean selected, float dens, Runnable onClick) {
        LinearLayout row = new LinearLayout(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = (int) (8 * dens);
        row.setLayoutParams(lp);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setBackgroundResource(R.drawable.bg_local_update_card);
        if (selected) row.setBackgroundColor(0x33FF6A1A);
        row.setPadding((int) (14 * dens), (int) (12 * dens), (int) (14 * dens), (int) (12 * dens));

        TextView nameTv = new TextView(this);
        nameTv.setLayoutParams(new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        nameTv.setText(name + (selected ? "  ✓" : ""));
        nameTv.setTextColor(selected ? 0xFFFF6A1A : 0xFFFFFFFF);
        nameTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        nameTv.setTypeface(nameTv.getTypeface(), android.graphics.Typeface.BOLD);
        row.addView(nameTv);

        TextView metaTv = new TextView(this);
        metaTv.setText(meta != null ? meta : "");
        metaTv.setTextColor(0xFF94A3B8);
        metaTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        row.addView(metaTv);

        row.setOnClickListener(v -> onClick.run());
        return row;
    }

    private String heroPriceLabel(@NonNull JSONObject f) {
        double p = LocalUpdatesConfigHelper.optNumber(f, "petrol_price", 0);
        return p > 0 ? ("₹" + LocalUpdatesConfigHelper.formatMoney2(p)) : "";
    }

    private void scrollToCities() {
        NestedScrollView scroll = findViewById(R.id.fuel_scroll);
        View cities = findViewById(R.id.fuel_cities_list);
        if (scroll != null && cities != null) {
            scroll.post(() -> scroll.smoothScrollTo(0, Math.max(0, cities.getTop() - 80)));
        }
    }

    private void applyStateSelection(@NonNull String key) {
        statePickedInSession = true;
        JSONObject root = LocalUpdatesConfigHelper.fuelJson(this);
        if (root == null) root = new JSONObject();
        selectedStateKey = key;
        selectedCity = resolveInitialCity(root, key);
        if (TextUtils.isEmpty(selectedCity)) {
            selectedCity = firstCityInState(root, key);
        }
        activeFuel = overlayState(root, key);
        bindUi(activeFuel);
        ensureLocationNames();
    }

    /** Open the sheet on the saved row, not the first row. */
    private void revealPickerRow(
            @NonNull BottomSheetDialog sheet,
            @NonNull View content,
            @Nullable View selectedRow) {
        ScrollView scroll = content.findViewById(R.id.fuel_picker_scroll);
        sheet.setOnShowListener(dialog -> {
            if (scroll == null || selectedRow == null) return;
            int maxH = (int) (getResources().getDisplayMetrics().heightPixels * 0.62f);
            scroll.post(() -> {
                View list = content.findViewById(R.id.fuel_picker_list);
                int contentH = list != null ? list.getHeight() : 0;
                if (contentH > maxH && scroll.getLayoutParams() != null) {
                    scroll.getLayoutParams().height = maxH;
                    scroll.requestLayout();
                }
                scroll.post(() -> scroll.smoothScrollTo(0, Math.max(0, selectedRow.getTop())));
            });
        });
    }

    /**
     * Merge {@code by_state[key]} onto the splash root so chrome / cities / hero refresh.
     */
    @NonNull
    private JSONObject overlayState(@NonNull JSONObject root, @NonNull String key) {
        if (hasPlaces(root)) return overlayFromPlaces(root, key);
        try {
            JSONObject out = new JSONObject(root.toString());
            JSONObject byState = root.optJSONObject("by_state");
            JSONObject snap = byState != null ? byState.optJSONObject(key) : null;
            if (snap != null
                    && LocalUpdatesConfigHelper.optNumber(snap, "petrol_price", 0) > 0) {
                copyIfPresent(snap, out, "location_name");
                copyIfPresent(snap, out, "location_region");
                copyIfPresent(snap, out, "state_filter");
                copyIfPresent(snap, out, "last_updated");
                copyIfPresent(snap, out, "petrol_price");
                copyIfPresent(snap, out, "diesel_price");
                copyIfPresent(snap, out, "petrol_change");
                copyIfPresent(snap, out, "diesel_change");
                copyIfPresent(snap, out, "history_title");
                copyIfPresent(snap, out, "history_note");
                copyIfPresent(snap, out, "history_kind");
                copyIfPresent(snap, out, "alert_subtext");
                if (snap.has("cities")) out.put("cities", snap.get("cities"));
                if (snap.has("history_7day")) out.put("history_7day", snap.get("history_7day"));
                out.put("state_key", key);
            } else {
                out.put("state_key", key);
            }
            // Keep by_state + chrome fields from root
            if (byState != null) out.put("by_state", byState);
            return out;
        } catch (Exception e) {
            return root;
        }
    }

    private static void copyIfPresent(JSONObject from, JSONObject to, String key) throws Exception {
        if (from.has(key) && !from.isNull(key)) to.put(key, from.get(key));
    }

    private boolean hasPlaces(@Nullable JSONObject root) {
        JSONArray places = root != null ? root.optJSONArray("places") : null;
        return places != null && places.length() > 0;
    }

    @NonNull
    private JSONArray placesOf(@Nullable JSONObject root) {
        if (root == null) return new JSONArray();
        JSONArray places = root.optJSONArray("places");
        return places != null ? places : new JSONArray();
    }

    @NonNull
    private String resolveInitialStateKey(@NonNull JSONObject root) {
        if (!hasPlaces(root)) return resolveStateKey(root);
        int stateId = savedStateId();
        if (stateId > 0) {
            JSONArray places = placesOf(root);
            for (int i = 0; i < places.length(); i++) {
                JSONObject row = places.optJSONObject(i);
                if (row == null || row.optInt("state_id", 0) != stateId) continue;
                String key = row.optString("state_key", "");
                if (!key.isEmpty()) return key;
            }
        }
        return resolveStateKey(root);
    }

    @Nullable
    private String resolveInitialCity(@NonNull JSONObject root, @NonNull String stateKey) {
        selectedDistrictId = 0;
        selectedCityEn = null;
        if (!hasPlaces(root)) return null;
        int distId = SharedPrefsData.getInt(this, Constants.Selected_Distric_Id, Constants.PREF_NAME);
        String nameA = SharedPrefsData.getString(this, Constants.Selected_Distric_Name1, Constants.PREF_NAME);
        String nameB = SharedPrefsData.getString(this, Constants.Selected_Distric_Name, Constants.PREF_NAME);
        JSONArray places = placesOf(root);
        String byName = null;
        String byEn = null;
        int byDist = 0;
        for (int i = 0; i < places.length(); i++) {
            JSONObject row = places.optJSONObject(i);
            if (row == null) continue;
            if (!stateKey.equals(row.optString("state_key", ""))) continue;
            String city = displayCityName(row);
            String cityEn = row.optString("city_en", "");
            int rowDist = row.optInt("district_id", 0);
            if (distId > 0 && rowDist == distId && !city.isEmpty()) {
                selectedDistrictId = rowDist;
                selectedCityEn = cityEn;
                return city;
            }
            if (namesMatch(nameA, city, cityEn) || namesMatch(nameB, city, cityEn)) {
                byName = city;
                byEn = cityEn;
                byDist = rowDist;
            }
        }
        if (byName != null) {
            selectedDistrictId = byDist;
            selectedCityEn = byEn;
        }
        return byName;
    }

    @NonNull
    private java.util.List<String[]> statesFromPlaces(@NonNull JSONObject root) {
        java.util.LinkedHashMap<String, String> labels = new java.util.LinkedHashMap<>();
        JSONArray places = placesOf(root);
        for (int i = 0; i < places.length(); i++) {
            JSONObject row = places.optJSONObject(i);
            if (row == null) continue;
            if (!(row.optDouble("petrol", 0) > 0) || !(row.optDouble("diesel", 0) > 0)) continue;
            String key = row.optString("state_key", "");
            if (key.isEmpty() || labels.containsKey(key)) continue;
            String label = displayState(row);
            if (label.isEmpty()) label = key;
            labels.put(key, label);
        }
        java.util.List<String[]> out = new java.util.ArrayList<>();
        if (labels.containsKey(KEY_TG)) {
            out.add(new String[]{KEY_TG, labels.remove(KEY_TG)});
        }
        if (labels.containsKey(KEY_AP)) {
            out.add(new String[]{KEY_AP, labels.remove(KEY_AP)});
        }
        java.util.List<String> rest = new java.util.ArrayList<>(labels.keySet());
        rest.sort((a, b) -> String.valueOf(labels.get(a)).compareToIgnoreCase(String.valueOf(labels.get(b))));
        for (String key : rest) out.add(new String[]{key, labels.get(key)});
        return out;
    }

    private double priceForState(@NonNull JSONObject root, @NonNull String key, @Nullable JSONObject snap) {
        if (hasPlaces(root)) {
            double first = 0;
            double hyd = 0;
            JSONArray places = placesOf(root);
            for (int i = 0; i < places.length(); i++) {
                JSONObject row = places.optJSONObject(i);
                if (row == null || !key.equals(row.optString("state_key", ""))) continue;
                double petrol = row.optDouble("petrol", 0);
                if (!(petrol > 0)) continue;
                if (!(first > 0)) first = petrol;
                if ("hyderabad".equalsIgnoreCase(row.optString("city_en", ""))) hyd = petrol;
            }
            if (hyd > 0) return hyd;
            if (first > 0) return first;
        }
        double p = snap != null ? LocalUpdatesConfigHelper.optNumber(snap, "petrol_price", 0) : 0;
        if (!(p > 0) && key.equals(resolveStateKey(root))) {
            p = LocalUpdatesConfigHelper.optNumber(root, "petrol_price", 0);
        }
        return p;
    }

    @NonNull
    private JSONObject overlayFromPlaces(@NonNull JSONObject root, @NonNull String key) {
        try {
            JSONObject out = new JSONObject(root.toString());
            JSONArray cities = new JSONArray();
            JSONObject hero = null;
            String stateLabel = "";
            JSONArray places = placesOf(root);
            for (int i = 0; i < places.length(); i++) {
                JSONObject row = places.optJSONObject(i);
                if (row == null || !key.equals(row.optString("state_key", ""))) continue;
                double petrol = row.optDouble("petrol", 0);
                double diesel = row.optDouble("diesel", 0);
                if (!(petrol > 0) || !(diesel > 0)) continue;
                String city = displayCityName(row);
                if (city.isEmpty()) continue;
                JSONObject cityRow = new JSONObject();
                cityRow.put("city", city);
                cityRow.put("city_en", row.optString("city_en", city));
                cityRow.put("petrol", petrol);
                cityRow.put("diesel", diesel);
                cityRow.put("change", row.optDouble("change", 0));
                cityRow.put("diesel_change", row.optDouble("diesel_change", row.optDouble("change", 0)));
                if (row.has("history")) cityRow.put("history", row.get("history"));
                if (row.optInt("district_id", 0) > 0) cityRow.put("district_id", row.optInt("district_id"));
                cities.put(cityRow);
                if (stateLabel.isEmpty()) stateLabel = displayState(row);
                boolean picked = !TextUtils.isEmpty(selectedCity) && selectedCity.equals(city);
                if (picked || hero == null) {
                    hero = cityRow;
                }
            }
            if (!stateLabel.isEmpty()) {
                out.put("location_name", stateLabel);
                out.put("location_region", stateLabel);
                out.put("state_filter", stateLabel);
            }
            out.put("cities", cities);
            out.put("state_key", key);
            if (hero != null) {
                out.put("petrol_price", hero.optDouble("petrol"));
                out.put("diesel_price", hero.optDouble("diesel"));
                out.put("petrol_change", hero.optDouble("change", 0));
                out.put("diesel_change", hero.optDouble("change", 0));
            }
            return out;
        } catch (Exception e) {
            return root;
        }
    }

    @Nullable
    private String firstCityInState(@NonNull JSONObject root, @NonNull String stateKey) {
        JSONArray places = placesOf(root);
        String first = null;
        String firstEn = null;
        int firstDist = 0;
        for (int i = 0; i < places.length(); i++) {
            JSONObject row = places.optJSONObject(i);
            if (row == null || !stateKey.equals(row.optString("state_key", ""))) continue;
            if (!(row.optDouble("petrol", 0) > 0) || !(row.optDouble("diesel", 0) > 0)) continue;
            String city = displayCityName(row);
            if (city.isEmpty()) continue;
            if (first == null || city.compareToIgnoreCase(first) < 0) {
                first = city;
                firstEn = row.optString("city_en", "");
                firstDist = row.optInt("district_id", 0);
            }
        }
        if (first != null) {
            selectedCityEn = firstEn;
            selectedDistrictId = firstDist;
        }
        return first;
    }

    /**
     * Redis pack only. Apply when the hourly snapshot is newer, or when this
     * phone is still missing per-district history for the same snapshot.
     */
    private void refreshFromRedis() {
        try {
            com.anyNews.anynews.Services.APIService api =
                    com.anyNews.anynews.Services.RestApiClient.getClient()
                            .create(com.anyNews.anynews.Services.APIService.class);
            api.fuelDataset("v3").enqueue(new retrofit2.Callback<com.google.gson.JsonObject>() {
                @Override
                public void onResponse(
                        @NonNull retrofit2.Call<com.google.gson.JsonObject> call,
                        @NonNull retrofit2.Response<com.google.gson.JsonObject> response) {
                    if (isFinishing() || response.body() == null) return;
                    applyRedisPack(response.body().toString());
                }

                @Override
                public void onFailure(
                        @NonNull retrofit2.Call<com.google.gson.JsonObject> call,
                        @NonNull Throwable t) {
                    /* keep the splash / feed pack */
                }
            });
        } catch (Exception ignored) {
        }
    }

    private void applyRedisPack(@NonNull String raw) {
        try {
            JSONObject body = new JSONObject(raw);
            JSONArray places = body.optJSONArray("places");
            if (places == null || places.length() == 0) return;
            String remoteAt = body.optString("fetched_at", "");
            JSONObject local = LocalUpdatesConfigHelper.fuelJson(this);
            if (local == null) local = new JSONObject();
            String localAt = local.optString("fetched_at", "");
            // Older Redis snapshot must not replace a newer pack already on the phone.
            if (!remoteAt.isEmpty() && !localAt.isEmpty() && remoteAt.compareTo(localAt) < 0) return;
            if (remoteAt.isEmpty() && !localAt.isEmpty()) return;
            JSONObject next = new JSONObject(local.toString());
            next.put("places", places);
            if (!remoteAt.isEmpty()) next.put("fetched_at", remoteAt);
            if (body.has("cache_date")) next.put("cache_date", body.optString("cache_date", ""));
            LocalUpdatesConfigHelper.saveFuelFromSplash(this, next);
            runOnUiThread(() -> {
                if (isFinishing()) return;
                applyNameMaps();
                JSONObject root = LocalUpdatesConfigHelper.fuelJson(this);
                if (root == null) return;
                if (!statePickedInSession) {
                    String again = resolveInitialStateKey(root);
                    if (!again.equals(selectedStateKey)) {
                        selectedStateKey = again;
                        selectedCity = null;
                    }
                }
                if (TextUtils.isEmpty(selectedCity)
                        || !cityExistsInState(root, selectedStateKey, selectedCity)) {
                    selectedCity = resolveInitialCity(root, selectedStateKey);
                    if (TextUtils.isEmpty(selectedCity)) {
                        selectedCity = firstCityInState(root, selectedStateKey);
                    }
                } else {
                    refreshSelectionLabel(root);
                }
                activeFuel = overlayState(root, selectedStateKey);
                bindUi(activeFuel);
                ensureLocationNames();
            });
        } catch (Exception ignored) {
        }
    }

    private boolean cityExistsInState(
            @NonNull JSONObject root, @NonNull String stateKey, @NonNull String cityName) {
        JSONArray places = root.optJSONArray("places");
        if (places == null) return false;
        for (int i = 0; i < places.length(); i++) {
            JSONObject row = places.optJSONObject(i);
            if (row == null || !stateKey.equals(row.optString("state_key", ""))) continue;
            if (selectedDistrictId > 0 && row.optInt("district_id", 0) == selectedDistrictId) {
                return true;
            }
            if (cityName.equals(displayCityName(row))
                    || cityName.equalsIgnoreCase(row.optString("city_en", ""))
                    || cityName.equals(row.optString("city", ""))) {
                return true;
            }
        }
        return false;
    }

    /**
     * City in the selected language. Location-master wins when it is already
     * that script. Otherwise the English source is mapped into that script.
     * English and another language's script are not shown.
     */
    @NonNull
    private String displayCityName(@NonNull JSONObject row) {
        int lang = languageId();
        int stateId = row.optInt("state_id", 0);
        int distId = row.optInt("district_id", 0);
        if (stateId > 0 && distId > 0) {
            String master = districtNamesByKey.get(stateId + ":" + distId);
            if (FuelPlaceNames.matchesLanguage(master, lang)) return master.trim();
        }
        if (distId > 0) {
            String saved = savedDistrictLabel(distId);
            if (FuelPlaceNames.matchesLanguage(saved, lang)) return saved.trim();
        }
        String written = row.optString("city", "");
        if (FuelPlaceNames.matchesLanguage(written, lang)) return written.trim();
        String en = row.optString("city_en", "");
        if (en.isEmpty()) en = written;
        String mapped = FuelPlaceNames.inLanguage(en, lang);
        if (!mapped.isEmpty()) return mapped;
        return en;
    }

    /**
     * State name in the selected language. Andhra Pradesh and Telangana use
     * the language labels. Every other state is mapped into that same script.
     */
    @NonNull
    private String displayState(@NonNull JSONObject row) {
        int lang = languageId();
        int stateId = row.optInt("state_id", 0);
        if (stateId > 0) {
            String master = stateNamesById.get(stateId);
            if (FuelPlaceNames.matchesLanguage(master, lang)) return master.trim();
        }
        String key = row.optString("state_key", "");
        if (KEY_TG.equals(key)) {
            String labeled = FuelLabels.stateTelangana(this);
            if (FuelPlaceNames.matchesLanguage(labeled, lang)) return labeled;
        } else if (KEY_AP.equals(key)) {
            String labeled = FuelLabels.stateAndhra(this);
            if (FuelPlaceNames.matchesLanguage(labeled, lang)) return labeled;
        }
        String en = row.optString("state", "");
        if (en.isEmpty()) en = row.optString("source_state", "");
        String mapped = FuelPlaceNames.inLanguage(en, lang);
        if (!mapped.isEmpty()) return mapped;
        if (KEY_TG.equals(key)) return FuelLabels.stateTelangana(this);
        if (KEY_AP.equals(key)) return FuelLabels.stateAndhra(this);
        return en;
    }

    @Nullable
    private String savedDistrictLabel(int distId) {
        int saved = SharedPrefsData.getInt(this, Constants.Selected_Distric_Id, Constants.PREF_NAME);
        if (distId <= 0 || distId != saved) return null;
        return DistrictLabelHelper.resolveDisplayLabel(this);
    }

    private static boolean namesMatch(@Nullable String pref, @NonNull String city, @NonNull String cityEn) {
        if (pref == null) return false;
        String n = pref.trim();
        if (n.isEmpty()) return false;
        return n.equalsIgnoreCase(city) || n.equalsIgnoreCase(cityEn);
    }

    @NonNull
    private static String resolveStateKey(@Nullable JSONObject f) {
        if (f == null) return KEY_AP;
        String key = LocalUpdatesConfigHelper.optString(f, "state_key", "");
        if (KEY_TG.equalsIgnoreCase(key) || "ts".equalsIgnoreCase(key)) return KEY_TG;
        if (KEY_AP.equalsIgnoreCase(key)) return KEY_AP;
        String hay = (LocalUpdatesConfigHelper.optString(f, "state_filter", "")
                + " " + LocalUpdatesConfigHelper.optString(f, "location_region", "")).toLowerCase();
        if (hay.contains("telangana") || hay.contains("తెలంగాణ") || hay.contains("tg")) {
            return KEY_TG;
        }
        return KEY_AP;
    }

    @NonNull
    private String stateLabelForKey(@NonNull String key, @Nullable JSONObject f) {
        JSONObject root = LocalUpdatesConfigHelper.fuelJson(this);
        JSONArray places = placesOf(root != null ? root : f);
        for (int i = 0; i < places.length(); i++) {
            JSONObject row = places.optJSONObject(i);
            if (row == null || !key.equals(row.optString("state_key", ""))) continue;
            String label = displayState(row);
            if (!label.isEmpty()) return label;
        }
        if (KEY_TG.equals(key)) return FuelLabels.stateTelangana(this);
        if (KEY_AP.equals(key)) return FuelLabels.stateAndhra(this);
        if (f != null) {
            String s = LocalUpdatesConfigHelper.optString(f, "state_filter", "");
            s = s.replace("▾", "").replace("▼", "").trim();
            String mapped = FuelPlaceNames.inLanguage(s, languageId());
            if (FuelPlaceNames.matchesLanguage(mapped, languageId())) return mapped;
            s = LocalUpdatesConfigHelper.optString(f, "location_region", "");
            mapped = FuelPlaceNames.inLanguage(s, languageId());
            if (FuelPlaceNames.matchesLanguage(mapped, languageId())) return mapped;
        }
        return FuelLabels.stateAndhra(this);
    }

    @Nullable
    private static JSONObject findCity(@NonNull JSONObject f, @NonNull String cityName) {
        JSONArray cities = LocalUpdatesConfigHelper.optArray(f, "cities");
        for (int i = 0; i < cities.length(); i++) {
            JSONObject row = cities.optJSONObject(i);
            if (row == null) continue;
            if (cityName.equals(LocalUpdatesConfigHelper.optString(row, "city", ""))) return row;
        }
        return null;
    }

    private static double resolvePrice(@Nullable JSONObject f, boolean petrol) {
        if (f == null) return 0;
        String[] keys = petrol
                ? new String[]{"petrol_price", "petrol"}
                : new String[]{"diesel_price", "diesel"};
        for (String k : keys) {
            double v = LocalUpdatesConfigHelper.optNumber(f, k, 0);
            if (v > 0) return v;
        }
        JSONArray cities = LocalUpdatesConfigHelper.optArray(f, "cities");
        if (cities.length() > 0) {
            JSONObject row = cities.optJSONObject(0);
            if (row != null) {
                return LocalUpdatesConfigHelper.optNumber(row, petrol ? "petrol" : "diesel", 0);
            }
        }
        return 0;
    }

    private static double resolveChange(@Nullable JSONObject f, boolean petrol) {
        if (f == null) return 0;
        String k = petrol ? "petrol_change" : "diesel_change";
        if (f.has(k) && !f.isNull(k)) return LocalUpdatesConfigHelper.optNumber(f, k, 0);
        JSONArray cities = LocalUpdatesConfigHelper.optArray(f, "cities");
        if (cities.length() > 0) {
            JSONObject row = cities.optJSONObject(0);
            if (row != null && row.has("change")) {
                return LocalUpdatesConfigHelper.optNumber(row, "change", 0);
            }
        }
        return 0;
    }

    private void setText(int id, String value) {
        TextView tv = findViewById(id);
        if (tv != null) tv.setText(value);
    }

    private int languageId() {
        return SharedPrefsData.getInt(this, Constants.LanguageID, Constants.PREF_NAME);
    }

    private int savedStateId() {
        int stateId = SharedPrefsData.getInt(this, Constants.StateID, Constants.PREF_NAME);
        if (stateId > 0) return stateId;
        int lang = languageId();
        if (lang <= 0) return 0;
        return SharedPrefsData.getInt(
                this, Constants.StateID + "_lang_" + lang, Constants.PREF_NAME);
    }

    private boolean nameOk(@Nullable String name) {
        if (!DistrictLabelHelper.isUsable(name)) return false;
        int lang = languageId();
        if (lang >= 1 && lang <= 6) {
            return !DistrictLabelHelper.isClearScriptConflict(name, lang);
        }
        return !hasIndicScript(name);
    }

    private static boolean hasIndicScript(@NonNull String s) {
        for (int i = 0; i < s.length(); ) {
            int cp = s.codePointAt(i);
            i += Character.charCount(cp);
            if (cp >= 0x0900 && cp <= 0x0D7F) return true;
        }
        return false;
    }

    /** State and district names already cached for the selected language. */
    private void applyNameMaps() {
        stateNamesById.clear();
        districtNamesByKey.clear();
        int lang = languageId();
        if (lang <= 0) return;
        List<StatesModel.Datum> states = SelectionListCache.loadStates(this, lang);
        if (states != null) {
            for (StatesModel.Datum s : states) {
                if (s == null || s.getId() == null || s.getId() <= 0) continue;
                if (nameOk(s.getName())) stateNamesById.put(s.getId(), s.getName().trim());
            }
        }
        Set<Integer> stateIds = new HashSet<>();
        JSONArray places = placesOf(LocalUpdatesConfigHelper.fuelJson(this));
        for (int i = 0; i < places.length(); i++) {
            JSONObject row = places.optJSONObject(i);
            if (row == null) continue;
            int sid = row.optInt("state_id", 0);
            if (sid > 0) stateIds.add(sid);
        }
        for (int sid : stateIds) {
            // District lists are per language. Skip a state the language does
            // not own — that request would return another state's districts.
            if (!stateNamesById.containsKey(sid)) continue;
            List<DistricsModel.Datum> dists = SelectionListCache.loadDistricts(this, lang, sid);
            if (dists == null) continue;
            for (DistricsModel.Datum d : dists) {
                if (d == null || d.getId() == null || d.getId() <= 0) continue;
                if (!nameOk(d.getName())) continue;
                districtNamesByKey.put(sid + ":" + d.getId(), d.getName().trim());
            }
        }
    }

    private void refreshSelectionLabel(@NonNull JSONObject root) {
        JSONArray places = placesOf(root);
        for (int i = 0; i < places.length(); i++) {
            JSONObject row = places.optJSONObject(i);
            if (row == null || !selectedStateKey.equals(row.optString("state_key", ""))) continue;
            int dist = row.optInt("district_id", 0);
            String en = row.optString("city_en", "");
            boolean hit = (selectedDistrictId > 0 && dist == selectedDistrictId)
                    || (!TextUtils.isEmpty(selectedCityEn) && selectedCityEn.equalsIgnoreCase(en))
                    || (!TextUtils.isEmpty(selectedCity) && (selectedCity.equals(row.optString("city", ""))
                    || selectedCity.equalsIgnoreCase(en)));
            if (!hit) continue;
            String name = displayCityName(row);
            if (name.isEmpty()) return;
            selectedCity = name;
            if (dist > 0) selectedDistrictId = dist;
            if (!en.isEmpty()) selectedCityEn = en;
            return;
        }
    }

    private void ensureLocationNames() {
        int lang = languageId();
        if (lang <= 0 || isFinishing()) return;
        if (stateNamesById.isEmpty() && !statesFetchStarted) {
            List<StatesModel.Datum> cached = SelectionListCache.loadStates(this, lang);
            if (cached == null || cached.isEmpty()) {
                fetchStates(lang);
                return;
            }
        }
        int sid = stateIdForKey(selectedStateKey);
        if (sid > 0 && stateNamesById.containsKey(sid)
                && districtsMissing(lang, sid)
                && districtFetchStarted.add(sid)) {
            fetchDistricts(lang, sid);
        }
    }

    private int stateIdForKey(@NonNull String key) {
        JSONArray places = placesOf(LocalUpdatesConfigHelper.fuelJson(this));
        for (int i = 0; i < places.length(); i++) {
            JSONObject row = places.optJSONObject(i);
            if (row == null || !key.equals(row.optString("state_key", ""))) continue;
            int sid = row.optInt("state_id", 0);
            if (sid > 0) return sid;
        }
        return 0;
    }

    private boolean districtsMissing(int lang, int stateId) {
        List<DistricsModel.Datum> list = SelectionListCache.loadDistricts(this, lang, stateId);
        return list == null || list.isEmpty();
    }

    private void onLocationNamesUpdated() {
        if (isFinishing()) return;
        applyNameMaps();
        JSONObject root = LocalUpdatesConfigHelper.fuelJson(this);
        if (root == null) root = new JSONObject();
        if (!statePickedInSession) {
            String again = resolveInitialStateKey(root);
            if (!again.equals(selectedStateKey)) {
                selectedStateKey = again;
                selectedCity = null;
            }
        }
        if (TextUtils.isEmpty(selectedCity)) {
            selectedCity = resolveInitialCity(root, selectedStateKey);
            if (TextUtils.isEmpty(selectedCity)) {
                selectedCity = firstCityInState(root, selectedStateKey);
            }
        } else {
            refreshSelectionLabel(root);
        }
        activeFuel = overlayState(root, selectedStateKey);
        bindUi(activeFuel);
    }

    private void fetchStates(int lang) {
        statesFetchStarted = true;
        try {
            LanguageSelectionRequest requestModel = new LanguageSelectionRequest();
            requestModel.setDraftId(SharedPrefsData.getInt(this, Constants.DraftID, Constants.PREF_NAME));
            requestModel.setLanguageId(lang);
            EncritonRequestData enc = new EncritonRequestData();
            enc.setData(encrypt(new Gson().toJson(requestModel)));
            JsonObject body = new Gson().toJsonTree(enc).getAsJsonObject();
            APIService api = RestApiClient.getClient().create(APIService.class);
            api.Get_states_requiest("v3", body).enqueue(new Callback<EncreptionRequest>() {
                @Override
                public void onResponse(@NonNull Call<EncreptionRequest> call,
                                       @NonNull Response<EncreptionRequest> response) {
                    if (!response.isSuccessful() || response.body() == null
                            || TextUtils.isEmpty(response.body().getData())) {
                        return;
                    }
                    try {
                        StatesModel model = new Gson().fromJson(
                                decrypt(response.body().getData()), StatesModel.class);
                        if (model != null && model.getData() != null && !model.getData().isEmpty()) {
                            SelectionListCache.saveStates(FuelDetailActivity.this, lang, model);
                        }
                    } catch (Exception ignored) {
                        return;
                    }
                    runOnUiThread(() -> {
                        if (isFinishing()) return;
                        onLocationNamesUpdated();
                        ensureLocationNames();
                    });
                }

                @Override
                public void onFailure(@NonNull Call<EncreptionRequest> call, @NonNull Throwable t) {
                }
            });
        } catch (Exception ignored) {
        }
    }

    private void fetchDistricts(int lang, int stateId) {
        if (!stateNamesById.containsKey(stateId)) return;
        try {
            LanguageSelectionRequest requestModel = new LanguageSelectionRequest();
            requestModel.setDraftId(SharedPrefsData.getInt(this, Constants.DraftID, Constants.PREF_NAME));
            requestModel.setLanguageId(lang);
            requestModel.setStateId(stateId);
            EncritonRequestData enc = new EncritonRequestData();
            enc.setData(encrypt(new Gson().toJson(requestModel)));
            JsonObject body = new Gson().toJsonTree(enc).getAsJsonObject();
            APIService api = RestApiClient.getClient().create(APIService.class);
            api.Get_districts("v3", body).enqueue(new Callback<EncreptionRequest>() {
                @Override
                public void onResponse(@NonNull Call<EncreptionRequest> call,
                                       @NonNull Response<EncreptionRequest> response) {
                    if (!response.isSuccessful() || response.body() == null
                            || TextUtils.isEmpty(response.body().getData())) {
                        return;
                    }
                    try {
                        DistricsModel model = new Gson().fromJson(
                                decrypt(response.body().getData()), DistricsModel.class);
                        if (model != null && model.getData() != null && !model.getData().isEmpty()) {
                            SelectionListCache.saveDistricts(
                                    FuelDetailActivity.this, lang, stateId, model);
                        }
                    } catch (Exception ignored) {
                        return;
                    }
                    runOnUiThread(() -> {
                        if (isFinishing()) return;
                        onLocationNamesUpdated();
                    });
                }

                @Override
                public void onFailure(@NonNull Call<EncreptionRequest> call, @NonNull Throwable t) {
                }
            });
        } catch (Exception ignored) {
        }
    }
}
