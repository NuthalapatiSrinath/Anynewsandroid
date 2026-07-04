package com.anyNews.anynews.Activities;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.anyNews.anynews.Adapters.PaginationListener;
import com.anyNews.anynews.Adapters.PostRecyclerAdapter;
import com.anyNews.anynews.Pojo.EncreptionRequest;
import com.anyNews.anynews.Pojo.EncritonRequestData;
import com.anyNews.anynews.Pojo.ScratchCardListRequiest;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.anyNews.anynews.models.PaginationModel;
import com.anyNews.anynews.models.PostItem;
import com.anyNews.anynews.models.ScratchCardModel;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.anyNews.anynews.Adapters.PaginationListener.PAGE_START;
import static com.anyNews.anynews.Utils.AppHelper.showLog;
import static com.anyNews.anynews.Utils.MyUtils.decrypt;
import static com.anyNews.anynews.Utils.MyUtils.encrypt;


public class Pagination extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    NestedScrollView nestedScrollView;
    RecyclerView recyclerView;
    ProgressBar progressBar;
    GridLayoutManager gridLayoutManager;
    ArrayList<ScratchCardModel.Datum> paginationModels;
    private PostRecyclerAdapter adapter;
    long page=0,limit=0;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pagination);
        ButterKnife.bind(this);

        nestedScrollView = findViewById(R.id.scroll_view);
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progress_bar);

        gridLayoutManager =new GridLayoutManager(Pagination.this,2);
        recyclerView.setLayoutManager(gridLayoutManager);
        paginationModels=new ArrayList<>();

        get_scratch_cards(page,limit);



       nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
           @Override
           public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {

               if(scrollY==v.getChildAt(0).getMeasuredHeight()-v.getMeasuredHeight()){


                   progressBar.setVisibility(View.VISIBLE);

                   get_scratch_cards(page,limit);
               }
           }
       });
    }
    private JsonObject set_scatch_cards(long page,long limit) {

        ScratchCardListRequiest requestModel = new ScratchCardListRequiest();

        requestModel.setDraftId(49221);
        requestModel.setUserId((long)763);
        requestModel.setSkip((long)page);
        requestModel.setFirstCardId((long)limit);

        String jsonString = new com.google.gson.Gson().toJson(requestModel);
        String encrpt_data = null;
        /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            encrpt_data = encrypt(jsonString);
        }*/
        encrpt_data = encrypt(jsonString);

        EncritonRequestData Encriton_Request=new EncritonRequestData();
        Encriton_Request.setData(encrpt_data);
        return new Gson().toJsonTree(Encriton_Request).getAsJsonObject();

    }

    public void get_scratch_cards(long page1,long limit1){

        JsonObject otp = set_scatch_cards(page1,limit1);
        showLog("API REQ   "+otp.toString());
        APIService apiService = RestApiClient.getClient().create(APIService.class);
        Call<EncreptionRequest> apiResponse = apiService.get_scratch_cards("v3",otp);

        apiResponse.enqueue(new Callback<EncreptionRequest>() {
            //@RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {


                progressBar.setVisibility(View.VISIBLE);

                EncreptionRequest encreptionRequest=response.body();
                showLog("RES"+response.body().getData());
                showLog("API REQUEST R"+decrypt(response.body().getData()));
                String encrpt_data =  decrypt(encreptionRequest.getData());

                Gson gsonObj = new Gson();
                ScratchCardModel languageModel  = gsonObj.fromJson(encrpt_data, ScratchCardModel.class);



                if (languageModel.getSuccess()) {

                    page=languageModel.getSkip();
                    limit=languageModel.getFirstCardId();

                    Toast.makeText(Pagination.this, " "+languageModel.getData().size(), Toast.LENGTH_SHORT).show();
                    if(languageModel.getData().size()>0){
                      //  progressBar.setVisibility(View.GONE);

                        paginationModels.addAll(languageModel.getData());


                        gridLayoutManager = new GridLayoutManager(Pagination.this, 2, GridLayoutManager.VERTICAL, false);

                        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                            @Override
                            public int getSpanSize(int position) {

                                // grid items to take 1 column
                                if (paginationModels.size() % 2 == 0) {
                                    return 1;
                                } else {
                                    return (position == paginationModels.size() - 1) ? 2 : 1;
                                }
                            }
                        });


                        adapter = new PostRecyclerAdapter(Pagination.this, paginationModels);
                        recyclerView.setAdapter(adapter);


                    }else {


                    }

                }else {


                }

            }

            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {

            }
        });
    }
}
