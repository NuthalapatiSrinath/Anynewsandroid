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
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.KeyEvent;
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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anyNews.anynews.BuildConfig;
import com.anyNews.anynews.Pojo.EncreptionRequest;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.RecentSearchStore;
import com.anyNews.anynews.Utils.SearchFuzzyHelper;
import com.anyNews.anynews.Utils.SearchRevealHelper;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.anyNews.anynews.Utils.MyUtils.decrypt;
import static com.anyNews.anynews.Utils.MyUtils.encrypt;

/**
 * In-app Search: mist + teal chrome, recent-searches idle state.
 * Typing shows topic rows with counts (highest first) and, under them, the
 * matching stories (title, date, photo) from that same response. Tapping a
 * topic filters those stories in place. It does not open a second list.
 * Empty state only after the suggest request settles with zero hits — never
 * flash "No matching stories" while a request is still in flight.
 */
public class InAppSearchActivity extends AppCompatActivity {

    public static final String EXTRA_QUERY = "extra_search_query";
    /** HomePage bounced here after Search/Enter with zero FlipView hits. */
    public static final String EXTRA_SHOW_NO_RESULTS = "extra_search_show_no_results";

    private static final long SUGGEST_DEBOUNCE_MS = 80L;
    /** Topic rows with counts, stories listed under them. */
    private static final int MODE_TOPICS = 0;
    /** Article rows after a topic row is tapped. */
    private static final int MODE_ARTICLES = 1;

