package com.anyNews.anynews.Activities;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;

import com.anyNews.anynews.R;
import com.anyNews.anynews.Ui.GoldHistoryChartView;
import com.anyNews.anynews.Utils.DailyUpdatesShareLanguage;
import com.anyNews.anynews.Utils.DailyUpdatesShareLinks;
import com.anyNews.anynews.Utils.GoldLabels;
import com.anyNews.anynews.Utils.LocalUpdatesConfigHelper;
import com.bumptech.glide.Glide;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;

/**
 * Gold &amp; Silver detail — premium dark mock (gold/silver cards, date-only bar).
 * Rates from splash {@code root_settings.gold} (IBJA via admin). Never invent stub ₹.
 * Date chip opens a picker; tables refresh from cached {@code history} when present.
 */
public class GoldDetailActivity extends AppCompatActivity {

    @Nullable
    private static WeakReference<GoldDetailActivity> openRef;

    private static final int COLOR_GOLD = 0xFFD4AF37;
    private static final int COLOR_GOLD_TEXT = 0xFFE8D48A;
    private static final int COLOR_SILVER = 0xFFA5B1C2;
    private static final int COLOR_WHITE = 0xFFFFFFFF;
    private static final int COLOR_MUTED = 0xFF94A3B8;
    private static final int COLOR_UP = 0xFF34D399;
    private static final int COLOR_DOWN = 0xFFF87171;

    private static final int[] GOLD_WEIGHTS_G = {1, 8, 10, 100, 1000};
    private static final int[] SILVER_WEIGHTS_G = {1, 10, 100, 1000};

    private static final TimeZone TZ_IST = TimeZone.getTimeZone("Asia/Kolkata");

    private int goldWeightG = 1;
    private int silverWeightG = 1;
    @Nullable
    private JSONObject goldData;
    /** Full splash gold blob (unchanged) — used for history + live rates. */
    @Nullable
    private JSONObject goldSource;
    private boolean ratesAvailable;
    private boolean goldChartOpen;
    private boolean silverChartOpen;

