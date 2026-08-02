package com.anyNews.anynews.Utils;

public class Constants {

    public static String SURVEY_STATUS = "SURVEY_STATUS" ;
    public static String IS_LANGUAGE_SELECTED = "isLanguageSelected";
    public  static  String Text_Size="Text_Size";

    public static String PREF_NAME = "my_pref";

    public static String ApiKEY = "ApiKey";

    public static String DraftID = "DraftID";

    public static String Test_Size = "Test_Size";


    public static String Version_ID = "Version_ID";

    public static String POST_ID = "Post_ID";

    public static String DeviceID = "DeviceID";

    public static String FCM_TOKEN = "FCM_TOKEN";
    public static String USER_SESSION_ID = "user_session_id";
    public static String HOME_SCREEN = "HOME_SCREEN";
    public static final String Channel_ID=  "AnyNewsApp";

    public static  String IS_USER_ALREADY_LOGIN = "IS_USER_ALREADY_LOGIN";

    public static  String IS_ALREADY_LOGIN = "IS_ALREADY_LOGIN";

    public static  String NOT_FIRST_TIME_LOGIN = "FIRST_TIME_LOGIN";

    public static String CategoryID = "CategoryID";

    public static String Search_icon = "SearchIcon";

    public static String PhoneNumber = "PhoneNumber";

    public static String UserID ="USERID";

    public static String SessionID ="SessionID";

    public static String Buyer_Name ="Buyer_Name";

    public static String CategoryIDDefault ="CategoryIDDefault";

    public static String Add_Remove = "Add_Remove";

    public static String ArticleNewsID = "Article_NewsID" ;

    public static String Title ="Title";

    public static  String Description="Description";

    public static  String Long="Longitude";

    public static  String AddressId="AddressID";

    public static String Response = "Response";

    public static String Delivery_type = "Delivery_Type";

    public  static  String Address_Default = "Address_Default";

    public static String OrderId = "OrderID";

    public static String switchLocation = "switchLocation";

    public static String switchNotification = "switchNotification";

    public static String WalletAmount = "Wallet_Amount" ;

    public static String Email = "Email";
    public static String USER_NAME = "USER_NAME";

    //
    public static String Lang = "LanName";

    //


    public static String LanguageID = "LanguageID";
    public static String RegionID = "RegionID";


    public static String Show_Animation="Show_Animation";


    /* from now */
    public static String State_Name="State_Name";

    public static String Distric_Name="Distric_Name";

    public static String StateID="StateID";

    public static String StaticDataApi="staticDataApi";

    public static String is_login="is_login";
    public static String is_reporter="is_reporter";
    public static String reporter_language_id="reporter_language_id";
    public static String REPORTER_STATUS_CHANGED = "REPORTER_STATUS_CHANGED";
    public static String dist_name="dist_name";


    public static String Selected_Distric_Name="Selected_Distric_Name";
    public static String Selected_Distric_Name1="Selected_Distric_Name1";
    public static String MOVE_NEXT_SCREEN="MOVE_NEXT_SCREEN";
    public static String REFERRER_POST="referrer_post";

    public static String Selected_Phone_Number="Selected_Phone_Number";

    public static String CUSST_ID="CUSST_ID";
    public static String AVAILABLEPOINTS="AVAILABLEPOINTS";
    public static String REDEEMPOINTS="REDEEMPOINTS";
    public static String REFERAL_LINK="REFERAL_LINK";
    public static String REFERAL_LINK2="REFERAL_LINK2";
    public static String PAN_CARD_STATUS="PAN_CARD_STATUS";
    public static String ARTICLE_ID="ARTICLE_ID";
    public static String  PRD_ID="PRD_ID";

    public static String USER_ID="Selected_Distric_Name";

    public static String Back_Button_Move="Back_Button_Move";

    public static String POST_UPDATED_ID = "post_id";

