package com.anyNews.anynews.Activities;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import com.anyNews.anynews.Pojo.ActiveMatch;
import com.anyNews.anynews.Pojo.CricketActiveMatchResponse;
import com.anyNews.anynews.Pojo.CricketMatchDetailResponse;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Cricket Match Center — full scoreboard + tabs (Live / Scorecard / Commentary /
 * Stats). Opened when the user taps the floating mini player.
 *
 * The header scoreboard comes from the active_match contract; the Scorecard,
 * Commentary and Stats tabs fetch real per-ball detail from the app-facing
 * /app/v3/cricket_match_detail endpoint and render batting/bowling tables,
 * ball-by-ball commentary and live stats. Sections with no data from CREX show
 * a clean empty state.
 */
public class MatchCenterActivity extends AppCompatActivity {

    public static final String EXTRA_MATCH = "extra_active_match";

    private static final int DETAIL_POLL_MS = 8000;

    private ActiveMatch match;
    private CricketMatchDetailResponse.Detail detail;

    private TextView tabLive, tabScorecard, tabCommentary, tabStats;
    private View contentLive, contentPlaceholder;
    private LinearLayout contentScorecard, contentCommentary, contentStats;
    private LinearLayout liveDynamic;
    private TextView placeholderTitle, placeholderMessage;

