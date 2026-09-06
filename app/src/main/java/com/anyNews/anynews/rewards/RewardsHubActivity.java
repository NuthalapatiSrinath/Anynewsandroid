package com.anyNews.anynews.rewards;

import android.content.Intent;
import android.graphics.Color;
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
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

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
    private boolean joined;
    private boolean guest;
    private double walletRupees;
    private double minWithdraw = 10;

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

        renderHistory(body);
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
}
