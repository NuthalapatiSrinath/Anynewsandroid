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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Ui.StockSparklineView;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.DailyUpdatesConfigHelper;
import com.anyNews.anynews.Utils.DailyUpdatesLabels;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Stock Market detail — live prices via server Redis/Yahoo cache. No fake prices.
 * Pinned symbols persist in SharedPreferences and sort under a dedicated section.
 */
public class StockMarketActivity extends AppCompatActivity {

    /** Keep in sync with anynewsserver helpers/stock/fetchStock.js DEFAULT_WATCHLIST. */
    private static final String[] DEFAULT_POPULAR = {
            "RELIANCE.NS", "TCS.NS", "INFY.NS", "HDFCBANK.NS", "ICICIBANK.NS",
            "SBIN.NS", "BHARTIARTL.NS", "ITC.NS", "LT.NS", "AXISBANK.NS"
    };

    private static final long UI_REFRESH_MS = 240_000L; // match server ~4 min tick
    private static final int COLOR_UP = 0xFF22C55E;
    private static final int COLOR_DOWN = 0xFFEF4444;

    private final Handler ui = new Handler(Looper.getMainLooper());
    private final List<ListItem> items = new ArrayList<>();
    private Adapter adapter;
    private TextView empty;
    private TextView niftyPrice;
    private TextView niftyChange;
    private TextView sensexPrice;
    private TextView sensexChange;
    private StockSparklineView niftySpark;
    private StockSparklineView sensexSpark;
    private EditText searchField;
    private LinearLayout searchWrap;
    private ImageView searchIcon;
    private Runnable searchRunnable;
    private Runnable refreshRunnable;
    @Nullable
    private Call<JsonObject> indicesCall;
    @Nullable
    private Call<JsonObject> listCall;
    private boolean searching;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!DailyUpdatesConfigHelper.isStockEnabled(this)) {
            Toast.makeText(this, DailyUpdatesLabels.stockDisabled(this), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        setContentView(R.layout.activity_stock_market);
        applySystemBars(R.id.stock_market_root);

        niftyPrice = findViewById(R.id.stock_nifty_price);
        niftyChange = findViewById(R.id.stock_nifty_change);
        sensexPrice = findViewById(R.id.stock_sensex_price);
        sensexChange = findViewById(R.id.stock_sensex_change);
        niftySpark = findViewById(R.id.stock_nifty_spark);
        sensexSpark = findViewById(R.id.stock_sensex_spark);
        empty = findViewById(R.id.stock_empty);
        searchWrap = findViewById(R.id.stock_search_wrap);
        searchIcon = findViewById(R.id.stock_search_icon);

        TextView title = findViewById(R.id.stock_title);
        if (title != null) title.setText(DailyUpdatesConfigHelper.stockTitle(this));
        TextView source = findViewById(R.id.stock_source);
        if (source != null) source.setText(DailyUpdatesConfigHelper.stockLiveLabel(this));
        TextView niftyLabel = findViewById(R.id.stock_nifty_label);
        if (niftyLabel != null) niftyLabel.setText(DailyUpdatesLabels.nifty50(this));
        TextView sensexLabel = findViewById(R.id.stock_sensex_label);
        if (sensexLabel != null) sensexLabel.setText(DailyUpdatesLabels.sensex(this));

        ImageView back = findViewById(R.id.stock_btn_back);
        if (back != null) back.setOnClickListener(v -> finish());

        RecyclerView list = findViewById(R.id.stock_list);
        adapter = new Adapter(items, this::togglePin);
        if (list != null) {
            list.setLayoutManager(new LinearLayoutManager(this));
            list.setAdapter(adapter);
            list.setHasFixedSize(true);
        }

        searchField = findViewById(R.id.stock_search);
        if (searchField != null) {
            searchField.setHint(DailyUpdatesLabels.stockSearchHint(this));
            searchField.setOnFocusChangeListener((v, hasFocus) -> {
                if (searchWrap != null) searchWrap.setSelected(hasFocus);
                if (searchIcon != null) {
                    searchIcon.setColorFilter(Color.parseColor(hasFocus ? "#FF818CF8" : "#FF94A3B8"));
                }
            });
            searchField.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    if (searchRunnable != null) ui.removeCallbacks(searchRunnable);
                    final String q = s != null ? s.toString().trim() : "";
                    searchRunnable = () -> {
                        if (TextUtils.isEmpty(q)) {
                            searching = false;
                            loadWatchlist();
                        } else {
                            searching = true;
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
        scheduleUiRefresh();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (searchRunnable != null) ui.removeCallbacks(searchRunnable);
        if (refreshRunnable != null) ui.removeCallbacks(refreshRunnable);
        if (indicesCall != null) indicesCall.cancel();
        if (listCall != null) listCall.cancel();
    }

    private void scheduleUiRefresh() {
        if (refreshRunnable != null) ui.removeCallbacks(refreshRunnable);
        refreshRunnable = () -> {
            if (isFinishing()) return;
            loadIndices();
            if (!searching) loadWatchlist();
            scheduleUiRefresh();
        };
        ui.postDelayed(refreshRunnable, UI_REFRESH_MS);
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
            paintIndex(o.optJSONObject("nifty"), niftyPrice, niftyChange, niftySpark);
            paintIndex(o.optJSONObject("sensex"), sensexPrice, sensexChange, sensexSpark);
        } catch (Exception ignored) {
        }
    }

    private void paintIndex(@Nullable org.json.JSONObject q,
                            @Nullable TextView priceTv,
                            @Nullable TextView changeTv,
                            @Nullable StockSparklineView spark) {
        if (q == null || priceTv == null || changeTv == null) return;
        if (!q.has("price") || q.isNull("price")) return;
        double price = q.optDouble("price", Double.NaN);
        if (Double.isNaN(price)) return;
        priceTv.setText(formatPrice(price));
        double ch = q.optDouble("change", 0);
        double pct = q.optDouble("change_percent", 0);
        applyChangeChip(changeTv, ch, pct);
        if (spark != null) {
            spark.setPoints(sparkFromOrgJson(q));
        }
    }

    private void loadIndices() {
        APIService api = RestApiClient.getClient().create(APIService.class);
        Call<JsonObject> call = api.stockMarket("v3", "indices", null, null);
        enqueueIndices(call, body -> {
            if (body == null || !body.has("data") || body.get("data").isJsonNull()) return;
            JsonObject data = body.getAsJsonObject("data");
            JsonArray indices = data.has("indices") && data.get("indices").isJsonArray()
                    ? data.getAsJsonArray("indices") : null;
            if (indices == null) return;
            for (JsonElement el : indices) {
                if (!el.isJsonObject()) continue;
                JsonObject q = el.getAsJsonObject();
                String key = optStr(q, "key");
                if ("nifty".equals(key)) paintGsonIndex(q, niftyPrice, niftyChange, niftySpark);
                if ("sensex".equals(key)) paintGsonIndex(q, sensexPrice, sensexChange, sensexSpark);
            }
        });
    }

    private void loadWatchlist() {
        LinkedHashSet<String> ordered = new LinkedHashSet<>();
        for (String s : loadPinnedSymbols()) ordered.add(s);
        for (String s : DEFAULT_POPULAR) ordered.add(s);
        String symbolsParam = TextUtils.join(",", ordered);
        APIService api = RestApiClient.getClient().create(APIService.class);
        Call<JsonObject> call = api.stockMarket("v3", "watchlist", null, symbolsParam);
        enqueueList(call, body -> {
            List<Row> fetched = new ArrayList<>();
            if (body != null && body.has("data") && body.get("data").isJsonObject()) {
                JsonObject data = body.getAsJsonObject("data");
                JsonArray quotes = data.has("quotes") && data.get("quotes").isJsonArray()
                        ? data.getAsJsonArray("quotes") : null;
                if (quotes != null) {
                    for (JsonElement el : quotes) {
                        if (!el.isJsonObject()) continue;
                        Row r = rowFromQuote(el.getAsJsonObject());
                        if (r != null) fetched.add(r);
                    }
                }
            }
            publishRows(sortPinnedFirst(fetched), false);
        });
    }

    private void searchQuotes(@NonNull String q) {
        APIService api = RestApiClient.getClient().create(APIService.class);
        Call<JsonObject> call = api.stockMarket("v3", "search", q, null);
        enqueueList(call, body -> {
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
                publishRows(new ArrayList<>(), true);
                return;
            }
            String joined = TextUtils.join(",", symbols);
            Call<JsonObject> quotesCall = api.stockMarket("v3", "quote", null, joined);
            enqueueList(quotesCall, qBody -> {
                List<Row> fetched = new ArrayList<>();
                if (qBody != null && qBody.has("data") && qBody.get("data").isJsonObject()) {
                    JsonArray quotes = qBody.getAsJsonObject("data").has("quotes")
                            ? qBody.getAsJsonObject("data").getAsJsonArray("quotes") : null;
                    if (quotes != null) {
                        for (JsonElement el : quotes) {
                            if (!el.isJsonObject()) continue;
                            Row r = rowFromQuote(el.getAsJsonObject());
                            if (r != null) fetched.add(r);
                        }
                    }
                }
                publishRows(sortPinnedFirst(fetched), true);
            });
        });
    }

    private void publishRows(@NonNull List<Row> rows, boolean isSearch) {
        items.clear();
        if (isSearch) {
            if (!rows.isEmpty()) {
                items.add(ListItem.header(DailyUpdatesLabels.stockSearchResults(this)));
                for (Row r : rows) items.add(ListItem.row(r));
            }
        } else {
            List<Row> pinned = new ArrayList<>();
            List<Row> rest = new ArrayList<>();
            for (Row r : rows) {
                if (r != null && r.pinned) pinned.add(r);
                else if (r != null) rest.add(r);
            }
            if (!pinned.isEmpty()) {
                items.add(ListItem.header("పిన్ చేసినవి"));
                for (Row r : pinned) items.add(ListItem.row(r));
            }
            if (!rest.isEmpty()) {
                items.add(ListItem.header("ప్రముఖ షేర్లు"));
                for (Row r : rest) items.add(ListItem.row(r));
            }
        }
        adapter.notifyDataSetChanged();
        boolean hasRows = false;
        for (ListItem it : items) {
            if (it.type == ListItem.TYPE_ROW) {
                hasRows = true;
                break;
            }
        }
        if (empty != null) empty.setVisibility(hasRows ? View.GONE : View.VISIBLE);
    }

    @NonNull
    private List<Row> sortPinnedFirst(@NonNull List<Row> source) {
        Set<String> pinned = new LinkedHashSet<>(loadPinnedSymbols());
        List<Row> out = new ArrayList<>();
        for (Row r : source) {
            if (r != null && pinned.contains(normalizeSymbol(r.symbol))) {
                r.pinned = true;
                out.add(r);
            }
        }
        for (Row r : source) {
            if (r == null) continue;
            String sym = normalizeSymbol(r.symbol);
            if (!pinned.contains(sym)) {
                r.pinned = false;
                out.add(r);
            }
        }
        return out;
    }

    private void togglePin(@NonNull String symbol) {
        String sym = normalizeSymbol(symbol);
        if (TextUtils.isEmpty(sym)) return;
        List<String> pinned = loadPinnedSymbols();
        if (pinned.contains(sym)) {
            pinned.remove(sym);
            Toast.makeText(this, "పిన్ తీసివేయబడింది", Toast.LENGTH_SHORT).show();
        } else {
            if (pinned.size() >= 20) {
                Toast.makeText(this, "గరిష్టం 20 పిన్లు", Toast.LENGTH_SHORT).show();
                return;
            }
            pinned.add(0, sym);
            Toast.makeText(this, "పిన్ చేయబడింది", Toast.LENGTH_SHORT).show();
        }
        savePinnedSymbols(pinned);
        if (!searching) {
            loadWatchlist();
        } else {
            List<Row> current = new ArrayList<>();
            for (ListItem it : items) {
                if (it.type == ListItem.TYPE_ROW && it.row != null) {
                    it.row.pinned = pinned.contains(normalizeSymbol(it.row.symbol));
                    current.add(it.row);
                }
            }
            publishRows(sortPinnedFirst(current), true);
        }
    }

    @NonNull
    private List<String> loadPinnedSymbols() {
        List<String> out = new ArrayList<>();
        String raw = SharedPrefsData.getString(this, Constants.STOCK_PINNED_SYMBOLS, Constants.PREF_NAME);
        if (TextUtils.isEmpty(raw)) return out;
        for (String part : raw.split(",")) {
            String s = normalizeSymbol(part);
            if (!TextUtils.isEmpty(s) && !out.contains(s)) out.add(s);
        }
        return out;
    }

    private void savePinnedSymbols(@NonNull List<String> symbols) {
        SharedPrefsData.putString(this, Constants.STOCK_PINNED_SYMBOLS,
                TextUtils.join(",", symbols), Constants.PREF_NAME);
    }

    @NonNull
    private static String normalizeSymbol(@Nullable String s) {
        return s == null ? "" : s.trim().toUpperCase(Locale.US);
    }

    private void paintGsonIndex(@NonNull JsonObject q,
                                @Nullable TextView priceTv,
                                @Nullable TextView changeTv,
                                @Nullable StockSparklineView spark) {
        if (priceTv == null || changeTv == null) return;
        if (!q.has("price") || q.get("price").isJsonNull()) return;
        double price = q.get("price").getAsDouble();
        priceTv.setText(formatPrice(price));
        double ch = q.has("change") && !q.get("change").isJsonNull()
                ? q.get("change").getAsDouble() : 0;
        double pct = q.has("change_percent") && !q.get("change_percent").isJsonNull()
                ? q.get("change_percent").getAsDouble() : 0;
        applyChangeChip(changeTv, ch, pct);
        if (spark != null) spark.setPoints(sparkFromJson(q));
    }

    private static void applyChangeChip(@NonNull TextView changeTv, double ch, double pct) {
        boolean up = ch >= 0;
        changeTv.setText(String.format(Locale.US, "%s %.2f (%.2f%%)",
                up ? "▲" : "▼", Math.abs(ch), Math.abs(pct)));
        changeTv.setTextColor(up ? COLOR_UP : COLOR_DOWN);
        changeTv.setBackgroundResource(up
                ? R.drawable.bg_stock_change_up
                : R.drawable.bg_stock_change_down);
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
        r.sparkline = sparkFromJson(q);
        if (TextUtils.isEmpty(r.symbol)) return null;
        return r;
    }

    @Nullable
    private static float[] sparkFromJson(@NonNull JsonObject q) {
        if (!q.has("sparkline") || !q.get("sparkline").isJsonArray()) return null;
        JsonArray arr = q.getAsJsonArray("sparkline");
        List<Float> pts = new ArrayList<>();
        for (JsonElement el : arr) {
            try {
                if (el == null || el.isJsonNull()) continue;
                float v = el.getAsFloat();
                if (Float.isFinite(v) && v > 0f) pts.add(v);
            } catch (Exception ignored) {
            }
        }
        if (pts.size() < 2) return null;
        float[] out = new float[pts.size()];
        for (int i = 0; i < pts.size(); i++) out[i] = pts.get(i);
        return out;
    }

    @Nullable
    private static float[] sparkFromOrgJson(@NonNull org.json.JSONObject q) {
        try {
            if (!q.has("sparkline")) return null;
            org.json.JSONArray arr = q.optJSONArray("sparkline");
            if (arr == null || arr.length() < 2) return null;
            List<Float> pts = new ArrayList<>();
            for (int i = 0; i < arr.length(); i++) {
                double v = arr.optDouble(i, Double.NaN);
                if (Double.isFinite(v) && v > 0) pts.add((float) v);
            }
            if (pts.size() < 2) return null;
            float[] out = new float[pts.size()];
            for (int i = 0; i < pts.size(); i++) out[i] = pts.get(i);
            return out;
        } catch (Exception e) {
            return null;
        }
    }

    private interface BodyConsumer {
        void accept(@Nullable JsonObject body);
    }

    private interface PinListener {
        void onToggle(@NonNull String symbol);
    }

    private void enqueueIndices(@Nullable Call<JsonObject> call, @NonNull BodyConsumer consumer) {
        if (call == null) return;
        if (indicesCall != null) indicesCall.cancel();
        indicesCall = call;
        enqueueShared(call, consumer, false);
    }

    private void enqueueList(@Nullable Call<JsonObject> call, @NonNull BodyConsumer consumer) {
        if (call == null) return;
        if (listCall != null) listCall.cancel();
        listCall = call;
        enqueueShared(call, consumer, true);
    }

    private void enqueueShared(@NonNull Call<JsonObject> call,
                               @NonNull BodyConsumer consumer,
                               boolean showEmptyOnFail) {
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> c, @NonNull Response<JsonObject> response) {
                if (isFinishing()) return;
                if (!response.isSuccessful() || response.body() == null) {
                    if (showEmptyOnFail && empty != null && !hasRowItems()) {
                        empty.setVisibility(View.VISIBLE);
                    }
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
                if (showEmptyOnFail && empty != null && !hasRowItems()) {
                    empty.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private boolean hasRowItems() {
        for (ListItem it : items) {
            if (it.type == ListItem.TYPE_ROW) return true;
        }
        return false;
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

    @NonNull
    private static String tickerInitial(@Nullable String symbol) {
        if (TextUtils.isEmpty(symbol)) return "?";
        String s = symbol.trim();
        int dot = s.indexOf('.');
        if (dot > 0) s = s.substring(0, dot);
        if (s.isEmpty()) return "?";
        return String.valueOf(Character.toUpperCase(s.charAt(0)));
    }

    private static final class Row {
        String symbol;
        String name;
        double price;
        double change;
        double changePct;
        boolean pinned;
        @Nullable float[] sparkline;
    }

    private static final class ListItem {
        static final int TYPE_HEADER = 0;
        static final int TYPE_ROW = 1;
        final int type;
        @Nullable final String header;
        @Nullable final Row row;

        private ListItem(int type, @Nullable String header, @Nullable Row row) {
            this.type = type;
            this.header = header;
            this.row = row;
        }

        static ListItem header(@NonNull String title) {
            return new ListItem(TYPE_HEADER, title, null);
        }

        static ListItem row(@NonNull Row r) {
            return new ListItem(TYPE_ROW, null, r);
        }
    }

    private static final class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final List<ListItem> data;
        private final PinListener pinListener;

        Adapter(List<ListItem> data, PinListener pinListener) {
            this.data = data;
            this.pinListener = pinListener;
        }

        @Override
        public int getItemViewType(int position) {
            return data.get(position).type;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            if (viewType == ListItem.TYPE_HEADER) {
                View v = inflater.inflate(R.layout.item_stock_section_header, parent, false);
                return new HeaderVH(v);
            }
            View v = inflater.inflate(R.layout.item_stock_row, parent, false);
            return new RowVH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ListItem item = data.get(position);
            if (holder instanceof HeaderVH) {
                ((HeaderVH) holder).title.setText(item.header != null ? item.header : "");
                return;
            }
            if (!(holder instanceof RowVH) || item.row == null) return;
            RowVH h = (RowVH) holder;
            Row r = item.row;
            h.symbol.setText(r.symbol);
            h.name.setText(TextUtils.isEmpty(r.name) ? r.symbol : r.name);
            h.price.setText(formatPrice(r.price));
            if (h.avatar != null) h.avatar.setText(tickerInitial(r.symbol));
            applyChangeChip(h.change, r.change, r.changePct);
            if (h.spark != null) h.spark.setPoints(r.sparkline);
            if (h.pin != null) {
                h.pin.setImageResource(r.pinned
                        ? R.drawable.ic_action_bookmark
                        : R.drawable.ic_action_bookmark_outline);
                h.pin.setColorFilter(Color.parseColor(r.pinned ? "#FFA5B4FC" : "#FF64748B"));
                h.pin.setOnClickListener(v -> {
                    if (pinListener != null && !TextUtils.isEmpty(r.symbol)) {
                        pinListener.onToggle(r.symbol);
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        static final class HeaderVH extends RecyclerView.ViewHolder {
            final TextView title;

            HeaderVH(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.stock_section_title);
            }
        }

        static final class RowVH extends RecyclerView.ViewHolder {
            final TextView symbol;
            final TextView name;
            final TextView price;
            final TextView change;
            @Nullable final TextView avatar;
            @Nullable final StockSparklineView spark;
            @Nullable final ImageView pin;

            RowVH(@NonNull View itemView) {
                super(itemView);
                symbol = itemView.findViewById(R.id.stock_row_symbol);
                name = itemView.findViewById(R.id.stock_row_name);
                price = itemView.findViewById(R.id.stock_row_price);
                change = itemView.findViewById(R.id.stock_row_change);
                avatar = itemView.findViewById(R.id.stock_row_avatar);
                spark = itemView.findViewById(R.id.stock_row_spark);
                pin = itemView.findViewById(R.id.stock_row_pin);
            }
        }
    }
}
