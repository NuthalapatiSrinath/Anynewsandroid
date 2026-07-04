package com.anyNews.anynews.Pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class CategorySearchResponse implements Serializable {

   public class Datum implements Serializable
    {

        @SerializedName("_id")
        @Expose
        private String id;
        @SerializedName("CategoryID")
        @Expose
        private String categoryID;
        @SerializedName("LanguageID")
        @Expose
        private String languageID;
        @SerializedName("Category_Name")
        @Expose
        private String categoryName;
        private final static long serialVersionUID = -5500482473607289477L;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getCategoryID() {
            return categoryID;
        }

        public void setCategoryID(String categoryID) {
            this.categoryID = categoryID;
        }

        public String getLanguageID() {
            return languageID;
        }

        public void setLanguageID(String languageID) {
            this.languageID = languageID;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public void setCategoryName(String categoryName) {
            this.categoryName = categoryName;
        }

    }

    public class Extras implements Serializable
    {
        @SerializedName("msg")
        @Expose
        private int msg;

        public void setMsg(int msg) {
            this.msg = msg;
        }

        public int getMsg() {
            return msg;
        }

        @SerializedName("Data")
        @Expose
        private List<Datum> data = null;
        private final static long serialVersionUID = -8306209157239107398L;

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
        private final static long serialVersionUID = -5843050117614797004L;

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
