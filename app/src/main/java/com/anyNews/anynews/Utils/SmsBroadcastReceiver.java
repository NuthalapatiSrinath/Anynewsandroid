package com.anyNews.anynews.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.Status;

/**
 * SMS Retriever delivery. Play Services includes the message only when the SMS
 * ends with this app's 11-character hash. No READ_SMS and no Allow sheet.
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
                String message = extras.getString(SmsRetriever.EXTRA_SMS_MESSAGE);
                if (message != null && !message.isEmpty()) {
                    smsBroadcastReceiverListener.onSmsMessageReceived(message);
                } else {
                    smsBroadcastReceiverListener.onFailure();
                }
                break;
            case CommonStatusCodes.TIMEOUT:
                smsBroadcastReceiverListener.onFailure();
                break;
            default:
                smsBroadcastReceiverListener.onFailure();
                break;
        }
    }

    public interface SmsBroadcastReceiverListener {
        /** Automatic fill from SMS Retriever (no user prompt). */
        void onSmsMessageReceived(String message);

        void onFailure();
    }
}