    private int currentTab = 0;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable detailPoll = new Runnable() {
        @Override
        public void run() {
            fetchDetail();
            handler.postDelayed(this, DETAIL_POLL_MS);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_center);

        applyWindowInsets();

        Object extra = getIntent() != null ? getIntent().getSerializableExtra(EXTRA_MATCH) : null;
        if (extra instanceof ActiveMatch) {
            match = (ActiveMatch) extra;
        }

        findViewById(R.id.mc_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        tabLive = findViewById(R.id.tab_live);
        tabScorecard = findViewById(R.id.tab_scorecard);
        tabCommentary = findViewById(R.id.tab_commentary);
        tabStats = findViewById(R.id.tab_stats);
        contentLive = findViewById(R.id.content_live);
        liveDynamic = findViewById(R.id.live_dynamic);
        contentScorecard = findViewById(R.id.content_scorecard);
        contentCommentary = findViewById(R.id.content_commentary);
        contentStats = findViewById(R.id.content_stats);
        contentPlaceholder = findViewById(R.id.content_placeholder);
        placeholderTitle = findViewById(R.id.placeholder_title);
        placeholderMessage = findViewById(R.id.placeholder_message);

        tabLive.setOnClickListener(v -> selectTab(0));
        tabScorecard.setOnClickListener(v -> selectTab(1));
        tabCommentary.setOnClickListener(v -> selectTab(2));
        tabStats.setOnClickListener(v -> selectTab(3));

        bind();
        selectTab(0);

        refresh();
        fetchDetail();
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.removeCallbacks(detailPoll);
        handler.postDelayed(detailPoll, DETAIL_POLL_MS);
    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(detailPoll);
        super.onPause();
    }

    /**
     * targetSdk 35 forces edge-to-edge, so add the status-bar inset to the
     * header's top padding (its orange background then fills behind the status
     * bar) and the navigation-bar inset to the scroll content bottom. White
     * status-bar icons read cleanly over the orange header.
     */
    private void applyWindowInsets() {
        final View header = findViewById(R.id.mc_header);
        final View scroll = findViewById(R.id.mc_scroll);
        final int headerPadTop = header.getPaddingTop();
        final int scrollPadBottom = scroll.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mc_root), (v, insets) -> {
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
        ViewCompat.requestApplyInsets(findViewById(R.id.mc_root));
    }

    private void bind() {
        if (match == null) return;

        TextView series = findViewById(R.id.mc_series);
        TextView title = findViewById(R.id.mc_title);
        TextView t1code = findViewById(R.id.mc_team1_code);
        TextView t1score = findViewById(R.id.mc_team1_score);
        TextView t2code = findViewById(R.id.mc_team2_code);
        TextView t2score = findViewById(R.id.mc_team2_score);
        TextView status = findViewById(R.id.mc_status);
        TextView venue = findViewById(R.id.mc_venue);
        TextView liveBadge = findViewById(R.id.mc_live_badge);

        series.setText(match.getSeries_name());
        String heading = !TextUtils.isEmpty(match.getMatch_title())
                ? match.getMatch_title()
                : match.getTeam1_code() + " vs " + match.getTeam2_code();
        title.setText(heading);

        t1code.setText(fullTeam(match.getTeam1_code(), match.getTeam1_name()));
        t2code.setText(fullTeam(match.getTeam2_code(), match.getTeam2_name()));
        t1score.setText(scoreOrDash(match.getTeam1_score(), match.getTeam1_overs()));
        t2score.setText(scoreOrDash(match.getTeam2_score(), match.getTeam2_overs()));

        status.setText(TextUtils.isEmpty(match.getStatus_text()) ? "" : match.getStatus_text());
        if (TextUtils.isEmpty(match.getVenue())) {
            venue.setVisibility(View.GONE);
        } else {
            venue.setVisibility(View.VISIBLE);
            venue.setText(match.getVenue());
        }

        liveBadge.setVisibility(match.isLive() ? View.VISIBLE : View.GONE);

        TextView liveStatus = findViewById(R.id.live_status_text);
        TextView liveLastBall = findViewById(R.id.live_last_ball);
        TextView liveResult = findViewById(R.id.live_result);
        liveStatus.setText(TextUtils.isEmpty(match.getStatus_text()) ? "Live" : match.getStatus_text());
        liveLastBall.setText(TextUtils.isEmpty(match.getLast_ball()) ? "—" : match.getLast_ball());
        if (TextUtils.isEmpty(match.getResult())) {
            liveResult.setVisibility(View.GONE);
        } else {
            liveResult.setVisibility(View.VISIBLE);
            liveResult.setText(match.getResult());
        }
    }

    private void selectTab(int index) {
        currentTab = index;
        int active = ContextCompat.getColor(this, R.color.cmp_brand_orange);
        int inactive = ContextCompat.getColor(this, R.color.cmp_text_secondary);
        tabLive.setTextColor(index == 0 ? active : inactive);
        tabScorecard.setTextColor(index == 1 ? active : inactive);
        tabCommentary.setTextColor(index == 2 ? active : inactive);
        tabStats.setTextColor(index == 3 ? active : inactive);

        contentLive.setVisibility(index == 0 ? View.VISIBLE : View.GONE);
        contentScorecard.setVisibility(index == 1 ? View.VISIBLE : View.GONE);
        contentCommentary.setVisibility(index == 2 ? View.VISIBLE : View.GONE);
        contentStats.setVisibility(index == 3 ? View.VISIBLE : View.GONE);
        contentPlaceholder.setVisibility(View.GONE);

        renderCurrentTab();
    }

    private void renderCurrentTab() {
        switch (currentTab) {
            case 1:
                renderScorecard();
                break;
            case 2:
                renderCommentary();
                break;
            case 3:
                renderStats();
                break;
            default:
                renderLive();
                break;
        }
    }

    private void showEmptyFor(String title, String message) {
        contentLive.setVisibility(View.GONE);
        contentScorecard.setVisibility(View.GONE);
        contentCommentary.setVisibility(View.GONE);
        contentStats.setVisibility(View.GONE);
        contentPlaceholder.setVisibility(View.VISIBLE);
        placeholderTitle.setText(title);
        placeholderMessage.setText(message);
    }

    // ---- Live ---------------------------------------------------------------

    /**
     * Live tab: current/last batsmen, recent over-by-over ball strips (Over 18,
     * 19, 20 …) and an "All Overs" button — all kept visible after the match
     * ends (the backend serves a persisted snapshot). Player of the match /
     * series is shown when CREX exposes it.
     */
    private void renderLive() {
        contentPlaceholder.setVisibility(View.GONE);
        contentLive.setVisibility(View.VISIBLE);
        liveDynamic.removeAllViews();
        if (detail == null) return;

        CricketMatchDetailResponse.Live live = detail.getLive();

        // Batsmen status.
        if (live != null && live.getBatsmen() != null && !live.getBatsmen().isEmpty()) {
            liveDynamic.addView(sectionDivider());
            liveDynamic.addView(sectionTitle("Batsmen"));
            for (CricketMatchDetailResponse.Batsman b : live.getBatsmen()) {
                liveDynamic.addView(buildBatsmanRow(b));
            }
        }

        // Recent over strips.
        List<CricketMatchDetailResponse.Over> overs = detail.getAll_overs();
        if (overs != null && !overs.isEmpty()) {
            liveDynamic.addView(sectionDivider());
            LinearLayout headRow = new LinearLayout(this);
            headRow.setOrientation(LinearLayout.HORIZONTAL);
            headRow.setGravity(Gravity.CENTER_VERTICAL);
            TextView title = sectionTitle("Recent overs");
            title.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            headRow.addView(title);
            headRow.addView(buildAllOversButton());
            liveDynamic.addView(headRow);

            int from = Math.max(0, overs.size() - 3);
            for (int i = from; i < overs.size(); i++) {
                liveDynamic.addView(buildOverStrip(overs.get(i)));
            }
        }

        // Player of the match / series (graceful — only when CREX provides it).
        renderPlayerOf(detail.getPlayer_of_match());
        renderPlayerOf(detail.getPlayer_of_series());
    }

    private void renderPlayerOf(CricketMatchDetailResponse.PlayerOf player) {
        if (player == null || TextUtils.isEmpty(player.getName())) return;
        liveDynamic.addView(sectionDivider());
        liveDynamic.addView(labelLine(TextUtils.isEmpty(player.getLabel()) ? "Player of the Match" : player.getLabel()));
        TextView t = new TextView(this);
        t.setText(player.getName());
        t.setTextColor(ContextCompat.getColor(this, R.color.cmp_text_primary));
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        liveDynamic.addView(t);
    }

    private View buildBatsmanRow(CricketMatchDetailResponse.Batsman b) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(5), 0, dp(5));

