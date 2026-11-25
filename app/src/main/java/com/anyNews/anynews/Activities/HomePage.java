package com.anyNews.anynews.Activities;

import static com.anyNews.anynews.App.getContext;
import static com.anyNews.anynews.Utils.AppHelper.showLog;
import static com.anyNews.anynews.Utils.Constants.button_name_category;
import static com.anyNews.anynews.Utils.Constants.button_name_home;
import static com.anyNews.anynews.Utils.Constants.button_name_local;
import static com.anyNews.anynews.Utils.Constants.button_name_myfeed;
import static com.anyNews.anynews.Utils.Constants.button_name_post;
import static com.anyNews.anynews.Utils.Constants.button_name_profile;
import static com.anyNews.anynews.Utils.Constants.type_firebase_button;
import static com.anyNews.anynews.Utils.MyUtils.decrypt;
import static com.anyNews.anynews.Utils.MyUtils.encrypt;

import android.Manifest;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.widget.ImageViewCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.percentlayout.widget.PercentRelativeLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anyNews.anynews.Adapters.MailAdapter;
import com.anyNews.anynews.BuildConfig;
import com.anyNews.anynews.FCMTrackingEvents;
import com.anyNews.anynews.Pojo.AdsPojo;
import com.anyNews.anynews.Pojo.CommentResponuse;
import com.anyNews.anynews.Pojo.DialogSendRequest;
import com.anyNews.anynews.Pojo.DilogeResest;
import com.anyNews.anynews.Pojo.EncreptionRequest;
import com.anyNews.anynews.Pojo.EncritonRequestData;
import com.anyNews.anynews.Pojo.FlipGoogleNativeAds;
import com.anyNews.anynews.Pojo.LanguageModelResponse;
import com.anyNews.anynews.Pojo.LanguageSelectionRequest;
import com.anyNews.anynews.Pojo.LikeDownloadRespouse;
import com.anyNews.anynews.Pojo.LikeSelectionRequest;
import com.anyNews.anynews.Pojo.ListAllArticlesRequest;
import com.anyNews.anynews.Pojo.ListOfCommentsRequest;
import com.anyNews.anynews.Pojo.ListOfCommentsResponse;
import com.anyNews.anynews.Pojo.ListOfUserArticlesRequest;
import com.anyNews.anynews.Pojo.SurveyFeedbackResponse;
import com.anyNews.anynews.Pojo.SurveyRequestFeedback;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.MyFirebaseMessagingService;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Ui.HomeFeedRevealView;
import com.anyNews.anynews.Ui.NeomorphFrameLayout;
import com.anyNews.anynews.Utils.AdScreenArticleOverlay;
import com.anyNews.anynews.Utils.Allsharedpreference;
import com.anyNews.anynews.Utils.ArticleTtsHelper;
import com.anyNews.anynews.Utils.CommonMethods;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.FeedAdConfig;
import com.anyNews.anynews.Utils.FeedEndHelper;
import com.anyNews.anynews.Utils.FirebaseAnalyticsJSON;
import com.anyNews.anynews.Utils.FlipAdWorkGate;
import com.anyNews.anynews.Utils.FlipperAdapter;
import com.anyNews.anynews.Utils.FooterStyleHelper;
import com.anyNews.anynews.Utils.FooterTabLabels;
import com.anyNews.anynews.Utils.LanguageFeedInvalidator;
import com.anyNews.anynews.Utils.Logone;
import com.anyNews.anynews.Utils.ModelClass;
import com.anyNews.anynews.Utils.OpenPathCache;
import com.anyNews.anynews.Utils.OpenPathSplashConfigRefresher;
import com.anyNews.anynews.Utils.EdgeOriginCallWatch;
import com.anyNews.anynews.Utils.PublicEdgeFeedHelper;
import com.anyNews.anynews.Utils.EdgeFeedPostNormalizer;
import com.anyNews.anynews.Utils.ReadArticleStore;
import com.anyNews.anynews.Pojo.PublicEdgeFeedResponse;
import com.anyNews.anynews.Utils.MovementMethodTV;
import com.anyNews.anynews.Utils.PlayInAppUpdateHelper;
import com.anyNews.anynews.Utils.ProfileHubConfigHelper;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.anyNews.anynews.Utils.PostDwellTracker;
import com.anyNews.anynews.Utils.ViewsBatchTracker;
import com.anyNews.anynews.flipview.FlipView;
import com.anyNews.anynews.flipview.OverFlipMode;
import com.anyNews.anynews.fragments.CommentsBottomSheetDialog;
import com.anyNews.anynews.interfaces.CallShareListener;
import com.anyNews.anynews.interfaces.SharePostToWhatsApp;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.NativeAdBase;
import com.facebook.ads.NativeAdListener;
import com.facebook.appevents.AppEventsLogger;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.ResponseInfo;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.common.AccountPicker;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.ExplainReasonCallback;
import com.permissionx.guolindev.callback.ForwardToSettingsCallback;
import com.permissionx.guolindev.callback.RequestCallback;
import com.permissionx.guolindev.request.ExplainScope;
import com.permissionx.guolindev.request.ForwardScope;
import com.pierfrancescosoffritti.youtubeplayer.player.YouTubePlayerFullScreenListener;
import com.pierfrancescosoffritti.youtubeplayer.player.YouTubePlayerView;

import org.json.JSONObject;

import java.io.File;
import java.security.Permission;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomePage extends AppCompatActivity implements FlipperAdapter.Callbacks, FlipView.OnFlipListener,FlipView.OnFlipCompleteListener ,FlipView.OnOverFlipListener, FlipperAdapter.AdapterCallback, CommentsBottomSheetDialog.ItemClickListener, FlipView.OnFlipAppeared {


    private static final String TAG = "HomePage :";

    /** Open this footer tab after CLEAR_TOP from Profile / login (menu item id). */
    public static final String EXTRA_FOOTER_TAB = "extra_footer_tab";
    /** Optional display name for a Profile/Search category open. */
    public static final String EXTRA_CATEGORY_NAME = "EXTRA_CATEGORY_NAME";
    /** In-app Search → FlipView keyword mode. */
    public static final String EXTRA_SEARCH_QUERY = "extra_search_query";
    public static final String EXTRA_SEARCH_POST_ID = "extra_search_post_id";
    public static final String EXTRA_SEARCH_SINGLE_ONLY = "extra_search_single_only";
    public static final String EXTRA_SEARCH_STORY = "extra_search_story";
    public static final String EXTRA_SEARCH_STORY_COUNT = "extra_search_story_count";

    LinearLayout floating_action_button, search_icon;
    NeomorphFrameLayout neo_profile, neo_post_artical, neo_home, neo_inner_home, neo_inner_profile;
    long mBackPressed;
    SharePostToWhatsApp sharePostToWhatsApp;

    private float x1, x2;
    static final int MIN_DISTANCE = 0;

    // FloatingActionButton fab1,fab2,fab4;
    ImageView fab1, fab2, fab4;
    Animation fab_open, fab_close, fab_forword, fab_reverse;
    boolean isOpen = false;

    /* flipView Detailes*/
    private FlipView mFlipView;
   private  FlipView currentView;
    private FlipperAdapter adapter;
    ArrayList<ModelClass.Datum> list;
    ArrayList<ModelClass.Datum> dummylist=new ArrayList<>();
    Response<EncreptionRequest> dummyResponse;
    int ppi=0;
    String post_type="";


    /* comment scrren */
    RelativeLayout comment_page;
    ImageButton send_btn_layout;
    ImageView goHomePage;

    LinearLayoutManager comment_linearlayoutmanager;
    RecyclerView comment_recycler_view;
    List<ListOfCommentsResponse.Datum> comment_chooseLanguage_models;

    ProgressBar comment_progress;
    TextView comment_totoal_count,textViewCommentsTag;
    ImageView commet_close;
    EditText leave_comment_white;
    LinearLayout linear_no_comment_page;
    RelativeLayout layoutCommentProgressbar;
    public  boolean adImpress=false;
    public  String adError="";

    static RelativeLayout no_internet, yes_internet,googleAds;

    private static final String LOG_TAG = "CheckNetworkStatus";
    private NetworkChangeReceiver receiver;
    private boolean isConnected = false;
    private boolean isApiRequested = false;
    int positionx=0,requestPostition=1;
    int requestSize=0;
    /** Trigger next public-feed GET when this many cards from the end (prod ~5). */
    private static final int FEED_PREFETCH_FROM_END = 5;
    boolean isFromLocalArticles=false;
    FCMTrackingEvents trackingEvents;
    public static boolean isPlayingVideoInFullScreen=false;
    public  boolean isDataParsed=true;


    // video view detaiels list
    ProgressBar progress_bar_for_video;
    ImageView thaumbnail_for_video;
    LinearLayout video_side_menu, bottom_layout_detailes, comment_for_video, mute_for_video;
    LinearLayout like_for_video, dislike_for_video, share_for_video;
    TextView username_for_video;
    SeekBar simpleSeekBar;
    ImageView play_and_pause_button_for_video, ic_play_and_pause_button_for_video;
    TextView play_and_pause_text_video, time_for_video;
    LinearLayout linear_play_and_pause_details;
    ImageView mute_video_player_image;
    TextView mute_video_player_text;
    CountDownTimer countDownTimer;
    private PlayerView simpleExoPlayerView;
    private @Nullable
    ExoPlayer player;
    Runnable onEverySecond;
    CircleImageView full_video_user_pic;

    // server with out and with data detailes
    LinearLayout layout_with_out_data, layout_with_data;
    /** Article skeleton + L→R shimmer inside {@link #layout_with_out_data}. */
    HomeFeedRevealView homeFeedReveal;

    // server skip values list
    long last_post_id = 0;
    long skip = 0;
    long actual_skip = 0;

    // comment post id
    String SHARE_REDIRECT_ID = "", TITLE_REWARDS = "", pushUpdatedID,DATAFROM2G;
    long SHARE_ID;

    // local distic name
    TextView local_distic_name, selelcted_local_distic_name;
    LinearLayout selected_local, un_selected_local, un_selected_my_feed, selected_my_feed;

    // Title click events
    TextView half_content_title, tamil_half_content_title, english_half_content_title,
            english_half_content_description, tamil_half_content_description, half_content_description, strip_text_name;
    TextView writer_time, writer_designation, writer_name;


    int category_id = 0;
    private Activity Activity;


    /*   Video*/
    ImageView like_for_video_image, dislike_for_video_image;
    LinearLayout half_image_like_icon;
    ImageView half_image_like, full_video_varified_btn;


    TextView test_folloew, nodatascreen, half_image_my_feed;
    LinearLayout writer_follow, writer_follower, writer_profile_image, relative_half_content, half_comment, report, share_and_earn_half;

    //normal youtube video
    LinearLayout half_image_like_iconytd, layoutParentMiddle,layoutParentReportDetails,layoutParentTitle,with_strip_layoutytd, with_out_strip_layoutytd, half_image_shareytd,
            writer_followytd, writer_followerytd, writer_profile_imageytd, relative_half_contentytd, half_commentytd, reportytd;
    TextView half_content_titleytd, tamil_half_content_titleytd, english_half_content_titleytd, test_followytd;
    ImageView half_image_likeytd, imageViewItems, btnMuteHalf;
    ConstraintLayout layoutParentTop;

    //com.pierfrancescosoffritti.youtubeplayer.player.YouTubePlayerView youTubePlayerViewHalf , youTubePlayerViewFull;
    DisplayMetrics displayMetrics = new DisplayMetrics();


    /*  full image view detailes */
    LinearLayout full_image_like, full_image_comment, full_image_whatsapp, full_image_share, layout_strip_iconnew, side_menu, full_image_main;
    ImageView full_image_selected_like, varified_btn;
    LinearLayout full_image_view_details;
    CircleImageView user_pic;


    /* static data details */
    LanguageModelResponse languageModelResponse;
    Allsharedpreference allsharedpreference;

    TextView my_feed, my_feed_text;
    TextView fab_article, fab_video, fab_fullpost;
    LinearLayout txtNoData;

    LinearLayout top_item_layout, with_strip_layout, with_out_strip_layout;
    LinearLayout half_image_share;

    int TITLE_VALUE = 0;
    LinearLayout data_finding_value;
    PercentRelativeLayout post_news_scrren;
    // bottom navig

    String navigation_home_text = "Home";
    BottomNavigationView bottomNavigationView;
    FloatingActionButton floatingActionButtonHome;

    /** Soft footer / Daily Updates (lean ports — no fat flip settle). */
    private boolean suppressBottomNavCallbacks = false;
    private boolean dailyUpdatesModeActive = false;
    @Nullable private FrameLayout homeDailyUpdatesHost;
    @Nullable private DailyUpdatesController dailyUpdatesController;

    /**
     * Special-tab blink (Play Store parity). Overlay must stay on
     * {@code navigation_catagiries} — Material LIFO pool can otherwise park it on
     * Daily Updates. Cleared only for this process after Special is opened.
     */
    private static boolean specialTabViewedThisSession = false;
    @Nullable private ImageView specialTabBulbOverlay;
    @Nullable private Animator specialTabBulbAnimator;
    private int specialTabBulbSetupRetryCount;
    private boolean centerFooterSpacerPrepared = false;
    @Nullable private View layoutMagazineShareFooter;
    @Nullable private ImageView ivMagazineShareFooter;
    @Nullable private TextView tvMagazineShareFooter;

    /** Cancel / generation guard for public edge GET or List_All_Articles (warm push N2). */
    @Nullable private Call<?> listAllArticlesCall;
    private int listAllArticlesGen = 0;

    /** Embedded Telugu calendar / Raasi (from Daily Updates). */
    private boolean calendarModeActive = false;
    private boolean calendarFromDailyUpdates = false;
    @Nullable private FrameLayout homeCalendarHost;
    @Nullable private TeluguCalendarController calendarController;

    /** Thin Flash News (push_posts) mode — no fat video-tab settle. */
    private boolean flashNewsModeActive = false;
    private boolean localFeedFromDailyUpdates = false;

    /** Ad-screen recommend tap — temp surface; does not splice main FlipView. */
    @Nullable private FrameLayout homeAdArticleOverlayHost;
    @Nullable private AdScreenArticleOverlay adScreenArticleOverlay;
    @Nullable private View localFeedChromeRoot;
    @Nullable private TextView localFeedContextLabel;
    @Nullable private View localFeedBackBtn;

    @Nullable private PlayInAppUpdateHelper playInAppUpdateHelper;

    //Survey
    NeomorphFrameLayout neomorphFrameLayoutYes, neomorphFrameLayoutNo, neomorphFrameLayoutYesSelected, neomorphFrameLayoutNoSelected, neomorphFrameLayoutYesPercent, neomorphFrameLayoutNoPercent;
    RelativeLayout relativeLayoutSurvayMain;
    LinearLayout llPercentLayeout;

    private TextView tvSurveyQuestionEnglish, tvSurveyQuestionTamil, tvSurveyQuestionCommon, tvAnswerYes, tvAnswerNo, tvYesPercent, tvNoPercent;

    private String responseSurvey = "", surveyPercent = "";
    private long postIDSurvey;

    FirebaseMessaging firebaseMessaging;
    //Polling
    LinearLayout llPercentLayout;
       LinearLayout     layoutProgressbar;
    NeomorphFrameLayout neomorphFrameLayoutA, neomorphFrameLayoutB, neomorphFrameLayoutC,
            neomorphFrameLayoutD;
    NeomorphFrameLayout select_neomorphFrameLayoutA, select_neomorphFrameLayoutB, select_neomorphFrameLayoutC,
            select_neomorphFrameLayoutD;

    ImageView polling_half_image;
    RelativeLayout relativeLayoutSurveyMain;
    TextView polling_A, polling_B, polling_C, polling_D, tv_answer_A, tv_answer_B, tv_answer_C, tv_answer_D;
    TextView select_polling_A, select_polling_B, select_polling_C, select_polling_D, select_tv_answer_A, select_tv_answer_B, select_tv_answer_C, select_tv_answer_D;

    private TextView tv_polling_question;


    SurveyFeedbackResponse surveyFeedbackResponse;
    String percentageYes = "", percentageNo = "";
    com.pierfrancescosoffritti.youtubeplayer.player.YouTubePlayerView youTubePlayerViewHalf;
    String videoUrl="";
    int seekTO=0;
    EncreptionRequest encreptionRequest__;


    private String descriptionText = "", descriptionHash = "", descriptionDirectionLink = "";

    CommonMethods commonMethods;
    Activity activity;

    // full youtube video
    ImageView newFullImageView1, full_video_varified_btn_ytb, like_for_video_image_ytb, dislike_for_video_image_ytb, btnMuteFull;
    YouTubePlayerView youTubePlayerView1;
    ImageView imageVIewShareWhatsapp;
    YouTubePlayerFullScreenListener youTubePlayerFullScreenListener;
    CircleImageView full_video_user_pic_ytb;
    LinearLayout like_for_video_ytb, dislike_for_video_ytb, share_for_video_ytb, comment_for_video_ytb;

    // firebase storage
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();
    HashMap<String, Object> hashMapUser;
    JsonObject firbaseJSONObj;
    CoordinatorLayout home_page_navigation;

    int draft_id_firebase = 0;

    int language_id_firebase = 0;
    String device_id_firebase = "";
    int updated_post_id_firebase = 0;
    long current_time_firebase = System.currentTimeMillis();
    boolean isMutedHalf = false;
    boolean isMutedFull = false;
    String type;
    float percent = 0.5f;
    private boolean fullscreen=false;
    static  public List<FlipGoogleNativeAds> nativeGoogleAdList =new ArrayList<>();
    static  public List<FlipGoogleNativeAds> nativeFBAdList =new ArrayList<>();
    static  public List<Integer> nativeGoogleAdArray =new ArrayList<>();
    static  public List<Integer> nativeFBAdArray =new ArrayList<>();
    public List<AdsPojo> adsPojoList =new ArrayList<>();
    public List<Integer> adsReqPosList =new ArrayList<>();
    static  int adIndex=0;
    private boolean showReporterLogin=false;
    public boolean initialCall=false;
    public String no_article_message="Please wait while loading ...";

    /**
     * Way2-style: while the user stays on an ads_screen seat, request a fresh
     * native every {@link #AD_SEAT_REFRESH_MS}. Cancelled on leave/pause.
     */
    private static final long AD_SEAT_REFRESH_MS = 30_000L;
    private final Handler adSeatRefreshHandler = new Handler(Looper.getMainLooper());
    private final Runnable adSeatRefreshRunnable = this::onAdSeatStayRefreshTick;
    /** At most one AdLoader in flight per feed {@code ad_req_position}. */
    private final HashSet<Integer> feedNativeAdSeatsInFlight = new HashSet<>();
    private boolean homeLifecycleResumed = false;
    private int adSeatRefreshPage = -1;
    private int adSeatRefreshReqPos = -1;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        MobileAds.initialize(this, initializationStatus -> {
        });
//        RequestConfiguration.Builder requestConfiguration=new RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("CEB66515B6C9B2ECD92786955DF252CD"));
//        MobileAds.setRequestConfiguration(requestConfiguration.build());

        setContentView(R.layout.activity_home_page);
        allsharedpreference = new Allsharedpreference(HomePage.this);
        Activity=this;
        activity=this;
        context=this;
        trackingEvents=new FCMTrackingEvents(this);
        fab_article = findViewById(R.id.fab_article);
        fab_video = findViewById(R.id.fab_video);
        fab_fullpost = findViewById(R.id.fab_fullpost);
        floatingActionButtonHome = findViewById(R.id.fab);

        selected_local = findViewById(R.id.selected_local);
        un_selected_local = findViewById(R.id.un_selected_local);
        un_selected_my_feed = findViewById(R.id.un_selected_my_feed);
        selected_my_feed = findViewById(R.id.selected_my_feed);
        txtNoData = findViewById(R.id.txtNoData);
        data_finding_value = findViewById(R.id.data_finding_value);

        post_news_scrren = findViewById(R.id.post_news_scrren);
        // Do not wipe RTDB root on every Home create (cold/push stampede). Events
        // still push under child("Events"); the twin call below was already disabled.
        // myRef.removeValue();
        linear_no_comment_page = findViewById(R.id.linear_no_comment_page);

        top_item_layout = findViewById(R.id.top_item_layout);

        my_feed = findViewById(R.id.my_feed);
        my_feed_text = findViewById(R.id.my_feed_text);

        mFlipView = findViewById(R.id.flip_view);
        local_distic_name = findViewById(R.id.local_distic_name);
        selelcted_local_distic_name = findViewById(R.id.selelcted_local_distic_name);
        /*post_artical_text = findViewById(R.id.post_artical_text);*/
        nodatascreen = findViewById(R.id.nodatascreen);
        home_page_navigation = findViewById(R.id.home_page_navigation);
        home_page_navigation.setVisibility(View.GONE);
        imageVIewShareWhatsapp = findViewById(R.id.imageVIewShareWhatsapp);
        layoutMagazineShareFooter = findViewById(R.id.layoutMagazineShareFooter);
        ivMagazineShareFooter = findViewById(R.id.ivMagazineShareFooter);
        tvMagazineShareFooter = findViewById(R.id.tvMagazineShareFooter);



        commonMethods = new CommonMethods(activity);
        //player = new SimpleExoPlayer.Builder(getApplicationContext()).build();
        AudioManager audio = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

        incomment();

        //inpostarticales();

        ininointernet();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);


        bottomNavigationView = findViewById(R.id.nav_view);


        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.parseColor("#0D1528"));

        //
        /*search_icon = findViewById(R.id.search_icon);*/
        floating_action_button = findViewById(R.id.floating_action_button);
        //new_bottom_nav_view = findViewById(R.id.new_bottom_nav_view);

        // layout with data detailes
        layout_with_data = findViewById(R.id.layout_with_data);
        layout_with_out_data = findViewById(R.id.layout_with_out_data);
        homeFeedReveal = findViewById(R.id.home_feed_reveal);
        homeDailyUpdatesHost = findViewById(R.id.home_daily_updates_host);
        if (homeDailyUpdatesHost != null) {
            homeDailyUpdatesHost.setVisibility(View.GONE);
        }
        homeCalendarHost = findViewById(R.id.home_calendar_host);
        if (homeCalendarHost != null) {
            homeCalendarHost.setVisibility(View.GONE);
        }
        homeAdArticleOverlayHost = findViewById(R.id.home_ad_article_overlay_host);
        if (homeAdArticleOverlayHost != null) {
            homeAdArticleOverlayHost.setVisibility(View.GONE);
            adScreenArticleOverlay =
                    new AdScreenArticleOverlay(HomePage.this, homeAdArticleOverlayHost);
        }
        try {
            setupLocalFeedChromeThin();
        } catch (Exception ignored) {
        }
        try {
            ensurePlayInAppUpdateHelper();
        } catch (Exception ignored) {
        }

        // Play-style skeleton shimmer until first inlist bind.
        showHomeFeedLoadingReveal();
        layout_with_data.setVisibility(View.GONE);

        firebaseMessaging = FirebaseMessaging.getInstance();

        //  Title click Events


        // facebook integration
//        FacebookSdk.setIsDebugEnabled(true);
//        FacebookSdk.addLoggingBehavior(LoggingBehavior.APP_EVENTS);
//        setAutoLogAppEventsEnabled(true);
//        FacebookSdk.setAutoInitEnabled(true);
//        FacebookSdk.fullyInitialize();
//        setAdvertiserIDCollectionEnabled(true);
//        logSentFriendRequestEvent();


        String sessionId = "" + getIntent().getStringExtra("EXTRA_SESSION_ID");
        pushUpdatedID = "" + getIntent().getStringExtra("TITLE");
        SHARE_REDIRECT_ID = "" + getIntent().getStringExtra("SHARE_REDIRECT_ID");
        TITLE_REWARDS = "" + getIntent().getStringExtra("TITLE_REWARDS");
        DATAFROM2G = "" + getIntent().getStringExtra("DATAFROM2G");
        showLog("SHARE_REDIRECT_ID::" + SHARE_REDIRECT_ID);
        if (SHARE_REDIRECT_ID == null || SHARE_REDIRECT_ID.equals("null") || SHARE_REDIRECT_ID.trim().isEmpty()) {
            SHARE_ID = 0;
        } else {
            try {
                SHARE_ID = Long.parseLong(SHARE_REDIRECT_ID.trim());
            } catch (NumberFormatException e) {
                SHARE_ID = 0;
            }
        }
        if (getIntent().hasExtra("from")) {
            showLog("isUnreadPostsSelected set to true #7");
            if (getIntent().getStringExtra("from").equalsIgnoreCase("special")) {
                showLog("isUnreadPostsSelected set to true #8");
                SharedPrefsData.putBool(HomePage.this, Constants.isUnreadPostsSelected, true, Constants.PREF_NAME);
            } else {
                SharedPrefsData.putBool(HomePage.this, Constants.isUnreadPostsSelected, false, Constants.PREF_NAME);
            }
        } else {
            SharedPrefsData.putBool(HomePage.this, Constants.isUnreadPostsSelected, false, Constants.PREF_NAME);
        }

        // if rewards gots then redirect to profile page
        if (TITLE_REWARDS != null && TITLE_REWARDS.contains("rewards")) {

            writeButtonClick("User", type_firebase_button, current_time_firebase, draft_id_firebase, device_id_firebase, button_name_profile);

            Intent intent = new Intent(HomePage.this, newProfileDetails.class);
            if (getApplicationContext() != null && getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)) {
                startActivity(intent);
            }

            finish();
        }
        showLog("ONCREATE CALLED");
        if (sessionId.equals("0") || sessionId.equals("null")) {
            isFromLocalArticles=false;
            showLog("inlist from oncreate 1");
            inlist(0);

        } else {

            showHomeFeedLoadingReveal();
            layout_with_data.setVisibility(View.GONE);


            last_post_id = 0;
            skip = 0;
            actual_skip = 0;
            category_id = Integer.parseInt(sessionId);
            isFromLocalArticles=false;
            showLog("inlist from oncreate 2:"+Integer.parseInt(sessionId));
            inlist(Integer.parseInt(sessionId));

            try {

            } catch (AssertionError e) {
                e.printStackTrace();
            }


            un_selected_local.setVisibility(View.VISIBLE);
            un_selected_my_feed.setVisibility(View.GONE);
            selected_local.setVisibility(View.GONE);
            selected_my_feed.setVisibility(View.VISIBLE);


            showHomeFeedLoadingReveal();
            layout_with_data.setVisibility(View.GONE);
            comment_page.setVisibility(View.GONE);


        }

        local_distic_name.setText("" + String.valueOf(SharedPrefsData.getString(HomePage.this, Constants.Selected_Distric_Name1, Constants.PREF_NAME)));
        selelcted_local_distic_name.setText("" + String.valueOf(SharedPrefsData.getString(HomePage.this, Constants.Selected_Distric_Name1, Constants.PREF_NAME)));


        if (local_distic_name.getText().toString().equals("null")) {
            local_distic_name.setVisibility(View.VISIBLE);
            local_distic_name.setText("" + SharedPrefsData.getString(HomePage.this, Constants.Selected_Distric_Name1, Constants.PREF_NAME));
        } else {
            local_distic_name.setVisibility(View.VISIBLE);
            local_distic_name.setText("" + SharedPrefsData.getString(HomePage.this, Constants.Selected_Distric_Name1, Constants.PREF_NAME));
        }


        fab1 = findViewById(R.id.fab1);
        fab2 = findViewById(R.id.fab2);
        fab4 = findViewById(R.id.fab4);


        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
        fab_forword = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_forward);
        fab_reverse = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_backward);


   //     myRef.removeValue();
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // video post
                Intent intent = new Intent(HomePage.this, PostVideo.class);
                intent.putExtra("POST_VIDEO", "1");
                if (getApplicationContext() != null && getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)) {
                    startActivity(intent);
                }

            }
        });

        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Intent intent = new Intent(HomePage.this, FullPost.class);
                if (getApplicationContext() != null && getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)) {
                    startActivity(intent);
                }
            }
        });

        fab4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomePage.this, PostArticles.class);
                intent.putExtra("POST_VIDEO", "0");
                if (getApplicationContext() != null && getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)) {
                    startActivity(intent);
                }
            }
        });

        LocalBroadcastManager.getInstance(getContext()).registerReceiver(COMMENT_PAGE_ACTIVE,
                new IntentFilter("COMMENT_PAGE_ACTIVE"));


        // half imagview click events
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(COMMENTS_PAGE,
                new IntentFilter("COMMENTS_PAGE"));


        LocalBroadcastManager.getInstance(getContext()).registerReceiver(LIKE_PAGE,
                new IntentFilter("LIKE_PAGE"));


        LocalBroadcastManager.getInstance(getContext()).registerReceiver(CURRENT_LOCATION,
                new IntentFilter("CURRENT_LOCATION"));


        LocalBroadcastManager.getInstance(getContext()).registerReceiver(WRITER_FOLLOW,
                new IntentFilter("WRITER_FOLLOW"));

        LocalBroadcastManager.getInstance(getContext()).registerReceiver(WRITER_FOLLOWER,
                new IntentFilter("WRITER_FOLLOWER"));

        LocalBroadcastManager.getInstance(getContext()).registerReceiver(SURVEY_STATUS,
                new IntentFilter("SURVEY_STATUS"));

        LocalBroadcastManager.getInstance(getContext()).registerReceiver(SURVEY_POLL,
                new IntentFilter("SURVEY_POLL"));

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        receiver = new NetworkChangeReceiver();
        registerReceiver(receiver, filter);


        un_selected_local.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                un_selected_local.setVisibility(View.GONE);
                selected_local.setVisibility(View.VISIBLE);
                un_selected_my_feed.setVisibility(View.VISIBLE);
                selected_my_feed.setVisibility(View.GONE);
                mFlipView.setVisibility(View.VISIBLE);

                showHomeFeedLoadingReveal();
                layout_with_data.setVisibility(View.GONE);
                comment_page.setVisibility(View.GONE);


                last_post_id = 0;
                skip = 0;
                actual_skip = 0;
                category_id = -1;
              //  inlist(-1);

                writeButtonClick("User", type_firebase_button, current_time_firebase, draft_id_firebase, device_id_firebase, button_name_local);

            }
        });

        un_selected_my_feed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                un_selected_local.setVisibility(View.VISIBLE);

                un_selected_my_feed.setVisibility(View.GONE);
                selected_my_feed.setVisibility(View.VISIBLE);
                selected_local.setVisibility(View.GONE);

                showHomeFeedLoadingReveal();
                layout_with_data.setVisibility(View.GONE);
                comment_page.setVisibility(View.GONE);

                last_post_id = 0;
                skip = 0;
                actual_skip = 0;
                category_id = 0;
              //  inlist(0);

                writeButtonClick("User", type_firebase_button, current_time_firebase, draft_id_firebase, device_id_firebase, button_name_myfeed);

            }
        });


        setStaticData();

        // Lean open: StartingActivity stays zero-API; soft-refresh Splash_Screen fonts/admin
        // prefs about once/day so existing users are not stuck on stale FontSettingsHelper.
        // Push/warm re-entry respects the same throttle + process in-flight guard.
        try {
            OpenPathSplashConfigRefresher.maybeRefreshInBackground(this);
        } catch (Exception ignored) {
        }

        // Event_Status seeds process blink; Special glyph stays ic_footer_special
        // (never transparent — that caused label-only Special + misplaced blink).
        specialTabViewedThisSession = !SharedPrefsData.getBool(
                HomePage.this, Constants.Event_Status, Constants.PREF_NAME);

        bottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        // Long-press Daily Updates or Profile footer tab → Origin/CF call log.
        try {
            bottomNavigationView.post(this::wireOriginCallsFooterLongPress);
        } catch (Exception ignored) {
        }
        try {
            applyFooterSlotVisibility();
            applyFooterWhatsapp();
            FooterStyleHelper.apply(this, bottomNavigationView);
            FooterStyleHelper.applyFab(this, floatingActionButtonHome);
            FooterStyleHelper.applyMagazineShareChrome(
                    this,
                    home_page_navigation,
                    bottomNavigationView,
                    layoutMagazineShareFooter,
                    ivMagazineShareFooter);
            if (languageModelResponse != null) {
                FooterTabLabels.apply(this, bottomNavigationView, languageModelResponse.getData());
            }
            // Labels may touch the center slot — re-empty spacer under WhatsApp/Share.
            prepareMagazineShareCenterSlot(
                    bottomNavigationView.getMenu().findItem(R.id.navigation_stricker));
            hideShareCenterSlotChrome();
            ensureSpecialMenuIconAlwaysVisible();
            setupSpecialTabBulb();
            repositionCenterFabForVisibleTabs();
            if (bottomNavigationView != null) {
                bottomNavigationView.post(this::setupSpecialTabBulb);
                bottomNavigationView.postDelayed(this::repositionCenterFabForVisibleTabs, 80);
            }
        } catch (Exception ignored) {
        }
        try {
            handleFooterTabExtra(getIntent());
        } catch (Exception ignored) {
        }
        // firebase store
        hashMapUser = new HashMap<>();
        draft_id_firebase = SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME);
        language_id_firebase = SharedPrefsData.getInt(getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME);
        device_id_firebase = SharedPrefsData.getString(getApplicationContext(), Constants.DeviceID, Constants.PREF_NAME);





    }
    public void showNotificationAlert(boolean showPopup){
        if(showPopup){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                PermissionX.init(HomePage.this)
                        .permissions(Manifest.permission.POST_NOTIFICATIONS)
                        .onExplainRequestReason(new ExplainReasonCallback() {
                            @Override
                            public void onExplainReason(@NonNull ExplainScope scope, @NonNull List<String> deniedList) {
                                scope.showRequestReasonDialog(deniedList, "Please allow notifications and don't miss out on important news", "OK", "Cancel");
                            }
                        })
                        .onForwardToSettings(new ForwardToSettingsCallback() {
                            @Override
                            public void onForwardToSettings(@NonNull ForwardScope scope, @NonNull List<String> deniedList) {
                                scope.showForwardToSettingsDialog(deniedList, "Please allow notifications manually from settings page and don't miss out on important news", "OK", "Cancel");

                            }
                        })
                        .request(new RequestCallback() {
                            @Override
                            public void onResult(boolean allGranted, @NonNull List<String> grantedList, @NonNull List<String> deniedList) {
                                if (allGranted) {

                                } else {

                                }
                            }
                        });

            }
        }



