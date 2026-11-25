package com.anyNews.anynews.Activities;

import com.anyNews.anynews.Utils.MyUtils;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.anyNews.anynews.Adapters.PostsStatusAdopter;
import com.anyNews.anynews.Pojo.EncreptionRequest;
import com.anyNews.anynews.Pojo.EncritonRequestData;
import com.anyNews.anynews.Pojo.LanguageModelResponse;
import com.anyNews.anynews.Pojo.NewUserProfileResponse;
import com.anyNews.anynews.Pojo.PostNewsRequest;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Utils.Allsharedpreference;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.PostStatusListCache;
import com.anyNews.anynews.Utils.ReporterPaymentAppConfig;
import com.anyNews.anynews.Utils.ReporterProfileCache;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.anyNews.anynews.models.PostsModel;
import com.anyNews.anynews.Ui.NeomorphFrameLayout;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicIntegerArray;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.anyNews.anynews.Utils.AppHelper.showLog;
import static com.anyNews.anynews.Utils.MyUtils.decrypt;
import static com.anyNews.anynews.Utils.MyUtils.encrypt;

public class PostArticleStatus extends AppCompatActivity {

    public static final String EXTRA_MOBILE_NUMBER = "extra_mobile_number";
    /**
     * When set: {@code likes}, {@code comments}, or {@code views}.
     * Opens approved articles sorted by that metric (no status tabs).
     */
    public static final String EXTRA_METRIC = "extra_metric";

    /** API status values: 0 pending, 1 approved, 2 rejected. */
    private static final int[] ALL_STATUSES = {1, 0, 2};

    RecyclerView post_status;
    LinearLayoutManager linearLayoutManager;
    ArrayList<PostsModel.Datum> list;
    PostsStatusAdopter postsStatusAdopter;
    ImageView go_bck;
    NeomorphFrameLayout neomorphFrameLayout_approved_un_selected, neomorphFrameLayout_approved_selected,
            neomorphFrameLayout_pending_un_selected, neomorphFrameLayout_pending_selected,
            neomorphFrameLayout_rejected_un_selected, neomorphFrameLayout_rejected_selected;
    SwipeRefreshLayout swipeRefresh;
    TextView emptyState;
    private int currentStatus = 1;
    private String resolvedMobile = "";
    private boolean systemBarInsetsInstalled;
    private boolean prefetchedOthers;
    /** Empty = normal status tabs; likes|comments|views = engagement list. */
    private String metricMode = "";
    private View tabsRow;
    private TextView pageTitle;
    private TextView tabApprovedSelected;
    private TextView tabApprovedUnselected;
    private TextView tabPendingSelected;
    private TextView tabPendingUnselected;
    private TextView tabRejectedSelected;
    private TextView tabRejectedUnselected;
    /** Localized base labels (no counts). */
    private String labelApproved = "Approved";
    private String labelPending = "Pending";
    private String labelRejected = "Rejected";
    /** -1 = unknown (show 0 from cache/list until API totals arrive). */
    private int countApproved = -1;
    private int countPending = -1;
    private int countRejected = -1;

    /** Generation per status so slow responses don't overwrite a newer tab paint. */
    private final AtomicIntegerArray requestGen = new AtomicIntegerArray(3);
    private final boolean[] inflight = new boolean[3];

    private View dateFilterRow;
    private TextView dateFromView;
    private TextView dateToView;
    private TextView dateApplyView;
    private TextView dateClearView;
    private TextView dateAppliedLabel;
    /** yyyy-MM-dd or empty */
    private String filterStart = "";
    private String filterTo = "";
    /** True after both dates are set and filter was applied (auto or Apply). */
    private boolean dateFilterApplied;
    private int nextSkip = 0;
    private long firstPostId = 0L;
    private boolean hasMore = false;
    private boolean loadingPage = false;
    /** 1-based page index (admin-style pager, not endless scroll). */
    private int currentPage = 1;
    /** Flat article rows for the current page only. */
    private final ArrayList<PostsModel.Datum> flatArticles = new ArrayList<>();

    private View pagerRow;
    private TextView pagePrevView;
    private TextView pageNextView;
    private TextView pageLabelView;
    private String labelPrev = "Prev";
    private String labelNext = "Next";