    /** Selected calendar day (IST), yyyy-MM-dd. */
    private String selectedDateIso = "";
    /** Display rates reconstructed for selectedDateIso (or live). */
    @Nullable
    private JSONObject displayGold;
    private boolean displayHasRates;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Gold rates are national (same numbers for every LanguageID). Language only
        // localizes chrome via GoldLabels — never reject a pack that already shows on the hub.
        if (!LocalUpdatesConfigHelper.isGoldEnabled(this)
                && !LocalUpdatesConfigHelper.hasRealGoldRates(this)) {
            Toast.makeText(this, R.string.gold_disabled, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        DailyUpdatesShareLinks.beginLanguage(this);
        try {
            setContentView(R.layout.activity_gold_detail);
        } catch (Throwable t) {
            android.util.Log.e("GoldDetail", "inflate failed", t);
            Toast.makeText(this, GoldLabels.ratesUnavailable(this), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        try {
            applySystemBars(R.id.gold_detail_root);
            goldSource = LocalUpdatesConfigHelper.goldJson(this);
            if (goldSource == null) goldSource = new JSONObject();
            goldData = goldSource;
            ratesAvailable = LocalUpdatesConfigHelper.hasRealGoldRates(this);
            selectedDateIso = resolveDefaultDateIso(goldSource);
            if (selectedDateIso == null || selectedDateIso.isEmpty()) {
                selectedDateIso = todayIsoIst();
            }
            applyRatesForSelectedDate();
            // Hub already showed numbers → never leave the detail on an empty table.
            if (!displayHasRates && ratesAvailable) {
                displayGold = goldSource;
                displayHasRates = true;
                goldData = displayGold;
            }
            bindStatic();
            buildGoldTabs();
            buildSilverTabs();
            refreshTables();
            bindCharts();
            wireClicks();
        } catch (Throwable t) {
            // Bind/layout faults must not masquerade as "rates unavailable" when
            // the splash pack already has IBJA numbers (ClassCast on chart CTA, etc.).
            android.util.Log.e("GoldDetail", "bind failed", t);
            if (!ratesAvailable && !LocalUpdatesConfigHelper.hasRealGoldRates(this)) {
                Toast.makeText(this, GoldLabels.ratesUnavailable(this), Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        openRef = new WeakReference<>(this);
    }

    @Override
    protected void onStop() {
        GoldDetailActivity cur = openRef != null ? openRef.get() : null;
        if (cur == this) openRef = null;
        super.onStop();
    }

    /**
     * Feed response replaced {@code gold_config_json}. Repaint if this screen
     * is open. Keeps the selected 10g / 1g tab.
     */
    public static void onGoldPackUpdated() {
        GoldDetailActivity activity = openRef != null ? openRef.get() : null;
        if (activity == null || activity.isFinishing()) return;
        activity.runOnUiThread(() -> {
            if (activity.isFinishing()) return;
            activity.reloadFromSavedPack();
        });
    }

    private void reloadFromSavedPack() {
        String previousLive = goldSource == null
                ? ""
                : LocalUpdatesConfigHelper.optString(goldSource, "rate_date", "");
        boolean wasOnLive = !isIsoDate(previousLive) || previousLive.equals(selectedDateIso);
        JSONObject next = LocalUpdatesConfigHelper.goldJson(this);
        if (next == null) return;
        goldSource = next;
        ratesAvailable = LocalUpdatesConfigHelper.hasRealGoldRates(this);
        if (wasOnLive) {
            String live = resolveDefaultDateIso(goldSource);
            if (isIsoDate(live)) selectedDateIso = live;
        }
        applyRatesForSelectedDate();
        if (!displayHasRates && ratesAvailable) {
            displayGold = goldSource;
            displayHasRates = true;
            goldData = displayGold;
        }
        bindStatic();
        styleGoldTabs();
        styleSilverTabs();
        refreshTables();
        bindCharts();
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

    private void bindStatic() {
        JSONObject g = goldSource != null ? goldSource : new JSONObject();
        // Always LanguageID chrome — splash Telugu strings must never stick on HI/ML/etc.
        setText(R.id.gold_top_title, GoldLabels.title(this));
        setText(R.id.gold_date_label, GoldLabels.dateLabel(this));
        setText(R.id.gold_date_value, formatDateLabel(selectedDateIso));
        setText(R.id.gold_disclaimer, GoldLabels.disclaimer(this));
        setText(R.id.gold_card_title, GoldLabels.cardTitle(this));
        setText(R.id.silver_card_title, GoldLabels.silverCardTitle(this));
        setText(R.id.gold_col_details, GoldLabels.colDetails(this));
        setText(R.id.gold_col_price, GoldLabels.colPrice(this));
        setText(R.id.gold_col_change, GoldLabels.colChange(this));
        setText(R.id.silver_col_details, GoldLabels.colDetails(this));
        setText(R.id.silver_col_price, GoldLabels.colPrice(this));
        setText(R.id.silver_col_change, GoldLabels.colChange(this));
        // Chart CTAs are LinearLayouts with a nested TextView — never cast the root.
        setLabelOnViewOrChild(R.id.gold_chart_cta, GoldLabels.chartCta(this));
        setLabelOnViewOrChild(R.id.silver_chart_cta, GoldLabels.chartCta(this));
        setText(R.id.gold_chart_empty, GoldLabels.chartEmpty(this));
        setText(R.id.silver_chart_empty, GoldLabels.chartEmpty(this));

        View source = findViewById(R.id.gold_source);
        if (source != null) source.setVisibility(View.GONE);

        ImageView banner = findViewById(R.id.gold_hero_banner);
        if (banner != null) {
            String hero = LocalUpdatesConfigHelper.optString(g, "hero_image_url", "");
            if (hero.startsWith("http://") || hero.startsWith("https://")) {
                Glide.with(this).load(hero).centerCrop()
                        .placeholder(R.drawable.gold_silver_hero_banner)
                        .into(banner);
            } else {
                banner.setImageResource(R.drawable.gold_silver_hero_banner);
            }
        }
    }

    // ─── Date selection ───────────────────────────────────────────────────────

    private String resolveDefaultDateIso(JSONObject g) {
        String rateDate = LocalUpdatesConfigHelper.optString(g, "rate_date", "");
        if (isIsoDate(rateDate)) return rateDate;
        JSONArray hist = fullHistory(g);
        for (int i = hist.length() - 1; i >= 0; i--) {
            JSONObject row = hist.optJSONObject(i);
            if (row == null) continue;
            String d = LocalUpdatesConfigHelper.optString(row, "date", "");
            if (isIsoDate(d) && LocalUpdatesConfigHelper.optNumber(row, "gold_24k", 0) > 0) {
                return d;
            }
        }
        String fromFetched = isoFromFetchedAt(LocalUpdatesConfigHelper.optString(g, "fetched_at", ""));
        if (isIsoDate(fromFetched)) return fromFetched;
        return todayIsoIst();
    }

    private void applyRatesForSelectedDate() {
        JSONObject src = goldSource != null ? goldSource : new JSONObject();
        String rateDate = LocalUpdatesConfigHelper.optString(src, "rate_date", "");

        // Prefer live purity_rows (full 18K/14K) when the selected day is the
        // published rate_date from the last IBJA fetch.
        if (ratesAvailable && isIsoDate(rateDate) && rateDate.equals(selectedDateIso)) {
            displayGold = src;
            displayHasRates = true;
            goldData = displayGold;
            return;
        }

        JSONArray hist = fullHistory(src);
        int idx = findHistoryIndex(hist, selectedDateIso);
        if (idx >= 0) {
            JSONObject day = hist.optJSONObject(idx);
            JSONObject prev = idx > 0 ? hist.optJSONObject(idx - 1) : null;
            displayGold = buildDisplayFromHistoryDay(src, day, prev);
            displayHasRates = day != null && (
                    LocalUpdatesConfigHelper.optNumber(day, "gold_24k", 0) > 0
                            || LocalUpdatesConfigHelper.optNumber(day, "silver_kg", 0) > 0);
            goldData = displayGold;
            return;
        }

        // No rate_date on older payloads: allow live rates only for fetch day.
        if (ratesAvailable && !isIsoDate(rateDate)) {
            String fetchedIso = isoFromFetchedAt(
                    LocalUpdatesConfigHelper.optString(src, "fetched_at", ""));
            if (isIsoDate(fetchedIso) && fetchedIso.equals(selectedDateIso)) {
                displayGold = src;
                displayHasRates = true;
                goldData = displayGold;
                return;
            }
        }

        // National live pack fallback: show splash IBJA numbers when the selected
        // day is the published rate day / fetch day / calendar-today (weekend
        // carry-forward). Unknown history days stay empty (date picker toast).
        displayGold = src;
        if (ratesAvailable) {
            String liveDay = isIsoDate(rateDate) ? rateDate
                    : isoFromFetchedAt(LocalUpdatesConfigHelper.optString(src, "fetched_at", ""));
            String today = todayIsoIst();
            displayHasRates = selectedDateIso.equals(today)
                    || (isIsoDate(liveDay) && selectedDateIso.equals(liveDay));
        } else {
            displayHasRates = false;
        }
        goldData = displayGold;
    }

    private JSONObject buildDisplayFromHistoryDay(JSONObject src, JSONObject day,
                                                  @Nullable JSONObject prev) {
        JSONObject out = new JSONObject();
        try {
            // Copy chrome / labels from source.
            copyString(src, out, "header_title");
            copyString(src, out, "disclaimer");
            copyString(src, out, "location_name");
            copyString(src, out, "hero_image_url");
            copyString(src, out, "fetch_source");
            copyString(src, out, "source_url");
            out.put("rate_date", LocalUpdatesConfigHelper.optString(day, "date", selectedDateIso));
            out.put("history", fullHistory(src));
            out.put("history_7day", LocalUpdatesConfigHelper.optArray(src, "history_7day"));

            double g24_10 = LocalUpdatesConfigHelper.optNumber(day, "gold_24k", 0);
            double g22_10 = LocalUpdatesConfigHelper.optNumber(day, "gold_22k", 0);
            if (!(g22_10 > 0) && g24_10 > 0) g22_10 = g24_10 * 0.916;
            double prev24 = prev != null
                    ? LocalUpdatesConfigHelper.optNumber(prev, "gold_24k", Double.NaN) : Double.NaN;
            double ch24 = (g24_10 > 0 && Double.isFinite(prev24) && prev24 > 0)
                    ? g24_10 - prev24 : Double.NaN;

            out.put("gold_24k_10g", g24_10);
            out.put("gold_24k_1g", g24_10 > 0 ? g24_10 / 10.0 : 0);
            if (Double.isFinite(ch24)) {
                out.put("gold_24k_change", ch24);
                out.put("daily_change_24k", ch24);
                out.put("yesterday_price_24k", prev24);
            }

            JSONArray purity = new JSONArray();
            boolean joyalukkas = "joyalukkas".equalsIgnoreCase(
                    LocalUpdatesConfigHelper.optString(src, "fetch_source", ""));
            if (joyalukkas) {
                // Saved Joyalukkas days quote each purity. A missing karat stays
                // off the table — do not scale it from 24K.
                if (g24_10 > 0) {
                    purity.put(purityObj("24K", "99.9%", g24_10, ch24));
                }
                double g22 = LocalUpdatesConfigHelper.optNumber(day, "gold_22k", 0);
                double prev22 = prev != null
                        ? LocalUpdatesConfigHelper.optNumber(prev, "gold_22k", Double.NaN)
                        : Double.NaN;
                if (g22 > 0) {
                    double ch22 = (Double.isFinite(prev22) && prev22 > 0) ? g22 - prev22 : Double.NaN;
                    purity.put(purityObj("22K", "91.6%", g22, ch22));
                }
                double g18 = LocalUpdatesConfigHelper.optNumber(day, "gold_18k", 0);
                double prev18 = prev != null
                        ? LocalUpdatesConfigHelper.optNumber(prev, "gold_18k", Double.NaN)
                        : Double.NaN;
                if (g18 > 0) {
                    double ch18 = (Double.isFinite(prev18) && prev18 > 0) ? g18 - prev18 : Double.NaN;
                    purity.put(purityObj("18K", "75.0%", g18, ch18));
                }
                double g14 = LocalUpdatesConfigHelper.optNumber(day, "gold_14k", 0);
                double prev14 = prev != null
                        ? LocalUpdatesConfigHelper.optNumber(prev, "gold_14k", Double.NaN)
                        : Double.NaN;
                if (g14 > 0) {
                    double ch14 = (Double.isFinite(prev14) && prev14 > 0) ? g14 - prev14 : Double.NaN;
                    purity.put(purityObj("14K", "58.5%", g14, ch14));
                }
            } else if (g24_10 > 0) {
                purity.put(purityObj("24K", "99.9%", g24_10, ch24));
                purity.put(purityObj("22K", "91.6%", g22_10,
                        Double.isFinite(ch24) ? ch24 * 0.916 : Double.NaN));
                purity.put(purityObj("18K", "75.0%", g24_10 * 0.75,
                        Double.isFinite(ch24) ? ch24 * 0.75 : Double.NaN));
                purity.put(purityObj("14K", "58.5%", g24_10 * 0.585,
                        Double.isFinite(ch24) ? ch24 * 0.585 : Double.NaN));
            }
            out.put("purity_rows", purity);

            double skg = LocalUpdatesConfigHelper.optNumber(day, "silver_kg", 0);
            double prevS = prev != null
                    ? LocalUpdatesConfigHelper.optNumber(prev, "silver_kg", Double.NaN) : Double.NaN;
            if (skg > 0) {
                out.put("silver_kg", skg);
                out.put("silver_10g", skg / 100.0);
                out.put("silver_1g", skg / 1000.0);
                out.put("silver_100g", skg / 10.0);
                if (Double.isFinite(prevS) && prevS > 0) {
                    out.put("silver_kg_change", skg - prevS);
                    out.put("silver_change", (skg - prevS) / 100.0);
                    out.put("yesterday_silver_kg", prevS);
                }
            }
            out.put("rates_available", g24_10 > 0 || skg > 0);
        } catch (Exception ignored) {
        }
        return out;
    }

    private static JSONObject purityObj(String karat, String purity, double price10,
                                        double change10) {
        JSONObject o = new JSONObject();
        try {
            o.put("karat", karat);
            o.put("purity", purity);
            o.put("price_10g", price10);
            o.put("price_1g", price10 / 10.0);
            if (Double.isFinite(change10)) o.put("change", change10);
        } catch (Exception ignored) {
        }
        return o;
    }

    private static void copyString(JSONObject from, JSONObject to, String key) {
        try {
            String v = from.optString(key, "");
            if (!v.isEmpty()) to.put(key, v);
        } catch (Exception ignored) {
        }
    }

    private static final String TAG_GOLD_DATE_PICKER = "gold_history_date";

    private void openDatePicker() {
        final TreeSet<String> available = availableHistoryDates(goldSource);
        if (available.isEmpty()) {
            Toast.makeText(this, GoldLabels.dateNoData(this), Toast.LENGTH_SHORT).show();
            return;
        }

        String initial = selectedDateIso;
        if (!available.contains(initial)) {
            initial = nearestAvailableDate(initial, available);
        }

        // Drop a stale Material picker so re-open never throws "Fragment already added"
        // or leaves a half-attached dialog that can finish this activity on back.
        try {
            Fragment existing = getSupportFragmentManager().findFragmentByTag(TAG_GOLD_DATE_PICKER);
            if (existing instanceof MaterialDatePicker) {
                ((MaterialDatePicker<?>) existing).dismissAllowingStateLoss();
                getSupportFragmentManager().executePendingTransactions();
            }
        } catch (Exception ignored) {
        }

        try {
            showMaterialHistoryPicker(available, initial);
        } catch (Exception e) {
            // AppCompat host / theme gaps used to crash here and pop Gold → Home/News.
            openDatePickerFallback(available, initial);
        }
    }

    /**
     * Material calendar with {@link AvailableDatesValidator} so IBJA non-trading days
     * (weekends/holidays) stay dimmed. Requires {@code GoldDetailTheme} (Material Bridge).
     */
    private void showMaterialHistoryPicker(@NonNull TreeSet<String> available,
                                           @NonNull String initial) {
        // Material calendar uses UTC midnight per civil date (not IST wall-clock).
        Set<Long> validUtc = new HashSet<>();
        for (String iso : available) {
            long utc = isoToUtcMidnightMs(iso);
            if (utc > 0) validUtc.add(utc);
        }
        long startMs = isoToUtcMidnightMs(available.first());
        long endMs = isoToUtcMidnightMs(available.last());
        String todayIso = todayIsoIst();
        long todayUtc = isoToUtcMidnightMs(todayIso);
        if (todayUtc > 0 && endMs > todayUtc) endMs = todayUtc;
        long selectionMs = isoToUtcMidnightMs(initial);
        if (selectionMs <= 0 || !validUtc.contains(selectionMs)) {
            selectionMs = endMs;
        }

        CalendarConstraints constraints = new CalendarConstraints.Builder()
                .setStart(startMs)
                .setEnd(endMs)
                .setOpenAt(selectionMs)
                .setValidator(new AvailableDatesValidator(validUtc))
                .build();

        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(GoldLabels.dateLabel(this))
                .setSelection(selectionMs)
                .setCalendarConstraints(constraints)
                .setTheme(R.style.ThemeOverlay_AnyNews_GoldMaterialCalendar)
                .build();

        picker.addOnPositiveButtonClickListener(selection -> {
            if (selection == null) return;
            String picked = utcMsToIsoDate(selection);
            applyPickedIso(available.contains(picked) ? picked
                    : nearestAvailableDate(picked, available));
        });
        // Cancel / dismiss must stay on Gold & Silver — never finish() this activity.
        picker.addOnNegativeButtonClickListener(v -> { /* no-op */ });
        picker.addOnCancelListener(dialog -> { /* no-op */ });
        picker.addOnDismissListener(dialog -> { /* no-op */ });
        picker.show(getSupportFragmentManager(), TAG_GOLD_DATE_PICKER);
    }

    /** Pre-Material fallback: spinner + snap to nearest history day (no dimming). */
    private void openDatePickerFallback(@NonNull TreeSet<String> available,
                                        @NonNull String initial) {
        Calendar cal = Calendar.getInstance(TZ_IST);
        if (isIsoDate(initial)) {
            try {
                String[] p = initial.split("-");
                cal.set(Integer.parseInt(p[0]), Integer.parseInt(p[1]) - 1, Integer.parseInt(p[2]));
            } catch (Exception ignored) {
            }
        }

        final boolean[] snapping = {false};
        DatePickerDialog dlg = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String picked = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            applyPickedIso(available.contains(picked) ? picked
                    : nearestAvailableDate(picked, available));
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));

        long minMs = isoToStartOfDayMs(available.first());
        long maxMs = isoToEndOfDayMs(available.last());
        long todayEnd = Calendar.getInstance(TZ_IST).getTimeInMillis();
        if (maxMs > todayEnd) maxMs = todayEnd;
        if (minMs > 0) dlg.getDatePicker().setMinDate(minMs);
        if (maxMs > 0) dlg.getDatePicker().setMaxDate(maxMs);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            dlg.getDatePicker().setOnDateChangedListener((view, year, month, dayOfMonth) -> {
                if (snapping[0]) return;
                String picked = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                if (available.contains(picked)) return;
                String nearest = nearestAvailableDate(picked, available);
                if (!isIsoDate(nearest) || nearest.equals(picked)) return;
                try {
                    String[] p = nearest.split("-");
                    snapping[0] = true;
                    view.updateDate(Integer.parseInt(p[0]), Integer.parseInt(p[1]) - 1,
                            Integer.parseInt(p[2]));
                } catch (Exception ignored) {
                } finally {
                    snapping[0] = false;
                }
            });
        }
        dlg.setOnCancelListener(dialog -> { /* stay on Gold */ });
        dlg.show();
    }

    private void applyPickedIso(@NonNull String use) {
        selectedDateIso = use;
        setText(R.id.gold_date_value, formatDateLabel(selectedDateIso));
        applyRatesForSelectedDate();
        refreshTables();
        if (!displayHasRates) {
            Toast.makeText(this, GoldLabels.dateNoData(this), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * ISO dates that have gold/silver history, plus live {@code rate_date} /
     * {@code api_data_date} when rates exist.
     * <p>
     * IBJA does not publish Sat/Sun (or central holidays), so those days are
     * normally absent from history and appear dimmed/disabled in the picker.
     * If {@code rate_date} equals calendar-today on a weekend, that is our live
     * pack stamp (last trading-day prices), not an IBJA weekend print.
     */
    private TreeSet<String> availableHistoryDates(@Nullable JSONObject src) {
        TreeSet<String> out = new TreeSet<>();
        JSONObject g = src != null ? src : new JSONObject();
        JSONArray hist = fullHistory(g);
        for (int i = 0; i < hist.length(); i++) {
            JSONObject row = hist.optJSONObject(i);
            if (row == null) continue;
            if (!(LocalUpdatesConfigHelper.optNumber(row, "gold_24k", 0) > 0
                    || LocalUpdatesConfigHelper.optNumber(row, "silver_kg", 0) > 0)) {
                continue;
            }
            String d = LocalUpdatesConfigHelper.optString(row, "date", "");
            if (isIsoDate(d)) {
                out.add(d);
                continue;
            }
            // Older packs may only have "Aug 28" labels — resolve via year from neighbors/fetched_at.
            String lab = LocalUpdatesConfigHelper.optString(row, "date_label", "");
            String resolved = resolveLabelToIso(lab, g);
            if (isIsoDate(resolved)) out.add(resolved);
        }
        if (ratesAvailable) {
            String today = todayIsoIst();
            String rateDate = LocalUpdatesConfigHelper.optString(g, "rate_date", "");
            // Allow live rate_date always when it is a known history day, or when it is
            // calendar-today (may be a weekend carry-forward of Friday's live spans).
            if (isIsoDate(rateDate) && (rateDate.equals(today) || out.contains(rateDate)
                    || !isWeekendIso(rateDate))) {
                out.add(rateDate);
            }
            String apiDate = LocalUpdatesConfigHelper.optString(g, "api_data_date", "");
            if (isIsoDate(apiDate) && (apiDate.equals(today) || out.contains(apiDate)
                    || !isWeekendIso(apiDate))) {
                out.add(apiDate);
            }
        }
        return out;
    }

    /** True for Sat/Sun civil dates (IBJA non-publish days). */
    private static boolean isWeekendIso(String iso) {
        if (!isIsoDate(iso)) return false;
        try {
            String[] p = iso.split("-");
            Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            c.clear();
            c.set(Integer.parseInt(p[0]), Integer.parseInt(p[1]) - 1, Integer.parseInt(p[2]));
            int dow = c.get(Calendar.DAY_OF_WEEK);
            return dow == Calendar.SATURDAY || dow == Calendar.SUNDAY;
        } catch (Exception e) {
            return false;
        }
    }

    /** MaterialDatePicker day key: UTC midnight for yyyy-MM-dd. */
    private static long isoToUtcMidnightMs(String iso) {
        if (!isIsoDate(iso)) return 0;
        try {
            String[] p = iso.split("-");
            Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            c.clear();
            c.set(Integer.parseInt(p[0]), Integer.parseInt(p[1]) - 1, Integer.parseInt(p[2]));
            return c.getTimeInMillis();
        } catch (Exception e) {
            return 0;
        }
    }

    /** IST start-of-day for DatePickerDialog min/max bounds. */
    private static long isoToStartOfDayMs(String iso) {
        if (!isIsoDate(iso)) return 0;
        try {
            String[] p = iso.split("-");
            Calendar c = Calendar.getInstance(TZ_IST);
            c.clear();
            c.set(Integer.parseInt(p[0]), Integer.parseInt(p[1]) - 1, Integer.parseInt(p[2]),
                    0, 0, 0);
            c.set(Calendar.MILLISECOND, 0);
            return c.getTimeInMillis();
        } catch (Exception e) {
            return 0;
        }
    }

    /** IST end-of-day for DatePickerDialog max bound. */
    private static long isoToEndOfDayMs(String iso) {
        if (!isIsoDate(iso)) return 0;
        try {
            String[] p = iso.split("-");
            Calendar c = Calendar.getInstance(TZ_IST);
            c.clear();
            c.set(Integer.parseInt(p[0]), Integer.parseInt(p[1]) - 1, Integer.parseInt(p[2]),
                    23, 59, 59);
            c.set(Calendar.MILLISECOND, 999);
            return c.getTimeInMillis();
        } catch (Exception e) {
            return 0;
        }
    }

    private static String utcMsToIsoDate(long utcMs) {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        c.setTimeInMillis(utcMs);
        return String.format(Locale.US, "%04d-%02d-%02d",
                c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));
    }

    /**
     * Only ISO dates present in {@link #availableHistoryDates} validate; others are
     * dimmed and non-selectable (Way2-style disabled look via Material calendar).
     * Public so MaterialDatePicker can parcel/unparcel the validator reliably.
     */
    public static final class AvailableDatesValidator implements CalendarConstraints.DateValidator {
        private final HashSet<Long> validUtcDays;

        AvailableDatesValidator(Set<Long> validUtcDays) {
            this.validUtcDays = new HashSet<>(validUtcDays != null ? validUtcDays : new HashSet<>());
        }

        AvailableDatesValidator(Parcel in) {
            int n = in.readInt();
            validUtcDays = new HashSet<>(Math.max(n, 0));
            for (int i = 0; i < n; i++) validUtcDays.add(in.readLong());
        }

        @Override
        public boolean isValid(long date) {
            return validUtcDays.contains(date);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(@NonNull Parcel dest, int flags) {
            dest.writeInt(validUtcDays.size());
            for (Long d : validUtcDays) dest.writeLong(d != null ? d : 0L);
        }

        public static final Parcelable.Creator<AvailableDatesValidator> CREATOR =
                new Parcelable.Creator<AvailableDatesValidator>() {
                    @Override
                    public AvailableDatesValidator createFromParcel(Parcel in) {
                        return new AvailableDatesValidator(in);
                    }

                    @Override
                    public AvailableDatesValidator[] newArray(int size) {
                        return new AvailableDatesValidator[size];
                    }
                };
    }

    /** Prefer same-or-earlier trading day; otherwise next available. */
    private static String nearestAvailableDate(String iso, TreeSet<String> available) {
        if (available == null || available.isEmpty()) return iso;
        if (available.contains(iso)) return iso;
        String floor = available.floor(iso);
        if (floor != null) return floor;
        String ceil = available.ceiling(iso);
        return ceil != null ? ceil : available.last();
    }

    /** Best-effort "Aug 28" → yyyy-MM-dd using fetched_at / rate_date year. */
    private static String resolveLabelToIso(String label, JSONObject g) {
        if (label == null || label.isEmpty()) return "";
        try {
            SimpleDateFormat parse = new SimpleDateFormat("MMM d", Locale.US);
            parse.setLenient(true);
            Date d = parse.parse(label.trim());
            if (d == null) return "";
            Calendar parsed = Calendar.getInstance(TZ_IST);
            parsed.setTime(d);
            int year = Calendar.getInstance(TZ_IST).get(Calendar.YEAR);
            String rateDate = LocalUpdatesConfigHelper.optString(g, "rate_date", "");
            if (isIsoDate(rateDate)) {
                year = Integer.parseInt(rateDate.substring(0, 4));
            } else {
                String fetched = isoFromFetchedAt(
                        LocalUpdatesConfigHelper.optString(g, "fetched_at", ""));
                if (isIsoDate(fetched)) year = Integer.parseInt(fetched.substring(0, 4));
            }
            return String.format(Locale.US, "%04d-%02d-%02d",
                    year, parsed.get(Calendar.MONTH) + 1, parsed.get(Calendar.DAY_OF_MONTH));
        } catch (Exception e) {
            return "";
        }
    }

    // ─── Tabs ─────────────────────────────────────────────────────────────────

    private void buildGoldTabs() {
        LinearLayout tabs = findViewById(R.id.gold_weight_tabs);
        if (tabs == null) return;
        tabs.removeAllViews();
        String[] labels = {
                GoldLabels.weight(this, 1),
                GoldLabels.weight(this, 8),
                GoldLabels.weight(this, 10),
                GoldLabels.weight(this, 100),
                GoldLabels.weight(this, 1000)
        };
        float d = getResources().getDisplayMetrics().density;
        for (int i = 0; i < GOLD_WEIGHTS_G.length; i++) {
            final int grams = GOLD_WEIGHTS_G[i];
            TextView tab = new TextView(this);
            tab.setText(labels[i]);
            tab.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            tab.setTypeface(Typeface.DEFAULT_BOLD);
            tab.setGravity(Gravity.CENTER);
            tab.setPadding((int) (12 * d), (int) (8 * d), (int) (12 * d), (int) (8 * d));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            if (i > 0) lp.setMarginStart((int) (6 * d));
            tab.setLayoutParams(lp);
            tab.setOnClickListener(v -> {
                goldWeightG = grams;
                styleGoldTabs();
                refreshGoldTable();
                refreshGoldChart();
            });
            tabs.addView(tab);
        }
        styleGoldTabs();
    }

    private void styleGoldTabs() {
        LinearLayout tabs = findViewById(R.id.gold_weight_tabs);
        if (tabs == null) return;
        for (int i = 0; i < tabs.getChildCount(); i++) {
            View child = tabs.getChildAt(i);
            if (!(child instanceof TextView)) continue;
            TextView tv = (TextView) child;
            boolean sel = GOLD_WEIGHTS_G[i] == goldWeightG;
            tv.setBackgroundResource(sel ? R.drawable.bg_gold_tab_selected
                    : R.drawable.bg_gold_tab_idle);
            tv.setTextColor(sel ? 0xFF1A1200 : COLOR_GOLD_TEXT);
        }
    }

    private void buildSilverTabs() {
        LinearLayout tabs = findViewById(R.id.silver_weight_tabs);
        if (tabs == null) return;
        tabs.removeAllViews();
        String[] labels = {
                GoldLabels.weight(this, 1),
                GoldLabels.weight(this, 10),
                GoldLabels.weight(this, 100),
                GoldLabels.weight(this, 1000)
        };
        float d = getResources().getDisplayMetrics().density;
        for (int i = 0; i < SILVER_WEIGHTS_G.length; i++) {
            final int grams = SILVER_WEIGHTS_G[i];
            TextView tab = new TextView(this);
            tab.setText(labels[i]);
            tab.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            tab.setTypeface(Typeface.DEFAULT_BOLD);
            tab.setGravity(Gravity.CENTER);
            tab.setPadding((int) (12 * d), (int) (8 * d), (int) (12 * d), (int) (8 * d));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            if (i > 0) lp.setMarginStart((int) (6 * d));
            tab.setLayoutParams(lp);
            tab.setOnClickListener(v -> {
                silverWeightG = grams;
                styleSilverTabs();
                refreshSilverTable();
                refreshSilverChart();
            });
            tabs.addView(tab);
        }
        styleSilverTabs();
    }

    private void styleSilverTabs() {
        LinearLayout tabs = findViewById(R.id.silver_weight_tabs);
        if (tabs == null) return;
        for (int i = 0; i < tabs.getChildCount(); i++) {
            View child = tabs.getChildAt(i);
            if (!(child instanceof TextView)) continue;
            TextView tv = (TextView) child;
            boolean sel = SILVER_WEIGHTS_G[i] == silverWeightG;
            tv.setBackgroundResource(sel ? R.drawable.bg_silver_tab_selected
                    : R.drawable.bg_silver_tab_idle);
            tv.setTextColor(sel ? COLOR_WHITE : COLOR_SILVER);
        }
    }

    // ─── Tables ───────────────────────────────────────────────────────────────

    private void refreshTables() {
        refreshGoldTable();
        refreshSilverTable();
    }

    private void refreshGoldTable() {
        LinearLayout list = findViewById(R.id.gold_purity_list);
        if (list == null) return;
        list.removeAllViews();
        ArrayList<PurityRow> rows = collectGoldRows();
        if (rows.isEmpty()) {
            list.addView(emptyHintRow(true));
            return;
        }
        for (int i = 0; i < rows.size(); i++) {
            list.addView(priceRow(rows.get(i), true));
            if (i < rows.size() - 1) {
                list.addView(divider(true));
            }
        }
    }

    private void refreshSilverTable() {
        LinearLayout list = findViewById(R.id.silver_purity_list);
        if (list == null) return;
        list.removeAllViews();
        ArrayList<PurityRow> rows = collectSilverRows();
        if (rows.isEmpty()) {
            list.addView(emptyHintRow(false));
            return;
        }
        for (int i = 0; i < rows.size(); i++) {
            list.addView(priceRow(rows.get(i), false));
            if (i < rows.size() - 1) {
                list.addView(divider(false));
            }
        }
    }

    private ArrayList<PurityRow> collectGoldRows() {
        ArrayList<PurityRow> out = new ArrayList<>();
        if (!displayHasRates) return out;
        JSONObject g = goldData != null ? goldData : new JSONObject();
        JSONArray purity = LocalUpdatesConfigHelper.optArray(g, "purity_rows");
        if (purity.length() > 0) {
            for (int i = 0; i < purity.length(); i++) {
                JSONObject row = purity.optJSONObject(i);
                if (row == null) continue;
                double per1g = LocalUpdatesConfigHelper.optNumber(row, "price_1g", 0);
                if (!(per1g > 0)) {
                    double p10 = LocalUpdatesConfigHelper.optNumber(row, "price_10g", 0);
                    if (p10 > 0) per1g = p10 / 10.0;
                }
                if (!(per1g > 0)) continue;
                // change is ₹ per 10g from server → scale to selected weight
                double change10 = LocalUpdatesConfigHelper.optNumber(row, "change", Double.NaN);
                double change1g = Double.isFinite(change10) ? change10 / 10.0 : Double.NaN;
                String karat = LocalUpdatesConfigHelper.optString(row, "karat", "");
                String purityPct = LocalUpdatesConfigHelper.optString(row, "purity", "");
                String label = formatGoldLabel(karat, purityPct);
                out.add(new PurityRow(label, per1g * goldWeightG,
                        Double.isFinite(change1g) ? change1g * goldWeightG : Double.NaN));
            }
        }
        if (out.isEmpty()) {
            double g1 = LocalUpdatesConfigHelper.optNumber(g, "gold_24k_1g", 0);
            if (!(g1 > 0)) {
                double g10 = LocalUpdatesConfigHelper.optNumber(g, "gold_24k_10g", 0);
                if (g10 > 0) g1 = g10 / 10.0;
            }
            if (g1 > 0) {
                double ch10 = LocalUpdatesConfigHelper.optNumber(g, "gold_24k_change", Double.NaN);
                double ch1 = Double.isFinite(ch10) ? ch10 / 10.0 : Double.NaN;
                out.add(new PurityRow("24K (999)", g1 * goldWeightG,
                        Double.isFinite(ch1) ? ch1 * goldWeightG : Double.NaN));
            }
        }
        return out;
    }

    private ArrayList<PurityRow> collectSilverRows() {
        ArrayList<PurityRow> out = new ArrayList<>();
        if (!displayHasRates) return out;
        JSONObject g = goldData != null ? goldData : new JSONObject();
        double kg = LocalUpdatesConfigHelper.optNumber(g, "silver_kg", 0);
        if (!(kg > 0)) {
            double s10 = LocalUpdatesConfigHelper.optNumber(g, "silver_10g", 0);
            if (s10 > 0) kg = s10 * 100.0;
        }
        if (!(kg > 0)) {
            double s1 = LocalUpdatesConfigHelper.optNumber(g, "silver_1g", 0);
            if (s1 > 0) kg = s1 * 1000.0;
        }
        if (!(kg > 0)) return out;

        double per1g = kg / 1000.0;
        double kgChange = LocalUpdatesConfigHelper.optNumber(g, "silver_kg_change", Double.NaN);
        if (!Double.isFinite(kgChange)) {
            double s10ch = LocalUpdatesConfigHelper.optNumber(g, "silver_change", Double.NaN);
            if (Double.isFinite(s10ch)) kgChange = s10ch * 100.0;
        }
        double change1g = Double.isFinite(kgChange) ? kgChange / 1000.0 : Double.NaN;

        out.add(new PurityRow("999 (Pure Silver)", per1g * silverWeightG,
                Double.isFinite(change1g) ? change1g * silverWeightG : Double.NaN));
        out.add(new PurityRow("925 (Sterling Silver)", per1g * 0.925 * silverWeightG,
                Double.isFinite(change1g) ? change1g * 0.925 * silverWeightG : Double.NaN));
        return out;
    }

    private static String formatGoldLabel(String karat, String purity) {
        String k = karat != null ? karat.trim() : "";
        String p = purity != null ? purity.trim() : "";
        if (p.contains("%")) {
            try {
                double pct = Double.parseDouble(p.replace("%", "").trim());
                int code = (int) Math.round(pct * 10);
                return k + " (" + code + ")";
            } catch (Exception ignored) {
            }
        }
        if (!p.isEmpty()) return k + " (" + p + ")";
        return k;
    }

    private View priceRow(PurityRow row, boolean goldTheme) {
        float d = getResources().getDisplayMetrics().density;
        LinearLayout line = new LinearLayout(this);
        line.setOrientation(LinearLayout.HORIZONTAL);
        line.setGravity(Gravity.CENTER_VERTICAL);
        line.setPadding(0, (int) (10 * d), 0, (int) (10 * d));

        TextView details = new TextView(this);
        details.setText(row.label);
        details.setTextColor(COLOR_WHITE);
        details.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        details.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1.2f);
        details.setLayoutParams(dlp);
        line.addView(details);

        LinearLayout priceWrap = new LinearLayout(this);
        priceWrap.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams plp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        priceWrap.setLayoutParams(plp);
        if (row.price > 0 && Double.isFinite(row.price)) {
            TextView price = new TextView(this);
            price.setText("₹" + LocalUpdatesConfigHelper.formatInr(row.price));
            price.setTextColor(COLOR_WHITE);
            price.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            price.setTypeface(Typeface.DEFAULT_BOLD);
            price.setGravity(Gravity.CENTER);
            priceWrap.addView(price);
        } else {
            View ph = new View(this);
            LinearLayout.LayoutParams php = new LinearLayout.LayoutParams(
                    (int) (72 * d), (int) (10 * d));
            ph.setLayoutParams(php);
            ph.setBackgroundResource(R.drawable.bg_gold_price_placeholder);
            priceWrap.addView(ph);
        }
        line.addView(priceWrap);

        FrameTrend changeWrap = new FrameTrend(this);
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0.85f);
        changeWrap.setLayoutParams(clp);
        changeWrap.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        TextView changeTv = new TextView(this);
        changeTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        changeTv.setTypeface(Typeface.DEFAULT_BOLD);
        changeTv.setGravity(Gravity.END);
        if (Double.isFinite(row.change) && Math.abs(row.change) >= 0.5) {
            boolean up = row.change > 0;
            String arrow = up ? "▲" : "▼";
            changeTv.setText(arrow + " ₹" + LocalUpdatesConfigHelper.formatInr(Math.abs(row.change)));
            changeTv.setTextColor(up ? COLOR_UP : COLOR_DOWN);
        } else {
            changeTv.setText("—");
            changeTv.setTextColor(COLOR_MUTED);
        }
        changeWrap.addView(changeTv);
        line.addView(changeWrap);
        return line;
    }

    private static final class FrameTrend extends LinearLayout {
        FrameTrend(android.content.Context ctx) {
            super(ctx);
            setOrientation(HORIZONTAL);
        }
    }

    private View emptyHintRow(boolean goldTheme) {
        float d = getResources().getDisplayMetrics().density;
        LinearLayout line = new LinearLayout(this);
        line.setOrientation(LinearLayout.VERTICAL);
        line.setPadding(0, (int) (12 * d), 0, (int) (8 * d));
        TextView hint = new TextView(this);
        hint.setText(displayHasRates
                ? GoldLabels.ratesUnavailable(this)
                : GoldLabels.dateNoData(this));
        hint.setTextColor(COLOR_MUTED);
        hint.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        line.addView(hint);
        for (int i = 0; i < 3; i++) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, (int) (10 * d), 0, (int) (10 * d));
            TextView lab = new TextView(this);
            if (goldTheme) {
                String[] labs = {"24K (999)", "22K (916)", "18K (750)"};
                lab.setText(labs[i]);
            } else {
                String[] labs = {"999 (Pure Silver)", "925 (Sterling Silver)", ""};
                lab.setText(labs[i]);
                if (labs[i].isEmpty()) continue;
            }
            lab.setTextColor(COLOR_WHITE);
            lab.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            lab.setTypeface(Typeface.DEFAULT_BOLD);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 1.2f);
            lab.setLayoutParams(lp);
            row.addView(lab);
            LinearLayout mid = new LinearLayout(this);
            mid.setGravity(Gravity.CENTER);
            mid.setLayoutParams(new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            View ph = new View(this);
            ph.setLayoutParams(new LinearLayout.LayoutParams((int) (72 * d), (int) (10 * d)));
            ph.setBackgroundResource(R.drawable.bg_gold_price_placeholder);
            mid.addView(ph);
            row.addView(mid);
            View spacer = new View(this);
            spacer.setLayoutParams(new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 0.7f));
            row.addView(spacer);
            line.addView(row);
            if (i < 2) line.addView(divider(goldTheme));
        }
        return line;
    }

    private View divider(boolean goldTheme) {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                Math.max(1, (int) (getResources().getDisplayMetrics().density + 0.5f))));
        v.setBackgroundResource(goldTheme
                ? R.drawable.bg_gold_table_row_divider
                : R.drawable.bg_silver_table_row_divider);
        return v;
    }

