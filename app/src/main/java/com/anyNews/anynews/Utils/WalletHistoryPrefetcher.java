package com.anyNews.anynews.Utils;

import android.content.Context;

import com.anyNews.anynews.Pojo.EncreptionRequest;
import com.anyNews.anynews.Pojo.EncritonRequestData;
import com.anyNews.anynews.Pojo.PaymentHistoryRequest;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.testing.PostItem;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.anyNews.anynews.Utils.MyUtils.decrypt;
import static com.anyNews.anynews.Utils.MyUtils.encrypt;

/**
 * Lightweight page-0 prefetch for wallet history while profile is open
 * (reporter + network only), so Total Cash / Payment History opens from disk.
 */
public final class WalletHistoryPrefetcher {

    private static final String TAG = "WalletHistoryPrefetcher";
    private static volatile boolean inFlight;
    private static final java.util.concurrent.atomic.AtomicInteger pending =
            new java.util.concurrent.atomic.AtomicInteger(0);

    private WalletHistoryPrefetcher() {
    }

    public static void prefetchIfPossible(Context context) {
        if (context == null) return;
        final Context app = context.getApplicationContext();
        if (inFlight) return;

        boolean reporter = SharedPrefsData.getBool(app, Constants.is_reporter, Constants.PREF_NAME);
        if (!reporter) return;

        long userId = WalletUserId.resolve(app);
        int draftId = SharedPrefsData.getInt(app, Constants.DraftID, Constants.PREF_NAME);
        if (userId <= 0 || draftId <= 0) return;

        if (!isNetworkFree(app)) return;

        boolean needArticle = WalletHistoryCache.isMissingOrStale(app, WalletHistoryCache.TYPE_ARTICLE);
        boolean needRedeem = WalletHistoryCache.isMissingOrStale(app, WalletHistoryCache.TYPE_REDEEM);
        if (!needArticle && !needRedeem) return;

        inFlight = true;
        pending.set(0);
        if (needArticle) fetchAndCache(app, true);
        if (needRedeem) fetchAndCache(app, false);
        if (pending.get() <= 0) {
            inFlight = false;
        }
    }

    private static boolean isNetworkFree(Context app) {
        try {
            if (!BookmarkStore.isNetworkAvailable(app)) return false;
        } catch (Exception e) {
            try {
                if (!MyUtils.isNetworkAvailable(app)) return false;
            } catch (Exception ignored) {
                return false;
            }
        }
        try {
            android.net.ConnectivityManager cm =
                    (android.net.ConnectivityManager) app.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) return true;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                android.net.Network net = cm.getActiveNetwork();
                if (net == null) return false;
                android.net.NetworkCapabilities caps = cm.getNetworkCapabilities(net);
                if (caps == null) return false;
                if (!caps.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                    return false;
                }
            }
        } catch (Exception ignored) {
        }
        return true;
    }

    private static void fetchAndCache(final Context app, final boolean article) {
        pending.incrementAndGet();
        try {
            JsonObject body = buildBody(app);
            APIService api = RestApiClient.getClient().create(APIService.class);
            Call<EncreptionRequest> call = article
                    ? api.get_article_transactions("v3", body)
                    : api.get_redeem_transactions("v3", body);
            call.enqueue(new Callback<EncreptionRequest>() {
                @Override
                public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {
                    try {
                        if (response == null || response.body() == null || response.body().getData() == null) {
                            return;
                        }
                        String decrypted = decrypt(response.body().getData());
                        PostItem item = new Gson().fromJson(decrypted, PostItem.class);
                        if (item != null && Boolean.TRUE.equals(item.getSuccess())) {
                            WalletHistoryCache.save(app,
                                    article ? WalletHistoryCache.TYPE_ARTICLE : WalletHistoryCache.TYPE_REDEEM,
                                    item);
                        }
                    } catch (Exception e) {
                        Logone.w(e, TAG + " prefetch parse failed article=" + article);
                    } finally {
                        finishOne();
                    }
                }

                @Override
                public void onFailure(Call<EncreptionRequest> call, Throwable t) {
                    finishOne();
                }
            });
        } catch (Exception e) {
            Logone.w(e, TAG + " prefetch enqueue failed");
            finishOne();
        }
    }

    private static void finishOne() {
        if (pending.decrementAndGet() <= 0) {
            pending.set(0);
            inFlight = false;
        }
    }

    private static JsonObject buildBody(Context app) {
        PaymentHistoryRequest requestModel = new PaymentHistoryRequest();
        requestModel.setUserId(WalletUserId.resolve(app));
        requestModel.setDraftId(SharedPrefsData.getInt(app, Constants.DraftID, Constants.PREF_NAME));
        requestModel.setSkip(0L);
        requestModel.setFirstTransactionId(0L);
        String jsonString = new Gson().toJson(requestModel);
        EncritonRequestData enc = new EncritonRequestData();
        enc.setData(encrypt(jsonString));
        return new Gson().toJsonTree(enc).getAsJsonObject();
    }
}
