package com.anyNews.anynews.Activities;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Utils.DailyUpdatesConfigHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Stock Market detail — live prices via server cache. No fake prices.
 * UI branding is ANYNEWS-only (no third-party source labels).
 */
public class StockMarketActivity extends AppCompatActivity {

    private final Handler ui = new Handler(Looper.getMainLooper());
    private final List<Row> rows = new ArrayList<>();
    private Adapter adapter;
    private TextView empty;
    private TextView niftyPrice;
    private TextView niftyChange;
    private TextView sensexPrice;
    private TextView sensexChange;
    private TextView listLabel;
    private Runnable searchRunnable;
    @Nullable
    private Call<JsonObject> inFlight;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!DailyUpdatesConfigHelper.isStockEnabled(this)) {
            Toast.makeText(this, "స్టాక్ మార్కెట్ అందుబాటులో లేదు", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        setContentView(R.layout.activity_stock_market);
        applySystemBars(R.id.stock_market_root);

        niftyPrice = findViewById(R.id.stock_nifty_price);
        niftyChange = findViewById(R.id.stock_nifty_change);
        sensexPrice = findViewById(R.id.stock_sensex_price);
        sensexChange = findViewById(R.id.stock_sensex_change);
        empty = findViewById(R.id.stock_empty);
        listLabel = findViewById(R.id.stock_list_label);

        TextView title = findViewById(R.id.stock_title);
        if (title != null) title.setText(DailyUpdatesConfigHelper.stockTitle(this));
        TextView source = findViewById(R.id.stock_source);
        if (source != null) source.setText(DailyUpdatesConfigHelper.stockLiveLabel(this));

        ImageView back = findViewById(R.id.stock_btn_back);
        if (back != null) back.setOnClickListener(v -> finish());

        RecyclerView list = findViewById(R.id.stock_list);
        adapter = new Adapter(rows);
        if (list != null) {
            list.setLayoutManager(new LinearLayoutManager(this));
            list.setAdapter(adapter);
        }

        EditText search = findViewById(R.id.stock_search);
        if (search != null) {
            search.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    if (searchRunnable != null) ui.removeCallbacks(searchRunnable);
                    final String q = s != null ? s.toString().trim() : "";
                    searchRunnable = () -> {
                        if (TextUtils.isEmpty(q)) {
                            loadWatchlist();
                        } else {
                            searchQuotes(q);
                        }
                    };
                    ui.postDelayed(searchRunnable, 450L);
                }
            });
        }

        paintSplashSummary();
        loadIndices();
        loadWatchlist();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (searchRunnable != null) ui.removeCallbacks(searchRunnable);
        if (inFlight != null) inFlight.cancel();
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

    private void paintSplashSummary() {
        try {
            org.json.JSONObject o = DailyUpdatesConfigHelper.stockSummaryJson(this);
            if (o == null) return;
            paintIndex(o.optJSONObject("nifty"), niftyPrice, niftyChange);
            paintIndex(o.optJSONObject("sensex"), sensexPrice, sensexChange);
        } catch (Exception ignored) {
        }
    }

    private void paintIndex(@Nullable org.json.JSONObject q,
                            @Nullable TextView priceTv,
                            @Nullable TextView changeTv) {
        if (q == null || priceTv == null || changeTv == null) return;
        if (!q.has("price") || q.isNull("price")) return;
        double price = q.optDouble("price", Double.NaN);
        if (Double.isNaN(price)) return;
        priceTv.setText(formatPrice(price));
        double ch = q.optDouble("change", 0);
        double pct = q.optDouble("change_percent", 0);
        boolean up = ch >= 0;
        changeTv.setText(String.format(Locale.US, "%s %.2f (%.2f%%)",
                up ? "▲" : "▼", Math.abs(ch), Math.abs(pct)));
        changeTv.setTextColor(Color.parseColor(up ? "#FF4ADE80" : "#FFF87171"));
    }

    private void loadIndices() {
        APIService api = RestApiClient.getClient().create(APIService.class);
        Call<JsonObject> call = api.stockMarket("v3", "indices", null, null);
        enqueue(call, body -> {
            if (body == null || !body.has("data") || body.get("data").isJsonNull()) return;
            JsonObject data = body.getAsJsonObject("data");
            JsonArray indices = data.has("indices") && data.get("indices").isJsonArray()
                    ? data.getAsJsonArray("indices") : null;
            if (indices == null) return;
            for (JsonElement el : indices) {
                if (!el.isJsonObject()) continue;
                JsonObject q = el.getAsJsonObject();
                String key = optStr(q, "key");
                if ("nifty".equals(key)) paintGsonIndex(q, niftyPrice, niftyChange);
                if ("sensex".equals(key)) paintGsonIndex(q, sensexPrice, sensexChange);
            }
        });
    }

    private void loadWatchlist() {
        if (listLabel != null) listLabel.setText("ప్రముఖ షేర్లు");
        APIService api = RestApiClient.getClient().create(APIService.class);
        Call<JsonObject> call = api.stockMarket("v3", "watchlist", null, null);
        enqueue(call, body -> {
            rows.clear();
            if (body != null && body.has("data") && body.get("data").isJsonObject()) {
                JsonObject data = body.getAsJsonObject("data");
                JsonArray quotes = data.has("quotes") && data.get("quotes").isJsonArray()
                        ? data.getAsJsonArray("quotes") : null;
                if (quotes != null) {
                    for (JsonElement el : quotes) {
                        if (!el.isJsonObject()) continue;
                        Row r = rowFromQuote(el.getAsJsonObject());
                        if (r != null) rows.add(r);
                    }
                }
            }
            adapter.notifyDataSetChanged();
            if (empty != null) {
                empty.setVisibility(rows.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void searchQuotes(@NonNull String q) {
        if (listLabel != null) listLabel.setText("శోధన ఫలితాలు");
        APIService api = RestApiClient.getClient().create(APIService.class);
        Call<JsonObject> call = api.stockMarket("v3", "search", q, null);
        enqueue(call, body -> {
            final List<String> symbols = new ArrayList<>();
            if (body != null && body.has("data") && body.get("data").isJsonObject()) {
                JsonObject data = body.getAsJsonObject("data");
                JsonArray results = data.has("results") && data.get("results").isJsonArray()
                        ? data.getAsJsonArray("results") : null;
                if (results != null) {
                    for (JsonElement el : results) {
                        if (!el.isJsonObject()) continue;
                        String sym = optStr(el.getAsJsonObject(), "symbol");
                        if (!TextUtils.isEmpty(sym)) symbols.add(sym);
                    }
                }
            }
            if (symbols.isEmpty()) {
                rows.clear();
                adapter.notifyDataSetChanged();
                if (empty != null) empty.setVisibility(View.VISIBLE);
                return;
            }
            String joined = TextUtils.join(",", symbols);
            Call<JsonObject> quotesCall = api.stockMarket("v3", "quote", null, joined);
            enqueue(quotesCall, qBody -> {
                rows.clear();
                if (qBody != null && qBody.has("data") && qBody.get("data").isJsonObject()) {
                    JsonArray quotes = qBody.getAsJsonObject("data").has("quotes")
                            ? qBody.getAsJsonObject("data").getAsJsonArray("quotes") : null;
                    if (quotes != null) {
                        for (JsonElement el : quotes) {
                            if (!el.isJsonObject()) continue;
                            Row r = rowFromQuote(el.getAsJsonObject());
                            if (r != null) rows.add(r);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
                if (empty != null) {
                    empty.setVisibility(rows.isEmpty() ? View.VISIBLE : View.GONE);
                }
            });
        });
    }

    private void paintGsonIndex(@NonNull JsonObject q,
                                @Nullable TextView priceTv,
                                @Nullable TextView changeTv) {
        if (priceTv == null || changeTv == null) return;
        if (!q.has("price") || q.get("price").isJsonNull()) return;
        double price = q.get("price").getAsDouble();
        priceTv.setText(formatPrice(price));
        double ch = q.has("change") && !q.get("change").isJsonNull()
                ? q.get("change").getAsDouble() : 0;
        double pct = q.has("change_percent") && !q.get("change_percent").isJsonNull()
                ? q.get("change_percent").getAsDouble() : 0;
        boolean up = ch >= 0;
        changeTv.setText(String.format(Locale.US, "%s %.2f (%.2f%%)",
                up ? "▲" : "▼", Math.abs(ch), Math.abs(pct)));
        changeTv.setTextColor(Color.parseColor(up ? "#FF4ADE80" : "#FFF87171"));
    }

    @Nullable
    private static Row rowFromQuote(@NonNull JsonObject q) {
        if (!q.has("price") || q.get("price").isJsonNull()) return null;
        Row r = new Row();
        r.symbol = optStr(q, "symbol");
        r.name = optStr(q, "name");
        r.price = q.get("price").getAsDouble();
        r.change = q.has("change") && !q.get("change").isJsonNull()
                ? q.get("change").getAsDouble() : 0;
        r.changePct = q.has("change_percent") && !q.get("change_percent").isJsonNull()
                ? q.get("change_percent").getAsDouble() : 0;
        if (TextUtils.isEmpty(r.symbol)) return null;
        return r;
    }

    private interface BodyConsumer {
        void accept(@Nullable JsonObject body);
    }

    private void enqueue(@Nullable Call<JsonObject> call, @NonNull BodyConsumer consumer) {
        if (call == null) return;
        if (inFlight != null) inFlight.cancel();
        inFlight = call;
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> c, @NonNull Response<JsonObject> response) {
                if (isFinishing()) return;
                if (!response.isSuccessful() || response.body() == null) {
                    if (empty != null && rows.isEmpty()) empty.setVisibility(View.VISIBLE);
                    return;
                }
                JsonObject body = response.body();
                if (body.has("enabled") && body.get("enabled").isJsonPrimitive()
                        && !body.get("enabled").getAsBoolean()) {
                    Toast.makeText(StockMarketActivity.this,
                            "స్టాక్ మార్కెట్ అందుబాటులో లేదు", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                consumer.accept(body);
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> c, @NonNull Throwable t) {
                if (isFinishing() || c.isCanceled()) return;
                if (empty != null && rows.isEmpty()) empty.setVisibility(View.VISIBLE);
            }
        });
    }

    @NonNull
    private static String optStr(@NonNull JsonObject o, @NonNull String key) {
        try {
            if (!o.has(key) || o.get(key).isJsonNull()) return "";
            return o.get(key).getAsString();
        } catch (Exception e) {
            return "";
        }
    }

    @NonNull
    private static String formatPrice(double price) {
        if (price >= 1000) {
            return String.format(Locale.US, "%,.2f", price);
        }
        return String.format(Locale.US, "%.2f", price);
    }

    private static final class Row {
        String symbol;
        String name;
        double price;
        double change;
        double changePct;
    }

    private static final class Adapter extends RecyclerView.Adapter<Adapter.VH> {
        private final List<Row> data;

        Adapter(List<Row> data) {
            this.data = data;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_stock_row, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            Row r = data.get(position);
            h.symbol.setText(r.symbol);
            h.name.setText(TextUtils.isEmpty(r.name) ? r.symbol : r.name);
            h.price.setText(formatPrice(r.price));
            boolean up = r.change >= 0;
            h.change.setText(String.format(Locale.US, "%s %.2f (%.2f%%)",
                    up ? "▲" : "▼", Math.abs(r.change), Math.abs(r.changePct)));
            h.change.setTextColor(Color.parseColor(up ? "#FF4ADE80" : "#FFF87171"));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        static final class VH extends RecyclerView.ViewHolder {
            final TextView symbol;
            final TextView name;
            final TextView price;
            final TextView change;

            VH(@NonNull View itemView) {
                super(itemView);
                symbol = itemView.findViewById(R.id.stock_row_symbol);
                name = itemView.findViewById(R.id.stock_row_name);
                price = itemView.findViewById(R.id.stock_row_price);
                change = itemView.findViewById(R.id.stock_row_change);
            }
        }
    }
}
