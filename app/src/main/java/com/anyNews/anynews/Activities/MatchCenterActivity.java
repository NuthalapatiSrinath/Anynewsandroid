package com.anyNews.anynews.Activities;

import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
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
import com.anyNews.anynews.Services.CommonUtilities;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Utils.LiveBadgeBlinkHelper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
 * live stats, match info, summary/top performers and
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
    private View tabIndicator;
    private View tabBar;
    private View contentLive, contentPlaceholder;
    private LinearLayout contentScorecard;
    private LinearLayout liveDynamic;
    private TextView placeholderTitle, placeholderMessage;

    // Two tabs only — 1 Live · 2 Scorecard. Scorecard consolidates everything
    // that used to live on the removed Summary / Commentary / Stats tabs.
    private int currentTab = 1;
    private boolean initialTabChosen = false;
    private boolean tabUiReady = false;
    private static final long TAB_ANIM_MS = 220;

    // Scorecard "Overs" list: show newest first, 3 at a time, then Load more.
    private static final int OVERS_PAGE_SIZE = 3;
    private int oversVisibleCount = OVERS_PAGE_SIZE;

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

    /** Whole-pill opacity blink for header + centre LIVE chips while match is live. */
    private ObjectAnimator headerLiveBlink;
    private ObjectAnimator centerLiveBlink;

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
        tabIndicator = findViewById(R.id.tab_indicator);
        tabBar = findViewById(R.id.mc_tab_bar);
        contentLive = findViewById(R.id.content_live);
        liveDynamic = findViewById(R.id.live_dynamic);
        contentScorecard = findViewById(R.id.content_scorecard);
        contentPlaceholder = findViewById(R.id.content_placeholder);
        placeholderTitle = findViewById(R.id.placeholder_title);
        placeholderMessage = findViewById(R.id.placeholder_message);

        tabLive.setOnClickListener(v -> selectTab(1, true));
        tabScorecard.setOnClickListener(v -> selectTab(2, true));

        bind();
        selectTab(1, false);

        // Position the sliding underline once the tab bar has a real width.
        tabBar.post(() -> {
            tabUiReady = true;
            moveTabIndicator(currentTab, false);
        });

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

    @Override
    protected void onDestroy() {
        stopLiveBadgeBlink();
        super.onDestroy();
    }

    /**
     * targetSdk 35 forces edge-to-edge, so add the status-bar inset to the
     * header CONTENT top padding (the stadium photo still fills behind the
     * status bar) and the navigation-bar inset to the scroll content bottom.
     * White status-bar icons read cleanly over the orange header.
     */
    private void applyWindowInsets() {
        final View header = findViewById(R.id.mc_header);
        final View headerContent = findViewById(R.id.mc_header_content);
        final View scroll = findViewById(R.id.mc_scroll);
        final int headerPadTop = headerContent.getPaddingTop();
        final int scrollPadBottom = scroll.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mc_root), (v, insets) -> {
            int top = insets.getSystemWindowInsetTop();
            int bottom = insets.getSystemWindowInsetBottom();
            headerContent.setPadding(headerContent.getPaddingLeft(), headerPadTop + top,
                    headerContent.getPaddingRight(), headerContent.getPaddingBottom());
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
        View liveBadge = findViewById(R.id.mc_live_badge);

        series.setText(match.getSeries_name());
        title.setText(matchHeading());

        boolean live = match.isLive();
        headerLiveBlink = LiveBadgeBlinkHelper.sync(liveBadge, live, headerLiveBlink);

        // Team score rows (flag + name + big score + overs + run rate).
        bindTeam(match.getTeam1_flag(), match.getTeam1_code(),
                fullTeam(match.getTeam1_code(), match.getTeam1_name()),
                match.getTeam1_score(), match.getTeam1_overs(),
                R.id.mc_team1_flag, R.id.mc_team1_name, R.id.mc_team1_score,
                R.id.mc_team1_overs, R.id.mc_team1_rr);
        bindTeam(match.getTeam2_flag(), match.getTeam2_code(),
                fullTeam(match.getTeam2_code(), match.getTeam2_name()),
                match.getTeam2_score(), match.getTeam2_overs(),
                R.id.mc_team2_flag, R.id.mc_team2_name, R.id.mc_team2_score,
                R.id.mc_team2_overs, R.id.mc_team2_rr);

        // Centre LIVE badge + a compact format tag (e.g. T20) when available.
        View centerLive = findViewById(R.id.mc_center_live);
        TextView centerSub = findViewById(R.id.mc_center_sub);
        centerLiveBlink = LiveBadgeBlinkHelper.sync(centerLive, live, centerLiveBlink);
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

    private void stopLiveBadgeBlink() {
        headerLiveBlink = LiveBadgeBlinkHelper.stop(
                findViewById(R.id.mc_live_badge), headerLiveBlink);
        centerLiveBlink = LiveBadgeBlinkHelper.stop(
                findViewById(R.id.mc_center_live), centerLiveBlink);
    }

    /** Fill one team's flag/name/score/overs/run-rate slot in the hero header. */
    private void bindTeam(String flagUrl, String teamCode, String name, String score, String overs,
                          int flagId, int nameId, int scoreId, int oversId, int rrId) {
        ImageView flag = findViewById(flagId);
        TextView nameView = findViewById(nameId);
        TextView scoreView = findViewById(scoreId);
        TextView oversView = findViewById(oversId);
        TextView rrView = findViewById(rrId);

        nameView.setText(name);
        loadTeamFlag(flag, flagUrl, teamCode, name);

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

    /**
     * CREX team flags live at cricketvectors.akamaized.net/Teams/{code}.png.
     * Admin settings may supply a full URL, a protocol-relative URL, a relative
     * path, or nothing — in which case we build the CDN URL from the team code.
     */
    private static final String CREX_FLAG_CDN = "https://cricketvectors.akamaized.net/Teams/";

    private void loadTeamFlag(ImageView flag, String flagUrl, String teamCode, String teamName) {
        if (flag == null) return;
        flag.setScaleType(ImageView.ScaleType.CENTER_CROP);
        flag.setBackgroundResource(R.drawable.bg_cmp_flag_circle);
        Drawable placeholder = initialsFlagDrawable(teamCode, teamName);
        String url = resolveFlagUrl(flagUrl, teamCode);
        if (TextUtils.isEmpty(url)) {
            flag.setImageDrawable(placeholder);
            return;
        }
        try {
            Glide.with(this)
                    .load(url)
                    .circleCrop()
                    .placeholder(placeholder)
                    .error(placeholder)
                    .into(flag);
        } catch (Exception e) {
            flag.setImageDrawable(placeholder);
        }
    }

    private String resolveFlagUrl(String flagUrl, String teamCode) {
        String url = flagUrl == null ? "" : flagUrl.trim();
        if (!TextUtils.isEmpty(url)) {
            if (url.startsWith("//")) return "https:" + url;
            if (url.startsWith("/")) {
                String base = CommonUtilities.URL;
                if (base.endsWith("/")) base = base.substring(0, base.length() - 1);
                return base + url;
            }
            if (url.startsWith("http://") || url.startsWith("https://")) return url;
            // Bare filename or team-code-like token from the feed.
            if (url.toLowerCase(Locale.US).endsWith(".png") || url.contains("/")) {
                return "https://cricketvectors.akamaized.net/" + url.replaceFirst("^/+", "");
            }
            return CREX_FLAG_CDN + url.replaceFirst("^\\^", "") + ".png";
        }
        String code = teamCode == null ? "" : teamCode.trim().replaceFirst("^\\^", "");
        if (!TextUtils.isEmpty(code)) return CREX_FLAG_CDN + code + ".png";
        return "";
    }

    /** Circular initials plate used as Glide placeholder / error for team flags. */
    private Drawable initialsFlagDrawable(String teamCode, String teamName) {
        String initials = teamInitials(teamCode, teamName);
        int size = dp(34);
        Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        Paint bg = new Paint(Paint.ANTI_ALIAS_FLAG);
        bg.setColor(0x55FFFFFF);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, bg);
        Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
        text.setColor(0xFFFFFFFF);
        text.setTypeface(Typeface.DEFAULT_BOLD);
        text.setTextAlign(Paint.Align.CENTER);
        text.setTextSize(size * (initials.length() > 2 ? 0.32f : 0.38f));
        Paint.FontMetrics fm = text.getFontMetrics();
        float cy = size / 2f - (fm.ascent + fm.descent) / 2f;
        canvas.drawText(initials, size / 2f, cy, text);
        return new BitmapDrawable(getResources(), bmp);
    }

    private static String teamInitials(String teamCode, String teamName) {
        String code = teamCode == null ? "" : teamCode.trim().replaceFirst("^\\^", "");
        if (!TextUtils.isEmpty(code) && code.length() <= 4 && !code.contains(" ")) {
            return code.toUpperCase(Locale.US);
        }
        String name = !TextUtils.isEmpty(teamName) ? teamName.trim() : code;
        if (TextUtils.isEmpty(name)) return "?";
        String[] parts = name.split("\\s+");
        if (parts.length >= 2) {
            return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase(Locale.US);
        }
        return name.substring(0, Math.min(3, name.length())).toUpperCase(Locale.US);
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
        selectTab(index, false);
    }

    private void selectTab(int index, boolean animate) {
        if (index != 1 && index != 2) return;
        boolean switching = tabUiReady && currentTab != index;
        int from = currentTab;
        currentTab = index;

        int active = ContextCompat.getColor(this, R.color.cmp_brand_orange);
        int inactive = ContextCompat.getColor(this, R.color.cmp_text_secondary);
        tabLive.setTextColor(index == 1 ? active : inactive);
        tabScorecard.setTextColor(index == 2 ? active : inactive);
        moveTabIndicator(index, animate && switching);

        contentPlaceholder.setVisibility(View.GONE);
        contentPlaceholder.animate().cancel();
        contentLive.animate().cancel();
        contentScorecard.animate().cancel();

        if (animate && switching) {
            // Preempt a one-frame flash of the incoming panel at full opacity.
            View incoming = index == 1 ? contentLive : contentScorecard;
            incoming.setAlpha(0f);
            incoming.setVisibility(View.VISIBLE);
        }

        renderCurrentTab();

        if (animate && switching) {
            animateTabContent(from, index);
        } else {
            contentLive.setAlpha(1f);
            contentLive.setTranslationX(0f);
            contentScorecard.setAlpha(1f);
            contentScorecard.setTranslationX(0f);
            contentLive.setVisibility(index == 1 ? View.VISIBLE : View.GONE);
            contentScorecard.setVisibility(index == 2 ? View.VISIBLE : View.GONE);
        }
    }

    /** Slide the shared orange underline under the active tab label. */
    private void moveTabIndicator(int index, boolean animate) {
        if (tabIndicator == null || tabBar == null) return;
        int barW = tabBar.getWidth();
        int indW = tabIndicator.getWidth();
        if (barW <= 0 || indW <= 0) {
            tabBar.post(() -> moveTabIndicator(index, animate));
            return;
        }
        float half = barW / 2f;
        float target = (index == 1 ? half * 0.5f : half * 1.5f) - indW / 2f;
        tabIndicator.animate().cancel();
        if (animate) {
            tabIndicator.animate()
                    .translationX(target)
                    .setDuration(TAB_ANIM_MS)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        } else {
            tabIndicator.setTranslationX(target);
        }
    }

    /** Crossfade + short horizontal slide between Live and Scorecard panels. */
    private void animateTabContent(int from, int to) {
        final View hide = from == 1 ? contentLive : contentScorecard;
        final View show = to == 1 ? contentLive : contentScorecard;
        float slide = dp(22);
        int dir = to > from ? 1 : -1;

        show.setVisibility(View.VISIBLE);
        show.setAlpha(0f);
        show.setTranslationX(dir * slide);
        hide.setVisibility(View.VISIBLE);

        hide.animate()
                .alpha(0f)
                .translationX(-dir * slide)
                .setDuration(TAB_ANIM_MS)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> {
                    if (currentTab != from) {
                        hide.setVisibility(View.GONE);
                        hide.setAlpha(1f);
                        hide.setTranslationX(0f);
                    }
                })
                .start();

        show.animate()
                .alpha(1f)
                .translationX(0f)
                .setDuration(TAB_ANIM_MS)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> {
                    // Always land visible — a cancelled/interrupted fade must not
                    // leave the active panel at alpha 0 (empty-looking content).
                    if (currentTab == to) {
                        show.setAlpha(1f);
                        show.setTranslationX(0f);
                        show.setVisibility(View.VISIBLE);
                    }
                })
                .start();
    }

    private void renderCurrentTab() {
        if (currentTab == 2) {
            renderScorecard();
        } else {
            renderLive();
        }
    }

    private void showEmptyFor(String title, String message) {
        contentLive.animate().cancel();
        contentScorecard.animate().cancel();
        contentLive.setVisibility(View.GONE);
        contentScorecard.setVisibility(View.GONE);
        contentLive.setAlpha(1f);
        contentLive.setTranslationX(0f);
        contentScorecard.setAlpha(1f);
        contentScorecard.setTranslationX(0f);
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
        loadTeamFlag(flag, t.getFlag(), t.getShort_name(), t.getTeam());
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
        // Visibility / crossfade is owned by selectTab — only force-show when
        // this is the active tab and no transition is in flight.
        if (currentTab == 1 && contentLive.getVisibility() != View.VISIBLE) {
            contentLive.setVisibility(View.VISIBLE);
            contentLive.setAlpha(1f);
            contentLive.setTranslationX(0f);
        }
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
     * Scorecard tab (CREX-style): one white innings card per innings — a team
     * header (flag + name + total), the batting table (R/B/4s/6s/SR), an orange
     * Extras and Total row, a grey Yet to Bat list and the innings' bowling +
     * fall-of-wickets — followed by a Match Details card (match / venue / date /
     * toss). Sections consolidated from the removed tabs that no longer fit this
     * clean layout (live stats, summary / player of the match / top performers)
     * are appended BELOW the Match Details card so no snapshot data is lost.
     * Ball-by-ball commentary is NOT dumped here — instead a paginated
     * over-wise summary list (last 3 + Load more) is shown. Every value binds
     * to the DB snapshot; sections with no data are omitted.
     */
    private void renderScorecard() {
        contentScorecard.removeAllViews();
        boolean any = false;

        // Innings cards — the primary scorecard content, one per innings.
        List<CricketMatchDetailResponse.Innings> innings = detail != null ? detail.getScorecards() : null;
        if (innings != null && !innings.isEmpty()) {
            for (CricketMatchDetailResponse.Innings inn : innings) {
                contentScorecard.addView(buildInningsCard(inn));
            }
            any = true;
        }

        // Match Details card (match / venue / date / toss) from the info snapshot.
        View matchDetails = buildMatchDetailsCard();
        if (matchDetails != null) {
            contentScorecard.addView(matchDetails);
            any = true;
        }

        // Sections that do not fit the clean scorecard layout are kept BELOW the
        // Match Details card so no snapshot data is lost. Ball-by-ball commentary
        // is replaced by a paginated over-wise list (last 3 + Load more).
        any |= appendStats(contentScorecard);
        any |= appendSummary(contentScorecard);
        any |= appendOversList(contentScorecard);

        if (!any) {
            showEmptyFor("Scorecard", detail == null
                    ? "Loading scorecard…"
                    : "The full batting & bowling scorecard will appear here once the match is under way.");
            return;
        }
        contentPlaceholder.setVisibility(View.GONE);
        if (currentTab == 2 && contentScorecard.getVisibility() != View.VISIBLE) {
            contentScorecard.setVisibility(View.VISIBLE);
            contentScorecard.setAlpha(1f);
            contentScorecard.setTranslationX(0f);
        }
    }

    /** One innings as a white rounded card matching the CREX scorecard layout. */
    private View buildInningsCard(CricketMatchDetailResponse.Innings inn) {
        LinearLayout card = card();

        // Team header: circular flag + team name (left), total score (right).
        card.addView(buildInningsHeader(inn));

        // Batting table header + one row per batter; collect the did-not-bats.
        card.addView(thinDivider());
        card.addView(scHeaderRow());
        List<CricketMatchDetailResponse.Batting> batting = inn.getBatting();
        StringBuilder yetToBat = new StringBuilder();
        boolean anyBatter = false;
        if (batting != null) {
            for (CricketMatchDetailResponse.Batting b : batting) {
                if (b.isDid_not_bat()) {
                    if (yetToBat.length() > 0) yetToBat.append(", ");
                    yetToBat.append(b.getName());
                    continue;
                }
                card.addView(thinDivider());
                card.addView(scBattingRow(b));
                anyBatter = true;
            }
        }
        if (!anyBatter) {
            card.addView(mutedLine("Yet to bat."));
        }

        // Extras (orange label) + Total (all orange), mirroring the reference.
        if (!TextUtils.isEmpty(inn.getExtras())) {
            card.addView(thinDivider());
            card.addView(accentRow("Extras", inn.getExtras(), false));
        }
        String total = innTotalText(inn);
        if (!TextUtils.isEmpty(total)) {
            card.addView(thinDivider());
            card.addView(accentRow("Total", total, true));
        }

        // Yet to Bat — grey label + comma-separated names.
        if (yetToBat.length() > 0) {
            card.addView(thinDivider());
            card.addView(buildYetToBat(yetToBat.toString()));
        }

        // Bowling figures for this innings (retained scorecard data).
        List<CricketMatchDetailResponse.Bowling> bowling = inn.getBowling();
        if (bowling != null && !bowling.isEmpty()) {
            card.addView(thinDivider());
            card.addView(scBowlHeaderRow());
            for (CricketMatchDetailResponse.Bowling bw : bowling) {
                card.addView(thinDivider());
                card.addView(scBowlingRow(bw));
            }
        }

        // Fall of wickets (retained scorecard data).
        List<CricketMatchDetailResponse.FallOfWicket> fow = inn.getFall_of_wickets();
        if (fow != null && !fow.isEmpty()) {
            card.addView(thinDivider());
            card.addView(buildFallOfWickets(fow));
        }

        return card;
    }

    /** Innings card header: circular flag + team name on the left, total right. */
    private View buildInningsHeader(CricketMatchDetailResponse.Innings inn) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, 0, 0, dp(6));

        ImageView flag = new ImageView(this);
        LinearLayout.LayoutParams flagLp = new LinearLayout.LayoutParams(dp(26), dp(26));
        flagLp.rightMargin = dp(10);
        flag.setLayoutParams(flagLp);
        String flagUrl = flagForTeam(inn.getTeam_name());
        String code = codeForTeam(inn.getTeam_name());
        loadTeamFlag(flag, flagUrl, code, inn.getTeam_name());
        row.addView(flag);

        TextView name = new TextView(this);
        name.setText(inn.getTeam_name());
        name.setTextColor(color(R.color.cmp_text_primary));
        name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        name.setTypeface(Typeface.DEFAULT_BOLD);
        name.setMaxLines(1);
        name.setEllipsize(TextUtils.TruncateAt.END);
        name.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(name);

        String total = innTotalText(inn);
        if (!TextUtils.isEmpty(total)) {
            TextView tot = new TextView(this);
            tot.setText(total);
            tot.setTextColor(color(R.color.cmp_text_primary));
            tot.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            tot.setTypeface(Typeface.DEFAULT_BOLD);
            tot.setPadding(dp(8), 0, 0, 0);
            row.addView(tot);
        }
        return row;
    }

    private LinearLayout scHeaderRow() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(6), 0, dp(6));
        int grey = color(R.color.cmp_text_secondary);
        row.addView(scCell("Batting", 3.6f, Gravity.START, grey, 11.5f, false));
        row.addView(scCell("R", 1.0f, Gravity.CENTER, grey, 11.5f, false));
        row.addView(scCell("B", 1.0f, Gravity.CENTER, grey, 11.5f, false));
        row.addView(scCell("4s", 1.0f, Gravity.CENTER, grey, 11.5f, false));
        row.addView(scCell("6s", 1.0f, Gravity.CENTER, grey, 11.5f, false));
        row.addView(scCell("SR", 1.6f, Gravity.END, grey, 11.5f, false));
        return row;
    }

    private View scBattingRow(CricketMatchDetailResponse.Batting b) {
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(9), 0, dp(9));
        int dark = color(R.color.cmp_text_primary);

        boolean striker = isStrikerInScorecard(b.getName());
        String name = b.getName() + (striker ? " *" : "");
        row.addView(scCell(name, 3.6f, Gravity.START, dark, 13f, striker));
        row.addView(scCell(String.valueOf(b.getRuns()), 1.0f, Gravity.CENTER, dark, 13f, true));
        row.addView(scCell(String.valueOf(b.getBalls()), 1.0f, Gravity.CENTER, dark, 13f, false));
        row.addView(scCell(String.valueOf(b.getFours()), 1.0f, Gravity.CENTER, dark, 13f, false));
        row.addView(scCell(String.valueOf(b.getSixes()), 1.0f, Gravity.CENTER, dark, 13f, false));
        row.addView(scCell(TextUtils.isEmpty(b.getStrike_rate()) ? "-" : b.getStrike_rate(),
                1.6f, Gravity.END, dark, 13f, false));
        wrap.addView(row);

        // Dismissal sub-line for out batters (retains "how out"; not-out shows none).
        String note = "batting".equalsIgnoreCase(b.getStatus()) ? null : b.getDismissal();
        if (!TextUtils.isEmpty(note)) {
            TextView sub = new TextView(this);
            sub.setText(note);
            sub.setTextColor(color(R.color.cmp_text_secondary));
            sub.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            sub.setPadding(0, 0, 0, dp(6));
            wrap.addView(sub);
        }
        return wrap;
    }

    private LinearLayout scBowlHeaderRow() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(6), 0, dp(6));
        int grey = color(R.color.cmp_text_secondary);
        row.addView(scCell("Bowling", 3.6f, Gravity.START, grey, 11.5f, false));
        row.addView(scCell("O", 1.2f, Gravity.CENTER, grey, 11.5f, false));
        row.addView(scCell("R", 1.0f, Gravity.CENTER, grey, 11.5f, false));
        row.addView(scCell("W", 1.0f, Gravity.CENTER, grey, 11.5f, false));
        row.addView(scCell("Econ", 1.6f, Gravity.END, grey, 11.5f, false));
        return row;
    }

    private View scBowlingRow(CricketMatchDetailResponse.Bowling bw) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(9), 0, dp(9));
        int dark = color(R.color.cmp_text_primary);
        row.addView(scCell(bw.getName(), 3.6f, Gravity.START, dark, 13f, false));
        row.addView(scCell(TextUtils.isEmpty(bw.getOvers()) ? "-" : bw.getOvers(),
                1.2f, Gravity.CENTER, dark, 13f, false));
        row.addView(scCell(String.valueOf(bw.getRuns()), 1.0f, Gravity.CENTER, dark, 13f, false));
        row.addView(scCell(String.valueOf(bw.getWickets()), 1.0f, Gravity.CENTER, dark, 13f, true));
        row.addView(scCell(TextUtils.isEmpty(bw.getEconomy()) ? "-" : bw.getEconomy(),
                1.6f, Gravity.END, dark, 13f, false));
        return row;
    }

    private View buildFallOfWickets(List<CricketMatchDetailResponse.FallOfWicket> fow) {
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setPadding(0, dp(8), 0, dp(2));

        TextView label = new TextView(this);
        label.setText("Fall of Wickets");
        label.setTextColor(color(R.color.cmp_text_secondary));
        label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        label.setTypeface(Typeface.DEFAULT_BOLD);
        col.addView(label);

        StringBuilder sb = new StringBuilder();
        for (CricketMatchDetailResponse.FallOfWicket f : fow) {
            if (sb.length() > 0) sb.append("   ");
            sb.append(f.getScore());
            if (!TextUtils.isEmpty(f.getBatsman())) sb.append(" (").append(f.getBatsman()).append(')');
        }
        TextView val = new TextView(this);
        val.setText(sb.toString());
        val.setTextColor(color(R.color.cmp_text_secondary));
        val.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
        val.setPadding(0, dp(3), 0, 0);
        col.addView(val);
        return col;
    }

    /** Orange-accented Extras / Total row (Total's value is orange too). */
    private View accentRow(String label, String value, boolean valueOrange) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(9), 0, dp(9));
        int orange = color(R.color.cmp_brand_orange);

        TextView k = new TextView(this);
        k.setText(label);
        k.setTextColor(orange);
        k.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        k.setTypeface(Typeface.DEFAULT_BOLD);
        k.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(k);

        TextView v = new TextView(this);
        v.setText(value);
        v.setTextColor(valueOrange ? orange : color(R.color.cmp_text_primary));
        v.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        v.setTypeface(Typeface.DEFAULT_BOLD);
        v.setGravity(Gravity.END);
        v.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 2.4f));
        row.addView(v);
        return row;
    }

    private View buildYetToBat(String names) {
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setPadding(0, dp(8), 0, dp(2));

        TextView label = new TextView(this);
        label.setText("Yet to Bat");
        label.setTextColor(color(R.color.cmp_text_secondary));
        label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        label.setTypeface(Typeface.DEFAULT_BOLD);
        col.addView(label);

        TextView val = new TextView(this);
        val.setText(names);
        val.setTextColor(color(R.color.cmp_text_secondary));
        val.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
        val.setPadding(0, dp(3), 0, 0);
        col.addView(val);
        return col;
    }

    /** Bottom "Match Details" card — match / venue / date / toss label rows. */
    private View buildMatchDetailsCard() {
        if (detail == null) return null;
        CricketMatchDetailResponse.Info info = detail.getInfo();

        java.util.List<String[]> rows = new java.util.ArrayList<>();

        String matchLine = matchHeading();
        String num = info != null ? info.getMatch_number() : "";
        if (!TextUtils.isEmpty(matchLine)) {
            if (!TextUtils.isEmpty(num)) matchLine = matchLine + ", " + num;
            rows.add(new String[]{"Match", matchLine});
        }
        String venue = info != null && !TextUtils.isEmpty(info.getVenue())
                ? info.getVenue()
                : (match != null ? match.getVenue() : "");
        if (!TextUtils.isEmpty(venue)) rows.add(new String[]{"Venue", venue});
        String date = info != null ? formatMatchDate(info.getStart_time()) : "";
        if (!TextUtils.isEmpty(date)) rows.add(new String[]{"Date", date});
        String toss = info != null ? info.getToss() : "";
        if (!TextUtils.isEmpty(toss)) rows.add(new String[]{"Toss", toss});

        if (rows.isEmpty()) return null;

        LinearLayout card = card();
        card.addView(sectionTitle("Match Details"));
        for (int i = 0; i < rows.size(); i++) {
            card.addView(thinDivider());
            card.addView(detailRow(rows.get(i)[0], rows.get(i)[1]));
        }
        return card;
    }

    private View detailRow(String label, String value) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(9), 0, dp(9));

        TextView k = new TextView(this);
        k.setText(label);
        k.setTextColor(color(R.color.cmp_text_secondary));
        k.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        k.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(k);

        TextView v = new TextView(this);
        v.setText(value);
        v.setTextColor(color(R.color.cmp_text_primary));
        v.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        v.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 2.4f));
        row.addView(v);
        return row;
    }

    /** Full team flag URL for an innings, matched by team name against the header. */
    private String flagForTeam(String teamName) {
        if (match == null || TextUtils.isEmpty(teamName)) return "";
        if (teamMatches(teamName, match.getTeam1_name(), match.getTeam1_code())) return match.getTeam1_flag();
        if (teamMatches(teamName, match.getTeam2_name(), match.getTeam2_code())) return match.getTeam2_flag();
        // Summary snapshot often carries absolute CDN flag URLs even when the
        // active_match settings left team*_flag blank.
        if (detail != null && detail.getSummary() != null && detail.getSummary().getTeams() != null) {
            for (CricketMatchDetailResponse.SummaryTeam t : detail.getSummary().getTeams()) {
                if (teamMatches(teamName, t.getTeam(), t.getShort_name()) && !TextUtils.isEmpty(t.getFlag())) {
                    return t.getFlag();
                }
            }
        }
        return "";
    }

    private String codeForTeam(String teamName) {
        if (match == null || TextUtils.isEmpty(teamName)) return "";
        if (teamMatches(teamName, match.getTeam1_name(), match.getTeam1_code())) return match.getTeam1_code();
        if (teamMatches(teamName, match.getTeam2_name(), match.getTeam2_code())) return match.getTeam2_code();
        return "";
    }

    /** Overs for an innings' team, taken from the header active_match feed. */
    private String oversForTeam(String teamName) {
        if (match == null || TextUtils.isEmpty(teamName)) return "";
        if (teamMatches(teamName, match.getTeam1_name(), match.getTeam1_code())) return match.getTeam1_overs();
        if (teamMatches(teamName, match.getTeam2_name(), match.getTeam2_code())) return match.getTeam2_overs();
        return "";
    }

    private static boolean teamMatches(String teamName, String name, String code) {
        return namesMatch(teamName, name) || namesMatch(teamName, code);
    }

    /** "44/1 (21.2 Overs)" — innings score plus overs when the feed carries them. */
    private String innTotalText(CricketMatchDetailResponse.Innings inn) {
        String score = inn.getScore();
        if (TextUtils.isEmpty(score)) return "";
        String overs = oversForTeam(inn.getTeam_name());
        if (!TextUtils.isEmpty(overs)) return score + " (" + overs + " Overs)";
        return score;
    }

    /** True when the scorecard batter is currently on strike per the live feed. */
    private boolean isStrikerInScorecard(String name) {
        if (detail == null || detail.getLive() == null || TextUtils.isEmpty(name)) return false;
        List<CricketMatchDetailResponse.Batsman> bs = detail.getLive().getBatsmen();
        if (bs == null) return false;
        for (CricketMatchDetailResponse.Batsman b : bs) {
            if (b.isOn_strike() && namesMatch(b.getName(), name)) return true;
        }
        return false;
    }

    /** Format an ISO / epoch start_time into "17 July 2026"; blank on failure. */
    private String formatMatchDate(String raw) {
        if (TextUtils.isEmpty(raw)) return "";
        String r = raw.trim();
        try {
            java.text.SimpleDateFormat out =
                    new java.text.SimpleDateFormat("d MMMM yyyy", java.util.Locale.US);
            if (r.matches("\\d{10,}")) {
                long v = Long.parseLong(r);
                long ms = r.length() <= 10 ? v * 1000L : v;
                return out.format(new java.util.Date(ms));
            }
            String datePart = r.contains("T") ? r.substring(0, r.indexOf('T')) : r;
            java.text.SimpleDateFormat in =
                    new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US);
            java.util.Date d = in.parse(datePart);
            if (d != null) return out.format(d);
        } catch (Exception ignored) {
        }
        return "";
    }

    private int color(int res) {
        return ContextCompat.getColor(this, res);
    }

    private TextView scCell(String text, float weight, int gravity, int textColor,
                            float sizeSp, boolean bold) {
        TextView t = new TextView(this);
        t.setText(text == null ? "" : text);
        t.setGravity(gravity);
        t.setMaxLines(1);
        t.setEllipsize(TextUtils.TruncateAt.END);
        t.setTextColor(textColor);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp);
        if (bold) t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, weight));
        return t;
    }

    private View thinDivider() {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Math.max(1, dp(0.5f))));
        v.setBackgroundColor(color(R.color.cmp_border));
        return v;
    }

    // ---- Overs list (Scorecard — replaces ball-by-ball commentary dump) -----

    /**
     * Admin Live Hub "Overs" style: OVER N + team score on the right, batsman /
     * bowler summary underneath. Shows the newest {@link #OVERS_PAGE_SIZE}
     * overs first; "Load more overs" reveals the next older page of 3.
     */
    private boolean appendOversList(LinearLayout container) {
        List<OverSummaryRow> rows = buildOverSummaryRows();
        if (rows.isEmpty()) return false;

        if (oversVisibleCount < OVERS_PAGE_SIZE) oversVisibleCount = OVERS_PAGE_SIZE;
        if (oversVisibleCount > rows.size()) oversVisibleCount = rows.size();

        LinearLayout card = card();
        card.addView(sectionTitle("Overs"));

        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        card.addView(list);

        int show = Math.min(oversVisibleCount, rows.size());
        for (int i = 0; i < show; i++) {
            if (i > 0) list.addView(thinDivider());
            list.addView(buildOverSummaryView(rows.get(i)));
        }

        if (show < rows.size()) {
            TextView more = new TextView(this);
            more.setText("Load more overs");
            more.setTextColor(color(R.color.cmp_brand_orange));
            more.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            more.setTypeface(Typeface.DEFAULT_BOLD);
            more.setGravity(Gravity.CENTER);
            more.setPadding(0, dp(14), 0, dp(4));
            more.setOnClickListener(v -> {
                oversVisibleCount = Math.min(rows.size(), oversVisibleCount + OVERS_PAGE_SIZE);
                renderScorecard();
            });
            card.addView(more);
        }

        container.addView(card);
        return true;
    }

    /** Lightweight over-summary row for the Scorecard list (newest first). */
    private static final class OverSummaryRow {
        final String overLabel;
        final String teamScore;
        final String summary;

        OverSummaryRow(String overLabel, String teamScore, String summary) {
            this.overLabel = overLabel;
            this.teamScore = teamScore;
            this.summary = summary;
        }
    }

    private View buildOverSummaryView(OverSummaryRow row) {
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setPadding(0, dp(10), 0, dp(10));

        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(Gravity.CENTER_VERTICAL);

        TextView over = new TextView(this);
        over.setText(row.overLabel);
        over.setTextColor(color(R.color.cmp_text_primary));
        over.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        over.setTypeface(Typeface.DEFAULT_BOLD);
        over.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        head.addView(over);

        if (!TextUtils.isEmpty(row.teamScore)) {
            TextView score = new TextView(this);
            score.setText(row.teamScore);
            score.setTextColor(color(R.color.cmp_text_primary));
            score.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            score.setTypeface(Typeface.DEFAULT_BOLD);
            score.setGravity(Gravity.END);
            head.addView(score);
        }
        col.addView(head);

        if (!TextUtils.isEmpty(row.summary)) {
            TextView sum = new TextView(this);
            sum.setText(row.summary);
            sum.setTextColor(color(R.color.cmp_text_secondary));
            sum.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
            sum.setPadding(0, dp(4), 0, 0);
            col.addView(sum);
        }
        return col;
    }

    /**
     * Prefer commentary {@code type=over} rows (batsman + bowler figures), else
     * {@code all_overs}, else aggregate ball-by-ball commentary by over number.
     * Result is newest-first.
     */
    private List<OverSummaryRow> buildOverSummaryRows() {
        List<OverSummaryRow> fromCommentary = overSummariesFromCommentary();
        if (!fromCommentary.isEmpty()) return fromCommentary;

        List<OverSummaryRow> fromAllOvers = overSummariesFromAllOvers();
        if (!fromAllOvers.isEmpty()) return fromAllOvers;

        return overSummariesFromBalls();
    }

    private List<OverSummaryRow> overSummariesFromCommentary() {
        List<OverSummaryRow> out = new ArrayList<>();
        List<CricketMatchDetailResponse.Commentary> items =
                detail != null ? detail.getCommentary() : null;
        if (items == null) return out;
        for (CricketMatchDetailResponse.Commentary c : items) {
            if (c == null || !c.isOver()) continue;
            String label = !TextUtils.isEmpty(c.getTitle())
                    ? c.getTitle().toUpperCase(Locale.US)
                    : ("OVER " + c.getOver());
            if (!label.startsWith("OVER")) label = "OVER " + c.getOver();
            String teamScore = "";
            if (!TextUtils.isEmpty(c.getScore())) {
                teamScore = (TextUtils.isEmpty(c.getTeam()) ? "" : c.getTeam() + " ") + c.getScore();
                teamScore = teamScore.trim();
            }
            out.add(new OverSummaryRow(label, teamScore, overSummaryLine(c)));
        }
        return out;
    }

    private String overSummaryLine(CricketMatchDetailResponse.Commentary c) {
        List<String> parts = new ArrayList<>();
        String b1 = formatBatterLine(c.getBatsman1(), c.getBatsman1_score());
        String b2 = formatBatterLine(c.getBatsman2(), c.getBatsman2_score());
        if (!TextUtils.isEmpty(b1)) parts.add(b1);
        if (!TextUtils.isEmpty(b2)) parts.add(b2);
        String bowl = formatBowlerLine(c.getBowler(), c.getBowler_figures());
        if (!TextUtils.isEmpty(bowl)) parts.add(bowl);
        if (!parts.isEmpty()) return TextUtils.join(" · ", parts);
        if (!TextUtils.isEmpty(c.getDescription())) return c.getDescription();
        if (!TextUtils.isEmpty(c.getText())) return c.getText();
        return "";
    }

    private static String formatBatterLine(String name, String score) {
        if (TextUtils.isEmpty(name) || "—".equals(name)) return "";
        if (TextUtils.isEmpty(score)) return name;
        return name + " " + score;
    }

    private static String formatBowlerLine(String name, String figures) {
        if (TextUtils.isEmpty(name) || "—".equals(name)) return "";
        if (TextUtils.isEmpty(figures)) return name;
        return name + " " + figures;
    }

    private List<OverSummaryRow> overSummariesFromAllOvers() {
        List<OverSummaryRow> out = new ArrayList<>();
        List<CricketMatchDetailResponse.Over> overs =
                detail != null ? detail.getAll_overs() : null;
        if (overs == null || overs.isEmpty()) return out;
        // all_overs is oldest→newest; Scorecard wants newest first.
        for (int i = overs.size() - 1; i >= 0; i--) {
            CricketMatchDetailResponse.Over o = overs.get(i);
            if (o == null || o.getOver() <= 0) continue;
            String label = "OVER " + o.getOver();
            String teamScore = "";
            if (!TextUtils.isEmpty(o.getScore())) {
                String team = !TextUtils.isEmpty(o.getTeam_code())
                        ? o.getTeam_code()
                        : teamNameForCode(o.getTeam_code());
                teamScore = (TextUtils.isEmpty(team) ? "" : team + " ") + o.getScore();
                teamScore = teamScore.trim();
            }
            StringBuilder sum = new StringBuilder();
            if (!TextUtils.isEmpty(o.getBowler())) sum.append(o.getBowler());
            if (!TextUtils.isEmpty(o.getRuns())) {
                if (sum.length() > 0) sum.append(" · ");
                sum.append(o.getRuns()).append(" runs");
            }
            String seq = overSequence(o);
            if (!TextUtils.isEmpty(seq)) {
                if (sum.length() > 0) sum.append(" · ");
                sum.append(seq);
            }
            out.add(new OverSummaryRow(label, teamScore, sum.toString()));
        }
        return out;
    }

    private String teamNameForCode(String code) {
        if (match == null || TextUtils.isEmpty(code)) return code;
        if (code.equalsIgnoreCase(match.getTeam1_code())) {
            return fullTeam(match.getTeam1_code(), match.getTeam1_name());
        }
        if (code.equalsIgnoreCase(match.getTeam2_code())) {
            return fullTeam(match.getTeam2_code(), match.getTeam2_name());
        }
        return code;
    }

    /** Last-resort: group ball commentary by over integer, newest first. */
    private List<OverSummaryRow> overSummariesFromBalls() {
        List<OverSummaryRow> out = new ArrayList<>();
        List<CricketMatchDetailResponse.Commentary> items =
                detail != null ? detail.getCommentary() : null;
        if (items == null || items.isEmpty()) return out;

        Map<Integer, CricketMatchDetailResponse.Commentary> lastSeen = new LinkedHashMap<>();
        Map<Integer, StringBuilder> ballBits = new LinkedHashMap<>();
        for (CricketMatchDetailResponse.Commentary c : items) {
            if (c == null || c.isOver()) continue;
            int overNo = overNumberFrom(c.getOver());
            if (overNo <= 0) continue;
            if (!lastSeen.containsKey(overNo)) lastSeen.put(overNo, c);
            StringBuilder sb = ballBits.get(overNo);
            if (sb == null) {
                sb = new StringBuilder();
                ballBits.put(overNo, sb);
            }
            if (sb.length() > 0) sb.append('.');
            sb.append(c.isWicket() ? "W" : (TextUtils.isEmpty(c.getRuns()) ? "0" : c.getRuns()));
        }
        for (Map.Entry<Integer, CricketMatchDetailResponse.Commentary> e : lastSeen.entrySet()) {
            int overNo = e.getKey();
            CricketMatchDetailResponse.Commentary c = e.getValue();
            String teamScore = "";
            if (!TextUtils.isEmpty(c.getScore())) {
                teamScore = (TextUtils.isEmpty(c.getTeam()) ? "" : c.getTeam() + " ") + c.getScore();
                teamScore = teamScore.trim();
            }
            StringBuilder sum = new StringBuilder();
            String bits = ballBits.containsKey(overNo) ? ballBits.get(overNo).toString() : "";
            if (!TextUtils.isEmpty(bits)) sum.append(overNo).append(':').append(bits);
            out.add(new OverSummaryRow("OVER " + overNo, teamScore, sum.toString()));
        }
        return out;
    }

    private static int overNumberFrom(String over) {
        if (TextUtils.isEmpty(over)) return 0;
        try {
            String head = over.contains(".") ? over.substring(0, over.indexOf('.')) : over;
            return Integer.parseInt(head.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return 0;
        }
    }

    // ---- Stats (consolidated into Scorecard) --------------------------------

    /**
     * Live run-rate / partnership / last-wicket only. Do not render CREX win-%
     * bars here (or on Live) — the API may still send them; we ignore them.
     */
    private boolean appendStats(LinearLayout container) {
        if (detail == null) return false;
        CricketMatchDetailResponse.Live live = detail.getLive();
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

        return any;
    }

    // ---- View builders ------------------------------------------------------

    private LinearLayout card() {
        return card(LinearLayout.VERTICAL);
    }

    private LinearLayout card(int orientation) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(orientation);
        card.setBackgroundResource(R.drawable.bg_cmp_card);
        card.setPadding(dp(14), dp(14), dp(14), dp(14));
        card.setElevation(dp(1.5f));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(14);
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
