package com.anyNews.anynews.Pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PostNewsRequest {

    @SerializedName("draft_id")
    @Expose
    private Integer draftId;
    @SerializedName("mobile_number")
    @Expose
    private String mobileNumber;
    @SerializedName("post_status")
    @Expose
    private Integer postStatus;
    @SerializedName("first_post_id")
    @Expose
    private Long firstPostId;
    @SerializedName("skip")
    @Expose
    private Integer skip;
    @SerializedName("limit")
    @Expose
    private Integer limit;
    @SerializedName("start_time")
    @Expose
    private String startTime;
    @SerializedName("end_time")
    @Expose
    private String endTime;
    @SerializedName("group_by_week")
    @Expose
    private Boolean groupByWeek;

    public Integer getDraftId() {
        return draftId;
    }

    public void setDraftId(Integer draftId) {
        this.draftId = draftId;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public Integer getPostStatus() {
        return postStatus;
    }

    public void setPostStatus(Integer postStatus) {
        this.postStatus = postStatus;
    }

    public Long getFirstPostId() {
        return firstPostId;
    }

    public void setFirstPostId(Long firstPostId) {
        this.firstPostId = firstPostId;
    }

    public Integer getSkip() {
        return skip;
    }

    public void setSkip(Integer skip) {
        this.skip = skip;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public Boolean getGroupByWeek() {
        return groupByWeek;
    }

    public void setGroupByWeek(Boolean groupByWeek) {
        this.groupByWeek = groupByWeek;
    }
}
