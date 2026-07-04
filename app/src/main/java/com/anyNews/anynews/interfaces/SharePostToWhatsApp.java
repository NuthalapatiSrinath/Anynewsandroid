package com.anyNews.anynews.interfaces;

public class SharePostToWhatsApp {
    shareInter shareInter;

    public SharePostToWhatsApp(SharePostToWhatsApp.shareInter shareInter) {
        this.shareInter = shareInter;
    }
    public void shareInterface(boolean b,int pos){
        shareInter.sharingInWhatsApp(b,pos);
    }
   public void showHalfVideoFullScreen(boolean b){
        shareInter.showHalfVideoFullScreen(b);
    }


    public interface shareInter {
    public void sharingInWhatsApp(boolean b,int pos);
    public void showHalfVideoFullScreen(boolean b);
}
}