        TextView name = new TextView(this);
        name.setText(b.getName() + (b.isOn_strike() ? "  *" : ""));
        name.setTextColor(ContextCompat.getColor(this, R.color.cmp_text_primary));
        name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        name.setTypeface(b.isOn_strike() ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        name.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(name);

        TextView score = new TextView(this);
        String balls = b.getBalls().replaceAll("[()]", "");
        score.setText(b.getRuns() + (TextUtils.isEmpty(balls) ? "" : " (" + balls + ")"));
        score.setTextColor(ContextCompat.getColor(this, R.color.cmp_text_primary));
        score.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        score.setTypeface(Typeface.DEFAULT_BOLD);
        row.addView(score);
        return row;
    }

    private View buildAllOversButton() {
        TextView btn = new TextView(this);
        btn.setText("All Overs");
        btn.setTextColor(ContextCompat.getColor(this, R.color.cmp_brand_orange));
        btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        btn.setTypeface(Typeface.DEFAULT_BOLD);
        btn.setPadding(dp(10), dp(6), dp(4), dp(6));
        btn.setOnClickListener(v -> openAllOvers());
        return btn;
    }

    private void openAllOvers() {
        if (match == null) return;
        final String key = !TextUtils.isEmpty(match.getCrex_key()) ? match.getCrex_key() : match.getMatch_id();
        if (TextUtils.isEmpty(key)) return;
        android.content.Intent intent = new android.content.Intent(this, AllOversActivity.class);
        intent.putExtra(AllOversActivity.EXTRA_CREX_KEY, key);
        String title = !TextUtils.isEmpty(match.getMatch_title())
                ? match.getMatch_title()
                : match.getTeam1_code() + " vs " + match.getTeam2_code();
        intent.putExtra(AllOversActivity.EXTRA_TITLE, title);
        startActivity(intent);
    }

