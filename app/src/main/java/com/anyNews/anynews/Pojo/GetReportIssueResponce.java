package com.anyNews.anynews.Pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.rollbar.api.payload.data.Data;

import java.util.List;

public class GetReportIssueResponce {

    @SerializedName("success")
    @Expose
    private Boolean success;
    @SerializedName("data")
    @Expose
    private Data data;

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public class Data {

        @SerializedName("_id")
        @Expose
        private String id;
        @SerializedName("language_id")
        @Expose
        private Integer languageId;
        @SerializedName("options")
        @Expose
        private List<String> options = null;
        @SerializedName("title")
        @Expose
        private String title;
        @SerializedName("other_reason")
        @Expose
        private String otherReason;
        @SerializedName("placeholder")
        @Expose
        private String placeholder;
        @SerializedName("status")
        @Expose
        private Boolean status;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Integer getLanguageId() {
            return languageId;
        }

        public void setLanguageId(Integer languageId) {
            this.languageId = languageId;
        }

        public List<String> getOptions() {
            return options;
        }

        public void setOptions(List<String> options) {
            this.options = options;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getOtherReason() {
            return otherReason;
        }

        public void setOtherReason(String otherReason) {
            this.otherReason = otherReason;
        }

        public String getPlaceholder() {
            return placeholder;
        }

        public void setPlaceholder(String placeholder) {
            this.placeholder = placeholder;
        }

        public Boolean getStatus() {
            return status;
        }

        public void setStatus(Boolean status) {
            this.status = status;
        }

    }
}
