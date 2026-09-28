package com.anyNews.anynews.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Outline;
import android.os.Bundle;
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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.lang.ref.WeakReference;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.anyNews.anynews.R;
import com.anyNews.anynews.Utils.LocalUpdatesConfigHelper;
import com.anyNews.anynews.Utils.WeatherDetailLabels;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Weather detail — matches admin WeatherPreview; data from splash root_settings.weather.
 */
public class WeatherDetailActivity extends AppCompatActivity {

    private static final int ACCENT = 0xFFFF6A1A;

    @Nullable private static WeakReference<WeatherDetailActivity> openRef;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!LocalUpdatesConfigHelper.isWeatherEnabled(this)) {
            Toast.makeText(this, R.string.weather_disabled, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        setContentView(R.layout.activity_weather_detail);
        applySystemBars(R.id.weather_detail_root);
        clipHero();
        bindUi(LocalUpdatesConfigHelper.weatherJson(this));
        wireClicks();
    }

    @Override
    protected void onStart() {
        super.onStart();
        openRef = new WeakReference<>(this);
    }

    @Override
    protected void onStop() {
        WeatherDetailActivity cur = openRef != null ? openRef.get() : null;
        if (cur == this) openRef = null;
        super.onStop();
    }

    /** Feed replaced the cached weather pack. */
    public static void onWeatherPackUpdated() {
        WeatherDetailActivity activity = openRef != null ? openRef.get() : null;
        if (activity == null || activity.isFinishing()) return;
        activity.runOnUiThread(() -> {
            if (activity.isFinishing()) return;
            activity.bindUi(LocalUpdatesConfigHelper.weatherJson(activity));
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
        View hero = findViewById(R.id.weather_hero);
        if (hero == null) return;
        final float radius = 16f * getResources().getDisplayMetrics().density;
        hero.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radius);
            }
        });
        hero.setClipToOutline(true);
    }

    private void bindUi(@Nullable JSONObject w) {
        if (w == null) w = new JSONObject();
        // Prefer state / region for hero — not a district name.
        String region = LocalUpdatesConfigHelper.optString(
                w, "location_region", WeatherDetailLabels.defaultRegion(this));
        String place = LocalUpdatesConfigHelper.optString(w, "location_name", "");
        // If location_name looks like a district desk city, prefer region’s first segment.
        String heroPlace = region;
        int comma = region.indexOf(',');
        if (comma > 0) heroPlace = region.substring(0, comma).trim();
        if (heroPlace.isEmpty()) heroPlace = place;
        setText(R.id.weather_location, "📍 " + heroPlace);
        setText(R.id.weather_region, region);
        setText(R.id.weather_updated, LocalUpdatesConfigHelper.optString(w, "last_updated", "—"));
        int temp = LocalUpdatesConfigHelper.optNumberInt(w, "temp_c", 31);
        setText(R.id.weather_temp, temp + "°C");
        setText(R.id.weather_condition, LocalUpdatesConfigHelper.weatherCondition(this));
        setText(R.id.weather_feels, WeatherDetailLabels.feelsHighLow(
                this,
                LocalUpdatesConfigHelper.optNumberInt(w, "feels_like_c", 34),
                LocalUpdatesConfigHelper.optNumberInt(w, "high_c", 32),
                LocalUpdatesConfigHelper.optNumberInt(w, "low_c", 25)));
        setText(R.id.weather_humidity, WeatherDetailLabels.rainHumidity(
                this,
                LocalUpdatesConfigHelper.optNumberInt(w, "precip_pct", 0),
                LocalUpdatesConfigHelper.optNumberInt(w, "humidity", 62)));
        setText(R.id.weather_wind, WeatherDetailLabels.windSpeed(
                this, LocalUpdatesConfigHelper.optNumberInt(w, "wind_kmh", 14)));
        setText(R.id.weather_wind_dir, WeatherDetailLabels.windDir(
                this, LocalUpdatesConfigHelper.optString(w, "wind_dir", "SW")));
        setText(R.id.weather_visibility, WeatherDetailLabels.visibility(
                this, LocalUpdatesConfigHelper.optNumber(w, "visibility_km", 10)));
        setText(R.id.weather_alert_text, LocalUpdatesConfigHelper.optString(w, "alert_text", ""));
        setText(R.id.weather_sunrise, LocalUpdatesConfigHelper.optString(w, "sunrise", "05:59 AM"));
        setText(R.id.weather_sunset, LocalUpdatesConfigHelper.optString(w, "sunset", "06:23 PM"));
        // Data sources are admin-only — never show Open-Meteo / platform labels in the app.
        View weatherSource = findViewById(R.id.weather_source);
        if (weatherSource != null) weatherSource.setVisibility(View.GONE);

        String heroUrl = LocalUpdatesConfigHelper.optString(w, "hero_image_url", "");
        View heroIv = findViewById(R.id.weather_hero_image);
        if (heroIv instanceof android.widget.ImageView) {
            if (heroUrl.startsWith("http://") || heroUrl.startsWith("https://")) {
                Glide.with(this).load(heroUrl).centerCrop()
                        .placeholder(R.drawable.weather_hero_sun)
                        .into((android.widget.ImageView) heroIv);
            } else {
                ((android.widget.ImageView) heroIv).setImageResource(R.drawable.weather_hero_sun);
            }
        }

        bindForecast(LocalUpdatesConfigHelper.optArray(w, "forecast_3day"));
        bindHourly(LocalUpdatesConfigHelper.optArray(w, "hourly"));

        View alert = findViewById(R.id.weather_alert);
        if (alert != null) {
            String tone = LocalUpdatesConfigHelper.optString(w, "alert_tone", "ok");
            int border = tone.equals("danger") ? 0x80F87171
                    : tone.equals("warn") ? 0x80FBBF24 : 0x8034D399;
            alert.setBackgroundColor(0xFF0F1F18);
            try {
                android.graphics.drawable.GradientDrawable gd =
                        new android.graphics.drawable.GradientDrawable();
                gd.setColor(tone.equals("danger") ? 0xFF2A1215
                        : tone.equals("warn") ? 0xFF2A2210 : 0xFF0F1F18);
                gd.setStroke((int) (1 * getResources().getDisplayMetrics().density), border);
                gd.setCornerRadius(12 * getResources().getDisplayMetrics().density);
                alert.setBackground(gd);
            } catch (Exception ignored) {
            }
        }
    }

    private void bindForecast(JSONArray days) {
        LinearLayout row = findViewById(R.id.weather_forecast_row);
        if (row == null) return;
        row.removeAllViews();
        float d = getResources().getDisplayMetrics().density;
        int count = Math.min(3, days.length());
        for (int i = 0; i < Math.max(count, 1); i++) {
            JSONObject day = days.optJSONObject(i);
            if (day == null) day = new JSONObject();
            LinearLayout card = new LinearLayout(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            lp.setMargins((int) (3 * d), 0, (int) (3 * d), 0);
            card.setLayoutParams(lp);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setGravity(Gravity.CENTER_HORIZONTAL);
            card.setBackgroundResource(R.drawable.bg_local_update_card);
            card.setPadding((int) (6 * d), (int) (10 * d), (int) (6 * d), (int) (10 * d));

            addCenterText(card, LocalUpdatesConfigHelper.optString(day, "day_label", "—"),
                    0xFFFFFFFF, 10, true);
            addCenterText(card, LocalUpdatesConfigHelper.optString(day, "date_label", "—"),
                    0xFF94A3B8, 9, false);
            addCenterText(card, iconGlyph(LocalUpdatesConfigHelper.optString(day, "icon", "")),
                    ACCENT, 18, false);
            addCenterText(card,
                    LocalUpdatesConfigHelper.optNumberInt(day, "high_c", 32) + "° / "
                            + LocalUpdatesConfigHelper.optNumberInt(day, "low_c", 25) + "°",
                    0xFFFFFFFF, 11, true);
            addCenterText(card, LocalUpdatesConfigHelper.optString(day, "condition", ""),
                    0xFF94A3B8, 9, false);
            addCenterText(card, LocalUpdatesConfigHelper.optNumberInt(day, "precip_pct", 0) + "%",
                    0xFF7DD3FC, 9, false);
            row.addView(card);
        }
    }

    private void bindHourly(JSONArray hourly) {
        LinearLayout row = findViewById(R.id.weather_hourly_row);
        if (row == null) return;
        row.removeAllViews();
        float d = getResources().getDisplayMetrics().density;
        for (int i = 0; i < hourly.length(); i++) {
            JSONObject h = hourly.optJSONObject(i);
            if (h == null) continue;
            LinearLayout tile = new LinearLayout(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    (int) (54 * d), ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins((int) (3 * d), 0, (int) (3 * d), 0);
            tile.setLayoutParams(lp);
            tile.setOrientation(LinearLayout.VERTICAL);
            tile.setGravity(Gravity.CENTER_HORIZONTAL);
            tile.setPadding((int) (4 * d), (int) (8 * d), (int) (4 * d), (int) (8 * d));
            boolean isNow = h.optBoolean("is_now", false);
            tile.setBackgroundResource(isNow
                    ? R.drawable.bg_local_update_now
                    : R.drawable.bg_local_update_card);
            addCenterText(tile, LocalUpdatesConfigHelper.optString(h, "time_label", ""),
                    0xFFCBD5E1, 9, false);
            addCenterText(tile, iconGlyph(LocalUpdatesConfigHelper.optString(h, "icon", "")),
                    ACCENT, 14, false);
            addCenterText(tile, LocalUpdatesConfigHelper.optNumberInt(h, "temp_c", 0) + "°",
                    0xFFFFFFFF, 11, true);
            row.addView(tile);
        }
    }

    private void addCenterText(LinearLayout parent, String text, int color, int sp, boolean bold) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(color);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        tv.setGravity(Gravity.CENTER);
        if (bold) tv.setTypeface(tv.getTypeface(), android.graphics.Typeface.BOLD);
        parent.addView(tv);
    }

    private static String iconGlyph(String icon) {
        switch (String.valueOf(icon)) {
            case "sunny": return "☀";
            case "night": return "☾";
            case "rainy": return "☂";
            case "storm": return "⚡";
            case "cloudy": return "☁";
            default: return "⛅";
        }
    }

    /** Prefer forecast_7day; fall back to forecast_3day when older splash packs lack 7 days. */
    private JSONArray forecastDaysForSheet(JSONObject w) {
        JSONArray seven = LocalUpdatesConfigHelper.optArray(w, "forecast_7day");
        if (seven != null && seven.length() > 0) return seven;
        return LocalUpdatesConfigHelper.optArray(w, "forecast_3day");
    }

    private void showSevenDayForecast() {
        JSONObject w = LocalUpdatesConfigHelper.weatherJson(this);
        if (w == null) w = new JSONObject();
        JSONArray days = forecastDaysForSheet(w);
        if (days == null || days.length() == 0) {
            Toast.makeText(this, "ఫోరకాస్ట్ డేటా లేదు", Toast.LENGTH_SHORT).show();
            return;
        }

        BottomSheetDialog sheet = new BottomSheetDialog(this);
        View content = getLayoutInflater().inflate(R.layout.bottom_sheet_weather_7day, null);
        LinearLayout list = content.findViewById(R.id.weather_7day_list);
        float d = getResources().getDisplayMetrics().density;
        int n = Math.min(7, days.length());
        for (int i = 0; i < n; i++) {
            JSONObject day = days.optJSONObject(i);
            if (day == null) day = new JSONObject();
            LinearLayout row = new LinearLayout(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            if (i > 0) lp.topMargin = (int) (8 * d);
            row.setLayoutParams(lp);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setBackgroundResource(R.drawable.bg_local_update_card);
            row.setPadding((int) (12 * d), (int) (10 * d), (int) (12 * d), (int) (10 * d));

            LinearLayout left = new LinearLayout(this);
            left.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams leftLp = new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            left.setLayoutParams(leftLp);
            addLeftText(left, LocalUpdatesConfigHelper.optString(day, "day_label", "—"),
                    0xFFFFFFFF, 13, true);
            addLeftText(left, LocalUpdatesConfigHelper.optString(day, "date_label", ""),
                    0xFF94A3B8, 11, false);
            addLeftText(left, LocalUpdatesConfigHelper.optString(day, "condition", ""),
                    0xFFCBD5E1, 11, false);
            row.addView(left);

            TextView icon = new TextView(this);
            icon.setText(iconGlyph(LocalUpdatesConfigHelper.optString(day, "icon", "")));
            icon.setTextColor(ACCENT);
            icon.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
            icon.setPadding((int) (8 * d), 0, (int) (10 * d), 0);
            row.addView(icon);

            LinearLayout right = new LinearLayout(this);
            right.setOrientation(LinearLayout.VERTICAL);
            right.setGravity(Gravity.END);
            addLeftText(right,
                    LocalUpdatesConfigHelper.optNumberInt(day, "high_c", 32) + "° / "
                            + LocalUpdatesConfigHelper.optNumberInt(day, "low_c", 25) + "°",
                    0xFFFFFFFF, 13, true);
            addLeftText(right,
                    "వర్షం " + LocalUpdatesConfigHelper.optNumberInt(day, "precip_pct", 0) + "%",
                    0xFF7DD3FC, 11, false);
            row.addView(right);

            list.addView(row);
        }
        sheet.setContentView(content);
        sheet.show();
    }

    private void addLeftText(LinearLayout parent, String text, int color, int sp, boolean bold) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(color);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        tv.setGravity(Gravity.START);
        if (bold) tv.setTypeface(tv.getTypeface(), android.graphics.Typeface.BOLD);
        parent.addView(tv);
    }

    private void wireClicks() {
        View back = findViewById(R.id.weather_back);
        if (back != null) back.setOnClickListener(v -> finish());
        View share = findViewById(R.id.weather_share);
        if (share != null) {
            share.setOnClickListener(v -> {
                JSONObject w = LocalUpdatesConfigHelper.weatherJson(this);
                String body = "వాతావరణం — "
                        + LocalUpdatesConfigHelper.optString(w, "location_name", "")
                        + ": " + LocalUpdatesConfigHelper.optNumberInt(w, "temp_c", 0) + "°C, "
                        + LocalUpdatesConfigHelper.optString(w, "condition", "");
                Intent send = new Intent(Intent.ACTION_SEND);
                send.setType("text/plain");
                send.putExtra(Intent.EXTRA_TEXT, body);
                startActivity(Intent.createChooser(send, getString(R.string.local_update_share)));
            });
        }
        View sevenDay = findViewById(R.id.weather_forecast_7day_btn);
        if (sevenDay != null) {
            sevenDay.setOnClickListener(v -> showSevenDayForecast());
        }
    }

    private void setText(int id, String value) {
        TextView tv = findViewById(id);
        if (tv != null) tv.setText(value);
    }
}
