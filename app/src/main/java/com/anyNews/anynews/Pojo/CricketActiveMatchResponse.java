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

    public Boolean getSuccess() {
        return success;
    }

    public ActiveMatch getActive_match() {
        return active_match;
    }

    public CricketMiniPlayerPosition getMini_player_position() {
        return mini_player_position;
    }
}
