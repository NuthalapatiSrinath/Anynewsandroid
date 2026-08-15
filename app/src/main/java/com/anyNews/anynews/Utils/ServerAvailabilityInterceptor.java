package com.anyNews.anynews.Utils;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Classifies Retrofit/OkHttp outcomes for {@link ServerAvailabilityMonitor}.
 * Does not alter responses — observation only.
 */
public final class ServerAvailabilityInterceptor implements Interceptor {

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();
        // Health probes report themselves; skip to avoid double-counting.
        if ("1".equals(request.header(ServerAvailabilityMonitor.HEALTH_HEADER))) {
            return chain.proceed(request);
        }
        try {
            Response response = chain.proceed(request);
            ServerAvailabilityMonitor.get().onHttpResponse(response.code());
            return response;
        } catch (IOException e) {
            ServerAvailabilityMonitor.get().onTransportFailure(e);
            throw e;
        }
    }
}
