package com.anyNews.anynews.rewards;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.anyNews.anynews.Activities.LoginActivityDetails;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.DistrictPinVisibilityConfig;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.anyNews.anynews.Pojo.UserImagePojoResponse;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Profile → Rewards hub: coins, earn actions, leaderboard, coin history (+N/−N + time).
 * Wallet / UPI / withdraw live on {@link RewardsWalletActivity}.
 */
public class RewardsHubActivity extends AppCompatActivity {

    private TextView coinsView;
    private TextView redeemedLine;
    private TextView rankView;
    private TextView rateView;
    private TextView weekView;
    private TextView periodBanner;
    private TextView subtitleView;
    private TextView rulesView;
    private TextView joinBtn;
    private TextView goWalletBtn;
    private LinearLayout leaderboard;
    private LinearLayout history;
    private TextView checkinBtn;
    private TextView watchAdsBtn;
    private TextView ratingBtn;
    private View earnCard;
    private TextView coinsCaption;
    private TextView earnTitle;
    private TextView leaderboardTitle;
    private TextView leaderboardHint;
    private TextView historyTitle;
    private TextView historyHint;
    private View historyRow;
    private boolean joined;
    private boolean guest;
    private boolean earnBlocked;
    private boolean periodScoringOpen = true;
    private String periodBannerText = "";
    private int historyLimit = 20;
    private String outOfTop10Copy = "";
    private int watchAdsRequired = 3;
    private int watchAdsCoins = 40;
    private String watchAdsUnit = "";
    private String watchAdsFormat = "rewarded";
    private boolean watchPersistProgress = true;
    private int checkinCoins = 25;
    private int ratingCoins = 100;
    private boolean checkinAvailable;
    private boolean watchAdsAvailable;
    private boolean ratingAvailable;
    private int watchCooldownSec;
    private boolean watchAdsCapped;
    private int watchAdsRemaining = -1;
    private int watchCooldownMinutes = 5;
    private boolean pendingScreenshotAfterStore;
    private String leaderboardSubtitle = "";
    private String historyShowingLast = "";
    private final Handler cooldownHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService uploadExecutor = Executors.newSingleThreadExecutor();
    private final Runnable cooldownTick =
            new Runnable() {
                @Override
                public void run() {
                    if (watchCooldownSec > 0) {
                        watchCooldownSec--;
                        paintWatchAdsButton();
                        if (watchCooldownSec > 0) {
                            cooldownHandler.postDelayed(this, 1000);
                        } else {
                            watchAdsAvailable = joined && !guest && periodScoringOpen;
                            paintWatchAdsButton();
                        }
                    }
                }
            };

