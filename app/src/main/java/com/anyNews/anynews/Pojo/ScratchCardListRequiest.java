package com.anyNews.anynews.Pojo;

import android.content.Intent;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ScratchCardListRequiest {

    @SerializedName("draft_id")
    @Expose
    private Integer draftId;
    @SerializedName("active_time")
    @Expose
    private Long active_time;

    @SerializedName("user_id")
    @Expose
    private Long userId;
    @SerializedName("skip")
    @Expose
    private Long skip;
    @SerializedName("first_card_id")
    @Expose
    private Long firstCardId;


    @SerializedName("card_id")
    @Expose
    private Long cardId;

    public Long getActive_time() {
        return active_time;
    }

    public void setActive_time(Long active_time) {
        this.active_time = active_time;
    }

    public Long getCardId() {
        return cardId;
    }

    public void setCardId(Long cardId) {
        this.cardId = cardId;
    }
    public Integer getDraftId() {
        return draftId;
    }

    public void setDraftId(Integer draftId) {
        this.draftId = draftId;
    }

    public Long getUserId() {

        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getSkip() {
        return skip;
    }

    public void setSkip(Long skip) {
        this.skip = skip;
    }

    public Long getFirstCardId() {
        return firstCardId;
    }

    public void setFirstCardId(Long firstCardId) {
        this.firstCardId = firstCardId;
    }
}
