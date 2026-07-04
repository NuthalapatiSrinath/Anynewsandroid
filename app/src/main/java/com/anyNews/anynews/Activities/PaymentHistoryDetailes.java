package com.anyNews.anynews.Activities;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.anyNews.anynews.Adapters.PaymentHistoryAdopter;
import com.anyNews.anynews.Pojo.EncreptionRequest;
import com.anyNews.anynews.Pojo.EncritonRequestData;
import com.anyNews.anynews.Pojo.PaymentHistoryRequest;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.anyNews.anynews.models.PaymentHistoryModel;
import com.anyNews.anynews.testing.MainActivity;
import com.anyNews.anynews.testing.PaginationListener;
import com.anyNews.anynews.testing.PostItem;
import com.anyNews.anynews.testing.PostRecyclerAdapter;
import com.anyNews.anynews.Ui.NeomorphFrameLayout;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.anyNews.anynews.Utils.AppHelper.showLog;
import static com.anyNews.anynews.Utils.MyUtils.decrypt;
import static com.anyNews.anynews.Utils.MyUtils.encrypt;
import static com.anyNews.anynews.testing.PaginationListener.PAGE_START;

public class PaymentHistoryDetailes extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    @BindView(R.id.swipeRefresh)
    SwipeRefreshLayout swipeRefresh;
    LinearLayoutManager linearLayoutManager;
    PaymentHistoryAdopter adapter;
    private int currentPage = PAGE_START;
    ArrayList<PaymentHistoryModel.Datum>  paymentHistoryModel = new ArrayList<PaymentHistoryModel.Datum>();
    LinearLayout txtNoData,loading;
    NeomorphFrameLayout neomorphFrameLayout_approved_un_selected, neomorphFrameLayout_approved_selected,
            neomorphFrameLayout_pending_un_selected, neomorphFrameLayout_pending_selected;
    LinearLayout back_button;
    private Activity Activity;
    TextView payment_total_amount;
    NestedScrollView nested_scrollview_detailes;
    ProgressBar progress_bar;

    long page=0; long limit=0;
    private int totalPage = 10;
    private boolean isLoading = false;
    int itemCount = 0;
    public int article_refersh = 1;

    private boolean isLastPage = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_history);
        ButterKnife.bind(this);

        payment_total_amount = findViewById(R.id.payment_total_amount);
        loading = findViewById(R.id.loading);
        txtNoData = findViewById(R.id.txtNoData);


        swipeRefresh.setOnRefreshListener(this);

        mRecyclerView.setHasFixedSize(true);


        neomorphFrameLayout_approved_selected = findViewById(R.id.neomorphFrameLayout_approved_selected);
        neomorphFrameLayout_approved_un_selected = findViewById(R.id.neomorphFrameLayout_approved_un_selected);
        neomorphFrameLayout_pending_un_selected = findViewById(R.id.neomorphFrameLayout_pending_un_selected);
        neomorphFrameLayout_pending_selected = findViewById(R.id.neomorphFrameLayout_pending_selected);
        nested_scrollview_detailes = findViewById(R.id.nested_scrollview_detailes);

        loading.setVisibility(View.VISIBLE);
        txtNoData.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.GONE);


        neomorphFrameLayout_approved_un_selected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                neomorphFrameLayout_approved_selected.setVisibility(View.VISIBLE);
                neomorphFrameLayout_approved_un_selected.setVisibility(View.GONE);
                neomorphFrameLayout_pending_selected.setVisibility(View.GONE);
                neomorphFrameLayout_pending_un_selected.setVisibility(View.VISIBLE);

                loading.setVisibility(View.VISIBLE);
                txtNoData.setVisibility(View.GONE);
                swipeRefresh.setVisibility(View.GONE);
                adapter.clear();
                page = 0;
                limit = 0;
                currentPage = 1;
                article_refersh = 1;
                totalPage = 10;

                doApiCall(1, page, limit);
            }
        });

        neomorphFrameLayout_pending_un_selected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                neomorphFrameLayout_approved_selected.setVisibility(View.GONE);
                neomorphFrameLayout_approved_un_selected.setVisibility(View.VISIBLE);
                neomorphFrameLayout_pending_selected.setVisibility(View.VISIBLE);
                neomorphFrameLayout_pending_un_selected.setVisibility(View.GONE);

                loading.setVisibility(View.VISIBLE);
                txtNoData.setVisibility(View.GONE);
                swipeRefresh.setVisibility(View.GONE);
                page = 0;
                limit = 0;
                adapter.clear();
                article_refersh = 2;
                currentPage = 1;
                totalPage = 10;


                get_redeem_transactions(2, page, limit);

            }
        });

        neomorphFrameLayout_approved_selected.setVisibility(View.VISIBLE);
        neomorphFrameLayout_approved_un_selected.setVisibility(View.GONE);
        neomorphFrameLayout_pending_selected.setVisibility(View.GONE);
        neomorphFrameLayout_pending_un_selected.setVisibility(View.VISIBLE);
        // use a linear layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);

        adapter = new PaymentHistoryAdopter(PaymentHistoryDetailes.this, new ArrayList<>());
        mRecyclerView.setAdapter(adapter);
        doApiCall(1, page, limit);

        mRecyclerView.addOnScrollListener(new PaginationListener(layoutManager) {
            @Override
            protected void loadMoreItems() {
                isLoading = true;
                currentPage++;
                if (article_refersh == 1) {

                    doApiCall(1, page, limit);
                } else {
                    get_redeem_transactions(2, page, limit);
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


        swipeRefresh.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.GONE);
    }

    /**
     * do api call here to fetch data from server
     * In example i'm adding data manually
     */

    private void doApiCall(int value, long page1, long limit1) {


        JsonObject otp = set_transaction(page1, limit1);

        APIService apiService = RestApiClient.getClient().create(APIService.class);
        Call<EncreptionRequest> apiResponse = apiService.get_article_transactions("v3", otp);

        apiResponse.enqueue(new Callback<EncreptionRequest>() {
            //@RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {

                EncreptionRequest encreptionRequest = response.body();
                String encrpt_data = null;
                /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

                    encrpt_data = decrypt(encreptionRequest.getData());
                }
                */
                encrpt_data = decrypt(encreptionRequest.getData());


                Gson gsonObj = new Gson();
                PostItem languageModel = gsonObj.fromJson(encrpt_data, PostItem.class);

                payment_total_amount.setText("\u20B9" + languageModel.getTotalPoints());


                page = languageModel.getSkip();
                limit = languageModel.getFirstTransactionId();

                if (languageModel.getSuccess()) {

                    final ArrayList<PostItem.Datum> items = new ArrayList<>();

                    if (languageModel.getData().size() > 0) {


                        new Handler().postDelayed(new Runnable() {

                            @Override
                            public void run() {

                                items.addAll(languageModel.getData());

                                Toast.makeText(PaymentHistoryDetailes.this, "VVV"+items.size(), Toast.LENGTH_SHORT).show();

                                if (currentPage != PAGE_START) {

                                    showLog("CURRENT_PAGE DATA" + currentPage + " ,  " + PAGE_START);

                                }
                                if (currentPage != PAGE_START) adapter.removeLoading();
                                adapter.addItems(items);
                                swipeRefresh.setRefreshing(false);


                                swipeRefresh.setVisibility(View.VISIBLE);
                                txtNoData.setVisibility(View.GONE);
                                mRecyclerView.setVisibility(View.VISIBLE);
                                loading.setVisibility(View.GONE);

                                // check weather is last page or not
                                if (currentPage < totalPage) {
                                    adapter.addLoading();
                                } else {
                                    isLastPage = true;
                                }
                                isLoading = false;
                            }
                        }, 1500);


                    } else {

                    }

                }
            }


            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {


            }
        });


    }

    private JsonObject set_transaction(long page, long limit) {

        PaymentHistoryRequest requestModel = new PaymentHistoryRequest();

        requestModel.setUserId((long) 763);
        requestModel.setDraftId(49221);
        requestModel.setSkip((long) page);
        requestModel.setFirstTransactionId((long) limit);

        String jsonString = new com.google.gson.Gson().toJson(requestModel);
        String encrpt_data = null;

        encrpt_data = encrypt(jsonString);
        showLog("API REQUEST E" + jsonString.toString());
        EncritonRequestData Encriton_Request = new EncritonRequestData();
        Encriton_Request.setData(encrpt_data);
        return new Gson().toJsonTree(Encriton_Request).getAsJsonObject();
    }

    private void get_redeem_transactions(int value, long page1, long limit1) {


        JsonObject otp = set_transaction(page1, limit1);

        APIService apiService = RestApiClient.getClient().create(APIService.class);
        Call<EncreptionRequest> apiResponse = apiService.get_redeem_transactions("v3", otp);

        apiResponse.enqueue(new Callback<EncreptionRequest>() {
            //@RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {

                EncreptionRequest encreptionRequest = response.body();
                String encrpt_data = null;
                /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

                    encrpt_data = decrypt(encreptionRequest.getData());
                }*/
                encrpt_data = decrypt(encreptionRequest.getData());


                Gson gsonObj = new Gson();
                PostItem languageModel = gsonObj.fromJson(encrpt_data, PostItem.class);

                payment_total_amount.setText("\u20B9" + languageModel.getTotalPoints());

                page = languageModel.getSkip();
                limit = languageModel.getFirstTransactionId();
                if (languageModel.getSuccess()) {

                    final ArrayList<PostItem.Datum> items = new ArrayList<>();

                    showLog("AKKISETTY_NARASIMHA" + languageModel.getData().size());
                    if (languageModel.getData().size() > 0) {


                        new Handler().postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                items.addAll(languageModel.getData());

                                showLog("CURRENT_PAGE_NARASIMHA" + currentPage + "  ,  " + PAGE_START);


                                if (currentPage != PAGE_START) adapter.removeLoading();
                                adapter.addItems(items);
                                swipeRefresh.setRefreshing(false);


                                swipeRefresh.setVisibility(View.VISIBLE);
                                txtNoData.setVisibility(View.GONE);
                                mRecyclerView.setVisibility(View.VISIBLE);
                                loading.setVisibility(View.GONE);
                                // check weather is last page or not
                                if (currentPage < totalPage) {
                                    adapter.addLoading();
                                } else {
                                    isLastPage = true;
                                }
                                isLoading = false;
                            }
                        }, 1500);


                    } else {

                    }

                }
            }

            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {

            }
        });

    }

    @Override
    public void onRefresh() {
        itemCount = 0;
        currentPage = PAGE_START;
        isLastPage = false;
        adapter.clear();
        page = 0;
        limit = 0;

        if (article_refersh == 1) {

            doApiCall(1, page, limit);

        } else {

            get_redeem_transactions(1, page, limit);
        }
    }
}
