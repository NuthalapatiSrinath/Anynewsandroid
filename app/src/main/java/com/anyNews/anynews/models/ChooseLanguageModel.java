package com.anyNews.anynews.models;

public class ChooseLanguageModel {

    String name_type;
    String language_id;
    int image;
    int image_selected;

    public ChooseLanguageModel(String name_type, String language_id, int image, int image_selected) {
        this.name_type = name_type;
        this.language_id = language_id;
        this.image = image;
        this.image_selected = image_selected;
    }

    public String getLanguage_id() {
        return language_id;
    }

    public void setLanguage_id(String language_id) {
        this.language_id = language_id;
    }

    public int getImage_selected() {
        return image_selected;
    }

    public void setImage_selected(int image_selected) {
        this.image_selected = image_selected;
    }

    public String getName_type() {
        return name_type;
    }

    public void setName_type(String name_type) {
        this.name_type = name_type;
    }


    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }
}