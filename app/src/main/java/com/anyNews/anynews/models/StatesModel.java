package com.anyNews.anynews.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Collection;
import java.util.List;

public class StatesModel {

    public class Datum {

        @SerializedName("name")
        @Expose
        private String name;
        @SerializedName("id")
        @Expose
        private Integer id;
        @SerializedName("icon_url")
        @Expose
        private String iconUrl;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getIconUrl() {
            return iconUrl;
        }

        public void setIconUrl(String iconUrl) {
            this.iconUrl = iconUrl;
        }

    }
    @SerializedName("success")
    @Expose
    private Boolean success;
    @SerializedName("data")
    @Expose
    private List<Datum> data = null;
    @SerializedName("next_screen")
    @Expose
    private String nextScreen;
    @SerializedName("msg")
    @Expose
    private String msg;

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

    public String getNextScreen() {
        return nextScreen;
    }

    public void setNextScreen(String nextScreen) {
        this.nextScreen = nextScreen;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

}
