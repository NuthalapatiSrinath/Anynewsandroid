package com.anyNews.anynews.Activities;

import static com.anyNews.anynews.Utils.AppHelper.showLog;
import static com.anyNews.anynews.Utils.MyUtils.decrypt;
import static com.anyNews.anynews.Utils.MyUtils.encrypt;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.anyNews.anynews.Adapters.DistricsAdopter;
import com.anyNews.anynews.Pojo.EncreptionRequest;
import com.anyNews.anynews.Pojo.EncritonRequestData;
import com.anyNews.anynews.Pojo.ListOfCommentsRequest;
import com.anyNews.anynews.Pojo.ListOfUserArticlesRequest;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.anyNews.anynews.models.DistricsModel;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TestingClass extends AppCompatActivity {

    Button decode,encode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing_class);
        decode=findViewById(R.id.decode);
        encode=findViewById(R.id.encode);

        decode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            showLog(" DECRYPT "+decrypt("N2UB1B0Te+trvTRsRxVEQrrKrdJHpD83EJGC5tGOoyrxzRTZ9STAXxSnbO12izNCZrGizbgmYdq5meAQJuZwj6V9zgG9CqtpkhAEjo/1YoZ6bbwfXTeYXuLPesndxHoTf9NiZ8TsZWQDbFoLujM+k4mIbqcAG2iA2dl7QcOAkV/2nMpWvH6xsb+/7CyWMSBZh78sj3ZWVoWf7l98qL/wd6mOEWCU9b5t+Xq/2AVeEnoyxL1H52va7aM9F2NQIdQ4btIZB3Dc4rXNeGyxpcrm3Ce+S9laqC/rWnzVlz8X7njSpFDprMtNjvpWWMIWBpMo3r/FPUF2jNoPN+ClielssvPJZXQqh6aviVFOWjRnNdOWk9dHinz0NKwCbcDUiGaL0jj28+bgu8YrxzsD5TQB7GVFmJd3040za8thu2prW0u5Hq3t8ob0Vqr03TKyWmZ+s0bXcAEbwR9otEfiSWCLkn6TxAGDiqrXD8wCqOo1zVK3Il6fxMMtHYKaAnI5A4wbX3BKhPvIWfKP8lTqch1wIXHZ9zLXvXCqsA6KWGku23hSrSujdrzsoSopnxOCsF9IqSxeynELErIl4PpcX3o2LIvnzfMa0Y3e/iDJbRAs80ySV1lVA2gYDNlJWQ6hixVdrL9Vr6eLQYrpJwbQx2pZ39BcpeRTle8/vd2mAP0JUulvwT9Rz0x7wabS0w2PMud93VVwfC7eJPKbhhDOSOevGSGrADaa4zmIUAa3T4wwFR/wwjdLTAfALx1myF+SpSHnYCDvcCGA4zttYrTCkGHYPd6SJ01kFiQ8HYnDmYM61WE68tUC4spgGwQn0adq7NwRilS//eaGhE6MD0oQjlBihUeUgff7GWKkg1AlQabOZHTGidxX4gSN2xdrp4suSaaJ8/RQjUt4mOL6y6on3fU/0YVFaHFRYGi4rXL4T2T4aVg="));    decrypt("N2UB1B0Te+trvTRsRxVEQrHOZLXm0CPNdQKaPETF1gC6ziIXDDRf9IhOPj8YXztE");
            showLog(" DECRYPT "+decrypt("2CObLSrueHWuIu/u0W/J9TTOWpRIYAiQpq+PnA9uCKnnAlZHJVHC57XstF+ehcJy"));    decrypt("N2UB1B0Te+trvTRsRxVEQrHOZLXm0CPNdQKaPETF1gC6ziIXDDRf9IhOPj8YXztE");
            }
        });
        encode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               showLog(" ENCRYPT "+encrypt("2CObLSrueHWuIu/u0W/J9TTOWpRIYAiQpq+PnA9uCKnnAlZHJVHC57XstF+ehcJy")); encrypt("2CObLSrueHWuIu/u0W/J9TTOWpRIYAiQpq+PnA9uCKnnAlZHJVHC57XstF+ehcJy");
            }
        });
        getDisrics();

    }

    public void getDisrics() {

        JsonObject otp = write_comment_data();
        showLog("VIEWS " + otp.toString());

    }

    private JsonObject write_comment_data() {

        //SessionID
       // ApiKey

        ListOfUserArticlesRequest requestModel = new ListOfUserArticlesRequest();
        requestModel.setSessionID("0");
        requestModel.setApiKey("122");
        requestModel.setDraftId(1);
        requestModel.setPostId((long)1);

        String jsonString = new com.google.gson.Gson().toJson(requestModel);
        String encrpt_data = null;
        showLog(" VIEWS !"+jsonString.toString());
        encrpt_data = encrypt(jsonString);


        EncritonRequestData Encriton_Request = new EncritonRequestData();
        Encriton_Request.setData(encrpt_data);
        return new Gson().toJsonTree(Encriton_Request).getAsJsonObject();
    }

}