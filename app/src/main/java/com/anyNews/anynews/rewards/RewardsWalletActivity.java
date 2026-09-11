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
import com.anyNews.anynews.rewards.RewardsConfigHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Profile → Wallet: shared balance ₹, coins, convert at admin rate, UPI withdraw.
 * Kept separate from Rewards hub (earn / leaderboard).
 */
public class RewardsWalletActivity extends AppCompatActivity {

    private TextView balanceView;
    private TextView coinsLine;
    private TextView rateView;
    private TextView withdrawEtaView;
    private TextView convertBtn;
    private TextView saveUpiBtn;
    private TextView withdrawBtn;
    private EditText upiInput;
    private EditText amountInput;
    private LinearLayout history;
    private boolean joined;
    private boolean guest;
    private double walletRupees;
    private double minWithdraw = 10;
    private int periodCoins;
    private int minCoinsToConvert = 200;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!DistrictPinVisibilityConfig.rewardsMasterEnabled(this)
                || !DistrictPinVisibilityConfig.rewardsVisibleForUser(this)) {
            Toast.makeText(this, "Wallet is currently off", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        setContentView(R.layout.activity_rewards_wallet);
        applySystemBars();
        bindViews();
        applyLabels();
        findViewById(R.id.wallet_back).setOnClickListener(v -> finish());
        convertBtn.setOnClickListener(v -> onConvert());
        saveUpiBtn.setOnClickListener(v -> onSaveUpi());
        withdrawBtn.setOnClickListener(v -> onWithdraw());
        loadWallet();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (balanceView != null) loadWallet();
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
        View root = findViewById(R.id.wallet_root);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, bars.top, 0, bars.bottom);
            return insets;
        });
    }

    private void bindViews() {
        balanceView = findViewById(R.id.wallet_balance);
        coinsLine = findViewById(R.id.wallet_coins_line);
        rateView = findViewById(R.id.wallet_rate);
        withdrawEtaView = findViewById(R.id.wallet_withdraw_eta);
        convertBtn = findViewById(R.id.wallet_convert_btn);
        saveUpiBtn = findViewById(R.id.wallet_save_upi);
        withdrawBtn = findViewById(R.id.wallet_withdraw_btn);
        upiInput = findViewById(R.id.wallet_upi);
        amountInput = findViewById(R.id.wallet_withdraw_amount);
        history = findViewById(R.id.wallet_history);
    }

    private void applyLabels() {
        TextView top = findViewById(R.id.wallet_top_title);
        if (top != null) top.setText(RewardsLabels.walletScreenTitle(this));
        TextView balLabel = findViewById(R.id.wallet_balance_label);
        if (balLabel != null) balLabel.setText(RewardsLabels.balanceLabel(this));
        TextView histTitle = findViewById(R.id.wallet_history_title);
        if (histTitle != null) histTitle.setText(RewardsLabels.history(this));
        convertBtn.setText(RewardsLabels.convertCoins(this));
        saveUpiBtn.setText(RewardsLabels.saveUpi(this));
        withdrawBtn.setText(RewardsLabels.withdraw(this));
        if (upiInput != null) upiInput.setHint(RewardsLabels.upiHint(this));
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

    private void loadWallet() {
        APIService api = RestApiClient.getClient().create(APIService.class);
        api.rewards_hub("v3", authBody()).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                JsonObject body = response.body();
                if (body == null || !body.has("success")) {
                    toast("Could not load wallet");
                    return;
                }
                if (body.has("enabled") && !body.get("enabled").getAsBoolean()) {
                    toast("Wallet is currently off");
                    finish();
                    return;
                }
                if (body.has("excluded") && body.get("excluded").getAsBoolean()) {
                    toast("Wallet is not available for reporter accounts");
                    finish();
                    return;
                }
                applyWallet(body);
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                toast("Network error");
            }
        });
    }

    private void applyWallet(JsonObject body) {
        guest = body.has("guest") && body.get("guest").getAsBoolean();
        joined = body.has("joined") && body.get("joined").getAsBoolean();
        RewardsConfigHelper.applyFromRewardsPayload(this, body);

        JsonObject copy =
                body.has("copy") && body.get("copy").isJsonObject()
                        ? body.getAsJsonObject("copy")
                        : new JsonObject();

        periodCoins = body.has("coins") ? body.get("coins").getAsInt() : 0;
        walletRupees =
                body.has("wallet_rupees") ? body.get("wallet_rupees").getAsDouble() : 0;
        minWithdraw =
                body.has("min_withdraw_rupees")
                        ? body.get("min_withdraw_rupees").getAsDouble()
                        : 10;
        if (body.has("min_coins_to_convert") && !body.get("min_coins_to_convert").isJsonNull()) {
            minCoinsToConvert = body.get("min_coins_to_convert").getAsInt();
        } else {
            minCoinsToConvert = 200;
        }

        balanceView.setText("₹" + formatMoney(walletRupees));
        coinsLine.setText(periodCoins + " " + RewardsLabels.coinsWord(this));
        String rate =
                body.has("rate_label")
                        ? body.get("rate_label").getAsString()
                        : RewardsConfigHelper.rateLabel(this);
        if (TextUtils.isEmpty(rate) && body.has("coins_per_rupee")) {
            rate = body.get("coins_per_rupee").getAsInt() + " coins = ₹1";
        }
        rateView.setText(rate != null ? rate : "");
        withdrawEtaView.setText(
                optStr(
                        copy,
                        "withdraw_eta",
                        "Credit to your UPI usually takes 24–48 hours after approval."));

        if (body.has("upi_id") && !body.get("upi_id").isJsonNull()) {
            String upi = body.get("upi_id").getAsString();
            if (!TextUtils.isEmpty(upi)) upiInput.setText(upi);
        }

        boolean canAct = joined && !guest;
        convertBtn.setEnabled(canAct && periodCoins >= minCoinsToConvert);
        convertBtn.setAlpha(convertBtn.isEnabled() ? 1f : 0.45f);
        saveUpiBtn.setEnabled(!guest);
        saveUpiBtn.setAlpha(saveUpiBtn.isEnabled() ? 1f : 0.45f);
        withdrawBtn.setEnabled(canAct);
        withdrawBtn.setAlpha(withdrawBtn.isEnabled() ? 1f : 0.45f);

        try {
            SharedPrefsData.putString(
                    this, "rewards_wallet_rupees", String.valueOf(walletRupees), Constants.PREF_NAME);
            SharedPrefsData.putString(
                    this, "rewards_period_coins", String.valueOf(periodCoins), Constants.PREF_NAME);
        } catch (Exception ignored) {
        }

        renderHistory(body);
    }

    private void renderHistory(JsonObject body) {
        history.removeAllViews();
        addSignedHistory(body, "conversions", true);
        addSignedHistory(body, "withdrawals", false);
        if (history.getChildCount() == 0) {
            TextView empty = new TextView(this);
            empty.setText(RewardsLabels.noHistory(this));
            empty.setTextColor(Color.parseColor("#FFA89880"));
            empty.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            empty.setPadding(dp(20), dp(24), dp(20), dp(24));
            history.addView(empty);
        }
    }

    private void addSignedHistory(JsonObject body, String key, boolean credit) {
        if (!body.has(key) || !body.get(key).isJsonArray()) return;
        JsonArray arr = body.getAsJsonArray(key);
        int n = Math.min(12, arr.size());
        for (int i = 0; i < n; i++) {
            JsonObject row = arr.get(i).getAsJsonObject();
            double amt =
                    credit
                            ? optDouble(row, "rupees")
                            : optDouble(row, "amount_rupees");
            if (amt <= 0) continue;
            String sign = credit ? "+" : "−";
            String when = formatWhen(row);
            LinearLayout line = new LinearLayout(this);
            line.setOrientation(LinearLayout.HORIZONTAL);
            line.setGravity(android.view.Gravity.CENTER_VERTICAL);
            line.setPadding(dp(20), dp(16), dp(20), dp(16));
            TextView amtView = new TextView(this);
            amtView.setText(sign + "₹" + formatMoney(amt));
            amtView.setTextColor(Color.parseColor("#FFE4C27A"));
            amtView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            amtView.setTypeface(amtView.getTypeface(), android.graphics.Typeface.BOLD);
            amtView.setMinWidth(dp(72));
            TextView whenView = new TextView(this);
            whenView.setText(when);
            whenView.setTextColor(Color.parseColor("#FFF4EDE0"));
            whenView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            LinearLayout.LayoutParams whenLp =
                    new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            whenLp.setMarginStart(dp(16));
            whenView.setLayoutParams(whenLp);
            line.addView(amtView);
            line.addView(whenView);
            history.addView(line);
        }
    }

    private void onConvert() {
        if (!requireLogin()) return;
        if (!joined) {
            toast(RewardsLabels.joinFirst(this));
            return;
        }
        APIService api = RestApiClient.getClient().create(APIService.class);
        api.rewards_convert("v3", authBody())
                .enqueue(
                        new Callback<JsonObject>() {
                            @Override
                            public void onResponse(
                                    Call<JsonObject> call, Response<JsonObject> response) {
                                JsonObject b = response.body();
                                if (b != null
                                        && b.has("success")
                                        && b.get("success").getAsBoolean()) {
                                    double rupees =
                                            b.has("rupees") ? b.get("rupees").getAsDouble() : 0;
                                    toast("+" + formatMoney(rupees) + " to wallet");
                                    loadWallet();
                                } else {
                                    toast(
                                            b != null && b.has("msg")
                                                    ? b.get("msg").getAsString()
                                                    : "Convert failed");
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
            toast(RewardsLabels.enterUpi(this));
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
                    loadWallet();
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
            toast(RewardsLabels.joinFirst(this));
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
                    loadWallet();
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

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
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
        String raw = optStr(row, "createdAt", null);
        if (TextUtils.isEmpty(raw)) raw = optStr(row, "updatedAt", null);
        if (TextUtils.isEmpty(raw)) return optStr(row, "day_key", "");
        try {
            // Gson typically yields ISO-8601 like 2026-09-07T02:04:00.000Z
            String iso = raw.trim();
            if (iso.endsWith("Z")) {
                SimpleDateFormat in =
                        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                in.setTimeZone(TimeZone.getTimeZone("UTC"));
                String trunc = iso;
                int dot = iso.indexOf('.');
                if (dot > 0) {
                    trunc = iso.substring(0, Math.min(iso.length(), dot + 4)) + "Z";
                    if (trunc.charAt(trunc.length() - 2) != 'Z') {
                        // ensure form …SSS'Z'
                        String frac = iso.substring(dot + 1).replace("Z", "");
                        if (frac.length() < 3) {
                            while (frac.length() < 3) frac += "0";
                        } else if (frac.length() > 3) {
                            frac = frac.substring(0, 3);
                        }
                        trunc = iso.substring(0, dot + 1) + frac + "Z";
                    }
                } else {
                    in = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
                    in.setTimeZone(TimeZone.getTimeZone("UTC"));
                    trunc = iso;
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
}
