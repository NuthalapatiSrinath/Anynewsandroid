package com.anyNews.anynews.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anyNews.anynews.Adapters.PostsStatusAdopter;
import com.anyNews.anynews.Pojo.EncreptionRequest;
import com.anyNews.anynews.Pojo.EncritonRequestData;
import com.anyNews.anynews.Pojo.PostNewsRequest;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.anyNews.anynews.models.PostsModel;
import com.anyNews.anynews.Ui.NeomorphFrameLayout;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.anyNews.anynews.Utils.AppHelper.showLog;
import static com.anyNews.anynews.Utils.MyUtils.decrypt;
import static com.anyNews.anynews.Utils.MyUtils.encrypt;

public class PostArticleStatus extends AppCompatActivity {

    RecyclerView post_status;
    LinearLayoutManager linearLayoutManager;
    ArrayList<PostsModel.Datum>list;
    PostsStatusAdopter postsStatusAdopter;
    ImageView go_bck;
    NeomorphFrameLayout neomorphFrameLayout_approved_un_selected,neomorphFrameLayout_approved_selected,
            neomorphFrameLayout_pending_un_selected,neomorphFrameLayout_pending_selected,
            neomorphFrameLayout_rejected_un_selected,neomorphFrameLayout_rejected_selected;

    private Activity Activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posts_status);

        post_status=findViewById(R.id.post_status);

        neomorphFrameLayout_approved_selected=findViewById(R.id.neomorphFrameLayout_approved_selected);
        neomorphFrameLayout_approved_un_selected=findViewById(R.id.neomorphFrameLayout_approved_un_selected);
        neomorphFrameLayout_pending_un_selected=findViewById(R.id.neomorphFrameLayout_pending_un_selected);
        neomorphFrameLayout_pending_selected=findViewById(R.id.neomorphFrameLayout_pending_selected);
        neomorphFrameLayout_rejected_selected=findViewById(R.id.neomorphFrameLayout_rejected_selected);
        neomorphFrameLayout_rejected_un_selected=findViewById(R.id.neomorphFrameLayout_rejected_un_selected);


        go_bck=findViewById(R.id.go_bck);
        go_bck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent=new Intent(PostArticleStatus.this, newProfileDetails.class);
                startActivity(intent);
            }
        });

        neomorphFrameLayout_approved_un_selected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                neomorphFrameLayout_approved_selected.setVisibility(View.VISIBLE);
                neomorphFrameLayout_approved_un_selected.setVisibility(View.GONE);
                neomorphFrameLayout_rejected_selected.setVisibility(View.GONE);
                neomorphFrameLayout_rejected_un_selected.setVisibility(View.VISIBLE);
                neomorphFrameLayout_pending_selected.setVisibility(View.GONE);
                neomorphFrameLayout_pending_un_selected.setVisibility(View.VISIBLE);

                getDisrics(1);

            }
        });

        neomorphFrameLayout_pending_un_selected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                neomorphFrameLayout_approved_selected.setVisibility(View.GONE);
                neomorphFrameLayout_approved_un_selected.setVisibility(View.VISIBLE);
                neomorphFrameLayout_rejected_selected.setVisibility(View.GONE);
                neomorphFrameLayout_rejected_un_selected.setVisibility(View.VISIBLE);
                neomorphFrameLayout_pending_selected.setVisibility(View.VISIBLE);
                neomorphFrameLayout_pending_un_selected.setVisibility(View.GONE);

                getDisrics(0);
            }
        });

        neomorphFrameLayout_rejected_un_selected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                neomorphFrameLayout_approved_selected.setVisibility(View.GONE);
                neomorphFrameLayout_approved_un_selected.setVisibility(View.VISIBLE);
                neomorphFrameLayout_rejected_selected.setVisibility(View.VISIBLE);
                neomorphFrameLayout_rejected_un_selected.setVisibility(View.GONE);
                neomorphFrameLayout_pending_selected.setVisibility(View.GONE);
                neomorphFrameLayout_pending_un_selected.setVisibility(View.VISIBLE);

                getDisrics(2);
            }
        });


        Toast.makeText(this, "AKKISETTY NARASIMHA", Toast.LENGTH_SHORT).show();
        neomorphFrameLayout_approved_selected.setVisibility(View.VISIBLE);
        neomorphFrameLayout_approved_un_selected.setVisibility(View.GONE);
        neomorphFrameLayout_rejected_selected.setVisibility(View.GONE);
        neomorphFrameLayout_rejected_un_selected.setVisibility(View.VISIBLE);
        neomorphFrameLayout_pending_selected.setVisibility(View.GONE);
        neomorphFrameLayout_pending_un_selected.setVisibility(View.VISIBLE);

        getDisrics(1);

    }

    private JsonObject set_districs(int status) {

        PostNewsRequest requestModel = new PostNewsRequest();

        requestModel.setDraftId(SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME));
        requestModel.setMobileNumber(SharedPrefsData.getString(getApplicationContext(), Constants.Selected_Phone_Number, Constants.PREF_NAME));
        requestModel.setPostStatus(status);
        requestModel.setFirstPostId((long)0);
        requestModel.setSkip(0);

        String jsonString = new com.google.gson.Gson().toJson(requestModel);
        String encrpt_data = null;
        Toast.makeText(this, "AKKISETTY", Toast.LENGTH_SHORT).show();
        encrpt_data = encrypt(jsonString);
        showLog("API REQUEST E" + jsonString.toString());
        EncritonRequestData Encriton_Request=new EncritonRequestData();
        Encriton_Request.setData(encrpt_data);
        return new Gson().toJsonTree(Encriton_Request).getAsJsonObject();
    }
    public void getDisrics(int status){

        JsonObject otp = set_districs(status);
        showLog("API REQ   "+otp.toString());
        APIService apiService = RestApiClient.getClient().create(APIService.class);
        Call<EncreptionRequest> apiResponse = apiService.get_user_posts("v3",otp);

        apiResponse.enqueue(new Callback<EncreptionRequest>() {
            //@RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {

                list=new ArrayList<>();

                EncreptionRequest encreptionRequest=response.body();
                showLog("API RES   "+response.body().getData());
                String encrpt_data =  decrypt(encreptionRequest.getData());


                Gson gsonObj = new Gson();
                PostsModel postsModel  = gsonObj.fromJson(encrpt_data, PostsModel.class);

                if(postsModel.getSuccess()){

                    list.addAll(postsModel.getData());
                    linearLayoutManager=new LinearLayoutManager(PostArticleStatus.this);
                    post_status.setLayoutManager(linearLayoutManager);
                    postsStatusAdopter = new PostsStatusAdopter(Activity, PostArticleStatus.this, list,status);
                    post_status.setAdapter(postsStatusAdopter);

                }

            }

            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {

            }
        });
    }

}