    private View buildOverStrip(CricketMatchDetailResponse.Over over) {
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setPadding(0, dp(8), 0, dp(4));

        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(Gravity.CENTER_VERTICAL);

        TextView label = new TextView(this);
        String labelText = over.getOver_label();
        if (!TextUtils.isEmpty(over.getBowler())) labelText = labelText + " · " + over.getBowler();
        label.setText(labelText);
        label.setTextColor(ContextCompat.getColor(this, R.color.cmp_text_secondary));
        label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        label.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        head.addView(label);

        if (!TextUtils.isEmpty(over.getRuns())) {
            TextView total = new TextView(this);
            total.setText(over.getRuns() + " runs");
            total.setTextColor(ContextCompat.getColor(this, R.color.cmp_text_secondary));
            total.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            total.setTypeface(Typeface.DEFAULT_BOLD);
            head.addView(total);
        }
        wrap.addView(head);

        List<CricketMatchDetailResponse.Ball> balls = over.getBalls();
        if (balls != null && !balls.isEmpty()) {
            android.widget.HorizontalScrollView hsv = new android.widget.HorizontalScrollView(this);
            hsv.setHorizontalScrollBarEnabled(false);
            LinearLayout inner = new LinearLayout(this);
            inner.setOrientation(LinearLayout.HORIZONTAL);
            inner.setPadding(0, dp(6), 0, 0);
            for (CricketMatchDetailResponse.Ball b : balls) {
                inner.addView(ballBadge(b));
            }
            hsv.addView(inner);
            wrap.addView(hsv);
        }
        return wrap;
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

    private View sectionDivider() {
        View v = new View(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(1));
        lp.topMargin = dp(14);
        lp.bottomMargin = dp(10);
        v.setLayoutParams(lp);
        v.setBackgroundColor(ContextCompat.getColor(this, R.color.cmp_border));
        return v;
    }

    // ---- Scorecard ----------------------------------------------------------

    private void renderScorecard() {
        contentScorecard.removeAllViews();
        List<CricketMatchDetailResponse.Innings> innings = detail != null ? detail.getScorecards() : null;
        if (innings == null || innings.isEmpty()) {
            showEmptyFor("Scorecard", detail == null
                    ? "Loading scorecard…"
                    : "The full batting & bowling scorecard will appear here once the match is under way.");
            return;
        }
        for (CricketMatchDetailResponse.Innings inn : innings) {
            contentScorecard.addView(buildInningsCard(inn));
        }
        contentPlaceholder.setVisibility(View.GONE);
        contentScorecard.setVisibility(View.VISIBLE);
    }

    private View buildInningsCard(CricketMatchDetailResponse.Innings inn) {
        LinearLayout card = card();

        String header = inn.getLabel();
        if (TextUtils.isEmpty(header)) header = inn.getTeam_name();
        if (!TextUtils.isEmpty(inn.getScore())) header = header + " — " + inn.getScore();
        card.addView(sectionTitle(header + (inn.isCurrent() ? "  ●" : "")));

        // Batting table.
        card.addView(tableRow(true, "Batter", "R", "B", "4s", "6s", "SR"));
        List<CricketMatchDetailResponse.Batting> batting = inn.getBatting();
        boolean anyBatter = false;
        if (batting != null) {
            for (CricketMatchDetailResponse.Batting b : batting) {
                if (b.isDid_not_bat()) continue;
                anyBatter = true;
                LinearLayout row = tableRow(false, b.getName(),
                        String.valueOf(b.getRuns()), String.valueOf(b.getBalls()),
                        String.valueOf(b.getFours()), String.valueOf(b.getSixes()), b.getStrike_rate());
                String note = "batting".equalsIgnoreCase(b.getStatus())
                        ? "batting"
                        : b.getDismissal();
                if (!TextUtils.isEmpty(note)) {
                    TextView sub = new TextView(this);
                    sub.setText(note);
                    sub.setTextColor(ContextCompat.getColor(this, R.color.cmp_text_secondary));
                    sub.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
                    sub.setPadding(0, 0, 0, dp(4));
                    card.addView(row);
                    card.addView(sub);
                    continue;
                }
                card.addView(row);
            }
        }
        if (!anyBatter) {
            card.addView(mutedLine("Yet to bat."));
        }

        if (!TextUtils.isEmpty(inn.getExtras())) {
            card.addView(mutedLine("Extras  " + inn.getExtras()));
        }

        // Bowling table.
        List<CricketMatchDetailResponse.Bowling> bowling = inn.getBowling();
        if (bowling != null && !bowling.isEmpty()) {
            card.addView(divider());
            card.addView(tableRow(true, "Bowler", "O", "R", "W", "Econ", ""));
            for (CricketMatchDetailResponse.Bowling bw : bowling) {
                card.addView(tableRow(false, bw.getName(), bw.getOvers(),
                        String.valueOf(bw.getRuns()), String.valueOf(bw.getWickets()), bw.getEconomy(), ""));
            }
        }

        // Fall of wickets.
        List<CricketMatchDetailResponse.FallOfWicket> fow = inn.getFall_of_wickets();
        if (fow != null && !fow.isEmpty()) {
            card.addView(divider());
            card.addView(labelLine("Fall of wickets"));
            StringBuilder sb = new StringBuilder();
            for (CricketMatchDetailResponse.FallOfWicket f : fow) {
                if (sb.length() > 0) sb.append("   ");
                sb.append(f.getScore());
                if (!TextUtils.isEmpty(f.getBatsman())) sb.append(" (").append(f.getBatsman()).append(')');
            }
            card.addView(mutedLine(sb.toString()));
        }

        return card;
    }

    // ---- Commentary ---------------------------------------------------------

    private void renderCommentary() {
        contentCommentary.removeAllViews();
        List<CricketMatchDetailResponse.Commentary> items = detail != null ? detail.getCommentary() : null;
        if (items == null || items.isEmpty()) {
            showEmptyFor("Commentary", detail == null
                    ? "Loading commentary…"
                    : "Ball-by-ball commentary will appear here while the match is live.");
            return;
        }
        for (CricketMatchDetailResponse.Commentary c : items) {
            contentCommentary.addView(buildCommentaryRow(c));
        }
        contentPlaceholder.setVisibility(View.GONE);
        contentCommentary.setVisibility(View.VISIBLE);
    }

    private View buildCommentaryRow(CricketMatchDetailResponse.Commentary c) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(dp(10), dp(10), dp(10), dp(10));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(8);
        row.setLayoutParams(lp);
        row.setBackgroundResource(R.drawable.bg_cmp_card);

        if (c.isOver()) {
            row.setOrientation(LinearLayout.VERTICAL);
            TextView t = new TextView(this);
            String head = TextUtils.isEmpty(c.getTitle()) ? ("OVER " + c.getOver()) : c.getTitle();
            if (!TextUtils.isEmpty(c.getScore())) head = head + "   " + c.getTeam() + " " + c.getScore();
            t.setText(head.trim());
            t.setTextColor(ContextCompat.getColor(this, R.color.cmp_text_primary));
            t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            t.setTypeface(Typeface.DEFAULT_BOLD);
            row.addView(t);
            return row;
        }

        TextView badge = new TextView(this);
        badge.setWidth(dp(38));
        badge.setGravity(Gravity.CENTER);
        badge.setPadding(dp(2), dp(4), dp(2), dp(4));
        badge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        badge.setTypeface(Typeface.DEFAULT_BOLD);
        String ball = c.isWicket() ? "W" : (TextUtils.isEmpty(c.getRuns()) ? "•" : c.getRuns());
        badge.setText(ball);
        if (c.isWicket()) {
            badge.setTextColor(0xFFFFFFFF);
            badge.setBackgroundColor(ContextCompat.getColor(this, R.color.cmp_live_red));
        } else if (c.isFour() || c.isSix()) {
            badge.setTextColor(0xFFFFFFFF);
            badge.setBackgroundColor(ContextCompat.getColor(this, R.color.cmp_brand_orange));
        } else {
            badge.setTextColor(ContextCompat.getColor(this, R.color.cmp_text_secondary));
        }
        LinearLayout.LayoutParams badgeLp = new LinearLayout.LayoutParams(dp(38), ViewGroup.LayoutParams.WRAP_CONTENT);
        badgeLp.rightMargin = dp(10);
        badge.setLayoutParams(badgeLp);
        row.addView(badge);

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView over = new TextView(this);
        over.setText(TextUtils.isEmpty(c.getOver()) ? "" : ("Over " + c.getOver()));
        over.setTextColor(ContextCompat.getColor(this, R.color.cmp_text_secondary));
        over.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        col.addView(over);

        TextView title = new TextView(this);
        String head = !TextUtils.isEmpty(c.getTitle()) ? c.getTitle() : c.getText();
        title.setText(head);
        title.setTextColor(ContextCompat.getColor(this, R.color.cmp_text_primary));
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        col.addView(title);

        if (!TextUtils.isEmpty(c.getDescription()) && !c.getDescription().equals(head)) {
            TextView desc = new TextView(this);
            desc.setText(c.getDescription());
            desc.setTextColor(ContextCompat.getColor(this, R.color.cmp_text_secondary));
            desc.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            desc.setPadding(0, dp(2), 0, 0);
            col.addView(desc);
        }

        row.addView(col);
        return row;
    }

