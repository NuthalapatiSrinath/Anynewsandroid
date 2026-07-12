package com.anyNews.anynews.Utils;

import android.content.Context;

import com.anyNews.anynews.BuildConfig;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/** Debug-only image downloader so emulator can load HTTPS S3 images when system CAs fail. */
public class DebugSslImageDownloader extends BaseImageDownloader {

    public DebugSslImageDownloader(Context context) {
        super(context);
    }

    @Override
    protected HttpURLConnection createConnection(String url, Object extra) throws IOException {
        HttpURLConnection connection = super.createConnection(url, extra);
        if (BuildConfig.DEBUG && connection instanceof HttpsURLConnection) {
            try {
                TrustManager[] trustAll = new TrustManager[]{
                        new X509TrustManager() {
                            @Override
                            public void checkClientTrusted(X509Certificate[] chain, String authType) { }

                            @Override
                            public void checkServerTrusted(X509Certificate[] chain, String authType) { }

                            @Override
                            public X509Certificate[] getAcceptedIssuers() {
                                return new X509Certificate[0];
                            }
                        }
                };
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, trustAll, new SecureRandom());
                HttpsURLConnection https = (HttpsURLConnection) connection;
                https.setSSLSocketFactory(sslContext.getSocketFactory());
                https.setHostnameVerifier((hostname, session) -> true);
            } catch (Exception ignored) {
            }
        }
        return connection;
    }
}
