package com.anyNews.anynews.models;

public class NotificationModel {
    String title;
    String desc;
    int items;

    public NotificationModel(String title, String desc, int items) {
        this.title = title;
        this.desc = desc;
        this.items = items;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getItems() {
        return items;
    }

    public void setItems(int items) {
        this.items = items;
    }
}