    /** Match admin Local default band; smaller pages for thousands of articles. */
    private static final int PAGE_LIMIT = 25;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posts_status);
        applySystemBarInsets();

        metricMode = getIntent() != null ? getIntent().getStringExtra(EXTRA_METRIC) : null;
        if (metricMode == null) metricMode = "";
        metricMode = metricMode.trim().toLowerCase();

        post_status = findViewById(R.id.post_status);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        emptyState = findViewById(R.id.posts_empty_state);
        tabsRow = findViewById(R.id.posts_status_tabs_row);
        pageTitle = findViewById(R.id.username);

        neomorphFrameLayout_approved_selected = findViewById(R.id.neomorphFrameLayout_approved_selected);
        neomorphFrameLayout_approved_un_selected = findViewById(R.id.neomorphFrameLayout_approved_un_selected);
        neomorphFrameLayout_pending_un_selected = findViewById(R.id.neomorphFrameLayout_pending_un_selected);
        neomorphFrameLayout_pending_selected = findViewById(R.id.neomorphFrameLayout_pending_selected);
        neomorphFrameLayout_rejected_selected = findViewById(R.id.neomorphFrameLayout_rejected_selected);
        neomorphFrameLayout_rejected_un_selected = findViewById(R.id.neomorphFrameLayout_rejected_un_selected);

        tabApprovedSelected = findViewById(R.id.selected_approved);
        tabApprovedUnselected = findViewById(R.id.un_selected_approved);
        tabPendingSelected = findViewById(R.id.selected_pending);
        tabPendingUnselected = findViewById(R.id.un_selected_pending);
        tabRejectedSelected = findViewById(R.id.selected_rejected);
        tabRejectedUnselected = findViewById(R.id.un_selected_rejected);

        resolvedMobile = resolveMobileNumber();
        if (!TextUtils.isEmpty(resolvedMobile)) {
            SharedPrefsData.putString(getApplicationContext(), Constants.Selected_Phone_Number,
                    resolvedMobile, Constants.PREF_NAME);
        }

        // List + layout once; tab switches only rebind data (no empty flash).
        list = new ArrayList<>();
        linearLayoutManager = new LinearLayoutManager(this);
        post_status.setLayoutManager(linearLayoutManager);
        postsStatusAdopter = new PostsStatusAdopter(this, this, list, currentStatus, metricMode);
        post_status.setAdapter(postsStatusAdopter);

        go_bck = findViewById(R.id.go_bck);
        go_bck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        dateFilterRow = findViewById(R.id.posts_date_filter_row);
        dateFromView = findViewById(R.id.posts_date_from);
        dateToView = findViewById(R.id.posts_date_to);
        dateApplyView = findViewById(R.id.posts_date_apply);
        dateClearView = findViewById(R.id.posts_date_clear);
        dateAppliedLabel = findViewById(R.id.posts_date_applied_label);
        pagerRow = findViewById(R.id.posts_pager_row);
        pagePrevView = findViewById(R.id.posts_page_prev);
        pageNextView = findViewById(R.id.posts_page_next);
        pageLabelView = findViewById(R.id.posts_page_label);
        setupDateFilterUi();
        setupPagerUi();

        applyMetricChrome();
        if (!isMetricMode()) {
            applyLocalizedTabLabels();
            seedTabCountsFromCache();
            refreshTabCountLabels();
        }

        if (!isMetricMode()) {
            neomorphFrameLayout_approved_un_selected.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectApprovedTab();
                    showTab(1, false);
                }
            });

            neomorphFrameLayout_pending_un_selected.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectPendingTab();
                    showTab(0, false);
                }
            });

            neomorphFrameLayout_rejected_un_selected.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectRejectedTab();
                    showTab(2, false);
                }
            });
        }

        if (swipeRefresh != null) {
            swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    // Soft refresh current page; keep list until response.
                    fetchUserPosts(currentStatus, true, currentPage);
                }
            });
        }

        selectApprovedTab();
        showTab(1, true);
    }

    private boolean isMetricMode() {
        return !TextUtils.isEmpty(metricMode);
    }

    /** Title + hide status tabs for likes / comments / views shortcut destinations. */
    private void applyMetricChrome() {
        if (!isMetricMode()) return;
        if (tabsRow != null) tabsRow.setVisibility(View.GONE);
        if (dateFilterRow != null) dateFilterRow.setVisibility(View.GONE);
        if (dateAppliedLabel != null) dateAppliedLabel.setVisibility(View.GONE);
        if (pagerRow != null) pagerRow.setVisibility(View.GONE);
        if (pageTitle != null) {
            pageTitle.setVisibility(View.VISIBLE);
            pageTitle.setText(buildMetricTitle());
        }
    }

    private void setupDateFilterUi() {
        if (dateFilterRow == null) return;
        if (isMetricMode()) {
            dateFilterRow.setVisibility(View.GONE);
            if (dateAppliedLabel != null) dateAppliedLabel.setVisibility(View.GONE);
            return;
        }
        dateFilterRow.setVisibility(View.VISIBLE);
        String floor = ReporterPaymentAppConfig.articlesFromDate(this);
        if (!TextUtils.isEmpty(floor) && TextUtils.isEmpty(filterStart)) {
            filterStart = floor;
            if (dateFromView != null) dateFromView.setText(floor);
        }
        if (dateFromView != null) {
            dateFromView.setHint(getString(R.string.posts_date_from));
            dateFromView.setOnClickListener(v -> pickDate(true));
        }
        if (dateToView != null) {
            dateToView.setHint(getString(R.string.posts_date_to));
            dateToView.setOnClickListener(v -> pickDate(false));
        }
        if (dateApplyView != null) {
            dateApplyView.setText(getString(R.string.posts_date_apply));
            // Hidden: end-date selection auto-applies; keep as optional fallback.
            dateApplyView.setOnClickListener(v -> applyDateFilter());
        }
        if (dateClearView != null) {
            dateClearView.setText(getString(R.string.posts_date_clear));
            dateClearView.setOnClickListener(v -> clearDateFilter());
        }
        updateDateAppliedLabel();
    }

    private void setupPagerUi() {
        if (pagerRow == null || isMetricMode()) {
            if (pagerRow != null) pagerRow.setVisibility(View.GONE);
            return;
        }
        if (pagePrevView != null) {
            pagePrevView.setText(labelPrev);
            pagePrevView.setOnClickListener(v -> {
                if (currentPage <= 1 || loadingPage) return;
                fetchUserPosts(currentStatus, false, currentPage - 1);
            });
        }
        if (pageNextView != null) {
            pageNextView.setText(labelNext);
            pageNextView.setOnClickListener(v -> {
                if (!canGoNextPage() || loadingPage) return;
                fetchUserPosts(currentStatus, false, currentPage + 1);
            });
        }
        refreshPagerUi();
    }

    private void clearDateFilter() {
        String floor = ReporterPaymentAppConfig.articlesFromDate(this);
        filterStart = !TextUtils.isEmpty(floor) ? floor : "";
        filterTo = "";
        dateFilterApplied = !TextUtils.isEmpty(filterStart);
        if (dateFromView != null) dateFromView.setText(filterStart);
        if (dateToView != null) dateToView.setText("");
        updateDateAppliedLabel();
        resetPaging();
        fetchUserPosts(currentStatus, true, 1);
    }

    private void applyDateFilter() {
        String floor = ReporterPaymentAppConfig.articlesFromDate(this);
        if (!TextUtils.isEmpty(floor)) {
            if (TextUtils.isEmpty(filterStart) || filterStart.compareTo(floor) < 0) {
                filterStart = floor;
                if (dateFromView != null) dateFromView.setText(filterStart);
            }
        }
        if (!TextUtils.isEmpty(filterStart) && !TextUtils.isEmpty(filterTo)
                && filterStart.compareTo(filterTo) > 0) {
            Toast.makeText(this, getString(R.string.posts_date_order_error), Toast.LENGTH_SHORT).show();
            return;
        }
        dateFilterApplied = !TextUtils.isEmpty(filterStart) || !TextUtils.isEmpty(filterTo);
        updateDateAppliedLabel();
        resetPaging();
        fetchUserPosts(currentStatus, true, 1);
    }

    private void updateDateAppliedLabel() {
        if (dateAppliedLabel == null) return;
        if (isMetricMode() || !dateFilterApplied
                || TextUtils.isEmpty(filterStart) || TextUtils.isEmpty(filterTo)) {
            dateAppliedLabel.setVisibility(View.GONE);
            dateAppliedLabel.setText("");
            return;
        }
        dateAppliedLabel.setText(getString(
                R.string.posts_date_range_applied, filterStart, filterTo));
        dateAppliedLabel.setVisibility(View.VISIBLE);
    }

    private void pickDate(final boolean isFrom) {
        Calendar c = Calendar.getInstance();
        // Seed picker from existing selection when present.
        try {
            String seed = isFrom ? filterStart : filterTo;
            if (!TextUtils.isEmpty(seed) && seed.length() >= 10) {
                String[] p = seed.split("-");
                if (p.length == 3) {
                    c.set(Integer.parseInt(p[0]), Integer.parseInt(p[1]) - 1, Integer.parseInt(p[2]));
                }
            }
        } catch (Exception ignored) {
        }
        final String floor = ReporterPaymentAppConfig.articlesFromDate(this);
        final long floorMs = ReporterPaymentAppConfig.fromDateMillis(floor);
        DatePickerDialog dlg = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String iso = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    if (isFrom) {
                        if (!TextUtils.isEmpty(floor) && iso.compareTo(floor) < 0) {
                            iso = floor;
                        }
                        filterStart = iso;
                        if (dateFromView != null) dateFromView.setText(iso);
                        // From changed — wait for To before treating as applied.
                        dateFilterApplied = false;
                        updateDateAppliedLabel();
                        // After From: open To picker next (no Apply required).
                        View anchor = dateFromView != null ? dateFromView : dateFilterRow;
                        if (anchor != null) {
                            anchor.post(() -> {
                                if (!isFinishing()) pickDate(false);
                            });
                        } else {
                            pickDate(false);
                        }
                    } else {
                        if (!TextUtils.isEmpty(floor) && iso.compareTo(floor) < 0) {
                            iso = floor;
                        }
                        filterTo = iso;
                        if (dateToView != null) dateToView.setText(iso);
                        // After To: auto-apply and show filtered list.
                        applyDateFilter();
                    }
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH));
        if (floorMs > Long.MIN_VALUE) {
            try {
                dlg.getDatePicker().setMinDate(floorMs);
            } catch (Exception ignored) {
            }
        }
        dlg.show();
    }

    private void resetPaging() {
        nextSkip = 0;
        firstPostId = 0L;
        hasMore = false;
        loadingPage = false;
        currentPage = 1;
        flatArticles.clear();
        refreshPagerUi();
    }

    private int statusTotalCount(int status) {
        if (status == 1) return Math.max(0, countApproved);
        if (status == 0) return Math.max(0, countPending);
        if (status == 2) return Math.max(0, countRejected);
        return 0;
    }

    private int totalPagesForCurrent() {
        boolean dateFilterOn = !TextUtils.isEmpty(filterStart) || !TextUtils.isEmpty(filterTo);
        if (dateFilterOn) {
            // Filtered totals are not returned by the API — use has_more.
            return hasMore ? currentPage + 1 : Math.max(1, currentPage);
        }
        int total = statusTotalCount(currentStatus);
        if (total <= 0) return Math.max(1, currentPage);
        return Math.max(1, (int) Math.ceil(total / (double) PAGE_LIMIT));
    }

    private boolean canGoNextPage() {
        if (hasMore) return true;
        boolean dateFilterOn = !TextUtils.isEmpty(filterStart) || !TextUtils.isEmpty(filterTo);
        if (dateFilterOn) return false;
        return currentPage < totalPagesForCurrent();
    }

    private void refreshPagerUi() {
        if (pagerRow == null || isMetricMode()) {
            if (pagerRow != null) pagerRow.setVisibility(View.GONE);
            return;
        }
        int pages = totalPagesForCurrent();
        boolean show = pages > 1 || currentPage > 1 || hasMore
                || statusTotalCount(currentStatus) > PAGE_LIMIT;
        pagerRow.setVisibility(show ? View.VISIBLE : View.GONE);
        if (pagePrevView != null) {
            pagePrevView.setEnabled(currentPage > 1 && !loadingPage);
            pagePrevView.setAlpha(currentPage > 1 ? 1f : 0.4f);
        }
        if (pageNextView != null) {
            boolean next = canGoNextPage() && !loadingPage;
            pageNextView.setEnabled(next);
            pageNextView.setAlpha(next ? 1f : 0.4f);
        }
        if (pageLabelView != null) {
            boolean dateFilterOn = !TextUtils.isEmpty(filterStart) || !TextUtils.isEmpty(filterTo);
            if (!dateFilterOn && pages > 1) {
                pageLabelView.setText(getString(R.string.posts_page_of, currentPage, pages));
            } else {
                pageLabelView.setText(getString(R.string.posts_page_label, currentPage));
            }
        }
    }

    private String buildMetricTitle() {
        long count = resolveProfileMetricCount(metricMode);
        String label;
        if ("likes".equals(metricMode)) {
            label = getString(R.string.profile_likes_received);
        } else if ("comments".equals(metricMode)) {
            label = getString(R.string.profile_comments);
        } else if ("views".equals(metricMode)) {
            label = getString(R.string.profile_total_views);
        } else {
            label = "Articles";
        }
        if (count > 0) {
            return label + " · " + count;
        }
        return label;
    }

    private long resolveProfileMetricCount(String metric) {
        try {
            NewUserProfileResponse cached = ReporterProfileCache.load(getApplicationContext());
            if (cached == null || cached.getData() == null) return 0L;
            NewUserProfileResponse.Data d = cached.getData();
            if ("likes".equals(metric)) {
                return d.getLikes() != null ? d.getLikes() : 0L;
            }
            if ("comments".equals(metric)) {
                return d.getComments() != null ? d.getComments() : 0L;
            }
            if ("views".equals(metric)) {
                return d.getTotalViews() != null ? d.getTotalViews() : 0L;
            }
        } catch (Exception ignored) {
        }
        return 0L;
    }

    @Override
    protected void onStart() {
        super.onStart();
        // AppController applies global bars in onActivityCreated (after onCreate).
        // Re-apply so this screen keeps content below the status bar on targetSdk 35.
        applySystemBarInsets();
    }

    /**
     * Keep back arrow + Approved/Pending/Rejected tabs in the app area only.
     * Takes ownership of insets for this activity (does not change Home edge-to-edge).
     * Called from onCreate and onStart so it wins over AppController's global bar setup.
     */
    private void applySystemBarInsets() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        final View root = findViewById(R.id.posts_status_root);
        if (root == null) {
            return;
        }
        if (!systemBarInsetsInstalled) {
            systemBarInsetsInstalled = true;
            final int baseLeft = root.getPaddingLeft();
            final int baseTop = root.getPaddingTop();
            final int baseRight = root.getPaddingRight();
            final int baseBottom = root.getPaddingBottom();
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
                Insets status = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars());
                Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(
                        baseLeft + bars.left,
                        baseTop + status.top,
                        baseRight + bars.right,
                        baseBottom + bars.bottom);
                return WindowInsetsCompat.CONSUMED;
            });
        }
        ViewCompat.requestApplyInsets(root);
    }

    private void selectApprovedTab() {
        neomorphFrameLayout_approved_selected.setVisibility(View.VISIBLE);
        neomorphFrameLayout_approved_un_selected.setVisibility(View.GONE);
        neomorphFrameLayout_rejected_selected.setVisibility(View.GONE);
        neomorphFrameLayout_rejected_un_selected.setVisibility(View.VISIBLE);
        neomorphFrameLayout_pending_selected.setVisibility(View.GONE);
        neomorphFrameLayout_pending_un_selected.setVisibility(View.VISIBLE);
    }

    private void selectPendingTab() {
        neomorphFrameLayout_approved_selected.setVisibility(View.GONE);
        neomorphFrameLayout_approved_un_selected.setVisibility(View.VISIBLE);
        neomorphFrameLayout_rejected_selected.setVisibility(View.GONE);
        neomorphFrameLayout_rejected_un_selected.setVisibility(View.VISIBLE);
        neomorphFrameLayout_pending_selected.setVisibility(View.VISIBLE);
        neomorphFrameLayout_pending_un_selected.setVisibility(View.GONE);
    }

    private void selectRejectedTab() {
        neomorphFrameLayout_approved_selected.setVisibility(View.GONE);
        neomorphFrameLayout_approved_un_selected.setVisibility(View.VISIBLE);
        neomorphFrameLayout_rejected_selected.setVisibility(View.VISIBLE);
        neomorphFrameLayout_rejected_un_selected.setVisibility(View.GONE);
        neomorphFrameLayout_pending_selected.setVisibility(View.GONE);
        neomorphFrameLayout_pending_un_selected.setVisibility(View.VISIBLE);
    }

    /** Same translator pack as FullViewPager / PostArticles (Telugu etc.). */
    private void applyLocalizedTabLabels() {
        try {
            Allsharedpreference prefShare = new Allsharedpreference(this);
            String translation = prefShare.getShared_String(Allsharedpreference.Translator);
            if (TextUtils.isEmpty(translation)) return;
            LanguageModelResponse lang =
                    new Gson().fromJson(translation, LanguageModelResponse.class);
            if (lang == null || lang.getData() == null) return;
            if (!TextUtils.isEmpty(lang.getData().getApproved())) {
                labelApproved = lang.getData().getApproved();
            }
            if (!TextUtils.isEmpty(lang.getData().getPending())) {
                labelPending = lang.getData().getPending();
            }
            if (!TextUtils.isEmpty(lang.getData().getRejected())) {
                labelRejected = lang.getData().getRejected();
            }
            if (!TextUtils.isEmpty(lang.getData().getNext())) {
                labelNext = lang.getData().getNext();
                if (pageNextView != null) pageNextView.setText(labelNext);
            }
        } catch (Exception e) {
            showLog("PostArticleStatus tab labels: " + e.getMessage());
        }
        // Prefer string resources (Telugu defaults) when translator has no prev/next.
        if (TextUtils.isEmpty(labelPrev) || "Prev".equals(labelPrev)) {
            labelPrev = getString(R.string.posts_page_prev);
        }
        if (TextUtils.isEmpty(labelNext) || "Next".equals(labelNext)) {
            labelNext = getString(R.string.posts_page_next);
        }
        if (pagePrevView != null) pagePrevView.setText(labelPrev);
        if (pageNextView != null) pageNextView.setText(labelNext);
    }

    /** Offline / before network: use cached list sizes until API totals arrive. */
    private void seedTabCountsFromCache() {
        int draftId = SharedPrefsData.getInt(
                getApplicationContext(), Constants.DraftID, Constants.PREF_NAME);
        countApproved = cachedListSize(draftId, 1);
        countPending = cachedListSize(draftId, 0);
        countRejected = cachedListSize(draftId, 2);
    }

    private int cachedListSize(int draftId, int status) {
        List<PostsModel.Datum> cached =
                PostStatusListCache.load(this, draftId, resolvedMobile, status);
        return cached != null ? cached.size() : 0;
    }

    /**
     * Prefer server totals from get_user_posts (approved_count / pending_count / rejected_count)
     * so tabs match profile totals even when the list page is capped at 10.
     */
    private void applyStatusCountsFromResponse(PostsModel postsModel) {
        if (postsModel == null) return;
        if (postsModel.getApprovedCount() != null) {
            countApproved = Math.max(0, postsModel.getApprovedCount());
        }
        if (postsModel.getPendingCount() != null) {
            countPending = Math.max(0, postsModel.getPendingCount());
        }
        if (postsModel.getRejectedCount() != null) {
            countRejected = Math.max(0, postsModel.getRejectedCount());
        }
        refreshTabCountLabels();
    }

    private void refreshTabCountLabels() {
        if (isMetricMode()) return;
        setTabPairText(tabApprovedSelected, tabApprovedUnselected, labelApproved, countApproved);
        setTabPairText(tabPendingSelected, tabPendingUnselected, labelPending, countPending);
        setTabPairText(tabRejectedSelected, tabRejectedUnselected, labelRejected, countRejected);
        refreshPagerUi();
    }

    private static void setTabPairText(
            TextView selected, TextView unselected, String baseLabel, int count) {
        int n = count < 0 ? 0 : count;
        String text = baseLabel + " (" + n + ")";
        if (selected != null) selected.setText(text);
        if (unselected != null) unselected.setText(text);
    }

    /**
     * Instant paint from per-tab cache, then soft background refresh.
     * Never clears an already-shown list while waiting on network.
     */
    private void showTab(int status, boolean isInitialOpen) {
        currentStatus = status;
        resetPaging();
        final int draftId = SharedPrefsData.getInt(
                getApplicationContext(), Constants.DraftID, Constants.PREF_NAME);
        if (TextUtils.isEmpty(resolvedMobile)) {
            resolvedMobile = resolveMobileNumber();
        }

        if (draftId <= 0 && TextUtils.isEmpty(resolvedMobile)) {
            stopRefreshing();
            showEmpty("Login again — missing draft or mobile.");
            Toast.makeText(this, "Missing account identity. Please re-login.", Toast.LENGTH_LONG).show();
            return;
        }

        // Date filter active → skip stale cache (cache is unfiltered).
        boolean dateFilterOn = !TextUtils.isEmpty(filterStart) || !TextUtils.isEmpty(filterTo);
        List<PostsModel.Datum> cached = dateFilterOn
                ? null
                : PostStatusListCache.load(this, draftId, resolvedMobile, status);
        boolean hasCache = cached != null;

        if (hasCache && currentPage == 1) {
            flatArticles.clear();
            int max = Math.min(cached.size(), PAGE_LIMIT);
            for (int i = 0; i < max; i++) {
                PostsModel.Datum d = cached.get(i);
                if (d != null && !d.isWeekHeader) flatArticles.add(d);
            }
            paintList(status, withWeekHeaders(flatArticles));
        } else {
            paintList(status, new ArrayList<PostsModel.Datum>());
            if (emptyState != null) {
                emptyState.setVisibility(View.GONE);
            }
        }

        boolean online = MyUtils.isNetworkAvailable(this);
        if (!online) {
            stopRefreshing();
            if (!hasCache) {
                showEmpty("Offline — no cached articles.");
            }
            refreshPagerUi();
            return;
        }

        fetchUserPosts(status, false, 1);

        if (isInitialOpen && !isMetricMode()) {
            prefetchOtherTabs(status);
        }
    }

    /** Kick off background loads for the other two tabs so switches are instant. */
    private void prefetchOtherTabs(int visibleStatus) {
        if (prefetchedOthers) return;
        prefetchedOthers = true;
        post_status.post(() -> {
            if (isFinishing()) return;
            for (int s : ALL_STATUSES) {
                if (s == visibleStatus) continue;
                fetchUserPosts(s, false, 1);
            }
        });
    }

    /**
     * After reinstall prefs are gone. Prefer explicit intent from profile, then saved prefs,
     * then last profile API cache so get_user_posts still receives mobile_number.
     */
    private String resolveMobileNumber() {
        String fromIntent = getIntent() != null ? getIntent().getStringExtra(EXTRA_MOBILE_NUMBER) : null;
        if (isUsableMobile(fromIntent)) {
            return normalizeMobile(fromIntent);
        }

        String fromPrefs = SharedPrefsData.getString(getApplicationContext(),
                Constants.Selected_Phone_Number, Constants.PREF_NAME);
        if (isUsableMobile(fromPrefs)) {
            return normalizeMobile(fromPrefs);
        }

        try {
            String cached = SharedPrefsData.getString(getApplicationContext(),
                    Constants.PROFILE_CACHE, Constants.PREF_NAME);
            if (!TextUtils.isEmpty(cached)) {
                NewUserProfileResponse resp = new Gson().fromJson(cached, NewUserProfileResponse.class);
                if (resp != null && resp.getData() != null
                        && isUsableMobile(resp.getData().getMobileNumber())) {
                    return normalizeMobile(resp.getData().getMobileNumber());
                }
            }
        } catch (Exception e) {
            showLog("PostArticleStatus resolveMobile cache: " + e.getMessage());
        }
        return "";
    }

    private static boolean isUsableMobile(String mobile) {
        if (mobile == null) return false;
        String t = mobile.trim();
        if (t.isEmpty() || "null".equalsIgnoreCase(t) || "N/A".equalsIgnoreCase(t)) {
            return false;
        }
        String digits = t.replaceAll("\\D", "");
        return digits.length() >= 10;
    }

    private static String normalizeMobile(String mobile) {
        String digits = String.valueOf(mobile).replaceAll("\\D", "");
        if (digits.length() > 10) {
            digits = digits.substring(digits.length() - 10);
        }
        return digits;
    }

    private JsonObject buildRequestBody(int status, int skip, long firstPost) {
        PostNewsRequest requestModel = new PostNewsRequest();

        int draftId = SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME);
        if (TextUtils.isEmpty(resolvedMobile)) {
            resolvedMobile = resolveMobileNumber();
        }
        if (!TextUtils.isEmpty(resolvedMobile)) {
            SharedPrefsData.putString(getApplicationContext(), Constants.Selected_Phone_Number,
                    resolvedMobile, Constants.PREF_NAME);
        }

        requestModel.setDraftId(draftId);
        requestModel.setMobileNumber(resolvedMobile != null ? resolvedMobile : "");
        requestModel.setPostStatus(status);
        requestModel.setFirstPostId(firstPost > 0 ? firstPost : 0L);
        requestModel.setSkip(skip);
        requestModel.setLimit(PAGE_LIMIT);
        requestModel.setGroupByWeek(true);
        // Metric screens need likes/comments/views; status tabs stay lean.
        requestModel.setIncludeEngagement(isMetricMode());
        requestModel.setLanguageId(SharedPrefsData.getInt(
                getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME));
        String start = filterStart;
        String floor = ReporterPaymentAppConfig.articlesFromDate(this);
        if (!TextUtils.isEmpty(floor)
                && (TextUtils.isEmpty(start) || start.compareTo(floor) < 0)) {
            start = floor;
        }
        if (!TextUtils.isEmpty(start)) {
            requestModel.setStartTime(start);
        }
        if (!TextUtils.isEmpty(filterTo)) {
            requestModel.setEndTime(filterTo);
        }

        String jsonString = new Gson().toJson(requestModel);
        String encrpt_data = encrypt(jsonString);
        showLog("API REQUEST get_user_posts plain=" + jsonString);
        EncritonRequestData encritonRequest = new EncritonRequestData();
        encritonRequest.setData(encrpt_data);
        return new Gson().toJsonTree(encritonRequest).getAsJsonObject();
    }

    /**
     * Network load for one status page. Does not clear the current adapter until response.
     *
     * @param userPull true only for SwipeRefresh / explicit filter apply.
     * @param page     1-based page index (admin-style skip/limit).
     */
    private void fetchUserPosts(final int status, final boolean userPull, final int page) {
        final int draftId = SharedPrefsData.getInt(
                getApplicationContext(), Constants.DraftID, Constants.PREF_NAME);
        if (draftId <= 0 && TextUtils.isEmpty(resolvedMobile)) {
            if (userPull) stopRefreshing();
            return;
        }
        if (status < 0 || status > 2) return;

        final int targetPage = Math.max(1, page);
        final boolean isVisible = status == currentStatus;

        if (isVisible) {
            if (loadingPage && !userPull && targetPage == currentPage) {
                return;
            }
            loadingPage = true;
        } else {
            // Prefetch other tabs: avoid stacking identical background fetches.
            if (!userPull && inflight[status]) {
                return;
            }
            inflight[status] = true;
        }

        // Page 1 resets the sticky cursor; later pages reuse firstPostId.
        final long reqFirst;
        final int reqSkip = (targetPage - 1) * PAGE_LIMIT;
        if (isVisible) {
            if (targetPage == 1) {
                firstPostId = 0L;
                nextSkip = 0;
                hasMore = false;
                // Keep flatArticles until response so soft refresh does not flash empty.
            }
            currentPage = targetPage;
            reqFirst = targetPage == 1 ? 0L : firstPostId;
            refreshPagerUi();
        } else {
            reqFirst = 0L;
        }

        final int gen = requestGen.incrementAndGet(status);

        if (userPull && swipeRefresh != null && isVisible) {
            swipeRefresh.setRefreshing(true);
        }

        if (!MyUtils.isNetworkAvailable(this)) {
            if (isVisible) {
                loadingPage = false;
            } else {
                inflight[status] = false;
            }
            if (userPull && isVisible) {
                stopRefreshing();
                if (list == null || list.isEmpty()) {
                    showEmpty("Offline — no cached articles.");
                } else {
                    Toast.makeText(this, "Offline — showing cached list", Toast.LENGTH_SHORT).show();
                }
            }
            refreshPagerUi();
            return;
        }

        JsonObject body = buildRequestBody(status, reqSkip, reqFirst);
        showLog("API REQ get_user_posts encrypted body " + body.toString());
        APIService apiService = RestApiClient.getClient().create(APIService.class);
        Call<EncreptionRequest> apiResponse = apiService.get_user_posts("v3", body);

        apiResponse.enqueue(new Callback<EncreptionRequest>() {
            @Override
            public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {
                if (isVisible) {
                    loadingPage = false;
                    stopRefreshing();
                } else {
                    inflight[status] = false;
                }

                try {
                    if (!response.isSuccessful() || response.body() == null || response.body().getData() == null) {
                        if (isVisible && gen == requestGen.get(status)) {
                            handleFetchError(status, "Could not load articles.",
                                    "Failed to load posts (" + response.code() + ")", true);
                            refreshPagerUi();
                        }
                        return;
                    }

                    EncreptionRequest encreptionRequest = response.body();
                    showLog("API RES get_user_posts cipher len=" + encreptionRequest.getData().length());
                    String plain = decrypt(encreptionRequest.getData());
                    showLog("API RES get_user_posts plain=" + plain);

                    PostsModel postsModel = new Gson().fromJson(plain, PostsModel.class);
                    if (postsModel == null) {
                        if (isVisible && gen == requestGen.get(status)) {
                            handleFetchError(status, "Invalid response.", null, false);
                            refreshPagerUi();
                        }
                        return;
                    }

                    if (Boolean.TRUE.equals(postsModel.getSuccess())) {
                        List<PostsModel.Datum> data = postsModel.getData();
                        ArrayList<PostsModel.Datum> pageRows = new ArrayList<>();
                        if (data != null) {
                            for (PostsModel.Datum d : data) {
                                if (d != null && !d.isWeekHeader) pageRows.add(d);
                            }
                        }

                        if (isVisible && gen == requestGen.get(status)) {
                            flatArticles.clear();
                            flatArticles.addAll(pageRows);
                            if (postsModel.getSkip() != null) {
                                nextSkip = postsModel.getSkip();
                            } else {
                                nextSkip = reqSkip + pageRows.size();
                            }
                            if (targetPage == 1) {
                                if (postsModel.getFirstPostId() != null && postsModel.getFirstPostId() > 0) {
                                    firstPostId = postsModel.getFirstPostId();
                                } else if (!flatArticles.isEmpty() && flatArticles.get(0).getCreatedAt() != null) {
                                    firstPostId = flatArticles.get(0).getCreatedAt();
                                } else {
                                    firstPostId = 0L;
                                }
                            }
                            hasMore = Boolean.TRUE.equals(postsModel.getHasMore());

                            boolean dateFilterOn = !TextUtils.isEmpty(filterStart) || !TextUtils.isEmpty(filterTo);
                            // Cache page-1 only (instant tab paint); never cache filtered pages.
                            if (targetPage == 1 && !dateFilterOn) {
                                PostStatusListCache.save(
                                        getApplicationContext(), draftId, resolvedMobile, status,
                                        new ArrayList<>(flatArticles));
                            }

                            applyStatusCountsFromResponse(postsModel);
                            paintList(status, withWeekHeaders(flatArticles));
                            refreshPagerUi();
                        } else if (!isVisible) {
                            // Prefetch other tabs: cache page-1 flat list only
                            applyStatusCountsFromResponse(postsModel);
                            boolean dateFilterOn = !TextUtils.isEmpty(filterStart) || !TextUtils.isEmpty(filterTo);
                            if (!dateFilterOn && targetPage == 1) {
                                PostStatusListCache.save(
                                        getApplicationContext(), draftId, resolvedMobile, status, pageRows);
                            }
                        }
                    } else {
                        String msg = "Could not load articles.";
                        try {
                            com.google.gson.JsonObject o = new Gson().fromJson(plain, com.google.gson.JsonObject.class);
                            if (o != null && o.has("msg") && !o.get("msg").isJsonNull()) {
                                msg = o.get("msg").getAsString();
                            }
                        } catch (Exception ignored) {
                        }
                        if (isVisible && gen == requestGen.get(status)) {
                            if (list == null || list.isEmpty()) {
                                showEmpty(msg);
                            }
                            Toast.makeText(PostArticleStatus.this, msg, Toast.LENGTH_LONG).show();
                            refreshPagerUi();
                        }
                    }
                } catch (Exception e) {
                    showLog("get_user_posts parse error: " + e.getMessage());
                    if (isVisible && gen == requestGen.get(status)) {
                        handleFetchError(status, "Error showing articles.", "Error loading articles", true);
                        refreshPagerUi();
                    }
                }
            }

            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {
                if (isVisible) {
                    loadingPage = false;
                    stopRefreshing();
                } else {
                    inflight[status] = false;
                }
                showLog("get_user_posts onFailure: " + (t != null ? t.getMessage() : "null"));
                if (isVisible && gen == requestGen.get(status)) {
                    handleFetchError(status, "Network error.",
                            "Network error loading articles", true);
                    refreshPagerUi();
                }
            }
        });
    }

    /** Insert week section headers (ISO week) for reporter browsing. */
    private ArrayList<PostsModel.Datum> withWeekHeaders(List<PostsModel.Datum> articles) {
        ArrayList<PostsModel.Datum> out = new ArrayList<>();
        if (articles == null || articles.isEmpty()) return out;
        if (isMetricMode()) {
            out.addAll(articles);
            return out;
        }
        Map<String, List<PostsModel.Datum>> byWeek = new LinkedHashMap<>();
        for (PostsModel.Datum d : articles) {
            if (d == null || d.isWeekHeader) continue;
            String key = d.getWeekKey();
            if (TextUtils.isEmpty(key)) {
                key = weekKeyFromCreated(d.getCreatedAt());
            }
            if (!byWeek.containsKey(key)) byWeek.put(key, new ArrayList<PostsModel.Datum>());
            byWeek.get(key).add(d);
        }
        for (Map.Entry<String, List<PostsModel.Datum>> e : byWeek.entrySet()) {
            List<PostsModel.Datum> group = e.getValue();
            PostsModel.Datum header = new PostsModel.Datum();
            header.isWeekHeader = true;
            header.headerCount = group.size();
            String label = null;
            if (!group.isEmpty() && !TextUtils.isEmpty(group.get(0).getWeekLabel())) {
                label = group.get(0).getWeekLabel();
            }
            if (TextUtils.isEmpty(label)) label = "Week " + e.getKey();
            header.headerLabel = label;
            out.add(header);
            out.addAll(group);
        }
        return out;
    }

    private static String weekKeyFromCreated(Long createdAtMs) {
        if (createdAtMs == null || createdAtMs <= 0) return "unknown";
        Calendar c = Calendar.getInstance();
        c.setFirstDayOfWeek(Calendar.MONDAY);
        c.setMinimalDaysInFirstWeek(4);
        c.setTimeInMillis(createdAtMs);
        return c.get(Calendar.YEAR) + "-W" + String.format(Locale.US, "%02d", c.get(Calendar.WEEK_OF_YEAR));
    }

    /**
     * On failure: keep existing cached UI; only toast/empty when there is nothing to show.
     */
    private void handleFetchError(int status, String emptyMsg, String toast, boolean showToast) {
        boolean hasItems = list != null && !list.isEmpty()
                && postsStatusAdopter != null;
        if (hasItems) {
            if (emptyState != null) {
                emptyState.setVisibility(View.GONE);
            }
            return;
        }
        int draftId = SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME);
        List<PostsModel.Datum> cached =
                PostStatusListCache.load(this, draftId, resolvedMobile, status);
        if (cached != null) {
            flatArticles.clear();
            for (PostsModel.Datum d : cached) {
                if (d != null && !d.isWeekHeader) flatArticles.add(d);
            }
            paintList(status, withWeekHeaders(flatArticles));
            return;
        }
        showEmpty(emptyMsg);
        if (showToast && toast != null) {
            Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
        }
    }

    /** Reuse single adapter; mutate shared list and notify — never flash empty while soft-refreshing. */
    private void paintList(int status, List<PostsModel.Datum> data) {
        if (list == null) {
            list = new ArrayList<>();
        }
        list.clear();
        if (data != null) {
            list.addAll(data);
        }
        if (isMetricMode()) {
            ArrayList<PostsModel.Datum> onlyPosts = new ArrayList<>();
            for (PostsModel.Datum d : list) {
                if (d != null && !d.isWeekHeader) onlyPosts.add(d);
            }
            sortListByMetric(onlyPosts);
            list.clear();
            list.addAll(onlyPosts);
            if (pageTitle != null) {
                pageTitle.setText(buildMetricTitle());
            }
        }
        if (linearLayoutManager == null) {
            linearLayoutManager = new LinearLayoutManager(this);
            post_status.setLayoutManager(linearLayoutManager);
        }
        if (postsStatusAdopter == null) {
            postsStatusAdopter = new PostsStatusAdopter(this, this, list, status, metricMode);
            post_status.setAdapter(postsStatusAdopter);
        } else {
            postsStatusAdopter.setMetricMode(metricMode);
            postsStatusAdopter.notifyDataSetChanged();
        }
        if (list.isEmpty()) {
            int draftId = SharedPrefsData.getInt(
                    getApplicationContext(), Constants.DraftID, Constants.PREF_NAME);
            if (PostStatusListCache.hasLoaded(this, draftId, resolvedMobile, status)
                    || !TextUtils.isEmpty(filterStart) || !TextUtils.isEmpty(filterTo)) {
                showEmpty(isMetricMode()
                        ? "No articles for this metric yet."
                        : "No articles in this tab.");
            } else if (emptyState != null) {
                emptyState.setVisibility(View.GONE);
            }
        } else if (emptyState != null) {
            emptyState.setVisibility(View.GONE);
        }
    }

    private void sortListByMetric(List<PostsModel.Datum> items) {
        if (items == null || items.size() < 2 || TextUtils.isEmpty(metricMode)) return;
        final String m = metricMode;
        Collections.sort(items, new Comparator<PostsModel.Datum>() {
            @Override
            public int compare(PostsModel.Datum a, PostsModel.Datum b) {
                int va = metricValue(a, m);
                int vb = metricValue(b, m);
                return Integer.compare(vb, va); // high first
            }
        });
    }

    private static int metricValue(PostsModel.Datum d, String metric) {
        if (d == null) return 0;
        if ("likes".equals(metric)) return d.getLikes() != null ? d.getLikes() : 0;
        if ("comments".equals(metric)) return d.getComments() != null ? d.getComments() : 0;
        if ("views".equals(metric)) return d.getViews() != null ? d.getViews() : 0;
        return 0;
    }

    private void showEmpty(String message) {
        if (emptyState != null) {
            emptyState.setText(message);
            emptyState.setVisibility(View.VISIBLE);
        }
    }

    private void stopRefreshing() {
        if (swipeRefresh != null) {
            swipeRefresh.setRefreshing(false);
        }
    }

    /** @deprecated kept for external call compatibility; use {@link #getUserPosts(int)} */
    public void getDisrics(int status) {
        getUserPosts(status);
    }

    /** Public entry: same as tab select — instant cache + soft refresh. */
    public void getUserPosts(int status) {
        if (status == 1) {
            selectApprovedTab();
        } else if (status == 0) {
            selectPendingTab();
        } else if (status == 2) {
            selectRejectedTab();
        }
        showTab(status, false);
    }
}
