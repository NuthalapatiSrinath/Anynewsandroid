package com.anyNews.anynews.Services;

import com.anyNews.anynews.Pojo.AddArticleResponse;
import com.anyNews.anynews.Pojo.AddBookmarkResponse;
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
import com.anyNews.anynews.Pojo.ListOfBookmarksResponse;
import com.anyNews.anynews.Pojo.ListOfCommentsResponse;
import com.anyNews.anynews.Pojo.ListOfUserArticlesResponse;
import com.anyNews.anynews.Pojo.LoginDetailesResponse;
import com.anyNews.anynews.Pojo.NotificationAllNotificationsResponse;
import com.anyNews.anynews.Pojo.RemoveBookmarkResponse;
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
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface APIService {


    String name="";
    //SplashScreen
    @POST("app/{version}/Splash_Screen")     // @POST("app/V1/Splash_Screen")         // ok
    Call<StartActivityReqModel> Splash(@Path("version") String version, @Body JsonObject splash);

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
    //Bookmarks
    @POST("app/List_User_Bookmarks")
    Call<ListOfBookmarksResponse> bookmarkslist(@Body JsonObject remove);
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



    //Bookmark
    @POST("app/Add_Bookmark")
    Call<AddBookmarkResponse> mark(@Body JsonObject markss);


    //RemoveBookmark
    @POST("app/Remove_Bookmark")
    Call<RemoveBookmarkResponse> remove(@Body JsonObject removed);

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


    //Add_Comment
    @POST("app/{version}/Add_Comment")
    Call<EncreptionRequest> comment(@Path("version") String version, @Body JsonObject comments);



    //Update Like Or DisLike
    @POST("app/{version}/update_like")                  //  ok
    Call<EncreptionRequest> update_like(@Path("version") String version, @Body JsonObject shop);


    //Shops
    @POST("app/{version}/update_post_count")                  //  ok
    Call<EncreptionRequest> update_post_count(@Path("version") String version, @Body JsonObject shop);


    //Categories
    @POST("app/{version}/List_All_categories")
    Call<EncreptionRequest>List_All_Categories(@Path("version") String version, @Body JsonObject catssss);



    @POST("app/{version}/List_All_Articles")
    Call<EncreptionRequest> List_All_Articles(@Path("version") String version, @Body JsonObject newsss);

    @POST("app/{version}/{langId}/articles")
    Call<EncreptionRequest> List_All_Articles1(@Path("version") String version,@Path("langId") String langid, @Body JsonObject newsss);


    @POST("app/{version}/Add_Article_News")   // get_districts
    Call<EncreptionRequest>Add_Article_News(@Path("version") String version, @Body JsonObject langgg);


    @POST("app/{version}/get_article_transactions")   // get_districts
    Call<EncreptionRequest>get_article_transactions(@Path("version") String version, @Body JsonObject langgg);



    //Update Like Or DisLike
    @POST("app/{version}/update_follow")                  //  ok
    Call<EncreptionRequest> update_follow(@Path("version") String version, @Body JsonObject shop);


    //Update Like Or DisLike
    @POST("app/{version}/get_follow_list")                  //  ok
    Call<EncreptionRequest> get_follow_list(@Path("version") String version, @Body JsonObject shop);

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





    @POST("app/{version}/get_settings")   // get_districts
    Call<EncreptionRequest>get_settings(@Path("version") String version, @Body JsonObject langgg);

    @POST("app/{version}/update_settings")   // get_districts
    Call<EncreptionRequest>update_settings(@Path("version") String version, @Body JsonObject langgg);


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

}
