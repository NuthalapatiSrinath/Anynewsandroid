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
    private View tabLiveIndicator, tabScorecardIndicator;
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
        tabLiveIndicator = findViewById(R.id.tab_live_indicator);
        tabScorecardIndicator = findViewById(R.id.tab_scorecard_indicator);
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
        TextView liveBadge = findViewById(R.id.mc_live_badge);

        series.setText(match.getSeries_name());
        title.setText(matchHeading());
        liveBadge.setVisibility(match.isLive() ? View.VISIBLE : View.GONE);

        // Team score rows (flag + name + big score + overs + run rate).
        bindTeam(match.getTeam1_flag(), fullTeam(match.getTeam1_code(), match.getTeam1_name()),
                match.getTeam1_score(), match.getTeam1_overs(),
                R.id.mc_team1_flag, R.id.mc_team1_name, R.id.mc_team1_score,
                R.id.mc_team1_overs, R.id.mc_team1_rr);
        bindTeam(match.getTeam2_flag(), fullTeam(match.getTeam2_code(), match.getTeam2_name()),
                match.getTeam2_score(), match.getTeam2_overs(),
                R.id.mc_team2_flag, R.id.mc_team2_name, R.id.mc_team2_score,
                R.id.mc_team2_overs, R.id.mc_team2_rr);

        // Centre LIVE badge + a compact format tag (e.g. T20) when available.
        TextView centerLive = findViewById(R.id.mc_center_live);
        TextView centerSub = findViewById(R.id.mc_center_sub);
        centerLive.setVisibility(match.isLive() ? View.VISIBLE : View.GONE);
        String fmt = match.getFormat();
        if (!TextUtils.isEmpty(fmt) && fmt.length() <= 4) {
            centerSub.setVisibility(View.VISIBLE);
            centerSub.setText(fmt.toUpperCase());
        } else {
            centerSub.setVisibility(View.GONE);
        }

        // Equation / status pill (e.g. "… need 1 runs in 4.4 overs"). Hidden when
        // the only status is the plain "Live" flag that the badge already shows.
        TextView equation = findViewById(R.id.mc_equation);
        String eq = liveStatusText();
        if (TextUtils.isEmpty(eq) || eq.equalsIgnoreCase("live")) {
            equation.setVisibility(View.GONE);
        } else {
            equation.setVisibility(View.VISIBLE);
            equation.setText(eq);
        }
    }

    /** Fill one team's flag/name/score/overs/run-rate slot in the hero header. */
    private void bindTeam(String flagUrl, String name, String score, String overs,
                          int flagId, int nameId, int scoreId, int oversId, int rrId) {
        ImageView flag = findViewById(flagId);
        TextView nameView = findViewById(nameId);
        TextView scoreView = findViewById(scoreId);
        TextView oversView = findViewById(oversId);
        TextView rrView = findViewById(rrId);

        nameView.setText(name);
        if (!TextUtils.isEmpty(flagUrl)) {
            try {
                Glide.with(this).load(flagUrl).circleCrop().into(flag);
            } catch (Exception ignored) {
            }
        }

        if (TextUtils.isEmpty(score)) {
            scoreView.setText("–");
            oversView.setVisibility(View.GONE);
            rrView.setVisibility(View.GONE);
            return;
        }
        scoreView.setText(score);
        if (TextUtils.isEmpty(overs)) {
            oversView.setVisibility(View.GONE);
        } else {
            oversView.setVisibility(View.VISIBLE);
            oversView.setText("(" + overs + ")");
        }
        String rr = runRate(score, overs);
        if (TextUtils.isEmpty(rr)) {
            rrView.setVisibility(View.GONE);
        } else {
            rrView.setVisibility(View.VISIBLE);
            rrView.setText("RR: " + rr);
        }
    }

    /** Latest match status text, preferring the live detail over the header feed. */
    private String liveStatusText() {
        if (detail != null && detail.getLive() != null
                && !TextUtils.isEmpty(detail.getLive().getStatus_text())) {
            return detail.getLive().getStatus_text();
        }
        return match != null ? match.getStatus_text() : "";
    }

    /** Derive run rate (runs per over) from a "44/1" score and a "21.2" over count. */
    private static String runRate(String score, String overs) {
        if (TextUtils.isEmpty(score) || TextUtils.isEmpty(overs)) return "";
        try {
            String runsPart = score.contains("/") ? score.substring(0, score.indexOf('/')) : score;
            runsPart = runsPart.replaceAll("[^0-9]", "");
            if (runsPart.isEmpty()) return "";
            int runs = Integer.parseInt(runsPart);
            int balls = oversToBalls(overs);
            if (balls <= 0) return "";
            double rr = runs * 6.0 / balls;
            return String.format(java.util.Locale.US, "%.2f", rr);
        } catch (Exception e) {
            return "";
        }
    }

    /** "21.2" overs → 21*6 + 2 legal balls. */
    private static int oversToBalls(String overs) {
        if (TextUtils.isEmpty(overs)) return 0;
        try {
            String o = overs.trim();
            int dot = o.indexOf('.');
            int full = Integer.parseInt(dot >= 0 ? o.substring(0, dot) : o);
            int part = 0;
            if (dot >= 0 && dot + 1 < o.length()) {
                part = Integer.parseInt(o.substring(dot + 1).replaceAll("[^0-9]", ""));
            }
            return full * 6 + part;
        } catch (Exception e) {
            return 0;
        }
    }

    private void selectTab(int index) {
        currentTab = index;
        int active = ContextCompat.getColor(this, R.color.cmp_brand_orange);
        int inactive = ContextCompat.getColor(this, R.color.cmp_text_secondary);
        tabLive.setTextColor(index == 1 ? active : inactive);
        tabScorecard.setTextColor(index == 2 ? active : inactive);
        tabLiveIndicator.setVisibility(index == 1 ? View.VISIBLE : View.INVISIBLE);
        tabScorecardIndicator.setVisibility(index == 2 ? View.VISIBLE : View.INVISIBLE);

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
     * Live tab (CREX-style): a combined Match Status + Last Ball card, current
     * batting and bowling mini-tables, a run-rate / target metrics strip and a
     * Fall of Wicket bar. Every value is bound to the DB snapshot already
     * consumed by the Match Center — sections with no backing data are omitted
     * gracefully. Tapping the recent-over card or the Fall of Wicket bar still
     * opens the full over-by-over breakdown.
     */
    private void renderLive() {
        contentPlaceholder.setVisibility(View.GONE);
        contentLive.setVisibility(View.VISIBLE);
        liveDynamic.removeAllViews();
        if (detail == null) return;

        CricketMatchDetailResponse.Live live = detail.getLive();
        CricketMatchDetailResponse.Innings inn = currentInnings();

        View statusCard = buildStatusLastBallCard(live);
        if (statusCard != null) liveDynamic.addView(statusCard);

        View battingCard = buildBattingCard(live, inn);
        if (battingCard != null) liveDynamic.addView(battingCard);

        View bowlingCard = buildBowlingCard(live, inn);
        if (bowlingCard != null) liveDynamic.addView(bowlingCard);

        View metrics = buildMetricsStrip(live);
        if (metrics != null) liveDynamic.addView(metrics);

        View fow = buildFowBar(inn);
        if (fow != null) liveDynamic.addView(fow);
    }

    /** The innings currently in progress (falls back to the latest innings). */
    private CricketMatchDetailResponse.Innings currentInnings() {
        List<CricketMatchDetailResponse.Innings> list = detail != null ? detail.getScorecards() : null;
        if (list == null || list.isEmpty()) return null;
        CricketMatchDetailResponse.Innings current = null;
        for (CricketMatchDetailResponse.Innings inn : list) {
            if (inn.isCurrent()) current = inn;
        }
        return current != null ? current : list.get(list.size() - 1);
    }

    private CricketMatchDetailResponse.Over lastOver() {
        List<CricketMatchDetailResponse.Over> overs = detail != null ? detail.getAll_overs() : null;
        if (overs == null || overs.isEmpty()) return null;
        return overs.get(overs.size() - 1);
    }

    /** Left "Match Status" column + right "Last Ball / Recent" over column. */
    private View buildStatusLastBallCard(CricketMatchDetailResponse.Live live) {
        CricketMatchDetailResponse.Over over = lastOver();

        LinearLayout cardView = card(LinearLayout.HORIZONTAL);

        LinearLayout left = new LinearLayout(this);
        left.setOrientation(LinearLayout.VERTICAL);
        left.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        left.addView(iconLabelRow(R.drawable.ic_cmp_ball, "Match Status"));
        TextView sv = new TextView(this);
        sv.setText(shortStatus());
        sv.setTextColor(ContextCompat.getColor(this, R.color.cmp_brand_orange));
        sv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        sv.setTypeface(Typeface.DEFAULT_BOLD);
        sv.setPadding(0, dp(8), 0, 0);
        left.addView(sv);
        cardView.addView(left);

        View divCol = new View(this);
        LinearLayout.LayoutParams dLp = new LinearLayout.LayoutParams(dp(1), ViewGroup.LayoutParams.MATCH_PARENT);
        dLp.leftMargin = dp(10);
        dLp.rightMargin = dp(12);
        divCol.setLayoutParams(dLp);
        divCol.setBackgroundColor(ContextCompat.getColor(this, R.color.cmp_border));
        cardView.addView(divCol);

        LinearLayout right = new LinearLayout(this);
        right.setOrientation(LinearLayout.VERTICAL);
        right.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 2f));

        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(Gravity.CENTER_VERTICAL);
        View label = iconLabelRow(R.drawable.ic_cmp_ball, "Last Ball / Recent");
        label.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        head.addView(label);
        if (over != null && !TextUtils.isEmpty(over.getOver_label())) {
            TextView ovl = new TextView(this);
            ovl.setText(over.getOver_label());
            ovl.setTextColor(ContextCompat.getColor(this, R.color.cmp_text_primary));
            ovl.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            ovl.setTypeface(Typeface.DEFAULT_BOLD);
            head.addView(ovl);
        }
        right.addView(head);

        if (over != null) {
            String seq = overSequence(over);
            if (!TextUtils.isEmpty(seq)) {
                TextView seqView = new TextView(this);
                seqView.setText(seq);
                seqView.setTextColor(ContextCompat.getColor(this, R.color.cmp_text_primary));
                seqView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
                seqView.setTypeface(Typeface.DEFAULT_BOLD);
                seqView.setPadding(0, dp(6), 0, 0);
                right.addView(seqView);
            }
            List<CricketMatchDetailResponse.Ball> balls = over.getBalls();
            if (balls != null && !balls.isEmpty()) {
                android.widget.HorizontalScrollView hsv = new android.widget.HorizontalScrollView(this);
                hsv.setHorizontalScrollBarEnabled(false);
                LinearLayout inner = new LinearLayout(this);
                inner.setOrientation(LinearLayout.HORIZONTAL);
                inner.setPadding(0, dp(8), 0, 0);
                for (CricketMatchDetailResponse.Ball b : balls) inner.addView(liveBallCircle(b));
                hsv.addView(inner);
                right.addView(hsv);
            }
            right.setOnClickListener(v -> openAllOvers());
        }
        cardView.addView(right);
        return cardView;
    }

    /** "20:2.0.0.0.0.0" — over number followed by each ball's outcome. */
    private String overSequence(CricketMatchDetailResponse.Over over) {
        List<CricketMatchDetailResponse.Ball> balls = over.getBalls();
        if (balls == null || balls.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        sb.append(over.getOver()).append(':');
        boolean first = true;
        for (CricketMatchDetailResponse.Ball b : balls) {
            if (!first) sb.append('.');
            first = false;
            sb.append(b.isWicket() ? "W" : (TextUtils.isEmpty(b.getValue()) ? "0" : b.getValue()));
        }
        return sb.toString();
    }

    /** Match Status value — plain "Live" while in play, else the result/status. */
    private String shortStatus() {
        if (match != null && match.isLive()) return "Live";
        if (match != null && !TextUtils.isEmpty(match.getResult())) return match.getResult();
        String s = liveStatusText();
        return TextUtils.isEmpty(s) ? "Live" : s;
    }

    private TextView liveBallCircle(CricketMatchDetailResponse.Ball b) {
        TextView c = new TextView(this);
        c.setGravity(Gravity.CENTER);
        c.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        c.setTypeface(Typeface.DEFAULT_BOLD);
        String val = b.isWicket() ? "W" : (TextUtils.isEmpty(b.getValue()) ? "0" : b.getValue());
        c.setText(val);
        int size = dp(32);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
        lp.rightMargin = dp(8);
        c.setLayoutParams(lp);
        if (b.isWicket()) {
            c.setTextColor(0xFFFFFFFF);
            c.setBackgroundResource(R.drawable.bg_over_ball_red);
        } else if (b.isFour() || b.isSix() || isScoringBall(val)) {
            c.setTextColor(0xFFFFFFFF);
            c.setBackgroundResource(R.drawable.bg_over_ball_orange);
        } else {
            c.setTextColor(ContextCompat.getColor(this, R.color.cmp_text_primary));
            c.setBackgroundResource(R.drawable.bg_cmp_ball_outline);
        }
        return c;
    }

    private static boolean isScoringBall(String val) {
        try {
            return Integer.parseInt(val.trim()) > 0;
        } catch (Exception e) {
            return false;
        }
    }

    // ---- Batting / Bowling mini-tables --------------------------------------

    private View buildBattingCard(CricketMatchDetailResponse.Live live,
                                  CricketMatchDetailResponse.Innings inn) {
        List<CricketMatchDetailResponse.Batsman> batsmen = live != null ? live.getBatsmen() : null;
        if (batsmen == null || batsmen.isEmpty()) return null;

        float nameWeight = 3.4f;
        String[] cols = {"R (B)", "4s", "6s", "SR"};
        float[] weights = {1.6f, 0.9f, 0.9f, 1.3f};

        LinearLayout cardView = card();
        cardView.addView(statHeader(R.drawable.ic_cmp_bat, "Batting (" + batsmen.size() + ")",
                cols, weights, nameWeight));
        for (CricketMatchDetailResponse.Batsman b : batsmen) {
            CricketMatchDetailResponse.Batting sc = findBatting(inn, b.getName());
            String rb = b.getRuns() + " (" + b.getBalls().replaceAll("[()]", "") + ")";
            String fours = sc != null ? String.valueOf(sc.getFours()) : "0";
            String sixes = sc != null ? String.valueOf(sc.getSixes()) : "0";
            String sr = !TextUtils.isEmpty(b.getStrike_rate()) ? b.getStrike_rate()
                    : (sc != null ? sc.getStrike_rate() : "");
            String[] vals = {rb, fours, sixes, TextUtils.isEmpty(sr) ? "-" : sr};
            cardView.addView(statRow(b.getName(), b.isOn_strike(), vals, weights, nameWeight));
        }
        return cardView;
    }

    private View buildBowlingCard(CricketMatchDetailResponse.Live live,
                                  CricketMatchDetailResponse.Innings inn) {
        CricketMatchDetailResponse.Bowler bowler = live != null ? live.getBowler() : null;
        if (bowler == null || TextUtils.isEmpty(bowler.getName())) return null;

        float nameWeight = 3.4f;
        String[] cols = {"O", "M", "R", "W", "Eco"};
        float[] weights = {1.0f, 0.9f, 1.0f, 0.9f, 1.3f};

        CricketMatchDetailResponse.Bowling sc = findBowling(inn, bowler.getName());
        String o = !TextUtils.isEmpty(bowler.getOvers()) ? bowler.getOvers()
                : (sc != null ? sc.getOvers() : "-");
        String r = sc != null ? String.valueOf(sc.getRuns()) : "-";
        String w = sc != null ? String.valueOf(sc.getWickets()) : "-";
        String eco = !TextUtils.isEmpty(bowler.getEconomy()) ? bowler.getEconomy()
                : (sc != null ? sc.getEconomy() : "-");

        LinearLayout cardView = card();
        cardView.addView(statHeader(R.drawable.ic_cmp_ball, "Bowling (1)", cols, weights, nameWeight));
        String[] vals = {TextUtils.isEmpty(o) ? "-" : o, "-", r, w, TextUtils.isEmpty(eco) ? "-" : eco};
        cardView.addView(statRow(bowler.getName() + " (Bowling)", false, vals, weights, nameWeight));
        return cardView;
    }

    private View statHeader(int iconRes, String title, String[] cols, float[] weights, float nameWeight) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, 0, 0, dp(6));

        LinearLayout titleCell = new LinearLayout(this);
        titleCell.setOrientation(LinearLayout.HORIZONTAL);
        titleCell.setGravity(Gravity.CENTER_VERTICAL);
        titleCell.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, nameWeight));
        ImageView ic = new ImageView(this);
        LinearLayout.LayoutParams icLp = new LinearLayout.LayoutParams(dp(16), dp(16));
        icLp.rightMargin = dp(6);
        ic.setLayoutParams(icLp);
        ic.setImageResource(iconRes);
        titleCell.addView(ic);
        TextView t = new TextView(this);
        t.setText(title);
        t.setTextColor(ContextCompat.getColor(this, R.color.cmp_text_primary));
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        titleCell.addView(t);
        row.addView(titleCell);

        for (int i = 0; i < cols.length; i++) {
            row.addView(statCell(cols[i], weights[i], true));
        }
        return row;
    }

    private View statRow(String name, boolean strike, String[] vals, float[] weights, float nameWeight) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(7), 0, dp(7));

        LinearLayout nameCell = new LinearLayout(this);
        nameCell.setOrientation(LinearLayout.HORIZONTAL);
        nameCell.setGravity(Gravity.CENTER_VERTICAL);
        nameCell.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, nameWeight));

        TextView dot = new TextView(this);
        dot.setText("●");
        dot.setTextColor(ContextCompat.getColor(this, R.color.cmp_brand_orange));
        dot.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8);
        dot.setPadding(0, 0, dp(6), 0);
        nameCell.addView(dot);

        TextView n = new TextView(this);
        n.setText(name + (strike ? "  *" : ""));
        n.setTextColor(ContextCompat.getColor(this, R.color.cmp_text_primary));
        n.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        n.setTypeface(strike ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        n.setMaxLines(1);
        n.setEllipsize(TextUtils.TruncateAt.END);
        nameCell.addView(n);
        row.addView(nameCell);

        for (int i = 0; i < vals.length; i++) {
            row.addView(statCell(vals[i], weights[i], false));
        }
        return row;
    }

    private TextView statCell(String text, float weight, boolean header) {
        TextView t = new TextView(this);
        t.setText(text == null ? "" : text);
        t.setGravity(Gravity.CENTER);
        t.setMaxLines(1);
        t.setEllipsize(TextUtils.TruncateAt.END);
        if (header) {
            t.setTextColor(ContextCompat.getColor(this, R.color.cmp_text_secondary));
            t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        } else {
            t.setTextColor(ContextCompat.getColor(this, R.color.cmp_text_primary));
            t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        }
        t.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, weight));
        return t;
    }

    private CricketMatchDetailResponse.Batting findBatting(CricketMatchDetailResponse.Innings inn, String name) {
        if (inn == null || inn.getBatting() == null || TextUtils.isEmpty(name)) return null;
        for (CricketMatchDetailResponse.Batting b : inn.getBatting()) {
            if (namesMatch(b.getName(), name)) return b;
        }
        return null;
    }

    private CricketMatchDetailResponse.Bowling findBowling(CricketMatchDetailResponse.Innings inn, String name) {
        if (inn == null || inn.getBowling() == null || TextUtils.isEmpty(name)) return null;
        for (CricketMatchDetailResponse.Bowling b : inn.getBowling()) {
            if (namesMatch(b.getName(), name)) return b;
        }
        return null;
    }

    private static boolean namesMatch(String a, String b) {
        if (TextUtils.isEmpty(a) || TextUtils.isEmpty(b)) return false;
        String x = a.trim().toLowerCase();
        String y = b.trim().toLowerCase();
        return x.equals(y) || x.contains(y) || y.contains(x);
    }

    // ---- Metrics strip ------------------------------------------------------

    private View buildMetricsStrip(CricketMatchDetailResponse.Live live) {
        java.util.List<View> cards = new java.util.ArrayList<>();

        String crr = live != null ? live.getCurrent_run_rate() : "";
        if (!TextUtils.isEmpty(crr)) {
            cards.add(metricCard(R.drawable.ic_cmp_ball, "Current Run Rate", crr, null));
        }
        String rrr = live != null ? live.getRequired_rate() : "";
        if (!TextUtils.isEmpty(rrr)) {
            cards.add(metricCard(R.drawable.ic_cmp_speed, "Required Run Rate", rrr, null));
        }

        String statusText = liveStatusText();
        String oversRem = extractOversRemaining(statusText);
        if (!TextUtils.isEmpty(oversRem)) {
            int balls = oversToBalls(oversRem);
            cards.add(metricCard(R.drawable.ic_cmp_timer, "Overs Remaining", oversRem,
                    balls > 0 ? balls + " Balls" : null));
        }
        String target = extractTarget(statusText);
        if (!TextUtils.isEmpty(target)) {
            cards.add(metricCard(R.drawable.ic_cmp_target, "Target", target, null));
        }

        if (cards.isEmpty()) return null;

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rowLp.bottomMargin = dp(12);
        row.setLayoutParams(rowLp);
        for (int i = 0; i < cards.size(); i++) {
            View c = cards.get(i);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            if (i < cards.size() - 1) lp.rightMargin = dp(8);
            c.setLayoutParams(lp);
            row.addView(c);
        }
        return row;
    }

    private View metricCard(int iconRes, String label, String value, String extra) {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setBackgroundResource(R.drawable.bg_cmp_card);
        c.setPadding(dp(10), dp(10), dp(10), dp(10));

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        ImageView ic = new ImageView(this);
        LinearLayout.LayoutParams icLp = new LinearLayout.LayoutParams(dp(15), dp(15));
        icLp.rightMargin = dp(5);
        ic.setLayoutParams(icLp);
        ic.setImageResource(iconRes);
        top.addView(ic);
        TextView lab = new TextView(this);
        lab.setText(label);
        lab.setTextColor(ContextCompat.getColor(this, R.color.cmp_text_secondary));
        lab.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        lab.setMaxLines(2);
        lab.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        top.addView(lab);
        c.addView(top);

        LinearLayout valRow = new LinearLayout(this);
        valRow.setOrientation(LinearLayout.HORIZONTAL);
        valRow.setGravity(Gravity.BOTTOM);
        valRow.setPadding(0, dp(6), 0, 0);
        TextView val = new TextView(this);
        val.setText(value);
        val.setTextColor(ContextCompat.getColor(this, R.color.cmp_text_primary));
        val.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        val.setTypeface(Typeface.DEFAULT_BOLD);
        valRow.addView(val);
        if (!TextUtils.isEmpty(extra)) {
            TextView ex = new TextView(this);
            ex.setText(" (" + extra + ")");
            ex.setTextColor(ContextCompat.getColor(this, R.color.cmp_brand_orange));
            ex.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
            ex.setPadding(dp(2), 0, 0, dp(1));
            valRow.addView(ex);
        }
        c.addView(valRow);
        return c;
    }

    /** Overs remaining parsed from an equation like "… in 4.4 overs". */
    private static String extractOversRemaining(String text) {
        if (TextUtils.isEmpty(text)) return "";
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("([0-9]+(?:\\.[0-9]+)?)\\s*over", java.util.regex.Pattern.CASE_INSENSITIVE)
                .matcher(text);
        return m.find() ? m.group(1) : "";
    }

    /** Target parsed from "target N", else derived from "<team> need N runs". */
    private String extractTarget(String text) {
        if (TextUtils.isEmpty(text)) return "";
        java.util.regex.Matcher tm = java.util.regex.Pattern
                .compile("target\\s+(?:of\\s+)?([0-9]+)", java.util.regex.Pattern.CASE_INSENSITIVE)
                .matcher(text);
        if (tm.find()) return tm.group(1);

        java.util.regex.Matcher nm = java.util.regex.Pattern
                .compile("need\\s+([0-9]+)\\s+run", java.util.regex.Pattern.CASE_INSENSITIVE)
                .matcher(text);
        if (nm.find() && match != null) {
            try {
                int need = Integer.parseInt(nm.group(1));
                String lower = text.toLowerCase();
                String chasingScore = null;
                String t1 = fullTeam(match.getTeam1_code(), match.getTeam1_name()).toLowerCase();
                String t2 = fullTeam(match.getTeam2_code(), match.getTeam2_name()).toLowerCase();
                if (!TextUtils.isEmpty(t1) && lower.contains(t1)) chasingScore = match.getTeam1_score();
                else if (!TextUtils.isEmpty(t2) && lower.contains(t2)) chasingScore = match.getTeam2_score();
                if (!TextUtils.isEmpty(chasingScore)) {
                    String runsPart = chasingScore.contains("/")
                            ? chasingScore.substring(0, chasingScore.indexOf('/')) : chasingScore;
                    runsPart = runsPart.replaceAll("[^0-9]", "");
                    if (!runsPart.isEmpty()) return String.valueOf(Integer.parseInt(runsPart) + need);
                }
            } catch (Exception ignored) {
            }
        }
        return "";
    }

    // ---- Fall of Wicket bar -------------------------------------------------

    private View buildFowBar(CricketMatchDetailResponse.Innings inn) {
        if (inn == null || inn.getFall_of_wickets() == null || inn.getFall_of_wickets().isEmpty()) {
            return null;
        }
        List<CricketMatchDetailResponse.FallOfWicket> list = inn.getFall_of_wickets();
        CricketMatchDetailResponse.FallOfWicket fow = list.get(list.size() - 1);

        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setBackgroundResource(R.drawable.bg_cmp_fow_bar);
        bar.setPadding(dp(14), dp(12), dp(12), dp(12));
        LinearLayout.LayoutParams barLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        barLp.bottomMargin = dp(6);
        bar.setLayoutParams(barLp);

        ImageView ic = new ImageView(this);
        LinearLayout.LayoutParams icLp = new LinearLayout.LayoutParams(dp(22), dp(22));
        icLp.rightMargin = dp(10);
        ic.setLayoutParams(icLp);
        ic.setImageResource(R.drawable.ic_cmp_stumps);
        bar.addView(ic);

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        TextView title = new TextView(this);
        title.setText("Fall of Wicket");
        title.setTextColor(0xFFFFFFFF);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        col.addView(title);
        if (!TextUtils.isEmpty(fow.getBatsman())) {
            TextView bt = new TextView(this);
            bt.setText(fow.getBatsman());
            bt.setTextColor(0xFFFFFFFF);
            bt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            bt.setTypeface(Typeface.DEFAULT_BOLD);
            bt.setPadding(0, dp(2), 0, 0);
            col.addView(bt);
        }
        if (!TextUtils.isEmpty(fow.getScore())) {
            TextView sc = new TextView(this);
            sc.setText(fow.getScore());
            sc.setTextColor(0xE6FFFFFF);
            sc.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            col.addView(sc);
        }
        bar.addView(col);

        String total = inn.getScore();
        if (TextUtils.isEmpty(total)) total = scoreOrDash(match != null ? match.getTeam1_score() : "",
                match != null ? match.getTeam1_overs() : "");
        if (!TextUtils.isEmpty(total)) {
            TextView tot = new TextView(this);
            tot.setText(total);
            tot.setTextColor(0xFFFFFFFF);
            tot.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
            tot.setTypeface(Typeface.DEFAULT_BOLD);
            tot.setPadding(dp(8), 0, dp(6), 0);
            bar.addView(tot);
        }

        ImageView chev = new ImageView(this);
        chev.setLayoutParams(new LinearLayout.LayoutParams(dp(20), dp(20)));
        chev.setImageResource(R.drawable.ic_cmp_chevron_down);
        bar.addView(chev);

        bar.setOnClickListener(v -> openAllOvers());
        return bar;
    }

    private View iconLabelRow(int iconRes, String text) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        ImageView ic = new ImageView(this);
        LinearLayout.LayoutParams icLp = new LinearLayout.LayoutParams(dp(16), dp(16));
        icLp.rightMargin = dp(6);
        ic.setLayoutParams(icLp);
        ic.setImageResource(iconRes);
        row.addView(ic);
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextColor(ContextCompat.getColor(this, R.color.cmp_text_secondary));
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        row.addView(t);
        return row;
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
        return card(LinearLayout.VERTICAL);
    }

    private LinearLayout card(int orientation) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(orientation);
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
