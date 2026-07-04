package com.anyNews.anynews.Services;

import static com.anyNews.anynews.Utils.AppHelper.showLog;

import android.os.Build;

import com.anyNews.anynews.BuildConfig;
import com.google.gson.Gson;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RestApiClient
{
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        showLog("CHECK POINT GETCLIENT-1 ::");
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        if(BuildConfig.DEBUG){
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        }
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(100, TimeUnit.SECONDS)
                .addNetworkInterceptor(interceptor)
                .readTimeout(100, TimeUnit.SECONDS).build();
        showLog("CHECK POINT GETCLIENT-2 ::");
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(CommonUtilities.URL).client(client)
                    .addConverterFactory(GsonConverterFactory.create(new Gson()))
                    .build();
        }
        showLog("CHECK POINT GETCLIENT-3 ::");
        return retrofit;
    }
}
