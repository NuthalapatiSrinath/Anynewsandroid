package com.anyNews.anynews.Pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Admin-configurable cricket mini-player placement.
 * {@code use_default=true} (or missing) → hard-coded bottom-right in
 * {@link com.anyNews.anynews.Utils.CricketMiniPlayerManager}.
 * Custom: {@code x_percent}/{@code y_percent} are 0–100 within the movable area.
 */
public class CricketMiniPlayerPosition implements Serializable {

    @SerializedName("use_default")
    @Expose
    private Boolean use_default;

    @SerializedName("x_percent")
    @Expose
    private Double x_percent;

    @SerializedName("y_percent")
    @Expose
    private Double y_percent;

    public boolean isUseDefault() {
        return use_default == null || use_default;
    }

    public double getXPercent() {
        if (x_percent == null || !Double.isFinite(x_percent)) return 100d;
        return Math.max(0d, Math.min(100d, x_percent));
    }

    public double getYPercent() {
        if (y_percent == null || !Double.isFinite(y_percent)) return 80d;
        return Math.max(0d, Math.min(100d, y_percent));
    }
}
