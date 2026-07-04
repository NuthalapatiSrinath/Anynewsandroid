package com.anyNews.anynews.Pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RedeemPageRequest {

    @SerializedName("draft_id")
    @Expose
    private Integer draftId;
    @SerializedName("user_id")
    @Expose
    private Long userId;
    @SerializedName("payment_type")
    @Expose
    private String paymentType;
    @SerializedName("transfer_to")
    @Expose
    private String transferTo;
    @SerializedName("pan_number")
    @Expose
    private String panNumber;
    @SerializedName("withdraw_amount")
    @Expose
    private Long withdrawAmount;

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

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public String getTransferTo() {
        return transferTo;
    }

    public void setTransferTo(String transferTo) {
        this.transferTo = transferTo;
    }

    public String getPanNumber() {
        return panNumber;
    }

    public void setPanNumber(String panNumber) {
        this.panNumber = panNumber;
    }

    public Long getWithdrawAmount() {
        return withdrawAmount;
    }

    public void setWithdrawAmount(Long withdrawAmount) {
        this.withdrawAmount = withdrawAmount;
    }
}
