package com.anyNews.anynews.testing;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.anyNews.anynews.Pojo.EncreptionRequest;
import com.anyNews.anynews.Pojo.EncritonRequestData;
import com.anyNews.anynews.Pojo.LanguageModelResponse;
import com.anyNews.anynews.Pojo.PaymentHistoryRequest;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Ui.NeomorphFrameLayout;
import com.anyNews.anynews.Utils.Allsharedpreference;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.ReporterPaymentAppConfig;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.anyNews.anynews.Utils.WalletHistoryCache;
import com.anyNews.anynews.Utils.WalletUserId;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.anyNews.anynews.Utils.AppHelper.showLog;
import static com.anyNews.anynews.Utils.MyUtils.decrypt;
import static com.anyNews.anynews.Utils.MyUtils.encrypt;
import static com.anyNews.anynews.testing.PaginationListener.PAGE_START;
import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Wallet history: article amount transactions + payment (redeem) history.
 * Opens immediately with last cached page-0 + header amount; soft-refreshes in background.
 * No artificial delays and no full-screen blocker spinner.
 */
public class MainActivity extends AppCompatActivity
        implements SwipeRefreshLayout.OnRefreshListener {

    public static final String EXTRA_PAYMENT_HISTORY = "PAYMENT_HISTORY";
    /** 1 = article transactions (default), 2 = payment / redeem history */
    public static final String EXTRA_OPEN_TAB = "OPEN_HISTORY_TAB";

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    @BindView(R.id.swipeRefresh)
    SwipeRefreshLayout swipeRefresh;

    private PostRecyclerAdapter adapter;
    private int currentPage = PAGE_START;
    private boolean isLastPage = false;
    private int totalPage = 10;
    private boolean isLoading = false;
    int itemCount = 0;
    long page = 0;
    long limit = 0;

    public int article_refersh = 1;

    NeomorphFrameLayout neomorphFrameLayout_approved_un_selected, neomorphFrameLayout_approved_selected,
            neomorphFrameLayout_pending_un_selected, neomorphFrameLayout_pending_selected;

    LinearLayout loading, txtNoData;
    TextView payment_total_amount;
    LinearLayout back_button;
    TextView un_selected_article_transaction, selected_article_transaction, un_selected_redeem_transaction, selected_redeem_transaction, total_earning;
    LanguageModelResponse languageModelResponse;
    Allsharedpreference prefShare;

    long items_list_count = 0;
    private Call<EncreptionRequest> activeCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_data);
        ButterKnife.bind(this);

        payment_total_amount = findViewById(R.id.payment_total_amount);
        loading = findViewById(R.id.loading);
        txtNoData = findViewById(R.id.txtNoData);
        back_button = findViewById(R.id.back_button);

        un_selected_article_transaction = findViewById(R.id.un_selected_article_transaction);
        selected_article_transaction = findViewById(R.id.selected_article_transaction);
        un_selected_redeem_transaction = findViewById(R.id.un_selected_redeem_transaction);
        selected_redeem_transaction = findViewById(R.id.selected_redeem_transaction);
        total_earning = findViewById(R.id.total_earning);

        bindPaymentHistoryNotice();

        swipeRefresh.setOnRefreshListener(this);
        mRecyclerView.setHasFixedSize(true);

        neomorphFrameLayout_approved_selected = findViewById(R.id.neomorphFrameLayout_approved_selected);
        neomorphFrameLayout_approved_un_selected = findViewById(R.id.neomorphFrameLayout_approved_un_selected);
        neomorphFrameLayout_pending_un_selected = findViewById(R.id.neomorphFrameLayout_pending_un_selected);
        neomorphFrameLayout_pending_selected = findViewById(R.id.neomorphFrameLayout_pending_selected);

        // Never block first paint with full-screen spinner
        loading.setVisibility(View.GONE);
        txtNoData.setVisibility(View.GONE);

        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        neomorphFrameLayout_approved_un_selected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchToArticleTab(true);
            }
        });

        neomorphFrameLayout_pending_un_selected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchToRedeemTab(true);
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);

        adapter = new PostRecyclerAdapter(MainActivity.this, new ArrayList<>());
        mRecyclerView.setAdapter(adapter);

        mRecyclerView.addOnScrollListener(new PaginationListener(layoutManager) {
            @Override
            protected void loadMoreItems() {
                isLoading = true;
                currentPage++;
                if (article_refersh == 1) {
                    doApiCall(false);
                } else {
                    get_redeem_transactions(false);
                }
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }
        });

        setStaticdata();
        paintHeaderAmountInstant();

        int openTab = getIntent() != null ? getIntent().getIntExtra(EXTRA_OPEN_TAB, 1) : 1;
        if (openTab == 2) {
            switchToRedeemTab(false);
        } else {
            switchToArticleTab(false);
        }
    }

    private void switchToArticleTab(boolean fromUser) {
        cancelActiveCall();
        neomorphFrameLayout_approved_selected.setVisibility(View.VISIBLE);
        neomorphFrameLayout_approved_un_selected.setVisibility(View.GONE);
        neomorphFrameLayout_pending_selected.setVisibility(View.GONE);
        neomorphFrameLayout_pending_un_selected.setVisibility(View.VISIBLE);

        resetPaging();
        article_refersh = 1;
        boolean painted = paintCachedList(WalletHistoryCache.TYPE_ARTICLE);
        if (!painted) {
            showSoftLoadingEmpty();
        }
        doApiCall(!painted);
    }

    private void switchToRedeemTab(boolean fromUser) {
        cancelActiveCall();
        neomorphFrameLayout_approved_selected.setVisibility(View.GONE);
        neomorphFrameLayout_approved_un_selected.setVisibility(View.VISIBLE);
        neomorphFrameLayout_pending_selected.setVisibility(View.VISIBLE);
        neomorphFrameLayout_pending_un_selected.setVisibility(View.GONE);

        resetPaging();
        article_refersh = 2;
        boolean painted = paintCachedList(WalletHistoryCache.TYPE_REDEEM);
        if (!painted) {
            showSoftLoadingEmpty();
        }
        get_redeem_transactions(!painted);
    }

    private void resetPaging() {
        page = 0;
        limit = 0;
        currentPage = PAGE_START;
        totalPage = 10;
        isLastPage = false;
        isLoading = false;
        itemCount = 0;
        items_list_count = 0;
        adapter.clear();
    }

    /**
     * Header amount immediately from intent extra / disk / profile prefs — never wait on network.
     */
    private void paintHeaderAmountInstant() {
        String fromIntent = getIntent() != null ? getIntent().getStringExtra(EXTRA_PAYMENT_HISTORY) : null;
        if (fromIntent != null && !fromIntent.trim().isEmpty()) {
            payment_total_amount.setText(fromIntent.trim().startsWith("\u20B9") || fromIntent.contains("₹")
                    ? fromIntent.trim()
                    : "\u20B9" + fromIntent.trim());
            WalletHistoryCache.saveTotalPointsDisplay(this, fromIntent);
            return;
        }
        String cached = WalletHistoryCache.loadTotalPointsDisplay(this);
        if (cached != null && !cached.isEmpty()) {
            payment_total_amount.setText("\u20B9" + cached);
            return;
        }
        try {
            String profileJson = SharedPrefsData.getString(this, Constants.PROFILE_CACHE, Constants.PREF_NAME);
            if (profileJson != null && !profileJson.isEmpty()) {
                com.anyNews.anynews.Pojo.NewUserProfileResponse resp =
                        new Gson().fromJson(profileJson, com.anyNews.anynews.Pojo.NewUserProfileResponse.class);
                if (resp != null && resp.getData() != null) {
                    payment_total_amount.setText("\u20B9" + resp.getData().getTotalPoints());
                }
            }
        } catch (Exception ignored) {
        }
    }

    private boolean paintCachedList(String type) {
        PostItem cached = WalletHistoryCache.load(this, type);
        if (cached == null || cached.getData() == null || cached.getData().isEmpty()) {
            return false;
        }
        if (cached.getTotalPoints() != null) {
            payment_total_amount.setText("\u20B9" + cached.getTotalPoints());
        }
        if (cached.getSkip() != null) page = cached.getSkip();
        if (cached.getFirstTransactionId() != null) limit = cached.getFirstTransactionId();

        List<PostItem.Datum> rows = new ArrayList<>(cached.getData());
        items_list_count = rows.size();
        adapter.setItems(rows);
        showListContent();
        // Soft refresh indicator only — list stays visible
        safeSwipeRefreshing(true);
        return true;
    }

    private void showSoftLoadingEmpty() {
        loading.setVisibility(View.GONE);
        txtNoData.setVisibility(View.GONE);
        swipeRefresh.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
        safeSwipeRefreshing(true);
    }

    private void showListContent() {
        loading.setVisibility(View.GONE);
        txtNoData.setVisibility(View.GONE);
        swipeRefresh.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showEmpty() {
        loading.setVisibility(View.GONE);
        txtNoData.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.GONE);
        swipeRefresh.setVisibility(View.GONE);
        safeSwipeRefreshing(false);
        isLoading = false;
    }

    private void safeSwipeRefreshing(boolean refreshing) {
        if (swipeRefresh != null) {
            swipeRefresh.setRefreshing(refreshing);
        }
    }

    private void setStaticdata() {
        prefShare = new Allsharedpreference(MainActivity.this);
        try {
            Gson gson = new Gson();
            String Translation = prefShare.getShared_String(Allsharedpreference.Translator);
            languageModelResponse = gson.fromJson(Translation, LanguageModelResponse.class);
            if (languageModelResponse != null && languageModelResponse.getData() != null) {
                un_selected_article_transaction.setText("" + languageModelResponse.getData().getArticleTransactions());
                selected_article_transaction.setText("" + languageModelResponse.getData().getArticleTransactions());
                total_earning.setText("" + languageModelResponse.getData().getTotalEarning());
            }
        } catch (Exception ignored) {
        }
        un_selected_redeem_transaction.setText("Payment History");
        selected_redeem_transaction.setText("Payment History");
    }

    private void doApiCall(boolean firstPageEmpty) {
        JsonObject otp = set_transaction(page, limit);
        APIService apiService = RestApiClient.getClient().create(APIService.class);
        cancelActiveCall();
        activeCall = apiService.get_article_transactions("v3", otp);
        final int requestPage = currentPage;

        activeCall.enqueue(new Callback<EncreptionRequest>() {
            @Override
            public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {
                if (isFinishing() || call.isCanceled()) return;
                handleHistoryResponse(response, WalletHistoryCache.TYPE_ARTICLE, requestPage);
            }

            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {
                if (isFinishing() || call.isCanceled()) return;
                safeSwipeRefreshing(false);
                isLoading = false;
                finishLoadingKeepCache();
            }
        });
    }

    private void get_redeem_transactions(boolean firstPageEmpty) {
        JsonObject otp = set_transaction(page, limit);
        APIService apiService = RestApiClient.getClient().create(APIService.class);
        cancelActiveCall();
        activeCall = apiService.get_redeem_transactions("v3", otp);
        final int requestPage = currentPage;

        activeCall.enqueue(new Callback<EncreptionRequest>() {
            @Override
            public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {
                if (isFinishing() || call.isCanceled()) return;
                handleHistoryResponse(response, WalletHistoryCache.TYPE_REDEEM, requestPage);
            }

            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {
                if (isFinishing() || call.isCanceled()) return;
                safeSwipeRefreshing(false);
                isLoading = false;
                finishLoadingKeepCache();
            }
        });
    }

    private void handleHistoryResponse(Response<EncreptionRequest> response, String cacheType, int requestPage) {
        try {
            if (response == null || response.body() == null || response.body().getData() == null) {
                finishLoadingKeepCache();
                return;
            }
            String encrpt_data = decrypt(response.body().getData());
            PostItem languageModel = new Gson().fromJson(encrpt_data, PostItem.class);
            if (languageModel == null) {
                finishLoadingKeepCache();
                return;
            }

            if (languageModel.getTotalPoints() != null) {
                payment_total_amount.setText("\u20B9" + languageModel.getTotalPoints());
            }
            applyPaymentHistoryNoticeFromApi(
                    languageModel.getReporterPaymentHistoryNoticeActive(),
                    languageModel.getReporterPaymentHistoryNoticeText());
            if (languageModel.getSkip() != null) page = languageModel.getSkip();
            if (languageModel.getFirstTransactionId() != null) limit = languageModel.getFirstTransactionId();

            if (!Boolean.TRUE.equals(languageModel.getSuccess())) {
                finishLoadingKeepCache();
                return;
            }

            List<PostItem.Datum> data = languageModel.getData();
            if (data == null) data = new ArrayList<>();

            if (data.isEmpty()) {
                if (requestPage == PAGE_START) {
                    // Real empty from server — keep soft loading if user id may still heal.
                    long uid = WalletUserId.resolve(MainActivity.this);
                    if (uid <= 0) {
                        finishLoadingKeepCache();
                        return;
                    }
                    // Do not persist "known empty" over a previously good non-empty cache.
                    PostItem prior = WalletHistoryCache.load(MainActivity.this, cacheType);
                    if (prior != null && prior.getData() != null && !prior.getData().isEmpty()) {
                        paintCachedList(cacheType);
                        safeSwipeRefreshing(false);
                        isLoading = false;
                        return;
                    }
                    WalletHistoryCache.save(MainActivity.this, cacheType, languageModel);
                    adapter.clear();
                    showEmpty();
                } else {
                    isLastPage = true;
                    showListContent();
                    safeSwipeRefreshing(false);
                }
                isLoading = false;
                return;
            }

            // Cache first page only for instant next open
            if (requestPage == PAGE_START) {
                WalletHistoryCache.save(MainActivity.this, cacheType, languageModel);
            }

            // Apply immediately — no postDelayed fake wait
            if (requestPage != PAGE_START) {
                adapter.removeLoading();
            }

            if (requestPage == PAGE_START) {
                adapter.setItems(new ArrayList<>(data));
            } else {
                adapter.addItems(new ArrayList<>(data));
            }
            items_list_count = data.size();
            showListContent();
            safeSwipeRefreshing(false);

            if (currentPage < totalPage && data.size() >= 10) {
                adapter.addLoading();
            } else {
                isLastPage = true;
                adapter.removeLoading();
            }
            isLoading = false;
        } catch (Exception e) {
            showLog("wallet history parse error: " + e.getMessage());
            finishLoadingKeepCache();
        }
    }

    /**
     * Network failed or soft error: keep any painted/cached rows.
     * Show "No Data Found" only when there is nothing left to show (no cache).
     */
    private void finishLoadingKeepCache() {
        safeSwipeRefreshing(false);
        isLoading = false;
        if (adapter != null && adapter.hasItems()) {
            showListContent();
            return;
        }
        String type = article_refersh == 1
                ? WalletHistoryCache.TYPE_ARTICLE
                : WalletHistoryCache.TYPE_REDEEM;
        if (paintCachedList(type)) {
            safeSwipeRefreshing(false);
            return;
        }
        showEmpty();
    }

    private JsonObject set_transaction(long pageVal, long limitVal) {
        PaymentHistoryRequest requestModel = new PaymentHistoryRequest();
        requestModel.setUserId(WalletUserId.resolve(MainActivity.this));
        requestModel.setDraftId(SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME));
        requestModel.setSkip(pageVal);
        requestModel.setFirstTransactionId(limitVal);

        String jsonString = new Gson().toJson(requestModel);
        String encrpt_data = encrypt(jsonString);
        showLog("API REQUEST E" + jsonString);
        EncritonRequestData Encriton_Request = new EncritonRequestData();
        Encriton_Request.setData(encrpt_data);
        return new Gson().toJsonTree(Encriton_Request).getAsJsonObject();
    }

    private void cancelActiveCall() {
        if (activeCall != null && !activeCall.isCanceled()) {
            activeCall.cancel();
        }
        activeCall = null;
    }

    private void bindPaymentHistoryNotice() {
        applyPaymentHistoryNoticeFromApi(null, null);
    }

    private void applyPaymentHistoryNoticeFromApi(Boolean activeFromApi, String textFromApi) {
        TextView notice = findViewById(R.id.tv_payment_history_notice);
        if (notice == null) return;
        try {
            boolean active = activeFromApi != null
                    ? Boolean.TRUE.equals(activeFromApi)
                    : ReporterPaymentAppConfig.historyNoticeActive(this);
            String msg = textFromApi != null
                    ? textFromApi.trim()
                    : ReporterPaymentAppConfig.historyNoticeText(this);
            if (msg == null) msg = "";
            if (activeFromApi != null || textFromApi != null) {
                ReporterPaymentAppConfig.saveFromServer(
                        this, null, null, null, active, msg);
            }
            if (active && !android.text.TextUtils.isEmpty(msg)) {
                notice.setText(msg);
                notice.setVisibility(View.VISIBLE);
            } else {
                notice.setVisibility(View.GONE);
            }
        } catch (Exception ignored) {
            notice.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        cancelActiveCall();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void onRefresh() {
        itemCount = 0;
        currentPage = PAGE_START;
        isLastPage = false;
        page = 0;
        limit = 0;
        // Keep visible rows until network returns (no flash)
        if (article_refersh == 1) {
            doApiCall(false);
        } else {
            get_redeem_transactions(false);
        }
    }
}
