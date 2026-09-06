package com.anyNews.anynews.rewards;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
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
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

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
    private TextView rankView;
    private TextView rateView;
    private TextView weekView;
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
    private boolean joined;
    private boolean guest;
    private int watchAdsRequired = 3;
    private int watchAdsCoins = 40;
    private String watchAdsUnit = "";
    private String watchAdsFormat = "native";
    private int checkinCoins = 25;
    private int ratingCoins = 100;
    private boolean checkinAvailable;
    private boolean watchAdsAvailable;
    private boolean ratingAvailable;
    private int watchCooldownSec;
    private boolean pendingScreenshotAfterStore;

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
        window.setStatusBarColor(Color.parseColor("#FFF8F4EC"));
        WindowInsetsControllerCompat c =
                WindowCompat.getInsetsController(window, window.getDecorView());
        if (c != null) c.setAppearanceLightStatusBars(true);
        View root = findViewById(R.id.rewards_root);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, bars.top, 0, bars.bottom);
            return insets;
        });
    }

    private void bindViews() {
        coinsView = findViewById(R.id.rewards_coins);
        rankView = findViewById(R.id.rewards_rank);
        rateView = findViewById(R.id.rewards_rate);
        weekView = findViewById(R.id.rewards_week);
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
    }

    private void applyStaticLabels() {
        if (coinsCaption != null) coinsCaption.setText(RewardsLabels.coinsThisWeek(this));
        if (earnTitle != null) earnTitle.setText(RewardsLabels.earnMore(this));
        if (leaderboardTitle != null) leaderboardTitle.setText(RewardsLabels.leaderboard(this));
        if (leaderboardHint != null) leaderboardHint.setVisibility(View.GONE);
        if (historyTitle != null) historyTitle.setText(RewardsLabels.history(this));
        if (goWalletBtn != null) goWalletBtn.setText(RewardsLabels.goToWallet(this));
    }

    private void openWallet() {
        if (!requireLogin()) return;
        startActivity(new Intent(this, RewardsWalletActivity.class));
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
        coinsView.setText(String.valueOf(coins));
        if (coinsCaption != null) coinsCaption.setText(RewardsLabels.coinsThisWeek(this));

        if (body.has("rank") && !body.get("rank").isJsonNull()) {
            rankView.setText(RewardsLabels.rankPrefix(this, body.get("rank").getAsInt()));
        } else {
            rankView.setText(
                    joined ? "Rank —" : RewardsLabels.loginToRank(this));
        }

        String rate =
                body.has("rate_label")
                        ? body.get("rate_label").getAsString()
                        : RewardsConfigHelper.rateLabel(this);
        rateView.setText(rate);

        if (body.has("week") && body.get("week").isJsonObject()) {
            JsonObject w = body.getAsJsonObject("week");
            weekView.setText(
                    "Week "
                            + optStr(w, "week_key", "")
                            + " · "
                            + optStr(w, "status", ""));
        }

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
        renderHistory(body);
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
            if (w.has("ad_unit_id") && !w.get("ad_unit_id").isJsonNull()) {
                watchAdsUnit = w.get("ad_unit_id").getAsString();
            }
            if (w.has("ad_format") && !w.get("ad_format").isJsonNull()) {
                String fmt = w.get("ad_format").getAsString();
                if (!TextUtils.isEmpty(fmt)) {
                    watchAdsFormat = fmt.trim().toLowerCase(Locale.US);
                }
            }
            if (watchAdsBtn != null) {
                if (watchCooldownSec > 0) {
                    int m = Math.max(1, (int) Math.ceil(watchCooldownSec / 60.0));
                    watchAdsBtn.setText("Watch ads — wait " + m + "m");
                } else {
                    watchAdsBtn.setText(
                            RewardsLabels.watchAds(this, watchAdsRequired, watchAdsCoins));
                }
                watchAdsBtn.setEnabled(watchAdsAvailable && joined && !guest);
                watchAdsBtn.setAlpha(watchAdsBtn.isEnabled() ? 1f : 0.45f);
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
                                ? "Rating screenshot pending review"
                                : RewardsLabels.ratingAction(this, ratingCoins));
                ratingBtn.setEnabled(ratingAvailable && joined && !guest);
                ratingBtn.setAlpha(ratingBtn.isEnabled() ? 1f : 0.45f);
            }
        }
        if (earnCard != null) earnCard.setVisibility(View.VISIBLE);
        if (earnTitle != null) earnTitle.setText(RewardsLabels.earnMore(this));
    }

    private void onCheckin() {
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
        if (!requireLogin()) return;
        if (!joined) {
            toast(RewardsLabels.joinFirst(this));
            return;
        }
        Intent intent = new Intent(this, RewardsWatchAdsActivity.class);
        intent.putExtra(RewardsWatchAdsActivity.EXTRA_ADS_REQUIRED, watchAdsRequired);
        intent.putExtra(RewardsWatchAdsActivity.EXTRA_COINS, watchAdsCoins);
        intent.putExtra(RewardsWatchAdsActivity.EXTRA_AD_FORMAT, watchAdsFormat);
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
        try {
            InputStream in = getContentResolver().openInputStream(uri);
            if (in == null) {
                toast("Could not read image");
                return;
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) >= 0) bos.write(buf, 0, n);
            in.close();
            byte[] bytes = bos.toByteArray();
            RequestBody req = RequestBody.create(MediaType.parse("image/jpeg"), bytes);
            MultipartBody.Part part =
                    MultipartBody.Part.createFormData("file", "rewards_rating.jpg", req);
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
                                                && response.body().getExtras() != null) {
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
                                            return;
                                        }
                                        submitRatingUrl(url);
                                    } catch (Exception e) {
                                        toast("Upload failed");
                                    }
                                }

                                @Override
                                public void onFailure(
                                        Call<UserImagePojoResponse> call, Throwable t) {
                                    toast("Upload network error");
                                }
                            });
        } catch (Exception e) {
            toast("Could not read image");
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
                                    toast("Submitted — pending admin approval");
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

    /** User history: only +N / −N and time — no task/factor names. */
    private void renderHistory(JsonObject body) {
        history.removeAllViews();
        addSignedCoinRows(body);
        addSignedMoneyRows(body, "conversions", true);
        addSignedMoneyRows(body, "withdrawals", false);
        if (history.getChildCount() == 0) {
            TextView empty = new TextView(this);
            empty.setText(RewardsLabels.noHistory(this));
            empty.setTextColor(Color.parseColor("#FFA8A29E"));
            empty.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            empty.setPadding(12, 16, 12, 16);
            history.addView(empty);
        }
    }

    private void addSignedCoinRows(JsonObject body) {
        if (!body.has("coin_history") || !body.get("coin_history").isJsonArray()) return;
        JsonArray arr = body.getAsJsonArray("coin_history");
        int n = Math.min(12, arr.size());
        for (int i = 0; i < n; i++) {
            JsonObject row = arr.get(i).getAsJsonObject();
            int coins = optInt(row, "coins");
            if (coins == 0) continue;
            String sign = coins > 0 ? "+" : "";
            String when = formatWhen(row);
            TextView line = new TextView(this);
            line.setText(sign + coins + (TextUtils.isEmpty(when) ? "" : "  ·  " + when));
            line.setTextColor(Color.parseColor("#FF44403C"));
            line.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            line.setPadding(12, 8, 12, 8);
            history.addView(line);
        }
    }

    private void addSignedMoneyRows(JsonObject body, String key, boolean credit) {
        if (!body.has(key) || !body.get(key).isJsonArray()) return;
        JsonArray arr = body.getAsJsonArray(key);
        int n = Math.min(8, arr.size());
        for (int i = 0; i < n; i++) {
            JsonObject row = arr.get(i).getAsJsonObject();
            double amt =
                    credit ? optDouble(row, "rupees") : optDouble(row, "amount_rupees");
            if (amt <= 0) continue;
            String sign = credit ? "+" : "−";
            String when = formatWhen(row);
            TextView line = new TextView(this);
            line.setText(
                    sign
                            + "₹"
                            + formatMoney(amt)
                            + (TextUtils.isEmpty(when) ? "" : "  ·  " + when));
            line.setTextColor(Color.parseColor("#FF44403C"));
            line.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            line.setPadding(12, 8, 12, 8);
            history.addView(line);
        }
    }

    private void loadLeaderboard() {
        APIService api = RestApiClient.getClient().create(APIService.class);
        api.rewards_leaderboard("v3", authBody()).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                JsonObject body = response.body();
                leaderboard.removeAllViews();
                if (body == null || !body.has("rows") || !body.get("rows").isJsonArray()) {
                    TextView empty = new TextView(RewardsHubActivity.this);
                    empty.setText("Leaderboard empty");
                    empty.setPadding(16, 16, 16, 16);
                    empty.setTextColor(Color.parseColor("#FFA8A29E"));
                    leaderboard.addView(empty);
                    return;
                }
                JsonArray rows = body.getAsJsonArray("rows");
                int n = Math.min(20, rows.size());
                for (int i = 0; i < n; i++) {
                    JsonObject r = rows.get(i).getAsJsonObject();
                    LinearLayout row = new LinearLayout(RewardsHubActivity.this);
                    row.setOrientation(LinearLayout.HORIZONTAL);
                    row.setPadding(14, 12, 14, 12);
                    TextView rank = new TextView(RewardsHubActivity.this);
                    rank.setText("#" + optInt(r, "rank"));
                    rank.setTextColor(Color.parseColor("#FFB45309"));
                    rank.setTypeface(rank.getTypeface(), android.graphics.Typeface.BOLD);
                    rank.setMinWidth(dp(42));
                    TextView name = new TextView(RewardsHubActivity.this);
                    name.setText(optStr(r, "name", "Reader"));
                    name.setTextColor(Color.parseColor("#FF1C1917"));
                    name.setLayoutParams(
                            new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                    TextView coins = new TextView(RewardsHubActivity.this);
                    coins.setText(optInt(r, "coins") + " coins");
                    coins.setTextColor(Color.parseColor("#FF78716C"));
                    row.addView(rank);
                    row.addView(name);
                    row.addView(coins);
                    leaderboard.addView(row);
                    if (i < n - 1) {
                        View divider = new View(RewardsHubActivity.this);
                        divider.setBackgroundColor(Color.parseColor("#FFF5F5F4"));
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
