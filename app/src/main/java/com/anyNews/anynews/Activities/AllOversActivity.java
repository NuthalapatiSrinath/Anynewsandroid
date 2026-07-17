package com.anyNews.anynews.Activities;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.anyNews.anynews.Pojo.CricketMatchDetailResponse;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * "All Overs" screen — the full over-by-over breakdown for a match: over number,
 * bowler, each ball outcome (with W/4/6 colour badges), the over run total /
 * cumulative score, and wicket rows ("W — batsman").
 *
 * Data comes from /app/v3/cricket_match_detail (detail.all_overs), which the
 * backend keeps in a persisted snapshot so this works AFTER the match ends too.
 * Edge-to-edge handling mirrors MatchCenterActivity (status-bar inset added to
 * the orange header's top padding; white status-bar icons).
 */
public class AllOversActivity extends AppCompatActivity {

    public static final String EXTRA_CREX_KEY = "extra_crex_key";
    public static final String EXTRA_TITLE = "extra_title";

    private LinearLayout container;
    private String crexKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_overs);

        applyWindowInsets();

        crexKey = getIntent() != null ? getIntent().getStringExtra(EXTRA_CREX_KEY) : null;
        String title = getIntent() != null ? getIntent().getStringExtra(EXTRA_TITLE) : null;

        container = findViewById(R.id.ao_container);
        TextView subtitle = findViewById(R.id.ao_subtitle);
        if (!TextUtils.isEmpty(title)) subtitle.setText(title);

        findViewById(R.id.ao_back).setOnClickListener(v -> finish());

        showMessage("Loading overs…");
        fetchDetail();
    }

    private void applyWindowInsets() {
        final View header = findViewById(R.id.ao_header);
        final View scroll = findViewById(R.id.ao_scroll);
        final int headerPadTop = header.getPaddingTop();
        final int scrollPadBottom = scroll.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.ao_root), (v, insets) -> {
            int top = insets.getSystemWindowInsetTop();
            int bottom = insets.getSystemWindowInsetBottom();
            header.setPadding(header.getPaddingLeft(), headerPadTop + top,
                    header.getPaddingRight(), header.getPaddingBottom());
            scroll.setPadding(scroll.getPaddingLeft(), scroll.getPaddingTop(),
                    scroll.getPaddingRight(), scrollPadBottom + bottom);
            return insets;
        });

        WindowInsetsControllerCompat controller = ViewCompat.getWindowInsetsController(header);
        if (controller != null) {
            controller.setAppearanceLightStatusBars(false);
        }
        ViewCompat.requestApplyInsets(findViewById(R.id.ao_root));
    }

    private void fetchDetail() {
        if (TextUtils.isEmpty(crexKey)) {
            showMessage("Over-by-over data is not available for this match.");
            return;
        }
        try {
            APIService api = RestApiClient.getClient().create(APIService.class);
            api.cricketMatchDetail("v3", crexKey).enqueue(new Callback<CricketMatchDetailResponse>() {
                @Override
                public void onResponse(@NonNull Call<CricketMatchDetailResponse> call,
                                       @NonNull Response<CricketMatchDetailResponse> response) {
                    if (isFinishing() || response.body() == null) return;
                    if (response.body().getSuccess() != null && response.body().getSuccess()
                            && response.body().getData() != null) {
                        render(response.body().getData());
                    } else {
                        showMessage("Over-by-over data is not available for this match.");
                    }
                }

                @Override
                public void onFailure(@NonNull Call<CricketMatchDetailResponse> call, @NonNull Throwable t) {
                    if (!isFinishing()) showMessage("Couldn't load overs. Check your connection.");
                }
            });
        } catch (Exception e) {
            showMessage("Over-by-over data is not available for this match.");
        }
    }

    private void render(CricketMatchDetailResponse.Detail data) {
        container.removeAllViews();
        List<CricketMatchDetailResponse.Over> overs = data != null ? data.getAll_overs() : null;
        if (overs == null || overs.isEmpty()) {
            showMessage("Over-by-over data is not available for this match.");
            return;
        }

        // Map innings number → readable label from the scorecard (e.g.
        // "India 1st Inn"), so each innings group has a proper heading.
        Map<Integer, String> inningLabels = new LinkedHashMap<>();
        if (data.getScorecards() != null) {
            for (CricketMatchDetailResponse.Innings inn : data.getScorecards()) {
                String label = !TextUtils.isEmpty(inn.getLabel()) ? inn.getLabel() : inn.getTeam_name();
                if (!TextUtils.isEmpty(label)) inningLabels.put(inn.innings(), label);
            }
        }

        // Group overs by innings, preserving innings order (ascending). Within
        // each innings, list every over newest-first like the reference.
        Map<Integer, List<CricketMatchDetailResponse.Over>> byInnings = new LinkedHashMap<>();
        for (CricketMatchDetailResponse.Over o : overs) {
            int inn = o.getInning();
            List<CricketMatchDetailResponse.Over> list = byInnings.get(inn);
            if (list == null) {
                list = new ArrayList<>();
                byInnings.put(inn, list);
            }
            list.add(o);
        }

        for (Map.Entry<Integer, List<CricketMatchDetailResponse.Over>> entry : byInnings.entrySet()) {
            int inn = entry.getKey();
            String label = inningLabels.get(inn);
            if (TextUtils.isEmpty(label)) {
                label = inn > 0 ? ordinal(inn) + " Innings" : "Overs";
            }
            container.addView(buildInningsHeader(label, entry.getValue().size()));
            List<CricketMatchDetailResponse.Over> list = entry.getValue();
            for (int i = list.size() - 1; i >= 0; i--) {
                container.addView(buildOverCard(list.get(i)));
            }
        }
    }

    private static String ordinal(int n) {
        switch (n) {
            case 1: return "1st";
            case 2: return "2nd";
            case 3: return "3rd";
            default: return n + "th";
        }
    }

    private View buildInningsHeader(String label, int overCount) {
        TextView t = new TextView(this);
        t.setText(label + "  ·  " + overCount + (overCount == 1 ? " over" : " overs"));
        t.setTextColor(ContextCompat.getColor(this, R.color.cmp_brand_orange));
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setPadding(dp(2), dp(14), dp(2), dp(8));
        return t;
    }

    private View buildOverCard(CricketMatchDetailResponse.Over over) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.bg_cmp_card);
        card.setPadding(dp(12), dp(12), dp(12), dp(12));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(10);
        card.setLayoutParams(lp);

        // Header row: "Over 18" + bowler ....... runs / score
        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout headLeft = new LinearLayout(this);
        headLeft.setOrientation(LinearLayout.VERTICAL);
        headLeft.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView overLabel = new TextView(this);
        overLabel.setText(over.getOver_label());
        overLabel.setTextColor(ContextCompat.getColor(this, R.color.cmp_text_primary));
        overLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        overLabel.setTypeface(Typeface.DEFAULT_BOLD);
        headLeft.addView(overLabel);

        if (!TextUtils.isEmpty(over.getBowler())) {
            TextView bowler = new TextView(this);
            bowler.setText(over.getBowler());
            bowler.setTextColor(ContextCompat.getColor(this, R.color.cmp_text_secondary));
            bowler.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            headLeft.addView(bowler);
        }
        head.addView(headLeft);

        LinearLayout headRight = new LinearLayout(this);
        headRight.setOrientation(LinearLayout.VERTICAL);
        headRight.setGravity(Gravity.END);

        if (!TextUtils.isEmpty(over.getRuns())) {
            TextView runs = new TextView(this);
            runs.setText(over.getRuns() + " runs");
            runs.setTextColor(ContextCompat.getColor(this, R.color.cmp_brand_orange));
            runs.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            runs.setTypeface(Typeface.DEFAULT_BOLD);
            runs.setGravity(Gravity.END);
            headRight.addView(runs);
        }
        if (!TextUtils.isEmpty(over.getScore())) {
            TextView score = new TextView(this);
            score.setText(over.getScore());
            score.setTextColor(ContextCompat.getColor(this, R.color.cmp_text_secondary));
            score.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            score.setGravity(Gravity.END);
            headRight.addView(score);
        }
        head.addView(headRight);
        card.addView(head);

        // Ball badges row.
        List<CricketMatchDetailResponse.Ball> balls = over.getBalls();
        if (balls != null && !balls.isEmpty()) {
            LinearLayout ballsRow = new LinearLayout(this);
            ballsRow.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams brLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            brLp.topMargin = dp(10);
            ballsRow.setLayoutParams(brLp);
            ballsRow.setPadding(0, 0, 0, 0);
            android.widget.HorizontalScrollView hsv = new android.widget.HorizontalScrollView(this);
            hsv.setHorizontalScrollBarEnabled(false);
            LinearLayout inner = new LinearLayout(this);
            inner.setOrientation(LinearLayout.HORIZONTAL);
            for (CricketMatchDetailResponse.Ball b : balls) {
                inner.addView(ballBadge(b));
            }
            hsv.addView(inner);
            ballsRow.addView(hsv);
            card.addView(ballsRow);

            // Wicket rows ("W — batsman").
            for (CricketMatchDetailResponse.Ball b : balls) {
                if (b.isWicket() && !TextUtils.isEmpty(b.getWicket_batsman())) {
                    TextView w = new TextView(this);
                    w.setText("W — " + b.getWicket_batsman());
                    w.setTextColor(ContextCompat.getColor(this, R.color.cmp_live_red));
                    w.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                    w.setTypeface(Typeface.DEFAULT_BOLD);
                    w.setPadding(0, dp(8), 0, 0);
                    card.addView(w);
                }
            }
        }

        return card;
    }

    private TextView ballBadge(CricketMatchDetailResponse.Ball b) {
        TextView badge = new TextView(this);
        badge.setGravity(Gravity.CENTER);
        badge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        badge.setTypeface(Typeface.DEFAULT_BOLD);
        badge.setText(b.isWicket() ? "W" : (TextUtils.isEmpty(b.getValue()) ? "0" : b.getValue()));

        int size = dp(30);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
        lp.rightMargin = dp(6);
        badge.setLayoutParams(lp);

        if (b.isWicket()) {
            badge.setTextColor(0xFFFFFFFF);
            badge.setBackgroundResource(R.drawable.bg_over_ball_red);
        } else if (b.isFour() || b.isSix()) {
            badge.setTextColor(0xFFFFFFFF);
            badge.setBackgroundResource(R.drawable.bg_over_ball_orange);
        } else {
            badge.setTextColor(ContextCompat.getColor(this, R.color.cmp_text_primary));
            badge.setBackgroundResource(R.drawable.bg_over_ball_grey);
        }
        return badge;
    }

    private void showMessage(String message) {
        container.removeAllViews();
        TextView t = new TextView(this);
        t.setText(message);
        t.setTextColor(ContextCompat.getColor(this, R.color.cmp_text_secondary));
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        t.setGravity(Gravity.CENTER);
        t.setPadding(0, dp(48), 0, 0);
        container.addView(t);
    }

    private int dp(float v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
