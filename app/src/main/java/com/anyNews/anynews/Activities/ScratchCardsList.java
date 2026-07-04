//  package com.anyNews.anynews.Activities;
//
//import androidx.annotation.RequiresApi;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.widget.NestedScrollView;
//import androidx.localbroadcastmanager.content.LocalBroadcastManager;
//import androidx.recyclerview.widget.GridLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import android.app.Activity;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.content.pm.ActivityInfo;
//import android.os.Build;
//import android.os.Bundle;
//import android.view.View;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.ProgressBar;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.anyNews.anynews.Adapters.DistricsAdopter;
//import com.anyNews.anynews.Adapters.ScratchCardAdopter;
//import com.anyNews.anynews.BuildConfig;
//import com.anyNews.anynews.Pojo.Datum;
//import com.anyNews.anynews.Pojo.EncreptionRequest;
//import com.anyNews.anynews.Pojo.EncritonRequestData;
//import com.anyNews.anynews.Pojo.LanguageModelResponse;
//import com.anyNews.anynews.Pojo.LanguageSelectionRequest;
//import com.anyNews.anynews.Pojo.ScratchCardListRequiest;
//import com.anyNews.anynews.Pojo.ScratchCardRespounse;
//import com.anyNews.anynews.R;
//import com.anyNews.anynews.Services.APIService;
//import com.anyNews.anynews.Services.RestApiClient;
//import com.anyNews.anynews.Utils.Allsharedpreference;
//import com.anyNews.anynews.Utils.Constants;
//import com.anyNews.anynews.Utils.SharedPrefsData;
//import com.anyNews.anynews.models.DistricsModel;
//import com.anyNews.anynews.models.ScratchCardModel;
//import com.google.gson.Gson;
//import com.google.gson.JsonObject;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import in.myinnos.androidscratchcard.ScratchCard;
//import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.Response;
//
//import static com.anyNews.anynews.App.getContext;
//import static com.anyNews.anynews.Utils.AppHelper.showLog;
//import static com.anyNews.anynews.Utils.MyUtils.decrypt;
//import static com.anyNews.anynews.Utils.MyUtils.encrypt;
//
//public class ScratchCardsList extends AppCompatActivity {
//
//    GridLayoutManager gridLayoutManager;
//    ArrayList<ScratchCardModel.Datum> scratchCardRespounses;
//    ScratchCardAdopter scratchCardAdopter;
//    RecyclerView scratcard_recyclerview;
//    TextView total_amount;
//    LinearLayout txtNoData,rewords_total_amount;
//    LinearLayout back_button,layout_with_out_data,layout_with_data;
//    Activity activity;
//    ImageView scratch_card_refer_now;
//    String REFER_LINK;
//    NestedScrollView nexted_scroll_view;
//    long page=0,limit=0;
//    ProgressBar progress_bar;
//    TextView scarch_totoal_earing,norewards;
//    LanguageModelResponse languageModelResponse;
//
//    Allsharedpreference prefShare;
//    long long_scratchCardRespounses;
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_scratch_cards_list);
//        scratcard_recyclerview=findViewById(R.id.scratcard_recyclerview);
//        total_amount=findViewById(R.id.total_amount);
//        back_button=findViewById(R.id.back_button);
//        txtNoData=findViewById(R.id.txtNoData);
//        norewards=findViewById(R.id.norewards);
//        layout_with_out_data=findViewById(R.id.layout_with_out_data);
//        layout_with_data=findViewById(R.id.layout_with_data);
//        nexted_scroll_view=findViewById(R.id.nexted_scroll_view);
//        progress_bar=findViewById(R.id.progress_bar);
//        scarch_totoal_earing=findViewById(R.id.scarch_totoal_earing);
//        scratch_card_refer_now=findViewById(R.id.scratch_card_refer_now);
//        rewords_total_amount=findViewById(R.id.rewords_total_amount);
//        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        scratchCardRespounses=new ArrayList<>();
//
//        if (getIntent().getExtras() != null) {
//             REFER_LINK = getIntent().getStringExtra("REFER_LINK");
//            //The key argument must always match that used send and retrive value from one activity to another.
//        }
//
//        layout_with_data.setVisibility(View.GONE);
//        layout_with_out_data.setVisibility(View.VISIBLE);
//        back_button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//               finish();
//            }
//        });
//
//        scratch_card_refer_now.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                Intent intent = new Intent();
//                intent.setAction(Intent.ACTION_SEND);
//                intent.putExtra(Intent.EXTRA_TEXT,  ""+REFER_LINK);
//                intent.setType("text/plain");
//                if (getApplicationContext() != null && getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)){
//                    startActivity(intent);
//                }
//
//            }
//        });
//
//        LocalBroadcastManager.getInstance(getContext()).registerReceiver(SCARTCH_CARD_ACTIVE,
//                new IntentFilter("SCARTCH_CARD_ACTIVE"));
//
//        nexted_scroll_view.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
//            @Override
//            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
//
//                if(scrollY==v.getChildAt(0).getMeasuredHeight()-v.getMeasuredHeight()){
//
//
//                    progress_bar.setVisibility(View.VISIBLE);
//
//                    get_scratch_cards(page,limit);
//
//                }
//
//            }
//        });
//        get_scratch_cards(page,limit);
//
//     setStaticdata();
//    }
//    private void setStaticdata() {
//        prefShare = new Allsharedpreference(ScratchCardsList.this);
//        Gson gson = new Gson();
//        String Translation = prefShare.getShared_String(Allsharedpreference.Translator);
//        languageModelResponse = gson.fromJson(Translation, LanguageModelResponse.class);
//        scarch_totoal_earing.setText(languageModelResponse.getData().getTotalRewards());
//       norewards.setText(languageModelResponse.getData().getNoRewards());
//
//
//
//    }
//    public BroadcastReceiver SCARTCH_CARD_ACTIVE = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            scratchCardRespounses.clear();
//            get_scratch_cards(0,0);
//        }
//
//    };
//    private JsonObject set_scatch_cards(long page,long limit) {
//
//        ScratchCardListRequiest requestModel = new ScratchCardListRequiest();
//
//        requestModel.setDraftId(SharedPrefsData.getInt(ScratchCardsList.this, Constants.DraftID, Constants.PREF_NAME));
//        requestModel.setUserId(SharedPrefsData.getLong(ScratchCardsList.this, Constants.USER_ID, Constants.PREF_NAME));
//        requestModel.setSkip((long)page);
//        requestModel.setFirstCardId((long)limit);
//
//        String jsonString = new com.google.gson.Gson().toJson(requestModel);
//        String encrpt_data = null;
//        /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//            encrpt_data = encrypt(jsonString);
//        }*/
//        encrpt_data = encrypt(jsonString);
//
//        EncritonRequestData Encriton_Request=new EncritonRequestData();
//        Encriton_Request.setData(encrpt_data);
//
//        return new Gson().toJsonTree(Encriton_Request).getAsJsonObject();
//    }
//
//    public void get_scratch_cards(long page1,long limit1){
//
//        JsonObject otp = set_scatch_cards(page1,limit1);
//        showLog("API REQ   "+otp.toString());
//        APIService apiService = RestApiClient.getClient().create(APIService.class);
//        Call<EncreptionRequest> apiResponse = apiService.get_scratch_cards("v3",otp);
//
//        apiResponse.enqueue(new Callback<EncreptionRequest>() {
//            //@RequiresApi(api = Build.VERSION_CODES.O)
//            @Override
//            public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {
//
//                EncreptionRequest encreptionRequest=response.body();
//                showLog("API RES   "+response.body().getData());
//                String encrpt_data =  decrypt(encreptionRequest.getData());
//
//                Gson gsonObj = new Gson();
//                ScratchCardModel languageModel  = gsonObj.fromJson(encrpt_data, ScratchCardModel.class);
//
//                total_amount.setText("\u20B9 "+languageModel.getTotal_points());
//
//
//                //
//                if (languageModel.getSuccess()) {
//
//                    if(languageModel.getData().size()>0){
//
//                        page=languageModel.getSkip();
//                        limit=languageModel.getFirstCardId();
//
//                        showLog("AKKISETTY NARASIMHA "+scratchCardRespounses.size());
//
//                        scratchCardRespounses.addAll(languageModel.getData());
//
//                        showLog("AKKISETTY NARASIMHA "+scratchCardRespounses.size());
//
//                        long_scratchCardRespounses=scratchCardRespounses.size();
//
//                        gridLayoutManager = new GridLayoutManager(ScratchCardsList.this, 2, GridLayoutManager.VERTICAL, false);
//
//                        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
//                            @Override
//                            public int getSpanSize(int position) {
//                                // grid items to take 1 column
//                                if (scratchCardRespounses.size() % 2 == 0) {
//                                    return 1;
//                                } else {
//                                    return (position == scratchCardRespounses.size() - 1) ? 2 : 1;
//                                }
//                            }
//                        });
//                        scratcard_recyclerview.setLayoutManager(gridLayoutManager);
//
//                        scratchCardAdopter= new ScratchCardAdopter(activity,ScratchCardsList.this,scratchCardRespounses,REFER_LINK);
//                        scratcard_recyclerview.setAdapter(scratchCardAdopter);
//
//                        rewords_total_amount.setVisibility(View.VISIBLE);
//                        layout_with_data.setVisibility(View.VISIBLE);
//                        layout_with_out_data.setVisibility(View.GONE);
//                        scratcard_recyclerview.setVisibility(View.VISIBLE);
//                        txtNoData.setVisibility(View.GONE);
//
//                    }else {
//
//
//                        if( scratchCardRespounses.size()==0){
//
//
//                            layout_with_data.setVisibility(View.VISIBLE);
//                            layout_with_out_data.setVisibility(View.GONE);
//                            scratcard_recyclerview.setVisibility(View.GONE);
//                            txtNoData.setVisibility(View.VISIBLE);
//                            rewords_total_amount.setVisibility(View.GONE);
//
//                        }else if(scratchCardRespounses.size()==long_scratchCardRespounses){
//
//                            progress_bar.setVisibility(View.GONE);
//
//                        }
//
//                    }
//
//                }else {
//
//                    layout_with_data.setVisibility(View.GONE);
//                    layout_with_out_data.setVisibility(View.VISIBLE);
//                    progress_bar.setVisibility(View.GONE);
//
//                }
//
//            }
//
//            @Override
//            public void onFailure(Call<EncreptionRequest> call, Throwable t) {
//
//            }
//        });
//    }
//}