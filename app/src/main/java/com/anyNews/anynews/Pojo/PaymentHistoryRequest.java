package com.anyNews.anynews.Pojo;

import com.anyNews.anynews.Activities.LoginActivityDetails;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PaymentHistoryRequest {

    @SerializedName("draft_id")
    @Expose
    private Long draftId;
    @SerializedName("user_id")
    @Expose
    private Long userId;
    @SerializedName("skip")
    @Expose
    private Long skip;
    @SerializedName("first_transaction_id")
    @Expose
    private Long firstTransactionId;


    @SerializedName("mobile_number")
    @Expose
    private String mobileNumber;
    @SerializedName("language_id")
    @Expose
    private Integer languageId;
    @SerializedName("post_status")
    @Expose
    private Long postStatus;
    @SerializedName("first_post_id")
    @Expose
    private Long firstPostId;

    public Long getFirstPostId() {
        return firstPostId;
    }

    public void setFirstPostId(Long firstPostId) {
        this.firstPostId = firstPostId;
    }
    public Long getPostStatus() {
        return postStatus;
    }

    public void setPostStatus(Long postStatus) {
        this.postStatus = postStatus;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public Integer getLanguageId() {
        return languageId;
    }

    public void setLanguageId(Integer languageId) {
        this.languageId = languageId;
    }

    public Long getDraftId() {
        return draftId;
    }

    public void setDraftId(long draftId) {
        this.draftId = draftId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public long getSkip() {
        return skip;
    }

    public void setSkip(long skip) {
        this.skip = skip;
    }

    public Long getFirstTransactionId() {
        return firstTransactionId;
    }

    public void setFirstTransactionId(Long firstTransactionId) {
        this.firstTransactionId = firstTransactionId;
    }
}
