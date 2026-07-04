package com.anyNews.anynews.Activities;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.anyNews.anynews.Adapters.DistricsAdopter;
import com.anyNews.anynews.Pojo.EncreptionRequest;
import com.anyNews.anynews.Pojo.EncritonRequestData;
import com.anyNews.anynews.Pojo.LanguageModelResponse;
import com.anyNews.anynews.Pojo.LanguageSelectionRequest;
import com.anyNews.anynews.Pojo.RedeemPageRequest;
import com.anyNews.anynews.Pojo.RedeemWalletRespouse;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Utils.Allsharedpreference;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.anyNews.anynews.models.DistricsModel;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.anyNews.anynews.Utils.AppHelper.showLog;
import static com.anyNews.anynews.Utils.MyUtils.decrypt;
import static com.anyNews.anynews.Utils.MyUtils.encrypt;
import static java.util.Objects.isNull;

public class RedeemPageActivity extends AppCompatActivity {

    TextView total_amount;
    ImageView back_button;
    EditText enter_paytm_number, enter_pan_card_number, enter_amount;
    LinearLayout redeem_send_button, redeem_send_progress, pan_card_status;

    public String pan_pattern = "(([A-Za-z]{5})([0-9]{4})([a-zA-Z]))";
    String panCardNo = "";
    String REDEEM_UP_TO_VALUE, REDEEM_VALUE;
    ProgressBar progressBarLarge;
    TextView avilable_amount;
    LanguageModelResponse languageModelResponse;

