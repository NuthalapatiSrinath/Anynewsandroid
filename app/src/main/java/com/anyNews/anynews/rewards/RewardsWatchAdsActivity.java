package com.anyNews.anynews.rewards;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.gson.JsonObject;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Ads-only earn flow: show N ads (native / interstitial / rewarded per admin), then claim coins.
 * Progress may persist across reopen when admin enables persist_progress (default on).
 */
public class RewardsWatchAdsActivity extends AppCompatActivity {

    public static final String EXTRA_ADS_REQUIRED = "ads_required";
    public static final String EXTRA_AD_UNIT = "ad_unit_id";
    public static final String EXTRA_AD_FORMAT = "ad_format";
    public static final String EXTRA_COINS = "coins";
    public static final String EXTRA_PERSIST_PROGRESS = "persist_progress";

    public static final String FORMAT_NATIVE = "native";
    public static final String FORMAT_INTERSTITIAL = "interstitial";
    public static final String FORMAT_REWARDED = "rewarded";

    private static final String PREF_WATCH_PROGRESS = "rewards_watch_ads_progress";
    private static final String PREF_WATCH_REQUIRED = "rewards_watch_ads_required_at";

    private static final String FEED_NATIVE_UNIT =
            "ca-app-pub-8562336752221105/4901277505";
    private static final String WATCH_ADS_REWARDED_UNIT =
            "ca-app-pub-8562336752221105/4844814289";

    private TextView progressView;
    private TextView hintView;
    private TextView nextBtn;
    private FrameLayout adContainer;
    private int required = 3;
    private int watched = 0;
    private int coins = 40;
    private String adUnit = WATCH_ADS_REWARDED_UNIT;
    private String adFormat = FORMAT_REWARDED;
    private boolean persistProgress = true;
    @Nullable private NativeAd loadedNative;
    @Nullable private InterstitialAd loadedInterstitial;
    @Nullable private RewardedAd loadedRewarded;
    private boolean loading;
    private boolean claiming;
    private boolean fullscreenShowing;
    private boolean rewardedEarnedThisShow;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyImmersiveFullscreen();
        setContentView(R.layout.activity_rewards_watch_ads);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        setTitle("");
        try {
            MobileAds.initialize(this);
        } catch (Exception ignored) {
        }
        required = Math.max(1, getIntent().getIntExtra(EXTRA_ADS_REQUIRED, 3));
        coins = Math.max(0, getIntent().getIntExtra(EXTRA_COINS, 40));
        persistProgress = getIntent().getBooleanExtra(EXTRA_PERSIST_PROGRESS, true);
        String fmt = getIntent().getStringExtra(EXTRA_AD_FORMAT);
        if (!TextUtils.isEmpty(fmt)) {
            fmt = fmt.trim().toLowerCase(Locale.US);
            if (FORMAT_INTERSTITIAL.equals(fmt)
                    || FORMAT_REWARDED.equals(fmt)
                    || FORMAT_NATIVE.equals(fmt)) {
                adFormat = fmt;
            } else {
                adFormat = FORMAT_REWARDED;
            }
        }
        String unit = getIntent().getStringExtra(EXTRA_AD_UNIT);
        if (!TextUtils.isEmpty(unit) && !unit.contains("3940256099942544")) {
            adUnit = unit.trim();
        } else if (FORMAT_REWARDED.equals(adFormat)) {
            adUnit = WATCH_ADS_REWARDED_UNIT;
        } else if (FORMAT_NATIVE.equals(adFormat)) {
            try {
                String fallback = getString(R.string.admob_native_ad_unit_fallback);
                if (!TextUtils.isEmpty(fallback)) adUnit = fallback.trim();
            } catch (Exception ignored) {
                adUnit = FEED_NATIVE_UNIT;
            }
        }

        progressView = findViewById(R.id.watch_ads_progress);
        hintView = findViewById(R.id.watch_ads_hint);
        nextBtn = findViewById(R.id.watch_ads_next_btn);
        adContainer = findViewById(R.id.watch_ads_container);
        findViewById(R.id.watch_ads_close_btn).setOnClickListener(v -> finish());
        nextBtn.setOnClickListener(v -> onNext());

