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

import com.bumptech.glide.Glide;
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
 * Cricket Match Center — full scoreboard + two tabs (Live / Scorecard). Opened
 * when the user taps the floating mini player.
 *
 * The header scoreboard comes from the active_match contract. The Scorecard tab
 * fetches real per-ball detail from the app-facing /app/v3/cricket_match_detail
 * endpoint and renders the innings batting/bowling tables plus the consolidated
 * live stats, win probability, match info, summary/top performers and
 * ball-by-ball commentary. Sections with no data from CREX are simply omitted,
 * and an empty match shows a clean empty state.
 */
public class MatchCenterActivity extends AppCompatActivity {

    public static final String EXTRA_MATCH = "extra_active_match";

    // Detail + header both refresh on this cadence while the screen is open.
    // Reads are served from the backend DB snapshot (no per-request CREX call),
    // so a few seconds keeps the score/commentary live without hammering CREX.
    private static final int DETAIL_POLL_MS = 3000;

    private ActiveMatch match;
    private CricketMatchDetailResponse.Detail detail;

    private TextView tabLive, tabScorecard;
    private View contentLive, contentPlaceholder;
    private LinearLayout contentScorecard;
    private LinearLayout liveDynamic;
    private TextView placeholderTitle, placeholderMessage;

    // Two tabs only — 1 Live · 2 Scorecard. Scorecard consolidates everything
    // that used to live on the removed Summary / Commentary / Stats tabs.
    private int currentTab = 1;
    private boolean initialTabChosen = false;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable detailPoll = new Runnable() {
        @Override
        public void run() {
            // Keep BOTH the header (active_match) and the tab detail live on the
            // same cadence so the orange header never lags the Summary/Live tabs.
            refresh();
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
        contentLive = findViewById(R.id.content_live);
        liveDynamic = findViewById(R.id.live_dynamic);
        contentScorecard = findViewById(R.id.content_scorecard);
        contentPlaceholder = findViewById(R.id.content_placeholder);
        placeholderTitle = findViewById(R.id.placeholder_title);
        placeholderMessage = findViewById(R.id.placeholder_message);

        tabLive.setOnClickListener(v -> selectTab(1));
        tabScorecard.setOnClickListener(v -> selectTab(2));

        bind();
        selectTab(1);

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
        title.setText(matchHeading());

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
        tabLive.setTextColor(index == 1 ? active : inactive);
        tabScorecard.setTextColor(index == 2 ? active : inactive);

        contentLive.setVisibility(index == 1 ? View.VISIBLE : View.GONE);
        contentScorecard.setVisibility(index == 2 ? View.VISIBLE : View.GONE);
        contentPlaceholder.setVisibility(View.GONE);

        renderCurrentTab();
    }

    private void renderCurrentTab() {
        if (currentTab == 2) {
            renderScorecard();
        } else {
            renderLive();
        }
    }

    private void showEmptyFor(String title, String message) {
        contentLive.setVisibility(View.GONE);
        contentScorecard.setVisibility(View.GONE);
        contentPlaceholder.setVisibility(View.VISIBLE);
        placeholderTitle.setText(title);
        placeholderMessage.setText(message);
    }

    // ---- Summary (consolidated into Scorecard) ------------------------------

    /**
     * Former Summary tab, now appended under Scorecard: each side with flag +
     * full name + score, a trophy on the winning team, the winner/result line,
     * the last-over ball strip, a Player of the Match card (photo + name +
     * figures) and per-innings Top Performers (top batters with SR, best bowler
     * with ER). Returns true when anything was added.
     */
    private boolean appendSummary(LinearLayout container) {
        CricketMatchDetailResponse.Summary summary = detail != null ? detail.getSummary() : null;
        if (summary == null) return false;
        boolean any = false;

        // Teams card with flags, scores and trophy on the winner.
        List<CricketMatchDetailResponse.SummaryTeam> teams = summary.getTeams();
        if (teams != null && !teams.isEmpty()) {
            LinearLayout teamsCard = card();
            for (CricketMatchDetailResponse.SummaryTeam t : teams) {
                teamsCard.addView(buildSummaryTeamRow(t));
            }
            if (!TextUtils.isEmpty(summary.getResult_text())) {
                TextView res = new TextView(this);
                res.setText(summary.getResult_text());
                res.setTextColor(ContextCompat.getColor(this, R.color.cmp_brand_orange));
                res.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                res.setTypeface(Typeface.DEFAULT_BOLD);
                res.setPadding(0, dp(10), 0, 0);
                teamsCard.addView(res);
            }
            container.addView(teamsCard);
            any = true;
        }

        // Last over ball strip.
        CricketMatchDetailResponse.Over lastOver = summary.getLast_over();
        if (lastOver != null && lastOver.getBalls() != null && !lastOver.getBalls().isEmpty()) {
            LinearLayout overCard = card();
            overCard.addView(sectionTitle("Last over"));
            overCard.addView(buildOverStrip(lastOver));
            container.addView(overCard);
            any = true;
        }

        // Player of the Match.
        CricketMatchDetailResponse.PlayerOf mom = summary.getPlayer_of_match() != null
                ? summary.getPlayer_of_match()
                : detail.getPlayer_of_match();
        if (mom != null && !TextUtils.isEmpty(mom.getName())) {
            container.addView(buildPlayerOfMatchCard(mom));
            any = true;
        }

        // Top performers per innings.
        List<CricketMatchDetailResponse.TopPerformer> performers = summary.getTop_performers();
        if (performers != null && !performers.isEmpty()) {
            LinearLayout tpCard = card();
            tpCard.addView(sectionTitle("Top Performers"));
            for (CricketMatchDetailResponse.TopPerformer tp : performers) {
                tpCard.addView(buildTopPerformerBlock(tp));
            }
            container.addView(tpCard);
            any = true;
        }
        return any;
    }

    private View buildSummaryTeamRow(CricketMatchDetailResponse.SummaryTeam t) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(6), 0, dp(6));

