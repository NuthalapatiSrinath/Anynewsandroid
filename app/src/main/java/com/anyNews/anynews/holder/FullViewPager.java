package com.anyNews.anynews.holder;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.anyNews.anynews.Activities.FullVIewPager;
import com.anyNews.anynews.Activities.newProfileDetails;
import com.anyNews.anynews.Pojo.EncreptionRequest;
import com.anyNews.anynews.Pojo.EncritonRequestData;
import com.anyNews.anynews.Pojo.LanguageModelResponse;
import com.anyNews.anynews.Pojo.PaymentHistoryRequest;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Utils.Allsharedpreference;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.anyNews.anynews.testing.MainActivity;
import com.anyNews.anynews.testing.PaginationListener;
import com.anyNews.anynews.testing.PostItem;
import com.anyNews.anynews.testing.PostRecyclerAdapter;
import com.anyNews.anynews.Ui.NeomorphFrameLayout;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.rollbar.android.Rollbar;

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

public class FullViewPager extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = "MainActivity";

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    @BindView(R.id.swipeRefresh)
    SwipeRefreshLayout swipeRefresh;
    private android.app.Activity Activity;
    private FullPostRecyclerAdopter adapter;
    private int currentPage = PAGE_START;
    private boolean isLastPage = false;
    private int totalPage = 10;
    private boolean isLoading = false;
    int itemCount = 0;
    long page = 0;
    long limit = 0;
    long article_transation = 1;

    public int article_refersh = 1;

    long total_articles_count = 0;
    NeomorphFrameLayout neomorphFrameLayout_approved_un_selected, neomorphFrameLayout_approved_selected,
            neomorphFrameLayout_pending_un_selected, neomorphFrameLayout_pending_selected,
            neomorphFrameLayout_rejected_un_selected, neomorphFrameLayout_rejectd_selected;
    NestedScrollView nested_scrollview_detailes;

    LinearLayout loading, txtNoData;
    TextView payment_total_amount;
    LinearLayout back_button;

    TextView un_selected_approved, selected_approved, un_selected_pending, selected_pending, selected_rejected, un_selected_rejected;

    LanguageModelResponse languageModelResponse;

    Allsharedpreference prefShare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_pager);
        ButterKnife.bind(this);
        Rollbar.init(this);
        // Rollbar.instance().error(new Exception("This is a test error"));
        payment_total_amount = findViewById(R.id.payment_total_amount);
        loading = findViewById(R.id.loading);
        txtNoData = findViewById(R.id.txtNoData);

        swipeRefresh.setOnRefreshListener(this);
        mRecyclerView.setHasFixedSize(true);
        back_button = findViewById(R.id.back_button);
        neomorphFrameLayout_approved_selected = findViewById(R.id.neomorphFrameLayout_approved_selected);
        neomorphFrameLayout_approved_un_selected = findViewById(R.id.neomorphFrameLayout_approved_un_selected);
        neomorphFrameLayout_pending_un_selected = findViewById(R.id.neomorphFrameLayout_pending_un_selected);
        neomorphFrameLayout_pending_selected = findViewById(R.id.neomorphFrameLayout_pending_selected);
        neomorphFrameLayout_rejected_un_selected = findViewById(R.id.neomorphFrameLayout_rejected_un_selected);
        neomorphFrameLayout_rejectd_selected = findViewById(R.id.neomorphFrameLayout_rejected_selected);
        nested_scrollview_detailes = findViewById(R.id.nested_scrollview_detailes);


        un_selected_approved = findViewById(R.id.un_selected_approved);
        selected_approved = findViewById(R.id.selected_approved);
        un_selected_pending = findViewById(R.id.un_selected_pending);
        selected_pending = findViewById(R.id.selected_pending);
        selected_rejected = findViewById(R.id.selected_rejected);
        un_selected_rejected = findViewById(R.id.un_selected_rejected);

        loading.setVisibility(View.VISIBLE);
        txtNoData.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.GONE);

        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        neomorphFrameLayout_approved_un_selected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                neomorphFrameLayout_rejected_un_selected.setVisibility(View.VISIBLE);
                neomorphFrameLayout_rejectd_selected.setVisibility(View.GONE);
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
                total_articles_count = 0;

                doApiCall(1, page, limit);
            }
        });

        neomorphFrameLayout_rejected_un_selected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                neomorphFrameLayout_rejected_un_selected.setVisibility(View.GONE);
                neomorphFrameLayout_rejectd_selected.setVisibility(View.VISIBLE);
                neomorphFrameLayout_approved_selected.setVisibility(View.GONE);
                neomorphFrameLayout_approved_un_selected.setVisibility(View.VISIBLE);
                neomorphFrameLayout_pending_selected.setVisibility(View.GONE);
                neomorphFrameLayout_pending_un_selected.setVisibility(View.VISIBLE);

                loading.setVisibility(View.VISIBLE);
                txtNoData.setVisibility(View.GONE);
                swipeRefresh.setVisibility(View.GONE);
                page = 0;
                limit = 0;
                adapter.clear();
                article_refersh = 2;
                currentPage = 1;
                totalPage = 10;
                total_articles_count = 0;

                doApiCall(2, page, limit);
            }
        });

        neomorphFrameLayout_pending_un_selected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                neomorphFrameLayout_rejected_un_selected.setVisibility(View.VISIBLE);
                neomorphFrameLayout_rejectd_selected.setVisibility(View.GONE);
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
                article_refersh = 0;
                currentPage = 1;
                totalPage = 10;
                total_articles_count = 0;

                doApiCall(0, page, limit);

            }
        });

        neomorphFrameLayout_approved_selected.setVisibility(View.VISIBLE);
        neomorphFrameLayout_approved_un_selected.setVisibility(View.GONE);
        neomorphFrameLayout_pending_selected.setVisibility(View.GONE);
        neomorphFrameLayout_pending_un_selected.setVisibility(View.VISIBLE);

        // use a linear layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);

        adapter = new FullPostRecyclerAdopter(Activity, FullViewPager.this, new ArrayList<>());
        mRecyclerView.setAdapter(adapter);
        doApiCall(1, page, limit);

        mRecyclerView.addOnScrollListener(new PaginationListener(layoutManager) {
            @Override
            protected void loadMoreItems() {
                isLoading = true;
                currentPage++;

                if (article_refersh == 1) {

                    article_refersh = 1;
                    doApiCall(1, page, limit);

                } else if (article_refersh == 2) {

                    article_refersh = 2;
                    doApiCall(2, page, limit);

                } else if (article_refersh == 0) {

                    article_refersh = 0;
                    doApiCall(0, page, limit);

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

        setStaticdata();

    }

    private void setStaticdata() {
        prefShare = new Allsharedpreference(FullViewPager.this);
        Gson gson = new Gson();
        String Translation = prefShare.getShared_String(Allsharedpreference.Translator);
        languageModelResponse = gson.fromJson(Translation, LanguageModelResponse.class);

        un_selected_approved.setText("" + languageModelResponse.getData().getApproved());
        selected_approved.setText("" + languageModelResponse.getData().getApproved());
        un_selected_pending.setText("" + languageModelResponse.getData().getPending());
        selected_pending.setText("" + languageModelResponse.getData().getPending());
        selected_rejected.setText("" + languageModelResponse.getData().getRejected());
        un_selected_rejected.setText("" + languageModelResponse.getData().getRejected());
    }

    /**
     * do api call here to fetch data from server
     * In example i'm adding data manually
     */

    private void doApiCall(int value, long page1, long limit1) {

        try {
            JsonObject otp = set_transaction(value, page1, limit1);

            APIService apiService = RestApiClient.getClient().create(APIService.class);
            Call<EncreptionRequest> apiResponse = apiService.get_user_posts("v3", otp);

            apiResponse.enqueue(new Callback<EncreptionRequest>() {
                //@RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {

                    try {

                        EncreptionRequest encreptionRequest = response.body();
                        String encrpt_data = null;

                        encrpt_data = decrypt(encreptionRequest.getData());
                        Gson gsonObj = new Gson();
                        PostItem languageModel = gsonObj.fromJson(encrpt_data, PostItem.class);

                        page = languageModel.getSkip();
                        limit = languageModel.getFirstPostId();

                        if (languageModel.getSuccess()) {

                            final ArrayList<PostItem.Datum> items = new ArrayList<>();

                            if (languageModel.getData().size() > 0) {


                                new Handler().postDelayed(new Runnable() {

                                    @Override
                                    public void run() {
                                        items.addAll(languageModel.getData());
                                        total_articles_count = items.size();

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

                                        if (items.size() >= 10) {

                                        } else {

                                            adapter.removeLoading();
                                        }
                                    }
                                }, 1500);


                            } else {


                                if (total_articles_count == 0) {
                                    swipeRefresh.setVisibility(View.GONE);
                                    txtNoData.setVisibility(View.VISIBLE);
                                    mRecyclerView.setVisibility(View.GONE);
                                    loading.setVisibility(View.GONE);


                                } else {
                                    swipeRefresh.setVisibility(View.VISIBLE);
                                    txtNoData.setVisibility(View.GONE);
                                    mRecyclerView.setVisibility(View.VISIBLE);
                                    loading.setVisibility(View.GONE);

                                }
                                if ((items.size() != 0)) {

                                    if (items.size() == total_articles_count) {
                                        adapter.removeLoading();
                                    } else {


                                    }
                                }

                            }

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<EncreptionRequest> call, Throwable t) {
                }
            });
        } catch (Exception e) {
        }
    }

    private JsonObject set_transaction(int value, long page, long limit) {

        PaymentHistoryRequest requestModel = new PaymentHistoryRequest();

        requestModel.setUserId((long) SharedPrefsData.getLong(getApplicationContext(), Constants.USER_ID, Constants.PREF_NAME));
        requestModel.setDraftId(SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME));
        requestModel.setMobileNumber(SharedPrefsData.getString(getApplicationContext(), Constants.Selected_Phone_Number, Constants.PREF_NAME));
        requestModel.setPostStatus((long) value);
        requestModel.setSkip((long) page);
        requestModel.setFirstPostId((long) limit);

        String jsonString = new com.google.gson.Gson().toJson(requestModel);
        String encrpt_data = null;
        encrpt_data = encrypt(jsonString);
        showLog("API REQUEST E" + jsonString.toString());
        EncritonRequestData Encriton_Request = new EncritonRequestData();
        Encriton_Request.setData(encrpt_data);
        return new Gson().toJsonTree(Encriton_Request).getAsJsonObject();
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

        }
        if (article_refersh == 2) {

            doApiCall(2, page, limit);

        } else {

            doApiCall(0, page, limit);
        }
    }
}
