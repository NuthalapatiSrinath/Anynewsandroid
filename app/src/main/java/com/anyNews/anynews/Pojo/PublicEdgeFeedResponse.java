package com.anyNews.anynews.Pojo;

import com.google.gson.JsonArray;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Plain JSON from {@code GET /app/v3/public/feed} (Cloudflare edge).
 * Engagement counts are snapshots at cache time — not live.
 */
public class PublicEdgeFeedResponse {

    @SerializedName("success")
    @Expose
    private Boolean success;

    @SerializedName("data")
    @Expose
    private JsonArray data;

    @SerializedName("skip")
    @Expose
    private Integer skip;

    @SerializedName("actual_skip")
    @Expose
    private Integer actual_skip;

    @SerializedName("last_post_id")
    @Expose
    private Long last_post_id;

    @SerializedName("language_id")
    @Expose
    private Integer language_id;

    @SerializedName("state_id")
    @Expose
    private Integer state_id;

    @SerializedName("dist_id")
    @Expose
    private Integer dist_id;

    @SerializedName("edge")
    @Expose
    private Boolean edge;

    @SerializedName("eng")
    @Expose
    private String eng;

    @SerializedName("pinned_post_id")
    @Expose
    private Long pinned_post_id;

    @SerializedName("msg")
    @Expose
    private String msg;

    public Boolean getSuccess() {
        return success;
    }

    public JsonArray getData() {
        return data;
    }

    public Integer getSkip() {
        return skip;
    }

    public Integer getActual_skip() {
        return actual_skip;
    }

    public Long getLast_post_id() {
        return last_post_id;
    }

    public Integer getLanguage_id() {
        return language_id;
    }

    public Integer getState_id() {
        return state_id;
    }

    public Integer getDist_id() {
        return dist_id;
    }

    public Boolean getEdge() {
        return edge;
    }

    public String getEng() {
        return eng;
    }

    public Long getPinned_post_id() {
        return pinned_post_id;
    }

    public String getMsg() {
        return msg;
    }
}