    // ─── Charts ───────────────────────────────────────────────────────────────

    private void bindCharts() {
        refreshGoldChart();
        refreshSilverChart();
        updateChartEmptyHints();
    }

    /** Gold history base is ₹/10g (24K) — scale by selected weight tab. */
    private void refreshGoldChart() {
        JSONObject src = goldSource != null ? goldSource : new JSONObject();
        JSONArray hist = fullHistory(src);
        GoldHistoryChartView goldChart = findViewById(R.id.gold_history_chart);
        if (goldChart != null) {
            goldChart.setAccentColor(COLOR_GOLD);
            // Payload gold_24k is ₹ per 10g — scale before setHistory so series matches tab.
            goldChart.setPriceScale(goldWeightG / 10f);
            goldChart.setHistory(hist, GoldHistoryChartView.PRICE_KEY_GOLD);
        }
        String goldTitle = GoldLabels.historyTitleGold(this);
        setText(R.id.gold_chart_panel_title, goldTitle);
        updateChartEmptyHints();
    }

    /** Silver history base is ₹/kg — scale by selected weight tab. */
    private void refreshSilverChart() {
        JSONObject src = goldSource != null ? goldSource : new JSONObject();
        JSONArray hist = historyForSilverChart(src);
        GoldHistoryChartView silverChart = findViewById(R.id.silver_history_chart);
        if (silverChart != null) {
            silverChart.setAccentColor(COLOR_SILVER);
            // Payload silver_kg is ₹ per kg — scale before setHistory so series matches tab.
            silverChart.setPriceScale(silverWeightG / 1000f);
            silverChart.setHistory(hist, GoldHistoryChartView.PRICE_KEY_SILVER);
        }
        String silverTitle = GoldLabels.historyTitleSilver(this);
        setText(R.id.silver_chart_panel_title, silverTitle);
        updateChartEmptyHints();
    }

