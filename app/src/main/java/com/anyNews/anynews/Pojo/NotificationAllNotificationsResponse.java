package com.anyNews.anynews.Pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class NotificationAllNotificationsResponse implements Serializable {


    public class Datum implements Serializable
    {

        @SerializedName("image_url")
        @Expose
        private String imageUrl;
        @SerializedName("push_title")
        @Expose
        private String pushTitle;
        @SerializedName("push_description")
        @Expose
        private String pushDescription;
        @SerializedName("publish_time")
        @Expose
        private Long publishTime;
        @SerializedName("post_id")
        @Expose
        private Integer postId;
        private final static long serialVersionUID = 3416254210976415319L;

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public String getPushTitle() {
            return pushTitle;
        }

        public void setPushTitle(String pushTitle) {
            this.pushTitle = pushTitle;
        }

        public String getPushDescription() {
            return pushDescription;
        }

        public void setPushDescription(String pushDescription) {
            this.pushDescription = pushDescription;
        }

        public Long getPublishTime() {
            return publishTime;
        }

        public void setPublishTime(Long publishTime) {
            this.publishTime = publishTime;
        }

        public Integer getPostId() {
            return postId;
        }

        public void setPostId(Integer postId) {
            this.postId = postId;
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
    private final static long serialVersionUID = 2882389038047626877L;

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