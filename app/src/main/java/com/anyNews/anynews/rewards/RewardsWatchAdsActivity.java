package com.anyNews.anynews.rewards;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Ads-only earn flow: watch N interstitials, then claim coins (server cooldown).
 */
public class RewardsWatchAdsActivity extends AppCompatActivity {

    public static final String EXTRA_ADS_REQUIRED = "ads_required";
    public static final String EXTRA_AD_UNIT = "ad_unit_id";
    public static final String EXTRA_COINS = "coins";

    /** Google sample interstitial — replaced when admin sets a real unit. */
    private static final String FALLBACK_UNIT =
            "ca-app-pub-3940256099942544/1033173712";

    private TextView progressView;
    private TextView hintView;
    private TextView nextBtn;
    private int required = 3;
    private int watched = 0;
    private int coins = 40;
    private String adUnit = FALLBACK_UNIT;
    private InterstitialAd loadedAd;
    private boolean loading;
    private boolean claiming;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rewards_watch_ads);
        required = Math.max(1, getIntent().getIntExtra(EXTRA_ADS_REQUIRED, 3));
        coins = Math.max(0, getIntent().getIntExtra(EXTRA_COINS, 40));
        String unit = getIntent().getStringExtra(EXTRA_AD_UNIT);
        if (!TextUtils.isEmpty(unit)) adUnit = unit.trim();

        progressView = findViewById(R.id.watch_ads_progress);
        hintView = findViewById(R.id.watch_ads_hint);
        nextBtn = findViewById(R.id.watch_ads_next_btn);
        findViewById(R.id.watch_ads_close_btn).setOnClickListener(v -> finish());
        nextBtn.setOnClickListener(v -> onNext());
        paint();
        preload();
    }

    private void paint() {
        progressView.setText(watched + " / " + required);
        hintView.setText(
                "Earn " + coins + " coins after " + required
                        + " ads. Cooldown starts when you finish.");
        if (watched >= required) {
            nextBtn.setText("Claim coins");
        } else if (loadedAd != null) {
            nextBtn.setText("Watch next ad");
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
            showAd(loadedAd);
            loadedAd = null;
            return;
        }
        preload();
    }

    private void preload() {
        if (loading || watched >= required) return;
        loading = true;
        paint();
        InterstitialAd.load(
                this,
                adUnit,
                new AdRequest.Builder().build(),
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        loading = false;
                        loadedAd = interstitialAd;
                        paint();
                    }

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
                });
    }

    private void showAd(@NonNull InterstitialAd ad) {
        ad.setFullScreenContentCallback(
                new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        watched++;
                        paint();
                        if (watched < required) preload();
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                        Toast.makeText(
                                        RewardsWatchAdsActivity.this,
                                        "Could not show ad",
                                        Toast.LENGTH_SHORT)
                                .show();
                        preload();
                    }
                });
        ad.show(this);
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
