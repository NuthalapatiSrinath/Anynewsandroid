package com.anyNews.anynews.Pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

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
    private int firstImage;
    @SerializedName("second_image")
    @Expose
    private int secondImage;
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

    public int getFirstImage() {
        return firstImage;
    }

    public void setFirstImage(int firstImage) {
        this.firstImage = firstImage;
    }

    public int getSecondImage() {
        return secondImage;
    }

    public void setSecondImage(int secondImage) {
        this.secondImage = secondImage;
    }

}