package com.anyNews.anynews.Services;

import static com.anyNews.anynews.Utils.AppHelper.showLog;

import com.anyNews.anynews.BuildConfig;
import com.google.gson.Gson;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

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
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .connectTimeout(100, TimeUnit.SECONDS)
                .addNetworkInterceptor(interceptor)
                .readTimeout(100, TimeUnit.SECONDS);

        // Debug-only: allow API calls from emulator when host SSL inspection / missing CA breaks handshake.
        if (BuildConfig.DEBUG) {
            try {
                final X509TrustManager trustAll = new X509TrustManager() {
                    @Override public void checkClientTrusted(X509Certificate[] chain, String authType) {}
                    @Override public void checkServerTrusted(X509Certificate[] chain, String authType) {}
                    @Override public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                };
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, new TrustManager[]{trustAll}, new SecureRandom());
                clientBuilder.sslSocketFactory(sslContext.getSocketFactory(), trustAll);
                clientBuilder.hostnameVerifier(new HostnameVerifier() {
                    @Override public boolean verify(String hostname, SSLSession session) { return true; }
                });
            } catch (Exception ignored) {}
        }

        OkHttpClient client = clientBuilder.build();
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
