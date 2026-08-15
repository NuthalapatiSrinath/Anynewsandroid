package com.anyNews.anynews.Utils;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Coalesces concurrent identical Retrofit calls so a stampede of the same
 * open-path request (e.g. double List_All_Articles) hits the network once.
 */
public final class NetworkCallDeduper {

    private static final ConcurrentHashMap<String, List<Callback<?>>> waiters =
            new ConcurrentHashMap<>();

    private NetworkCallDeduper() {
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> void enqueueUnique(@NonNull String key,
                                         @NonNull Call<T> call,
                                         @NonNull Callback<T> callback) {
        List existing = waiters.get(key);
        if (existing != null) {
            synchronized (existing) {
                existing.add(callback);
            }
            // Redundant identical in-flight request — drop the unused Call.
            try {
                if (!call.isExecuted()) call.cancel();
            } catch (Exception ignored) {
            }
            Logone.d("NetworkCallDeduper coalesced key=" + key);
            return;
        }

        List<Callback<?>> list = new ArrayList<>();
        list.add(callback);
        List previous = waiters.putIfAbsent(key, list);
        if (previous != null) {
            synchronized (previous) {
                previous.add(callback);
            }
            try {
                if (!call.isExecuted()) call.cancel();
            } catch (Exception ignored) {
            }
            return;
        }

        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(@NonNull Call<T> c, @NonNull Response<T> response) {
                List<Callback<?>> listeners = waiters.remove(key);
                if (listeners == null) return;
                List<Callback<?>> copy;
                synchronized (listeners) {
                    copy = new ArrayList<>(listeners);
                }
                for (Callback<?> raw : copy) {
                    try {
                        ((Callback<T>) raw).onResponse(c, response);
                    } catch (Exception e) {
                        Logone.w(e, "NetworkCallDeduper listener onResponse");
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<T> c, @NonNull Throwable t) {
                List<Callback<?>> listeners = waiters.remove(key);
                if (listeners == null) return;
                List<Callback<?>> copy;
                synchronized (listeners) {
                    copy = new ArrayList<>(listeners);
                }
                for (Callback<?> raw : copy) {
                    try {
                        ((Callback<T>) raw).onFailure(c, t);
                    } catch (Exception e) {
                        Logone.w(e, "NetworkCallDeduper listener onFailure");
                    }
                }
            }
        });
    }
}
