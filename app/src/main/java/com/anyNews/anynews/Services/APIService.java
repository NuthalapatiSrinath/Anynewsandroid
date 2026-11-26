package com.anyNews.anynews.Services;

import com.anyNews.anynews.Pojo.BaseResponse;
import com.anyNews.anynews.Pojo.AddArticleResponse;
import com.anyNews.anynews.Pojo.AddLikeResponse;
import com.anyNews.anynews.Pojo.CategoriesResponse;
import com.anyNews.anynews.Pojo.CategorySearchResponse;
import com.anyNews.anynews.Pojo.DailogSendRespouse;
import com.anyNews.anynews.Pojo.EncreptionRequest;
import com.anyNews.anynews.Pojo.GenerateOtpResponse;
import com.anyNews.anynews.Pojo.GetReportIssueResponce;
import com.anyNews.anynews.Pojo.HelpResponse;
import com.anyNews.anynews.Pojo.LanguageDisply;
import com.anyNews.anynews.Pojo.LanguagesCategoriesResponse;
import com.anyNews.anynews.Pojo.ListAllArticlesResponse;
import com.anyNews.anynews.Pojo.ListOfCommentsResponse;
import com.anyNews.anynews.Pojo.ListOfUserArticlesResponse;
import com.anyNews.anynews.Pojo.LoginDetailesResponse;
import com.anyNews.anynews.Pojo.NotificationAllNotificationsResponse;
import com.anyNews.anynews.Pojo.ReportResponse;
import com.anyNews.anynews.Pojo.SingleArticleResponse;
import com.anyNews.anynews.Pojo.StartActivityReqModel;
import com.anyNews.anynews.Pojo.UpdateSettingsResponse;
import com.anyNews.anynews.Pojo.UserImagePojoResponse;
import com.anyNews.anynews.Pojo.UserProfileResponse;
import com.anyNews.anynews.Pojo.UserProfileUpdateResponse;
import com.anyNews.anynews.Utils.ModelClass;
import com.google.gson.JsonObject;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface APIService {


    String name="";
    //SplashScreen
    @POST("app/{version}/Splash_Screen")     // @POST("app/V1/Splash_Screen")         // ok
    Call<StartActivityReqModel> Splash(@Path("version") String version, @Body JsonObject splash);

    // Cricket Live Mini Player — single active match (or null).
    @GET("app/{version}/cricket_active_match")
    Call<com.anyNews.anynews.Pojo.CricketActiveMatchResponse> cricketActiveMatch(@Path("version") String version);

    // Cricket Match Center — full scorecard / commentary / stats for a match.
    @GET("app/{version}/cricket_match_detail")
    Call<com.anyNews.anynews.Pojo.CricketMatchDetailResponse> cricketMatchDetail(
            @Path("version") String version,
            @retrofit2.http.Query("crex_key") String crexKey);

    /** Full Telugu panchang year pack (cacheable forever for year+lat/lng). */
    @GET("app/{version}/calendar/year/{year}")
    Call<com.anyNews.anynews.panchang.PanchangModels.YearPack> calendarYear(
            @Path("version") String version,
            @Path("year") int year,
            @retrofit2.http.Query("lat") double lat,
            @retrofit2.http.Query("lng") double lng,
            @retrofit2.http.Query("tz") String tz);

    //Shops
    @POST("app/{version}/Update_Device_FCM_Token")                  //  ok
    Call<AddLikeResponse> tokens(@Path("version") String version, @Body JsonObject shop);

    //app/List_All_Languages
    @GET("app/{version}/List_All_Languages")
    Call<LanguageDisply> LanguagesList(@Path("version") String version);

    //category dropdown
    @POST("app/{version}/List_All_Categories_With_Language")
    Call<LanguagesCategoriesResponse> lc(@Path("version") String version, @Body JsonObject home);


    //Chooselanguage
    @POST("app/{version}/Update_User_Language")
    Call<JsonObject> lang(@Path("version") String version, @Body JsonObject langgg);


    //list_of_all_articles
    @POST("app/{version}/List_All_Articles")
    Call<ListAllArticlesResponse> News(@Path("version") String version, @Body JsonObject newsss);


    @POST("app/{version}/List_All_Articles")
    Call<ModelClass> News_data(@Path("version") String version, @Body JsonObject newsss);

    //Categories
    @POST("app/{version}/List_All_categories")
    Call<CategoriesResponse> categories(@Path("version") String version, @Body JsonObject catssss);



    //CategorySearch
    @POST("app/{version}/Category_Search")
    Call<CategorySearchResponse> catSearch(@Path("version") String version, @Body JsonObject search);

    //Chooselanguage
    @POST("app/{version}/notifications")
    Call<NotificationAllNotificationsResponse> notifications(@Path("version") String version, @Body JsonObject langgg);

    //GenerateOtp
    @POST("app/{version}/Generate_User_OTP")
    Call<EncreptionRequest> SendOtp(@Path("version") String version, @Body JsonObject otp);


    //GenerateOtp
    @POST("app/v1/Generate_User_OTP")
    Call<GenerateOtpResponse> readotp(@Body JsonObject otp);

    //ValidateOtp
    @POST("app/{version}/Validate_User_OTP")
    Call<EncreptionRequest> ReadOtp(@Path("version") String version, @Body JsonObject readotp);


    //List_All_Comment_For_Article
    @POST("app/{version}/List_All_Comment_For_Article")
    Call<ListOfCommentsResponse> listofComments(@Path("version") String version, @Body JsonObject pincode);


    //UserProfile
    @POST("app/{version}/settings")
    Call<UserProfileResponse> setting(@Path("version") String version, @Body JsonObject usersss);

    //UserProfile
    @POST("app/{version}/update_settings")
    Call<UpdateSettingsResponse> notifiction_upate(@Path("version") String version, @Body JsonObject usersss);

    //UserProfile
    @POST("app/{version}/update_profile_pic")
    Call<EncreptionRequest> profileUpdateimage(@Path("version") String version, @Body JsonObject usersss);


    //AddAricle
    @POST("app/{version}/Add_Article_News")
    Call<AddArticleResponse> adddart(@Path("version") String version, @Body JsonObject arts);


    /* i  did so  for*/
    @Multipart
    @POST("upload/Upload_Image")
    Call<UserImagePojoResponse> uploadImage(@Part MultipartBody.Part body);
    //Register

    @POST("app/v1/User_Profile_Update")
    Call<UserProfileUpdateResponse> Update(@Body JsonObject update);
    //Get_single_article
    @POST("app/Get_Single_Article")
    Call<SingleArticleResponse> Single(@Body JsonObject singleeee);
    //Like
    @POST("app/Add_Like")
    Call<AddLikeResponse> likess(@Body JsonObject likessss);
    //Advertisements
    @POST("app/Fetch_Advertisement")
    Call<JsonObject> fetchAdvertisements(@Body JsonObject jsonObject);

    //Empty_Cart
    @POST("app/Add_Report")
    Call<ReportResponse> report(@Body JsonObject reports);


    //My_posts
    @POST("app/List_All_User_Articles")
    Call<ListOfUserArticlesResponse> myarticles(@Body JsonObject articlesss);


    public static final String URL_Upload_Image = "upload/Upload_File";
    public static final String GENERATE_USER_OTP = "app/v1/Generate_User_OTP";



    //UpdateProfile
    @POST("app/List_All_Help_Data")
    Call<HelpResponse> Help(@Body JsonObject update);




//
//    //GenerateShopotp
//    @POST("Generate_ShopOTP_For_Buyer_Order")
//    Call<GenerateShopOtp> generate(@Body JsonObject generate_otp);
//
//    //ReferrenceNumberValidation
//    @POST("Validate_Referral_Phone_Number")
//    Call<RegistrationValidation> validate_ref(@Body JsonObject referral);









    // 1


    @POST("app/{version}/Splash_Screen")     // @POST("app/V1/Splash_Screen")         // ok
    Call<EncreptionRequest> Splash_Screen(@Path("version") String version, @Body JsonObject splash);

   //2
    //app/List_All_Languages
    @GET("app/{version}/List_All_Languages")
    Call<EncreptionRequest> Languages_All_Languages(@Path("version") String version);


    // 3
    //Chooselanguage
    @POST("app/{version}/Update_User_Language")
    Call<JsonObject> Update_Language(@Path("version") String version, @Body JsonObject langgg);

    //4
    // https://xapi.anynews.co.in/app/v2/get_states
    //Chooselanguage
    @POST("app/{version}/get_states")   // get_districts
    Call<EncreptionRequest>Get_states_requiest(@Path("version") String version, @Body JsonObject langgg);

    //5

    //Update Like Or DisLike
    @POST("app/{version}/update_state")                  //  ok
    Call<EncreptionRequest> update_state(@Path("version") String version, @Body JsonObject shop);

   // 6
   @POST("app/{version}/get_districts")   // get_districts
   Call<EncreptionRequest>Get_districts(@Path("version") String version, @Body JsonObject langgg);

    @POST("app/{version}/update_district")   // get_districts
    Call<EncreptionRequest>Update_district(@Path("version") String version, @Body JsonObject langgg);



    //List_All_Comment_For_Article
    @POST("app/{version}/List_All_Comment_For_Article")
    Call<EncreptionRequest> List_All_Comment_For_Article(@Path("version") String version, @Body JsonObject pincode);

 //List_All_Comment_For_Article
    @GET("app/{version}/comments/report-problems/list")
    Call<EncreptionRequest> getListOfReportsProblems(@Path("version") String version);

    @POST("app/{version}/comments/report")
    Call<EncreptionRequest> postReportFlag(@Path("version") String version,@Body JsonObject flag);

    @POST("app/{version}/comments/like")
    Call<EncreptionRequest> commentLike(@Path("version") String version, @Body JsonObject body);

    //Add_Comment
    @POST("app/{version}/Add_Comment")
    Call<EncreptionRequest> comment(@Path("version") String version, @Body JsonObject comments);



    //Update Like Or DisLike
    @POST("app/{version}/update_like")                  //  ok
    Call<EncreptionRequest> update_like(@Path("version") String version, @Body JsonObject shop);

    //Shops
    @POST("app/{version}/update_post_count")                  //  ok
    Call<EncreptionRequest> update_post_count(@Path("version") String version, @Body JsonObject shop);

    /** Completed Daily Updates share (fuel, gold, rashi, …). Not an article count. */
    @POST("app/{version}/daily_updates_share")
    Call<EncreptionRequest> daily_updates_share(@Path("version") String version, @Body JsonObject body);


    //Categories
    @POST("app/{version}/List_All_categories")
    Call<EncreptionRequest>List_All_Categories(@Path("version") String version, @Body JsonObject catssss);



    @POST("app/{version}/List_All_Articles")
    Call<EncreptionRequest> List_All_Articles(@Path("version") String version, @Body JsonObject newsss);

    /**
     * Cloudflare-cacheable anonymous feed (snapshot engagement counts).
     * Prefer for main/local flip reads; keep List_All_Articles for search/special/flash
     * and as soft fallback when this misses.
     */
    @GET("app/{version}/public/feed")
    Call<com.anyNews.anynews.Pojo.PublicEdgeFeedResponse> Public_Feed(
            @Path("version") String version,
            @Query("language_id") int languageId,
            @Query("category_id") Integer categoryId,
            @Query("state_id") Integer stateId,
            @Query("dist_id") Integer distId,
            @Query("skip") Integer skip,
            @Query("post_id") Integer postId,
            @Query("feed_kind") String feedKind);

    @GET("app/{version}/public/main/lang/{language_id}/page/{page}")
    Call<com.anyNews.anynews.Pojo.PublicEdgeFeedResponse> Public_Feed_Main(
            @Path("version") String version,
            @Path("language_id") int languageId,
            @Path("page") int page,
            @Query("pin") Integer pinPostId);

    @GET("app/{version}/public/local/lang/{language_id}/state/{state_id}/dist/{dist_id}/page/{page}")
    Call<com.anyNews.anynews.Pojo.PublicEdgeFeedResponse> Public_Feed_Local(
            @Path("version") String version,
            @Path("language_id") int languageId,
            @Path("state_id") int stateId,
            @Path("dist_id") int distId,
            @Path("page") int page);

    @GET("app/{version}/public/flash/lang/{language_id}/page/{page}")
    Call<com.anyNews.anynews.Pojo.PublicEdgeFeedResponse> Public_Feed_Flash(
            @Path("version") String version,
            @Path("language_id") int languageId,
            @Path("page") int page);

    @GET("app/{version}/public/special/lang/{language_id}/page/{page}")
    Call<com.anyNews.anynews.Pojo.PublicEdgeFeedResponse> Public_Feed_Special(
            @Path("version") String version,
            @Path("language_id") int languageId,
            @Path("page") int page);

    /** Shared splash shell (no draft) — edge-cacheable. */
    @GET("app/{version}/public/splash_config")
    Call<com.anyNews.anynews.Pojo.PublicEdgeSplashResponse> Public_Splash_Config(
            @Path("version") String version,
            @Query("language_id") int languageId);

    /**
     * Daily Updates hub chrome (gold/weather/stock/flash/local/cricket) —
     * Cloudflare-cacheable anonymous GET. Prefer over stock_market / List for paint.
     */
    @GET("app/{version}/public/daily_updates")
    Call<com.google.gson.JsonObject> Public_Daily_Updates(
            @Path("version") String version,
            @Query("language_id") int languageId,
            @Query("state_id") Integer stateId,
            @Query("dist_id") Integer distId);

    /**
     * Lightweight new-APK beacon when CF-intended open fell back (Main/Local List
     * soft fallback, or push CF fail without List). Fire-and-forget; 204.
     */
    @POST("app/{version}/public/edge_origin_event")
    Call<Void> Public_Edge_Origin_Event(
            @Path("version") String version,
            @Body JsonObject body);

    /** CF-Cache-Status samples from public GET responses (debug telemetry). */
    @POST("app/{version}/public/edge_telemetry")
    Call<Void> Public_Edge_Telemetry(
            @Path("version") String version,
            @Body JsonObject body);

    /** Dedicated Flash / push feed — Redis-backed; same envelope as List_All_Articles. */
    @POST("app/{version}/Flash_News")
    Call<EncreptionRequest> Flash_News(@Path("version") String version, @Body JsonObject body);

    /** Way2-style typeahead suggestions (text + count). Debounce on client. */
    @POST("app/{version}/Search_Suggestions")
    Call<EncreptionRequest> Search_Suggestions(@Path("version") String version, @Body JsonObject body);

    /** Full story search results (List_All_Articles-shaped). */
    @POST("app/{version}/Search_Stories")
    Call<EncreptionRequest> Search_Stories(@Path("version") String version, @Body JsonObject body);

    @POST("app/{version}/{langId}/articles")
    Call<EncreptionRequest> List_All_Articles1(@Path("version") String version,@Path("langId") String langid, @Body JsonObject newsss);


    @POST("app/{version}/Add_Article_News")   // get_districts
    Call<EncreptionRequest>Add_Article_News(@Path("version") String version, @Body JsonObject langgg);


    // Edit + resubmit a reporter's (rejected) user post
    @POST("app/{version}/Edit_User_Post")
    Call<EncreptionRequest>Edit_User_Post(@Path("version") String version, @Body JsonObject langgg);


    @POST("app/{version}/get_article_transactions")   // get_districts
    Call<EncreptionRequest>get_article_transactions(@Path("version") String version, @Body JsonObject langgg);



    //Update Like Or DisLike
    @POST("app/{version}/update_follow")                  //  ok
    Call<EncreptionRequest> update_follow(@Path("version") String version, @Body JsonObject shop);


    //Update Like Or DisLike
    @POST("app/{version}/get_follow_list")                  //  ok
    Call<EncreptionRequest> get_follow_list(@Path("version") String version, @Body JsonObject shop);

    /** People who follow this reporter (writer). */
    @POST("app/{version}/list_followers")
    Call<EncreptionRequest> list_followers(@Path("version") String version, @Body JsonObject body);

    @POST("app/{version}/follow_more")   // get_districts
    Call<EncreptionRequest>follow_more(@Path("version") String version, @Body JsonObject langgg);


    //Get Writer Profile
    @POST("app/{version}/get_writer_profile")   // get_districts
    Call<EncreptionRequest> writer_profile(@Path("version") String version, @Body JsonObject langgg );


    @POST("app/{version}/get_user_profile")   // get_districts
    Call<EncreptionRequest> get_user_profile(@Path("version") String version, @Body JsonObject langgg );

    //

    @POST("app/{version}/update_user_name")   // get_districts
    Call<EncreptionRequest> update_user_name(@Path("version") String version, @Body JsonObject langgg );

    // Consolidated reporter/user profile edit (name + email)
    @POST("app/{version}/update_reporter_profile")
    Call<EncreptionRequest> update_reporter_profile(@Path("version") String version, @Body JsonObject profile );





    @POST("app/{version}/get_settings")   // get_districts
    Call<EncreptionRequest>get_settings(@Path("version") String version, @Body JsonObject langgg);

    @POST("app/{version}/update_settings")   // get_districts
    Call<EncreptionRequest>update_settings(@Path("version") String version, @Body JsonObject langgg);

    @POST("app/{version}/ack_notification_reprompt")
    Call<EncreptionRequest>ack_notification_reprompt(@Path("version") String version, @Body JsonObject body);

    @POST("app/{version}/log_notification_permission_ask")
    Call<EncreptionRequest>log_notification_permission_ask(@Path("version") String version, @Body JsonObject body);


    @POST("app/{version}/get_static_data")
    Call<JsonObject> get_static_data(@Path("version") String version, @Body JsonObject langgg);


    @POST("app/{version}/get_login_status")   // get_districts
    Call<LoginDetailesResponse>get_login_status(@Path("version") String version, @Body JsonObject langgg);

    //enc_dec
    @POST("app/{version}/enc_dec")   // get_districts
    Call<EncreptionRequest>enc_dec(@Path("version") String version,@Body JsonObject enc_dec);





    @POST("app/{version}/get_redeem_transactions")   // get_districts
    Call<EncreptionRequest>get_redeem_transactions(@Path("version") String version, @Body JsonObject langgg);

    @POST("app/{version}/redeem_wallet")   // get_districts
    Call<EncreptionRequest>redeem_wallet(@Path("version") String version, @Body JsonObject langgg);

    @POST("app/{version}/get_scratch_cards")   // get_districts
    Call<EncreptionRequest>get_scratch_cards(@Path("version") String version, @Body JsonObject langgg);


    @POST("app/{version}/update_scratch_cards")   // get_districts
    Call<EncreptionRequest>get_update_scratch_cards(@Path("version") String version, @Body JsonObject langgg);

    /** Jobs hub list (+ stats). Cache on device; empty items when none posted. */
    @POST("app/{version}/get_jobs")
    Call<JsonObject> get_jobs(@Path("version") String version, @Body JsonObject body);

    /** Udyogalu — report a job (reason + mobile). */
    @POST("app/{version}/report_job")
    Call<JsonObject> report_job(@Path("version") String version, @Body JsonObject body);

    @POST("app/{version}/reporter_applicant_get")
    Call<JsonObject> reporter_applicant_get(@Path("version") String version, @Body JsonObject body);

    @POST("app/{version}/reporter_applicant_upsert_profile")
    Call<JsonObject> reporter_applicant_upsert_profile(@Path("version") String version, @Body JsonObject body);

    @POST("app/{version}/reporter_applicant_create_post")
    Call<JsonObject> reporter_applicant_create_post(@Path("version") String version, @Body JsonObject body);

    /** Fuel screen — Redis dataset and per-district history. Does not scrape upstream. */
    @GET("app/{version}/fuel_dataset")
    Call<JsonObject> fuelDataset(@Path("version") String version);

    /** TTD screen — Redis darshan + news pack. Does not scrape tirumala.org. */
    @GET("app/{version}/ttd_dataset")
    Call<JsonObject> ttdDataset(
            @Path("version") String version,
            @Query("language_id") int languageId);

    /** Stock Market — Yahoo via server Redis/memory cache. section=indices|watchlist|quote|search */
    @GET("app/{version}/stock_market")
    Call<JsonObject> stockMarket(
            @Path("version") String version,
            @Query("section") String section,
            @Query("q") String query,
            @Query("symbols") String symbols);

    @POST("app/{version}/disable_update_menu")     // @POST("app/V1/Splash_Screen")         // ok
    Call<EncreptionRequest> disable_update_menu(@Path("version") String version, @Body JsonObject splash);


    @POST("app/{version}/get_user_posts")   // get_districts
    Call<EncreptionRequest>get_user_posts(@Path("version") String version, @Body JsonObject langgg);


    @POST("app/v3/get_report_issues")
    Call<GetReportIssueResponce> reportissue(@Body JsonObject reportissue);

    @POST("app/v3/add_issue")
    Call<DailogSendRespouse> dailogSend(@Body JsonObject reportissue);

    @POST("app/{version}/event_link")
    Call<EncreptionRequest> EventPageLink(@Path("version") String version , @Body JsonObject draftID);

    //Survey Response
    @POST("app/{version}/survey_response")                  //  ok
    Call<EncreptionRequest> survey_response(@Path("version") String version, @Body JsonObject surveyResponse);

    @POST("app/{version}/views")
    Call<JsonObject> get_views(@Path("version") String version, @Body JsonObject pincode);

    /** Batched view opens + dwell segments (Android ViewsBatchTracker). */
    @POST("app/{version}/views/batch")
    Call<JsonObject> post_views_batch(@Path("version") String version, @Body JsonObject payload);

    /** Soft-accounting: public OCI / object media opens (MediaAccessReporter). */
    @POST("app/{version}/report_media_access")
    Call<JsonObject> report_media_access(@Path("version") String version, @Body JsonObject payload);

    /** Batched real native-ad impressions (AdImpressionReporter) — no per-ad calls. */
    @POST("app/{version}/report_ad_impressions")
    Call<JsonObject> report_ad_impressions(@Path("version") String version, @Body JsonObject payload);

    /** AnyNews Coins / Rewards (master flag gated server-side). */
    @POST("app/{version}/rewards_hub")
    Call<JsonObject> rewards_hub(@Path("version") String version, @Body JsonObject body);

    @POST("app/{version}/rewards_leaderboard")
    Call<JsonObject> rewards_leaderboard(@Path("version") String version, @Body JsonObject body);

    @POST("app/{version}/rewards_events_batch")
    Call<JsonObject> rewards_events_batch(@Path("version") String version, @Body JsonObject body);

    @POST("app/{version}/rewards_join")
    Call<JsonObject> rewards_join(@Path("version") String version, @Body JsonObject body);

    @POST("app/{version}/rewards_update_upi")
    Call<JsonObject> rewards_update_upi(@Path("version") String version, @Body JsonObject body);

    @POST("app/{version}/rewards_withdraw")
    Call<JsonObject> rewards_withdraw(@Path("version") String version, @Body JsonObject body);

    @POST("app/{version}/rewards_convert")
    Call<JsonObject> rewards_convert(@Path("version") String version, @Body JsonObject body);

    @POST("app/{version}/rewards_history")
    Call<JsonObject> rewards_history(@Path("version") String version, @Body JsonObject body);

    @POST("app/{version}/rewards_withdrawal_proof")
    Call<JsonObject> rewards_withdrawal_proof(
            @Path("version") String version, @Body JsonObject body);

    @POST("app/{version}/rewards_checkin")
    Call<JsonObject> rewards_checkin(@Path("version") String version, @Body JsonObject body);

    @POST("app/{version}/rewards_watch_ads_claim")
    Call<JsonObject> rewards_watch_ads_claim(@Path("version") String version, @Body JsonObject body);

    @POST("app/{version}/rewards_rating_submit")
    Call<JsonObject> rewards_rating_submit(@Path("version") String version, @Body JsonObject body);

    @POST("app/{version}/rewards_earnings_status")
    Call<JsonObject> rewards_earnings_status(@Path("version") String version, @Body JsonObject body);

    /** Settings Rewards switch. Omit rewards_enabled to read; send boolean to save. */
    @POST("app/{version}/rewards_set_enabled")
    Call<JsonObject> rewards_set_enabled(@Path("version") String version, @Body JsonObject body);

    /** Advertise on AnyNews — offer banner + lead chat (no continuous polling). */
    @POST("app/{version}/advertise_offer")
    Call<JsonObject> advertise_offer(@Path("version") String version, @Body JsonObject body);

    @POST("app/{version}/advertise_start")
    Call<JsonObject> advertise_start(@Path("version") String version, @Body JsonObject body);

    @POST("app/{version}/advertise_messages")
    Call<JsonObject> advertise_messages(@Path("version") String version, @Body JsonObject body);

    @POST("app/{version}/advertise_send")
    Call<JsonObject> advertise_send(@Path("version") String version, @Body JsonObject body);

    /** Automatic Birthday Special — config + schedule (logged-in). */
    @POST("app/{version}/birthday_config")
    Call<JsonObject> birthday_config(@Path("version") String version, @Body JsonObject body);

    @POST("app/{version}/birthday_schedule")
    Call<JsonObject> birthday_schedule(@Path("version") String version, @Body JsonObject body);

    @POST("app/{version}/birthday_preview")
    Call<JsonObject> birthday_preview(@Path("version") String version, @Body JsonObject body);

    @POST("app/{version}/birthday_my")
    Call<JsonObject> birthday_my(@Path("version") String version, @Body JsonObject body);

    @POST("app/{version}/birthday_update")
    Call<JsonObject> birthday_update(@Path("version") String version, @Body JsonObject body);

    @POST("app/{version}/birthday_cancel")
    Call<JsonObject> birthday_cancel(@Path("version") String version, @Body JsonObject body);

    /** General media upload (image/video/voice) → OCI URL in extras.url. */
    @Multipart
    @POST("upload/Upload_File")
    Call<JsonObject> uploadFile(@Part MultipartBody.Part body);

    @POST("app/{version}/session")
    Call<JsonObject> post_session(@Path("version") String version, @Body JsonObject session);

    /** Isolated, OTP-session-bound referral programme endpoints. */
    @POST("app/v3/referral/integrity-nonce")
    Call<JsonObject> referralIntegrityNonce(@Header("x-referral-session") String referralSession, @Body JsonObject body);

    @POST("app/v3/referral/claim-install")
    Call<JsonObject> referralClaimInstall(@Header("x-referral-session") String referralSession, @Body JsonObject body);

    @POST("app/v3/referral/create-link")
    Call<JsonObject> referralCreateLink(@Header("x-referral-session") String referralSession, @Body JsonObject body);

    @POST("app/{version}/regions")   // get_districts
    Call<EncreptionRequest>get_regions_requiest(@Path("version") String version, @Body JsonObject langgg);

    @POST("app/{version}/onboarding/region")   // get_districts
    Call<EncreptionRequest>get_onboarding_regions_requiest(@Path("version") String version, @Body JsonObject langgg);

    @POST("app/{version}/survey/poll")                  //  ok
    Call<JsonObject> update_survey_poll(@Path("version") String version, @Body JsonObject shop);

    @POST("app/{version}/ratings")
    Call<JsonObject> submitRating(@Path("version") String version, @Body JsonObject shop);

 @POST("app/{version}/delete-account")
    Call<JsonObject> deleteUserAccount(@Path("version") String version, @Body JsonObject shop);

 @POST("app/{version}/events")
    Call<BaseResponse> sendEvents(@Path("version") String version, @Body JsonObject shop);

}
