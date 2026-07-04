package com.anyNews.anynews.Pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LikeSelectionRequest {


    @SerializedName("option")
    @Expose
    private Long option;

    @SerializedName("writer_id")
    @Expose
    private Long writer_id;

    public Long getWriter_id() {
        return writer_id;
    }

    public void setWriter_id(Long writer_id) {
        this.writer_id = writer_id;
    }

    public Integer getIs_follow() {
        return is_follow;
    }

    public void setIs_follow(Integer is_follow) {
        this.is_follow = is_follow;
    }

    @SerializedName("is_follow")
    @Expose
    private Integer is_follow;

    @SerializedName("draft_id")
    @Expose
    private Long draft_id;

    @SerializedName("post_id")
    @Expose
    private Long post_id;

    @SerializedName("sub_post_id")
    @Expose
    private Long sub_post_id;

    @SerializedName("type")
    @Expose
    private Integer type;

    @SerializedName("property")
    @Expose
    private String property;

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public Long getDraft_id() {
        return draft_id;
    }

    public void setDraft_id(Long draft_id) {
        this.draft_id = draft_id;
    }

    public Long getPost_id() {
        return post_id;
    }

    public void setPost_id(Long post_id) {
        this.post_id = post_id;
    }

    public Long getSub_post_id() {
        return sub_post_id;
    }

    public void setSub_post_id(Long sub_post_id) {
        this.sub_post_id = sub_post_id;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Long getOption() {
        return option;
    }

    public void setOption(Long option) {
        this.option = option;
    }
}
