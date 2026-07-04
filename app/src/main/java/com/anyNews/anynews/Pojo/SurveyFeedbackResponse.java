package com.anyNews.anynews.Pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class SurveyFeedbackResponse {

    @SerializedName("success")
    @Expose
    private Boolean success;

    @SerializedName("msg")
    @Expose
    private String message;

    @SerializedName("data")
    @Expose
    private List<SurveyFeedbackResponse.Datum> data = null;

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<SurveyFeedbackResponse.Datum> getData() {
        return data;
    }

    public void setData(List<SurveyFeedbackResponse.Datum> data) {
        this.data = data;
    }


    public static class Datum {

        @SerializedName("name")
        @Expose
        private String name;

        @SerializedName("percent")
        @Expose
        private int percent;

        @SerializedName("count")
        @Expose
        private int count;

        @SerializedName("survey_status")
        @Expose
        private Boolean survey_status;


        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public int getPercent() {
            return percent;
        }

        public void setPercent(int percent) {
            this.percent = percent;
        }

        public Boolean getSurvey_status() {
            return survey_status;
        }

        public void setSurvey_status(Boolean survey_status) {
            this.survey_status = survey_status;
        }
    }
}
