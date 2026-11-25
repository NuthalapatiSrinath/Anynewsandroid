package com.anyNews.anynews.Utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.util.DisplayMetrics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.anyNews.anynews.BuildConfig;
import com.anyNews.anynews.Pojo.EncreptionRequest;
import com.anyNews.anynews.Pojo.EncritonRequestData;
import com.anyNews.anynews.Pojo.ListAllArticlesRequest;
import com.anyNews.anynews.Pojo.PublicEdgeFeedResponse;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.concurrent.atomic.AtomicBoolean;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.anyNews.anynews.Utils.MyUtils.encrypt;

/**
 * Optional early main page-0 before Home binds.
 * <p>
 * <b>StartingActivity must not prefetch</b> ({@link #PREFETCH_FROM_STARTING_DISABLED}):
 * with {@link OpenPathCache#FEED_DISK_DISABLED}, a splash prefetch only duplicates Home's
 * page-0 for every cold open (~300k users). HomePage remains the single feed owner;
 * NetworkCallDeduper still coalesces if another caller enqueues the same key.
 * <p>
 * Prefers Cloudflare {@code GET /public/feed}; soft-falls back to encrypted
 * {@code List_All_Articles}.
 */
public final class OpenPathFeedPrefetcher {

    private static final String TAG = "OpenPathFeedPrefetcher";
  /** When true, {@link #maybePrefetchFromStartingActivity} is a no-op (Way2-style lean open). */
    public static final boolean PREFETCH_FROM_STARTING_DISABLED = true;
    private static final AtomicBoolean launched = new AtomicBoolean(false);

    private OpenPathFeedPrefetcher() {
    }

    /** Allow a new early page-0 after language change (same process). */
    public static void resetForLanguageChange() {
        launched.set(false);
    }

    /**
     * Historical StartingActivity entry point — disabled so Home owns the one page-0 fetch.
     */
    public static void maybePrefetchFromStartingActivity(
            @Nullable Context context,
            @Nullable String pushTitle,
            @Nullable String sharePostId) {
        if (PREFETCH_FROM_STARTING_DISABLED) {
            Logone.v(TAG, "skip StartingActivity prefetch (Home owns page-0)");
            return;
        }
        maybePrefetchMainPage0(context, pushTitle, sharePostId);
    }

    /**
     * Best-effort early main page-0. Skips when offline, no identity, sticky
     * push/share, or feed is already inside the network min-interval window.
     * Uses the same {@link NetworkCallDeduper} key as Home page-0 so Home joins
     * the in-flight call instead of doubling traffic (POST fallback path).
     */
    public static void maybePrefetchMainPage0(@Nullable Context context) {
        maybePrefetchMainPage0(context, null, null);
    }

