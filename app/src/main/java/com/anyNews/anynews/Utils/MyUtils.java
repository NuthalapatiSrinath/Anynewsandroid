package com.anyNews.anynews.Utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.RequiresApi;


import com.anyNews.anynews.CryptoUtil;
import com.anyNews.anynews.Pojo.EncreptionRequest;
import com.anyNews.anynews.Pojo.EncritonRequestData;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonObject;



import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import android.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyUtils {

    // private static final Logger L = LoggerFactory.getLogger(CryptoUtil.class);
    String encrpt_data;
    private static final String ALGORITHM = "AES/ECB/PKCS5Padding";
    private static final byte[] KEY_BYTES = {
            65, 78, 50, 107, 64, 82, 83, 71, 66, 68, 66, 75, 78, 89, 83, 83
    };

    public static boolean isNetworkAvailable(Context context) {
        boolean netstate = false;
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {

            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {

                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {

                        netstate = true;
                        break;
                    }
                }
            }
        }
        return netstate;
    }



    public static String encrypt(String  str) {
        try {
            // str(utf8) -> bytes -> encrypt -> bytes -> base64(ascii)
            return new String(Base64.encode(encrypt(str.getBytes(StandardCharsets.UTF_8)),Base64.NO_WRAP), StandardCharsets.ISO_8859_1);

        } catch (Exception e) {
            //UnsupportedEncodingException...
//            if (L.isWarnEnabled()) {
//                L.warn("encrypt error:", e);
//            }
            return str;
        }
    }

    public static byte[] encrypt(byte[] data) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, getEncryptionKey());
            byte[] encrypted = cipher.doFinal(data);
            return encrypted;
        } catch (Exception e) {
            // GeneralSecurityException
//            if (L.isWarnEnabled()) {
//                L.warn("encrypt error:", e);
//            }
            return data;
        }
    }


    public static String decrypt(String str) {
        try {
            // base64(ascii) -> bytes --> decrypt -> bytes -> str(utf8)
            return new String(decrypt(Base64.decode(str.getBytes(StandardCharsets.ISO_8859_1),Base64.NO_WRAP)), StandardCharsets.UTF_8);
        } catch (Exception e) {
            //UnsupportedEncodingException...
//            if (L.isWarnEnabled()) {
//                L.warn("decrypt error:", e);
//            }
            return str;
        }
    }

    public static byte[] decrypt(byte[] data) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, getEncryptionKey());
            return cipher.doFinal(data);
        } catch (Exception e) {
            // GeneralSecurityException
//            if (L.isWarnEnabled()) {
//                L.warn("decrypt error:", e);
//            }
            return data;
        }
    }

    private static Key getEncryptionKey() {
        try {
            return new SecretKeySpec(MessageDigest.getInstance("MD5").digest(KEY_BYTES), "AES");
        } catch (NoSuchAlgorithmException e) {
            throw new Error("failed to get encryption key", e);
        }
    }

    public static byte[] compress(String string) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream(string.length());
        GZIPOutputStream gos = new GZIPOutputStream(os);
        gos.write(string.getBytes());
        gos.close();
        byte[] compressed = os.toByteArray();
        os.close();
        return compressed;
    }

    public static String decompress(byte[] compressed) throws IOException {
        final int BUFFER_SIZE = 32;
        ByteArrayInputStream is = new ByteArrayInputStream(compressed);
        GZIPInputStream gis = new GZIPInputStream(is, BUFFER_SIZE);
        StringBuilder string = new StringBuilder();
        byte[] data = new byte[BUFFER_SIZE];
        int bytesRead;
        while ((bytesRead = gis.read(data)) != -1) {
            string.append(new String(data, 0, bytesRead));
        }
        gis.close();
        is.close();
        return string.toString();
    }

    public static void showSnackbar(View view, String message){
        Snackbar snackbar = Snackbar.make(view,message,Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    public static void showSnackbar(View view, String message, String actionTitle,View.OnClickListener mClickListener){
        Snackbar snackbar = Snackbar.make(view,message,Snackbar.LENGTH_LONG);
        if(mClickListener!=null)
            snackbar.setAction(actionTitle,mClickListener);
        snackbar.show();
    }


  /*  @RequiresApi(api = Build.VERSION_CODES.O)
    private String sendDeviceDetails() {

        JsonObject otp = ReadOTP();

        APIService apiService = RestApiClient.getClient().create(APIService.class);
        Call<EncreptionRequest> apiResponse = apiService.enc_dec("v2",otp);

        apiResponse.enqueue(new Callback<EncreptionRequest>() {

            @Override
            public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {

                EncreptionRequest encreptionRequest=response.body();
                encrpt_data =  decrypt(encreptionRequest.getData());
                showLog("ENCRYPT"+encrpt_data);

            }

            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {


            }
        });
        return encrpt_data;
    }*/
}
