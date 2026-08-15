package com.anyNews.anynews.Utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.anyNews.anynews.models.StatesModel;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Warms Picasso's disk/memory cache for state landmark icon URLs so the state
 * selection grid can bind without flashing local placeholder drawables.
 */
public final class StateLandmarkPrefetcher {

    /** Cap wait so a slow network still shows the list (without wrong placeholders). */
    private static final long TIMEOUT_MS = 2500L;

    public interface OnCompleteListener {
        void onComplete();
    }

    private StateLandmarkPrefetcher() {
    }

    public static void prefetch(Context context,
                                List<StatesModel.Datum> states,
                                @Nullable OnCompleteListener listener) {
        final Context appContext = context != null ? context.getApplicationContext() : null;
        List<String> urls = collectIconUrls(states);
        if (urls.isEmpty() || appContext == null) {
            if (listener != null) {
                listener.onComplete();
            }
            return;
        }

        final AtomicInteger remaining = new AtomicInteger(urls.size());
        final AtomicBoolean finished = new AtomicBoolean(false);
        final Handler mainHandler = new Handler(Looper.getMainLooper());
        final Runnable finishOnce = () -> {
            if (finished.compareAndSet(false, true) && listener != null) {
                listener.onComplete();
            }
        };

        if (listener != null) {
            mainHandler.postDelayed(finishOnce, TIMEOUT_MS);
        }

        for (String url : urls) {
            Picasso.get()
                    .load(url)
                    .fetch(new Callback() {
                        @Override
                        public void onSuccess() {
                            if (remaining.decrementAndGet() <= 0) {
                                if (listener != null) {
                                    mainHandler.removeCallbacks(finishOnce);
                                }
                                finishOnce.run();
                            }
                        }

                        @Override
                        public void onError(Exception e) {
                            if (remaining.decrementAndGet() <= 0) {
                                if (listener != null) {
                                    mainHandler.removeCallbacks(finishOnce);
                                }
                                finishOnce.run();
                            }
                        }
                    });
        }
    }

    private static List<String> collectIconUrls(List<StatesModel.Datum> states) {
        List<String> urls = new ArrayList<>();
        if (states == null) {
            return urls;
        }
        for (StatesModel.Datum state : states) {
            if (state == null || TextUtils.isEmpty(state.getIconUrl())) {
                continue;
            }
            String resolved = MediaUrl.resolve(state.getIconUrl());
            if (!TextUtils.isEmpty(resolved) && !urls.contains(resolved)) {
                urls.add(resolved);
            }
        }
        return urls;
    }
}