    /**
     * @param pushTitle optional push post id string from splash; non-empty blocks prefetch
     * @param sharePostId optional share redirect id from splash; non-empty blocks prefetch
     */
    public static void maybePrefetchMainPage0(
            @Nullable Context context,
            @Nullable String pushTitle,
            @Nullable String sharePostId) {
        if (context == null) return;
        final Context app = context.getApplicationContext();
        if (!launched.compareAndSet(false, true)) {
            return;
        }
        try {
            if (!isDeviceOnline(app)) {
                launched.set(false);
                return;
            }
            if (!OpenPathCache.hasUsableIdentity(app)) {
                launched.set(false);
                return;
            }
            // Sticky opens must run on Home with push/share ids — do not race plain page-0.
            if (isStickyToken(pushTitle)
                    || isStickyToken(sharePostId)
                    || hasStickyOpen(app, context)) {
                launched.set(false);
                return;
            }
            if (!OpenPathCache.shouldNetworkRefreshFeed(app, false)) {
                // Already minutes-fresh online success — warm paint covers Home.
                return;
            }
            final int langId = SharedPrefsData.getInt(app, Constants.LanguageID, Constants.PREF_NAME);
            Logone.v(TAG, "early main page-0 public edge lang=" + langId);
            Call<PublicEdgeFeedResponse> edgeCall =
                    PublicEdgeFeedHelper.enqueuePublicFeedCoalesced(
                            app, 0, 0L, 0L, new Callback<PublicEdgeFeedResponse>() {
                    @Override
                    public void onResponse(
                            @NonNull Call<PublicEdgeFeedResponse> c,
                            @NonNull Response<PublicEdgeFeedResponse> response) {
                        try {
                            int langNow = SharedPrefsData.getInt(
                                    app, Constants.LanguageID, Constants.PREF_NAME);
                            if (langNow != langId) {
                                Logone.v(TAG, "early edge dropped lang enq="
                                        + langId + " now=" + langNow);
                                return;
                            }
                            // Empty CF page-0 must not warm OpenPath — soft-fall to List.
                            if (!response.isSuccessful()
                                    || !PublicEdgeFeedHelper.isAcceptableEdgeResponse(
                                            response.body(), 0L)) {
                                enqueuePostFallback(app, langId);
                                return;
                            }
                            EncreptionRequest wrapped =
                                    PublicEdgeFeedHelper.toEncreptionRequest(
                                            app, response.body(), 0L);
                            if (wrapped != null
                                    && wrapped.getData() != null
                                    && !wrapped.getData().isEmpty()) {
                                OpenPathCache.saveFeed(
                                        app, 0, wrapped, OpenPathCache.FEED_MODE_MAIN);
                                try {
                                    LanguageFeedInvalidator.markFeedLanguageApplied(
                                            app, langNow);
                                } catch (Exception ignored) {
                                }
                                Logone.v(TAG, "early main page-0 edge saved to OpenPath");
                                return;
                            }
                        } catch (Exception e) {
                            Logone.w(e, TAG + " edge save failed");
                        }
                        // Soft-fail → encrypted POST.
                        enqueuePostFallback(app, langId);
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<PublicEdgeFeedResponse> c,
                            @NonNull Throwable t) {
                        Logone.w(t, TAG + " early edge failed");
                        enqueuePostFallback(app, langId);
                    }
                });
            if (edgeCall != null) {
                return;
            }
            enqueuePostFallback(app, langId);
        } catch (Exception e) {
            Logone.w(e, TAG + " enqueue failed");
            launched.set(false);
        }
    }

    private static void enqueuePostFallback(@NonNull Context app, int langId) {
        try {
            final JsonObject body = buildMainPage0Body(app);
            if (body == null) {
                launched.set(false);
                return;
            }
            APIService api = RestApiClient.getClient().create(APIService.class);
            Call<EncreptionRequest> call = api.List_All_Articles("v3", body);
            final String dedupeKey = "articles_v3_" + langId + "_0_0_0_0_0_false_false";
            Logone.v(TAG, "early main page-0 POST fallback lang=" + langId);
            NetworkCallDeduper.enqueueUnique(dedupeKey, call, new Callback<EncreptionRequest>() {
                @Override
                public void onResponse(
                        @NonNull Call<EncreptionRequest> c,
                        @NonNull Response<EncreptionRequest> response) {
                    try {
                        int langNow = SharedPrefsData.getInt(
                                app, Constants.LanguageID, Constants.PREF_NAME);
                        if (langNow != langId) {
                            Logone.v(TAG, "early main page-0 dropped lang enq="
                                    + langId + " now=" + langNow);
                            return;
                        }
                        if (response.body() != null
                                && response.body().getData() != null
                                && !response.body().getData().isEmpty()) {
                            OpenPathCache.saveFeed(
                                    app, 0, response.body(), OpenPathCache.FEED_MODE_MAIN);
                            try {
                                LanguageFeedInvalidator.markFeedLanguageApplied(
                                        app,
                                        SharedPrefsData.getInt(
                                                app, Constants.LanguageID, Constants.PREF_NAME));
                            } catch (Exception ignored) {
                            }
                            Logone.v(TAG, "early main page-0 POST saved to OpenPath");
                        }
                    } catch (Exception e) {
                        Logone.w(e, TAG + " save failed");
                    }
                }

                @Override
                public void onFailure(
                        @NonNull Call<EncreptionRequest> c, @NonNull Throwable t) {
                    Logone.w(t, TAG + " early page-0 failed");
                    launched.set(false);
                }
            });
        } catch (Exception e) {
            Logone.w(e, TAG + " POST fallback failed");
            launched.set(false);
        }
    }

