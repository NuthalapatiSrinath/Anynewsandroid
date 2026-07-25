package com.anyNews.anynews.Services;

public class Config {


    public static final int NEWS_PER_PAGE = 10;
    public static final int NEWS_TOTAL_ROWS = 100;
    public static final int COMMENTS_TOTAL_ROWS = 15;

    // Test / Coolify staging — switch SITE_URL back to api.anynews.co.in for Play release
    public static final String SITE_URL = "https://anynews.fusionvertexsolutions.com/";
    // Production (Play):
//    public static final String SITE_URL = "https://api.anynews.co.in/";

    static final String API_URL = SITE_URL+"api/mobileapi/";

    public static final String IMAGE_PATH_NEWS_THUMBS = "public/uploads/images/thumbs/";
    public static final String IMAGE_PATH_NEWS = "public/uploads/images/";
    public static final String AVATAR_PATH = SITE_URL+"public/uploads/images/avatar.png";

    public static final String YOUTUBE_API_KEY = "YOUR_YOUTUBE_API_KEY";

    public static final String LINK_PRIVACY_POLICY = "https://anynews.co.in/2019/04/privacy-policy.html"; //your privacy_policy
    public static final String LINK_CONTACT_EMAIL = "support@anynews.co.in"; //your email for contact


    public static final boolean SHOW_ADMOB_ADS = true; //show ads

    public static final String TAG = "MYMY";
}
