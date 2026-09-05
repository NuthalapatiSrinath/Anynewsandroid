package com.anyNews.anynews.Utils;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.Status;

import static android.content.Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION;
import static android.content.Intent.FLAG_GRANT_PREFIX_URI_PERMISSION;
import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION;

/**
 * Handles both SMS Retriever (auto message string) and SMS User Consent (consent Intent).
 * Prefer Retriever — no READ_SMS / RECEIVE_SMS required when the SMS includes the app hash.
 */
public class SmsBroadcastReceiver extends BroadcastReceiver {
    public SmsBroadcastReceiverListener smsBroadcastReceiverListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!SmsRetriever.SMS_RETRIEVED_ACTION.equals(intent.getAction())) {
            return;
        }
        Bundle extras = intent.getExtras();
        if (extras == null || smsBroadcastReceiverListener == null) {
            return;
        }
        Status smsRetrieverStatus = (Status) extras.get(SmsRetriever.EXTRA_STATUS);
        if (smsRetrieverStatus == null) {
            return;
        }
        switch (smsRetrieverStatus.getStatusCode()) {
            case CommonStatusCodes.SUCCESS:
                // SMS Retriever API: message delivered directly (app-hash SMS).
                String message = extras.getString(SmsRetriever.EXTRA_SMS_MESSAGE);
                if (message != null && !message.isEmpty()) {
                    smsBroadcastReceiverListener.onSmsMessageReceived(message);
                    return;
                }
                // SMS User Consent API: one-time consent prompt.
                Intent messageIntent = extras.getParcelable(SmsRetriever.EXTRA_CONSENT_INTENT);
                if (messageIntent == null) {
                    smsBroadcastReceiverListener.onFailure();
                    return;
                }
                try {
                    ComponentName name = messageIntent.resolveActivity(context.getPackageManager());
                    if (name != null) {
                        Logone.e("SmsBroadcastReceiver",
                                "onReceive: " + name.getPackageName() + " " + name.getClassName());
                        if (name.getPackageName().equalsIgnoreCase("com.google.android.gms")
                                && name.getClassName().equalsIgnoreCase(
                                "com.google.android.gms.auth.api.phone.ui.UserConsentPromptActivity")) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                messageIntent.removeFlags(FLAG_GRANT_READ_URI_PERMISSION);
                                messageIntent.removeFlags(FLAG_GRANT_WRITE_URI_PERMISSION);
                                messageIntent.removeFlags(FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                                messageIntent.removeFlags(FLAG_GRANT_PREFIX_URI_PERMISSION);
                            }
                        }
                    }
                    smsBroadcastReceiverListener.onConsentIntent(messageIntent);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                    smsBroadcastReceiverListener.onFailure();
                }
                break;
            case CommonStatusCodes.TIMEOUT:
                smsBroadcastReceiverListener.onFailure();
                break;
            default:
                break;
        }
    }

    public interface SmsBroadcastReceiverListener {
        /** Automatic fill from SMS Retriever (no user prompt). */
        void onSmsMessageReceived(String message);

        /** Launch User Consent activity; result returns the SMS body. */
        void onConsentIntent(Intent consentIntent);

        void onFailure();
    }
}
