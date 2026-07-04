package com.anyNews.anynews;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import com.anyNews.anynews.Pojo.EncreptionRequest;
import com.anyNews.anynews.Pojo.LanguageSelectionRequest;
import com.anyNews.anynews.Pojo.TestingResponse;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.anyNews.anynews.Utils.AppHelper.showLog;
import static com.anyNews.anynews.Utils.MyUtils.decrypt;
import static com.anyNews.anynews.Utils.MyUtils.encrypt;


public class CryptoUtil extends AppCompatActivity {
    //private static final Logger L = LoggerFactory.getLogger(CryptoUtil.class);

    private static final String ALGORITHM = "AES/ECB/PKCS5Padding";
    private static final byte[] KEY_BYTES = {

            65, 78, 50, 107, 64, 82, 83, 71, 66, 68, 66, 75, 78, 89, 83, 83
    };

             //   0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15

    //@RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_crop);


      /*  String encrpt_data =  encrypt("hello");
        showLog("ENCRYPT"+encrpt_data);
        try {
            showLog("compress"+compress(encrpt_data));
            showLog("decompress"+decompress(compress(encrpt_data)));

        } catch (IOException e) {
            e.printStackTrace();
        }-
        String decrypt_data=decrypt(encrpt_data);
        showLog("DECRYPT"+decrypt_data);*/

        sendDeviceDetails();

        //        try {
//            showLog("decompress"+decompress());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }



    //@RequiresApi(api = Build.VERSION_CODES.O)
    private JsonObject ReadOTP() {
/*
        EncritonRequestData data_va=new EncritonRequestData();
        data_va.setDraftId("14");
        String jsonString = new com.google.gson.Gson().toJson(data_va);
        String encrpt_data =  encrypt(jsonString);
        showLog("ENCRYPT"+encrpt_data
*/
        LanguageSelectionRequest requestModel = new LanguageSelectionRequest();
        requestModel.setData("");
        return new Gson().toJsonTree(requestModel).getAsJsonObject();
    }

    //@RequiresApi(api = Build.VERSION_CODES.O)
    private void sendDeviceDetails() {


        JsonObject otp = ReadOTP();

        showLog("API REQ   "+otp.toString());
        APIService apiService = RestApiClient.getClient().create(APIService.class);
        Call<EncreptionRequest> apiResponse = apiService.enc_dec("v3",otp);

        apiResponse.enqueue(new Callback<EncreptionRequest>() {

            @Override
            public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {

                EncreptionRequest encreptionRequest=response.body();
                showLog("API RES   "+response.body().getData());
                String encrpt_data =  decrypt(encreptionRequest.getData());

                Gson gsonObj = new Gson();
                TestingResponse splash = gsonObj.fromJson(encrpt_data,  TestingResponse.class);

                showLog("SPLASH_SCRREN "+splash.getSuccess());
            }

            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {

              Toast.makeText(CryptoUtil.this, "DDD  "+call, Toast.LENGTH_SHORT).show();

            }
        });
    }



//try {
//        byte[] compressed = compress(text);
//        for (byte character : compressed) {
//            Logone.d("test", String.valueOf(character));
//        }
//        String decompressed = decompress(compressed);
//        Logone.d("test", decompressed);
//    } catch (IOException e) {
//        e.printStackTrace();
//    }

}