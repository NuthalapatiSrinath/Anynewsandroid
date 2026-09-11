package com.anyNews.anynews.rewards;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Rewards watch-ad page. The ad preloads during a 5s countdown and starts only
 * when the user taps. Leaving the page drops the loaded ad so it cannot play
 * on Home or the feed. Coins follow ads_seen (total coins / cap-day ad count).
 */
public class RewardsWatchAdsActivity extends AppCompatActivity {

    public static final String EXTRA_ADS_REQUIRED = "ads_required";
    public static final String EXTRA_AD_UNIT = "ad_unit_id";
    public static final String EXTRA_AD_FORMAT = "ad_format";
    public static final String EXTRA_COINS = "coins";
    public static final String EXTRA_PERSIST_PROGRESS = "persist_progress";
    public static final String EXTRA_COOLDOWN_MINUTES = "cooldown_minutes";
    public static final String EXTRA_ADS_CAPPED = "ads_capped";
    public static final String EXTRA_ADS_REMAINING = "ads_remaining";

    public static final String FORMAT_NATIVE = "native";
    public static final String FORMAT_INTERSTITIAL = "interstitial";
    public static final String FORMAT_REWARDED = "rewarded";

    private static final String PREF_WATCH_PROGRESS = "rewards_watch_ads_progress";
    private static final String PREF_WATCH_REQUIRED = "rewards_watch_ads_required_at";
    private static final int COUNTDOWN_SEC = 5;

    private static final String FEED_NATIVE_UNIT =
            "ca-app-pub-8562336752221105/4901277505";
    private static final String WATCH_ADS_REWARDED_UNIT =
            "ca-app-pub-8562336752221105/4844814289";

    private TextView progressView;
    private TextView countdownView;
    private TextView hintView;
    private TextView waitView;
    private TextView nextBtn;
    private FrameLayout adContainer;

    private int required = 2;
    private int watched = 0;
    private int coins = 0;
    private int sessionEarned = 0;
    private int cooldownMinutes = 5;
    private int countdownLeft = COUNTDOWN_SEC;
    private int waitSeconds = 0;
    private String adUnit = WATCH_ADS_REWARDED_UNIT;
    private String adFormat = FORMAT_REWARDED;

    @Nullable private NativeAd loadedNative;
    @Nullable private InterstitialAd loadedInterstitial;
    @Nullable private RewardedAd loadedRewarded;