    // ---- Stats --------------------------------------------------------------

    private void renderStats() {
        contentStats.removeAllViews();
        if (detail == null) {
            showEmptyFor("Stats", "Loading stats…");
            return;
        }
        CricketMatchDetailResponse.Live live = detail.getLive();
        CricketMatchDetailResponse.Info info = detail.getInfo();
        boolean any = false;

        LinearLayout live_card = card();
        if (live != null) {
            if (!TextUtils.isEmpty(live.getSession_label())) {
                live_card.addView(keyValue("Session", live.getSession_label()));
                any = true;
            }
            if (!TextUtils.isEmpty(live.getCurrent_run_rate())) {
                live_card.addView(keyValue("Current run rate", live.getCurrent_run_rate()));
                any = true;
            }
            if (!TextUtils.isEmpty(live.getRequired_rate())) {
                live_card.addView(keyValue("Required run rate", live.getRequired_rate()));
                any = true;
            }
            if (live.getPartnership() != null && !TextUtils.isEmpty(live.getPartnership().getText())) {
                live_card.addView(keyValue("Partnership", live.getPartnership().getText()));
                any = true;
            }
            if (live.getLast_wicket() != null && !TextUtils.isEmpty(live.getLast_wicket().getText())) {
                live_card.addView(keyValue("Last wicket", live.getLast_wicket().getText()));
                any = true;
            }
        }
        if (live_card.getChildCount() > 0) {
            live_card.addView(sectionTitle("Live stats"), 0);
            contentStats.addView(live_card);
        }

        // Win probability bar.
        if (live != null && live.getWin_probability() != null) {
            CricketMatchDetailResponse.WinProbability wp = live.getWin_probability();
            int p1 = wp.getTeam1() != null ? wp.getTeam1().getPct() : 0;
            int p2 = wp.getTeam2() != null ? wp.getTeam2().getPct() : 0;
            if (p1 > 0 || p2 > 0) {
                LinearLayout wpCard = card();
                wpCard.addView(sectionTitle("Win probability"));
                wpCard.addView(winProbBar(wp));
                contentStats.addView(wpCard);
                any = true;
            }
        }

        // Match info.
        LinearLayout infoCard = card();
        if (info != null) {
            if (!TextUtils.isEmpty(info.getSeries())) infoCard.addView(keyValue("Series", info.getSeries()));
            if (!TextUtils.isEmpty(info.getFormat())) infoCard.addView(keyValue("Format", info.getFormat()));
            if (!TextUtils.isEmpty(info.getVenue())) infoCard.addView(keyValue("Venue", info.getVenue()));
            if (!TextUtils.isEmpty(info.getToss())) infoCard.addView(keyValue("Toss", info.getToss()));
        }
        if (infoCard.getChildCount() > 0) {
            infoCard.addView(sectionTitle("Match info"), 0);
            contentStats.addView(infoCard);
            any = true;
        }

        if (!any) {
            showEmptyFor("Stats", "Match stats will appear here once the match is live.");
        } else {
            contentPlaceholder.setVisibility(View.GONE);
            contentStats.setVisibility(View.VISIBLE);
        }
    }

