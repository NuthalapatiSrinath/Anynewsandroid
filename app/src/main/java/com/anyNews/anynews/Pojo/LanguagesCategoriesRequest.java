package com.anyNews.anynews.Pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class LanguagesCategoriesRequest implements Serializable {
    @SerializedName("DraftID")
    @Expose
    private Integer draftID;
    @SerializedName("language_id")
    @Expose
    private Integer languageId;
    @SerializedName("skip")
    @Expose
    private Integer skip;
    @SerializedName("limit")
    @Expose
    private Integer limit;
    private final static long serialVersionUID = 3763684851187578968L;

    public Integer getDraftID() {
        return draftID;
    }

    public void setDraftID(Integer draftID) {
        this.draftID = draftID;
    }

    public Integer getLanguageId() {
        return languageId;
    }

    public void setLanguageId(Integer languageId) {
        this.languageId = languageId;
    }

    public Integer getSkip() {
        return skip;
    }

    public void setSkip(Integer skip) {
        this.skip = skip;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }


}
