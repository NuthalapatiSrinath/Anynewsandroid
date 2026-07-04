package com.anyNews.anynews.Utils;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.Status;

import static android.content.Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION;
import static android.content.Intent.FLAG_GRANT_PREFIX_URI_PERMISSION;
import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION;


public class SmsBroadcastReceiver extends BroadcastReceiver {
    public SmsBroadcastReceiverListener smsBroadcastReceiverListener;
    @Override
    public void onReceive(Context context, Intent intent) {
        if (SmsRetriever.SMS_RETRIEVED_ACTION.equals(intent.getAction())) {
            Bundle extras = intent.getExtras();
            Status smsRetrieverStatus = (Status) extras.get(SmsRetriever.EXTRA_STATUS);
            switch (smsRetrieverStatus.getStatusCode()) {
                case CommonStatusCodes.SUCCESS:

                    Intent messageIntent = extras.getParcelable(SmsRetriever.EXTRA_CONSENT_INTENT);
                    try {

                        ComponentName name = messageIntent.resolveActivity(context.getPackageManager());

                        Logone.e("SmsBroadcastReceiver", "onReceive: "+name.getPackageName() + " " + name.getClassName());

                        if (name.getPackageName().equalsIgnoreCase("com.google.android.gms") &&
                                name.getClassName().equalsIgnoreCase("com.google.android.gms.auth.api.phone.ui.UserConsentPromptActivity")) {

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                messageIntent.removeFlags(FLAG_GRANT_READ_URI_PERMISSION);
                                messageIntent.removeFlags(FLAG_GRANT_WRITE_URI_PERMISSION);
                                messageIntent.removeFlags(FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                                messageIntent.removeFlags(FLAG_GRANT_PREFIX_URI_PERMISSION);
                            }

                            smsBroadcastReceiverListener.onSuccess(messageIntent);
                        }
                    } catch (ActivityNotFoundException e) {
                        // Handle the exception ...
                        e.printStackTrace();
                    }
                    break;
                case CommonStatusCodes.TIMEOUT:
                    smsBroadcastReceiverListener.onFailure();
                    break;
            }
        }
    }
    public interface SmsBroadcastReceiverListener {
        void onSuccess(Intent intent);
        void onFailure();
    }
}