//        if(true){
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
//                    //ActivityCompat.requestPermissions();
//                    String[] permissions = new String[]{Manifest.permission.POST_NOTIFICATIONS};
//                    ActivityCompat.requestPermissions(this, permissions, 289);
//                }
//            }
//        }

    }

    private void sendNotification(String title) {
        RemoteViews collapsedView = new RemoteViews(context.getPackageName(), R.layout.download_video_notification);
        final Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        String GROUP_KEY_WORK_EMAIL = "com.anyNews.anynews.Services.WORK_EMAIL";
        try {
            Intent resultIntent = new Intent(Intent.ACTION_SEND);
            resultIntent.setAction(Intent.ACTION_SEND);
            resultIntent.setType("video/*");
            resultIntent.setPackage("com.whatsapp");;
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), title);
            file.setReadable(true, true);
            Uri uriForFile = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
            showLog("file uri:"+uriForFile.getPath());
            resultIntent.putExtra(Intent.EXTRA_STREAM, uriForFile);
            List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(Intent.createChooser(resultIntent, title), PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                context.grantUriPermission(packageName, uriForFile, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            resultIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            showLog("FLAG_GRANT_READ_URI_PERMISSION:");
            context.startActivity(Intent.createChooser(resultIntent, "Share Link"));
            PendingIntent pendingIntent;
            if (Build.VERSION.SDK_INT >= 31) {
                pendingIntent = PendingIntent.getActivity(
                        getApplicationContext(),
                        10,
                        resultIntent,
                        PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT // setting the mutability flag
                );
            } else {
                pendingIntent = PendingIntent.getActivity(context,
                        Integer.parseInt("10"), resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            }
            try {

                NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), Constants.Channel_ID)
                        // use xxxhdpi
                        .setSmallIcon(R.drawable.ic_arman_notificatio)
                        .setCustomContentView(collapsedView)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setVibrate(new long[]{1000, 1000, 1000, 1000})
                        .setSound(uri)
                        .setShowWhen(true)
                        .setGroup(GROUP_KEY_WORK_EMAIL)
                        .setGroupSummary(false)
                        .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder.setSmallIcon(R.drawable.ic_arman_notificatio);
                    builder.setColor(context.getResources().getColor(R.color.colorPrimary));
                } else {
                    builder.setSmallIcon(R.drawable.ic_arman_notificatio);
                }
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());

                int m = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
                //notificationManager.notify(random.nextInt(), builder.build());
                notificationManager.notify(m, builder.build());
            } catch (Exception e) {
                Log.v("NotificationData", String.valueOf(e.getMessage()));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Flip-safe: skip AdMob bind work while FlipView is mid-gesture / settle grace. */
    public boolean shouldDeferAdWork() {
        try {
            return FlipAdWorkGate.shouldDeferAdWork();
        } catch (Exception e) {
            return false;
        }
    }

    private void loadAds(String ad_code, Integer ad_req_position, String ad_type) {
        loadAds(ad_code, ad_req_position, ad_type, false);
    }

    /**
     * @param replaceExisting when true (stay-on-seat refresh), force a new AdLoader
     *                        even if this reqPos already has a creative; on fail keep
     *                        the previous fill. Always respects the one-in-flight cap.
     */
    private void loadAds(String ad_code, Integer ad_req_position, String ad_type,
                         boolean replaceExisting) {
        if (shouldDeferAdWork()) {
            showLog("loadAds deferred (flip settle)");
            return;
        }
        if (ad_req_position == null) {
            showLog("loadAds skip null reqPos");
            return;
        }
        if (isFeedNativeSeatRequestInFlight(ad_req_position)) {
            showLog("loadAds skip — AdLoader in flight pos=" + ad_req_position);
            return;
        }
        if (ad_type == null) {
            ad_type = "google";
        }
       showLog("Ad loading ad_code 1 :"+ad_code);
       showLog("Ad loading ad_req_position :"+ad_req_position);
        showLog("Ad loading ad_type:"+ad_type);
        if(ad_type.equalsIgnoreCase("google")){
            if (!replaceExisting && isFeedNativeSeatLoaded(ad_req_position)) {
                showLog("loadAds skip — creative already loaded pos=" + ad_req_position);
                return;
            }
            loadGoogleAds(ad_code, ad_req_position, 0, replaceExisting);
        }else{
            if (!replaceExisting
                    && nativeFBAdArray != null
                    && nativeFBAdArray.contains(ad_req_position)) {
                showLog("loadAds skip — fb creative already loaded pos=" + ad_req_position);
                return;
            }
            loadFBAds(ad_code,ad_req_position);
        }

    }

    private boolean isFeedNativeSeatRequestInFlight(int reqPos) {
        synchronized (feedNativeAdSeatsInFlight) {
            return feedNativeAdSeatsInFlight.contains(reqPos);
        }
    }

    private boolean isFeedNativeSeatLoaded(int reqPos) {
        return nativeGoogleAdArray != null && nativeGoogleAdArray.contains(reqPos);
    }

    private void markFeedNativeSeatRequestInFlight(int reqPos) {
        synchronized (feedNativeAdSeatsInFlight) {
            feedNativeAdSeatsInFlight.add(reqPos);
        }
    }

    private void clearFeedNativeSeatRequestInFlight(int reqPos) {
        synchronized (feedNativeAdSeatsInFlight) {
            feedNativeAdSeatsInFlight.remove(reqPos);
        }
    }

    /** Full-bleed ad page: bottom nav GONE on ad seats, restored elsewhere. */
    private void updateHomePageNavigationForSeat() {
        if (home_page_navigation == null || list == null || positionx < 0
                || positionx >= list.size()) {
            return;
        }
        try {
            String pt = list.get(positionx).getPostType();
            if (pt != null
                    && (pt.equalsIgnoreCase("share_app")
                            || pt.equalsIgnoreCase("ratings"))) {
                home_page_navigation.setVisibility(View.GONE);
            } else {
                home_page_navigation.setVisibility(View.VISIBLE);
            }
        } catch (Exception ignored) {
        }
    }

    private void loadGoogleAds(String adCode, Integer ad_req_position, int reqType) {
        loadGoogleAds(adCode, ad_req_position, reqType, false);
    }

    private void loadGoogleAds(String adCode, Integer ad_req_position, int reqType,
                               boolean replaceExisting) {
        if(reqType==0){
            loadAdLoader(adCode, ad_req_position, replaceExisting);
        }else if(reqType==1){
            loadInterstitialAd(adCode,ad_req_position);
        }
        showLog("Ad2 loading");
    }

    private void loadAdLoader(String adCode, Integer ad_req_position) {
        loadAdLoader(adCode, ad_req_position, false);
    }

    private void loadAdLoader(String adCode, Integer ad_req_position, boolean replaceExisting) {
        if (TextUtils.isEmpty(adCode) || ad_req_position == null) {
            showLog("loadAdLoader skip empty unit/pos");
            return;
        }
        if (isFeedNativeSeatRequestInFlight(ad_req_position)) {
            showLog("loadAdLoader skip — request already in flight pos=" + ad_req_position);
            return;
        }
        if (!replaceExisting && isFeedNativeSeatLoaded(ad_req_position)) {
            showLog("loadAdLoader skip — seat already filled pos=" + ad_req_position);
            return;
        }
        if (isFinishing() || isDestroyed()) {
            return;
        }
        markFeedNativeSeatRequestInFlight(ad_req_position);
        showLog("loadAdLoader START pos=" + ad_req_position
                + " replace=" + replaceExisting + " unit=" + adCode);
        AdLoader adLoader = new AdLoader.Builder(context, adCode)
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdClicked() {
                        super.onAdClicked();
                        showLog(TAG+ "onAdClicked: ");
                    }

                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();
                        showLog(TAG+  "onAdClosed: ");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError error) {
                        super.onAdFailedToLoad(error);
                        clearFeedNativeSeatRequestInFlight(ad_req_position);
                        try{

                            String errorDomain = error.getDomain();
                            int errorCode = error.getCode();
                            String errorMessage = error.getMessage();
                            // All of this information is available via the error's toString() method.
                            Log.d("Ads", error.toString());
                            JSONObject object=new JSONObject();
                            object.put("errorDomain",errorDomain);
                            object.put("errorCode",errorCode);
                            object.put("errorMessage",errorMessage);
                            object.put("adCode",adCode);
                            object.put("adPos",ad_req_position);
                            adError=object.toString();
                            showLog("loadAdLoader FAIL pos=" + ad_req_position
                                    + " keep previous/empty shell code=" + errorCode
                                    + " msg=" + errorMessage);
                            // Stay-refresh fail: keep previous creative; do not clear array.

                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onAdImpression() {
                        super.onAdImpression();
                        adImpress=true;
                    }

                    @Override
                    public void onAdLoaded() {
                        super.onAdLoaded();
                        showLog(TAG+  "onAdLoaded: ");
                    }

                    @Override
                    public void onAdOpened() {
                        super.onAdOpened();
                        showLog(TAG+  "onAdOpened: ");
                    }
                })
                .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
                    @Override
                    public void onNativeAdLoaded(@NonNull NativeAd nativeAd) {
                        showLog(TAG+  "onNativeAdLoaded: ");
                        try {
                            if (list != null && list.size() > 0) {
                                storeLoadedGoogleNative(
                                        nativeAd, adCode, ad_req_position, replaceExisting);
                                rebindAdsScreenAfterNativeLoad(ad_req_position);
                            } else {
                                try {
                                    nativeAd.destroy();
                                } catch (Exception ignored) {
                                }
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        } finally {
                            clearFeedNativeSeatRequestInFlight(ad_req_position);
                        }

                    }

                })
                .build();
        try {
            adLoader.loadAd(new AdRequest.Builder().build());
        } catch (Exception e) {
            clearFeedNativeSeatRequestInFlight(ad_req_position);
            showLog("loadAdLoader exception: " + e.getMessage());
        }
    }

    private void storeLoadedGoogleNative(
            @NonNull NativeAd nativeAd,
            String adCode,
            Integer ad_req_position,
            boolean replaceExisting) {
        if (nativeGoogleAdList == null) {
            nativeGoogleAdList = new ArrayList<>();
        }
        if (nativeGoogleAdArray == null) {
            nativeGoogleAdArray = new ArrayList<>();
        }
        NativeAd oldToDestroy = null;
        FlipGoogleNativeAds existing = null;
        for (int i = 0; i < nativeGoogleAdList.size(); i++) {
            FlipGoogleNativeAds packed = nativeGoogleAdList.get(i);
            if (packed != null && packed.getReqPos() == ad_req_position) {
                existing = packed;
                break;
            }
        }
        if (existing != null) {
            oldToDestroy = existing.getAd();
            existing.setAd(nativeAd);
            existing.setAdCode(adCode);
            existing.setClaimed(false);
            existing.setImpressed(false);
        } else {
            FlipGoogleNativeAds flipGoogleNativeAds = new FlipGoogleNativeAds();
            flipGoogleNativeAds.setAd(nativeAd);
            flipGoogleNativeAds.setAdCode(adCode);
            flipGoogleNativeAds.setReqPos(ad_req_position);
            nativeGoogleAdList.add(flipGoogleNativeAds);
        }
        if (!nativeGoogleAdArray.contains(ad_req_position)) {
            nativeGoogleAdArray.add(ad_req_position);
        }
        // Keep feed shell ad_code aligned so FlipperAdapter match (code+reqPos) hits.
        try {
            if (list != null) {
                for (int i = 0; i < list.size(); i++) {
                    ModelClass.Datum d = list.get(i);
                    if (d == null || d.getPostType() == null
                            || !d.getPostType().equalsIgnoreCase("ad")) {
                        continue;
                    }
                    if (d.getAd_req_position() == ad_req_position) {
                        d.setAd_code(adCode);
                        break;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        if (oldToDestroy != null && oldToDestroy != nativeAd) {
            try {
                oldToDestroy.destroy();
            } catch (Exception ignored) {
            }
        }
        try {
            if (nativeAd.getResponseInfo() != null
                    && nativeAd.getResponseInfo().getLoadedAdapterResponseInfo() != null
                    && nativeAd.getResponseInfo().getLoadedAdapterResponseInfo()
                    .getAdapterClassName() != null) {
                showLog("Data:" + nativeAd.getResponseInfo()
                        .getLoadedAdapterResponseInfo().getAdapterClassName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        showLog("storeLoadedGoogleNative pos=" + ad_req_position
                + " replace=" + replaceExisting);
    }

    /** Flip-safe paint of the current ads_screen after a seat fill/refresh. */
    private void rebindAdsScreenAfterNativeLoad(final int reqPos) {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        Runnable paint = new Runnable() {
            @Override
            public void run() {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                if (shouldDeferAdWork()) {
                    long wait = Math.max(80L, FlipAdWorkGate.millisUntilAdWorkSafe() + 40L);
                    adSeatRefreshHandler.postDelayed(this, wait);
                    return;
                }
                try {
                    if (list == null || positionx < 0 || positionx >= list.size()) {
                        return;
                    }
                    ModelClass.Datum seat = list.get(positionx);
                    if (seat == null || seat.getPostType() == null
                            || !seat.getPostType().equalsIgnoreCase("ad")) {
                        return;
                    }
                    if (seat.getAd_req_position() != reqPos) {
                        return;
                    }
                    if (adapter != null) {
                        final int keep = positionx;
                        adapter.notifyDataSetChanged();
                        syncFlipViewToFeedIndex(keep);
                    }
                } catch (Exception e) {
                    showLog("rebindAdsScreenAfterNativeLoad: " + e.getMessage());
                }
            }
        };
        runOnUiThread(paint);
    }

    private boolean isCurrentPageAdSeat() {
        try {
            if (list == null || positionx < 0 || positionx >= list.size()) {
                return false;
            }
            ModelClass.Datum d = list.get(positionx);
            return d != null && d.getPostType() != null
                    && d.getPostType().equalsIgnoreCase("ad");
        } catch (Exception e) {
            return false;
        }
    }

    private void cancelAdSeatStayRefresh() {
        try {
            adSeatRefreshHandler.removeCallbacks(adSeatRefreshRunnable);
        } catch (Exception ignored) {
        }
        adSeatRefreshPage = -1;
        adSeatRefreshReqPos = -1;
    }

    /**
     * Start / reset the ~30s stay-on-ad timer. Only while the FlipView page is an
     * ad seat and the activity is resumed.
     */
    private void scheduleAdSeatStayRefresh() {
        cancelAdSeatStayRefresh();
        if (!homeLifecycleResumed || isFinishing() || isDestroyed()) {
            return;
        }
        if (!isCurrentPageAdSeat()) {
            return;
        }
        try {
            adSeatRefreshPage = positionx;
            adSeatRefreshReqPos = list.get(positionx).getAd_req_position();
            adSeatRefreshHandler.postDelayed(adSeatRefreshRunnable, AD_SEAT_REFRESH_MS);
            showLog("adSeatStayRefresh scheduled page=" + adSeatRefreshPage
                    + " reqPos=" + adSeatRefreshReqPos
                    + " in " + AD_SEAT_REFRESH_MS + "ms");
        } catch (Exception e) {
            showLog("scheduleAdSeatStayRefresh: " + e.getMessage());
        }
    }

    private void onAdSeatStayRefreshTick() {
        if (!homeLifecycleResumed || isFinishing() || isDestroyed()) {
            cancelAdSeatStayRefresh();
            return;
        }
        if (!isCurrentPageAdSeat() || positionx != adSeatRefreshPage) {
            cancelAdSeatStayRefresh();
            return;
        }
        try {
            ModelClass.Datum seat = list.get(positionx);
            if (seat == null || seat.getAd_req_position() != adSeatRefreshReqPos) {
                cancelAdSeatStayRefresh();
                return;
            }
            if (shouldDeferAdWork()) {
                long wait = Math.max(120L, FlipAdWorkGate.millisUntilAdWorkSafe() + 80L);
                adSeatRefreshHandler.postDelayed(adSeatRefreshRunnable, wait);
                return;
            }
            if (isFeedNativeSeatRequestInFlight(adSeatRefreshReqPos)) {
                // Do not fan-out; retry shortly after the in-flight fill settles.
                adSeatRefreshHandler.postDelayed(adSeatRefreshRunnable, 2_000L);
                return;
            }
            String code = seat.getAd_code();
            if (TextUtils.isEmpty(code)) {
                code = seat.getNext_req_ad_code();
            }
            if (TextUtils.isEmpty(code)) {
                code = FeedAdConfig.getUnitId(this);
            }
            String type = seat.getAd_type();
            if (TextUtils.isEmpty(type)) {
                type = "google";
            }
            showLog("adSeatStayRefresh tick → new AdMob request reqPos="
                    + adSeatRefreshReqPos);
            loadAds(code, seat.getAd_req_position(), type, true);
            // Arm the next 30s cycle only after a real stay-refresh attempt.
            if (homeLifecycleResumed && isCurrentPageAdSeat()
                    && positionx == adSeatRefreshPage) {
                adSeatRefreshHandler.removeCallbacks(adSeatRefreshRunnable);
                adSeatRefreshHandler.postDelayed(
                        adSeatRefreshRunnable, AD_SEAT_REFRESH_MS);
            }
        } catch (Exception e) {
            showLog("onAdSeatStayRefreshTick: " + e.getMessage());
            if (homeLifecycleResumed && isCurrentPageAdSeat()
                    && positionx == adSeatRefreshPage) {
                try {
                    adSeatRefreshHandler.removeCallbacks(adSeatRefreshRunnable);
                    adSeatRefreshHandler.postDelayed(
                            adSeatRefreshRunnable, AD_SEAT_REFRESH_MS);
                } catch (Exception ignored) {
                }
            }
        }
    }

    /** Ad shell from feed-level {@code ads_data} / {@code ad_positions} when the page had no ad row. */
    @Nullable
    private ModelClass.Datum adShellFromAdsData(@Nullable EncreptionRequest enc) {
        if (enc == null || !enc.isShow_ads()) return null;
        java.util.List<AdsPojo> positions = enc.getAd_positions();
        if (positions == null || positions.isEmpty()) return null;
        AdsPojo first = positions.get(0);
        if (first == null || TextUtils.isEmpty(first.getAd_code())) {
            return null;
        }
        try {
            ModelClass.Datum shell = new ModelClass.Datum();
            shell.setPostType("ad");
            shell.setAd_type(first.getAd_type() != null ? first.getAd_type() : "google");
            String code = first.getAd_code();
            shell.setAd_code(code);
            shell.setNext_req_ad_code(code);
            shell.setAds_from(code != null && code.startsWith("/") ? "admanager" : "admob");
            shell.setAd_screen_type("native");
            shell.setAd_view_type("regular");
            shell.setAd_audio_mute(true);
            shell.setAd_check(true);
            if (first.getAd_req_position() != null) {
                shell.setAd_req_position(first.getAd_req_position());
            } else {
                shell.setAd_req_position(FeedAdConfig.firstFixedAdPosition());
            }
            shell.setPostId(901012L);
            shell.setSubPostId(1L);
            return shell;
        } catch (Exception e) {
            return null;
        }
    }

    /** Client rebalance template when edge/List payload has articles only (no ad row). */
    @Nullable
    private ModelClass.Datum adShellFromFeedAdConfig() {
        try {
            String code = FeedAdConfig.getUnitId(this);
            if (TextUtils.isEmpty(code)) return null;
            ModelClass.Datum shell = new ModelClass.Datum();
            shell.setPostType("ad");
            shell.setAd_type("google");
            shell.setAd_code(code);
            shell.setNext_req_ad_code(code);
            shell.setAds_from(code.startsWith("/") ? "admanager" : "admob");
            shell.setAd_screen_type("native");
            shell.setAd_view_type("regular");
            shell.setAd_audio_mute(true);
            shell.setAd_check(true);
            shell.setAd_req_position(FeedAdConfig.firstFixedAdPosition());
            shell.setPostId(901012L);
            shell.setSubPostId(1L);
            return shell;
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    private ModelClass.Datum resolveFeedAdTemplate(@Nullable EncreptionRequest enc) {
        ModelClass.Datum t = ReadArticleStore.firstAdTemplate(list);
        if (t != null) return t;
        t = adShellFromAdsData(enc);
        if (t != null) return t;
        return adShellFromFeedAdConfig();
    }

    /**
     * Re-insert seats at {@link FeedAdConfig#FIXED_AD_POSITIONS} after article merge
     * (public edge ships no {@code post_type=ad} rows).
     */
    private void advanceFeedSkipAfterResponse(ModelClass body, int reqPos) {
        if (body == null) {
            return;
        }
        Integer respSkip = body.getSkip();
        Long respActual = body.getActualSkip();
        if (reqPos == 3) {
            if (respActual != null && respActual > actual_skip) {
                actual_skip = respActual;
            } else if (respSkip != null && respSkip > skip) {
                actual_skip = Math.max(actual_skip, respSkip.longValue());
            } else {
                actual_skip = Math.max(actual_skip, list.size());
            }
            skip = (int) Math.min(Integer.MAX_VALUE, actual_skip);
        } else {
            if (respSkip != null) {
                skip = respSkip;
            }
            if (respActual != null) {
                actual_skip = respActual;
            } else if (respSkip != null) {
                actual_skip = respSkip.longValue();
            }
        }
    }

    /** After list mutation + notify, re-seat FlipView on the same feed index. */
    private void syncFlipViewToFeedIndex(int index) {
        if (mFlipView == null || list == null || list.isEmpty()) {
            return;
        }
        int safe = Math.max(0, Math.min(index, list.size() - 1));
        positionx = safe;
        if (safe < 2) {
            ppi = safe;
        } else {
            ppi = 1;
        }
        try {
            mFlipView.flipTo(safe);
        } catch (Exception ignored) {
        }
    }

    private void shareAnyNewsAppPromotion() {
        try {
            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            sendIntent.putExtra(
                    Intent.EXTRA_TEXT,
                    "Hey check out my app at: https://play.google.com/store/apps/details?id="
                            + BuildConfig.APPLICATION_ID);
            sendIntent.setType("text/plain");
            startActivity(Intent.createChooser(sendIntent, null));
        } catch (Exception ignored) {
        }
    }

    private void rebalanceFeedAdsIfNeeded(@Nullable EncreptionRequest enc) {
        if (enc == null || !enc.isShow_ads() || list == null || list.isEmpty()) return;
        if (!isDeviceOnline()) return;
        try {
            ModelClass.Datum template = resolveFeedAdTemplate(enc);
            if (template == null) return;
            boolean changed =
                    ReadArticleStore.rebalanceFeedSlots(this, list, template);
            if (changed) {
                showLog("feed: rebalanced ad slots listSize=" + list.size());
            }
        } catch (Exception ignored) {
        }
    }

    /** One native preload for the nearest upcoming ad seat (in-flight cap = 1). */
    private void preloadNativeAdsForFeedSlots() {
        if (list == null || list.isEmpty() || !isDeviceOnline()) return;
        if (shouldDeferAdWork()) return;
        try {
            int from = Math.max(0, positionx);
            int end = Math.min(
                    list.size(),
                    from + FeedAdConfig.getPrefetchLookahead(this) + 1);
            for (int i = from; i < end; i++) {
                ModelClass.Datum d = list.get(i);
                if (d == null || d.getPostType() == null
                        || !d.getPostType().equalsIgnoreCase("ad")) {
                    continue;
                }
                String code = d.getAd_code();
                if (TextUtils.isEmpty(code)) {
                    code = d.getNext_req_ad_code();
                }
                if (TextUtils.isEmpty(code)) {
                    code = FeedAdConfig.getUnitId(this);
                }
                Integer pos = d.getAd_req_position();
                if (pos == null) {
                    pos = i + 1;
                }
                if (isFeedNativeSeatLoaded(pos)
                        || isFeedNativeSeatRequestInFlight(pos)) {
                    showLog("preloadNativeAdsForFeedSlots skip loaded/in-flight reqPos=" + pos);
                    continue;
                }
                String type = d.getAd_type();
                if (TextUtils.isEmpty(type)) {
                    type = "google";
                }
                showLog("preloadNativeAdsForFeedSlots seatIndex=" + i + " reqPos=" + pos);
                loadAds(code, pos, type);
                return;
            }
        } catch (Exception ignored) {
        }
    }

    private void loadInterstitialAd(String adCode, Integer ad_req_position) {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(this, "ca-app-pub-3940256099942544/1033173712", adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
            }

            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                super.onAdLoaded(interstitialAd);
                try {
                    if (list != null && list.size() > 0) {
                        FlipGoogleNativeAds flipGoogleNativeAds = new FlipGoogleNativeAds();
                        flipGoogleNativeAds.setInterstitialAd(interstitialAd);
                        flipGoogleNativeAds.setAdCode(adCode);
                        flipGoogleNativeAds.setReqPos(ad_req_position);
                        if (!nativeGoogleAdArray.contains(list.get(positionx).getAd_req_position())) {
                            nativeGoogleAdArray.add(ad_req_position);
                        }
                        nativeGoogleAdList.add(flipGoogleNativeAds);
                        try {
                            if(interstitialAd.getResponseInfo()!=null && interstitialAd.getResponseInfo().getLoadedAdapterResponseInfo()!=null &&
                                    interstitialAd.getResponseInfo().getLoadedAdapterResponseInfo().getAdapterClassName()!=null){
                                showLog("Data:"+interstitialAd.getResponseInfo().getLoadedAdapterResponseInfo().getAdapterClassName());
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }


            }
        });

    }

    private void loadFBAds(String adCode, Integer ad_req_position) {
        showLog("loadAds3");
      //  AdSettings.addTestDevice("b14f44bb-6e6c-4e38-910a-17b1b14ccc78");
        //AdSettings.addTestDevice(" 7f8b87ee-f0f2-4e3f-8a78-e3e60e226f41");
        //AdSettings.addTestDevice("811a589f-fb7f-496b-be57-863f812ab0db");
//        AdSettings.addTestDevice("e281cb34-438f-441d-a6f4-f44f12a2cbc8");
//        AdSettings.addTestDevice("2769be7c-e83c-42f5-b10c-79c0d6ea8d8e");
        //AdSettings.addTestDevice("3a138881-78e4-4aa0-9d51-45a91f7c9539");
        // AdSettings.addTestDevice("ba679175-04d5-4eb0-a5d3-b34a272ddc58");
       // String placementID="1908969226136425_1909067626126585";
//         String adCodeN="VID_HD_16_9_46S_LINK#"+adCode;
        com.facebook.ads.NativeAd nativeAd = new com.facebook.ads.NativeAd(HomePage.this, adCode);
        NativeAdListener nativeAdListener = new NativeAdListener() {
            @Override
            public void onMediaDownloaded(Ad ad) {
                // Native ad finished downloading all assets
                showLog(TAG+  "Native ad finished downloading all assets.");
            }

            @Override
            public void onError(Ad ad, AdError adError) {
                // Native ad failed to load
                showLog(TAG+  "Native ad failed to load: " + adError.getErrorMessage());

            }

            @Override
            public void onAdLoaded(Ad ad) {
                if (nativeAd == null || nativeAd != ad) {
                    return;
                }
                // Native ad is loaded and ready to be displayed
                showLog(TAG+  "Native ad is loaded and ready to be displayed!");
                FlipGoogleNativeAds flipGoogleNativeAds=new FlipGoogleNativeAds();
                flipGoogleNativeAds.setFbAd(nativeAd);
                flipGoogleNativeAds.setAdCode("");
                flipGoogleNativeAds.setReqPos(list.get(positionx).getAd_req_position());
                if(!nativeFBAdArray.contains(list.get(positionx).getAd_req_position())){
                    nativeFBAdArray.add(list.get(positionx).getAd_req_position());
                }
                nativeFBAdList.add(flipGoogleNativeAds);

            }

            @Override
            public void onAdClicked(Ad ad) {
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                // Native ad impression
                adImpress=true;
            }
        };
        nativeAd.loadAd(
                nativeAd.buildLoadAdConfig()
                        .withMediaCacheFlag(NativeAdBase.MediaCacheFlag.ALL)
                        .withAdListener(nativeAdListener)
                        .build());
    }

    @Override
    public void onPageRequested(int page) {
        mFlipView.smoothFlipTo(page);
    }

    public void downloadRequests(int page){
        Random rand = new Random();
        int uniqID = rand.nextInt(1000);
        try {
            if(page==0){
                //PDF
                adapter.downloadPDFWithProgressBar(list.get(positionx).getDownloadUrl(), "anyNews_"+uniqID+"_"+ list.get(positionx).getPostId() + ".pdf", positionx,page);
            }else{
                showLog("getMediaType::"+list.get(positionx).getMediaType());
                if(list.get(positionx).getMediaType().equals("4") || list.get(positionx).getMediaType().equals("6") ){
                    //Youtube full and half video download
                    showLog("DATA::"+list.get(positionx).getMediaType());
                    adapter.downloadPDFWithProgressBar(list.get(positionx).getDownloadUrl(), "anyNews_"+uniqID+"_"+ + list.get(positionx).getPostId() + "."+getMimeType(list.get(positionx).getDownloadUrl()), positionx,page);
                }else{
                    //normal full and half video download
                    adapter.downloadPDFWithProgressBar(list.get(positionx).getVideoUrl(), "anyNews_"+uniqID+"_"+ + list.get(positionx).getPostId() + "."+getMimeType(list.get(positionx).getVideoUrl()), positionx,page);
                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestedPermission(int page) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                downloadRequests(page);

            }else{

                PermissionX.init(HomePage.this)
                        .permissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .onExplainRequestReason(new ExplainReasonCallback() {
                            @Override
                            public void onExplainReason(@NonNull ExplainScope scope, @NonNull List<String> deniedList) {
                                scope.showRequestReasonDialog(deniedList, "Please allow the below necessary permissions to download videos", "OK", "Cancel");
                            }
                        })
                        .onForwardToSettings(new ForwardToSettingsCallback() {
                            @Override
                            public void onForwardToSettings(@NonNull ForwardScope scope, @NonNull List<String> deniedList) {
                                scope.showForwardToSettingsDialog(deniedList, "Please allow below permission manually from settings page to download videos", "OK", "Cancel");

                            }
                        })
                        .request(new RequestCallback() {
                            @Override
                            public void onResult(boolean allGranted, @NonNull List<String> grantedList, @NonNull List<String> deniedList) {
                                if (allGranted) {
                                    downloadRequests(page);
                                } else {

                                }
                            }
                        });

//                if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                        == PackageManager.PERMISSION_GRANTED) {
//                   downloadRequests(page);.
//                } else {
//                    Log.v(TAG, "Permission is revoked");
//                    //ActivityCompat.requestPermissions();
//                    String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
//                    if(page==0){
//                        ActivityCompat.requestPermissions(this, permissions, 223);
//                    }else{
//                        ActivityCompat.requestPermissions(this, permissions, 234);
//                    }
//
//                }
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            downloadRequests(page);
        }
    }

    @Override
    public void openFlashNewsFromFeedChip() {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        performFlashNewsOpen();
    }

    @Override
    public void openAdScreenArticleOverlay(@Nullable ModelClass.Datum datum) {
        if (isFinishing() || isDestroyed() || datum == null) {
            return;
        }
        try {
            if (adScreenArticleOverlay == null && homeAdArticleOverlayHost != null) {
                adScreenArticleOverlay =
                        new AdScreenArticleOverlay(HomePage.this, homeAdArticleOverlayHost);
            }
            if (adScreenArticleOverlay != null) {
                if (adapter != null) {
                    adScreenArticleOverlay.setFeedAdapter(adapter);
                }
                // Temp surface only — does not flipTo, splice feed, or change positionx.
                adScreenArticleOverlay.show(datum);
            }
        } catch (Exception e) {
            showLog("openAdScreenArticleOverlay: " + e.getMessage());
        }
    }

    @Nullable
    private ModelClass.Datum resolveAdsScreenOverlayForFlip(int adFeedIndex) {
        try {
            if (adapter != null) {
                return adapter.getAdsScreenOverlayDatum(adFeedIndex);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /** Engagement / Continue-reading hub — search feed mode not ported; soft News. */
    public boolean isSearchFeedActive() {
        return false;
    }

    public void openMainNewsFromSearchHub() {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        beginInstantNewsFooterReturn(true);
    }

    public void openLocalNewsFromSearchHub() {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        try {
            performLocalFeedOpenFromDailyUpdates();
        } catch (Exception e) {
            beginInstantNewsFooterReturn(true);
        }
    }

    public void openFlashNewsFromSearchHub() {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        performFlashNewsOpen();
    }

    public void openDailyUpdatesFromSearchHub() {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        performDailyUpdatesOpen();
    }

    public boolean isSearchHubLocalAvailable() {
        try {
            return isDeviceOnline();
        } catch (Exception e) {
            return true;
        }
    }

    public boolean isSearchHubFlashAvailable() {
        try {
            return isDeviceOnline();
        } catch (Exception e) {
            return true;
        }
    }

    public boolean isSearchHubDailyUpdatesAvailable() {
        try {
            if (bottomNavigationView == null) {
                return true;
            }
            MenuItem item = bottomNavigationView.getMenu().findItem(R.id.navigation_status);
            return item != null && item.isVisible();
        } catch (Exception e) {
            return true;
        }
    }

    @Override
    public void callLocationRequest(int from) {
        initialCall=false;

        try {
            showLog("CURRENT_LOCATION category_ID:"+category_id);
            int timer=0;
            if(positionx==requestPostition){
                timer=2000;
            }else{
                timer=0;
            }


            if (category_id == 0) {
                mFlipView.setVisibility(View.VISIBLE);
                showHomeFeedLoadingReveal();
                layout_with_data.setVisibility(View.GONE);
                comment_page.setVisibility(View.GONE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        last_post_id = 0;
                        skip = 0;
                        actual_skip = 0;
                        category_id = -1;
                        requestPostition=1;
                        requestSize=0;
                        isFromLocalArticles=true;
                        positionx=0;
                        list=new ArrayList<>();
                        dummyResponse=null;
                        inlist(-1);
                    }
                },timer);



            } else {
                  /*  un_selected_local.setVisibility(View.VISIBLE);

                    un_selected_my_feed.setVisibility(View.GONE);
                    selected_my_feed.setVisibility(View.VISIBLE);
                    selected_local.setVisibility(View.GONE);*/




                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showHomeFeedLoadingReveal();
                        layout_with_data.setVisibility(View.GONE);
                        comment_page.setVisibility(View.GONE);

                        /*linear_home_image.setImageResource(R.drawable.ic_new_home);*/
                        /*linear_home_text.setText("" + languageModelResponse.getData().getHome());*/
                        list=new ArrayList<>();
                        last_post_id = 0;
                        skip = 0;
                        actual_skip = 0;
                        category_id = 0;

                        requestSize=0;
                        isFromLocalArticles=true;
                        positionx=0;
                        requestPostition=1;
                        dummyResponse=null;
                        //   inlist(0);
                        inlist(0);
                    }
                },timer);
            }

            writeButtonClick("User", type_firebase_button, current_time_firebase, draft_id_firebase, device_id_firebase, button_name_local);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode==289){
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==234){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                showLog(""+TAG+"Permission: "+permissions[0]+ "was "+grantResults[0]);
                downloadRequests(1);
              //  adapter.downloadPDFWithProgressBar(list.get(positionx).getVideoUrl(),"anyNews_+"+list.get(positionx).getPostId()+"."+getMimeType(list.get(positionx).getVideoUrl()),positionx,1);
                //resume tasks needing this permission
            }
        }else if(requestCode==223){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                showLog(""+TAG+"Permission: "+permissions[0]+ "was "+grantResults[0]);
                downloadRequests(0);
//                adapter.downloadPDFWithProgressBar(list.get(positionx).getDownloadUrl(),"anyNews_+"+list.get(positionx).getPostId()+".pdf",positionx,0);
                //resume tasks needing this permission
            }
        }

    }
    public static String getMimeType(String url) {
        String type = null;
        String[] array=url.split("/");
        String data=url.substring(url.lastIndexOf("/") + 1);
        data=data.substring(data.lastIndexOf(".") + 1);
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        showLog("type:::::::"+type);
        showLog("data:::::::"+data);
        return data;
    }
    boolean loadingOnOverFlip =true;
    @Override
    public void onOverFlip(FlipView v, OverFlipMode mode, boolean overFlippingPrevious, float overFlipDistance, float flipDistancePerPage) {
        Logone.i("onoverflip", "overFlipDistance = " + overFlipDistance);
        showLog("onOverFlip::overFlipDistance:"+ overFlipDistance);
        showLog("onOverFlip::flipDistancePerPage:"+ flipDistancePerPage);
        showLog("onOverFlip::getPageCount:"+ v.getPageCount());
        showLog("onOverFlip::size:"+ list.size());
        showLog("onOverFlip::positionx:"+ positionx);
        showLog("onOverFlip::positionx:"+((list.size()-1)==positionx));

        if(v.getPageCount()==list.size() && (list.size()-1)==positionx && loadingOnOverFlip && overFlipDistance>0){
            boolean showLoadingToast = false;
            if (!isDataParsed && dummyResponse != null) {
                convertListOfArticles(dummyResponse, 3);
            } else if (!isDataParsed && !isApiRequested) {
                isApiRequested = true;
                ShowNews_Data_Values(category_id, 3);
                showLoadingToast = true;
            } else if (!isDataParsed && isApiRequested && dummyResponse == null) {
                showLoadingToast = true;
            } else if (isDataParsed && "No articles to read further".equals(no_article_message)) {
                Toasty.info(getApplicationContext(), no_article_message, Toast.LENGTH_SHORT, true).show();
            }
            if (showLoadingToast) {
                Toasty.info(getApplicationContext(), no_article_message, Toast.LENGTH_SHORT, true).show();
            }
            loadingOnOverFlip = false;
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loadingOnOverFlip =true;
            }
        },1500);



    }

    @Override
    public void flipfinsh() {
        // Ad seat: forward flip advances feed — no overlay hijack on over-flip end.
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        //navigation_stricker.setCheckable(true);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        try {
            setIntent(intent);
            handleWarmPushOpen(intent);
        } catch (Exception e) {
            Logone.w("HomePage", "onNewIntent failed", e);
        }
    }

    /**
     * FCM tray tap while Home is already alive ({@code singleTop} +
     * {@code CLEAR_TOP}). Pins sticky {@code push_post_id} and reloads page-0
     * so {@code List_All_Articles} records the admin push Open.
     */
    private void handleWarmPushOpen(@Nullable Intent intent) {
        if (intent == null) {
            return;
        }
        // Share deep-link while Home is alive: pin SHARE_ID and page-0 seat.
        if (!isHomeWarmPushTap(intent) && applyWarmShareRedirect(intent)) {
            reloadFeedForWarmPushOpen();
            return;
        }
        if (!isHomeWarmPushTap(intent)) {
            return;
        }
        String pushId = MyFirebaseMessagingService.resolvePushPostId(this, intent);
        if (!isNumericPushId(pushId)) {
            if (MyFirebaseMessagingService.isHistoryLauncherFallback(this, intent)) {
                pushId = MyFirebaseMessagingService.resolveHistoryDisplayedTitle(this, intent);
            }
        }
        if (!isNumericPushId(pushId)) {
            return;
        }
        pushId = pushId.trim();
        try {
            MyFirebaseMessagingService.dismissOpenedPush(this, intent);
        } catch (Exception ignored) {
        }
        pushUpdatedID = pushId;
        try {
            String rewards = intent.getStringExtra("TITLE_REWARDS");
            if (rewards != null && !rewards.isEmpty() && !"null".equalsIgnoreCase(rewards)) {
                TITLE_REWARDS = rewards;
            }
            String data2g = intent.getStringExtra("DATAFROM2G");
            if (data2g != null && !"null".equalsIgnoreCase(data2g)) {
                DATAFROM2G = data2g;
            }
        } catch (Exception ignored) {
        }
        // Push owns seating — clear share pin so the push card is page 0.
        SHARE_REDIRECT_ID = "";
        SHARE_ID = 0;
        try {
            SharedPrefsData.putString(
                    getApplicationContext(),
                    Constants.PENDING_PUSH_POST,
                    pushId,
                    Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
        if (TITLE_REWARDS != null && TITLE_REWARDS.contains("rewards")) {
            try {
                Intent profile = new Intent(HomePage.this, newProfileDetails.class);
                if (getApplicationContext() != null
                        && getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)) {
                    startActivity(profile);
                }
            } catch (Exception ignored) {
            }
            return;
        }
        reloadFeedForWarmPushOpen();
    }

    /**
     * Thin share seating: read SHARE_REDIRECT_ID from a warm intent and pin
     * {@link #SHARE_ID} so List_All_Articles returns that card first.
     *
     * @return true when a numeric share id was applied
     */
    private boolean applyWarmShareRedirect(@Nullable Intent intent) {
        if (intent == null || isFinishing() || isDestroyed()) {
            return false;
        }
        String shareRaw = null;
        try {
            shareRaw = intent.getStringExtra("SHARE_REDIRECT_ID");
        } catch (Exception ignored) {
        }
        if (shareRaw == null || shareRaw.isEmpty() || "null".equalsIgnoreCase(shareRaw)) {
            return false;
        }
        long shareId;
        try {
            shareId = Long.parseLong(shareRaw.trim());
        } catch (Exception e) {
            return false;
        }
        if (shareId <= 0L) {
            return false;
        }
        SHARE_REDIRECT_ID = shareRaw.trim();
        SHARE_ID = shareId;
        // Avoid fighting push pin with the same id as TITLE.
        pushUpdatedID = "0";
        TITLE_VALUE = 0;
        try {
            SharedPrefsData.putString(
                    getApplicationContext(),
                    Constants.PENDING_PUSH_POST,
                    "",
                    Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
        showLog("HomePage warm share seat SHARE_ID=" + SHARE_ID);
        return true;
    }

    private void reloadFeedForWarmPushOpen() {
        try {
            if (isFinishing() || isDestroyed()) {
                return;
            }
            last_post_id = 0;
            skip = 0;
            actual_skip = 0;
            category_id = 0;
            isFromLocalArticles = false;
            positionx = 0;
            ppi = 0;
            TITLE_VALUE = 0;
            try {
                if (pushUpdatedID != null
                        && !pushUpdatedID.isEmpty()
                        && !"0".equals(pushUpdatedID)
                        && !"null".equalsIgnoreCase(pushUpdatedID)) {
                    TITLE_VALUE = Integer.parseInt(pushUpdatedID.trim());
                }
            } catch (Exception ignored) {
            }
            try {
                if (mFlipView != null) {
                    mFlipView.resetSeatForNewFeed();
                }
            } catch (Exception ignored) {
            }
            showLog("HomePage warm open reload push=" + pushUpdatedID
                    + " share=" + SHARE_ID);
            inlist(0);
        } catch (Exception e) {
            Logone.w("HomePage", "reloadFeedForWarmPushOpen failed", e);
        }
    }

    /** Tray tap — not a generic launcher relaunch; TITLE alone is too broad. */
    private static boolean isHomeWarmPushTap(@Nullable Intent intent) {
        if (intent == null) {
            return false;
        }
        if (isFcmPushIntentUri(intent.getData())
                || intent.getBooleanExtra("notification", false)
                || MyFirebaseMessagingService.isOpenPushAction(intent.getAction())) {
            return true;
        }
        String titile = intent.getStringExtra("TITILE");
        if (titile == null || titile.isEmpty() || "null".equalsIgnoreCase(titile)) {
            return false;
        }
        try {
            return Long.parseLong(titile.trim()) > 0L;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isFcmPushIntentUri(@Nullable Uri uri) {
        if (uri == null) {
            return false;
        }
        return "anynews".equalsIgnoreCase(uri.getScheme())
                && "push".equalsIgnoreCase(uri.getHost());
    }

    private static boolean isNumericPushId(@Nullable String raw) {
        if (raw == null) {
            return false;
        }
        try {
            return Long.parseLong(raw.trim()) > 0L;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void setStaticData() {

        try {
            allsharedpreference = new Allsharedpreference(HomePage.this);
            Gson gson = new Gson();
            String Translation = allsharedpreference.getShared_String(Allsharedpreference.Translator);
            languageModelResponse = gson.fromJson(Translation, LanguageModelResponse.class);

            if (languageModelResponse.getData() != null) {
                my_feed.setText("" + languageModelResponse.getData().getMyFeed());
                my_feed_text.setText("" + languageModelResponse.getData().getMyFeed());

                try {
                    FooterTabLabels.apply(this, bottomNavigationView, languageModelResponse.getData());
                } catch (Exception ignored) {
                }
                fab_fullpost.setText("" + languageModelResponse.getData().getFullPost());
                fab_video.setText("" + languageModelResponse.getData().getVideoPost());
                fab_article.setText("" + languageModelResponse.getData().getArticle());
                nodatascreen.setText("" + languageModelResponse.getData().getNoData());
                leave_comment_white.setHint("" + languageModelResponse.getData().getWriteComments());
            }

        } catch (NullPointerException e) {
            e.printStackTrace();
        }


    }

    public BroadcastReceiver WRITER_FOLLOWER = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            writer_follow = findViewById(R.id.writer_follow);
            writer_follower = findViewById(R.id.writer_follower);
            test_folloew = findViewById(R.id.test_folloew);

            showLog("WRITER_FOLLOWER 1 :positionx:"+positionx);

            if (list!=null && list.size()>0 && list.get(positionx).getIsFollow() == 0) {


                for (int i = 0; i < list.size(); i++) {

                    showLog(list.get(i).getTitle() + ": Title " + list.get(i).getIsFollow());


                    if (list.get(positionx).getWriterName().equals(list.get(i).getWriterName())) {
                        ModelClass.Datum datum_1 =list.get(i);
//
//                        datum_1.setLikeStatus(list.get(i).getLikeStatus());
//                        datum_1.setTitle(list.get(i).getTitle());
//                        datum_1.setDescription(list.get(i).getDescription());
//                        datum_1.setCategoryId(list.get(i).getCategoryId());
//                        datum_1.setPostType(list.get(i).getPostType());
//                        datum_1.setMediaType(list.get(i).getMediaType());
//                        datum_1.setImages(list.get(i).getImages());
//                        datum_1.setVideos(list.get(i).getVideos());
//                        datum_1.setVideoUrl(list.get(i).getVideoUrl());
//                        datum_1.setThumbnail(list.get(i).getThumbnail());
//                        datum_1.setPublishedTime(list.get(i).getPublishedTime());
//                        datum_1.setShares(list.get(i).getShares());
//                        datum_1.setDownloads(list.get(i).getDownloads());
//                        datum_1.setStickyPosition(list.get(i).getStickyPosition());
//                        datum_1.setDownloads(list.get(i).getDownloads());
//                        datum_1.setCommentsFlag(list.get(i).getCommentsFlag());
//                        datum_1.setWriterType(list.get(i).getWriterType());
//                        datum_1.setShareLink(list.get(i).getShareLink());
//                        datum_1.setBackgroundImage(list.get(i).getBackgroundImage());
//                        datum_1.setPostId(list.get(i).getPostId());
//                        datum_1.setWriterId(list.get(i).getWriterId());
//                        datum_1.setSubPostId(list.get(i).getSubPostId());
//                        datum_1.setWriterName(list.get(i).getWriterName());
//                        datum_1.setWriterType(list.get(i).getWriterType());
//                        datum_1.setRole(list.get(i).getRole());
//
//                        datum_1.setIs_source(list.get(i).getIs_source());
//                        datum_1.setSource_link(list.get(i).getSource_link());
//                        datum_1.setStrip_content(list.get(i).getStrip_content());
//                        datum_1.setWriterProfile(list.get(i).getWriterProfile());
//                        datum_1.setWeb_url(list.get(i).getWeb_url());
//                        datum_1.setThumbnail(list.get(i).getThumbnail());

                        datum_1.setIsFollow(1);
                        list.set(i, datum_1);
                        showLog(list.get(i).getTitle() + ": Title " + list.get(i).getIsFollow());
                        try{
                            test_folloew.setText("Following");
                            writer_follow.setVisibility(View.GONE);
                            writer_follower.setVisibility(View.VISIBLE);
                        }catch (Exception e){
                            e.printStackTrace();
                        }



                    }


                }


            } else {


                for (int j = 0; j < list.size(); j++) {


                    if (list.get(positionx).getWriterName().equals(list.get(j).getWriterName())) {
                        ModelClass.Datum datum_1 = list.get(j);
//
//                        datum_1.setLikeStatus(list.get(j).getLikeStatus());
//                        datum_1.setTitle(list.get(j).getTitle());
//                        datum_1.setDescription(list.get(j).getDescription());
//                        datum_1.setCategoryId(list.get(j).getCategoryId());
//                        datum_1.setPostType(list.get(j).getPostType());
//                        datum_1.setMediaType(list.get(j).getMediaType());
//                        datum_1.setImages(list.get(j).getImages());
//                        datum_1.setVideos(list.get(j).getVideos());
//                        datum_1.setVideoUrl(list.get(j).getVideoUrl());
//                        datum_1.setThumbnail(list.get(j).getThumbnail());
//                        datum_1.setPublishedTime(list.get(j).getPublishedTime());
//                        datum_1.setShares(list.get(j).getShares());
//                        datum_1.setDownloads(list.get(j).getDownloads());
//                        datum_1.setStickyPosition(list.get(j).getStickyPosition());
//                        datum_1.setDownloads(list.get(j).getDownloads());
//                        datum_1.setCommentsFlag(list.get(j).getCommentsFlag());
//                        datum_1.setWriterType(list.get(j).getWriterType());
//                        datum_1.setShareLink(list.get(j).getShareLink());
//                        datum_1.setBackgroundImage(list.get(j).getBackgroundImage());
//                        datum_1.setPostId(list.get(j).getPostId());
//                        datum_1.setWriterId(list.get(j).getWriterId());
//                        datum_1.setSubPostId(list.get(j).getSubPostId());
//                        datum_1.setWriterName(list.get(j).getWriterName());
//                        datum_1.setWriterType(list.get(j).getWriterType());
//                        datum_1.setRole(list.get(j).getRole());
//
//                        datum_1.setIs_source(list.get(j).getIs_source());
//                        datum_1.setSource_link(list.get(j).getSource_link());
//                        datum_1.setStrip_content(list.get(j).getStrip_content());
//                        datum_1.setWriterProfile(list.get(j).getWriterProfile());
//                        datum_1.setWeb_url(list.get(j).getWeb_url());
//                        datum_1.setThumbnail(list.get(j).getThumbnail());
                        datum_1.setIsFollow(0);

                        list.set(j, datum_1);

                        try{
                            if(test_folloew!=null){
                                test_folloew.setText("Follow");
                            }
                            writer_follow.setVisibility(View.VISIBLE);
                            writer_follower.setVisibility(View.GONE);
                        }catch (Exception e){
                            e.printStackTrace();
                        }



                    }

                }

            }

        }

    };



    public BroadcastReceiver WRITER_FOLLOW = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {


                writer_follow = findViewById(R.id.writer_follow);
                writer_follower = findViewById(R.id.writer_follower);
                test_folloew = mFlipView.getChildAt(ppi).findViewById(R.id.test_folloew);
                showLog("WRITER_FOLLOW positionx:"+positionx);

                if (list!=null && list.size()>0 && list.get(positionx).getIsFollow() == 0) {

                    for (int i = 0; i < list.size(); i++) {


                        if (list.get(positionx).getWriterName().equals(list.get(i).getWriterName())) {
                            ModelClass.Datum datum_1 = list.get(i);

//                        datum_1.setLikeStatus(list.get(i).getLikeStatus());
//                        datum_1.setTitle(list.get(i).getTitle());
//                        datum_1.setDescription(list.get(i).getDescription());
//                        datum_1.setCategoryId(list.get(i).getCategoryId());
//                        datum_1.setPostType(list.get(i).getPostType());
//                        datum_1.setMediaType(list.get(i).getMediaType());
//                        datum_1.setImages(list.get(i).getImages());
//                        datum_1.setVideos(list.get(i).getVideos());
//                        datum_1.setVideoUrl(list.get(i).getVideoUrl());
//                        datum_1.setThumbnail(list.get(i).getThumbnail());
//                        datum_1.setPublishedTime(list.get(i).getPublishedTime());
//                        datum_1.setShares(list.get(i).getShares());
//                        datum_1.setDownloads(list.get(i).getDownloads());
//                        datum_1.setStickyPosition(list.get(i).getStickyPosition());
//                        datum_1.setDownloads(list.get(i).getDownloads());
//                        datum_1.setCommentsFlag(list.get(i).getCommentsFlag());
//                        datum_1.setWriterType(list.get(i).getWriterType());
//                        datum_1.setShareLink(list.get(i).getShareLink());
//                        datum_1.setBackgroundImage(list.get(i).getBackgroundImage());
//                        datum_1.setPostId(list.get(i).getPostId());
//                        datum_1.setWriterId(list.get(i).getWriterId());
//                        datum_1.setSubPostId(list.get(i).getSubPostId());
//                        datum_1.setWriterName(list.get(i).getWriterName());
//                        datum_1.setWriterType(list.get(i).getWriterType());
//                        datum_1.setRole(list.get(i).getRole());
//
//                        datum_1.setIs_source(list.get(i).getIs_source());
//                        datum_1.setSource_link(list.get(i).getSource_link());
//                        datum_1.setStrip_content(list.get(i).getStrip_content());
//                        datum_1.setWriterProfile(list.get(i).getWriterProfile());

                            datum_1.setIsFollow(1);
                            list.set(i, datum_1);
                            showLog(list.get(i).getTitle() + ": Title " + list.get(i).getIsFollow());
                            try{
                                writer_follow.setVisibility(View.GONE);
                                writer_follower.setVisibility(View.VISIBLE);
                                test_folloew.setText("Following");
                            }catch (Exception e){
                                e.printStackTrace();
                            }


                        }


                    }


                } else {

                    for (int j = 0; j < list.size(); j++) {


                        if (list.get(positionx).getWriterName().equals(list.get(j).getWriterName())) {
                            ModelClass.Datum datum_1 = list.get(j);

//                        datum_1.setLikeStatus(list.get(j).getLikeStatus());
//                        datum_1.setTitle(list.get(j).getTitle());
//                        datum_1.setDescription(list.get(j).getDescription());
//                        datum_1.setCategoryId(list.get(j).getCategoryId());
//                        datum_1.setPostType(list.get(j).getPostType());
//                        datum_1.setMediaType(list.get(j).getMediaType());
//                        datum_1.setImages(list.get(j).getImages());
//                        datum_1.setVideos(list.get(j).getVideos());
//                        datum_1.setVideoUrl(list.get(j).getVideoUrl());
//                        datum_1.setThumbnail(list.get(j).getThumbnail());
//                        datum_1.setPublishedTime(list.get(j).getPublishedTime());
//                        datum_1.setShares(list.get(j).getShares());
//                        datum_1.setDownloads(list.get(j).getDownloads());
//                        datum_1.setStickyPosition(list.get(j).getStickyPosition());
//                        datum_1.setDownloads(list.get(j).getDownloads());
//                        datum_1.setCommentsFlag(list.get(j).getCommentsFlag());
//                        datum_1.setWriterType(list.get(j).getWriterType());
//                        datum_1.setShareLink(list.get(j).getShareLink());
//                        datum_1.setBackgroundImage(list.get(j).getBackgroundImage());
//                        datum_1.setPostId(list.get(j).getPostId());
//                        datum_1.setWriterId(list.get(j).getWriterId());
//                        datum_1.setSubPostId(list.get(j).getSubPostId());
//                        datum_1.setWriterName(list.get(j).getWriterName());
//                        datum_1.setWriterType(list.get(j).getWriterType());
//                        datum_1.setRole(list.get(j).getRole());
//
//                        datum_1.setIs_source(list.get(j).getIs_source());
//                        datum_1.setSource_link(list.get(j).getSource_link());
//                        datum_1.setStrip_content(list.get(j).getStrip_content());
//                        datum_1.setWriterProfile(list.get(j).getWriterProfile());

                            datum_1.setIsFollow(0);

                            list.set(j, datum_1);
                            try{
                                test_folloew.setText("Follow");
                                writer_follow.setVisibility(View.VISIBLE);
                                writer_follower.setVisibility(View.GONE);
                            }catch (Exception e){
                                e.printStackTrace();
                            }


                        }

                    }
                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }

    };
    public BroadcastReceiver SURVEY_STATUS = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (list.get(positionx).getSurveyStatus() == 0) {


                for (int i = 0; i < list.size(); i++) {

                    if (list.get(positionx).getSurveyStatus().equals(list.get(i).getSurveyStatus())) {
                        ModelClass.Datum datum_1 =list.get(i);

//                        datum_1.setLikeStatus(list.get(i).getLikeStatus());
//                        datum_1.setTitle(list.get(i).getTitle());
//                        datum_1.setDescription(list.get(i).getDescription());
//                        datum_1.setCategoryId(list.get(i).getCategoryId());
//                        datum_1.setPostType(list.get(i).getPostType());
//                        datum_1.setMediaType(list.get(i).getMediaType());
//                        datum_1.setImages(list.get(i).getImages());
//                        datum_1.setVideos(list.get(i).getVideos());
//                        datum_1.setVideoUrl(list.get(i).getVideoUrl());
//                        datum_1.setThumbnail(list.get(i).getThumbnail());
//                        datum_1.setPublishedTime(list.get(i).getPublishedTime());
//                        datum_1.setShares(list.get(i).getShares());
//                        datum_1.setDownloads(list.get(i).getDownloads());
//                        datum_1.setStickyPosition(list.get(i).getStickyPosition());
//                        datum_1.setDownloads(list.get(i).getDownloads());
//                        datum_1.setCommentsFlag(list.get(i).getCommentsFlag());
//                        datum_1.setWriterType(list.get(i).getWriterType());
//                        datum_1.setShareLink(list.get(i).getShareLink());
//                        datum_1.setBackgroundImage(list.get(i).getBackgroundImage());
//                        datum_1.setPostId(list.get(i).getPostId());
//                        datum_1.setWriterId(list.get(i).getWriterId());
//                        datum_1.setSubPostId(list.get(i).getSubPostId());
//                        datum_1.setWriterName(list.get(i).getWriterName());
//                        datum_1.setWriterType(list.get(i).getWriterType());
//                        datum_1.setRole(list.get(i).getRole());
                        datum_1.setIsFollow(1);
                        list.set(i, datum_1);
                        showLog(list.get(i).getTitle() + ": Title " + list.get(i).getIsFollow());
                    }

                }

            } else {

                for (int j = 0; j < list.size(); j++) {

                    if (list.get(positionx).getSurveyStatus().equals(list.get(j).getSurveyStatus())) {

                        ModelClass.Datum datum_1 =list.get(j);

//                        datum_1.setLikeStatus(list.get(j).getLikeStatus());
//                        datum_1.setTitle(list.get(j).getTitle());
//                        datum_1.setDescription(list.get(j).getDescription());
//                        datum_1.setCategoryId(list.get(j).getCategoryId());
//                        datum_1.setPostType(list.get(j).getPostType());
//                        datum_1.setMediaType(list.get(j).getMediaType());
//                        datum_1.setImages(list.get(j).getImages());
//                        datum_1.setVideos(list.get(j).getVideos());
//                        datum_1.setVideoUrl(list.get(j).getVideoUrl());
//                        datum_1.setThumbnail(list.get(j).getThumbnail());
//                        datum_1.setPublishedTime(list.get(j).getPublishedTime());
//                        datum_1.setShares(list.get(j).getShares());
//                        datum_1.setDownloads(list.get(j).getDownloads());
//                        datum_1.setStickyPosition(list.get(j).getStickyPosition());
//                        datum_1.setDownloads(list.get(j).getDownloads());
//                        datum_1.setCommentsFlag(list.get(j).getCommentsFlag());
//                        datum_1.setWriterType(list.get(j).getWriterType());
//                        datum_1.setShareLink(list.get(j).getShareLink());
//                        datum_1.setBackgroundImage(list.get(j).getBackgroundImage());
//                        datum_1.setPostId(list.get(j).getPostId());
//                        datum_1.setWriterId(list.get(j).getWriterId());
//                        datum_1.setSubPostId(list.get(j).getSubPostId());
//                        datum_1.setWriterName(list.get(j).getWriterName());
//                        datum_1.setWriterType(list.get(j).getWriterType());
//                        datum_1.setRole(list.get(j).getRole());
                        datum_1.setIsFollow(0);

                        list.set(j, datum_1);
                    }

                }
            }
        }

    };

    ActivityResultLauncher<Intent> postCommentActivityListerner = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    showLog("someActivityResultLauncher in home");
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        if(data!=null){
                            String emailID=data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                            showLog("EmailID"+emailID);
                            allsharedpreference.setEmailID(emailID);
                            postComment();
                        }else{
                            Toast.makeText(HomePage.this,"Unable to pickup the account please try again later..",Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    showLog("someActivityResultLauncher in home");
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        if(data!=null){
                            String emailID=data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                            showLog("EmailID"+emailID);
                            allsharedpreference.setEmailID(emailID);

                        }else{
                            Toast.makeText(HomePage.this,"Unable to pickup the account please try again later..",Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });

    ActivityResultLauncher<IntentSenderRequest> playAppUpdateLauncher = registerForActivityResult(
            new ActivityResultContracts.StartIntentSenderForResult(),
            result -> {
                try {
                    if (playInAppUpdateHelper != null) {
                        playInAppUpdateHelper.resumeIfDownloaded();
                    }
                } catch (Exception ignored) {
                }
            });

    public BroadcastReceiver DIS_LIKE_PAGE = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            showLog("DIS LIKE " + list.get(positionx).getLikeStatus());

            try {

                if (list.get(positionx).getLikeStatus() == 0) {

                    showLog("DIS LIKE 00 " + list.get(positionx).getLikeStatus());

                    ModelClass.Datum datum = list.get(positionx);
                    datum.setLikeStatus(2);
//                    datum.setTitle(list.get(positionx).getTitle());
//                    datum.setDescription(list.get(positionx).getDescription());
//                    datum.setCategoryId(list.get(positionx).getCategoryId());
//                    datum.setPostType(list.get(positionx).getPostType());
//                    datum.setMediaType(list.get(positionx).getMediaType());
//                    datum.setImages(list.get(positionx).getImages());
//                    datum.setVideos(list.get(positionx).getVideos());
//                    datum.setVideoUrl(list.get(positionx).getVideoUrl());
//                    datum.setThumbnail(list.get(positionx).getThumbnail());
//                    datum.setPublishedTime(list.get(positionx).getPublishedTime());
//                    datum.setShares(list.get(positionx).getShares());
//                    datum.setDownloads(list.get(positionx).getDownloads());
//                    datum.setStickyPosition(list.get(positionx).getStickyPosition());
//                    datum.setDownloads(list.get(positionx).getDownloads());
//                    datum.setCommentsFlag(list.get(positionx).getCommentsFlag());
//                    datum.setWriterType(list.get(positionx).getWriterType());
//                    datum.setShareLink(list.get(positionx).getShareLink());
//                    datum.setBackgroundImage(list.get(positionx).getBackgroundImage());
//                    datum.setPostId(list.get(positionx).getPostId());
//                    datum.setWriterId(list.get(positionx).getWriterId());
//                    datum.setSubPostId(list.get(positionx).getSubPostId());
//                    datum.setWriterName(list.get(positionx).getWriterName());
//                    datum.setWriterType(list.get(positionx).getWriterType());
//                    datum.setRole(list.get(positionx).getRole());
//                    datum.setIsFollow(list.get(positionx).getIsFollow());

                    list.set(positionx, datum);

                    //half_image_dislike.setImageResource(R.drawable.ic_selected_dislike_icon);
                    half_image_like.setImageResource(R.drawable.ic_dislike_half_selected1);


                    countforshareanddownload(list.get(positionx).getPostId(), list.get(positionx).getSubPostId(), 2);


                } else if (list.get(positionx).getLikeStatus() == 2) {

                    showLog("DIS LIKE 22 " + list.get(positionx).getLikeStatus());


                    ModelClass.Datum datum = list.get(positionx);
                    datum.setLikeStatus(0);
//                    datum.setTitle(list.get(positionx).getTitle());
//                    datum.setDescription(list.get(positionx).getDescription());
//                    datum.setCategoryId(list.get(positionx).getCategoryId());
//                    datum.setPostType(list.get(positionx).getPostType());
//                    datum.setMediaType(list.get(positionx).getMediaType());
//                    datum.setImages(list.get(positionx).getImages());
//                    datum.setVideos(list.get(positionx).getVideos());
//                    datum.setVideoUrl(list.get(positionx).getVideoUrl());
//                    datum.setThumbnail(list.get(positionx).getThumbnail());
//                    datum.setPublishedTime(list.get(positionx).getPublishedTime());
//                    datum.setShares(list.get(positionx).getShares());
//                    datum.setDownloads(list.get(positionx).getDownloads());
//                    datum.setStickyPosition(list.get(positionx).getStickyPosition());
//                    datum.setDownloads(list.get(positionx).getDownloads());
//                    datum.setCommentsFlag(list.get(positionx).getCommentsFlag());
//                    datum.setWriterType(list.get(positionx).getWriterType());
//                    datum.setShareLink(list.get(positionx).getShareLink());
//                    datum.setBackgroundImage(list.get(positionx).getBackgroundImage());
//                    datum.setPostId(list.get(positionx).getPostId());
//                    datum.setWriterId(list.get(positionx).getWriterId());
//                    datum.setSubPostId(list.get(positionx).getSubPostId());
//                    datum.setWriterName(list.get(positionx).getWriterName());
//                    datum.setWriterType(list.get(positionx).getWriterType());
//                    datum.setRole(list.get(positionx).getRole());
//                    datum.setIsFollow(list.get(positionx).getIsFollow());

                    list.set(positionx, datum);


                    //half_image_dislike.setImageResource(R.drawable.ic_new_dislike_icon);


                    countforshareanddownload(list.get(positionx).getPostId(), list.get(positionx).getSubPostId(), 0);

                    //  countforshareanddownload(post_id,subPostId,0);

                } else if (list.get(positionx).getLikeStatus() == 1) {


                    ModelClass.Datum datum = new ModelClass.Datum();
                    datum.setLikeStatus(2);
                    datum.setTitle(list.get(positionx).getTitle());
                    datum.setDescription(list.get(positionx).getDescription());
                    datum.setCategoryId(list.get(positionx).getCategoryId());
                    datum.setPostType(list.get(positionx).getPostType());
                    datum.setMediaType(list.get(positionx).getMediaType());
                    datum.setImages(list.get(positionx).getImages());
                    datum.setVideos(list.get(positionx).getVideos());
                    datum.setVideoUrl(list.get(positionx).getVideoUrl());
                    datum.setThumbnail(list.get(positionx).getThumbnail());
                    datum.setPublishedTime(list.get(positionx).getPublishedTime());
                    datum.setShares(list.get(positionx).getShares());
                    datum.setDownloads(list.get(positionx).getDownloads());
                    datum.setStickyPosition(list.get(positionx).getStickyPosition());
                    datum.setDownloads(list.get(positionx).getDownloads());
                    datum.setCommentsFlag(list.get(positionx).getCommentsFlag());
                    datum.setWriterType(list.get(positionx).getWriterType());
                    datum.setShareLink(list.get(positionx).getShareLink());
                    datum.setBackgroundImage(list.get(positionx).getBackgroundImage());
                    datum.setPostId(list.get(positionx).getPostId());
                    datum.setWriterId(list.get(positionx).getWriterId());
                    datum.setSubPostId(list.get(positionx).getSubPostId());
                    datum.setWriterName(list.get(positionx).getWriterName());
                    datum.setWriterType(list.get(positionx).getWriterType());
                    datum.setRole(list.get(positionx).getRole());
                    datum.setIsFollow(list.get(positionx).getIsFollow());

                    list.set(positionx, datum);

                    half_image_like.setImageResource(R.drawable.ic_like_half1);
                    //half_image_dislike.setImageResource(R.drawable.ic_selected_dislike_icon);

                    countforshareanddownload(list.get(positionx).getPostId(), list.get(positionx).getSubPostId(), 2);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    };

    public BroadcastReceiver LIKE_PAGE = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            try {
                showLog("LIKE_PAGE");
                // All like/dislike buttons now use applyLikeChange which updates model + UI
                // directly. No action needed here.
                int newStatus = intent.getIntExtra("new_status", -1);
                if (true) return; // all handlers migrated; legacy path no longer reached
                half_image_like = findViewById(R.id.half_image_like);
                full_image_selected_like = findViewById(R.id.full_image_selected_like);
                showLog("LIKE_PAGE 1 :POSITIONX:"+positionx);
                if (list.get(positionx).getLikeStatus() == 0) {

                    ModelClass.Datum datum = list.get(positionx);

                    datum.setLikeStatus(1);

//                    datum.setTitle(list.get(positionx).getTitle());
//                    datum.setDescription(list.get(positionx).getDescription());
//                    datum.setCategoryId(list.get(positionx).getCategoryId());
//                    datum.setPostType(list.get(positionx).getPostType());
//                    datum.setMediaType(list.get(positionx).getMediaType());
//                    datum.setImages(list.get(positionx).getImages());
//                    datum.setVideos(list.get(positionx).getVideos());
//                    datum.setVideoUrl(list.get(positionx).getVideoUrl());
//                    datum.setThumbnail(list.get(positionx).getThumbnail());
//                    datum.setPublishedTime(list.get(positionx).getPublishedTime());
//                    datum.setShares(list.get(positionx).getShares());
//                    datum.setDownloads(list.get(positionx).getDownloads());
//                    datum.setStickyPosition(list.get(positionx).getStickyPosition());
//                    datum.setDownloads(list.get(positionx).getDownloads());
//                    datum.setCommentsFlag(list.get(positionx).getCommentsFlag());
//                    datum.setWriterType(list.get(positionx).getWriterType());
//                    datum.setShareLink(list.get(positionx).getShareLink());
//                    datum.setBackgroundImage(list.get(positionx).getBackgroundImage());
//                    datum.setPostId(list.get(positionx).getPostId());
//                    datum.setWriterId(list.get(positionx).getWriterId());
//                    datum.setSubPostId(list.get(positionx).getSubPostId());
//                    datum.setWriterName(list.get(positionx).getWriterName());
//                    datum.setWriterType(list.get(positionx).getWriterType());
//                    datum.setRole(list.get(positionx).getRole());
//                    datum.setIsFollow(list.get(positionx).getIsFollow());
//                    datum.setStrip_content(list.get(positionx).getStrip_content());
//                    datum.setWriterProfile(list.get(positionx).getWriterProfile());


                    list.set(positionx, datum);

                    //half_image_like.setImageDrawable((ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_like_half_selected1)));

                    //half_image_dislike.setImageResource(R.drawable.ic_new_dislike_icon);
                    //full_image_selected_like.setImageDrawable((ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_like_half_selected1)));


                    //countforshareanddownload(list.get(positionx).getPostId(), list.get(positionx).getSubPostId(), 1);

                } else if (list.get(positionx).getLikeStatus() == 1) {

                    ModelClass.Datum datum = list.get(positionx);

                    datum.setLikeStatus(2);
//                    datum.setTitle(list.get(positionx).getTitle());
//                    datum.setDescription(list.get(positionx).getDescription());
//                    datum.setCategoryId(list.get(positionx).getCategoryId());
//                    datum.setPostType(list.get(positionx).getPostType());
//                    datum.setMediaType(list.get(positionx).getMediaType());
//                    datum.setImages(list.get(positionx).getImages());
//                    datum.setVideos(list.get(positionx).getVideos());
//                    datum.setVideoUrl(list.get(positionx).getVideoUrl());
//                    datum.setThumbnail(list.get(positionx).getThumbnail());
//                    datum.setPublishedTime(list.get(positionx).getPublishedTime());
//                    datum.setShares(list.get(positionx).getShares());
//                    datum.setDownloads(list.get(positionx).getDownloads());
//                    datum.setStickyPosition(list.get(positionx).getStickyPosition());
//                    datum.setDownloads(list.get(positionx).getDownloads());
//                    datum.setCommentsFlag(list.get(positionx).getCommentsFlag());
//                    datum.setWriterType(list.get(positionx).getWriterType());
//                    datum.setShareLink(list.get(positionx).getShareLink());
//                    datum.setBackgroundImage(list.get(positionx).getBackgroundImage());
//                    datum.setPostId(list.get(positionx).getPostId());
//                    datum.setWriterId(list.get(positionx).getWriterId());
//                    datum.setSubPostId(list.get(positionx).getSubPostId());
//                    datum.setWriterName(list.get(positionx).getWriterName());
//                    datum.setWriterType(list.get(positionx).getWriterType());
//                    datum.setRole(list.get(positionx).getRole());
//                    datum.setIsFollow(list.get(positionx).getIsFollow());
//                    datum.setStrip_content(list.get(positionx).getStrip_content());
//                    datum.setWriterProfile(list.get(positionx).getWriterProfile());

                    list.set(positionx, datum);
                   // half_image_like.setImageDrawable((ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_dislike_half_selected1)));
                    //full_image_selected_like.setImageDrawable((ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_dislike_half_selected1)));

                    //countforshareanddownload(list.get(positionx).getPostId(), list.get(positionx).getSubPostId(), 2);

                } else if (list.get(positionx).getLikeStatus() == 2) {


                    ModelClass.Datum datum = list.get(positionx);


                    datum.setLikeStatus(0);
//                    datum.setTitle(list.get(positionx).getTitle());
//                    datum.setDescription(list.get(positionx).getDescription());
//                    datum.setCategoryId(list.get(positionx).getCategoryId());
//                    datum.setPostType(list.get(positionx).getPostType());
//                    datum.setMediaType(list.get(positionx).getMediaType());
//                    datum.setImages(list.get(positionx).getImages());
//                    datum.setVideos(list.get(positionx).getVideos());
//                    datum.setVideoUrl(list.get(positionx).getVideoUrl());
//                    datum.setThumbnail(list.get(positionx).getThumbnail());
//                    datum.setPublishedTime(list.get(positionx).getPublishedTime());
//                    datum.setShares(list.get(positionx).getShares());
//                    datum.setDownloads(list.get(positionx).getDownloads());
//                    datum.setStickyPosition(list.get(positionx).getStickyPosition());
//                    datum.setDownloads(list.get(positionx).getDownloads());
//                    datum.setCommentsFlag(list.get(positionx).getCommentsFlag());
//                    datum.setWriterType(list.get(positionx).getWriterType());
//                    datum.setShareLink(list.get(positionx).getShareLink());
//                    datum.setBackgroundImage(list.get(positionx).getBackgroundImage());
//                    datum.setPostId(list.get(positionx).getPostId());
//                    datum.setWriterId(list.get(positionx).getWriterId());
//                    datum.setSubPostId(list.get(positionx).getSubPostId());
//                    datum.setWriterName(list.get(positionx).getWriterName());
//                    datum.setWriterType(list.get(positionx).getWriterType());
//                    datum.setRole(list.get(positionx).getRole());
//                    datum.setIsFollow(list.get(positionx).getIsFollow());
//                    datum.setStrip_content(list.get(positionx).getStrip_content());
//                    datum.setWriterProfile(list.get(positionx).getWriterProfile());

                    list.set(positionx, datum);
                    //half_image_dislike.setImageResource(R.drawable.ic_new_dislike_icon);
                   // half_image_like.setImageDrawable((ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_like_half1)));
                    //full_image_selected_like.setImageDrawable((ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_like_half1)));

                    //countforshareanddownload(list.get(positionx).getPostId(), list.get(positionx).getSubPostId(), 0);

                }
                adapter.notifyDataSetChanged();

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    };
    public BroadcastReceiver SURVEY_POLL = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            initialCall=false;
            try {

                int optionID = intent.getExtras().getInt("extra");


                ModelClass.Datum datum = list.get(positionx);
                datum.setSelected_option(optionID);
//                datum.setLikeStatus(list.get(positionx).getLikeStatus());
//                datum.setTitle(list.get(positionx).getTitle());
//                datum.setDescription(list.get(positionx).getDescription());
//                datum.setCategoryId(list.get(positionx).getCategoryId());
//                datum.setPostType(list.get(positionx).getPostType());
//                datum.setMediaType(list.get(positionx).getMediaType());
//                datum.setImages(list.get(positionx).getImages());
//                datum.setVideos(list.get(positionx).getVideos());
//                datum.setVideoUrl(list.get(positionx).getVideoUrl());
//                datum.setThumbnail(list.get(positionx).getThumbnail());
//                datum.setPublishedTime(list.get(positionx).getPublishedTime());
//                datum.setShares(list.get(positionx).getShares());
//                datum.setDownloads(list.get(positionx).getDownloads());
//                datum.setStickyPosition(list.get(positionx).getStickyPosition());
//                datum.setDownloads(list.get(positionx).getDownloads());
//                datum.setCommentsFlag(list.get(positionx).getCommentsFlag());
//                datum.setWriterType(list.get(positionx).getWriterType());
//                datum.setShareLink(list.get(positionx).getShareLink());
//                datum.setBackgroundImage(list.get(positionx).getBackgroundImage());
//                datum.setPostId(list.get(positionx).getPostId());
//                datum.setWriterId(list.get(positionx).getWriterId());
//                datum.setSubPostId(list.get(positionx).getSubPostId());
//                datum.setWriterName(list.get(positionx).getWriterName());
//                datum.setWriterType(list.get(positionx).getWriterType());
//                datum.setRole(list.get(positionx).getRole());
//                datum.setIsFollow(list.get(positionx).getIsFollow());
//                datum.setStrip_content(list.get(positionx).getStrip_content());
//                datum.setWriterProfile(list.get(positionx).getWriterProfile());


                List<ModelClass.SurveyOptions> surveyOptionsArrayList = new ArrayList<>();
                surveyOptionsArrayList.clear();
                ModelClass.SurveyOptions surveyOptions = new ModelClass.SurveyOptions();
                for (int i = 0; i < list.get(positionx).getSurveyOptions().size(); i++) {

                    surveyOptions.setId(list.get(positionx).getSurveyOptions().get(i).getId());
                    surveyOptions.setPercent(list.get(positionx).getSurveyOptions().get(i).getPercent());
                    surveyOptions.setSurveyOptionsName(i + " A ");
                    surveyOptions.setCount(list.get(positionx).getSurveyOptions().get(i).getCount());
                    surveyOptionsArrayList.add(surveyOptions);
                    showLog("UPDATED VALUES 1" + list.get(positionx).getSurveyOptions().size() + " , " + optionID);
                }

                datum.setSurveyOptions(surveyOptionsArrayList);
                list.set(positionx, datum);


            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    };
    public BroadcastReceiver CURRENT_LOCATION = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            initialCall=false;

            try {
                showLog("CURRENT_LOCATION category_ID:"+category_id);


                if (category_id == 0) {

                    mFlipView.setVisibility(View.VISIBLE);
                    showHomeFeedLoadingReveal();
                    layout_with_data.setVisibility(View.GONE);
                    comment_page.setVisibility(View.GONE);
                    last_post_id = 0;
                    skip = 0;
                    actual_skip = 0;
                    category_id = -1;
                    requestPostition=1;
                    requestSize=0;
                    isFromLocalArticles=true;
                    positionx=0;
                    list=new ArrayList<>();
                    dummyResponse=null;
                    inlist(-1);


                } else {
                  /*  un_selected_local.setVisibility(View.VISIBLE);

                    un_selected_my_feed.setVisibility(View.GONE);
                    selected_my_feed.setVisibility(View.VISIBLE);
                    selected_local.setVisibility(View.GONE);*/

                    showHomeFeedLoadingReveal();
                    layout_with_data.setVisibility(View.GONE);
                    comment_page.setVisibility(View.GONE);

                    /*linear_home_image.setImageResource(R.drawable.ic_new_home);*/
                    /*linear_home_text.setText("" + languageModelResponse.getData().getHome());*/
                    list=new ArrayList<>();
                    last_post_id = 0;
                    skip = 0;
                    actual_skip = 0;
                    category_id = 0;
                    requestPostition=1;
                    requestSize=0;
                    isFromLocalArticles=true;
                    positionx=0;
                    requestPostition=1;
                    dummyResponse=null;
                    inlist(0);
                }

                writeButtonClick("User", type_firebase_button, current_time_firebase, draft_id_firebase, device_id_firebase, button_name_local);

                LocalBroadcastManager.getInstance(HomePage.this).unregisterReceiver(CURRENT_LOCATION);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        LocalBroadcastManager.getInstance(getContext()).registerReceiver(CURRENT_LOCATION,
                                new IntentFilter("CURRENT_LOCATION"));
                    }
                },4000);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    };

    public BroadcastReceiver COMMENT_PAGE_ACTIVE = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean value = intent.getBooleanExtra("message", true);
            if (value) {


                top_item_layout.setVisibility(View.GONE);

            } else {
                top_item_layout.setVisibility(View.GONE);
            }
        }

    };

    public BroadcastReceiver COMMENTS_PAGE = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String value = intent.getStringExtra("message");

            if (comment_page.isShown()) {
                comment_page.setVisibility(View.GONE);
            } else {
                showLog("COMMENTS_PAGE");
                showProgress();
                comment_page.setVisibility(View.VISIBLE);
                comment_recycler_view.setVisibility(View.GONE);
                comment_totoal_count.setVisibility(View.GONE);

            }

        }

    };

    public void ininointernet() {
        no_internet = findViewById(R.id.no_internet);
        yes_internet = findViewById(R.id.yes_internet);

    }

    public void incomment() {

        layoutCommentProgressbar = findViewById(R.id.layoutCommentProgressbar);
        commet_close = findViewById(R.id.commet_close);
        comment_page = findViewById(R.id.comment_page);
        comment_page.setVisibility(View.GONE);
        comment_recycler_view = findViewById(R.id.comment_recycler_view);
        comment_progress = findViewById(R.id.send_progress);

        comment_totoal_count = findViewById(R.id.comment_totoal_count);
        leave_comment_white = findViewById(R.id.leave_comment_white);
        send_btn_layout = findViewById(R.id.send_btn_layout);
        goHomePage = findViewById(R.id.goHomePage);
        comment_page.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLog("comment writtenr");
            }
        });

        commet_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //   player.setPlayWhenReady(true);
                comment_page.setVisibility(View.GONE);
                leave_comment_white.setText("");
                CommonMethods commonMethods = new CommonMethods(HomePage.this);

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(leave_comment_white.getWindowToken(), 0);
            }
        });


        send_btn_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(leave_comment_white.getText().toString().equalsIgnoreCase("") && leave_comment_white.getText().toString().length()<=0){
                    Toast.makeText(HomePage.this,"Comment is not empty",Toast.LENGTH_LONG).show();
                    return;
                }

                if (leave_comment_white.getText().toString().length() >= 1) {
                    if(allsharedpreference.getEmailId()!=null && !allsharedpreference.getEmailId().equalsIgnoreCase("")){
                        postComment();
                    }else{
                        try {
                            Intent intent =
                                    AccountPicker.newChooseAccountIntent(
                                            new AccountPicker.AccountChooserOptions.Builder()
                                                    .setAllowableAccountsTypes(Arrays.asList("com.google"))
                                                    .setAlwaysShowAccountPicker(true)
                                                    .setTitleOverrideText("Choose Account For AnyNews")
                                                    .build());
                            postCommentActivityListerner.launch(intent);
                        } catch (ActivityNotFoundException e) {
                            // TODO
                        }
                    }

                } else {
                    ((InputMethodManager) HomePage.this.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(leave_comment_white.getWindowToken(), 0);

                }
            }
        });
        goHomePage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLog("Check Point #1 : HomePage.setOnClickListener ");

                Intent intent = new Intent(HomePage.this, HomePage.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                if (getApplicationContext() != null && getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)) {
                    startActivity(intent);
                }
                finish();
            }
        });



        textViewCommentsTag = findViewById(R.id.textViewCommentsTag);
        textViewCommentsTag.setText("Remember to follow our Terms and Conditions and be respectful when messaging other");
        Spannable spannable = new SpannableString("Remember to follow our Terms and Conditions and be respectful when messaging other");
        String str = spannable.toString();
        int iStart = str.indexOf("Terms and Conditions");
        int length = "Terms and Conditions".length();
        int iEnd = iStart + length;/*10 characters = in-network. */
        SpannableStringBuilder ssText = new SpannableStringBuilder(spannable);

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                comment_page.setVisibility(View.GONE);
                Intent intent = new Intent(HomePage.this, PrivacyPolicy.class);
                startActivity(intent);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
                ds.setColor(0xFF3B5998);
            }
        };
        try {
            ssText.setSpan(clickableSpan, iStart, iEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            int index = str.indexOf(descriptionDirectionLink) - 1;
            ssText = ssText.replace(index, iStart, " ");
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        textViewCommentsTag.setText(ssText);
        textViewCommentsTag.setMovementMethod(new MovementMethodTV());

    }
    public void postComment(){

        ((InputMethodManager) HomePage.this.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(leave_comment_white.getWindowToken(), 0);

        //   comment_progress.setVisibility(View.VISIBLE);
        int count = comment_chooseLanguage_models.size() + 1;
        comment_totoal_count.setText(" Comments ( " + count + " )");
        WriteComments();
    }

    @Override
    public void OpenComments(Long post_id, Long sub_post_id) {

            showProgress();

        comment_chooseLanguage_models = new ArrayList<>();
        comment_chooseLanguage_models.clear();
        JsonObject otp = getAllCommentRequestData(post_id, sub_post_id);
        linear_no_comment_page.setVisibility(View.GONE);
        APIService apiService = RestApiClient.getClient().create(APIService.class);
        Call<EncreptionRequest> otp_generation = apiService.List_All_Comment_For_Article("v3", otp);
        try {
            otp_generation.enqueue(new Callback<EncreptionRequest>() {

                @Override
                public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {
                    EncreptionRequest encreptionRequest = response.body();
                    String encrpt_data = null;
                    /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        encrpt_data = decrypt(encreptionRequest.getData());
                    }*/
                    encrpt_data = decrypt(encreptionRequest.getData());
                    showLog("comments response:"+encrpt_data);
                    Gson gsonObj = new Gson();
                    ListOfCommentsResponse validateUserOtp = gsonObj.fromJson(encrpt_data, ListOfCommentsResponse.class);

                    hideProgress();
                    if (validateUserOtp.getSuccess()) {
                        if (validateUserOtp.getData().size() == 0) {
                            comment_recycler_view.setVisibility(View.INVISIBLE);
                            linear_no_comment_page.setVisibility(View.VISIBLE);

                        } else {
                            for (int i = 0; i < validateUserOtp.getData().size(); i++) {
                                ListOfCommentsResponse.Datum datum = new ListOfCommentsResponse.Datum();
                                datum.setId(validateUserOtp.getData().get(i).getId());
                                datum.setPostId(validateUserOtp.getData().get(i).getPostId());
                                datum.setDraftId(validateUserOtp.getData().get(i).getDraftId());
                                datum.setUserName(validateUserOtp.getData().get(i).getUserName());
                                datum.setComment(validateUserOtp.getData().get(i).getComment());
                                datum.setCreatedAt(validateUserOtp.getData().get(i).getCreatedAt());
                                datum.setCommentTime(validateUserOtp.getData().get(i).getCommentTime());
                                datum.setUser_profile_pic(validateUserOtp.getData().get(i).getUser_profile_pic());
                                datum.setEmail(validateUserOtp.getData().get(i).getEmail());
                                comment_chooseLanguage_models.add(datum);
                            }


                            //   comment_progress.setVisibility(View.GONE);

                            comment_linearlayoutmanager = new LinearLayoutManager(getContext());
                            comment_recycler_view.setLayoutManager(comment_linearlayoutmanager);
                            comment_totoal_count.setVisibility(View.VISIBLE);
                            comment_recycler_view.setVisibility(View.VISIBLE);
                            linear_no_comment_page.setVisibility(View.GONE);
                            MailAdapter mMailAdapter = new MailAdapter(
                                    HomePage.this,
                                    comment_chooseLanguage_models,
                                    position -> openReportPage(position),
                                    position -> { },
                                    position -> { },
                                    position -> { });
                            comment_recycler_view.setAdapter(mMailAdapter);
                            comment_totoal_count.setText(" Comments ( " + comment_chooseLanguage_models.size() + " )");
                            //        leave_comment_white.setText(" " + languageModelResponse.getData().getLeaveAComment());
                        }


                    } else {


                    }

                }

                @Override
                public void onFailure(Call<EncreptionRequest> call, Throwable t) {
                    hideProgress();


                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void shareInWhatsApp(SharePostToWhatsApp sharePostToWhatsApp) {
        this.sharePostToWhatsApp = sharePostToWhatsApp;

    }

    @Override
    public void closeBottomStrip(boolean closeStrip) {
        showLog("closeBottomStrip:"+closeStrip);
        updateHomePageNavigationForSeat();
    }

    private void openReportPage(int pos) {
        if(allsharedpreference.getEmailId()!=null && !allsharedpreference.getEmailId().equalsIgnoreCase("")){
            ListOfCommentsResponse.Datum lDatum=  comment_chooseLanguage_models.get(pos);
            CommentsBottomSheetDialog commentsBottomSheetDialog =
                    CommentsBottomSheetDialog.newInstance();
            Bundle bundle=new Bundle();
            bundle.putSerializable("lDatum",lDatum);

            commentsBottomSheetDialog.setArguments(bundle);
            commentsBottomSheetDialog.show(getSupportFragmentManager(),
                    commentsBottomSheetDialog.TAG);
        }else{

            try {
                Intent intent =
                        AccountPicker.newChooseAccountIntent(
                                new AccountPicker.AccountChooserOptions.Builder()
                                        .setAllowableAccountsTypes(Arrays.asList("com.google"))
                                        .setAlwaysShowAccountPicker(true)
                                        .setTitleOverrideText("Choose Account For AnyNews")
                                        .build());
                someActivityResultLauncher.launch(intent);
            } catch (ActivityNotFoundException e) {
                // TODO
            }

        }
    }

    @Override
    public void reportBottomSheetDismiss() {
        try {
            comment_page.setVisibility(View.GONE);
            leave_comment_white.setText("");
            CommonMethods commonMethods = new CommonMethods(HomePage.this);
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(leave_comment_white.getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onFlippedToAppear(FlipView v) {
        Logone.v(LOG_TAG, ""+v.getAdapter());
        showLog("onFlippedToAppear");
    }
    public  void pauseVideo(){
        try {
            FlipperAdapter.pauseVideo();
        } catch (Exception ignored) {
        }
        try{
            showLog("youTubePlayerViewHalf pause");
            if (FlipperAdapter.youTubePlayerViewHalf != null) {
                showLog("youTubePlayerViewHalf pause");
                FlipperAdapter. youTubePlayerViewHalf.pause();
                if(adapter != null && adapter.customPlayerUiController!=null){
                    adapter.customPlayerUiController.showUiControlls();
                }
                if(adapter != null && adapter.fullVideocustomPlayerUiController!=null){
                    adapter.fullVideocustomPlayerUiController.showUiControlls();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        try{
            if (FlipperAdapter.exoPlayer != null) {
                FlipperAdapter.exoPlayer.setPlayWhenReady(false);
                try {
                    FlipperAdapter.exoPlayer.pause();
                } catch (Exception ignored) {
                }
                FlipperAdapter.exoPlayer.stop();
                FlipperAdapter.exoPlayer.seekTo(0);
                //youTubePlayerViewHalf.cueVideo(videoURL,0);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        try {
            if (adapter != null) {
                adapter.muteActiveNativeAdAudio();
                adapter.pauseAdMediaForLeaveGesture(positionx);
            }
        } catch (Exception ignored) {
        }
        try {
            if (adapter != null && mFlipView != null) {
                adapter.pauseOffscreenMedia(mFlipView);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void OnFlipCompletedListener(FlipView v) {
        showLog("OnFlipCompletedListener:::::::::");
    }

    public class NetworkChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            Logone.v(LOG_TAG, "Receieved notification about network status");
            isNetworkAvailable(context);
        }


        private boolean isNetworkAvailable(Context context) {
            ConnectivityManager connectivity = (ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity != null) {
                NetworkInfo[] info = connectivity.getAllNetworkInfo();
                if (info != null) {
                    for (int i = 0; i < info.length; i++) {
                        if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                            if (!isConnected) {
                                Logone.v(LOG_TAG, "Now you are connected to Internet!");
                                //    networkStatus.setText("Now you are connected to Internet!");
                                yes_internet.setVisibility(View.VISIBLE);
                                no_internet.setVisibility(View.GONE);

                                isConnected = true;
                                //do your processing here ---
                                //if you need to post any data to the server or get status
                                //update from the server
                            }
                            return true;
                        }
                    }
                }
            }


            Logone.v(LOG_TAG, "You are not connected to Internet!");
            yes_internet.setVisibility(View.GONE);
            no_internet.setVisibility(View.VISIBLE);

            //   one.setText("You are not connected to Internet!");
            isConnected = false;
            return false;
        }
    }

    private JsonObject getCommentRequestData() {

        long post_id = list.get(positionx).getPostId();
        long sub_post_id = list.get(positionx).getSubPostId();
        ListOfCommentsRequest requestModel = new ListOfCommentsRequest();
        requestModel.setDraftId(SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME));
        requestModel.setPostId(post_id);
        requestModel.setSubPostId(sub_post_id);
        requestModel.setUserid(allsharedpreference.getEmailId());
        requestModel.setComment("" + leave_comment_white.getText().toString());

        String jsonString = new com.google.gson.Gson().toJson(requestModel);
        String encrpt_data = null;
        showLog("API COMMENT REQUEST :" + jsonString.toString());
        encrpt_data = encrypt(jsonString);
        EncritonRequestData Encriton_Request = new EncritonRequestData();
        Encriton_Request.setData(encrpt_data);
        return new Gson().toJsonTree(Encriton_Request).getAsJsonObject();
    }

    private void WriteComments() {

        linear_no_comment_page.setVisibility(View.GONE);
        JsonObject otp = getCommentRequestData();
        showProgress();

        APIService apiService = RestApiClient.getClient().create(APIService.class);

        Call<EncreptionRequest> otp_generation = apiService.comment("v3", otp);
        try {
            otp_generation.enqueue(new Callback<EncreptionRequest>() {

                //@RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {
                    hideProgress();

                    EncreptionRequest encreptionRequest = response.body();
                    String encrpt_data = null;
                    /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        encrpt_data = decrypt(encreptionRequest.getData());
                    }*/
                    encrpt_data = decrypt(encreptionRequest.getData());

                    Gson gsonObj = new Gson();
                    CommentResponuse validateUserOtp = gsonObj.fromJson(encrpt_data, CommentResponuse.class);


                    if (validateUserOtp.getSuccess()) {

                        leave_comment_white.setText("");

                        comment_recycler_view.setVisibility(View.GONE);
                        comment_totoal_count.setVisibility(View.GONE);

                        long post_id = list.get(positionx).getPostId();
                        long sub_post_id = list.get(positionx).getSubPostId();

                        OpenComment(post_id, sub_post_id);


                    } else {


                    }

                }

                @Override
                public void onFailure(Call<EncreptionRequest> call, Throwable t) {
                    hideProgress();


                }
            });
        } catch (Exception e) {
        }
    }

    private JsonObject getAllCommentRequestData(long post_id, long sub_post_id) {

        ListOfCommentsRequest requestModel = new ListOfCommentsRequest();
        requestModel.setDraftId(SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME));
        requestModel.setPostId(post_id);
        requestModel.setSubPostId(sub_post_id);
        requestModel.setUserid(allsharedpreference.getEmailId());


        String jsonString = new com.google.gson.Gson().toJson(requestModel);
        String encrpt_data = null;
        showLog("API REQUEST E" + jsonString.toString());
        encrpt_data = encrypt(jsonString);
        EncritonRequestData Encriton_Request = new EncritonRequestData();
        Encriton_Request.setData(encrpt_data);
        return new Gson().toJsonTree(Encriton_Request).getAsJsonObject();
    }

    private void OpenComment(long post_id, long sub_post_id) {
        showProgress();
        comment_chooseLanguage_models = new ArrayList<>();
        comment_chooseLanguage_models.clear();
        JsonObject otp = getAllCommentRequestData(post_id, sub_post_id);
        linear_no_comment_page.setVisibility(View.GONE);


        APIService apiService = RestApiClient.getClient().create(APIService.class);
        Call<EncreptionRequest> otp_generation = apiService.List_All_Comment_For_Article("v3", otp);
        try {
            otp_generation.enqueue(new Callback<EncreptionRequest>() {

                @Override
                public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {
                    hideProgress();
                    EncreptionRequest encreptionRequest = response.body();
                    String encrpt_data = null;
                    /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        encrpt_data = decrypt(encreptionRequest.getData());
                    }*/
                    encrpt_data = decrypt(encreptionRequest.getData());
                    showLog("comments response:"+encrpt_data);

                    Gson gsonObj = new Gson();
                    ListOfCommentsResponse validateUserOtp = gsonObj.fromJson(encrpt_data, ListOfCommentsResponse.class);
                    if (validateUserOtp.getSuccess()) {
                        if (validateUserOtp.getData().size() == 0) {
                            comment_recycler_view.setVisibility(View.INVISIBLE);
                            linear_no_comment_page.setVisibility(View.VISIBLE);
                        } else {
                            for (int i = 0; i < validateUserOtp.getData().size(); i++) {
                                ListOfCommentsResponse.Datum datum = new ListOfCommentsResponse.Datum();
                                datum.setId(validateUserOtp.getData().get(i).getId());
                                datum.setPostId(validateUserOtp.getData().get(i).getPostId());
                                datum.setDraftId(validateUserOtp.getData().get(i).getDraftId());
                                datum.setUserName(validateUserOtp.getData().get(i).getUserName());
                                datum.setComment(validateUserOtp.getData().get(i).getComment());
                                datum.setCreatedAt(validateUserOtp.getData().get(i).getCreatedAt());
                                datum.setCommentTime(validateUserOtp.getData().get(i).getCommentTime());
                                datum.setUser_profile_pic(validateUserOtp.getData().get(i).getUser_profile_pic());
                                datum.setEmail(validateUserOtp.getData().get(i).getEmail());
                                comment_chooseLanguage_models.add(datum);
                            }


                            //   comment_progress.setVisibility(View.GONE);

                            comment_linearlayoutmanager = new LinearLayoutManager(getContext());
                            comment_recycler_view.setLayoutManager(comment_linearlayoutmanager);
                            comment_totoal_count.setVisibility(View.VISIBLE);
                            comment_recycler_view.setVisibility(View.VISIBLE);
                            linear_no_comment_page.setVisibility(View.GONE);
                            MailAdapter mMailAdapter = new MailAdapter(
                                    HomePage.this,
                                    comment_chooseLanguage_models,
                                    position -> openReportPage(position),
                                    position -> { },
                                    position -> { },
                                    position -> { });
                            comment_recycler_view.setAdapter(mMailAdapter);
                            comment_totoal_count.setText(" Comments ( " + comment_chooseLanguage_models.size() + " )");
                            //        leave_comment_white.setText(" " + languageModelResponse.getData().getLeaveAComment());
                        }


                    } else {


                    }

                }

                @Override
                public void onFailure(Call<EncreptionRequest> call, Throwable t) {
                    hideProgress();


                }
            });

        } catch (Exception e) {
        }


    }

    public void inlist(int category_id) {

        list = new ArrayList<>();
        dummyResponse=null;
        showLog("inlist called:"+category_id);
        initialCall=false;
        ShowNews_Data_Values(category_id,-1);

        try {

            allsharedpreference = new Allsharedpreference(HomePage.this);
            Gson gson = new Gson();
            String Translation = allsharedpreference.getShared_String(Allsharedpreference.Translator);
            if (Translation != null && !Translation.isEmpty()) {
                languageModelResponse = gson.fromJson(Translation, LanguageModelResponse.class);
            }

            if (languageModelResponse != null && languageModelResponse.getData() != null) {
                showLog("");
                showLog("Check Point #6 language response is not null");
                my_feed.setText("" + languageModelResponse.getData().getMyFeed());
                my_feed_text.setText("" + languageModelResponse.getData().getMyFeed());

                try {
                    FooterTabLabels.apply(this, bottomNavigationView, languageModelResponse.getData());
                } catch (Exception ignored) {
                    bottomNavigationView.getMenu().getItem(0).setTitle("" + languageModelResponse.getData().getReload());
                    bottomNavigationView.getMenu().getItem(1).setTitle("" + languageModelResponse.getData().getEvents());
                    bottomNavigationView.getMenu().getItem(3).setTitle("" + languageModelResponse.getData().getCategory());
                    bottomNavigationView.getMenu().getItem(4).setTitle("" + languageModelResponse.getData().getProfile());
                }
                fab_fullpost.setText("" + languageModelResponse.getData().getFullPost());
                fab_video.setText("" + languageModelResponse.getData().getVideoPost());
                fab_article.setText("" + languageModelResponse.getData().getArticle());
                nodatascreen.setText("" + languageModelResponse.getData().getNoData());
                leave_comment_white.setHint("" + languageModelResponse.getData().getWriteComments());

                if (category_id == 0) {
                    type = "" + String.valueOf(SharedPrefsData.getString(HomePage.this, Constants.Selected_Distric_Name1, Constants.PREF_NAME));
                } else {
                    type = languageModelResponse.getData().getMyFeed().toString();
                }
                // Do not steal selection while Daily Updates covers the feed.
                if (!isDailyUpdatesMode()) {
                    if (SharedPrefsData.getBool(HomePage.this, Constants.isUnreadPostsSelected, Constants.PREF_NAME)) {
                        showLog("setSelectedItemId(R.id.navigation_catagiries)");
                        selectFooterTabWithoutMotion(R.id.navigation_catagiries);
                    } else {
                        showLog("setSelectedItemId(R.id.navigation_home)");
                        selectFooterTabWithoutMotion(R.id.navigation_home);
                    }
                }

                showLog("List size:"+list.size());

                ensureFlipperAdapter(category_id);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void ensureFlipperAdapter(int category_id) {
        if (adapter != null) {
            return;
        }
        if (languageModelResponse == null || languageModelResponse.getData() == null) {
            return;
        }
        if (category_id == 0) {
            type = "" + String.valueOf(SharedPrefsData.getString(HomePage.this, Constants.Selected_Distric_Name1, Constants.PREF_NAME));
        } else {
            type = languageModelResponse.getData().getMyFeed().toString();
        }
        adapter = new FlipperAdapter(Activity, HomePage.this, type, category_id, getLifecycle());
        adapter.setCallback(this);
        if (adScreenArticleOverlay != null) {
            adScreenArticleOverlay.setFeedAdapter(adapter);
        }
        mFlipView.setAdapter(adapter);
        mFlipView.setOnFlipListener(this);
        mFlipView.setOverFlipMode(OverFlipMode.GLOW);
        mFlipView.peakNext(false);
        mFlipView.setOnOverFlipListener(this);
        mFlipView.setOnFlipAppear(this);
    }


    private JsonObject Articles_All(int category_id) {
        int height=0,width=0;
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        width = displayMetrics.widthPixels;
        height = displayMetrics.heightPixels;
        int isPermissionGranted=0;
        int SDK_VERSION=Build.VERSION.SDK_INT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                isPermissionGranted=0;
            }else{
                isPermissionGranted=1;
            }
        }else{
            isPermissionGranted=1;
        }
        try {

            TITLE_VALUE = Integer.parseInt(pushUpdatedID);

        } catch (NumberFormatException e) {
            showLog("not a number");
        }

        ListAllArticlesRequest    requestModel = new ListAllArticlesRequest();
        requestModel.setDraftId(SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME));
        requestModel.setCategoryId(category_id);
        requestModel.setLanguageId(SharedPrefsData.getInt(getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME));
        requestModel.setSkip((long) skip);
        requestModel.setLastPostId((long) last_post_id);
        requestModel.setPushPostId((long) TITLE_VALUE);
        requestModel.setReferred_post_id(SharedPrefsData.getString(getApplicationContext(), Constants.REFERRER_POST,  Constants.PREF_NAME));
        requestModel.setSharePostId((long) SHARE_ID);
        requestModel.setActualSkip((long) actual_skip);
        requestModel.setAdvertiserID(allsharedpreference.getAdvertiserId(Allsharedpreference.ADVERTISER_ID));
        requestModel.setDatafrom2g(DATAFROM2G);
        requestModel.setSpecial_posts(SharedPrefsData.getBool(HomePage.this, Constants.isUnreadPostsSelected, Constants.PREF_NAME));
        requestModel.setScreenHeight(height);
        requestModel.setScreenWidth(width);
        requestModel.setUser_session_id(SharedPrefsData.getString(getApplicationContext(), Constants.USER_SESSION_ID, Constants.PREF_NAME));
        requestModel.setApp_version_name(""+BuildConfig.VERSION_NAME);
        requestModel.setApp_version_number(""+BuildConfig.VERSION_CODE);
        requestModel.setSdk_version(SDK_VERSION);
        requestModel.setNotif_perm_allowed(isPermissionGranted);

        String jsonString = new com.google.gson.Gson().toJson(requestModel);
        String encrpt_data = null;
        encrpt_data = encrypt(jsonString);
        showLog("API REQUEST E" + jsonString.toString());
        EncritonRequestData Encriton_Request = new EncritonRequestData();
        Encriton_Request.setData(encrpt_data);
        return new Gson().toJsonTree(Encriton_Request).getAsJsonObject();

    }

    private void cancelListAllArticlesCall() {
        try {
            if (listAllArticlesCall != null && !listAllArticlesCall.isCanceled()) {
                listAllArticlesCall.cancel();
            }
        } catch (Exception ignored) {
        }
        listAllArticlesCall = null;
    }

    private void ShowNews_Data_Values(int category_id,int reqPos) {
        showLog("API CATEGORY ID:" + category_id);
        showLog("API CATEGORY REQPOS:" + reqPos);
        // N2: cancel prior in-flight feed call; ignore stale callbacks via gen.
        cancelListAllArticlesCall();
        final int gen = ++listAllArticlesGen;

        boolean special = SharedPrefsData.getBool(
                HomePage.this, Constants.isUnreadPostsSelected, Constants.PREF_NAME);
        boolean video = SharedPrefsData.getBool(
                HomePage.this, Constants.isVideoPostsSelected, Constants.PREF_NAME);
        boolean search = isSearchFeedActive();
        boolean useEdge = PublicEdgeFeedHelper.canUsePublicEdge(
                search, special, video, flashNewsModeActive, category_id);

        long pinPostId = 0L;
        try {
            pinPostId = Math.max((long) TITLE_VALUE, SHARE_ID);
        } catch (Exception ignored) {
            pinPostId = SHARE_ID;
        }

        if (useEdge) {
            try {
                final long pinForWrap = pinPostId;
                Call<PublicEdgeFeedResponse> edgeCall =
                        PublicEdgeFeedHelper.enqueuePublicFeedCoalesced(
                                getApplicationContext(),
                                category_id,
                                skip,
                                pinPostId,
                                special,
                                flashNewsModeActive,
                                new Callback<PublicEdgeFeedResponse>() {
                        @Override
                        public void onResponse(
                                Call<PublicEdgeFeedResponse> call,
                                Response<PublicEdgeFeedResponse> response) {
                            if (gen != listAllArticlesGen || isFinishing() || isDestroyed()) {
                                return;
                            }
                            if (call != null && call.isCanceled()) {
                                return;
                            }
                            PublicEdgeFeedResponse body =
                                    response != null ? response.body() : null;
                            if (response != null
                                    && response.isSuccessful()
                                    && PublicEdgeFeedHelper.isAcceptableEdgeResponse(
                                            body, skip)) {
                                EncreptionRequest enc =
                                        PublicEdgeFeedHelper.toEncreptionRequest(
                                                getApplicationContext(), body, pinForWrap);
                                if (enc != null) {
                                    convertListOfArticles(Response.success(enc), reqPos);
                                    return;
                                }
                            }
                            // Soft-fall to encrypted List (Node → Redis → Mongo).
                            PublicEdgeFeedHelper.reportEdgeOriginBeacon(
                                    getApplicationContext(),
                                    "client_main_local_list_fallback",
                                    pinForWrap,
                                    SharedPrefsData.getInt(
                                            getApplicationContext(),
                                            Constants.LanguageID,
                                            Constants.PREF_NAME),
                                    SharedPrefsData.getInt(
                                            getApplicationContext(),
                                            Constants.StateID,
                                            Constants.PREF_NAME),
                                    SharedPrefsData.getInt(
                                            getApplicationContext(),
                                            Constants.Selected_Distric_Id,
                                            Constants.PREF_NAME));
                            enqueueListAllArticlesOrigin(
                                    category_id, reqPos, gen, true);
                        }

                        @Override
                        public void onFailure(
                                Call<PublicEdgeFeedResponse> call, Throwable t) {
                            if (gen != listAllArticlesGen) {
                                return;
                            }
                            if (call != null && call.isCanceled()) {
                                return;
                            }
                            PublicEdgeFeedHelper.reportEdgeOriginBeacon(
                                    getApplicationContext(),
                                    "client_main_local_list_fallback",
                                    pinForWrap,
                                    SharedPrefsData.getInt(
                                            getApplicationContext(),
                                            Constants.LanguageID,
                                            Constants.PREF_NAME),
                                    SharedPrefsData.getInt(
                                            getApplicationContext(),
                                            Constants.StateID,
                                            Constants.PREF_NAME),
                                    SharedPrefsData.getInt(
                                            getApplicationContext(),
                                            Constants.Selected_Distric_Id,
                                            Constants.PREF_NAME));
                            enqueueListAllArticlesOrigin(
                                    category_id, reqPos, gen, true);
                        }
                    });
                if (edgeCall != null) {
                    showLog("API public edge feed cat=" + category_id + " skip=" + skip);
                    listAllArticlesCall = edgeCall;
                    return;
                }
            } catch (Exception e) {
                showLog("public edge enqueue failed — List fallback");
            }
        }

        enqueueListAllArticlesOrigin(category_id, reqPos, gen, false);
    }

    private void annotateIntentionalListOrigin(int category_id) {
        try {
            if (flashNewsModeActive) {
                EdgeOriginCallWatch.annotateExpectedListOrigin("expected_flash_feed");
            } else {
                boolean special = SharedPrefsData.getBool(
                        HomePage.this,
                        Constants.isUnreadPostsSelected,
                        Constants.PREF_NAME);
                boolean video = SharedPrefsData.getBool(
                        HomePage.this,
                        Constants.isVideoPostsSelected,
                        Constants.PREF_NAME);
                boolean search = isSearchFeedActive();
                if (special) {
                    EdgeOriginCallWatch.annotateExpectedListOrigin(
                            "expected_special_posts");
                } else if (video) {
                    EdgeOriginCallWatch.annotateExpectedListOrigin(
                            "expected_video_posts");
                } else if (search) {
                    EdgeOriginCallWatch.annotateExpectedListOrigin(
                            "expected_search_feed");
                } else if (category_id != 0 && category_id != -1) {
                    EdgeOriginCallWatch.annotateExpectedListOrigin(
                            "expected_category_feed");
                }
            }
        } catch (Exception ignored) {
        }
    }

    /** Origin POST List_All_Articles (search/special/video/flash + CF soft-fallback). */
    private void enqueueListAllArticlesOrigin(
            int category_id, int reqPos, int gen, boolean fromEdgeFallback) {
        if (!fromEdgeFallback) {
            annotateIntentionalListOrigin(category_id);
        }
        JsonObject otp = Articles_All(category_id);
        APIService apiService = RestApiClient.getClient().create(APIService.class);
        Call<EncreptionRequest> otp_generation = apiService.List_All_Articles("v3", otp);
        listAllArticlesCall = otp_generation;
        try {
            otp_generation.enqueue(new Callback<EncreptionRequest>() {
                @Override
                public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {
                    if (gen != listAllArticlesGen || isFinishing() || isDestroyed()) {
                        return;
                    }
                    if (call != null && call.isCanceled()) {
                        return;
                    }
                    convertListOfArticles(response, reqPos);
                }

                @Override
                public void onFailure(Call<EncreptionRequest> call, Throwable t) {
                    if (gen != listAllArticlesGen) {
                        return;
                    }
                    isApiRequested = false;
                }
            });
        } catch (Exception e) {
            /* soft */
        }
    }
    /**
     * Append new articles while skipping any post already present in the list.
     * Prevents the same post showing twice when pagination returns overlapping data.
     * Ads / share_app / ratings (postId == null) are always kept since they are positional.
     */
    private void addUniqueArticles(java.util.List<ModelClass.Datum> newData) {
        if (newData == null) return;
        java.util.HashSet<String> existing = new java.util.HashSet<>();
        for (ModelClass.Datum d : list) {
            if (d != null && d.getPostId() != null) {
                existing.add(d.getPostId() + "_" + d.getSubPostId());
            }
        }
        for (ModelClass.Datum d : newData) {
            if (d == null) continue;
            EdgeFeedPostNormalizer.normalize(d);
            if (d.getPostId() == null) {
                list.add(d);
                continue;
            }
            String key = d.getPostId() + "_" + d.getSubPostId();
            if (!existing.contains(key)) {
                list.add(d);
                existing.add(key);
            } else {
                showLog("DEDUP skipped duplicate post key=" + key);
            }
        }
    }

    public void convertListOfArticles(Response<EncreptionRequest> response,int reqPos){
        if (reqPos != -1) {
            no_article_message = "Please wait while loading ...";
        }
        showLog("reqPos:::::"+reqPos);
        showLog("isFromLocalArticles:::::"+isFromLocalArticles);
        if (reqPos == 3 && !isFromLocalArticles) {
            if (response != null && response != dummyResponse) {
                isDataParsed = false;
                dummyResponse = response;
                showLog("requestPosition::" + requestPostition);
                requestPostition = 1 + requestSize;
                showLog("requestPosition::" + requestPostition);
                return;
            }
            dummyResponse = null;
        }
        try {
            if (response!=null && response.body() != null) {
                showLog("response body not null");
                dummyResponse=null;
                isDataParsed=true;
                isApiRequested = false;
                SharedPrefsData.putBool(getApplicationContext(), Constants.HOME_SCREEN, true, Constants.PREF_NAME);
                EncreptionRequest encreptionRequest = response.body();
                encreptionRequest__=encreptionRequest;
                String encrpt_data = decrypt(encreptionRequest.getData());
                if(encrpt_data!=null && encrpt_data.length()>1){
                    int maxLogSize = 2000;
                    for(int i = 0; i <= encrpt_data.length() / maxLogSize; i++) {
                        int start = i * maxLogSize;
                        int end = (i+1) * maxLogSize;
                        end = end > encrpt_data.length() ? encrpt_data.length() : end;
                        if(BuildConfig.DEBUG){
                            android.util.Log.d("ALL ARTICLES DATA:", encrpt_data.substring(start, end));
                        }
                    }
                }


                Gson gsonObj = new Gson();
                ModelClass validateUserOtp = gsonObj.fromJson(encrpt_data, ModelClass.class);
                if (Boolean.TRUE.equals(validateUserOtp.getSuccess())) {
                   if (encreptionRequest.getUpdateMenuStatus() != null && encreptionRequest.getUpdateMenuStatus()==3){
                       finish();
                        movetoappStore();
                    }
                    if (encreptionRequest.getUpdateMenuStatus() != null && encreptionRequest.getUpdateMenuStatus()==2 ){
                        PlaystoreUpdateDialog();
                    }
                    // ads_data / ad_positions: seat metadata for the feed — do NOT fan-out
                    // AdLoader for every position on page-0. At most one preload; seat
                    // loads happen on land + ~30s stay-refresh (single in-flight).
                    if (encreptionRequest.isShow_ads() && positionx == 0
                            && encreptionRequest.getAd_positions() != null
                            && !encreptionRequest.getAd_positions().isEmpty()) {
                        if (!shouldDeferAdWork()) {
                            AdsPojo first = null;
                            for (int i = 0; i < encreptionRequest.getAd_positions().size(); i++) {
                                AdsPojo p = encreptionRequest.getAd_positions().get(i);
                                if (p != null && !TextUtils.isEmpty(p.getAd_code())
                                        && p.getAd_req_position() != null) {
                                    first = p;
                                    break;
                                }
                            }
                            if (first != null) {
                                showLog("ListAll Articles Api trigger and AD Loading (single)");
                                loadAds(first.getAd_code(), first.getAd_req_position(),
                                        first.getAd_type());
                            }
                        } else {
                            showLog("ListAll Articles ad load deferred/skipped (flip or in-flight)");
                        }
                    }
                    if (validateUserOtp.getData() != null && validateUserOtp.getData().size() > 0) {
                        final int listSizeBefore = list.size();
                        addUniqueArticles(validateUserOtp.getData());
                        if (encreptionRequest.isShow_ads()) {
                            rebalanceFeedAdsIfNeeded(encreptionRequest);
                        }
                        requestSize = list.size();
                        last_post_id = validateUserOtp.getLastPostId();
                        advanceFeedSkipAfterResponse(validateUserOtp, reqPos);
                        if (list.get(0).getPostId() != null) {
                            updated_post_id_firebase = list.get(0).getPostId().intValue();
                        }
                        showLog("response body not null:"+isFromLocalArticles);
                        //writeNewUser("User", type_firebase, current_time_firebase, draft_id_firebase, device_id_firebase, language_id_firebase, updated_post_id_firebase, positionx);
                        if(isFromLocalArticles){
                           isFromLocalArticles=false;
                           ensureFlipperAdapter(category_id);
                       }
                        ensureFlipperAdapter(category_id);
                        if (adapter == null) {
                            showLog("adapter null after articles response");
                            return;
                        }
                        showLog("response body not null after load data:");
                        adapter.hasStableIds();
                        final int seatBefore =
                                positionx >= 0 && positionx < list.size() ? positionx : 0;
                        final int flipCountBefore =
                                mFlipView != null ? mFlipView.getPageCount() : listSizeBefore;
                        adapter.setData(list);
                        if (reqPos == 3 && mFlipView != null && list.size() > flipCountBefore) {
                            mFlipView.pageCountGrewQuietly();
                        }
                        if (encreptionRequest.isShow_ads()) {
                            preloadNativeAdsForFeedSlots();
                        }
                        isApiRequested = false;
                        requestPostition = 1 + list.size();
                        showLog(
                                "feed append seat="
                                        + seatBefore
                                        + " list="
                                        + list.size()
                                        + " skip="
                                        + skip
                                        + " actual_skip="
                                        + actual_skip);
                        // Append / pagination: keep the user's current card (never snap home).
                        if (reqPos == 3) {
                            syncFlipViewToFeedIndex(seatBefore);
                        } else if (reqPos == -1 || positionx < 0 || positionx >= list.size()) {
                            positionx = 0;
                            ppi = 0;
                            try {
                                if (mFlipView != null) {
                                    mFlipView.flipTo(0);
                                }
                            } catch (Exception ignored) {
                            }
                        } else {
                            syncFlipViewToFeedIndex(positionx);
                        }
                        mFlipView.setVisibility(View.VISIBLE);
                        txtNoData.setVisibility(View.GONE);
                        hideHomeFeedLoadingReveal(true);
                        layout_with_data.setVisibility(View.VISIBLE);
                        keepDailyUpdatesCovered();
                        keepCalendarCovered();
                        if (localFeedFromDailyUpdates) {
                            try {
                                showLocalFeedChromeThin();
                            } catch (Exception ignored) {
                            }
                        }

                        if(allsharedpreference.getShowFirstTimeAnimation()){
                            Display display = getWindowManager().getDefaultDisplay();
                            DisplayMetrics displayMetrics = new DisplayMetrics();
                            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                            int height = displayMetrics.heightPixels;
                            int width = displayMetrics.widthPixels;

                            int resourceId = getResources().getIdentifier(
                                    "status_bar_height", "dimen", "android");
                            int  screenheight = (height - getResources().getDimensionPixelSize(
                                    resourceId));
                            int distance=0;
                            if (getResources().getDisplayMetrics().density <= 1.0) {
                                distance = (int) (screenheight / 2.0);
                            } else {
                                distance = (int) (screenheight / getResources()
                                        .getDisplayMetrics().density);
                            }
                            showLog("DISTANCE1:"+distance);
                            showLog("DISTANCE2:"+getResources()
                                    .getDisplayMetrics().density);
                            mFlipView.peakNext(false);

                            mFlipView.setFlipDistanceperpage(200, getResources()
                                    .getDisplayMetrics().density);
                            allsharedpreference.setShowFirstTimeAnimation(false);

                        }

                    } else {

                        if (validateUserOtp.getData().size() == 0) {
                            if (list.size() == 0) {
                                mFlipView.setVisibility(View.GONE);
                                layout_with_data.setVisibility(View.VISIBLE);
                                hideHomeFeedLoadingReveal(true);
                                post_news_scrren.setVisibility(View.GONE);
                                txtNoData.setVisibility(View.VISIBLE);
                                initialCall=true;
                                showLog("txtNoData VISIBLE::");
                            } else {
                                no_article_message="No articles to read further";
                                try {
                                    if (FeedEndHelper.ensureAtEnd(list, HomePage.this)
                                            && adapter != null) {
                                        adapter.setData(list);
                                    }
                                } catch (Exception ignored) {
                                }
                            }

                        }
                    }
                } else {
                    mFlipView.setVisibility(View.GONE);
                    layout_with_data.setVisibility(View.VISIBLE);
                    hideHomeFeedLoadingReveal(true);
                    post_news_scrren.setVisibility(View.GONE);
                    txtNoData.setVisibility(View.VISIBLE);
                    initialCall=true;
                    showLog("txtNoData VISIBLE::");

                }

                if(encreptionRequest.isUpdate_user_language()){
                    LanguageSelection(SharedPrefsData.getInt(getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME));

                }


                showReporterLogin = encreptionRequest.isShow_reporter_login();
                showLog("showReporterLogin:" + showReporterLogin);
                // Footer center = WhatsApp only; reporter create lives on Profile (isFooterShowPlus false).
                floatingActionButtonHome.setVisibility(View.GONE);
                applyFooterWhatsapp();
                Boolean pushPermAlert = encreptionRequest.getShow_push_perm_alert();
                showNotificationAlert(pushPermAlert != null && pushPermAlert);
            }else{
                isDataParsed=true;
                showLog("response body null");
                no_article_message="No articles to read further";
            }

            initialCall=true;
            updateHomePageNavigationForSeat();
            try {
                applyFooterSlotVisibility();
                ensureSpecialMenuIconAlwaysVisible();
                setupSpecialTabBulb();
                repositionCenterFabForVisibleTabs();
            } catch (Exception ignored) {
            }

        } catch (NullPointerException e) {
            e.printStackTrace();

        }

    }

    @Override
    public void onBackPressed() {
        handleHomeBackNavigation();
    }

    /**
     * Thin back stack: comments → local chrome → calendar → Daily Updates →
     * Special → fullscreen video → double-tap exit. No fat search-exit path.
     */
    private void handleHomeBackNavigation() {
        showLog("isPlayingVideoInFullScreen" + FlipperAdapter.fullscreen);
        showLog("isPlayingVideoInFullScreen" + isPlayingVideoInFullScreen);
        if (isFinishing() || isDestroyed()) {
            return;
        }
        try {
            if (adScreenArticleOverlay != null && adScreenArticleOverlay.isShowing()) {
                adScreenArticleOverlay.dismiss(true);
                return;
            }
        } catch (Exception ignored) {
        }
        try {
            if (comment_page != null && comment_page.isShown()) {
                comment_page.setVisibility(View.GONE);
                return;
            }
        } catch (Exception ignored) {
        }
        if (localFeedFromDailyUpdates
                || (localFeedChromeRoot != null
                && localFeedChromeRoot.getVisibility() == View.VISIBLE)) {
            beginInstantNewsFooterReturn(true);
            return;
        }
        if (isCalendarMode()) {
            if (calendarFromDailyUpdates) {
                exitCalendarToDailyUpdates();
            } else {
                beginInstantNewsFooterReturn(false);
            }
            return;
        }
        if (isDailyUpdatesMode()) {
            beginInstantNewsFooterReturn(false);
            return;
        }
        if (flashNewsModeActive
                || SharedPrefsData.getBool(
                        HomePage.this, Constants.isUnreadPostsSelected, Constants.PREF_NAME)) {
            beginInstantNewsFooterReturn(true);
            return;
        }
        int count = this.getSupportFragmentManager().getBackStackEntryCount();
        if (count != 0) {
            getSupportFragmentManager().popBackStack();
            return;
        }
        if (mBackPressed + 2000 > System.currentTimeMillis()) {
            Intent setIntent = new Intent(Intent.ACTION_MAIN);
            setIntent.addCategory(Intent.CATEGORY_HOME);
            setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (getApplicationContext() != null
                    && getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)) {
                startActivity(setIntent);
            }
            return;
        }
        if (txtNoData != null && txtNoData.getVisibility() == View.VISIBLE) {
            showLog("Check Point #2 : HomePage back from empty");
            Intent intent = new Intent(HomePage.this, HomePage.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            if (getApplicationContext() != null
                    && getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)) {
                startActivity(intent);
            }
            finish();
            return;
        }
        if (FlipperAdapter.fullscreen) {
            try {
                if (mFlipView != null && ppi >= 0 && ppi < mFlipView.getChildCount()) {
                    View flipChild = mFlipView.getChildAt(ppi);
                    if (flipChild != null && adapter != null) {
                        PlayerView simpleExoPlayerView =
                                flipChild.findViewById(R.id.normal_video_player);
                        ConstraintLayout layoutParentTop =
                                flipChild.findViewById(R.id.layoutParentTop);
                        LinearLayout layoutParentTitle =
                                flipChild.findViewById(R.id.layoutParentTitle);
                        LinearLayout layoutParentReportDetails =
                                flipChild.findViewById(R.id.ll_footer_layout);
                        LinearLayout layoutParentMiddle =
                                flipChild.findViewById(R.id.layoutParentMiddle);
                        adapter.showExoPlayerInFullScreen(
                                FlipperAdapter.fullscreen,
                                simpleExoPlayerView,
                                layoutParentTop,
                                layoutParentMiddle,
                                layoutParentReportDetails,
                                layoutParentTitle);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        if (isPlayingVideoInFullScreen) {
            try {
                if (adapter != null && adapter.customPlayerUiController != null) {
                    adapter.customPlayerUiController.onYouTubePlayerExitFullScreen();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        if (floating_action_button != null && floating_action_button.isShown()) {
            floating_action_button.setVisibility(View.GONE);
            if (floatingActionButtonHome != null) {
                floatingActionButtonHome.setRotation(0);
            }
            try {
                if (fab1 != null) fab1.startAnimation(fab_close);
                if (fab2 != null) fab2.startAnimation(fab_close);
                if (fab4 != null) fab4.startAnimation(fab_close);
            } catch (Exception ignored) {
            }
            isOpen = false;
            if (mFlipView != null) {
                mFlipView.setVisibility(View.VISIBLE);
            }
            if (top_item_layout != null) {
                top_item_layout.setVisibility(View.GONE);
            }
            return;
        }
        try {
            if (languageModelResponse != null
                    && languageModelResponse.getData() != null
                    && languageModelResponse.getData().getTapToExit() != null) {
                Toast.makeText(
                                getBaseContext(),
                                languageModelResponse.getData().getTapToExit(),
                                Toast.LENGTH_SHORT)
                        .show();
            }
        } catch (Exception ignored) {
        }
        mBackPressed = System.currentTimeMillis();
    }

    private JsonObject ReportParams() {

        DilogeResest dilogeResest = new DilogeResest();
        dilogeResest.setLanguageId(SharedPrefsData.getInt(getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME));


        return new Gson().toJsonTree(dilogeResest).getAsJsonObject();
    }

    private JsonObject DailogRequest(String reson, long last_post_id, String full_image_edt_news_headlines) {

        DialogSendRequest dilogeResest = new DialogSendRequest();

        dilogeResest.setDraftId(SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME));
        dilogeResest.setLanguageId(SharedPrefsData.getInt(getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME));
        dilogeResest.setOption("" + reson);
        dilogeResest.setPostId(last_post_id);
        dilogeResest.setReason("" + full_image_edt_news_headlines);


        return new Gson().toJsonTree(dilogeResest).getAsJsonObject();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();

                break;

            case MotionEvent.ACTION_UP:

                x2 = event.getX();
                float deltaX = x2 - x1;
                if (Math.abs(deltaX) > MIN_DISTANCE) {
                } else {
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    /*   check internet connection */
    public static void dialog(boolean value) {

        if (value) {
            yes_internet.setVisibility(View.VISIBLE);
            no_internet.setVisibility(View.GONE);

            Handler handler = new Handler();
            Runnable delayrunnable = new Runnable() {
                @Override
                public void run() {
                    yes_internet.setVisibility(View.VISIBLE);
                    no_internet.setVisibility(View.GONE);
                }
            };
            handler.postDelayed(delayrunnable, 500);
        } else {
            yes_internet.setVisibility(View.GONE);
            no_internet.setVisibility(View.VISIBLE);
        }
    }

    private void refreshDetails() {

        showLog("positionx::"+positionx);
        showLog("requestPostition::"+requestPostition);
        showLog("LIST SIZE:"+(list.size()+":positionx:"+positionx));
        if(positionx==requestPostition){
            showLog("request at position::"+requestSize);
            isApiRequested=true;
            ShowNews_Data_Values(category_id,3);
        }

        if (list != null
                && !list.isEmpty()
                && positionx >= Math.max(0, list.size() - FEED_PREFETCH_FROM_END)) {
            showProgress();
            if (dummyResponse != null) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showLog("request at position:: convertListOfArticles");
                        convertListOfArticles(dummyResponse, 3);
                        hideProgress();
                    }
                }, 100);
            } else {
                showLog("request at position::" + requestSize + isApiRequested);
                if (!isApiRequested) {
                    isApiRequested = true;
                    ShowNews_Data_Values(category_id, 3);
                }
                hideProgress();
            }
        } else {
            bottomNavigationView.getMenu().getItem(0).setIcon(R.drawable.refresh_icon);
            bottomNavigationView.getMenu().getItem(0).setTitle("" + languageModelResponse.getData().getReload());
//
//            if (positionx >= 1) {
//                bottomNavigationView.getMenu().getItem(0).setIcon(R.drawable.refresh_icon);
//                bottomNavigationView.getMenu().getItem(0).setTitle("" + languageModelResponse.getData().getReload());
//                Logone.d("TestingHome", "" + languageModelResponse.getData().getReload());
//            }
//
//            if (positionx == 0) {
//                bottomNavigationView.getMenu().getItem(0).setIcon(R.drawable.ic_home);
//                bottomNavigationView.getMenu().getItem(0).setTitle("" + languageModelResponse.getData().getHome());
//                Logone.d("TestingHome", "" + languageModelResponse.getData().getHome());
//            }
        }
    }


    private JsonObject firebaseJSON() {
        FirebaseAnalyticsJSON requestModel = new FirebaseAnalyticsJSON();

        requestModel.setPostID("" + list.get(positionx).getPostId());
        requestModel.setDraftId(SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME));

        return new Gson().toJsonTree(requestModel).getAsJsonObject();

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onFlippedToPage(FlipView view, int position, long id) {
        final int settledFrom = positionx;
        try {
            if (view == mFlipView && adScreenArticleOverlay != null && adScreenArticleOverlay.isShowing()) {
                if (position != settledFrom) {
                    view.post(() -> view.flipTo(settledFrom));
                }
                return;
            }
        } catch (Exception ignored) {
        }
        // Samsung-critical: stop prior page audio (Exo/YT/native ad/VAST/WebView) + TTS
        // before the delayed settle work runs.
        try {
            if (position != positionx) {
                ArticleTtsHelper.stopAll();
            }
        } catch (Exception ignored) {
        }
        pauseVideo();
        try {
            if (adapter != null) {
                adapter.releaseOffscreenAdMediaAfterSettle(position);
            }
        } catch (Exception ignored) {
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                onFlippedToPage1(view,position,id);
            }
        },50);

    }
    public void onFlippedToPage1(FlipView view, int position, long id) {
        showLog("onFlippedToPage1:::");
        try {
            FlipAdWorkGate.noteFlipInteraction();
        } catch (Exception ignored) {
        }
        if (list == null || position < 0 || position >= list.size() || list.get(position) == null) {
            showLog("onFlippedToPage1 skip: bad position=" + position);
            return;
        }
        isPlayingVideoInFullScreen=false;
        currentView=view;
        initialCall=true;
        positionx = position;
        post_type=list.get(position).getPostType();

        updateHomePageNavigationForSeat();
        try {
            updated_post_id_firebase = list.get(positionx).getPostId().intValue();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        showLog("CHECK POINT ONFLIPPEDTOPAGE 2");
        firbaseJSONObj = firebaseJSON();
        if (position >= 0 && position < 2) {
            ppi = position;
        } else {
            ppi = 1;
        }
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
        if(post_type.equalsIgnoreCase("image_gallery")){
            adapter.closeGalleryShareLayout(mFlipView.getChildAt(ppi));
        }
        refreshDetails();
        showLog("CHECK POINT ONFLIPPEDTOPAGE 4");
//        if (adapter != null) {
//            adapter.hasStableIds();
//            adapter.notifyDataSetChanged();
//        }
        showLog("CHECK POINT ONFLIPPEDTOPAGE 5");
        try {
            if (player != null) {
                player.setPlayWhenReady(false);
                player.stop();
                player.seekTo(0);
            }
            if (progress_bar_for_video != null) {
                progress_bar_for_video.setVisibility(View.VISIBLE);
            }
            if (mute_video_player_image != null) {
                mute_video_player_image.setImageResource(R.drawable.ic_volume_off);
            }
            if (mute_video_player_text != null) {
                mute_video_player_text.setText("Unmute");
            }
            if (time_for_video != null) {
                time_for_video.setText("00:00");
            }
            if (simpleSeekBar != null) {
                simpleSeekBar.setMax(0);
            }

        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        showLog("CHECK POINT ONFLIPPEDTOPAGE 6");
//        AudioManager audio = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
//        int maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
//
//        int seventyVolume = (int) (maxVolume * percent);
//       // audio.setStreamVolume(AudioManager.STREAM_MUSIC, seventyVolume, 0);
//
        //youTubePlayerViewHalf= FlipperAdapter.youTubePlayerViewHalf;
       /* if (FlipperAdapter.youTubePlayerViewHalf != null) {
            FlipperAdapter.youTubePlayerViewHalf.pause();
            FlipperAdapter.youTubePlayerViewHalf=null;
            adapter.hasStableIds();
            adapter.notifyDataSetChanged();
        } else */

        //showNormalImage(view,position);
//        if (post_type.equals("normal_video")) {
//          //  FlipperAdapter.youTubePlayerViewHalf.cueVideo(list.get(position).getVideoUrl(),0);
//            //adapter.showNormalVideo(currentView,position);
//        }else{
//            pauseVideo();
//        }
        pauseVideo();



        //loading Ad (flip-safe defer — no full native pool rewrite)
        if (list.get(positionx).getPostType().equalsIgnoreCase("ad")) {
            showLog("ONFLIPPEDTOPAGE AD Loading");
            if (!shouldDeferAdWork()) {
                ModelClass.Datum adSeat = list.get(positionx);
                String seatCode = adSeat.getAd_code();
                if (TextUtils.isEmpty(seatCode)) {
                    seatCode = adSeat.getNext_req_ad_code();
                }
                if (TextUtils.isEmpty(seatCode)) {
                    seatCode = FeedAdConfig.getUnitId(this);
                }
                Integer seatPos = adSeat.getAd_req_position();
                String seatType = adSeat.getAd_type();
                if (TextUtils.isEmpty(seatType)) {
                    seatType = "google";
                }
                loadAds(seatCode, seatPos, seatType);
            }
            scheduleAdSeatStayRefresh();
        } else {
            cancelAdSeatStayRefresh();
        }
        showLog("CHECK POINT ONFLIPPEDTOPAGE 7");
        try {
            int draftId = SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME);
            long postId = list.get(position).getPostId();
            ViewsBatchTracker.get().onFlipSettled(
                    getApplicationContext(), postId, draftId, position, post_type);
        } catch (Exception ignored) {
        }
        trackingEvents.FireBase("post_type", list.get(position).getPostType()+"_pos_"+position);
        BundleDataToGcmPush(position);

//        if(list.get(positionx).getPostType().equalsIgnoreCase("normal_image")){
//            LocalBroadcastManager.getInstance(getContext()).registerReceiver(CURRENT_LOCATION,
//                    new IntentFilter("CURRENT_LOCATION"));
//        }

    }



    @Override
    public void onDestroy() {
        cancelAdSeatStayRefresh();
        synchronized (feedNativeAdSeatsInFlight) {
            feedNativeAdSeatsInFlight.clear();
        }
        try {
            cancelListAllArticlesCall();
        } catch (Exception ignored) {
        }
        try {
            hideHomeFeedLoadingReveal(true);
        } catch (Exception ignored) {
        }
        try {
            if (playInAppUpdateHelper != null) {
                playInAppUpdateHelper.unregister();
                playInAppUpdateHelper = null;
            }
        } catch (Exception ignored) {
        }
        adapter = null;
        super.onDestroy();
        isFromLocalArticles=false;

        if (player != null) {
            player.release();
        }
        if (FlipperAdapter.exoPlayer != null) {
            FlipperAdapter.exoPlayer.setPlayWhenReady(false);
            FlipperAdapter.exoPlayer.release();
        }
        try {
            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
            lbm.unregisterReceiver(COMMENT_PAGE_ACTIVE);
            lbm.unregisterReceiver(COMMENTS_PAGE);
            lbm.unregisterReceiver(LIKE_PAGE);
            lbm.unregisterReceiver(WRITER_FOLLOW);
            lbm.unregisterReceiver(WRITER_FOLLOWER);
            lbm.unregisterReceiver(SURVEY_STATUS);
            lbm.unregisterReceiver(SURVEY_POLL);
            if (receiver != null) unregisterReceiver(receiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        showLog("isUnreadPostsSelected set to false #4");
    }


    @Override
    public void onResume() {
        super.onResume();
        homeLifecycleResumed = true;
        try {
            ViewsBatchTracker.get().setHomeFlushActive(true);
            PostDwellTracker.get().onResumed(getApplicationContext());
        } catch (Exception ignored) {
        }
        try {
            if (isCurrentPageAdSeat()) {
                scheduleAdSeatStayRefresh();
            }
        } catch (Exception ignored) {
        }
        try {

            if (player != null) {
                player.setPlayWhenReady(true);

            }
            if (positionx > 0) {
               /* if (FlipperAdapter.youTubePlayerViewHalf != null) {
                    FlipperAdapter.youTubePlayerViewHalf.pause();
                    AudioManager audio = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                    int maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                    int seventyVolume = (int) (maxVolume * percent);
                   // audio.setStreamVolume(AudioManager.STREAM_MUSIC, seventyVolume, 0);
                }*/
                if (youTubePlayerView1 != null) {
                    youTubePlayerView1.release();
                    AudioManager audio = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                    int maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                    int seventyVolume = (int) (maxVolume * percent);
                    //audio.setStreamVolume(AudioManager.STREAM_MUSIC, seventyVolume, 0);
                }
            }
            try{
                showLog("onResume");
                if(list!=null && list.size()>0 && list.get(positionx).getPostType()!=null && list.get(positionx).getPostType().equalsIgnoreCase("normal_video")){
                    showLog("onResume"+"normal_video");
                    if(list.get(positionx).getMediaType().equalsIgnoreCase("3")){
                       // adapter.showNormalVideo(currentView,positionx);
                        showLog("onResume "+" normal_vide 1");
                    }
                }

            }catch (Exception e){
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    private JsonObject set_like_request(Long post_id, Long sub_post_id, int type) {

        LikeSelectionRequest requestModel = new LikeSelectionRequest();

        requestModel.setDraft_id((long) SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME));
        requestModel.setPost_id(post_id);
        requestModel.setSub_post_id(sub_post_id);
        requestModel.setType(type);

        String jsonString = new com.google.gson.Gson().toJson(requestModel);
        String encrpt_data = null;
        encrpt_data = encrypt(jsonString);
        showLog("API REQUEST E" + jsonString.toString());
        EncritonRequestData Encriton_Request = new EncritonRequestData();
        Encriton_Request.setData(encrpt_data);
        return new Gson().toJsonTree(Encriton_Request).getAsJsonObject();
    }

    private void countforshareanddownload(Long post_id, Long sub_post_id, int type) {


        JsonObject otp = set_like_request(post_id, sub_post_id, type);

        APIService apiService = RestApiClient.getClient().create(APIService.class);
        Call<EncreptionRequest> apiResponse = apiService.update_like("v3", otp);
        apiResponse.enqueue(new Callback<EncreptionRequest>() {
            //@RequiresApi(api = Build.VERSION_CODES.O)
            @Override

            public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {


                EncreptionRequest encreptionRequest = response.body();
                if (encreptionRequest == null || encreptionRequest.getData() == null) return;
                String encrpt_data = decrypt(encreptionRequest.getData());

                Gson gsonObj = new Gson();
                LikeDownloadRespouse likeDownloadRespouse = gsonObj.fromJson(encrpt_data, LikeDownloadRespouse.class);

                if (likeDownloadRespouse != null && Boolean.TRUE.equals(likeDownloadRespouse.getSuccess())) {
                    Logone.e(TAG, "Button Clicked Like");
                }


            }

            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {

            }
        });

    }

    private JsonObject set_count_request(String share, long post_id) {

        LikeSelectionRequest requestModel = new LikeSelectionRequest();

        requestModel.setDraft_id((long) SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME));
        requestModel.setPost_id(post_id);
        requestModel.setProperty(share);

        String jsonString = new com.google.gson.Gson().toJson(requestModel);
        String encrpt_data = null;
        encrpt_data = encrypt(jsonString);
        showLog("API REQUEST E" + jsonString.toString());
        EncritonRequestData Encriton_Request = new EncritonRequestData();
        Encriton_Request.setData(encrpt_data);
        return new Gson().toJsonTree(Encriton_Request).getAsJsonObject();
    }

    private JsonObject set_follow_un_follow_request(Long writer_id, int is_follow) {

        LikeSelectionRequest requestModel = new LikeSelectionRequest();

        requestModel.setDraft_id((long) SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME));
        requestModel.setWriter_id(writer_id);
        requestModel.setIs_follow(is_follow);

        String jsonString = new com.google.gson.Gson().toJson(requestModel);
        String encrpt_data = null;
        encrpt_data = encrypt(jsonString);
        showLog("API REQUEST E" + jsonString.toString());
        EncritonRequestData Encriton_Request = new EncritonRequestData();
        Encriton_Request.setData(encrpt_data);
        return new Gson().toJsonTree(Encriton_Request).getAsJsonObject();
    }

    private void set_follow_un_follow(Long writer_id, int is_follow) {
        JsonObject otp = set_follow_un_follow_request(writer_id, is_follow);
        APIService apiService = RestApiClient.getClient().create(APIService.class);
        Call<EncreptionRequest> apiResponse = apiService.update_follow("v3", otp);
        apiResponse.enqueue(new Callback<EncreptionRequest>() {
            @Override

            public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {


                EncreptionRequest encreptionRequest = response.body();
                String encrpt_data = null;
                encrpt_data = decrypt(encreptionRequest.getData());

                showLog("ENCRYPT" + encrpt_data);

                Gson gsonObj = new Gson();
                LikeDownloadRespouse likeDownloadRespouse = gsonObj.fromJson(encrpt_data, LikeDownloadRespouse.class);


                if (likeDownloadRespouse.getSuccess()) {

                }

            }

            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {


            }
        });

    }

    @Override
    public void onPause() {
        homeLifecycleResumed = false;
        cancelAdSeatStayRefresh();
        try {
            ViewsBatchTracker.get().setHomeFlushActive(false);
            PostDwellTracker.get().onPaused(getApplicationContext());
        } catch (Exception ignored) {
        }
        //new flip
        try {
            ArticleTtsHelper.stopAll();
        } catch (Exception ignored) {
        }
        try {
            pauseVideo();
        } catch (Exception ignored) {
        }
        super.onPause();
        try {
            if (FlipperAdapter.exoPlayer != null) {
                FlipperAdapter.exoPlayer.setPlayWhenReady(false);
                try {
                    FlipperAdapter.exoPlayer.pause();
                } catch (Exception ignored) {
                }
                FlipperAdapter.exoPlayer.stop();
                try {
                    FlipperAdapter.releaseActiveExoPlayer();
                } catch (Exception ignored) {
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if(adapter != null && adapter.nativeAd!=null){
                showLog("native Ad");
                if(adapter.nativeAd.getMediaContent()!=null){
                    showLog("native Ad getMediaContent");
                    if(adapter.nativeAd.getMediaContent().getVideoController()!=null){
                        showLog("native Ad getMediaContent pause");
                        adapter.nativeAd.getMediaContent().getVideoController().mute(true);
                        adapter.nativeAd.getMediaContent().getVideoController().pause();
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        if (player != null) {
            try {
                player.setPlayWhenReady(false);
                if (positionx > 0) {
                   /* if (FlipperAdapter.youTubePlayerViewHalf != null) {
                        FlipperAdapter.youTubePlayerViewHalf.pause();
                        AudioManager audio = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                        int maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                        int seventyVolume = (int) (maxVolume * percent);
                        audio.setStreamVolume(AudioManager.STREAM_MUSIC, seventyVolume, 0);
                    }*/
                    if (youTubePlayerView1 != null) {
                        youTubePlayerView1.release();
                        AudioManager audio = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                        int maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                        int seventyVolume = (int) (maxVolume * percent);
                        audio.setStreamVolume(AudioManager.STREAM_MUSIC, seventyVolume, 0);
                    }

                }

            } catch (NullPointerException e) {

            }

        }

    }

    private Context context;
    //botoom menu
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                // ... implement code here
                case R.id.navigation_home:
                    if (suppressBottomNavCallbacks) {
                        return true;
                    }
                    writeButtonClick("User", type_firebase_button, current_time_firebase, draft_id_firebase, device_id_firebase, button_name_home);
                    showLog("initialCall:::" + initialCall);
                    // Soft News return — never CLEAR_TASK + finish (that closed the app).
                    beginInstantNewsFooterReturn(true);
                    return true;
                case R.id.navigation_catagiries:
                    if (suppressBottomNavCallbacks) {
                        return true;
                    }
                    // Soft Special open — never recreate HomePage.
                    performSpecialFeedOpen();
                    return true;
                case R.id.navigation_stricker:
                    // firebase events triggered
                    showLog("isUnreadPostsSelected set to false #1");
                    //SharedPrefsData.putBool(HomePage.this, Constants.isUnreadPostsSelected, false, Constants.PREF_NAME);
                    writeButtonClick("User", type_firebase_button, current_time_firebase, draft_id_firebase, device_id_firebase, button_name_post);
                    showLog("showReporterLogin:"+showReporterLogin);
                    if (!showReporterLogin) {
                        if (list != null
                                && positionx >= 0
                                && positionx < list.size()
                                && list.get(positionx).getPostType() != null
                                && (list.get(positionx).getPostType().equalsIgnoreCase("ad")
                                        || list.get(positionx)
                                                .getPostType()
                                                .equalsIgnoreCase("share_app"))) {
                            shareAnyNewsAppPromotion();
                            return true;
                        }
                        showLog("onClick of whatsapp");
                        CallShareListener listener = new CallShareListener();
                        listener.setShareInterfaceListener(adapter);
                        listener.triggerEvent();
                        showLog("onClick of whatsapp:" + ppi);
                        if (list!=null && list.size()>0 && list.get(positionx).getPostType().equalsIgnoreCase("image_gallery")) {
                            //adapter.shareGalleryScreenShot(positionx, 1);
                            adapter.shareGalleryScreenShotUI(mFlipView.getChildAt(ppi));
                        }
                        if (list.get(positionx).getPostType().equalsIgnoreCase("full_image")) {
                            adapter.shareFullImageScree(mFlipView.getChildAt(ppi), positionx);
                        }
                        if (list.get(positionx).getPostType().equalsIgnoreCase("normal_image")) {
                            adapter.takeNormalImageScreenshot(mFlipView.getChildAt(ppi), positionx, 1);
                        }
                        if (list.get(positionx).getPostType().equalsIgnoreCase("magazine")) {
                            adapter.showMagazineShareOptions(mFlipView.getChildAt(ppi), 1);
                        }
                        if (list.get(positionx).getPostType().equalsIgnoreCase("normal_video")) {
                            if (list.get(positionx).getMediaType().equals("3")) {
                                PlayerView simpleExoPlayerView = mFlipView.getChildAt(ppi).findViewById(R.id.normal_video_player);
                                final ImageView thaumbnail_for_video = mFlipView.getChildAt(ppi).findViewById(R.id.thaumbnail_for_video);
                                final ImageView imageViewExoVideoPlayVideo = mFlipView.getChildAt(ppi).findViewById(R.id.imageViewVideoPlayVideo);
                                final ProgressBar progress_bar_for_video = mFlipView.getChildAt(ppi).findViewById(R.id.progress_bar_for_video);
                                adapter.shareExoPlayerVideoScreenShot(mFlipView.getChildAt(ppi), positionx, 1, simpleExoPlayerView, thaumbnail_for_video, imageViewExoVideoPlayVideo, progress_bar_for_video);


                            } else if (list.get(positionx).getMediaType().equals("4")) {
                                final ImageView thaumbnail_for_video = mFlipView.getChildAt(ppi).findViewById(R.id.thaumbnail_for_video);
                                final ImageView imageViewItem__ = mFlipView.getChildAt(ppi).findViewById(R.id.imageViewItem__);
                                final ImageView imageViewExoVideoPlayVideo = mFlipView.getChildAt(ppi).findViewById(R.id.imageViewVideoPlayVideo);
                                final ProgressBar progress_bar_for_video = mFlipView.getChildAt(ppi).findViewById(R.id.progress_bar_for_video);
                                com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView youTubePlayerView = mFlipView.getChildAt(ppi).findViewById(R.id.youtube_view_half_video);
                                adapter.shareYoutubeVideoScreenShot(mFlipView.getChildAt(ppi), positionx, youTubePlayerView, 1, imageViewItem__);
                            }

                        }
                        if (list.get(positionx).getPostType().equalsIgnoreCase("full_video")) {
                            if (list.get(positionx).getMediaType().equals("5")) {
                                final ImageView thaumbnail_for_video = mFlipView.getChildAt(ppi).findViewById(R.id.thaumbnail_for_video);
                                final RelativeLayout exo_play_layout = mFlipView.getChildAt(ppi).findViewById(R.id.exo_play_layout);
                                final ImageView imageViewExoVideoPlayVideo = mFlipView.getChildAt(ppi).findViewById(R.id.imageViewVideoPlayVideo);
                                PlayerView simpleExoPlayerViewFull = mFlipView.getChildAt(ppi).findViewById(R.id.normal_video_player);
                                adapter.shareExoPlayerFullVideoShare(positionx, 1, thaumbnail_for_video, exo_play_layout, imageViewExoVideoPlayVideo, simpleExoPlayerViewFull);


                            } else if (list.get(positionx).getMediaType().equals("6")) {
                                ImageView newFullImageView1 = mFlipView.getChildAt(ppi).findViewById(R.id.imageViewItem1);
                                RelativeLayout layoutYoutubeFullView = mFlipView.getChildAt(ppi).findViewById(R.id.layoutYoutubeFullView);
                                final com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView youTubePlayerView = mFlipView.getChildAt(ppi).findViewById(R.id.youtube_full_video);
                                ImageView imageViewYoutubePlayVideo = mFlipView.getChildAt(ppi).findViewById(R.id.imageViewPlayVideo);
                                LinearLayout side_menu = mFlipView.getChildAt(ppi).findViewById(R.id.video_side_menu);
                                adapter.shareYoutubePlayerFullVideoShare(positionx, 1, newFullImageView1, youTubePlayerView, layoutYoutubeFullView, imageViewYoutubePlayVideo, side_menu);
                            }

                        }

                    } else {
                        countDownTimer = new CountDownTimer(100, 100) {

                            public void onTick(long millisUntilFinished) {
                            }

                            public void onFinish() {
                                if (positionx > 0) {
                                    if (FlipperAdapter.youTubePlayerViewHalf != null) {
                                        FlipperAdapter.youTubePlayerViewHalf.pause();
                                        AudioManager audio = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                                        int maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                                        int seventyVolume = (int) (maxVolume * percent);
                                        // audio.setStreamVolume(AudioManager.STREAM_MUSIC, seventyVolume, 0);
                                    }
                                    if (youTubePlayerView1 != null) {
                                        youTubePlayerView1.release();
                                        AudioManager audio = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                                        int maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                                        int seventyVolume = (int) (maxVolume * percent);
                                        audio.setStreamVolume(AudioManager.STREAM_MUSIC, seventyVolume, 0);
                                    }
                                }
                                if (isOpen) {
                                    floating_action_button.setVisibility(View.GONE);
                                    floatingActionButtonHome.setRotation(0);
                                    fab1.startAnimation(fab_close);
                                    fab2.startAnimation(fab_close);
                                    fab4.startAnimation(fab_close);
                                    isOpen = false;


                                    /*post_artical_text.setText("" + languageModelResponse.getData().getPost());*/
                                    mFlipView.setVisibility(View.VISIBLE);
                                    floating_action_button.setVisibility(View.GONE);
                                    /*neo_home.setVisibility(View.VISIBLE);*/
                                    /*neo_profile.setVisibility(View.VISIBLE);*/
                                    top_item_layout.setVisibility(View.GONE);
//                                Intent intent = new Intent(HomePage.this, HomePage.class);
//                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                if (getApplicationContext() != null && getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)) {
//                                    startActivity(intent);
//                                }
//                                finish();
                                } else {
                                    boolean is_login = SharedPrefsData.getBool(HomePage.this, Constants.is_login, Constants.PREF_NAME);
                                    if (is_login) {
                                        floatingActionButtonHome.setRotation(45);
                                        fab1.startAnimation(fab_open);
                                        fab2.startAnimation(fab_open);
                                        fab4.startAnimation(fab_open);
                                        isOpen = true;
                                        txtNoData.setVisibility(View.GONE);
                                        mFlipView.setVisibility(View.GONE);
                                        post_news_scrren.setVisibility(View.VISIBLE);
                                        mFlipView.setVisibility(View.GONE);
                                        floating_action_button.setVisibility(View.VISIBLE);
                                        top_item_layout.setVisibility(View.GONE);

                                    } else {
                                        Intent intent = new Intent(HomePage.this, LoginActivityDetails.class);
                                        if (getApplicationContext() != null && getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)) {
                                            startActivity(intent);
                                        }

                                    }

                                }

                            }
                        }.start();
                    }
                    return false;
                case R.id.navigation_status:
                    if (suppressBottomNavCallbacks) {
                        return true;
                    }
                    showLog("isUnreadPostsSelected set to false #2");
                    writeButtonClick("User", type_firebase_button, current_time_firebase, draft_id_firebase, device_id_firebase, button_name_category);
                    // Soft Daily Updates hub — not SearchActivity.
                    performDailyUpdatesOpen();
                    return true;
                case R.id.navigation_profile:
                    if (suppressBottomNavCallbacks) {
                        return false;
                    }
                    showLog("isUnreadPostsSelected set to false #3");
                    writeButtonClick("User", type_firebase_button, current_time_firebase, draft_id_firebase, device_id_firebase, button_name_profile);
                    openProfileInstant();
                    return false;
                default:
                    return false;
            }
        }

    };

    private JsonObject set_static_data() {

        SurveyRequestFeedback requestModel = new SurveyRequestFeedback();
        requestModel.setDraftId(SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME));   //
        requestModel.setPostId(postIDSurvey);
        requestModel.setResponse(responseSurvey);

        String jsonString = new com.google.gson.Gson().toJson(requestModel);
        String encrpt_data = null;
        showLog("API REQUEST E" + jsonString.toString());
        encrpt_data = encrypt(jsonString);
        EncritonRequestData Encriton_Request = new EncritonRequestData();
        Encriton_Request.setData(encrpt_data);
        return new Gson().toJsonTree(Encriton_Request).getAsJsonObject();
    }


    private void SurveyResponse(String response, long post_id, int position) {

        responseSurvey = response;
        postIDSurvey = post_id;
        JsonObject jsonObject = set_static_data();

        APIService apiService = RestApiClient.getClient().create(APIService.class);
        Call<EncreptionRequest> apiResponse = apiService.survey_response("v3", jsonObject);
        apiResponse.enqueue(new Callback<EncreptionRequest>() {
            @SuppressLint("SetTextI18n")
            @Override

            public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {
                if (response.isSuccessful()) {

                    EncreptionRequest encreptionRequest = response.body();
                    String encrpt_data = null;
                    encrpt_data = decrypt(encreptionRequest.getData());

                    Gson gsonObj = new Gson();
                    surveyFeedbackResponse = gsonObj.fromJson(encrpt_data, SurveyFeedbackResponse.class);

                    if (surveyFeedbackResponse.getSuccess()) {
                        //list.get(position).setSurveyStatus(1);
                        ModelClass.Datum datum = new ModelClass.Datum();

                        datum.setSurveyStatus(1);

                        datum.setTitle(list.get(position).getTitle());
                        datum.setDescription(list.get(position).getDescription());
                        datum.setCategoryId(list.get(position).getCategoryId());
                        datum.setPostType(list.get(position).getPostType());
                        datum.setMediaType(list.get(position).getMediaType());
                        datum.setImages(list.get(position).getImages());
                        datum.setVideos(list.get(position).getVideos());
                        datum.setVideoUrl(list.get(position).getVideoUrl());
                        datum.setThumbnail(list.get(position).getThumbnail());
                        datum.setPublishedTime(list.get(position).getPublishedTime());
                        datum.setShares(list.get(position).getShares());
                        datum.setDownloads(list.get(position).getDownloads());
                        datum.setStickyPosition(list.get(position).getStickyPosition());
                        datum.setDownloads(list.get(position).getDownloads());
                        datum.setCommentsFlag(list.get(position).getCommentsFlag());
                        datum.setWriterType(list.get(position).getWriterType());
                        datum.setShareLink(list.get(position).getShareLink());
                        datum.setBackgroundImage(list.get(position).getBackgroundImage());
                        datum.setPostId(list.get(position).getPostId());
                        datum.setWriterId(list.get(position).getWriterId());
                        datum.setSubPostId(list.get(position).getSubPostId());
                        datum.setWriterName(list.get(position).getWriterName());
                        datum.setWriterType(list.get(position).getWriterType());
                        datum.setRole(list.get(position).getRole());
                        datum.setIsFollow(list.get(position).getIsFollow());
                        datum.setYes_percent(surveyFeedbackResponse.getData().get(0).getPercent());
                        datum.setNo_percent(surveyFeedbackResponse.getData().get(1).getPercent());
                        datum.setYes_option(list.get(position).getYes_option());
                        datum.setNo_option(list.get(position).getNo_option());


                        list.set(position, datum);
                        //list.get(position).setYes_percent();
                        //String percentageYes = " "+surveyFeedbackResponse.getData().get(position).getPercent();
                        percentageYes = " " + surveyFeedbackResponse.getData().get(0).getPercent();
                        percentageNo = " " + surveyFeedbackResponse.getData().get(1).getPercent();

                        tvYesPercent.setText(" " + surveyFeedbackResponse.getData().get(0).getPercent() + "%");
                        tvNoPercent.setText(" " + surveyFeedbackResponse.getData().get(1).getPercent() + "%");

                    }

                } else {

                }

            }

            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {


            }
        });

    }

    public void setViewsApi() {

        try {
            JsonObject otp = jsonObjectRequest_Views();
            APIService apiService = RestApiClient.getClient().create(APIService.class);
            Call<JsonObject> apiResponse = apiService.get_views("v3", otp);

            apiResponse.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                    JsonObject encreptionRequest = response.body();
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {

                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private JsonObject jsonObjectRequest_Views() {

        ListOfUserArticlesRequest requestModel = new ListOfUserArticlesRequest();
        requestModel.setPostId((long) updated_post_id_firebase);
        requestModel.setDraftId(SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME));   //


        String jsonString = new com.google.gson.Gson().toJson(requestModel);
        String encrpt_data = null;
        encrpt_data = encrypt(jsonString);
        showLog("API REQUEST E" + jsonString.toString());
        EncritonRequestData Encriton_Request = new EncritonRequestData();
        Encriton_Request.setData(encrpt_data);
        return new Gson().toJsonTree(Encriton_Request).getAsJsonObject();
    }

    public void writeNewUser(String userId, String type_firebase, long current_time, int draft_id, String device_id, int language_id, int post_id, int flip_count) {
        User user = new User(type_firebase, current_time, draft_id, device_id, language_id, post_id, flip_count);

        myRef.child("Events").push().setValue(user);
    }

    @IgnoreExtraProperties
    @Keep
    public class User {

        public String type;
        long current_time_view = System.currentTimeMillis();
        public long created_at;
        public int draft_id;
        public String device_id;
        public int language_id;
        public int post_id;
        public int flip_index;

        public User() {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }

        public User(String type_firebase, long current_time, int draft_id, String device_id, int language_id, int post_id, int flip_count) {
            this.type = type_firebase;
            this.created_at = current_time_view;
            this.draft_id = draft_id;
            this.device_id = device_id;
            this.language_id = language_id;
            this.post_id = post_id;
            this.flip_index = flip_count;
        }

    }

    // button click firebase event
    public void writeButtonClick(String userId, String type_firebase, long current_time, int draft_id, String device_id, String button_name) {
        ButtonClick user = new ButtonClick(type_firebase, current_time, draft_id, device_id, button_name);

        myRef.child("Events").push().setValue(user);
    }

    @IgnoreExtraProperties
    @Keep
    public class ButtonClick {

        public String type;
        long current_time_view = System.currentTimeMillis();
        public long created_at;
        public int draft_id;
        public String device_id;
        public String button_name;

        public ButtonClick() {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }

        public ButtonClick(String type_firebase, long current_time, int draft_id, String device_id, String button_name) {
            this.type = type_firebase;
            this.created_at = current_time_view;
            this.draft_id = draft_id;
            this.device_id = device_id;
            this.button_name = button_name;
        }

    }

    /**
     * This function assumes logger is an instance of AppEventsLogger and has been
     * created using AppEventsLogger.newLogger() call.
     */
    public void logSentFriendRequestEvent() {
        try {
            AppEventsLogger logger = AppEventsLogger.newLogger(this);
            //AppEventsLogger logger = null;
            logger.logEvent("sentFriendRequest");
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     * Play Store Special blink: attach overlay by menu id, never by child index.
     * Index-based inflate was parking the pulse on Daily Updates after center-slot rebuilds.
     */
    private void setupSpecialTabBulb() {
        if (bottomNavigationView == null || isFinishing() || isDestroyed()) {
            return;
        }
        if (!isFooterSpecialTabAllowed()) {
            stopSpecialTabBlinkAnimation();
            if (specialTabBulbOverlay != null) {
                specialTabBulbOverlay.setVisibility(View.GONE);
            }
            return;
        }
        ensureSpecialMenuIconAlwaysVisible();
        ensureSpecialTabBulbOverlay();
        stripSpecialPulseFromOtherFooterItems();
        if (!specialTabViewedThisSession) {
            startSpecialTabBlinkAnimation();
        } else {
            showSpecialTabStaticBulb();
        }
    }

    private boolean isFooterSpecialTabAllowed() {
        try {
            return isDeviceOnline() && ProfileHubConfigHelper.isFooterShowSpecial(this);
        } catch (Exception e) {
            return false;
        }
    }

    private void ensureSpecialMenuIconAlwaysVisible() {
        if (bottomNavigationView == null || !isFooterSpecialTabAllowed()) {
            return;
        }
        try {
            MenuItem special = bottomNavigationView.getMenu().findItem(R.id.navigation_catagiries);
            if (special != null) {
                special.setIcon(R.drawable.ic_footer_special);
                special.setVisible(true);
                special.setEnabled(true);
            }
            MenuItem profile = bottomNavigationView.getMenu().findItem(R.id.navigation_profile);
            if (profile != null) {
                profile.setIcon(R.drawable.ic_nav_profile);
            }
        } catch (Exception ignored) {
        }
        BottomNavigationItemView itemView = findSpecialTabItemView();
        if (itemView == null) {
            return;
        }
        View defaultIcon = itemView.findViewById(
                com.google.android.material.R.id.navigation_bar_item_icon_view);
        if (defaultIcon != null) {
            defaultIcon.setVisibility(View.VISIBLE);
            defaultIcon.setAlpha(1f);
            if (defaultIcon instanceof ImageView) {
                ImageView iv = (ImageView) defaultIcon;
                iv.setImageResource(R.drawable.ic_footer_special);
                iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
            }
        }
        applyFooterTabIconTints();
    }

    private void applyFooterTabIconTints() {
        if (bottomNavigationView == null) {
            return;
        }
        ColorStateList tint = bottomNavigationView.getItemIconTintList();
        if (tint == null) {
            tint = FooterStyleHelper.resolveIconTextTint(this);
        }
        if (tint == null) {
            tint = ContextCompat.getColorStateList(this, R.color.selector_item_primary_color);
        }
        if (tint == null) {
            return;
        }
        try {
            bottomNavigationView.setItemIconTintList(tint);
            BottomNavigationMenuView menuView =
                    (BottomNavigationMenuView) bottomNavigationView.getChildAt(0);
            if (menuView == null) {
                return;
            }
            for (int i = 0; i < menuView.getChildCount(); i++) {
                View item = menuView.getChildAt(i);
                if (!(item instanceof BottomNavigationItemView)) {
                    continue;
                }
                View iconView = item.findViewById(
                        com.google.android.material.R.id.navigation_bar_item_icon_view);
                if (iconView instanceof ImageView) {
                    ImageView iv = (ImageView) iconView;
                    iv.clearColorFilter();
                    ImageViewCompat.setImageTintList(iv, tint);
                }
            }
            if (specialTabBulbOverlay != null
                    && specialTabBulbOverlay.getVisibility() == View.VISIBLE
                    && !specialTabViewedThisSession
                    && isViewInsideFooterItem(specialTabBulbOverlay, R.id.navigation_catagiries)) {
                specialTabBulbOverlay.setColorFilter(
                        ContextCompat.getColor(this, R.color.brand_orange), PorterDuff.Mode.SRC_ATOP);
            }
        } catch (Exception ignored) {
        }
    }

    private void ensureSpecialTabBulbOverlay() {
        if (bottomNavigationView == null || !isFooterSpecialTabAllowed()) {
            return;
        }
        if (specialTabBulbOverlay != null
                && specialTabBulbOverlay.getParent() != null
                && !isViewInsideFooterItem(specialTabBulbOverlay, R.id.navigation_catagiries)) {
            detachMisplacedSpecialOverlay();
            stripSpecialPulseFromOtherFooterItems();
        }
        if (specialTabBulbOverlay != null
                && specialTabBulbOverlay.getParent() != null
                && specialTabBulbOverlay.isAttachedToWindow()
                && isViewInsideFooterItem(specialTabBulbOverlay, R.id.navigation_catagiries)) {
            if (specialTabViewedThisSession) {
                specialTabBulbOverlay.setVisibility(View.GONE);
            } else {
                specialTabBulbOverlay.setVisibility(View.VISIBLE);
                specialTabBulbOverlay.setImageResource(R.drawable.ic_footer_special);
            }
            return;
        }
        specialTabBulbOverlay = null;
        BottomNavigationItemView itemView = findSpecialTabItemView();
        if (itemView == null) {
            if (specialTabBulbSetupRetryCount < 16) {
                specialTabBulbSetupRetryCount++;
                bottomNavigationView.postDelayed(this::ensureSpecialTabBulbOverlay, 50);
            }
            return;
        }
        specialTabBulbSetupRetryCount = 0;
        View existingOverlay = itemView.findViewById(R.id.special_tab_bulb_overlay);
        if (existingOverlay instanceof ImageView) {
            specialTabBulbOverlay = (ImageView) existingOverlay;
        } else {
            LayoutInflater.from(this).inflate(R.layout.badge_layout, itemView, true);
            specialTabBulbOverlay = itemView.findViewById(R.id.special_tab_bulb_overlay);
        }
        if (specialTabBulbOverlay != null) {
            specialTabBulbOverlay.setImageResource(R.drawable.ic_footer_special);
            specialTabBulbOverlay.setScaleType(ImageView.ScaleType.FIT_CENTER);
            specialTabBulbOverlay.setAlpha(1f);
            specialTabBulbOverlay.setVisibility(
                    specialTabViewedThisSession ? View.GONE : View.VISIBLE);
            specialTabBulbOverlay.bringToFront();
        }
        ensureSpecialMenuIconAlwaysVisible();
    }

    private void clearSpecialTabBlink() {
        specialTabViewedThisSession = true;
        SharedPrefsData.putBool(HomePage.this, Constants.Event_Status, false, Constants.PREF_NAME);
        showSpecialTabStaticBulb();
    }

    private void showSpecialTabStaticBulb() {
        stopSpecialTabBlinkAnimation();
        specialTabBulbSetupRetryCount = 0;
        ensureSpecialMenuIconAlwaysVisible();
        if (specialTabBulbOverlay != null) {
            specialTabBulbOverlay.setVisibility(View.GONE);
            specialTabBulbOverlay.clearColorFilter();
            specialTabBulbOverlay.setAlpha(1f);
        }
        stripSpecialPulseFromOtherFooterItems();
        applyFooterTabIconTints();
    }

    @Nullable
    private BottomNavigationItemView findSpecialTabItemView() {
        View found = findFooterMenuItemView(R.id.navigation_catagiries);
        return found instanceof BottomNavigationItemView
                ? (BottomNavigationItemView) found
                : null;
    }

    private void startSpecialTabBlinkAnimation() {
        if (specialTabViewedThisSession || !isFooterSpecialTabAllowed()) {
            return;
        }
        ensureSpecialTabBulbOverlay();
        stripSpecialPulseFromOtherFooterItems();
        View blinkTarget = null;
        if (specialTabBulbOverlay != null
                && specialTabBulbOverlay.getParent() != null
                && isViewInsideFooterItem(specialTabBulbOverlay, R.id.navigation_catagiries)) {
            specialTabBulbOverlay.setVisibility(View.VISIBLE);
            blinkTarget = specialTabBulbOverlay;
        }
        if (blinkTarget == null) {
            return;
        }
        if (specialTabBulbAnimator != null) {
            specialTabBulbAnimator.cancel();
            specialTabBulbAnimator = null;
        }
        blinkTarget.setVisibility(View.VISIBLE);
        if (blinkTarget instanceof ImageView) {
            ((ImageView) blinkTarget).setColorFilter(
                    ContextCompat.getColor(this, R.color.brand_orange), PorterDuff.Mode.SRC_ATOP);
        }
        final View pulseTarget = blinkTarget;
        ObjectAnimator animator = ObjectAnimator.ofFloat(pulseTarget, View.ALPHA, 1f, 0.4f);
        animator.setDuration(700);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            if (pulseTarget == specialTabBulbOverlay
                    && isViewInsideFooterItem(pulseTarget, R.id.navigation_catagiries)) {
                return;
            }
            animation.cancel();
            if (specialTabBulbAnimator == animation) {
                specialTabBulbAnimator = null;
            }
            if (pulseTarget != null
                    && !isViewInsideFooterItem(pulseTarget, R.id.navigation_catagiries)) {
                pulseTarget.setVisibility(View.GONE);
                pulseTarget.clearAnimation();
                pulseTarget.setAlpha(1f);
                if (pulseTarget instanceof ImageView) {
                    ((ImageView) pulseTarget).clearColorFilter();
                }
                if (pulseTarget.getParent() instanceof ViewGroup) {
                    ((ViewGroup) pulseTarget.getParent()).removeView(pulseTarget);
                }
                if (specialTabBulbOverlay == pulseTarget) {
                    specialTabBulbOverlay = null;
                }
            }
            ensureSpecialTabBulbOverlay();
            if (!specialTabViewedThisSession && isFooterSpecialTabAllowed()) {
                startSpecialTabBlinkAnimation();
            }
        });
        specialTabBulbAnimator = animator;
        animator.start();
    }

    private void stopSpecialTabBlinkAnimation() {
        if (specialTabBulbAnimator != null) {
            specialTabBulbAnimator.cancel();
            specialTabBulbAnimator = null;
        }
        if (specialTabBulbOverlay != null) {
            specialTabBulbOverlay.clearAnimation();
            specialTabBulbOverlay.clearColorFilter();
            specialTabBulbOverlay.setAlpha(1f);
        }
        stripSpecialPulseFromOtherFooterItems();
    }

    /** Footer long-press opens the Origin/CF debug page (kept reachable from footer). */
    private void wireOriginCallsFooterLongPress() {
        if (bottomNavigationView == null || isFinishing() || isDestroyed()) {
            return;
        }
        View.OnLongClickListener open = v -> {
            try {
                startActivity(new Intent(HomePage.this, OriginCallsDebugActivity.class));
            } catch (Exception ignored) {
            }
            return true;
        };
        View status = findFooterItemViewById(R.id.navigation_status);
        if (status != null) {
            status.setOnLongClickListener(open);
        }
        View profile = findFooterItemViewById(R.id.navigation_profile);
        if (profile != null) {
            profile.setOnLongClickListener(open);
        }
    }

    @Nullable
    private View findFooterItemViewById(int itemId) {
        if (bottomNavigationView == null) {
            return null;
        }
        View menuChild = bottomNavigationView.getChildAt(0);
        if (!(menuChild instanceof BottomNavigationMenuView)) {
            return null;
        }
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) menuChild;
        for (int i = 0; i < menuView.getChildCount(); i++) {
            View child = menuView.getChildAt(i);
            if (!(child instanceof BottomNavigationItemView)) {
                continue;
            }
            MenuItem data = ((BottomNavigationItemView) child).getItemData();
            if (data != null && data.getItemId() == itemId) {
                return child;
            }
        }
        return null;
    }

    private boolean isViewInsideFooterItem(@Nullable View view, int itemId) {
        View current = view;
        while (current != null) {
            if (current instanceof BottomNavigationItemView) {
                MenuItem data = ((BottomNavigationItemView) current).getItemData();
                return data != null && data.getItemId() == itemId;
            }
            if (!(current.getParent() instanceof View)) {
                return false;
            }
            current = (View) current.getParent();
        }
        return false;
    }

    private void detachMisplacedSpecialOverlay() {
        if (specialTabBulbOverlay == null) {
            return;
        }
        if (isViewInsideFooterItem(specialTabBulbOverlay, R.id.navigation_catagiries)) {
            return;
        }
        specialTabBulbOverlay.clearAnimation();
        specialTabBulbOverlay.clearColorFilter();
        specialTabBulbOverlay.setAlpha(1f);
        if (specialTabBulbOverlay.getParent() instanceof ViewGroup) {
            ((ViewGroup) specialTabBulbOverlay.getParent()).removeView(specialTabBulbOverlay);
        }
        specialTabBulbOverlay = null;
    }

    private void stripSpecialPulseFromOtherFooterItems() {
        if (bottomNavigationView == null) {
            return;
        }
        View menuChild = bottomNavigationView.getChildAt(0);
        if (!(menuChild instanceof BottomNavigationMenuView)) {
            return;
        }
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) menuChild;
        for (int i = 0; i < menuView.getChildCount(); i++) {
            View child = menuView.getChildAt(i);
            if (!(child instanceof BottomNavigationItemView)) {
                continue;
            }
            BottomNavigationItemView itemView = (BottomNavigationItemView) child;
            MenuItem data = itemView.getItemData();
            if (data == null || data.getItemId() == R.id.navigation_catagiries) {
                continue;
            }
            removeSpecialOverlays(itemView);
            View icon = itemView.findViewById(
                    com.google.android.material.R.id.navigation_bar_item_icon_view);
            if (icon != null) {
                icon.clearAnimation();
                icon.setAlpha(1f);
                if (icon instanceof ImageView) {
                    ImageView iv = (ImageView) icon;
                    iv.clearColorFilter();
                    int iconRes = footerIconResForItem(data.getItemId());
                    if (iconRes != 0) {
                        iv.setImageResource(iconRes);
                        ColorStateList tint = bottomNavigationView.getItemIconTintList();
                        if (tint != null) {
                            ImageViewCompat.setImageTintList(iv, tint);
                        }
                    }
                }
            }
        }
    }

    private void removeSpecialOverlays(@Nullable ViewGroup item) {
        if (item == null) {
            return;
        }
        for (int i = item.getChildCount() - 1; i >= 0; i--) {
            View child = item.getChildAt(i);
            if (child.getId() != R.id.special_tab_bulb_overlay) {
                continue;
            }
            child.clearAnimation();
            child.setAlpha(1f);
            if (child instanceof ImageView) {
                ((ImageView) child).clearColorFilter();
            }
            item.removeViewAt(i);
            if (child == specialTabBulbOverlay) {
                specialTabBulbOverlay = null;
            }
        }
    }

    private static int footerIconResForItem(int itemId) {
        if (itemId == R.id.navigation_home) {
            return R.drawable.refresh_icon;
        }
        if (itemId == R.id.navigation_status) {
            return R.drawable.ic_footer_daily_updates;
        }
        if (itemId == R.id.navigation_profile) {
            return R.drawable.ic_nav_profile;
        }
        return 0;
    }

    private void liftSpecialPulseBeforeFooterRebind() {
        try {
            if (specialTabBulbAnimator != null) {
                Animator running = specialTabBulbAnimator;
                specialTabBulbAnimator = null;
                running.cancel();
            }
            if (specialTabBulbOverlay != null) {
                ImageView overlay = specialTabBulbOverlay;
                specialTabBulbOverlay = null;
                overlay.clearAnimation();
                overlay.animate().cancel();
                overlay.clearColorFilter();
                overlay.setAlpha(1f);
                overlay.setVisibility(View.GONE);
                if (overlay.getParent() instanceof ViewGroup) {
                    ((ViewGroup) overlay.getParent()).removeView(overlay);
                }
            }
        } catch (Exception ignored) {
        }
    }
    private void sendNotification2() {
        showLog("Notification:4444444444");
        Intent newIntent=new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
        newIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY|Intent.FLAG_GRANT_READ_URI_PERMISSION);
        PendingIntent pendingIntent1=PendingIntent.getActivity(context,0,newIntent,PendingIntent.FLAG_IMMUTABLE);


        NotificationManager notificationManager2 = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel("CHANNEL_ID", "HIGH", NotificationManager.IMPORTANCE_HIGH);
        channel.enableLights(true);
        channel.setLightColor(Color.GREEN);
        channel.enableVibration(false);
        notificationManager2.createNotificationChannel(channel);

        // Register the channel with the syste
        Notification.Builder n = new Notification.Builder(context,"CHANNEL_ID")
                .setContentTitle("14 version")
                .setContentText("Subject")
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pendingIntent1)
                .setAutoCancel(true);
        notificationManager2.notify(new Random().nextInt(5000), n.build());
    }
    private void sendNotification(Bitmap imgUrl, String title, String post_id, String object) {
        RemoteViews collapsedView = new RemoteViews(getPackageName(), R.layout.new_collapsed_request);
        RemoteViews expandedView = new RemoteViews(getPackageName(), R.layout.new_request);
        final Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        String GROUP_KEY_WORK_EMAIL = "com.anyNews.anynews.Services.WORK_EMAIL";
        Spannable pnTitle=new SpannableString(title);
        try {
            // String  content = title.substring(title.indexOf("#") + 1);
            Pattern p = Pattern.compile("\\[.*?\\]");
            Matcher matcher = p.matcher(title);
            if(matcher.find()){
                String  content = (matcher.group().subSequence(1, matcher.group().length()-1)).toString();
                showLog("Content:"+content);
                showLog("Content:"+content.length());
                showLog("Content:"+title.length());
                title= title.replace("[","").replace("]","");
                Spannable spannable = new SpannableString(title);
                String str = spannable.toString();
                int iStart = str.indexOf(content);
                int length = content.length();
                int iEnd = iStart + length;/*10 characters = in-network. */
                final StyleSpan bss = new StyleSpan(android.graphics.Typeface.BOLD);
                spannable.setSpan(new ForegroundColorSpan(Color.BLUE), iStart, iEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannable.setSpan(bss, iStart, iEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                pnTitle=spannable;
            }

        } catch (IndexOutOfBoundsException e    ) {
            e.printStackTrace();
        }

        collapsedView.setTextViewText(R.id.notification_titleE,pnTitle);
        collapsedView.setTextViewText(R.id.notification_titleTEL,pnTitle);
        collapsedView.setTextViewText(R.id.notification_titleTa,pnTitle);

        int languageId = SharedPrefsData.getInt(getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME);
        if (languageId == 4) {
            collapsedView.setViewVisibility(R.id.notification_titleE, View.GONE);
            collapsedView.setViewVisibility(R.id.notification_titleTEL,View.GONE);
            collapsedView.setViewVisibility(R.id.notification_titleTa,View.VISIBLE);
        } else if (languageId == 1) {
            collapsedView.setViewVisibility(R.id.notification_titleE,View.VISIBLE);
            collapsedView.setViewVisibility(R.id.notification_titleTEL,View.GONE);
            collapsedView.setViewVisibility(R.id.notification_titleTa,View.GONE);
        }else{
            collapsedView.setViewVisibility(R.id.notification_titleE,View.GONE);
            collapsedView.setViewVisibility(R.id.notification_titleTEL,View.VISIBLE);
            collapsedView.setViewVisibility(R.id.notification_titleTa,View.GONE);
        }


        expandedView.setTextViewText(R.id.notification_title,title);
        Intent resultIntent = new Intent(getApplicationContext(), StartingActivity.class);
        resultIntent.putExtra("notification", true);
        resultIntent.putExtra("TITILE", post_id);
        resultIntent.putExtra("TITLE_REWARDS",title);
        resultIntent.putExtra("OBJECT", object);

        // ary answe my call

        System.out.println("TITTIL_OBJECT" + post_id);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // add notification one by one in case of multiple
        //PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), Integer.parseInt(post_id), resultIntent, PendingIntent.FLAG_IMMUTABLE);
        // only updated one will show and over ride
        //final PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), Integer.parseInt(post_id), resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        /*PendingIntent resultPendingIntent = PendingIntent.getPendingIntent(Integer.parseInt(post_id)
                        , PendingIntent.FLAG_UPDATE_CURRENT);*/
        // flag mutable |
        PendingIntent pendingIntent;

        if (Build.VERSION.SDK_INT >= 31) {
            pendingIntent = PendingIntent.getActivity(
                    getApplicationContext(),
                    Integer.parseInt(post_id),
                    resultIntent,
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT // setting the mutability flag
            );


        } else {
            pendingIntent = PendingIntent.getActivity(this,
                    Integer.parseInt(post_id), resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        }

        try {

            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), Constants.Channel_ID)
                    // use xxxhdpi
                    .setSmallIcon(R.drawable.ic_arman_notificatio)
//                    .setContentTitle("" + title)
//                    .setContentText(body)
                    .setCustomContentView(collapsedView)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setVibrate(new long[]{1000, 1000, 1000, 1000})
                    .setSound(uri)
                    .setShowWhen(false)
                    .setGroup(GROUP_KEY_WORK_EMAIL)
                    .setGroupSummary(false)
                    .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder.setSmallIcon(R.drawable.ic_arman_notificatio);
                builder.setColor(getResources().getColor(R.color.colorPrimary));
            } else {
                builder.setSmallIcon(R.drawable.ic_arman_notificatio);
            }
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());

            Random random = new Random();
            int m = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
            //notificationManager.notify(random.nextInt(), builder.build());
            notificationManager.notify(m, builder.build());
        } catch (Exception e) {
            Log.v("NotificationData", String.valueOf(e.getMessage()));
        }


    }
    public void showProgress(){
        layoutCommentProgressbar.setVisibility(View.VISIBLE);

    }
    public void hideProgress(){
        layoutCommentProgressbar.setVisibility(View.GONE);
    }

    private void BundleDataToGcmPush(int pos) {
        try {
            HashMap data = new HashMap();
            data.put("POST_TYPE",list.get(pos).getPostType());
            data.put("POSTID", ""+list.get(pos).getPostId());
            data.put("POST_POS", ""+pos);
            data.put("VERSION_CODE", ""+BuildConfig.VERSION_CODE);
            data.put("VERSION_NAME", ""+BuildConfig.VERSION_NAME);
            data.put("EVENT_NAME", "onFlip");
            data.put("PLATFORM", "ANDROID");
            data.put("DRAFTID", ""+SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME));
            if(list!=null){
                if(list.get(positionx).getPostType().equalsIgnoreCase("ad")){
                    try{
                        if(adapter!=null && adapter.nativeAd.getResponseInfo()!=null && adapter.nativeAd.getResponseInfo().getLoadedAdapterResponseInfo()!=null &&
                                adapter.nativeAd.getResponseInfo().getLoadedAdapterResponseInfo().getAdapterClassName()!=null){
                            data.put("AD Type", adapter.nativeAd.getResponseInfo().getLoadedAdapterResponseInfo().getAdapterClassName());
                            data.put("AD Imps", ""+adImpress);
                            adImpress=false;
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        adImpress=false;
                    }

                    data.put("ADERROR", adError);
                    adError="";
                }
            }

            try {

                SimpleDateFormat s = new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss aa");
                String format = s.format(new Date());
                data.put("DATE_TIME", format);

            } catch (Exception e) {

                e.printStackTrace();

            }

            UUID uuid = UUID.randomUUID();
            String uuidAsString = uuid.toString();
            showLog("UUID:"+uuidAsString);
            showLog("GCM DAta:::"+data);
            final String finalUuid = uuidAsString;
            final HashMap finalData = data;
            new android.os.AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    try {
                        firebaseMessaging.send(new RemoteMessage.Builder("291191833913" + "@gcm.googleapis.com")
                                .setMessageId(finalUuid)
                                .setData(finalData)
                                .build());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }.execute();
        } catch (Exception e) {
            e.printStackTrace();

        }

    }
    private void PlaystoreUpdateDialog() {

        androidx.appcompat.app.AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(this.getString(R.string.app_name));
        alertDialogBuilder.setMessage(this.getString(R.string.update_message));
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton(R.string.update_now, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                try {
                    ensurePlayInAppUpdateHelper();
                    if (playInAppUpdateHelper != null) {
                        playInAppUpdateHelper.startFlexibleUpdate();
                        return;
                    }
                } catch (Exception ignored) {
                }
                PlayInAppUpdateHelper.openPlayStoreListing(HomePage.this);
            }
        });
        alertDialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });
        if (!isFinishing()){
            alertDialogBuilder.show();
        }
        //alertDialogBuilder.show();


    }
    private void movetoappStore() {

        final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
        try {
            if (getApplicationContext() != null && getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)){
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
            }

        } catch (android.content.ActivityNotFoundException anfe) {
            if (getApplicationContext() != null && getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)){
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
            }
        }
    }
    private JsonObject SetUpdateLanguage(int language_id) {

        LanguageSelectionRequest requestModel = new LanguageSelectionRequest();
        requestModel.setDraftId(SharedPrefsData.getInt(context, Constants.DraftID, Constants.PREF_NAME));   //
        requestModel.setLanguageId(language_id);
        return new Gson().toJsonTree(requestModel).getAsJsonObject();

    }

    private void LanguageSelection(int language_id) {

        JsonObject otp = SetUpdateLanguage(language_id);

        showLog("API REQUEST E "+otp.toString());
        APIService apiService = RestApiClient.getClient().create(APIService.class);
        Call<JsonObject> otp_generation = apiService.Update_Language("v3",otp);
        otp_generation.enqueue(new Callback<JsonObject>() {

            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                if (response.isSuccessful()) {

                    JsonObject jsonObject = response.body().getAsJsonObject();
                    showLog("API REQUEST R "+jsonObject);
                    Gson gson = new Gson();
                    LanguageModelResponse languageSelectionResponse = gson.fromJson(jsonObject, LanguageModelResponse.class);
                    showLog("API REQUEST R "+languageSelectionResponse.toString());
                    if (languageSelectionResponse.getSuccess()) {
                        allsharedpreference.setShared_Booleen(Allsharedpreference.Language, true);
                        String json = gson.toJson(languageSelectionResponse); // myObject - instance of MyObject
                        allsharedpreference.setShared_String(Allsharedpreference.Translator, json);
                    }

                }

            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {

                //    Toast.makeText(context, t.getMessage(), Toast.LENGTH_LONG).show();


            }
        });

    }

    /** Lean stubs — keep calendar / fullscreen callers compiling without fat chrome. */
    public void exitCalendarToDailyUpdates() {
        try {
            performDailyUpdatesOpen();
        } catch (Exception ignored) {
        }
    }

    public void syncHomeCalendarSegmentThumb(boolean raasiSelected) {
        // no-op
    }

    public void revealHomeCalendarTtdChrome() {
        // no-op
    }

    public void checkpointFlipPositionForFullscreen() {
        try {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_USER);
        } catch (Exception ignored) {
        }
    }

    public void restoreFlipPositionAfterFullscreen() {
        try {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } catch (Exception ignored) {
        }
    }

    // -------------------------------------------------------------------------
    // Soft footer / Daily Updates (Hour 1–3). Product wiring only — does NOT
    // touch flip hot path (onFlippedToPage*, convert bind loop, fat settle).
    // -------------------------------------------------------------------------

    private boolean isDeviceOnline() {
        try {
            ConnectivityManager connectivity =
                    (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity == null) {
                return true;
            }
            NetworkInfo active = connectivity.getActiveNetworkInfo();
            return active != null && active.isConnected();
        } catch (Exception e) {
            return true;
        }
    }

    private boolean isDailyUpdatesMode() {
        return dailyUpdatesModeActive;
    }

    private void persistDailyUpdatesDestination(boolean open) {
        try {
            getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE).edit()
                    .putBoolean(Constants.DAILY_UPDATES_RESTORE, open)
                    .putString(
                            Constants.DAILY_UPDATES_RESTORE_AT,
                            open ? String.valueOf(System.currentTimeMillis()) : "0")
                    .commit();
        } catch (Exception ignored) {
        }
    }

    /** Hide embedded Daily Updates page. */
    private void hideHomeDailyUpdatesMode() {
        dailyUpdatesModeActive = false;
        persistDailyUpdatesDestination(false);
        try {
            if (homeDailyUpdatesHost != null) {
                homeDailyUpdatesHost.setVisibility(View.GONE);
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Late feed paints can show FlipView under the hub. Keep DU above while
     * that mode is active.
     */
    private void keepDailyUpdatesCovered() {
        if (!isDailyUpdatesMode() || homeDailyUpdatesHost == null
                || isFinishing() || isDestroyed()) {
            return;
        }
        try {
            if (layout_with_data != null) {
                layout_with_data.setVisibility(View.GONE);
            }
            if (comment_page != null) {
                comment_page.setVisibility(View.GONE);
            }
            if (mFlipView != null) {
                mFlipView.setVisibility(View.GONE);
            }
            homeDailyUpdatesHost.setVisibility(View.VISIBLE);
            homeDailyUpdatesHost.bringToFront();
        } catch (Exception ignored) {
        }
    }

    private void ensureDailyUpdatesController() {
        if (homeDailyUpdatesHost == null || isFinishing() || isDestroyed()) {
            return;
        }
        if (dailyUpdatesController != null) {
            return;
        }
        try {
            dailyUpdatesController = DailyUpdatesController.attachEmbedded(
                    this,
                    homeDailyUpdatesHost,
                    new DailyUpdatesController.HostCallbacks() {
                        @Override
                        public void openFlashNewsFromDailyUpdates() {
                            performFlashNewsOpen();
                        }

                        @Override
                        public void openLocalNewsFromDailyUpdates() {
                            performLocalFeedOpenFromDailyUpdates();
                        }

                        @Override
                        public void openPanchangamFromDailyUpdates() {
                            if (!isDeviceOnline()) {
                                return;
                            }
                            performCalendarOpen(false, true);
                        }

                        @Override
                        public void openRashiFromDailyUpdates() {
                            if (!isDeviceOnline()) {
                                return;
                            }
                            performCalendarOpen(true, true);
                        }

                        @Override
                        public void openCricketFromDailyUpdates() {
                            try {
                                Intent intent = new Intent(HomePage.this, MatchCenterActivity.class);
                                intent.putExtra(MatchCenterActivity.EXTRA_FROM_DAILY_UPDATES, true);
                                startActivity(intent);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            dailyUpdatesController = null;
        }
    }

    /** Soft Daily Updates tab open: hub above footer, FlipView covered. */
    private void performDailyUpdatesOpen() {
        showLog("performDailyUpdatesOpen");
        if (!ProfileHubConfigHelper.isFooterShowVideos(this) || !isDeviceOnline()) {
            return;
        }
        dailyUpdatesModeActive = true;
        persistDailyUpdatesDestination(true);
        SharedPrefsData.putBool(HomePage.this, Constants.isUnreadPostsSelected, false, Constants.PREF_NAME);
        try {
            enforceExclusiveFooterSelection(R.id.navigation_status);
        } catch (Exception ignored) {
        }
        try {
            if (mFlipView != null) {
                mFlipView.setVisibility(View.GONE);
            }
            if (layout_with_data != null) {
                layout_with_data.setVisibility(View.GONE);
            }
            if (comment_page != null) {
                comment_page.setVisibility(View.GONE);
            }
            if (txtNoData != null) {
                txtNoData.setVisibility(View.GONE);
            }
            if (post_news_scrren != null) {
                post_news_scrren.setVisibility(View.GONE);
            }
            if (floating_action_button != null) {
                floating_action_button.setVisibility(View.GONE);
            }
        } catch (Exception ignored) {
        }
        try {
            if (homeDailyUpdatesHost != null) {
                homeDailyUpdatesHost.setVisibility(View.VISIBLE);
                homeDailyUpdatesHost.bringToFront();
                homeDailyUpdatesHost.post(() -> {
                    if (isFinishing() || isDestroyed() || !isDailyUpdatesMode()) {
                        return;
                    }
                    ensureDailyUpdatesController();
                    if (dailyUpdatesController != null) {
                        dailyUpdatesController.onHostResume();
                    }
                });
            } else {
                ensureDailyUpdatesController();
                if (dailyUpdatesController != null) {
                    dailyUpdatesController.onHostResume();
                }
            }
        } catch (Exception ignored) {
            ensureDailyUpdatesController();
        }
        keepDailyUpdatesCovered();
        if (homeDailyUpdatesHost != null) {
            homeDailyUpdatesHost.post(this::keepDailyUpdatesCovered);
            homeDailyUpdatesHost.postDelayed(this::keepDailyUpdatesCovered, 250L);
        }
    }

    private void resetFeedCursorsForSoftOpen() {
        try {
            cancelListAllArticlesCall();
            SHARE_ID = 0;
            SHARE_REDIRECT_ID = "0";
            DATAFROM2G = "0";
            last_post_id = 0;
            skip = 0;
            actual_skip = 0;
            positionx = 0;
            ppi = 0;
            requestPostition = 1;
            requestSize = 0;
            isApiRequested = false;
            isDataParsed = true;
            isFromLocalArticles = false;
            dummyResponse = null;
            category_id = 0;
            nativeGoogleAdList = new ArrayList<>();
            nativeFBAdList = new ArrayList<>();
            try {
                if (mFlipView != null) {
                    mFlipView.resetSeatForNewFeed();
                }
            } catch (Exception ignored) {
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Article skeleton + L→R shimmer while first-page feed is pending.
     * Lightweight: one ObjectAnimator loop inside {@link HomeFeedRevealView}; no settle/prefetch.
     */
    private void showHomeFeedLoadingReveal() {
        try {
            if (layout_with_out_data != null) {
                layout_with_out_data.setVisibility(View.VISIBLE);
                layout_with_out_data.setClickable(true);
                layout_with_out_data.setFocusable(true);
            }
            if (homeFeedReveal != null) {
                homeFeedReveal.start();
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Dismiss loading overlay when feed binds (or empty/error). Instant hide so the
     * solid surface never sticks over FlipView.
     */
    private void hideHomeFeedLoadingReveal(boolean instant) {
        try {
            if (layout_with_out_data != null) {
                layout_with_out_data.setVisibility(View.GONE);
                layout_with_out_data.setClickable(false);
                layout_with_out_data.setFocusable(false);
            }
            if (homeFeedReveal != null) {
                homeFeedReveal.stop(true, null);
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Soft Special-tab open: special_posts page 0 without recreating activity.
     */
    private void performSpecialFeedOpen() {
        showLog("performSpecialFeedOpen");
        if (!ProfileHubConfigHelper.isFooterShowSpecial(this) || !isDeviceOnline()) {
            return;
        }
        hideHomeDailyUpdatesMode();
        hideHomeCalendarMode();
        hideLocalFeedChromeThin();
        flashNewsModeActive = false;
        localFeedFromDailyUpdates = false;
        showLog("Check Point #5 : HomePage performSpecialFeedOpen soft");
        SharedPrefsData.putBoolImmediate(
                HomePage.this, Constants.isUnreadPostsSelected, true, Constants.PREF_NAME);
        SharedPrefsData.putBool(
                HomePage.this, Constants.isVideoPostsSelected, false, Constants.PREF_NAME);
        try {
            OpenPathCache.forceNextFeedNetwork(this);
        } catch (Exception ignored) {
        }
        try {
            clearSpecialTabBlink();
            enforceExclusiveFooterSelection(R.id.navigation_catagiries);
        } catch (Exception ignored) {
        }
        try {
            if (mFlipView != null) {
                mFlipView.setVisibility(View.VISIBLE);
            }
            showHomeFeedLoadingReveal();
            if (layout_with_data != null) {
                layout_with_data.setVisibility(View.GONE);
            }
            if (comment_page != null) {
                comment_page.setVisibility(View.GONE);
            }
            if (txtNoData != null) {
                txtNoData.setVisibility(View.GONE);
            }
            if (post_news_scrren != null) {
                post_news_scrren.setVisibility(View.GONE);
            }
        } catch (Exception ignored) {
        }
        resetFeedCursorsForSoftOpen();
        inlist(0);
    }

    /**
     * Instant News footer UX: highlight News, hide DU, soft reload without finish().
     */
    private void beginInstantNewsFooterReturn(boolean forceNetworkRefresh) {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        final boolean leavingOther = isDailyUpdatesMode()
                || isCalendarMode()
                || flashNewsModeActive
                || localFeedFromDailyUpdates
                || SharedPrefsData.getBool(HomePage.this, Constants.isUnreadPostsSelected, Constants.PREF_NAME);
        try {
            hideHomeDailyUpdatesMode();
        } catch (Exception ignored) {
        }
        try {
            hideHomeCalendarMode();
        } catch (Exception ignored) {
        }
        hideLocalFeedChromeThin();
        flashNewsModeActive = false;
        localFeedFromDailyUpdates = false;
        showLog("isUnreadPostsSelected set to false #5");
        SharedPrefsData.putBoolImmediate(
                HomePage.this, Constants.isUnreadPostsSelected, false, Constants.PREF_NAME);
        SharedPrefsData.putBool(
                HomePage.this, Constants.isVideoPostsSelected, false, Constants.PREF_NAME);
        if (forceNetworkRefresh) {
            try {
                // Slot-change / soft News: force network page-0 WITHOUT greeting poll.
                OpenPathCache.forceNextFeedNetwork(this);
            } catch (Exception ignored) {
            }
        }
        try {
            selectFooterTabWithoutMotion(R.id.navigation_home);
        } catch (Exception ignored) {
        }
        try {
            if (mFlipView != null) {
                mFlipView.setVisibility(View.VISIBLE);
            }
            if (comment_page != null) {
                comment_page.setVisibility(View.GONE);
            }
            if (post_news_scrren != null) {
                post_news_scrren.setVisibility(View.GONE);
            }
        } catch (Exception ignored) {
        }
        final boolean contentReady = !leavingOther
                && list != null
                && !list.isEmpty()
                && layout_with_data != null
                && layout_with_data.getVisibility() == View.VISIBLE;
        if (!forceNetworkRefresh && contentReady) {
            try {
                hideHomeFeedLoadingReveal(true);
                layout_with_data.setVisibility(View.VISIBLE);
            } catch (Exception ignored) {
            }
            return;
        }
        try {
            showHomeFeedLoadingReveal();
            if (layout_with_data != null) {
                layout_with_data.setVisibility(View.GONE);
            }
        } catch (Exception ignored) {
        }
        resetFeedCursorsForSoftOpen();
        final Runnable heavy = () -> {
            if (isFinishing() || isDestroyed()) {
                return;
            }
            try {
                inlist(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        try {
            if (bottomNavigationView != null) {
                bottomNavigationView.post(heavy);
            } else {
                heavy.run();
            }
        } catch (Exception e) {
            heavy.run();
        }
    }

    private void openProfileInstant() {
        if (getApplicationContext() == null
                || !getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)) {
            return;
        }
        try {
            SharedPrefsData.putBool(HomePage.this, Constants.isUnreadPostsSelected, false, Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
        Intent profile = new Intent(HomePage.this, newProfileDetails.class);
        profile.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(profile);
        try {
            overridePendingTransition(0, 0);
        } catch (Exception ignored) {
        }
    }

    /**
     * Profile / deep-link → News, Special, Daily Updates, or Profile without
     * CLEAR_TASK finish.
     */
    private void handleFooterTabExtra(@Nullable Intent intent) {
        if (intent == null || bottomNavigationView == null) {
            return;
        }
        if (!intent.hasExtra(EXTRA_FOOTER_TAB)) {
            return;
        }
        final int tabId = intent.getIntExtra(EXTRA_FOOTER_TAB, 0);
        intent.removeExtra(EXTRA_FOOTER_TAB);
        if (tabId == 0) {
            return;
        }
        if (tabId == R.id.navigation_home) {
            try {
                LanguageFeedInvalidator.clearDailyUpdatesHubRestore(this);
            } catch (Exception ignored) {
            }
            beginInstantNewsFooterReturn(true);
            return;
        }
        if (tabId == R.id.navigation_catagiries) {
            performSpecialFeedOpen();
            return;
        }
        if (tabId == R.id.navigation_status) {
            performDailyUpdatesOpen();
            return;
        }
        if (tabId == R.id.navigation_profile) {
            openProfileInstant();
        }
    }

    private void selectFooterTabWithoutMotion(int itemId) {
        if (bottomNavigationView == null || itemId == 0) {
            return;
        }
        if (bottomNavigationView.getSelectedItemId() != itemId) {
            final boolean prev = suppressBottomNavCallbacks;
            suppressBottomNavCallbacks = true;
            try {
                bottomNavigationView.setSelectedItemId(itemId);
            } catch (Exception ignored) {
            } finally {
                suppressBottomNavCallbacks = prev;
            }
        }
        enforceExclusiveFooterSelection(itemId);
    }

    private void enforceExclusiveFooterSelection(int wantId) {
        if (bottomNavigationView == null || wantId == 0) {
            return;
        }
        try {
            if (wantId != R.id.navigation_catagiries) {
                // Collapse dual-orange / misplaced pulse on DU, then re-anchor blink on Special
                // if Event_Status is still pending (Play Store keeps Special pulse while on News).
                stopSpecialTabBlinkAnimation();
                if (specialTabBulbOverlay != null) {
                    specialTabBulbOverlay.setVisibility(View.GONE);
                    specialTabBulbOverlay.clearColorFilter();
                    specialTabBulbOverlay.setAlpha(1f);
                }
                stripSpecialPulseFromOtherFooterItems();
                if (!specialTabViewedThisSession && isFooterSpecialTabAllowed()) {
                    bottomNavigationView.post(this::setupSpecialTabBulb);
                }
            }
        } catch (Exception ignored) {
        }
        try {
            Menu menu = bottomNavigationView.getMenu();
            for (int i = 0; i < menu.size(); i++) {
                MenuItem mi = menu.getItem(i);
                if (mi == null) {
                    continue;
                }
                final boolean want = mi.getItemId() == wantId;
                if (mi.isChecked() != want) {
                    mi.setChecked(want);
                }
            }
        } catch (Exception ignored) {
        }
        try {
            BottomNavigationMenuView menuView =
                    (BottomNavigationMenuView) bottomNavigationView.getChildAt(0);
            if (menuView == null) {
                return;
            }
            for (int i = 0; i < menuView.getChildCount(); i++) {
                View child = menuView.getChildAt(i);
                if (!(child instanceof BottomNavigationItemView)) {
                    continue;
                }
                BottomNavigationItemView itemView = (BottomNavigationItemView) child;
                MenuItem data = itemView.getItemData();
                if (data == null) {
                    continue;
                }
                final boolean want = data.getItemId() == wantId;
                if (itemView.isSelected() != want) {
                    itemView.setSelected(want);
                }
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Admin slot visibility: News | Special | empty center (WhatsApp/Share) | Daily Updates | Profile.
     * Calendar stays product-hidden ({@link ProfileHubConfigHelper#isFooterShowCalendar}).
     * Center never paints Panchangam — empty spacer under overlays (Play Store parity).
     */
    private void applyFooterSlotVisibility() {
        if (bottomNavigationView == null || bottomNavigationView.getMenu() == null) {
            return;
        }
        try {
            Menu menu = bottomNavigationView.getMenu();
            boolean online = isDeviceOnline();
            boolean showReload = ProfileHubConfigHelper.isFooterShowReload(this);
            boolean showSpecial = online && ProfileHubConfigHelper.isFooterShowSpecial(this);
            boolean showCalendar = online && ProfileHubConfigHelper.isFooterShowCalendar(this);
            boolean showVideos = online && ProfileHubConfigHelper.isFooterShowVideos(this);
            boolean showProfile = online && ProfileHubConfigHelper.isFooterShowProfile(this);

            setFooterMenuItemVisible(menu.findItem(R.id.navigation_home), showReload);
            setFooterMenuItemVisible(menu.findItem(R.id.navigation_catagiries), showSpecial);
            // Middle column: real Panchangam only if admin calendar on; else empty Share/WA spacer.
            MenuItem centerSlot = menu.findItem(R.id.navigation_stricker);
            if (!online) {
                hideFooterWhatsapp();
                centerFooterSpacerPrepared = false;
                setFooterMenuItemVisible(centerSlot, false);
            } else if (showCalendar) {
                centerFooterSpacerPrepared = false;
                if (centerSlot != null) {
                    centerSlot.setIcon(R.drawable.ic_footer_panchangam);
                }
                setFooterMenuItemVisible(centerSlot, true);
            } else {
                prepareMagazineShareCenterSlot(centerSlot);
                hideShareCenterSlotChrome();
            }
            setFooterMenuItemVisible(menu.findItem(R.id.navigation_status), showVideos);
            setFooterMenuItemVisible(menu.findItem(R.id.navigation_profile), showProfile);

            if (!showSpecial) {
                try {
                    stopSpecialTabBlinkAnimation();
                    if (specialTabBulbOverlay != null) {
                        specialTabBulbOverlay.setVisibility(View.GONE);
                    }
                } catch (Exception ignored) {
                }
            } else {
                ensureSpecialMenuIconAlwaysVisible();
            }

            if (!online && showReload) {
                int selectedId = bottomNavigationView.getSelectedItemId();
                if (selectedId == R.id.navigation_catagiries
                        || selectedId == R.id.navigation_stricker
                        || selectedId == R.id.navigation_status
                        || selectedId == R.id.navigation_profile) {
                    try {
                        hideHomeDailyUpdatesMode();
                        selectFooterTabWithoutMotion(R.id.navigation_home);
                    } catch (Exception ignored) {
                    }
                }
            }
        } catch (Exception e) {
            showLog("applyFooterSlotVisibility: " + e.getMessage());
        }
    }

    private void setFooterMenuItemVisible(@Nullable MenuItem item, boolean visible) {
        if (item == null) {
            return;
        }
        try {
            boolean wasVisible = item.isVisible();
            // Pool release on setVisible can park Special overlay on Daily Updates for a frame.
            if (wasVisible != visible) {
                liftSpecialPulseBeforeFooterRebind();
            }
            item.setVisible(visible);
            item.setEnabled(visible);
            if (wasVisible != visible) {
                if (isFooterSpecialTabAllowed() && !specialTabViewedThisSession) {
                    if (bottomNavigationView != null) {
                        bottomNavigationView.post(this::setupSpecialTabBulb);
                    }
                } else {
                    stripSpecialPulseFromOtherFooterItems();
                }
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Empty middle menu column for WhatsApp / blinking Share. Special keeps its own glyph.
     */
    private void prepareMagazineShareCenterSlot(@Nullable MenuItem slot) {
        if (slot == null) {
            return;
        }
        try {
            CharSequence title = slot.getTitle();
            boolean titleEmpty = title == null || title.length() == 0;
            if (centerFooterSpacerPrepared && titleEmpty && slot.isVisible() && slot.isEnabled()) {
                return;
            }
            if (!centerFooterSpacerPrepared) {
                slot.setIcon(new ColorDrawable(Color.TRANSPARENT));
            }
            if (!titleEmpty) {
                slot.setTitle("");
            }
            if (!slot.isVisible()) {
                liftSpecialPulseBeforeFooterRebind();
                slot.setVisible(true);
            }
            if (!slot.isEnabled()) {
                slot.setEnabled(true);
            }
            centerFooterSpacerPrepared = slot.isVisible() && slot.isEnabled();
        } catch (Exception e) {
            showLog("prepareMagazineShareCenterSlot: " + e.getMessage());
        }
    }

    /** Hide spacer item icon/label; WhatsApp or magazine Share overlay is the control. */
    private void hideShareCenterSlotChrome() {
        View item = findFooterMenuItemView(R.id.navigation_stricker);
        if (item == null) {
            return;
        }
        try {
            View icon = item.findViewById(
                    com.google.android.material.R.id.navigation_bar_item_icon_view);
            if (icon != null) {
                icon.setVisibility(View.INVISIBLE);
            }
            int smallId = getResources().getIdentifier(
                    "navigation_bar_item_small_label_view", "id", getPackageName());
            int largeId = getResources().getIdentifier(
                    "navigation_bar_item_large_label_view", "id", getPackageName());
            View small = smallId != 0 ? item.findViewById(smallId) : null;
            View large = largeId != 0 ? item.findViewById(largeId) : null;
            if (small != null) {
                small.setVisibility(View.INVISIBLE);
            }
            if (large != null) {
                large.setVisibility(View.INVISIBLE);
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Pin center WhatsApp / magazine Share / reporter + onto the empty middle column
     * ({@code navigation_stricker}), never onto Special.
     */
    private void repositionCenterFabForVisibleTabs() {
        if (bottomNavigationView == null) {
            return;
        }
        try {
            View shareSlot = findFooterMenuItemView(R.id.navigation_stricker);
            float dx = overlayDxForSlot(shareSlot);
            if (floatingActionButtonHome != null) {
                floatingActionButtonHome.setTranslationX(dx);
            }
            if (imageVIewShareWhatsapp != null
                    && imageVIewShareWhatsapp.getVisibility() == View.VISIBLE) {
                imageVIewShareWhatsapp.setTranslationX(dx);
            }
            if (layoutMagazineShareFooter != null
                    && layoutMagazineShareFooter.getVisibility() == View.VISIBLE) {
                layoutMagazineShareFooter.setTranslationX(dx);
                if (shareSlot != null && shareSlot.getWidth() > 0) {
                    ViewGroup.LayoutParams lp = layoutMagazineShareFooter.getLayoutParams();
                    if (lp != null && lp.width != shareSlot.getWidth()) {
                        lp.width = shareSlot.getWidth();
                        layoutMagazineShareFooter.setLayoutParams(lp);
                    }
                }
            }
            hideShareCenterSlotChrome();
        } catch (Exception e) {
            showLog("repositionCenterFabForVisibleTabs: " + e.getMessage());
        }
    }

    private float overlayDxForSlot(@Nullable View slot) {
        if (slot == null || bottomNavigationView == null
                || slot.getWidth() <= 0 || bottomNavigationView.getWidth() <= 0) {
            return 0f;
        }
        int[] navLoc = new int[2];
        int[] slotLoc = new int[2];
        bottomNavigationView.getLocationOnScreen(navLoc);
        slot.getLocationOnScreen(slotLoc);
        float slotCenterX = slotLoc[0] + slot.getWidth() / 2f;
        float navCenterX = navLoc[0] + bottomNavigationView.getWidth() / 2f;
        return slotCenterX - navCenterX;
    }

    @Nullable
    private View findFooterMenuItemView(int itemId) {
        if (bottomNavigationView == null) {
            return null;
        }
        View menuChild = bottomNavigationView.getChildAt(0);
        if (!(menuChild instanceof BottomNavigationMenuView)) {
            return null;
        }
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) menuChild;
        for (int i = 0; i < menuView.getChildCount(); i++) {
            View child = menuView.getChildAt(i);
            if (!(child instanceof BottomNavigationItemView)) {
                continue;
            }
            BottomNavigationItemView itemView = (BottomNavigationItemView) child;
            if (itemView.getItemData() != null
                    && itemView.getItemData().getItemId() == itemId) {
                return itemView;
            }
        }
        return null;
    }

    /**
     * Half-row WhatsApp chip — same share path as center footer WhatsApp.
     * Does not re-hit like APIs or start extra feed calls.
     */
    public void onArticleWhatsappClicked(int position) {
        try {
            if (position >= 0) {
                positionx = position;
            }
            if (adapter == null || list == null || positionx < 0 || positionx >= list.size()) {
                return;
            }
            CallShareListener listener = new CallShareListener();
            listener.setShareInterfaceListener(adapter);
            listener.triggerEvent();
            View flipChild = (mFlipView != null) ? mFlipView.getChildAt(ppi) : null;
            if (flipChild == null) {
                return;
            }
            ModelClass.Datum item = list.get(positionx);
            if (item == null || item.getPostType() == null) {
                return;
            }
            String postType = item.getPostType();
            if (postType.equalsIgnoreCase("image_gallery")) {
                adapter.shareGalleryScreenShotUI(flipChild);
            } else if (postType.equalsIgnoreCase("full_image")) {
                adapter.shareFullImageScree(flipChild, positionx);
            } else if (postType.equalsIgnoreCase("normal_image")) {
                adapter.takeNormalImageScreenshot(flipChild, positionx, 1);
            } else if (postType.equalsIgnoreCase("magazine")) {
                adapter.showMagazineShareOptions(flipChild, 1);
            } else if (postType.equalsIgnoreCase("normal_video")) {
                if ("3".equals(item.getMediaType())) {
                    PlayerView pv = flipChild.findViewById(R.id.normal_video_player);
                    ImageView thumb = flipChild.findViewById(R.id.thaumbnail_for_video);
                    ImageView play = flipChild.findViewById(R.id.imageViewVideoPlayVideo);
                    ProgressBar bar = flipChild.findViewById(R.id.progress_bar_for_video);
                    adapter.shareExoPlayerVideoScreenShot(
                            flipChild, positionx, 1, pv, thumb, play, bar);
                } else if ("4".equals(item.getMediaType())) {
                    ImageView thumb = flipChild.findViewById(R.id.imageViewItem__);
                    com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView yt =
                            flipChild.findViewById(R.id.youtube_view_half_video);
                    adapter.shareYoutubeVideoScreenShot(flipChild, positionx, yt, 1, thumb);
                }
            } else if (postType.equalsIgnoreCase("full_video")) {
                if ("5".equals(item.getMediaType())) {
                    ImageView thumb = flipChild.findViewById(R.id.thaumbnail_for_video);
                    RelativeLayout exoPlay = flipChild.findViewById(R.id.exo_play_layout);
                    ImageView play = flipChild.findViewById(R.id.imageViewVideoPlayVideo);
                    PlayerView pv = flipChild.findViewById(R.id.normal_video_player);
                    adapter.shareExoPlayerFullVideoShare(
                            positionx, 1, thumb, exoPlay, play, pv);
                } else if ("6".equals(item.getMediaType())) {
                    ImageView cover = flipChild.findViewById(R.id.imageViewItem1);
                    RelativeLayout ytLayout = flipChild.findViewById(R.id.layoutYoutubeFullView);
                    com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView yt =
                            flipChild.findViewById(R.id.youtube_full_video);
                    ImageView play = flipChild.findViewById(R.id.imageViewPlayVideo);
                    LinearLayout side = flipChild.findViewById(R.id.video_side_menu);
                    adapter.shareYoutubePlayerFullVideoShare(
                            positionx, 1, cover, yt, ytLayout, play, side);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Center WhatsApp polish — no blink spam; cancel scale/alpha before show.
     */
    private void applyFooterWhatsapp() {
        if (imageVIewShareWhatsapp == null || isFinishing() || isDestroyed()) {
            return;
        }
        try {
            imageVIewShareWhatsapp.clearAnimation();
            imageVIewShareWhatsapp.animate().cancel();
            imageVIewShareWhatsapp.setScaleX(1f);
            imageVIewShareWhatsapp.setScaleY(1f);
            imageVIewShareWhatsapp.setAlpha(1f);
            boolean alreadyShowing = imageVIewShareWhatsapp.getVisibility() == View.VISIBLE
                    && imageVIewShareWhatsapp.getDrawable() != null;
            if (!alreadyShowing) {
                imageVIewShareWhatsapp.setImageResource(R.drawable.ic_whatsapp_home);
                imageVIewShareWhatsapp.clearColorFilter();
                imageVIewShareWhatsapp.setVisibility(View.VISIBLE);
            }
            FooterStyleHelper.applyFooterWhatsappIcon(this, imageVIewShareWhatsapp);
            imageVIewShareWhatsapp.setEnabled(true);
            imageVIewShareWhatsapp.setClickable(true);
            if (home_page_navigation != null) {
                imageVIewShareWhatsapp.bringToFront();
            }
            hideShareCenterSlotChrome();
            repositionCenterFabForVisibleTabs();
        } catch (Exception e) {
            showLog("applyFooterWhatsapp: " + e.getMessage());
        }
    }

    private void hideFooterWhatsapp() {
        if (imageVIewShareWhatsapp == null) {
            return;
        }
        try {
            imageVIewShareWhatsapp.clearAnimation();
            imageVIewShareWhatsapp.animate().cancel();
            imageVIewShareWhatsapp.setAlpha(1f);
            imageVIewShareWhatsapp.setScaleX(1f);
            imageVIewShareWhatsapp.setScaleY(1f);
            imageVIewShareWhatsapp.setVisibility(View.GONE);
        } catch (Exception ignored) {
        }
    }

    private boolean isCalendarMode() {
        return calendarModeActive;
    }

    private void hideHomeCalendarMode() {
        calendarModeActive = false;
        calendarFromDailyUpdates = false;
        try {
            if (calendarController != null) {
                calendarController.setOpenedFromDailyUpdates(false);
            }
        } catch (Exception ignored) {
        }
        try {
            if (homeCalendarHost != null) {
                homeCalendarHost.setVisibility(View.GONE);
            }
        } catch (Exception ignored) {
        }
    }

    private void keepCalendarCovered() {
        if (!isCalendarMode() || homeCalendarHost == null
                || isFinishing() || isDestroyed()) {
            return;
        }
        try {
            if (layout_with_data != null) {
                layout_with_data.setVisibility(View.GONE);
            }
            if (mFlipView != null) {
                mFlipView.setVisibility(View.GONE);
            }
            homeCalendarHost.setVisibility(View.VISIBLE);
            homeCalendarHost.bringToFront();
        } catch (Exception ignored) {
        }
    }

    private void ensureCalendarController() {
        if (homeCalendarHost == null || isFinishing() || isDestroyed()) {
            return;
        }
        if (calendarController != null) {
            return;
        }
        try {
            calendarController = TeluguCalendarController.attachEmbedded(this, homeCalendarHost);
        } catch (Exception e) {
            e.printStackTrace();
            calendarController = null;
        }
    }

    private void performCalendarOpen(boolean openRaasi, boolean fromDailyUpdates) {
        showLog("performCalendarOpen raasi=" + openRaasi + " fromDu=" + fromDailyUpdates);
        if (!isDeviceOnline() || isFinishing() || isDestroyed()) {
            return;
        }
        calendarFromDailyUpdates = fromDailyUpdates;
        calendarModeActive = true;
        try {
            hideHomeDailyUpdatesMode();
        } catch (Exception ignored) {
        }
        try {
            if (mFlipView != null) {
                mFlipView.setVisibility(View.GONE);
            }
            if (layout_with_data != null) {
                layout_with_data.setVisibility(View.GONE);
            }
            if (comment_page != null) {
                comment_page.setVisibility(View.GONE);
            }
            if (txtNoData != null) {
                txtNoData.setVisibility(View.GONE);
            }
        } catch (Exception ignored) {
        }
        try {
            if (homeCalendarHost != null) {
                homeCalendarHost.setVisibility(View.VISIBLE);
                homeCalendarHost.bringToFront();
                homeCalendarHost.post(() -> {
                    if (isFinishing() || isDestroyed() || !isCalendarMode()) {
                        return;
                    }
                    ensureCalendarController();
                    if (calendarController != null) {
                        try {
                            calendarController.setOpenedFromDailyUpdates(fromDailyUpdates);
                            if (openRaasi) {
                                calendarController.selectRaasiPage(false);
                            } else {
                                calendarController.selectPanchangamPage(false);
                            }
                            calendarController.onHostResume();
                        } catch (Exception ignored) {
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        keepCalendarCovered();
    }

    /** Thin Flash News: push_posts page-0 without fat video-tab settle. */
    private void performFlashNewsOpen() {
        if (isFinishing() || isDestroyed() || !isDeviceOnline()) {
            return;
        }
        try {
            hideHomeDailyUpdatesMode();
            hideHomeCalendarMode();
        } catch (Exception ignored) {
        }
        hideLocalFeedChromeThin();
        localFeedFromDailyUpdates = false;
        flashNewsModeActive = true;
        SharedPrefsData.putBool(
                HomePage.this, Constants.isUnreadPostsSelected, false, Constants.PREF_NAME);
        try {
            selectFooterTabWithoutMotion(R.id.navigation_home);
        } catch (Exception ignored) {
        }
        try {
            if (mFlipView != null) {
                mFlipView.setVisibility(View.VISIBLE);
            }
            showHomeFeedLoadingReveal();
            if (layout_with_data != null) {
                layout_with_data.setVisibility(View.GONE);
            }
        } catch (Exception ignored) {
        }
        resetFeedCursorsForSoftOpen();
        try {
            OpenPathCache.forceNextFeedNetwork(this);
        } catch (Exception ignored) {
        }
        inlist(0);
    }

    private void performLocalFeedOpenFromDailyUpdates() {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        hideHomeDailyUpdatesMode();
        hideHomeCalendarMode();
        flashNewsModeActive = false;
        localFeedFromDailyUpdates = true;
        SharedPrefsData.putBool(HomePage.this,
                Constants.isUnreadPostsSelected, false, Constants.PREF_NAME);
        selectFooterTabWithoutMotion(R.id.navigation_home);
        try {
            if (mFlipView != null) {
                mFlipView.setVisibility(View.VISIBLE);
            }
            showHomeFeedLoadingReveal();
            if (layout_with_data != null) {
                layout_with_data.setVisibility(View.GONE);
            }
            if (comment_page != null) {
                comment_page.setVisibility(View.GONE);
            }
        } catch (Exception ignored) {
        }
        resetFeedCursorsForSoftOpen();
        category_id = -1;
        isFromLocalArticles = true;
        try {
            OpenPathCache.forceNextFeedNetwork(this);
        } catch (Exception ignored) {
        }
        inlist(-1);
        try {
            showLocalFeedChromeThin();
        } catch (Exception ignored) {
        }
    }

    private void setupLocalFeedChromeThin() {
        View include = findViewById(R.id.include_local_feed_chrome);
        if (include != null) {
            localFeedChromeRoot = include.findViewById(R.id.local_feed_chrome_root);
            if (localFeedChromeRoot == null) {
                localFeedChromeRoot = include;
            }
        } else {
            localFeedChromeRoot = findViewById(R.id.local_feed_chrome_root);
        }
        if (localFeedChromeRoot == null) {
            return;
        }
        localFeedChromeRoot.setVisibility(View.GONE);
        localFeedContextLabel = localFeedChromeRoot.findViewById(R.id.local_feed_context_label);
        localFeedBackBtn = localFeedChromeRoot.findViewById(R.id.local_feed_back);
        if (localFeedBackBtn != null) {
            localFeedBackBtn.setOnClickListener(v -> {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                beginInstantNewsFooterReturn(true);
            });
        }
    }

    private void showLocalFeedChromeThin() {
        if (localFeedChromeRoot == null || isFinishing() || isDestroyed()) {
            return;
        }
        try {
            if (localFeedContextLabel != null) {
                String district = SharedPrefsData.getString(
                        this, Constants.Selected_Distric_Name1, Constants.PREF_NAME);
                if (district == null || district.trim().isEmpty()
                        || "null".equalsIgnoreCase(district)) {
                    district = "Local";
                }
                localFeedContextLabel.setText(district.trim());
            }
            localFeedChromeRoot.setVisibility(View.VISIBLE);
            localFeedChromeRoot.bringToFront();
        } catch (Exception ignored) {
        }
    }

    private void hideLocalFeedChromeThin() {
        if (localFeedChromeRoot == null) {
            return;
        }
        try {
            localFeedChromeRoot.setVisibility(View.GONE);
        } catch (Exception ignored) {
        }
    }

    private void ensurePlayInAppUpdateHelper() {
        if (playInAppUpdateHelper != null || isFinishing() || isDestroyed()) {
            return;
        }
        try {
            playInAppUpdateHelper = new PlayInAppUpdateHelper(this);
            playInAppUpdateHelper.setUpdateLauncher(playAppUpdateLauncher);
            playInAppUpdateHelper.setListener(new PlayInAppUpdateHelper.Listener() {
                @Override
                public void onFlexibleUpdateDownloaded() {
                    runOnUiThread(() -> {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }
                        try {
                            Toast.makeText(HomePage.this,
                                    R.string.update_message, Toast.LENGTH_SHORT).show();
                        } catch (Exception ignored) {
                        }
                    });
                }

                @Override
                public void onFlexibleUpdateProgress(int percent) {
                }

                @Override
                public void onFlexibleUpdateUnavailable(@Nullable Exception error) {
                    runOnUiThread(() -> {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }
                        PlayInAppUpdateHelper.openPlayStoreListing(HomePage.this);
                    });
                }
            });
        } catch (Exception e) {
            playInAppUpdateHelper = null;
        }
    }

}
