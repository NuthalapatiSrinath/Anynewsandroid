package com.anyNews.anynews.rewards;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Utils.DistrictPinVisibilityConfig;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Rewards → History: immutable credit rows (+ conversions / withdrawals).
 * Timestamps come from credited_at / createdAt only (never updatedAt).
 */
public class RewardsHistoryActivity extends AppCompatActivity {

    private LinearLayout list;
    private TextView hint;
    private int historyLimit = 20;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!DistrictPinVisibilityConfig.rewardsMasterEnabled(this)
                || !DistrictPinVisibilityConfig.rewardsVisibleForUser(this)) {
            Toast.makeText(this, "Rewards is currently off", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        setContentView(R.layout.activity_rewards_history);
        applySystemBars();
        list = findViewById(R.id.history_list);
        hint = findViewById(R.id.history_hint);
        TextView title = findViewById(R.id.history_top_title);
        if (title != null) title.setText(RewardsLabels.history(this));
        findViewById(R.id.history_back).setOnClickListener(v -> finish());
        if (getIntent() != null && getIntent().hasExtra("history_limit")) {
            historyLimit = Math.max(5, getIntent().getIntExtra("history_limit", 20));
        }
        loadHistory();
    }

    private void applySystemBars() {
        Window window = getWindow();
        WindowCompat.setDecorFitsSystemWindows(window, false);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.parseColor("#FF0B1020"));
        window.setNavigationBarColor(Color.parseColor("#FF0B1020"));
        WindowInsetsControllerCompat c =
                WindowCompat.getInsetsController(window, window.getDecorView());
        if (c != null) {
            c.setAppearanceLightStatusBars(false);
            c.setAppearanceLightNavigationBars(false);
        }
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.rewards_history_root),
                (v, insets) -> {
                    Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(0, bars.top, 0, bars.bottom);
                    return insets;
                });
    }

    private void loadHistory() {
        APIService api = RestApiClient.getClient().create(APIService.class);
        JsonObject body = RewardsAuth.authBody(this);
        body.addProperty("limit", historyLimit);
        body.addProperty("all_periods", true);
        api.rewards_history("v3", body)
                .enqueue(
                        new Callback<JsonObject>() {
                            @Override
                            public void onResponse(
                                    Call<JsonObject> call, Response<JsonObject> response) {
                                JsonObject b = response.body();
                                if (b == null
                                        || !b.has("success")
                                        || !b.get("success").getAsBoolean()) {
                                    toast("Could not load history");
                                    return;
                                }
                                if (b.has("history_limit")) {
                                    historyLimit =
                                            Math.max(5, b.get("history_limit").getAsInt());
                                }
                                render(b);
                            }

                            @Override
                            public void onFailure(Call<JsonObject> call, Throwable t) {
                                toast("Network error");
                            }
                        });
    }

    private void render(JsonObject body) {
        list.removeAllViews();
        List<Row> rows = new ArrayList<>();
        // Account-scoped day totals first (so large factors aren't hidden by last-N).
        renderByTaskToday(body);
        collectCoins(body, rows);
        collectMoney(body, "conversions", true, rows);
        collectMoney(body, "withdrawals", false, rows);
        Collections.sort(
                rows,
                new Comparator<Row>() {
                    @Override
                    public int compare(Row a, Row b) {
                        return Long.compare(b.atMs, a.atMs);
                    }
                });
        int n = Math.min(historyLimit, rows.size());
        for (int i = 0; i < n; i++) {
            addLedgerRow(rows.get(i).amount, rows.get(i).title, rows.get(i).meta);
            if (i < n - 1) addHairline();
        }
        if (hint != null) {
            if (n > 0 || list.getChildCount() > 0) {
                hint.setVisibility(View.VISIBLE);
                hint.setText(RewardsLabels.historyShowingLastN(this, historyLimit));
            } else {
                hint.setVisibility(View.GONE);
            }
        }
        if (list.getChildCount() == 0) {
            TextView empty = new TextView(this);
            empty.setText(RewardsLabels.noHistory(this));
            empty.setTextColor(Color.parseColor("#FFA89880"));
            empty.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            empty.setPadding(dp(20), dp(28), dp(20), dp(28));
            list.addView(empty);
        }
    }

    /** Gold +amount, cream factor, muted time. Generous padding — never a cramped line. */
    private void addLedgerRow(String amount, String title, String meta) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(20), dp(18), dp(20), dp(18));

        TextView amt = new TextView(this);
        amt.setText(amount == null ? "" : amount);
        amt.setTextColor(Color.parseColor("#FFE4C27A"));
        amt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        amt.setTypeface(amt.getTypeface(), android.graphics.Typeface.BOLD);
        amt.setMinWidth(dp(72));
        row.addView(amt);

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams colLp =
                new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        colLp.setMarginStart(dp(16));
        col.setLayoutParams(colLp);

        TextView name = new TextView(this);
        name.setText(title == null ? "" : title);
        name.setTextColor(Color.parseColor("#FFF4EDE0"));
        name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        col.addView(name);

        if (!TextUtils.isEmpty(meta)) {
            TextView sub = new TextView(this);
            sub.setText(meta);
            sub.setTextColor(Color.parseColor("#FFA89880"));
            sub.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            sub.setPadding(0, dp(4), 0, 0);
            col.addView(sub);
        }
        row.addView(col);
        list.addView(row);
    }

    private void addHairline() {
        View div = new View(this);
        div.setBackgroundColor(Color.parseColor("#FF243049"));
        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1);
        lp.setMargins(dp(20), 0, dp(20), 0);
        list.addView(div, lp);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    /** Today’s ledger-by-factor (server aggregate) — account user_id, not device. */
    private void renderByTaskToday(JsonObject body) {
        if (!body.has("by_task_today") || !body.get("by_task_today").isJsonArray()) return;
        JsonArray arr = body.getAsJsonArray("by_task_today");
        if (arr.size() == 0) return;
        TextView head = new TextView(this);
        head.setText(RewardsLabels.todayByTask(this));
        head.setTextColor(Color.parseColor("#FFE4C27A"));
        head.setLetterSpacing(0.04f);
        head.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        head.setPadding(dp(20), dp(16), dp(20), dp(8));
        list.addView(head);
        boolean any = false;
        for (int i = 0; i < arr.size(); i++) {
            JsonObject row = arr.get(i).getAsJsonObject();
            int coins = optInt(row, "coins");
            int events = optInt(row, "events");
            if (coins <= 0) continue;
            if (any) addHairline();
            any = true;
            String factor = optStr(row, "factor", "");
            String eventsBit = events > 0 ? "(" + events + ")" : "";
            addLedgerRow(
                    "+" + coins,
                    RewardsLabels.factorLabel(this, factor),
                    eventsBit);
        }
        View div = new View(this);
        div.setBackgroundColor(Color.parseColor("#55E4C27A"));
        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1);
        lp.setMargins(dp(20), dp(10), dp(20), dp(6));
        list.addView(div, lp);
    }

    private static final class Row {
        final String amount;
        final String title;
        final String meta;
        final long atMs;

        Row(String amount, String title, String meta, long atMs) {
            this.amount = amount;
            this.title = title;
            this.meta = meta;
            this.atMs = atMs;
        }
    }

    private void collectCoins(JsonObject body, List<Row> out) {
        if (!body.has("coin_history") || !body.get("coin_history").isJsonArray()) return;
        JsonArray arr = body.getAsJsonArray("coin_history");
        for (int i = 0; i < arr.size(); i++) {
            JsonObject row = arr.get(i).getAsJsonObject();
            int coins = optInt(row, "coins");
            if (coins == 0) continue;
            String sign = coins > 0 ? "+" : "";
            String when = formatWhen(row);
            String factor = optStr(row, "factor", "");
            String title =
                    TextUtils.isEmpty(factor)
                            ? RewardsLabels.coinsWord(this)
                            : RewardsLabels.factorLabel(this, factor);
            out.add(new Row(sign + coins, title, when, parseWhenMs(row)));
        }
    }

    private void collectMoney(JsonObject body, String key, boolean credit, List<Row> out) {
        if (!body.has(key) || !body.get(key).isJsonArray()) return;
        JsonArray arr = body.getAsJsonArray(key);
        for (int i = 0; i < arr.size(); i++) {
            JsonObject row = arr.get(i).getAsJsonObject();
            double amt =
                    credit ? optDouble(row, "rupees") : optDouble(row, "amount_rupees");
            if (amt <= 0) continue;
            String sign = credit ? "+" : "−";
            String when = formatWhen(row);
            String title =
                    credit ? RewardsLabels.convertCoins(this) : RewardsLabels.withdraw(this);
            out.add(new Row(sign + "₹" + formatMoney(amt), title, when, parseWhenMs(row)));
        }
    }

    /** Prefer credited_at / createdAt — never updatedAt (mutable on aggregated ledger). */
    private long parseWhenMs(JsonObject row) {
        String raw = optStr(row, "credited_at", null);
        if (TextUtils.isEmpty(raw)) raw = optStr(row, "createdAt", null);
        if (TextUtils.isEmpty(raw)) return 0L;
        return parseIsoMs(raw);
    }

    private String formatWhen(JsonObject row) {
        long ms = parseWhenMs(row);
        if (ms <= 0) return "";
        try {
            SimpleDateFormat out =
                    new SimpleDateFormat("dd MMM, h:mm a", Locale.getDefault());
            out.setTimeZone(TimeZone.getDefault());
            return out.format(new Date(ms));
        } catch (Exception e) {
            return "";
        }
    }

    private static long parseIsoMs(String raw) {
        try {
            String iso = raw.trim();
            if (iso.endsWith("Z")) {
                SimpleDateFormat in =
                        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                in.setTimeZone(TimeZone.getTimeZone("UTC"));
                int dot = iso.indexOf('.');
                String trunc = iso;
                if (dot > 0) {
                    String frac = iso.substring(dot + 1).replace("Z", "");
                    if (frac.length() < 3) {
                        while (frac.length() < 3) frac += "0";
                    } else if (frac.length() > 3) {
                        frac = frac.substring(0, 3);
                    }
                    trunc = iso.substring(0, dot + 1) + frac + "Z";
                } else {
                    in = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
                    in.setTimeZone(TimeZone.getTimeZone("UTC"));
                }
                Date d = in.parse(trunc);
                return d != null ? d.getTime() : 0L;
            }
        } catch (Exception ignored) {
        }
        return 0L;
    }

    private static int optInt(JsonObject o, String k) {
        try {
            return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsInt() : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private static double optDouble(JsonObject o, String k) {
        try {
            return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsDouble() : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private static String optStr(JsonObject o, String k, String def) {
        try {
            if (o != null && o.has(k) && !o.get(k).isJsonNull()) {
                return o.get(k).getAsString();
            }
        } catch (Exception ignored) {
        }
        return def;
    }

    private static String formatMoney(double amt) {
        if (Math.abs(amt - Math.rint(amt)) < 0.001) {
            return String.valueOf((long) Math.rint(amt));
        }
        return String.format(Locale.US, "%.2f", amt);
    }

    private void toast(String m) {
        Toast.makeText(this, m, Toast.LENGTH_SHORT).show();
    }
}
