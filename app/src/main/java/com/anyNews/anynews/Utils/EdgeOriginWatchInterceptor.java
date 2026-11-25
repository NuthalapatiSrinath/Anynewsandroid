package com.anyNews.anynews.Utils;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Observes Retrofit/OkHttp traffic for unexpected origin API calls on debug APKs
 * ({@link BuildConfig#DEBUG} via {@link EdgeOriginCallWatch#isInstrumentedBuild()}).
 * Never alters requests/responses; beacons are fire-and-forget.
 */
public final class EdgeOriginWatchInterceptor implements Interceptor {

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();
        try {
            // Always log for the footer Origin Calls page (in-memory only).
            OriginCallLog.record(request.url(), request.method());
        } catch (Exception ignored) {
            /* never break the call path */
        }
        try {
            if (!"1".equals(request.header(EdgeOriginCallWatch.HEADER_SKIP))) {
                String detailHint = request.header(EdgeOriginCallWatch.HEADER_DETAIL);
                EdgeOriginCallWatch.observeRequest(
                        request.url(),
                        request.method(),
                        detailHint);
            }
        } catch (Exception ignored) {
            /* never break the call path */
        }
        return chain.proceed(request);
    }
}