    /**
     * Older splash packs often ship {@code history_7day} with {@code gold_24k} only
     * (no {@code silver_kg}). Prefer full {@code history}; when still &lt;2 silver
     * points, backfill the last 1–2 days from live IBJA fields already on the
     * payload ({@code silver_kg}, {@code yesterday_silver_kg} / change) — never invent
     * a multi-day silver series. Full 7-day silver requires a fresh admin IBJA fetch
     * ({@code fetchGold.js} merges HdnSilver into history).
     */
    private JSONArray historyForSilverChart(JSONObject src) {
        JSONArray hist = fullHistory(src);
        if (countPositiveKey(hist, GoldHistoryChartView.PRICE_KEY_SILVER) >= 2) {
            return hist;
        }

        double skg = LocalUpdatesConfigHelper.optNumber(src, "silver_kg", 0);
        if (!(skg > 0)) {
            double s10 = LocalUpdatesConfigHelper.optNumber(src, "silver_10g", 0);
            if (s10 > 0) skg = s10 * 100.0;
        }
        if (!(skg > 0)) {
            double s1 = LocalUpdatesConfigHelper.optNumber(src, "silver_1g", 0);
            if (s1 > 0) skg = s1 * 1000.0;
        }
        double prev = LocalUpdatesConfigHelper.optNumber(src, "yesterday_silver_kg", 0);
        if (!(prev > 0)) {
            double ch = LocalUpdatesConfigHelper.optNumber(src, "silver_kg_change", Double.NaN);
            if (!(Double.isFinite(ch))) {
                double s10ch = LocalUpdatesConfigHelper.optNumber(src, "silver_change", Double.NaN);
                if (Double.isFinite(s10ch)) ch = s10ch * 100.0;
            }
            if (skg > 0 && Double.isFinite(ch)) prev = skg - ch;
        }
        if (!(skg > 0)) return hist;

        try {
            JSONArray out = new JSONArray();
            for (int i = 0; i < hist.length(); i++) {
                JSONObject row = hist.optJSONObject(i);
                if (row == null) continue;
                out.put(new JSONObject(row.toString()));
            }

            if (out.length() == 0) {
                if (prev > 0) {
                    JSONObject a = new JSONObject();
                    a.put("date_label", "Prev");
                    a.put("silver_kg", prev);
                    out.put(a);
                }
                JSONObject b = new JSONObject();
                b.put("date_label", formatDateLabel(
                        isIsoDate(selectedDateIso) ? selectedDateIso : todayIsoIst()));
                b.put("silver_kg", skg);
                out.put(b);
                return out;
            }

            JSONObject last = out.optJSONObject(out.length() - 1);
            if (last != null && !(last.optDouble("silver_kg", 0) > 0)) {
                last.put("silver_kg", skg);
            }
            if (prev > 0 && out.length() >= 2) {
                JSONObject prevRow = out.optJSONObject(out.length() - 2);
                if (prevRow != null && !(prevRow.optDouble("silver_kg", 0) > 0)) {
                    prevRow.put("silver_kg", prev);
                }
            } else if (prev > 0 && out.length() == 1) {
                JSONArray merged = new JSONArray();
                JSONObject a = new JSONObject();
                a.put("date_label", "Prev");
                a.put("silver_kg", prev);
                merged.put(a);
                merged.put(out.optJSONObject(0));
                return merged;
            }
            return out;
        } catch (Exception ignored) {
            return hist;
        }
    }

