package com.anyNews.anynews.Pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class LanguagesListResponse implements Serializable {

    public class Datum implements Serializable
    {
        @SerializedName("id")
        @Expose
        private Integer id;
        @SerializedName("e_name")
        @Expose
        private String eName;
        @SerializedName("name")
        @Expose
        private String name;
        @SerializedName("type")
        @Expose
        private String type;
        @SerializedName("first_image")
        @Expose
        private String firstImage;
        @SerializedName("second_image")
        @Expose
        private String secondImage;
        private final static long serialVersionUID = 2611494583918793502L;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
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

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
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
    }

    public class Extras implements Serializable
    {

        @SerializedName("Data")
        @Expose
        private List<Datum> data = null;
        private final static long serialVersionUID = -7572517343576147319L;

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
