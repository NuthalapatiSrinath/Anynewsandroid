package com.anyNews.anynews.Pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class UserImagePojoResponse implements Serializable {

    public class Extras implements Serializable
    {

        @SerializedName("Status")
        @Expose
        private String status;
        @SerializedName("ImageID")
        @Expose
        private String imageID;
        @SerializedName("Image50")
        @Expose
        private String image50;
        @SerializedName("Image100")
        @Expose
        private String image100;
        @SerializedName("Image250")
        @Expose
        private String image250;
        @SerializedName("Image550")
        @Expose
        private String image550;
        @SerializedName("Image900")
        @Expose
        private String image900;
        @SerializedName("ImageOriginal")
        @Expose
        private String imageOriginal;
        private final static long serialVersionUID = 2123900296608014814L;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getImageID() {
            return imageID;
        }

        public void setImageID(String imageID) {
            this.imageID = imageID;
        }

        public String getImage50() {
            return image50;
        }

        public void setImage50(String image50) {
            this.image50 = image50;
        }

        public String getImage100() {
            return image100;
        }

        public void setImage100(String image100) {
            this.image100 = image100;
        }

        public String getImage250() {
            return image250;
        }

        public void setImage250(String image250) {
            this.image250 = image250;
        }

        public String getImage550() {
            return image550;
        }

        public void setImage550(String image550) {
            this.image550 = image550;
        }

        public String getImage900() {
            return image900;
        }

        public void setImage900(String image900) {
            this.image900 = image900;
        }

        public String getImageOriginal() {
            return imageOriginal;
        }

        public void setImageOriginal(String imageOriginal) {
            this.imageOriginal = imageOriginal;
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

