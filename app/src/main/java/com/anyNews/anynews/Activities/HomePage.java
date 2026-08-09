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
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.MessageQueue;
import android.os.StrictMode;
import android.os.SystemClock;
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
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
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
import com.anyNews.anynews.Ui.HomeFeedRevealView;
import com.anyNews.anynews.Utils.Allsharedpreference;
import com.anyNews.anynews.Utils.AdMobInitializer;
import com.anyNews.anynews.Utils.VideoShareHelper;
import com.anyNews.anynews.Utils.CommonMethods;
import com.anyNews.anynews.Utils.Constants;
import com.anyNews.anynews.Utils.CricketFooterLabel;
import com.anyNews.anynews.Utils.EngagementCountHelper;
import com.anyNews.anynews.Utils.FooterStyleHelper;
import com.anyNews.anynews.Utils.ProfileHubConfigHelper;
import com.anyNews.anynews.Utils.ScreenCaptureHelper;
import com.anyNews.anynews.Utils.PollOptionStyleHelper;
import com.anyNews.anynews.Utils.ThemeColorHelper;
import com.anyNews.anynews.Utils.PlayInAppUpdateHelper;
import com.anyNews.anynews.Utils.FirebaseAnalyticsJSON;
import com.anyNews.anynews.Utils.FlipperAdapter;
import com.anyNews.anynews.Utils.Logone;
import com.anyNews.anynews.Utils.ModelClass;
import com.anyNews.anynews.Utils.MovementMethodTV;
import com.anyNews.anynews.Utils.MyUtils;
import com.anyNews.anynews.Utils.SharedPrefsData;
import com.anyNews.anynews.Utils.OpenPathCache;
import com.anyNews.anynews.Utils.SelectionListCache;
import com.anyNews.anynews.Utils.OfflineFeedStore;
import com.anyNews.anynews.Utils.HomeModeChromeConfig;
import com.anyNews.anynews.Utils.OfflinePackPrefetcher;
import com.anyNews.anynews.Utils.ArticleOpenLog;
import com.anyNews.anynews.Utils.ReadArticleStore;
import com.anyNews.anynews.Utils.DistrictLabelHelper;
import com.anyNews.anynews.Utils.OfflineActionQueue;
import com.anyNews.anynews.Utils.BookmarkStore;
import com.anyNews.anynews.Utils.NetworkCallDeduper;
import com.anyNews.anynews.Utils.PostDwellTracker;
import com.anyNews.anynews.Utils.ViewsBatchTracker;
import com.anyNews.anynews.Utils.SessionAnalyticsTracker;
import com.anyNews.anynews.Utils.DeviceLocationHelper;
import com.bumptech.glide.Glide;
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
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
    /** Video-tab autoplay: delay before smoothFlipTo next after STATE_ENDED. */
    private static final long VIDEO_AUTOPLAY_CHAIN_DELAY_MS = 520L;
    /** After settle paint, start current video if autoplay ON. */
    private static final long VIDEO_AUTOPLAY_START_DELAY_MS = 320L;
    private Runnable videoAutoplayChainRunnable;
    private Runnable videoAutoplayStartRunnable;
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
    /**
     * True after we observed an offline session on Home; cleared once a restore
     * flush runs (or if cold-start was already online).
     */
    private boolean pendingOfflineToOnlineRestore = false;
    /** Soft-merge feed when network returns while user stays on the current card. */
    private boolean softMergeOnNextPage0 = false;
    /**
     * In-memory feed mode — authoritative for request build + strip filters.
     * SharedPreferences alone races because putBool uses apply() async; News → Special
     * could still enqueue special_posts=false and paint main half posts.
     */
    private boolean specialFeedModeActive = false;
    private boolean videoFeedModeActive = false;
    /**
     * Offline chrome (pill) is active for this session transition. Prefer over live
     * {@link #isDeviceOnline()} for hide-on-tap so mid-session Wi‑Fi loss does not
     * flap toggle off when the OS still briefly reports a transport.
     */
    private boolean offlineChromeMode = false;
    private ConnectivityManager.NetworkCallback offlineNetworkCallback;
    /** Dedup/debounce offline→online transition flush (avoid Wi‑Fi flap storms). */
    private static final long NETWORK_RESTORE_DEBOUNCE_MS = 2000L;
    /** Brief settle so flapping callbacks coalesce before feed hit. */
    private static final long NETWORK_RESTORE_SETTLE_MS = 350L;
    private long lastNetworkRestoreFlushMs = 0L;
    private final Handler networkRestoreHandler = new Handler(Looper.getMainLooper());
    private Runnable pendingNetworkRestoreRunnable;
    private boolean isApiRequested = false;
    /**
     * Bumped on every page-0 feed mode reset (News / Special / Video / Reload).
     * In-flight responses captured with a stale epoch are dropped so main News
     * cannot land into Special after a tab switch.
     */
    private int feedModeEpoch = 0;
    /** True while this activity is between onResume and onPause (for offline pack thrift). */
    private boolean homeActivityResumed = false;
    int positionx=0,requestPostition=1;
    int requestSize=0;
    boolean isFromLocalArticles=false;
    /**
     * When true, bottom-nav select/reselect listeners no-op so programmatic
     * {@code setSelectedItemId} (Special / Reload) does not re-enter feed open.
     */
    private boolean suppressBottomNavCallbacks = false;

    /** Bundle keys for flip position across DayNight recreates (manual night toggle). */
    private static final String STATE_FLIP_INDEX = "home_flip_index";
    private static final String STATE_FLIP_POST_ID = "home_flip_post_id";
    /** True after recreate until we apply (or abandon) restored flip position. */
    private boolean pendingFlipRestore = false;
    private int pendingRestoreFlipIndex = 0;
    private long pendingRestorePostId = -1L;
    /**
     * When Settings toggles night mode, AppCompat would otherwise recreate this heavy
     * activity immediately (UI freeze). Manifest handles uiMode; we recreate once on resume.
     */
    private int appliedNightUiMask = 0;
    private boolean pendingNightModeRecreate = false;
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
    HomeFeedRevealView homeFeedReveal;

    // server skip values list
    long last_post_id = 0;
    long skip = 0;
    long actual_skip = 0;

    // comment post id
    String SHARE_REDIRECT_ID = "", TITLE_REWARDS = "", pushUpdatedID,DATAFROM2G;
    long SHARE_ID;
    /**
     * One extra page-0 after a sticky share miss — List_All_Articles with
     * {@code share_post_id} re-fetches the post by id when the first page did not
     * pin it (e.g. cache race). Cleared with {@link #consumeDeepLinkStickyIds()}.
     */
    private boolean deepLinkShareRescueAttempted = false;

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

    String navigation_home_text = "News";
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

    // Top segmented toggle: offline Offline News|Bookmarks; online main News|Read News.
    private View homeModeToggleRoot;
    private View homeModeThumb;
    private View homeModePill;
    private View homeModeNewsSeg;
    private TextView homeModeNewsLabel;
    private TextView homeModeNewsBadge;
    private TextView homeModeBookmarksLabel;
    private View homeModeBookmarksSeg;
    private TextView homeModeBookmarksBadge;
    /** Secondary overlay: Calendar | Reload (syncs with top mode chrome). */
    private View homeActionChromeRoot;
    private TextView homeActionCalendarLabel;
    private TextView homeActionReloadLabel;
    private View homeBookmarksPanel;
    private RecyclerView homeBookmarksList;
    private TextView homeBookmarksEmpty;
    private TextView homeBookmarksHint;
    /**
     * Right segment selected: offline = Bookmarks panel; online main = Read News flip.
     * Left segment = Offline News / online unread News.
     */
    private boolean bookmarksModeSelected = false;
    /**
     * While online Read flip is showing, keep a snapshot of the unread News list + cursors
     * so switching back to News restores the same flip session.
     */
    private final ArrayList<ModelClass.Datum> savedOnlineNewsList = new ArrayList<>();
    private int savedOnlineNewsPage = 0;
    private long savedOnlineNewsSkip = 0L;
    private long savedOnlineNewsActualSkip = 0L;
    private long savedOnlineNewsLastPostId = 0L;
    private int savedOnlineNewsRequestPosition = 1;
    private int savedOnlineNewsRequestSize = 0;
    private boolean hasSavedOnlineNewsSession = false;
    /** User hid top chrome via content tap (body stays expanded). */
    private boolean homeModeChromeUserHidden = false;
    /**
     * Process-scoped: first-open auto-hide for News|Read chrome already fired or
     * cancelled (user hid first). Survives Home recreation within the same app
     * session; resets on process death (new app open).
     */
    private static boolean homeModeChromeSessionAutoHideConsumed = false;
    /** True while a delayed first-open auto-hide is queued on the main handler. */
    private boolean homeModeChromeAutoHidePending = false;
    private final Handler homeModeChromeAutoHideHandler =
            new Handler(Looper.getMainLooper());
    private final Runnable homeModeChromeAutoHideRunnable =
            this::runHomeModeChromeSessionAutoHide;
    /** After first layout seat, segment switches always animate the orange thumb. */
    private boolean homeModeThumbPlaced = false;
    private Animator homeModeThumbAnimator;
    /** Top + bottom chrome (+ optional chip) slide / fade set. */
    private Animator homeModeChromeAnimator;
    private static final long HOME_MODE_THUMB_ANIM_MS = 300L;
    private static final long HOME_MODE_CHROME_ANIM_MS = 280L;
    private final List<ModelClass.Datum> homeBookmarkItems = new ArrayList<>();
    private HomeBookmarksAdapter homeBookmarksAdapter;
    private int homeBookmarksReloadGen = 0;
    /** Read count badge: poll interval (not every flip). Instant on News|Read toggle. */
    private static final long READ_BADGE_POLL_MS = 30_000L;
    private final Handler readBadgeHandler = new Handler(Looper.getMainLooper());
    private final Runnable readBadgePollRunnable =
            new Runnable() {
                @Override
                public void run() {
                    if (isFinishing()) return;
                    try {
                        if (isOnlineMainNewsChromeActive()) {
                            refreshHomeReadNewsBadge();
                        }
                    } catch (Exception ignored) {
                    }
                    try {
                        readBadgeHandler.postDelayed(this, READ_BADGE_POLL_MS);
                    } catch (Exception ignored) {
                    }
                }
            };
    /**
     * Consecutive server pages skipped because every real article was already read.
     * Caps auto-pagination so we never spin forever.
     */
    private int onlineNewsAllReadSkipStreak = 0;
    /**
     * Max auto-pages while every real was already-read after strip.
     * Continuous unread: walk past today + yesterday + older unreads as long as
     * the catalog has posts (page size ~8 -> 200 pages ~ 1600 depth). Stop earlier
     * on true empty catalog when skip no longer advances (see empty-page path).
     * Was 28 - power users hit empty while older unread still stored.
     */
    private static final int ONLINE_NEWS_ALL_READ_SKIP_CAP = 200;
    /** Prefer at least this many real unread cards before stopping skip-ahead. */
    private static final int ONLINE_NEWS_MIN_UNREAD_CARDS = 4;
    /**
     * Last known ad shell from main feed (codes / ad_req_position). Used to re-place
     * ads after client unread strip when skip-ahead or filter left only one/few slots.
     */
    @androidx.annotation.Nullable
    private ModelClass.Datum onlineNewsAdTemplate = null;
    /**
     * Hard cap so home loading overlay (News/Special/Video skeleton) never stays forever
     * when pages fail or return only unfilled ad shells.
     */
    private static final long HOME_FEED_LOADING_TIMEOUT_MS = 12_000L;
    private final Handler homeFeedLoadingHandler = new Handler(Looper.getMainLooper());
    @androidx.annotation.Nullable
    private Runnable homeFeedLoadingTimeoutRunnable = null;
    /**
     * Admin stickies last seen on main News (server sticky_position). Used so Read flip
     * can re-apply pin indices for stickies that are already in the local read store.
     * Unread stickies stay on News until session view + process commit like other posts.
     */
    private final ArrayList<ModelClass.Datum> rememberedFeedStickies = new ArrayList<>();
    /**
     * Bumps when leaving/entering online Read flip so stale Read postcard paints never
     * overwrite News (and vice-versa snapshot logic can detect session). Does <b>not</b>
     * cancel in-flight {@link #ShowNews_Data_Values} — that uses {@link #feedModeEpoch}
     * only for Special/Video tab changes.
     */
    private int onlineNewsReadSessionEpoch = 0;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Promote previous-session flip views into permanent Read before News binds.
        // Once per process — same-session views stay in News until next process start.
        try {
            ReadArticleStore.commitSessionViewedToReadOncePerProcess(getApplicationContext());
        } catch (Exception ignored) {
        }
        appliedNightUiMask =
                getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        pendingNightModeRecreate = false;
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
                rebalanceFooterTabs();
                FooterStyleHelper.apply(HomePage.this, bottomNavigationView);
                FooterStyleHelper.applyFab(HomePage.this, floatingActionButtonHome);
                // Bulb after rebalance so Material item views exist + weights applied.
                setupSpecialTabBulb();
                repositionCenterFabForVisibleTabs();
            }
        });
        // Safety: rebalance again after first layout measure (menu item GONE needs a pass),
        // then re-attach the Special bulb (rebalance can rebuild item views).
        bottomNavigationView.postDelayed(() -> {
            rebalanceFooterTabs();
            setupSpecialTabBulb();
            repositionCenterFabForVisibleTabs();
        }, 120);
        bottomNavigationView.postDelayed(this::setupSpecialTabBulb, 320);
        bottomNavigationView.postDelayed(this::repositionCenterFabForVisibleTabs, 360);

        setupHomeGlassHeader();
        // Top News|Bookmarks pill — show immediately (esp. offline cold start), default News.
        setupNewsBookmarksToggle();
        bindOfflinePackPrefetcherHost();

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
        homeFeedReveal = findViewById(R.id.home_feed_reveal);

        // Shimmer until warm OpenPath (≤5 min same identity) or live network response.
        // Never treat OfflineFeedStore / multi-hour disk as an online first paint.
        boolean openSpecialSoon = false;
        try {
            if (getIntent() != null && getIntent().hasExtra("from")) {
                String from = getIntent().getStringExtra("from");
                openSpecialSoon = from != null && from.equalsIgnoreCase("special");
            }
        } catch (Exception ignored) {
        }
        boolean expectInstantMainPaint = false;
        if (!openSpecialSoon) {
            try {
                expectInstantMainPaint =
                        OpenPathCache.loadFeedForInstantPaint(this, 0) != null;
            } catch (Exception ignored) {
            }
        }
        if (!expectInstantMainPaint) {
            showHomeFeedLoadingReveal();
            layout_with_data.setVisibility(View.GONE);
        } else {
            // Keep feed container ready; hide skeleton if inflated visible.
            try {
                hideHomeFeedLoadingReveal(true);
            } catch (Exception ignored) {
            }
            layout_with_data.setVisibility(View.VISIBLE);
        }

        // Warm language/state/district pickers so profile hub opens them instantly.
        SelectionListCache.prefetchInBackground(this);
        // Warm reporter profile stats cache for instant profile open.
        try {
            com.anyNews.anynews.Utils.ReporterProfilePrefetcher.prefetchIfPossible(this);
        } catch (Exception ignored) {
        }
        // Warm wallet histories for reporters while network is free (page opens from disk).
        try {
            com.anyNews.anynews.Utils.WalletHistoryPrefetcher.prefetchIfPossible(this);
        } catch (Exception ignored) {
        }

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
        // Older splash still set TITLE=shareId. Prefer share_post_id (do not double-count Open).
        if (SHARE_ID > 0) {
            try {
                if (pushUpdatedID != null) {
                    String raw = pushUpdatedID.trim();
                    if (!raw.isEmpty()
                            && !raw.equalsIgnoreCase("null")
                            && !raw.equals("0")
                            && Long.parseLong(raw) == SHARE_ID) {
                        pushUpdatedID = "0";
                        try {
                            Intent i = getIntent();
                            if (i != null) {
                                i.putExtra("TITLE", "0");
                            }
                        } catch (Exception ignored) {
                        }
                        showLog("demote TITLE→0 when same as SHARE_ID (share deep-link)");
                    }
                }
            } catch (Exception ignored) {
            }
        }
        boolean openAsSpecial = false;
        if (getIntent().hasExtra("from")) {
            showLog("isUnreadPostsSelected set to true #7");
            if (getIntent().getStringExtra("from").equalsIgnoreCase("special")) {
                showLog("isUnreadPostsSelected set to true #8");
                setFeedModeFlags(true, false);
                openAsSpecial = true;
            } else {
                setFeedModeFlags(false, false);
            }
        } else {
            setFeedModeFlags(false, false);
        }
        // Cold open always starts My Feed (not Video News) unless Special deep-link above.

        // if rewards gots then redirect to profile page
        if (TITLE_REWARDS != null && TITLE_REWARDS.contains("rewards")) {

            writeButtonClick("User", type_firebase_button, current_time_firebase, draft_id_firebase, device_id_firebase, button_name_profile);

            Intent intent = new Intent(HomePage.this, newProfileDetails.class);
            if (getApplicationContext() != null && getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)) {
                startActivity(intent);
                overridePendingTransition(R.anim.activity_slide_up_in, R.anim.activity_hold);
            }

            finish();
            overridePendingTransition(R.anim.activity_hold, R.anim.activity_slide_down_out);
        }
        showLog("ONCREATE CALLED");
        // Special must never inherit main-feed skip/cursor or flip-restore mid-state.
        if (openAsSpecial) {
            prepareSpecialFeedFromTop();
        }
        if (sessionId.equals("0") || sessionId.equals("null")) {
            if (!openAsSpecial) {
                isFromLocalArticles=false;
            }
            showLog("inlist from oncreate 1 special=" + openAsSpecial);
            inlist(0);

        } else {
            int parsedSessionId;
            try {
                parsedSessionId = Integer.parseInt(sessionId.trim());
            } catch (NumberFormatException e) {
                parsedSessionId = 0;
            }

            // Do not re-cover the screen here — top-level already chose shimmer vs
            // warm OpenPath paint; inlist may paint warm disk before network.

            if (!openAsSpecial) {
                last_post_id = 0;
                skip = 0;
                actual_skip = 0;
                category_id = parsedSessionId;
                isFromLocalArticles=false;
            } else {
                // Special always category 0 page-0; session category must not override.
                category_id = 0;
            }
            showLog("inlist from oncreate 2:" + category_id + " special=" + openAsSpecial);
            inlist(openAsSpecial ? 0 : parsedSessionId);

            try {

            } catch (AssertionError e) {
                e.printStackTrace();
            }


            un_selected_local.setVisibility(View.VISIBLE);
            un_selected_my_feed.setVisibility(View.GONE);
            selected_local.setVisibility(View.GONE);
            selected_my_feed.setVisibility(View.VISIBLE);

            // Settings → Local Stories: hide local feed entry when user turned it off.
            if (!SharedPrefsData.getBool(HomePage.this, Constants.LOCAL_STORIES, Constants.PREF_NAME, false)) {
                un_selected_local.setVisibility(View.GONE);
                selected_local.setVisibility(View.GONE);
            }


            // Session-path: do not re-show skeleton after inlist may already have painted disk.
            if (list == null || list.isEmpty()) {
                showHomeFeedLoadingReveal();
                layout_with_data.setVisibility(View.GONE);
            }
            comment_page.setVisibility(View.GONE);


        }

        // Migrate legacy USER_ID Long off Selected_Distric_Name before district label reads.
        try {
            SharedPrefsData.getLong(this, Constants.USER_ID, Constants.PREF_NAME);
        } catch (Exception ignored) {
        }

        DistrictLabelHelper.applyTo(HomePage.this, local_distic_name, selelcted_local_distic_name);

        if (local_distic_name.getText().toString().equals("null")) {
            local_distic_name.setVisibility(View.VISIBLE);
            DistrictLabelHelper.applyTo(HomePage.this, local_distic_name);
        } else {
            local_distic_name.setVisibility(View.VISIBLE);
            DistrictLabelHelper.applyTo(HomePage.this, local_distic_name);
        }


        fab1 = findViewById(R.id.fab1);
        fab2 = findViewById(R.id.fab2);
        fab4 = findViewById(R.id.fab4);


        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
        fab_forword = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_forward);
        fab_reverse = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_backward);

        // Scrim / empty area of the create-menu: tap outside actions dismisses + → restore.
        View.OnClickListener dismissCenterPlusOnOutsideTap = v -> closeCenterPlusMenu(true);
        if (post_news_scrren != null) {
            post_news_scrren.setOnClickListener(dismissCenterPlusOnOutsideTap);
        }
        if (floating_action_button != null) {
            floating_action_button.setOnClickListener(dismissCenterPlusOnOutsideTap);
        }

   //     myRef.removeValue();
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // video post
                closeCenterPlusMenu(false);
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
                closeCenterPlusMenu(false);
                Intent intent = new Intent(HomePage.this, FullPost.class);
                if (getApplicationContext() != null && getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)) {
                    startActivity(intent);
                }
            }
        });

        fab4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeCenterPlusMenu(false);
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
        // Seed connectivity so cold online open does not treat first check as "restore".
        try {
            isConnected = isDeviceOnline();
            pendingOfflineToOnlineRestore = !isConnected;
        } catch (Exception ignored) {
            isConnected = false;
            pendingOfflineToOnlineRestore = true;
        }
        registerOfflineConnectivityCallbacks();
        // Apply Offline News chrome early (cold offline open + sticky CONNECTIVITY_ACTION).
        // Ensures full-screen no_internet never sticks over a usable pack.
        try {
            syncNetworkOverlay(false);
        } catch (Exception ignored) {
        }


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

                showHomeFeedLoadingReveal();
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
        // Reload is the already-selected home tab — selection listener does not fire on re-tap.
        bottomNavigationView.setOnNavigationItemReselectedListener(mOnNavigationItemReselectedListener);
        // firebase store
        hashMapUser = new HashMap<>();
        draft_id_firebase = SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME);
        language_id_firebase = SharedPrefsData.getInt(getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME);
        device_id_firebase = SharedPrefsData.getString(getApplicationContext(), Constants.DeviceID, Constants.PREF_NAME);





    }
    /** Process-once guard so feed cache refreshes cannot re-ask in the same session. */
    private boolean notificationRepromptHandledThisProcess = false;
    /** Process-once for post-install/update permission (independent of admin wave). */
    private boolean installNotificationPermHandledThisProcess = false;
    /** Request code for install + re-prompt POST_NOTIFICATIONS (ActivityCompat). */
    private static final int REQ_POST_NOTIFICATIONS = 2901;
    /** Pending result of {@link #requestNotificationPermission(boolean)}. */
    private boolean pendingNotifPermViaAdminReprompt = false;

    private boolean isSystemNotificationsEnabledSafe() {
        try {
            return NotificationManagerCompat.from(this).areNotificationsEnabled();
        } catch (Exception e) {
            return true;
        }
    }

    private boolean hasPostNotificationsPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true;
        }
        return checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasEverAskedPostNotifications() {
        if (SharedPrefsData.getBool(
                getApplicationContext(),
                Constants.NOTIFICATION_RUNTIME_EVER_ASKED,
                Constants.PREF_NAME,
                false)) {
            return true;
        }
        return SharedPrefsData.getInt(
                getApplicationContext(),
                Constants.NOTIFICATION_RUNTIME_ASK_VERSION,
                Constants.PREF_NAME) > 0;
    }

    private void markEverAskedPostNotifications() {
        SharedPrefsData.putBool(
                getApplicationContext(),
                Constants.NOTIFICATION_RUNTIME_EVER_ASKED,
                true,
                Constants.PREF_NAME);
        markNotificationRuntimeAskedVersion();
    }

    private boolean isAdminNotificationRepromptDue() {
        boolean masterOn = SharedPrefsData.getBool(
                getApplicationContext(),
                Constants.NOTIFICATION_REPROMPT_ENABLED,
                Constants.PREF_NAME,
                false);
        boolean due = SharedPrefsData.getBool(
                getApplicationContext(),
                Constants.NOTIFICATION_REPROMPT_DUE,
                Constants.PREF_NAME,
                false);
        return masterOn && due;
    }

    /**
     * Home open: (1) admin re-prompt when due, (2) first open after install/update
     * for POST_NOTIFICATIONS. Location soft-ask remains {@link #maybeSoftRequestDraftGpsPermission}.
     */
    private void maybeRequestInstallAndRepromptPermissions() {
        if (isFinishing() || isDestroyed()) return;
        if (isAdminNotificationRepromptDue()) {
            showNotificationAlert(false);
            return;
        }
        maybeRequestInstallNotificationPermission();
    }

    /**
     * Once per versionCode if POST_NOTIFICATIONS not granted. Opt-in default true after grant.
     */
    private void maybeRequestInstallNotificationPermission() {
        if (installNotificationPermHandledThisProcess || notificationRepromptHandledThisProcess) {
            return;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            // Pre-13: ensure opt-in pref tracks system tray when not user-disabled.
            ensureNotificationPrefAlignedWithSystem(false);
            return;
        }
        if (hasPostNotificationsPermission()) {
            ensureNotificationPrefAlignedWithSystem(false);
            markNotificationRuntimeAskedVersion();
            return;
        }
        int askedVersion = SharedPrefsData.getInt(
                getApplicationContext(),
                Constants.NOTIFICATION_RUNTIME_ASK_VERSION,
                Constants.PREF_NAME);
        if (askedVersion >= BuildConfig.VERSION_CODE) {
            return;
        }
        installNotificationPermHandledThisProcess = true;
        getWindow().getDecorView().postDelayed(() -> {
            if (isFinishing() || isDestroyed()) return;
            if (hasPostNotificationsPermission()) {
                ensureNotificationPrefAlignedWithSystem(false);
                markNotificationRuntimeAskedVersion();
                return;
            }
            requestNotificationPermission(false /* force / viaAdminReprompt */);
        }, 450L);
    }

    private void markNotificationRuntimeAskedVersion() {
        SharedPrefsData.putInt(
                getApplicationContext(),
                Constants.NOTIFICATION_RUNTIME_ASK_VERSION,
                BuildConfig.VERSION_CODE,
                Constants.PREF_NAME);
    }

    /**
     * After OS grant: keep pref ON and sync server. Do not force OFF before user is asked.
     */
    private void ensureNotificationPrefAlignedWithSystem(boolean viaAdminReprompt) {
        boolean systemOn = isSystemNotificationsEnabledSafe() && hasPostNotificationsPermission();
        if (!systemOn) {
            return;
        }
        SharedPrefsData.putBool(
                getApplicationContext(),
                Constants.NOTIFICATION_ENABLED,
                true,
                Constants.PREF_NAME);
        SharedPrefsData.putBool(
                getApplicationContext(),
                Constants.switchNotification,
                true,
                Constants.PREF_NAME);
        syncNotificationFlagAfterReprompt(true, viaAdminReprompt);
    }

    /**
     * Shared install + admin re-prompt path. Uses the same ActivityCompat OS dialog as
     * first install when the system can still show it (never asked or
     * shouldShowRequestPermissionRationale). force=true for admin wave: permanent denial
     * opens app notification settings once.
     */
    private void requestNotificationPermission(boolean force) {
        requestPostNotificationsPermission(force);
    }

    private void requestPostNotificationsPermission(final boolean viaAdminReprompt) {
        pendingNotifPermViaAdminReprompt = viaAdminReprompt;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            if (viaAdminReprompt && !isSystemNotificationsEnabledSafe()) {
                openAppNotificationSettingsForReprompt();
            }
            return;
        }

        if (hasPostNotificationsPermission()) {
            if (!isSystemNotificationsEnabledSafe()) {
                // Runtime granted but tray master off — settings only.
                if (viaAdminReprompt) {
                    openAppNotificationSettingsForReprompt();
                }
                return;
            }
            onNotificationPermissionGrantedFromReprompt(viaAdminReprompt);
            return;
        }

        boolean canAskAgain = ActivityCompat.shouldShowRequestPermissionRationale(
                this, Manifest.permission.POST_NOTIFICATIONS);
        boolean neverAsked = !hasEverAskedPostNotifications();

        // Same as install: ActivityCompat OS dialog whenever still allowed.
        if (neverAsked || canAskAgain) {
            markEverAskedPostNotifications();
            try {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQ_POST_NOTIFICATIONS);
            } catch (Exception e) {
                showLog("request POST_NOTIFICATIONS failed: " + e.getMessage());
                if (viaAdminReprompt) {
                    openAppNotificationSettingsForReprompt();
                }
            }
            return;
        }

        // Permanent deny: OS will not re-show the system dialog.
        SharedPrefsData.putBool(
                getApplicationContext(),
                Constants.NOTIFICATION_ENABLED,
                false,
                Constants.PREF_NAME);
        if (viaAdminReprompt) {
            openAppNotificationSettingsForReprompt();
        }
    }

    private void handlePostNotificationsPermissionResult(boolean granted) {
        final boolean viaAdmin = pendingNotifPermViaAdminReprompt;
        pendingNotifPermViaAdminReprompt = false;
        if (granted) {
            onNotificationPermissionGrantedFromReprompt(viaAdmin);
            return;
        }
        SharedPrefsData.putBool(
                getApplicationContext(),
                Constants.NOTIFICATION_ENABLED,
                false,
                Constants.PREF_NAME);
        if (viaAdmin) {
            // Permanent denial only — install-style ask already used; settings is last resort.
            boolean canAskAgain = ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.POST_NOTIFICATIONS);
            if (!canAskAgain) {
                openAppNotificationSettingsForReprompt();
            }
        }
    }

    private void openAppNotificationSettingsForReprompt() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Intent intent = new Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                intent.putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, getPackageName());
                startActivity(intent);
            } else {
                Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        } catch (Exception ignored) {
        }
    }

    public void showNotificationAlert(boolean showPopup){
        boolean masterOn = SharedPrefsData.getBool(
                getApplicationContext(),
                Constants.NOTIFICATION_REPROMPT_ENABLED,
                Constants.PREF_NAME,
                false);
        boolean due = SharedPrefsData.getBool(
                getApplicationContext(),
                Constants.NOTIFICATION_REPROMPT_DUE,
                Constants.PREF_NAME,
                false);
        // Admin wave (due) takes priority; legacy daily flag still works when showPopup true.
        boolean shouldConsider = (masterOn && due) || showPopup;
        if (!shouldConsider) {
            return;
        }
        // Once per process — do not re-ask on every feed refresh.
        if (notificationRepromptHandledThisProcess) {
            return;
        }

        boolean systemOn = isSystemNotificationsEnabledSafe();
        boolean prefOn = SharedPrefsData.getBool(
                getApplicationContext(),
                Constants.NOTIFICATION_ENABLED,
                Constants.PREF_NAME,
                true);
        boolean fullyOn = systemOn && prefOn && hasPostNotificationsPermission();
        final boolean isAdminDue = masterOn && due;

        // Fully enabled: clear due without dialog (only once for this wave).
        if (fullyOn) {
            notificationRepromptHandledThisProcess = true;
            installNotificationPermHandledThisProcess = true;
            markNotificationRuntimeAskedVersion();
            if (isAdminDue) {
                clearNotificationRepromptDueLocal();
                ackNotificationReprompt(false, true, true);
            }
            ensureNotificationPrefAlignedWithSystem(isAdminDue);
            return;
        }

        notificationRepromptHandledThisProcess = true;
        installNotificationPermHandledThisProcess = true;
        // Ack immediately so same wave never re-asks after this open (even if user denies).
        if (isAdminDue) {
            clearNotificationRepromptDueLocal();
            ackNotificationReprompt(false, false, false);
        }

        // Admin force: same install-style OS dialog when can ask; settings if permanent deny.
        requestNotificationPermission(isAdminDue || showPopup);
    }

    private void clearNotificationRepromptDueLocal() {
        SharedPrefsData.putBool(
                getApplicationContext(),
                Constants.NOTIFICATION_REPROMPT_DUE,
                false,
                Constants.PREF_NAME);
    }

    private void onNotificationPermissionGrantedFromReprompt(boolean viaAdminReprompt) {
        SharedPrefsData.putBool(
                getApplicationContext(),
                Constants.NOTIFICATION_ENABLED,
                true,
                Constants.PREF_NAME);
        SharedPrefsData.putBool(
                getApplicationContext(),
                Constants.switchNotification,
                true,
                Constants.PREF_NAME);
        if (viaAdminReprompt) {
            ackNotificationReprompt(true, false, true);
        }
        // Sync app opt-in flag (also marks converted when due/shown on server).
        syncNotificationFlagAfterReprompt(true, viaAdminReprompt);
    }

    private void ackNotificationReprompt(boolean converted, boolean alreadyEnabled, boolean notificationFlagOn) {
        try {
            int draftId = SharedPrefsData.getInt(
                    getApplicationContext(), Constants.DraftID, Constants.PREF_NAME);
            if (draftId <= 0) return;
            LanguageSelectionRequest requestModel = new LanguageSelectionRequest();
            requestModel.setDraftId(draftId);
            requestModel.setConverted(converted);
            requestModel.setAlreadyEnabled(alreadyEnabled);
            if (notificationFlagOn || converted) {
                requestModel.setNotificationFlag(true);
            }
            String jsonString = new Gson().toJson(requestModel);
            String encrpt_data = encrypt(jsonString);
            EncritonRequestData body = new EncritonRequestData();
            body.setData(encrpt_data);
            JsonObject payload = new Gson().toJsonTree(body).getAsJsonObject();
            APIService apiService = RestApiClient.getClient().create(APIService.class);
            apiService.ack_notification_reprompt("v3", payload).enqueue(new Callback<EncreptionRequest>() {
                @Override
                public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {
                }

                @Override
                public void onFailure(Call<EncreptionRequest> call, Throwable t) {
                }
            });
        } catch (Exception e) {
            showLog("ack reprompt failed: " + e.getMessage());
        }
    }

    private void syncNotificationFlagAfterReprompt(boolean enabled, boolean viaReprompt) {
        try {
            LanguageSelectionRequest requestModel = new LanguageSelectionRequest();
            requestModel.setDraftId(
                    SharedPrefsData.getInt(
                            getApplicationContext(), Constants.DraftID, Constants.PREF_NAME));
            requestModel.setProperty("notification_flag");
            requestModel.setValue(enabled);
            if (viaReprompt) {
                requestModel.setViaReprompt(true);
            }
            String jsonString = new Gson().toJson(requestModel);
            String encrpt_data = encrypt(jsonString);
            EncritonRequestData body = new EncritonRequestData();
            body.setData(encrpt_data);
            JsonObject payload = new Gson().toJsonTree(body).getAsJsonObject();
            APIService apiService = RestApiClient.getClient().create(APIService.class);
            apiService.update_settings("v3", payload).enqueue(new Callback<EncreptionRequest>() {
                @Override
                public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {
                }

                @Override
                public void onFailure(Call<EncreptionRequest> call, Throwable t) {
                }
            });
        } catch (Exception e) {
            showLog("update notif after reprompt failed: " + e.getMessage());
        }
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
        if (ad_code == null || ad_code.trim().isEmpty() || ad_req_position == null) {
            return;
        }
        if (ad_type == null || ad_type.trim().isEmpty()) {
            ad_type = "google";
        }
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

    /**
     * Public for FlipperAdapter ad shells: load a native creative for a concrete
     * slot. Avoids permanent share fallback. Does not re-fire while a request for
     * this position is already in-flight (tracked by {@link #nativeGoogleAdArray}).
     */
    public void ensureFeedNativeAdLoaded(
            @Nullable String adCode,
            @Nullable Integer adReqPosition,
            @Nullable String adType) {
        if (adCode == null || adCode.trim().isEmpty() || adReqPosition == null) {
            // No unit: FlipperAdapter will timeout-skip this shell; do not hang.
            showLog("ensureFeedNativeAdLoaded skip empty code/pos code="
                    + adCode + " pos=" + adReqPosition);
            return;
        }
        try {
            if (nativeGoogleAdList != null) {
                for (int i = 0; i < nativeGoogleAdList.size(); i++) {
                    FlipGoogleNativeAds packed = nativeGoogleAdList.get(i);
                    if (packed != null
                            && packed.getReqPos() == adReqPosition
                            && packed.getAd() != null) {
                        return;
                    }
                }
            }
            // Already requested this position this session — wait for fill/fail.
            if (nativeGoogleAdArray != null && nativeGoogleAdArray.contains(adReqPosition)) {
                return;
            }
            loadAds(adCode, adReqPosition, adType != null ? adType : "google");
        } catch (Exception e) {
            showLog("ensureFeedNativeAdLoaded: " + e.getMessage());
        }
    }

    /**
     * FlipperAdapter: ad seat still has no native creative after the soft fill
     * timeout (Share UI was already shown as shell). Mutate seat to {@code share_app}
     * and rebind in place. Hard failures leave share-shell UI but keep the seat as
     * {@code ad} so a late fill can still replace it.
     */
    public void onUnfilledAdShellTimedOut(int listPosition) {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                convertUnfilledAdShellToShareApp(listPosition);
            }
        });
    }

    private boolean hasLoadedNativeForSlot(@Nullable ModelClass.Datum slot) {
        if (slot == null) {
            return false;
        }
        Integer slotPos = slot.getAd_req_position();
        try {
            if (nativeGoogleAdList != null) {
                boolean anyGoogle = false;
                for (int i = 0; i < nativeGoogleAdList.size(); i++) {
                    FlipGoogleNativeAds packed = nativeGoogleAdList.get(i);
                    if (packed == null || packed.getAd() == null) {
                        continue;
                    }
                    anyGoogle = true;
                    if (slotPos != null && packed.getReqPos() == slotPos) {
                        return true;
                    }
                }
                // Soft-match: FlipperAdapter paints any loaded Google native on rebalance drift.
                if (anyGoogle) {
                    return true;
                }
            }
            if (nativeFBAdList != null) {
                for (int i = 0; i < nativeFBAdList.size(); i++) {
                    FlipGoogleNativeAds packed = nativeFBAdList.get(i);
                    if (packed == null || packed.getFbAd() == null) {
                        continue;
                    }
                    if (slotPos == null || packed.getReqPos() == slotPos) {
                        return true;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    /**
     * After true fail timeout: keep list index, turn unfilled ad shell into share_app
     * so Flipper rebinds Share layout at the same flip position (user can flip past).
     * Adjacent unfilled shells are also converted to avoid spinner→spinner hops.
     */
    private void convertUnfilledAdShellToShareApp(int listPosition) {
        if (list == null || list.isEmpty()) {
            resolveSoftEmptyNewsFeed("ad_timeout_empty_list");
            return;
        }
        if (listPosition < 0 || listPosition >= list.size()) {
            return;
        }
        ModelClass.Datum d = list.get(listPosition);
        if (d == null || !ReadArticleStore.isAdFeedSlot(d)) {
            return;
        }
        if (hasLoadedNativeForSlot(d)) {
            // Race: fill arrived after timeout posted — rebind spinner page as ad.
            try {
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            } catch (Exception ignored) {
            }
            return;
        }
        showLog("convert unfilled ad shell → share_app at pos=" + listPosition
                + " reqPos=" + d.getAd_req_position()
                + " code=" + d.getAd_code());
        mutateAdShellToShareApp(d);
        // Chain-convert adjacent still-empty ads so flip next is not another white spin.
        int probe = listPosition + 1;
        while (probe < list.size()) {
            ModelClass.Datum next = list.get(probe);
            if (next == null || !ReadArticleStore.isAdFeedSlot(next)) {
                break;
            }
            if (hasLoadedNativeForSlot(next)) {
                break;
            }
            showLog("chain-convert adjacent unfilled ad → share_app at pos=" + probe);
            mutateAdShellToShareApp(next);
            probe++;
        }
        // Also convert empty neighbors immediately before (rare clump after rebalance).
        probe = listPosition - 1;
        while (probe >= 0) {
            ModelClass.Datum prev = list.get(probe);
            if (prev == null || !ReadArticleStore.isAdFeedSlot(prev)) {
                break;
            }
            if (hasLoadedNativeForSlot(prev)) {
                break;
            }
            showLog("chain-convert adjacent unfilled ad → share_app at pos=" + probe);
            mutateAdShellToShareApp(prev);
            probe--;
        }
        positionx = Math.max(0, Math.min(listPosition, list.size() - 1));
        try {
            ensureHomeFlipAdapterBoundAndData(list);
            if (mFlipView != null && !list.isEmpty()) {
                mFlipView.setVisibility(View.VISIBLE);
                if (layout_with_data != null) {
                    layout_with_data.setVisibility(View.VISIBLE);
                }
                if (txtNoData != null) {
                    txtNoData.setVisibility(View.GONE);
                }
                hideHomeFeedLoadingReveal(true);
                final int flipTo = positionx;
                if (adapter != null) {
                    // Clear deferred native binds for demoted seats (current ± adjacent).
                    for (int i = Math.max(0, listPosition - 2);
                         i < Math.min(list.size(), listPosition + 3); i++) {
                        adapter.clearPendingNativeAdBind(i);
                    }
                    adapter.setCurrentFlipPage(flipTo);
                }
                // Re-seat same index so getView rebinds share_app (not cached ad spinner).
                mFlipView.flipTo(flipTo);
            }
        } catch (Exception e) {
            showLog("convertUnfilledAdShellToShareApp bind: " + e.getMessage());
        }
        try {
            maybePrefetchOnlineNewsIfNearEnd();
        } catch (Exception ignored) {
        }
    }

    /** In-place demote of a synthetic ad row to the existing share promo page. */
    private void mutateAdShellToShareApp(@Nullable ModelClass.Datum slot) {
        if (slot == null || !ReadArticleStore.isAdFeedSlot(slot)) {
            return;
        }
        try {
            slot.setPostType("share_app");
        } catch (Exception ignored) {
        }
    }

    /**
     * Drop an unfilled ad at {@code listPosition} and flip to the same index
     * (now the next card) or the first remaining content card.
     * Prefer {@link #convertUnfilledAdShellToShareApp} for user-visible timeout paths.
     */
    private void skipUnfilledAdShellAt(int listPosition) {
        if (list == null || list.isEmpty()) {
            resolveSoftEmptyNewsFeed("ad_skip_empty_list");
            return;
        }
        if (listPosition < 0 || listPosition >= list.size()) {
            return;
        }
        ModelClass.Datum d = list.get(listPosition);
        if (d == null || !ReadArticleStore.isAdFeedSlot(d)) {
            return;
        }
        if (hasLoadedNativeForSlot(d)) {
            // Race: fill arrived after timeout posted — rebind spinner page.
            try {
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            } catch (Exception ignored) {
            }
            return;
        }
        showLog("skip unfilled ad shell at pos=" + listPosition
                + " reqPos=" + d.getAd_req_position()
                + " code=" + d.getAd_code());
        try {
            list.remove(listPosition);
        } catch (Exception e) {
            return;
        }
        // Chain-remove adjacent unfilled ads so we don't hop spinner→spinner.
        while (listPosition < list.size()) {
            ModelClass.Datum next = list.get(listPosition);
            if (next == null || !ReadArticleStore.isAdFeedSlot(next)) {
                break;
            }
            if (hasLoadedNativeForSlot(next)) {
                break;
            }
            showLog("chain-skip adjacent unfilled ad at pos=" + listPosition);
            list.remove(listPosition);
        }
        int paintables = ReadArticleStore.countPaintableArticles(list);
        if (list.isEmpty() || paintables == 0) {
            // Only shells/spinners left — keep paging or show empty; never hang.
            try {
                ensureHomeFlipAdapterBoundAndData(list);
            } catch (Exception ignored) {
            }
            resolveSoftEmptyNewsFeed("ad_skip_no_paintables");
            return;
        }
        int target = Math.min(listPosition, list.size() - 1);
        // Prefer landing on a non-ad if current was stripped mid-open.
        if (target >= 0 && target < list.size()
                && ReadArticleStore.isAdFeedSlot(list.get(target))
                && !hasLoadedNativeForSlot(list.get(target))) {
            int better = indexOfFirstPaintableOrFilledCard(list);
            if (better >= 0) {
                target = better;
            }
        }
        positionx = Math.max(0, target);
        try {
            ensureHomeFlipAdapterBoundAndData(list);
            if (mFlipView != null && !list.isEmpty()) {
                mFlipView.setVisibility(View.VISIBLE);
                if (layout_with_data != null) {
                    layout_with_data.setVisibility(View.VISIBLE);
                }
                if (txtNoData != null) {
                    txtNoData.setVisibility(View.GONE);
                }
                hideHomeFeedLoadingReveal(true);
                final int flipTo = positionx;
                if (adapter != null) {
                    adapter.setCurrentFlipPage(flipTo);
                }
                mFlipView.flipTo(flipTo);
            }
        } catch (Exception e) {
            showLog("skipUnfilledAdShellAt bind: " + e.getMessage());
        }
        try {
            maybePrefetchOnlineNewsIfNearEnd();
        } catch (Exception ignored) {
        }
    }

    /** First real/sticky card, or first ad that already has a creative. */
    private int indexOfFirstPaintableOrFilledCard(
            @Nullable java.util.List<ModelClass.Datum> items) {
        if (items == null || items.isEmpty()) {
            return -1;
        }
        for (int i = 0; i < items.size(); i++) {
            ModelClass.Datum d = items.get(i);
            if (d == null) {
                continue;
            }
            if (ReadArticleStore.isAdFeedSlot(d)) {
                if (hasLoadedNativeForSlot(d)) {
                    return i;
                }
                continue;
            }
            if (ReadArticleStore.isShareFeedSlot(d)) {
                return i;
            }
            if (d.getPostId() != null && d.getPostId() > 0) {
                return i;
            }
        }
        return items.isEmpty() ? -1 : 0;
    }

    /**
     * Mid skip-ahead / strip left no real cards: keep fetching next pages, or
     * surface offline/empty — never leave News/Special/Video on loading or an
     * unfilled ad spinner forever.
     */
    private void resolveSoftEmptyNewsFeed(String reason) {
        showLog("resolveSoftEmptyNewsFeed: " + reason
                + " streak=" + onlineNewsAllReadSkipStreak
                + " list=" + (list != null ? list.size() : -1)
                + " video=" + isVideoFeedMode()
                + " special=" + isSpecialFeedMode());
        try {
            if (canPageActiveOnlineFlipFeed()
                    && !bookmarksModeSelected
                    && onlineNewsAllReadSkipStreak < ONLINE_NEWS_ALL_READ_SKIP_CAP
                    && isDeviceOnline()) {
                if (txtNoData != null) {
                    txtNoData.setVisibility(View.GONE);
                }
                showHomeFeedLoadingReveal();
                // Clear hung in-flight flag so skip-ahead / timeout can re-request.
                isApiRequested = false;
                onlineNewsAllReadSkipStreak++;
                isApiRequested = true;
                final boolean videoPage = isVideoFeedMode();
                final boolean specialPage = isSpecialFeedMode();
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (isFinishing() || isDestroyed()) {
                            isApiRequested = false;
                            return;
                        }
                        // Mode may have changed while the post was pending.
                        if (!canPageActiveOnlineFlipFeed()
                                || bookmarksModeSelected
                                || videoPage != isVideoFeedMode()
                                || specialPage != isSpecialFeedMode()) {
                            isApiRequested = false;
                            finishEmptyNewsFeedUi();
                            return;
                        }
                        // Page-0 style replace when list is empty soft-empty; append when
                        // we already have shells / partial list (Video filtered empty etc.).
                        int req = (list == null || list.isEmpty()
                                || ReadArticleStore.countPaintableArticles(list) == 0)
                                ? 1 : 3;
                        ShowNews_Data_Values(category_id, req);
                    }
                });
                return;
            }
        } catch (Exception ignored) {
        }
        finishEmptyNewsFeedUi();
    }

    private void finishEmptyNewsFeedUi() {
        try {
            hideHomeFeedLoadingReveal(true);
            // Demote pure unfilled ad shells to share_app so FlipView is not a lone spinner.
            if (list != null) {
                for (int i = list.size() - 1; i >= 0; i--) {
                    ModelClass.Datum d = list.get(i);
                    if (d != null && ReadArticleStore.isAdFeedSlot(d)
                            && !hasLoadedNativeForSlot(d)) {
                        mutateAdShellToShareApp(d);
                    }
                }
            }
            if (list != null && !list.isEmpty()) {
                ensureHomeFlipAdapterBoundAndData(list);
                if (mFlipView != null) {
                    mFlipView.setVisibility(View.VISIBLE);
                }
                if (layout_with_data != null) {
                    layout_with_data.setVisibility(View.VISIBLE);
                }
                if (txtNoData != null) {
                    txtNoData.setVisibility(View.GONE);
                }
                try {
                    seatAwayFromUnfilledAdIfNeeded();
                } catch (Exception ignored) {
                }
                return;
            }
            if (mFlipView != null) {
                mFlipView.setVisibility(View.GONE);
            }
            if (layout_with_data != null) {
                layout_with_data.setVisibility(View.VISIBLE);
            }
            if (txtNoData != null) {
                txtNoData.setVisibility(View.VISIBLE);
            }
            initialCall = true;
        } catch (Exception ignored) {
        }
    }

    /** Rebind ad page after a creative arrives so spinner shells paint the native. */
    private void maybeRebindCurrentAdAfterLoad(Integer ad_req_position) {
        try {
            if (adapter == null || mFlipView == null || list == null || list.isEmpty()) {
                return;
            }
            int cur = positionx;
            try {
                cur = mFlipView.getCurrentPage();
            } catch (Exception ignored) {
            }
            if (cur < 0 || cur >= list.size()) return;
            ModelClass.Datum d = list.get(cur);
            if (d == null || d.getPostType() == null
                    || !d.getPostType().equalsIgnoreCase("ad")) {
                return;
            }
            Integer slotPos = d.getAd_req_position();
            // Rebind if this creative is for the on-screen slot, or slot has no match yet.
            if (ad_req_position != null && slotPos != null
                    && !ad_req_position.equals(slotPos)) {
                // Still rebind soft-match path (any creative usable).
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (adapter != null) {
                            adapter.notifyDataSetChanged();
                        }
                    } catch (Exception ignored) {
                    }
                }
            });
        } catch (Exception ignored) {
        }
    }
    private void scheduleSkipCurrentAdIfStillUnfilled(
            @Nullable Integer adReqPosition, long delayMs) {
        if (adReqPosition == null) {
            return;
        }
        // Instant (or caller delay) demote when load failed / still empty — no 2.5s/10s floor.
        final long wait = Math.max(0L, delayMs);
        Runnable convertIfStillUnfilled = new Runnable() {
            @Override
            public void run() {
                try {
                    if (isFinishing() || isDestroyed() || list == null || list.isEmpty()) {
                        return;
                    }
                    int cur = positionx;
                    try {
                        if (mFlipView != null) {
                            cur = mFlipView.getCurrentPage();
                        }
                    } catch (Exception ignored) {
                    }
                    if (cur < 0 || cur >= list.size()) {
                        return;
                    }
                    ModelClass.Datum d = list.get(cur);
                    if (d == null || !ReadArticleStore.isAdFeedSlot(d)) {
                        return;
                    }
                    Integer slotPos = d.getAd_req_position();
                    if (slotPos != null && !adReqPosition.equals(slotPos)) {
                        return;
                    }
                    if (hasLoadedNativeForSlot(d)) {
                        return;
                    }
                    showLog("ad load fail — convert unfilled shell to share_app at flip="
                            + cur + " reqPos=" + adReqPosition);
                    convertUnfilledAdShellToShareApp(cur);
                } catch (Exception ignored) {
                }
            }
        };
        if (wait <= 0L) {
            // Failure path: demote immediately on next UI frame (Share already shown as shell).
            new Handler(Looper.getMainLooper()).post(convertIfStillUnfilled);
        } else {
            new Handler(Looper.getMainLooper()).postDelayed(convertIfStillUnfilled, wait);
        }
    }

    private void loadGoogleAds(String adCode, Integer ad_req_position,int reqType) {
        //   adCode="/21833905170,23071623886/Anynews_New_Native";
         //    adCode="/23071623886/AnyNews_2026";
                adCode="ca-app-pub-8562336752221105/4901277505";

        // Mark in-flight immediately so Flipper ensure/getView does not multi-fire.
        if (ad_req_position != null && nativeGoogleAdArray != null
                && !nativeGoogleAdArray.contains(ad_req_position)) {
            nativeGoogleAdArray.add(ad_req_position);
        }

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
                            // Allow a later ensureFeedNativeAdLoaded / settle to retry.
                            if (nativeGoogleAdArray != null && ad_req_position != null) {
                                nativeGoogleAdArray.remove(ad_req_position);
                            }

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

                            // Fail = no creative for this request. Flipper already mounts Share UI
                            // as the ad shell (not a 10s white spinner). Keep seat as "ad" so a
                            // later ensureFeedNativeAdLoaded / retry fill can still rebind.
                            // Soft AD_SHELL_FILL_TIMEOUT only demotes type if fill never arrives.
                            if (adapter != null) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            maybeRebindCurrentAdAfterLoad(ad_req_position);
                                        } catch (Exception ignored) {
                                        }
                                    }
                                });
                            }

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
                                maybeRebindCurrentAdAfterLoad(ad_req_position);
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

    /**
     * FlipperAdapter: Video News tab + settings autoplay — current video finished.
     * Smooth-flip to next after a short delay so the user can perceive end → next.
     * No wrap at last item.
     */
    @Override
    public void onVideoAutoplayEndedRequestNext(int endedPosition) {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        if (!SharedPrefsData.getBool(this, Constants.isVideoPostsSelected, Constants.PREF_NAME)) {
            return;
        }
        if (!FlipperAdapter.isVideoAutoplayEnabled(this)) {
            return;
        }
        if (list == null || endedPosition < 0) {
            return;
        }
        final int next = endedPosition + 1;
        if (next >= list.size()) {
            showLog("video autoplay chain stop at last item pos=" + endedPosition);
            return;
        }
        cancelVideoAutoplayChain();
        videoAutoplayChainRunnable = () -> {
            videoAutoplayChainRunnable = null;
            if (isFinishing() || isDestroyed()) {
                return;
            }
            // User already left this page (manual flip) — do not force another jump.
            if (positionx != endedPosition) {
                return;
            }
            if (mFlipView == null || mFlipView.mIsFlipping) {
                return;
            }
            if (!SharedPrefsData.getBool(this, Constants.isVideoPostsSelected, Constants.PREF_NAME)
                    || !FlipperAdapter.isVideoAutoplayEnabled(this)) {
                return;
            }
            try {
                if (next < 0 || next >= mFlipView.getPageCount()) {
                    return;
                }
                showLog("video autoplay chain smoothFlipTo " + next);
                mFlipView.smoothFlipTo(next);
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        flipSettleHandler.postDelayed(videoAutoplayChainRunnable, VIDEO_AUTOPLAY_CHAIN_DELAY_MS);
    }

    private void cancelVideoAutoplayChain() {
        if (videoAutoplayChainRunnable != null) {
            flipSettleHandler.removeCallbacks(videoAutoplayChainRunnable);
            videoAutoplayChainRunnable = null;
        }
    }

    private void cancelVideoAutoplayStart() {
        if (videoAutoplayStartRunnable != null) {
            flipSettleHandler.removeCallbacks(videoAutoplayStartRunnable);
            if (mFlipView != null) {
                mFlipView.removeCallbacks(videoAutoplayStartRunnable);
            }
            videoAutoplayStartRunnable = null;
        }
    }

    /**
     * After a Video-tab page settles with settings autoplay ON, start the current card.
     * Skipped when autoplay is OFF or the user paused (until next flip).
     */
    private void scheduleVideoFooterAutoplayStart(final int settlePos) {
        cancelVideoAutoplayStart();
        if (!SharedPrefsData.getBool(this, Constants.isVideoPostsSelected, Constants.PREF_NAME)) {
            return;
        }
        if (!FlipperAdapter.isVideoAutoplayEnabled(this)) {
            return;
        }
        if (list == null || settlePos < 0 || settlePos >= list.size()) {
            return;
        }
        String type = list.get(settlePos).getPostType();
        if (type == null
                || (!"normal_video".equalsIgnoreCase(type)
                && !"full_video".equalsIgnoreCase(type))) {
            return;
        }
        videoAutoplayStartRunnable = () -> {
            videoAutoplayStartRunnable = null;
            if (isFinishing() || isDestroyed()) {
                return;
            }
            if (positionx != settlePos) {
                return;
            }
            if (!SharedPrefsData.getBool(this, Constants.isVideoPostsSelected, Constants.PREF_NAME)
                    || !FlipperAdapter.isVideoAutoplayEnabled(this)) {
                return;
            }
            if (adapter == null || mFlipView == null || mFlipView.mIsFlipping) {
                return;
            }
            try {
                View flipChild = getFlipChildForShare();
                adapter.maybeAutoplayVisibleVideo(flipChild, settlePos);
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        flipSettleHandler.postDelayed(videoAutoplayStartRunnable, VIDEO_AUTOPLAY_START_DELAY_MS);
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
                showHomeFeedLoadingReveal();
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
        if (requestCode == REQ_POST_NOTIFICATIONS) {
            boolean granted = grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            handlePostNotificationsPermissionResult(granted);
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

                applyFooterTabLabelsFromTranslator();
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
                    clearPendingFlipRestore();
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
        if (source == null) {
            return datum;
        }
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

    /** Settings parental control: hide coarse abusive word list when enabled. */
    private static boolean looksAbusiveComment(String text) {
        if (text == null || text.trim().isEmpty()) return false;
        String t = text.toLowerCase(Locale.US);
        String[] hits = {
                "fuck", "shit", "bitch", "asshole", "bastard", "cunt", "whore",
                "rape", "nigger", "nigga", "slut"
        };
        for (String h : hits) {
            if (t.contains(h)) return true;
        }
        return false;
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
            if (!ProfileHubConfigHelper.isFooterShowPlus(this) || !isDeviceOnline()) {
                hideCenterPlusOverlays();
                showLog("updateCenterFab magazine suppressed (admin plus off or offline)");
                return;
            }
            // Share mode replaces create-menu — do not leave X/open state around.
            closeCenterPlusMenu(false);
            applyMagazineCenterShare();
            Log.i(MAGAZINE_GLOW_TAG, "MODE=SHARE_FOOTER postType=[" + type + "] title="
                    + (title.length() > 40 ? title.substring(0, 40) : title));
            showLog("updateCenterFab magazine share footer");
            return;
        }
        // Not a magazine -> restore the normal center button (+ or WhatsApp).
        hideMagazineShareFooter();
        if (!ProfileHubConfigHelper.isFooterShowPlus(this) || !isDeviceOnline()) {
            hideCenterPlusOverlays();
            showLog("updateCenterFab plus suppressed (admin off or offline)");
            return;
        }
        if (floatingActionButtonHome != null) {
            floatingActionButtonHome.clearAnimation();
            floatingActionButtonHome.setScaleX(1f);
            floatingActionButtonHome.setScaleY(1f);
            floatingActionButtonHome.setAlpha(1f);
            floatingActionButtonHome.setBackgroundTintList(
                    ContextCompat.getColorStateList(this, R.color.bottom_navigation_view_color));
            floatingActionButtonHome.setRippleColor(
                    ContextCompat.getColorStateList(this, R.color.bottom_navigation_view_color));
            // If create-menu still open, keep X; else force clean + (no rotation residue).
            applyCenterPlusExpandedIcon(isCenterPlusMenuOpen());
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
        // Keep Share over the same grid slot as the + (rebalance when Categories toggled).
        repositionCenterFabForVisibleTabs();
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
     * Top-left glass profile chip is retired — profile opens from the bottom footer tab.
     * Kept GONE always (admin flag ignored) so stale splash prefs never show it again.
     */
    private void setupHomeGlassHeader() {
        View glassHeader = findViewById(R.id.layout_home_glass_header);
        ImageView btnProfile = findViewById(R.id.btn_home_profile);
        if (glassHeader != null) {
            glassHeader.setVisibility(View.GONE);
        }
        if (btnProfile != null) {
            btnProfile.setOnClickListener(null);
        }
    }

    /**
     * Top Offline News | Bookmarks pill + bottom Home|Calendar|Reload overlay.
     * Content tap toggles top+bottom together.
     */
    private void setupNewsBookmarksToggle() {
        homeModeToggleRoot = findViewById(R.id.home_news_bookmarks_toggle_root);
        if (homeModeToggleRoot == null) {
            // Include merge: root id may be on include wrapper
            View include = findViewById(R.id.include_home_news_bookmarks_toggle);
            if (include != null) {
                homeModeToggleRoot = include.findViewById(R.id.home_news_bookmarks_toggle_root);
                if (homeModeToggleRoot == null) {
                    homeModeToggleRoot = include;
                }
            }
        }
        homeModeThumb = findViewById(R.id.home_mode_thumb);
        homeModePill = findViewById(R.id.home_mode_pill);
        homeModeNewsSeg = findViewById(R.id.home_mode_news_seg);
        homeModeNewsLabel = findViewById(R.id.home_mode_news);
        homeModeNewsBadge = findViewById(R.id.home_mode_news_badge);
        homeModeBookmarksLabel = findViewById(R.id.home_mode_bookmarks);
        homeModeBookmarksSeg = findViewById(R.id.home_mode_bookmarks_seg);
        homeModeBookmarksBadge = findViewById(R.id.home_mode_bookmarks_badge);
        homeBookmarksPanel = findViewById(R.id.home_bookmarks_panel);
        if (homeBookmarksPanel == null) {
            View panelInclude = findViewById(R.id.include_home_bookmarks_panel);
            if (panelInclude != null) {
                homeBookmarksPanel = panelInclude.findViewById(R.id.home_bookmarks_panel);
                if (homeBookmarksPanel == null) homeBookmarksPanel = panelInclude;
            }
        }
        homeBookmarksList = findViewById(R.id.home_bookmarks_list);
        homeBookmarksEmpty = findViewById(R.id.home_bookmarks_empty);
        homeBookmarksHint = findViewById(R.id.home_bookmarks_hint);

        bindHomeActionChrome();

        if (homeBookmarksList != null && homeBookmarksAdapter == null) {
            homeBookmarksAdapter = new HomeBookmarksAdapter();
            homeBookmarksList.setLayoutManager(new LinearLayoutManager(this));
            homeBookmarksList.setAdapter(homeBookmarksAdapter);
        }

        View.OnClickListener openNews = v -> selectHomeModeNews(false);
        if (homeModeNewsSeg != null) {
            homeModeNewsSeg.setOnClickListener(openNews);
        } else if (homeModeNewsLabel != null) {
            homeModeNewsLabel.setOnClickListener(openNews);
        }
        View.OnClickListener openBm = v -> selectHomeModeBookmarks();
        if (homeModeBookmarksSeg != null) {
            homeModeBookmarksSeg.setOnClickListener(openBm);
        } else if (homeModeBookmarksLabel != null) {
            homeModeBookmarksLabel.setOnClickListener(openBm);
        }
        // Absorb taps on chrome band so they do not propagate to content hide/show.
        if (homeModeToggleRoot != null) {
            homeModeToggleRoot.setOnClickListener(v -> { /* consume */ });
        }
        // Empty bookmarks state: tap shows/hides chrome (list items open articles).
        if (homeBookmarksEmpty != null) {
            homeBookmarksEmpty.setOnClickListener(v -> toggleHomeModeChrome());
        }

        // Default left segment; visibility driven by network.
        bookmarksModeSelected = false;
        homeModeChromeUserHidden = false;
        refreshHomeModeToggleUi();
        refreshHomeModeCounts();
        // Seed Read badge once; thereafter 30s poll + News|Read toggle only.
        try {
            refreshHomeReadNewsBadge();
            ensureReadBadgePollRunning();
        } catch (Exception ignored) {
        }
        applyHomeModeContentVisibility();
        scheduleHomeModeChromeSessionAutoHideIfNeeded();
    }

    private void bindHomeActionChrome() {
        homeActionChromeRoot = findViewById(R.id.home_action_chrome_root);
        if (homeActionChromeRoot == null) {
            View include = findViewById(R.id.include_home_action_chrome);
            if (include != null) {
                homeActionChromeRoot = include.findViewById(R.id.home_action_chrome_root);
                if (homeActionChromeRoot == null) {
                    homeActionChromeRoot = include;
                }
            }
        }
        homeActionCalendarLabel = findViewById(R.id.home_action_calendar_label);
        homeActionReloadLabel = findViewById(R.id.home_action_reload_label);
        View calBtn = findViewById(R.id.home_action_calendar);
        View reloadBtn = findViewById(R.id.home_action_reload);
        if (calBtn != null) {
            calBtn.setOnClickListener(v -> onHomeActionChromeCalendar());
        }
        if (reloadBtn != null) {
            reloadBtn.setOnClickListener(v -> onHomeActionChromeReload());
        }
        if (homeActionChromeRoot != null) {
            homeActionChromeRoot.setOnClickListener(v -> { /* consume */ });
        }
        applyHomeActionChromeLabels();
    }



    private void applyHomeActionChromeLabels() {
        if (homeActionCalendarLabel != null) {
            homeActionCalendarLabel.setText(
                    com.anyNews.anynews.Utils.HomeChromeActionLabels.calendar(this));
        }
        if (homeActionReloadLabel != null) {
            homeActionReloadLabel.setText(
                    com.anyNews.anynews.Utils.HomeChromeActionLabels.reload(this));
        }
    }

    private void onHomeActionChromeCalendar() {
        if (isFinishing() || isDestroyed()) return;
        cancelHomeModeChromeSessionAutoHide(true);
        try {
            // Optional pre-warm so first calendar open is instant offline.
            try {
                com.anyNews.anynews.panchang.PanchangRepository.warmCurrentYear(this);
            } catch (Exception ignored) {
            }
            startActivity(new Intent(this, TeluguCalendarActivity.class));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    

    /**
     * Single tap on article content (from FlipperAdapter) — hide/show the Offline
     * News|Bookmarks chrome only. Does not collapse article body/text.
     * Top + bottom action chrome move together.
     */
    public void toggleHomeModeChrome() {
        if (isFinishing() || isDestroyed()) return;
        // Offline Offline-News|Bookmarks or online main News|Read News only.
        if (!isHomeModeChromeActive()) return;
        if (homeModeToggleRoot == null && homeActionChromeRoot == null) return;
        // User interaction ends the first-open auto-hide window for this session.
        cancelHomeModeChromeSessionAutoHide(true);
        setHomeModeChromeVisible(homeModeChromeUserHidden, true);
    }

    /**
     * Once per app process: after N seconds (admin
     * {@code home_mode_chrome_auto_hide_seconds}, default 3), hide top + bottom
     * chrome using the same path as a content tap. 0 disables.
     * Subsequent show/hide is tap-only.
     */
    private void scheduleHomeModeChromeSessionAutoHideIfNeeded() {
        if (homeModeChromeSessionAutoHideConsumed) return;
        if (homeModeChromeAutoHidePending) return;
        if (isFinishing() || isDestroyed()) return;
        if (!isHomeModeChromeActive()) return;
        if (homeModeChromeUserHidden) return;
        if (homeModeToggleRoot == null && homeActionChromeRoot == null) return;
        // Only when chrome is actually on-screen (first presentation this session).
        boolean topShowing = homeModeToggleRoot != null
                && homeModeToggleRoot.getVisibility() == View.VISIBLE;
        boolean bottomShowing = homeActionChromeRoot != null
                && homeActionChromeRoot.getVisibility() == View.VISIBLE;
        if (!topShowing && !bottomShowing) return;
        int seconds = HomeModeChromeConfig.getAutoHideSeconds(this);
        if (seconds <= 0) {
            // Admin disabled: do not auto-hide this session.
            homeModeChromeSessionAutoHideConsumed = true;
            return;
        }
        homeModeChromeAutoHidePending = true;
        try {
            homeModeChromeAutoHideHandler.removeCallbacks(homeModeChromeAutoHideRunnable);
            homeModeChromeAutoHideHandler.postDelayed(
                    homeModeChromeAutoHideRunnable, seconds * 1000L);
        } catch (Exception ignored) {
            homeModeChromeAutoHidePending = false;
        }
    }

    private void runHomeModeChromeSessionAutoHide() {
        homeModeChromeAutoHidePending = false;
        if (homeModeChromeSessionAutoHideConsumed) return;
        homeModeChromeSessionAutoHideConsumed = true;
        if (isFinishing() || isDestroyed()) return;
        if (!isHomeModeChromeActive()) return;
        if (homeModeChromeUserHidden) return;
        // Exact same animation path as content-tap hide (top + bottom).
        setHomeModeChromeVisible(false, true);
    }

    /**
     * @param consumeSession when true, marks this process's first-open auto-hide
     *                       as finished (user or system already hid / cancelled).
     */
    private void cancelHomeModeChromeSessionAutoHide(boolean consumeSession) {
        homeModeChromeAutoHidePending = false;
        try {
            homeModeChromeAutoHideHandler.removeCallbacks(homeModeChromeAutoHideRunnable);
        } catch (Exception ignored) {
        }
        if (consumeSession) {
            homeModeChromeSessionAutoHideConsumed = true;
        }
    }

    /** True when Offline News|Bookmarks pill chrome is the active UI mode. */
    public boolean isOfflineChromeMode() {
        return offlineChromeMode;
    }

    /**
     * Online main News feed only: News | Read News pill (not Special / Video / offline).
     */
    public boolean isOnlineMainNewsChromeActive() {
        return isDeviceOnline()
                && !offlineChromeMode
                && !isSpecialFeedMode()
                && !isVideoFeedMode();
    }

    /**
     * Online main + Read segment: FlipView shows already-read postcards
     * (same adapter UX as News; no vertical bookmark-style list).
     */
    public boolean isOnlineReadFlipMode() {
        return isOnlineMainNewsChromeActive() && bookmarksModeSelected;
    }

    /** Top mode pill eligible for show / content-tap hide. */
    public boolean isHomeModeChromeActive() {
        return offlineChromeMode || isOnlineMainNewsChromeActive();
    }

    /**
     * Slide top News|Read bar (scrim + pill) up / bottom Home|Calendar|Reload
     * down. Article body layout is untouched.
     */
    private void setHomeModeChromeVisible(boolean visible, boolean animate) {
        if (homeModeToggleRoot == null && homeActionChromeRoot == null) return;
        if (!isHomeModeChromeActive()) {
            homeModeChromeUserHidden = false;
            cancelHomeModeChromeAnimator();
            forceHideAllHomeModeChrome();
            return;
        }
        homeModeChromeUserHidden = !visible;
        cancelHomeModeChromeAnimator();

        Runnable run = () -> {
            if (isFinishing()) return;
            float dens = getResources().getDisplayMetrics().density;
            ArrayList<Animator> parts = new ArrayList<>();

            if (homeModeToggleRoot != null) {
                int h = homeModeToggleRoot.getHeight();
                if (h <= 0) {
                    h = (int) (56f * dens);
                }
                float hiddenY = -h - 8f * dens;
                prepareChromeSlideIn(homeModeToggleRoot, visible, hiddenY);
                Animator a = buildChromeSlideAlpha(
                        homeModeToggleRoot, visible, hiddenY, animate);
                if (a != null) parts.add(a);
            }
            if (homeActionChromeRoot != null) {
                int h = homeActionChromeRoot.getHeight();
                if (h <= 0) {
                    h = (int) (56f * dens);
                }
                float hiddenY = h + 8f * dens; // slide down off bottom
                prepareChromeSlideIn(homeActionChromeRoot, visible, hiddenY);
                Animator a = buildChromeSlideAlpha(
                        homeActionChromeRoot, visible, hiddenY, animate);
                if (a != null) parts.add(a);
            }

            if (parts.isEmpty() || !animate) {
                applyChromeVisibilityEndState(visible);
                return;
            }

            AnimatorSet set = new AnimatorSet();
            set.playTogether(parts);
            set.setDuration(HOME_MODE_CHROME_ANIM_MS);
            set.setInterpolator(
                    new androidx.interpolator.view.animation.FastOutSlowInInterpolator());
            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    applyChromeVisibilityEndState(visible);
                    homeModeChromeAnimator = null;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    homeModeChromeAnimator = null;
                }
            });
            homeModeChromeAnimator = set;
            set.start();
        };

        boolean needMeasure =
                (homeModeToggleRoot != null && homeModeToggleRoot.getHeight() <= 0)
                        || (homeActionChromeRoot != null
                        && homeActionChromeRoot.getHeight() <= 0);
        if (needMeasure) {
            if (visible) {
                if (homeModeToggleRoot != null) {
                    homeModeToggleRoot.setVisibility(View.VISIBLE);
                    homeModeToggleRoot.setAlpha(0f);
                }
                if (homeActionChromeRoot != null) {
                    homeActionChromeRoot.setVisibility(View.VISIBLE);
                    homeActionChromeRoot.setAlpha(0f);
                }
            }
            View postHost = homeModeToggleRoot != null
                    ? homeModeToggleRoot
                    : homeActionChromeRoot;
            if (postHost != null) {
                postHost.post(run);
            } else {
                run.run();
            }
        } else {
            run.run();
        }
    }

    private void prepareChromeSlideIn(View v, boolean visible, float hiddenY) {
        if (v == null) return;
        if (visible) {
            if (v.getVisibility() != View.VISIBLE || v.getAlpha() < 0.05f) {
                v.setTranslationY(hiddenY);
                v.setAlpha(0f);
            }
            v.setVisibility(View.VISIBLE);
        }
    }

    /**
     * @return null if snap without animation is preferred
     */
    @Nullable
    private Animator buildChromeSlideAlpha(
            View v, boolean visible, float hiddenY, boolean animate) {
        if (v == null) return null;
        final float startY = v.getTranslationY();
        final float startA = v.getAlpha();
        final float endY = visible ? 0f : hiddenY;
        final float endA = visible ? 1f : 0f;
        boolean shouldAnimate = animate
                && (Math.abs(startY - endY) > 1f || Math.abs(startA - endA) > 0.02f);
        if (!shouldAnimate) {
            v.setTranslationY(endY);
            v.setAlpha(endA);
            v.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
            return null;
        }
        ObjectAnimator ty = ObjectAnimator.ofFloat(v, View.TRANSLATION_Y, startY, endY);
        ObjectAnimator al = ObjectAnimator.ofFloat(v, View.ALPHA, startA, endA);
        AnimatorSet pair = new AnimatorSet();
        pair.playTogether(ty, al);
        return pair;
    }

    private void applyChromeVisibilityEndState(boolean visible) {
        if (homeModeToggleRoot != null) {
            homeModeToggleRoot.setTranslationY(visible ? 0f : homeModeToggleRoot.getTranslationY());
            homeModeToggleRoot.setAlpha(visible ? 1f : 0f);
            homeModeToggleRoot.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
            if (visible) {
                homeModeToggleRoot.setTranslationY(0f);
            }
        }
        if (homeActionChromeRoot != null) {
            homeActionChromeRoot.setAlpha(visible ? 1f : 0f);
            homeActionChromeRoot.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
            if (visible) {
                homeActionChromeRoot.setTranslationY(0f);
            }
        }
    }

    private void hideChromeViewImmediate(@Nullable View v) {
        if (v == null) return;
        v.setAlpha(0f);
        v.setVisibility(View.INVISIBLE);
    }

    private void forceHideAllHomeModeChrome() {
        if (homeModeToggleRoot != null) {
            homeModeToggleRoot.setVisibility(View.GONE);
            homeModeToggleRoot.setTranslationY(0f);
            homeModeToggleRoot.setAlpha(1f);
        }
        if (homeActionChromeRoot != null) {
            homeActionChromeRoot.setVisibility(View.GONE);
            homeActionChromeRoot.setTranslationY(0f);
            homeActionChromeRoot.setAlpha(1f);
        }
    }

    /**
     * Snap secondary chrome (bottom action bar) to the same show/hide as top when
     * refreshHomeModeToggleUi decides without animation.
     */
    private void syncSecondaryChromeVisibilityFromTop() {
        if (!isHomeModeChromeActive()) {
            forceHideAllHomeModeChrome();
            return;
        }
        if (homeModeChromeUserHidden) {
            hideSecondaryChromeInstant();
            return;
        }
        // Top will be made VISIBLE by caller; mirror bottom.
        if (homeActionChromeRoot != null) {
            homeActionChromeRoot.setVisibility(View.VISIBLE);
            if (homeModeChromeAnimator == null) {
                homeActionChromeRoot.setTranslationY(0f);
                homeActionChromeRoot.setAlpha(1f);
            }
        }
    }

    private void hideSecondaryChromeInstant() {
        if (homeActionChromeRoot != null) {
            if (homeActionChromeRoot.getVisibility() == View.VISIBLE
                    && homeActionChromeRoot.getAlpha() > 0.05f
                    && homeModeChromeAnimator == null) {
                homeActionChromeRoot.setVisibility(View.INVISIBLE);
                homeActionChromeRoot.setAlpha(0f);
                int h = homeActionChromeRoot.getHeight();
                if (h > 0) {
                    homeActionChromeRoot.setTranslationY(
                            h + 8f * getResources().getDisplayMetrics().density);
                }
            }
        }
    }

    private void cancelHomeModeChromeAnimator() {
        if (homeModeChromeAnimator != null) {
            try {
                homeModeChromeAnimator.cancel();
            } catch (Exception ignored) {
            }
            homeModeChromeAnimator = null;
        }
    }

    private void resetHomeModeChromeTransform() {
        if (homeModeToggleRoot != null) {
            homeModeToggleRoot.setTranslationY(0f);
            homeModeToggleRoot.setAlpha(1f);
        }
        if (homeActionChromeRoot != null) {
            homeActionChromeRoot.setTranslationY(0f);
            homeActionChromeRoot.setAlpha(1f);
        }
    }

    /** Offline pack/bookmarks counts, or online Read News count; call on resume / paint / switch. */
    private void refreshHomeModeCounts() {
        if (isOnlineMainNewsChromeActive()) {
            // Product: News has NO count badge (ever) when online main.
            hideHomeModeNewsBadgeForeverOnline();
            // Read badge: NOT on every flip / incidental refresh — 30s timer + mode toggle only.
            return;
        }
        // Online non-main (Special/Video): hide News badge if pill brief-flashes.
        if (isDeviceOnline() && !offlineChromeMode) {
            hideHomeModeNewsBadgeForeverOnline();
        }
        refreshHomeOfflineNewsBadge();
        refreshHomeBookmarksBadge();
    }

    /** Start / keep the Read badge 30s poll while Home is live. */
    private void ensureReadBadgePollRunning() {
        try {
            readBadgeHandler.removeCallbacks(readBadgePollRunnable);
            readBadgeHandler.postDelayed(readBadgePollRunnable, READ_BADGE_POLL_MS);
        } catch (Exception ignored) {
        }
    }

    private void stopReadBadgePoll() {
        try {
            readBadgeHandler.removeCallbacks(readBadgePollRunnable);
        } catch (Exception ignored) {
        }
    }

    /** Online News segment never shows a numeric badge — hide hard. */
    private void hideHomeModeNewsBadgeForeverOnline() {
        if (homeModeNewsBadge == null) return;
        homeModeNewsBadge.setVisibility(View.GONE);
        homeModeNewsBadge.setText("");
        try {
            ViewGroup.LayoutParams lp = homeModeNewsBadge.getLayoutParams();
            if (lp instanceof ViewGroup.MarginLayoutParams) {
                ((ViewGroup.MarginLayoutParams) lp).setMarginStart(0);
            }
            if (lp != null) {
                lp.width = 0;
                lp.height = 0;
                homeModeNewsBadge.setLayoutParams(lp);
            }
        } catch (Exception ignored) {
        }
    }

    private void restoreHomeModeNewsBadgeLayoutIfNeeded() {
        if (homeModeNewsBadge == null) return;
        try {
            ViewGroup.LayoutParams lp = homeModeNewsBadge.getLayoutParams();
            if (lp != null && (lp.width == 0 || lp.height == 0)) {
                lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                lp.height = (int) (18f * getResources().getDisplayMetrics().density + 0.5f);
                if (lp instanceof ViewGroup.MarginLayoutParams) {
                    ((ViewGroup.MarginLayoutParams) lp).setMarginStart(
                            (int) (6f * getResources().getDisplayMetrics().density + 0.5f));
                }
                homeModeNewsBadge.setLayoutParams(lp);
            }
        } catch (Exception ignored) {
        }
    }

    private void refreshHomeOfflineNewsBadge() {
        if (homeModeNewsBadge == null) return;
        // Never show a News-side count while online main News|Read UI is active
        // (OfflinePack change listener used to race and restore pack count).
        if (isDeviceOnline() && !offlineChromeMode) {
            hideHomeModeNewsBadgeForeverOnline();
            remeasureHomeModeThumbAfterBadge();
            return;
        }
        restoreHomeModeNewsBadgeLayoutIfNeeded();
        int pack = 0;
        try {
            // Only media-ready offline stories (same set as Offline News paint).
            pack = OfflineFeedStore.mainPackCount(this);
        } catch (Exception ignored) {
        }
        // Do not fall back to raw list.size() — that inflated the badge with
        // placeholder-only cards that lack a cached hero image.
        applyModeCountBadge(homeModeNewsBadge, pack);
        // Badge width can change segment size; remeasure only (never kill a slide).
        remeasureHomeModeThumbAfterBadge();
    }

    private void refreshHomeReadNewsBadge() {
        if (homeModeBookmarksBadge == null) return;
        try {
            ReadArticleStore.countAsync(
                    this,
                    count -> {
                        if (isFinishing()) return;
                        // Only update right badge when online main (avoid racing with bookmarks).
                        if (!isOnlineMainNewsChromeActive()) return;
                        applyModeCountBadge(homeModeBookmarksBadge, count);
                        hideHomeModeNewsBadgeForeverOnline();
                        remeasureHomeModeThumbAfterBadge();
                    });
        } catch (Exception ignored) {
            applyModeCountBadge(homeModeBookmarksBadge, 0);
        }
    }

    private static void applyModeCountBadge(@Nullable TextView badge, int count) {
        if (badge == null) return;
        if (count <= 0) {
            badge.setVisibility(View.GONE);
            badge.setText("0");
            return;
        }
        badge.setVisibility(View.VISIBLE);
        if (count > 999) {
            badge.setText("999+");
        } else {
            badge.setText(String.valueOf(count));
        }
    }

    /**
     * When online + main News: always keep the bottom tabs visible.
     * Ads/share cards used to GONE the footer (white bottom).
     */
    private void ensureOnlineBottomNavVisible() {
        if (home_page_navigation == null) return;
        if (!isDeviceOnline() || offlineChromeMode) return;
        try {
            if (home_page_navigation.getVisibility() != View.VISIBLE) {
                home_page_navigation.setVisibility(View.VISIBLE);
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Top News|Read pill should hide on ads / share_app / web interstitials —
     * only show for real flip articles (or the Read-tab panel).
     */
    private boolean shouldShowOnlineModeToggleForCurrentCard() {
        if (!isOnlineMainNewsChromeActive()) return false;
        // Read flip: always keep the pill (same as former panel behavior).
        if (bookmarksModeSelected) return true;
        if (list == null || list.isEmpty()) return false;
        int pos = positionx;
        try {
            if (mFlipView != null) {
                int cur = mFlipView.getCurrentPage();
                if (cur >= 0 && cur < list.size()) pos = cur;
            }
        } catch (Exception ignored) {
        }
        if (pos < 0 || pos >= list.size()) return false;
        ModelClass.Datum d = list.get(pos);
        if (d == null) return false;
        if (ReadArticleStore.isSyntheticFeedSlot(d)) return false;
        String type = d.getPostType();
        if (type == null) return false;
        // Hide on common non-article shells.
        return !type.equalsIgnoreCase("ad")
                && !type.equalsIgnoreCase("share_app")
                && !type.equalsIgnoreCase("ratings")
                && !type.equalsIgnoreCase("web_ads")
                && !type.equalsIgnoreCase("web");
    }

    /**
     * True when main online News (and optional in-background while on Read) may page
     * List_All_Articles. Special/Video never. Does not check Read segment — responses
     * while on Read absorb into the News snapshot instead of painting the flip.
     */
    private boolean canPageOnlineMainNews() {
        return isDeviceOnline()
                && !offlineChromeMode
                && !isSpecialFeedMode()
                && !isVideoFeedMode();
    }

    /**
     * True when the active online flip feed may request another {@code List_All_Articles}
     * page: main News <b>or</b> Special <b>or</b> Video. Soft-empty / synthetic-only /
     * loader-timeout recovery must not leave Video on a permanent white ad spinner because
     * paging was hard-gated to main News only.
     */
    private boolean canPageActiveOnlineFlipFeed() {
        if (!isDeviceOnline() || offlineChromeMode) {
            return false;
        }
        if (isVideoFeedMode() || isSpecialFeedMode()) {
            return true;
        }
        return canPageOnlineMainNews();
    }

    /** Left segment: Offline News (offline) or unread News feed (online main). */
    private void selectHomeModeNews(boolean forceReload) {
        if (isOnlineMainNewsChromeActive()) {
            if (!bookmarksModeSelected && !forceReload) {
                refreshHomeModeToggleUi();
                return;
            }
            final boolean leavingRead = bookmarksModeSelected;
            if (leavingRead) {
                onlineNewsReadSessionEpoch++;
            }
            bookmarksModeSelected = false;
            applyHomeModeContentVisibility();
            refreshHomeModeToggleUi();
            refreshHomeModeCounts();
            // Instant Read badge when leaving Read (and vice versa path below).
            refreshHomeReadNewsBadge();
            if (leavingRead) {
                restoreOnlineNewsFlipFromSnapshot();
            } else {
                try {
                    filterOnlineNewsListToUnread(forceReload);
                } catch (Exception ignored) {
                }
            }
            return;
        }
        if (isDeviceOnline()) {
            // Special/Video etc. — pill not active.
            refreshHomeModeToggleUi();
            return;
        }
        if (!bookmarksModeSelected && !forceReload) {
            refreshHomeModeToggleUi();
            return;
        }
        bookmarksModeSelected = false;
        applyHomeModeContentVisibility();
        refreshHomeModeToggleUi();
        refreshHomeModeCounts();
        // If main flip list empty offline after bookmarks, re-paint cache.
        try {
            if ((list == null || list.isEmpty())
                    && !isSpecialFeedMode()) {
                paintOfflineFeed(false, false);
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Right segment: offline Bookmarks list, or online Read News (flip + local cache).
     */
    private void selectHomeModeBookmarks() {
        if (isOnlineMainNewsChromeActive()) {
            if (bookmarksModeSelected) {
                refreshHomeModeToggleUi();
                return;
            }
            snapshotOnlineNewsFlipSession();
            onlineNewsReadSessionEpoch++;
            bookmarksModeSelected = true;
            // Do not bump feedModeEpoch — in-flight News pages must still land into
            // the saved snapshot (absorbMainFeedResponseIntoNewsSnapshot).
            applyHomeModeContentVisibility();
            refreshHomeModeToggleUi();
            refreshHomeModeCounts();
            // Instant badge + list paint on toggle to Read.
            refreshHomeReadNewsBadge();
            paintOnlineReadNewsFlip();
            return;
        }
        if (isDeviceOnline()) {
            refreshHomeModeToggleUi();
            return;
        }
        bookmarksModeSelected = true;
        applyHomeModeContentVisibility();
        refreshHomeModeToggleUi();
        refreshHomeModeCounts();
        reloadHomeBookmarksPanel();
        try {
            BookmarkStore.syncFromServer(this, () -> {
                reloadHomeBookmarksPanel();
                refreshHomeModeCounts();
            });
            BookmarkStore.flushPending(this);
        } catch (Exception ignored) {
        }
    }

    private void applyHomeModeContentVisibility() {
        // Online Read uses FlipView (same as News); offline Bookmarks still use the list panel.
        final boolean showBookmarkPanel =
                bookmarksModeSelected && !isOnlineMainNewsChromeActive();
        if (homeBookmarksPanel != null) {
            homeBookmarksPanel.setVisibility(showBookmarkPanel ? View.VISIBLE : View.GONE);
        }
        // Keep flip data under the panel when offline bookmarks open (no destroy).
        try {
            if (layout_with_data != null && !bookmarksModeSelected) {
                // Do not force-show if still loading reveal; paintOffline/inlist owns that.
            }
        } catch (Exception ignored) {
        }
    }

    private void refreshHomeModeToggleUi() {
        final boolean onlineMain = isOnlineMainNewsChromeActive();
        final boolean offlinePill = offlineChromeMode || !isDeviceOnline();

        // Online but Special / Video (or other non-main): hide pill + close right panel.
        if (isDeviceOnline() && !offlineChromeMode && !onlineMain) {
            homeModeChromeUserHidden = false;
            cancelHomeModeChromeAnimator();
            cancelHomeModeThumbAnimator();
            homeModeThumbPlaced = false;
            resetHomeModeChromeTransform();
            forceHideAllHomeModeChrome();
            if (bookmarksModeSelected) {
                bookmarksModeSelected = false;
                applyHomeModeContentVisibility();
            }
            ensureOnlineBottomNavVisible();
            return;
        }

        // Neither offline chrome nor online main News: hide.
        if (!onlineMain && !offlinePill) {
            forceHideAllHomeModeChrome();
            return;
        }

        // Online main: hide pill on ads / share_app interstitials (not meaningful feed).
        if (onlineMain && !shouldShowOnlineModeToggleForCurrentCard()) {
            if (homeModeToggleRoot != null) {
                homeModeToggleRoot.setVisibility(View.GONE);
            }
            if (homeActionChromeRoot != null) {
                homeActionChromeRoot.setVisibility(View.GONE);
            }
            hideHomeModeNewsBadgeForeverOnline();
            ensureOnlineBottomNavVisible();
            // Still keep right-segment selected if Read panel open (panel-only).
            if (bookmarksModeSelected) {
                // Keep Read list; pill hidden until they leave interstitial... but panel
                // needs toggle to switch back — re-show pill when bookmarks selected.
                if (!homeModeChromeUserHidden) {
                    if (homeModeToggleRoot != null) {
                        homeModeToggleRoot.setVisibility(View.VISIBLE);
                    }
                    if (homeActionChromeRoot != null) {
                        homeActionChromeRoot.setVisibility(View.VISIBLE);
                    }
                    if (homeModeChromeAnimator == null) {
                        resetHomeModeChromeTransform();
                    }
                    syncSecondaryChromeVisibilityFromTop();
                }
            } else {
                return;
            }
        }

        // Online main or offline: respect user hide-chrome preference.
        if (homeModeToggleRoot != null) {
            if (homeModeChromeUserHidden) {
                if (homeModeToggleRoot.getVisibility() == View.VISIBLE
                        && homeModeToggleRoot.getAlpha() > 0.05f
                        && homeModeChromeAnimator == null) {
                    homeModeToggleRoot.setVisibility(View.INVISIBLE);
                    homeModeToggleRoot.setAlpha(0f);
                    int h = homeModeToggleRoot.getHeight();
                    if (h > 0) {
                        homeModeToggleRoot.setTranslationY(
                                -h - 8f * getResources().getDisplayMetrics().density);
                    }
                }
            } else {
                homeModeToggleRoot.setVisibility(View.VISIBLE);
                if (homeModeChromeAnimator == null) {
                    resetHomeModeChromeTransform();
                }
            }
        }
        // Bottom action chrome follows the same user-hidden preference.
        if (homeModeChromeUserHidden) {
            hideSecondaryChromeInstant();
        } else {
            syncSecondaryChromeVisibilityFromTop();
        }
        if (onlineMain) {
            hideHomeModeNewsBadgeForeverOnline();
            ensureOnlineBottomNavVisible();
        }

        // Way2: white labels; language from LanguageID.
        if (homeModeNewsLabel != null) {
            if (onlineMain) {
                homeModeNewsLabel.setText(
                        com.anyNews.anynews.Utils.HomeOnlineModeLabels.news(this));
            } else {
                homeModeNewsLabel.setText(
                        com.anyNews.anynews.Utils.HomeOfflineModeLabels.offlineNews(this));
            }
            homeModeNewsLabel.setBackground(null);
            homeModeNewsLabel.setTextColor(Color.WHITE);
        }
        if (homeModeBookmarksLabel != null) {
            if (onlineMain) {
                homeModeBookmarksLabel.setText(
                        com.anyNews.anynews.Utils.HomeOnlineModeLabels.readNews(this));
            } else {
                homeModeBookmarksLabel.setText(
                        com.anyNews.anynews.Utils.HomeOfflineModeLabels.bookmarks(this));
            }
            homeModeBookmarksLabel.setTextColor(Color.WHITE);
        }
        applyHomeActionChromeLabels();
        if (homeModeNewsSeg != null) {
            homeModeNewsSeg.setBackground(null);
        }
        if (homeModeBookmarksSeg != null) {
            homeModeBookmarksSeg.setBackground(null);
        }
        animateHomeModeThumb(bookmarksModeSelected, /* animate= */ true);
        refreshHomeModeCounts();
        scheduleHomeModeChromeSessionAutoHideIfNeeded();
    }

    /**
     * After badge text/visibility changes, re-seat thumb width/X without
     * cancelling an in-flight Offline News ↔ Bookmarks slide.
     */
    private void remeasureHomeModeThumbAfterBadge() {
        View host = homeModePill != null ? homeModePill : homeModeToggleRoot;
        if (host == null) {
            if (isHomeModeThumbAnimating()) return;
            animateHomeModeThumb(bookmarksModeSelected, false);
            return;
        }
        host.post(() -> {
            if (isHomeModeThumbAnimating()) return;
            animateHomeModeThumb(bookmarksModeSelected, false);
        });
    }

    private boolean isHomeModeThumbAnimating() {
        return homeModeThumbAnimator != null && homeModeThumbAnimator.isRunning();
    }

    private void cancelHomeModeThumbAnimator() {
        if (homeModeThumbAnimator != null) {
            homeModeThumbAnimator.cancel();
            homeModeThumbAnimator = null;
        }
    }

    /**
     * Sliding orange selected thumb under Offline News | Bookmarks.
     * Labels stay fixed on top; only the thumb moves (translationX + width).
     * First seat snaps; later segment switches animate ~300ms FastOutSlowIn.
     */
    private void animateHomeModeThumb(boolean bookmarksSelected, boolean animate) {
        if (homeModeThumb == null) return;
        final View target = bookmarksSelected
                ? (homeModeBookmarksSeg != null ? homeModeBookmarksSeg : homeModeBookmarksLabel)
                : (homeModeNewsSeg != null ? homeModeNewsSeg : homeModeNewsLabel);
        if (target == null) return;

        Runnable place = () -> {
            if (homeModeThumb == null || target.getWidth() <= 0) return;

            // Quiet remeasure must never cancel a travel animation.
            if (!animate && isHomeModeThumbAnimating()) {
                return;
            }

            int targetW = target.getWidth();
            int targetX = target.getLeft();
            // Segments row is sibling of thumb; offset by segments left inside pill.
            View segments = homeModePill != null
                    ? homeModePill.findViewById(R.id.home_mode_segments)
                    : findViewById(R.id.home_mode_segments);
            if (segments != null && target.getParent() == segments) {
                targetX = segments.getLeft() + target.getLeft();
            }

            float toX = targetX;
            float fromX = homeModeThumb.getTranslationX();
            int fromW = homeModeThumb.getWidth();
            if (fromW <= 0) {
                ViewGroup.LayoutParams curLp = homeModeThumb.getLayoutParams();
                if (curLp != null && curLp.width > 0) {
                    fromW = curLp.width;
                }
            }

            boolean needMove = Math.abs(fromX - toX) > 1f;
            boolean needResize = fromW > 0 && Math.abs(fromW - targetW) > 1;
            // Subsequent switches always animate; first show snaps in place.
            boolean shouldAnimate = animate
                    && homeModeThumbPlaced
                    && (needMove || needResize);

            cancelHomeModeThumbAnimator();

            if (!shouldAnimate) {
                ViewGroup.LayoutParams lp = homeModeThumb.getLayoutParams();
                if (lp != null && lp.width != targetW) {
                    lp.width = targetW;
                    homeModeThumb.setLayoutParams(lp);
                }
                homeModeThumb.setTranslationX(toX);
                homeModeThumbPlaced = true;
                return;
            }

            androidx.interpolator.view.animation.FastOutSlowInInterpolator interp =
                    new androidx.interpolator.view.animation.FastOutSlowInInterpolator();

            ObjectAnimator txAnim = ObjectAnimator.ofFloat(
                    homeModeThumb, View.TRANSLATION_X, fromX, toX);
            txAnim.setInterpolator(interp);

            Animator travel;
            if (needResize) {
                final ViewGroup.LayoutParams lp = homeModeThumb.getLayoutParams();
                ValueAnimator wAnim = ValueAnimator.ofInt(fromW, targetW);
                wAnim.setInterpolator(interp);
                wAnim.addUpdateListener(a -> {
                    if (lp == null || homeModeThumb == null) return;
                    lp.width = (int) a.getAnimatedValue();
                    homeModeThumb.setLayoutParams(lp);
                });
                AnimatorSet set = new AnimatorSet();
                set.playTogether(txAnim, wAnim);
                travel = set;
            } else {
                // Already correct width — only slide X.
                ViewGroup.LayoutParams lp = homeModeThumb.getLayoutParams();
                if (lp != null && lp.width != targetW) {
                    lp.width = targetW;
                    homeModeThumb.setLayoutParams(lp);
                }
                travel = txAnim;
            }

            travel.setDuration(HOME_MODE_THUMB_ANIM_MS);
            travel.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (homeModeThumbAnimator == animation) {
                        homeModeThumbAnimator = null;
                    }
                    homeModeThumbPlaced = true;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    if (homeModeThumbAnimator == animation) {
                        homeModeThumbAnimator = null;
                    }
                }
            });
            homeModeThumbAnimator = travel;
            travel.start();
        };

        View host = homeModePill != null ? homeModePill : homeModeToggleRoot;
        // After badge/layout changes, measure on next frame so left/width are correct.
        if (target.getWidth() <= 0 || homeModeThumb.getHeight() <= 0 || host != null) {
            if (host != null) {
                host.post(place);
            } else {
                place.run();
            }
        } else {
            place.run();
        }
    }

    private void refreshHomeBookmarksBadge() {
        // Online main uses Read News badge (postcard count), not bookmark count.
        if (isOnlineMainNewsChromeActive()) {
            hideHomeModeNewsBadgeForeverOnline();
            refreshHomeReadNewsBadge();
            return;
        }
        BookmarkStore.countLocalAsync(
                this,
                count -> {
                    if (isFinishing()) return;
                    if (isOnlineMainNewsChromeActive()) {
                        // Async race: stay on Read count path.
                        refreshHomeReadNewsBadge();
                        return;
                    }
                    applyModeCountBadge(homeModeBookmarksBadge, count);
                    remeasureHomeModeThumbAfterBadge();
                });
    }

    private void reloadHomeBookmarksPanel() {
        final int gen = ++homeBookmarksReloadGen;
        if (homeBookmarksHint != null) {
            try {
                homeBookmarksHint.setText(getString(R.string.bookmarks_offline_hint));
            } catch (Exception ignored) {
            }
        }
        BookmarkStore.listLocalAsync(
                this,
                listBm -> {
                    if (isFinishing() || gen != homeBookmarksReloadGen) return;
                    homeBookmarkItems.clear();
                    if (listBm != null) {
                        homeBookmarkItems.addAll(listBm);
                    }
                    if (homeBookmarksAdapter != null) {
                        homeBookmarksAdapter.notifyDataSetChanged();
                    }
                    boolean empty = homeBookmarkItems.isEmpty();
                    if (homeBookmarksEmpty != null) {
                        homeBookmarksEmpty.setText(R.string.no_bookmarks);
                        homeBookmarksEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
                    }
                    if (homeBookmarksList != null) {
                        homeBookmarksList.setVisibility(empty ? View.GONE : View.VISIBLE);
                    }
                    refreshHomeBookmarksBadge();
                });
    }

    /**
     * While online Read flip is visible, merge a main-feed network page into the
     * saved News session (cursor + list) so returning to News stays current.
     */
    private void absorbMainFeedResponseIntoNewsSnapshot(
            @Nullable EncreptionRequest encreptionRequest, boolean page0Replace) {
        if (encreptionRequest == null || encreptionRequest.getData() == null) {
            return;
        }
        if (isSpecialFeedMode() || isVideoFeedMode()) {
            return;
        }
        try {
            String encrpt_data = decrypt(encreptionRequest.getData());
            if (encrpt_data == null || encrpt_data.length() <= 1) {
                return;
            }
            ModelClass payload = new Gson().fromJson(encrpt_data, ModelClass.class);
            if (payload == null || !Boolean.TRUE.equals(payload.getSuccess())) {
                return;
            }
            if (payload.getLastPostId() != null) {
                savedOnlineNewsLastPostId = payload.getLastPostId();
                last_post_id = payload.getLastPostId();
            }
            if (payload.getSkip() != null) {
                savedOnlineNewsSkip = payload.getSkip();
                skip = payload.getSkip();
            }
            if (payload.getActualSkip() != null) {
                savedOnlineNewsActualSkip = payload.getActualSkip();
                actual_skip = payload.getActualSkip();
            }
            java.util.List<ModelClass.Datum> page = payload.getData();
            if (page == null) {
                hasSavedOnlineNewsSession = true;
                return;
            }
            ArrayList<ModelClass.Datum> working = new ArrayList<>(page);
            try {
                rememberOnlineNewsAdTemplate(working);
                rememberFeedStickiesFrom(working);
                // New server pages: strip fully (no hold). Snapshot list itself still
                // keeps deferred-visible cards until the scroll gap is met.
                ReadArticleStore.stripReadFromFeed(getApplicationContext(), working);
                rebalanceOnlineNewsAdsIfNeeded(working);
            } catch (Exception ignored) {
            }
            // Share-only / no real after strip: still advance saved cursors so restoring
            // News can keep paging — never treat share_app as end of feed.
            if (page0Replace || !hasSavedOnlineNewsSession || savedOnlineNewsList.isEmpty()) {
                savedOnlineNewsList.clear();
                savedOnlineNewsList.addAll(working);
            } else {
                java.util.HashSet<String> existing = new java.util.HashSet<>();
                for (ModelClass.Datum d : savedOnlineNewsList) {
                    String key = articleDedupKey(d);
                    if (key != null) existing.add(key);
                }
                for (ModelClass.Datum d : working) {
                    if (d == null) continue;
                    if (d.getPostId() == null) {
                        savedOnlineNewsList.add(d);
                        continue;
                    }
                    String key = articleDedupKey(d);
                    if (key == null || !existing.contains(key)) {
                        savedOnlineNewsList.add(d);
                        if (key != null) existing.add(key);
                    }
                }
            }
            // Drop share/ad-only pages that didn't contribute reals when merging for skip-ahead.
            try {
                rebalanceOnlineNewsAdsIfNeeded(savedOnlineNewsList);
            } catch (Exception ignored) {
            }
            savedOnlineNewsRequestSize = savedOnlineNewsList.size();
            savedOnlineNewsRequestPosition = 1 + savedOnlineNewsRequestSize;
            hasSavedOnlineNewsSession = true;
            showLog("absorb News snapshot while Read flip size="
                    + savedOnlineNewsList.size()
                    + " reals="
                    + ReadArticleStore.countRealArticles(savedOnlineNewsList));
            // Keep requesting if unread snapshot is still short (don't stop on share shells).
            int snapReals = ReadArticleStore.countRealArticles(savedOnlineNewsList);
            if (snapReals < ONLINE_NEWS_MIN_UNREAD_CARDS
                    && onlineNewsAllReadSkipStreak < ONLINE_NEWS_ALL_READ_SKIP_CAP
                    && canPageOnlineMainNews()) {
                onlineNewsAllReadSkipStreak++;
                isApiRequested = false;
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (!isApiRequested && canPageOnlineMainNews()) {
                        isApiRequested = true;
                        ShowNews_Data_Values(category_id, 1);
                    }
                });
            } else if (snapReals >= ONLINE_NEWS_MIN_UNREAD_CARDS) {
                onlineNewsAllReadSkipStreak = 0;
            }
        } catch (Exception e) {
            showLog("absorbMainFeedResponseIntoNewsSnapshot: " + e.getMessage());
        }
    }

    /**
     * Capture unread News flip session before switching to online Read flip.
     */
    private void snapshotOnlineNewsFlipSession() {
        try {
            savedOnlineNewsList.clear();
            if (list != null) {
                savedOnlineNewsList.addAll(list);
            }
            savedOnlineNewsPage = 0;
            try {
                if (mFlipView != null) {
                    savedOnlineNewsPage = Math.max(0, mFlipView.getCurrentPage());
                } else {
                    savedOnlineNewsPage = Math.max(0, positionx);
                }
            } catch (Exception ignored) {
                savedOnlineNewsPage = Math.max(0, positionx);
            }
            savedOnlineNewsSkip = skip;
            savedOnlineNewsActualSkip = actual_skip;
            savedOnlineNewsLastPostId = last_post_id;
            savedOnlineNewsRequestPosition = requestPostition;
            savedOnlineNewsRequestSize = requestSize;
            hasSavedOnlineNewsSession = true;
            // Session-pending views stay on News until next process commit; no deferred map.
        } catch (Exception e) {
            hasSavedOnlineNewsSession = false;
        }
    }

    /**
     * Restore unread News flip after leaving online Read.
     */
    private void restoreOnlineNewsFlipFromSnapshot() {
        try {
            if (list == null) {
                list = new ArrayList<>();
            } else {
                list.clear();
            }
            if (hasSavedOnlineNewsSession) {
                list.addAll(savedOnlineNewsList);
                skip = savedOnlineNewsSkip;
                actual_skip = savedOnlineNewsActualSkip;
                last_post_id = savedOnlineNewsLastPostId;
                requestPostition = savedOnlineNewsRequestPosition;
                requestSize = savedOnlineNewsRequestSize > 0
                        ? savedOnlineNewsRequestSize
                        : list.size();
                positionx = savedOnlineNewsPage;
                if (positionx < 0) positionx = 0;
                if (!list.isEmpty() && positionx >= list.size()) {
                    positionx = list.size() - 1;
                }
            }
            // Strip only committed reads (prior sessions). Session-pending stay on News.
            try {
                rememberOnlineNewsAdTemplate(list);
                rememberFeedStickiesFrom(list);
                rememberFeedStickiesFrom(savedOnlineNewsList);
                ReadArticleStore.stripReadFromFeed(getApplicationContext(), list);
                rebalanceOnlineNewsAdsIfNeeded(list);
            } catch (Exception ignored) {
            }
            final int reals = ReadArticleStore.countRealArticles(list);
            final int paintables = ReadArticleStore.countPaintableArticles(list);
            // Empty of all paintables after restore: re-fetch unread cleanly.
            if (paintables == 0) {
                showLog("restore News: no paintables — re-fetch unread"
                        + " hasSession=" + hasSavedOnlineNewsSession
                        + " reals=" + reals);
                if (!hasSavedOnlineNewsSession) {
                    skip = 0;
                    actual_skip = 0;
                    last_post_id = 0;
                    requestPostition = 1;
                    requestSize = 0;
                }
                try {
                    if (txtNoData != null) {
                        txtNoData.setVisibility(View.GONE);
                    }
                } catch (Exception ignored) {
                }
                try {
                    showHomeFeedLoadingReveal();
                } catch (Exception ignored) {
                }
                ensureHomeFlipAdapterBoundAndData(list);
                onlineNewsAllReadSkipStreak = 0;
                if (canPageOnlineMainNews() && !isApiRequested) {
                    isApiRequested = true;
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (!isFinishing() && canPageOnlineMainNews()
                                && !bookmarksModeSelected) {
                            ShowNews_Data_Values(category_id, 1);
                        } else {
                            isApiRequested = false;
                        }
                    });
                }
                ensureOnlineBottomNavVisible();
                try {
                    refreshHomeModeToggleUi();
                } catch (Exception ignored) {
                }
                return;
            }
            ensureHomeFlipAdapterBoundAndData(list);
            try {
                if (mFlipView != null) {
                    mFlipView.setVisibility(View.VISIBLE);
                    if (list != null && !list.isEmpty()) {
                        int page = Math.max(0, Math.min(positionx, list.size() - 1));
                        mFlipView.flipTo(page);
                    }
                }
                if (layout_with_data != null) {
                    layout_with_data.setVisibility(View.VISIBLE);
                }
                if (txtNoData != null) {
                    txtNoData.setVisibility(
                            (list == null || list.isEmpty()) ? View.VISIBLE : View.GONE);
                }
                if (comment_page != null) {
                    comment_page.setVisibility(View.GONE);
                }
            } catch (Exception ignored) {
            }
            filterOnlineNewsListToUnread(true);
            maybePrefetchOnlineNewsIfNearEnd();
        } catch (Exception e) {
            try {
                filterOnlineNewsListToUnread(true);
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Online Read News — FlipView with local postcard cache (newest read first).
     * Read sticky postcards are re-pinned at the same admin sticky_position indices
     * ({@link ReadArticleStore#buildReadListWithStickies}). Unread stickies do not appear here.
     * Same FlipperAdapter bindings as News; no vertical bookmark list.
     */
    private void paintOnlineReadNewsFlip() {
        paintOnlineReadNewsFlip(0L, null);
    }

    /**
     * Paint the online Read (Chadivinavi) flip. When {@code landPostId} &gt; 0, flip to
     * that postcard (insert {@code landDatum} at front if missing from the local cache).
     */
    private void paintOnlineReadNewsFlip(
            final long landPostId, @Nullable final ModelClass.Datum landDatum) {
        final int gen = ++homeBookmarksReloadGen;
        // Hide offline-style list panel; flip is the surface.
        try {
            if (homeBookmarksPanel != null) {
                homeBookmarksPanel.setVisibility(View.GONE);
            }
        } catch (Exception ignored) {
        }
        final ArrayList<ModelClass.Datum> stickiesCopy =
                new ArrayList<>(rememberedFeedStickies);
        // Also pull stickies from the News snapshot if remember list is cold.
        try {
            if (stickiesCopy.isEmpty() && hasSavedOnlineNewsSession) {
                stickiesCopy.addAll(
                        ReadArticleStore.extractStickyArticles(savedOnlineNewsList));
            }
        } catch (Exception ignored) {
        }
        ReadArticleStore.listCachedAsync(
                this,
                listRead -> {
                    if (isFinishing() || gen != homeBookmarksReloadGen) return;
                    if (!isOnlineReadFlipMode()) return;
                    if (list == null) {
                        list = new ArrayList<>();
                    } else {
                        list.clear();
                    }
                    try {
                        ArrayList<ModelClass.Datum> withPins =
                                ReadArticleStore.buildReadListWithStickies(
                                        listRead, stickiesCopy);
                        list.addAll(withPins);
                    } catch (Exception e) {
                        if (listRead != null) {
                            list.addAll(listRead);
                        }
                    }
                    // Deep-link land: ensure the exact article is in the Read deck.
                    if (landPostId > 0) {
                        int existing = findListIndexForPostId(landPostId);
                        if (existing < 0) {
                            ModelClass.Datum pin = landDatum;
                            if (pin == null) {
                                pin = ReadArticleStore.getCached(
                                        getApplicationContext(), landPostId);
                            }
                            if (pin != null && pin.getPostId() != null) {
                                list.add(0, pin);
                            }
                        }
                    }
                    // Ads at playstore-aligned cadence (every 4 reals) on Read flip too,
                    // independent of news-vs-read filtering — same template as News.
                    try {
                        ModelClass.Datum adTpl = onlineNewsAdTemplate;
                        if (adTpl == null && hasSavedOnlineNewsSession) {
                            adTpl = ReadArticleStore.firstAdTemplate(savedOnlineNewsList);
                            if (adTpl != null) {
                                onlineNewsAdTemplate = adTpl;
                            }
                        }
                        ReadArticleStore.rebalanceFeedSlots(list, adTpl != null
                                ? adTpl
                                : onlineNewsAdTemplate);
                    } catch (Exception ignored) {
                    }
                    int landIdx = landPostId > 0
                            ? findListIndexForPostId(landPostId) : -1;
                    if (landIdx < 0) {
                        landIdx = 0;
                    }
                    positionx = landIdx;
                    ensureHomeFlipAdapterBoundAndData(list);
                    try {
                        hideHomeFeedLoadingReveal(true);
                        if (layout_with_data != null) {
                            layout_with_data.setVisibility(View.VISIBLE);
                        }
                        if (comment_page != null) {
                            comment_page.setVisibility(View.GONE);
                        }
                        if (mFlipView != null) {
                            mFlipView.setVisibility(View.VISIBLE);
                            if (list != null && !list.isEmpty()) {
                                final int flipPage = Math.max(0,
                                        Math.min(landIdx, list.size() - 1));
                                flipToDeepLinkPage(flipPage);
                            }
                        }
                        if (txtNoData != null) {
                            txtNoData.setVisibility(
                                    (list == null || list.isEmpty())
                                            ? View.VISIBLE
                                            : View.GONE);
                        }
                    } catch (Exception ignored) {
                    }
                    refreshHomeReadNewsBadge();
                    ensureOnlineBottomNavVisible();
                    try {
                        refreshHomeModeToggleUi();
                    } catch (Exception ignored) {
                    }
                });
    }

    /**
     * @deprecated online Read uses {@link #paintOnlineReadNewsFlip()}; kept for call-site safety.
     */
    private void reloadHomeReadNewsPanel() {
        if (isOnlineMainNewsChromeActive()) {
            paintOnlineReadNewsFlip();
            return;
        }
        // Offline path should not use this; fall through empty.
        final int gen = ++homeBookmarksReloadGen;
        ReadArticleStore.listCachedAsync(
                this,
                listRead -> {
                    if (isFinishing() || gen != homeBookmarksReloadGen) return;
                    homeBookmarkItems.clear();
                    if (listRead != null) {
                        homeBookmarkItems.addAll(listRead);
                    }
                    if (homeBookmarksAdapter != null) {
                        homeBookmarksAdapter.notifyDataSetChanged();
                    }
                });
    }

    private class HomeBookmarksAdapter extends RecyclerView.Adapter<HomeBookmarksAdapter.VH> {

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v =
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_bookmark, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            ModelClass.Datum item = homeBookmarkItems.get(position);
            holder.title.setText(item.getTitle() != null ? item.getTitle() : "");
            String meta = "";
            if (!TextUtils.isEmpty(item.getWriterName())) {
                meta = item.getWriterName();
            }
            if (!TextUtils.isEmpty(item.getPost_date_time())) {
                meta =
                        TextUtils.isEmpty(meta)
                                ? item.getPost_date_time()
                                : meta + " · " + item.getPost_date_time();
            }
            holder.meta.setText(meta);

            String imageUrl = item.getThumbnail();
            if (TextUtils.isEmpty(imageUrl)
                    && item.getImages() != null
                    && !item.getImages().isEmpty()) {
                imageUrl = item.getImages().get(0);
            }
            if (TextUtils.isEmpty(imageUrl)) {
                imageUrl = item.getBackgroundImage();
            }
            if (!TextUtils.isEmpty(imageUrl)) {
                Glide.with(holder.image.getContext())
                        .load(imageUrl)
                        .centerCrop()
                        .into(holder.image);
            } else {
                holder.image.setImageDrawable(null);
            }

            holder.itemView.setOnClickListener(
                    v -> {
                        Intent intent =
                                new Intent(HomePage.this, BookmarkReaderActivity.class);
                        long postId = item.getPostId() != null ? item.getPostId() : 0L;
                        intent.putExtra(BookmarksActivity.EXTRA_POST_ID, postId);
                        startActivity(intent);
                    });

            final boolean onlineReadPanel =
                    isDeviceOnline() && !offlineChromeMode && bookmarksModeSelected;
            if (onlineReadPanel) {
                holder.remove.setVisibility(View.GONE);
                holder.remove.setOnClickListener(null);
            } else {
                holder.remove.setVisibility(View.VISIBLE);
                holder.remove.setOnClickListener(
                        v ->
                                BookmarkStore.toggle(
                                        HomePage.this,
                                        item,
                                        (bookmarked, toastMsg) -> {
                                            if (toastMsg != null) {
                                                Toast.makeText(
                                                                HomePage.this,
                                                                getString(R.string.bookmark_removed),
                                                                Toast.LENGTH_SHORT)
                                                        .show();
                                            }
                                            reloadHomeBookmarksPanel();
                                            refreshHomeBookmarksBadge();
                                        }));
            }
        }

        @Override
        public int getItemCount() {
            return homeBookmarkItems.size();
        }

        class VH extends RecyclerView.ViewHolder {
            final ImageView image;
            final TextView title;
            final TextView meta;
            final ImageView remove;

            VH(@NonNull View itemView) {
                super(itemView);
                image = itemView.findViewById(R.id.bookmark_item_image);
                title = itemView.findViewById(R.id.bookmark_item_title);
                meta = itemView.findViewById(R.id.bookmark_item_meta);
                remove = itemView.findViewById(R.id.bookmark_item_remove);
            }
        }
    }

    /**
     * Footer tab titles: first tab always user-facing "News" (never Reload/Home pack
     * wording); Special / profile from translator when present. Video News uses EN/TE.
     * Bookmarks stay on the top pill only (no footer label).
     * Internal action ids stay navigation_home / performFeedReload.
     */
    private void applyFooterTabLabelsFromTranslator() {
        if (bottomNavigationView == null || bottomNavigationView.getMenu() == null) return;
        try {
            LanguageModelResponse.Data d =
                    languageModelResponse != null ? languageModelResponse.getData() : null;
            android.view.Menu menu = bottomNavigationView.getMenu();
            MenuItem home = menu.findItem(R.id.navigation_home);
            if (home != null) {
                // Prefer pack only when it already means News; map Reload/Home → News.
                String reload = d != null ? d.getReload() : null;
                String homePack = d != null ? d.getHome() : null;
                String newsLabel = com.anyNews.anynews.Utils.NewsFooterLabel.resolveDisplay(
                        this, reload, homePack);
                home.setTitle(newsLabel);
                navigation_home_text = newsLabel;
            }
            MenuItem special = menu.findItem(R.id.navigation_catagiries);
            if (special != null) {
                String events = d != null ? d.getEvents() : null;
                special.setTitle(footerLabelOr(events, getString(R.string.special_badge)));
            }
            MenuItem videoNews = menu.findItem(R.id.navigation_status);
            if (videoNews != null) {
                videoNews.setTitle(resolveVideoNewsFooterLabel());
            }
            MenuItem profile = menu.findItem(R.id.navigation_profile);
            if (profile != null) {
                String p = d != null ? d.getProfile() : null;
                profile.setTitle(footerLabelOr(p, "Profile"));
            }
            // setTitle can trigger Material item rebinds that re-surface hidden tabs
            // while Offline News pack chrome is up — re-apply offline menu visibility.
            if (!isDeviceOnline()) {
                applyFooterSlotVisibility();
                forceFooterItemViewsMatchMenuVisibility();
            }
        } catch (Exception e) {
            showLog("applyFooterTabLabelsFromTranslator: " + e.getMessage());
        }
    }

    /** English "Video News"; Telugu language_id=3 → short "వీడియో". */
    private String resolveVideoNewsFooterLabel() {
        try {
            int langId = SharedPrefsData.getInt(
                    getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME);
            if (langId == 3) {
                return getString(R.string.video_news_te);
            }
        } catch (Exception ignored) {
        }
        return getString(R.string.video_news);
    }

    private static String footerLabelOr(String translator, String fallback) {
        if (translator != null) {
            String t = translator.trim();
            if (!t.isEmpty() && !"null".equalsIgnoreCase(t)) return t;
        }
        return fallback;
    }

    /**
     * Admin + offline-aware home footer slots.
     * Online: News (Reload) / Special / center + / Video News / Profile per Root_Settings flags.
     * Offline: only News (Reload); hide Special, center +, Video News, and Profile.
     * Bookmarks stay on the top News|Bookmarks pill (never a footer item).
     * {@link #rebalanceFooterTabs()} equal-weights whichever items remain visible
     * (single News tab gets full bar / weight 1).
     */
    private void applyFooterSlotVisibility() {
        if (bottomNavigationView == null || bottomNavigationView.getMenu() == null) {
            return;
        }
        try {
            android.view.Menu menu = bottomNavigationView.getMenu();
            boolean online = isDeviceOnline();

            // Offline chrome: exactly News (Reload) when admin allows it.
            // Special / Profile / Video / center + never appear offline — not even briefly.
            boolean showReload = ProfileHubConfigHelper.isFooterShowReload(this);
            boolean showSpecial = online && ProfileHubConfigHelper.isFooterShowSpecial(this);
            boolean showPlus = online && ProfileHubConfigHelper.isFooterShowPlus(this);
            boolean showVideos = online && ProfileHubConfigHelper.isFooterShowVideos(this);
            boolean showProfile = online && ProfileHubConfigHelper.isFooterShowProfile(this);

            setFooterMenuItemVisible(menu.findItem(R.id.navigation_home), showReload);
            setFooterMenuItemVisible(menu.findItem(R.id.navigation_catagiries), showSpecial);
            setFooterMenuItemVisible(menu.findItem(R.id.navigation_stricker), showPlus);
            if (!showPlus) {
                hideCenterPlusOverlays();
            }
            setFooterMenuItemVisible(menu.findItem(R.id.navigation_status), showVideos);
            setFooterMenuItemVisible(menu.findItem(R.id.navigation_profile), showProfile);

            // MenuItem.setVisible alone can leave Material item views painted; force GONE.
            forceFooterItemViewsMatchMenuVisibility();

            if (!showSpecial) {
                try {
                    stopSpecialTabBlinkAnimation();
                    if (specialTabBulbOverlay != null) {
                        specialTabBulbOverlay.setVisibility(View.GONE);
                    }
                } catch (Exception ignored) {
                }
            }

            // If a now-hidden tab was selected (Special / + / Video / Profile), pin selection
            // to News so the offline single-tab bar has a clear primary selection.
            if (!online && showReload) {
                int selectedId = bottomNavigationView.getSelectedItemId();
                if (selectedId == R.id.navigation_catagiries
                        || selectedId == R.id.navigation_stricker
                        || selectedId == R.id.navigation_status
                        || selectedId == R.id.navigation_profile) {
                    try {
                        suppressBottomNavCallbacks = true;
                        bottomNavigationView.setSelectedItemId(R.id.navigation_home);
                    } catch (Exception ignored) {
                    } finally {
                        suppressBottomNavCallbacks = false;
                    }
                }
            }
        } catch (Exception e) {
            showLog("applyFooterSlotVisibility: " + e.getMessage());
        }
    }

    private void setFooterMenuItemVisible(@Nullable MenuItem item, boolean visible) {
        if (item != null) {
            item.setVisible(visible);
            item.setEnabled(visible);
        }
    }

    /**
     * Material BottomNavigation can keep item views VISIBLE after MenuItem.setVisible(false).
     * Force each item view (and its weight) to match the MenuItem's visible flag.
     */
    private void forceFooterItemViewsMatchMenuVisibility() {
        if (bottomNavigationView == null) {
            return;
        }
        try {
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
                if (data == null) {
                    continue;
                }
                boolean wantVisible = data.isVisible();
                child.setVisibility(wantVisible ? View.VISIBLE : View.GONE);
                ViewGroup.LayoutParams lp = child.getLayoutParams();
                if (lp instanceof LinearLayout.LayoutParams) {
                    LinearLayout.LayoutParams llp = (LinearLayout.LayoutParams) lp;
                    if (wantVisible) {
                        llp.width = 0;
                        llp.weight = 1f;
                    } else {
                        llp.width = 0;
                        llp.weight = 0f;
                    }
                    child.setLayoutParams(llp);
                }
            }
        } catch (Exception e) {
            showLog("forceFooterItemViewsMatchMenuVisibility: " + e.getMessage());
        }
    }

    /** Online + admin: Special tab may show. Offline always false. */
    private boolean isFooterSpecialTabAllowed() {
        try {
            return isDeviceOnline() && ProfileHubConfigHelper.isFooterShowSpecial(this);
        } catch (Exception e) {
            return false;
        }
    }

    /** Force-hide center FAB + magazine/WA overlays when the + slot is off (admin or offline). */
    private void hideCenterPlusOverlays() {
        try {
            closeCenterPlusMenu(false);
            if (floatingActionButtonHome != null) {
                floatingActionButtonHome.setVisibility(View.INVISIBLE);
                try {
                    floatingActionButtonHome.hide();
                } catch (Exception ignored) {
                }
            }
            if (imageVIewShareWhatsapp != null) {
                imageVIewShareWhatsapp.setVisibility(View.GONE);
            }
            if (layoutMagazineShareFooter != null) {
                layoutMagazineShareFooter.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            showLog("hideCenterPlusOverlays: " + e.getMessage());
        }
    }

    /** True when center + create-menu overlay is expanded (X shown). */
    private boolean isCenterPlusMenuOpen() {
        if (isOpen) {
            return true;
        }
        try {
            if (post_news_scrren != null && post_news_scrren.getVisibility() == View.VISIBLE) {
                return true;
            }
            if (floating_action_button != null && floating_action_button.isShown()) {
                return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    /**
     * Center FAB glyph: swap full drawables (+ vs X). Never leave a non-zero rotation
     * residual — rotating {@code ic_repoter_post} created a faint leftover stroke.
     */
    private void applyCenterPlusExpandedIcon(boolean expanded) {
        if (floatingActionButtonHome == null) {
            return;
        }
        try {
            floatingActionButtonHome.clearAnimation();
            floatingActionButtonHome.setRotation(0f);
            floatingActionButtonHome.setImageResource(
                    expanded ? R.drawable.ic_repoter_close : R.drawable.ic_repoter_post);
        } catch (Exception e) {
            showLog("applyCenterPlusExpandedIcon: " + e.getMessage());
        }
    }

    /**
     * Collapse reporter create-menu: hide overlay, restore feed, center icon back to +.
     *
     * @param animate when true, play satellite FAB close animation briefly
     */
    private void closeCenterPlusMenu(boolean animate) {
        try {
            if (countDownTimer != null) {
                countDownTimer.cancel();
                countDownTimer = null;
            }
        } catch (Exception ignored) {
        }
        boolean wasOpen = isCenterPlusMenuOpen();
        isOpen = false;
        try {
            if (animate && wasOpen) {
                try {
                    if (fab1 != null && fab_close != null) fab1.startAnimation(fab_close);
                    if (fab2 != null && fab_close != null) fab2.startAnimation(fab_close);
                    if (fab4 != null && fab_close != null) fab4.startAnimation(fab_close);
                } catch (Exception ignored) {
                }
            }
            if (floating_action_button != null) {
                floating_action_button.setVisibility(View.GONE);
            }
            if (post_news_scrren != null) {
                post_news_scrren.setVisibility(View.GONE);
            }
            if (mFlipView != null) {
                mFlipView.setVisibility(View.VISIBLE);
            }
            if (top_item_layout != null) {
                top_item_layout.setVisibility(View.GONE);
            }
            applyCenterPlusExpandedIcon(false);
        } catch (Exception e) {
            showLog("closeCenterPlusMenu: " + e.getMessage());
            isOpen = false;
            applyCenterPlusExpandedIcon(false);
        }
    }

    /** Expand reporter create-menu from center + (caller already verified login). */
    private void openCenterPlusMenu() {
        try {
            isOpen = true;
            applyCenterPlusExpandedIcon(true);
            if (txtNoData != null) {
                txtNoData.setVisibility(View.GONE);
            }
            if (mFlipView != null) {
                mFlipView.setVisibility(View.GONE);
            }
            if (post_news_scrren != null) {
                post_news_scrren.setVisibility(View.VISIBLE);
            }
            if (floating_action_button != null) {
                floating_action_button.setVisibility(View.VISIBLE);
            }
            if (top_item_layout != null) {
                top_item_layout.setVisibility(View.GONE);
            }
            try {
                if (fab1 != null && fab_open != null) fab1.startAnimation(fab_open);
                if (fab2 != null && fab_open != null) fab2.startAnimation(fab_open);
                if (fab4 != null && fab_open != null) fab4.startAnimation(fab_open);
            } catch (Exception ignored) {
            }
            applyReporterPostTypeVisibility();
        } catch (Exception e) {
            showLog("openCenterPlusMenu: " + e.getMessage());
        }
    }

    /**
     * Admin-driven footer slots: apply visibility (admin + offline filter), then force
     * remaining visible tabs to share width evenly. Repositions the floating center
     * +/Share/WA overlays onto the FAB menu slot.
     */
    private void rebalanceFooterTabs() {
        if (bottomNavigationView == null) {
            return;
        }
        try {
            applyFooterSlotVisibility();
            View menuChild = bottomNavigationView.getChildAt(0);
            if (!(menuChild instanceof BottomNavigationMenuView)) {
                bottomNavigationView.requestLayout();
                return;
            }
            BottomNavigationMenuView menuView = (BottomNavigationMenuView) menuChild;
            menuView.setClipChildren(false);
            menuView.setClipToPadding(false);
            // Align item views with MenuItem visibility before weighting.
            forceFooterItemViewsMatchMenuVisibility();
            int visibleCount = 0;
            for (int i = 0; i < menuView.getChildCount(); i++) {
                View child = menuView.getChildAt(i);
                if (child != null && child.getVisibility() != View.GONE) {
                    visibleCount++;
                }
            }
            if (visibleCount <= 0) {
                return;
            }
            for (int i = 0; i < menuView.getChildCount(); i++) {
                View child = menuView.getChildAt(i);
                if (child == null) continue;
                if (child.getVisibility() == View.GONE) {
                    ViewGroup.LayoutParams lp = child.getLayoutParams();
                    if (lp instanceof LinearLayout.LayoutParams) {
                        LinearLayout.LayoutParams llp = (LinearLayout.LayoutParams) lp;
                        llp.width = 0;
                        llp.weight = 0f;
                        child.setLayoutParams(llp);
                    }
                    continue;
                }
                ViewGroup.LayoutParams lp = child.getLayoutParams();
                if (lp instanceof LinearLayout.LayoutParams) {
                    LinearLayout.LayoutParams llp = (LinearLayout.LayoutParams) lp;
                    llp.width = 0;
                    llp.weight = 1f;
                    child.setLayoutParams(llp);
                }
            }
            menuView.requestLayout();
            bottomNavigationView.requestLayout();
            // Re-apply admin icon size / label sizes / tints after item-view rebuild.
            FooterStyleHelper.apply(HomePage.this, bottomNavigationView);
            // Weights / visibility changes rebuild item views — re-pin Special + Profile glyphs
            // only when their slots are allowed (never while offline).
            ensureSpecialMenuIconAlwaysVisible();
            // Reposition after weights measure; size FAB from admin toggle as well.
            if (ProfileHubConfigHelper.isFooterShowPlus(this) && isDeviceOnline()) {
                FooterStyleHelper.applyFab(HomePage.this, floatingActionButtonHome);
            } else {
                hideCenterPlusOverlays();
            }
            bottomNavigationView.post(this::repositionCenterFabForVisibleTabs);
            bottomNavigationView.postDelayed(this::repositionCenterFabForVisibleTabs, 80);
            // Offline: never re-setup Special bulb / force-show Special menu.
            // Material can rebuild item views after setVisible/weight changes — re-hide
            // Special/Video/Profile/+ on several frames so News-only offline sticks.
            if (isFooterSpecialTabAllowed()) {
                bottomNavigationView.post(this::setupSpecialTabBulb);
                bottomNavigationView.postDelayed(this::ensureSpecialMenuIconAlwaysVisible, 120);
            } else {
                bottomNavigationView.post(this::forceFooterItemViewsMatchMenuVisibility);
                bottomNavigationView.postDelayed(this::forceFooterItemViewsMatchMenuVisibility, 120);
                bottomNavigationView.postDelayed(this::forceFooterItemViewsMatchMenuVisibility, 320);
                bottomNavigationView.postDelayed(() -> {
                    try {
                        if (!isFooterSpecialTabAllowed()) {
                            applyFooterSlotVisibility();
                            forceFooterItemViewsMatchMenuVisibility();
                        }
                    } catch (Exception ignored) {
                    }
                }, 480);
            }
        } catch (Exception e) {
            showLog("rebalanceFooterTabs: " + e.getMessage());
        }
    }

    /**
     * Centers the floating + FAB (and Share/WA overlays) over the empty
     * {@code navigation_stricker} menu slot — not the absolute bar center.
     * When Categories is hidden, equal weights leave that slot off geometric center;
     * translationX aligns the FAB with the visible-tab grid.
     */
    private void repositionCenterFabForVisibleTabs() {
        if (bottomNavigationView == null) {
            return;
        }
        try {
            View fabSlot = findFabSlotItemView();
            if (fabSlot == null || fabSlot.getWidth() <= 0 || bottomNavigationView.getWidth() <= 0) {
                // Layout not ready — retry once.
                bottomNavigationView.postDelayed(() -> {
                    try {
                        View slot = findFabSlotItemView();
                        applyCenterOverlayTranslation(slot);
                    } catch (Exception ignored) {
                    }
                }, 60);
                return;
            }
            applyCenterOverlayTranslation(fabSlot);
        } catch (Exception e) {
            showLog("repositionCenterFabForVisibleTabs: " + e.getMessage());
        }
    }

    private void applyCenterOverlayTranslation(@Nullable View fabSlot) {
        if (bottomNavigationView == null) {
            return;
        }
        float dx = 0f;
        if (fabSlot != null && fabSlot.getWidth() > 0 && bottomNavigationView.getWidth() > 0) {
            int[] navLoc = new int[2];
            int[] slotLoc = new int[2];
            bottomNavigationView.getLocationOnScreen(navLoc);
            fabSlot.getLocationOnScreen(slotLoc);
            float slotCenterX = slotLoc[0] + fabSlot.getWidth() / 2f;
            float navCenterX = navLoc[0] + bottomNavigationView.getWidth() / 2f;
            dx = slotCenterX - navCenterX;
        }
        if (floatingActionButtonHome != null) {
            floatingActionButtonHome.setTranslationX(dx);
        }
        if (layoutMagazineShareFooter != null) {
            layoutMagazineShareFooter.setTranslationX(dx);
        }
        if (imageVIewShareWhatsapp != null) {
            imageVIewShareWhatsapp.setTranslationX(dx);
        }
    }

    /** Empty center menu item that reserves the + FAB track in the bottom nav. */
    @Nullable
    private View findFabSlotItemView() {
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
                    && itemView.getItemData().getItemId() == R.id.navigation_stricker) {
                return itemView;
            }
        }
        return null;
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
        // Online: never collapse the bottom tabs (ads/share used to GONE it → white bar).
        if (isDeviceOnline() && !offlineChromeMode) {
            ensureOnlineBottomNavVisible();
            return;
        }
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
        cancelVideoAutoplayChain();
        cancelVideoAutoplayStart();
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
                scheduleVideoFooterAutoplayStart(positionx);
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
            // Always re-sync UI; CONNECTIVITY_ACTION can be delayed/missed while
            // screen is off, so do not rely only on edge transitions.
            syncNetworkOverlay(false);
        }
    }

    /**
     * True when the device reports an active connected network (has Internet path).
     * Prefer activeNetwork + capabilities on modern APIs; fall back to legacy scan.
     * Used for offline UI mode (pill, footer, feed paint guards).
     */
    private boolean isDeviceOnline() {
        try {
            ConnectivityManager connectivity = (ConnectivityManager)
                    getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity == null) {
                return false;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                android.net.Network network = connectivity.getActiveNetwork();
                if (network == null) {
                    return false;
                }
                android.net.NetworkCapabilities caps = connectivity.getNetworkCapabilities(network);
                return caps != null
                        && caps.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        && (caps.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI)
                        || caps.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR)
                        || caps.hasTransport(android.net.NetworkCapabilities.TRANSPORT_ETHERNET)
                        || caps.hasTransport(android.net.NetworkCapabilities.TRANSPORT_VPN));
            }
            NetworkInfo active = connectivity.getActiveNetworkInfo();
            if (active != null && active.isConnected()) {
                return true;
            }
            return MyUtils.isNetworkAvailable(this);
        } catch (Exception e) {
            try {
                return MyUtils.isNetworkAvailable(this);
            } catch (Exception ignored) {
                return false;
            }
        }
    }

    /**
     * True when the default network can actually reach the Internet.
     * Prefer {@link NetworkCapabilities#NET_CAPABILITY_VALIDATED}; fall back to
     * {@link #isDeviceOnline()} when VALIDATED is unavailable (pre-M / odd OEMs).
     */
    private boolean isDeviceNetworkValidated() {
        try {
            ConnectivityManager connectivity = (ConnectivityManager)
                    getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity == null) {
                return false;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Network network = connectivity.getActiveNetwork();
                if (network == null) {
                    return false;
                }
                NetworkCapabilities caps = connectivity.getNetworkCapabilities(network);
                if (caps == null
                        || !caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                    return false;
                }
                // Validated = system confirmed real Internet (not captive portal only).
                if (caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                    return true;
                }
                // Some devices delay or omit VALIDATED briefly after onAvailable;
                // still allow feed restore if a clear transport is present.
                return caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                        || caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                        || caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                        || caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN);
            }
            return isDeviceOnline();
        } catch (Exception e) {
            return isDeviceOnline();
        }
    }

    /** Wire thrifty offline pack fills to Home feed state (main only). */
    private void bindOfflinePackPrefetcherHost() {
        try {
            OfflineFeedStore.setPackChangeListener(
                    new OfflineFeedStore.PackChangeListener() {
                        @Override
                        public void onOfflinePackChanged() {
                            try {
                                runOnUiThread(
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    // Route through counts so online main never
                                                    // repaints Offline pack count on News badge.
                                                    refreshHomeModeCounts();
                                                } catch (Exception ignored) {
                                                }
                                            }
                                        });
                            } catch (Exception ignored) {
                            }
                        }
                    });
            OfflinePackPrefetcher.get().bindHost(new OfflinePackPrefetcher.Host() {
                @Override
                public boolean isDeviceOnline() {
                    return HomePage.this.isDeviceOnline();
                }

                @Override
                public boolean isSpecialMode() {
                    return HomePage.this.isSpecialFeedMode();
                }

                @Override
                public boolean isStickyFeedOpen() {
                    return HomePage.this.isStickyFeedOpen();
                }

                @Override
                public boolean isMainFeedBusy() {
                    // Avoid racing user-visible List_All_Articles / held load-more.
                    return isApiRequested;
                }

                @Override
                public boolean isHostResumed() {
                    return homeActivityResumed;
                }

                @Override
                public int getCategoryId() {
                    return category_id;
                }

                @Override
                public String getDataFrom2g() {
                    return DATAFROM2G;
                }

                @Override
                public int getScreenWidth() {
                    try {
                        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                        return displayMetrics.widthPixels;
                    } catch (Exception e) {
                        return 0;
                    }
                }

                @Override
                public int getScreenHeight() {
                    try {
                        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                        return displayMetrics.heightPixels;
                    } catch (Exception e) {
                        return 0;
                    }
                }

                @Override
                public void offerHeldNextPageIfUseful(
                        long skip,
                        long lastPostId,
                        long actualSkip,
                        @NonNull EncreptionRequest body) {
                    // Prefetch already merges JSON into OfflineFeedStore.
                    // If Home has not held a next page yet and cursors match the visible
                    // feed cursor, reuse body as the held response so load-more does not
                    // re-request (thrift). Requires Response wrapper — only when dummy empty
                    // and cursors align we still leave UI path alone (offline pack is enough).
                    // No-op for response type constraints; OfflinePackPrefetcher already merged.
                }
            });
        } catch (Exception e) {
            Logone.w(e, "bindOfflinePackPrefetcherHost");
        }
    }

    /**
     * Home chrome for offline: always keep {@link #yes_internet} (feed + News footer +
     * Offline News|Bookmarks pill). Never use the full-screen {@link #no_internet}
     * blocker — that hid footer/pill even when OfflineFeedStore had a pack.
     */
    private void hideBlockingNoInternetOverlay() {
        try {
            if (yes_internet != null) {
                yes_internet.setVisibility(View.VISIBLE);
            }
            if (no_internet != null) {
                no_internet.setVisibility(View.GONE);
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * True when Offline News pack, OpenPath page-0 disk (incl. stale for offline),
     * or an in-memory main feed is available to keep Home usable offline.
     */
    private boolean hasOfflineMainContent(boolean includeInMemoryList) {
        try {
            if (OfflineFeedStore.hasUsable(this, false)) {
                return true;
            }
        } catch (Exception ignored) {
        }
        try {
            if (OpenPathCache.hasFeedDisk(this)
                    || OpenPathCache.loadFeedForOffline(this, 0) != null) {
                return true;
            }
        } catch (Exception ignored) {
        }
        return includeInMemoryList && list != null && !list.isEmpty();
    }

    /**
     * Show or hide offline vs online Home chrome to match current connectivity.
     * Offline with any offline pack / OpenPath disk / live list → paint Offline News
     * (never full-screen “No internet!”). Empty pack → Offline News chrome + empty
     * messaging inside the feed (footer News still visible).
     * Special is online-only while offline: never paint special cache into Special.
     * When recovering from offline (especially on resume after lock/unlock), soft-sync
     * live data without splash / StartingActivity — stay on Home.
     *
     * @param fromLifecycle true when called from onResume / foreground; enables reload
     */
    private void syncNetworkOverlay(boolean fromLifecycle) {
        try {
            if (no_internet == null || yes_internet == null) {
                return;
            }
            boolean online = isDeviceOnline();
            if (online) {
                boolean overlayWasShowing = no_internet.getVisibility() == View.VISIBLE;
                boolean recovering = !isConnected || overlayWasShowing || pendingOfflineToOnlineRestore;
                Logone.v(LOG_TAG, "Now you are connected to Internet! recovering=" + recovering
                        + " overlayWasShowing=" + overlayWasShowing
                        + " pendingRestore=" + pendingOfflineToOnlineRestore
                        + " fromLifecycle=" + fromLifecycle);
                applyOnlineChromeUi();
                boolean wasOffline = !isConnected;
                isConnected = true;
                if (wasOffline || overlayWasShowing || pendingOfflineToOnlineRestore) {
                    // Single debounced path — no splash, stay on Home.
                    scheduleNetworkRestoredFromOffline("syncOverlay");
                }
            } else {
                Logone.v(LOG_TAG, "You are not connected to Internet!");
                isConnected = false;
                pendingOfflineToOnlineRestore = true;
                // Mid-session offline: chrome hide-on-tap uses this flag, not only live caps.
                offlineChromeMode = true;
                homeModeChromeUserHidden = false;
                // Cancel any scheduled restore flush while still offline.
                cancelPendingNetworkRestore();
                // Offline footer is only News — leave Special / Video News modes
                // (those tabs + Profile are hidden) so content matches News.
                boolean wasSpecial = isSpecialFeedMode();
                boolean wasVideoFeed = isVideoFeedMode();
                if (wasSpecial || wasVideoFeed) {
                    setFeedModeFlags(false, false);
                }
                // Always keep feed chrome — never full-screen no_internet while on Home.
                hideBlockingNoInternetOverlay();
                // Offline → show Offline News | Bookmarks pill; online hides it.
                try {
                    refreshHomeModeToggleUi();
                    refreshHomeModeCounts();
                } catch (Exception ignored) {
                }
                // Re-wire article taps so hide-on-tap works immediately without cold paint.
                try {
                    rebindOfflineChromeTapsOnVisiblePages();
                } catch (Exception ignored) {
                }
                // Main News offline only — never hydrate from special cache here.
                // Include mid-session in-memory list only when we were already on main.
                boolean hasOfflineFeed = hasOfflineMainContent(
                        !wasSpecial && !wasVideoFeed);
                if (hasOfflineFeed) {
                    // Prefer OfflineFeedStore / OpenPath when list empty or leaving
                    // Special/Video. Non-empty main list stays (no white rebind flash).
                    if (wasSpecial || wasVideoFeed || list == null || list.isEmpty()) {
                        if (!paintOfflineFeed(false, true)
                                && (list == null || list.isEmpty())
                                && !bookmarksModeSelected) {
                            showMainOfflineEmptyState();
                        }
                    } else {
                        // Kept the in-memory main list — ensure chrome taps still work.
                        try {
                            rebindOfflineChromeTapsOnVisiblePages();
                        } catch (Exception ignored) {
                        }
                    }
                } else if (!bookmarksModeSelected) {
                    // No pack / disk / list — empty Offline News chrome (not white no-internet).
                    showMainOfflineEmptyState();
                }
                // Offline footer: only News (equal weight / full bar).
                rebalanceFooterTabs();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Online UI chrome on Home (no activity restart, no splash):
     * hide no-internet overlay, switch pill to News|Read News on main feed
     * (hidden on Special/Video), exit offline bookmarks panel, restore admin footer.
     */
    private void applyOnlineChromeUi() {
        try {
            if (yes_internet != null) {
                yes_internet.setVisibility(View.VISIBLE);
            }
            if (no_internet != null) {
                no_internet.setVisibility(View.GONE);
            }
        } catch (Exception ignored) {
        }
        try {
            offlineChromeMode = false;
            homeModeChromeUserHidden = false;
            // Leave offline bookmarks list; open on main News tab of online pill.
            if (bookmarksModeSelected) {
                bookmarksModeSelected = false;
                applyHomeModeContentVisibility();
            }
            onlineNewsAllReadSkipStreak = 0;
            refreshHomeModeToggleUi();
            refreshHomeModeCounts();
            // Instant Read badge when reconnecting to online chrome.
            refreshHomeReadNewsBadge();
            ensureOnlineBottomNavVisible();
            ensureReadBadgePollRunning();
        } catch (Exception ignored) {
        }
        try {
            rebalanceFooterTabs();
        } catch (Exception ignored) {
        }
        try {
            updateCenterFab();
        } catch (Exception ignored) {
        }
    }

    /**
     * Re-install Offline pill hide-on-tap on currently attached FlipView pages
     * (needed mid-session when list is kept and getView does not rebind).
     */
    private void rebindOfflineChromeTapsOnVisiblePages() {
        if (mFlipView == null) return;
        try {
            for (int i = 0; i < mFlipView.getChildCount(); i++) {
                View child = mFlipView.getChildAt(i);
                if (child != null) {
                    FlipperAdapter.installOfflineChromeTaps(child);
                }
            }
        } catch (Exception ignored) {
        }
    }

    /** Modern NetworkCallback for same-process connectivity restore (app stays open). */
    private void registerOfflineConnectivityCallbacks() {
        try {
            ConnectivityManager cm =
                    (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) return;
            if (offlineNetworkCallback != null) {
                try {
                    cm.unregisterNetworkCallback(offlineNetworkCallback);
                } catch (Exception ignored) {
                }
            }
            offlineNetworkCallback =
                    new ConnectivityManager.NetworkCallback() {
                        @Override
                        public void onAvailable(@NonNull Network network) {
                            runOnUiThread(
                                    () -> {
                                        if (!isDeviceOnline()) return;
                                        Logone.v(LOG_TAG,
                                                "NetworkCallback onAvailable → schedule restore");
                                        // Mark online chrome immediately; feed waits for
                                        // validated settle + debounce.
                                        if (!isConnected || pendingOfflineToOnlineRestore) {
                                            applyOnlineChromeUi();
                                            isConnected = true;
                                            scheduleNetworkRestoredFromOffline("onAvailable");
                                        } else {
                                            rebalanceFooterTabs();
                                            try {
                                                updateCenterFab();
                                            } catch (Exception ignored) {
                                            }
                                        }
                                    });
                        }

                        @Override
                        public void onCapabilitiesChanged(
                                @NonNull Network network,
                                @NonNull NetworkCapabilities networkCapabilities) {
                            // Prefer VALIDATED so we load only when path is real Internet.
                            final boolean validated =
                                    networkCapabilities.hasCapability(
                                            NetworkCapabilities.NET_CAPABILITY_INTERNET)
                                            && (networkCapabilities.hasCapability(
                                                    NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                                            || networkCapabilities.hasTransport(
                                                    NetworkCapabilities.TRANSPORT_WIFI)
                                            || networkCapabilities.hasTransport(
                                                    NetworkCapabilities.TRANSPORT_CELLULAR)
                                            || networkCapabilities.hasTransport(
                                                    NetworkCapabilities.TRANSPORT_ETHERNET)
                                            || networkCapabilities.hasTransport(
                                                    NetworkCapabilities.TRANSPORT_VPN));
                            if (!validated) return;
                            runOnUiThread(
                                    () -> {
                                        if (!isDeviceNetworkValidated() && !isDeviceOnline()) {
                                            return;
                                        }
                                        if (pendingOfflineToOnlineRestore || !isConnected) {
                                            Logone.v(LOG_TAG,
                                                    "NetworkCallback onCapabilitiesChanged"
                                                            + " validated → schedule restore");
                                            applyOnlineChromeUi();
                                            isConnected = true;
                                            scheduleNetworkRestoredFromOffline(
                                                    "onCapabilitiesChanged");
                                        }
                                    });
                        }

                        @Override
                        public void onLost(@NonNull Network network) {
                            runOnUiThread(() -> {
                                // Re-check active network (may still have alternate path).
                                syncNetworkOverlay(false);
                            });
                        }
                    };
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                cm.registerDefaultNetworkCallback(offlineNetworkCallback);
            } else {
                NetworkRequest req =
                        new NetworkRequest.Builder()
                                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                                .build();
                cm.registerNetworkCallback(req, offlineNetworkCallback);
            }
        } catch (Exception e) {
            Logone.w(e, "registerOfflineConnectivityCallbacks");
        }
    }

    private void unregisterOfflineConnectivityCallbacks() {
        try {
            cancelPendingNetworkRestore();
            if (offlineNetworkCallback == null) return;
            ConnectivityManager cm =
                    (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                cm.unregisterNetworkCallback(offlineNetworkCallback);
            }
            offlineNetworkCallback = null;
        } catch (Exception ignored) {
        }
    }

    private void cancelPendingNetworkRestore() {
        try {
            if (pendingNetworkRestoreRunnable != null) {
                networkRestoreHandler.removeCallbacks(pendingNetworkRestoreRunnable);
                pendingNetworkRestoreRunnable = null;
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Coalesce flapping Wi‑Fi / dual callbacks (Broadcast + NetworkCallback) into one
     * restore flush after a short settle delay. Replaces any prior pending timer so
     * one flap storm collapses to a single flush.
     */
    private void scheduleNetworkRestoredFromOffline(@NonNull String reason) {
        pendingOfflineToOnlineRestore = true;
        cancelPendingNetworkRestore();
        final String why = reason;
        pendingNetworkRestoreRunnable =
                () -> {
                    pendingNetworkRestoreRunnable = null;
                    onNetworkRestoredFromOffline(why);
                };
        long now = SystemClock.elapsedRealtime();
        long sinceFlush = now - lastNetworkRestoreFlushMs;
        long delay = NETWORK_RESTORE_SETTLE_MS;
        if (lastNetworkRestoreFlushMs > 0
                && sinceFlush < NETWORK_RESTORE_DEBOUNCE_MS) {
            // Wait out remaining debounce so a real reconnect after a flap still lands.
            delay = NETWORK_RESTORE_DEBOUNCE_MS - sinceFlush;
            if (delay < NETWORK_RESTORE_SETTLE_MS) {
                delay = NETWORK_RESTORE_SETTLE_MS;
            }
        }
        networkRestoreHandler.postDelayed(pendingNetworkRestoreRunnable, delay);
        Logone.v(LOG_TAG, "schedule restore in " + delay + "ms reason=" + reason);
    }

    /**
     * Connectivity came back while Home is open mid-session (no splash / StartingActivity).
     * One debounced transition: online chrome → flush queues → {@link #performFeedReload()}
     * — exact same path as footer Reload (select + reselect): full page-0 reset, fresh online
     * {@code List_All_Articles}, pagination cursors ready for batches of 8.
     * Never soft-merges offline pack state (that left skip/last_post_id stuck and broke scroll).
     */
    private void onNetworkRestoredFromOffline(@Nullable String reason) {
        long now = SystemClock.elapsedRealtime();
        if (lastNetworkRestoreFlushMs > 0
                && (now - lastNetworkRestoreFlushMs) < NETWORK_RESTORE_DEBOUNCE_MS) {
            long retryIn = NETWORK_RESTORE_DEBOUNCE_MS - (now - lastNetworkRestoreFlushMs);
            if (retryIn < NETWORK_RESTORE_SETTLE_MS) {
                retryIn = NETWORK_RESTORE_SETTLE_MS;
            }
            Logone.v(LOG_TAG, "onNetworkRestoredFromOffline requeue reason=" + reason
                    + " in=" + retryIn);
            if (isDeviceOnline()) {
                final String why = reason != null ? reason : "retry";
                cancelPendingNetworkRestore();
                pendingOfflineToOnlineRestore = true;
                pendingNetworkRestoreRunnable =
                        () -> {
                            pendingNetworkRestoreRunnable = null;
                            onNetworkRestoredFromOffline(why + "_debounced");
                        };
                networkRestoreHandler.postDelayed(pendingNetworkRestoreRunnable, retryIn);
            }
            return;
        }
        if (!isDeviceOnline()) {
            Logone.v(LOG_TAG, "onNetworkRestoredFromOffline skip offline reason=" + reason);
            return;
        }
        lastNetworkRestoreFlushMs = now;
        pendingOfflineToOnlineRestore = false;
        isConnected = true;

        Logone.v(LOG_TAG, "onNetworkRestoredFromOffline RUN reason=" + reason
                + " listEmpty=" + (list == null || list.isEmpty()));

        // Drop any pending soft-merge so reconnect cannot keep offline pack pagination.
        softMergeOnNextPage0 = false;

        // Online chrome (pill gone, bookmarks panel closed, footer full).
        applyOnlineChromeUi();

        // One transition flush of offline queues.
        try {
            OfflineActionQueue.flushAll(getApplicationContext());
            BookmarkStore.flushPending(getApplicationContext());
            ViewsBatchTracker.get().flush(getApplicationContext(), false);
        } catch (Exception ignored) {
        }

        // Prefer main Reload: pin footer to navigation_home without re-entering listeners
        // (Special / Video while offline content was broken for pagination after soft merge).
        try {
            if (bottomNavigationView != null
                    && ProfileHubConfigHelper.isFooterShowReload(this)
                    && bottomNavigationView.getSelectedItemId() != R.id.navigation_home) {
                suppressBottomNavCallbacks = true;
                try {
                    bottomNavigationView.setSelectedItemId(R.id.navigation_home);
                } finally {
                    suppressBottomNavCallbacks = false;
                }
            }
        } catch (Exception e) {
            suppressBottomNavCallbacks = false;
        }

        // Same method as footer Reload tab click / reselect — prepareFreshPage0FeedCursors
        // (skip, last_post_id, requestPostition, dummyResponse, isApiRequested, list) + inlist(0).
        try {
            Logone.v(LOG_TAG,
                    "onNetworkRestoredFromOffline → performFeedReload (same as Reload footer)");
            performFeedReload();
        } catch (Exception reloadEx) {
            reloadEx.printStackTrace();
        }
    }

    /** Clear offline empty-state chrome once live feed has items. */
    private void clearOfflineOnlyEmptyStatesIfFeedReady() {
        try {
            if (list == null || list.isEmpty()) return;
            if (txtNoData != null) {
                txtNoData.setVisibility(View.GONE);
            }
            if (mFlipView != null) {
                mFlipView.setVisibility(View.VISIBLE);
            }
            if (layout_with_data != null) {
                layout_with_data.setVisibility(View.VISIBLE);
            }
            hideHomeFeedLoadingReveal(true);
            if (no_internet != null) {
                no_internet.setVisibility(View.GONE);
            }
            if (yes_internet != null) {
                yes_internet.setVisibility(View.VISIBLE);
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Special tab is online-only while offline: clear flipper items and show a clear
     * empty state. Never hydrates {@link OfflineFeedStore} special cache or bookmarks
     * into Special (and never falls through to main offline content).
     */
    private void showSpecialOfflineEmptyState() {
        try {
            showLog("showSpecialOfflineEmptyState");
            if (list == null) {
                list = new ArrayList<>();
            } else {
                list.clear();
            }
            requestSize = 0;
            hideHomeFeedLoadingReveal(true);
            if (adapter != null) {
                try {
                    // Prefer setData so adapter never keeps a stale non-empty copy.
                    adapter.setData(list);
                } catch (Exception ignored) {
                    try {
                        adapter.notifyDataSetChanged();
                    } catch (Exception ignored2) {
                    }
                }
            }
            if (mFlipView != null) {
                mFlipView.setVisibility(View.GONE);
            }
            try {
                if (comment_page != null) {
                    comment_page.setVisibility(View.GONE);
                }
            } catch (Exception ignored) {
            }
            if (layout_with_data != null) {
                layout_with_data.setVisibility(View.VISIBLE);
            }
            if (txtNoData != null) {
                txtNoData.setVisibility(View.VISIBLE);
            }
            if (nodatascreen != null) {
                nodatascreen.setText(getString(R.string.special_requires_internet));
            }
            // Keep chrome usable so user can switch to Reload offline feed / Bookmarks.
            if (yes_internet != null) {
                yes_internet.setVisibility(View.VISIBLE);
            }
            if (no_internet != null) {
                no_internet.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Logone.w(e, "showSpecialOfflineEmptyState");
        }
    }

    /**
     * Video News offline with no saved videos: empty message (pack may still have
     * non-video articles).
     */
    private void showVideoOfflineEmptyState() {
        try {
            showLog("showVideoOfflineEmptyState");
            if (list == null) {
                list = new ArrayList<>();
            } else {
                list.clear();
            }
            requestSize = 0;
            hideHomeFeedLoadingReveal(true);
            if (adapter != null) {
                try {
                    adapter.setData(list);
                } catch (Exception ignored) {
                    try {
                        adapter.notifyDataSetChanged();
                    } catch (Exception ignored2) {
                    }
                }
            }
            if (mFlipView != null) {
                mFlipView.setVisibility(View.GONE);
            }
            try {
                if (comment_page != null) {
                    comment_page.setVisibility(View.GONE);
                }
            } catch (Exception ignored) {
            }
            if (layout_with_data != null) {
                layout_with_data.setVisibility(View.VISIBLE);
            }
            if (txtNoData != null) {
                txtNoData.setVisibility(View.VISIBLE);
            }
            if (nodatascreen != null) {
                nodatascreen.setText(getString(R.string.video_news_offline_empty));
            }
            if (yes_internet != null) {
                yes_internet.setVisibility(View.VISIBLE);
            }
            if (no_internet != null) {
                no_internet.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Logone.w(e, "showVideoOfflineEmptyState");
        }
    }

    /**
     * Offline Video News: filter main offline pack to half/full video posts only.
     * @return true when at least one video item was painted
     */
    private boolean paintOfflineVideoFeed() {
        try {
            java.util.List<ModelClass.Datum> offline = OfflineFeedStore.loadForOfflinePaint(this);
            ArrayList<ModelClass.Datum> videos = new ArrayList<>();
            if (offline != null) {
                for (ModelClass.Datum d : offline) {
                    if (d != null && isVideoNewsPostType(d.getPostType())) {
                        videos.add(d);
                    }
                }
            }
            if (videos.isEmpty()) {
                EncreptionRequest cached = OpenPathCache.loadFeed(this, 0);
                if (cached == null) {
                    cached = OpenPathCache.loadFeedForOffline(this, 0);
                }
                if (cached != null) {
                    convertListOfArticles(cached, 0, true);
                    filterListToVideoPostsOnly(list);
                    if (list != null && !list.isEmpty()) {
                        rebindFlipperAdapterWithCurrentList();
                        if (txtNoData != null) txtNoData.setVisibility(View.GONE);
                        if (mFlipView != null) mFlipView.setVisibility(View.VISIBLE);
                        if (layout_with_data != null) layout_with_data.setVisibility(View.VISIBLE);
                        return true;
                    }
                }
                return false;
            }
            list = videos;
            requestSize = list.size();
            positionx = 0;
            lastSettledFlipIndex = -1;
            rebindFlipperAdapterWithCurrentList();
            if (txtNoData != null) txtNoData.setVisibility(View.GONE);
            if (mFlipView != null) mFlipView.setVisibility(View.VISIBLE);
            if (layout_with_data != null) layout_with_data.setVisibility(View.VISIBLE);
            return true;
        } catch (Exception e) {
            Logone.w(e, "paintOfflineVideoFeed");
            return false;
        }
    }

    /** Ensure FlipperAdapter exists and setData(list) after offline video paint. */
    private void rebindFlipperAdapterWithCurrentList() {
        try {
            if (type == null || type.isEmpty() || "null".equalsIgnoreCase(String.valueOf(type).trim())) {
                type = DistrictLabelHelper.resolveDisplayLabel(HomePage.this);
            }
            if (adapter == null && mFlipView != null) {
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
            if (adapter != null && list != null) {
                adapter.setData(list);
            }
        } catch (Exception e) {
            showLog("rebindFlipperAdapterWithCurrentList: " + e.getMessage());
        }
    }

    /**
     * Paint main offline pack from disk when offline (unread-first when open log exists).
     * Special offline paint is intentionally blocked — use
     * {@link #showSpecialOfflineEmptyState()} instead.
     * <p>
     * FlipperAdapter keeps its own list; after any full replace we must call
     * {@link FlipperAdapter#setData} (notify alone is not enough when HomePage
     * reassigned {@link #list} or a brand-new adapter was created empty).
     * Same path for splash cold-open offline and mid-session network drop.
     */
    private boolean paintOfflineFeed(boolean special, boolean showToast) {
        try {
            if (special) {
                // Spec: Special never surfaces offline cache / bookmarks / half articles.
                showLog("paintOfflineFeed blocked for special (online-only tab)");
                return false;
            }
            // Load fully before mutating UI so a failed load never blanks a good list.
            // Unread / not-opened first when ArticleOpenLog has ids; else pack order (newest).
            java.util.List<ModelClass.Datum> offline = OfflineFeedStore.loadForOfflinePaint(this);
            if (offline == null || offline.isEmpty()) {
                // Fall back to OpenPathCache page-0 (warm TTL first, then stale offline).
                EncreptionRequest cached = OpenPathCache.loadFeed(this, 0);
                if (cached == null) {
                    cached = OpenPathCache.loadFeedForOffline(this, 0);
                }
                if (cached != null) {
                    convertListOfArticles(cached, 0, true);
                    // Same media gate as OfflineFeedStore — never paint logo placeholders.
                    filterListToOfflineMediaReady(list);
                    if (list != null && !list.isEmpty() && adapter != null) {
                        try {
                            adapter.setData(list);
                        } catch (Exception ignored) {
                        }
                    }
                    boolean ok = list != null && !list.isEmpty();
                    if (ok) {
                        applyOfflineMainFeedChrome(showToast);
                    }
                    return ok;
                }
                return false;
            }
            // Atomic full replace into a new list, then one setData — avoid empty intermediate.
            ArrayList<ModelClass.Datum> next = new ArrayList<>(offline);
            list = next;
            requestSize = list.size();
            positionx = 0;
            lastSettledFlipIndex = -1;
            // Prefer language-matched offline/prefs district (theme recreate / cold offline).
            String offlineDistrict = OfflineFeedStore.ensureDistrictPrefs(HomePage.this);
            if (type == null || type.isEmpty() || "null".equalsIgnoreCase(String.valueOf(type).trim())
                    || !DistrictLabelHelper.labelCompatibleWithLanguage(
                            type, SharedPrefsData.getInt(HomePage.this, Constants.LanguageID, Constants.PREF_NAME))) {
                if (!TextUtils.isEmpty(offlineDistrict)) {
                    type = offlineDistrict;
                } else {
                    type = DistrictLabelHelper.resolveDisplayLabel(HomePage.this);
                }
            }
            if (!TextUtils.isEmpty(offlineDistrict)
                    || DistrictLabelHelper.isUsable(type)) {
                try {
                    // Always use language-safe resolver for header; never paint a
                    // foreign-script offline string.
                    String resolved = DistrictLabelHelper.resolveDisplayLabel(HomePage.this);
                    if (DistrictLabelHelper.isUsable(resolved)) {
                        type = resolved;
                        if (local_distic_name != null) {
                            local_distic_name.setText(resolved);
                        }
                        if (selelcted_local_distic_name != null) {
                            selelcted_local_distic_name.setText(resolved);
                        }
                    } else {
                        DistrictLabelHelper.applyTo(
                                HomePage.this, local_distic_name, selelcted_local_distic_name);
                    }
                } catch (Exception ignored) {
                }
            }
            ensureHomeFlipAdapterBoundAndData(list);
            applyOfflineMainFeedChrome(showToast);
            showLog("paintOfflineFeed special=false n=" + list.size());
            return true;
        } catch (Exception e) {
            Logone.w(e, "paintOfflineFeed");
            return false;
        }
    }

    /**
     * Ensure FlipperAdapter exists on {@link #mFlipView} and is bound to {@code data}.
     * Always uses setData so adapter count matches HomePage list after offline/reload.
     * After a successful non-empty list, also forces flip + content container visible
     * so cold open / News|Read toggle never ends on a blank white FlipView.
     */
    private void ensureHomeFlipAdapterBoundAndData(@Nullable ArrayList<ModelClass.Datum> data) {
        if (mFlipView == null) {
            return;
        }
        if (type == null || type.isEmpty() || "null".equalsIgnoreCase(String.valueOf(type).trim())
                || !DistrictLabelHelper.labelCompatibleWithLanguage(
                        type, SharedPrefsData.getInt(HomePage.this, Constants.LanguageID, Constants.PREF_NAME))) {
            String fromPrefs = OfflineFeedStore.ensureDistrictPrefs(HomePage.this);
            if (!TextUtils.isEmpty(fromPrefs)) {
                type = fromPrefs;
            } else {
                type = DistrictLabelHelper.resolveDisplayLabel(HomePage.this);
            }
        }
        boolean needNew = adapter == null || isFromLocalArticles;
        isFromLocalArticles = false;
        if (needNew) {
            adapter = new FlipperAdapter(Activity, HomePage.this, type, category_id, getLifecycle());
            adapter.setCallback(this);
            mFlipView.setAdapter(adapter);
            mFlipView.setOnFlipListener(this);
            mFlipView.setOnFlipCompleteListener(this);
            mFlipView.setOverFlipMode(OverFlipMode.GLOW);
            mFlipView.peakNext(false);
            mFlipView.setOnOverFlipListener(this);
            mFlipView.setOnFlipAppear(this);
        }
        if (adapter == null) {
            return;
        }
        int flipTo = 0;
        if (data != null && !data.isEmpty()) {
            flipTo = Math.max(0, Math.min(positionx, data.size() - 1));
        }
        adapter.setCurrentFlipPage(flipTo);
        if (data != null) {
            adapter.setData(data);
        }
        // First-paint guarantee: successful list must show the flip surface.
        if (data != null && !data.isEmpty()) {
            try {
                mFlipView.setVisibility(View.VISIBLE);
                if (layout_with_data != null) {
                    layout_with_data.setVisibility(View.VISIBLE);
                }
                if (txtNoData != null) {
                    txtNoData.setVisibility(View.GONE);
                }
                hideHomeFeedLoadingReveal(true);
                // Prefer not to open on a bare ad spinner if content exists.
                seatAwayFromUnfilledAdIfNeeded();
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Remember admin stickies from a main feed list so Read can re-apply sticky_position
     * for pins that have been session-viewed / committed into the local read store.
     */
    private void rememberFeedStickiesFrom(
            @androidx.annotation.Nullable java.util.List<ModelClass.Datum> items) {
        if (items == null || items.isEmpty()) return;
        if (isSpecialFeedMode() || isVideoFeedMode()) return;
        try {
            ArrayList<ModelClass.Datum> pins = ReadArticleStore.extractStickyArticles(items);
            if (pins.isEmpty()) return;
            java.util.LinkedHashMap<Long, ModelClass.Datum> byId = new java.util.LinkedHashMap<>();
            for (ModelClass.Datum d : rememberedFeedStickies) {
                if (d != null && d.getPostId() != null && d.getPostId() > 0) {
                    byId.put(d.getPostId(), d);
                }
            }
            for (ModelClass.Datum d : pins) {
                if (d != null && d.getPostId() != null && d.getPostId() > 0) {
                    byId.put(d.getPostId(), d);
                }
            }
            rememberedFeedStickies.clear();
            rememberedFeedStickies.addAll(byId.values());
        } catch (Exception ignored) {
        }
    }

    /** Shared chrome after main offline feed is painted (or OpenPath convert succeeded). */
    private void applyOfflineMainFeedChrome(boolean showToast) {
        hideHomeFeedLoadingReveal(true);
        hideBlockingNoInternetOverlay();
        try {
            if (layout_with_data != null) {
                layout_with_data.setVisibility(View.VISIBLE);
            }
            if (txtNoData != null) {
                txtNoData.setVisibility(View.GONE);
            }
            if (mFlipView != null) {
                mFlipView.setVisibility(View.VISIBLE);
            }
            if (comment_page != null) {
                comment_page.setVisibility(View.GONE);
            }
        } catch (Exception ignored) {
        }
        if (showToast) {
            try {
                Toast.makeText(this, R.string.offline_showing_saved_news, Toast.LENGTH_SHORT).show();
            } catch (Exception ignored) {
            }
        }
        try {
            refreshHomeModeToggleUi();
            refreshHomeModeCounts();
            rebalanceFooterTabs();
        } catch (Exception ignored) {
        }
    }

    /**
     * Page-0 cursor/sticky reset without wiping the in-memory list or FlipperAdapter.
     * Used for offline Reload so a good feed stays up until paintOfflineFeed rebinds.
     */
    private void prepareOfflineMainReloadCursors() {
        clearPendingFlipRestore();
        clearStickyPushPostId();
        SHARE_ID = 0;
        SHARE_REDIRECT_ID = "0";
        DATAFROM2G = "0";
        last_post_id = 0;
        skip = 0;
        actual_skip = 0;
        positionx = 0;
        lastSettledFlipIndex = -1;
        requestSize = 0;
        requestPostition = 1;
        isApiRequested = false;
        isDataParsed = true;
        isFromLocalArticles = true; // force Flipper rebuild on next paint
        dummyResponse = null;
        nativeGoogleAdList.clear();
        nativeFBAdList.clear();
        nativeGoogleAdArray.clear();
        nativeFBAdArray.clear();
        OpenPathCache.clearForceFeedNetwork();
        category_id = 0;
    }

    /**
     * Main Reload offline with no OfflineFeedStore / OpenPathCache — empty Offline News
     * feed chrome, not the full-screen “No internet!” layout (footer + pill stay).
     */
    private void showMainOfflineEmptyState() {
        try {
            hideHomeFeedLoadingReveal(true);
            hideBlockingNoInternetOverlay();
            if (list == null) {
                list = new ArrayList<>();
            } else {
                list.clear();
            }
            requestSize = 0;
            if (adapter != null) {
                try {
                    adapter.setData(list);
                } catch (Exception ignored) {
                }
            }
            if (mFlipView != null) {
                mFlipView.setVisibility(View.GONE);
            }
            if (layout_with_data != null) {
                layout_with_data.setVisibility(View.VISIBLE);
            }
            if (txtNoData != null) {
                txtNoData.setVisibility(View.VISIBLE);
            }
            // Keep Offline News | Bookmarks + News footer (yes_internet stays visible).
            try {
                refreshHomeModeToggleUi();
                refreshHomeModeCounts();
                rebalanceFooterTabs();
            } catch (Exception ignored) {
            }
        } catch (Exception e) {
            Logone.w(e, "showMainOfflineEmptyState");
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
        if (!isDeviceOnline()) {
            try {
                long post_id = list.get(positionx).getPostId();
                long sub_post_id = list.get(positionx).getSubPostId();
                String text = leave_comment_white.getText() != null
                        ? leave_comment_white.getText().toString()
                        : "";
                OfflineActionQueue.enqueueComment(
                        getApplicationContext(),
                        post_id,
                        sub_post_id,
                        text,
                        replyParentCommentId,
                        allsharedpreference.getEmailId());
                leave_comment_white.setText("");
                clearReplyContext();
                if (adapter != null) {
                    adapter.incrementCommentCount(positionx);
                }
                Toast.makeText(this, R.string.offline_action_queued, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
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
                            final boolean parentalOn = SharedPrefsData.getBool(
                                    HomePage.this, Constants.PARENTAL_CONTROL, Constants.PREF_NAME, false);
                            for (int i = 0; i < validateUserOtp.getData().size(); i++) {
                                ListOfCommentsResponse.Datum src = validateUserOtp.getData().get(i);
                                if (parentalOn && looksAbusiveComment(src != null ? src.getComment() : null)) {
                                    continue;
                                }
                                comment_chooseLanguage_models.add(mapCommentDatum(src));
                            }

                            if (comment_chooseLanguage_models.isEmpty()) {
                                comment_recycler_view.setVisibility(View.INVISIBLE);
                                linear_no_comment_page.setVisibility(View.VISIBLE);
                            } else {
                                bindCommentsAdapter();
                            }
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

                applyFooterTabLabelsFromTranslator();
                fab_fullpost.setText("" + languageModelResponse.getData().getFullPost());
                fab_video.setText("" + languageModelResponse.getData().getVideoPost());
                fab_article.setText("" + languageModelResponse.getData().getArticle());
                nodatascreen.setText("" + languageModelResponse.getData().getNoData());
                leave_comment_white.setHint("" + languageModelResponse.getData().getWriteComments());
                applyMagazineShareFooterLabel();

                if (category_id == 0) {
                    type = DistrictLabelHelper.resolveDisplayLabel(HomePage.this);
                } else {
                    type = languageModelResponse.getData().getMyFeed().toString();
                }
                // Programmatic chrome only — avoid re-entering Special/Video/Reload open paths.
                // Mutual exclusive: Special | Video News | News (home). Without Video branch,
                // inlist after performVideoFeedOpen forced navigation_home (re-tap → News).
                suppressBottomNavCallbacks = true;
                try {
                    if (isSpecialFeedMode()) {
                        showLog("setSelectedItemId(R.id.navigation_catagiries)");
                        bottomNavigationView.setSelectedItemId(R.id.navigation_catagiries);
                    } else if (isVideoFeedMode()) {
                        showLog("setSelectedItemId(R.id.navigation_status)");
                        bottomNavigationView.setSelectedItemId(R.id.navigation_status);
                    } else {
                        showLog("setSelectedItemId(R.id.navigation_home)");
                        bottomNavigationView.setSelectedItemId(R.id.navigation_home);
                    }
                } finally {
                    suppressBottomNavCallbacks = false;
                }

                showLog("List size:"+list.size());

                // IMPORTANT: ShowNews_Data_Values may already paint page-0 from disk
                // (sync) into list + adapter. FlipperAdapter() starts with its own
                // empty list — re-bind list after setAdapter or cache paint is wiped
                // and warm reopen can skip network → permanent blank feed.
                bindHomeFlipAdapter(type, category_id, list != null && !list.isEmpty());

            } else {
                // Translator prefs not ready: still attach adapter so network/cache paint works.
                showLog("inlist: languageModelResponse null — binding adapter without chrome labels");
                if (type == null || type.isEmpty() || "null".equalsIgnoreCase(String.valueOf(type).trim())) {
                    type = OfflineFeedStore.ensureDistrictPrefs(HomePage.this);
                    if (TextUtils.isEmpty(type)) {
                        type = DistrictLabelHelper.resolveDisplayLabel(HomePage.this);
                    }
                }
                bindHomeFlipAdapter(type, category_id, list != null && !list.isEmpty());
            }

        } catch (Exception e) {
            e.printStackTrace();
            // Guarantee adapter exists even if translator parse fails.
            try {
                if (type == null || type.isEmpty() || "null".equalsIgnoreCase(String.valueOf(type).trim())) {
                    type = OfflineFeedStore.ensureDistrictPrefs(HomePage.this);
                    if (TextUtils.isEmpty(type)) {
                        type = DistrictLabelHelper.resolveDisplayLabel(HomePage.this);
                    }
                }
                bindHomeFlipAdapter(type, category_id, list != null && !list.isEmpty());
            } catch (Exception ignored) {
            }
        }

    }

    /**
     * Attach FlipperAdapter to mFlipView and re-bind any articles already in
     * {@link #list} (e.g. OpenPathCache disk paint before this method ran).
     */
    private void bindHomeFlipAdapter(String feedType, int catId, boolean rebindExistingList) {
        if (mFlipView == null) {
            return;
        }
        if (feedType == null || feedType.isEmpty() || "null".equalsIgnoreCase(feedType.trim())
                || !DistrictLabelHelper.labelCompatibleWithLanguage(
                        feedType, SharedPrefsData.getInt(HomePage.this, Constants.LanguageID, Constants.PREF_NAME))) {
            feedType = OfflineFeedStore.ensureDistrictPrefs(HomePage.this);
            if (TextUtils.isEmpty(feedType)) {
                feedType = DistrictLabelHelper.resolveDisplayLabel(HomePage.this);
            }
        }
        if (feedType != null && !feedType.isEmpty() && !"null".equalsIgnoreCase(feedType.trim())) {
            type = feedType;
        }
        adapter = new FlipperAdapter(Activity, HomePage.this, feedType, catId, getLifecycle());
        adapter.setCallback(this);
        adapter.setCurrentFlipPage(positionx);
        mFlipView.setAdapter(adapter);
        mFlipView.setOnFlipListener(this);
        mFlipView.setOnFlipCompleteListener(this);
        mFlipView.setOverFlipMode(OverFlipMode.GLOW);
        mFlipView.peakNext(false);
        mFlipView.setOnOverFlipListener(this);
        mFlipView.setOnFlipAppear(this);
        if (rebindExistingList && list != null && !list.isEmpty()) {
            showLog("bindHomeFlipAdapter rebind list size=" + list.size());
            adapter.setData(list);
            mFlipView.setVisibility(View.VISIBLE);
            hideHomeFeedLoadingReveal(true);
            if (layout_with_data != null) {
                layout_with_data.setVisibility(View.VISIBLE);
            }
            if (txtNoData != null) {
                txtNoData.setVisibility(View.GONE);
            }
        }
    }

    /**
     * One-shot push deep-link: never re-send a consumed/stale push_post_id on
     * list refreshes (Reload / tab switch / pagination). Call before any
     * intentional soft feed reload so Open is not inflated.
     */
    private void clearStickyPushPostId() {
        TITLE_VALUE = 0;
        pushUpdatedID = "0";
        try {
            Intent intent = getIntent();
            if (intent != null) {
                intent.removeExtra("TITLE");
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Synchronously clear the visible FlipperAdapter (including when HomePage.adapter
     * was already nulled but FlipView still holds the old News list). Prevents first
     * News→Special paint of half cards while special_posts loads.
     */
    private void hardClearFlipFeedImmediate() {
        FlipperAdapter attached = adapter;
        try {
            if (attached == null && mFlipView != null) {
                android.widget.ListAdapter la = mFlipView.getAdapter();
                if (la instanceof FlipperAdapter) {
                    attached = (FlipperAdapter) la;
                }
            }
        } catch (Exception ignored) {
        }
        if (attached != null) {
            try {
                attached.clearAllDataImmediate();
            } catch (Exception e) {
                try {
                    attached.setData(new ArrayList<>());
                } catch (Exception ignored) {
                }
            }
        }
        if (list == null) {
            list = new ArrayList<>();
        } else {
            list.clear();
        }
        try {
            if (mFlipView != null) {
                // Detach so any residual scrap/page bitmap cannot revive News half cards.
                mFlipView.setAdapter(null);
                mFlipView.setVisibility(View.GONE);
            }
        } catch (Exception ignored) {
        }
        adapter = null;
        requestSize = 0;
    }

    /**
     * Clear pagination cursor + held next page so the next {@link #inlist}
     * call is a true page-0 fetch (main My Feed or Special). Does not change
     * {@link Constants#isUnreadPostsSelected}.
     */
    private void prepareFreshPage0FeedCursors() {
        // Invalidate any in-flight list response from a previous feed mode.
        feedModeEpoch++;
        clearPendingFlipRestore();
        clearStickyPushPostId();
        SHARE_ID = 0;
        SHARE_REDIRECT_ID = "0";
        DATAFROM2G = "0";
        last_post_id = 0;
        skip = 0;
        actual_skip = 0;
        positionx = 0;
        lastSettledFlipIndex = -1;
        requestSize = 0;
        requestPostition = 1;
        isApiRequested = false;
        isDataParsed = true;
        isFromLocalArticles = true; // force FlipperAdapter rebuild with fresh first page
        dummyResponse = null; // drop prefetched/held next page from prior feed
        // Fresh feed mode page-0: reset soft-empty skip streak (shared News/Special/Video cap).
        onlineNewsAllReadSkipStreak = 0;
        // Sync-clear FlipView adapter data BEFORE nulling HomePage.adapter (prior bug:
        // adapter=null skipped setData clear, so FlipView kept News half posts).
        hardClearFlipFeedImmediate();
        nativeGoogleAdList.clear();
        nativeFBAdList.clear();
        nativeGoogleAdArray.clear();
        nativeFBAdArray.clear();
        OpenPathCache.forceNextFeedNetwork(this);
        category_id = 0;
        try {
            hasSavedOnlineNewsSession = false;
            savedOnlineNewsList.clear();
        } catch (Exception ignored) {
        }
    }

    /**
     * Enter Special as a fresh list: page 0, skip=0, special_posts=true,
     * flip from index 0. Call before inlist when opening Special (tab or intent).
     * <p>
     * Mode flag is set <b>before</b> clear/request so strip/getView never see News mode
     * on the first News→Special transition frame.
     */
    private void prepareSpecialFeedFromTop() {
        showLog("prepareSpecialFeedFromTop");
        // Mode first — any concurrent getView/setData strip uses Special immediately.
        softMergeOnNextPage0 = false;
        setFeedModeFlags(true, false);
        prepareFreshPage0FeedCursors();
        // prepareFresh also bumps epoch again after mode flag (stale News responses die).
        try {
            OfflinePackPrefetcher.get().resetSession();
        } catch (Exception ignored) {
        }
    }

    /**
     * Enter Video News as a fresh list: page 0, skip=0, video_posts=true,
     * flip from index 0. Call before inlist when opening Video News tab.
     */
    private void prepareVideoFeedFromTop() {
        showLog("prepareVideoFeedFromTop");
        // Mode first — same order as Special so concurrent bind/getView never sees News.
        softMergeOnNextPage0 = false;
        setFeedModeFlags(false, true);
        prepareFreshPage0FeedCursors();
        try {
            OfflinePackPrefetcher.get().resetSession();
        } catch (Exception ignored) {
        }
    }

    /**
     * Atomically set Special/Video in-memory + committed prefs (no apply() race).
     * Video wins over Special when both requested.
     */
    private void setFeedModeFlags(boolean special, boolean video) {
        if (video) {
            special = false;
        }
        specialFeedModeActive = special;
        videoFeedModeActive = video;
        SharedPrefsData.putBoolImmediate(
                HomePage.this, Constants.isUnreadPostsSelected, special, Constants.PREF_NAME);
        SharedPrefsData.putBoolImmediate(
                HomePage.this, Constants.isVideoPostsSelected, video, Constants.PREF_NAME);
        // Online News|Read pill only on main News — hide for Special/Video.
        try {
            if (special || video) {
                if (bookmarksModeSelected) {
                    bookmarksModeSelected = false;
                    applyHomeModeContentVisibility();
                }
            }
            refreshHomeModeToggleUi();
            refreshHomeModeCounts();
        } catch (Exception ignored) {
        }
    }

    /** Public for FlipperAdapter half-strip / getView reject. */
    public boolean isSpecialFeedModeActive() {
        return isSpecialFeedMode();
    }

    private boolean isSpecialFeedMode() {
        // Prefer in-memory; prefs may lag only if someone wrote via apply() elsewhere.
        if (videoFeedModeActive) return false;
        if (specialFeedModeActive) return true;
        return SharedPrefsData.getBool(
                HomePage.this, Constants.isUnreadPostsSelected, Constants.PREF_NAME)
                && !SharedPrefsData.getBool(
                HomePage.this, Constants.isVideoPostsSelected, Constants.PREF_NAME);
    }

    private boolean isVideoFeedMode() {
        if (videoFeedModeActive) return true;
        return SharedPrefsData.getBool(
                HomePage.this, Constants.isVideoPostsSelected, Constants.PREF_NAME);
    }

    /** Half My-Feed types — never shown in Special. */
    private static boolean isHalfNewsPostType(@Nullable String postType) {
        if (postType == null) return false;
        return "normal_image".equalsIgnoreCase(postType)
                || "normal_video".equalsIgnoreCase(postType);
    }

    /**
     * Client safety filter for Special: strip {@code normal_image} / {@code normal_video}
     * even if the server errs or a main-list response races in. Mutates list in place.
     */
    private void filterListStripHalfPosts(@Nullable java.util.List<ModelClass.Datum> items) {
        if (items == null || items.isEmpty()) return;
        for (int i = items.size() - 1; i >= 0; i--) {
            ModelClass.Datum d = items.get(i);
            if (d == null || isHalfNewsPostType(d.getPostType())) {
                items.remove(i);
            }
        }
    }

    /**
     * Offline paint gate: drop items without a successfully cached hero image.
     * Keeps Offline News list free of ANYNEWS logo placeholders.
     */
    private void filterListToOfflineMediaReady(
            @Nullable java.util.List<ModelClass.Datum> items) {
        if (items == null || items.isEmpty()) return;
        for (int i = items.size() - 1; i >= 0; i--) {
            ModelClass.Datum d = items.get(i);
            if (d == null || !OfflineFeedStore.isServingReady(this, d)) {
                items.remove(i);
            }
        }
    }

    /** Half ({@code normal_video}) + full ({@code full_video}) only. */
    private static boolean isVideoNewsPostType(@Nullable String postType) {
        if (postType == null) return false;
        return "normal_video".equalsIgnoreCase(postType)
                || "full_video".equalsIgnoreCase(postType);
    }

    /**
     * Kept in Video News when mixed payloads arrive: video primary content plus the
     * same ad/ratings/vast slots News and Special streams show (do not strip ads).
     */
    private static boolean isVideoFeedAllowedPostType(@Nullable String postType) {
        if (isVideoNewsPostType(postType)) return true;
        if (postType == null) return false;
        return "ad".equalsIgnoreCase(postType)
                || "web_ads".equalsIgnoreCase(postType)
                || "ratings".equalsIgnoreCase(postType)
                || "google_vasttag".equalsIgnoreCase(postType)
                || "share_app".equalsIgnoreCase(postType);
    }

    /**
     * Client safety filter when server ignores {@code video_posts} or returns
     * greetings/mixed article types. Keeps half/full video and ad slots; strips
     * everything else. Mutates the list in place.
     */
    private void filterListToVideoPostsOnly(@Nullable java.util.List<ModelClass.Datum> items) {
        if (items == null || items.isEmpty()) return;
        for (int i = items.size() - 1; i >= 0; i--) {
            ModelClass.Datum d = items.get(i);
            if (d == null || !isVideoFeedAllowedPostType(d.getPostType())) {
                items.remove(i);
            }
        }
    }

    /**
     * Build a rebalance-ready ad shell from feed-level {@code ads_data} when the
     * page response had no {@code post_type=ad} row (short video pages).
     */
    @Nullable
    private ModelClass.Datum adShellFromAdsData(@Nullable EncreptionRequest enc) {
        if (enc == null || !enc.isShow_ads()) return null;
        java.util.List<com.anyNews.anynews.Pojo.AdsPojo> positions = enc.getAd_positions();
        if (positions == null || positions.isEmpty()) return null;
        com.anyNews.anynews.Pojo.AdsPojo first = positions.get(0);
        if (first == null || first.getAd_code() == null || first.getAd_code().isEmpty()) {
            return null;
        }
        try {
            ModelClass.Datum shell = new ModelClass.Datum();
            shell.setPostType("ad");
            shell.setAd_type(first.getAd_type() != null ? first.getAd_type() : "google");
            shell.setAd_code(first.getAd_code());
            shell.setNext_req_ad_code(first.getAd_code());
            String code = first.getAd_code();
            shell.setAds_from(code != null && code.startsWith("/") ? "admanager" : "admob");
            shell.setAd_screen_type("native");
            shell.setAd_view_type("regular");
            shell.setAd_audio_mute(true);
            shell.setAd_check(true);
            if (first.getAd_req_position() != null) {
                shell.setAd_req_position(first.getAd_req_position());
            }
            // Stable synthetic id so Flipper/ad loaders treat it as an ad shell.
            shell.setPostId(901012L);
            shell.setSubPostId(1L);
            return shell;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Video News: remember ad shell + re-place ads every
     * {@link ReadArticleStore#AD_EVERY_N_REAL} real video cards (same as main News).
     * Uses feed ad rows and/or {@code ads_data} when the page had no ad slot yet.
     */
    private void rebalanceVideoFeedAdsIfNeeded(
            @Nullable java.util.List<ModelClass.Datum> items,
            @Nullable EncreptionRequest enc) {
        if (items == null || items.isEmpty()) return;
        if (!isVideoFeedMode() || !isDeviceOnline() || offlineChromeMode) return;
        try {
            rememberFeedAdTemplate(items);
            if (onlineNewsAdTemplate == null && enc != null) {
                ModelClass.Datum fromData = adShellFromAdsData(enc);
                if (fromData != null) {
                    onlineNewsAdTemplate = fromData;
                }
            }
            if (onlineNewsAdTemplate == null) return;
            boolean changed =
                    ReadArticleStore.rebalanceFeedSlots(items, onlineNewsAdTemplate);
            if (changed) {
                showLog("video feed: rebalanced ads listSize=" + items.size()
                        + " reals=" + ReadArticleStore.countRealArticles(items));
                preloadNativeAdsForFeedSlots(items);
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Soft feed restart matching cold-start intent: latest page 0, no push
     * sticky, no flip-restore jump to a previously-viewed article.
     * Leaves Special ({@code isUnreadPostsSelected=false}) and reloads My Feed.
     * Used by bottom-nav Reload (select + reselect).
     * <p>
     * Offline: re-paint OfflineFeedStore / OpenPathCache without blanking the
     * adapter first (same reliable path as cold offline open after rebind).
     */
    private void performFeedReload() {
        showLog("performFeedReload initialCall=" + initialCall);
        writeButtonClick("User", type_firebase_button, current_time_firebase,
                draft_id_firebase, device_id_firebase, button_name_home);
        showLog("isUnreadPostsSelected set to false #5");
        setFeedModeFlags(false, false);
        try {
            OfflinePackPrefetcher.get().resetSession();
        } catch (Exception ignored) {
        }
        // Footer Reload always returns to News stream (not bookmarks list).
        try {
            selectHomeModeNews(true);
        } catch (Exception ignored) {
        }
        // My Feed chrome — same as tapping My Feed
        try {
            comment_page.setVisibility(View.GONE);
            un_selected_local.setVisibility(View.VISIBLE);
            un_selected_my_feed.setVisibility(View.GONE);
            selected_my_feed.setVisibility(View.VISIBLE);
            selected_local.setVisibility(View.GONE);
        } catch (Exception ignored) {
        }
        showLog("Check Point #3 : HomePage performFeedReload soft");
        if (!isDeviceOnline()) {
            // Offline Reload: keep prior list until disk paint rebinds adapter with setData.
            // Do NOT clear list/adapter first (that left a permanent white FlipView before).
            prepareOfflineMainReloadCursors();
            hideHomeFeedLoadingReveal(true);
            if (!paintOfflineFeed(false, true)) {
                // No cache — clear and show empty (not pure white FlipView).
                try {
                    list = new ArrayList<>();
                    adapter = null;
                    showMainOfflineEmptyState();
                } catch (Exception ignored) {
                }
            }
            return;
        }
        // Online: full page-0 reset then network inlist (same as cold soft restart).
        prepareFreshPage0FeedCursors();
        try {
            showHomeFeedLoadingReveal();
            layout_with_data.setVisibility(View.GONE);
            comment_page.setVisibility(View.GONE);
        } catch (Exception ignored) {
        }
        inlist(0);
    }

    /**
     * Soft Special-tab open: always {@code special_posts=true} page 0 from the
     * top. Does not keep main-feed skip/cursor, held page, or flip-restore.
     * Does not recreate the activity (avoids inheriting mid-scroll pagination).
     * Offline: online-only empty message — never paints special/main offline cache.
     */
    private void performSpecialFeedOpen() {
        showLog("performSpecialFeedOpen");
        clearSpecialTabBlink();
        showLog("Check Point #5 : HomePage performSpecialFeedOpen soft");
        // Never soft-merge main Offline/reconnect list into Special.
        softMergeOnNextPage0 = false;
        // Mode flag FIRST (before any clear/paint/network): first News→Special transition.
        setFeedModeFlags(true, false);
        // Special is feed-mode (not bookmarks list) — hide panel only, do not paint main offline.
        try {
            bookmarksModeSelected = false;
            applyHomeModeContentVisibility();
            refreshHomeModeToggleUi();
        } catch (Exception ignored) {
        }
        if (!isDeviceOnline()) {
            // Online-only: Special flag already set; hard-clear FlipView + empty state.
            OpenPathCache.clearForceFeedNetwork();
            try {
                clearPendingFlipRestore();
                clearStickyPushPostId();
                SHARE_ID = 0;
                SHARE_REDIRECT_ID = "0";
                DATAFROM2G = "0";
                last_post_id = 0;
                skip = 0;
                actual_skip = 0;
                positionx = 0;
                lastSettledFlipIndex = -1;
                requestPostition = 1;
                isApiRequested = false;
                isDataParsed = true;
                isFromLocalArticles = true;
                dummyResponse = null;
                category_id = 0;
                hardClearFlipFeedImmediate();
            } catch (Exception ignored) {
            }
            hideHomeFeedLoadingReveal(true);
            showSpecialOfflineEmptyState();
            return;
        }
        prepareSpecialFeedFromTop();
        try {
            showHomeFeedLoadingReveal();
            if (layout_with_data != null) {
                layout_with_data.setVisibility(View.GONE);
            }
            if (comment_page != null) {
                comment_page.setVisibility(View.GONE);
            }
            // Belt-and-suspenders: ensure FlipView is empty/detached even if a path
            // reattached the main adapter between prepare and inlist.
            hardClearFlipFeedImmediate();
            if (txtNoData != null) {
                txtNoData.setVisibility(View.GONE);
            }
        } catch (Exception ignored) {
        }
        inlist(0);
    }

    /**
     * Soft Video News tab open: always {@code video_posts=true} page 0 from the top
     * (half + full video only). Offline: paints main offline pack filtered to videos
     * when any exist; otherwise a clear empty message.
     */
    private void performVideoFeedOpen() {
        showLog("performVideoFeedOpen");
        try {
            bookmarksModeSelected = false;
            applyHomeModeContentVisibility();
            refreshHomeModeToggleUi();
        } catch (Exception ignored) {
        }
        if (!isDeviceOnline()) {
            OpenPathCache.clearForceFeedNetwork();
            try {
                clearPendingFlipRestore();
                clearStickyPushPostId();
                SHARE_ID = 0;
                SHARE_REDIRECT_ID = "0";
                DATAFROM2G = "0";
                last_post_id = 0;
                skip = 0;
                actual_skip = 0;
                positionx = 0;
                lastSettledFlipIndex = -1;
                requestSize = 0;
                requestPostition = 1;
                isApiRequested = false;
                isDataParsed = true;
                isFromLocalArticles = true;
                dummyResponse = null;
                category_id = 0;
                setFeedModeFlags(false, true);
            } catch (Exception ignored) {
            }
            hideHomeFeedLoadingReveal(true);
            if (!paintOfflineVideoFeed()) {
                showVideoOfflineEmptyState();
            }
            return;
        }
        prepareVideoFeedFromTop();
        try {
            showHomeFeedLoadingReveal();
            layout_with_data.setVisibility(View.GONE);
            comment_page.setVisibility(View.GONE);
        } catch (Exception ignored) {
        }
        inlist(0);
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
        // Only a real numeric push post id (>0). Do not sticky-parse "null"/empty
        // or leave a prior TITLE_VALUE when parse fails — that re-opened Open on Reload.
        TITLE_VALUE = 0;
        try {
            if (pushUpdatedID != null) {
                String raw = pushUpdatedID.trim();
                if (!raw.isEmpty()
                        && !raw.equalsIgnoreCase("null")
                        && !raw.equals("0")) {
                    TITLE_VALUE = Integer.parseInt(raw);
                    if (TITLE_VALUE < 0) {
                        TITLE_VALUE = 0;
                    }
                }
            }
        } catch (NumberFormatException e) {
            showLog("not a number");
            TITLE_VALUE = 0;
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
        boolean specialMode = isSpecialFeedMode();
        boolean videoMode = isVideoFeedMode();
        // Mutually exclusive: Video News always wins over Special when video tab is on
        // so page 2+ never remaps to special_posts / main sticky pagination.
        if (videoMode) {
            specialMode = false;
        }
        requestModel.setSpecial_posts(specialMode);
        requestModel.setVideo_posts(videoMode);
        // New clients only: optional capped recent-read ids so server can densify unread pages
        // via shared feed cache + in-memory strip (never per-user Mongo). Primary UX filter
        // remains client stripReadFromFeed on merge/paint; exclude is additive densify only.
        // Cap = full committed ReadArticleStore (MAX_IDS) so older-than-N reads are not
        // re-served and client-stripped to empty while deeper unreads exist.
        // Never on Special/Video. Old server ignores unknown fields; omit → legacy shared path.
        if (!specialMode && !videoMode && isDeviceOnline()) {
            try {
                String excludeCsv = buildExcludePostIdsCsvOmittingDeepLinks();
                if (!TextUtils.isEmpty(excludeCsv)) {
                    requestModel.setExclude_post_ids(excludeCsv);
                }
            } catch (Exception ignored) {
            }
        }
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

        // Online Read flip: still allow main-News network so unread skip-ahead /
        // pagination can fill the saved News snapshot (absorb path). Never on
        // Special/Video. Flip paint of Read postcards is local-only.
        if (isOnlineReadFlipMode()) {
            if (!canPageOnlineMainNews()) {
                showLog("ShowNews_Data_Values skipped — Read + non-main");
                isApiRequested = false;
                return;
            }
            showLog("ShowNews_Data_Values while Read — News snapshot page reqPos="
                    + reqPos);
        }

        // Page-0 open path: optional disk paint for plain reopen only.
        // Never paint cache / never throttle when: Reload, Special, push/share sticky.
        final boolean isFirstPage = skip == 0 && actual_skip == 0 && last_post_id == 0
                && reqPos != 3;
        final boolean special = isSpecialFeedMode();
        final boolean videoFeed = isVideoFeedMode();
        final boolean stickyPushOrShare = isStickyFeedOpen();
        final boolean forceUserRefresh = special
                || videoFeed
                || stickyPushOrShare
                || OpenPathCache.isForceFeedNetworkPending();

        // Full offline: main → OfflineFeedStore; Special online-only; Video filters pack.
        if (!isDeviceOnline()) {
            if (special) {
                showSpecialOfflineEmptyState();
            } else if (videoFeed) {
                if (!paintOfflineVideoFeed()) {
                    showVideoOfflineEmptyState();
                }
            } else if (isFirstPage || list == null || list.isEmpty()) {
                boolean painted = paintOfflineFeed(false, true);
                if (!painted && isFirstPage) {
                    hideHomeFeedLoadingReveal(true);
                    try {
                        if (txtNoData != null) txtNoData.setVisibility(View.VISIBLE);
                        if (layout_with_data != null) layout_with_data.setVisibility(View.VISIBLE);
                    } catch (Exception ignored) {
                    }
                }
            } else {
                // Pagination while offline on main: keep current list, no network.
                hideHomeFeedLoadingReveal(true);
            }
            if (OpenPathCache.isForceFeedNetworkPending()) {
                OpenPathCache.clearForceFeedNetwork();
            }
            softMergeOnNextPage0 = false;
            return;
        }

        boolean paintedFromDisk = false;
        // After warm disk paint, convert applies skip/actual_skip/last_post_id so scroll continues.
        // Capture those for interim use; page-0 network still requests with zeroed cursors.
        long paintedSkip = 0;
        long paintedActualSkip = 0;
        long paintedLastPostId = 0;
        if (isFirstPage && !forceUserRefresh) {
            // Main mode only — special/video never hydrate from main page-0 disk.
            // Warm OpenPath only (≤5 min). No offline pack decoy while online.
            EncreptionRequest cached = OpenPathCache.loadFeedForInstantPaint(
                    this, category_id, OpenPathCache.FEED_MODE_MAIN);
            if (cached != null) {
                showLog("OpenPath: paint feed from warm disk cat=" + category_id);
                convertListOfArticles(cached, reqPos, true);
                paintedFromDisk = list != null && !list.isEmpty();
                paintedSkip = skip;
                paintedActualSkip = actual_skip;
                paintedLastPostId = last_post_id;
            }
            // Online with no warm cache: keep grey shimmer; list fills only from network.
        }

        // If warm disk already filled the feed, skip heavy reveal loop flash.
        if (paintedFromDisk && list != null && !list.isEmpty()) {
            hideHomeFeedLoadingReveal(true);
            try {
                if (layout_with_data != null) {
                    layout_with_data.setVisibility(View.VISIBLE);
                }
                if (mFlipView != null) {
                    mFlipView.setVisibility(View.VISIBLE);
                }
                if (txtNoData != null) {
                    txtNoData.setVisibility(View.GONE);
                }
            } catch (Exception ignored) {
            }
        }

        // Reload / special / sticky always network. Throttle only plain warm reopen.
        // Warm paint still soft-refreshes outside FEED_NETWORK_MIN_INTERVAL_MS.
        boolean needNetwork = !isFirstPage
                || forceUserRefresh
                || OpenPathCache.shouldNetworkRefreshFeed(this, forceUserRefresh)
                || OpenPathCache.loadFeed(this, category_id) == null
                || OpenPathCache.isForceFeedNetworkPending();

        // Cache-only warm reopen still needs pagination cursors; without them do not soft-skip.
        final boolean paintedHasCursors = paintedLastPostId != 0
                || paintedSkip != 0
                || paintedActualSkip != 0;
        if (!needNetwork && paintedFromDisk && !paintedHasCursors) {
            showLog("OpenPath: cache paint missing skip/actual_skip/last_post_id — forcing network");
            needNetwork = true;
        }

        if (!needNetwork) {
            showLog("OpenPath: skip feed network (fresh within "
                    + OpenPathCache.FEED_NETWORK_MIN_INTERVAL_MS + "ms)"
                    + " cursors skip=" + skip + " actual_skip=" + actual_skip
                    + " last_post_id=" + last_post_id);
            return;
        }

        // Keep shimmer visible until network lands when nothing warm was painted.
        if (!paintedFromDisk && isFirstPage) {
            try {
                showHomeFeedLoadingReveal();
            } catch (Exception ignored) {
            }
        }

        // Page-0 network after disk paint must not use advanced cache cursors in the request body.
        if (isFirstPage && paintedFromDisk) {
            last_post_id = 0;
            skip = 0;
            actual_skip = 0;
        }

        JsonObject otp = Articles_All(category_id);

        // Dedupe must match the request body (page-0 zeros), not interim painted cursors —
        // so splash early prefetch can coalesce with Home.
        final long dedupeSkip = skip;
        final long dedupeLastPostId = last_post_id;

        // Keep painted cursors for interim scroll until page-0 network lands and replaces.
        if (isFirstPage && paintedFromDisk) {
            last_post_id = paintedLastPostId;
            skip = paintedSkip;
            actual_skip = paintedActualSkip;
        }

        APIService apiService = RestApiClient.getClient().create(APIService.class);
        Call<EncreptionRequest> otp_generation = apiService.List_All_Articles("v3", otp);

        // Consume one-shot reload force now that we are committing to network.
        if (OpenPathCache.isForceFeedNetworkPending()) {
            OpenPathCache.clearForceFeedNetwork();
        }

        // Drop in-flight responses after Special/News/Video mode change.
        final int epochAtEnqueue = feedModeEpoch;
        final boolean specialAtEnqueue = special;
        final boolean videoAtEnqueue = videoFeed;

        Callback<EncreptionRequest> feedCallback = new Callback<EncreptionRequest>() {

            @Override
            public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {
                if (epochAtEnqueue != feedModeEpoch) {
                    showLog("List_All_Articles stale response dropped epoch="
                            + epochAtEnqueue + " now=" + feedModeEpoch
                            + " specialWas=" + specialAtEnqueue + " videoWas=" + videoAtEnqueue);
                    isApiRequested = false;
                    return;
                }
                // Mode must still match request, else main News would paint into Special.
                boolean specialNow = isSpecialFeedMode();
                boolean videoNow = isVideoFeedMode();
                if (specialNow != specialAtEnqueue || videoNow != videoAtEnqueue) {
                    showLog("List_All_Articles mode mismatch dropped special="
                            + specialNow + " vs " + specialAtEnqueue
                            + " video=" + videoNow + " vs " + videoAtEnqueue);
                    isApiRequested = false;
                    return;
                }
                if (response != null && response.body() != null && isFirstPage && !special && !videoFeed && !isStickyFeedOpen()) {
                    OpenPathCache.saveFeed(
                            HomePage.this,
                            category_id,
                            response.body(),
                            OpenPathCache.FEED_MODE_MAIN);
                } else if (response != null && response.body() != null && isFirstPage) {
                    // Special/video/sticky still timestamps success so plain reopens can soft-skip later,
                    // but do not overwrite main disk cache with special/video payloads.
                    OpenPathCache.markFeedNetworkSuccess(HomePage.this);
                }
                // isFirstPage → replace (reload/special/open + network over disk paint).
                // Held next page (reqPos=-1 apply of dummy held at 3) uses Response overload: append.
                convertListOfArticles(response, reqPos, isFirstPage);
            }

            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {
                if (epochAtEnqueue != feedModeEpoch) {
                    showLog("List_All_Articles stale failure ignored");
                    return;
                }
                isApiRequested = false;
                showLog("List_All_Articles onFailure: " + (t != null ? t.getMessage() : "null"));
                softMergeOnNextPage0 = false;
                final boolean specialFlag = isSpecialFeedMode();
                final boolean videoFlag = isVideoFeedMode();
                // Network failed after clear (e.g. Reload) while offline → rehydrate disk / empty UX.
                if (list == null || list.isEmpty()) {
                    hideHomeFeedLoadingReveal(true);
                    try {
                        if (specialFlag) {
                            showSpecialOfflineEmptyState();
                            initialCall = true;
                            return;
                        }
                        if (videoFlag) {
                            if (!paintOfflineVideoFeed()) {
                                showVideoOfflineEmptyState();
                            }
                            initialCall = true;
                            return;
                        }
                        if (paintOfflineFeed(false, true)) {
                            initialCall = true;
                            return;
                        }
                        if (mFlipView != null) {
                            mFlipView.setVisibility(View.GONE);
                        }
                        if (layout_with_data != null) {
                            layout_with_data.setVisibility(View.VISIBLE);
                        }
                        if (post_news_scrren != null) {
                            post_news_scrren.setVisibility(View.GONE);
                        }
                        if (txtNoData != null) {
                            txtNoData.setVisibility(View.VISIBLE);
                        }
                        initialCall = true;
                    } catch (Exception ignored) {
                    }
                } else {
                    // Disk paint / non-empty list: never re-expose FlipView on Special with
                    // leftover News half cards after a failed special_posts fetch.
                    hideHomeFeedLoadingReveal(true);
                    if (specialFlag) {
                        try {
                            hardClearFlipFeedImmediate();
                            showSpecialOfflineEmptyState();
                        } catch (Exception ignored) {
                        }
                        initialCall = true;
                        return;
                    }
                    if (layout_with_data != null) {
                        layout_with_data.setVisibility(View.VISIBLE);
                    }
                    if (mFlipView != null) {
                        mFlipView.setVisibility(View.VISIBLE);
                    }
                }
            }
        };

        try {
            // Bypass deduper on reload/special/video/sticky so coalescing never drops a forced fetch.
            if (forceUserRefresh) {
                showLog("OpenPath: force network (reload/special/video/sticky) cat=" + category_id
                        + " special=" + special + " video=" + videoFeed + " sticky=" + stickyPushOrShare);
                otp_generation.enqueue(feedCallback);
            } else {
                String dedupeKey = "articles_v3_" + category_id + "_" + dedupeSkip + "_" + dedupeLastPostId
                        + "_" + TITLE_VALUE + "_" + SHARE_ID + "_" + special + "_" + videoFeed;
                NetworkCallDeduper.enqueueUnique(dedupeKey, otp_generation, feedCallback);
            }

        } catch (Exception e) {
        }
    }

    /**
     * Article skeleton + L→R shimmer while first page feed is pending.
     * No-op when FlipView already has paintables (warm OpenPath / keep-prior frame).
     */
    private void showHomeFeedLoadingReveal() {
        try {
            // Never cover an already-bound News card with a full-screen skeleton.
            if (list != null && !list.isEmpty()
                    && ReadArticleStore.countPaintableArticles(list) > 0
                    && mFlipView != null
                    && mFlipView.getVisibility() == View.VISIBLE
                    && (layout_with_out_data == null
                            || layout_with_out_data.getVisibility() != View.VISIBLE)) {
                showLog("showHomeFeedLoadingReveal skipped — feed already painted");
                return;
            }
            if (layout_with_out_data != null) {
                layout_with_out_data.setVisibility(View.VISIBLE);
                // Solid surface color must not block touch-through forever: child draw only.
                layout_with_out_data.setClickable(true);
                layout_with_out_data.setFocusable(true);
            }
            if (homeFeedReveal != null) {
                homeFeedReveal.start();
            }
            scheduleHomeFeedLoadingSafetyTimeout();
        } catch (Exception ignored) {
        }
    }

    /**
     * Dismiss loading overlay. Always force-hides the container immediately so a solid
     * feed_shimmer_surface (dark gray) can never stay stuck over the FlipView.
     * @param instant ignored for overlay container (always GONE); still used for reveal child.
     */
    private void hideHomeFeedLoadingReveal(boolean instant) {
        try {
            cancelHomeFeedLoadingSafetyTimeout();
            // Always kill the solid overlay first — never depend only on fade endAction.
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

    private void cancelHomeFeedLoadingSafetyTimeout() {
        try {
            if (homeFeedLoadingTimeoutRunnable != null) {
                homeFeedLoadingHandler.removeCallbacks(homeFeedLoadingTimeoutRunnable);
                homeFeedLoadingTimeoutRunnable = null;
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Never leave News/Special/Video on a permanent loading/white surface: after
     * {@link #HOME_FEED_LOADING_TIMEOUT_MS}, either bind whatever paintables we
     * have, keep paging once if allowed, or show the empty state.
     */
    private void scheduleHomeFeedLoadingSafetyTimeout() {
        cancelHomeFeedLoadingSafetyTimeout();
        homeFeedLoadingTimeoutRunnable = new Runnable() {
            @Override
            public void run() {
                homeFeedLoadingTimeoutRunnable = null;
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                try {
                    boolean overlayUp = layout_with_out_data != null
                            && layout_with_out_data.getVisibility() == View.VISIBLE;
                    if (!overlayUp) {
                        return;
                    }
                    showLog("home feed loading safety timeout — force resolve");
                    int paintables = list != null
                            ? ReadArticleStore.countPaintableArticles(list) : 0;
                    if (list != null && !list.isEmpty() && paintables > 0) {
                        hideHomeFeedLoadingReveal(true);
                        ensureHomeFlipAdapterBoundAndData(list);
                        if (mFlipView != null) {
                            mFlipView.setVisibility(View.VISIBLE);
                        }
                        if (layout_with_data != null) {
                            layout_with_data.setVisibility(View.VISIBLE);
                        }
                        if (txtNoData != null) {
                            txtNoData.setVisibility(View.GONE);
                        }
                        // If current seat is empty ad, jump to first real content.
                        seatAwayFromUnfilledAdIfNeeded();
                        return;
                    }
                    // Soft-empty while still able to page — one more fetch, else empty.
                    resolveSoftEmptyNewsFeed("loading_timeout");
                } catch (Exception e) {
                    showLog("homeFeedLoadingTimeout: " + e.getMessage());
                    try {
                        hideHomeFeedLoadingReveal(true);
                    } catch (Exception ignored) {
                    }
                }
            }
        };
        try {
            homeFeedLoadingHandler.postDelayed(
                    homeFeedLoadingTimeoutRunnable, HOME_FEED_LOADING_TIMEOUT_MS);
        } catch (Exception ignored) {
        }
    }

    /**
     * If FlipView is parked on an unfilled ad spinner at open, slide to the first
     * real/paintables card (ads without creative never block first content forever).
     */
    private void seatAwayFromUnfilledAdIfNeeded() {
        try {
            if (list == null || list.isEmpty() || mFlipView == null) {
                return;
            }
            int cur = positionx;
            try {
                cur = mFlipView.getCurrentPage();
            } catch (Exception ignored) {
            }
            if (cur < 0 || cur >= list.size()) {
                cur = 0;
            }
            ModelClass.Datum d = list.get(cur);
            if (d == null || !ReadArticleStore.isAdFeedSlot(d) || hasLoadedNativeForSlot(d)) {
                return;
            }
            int better = indexOfFirstPaintableOrFilledCard(list);
            if (better < 0 || better == cur) {
                // No better card yet — timeout path will remove empty ads.
                return;
            }
            positionx = better;
            if (adapter != null) {
                adapter.setCurrentFlipPage(better);
            }
            mFlipView.flipTo(better);
        } catch (Exception ignored) {
        }
    }

    /** True when open carries a one-shot push or share post id that requires live feed. */
    private boolean isStickyFeedOpen() {
        try {
            if (pushUpdatedID != null) {
                String raw = pushUpdatedID.trim();
                if (!raw.isEmpty() && !raw.equalsIgnoreCase("null") && !raw.equals("0")) {
                    int v = Integer.parseInt(raw);
                    if (v > 0) return true;
                }
            }
        } catch (Exception ignored) {
        }
        return SHARE_ID > 0;
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
        // Held next-page merge (dummyResponse, reqPos=-1): never force page-0 replace.
        convertListOfArticles(response, reqPos, false);
    }

    /**
     * @param replacePage0 true when this network response is first-page (open/reload/special);
     *                     false for pagination / held-merge apply (append unique).
     */
    public void convertListOfArticles(Response<EncreptionRequest> response, int reqPos, boolean replacePage0){
        if (reqPos==3 && !isFromLocalArticles) {
            if (response != null && response.body() != null) {
                showReporterLogin = response.body().isShow_reporter_login();
                showLog("showReporterLogin (stored early):" + showReporterLogin);
            }
            isDataParsed=false;
            dummyResponse=response;
            showLog("requestPosition::"+requestPostition);
            requestPostition=1+requestSize;
            showLog("requestPosition::"+requestPostition);
            // Also grow offline pack from held next page (no wait for end-of-list apply).
            try {
                if (!SharedPrefsData.getBool(
                        HomePage.this, Constants.isUnreadPostsSelected, Constants.PREF_NAME)
                        && !isSpecialFeedMode()
                        && response != null
                        && response.body() != null) {
                    OfflinePackPrefetcher.get()
                            .onHeldPageForDisplay(getApplicationContext(), response.body());
                }
            } catch (Exception ignored) {
            }
            return;
        }
        EncreptionRequest body = (response != null) ? response.body() : null;
        convertListOfArticles(body, reqPos, false, replacePage0);
    }

    public void convertListOfArticles(EncreptionRequest encreptionRequest, int reqPos, boolean fromCache){
        // Disk page-0 paint always replaces. Network / held-merge pass replace via 4-arg overload.
        convertListOfArticles(encreptionRequest, reqPos, fromCache, fromCache);
    }

    public void convertListOfArticles(EncreptionRequest encreptionRequest, int reqPos, boolean fromCache, boolean replacePage0){
        no_article_message="Please wait while loading ...";
        showLog("reqPos:::::"+reqPos);
        showLog("isFromLocalArticles:::::"+isFromLocalArticles);
        showLog("fromCache:::::"+fromCache);
        // In-flight News page landed while user is on online Read flip: update News
        // snapshot only — never replace the read FlipView list. Also continues skip-ahead
        // when the snapshot is short (absorb schedules next page).
        if (!fromCache && isOnlineReadFlipMode()) {
            try {
                absorbMainFeedResponseIntoNewsSnapshot(encreptionRequest, replacePage0 || reqPos == 0);
            } catch (Exception ignored) {
            }
            // Only clear request flag if absorb did not immediately re-arm fetch.
            if (!isApiRequested) {
                isApiRequested = false;
            }
            isDataParsed = true;
            return;
        }
        if(reqPos==3 && !isFromLocalArticles){
            if (encreptionRequest != null) {
                showReporterLogin = encreptionRequest.isShow_reporter_login();
                showLog("showReporterLogin (stored early):" + showReporterLogin);
            }
            isDataParsed=false;
            showLog("requestPosition::"+requestPostition);
            requestPostition=1+requestSize;
            showLog("requestPosition::"+requestPostition);
            return;
        }
        try {
            if (encreptionRequest != null) {
                showLog("response body not null");
                if (!fromCache) {
                    dummyResponse=null;
                    isDataParsed=true;
                }
                SharedPrefsData.putBool(getApplicationContext(), Constants.HOME_SCREEN, true, Constants.PREF_NAME);
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
                    // Share deep-link already in Read history → Chadivinavi, not News top.
                    if (!fromCache
                            && tryOpenShareDeepLinkOnReadTab(
                                    validateUserOtp.getData(),
                                    encreptionRequest,
                                    replacePage0 || reqPos == 0)) {
                        isDataParsed = true;
                        isApiRequested = false;
                        initialCall = true;
                        try {
                            hideHomeFeedLoadingReveal(true);
                            if (home_page_navigation != null) {
                                home_page_navigation.setVisibility(View.VISIBLE);
                            }
                            ensureOnlineBottomNavVisible();
                        } catch (Exception ignored) {
                        }
                        return;
                    }
                   if (!fromCache) {
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
                    applyPushNotificationAppearanceConfig(
                            encreptionRequest.getPn_show_article_image(),
                            encreptionRequest.getPn_style_mode(),
                            encreptionRequest.getPn_large_icon_url(),
                            encreptionRequest.getPush_include_logo_default(),
                            encreptionRequest.getPn_left_icon_size_dp(),
                            encreptionRequest.getPn_thumb_size_dp(),
                            encreptionRequest.getPn_title_size_sp()
                    );
                    ScreenCaptureHelper.saveAllowed(HomePage.this, encreptionRequest.isAllow_screen_capture());
                    cachedCaptureAllowed = encreptionRequest.isAllow_screen_capture();
                    // Re-apply so a toggle change takes effect on this screen without restart.
                    ScreenCaptureHelper.applySecureFlag(HomePage.this);
                    appliedSecureFlag = !cachedCaptureAllowed;
                    // Cricket: paint from feed active_match only when mini play is ON.
                    // Manager performs zero cricket HTTP when admin toggled mini off.
                    if (cricketMiniPlayerManager != null) {
                        if (com.anyNews.anynews.Utils.CricketSurfaceConfigHelper
                                .isMiniPlayerEnabled(HomePage.this)) {
                            cricketMiniPlayerManager.update(encreptionRequest.getActive_match());
                        } else {
                            cricketMiniPlayerManager.update(null);
                        }
                    }
                    if (validateUserOtp.getData() != null) {
                        // Video News: half + full video, plus synthetic ad/ratings slots
                        // (same injection pipeline as News/Special). Strip greetings/mixed
                        // types only. Empty after filter is not end-of-feed — keep paging.
                        int preFilterCount = validateUserOtp.getData().size();
                        if (isVideoFeedMode()) {
                            filterListToVideoPostsOnly(validateUserOtp.getData());
                            int postFilterCount = validateUserOtp.getData().size();
                            if (preFilterCount > 0 && postFilterCount == 0 && !fromCache) {
                                // Cursor advanced on a mixed page with zero videos — keep paging.
                                // Includes page-0 empty list (used to fall through to permanent
                                // empty / loader with no further requests).
                                showLog("video feed: filtered empty page (" + preFilterCount
                                        + " raw) — continue pagination listSize="
                                        + (list != null ? list.size() : -1)
                                        + " streak=" + onlineNewsAllReadSkipStreak);
                                if (validateUserOtp.getLastPostId() != null) {
                                    last_post_id = validateUserOtp.getLastPostId();
                                }
                                if (validateUserOtp.getSkip() != null) {
                                    skip = validateUserOtp.getSkip();
                                }
                                if (validateUserOtp.getActualSkip() != null) {
                                    actual_skip = validateUserOtp.getActualSkip();
                                }
                                // Re-arm next-page trigger near the current index.
                                requestSize = list != null ? list.size() : 0;
                                requestPostition = Math.max(0, requestSize - 1);
                                if (isDeviceOnline()
                                        && onlineNewsAllReadSkipStreak
                                                < ONLINE_NEWS_ALL_READ_SKIP_CAP) {
                                    onlineNewsAllReadSkipStreak++;
                                    try {
                                        if (txtNoData != null) {
                                            txtNoData.setVisibility(View.GONE);
                                        }
                                        // Keep skeleton only when FlipView has nothing real yet.
                                        if (list == null || list.isEmpty()
                                                || ReadArticleStore.countPaintableArticles(list)
                                                        == 0) {
                                            showHomeFeedLoadingReveal();
                                        }
                                    } catch (Exception ignored) {
                                    }
                                    isApiRequested = false;
                                    final boolean listHadPaintables = list != null
                                            && ReadArticleStore.countPaintableArticles(list) > 0;
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (!isApiRequested && isVideoFeedMode()
                                                    && isDeviceOnline()) {
                                                isApiRequested = true;
                                                // Append when we already have cards; page-0 style
                                                // when still soft-empty.
                                                ShowNews_Data_Values(category_id,
                                                        listHadPaintables ? 3 : 1);
                                            }
                                        }
                                    });
                                    initialCall = true;
                                    return;
                                }
                                // Cap / offline: fall through to empty (or keep prior list).
                            }
                        } else if (isSpecialFeedMode()) {
                            // Special: never show half articles even if server or a raced
                            // main payload returns normal_image / normal_video.
                            filterListStripHalfPosts(validateUserOtp.getData());
                            int postFilterCount = validateUserOtp.getData().size();
                            if (preFilterCount > 0 && postFilterCount == 0) {
                                showLog("special feed: stripped all half posts from page ("
                                        + preFilterCount + " raw)");
                            }
                        }
                    }
                    if (validateUserOtp.getData() != null && validateUserOtp.getData().size() > 0) {
                        // Offline hydrate: keep bookmarked posts available without network.
                        // Also reflect local bookmark index onto feed items (bookmark icon state).
                        try {
                            for (ModelClass.Datum d : validateUserOtp.getData()) {
                                if (d == null || d.getPostId() == null || d.getPostId() <= 0) {
                                    continue;
                                }
                                boolean localBm =
                                        com.anyNews.anynews.Utils.BookmarkStore.isBookmarked(
                                                getApplicationContext(), d.getPostId());
                                if (d.getBookmarkStatus() != null
                                        && d.getBookmarkStatus() == 1) {
                                    // Server says bookmarked — ensure full payload is offline.
                                    com.anyNews.anynews.Utils.BookmarkStore.saveLocal(
                                            getApplicationContext(), d, false);
                                } else if (localBm) {
                                    d.setBookmarkStatus(1);
                                    // Refresh offline body if feed has fuller description.
                                    com.anyNews.anynews.Utils.BookmarkStore.saveLocal(
                                            getApplicationContext(), d, false);
                                }
                            }
                        } catch (Exception ignored) {
                        }
                        int preReadStripCount = validateUserOtp.getData() != null
                                ? validateUserOtp.getData().size() : 0;
                        // Capture ad shell even before unread strip / skip-ahead so we can
                        // re-place ads after the client filter without a backend redeploy.
                        try {
                            if (!fromCache && validateUserOtp.getData() != null) {
                                rememberOnlineNewsAdTemplate(validateUserOtp.getData());
                            }
                        } catch (Exception ignored) {
                        }
                        // If every *real* article on this network page is already read,
                        // advance cursor and fetch more (do not mutate payload used for
                        // offline pack). Ads/share_app alone used to make probe non-empty
                        // so pagination stopped on the Share shell with an empty News.
                        if (!fromCache
                                && preReadStripCount > 0
                                && canPageOnlineMainNews()) {
                            try {
                                java.util.ArrayList<ModelClass.Datum> probe =
                                        new java.util.ArrayList<>(validateUserOtp.getData());
                                int listRealsBefore =
                                        (list != null && !isOnlineReadFlipMode())
                                                ? ReadArticleStore.countRealArticles(list)
                                                : (hasSavedOnlineNewsSession
                                                        ? ReadArticleStore.countRealArticles(
                                                                savedOnlineNewsList)
                                                        : 0);
                                if (isOnlineReadFlipMode() && !hasSavedOnlineNewsSession) {
                                    listRealsBefore = 0;
                                }
                                int realBefore = ReadArticleStore.countRealArticles(probe);
                                rememberFeedStickiesFrom(probe);
                                java.util.Collection<Long> probeHold = null;
                                long landProbeId = resolvePendingDeepLinkPostId();
                                if (landProbeId > 0) {
                                    probeHold = java.util.Collections.singleton(landProbeId);
                                }
                                ReadArticleStore.stripReadFromFeed(
                                        getApplicationContext(), probe, probeHold);
                                int realAfter = ReadArticleStore.countRealArticles(probe);
                                int paintAfter = ReadArticleStore.countPaintableArticles(probe);
                                int stickyAfter = ReadArticleStore.countStickyArticles(probe);
                                boolean underMin =
                                        (listRealsBefore + realAfter)
                                                < ONLINE_NEWS_MIN_UNREAD_CARDS;
                                // Only skip the page when nothing paintables remain
                                // (unread stickies still paint after strip of committed reads).
                                boolean noUnreadOnPage = realAfter == 0
                                        && paintAfter == 0
                                        && (realBefore > 0 || preReadStripCount > 0);
                                // Unread sticky-only page after strip: merge it (don't skip).
                                boolean stickyOnlyKeep =
                                        stickyAfter > 0 && realAfter == 0;
                                boolean shouldSkipPage =
                                        !stickyOnlyKeep
                                        && onlineNewsAllReadSkipStreak
                                                < ONLINE_NEWS_ALL_READ_SKIP_CAP
                                        && (noUnreadOnPage
                                                || (underMin && realAfter == 0 && paintAfter == 0)
                                                || (underMin
                                                        && realAfter > 0
                                                        && realAfter < ONLINE_NEWS_MIN_UNREAD_CARDS
                                                        && listRealsBefore
                                                                < ONLINE_NEWS_MIN_UNREAD_CARDS
                                                        && realBefore > realAfter
                                                        && paintAfter == 0));
                                // If page has zero paintables after strip, always keep requesting
                                // until cap (share/ad-only is not end of feed).
                                if ((shouldSkipPage
                                        || (paintAfter == 0
                                        && onlineNewsAllReadSkipStreak
                                        < ONLINE_NEWS_ALL_READ_SKIP_CAP
                                        && listRealsBefore
                                        < ONLINE_NEWS_MIN_UNREAD_CARDS
                                        && !stickyOnlyKeep)) && !stickyOnlyKeep) {
                                    // Keep ad shell from discarded page for later rebalance.
                                    try {
                                        rememberOnlineNewsAdTemplate(validateUserOtp.getData());
                                        rememberOnlineNewsAdTemplate(probe);
                                    } catch (Exception ignored) {
                                    }
                                    // When page still has a few reals and we are under min,
                                    // fall through to append (do not discard the reals).
                                    if (realAfter > 0 && underMin) {
                                        onlineNewsAllReadSkipStreak = 0;
                                        // fall through to merge this page, then auto-page below
                                    } else {
                                    onlineNewsAllReadSkipStreak++;
                                    showLog("online News: page no unread real articles ("
                                            + preReadStripCount + " raw, real=" + realBefore
                                            + " → " + realAfter + ") skip streak="
                                            + onlineNewsAllReadSkipStreak
                                            + " listReals=" + listRealsBefore
                                            + " - continue pagination");
                                    if (validateUserOtp.getLastPostId() != null) {
                                        last_post_id = validateUserOtp.getLastPostId();
                                    }
                                    if (validateUserOtp.getSkip() != null) {
                                        skip = validateUserOtp.getSkip();
                                    }
                                    if (validateUserOtp.getActualSkip() != null) {
                                        actual_skip = validateUserOtp.getActualSkip();
                                    }
                                    // Snapshot while on Read also tracks cursors.
                                    if (isOnlineReadFlipMode()) {
                                        try {
                                            savedOnlineNewsSkip = skip;
                                            savedOnlineNewsActualSkip = actual_skip;
                                            savedOnlineNewsLastPostId = last_post_id;
                                            hasSavedOnlineNewsSession = true;
                                        } catch (Exception ignored) {
                                        }
                                    }
                                    requestSize = list != null ? list.size() : 0;
                                    requestPostition = Math.max(0, requestSize - 1);
                                    isApiRequested = false;
                                    final int contReq = 1;
                                    // Keep loading state — do not flash Share/empty mid-skip.
                                    if ((list == null || list.isEmpty()
                                            || ReadArticleStore.countRealArticles(list) == 0)
                                            && !isOnlineReadFlipMode()) {
                                        try {
                                            if (txtNoData != null) {
                                                txtNoData.setVisibility(View.GONE);
                                            }
                                        } catch (Exception ignored) {
                                        }
                                    }
                                    new Handler(Looper.getMainLooper()).post(() -> {
                                        if (!isApiRequested && canPageOnlineMainNews()) {
                                            isApiRequested = true;
                                            ShowNews_Data_Values(category_id, contReq);
                                        }
                                    });
                                    if (list != null && !list.isEmpty()
                                            && !isOnlineReadFlipMode()) {
                                        clearOfflineOnlyEmptyStatesIfFeedReady();
                                    }
                                    return;
                                    }
                                } else {
                                    onlineNewsAllReadSkipStreak = 0;
                                }
                            } catch (Exception ignored) {
                            }
                        }
                        if (list == null) {
                            list = new ArrayList<>();
                        }
                        int listSizeBeforeAppend = list.size();
                        final boolean specialNow = isSpecialFeedMode();
                        final boolean videoNow = isVideoFeedMode();
                        // Soft reconnect merges are main-feed only. Special/Video always replace
                        // so My Feed offline pack never bleeds into those tabs.
                        final boolean softMerge = softMergeOnNextPage0 && !fromCache
                                && listSizeBeforeAppend > 0 && isDeviceOnline()
                                && !specialNow && !videoNow;
                        if (softMerge) {
                            softMergeOnNextPage0 = false;
                            addUniqueArticles(validateUserOtp.getData());
                            // Soft-merge must not reintroduce half posts in Special.
                            if (isSpecialFeedMode()) {
                                filterListStripHalfPosts(list);
                            }
                            try {
                                if (adapter != null) {
                                    adapter.notifyDataSetChanged();
                                }
                            } catch (Exception ignored) {
                            }
                            // Do not rebuild adapter / do not jump flip — capture offline store.
                            try {
                                OfflineFeedStore.capture(getApplicationContext(), false, list);
                                OpenPathCache.saveFeed(
                                        HomePage.this,
                                        category_id,
                                        encreptionRequest,
                                        OpenPathCache.FEED_MODE_MAIN);
                                OfflinePackPrefetcher.get().onMainFeedCaptured(
                                        getApplicationContext(),
                                        list,
                                        validateUserOtp,
                                        false);
                            } catch (Exception ignored) {
                            }
                            try {
                                if (!bookmarksModeSelected) {
                                    rememberFeedStickiesFrom(list);
                                    stripReadArticlesIfOnlineNews(list);
                                    if (adapter != null) {
                                        adapter.notifyDataSetChanged();
                                    }
                                    if (list != null && !list.isEmpty()) {
                                        try {
                                            if (mFlipView != null) {
                                                mFlipView.setVisibility(View.VISIBLE);
                                            }
                                        } catch (Exception ignored) {
                                        }
                                    }
                                }
                            } catch (Exception ignored) {
                            }
                            requestSize = list.size();
                            clearOfflineOnlyEmptyStatesIfFeedReady();
                            return;
                        }
                        softMergeOnNextPage0 = false;
                        // Page-0 (open / reload / special / disk paint / first-page network): replace.
                        // Later pages incl. held merge with reqPos=-1: append unique (never clear).
                        // Do NOT use reqPos < 0 as replace — that wrongly wiped the feed on held apply.
                        final boolean page0Replace = fromCache || replacePage0 || list.isEmpty();
                        // Keep prior FlipView frame so a network page that strips to empty
                        // (or fails mid-path) never blanks an instant disk open.
                        final ArrayList<ModelClass.Datum> priorPaintedFrame =
                                (!fromCache
                                        && page0Replace
                                        && list != null
                                        && !list.isEmpty()
                                        && ReadArticleStore.countPaintableArticles(list) > 0)
                                        ? new ArrayList<>(list) : null;
                        if (page0Replace) {
                            list.clear();
                            list.addAll(validateUserOtp.getData());
                            listSizeBeforeAppend = 0;
                        } else {
                            addUniqueArticles(validateUserOtp.getData());
                        }
                        // Hard guarantee after any merge: Special list has no half posts.
                        if (isSpecialFeedMode()) {
                            filterListStripHalfPosts(list);
                        }
                        // Persist offline main pack (hard cap + merge); special/video skip main pack.
                        if (!fromCache) {
                            try {
                                boolean skipMainPack = specialNow || videoNow;
                                OfflineFeedStore.capture(getApplicationContext(), skipMainPack, list);
                                if (!skipMainPack) {
                                    OfflinePackPrefetcher.get().onMainFeedCaptured(
                                            getApplicationContext(),
                                            list,
                                            validateUserOtp,
                                            page0Replace);
                                } else {
                                    OfflinePackPrefetcher.get().resetSession();
                                }
                            } catch (Exception ignored) {
                            }
                        }
                        // Online main News UI only: drop already-read after pack capture.
                        // Stickies always survive strip (not part of read filter).
                        try {
                            if (!bookmarksModeSelected) {
                                // Snapshot before unread-strip so instant disk open never
                                // becomes empty shimmer when strip empties a cached page.
                                final ArrayList<ModelClass.Datum> preUnreadStrip =
                                        (list != null && !list.isEmpty())
                                                ? new ArrayList<>(list) : null;
                                final int preStripPaintables = preUnreadStrip != null
                                        ? ReadArticleStore.countPaintableArticles(preUnreadStrip)
                                        : 0;
                                rememberFeedStickiesFrom(list);
                                stripReadArticlesIfOnlineNews(list);
                                int paintablesNow = list != null
                                        ? ReadArticleStore.countPaintableArticles(list) : 0;
                                int realsNow = list != null
                                        ? ReadArticleStore.countRealArticles(list) : 0;
                                boolean densifyAlreadyScheduled = false;
                                // Disk (or soft page-0) emptied by strip: keep last unfiltered
                                // frame visible while network / auto-page fills unread denser.
                                if (paintablesNow == 0
                                        && priorPaintedFrame != null
                                        && !priorPaintedFrame.isEmpty()
                                        && canPageOnlineMainNews()) {
                                    // Prefer the already-visible disk/last frame over a blank.
                                    showLog("online News: page0 strip emptied — restore prior frame n="
                                            + priorPaintedFrame.size());
                                    list.clear();
                                    list.addAll(priorPaintedFrame);
                                    paintablesNow = ReadArticleStore.countPaintableArticles(list);
                                    realsNow = ReadArticleStore.countRealArticles(list);
                                    try {
                                        hideHomeFeedLoadingReveal(true);
                                    } catch (Exception ignored) {
                                    }
                                    isApiRequested = false;
                                    if (onlineNewsAllReadSkipStreak
                                            < ONLINE_NEWS_ALL_READ_SKIP_CAP) {
                                        onlineNewsAllReadSkipStreak++;
                                        densifyAlreadyScheduled = true;
                                        if (validateUserOtp.getLastPostId() != null) {
                                            last_post_id = validateUserOtp.getLastPostId();
                                        }
                                        if (validateUserOtp.getSkip() != null) {
                                            skip = validateUserOtp.getSkip();
                                        }
                                        if (validateUserOtp.getActualSkip() != null) {
                                            actual_skip = validateUserOtp.getActualSkip();
                                        }
                                        new Handler(Looper.getMainLooper()).post(() -> {
                                            if (!isApiRequested && canPageOnlineMainNews()
                                                    && !bookmarksModeSelected) {
                                                isApiRequested = true;
                                                ShowNews_Data_Values(category_id, 1);
                                            }
                                        });
                                    }
                                    // Fall through to setData with restored frame.
                                } else if (paintablesNow == 0
                                        && preStripPaintables > 0
                                        && preUnreadStrip != null
                                        && !preUnreadStrip.isEmpty()
                                        && canPageOnlineMainNews()) {
                                    showLog("online News: strip emptied paintables — keep unfiltered frame n="
                                            + preUnreadStrip.size()
                                            + " fromCache=" + fromCache);
                                    list.clear();
                                    list.addAll(preUnreadStrip);
                                    paintablesNow = preStripPaintables;
                                    realsNow = ReadArticleStore.countRealArticles(list);
                                    try {
                                        hideHomeFeedLoadingReveal(true);
                                    } catch (Exception ignored) {
                                    }
                                    // Still densify unread via network, but never re-cover FlipView.
                                    if (fromCache) {
                                        OpenPathCache.forceNextFeedNetwork(HomePage.this);
                                    }
                                    isApiRequested = false;
                                    if (onlineNewsAllReadSkipStreak
                                            < ONLINE_NEWS_ALL_READ_SKIP_CAP) {
                                        onlineNewsAllReadSkipStreak++;
                                        densifyAlreadyScheduled = true;
                                        new Handler(Looper.getMainLooper()).post(() -> {
                                            if (!isApiRequested && canPageOnlineMainNews()
                                                    && !bookmarksModeSelected) {
                                                isApiRequested = true;
                                                ShowNews_Data_Values(category_id, 1);
                                            }
                                        });
                                    }
                                    // Fall through to setData with restored frame.
                                } else if (fromCache
                                        && paintablesNow == 0
                                        && canPageOnlineMainNews()) {
                                    // No restorable frame (cache truly empty of paintables).
                                    showLog("online News: cache paint empty after strip — force network");
                                    try {
                                        if (txtNoData != null) {
                                            txtNoData.setVisibility(View.GONE);
                                        }
                                        // Only skeleton when FlipView has nothing to show.
                                        if (list == null || list.isEmpty()) {
                                            showHomeFeedLoadingReveal();
                                        }
                                    } catch (Exception ignored) {
                                    }
                                    OpenPathCache.forceNextFeedNetwork(HomePage.this);
                                    isApiRequested = false;
                                    densifyAlreadyScheduled = true;
                                    new Handler(Looper.getMainLooper()).post(() -> {
                                        if (!isApiRequested && canPageOnlineMainNews()
                                                && !bookmarksModeSelected) {
                                            isApiRequested = true;
                                            ShowNews_Data_Values(category_id, 1);
                                        }
                                    });
                                    // Fall through only if we still have something later; empty: stop
                                    // paint of share-only blank and let network fill.
                                    if (list == null || list.isEmpty()) {
                                        return;
                                    }
                                }
                                // Below min real unread: auto-page for more (keep stickies painted).
                                if (!densifyAlreadyScheduled
                                        && canPageOnlineMainNews()
                                        && list != null
                                        && realsNow < ONLINE_NEWS_MIN_UNREAD_CARDS
                                        && onlineNewsAllReadSkipStreak
                                                < ONLINE_NEWS_ALL_READ_SKIP_CAP) {
                                    int preReal = ReadArticleStore.countRealArticles(
                                            validateUserOtp.getData());
                                    if (preReal > 0 || paintablesNow == 0 || realsNow == 0) {
                                        onlineNewsAllReadSkipStreak++;
                                        showLog("online News: list unread below min after strip"
                                                + " — auto page skip="
                                                + onlineNewsAllReadSkipStreak
                                                + " reals=" + realsNow
                                                + " paintables=" + paintablesNow);
                                        if (validateUserOtp.getLastPostId() != null) {
                                            last_post_id = validateUserOtp.getLastPostId();
                                        }
                                        if (validateUserOtp.getSkip() != null) {
                                            skip = validateUserOtp.getSkip();
                                        }
                                        if (validateUserOtp.getActualSkip() != null) {
                                            actual_skip = validateUserOtp.getActualSkip();
                                        }
                                        isApiRequested = false;
                                        new Handler(Looper.getMainLooper()).post(() -> {
                                            if (!isApiRequested && canPageOnlineMainNews()
                                                    && !bookmarksModeSelected) {
                                                isApiRequested = true;
                                                ShowNews_Data_Values(category_id, 1);
                                            }
                                        });
                                        // Zero paintables mid-skip: loading, not blank white forever.
                                        if (paintablesNow == 0) {
                                            try {
                                                if (txtNoData != null) {
                                                    txtNoData.setVisibility(View.GONE);
                                                }
                                                // Never re-cover an already-painted frame.
                                                if (!fromCache && (list == null || list.isEmpty())) {
                                                    showHomeFeedLoadingReveal();
                                                }
                                            } catch (Exception ignored) {
                                            }
                                            // Don't return when stickies/ads absent — wait for pages.
                                            // Only abort setData path when list is fully empty.
                                            if (list.isEmpty()) {
                                                return;
                                            }
                                        }
                                        // Stickies/partial list: fall through to setData bind.
                                    }
                                } else if (list != null
                                        && ReadArticleStore.countRealArticles(list)
                                                >= ONLINE_NEWS_MIN_UNREAD_CARDS) {
                                    onlineNewsAllReadSkipStreak = 0;
                                }
                            }
                        } catch (Exception ignored) {
                        }
                        // Video News: keep half/full videos + re-place ads every 4 reals
                        // (server short pages + historic inject often under-supply slots).
                        if (!fromCache && isVideoFeedMode()) {
                            rebalanceVideoFeedAdsIfNeeded(list, encreptionRequest);
                            if (list != null
                                    && ReadArticleStore.countPaintableArticles(list) > 0) {
                                onlineNewsAllReadSkipStreak = 0;
                            }
                        }
                        // Main News: seed template from ads_data when response had codes
                        // but no post_type=ad row (short page / strip), then rebalance.
                        if (!fromCache && !isVideoFeedMode() && !isSpecialFeedMode()) {
                            try {
                                if (onlineNewsAdTemplate == null && encreptionRequest != null) {
                                    ModelClass.Datum fromAds =
                                            adShellFromAdsData(encreptionRequest);
                                    if (fromAds != null) {
                                        onlineNewsAdTemplate = fromAds;
                                    }
                                }
                                if (list != null && onlineNewsAdTemplate != null) {
                                    rebalanceOnlineNewsAdsIfNeeded(list);
                                }
                            } catch (Exception ignored) {
                            }
                        }
                        // Also preload using live list slot positions (post-rebalance).
                        if (!fromCache && list != null
                                && encreptionRequest != null
                                && encreptionRequest.isShow_ads()) {
                            try {
                                preloadNativeAdsForFeedSlots(list);
                            } catch (Exception ignored) {
                            }
                        }
                        if (!fromCache && encreptionRequest.isShow_ads() && encreptionRequest.getAd_positions() != null) {
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
                            if (adListSizeBeforeAppend > 0) {
                                new Handler(Looper.getMainLooper()).post(preloadAds);
                            } else {
                                preloadAds.run();
                            }
                        }
                        requestSize=list.size();
                        // Always take cursors from this payload — including disk cache —
                        // so warm reopen / skip-network can keep scrolling past page 0.
                        if (validateUserOtp.getLastPostId() != null) {
                            last_post_id = validateUserOtp.getLastPostId();
                        }
                        if (validateUserOtp.getSkip() != null) {
                            skip = validateUserOtp.getSkip();
                        }
                        if (validateUserOtp.getActualSkip() != null) {
                            actual_skip = validateUserOtp.getActualSkip();
                        }
                        // Video News: keep next-page trigger inside the loaded list so short
                        // first pages (1–7) still prefetch page N+1 before the end.
                        if (!fromCache && isVideoFeedMode() && list != null && !list.isEmpty()) {
                            int n = list.size();
                            // Load more when flipping near the end (or sooner if list is tiny).
                            requestPostition = Math.max(1, Math.min(n - 1, Math.max(1, n - 3)));
                            // If first batches look full (or near-full), prefetch immediately so
                            // the user can scroll through hundreds of videos without stalling.
                            final boolean maybeMore = validateUserOtp.getData().size() >= 6;
                            if (maybeMore && !isApiRequested && dummyResponse == null && isDeviceOnline()) {
                                final int prefetchAt = requestPostition;
                                showLog("video feed prefetch at pos=" + prefetchAt
                                        + " list=" + n + " skip=" + skip);
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (!isVideoFeedMode() || !isDeviceOnline()) return;
                                        if (dummyResponse != null || isApiRequested) return;
                                        if (list == null || list.isEmpty()) return;
                                        isApiRequested = true;
                                        ShowNews_Data_Values(category_id, 3);
                                    }
                                });
                            }
                        } else if (!fromCache
                                && canPageOnlineMainNews()
                                && !bookmarksModeSelected
                                && list != null
                                && !list.isEmpty()) {
                            // Online News: hold next page before the end so the user
                            // is not stranded on a terminal last card.
                            int n = list.size();
                            requestPostition = Math.max(1, Math.min(n - 1, Math.max(1, n - 3)));
                            int reals = ReadArticleStore.countRealArticles(list);
                            if (reals < ONLINE_NEWS_MIN_UNREAD_CARDS * 2
                                    && !isApiRequested
                                    && dummyResponse == null
                                    && onlineNewsAllReadSkipStreak
                                            < ONLINE_NEWS_ALL_READ_SKIP_CAP) {
                                new Handler(Looper.getMainLooper()).post(() -> {
                                    if (!canPageOnlineMainNews() || bookmarksModeSelected) {
                                        return;
                                    }
                                    if (dummyResponse != null || isApiRequested) return;
                                    isApiRequested = true;
                                    ShowNews_Data_Values(category_id, 3);
                                });
                            }
                        }
                        showLog("pagination cursors fromCache=" + fromCache
                                + " skip=" + skip + " actual_skip=" + actual_skip
                                + " last_post_id=" + last_post_id
                                + " page0Replace=" + page0Replace);
                        if (list != null && !list.isEmpty() && list.get(0).getPostId() != null) {
                            updated_post_id_firebase = list.get(0).getPostId().intValue();
                        }
                        // Consume push deep-link once the push article is in the feed.
                        // Leaving TITLE sticky re-sent push_post_id on every skip=0
                        // (tab switch / Reload) and inflated server Open without a new tap.
                        if (!fromCache && listSizeBeforeAppend == 0 && TITLE_VALUE > 0) {
                            showLog("clearing push_post_id after land TITLE_VALUE=" + TITLE_VALUE);
                            clearStickyPushPostId();
                        }
                        showLog("response body not null:"+isFromLocalArticles);
                        //writeNewUser("User", type_firebase, current_time_firebase, draft_id_firebase, device_id_firebase, language_id_firebase, updated_post_id_firebase, positionx);
                        if(isFromLocalArticles || fromCache || adapter == null){
                           isFromLocalArticles=false;
                           if (languageModelResponse != null && languageModelResponse.getData() != null) {
                               if (category_id == 0) {
                                   type = DistrictLabelHelper.resolveDisplayLabel(HomePage.this);
                               } else {
                                   type = languageModelResponse.getData().getMyFeed().toString();
                               }
                           }
                           if (adapter == null || fromCache) {
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
                       }
                        // Theme recreate can leave adapter null if inlist skipped creation
                        // (e.g. translator prefs not ready yet). Ensure adapter exists so
                        // flip restore can run for light↔dark.
                        if (adapter == null && mFlipView != null) {
                            showLog("adapter null after articles — creating for flip restore");
                            if (type == null || type.isEmpty() || "null".equalsIgnoreCase(String.valueOf(type).trim())) {
                                type = OfflineFeedStore.ensureDistrictPrefs(HomePage.this);
                                if (TextUtils.isEmpty(type)) {
                                    type = DistrictLabelHelper.resolveDisplayLabel(HomePage.this);
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
                        if (adapter == null) {
                            showLog("adapter null after articles response");
                        } else {
                            showLog("response body not null after load data:");
                            adapter.hasStableIds();
                            if (!fromCache && listSizeBeforeAppend > 0 && listSizeBeforeAppend < list.size()) {
                                adapter.appendData(list, listSizeBeforeAppend);
                            } else {
                                adapter.setData(list);
                            }
                            if (!fromCache) {
                                maybeRestoreFlipPositionAfterArticlesLoad();
                            }
                            // Always show flip when list has paintables — never empty white forever.
                            if (list != null && !list.isEmpty()) {
                                mFlipView.setVisibility(View.VISIBLE);
                                txtNoData.setVisibility(View.GONE);
                                layout_with_data.setVisibility(View.VISIBLE);
                            }
                            hideHomeFeedLoadingReveal(fromCache);
                            if (list != null && !list.isEmpty()) {
                                hideHomeFeedLoadingReveal(true);
                            }
                            // Don't leave first seat on an empty ad spinner when real cards exist.
                            try {
                                seatAwayFromUnfilledAdIfNeeded();
                            } catch (Exception ignored) {
                            }
                            // Share / sticky open: flip to the exact post on News when present.
                            try {
                                maybeLandDeepLinkPostOnNews(fromCache);
                            } catch (Exception ignored) {
                            }
                            // Synthetic-only list (ads/share) mid-skip: keep paging, not spinner forever.
                            // Same for Video/Special (canPageOnlineMainNews is false there).
                            try {
                                if (!fromCache
                                        && list != null
                                        && !bookmarksModeSelected
                                        && canPageActiveOnlineFlipFeed()
                                        && ReadArticleStore.countPaintableArticles(list) == 0) {
                                    resolveSoftEmptyNewsFeed("paint_only_synthetics");
                                }
                            } catch (Exception ignored) {
                            }
                            if (!fromCache) {
                                clearOfflineOnlyEmptyStatesIfFeedReady();
                            }
                            try {
                                refreshHomeModeToggleUi();
                                ensureOnlineBottomNavVisible();
                            } catch (Exception ignored) {
                            }

                            if(!fromCache && allsharedpreference.getShowFirstTimeAnimation()){
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
                            // Empty payload still carries next skip/actual_skip/last_post_id.
                            // Apply cursors first so densify walks deeper (all-read strip +
                            // exclude top-up shortfall) instead of re-requesting the same page.
                            int skipBeforeEmpty = skip;
                            long actualBeforeEmpty = actual_skip;
                            if (!fromCache) {
                                try {
                                    if (validateUserOtp.getLastPostId() != null) {
                                        last_post_id = validateUserOtp.getLastPostId();
                                    }
                                    if (validateUserOtp.getSkip() != null) {
                                        skip = validateUserOtp.getSkip();
                                    }
                                    if (validateUserOtp.getActualSkip() != null) {
                                        actual_skip = validateUserOtp.getActualSkip();
                                    }
                                } catch (Exception ignored) {
                                }
                            }
                            final boolean cursorAdvanced = !fromCache
                                    && (skip > skipBeforeEmpty
                                            || actual_skip > actualBeforeEmpty);
                            if (list.size() == 0 && !fromCache) {
                                if (canPageOnlineMainNews()
                                        && cursorAdvanced
                                        && onlineNewsAllReadSkipStreak
                                                < ONLINE_NEWS_ALL_READ_SKIP_CAP) {
                                    // Server page empty after densify window - keep walking
                                    // while cursor advances (older unread may still exist).
                                    resolveSoftEmptyNewsFeed("api_empty_page_data");
                                    initialCall = true;
                                } else {
                                    // True end of catalog (no cursor progress) or skip cap.
                                    mFlipView.setVisibility(View.GONE);
                                    layout_with_data.setVisibility(View.VISIBLE);
                                    hideHomeFeedLoadingReveal(fromCache);
                                    post_news_scrren.setVisibility(View.GONE);
                                    txtNoData.setVisibility(View.VISIBLE);
                                    try {
                                        if (nodatascreen != null && isVideoFeedMode()) {
                                            nodatascreen.setText(getString(R.string.video_news_empty));
                                        }
                                    } catch (Exception ignored) {
                                    }
                                    initialCall=true;
                                    showLog("txtNoData VISIBLE:: empty page end"
                                            + " cursorAdvanced=" + cursorAdvanced
                                            + " streak=" + onlineNewsAllReadSkipStreak);
                                }
                            } else if (!fromCache) {
                                no_article_message="No articles to read further";
                                // List has cards; empty next page. Keep densify only when
                                // under min unread and cursor still moves (continuous fill).
                                try {
                                    if (canPageOnlineMainNews()
                                            && !bookmarksModeSelected
                                            && cursorAdvanced
                                            && list != null
                                            && ReadArticleStore.countRealArticles(list)
                                                    < ONLINE_NEWS_MIN_UNREAD_CARDS
                                            && onlineNewsAllReadSkipStreak
                                                    < ONLINE_NEWS_ALL_READ_SKIP_CAP
                                            && !isApiRequested) {
                                        onlineNewsAllReadSkipStreak++;
                                        isApiRequested = false;
                                        new Handler(Looper.getMainLooper()).post(() -> {
                                            if (!isApiRequested && canPageOnlineMainNews()
                                                    && !bookmarksModeSelected) {
                                                isApiRequested = true;
                                                ShowNews_Data_Values(category_id, 3);
                                            }
                                        });
                                    }
                                } catch (Exception ignored) {
                                }
                            }

                        }
                    }
                } else if (!fromCache) {
                    mFlipView.setVisibility(View.GONE);
                    layout_with_data.setVisibility(View.VISIBLE);
                    hideHomeFeedLoadingReveal(fromCache);
                    post_news_scrren.setVisibility(View.GONE);
                    txtNoData.setVisibility(View.VISIBLE);
                    initialCall=true;
                    showLog("txtNoData VISIBLE::");

                }

                if(!fromCache && encreptionRequest.isUpdate_user_language()){
                    LanguageSelection(SharedPrefsData.getInt(getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME));

                }


                showReporterLogin=encreptionRequest.isShow_reporter_login();
                showLog("showReporterLogin:"+showReporterLogin);
                updateCenterFab();
                // Admin re-prompt can run even on cache paint; daily/legacy only when live.
                boolean duePreferPrefs = SharedPrefsData.getBool(
                        getApplicationContext(),
                        Constants.NOTIFICATION_REPROMPT_DUE,
                        Constants.PREF_NAME,
                        false)
                        && SharedPrefsData.getBool(
                                getApplicationContext(),
                                Constants.NOTIFICATION_REPROMPT_ENABLED,
                                Constants.PREF_NAME,
                                false);
                if (!fromCache || duePreferPrefs) {
                    Boolean pushPermAlert = encreptionRequest.getShow_push_perm_alert();
                    showNotificationAlert(
                            (pushPermAlert != null && pushPermAlert) || duePreferPrefs);
                }
            }else if (!fromCache){
                isDataParsed=true;
                showLog("response body null");
                no_article_message="No articles to read further";
                if (list == null || list.isEmpty()) {
                    hideHomeFeedLoadingReveal(true);
                    if (mFlipView != null) {
                        mFlipView.setVisibility(View.GONE);
                    }
                    if (layout_with_data != null) {
                        layout_with_data.setVisibility(View.VISIBLE);
                    }
                    if (txtNoData != null) {
                        txtNoData.setVisibility(View.VISIBLE);
                    }
                    initialCall = true;
                } else {
                    hideHomeFeedLoadingReveal(true);
                }
            }

            initialCall=true;
            home_page_navigation.setVisibility(View.VISIBLE);
            ensureOnlineBottomNavVisible();
            if (!fromCache) {
                isApiRequested=false;
            }

        } catch (Exception e) {
            // Gson JsonSyntaxException (e.g. survey options as plain strings) must not crash open.
            e.printStackTrace();
            showLog("convertListOfArticles parse error: " + e.getMessage());
            // Don't leave the solid dark loading surface indefinitely after a parse error.
            try {
                hideHomeFeedLoadingReveal(true);
                if (list != null && !list.isEmpty()) {
                    if (adapter != null) {
                        adapter.setData(list);
                    }
                    if (mFlipView != null) {
                        mFlipView.setVisibility(View.VISIBLE);
                    }
                    if (layout_with_data != null) {
                        layout_with_data.setVisibility(View.VISIBLE);
                    }
                } else if (!fromCache) {
                    if (mFlipView != null) {
                        mFlipView.setVisibility(View.GONE);
                    }
                    if (layout_with_data != null) {
                        layout_with_data.setVisibility(View.VISIBLE);
                    }
                    if (txtNoData != null) {
                        txtNoData.setVisibility(View.VISIBLE);
                    }
                    initialCall = true;
                }
            } catch (Exception ignored) {
            }
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
                    if (isCenterPlusMenuOpen()) {
                        closeCenterPlusMenu(true);
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

    /**
     * Legacy connectivity dialog used by old receivers.
     * Online: clear block. Offline: never full-screen “No internet!” on Home —
     * keep yes_internet (Offline News pack path is applied by {@link #syncNetworkOverlay}).
     */
    public static void dialog(boolean value) {
        if (yes_internet == null || no_internet == null) {
            return;
        }
        // Always keep feed host visible; offline chrome is handled on the instance path.
        yes_internet.setVisibility(View.VISIBLE);
        no_internet.setVisibility(View.GONE);
        if (value) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(() -> {
                if (yes_internet != null) {
                    yes_internet.setVisibility(View.VISIBLE);
                }
                if (no_internet != null) {
                    no_internet.setVisibility(View.GONE);
                }
            }, 500);
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
     * Admin push tray appearance defaults (logo / show article image / style / sizes).
     * Cached for {@link com.anyNews.anynews.Services.MyFirebaseMessagingService}.
     * Requires the Play-updated APK layout; knobs apply without another rebuild.
     */
    private void applyPushNotificationAppearanceConfig(
            Boolean showArticleImage,
            String styleMode,
            String largeIconUrl,
            Boolean includeLogoDefault,
            Integer leftIconSizeDp,
            Integer thumbSizeDp,
            Integer titleSizeSp) {
        try {
            if (showArticleImage != null) {
                SharedPrefsData.putBool(
                        HomePage.this,
                        Constants.PN_SHOW_ARTICLE_IMAGE,
                        showArticleImage,
                        Constants.PREF_NAME);
            }
            if (styleMode != null && !styleMode.isEmpty()) {
                SharedPrefsData.putString(
                        HomePage.this, Constants.PN_STYLE_MODE, styleMode, Constants.PREF_NAME);
            }
            if (largeIconUrl != null) {
                SharedPrefsData.putString(
                        HomePage.this, Constants.PN_LARGE_ICON_URL, largeIconUrl, Constants.PREF_NAME);
            }
            if (includeLogoDefault != null) {
                SharedPrefsData.putBool(
                        HomePage.this,
                        Constants.PN_INCLUDE_LOGO_DEFAULT,
                        includeLogoDefault,
                        Constants.PREF_NAME);
            }
            if (leftIconSizeDp != null && leftIconSizeDp > 0) {
                SharedPrefsData.putInt(
                        HomePage.this,
                        Constants.PN_LEFT_ICON_SIZE_DP,
                        leftIconSizeDp,
                        Constants.PREF_NAME);
            }
            if (thumbSizeDp != null && thumbSizeDp > 0) {
                SharedPrefsData.putInt(
                        HomePage.this,
                        Constants.PN_THUMB_SIZE_DP,
                        thumbSizeDp,
                        Constants.PREF_NAME);
            }
            if (titleSizeSp != null && titleSizeSp > 0) {
                SharedPrefsData.putInt(
                        HomePage.this,
                        Constants.PN_TITLE_SIZE_SP,
                        titleSizeSp,
                        Constants.PREF_NAME);
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
        // Offline: never fire List_All_Articles / held next-page poll (ShowNews also guards).
        if (!isDeviceOnline()) {
            showLog("refreshDetails skip network while offline");
            isApiRequested = false;
            try {
                if (bottomNavigationView != null
                        && bottomNavigationView.getMenu().size() > 0) {
                    bottomNavigationView.getMenu().getItem(0).setIcon(R.drawable.refresh_icon);
                    applyFooterTabLabelsFromTranslator();
                }
            } catch (Exception ignored) {
            }
            return;
        }
        // Online Read: flip local postcards only — no News pagination.
        if (isOnlineReadFlipMode()) {
            showLog("refreshDetails skip news pagination in Read flip");
            isApiRequested = false;
            try {
                if (bottomNavigationView != null
                        && bottomNavigationView.getMenu().size() > 0) {
                    bottomNavigationView.getMenu().getItem(0).setIcon(R.drawable.refresh_icon);
                    applyFooterTabLabelsFromTranslator();
                }
            } catch (Exception ignored) {
            }
            return;
        }
        if(positionx==requestPostition){
            showLog("request at position::"+requestSize);
            isApiRequested=true;
            ShowNews_Data_Values(category_id,3);
        } else if (isVideoFeedMode()
                && list != null
                && !list.isEmpty()
                && !isApiRequested
                && dummyResponse == null
                && isDeviceOnline()
                && positionx >= Math.max(0, list.size() - 2)
                && !"No articles to read further".equals(no_article_message)) {
            // Video News safety: always fetch the next batch when near the end of
            // whatever is currently loaded (small pages used to miss requestPostition).
            showLog("video feed end-edge prefetch pos=" + positionx + " size=" + list.size());
            isApiRequested = true;
            ShowNews_Data_Values(category_id, 3);
        } else if (canPageOnlineMainNews()
                && !bookmarksModeSelected
                && list != null
                && !list.isEmpty()
                && !isApiRequested
                && dummyResponse == null
                && positionx >= Math.max(0, list.size() - 2)
                && !"No articles to read further".equals(no_article_message)) {
            // Online News: near end after strip/page load — keep paging unread.
            showLog("online News end-edge prefetch pos=" + positionx
                    + " size=" + list.size()
                    + " reals=" + ReadArticleStore.countRealArticles(list));
            isApiRequested = true;
            ShowNews_Data_Values(category_id, 3);
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
            applyFooterTabLabelsFromTranslator();
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
        // New page: cancel pending autoplay chain and allow autoplay again (user pause was for prior page).
        cancelVideoAutoplayChain();
        cancelVideoAutoplayStart();
        if (adapter != null) {
            adapter.clearUserPausedVideoAutoplay();
        }
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
        // Online: always keep bottom tabs. Offline/ads may still hide (legacy).
        if (isDeviceOnline() && !offlineChromeMode) {
            ensureOnlineBottomNavVisible();
        } else if (post_type.equalsIgnoreCase("ad")
                || post_type.equalsIgnoreCase("share_app")
                || post_type.equalsIgnoreCase("ratings")) {
            home_page_navigation.setVisibility(View.INVISIBLE);
        } else {
            home_page_navigation.setVisibility(View.VISIBLE);
        }
        // Ads / Share shell: hide News|Read pill (no meaningful article chrome).
        try {
            refreshHomeModeToggleUi();
        } catch (Exception ignored) {
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
            if (!isSpecialFeedMode() && isDeviceOnline()) {
                OfflinePackPrefetcher.get().onMainFlipSettled(getApplicationContext());
            }
        } catch (Exception ignored) {
        }
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
            // Current slot first (rebalance may have new positions not in page ads_data)
            try {
                String code = list.get(position).getAd_code();
                if (code == null || code.isEmpty()) {
                    code = list.get(position).getNext_req_ad_code();
                }
                ensureFeedNativeAdLoaded(
                        code,
                        list.get(position).getAd_req_position(),
                        list.get(position).getAd_type());
            } catch (Exception ignored) {
            }
            // Prefetch next shell unit
            try {
                loadAds(list.get(position).getNext_req_ad_code(),
                        list.get(position).getNext_ad_req_position(),
                        list.get(position).getAd_type());
            } catch (Exception ignored) {
            }
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
        // Drop handler only — do not consume session so process-scoped first-open
        // auto-hide can still arm if Home is recreated before it fired.
        cancelHomeModeChromeSessionAutoHide(false);
        cancelPendingFlipSettleCallbacks();
        cancelHomeFeedLoadingSafetyTimeout();
        stopReadBadgePoll();
        try {
            OfflinePackPrefetcher.get().bindHost(null);
            OfflineFeedStore.setPackChangeListener(null);
            OfflinePackPrefetcher.get().resetSession();
        } catch (Exception ignored) {
        }
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
            unregisterOfflineConnectivityCallbacks();
        } catch (Exception e) {
            e.printStackTrace();
        }
        showLog("isUnreadPostsSelected set to false #4");
    }


    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int nightMask = newConfig.uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (nightMask != appliedNightUiMask) {
            // Defer heavy recreate while Settings (or another activity) is on top.
            pendingNightModeRecreate = true;
            if (!isFinishing() && hasWindowFocus()) {
                maybeRecreateForNightModeChange();
            }
        }
    }

    /**
     * One recreate after manual night toggle so DayNight resources re-inflate.
     * Prefs flip-restore keeps the feed session (no splash / CLEAR_TASK).
     */
    private void maybeRecreateForNightModeChange() {
        if (!pendingNightModeRecreate && !isNightUiOutOfSync()) {
            return;
        }
        pendingNightModeRecreate = false;
        appliedNightUiMask =
                getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        try {
            // Refresh flip checkpoint so onCreate restore survives this single recreate.
            int flipIndex = resolveCurrentFlipIndexForSave();
            long postId = resolvePostIdForFlipIndex(flipIndex);
            persistFlipRestoreCheckpoint(flipIndex, postId, flipIndex > 0 || postId > 0);
        } catch (Exception ignored) {
        }
        com.anyNews.anynews.Utils.NightModeHelper.recreateWithFade(this);
    }

    private boolean isNightUiOutOfSync() {
        boolean confNight =
                (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                        == Configuration.UI_MODE_NIGHT_YES;
        return confNight != com.anyNews.anynews.Utils.NightModeHelper.isEnabled(this)
                || ((getResources().getConfiguration().uiMode
                                & Configuration.UI_MODE_NIGHT_MASK)
                        != appliedNightUiMask);
    }

    @Override
    public void onResume() {
        super.onResume();
        homeActivityResumed = true;
        // If create-menu was left open while backgrounded, collapse so feed + icon match.
        try {
            if (isCenterPlusMenuOpen()) {
                closeCenterPlusMenu(false);
            }
        } catch (Exception ignored) {
        }
        try {
            if (pendingNightModeRecreate || isNightUiOutOfSync()) {
                maybeRecreateForNightModeChange();
                return;
            }
        } catch (Exception ignored) {
        }
        try {
            com.anyNews.anynews.Utils.BookmarkStore.flushPending(getApplicationContext());
            OfflineActionQueue.flushAll(getApplicationContext());
            ViewsBatchTracker.get().flush(getApplicationContext(), false);
        } catch (Exception ignored) {
        }
        // Lock/unlock (and Doze) often miss CONNECTIVITY_ACTION while backgrounded.
        // Re-check on foreground so "No internet!" clears when network is back.
        syncNetworkOverlay(true);
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
            if (isOnlineMainNewsChromeActive()) {
                ensureReadBadgePollRunning();
            }
        } catch (Exception ignored) {
        }
        try {
            // Admin re-prompt / install POST_NOTIFICATIONS before soft GPS so one dialog at a time.
            maybeRequestInstallAndRepromptPermissions();
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
        try {
            // Re-resolve district for current Language_Id after background resume /
            // theme toggle so the pill never keeps a stale Telugu (etc.) label.
            refreshDistrictLabelsForCurrentLanguage();
        } catch (Exception ignored) {
        }
        try {
            refreshHomeModeToggleUi();
            refreshHomeModeCounts();
            if (bookmarksModeSelected) {
                if (isOnlineMainNewsChromeActive()) {
                    reloadHomeReadNewsPanel();
                } else {
                    reloadHomeBookmarksPanel();
                    try {
                        BookmarkStore.syncFromServer(this, () -> {
                            reloadHomeBookmarksPanel();
                            refreshHomeModeCounts();
                        });
                    } catch (Exception ignored) {
                    }
                }
            }
        } catch (Exception ignored) {
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

    /**
     * Re-bind header + in-memory type + Flipper location pill for the selected language.
     * Call on resume / after language prefs may have changed.
     */
    private void refreshDistrictLabelsForCurrentLanguage() {
        String label = DistrictLabelHelper.resolveDisplayLabel(HomePage.this);
        if (category_id == 0 && DistrictLabelHelper.isUsable(label)) {
            type = label;
        }
        DistrictLabelHelper.applyTo(HomePage.this, local_distic_name, selelcted_local_distic_name);
        if (adapter != null && category_id == 0) {
            try {
                // Force visible page(s) to re-bind via partial notify when possible.
                adapter.notifyDataSetChanged();
            } catch (Exception ignored) {
            }
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

        if (!isDeviceOnline()) {
            try {
                OfflineActionQueue.enqueueLike(
                        getApplicationContext(),
                        post_id != null ? post_id : 0L,
                        sub_post_id != null ? sub_post_id : 0L,
                        type);
                Toast.makeText(this, R.string.offline_action_queued, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

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
        }, 1600L);
    }

    @Override
    public void onPause() {
        homeActivityResumed = false;
        stopReadBadgePoll();
        try {
            OfflinePackPrefetcher.get().onHostPaused();
        } catch (Exception ignored) {
        }
        try {
            PostDwellTracker.get().onPaused(this);
        } catch (Exception ignored) {
        }
        // new flip — collapse reporter create-menu so pause never leaves a stuck X.
        try {
            closeCenterPlusMenu(false);
        } catch (Exception ignored) {
        }
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
    private final BottomNavigationView.OnNavigationItemReselectedListener mOnNavigationItemReselectedListener
            = new BottomNavigationView.OnNavigationItemReselectedListener() {
        @Override
        public void onNavigationItemReselected(@NonNull MenuItem item) {
            if (suppressBottomNavCallbacks) {
                return;
            }
            // Center + returns false (never stays selected). Reselecting Reload / Special
            // while create-menu is open must still dismiss X + overlay.
            if (item.getItemId() != R.id.navigation_stricker) {
                closeCenterPlusMenu(true);
            }
            if (item.getItemId() == R.id.navigation_home) {
                // User tapped already-selected Reload (రీలోడ్) — same as cold soft restart.
                if (initialCall) {
                    performFeedReload();
                }
            } else if (item.getItemId() == R.id.navigation_catagiries) {
                // Re-tap Special while already on Special: always restart from article 0.
                performSpecialFeedOpen();
            } else if (item.getItemId() == R.id.navigation_status) {
                performVideoFeedOpen();
            }
        }
    };
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            if (suppressBottomNavCallbacks) {
                return true;
            }
            Intent intent;
            switch (item.getItemId()) {
                // ... implement code here
                case R.id.navigation_home:
                    if (!ProfileHubConfigHelper.isFooterShowReload(HomePage.this)) {
                        return false;
                    }
                    closeCenterPlusMenu(true);
                    showLog("initialCall:::" + initialCall);
                    try {
                        if (languageModelResponse != null && languageModelResponse.getData() != null) {
                            showLog("RELOAD:::" + languageModelResponse.getData().getReload());
                        }
                    } catch (Exception ignored) {
                    }
                    showLog("RELOAD getTitle:::" + item.getTitle().toString());
                    showLog(" layout_with_out_data:::" + layout_with_out_data.getVisibility());
                    showLog(" layout_with_data:::" + layout_with_data.getVisibility());
                    showLog(" txtNoData:::" + txtNoData.getVisibility());
                    if (initialCall) {
                        // Soft restart: clear push sticky + flip restore + fetch skip=0.
                        // Leaves Special if open and reloads My Feed page 0.
                        // Avoid activity CLEAR_TASK recreate (re-applied flip-restore prefs
                        // and could re-land with stale state).
                        performFeedReload();
                    }
                    return true;
                case R.id.navigation_catagiries:
                    if (!ProfileHubConfigHelper.isFooterShowSpecial(HomePage.this)
                            || !isDeviceOnline()) {
                        return false;
                    }
                    closeCenterPlusMenu(true);
                    // Soft Special open at page 0 — do not recreate activity or keep
                    // main-feed skip/actual_skip/last_post_id / held next page.
                    performSpecialFeedOpen();
                    return true;
                case R.id.navigation_status:
                    if (!ProfileHubConfigHelper.isFooterShowVideos(HomePage.this)
                            || !isDeviceOnline()) {
                        return false;
                    }
                    closeCenterPlusMenu(true);
                    performVideoFeedOpen();
                    return true;
                case R.id.navigation_stricker:
                    // Center + only when admin shows it and device is online.
                    if (!ProfileHubConfigHelper.isFooterShowPlus(HomePage.this)
                            || !isDeviceOnline()) {
                        return false;
                    }
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
                        try {
                            if (countDownTimer != null) {
                                countDownTimer.cancel();
                                countDownTimer = null;
                            }
                        } catch (Exception ignored) {
                        }
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
                                // Toggle: open → close with +, closed → open with X.
                                if (isCenterPlusMenuOpen()) {
                                    closeCenterPlusMenu(true);
                                } else {
                                    boolean is_login = SharedPrefsData.getBool(HomePage.this, Constants.is_login, Constants.PREF_NAME);
                                    if (is_login) {
                                        openCenterPlusMenu();
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
                case R.id.navigation_profile:
                    // Profile is online-only (hidden offline with Special / Video / +).
                    if (!ProfileHubConfigHelper.isFooterShowProfile(HomePage.this)
                            || !isDeviceOnline()) {
                        return false;
                    }
                    closeCenterPlusMenu(true);
                    showLog("isUnreadPostsSelected set to false #3");
                    setFeedModeFlags(false, false);
                    // firebase events triggered
                    writeButtonClick("User", type_firebase_button, current_time_firebase, draft_id_firebase, device_id_firebase, button_name_profile);

                     intent = new Intent(HomePage.this, newProfileDetails.class);
                    if (getApplicationContext() != null && getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)) {
                        startActivity(intent);
                        overridePendingTransition(R.anim.activity_slide_up_in, R.anim.activity_hold);
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
            // Local read/open store only — no server mark-as-read per flip.
            markCurrentArticleReadLocally(postId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Session-only view log: pending id + postcard cache. Does <b>not</b> soft-remove
     * from News, rebalance for read, or refresh the Read badge. Permanent commit happens
     * once per process start via {@link ReadArticleStore#commitSessionViewedToReadOncePerProcess}.
     * Never hits read/unread APIs.
     */
    private void markCurrentArticleReadLocally(long postId) {
        if (postId <= 0) return;
        ModelClass.Datum snap = null;
        try {
            if (list != null) {
                for (int i = 0; i < list.size(); i++) {
                    ModelClass.Datum d = list.get(i);
                    if (d != null && d.getPostId() != null && d.getPostId() == postId) {
                        snap = d;
                        break;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        // Ads / share shells: never enter the Read store. Stickies session-view like normal posts.
        if (snap != null && ReadArticleStore.isSyntheticFeedSlot(snap)) {
            return;
        }
        try {
            ReadArticleStore.markSessionViewed(getApplicationContext(), postId, snap);
        } catch (Exception e) {
            try {
                ArticleOpenLog.markOpened(getApplicationContext(), postId);
            } catch (Exception ignored) {
            }
        }
        // Mid-session: leave FlipperAdapter News list untouched (no soft-remove / rebalance).
    }

    /**
     * After page-0 / pagination merge: drop <b>committed</b> already-read real articles
     * for online News tab. Session-pending views remain until next process commit.
     */
    private void stripReadArticlesIfOnlineNews(@androidx.annotation.Nullable java.util.List<ModelClass.Datum> items) {
        if (items == null || items.isEmpty()) return;
        if (!isDeviceOnline() || offlineChromeMode) return;
        if (isSpecialFeedMode() || isVideoFeedMode()) return;
        // Read flip intentionally holds already-read cards.
        if (bookmarksModeSelected) return;
        try {
            rememberOnlineNewsAdTemplate(items);
            rememberFeedStickiesFrom(items);
            // Hold the share/push land target through strip until land/consume — including
            // committed-read ids so maybeLand can hand off to Chadivinavi with a postcard.
            java.util.Collection<Long> hold = null;
            long landId = resolvePendingDeepLinkPostId();
            if (landId > 0) {
                hold = java.util.Collections.singleton(landId);
            }
            ReadArticleStore.stripReadFromFeed(getApplicationContext(), items, hold);
            rebalanceOnlineNewsAdsIfNeeded(items);
        } catch (Exception ignored) {
        }
    }

    /**
     * Share URL / sticky open target: {@link #SHARE_ID} preferred, else push {@code TITLE}.
     */
    private long resolvePendingDeepLinkPostId() {
        if (SHARE_ID > 0) {
            return SHARE_ID;
        }
        try {
            if (pushUpdatedID != null) {
                String raw = pushUpdatedID.trim();
                if (!raw.isEmpty()
                        && !raw.equalsIgnoreCase("null")
                        && !raw.equals("0")) {
                    long v = Long.parseLong(raw);
                    if (v > 0) return v;
                }
            }
        } catch (Exception ignored) {
        }
        return TITLE_VALUE > 0 ? (long) TITLE_VALUE : 0L;
    }

    @Nullable
    private ModelClass.Datum findDatumInPage(
            @Nullable java.util.List<ModelClass.Datum> pageData, long postId) {
        if (pageData == null || postId <= 0) return null;
        for (ModelClass.Datum d : pageData) {
            if (d != null && d.getPostId() != null && d.getPostId() == postId) {
                return d;
            }
        }
        return null;
    }

    /**
     * One-shot deep-link already in Read history → switch to Chadivinavi and show that card.
     * Absorbs the live News page into the unread snapshot so returning to News stays warm.
     *
     * @return true when FlipView is owned by Read and the caller should stop News paint
     */
    private boolean tryOpenShareDeepLinkOnReadTab(
            @Nullable java.util.List<ModelClass.Datum> pageData,
            @Nullable EncreptionRequest bodyForSnapshot,
            boolean page0Replace) {
        long targetId = resolvePendingDeepLinkPostId();
        if (targetId <= 0) return false;
        if (!isDeviceOnline() || offlineChromeMode) return false;
        if (isSpecialFeedMode() || isVideoFeedMode()) return false;
        // Only route **committed** Read history here. Session-pending (viewed this
        // process but still on News) stays on News so the share open can flip there.
        if (!ReadArticleStore.isRead(getApplicationContext(), targetId)) {
            return false;
        }
        ModelClass.Datum land = findDatumInPage(pageData, targetId);
        if (land == null && list != null) {
            int i = findListIndexForPostId(targetId);
            if (i >= 0) {
                land = list.get(i);
            }
        }
        if (land == null) {
            land = ReadArticleStore.getCached(getApplicationContext(), targetId);
        }
        if (land == null) {
            return false;
        }
        // Refresh postcard on disk when the feed returned a fuller payload.
        try {
            ReadArticleStore.markRead(getApplicationContext(), targetId, land);
        } catch (Exception ignored) {
        }
        showLog("deep-link land → Read (Chadivinavi) postId=" + targetId);
        if (!bookmarksModeSelected) {
            try {
                snapshotOnlineNewsFlipSession();
            } catch (Exception ignored) {
            }
            onlineNewsReadSessionEpoch++;
            bookmarksModeSelected = true;
            applyHomeModeContentVisibility();
            try {
                refreshHomeModeToggleUi();
                refreshHomeModeCounts();
                refreshHomeReadNewsBadge();
            } catch (Exception ignored) {
            }
        }
        paintOnlineReadNewsFlip(targetId, land);
        try {
            if (bodyForSnapshot != null) {
                absorbMainFeedResponseIntoNewsSnapshot(bodyForSnapshot, page0Replace);
            }
        } catch (Exception ignored) {
        }
        consumeDeepLinkStickyIds();
        return true;
    }

    /**
     * After News list paint: flip to share/push post when it is in the deck, or hand
     * committed-read shares to Chadivinavi. Cache paint may land early if present;
     * otherwise wait for sticky network. One rescue page-0 re-pins via share_post_id.
     */
    private void maybeLandDeepLinkPostOnNews(boolean fromCache) {
        if (bookmarksModeSelected) return;
        long targetId = resolvePendingDeepLinkPostId();
        if (targetId <= 0) return;
        int idx = findListIndexForPostId(targetId);
        if (idx < 0) {
            // Committed-read postcard only (no list row yet).
            if (!fromCache
                    && ReadArticleStore.isRead(getApplicationContext(), targetId)) {
                ModelClass.Datum cached =
                        ReadArticleStore.getCached(getApplicationContext(), targetId);
                if (cached != null
                        && tryOpenShareDeepLinkOnReadTab(
                                java.util.Collections.singletonList(cached),
                                null,
                                false)) {
                    return;
                }
            }
            if (!fromCache && scheduleDeepLinkShareRescue(targetId)) {
                return;
            }
            if (!fromCache) {
                showLog("deep-link News land: postId=" + targetId + " not in list after network");
                // Avoid sticky force-network loops if server never pins the post.
                consumeDeepLinkStickyIds();
            }
            return;
        }
        // In deck: Read history → Chadivinavi; else stay on News and flip exact card.
        if (ReadArticleStore.isRead(getApplicationContext(), targetId)) {
            ModelClass.Datum land = null;
            try {
                if (idx >= 0 && list != null && idx < list.size()) {
                    land = list.get(idx);
                }
            } catch (Exception ignored) {
            }
            if (tryOpenShareDeepLinkOnReadTab(
                    land != null
                            ? java.util.Collections.singletonList(land)
                            : null,
                    null,
                    false)) {
                return;
            }
        }
        showLog("deep-link land → News flip postId=" + targetId + " idx=" + idx);
        flipToDeepLinkPage(idx);
        consumeDeepLinkStickyIds();
    }

    /**
     * Sticky share miss: one page-0 with share_post_id so List_All_Articles can
     * {@code findPostById} and pin the article. Preserves SHARE_ID (no cursor wipe).
     */
    private boolean scheduleDeepLinkShareRescue(long targetId) {
        if (targetId <= 0 || deepLinkShareRescueAttempted) return false;
        if (SHARE_ID <= 0 && resolvePendingDeepLinkPostId() <= 0) return false;
        if (!isDeviceOnline() || offlineChromeMode) return false;
        if (isSpecialFeedMode() || isVideoFeedMode()) return false;
        if (isApiRequested) return false;
        deepLinkShareRescueAttempted = true;
        if (SHARE_ID <= 0) {
            SHARE_ID = targetId;
            SHARE_REDIRECT_ID = String.valueOf(targetId);
        }
        showLog("deep-link rescue page-0 share_post_id=" + SHARE_ID);
        new Handler(Looper.getMainLooper()).post(() -> {
            try {
                if (resolvePendingDeepLinkPostId() <= 0) return;
                if (findListIndexForPostId(targetId) >= 0) {
                    maybeLandDeepLinkPostOnNews(false);
                    return;
                }
                last_post_id = 0;
                skip = 0;
                actual_skip = 0;
                isApiRequested = true;
                isDataParsed = true;
                dummyResponse = null;
                ShowNews_Data_Values(category_id, 0);
            } catch (Exception e) {
                showLog("deep-link rescue failed: " + e.getMessage());
                consumeDeepLinkStickyIds();
            }
        });
        return true;
    }

    private void flipToDeepLinkPage(int page) {
        if (mFlipView == null || list == null || list.isEmpty()) return;
        final int safe = Math.max(0, Math.min(page, list.size() - 1));
        positionx = safe;
        try {
            if (adapter != null) {
                adapter.setCurrentFlipPage(safe);
            }
        } catch (Exception ignored) {
        }
        // Double-post: setData often resets FlipView to 0 in the next frames.
        mFlipView.post(new Runnable() {
            @Override
            public void run() {
                flipToRestoredPage(safe);
                if (mFlipView != null) {
                    mFlipView.post(new Runnable() {
                        @Override
                        public void run() {
                            flipToRestoredPage(safe);
                        }
                    });
                }
            }
        });
    }

    /** Drop one-shot share + push deep-link sticky so Reload / next pages do not re-pin. */
    private void consumeDeepLinkStickyIds() {
        SHARE_ID = 0;
        SHARE_REDIRECT_ID = "0";
        deepLinkShareRescueAttempted = false;
        clearStickyPushPostId();
        try {
            Intent intent = getIntent();
            if (intent != null) {
                intent.removeExtra("SHARE_REDIRECT_ID");
            }
        } catch (Exception ignored) {
        }
        try {
            SharedPrefsData.putString(
                    getApplicationContext(),
                    Constants.PENDING_SHARE_POST,
                    "",
                    Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
    }

    /**
     * exclude_post_ids densify must not suppress the post we are opening via share/push.
     */
    @NonNull
    private String buildExcludePostIdsCsvOmittingDeepLinks() {
        String csv = ReadArticleStore.excludePostIdsCsv(
                getApplicationContext(),
                ReadArticleStore.EXCLUDE_IDS_SEND_CAP);
        if (TextUtils.isEmpty(csv)) return "";
        long share = SHARE_ID > 0 ? SHARE_ID : 0L;
        long push = TITLE_VALUE > 0 ? (long) TITLE_VALUE : 0L;
        // TITLE may still be raw on first request before Articles_All parses TITLE_VALUE.
        if (push <= 0) {
            try {
                if (pushUpdatedID != null) {
                    String raw = pushUpdatedID.trim();
                    if (!raw.isEmpty()
                            && !raw.equalsIgnoreCase("null")
                            && !raw.equals("0")) {
                        push = Long.parseLong(raw);
                    }
                }
            } catch (Exception ignored) {
            }
        }
        if (share <= 0 && push <= 0) return csv;
        StringBuilder out = new StringBuilder();
        String[] parts = csv.split(",");
        for (String p : parts) {
            if (p == null) continue;
            String t = p.trim();
            if (t.isEmpty()) continue;
            try {
                long id = Long.parseLong(t);
                if (id == share || id == push) continue;
            } catch (NumberFormatException ignored) {
                continue;
            }
            if (out.length() > 0) out.append(',');
            out.append(t);
        }
        return out.toString();
    }

    /**
     * Cache first synthetic ad shell from a feed page for later client rebalance.
     * Used by online News and Video News (same native ad template).
     * Special never rebalances/strips on read cadence — skip here.
     */
    private void rememberOnlineNewsAdTemplate(
            @androidx.annotation.Nullable java.util.List<ModelClass.Datum> items) {
        rememberFeedAdTemplate(items);
    }

    private void rememberFeedAdTemplate(
            @androidx.annotation.Nullable java.util.List<ModelClass.Datum> items) {
        if (items == null || items.isEmpty()) return;
        if (isSpecialFeedMode()) return;
        try {
            ModelClass.Datum t = ReadArticleStore.firstAdTemplate(items);
            if (t != null) {
                onlineNewsAdTemplate = t;
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Re-insert ads every {@link ReadArticleStore#AD_EVERY_N_REAL} real cards and
     * throttle share_app after unread strip. Online News + Video News.
     * No-op for Special / offline / active Read flip.
     * Snapshot absorption may call while Read is selected (list is not the Read flip).
     */
    private void rebalanceOnlineNewsAdsIfNeeded(
            @androidx.annotation.Nullable java.util.List<ModelClass.Datum> items) {
        if (items == null || items.isEmpty()) return;
        if (!isDeviceOnline() || offlineChromeMode) return;
        if (isSpecialFeedMode()) return;
        // Never rebalance the active Read flip list here — paintOnlineReadNewsFlip does that.
        if (bookmarksModeSelected && items == list) return;
        try {
            // When skip-ahead / short pages stripped all ad rows, seed from last ads_data shell.
            if (onlineNewsAdTemplate == null) {
                // nothing cached yet — callers with EncreptionRequest use remember + ads_data path
            }
            boolean changed =
                    ReadArticleStore.rebalanceFeedSlots(items, onlineNewsAdTemplate);
            if (changed) {
                showLog((isVideoFeedMode() ? "video feed" : "online News")
                        + ": rebalanced ad/share slots listSize=" + items.size()
                        + " reals=" + ReadArticleStore.countRealArticles(items));
                // Positions shifted — request native fills for each shell.
                preloadNativeAdsForFeedSlots(items);
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * After client rebalance, load Google natives for each synthetic ad row that
     * still lacks a creative. Uses each shell's ad_req_position / ad_code.
     */
    private void preloadNativeAdsForFeedSlots(
            @androidx.annotation.Nullable java.util.List<ModelClass.Datum> items) {
        if (items == null || items.isEmpty()) return;
        try {
            for (int i = 0; i < items.size(); i++) {
                ModelClass.Datum d = items.get(i);
                if (d == null || !ReadArticleStore.isAdFeedSlot(d)) continue;
                String code = d.getAd_code();
                if (code == null || code.isEmpty()) {
                    code = d.getNext_req_ad_code();
                }
                Integer pos = d.getAd_req_position();
                if (pos == null) {
                    pos = i;
                    try {
                        d.setAd_req_position(pos);
                    } catch (Exception ignored) {
                    }
                }
                ensureFeedNativeAdLoaded(code, pos, d.getAd_type());
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * If the active online flip (News / Video / Special) is short or the user is near
     * the last card, fetch the next page so the feed is not stranded on share/ad end.
     */
    private void maybePrefetchOnlineNewsIfNearEnd() {
        if (!canPageActiveOnlineFlipFeed() || bookmarksModeSelected) return;
        if (list == null || list.isEmpty()) return;
        if (isApiRequested || dummyResponse != null) return;
        if (onlineNewsAllReadSkipStreak >= ONLINE_NEWS_ALL_READ_SKIP_CAP) return;
        int n = list.size();
        int reals = ReadArticleStore.countRealArticles(list);
        int pos = positionx;
        try {
            if (mFlipView != null) {
                pos = mFlipView.getCurrentPage();
            }
        } catch (Exception ignored) {
        }
        boolean nearEnd = pos >= Math.max(0, n - 2);
        boolean shortList = reals < ONLINE_NEWS_MIN_UNREAD_CARDS
                || n <= ONLINE_NEWS_MIN_UNREAD_CARDS + 1;
        // Video: always prefetch near end; density rules still apply for main News.
        boolean videoNearEnd = isVideoFeedMode() && nearEnd;
        if (!nearEnd && !shortList && !videoNearEnd) return;
        showLog((isVideoFeedMode() ? "video feed" : isSpecialFeedMode() ? "special"
                : "online News")
                + " prefetch nearEnd=" + nearEnd
                + " shortList=" + shortList
                + " pos=" + pos + " n=" + n + " reals=" + reals);
        isApiRequested = true;
        new Handler(Looper.getMainLooper()).post(() -> {
            if (!canPageActiveOnlineFlipFeed() || bookmarksModeSelected) {
                isApiRequested = false;
                return;
            }
            if (dummyResponse != null) {
                isApiRequested = false;
                return;
            }
            ShowNews_Data_Values(category_id, shortList && reals == 0 ? 1 : 3);
        });
    }

    /**
     * Filter in-memory flip list using the <b>committed</b> read set only (reopen / restore /
     * footer reload). Session-pending views are left on News until the next process start.
     * If nothing remains, auto-paginates for more unread instead of sticky empty.
     */
    private void filterOnlineNewsListToUnread(boolean forceNotify) {
        if (!isOnlineMainNewsChromeActive() || list == null) return;
        if (bookmarksModeSelected) return;
        int before = list.size();
        try {
            rememberOnlineNewsAdTemplate(list);
            rememberFeedStickiesFrom(list);
            ReadArticleStore.stripReadFromFeed(getApplicationContext(), list);
            rebalanceOnlineNewsAdsIfNeeded(list);
        } catch (Exception ignored) {
            return;
        }
        if (list.size() != before || forceNotify) {
            try {
                // Prefer full setData so FlipView never keeps a stale empty adapter copy.
                ensureHomeFlipAdapterBoundAndData(list);
            } catch (Exception ignored) {
                try {
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                } catch (Exception ignored2) {
                }
            }
            try {
                if (mFlipView != null && list.size() > 0) {
                    int cur = 0;
                    try {
                        cur = mFlipView.getCurrentPage();
                    } catch (Exception ignored) {
                    }
                    if (cur < 0 || cur >= list.size()) {
                        mFlipView.flipTo(0);
                    }
                }
            } catch (Exception ignored) {
            }
        }
        // Empty / below min after filter: fetch further pages for true unread content.
        // Stickies alone do not fill min — but still paint while we page.
        int reals = ReadArticleStore.countRealArticles(list);
        int paintables = ReadArticleStore.countPaintableArticles(list);
        if (reals < ONLINE_NEWS_MIN_UNREAD_CARDS
                && canPageOnlineMainNews()
                && !isApiRequested
                && onlineNewsAllReadSkipStreak < ONLINE_NEWS_ALL_READ_SKIP_CAP) {
            try {
                if (txtNoData != null && paintables == 0) {
                    txtNoData.setVisibility(View.GONE);
                }
                if (paintables == 0) {
                    showHomeFeedLoadingReveal();
                }
            } catch (Exception ignored) {
            }
            onlineNewsAllReadSkipStreak++;
            isApiRequested = true;
            new Handler(Looper.getMainLooper()).post(() -> {
                if (!isFinishing() && canPageOnlineMainNews() && !bookmarksModeSelected) {
                    ShowNews_Data_Values(category_id, 1);
                } else {
                    isApiRequested = false;
                }
            });
        } else if (reals >= ONLINE_NEWS_MIN_UNREAD_CARDS) {
            onlineNewsAllReadSkipStreak = 0;
            maybePrefetchOnlineNewsIfNearEnd();
        } else if (paintables > 0) {
            // Exhausted skip cap or cannot page, but stickies/unread remain — show them.
            try {
                hideHomeFeedLoadingReveal(true);
                ensureHomeFlipAdapterBoundAndData(list);
            } catch (Exception ignored) {
            }
        }
        ensureOnlineBottomNavVisible();
        try {
            refreshHomeModeToggleUi();
        } catch (Exception ignored) {
        }
    }

    private int findListIndexForPostId(long postId) {
        if (postId <= 0 || list == null || list.isEmpty()) return -1;
        for (int i = 0; i < list.size(); i++) {
            ModelClass.Datum d = list.get(i);
            if (d != null && d.getPostId() != null && d.getPostId() == postId) {
                return i;
            }
        }
        return -1;
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

    /**
     * Permanent Special glyph: Material menu icon is ALWAYS {@link View#VISIBLE}
     * with {@code ic_footer_special} when the Special slot is allowed (online + admin).
     * Offline / admin-off: never force-show Special (that reintroduced the offline
     * News+Special leak after rebalance).
     * Overlay is optional blink polish only.
     */
    private void setupSpecialTabBulb() {
        if (bottomNavigationView == null) {
            return;
        }
        if (!isFooterSpecialTabAllowed()) {
            try {
                stopSpecialTabBlinkAnimation();
                if (specialTabBulbOverlay != null) {
                    specialTabBulbOverlay.setVisibility(View.GONE);
                }
                MenuItem special = bottomNavigationView.getMenu().findItem(R.id.navigation_catagiries);
                if (special != null) {
                    special.setVisible(false);
                    special.setEnabled(false);
                }
                forceFooterItemViewsMatchMenuVisibility();
            } catch (Exception ignored) {
            }
            return;
        }
        ensureSpecialMenuIconAlwaysVisible();
        ensureSpecialTabBulbOverlay();
        updateSpecialTabBlinkIndicator();
        // Post one more pass after layout weights settle (rebalance, first measure).
        bottomNavigationView.post(() -> {
            if (!isFooterSpecialTabAllowed()) {
                return;
            }
            ensureSpecialMenuIconAlwaysVisible();
            ensureSpecialTabBulbOverlay();
            if (!specialTabViewedThisSession) {
                startSpecialTabBlinkAnimation();
            }
        });
    }

    /**
     * Re-bind footer menu ImageViews to {@link ColorStateList} so checked items
     * (Special / Profile) show brand orange on icon+label; unchecked stays gray.
     * Must run after programmatic {@code setImageResource} which can drop tint.
     */
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
        // Keep BNV + any admin colors in sync.
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
                androidx.core.widget.ImageViewCompat.setImageTintList(iv, tint);
            }
        }
        // Blink overlay is not Material-managed — tint it orange only while pending.
        if (specialTabBulbOverlay != null && specialTabBulbOverlay.getVisibility() == View.VISIBLE
                && !specialTabViewedThisSession) {
            specialTabBulbOverlay.setColorFilter(
                    ContextCompat.getColor(this, R.color.brand_orange), PorterDuff.Mode.SRC_ATOP);
        }
    }

    /**
     * When Special is allowed: keep a non-null icon and its ImageView VISIBLE.
     * When offline / admin-off: force Special hidden — never setVisible(true).
     */
    private void ensureSpecialMenuIconAlwaysVisible() {
        if (bottomNavigationView == null) {
            return;
        }
        final boolean specialAllowed = isFooterSpecialTabAllowed();
        try {
            MenuItem special = bottomNavigationView.getMenu().findItem(R.id.navigation_catagiries);
            if (!specialAllowed) {
                if (special != null) {
                    special.setVisible(false);
                    special.setEnabled(false);
                } else if (bottomNavigationView.getMenu().size() > SPECIAL_TAB_INDEX) {
                    MenuItem byIndex = bottomNavigationView.getMenu().getItem(SPECIAL_TAB_INDEX);
                    if (byIndex != null) {
                        byIndex.setVisible(false);
                        byIndex.setEnabled(false);
                    }
                }
                try {
                    stopSpecialTabBlinkAnimation();
                    if (specialTabBulbOverlay != null) {
                        specialTabBulbOverlay.setVisibility(View.GONE);
                    }
                } catch (Exception ignored) {
                }
                forceFooterItemViewsMatchMenuVisibility();
                // Profile glyph still locked when Profile is shown (online).
                if (isDeviceOnline() && ProfileHubConfigHelper.isFooterShowProfile(this)) {
                    MenuItem profile = bottomNavigationView.getMenu().findItem(R.id.navigation_profile);
                    if (profile != null) {
                        profile.setIcon(R.drawable.ic_nav_profile);
                    }
                    try {
                        BottomNavigationMenuView menuView =
                                (BottomNavigationMenuView) bottomNavigationView.getChildAt(0);
                        if (menuView != null) {
                            for (int i = 0; i < menuView.getChildCount(); i++) {
                                View item = menuView.getChildAt(i);
                                if (!(item instanceof BottomNavigationItemView)) {
                                    continue;
                                }
                                BottomNavigationItemView piv = (BottomNavigationItemView) item;
                                if (piv.getItemData() == null
                                        || piv.getItemData().getItemId() != R.id.navigation_profile) {
                                    continue;
                                }
                                View profileIcon = piv.findViewById(
                                        com.google.android.material.R.id.navigation_bar_item_icon_view);
                                if (profileIcon instanceof ImageView) {
                                    ImageView pivIv = (ImageView) profileIcon;
                                    pivIv.setImageResource(R.drawable.ic_nav_profile);
                                    pivIv.setScaleType(ImageView.ScaleType.FIT_CENTER);
                                    pivIv.setVisibility(View.VISIBLE);
                                    pivIv.setAlpha(1f);
                                }
                                break;
                            }
                        }
                    } catch (Exception ignored) {
                    }
                    applyFooterTabIconTints();
                }
                return;
            }
            if (special != null) {
                special.setIcon(R.drawable.ic_footer_special);
                special.setVisible(true);
                special.setEnabled(true);
            } else if (bottomNavigationView.getMenu().size() > SPECIAL_TAB_INDEX) {
                MenuItem byIndex = bottomNavigationView.getMenu().getItem(SPECIAL_TAB_INDEX);
                if (byIndex != null) {
                    byIndex.setIcon(R.drawable.ic_footer_special);
                    byIndex.setVisible(true);
                    byIndex.setEnabled(true);
                }
            }
            // Also lock footer Profile to the person glyph every rebalance.
            MenuItem profile = bottomNavigationView.getMenu().findItem(R.id.navigation_profile);
            if (profile != null) {
                profile.setIcon(R.drawable.ic_nav_profile);
            }
        } catch (Exception ignored) {
        }
        BottomNavigationItemView itemView = findSpecialTabItemView();
        if (itemView != null) {
            View defaultIcon = itemView.findViewById(
                    com.google.android.material.R.id.navigation_bar_item_icon_view);
            if (defaultIcon != null) {
                // CRITICAL: never leave this INVISIBLE — was the label-only bug after rebalance.
                defaultIcon.setVisibility(View.VISIBLE);
                defaultIcon.setAlpha(1f);
                if (defaultIcon instanceof ImageView) {
                    ImageView iv = (ImageView) defaultIcon;
                    iv.setImageResource(R.drawable.ic_footer_special);
                    iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
                }
            }
        }
        // Profile icon view (same selector tint as reload).
        try {
            BottomNavigationMenuView menuView =
                    (BottomNavigationMenuView) bottomNavigationView.getChildAt(0);
            if (menuView != null) {
                for (int i = 0; i < menuView.getChildCount(); i++) {
                    View item = menuView.getChildAt(i);
                    if (!(item instanceof BottomNavigationItemView)) {
                        continue;
                    }
                    BottomNavigationItemView piv = (BottomNavigationItemView) item;
                    if (piv.getItemData() == null
                            || piv.getItemData().getItemId() != R.id.navigation_profile) {
                        continue;
                    }
                    View profileIcon = piv.findViewById(
                            com.google.android.material.R.id.navigation_bar_item_icon_view);
                    if (profileIcon instanceof ImageView) {
                        ImageView pivIv = (ImageView) profileIcon;
                        pivIv.setImageResource(R.drawable.ic_nav_profile);
                        pivIv.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        pivIv.setVisibility(View.VISIBLE);
                        pivIv.setAlpha(1f);
                    }
                    break;
                }
            }
        } catch (Exception ignored) {
        }
        applyFooterTabIconTints();
    }

    private void ensureSpecialTabBulbOverlay() {
        if (bottomNavigationView == null) {
            return;
        }
        // Offline / admin-off: never attach bulb or force-show Special (in-flight
        // postDelayed retries after network drop must no-op).
        if (!isFooterSpecialTabAllowed()) {
            try {
                stopSpecialTabBlinkAnimation();
                if (specialTabBulbOverlay != null) {
                    specialTabBulbOverlay.setVisibility(View.GONE);
                }
                ensureSpecialMenuIconAlwaysVisible();
            } catch (Exception ignored) {
            }
            return;
        }
        // Menu icon first — independent of overlay success.
        ensureSpecialMenuIconAlwaysVisible();

        if (specialTabBulbOverlay != null) {
            if (specialTabBulbOverlay.getParent() != null
                    && specialTabBulbOverlay.isAttachedToWindow()) {
                // After first session open: hide overlay so only one icon shows;
                // while blink pending keep overlay for orange pulse on top of solid icon.
                if (specialTabViewedThisSession) {
                    specialTabBulbOverlay.setVisibility(View.GONE);
                } else {
                    specialTabBulbOverlay.setVisibility(View.VISIBLE);
                    specialTabBulbOverlay.setImageResource(R.drawable.ic_footer_special);
                }
                return;
            }
            specialTabBulbOverlay = null;
        }

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
            // Overlay is blink-only polish; menu Material icon stays VISIBLE underneath.
            specialTabBulbOverlay.setVisibility(
                    specialTabViewedThisSession ? View.GONE : View.VISIBLE);
            specialTabBulbOverlay.bringToFront();
        }
        // Always re-assert Material icon after any overlay attach — never INVISIBLE.
        ensureSpecialMenuIconAlwaysVisible();
    }

    private void updateSpecialTabBlinkIndicator() {
        if (bottomNavigationView == null) {
            return;
        }
        Logone.d("HomePage", "SpecialTabBlink pending:" + !specialTabViewedThisSession);
        ensureSpecialMenuIconAlwaysVisible();
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
        ensureSpecialMenuIconAlwaysVisible();
        ensureSpecialTabBulbOverlay();
        if (specialTabBulbOverlay != null) {
            specialTabBulbOverlay.setVisibility(View.VISIBLE);
            specialTabBulbOverlay.setImageResource(R.drawable.ic_footer_special);
        }
        // Blink target: overlay if present, else the Material icon itself.
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
        stopSpecialTabBlinkAnimation();
        specialTabBulbSetupRetryCount = 0;
        ensureSpecialMenuIconAlwaysVisible();
        if (specialTabBulbOverlay != null) {
            // Hide overlay post-viewed so only the permanent menu icon shows (no double).
            specialTabBulbOverlay.setVisibility(View.GONE);
            specialTabBulbOverlay.clearColorFilter();
            specialTabBulbOverlay.setAlpha(1f);
        }
        applyFooterTabIconTints();
    }

    private void restoreSpecialTabIcon() {
        if (bottomNavigationView == null) {
            return;
        }
        specialTabBulbSetupRetryCount = 0;
        stopSpecialTabBlinkAnimation();
        ensureSpecialMenuIconAlwaysVisible();
        if (specialTabBulbOverlay != null) {
            specialTabBulbOverlay.setVisibility(View.GONE);
            specialTabBulbOverlay.clearColorFilter();
            specialTabBulbOverlay.setAlpha(1f);
        }
        applyFooterTabIconTints();
    }

    /**
     * Locates the Material bottom-nav item view for the Special tab so the icon
     * overlay can be centered on it (24dp, matching other footer glyphs).
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
        if (specialTabViewedThisSession || !isFooterSpecialTabAllowed()) {
            return;
        }
        ensureSpecialMenuIconAlwaysVisible();
        View blinkTarget = null;
        if (specialTabBulbOverlay != null
                && specialTabBulbOverlay.getParent() != null
                && specialTabBulbOverlay.isAttachedToWindow()) {
            specialTabBulbOverlay.setVisibility(View.VISIBLE);
            blinkTarget = specialTabBulbOverlay;
        } else {
            BottomNavigationItemView itemView = findSpecialTabItemView();
            if (itemView != null) {
                blinkTarget = itemView.findViewById(
                        com.google.android.material.R.id.navigation_bar_item_icon_view);
            }
        }
        if (blinkTarget == null) {
            return;
        }
        stopSpecialTabBlinkAnimation();
        blinkTarget.setVisibility(View.VISIBLE);
        if (blinkTarget instanceof ImageView) {
            ((ImageView) blinkTarget).setColorFilter(
                    ContextCompat.getColor(this, R.color.brand_orange), PorterDuff.Mode.SRC_ATOP);
        }
        ObjectAnimator animator = ObjectAnimator.ofFloat(blinkTarget, View.ALPHA, 1f, 0.4f);
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
        BottomNavigationItemView itemView = findSpecialTabItemView();
        if (itemView != null) {
            View defaultIcon = itemView.findViewById(
                    com.google.android.material.R.id.navigation_bar_item_icon_view);
            if (defaultIcon != null) {
                defaultIcon.clearAnimation();
                defaultIcon.setAlpha(1f);
                defaultIcon.setVisibility(View.VISIBLE);
                if (defaultIcon instanceof ImageView) {
                    ((ImageView) defaultIcon).clearColorFilter();
                }
            }
        }
        // Re-apply ColorStateList so selected Special/Profile stay brand orange.
        applyFooterTabIconTints();
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
        try {
            // Prefer article thumb; never white launcher as content logo.
            if (imgUrl != null) {
                collapsedView.setImageViewBitmap(R.id.imageViewIcon, imgUrl);
                expandedView.setImageViewBitmap(R.id.imageViewIconExpanded, imgUrl);
                collapsedView.setViewVisibility(R.id.imageViewIcon, View.VISIBLE);
                expandedView.setViewVisibility(R.id.imageViewIconExpanded, View.VISIBLE);
            } else {
                collapsedView.setImageViewResource(R.id.imageViewIcon, R.drawable.app_share_logo);
                expandedView.setImageViewResource(R.id.imageViewIconExpanded, R.drawable.app_share_logo);
                collapsedView.setViewVisibility(R.id.imageViewIcon, View.VISIBLE);
                expandedView.setViewVisibility(R.id.imageViewIconExpanded, View.VISIBLE);
            }
            collapsedView.setViewVisibility(R.id.notificationThumb, View.GONE);
            expandedView.setViewVisibility(R.id.notificationView, View.GONE);
        } catch (Exception ignored) {
        }
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
                    .setCustomBigContentView(expandedView)
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
