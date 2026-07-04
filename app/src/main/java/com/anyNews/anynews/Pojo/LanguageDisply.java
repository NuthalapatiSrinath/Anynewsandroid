package com.anyNews.anynews.Pojo;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class LanguageDisply implements Serializable
{

    @SerializedName("success")
    @Expose
    private Boolean success;
    @SerializedName("extras")
    @Expose
    private Extras extras;

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


    public class Datum {

        @SerializedName("LanguageID")
        @Expose
        private String languageID;
        @SerializedName("Language_Name")
        @Expose
        private String languageName;
        @SerializedName("Language_Type")
        @Expose
        private String languageType;
        @SerializedName("SNo")
        @Expose
        private Integer sNo;
        @SerializedName("Time")
        @Expose
        private String time;
        @SerializedName("__v")
        @Expose
        private Integer v;
        @SerializedName("e_name")
        @Expose
        private String eName;
        @SerializedName("first_image")
        @Expose
        private String firstImage;
        @SerializedName("id")
        @Expose
        private Integer id;
        @SerializedName("name")
        @Expose
        private String name;
        @SerializedName("second_image")
        @Expose
        private String secondImage;
        @SerializedName("type")
        @Expose
        private String type;
        @SerializedName("updatedAt")
        @Expose
        private String updatedAt;
        @SerializedName("createdAt")
        @Expose
        private String createdAt;

        public String getLanguageID() {
            return languageID;
        }

        public void setLanguageID(String languageID) {
            this.languageID = languageID;
        }

        public String getLanguageName() {
            return languageName;
        }

        public void setLanguageName(String languageName) {
            this.languageName = languageName;
        }

        public String getLanguageType() {
            return languageType;
        }

        public void setLanguageType(String languageType) {
            this.languageType = languageType;
        }

        public Integer getSNo() {
            return sNo;
        }

        public void setSNo(Integer sNo) {
            this.sNo = sNo;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public Integer getV() {
            return v;
        }

        public void setV(Integer v) {
            this.v = v;
        }

        public String getEName() {
            return eName;
        }

        public void setEName(String eName) {
            this.eName = eName;
        }

        public String getFirstImage() {
            return firstImage;
        }

        public void setFirstImage(String firstImage) {
            this.firstImage = firstImage;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSecondImage() {
            return secondImage;
        }

        public void setSecondImage(String secondImage) {
            this.secondImage = secondImage;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(String updatedAt) {
            this.updatedAt = updatedAt;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

    }


    public class Extras {

        @SerializedName("Data")
        @Expose
        private List<Datum> data = null;

        public List<Datum> getData() {
            return data;
        }

        public void setData(List<Datum> data) {
            this.data = data;
        }

    }

}
