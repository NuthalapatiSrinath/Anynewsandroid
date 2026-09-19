package com.anyNews.anynews.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anyNews.anynews.BuildConfig;
import com.anyNews.anynews.Pojo.EncreptionRequest;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.DailyUpdatesSearchSuggest;
import com.anyNews.anynews.Utils.RecentSearchStore;
import com.anyNews.anynews.Utils.SearchRevealHelper;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.anyNews.anynews.models.SearchModel;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.anyNews.anynews.Utils.MyUtils.decrypt;
import static com.anyNews.anynews.Utils.MyUtils.encrypt;

/**
 * In-app Search: mist + teal chrome, recent + categories idle state,
 * debounced suggestions with counts while typing, submit opens HomePage search mode.
 */
public class InAppSearchActivity extends AppCompatActivity {

    public static final String EXTRA_QUERY = "extra_search_query";

    private static final long SUGGEST_DEBOUNCE_MS = 250L;

    private View root;
    private EditText searchInput;
    private TextView noResults;
    private TextView examples;
    private RecyclerView listView;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable pendingSuggest;
    private Call<EncreptionRequest> suggestCall;
    private String lastSuggestQuery = "";
    private final List<Row> rows = new ArrayList<>();
    private SearchRowsAdapter adapter;
    private final List<SearchModel.Datum> categories = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_app_search);
        root = findViewById(R.id.in_app_search_root);
        searchInput = findViewById(R.id.search_input);
        noResults = findViewById(R.id.search_no_results);
        examples = findViewById(R.id.search_examples);
        listView = findViewById(R.id.search_list);
        listView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SearchRowsAdapter();
        listView.setAdapter(adapter);

        findViewById(R.id.search_back_row).setOnClickListener(v -> closeSearch());
        findViewById(R.id.search_back).setOnClickListener(v -> closeSearch());

        searchInput.setOnEditorActionListener(
                (v, actionId, event) -> {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                        submitQuery(searchInput.getText() != null
                                ? searchInput.getText().toString()
                                : "");
                        return true;
                    }
                    return false;
                });
        searchInput.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}

                    @Override
                    public void afterTextChanged(Editable s) {
                        String q = s != null ? s.toString().trim() : "";
                        noResults.setVisibility(View.GONE);
                        if (examples != null) {
                            examples.setVisibility(q.isEmpty() ? View.VISIBLE : View.GONE);
                        }
                        if (q.isEmpty()) {
                            cancelSuggest();
                            showIdleState();
                            return;
                        }
                        scheduleSuggest(q);
                    }
                });

        SearchRevealHelper.playEnterReveal(this, root, getIntent());
        loadCategories();
        showIdleState();
        searchInput.requestFocus();
    }

    @Override
    public void onBackPressed() {
        closeSearch();
    }

    private void closeSearch() {
        cancelSuggest();
        SearchRevealHelper.playExitAndFinish(this, root, getIntent());
    }

    private void scheduleSuggest(final String q) {
        cancelSuggest();
        pendingSuggest =
                () -> {
                    pendingSuggest = null;
                    fetchSuggestions(q);
                };
        handler.postDelayed(pendingSuggest, SUGGEST_DEBOUNCE_MS);
    }

    private void cancelSuggest() {
        if (pendingSuggest != null) {
            handler.removeCallbacks(pendingSuggest);
            pendingSuggest = null;
        }
        if (suggestCall != null) {
            suggestCall.cancel();
            suggestCall = null;
        }
    }

    private void showIdleState() {
        rows.clear();
        List<String> recent = RecentSearchStore.load(this);
        if (!recent.isEmpty()) {
            rows.add(Row.header(getString(R.string.recent_searches)));
            for (String r : recent) {
                rows.add(Row.recent(r));
            }
        }
        if (!categories.isEmpty()) {
            rows.add(Row.header(getString(R.string.search_categories)));
            for (SearchModel.Datum c : categories) {
                if (c == null || TextUtils.isEmpty(c.getName())) continue;
                int cid = c.getCategoryId() != null ? c.getCategoryId() : 0;
                rows.add(Row.category(c.getName(), cid));
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void loadCategories() {
        try {
            final int langId =
                    SharedPrefsData.getInt(this, Constants.LanguageID, Constants.PREF_NAME);
            String cached =
                    com.anyNews.anynews.Utils.CategoriesDiskCache.loadIfMatches(this, langId);
            if (cached != null && !cached.isEmpty()) {
                SearchModel fromDisk = new Gson().fromJson(cached, SearchModel.class);
                if (fromDisk != null
                        && Boolean.TRUE.equals(fromDisk.getSuccess())
                        && fromDisk.getData() != null) {
                    categories.clear();
                    categories.addAll(fromDisk.getData());
                    showIdleState();
                    return;
                }
            }
        } catch (Exception ignored) {
        }

        try {
            JsonObject body = new JsonObject();
            JsonObject req = new JsonObject();
            req.addProperty(
                    "draft_id",
                    SharedPrefsData.getInt(this, Constants.DraftID, Constants.PREF_NAME));
            req.addProperty(
                    "language_id",
                    SharedPrefsData.getInt(this, Constants.LanguageID, Constants.PREF_NAME));
            req.addProperty("screen_type", 0);
            String enc = encrypt(new Gson().toJson(req));
            body.addProperty("data", enc);
            APIService api = RestApiClient.getClient().create(APIService.class);
            api.List_All_Categories("v3", body)
                    .enqueue(
                            new Callback<EncreptionRequest>() {
                                @Override
                                public void onResponse(
                                        Call<EncreptionRequest> call,
                                        Response<EncreptionRequest> response) {
                                    try {
                                        if (response.body() == null
                                                || response.body().getData() == null) {
                                            return;
                                        }
                                        String raw = decrypt(response.body().getData());
                                        SearchModel model =
                                                new Gson().fromJson(raw, SearchModel.class);
                                        if (model != null
                                                && Boolean.TRUE.equals(model.getSuccess())
                                                && model.getData() != null) {
                                            categories.clear();
                                            categories.addAll(model.getData());
                                            if (TextUtils.isEmpty(
                                                    searchInput.getText() != null
                                                            ? searchInput.getText().toString()
                                                            : "")) {
                                                showIdleState();
                                            }
                                        }
                                    } catch (Exception ignored) {
                                    }
                                }

                                @Override
                                public void onFailure(Call<EncreptionRequest> call, Throwable t) {}
                            });
        } catch (Exception ignored) {
        }
    }

    private void fetchSuggestions(final String q) {
        if (q.equals(lastSuggestQuery)
                && !rows.isEmpty()
                && (rows.get(0).type == Row.TYPE_SUGGEST
                        || rows.get(0).type == Row.TYPE_HEADER)) {
            // Allow re-paint when only DU header was showing; skip identical story burst.
            boolean hasStorySuggest = false;
            for (Row r : rows) {
                if (r.type == Row.TYPE_SUGGEST) {
                    hasStorySuggest = true;
                    break;
                }
            }
            if (hasStorySuggest) {
                return;
            }
        }
        lastSuggestQuery = q;
        // Daily Updates category matches first (local, instant) while story API runs.
        final List<Row> duRows = buildDailyUpdatesSuggestRows(q);
        try {
            JsonObject req = new JsonObject();
            req.addProperty(
                    "draft_id",
                    SharedPrefsData.getInt(this, Constants.DraftID, Constants.PREF_NAME));
            req.addProperty(
                    "language_id",
                    SharedPrefsData.getInt(this, Constants.LanguageID, Constants.PREF_NAME));
            req.addProperty("search_text", q);
            req.addProperty("limit", 12);
            String enc = encrypt(new Gson().toJson(req));
            JsonObject body = new JsonObject();
            body.addProperty("data", enc);
            APIService api = RestApiClient.getClient().create(APIService.class);
            suggestCall = api.Search_Suggestions("v3", body);
            suggestCall.enqueue(
                    new Callback<EncreptionRequest>() {
                        @Override
                        public void onResponse(
                                Call<EncreptionRequest> call,
                                Response<EncreptionRequest> response) {
                            if (isFinishing() || call.isCanceled()) return;
                            String typed =
                                    searchInput.getText() != null
                                            ? searchInput.getText().toString().trim()
                                            : "";
                            if (!typed.equals(q)) return;
                            try {
                                List<Row> next = new ArrayList<>();
                                if (!duRows.isEmpty()) {
                                    next.add(Row.header(getString(R.string.search_daily_updates_section)));
                                    next.addAll(duRows);
                                }
                                if (response.body() != null
                                        && response.body().getData() != null) {
                                    String raw = decrypt(response.body().getData());
                                    JsonObject obj = new Gson().fromJson(raw, JsonObject.class);
                                    JsonArray data =
                                            obj != null && obj.has("data")
                                                    ? obj.getAsJsonArray("data")
                                                    : null;
                                    if (data != null) {
                                        for (JsonElement el : data) {
                                            if (el == null || !el.isJsonObject()) continue;
                                            JsonObject o = el.getAsJsonObject();
                                            String text =
                                                    o.has("text") ? o.get("text").getAsString() : "";
                                            int count =
                                                    o.has("count") ? o.get("count").getAsInt() : 0;
                                            if (!TextUtils.isEmpty(text)) {
                                                next.add(Row.suggest(text, count, q));
                                            }
                                        }
                                    }
                                }
                                if (next.isEmpty()) {
                                    // Keep recent+categories instead of harsh empty; soft banner.
                                    noResults.setVisibility(View.VISIBLE);
                                    showIdleState();
                                    return;
                                }
                                noResults.setVisibility(View.GONE);
                                rows.clear();
                                rows.addAll(next);
                                adapter.notifyDataSetChanged();
                            } catch (Exception e) {
                                paintDailyUpdatesOnly(q, duRows);
                            }
                        }

                        @Override
                        public void onFailure(Call<EncreptionRequest> call, Throwable t) {
                            if (!call.isCanceled()) {
                                if (!duRows.isEmpty()) {
                                    paintDailyUpdatesOnly(q, duRows);
                                } else {
                                    noResults.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                    });
        } catch (Exception e) {
            if (!duRows.isEmpty()) {
                paintDailyUpdatesOnly(q, duRows);
            } else {
                noResults.setVisibility(View.VISIBLE);
            }
        }
    }

    @NonNull
    private List<Row> buildDailyUpdatesSuggestRows(@Nullable String q) {
        List<Row> out = new ArrayList<>();
        try {
            for (DailyUpdatesSearchSuggest.Match m :
                    DailyUpdatesSearchSuggest.matches(this, q)) {
                out.add(Row.duCategory(m));
            }
        } catch (Exception ignored) {
        }
        return out;
    }

    private void paintDailyUpdatesOnly(
            @NonNull String q, @NonNull List<Row> duRows) {
        String typed =
                searchInput.getText() != null
                        ? searchInput.getText().toString().trim()
                        : "";
        if (!typed.equals(q) || duRows.isEmpty()) {
            noResults.setVisibility(View.VISIBLE);
            return;
        }
        noResults.setVisibility(View.GONE);
        rows.clear();
        rows.add(Row.header(getString(R.string.search_daily_updates_section)));
        rows.addAll(duRows);
        adapter.notifyDataSetChanged();
    }

    private void openDailyUpdatesMatch(@NonNull DailyUpdatesSearchSuggest.Match match) {
        try {
            Intent intent = DailyUpdatesSearchSuggest.launchIntent(this, match);
            if (getApplicationContext() != null
                    && getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)) {
                startActivity(intent);
            }
            finish();
            overridePendingTransition(0, 0);
        } catch (Exception e) {
            submitQuery(match.title);
        }
    }

    private void submitQuery(String q) {
        String clean = softenClientQuery(q);
        if (clean.isEmpty()) return;
        RecentSearchStore.add(this, clean);
        Intent intent = new Intent(this, HomePage.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(HomePage.EXTRA_SEARCH_QUERY, clean);
        // Circular reveal into search results (same origin as opening search when available).
        Intent self = getIntent();
        if (self != null && self.getBooleanExtra(SearchRevealHelper.EXTRA_HAS_REVEAL, false)) {
            intent.putExtra(SearchRevealHelper.EXTRA_HAS_REVEAL, true);
            intent.putExtra(
                    SearchRevealHelper.EXTRA_REVEAL_CX,
                    self.getIntExtra(SearchRevealHelper.EXTRA_REVEAL_CX, 0));
            intent.putExtra(
                    SearchRevealHelper.EXTRA_REVEAL_CY,
                    self.getIntExtra(SearchRevealHelper.EXTRA_REVEAL_CY, 0));
        } else {
            // Fall back to center of the tapped suggestion / screen.
            View anchor = listView != null ? listView : root;
            if (anchor != null) {
                int[] loc = new int[2];
                anchor.getLocationOnScreen(loc);
                intent.putExtra(SearchRevealHelper.EXTRA_HAS_REVEAL, true);
                intent.putExtra(
                        SearchRevealHelper.EXTRA_REVEAL_CX,
                        loc[0] + anchor.getWidth() / 2);
                intent.putExtra(
                        SearchRevealHelper.EXTRA_REVEAL_CY,
                        loc[1] + Math.min(anchor.getHeight() / 3, 200));
            }
        }
        if (getApplicationContext() != null
                && getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)) {
            startActivity(intent);
        }
        finish();
        overridePendingTransition(0, 0);
    }

    /** Same path as Profile category chips → HomePage EXTRA_SESSION_ID. */
    private void openCategory(int categoryId) {
        if (categoryId <= 0) return;
        try {
            SharedPrefsData.putInt(this, Constants.CategoryID, categoryId, Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
        Intent intent = new Intent(this, HomePage.class);
        intent.putExtra("EXTRA_SESSION_ID", "" + categoryId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if (getApplicationContext() != null
                && getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)) {
            startActivity(intent);
        }
        finish();
        overridePendingTransition(0, 0);
    }

    /** Align FlipView search_text with Suggestions / server normalizeQuery. */
    private static String softenClientQuery(String q) {
        String clean = q != null ? q.trim() : "";
        clean = clean.replace("\u2026", " ").replace("...", " ").replace('\u00A0', ' ');
        clean = clean.replaceAll("^[\\?\\[\\](){}:;!.,\\s\\-\\u2026]+|[\\?\\[\\](){}:;!.,\\s\\-\\u2026]+$", "");
        clean = clean.replaceAll("\\s+", " ").trim();
        clean = clean.replaceAll("\\.{2,}", " ").replaceAll("\\s+", " ").trim();
        return clean;
    }

    private static String suggestSubmitText(Row row) {
        if (row == null) return "";
        String title = row.title != null ? row.title : "";
        boolean truncated =
                title.indexOf('\u2026') >= 0 || title.contains("...");
        // Truncated phrase → search the typed keyword (highlight), not the snippet.
        if (truncated && !TextUtils.isEmpty(row.highlight)) {
            return softenClientQuery(row.highlight);
        }
        // Keyword bucket row (text == typed query) → use typed query.
        if (!TextUtils.isEmpty(row.highlight)
                && title.trim().equalsIgnoreCase(row.highlight.trim())) {
            return softenClientQuery(row.highlight);
        }
        return softenClientQuery(title);
    }

    private static final class Row {
        static final int TYPE_HEADER = 0;
        static final int TYPE_RECENT = 1;
        static final int TYPE_CATEGORY = 2;
        static final int TYPE_SUGGEST = 3;
        static final int TYPE_DU_CATEGORY = 4;

        final int type;
        final String title;
        final int meta;
        final int categoryId;
        final String highlight;
        @Nullable final DailyUpdatesSearchSuggest.Match duMatch;

        private Row(
                int type,
                String title,
                int meta,
                int categoryId,
                String highlight,
                @Nullable DailyUpdatesSearchSuggest.Match duMatch) {
            this.type = type;
            this.title = title;
            this.meta = meta;
            this.categoryId = categoryId;
            this.highlight = highlight;
            this.duMatch = duMatch;
        }

        static Row header(String t) {
            return new Row(TYPE_HEADER, t, 0, 0, null, null);
        }

        static Row recent(String t) {
            return new Row(TYPE_RECENT, t, 0, 0, null, null);
        }

        static Row category(String t, int id) {
            return new Row(TYPE_CATEGORY, t, 0, id, null, null);
        }

        static Row suggest(String t, int count, String highlight) {
            return new Row(TYPE_SUGGEST, t, count, 0, highlight, null);
        }

        static Row duCategory(@NonNull DailyUpdatesSearchSuggest.Match m) {
            return new Row(TYPE_DU_CATEGORY, m.title, 0, 0, null, m);
        }
    }

    private class SearchRowsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @Override
        public int getItemViewType(int position) {
            return rows.get(position).type;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == Row.TYPE_HEADER) {
                TextView tv = new TextView(parent.getContext());
                tv.setLayoutParams(
                        new RecyclerView.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT));
                int pad = (int) (12 * getResources().getDisplayMetrics().density);
                tv.setPadding(pad + 4, pad, pad, pad / 2);
                tv.setTextColor(0xFF757575);
                tv.setTextSize(13f);
                tv.setTypeface(tv.getTypeface(), android.graphics.Typeface.BOLD);
                return new RecyclerView.ViewHolder(tv) {};
            }
            View v =
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_in_app_search_row, parent, false);
            return new RowVH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            final Row row = rows.get(position);
            if (row.type == Row.TYPE_HEADER) {
                ((TextView) holder.itemView).setText(row.title);
                return;
            }
            RowVH vh = (RowVH) holder;
            if (row.type == Row.TYPE_SUGGEST && !TextUtils.isEmpty(row.highlight)) {
                vh.title.setText(highlightMatch(row.title, row.highlight));
            } else {
                vh.title.setText(row.title);
            }
            if (row.type == Row.TYPE_SUGGEST) {
                vh.meta.setVisibility(View.VISIBLE);
                vh.meta.setText(String.valueOf(row.meta));
                vh.icon.setImageResource(R.drawable.ic_action_search);
                vh.action.setVisibility(View.GONE);
                // Prefer the typed needle when the row is a truncated snippet (…),
                // so FlipView search_text matches what Suggestions found.
                vh.itemView.setOnClickListener(
                        v -> submitQuery(suggestSubmitText(row)));
            } else if (row.type == Row.TYPE_DU_CATEGORY) {
                vh.meta.setVisibility(View.GONE);
                vh.icon.setImageResource(R.drawable.ic_action_more_vert);
                vh.action.setVisibility(View.VISIBLE);
                vh.action.setImageResource(R.drawable.ic_action_more_vert);
                vh.action.setRotation(270f);
                final DailyUpdatesSearchSuggest.Match match = row.duMatch;
                vh.itemView.setOnClickListener(
                        v -> {
                            if (match != null) {
                                openDailyUpdatesMatch(match);
                            }
                        });
            } else if (row.type == Row.TYPE_RECENT) {
                vh.meta.setVisibility(View.GONE);
                vh.icon.setImageResource(android.R.drawable.ic_menu_recent_history);
                vh.action.setVisibility(View.VISIBLE);
                vh.action.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
                vh.action.setOnClickListener(
                        v -> {
                            RecentSearchStore.remove(InAppSearchActivity.this, row.title);
                            showIdleState();
                        });
                vh.itemView.setOnClickListener(v -> submitQuery(row.title));
            } else {
                vh.meta.setVisibility(View.GONE);
                vh.icon.setImageResource(R.drawable.ic_action_more_vert);
                vh.action.setVisibility(View.VISIBLE);
                vh.action.setImageResource(R.drawable.ic_action_more_vert);
                vh.action.setRotation(270f);
                vh.itemView.setOnClickListener(v -> openCategory(row.categoryId));
            }
        }

        @Override
        public int getItemCount() {
            return rows.size();
        }
    }

    private static CharSequence highlightMatch(String title, String needle) {
        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(needle)) return title;
        SpannableString ss = new SpannableString(title);
        String lower = title.toLowerCase(Locale.ROOT);
        String n = needle.toLowerCase(Locale.ROOT);
        int idx = lower.indexOf(n);
        while (idx >= 0) {
            ss.setSpan(
                    new StyleSpan(android.graphics.Typeface.BOLD),
                    idx,
                    idx + n.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            idx = lower.indexOf(n, idx + n.length());
        }
        return ss;
    }

    private static class RowVH extends RecyclerView.ViewHolder {
        final ImageView icon;
        final TextView title;
        final TextView meta;
        final ImageView action;

        RowVH(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.row_icon);
            title = itemView.findViewById(R.id.row_title);
            meta = itemView.findViewById(R.id.row_meta);
            action = itemView.findViewById(R.id.row_action);
        }
    }
}
