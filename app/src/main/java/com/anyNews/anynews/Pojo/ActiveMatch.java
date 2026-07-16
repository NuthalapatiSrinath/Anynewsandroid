package com.anyNews.anynews.Pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Cricket Live Mini Player — the single active match reported by the backend.
 *
 * Field names here MUST match the backend data contract (helpers/crex/activeMatch.js)
 * and are the same shape whether the object arrives via the feed config response
 * ("active_match") or the dedicated /app/v3/cricket_active_match endpoint.
 */
public class ActiveMatch implements Serializable {

    @SerializedName("match_id")
    @Expose
    private String match_id;

    @SerializedName("crex_key")
    @Expose
    private String crex_key;

    @SerializedName("series_name")
    @Expose
    private String series_name;

    @SerializedName("match_title")
    @Expose
    private String match_title;

    @SerializedName("team1_code")
    @Expose
    private String team1_code;

    @SerializedName("team2_code")
    @Expose
    private String team2_code;

    @SerializedName("team1_name")
    @Expose
    private String team1_name;

    @SerializedName("team2_name")
    @Expose
    private String team2_name;

    @SerializedName("team1_flag")
    @Expose
    private String team1_flag;

    @SerializedName("team2_flag")
    @Expose
    private String team2_flag;

    @SerializedName("team1_score")
    @Expose
    private String team1_score;

    @SerializedName("team2_score")
    @Expose
    private String team2_score;

    @SerializedName("team1_overs")
    @Expose
    private String team1_overs;

    @SerializedName("team2_overs")
    @Expose
    private String team2_overs;

    @SerializedName("status")
    @Expose
    private String status;

    @SerializedName("status_text")
    @Expose
    private String status_text;

    @SerializedName("venue")
    @Expose
    private String venue;

    @SerializedName("format")
    @Expose
    private String format;

    @SerializedName("result")
    @Expose
    private String result;

    @SerializedName("last_ball")
    @Expose
    private String last_ball;

    @SerializedName("updated_at")
    @Expose
    private String updated_at;

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    public String getMatch_id() {
        return safe(match_id);
    }

    public String getCrex_key() {
        return safe(crex_key);
    }

    public String getSeries_name() {
        return safe(series_name);
    }

    public String getMatch_title() {
        return safe(match_title);
    }

    public String getTeam1_code() {
        return safe(team1_code);
    }

    public String getTeam2_code() {
        return safe(team2_code);
    }

    public String getTeam1_name() {
        return safe(team1_name);
    }

    public String getTeam2_name() {
        return safe(team2_name);
    }

    public String getTeam1_flag() {
        return safe(team1_flag);
    }

    public String getTeam2_flag() {
        return safe(team2_flag);
    }

    public String getTeam1_score() {
        return safe(team1_score);
    }

    public String getTeam2_score() {
        return safe(team2_score);
    }

    public String getTeam1_overs() {
        return safe(team1_overs);
    }

    public String getTeam2_overs() {
        return safe(team2_overs);
    }

    public String getStatus() {
        return safe(status);
    }

    public String getStatus_text() {
        return safe(status_text);
    }

    public String getVenue() {
        return safe(venue);
    }

    public String getFormat() {
        return safe(format);
    }

    public String getResult() {
        return safe(result);
    }

    public String getLast_ball() {
        return safe(last_ball);
    }

    public String getUpdated_at() {
        return safe(updated_at);
    }

    public boolean isLive() {
        return "live".equalsIgnoreCase(getStatus());
    }

    /** Compact score line for the mini player, e.g. "IND 245/6 (42.3)". */
    public String scoreLine(String teamCode, String score, String overs) {
        StringBuilder sb = new StringBuilder();
        if (!teamCode.isEmpty()) sb.append(teamCode).append(' ');
        if (!score.isEmpty()) sb.append(score);
        if (!overs.isEmpty()) sb.append(" (").append(overs).append(')');
        return sb.toString().trim();
    }
}
