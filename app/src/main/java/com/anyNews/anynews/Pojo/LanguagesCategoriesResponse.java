package com.anyNews.anynews.Pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class LanguagesCategoriesResponse implements Serializable {

    public class Datum implements Serializable
    {
        @SerializedName("id")
        @Expose
        private Integer categoryId;
        @SerializedName("language_id")
        @Expose
        private String languageId;
        @SerializedName("name")
        @Expose
        private String categoryName;
        @SerializedName("status")
        @Expose
        private Boolean status;
        @SerializedName("sub_category")
        @Expose
        private List<Object> subCategory = null;
        private final static long serialVersionUID = -4928284183409350738L;

        public Integer getCategoryId() {
            return categoryId;
        }

        public void setCategoryId(Integer categoryId) {
            this.categoryId = categoryId;
        }

        public String getLanguageId() {
            return languageId;
        }

        public void setLanguageId(String languageId) {
            this.languageId = languageId;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public void setCategoryName(String categoryName) {
            this.categoryName = categoryName;
        }

        public Boolean getStatus() {
            return status;
        }

        public void setStatus(Boolean status) {
            this.status = status;
        }

        public List<Object> getSubCategory() {
            return subCategory;
        }

        public void setSubCategory(List<Object> subCategory) {
            this.subCategory = subCategory;
        }
    }

    public class Extras implements Serializable
    {
        @SerializedName("Count")
        @Expose
        private Integer count;
        @SerializedName("Data")
        @Expose
        private List<Datum> data = null;
        private final static long serialVersionUID = -2557662919545807446L;

        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }

        public List<Datum> getData() {
            return data;
        }

        public void setData(List<Datum> data) {
            this.data = data;
        }
    }

    @SerializedName("success")
    @Expose
    private Boolean success;
    @SerializedName("extras")
    @Expose
    private Extras extras;
    private final static long serialVersionUID = 2154993296779652056L;

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public Extras getExtras() {
        return extras;
    }

    public void setExtras(Extras extras) {
        this.extras = extras;
    }

}