        ImageView flag = new ImageView(this);
        LinearLayout.LayoutParams flagLp = new LinearLayout.LayoutParams(dp(30), dp(30));
        flagLp.rightMargin = dp(10);
        flag.setLayoutParams(flagLp);
        if (!TextUtils.isEmpty(t.getFlag())) {
            try {
                Glide.with(this).load(t.getFlag()).circleCrop().into(flag);
            } catch (Exception ignored) {
            }
        }
        row.addView(flag);

        TextView name = new TextView(this);
        name.setText(t.getTeam() + (t.isWinner() ? "  🏆" : ""));
        name.setTextColor(ContextCompat.getColor(this, R.color.cmp_text_primary));
        name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        name.setTypeface(t.isWinner() ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        name.setMaxLines(1);
        name.setEllipsize(android.text.TextUtils.TruncateAt.END);
        name.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(name);

        StringBuilder scoreSb = new StringBuilder();
        List<CricketMatchDetailResponse.TeamInnings> inns = t.getInnings();
        if (inns != null) {
            for (CricketMatchDetailResponse.TeamInnings inn : inns) {
                if (scoreSb.length() > 0) scoreSb.append(" & ");
                scoreSb.append(inn.getScore());
                if (!TextUtils.isEmpty(inn.getOvers())) scoreSb.append(" (").append(inn.getOvers()).append(')');
            }
        }
        TextView score = new TextView(this);
        score.setText(scoreSb.toString());
        score.setTextColor(ContextCompat.getColor(this, R.color.cmp_text_primary));
        score.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        score.setTypeface(Typeface.DEFAULT_BOLD);
        row.addView(score);
        return row;
    }

