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
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
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
import android.os.MessageQueue;
import android.os.StrictMode;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
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
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.percentlayout.widget.PercentRelativeLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anyNews.anynews.Adapters.MailAdapter;
import com.anyNews.anynews.BuildConfig;
import com.anyNews.anynews.FCMTrackingEvents;
import com.anyNews.anynews.Pojo.BaseResponse;
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
import com.anyNews.anynews.models.FlipItems;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Ui.NeomorphFrameLayout;
import com.anyNews.anynews.Utils.Allsharedpreference;
import com.anyNews.anynews.Utils.AdMobInitializer;
import com.anyNews.anynews.Utils.VideoShareHelper;
import com.anyNews.anynews.Utils.CommonMethods;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.CricketFooterLabel;
import com.anyNews.anynews.Utils.EngagementCountHelper;
import com.anyNews.anynews.Utils.FooterStyleHelper;
import com.anyNews.anynews.Utils.ScreenCaptureHelper;
import com.anyNews.anynews.Utils.PollOptionStyleHelper;
import com.anyNews.anynews.Utils.ThemeColorHelper;
import com.anyNews.anynews.Utils.PlayInAppUpdateHelper;
import com.anyNews.anynews.Utils.FirebaseAnalyticsJSON;
import com.anyNews.anynews.Utils.FlipperAdapter;
import com.anyNews.anynews.Utils.Logone;
import com.anyNews.anynews.Utils.ModelClass;
import com.anyNews.anynews.Utils.MovementMethodTV;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.anyNews.anynews.Utils.PostDwellTracker;
import com.anyNews.anynews.Utils.ViewsBatchTracker;
import com.anyNews.anynews.Utils.SessionAnalyticsTracker;
import com.anyNews.anynews.Utils.DeviceLocationHelper;
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
import com.google.android.gms.ads.MediaContent;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.common.AccountPicker;

