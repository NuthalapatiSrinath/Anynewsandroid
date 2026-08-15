package com.anyNews.anynews.referral;

import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.MessageDigest;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;

/** Stable per-install opaque identity. Only its SHA-256 hash leaves the device. */
public final class ReferralInstallIdentity {
    private static final String ALIAS = "anynews.referral.install.v1";
    public static String hash(Context context) throws Exception {
        KeyStore ks = KeyStore.getInstance("AndroidKeyStore"); ks.load(null);
        SecretKey key;
        if (!ks.containsAlias(ALIAS)) {
            KeyGenerator generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_HMAC_SHA256, "AndroidKeyStore");
            generator.init(new KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_SIGN).build());
            generator.generateKey();
        }
        key = ((KeyStore.SecretKeyEntry) ks.getEntry(ALIAS, null)).getSecretKey();
        Mac mac = Mac.getInstance("HmacSHA256"); mac.init(key);
        // The keystore key itself is random and per-install; HMAC makes a stable,
        // non-exportable install secret without persisting raw key material.
        byte[] secret = mac.doFinal("anynews-referral-install-v1".getBytes(StandardCharsets.UTF_8));
        return hex(MessageDigest.getInstance("SHA-256").digest(secret));
    }
    private static String hex(byte[] value) { StringBuilder out = new StringBuilder(); for (byte b : value) out.append(String.format("%02x", b)); return out.toString(); }
    private ReferralInstallIdentity() {}
}