        restoreProgressIfEnabled();
        paint();
        preload();
        if (isFullscreenFormat()) {
            nextBtn.postDelayed(
                    () -> {
                        if (!isFinishing() && hasFullscreenReady() && !fullscreenShowing) {
                            showFullscreen();
                        }
                    },
                    400);
        }
    }

    private void applyImmersiveFullscreen() {
        try {
            WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
            getWindow()
                    .setFlags(
                            WindowManager.LayoutParams.FLAG_FULLSCREEN,
                            WindowManager.LayoutParams.FLAG_FULLSCREEN);
            WindowInsetsControllerCompat c =
                    WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
            if (c != null) {
                c.hide(WindowInsetsCompat.Type.statusBars() | WindowInsetsCompat.Type.navigationBars());
                c.setSystemBarsBehavior(
                        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } catch (Exception ignored) {
        }
    }

    private void restoreProgressIfEnabled() {
        if (!persistProgress) {
            clearSavedProgress();
            return;
        }
        try {
            int savedReq =
                    SharedPrefsData.getInt(this, PREF_WATCH_REQUIRED, Constants.PREF_NAME);
            int saved =
                    SharedPrefsData.getInt(this, PREF_WATCH_PROGRESS, Constants.PREF_NAME);
            if (savedReq == required && saved > 0 && saved < required) {
                watched = saved;
            }
        } catch (Exception ignored) {
        }
    }

    private void saveProgress() {
        if (!persistProgress) return;
        try {
            SharedPrefsData.putInt(this, PREF_WATCH_PROGRESS, watched, Constants.PREF_NAME);
            SharedPrefsData.putInt(this, PREF_WATCH_REQUIRED, required, Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
    }

    private void clearSavedProgress() {
        try {
            SharedPrefsData.putInt(this, PREF_WATCH_PROGRESS, 0, Constants.PREF_NAME);
            SharedPrefsData.putInt(this, PREF_WATCH_REQUIRED, 0, Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void onDestroy() {
        destroyLoadedAds();
        super.onDestroy();
    }

    private void destroyLoadedAds() {
        if (loadedNative != null) {
            loadedNative.destroy();
            loadedNative = null;
        }
        loadedInterstitial = null;
        loadedRewarded = null;
    }

    private boolean isFullscreenFormat() {
        return FORMAT_INTERSTITIAL.equals(adFormat) || FORMAT_REWARDED.equals(adFormat);
    }

    private void paint() {
        progressView.setText(watched + " / " + required);
        String formatHint =
                FORMAT_REWARDED.equals(adFormat)
                        ? "Rewarded video"
                        : FORMAT_INTERSTITIAL.equals(adFormat)
                                ? "Full-screen ad"
                                : "Ad";
        hintView.setText(
                "Earn "
                        + coins
                        + " coins after "
                        + required
                        + " ads ("
                        + formatHint
                        + "). Cooldown starts when you finish.");
        if (adContainer != null) {
            adContainer.setVisibility(
                    watched >= required || isFullscreenFormat() ? View.GONE : View.VISIBLE);
        }
        if (watched >= required) {
            nextBtn.setText("Claim coins");
        } else if (isFullscreenFormat()) {
            if (hasFullscreenReady()) {
                nextBtn.setText("Watch ad · " + (watched + 1) + "/" + required);
            } else {
                nextBtn.setText(loading ? "Loading ad…" : "Load ad");
            }
        } else if (loadedNative != null) {
            nextBtn.setText("Mark ad seen · next");
        } else {
            nextBtn.setText(loading ? "Loading ad…" : "Load ad");
        }
    }

    private boolean hasFullscreenReady() {
        if (FORMAT_INTERSTITIAL.equals(adFormat)) return loadedInterstitial != null;
        if (FORMAT_REWARDED.equals(adFormat)) return loadedRewarded != null;
        return false;
    }

    private void onNext() {
        if (watched >= required) {
            claim();
            return;
        }
        if (isFullscreenFormat()) {
            if (hasFullscreenReady() && !fullscreenShowing) {
                showFullscreen();
            } else if (!loading) {
                preload();
            }
            return;
        }
        if (loadedNative != null) {
            loadedNative.destroy();
            loadedNative = null;
            if (adContainer != null) adContainer.removeAllViews();
            watched++;
            saveProgress();
            paint();
            if (watched < required) preload();
            return;
        }
        preload();
    }

    private void preload() {
        if (loading || watched >= required) return;
        loading = true;
        paint();
        if (FORMAT_INTERSTITIAL.equals(adFormat)) {
            preloadInterstitial();
        } else if (FORMAT_REWARDED.equals(adFormat)) {
            preloadRewarded();
        } else {
            preloadNative();
        }
    }

    private void preloadNative() {
        AdLoader loader =
                new AdLoader.Builder(this, adUnit)
                        .forNativeAd(
                                nativeAd -> {
                                    loading = false;
                                    if (isDestroyed()) {
                                        nativeAd.destroy();
                                        return;
                                    }
                                    if (loadedNative != null) loadedNative.destroy();
                                    loadedNative = nativeAd;
                                    showNative(nativeAd);
                                    paint();
                                })
                        .withAdListener(
                                new AdListener() {
                                    @Override
                                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                                        loading = false;
                                        loadedNative = null;
                                        paint();
                                        toastLoadFail();
                                    }
                                })
                        .withNativeAdOptions(new NativeAdOptions.Builder().build())
                        .build();
        loader.loadAd(new AdRequest.Builder().build());
    }

    private void preloadInterstitial() {
        InterstitialAd.load(
                this,
                adUnit,
                new AdRequest.Builder().build(),
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd ad) {
                        loading = false;
                        loadedInterstitial = ad;
                        paint();
                        if (!fullscreenShowing && watched < required) {
                            showFullscreen();
                        }
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        loading = false;
                        loadedInterstitial = null;
                        paint();
                        toastLoadFail();
                    }
                });
    }

    private void preloadRewarded() {
        RewardedAd.load(
                this,
                adUnit,
                new AdRequest.Builder().build(),
                new RewardedAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull RewardedAd ad) {
                        loading = false;
                        loadedRewarded = ad;
                        paint();
                        if (!fullscreenShowing && watched < required) {
                            showFullscreen();
                        }
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        loading = false;
                        loadedRewarded = null;
                        paint();
                        toastLoadFail();
                    }
                });
    }

    private void toastLoadFail() {
        Toast.makeText(
                        this,
                        "Ad unavailable — check AdMob unit matches format ("
                                + adFormat
                                + ")",
                        Toast.LENGTH_LONG)
                .show();
    }

    private void showFullscreen() {
        applyImmersiveFullscreen();
        if (FORMAT_INTERSTITIAL.equals(adFormat) && loadedInterstitial != null) {
            fullscreenShowing = true;
            InterstitialAd ad = loadedInterstitial;
            loadedInterstitial = null;
            ad.setFullScreenContentCallback(
                    new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            fullscreenShowing = false;
                            watched++;
                            saveProgress();
                            paint();
                            if (watched < required) preload();
                            else if (watched >= required) claim();
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                            fullscreenShowing = false;
                            paint();
                            Toast.makeText(
                                            RewardsWatchAdsActivity.this,
                                            "Could not show ad",
                                            Toast.LENGTH_SHORT)
                                    .show();
                            preload();
                        }
                    });
            ad.show(this);
            paint();
            return;
        }
        if (FORMAT_REWARDED.equals(adFormat) && loadedRewarded != null) {
            fullscreenShowing = true;
            rewardedEarnedThisShow = false;
            RewardedAd ad = loadedRewarded;
            loadedRewarded = null;
            ad.setFullScreenContentCallback(
                    new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            fullscreenShowing = false;
                            if (rewardedEarnedThisShow) {
                                watched++;
                                saveProgress();
                            } else {
                                Toast.makeText(
                                                RewardsWatchAdsActivity.this,
                                                "Finish the video to count this ad",
                                                Toast.LENGTH_SHORT)
                                        .show();
                            }
                            paint();
                            if (watched < required) preload();
                            else if (watched >= required) claim();
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                            fullscreenShowing = false;
                            paint();
                            Toast.makeText(
                                            RewardsWatchAdsActivity.this,
                                            "Could not show ad",
                                            Toast.LENGTH_SHORT)
                                    .show();
                            preload();
                        }
                    });
            ad.show(
                    this,
                    new OnUserEarnedRewardListener() {
                        @Override
                        public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                            rewardedEarnedThisShow = true;
                        }
                    });
            paint();
        }
    }

    private void showNative(@NonNull NativeAd nativeAd) {
        if (adContainer == null) return;
        adContainer.setVisibility(View.VISIBLE);
        adContainer.removeAllViews();
        NativeAdView adView =
                (NativeAdView)
                        LayoutInflater.from(this)
                                .inflate(R.layout.ad_unified_new_ad, adContainer, false);
        populateNativeAdView(nativeAd, adView);
        adContainer.addView(adView);
    }

    private void populateNativeAdView(@NonNull NativeAd nativeAd, @NonNull NativeAdView adView) {
        MediaView mediaView = adView.findViewById(R.id.ad_media);
        if (mediaView != null) {
            adView.setMediaView(mediaView);
        }
        TextView headline = adView.findViewById(R.id.ad_headline);
        TextView body = adView.findViewById(R.id.ad_body);
        TextView cta = adView.findViewById(R.id.ad_call_to_action);
        ImageView icon = adView.findViewById(R.id.ad_app_icon);
        RatingBar rating = adView.findViewById(R.id.ad_stars);
        TextView advertiser = adView.findViewById(R.id.ad_advertiser);

        if (headline != null) {
            headline.setText(nativeAd.getHeadline());
            adView.setHeadlineView(headline);
        }
        if (body != null) {
            body.setText(nativeAd.getBody());
            adView.setBodyView(body);
        }
        if (cta != null) {
            cta.setText(nativeAd.getCallToAction());
            adView.setCallToActionView(cta);
        }
        if (icon != null) {
            if (nativeAd.getIcon() != null) {
                icon.setImageDrawable(nativeAd.getIcon().getDrawable());
                icon.setVisibility(View.VISIBLE);
            } else {
                icon.setVisibility(View.GONE);
            }
            adView.setIconView(icon);
        }
        if (rating != null) {
            if (nativeAd.getStarRating() != null) {
                rating.setRating(nativeAd.getStarRating().floatValue());
                rating.setVisibility(View.VISIBLE);
            } else {
                rating.setVisibility(View.GONE);
            }
            adView.setStarRatingView(rating);
        }
        if (advertiser != null) {
            advertiser.setText(nativeAd.getAdvertiser());
            adView.setAdvertiserView(advertiser);
        }
        adView.setNativeAd(nativeAd);
    }

    private void claim() {
        if (claiming) return;
        claiming = true;
        JsonObject body = RewardsAuth.authBody(this);
        body.addProperty("ads_watched", watched);
        APIService api = RestApiClient.getClient().create(APIService.class);
        api.rewards_watch_ads_claim("v3", body)
                .enqueue(
                        new Callback<JsonObject>() {
                            @Override
                            public void onResponse(
                                    Call<JsonObject> call, Response<JsonObject> response) {
                                claiming = false;
                                JsonObject b = response.body();
                                if (b != null
                                        && b.has("success")
                                        && b.get("success").getAsBoolean()) {
                                    clearSavedProgress();
                                    int awarded =
                                            b.has("coins_awarded")
                                                    ? b.get("coins_awarded").getAsInt()
                                                    : coins;
                                    Toast.makeText(
                                                    RewardsWatchAdsActivity.this,
                                                    "+" + awarded + " coins",
                                                    Toast.LENGTH_SHORT)
                                            .show();
                                    setResult(RESULT_OK);
                                    finish();
                                } else {
                                    String msg =
                                            b != null && b.has("msg")
                                                    ? b.get("msg").getAsString()
                                                    : "Claim failed";
                                    Toast.makeText(
                                                    RewardsWatchAdsActivity.this,
                                                    msg,
                                                    Toast.LENGTH_SHORT)
                                            .show();
                                }
                            }

                            @Override
                            public void onFailure(Call<JsonObject> call, Throwable t) {
                                claiming = false;
                                Toast.makeText(
                                                RewardsWatchAdsActivity.this,
                                                "Network error",
                                                Toast.LENGTH_SHORT)
                                        .show();
                            }
                        });
    }
}
