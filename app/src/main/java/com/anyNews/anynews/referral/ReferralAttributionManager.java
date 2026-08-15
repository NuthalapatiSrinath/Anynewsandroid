package com.anyNews.anynews.referral;

import android.content.Context;
import android.content.SharedPreferences;
import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.ReferrerDetails;

/** Pending attribution is transport only; the server decides every eligibility state. */
public final class ReferralAttributionManager {
    private static final String PREF = "referral_transport";
    private static final String KEY_CONSUMED = "install_referrer_consumed";
    private static final String KEY_CODE = "pending_referral_code";
    private static final String KEY_RAW = "install_referrer_raw";
    private static final String KEY_CLICK_AT = "install_referrer_click_at";
    public static void captureOnce(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        if (prefs.getBoolean(KEY_CONSUMED, false)) return;
        InstallReferrerClient client = InstallReferrerClient.newBuilder(context).build();
        client.startConnection(new com.android.installreferrer.api.InstallReferrerStateListener() {
            @Override public void onInstallReferrerSetupFinished(int responseCode) {
                try {
                    if (responseCode == InstallReferrerClient.InstallReferrerResponse.OK) {
                        ReferrerDetails details = client.getInstallReferrer();
                        String referrer = details.getInstallReferrer();
                        String code = android.net.Uri.parse("https://x/?" + referrer).getQueryParameter("an_ref");
                        if (code != null && code.matches("[A-Za-z0-9_-]{12,128}")) {
                            prefs.edit()
                                    .putString(KEY_CODE, code)
                                    .putString(KEY_RAW, referrer)
                                    .putLong(KEY_CLICK_AT, details.getReferrerClickTimestampSeconds() * 1000L)
                                    .apply();
                        }
                    }
                } catch (Exception ignored) {
                } finally { prefs.edit().putBoolean(KEY_CONSUMED, true).apply(); client.endConnection(); }
            }
            @Override public void onInstallReferrerServiceDisconnected() { }
        });
    }
    public static String pendingCode(Context c) { return c.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString(KEY_CODE, null); }
    public static String rawInstallReferrer(Context c) { return c.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString(KEY_RAW, null); }
    public static long referrerClickAtMillis(Context c) { return c.getSharedPreferences(PREF, Context.MODE_PRIVATE).getLong(KEY_CLICK_AT, 0L); }
    private ReferralAttributionManager() {}
}