    private final ActivityResultLauncher<String> pickScreenshot =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null) uploadRatingScreenshot(uri);
                    });

    private final ActivityResultLauncher<Intent> watchAdsLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) loadHub();
                    });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!DistrictPinVisibilityConfig.rewardsMasterEnabled(this)
                || !DistrictPinVisibilityConfig.rewardsVisibleForUser(this)) {
            Toast.makeText(this, "Rewards is currently off", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        setContentView(R.layout.activity_rewards_hub);
        applySystemBars();
        bindViews();
        applyStaticLabels();
        findViewById(R.id.rewards_back).setOnClickListener(v -> finish());
        joinBtn.setOnClickListener(v -> onJoin());
        if (goWalletBtn != null) {
            goWalletBtn.setOnClickListener(v -> openWallet());
        }
        if (checkinBtn != null) checkinBtn.setOnClickListener(v -> onCheckin());
        if (watchAdsBtn != null) watchAdsBtn.setOnClickListener(v -> onWatchAds());
        if (ratingBtn != null) ratingBtn.setOnClickListener(v -> onRating());
        RewardsEngagementBatcher.get().trackActiveDay(this);
        loadHub();
        loadLeaderboard();
    }

    @Override
    protected void onDestroy() {
        cooldownHandler.removeCallbacks(cooldownTick);
        try {
            uploadExecutor.shutdownNow();
        } catch (Exception ignored) {
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            RewardsEngagementBatcher.get().flushNow(this);
        } catch (Exception ignored) {
        }
        if (pendingScreenshotAfterStore) {
            pendingScreenshotAfterStore = false;
            toast("Pick your Play Store rating screenshot");
            pickScreenshot.launch("image/*");
        }
        if (joinBtn != null) {
            loadHub();
            loadLeaderboard();
        }
    }

    private void applySystemBars() {
        Window window = getWindow();
        WindowCompat.setDecorFitsSystemWindows(window, false);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.parseColor("#FF0B1020"));
        window.setNavigationBarColor(Color.parseColor("#FF0B1020"));
        WindowInsetsControllerCompat c =
                WindowCompat.getInsetsController(window, window.getDecorView());
        if (c != null) {
            c.setAppearanceLightStatusBars(false);
            c.setAppearanceLightNavigationBars(false);
        }
        View root = findViewById(R.id.rewards_root);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, bars.top, 0, bars.bottom);
            return insets;
        });
    }

    private void bindViews() {
        coinsView = findViewById(R.id.rewards_coins);
        redeemedLine = findViewById(R.id.rewards_redeemed_line);
        rankView = findViewById(R.id.rewards_rank);
        rateView = findViewById(R.id.rewards_rate);
        weekView = findViewById(R.id.rewards_week);
        periodBanner = findViewById(R.id.rewards_period_banner);
        subtitleView = findViewById(R.id.rewards_subtitle);
        rulesView = findViewById(R.id.rewards_rules);
        joinBtn = findViewById(R.id.rewards_join_btn);
        goWalletBtn = findViewById(R.id.rewards_go_wallet_btn);
        leaderboard = findViewById(R.id.rewards_leaderboard);
        history = findViewById(R.id.rewards_history);
        checkinBtn = findViewById(R.id.rewards_checkin_btn);
        watchAdsBtn = findViewById(R.id.rewards_watch_ads_btn);
        ratingBtn = findViewById(R.id.rewards_rating_btn);
        earnCard = findViewById(R.id.rewards_earn_card);
        coinsCaption = findViewById(R.id.rewards_coins_caption);
        earnTitle = findViewById(R.id.rewards_earn_title);
        leaderboardTitle = findViewById(R.id.rewards_leaderboard_title);
        leaderboardHint = findViewById(R.id.rewards_leaderboard_hint);
        historyTitle = findViewById(R.id.rewards_history_title);
        historyHint = findViewById(R.id.rewards_history_hint);
        historyRow = findViewById(R.id.rewards_history_row);
        if (historyRow != null) {
            historyRow.setOnClickListener(v -> openHistory());
        }
        View watchPanel = findViewById(R.id.rewards_watch_panel);
        if (watchPanel != null && watchAdsBtn != null) {
            watchPanel.setOnClickListener(v -> watchAdsBtn.performClick());
        }
        TextView heroKicker = findViewById(R.id.rewards_hero_kicker);
        if (heroKicker != null) heroKicker.setText(RewardsLabels.memberBalance(this));
        TextView watchKicker = findViewById(R.id.rewards_watch_kicker);
        if (watchKicker != null) watchKicker.setText(RewardsLabels.primaryEarn(this));
    }

    private void applyStaticLabels() {
        if (coinsCaption != null) coinsCaption.setText(RewardsLabels.coinsThisWeek(this));
        if (earnTitle != null) earnTitle.setText(RewardsLabels.alsoEarn(this));
        if (leaderboardTitle != null) leaderboardTitle.setText(RewardsLabels.leaderboard(this));
        if (leaderboardHint != null) {
            leaderboardHint.setText(RewardsLabels.leaderboardHint(this));
            leaderboardHint.setVisibility(View.VISIBLE);
        }
        if (historyTitle != null) historyTitle.setText(RewardsLabels.history(this));
        if (goWalletBtn != null) goWalletBtn.setText(RewardsLabels.goToWallet(this));
    }

    private void openWallet() {
        if (!requireLogin()) return;
        startActivity(new Intent(this, RewardsWalletActivity.class));
    }

    private void openHistory() {
        if (!requireLogin()) return;
        Intent i = new Intent(this, RewardsHistoryActivity.class);
        i.putExtra("history_limit", historyLimit);
        startActivity(i);
    }

    private JsonObject authBody() {
        return RewardsAuth.authBody(this);
    }

    private boolean requireLogin() {
        if (!RewardsAuth.isLoggedIn(this)) {
            Intent login = new Intent(this, LoginActivityDetails.class);
            login.putExtra(LoginActivityDetails.EXTRA_OPEN_REWARDS, true);
            startActivity(login);
            return false;
        }
        return true;
    }

    private void loadHub() {
        APIService api = RestApiClient.getClient().create(APIService.class);
        api.rewards_hub("v3", authBody()).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                JsonObject body = response.body();
                if (body == null || !body.has("success")) {
                    toast("Could not load rewards");
                    return;
                }
                if (body.has("enabled") && !body.get("enabled").getAsBoolean()) {
                    toast("Rewards is currently off");
                    finish();
                    return;
                }
                if (body.has("excluded") && body.get("excluded").getAsBoolean()) {
                    toast("Rewards is not available for reporter accounts");
                    finish();
                    return;
                }
                applyHub(body);
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                toast("Network error");
            }
        });
    }

    private void applyHub(JsonObject body) {
        guest = body.has("guest") && body.get("guest").getAsBoolean();
        joined = body.has("joined") && body.get("joined").getAsBoolean();
        RewardsConfigHelper.setJoined(this, joined && !guest);
        RewardsConfigHelper.applyFromRewardsPayload(this, body);

        JsonObject copy =
                body.has("copy") && body.get("copy").isJsonObject()
                        ? body.getAsJsonObject("copy")
                        : new JsonObject();

        try {
            RewardsConfigHelper.saveChipCopyFromServer(
                    this,
                    optStr(copy, "guest_chip", null),
                    optStr(copy, "joined_chip", null),
                    body.has("rate_label") ? body.get("rate_label").getAsString() : null,
                    optStr(copy, "chip_color", null),
                    copy.has("chip_size_sp") ? copy.get("chip_size_sp").getAsInt() : null,
                    optStr(copy, "chip_weight", null));
        } catch (Exception ignored) {
        }

        String title =
                joined
                        ? optStr(copy, "joined_hub_title", RewardsLabels.joinedHubTitle(this))
                        : optStr(copy, "guest_hub_title", RewardsLabels.guestHubTitle(this));
        if (!joined && "Join AnyNews Rewards".equals(title)) {
            title = RewardsLabels.guestHubTitle(this);
        }
        if (joined && "AnyNews Coins".equals(title)) {
            title = RewardsLabels.joinedHubTitle(this);
        }
        TextView top = findViewById(R.id.rewards_top_title);
        if (top != null) top.setText(title);

        String subtitle =
                joined
                        ? optStr(copy, "joined_hub_subtitle", RewardsLabels.joinedHubSubtitle(this))
                        : optStr(copy, "guest_hub_subtitle", RewardsLabels.guestHubSubtitle(this));
        if (!joined
                && subtitle != null
                && subtitle.startsWith("Login and keep reading")) {
            subtitle = RewardsLabels.guestHubSubtitle(this);
        }
        if (joined
                && subtitle != null
                && subtitle.startsWith("Earn coins by reading")) {
            subtitle = RewardsLabels.joinedHubSubtitle(this);
        }
        subtitleView.setText(subtitle);

        int coins = body.has("coins") ? body.get("coins").getAsInt() : 0;
        int earned =
                body.has("earned_coins") ? body.get("earned_coins").getAsInt() : coins;
        int converted =
                body.has("converted_coins") ? body.get("converted_coins").getAsInt() : 0;
        // Hub balance = spendable; show gross in rank context via redeemed line
        coinsView.setText(String.valueOf(coins));
        if (redeemedLine != null) {
            if (converted > 0) {
                redeemedLine.setVisibility(View.VISIBLE);
                redeemedLine.setText(RewardsLabels.redeemedThisWeek(this, converted));
            } else {
                redeemedLine.setVisibility(View.GONE);
            }
        }
        if (coinsCaption != null) coinsCaption.setText(RewardsLabels.coinsThisWeek(this));

        leaderboardSubtitle =
                optStr(copy, "leaderboard_subtitle", RewardsLabels.leaderboardHint(this));
        historyShowingLast =
                optStr(copy, "history_showing_last", RewardsLabels.historyShowingLast(this));
        outOfTop10Copy =
                optStr(copy, "out_of_top10", RewardsLabels.outOfTop10(this));
        if (body.has("out_of_top10_copy") && !body.get("out_of_top10_copy").isJsonNull()) {
            String s = body.get("out_of_top10_copy").getAsString();
            if (!TextUtils.isEmpty(s)) outOfTop10Copy = s;
        }
        if (body.has("history_limit") && !body.get("history_limit").isJsonNull()) {
            historyLimit = Math.max(5, body.get("history_limit").getAsInt());
        }
        earnBlocked = body.has("earn_blocked") && body.get("earn_blocked").getAsBoolean();
        if (leaderboardHint != null) {
            leaderboardHint.setText(leaderboardSubtitle);
            leaderboardHint.setVisibility(View.VISIBLE);
        }

        String rankDisplay =
                body.has("rank_display") ? optStr(body, "rank_display", "") : "";
        if ("top10".equals(rankDisplay)
                && body.has("rank")
                && !body.get("rank").isJsonNull()) {
            rankView.setText(RewardsLabels.rankPrefix(this, body.get("rank").getAsInt()));
        } else if (joined && !guest) {
            rankView.setText(
                    !TextUtils.isEmpty(outOfTop10Copy)
                            ? outOfTop10Copy
                            : RewardsLabels.outOfTop10(this));
        } else {
            rankView.setText(RewardsLabels.loginToRank(this));
        }

        // Conversion rate only on Wallet — keep hidden on hub.
        if (rateView != null) {
            rateView.setVisibility(View.GONE);
            rateView.setText("");
        }

        if (body.has("week") && body.get("week").isJsonObject()) {
            JsonObject w = body.getAsJsonObject("week");
            String label = optStr(w, "period_label", "");
            if (TextUtils.isEmpty(label)) {
                // Fallback for older servers
                label = optStr(w, "week_key", "");
            }
            weekView.setText(label != null ? label : "");
        }
        applyPeriodBanner(body);

        double walletRupees =
                body.has("wallet_rupees") ? body.get("wallet_rupees").getAsDouble() : 0;
        String rules = optStr(copy, "rules_summary", "");
        if (TextUtils.isEmpty(rules)
                || rules.startsWith("Earn AnyNews Coins for quality reading")) {
            rules = RewardsLabels.rulesSummary(this);
        }
        rulesView.setText(rules);

        try {
            SharedPrefsData.putString(
                    this, "rewards_wallet_rupees", String.valueOf(walletRupees), Constants.PREF_NAME);
            SharedPrefsData.putString(
                    this, "rewards_period_coins", String.valueOf(coins), Constants.PREF_NAME);
            SharedPrefsData.putString(
                    this, "rewards_earned_coins", String.valueOf(earned), Constants.PREF_NAME);
        } catch (Exception ignored) {
        }

        if (guest || !joined) {
            joinBtn.setVisibility(View.VISIBLE);
            joinBtn.setText(
                    guest ? RewardsLabels.loginToJoin(this) : RewardsLabels.joinRewards(this));
        } else {
            joinBtn.setVisibility(View.GONE);
        }
        if (goWalletBtn != null) {
            goWalletBtn.setVisibility(View.VISIBLE);
            goWalletBtn.setText(RewardsLabels.goToWallet(this));
        }

        applyEarnState(body);
        if (!periodScoringOpen) {
            checkinAvailable = false;
            watchAdsAvailable = false;
            ratingAvailable = false;
            if (checkinBtn != null) {
                checkinBtn.setEnabled(false);
                checkinBtn.setAlpha(0.45f);
            }
            if (ratingBtn != null) {
                ratingBtn.setEnabled(false);
                ratingBtn.setAlpha(0.45f);
            }
            paintWatchAdsButton();
        }
        if (earnBlocked && earnCard != null) {
            // Soft-disable earn actions; wallet still reachable
            if (checkinBtn != null) {
                checkinBtn.setEnabled(false);
                checkinBtn.setAlpha(0.45f);
            }
            if (watchAdsBtn != null) {
                watchAdsBtn.setEnabled(false);
                watchAdsBtn.setAlpha(0.45f);
            }
            if (ratingBtn != null) {
                ratingBtn.setEnabled(false);
                ratingBtn.setAlpha(0.45f);
            }
        }
        animateHeroIn();
    }

    private void animateHeroIn() {
        View hero = findViewById(R.id.rewards_hero_card);
        if (hero != null) {
            hero.setAlpha(0.92f);
            hero.setTranslationY(10f);
            hero.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(380)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }
        if (coinsView != null) {
            coinsView.setAlpha(0f);
            coinsView.setScaleX(0.94f);
            coinsView.setScaleY(0.94f);
            coinsView
                    .animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(420)
                    .setStartDelay(60)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }
        if (earnCard != null) {
            earnCard.setAlpha(0.85f);
            earnCard.setTranslationY(16f);
            earnCard
                    .animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(360)
                    .setStartDelay(90)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }
        if (historyRow != null) {
            historyRow.setAlpha(0.8f);
            historyRow
                    .animate()
                    .alpha(1f)
                    .setDuration(300)
                    .setStartDelay(140)
                    .start();
        }
    }

    private void applyPeriodBanner(JsonObject body) {
        RewardsConfigHelper.applyProgramWindow(this, body);
        String status = optStr(body, "program_status", "");
        if (body.has("program") && body.get("program").isJsonObject()) {
            JsonObject program = body.getAsJsonObject("program");
            if (TextUtils.isEmpty(status)) status = optStr(program, "status", "");
        }
        if (body.has("scoring_open") && !body.get("scoring_open").isJsonNull()) {
            periodScoringOpen = body.get("scoring_open").getAsBoolean();
        } else if (!TextUtils.isEmpty(status)) {
            periodScoringOpen = "live".equalsIgnoreCase(status);
        } else {
            periodScoringOpen = RewardsConfigHelper.scoringOpen(this);
        }
        periodBannerText = optStr(body, "period_banner", "");
        if (TextUtils.isEmpty(periodBannerText)) {
            periodBannerText = RewardsConfigHelper.periodBannerText(this);
        }
        if (periodBanner != null) {
            if (!periodScoringOpen && !TextUtils.isEmpty(periodBannerText)) {
                periodBanner.setText(periodBannerText);
                periodBanner.setVisibility(View.VISIBLE);
            } else {
                periodBanner.setVisibility(View.GONE);
            }
        }
        if (!periodScoringOpen) {
            checkinAvailable = false;
            watchAdsAvailable = false;
            ratingAvailable = false;
        }
    }

    private void applyEarnState(JsonObject body) {
        if (body.has("checkin") && body.get("checkin").isJsonObject()) {
            JsonObject c = body.getAsJsonObject("checkin");
            checkinAvailable = c.has("available") && c.get("available").getAsBoolean();
            checkinCoins = c.has("coins") ? c.get("coins").getAsInt() : checkinCoins;
            if (checkinBtn != null) {
                boolean already = c.has("already_today") && c.get("already_today").getAsBoolean();
                checkinBtn.setText(
                        already
                                ? RewardsLabels.checkedInToday(this)
                                : RewardsLabels.dailyCheckin(this, checkinCoins));
                checkinBtn.setEnabled(checkinAvailable && joined && !guest);
                checkinBtn.setAlpha(checkinBtn.isEnabled() ? 1f : 0.45f);
            }
        }
        if (body.has("watch_ads") && body.get("watch_ads").isJsonObject()) {
            JsonObject w = body.getAsJsonObject("watch_ads");
            watchAdsAvailable = w.has("available") && w.get("available").getAsBoolean();
            watchAdsRequired =
                    w.has("ads_required") ? w.get("ads_required").getAsInt() : watchAdsRequired;
            watchAdsCoins = w.has("coins") ? w.get("coins").getAsInt() : watchAdsCoins;
            watchCooldownSec =
                    w.has("cooldown_remaining_sec")
                            ? w.get("cooldown_remaining_sec").getAsInt()
                            : 0;
            if (w.has("cooldown_minutes") && !w.get("cooldown_minutes").isJsonNull()) {
                watchCooldownMinutes = Math.max(0, w.get("cooldown_minutes").getAsInt());
            }
            if (w.has("persist_progress") && !w.get("persist_progress").isJsonNull()) {
                watchPersistProgress = w.get("persist_progress").getAsBoolean();
            }
            if (w.has("ad_unit_id") && !w.get("ad_unit_id").isJsonNull()) {
                watchAdsUnit = w.get("ad_unit_id").getAsString();
            }
            watchAdsCapped =
                    w.has("capped") && !w.get("capped").isJsonNull() && w.get("capped").getAsBoolean();
            if (w.has("ads_remaining") && !w.get("ads_remaining").isJsonNull()) {
                try {
                    watchAdsRemaining = w.get("ads_remaining").getAsInt();
                } catch (Exception ignored) {
                    watchAdsRemaining = -1;
                }
            } else {
                watchAdsRemaining = RewardsConfigHelper.factorRemaining(this, "ads_seen");
            }
            if (!watchAdsCapped && watchAdsRemaining == 0) watchAdsCapped = true;
            if (w.has("ad_format") && !w.get("ad_format").isJsonNull()) {
                String fmt = w.get("ad_format").getAsString();
                if (!TextUtils.isEmpty(fmt)) {
                    watchAdsFormat = fmt.trim().toLowerCase(Locale.US);
                }
            }
            cooldownHandler.removeCallbacks(cooldownTick);
            paintWatchAdsButton();
            if (watchCooldownSec > 0) {
                cooldownHandler.postDelayed(cooldownTick, 1000);
            }
        }
        if (body.has("play_rating") && body.get("play_rating").isJsonObject()) {
            JsonObject p = body.getAsJsonObject("play_rating");
            ratingAvailable = p.has("available") && p.get("available").getAsBoolean();
            ratingCoins = p.has("coins") ? p.get("coins").getAsInt() : ratingCoins;
            boolean pending = p.has("pending") && p.get("pending").getAsBoolean();
            if (ratingBtn != null) {
                ratingBtn.setText(
                        pending
                                ? RewardsLabels.ratingPending(this)
                                : RewardsLabels.ratingAction(this, ratingCoins));
                ratingBtn.setEnabled(ratingAvailable && joined && !guest);
                ratingBtn.setAlpha(ratingBtn.isEnabled() ? 1f : 0.45f);
            }
        }
        if (earnCard != null) earnCard.setVisibility(View.VISIBLE);
        if (earnTitle != null) earnTitle.setText(RewardsLabels.alsoEarn(this));
    }

    private void paintWatchAdsButton() {
        if (watchAdsBtn == null) return;
        if (!periodScoringOpen) {
            watchAdsBtn.setEnabled(false);
            watchAdsBtn.setAlpha(0.45f);
            View panel = findViewById(R.id.rewards_watch_panel);
            if (panel != null) panel.setAlpha(0.55f);
            return;
        }
        if (watchAdsCapped || watchAdsRemaining == 0) {
            // Opens the watch-ad page in the limit state. Does not load an ad.
            watchAdsBtn.setText(RewardsLabels.adsLimitReached(this));
            watchAdsBtn.setEnabled(joined && !guest);
            watchAdsBtn.setAlpha(1f);
        } else if (watchCooldownSec > 0) {
            watchAdsBtn.setText(RewardsLabels.watchAdsWait(this, watchCooldownSec));
            watchAdsBtn.setEnabled(false);
            watchAdsBtn.setAlpha(0.45f);
        } else {
            int hubAds = watchAdsRequired;
            int hubCoins = watchAdsCoins;
            int cap = RewardsConfigHelper.factorCapPerDay(this, "ads_seen");
            int factorCoins = RewardsConfigHelper.factorCoinsPer(this, "ads_seen");
            if (cap > 0) hubAds = cap;
            if (factorCoins > 0) hubCoins = factorCoins;
            watchAdsBtn.setText(RewardsLabels.watchAds(this, hubAds, hubCoins));
            watchAdsBtn.setEnabled(watchAdsAvailable && joined && !guest);
            watchAdsBtn.setAlpha(watchAdsBtn.isEnabled() ? 1f : 0.45f);
        }
        View panel = findViewById(R.id.rewards_watch_panel);
        if (panel != null) panel.setAlpha(watchAdsBtn.isEnabled() ? 1f : 0.55f);
    }

    private void onCheckin() {
        if (!periodScoringOpen) {
            toast(
                    !TextUtils.isEmpty(periodBannerText)
                            ? periodBannerText
                            : RewardsConfigHelper.periodBannerText(this));
            return;
        }
        if (!requireLogin()) return;
        if (!joined) {
            toast(RewardsLabels.joinFirst(this));
            return;
        }
        APIService api = RestApiClient.getClient().create(APIService.class);
        api.rewards_checkin("v3", authBody())
                .enqueue(
                        new Callback<JsonObject>() {
                            @Override
                            public void onResponse(
                                    Call<JsonObject> call, Response<JsonObject> response) {
                                JsonObject b = response.body();
                                if (b != null
                                        && b.has("success")
                                        && b.get("success").getAsBoolean()) {
                                    int c =
                                            b.has("coins_awarded")
                                                    ? b.get("coins_awarded").getAsInt()
                                                    : checkinCoins;
                                    toast("+" + c + " coins");
                                    loadHub();
                                } else {
                                    toast(
                                            b != null && b.has("msg")
                                                    ? b.get("msg").getAsString()
                                                    : "Check-in failed");
                                }
                            }

                            @Override
                            public void onFailure(Call<JsonObject> call, Throwable t) {
                                toast("Network error");
                            }
                        });
    }

    private void onWatchAds() {
        if (!periodScoringOpen || !RewardsConfigHelper.scoringOpen(this)) {
            String msg =
                    !TextUtils.isEmpty(periodBannerText)
                            ? periodBannerText
                            : RewardsConfigHelper.periodBannerText(this);
            toast(msg);
            return;
        }
        if (!requireLogin()) return;
        if (!joined) {
            toast(RewardsLabels.joinFirst(this));
            return;
        }
        int adsRequired = watchAdsRequired;
        int adsCoins = watchAdsCoins;
        int adsCap = RewardsConfigHelper.factorCapPerDay(this, "ads_seen");
        int adsCoinsTotal = RewardsConfigHelper.factorCoinsPer(this, "ads_seen");
        if (adsCap > 0) adsRequired = adsCap;
        if (adsCoinsTotal > 0) adsCoins = adsCoinsTotal;
        Intent intent = new Intent(this, RewardsWatchAdsActivity.class);
        intent.putExtra(RewardsWatchAdsActivity.EXTRA_ADS_REQUIRED, adsRequired);
        intent.putExtra(RewardsWatchAdsActivity.EXTRA_COINS, adsCoins);
        intent.putExtra(RewardsWatchAdsActivity.EXTRA_AD_FORMAT, watchAdsFormat);
        intent.putExtra(RewardsWatchAdsActivity.EXTRA_COOLDOWN_MINUTES, watchCooldownMinutes);
        intent.putExtra(RewardsWatchAdsActivity.EXTRA_PERSIST_PROGRESS, false);
        intent.putExtra(RewardsWatchAdsActivity.EXTRA_ADS_CAPPED, watchAdsCapped);
        intent.putExtra(RewardsWatchAdsActivity.EXTRA_ADS_REMAINING, watchAdsRemaining);
        if (!TextUtils.isEmpty(watchAdsUnit)) {
            intent.putExtra(RewardsWatchAdsActivity.EXTRA_AD_UNIT, watchAdsUnit);
        }
        watchAdsLauncher.launch(intent);
    }

    private void onRating() {
        if (!requireLogin()) return;
        if (!joined) {
            toast(RewardsLabels.joinFirst(this));
            return;
        }
        try {
            String pkg = getPackageName();
            if (TextUtils.isEmpty(pkg)) pkg = "com.anyNews.anynews";
            Intent market =
                    new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + pkg));
            market.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                startActivity(market);
            } catch (Exception e) {
                startActivity(
                        new Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(
                                        "https://play.google.com/store/apps/details?id="
                                                + pkg)));
            }
            pendingScreenshotAfterStore = true;
            toast("Rate 5★ on Play Store, then return here");
        } catch (Exception ignored) {
            toast("Could not open Play Store");
        }
    }

    private void uploadRatingScreenshot(Uri uri) {
        // Optimistic UI — don't block on spinner forever
        if (ratingBtn != null) {
            ratingBtn.setEnabled(false);
            ratingBtn.setAlpha(0.45f);
            ratingBtn.setText(RewardsLabels.ratingSubmitting(this));
        }
        toast(RewardsLabels.ratingSubmitting(this));
        uploadExecutor.execute(
                () -> {
                    try {
                        byte[] bytes = compressScreenshot(uri);
                        if (bytes == null || bytes.length == 0) {
                            runOnUiThread(
                                    () -> {
                                        toast("Could not read image");
                                        loadHub();
                                    });
                            return;
                        }
                        RequestBody req =
                                RequestBody.create(MediaType.parse("image/jpeg"), bytes);
                        MultipartBody.Part part =
                                MultipartBody.Part.createFormData(
                                        "file", "rewards_rating.jpg", req);
                        APIService api = RestApiClient.getClient().create(APIService.class);
                        api.uploadImage(part)
                                .enqueue(
                                        new Callback<UserImagePojoResponse>() {
                                            @Override
                                            public void onResponse(
                                                    Call<UserImagePojoResponse> call,
                                                    Response<UserImagePojoResponse> response) {
                                                try {
                                                    String url = null;
                                                    if (response.body() != null
                                                            && response.body().getExtras()
                                                                    != null) {
                                                        UserImagePojoResponse.Extras ex =
                                                                response.body().getExtras();
                                                        url =
                                                                firstNonEmpty(
                                                                        ex.getImageOriginal(),
                                                                        ex.getImage900(),
                                                                        ex.getImage550(),
                                                                        ex.getImage250());
                                                    }
                                                    if (TextUtils.isEmpty(url)) {
                                                        toast("Upload failed");
                                                        loadHub();
                                                        return;
                                                    }
                                                    submitRatingUrl(url);
                                                } catch (Exception e) {
                                                    toast("Upload failed");
                                                    loadHub();
                                                }
                                            }

                                            @Override
                                            public void onFailure(
                                                    Call<UserImagePojoResponse> call,
                                                    Throwable t) {
                                                toast("Upload network error");
                                                loadHub();
                                            }
                                        });
                    } catch (Exception e) {
                        runOnUiThread(
                                () -> {
                                    toast("Could not read image");
                                    loadHub();
                                });
                    }
                });
    }

    @Nullable
    private byte[] compressScreenshot(Uri uri) {
        try {
            InputStream in = getContentResolver().openInputStream(uri);
            if (in == null) return null;
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, bounds);
            in.close();
            int sample = 1;
            int maxSide = Math.max(bounds.outWidth, bounds.outHeight);
            while (maxSide / sample > 1600) sample *= 2;
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = Math.max(1, sample);
            in = getContentResolver().openInputStream(uri);
            if (in == null) return null;
            Bitmap bmp = BitmapFactory.decodeStream(in, null, opts);
            in.close();
            if (bmp == null) {
                // Fallback raw copy
                in = getContentResolver().openInputStream(uri);
                if (in == null) return null;
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) >= 0) bos.write(buf, 0, n);
                in.close();
                return bos.toByteArray();
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 82, bos);
            bmp.recycle();
            return bos.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }

    private void submitRatingUrl(String url) {
        JsonObject body = authBody();
        body.addProperty("screenshot_url", url);
        APIService api = RestApiClient.getClient().create(APIService.class);
        api.rewards_rating_submit("v3", body)
                .enqueue(
                        new Callback<JsonObject>() {
                            @Override
                            public void onResponse(
                                    Call<JsonObject> call, Response<JsonObject> response) {
                                JsonObject b = response.body();
                                if (b != null
                                        && b.has("success")
                                        && b.get("success").getAsBoolean()) {
                                    toast(RewardsLabels.ratingSubmitting(RewardsHubActivity.this));
                                    loadHub();
                                } else {
                                    toast(
                                            b != null && b.has("msg")
                                                    ? b.get("msg").getAsString()
                                                    : "Submit failed");
                                }
                            }

                            @Override
                            public void onFailure(Call<JsonObject> call, Throwable t) {
                                toast("Network error");
                            }
                        });
    }

    /** User history: only +N / −N and time — no task/factor names. Last 10. */
    private void renderHistory(JsonObject body) {
        history.removeAllViews();
        List<HistoryRow> rows = new ArrayList<>();
        collectCoinRows(body, rows);
        collectMoneyRows(body, "conversions", true, rows);
        collectMoneyRows(body, "withdrawals", false, rows);
        Collections.sort(
                rows,
                new Comparator<HistoryRow>() {
                    @Override
                    public int compare(HistoryRow a, HistoryRow b) {
                        return Long.compare(b.atMs, a.atMs);
                    }
                });
        int n = Math.min(10, rows.size());
        for (int i = 0; i < n; i++) {
            HistoryRow r = rows.get(i);
            TextView line = new TextView(this);
            line.setText(r.text);
            line.setTextColor(Color.parseColor("#FFF4EDE0"));
            line.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            line.setPadding(12, 8, 12, 8);
            history.addView(line);
        }
        if (historyHint != null) {
            if (n >= 10) {
                String hint =
                        !TextUtils.isEmpty(historyShowingLast)
                                ? historyShowingLast
                                : RewardsLabels.historyShowingLast(this);
                historyHint.setText(hint);
                historyHint.setVisibility(View.VISIBLE);
            } else {
                historyHint.setVisibility(View.GONE);
            }
        }
        if (history.getChildCount() == 0) {
            TextView empty = new TextView(this);
            empty.setText(RewardsLabels.noHistory(this));
            empty.setTextColor(Color.parseColor("#FFA89880"));
            empty.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            empty.setPadding(12, 16, 12, 16);
            history.addView(empty);
        }
    }

    private static final class HistoryRow {
        final String text;
        final long atMs;

        HistoryRow(String text, long atMs) {
            this.text = text;
            this.atMs = atMs;
        }
    }

    private void collectCoinRows(JsonObject body, List<HistoryRow> out) {
        if (!body.has("coin_history") || !body.get("coin_history").isJsonArray()) return;
        JsonArray arr = body.getAsJsonArray("coin_history");
        for (int i = 0; i < arr.size(); i++) {
            JsonObject row = arr.get(i).getAsJsonObject();
            int coins = optInt(row, "coins");
            if (coins == 0) continue;
            String sign = coins > 0 ? "+" : "";
            String when = formatWhen(row);
            out.add(
                    new HistoryRow(
                            sign + coins + (TextUtils.isEmpty(when) ? "" : "  ·  " + when),
                            parseWhenMs(row)));
        }
    }

    private void collectMoneyRows(
            JsonObject body, String key, boolean credit, List<HistoryRow> out) {
        if (!body.has(key) || !body.get(key).isJsonArray()) return;
        JsonArray arr = body.getAsJsonArray(key);
        for (int i = 0; i < arr.size(); i++) {
            JsonObject row = arr.get(i).getAsJsonObject();
            double amt =
                    credit ? optDouble(row, "rupees") : optDouble(row, "amount_rupees");
            if (amt <= 0) continue;
            String sign = credit ? "+" : "−";
            String when = formatWhen(row);
            out.add(
                    new HistoryRow(
                            sign
                                    + "₹"
                                    + formatMoney(amt)
                                    + (TextUtils.isEmpty(when) ? "" : "  ·  " + when),
                            parseWhenMs(row)));
        }
    }

    private long parseWhenMs(JsonObject row) {
        String raw = optStr(row, "updatedAt", null);
        if (TextUtils.isEmpty(raw)) raw = optStr(row, "createdAt", null);
        if (TextUtils.isEmpty(raw)) return 0L;
        try {
            String iso = raw.trim();
            if (iso.endsWith("Z")) {
                SimpleDateFormat in =
                        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                in.setTimeZone(TimeZone.getTimeZone("UTC"));
                int dot = iso.indexOf('.');
                String trunc = iso;
                if (dot > 0) {
                    String frac = iso.substring(dot + 1).replace("Z", "");
                    if (frac.length() < 3) {
                        while (frac.length() < 3) frac += "0";
                    } else if (frac.length() > 3) {
                        frac = frac.substring(0, 3);
                    }
                    trunc = iso.substring(0, dot + 1) + frac + "Z";
                } else {
                    in = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
                    in.setTimeZone(TimeZone.getTimeZone("UTC"));
                }
                Date d = in.parse(trunc);
                return d != null ? d.getTime() : 0L;
            }
        } catch (Exception ignored) {
        }
        return 0L;
    }

    private void loadLeaderboard() {
        APIService api = RestApiClient.getClient().create(APIService.class);
        JsonObject body = authBody();
        body.addProperty("limit", 10);
        api.rewards_leaderboard("v3", body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                JsonObject resp = response.body();
                leaderboard.removeAllViews();
                if (resp == null || !resp.has("rows") || !resp.get("rows").isJsonArray()) {
                    TextView empty = new TextView(RewardsHubActivity.this);
                    empty.setText("Leaderboard empty");
                    empty.setPadding(16, 16, 16, 16);
                    empty.setTextColor(Color.parseColor("#FFA89880"));
                    leaderboard.addView(empty);
                    return;
                }
                JsonArray rows = resp.getAsJsonArray("rows");
                int n = Math.min(10, rows.size());
                for (int i = 0; i < n; i++) {
                    JsonObject r = rows.get(i).getAsJsonObject();
                    LinearLayout row = new LinearLayout(RewardsHubActivity.this);
                    row.setOrientation(LinearLayout.HORIZONTAL);
                    row.setPadding(dp(20), dp(16), dp(20), dp(16));
                    row.setAlpha(0f);
                    TextView rank = new TextView(RewardsHubActivity.this);
                    rank.setText("#" + optInt(r, "rank"));
                    rank.setTextColor(Color.parseColor("#FFE4C27A"));
                    rank.setTypeface(rank.getTypeface(), android.graphics.Typeface.BOLD);
                    rank.setMinWidth(dp(42));
                    TextView name = new TextView(RewardsHubActivity.this);
                    name.setText(optStr(r, "name", "Reader"));
                    name.setTextColor(Color.parseColor("#FFF4EDE0"));
                    name.setLayoutParams(
                            new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                    TextView coins = new TextView(RewardsHubActivity.this);
                    coins.setText(optInt(r, "coins") + " coins");
                    coins.setTextColor(Color.parseColor("#FFA89880"));
                    row.addView(rank);
                    row.addView(name);
                    row.addView(coins);
                    leaderboard.addView(row);
                    row.animate()
                            .alpha(1f)
                            .setStartDelay(i * 40L)
                            .setDuration(220)
                            .setInterpolator(new DecelerateInterpolator())
                            .start();
                    if (i < n - 1) {
                        View divider = new View(RewardsHubActivity.this);
                        divider.setBackgroundColor(Color.parseColor("#FF243049"));
                        divider.setLayoutParams(
                                new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT, 1));
                        leaderboard.addView(divider);
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                /* soft */
            }
        });
    }

    private void onJoin() {
        if (!requireLogin()) return;
        APIService api = RestApiClient.getClient().create(APIService.class);
        api.rewards_join("v3", authBody()).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                JsonObject b = response.body();
                if (b != null && b.has("success") && b.get("success").getAsBoolean()) {
                    toast("You're in!");
                    RewardsConfigHelper.setJoined(RewardsHubActivity.this, true);
                    RewardsEngagementBatcher.get().trackActiveDay(RewardsHubActivity.this);
                    loadHub();
                } else {
                    toast(b != null && b.has("msg") ? b.get("msg").getAsString() : "Join failed");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                toast("Network error");
            }
        });
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private static String optStr(JsonObject o, String k, String fb) {
        try {
            if (o != null && o.has(k) && !o.get(k).isJsonNull()) return o.get(k).getAsString();
        } catch (Exception ignored) {
        }
        return fb;
    }

    private static int optInt(JsonObject o, String k) {
        try {
            if (o != null && o.has(k) && !o.get(k).isJsonNull()) return o.get(k).getAsInt();
        } catch (Exception ignored) {
        }
        return 0;
    }

    private static double optDouble(JsonObject o, String k) {
        try {
            if (o != null && o.has(k) && !o.get(k).isJsonNull()) return o.get(k).getAsDouble();
        } catch (Exception ignored) {
        }
        return 0;
    }

    private static String formatMoney(double v) {
        if (Math.abs(v - Math.rint(v)) < 0.001) return String.valueOf((long) Math.rint(v));
        return String.format(Locale.US, "%.2f", v);
    }

    private static String formatWhen(JsonObject row) {
        String raw = optStr(row, "updatedAt", null);
        if (TextUtils.isEmpty(raw)) raw = optStr(row, "createdAt", null);
        if (TextUtils.isEmpty(raw)) return optStr(row, "day_key", "");
        try {
            String iso = raw.trim();
            if (iso.endsWith("Z")) {
                SimpleDateFormat in =
                        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                in.setTimeZone(TimeZone.getTimeZone("UTC"));
                String trunc = iso;
                int dot = iso.indexOf('.');
                if (dot > 0) {
                    String frac = iso.substring(dot + 1).replace("Z", "");
                    if (frac.length() < 3) {
                        while (frac.length() < 3) frac += "0";
                    } else if (frac.length() > 3) {
                        frac = frac.substring(0, 3);
                    }
                    trunc = iso.substring(0, dot + 1) + frac + "Z";
                } else {
                    in = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
                    in.setTimeZone(TimeZone.getTimeZone("UTC"));
                }
                Date d = in.parse(trunc);
                if (d != null) {
                    return new SimpleDateFormat("dd MMM, h:mm a", Locale.getDefault()).format(d);
                }
            }
        } catch (Exception ignored) {
        }
        if (raw.length() >= 16) return raw.substring(0, 16).replace('T', ' ');
        return raw;
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    private static String firstNonEmpty(String... vals) {
        if (vals == null) return null;
        for (String v : vals) {
            if (!TextUtils.isEmpty(v) && !"null".equalsIgnoreCase(v)) return v;
        }
        return null;
    }
}