    private static boolean isStickyToken(@Nullable String raw) {
        if (raw == null) return false;
        String t = raw.trim();
        return !t.isEmpty()
                && !t.equals("0")
                && !t.equalsIgnoreCase("null");
    }

    private static boolean hasStickyOpen(@NonNull Context app, @NonNull Context any) {
        try {
            String pendingShare = SharedPrefsData.getString(
                    app, Constants.PENDING_SHARE_POST, Constants.PREF_NAME);
            if (pendingShare != null
                    && !pendingShare.trim().isEmpty()
                    && !pendingShare.equals("0")
                    && !pendingShare.equalsIgnoreCase("null")) {
                return true;
            }
            if (any instanceof android.app.Activity) {
                android.content.Intent intent = ((android.app.Activity) any).getIntent();
                if (intent != null) {
                    String title = intent.getStringExtra("TITLE");
                    if (title != null
                            && !title.trim().isEmpty()
                            && !title.equals("0")
                            && !title.equalsIgnoreCase("null")) {
                        return true;
                    }
                    String share = intent.getStringExtra("SHARE_REDIRECT_ID");
                    if (share != null
                            && !share.trim().isEmpty()
                            && !share.equals("0")
                            && !share.equalsIgnoreCase("null")) {
                        return true;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    @Nullable
    private static JsonObject buildMainPage0Body(@NonNull Context app) {
        try {
            ListAllArticlesRequest requestModel = new ListAllArticlesRequest();
            requestModel.setDraftId(
                    SharedPrefsData.getInt(app, Constants.DraftID, Constants.PREF_NAME));
            requestModel.setCategoryId(0);
            requestModel.setLanguageId(
                    SharedPrefsData.getInt(app, Constants.LanguageID, Constants.PREF_NAME));
            requestModel.setSkip(0L);
            requestModel.setLastPostId(0L);
            requestModel.setPushPostId(0L);
            requestModel.setReferred_post_id(
                    SharedPrefsData.getString(app, Constants.REFERRER_POST, Constants.PREF_NAME));
            requestModel.setSharePostId(0L);
            requestModel.setActualSkip(0L);
            try {
                Allsharedpreference prefs = new Allsharedpreference(app);
                requestModel.setAdvertiserID(
                        prefs.getAdvertiserId(Allsharedpreference.ADVERTISER_ID));
            } catch (Exception ignored) {
                requestModel.setAdvertiserID("");
            }
            requestModel.setDatafrom2g("0");
            requestModel.setSpecial_posts(false);
            requestModel.setVideo_posts(false);
            DisplayMetrics dm = app.getResources().getDisplayMetrics();
            requestModel.setScreenHeight(dm.heightPixels);
            requestModel.setScreenWidth(dm.widthPixels);
            requestModel.setUser_session_id(
                    SharedPrefsData.getString(app, Constants.USER_SESSION_ID, Constants.PREF_NAME));
            requestModel.setApp_version_name("" + BuildConfig.VERSION_NAME);
            requestModel.setApp_version_number("" + BuildConfig.VERSION_CODE);
            requestModel.setSdk_version(Build.VERSION.SDK_INT);
            requestModel.setNotif_perm_allowed(1);

            String jsonString = new Gson().toJson(requestModel);
            EncritonRequestData enc = new EncritonRequestData();
            enc.setData(encrypt(jsonString));
            return new Gson().toJsonTree(enc).getAsJsonObject();
        } catch (Exception e) {
            Logone.w(e, TAG + " build body failed");
            return null;
        }
    }

    private static boolean isDeviceOnline(@NonNull Context context) {
        try {
            ConnectivityManager cm =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) return false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                android.net.Network net = cm.getActiveNetwork();
                if (net == null) return false;
                NetworkCapabilities caps = cm.getNetworkCapabilities(net);
                if (caps == null) return false;
                return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        && (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                        || caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                        || caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                        || caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN));
            }
            @SuppressWarnings("deprecation")
            android.net.NetworkInfo info = cm.getActiveNetworkInfo();
            return info != null && info.isConnected();
        } catch (Exception e) {
            return false;
        }
    }
}
