package com.anyNews.anynews.Utils;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Base64;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Computes the 11-character app hash required in SMS bodies for
 * {@link com.google.android.gms.auth.api.phone.SmsRetriever} (no READ_SMS).
 * Log {@link #getAppSignatures()} once and append the hash to the OTP SMS template on the server.
 */
public class AppSignatureHelper extends ContextWrapper {
    private static final String TAG = "AppSignatureHelper";
    private static final String HASH_TYPE = "SHA-256";
    private static final int NUM_HASHED_BYTES = 9;
    private static final int NUM_BASE64_CHAR = 11;

    public AppSignatureHelper(Context context) {
        super(context);
    }

    public ArrayList<String> getAppSignatures() {
        ArrayList<String> appCodes = new ArrayList<>();
        try {
            String packageName = getPackageName();
            @SuppressWarnings("deprecation")
            Signature[] signatures = getPackageManager()
                    .getPackageInfo(packageName, PackageManager.GET_SIGNATURES).signatures;
            if (signatures == null) {
                return appCodes;
            }
            for (Signature signature : signatures) {
                String hash = hash(packageName, signature.toCharsString());
                if (hash != null) {
                    appCodes.add(hash);
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Unable to find package to obtain hash.", e);
        }
        return appCodes;
    }

    private static String hash(String packageName, String signature) {
        String appInfo = packageName + " " + signature;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(HASH_TYPE);
            messageDigest.update(appInfo.getBytes(StandardCharsets.UTF_8));
            byte[] hashSignature = messageDigest.digest();
            hashSignature = Arrays.copyOfRange(hashSignature, 0, NUM_HASHED_BYTES);
            String base64Hash = Base64.encodeToString(hashSignature, Base64.NO_PADDING | Base64.NO_WRAP);
            base64Hash = base64Hash.substring(0, NUM_BASE64_CHAR);
            Log.d(TAG, String.format("SMS Retriever app hash: [%s]", base64Hash));
            return base64Hash;
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "hash:NoSuchAlgorithm", e);
        }
        return null;
    }
}