import org.json.JSONObject;
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
import com.google.gson.JsonArray;
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
import java.util.Locale;
import java.util.Map;
import java.util.Set;
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
    LinearLayout floating_action_button, search_icon;
    NeomorphFrameLayout neo_profile, neo_post_artical, neo_home, neo_inner_home, neo_inner_profile;
    long mBackPressed;
    SharePostToWhatsApp sharePostToWhatsApp;

    private float x1, x2;
    static final int MIN_DISTANCE = 0;

    // FloatingActionButton fab1,fab2,fab4;
    ImageView fab1, fab2, fab4;
    Animation fab_open, fab_close, fab_forword, fab_reverse;
    /** Continuous alpha blink for magazine footer Share (same pattern as Special bulb). */
    private Animator magazineShareBlinkAnimator;
    private View layoutMagazineShareFooter;
    private ImageView ivMagazineShareFooter;
    private TextView tvMagazineShareFooter;
    // Admin-controlled bottom-footer cricket tab (APK 3.40.79+). Shown only when
    // the admin enables it AND there is an active match; hidden otherwise so the
    // footer is exactly as before. Opens the same Match Center as the mini player.
    private View layoutCricketFooterBar;
    private View layoutCricketFooter;
    private ImageView ivCricketFooter;
    private TextView tvCricketFooter;
    private ObjectAnimator cricketFooterBlinkAnimator;
    private com.anyNews.anynews.Pojo.ActiveMatch cricketFooterActiveMatch;
    // Sticky bottom update banner (Play flexible in-app update). Soft = dismissible; force = Install only.
    private View layoutAppUpdateBanner;
    private TextView tvAppUpdateTitle;
    private TextView tvAppUpdateSubtitle;
    private TextView btnAppUpdateAction;
    private ImageView btnAppUpdateDismiss;
    private PlayInAppUpdateHelper playInAppUpdateHelper;
    private boolean appUpdateForceMode = false;
    private boolean appUpdateDownloaded = false;
    private boolean appUpdateDownloading = false;
    boolean isOpen = false;

    /* flipView Detailes*/
    private FlipView mFlipView;
   private  FlipView currentView;
    private FlipperAdapter adapter;
    // Tracks the FLAG_SECURE state currently applied to the window so we only touch
    // WindowManager (which forces a window relayout) when the value actually changes,
    // instead of on every flip. null = not yet applied.
    private Boolean appliedSecureFlag = null;
    /** Cached remote capture gate — avoid SharedPreferences reads on every flip settle. */
    private boolean cachedCaptureAllowed = false;
    // Non-visual flip analytics deferred until after settle paint (avoids mid-animation jank).
    private int pendingFlipTrackingPos = -1;
    // Heavy settle work (refreshDetails/loadAds/FLAG_SECURE/UI) runs after scroller finishes.
    private boolean pendingFlipSettleWork = false;
    /** Skip redundant settle when complete fires for the same page again. */
    private int lastSettledFlipIndex = -1;
    /** Debounce views+dwell when rapid flips land on the same post. */
    private long lastTrackedFlipPostId = -1L;
    private long lastTrackedFlipAtMs = 0L;
    private static final long VIEWS_DWELL_DEBOUNCE_MS = 400L;
    /** Leave dispatchDraw / first settle frames free before non-visual work (Narzo). */
    private static final long FLIP_VISUAL_SETTLE_MS = 16L;
    private static final long FLIP_VIDEO_TEARDOWN_MS = 48L;
    private static final long FLIP_TRACKING_DEFER_MS = 72L;
    private static final long FLIP_RESTORE_DEFER_MS = 160L;
    private final Handler flipSettleHandler = new Handler(Looper.getMainLooper());
    private Runnable flipVisualSettleRunnable;
    private Runnable flipHeavySettleRunnable;
    private Runnable flipVideoTeardownRunnable;
    private Runnable flipTrackingRunnable;
    private Runnable flipRestoreRunnable;
    private MessageQueue.IdleHandler flipTrackingIdleHandler;
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
    LinearLayout reply_context_layout;
    TextView reply_context_text;
    ImageView reply_context_cancel;
    Long replyParentCommentId = null;
    Long pendingExpandParentId = null;
    public  boolean adImpress=false;
    public  String adError="";

    static RelativeLayout no_internet, yes_internet,googleAds;

    private static final String LOG_TAG = "CheckNetworkStatus";
    private NetworkChangeReceiver receiver;
    private boolean isConnected = false;
    private boolean isApiRequested = false;
    int positionx=0,requestPostition=1;
    int requestSize=0;
    boolean isFromLocalArticles=false;

    /** Bundle keys for flip position across DayNight / battery-saver recreates. */
    private static final String STATE_FLIP_INDEX = "home_flip_index";
    private static final String STATE_FLIP_POST_ID = "home_flip_post_id";
    /** True after recreate until we apply (or abandon) restored flip position. */
    private boolean pendingFlipRestore = false;
    private int pendingRestoreFlipIndex = 0;
    private long pendingRestorePostId = -1L;
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
    private static final int SPECIAL_TAB_INDEX = 1;
    /** Cleared only on process start; stops Special-tab blink until next app launch. */
    private static boolean specialTabViewedThisSession = false;
    private ImageView specialTabBulbOverlay;
    private Animator specialTabBulbAnimator;
    private int specialTabBulbSetupRetryCount;
    FloatingActionButton floatingActionButtonHome;

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
    // True when the logged-in user is an approved reporter (from articles/profile API).
    private boolean isReporterUser=false;
    public boolean initialCall=false;
    public String no_article_message="Please wait while loading ...";

    // Cricket Live Mini Player — draggable floating overlay over the news feed.
    private com.anyNews.anynews.Utils.CricketMiniPlayerManager cricketMiniPlayerManager;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        readPendingFlipRestoreState(savedInstanceState);
        // Secure by default (blocks screenshots/recording); cleared only when the
        // remote allow_screen_capture flag is ON. Falls back to secure if unset.
        cachedCaptureAllowed = ScreenCaptureHelper.isCaptureAllowed(this);
        ScreenCaptureHelper.applySecureFlag(this);
        appliedSecureFlag = !cachedCaptureAllowed;
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        AdMobInitializer.initializeFromManifestIfNeeded(this);
//        RequestConfiguration.Builder requestConfiguration=new RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("CEB66515B6C9B2ECD92786955DF252CD"));
//        MobileAds.setRequestConfiguration(requestConfiguration.build());

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            getWindow().setAttributes(lp);
        }

        setContentView(R.layout.activity_home_page);
        allsharedpreference = new Allsharedpreference(HomePage.this);
        Activity=this;
        activity=this;
        context=this;
        cricketMiniPlayerManager = new com.anyNews.anynews.Utils.CricketMiniPlayerManager(HomePage.this);
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
        myRef.removeValue();
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

        // Bottom-footer cricket tab (starts GONE; driven by the cricket poll below).
        layoutCricketFooterBar = findViewById(R.id.layoutCricketFooterBar);
        layoutCricketFooter = findViewById(R.id.layoutCricketFooter);
        ivCricketFooter = findViewById(R.id.ivCricketFooter);
        tvCricketFooter = findViewById(R.id.tvCricketFooter);
        bindAppUpdateBanner();
        if (cricketMiniPlayerManager != null) {
            cricketMiniPlayerManager.setPollListener(
                    (match, footerEnabled) -> updateCricketFooterTab(match, footerEnabled));
        }



        commonMethods = new CommonMethods(activity);
        //player = new SimpleExoPlayer.Builder(getApplicationContext()).build();
        AudioManager audio = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

        incomment();

        //inpostarticales();

        ininointernet();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);


        bottomNavigationView = findViewById(R.id.nav_view);

        // Admin-configurable footer icon/label styling (defaults = current look).
        // Posted so the internal nav item views exist before we style them.
        bottomNavigationView.post(new Runnable() {
            @Override
            public void run() {
                FooterStyleHelper.apply(HomePage.this, bottomNavigationView);
                FooterStyleHelper.applyFab(HomePage.this, floatingActionButtonHome);
            }
        });


        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        // Feed chrome stays dark navy in both themes; force light system-bar icons.
        int homeBar = Color.parseColor("#0D1528");
        window.setStatusBarColor(homeBar);
        window.setNavigationBarColor(homeBar);
        androidx.core.view.WindowInsetsControllerCompat insetsController =
                androidx.core.view.WindowCompat.getInsetsController(window, window.getDecorView());
        if (insetsController != null) {
            insetsController.setAppearanceLightStatusBars(false);
            insetsController.setAppearanceLightNavigationBars(false);
        }

        //
        /*search_icon = findViewById(R.id.search_icon);*/
        floating_action_button = findViewById(R.id.floating_action_button);
        //new_bottom_nav_view = findViewById(R.id.new_bottom_nav_view);

        // layout with data detailes
        layout_with_data = findViewById(R.id.layout_with_data);
        layout_with_out_data = findViewById(R.id.layout_with_out_data);

        layout_with_out_data.setVisibility(View.VISIBLE);
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
        // Fallback: some splash routing branches launch HomePage without the
        // SHARE_REDIRECT_ID extra. StartingActivity persists the shared post id
        // as a one-shot; consume it here (and clear it) so the exact post opens.
        if (SHARE_REDIRECT_ID == null || SHARE_REDIRECT_ID.equals("null") || SHARE_REDIRECT_ID.trim().isEmpty()) {
            String pendingShare = SharedPrefsData.getString(getApplicationContext(), Constants.PENDING_SHARE_POST, Constants.PREF_NAME);
            if (pendingShare != null && !pendingShare.trim().isEmpty() && !pendingShare.equals("0")) {
                SHARE_REDIRECT_ID = pendingShare.trim();
            }
        }
        SharedPrefsData.putString(getApplicationContext(), Constants.PENDING_SHARE_POST, "", Constants.PREF_NAME);
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
            int parsedSessionId;
            try {
                parsedSessionId = Integer.parseInt(sessionId.trim());
            } catch (NumberFormatException e) {
                parsedSessionId = 0;
            }

            layout_with_out_data.setVisibility(View.VISIBLE);
            layout_with_data.setVisibility(View.GONE);


            last_post_id = 0;
            skip = 0;
            actual_skip = 0;
            category_id = parsedSessionId;
            isFromLocalArticles=false;
            showLog("inlist from oncreate 2:" + parsedSessionId);
            inlist(parsedSessionId);

            try {

            } catch (AssertionError e) {
                e.printStackTrace();
            }


            un_selected_local.setVisibility(View.VISIBLE);
            un_selected_my_feed.setVisibility(View.GONE);
            selected_local.setVisibility(View.GONE);
            selected_my_feed.setVisibility(View.VISIBLE);


            layout_with_out_data.setVisibility(View.VISIBLE);
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
                // Do not use CLEAR_TOP / CLEAR_TASK / NEW_TASK — those replace the feed
                // underneath the reporter form and make gallery/camera return land on Home.
                startActivity(intent);
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

        LocalBroadcastManager.getInstance(getContext()).registerReceiver(REPORTER_STATUS,
                new IntentFilter(Constants.REPORTER_STATUS_CHANGED));

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        receiver = new NetworkChangeReceiver();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(receiver, filter);
        }


        un_selected_local.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                un_selected_local.setVisibility(View.GONE);
                selected_local.setVisibility(View.VISIBLE);
                un_selected_my_feed.setVisibility(View.VISIBLE);
                selected_my_feed.setVisibility(View.GONE);
                mFlipView.setVisibility(View.VISIBLE);

                layout_with_out_data.setVisibility(View.VISIBLE);
                layout_with_data.setVisibility(View.GONE);
                comment_page.setVisibility(View.GONE);


                last_post_id = 0;
                skip = 0;
                actual_skip = 0;
                category_id = -1;
                requestPostition = 1;
                requestSize = 0;
                isFromLocalArticles = true;
                positionx = 0;
                clearPendingFlipRestore();
                list = new ArrayList<>();
                dummyResponse = null;
                inlist(-1);

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

                layout_with_out_data.setVisibility(View.VISIBLE);
                layout_with_data.setVisibility(View.GONE);
                comment_page.setVisibility(View.GONE);

                last_post_id = 0;
                skip = 0;
                actual_skip = 0;
                category_id = 0;
                requestPostition = 1;
                requestSize = 0;
                isFromLocalArticles = true;
                positionx = 0;
                clearPendingFlipRestore();
                list = new ArrayList<>();
                dummyResponse = null;
                inlist(0);

                writeButtonClick("User", type_firebase_button, current_time_firebase, draft_id_firebase, device_id_firebase, button_name_myfeed);

            }
        });


        setStaticData();

        // Special tab uses a large bulb overlay (40dp x 50sdp) centered on the tab item.
        if (bottomNavigationView != null) {
            bottomNavigationView.post(this::setupSpecialTabBulb);
        }

        bottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
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

    private void loadAds(String ad_code, Integer ad_req_position, String ad_type) {
       showLog("Ad loading ad_code 1 :"+ad_code);
       showLog("Ad loading ad_req_position :"+ad_req_position);
        showLog("Ad loading ad_type:"+ad_type);
       // ad_code="/21833905170,23071623886/Anynews_APP_Banner_300x600";
        //ad_code="/21833905170,23071623886/Anynews_New_Native_DU";
       //new RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("CEB66515B6C9B2ECD92786955DF252CD"));
//        showLog("Ad loading ad_code 2 :"+ad_code);
       // ad_type="google";
        if(ad_type.equalsIgnoreCase("google")){
            if(nativeGoogleAdList !=null && nativeGoogleAdArray !=null && nativeGoogleAdList.size()>0){
                if (!nativeGoogleAdArray.contains(positionx) || !nativeGoogleAdArray.contains(ad_req_position)) {
                    loadGoogleAds(ad_code,ad_req_position,0);
                }
            }else{
                loadGoogleAds(ad_code, ad_req_position,0);
            }
        }else{
            if(nativeFBAdList !=null && nativeFBAdArray !=null && nativeFBAdList.size()>0){
                if (!nativeFBAdArray.contains(positionx) || !nativeFBAdArray.contains(list.get(positionx).getAd_req_position())) {
                    loadFBAds(ad_code,ad_req_position);
                }
            }else{
                loadFBAds(ad_code,ad_req_position);
            }
        }

    }
    private void loadGoogleAds(String adCode, Integer ad_req_position,int reqType) {
        //   adCode="/21833905170,23071623886/Anynews_New_Native";
         //    adCode="/23071623886/AnyNews_2026";
                adCode="ca-app-pub-8562336752221105/4901277505";

        if(reqType==0){
            loadAdLoader(adCode,ad_req_position);

        }else if(reqType==1){
            loadInterstitialAd(adCode,ad_req_position);
        }

        showLog("Ad2 loading");
        //String adCode="ca-app-pub-8562336752221105/4901277505"; //Live
        //String adCode="ca-app-pub-3940256099942544/2247696110"; //Test



    }

    private void loadAdLoader(String adCode, Integer ad_req_position) {
        Log.e(TAG, "loadAdLoader: "+adCode );
        VideoOptions videoOptions = new VideoOptions.Builder()
                .setStartMuted(true)
                .build();

        NativeAdOptions adOptions = new NativeAdOptions.Builder()
                .setVideoOptions(videoOptions)
                .build();

        AdLoader adLoader = new AdLoader.Builder(context, adCode)
                .withNativeAdOptions(adOptions)
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
                        try{

                            String errorDomain = error.getDomain();
                            int errorCode = error.getCode();
                            String errorMessage = error.getMessage();
                            com.google.android.gms.ads.AdError cause = error.getCause();
                            // All of this information is available via the error's toString() method.
                            Log.d("Ads", error.toString());
                            JSONObject object=new JSONObject();
                            object.put("errorDomain",errorDomain);
                            object.put("errorCode",errorCode);
                            object.put("errorMessage",errorMessage);
                            object.put("adCode",adCode);
                            object.put("adPos",ad_req_position);
                            adError=object.toString();

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
                            MediaContent mediaContent = nativeAd.getMediaContent();
                            if (mediaContent != null) {
                                VideoController videoController = mediaContent.getVideoController();
                                boolean canShowCustomControls = videoController.isCustomControlsEnabled();
                                showLog("canShowCustomControls::"+canShowCustomControls);
                            }
                            if (list != null && list.size() > 0) {
                                FlipGoogleNativeAds flipGoogleNativeAds = new FlipGoogleNativeAds();
                                flipGoogleNativeAds.setAd(nativeAd);
                                flipGoogleNativeAds.setAdCode(adCode);
                                flipGoogleNativeAds.setReqPos(ad_req_position);
                                if (!nativeGoogleAdArray.contains(list.get(positionx).getAd_req_position())) {
                                    nativeGoogleAdArray.add(ad_req_position);
                                }
                                nativeGoogleAdList.add(flipGoogleNativeAds);
                                try {
                                    if(nativeAd.getResponseInfo()!=null && nativeAd.getResponseInfo().getLoadedAdapterResponseInfo()!=null &&
                                            nativeAd.getResponseInfo().getLoadedAdapterResponseInfo().getAdapterClassName()!=null){
                                        showLog("Data:"+nativeAd.getResponseInfo().getLoadedAdapterResponseInfo().getAdapterClassName());
                                    }
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }

                })
                .build();
        adLoader.loadAd(new AdRequest.Builder().build());
        //  adLoader.loadAd(new AdManagerAdRequest.Builder().build());

    }

    private void loadInterstitialAd(String adCode, Integer ad_req_position) {
        if (adCode == null || adCode.isEmpty()) {
            return;
        }
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(this, adCode, adRequest, new InterstitialAdLoadCallback() {
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
            if (list == null || positionx < 0 || positionx >= list.size()) {
                return;
            }
            if(page==0){
                adapter.shareFullMagazine(positionx);
            }else{
                showLog("getMediaType::"+list.get(positionx).getMediaType());
                String mediaType = list.get(positionx).getMediaType();
                if("4".equals(mediaType) || "6".equals(mediaType) ){
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
                layout_with_out_data.setVisibility(View.VISIBLE);
                layout_with_data.setVisibility(View.GONE);
                comment_page.setVisibility(View.GONE);
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
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
                        clearPendingFlipRestore();
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




                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        layout_with_out_data.setVisibility(View.VISIBLE);
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
                        clearPendingFlipRestore();
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
            loadingOnOverFlip = false;
            boolean paginationInFlight = isApiRequested && dummyResponse == null;
            if (!isDataParsed) {
                if (dummyResponse != null) {
                    convertListOfArticles(dummyResponse, -1);
                }
            } else if (paginationInFlight) {
                // Next page API is in flight; avoid misleading toast.
            } else if ("No articles to read further".equals(no_article_message)) {
                Toasty.info(getApplicationContext(), no_article_message, Toast.LENGTH_SHORT, true).show();
            }
        }
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                loadingOnOverFlip =true;
            }
        },1500);



    }

    @Override
    public void flipfinsh() {
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        //navigation_stricker.setCheckable(true);
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

                bottomNavigationView.getMenu().getItem(0).setTitle("" + languageModelResponse.getData().getReload());
                bottomNavigationView.getMenu().getItem(1).setTitle("" + languageModelResponse.getData().getEvents());
                bottomNavigationView.getMenu().getItem(3).setTitle("" + languageModelResponse.getData().getCategory());
                bottomNavigationView.getMenu().getItem(4).setTitle("" + languageModelResponse.getData().getProfile());
                fab_fullpost.setText("" + languageModelResponse.getData().getFullPost());
                fab_video.setText("" + languageModelResponse.getData().getVideoPost());
                fab_article.setText("" + languageModelResponse.getData().getArticle());
                nodatascreen.setText("" + languageModelResponse.getData().getNoData());
                leave_comment_white.setHint("" + languageModelResponse.getData().getWriteComments());
                applyMagazineShareFooterLabel();
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

            if (list == null || positionx < 0 || positionx >= list.size()) {
                return;
            }
            String writerNameAtPos = list.get(positionx).getWriterName();

            if (list.size()>0 && list.get(positionx).getIsFollow() == 0) {


                for (int i = 0; i < list.size(); i++) {

                    showLog(list.get(i).getTitle() + ": Title " + list.get(i).getIsFollow());


                    if (writerNameAtPos != null && writerNameAtPos.equals(list.get(i).getWriterName())) {
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


                    if (writerNameAtPos != null && writerNameAtPos.equals(list.get(j).getWriterName())) {
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
                if (mFlipView == null || ppi < 0 || ppi >= mFlipView.getChildCount()) {
                    return;
                }
                View flipChild = mFlipView.getChildAt(ppi);
                if (flipChild == null) {
                    return;
                }
                test_folloew = flipChild.findViewById(R.id.test_folloew);
                showLog("WRITER_FOLLOW positionx:"+positionx);

                if (list == null || positionx < 0 || positionx >= list.size()) {
                    return;
                }
                String writerNameAtPos = list.get(positionx).getWriterName();

                if (list.size()>0 && list.get(positionx).getIsFollow() == 0) {

                    for (int i = 0; i < list.size(); i++) {


                        if (writerNameAtPos != null && writerNameAtPos.equals(list.get(i).getWriterName())) {
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


                        if (writerNameAtPos != null && writerNameAtPos.equals(list.get(j).getWriterName())) {
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
    public BroadcastReceiver REPORTER_STATUS = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                return;
            }
            // Reporter approval no longer drives the center FAB; show_reporter_login does.
            updateCenterFab();
        }
    };

    public BroadcastReceiver SURVEY_STATUS = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (list == null || positionx < 0 || positionx >= list.size()) {
                return;
            }
            Integer surveyStatusAtPos = list.get(positionx).getSurveyStatus();
            if (surveyStatusAtPos == null) {
                return;
            }

            if (surveyStatusAtPos == 0) {


                for (int i = 0; i < list.size(); i++) {

                    if (surveyStatusAtPos.equals(list.get(i).getSurveyStatus())) {
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

                    if (surveyStatusAtPos.equals(list.get(j).getSurveyStatus())) {

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

    /** Play Core flexible update confirmation sheet result. */
    private final ActivityResultLauncher<androidx.activity.result.IntentSenderRequest> playAppUpdateLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartIntentSenderForResult(),
                    result -> {
                        if (result.getResultCode() != Activity.RESULT_OK) {
                            showLog("Play flexible update flow canceled or failed: " + result.getResultCode());
                            appUpdateDownloading = false;
                            refreshAppUpdateActionLabel();
                        }
                    });

    /**
     * Soft LOCATION prompt once per install so guest/reader drafts get lat/lng.
     * Create-post flows already request permission; splash/session previously did not.
     */
    private final ActivityResultLauncher<String[]> draftGpsPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(),
                    result -> {
                        DeviceLocationHelper.markSoftAsked(HomePage.this);
                        boolean granted = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_FINE_LOCATION))
                                || Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_COARSE_LOCATION));
                        if (granted) {
                            try {
                                SessionAnalyticsTracker.get().forceRefreshDeviceLocation(HomePage.this);
                            } catch (Exception ignored) {
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

    public BroadcastReceiver DIS_LIKE_PAGE = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (list == null || positionx < 0 || positionx >= list.size()) {
                return;
            }
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
                    datum.setLikes(list.get(positionx).getLikes());
                    datum.setComments(list.get(positionx).getComments());
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
                    layout_with_out_data.setVisibility(View.VISIBLE);
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
                    clearPendingFlipRestore();
                    list=new ArrayList<>();
                    dummyResponse=null;
                    inlist(-1);


                } else {
                  /*  un_selected_local.setVisibility(View.VISIBLE);

                    un_selected_my_feed.setVisibility(View.GONE);
                    selected_my_feed.setVisibility(View.VISIBLE);
                    selected_local.setVisibility(View.GONE);*/

                    layout_with_out_data.setVisibility(View.VISIBLE);
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
                    clearPendingFlipRestore();
                    requestPostition=1;
                    dummyResponse=null;
                    inlist(0);
                }

                writeButtonClick("User", type_firebase_button, current_time_firebase, draft_id_firebase, device_id_firebase, button_name_local);

                LocalBroadcastManager.getInstance(HomePage.this).unregisterReceiver(CURRENT_LOCATION);
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
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
        reply_context_layout = findViewById(R.id.reply_context_layout);
        reply_context_text = findViewById(R.id.reply_context_text);
        reply_context_cancel = findViewById(R.id.reply_context_cancel);
        reply_context_cancel.setOnClickListener(view -> clearReplyContext());
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
                clearReplyContext();
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
                ds.setColor(getResources().getColor(R.color.colorPrimary));
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

    private ListOfCommentsResponse.Datum mapCommentDatum(ListOfCommentsResponse.Datum source) {
        ListOfCommentsResponse.Datum datum = new ListOfCommentsResponse.Datum();
        datum.setId(source.getId());
        datum.setPostId(source.getPostId());
        datum.setDraftId(source.getDraftId());
        datum.setUserName(source.getUserName());
        datum.setComment(source.getComment());
        datum.setCreatedAt(source.getCreatedAt());
        datum.setCommentTime(source.getCommentTime());
        datum.setUser_profile_pic(source.getUser_profile_pic());
        datum.setEmail(source.getEmail());
        datum.setParentCommentId(source.getParentCommentId());
        datum.setReplyCount(source.getReplyCount());
        return datum;
    }

    private void bindCommentsAdapter() {
        comment_linearlayoutmanager = new LinearLayoutManager(getContext());
        comment_recycler_view.setLayoutManager(comment_linearlayoutmanager);
        comment_totoal_count.setVisibility(View.VISIBLE);
        comment_recycler_view.setVisibility(View.VISIBLE);
        linear_no_comment_page.setVisibility(View.GONE);
        MailAdapter mMailAdapter = new MailAdapter(
                HomePage.this,
                comment_chooseLanguage_models,
                position -> openReportPage(position),
                position -> startReplyToComment(position));
        if (pendingExpandParentId != null) {
            mMailAdapter.expandParent(pendingExpandParentId);
            pendingExpandParentId = null;
        }
        comment_recycler_view.setAdapter(mMailAdapter);
        comment_totoal_count.setText(" Comments ( " + comment_chooseLanguage_models.size() + " )");
    }

    private void startReplyToComment(int position) {
        if (comment_chooseLanguage_models == null
                || position < 0
                || position >= comment_chooseLanguage_models.size()) {
            return;
        }
        ListOfCommentsResponse.Datum datum = comment_chooseLanguage_models.get(position);
        if (datum.getId() == null) {
            return;
        }
        replyParentCommentId = datum.getId();
        String userName = datum.getUserName() != null ? datum.getUserName() : "comment";
        if (allsharedpreference.getEmailId() != null
                && datum.getEmail() != null
                && allsharedpreference.getEmailId().equalsIgnoreCase(datum.getEmail())) {
            userName = "yourself";
        }
        reply_context_layout.setVisibility(View.VISIBLE);
        reply_context_text.setText("Replying to " + userName);
        leave_comment_white.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(leave_comment_white, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void clearReplyContext() {
        replyParentCommentId = null;
        if (reply_context_layout != null) {
            reply_context_layout.setVisibility(View.GONE);
        }
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
                    if (encreptionRequest == null || encreptionRequest.getData() == null) {
                        hideProgress();
                        return;
                    }
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
                                comment_chooseLanguage_models.add(
                                        mapCommentDatum(validateUserOtp.getData().get(i)));
                            }


                            //   comment_progress.setVisibility(View.GONE);

                            bindCommentsAdapter();
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

    private View getFlipChildForShare() {
        if (mFlipView == null || ppi < 0 || ppi >= mFlipView.getChildCount()) {
            return null;
        }
        return mFlipView.getChildAt(ppi);
    }

    private static final String MAGAZINE_GLOW_TAG = "MagazineGlow";

    /**
     * Prefer FlipView's current page index (source of truth) over stale positionx.
     */
    private int getVisiblePostIndex() {
        try {
            if (mFlipView != null && list != null) {
                int cur = mFlipView.getCurrentPage();
                if (cur >= 0 && cur < list.size()) {
                    return cur;
                }
            }
        } catch (Exception ignored) {
        }
        return positionx;
    }

    /**
     * Center FAB follows show_reporter_login from the articles API (per language),
     * matching production: + when reporter login is enabled, WhatsApp otherwise.
     * Magazine posts replace the center "+" with a footer-style Share (icon + label).
     */
    private void updateCenterFab() {
        boolean isMagazine = isCurrentPostMagazine();
        int idx = getVisiblePostIndex();
        String type = "";
        String title = "";
        try {
            if (list != null && idx >= 0 && idx < list.size() && list.get(idx) != null) {
                type = list.get(idx).getPostType() != null ? list.get(idx).getPostType() : "";
                title = list.get(idx).getTitle() != null ? String.valueOf(list.get(idx).getTitle()) : "";
            }
        } catch (Exception ignored) {
        }
        Log.i(MAGAZINE_GLOW_TAG, "updateCenterFab idx=" + idx
                + " positionx=" + positionx
                + " flipPage=" + (mFlipView != null ? mFlipView.getCurrentPage() : -1)
                + " postType=[" + type + "]"
                + " isMagazine=" + isMagazine
                + " showReporterLogin=" + showReporterLogin
                + " title=" + (title.length() > 40 ? title.substring(0, 40) : title));

        if (isMagazine) {
            applyMagazineCenterShare();
            Log.i(MAGAZINE_GLOW_TAG, "MODE=SHARE_FOOTER postType=[" + type + "] title="
                    + (title.length() > 40 ? title.substring(0, 40) : title));
            showLog("updateCenterFab magazine share footer");
            return;
        }
        // Not a magazine -> restore the normal center button (+ or WhatsApp).
        hideMagazineShareFooter();
        if (floatingActionButtonHome != null) {
            floatingActionButtonHome.clearAnimation();
            floatingActionButtonHome.setScaleX(1f);
            floatingActionButtonHome.setScaleY(1f);
            floatingActionButtonHome.setAlpha(1f);
            floatingActionButtonHome.setBackgroundTintList(
                    ContextCompat.getColorStateList(this, R.color.bottom_navigation_view_color));
            floatingActionButtonHome.setRippleColor(
                    ContextCompat.getColorStateList(this, R.color.bottom_navigation_view_color));
            floatingActionButtonHome.setImageResource(R.drawable.ic_repoter_post);
            floatingActionButtonHome.setContentDescription(getString(R.string.post_selection));
            floatingActionButtonHome.setEnabled(true);
            // Non-magazine: the "+" must NOT consume the tap. The center action (reporter
            // menu / WhatsApp) is driven by BottomNavigationView (navigation_stricker)
            // underneath, so keep the FAB non-clickable to let the tap flow through.
            floatingActionButtonHome.setOnClickListener(null);
            floatingActionButtonHome.setClickable(false);
            floatingActionButtonHome.setFocusable(false);
            if (showReporterLogin) {
                floatingActionButtonHome.setVisibility(View.VISIBLE);
                try {
                    floatingActionButtonHome.show();
                } catch (Exception ignored) {
                }
            } else {
                floatingActionButtonHome.setVisibility(View.INVISIBLE);
            }
        }
        if (imageVIewShareWhatsapp != null) {
            imageVIewShareWhatsapp.clearAnimation();
            imageVIewShareWhatsapp.setScaleX(1f);
            imageVIewShareWhatsapp.setScaleY(1f);
            imageVIewShareWhatsapp.setAlpha(1f);
            imageVIewShareWhatsapp.setOnClickListener(null);
            imageVIewShareWhatsapp.setClickable(false);
            int normalSize = Math.round(48 * getResources().getDisplayMetrics().density);
            ViewGroup.LayoutParams normalLp = imageVIewShareWhatsapp.getLayoutParams();
            if (normalLp instanceof CoordinatorLayout.LayoutParams) {
                CoordinatorLayout.LayoutParams clp = (CoordinatorLayout.LayoutParams) normalLp;
                clp.width = normalSize;
                clp.height = normalSize;
                clp.setAnchorId(R.id.nav_view);
                clp.anchorGravity = Gravity.CENTER;
                clp.gravity = Gravity.CENTER;
                imageVIewShareWhatsapp.setLayoutParams(clp);
            } else if (normalLp != null) {
                normalLp.width = normalSize;
                normalLp.height = normalSize;
                imageVIewShareWhatsapp.setLayoutParams(normalLp);
            }
            imageVIewShareWhatsapp.setImageResource(R.drawable.ic_whatsapp_home);
            imageVIewShareWhatsapp.setVisibility(showReporterLogin ? View.GONE : View.VISIBLE);
        }
        Log.i(MAGAZINE_GLOW_TAG, "MODE=PLUS_OR_WA showReporterLogin=" + showReporterLogin
                + " fabVisible=" + (showReporterLogin ? "VISIBLE" : "INVISIBLE"));
        showLog("updateCenterFab showReporterLogin:" + showReporterLogin);
    }

    /**
     * True when the currently visible post should use the glow Share FAB instead of "+".
     * Live "రామాయణం స్పెషల్" is post_type {@code image_gallery} in Magazines (category 15) —
     * not {@code magazine}. Only true magazines + Magazines-category galleries get glow.
     * Normal image_gallery posts keep "+" / WhatsApp; on-image corner share stays hidden.
     */
    private boolean isCurrentPostMagazine() {
        try {
            int idx = getVisiblePostIndex();
            if (list == null || idx < 0 || idx >= list.size()) {
                Log.i(MAGAZINE_GLOW_TAG, "isMagazine=false reason=bad_index idx=" + idx
                        + " listSize=" + (list == null ? -1 : list.size()));
                return false;
            }
            return usesMagazineGlowShare(list.get(idx), idx);
        } catch (Exception e) {
            Log.e(MAGAZINE_GLOW_TAG, "isMagazine exception: " + e.getMessage());
            return false;
        }
    }

    /**
     * Footer glow Share applies only to card magazines and Magazines-category specials
     * (e.g. Ramayana). Ordinary {@code image_gallery} news posts must NOT match — they keep
     * the reporter "+" / WhatsApp FAB. On-image {@code image_gallary_share} stays hidden.
     */
    public static boolean usesMagazineGlowShare(ModelClass.Datum item) {
        return usesMagazineGlowShare(item, -1);
    }

    private static boolean usesMagazineGlowShare(ModelClass.Datum item, int idx) {
        try {
            if (item == null) {
                Log.i(MAGAZINE_GLOW_TAG, "isMagazine=false reason=null_item idx=" + idx);
                return false;
            }
            String type = item.getPostType();
            if (type == null) {
                Log.i(MAGAZINE_GLOW_TAG, "isMagazine=false reason=null_postType idx=" + idx);
                return false;
            }
            type = type.trim();
            boolean glow = type.equalsIgnoreCase("magazine")
                    || type.equalsIgnoreCase("magazines");
            // Magazines-category image_gallery specials only (NOT all galleries).
            Integer catId = item.getCategoryId();
            if (!glow && type.equalsIgnoreCase("image_gallery")
                    && catId != null && catId.intValue() == 15) {
                glow = true;
                Log.i(MAGAZINE_GLOW_TAG, "isMagazine=true via Magazines image_gallery cat=15"
                        + " idx=" + idx);
            }
            // Magazines card posts: media_type "2" with strip JSON (non-gallery).
            if (!glow && !"image_gallery".equalsIgnoreCase(type)
                    && "2".equals(String.valueOf(item.getMediaType()))) {
                String strip = item.getStrip_content();
                if (strip != null && strip.trim().length() > 2
                        && (strip.trim().startsWith("[") || strip.contains("http"))) {
                    glow = true;
                    Log.i(MAGAZINE_GLOW_TAG, "isMagazine=true via media_type=2+strip"
                            + " postType=[" + type + "] idx=" + idx);
                }
            }
            // Magazines category_id 15 + media_type 2 — safety net if post_type mistyped.
            if (!glow && catId != null && catId.intValue() == 15
                    && "2".equals(String.valueOf(item.getMediaType()))
                    && !"image_gallery".equalsIgnoreCase(type)) {
                glow = true;
                Log.i(MAGAZINE_GLOW_TAG, "isMagazine=true via Magazines category_id=15"
                        + " postType=[" + type + "] idx=" + idx);
            }
            if (!glow) {
                Log.i(MAGAZINE_GLOW_TAG, "isMagazine=false postType=[" + type + "] mediaType="
                        + item.getMediaType() + " cat=" + item.getCategoryId() + " idx=" + idx);
            } else {
                Log.i(MAGAZINE_GLOW_TAG, "isMagazine=true postType=[" + type + "] mediaType="
                        + item.getMediaType() + " cat=" + item.getCategoryId() + " idx=" + idx);
            }
            return glow;
        } catch (Exception e) {
            Log.e(MAGAZINE_GLOW_TAG, "usesMagazineGlowShare exception: " + e.getMessage());
            return false;
        }
    }

    /**
     * Magazines / image_gallery: replace the center "+" FAB with a footer-style Share
     * control (24dp tinted icon + translated label) that blinks like the Special bulb.
     */
    private void applyMagazineCenterShare() {
        if (imageVIewShareWhatsapp != null) {
            imageVIewShareWhatsapp.clearAnimation();
            imageVIewShareWhatsapp.setOnClickListener(null);
            imageVIewShareWhatsapp.setClickable(false);
            imageVIewShareWhatsapp.setVisibility(View.GONE);
        }
        // Hide the + FAB plate so Share reads like a normal footer tab.
        if (floatingActionButtonHome != null) {
            floatingActionButtonHome.clearAnimation();
            floatingActionButtonHome.setOnClickListener(null);
            floatingActionButtonHome.setClickable(false);
            floatingActionButtonHome.setFocusable(false);
            floatingActionButtonHome.setVisibility(View.INVISIBLE);
            try {
                floatingActionButtonHome.hide();
            } catch (Exception ignored) {
            }
        }
        if (layoutMagazineShareFooter == null) {
            Log.e(MAGAZINE_GLOW_TAG, "applyMagazineCenterShare FAILED: layoutMagazineShareFooter null");
            return;
        }
        applyMagazineShareFooterLabel();
        layoutMagazineShareFooter.setVisibility(View.VISIBLE);
        layoutMagazineShareFooter.setEnabled(true);
        layoutMagazineShareFooter.setClickable(true);
        layoutMagazineShareFooter.setOnClickListener(v -> {
            try {
                int visibleIdx = getVisiblePostIndex();
                if (visibleIdx >= 0) {
                    positionx = visibleIdx;
                }
                CallShareListener magListener = new CallShareListener();
                magListener.setShareInterfaceListener(adapter);
                magListener.triggerEvent();
                shareCurrentPostViaWhatsAppFooter();
            } catch (Exception magEx) {
                magEx.printStackTrace();
            }
        });
        layoutMagazineShareFooter.bringToFront();
        if (home_page_navigation != null) {
            home_page_navigation.bringChildToFront(layoutMagazineShareFooter);
        }
        startMagazineShareBlink();
        Log.i(MAGAZINE_GLOW_TAG, "applyMagazineCenterShare OK mode=FOOTER_SHARE"
                + " vis=" + layoutMagazineShareFooter.getVisibility());
    }

    /** Localized "Share" from language pack (same source as reload/profile footer labels). */
    private void applyMagazineShareFooterLabel() {
        if (tvMagazineShareFooter == null) {
            return;
        }
        String label = null;
        try {
            if (languageModelResponse != null
                    && languageModelResponse.getData() != null
                    && languageModelResponse.getData().getShare() != null) {
                label = languageModelResponse.getData().getShare().trim();
            }
        } catch (Exception ignored) {
        }
        if (label == null || label.isEmpty()) {
            label = getString(R.string.share);
        }
        tvMagazineShareFooter.setText(label);
        if (layoutMagazineShareFooter != null) {
            layoutMagazineShareFooter.setContentDescription(label);
        }
        if (ivMagazineShareFooter != null) {
            ivMagazineShareFooter.setContentDescription(label);
        }
    }

    private void hideMagazineShareFooter() {
        stopMagazineShareBlink();
        if (layoutMagazineShareFooter != null) {
            layoutMagazineShareFooter.setOnClickListener(null);
            layoutMagazineShareFooter.setClickable(false);
            layoutMagazineShareFooter.setVisibility(View.GONE);
        }
    }

    private void startMagazineShareBlink() {
        if (ivMagazineShareFooter == null) {
            return;
        }
        stopMagazineShareBlink();
        ivMagazineShareFooter.setAlpha(1f);
        ObjectAnimator animator = ObjectAnimator.ofFloat(ivMagazineShareFooter, View.ALPHA, 1f, 0.35f);
        animator.setDuration(700);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        magazineShareBlinkAnimator = animator;
        animator.start();
    }

    private void stopMagazineShareBlink() {
        if (magazineShareBlinkAnimator != null) {
            magazineShareBlinkAnimator.cancel();
            magazineShareBlinkAnimator = null;
        }
        if (ivMagazineShareFooter != null) {
            ivMagazineShareFooter.clearAnimation();
            ivMagazineShareFooter.setAlpha(1f);
        }
        if (layoutMagazineShareFooter != null) {
            layoutMagazineShareFooter.clearAnimation();
            layoutMagazineShareFooter.setAlpha(1f);
        }
    }

    /**
     * Shows / hides the bottom-footer cricket tab from the cricket poll. The tab
     * appears ONLY when the admin enabled it AND there is a live/active match, so
     * when the footer flag is off (default) the footer is EXACTLY as before — the
     * bar is GONE and reserves no space. Tapping opens the SAME Match Center the
     * mini player opens. The tab and mini player are independent surfaces.
     */
    private void updateCricketFooterTab(com.anyNews.anynews.Pojo.ActiveMatch match,
                                        boolean footerEnabled) {
        if (layoutCricketFooterBar == null) {
            return;
        }
        boolean hasMatch = match != null && !TextUtils.isEmpty(match.getMatch_id());
        if (!footerEnabled || !hasMatch) {
            cricketFooterActiveMatch = null;
            stopCricketFooterBlink();
            if (layoutCricketFooterBar.getVisibility() != View.GONE) {
                layoutCricketFooterBar.setVisibility(View.GONE);
            }
            if (layoutCricketFooter != null) {
                layoutCricketFooter.setOnClickListener(null);
                layoutCricketFooter.setClickable(false);
            }
            return;
        }

        cricketFooterActiveMatch = match;
        if (tvCricketFooter != null) {
            String label = CricketFooterLabel.forCurrentLanguage(HomePage.this);
            tvCricketFooter.setText(label);
            layoutCricketFooter.setContentDescription(label);
        }
        if (layoutCricketFooterBar.getVisibility() != View.VISIBLE) {
            layoutCricketFooterBar.setVisibility(View.VISIBLE);
        }
        if (layoutCricketFooter != null) {
            layoutCricketFooter.setClickable(true);
            layoutCricketFooter.setOnClickListener(v -> openCricketMatchCenter());
        }
        startCricketFooterBlink();
    }

    /** Opens the cricket Match Center for the current footer match (same as mini player). */
    private void openCricketMatchCenter() {
        com.anyNews.anynews.Pojo.ActiveMatch match = cricketFooterActiveMatch;
        if (match == null) {
            match = com.anyNews.anynews.Utils.CricketMiniPlayerManager.getLastKnownActive();
        }
        if (match == null || TextUtils.isEmpty(match.getMatch_id())) {
            return;
        }
        try {
            Intent intent = new Intent(HomePage.this, MatchCenterActivity.class);
            intent.putExtra(MatchCenterActivity.EXTRA_MATCH, match);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Subtle infinite alpha pulse on the cricket icon (same feel as the Share bulb). */
    private void startCricketFooterBlink() {
        if (ivCricketFooter == null) {
            return;
        }
        if (cricketFooterBlinkAnimator != null && cricketFooterBlinkAnimator.isRunning()) {
            return;
        }
        stopCricketFooterBlink();
        ivCricketFooter.setAlpha(1f);
        ObjectAnimator animator = ObjectAnimator.ofFloat(ivCricketFooter, View.ALPHA, 1f, 0.35f);
        animator.setDuration(750);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        cricketFooterBlinkAnimator = animator;
        animator.start();
    }

    private void stopCricketFooterBlink() {
        if (cricketFooterBlinkAnimator != null) {
            cricketFooterBlinkAnimator.cancel();
            cricketFooterBlinkAnimator = null;
        }
        if (ivCricketFooter != null) {
            ivCricketFooter.clearAnimation();
            ivCricketFooter.setAlpha(1f);
        }
    }

    /**
     * Called from FlipperAdapter when a magazine page view is bound so the footer
     * center button can swap to Share even if flip callbacks lag.
     */
    public void onMagazinePageBound(int boundPosition) {
        Log.i(MAGAZINE_GLOW_TAG, "onMagazinePageBound boundPosition=" + boundPosition
                + " positionx=" + positionx
                + " flipPage=" + (mFlipView != null ? mFlipView.getCurrentPage() : -1));
        try {
            // Only morph footer when this bound page is (or is becoming) the visible one.
            int visible = getVisiblePostIndex();
            if (boundPosition == visible || boundPosition == positionx
                    || (mFlipView != null && boundPosition == mFlipView.getCurrentPage())) {
                if (boundPosition >= 0) {
                    positionx = boundPosition;
                }
                updateCenterFab();
            } else {
                Log.i(MAGAZINE_GLOW_TAG, "onMagazinePageBound skipped (preloading) bound="
                        + boundPosition + " visible=" + visible);
            }
        } catch (Exception e) {
            Log.e(MAGAZINE_GLOW_TAG, "onMagazinePageBound error: " + e.getMessage());
        }
    }

    private void shareCurrentPostViaWhatsAppFooter() {
        if (adapter == null) {
            return;
        }
        if (list == null || positionx < 0 || positionx >= list.size()) {
            return;
        }
        ModelClass.Datum item = list.get(positionx);
        if (item == null || item.getPostType() == null) {
            return;
        }
        String postType = item.getPostType();
        if (postType.equalsIgnoreCase("ad")
                || postType.equalsIgnoreCase("web_ads")
                || postType.equalsIgnoreCase("web")
                || postType.equalsIgnoreCase("ratings")
                || postType.equalsIgnoreCase("share_app")) {
            return;
        }

        View flipChild = getFlipChildForShare();
        if (flipChild == null) {
            return;
        }

        if (postType.equalsIgnoreCase("image_gallery")) {
            adapter.shareGalleryScreenShotUI(flipChild);
        } else if (postType.equalsIgnoreCase("full_image")) {
            adapter.shareFullImageScree(flipChild, positionx);
        } else if (postType.equalsIgnoreCase("normal_image")) {
            adapter.takeNormalImageScreenshot(flipChild, positionx, 1);
        } else if (postType.equalsIgnoreCase("magazine")) {
            adapter.showMagazineShareOptions(flipChild, 1);
        } else if (postType.equalsIgnoreCase("normal_video")
                || postType.equalsIgnoreCase("full_video")) {
            adapter.showVideoShareChooserForPost(positionx, flipChild);
        } else if (postType.equalsIgnoreCase("survey")
                || postType.equalsIgnoreCase("survey_poll")) {
            adapter.shareSurveyScreenshot(flipChild, positionx);
        }
    }

    @Override
    public void closeBottomStrip(boolean closeStrip) {
        showLog("closeBottomStrip:"+closeStrip);
        if (list == null || positionx < 0 || positionx >= list.size()) {
            return;
        }
        String postType = list.get(positionx).getPostType();
        if (postType != null && (postType.equalsIgnoreCase("ad")
                || postType.equalsIgnoreCase("share_app")
                || postType.equalsIgnoreCase("ratings"))) {
            home_page_navigation.setVisibility(View.GONE);
        } else {
            home_page_navigation.setVisibility(View.VISIBLE);
        }

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
            clearReplyContext();
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
        // Hide cricket mini-player for the gesture only (idempotent). Restore on settle.
        if (cricketMiniPlayerManager != null) {
            cricketMiniPlayerManager.setHiddenForFlipGesture(true);
        }
        // FAB refresh is handled in OnFlipCompletedListener — avoid a second layout
        // pass 80ms after appear (stacking with settle work on low-end).
    }

    /**
     * Mid-flip pause: stop playback only. No seek/stop/release/YouTube re-init
     * (those thrash Camera/GL during 3D flip on low-end). Full cleanup in
     * {@link #teardownFlipVideoPlayers()} after settle.
     */
    private void pauseVideoLight() {
        try {
            if (FlipperAdapter.youTubePlayerViewHalf != null) {
                FlipperAdapter.youTubePlayerViewHalf.pause();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (FlipperAdapter.exoPlayer != null) {
                FlipperAdapter.exoPlayer.setPlayWhenReady(false);
                FlipperAdapter.exoPlayer.pause();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Heavier pause used outside active flip animation (e.g. onPause).
     * During flip prefer {@link #pauseVideoLight()} + settle teardown.
     */
    public  void pauseVideo(){
        pauseVideoLight();
        try{
            showLog("pauseVideo: stopping active players");
            if (FlipperAdapter.youTubePlayerViewHalf != null) {
                if (adapter != null) {
                    if (adapter.customPlayerUiController != null) {
                        adapter.customPlayerUiController.showUiControlls();
                    }
                    if (adapter.fullVideocustomPlayerUiController != null) {
                        adapter.fullVideocustomPlayerUiController.showUiControlls();
                    }
                }
            }
            FlipperAdapter.isFullYoutubeVideoIsInitialized = false;
        }catch (Exception e){
            e.printStackTrace();
        }
        try{
            if (FlipperAdapter.exoPlayer != null) {
                FlipperAdapter.exoPlayer.stop();
                // Seek only — defer release until flip settle (Narzo jank if release mid-flip).
                FlipperAdapter.exoPlayer.seekTo(0);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        try {
            View flipChild = getFlipChildForShare();
            if (adapter != null && flipChild != null) {
                ImageView imageViewItems = flipChild.findViewById(R.id.imageViewItem__);
                LinearLayout halfyoutubecontainer = flipChild.findViewById(R.id.halfyoutubecontainer);
                adapter.callYoutubeReInit(imageViewItems, halfyoutubecontainer);
                if (imageViewItems != null) {
                    imageViewItems.setVisibility(View.VISIBLE);
                }
                if (halfyoutubecontainer != null) {
                    halfyoutubecontainer.setVisibility(View.GONE);
                }
            }
            if (list != null && positionx >= 0 && positionx < list.size()) {
                showLog("pauseVideo post_type::: "+list.get(positionx).getPostType());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /** Heavier video cleanup after flip animation finishes. */
    private void teardownFlipVideoPlayers() {
        try {
            // post_type is already the DESTINATION page (set in onFlippedToPage).
            // Do not tear down when settling ON a video — getView just bound it.
            // Destroying here is a common cause of a blank/white 2nd (or Nth) flip page
            // when that item is normal_video / full_video / vast.
            boolean landingOnVideo = post_type != null
                    && (post_type.equalsIgnoreCase("full_video")
                    || post_type.equalsIgnoreCase("normal_video")
                    || post_type.equalsIgnoreCase("google_vasttag"));
            if (landingOnVideo) {
                return;
            }
            boolean hadPlayers = FlipperAdapter.hasActiveVideoPlayer();
            if (!hadPlayers) {
                return;
            }
            FlipperAdapter.releaseActiveExoPlayer();
            try {
                if (FlipperAdapter.youTubePlayerViewHalf != null) {
                    FlipperAdapter.youTubePlayerViewHalf.pause();
                    FlipperAdapter.youTubePlayerViewHalf = null;
                }
            } catch (Exception ignored) {
            }
            if (adapter != null && mFlipView != null) {
                adapter.resetAllFlipChildrenVideoPlayers(mFlipView);
                for (int i = 0; i < mFlipView.getChildCount(); i++) {
                    View flipChild = mFlipView.getChildAt(i);
                    if (flipChild == null) continue;
                    ImageView imageViewItems = flipChild.findViewById(R.id.imageViewItem__);
                    LinearLayout halfyoutubecontainer = flipChild.findViewById(R.id.halfyoutubecontainer);
                    if (imageViewItems == null && halfyoutubecontainer == null) {
                        continue;
                    }
                    adapter.callYoutubeReInit(imageViewItems, halfyoutubecontainer);
                    if (imageViewItems != null) {
                        imageViewItems.setVisibility(View.VISIBLE);
                    }
                    if (halfyoutubecontainer != null) {
                        halfyoutubecontainer.setVisibility(View.GONE);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void resetFullVideoUi(View flipChild) {
        if (flipChild == null || adapter == null) {
            return;
        }
        adapter.resetFullExoVideoView(flipChild);
        adapter.resetFullYoutubeVideoView(flipChild);
    }

    @Override
    public void OnFlipCompletedListener(FlipView v) {
        showLog("OnFlipCompletedListener:::::::::");
        // Restore cricket mini-player after flip gesture (feature kept; only paused visually).
        if (cricketMiniPlayerManager != null) {
            cricketMiniPlayerManager.setHiddenForFlipGesture(false);
        }
        // OnFlipCompleted is invoked from FlipView.dispatchDraw — never run heavy
        // settle work here or the first landed frame hitches (Narzo / low-RAM).
        schedulePostFlipSettleWork();
    }

    /** Cancel queued settle batches when a newer flip supersedes them. */
    private void cancelPendingFlipSettleCallbacks() {
        if (flipVisualSettleRunnable != null) {
            flipSettleHandler.removeCallbacks(flipVisualSettleRunnable);
            if (mFlipView != null) {
                mFlipView.removeCallbacks(flipVisualSettleRunnable);
            }
            flipVisualSettleRunnable = null;
        }
        if (flipHeavySettleRunnable != null) {
            flipSettleHandler.removeCallbacks(flipHeavySettleRunnable);
            flipHeavySettleRunnable = null;
        }
        if (flipVideoTeardownRunnable != null) {
            flipSettleHandler.removeCallbacks(flipVideoTeardownRunnable);
            flipVideoTeardownRunnable = null;
        }
        if (flipTrackingRunnable != null) {
            flipSettleHandler.removeCallbacks(flipTrackingRunnable);
            flipTrackingRunnable = null;
        }
        if (flipRestoreRunnable != null) {
            flipSettleHandler.removeCallbacks(flipRestoreRunnable);
            flipRestoreRunnable = null;
        }
        if (flipTrackingIdleHandler != null) {
            try {
                Looper.myQueue().removeIdleHandler(flipTrackingIdleHandler);
            } catch (Exception ignored) {
            }
            flipTrackingIdleHandler = null;
        }
    }

    /**
     * Staged settle: paint first → light UI → heavy UI → video teardown →
     * tracking/prefs. Keeps features; moves cost off the flip animation frames.
     */
    private void schedulePostFlipSettleWork() {
        final int settlePos = positionx;
        // Same-index re-entry (completeCheck / bounce): skip redundant batch.
        // Do not cancel in-flight restore/tracking from the prior settle of this page.
        if (settlePos == lastSettledFlipIndex && !pendingFlipSettleWork
                && pendingFlipTrackingPos < 0) {
            showLog("schedulePostFlipSettleWork skip same index=" + settlePos);
            return;
        }
        cancelPendingFlipSettleCallbacks();
        flipVisualSettleRunnable = () -> {
            flipVisualSettleRunnable = null;
            if (isFinishing() || isDestroyed()) {
                return;
            }
            if (settlePos != positionx) {
                return;
            }
            try {
                updateCenterFab();
            } catch (Exception fabEx) {
                fabEx.printStackTrace();
            }
            flipHeavySettleRunnable = () -> {
                flipHeavySettleRunnable = null;
                if (isFinishing() || isDestroyed() || settlePos != positionx) {
                    return;
                }
                runDeferredFlipSettleWork();
                if (adapter != null) {
                    adapter.setCurrentFlipPage(positionx);
                    adapter.bindPendingNativeAdMediaForPage(positionx);
                }
                lastSettledFlipIndex = positionx;
            };
            // One more frame so FAB layout does not stack with FLAG_SECURE / ads.
            flipSettleHandler.post(flipHeavySettleRunnable);

            flipVideoTeardownRunnable = () -> {
                flipVideoTeardownRunnable = null;
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                teardownFlipVideoPlayers();
            };
            flipSettleHandler.postDelayed(flipVideoTeardownRunnable, FLIP_VIDEO_TEARDOWN_MS);

            flipTrackingRunnable = () -> {
                flipTrackingRunnable = null;
                flipTrackingIdleHandler = null;
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                flushPendingFlipTracking();
            };
            // Prefer idle after settle frames; fallback delay if queue stays busy.
            flipTrackingIdleHandler = () -> {
                flipTrackingIdleHandler = null;
                if (flipTrackingRunnable != null) {
                    flipSettleHandler.removeCallbacks(flipTrackingRunnable);
                    flipTrackingRunnable.run();
                }
                return false;
            };
            try {
                Looper.myQueue().addIdleHandler(flipTrackingIdleHandler);
            } catch (Exception ignored) {
            }
            flipSettleHandler.postDelayed(flipTrackingRunnable, FLIP_TRACKING_DEFER_MS);

            flipRestoreRunnable = () -> {
                flipRestoreRunnable = null;
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                try {
                    long pid = resolvePostIdForFlipIndex(positionx);
                    persistFlipRestoreCheckpoint(positionx, pid, positionx > 0 || pid > 0);
                } catch (Exception ignored) {
                }
            };
            flipSettleHandler.postDelayed(flipRestoreRunnable, FLIP_RESTORE_DEFER_MS);
        };
        // Exit dispatchDraw before any settle work (critical for low-end smoothness).
        if (mFlipView != null) {
            mFlipView.post(flipVisualSettleRunnable);
        } else {
            flipSettleHandler.postDelayed(flipVisualSettleRunnable, FLIP_VISUAL_SETTLE_MS);
        }
    }

    /**
     * Analytics + view/dwell APIs — run after flip settles. AES encrypt + network
     * on the main thread mid-flip was a major Narzo jank source vs Play 3.40.45.
     */
    private void flushPendingFlipTracking() {
        int pos = pendingFlipTrackingPos;
        if (pos < 0 || list == null || pos >= list.size()) {
            return;
        }
        pendingFlipTrackingPos = -1;
        try {
            ModelClass.Datum datum = list.get(pos);
            String type = datum != null ? datum.getPostType() : null;
            if (type == null) {
                type = "";
            }
            long postId = 0L;
            if (datum != null && datum.getPostId() != null) {
                postId = datum.getPostId();
            }
            long now = System.currentTimeMillis();
            boolean samePostDebounced = postId > 0
                    && postId == lastTrackedFlipPostId
                    && (now - lastTrackedFlipAtMs) < VIEWS_DWELL_DEBOUNCE_MS;
            if (samePostDebounced) {
                showLog("flushPendingFlipTracking debounced postId=" + postId);
                // Keep dwell timer aligned without re-firing views/GCM.
                try {
                    if (!type.equalsIgnoreCase("ad")
                            && !type.equalsIgnoreCase("share_app")
                            && !type.equalsIgnoreCase("ratings")
                            && !type.equalsIgnoreCase("web_ads")
                            && !type.equalsIgnoreCase("web")) {
                        PostDwellTracker.get().onPostShown(this, postId, type);
                    }
                } catch (Exception ignored) {
                }
                return;
            }
            lastTrackedFlipPostId = postId;
            lastTrackedFlipAtMs = now;
            if (datum != null && datum.getPostType() != null) {
                trackingEvents.FireBase("post_type", datum.getPostType() + "_pos_" + pos);
            }
            BundleDataToGcmPush(pos);
            // View open + dwell: local queue only (ViewsBatchTracker flushes ~60s / background).
            if (!type.equalsIgnoreCase("ad")
                    && !type.equalsIgnoreCase("share_app")
                    && !type.equalsIgnoreCase("ratings")
                    && !type.equalsIgnoreCase("web_ads")
                    && !type.equalsIgnoreCase("web")) {
                try {
                    if (datum != null && datum.getPostId() != null) {
                        updated_post_id_firebase = datum.getPostId().intValue();
                    }
                    setViewsApi();
                    PostDwellTracker.get().onPostShown(this, updated_post_id_firebase, type);
                } catch (Exception viewEx) {
                    viewEx.printStackTrace();
                }
            } else {
                try {
                    PostDwellTracker.get().onPostShown(this, 0, type);
                } catch (Exception ignored) {
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        if (replyParentCommentId != null && replyParentCommentId > 0) {
            requestModel.setParentCommentId(replyParentCommentId);
        }

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
                    if (encreptionRequest == null || encreptionRequest.getData() == null) {
                        return;
                    }
                    String encrpt_data = null;
                    /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        encrpt_data = decrypt(encreptionRequest.getData());
                    }*/
                    encrpt_data = decrypt(encreptionRequest.getData());

                    Gson gsonObj = new Gson();
                    CommentResponuse validateUserOtp = gsonObj.fromJson(encrpt_data, CommentResponuse.class);


                    if (validateUserOtp.getSuccess()) {

                        if (replyParentCommentId != null && replyParentCommentId > 0) {
                            pendingExpandParentId = replyParentCommentId;
                        }
                        leave_comment_white.setText("");
                        clearReplyContext();
                        if (adapter != null) {
                            adapter.incrementCommentCount(positionx);
                        } else {
                            ModelClass.Datum item = list.get(positionx);
                            int comments = item.getComments() != null ? item.getComments() : 0;
                            item.setComments(comments + 1);
                        }
                        if (validateUserOtp.getComments() != null && adapter != null) {
                            adapter.syncCommentCountFromServer(
                                    list.get(positionx).getPostId(),
                                    list.get(positionx).getSubPostId(),
                                    validateUserOtp.getComments());
                        }

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
                    if (encreptionRequest == null || encreptionRequest.getData() == null) {
                        return;
                    }
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
                                comment_chooseLanguage_models.add(
                                        mapCommentDatum(validateUserOtp.getData().get(i)));
                            }


                            //   comment_progress.setVisibility(View.GONE);

                            bindCommentsAdapter();
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
        nativeGoogleAdList.clear();
        nativeFBAdList.clear();
        nativeGoogleAdArray.clear();
        nativeFBAdArray.clear();
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

                bottomNavigationView.getMenu().getItem(0).setTitle("" + languageModelResponse.getData().getReload());
                bottomNavigationView.getMenu().getItem(1).setTitle("" + languageModelResponse.getData().getEvents());
                bottomNavigationView.getMenu().getItem(3).setTitle("" + languageModelResponse.getData().getCategory());
                bottomNavigationView.getMenu().getItem(4).setTitle("" + languageModelResponse.getData().getProfile());
                fab_fullpost.setText("" + languageModelResponse.getData().getFullPost());
                fab_video.setText("" + languageModelResponse.getData().getVideoPost());
                fab_article.setText("" + languageModelResponse.getData().getArticle());
                nodatascreen.setText("" + languageModelResponse.getData().getNoData());
                leave_comment_white.setHint("" + languageModelResponse.getData().getWriteComments());
                applyMagazineShareFooterLabel();

                if (category_id == 0) {
                    type = "" + String.valueOf(SharedPrefsData.getString(HomePage.this, Constants.Selected_Distric_Name1, Constants.PREF_NAME));
                } else {
                    type = languageModelResponse.getData().getMyFeed().toString();
                }
                if(SharedPrefsData.getBool(HomePage.this, Constants.isUnreadPostsSelected, Constants.PREF_NAME)){
                    showLog("setSelectedItemId(R.id.navigation_catagiries)");
                    bottomNavigationView.setSelectedItemId(R.id.navigation_catagiries);
                }else{
                    showLog("setSelectedItemId(R.id.navigation_home)");
                    bottomNavigationView.setSelectedItemId(R.id.navigation_home);
                }

                showLog("List size:"+list.size());

                adapter = new FlipperAdapter(Activity, HomePage.this, type, category_id, getLifecycle());
                adapter.setCallback(this);
                adapter.setCurrentFlipPage(positionx);
                mFlipView.setAdapter(adapter);
                mFlipView.setOnFlipListener(this);
                mFlipView.setOnFlipCompleteListener(this);
                mFlipView.setOverFlipMode(OverFlipMode.GLOW);
                mFlipView.peakNext(false);
                mFlipView.setOnOverFlipListener(this);
                mFlipView.setOnFlipAppear(this);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private JsonObject Articles_All(int category_id) {
        int height=0,width=0;
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        width = displayMetrics.widthPixels;
        height = displayMetrics.heightPixels;
        int isPermissionGranted=0;
        int SDK_VERSION=Build.VERSION.SDK_INT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
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

    public void sendEvents(){
        List<FlipItems> flipItemsList= allsharedpreference.getFlipData();
        JsonArray array= new Gson().toJsonTree(flipItemsList).getAsJsonArray();
        APIService apiService=RestApiClient.getClient().create(APIService.class);

        String jsonString = new com.google.gson.Gson().toJson(array);
        String encrpt_data = null;
        encrpt_data = encrypt(jsonString);
        showLog("API REQUEST E" + jsonString.toString());
        EncritonRequestData Encriton_Request = new EncritonRequestData();
        Encriton_Request.setData(encrpt_data);
        JsonObject array1= new Gson().toJsonTree(Encriton_Request).getAsJsonObject();

        Call<BaseResponse> eventCall=apiService.sendEvents("v3",array1);
        eventCall.enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                if(response.body() != null && 200 == response.body().getStatusCode()){
                    allsharedpreference.setEmptyFlipData();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse> call, Throwable t) {
            }
        });
    }

    private void ShowNews_Data_Values(int category_id,int reqPos) {
        showLog("API CATEGORY ID:" + category_id);
        showLog("API CATEGORY REQPOS:" + reqPos);
        JsonObject otp = Articles_All(category_id);

        APIService apiService = RestApiClient.getClient().create(APIService.class);
        Call<EncreptionRequest> otp_generation = apiService.List_All_Articles("v3", otp);

        try {

            otp_generation.enqueue(new Callback<EncreptionRequest>() {

                @Override
                public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {
                    convertListOfArticles(response,reqPos);
                }

                @Override
                public void onFailure(Call<EncreptionRequest> call, Throwable t) {
                    isApiRequested=false;
                }
            });

        } catch (Exception e) {
        }
    }
    /**
     * Append new articles while skipping any post already present in the list.
     * Prevents the same post showing twice when pagination returns overlapping data.
     * Ads / share_app / ratings (postId == null) are always kept since they are positional.
     */
    private String articleDedupKey(ModelClass.Datum d) {
        if (d == null || d.getPostId() == null) {
            return null;
        }
        String postType = d.getPostType();
        if (postType != null && (postType.equalsIgnoreCase("ad")
                || postType.equalsIgnoreCase("web_ads")
                || postType.equalsIgnoreCase("web")
                || postType.equalsIgnoreCase("ratings")
                || postType.equalsIgnoreCase("share_app"))) {
            return d.getPostId() + "_" + d.getSubPostId() + "_" + postType + "_" + d.getAd_req_position();
        }
        return d.getPostId() + "_" + d.getSubPostId();
    }

    private void addUniqueArticles(java.util.List<ModelClass.Datum> newData) {
        if (newData == null) return;
        java.util.HashSet<String> existing = new java.util.HashSet<>();
        for (ModelClass.Datum d : list) {
            String key = articleDedupKey(d);
            if (key != null) {
                existing.add(key);
            }
        }
        for (ModelClass.Datum d : newData) {
            if (d == null) continue;
            if (d.getPostId() == null) {
                list.add(d);
                continue;
            }
            String key = articleDedupKey(d);
            if (key == null || !existing.contains(key)) {
                list.add(d);
                if (key != null) {
                    existing.add(key);
                }
            } else {
                showLog("DEDUP skipped duplicate post key=" + key);
            }
        }
    }

    public void convertListOfArticles(Response<EncreptionRequest> response,int reqPos){
        no_article_message="Please wait while loading ...";
        showLog("reqPos:::::"+reqPos);
        showLog("isFromLocalArticles:::::"+isFromLocalArticles);
        if(reqPos==3 && !isFromLocalArticles){
            if (response != null && response.body() != null) {
                showReporterLogin = response.body().isShow_reporter_login();
                showLog("showReporterLogin (stored early):" + showReporterLogin);
            }
            isDataParsed=false;
            dummyResponse=response;
            showLog("requestPosition::"+requestPostition);
            requestPostition=1+requestSize;
            showLog("requestPosition::"+requestPostition);
            return;
        }
        try {
            if (response!=null && response.body() != null) {
                showLog("response body not null");
                dummyResponse=null;
                isDataParsed=true;
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
                        showAppUpdateBanner(true);
                    } else if (encreptionRequest.getUpdateMenuStatus() != null && encreptionRequest.getUpdateMenuStatus()==2 ){
                        showAppUpdateBanner(false);
                    } else {
                        hideAppUpdateBanner();
                    }
                    if(encreptionRequest.isCallAppEvents()){
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                sendEvents();
                            }
                        },1000);
                    }
                    if (encreptionRequest.getAdmob_app_id() != null && !encreptionRequest.getAdmob_app_id().isEmpty()) {
                        AdMobInitializer.initialize(HomePage.this, encreptionRequest.getAdmob_app_id());
                    }
                    if (encreptionRequest.getEngagement_badge_color() != null
                            && !encreptionRequest.getEngagement_badge_color().isEmpty()) {
                        EngagementCountHelper.saveColor(HomePage.this, encreptionRequest.getEngagement_badge_color());
                    }
                    if (encreptionRequest.getTheme_color() != null
                            && !encreptionRequest.getTheme_color().isEmpty()) {
                        ThemeColorHelper.saveColor(HomePage.this, encreptionRequest.getTheme_color());
                    }
                    if (encreptionRequest.getPoll_survey_colors() != null) {
                        PollOptionStyleHelper.save(
                                HomePage.this,
                                encreptionRequest.getPoll_survey_colors()
                        );
                    }
                    // Admin-controlled half-post action icon + white-circle sizes (dp).
                    // Cache the polled values; if they changed, re-apply immediately to the
                    // currently visible card so the resize is live (no activity restart).
                    applyActionIconSizeConfig(
                            encreptionRequest.getAction_icon_size_dp(),
                            encreptionRequest.getLike_circle_size_dp()
                    );
                    // Admin-controlled reporter create-menu post-type toggles. Cache the
                    // polled values; if the create menu is open, refresh its buttons live.
                    applyReporterPostTypeConfig(
                            encreptionRequest.getEnable_video_post(),
                            encreptionRequest.getEnable_article_post(),
                            encreptionRequest.getEnable_full_post()
                    );
                    // Admin-controlled title↔body gap (dp). Cache and, if changed,
                    // re-apply to the currently visible card so it updates live.
                    applyTitleBodyGapConfig(encreptionRequest.getTitle_body_gap_dp());
                    // Admin-controlled content-block vertical offset (dp). Same live path.
                    applyContentBlockOffsetConfig(encreptionRequest.getContent_block_offset_dp());
                    ScreenCaptureHelper.saveAllowed(HomePage.this, encreptionRequest.isAllow_screen_capture());
                    cachedCaptureAllowed = encreptionRequest.isAllow_screen_capture();
                    // Re-apply so a toggle change takes effect on this screen without restart.
                    ScreenCaptureHelper.applySecureFlag(HomePage.this);
                    appliedSecureFlag = !cachedCaptureAllowed;
                    // Cricket Live Mini Player: apply feed config immediately, then
                    // kick a dedicated poll so admin ON/OFF is not stuck on a stale
                    // feed payload until the next resume cycle.
                    if (cricketMiniPlayerManager != null) {
                        cricketMiniPlayerManager.update(encreptionRequest.getActive_match());
                        cricketMiniPlayerManager.refreshNow();
                    }
                    if (validateUserOtp.getData() != null && validateUserOtp.getData().size() > 0) {
                        int listSizeBeforeAppend = list.size();
                        if (list.isEmpty()) {
                            list.addAll(validateUserOtp.getData());
                        } else {
                            addUniqueArticles(validateUserOtp.getData());
                        }
                        if (encreptionRequest.isShow_ads() && encreptionRequest.getAd_positions() != null) {
                            final EncreptionRequest adEncRequest = encreptionRequest;
                            final int adListSizeBeforeAppend = listSizeBeforeAppend;
                            Runnable preloadAds = new Runnable() {
                                @Override
                                public void run() {
                                    for (int i = 0; i < adEncRequest.getAd_positions().size(); i++) {
                                        Integer adReqPos = adEncRequest.getAd_positions().get(i).getAd_req_position();
                                        if (adListSizeBeforeAppend > 0 && adReqPos != null
                                                && nativeGoogleAdArray.contains(adReqPos)) {
                                            continue;
                                        }
                                        showLog("ListAll Articles Api trigger and AD Loading 22");
                                        loadAds(
                                                adEncRequest.getAd_positions().get(i).getAd_code(),
                                                adReqPos,
                                                adEncRequest.getAd_positions().get(i).getAd_type()
                                        );
                                    }
                                    showLog("ListAll Articles Api trigger and AD Loading");
                                }
                            };
                            if (listSizeBeforeAppend > 0) {
                                new Handler(Looper.getMainLooper()).post(preloadAds);
                            } else {
                                preloadAds.run();
                            }
                        }
                        requestSize=list.size();
                        last_post_id = validateUserOtp.getLastPostId();
                        skip = validateUserOtp.getSkip();
                        actual_skip = validateUserOtp.getActualSkip();
                        if (list.get(0).getPostId() != null) {
                            updated_post_id_firebase = list.get(0).getPostId().intValue();
                        }
                        showLog("response body not null:"+isFromLocalArticles);
                        //writeNewUser("User", type_firebase, current_time_firebase, draft_id_firebase, device_id_firebase, language_id_firebase, updated_post_id_firebase, positionx);
                        if(isFromLocalArticles){
                           isFromLocalArticles=false;
                           if (languageModelResponse != null && languageModelResponse.getData() != null) {
                               if (category_id == 0) {
                                   type = "" + String.valueOf(SharedPrefsData.getString(HomePage.this, Constants.Selected_Distric_Name1, Constants.PREF_NAME));
                               } else {
                                   type = languageModelResponse.getData().getMyFeed().toString();
                               }
                           }
                           adapter = new FlipperAdapter(Activity, HomePage.this, type, category_id, getLifecycle());
                           adapter.setCallback(this);
                           adapter.setCurrentFlipPage(positionx);
                           mFlipView.setAdapter(adapter);
                           mFlipView.setOnFlipListener(this);
                           mFlipView.setOnFlipCompleteListener(this);
                           mFlipView.setOverFlipMode(OverFlipMode.GLOW);
                           mFlipView.peakNext(false);
                           mFlipView.setOnOverFlipListener(this);
                           mFlipView.setOnFlipAppear(this);
                       }
                        // Theme recreate can leave adapter null if inlist skipped creation
                        // (e.g. translator prefs not ready yet). Ensure adapter exists so
                        // flip restore can run for light↔dark.
                        if (adapter == null && mFlipView != null) {
                            showLog("adapter null after articles — creating for flip restore");
                            if (type == null || type.isEmpty()) {
                                type = String.valueOf(SharedPrefsData.getString(
                                        HomePage.this, Constants.Selected_Distric_Name1, Constants.PREF_NAME));
                            }
                            adapter = new FlipperAdapter(Activity, HomePage.this, type, category_id, getLifecycle());
                            adapter.setCallback(this);
                            adapter.setCurrentFlipPage(positionx);
                            mFlipView.setAdapter(adapter);
                            mFlipView.setOnFlipListener(this);
                            mFlipView.setOnFlipCompleteListener(this);
                            mFlipView.setOverFlipMode(OverFlipMode.GLOW);
                            mFlipView.peakNext(false);
                            mFlipView.setOnOverFlipListener(this);
                            mFlipView.setOnFlipAppear(this);
                        }
                        if (adapter == null) {
                            showLog("adapter null after articles response");
                        } else {
                            showLog("response body not null after load data:");
                            adapter.hasStableIds();
                            if (listSizeBeforeAppend > 0 && listSizeBeforeAppend < list.size()) {
                                adapter.appendData(list, listSizeBeforeAppend);
                            } else {
                                adapter.setData(list);
                            }
                            maybeRestoreFlipPositionAfterArticlesLoad();
                            mFlipView.setVisibility(View.VISIBLE);
                            txtNoData.setVisibility(View.GONE);
                            layout_with_out_data.setVisibility(View.GONE);
                            layout_with_data.setVisibility(View.VISIBLE);

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
                        }

                    } else {

                        if (validateUserOtp.getData().size() == 0) {
                            if (list.size() == 0) {
                                mFlipView.setVisibility(View.GONE);
                                layout_with_data.setVisibility(View.VISIBLE);
                                layout_with_out_data.setVisibility(View.GONE);
                                post_news_scrren.setVisibility(View.GONE);
                                txtNoData.setVisibility(View.VISIBLE);
                                initialCall=true;
                                showLog("txtNoData VISIBLE::");
                            } else {
                                no_article_message="No articles to read further";
                            }

                        }
                    }
                } else {
                    mFlipView.setVisibility(View.GONE);
                    layout_with_data.setVisibility(View.VISIBLE);
                    layout_with_out_data.setVisibility(View.GONE);
                    post_news_scrren.setVisibility(View.GONE);
                    txtNoData.setVisibility(View.VISIBLE);
                    initialCall=true;
                    showLog("txtNoData VISIBLE::");

                }

                if(encreptionRequest.isUpdate_user_language()){
                    LanguageSelection(SharedPrefsData.getInt(getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME));

                }


                showReporterLogin=encreptionRequest.isShow_reporter_login();
                showLog("showReporterLogin:"+showReporterLogin);
                updateCenterFab();
                Boolean pushPermAlert = encreptionRequest.getShow_push_perm_alert();
                showNotificationAlert(pushPermAlert != null && pushPermAlert);
            }else{
                isDataParsed=true;
                showLog("response body null");
                no_article_message="No articles to read further";
            }

            initialCall=true;
            home_page_navigation.setVisibility(View.VISIBLE);
            isApiRequested=false;

        } catch (Exception e) {
            // Gson JsonSyntaxException (e.g. survey options as plain strings) must not crash open.
            e.printStackTrace();
            showLog("convertListOfArticles parse error: " + e.getMessage());
        }

    }

    public void onBackPressed() {
        showLog("isPlayingVideoInFullScreen"+FlipperAdapter.fullscreen);
        showLog("isPlayingVideoInFullScreen"+isPlayingVideoInFullScreen);
        int count = this.getSupportFragmentManager().getBackStackEntryCount();
        if (count == 0) {
            if (mBackPressed + 2000 > System.currentTimeMillis()) {
                Intent setIntent = new Intent(Intent.ACTION_MAIN);
                setIntent.addCategory(Intent.CATEGORY_HOME);
                setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (getApplicationContext() != null && getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)) {
                    startActivity(setIntent);
                }
                return;

            } else {

                if (comment_page.isShown()) {
                    comment_page.setVisibility(View.GONE);

                } else if(txtNoData.getVisibility()==View.VISIBLE){
                    showLog("Check Point #2 : HomePage.setOnClickListener ");
                    Intent intent = new Intent(HomePage.this, HomePage.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    if (getApplicationContext() != null && getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)) {
                        startActivity(intent);
                    }
                    finish();
                }else if(FlipperAdapter.fullscreen){
                    try{
                        if (mFlipView != null && ppi >= 0 && ppi < mFlipView.getChildCount()) {
                            View flipChild = mFlipView.getChildAt(ppi);
                            if (flipChild != null) {
                                PlayerView simpleExoPlayerView = flipChild.findViewById(R.id.normal_video_player);
                                ConstraintLayout layoutParentTop;
                                LinearLayout layoutParentTitle;
                                LinearLayout layoutParentReportDetails;
                                LinearLayout layoutParentMiddle;
                                layoutParentTop = flipChild.findViewById(R.id.layoutParentTop);
                                layoutParentTitle = flipChild.findViewById(R.id.layoutParentTitle);
                                layoutParentReportDetails = flipChild.findViewById(R.id.ll_footer_layout);
                                layoutParentMiddle = flipChild.findViewById(R.id.layoutParentMiddle);
                                adapter.showExoPlayerInFullScreen(FlipperAdapter.fullscreen, simpleExoPlayerView, layoutParentTop, layoutParentMiddle, layoutParentReportDetails, layoutParentTitle);
                            }
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }else if(isPlayingVideoInFullScreen){
                    try{
                        if(adapter.customPlayerUiController!=null){
                            adapter.customPlayerUiController.onYouTubePlayerExitFullScreen();
                        }

                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }
                else{
                    if (floating_action_button.isShown()) {

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

                    } else {

                        if (languageModelResponse != null && languageModelResponse.getData() != null
                                && languageModelResponse.getData().getTapToExit() != null) {
                            Toast.makeText(getBaseContext(), languageModelResponse.getData().getTapToExit(), Toast.LENGTH_SHORT).show();
                        }
                        mBackPressed = System.currentTimeMillis();
                    }
                }

            }
        } else {
            super.onBackPressed();
        }

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

            Handler handler = new Handler(Looper.getMainLooper());
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

    /**
     * Admin-controlled gap (dp) between the half-post title and body, delivered on
     * every feed poll. Caches the value in prefs (FlipperAdapter reads it at bind
     * time) and, when it changes, re-applies to the currently visible card so the
     * spacing updates live without an activity restart.
     */
    private void applyTitleBodyGapConfig(Integer titleBodyGapDp) {
        try {
            if (titleBodyGapDp == null) {
                return;
            }
            int prev = SharedPrefsData.getInt(HomePage.this, Constants.TITLE_BODY_GAP_DP, Constants.PREF_NAME);
            if (prev != titleBodyGapDp) {
                SharedPrefsData.putInt(HomePage.this, Constants.TITLE_BODY_GAP_DP, titleBodyGapDp, Constants.PREF_NAME);
                if (mFlipView != null) {
                    final FlipView flipView = mFlipView;
                    flipView.post(new Runnable() {
                        @Override
                        public void run() {
                            FlipperAdapter.applyTitleBodyGap(HomePage.this, flipView);
                            flipView.requestLayout();
                        }
                    });
                }
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Admin-controlled vertical offset (dp) of the half-post heading+body content
     * block, delivered on every feed poll. Caches the value in prefs (FlipperAdapter
     * reads it at bind time) and, when it changes, re-applies to the currently
     * visible card so the block shifts live without an activity restart.
     */
    private void applyContentBlockOffsetConfig(Integer contentBlockOffsetDp) {
        try {
            if (contentBlockOffsetDp == null) {
                return;
            }
            int prev = SharedPrefsData.getInt(HomePage.this, Constants.CONTENT_BLOCK_OFFSET_DP, Constants.PREF_NAME);
            if (prev != contentBlockOffsetDp) {
                SharedPrefsData.putInt(HomePage.this, Constants.CONTENT_BLOCK_OFFSET_DP, contentBlockOffsetDp, Constants.PREF_NAME);
                if (mFlipView != null) {
                    final FlipView flipView = mFlipView;
                    flipView.post(new Runnable() {
                        @Override
                        public void run() {
                            FlipperAdapter.applyContentBlockOffset(HomePage.this, flipView);
                            flipView.requestLayout();
                        }
                    });
                }
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Admin-controlled reporter create-menu post-type toggles. Caches the polled
     * values in prefs (default true) and, if the create menu is currently open,
     * refreshes its button visibility live without an activity restart.
     */
    private void applyReporterPostTypeConfig(Boolean video, Boolean article, Boolean full) {
        try {
            if (video != null) {
                SharedPrefsData.putBool(HomePage.this, Constants.ENABLE_VIDEO_POST, video, Constants.PREF_NAME);
            }
            if (article != null) {
                SharedPrefsData.putBool(HomePage.this, Constants.ENABLE_ARTICLE_POST, article, Constants.PREF_NAME);
            }
            if (full != null) {
                SharedPrefsData.putBool(HomePage.this, Constants.ENABLE_FULL_POST, full, Constants.PREF_NAME);
            }
            if (post_news_scrren != null && post_news_scrren.getVisibility() == View.VISIBLE) {
                post_news_scrren.post(new Runnable() {
                    @Override
                    public void run() {
                        applyReporterPostTypeVisibility();
                    }
                });
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Reads the reporter create-menu post-type toggles from prefs (default true =
     * enabled) and shows/hides each button (icon + label) in the expanded "+" menu.
     * Called when the menu opens and when a live poll updates a visible menu, so a
     * freshly-opened menu always honors the latest saved config. No rebuild needed.
     */
    private void applyReporterPostTypeVisibility() {
        try {
            android.content.SharedPreferences sp =
                    getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE);
            int videoVis = sp.getBoolean(Constants.ENABLE_VIDEO_POST, true) ? View.VISIBLE : View.GONE;
            int articleVis = sp.getBoolean(Constants.ENABLE_ARTICLE_POST, true) ? View.VISIBLE : View.GONE;
            int fullVis = sp.getBoolean(Constants.ENABLE_FULL_POST, true) ? View.VISIBLE : View.GONE;
            if (fab1 != null) fab1.setVisibility(videoVis);
            if (fab_video != null) fab_video.setVisibility(videoVis);
            if (fab4 != null) fab4.setVisibility(articleVis);
            if (fab_article != null) fab_article.setVisibility(articleVis);
            if (fab2 != null) fab2.setVisibility(fullVis);
            if (fab_fullpost != null) fab_fullpost.setVisibility(fullVis);
        } catch (Exception ignored) {
        }
    }

    /**
     * Admin-controlled half-post action icon + white-circle sizes (dp), delivered
     * on every feed poll. Caches the values in prefs (FlipperAdapter reads them at
     * bind time) and, when a value actually changes, re-applies to the currently
     * visible card immediately so the resize is live without an activity restart.
     */
    private void applyActionIconSizeConfig(Integer actionIconSizeDp, Integer likeCircleSizeDp) {
        try {
            boolean changed = false;
            if (actionIconSizeDp != null) {
                int prev = SharedPrefsData.getInt(HomePage.this, Constants.ACTION_ICON_SIZE_DP, Constants.PREF_NAME);
                if (prev != actionIconSizeDp) {
                    SharedPrefsData.putInt(HomePage.this, Constants.ACTION_ICON_SIZE_DP, actionIconSizeDp, Constants.PREF_NAME);
                    changed = true;
                }
            }
            if (likeCircleSizeDp != null) {
                int prev = SharedPrefsData.getInt(HomePage.this, Constants.LIKE_CIRCLE_SIZE_DP, Constants.PREF_NAME);
                if (prev != likeCircleSizeDp) {
                    SharedPrefsData.putInt(HomePage.this, Constants.LIKE_CIRCLE_SIZE_DP, likeCircleSizeDp, Constants.PREF_NAME);
                    changed = true;
                }
            }
            if (changed && mFlipView != null) {
                final FlipView flipView = mFlipView;
                flipView.post(new Runnable() {
                    @Override
                    public void run() {
                        FlipperAdapter.applyHalfActionIconSizes(HomePage.this, flipView);
                        flipView.requestLayout();
                    }
                });
            }
        } catch (Exception ignored) {
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

        if (positionx == ((list.size() - 2)) || (positionx == list.size())) {
            if (dummyResponse != null) {
                showLog("request at position:: convertListOfArticles");
                convertListOfArticles(dummyResponse, -1);
            } else {
                showLog("request at position::" + requestSize + isApiRequested);
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
        // Mid-flip: minimal index tracking + light pause only.
        // Heavy work (refreshDetails/loadAds/FLAG_SECURE/dwell/views) waits for OnFlipCompleted.
        if (list == null || position < 0 || position >= list.size()) {
            return;
        }
        // Same destination as already settled — skip pause/settle/tracking churn.
        if (position == lastSettledFlipIndex
                && position == positionx
                && !pendingFlipSettleWork
                && mFlipView != null
                && !mFlipView.mIsFlipping) {
            showLog("onFlippedToPage skip same index=" + position);
            return;
        }
        pendingFlipTrackingPos = position;
        showLog("onFlippedToPage:::POS::" + position + "....post_type.." + list.get(position).getPostType());
        isPlayingVideoInFullScreen = false;
        currentView = view;
        initialCall = true;
        positionx = position;
        // Flip restore prefs are written after settle paint (not mid-flip / not at settle start)
        // to avoid SharedPreferences editor work during the animation.
        post_type = list.get(position).getPostType();
        if (post_type == null) {
            post_type = "";
        }
        if (adapter != null) {
            adapter.setCurrentFlipPage(position);
        }
        // Cheap visibility toggle (no WindowManager relayout).
        if (post_type.equalsIgnoreCase("ad")
                || post_type.equalsIgnoreCase("share_app")
                || post_type.equalsIgnoreCase("ratings")) {
            home_page_navigation.setVisibility(View.INVISIBLE);
        } else {
            home_page_navigation.setVisibility(View.VISIBLE);
        }
        if (position >= 0 && position < 2) {
            ppi = position;
        } else {
            ppi = 1;
        }
        pauseVideoLight();
        pendingFlipSettleWork = true;
        // No active flip gesture (programmatic / already settled): same staged settle path.
        if (mFlipView != null && !mFlipView.mIsFlipping) {
            if (cricketMiniPlayerManager != null) {
                cricketMiniPlayerManager.setHiddenForFlipGesture(false);
            }
            schedulePostFlipSettleWork();
        }
    }

    /**
     * Post-settle flip work formerly in onFlippedToPage1 (+60ms mid-animation).
     * Matches Play intent: keep features, don't thrash during Camera flip frames.
     */
    private void runDeferredFlipSettleWork() {
        if (!pendingFlipSettleWork) {
            return;
        }
        pendingFlipSettleWork = false;
        int position = positionx;
        if (list == null || position < 0 || position >= list.size()) {
            return;
        }
        showLog("runDeferredFlipSettleWork:::POS::" + position);
        post_type = list.get(position).getPostType();
        if (post_type == null) {
            post_type = "";
        }
        try {
            if (list.get(position).getPostId() != null) {
                updated_post_id_firebase = list.get(position).getPostId().intValue();
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        // firbaseJSONObj was write-only (never read) — skip Gson build on settle.
        // Whether this post is allowed to be captured (screenshots): ad/web surfaces
        // and admin-allowed capture clear FLAG_SECURE; everything else keeps it set.
        // Use cachedCaptureAllowed — do not hit SharedPreferences on settle.
        boolean captureAllowed = cachedCaptureAllowed
                || post_type.equalsIgnoreCase("ad")
                || post_type.equalsIgnoreCase("web_ads")
                || post_type.equalsIgnoreCase("web");
        boolean desiredSecure = !captureAllowed;
        // Only hit WindowManager when the secure state actually changes.
        if (appliedSecureFlag == null || appliedSecureFlag != desiredSecure) {
            if (desiredSecure) {
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
            } else {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
            }
            appliedSecureFlag = desiredSecure;
        }
        if (post_type.equalsIgnoreCase("image_gallery")) {
            if (adapter != null && mFlipView != null && ppi >= 0 && ppi < mFlipView.getChildCount()) {
                View flipChild = mFlipView.getChildAt(ppi);
                if (flipChild != null && flipChild.findViewById(R.id.relativeLayoutShareOpt) != null) {
                    adapter.closeGalleryShareLayout(flipChild);
                }
            }
        }
        refreshDetails();
        try {
            if (player != null) {
                player.setPlayWhenReady(false);
                player.stop();
                player.seekTo(0);
            }
            if ("full_video".equalsIgnoreCase(post_type) || "normal_video".equalsIgnoreCase(post_type)) {
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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String postTypeAtPos = list.get(position).getPostType();
        if (postTypeAtPos != null && postTypeAtPos.equalsIgnoreCase("ad")) {
            showLog("ONFLIPSETTLE AD Loading");
            loadAds(list.get(position).getNext_req_ad_code(),
                    list.get(position).getNext_ad_req_position(),
                    list.get(position).getAd_type());
        }
    }



    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        int flipIndex = resolveCurrentFlipIndexForSave();
        long postId = resolvePostIdForFlipIndex(flipIndex);
        outState.putInt(STATE_FLIP_INDEX, flipIndex);
        outState.putLong(STATE_FLIP_POST_ID, postId);
        // Mirror to prefs so light↔dark restore works even if bundle is dropped
        // (e.g. CLEAR_TASK relaunch during uiMode) or read after a delayed recreate.
        persistFlipRestoreCheckpoint(flipIndex, postId, flipIndex > 0 || postId > 0);
        showLog("onSaveInstanceState flip index=" + flipIndex + " postId=" + postId);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // onCreate already reads this; re-read as a safety net if recreate skipped that path.
        if (!pendingFlipRestore) {
            readPendingFlipRestoreState(savedInstanceState);
        }
    }

    private void readPendingFlipRestoreState(Bundle savedInstanceState) {
        int bundleIndex = 0;
        long bundlePostId = -1L;
        if (savedInstanceState != null) {
            bundleIndex = savedInstanceState.getInt(STATE_FLIP_INDEX, 0);
            bundlePostId = savedInstanceState.getLong(STATE_FLIP_POST_ID, -1L);
        }

        // Prefs backup: covers dark→light (and light→dark) when bundle is empty/stale.
        // Only honor prefs written recently (theme recreate is near-instant).
        // StartingActivity clears these on splash cold launch so Recents kill→reopen
        // cannot restore mid-feed via a still-fresh TTL checkpoint.
        boolean prefsPending = SharedPrefsData.getBool(
                this, Constants.FLIP_RESTORE_PENDING, Constants.PREF_NAME);
        long prefsAt = 0L;
        try {
            String atRaw = SharedPrefsData.getString(
                    this, Constants.FLIP_RESTORE_AT, Constants.PREF_NAME);
            if (atRaw != null && !atRaw.isEmpty()) {
                prefsAt = Long.parseLong(atRaw.trim());
            }
        } catch (Exception ignored) {
        }
        final long PREFS_TTL_MS = 120_000L;
        boolean prefsFresh = prefsPending
                && prefsAt > 0
                && (System.currentTimeMillis() - prefsAt) <= PREFS_TTL_MS;
        int prefsIndex = SharedPrefsData.getInt(
                this, Constants.FLIP_RESTORE_INDEX, Constants.PREF_NAME);
        long prefsPostId = -1L;
        try {
            String raw = SharedPrefsData.getString(
                    this, Constants.FLIP_RESTORE_POST_ID, Constants.PREF_NAME);
            if (raw != null && !raw.isEmpty()) {
                prefsPostId = Long.parseLong(raw.trim());
            }
        } catch (Exception ignored) {
        }

        // Prefer the richer of bundle vs prefs (postId match is most reliable).
        if (bundlePostId > 0) {
            pendingRestoreFlipIndex = bundleIndex;
            pendingRestorePostId = bundlePostId;
        } else if (prefsFresh && prefsPostId > 0) {
            pendingRestoreFlipIndex = prefsIndex;
            pendingRestorePostId = prefsPostId;
        } else if (bundleIndex > 0) {
            pendingRestoreFlipIndex = bundleIndex;
            pendingRestorePostId = bundlePostId;
        } else if (prefsFresh && prefsIndex > 0) {
            pendingRestoreFlipIndex = prefsIndex;
            pendingRestorePostId = prefsPostId;
        } else {
            pendingRestoreFlipIndex = 0;
            pendingRestorePostId = -1L;
            if (prefsPending && !prefsFresh) {
                persistFlipRestoreCheckpoint(0, -1L, false);
            }
        }

        // Cold start: no bundle and no pending prefs → stay at article 0.
        pendingFlipRestore = pendingRestoreFlipIndex > 0 || pendingRestorePostId > 0;
        showLog("pending flip restore index=" + pendingRestoreFlipIndex
                + " postId=" + pendingRestorePostId
                + " active=" + pendingFlipRestore
                + " bundle=" + (savedInstanceState != null)
                + " prefsPending=" + prefsPending);
    }

    /** Keep a durable checkpoint while the user flips so theme recreate can restore. */
    private void persistFlipRestoreCheckpoint(int flipIndex, long postId, boolean pending) {
        try {
            // Single editor.apply() — avoids 4 separate SharedPreferences transactions.
            android.content.SharedPreferences.Editor editor =
                    getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE).edit();
            editor.putInt(Constants.FLIP_RESTORE_INDEX, flipIndex);
            editor.putString(Constants.FLIP_RESTORE_POST_ID, String.valueOf(postId));
            editor.putBoolean(Constants.FLIP_RESTORE_PENDING, pending);
            editor.putString(
                    Constants.FLIP_RESTORE_AT,
                    pending ? String.valueOf(System.currentTimeMillis()) : "0");
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int resolveCurrentFlipIndexForSave() {
        int flipIndex = positionx;
        try {
            if (mFlipView != null) {
                int cur = mFlipView.getCurrentPage();
                if (cur >= 0) {
                    flipIndex = cur;
                }
            }
        } catch (Exception ignored) {
        }
        if (flipIndex < 0) {
            flipIndex = 0;
        }
        return flipIndex;
    }

    private long resolvePostIdForFlipIndex(int flipIndex) {
        try {
            if (list != null && flipIndex >= 0 && flipIndex < list.size()) {
                Long id = list.get(flipIndex).getPostId();
                if (id != null) {
                    return id;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1L;
    }

    /** Call on intentional feed resets (local/my-feed switch) — not on theme recreate. */
    private void clearPendingFlipRestore() {
        pendingFlipRestore = false;
        pendingRestoreFlipIndex = 0;
        pendingRestorePostId = -1L;
        persistFlipRestoreCheckpoint(0, -1L, false);
    }

    /**
     * After articles load (setData / append), restore the flip page that was showing
     * before a DayNight recreate (battery saver ↔ light/dark, either direction).
     * <p>
     * Behavior:
     * <ul>
     *   <li>Prefer matching {@code pendingRestorePostId} in the loaded list.</li>
     *   <li>Else if saved index is within the loaded list, use that index.</li>
     *   <li>Else stay at 0 (post not in loaded list / index past end — no extra
     *       fetch solely for restore).</li>
     *   <li>Cold start (no saved state) never sets pending → stays at article 0.</li>
     * </ul>
     */
    private void maybeRestoreFlipPositionAfterArticlesLoad() {
        if (!pendingFlipRestore) {
            return;
        }
        if (list == null || list.isEmpty() || mFlipView == null || adapter == null) {
            return;
        }
        int target = -1;
        if (pendingRestorePostId > 0) {
            for (int i = 0; i < list.size(); i++) {
                try {
                    Long id = list.get(i).getPostId();
                    if (id != null && id.longValue() == pendingRestorePostId) {
                        target = i;
                        break;
                    }
                } catch (Exception ignored) {
                }
            }
        }
        if (target < 0 && pendingRestoreFlipIndex > 0 && pendingRestoreFlipIndex < list.size()) {
            target = pendingRestoreFlipIndex;
            showLog("flip restore: postId not in list, using saved index=" + target);
        }
        if (target < 0) {
            // Post not in the loaded batch and index past end — stay at 0.
            clearPendingFlipRestore();
            showLog("flip restore: target not found in loaded list, staying at 0");
            return;
        }

        // Clear pending before flip so a later intentional reset does not re-jump.
        clearPendingFlipRestore();
        if (target <= 0) {
            return;
        }
        try {
            positionx = target;
            adapter.setCurrentFlipPage(target);
            final int page = target;
            // Double-post: FlipView setData/dataSetChanged flips to 0 first; a second
            // frame covers any delayed observer / peakNext that would reset again.
            mFlipView.post(new Runnable() {
                @Override
                public void run() {
                    flipToRestoredPage(page);
                    if (mFlipView != null) {
                        mFlipView.post(new Runnable() {
                            @Override
                            public void run() {
                                flipToRestoredPage(page);
                            }
                        });
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void flipToRestoredPage(int page) {
        try {
            if (mFlipView == null || adapter == null) {
                return;
            }
            int pageCount = mFlipView.getPageCount();
            if (pageCount <= 0) {
                return;
            }
            int safePage = Math.min(page, pageCount - 1);
            if (safePage < 0) {
                return;
            }
            positionx = safePage;
            adapter.setCurrentFlipPage(safePage);
            mFlipView.flipTo(safePage);
            showLog("flip restore: flipped to page " + safePage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        cancelPendingFlipSettleCallbacks();
        try {
            PostDwellTracker.get().flush(this);
        } catch (Exception ignored) {
        }
        if (playInAppUpdateHelper != null) {
            playInAppUpdateHelper.unregister();
            playInAppUpdateHelper = null;
        }
        if (cricketMiniPlayerManager != null) cricketMiniPlayerManager.onDestroy();
        adapter = null;
        stopMagazineShareBlink();
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
            lbm.unregisterReceiver(REPORTER_STATUS);
            lbm.unregisterReceiver(CURRENT_LOCATION);
            if (receiver != null) unregisterReceiver(receiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        showLog("isUnreadPostsSelected set to false #4");
    }


    @Override
    public void onResume() {
        super.onResume();
        try {
            PostDwellTracker.get().onResumed(this);
        } catch (Exception ignored) {
        }
        try {
            // Quiet GPS refresh for admin "last device location" (throttled; no permission prompt).
            SessionAnalyticsTracker.get().maybeRefreshDeviceLocation(this);
        } catch (Exception ignored) {
        }
        try {
            maybeSoftRequestDraftGpsPermission();
        } catch (Exception ignored) {
        }
        if (playInAppUpdateHelper != null
                && layoutAppUpdateBanner != null
                && layoutAppUpdateBanner.getVisibility() == View.VISIBLE) {
            playInAppUpdateHelper.resumeIfDownloaded();
        }
        if (cricketMiniPlayerManager != null) cricketMiniPlayerManager.onResume();
        updateSpecialTabBlinkIndicator();
        try {
            updateCenterFab();
        } catch (Exception fabEx) {
            fabEx.printStackTrace();
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
                    if("3".equalsIgnoreCase(list.get(positionx).getMediaType())){
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
                if (encreptionRequest == null || encreptionRequest.getData() == null) return;
                String encrpt_data = decrypt(encreptionRequest.getData());

                showLog("ENCRYPT" + encrpt_data);

                Gson gsonObj = new Gson();
                LikeDownloadRespouse likeDownloadRespouse = gsonObj.fromJson(encrpt_data, LikeDownloadRespouse.class);


                if (likeDownloadRespouse != null && Boolean.TRUE.equals(likeDownloadRespouse.getSuccess())) {

                }

            }

            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {


            }
        });

    }

    @Override
    protected void onUserLeaveHint() {
        try {
            SessionAnalyticsTracker.get().onUserLeaveHint(this);
        } catch (Exception ignored) {
        }
        super.onUserLeaveHint();
    }

    /**
     * Soft-request LOCATION once on first Home so draft GPS works for guests/readers
     * who never open create-post. Does not re-prompt after deny / prior ask.
     */
    private void maybeSoftRequestDraftGpsPermission() {
        if (draftGpsPermissionLauncher == null) return;
        if (!DeviceLocationHelper.shouldSoftRequestOnce(this)) return;
        // Mark before launch so rotation / multi-resume does not stack dialogs.
        DeviceLocationHelper.markSoftAsked(this);
        // Slight delay so Home UI settles before the system dialog.
        getWindow().getDecorView().postDelayed(() -> {
            if (isFinishing() || isDestroyed()) return;
            if (DeviceLocationHelper.hasLocationPermission(this)) {
                try {
                    SessionAnalyticsTracker.get().forceRefreshDeviceLocation(this);
                } catch (Exception ignored) {
                }
                return;
            }
            try {
                draftGpsPermissionLauncher.launch(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                });
            } catch (Exception e) {
                showLog("draft GPS soft request failed: " + e.getMessage());
            }
        }, 800L);
    }

    @Override
    public void onPause() {
        try {
            PostDwellTracker.get().onPaused(this);
        } catch (Exception ignored) {
        }
        //new flip
        if (cricketMiniPlayerManager != null) cricketMiniPlayerManager.onPause();
        pauseVideo();
        super.onPause();
        try {
            if (FlipperAdapter.exoPlayer != null) {
                FlipperAdapter.exoPlayer.setPlayWhenReady(false);
                FlipperAdapter.exoPlayer.pause();
                FlipperAdapter.exoPlayer.stop();
                FlipperAdapter.exoPlayer.release();
                FlipperAdapter.exoPlayer = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (adapter != null && adapter.nativeAd != null) {
                showLog("native Ad");
                NativeAd activeNativeAd = adapter.nativeAd;
                if (activeNativeAd.getMediaContent() != null
                        && activeNativeAd.getMediaContent().getVideoController() != null) {
                    showLog("native Ad getMediaContent pause");
                    activeNativeAd.getMediaContent().getVideoController().mute(true);
                    activeNativeAd.getMediaContent().getVideoController().pause();
                }
            }
        } catch (Exception e) {
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
            Intent intent;
            switch (item.getItemId()) {
                // ... implement code here
                case R.id.navigation_home:
                    //firebase events trigger

                    writeButtonClick("User", type_firebase_button, current_time_firebase, draft_id_firebase, device_id_firebase, button_name_home);
                    showLog("initialCall:::" + initialCall);
                    showLog("RELOAD:::" + languageModelResponse.getData().getReload());
                    showLog("RELOAD getTitle:::" + item.getTitle().toString());
                    showLog(" layout_with_out_data:::" + layout_with_out_data.getVisibility());
                    showLog(" layout_with_data:::" + layout_with_data.getVisibility());
                    showLog(" txtNoData:::" + txtNoData.getVisibility());
                    if(initialCall){
                        nativeGoogleAdList.clear();
                        nativeFBAdList.clear();
                        nativeGoogleAdArray.clear();
                        nativeFBAdArray.clear();
                        showLog("isUnreadPostsSelected set to false #5");
                        SharedPrefsData.putBool(HomePage.this, Constants.isUnreadPostsSelected, false, Constants.PREF_NAME);
                        showLog("Check Point #3 : HomePage.setOnClickListener ");
                        intent = new Intent(HomePage.this, HomePage.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        if (getApplicationContext() != null && getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)) {
                            startActivity(intent);
                        }
                        finish();
                    }


//                    if (item.getTitle().toString().equalsIgnoreCase("" + languageModelResponse.getData().getReload())) {
//                        showLog("Check Point #3 : HomePage.setOnClickListener ");
//                        Intent intent = new Intent(HomePage.this, HomePage.class);
//                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                        if (getApplicationContext() != null && getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)) {
//                            startActivity(intent);
//                        }
//                        finish();
//
//                    } else {
//                        if (txtNoData.getVisibility() == View.VISIBLE && item.getTitle().toString().equalsIgnoreCase(languageModelResponse.getData().getHome())) {
//                            showLog("Check Point #4 : HomePage.setOnClickListener ");
//                            Intent intent = new Intent(HomePage.this, HomePage.class);
//                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                            if (getApplicationContext() != null && getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)) {
//                                startActivity(intent);
//                            }
//                            finish();
//                        }
//                    }
                    return true;
                case R.id.navigation_catagiries:
                    nativeGoogleAdList.clear();
                    nativeFBAdList.clear();
                    nativeGoogleAdArray.clear();
                    nativeFBAdArray.clear();
                    clearSpecialTabBlink();
                    SharedPrefsData.putBool(HomePage.this, Constants.isUnreadPostsSelected, true, Constants.PREF_NAME);
                    // firebase events triggered
                  //  Toast.makeText(HomePage.this, "Coming soon..", Toast.LENGTH_SHORT).show();
                    // Bihar Chimney Explosion: PM Modi Announces Rs 2 Lakh Ex-Gratia
                    //एक्टर पर पत्नी से मारपीट और रेप का आरोप
                    //सिक्किम रोड एक्सीडेंट में राजस्थान के 3 जवान शहीद
                    //தம்பி பில் இன்னும் வரல...திமுக கலகல...!

                    //ಭೀಕರ ಅಪಘಾತ: 8 ಮಂಡಿ ಅಯ್ಯಪ್ಪಸ್ವಾಮಿ ಭಕ್ತರು ಸಾವು
                    //BREAKING: 5,204 పోస్టులకు నోటిఫికేషన్ విడుదల!
                  //  sendNotification2();
                   // sendNotification(null,"మోడీ రోడ్ షో నేపథ్యంలో బాంబు బెదిరింపులు వచ్చాయి","1110671","");
                   // writeButtonClick("User", type_firebase_button, current_time_firebase, draft_id_firebase, device_id_firebase, button_name_events);
//                    Intent quizIntent = new Intent(HomePage.this, QuizPageActivity.class);
//                    if (getApplicationContext() != null && getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)) {
//                        startActivity(quizIntent);
//                    }
//                    finish();
                    showLog("Check Point #5 : HomePage.setOnClickListener ");
                     intent = new Intent(HomePage.this, HomePage.class);
                     intent.putExtra("from","special");
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    if (getApplicationContext() != null && getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)) {
                        startActivity(intent);
                    }
                    finish();
                    return true;
                case R.id.navigation_stricker:
                    // firebase events triggered
                    showLog("isUnreadPostsSelected set to false #1");
                    //SharedPrefsData.putBool(HomePage.this, Constants.isUnreadPostsSelected, false, Constants.PREF_NAME);
                    writeButtonClick("User", type_firebase_button, current_time_firebase, draft_id_firebase, device_id_firebase, button_name_post);
                    showLog("showReporterLogin:"+showReporterLogin);
                    // Magazine posts: center footer Share -> always share.
                    if (isCurrentPostMagazine()) {
                        try {
                            CallShareListener magListener = new CallShareListener();
                            magListener.setShareInterfaceListener(adapter);
                            magListener.triggerEvent();
                            shareCurrentPostViaWhatsAppFooter();
                        } catch (Exception magEx) {
                            magEx.printStackTrace();
                        }
                        return false;
                    }
                    if (!showReporterLogin) {
                        showLog("onClick of whatsapp");
                        try {
                            CallShareListener listener = new CallShareListener();
                            listener.setShareInterfaceListener(adapter);
                            listener.triggerEvent();
                            showLog("onClick of whatsapp:" + ppi);
                            shareCurrentPostViaWhatsAppFooter();
                        } catch (Exception waEx) {
                            throw waEx;
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
                                        applyReporterPostTypeVisibility();

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
                    showLog("isUnreadPostsSelected set to false #2");
                    SharedPrefsData.putBool(HomePage.this, Constants.isUnreadPostsSelected, false, Constants.PREF_NAME);
                    // firebase events triggered
                    writeButtonClick("User", type_firebase_button, current_time_firebase, draft_id_firebase, device_id_firebase, button_name_category);

                    Intent searchIntent = new Intent(HomePage.this, SearchActivity.class);
                    if (getApplicationContext() != null && getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)) {
                        startActivity(searchIntent);
                    }
                    //finish();

                    return false;
                case R.id.navigation_profile:
                    showLog("isUnreadPostsSelected set to false #3");
                    SharedPrefsData.putBool(HomePage.this, Constants.isUnreadPostsSelected, false, Constants.PREF_NAME);
                    // firebase events triggered
                    writeButtonClick("User", type_firebase_button, current_time_firebase, draft_id_firebase, device_id_firebase, button_name_profile);

                     intent = new Intent(HomePage.this, newProfileDetails.class);
                    if (getApplicationContext() != null && getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)) {
                        startActivity(intent);
                    }

                    //finish();

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
                    if (encreptionRequest == null || encreptionRequest.getData() == null) {
                        return;
                    }
                    String encrpt_data = null;
                    encrpt_data = decrypt(encreptionRequest.getData());

                    Gson gsonObj = new Gson();
                    surveyFeedbackResponse = gsonObj.fromJson(encrpt_data, SurveyFeedbackResponse.class);

                    if (surveyFeedbackResponse != null && surveyFeedbackResponse.getSuccess()
                            && surveyFeedbackResponse.getData() != null
                            && surveyFeedbackResponse.getData().size() >= 2) {
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
                        datum.setLikes(list.get(position).getLikes());
                        datum.setComments(list.get(position).getComments());
                        datum.setLikeStatus(list.get(position).getLikeStatus());
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
        // Enqueue only — ViewsBatchTracker flushes ~60s or on background (no per-flip POST).
        try {
            final int postId = updated_post_id_firebase;
            final int draftId = draft_id_firebase > 0
                    ? draft_id_firebase
                    : SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME);
            final String analyticsSession = SessionAnalyticsTracker.get().getActiveSessionId();
            ViewsBatchTracker.get().enqueueImpression(
                    getApplicationContext(), postId, draftId, analyticsSession, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JsonObject jsonObjectRequest_Views() {

        ListOfUserArticlesRequest requestModel = new ListOfUserArticlesRequest();
        requestModel.setPostId((long) updated_post_id_firebase);
        requestModel.setDraftId(SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME));   //
        String analyticsSession = SessionAnalyticsTracker.get().getActiveSessionId();
        if (analyticsSession != null) {
            requestModel.setAnalyticsSessionId(analyticsSession);
        }


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

    private void setupSpecialTabBulb() {
        if (bottomNavigationView == null) {
            return;
        }
        bottomNavigationView.getMenu().getItem(SPECIAL_TAB_INDEX)
                .setIcon(R.drawable.ic_nav_icon_transparent);
        ensureSpecialTabBulbOverlay();
        updateSpecialTabBlinkIndicator();
    }

    private void ensureSpecialTabBulbOverlay() {
        if (bottomNavigationView == null) {
            return;
        }
        if (specialTabBulbOverlay != null) {
            return;
        }

        BottomNavigationItemView itemView = findSpecialTabItemView();
        if (itemView == null) {
            if (specialTabBulbSetupRetryCount < 10) {
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
            specialTabBulbOverlay.setImageResource(R.drawable.ic_bulb_blinking);
            specialTabBulbOverlay.setScaleType(ImageView.ScaleType.FIT_CENTER);
            specialTabBulbOverlay.setAlpha(1f);
        }

        View defaultIcon = itemView.findViewById(
                com.google.android.material.R.id.navigation_bar_item_icon_view);
        if (defaultIcon != null) {
            defaultIcon.setVisibility(View.INVISIBLE);
        }
    }

    private void updateSpecialTabBlinkIndicator() {
        if (bottomNavigationView == null) {
            return;
        }
        Logone.d("HomePage", "SpecialTabBlink pending:" + !specialTabViewedThisSession);
        ensureSpecialTabBulbOverlay();
        if (!specialTabViewedThisSession) {
            showSpecialTabBlink();
        } else {
            showSpecialTabStaticBulb();
        }
    }

    private void showSpecialTabBlink() {
        if (bottomNavigationView == null) {
            return;
        }
        ensureSpecialTabBulbOverlay();
        if (specialTabBulbOverlay == null) {
            if (specialTabBulbSetupRetryCount < 10) {
                specialTabBulbSetupRetryCount++;
                bottomNavigationView.postDelayed(this::showSpecialTabBlink, 50);
            }
            return;
        }
        specialTabBulbSetupRetryCount = 0;
        specialTabBulbOverlay.setVisibility(View.VISIBLE);
        BottomNavigationItemView itemView = findSpecialTabItemView();
        if (itemView != null) {
            View defaultIcon = itemView.findViewById(
                    com.google.android.material.R.id.navigation_bar_item_icon_view);
            if (defaultIcon != null) {
                defaultIcon.setVisibility(View.INVISIBLE);
            }
        }
        startSpecialTabBlinkAnimation();
    }

    private void clearSpecialTabBlink() {
        specialTabViewedThisSession = true;
        SharedPrefsData.putBool(HomePage.this, Constants.Event_Status, false, Constants.PREF_NAME);
        showSpecialTabStaticBulb();
    }

    private void showSpecialTabStaticBulb() {
        if (bottomNavigationView == null) {
            return;
        }
        ensureSpecialTabBulbOverlay();
        if (specialTabBulbOverlay == null) {
            return;
        }
        specialTabBulbSetupRetryCount = 0;
        stopSpecialTabBlinkAnimation();
        specialTabBulbOverlay.setImageResource(R.drawable.ic_bulb_blinking);
        specialTabBulbOverlay.setAlpha(1f);
        specialTabBulbOverlay.setVisibility(View.VISIBLE);
        BottomNavigationItemView itemView = findSpecialTabItemView();
        if (itemView != null) {
            View defaultIcon = itemView.findViewById(
                    com.google.android.material.R.id.navigation_bar_item_icon_view);
            if (defaultIcon != null) {
                defaultIcon.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void restoreSpecialTabIcon() {
        if (bottomNavigationView == null) {
            return;
        }
        specialTabBulbSetupRetryCount = 0;
        stopSpecialTabBlinkAnimation();
        if (specialTabBulbOverlay != null) {
            specialTabBulbOverlay.setVisibility(View.GONE);
            specialTabBulbOverlay.setImageResource(R.drawable.ic_bulb_blinking);
            specialTabBulbOverlay.setAlpha(1f);
        }
        BottomNavigationItemView itemView = findSpecialTabItemView();
        if (itemView != null) {
            View defaultIcon = itemView.findViewById(
                    com.google.android.material.R.id.navigation_bar_item_icon_view);
            if (defaultIcon != null) {
                defaultIcon.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * Locates the Material bottom-nav item view for the Special tab so the large
     * bulb overlay can be centered on it (40dp x 50sdp, matching legacy badge_layout).
     */
    @Nullable
    private BottomNavigationItemView findSpecialTabItemView() {
        if (bottomNavigationView == null) {
            return null;
        }
        BottomNavigationMenuView bottomNavigationMenuView =
                (BottomNavigationMenuView) bottomNavigationView.getChildAt(0);
        if (bottomNavigationMenuView == null) {
            return null;
        }
        bottomNavigationMenuView.setClipChildren(false);
        bottomNavigationMenuView.setClipToPadding(false);
        for (int i = 0; i < bottomNavigationMenuView.getChildCount(); i++) {
            View item = bottomNavigationMenuView.getChildAt(i);
            if (!(item instanceof BottomNavigationItemView)) {
                continue;
            }
            BottomNavigationItemView itemView = (BottomNavigationItemView) item;
            if (itemView.getItemData().getItemId() != R.id.navigation_catagiries) {
                continue;
            }
            itemView.setClipChildren(false);
            itemView.setClipToPadding(false);
            return itemView;
        }
        return null;
    }

    private void startSpecialTabBlinkAnimation() {
        if (specialTabBulbOverlay == null || specialTabViewedThisSession) {
            return;
        }
        stopSpecialTabBlinkAnimation();
        specialTabBulbOverlay.setVisibility(View.VISIBLE);
        specialTabBulbOverlay.setColorFilter(
                ContextCompat.getColor(this, R.color.red), PorterDuff.Mode.SRC_ATOP);
        ObjectAnimator animator = ObjectAnimator.ofFloat(specialTabBulbOverlay, View.ALPHA, 1f, 0.35f);
        animator.setDuration(700);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
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
        Spannable pnTitle=makeSpannable(title,"\\[.*?\\]");

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
        showLog("BundleDataToGcmPush");
        try {
            FlipItems flipItems=new FlipItems();
            HashMap<String,String> data = new HashMap<>();

            data.put("POST_TYPE",list.get(pos).getPostType());
            data.put("POSTID", ""+list.get(pos).getPostId());
            data.put("POST_POS", ""+pos);
            data.put("VERSION_CODE", ""+BuildConfig.VERSION_CODE);
            data.put("VERSION_NAME", ""+BuildConfig.VERSION_NAME);
            data.put("EVENT_NAME", "onFlip");
            data.put("PLATFORM", "ANDROID");
            flipItems.setPOST_TYPE(list.get(pos).getPostType());
            flipItems.setPOSTID(""+list.get(pos).getPostId());
            flipItems.setPOST_POS(""+pos);
            flipItems.setVERSION_CODE(""+BuildConfig.VERSION_CODE);
            flipItems.setVERSION_NAME(BuildConfig.VERSION_NAME);
            flipItems.setPLATFORM("ANDROID");
            flipItems.setEVENT_NAME("onFlip");
            flipItems.setDraft_id(draft_id_firebase > 0
                    ? draft_id_firebase
                    : SharedPrefsData.getInt(context, Constants.DraftID, Constants.PREF_NAME));

            trackingEvents.trackFirebaseAnalytics(new Random().nextInt(),"POST_TYPE_"+list.get(pos).getPostType());
            trackingEvents.trackFirebaseAnalytics(new Random().nextInt(),"POSTID_"+list.get(pos).getPostId());
            trackingEvents.trackFirebaseAnalytics(new Random().nextInt(),"POST_POS_"+pos);
            trackingEvents.trackFirebaseAnalytics(new Random().nextInt(),"VERSION_CODE_"+BuildConfig.VERSION_CODE);
            trackingEvents.trackFirebaseAnalytics(new Random().nextInt(),"VERSION_NAME_"+BuildConfig.VERSION_NAME.replace(".","_"));
            trackingEvents.trackFirebaseAnalytics(new Random().nextInt(),"EVENT_NAME_onFlip");
            trackingEvents.trackFirebaseAnalytics(new Random().nextInt(),"PLATFORM_ANDROID");
            data.put("DRAFTID", ""+(draft_id_firebase > 0
                    ? draft_id_firebase
                    : SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME)));
            if(list!=null && positionx >= 0 && positionx < list.size()){
                String pt = list.get(positionx).getPostType();
                if(pt != null && pt.equalsIgnoreCase("ad")){
                    try{
                        if(adapter!=null && adapter.nativeAd != null && adapter.nativeAd.getResponseInfo()!=null && adapter.nativeAd.getResponseInfo().getLoadedAdapterResponseInfo()!=null &&
                                adapter.nativeAd.getResponseInfo().getLoadedAdapterResponseInfo().getAdapterClassName()!=null){
                            data.put("AD Type", adapter.nativeAd.getResponseInfo().getLoadedAdapterResponseInfo().getAdapterClassName());
                            data.put("AD Imps", ""+adImpress);
                            flipItems.setAD_Type(adapter.nativeAd.getResponseInfo().getLoadedAdapterResponseInfo().getAdapterClassName());
                            flipItems.setADImps(""+adImpress);
                            trackingEvents.trackFirebaseAnalytics(new Random().nextInt(),"AD_Type_"+adapter.nativeAd.getResponseInfo().getLoadedAdapterResponseInfo().getAdapterClassName().replace("","_"));
                            trackingEvents.trackFirebaseAnalytics(new Random().nextInt(),"AD_Imps_"+adImpress);
                            adImpress=false;
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        adImpress=false;
                    }

                    data.put("ADERROR", adError);
                    trackingEvents.trackFirebaseAnalytics(new Random().nextInt(),"ADERROR_"+adError);
                    adError="";
                }
            }

            try {

                SimpleDateFormat s = new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss aa", Locale.ENGLISH);
                String format = s.format(new Date());
                String regex = "(?i)A.M.|a.m.|A.M";
                String regexPM = "(?i)P.M.|p.m.|P.M";
                if(format.contains(regexPM)){
                    format=format.replaceAll(regexPM, "PM");
                }
                if(format.contains(regex)){
                    format=format.replaceAll(regex, "AM");
                }
                data.put("DATE_TIME", format);
                flipItems.setDATE_TIME(format);

            } catch (Exception e) {

                e.printStackTrace();

            }
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
                String format = sdf.format(new Date());
                showLog("DATE_TIME ###" + format);
                showLog("DATE_TIME ###;;;" + new Date().toString());
                flipItems.setDATE_TIME_1(format);
                flipItems.setDATE_TIME_2(new Date().toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            allsharedpreference.setFlipData(flipItems);

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
    public SpannableStringBuilder makeSpannable(String text, String regex) {
        String colorCode="#FF0000,#02a6d8";
        int colorIndex=0;
        ArrayList<String> colorCodeList=new ArrayList<>();
        if(colorCode.contains(",")){
            colorCodeList=new ArrayList<>(Arrays.asList(colorCode.split(",")));
        }else{
            colorCodeList.add(colorCode);
        }

        StringBuffer sb = new StringBuffer();
        SpannableStringBuilder spannable = new SpannableStringBuilder();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            sb.setLength(0);
            String group = matcher.group();
            String spanText = group.substring(1, group.length() - 1);
            matcher.appendReplacement(sb, spanText);
            spannable.append(sb.toString());
            int start = spannable.length() - spanText.length();
            final StyleSpan boldSpan = new StyleSpan(android.graphics.Typeface.BOLD);
            spannable.setSpan(new ForegroundColorSpan(Color.parseColor(colorCodeList.get(colorIndex % colorCodeList.size()))), start, spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(boldSpan, start, spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            colorIndex++;
        }

        sb.setLength(0);
        matcher.appendTail(sb);
        spannable.append(sb.toString());
        return spannable;
    }

    private void PlaystoreUpdateDialog() {
        showAppUpdateBanner(false);
    }

    /**
     * Soft: bottom banner with Install + dismiss (X silences via disable_update_menu).
     * Force: Install-only banner (no dismiss). Uses Play flexible in-app update.
     */
    private void PlaystoreUpdateDialog(boolean force) {
        showAppUpdateBanner(force);
    }

    private void bindAppUpdateBanner() {
        View include = findViewById(R.id.includeAppUpdateBanner);
        if (include == null) {
            return;
        }
        layoutAppUpdateBanner = include;
        tvAppUpdateTitle = include.findViewById(R.id.tvAppUpdateTitle);
        tvAppUpdateSubtitle = include.findViewById(R.id.tvAppUpdateSubtitle);
        btnAppUpdateAction = include.findViewById(R.id.btnAppUpdateAction);
        btnAppUpdateDismiss = include.findViewById(R.id.btnAppUpdateDismiss);

        if (btnAppUpdateAction != null) {
            try {
                btnAppUpdateAction.getBackground().setColorFilter(
                        ThemeColorHelper.getThemeColor(this),
                        android.graphics.PorterDuff.Mode.SRC_IN);
            } catch (Exception ignored) {
            }
            btnAppUpdateAction.setOnClickListener(v -> onAppUpdateActionClicked());
        }
        if (btnAppUpdateDismiss != null) {
            btnAppUpdateDismiss.setOnClickListener(v -> dismissSoftAppUpdateBanner());
        }

        playInAppUpdateHelper = new PlayInAppUpdateHelper(this);
        playInAppUpdateHelper.setUpdateLauncher(playAppUpdateLauncher);
        playInAppUpdateHelper.setListener(new PlayInAppUpdateHelper.Listener() {
            @Override
            public void onFlexibleUpdateDownloaded() {
                runOnUiThread(() -> {
                    appUpdateDownloaded = true;
                    appUpdateDownloading = false;
                    refreshAppUpdateActionLabel();
                });
            }

            @Override
            public void onFlexibleUpdateProgress(int percent) {
                runOnUiThread(() -> {
                    appUpdateDownloading = true;
                    if (btnAppUpdateAction != null) {
                        btnAppUpdateAction.setText(getString(R.string.app_update_downloading));
                    }
                });
            }

            @Override
            public void onFlexibleUpdateUnavailable(@Nullable Exception error) {
                runOnUiThread(() -> {
                    appUpdateDownloading = false;
                    refreshAppUpdateActionLabel();
                    PlayInAppUpdateHelper.openPlayStoreListing(HomePage.this);
                });
            }
        });
    }

    private void showAppUpdateBanner(boolean force) {
        appUpdateForceMode = force;
        if (layoutAppUpdateBanner == null) {
            bindAppUpdateBanner();
        }
        if (layoutAppUpdateBanner == null) {
            return;
        }
        layoutAppUpdateBanner.setVisibility(View.VISIBLE);
        if (btnAppUpdateDismiss != null) {
            btnAppUpdateDismiss.setVisibility(force ? View.GONE : View.VISIBLE);
        }
        if (tvAppUpdateSubtitle != null) {
            tvAppUpdateSubtitle.setText(R.string.app_update_available_subtitle);
        }
        refreshAppUpdateActionLabel();
        if (playInAppUpdateHelper != null) {
            playInAppUpdateHelper.resumeIfDownloaded();
        }
    }

    private void hideAppUpdateBanner() {
        if (layoutAppUpdateBanner != null) {
            layoutAppUpdateBanner.setVisibility(View.GONE);
        }
        appUpdateForceMode = false;
        appUpdateDownloaded = false;
        appUpdateDownloading = false;
    }

    private void refreshAppUpdateActionLabel() {
        if (btnAppUpdateAction == null) {
            return;
        }
        if (appUpdateDownloading && !appUpdateDownloaded) {
            btnAppUpdateAction.setText(R.string.app_update_downloading);
        } else {
            // Uber-style primary CTA: Install starts Play flexible update, or completes when ready.
            btnAppUpdateAction.setText(R.string.app_update_install);
        }
    }

    private void onAppUpdateActionClicked() {
        if (playInAppUpdateHelper == null) {
            PlayInAppUpdateHelper.openPlayStoreListing(this);
            return;
        }
        if (appUpdateDownloaded) {
            playInAppUpdateHelper.completeFlexibleUpdate();
            return;
        }
        if (appUpdateDownloading) {
            return;
        }
        appUpdateDownloading = true;
        refreshAppUpdateActionLabel();
        playInAppUpdateHelper.startFlexibleUpdate();
    }

    private void dismissSoftAppUpdateBanner() {
        if (appUpdateForceMode) {
            return;
        }
        hideAppUpdateBanner();
        silenceAppUpdatePrompt();
    }

    /** Soft dismiss: silence forever for this device draft (update_menu_status = 0). */
    private void silenceAppUpdatePrompt() {
        try {
            LanguageSelectionRequest requestModel = new LanguageSelectionRequest();
            requestModel.setDraftId(SharedPrefsData.getInt(this, Constants.DraftID, Constants.PREF_NAME));
            String encrpt_data = encrypt(new com.google.gson.Gson().toJson(requestModel));
            EncritonRequestData encritonRequest = new EncritonRequestData();
            encritonRequest.setData(encrpt_data);
            JsonObject body = new com.google.gson.Gson().toJsonTree(encritonRequest).getAsJsonObject();
            APIService apiService = RestApiClient.getClient().create(APIService.class);
            apiService.disable_update_menu("v3", body).enqueue(new Callback<EncreptionRequest>() {
                @Override
                public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {
                    showLog("disable_update_menu soft silence: " + response.isSuccessful());
                }

                @Override
                public void onFailure(Call<EncreptionRequest> call, Throwable t) {
                    showLog("disable_update_menu failed: " + t);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void movetoappStore() {
        PlayInAppUpdateHelper.openPlayStoreListing(this);
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

}
