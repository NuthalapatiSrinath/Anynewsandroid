package com.anyNews.anynews.Pojo;

import java.io.Serializable;

public class NewArticlemodel implements Serializable {
    String Title;
    String Description;
    String ImageOriginal;

    public NewArticlemodel() {
    }

    public NewArticlemodel(String title, String description, String imageOriginal) {
        Title = title;
        Description = description;
        ImageOriginal = imageOriginal;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getImageOriginal() {
        return ImageOriginal;
    }

    public void setImageOriginal(String imageOriginal) {
        ImageOriginal = imageOriginal;
    }
}
