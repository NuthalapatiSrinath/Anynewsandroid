package com.anyNews.anynews.Activities;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.anyNews.anynews.BuildConfig;
import com.anyNews.anynews.Utils.OriginCallLog;

import java.util.ArrayList;

/**
 * Footer-reachable debug page: every API host call this process made.
 * Edge = CF-cacheable public GETs; Origin = still hitting Node (writes + leftover reads).
 */
public class OriginCallsDebugActivity extends AppCompatActivity {

    private TextView summary;
    private LinearLayout listHost;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#0F172A"));
        int pad = dp(16);
        root.setPadding(pad, pad, pad, pad);

        TextView title = new TextView(this);
        title.setText("Server / CF calls");
        title.setTextColor(Color.WHITE);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("API " + BuildConfig.API_BASE_URL
                + "\nedge = public CF GET · origin = Node POST/GET");
        sub.setTextColor(Color.parseColor("#94A3B8"));
        sub.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        sub.setPadding(0, dp(6), 0, dp(12));
        root.addView(sub);

        summary = new TextView(this);
        summary.setTextColor(Color.parseColor("#E2E8F0"));
        summary.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        root.addView(summary);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setPadding(0, dp(8), 0, dp(8));

        Button refresh = new Button(this);
        refresh.setText("Refresh");
        refresh.setOnClickListener(v -> render());
        actions.addView(refresh);

        Button clear = new Button(this);
        clear.setText("Clear");
        clear.setOnClickListener(v -> {
            OriginCallLog.clear();
            render();
        });
        actions.addView(clear);

        Button back = new Button(this);
        back.setText("Back");
        back.setOnClickListener(v -> finish());
        actions.addView(back);

        root.addView(actions);

        ScrollView scroll = new ScrollView(this);
        listHost = new LinearLayout(this);
        listHost.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(listHost);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
        root.addView(scroll, lp);

        setContentView(root);
        render();
    }

    @Override
    protected void onResume() {
        super.onResume();
        render();
    }

    private void render() {
        ArrayList<OriginCallLog.Entry> entries = OriginCallLog.snapshot();
        summary.setText("origin " + OriginCallLog.originCount()
                + " · edge " + OriginCallLog.edgeCount()
                + " · rows " + entries.size());

        listHost.removeAllViews();
        if (entries.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("No API calls logged yet. Open News / Daily Updates first.");
            empty.setTextColor(Color.parseColor("#94A3B8"));
            listHost.addView(empty);
            return;
        }
        // Newest first
        for (int i = entries.size() - 1; i >= 0; i--) {
            OriginCallLog.Entry e = entries.get(i);
            TextView row = new TextView(this);
            row.setText(e.line());
            row.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            row.setTypeface(Typeface.MONOSPACE);
            row.setPadding(0, dp(4), 0, dp(4));
            if ("origin".equals(e.kind)) {
                row.setTextColor(Color.parseColor("#FCA5A5"));
            } else {
                row.setTextColor(Color.parseColor("#86EFAC"));
            }
            listHost.addView(row);
        }
    }

    private int dp(int v) {
        float d = getResources().getDisplayMetrics().density;
        return Math.round(v * d);
    }
}
