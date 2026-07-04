package com.anyNews.anynews.testing;

import static android.view.View.VISIBLE;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.anyNews.anynews.Activities.Districs;
import com.anyNews.anynews.Activities.PaymentHistoryDetailes;
import com.anyNews.anynews.Activities.WriterProfileActivity;
import com.anyNews.anynews.Activities.newProfileDetails;
import com.anyNews.anynews.Adapters.DistricsAdopter;
import com.anyNews.anynews.Adapters.PaymentHistoryAdopter;
import com.anyNews.anynews.BuildConfig;
import com.anyNews.anynews.Pojo.DailogSendRespouse;
import com.anyNews.anynews.Pojo.EncreptionRequest;
import com.anyNews.anynews.Pojo.EncritonRequestData;
import com.anyNews.anynews.Pojo.GetReportIssueResponce;
import com.anyNews.anynews.Pojo.LanguageModelResponse;
import com.anyNews.anynews.Pojo.LanguageSelectionRequest;
import com.anyNews.anynews.Pojo.PaymentHistoryRequest;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Utils.Allsharedpreference;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.Logone;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.anyNews.anynews.Ui.NeomorphFrameLayout;
import com.anyNews.anynews.interfaces.CustomPlayerUiController;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerUtils;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.anyNews.anynews.R.drawable.ic__volume_up;
import static com.anyNews.anynews.R.drawable.ic_dislike_half_selected1;
import static com.anyNews.anynews.R.drawable.ic_like_half1;
import static com.anyNews.anynews.R.drawable.ic_like_half_selected1;
import static com.anyNews.anynews.R.drawable.ic_location_icon;
import static com.anyNews.anynews.R.drawable.ic_news_icon;
import static com.anyNews.anynews.R.drawable.ic_volume_off;
import static com.anyNews.anynews.Utils.AppHelper.showLog;
import static com.anyNews.anynews.Utils.MyUtils.decrypt;
import static com.anyNews.anynews.Utils.MyUtils.encrypt;
import static com.anyNews.anynews.testing.PaginationListener.PAGE_START;
import static com.facebook.FacebookSdk.getApplicationContext;
import static com.google.android.exoplayer2.ui.AspectRatioFrameLayout.RESIZE_MODE_FILL;
import static com.google.android.exoplayer2.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT;

public class MainActivity extends AppCompatActivity
        implements SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "MainActivity";

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
    long article_transation = 1;

    public int article_refersh = 1;

    NeomorphFrameLayout neomorphFrameLayout_approved_un_selected, neomorphFrameLayout_approved_selected,
            neomorphFrameLayout_pending_un_selected, neomorphFrameLayout_pending_selected;
    NestedScrollView nested_scrollview_detailes;

    LinearLayout loading, txtNoData;
    TextView payment_total_amount;
    LinearLayout back_button;
    TextView un_selected_article_transaction, selected_article_transaction, un_selected_redeem_transaction, selected_redeem_transaction, total_earning;
    LanguageModelResponse languageModelResponse;
    Allsharedpreference prefShare;

    long items_list_count = 0;

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

        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

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

        adapter = new PostRecyclerAdapter(MainActivity.this, new ArrayList<>());
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

        setStaticdata();
    }

    private void setStaticdata() {
        prefShare = new Allsharedpreference(MainActivity.this);
        Gson gson = new Gson();
        String Translation = prefShare.getShared_String(Allsharedpreference.Translator);
        languageModelResponse = gson.fromJson(Translation, LanguageModelResponse.class);


        un_selected_article_transaction.setText("" + languageModelResponse.getData().getArticleTransactions());
        selected_article_transaction.setText("" + languageModelResponse.getData().getArticleTransactions());
        un_selected_redeem_transaction.setText("" + languageModelResponse.getData().getRedeemTransactions());
        selected_redeem_transaction.setText("" + languageModelResponse.getData().getRedeemTransactions());
        total_earning.setText("" + languageModelResponse.getData().getTotalEarning());

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
                                items_list_count = items.size();

                                if (currentPage != PAGE_START) {
                                    showLog("CURRENT_PAGE DATA" + currentPage + " ,  " + PAGE_START);
                                }
                                if (currentPage != PAGE_START)
                                    adapter.removeLoading();
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


                        if (items.size() == 0) {

                            swipeRefresh.setVisibility(View.GONE);
                            txtNoData.setVisibility(View.VISIBLE);
                            mRecyclerView.setVisibility(View.GONE);
                            loading.setVisibility(View.GONE);
                        } else {
                            if ((items.size() != 0)) {
                                if (items.size() == items_list_count) {

                                    adapter.removeLoading();
                                } else {


                                }

                            }
                        }
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

        requestModel.setUserId((long) SharedPrefsData.getLong(getApplicationContext(), Constants.USER_ID, Constants.PREF_NAME));
        requestModel.setDraftId(SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME));
        requestModel.setSkip((long) page);
        requestModel.setFirstTransactionId((long) limit);

        String jsonString = new com.google.gson.Gson().toJson(requestModel);
        String encrpt_data = null;
        /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            encrpt_data = encrypt(jsonString);
        }*/
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

                                if (items.size() >= 10) {

                                } else {
                                    adapter.removeLoading();
                                }
                            }
                        }, 1500);


                    } else {

                        if (items.size() == 0) {

                            swipeRefresh.setVisibility(View.GONE);
                            txtNoData.setVisibility(View.VISIBLE);
                            mRecyclerView.setVisibility(View.GONE);
                            loading.setVisibility(View.GONE);
                        } else {
                            if ((items.size() != 0)) {
                                if (items.size() == items_list_count) {

                                    adapter.removeLoading();
                                } else {


                                }

                            }
                        }
                    }

                }
            }

            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {

            }
        });

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