    private int loadGen;
    private boolean loading;
    private boolean claiming;
    private boolean fullscreenShowing;
    private boolean rewardedEarnedThisShow;
    private boolean userAskedToShow;
    private boolean leaving;
    private boolean started;
    private boolean sessionClosed;
    private boolean needsFreshOnResume;
    private boolean pendingUnclaimedEarn;
    /** Daily cap used — stay on this page, no Watch ad button, no AdMob load. */
    private boolean dailyLimit;

    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private final Runnable countdownTick =
            new Runnable() {
                @Override
                public void run() {
                    if (!canKeepTimers()) return;
                    if (countdownLeft <= 1) {
                        countdownLeft = 0;
                        paint();
                        return;
                    }
                    countdownLeft--;
                    paint();
                    uiHandler.postDelayed(this, 1000);
                }
            };
    private final Runnable waitTick =
            new Runnable() {
                @Override
                public void run() {
                    if (!canKeepTimers() || waitSeconds <= 0) {
                        paint();
                        return;
                    }
                    waitSeconds--;
                    if (waitSeconds <= 0 && dailyLimit && !noRewardSlots()) {
                        dailyLimit = false;
                        sessionClosed = false;
                        beginCountdownAndPreload();
                        return;
                    }
                    paint();
                    if (waitSeconds > 0) uiHandler.postDelayed(this, 1000);
                }
            };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rewards_watch_ads);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        setTitle("");
        try {
            MobileAds.initialize(this);
        } catch (Exception ignored) {
        }

        required = Math.max(1, getIntent().getIntExtra(EXTRA_ADS_REQUIRED, 2));
        coins = Math.max(0, getIntent().getIntExtra(EXTRA_COINS, 0));
        cooldownMinutes = Math.max(0, getIntent().getIntExtra(EXTRA_COOLDOWN_MINUTES, 5));
        applyAdsSeenFactor();

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

        TextView watchTitle = findViewById(R.id.watch_ads_title);
        if (watchTitle != null) watchTitle.setText(RewardsLabels.watchAdsTitle(this));
        TextView watchKicker = findViewById(R.id.watch_ads_kicker);
        if (watchKicker != null) watchKicker.setText(RewardsLabels.primaryEarn(this));
        progressView = findViewById(R.id.watch_ads_progress);
        countdownView = findViewById(R.id.watch_ads_countdown);
        hintView = findViewById(R.id.watch_ads_hint);
        waitView = findViewById(R.id.watch_ads_wait);
        nextBtn = findViewById(R.id.watch_ads_next_btn);
        adContainer = findViewById(R.id.watch_ads_container);
        findViewById(R.id.watch_ads_close_btn).setOnClickListener(v -> leaveAndCancelAds());
        nextBtn.setOnClickListener(v -> onWatchTapped());

        getOnBackPressedDispatcher()
                .addCallback(
                        this,
                        new OnBackPressedCallback(true) {
                            @Override
                            public void handleOnBackPressed() {
                                leaveAndCancelAds();
                            }
                        });

        // Re-enter always starts at the countdown. Do not restore a half session
        // that would auto-play a previously loaded ad.
        clearSavedProgress();
        if (getIntent().getBooleanExtra(EXTRA_ADS_CAPPED, false)
                || getIntent().getIntExtra(EXTRA_ADS_REMAINING, -1) == 0) {
            RewardsConfigHelper.setFactorRemaining(this, "ads_seen", 0, required);
        }
        startFreshEntry();
    }

    /** ads_seen Coins = session total, cap/day = ad count. Intent values are fallback. */
    private void applyAdsSeenFactor() {
        int cap = RewardsConfigHelper.factorCapPerDay(this, "ads_seen");
        int factorCoins = RewardsConfigHelper.factorCoinsPer(this, "ads_seen");
        if (cap > 0) required = cap;
        if (factorCoins > 0) coins = factorCoins;
        required = Math.max(1, required);
        coins = Math.max(0, coins);
    }

    /**
     * Even share of the session total. Last ad gets the remainder so
     * 2 ads / 130 coins → 65 + 65, and 3 ads / 100 → 33 + 33 + 34.
     */
    static int coinsForAd(int adIndex, int allowed, int totalCoins) {
        if (adIndex < 1 || allowed < 1 || totalCoins <= 0 || adIndex > allowed) return 0;
        int base = totalCoins / allowed;
        int rem = totalCoins % allowed;
        return adIndex == allowed ? base + rem : base;
    }

    static int coinsAfter(int watchedCount, int allowed, int totalCoins) {
        int sum = 0;
        int n = Math.min(Math.max(0, watchedCount), Math.max(0, allowed));
        for (int i = 1; i <= n; i++) sum += coinsForAd(i, allowed, totalCoins);
        return sum;
    }

    /** Server/poll said the rewards window is not live. Do not start an ad. */
    private boolean periodClosedNow() {
        if (!RewardsConfigHelper.scoringOpen(this)) return true;
        String status = RewardsConfigHelper.programStatus(this);
        return "ended".equals(status) || "upcoming".equals(status);
    }

    private void showPeriodClosed() {
        String msg = RewardsConfigHelper.periodBannerText(this);
        if (TextUtils.isEmpty(msg)) {
            msg = "The current week has ended. Next week’s rewards will start shortly.";
        }
        sessionClosed = true;
        userAskedToShow = false;
        countdownLeft = 0;
        uiHandler.removeCallbacks(countdownTick);
        uiHandler.removeCallbacks(waitTick);
        invalidateLoads();
        if (adContainer != null) adContainer.removeAllViews();
        if (hintView != null) {
            hintView.setText(msg);
            hintView.setVisibility(View.VISIBLE);
        }
        if (waitView != null) {
            waitView.setText(msg);
            waitView.setVisibility(View.VISIBLE);
        }
        if (nextBtn != null) {
            nextBtn.setEnabled(false);
        }
        if (countdownView != null) countdownView.setVisibility(View.GONE);
    }

    private void startFreshEntry() {
        if (periodClosedNow()) {
            showPeriodClosed();
            return;
        }
        watched = 0;
        sessionEarned = 0;
        sessionClosed = false;
        userAskedToShow = false;
        pendingUnclaimedEarn = false;
        rewardedEarnedThisShow = false;
        waitSeconds = 0;
        uiHandler.removeCallbacks(waitTick);
        clearSavedProgress();
        if (adContainer != null) adContainer.removeAllViews();
        if (noRewardSlots()) {
            enterDailyLimit();
            return;
        }
        beginCountdownAndPreload();
        refreshCapBeforeShow();
    }

    /** Reward-ad slots left today are zero. Unknown (-1) is not a block. */
    private boolean noRewardSlots() {
        if (RewardsConfigHelper.isFactorCapped(this, "ads_seen")) return true;
        return RewardsConfigHelper.factorRemaining(this, "ads_seen") == 0;
    }

    /**
     * Stay on this page. Hide the load button and drop any loaded ad so
     * nothing plays here, on Home, or in the feed.
     */
    private void enterDailyLimit() {
        dailyLimit = true;
        sessionClosed = true;
        userAskedToShow = false;
        countdownLeft = 0;
        uiHandler.removeCallbacks(countdownTick);
        invalidateLoads();
        if (nextBtn != null) nextBtn.setVisibility(View.GONE);
        if (countdownView != null) countdownView.setVisibility(View.GONE);
        if (adContainer != null) {
            adContainer.removeAllViews();
            adContainer.setVisibility(View.GONE);
        }
        beginWait(secondsUntilNextIstDay());
    }

    /** Confirm slots before the user can start an ad. Drop a load if the cap is already used. */
    private void refreshCapBeforeShow() {
        if (leaving || isFinishing() || dailyLimit) return;
        APIService api = RestApiClient.getClient().create(APIService.class);
        api.rewards_earnings_status("v3", RewardsAuth.authBody(this))
                .enqueue(
                        new Callback<JsonObject>() {
                            @Override
                            public void onResponse(
                                    Call<JsonObject> call, Response<JsonObject> response) {
                                if (leaving || isFinishing() || isDestroyed() || fullscreenShowing) {
                                    return;
                                }
                                JsonObject b = response.body();
                                if (b == null) return;
                                RewardsConfigHelper.applyFromRewardsPayload(
                                        RewardsWatchAdsActivity.this, b);
                                applyWatchAdsCapFromStatus(b);
                                if (noRewardSlots()) {
                                    enterDailyLimit();
                                } else if (!loading && !hasAdReady() && countdownLeft >= 0 && !dailyLimit) {
                                    preload();
                                }
                            }

                            @Override
                            public void onFailure(Call<JsonObject> call, Throwable t) {
                                if (leaving || isFinishing() || dailyLimit) return;
                                if (noRewardSlots()) {
                                    enterDailyLimit();
                                    return;
                                }
                                int left = RewardsConfigHelper.factorRemaining(
                                        RewardsWatchAdsActivity.this, "ads_seen");
                                if (left > 0 && !loading && !hasAdReady()) preload();
                            }
                        });
    }

    private void applyWatchAdsCapFromStatus(@NonNull JsonObject body) {
        if (!body.has("watch_ads") || !body.get("watch_ads").isJsonObject()) return;
        JsonObject w = body.getAsJsonObject("watch_ads");
        boolean capped =
                w.has("capped")
                        && !w.get("capped").isJsonNull()
                        && w.get("capped").getAsBoolean();
        int remaining = -1;
        if (w.has("ads_remaining") && !w.get("ads_remaining").isJsonNull()) {
            try {
                remaining = w.get("ads_remaining").getAsInt();
            } catch (Exception ignored) {
                remaining = -1;
            }
        }
        if (capped || remaining == 0) {
            int cap = required;
            if (w.has("ads_required") && !w.get("ads_required").isJsonNull()) {
                try {
                    cap = Math.max(1, w.get("ads_required").getAsInt());
                } catch (Exception ignored) {
                }
            }
            RewardsConfigHelper.setFactorRemaining(this, "ads_seen", 0, cap);
        }
    }

    private void beginCountdownAndPreload() {
        if (periodClosedNow()) {
            showPeriodClosed();
            return;
        }
        if (noRewardSlots() || dailyLimit) {
            enterDailyLimit();
            return;
        }
        dailyLimit = false;
        if (nextBtn != null) nextBtn.setVisibility(View.VISIBLE);
        countdownLeft = COUNTDOWN_SEC;
        userAskedToShow = false;
        paint();
        if (cannotScoreMore()) {
            enterDailyLimit();
            return;
        }
        // Do not load here. refreshCapBeforeShow loads only after slots are confirmed.
        uiHandler.removeCallbacks(countdownTick);
        uiHandler.postDelayed(countdownTick, 1000);
    }

    private boolean canKeepTimers() {
        return !leaving && !isFinishing() && !isDestroyed();
    }

    private boolean canPresentAd() {
        if (periodClosedNow()) return false;
        return started
                && !leaving
                && !isFinishing()
                && !isDestroyed()
                && !fullscreenShowing
                && !sessionClosed
                && !dailyLimit
                && !noRewardSlots()
                && countdownLeft <= 0
                && hasWindowFocus();
    }

    /** Daily cap or session length blocks another scored ad. Extra taps must not score. */
    private boolean cannotScoreMore() {
        if (sessionClosed) return true;
        if (watched >= required) return true;
        if (RewardsConfigHelper.isFactorCapped(this, "ads_seen")) return true;
        int left = RewardsConfigHelper.factorRemaining(this, "ads_seen");
        return left == 0;
    }

    private void onWatchTapped() {
        if (periodClosedNow()) {
            showPeriodClosed();
            return;
        }
        if (leaving || isFinishing() || fullscreenShowing || claiming) return;
        if (countdownLeft > 0) return;
        if (pendingUnclaimedEarn) {
            claimCompletedAd();
            return;
        }
        if (cannotScoreMore() || noRewardSlots() || dailyLimit) {
            enterDailyLimit();
            return;
        }
        if (FORMAT_NATIVE.equals(adFormat)) {
            onNativeTap();
            return;
        }
        if (!hasFullscreenReady()) {
            userAskedToShow = true;
            if (!loading) preload();
            paint();
            return;
        }
        userAskedToShow = true;
        showFullscreen();
    }

    private void onNativeTap() {
        if (loadedNative == null) {
            userAskedToShow = true;
            if (!loading) preload();
            paint();
            return;
        }
        if (!userAskedToShow && adContainer != null && adContainer.getChildCount() == 0) {
            userAskedToShow = true;
            showNative(loadedNative);
            userAskedToShow = false;
            paint();
            return;
        }
        NativeAd ad = loadedNative;
        loadedNative = null;
        userAskedToShow = false;
        if (adContainer != null) adContainer.removeAllViews();
        ad.destroy();
        onAdCompleted(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        started = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (fullscreenShowing) return;
        if (needsFreshOnResume && !leaving && !isFinishing()) {
            needsFreshOnResume = false;
            invalidateLoads();
            startFreshEntry();
        }
    }

    @Override
    protected void onStop() {
        // Fullscreen AdMob content stops this activity. Dropping the ad then
        // would not cancel the one already showing, but a late load must not
        // attach to Home after the user leaves.
        if (!fullscreenShowing) {
            started = false;
            userAskedToShow = false;
            needsFreshOnResume = !isFinishing();
            invalidateLoads();
            countdownLeft = 0;
            uiHandler.removeCallbacks(countdownTick);
        } else {
            leaving = isFinishing();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        leaving = true;
        userAskedToShow = false;
        uiHandler.removeCallbacksAndMessages(null);
        invalidateLoads();
        super.onDestroy();
    }

    @Override
    public void finish() {
        leaving = true;
        userAskedToShow = false;
        if (!fullscreenShowing) invalidateLoads();
        super.finish();
    }

    private void leaveAndCancelAds() {
        leaving = true;
        userAskedToShow = false;
        needsFreshOnResume = false;
        uiHandler.removeCallbacksAndMessages(null);
        if (fullscreenShowing) {
            // Let the video close on this page. Finishing now would let AdMob
            // attach the rewarded ad to Home or the feed.
            return;
        }
        invalidateLoads();
        clearSavedProgress();
        finish();
    }

    /**
     * Drop every loaded ad and ignore in-flight load callbacks. RewardedAd has
     * no destroy(); releasing the reference and never calling show() is what
     * stops it from playing on the next screen.
     */
    private void invalidateLoads() {
        loadGen++;
        loading = false;
        if (loadedNative != null) {
            try {
                loadedNative.destroy();
            } catch (Exception ignored) {
            }
            loadedNative = null;
        }
        if (loadedInterstitial != null) {
            try {
                loadedInterstitial.setFullScreenContentCallback(null);
            } catch (Exception ignored) {
            }
            loadedInterstitial = null;
        }
        if (loadedRewarded != null) {
            try {
                loadedRewarded.setFullScreenContentCallback(null);
            } catch (Exception ignored) {
            }
            loadedRewarded = null;
        }
        if (adContainer != null) adContainer.removeAllViews();
    }

    private void clearSavedProgress() {
        try {
            SharedPrefsData.putInt(this, PREF_WATCH_PROGRESS, 0, Constants.PREF_NAME);
            SharedPrefsData.putInt(this, PREF_WATCH_REQUIRED, 0, Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
    }

    private void paint() {
        if (progressView == null) return;
        if (dailyLimit || noRewardSlots()) {
            paintDailyLimit();
            return;
        }
        if (nextBtn != null) nextBtn.setVisibility(View.VISIBLE);
        progressView.setText(watched + " / " + required);

        boolean counting = countdownLeft > 0 && !sessionClosed && !fullscreenShowing;
        if (countdownView != null) {
            countdownView.setVisibility(counting ? View.VISIBLE : View.GONE);
            if (counting) countdownView.setText(String.valueOf(countdownLeft));
        }
        if (waitView != null) {
            if (waitSeconds > 0) {
                waitView.setVisibility(View.VISIBLE);
                waitView.setText(RewardsLabels.watchAdsWait(this, waitSeconds));
            } else {
                waitView.setVisibility(View.GONE);
            }
        }
        if (adContainer != null && isFullscreenFormat()) {
            adContainer.setVisibility(View.GONE);
        }

        if (watched >= required && sessionEarned > 0) {
            hintView.setText("You earned " + sessionEarned + " coins");
        } else if (watched > 0) {
            hintView.setText("You have earned " + sessionEarned + " coins");
        } else if (counting) {
            hintView.setText("Your ad will load in " + countdownLeft + " seconds");
        } else if (cannotScoreMore()) {
            hintView.setText(
                    sessionEarned > 0
                            ? "You have earned " + sessionEarned + " coins"
                            : "Daily ads cap reached");
        } else if (loading) {
            hintView.setText("Your ad is loading. Tap watch when it is ready.");
        } else {
            hintView.setText("Tap watch to start. The ad will not play until you tap.");
        }

        boolean canTap =
                !leaving
                        && !fullscreenShowing
                        && !claiming
                        && countdownLeft <= 0
                        && !cannotScoreMore();
        if (!canTap) {
            nextBtn.setEnabled(false);
            nextBtn.setAlpha(0.45f);
            if (watched > 0 && watched < required) {
                nextBtn.setText("Watch next ad");
            } else if (watched <= 0) {
                nextBtn.setText("Watch ad 1/" + required);
            } else {
                nextBtn.setText("Watch ad " + Math.min(watched, required) + "/" + required);
            }
            return;
        }
        if (pendingUnclaimedEarn) {
            nextBtn.setEnabled(true);
            nextBtn.setAlpha(1f);
            nextBtn.setText(claiming ? "Saving coins…" : "Watch next ad");
            return;
        }
        if (watched <= 0) {
            if (hasAdReady() || FORMAT_NATIVE.equals(adFormat) && loadedNative != null) {
                nextBtn.setEnabled(true);
                nextBtn.setAlpha(1f);
                nextBtn.setText("Watch ad 1/" + required);
            } else {
                nextBtn.setEnabled(true);
                nextBtn.setAlpha(loading ? 0.7f : 1f);
                nextBtn.setText(loading ? "Loading ad…" : "Watch ad 1/" + required);
            }
            return;
        }
        nextBtn.setEnabled(true);
        nextBtn.setAlpha(1f);
        nextBtn.setText(hasAdReady() || !isFullscreenFormat() ? "Watch next ad" : "Watch next ad");
    }

    /** Limit reached — stay on this page, no Watch control, timer until the next slot. */
    private void paintDailyLimit() {
        progressView.setText(RewardsLabels.adsLimitReached(this));
        if (sessionEarned > 0) {
            hintView.setText(
                    RewardsLabels.adsComeBackLater(this) + " You earned " + sessionEarned + " coins.");
        } else {
            hintView.setText(RewardsLabels.adsComeBackLater(this));
        }
        if (countdownView != null) countdownView.setVisibility(View.GONE);
        if (adContainer != null) adContainer.setVisibility(View.GONE);
        if (waitView != null) {
            waitView.setVisibility(View.VISIBLE);
            waitView.setText(RewardsLabels.adsLimitWait(this, Math.max(0, waitSeconds)));
        }
        if (nextBtn != null) {
            nextBtn.setVisibility(View.GONE);
            nextBtn.setEnabled(false);
        }
    }

    private boolean isFullscreenFormat() {
        return FORMAT_INTERSTITIAL.equals(adFormat) || FORMAT_REWARDED.equals(adFormat);
    }

    private boolean hasFullscreenReady() {
        if (FORMAT_INTERSTITIAL.equals(adFormat)) return loadedInterstitial != null;
        if (FORMAT_REWARDED.equals(adFormat)) return loadedRewarded != null;
        return false;
    }

    private boolean hasAdReady() {
        if (FORMAT_NATIVE.equals(adFormat)) return loadedNative != null;
        return hasFullscreenReady();
    }

    private void preload() {
        if (periodClosedNow()) {
            showPeriodClosed();
            return;
        }
        if (leaving || isFinishing() || isDestroyed() || sessionClosed || dailyLimit) return;
        if (noRewardSlots() || cannotScoreMore()) {
            enterDailyLimit();
            return;
        }
        if (RewardsConfigHelper.factorRemaining(this, "ads_seen") < 0
                && !RewardsConfigHelper.isFactorCapped(this, "ads_seen")) {
            return;
        }
        if (loading || hasAdReady()) return;
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
        final int gen = loadGen;
        AdLoader loader =
                new AdLoader.Builder(this, adUnit)
                        .forNativeAd(
                                nativeAd -> {
                                    if (!acceptLoaded(gen)) {
                                        nativeAd.destroy();
                                        return;
                                    }
                                    loading = false;
                                    if (loadedNative != null) loadedNative.destroy();
                                    loadedNative = nativeAd;
                                    // Hold until tap. Do not render into the page.
                                    maybeShowAfterTap();
                                    paint();
                                })
                        .withAdListener(
                                new AdListener() {
                                    @Override
                                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                                        if (gen != loadGen) return;
                                        loading = false;
                                        loadedNative = null;
                                        paint();
                                        if (countdownLeft <= 0 && !leaving) toastLoadFail();
                                    }
                                })
                        .withNativeAdOptions(new NativeAdOptions.Builder().build())
                        .build();
        loader.loadAd(new AdRequest.Builder().build());
    }

    private void preloadInterstitial() {
        final int gen = loadGen;
        InterstitialAd.load(
                this,
                adUnit,
                new AdRequest.Builder().build(),
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd ad) {
                        if (!acceptLoaded(gen)) {
                            try {
                                ad.setFullScreenContentCallback(null);
                            } catch (Exception ignored) {
                            }
                            return;
                        }
                        loading = false;
                        loadedInterstitial = ad;
                        maybeShowAfterTap();
                        paint();
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        if (gen != loadGen) return;
                        loading = false;
                        loadedInterstitial = null;
                        paint();
                        if (countdownLeft <= 0 && !leaving) toastLoadFail();
                    }
                });
    }

    private void preloadRewarded() {
        final int gen = loadGen;
        RewardedAd.load(
                this,
                adUnit,
                new AdRequest.Builder().build(),
                new RewardedAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull RewardedAd ad) {
                        if (!acceptLoaded(gen)) {
                            try {
                                ad.setFullScreenContentCallback(null);
                            } catch (Exception ignored) {
                            }
                            return;
                        }
                        loading = false;
                        loadedRewarded = ad;
                        maybeShowAfterTap();
                        paint();
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        if (gen != loadGen) return;
                        loading = false;
                        loadedRewarded = null;
                        paint();
                        if (countdownLeft <= 0 && !leaving) toastLoadFail();
                    }
                });
    }

    private boolean acceptLoaded(int gen) {
        return gen == loadGen
                && !leaving
                && !isFinishing()
                && !isDestroyed()
                && started
                && !dailyLimit
                && !sessionClosed
                && !noRewardSlots();
    }

    /** Only if the user already tapped Watch. Never auto-play a load. */
    private void maybeShowAfterTap() {
        if (dailyLimit || noRewardSlots()) {
            enterDailyLimit();
            return;
        }
        if (!userAskedToShow || countdownLeft > 0 || !canPresentAd()) return;
        if (FORMAT_NATIVE.equals(adFormat)) {
            if (loadedNative != null) showNative(loadedNative);
            userAskedToShow = false;
            paint();
            return;
        }
        showFullscreen();
    }

    private void toastLoadFail() {
        Toast.makeText(this, "Ad unavailable. Tap watch to try again.", Toast.LENGTH_SHORT).show();
    }

    private void showFullscreen() {
        if (dailyLimit || noRewardSlots() || cannotScoreMore()) {
            enterDailyLimit();
            return;
        }
        if (!userAskedToShow || !canPresentAd() || !hasFullscreenReady()) return;
        userAskedToShow = false;
        if (FORMAT_INTERSTITIAL.equals(adFormat) && loadedInterstitial != null) {
            fullscreenShowing = true;
            InterstitialAd ad = loadedInterstitial;
            loadedInterstitial = null;
            ad.setFullScreenContentCallback(
                    new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            fullscreenShowing = false;
                            if (shouldIgnoreAdResult()) {
                                finishIfLeaving();
                                return;
                            }
                            onAdCompleted(true);
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                            fullscreenShowing = false;
                            userAskedToShow = false;
                            if (shouldIgnoreAdResult()) return;
                            paint();
                            Toast.makeText(
                                            RewardsWatchAdsActivity.this,
                                            "Could not show ad",
                                            Toast.LENGTH_SHORT)
                                    .show();
                        }
                    });
            try {
                ad.show(this);
            } catch (Exception e) {
                fullscreenShowing = false;
                paint();
            }
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
                            if (shouldIgnoreAdResult()) {
                                finishIfLeaving();
                                return;
                            }
                            boolean earned = rewardedEarnedThisShow;
                            rewardedEarnedThisShow = false;
                            if (!earned) {
                                Toast.makeText(
                                                RewardsWatchAdsActivity.this,
                                                "Finish the video to count this ad",
                                                Toast.LENGTH_SHORT)
                                        .show();
                                if (noRewardSlots() || dailyLimit) {
                                    enterDailyLimit();
                                } else {
                                    paint();
                                    if (!cannotScoreMore() && !loading) preload();
                                }
                                return;
                            }
                            onAdCompleted(true);
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                            fullscreenShowing = false;
                            userAskedToShow = false;
                            if (shouldIgnoreAdResult()) return;
                            paint();
                            Toast.makeText(
                                            RewardsWatchAdsActivity.this,
                                            "Could not show ad",
                                            Toast.LENGTH_SHORT)
                                    .show();
                        }
                    });
            try {
                ad.show(
                        this,
                        new OnUserEarnedRewardListener() {
                            @Override
                            public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                                if (leaving || isFinishing() || isDestroyed()) return;
                                rewardedEarnedThisShow = true;
                            }
                        });
            } catch (Exception e) {
                fullscreenShowing = false;
                paint();
            }
        }
    }

    private boolean shouldIgnoreAdResult() {
        return leaving || isFinishing() || isDestroyed();
    }

    private void finishIfLeaving() {
        if (leaving && !isFinishing()) finish();
    }

    private void onAdCompleted(boolean earned) {
        userAskedToShow = false;
        if (!earned || shouldIgnoreAdResult()) {
            paint();
            return;
        }
        if (cannotScoreMore() || noRewardSlots()) {
            enterDailyLimit();
            return;
        }
        pendingUnclaimedEarn = true;
        claimCompletedAd();
    }

    private void showNative(@NonNull NativeAd nativeAd) {
        if (adContainer == null || leaving || isFinishing()) return;
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

    private void claimCompletedAd() {
        if (claiming || leaving || isFinishing()) return;
        if (cannotScoreMore() && !pendingUnclaimedEarn) {
            closeSessionForCap();
            return;
        }
        claiming = true;
        paint();
        final int adIndex = watched + 1;
        JsonObject body = RewardsAuth.authBody(this);
        body.addProperty("ads_watched", 1);
        body.addProperty("per_ad", true);
        body.addProperty("ad_index", adIndex);
        APIService api = RestApiClient.getClient().create(APIService.class);
        api.rewards_watch_ads_claim("v3", body)
                .enqueue(
                        new Callback<JsonObject>() {
                            @Override
                            public void onResponse(
                                    Call<JsonObject> call, Response<JsonObject> response) {
                                claiming = false;
                                if (leaving || isFinishing() || isDestroyed()) return;
                                JsonObject b = response.body();
                                RewardsConfigHelper.applyFromRewardsPayload(
                                        RewardsWatchAdsActivity.this, b);
                                boolean ok =
                                        b != null
                                                && b.has("success")
                                                && b.get("success").getAsBoolean();
                                int awarded = 0;
                                if (ok && b.has("coins_awarded") && !b.get("coins_awarded").isJsonNull()) {
                                    awarded = Math.max(0, b.get("coins_awarded").getAsInt());
                                }
                                if (!ok || awarded <= 0) {
                                    pendingUnclaimedEarn = false;
                                    boolean capped =
                                            b != null
                                                    && b.has("capped")
                                                    && !b.get("capped").isJsonNull()
                                                    && b.get("capped").getAsBoolean();
                                    if (capped
                                            || noRewardSlots()
                                            || RewardsConfigHelper.isFactorCapped(
                                                    RewardsWatchAdsActivity.this, "ads_seen")) {
                                        enterDailyLimit();
                                    } else {
                                        String msg =
                                                b != null && b.has("msg")
                                                        ? b.get("msg").getAsString()
                                                        : "Could not save coins";
                                        Toast.makeText(
                                                        RewardsWatchAdsActivity.this,
                                                        msg,
                                                        Toast.LENGTH_SHORT)
                                                .show();
                                        paint();
                                    }
                                    return;
                                }
                                pendingUnclaimedEarn = false;
                                watched = adIndex;
                                if (b.has("ad_index") && !b.get("ad_index").isJsonNull()) {
                                    watched = Math.max(watched, b.get("ad_index").getAsInt());
                                }
                                if (b.has("ads_required") && !b.get("ads_required").isJsonNull()) {
                                    int serverAllowed = b.get("ads_required").getAsInt();
                                    if (serverAllowed > 0) required = serverAllowed;
                                }
                                if (b.has("session_earned") && !b.get("session_earned").isJsonNull()) {
                                    sessionEarned = Math.max(0, b.get("session_earned").getAsInt());
                                } else {
                                    sessionEarned = coinsAfter(watched, required, coins);
                                }
                                setResult(RESULT_OK);
                                boolean capped =
                                        (b.has("capped")
                                                        && !b.get("capped").isJsonNull()
                                                        && b.get("capped").getAsBoolean())
                                                || cannotScoreMore();
                                if (watched >= required || capped) {
                                    sessionClosed = true;
                                    int wait = 0;
                                    if (b.has("wait_until_sec")
                                            && !b.get("wait_until_sec").isJsonNull()) {
                                        wait = Math.max(0, b.get("wait_until_sec").getAsInt());
                                    }
                                    if (b.has("cooldown_remaining_sec")
                                            && !b.get("cooldown_remaining_sec").isJsonNull()) {
                                        wait =
                                                Math.max(
                                                        wait,
                                                        b.get("cooldown_remaining_sec").getAsInt());
                                    }
                                    if (capped || noRewardSlots()) {
                                        enterDailyLimit();
                                    } else {
                                        if (wait <= 0) wait = Math.max(1, cooldownMinutes * 60);
                                        sessionClosed = true;
                                        userAskedToShow = false;
                                        invalidateLoads();
                                        if (nextBtn != null) nextBtn.setVisibility(View.GONE);
                                        beginWait(wait);
                                    }
                                } else if (noRewardSlots()) {
                                    enterDailyLimit();
                                } else {
                                    paint();
                                    if (!loading && !hasAdReady()) preload();
                                }
                            }

                            @Override
                            public void onFailure(Call<JsonObject> call, Throwable t) {
                                claiming = false;
                                if (leaving || isFinishing()) return;
                                pendingUnclaimedEarn = true;
                                Toast.makeText(
                                                RewardsWatchAdsActivity.this,
                                                "Network error — tap again to save coins",
                                                Toast.LENGTH_SHORT)
                                        .show();
                                paint();
                            }
                        });
    }

    private void closeSessionForCap() {
        enterDailyLimit();
    }

    private void beginWait(int seconds) {
        waitSeconds = Math.max(0, seconds);
        uiHandler.removeCallbacks(waitTick);
        paint();
        if (waitSeconds > 0) uiHandler.postDelayed(waitTick, 1000);
    }

    static int secondsUntilNextIstDay() {
        try {
            TimeZone ist = TimeZone.getTimeZone("Asia/Kolkata");
            Calendar now = Calendar.getInstance(ist);
            Calendar next = Calendar.getInstance(ist);
            next.setTimeInMillis(now.getTimeInMillis());
            next.add(Calendar.DAY_OF_MONTH, 1);
            next.set(Calendar.HOUR_OF_DAY, 0);
            next.set(Calendar.MINUTE, 0);
            next.set(Calendar.SECOND, 0);
            next.set(Calendar.MILLISECOND, 0);
            long diff = next.getTimeInMillis() - now.getTimeInMillis();
            return (int) Math.max(1L, (diff + 999L) / 1000L);
        } catch (Exception e) {
            return 60;
        }
    }
}
