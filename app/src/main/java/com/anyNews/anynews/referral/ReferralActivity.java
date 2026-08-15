package com.anyNews.anynews.referral;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Thin isolated entry point; status/rewards are rendered only from referral/status. */
public class ReferralActivity extends Activity {
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL);
        int p = (int) (24 * getResources().getDisplayMetrics().density); root.setPadding(p, p, p, p);
        TextView title = new TextView(this); title.setText("Refer and earn"); title.setTextSize(24);
        TextView body = new TextView(this); body.setText("Referral rewards are verified after your friend meets the programme rules. Reward and payment status are confirmed by AnyNews.");
        root.addView(title); root.addView(body); setContentView(root);
        ReferralAttributionManager.captureOnce(this);
    }
}
