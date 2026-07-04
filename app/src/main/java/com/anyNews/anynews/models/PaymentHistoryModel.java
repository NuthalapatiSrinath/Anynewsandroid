package com.anyNews.anynews.models;


import com.anyNews.anynews.Pojo.Datum;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PaymentHistoryModel {


    public class Datum {

        @SerializedName("amount")
        @Expose
        private Long amount;
        @SerializedName("points")
        @Expose
        private Long points;

        @SerializedName("transaction_type")
        @Expose
        private Integer transactionType;
        @SerializedName("reason")
        @Expose
        private String reason;
        @SerializedName("date")
        @Expose
        private String date;
        @SerializedName("month")
        @Expose
        private String month;

        public Long getAmount() {
            return amount;
        }

        public void setAmount(Long amount) {
            this.amount = amount;
        }

        public Integer getTransactionType() {
            return transactionType;
        }

        public void setTransactionType(Integer transactionType) {
            this.transactionType = transactionType;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getMonth() {
            return month;
        }

        public void setMonth(String month) {
            this.month = month;
        }


        @SerializedName("title")
        @Expose
        private String title;

        @SerializedName("image")
        @Expose
        private String image;
        @SerializedName("post_status")
        @Expose
        private Long postStatus;
        @SerializedName("createdAt")
        @Expose
        private String createdAt;

        public Long getPoints() {
            return points;
        }

        public void setPoints(Long points) {
            this.points = points;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }


        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public Long getPostStatus() {
            return postStatus;
        }

        public void setPostStatus(Long postStatus) {
            this.postStatus = postStatus;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }


        @SerializedName("description")
        @Expose
        private String description;
        @SerializedName("transaction_status")
        @Expose
        private Integer transactionStatus;



        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Integer getTransactionStatus() {
            return transactionStatus;
        }

        public void setTransactionStatus(Integer transactionStatus) {
            this.transactionStatus = transactionStatus;
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
    @SerializedName("first_transaction_id")
    @Expose
    private Long firstTransactionId;


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

    public Long getFirstTransactionId() {
        return firstTransactionId;
    }

    public void setFirstTransactionId(Long firstTransactionId) {
        this.firstTransactionId = firstTransactionId;
    }




}