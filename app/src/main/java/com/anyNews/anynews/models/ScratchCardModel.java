package com.anyNews.anynews.models;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ScratchCardModel {

    public class Datum {

        @SerializedName("_id")
        @Expose
        private String id;
        @SerializedName("points")
        @Expose
        private Long points;
        @SerializedName("scratch_status")
        @Expose
        private Boolean scratchStatus;
        @SerializedName("createdAt")
        @Expose
        private Long createdAt;
        @SerializedName("card_id")
        @Expose
        private Long cardId;
        @SerializedName("user_name")
        @Expose
        private String userName;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Long getPoints() {
            return points;
        }

        public void setPoints(Long points) {
            this.points = points;
        }

        public Boolean getScratchStatus() {
            return scratchStatus;
        }

        public void setScratchStatus(Boolean scratchStatus) {
            this.scratchStatus = scratchStatus;
        }

        public Long getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(Long createdAt) {
            this.createdAt = createdAt;
        }

        public Long getCardId() {
            return cardId;
        }

        public void setCardId(Long cardId) {
            this.cardId = cardId;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }


        @SerializedName("status")
        @Expose
        private Boolean status;

        @SerializedName("active_time")
        @Expose
        private Long activeTime;


        public Boolean getStatus() {
            return status;
        }

        public void setStatus(Boolean status) {
            this.status = status;
        }

        public Long getActiveTime() {
            return activeTime;
        }

        public void setActiveTime(Long activeTime) {
            this.activeTime = activeTime;
        }


    }

    @SerializedName("success")
    @Expose
    private Boolean success;
    @SerializedName("data")
    @Expose
    private List<Datum> data = null;
    @SerializedName("skip")
    @Expose
    private Long skip;
    @SerializedName("first_card_id")
    @Expose
    private Long firstCardId;




    @SerializedName("total_points")
    @Expose
    private Long total_points;

    public Long getTotal_points() {
        return total_points;
    }

    public void setTotal_points(Long total_points) {
        this.total_points = total_points;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public List<Datum> getData() {
        return data;
    }

    public void setData(List<Datum> data) {
        this.data = data;
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