    public static String Event_Status = "event_status";
    public static String ENGAGEMENT_BADGE_COLOR = "engagement_badge_color";
    public static String THEME_COLOR = "theme_color";
    public static String ALLOW_SCREEN_CAPTURE = "allow_screen_capture";
    public static String LANGUAGE_FONT_SETTINGS = "language_font_settings";
    /** JSON map of custom font key → download URL (admin-uploaded TTF/OTF). */
    public static String CUSTOM_FONT_URLS = "custom_font_urls";
    public static String ACTION_ICON_ROW_OFFSET = "action_icon_row_offset";
    public static String ACTION_ICON_SIZE_DP = "action_icon_size_dp";
    public static String LIKE_CIRCLE_SIZE_DP = "like_circle_size_dp";
    public static String ENABLE_VIDEO_POST = "enable_video_post";
    public static String ENABLE_ARTICLE_POST = "enable_article_post";
    public static String ENABLE_FULL_POST = "enable_full_post";
    public static String TITLE_BODY_GAP_DP = "title_body_gap_dp";
    public static String CONTENT_BLOCK_OFFSET_DP = "content_block_offset_dp";
    public static String FOOTER_STYLE = "footer_style";
    public static String WRITER_STRIP_STYLE = "writer_strip_style";
    /** JSON of full-video share primary card config from splash. */
    public static String FULL_VIDEO_SHARE_CARD = "full_video_share_card";
    /** JSON of {@link com.anyNews.anynews.Pojo.PollSurveyColorsConfig} from splash. */
    public static String POLL_SURVEY_COLORS = "poll_survey_colors";
    /** Flip feed position across DayNight / battery-saver recreates (light↔dark). */
    public static String FLIP_RESTORE_INDEX = "flip_restore_index";
    public static String FLIP_RESTORE_POST_ID = "flip_restore_post_id";
    public static String FLIP_RESTORE_PENDING = "flip_restore_pending";
    /** epoch ms when flip checkpoint was written (theme recreate TTL). */
    public static String FLIP_RESTORE_AT = "flip_restore_at";
    /** JSON of {@link com.anyNews.anynews.Pojo.CricketMiniPlayerPosition} from splash / poll. */
    public static String CRICKET_MINI_PLAYER_POSITION = "cricket_mini_player_position";
    /** "true"/"false" admin toggle for the floating cricket mini player (default true). */
    public static String CRICKET_MINI_PLAYER_ENABLED = "cricket_mini_player_enabled";
    public static String CRICKET_MINI_PLAYER_POLL_INTERVAL_SECONDS = "cricket_mini_player_poll_interval_seconds";
    public static String SESSION_HEARTBEAT_INTERVAL_SECONDS = "session_heartbeat_interval_seconds";
    /** "true"/"false" admin toggle for the bottom-footer cricket tab (default false). */
    public static String CRICKET_FOOTER_TAB_ENABLED = "cricket_footer_tab_enabled";
    // One-shot post id parsed from a shared deep link (/p/{id}, anynews://post/{id},
    // anws.in/{id}, ...). Persisted by StartingActivity and consumed+cleared by
    // HomePage so the exact post opens even on splash routes that don't forward
    // the SHARE_REDIRECT_ID intent extra (returning-user paths).
    public static String PENDING_SHARE_POST = "pending_share_post_id";
    public static String PROFILE_CACHE = "reporter_profile_cache_json";
    public static String Selected_YES_PERCENT="Selected_YES_PERCENT";
    public static String Selected_NO_PERCENT="Selected_NO_PERCENT";
    public static String Selected_YES_COUNT="Selected_YES_COUNT";
    public static String Selected_NO_COUNT="Selected_NO_COUNT";
    // firebase events triggered
    public static String type_firebase = "views";
    public static String type_firebase_button = "button_click";
    public static String button_name_local = "local";
    public static String button_name_myfeed = "my_feed";
    public static String button_name_home = "home";
    public static String button_name_events = "events";
    public static String button_name_category = "category";
    public static String button_name_post = "post";
    public static String button_name_profile = "profile";
    public static String APP_RATING_SUBMITTED = "app_rating_submitted";
    public static String APP_RATING = "app_rating";
    public static String isUnreadPostsSelected = "isunreadpostsselected";
    public final static String AD_REGULAR = "regular";
    public final static String AD_FULL_IMAGE  = "fullImage";
    public final static String AD_MEDIUM  = "medium";
    public final static String VERSION_NAME= "V3" ;
    public final static String VERSION_NAME_DIVIDER= "/" ;
    public final static String VERSION_NAME_ARTICLES= "articles" ;
    public final static String MALAYALAM_LANG_ID_API_VERSION=VERSION_NAME+VERSION_NAME_DIVIDER+"1"+VERSION_NAME_ARTICLES;
    public final static String HINDI_LANG_ID_API_VERSION=VERSION_NAME+VERSION_NAME_DIVIDER+"2"+VERSION_NAME_ARTICLES;
    public final static String TELUGU_LANG_ID_API_VERSION=VERSION_NAME+VERSION_NAME_DIVIDER+"3"+VERSION_NAME_ARTICLES;
    public final static String TAMIL_LANG_ID_API_VERSION=VERSION_NAME+VERSION_NAME_DIVIDER+"4"+VERSION_NAME_ARTICLES;
    public final static String KANNADA_LANG_ID_API_VERSION=VERSION_NAME+VERSION_NAME_DIVIDER+"5"+VERSION_NAME_ARTICLES;
    public final static String ODIA_LANG_ID_API_VERSION=VERSION_NAME+VERSION_NAME_DIVIDER+"5"+VERSION_NAME_ARTICLES;
//StateID


}