    private static int countPositiveKey(JSONArray hist, String key) {
        if (hist == null) return 0;
        int n = 0;
        for (int i = 0; i < hist.length(); i++) {
            JSONObject row = hist.optJSONObject(i);
            if (row == null) continue;
            if (row.optDouble(key, 0) > 0) n++;
        }
        return n;
    }

    private void updateChartEmptyHints() {
        GoldHistoryChartView goldChart = findViewById(R.id.gold_history_chart);
        View goldEmpty = findViewById(R.id.gold_chart_empty);
        if (goldChart != null && goldEmpty != null) {
            boolean ok = goldChart.hasEnoughPoints();
            goldChart.setVisibility(ok ? View.VISIBLE : View.GONE);
            goldEmpty.setVisibility(ok ? View.GONE : View.VISIBLE);
        }
        GoldHistoryChartView silverChart = findViewById(R.id.silver_history_chart);
        View silverEmpty = findViewById(R.id.silver_chart_empty);
        if (silverChart != null && silverEmpty != null) {
            boolean ok = silverChart.hasEnoughPoints();
            silverChart.setVisibility(ok ? View.VISIBLE : View.GONE);
            silverEmpty.setVisibility(ok ? View.GONE : View.VISIBLE);
        }
    }

    private void toggleGoldChart() {
        goldChartOpen = !goldChartOpen;
        View panel = findViewById(R.id.gold_chart_panel);
        if (panel != null) panel.setVisibility(goldChartOpen ? View.VISIBLE : View.GONE);
        ImageView chevron = findViewById(R.id.gold_chart_chevron);
        if (chevron != null) chevron.setRotation(goldChartOpen ? 90f : 0f);
        View cta = findViewById(R.id.gold_chart_cta);
        if (cta instanceof TextView) {
            ((TextView) cta).setText(goldChartOpen
                    ? GoldLabels.chartCtaHide(this)
                    : GoldLabels.chartCta(this));
        } else if (cta instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) cta;
            for (int i = 0; i < vg.getChildCount(); i++) {
                View child = vg.getChildAt(i);
                if (child instanceof TextView) {
                    ((TextView) child).setText(goldChartOpen
                            ? GoldLabels.chartCtaHide(this)
                            : GoldLabels.chartCta(this));
                    break;
                }
            }
        }
        if (goldChartOpen) updateChartEmptyHints();
    }

    private void toggleSilverChart() {
        silverChartOpen = !silverChartOpen;
        View panel = findViewById(R.id.silver_chart_panel);
        if (panel != null) panel.setVisibility(silverChartOpen ? View.VISIBLE : View.GONE);
        ImageView chevron = findViewById(R.id.silver_chart_chevron);
        if (chevron != null) chevron.setRotation(silverChartOpen ? 90f : 0f);
        View cta = findViewById(R.id.silver_chart_cta);
        if (cta instanceof TextView) {
            ((TextView) cta).setText(silverChartOpen
                    ? GoldLabels.chartCtaHide(this)
                    : GoldLabels.chartCta(this));
        } else if (cta instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) cta;
            for (int i = 0; i < vg.getChildCount(); i++) {
                View child = vg.getChildAt(i);
                if (child instanceof TextView) {
                    ((TextView) child).setText(silverChartOpen
                            ? GoldLabels.chartCtaHide(this)
                            : GoldLabels.chartCta(this));
                    break;
                }
            }
        }
        if (silverChartOpen) updateChartEmptyHints();
    }

    // ─── Clicks / share ───────────────────────────────────────────────────────

    @Override
    protected void onDestroy() {
        DailyUpdatesShareLanguage.end();
        super.onDestroy();
    }

    private void wireClicks() {
        View back = findViewById(R.id.gold_back);
        if (back != null) back.setOnClickListener(v -> finish());
        View shareBtn = findViewById(R.id.gold_share);
        if (shareBtn != null) shareBtn.setOnClickListener(v -> sharePrices());
        View dateChip = findViewById(R.id.gold_date_chip);
        if (dateChip != null) {
            dateChip.setClickable(true);
            dateChip.setFocusable(true);
            dateChip.setOnClickListener(v -> openDatePicker());
        }
        View goldChart = findViewById(R.id.gold_chart_cta);
        if (goldChart != null) goldChart.setOnClickListener(v -> toggleGoldChart());
        View silverChart = findViewById(R.id.silver_chart_cta);
        if (silverChart != null) silverChart.setOnClickListener(v -> toggleSilverChart());
    }

    private void sharePrices() {
        JSONObject g = goldData != null ? goldData : new JSONObject();
        String body;
        if (!displayHasRates) {
            body = GoldLabels.title(this) + " — " + GoldLabels.ratesUnavailable(this);
        } else {
            double g10 = LocalUpdatesConfigHelper.optNumber(g, "gold_24k_10g", 0);
            double skg = LocalUpdatesConfigHelper.optNumber(g, "silver_kg", 0);
            body = GoldLabels.title(this) + " — "
                    + LocalUpdatesConfigHelper.optString(
                    goldSource != null ? goldSource : g, "location_name", "")
                    + " (" + formatDateLabel(selectedDateIso) + ")"
                    + (g10 > 0 ? ": 24K ₹" + LocalUpdatesConfigHelper.formatInr(g10) + " /10g" : "")
                    + (skg > 0 ? ", Silver ₹" + LocalUpdatesConfigHelper.formatInr(skg) + " /kg" : "");
        }
        DailyUpdatesShareLinks.launch(
                this, body, "gold", getString(R.string.local_update_share));
    }

    private void setText(int id, String value) {
        View v = findViewById(id);
        if (v instanceof TextView) {
            ((TextView) v).setText(value);
        }
    }

    /** Set label on a TextView, or the first TextView child of a ViewGroup (chart CTAs). */
    private void setLabelOnViewOrChild(int id, String value) {
        View v = findViewById(id);
        if (v instanceof TextView) {
            ((TextView) v).setText(value);
            return;
        }
        if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;
            for (int i = 0; i < vg.getChildCount(); i++) {
                View child = vg.getChildAt(i);
                if (child instanceof TextView) {
                    ((TextView) child).setText(value);
                    return;
                }
            }
        }
    }

    // ─── History / date helpers ───────────────────────────────────────────────

    private static JSONArray fullHistory(JSONObject g) {
        JSONArray hist = LocalUpdatesConfigHelper.optArray(g, "history");
        if (hist.length() > 0) return hist;
        return LocalUpdatesConfigHelper.optArray(g, "history_7day");
    }

    private static int findHistoryIndex(JSONArray hist, String iso) {
        if (!isIsoDate(iso) || hist == null) return -1;
        for (int i = 0; i < hist.length(); i++) {
            JSONObject row = hist.optJSONObject(i);
            if (row == null) continue;
            if (iso.equals(LocalUpdatesConfigHelper.optString(row, "date", ""))) return i;
        }
        // Fallback: match "Aug 28" style label
        String wantLabel = formatDateLabel(iso);
        for (int i = 0; i < hist.length(); i++) {
            JSONObject row = hist.optJSONObject(i);
            if (row == null) continue;
            String lab = LocalUpdatesConfigHelper.optString(row, "date_label", "");
            if (wantLabel.equalsIgnoreCase(lab)) return i;
        }
        return -1;
    }

    private static boolean isIsoDate(String s) {
        return s != null && s.matches("\\d{4}-\\d{2}-\\d{2}");
    }

    private static String todayIsoIst() {
        Calendar cal = Calendar.getInstance(TZ_IST);
        return String.format(Locale.US, "%04d-%02d-%02d",
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH));
    }

    private static String isoFromFetchedAt(String fetched) {
        if (fetched == null || fetched.isEmpty()) return "";
        try {
            Date d = null;
            try {
                SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                        Locale.US);
                iso.setTimeZone(TimeZone.getTimeZone("UTC"));
                d = iso.parse(fetched);
            } catch (Exception ignored) {
            }
            if (d == null) {
                SimpleDateFormat iso2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",
                        Locale.US);
                iso2.setTimeZone(TimeZone.getTimeZone("UTC"));
                d = iso2.parse(fetched);
            }
            if (d != null) {
                Calendar cal = Calendar.getInstance(TZ_IST);
                cal.setTime(d);
                return String.format(Locale.US, "%04d-%02d-%02d",
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH) + 1,
                        cal.get(Calendar.DAY_OF_MONTH));
            }
        } catch (Exception ignored) {
        }
        return "";
    }

    private static String formatDateLabel(String iso) {
        if (!isIsoDate(iso)) return iso != null ? iso : "";
        try {
            SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            in.setTimeZone(TZ_IST);
            Date d = in.parse(iso);
            if (d != null) {
                return new SimpleDateFormat("dd MMM yyyy", Locale.US).format(d);
            }
        } catch (Exception ignored) {
        }
        return iso;
    }

    private static final class PurityRow {
        final String label;
        final double price;
        final double change;

        PurityRow(String label, double price, double change) {
            this.label = label;
            this.price = price;
            this.change = change;
        }
    }
}
