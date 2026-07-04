package com.anyNews.anynews.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.anyNews.anynews.Pojo.DailogSendRespouse;
import com.anyNews.anynews.Pojo.DialogSendRequest;
import com.anyNews.anynews.Pojo.DilogeResest;
import com.anyNews.anynews.Pojo.GetReportIssueResponce;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;

public class Report_Demo_Activity extends AppCompatActivity {
    Button clickButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report__demo_);

        clickButton = (Button) findViewById(R.id.button);
        clickButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ReportDialog();
            }
        });

    }

    private void ReportDialog() {


        AlertDialog.Builder builder = new AlertDialog.Builder(Report_Demo_Activity.this);

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.custom_dialog_layout, null);

        builder.setView(dialogView);

        RadioButton one = (RadioButton) dialogView.findViewById(R.id.one);
        RadioButton two = (RadioButton) dialogView.findViewById(R.id.two);
        RadioButton three = (RadioButton) dialogView.findViewById(R.id.three);
        RadioButton four = (RadioButton) dialogView.findViewById(R.id.four);

        TextView report=dialogView.findViewById(R.id.report);
        TextView title=dialogView.findViewById(R.id.title);
        EditText full_image_edt_news_headlines=dialogView.findViewById(R.id.full_image_edt_news_headlines);
        Button yes=dialogView.findViewById(R.id.yes);
        ImageView close_dailog=dialogView.findViewById(R.id.close_dailog);



        // Create the alert dialog
        final AlertDialog dialog = builder.create();

        JsonObject updateparams = ReportParams();

        close_dailog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        APIService apiService = RestApiClient.getClient().create(APIService.class);
        Call<GetReportIssueResponce> update = apiService.reportissue(updateparams);
        update.enqueue(new Callback<GetReportIssueResponce>() {

            @Override
            public void onResponse(Call<GetReportIssueResponce> call, retrofit2.Response<GetReportIssueResponce> response) {

                GetReportIssueResponce updateResponce = response.body();

                one.setText("" + updateResponce.getData().getOptions().get(0));
                two.setText("" + updateResponce.getData().getOptions().get(1));
                three.setText("" + updateResponce.getData().getOptions().get(2));
                four.setText("" + updateResponce.getData().getOptions().get(3));
                report.setText(""+updateResponce.getData().getTitle());
                title.setText(""+updateResponce.getData().getOtherReason());
                full_image_edt_news_headlines.setHint(""+updateResponce.getData().getPlaceholder());

                  yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {


                        JsonObject updateparams = DailogRequest();


                        APIService apiService = RestApiClient.getClient().create(APIService.class);
                        Call<DailogSendRespouse> update = apiService.dailogSend(updateparams);


                        update.enqueue(new Callback<DailogSendRespouse>() {

                            @Override
                            public void onResponse(Call<DailogSendRespouse> call, retrofit2.Response<DailogSendRespouse> response) {

                                DailogSendRespouse updateResponce = response.body();


                                if(updateResponce.getSuccess()){

                                    Toast.makeText(Report_Demo_Activity.this, ""+updateResponce.getSuccess(), Toast.LENGTH_SHORT).show();

                                }else {
                                    Toast.makeText(Report_Demo_Activity.this, ""+updateResponce.getSuccess(), Toast.LENGTH_SHORT).show();


                                }
                                dialog.dismiss();
                            }

                            @Override
                            public void onFailure(Call<DailogSendRespouse> call, Throwable t) {

                                Toast.makeText(Report_Demo_Activity.this, "" + call, Toast.LENGTH_SHORT).show();

                            }
                        });
                    }
                });



            }

            @Override
            public void onFailure(Call<GetReportIssueResponce> call, Throwable t) {

                Toast.makeText(Report_Demo_Activity.this, "" + call, Toast.LENGTH_SHORT).show();

            }
        });


        dialog.show();


    }


    private void ReportDetails() {


    }

    private JsonObject ReportParams() {

        DilogeResest dilogeResest = new DilogeResest();
        dilogeResest.setLanguageId(1);

        return new Gson().toJsonTree(dilogeResest).getAsJsonObject();
    }
    private JsonObject DailogRequest() {

        DialogSendRequest dilogeResest = new DialogSendRequest();
        dilogeResest.setDraftId(34);
        dilogeResest.setLanguageId(1);
        dilogeResest.setOption("");
        dilogeResest.setPostId((long)123);
        dilogeResest.setReason("  SSSSSSSSSSSSS");


        return new Gson().toJsonTree(dilogeResest).getAsJsonObject();
    }
}