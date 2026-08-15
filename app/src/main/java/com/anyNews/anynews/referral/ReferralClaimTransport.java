package com.anyNews.anynews.referral;

import android.content.Context;
import com.anyNews.anynews.Services.APIService;
import com.google.android.play.core.integrity.IntegrityManager;
import com.google.android.play.core.integrity.IntegrityManagerFactory;
import com.google.android.play.core.integrity.IntegrityTokenRequest;
import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * The caller supplies only the short-lived OTP-bound referral session. User and
 * draft identity are deliberately never accepted from the Android claim body.
 */
public final class ReferralClaimTransport {
    public interface Result { void success(JsonObject value); void failure(String reason); }

    public static void claim(Context context, APIService api, String referralSession, Result result) {
        if (referralSession == null || referralSession.isEmpty()) { result.failure("Referral login verification is required"); return; }
        String code = ReferralAttributionManager.pendingCode(context);
        String rawReferrer = ReferralAttributionManager.rawInstallReferrer(context);
        if (code == null || rawReferrer == null) { result.failure("No verified Play install referral is available"); return; }
        final String installationHash;
        try { installationHash = ReferralInstallIdentity.hash(context); }
        catch (Exception e) { result.failure("Secure installation identity is unavailable"); return; }

        JsonObject nonceRequest = new JsonObject();
        nonceRequest.addProperty("installation_hash", installationHash);
        api.referralIntegrityNonce(referralSession, nonceRequest).enqueue(new Callback<JsonObject>() {
            @Override public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                String nonce = response.isSuccessful() && response.body() != null && response.body().has("data")
                        ? response.body().getAsJsonObject("data").get("nonce").getAsString() : null;
                if (nonce == null || nonce.isEmpty()) { result.failure("Could not obtain integrity nonce"); return; }
                IntegrityManager manager = IntegrityManagerFactory.create(context);
                manager.requestIntegrityToken(IntegrityTokenRequest.builder().setNonce(nonce).build())
                        .addOnSuccessListener(token -> submit(api, referralSession, installationHash, code, rawReferrer,
                                ReferralAttributionManager.referrerClickAtMillis(context), nonce, token.token(), result))
                        .addOnFailureListener(error -> result.failure("Play Integrity token unavailable"));
            }
            @Override public void onFailure(Call<JsonObject> call, Throwable error) { result.failure("Referral nonce request failed"); }
        });
    }

    private static void submit(APIService api, String session, String installationHash, String code, String rawReferrer,
                               long clickAt, String nonce, String integrityToken, Result result) {
        JsonObject body = new JsonObject();
        body.addProperty("installation_hash", installationHash);
        body.addProperty("referralCode", code);
        body.addProperty("installReferrer", rawReferrer);
        if (clickAt > 0) body.addProperty("installReferrerClickAt", clickAt);
        body.addProperty("integrityNonce", nonce);
        body.addProperty("integrityToken", integrityToken);
        api.referralClaimInstall(session, body).enqueue(new Callback<JsonObject>() {
            @Override public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null && response.body().has("success") && response.body().get("success").getAsBoolean()) result.success(response.body());
                else result.failure("Referral claim was rejected");
            }
            @Override public void onFailure(Call<JsonObject> call, Throwable error) { result.failure("Referral claim request failed"); }
        });
    }

    private ReferralClaimTransport() {}
}
