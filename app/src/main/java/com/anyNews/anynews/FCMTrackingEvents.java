package com.anyNews.anynews;

import android.content.Context;
import android.os.Bundle;

import com.anyNews.anynews.Utils.AppHelper;
import com.google.firebase.analytics.FirebaseAnalytics;



/**
 * Created by sys0037 on 1/2/17.
 */

public class FCMTrackingEvents {
    FirebaseAnalytics mFirebaseAnalytics;

    public FCMTrackingEvents(Context context) {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);

    }

    public void FireBase(String ID, String Name) {
        AppHelper.showLog("Firebse triggered on.." + ID);
        AppHelper.showLog( "Firebse triggered on ..." + Name);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, ID);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, Name);
        mFirebaseAnalytics.logEvent(Name, bundle);
    }

    public void trackFirebaseAnalytics(int ID, String Name) {
        AppHelper.showLog("Firebse triggered on.." + ID);
        AppHelper.showLog( "Firebse triggered on ..." + Name);
        Bundle bundle = new Bundle();
        bundle.putInt(FirebaseAnalytics.Param.ITEM_ID, ID);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, Name);
        mFirebaseAnalytics.logEvent(Name, bundle);
    }

    public void trackFirebaseAnalytics(String bundle2, String Name, String ID) {
        AppHelper.showLog("Firebse triggered on.." + ID);
        AppHelper.showLog( "Firebse triggered on ..." + Name);
        Bundle bundle = new Bundle();
        bundle.putInt(FirebaseAnalytics.Param.ITEM_ID, 3);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, bundle2);
        mFirebaseAnalytics.logEvent(Name, bundle);
    }

    public void trackFirebaseAnalytics(Bundle bundle, String Name) {
        mFirebaseAnalytics.logEvent(Name, bundle);
    }

}