    Allsharedpreference prefShare;
    TextView username, title_pan_card, payment_charges_detailes, redeem_text;
    long enter_amount1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_redeem_page);
        back_button = findViewById(R.id.back_button);
        total_amount = findViewById(R.id.total_amount);
        enter_paytm_number = findViewById(R.id.enter_paytm_number);
        enter_pan_card_number = findViewById(R.id.enter_pan_card_number);
        enter_amount = findViewById(R.id.payment_enter_amount);
        redeem_text = findViewById(R.id.redeem_text);

        payment_charges_detailes = findViewById(R.id.payment_charges_detailes);
        redeem_send_button = findViewById(R.id.redeem_send_button);
        redeem_send_progress = findViewById(R.id.redeem_send_progress);
        progressBarLarge = findViewById(R.id.progressBarLarge);
        avilable_amount = findViewById(R.id.avilable_amount);
        pan_card_status = findViewById(R.id.pan_card_status);
        username = findViewById(R.id.username);
        title_pan_card = findViewById(R.id.title_pan_card);

        REDEEM_VALUE = SharedPrefsData.getString(getApplicationContext(), Constants.AVAILABLEPOINTS, Constants.PREF_NAME);
        REDEEM_UP_TO_VALUE = SharedPrefsData.getString(getApplicationContext(), Constants.REDEEMPOINTS, Constants.PREF_NAME);

        Boolean PAN_CARD_STATUS = SharedPrefsData.getBool(getApplicationContext(), Constants.PAN_CARD_STATUS, Constants.PREF_NAME);
        if (PAN_CARD_STATUS) {
            pan_card_status.setVisibility(View.VISIBLE);
            enter_pan_card_number.setText("");
        } else {
            pan_card_status.setVisibility(View.GONE);
            enter_pan_card_number.setText("AAAAA0000A");
        }

        total_amount.setText("" + REDEEM_VALUE);
        enter_paytm_number.setText("" + SharedPrefsData.getString(getApplicationContext(), Constants.Selected_Phone_Number, Constants.PREF_NAME));

        redeem_send_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (enter_paytm_number.getText().toString().length() >= 10) {

                    panCardNo = enter_pan_card_number.getText().toString();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        if (isNull(panCardNo) || !panCardNo.matches(pan_pattern) || panCardNo.length() < 10) {
                            enter_pan_card_number.setError("Please enter valid Pancard No");
                        //    Toast.makeText(RedeemPageActivity.this, ""languageModelResponse.getData().getple(), Toast.LENGTH_SHORT).show();
                            enter_pan_card_number.requestFocus();
                            return;
                        } else {

                            if (enter_amount.getText().toString().equals("") ||
                                    enter_amount.getText().toString().equals(null) ||
                                    enter_amount.getText().toString().equals("null")) {

                                enter_amount1 = 0;

                            } else {
                                enter_amount1 = Long.parseLong(enter_amount.getText().toString());
                            }


                            long enter_amount_value = enter_amount1;
                            long REDEEM_UP_TO_VALUE_DATA = Long.parseLong(REDEEM_UP_TO_VALUE);

                            long total_amount_value = Long.parseLong(REDEEM_VALUE);


                            if (enter_amount_value >= REDEEM_UP_TO_VALUE_DATA) {


                                if (total_amount_value >= enter_amount_value) {
                                    redeem_send_progress.setVisibility(View.VISIBLE);
                                    redeem_send_button.setVisibility(View.GONE);

                                    if(enter_pan_card_number.getText().toString().equals("AAAAA0000A")){
                                        enter_pan_card_number.setText("");
                                        getRedeemActivity();
                                    }else {
                                        getRedeemActivity();
                                    }

                                } else {

                                    Toast.makeText(RedeemPageActivity.this, "Please Check the amount before you will withdrawal ", Toast.LENGTH_SHORT).show();

                                }


                            } else {
                                 //  Toast.makeText(RedeemPageActivity.this, ""+languageModelResponse.getData().getple(), Toast.LENGTH_SHORT).show();

                                Toast.makeText(RedeemPageActivity.this, "Minimum withdrawal amount is " + REDEEM_UP_TO_VALUE_DATA, Toast.LENGTH_SHORT).show();
                            }

                        }
                    }


                } else {
                    Toast.makeText(RedeemPageActivity.this, " Enter Paytm Number ", Toast.LENGTH_SHORT).show();
                    // Toast.makeText(RedeemPageActivity.this, ""+languageModelResponse.getData().getpa(), Toast.LENGTH_SHORT).show();

                }

            }
        });

        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        setStaticdata();

    }


    private JsonObject set_districs() {

        long enter_amount_for_user = Long.parseLong(enter_amount.getText().toString());
        RedeemPageRequest requestModel = new RedeemPageRequest();

        requestModel.setUserId(SharedPrefsData.getLong(RedeemPageActivity.this, Constants.USER_ID, Constants.PREF_NAME));
        requestModel.setDraftId(SharedPrefsData.getInt(RedeemPageActivity.this, Constants.DraftID, Constants.PREF_NAME));
        requestModel.setPaymentType("wallet");
        requestModel.setTransferTo("" + enter_paytm_number.getText().toString());
        requestModel.setPanNumber("" + enter_pan_card_number.getText().toString());
        requestModel.setWithdrawAmount(enter_amount_for_user);
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

    public void getRedeemActivity() {


        JsonObject otp = set_districs();

        APIService apiService = RestApiClient.getClient().create(APIService.class);
        Call<EncreptionRequest> apiResponse = apiService.redeem_wallet("v3", otp);

        apiResponse.enqueue(new Callback<EncreptionRequest>() {
            //@RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {


                EncreptionRequest encreptionRequest = response.body();
                String encrpt_data = decrypt(encreptionRequest.getData());

                Gson gsonObj = new Gson();
                RedeemWalletRespouse languageModel = gsonObj.fromJson(encrpt_data, RedeemWalletRespouse.class);


                Toast.makeText(RedeemPageActivity.this, "" + languageModel.getMsg(), Toast.LENGTH_SHORT).show();
                redeem_send_progress.setVisibility(View.GONE);
                redeem_send_button.setVisibility(View.VISIBLE);
                if (languageModel.getSuccess()) {

                    Intent intent = new Intent(RedeemPageActivity.this, RedeemSuccessful.class);
                    startActivity(intent);
                }

            }

            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {

            }
        });
    }

    private void setStaticdata() {
        prefShare = new Allsharedpreference(RedeemPageActivity.this);
        Gson gson = new Gson();
        String Translation = prefShare.getShared_String(Allsharedpreference.Translator);
        languageModelResponse = gson.fromJson(Translation, LanguageModelResponse.class);
        avilable_amount.setText(languageModelResponse.getData().getAvailableBalance());
        username.setText("" + languageModelResponse.getData().getPayment());
        title_pan_card.setText("" + languageModelResponse.getData().getPanCard());
        enter_pan_card_number.setHint("" + languageModelResponse.getData().getEnterPan());
        enter_amount.setHint("" + languageModelResponse.getData().getEnterAmount());
        payment_charges_detailes.setText("" + languageModelResponse.getData().getPaymentNote());
        redeem_text.setText("" + languageModelResponse.getData().getRedeem());

    }
}