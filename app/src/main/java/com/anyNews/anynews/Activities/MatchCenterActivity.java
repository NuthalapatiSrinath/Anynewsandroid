package com.anyNews.anynews.Activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.anyNews.anynews.Pojo.ActiveMatch;
import com.anyNews.anynews.Pojo.CricketActiveMatchResponse;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Cricket Match Center — full scoreboard + tabs (Live / Scorecard / Commentary /
 * Stats). Opened when the user taps the floating mini player.
 *
 * The active_match data contract carries live scoreboard fields, so the Live tab
 * is fully populated. Scorecard / Commentary / Stats require per-ball detail that
 * is not part of the app-facing contract, so they render a clean empty state
 * rather than fabricated data.
 */
public class MatchCenterActivity extends AppCompatActivity {

    public static final String EXTRA_MATCH = "extra_active_match";

    private ActiveMatch match;

    private TextView tabLive, tabScorecard, tabCommentary, tabStats;
    private View contentLive, contentPlaceholder;
    private TextView placeholderTitle, placeholderMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_center);

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
        contentPlaceholder = findViewById(R.id.content_placeholder);
        placeholderTitle = findViewById(R.id.placeholder_title);
        placeholderMessage = findViewById(R.id.placeholder_message);

        tabLive.setOnClickListener(v -> selectTab(0));
        tabScorecard.setOnClickListener(v -> selectTab(1));
        tabCommentary.setOnClickListener(v -> selectTab(2));
        tabStats.setOnClickListener(v -> selectTab(3));

        bind();
        selectTab(0);

        // Refresh from the dedicated endpoint so the scoreboard is current.
        refresh();
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

        // Live tab content
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
        int active = ContextCompat.getColor(this, R.color.cmp_brand_orange);
        int inactive = ContextCompat.getColor(this, R.color.cmp_text_secondary);
        tabLive.setTextColor(index == 0 ? active : inactive);
        tabScorecard.setTextColor(index == 1 ? active : inactive);
        tabCommentary.setTextColor(index == 2 ? active : inactive);
        tabStats.setTextColor(index == 3 ? active : inactive);

        if (index == 0) {
            contentLive.setVisibility(View.VISIBLE);
            contentPlaceholder.setVisibility(View.GONE);
            return;
        }

        contentLive.setVisibility(View.GONE);
        contentPlaceholder.setVisibility(View.VISIBLE);
        switch (index) {
            case 1:
                placeholderTitle.setText("Scorecard");
                placeholderMessage.setText("The full batting & bowling scorecard for this match will appear here when available.");
                break;
            case 2:
                placeholderTitle.setText("Commentary");
                placeholderMessage.setText("Ball-by-ball commentary for this match will appear here when available.");
                break;
            case 3:
                placeholderTitle.setText("Stats");
                placeholderMessage.setText("Match stats & head-to-head will appear here when available.");
                break;
            default:
                break;
        }
    }

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
