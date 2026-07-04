package com.anyNews.anynews.Pojo;



public class CardItem {

    private String  mTextResource;
    private String mTitleResource;
    private String share_link;
    private String title;
    private Long Post_id;

    public CardItem(String mTextResource, String mTitleResource, String share_link, String title,Long Post_id) {
        this.mTextResource = mTextResource;
        this.mTitleResource = mTitleResource;
        this.share_link = share_link;
        this.title = title;
        this.Post_id=Post_id;
    }

    public Long getPost_id() {
        return Post_id;
    }

    public void setPost_id(Long post_id) {
        Post_id = post_id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return mTextResource;
    }

    public String getTitle() {
        return mTitleResource;
    }

    public String getmTextResource() {
        return mTextResource;
    }

    public void setmTextResource(String mTextResource) {
        this.mTextResource = mTextResource;
    }

    public String getmTitleResource() {
        return mTitleResource;
    }

    public void setmTitleResource(String mTitleResource) {
        this.mTitleResource = mTitleResource;
    }

    public String getShare_link() {
        return share_link;
    }

    public void setShare_link(String share_link) {
        this.share_link = share_link;
    }
}
