package com.anyNews.anynews.Pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Response of the app-facing /app/v3/cricket_match_detail endpoint. Mirrors the
 * admin crex_get_match_detail payload (helpers/crex/crexMatchDetail.js) but only
 * models the subset the Match Center tabs render: full scorecard (batting +
 * bowling), ball-by-ball commentary, and live stats.
 */
public class CricketMatchDetailResponse implements Serializable {

    @SerializedName("success")
    @Expose
    private Boolean success;

    @SerializedName("data")
    @Expose
    private Detail data;

    public Boolean getSuccess() {
        return success;
    }

    public Detail getData() {
        return data;
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    public static class Detail implements Serializable {
        @SerializedName("crex_key")
        @Expose
        private String crex_key;
        @SerializedName("info")
        @Expose
        private Info info;
        @SerializedName("live")
        @Expose
        private Live live;
        @SerializedName("scorecards")
        @Expose
        private List<Innings> scorecards;
        @SerializedName("commentary")
        @Expose
        private List<Commentary> commentary;
        @SerializedName("all_overs")
        @Expose
        private List<Over> all_overs;
        @SerializedName("is_completed")
        @Expose
        private Boolean is_completed;
        @SerializedName("from_snapshot")
        @Expose
        private Boolean from_snapshot;
        @SerializedName("player_of_match")
        @Expose
        private PlayerOf player_of_match;
        @SerializedName("player_of_series")
        @Expose
        private PlayerOf player_of_series;
        @SerializedName("summary")
        @Expose
        private Summary summary;

        public Summary getSummary() {
            return summary;
        }

        public String getCrex_key() {
            return safe(crex_key);
        }

        public Info getInfo() {
            return info;
        }

        public Live getLive() {
            return live;
        }

        public List<Innings> getScorecards() {
            return scorecards;
        }

        public List<Commentary> getCommentary() {
            return commentary;
        }

        public List<Over> getAll_overs() {
            return all_overs;
        }

        public boolean isCompleted() {
            return is_completed != null && is_completed;
        }

        public boolean isFromSnapshot() {
            return from_snapshot != null && from_snapshot;
        }

        public PlayerOf getPlayer_of_match() {
            return player_of_match;
        }

        public PlayerOf getPlayer_of_series() {
            return player_of_series;
        }
    }

    public static class PlayerOf implements Serializable {
        @SerializedName("name")
        @Expose
        private String name;
        @SerializedName("label")
        @Expose
        private String label;
        @SerializedName("figures")
        @Expose
        private String figures;
        @SerializedName("image")
        @Expose
        private String image;

        public String getName() {
            return safe(name);
        }

        public String getLabel() {
            return safe(label);
        }

        public String getFigures() {
            return safe(figures);
        }

        public String getImage() {
            return safe(image);
        }
    }

    // ---- Summary tab ---------------------------------------------------------

    public static class Summary implements Serializable {
        @SerializedName("is_completed")
        @Expose
        private Boolean is_completed;
        @SerializedName("result_text")
        @Expose
        private String result_text;
        @SerializedName("winner_team")
        @Expose
        private String winner_team;
        @SerializedName("teams")
        @Expose
        private List<SummaryTeam> teams;
        @SerializedName("last_over")
        @Expose
        private Over last_over;
        @SerializedName("player_of_match")
        @Expose
        private PlayerOf player_of_match;
        @SerializedName("top_performers")
        @Expose
        private List<TopPerformer> top_performers;

        public boolean isCompleted() {
            return is_completed != null && is_completed;
        }

        public String getResult_text() {
            return safe(result_text);
        }

        public String getWinner_team() {
            return safe(winner_team);
        }

        public List<SummaryTeam> getTeams() {
            return teams;
        }

        public Over getLast_over() {
            return last_over;
        }

        public PlayerOf getPlayer_of_match() {
            return player_of_match;
        }

        public List<TopPerformer> getTop_performers() {
            return top_performers;
        }
    }

    public static class SummaryTeam implements Serializable {
        @SerializedName("team")
        @Expose
        private String team;
        @SerializedName("short")
        @Expose
        private String short_name;
        @SerializedName("flag")
        @Expose
        private String flag;
        @SerializedName("is_winner")
        @Expose
        private Boolean is_winner;
        @SerializedName("innings")
        @Expose
        private List<TeamInnings> innings;

        public String getTeam() {
            return safe(team);
        }

        public String getShort_name() {
            return safe(short_name);
        }

        public String getFlag() {
            return safe(flag);
        }

        public boolean isWinner() {
            return is_winner != null && is_winner;
        }

        public List<TeamInnings> getInnings() {
            return innings;
        }
    }

    public static class TeamInnings implements Serializable {
        @SerializedName("score")
        @Expose
        private String score;
        @SerializedName("overs")
        @Expose
        private String overs;

        public String getScore() {
            return safe(score);
        }

        public String getOvers() {
            return safe(overs);
        }
    }

    public static class TopPerformer implements Serializable {
        @SerializedName("label")
        @Expose
        private String label;
        @SerializedName("team_name")
        @Expose
        private String team_name;
        @SerializedName("team_code")
        @Expose
        private String team_code;
        @SerializedName("top_batters")
        @Expose
        private List<TopPlayer> top_batters;
        @SerializedName("top_bowler")
        @Expose
        private TopPlayer top_bowler;

        public String getLabel() {
            return safe(label);
        }

        public String getTeam_name() {
            return safe(team_name);
        }

        public List<TopPlayer> getTop_batters() {
            return top_batters;
        }

        public TopPlayer getTop_bowler() {
            return top_bowler;
        }
    }

    public static class TopPlayer implements Serializable {
        @SerializedName("name")
        @Expose
        private String name;
        @SerializedName("image")
        @Expose
        private String image;
        @SerializedName("figures")
        @Expose
        private String figures;
        @SerializedName("strike_rate")
        @Expose
        private String strike_rate;
        @SerializedName("economy")
        @Expose
        private String economy;

        public String getName() {
            return safe(name);
        }

        public String getImage() {
            return safe(image);
        }

        public String getFigures() {
            return safe(figures);
        }

        public String getStrike_rate() {
            return safe(strike_rate);
        }

        public String getEconomy() {
            return safe(economy);
        }
    }

    public static class Over implements Serializable {
        @SerializedName("inning")
        @Expose
        private Integer inning;
        @SerializedName("over")
        @Expose
        private Integer over;
        @SerializedName("over_label")
        @Expose
        private String over_label;
        @SerializedName("bowler")
        @Expose
        private String bowler;
        @SerializedName("team_code")
        @Expose
        private String team_code;
        @SerializedName("runs")
        @Expose
        private Integer runs;
        @SerializedName("score")
        @Expose
        private String score;
        @SerializedName("balls")
        @Expose
        private List<Ball> balls;

        public int getInning() {
            return inning == null ? 0 : inning;
        }

        public int getOver() {
            return over == null ? 0 : over;
        }

        public String getOver_label() {
            return TextUtils_isEmpty(over_label) ? ("Over " + getOver()) : over_label;
        }

        public String getBowler() {
            return safe(bowler);
        }

        public String getTeam_code() {
            return safe(team_code);
        }

        public String getRuns() {
            return runs == null ? "" : String.valueOf(runs);
        }

        public String getScore() {
            return safe(score);
        }

        public List<Ball> getBalls() {
            return balls;
        }

        private static boolean TextUtils_isEmpty(String s) {
            return s == null || s.length() == 0;
        }
    }

    public static class Ball implements Serializable {
        @SerializedName("value")
        @Expose
        private String value;
        @SerializedName("is_wicket")
        @Expose
        private Boolean is_wicket;
        @SerializedName("is_four")
        @Expose
        private Boolean is_four;
        @SerializedName("is_six")
        @Expose
        private Boolean is_six;
        @SerializedName("wicket_batsman")
        @Expose
        private String wicket_batsman;

        public String getValue() {
            return safe(value);
        }

        public boolean isWicket() {
            return is_wicket != null && is_wicket;
        }

        public boolean isFour() {
            return is_four != null && is_four;
        }

        public boolean isSix() {
            return is_six != null && is_six;
        }

        public String getWicket_batsman() {
            return safe(wicket_batsman);
        }
    }

    public static class Info implements Serializable {
        @SerializedName("series")
        @Expose
        private String series;
        @SerializedName("venue")
        @Expose
        private String venue;
        @SerializedName("toss")
        @Expose
        private String toss;
        @SerializedName("format")
        @Expose
        private String format;
        @SerializedName("match_number")
        @Expose
        private String match_number;
        @SerializedName("start_time")
        @Expose
        private String start_time;

        public String getSeries() {
            return safe(series);
        }

        public String getVenue() {
            return safe(venue);
        }

        public String getToss() {
            return safe(toss);
        }

        public String getFormat() {
            return safe(format);
        }

        public String getMatch_number() {
            return safe(match_number);
        }

        public String getStart_time() {
            return safe(start_time);
        }
    }

    public static class Live implements Serializable {
        @SerializedName("status_text")
        @Expose
        private String status_text;
        @SerializedName("current_run_rate")
        @Expose
        private String current_run_rate;
        @SerializedName("required_rate")
        @Expose
        private String required_rate;
        @SerializedName("session_label")
        @Expose
        private String session_label;
        @SerializedName("batsmen")
        @Expose
        private List<Batsman> batsmen;
        @SerializedName("bowler")
        @Expose
        private Bowler bowler;
        @SerializedName("partnership")
        @Expose
        private Partnership partnership;
        @SerializedName("last_wicket")
        @Expose
        private LastWicket last_wicket;
        @SerializedName("win_probability")
        @Expose
        private WinProbability win_probability;

        public String getStatus_text() {
            return safe(status_text);
        }

        public String getCurrent_run_rate() {
            return safe(current_run_rate);
        }

        public String getRequired_rate() {
            return safe(required_rate);
        }

        public String getSession_label() {
            return safe(session_label);
        }

        public List<Batsman> getBatsmen() {
            return batsmen;
        }

        public Bowler getBowler() {
            return bowler;
        }

        public Partnership getPartnership() {
            return partnership;
        }

        public LastWicket getLast_wicket() {
            return last_wicket;
        }

        public WinProbability getWin_probability() {
            return win_probability;
        }
    }

    public static class Batsman implements Serializable {
        @SerializedName("name")
        @Expose
        private String name;
        @SerializedName("runs")
        @Expose
        private String runs;
        @SerializedName("balls")
        @Expose
        private String balls;
        @SerializedName("strike_rate")
        @Expose
        private String strike_rate;
        @SerializedName("on_strike")
        @Expose
        private Boolean on_strike;

        public String getName() {
            return safe(name);
        }

        public String getRuns() {
            return safe(runs);
        }

        public String getBalls() {
            return safe(balls);
        }

        public String getStrike_rate() {
            return safe(strike_rate);
        }

        public boolean isOn_strike() {
            return on_strike != null && on_strike;
        }
    }

    public static class Bowler implements Serializable {
        @SerializedName("name")
        @Expose
        private String name;
        @SerializedName("figures")
        @Expose
        private String figures;
        @SerializedName("overs")
        @Expose
        private String overs;
        @SerializedName("economy")
        @Expose
        private String economy;

        public String getName() {
            return safe(name);
        }

        public String getFigures() {
            return safe(figures);
        }

        public String getOvers() {
            return safe(overs);
        }

        public String getEconomy() {
            return safe(economy);
        }
    }

    public static class Partnership implements Serializable {
        @SerializedName("text")
        @Expose
        private String text;

        public String getText() {
            return safe(text);
        }
    }

    public static class LastWicket implements Serializable {
        @SerializedName("text")
        @Expose
        private String text;

        public String getText() {
            return safe(text);
        }
    }

    public static class WinProbability implements Serializable {
        @SerializedName("team1")
        @Expose
        private WinProbSide team1;
        @SerializedName("team2")
        @Expose
        private WinProbSide team2;
        @SerializedName("draw")
        @Expose
        private WinProbSide draw;

        public WinProbSide getTeam1() {
            return team1;
        }

        public WinProbSide getTeam2() {
            return team2;
        }

        public WinProbSide getDraw() {
            return draw;
        }
    }

    public static class WinProbSide implements Serializable {
        @SerializedName("label")
        @Expose
        private String label;
        @SerializedName("pct")
        @Expose
        private Double pct;

        public String getLabel() {
            return safe(label);
        }

        public int getPct() {
            return pct == null ? 0 : (int) Math.round(pct);
        }
    }

    public static class Innings implements Serializable {
        @SerializedName("innings")
        @Expose
        private Integer innings;
        @SerializedName("team_name")
        @Expose
        private String team_name;
        @SerializedName("score")
        @Expose
        private String score;
        @SerializedName("label")
        @Expose
        private String label;
        @SerializedName("extras")
        @Expose
        private String extras;
        @SerializedName("is_current")
        @Expose
        private Boolean is_current;
        @SerializedName("batting")
        @Expose
        private List<Batting> batting;
        @SerializedName("bowling")
        @Expose
        private List<Bowling> bowling;
        @SerializedName("fall_of_wickets")
        @Expose
        private List<FallOfWicket> fall_of_wickets;

        public int innings() {
            return innings == null ? 0 : innings;
        }

        public String getTeam_name() {
            return safe(team_name);
        }

        public String getScore() {
            return safe(score);
        }

        public String getLabel() {
            return safe(label);
        }

        public String getExtras() {
            return safe(extras);
        }

        public boolean isCurrent() {
            return is_current != null && is_current;
        }

        public List<Batting> getBatting() {
            return batting;
        }

        public List<Bowling> getBowling() {
            return bowling;
        }

        public List<FallOfWicket> getFall_of_wickets() {
            return fall_of_wickets;
        }
    }

    public static class Batting implements Serializable {
        @SerializedName("name")
        @Expose
        private String name;
        @SerializedName("runs")
        @Expose
        private Integer runs;
        @SerializedName("balls")
        @Expose
        private Integer balls;
        @SerializedName("fours")
        @Expose
        private Integer fours;
        @SerializedName("sixes")
        @Expose
        private Integer sixes;
        @SerializedName("strike_rate")
        @Expose
        private String strike_rate;
        @SerializedName("dismissal")
        @Expose
        private String dismissal;
        @SerializedName("status")
        @Expose
        private String status;
        @SerializedName("did_not_bat")
        @Expose
        private Boolean did_not_bat;

        public String getName() {
            return safe(name);
        }

        public int getRuns() {
            return runs == null ? 0 : runs;
        }

        public int getBalls() {
            return balls == null ? 0 : balls;
        }

        public int getFours() {
            return fours == null ? 0 : fours;
        }

        public int getSixes() {
            return sixes == null ? 0 : sixes;
        }

        public String getStrike_rate() {
            return safe(strike_rate);
        }

        public String getDismissal() {
            return safe(dismissal);
        }

        public String getStatus() {
            return safe(status);
        }

        public boolean isDid_not_bat() {
            return did_not_bat != null && did_not_bat;
        }
    }

    public static class Bowling implements Serializable {
        @SerializedName("name")
        @Expose
        private String name;
        @SerializedName("overs")
        @Expose
        private String overs;
        @SerializedName("runs")
        @Expose
        private Integer runs;
        @SerializedName("wickets")
        @Expose
        private Integer wickets;
        @SerializedName("economy")
        @Expose
        private String economy;

        public String getName() {
            return safe(name);
        }

        public String getOvers() {
            return safe(overs);
        }

        public int getRuns() {
            return runs == null ? 0 : runs;
        }

        public int getWickets() {
            return wickets == null ? 0 : wickets;
        }

        public String getEconomy() {
            return safe(economy);
        }
    }

    public static class FallOfWicket implements Serializable {
        @SerializedName("batsman")
        @Expose
        private String batsman;
        @SerializedName("score")
        @Expose
        private String score;

        public String getBatsman() {
            return safe(batsman);
        }

        public String getScore() {
            return safe(score);
        }
    }

    public static class Commentary implements Serializable {
        @SerializedName("type")
        @Expose
        private String type;
        @SerializedName("over")
        @Expose
        private String over;
        @SerializedName("score")
        @Expose
        private String score;
        @SerializedName("title")
        @Expose
        private String title;
        @SerializedName("description")
        @Expose
        private String description;
        @SerializedName("text")
        @Expose
        private String text;
        @SerializedName("runs")
        @Expose
        private String runs;
        @SerializedName("team")
        @Expose
        private String team;
        @SerializedName("bowler")
        @Expose
        private String bowler;
        @SerializedName("bowler_figures")
        @Expose
        private String bowler_figures;
        @SerializedName("batsman1")
        @Expose
        private String batsman1;
        @SerializedName("batsman2")
        @Expose
        private String batsman2;
        @SerializedName("batsman1_score")
        @Expose
        private String batsman1_score;
        @SerializedName("batsman2_score")
        @Expose
        private String batsman2_score;
        @SerializedName("is_wicket")
        @Expose
        private Boolean is_wicket;
        @SerializedName("is_four")
        @Expose
        private Boolean is_four;
        @SerializedName("is_six")
        @Expose
        private Boolean is_six;

        public String getType() {
            return safe(type);
        }

        public String getOver() {
            return safe(over);
        }

        public String getScore() {
            return safe(score);
        }

        public String getTitle() {
            return safe(title);
        }

        public String getDescription() {
            return safe(description);
        }

        public String getText() {
            return safe(text);
        }

        public String getRuns() {
            return safe(runs);
        }

        public String getTeam() {
            return safe(team);
        }

        public String getBowler() {
            return safe(bowler);
        }

        public String getBowler_figures() {
            return safe(bowler_figures);
        }

        public String getBatsman1() {
            return safe(batsman1);
        }

        public String getBatsman2() {
            return safe(batsman2);
        }

        public String getBatsman1_score() {
            return safe(batsman1_score);
        }

        public String getBatsman2_score() {
            return safe(batsman2_score);
        }

        public boolean isWicket() {
            return is_wicket != null && is_wicket;
        }

        public boolean isFour() {
            return is_four != null && is_four;
        }

        public boolean isSix() {
            return is_six != null && is_six;
        }

        public boolean isOver() {
            return "over".equalsIgnoreCase(getType());
        }
    }
}
