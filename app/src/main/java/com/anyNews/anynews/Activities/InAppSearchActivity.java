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
import com.anyNews.anynews.Utils.SearchFuzzyHelper;
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
 * Typo intent: local edit-distance against recent (+ last suggest texts) with no
 * extra API; empty state paints quickly after a short timeout if the server is slow.
 */
public class InAppSearchActivity extends AppCompatActivity {

    public static final String EXTRA_QUERY = "extra_search_query";

    private static final long SUGGEST_DEBOUNCE_MS = 180L;
    /** Show empty / fuzzy hint if Suggestions still pending after this. */
    private static final long EMPTY_HINT_MS = 650L;

    private View root;
    private EditText searchInput;
    private TextView noResults;
    private TextView examples;
    private RecyclerView listView;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable pendingSuggest;
    private Runnable pendingEmptyHint;
    private Call<EncreptionRequest> suggestCall;
    private String lastSuggestQuery = "";
    private final List<Row> rows = new ArrayList<>();
    /** Last server suggestion texts — reused for local fuzzy (no extra call). */
    private final List<String> lastSuggestTexts = new ArrayList<>();
    private SearchRowsAdapter adapter;
    private final List<SearchModel.Datum> categories = new ArrayList<>();
    private int suggestSeq = 0;

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
                        // Instant local fuzzy (recent + last suggest) — no network wait.
                        paintLocalFuzzyWhileWaiting(q);
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
        final int seq = ++suggestSeq;
        pendingSuggest =
                () -> {
                    pendingSuggest = null;
                    if (seq != suggestSeq) return;
                    fetchSuggestions(q);
                };
        handler.postDelayed(pendingSuggest, SUGGEST_DEBOUNCE_MS);
        pendingEmptyHint =
                () -> {
                    pendingEmptyHint = null;
                    if (seq != suggestSeq) return;
                    String typed =
                            searchInput.getText() != null
                                    ? searchInput.getText().toString().trim()
                                    : "";
                    if (!typed.equals(q)) return;
                    // Still waiting on network — show empty / fuzzy so UI feels instant.
                    if (!hasStorySuggestRows()) {
                        paintEmptyOrFuzzy(q);
                    }
                };
        handler.postDelayed(pendingEmptyHint, EMPTY_HINT_MS);
    }

    private void cancelSuggest() {
        suggestSeq++;
        if (pendingSuggest != null) {
            handler.removeCallbacks(pendingSuggest);
            pendingSuggest = null;
        }
        if (pendingEmptyHint != null) {
            handler.removeCallbacks(pendingEmptyHint);
            pendingEmptyHint = null;
        }
        if (suggestCall != null) {
            suggestCall.cancel();
            suggestCall = null;
        }
    }

    private boolean hasStorySuggestRows() {
        for (Row r : rows) {
            if (r.type == Row.TYPE_SUGGEST || r.type == Row.TYPE_FUZZY) return true;
        }
        return false;
    }

    private void paintLocalFuzzyWhileWaiting(@NonNull String q) {
        List<String> pool = new ArrayList<>();
        for (RecentSearchStore.Entry e : RecentSearchStore.loadEntries(this)) {
            if (e != null && !TextUtils.isEmpty(e.term)) pool.add(e.term);
        }
        pool.addAll(lastSuggestTexts);
        List<String> fuzzy = SearchFuzzyHelper.closeMatches(q, pool, 4);
        if (fuzzy.isEmpty()) return;
        List<Row> next = new ArrayList<>();
        List<Row> duRows = buildDailyUpdatesSuggestRows(q);
        if (!duRows.isEmpty()) {
            next.add(Row.header(getString(R.string.search_daily_updates_section)));
            next.addAll(duRows);
        }
        next.add(Row.header(getString(R.string.search_did_you_mean)));
        for (String f : fuzzy) {
            next.add(Row.fuzzy(f));
        }
        noResults.setVisibility(View.GONE);
        rows.clear();
        rows.addAll(next);
        adapter.notifyDataSetChanged();
    }

    private void paintEmptyOrFuzzy(@NonNull String q) {
        List<String> pool = new ArrayList<>();
        for (RecentSearchStore.Entry e : RecentSearchStore.loadEntries(this)) {
            if (e != null && !TextUtils.isEmpty(e.term)) pool.add(e.term);
        }
        pool.addAll(lastSuggestTexts);
        List<String> fuzzy = SearchFuzzyHelper.closeMatches(q, pool, 5);
        if (!fuzzy.isEmpty()) {
            List<Row> next = new ArrayList<>();
            next.add(Row.header(getString(R.string.search_did_you_mean)));
            for (String f : fuzzy) {
                next.add(Row.fuzzy(f));
            }
            noResults.setVisibility(View.GONE);
            rows.clear();
            rows.addAll(next);
            adapter.notifyDataSetChanged();
            return;
        }
        noResults.setVisibility(View.VISIBLE);
        // Keep idle recent/categories underneath the banner.
        if (!hasStorySuggestRows()) {
            showIdleState();
            noResults.setVisibility(View.VISIBLE);
        }
    }

    private void showIdleState() {
        rows.clear();
        List<RecentSearchStore.Entry> recent = RecentSearchStore.loadEntries(this);
        if (!recent.isEmpty()) {
            rows.add(Row.header(getString(R.string.recent_searches)));
            for (RecentSearchStore.Entry r : recent) {
                if (r == null || TextUtils.isEmpty(r.term)) continue;
                rows.add(Row.recent(r.term, r.postId));
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
                                            long postId = 0L;
                                            try {
                                                if (o.has("post_id") && !o.get("post_id").isJsonNull()) {
                                                    postId = o.get("post_id").getAsLong();
                                                }
                                            } catch (Exception ignored) {
                                                postId = 0L;
                                            }
                                            // Only honor post_id on true singles (count 1).
                                            if (count != 1) {
                                                postId = 0L;
                                            }
                                            if (!TextUtils.isEmpty(text)) {
                                                next.add(Row.suggest(text, count, q, postId));
                                                lastSuggestTexts.add(text);
                                            }
                                        }
                                    }
                                }
                                // Cap cached suggest texts for local fuzzy reuse.
                                while (lastSuggestTexts.size() > 40) {
                                    lastSuggestTexts.remove(0);
                                }
                                boolean hasStory = false;
                                for (Row r : next) {
                                    if (r.type == Row.TYPE_SUGGEST) {
                                        hasStory = true;
                                        break;
                                    }
                                }
                                if (!hasStory) {
                                    // Local fuzzy first (recent + prior suggests) — no extra call.
                                    paintEmptyOrFuzzy(q);
                                    if (!duRows.isEmpty() && rows.isEmpty()) {
                                        paintDailyUpdatesOnly(q, duRows);
                                    }
                                    return;
                                }
                                noResults.setVisibility(View.GONE);
                                // Append Did-you-mean under results when typed looks like a typo.
                                List<String> pool = new ArrayList<>(lastSuggestTexts);
                                for (RecentSearchStore.Entry e :
                                        RecentSearchStore.loadEntries(InAppSearchActivity.this)) {
                                    if (e != null && !TextUtils.isEmpty(e.term)) {
                                        pool.add(e.term);
                                    }
                                }
                                List<String> fuzzy =
                                        SearchFuzzyHelper.closeMatches(q, pool, 3);
                                if (!fuzzy.isEmpty()) {
                                    next.add(Row.header(getString(R.string.search_did_you_mean)));
                                    for (String f : fuzzy) {
                                        boolean dup = false;
                                        for (Row r : next) {
                                            if (r.type == Row.TYPE_SUGGEST
                                                    && r.title != null
                                                    && r.title.equalsIgnoreCase(f)) {
                                                dup = true;
                                                break;
                                            }
                                        }
                                        if (!dup) next.add(Row.fuzzy(f));
                                    }
                                }
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
                                String typed =
                                        searchInput.getText() != null
                                                ? searchInput.getText().toString().trim()
                                                : "";
                                if (typed.equals(q)) {
                                    paintEmptyOrFuzzy(q);
                                }
                                if (!duRows.isEmpty() && !hasStorySuggestRows()) {
                                    paintDailyUpdatesOnly(q, duRows);
                                }
                            }
                        }
                    });
        } catch (Exception e) {
            if (!duRows.isEmpty()) {
                paintDailyUpdatesOnly(q, duRows);
            } else {
                paintEmptyOrFuzzy(q);
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
        openSearch(q, 0L);
    }

    /** Keyword FlipView search (multi-hit + Continue-reading hub). */
    private void openSearch(String q, long postId) {
        openSearch(q, postId, postId > 0L);
    }

    /**
     * @param singleOnly count=1 suggestion: at most one FlipView hit + hub
     *                   (even when server has not yet returned post_id).
     */
    private void openSearch(String q, long postId, boolean singleOnly) {
        String clean = softenClientQuery(q);
        if (clean.isEmpty() && postId <= 0L) return;
        if (!clean.isEmpty()) {
            RecentSearchStore.add(this, clean, postId > 0L ? postId : 0L);
        }
        Intent intent = new Intent(this, HomePage.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if (!clean.isEmpty()) {
            intent.putExtra(HomePage.EXTRA_SEARCH_QUERY, clean);
        }
        if (postId > 0L) {
            intent.putExtra(HomePage.EXTRA_SEARCH_POST_ID, postId);
        }
        if (singleOnly || postId > 0L) {
            intent.putExtra(HomePage.EXTRA_SEARCH_SINGLE_ONLY, true);
        }
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
        static final int TYPE_FUZZY = 5;

        final int type;
        final String title;
        final int meta;
        final int categoryId;
        final String highlight;
        final long postId;
        @Nullable final DailyUpdatesSearchSuggest.Match duMatch;

        private Row(
                int type,
                String title,
                int meta,
                int categoryId,
                String highlight,
                long postId,
                @Nullable DailyUpdatesSearchSuggest.Match duMatch) {
            this.type = type;
            this.title = title;
            this.meta = meta;
            this.categoryId = categoryId;
            this.highlight = highlight;
            this.postId = postId;
            this.duMatch = duMatch;
        }

        static Row header(String t) {
            return new Row(TYPE_HEADER, t, 0, 0, null, 0L, null);
        }

        static Row recent(String t) {
            return recent(t, 0L);
        }

        static Row recent(String t, long postId) {
            return new Row(TYPE_RECENT, t, 0, 0, null, postId, null);
        }

        static Row category(String t, int id) {
            return new Row(TYPE_CATEGORY, t, 0, id, null, 0L, null);
        }

        static Row suggest(String t, int count, String highlight) {
            return suggest(t, count, highlight, 0L);
        }

        static Row suggest(String t, int count, String highlight, long postId) {
            return new Row(TYPE_SUGGEST, t, count, 0, highlight, postId, null);
        }

        static Row fuzzy(String t) {
            return new Row(TYPE_FUZZY, t, 0, 0, null, 0L, null);
        }

        static Row duCategory(@NonNull DailyUpdatesSearchSuggest.Match m) {
            return new Row(TYPE_DU_CATEGORY, m.title, 0, 0, null, 0L, m);
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
                // Count=1 + post_id → [article, Continue-reading hub] (not soft keyword multi-hit).
                vh.itemView.setOnClickListener(
                        v -> {
                            if (row.meta == 1) {
                                // Single-article row: that story + Continue-reading hub only.
                                openSearch(suggestSubmitText(row), row.postId, true);
                            } else {
                                submitQuery(suggestSubmitText(row));
                            }
                        });
            } else if (row.type == Row.TYPE_FUZZY) {
                vh.meta.setVisibility(View.GONE);
                vh.icon.setImageResource(R.drawable.ic_action_search);
                vh.action.setVisibility(View.GONE);
                vh.itemView.setOnClickListener(
                        v -> {
                            if (searchInput != null) {
                                searchInput.setText(row.title);
                                searchInput.setSelection(row.title != null ? row.title.length() : 0);
                            }
                            submitQuery(row.title);
                        });
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
                vh.itemView.setOnClickListener(
                        v -> {
                            // Recent of a single story (stored post_id) or long title →
                            // single-article FlipView, not unlimited keyword hub.
                            boolean single =
                                    row.postId > 0L
                                            || (row.title != null && row.title.trim().length() >= 28);
                            openSearch(row.title, row.postId, single);
                        });
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
