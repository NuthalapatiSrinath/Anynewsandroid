package com.anyNews.anynews.rewards;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Ads-only earn flow: show N feed-inventory native ads, then claim coins.
 * Uses the same AdMob native unit as between-article feed ads (not Google test units).
 */
public class RewardsWatchAdsActivity extends AppCompatActivity {

    public static final String EXTRA_ADS_REQUIRED = "ads_required";
    public static final String EXTRA_AD_UNIT = "ad_unit_id";
    public static final String EXTRA_COINS = "coins";

    /** Same production native unit as HomePage / feed between articles. */
    private static final String FEED_NATIVE_UNIT =
            "ca-app-pub-8562336752221105/4901277505";

    private TextView progressView;
    private TextView hintView;
    private TextView nextBtn;
    private FrameLayout adContainer;
    private int required = 3;
    private int watched = 0;
    private int coins = 40;
    private String adUnit = FEED_NATIVE_UNIT;
    @Nullable private NativeAd loadedAd;
    private boolean loading;
    private boolean claiming;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rewards_watch_ads);
        try {
            MobileAds.initialize(this);
        } catch (Exception ignored) {
        }
        required = Math.max(1, getIntent().getIntExtra(EXTRA_ADS_REQUIRED, 3));
        coins = Math.max(0, getIntent().getIntExtra(EXTRA_COINS, 40));
        String unit = getIntent().getStringExtra(EXTRA_AD_UNIT);
        if (!TextUtils.isEmpty(unit)
                && !unit.contains("3940256099942544")) {
            adUnit = unit.trim();
        } else {
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
        paint();
        preload();
    }

    @Override
    protected void onDestroy() {
        if (loadedAd != null) {
            loadedAd.destroy();
            loadedAd = null;
        }
        super.onDestroy();
    }

    private void paint() {
        progressView.setText(watched + " / " + required);
        hintView.setText(
                "Earn " + coins + " coins after " + required
                        + " ads. Cooldown starts when you finish.");
        if (watched >= required) {
            nextBtn.setText("Claim coins");
            if (adContainer != null) adContainer.setVisibility(View.GONE);
        } else if (loadedAd != null) {
            nextBtn.setText("Mark ad seen · next");
        } else {
            nextBtn.setText(loading ? "Loading ad…" : "Load ad");
        }
    }

    private void onNext() {
        if (watched >= required) {
            claim();
            return;
        }
        if (loadedAd != null) {
            // User finished viewing the painted native ad
            loadedAd.destroy();
            loadedAd = null;
            if (adContainer != null) adContainer.removeAllViews();
            watched++;
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
        AdLoader loader =
                new AdLoader.Builder(this, adUnit)
                        .forNativeAd(
                                nativeAd -> {
                                    loading = false;
                                    if (isDestroyed()) {
                                        nativeAd.destroy();
                                        return;
                                    }
                                    if (loadedAd != null) loadedAd.destroy();
                                    loadedAd = nativeAd;
                                    showNative(nativeAd);
                                    paint();
                                })
                        .withAdListener(
                                new AdListener() {
                                    @Override
                                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                                        loading = false;
                                        loadedAd = null;
                                        paint();
                                        Toast.makeText(
                                                        RewardsWatchAdsActivity.this,
                                                        "Ad unavailable — try again",
                                                        Toast.LENGTH_SHORT)
                                                .show();
                                    }
                                })
                        .withNativeAdOptions(new NativeAdOptions.Builder().build())
                        .build();
        loader.loadAd(new AdRequest.Builder().build());
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
