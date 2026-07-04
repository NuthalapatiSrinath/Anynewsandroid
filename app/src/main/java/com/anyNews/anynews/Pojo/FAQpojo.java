package com.anyNews.anynews.Pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class FAQpojo implements Serializable {

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
        @SerializedName("Count")
        @Expose
        private Integer count;

        @SerializedName("Data")
        @Expose
        private List<FAQpojo> data = null;
        private final static long serialVersionUID = 765784740568455718L;

        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }

        public List<FAQpojo> getData() {
            return data;
        }

        public void setData(List<FAQpojo> data) {
            this.data = data;
        }

    }

    public String getHelpDataID() {
        return helpDataID;
    }

    public void setHelpDataID(String helpDataID) {
        this.helpDataID = helpDataID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @SerializedName("HelpDataID")
    @Expose
    private String helpDataID;
    @SerializedName("Title")
    @Expose
    private String title;
    @SerializedName("Description")
    @Expose
    private String description;

}
