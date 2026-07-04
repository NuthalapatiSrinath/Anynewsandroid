package com.anyNews.anynews.Pojo;



import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.nativead.NativeAd;

public class FlipGoogleNativeAds {
    NativeAd ad;

    public InterstitialAd getInterstitialAd() {
        return interstitialAd;
    }

    public void setInterstitialAd(InterstitialAd interstitialAd) {
        this.interstitialAd = interstitialAd;
    }

    InterstitialAd interstitialAd;
    String adId;
    String adCode;

    public String getAd_type() {
        return ad_type;
    }

    public void setAd_type(String ad_type) {
        this.ad_type = ad_type;
    }

    String ad_type;

    public com.facebook.ads.NativeAd getFbAd() {
        return fbAd;
    }

    public void setFbAd(com.facebook.ads.NativeAd fbAd) {
        this.fbAd = fbAd;
    }

    com.facebook.ads.NativeAd fbAd;

    public NativeAd getAd() {
        return ad;
    }

    public void setAd(NativeAd ad) {
        this.ad = ad;
    }

    public String getAdId() {
        return adId;
    }

    public void setAdId(String adId) {
        this.adId = adId;
    }

    public String getAdCode() {
        return adCode;
    }

    public void setAdCode(String adCode) {
        this.adCode = adCode;
    }


    public int getReqPos() {
        return reqPos;
    }

    public void setReqPos(int reqPos) {
        this.reqPos = reqPos;
    }

    int reqPos;

    public boolean isAdRequested() {
        return isAdRequested;
    }

    public void setAdRequested(boolean adRequested) {
        isAdRequested = adRequested;
    }

    boolean isAdRequested;
}