    private View buildPlayerOfMatchCard(CricketMatchDetailResponse.PlayerOf mom) {
        LinearLayout cardView = card();
        cardView.addView(labelLine(TextUtils.isEmpty(mom.getLabel()) ? "Player of the Match" : mom.getLabel()));

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        ImageView photo = new ImageView(this);
        LinearLayout.LayoutParams pLp = new LinearLayout.LayoutParams(dp(48), dp(48));
        pLp.rightMargin = dp(12);
        photo.setLayoutParams(pLp);
        if (!TextUtils.isEmpty(mom.getImage())) {
            try {
                Glide.with(this).load(mom.getImage()).circleCrop().into(photo);
            } catch (Exception ignored) {
            }
        }
        row.addView(photo);

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        TextView name = new TextView(this);
        name.setText(mom.getName());
        name.setTextColor(ContextCompat.getColor(this, R.color.cmp_text_primary));
        name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        name.setTypeface(Typeface.DEFAULT_BOLD);
        col.addView(name);
        if (!TextUtils.isEmpty(mom.getFigures())) {
            TextView fig = new TextView(this);
            fig.setText(mom.getFigures());
            fig.setTextColor(ContextCompat.getColor(this, R.color.cmp_text_secondary));
            fig.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            col.addView(fig);
        }
        row.addView(col);
        cardView.addView(row);
        return cardView;
    }

    private View buildTopPerformerBlock(CricketMatchDetailResponse.TopPerformer tp) {
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setPadding(0, dp(8), 0, dp(4));

        TextView label = new TextView(this);
        label.setText(TextUtils.isEmpty(tp.getLabel()) ? tp.getTeam_name() : tp.getLabel());
        label.setTextColor(ContextCompat.getColor(this, R.color.cmp_text_secondary));
        label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        label.setTypeface(Typeface.DEFAULT_BOLD);
        wrap.addView(label);

        List<CricketMatchDetailResponse.TopPlayer> batters = tp.getTop_batters();
        if (batters != null) {
            for (CricketMatchDetailResponse.TopPlayer b : batters) {
                wrap.addView(buildPerfRow(b.getName(), b.getFigures(),
                        TextUtils.isEmpty(b.getStrike_rate()) ? "" : "SR " + b.getStrike_rate()));
            }
        }
        CricketMatchDetailResponse.TopPlayer bowler = tp.getTop_bowler();
        if (bowler != null && !TextUtils.isEmpty(bowler.getName())) {
            wrap.addView(buildPerfRow(bowler.getName(), bowler.getFigures(),
                    TextUtils.isEmpty(bowler.getEconomy()) ? "" : "ER " + bowler.getEconomy()));
        }
        return wrap;
    }

