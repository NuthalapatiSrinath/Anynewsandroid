package com.anyNews.anynews.Pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class CategoriesResponse implements Serializable {

    public static class Datum implements Serializable
    {

        @SerializedName("CategoryID")
        @Expose
        private String categoryID;
        @SerializedName("LanguageID")
        @Expose
        private String languageID;
        @SerializedName("Category_Name")
        @Expose
        private String categoryName;
        @SerializedName("e_name")
        @Expose
        private String eName;
        @SerializedName("name")
        @Expose
        private String name;
        @SerializedName("status")
        @Expose
        private Boolean status;
        @SerializedName("first_image")
        @Expose
        private String firstImage;
        @SerializedName("second_image")
        @Expose
        private String secondImage;
        @SerializedName("id")
        @Expose
        private Integer id;
        @SerializedName("language_id")
        @Expose
        private Integer languageId;
        @SerializedName("sub_category")
        @Expose
        private List<Object> subCategory = null;
        @SerializedName("createdAt")
        @Expose
        private String createdAt;
        @SerializedName("Sub_Category_Data")
        @Expose
        private List<Object> subCategoryData = null;
        @SerializedName("__v")
        @Expose
        private Integer v;
        @SerializedName("updatedAt")
        @Expose
        private String updatedAt;
        private final static long serialVersionUID = 5729202893915767298L;

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

        public String getEName() {
            return eName;
        }

        public void setEName(String eName) {
            this.eName = eName;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Boolean getStatus() {
            return status;
        }

        public void setStatus(Boolean status) {
            this.status = status;
        }

        public String getFirstImage() {
            return firstImage;
        }

        public void setFirstImage(String firstImage) {
            this.firstImage = firstImage;
        }

        public String getSecondImage() {
            return secondImage;
        }

        public void setSecondImage(String secondImage) {
            this.secondImage = secondImage;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public Integer getLanguageId() {
            return languageId;
        }

        public void setLanguageId(Integer languageId) {
            this.languageId = languageId;
        }

        public List<Object> getSubCategory() {
            return subCategory;
        }

        public void setSubCategory(List<Object> subCategory) {
            this.subCategory = subCategory;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public List<Object> getSubCategoryData() {
            return subCategoryData;
        }

        public void setSubCategoryData(List<Object> subCategoryData) {
            this.subCategoryData = subCategoryData;
        }

        public Integer getV() {
            return v;
        }

        public void setV(Integer v) {
            this.v = v;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(String updatedAt) {
            this.updatedAt = updatedAt;
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
    private final static long serialVersionUID = -9019652905466300122L;

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