    private View winProbBar(CricketMatchDetailResponse.WinProbability wp) {
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);

        int p1 = wp.getTeam1() != null ? wp.getTeam1().getPct() : 0;
        int p2 = wp.getTeam2() != null ? wp.getTeam2().getPct() : 0;
        int pd = wp.getDraw() != null ? wp.getDraw().getPct() : 0;

        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams barLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(10));
        barLp.topMargin = dp(6);
        barLp.bottomMargin = dp(6);
        bar.setLayoutParams(barLp);
        bar.addView(barSegment(p1, ContextCompat.getColor(this, R.color.cmp_brand_orange)));
        if (pd > 0) bar.addView(barSegment(pd, ContextCompat.getColor(this, R.color.cmp_text_secondary)));
        bar.addView(barSegment(p2, 0xFF2563EB));
        wrap.addView(bar);

        StringBuilder legend = new StringBuilder();
        if (wp.getTeam1() != null) legend.append(wp.getTeam1().getLabel()).append(' ').append(p1).append('%');
        if (wp.getTeam2() != null) legend.append("   ").append(wp.getTeam2().getLabel()).append(' ').append(p2).append('%');
        wrap.addView(mutedLine(legend.toString()));
        return wrap;
    }

    private View barSegment(int weight, int color) {
        View v = new View(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, Math.max(weight, 0));
        v.setLayoutParams(lp);
        v.setBackgroundColor(color);
        return v;
    }

    // ---- View builders ------------------------------------------------------

    private LinearLayout card() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.bg_cmp_card);
        card.setPadding(dp(12), dp(12), dp(12), dp(12));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(12);
        card.setLayoutParams(lp);
        return card;
    }

    private TextView sectionTitle(String text) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextColor(ContextCompat.getColor(this, R.color.cmp_text_primary));
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setPadding(0, 0, 0, dp(8));
        return t;
    }

    private TextView labelLine(String text) {
        TextView t = new TextView(this);
        t.setText(text.toUpperCase());
        t.setTextColor(ContextCompat.getColor(this, R.color.cmp_text_secondary));
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setPadding(0, dp(2), 0, dp(4));
        return t;
    }

    private TextView mutedLine(String text) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextColor(ContextCompat.getColor(this, R.color.cmp_text_secondary));
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        t.setPadding(0, dp(4), 0, dp(2));
        return t;
    }

    private View divider() {
        View v = new View(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(1));
        lp.topMargin = dp(8);
        lp.bottomMargin = dp(8);
        v.setLayoutParams(lp);
        v.setBackgroundColor(ContextCompat.getColor(this, R.color.cmp_border));
        return v;
    }

    private View keyValue(String key, String value) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(5), 0, dp(5));

        TextView k = new TextView(this);
        k.setText(key);
        k.setTextColor(ContextCompat.getColor(this, R.color.cmp_text_secondary));
        k.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        k.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(k);

        TextView val = new TextView(this);
        val.setText(value);
        val.setTextColor(ContextCompat.getColor(this, R.color.cmp_text_primary));
        val.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        val.setTypeface(Typeface.DEFAULT_BOLD);
        val.setGravity(Gravity.END);
        val.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.4f));
        row.addView(val);
        return row;
    }

    private LinearLayout tableRow(boolean header, String c0, String c1, String c2, String c3, String c4, String c5) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(6), 0, dp(6));

        row.addView(cell(c0, 4f, Gravity.START, header));
        row.addView(cell(c1, 1.1f, Gravity.CENTER, header));
        row.addView(cell(c2, 1.1f, Gravity.CENTER, header));
        row.addView(cell(c3, 1.1f, Gravity.CENTER, header));
        row.addView(cell(c4, 1.3f, Gravity.CENTER, header));
        row.addView(cell(c5, 1.3f, Gravity.CENTER, header));
        return row;
    }

    private TextView cell(String text, float weight, int gravity, boolean header) {
        TextView t = new TextView(this);
        t.setText(text == null ? "" : text);
        t.setGravity(gravity);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, header ? 11 : 13);
        t.setMaxLines(1);
        t.setEllipsize(android.text.TextUtils.TruncateAt.END);
        if (header) {
            t.setTextColor(ContextCompat.getColor(this, R.color.cmp_text_secondary));
            t.setTypeface(Typeface.DEFAULT_BOLD);
        } else {
            t.setTextColor(ContextCompat.getColor(this, R.color.cmp_text_primary));
        }
        t.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, weight));
        return t;
    }

    // ---- Networking ---------------------------------------------------------

    private void refresh() {
        try {
            APIService api = RestApiClient.getClient().create(APIService.class);
            api.cricketActiveMatch("v3").enqueue(new Callback<CricketActiveMatchResponse>() {
                @Override
                public void onResponse(@NonNull Call<CricketActiveMatchResponse> call,
                                       @NonNull Response<CricketActiveMatchResponse> response) {
                    if (isFinishing() || response.body() == null) return;
                    ActiveMatch fresh = response.body().getActive_match();
                    if (fresh != null && match != null
                            && fresh.getMatch_id().equals(match.getMatch_id())) {
                        match = fresh;
                        bind();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<CricketActiveMatchResponse> call, @NonNull Throwable t) {
                    // Offline — keep the data passed via intent.
                }
            });
        } catch (Exception ignored) {
        }
    }

    private void fetchDetail() {
        if (match == null) return;
        final String key = !TextUtils.isEmpty(match.getCrex_key()) ? match.getCrex_key() : match.getMatch_id();
        if (TextUtils.isEmpty(key)) return;
        try {
            APIService api = RestApiClient.getClient().create(APIService.class);
            api.cricketMatchDetail("v3", key).enqueue(new Callback<CricketMatchDetailResponse>() {
                @Override
                public void onResponse(@NonNull Call<CricketMatchDetailResponse> call,
                                       @NonNull Response<CricketMatchDetailResponse> response) {
                    if (isFinishing() || response.body() == null) return;
                    if (response.body().getSuccess() != null && response.body().getSuccess()
                            && response.body().getData() != null) {
                        detail = response.body().getData();
                        renderCurrentTab();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<CricketMatchDetailResponse> call, @NonNull Throwable t) {
                    // Transient — keep the last rendered detail.
                }
            });
        } catch (Exception ignored) {
        }
    }

    // ---- Helpers ------------------------------------------------------------

    private int dp(float v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    private static String fullTeam(String code, String name) {
        if (!TextUtils.isEmpty(name) && !name.equalsIgnoreCase(code)) {
            return TextUtils.isEmpty(code) ? name : code + " · " + name;
        }
        return TextUtils.isEmpty(code) ? name : code;
    }

    private static String scoreOrDash(String score, String overs) {
        if (TextUtils.isEmpty(score)) return "Yet to bat";
        StringBuilder sb = new StringBuilder(score);
        if (!TextUtils.isEmpty(overs)) sb.append(" (").append(overs).append(')');
        return sb.toString();
    }
}
