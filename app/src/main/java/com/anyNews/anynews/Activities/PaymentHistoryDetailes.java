package com.anyNews.anynews.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.anyNews.anynews.Adapters.PaymentHistoryAdopter;
import com.anyNews.anynews.Pojo.EncreptionRequest;
import com.anyNews.anynews.Pojo.EncritonRequestData;
import com.anyNews.anynews.Pojo.PaymentHistoryRequest;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.ReporterPaymentAppConfig;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.anyNews.anynews.Utils.WalletHistoryCache;
import com.anyNews.anynews.testing.PaginationListener;
import com.anyNews.anynews.testing.PostItem;
import com.anyNews.anynews.Ui.NeomorphFrameLayout;
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

/**
 * Legacy payment history screen — same instant cache + soft-refresh path as testing.MainActivity.
 */
public class PaymentHistoryDetailes extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    @BindView(R.id.swipeRefresh)
    SwipeRefreshLayout swipeRefresh;
    PaymentHistoryAdopter adapter;
    private int currentPage = PAGE_START;
    LinearLayout txtNoData, loading;
    NeomorphFrameLayout neomorphFrameLayout_approved_un_selected, neomorphFrameLayout_approved_selected,
            neomorphFrameLayout_pending_un_selected, neomorphFrameLayout_pending_selected;
    TextView payment_total_amount;

    long page = 0;
    long limit = 0;
    private int totalPage = 10;
    private boolean isLoading = false;
    public int article_refersh = 1;
    private boolean isLastPage = false;
    private Call<EncreptionRequest> activeCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_history);
        ButterKnife.bind(this);

        payment_total_amount = findViewById(R.id.payment_total_amount);
        loading = findViewById(R.id.loading);
        txtNoData = findViewById(R.id.txtNoData);

        bindPaymentHistoryNotice();

        swipeRefresh.setOnRefreshListener(this);
        mRecyclerView.setHasFixedSize(true);

        neomorphFrameLayout_approved_selected = findViewById(R.id.neomorphFrameLayout_approved_selected);
        neomorphFrameLayout_approved_un_selected = findViewById(R.id.neomorphFrameLayout_approved_un_selected);
        neomorphFrameLayout_pending_un_selected = findViewById(R.id.neomorphFrameLayout_pending_un_selected);
        neomorphFrameLayout_pending_selected = findViewById(R.id.neomorphFrameLayout_pending_selected);

        loading.setVisibility(View.GONE);
        txtNoData.setVisibility(View.GONE);

        neomorphFrameLayout_approved_un_selected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchArticle();
            }
        });

        neomorphFrameLayout_pending_un_selected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchRedeem();
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);

        adapter = new PaymentHistoryAdopter(PaymentHistoryDetailes.this, new ArrayList<>());
        mRecyclerView.setAdapter(adapter);

        mRecyclerView.addOnScrollListener(new PaginationListener(layoutManager) {
            @Override
            protected void loadMoreItems() {
                isLoading = true;
                currentPage++;
                if (article_refersh == 1) {
                    doApiCall();
                } else {
                    get_redeem_transactions();
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

        String cachedTotal = WalletHistoryCache.loadTotalPointsDisplay(this);
        if (cachedTotal != null && !cachedTotal.isEmpty()) {
            payment_total_amount.setText("\u20B9" + cachedTotal);
        }

        switchArticle();
    }

    private void switchArticle() {
        cancelActiveCall();
        neomorphFrameLayout_approved_selected.setVisibility(View.VISIBLE);
        neomorphFrameLayout_approved_un_selected.setVisibility(View.GONE);
        neomorphFrameLayout_pending_selected.setVisibility(View.GONE);
        neomorphFrameLayout_pending_un_selected.setVisibility(View.VISIBLE);
        resetPaging();
        article_refersh = 1;
        if (!paintCache(WalletHistoryCache.TYPE_ARTICLE)) {
            showSoftEmpty();
        }
        doApiCall();
    }

    private void switchRedeem() {
        cancelActiveCall();
        neomorphFrameLayout_approved_selected.setVisibility(View.GONE);
        neomorphFrameLayout_approved_un_selected.setVisibility(View.VISIBLE);
        neomorphFrameLayout_pending_selected.setVisibility(View.VISIBLE);
        neomorphFrameLayout_pending_un_selected.setVisibility(View.GONE);
        resetPaging();
        article_refersh = 2;
        if (!paintCache(WalletHistoryCache.TYPE_REDEEM)) {
            showSoftEmpty();
        }
        get_redeem_transactions();
    }

    private void resetPaging() {
        page = 0;
        limit = 0;
        currentPage = PAGE_START;
        totalPage = 10;
        isLastPage = false;
        isLoading = false;
        adapter.clear();
    }

    private boolean paintCache(String type) {
        PostItem cached = WalletHistoryCache.load(this, type);
        if (cached == null || cached.getData() == null || cached.getData().isEmpty()) {
            return false;
        }
        if (cached.getTotalPoints() != null) {
            payment_total_amount.setText("\u20B9" + cached.getTotalPoints());
        }
        applyPaymentHistoryNoticeFromApi(
                cached.getReporterPaymentHistoryNoticeActive(),
                cached.getReporterPaymentHistoryNoticeText());
        if (cached.getSkip() != null) page = cached.getSkip();
        if (cached.getFirstTransactionId() != null) limit = cached.getFirstTransactionId();
        adapter.setItems(new ArrayList<>(cached.getData()));
        showList();
        safeRefreshing(true);
        return true;
    }

    private void showSoftEmpty() {
        loading.setVisibility(View.GONE);
        txtNoData.setVisibility(View.GONE);
        swipeRefresh.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
        safeRefreshing(true);
    }

    private void showList() {
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
        safeRefreshing(false);
        isLoading = false;
    }

    private void safeRefreshing(boolean r) {
        if (swipeRefresh != null) swipeRefresh.setRefreshing(r);
    }

    private void doApiCall() {
        JsonObject otp = set_transaction(page, limit);
        APIService apiService = RestApiClient.getClient().create(APIService.class);
        cancelActiveCall();
        activeCall = apiService.get_article_transactions("v3", otp);
        final int requestPage = currentPage;
        activeCall.enqueue(new Callback<EncreptionRequest>() {
            @Override
            public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {
                if (isFinishing() || call.isCanceled()) return;
                applyResponse(response, WalletHistoryCache.TYPE_ARTICLE, requestPage);
            }

            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {
                if (isFinishing() || call.isCanceled()) return;
                onNetworkFail();
            }
        });
    }

    private void get_redeem_transactions() {
        JsonObject otp = set_transaction(page, limit);
        APIService apiService = RestApiClient.getClient().create(APIService.class);
        cancelActiveCall();
        activeCall = apiService.get_redeem_transactions("v3", otp);
        final int requestPage = currentPage;
        activeCall.enqueue(new Callback<EncreptionRequest>() {
            @Override
            public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {
                if (isFinishing() || call.isCanceled()) return;
                applyResponse(response, WalletHistoryCache.TYPE_REDEEM, requestPage);
            }

            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {
                if (isFinishing() || call.isCanceled()) return;
                onNetworkFail();
            }
        });
    }

    private void applyResponse(Response<EncreptionRequest> response, String cacheType, int requestPage) {
        try {
            if (response == null || response.body() == null || response.body().getData() == null) {
                onNetworkFail();
                return;
            }
            PostItem languageModel = new Gson().fromJson(decrypt(response.body().getData()), PostItem.class);
            if (languageModel == null) {
                onNetworkFail();
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
                onNetworkFail();
                return;
            }

            List<PostItem.Datum> data = languageModel.getData();
            if (data == null) data = new ArrayList<>();

            if (requestPage == PAGE_START) {
                WalletHistoryCache.save(this, cacheType, languageModel);
            }
            if (requestPage != PAGE_START) {
                adapter.removeLoading();
            }

            if (data.isEmpty()) {
                if (requestPage == PAGE_START) {
                    adapter.clear();
                    showEmpty();
                } else {
                    isLastPage = true;
                    showList();
                    safeRefreshing(false);
                }
                isLoading = false;
                return;
            }

            if (requestPage == PAGE_START) {
                adapter.setItems(new ArrayList<>(data));
            } else {
                adapter.addItems(new ArrayList<>(data));
            }
            showList();
            safeRefreshing(false);
            if (currentPage < totalPage && data.size() >= 10) {
                adapter.addLoading();
            } else {
                isLastPage = true;
                adapter.removeLoading();
            }
            isLoading = false;
        } catch (Exception e) {
            showLog("PaymentHistoryDetailes parse: " + e.getMessage());
            onNetworkFail();
        }
    }

    private void onNetworkFail() {
        safeRefreshing(false);
        isLoading = false;
        if (adapter != null && adapter.getItemCount() > 0) {
            showList();
            return;
        }
        String type = article_refersh == 1
                ? WalletHistoryCache.TYPE_ARTICLE
                : WalletHistoryCache.TYPE_REDEEM;
        if (paintCache(type)) {
            safeRefreshing(false);
            return;
        }
        showEmpty();
    }

    private JsonObject set_transaction(long pageVal, long limitVal) {
        PaymentHistoryRequest requestModel = new PaymentHistoryRequest();
        requestModel.setUserId(SharedPrefsData.getLong(getApplicationContext(), Constants.USER_ID, Constants.PREF_NAME));
        requestModel.setDraftId(SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME));
        requestModel.setSkip(pageVal);
        requestModel.setFirstTransactionId(limitVal);
        requestModel.setLanguageId(
                SharedPrefsData.getInt(
                        getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME));
        String jsonString = new Gson().toJson(requestModel);
        EncritonRequestData Encriton_Request = new EncritonRequestData();
        Encriton_Request.setData(encrypt(jsonString));
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
    public void onRefresh() {
        currentPage = PAGE_START;
        isLastPage = false;
        page = 0;
        limit = 0;
        if (article_refersh == 1) {
            doApiCall();
        } else {
            get_redeem_transactions();
        }
    }
}
