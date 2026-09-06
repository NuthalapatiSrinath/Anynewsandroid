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
import android.widget.EditText;
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

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Profile → Rewards hub: coins, rank, rate, wallet ₹, UPI, withdraw, history, leaderboard.
 */
public class RewardsHubActivity extends AppCompatActivity {

    private TextView coinsView;
    private TextView rankView;
    private TextView rateView;
    private TextView weekView;
    private TextView subtitleView;
    private TextView walletView;
    private TextView withdrawEtaView;
    private TextView rulesView;
    private TextView joinBtn;
    private EditText upiInput;
    private EditText amountInput;
    private LinearLayout leaderboard;
    private LinearLayout history;
    private View walletCard;
    private TextView checkinBtn;
    private TextView watchAdsBtn;
    private TextView ratingBtn;
    private View earnCard;
    private boolean joined;
    private boolean guest;
    private double walletRupees;
    private double minWithdraw = 10;
    private int watchAdsRequired = 3;
    private int watchAdsCoins = 40;
    private String watchAdsUnit = "";
    private int checkinCoins = 25;
    private int ratingCoins = 100;
    private boolean checkinAvailable;
    private boolean watchAdsAvailable;
    private boolean ratingAvailable;
    private int watchCooldownSec;

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
        if (!DistrictPinVisibilityConfig.rewardsMasterEnabled(this)) {
            Toast.makeText(this, "Rewards is currently off", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        setContentView(R.layout.activity_rewards_hub);
        applySystemBars();
        bindViews();
        findViewById(R.id.rewards_back).setOnClickListener(v -> finish());
        joinBtn.setOnClickListener(v -> onJoin());
        findViewById(R.id.rewards_save_upi).setOnClickListener(v -> onSaveUpi());
        findViewById(R.id.rewards_withdraw_btn).setOnClickListener(v -> onWithdraw());
        if (checkinBtn != null) checkinBtn.setOnClickListener(v -> onCheckin());
        if (watchAdsBtn != null) watchAdsBtn.setOnClickListener(v -> onWatchAds());
        if (ratingBtn != null) ratingBtn.setOnClickListener(v -> onRating());
        RewardsEngagementBatcher.get().trackActiveDay(this);
        loadHub();
        loadLeaderboard();
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
        walletView = findViewById(R.id.rewards_wallet);
        withdrawEtaView = findViewById(R.id.rewards_withdraw_eta);
        rulesView = findViewById(R.id.rewards_rules);
        joinBtn = findViewById(R.id.rewards_join_btn);
        upiInput = findViewById(R.id.rewards_upi);
        amountInput = findViewById(R.id.rewards_withdraw_amount);
        leaderboard = findViewById(R.id.rewards_leaderboard);
        history = findViewById(R.id.rewards_history);
        walletCard = findViewById(R.id.rewards_wallet_card);
        checkinBtn = findViewById(R.id.rewards_checkin_btn);
        watchAdsBtn = findViewById(R.id.rewards_watch_ads_btn);
        ratingBtn = findViewById(R.id.rewards_rating_btn);
        earnCard = findViewById(R.id.rewards_earn_card);
    }

    private JsonObject authBody() {
        JsonObject body = new JsonObject();
        String uid = SharedPrefsData.getString(this, Constants.UserID, Constants.PREF_NAME);
        String sid = SharedPrefsData.getString(this, Constants.SessionID, Constants.PREF_NAME);
        if (!TextUtils.isEmpty(uid)) body.addProperty("USERID", uid);
        if (!TextUtils.isEmpty(sid)) body.addProperty("SessionID", sid);
        return body;
    }

    private boolean requireLogin() {
        boolean loggedIn =
                SharedPrefsData.getBool(this, Constants.is_login, Constants.PREF_NAME);
        if (!loggedIn) {
            startActivity(new Intent(this, LoginActivityDetails.class));
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
                        ? optStr(copy, "joined_hub_title", "AnyNews Coins")
                        : optStr(copy, "guest_hub_title", "Join AnyNews Rewards");
        TextView top = findViewById(R.id.rewards_top_title);
        if (top != null) top.setText(title);

        subtitleView.setText(
                joined
                        ? optStr(copy, "joined_hub_subtitle", "Keep reading to earn coins.")
                        : optStr(copy, "guest_hub_subtitle", "Login and join to earn coins."));

        int coins = body.has("coins") ? body.get("coins").getAsInt() : 0;
        coinsView.setText(String.valueOf(coins));

        if (body.has("rank") && !body.get("rank").isJsonNull()) {
            rankView.setText("Rank #" + body.get("rank").getAsInt());
        } else {
            rankView.setText(joined ? "Rank —" : "Login to rank");
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

        walletRupees =
                body.has("wallet_rupees") ? body.get("wallet_rupees").getAsDouble() : 0;
        walletView.setText("₹" + formatMoney(walletRupees));
        minWithdraw =
                body.has("min_withdraw_rupees")
                        ? body.get("min_withdraw_rupees").getAsDouble()
                        : 10;
        withdrawEtaView.setText(
                optStr(copy, "withdraw_eta", "Credit usually takes 24–48 hours."));
        rulesView.setText(optStr(copy, "rules_summary", ""));

        if (body.has("upi_id") && !body.get("upi_id").isJsonNull()) {
            upiInput.setText(body.get("upi_id").getAsString());
        }

        if (guest || !joined) {
            joinBtn.setVisibility(View.VISIBLE);
            joinBtn.setText(guest ? "Login to join" : "Join Rewards");
        } else {
            joinBtn.setVisibility(View.GONE);
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
                                ? "Checked in today"
                                : "Daily check-in (+" + checkinCoins + ")");
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
            if (watchAdsBtn != null) {
                if (watchCooldownSec > 0) {
                    int m = Math.max(1, (int) Math.ceil(watchCooldownSec / 60.0));
                    watchAdsBtn.setText("Watch ads — wait " + m + "m");
                } else {
                    watchAdsBtn.setText(
                            "Watch " + watchAdsRequired + " ads (+" + watchAdsCoins + ")");
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
                                : "Play Store 5★ + screenshot (+" + ratingCoins + ")");
                ratingBtn.setEnabled(ratingAvailable && joined && !guest);
                ratingBtn.setAlpha(ratingBtn.isEnabled() ? 1f : 0.45f);
            }
        }
        if (earnCard != null) {
            earnCard.setVisibility(View.VISIBLE);
        }
    }

    private void onCheckin() {
        if (!requireLogin()) return;
        if (!joined) {
            toast("Join rewards first");
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
            toast("Join rewards first");
            return;
        }
        Intent intent = new Intent(this, RewardsWatchAdsActivity.class);
        intent.putExtra(RewardsWatchAdsActivity.EXTRA_ADS_REQUIRED, watchAdsRequired);
        intent.putExtra(RewardsWatchAdsActivity.EXTRA_COINS, watchAdsCoins);
        if (!TextUtils.isEmpty(watchAdsUnit)) {
            intent.putExtra(RewardsWatchAdsActivity.EXTRA_AD_UNIT, watchAdsUnit);
        }
        watchAdsLauncher.launch(intent);
    }

    private void onRating() {
        if (!requireLogin()) return;
        if (!joined) {
            toast("Join rewards first");
            return;
        }
        try {
            // Open Play Store listing for rating, then pick screenshot.
            String pkg = getPackageName();
            Intent market =
                    new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=" + pkg));
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
        } catch (Exception ignored) {
        }
        toast("After rating, pick your screenshot");
        pickScreenshot.launch("image/*");
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
                                        url = firstNonEmpty(
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

    private void renderHistory(JsonObject body) {
        history.removeAllViews();
        addHistorySection(body, "withdrawals", "Withdrawals");
        addHistorySection(body, "conversions", "Wallet credits");
        if (history.getChildCount() == 0) {
            TextView empty = new TextView(this);
            empty.setText("No history yet");
            empty.setTextColor(Color.parseColor("#FFA8A29E"));
            empty.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            empty.setPadding(12, 16, 12, 16);
            history.addView(empty);
        }
    }

    private void addHistorySection(JsonObject body, String key, String title) {
        if (!body.has(key) || !body.get(key).isJsonArray()) return;
        JsonArray arr = body.getAsJsonArray(key);
        if (arr.size() == 0) return;
        TextView h = new TextView(this);
        h.setText(title);
        h.setTextColor(Color.parseColor("#FF78716C"));
        h.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        h.setPadding(12, 8, 12, 4);
        history.addView(h);
        int n = Math.min(8, arr.size());
        for (int i = 0; i < n; i++) {
            JsonObject row = arr.get(i).getAsJsonObject();
            TextView line = new TextView(this);
            String text;
            if ("withdrawals".equals(key)) {
                text =
                        "₹"
                                + optNum(row, "amount_rupees")
                                + " · "
                                + optStr(row, "status_label", "")
                                + " · "
                                + optStr(row, "upi_id", "");
            } else {
                text =
                        "+"
                                + "₹"
                                + optNum(row, "rupees")
                                + " from "
                                + optNum(row, "coins")
                                + " coins ("
                                + optStr(row, "week_key", "")
                                + ")";
            }
            line.setText(text);
            line.setTextColor(Color.parseColor("#FF44403C"));
            line.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            line.setPadding(12, 6, 12, 6);
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
        JsonObject body = authBody();
        String upi = upiInput.getText() != null ? upiInput.getText().toString().trim() : "";
        if (!TextUtils.isEmpty(upi)) body.addProperty("upi_id", upi);
        api.rewards_join("v3", body).enqueue(new Callback<JsonObject>() {
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

    private void onSaveUpi() {
        if (!requireLogin()) return;
        String upi = upiInput.getText() != null ? upiInput.getText().toString().trim() : "";
        if (TextUtils.isEmpty(upi)) {
            toast("Enter UPI ID");
            return;
        }
        JsonObject body = authBody();
        body.addProperty("upi_id", upi);
        APIService api = RestApiClient.getClient().create(APIService.class);
        api.rewards_update_upi("v3", body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                JsonObject b = response.body();
                if (b != null && b.has("success") && b.get("success").getAsBoolean()) {
                    toast("UPI saved");
                    if (!joined) loadHub();
                } else {
                    toast(b != null && b.has("msg") ? b.get("msg").getAsString() : "Failed");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                toast("Network error");
            }
        });
    }

    private void onWithdraw() {
        if (!requireLogin()) return;
        if (!joined) {
            toast("Join rewards first");
            return;
        }
        String amtStr =
                amountInput.getText() != null ? amountInput.getText().toString().trim() : "";
        double amt;
        try {
            amt = Double.parseDouble(amtStr);
        } catch (Exception e) {
            toast("Enter amount");
            return;
        }
        if (amt < minWithdraw) {
            toast("Minimum ₹" + formatMoney(minWithdraw));
            return;
        }
        JsonObject body = authBody();
        body.addProperty("amount_rupees", amt);
        APIService api = RestApiClient.getClient().create(APIService.class);
        api.rewards_withdraw("v3", body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                JsonObject b = response.body();
                if (b != null && b.has("success") && b.get("success").getAsBoolean()) {
                    toast("Withdraw requested");
                    amountInput.setText("");
                    loadHub();
                } else {
                    toast(b != null && b.has("msg") ? b.get("msg").getAsString() : "Failed");
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

    private static String optNum(JsonObject o, String k) {
        try {
            if (o != null && o.has(k) && !o.get(k).isJsonNull()) {
                return formatMoney(o.get(k).getAsDouble());
            }
        } catch (Exception ignored) {
        }
        return "0";
    }

    private static String formatMoney(double v) {
        if (Math.abs(v - Math.rint(v)) < 0.001) return String.valueOf((long) Math.rint(v));
        return String.format(java.util.Locale.US, "%.2f", v);
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
