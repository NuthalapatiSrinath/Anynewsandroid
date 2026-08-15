package com.anyNews.anynews.Pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/** Response of the dedicated /app/v3/cricket_active_match endpoint. */
public class CricketActiveMatchResponse implements Serializable {

    @SerializedName("success")
    @Expose
    private Boolean success;

    @SerializedName("active_match")
    @Expose
    private ActiveMatch active_match;

    /** Global mini-player placement; ignored by older APKs. */
    @SerializedName("mini_player_position")
    @Expose
    private CricketMiniPlayerPosition mini_player_position;

    /**
     * Admin toggle for the floating mini player. Null (missing / old server) →
     * treated as disabled (no mini / safe default).
     */
    @SerializedName("cricket_mini_player_enabled")
    @Expose
    private Boolean cricket_mini_player_enabled;

    /**
     * Admin toggle for the bottom-footer cricket tab. Null (missing) → treated
     * as disabled so the footer is unchanged.
     */
    @SerializedName("cricket_footer_tab_enabled")
    @Expose
    private Boolean cricket_footer_tab_enabled;

    public Boolean getSuccess() {
        return success;
    }

    public ActiveMatch getActive_match() {
        return active_match;
    }

    public CricketMiniPlayerPosition getMini_player_position() {
        return mini_player_position;
    }

    /** Missing → false (mini player off by default; zero cricket polls until ON). */
    public boolean isMiniPlayerEnabled() {
        return cricket_mini_player_enabled != null && cricket_mini_player_enabled;
    }

    /** Missing → false (footer tab off by default; footer stays unchanged). */
    public boolean isFooterTabEnabled() {
        return cricket_footer_tab_enabled != null && cricket_footer_tab_enabled;
    }

    /** Raw flag presence lets callers skip caching when the server omitted it. */
    public Boolean getCricket_mini_player_enabled() {
        return cricket_mini_player_enabled;
    }

    public Boolean getCricket_footer_tab_enabled() {
        return cricket_footer_tab_enabled;
    }
}
