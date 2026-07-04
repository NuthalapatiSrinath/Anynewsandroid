package com.anyNews.anynews.Pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CardsDisplay {


    public boolean isWallet_balance() {
        return wallet_balance;
    }

    public void setWallet_balance(boolean wallet_balance) {
        this.wallet_balance = wallet_balance;
    }

    public boolean isNext_payment() {
        return next_payment;
    }

    public void setNext_payment(boolean next_payment) {
        this.next_payment = next_payment;
    }

    public boolean isFollowers() {
        return followers;
    }

    public void setFollowers(boolean followers) {
        this.followers = followers;
    }

    public boolean isArticles() {
        return articles;
    }

    public void setArticles(boolean articles) {
        this.articles = articles;
    }

    public boolean isLikes() {
        return likes;
    }

    public void setLikes(boolean likes) {
        this.likes = likes;
    }

    @SerializedName("wallet_balance")
    @Expose
    boolean wallet_balance;

    @SerializedName("next_payment")
    @Expose
    boolean next_payment;

    @SerializedName("followers")
    @Expose
    boolean followers;
    @SerializedName("articles")
    @Expose
    boolean articles;
    @SerializedName("likes")
    @Expose
    boolean likes;
    //"cards_display":{"wallet_balance":true,"next_payment":true,"followers":true,"articles":true,"likes":true}}
}
