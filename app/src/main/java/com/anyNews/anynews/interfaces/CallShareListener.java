package com.anyNews.anynews.interfaces;

public class CallShareListener {
    public ShareInterfaceListener shareInterfaceListener;
    public void setShareInterfaceListener(ShareInterfaceListener shareInterfaceListener){
        this.shareInterfaceListener=shareInterfaceListener;
    }
    public void triggerEvent() {
        if (shareInterfaceListener != null) {
            shareInterfaceListener.shareListener();
        }
    }
}
