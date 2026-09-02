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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.widget.NestedScrollView;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.anyNews.anynews.R;
import com.anyNews.anynews.Ui.FuelHistoryChartView;
import com.anyNews.anynews.Utils.LocalUpdatesConfigHelper;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Fuel prices detail — splash {@code root_settings.fuel}.
 * State / city pickers refresh hero + city table from cached {@code by_state}.
 * Promo copy is derived from today-vs-yesterday deltas (not static admin strings).
 * Chart uses real yesterday→today points only (source has no 7-day series).
 */
public class FuelDetailActivity extends AppCompatActivity {

    private static final String KEY_AP = "ap";
    private static final String KEY_TG = "tg";

    /** Working view of fuel JSON for the selected state (mutated on switch). */
    @Nullable private JSONObject activeFuel;
    @NonNull private String selectedStateKey = KEY_AP;
    @Nullable private String selectedCity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!LocalUpdatesConfigHelper.isFuelEnabled(this)) {
            Toast.makeText(this, R.string.fuel_disabled, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        setContentView(R.layout.activity_fuel_detail);
        applySystemBars(R.id.fuel_detail_root);
        clipHero();
        JSONObject root = LocalUpdatesConfigHelper.fuelJson(this);
        if (root == null) root = new JSONObject();
        selectedStateKey = resolveStateKey(root);
        activeFuel = overlayState(root, selectedStateKey);
        bindUi(activeFuel);
        wireClicks();
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
        setText(R.id.fuel_top_title, LocalUpdatesConfigHelper.optString(f, "header_title",
                getString(R.string.fuel_title_default)));
        setText(R.id.fuel_top_subtitle, LocalUpdatesConfigHelper.optString(f, "header_subtitle",
                "నేటి పెట్రోల్ & డీజిల్ ధరలు"));

        String stateLabel = stateLabelForKey(selectedStateKey, f);
        setText(R.id.fuel_location, stateLabel);
        View regionView = findViewById(R.id.fuel_region);
        if (regionView != null) regionView.setVisibility(View.GONE);

        setText(R.id.fuel_updated, LocalUpdatesConfigHelper.optString(f, "last_updated", "—"));

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
        setText(R.id.fuel_petrol_change, changeLine(petrolCh) + " / లీటర్");
        setText(R.id.fuel_diesel_change, changeLine(dieselCh) + " / లీటర్");
        tintChange(R.id.fuel_petrol_change, petrolCh);
        tintChange(R.id.fuel_diesel_change, dieselCh);

        View bannerRow = findViewById(R.id.fuel_banner_row);
        if (bannerRow != null) bannerRow.setVisibility(View.GONE);
        View banner = findViewById(R.id.fuel_banner);
        if (banner != null) banner.setVisibility(View.GONE);
        setText(R.id.fuel_cities_title, LocalUpdatesConfigHelper.optString(f, "cities_title",
                "ప్రధాన నగరాల ధరలు"));
        updateStateFilterUi(stateLabel);
        setText(R.id.fuel_cities_footnote, LocalUpdatesConfigHelper.optString(f, "cities_footnote", ""));

        // Honest history title — source has no real 7-day series
        String histTitle = LocalUpdatesConfigHelper.optString(f, "history_title",
                "ధరల మార్పు (నిన్న → ఈరోజు)");
        if (histTitle.contains("7")) {
            histTitle = "ధరల మార్పు (నిన్న → ఈరోజు)";
        }
        setText(R.id.fuel_history_title, histTitle);

        String histNote = LocalUpdatesConfigHelper.optString(f, "history_note", "");
        if (TextUtils.isEmpty(histNote)) {
            histNote = "పూర్తి 7 రోజుల చరిత్ర అందుబాటులో లేదు — నిన్న vs ఈరోజు మాత్రమే.";
        }
        setText(R.id.fuel_history_note, histNote);

        bindPromoFromDeltas(petrolCh, dieselCh);
        setText(R.id.fuel_promo_cta, LocalUpdatesConfigHelper.optString(f, "promo_cta",
                "ఇతర నగరాలను చూడండి >"));

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

        FuelHistoryChartView chart = findViewById(R.id.fuel_history_chart);
        if (chart != null) {
            chart.setHistory(honestHistory(f, petrol, diesel, petrolCh, dieselCh));
        }
    }

    /** Prefer stored ≤3-point series; else rebuild from today ± day-change (never invent mid-week). */
    @NonNull
    private JSONArray honestHistory(@NonNull JSONObject f, double petrol, double diesel,
                                    double petrolCh, double dieselCh) {
        JSONArray stored = LocalUpdatesConfigHelper.optArray(f, "history_7day");
        String kind = LocalUpdatesConfigHelper.optString(f, "history_kind", "");
        boolean useStored = stored.length() > 0 && stored.length() <= 3
                && !"estimated".equalsIgnoreCase(kind)
                && !f.optBoolean("history_is_estimated", false);
        if (useStored && !isFlatFakeWeek(stored)) {
            return stored;
        }
        return buildTodayVsYesterday(petrol, diesel, petrolCh, dieselCh);
    }

    private static boolean isFlatFakeWeek(@NonNull JSONArray history) {
        if (history.length() < 4) return false;
        // Legacy 7-day invention / flat mock → reject
        return true;
    }

    @NonNull
    private static JSONArray buildTodayVsYesterday(double petrol, double diesel,
                                                   double petrolCh, double dieselCh) {
        JSONArray out = new JSONArray();
        if (!(petrol > 0) || !(diesel > 0)) return out;
        try {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
            cal.add(java.util.Calendar.DAY_OF_YEAR, -1);
            double prevP = petrol - petrolCh;
            double prevD = diesel - dieselCh;
            if (prevP > 0 && prevD > 0) {
                JSONObject y = new JSONObject();
                y.put("date_label", months[cal.get(java.util.Calendar.MONTH)]
                        + " " + cal.get(java.util.Calendar.DAY_OF_MONTH));
                y.put("petrol", Math.round(prevP * 100.0) / 100.0);
                y.put("diesel", Math.round(prevD * 100.0) / 100.0);
                out.put(y);
            }
            cal.add(java.util.Calendar.DAY_OF_YEAR, 1);
            JSONObject t = new JSONObject();
            t.put("date_label", months[cal.get(java.util.Calendar.MONTH)]
                    + " " + cal.get(java.util.Calendar.DAY_OF_MONTH));
            t.put("petrol", Math.round(petrol * 100.0) / 100.0);
            t.put("diesel", Math.round(diesel * 100.0) / 100.0);
            out.put(t);
        } catch (Exception ignored) {
        }
        return out;
    }

    private void bindPromoFromDeltas(double petrolCh, double dieselCh) {
        boolean pDown = petrolCh < -0.001;
        boolean pUp = petrolCh > 0.001;
        boolean dDown = dieselCh < -0.001;
        boolean dUp = dieselCh > 0.001;
        String title;
        String text;
        if (pDown && dDown) {
            title = "ధరలు తగ్గాయి!";
            text = "ఈరోజు పెట్రోల్ (▼"
                    + LocalUpdatesConfigHelper.formatMoney2(Math.abs(petrolCh))
                    + ") & డీజిల్ (▼"
                    + LocalUpdatesConfigHelper.formatMoney2(Math.abs(dieselCh))
                    + ") తగ్గాయి. లాభం పొందండి 🙂";
        } else if (pUp && dUp) {
            title = "ధరలు పెరిగాయి!";
            text = "ఈరోజు పెట్రోల్ (▲"
                    + LocalUpdatesConfigHelper.formatMoney2(Math.abs(petrolCh))
                    + ") & డీజిల్ (▲"
                    + LocalUpdatesConfigHelper.formatMoney2(Math.abs(dieselCh))
                    + ") పెరిగాయి.";
        } else if (pDown && dUp) {
            title = "మిశ్రమ మార్పు";
            text = "పెట్రోల్ తగ్గింది (▼"
                    + LocalUpdatesConfigHelper.formatMoney2(Math.abs(petrolCh))
                    + "), డీజిల్ పెరిగింది (▲"
                    + LocalUpdatesConfigHelper.formatMoney2(Math.abs(dieselCh))
                    + ").";
        } else if (pUp && dDown) {
            title = "మిశ్రమ మార్పు";
            text = "పెట్రోల్ పెరిగింది (▲"
                    + LocalUpdatesConfigHelper.formatMoney2(Math.abs(petrolCh))
                    + "), డీజిల్ తగ్గింది (▼"
                    + LocalUpdatesConfigHelper.formatMoney2(Math.abs(dieselCh))
                    + ").";
        } else if (pDown) {
            title = "పెట్రోల్ తగ్గింది";
            text = "ఈరోజు పెట్రోల్ తగ్గింది (▼"
                    + LocalUpdatesConfigHelper.formatMoney2(Math.abs(petrolCh))
                    + "). డీజిల్ మారలేదు.";
        } else if (pUp) {
            title = "పెట్రోల్ పెరిగింది";
            text = "ఈరోజు పెట్రోల్ పెరిగింది (▲"
                    + LocalUpdatesConfigHelper.formatMoney2(Math.abs(petrolCh))
                    + "). డీజిల్ మారలేదు.";
        } else if (dDown) {
            title = "డీజిల్ తగ్గింది";
            text = "ఈరోజు డీజిల్ తగ్గింది (▼"
                    + LocalUpdatesConfigHelper.formatMoney2(Math.abs(dieselCh))
                    + "). పెట్రోల్ మారలేదు.";
        } else if (dUp) {
            title = "డీజిల్ పెరిగింది";
            text = "ఈరోజు డీజిల్ పెరిగింది (▲"
                    + LocalUpdatesConfigHelper.formatMoney2(Math.abs(dieselCh))
                    + "). పెట్రోల్ మారలేదు.";
        } else {
            title = "ధరలు మారలేదు";
            text = "ఈరోజు పెట్రోల్ & డీజిల్ ధరలు నిన్నటితో సమానం.";
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
            LinearLayout line = new LinearLayout(this);
            line.setOrientation(LinearLayout.HORIZONTAL);
            line.setPadding(0, (int) (6 * d), 0, (int) (6 * d));
            line.setGravity(Gravity.CENTER_VERTICAL);
            boolean selected = !TextUtils.isEmpty(selectedCity)
                    && selectedCity.equals(cityName);
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
            line.setOnClickListener(v -> selectCity(cityName));
            list.addView(line);
        }
    }

    private void selectCity(@Nullable String cityName) {
        selectedCity = cityName;
        if (activeFuel != null) bindUi(activeFuel);
        Toast.makeText(this,
                TextUtils.isEmpty(cityName) ? "రాష్ట్ర సగటు" : cityName,
                Toast.LENGTH_SHORT).show();
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
                String body = "ఇంధన ధరలు — "
                        + (where.isEmpty() ? "" : (where + " — "))
                        + "పెట్రోల్ ₹" + (petrol > 0
                        ? LocalUpdatesConfigHelper.formatMoney2(petrol) : "—")
                        + ", డీజిల్ ₹" + (diesel > 0
                        ? LocalUpdatesConfigHelper.formatMoney2(diesel) : "—");
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

    /** States in splash {@code by_state} (or root) that have real petrol+diesel prices. */
    private int countStatesWithData() {
        return statesWithData().size();
    }

    @NonNull
    private java.util.List<String[]> statesWithData() {
        JSONObject root = LocalUpdatesConfigHelper.fuelJson(this);
        if (root == null) root = new JSONObject();
        JSONObject byState = root.optJSONObject("by_state");
        if (byState == null) byState = new JSONObject();

        String[] keys = {KEY_AP, KEY_TG};
        String[] labels = {"ఆంధ్రప్రదేశ్", "తెలంగాణ"};
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
        if (title != null) title.setText("రాష్ట్రం ఎంచుకోండి");
        TextView sub = content.findViewById(R.id.fuel_picker_subtitle);
        if (sub != null) sub.setVisibility(View.GONE);
        LinearLayout list = content.findViewById(R.id.fuel_picker_list);
        float dens = getResources().getDisplayMetrics().density;
        for (int i = 0; i < available.size(); i++) {
            final String key = available.get(i)[0];
            final String label = available.get(i)[1];
            JSONObject snap = byState.optJSONObject(key);

            LinearLayout row = new LinearLayout(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            if (i > 0) lp.topMargin = (int) (8 * dens);
            row.setLayoutParams(lp);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setBackgroundResource(R.drawable.bg_local_update_card);
            row.setPadding((int) (14 * dens), (int) (12 * dens), (int) (14 * dens), (int) (12 * dens));

            TextView name = new TextView(this);
            name.setLayoutParams(new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            name.setText(label + (key.equals(selectedStateKey) ? "  ✓" : ""));
            name.setTextColor(0xFFFFFFFF);
            name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            name.setTypeface(name.getTypeface(), android.graphics.Typeface.BOLD);
            row.addView(name);

            TextView meta = new TextView(this);
            double p = 0;
            if (snap != null) p = LocalUpdatesConfigHelper.optNumber(snap, "petrol_price", 0);
            if (!(p > 0) && key.equals(resolveStateKey(root))) {
                p = LocalUpdatesConfigHelper.optNumber(root, "petrol_price", 0);
            }
            meta.setText(p > 0 ? ("₹" + LocalUpdatesConfigHelper.formatMoney2(p)) : "");
            meta.setTextColor(0xFF94A3B8);
            meta.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            row.addView(meta);

            row.setOnClickListener(v -> {
                sheet.dismiss();
                applyStateSelection(key);
            });
            list.addView(row);
        }
        sheet.setContentView(content);
        sheet.show();
    }

    private void showCityPicker() {
        JSONObject f = activeFuel != null ? activeFuel : new JSONObject();
        JSONArray cities = LocalUpdatesConfigHelper.optArray(f, "cities");
        if (cities.length() == 0) {
            Toast.makeText(this, "నగర ధరలు లేవు", Toast.LENGTH_SHORT).show();
            return;
        }
        BottomSheetDialog sheet = new BottomSheetDialog(this);
        View content = getLayoutInflater().inflate(R.layout.bottom_sheet_fuel_picker, null);
        TextView title = content.findViewById(R.id.fuel_picker_title);
        if (title != null) title.setText("నగరం ఎంచుకోండి");
        TextView sub = content.findViewById(R.id.fuel_picker_subtitle);
        if (sub != null) {
            sub.setVisibility(View.VISIBLE);
            sub.setText(stateLabelForKey(selectedStateKey, f) + " — హీరో ధరలు నవీకరించబడతాయి");
        }
        LinearLayout list = content.findViewById(R.id.fuel_picker_list);
        float dens = getResources().getDisplayMetrics().density;

        // Option: state average / clear city override
        list.addView(pickerRow("రాష్ట్ర సగటు / ప్రాథమిక",
                heroPriceLabel(f), TextUtils.isEmpty(selectedCity), dens, () -> {
                    sheet.dismiss();
                    selectedCity = null;
                    bindUi(activeFuel);
                    scrollToCities();
                }));

        for (int i = 0; i < cities.length(); i++) {
            JSONObject row = cities.optJSONObject(i);
            if (row == null) continue;
            double p = LocalUpdatesConfigHelper.optNumber(row, "petrol", 0);
            double di = LocalUpdatesConfigHelper.optNumber(row, "diesel", 0);
            if (!(p > 0) || !(di > 0)) continue;
            final String cityName = LocalUpdatesConfigHelper.optString(row, "city", "");
            String price = "P ₹" + LocalUpdatesConfigHelper.formatMoney2(p)
                    + "  D ₹" + LocalUpdatesConfigHelper.formatMoney2(di);
            boolean sel = cityName.equals(selectedCity);
            list.addView(pickerRow(cityName, price, sel, dens, () -> {
                sheet.dismiss();
                selectCity(cityName);
                scrollToCities();
            }));
        }
        sheet.setContentView(content);
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
        row.setPadding((int) (14 * dens), (int) (12 * dens), (int) (14 * dens), (int) (12 * dens));

        TextView nameTv = new TextView(this);
        nameTv.setLayoutParams(new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        nameTv.setText(name + (selected ? "  ✓" : ""));
        nameTv.setTextColor(0xFFFFFFFF);
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
        JSONObject root = LocalUpdatesConfigHelper.fuelJson(this);
        if (root == null) root = new JSONObject();
        selectedStateKey = key;
        selectedCity = null;
        activeFuel = overlayState(root, key);
        bindUi(activeFuel);
    }

    /**
     * Merge {@code by_state[key]} onto the splash root so chrome / cities / hero refresh.
     */
    @NonNull
    private JSONObject overlayState(@NonNull JSONObject root, @NonNull String key) {
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
    private static String stateLabelForKey(@NonNull String key, @Nullable JSONObject f) {
        if (KEY_TG.equals(key)) return "తెలంగాణ";
        if (KEY_AP.equals(key)) return "ఆంధ్రప్రదేశ్";
        if (f != null) {
            String s = LocalUpdatesConfigHelper.optString(f, "state_filter", "");
            if (!TextUtils.isEmpty(s)) return s.replace("▾", "").replace("▼", "").trim();
            s = LocalUpdatesConfigHelper.optString(f, "location_region", "");
            if (!TextUtils.isEmpty(s)) return s;
        }
        return "ఆంధ్రప్రదేశ్";
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
}
