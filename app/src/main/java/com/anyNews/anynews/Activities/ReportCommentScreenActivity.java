package com.anyNews.anynews.Activities;

import static com.anyNews.anynews.Utils.AppHelper.showLog;
import static com.anyNews.anynews.Utils.MyUtils.decrypt;
import static com.anyNews.anynews.Utils.MyUtils.encrypt;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anyNews.anynews.Pojo.BaseResponse;
import com.anyNews.anynews.Pojo.EncreptionRequest;
import com.anyNews.anynews.Pojo.EncritonRequestData;
import com.anyNews.anynews.Pojo.ListOfCommentsResponse;
import com.anyNews.anynews.Pojo.ReportFlagsResponseData;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Utils.Allsharedpreference;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportCommentScreenActivity extends AppCompatActivity {

    RecyclerView reportsRecyclerView;
    ListOfCommentsResponse.Datum lDatum;
    Allsharedpreference allsharedpreference;
    LinearLayout layoutProgressbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_comment_screen);
        reportsRecyclerView = findViewById(R.id.reportsRecyclerView);
        layoutProgressbar = findViewById(R.id.layoutProgressbar);
        allsharedpreference = new Allsharedpreference(this);
        findViewById(R.id.img_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        lDatum = (ListOfCommentsResponse.Datum) getIntent().getSerializableExtra("data");
        APIService apiService = RestApiClient.getClient().create(APIService.class);
        Call<EncreptionRequest> requestCall = apiService.getListOfReportsProblems("v3");
        layoutProgressbar.setVisibility(View.VISIBLE);
        requestCall.enqueue(new Callback<EncreptionRequest>() {
            @Override
            public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {
                layoutProgressbar.setVisibility(View.GONE);
                EncreptionRequest encreptionRequest = response.body();
                String encrpt_data = null;
                encrpt_data = decrypt(encreptionRequest.getData());
                Gson gsonObj = new Gson();
                showLog("comments getListOfReportsProblems :" + encrpt_data);
                ReportFlagsResponseData[] reportFlags = gsonObj.fromJson(encrpt_data, ReportFlagsResponseData[].class);
                MyListAdapter adapter = new MyListAdapter(reportFlags);
                reportsRecyclerView.setHasFixedSize(true);
                reportsRecyclerView.setLayoutManager(new LinearLayoutManager(ReportCommentScreenActivity.this));
                reportsRecyclerView.setAdapter(adapter);


            }

            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {
                layoutProgressbar.setVisibility(View.GONE);
            }
        });

    }


    class MyListAdapter extends RecyclerView.Adapter<MyListAdapter.ViewHolder> {
        private ReportFlagsResponseData[] listdata;

        // RecyclerView recyclerView;
        public MyListAdapter(ReportFlagsResponseData[] listdata) {
            this.listdata = listdata;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View listItem = layoutInflater.inflate(R.layout.report_flag_list_item, parent, false);
            ViewHolder viewHolder = new ViewHolder(listItem);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.textViewReportFlagName.setText(listdata[position].getText());
            holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    postReportCommentFlag(listdata[position].getId());
                }
            });
        }


        @Override
        public int getItemCount() {
            return listdata.length;
        }

        public  class ViewHolder extends RecyclerView.ViewHolder {
            public TextView textViewReportFlagName;
            public RelativeLayout relativeLayout;

            public ViewHolder(View itemView) {
                super(itemView);
                this.textViewReportFlagName = (TextView) itemView.findViewById(R.id.textViewReportFlagName);
                relativeLayout = (RelativeLayout) itemView.findViewById(R.id.relativeLayoutTop);
            }
        }
    }

    private void postReportCommentFlag(String id) {
        layoutProgressbar.setVisibility(View.VISIBLE);
        showLog("postReportCommentFlag:"+id);
        APIService apiService = RestApiClient.getClient().create(APIService.class);
        JsonObject jsonObject=new JsonObject();
        jsonObject.addProperty("comment_id",lDatum.getId());
        jsonObject.addProperty("draft_id", SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME));
        jsonObject.addProperty("email",lDatum.getUserName());
        jsonObject.addProperty("blocked","0");
        jsonObject.addProperty("reported_email",allsharedpreference.getEmailId());
        jsonObject.addProperty("problem_id",id);
        showLog("REPORT FLAG REQUEST:"+jsonObject.toString());
        String encrpt_data = null;
        encrpt_data = encrypt(jsonObject.toString());
        EncritonRequestData encritonRequestData = new EncritonRequestData();
        encritonRequestData.setData(encrpt_data);
        Call<EncreptionRequest> postReportFlag = apiService.postReportFlag("v3",new Gson().toJsonTree(encritonRequestData).getAsJsonObject());
        postReportFlag.enqueue(new Callback<EncreptionRequest>() {
            @Override
            public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {
                layoutProgressbar.setVisibility(View.GONE);
                EncreptionRequest encreptionRequest = response.body();
                String encrpt_data = null;
                encrpt_data = decrypt(encreptionRequest.getData());
                showLog("REPORT FLAG REQUEST:"+encrpt_data);
                Gson gsonObj = new Gson();
                BaseResponse listOfCommentsResponse=gsonObj.fromJson(encrpt_data,BaseResponse.class);
                showLog("REPORT FLAG STATUS CODE:"+listOfCommentsResponse.getStatusCode());
                try {

                    if (listOfCommentsResponse!=null && listOfCommentsResponse.getStatusCode()==200) {
                        Toast.makeText(ReportCommentScreenActivity.this, "Reported Successfully", Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(ReportCommentScreenActivity.this, "Unable to process the request please try again later", Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                showLog("REPORT FLAG REQUEST:"+encrpt_data);


                finish();
            }

            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {
                Toast.makeText(ReportCommentScreenActivity.this, "Unable to process the request please try again later", Toast.LENGTH_SHORT).show();
                layoutProgressbar.setVisibility(View.GONE);
                finish();
            }
        });

    }
}