    private View root;
    private EditText searchInput;
    private View emptyState;
    private RecyclerView listView;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable pendingSuggest;
    private Call<EncreptionRequest> suggestCall;
    private String lastSuggestQuery = "";
    private final List<Row> rows = new ArrayList<>();
    /** Last server suggestion texts — reused for local fuzzy (no extra call). */
    private final List<String> lastSuggestTexts = new ArrayList<>();
    private SearchRowsAdapter adapter;
    private int suggestSeq = 0;
    /** True while a suggest XHR is outstanding for the current typed query. */
    private boolean suggestInFlight = false;
    /** Topics until a suggestion row is chosen; back returns here. */
    private int listMode = MODE_TOPICS;
    /** Topic tap filters the stories already on this screen. Empty = all. */
    private String focusedTopic = "";
    private final List<Row> topicRows = new ArrayList<>();
    private final List<Row> articleRows = new ArrayList<>();
    /** Article list pages inside the server recall cap. */
    private static final int ARTICLE_PAGE = 30;
    private boolean articleHasMore = false;
    private int articleNextSkip = 0;
    private int articleLayoutRetries = 0;
    private String articleQuery = "";
    private final TextWatcher queryWatcher =
            new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    String q = s != null ? s.toString().trim() : "";
                    // Editing the box clears a topic filter and refreshes this screen.
                    listMode = MODE_TOPICS;
                    focusedTopic = "";
                    resetArticlePaging();
                    dropArticleRows();
                    hideEmptyState();
                    if (q.isEmpty()) {
                        cancelSuggest();
                        showIdleState();
                        return;
                    }
                    scheduleSuggest(q);
                }
            };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_app_search);
        root = findViewById(R.id.in_app_search_root);
        searchInput = findViewById(R.id.search_input);
        // The field itself and the keyboard search action submit. No icon in the box.
        searchInput.setCompoundDrawablesRelative(null, null, null, null);
        emptyState = findViewById(R.id.search_empty_state);
        listView = findViewById(R.id.search_list);
        listView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SearchRowsAdapter();
        listView.setAdapter(adapter);
        listView.addOnScrollListener(
                new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                        if (dy <= 0) return;
                        maybeLoadMoreArticles();
                    }
                });

        findViewById(R.id.search_back_row).setOnClickListener(v -> onSearchBack());
        findViewById(R.id.search_back).setOnClickListener(v -> onSearchBack());

        searchInput.setOnEditorActionListener(
                (v, actionId, event) -> {
                    boolean enterDown =
                            event != null
                                    && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                                    && event.getAction() == KeyEvent.ACTION_DOWN;
                    if (actionId == EditorInfo.IME_ACTION_SEARCH
                            || actionId == EditorInfo.IME_ACTION_DONE
                            || actionId == EditorInfo.IME_ACTION_GO
                            || enterDown) {
                        submitQuery(searchInput.getText() != null
                                ? searchInput.getText().toString()
                                : "");
                        return true;
                    }
                    return false;
                });
        searchInput.addTextChangedListener(queryWatcher);

        SearchRevealHelper.playEnterReveal(this, root, getIntent());
        applyOpenExtras();
        searchInput.requestFocus();
    }

    /**
     * Prefill from {@link #EXTRA_QUERY} and optionally show empty/fuzzy (bounce from
     * HomePage when Search/Enter had zero FlipView hits).
     */
    private void applyOpenExtras() {
        Intent in = getIntent();
        String seed =
                in != null && in.hasExtra(EXTRA_QUERY) ? in.getStringExtra(EXTRA_QUERY) : null;
        boolean showEmpty =
                in != null && in.getBooleanExtra(EXTRA_SHOW_NO_RESULTS, false);
        if (in != null) {
            in.removeExtra(EXTRA_SHOW_NO_RESULTS);
        }
        if (TextUtils.isEmpty(seed)) {
            showIdleState();
            return;
        }
        String clean = seed.trim();
        // Avoid double scheduleSuggest from TextWatcher while we own the open path.
        searchInput.removeTextChangedListener(queryWatcher);
        searchInput.setText(clean);
        searchInput.setSelection(clean.length());
        searchInput.addTextChangedListener(queryWatcher);
        // One response: topic counts, then the stories under them.
        scheduleSuggest(clean);
        if (showEmpty && !hasStorySuggestRows()) {
            paintEmptyOrFuzzy(clean);
        }
    }

    private void showEmptyState() {
        if (emptyState != null) {
            emptyState.setVisibility(View.VISIBLE);
        }
        if (listView != null) {
            listView.setVisibility(View.INVISIBLE);
        }
    }

    private void hideEmptyState() {
        if (emptyState != null) {
            emptyState.setVisibility(View.GONE);
        }
        if (listView != null) {
            listView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        onSearchBack();
    }

    /** A topic filter clears first. The next back leaves search. */
    private void onSearchBack() {
        if (!TextUtils.isEmpty(focusedTopic)) {
            focusedTopic = "";
            renderMixed();
            return;
        }
        if (listMode == MODE_ARTICLES) {
            listMode = MODE_TOPICS;
            resetArticlePaging();
            dropArticleRows();
            String q =
                    searchInput != null && searchInput.getText() != null
                            ? searchInput.getText().toString().trim()
                            : "";
            if (q.isEmpty()) {
                showIdleState();
                return;
            }
            cancelSuggest();
            fetchSuggestions(q, false);
            return;
        }
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
        // Do NOT paint empty while the request is in flight — that caused the
        // yellow/centered "No Stories found" flash before late results arrived.
    }

    private void cancelSuggest() {
        suggestSeq++;
        suggestInFlight = false;
        if (pendingSuggest != null) {
            handler.removeCallbacks(pendingSuggest);
            pendingSuggest = null;
        }
        if (suggestCall != null) {
            suggestCall.cancel();
            suggestCall = null;
        }
    }

    private boolean hasStorySuggestRows() {
        for (Row r : rows) {
            if (r.type == Row.TYPE_SUGGEST
                    || r.type == Row.TYPE_ARTICLE
                    || r.type == Row.TYPE_FUZZY) {
                return true;
            }
        }
        return false;
    }

    private boolean hasTopicRows() {
        for (Row r : rows) {
            if (r.type == Row.TYPE_SUGGEST && r.topicBucket) return true;
        }
        return false;
    }

    /**
     * Same screen: topic counts on top, stories (title, date, photo) under them.
     * @param append true keeps the topics and adds the next story page
     */
    private void applyTopicRows(@Nullable JsonObject obj, @NonNull String q) {
        applyMixed(obj, q, false);
    }

    private void applyMixed(@Nullable JsonObject obj, @NonNull String q, boolean append) {
        boolean articleMode = isArticleSuggestMode(obj);
        if (!append) {
            if (!articleMode) topicRows.clear();
            articleRows.clear();
            if (!articleMode) {
                JsonArray data = jsonArray(obj, "data");
                if (data != null) {
                    for (JsonElement el : data) {
                        // A post_id row is one story, not a topic count.
                        if (readPostId(el) > 0L) continue;
                        Row topic = parseTopicRow(el);
                        if (topic == null) continue;
                        topicRows.add(topic);
                        if (!TextUtils.isEmpty(topic.title)) lastSuggestTexts.add(topic.title);
                    }
                }
                Collections.sort(
                        topicRows,
                        (a, b) -> {
                            int byCount = Integer.compare(b.meta, a.meta);
                            if (byCount != 0) return byCount;
                            int la = a.title != null ? a.title.length() : 0;
                            int lb = b.title != null ? b.title.length() : 0;
                            return Integer.compare(la, lb);
                        });
            }
        }
        int before = articleRows.size();
        ingestArticleElements(obj, q);
        int received = articleRows.size() - before;
        while (lastSuggestTexts.size() > 40) lastSuggestTexts.remove(0);
        articleQuery = q;
        if (topicRows.isEmpty() && articleRows.isEmpty()) {
            rows.clear();
            adapter.notifyDataSetChanged();
            paintLocalLiveSuggestions(q);
            if (!hasStorySuggestRows()) paintEmptyOrFuzzy(q);
            return;
        }
        renderMixed();
        noteArticlePage(obj, received);
    }

    private static boolean isArticleSuggestMode(@Nullable JsonObject obj) {
        if (obj == null || !obj.has("suggest_mode") || obj.get("suggest_mode").isJsonNull()) {
            return false;
        }
        try {
            return "articles".equalsIgnoreCase(obj.get("suggest_mode").getAsString());
        } catch (Exception ignored) {
            return false;
        }
    }

    @Nullable
    private static JsonArray jsonArray(@Nullable JsonObject obj, @NonNull String key) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) return null;
        JsonElement el = obj.get(key);
        return el.isJsonArray() ? el.getAsJsonArray() : null;
    }

    /** Stories from `articles` (and article-shaped `data` rows) onto this screen. */
    private void ingestArticleElements(@Nullable JsonObject obj, @NonNull String q) {
        if (obj == null) return;
        addArticleElements(jsonArray(obj, "articles"), q);
        addArticleElements(jsonArray(obj, "stories"), q);
        JsonArray data = jsonArray(obj, "data");
        if (data == null) return;
        for (JsonElement el : data) {
            if (readPostId(el) <= 0L) continue;
            addArticleElement(el, q);
        }
    }

    private void addArticleElements(@Nullable JsonArray arr, @NonNull String q) {
        if (arr == null) return;
        for (JsonElement el : arr) addArticleElement(el, q);
    }

    private void addArticleElement(@Nullable JsonElement el, @NonNull String q) {
        Row art;
        try {
            art = parseArticleRow(el, q);
        } catch (Exception ignored) {
            return;
        }
        if (art == null) return;
        if (art.postId > 0L && articleRowsHave(art.postId)) return;
        articleRows.add(art);
        if (!TextUtils.isEmpty(art.title)) lastSuggestTexts.add(art.title);
    }

    private static long readPostId(@Nullable JsonElement el) {
        if (el == null || !el.isJsonObject()) return 0L;
        JsonObject o = el.getAsJsonObject();
        try {
            if (o.has("post_id") && !o.get("post_id").isJsonNull()) {
                return o.get("post_id").getAsLong();
            }
        } catch (Exception ignored) {
            return 0L;
        }
        return 0L;
    }

    private boolean articleRowsHave(long postId) {
        for (Row r : articleRows) {
            if (r != null && r.type == Row.TYPE_ARTICLE && r.postId == postId) return true;
        }
        return false;
    }

    private boolean articleMatchesFocus(@NonNull Row art) {
        if (TextUtils.isEmpty(focusedTopic)) return true;
        if (art.topicLabel != null && art.topicLabel.equalsIgnoreCase(focusedTopic)) return true;
        if (art.title == null || focusedTopic == null) return false;
        return art.title.toLowerCase(Locale.ROOT).contains(focusedTopic.toLowerCase(Locale.ROOT));
    }

    private int visibleArticleCount() {
        int n = 0;
        for (Row art : articleRows) {
            if (art != null && art.type == Row.TYPE_ARTICLE && articleMatchesFocus(art)) n++;
        }
        return n;
    }

    /** Topics stay put. A focused topic only hides the other stories. */
    private void renderMixed() {
        List<Row> next = new ArrayList<>();
        next.addAll(topicRows);
        List<Row> stories = new ArrayList<>();
        for (Row art : articleRows) {
            if (art == null || art.type != Row.TYPE_ARTICLE) continue;
            if (!articleMatchesFocus(art)) continue;
            stories.add(art);
        }
        // A topic tap must not leave a blank list when the stories are already here.
        if (stories.isEmpty() && !articleRows.isEmpty() && !TextUtils.isEmpty(focusedTopic)) {
            for (Row art : articleRows) {
                if (art != null && art.type == Row.TYPE_ARTICLE) stories.add(art);
            }
        }
        next.addAll(stories);
        if (next.isEmpty()) {
            rows.clear();
            if (adapter != null) adapter.notifyDataSetChanged();
            return;
        }
        hideEmptyState();
        rows.clear();
        rows.addAll(next);
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    /** Show this topic's stories on this screen. A second tap shows every story again. */
    private void openTopic(@Nullable String topic) {
        String next = topic == null ? "" : topic.trim();
        if (next.isEmpty()) return;
        if (next.equalsIgnoreCase(focusedTopic) && visibleArticleCount() > 0) {
            focusedTopic = "";
            renderMixed();
            return;
        }
        focusedTopic = next;
        renderMixed();
        if (visibleArticleCount() == 0) {
            String typed =
                    searchInput != null && searchInput.getText() != null
                            ? searchInput.getText().toString().trim()
                            : "";
            String q = TextUtils.isEmpty(typed) ? next : typed;
            fetchSuggestions(q, true, 0, !topicRows.isEmpty());
        }
        if (listView == null || TextUtils.isEmpty(focusedTopic)) return;
        for (int i = 0; i < rows.size(); i++) {
            Row r = rows.get(i);
            if (r.topicBucket && focusedTopic.equalsIgnoreCase(r.title)) {
                listView.scrollToPosition(i);
                break;
            }
        }
    }

    private void dropArticleRows() {
        if (!hasArticleRows() || adapter == null) return;
        rows.clear();
        adapter.notifyDataSetChanged();
    }

    private boolean hasArticleRows() {
        for (Row r : rows) {
            if (r.type == Row.TYPE_ARTICLE) return true;
        }
        return false;
    }

    /** Build candidate pool: recent terms + last server suggestion texts. */
    @NonNull
    private List<String> localSuggestPool() {
        List<String> pool = new ArrayList<>();
        for (RecentSearchStore.Entry e : RecentSearchStore.loadEntries(this)) {
            if (e != null && !TextUtils.isEmpty(e.term)) pool.add(e.term);
        }
        pool.addAll(lastSuggestTexts);
        return pool;
    }

    /**
     * Local prefix/typo hints when the server returned nothing. Not used while
     * an article list for a chosen topic is on screen.
     */
    private void paintLocalLiveSuggestions(@NonNull String q) {
        if (listMode == MODE_ARTICLES) {
            return;
        }
        List<String> pool = localSuggestPool();
        List<String> live = SearchFuzzyHelper.livePrefixMatches(q, pool, 8);
        List<String> fuzzy = SearchFuzzyHelper.closeMatches(q, pool, 3);
        if (live.isEmpty() && fuzzy.isEmpty()) {
            // Keep prior suggest rows until network replies — avoid blank flash.
            if (!hasStorySuggestRows()) {
                rows.clear();
                adapter.notifyDataSetChanged();
            }
            return;
        }
        List<Row> next = new ArrayList<>();
        for (String t : live) {
            long postId = 0L;
            for (RecentSearchStore.Entry e : RecentSearchStore.loadEntries(this)) {
                if (e != null && e.term != null && e.term.equalsIgnoreCase(t) && e.postId > 0L) {
                    postId = e.postId;
                    break;
                }
            }
            next.add(Row.suggest(t, postId > 0L ? 1 : 0, q, postId));
        }
        if (!fuzzy.isEmpty()) {
            next.add(Row.header(getString(R.string.search_did_you_mean)));
            for (String f : fuzzy) {
                boolean dup = false;
                for (String t : live) {
                    if (t != null && t.equalsIgnoreCase(f)) {
                        dup = true;
                        break;
                    }
                }
                if (!dup) next.add(Row.fuzzy(f));
            }
        }
        hideEmptyState();
        rows.clear();
        rows.addAll(next);
        adapter.notifyDataSetChanged();
    }

    private void paintEmptyOrFuzzy(@NonNull String q) {
        // Never show empty while a suggest request is still outstanding.
        if (suggestInFlight) return;
        List<String> pool = localSuggestPool();
        List<String> live = SearchFuzzyHelper.livePrefixMatches(q, pool, 8);
        if (!live.isEmpty()) {
            paintLocalLiveSuggestions(q);
            return;
        }
        List<String> fuzzy = SearchFuzzyHelper.closeMatches(q, pool, 5);
        if (!fuzzy.isEmpty()) {
            List<Row> next = new ArrayList<>();
            next.add(Row.header(getString(R.string.search_did_you_mean)));
            for (String f : fuzzy) {
                next.add(Row.fuzzy(f));
            }
            hideEmptyState();
            rows.clear();
            rows.addAll(next);
            adapter.notifyDataSetChanged();
            return;
        }
        // Typed query with nothing useful — clean centered empty (671b0ff),
        // not yellow strip and not recent-searches under an error banner.
        rows.clear();
        adapter.notifyDataSetChanged();
        showEmptyState();
    }

    private void showIdleState() {
        hideEmptyState();
        rows.clear();
        List<RecentSearchStore.Entry> recent = RecentSearchStore.loadEntries(this);
        if (!recent.isEmpty()) {
            rows.add(Row.header(getString(R.string.recent_searches)));
            for (RecentSearchStore.Entry r : recent) {
                if (r == null || TextUtils.isEmpty(r.term)) continue;
                rows.add(Row.recent(r.term, r.postId));
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void fetchSuggestions(final String q) {
        fetchSuggestions(q, false, 0, false);
    }

    /**
     * @param articles false = topic rows with counts; true = stories for one topic
     */
    private void fetchSuggestions(final String q, final boolean articles) {
        fetchSuggestions(q, articles, 0, false);
    }

    /**
     * @param articles false = topic rows with counts; true = stories for one topic
     * @param append true = next page of the article list already on screen
     */
    private void fetchSuggestions(
            final String q, final boolean articles, final int skip, final boolean append) {
        if (TextUtils.isEmpty(q) || "null".equalsIgnoreCase(q.trim())) {
            suggestInFlight = false;
            return;
        }
        if (!articles
                && q.equals(lastSuggestQuery)
                && listMode == MODE_TOPICS
                && hasTopicRows()
                && !articleRows.isEmpty()) {
            return;
        }
        if (articles
                && !append
                && q.equals(lastSuggestQuery)
                && listMode == MODE_ARTICLES
                && hasArticleRows()) {
            return;
        }
        if (!append) lastSuggestQuery = q;
        suggestInFlight = true;
        try {
            JsonObject req = new JsonObject();
            req.addProperty(
                    "draft_id",
                    SharedPrefsData.getInt(this, Constants.DraftID, Constants.PREF_NAME));
            req.addProperty(
                    "language_id",
                    SharedPrefsData.getInt(this, Constants.LanguageID, Constants.PREF_NAME));
            req.addProperty("search_text", q);
            if (articles) {
                req.addProperty("limit", ARTICLE_PAGE);
                req.addProperty("skip", Math.max(0, skip));
                req.addProperty("include_articles", true);
                req.addProperty("suggest_mode", "articles");
            } else {
                // Same response: topic counts in data[], stories in articles[].
                req.addProperty("limit", ARTICLE_PAGE);
                req.addProperty("suggest_mode", "topics");
            }
            String enc = encrypt(new Gson().toJson(req));
            JsonObject body = new JsonObject();
            body.addProperty("data", enc);
            APIService api = RestApiClient.getClient().create(APIService.class);
            suggestCall = api.Search_Suggestions("v3", body);
            final int seqAtSend = suggestSeq;
            suggestCall.enqueue(
                    new Callback<EncreptionRequest>() {
                        @Override
                        public void onResponse(
                                Call<EncreptionRequest> call,
                                Response<EncreptionRequest> response) {
                            if (isFinishing() || call.isCanceled()) return;
                            if (seqAtSend != suggestSeq) return;
                            suggestInFlight = false;
                            String typed =
                                    searchInput.getText() != null
                                            ? searchInput.getText().toString().trim()
                                            : "";
                            // Topic search follows the box. Article search follows the
                            // tapped row, which may be Telugu while the box stays English.
                            if (!articles && !typed.equals(q)) return;
                            if (!articles && listMode != MODE_TOPICS) return;
                            if (articles && listMode != MODE_ARTICLES && listMode != MODE_TOPICS) {
                                return;
                            }
                            try {
                                JsonObject obj = null;
                                if (response.body() != null
                                        && response.body().getData() != null) {
                                    String raw = decrypt(response.body().getData());
                                    obj = new Gson().fromJson(raw, JsonObject.class);
                                }
                                if (!articles || listMode == MODE_TOPICS) {
                                    applyMixed(obj, q, articles && append);
                                    return;
                                }
                                List<Row> next = readArticleRows(obj, q);
                                for (Row art : next) {
                                    if (art != null && !TextUtils.isEmpty(art.title)) {
                                        lastSuggestTexts.add(art.title);
                                    }
                                }
                                while (lastSuggestTexts.size() > 40) {
                                    lastSuggestTexts.remove(0);
                                }
                                if (append) {
                                    appendArticleRows(next);
                                    noteArticlePage(obj, next.size());
                                    return;
                                }
                                boolean hasStory = false;
                                for (Row r : next) {
                                    if (r.type == Row.TYPE_SUGGEST
                                            || r.type == Row.TYPE_ARTICLE) {
                                        hasStory = true;
                                        break;
                                    }
                                }
                                if (!hasStory) {
                                    articleHasMore = false;
                                    // Stale article rows from the previous letter must not stick
                                    // when this query truly has zero hits.
                                    rows.clear();
                                    adapter.notifyDataSetChanged();
                                    // Local prefix only (no in-flight / no prior articles).
                                    paintLocalLiveSuggestions(q);
                                    if (!hasStorySuggestRows()) {
                                        paintEmptyOrFuzzy(q);
                                    }
                                    return;
                                }
                                hideEmptyState();
                                final int pageCount = next.size();
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
                                            if ((r.type == Row.TYPE_SUGGEST
                                                            || r.type == Row.TYPE_ARTICLE)
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
                                noteArticlePage(obj, pageCount);
                            } catch (Exception e) {
                                if (append && (hasArticleRows() || hasTopicRows())) {
                                    articleHasMore = false;
                                } else {
                                    paintEmptyOrFuzzy(q);
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<EncreptionRequest> call, Throwable t) {
                            if (seqAtSend != suggestSeq) return;
                            suggestInFlight = false;
                            if (!call.isCanceled()) {
                                String typed =
                                        searchInput.getText() != null
                                                ? searchInput.getText().toString().trim()
                                                : "";
                                boolean stillCurrent =
                                        articles
                                                ? listMode == MODE_ARTICLES
                                                        || (append && listMode == MODE_TOPICS)
                                                : typed.equals(q) && listMode == MODE_TOPICS;
                                if (stillCurrent) {
                                    if (append && (hasArticleRows() || hasTopicRows())) {
                                        articleHasMore = false;
                                        return;
                                    }
                                    paintLocalLiveSuggestions(q);
                                    if (!hasStorySuggestRows()) {
                                        paintEmptyOrFuzzy(q);
                                    }
                                }
                            }
                        }
                    });
        } catch (Exception e) {
            suggestInFlight = false;
            if (append && (hasArticleRows() || hasTopicRows())) {
                articleHasMore = false;
            } else {
                paintEmptyOrFuzzy(q);
            }
        }
    }

    @NonNull
    private List<Row> readArticleRows(@Nullable JsonObject obj, @NonNull String q) {
        List<Row> next = new ArrayList<>();
        if (obj == null) return next;
        JsonArray articleArr =
                obj.has("articles") && obj.get("articles").isJsonArray()
                        ? obj.getAsJsonArray("articles")
                        : null;
        if (articleArr != null && articleArr.size() > 0) {
            for (JsonElement el : articleArr) {
                Row art = parseArticleRow(el, q);
                if (art != null) next.add(art);
            }
        }
        if (!next.isEmpty()) return next;
        JsonArray data =
                obj.has("data") && obj.get("data").isJsonArray()
                        ? obj.getAsJsonArray("data")
                        : null;
        if (data == null) return next;
        for (JsonElement el : data) {
            Row parsed = parseSuggestDataRow(el, q);
            if (parsed != null) next.add(parsed);
        }
        return next;
    }

    private void appendArticleRows(@NonNull List<Row> more) {
        if (more.isEmpty() || adapter == null) return;
        int insertAt = rows.size();
        for (int i = 0; i < rows.size(); i++) {
            int type = rows.get(i).type;
            if (type == Row.TYPE_HEADER || type == Row.TYPE_FUZZY) {
                insertAt = i;
                break;
            }
        }
        int added = 0;
        for (Row art : more) {
            if (art == null || art.type != Row.TYPE_ARTICLE) continue;
            if (art.postId > 0L && articleListHasPost(art.postId)) continue;
            rows.add(insertAt, art);
            insertAt++;
            added++;
        }
        if (added > 0) adapter.notifyDataSetChanged();
    }

    private boolean articleListHasPost(long postId) {
        for (Row r : rows) {
            if (r.type == Row.TYPE_ARTICLE && r.postId == postId) return true;
        }
        return false;
    }

    private void noteArticlePage(@Nullable JsonObject obj, int received) {
        boolean more = false;
        int next = articleNextSkip;
        if (obj != null) {
            try {
                if (obj.has("has_more") && !obj.get("has_more").isJsonNull()) {
                    more = obj.get("has_more").getAsBoolean();
                }
            } catch (Exception ignored) {
                more = false;
            }
            try {
                if (obj.has("next_skip") && !obj.get("next_skip").isJsonNull()) {
                    next = obj.get("next_skip").getAsInt();
                }
            } catch (Exception ignored) {
                next = articleNextSkip;
            }
        }
        if (received <= 0 || next <= articleNextSkip) more = false;
        articleHasMore = more;
        if (more) articleNextSkip = next;
        if (listView != null) listView.post(this::maybeLoadMoreArticles);
    }

    private void maybeLoadMoreArticles() {
        if ((listMode != MODE_ARTICLES && listMode != MODE_TOPICS)
                || !articleHasMore
                || suggestInFlight) {
            return;
        }
        if (TextUtils.isEmpty(articleQuery) || listView == null) return;
        RecyclerView.LayoutManager layout = listView.getLayoutManager();
        if (!(layout instanceof LinearLayoutManager)) return;
        int last = ((LinearLayoutManager) layout).findLastVisibleItemPosition();
        if (last < 0) {
            if (articleLayoutRetries < 2 && listView.getChildCount() == 0) {
                articleLayoutRetries++;
                listView.post(this::maybeLoadMoreArticles);
            }
            return;
        }
        articleLayoutRetries = 0;
        if (last < rows.size() - 3) return;
        fetchSuggestions(articleQuery, true, articleNextSkip, true);
    }

    private void resetArticlePaging() {
        articleHasMore = false;
        articleNextSkip = 0;
        articleLayoutRetries = 0;
        articleQuery = "";
    }

    @Nullable
    private static Row parseArticleRow(JsonElement el, String highlight) {
        if (el == null || !el.isJsonObject()) return null;
        JsonObject o = el.getAsJsonObject();
        String title =
                o.has("title") && !o.get("title").isJsonNull()
                        ? o.get("title").getAsString()
                        : (o.has("text") && !o.get("text").isJsonNull()
                                ? o.get("text").getAsString()
                                : "");
        if (TextUtils.isEmpty(title) || title.trim().length() < 2) return null;
        if ("null".equalsIgnoreCase(title.trim())) return null;
        long postId = 0L;
        try {
            if (o.has("post_id") && !o.get("post_id").isJsonNull()) {
                postId = o.get("post_id").getAsLong();
            }
        } catch (Exception ignored) {
            postId = 0L;
        }
        String image = firstImageUrl(o);
        Row art = Row.article(title, highlight, postId, image, parseMatch(o), readPublishedTime(o));
        String topic = "";
        if (o.has("topic") && !o.get("topic").isJsonNull()) {
            topic = o.get("topic").getAsString();
        }
        if (topic == null) topic = "";
        topic = topic.trim();
        if ("null".equalsIgnoreCase(topic)) topic = "";
        art.topicLabel = topic;
        return art;
    }

    @Nullable
    private static Row parseSuggestDataRow(JsonElement el, String highlight) {
        if (el == null || !el.isJsonObject()) return null;
        JsonObject o = el.getAsJsonObject();
        String text = o.has("text") && !o.get("text").isJsonNull()
                ? o.get("text").getAsString()
                : "";
        if (TextUtils.isEmpty(text) || text.trim().length() < 2) return null;
        if ("null".equalsIgnoreCase(text.trim())) return null;
        int count = 0;
        try {
            if (o.has("count") && !o.get("count").isJsonNull()) {
                count = o.get("count").getAsInt();
            }
        } catch (Exception ignored) {
            count = 0;
        }
        long postId = 0L;
        try {
            if (o.has("post_id") && !o.get("post_id").isJsonNull()) {
                postId = o.get("post_id").getAsLong();
            }
        } catch (Exception ignored) {
            postId = 0L;
        }
        String image = firstImageUrl(o);
        // Article-shaped row (count 1 + post_id and/or image) → thumbnail UI.
        if ((count == 1 && postId > 0L) || !TextUtils.isEmpty(image)) {
            if (count != 1) postId = postId > 0L ? postId : 0L;
            return Row.article(
                    text, highlight, postId > 0L ? postId : 0L, image, parseMatch(o), readPublishedTime(o));
        }
        // Legacy keyword bucket — still open FlipView keyword search.
        return Row.suggest(text, count, highlight, 0L);
    }

    /**
     * Server `match`: title ranges, or one body snippet. Missing on old servers.
     * Ranges are UTF-16 indexes, same as Java String.
     */
    @Nullable
    private static MatchBits parseMatch(@Nullable JsonObject o) {
        if (o == null || !o.has("match") || !o.get("match").isJsonObject()) return null;
        JsonObject m = o.getAsJsonObject("match");
        String matchIn = "";
        if (m.has("match_in") && !m.get("match_in").isJsonNull()) {
            matchIn = m.get("match_in").getAsString();
        }
        if (matchIn == null) matchIn = "";
        matchIn = matchIn.trim().toLowerCase(Locale.ROOT);
        if (!"title".equals(matchIn) && !"body".equals(matchIn)) return null;
        int[][] titleRanges = "title".equals(matchIn) ? readRanges(m, "ranges") : null;
        String snippet = "";
        int[][] snipRanges = null;
        if ("body".equals(matchIn) && m.has("snippet") && !m.get("snippet").isJsonNull()) {
            snippet = m.get("snippet").getAsString();
        if (snippet == null) snippet = "";
        snippet = snippet.replace('\n', ' ').replace('\r', ' ').trim();
        if ("null".equalsIgnoreCase(snippet) || "undefined".equalsIgnoreCase(snippet)) {
            snippet = "";
        }
            snipRanges = readRanges(m, "snippet_ranges");
        }
        return new MatchBits(
                matchIn,
                snippet,
                titleRanges,
                snipRanges);
    }

    @Nullable
    private static int[][] readRanges(@Nullable JsonObject m, String key) {
        if (m == null || !m.has(key) || !m.get(key).isJsonArray()) return null;
        JsonArray arr = m.getAsJsonArray(key);
        int n = Math.min(arr.size(), 4);
        int[] starts = new int[n];
        int[] ends = new int[n];
        int kept = 0;
        for (int i = 0; i < n; i++) {
            JsonElement el = arr.get(i);
            if (el == null || !el.isJsonObject()) continue;
            JsonObject r = el.getAsJsonObject();
            int s = -1;
            int e = -1;
            try {
                if (r.has("start") && !r.get("start").isJsonNull()) s = r.get("start").getAsInt();
                if (r.has("end") && !r.get("end").isJsonNull()) e = r.get("end").getAsInt();
            } catch (Exception ignored) {
                continue;
            }
            if (s < 0 || e <= s) continue;
            starts[kept] = s;
            ends[kept] = e;
            kept++;
        }
        if (kept == 0) return null;
        if (kept == n) return new int[][] {starts, ends};
        int[] ss = new int[kept];
        int[] ee = new int[kept];
        System.arraycopy(starts, 0, ss, 0, kept);
        System.arraycopy(ends, 0, ee, 0, kept);
        return new int[][] {ss, ee};
    }

    @NonNull
    private static String firstImageUrl(JsonObject o) {
        if (o == null) return "";
        String[] keys = {
            "thumb_image", "image", "video_thumb", "thumbnail", "full_image"
        };
        for (String k : keys) {
            if (!o.has(k)) continue;
            String s = urlFromElement(o.get(k));
            if (!TextUtils.isEmpty(s)) return s;
        }
        String[] lists = {"images", "gallery", "thumbnails", "wifi_images"};
        for (String k : lists) {
            if (!o.has(k)) continue;
            String s = urlFromElement(o.get(k));
            if (!TextUtils.isEmpty(s)) return s;
        }
        return "";
    }

    /** Epoch millis from the server. Seconds are scaled up. 0 if missing. */
    private static long readPublishedTime(@Nullable JsonObject o) {
        if (o == null || !o.has("published_time") || o.get("published_time").isJsonNull()) {
            return 0L;
        }
        try {
            long ms = o.get("published_time").getAsLong();
            if (ms > 0L && ms < 100000000000L) ms *= 1000L;
            return ms > 0L ? ms : 0L;
        } catch (Exception ignored) {
            return 0L;
        }
    }

    /** Short IST date for an article row: 28 Sep, or 28 Sep 2025 when not this year. */
    @NonNull
    private static String formatArticleDate(long publishedMs) {
        if (publishedMs <= 0L) return "";
        try {
            TimeZone ist = TimeZone.getTimeZone("Asia/Kolkata");
            Calendar when = Calendar.getInstance(ist);
            when.setTimeInMillis(publishedMs);
            Calendar now = Calendar.getInstance(ist);
            String pattern =
                    when.get(Calendar.YEAR) == now.get(Calendar.YEAR) ? "d MMM" : "d MMM yyyy";
            SimpleDateFormat fmt = new SimpleDateFormat(pattern, Locale.ENGLISH);
            fmt.setTimeZone(ist);
            return fmt.format(new Date(publishedMs));
        } catch (Exception e) {
            return "";
        }
    }

    /** String, first array element, or {url|src|image} — skip blanks. */
    @NonNull
    private static String urlFromElement(@Nullable JsonElement el) {
        if (el == null || el.isJsonNull()) return "";
        try {
            if (el.isJsonPrimitive()) {
                String s = el.getAsString();
                if (s == null) return "";
                s = s.trim();
                if (s.isEmpty() || "null".equalsIgnoreCase(s)) return "";
                return s;
            }
            if (el.isJsonArray()) {
                JsonArray arr = el.getAsJsonArray();
                for (JsonElement child : arr) {
                    String s = urlFromElement(child);
                    if (!TextUtils.isEmpty(s)) return s;
                }
                return "";
            }
            if (el.isJsonObject()) {
                JsonObject obj = el.getAsJsonObject();
                String[] nested = {"url", "src", "image", "uri", "thumb"};
                for (String k : nested) {
                    if (!obj.has(k)) continue;
                    String s = urlFromElement(obj.get(k));
                    if (!TextUtils.isEmpty(s)) return s;
                }
            }
        } catch (Exception ignored) {
            /* skip malformed media */
        }
        return "";
    }

    /** Keyboard search: refresh topic counts and the stories under them. */
    private void submitQuery(String q) {
        showTopicCounts(q);
    }

    private void showTopicCounts(String q) {
        String clean = softenClientQuery(q);
        listMode = MODE_TOPICS;
        resetArticlePaging();
        dropArticleRows();
        if (clean.isEmpty() || "null".equalsIgnoreCase(clean)) {
            showIdleState();
            return;
        }
        RecentSearchStore.add(this, clean, 0L);
        cancelSuggest();
        fetchSuggestions(clean, false);
    }

    @Nullable
    private static Row parseTopicRow(JsonElement el) {
        if (el == null || !el.isJsonObject()) return null;
        JsonObject o = el.getAsJsonObject();
        String text =
                o.has("text") && !o.get("text").isJsonNull() ? o.get("text").getAsString() : "";
        if (text == null) return null;
        text = text.trim();
        if (text.length() < 2 || "null".equalsIgnoreCase(text)) return null;
        int count = 0;
        try {
            if (o.has("count") && !o.get("count").isJsonNull()) {
                count = o.get("count").getAsInt();
            }
        } catch (Exception ignored) {
            count = 0;
        }
        if (count < 1) return null;
        return Row.topic(text, count);
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
        static final int TYPE_SUGGEST = 3;
        static final int TYPE_ARTICLE = 4;
        static final int TYPE_FUZZY = 5;

        final int type;
        final String title;
        final int meta;
        final String highlight;
        final long postId;
        final String imageUrl;
        @Nullable final MatchBits match;
        /** Count bucket (Way2News row). Tap loads stories; it is not one article. */
        final boolean topicBucket;
        /** published_time epoch millis. 0 on topic / count rows. */
        final long publishedTime;
        /** Server topic label for this story, used to filter in place. */
        String topicLabel = "";

        private Row(
                int type,
                String title,
                int meta,
                String highlight,
                long postId,
                String imageUrl,
                @Nullable MatchBits match) {
            this(type, title, meta, highlight, postId, imageUrl, match, false, 0L);
        }

        private Row(
                int type,
                String title,
                int meta,
                String highlight,
                long postId,
                String imageUrl,
                @Nullable MatchBits match,
                boolean topicBucket) {
            this(type, title, meta, highlight, postId, imageUrl, match, topicBucket, 0L);
        }

        private Row(
                int type,
                String title,
                int meta,
                String highlight,
                long postId,
                String imageUrl,
                @Nullable MatchBits match,
                boolean topicBucket,
                long publishedTime) {
            this.type = type;
            this.title = title;
            this.meta = meta;
            this.highlight = highlight;
            this.postId = postId;
            this.imageUrl = imageUrl;
            this.match = match;
            this.topicBucket = topicBucket;
            this.publishedTime = publishedTime;
        }

        static Row header(String t) {
            return new Row(TYPE_HEADER, t, 0, null, 0L, null, null);
        }

        static Row recent(String t) {
            return recent(t, 0L);
        }

        static Row recent(String t, long postId) {
            return new Row(TYPE_RECENT, t, 0, null, postId, null, null);
        }

        static Row suggest(String t, int count, String highlight) {
            return suggest(t, count, highlight, 0L);
        }

        static Row suggest(String t, int count, String highlight, long postId) {
            return new Row(TYPE_SUGGEST, t, count, highlight, postId, null, null);
        }

        static Row topic(String t, int count) {
            return new Row(TYPE_SUGGEST, t, count, null, 0L, null, null, true);
        }

        static Row article(String t, String highlight, long postId, String imageUrl) {
            return article(t, highlight, postId, imageUrl, null);
        }

        static Row article(
                String t,
                String highlight,
                long postId,
                String imageUrl,
                @Nullable MatchBits match) {
            return article(t, highlight, postId, imageUrl, match, 0L);
        }

        static Row article(
                String t,
                String highlight,
                long postId,
                String imageUrl,
                @Nullable MatchBits match,
                long publishedTime) {
            return new Row(
                    TYPE_ARTICLE, t, 1, highlight, postId, imageUrl, match, false, publishedTime);
        }

        static Row fuzzy(String t) {
            return new Row(TYPE_FUZZY, t, 0, null, 0L, null, null);
        }
    }

    private class SearchRowsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final RequestOptions thumbOpts =
                new RequestOptions()
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .placeholder(R.drawable.ic_news_icon)
                        .error(R.drawable.ic_news_icon);

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
            vh.title.setTextColor(
                    ContextCompat.getColor(InAppSearchActivity.this, R.color.title_color));
            if (vh.date != null) {
                vh.date.setVisibility(View.GONE);
                vh.date.setText("");
            }
            if (vh.snippet != null) {
                vh.snippet.setVisibility(View.GONE);
                vh.snippet.setText("");
            }
            bindRowText(vh, row);
            if (row.topicBucket
                    && !TextUtils.isEmpty(focusedTopic)
                    && focusedTopic.equalsIgnoreCase(row.title)) {
                vh.title.setTextColor(
                        ContextCompat.getColor(InAppSearchActivity.this, R.color.search_chrome_accent));
            }
            if (row.type == Row.TYPE_ARTICLE) {
                String when = formatArticleDate(row.publishedTime);
                if (vh.date != null && !TextUtils.isEmpty(when)) {
                    vh.date.setText(when);
                    vh.date.setVisibility(View.VISIBLE);
                }
                vh.meta.setVisibility(View.GONE);
                vh.icon.setVisibility(View.GONE);
                vh.action.setVisibility(View.GONE);
                vh.thumb.setVisibility(View.VISIBLE);
                bindThumb(vh.thumb, row.imageUrl);
                vh.itemView.setOnClickListener(
                        v -> openSearch(suggestSubmitText(row), row.postId, true));
            } else if (row.type == Row.TYPE_SUGGEST) {
                vh.icon.setVisibility(View.VISIBLE);
                vh.thumb.setVisibility(View.GONE);
                clearThumb(vh.thumb);
                vh.icon.setImageResource(R.drawable.ic_action_search);
                vh.action.setVisibility(View.GONE);
                if (row.topicBucket) {
                    vh.meta.setVisibility(View.VISIBLE);
                    vh.meta.setText(String.valueOf(row.meta));
                    vh.itemView.setOnClickListener(v -> openTopic(row.title));
                } else if (row.postId > 0L) {
                    vh.meta.setVisibility(View.GONE);
                    vh.itemView.setOnClickListener(
                            v -> openSearch(suggestSubmitText(row), row.postId, true));
                } else {
                    if (row.meta > 1) {
                        vh.meta.setVisibility(View.VISIBLE);
                        vh.meta.setText(String.valueOf(row.meta));
                    } else {
                        vh.meta.setVisibility(View.GONE);
                    }
                    vh.itemView.setOnClickListener(
                            v -> showTopicCounts(suggestSubmitText(row)));
                }
            } else if (row.type == Row.TYPE_FUZZY) {
                vh.icon.setVisibility(View.VISIBLE);
                vh.meta.setVisibility(View.GONE);
                vh.thumb.setVisibility(View.GONE);
                clearThumb(vh.thumb);
                vh.icon.setImageResource(R.drawable.ic_action_search);
                vh.action.setVisibility(View.GONE);
                vh.itemView.setOnClickListener(
                        v -> {
                            String next = row.title != null ? row.title : "";
                            String cur =
                                    searchInput != null && searchInput.getText() != null
                                            ? searchInput.getText().toString()
                                            : "";
                            if (cur.equals(next)) {
                                showTopicCounts(next);
                            } else if (searchInput != null) {
                                searchInput.setText(next);
                                searchInput.setSelection(next.length());
                            }
                        });
            } else if (row.type == Row.TYPE_RECENT) {
                vh.icon.setVisibility(View.VISIBLE);
                vh.meta.setVisibility(View.GONE);
                vh.thumb.setVisibility(View.GONE);
                clearThumb(vh.thumb);
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
                            String next = row.title != null ? row.title : "";
                            String cur =
                                    searchInput != null && searchInput.getText() != null
                                            ? searchInput.getText().toString()
                                            : "";
                            if (cur.equals(next)) {
                                showTopicCounts(next);
                            } else if (searchInput != null) {
                                searchInput.setText(next);
                                searchInput.setSelection(next.length());
                            }
                        });
            } else {
                vh.icon.setVisibility(View.VISIBLE);
                vh.meta.setVisibility(View.GONE);
                vh.thumb.setVisibility(View.GONE);
                clearThumb(vh.thumb);
                vh.action.setVisibility(View.GONE);
                vh.itemView.setOnClickListener(null);
            }
        }

        private void bindThumb(@NonNull ImageView thumb, @Nullable String url) {
            if (TextUtils.isEmpty(url)) {
                thumb.setImageResource(R.drawable.ic_news_icon);
                return;
            }
            try {
                Glide.with(thumb.getContext())
                        .load(url)
                        .apply(thumbOpts)
                        .into(thumb);
            } catch (Exception e) {
                thumb.setImageResource(R.drawable.ic_news_icon);
            }
        }

        private void clearThumb(@NonNull ImageView thumb) {
            try {
                Glide.with(thumb.getContext()).clear(thumb);
            } catch (Exception ignored) {
                /* ignore */
            }
            thumb.setImageDrawable(null);
        }

        @Override
        public int getItemCount() {
            return rows.size();
        }
    }

    private void bindRowText(@NonNull RowVH vh, @NonNull Row row) {
        int accent =
                ContextCompat.getColor(vh.itemView.getContext(), R.color.search_chrome_accent);
        MatchBits match = row.match;
        if (match != null && "title".equals(match.matchIn)) {
            CharSequence styled =
                    highlightRanges(row.title, match.titleStarts, match.titleEnds, accent);
            if (styled != null) {
                vh.title.setText(styled);
            } else if (!TextUtils.isEmpty(row.highlight)) {
                vh.title.setText(highlightMatch(row.title, row.highlight));
            } else {
                vh.title.setText(row.title);
            }
            return;
        }
        if (match != null && "body".equals(match.matchIn)) {
            // Title does not explain the hit — one short line under it does.
            vh.title.setText(row.title != null ? row.title : "");
            if (vh.snippet != null && !TextUtils.isEmpty(match.snippet)) {
                vh.snippet.setVisibility(View.VISIBLE);
                CharSequence snip =
                        highlightRanges(match.snippet, match.snipStarts, match.snipEnds, accent);
                vh.snippet.setText(snip != null ? snip : match.snippet);
            }
            return;
        }
        if (!row.topicBucket
                && (row.type == Row.TYPE_SUGGEST || row.type == Row.TYPE_ARTICLE)
                && !TextUtils.isEmpty(row.highlight)) {
            vh.title.setText(highlightMatch(row.title, row.highlight));
        } else {
            vh.title.setText(row.title);
        }
    }

    @Nullable
    private static CharSequence highlightRanges(
            String text, @Nullable int[] starts, @Nullable int[] ends, int color) {
        if (TextUtils.isEmpty(text) || starts == null || ends == null) return null;
        SpannableString ss = new SpannableString(text);
        boolean any = false;
        int n = Math.min(starts.length, ends.length);
        for (int i = 0; i < n; i++) {
            int a = starts[i];
            int b = ends[i];
            if (a < 0 || b > text.length() || a >= b) continue;
            ss.setSpan(
                    new StyleSpan(android.graphics.Typeface.BOLD),
                    a,
                    b,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ss.setSpan(
                    new ForegroundColorSpan(color),
                    a,
                    b,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            any = true;
        }
        return any ? ss : null;
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
        final TextView date;
        final TextView snippet;
        final TextView meta;
        final ImageView thumb;
        final ImageView action;

        RowVH(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.row_icon);
            title = itemView.findViewById(R.id.row_title);
            date = itemView.findViewById(R.id.row_date);
            snippet = itemView.findViewById(R.id.row_snippet);
            meta = itemView.findViewById(R.id.row_meta);
            thumb = itemView.findViewById(R.id.row_thumb);
            action = itemView.findViewById(R.id.row_action);
        }
    }

    /** Why a suggestion matched. Null on old servers (title/image only). */
    private static final class MatchBits {
        final String matchIn;
        final String snippet;
        @Nullable final int[] titleStarts;
        @Nullable final int[] titleEnds;
        @Nullable final int[] snipStarts;
        @Nullable final int[] snipEnds;

        MatchBits(
                String matchIn,
                String snippet,
                @Nullable int[][] titleRanges,
                @Nullable int[][] snipRanges) {
            this.matchIn = matchIn;
            this.snippet = snippet != null ? snippet : "";
            this.titleStarts = titleRanges != null ? titleRanges[0] : null;
            this.titleEnds = titleRanges != null ? titleRanges[1] : null;
            this.snipStarts = snipRanges != null ? snipRanges[0] : null;
            this.snipEnds = snipRanges != null ? snipRanges[1] : null;
        }
    }
}