    private View buildPerfRow(String name, String figures, String extra) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(4), 0, dp(4));

        TextView n = new TextView(this);
        n.setText(name);
        n.setTextColor(ContextCompat.getColor(this, R.color.cmp_text_primary));
        n.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        n.setMaxLines(1);
        n.setEllipsize(android.text.TextUtils.TruncateAt.END);
        n.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(n);

        TextView f = new TextView(this);
        String txt = figures == null ? "" : figures;
        if (!TextUtils.isEmpty(extra)) txt = txt + "   " + extra;
        f.setText(txt);
        f.setTextColor(ContextCompat.getColor(this, R.color.cmp_text_secondary));
        f.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        f.setTypeface(Typeface.DEFAULT_BOLD);
        row.addView(f);
        return row;
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
        intent.putExtra(AllOversActivity.EXTRA_TITLE, matchHeading());
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

    /**
     * Scorecard tab: the full innings breakdown (batting/bowling/fall of
     * wickets) plus the sections consolidated from the removed tabs — live
     * stats / win probability / match info (former Stats tab), summary +
     * player of the match + top performers (former Summary tab) and ball-by-ball
     * commentary (former Commentary tab). All sections reuse the same card
     * builders, so the styling is unchanged.
     */
    private void renderScorecard() {
        contentScorecard.removeAllViews();
        boolean any = false;

        // Innings breakdown (the primary scorecard content).
        List<CricketMatchDetailResponse.Innings> innings = detail != null ? detail.getScorecards() : null;
        if (innings != null && !innings.isEmpty()) {
            for (CricketMatchDetailResponse.Innings inn : innings) {
                contentScorecard.addView(buildInningsCard(inn));
            }
            any = true;
        }

        // Consolidated sections from the former Stats / Summary / Commentary tabs.
        any |= appendStats(contentScorecard);
        any |= appendSummary(contentScorecard);
        any |= appendCommentary(contentScorecard);

        if (!any) {
            showEmptyFor("Scorecard", detail == null
                    ? "Loading scorecard…"
                    : "The full batting & bowling scorecard will appear here once the match is under way.");
            return;
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

    // ---- Commentary (consolidated into Scorecard) ---------------------------

    private boolean appendCommentary(LinearLayout container) {
        List<CricketMatchDetailResponse.Commentary> items = detail != null ? detail.getCommentary() : null;
        if (items == null || items.isEmpty()) return false;
        TextView heading = sectionTitle("Commentary");
        heading.setPadding(dp(2), dp(4), 0, dp(8));
        container.addView(heading);
        for (CricketMatchDetailResponse.Commentary c : items) {
            container.addView(buildCommentaryRow(c));
        }
        return true;
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

    // ---- Stats (consolidated into Scorecard) --------------------------------

    private boolean appendStats(LinearLayout container) {
        if (detail == null) return false;
        CricketMatchDetailResponse.Live live = detail.getLive();
        CricketMatchDetailResponse.Info info = detail.getInfo();
        boolean any = false;

        LinearLayout live_card = card();
        if (live != null) {
            if (!TextUtils.isEmpty(live.getSession_label())) {
                live_card.addView(keyValue("Session", live.getSession_label()));
            }
            if (!TextUtils.isEmpty(live.getCurrent_run_rate())) {
                live_card.addView(keyValue("Current run rate", live.getCurrent_run_rate()));
            }
            if (!TextUtils.isEmpty(live.getRequired_rate())) {
                live_card.addView(keyValue("Required run rate", live.getRequired_rate()));
            }
            if (live.getPartnership() != null && !TextUtils.isEmpty(live.getPartnership().getText())) {
                live_card.addView(keyValue("Partnership", live.getPartnership().getText()));
            }
            if (live.getLast_wicket() != null && !TextUtils.isEmpty(live.getLast_wicket().getText())) {
                live_card.addView(keyValue("Last wicket", live.getLast_wicket().getText()));
            }
        }
        if (live_card.getChildCount() > 0) {
            live_card.addView(sectionTitle("Live stats"), 0);
            container.addView(live_card);
            any = true;
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
                container.addView(wpCard);
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
            container.addView(infoCard);
            any = true;
        }
        return any;
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
                        // First load of a finished match opens on Scorecard (which
                        // now carries the summary content). Live matches stay on Live.
                        if (!initialTabChosen) {
                            initialTabChosen = true;
                            boolean completed = detail.isCompleted()
                                    || (detail.getSummary() != null && detail.getSummary().isCompleted());
                            if (completed) {
                                selectTab(2);
                                return;
                            }
                        }
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

    /** Prefer the FULL team name; fall back to the short code only if needed. */
    private static String fullTeam(String code, String name) {
        if (!TextUtils.isEmpty(name) && !name.equalsIgnoreCase(code)) {
            return name;
        }
        return TextUtils.isEmpty(code) ? name : code;
    }

    private String matchHeading() {
        if (match == null) return "Match Center";
        if (!TextUtils.isEmpty(match.getMatch_title())) return match.getMatch_title();
        String t1 = fullTeam(match.getTeam1_code(), match.getTeam1_name());
        String t2 = fullTeam(match.getTeam2_code(), match.getTeam2_name());
        return t1 + " vs " + t2;
    }

    private static String scoreOrDash(String score, String overs) {
        if (TextUtils.isEmpty(score)) return "Yet to bat";
        StringBuilder sb = new StringBuilder(score);
        if (!TextUtils.isEmpty(overs)) sb.append(" (").append(overs).append(')');
        return sb.toString();
    }
}
