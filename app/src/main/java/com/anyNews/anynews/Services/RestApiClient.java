package com.anyNews.anynews.Services;

import static com.anyNews.anynews.Utils.AppHelper.showLog;

import com.anyNews.anynews.BuildConfig;
import com.anyNews.anynews.Utils.EdgeOriginWatchInterceptor;
import com.anyNews.anynews.Utils.ServerAvailabilityInterceptor;
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
        // HEADERS only — BODY logs full session/settings JSON every ~15s and large feed
        // payloads on the OkHttp thread, which worsens GC jank and debug ANRs on emulator.
        if (BuildConfig.DEBUG) {
            interceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
        }
        // First-open feed must fail fast into shimmer retry, not hang 30–100s.
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .connectTimeout(8, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(new ServerAvailabilityInterceptor())
                // New APK only: report unexpected origin List/Flash/etc. to Admin Edge/DB watch.
                .addInterceptor(new EdgeOriginWatchInterceptor())
                .addNetworkInterceptor(interceptor)
                .readTimeout(15, TimeUnit.SECONDS)
                .callTimeout(20, TimeUnit.SECONDS);

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
