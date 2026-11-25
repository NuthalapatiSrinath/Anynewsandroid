package com.anyNews.anynews.Utils;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static androidx.core.content.ContextCompat.getSystemService;
import static com.anyNews.anynews.R.drawable.ic__volume_up;
import static com.anyNews.anynews.R.drawable.ic_dislike_half_selected1;
import static com.anyNews.anynews.R.drawable.ic_like_half1;
import static com.anyNews.anynews.R.drawable.ic_like_half_selected1;
import static com.anyNews.anynews.R.drawable.ic_location_icon;
import static com.anyNews.anynews.R.drawable.ic_new_dislike_icon;
import static com.anyNews.anynews.R.drawable.ic_new_like_icon;
import static com.anyNews.anynews.R.drawable.ic_news_icon;
import static com.anyNews.anynews.R.drawable.ic_selected_dislike_icon;
import static com.anyNews.anynews.R.drawable.ic_volume_off;
import static com.anyNews.anynews.Utils.AppHelper.showLog;
import static com.anyNews.anynews.Utils.MyUtils.decrypt;
import static com.anyNews.anynews.Utils.MyUtils.encrypt;
import static com.facebook.FacebookSdk.getApplicationContext;
import static androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FILL;
import static androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.Lifecycle;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import com.anyNews.anynews.Activities.HomePage;
import com.anyNews.anynews.Activities.QuizPageActivity;
import com.anyNews.anynews.Activities.SearchActivity;
import com.anyNews.anynews.Activities.WriterProfileActivity;
import com.anyNews.anynews.Activities.newProfileDetails;
import com.anyNews.anynews.Adapters.CardPagerAdapter;
import com.anyNews.anynews.Adapters.SamplePagerAdapter;
import com.anyNews.anynews.Adapters.VastTagAdVideoAdPlayerAdapter;
import com.anyNews.anynews.BuildConfig;
import com.anyNews.anynews.Pojo.CardItem;
import com.anyNews.anynews.Pojo.WriterStripStyleConfig;
import com.anyNews.anynews.Pojo.DailogSendRespouse;
import com.anyNews.anynews.Pojo.DialogSendRequest;
import com.anyNews.anynews.Pojo.DilogeResest;
import com.anyNews.anynews.Pojo.EncreptionRequest;
import com.anyNews.anynews.Pojo.EncritonRequestData;
import com.anyNews.anynews.Pojo.GetReportIssueResponce;
import com.anyNews.anynews.Pojo.LanguageModelResponse;
import com.anyNews.anynews.Pojo.LikeDownloadRespouse;
import com.anyNews.anynews.Pojo.LikeSelectionRequest;
import com.anyNews.anynews.Pojo.RatingSubmitRequest;
import com.anyNews.anynews.Pojo.SurveyFeedbackResponse;
import com.anyNews.anynews.Pojo.SurveyRequestFeedback;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Services.ShadowTransformer;
import com.anyNews.anynews.Ui.NeomorphFrameLayout;
import com.anyNews.anynews.interfaces.CallBackListener;
import com.anyNews.anynews.interfaces.CustomPlayerUiController;
import com.anyNews.anynews.interfaces.LoaderCommonOption;
import com.anyNews.anynews.interfaces.ScreenShott;
import com.anyNews.anynews.interfaces.ShareInterfaceListener;
import com.anyNews.anynews.interfaces.SharePostToWhatsApp;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.facebook.ads.AdOptionsView;
import com.facebook.ads.NativeAdLayout;
import com.google.ads.interactivemedia.v3.api.AdDisplayContainer;
import com.google.ads.interactivemedia.v3.api.AdErrorEvent;
import com.google.ads.interactivemedia.v3.api.AdEvent;
import com.google.ads.interactivemedia.v3.api.AdsLoader;
import com.google.ads.interactivemedia.v3.api.AdsManager;
import com.google.ads.interactivemedia.v3.api.AdsManagerLoadedEvent;
import com.google.ads.interactivemedia.v3.api.AdsRenderingSettings;
import com.google.ads.interactivemedia.v3.api.AdsRequest;
import com.google.ads.interactivemedia.v3.api.ImaSdkFactory;
import com.google.ads.interactivemedia.v3.api.ImaSdkSettings;
import com.google.ads.interactivemedia.v3.api.player.VideoProgressUpdate;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackParameters;
import androidx.media3.common.Player;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import androidx.media3.ui.PlayerView;
import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerUtils;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;
import com.pierfrancescosoffritti.youtubeplayer.player.YouTubePlayerFullScreenListener;
import com.squareup.picasso.LruCache;
import com.squareup.picasso.Picasso;
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.wasabeef.picasso.transformations.BlurTransformation;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FlipperAdapter extends BaseAdapter implements YouTubePlayer.OnInitializedListener, LoaderCommonOption, ShareInterfaceListener  {

    private  int layoutHeight,screenHeight,screenWidth;
    private  int statusbar_height;
    private Context context;
    private boolean isVideoDownloadCancel=false;
    private boolean fromWhatsapp;
    private final LayoutInflater inflater;
    private InterstitialAd interstitialAd;
    //   ViewHolder holder;
    HalfImageActivityHolder   halfImageActivity;

    RatingScreenViewHolder   ratingScreenViewHolder;
    AdsScreenViewHolder   adsScreenViewHolder;
    /** User-selected (or default) companion row per ad feed index — for flip-up overlay. */
    private final android.util.SparseArray<ModelClass.Datum> adsScreenOverlayPick =
            new android.util.SparseArray<>();
    FullImageActivityHolder fullImageActivity;
    ImageSliderHolder imageSliderHolder;
    ImageGallaryHolder imageGallaryHolder;
    HalfVideoViewHolder half_videoViewHolder;
    FullVideoViewHolder full_videoViewHolder;
    FullVideoNewYoutubeViewHolder fullVideoNewYoutubeViewHolder;
    FullYouTubePlayerHolder full_youTubePlayerHolder;
    HalfYouTubePlayerHolder half_youTubePlayerHolder;
    HalfVideoExoPlayerHolder halfVideoExoPlayerHolder;
    ImageMagazineHolder image_magazineHolder;
    WebViewHolder webViewHolder;
    WebViewHolderAD webViewHolderAD;
    SurveyViewHolder surveyViewHolder;
    PollingViewHolder pollingViewHolder;
    VideosliderHolder videoViewHolder;
    VideogalaryHolder video_galaryHolder;
    YouTubePlayerFullScreenListener youTubePlayerFullScreenListener;
   public static com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer youTubePlayerViewHalf;
   public static boolean isYoutubeVideoIsInitialized =false;
   public static boolean isFullYoutubeVideoIsInitialized =false;
   public static  ExoPlayer exoPlayer;
    static String videoURL;
    public static boolean fullscreen=false;
    public View customConvertView;
    public int customPosition;
    Callbacks callbacks;
   public NativeAd nativeAd=null;

    com.facebook.ads.NativeAd fbAd=null;
    /** Active IMA VAST session — destroyed on flip-away so Samsung does not keep ad audio. */
    @Nullable private AdsManager activeVastAdsManager;
    @Nullable private AdsLoader activeVastAdsLoader;
    @Nullable private VideoView activeVastVideoView;
    // sound
    AudioManager audioManager;
    private ArrayList<ModelClass.Datum> list;
    LinearLayout linear_no_recyclerView;

    private ViewPager mViewPager;
    private CardPagerAdapter mCardAdapter,halfImageCardAdapter;
    private ShadowTransformer mCardShadowTransformer;
    private TextView[] dots;
    boolean checking = false;


    ArrayList<String> halfImageGallery;
    CommonMethods commonMethods;

    Allsharedpreference prefShare;
    LanguageModelResponse languageModelResponse;
    CountDownTimer countDownTimer;

    int LanguageId = SharedPrefsData.getInt(getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME);
    DisplayMetrics displayMetrics = new DisplayMetrics();
    //DisplayImageOptions options;
    ImageLoader imageLoader;
    private static final int RECOVERY_DIALOG_REQUEST = 1;
    String youtubeFullVideoURL = "";
    private String responseSurvey;
    private long postIDSurvey;

    private List<ModelClass.SurveyOptions> listSurveyResponseData;
    private String surveyPercent;
    SurveyFeedbackResponse surveyFeedbackResponse;

    private String imageBackgroundURL = "";
    private String percentageYes = "", percentageNo = "";
    private String descriptionText = "", descriptionHash = "", descriptionDirectionLink = "";
    private AdapterCallback adapterCallback;
    private String refer_earn;
    private YouTubePlayer youTubePlayer;
    boolean isMutedFull = false;
    boolean isMutedHalf = false;
    String my_feed_name;
    int categery_id;
    float percent;
    ProgressiveMediaSource mediaSource ;
    boolean isReplay=false ;
    Activity activity;
    Lifecycle lifecycle ;
    Typeface teluguFont,tamilFont;
    //PlayerView simpleExoPlayerView;
    public static int heightImage;
    private float tamil_text_dp=0;
    private float hindi_text_dp=0,other_font=0;
    private float malayalam_text_dp =0;
    CardView adapterCardView;


   public CustomPlayerUiController customPlayerUiController;
   public CustomPlayerUiController fullVideocustomPlayerUiController;
    public FlipperAdapter(Activity activity, Context mainActivity,  String my_feed_name, int categery_id, Lifecycle lifecycle) {
        audioManager = (AudioManager) mainActivity.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        percent = 0.5f;
        this.activity = activity;
        this.context = mainActivity;
        this.lifecycle = lifecycle;
        inflater = LayoutInflater.from(mainActivity);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        halfImageGallery = new ArrayList<>();
        this.my_feed_name = my_feed_name;
        this.categery_id = categery_id;
        commonMethods = new CommonMethods(activity);
        list=new ArrayList<>();
        prefShare = new Allsharedpreference(context);
        Gson gson = new Gson();
        String Translation = prefShare.getShared_String(Allsharedpreference.Translator);
        if (Translation != null && !Translation.isEmpty()) {
            try {
                languageModelResponse = gson.fromJson(Translation, LanguageModelResponse.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // initialize image loader before using
        imageLoader = ImageLoader.getInstance();
        /*ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .memoryCacheExtraOptions(480, 800) // max width, max height
                .threadPoolSize(3)
                .threadPriority(Thread.NORM_PRIORITY - 1)
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(new UsingFreqLimitedMemoryCache(2 * 1024 * 1024)) // You can pass your own memory cache implementation
                .discCacheFileNameGenerator(new HashCodeFileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.FIFO)
                .defaultDisplayImageOptions(DisplayImageOptions.createSimple())
                .build();*/
        imageLoader.init(ImageLoaderConfiguration.createDefault(getApplicationContext()));
        listSurveyResponseData = new ArrayList<>();

        try {
            adapterCallback = ((AdapterCallback) context);
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
//        options = new DisplayImageOptions.Builder()
//                .showImageOnLoading(android.R.color.transparent)
//                .showImageForEmptyUri(android.R.color.transparent)
//                .showImageOnFail(android.R.color.transparent)
//                .cacheInMemory(true)
//                .cacheOnDisk(true)
//                .considerExifParams(true)
//                .bitmapConfig(Bitmap.Config.RGB_565)
//                // build will build the view for displaying image..
//                .build();
        statusbar_height=0;
        showLog("Adapter statusbar_height ::"+statusbar_height);
        screenHeight=getRealHeightOfFlipView(activity);
        screenWidth=getRealWidth(activity);
        showLog("Adapter screenHeight ::"+screenHeight);
        float f=2.35f;
        if (getSoftButtonsBarSizePort(activity) == 0) {

            layoutHeight = (int) ((getRealHeight(activity) - statusbar_height) / f);
            //titleBarHeight = 0;
        } else if (statusbar_height > 120) {
            layoutHeight = (int) ((getRealHeight(activity) - getSoftButtonsBarSizePort(activity) - statusbar_height) / f);
            //titleBarHeight = 0;
        } else if (((statusbar_height * 2) + 1) >= getSoftButtonsBarSizePort(activity)) {
            layoutHeight = (int) ((getRealHeight(activity) - getSoftButtonsBarSizePort(activity) - statusbar_height) / f);
            //titleBarHeight = 0;
        } else {
            layoutHeight = (int) ((getRealHeight(activity) - getSoftButtonsBarSizePort(activity)) / f);
            //titleBarHeight = 0;
        }
        showLog("Adapter heightImage_New ::"+ layoutHeight);
       // heightImage_New= (int) dipToPixels(context,R.dimen._320sdp);
        teluguFont=AppLanguagesFonts.getTeluguFont();
        int height1 = activity.getWindowManager().getDefaultDisplay().getHeight();
        int resourceId = activity.getResources().getIdentifier(
                "status_bar_height", "dimen", "android");


        heightImage = (height1 - (int) activity.getResources().getDimension(
                resourceId)) / 2;
        float density = context.getResources().getDisplayMetrics().density;
        tamil_text_dp = pxFromDp(12f * density);
        hindi_text_dp = pxFromDp(14f * density);
        malayalam_text_dp = pxFromDp(13f * density);
        other_font = pxFromDp(13f * density);

    }
    public  float dipToPixels( float dipValue) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
         showLog("Article Data:"+ "DIPCONVERT VALUEINT VALUE>>>>" + dipValue);
         showLog("Article Data:"+ "DIPCONVERT VALUE>>>>" + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics));
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
    }
    public  float pxFromDp(final float dp) {
        showLog("Article Data:"+ "DIPCONVERT VALUEINT VALUE>>>>" + dp);
        float data=dp / context.getResources().getDisplayMetrics().density;
        showLog("Article Data:"+ "DIPCONVERT VALUEINT data>>>>" + data);
       // int dp = (int) (context.getResources().getDimension(R.dimen._15sdp) / context.getResources().getDisplayMetrics().density);
        return data ;
    }

    /** Soft-apply admin title/body fonts+sizes from cached splash prefs (no Activity recreate). */
    private void applyFeedArticleFonts(TextView titleView, TextView bodyView) {
        Context ctx = context != null ? context : getApplicationContext();
        if (ctx == null) {
            return;
        }
        int langId = LanguageId > 0
                ? LanguageId
                : SharedPrefsData.getInt(ctx, Constants.LanguageID, Constants.PREF_NAME);
        FontSettingsHelper.applyArticleTypography(
                ctx,
                langId > 0 ? langId : 1,
                titleView,
                bodyView,
                "flipper"
        );
    }

    /** Soft-apply admin strip label size from cached splash prefs. */
    private void applyFeedStripFont(TextView stripView) {
        if (stripView == null) {
            return;
        }
        Context ctx = context != null ? context : getApplicationContext();
        if (ctx == null) {
            return;
        }
        int langId = LanguageId > 0
                ? LanguageId
                : SharedPrefsData.getInt(ctx, Constants.LanguageID, Constants.PREF_NAME);
        FontSettingsHelper.applyTextSize(
                ctx,
                stripView,
                FontSettingsHelper.getStripSize(ctx, langId > 0 ? langId : 1)
        );
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
    public static int getRealHeightOfFlipView(Activity activity) {

        Resources resources = activity.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            //  return resources.getDimensionPixelSize(resourceId);
            showLog("bottomNavigationView:::HEIGHT???:"+ resources.getDimensionPixelSize(resourceId));
            showLog("bottomNavigationView:::HEIGHT???:"+ resources.getDimensionPixelSize(resourceId));

            DisplayMetrics metrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            activity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
            int realHeight = metrics.heightPixels;
            showLog("bottomNavigationView:::realHeight???:"+ realHeight);
            realHeight=realHeight-resources.getDimensionPixelSize(resourceId);
            showLog("bottomNavigationView:::realHeight???:"+ realHeight);
            return realHeight-resources.getDimensionPixelSize(resourceId);
        }
        return 0;

    }
    public static int getRealHeight(Activity activity) {
            DisplayMetrics metrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            activity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
            int realHeight = metrics.heightPixels;
            return realHeight;
    }
    public static int getRealWidth(Activity activity) {
            DisplayMetrics metrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            activity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
            int realHeight = metrics.widthPixels;
            return realHeight;
    }

    public static int getSoftButtonsBarSizePort(Activity activity) {
        // getRealMetrics is only available with API 17 and +
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            DisplayMetrics metrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int usableHeight = metrics.heightPixels;
//            Log.i("*** Elenasys :: ", "usableHeight= " + usableHeight);
            activity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
            int realHeight = metrics.heightPixels;
//            Log.i("*** Elenasys :: ", "realHeight= " + realHeight);
            if (realHeight > usableHeight)
                return realHeight - usableHeight;
            else return 0;
        }
        return 0;
    }

    public void setData(ArrayList<ModelClass.Datum> list__) {
       list=list__;
        notifyDataSetChanged();

    }

    private void bindActionCount(TextView tv, Integer count) {
        if (tv == null) return;
        if (tv.getId() == R.id.half_like_count
                || tv.getId() == R.id.half_comment_count
                || tv.getId() == R.id.half_share_count) {
            bindHalfActionCount(tv, count);
            return;
        }
        int c = count == null ? 0 : count;
        if (c > 0) {
            tv.setText(formatCount(c));
            tv.setVisibility(View.VISIBLE);
        } else {
            tv.setVisibility(View.GONE);
        }
    }

    /**
     * Play Store half-row counts: plain colored text under the icon (not a badge chip).
     * INVISIBLE when 0 so the row height does not jump.
     */
    private void bindHalfActionCount(TextView tv, Integer count) {
        if (tv == null) return;
        int c = count == null ? 0 : count;
        if (c <= 0) {
            tv.setText("");
            tv.setVisibility(View.INVISIBLE);
            return;
        }
        tv.setText(formatCount(c));
        tv.setBackground(null);
        tv.setTextColor(EngagementCountHelper.getCountColor(context));
        tv.setTypeface(tv.getTypeface(), Typeface.BOLD);
        tv.setVisibility(View.VISIBLE);
    }

    /** Play Store half like: outline / filled heart inside white_oval + admin tint. */
    private void bindHalfLikeIcon(ImageView icon, Integer status) {
        if (icon == null) return;
        boolean liked = status != null && status == 1;
        icon.setBackgroundResource(R.drawable.white_oval);
        icon.setImageResource(liked ? R.drawable.ic_action_like_filled : R.drawable.ic_action_like);
        EngagementCountHelper.applyIconTint(context, icon);
    }

    /** Apply admin engagement color to like/comment/share/TTS/search/⋮ on half posts. */
    private void tintHalfActionIcons(View convertView) {
        if (convertView == null) return;
        EngagementCountHelper.applyIconTint(context, convertView.findViewById(R.id.half_image_like));
        EngagementCountHelper.applyIconTint(context, convertView.findViewById(R.id.half_comment_icon));
        EngagementCountHelper.applyIconTint(context, convertView.findViewById(R.id.half_share_icon));
        EngagementCountHelper.applyIconTint(context, convertView.findViewById(R.id.half_search_icon));
        EngagementCountHelper.applyIconTint(context, convertView.findViewById(R.id.half_tts_icon));
        EngagementCountHelper.applyIconTint(context, convertView.findViewById(R.id.share));
        EngagementCountHelper.applyIconTint(context, convertView.findViewById(R.id.report_icon));
        EngagementCountHelper.applyIconTint(context, convertView.findViewById(R.id.video_report));
    }

    /**
     * Insert Play Store WhatsApp chip before Share on half articles (admin half switch).
     * One tap → HomePage share path; no extra per-flip network.
     */
    private void ensureHalfWhatsappAction(View convertView, int position) {
        if (convertView == null || list == null || position < 0 || position >= list.size()) {
            return;
        }
        View shareSlot = convertView.findViewById(R.id.half_share);
        if (shareSlot == null) {
            shareSlot = convertView.findViewById(R.id.half_share__);
        }
        if (shareSlot == null || !(shareSlot.getParent() instanceof ViewGroup)) {
            return;
        }
        ViewGroup parent = (ViewGroup) shareSlot.getParent();
        View slot = parent.findViewById(R.id.half_whatsapp);
        if (slot == null) {
            slot = LayoutInflater.from(context).inflate(
                    R.layout.item_half_whatsapp_action, parent, false);
            parent.addView(slot, parent.indexOfChild(shareSlot));
        }
        slot.setVisibility(ActionRowWhatsappConfig.isHalfEnabled(context)
                ? View.VISIBLE : View.GONE);
        ImageView icon = slot.findViewById(R.id.half_whatsapp_icon);
        if (icon != null) {
            icon.setImageResource(R.drawable.ic_whatsapp_original1);
            icon.clearColorFilter();
            androidx.core.widget.ImageViewCompat.setImageTintList(icon, null);
        }
        TextView countView = slot.findViewById(R.id.half_whatsapp_count);
        if (countView != null) {
            // Same Total Shares as half_share_count (Share + WhatsApp → one counter).
            int totalShares = list.get(position).getShares() != null
                    ? list.get(position).getShares() : 0;
            countView.setText(formatCount(totalShares));
            countView.setBackground(null);
            countView.setTextColor(EngagementCountHelper.getCountColor(context));
            countView.setTypeface(countView.getTypeface(), Typeface.BOLD);
            countView.setVisibility(View.VISIBLE);
        }
        final int sharePosition = position;
        slot.setOnClickListener(v -> {
            if (context instanceof HomePage) {
                ((HomePage) context).onArticleWhatsappClicked(sharePosition);
            }
        });
    }

    /** Play Store half engagement: WhatsApp + counts + tinted action icons. Keeps Search/TTS. */
    private void bindHalfPostEngagementCounts(View convertView, int position) {
        if (convertView == null || list == null || position < 0 || position >= list.size()) {
            return;
        }
        ensureHalfWhatsappAction(convertView, position);
        ImageView commentIcon = convertView.findViewById(R.id.half_comment_icon);
        ImageView shareIcon = convertView.findViewById(R.id.half_share_icon);
        ImageView reportIcon = convertView.findViewById(R.id.report_icon);
        if (commentIcon != null) {
            commentIcon.setImageResource(R.drawable.ic_action_comment);
        }
        if (shareIcon != null) {
            shareIcon.setImageResource(R.drawable.ic_action_share);
        }
        if (reportIcon != null) {
            reportIcon.setImageResource(R.drawable.ic_action_more_vert);
        }
        bindHalfActionCount(convertView.findViewById(R.id.half_like_count), list.get(position).getLikes());
        bindHalfActionCount(convertView.findViewById(R.id.half_comment_count), list.get(position).getComments());
        bindHalfActionCount(convertView.findViewById(R.id.half_share_count), list.get(position).getShares());
        tintHalfActionIcons(convertView);
        applyHalfActionIconSizes(context, convertView);
        bindHalfSearchIcon(convertView);
        bindHalfTtsSpeaker(convertView, position);
        applyActionRowWhatsappVisibility(context, convertView);
    }

    /**
     * Admin {@code search_icon_on_engagement}: search between share and ⋮.
     * Opens InAppSearchActivity via {@link SearchRevealHelper} (thin path).
     */
    private void bindHalfSearchIcon(@Nullable View convertView) {
        if (convertView == null) return;
        View slot = convertView.findViewById(R.id.half_search);
        if (slot == null) return;
        boolean show = SearchIconConfig.isOnEngagement(context);
        slot.setVisibility(show ? View.VISIBLE : View.GONE);
        if (!show) return;
        View icon = convertView.findViewById(R.id.half_search_icon);
        if (icon instanceof ImageView) {
            ((ImageView) icon).setImageResource(R.drawable.ic_action_search);
        }
        View.OnClickListener open =
                v -> {
                    try {
                        Activity act = null;
                        if (context instanceof Activity) {
                            act = (Activity) context;
                        } else if (activity instanceof Activity) {
                            act = (Activity) activity;
                        }
                        if (act != null && !act.isFinishing()) {
                            SearchRevealHelper.openFrom(act, v);
                        }
                    } catch (Exception ignored) {
                    }
                };
        slot.setOnClickListener(open);
        if (icon != null) icon.setOnClickListener(open);
    }

    private static String formatCount(int c) {
        if (c >= 1000000) {
            double v = c / 1000000.0;
            return (v >= 10 || v == Math.floor(v))
                    ? String.format(java.util.Locale.US, "%.0fM", v)
                    : String.format(java.util.Locale.US, "%.1fM", v);
        }
        if (c >= 1000) {
            double v = c / 1000.0;
            return (v >= 10 || v == Math.floor(v))
                    ? String.format(java.util.Locale.US, "%.0fK", v)
                    : String.format(java.util.Locale.US, "%.1fK", v);
        }
        return String.valueOf(c);
    }

    private void bindFullPostLikeCount(TextView tv, Integer count, String ignoredLabel) {
        if (tv == null) return;
        int c = count == null ? 0 : count;
        tv.setText(formatCount(c));
        tv.setVisibility(View.VISIBLE);
    }

    private String getLikeLabel() {
        if (languageModelResponse != null && languageModelResponse.getData() != null
                && languageModelResponse.getData().getLike() != null) {
            return languageModelResponse.getData().getLike();
        }
        return "Like";
    }

    private String getCommentLabel() {
        if (languageModelResponse != null && languageModelResponse.getData() != null
                && languageModelResponse.getData().getComment() != null) {
            return languageModelResponse.getData().getComment();
        }
        return "Comment";
    }

    private String getShareLabel() {
        if (languageModelResponse != null && languageModelResponse.getData() != null
                && languageModelResponse.getData().getShare() != null) {
            return languageModelResponse.getData().getShare();
        }
        return "Share";
    }

    private void bindFullPostEngagementCounts(FullImageActivityHolder holder, int position) {
        if (holder == null || list == null || position < 0 || position >= list.size()) {
            return;
        }
        bindFullPostLikeCount(holder.full_img_like, list.get(position).getLikes(), getLikeLabel());
        bindFullPostLikeCount(holder.full_img_comment, list.get(position).getComments(), getCommentLabel());
        bindFullPostLikeCount(holder.full_img_share, list.get(position).getShares(), getShareLabel());
        bindFullPostLikeCount(holder.full_img_whtsapp, list.get(position).getWhatsappShares(), null);
    }

    private void bindFullVideoEngagementCounts(
            TextView likeView,
            TextView commentView,
            TextView shareView,
            @Nullable TextView whatsappView,
            int position) {
        if (list == null || position < 0 || position >= list.size()) {
            return;
        }
        bindFullPostLikeCount(likeView, list.get(position).getLikes(), getLikeLabel());
        bindFullPostLikeCount(commentView, list.get(position).getComments(), getCommentLabel());
        bindFullPostLikeCount(shareView, list.get(position).getShares(), getShareLabel());
        bindFullPostLikeCount(whatsappView, list.get(position).getWhatsappShares(), null);
    }

    @NonNull
    private static String safeBylineText(@Nullable String raw) {
        if (raw == null) {
            return "";
        }
        String t = raw.trim();
        if (t.isEmpty() || "null".equalsIgnoreCase(t) || "undefined".equalsIgnoreCase(t)) {
            return "";
        }
        return t;
    }

    @NonNull
    private static String displayCopy(@Nullable String raw) {
        return safeBylineText(raw);
    }

    private void bindWriterStripIdentity(
            ModelClass.Datum item,
            TextView nameView,
            TextView designationView,
            TextView timeView) {
        if (item == null) {
            return;
        }
        if (nameView != null) {
            String name = AutoNewsBylineHelper.displayWriterName(item.getWriterName());
            if (TextUtils.isEmpty(name)) {
                name = "Journalist";
            }
            nameView.setText(safeBylineText(name));
        }
        if (designationView != null) {
            String designation =
                    AutoNewsBylineHelper.displayDesignation(
                            context, languageModelResponse, item);
            designation = safeBylineText(designation);
            if (TextUtils.isEmpty(designation)) {
                designationView.setText("");
                designationView.setVisibility(View.GONE);
            } else {
                designationView.setText(designation);
                designationView.setVisibility(View.VISIBLE);
            }
        }
        if (timeView != null) {
            timeView.setText("");
            String relative = "";
            try {
                Long published = item.getPublishedTime();
                if (published != null && published > 0L && commonMethods != null) {
                    relative = commonMethods.getHrsFormatfromDate(published);
                }
            } catch (Exception ignored) {
                relative = "";
            }
            relative = safeBylineText(relative);
            if (TextUtils.isEmpty(relative)) {
                timeView.setText("");
                timeView.setVisibility(View.GONE);
            } else {
                timeView.setText(relative);
                timeView.setVisibility(View.VISIBLE);
            }
        }
    }

    private static int writerFollowUiState(@Nullable Integer isFollow) {
        return isFollow != null && isFollow == 1 ? 1 : 0;
    }

    /** Feed half-video cards: writer strip hidden until product re-enables it. */
    private static void hideFeedVideoWriterFooter(@Nullable View convertView) {
        if (convertView == null) {
            return;
        }
        View footer = convertView.findViewById(R.id.ll_footer_layout);
        if (footer != null) {
            footer.setVisibility(View.GONE);
        }
    }

    private void applyWriterFollowState(
            LinearLayout followRow,
            LinearLayout followingRow,
            TextView followLabel,
            @Nullable TextView followingLabel,
            int position) {
        if (list == null || position < 0 || position >= list.size()) {
            return;
        }
        int followState = writerFollowUiState(list.get(position).getIsFollow());
        if (followingRow != null) {
            followingRow.setVisibility(View.GONE);
        }
        if (followRow != null) {
            followRow.setVisibility(View.GONE);
        }
        if (followState == 0) {
            if (followRow != null) followRow.setVisibility(View.VISIBLE);
            if (followLabel != null) followLabel.setText("Follow");
        } else {
            if (followingRow != null) followingRow.setVisibility(View.VISIBLE);
            if (followingLabel != null) followingLabel.setText("Following");
        }
    }

    /** Writer profile screen from feed row JSON only (no per-card get_writer_profile). */
    private void openWriterProfileFromFeed(int position) {
        if (list == null || position < 0 || position >= list.size()) {
            return;
        }
        ModelClass.Datum item = list.get(position);
        if (item == null || item.getWriterId() == null) {
            return;
        }
        if (getApplicationContext() != null
                && getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)) {
            WriterProfileActivity.launchFromFeed(context, item);
        }
    }

    private void bindFullImageAvatarPlus(int position) {
        if (fullImageActivity.user_pic != null) {
            AvatarImageHelper.loadUil(
                    imageLoader, fullImageActivity.user_pic, list.get(position).getWriterProfile());
        }
        if (fullImageActivity.varified_btn != null) {
            int followState = list.get(position).getIsFollow() != null
                    ? list.get(position).getIsFollow() : 0;
            if (followState == 0) {
                fullImageActivity.varified_btn.setImageResource(R.drawable.ic_add_profile_icon);
                fullImageActivity.varified_btn.setVisibility(View.VISIBLE);
            } else {
                fullImageActivity.varified_btn.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Optimistically update like count in the data model and UI, then fire the API.
     * Returns the new likeStatus so the caller can update icon immediately.
     * type: 1=like, 2=dislike, 0=remove
     */
    private int applyLikeChange(int position, TextView countView) {
        ModelClass.Datum item = list.get(position);
        int oldStatus = item.getLikeStatus() != null ? item.getLikeStatus() : 0;
        int likes = item.getLikes() != null ? item.getLikes() : 0;
        boolean hadVisibleCount = likes > 0;
        int newStatus;
        int apiType;

        // Pure like toggle (no dislike): liked -> un-like, anything else -> like.
        if (oldStatus == 1) { newStatus = 0; apiType = 0; likes = Math.max(0, likes - 1); }
        else                { newStatus = 1; apiType = 1; likes = likes + 1; }

        item.setLikeStatus(newStatus);
        item.setLikes(likes);

        showLog("LIKE_DEBUG pos=" + position + " oldStatus=" + oldStatus + " newStatus=" + newStatus
                + " newLikes=" + likes + " hadVisible=" + hadVisibleCount);

        if (countView != null) {
            bindActionCount(countView, likes);
        }
        countforshareanddownload(item.getPostId(), item.getSubPostId(), apiType);
        return newStatus;
    }

    @Override
    public void shareListener() {
        showLog("shareListener:*********");

    }



    /**
     * District / feed label for the location pill.
     * Prefer the post's server {@code location_label} / post geo when it matches the
     * current language script; under Indic languages, upgrade Latin-only labels.
     * Never show another Indic script (e.g. Malayalam on a Telugu session).
     * When post geo is missing (common for national / {@code dist_id=0} cards), fall
     * back to the user's selected district — same as pre–hide-toggle main-feed
     * behavior. Callers gate main vs Local vs district posts via
     * {@link DistrictPinVisibilityConfig#shouldShowDistrict}.
     */
    private String resolveLocationLabel(@Nullable ModelClass.Datum post) {
        String selected = DistrictLabelHelper.resolveDisplayLabel(context);
        int langId = LanguageId;
        try {
            langId = SharedPrefsData.getInt(
                    context.getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
        boolean selectedUsable = isUsableLocationLabel(selected);
        boolean selectedNative = selectedUsable
                && !DistrictLabelHelper.needsNativeScriptUpgrade(selected, langId);

        if (post != null) {
            int postDistId = post.getDist_id() != null ? post.getDist_id() : 0;
            int postStateId = post.getState_id() != null ? post.getState_id() : 0;
            String fromPostGeo = DistrictLabelHelper.lookupLocalizedNameForDistrict(
                    context, langId, postDistId, postStateId);
            boolean fromPostGeoOk = isUsableLocationLabel(fromPostGeo)
                    && DistrictLabelHelper.labelCompatibleWithLanguage(fromPostGeo, langId);

            String postLabel = post.getLocation_label();
            if (isUsableLocationLabel(postLabel)) {
                String trimmed = postLabel.trim();
                boolean postNeedsUpgrade =
                        DistrictLabelHelper.needsNativeScriptUpgrade(trimmed, langId);
                boolean postCompatible =
                        DistrictLabelHelper.labelCompatibleWithLanguage(trimmed, langId);
                if (postNeedsUpgrade) {
                    // Latin server label → prefer native name for this post's district.
                    if (fromPostGeoOk
                            && !DistrictLabelHelper.needsNativeScriptUpgrade(fromPostGeo, langId)) {
                        return fromPostGeo.trim();
                    }
                    if (selectedNative
                            && (categery_id == -1
                            || DistrictLabelHelper.matchesStoredName(context, trimmed)
                            || (postDistId > 0
                            && postDistId == DistrictLabelHelper.getStoredDistId(context)))) {
                        // Never stamp district into my_feed_name — Local pill is
                        // Pradhana redirect; Main/category keep ctor titles.
                        return selected;
                    }
                    // Neutral Latin is still better than a blank pin.
                    if (postCompatible) {
                        return trimmed;
                    }
                } else if (postCompatible) {
                    return trimmed;
                }
                // Foreign-script / wrong-language label — never display it.
                if (fromPostGeoOk) {
                    return fromPostGeo.trim();
                }
            } else if (fromPostGeoOk) {
                return fromPostGeo.trim();
            }
        }
        // National / no-geo: return selected district for callers that need it;
        // never overwrite Local Pradhana / Main / category my_feed_name.
        if (selectedUsable) {
            return selected;
        }
        return "";
    }

    private static boolean isUsableLocationLabel(String s) {
        return DistrictLabelHelper.isUsable(s);
    }

    /**
     * Binds pin + district / feed text and keeps them visible after scroll / theme rebind.
     * Call after {@link WriterStripStyleHelper#apply} so style does not wipe the label.
     * <p>Location pill shows the <em>redirection destination</em>:
     * <ul>
     *   <li>Local ({@code categery_id == -1}): {@code my_feed_name} / getMyFeed()
     *       (ప్రధాన వార్తలు) → click opens Main.</li>
     *   <li>Main ({@code categery_id == 0}): selected district (e.g. Anakapalli)
     *       → click opens Local.</li>
     *   <li>Category feeds ({@code categery_id > 0}): category title
     *       (Politics, Sports, …) from HomePage.</li>
     *   <li>Promo Flash/Rewards only when no usable district/feed label remains.</li>
     * </ul>
     */
    private void bindLocationPill(
            TextView labelView,
            ImageView pinView,
            View pill,
            @Nullable ModelClass.Datum post) {
        // Prefer live LanguageID (prefs may change without recreating the adapter).
        try {
            LanguageId = SharedPrefsData.getInt(
                    context.getApplicationContext(), Constants.LanguageID, Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
        if (DistrictPinVisibilityConfig.shouldBindDualFeedChips(context, categery_id, post)) {
            bindDualFeedChips(labelView, pinView, pill, post);
            return;
        }
        hideCompanionFeedChip(pill);
        restoreSingleChipRow(pill, labelView);

        // Category: always paint category title from ctor.
        if (categery_id > 0 && isUsableLocationLabel(my_feed_name)) {
            bindDistrictLocationChrome(labelView, pinView, pill, my_feed_name.trim());
            return;
        }
        // Local: redirection pill = Main (Pradhana Varthalu).
        if (categery_id == -1) {
            String pradhana = resolvePradhanaRedirectLabel();
            if (isUsableLocationLabel(pradhana)) {
                bindDistrictLocationChrome(labelView, pinView, pill, pradhana.trim());
                return;
            }
        }

        // Main: district/location when present (respect hide-district). Never Pradhana.
        String districtLabel = resolveLocationLabel(post);
        boolean hasDistrict = isUsableLocationLabel(districtLabel)
                && DistrictPinVisibilityConfig.shouldShowDistrict(
                        context, categery_id, post);
        if (hasDistrict) {
            bindDistrictLocationChrome(labelView, pinView, pill, districtLabel);
            return;
        }

        boolean showRewardsSlot =
                DistrictPinVisibilityConfig.shouldShowRewardsInDistrictSlot(
                        context, categery_id, post);
        boolean showFlashSlot =
                DistrictPinVisibilityConfig.shouldShowFlashInDistrictSlot(
                        context, categery_id, post);
        String label = "";
        boolean promoChipMode = false;
        if (showRewardsSlot) {
            promoChipMode = true;
            label = resolveRewardsSlotLabel();
        } else if (showFlashSlot) {
            promoChipMode = true;
            label = FlashNewsChipHelper.chipLabel(context);
        }
        boolean hasLabel = isUsableLocationLabel(label);
        if (!promoChipMode || !hasLabel) {
            if (labelView != null) {
                labelView.setText("");
                labelView.setVisibility(View.GONE);
            }
            if (pinView != null) {
                pinView.setVisibility(View.GONE);
            }
            if (pill != null) {
                pill.setVisibility(View.GONE);
                pill.setAlpha(0f);
            }
            return;
        }
        if (labelView != null) {
            labelView.setVisibility(View.VISIBLE);
            labelView.setAlpha(1f);
            labelView.setText(label);
            labelView.setMaxLines(1);
            labelView.setEllipsize(TextUtils.TruncateAt.END);
        }
        if (pinView != null) {
            // Pulse host replaces district logo circle.
            pinView.setVisibility(View.GONE);
        }
        if (pill != null) {
            pill.setVisibility(View.VISIBLE);
            pill.setAlpha(1f);
            try {
                pill.clearAnimation();
            } catch (Exception ignored) {
            }
            // Pulse handled on the indicator; click bound later via
            // bindDistrictOrRewardsClick (must win over Local-news listener).
            applyRewardsChipVisual(labelView, pinView, pill);
        }
    }

    /** District / location black-pill chrome (Play Store padding + pin + label). */
    private void bindDistrictLocationChrome(
            @Nullable TextView labelView,
            @Nullable ImageView pinView,
            @Nullable View pill,
            @NonNull String label) {
        if (labelView != null) {
            labelView.setVisibility(View.VISIBLE);
            labelView.setAlpha(1f);
            labelView.setText(label);
            // Single-row footer: ellipsize long Telugu districts without crushing name/Follow.
            labelView.setMaxLines(1);
            labelView.setEllipsize(TextUtils.TruncateAt.END);
            // Undo promo chip textSize/typeface from a recycled Flash/Rewards bind.
            // Match Play Store / layout @dimen/_12sdp (or admin writer-strip location size).
            applyDistrictPillLabelSize(labelView);
            try {
                labelView.setTypeface(labelView.getTypeface(), android.graphics.Typeface.NORMAL);
            } catch (Exception ignored) {
            }
            try {
                labelView.setTextColor(ContextCompat.getColor(context, R.color.black));
            } catch (Exception e) {
                try {
                    labelView.setTextColor(ContextCompat.getColor(context, R.color.text_color_primary));
                } catch (Exception ignored) {
                }
            }
        }
        if (pinView != null) {
            pinView.setVisibility(View.VISIBLE);
            restoreDistrictPinSize(pinView);
            // Location pin for Main district; news icon for Local Pradhana /
            // category feed titles.
            if (categery_id == 0 && !isMainFeedTitleLabel(label)) {
                pinView.setBackgroundResource(ic_location_icon);
            } else {
                pinView.setBackgroundResource(ic_news_icon);
            }
        }
        if (pill != null) {
            pill.setVisibility(View.VISIBLE);
            pill.setAlpha(1f);
            try {
                pill.clearAnimation();
            } catch (Exception ignored) {
            }
            clearRewardsChipVisual(pinView, pill);
        }
    }

    /**
     * True when {@code s} is translator My Feed / Main News chrome (Pradhana Varthalu),
     * not a district or category place name. Used so Local paints this as the
     * redirection pill (click → Main).
     */
    private boolean isMainFeedTitleLabel(@Nullable String s) {
        if (!isUsableLocationLabel(s)) {
            return false;
        }
        String t = s.trim();
        if ("Main News".equalsIgnoreCase(t)
                || "My Feed".equalsIgnoreCase(t)
                || "MY FEED".equals(t)) {
            return true;
        }
        if ("ప్రధాన వార్తలు".equals(t)
                || "మెయిన్ వార్తలు".equals(t)
                || "నా ఫీడ్".equals(t)
                || "പ്രധാന വാർത്തകൾ".equals(t)
                || "मुख्य खबरें".equals(t)
                || "मेरा फ़ीड".equals(t)
                || "முதன்மை செய்திகள்".equals(t)
                || "ಮುಖ್ಯ ಸುದ್ದಿ".equals(t)
                || "ମୁଖ୍ୟ ଖବର".equals(t)) {
            return true;
        }
        // Prefer live translator getMyFeed when available.
        try {
            if (languageModelResponse != null
                    && languageModelResponse.getData() != null
                    && languageModelResponse.getData().getMyFeed() != null) {
                String myFeed = languageModelResponse.getData().getMyFeed().toString().trim();
                if (isUsableLocationLabel(myFeed) && t.equalsIgnoreCase(myFeed)) {
                    return !DistrictLabelHelper.matchesStoredName(context, t);
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    /**
     * Local-feed redirection label: ctor {@code my_feed_name} when it is Pradhana,
     * else live translator {@code getMyFeed()}.
     */
    @Nullable
    private String resolvePradhanaRedirectLabel() {
        if (isUsableLocationLabel(my_feed_name) && isMainFeedTitleLabel(my_feed_name)) {
            return my_feed_name.trim();
        }
        try {
            if (languageModelResponse != null
                    && languageModelResponse.getData() != null
                    && languageModelResponse.getData().getMyFeed() != null) {
                String myFeed = languageModelResponse.getData().getMyFeed().toString().trim();
                if (isUsableLocationLabel(myFeed)) {
                    return myFeed;
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * Opens Rewards hub; guests go through login first, then Rewards.
     * Never routes to Local news.
     */
    private void openRewardsHubFromChip() {
        try {
            boolean loggedIn = SharedPrefsData.getBool(
                    context.getApplicationContext(), Constants.is_login, Constants.PREF_NAME);
            android.content.Intent intent;
            if (!loggedIn) {
                intent = new android.content.Intent(
                        context,
                        com.anyNews.anynews.Activities.LoginActivityDetails.class);
                intent.putExtra(
                        com.anyNews.anynews.Activities.LoginActivityDetails.EXTRA_OPEN_REWARDS,
                        true);
            } else {
                intent = new android.content.Intent(
                        context,
                        com.anyNews.anynews.rewards.RewardsHubActivity.class);
            }
            if (!(context instanceof android.app.Activity)) {
                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            context.startActivity(intent);
        } catch (Exception ignored) {
        }
    }

    /**
     * District pill → Local/Main toggle (Play Store July / origin/main); Rewards → hub;
     * Flash → Flash News. Gated by {@code location_pill_opens_local_feed} (default ON).
     * Must run after legacy location listeners.
     */
    private void bindDistrictOrRewardsClick(View pill, @Nullable ModelClass.Datum post) {
        if (pill == null) return;
        final ModelClass.Datum boundPost = post;
        // Match visible chrome: category title / Local Pradhana / Main district
        // beats Flash/Rewards toggles. Click still toggles Local ↔ Main (category → Main).
        final boolean showingCategoryFeedTitle =
                categery_id > 0 && isUsableLocationLabel(my_feed_name);
        final boolean showingLocalPradhana =
                categery_id == -1 && isUsableLocationLabel(resolvePradhanaRedirectLabel());
        final boolean showingMainDistrict =
                categery_id == 0 && isUsableLocationLabel(
                        DistrictLabelHelper.resolveDisplayLabel(context));
        final boolean showingTogglePill =
                showingCategoryFeedTitle || showingLocalPradhana || showingMainDistrict;
        final boolean opensLocal =
                DistrictPinVisibilityConfig.locationPillOpensLocalFeed(context);
        pill.setOnClickListener(v -> {
            if (showingTogglePill) {
                // Main → Local (−1); Local/category → Main (0).
                // Admin OFF: district / ప్రధాన వార్తలు pill is display-only.
                if (opensLocal && callbacks != null) {
                    callbacks.callLocationRequest(0);
                }
                return;
            }
            if (DistrictPinVisibilityConfig.shouldShowFlashWhenBoth(
                    context, categery_id, boundPost)) {
                openFlashNewsFromChip();
            } else if (DistrictPinVisibilityConfig.shouldShowRewardsWhenBoth(
                    context, categery_id, boundPost)) {
                openRewardsHubFromChip();
            } else if (DistrictPinVisibilityConfig.shouldShowRewardsInDistrictSlot(
                    context, categery_id, boundPost)) {
                openRewardsHubFromChip();
            } else if (DistrictPinVisibilityConfig.shouldShowFlashInDistrictSlot(
                    context, categery_id, boundPost)) {
                openFlashNewsFromChip();
            } else if (opensLocal && callbacks != null) {
                callbacks.callLocationRequest(0);
            }
        });
        if (!showingTogglePill
                && DistrictPinVisibilityConfig.shouldShowFlashWhenBoth(
                        context, categery_id, boundPost)
                && DistrictPinVisibilityConfig.shouldShowRewardsWhenBoth(
                        context, categery_id, boundPost)) {
            View companion = findCompanionFeedChip(pill);
            if (companion != null) {
                companion.setOnClickListener(v -> openRewardsHubFromChip());
            }
        }
    }

    private void openFlashNewsFromChip() {
        if (callbacks != null) {
            callbacks.openFlashNewsFromFeedChip();
        }
    }

    private static final String COMPANION_CHIP_TAG = "feed_companion_chip";
    private static final String COMPANION_LABEL_TAG = "feed_companion_label";
    private static final String COMPANION_PIN_TAG = "feed_companion_pin";

    /**
     * Show-both: Flash stays on the existing right pill; Rewards sits beside it
     * (or stacked if the row is too narrow). Single-chip modes never call this.
     */
    private void bindDualFeedChips(
            @Nullable TextView labelView,
            @Nullable ImageView pinView,
            @Nullable View pill,
            @Nullable ModelClass.Datum post) {
        if (pill == null) return;
        boolean showFlash =
                DistrictPinVisibilityConfig.shouldShowFlashWhenBoth(context, categery_id, post);
        boolean showRewards =
                DistrictPinVisibilityConfig.shouldShowRewardsWhenBoth(context, categery_id, post);
        String flashLabel = FlashNewsChipHelper.chipLabel(context);
        String rewardsLabel = resolveRewardsSlotLabel();
        if (showFlash && showRewards) {
            View companion = ensureCompanionFeedChip(pill);
            bindOnePromoChip(labelView, pinView, pill, flashLabel, true);
            bindCompanionRewardsChip(companion, rewardsLabel);
            layoutDualChipRow(pill, companion, labelView, flashLabel, rewardsLabel);
            pill.setOnClickListener(v -> openFlashNewsFromChip());
            if (companion != null) {
                companion.setOnClickListener(v -> openRewardsHubFromChip());
            }
            return;
        }
        hideCompanionFeedChip(pill);
        restoreSingleChipRow(pill, labelView);
        if (showFlash) {
            bindOnePromoChip(labelView, pinView, pill, flashLabel, true);
            pill.setOnClickListener(v -> openFlashNewsFromChip());
        } else if (showRewards) {
            bindOnePromoChip(labelView, pinView, pill, rewardsLabel, false);
            pill.setOnClickListener(v -> openRewardsHubFromChip());
        }
    }

    private void bindOnePromoChip(
            @Nullable TextView labelView,
            @Nullable ImageView pinView,
            @Nullable View pill,
            @Nullable String label,
            boolean flash) {
        if (labelView != null) {
            labelView.setVisibility(View.VISIBLE);
            labelView.setAlpha(1f);
            labelView.setText(label == null ? "" : label);
            labelView.setMaxLines(1);
            labelView.setEllipsize(TextUtils.TruncateAt.END);
        }
        if (pill != null) {
            pill.setVisibility(View.VISIBLE);
            pill.setAlpha(1f);
            try {
                pill.clearAnimation();
            } catch (Exception ignored) {
            }
        }
        applyRewardsChipVisual(labelView, pinView, pill);
        if (flash && labelView != null) {
            try {
                labelView.setTextColor(android.graphics.Color.parseColor("#FF6B00"));
            } catch (Exception ignored) {
            }
        }
    }

    @Nullable
    private View ensureCompanionFeedChip(@Nullable View primaryPill) {
        if (primaryPill == null || !(primaryPill.getParent() instanceof android.view.ViewGroup)) {
            return null;
        }
        android.view.ViewGroup host = (android.view.ViewGroup) primaryPill.getParent();
        View existing = host.findViewWithTag(COMPANION_CHIP_TAG);
        if (existing != null) return existing;
        android.widget.LinearLayout chip = new android.widget.LinearLayout(context);
        chip.setTag(COMPANION_CHIP_TAG);
        chip.setOrientation(android.widget.LinearLayout.HORIZONTAL);
        chip.setGravity(android.view.Gravity.CENTER);
        chip.setClickable(true);
        chip.setFocusable(true);
        float density = context.getResources().getDisplayMetrics().density;
        int padH = (int) (6 * density);
        int padV = (int) (2 * density);
        chip.setPadding(padH, padV, padH, padV);
        android.widget.ImageView pin = new android.widget.ImageView(context);
        pin.setTag(COMPANION_PIN_TAG);
        pin.setVisibility(View.GONE);
        chip.addView(pin);
        android.widget.TextView label = new android.widget.TextView(context);
        label.setTag(COMPANION_LABEL_TAG);
        label.setMaxLines(1);
        label.setEllipsize(android.text.TextUtils.TruncateAt.END);
        label.setGravity(android.view.Gravity.CENTER);
        label.setIncludeFontPadding(false);
        chip.addView(
                label,
                new android.widget.LinearLayout.LayoutParams(
                        android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                        android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
        android.widget.LinearLayout.LayoutParams lp =
                new android.widget.LinearLayout.LayoutParams(
                        android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                        android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        int idx = host.indexOfChild(primaryPill);
        if (idx < 0) {
            host.addView(chip, lp);
        } else {
            host.addView(chip, idx, lp);
        }
        return chip;
    }

    private void bindCompanionRewardsChip(@Nullable View companion, @Nullable String label) {
        if (!(companion instanceof android.view.ViewGroup)) return;
        android.view.ViewGroup group = (android.view.ViewGroup) companion;
        android.widget.TextView tv = findTaggedText(group, COMPANION_LABEL_TAG);
        android.widget.ImageView pin = null;
        View pinView = group.findViewWithTag(COMPANION_PIN_TAG);
        if (pinView instanceof android.widget.ImageView) {
            pin = (android.widget.ImageView) pinView;
        }
        if (tv != null) {
            tv.setText(label == null ? "" : label);
            tv.setVisibility(View.VISIBLE);
        }
        companion.setVisibility(View.VISIBLE);
        companion.setAlpha(1f);
        companion.setClickable(true);
        applyRewardsChipVisual(tv, pin, companion);
    }

    @Nullable
    private static android.widget.TextView findTaggedText(
            @Nullable android.view.ViewGroup group, String tag) {
        if (group == null) return null;
        View v = group.findViewWithTag(tag);
        return v instanceof android.widget.TextView ? (android.widget.TextView) v : null;
    }

    private void hideCompanionFeedChip(@Nullable View primaryPill) {
        View companion = findCompanionFeedChip(primaryPill);
        if (companion == null) return;
        android.widget.ImageView pin = null;
        if (companion instanceof android.view.ViewGroup) {
            View pinView = ((android.view.ViewGroup) companion).findViewWithTag(COMPANION_PIN_TAG);
            if (pinView instanceof android.widget.ImageView) {
                pin = (android.widget.ImageView) pinView;
            }
        }
        clearRewardsChipVisual(pin, companion);
        companion.setVisibility(View.GONE);
        companion.setOnClickListener(null);
    }

    @Nullable
    private View findCompanionFeedChip(@Nullable View primaryPill) {
        if (primaryPill == null || !(primaryPill.getParent() instanceof android.view.ViewGroup)) {
            return null;
        }
        return ((android.view.ViewGroup) primaryPill.getParent()).findViewWithTag(COMPANION_CHIP_TAG);
    }

    /** Restore the writers-row right column after dual-chip (recycle safety). */
    private void restoreSingleChipRow(@Nullable View pill, @Nullable TextView labelView) {
        if (pill == null) return;
        float density = context.getResources().getDisplayMetrics().density;
        if (pill.getParent() instanceof android.widget.LinearLayout) {
            android.widget.LinearLayout host = (android.widget.LinearLayout) pill.getParent();
            host.setOrientation(android.widget.LinearLayout.HORIZONTAL);
            host.setGravity(android.view.Gravity.END | android.view.Gravity.CENTER_VERTICAL);
        }
        ViewGroup.LayoutParams plp = pill.getLayoutParams();
        if (plp != null) {
            plp.height = android.view.ViewGroup.LayoutParams.MATCH_PARENT;
            pill.setLayoutParams(plp);
        }
        setFooterHeight(pill, footerHeightPx());
        if (labelView != null) {
            labelView.setMaxWidth((int) (90 * density));
        }
    }

    /**
     * Place Rewards (companion, left) and Flash (primary, right). Side by side
     * when they fit; otherwise grow the writers row and stack so both stay tappable.
     */
    private void layoutDualChipRow(
            @Nullable View pill,
            @Nullable View companion,
            @Nullable TextView flashLabelView,
            @Nullable String flashLabel,
            @Nullable String rewardsLabel) {
        if (pill == null || companion == null) return;
        if (!(pill.getParent() instanceof android.widget.LinearLayout)) return;
        android.widget.LinearLayout host = (android.widget.LinearLayout) pill.getParent();
        float density = context.getResources().getDisplayMetrics().density;
        int screenW = context.getResources().getDisplayMetrics().widthPixels;
        int flashLen = flashLabel == null ? 0 : flashLabel.length();
        int rewardsLen = rewardsLabel == null ? 0 : rewardsLabel.length();
        float textPx = 9f * density;
        float estimated = (flashLen + rewardsLen) * textPx * 0.62f + (72f * density);
        int available = (int) (screenW * 0.52f);
        boolean stack = screenW < (360 * density) || estimated > available;

        host.setOrientation(
                stack
                        ? android.widget.LinearLayout.VERTICAL
                        : android.widget.LinearLayout.HORIZONTAL);
        host.setGravity(android.view.Gravity.END | android.view.Gravity.CENTER_VERTICAL);
        host.setClipChildren(false);

        int maxW = (int) (screenW * (stack ? 0.46f : 0.30f));
        int minTouch = (int) (32 * density);
        applyCompactChip(flashLabelView, pill, maxW, textPx, minTouch, stack);
        android.widget.TextView companionLabel = null;
        if (companion instanceof android.view.ViewGroup) {
            companionLabel = findTaggedText((android.view.ViewGroup) companion, COMPANION_LABEL_TAG);
        }
        applyCompactChip(companionLabel, companion, maxW, textPx, minTouch, stack);

        ViewGroup.LayoutParams clp = companion.getLayoutParams();
        if (clp instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) clp;
            mlp.height = stack
                    ? android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                    : android.view.ViewGroup.LayoutParams.MATCH_PARENT;
            if (stack) {
                mlp.bottomMargin = (int) (2 * density);
                mlp.setMarginEnd(0);
            } else {
                mlp.bottomMargin = 0;
                mlp.setMarginEnd((int) (4 * density));
            }
            companion.setLayoutParams(mlp);
        }
        ViewGroup.LayoutParams plp = pill.getLayoutParams();
        if (plp != null) {
            plp.height = stack
                    ? android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                    : android.view.ViewGroup.LayoutParams.MATCH_PARENT;
            pill.setLayoutParams(plp);
        }
        int footerH = stack
                ? Math.max(footerHeightPx() + (int) (22 * density), (int) (58 * density))
                : footerHeightPx();
        setFooterHeight(pill, footerH);
        host.requestLayout();
    }

    private void applyCompactChip(
            @Nullable TextView label,
            @Nullable View chip,
            int maxWidthPx,
            float textPx,
            int minTouchPx,
            boolean stacked) {
        if (label != null) {
            label.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, textPx);
            label.setMaxWidth(Math.max((int) (56 * context.getResources().getDisplayMetrics().density), maxWidthPx));
            label.setMaxLines(1);
            label.setEllipsize(TextUtils.TruncateAt.END);
        }
        if (chip != null) {
            chip.setMinimumHeight(stacked ? minTouchPx : 0);
            chip.setClickable(true);
        }
    }

    private int footerHeightPx() {
        return (int) (40 * context.getResources().getDisplayMetrics().density);
    }

    private void setFooterHeight(@Nullable View pill, int heightPx) {
        if (pill == null) return;
        View host = pill.getParent() instanceof View ? (View) pill.getParent() : null;
        View footer = host != null && host.getParent() instanceof View ? (View) host.getParent() : null;
        if (footer == null || footer.getId() != R.id.ll_footer_layout) return;
        ViewGroup.LayoutParams lp = footer.getLayoutParams();
        if (lp == null || lp.height == heightPx) return;
        lp.height = heightPx;
        footer.setLayoutParams(lp);
    }

    /** Orange solid dot + expanding pulse ring (LIVE-style); open text, no pill border. */
    private void applyRewardsChipVisual(
            @Nullable TextView labelView, @Nullable ImageView pinView, @Nullable View pill) {
        try {
            if (labelView != null) {
                String colorHex =
                        com.anyNews.anynews.rewards.RewardsConfigHelper.chipColor(context);
                try {
                    labelView.setTextColor(android.graphics.Color.parseColor(colorHex));
                } catch (Exception e) {
                    labelView.setTextColor(android.graphics.Color.parseColor("#FFFF6B00"));
                }
                float sp = com.anyNews.anynews.rewards.RewardsConfigHelper.chipSizeSp(context);
                labelView.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, sp);
                boolean bold =
                        com.anyNews.anynews.rewards.RewardsConfigHelper.chipBold(context);
                labelView.setTypeface(
                        labelView.getTypeface(),
                        bold
                                ? android.graphics.Typeface.BOLD
                                : android.graphics.Typeface.NORMAL);
            }
            if (pill != null) {
                // Open/plain: no district-style pill stroke/fill — pulse + text only.
                try {
                    pill.setBackground(null);
                } catch (Exception ignored) {
                }
                try {
                    pill.setPadding(0, pill.getPaddingTop(), 0, pill.getPaddingBottom());
                } catch (Exception ignored) {
                }
            }
            if (!(pill instanceof android.view.ViewGroup) || pinView == null) return;
            android.view.ViewGroup group = (android.view.ViewGroup) pill;
            View pulseHost = group.findViewWithTag("rewards_pulse_host");
            if (pulseHost == null) {
                android.widget.FrameLayout host = new android.widget.FrameLayout(context);
                host.setTag("rewards_pulse_host");
                int size = (int) (18 * context.getResources().getDisplayMetrics().density);
                android.widget.LinearLayout.LayoutParams lp =
                        new android.widget.LinearLayout.LayoutParams(size, size);
                lp.setMargins(0, 0, (int) (6 * context.getResources().getDisplayMetrics().density), 0);
                host.setLayoutParams(lp);

                View ring = new View(context);
                ring.setTag("rewards_pulse_ring");
                ring.setBackgroundResource(
                        com.anyNews.anynews.R.drawable.bg_rewards_chip_ring);
                android.widget.FrameLayout.LayoutParams ringLp =
                        new android.widget.FrameLayout.LayoutParams(
                                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                                android.widget.FrameLayout.LayoutParams.MATCH_PARENT);
                host.addView(ring, ringLp);

                View dot = new View(context);
                dot.setTag("rewards_pulse_dot");
                dot.setBackgroundResource(
                        com.anyNews.anynews.R.drawable.bg_rewards_chip_dot);
                int dotSize = (int) (8 * context.getResources().getDisplayMetrics().density);
                android.widget.FrameLayout.LayoutParams dotLp =
                        new android.widget.FrameLayout.LayoutParams(dotSize, dotSize);
                dotLp.gravity = android.view.Gravity.CENTER;
                host.addView(dot, dotLp);

                int pinIdx = group.indexOfChild(pinView);
                if (pinIdx >= 0) {
                    group.addView(host, pinIdx);
                } else {
                    group.addView(host, 0);
                }
                pulseHost = host;
            }
            pinView.setVisibility(View.GONE);
            pinView.setBackground(null);
            pulseHost.setVisibility(View.VISIBLE);
            View ring = pulseHost instanceof android.view.ViewGroup
                    ? ((android.view.ViewGroup) pulseHost).findViewWithTag("rewards_pulse_ring")
                    : null;
            if (ring != null) {
                ring.clearAnimation();
                // ValueAnimator loops reliably forever (AnimationSet child repeats often stop).
                android.animation.ValueAnimator pulse =
                        android.animation.ValueAnimator.ofFloat(0f, 1f);
                pulse.setDuration(900);
                pulse.setRepeatCount(android.animation.ValueAnimator.INFINITE);
                pulse.setRepeatMode(android.animation.ValueAnimator.RESTART);
                pulse.setInterpolator(new android.view.animation.LinearInterpolator());
                final View ringView = ring;
                pulse.addUpdateListener(anim -> {
                    float t = (float) anim.getAnimatedValue();
                    float scale = 0.15f + (1.35f - 0.15f) * t;
                    ringView.setScaleX(scale);
                    ringView.setScaleY(scale);
                    ringView.setAlpha(0.85f * (1f - t));
                });
                try {
                    Object prev = ring.getTag(com.anyNews.anynews.R.id.my_feed);
                    if (prev instanceof android.animation.ValueAnimator) {
                        ((android.animation.ValueAnimator) prev).cancel();
                    }
                } catch (Exception ignored) {
                }
                ring.setTag(com.anyNews.anynews.R.id.my_feed, pulse);
                pulse.start();
            }
        } catch (Exception ignored) {
        }
    }

    private void clearRewardsChipVisual(@Nullable ImageView pinView, @Nullable View pill) {
        try {
            if (pill instanceof android.view.ViewGroup) {
                View host = ((android.view.ViewGroup) pill).findViewWithTag("rewards_pulse_host");
                if (host != null) {
                    View ring = host instanceof android.view.ViewGroup
                            ? ((android.view.ViewGroup) host).findViewWithTag("rewards_pulse_ring")
                            : null;
                    if (ring != null) {
                        try {
                            Object anim = ring.getTag(com.anyNews.anynews.R.id.my_feed);
                            if (anim instanceof android.animation.ValueAnimator) {
                                ((android.animation.ValueAnimator) anim).cancel();
                            }
                        } catch (Exception ignored) {
                        }
                        ring.clearAnimation();
                        ring.setScaleX(1f);
                        ring.setScaleY(1f);
                        ring.setAlpha(1f);
                    }
                    host.setVisibility(View.GONE);
                }
                // Restore district pill chrome.
                try {
                    pill.setBackgroundResource(
                            com.anyNews.anynews.R.drawable.circle_article_location);
                } catch (Exception ignored) {
                }
            } else if (pill != null) {
                try {
                    pill.setBackgroundResource(
                            com.anyNews.anynews.R.drawable.circle_article_location);
                } catch (Exception ignored) {
                }
            }
            // Promo path zeroes horizontal padding — restore Play Store / July inner spacing.
            restoreDistrictPillPadding(pill);
            if (pinView != null) {
                pinView.setVisibility(View.VISIBLE);
                restoreDistrictPinSize(pinView);
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Play Store / July {@code ll_refer_and_earn_half}: paddingLeft/Right {@code _8sdp},
     * paddingTop/Bottom {@code _2sdp}. Must re-apply after {@link #applyRewardsChipVisual}
     * zeroes L/R padding on recycle. Resolve via {@code getIdentifier} — sdp dims are
     * non-transitive on {@code R.dimen} for Java.
     */
    private void restoreDistrictPillPadding(@Nullable View pill) {
        if (pill == null || context == null) return;
        try {
            int padH = sdpPx(8);
            int padV = sdpPx(2);
            pill.setPadding(padH, padV, padH, padV);
        } catch (Exception ignored) {
        }
    }

    /**
     * Play Store pin: {@code _12sdp} × {@code _10sdp}, end margin {@code _8sdp}.
     * Rewards pulse host can leave recycled layout params wrong.
     */
    private void restoreDistrictPinSize(@Nullable ImageView pinView) {
        if (pinView == null || context == null) return;
        try {
            android.view.ViewGroup.LayoutParams lp = pinView.getLayoutParams();
            if (lp == null) return;
            lp.width = sdpPx(12);
            lp.height = sdpPx(10);
            if (lp instanceof android.view.ViewGroup.MarginLayoutParams) {
                ((android.view.ViewGroup.MarginLayoutParams) lp).rightMargin = sdpPx(8);
            }
            pinView.setLayoutParams(lp);
            pinView.setPadding(0, 0, 0, 0);
        } catch (Exception ignored) {
        }
    }

    /** Label size: admin writer-strip location sdp, else Play Store {@code _12sdp}. */
    private void applyDistrictPillLabelSize(@Nullable TextView labelView) {
        if (labelView == null || context == null) return;
        float sizeSdp = 12f;
        try {
            WriterStripStyleConfig cfg = WriterStripStyleHelper.load(context);
            if (cfg != null) {
                sizeSdp = cfg.getLocationSizeSdp();
            }
        } catch (Exception ignored) {
        }
        try {
            FontSettingsHelper.applyTextSize(context, labelView, sizeSdp);
        } catch (Exception e) {
            labelView.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, sizeSdp);
        }
    }

    /** Resolve intuit {@code _Nsdp} px; approximate when the dimen is missing. */
    private int sdpPx(int sdp) {
        if (context == null) return sdp;
        try {
            int resId = context.getResources().getIdentifier(
                    "_" + sdp + "sdp", "dimen", context.getPackageName());
            if (resId != 0) {
                return context.getResources().getDimensionPixelSize(resId);
            }
        } catch (Exception ignored) {
        }
        android.util.DisplayMetrics dm = context.getResources().getDisplayMetrics();
        float swDp = Math.min(dm.widthPixels, dm.heightPixels) / dm.density;
        return Math.round(sdp * (swDp / 360f) * dm.density);
    }

    /** Guest vs joined copy for the main-feed rewards slot. */
    private String resolveRewardsSlotLabel() {
        boolean loggedIn = false;
        try {
            loggedIn = SharedPrefsData.getBool(
                    context.getApplicationContext(), Constants.is_login, Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
        boolean joined = false;
        try {
            joined = com.anyNews.anynews.rewards.RewardsConfigHelper.isJoined(context);
        } catch (Exception ignored) {
        }
        if (loggedIn && joined) {
            String server = com.anyNews.anynews.rewards.RewardsConfigHelper.joinedChip(context);
            if (server != null && !server.trim().isEmpty()
                    && !"Rewards".equalsIgnoreCase(server.trim())) {
                return server;
            }
            return com.anyNews.anynews.rewards.RewardsLabels.joinedChip(context);
        }
        String guest = com.anyNews.anynews.rewards.RewardsConfigHelper.guestChip(context);
        if (guest != null && !guest.trim().isEmpty()
                && !"Join rewards".equalsIgnoreCase(guest.trim())) {
            return guest;
        }
        return com.anyNews.anynews.rewards.RewardsLabels.guestChip(context);
    }


    /** Dedicated end-of-feed FlipView page — never a blank white View. */
    @NonNull
    private View inflateFeedEndCard(@Nullable ViewGroup parent, @Nullable View convertView) {
        return inflateFeedEndCard(parent, convertView, false);
    }

    @NonNull
    private View inflateFeedEndCard(@Nullable ViewGroup parent, @Nullable View convertView,
            boolean allowSearchHub) {
        final boolean searchHub = allowSearchHub && isSearchFeedEndHub();
        View root;
        if (searchHub) {
            root = inflater.inflate(R.layout.search_feed_hub_card,
                    parent != null ? parent : null, false);
            bindSearchFeedHub(root);
        } else {
            root = inflater.inflate(R.layout.feed_end_card,
                    parent != null ? parent : null, false);
            try {
                TextView msg = root.findViewById(R.id.feed_end_message);
                if (msg != null) {
                    msg.setText(FeedEndHelper.resolveMessage(context));
                }
                TextView hint = root.findViewById(R.id.feed_end_hint);
                if (hint != null) {
                    hint.setText(FeedEndHelper.resolveHint(context));
                }
            } catch (Exception ignored) {
            }
        }
        return root;
    }

    private boolean isSearchFeedEndHub() {
        try {
            HomePage hp = resolveHomePageHost(context);
            if (hp == null && activity instanceof HomePage) {
                hp = (HomePage) activity;
            }
            return hp != null && hp.isSearchFeedActive();
        } catch (Exception e) {
            return false;
        }
    }

    @Nullable
    private static HomePage resolveHomePageHost(@Nullable Context ctx) {
        if (ctx instanceof HomePage) {
            return (HomePage) ctx;
        }
        if (ctx instanceof android.content.ContextWrapper) {
            Context base = ((android.content.ContextWrapper) ctx).getBaseContext();
            if (base instanceof HomePage) {
                return (HomePage) base;
            }
        }
        return null;
    }

    private void bindSearchFeedHub(@NonNull View root) {
        try {
            HomePage hp = resolveHomePageHost(root.getContext());
            if (hp == null && activity instanceof HomePage) {
                hp = (HomePage) activity;
            }
            final HomePage host = hp;
            View main = root.findViewById(R.id.search_hub_main);
            View local = root.findViewById(R.id.search_hub_local);
            View flash = root.findViewById(R.id.search_hub_flash);
            View daily = root.findViewById(R.id.search_hub_daily);
            View mainDiv = root.findViewById(R.id.search_hub_div_main);
            View localDiv = root.findViewById(R.id.search_hub_div_local);
            View flashDiv = root.findViewById(R.id.search_hub_div_flash);
            boolean showDaily = host == null || host.isSearchHubDailyUpdatesAvailable();
            boolean showFlash = host == null || host.isSearchHubFlashAvailable();
            boolean showLocal = host == null || host.isSearchHubLocalAvailable();
            if (main != null) {
                main.setVisibility(View.VISIBLE);
                main.setOnClickListener(v -> {
                    if (host != null) host.openMainNewsFromSearchHub();
                });
            }
            if (mainDiv != null) {
                mainDiv.setVisibility(showLocal || showFlash || showDaily
                        ? View.VISIBLE : View.GONE);
            }
            if (local != null) {
                local.setVisibility(showLocal ? View.VISIBLE : View.GONE);
                local.setOnClickListener(v -> {
                    if (host != null) host.openLocalNewsFromSearchHub();
                });
            }
            if (localDiv != null) {
                localDiv.setVisibility(showLocal && (showFlash || showDaily)
                        ? View.VISIBLE : View.GONE);
            }
            if (flash != null) {
                flash.setVisibility(showFlash ? View.VISIBLE : View.GONE);
                flash.setOnClickListener(v -> {
                    if (host != null) host.openFlashNewsFromSearchHub();
                });
            }
            if (flashDiv != null) {
                flashDiv.setVisibility(showFlash && showDaily ? View.VISIBLE : View.GONE);
            }
            if (daily != null) {
                daily.setVisibility(showDaily ? View.VISIBLE : View.GONE);
                daily.setOnClickListener(v -> {
                    if (host != null) host.openDailyUpdatesFromSearchHub();
                });
            }
        } catch (Exception ignored) {
        }
    }

    public interface Callbacks {
        void onPageRequested(int page);
        void onRequestedPermission(int page);
        void callLocationRequest(int from);
        /** 528 Callbacks surface — no-ops allowed on NewHomePage */
        default void openFlashNewsFromFeedChip() { }
        default void onVideoAutoplayEndedRequestNext(int position) { }
        /** Ad-screen flash row → temp overlay (no FlipView splice). */
        default void openAdScreenArticleOverlay(@Nullable ModelClass.Datum datum) { }
    }

    public void setCallback(Callbacks callbacks) {
        this.callbacks=callbacks;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    // Returns true for "simple" pages (images/text) whose views can be safely
    // reused/shifted by the FlipView without re-inflating. Video/youtube/ad/web
    // pages are NOT reusable because their playback is (re)started inside getView,
    // so they must keep re-inflating to preserve existing behavior.
    public boolean isReusablePage(int position) {
        try {
            if (list == null || position < 0 || position >= list.size()) return false;
            ModelClass.Datum d = list.get(position);
            if (d == null || d.getPostType() == null) return false;
            String t = d.getPostType();
            return t.equalsIgnoreCase("normal_image")
                    || t.equalsIgnoreCase("full_image");
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public int getCount() {
        int a;

        if (list != null && !list.isEmpty()) {

            a = list.size();
        } else {

            a = 0;

        }

        return a;
        //return list.size();
    }

    @Override
    public String getItem(int position) {
        try {
            return list.get(position).getTitle();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getitemurl(int position) {
        return list.get(position).getThumbnail();
    }

    @Override
    public long getItemId(int position) {

        //return position;
        try {
            ModelClass.Datum album = list.get(position);
            if (album == null) {
                return -(position + 1L);
            }
            String pt = album.getPostType();
            if (pt != null) {
                if (pt.equalsIgnoreCase("ad")) {
                    Integer req = album.getAd_req_position();
                    if (req != null && req > 0) {
                        return 900_000_000L + req;
                    }
                    return 800_000_000L + position;
                }
                if (pt.equalsIgnoreCase("share_app")
                        || pt.equalsIgnoreCase("ratings")
                        || pt.equalsIgnoreCase("feed_end")) {
                    return 700_000_000L + position;
                }
            }
            Long pid = album.getPostId();
            if (pid == null || pid <= 0L) {
                return -(position + 1L);
            }
            return pid;
        } catch (Exception e) {
            e.printStackTrace();
            return -(position + 1L);
        }
    }


    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (BuildConfig.DEBUG) {
            showLog("getView pos=" + position + " type=" + list.get(position).getPostType());
        }
        customConvertView=convertView;
        customPosition=position;


        try {
            if (!list.isEmpty()) {

                String pagePostType = null;
                try {
                    pagePostType = list.get(position).getPostType();
                } catch (Exception ignored) {
                }
                if (FeedEndHelper.isFeedEndType(pagePostType)
                        || FeedEndHelper.isFeedEnd(list.get(position))) {
                    pauseVideo();
                    convertView = inflateFeedEndCard(parent, convertView, isSearchFeedEndHub());
                    customConvertView = convertView;
                    fromWhatsapp = false;
                    return convertView;
                }
                if (pagePostType == null) {
                    pagePostType = "";
                }
                switch (pagePostType) {
                    case "google_vasttag":
                        pauseVideo();
                        convertView = inflater.inflate(R.layout.activity_vast_tag, parent, false);
                        //    convertView = inflater.inflate(R.layout.activity_full_video, parent, false);
                        customConvertView = convertView;
                        fromWhatsapp = false;
                        full_videoViewHolder = new FullVideoViewHolder();
                        showVastTagAd(convertView, position);
                        //  showFullVideoWithExoPlayer2(position,convertView);
                        break;
                    case "normal_image":
                        showLog("Check Point Adapter:: 1");
                        pauseVideo();
                        halfImageActivity = new HalfImageActivityHolder();
                        showLog("Check Point Adapter:: 2");
                        convertView = inflater.inflate(R.layout.activity_half_image_new, parent, false);
                        customConvertView = convertView;
                        showLog("Check Point Adapter:: 3_11");
                        fromWhatsapp = false;
                        showNormalImage(position, convertView);
                        showLog("NORMAL_IMAGE Point Adapter:: 3_12");
                        break;
                    case "full_image":
                        pauseVideo();
                        showLog("Check Point FULL_IMAGE:: 1");
                        fullImageActivity = new FullImageActivityHolder();
                        convertView = inflater.inflate(R.layout.activity_full_image_new, parent, false);
                        customConvertView = convertView;
                        showFullImage(position, convertView);
                        fromWhatsapp = false;
                        showLog("Check Point FULL_IMAGE:: 9");
                        break;
                    case "full_video":
                        pauseVideo();
                        fullscreen = false;
                        //media_type: 5 => full normal video
                        //media_type: 6 => full youtube video
                        fromWhatsapp = false;
                        if (list.get(position).getMediaType().equals("5")) {
                            full_videoViewHolder = new FullVideoViewHolder();
                            convertView = inflater.inflate(R.layout.activity_full_video, parent, false);
                            showFullVideoWithExoPlayer(position, convertView);
                        }
                        // full youtube video
                        else if (list.get(position).getMediaType().equals("6")) {
                            fullVideoNewYoutubeViewHolder = new FullVideoNewYoutubeViewHolder();
                            convertView = inflater.inflate(R.layout.youtube_full_videos_new, parent, false);
                            showFullVideoWithYoutube(position, convertView);

                        }
                        // full youtube video
                        else {
                            full_youTubePlayerHolder = new FullYouTubePlayerHolder();
                            convertView = inflater.inflate(R.layout.activity_full_youtube_video_detailes, parent, false);
                        }
                        customConvertView = convertView;
                        break;
                    case "normal_video":
                        //pauseVideo();
                        showLog("post type:: normal_video");

                       /* if (list.get(position).getMediaType().equals("3")) {
                            half_videoViewHolder = new HalfVideoViewHolder();
                            convertView = inflater.inflate(R.layout.activity_half_video_detailes, parent, false);
                        }else */
                        fromWhatsapp = false;
                        if (list.get(position).getMediaType().equals("3")) {
                            halfVideoExoPlayerHolder = new HalfVideoExoPlayerHolder();
                            convertView = inflater.inflate(R.layout.activity_half_exo_video_detailes, parent, false);
                            showHalfPostExoPlayerVideo(convertView, position);
                        } else if (list.get(position).getMediaType().equals("4")) {
                            half_youTubePlayerHolder = new HalfYouTubePlayerHolder();
                            convertView = inflater.inflate(R.layout.activity_half_youtube_video_detailes, parent, false);
                            showHalfPostYoutubeVideo(convertView, position);
                        }
                        break;
                    case "magazine":
                        pauseVideo();
                        image_magazineHolder = new ImageMagazineHolder();
                        convertView = inflater.inflate(R.layout.activity_magazine_detailes, parent, false);
                        image_magazineHolder.magzine_viewPager_data = convertView.findViewById(R.id.magzine_viewPager_data);
                        image_magazineHolder.magzine_ll_dots = convertView.findViewById(R.id.magzine_ll_dots);
                        final RelativeLayout magzine_relative_slider = convertView.findViewById(R.id.magzine_relative_slider);
                        ImageView imageViewShareMag = convertView.findViewById(R.id.imageViewShare);
                        final RelativeLayout relativeLayoutShareOpt = convertView.findViewById(R.id.relativeLayoutShareOpt);
                        final TextView textViewAllPage = convertView.findViewById(R.id.textViewAllPage);
                        final TextView textViewSinglePage = convertView.findViewById(R.id.textViewSinglePage);
                        final ImageView imageViewShareClose = convertView.findViewById(R.id.imageViewShareClose);
                        //imageGallaryHolder.galleryLayout.setLayerType(View.LAYER_TYPE_NONE,null);
                        imageViewShareMag.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                relativeLayoutShareOpt.setVisibility(VISIBLE);
                            }
                        });
                        relativeLayoutShareOpt.setVisibility(GONE);
                        imageViewShareClose.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                relativeLayoutShareOpt.setVisibility(GONE);
                                imageViewShareMag.setVisibility(VISIBLE);
                                fromWhatsapp = false;
                                image_magazineHolder.magzine_ll_dots.setVisibility(VISIBLE);
                            }
                        });
                        textViewAllPage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                relativeLayoutShareOpt.setVisibility(GONE);
                                callbacks.onRequestedPermission(0);


                            }
                        });
                        textViewSinglePage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                shareMagazineScreenShot(imageViewShareMag, relativeLayoutShareOpt, position, magzine_relative_slider);
                                //   adapterShareImageView.setVisibility(VISIBLE);
                            }
                        });
                        mCardAdapter = null;
                        try {
                            Glide.with(context).load(list.get(position).getBackgroundImage()).into(new SimpleTarget<Drawable>() {
                                @Override
                                public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                                    magzine_relative_slider.setBackground(resource);
                                }
                            });
                            mCardAdapter = new CardPagerAdapter(context, list.get(position).getStrip_content(), new CardPagerAdapter.shareMagazine() {
                                @Override
                                public void onClickShareMagzine(CardView cardView, ImageView imageView1) {
                                    fromWhatsapp = false;
                                    adapterCardView = cardView;
                                    relativeLayoutShareOpt.setVisibility(VISIBLE);


                                }
                            });
                            for (int i = 0; i < list.get(position).getImages().size(); i++) {
                                mCardAdapter.addCardItem(new CardItem("" + list.get(position).getImages().get(i), "" + "" + list.get(position).getImages().get(i), list.get(position).getShareLink(), "" + list.get(position).getTitle(), list.get(position).getPostId()));
                            }
                            mCardShadowTransformer = new ShadowTransformer(image_magazineHolder.magzine_viewPager_data, mCardAdapter);
                            mCardShadowTransformer.enableScaling(true);
                            image_magazineHolder.magzine_viewPager_data.setAdapter(mCardAdapter);
                            image_magazineHolder.magzine_ll_dots.removeAllViews();
                            dots = new TextView[mCardAdapter.getCount()];
                            for (int i = 0; i < dots.length; i++) {
                                dots[i] = new TextView(context);
                                dots[i].setText(Html.fromHtml("&#9673;"));
                                dots[i].setTextSize(18);
                                dots[i].setTextColor(Color.WHITE);
                                image_magazineHolder.magzine_ll_dots.addView(dots[i]);
                            }
                            if (dots.length > 0) {
                                dots[0].setTextColor(Color.RED);
                                dots[0].setTextSize(30);
                            }
                            image_magazineHolder.magzine_viewPager_data.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                                @Override
                                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                                }

                                @Override
                                public void onPageSelected(int position) {
                                    try {
                                        dots = new TextView[mCardAdapter.getCount()];
                                        image_magazineHolder.magzine_ll_dots.removeAllViews();
                                        for (int i = 0; i < dots.length; i++) {
                                            dots[i] = new TextView(context);
                                            dots[i].setText(Html.fromHtml("&#9673;"));
                                            dots[i].setTextSize(20);
                                            dots[i].setTextColor(Color.WHITE);
                                            image_magazineHolder.magzine_ll_dots.addView(dots[i]);
                                        }
                                        if (dots.length > 0) {
                                            dots[position].setTextColor(Color.RED);
                                            dots[position].setTextSize(40);
                                        }
                                    } catch (ArrayIndexOutOfBoundsException e) {
                                        e.printStackTrace();
                                    }


                                }

                                @Override
                                public void onPageScrollStateChanged(int state) {
                                }
                            });

                        } catch (Exception e) {
                        }
                        break;
                    case "ad":
                        pauseVideo();
                        String TAG = "AD DATA";
                        adsScreenViewHolder = new AdsScreenViewHolder();
                        showLog("HomePage getAd_type::" + list.get(position).getAd_type());
                        if (list.get(position).getAd_type().equalsIgnoreCase("google")) {
                            convertView = inflater.inflate(R.layout.ads_screen, parent, false);
                            final RelativeLayout layoutParent = convertView.findViewById(R.id.layoutParent);
                            layoutParent.getLayoutParams().height = screenHeight;
                            final FrameLayout fl_adplaceholder = convertView.findViewById(R.id.fl_adplaceholder);
                            fl_adplaceholder.setVisibility(VISIBLE);
                            fl_adplaceholder.setBackgroundColor(0);
                            fl_adplaceholder.removeAllViews();
                            bindAdsScreenCompanion(convertView, position);
                            adapterCallback.closeBottomStrip(true);
                            View myView = LayoutInflater.from(context).inflate(R.layout.custom_ad_native, null);
                            int fromView = 0;
                            if (list.get(position).getAd_view_type().equalsIgnoreCase(Constants.AD_FULL_IMAGE)) {
                                fromView = 0;
                                myView = LayoutInflater.from(context).inflate(R.layout.anynews_ad_full_template_view, null);
                            } else if (list.get(position).getAd_view_type().equalsIgnoreCase(Constants.AD_MEDIUM)) {
                                fromView = 1;
                                myView = LayoutInflater.from(context).inflate(R.layout.anynews_ad_medium_template_view, null);
                            } else {
                                fromView = 2;
                                myView = LayoutInflater.from(context).inflate(R.layout.custom_ad_native, null);
                            }

                            NativeAd ad = null;
                            //   myView = LayoutInflater.from(context).inflate(R.layout.anynews_ad_medium_template_view, null);
                            if (HomePage.nativeGoogleAdList != null && HomePage.nativeGoogleAdList.size() > 0) {
                                showLog("HomePage.nativeAdList is not empty");
                                for (int i = 0; i < HomePage.nativeGoogleAdList.size(); i++) {
                                    showLog("AD CODE@ STORE DATA:::" + HomePage.nativeGoogleAdList.get(i).getAdCode() + "::POS::" + HomePage.nativeGoogleAdList.get(i).getReqPos());
                                    showLog("AD CODE@ LIST DATA:::" + list.get(position).getAd_code() + "::POS:::" + list.get(position).getAd_req_position());
                                    if (HomePage.nativeGoogleAdList.get(i).getAdCode().equalsIgnoreCase(list.get(position).getAd_code()) && HomePage.nativeGoogleAdList.get(i).getReqPos() == list.get(position).getAd_req_position()) {
                                        showLog("HomePage.nativeAdList displaying native ad:");
                                        ad = HomePage.nativeGoogleAdList.get(i).getAd();
                                        nativeAd = HomePage.nativeGoogleAdList.get(i).getAd();
                                    }
                                }
                                if (ad == null) {
                                    showLog("AD ERORRRRRRRRRRRR@@@");
                                    if(null != list.get(position).getAds_from()){
                                        if (list.get(position).getAds_from().equalsIgnoreCase("admanager")) {
                                            ad = nativeAd;
                                        } else if (list.get(position).getAds_from().equalsIgnoreCase("admob")) {
                                            ad = nativeAd;
                                        }
                                    }
                                }


                                if (ad != null) {
                                    View finalConvertView2 = convertView;
                                    View finalMyView = myView;
                                    int finalFromView = fromView;
                                    NativeAd finalAd = ad;
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            showLog("HomePage.nativeAdList 222 displaying native ad:");
                                            showLog("fromView:;" + finalFromView);
                                            if (finalFromView == 0) {
                                                displayGoogleFullNativeAd(fl_adplaceholder, finalAd, finalMyView, finalConvertView2);
                                            } else if (finalFromView == 1) {
                                                displayGoogleMediumNativeAd(fl_adplaceholder, finalAd, finalMyView, finalConvertView2);
                                            } else {
                                                displayGoogleNativeAd(fl_adplaceholder, finalAd, finalMyView, finalConvertView2);
                                            }
                                            //  displayGoogleNativeMediumAd(position, nativeAd,finalConvertView2);
                                            // displayGoogleNativeIntertitialsAd(fl_adplaceholder, nativeAd,myView, finalConvertView2);
                                        }
                                    }, 500);

                                } else {
                                    showLog("ads_screen: native no-fill — keep Way2 placeholder");
                                }
                            } else {
                                showLog("ads_screen: native pool empty — keep Way2 placeholder");
                            }
                        } else {
                            convertView = inflater.inflate(R.layout.fb_ad_post, parent, false);
                            NativeAdLayout nativeAdLayout = convertView.findViewById(R.id.native_ad_container);
                            LayoutInflater inflater = LayoutInflater.from(context);
                            // Inflate the Ad view.  The layout referenced should be the one you created in the last step.
                            RelativeLayout adView = (RelativeLayout) inflater.inflate(R.layout.custom_fb_ad, nativeAdLayout, false);
                            nativeAdLayout.addView(adView);
                            if (HomePage.nativeFBAdList != null && HomePage.nativeFBAdList.size() > 0) {
                                showLog("HomePage.nativeAdList is not empty");
                                for (int i = 0; i < HomePage.nativeFBAdList.size(); i++) {
                                    showLog("HomePage.nativeFBAdList getAdCode :" + HomePage.nativeFBAdList.get(i).getAdCode());
                                    showLog("HomePage.nativeFBAdList LIST getAd_code :" + list.get(position).getAd_code());
                                    showLog("HomePage.nativeFBAdList getReqPos :" + HomePage.nativeFBAdList.get(i).getReqPos());
                                    showLog("HomePage.nativeFBAdList LIST getAd_req_position :" + list.get(position).getAd_req_position());
                                    if (HomePage.nativeFBAdList.get(i).getAdCode().equalsIgnoreCase(list.get(position).getAd_code()) && HomePage.nativeFBAdList.get(i).getReqPos() == list.get(position).getAd_req_position()) {
                                        showLog("HomePage.nativeAdList AD MATCHED");
                                        fbAd = HomePage.nativeFBAdList.get(i).getFbAd();
                                    }
                                }
                                View finalConvertView1 = convertView;
                                if (fbAd != null) {
                                    showLog(" fb ad :");
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            displayFBNativeAd(finalConvertView1, fbAd, adView, nativeAdLayout);
                                        }
                                    }, 500);
                                } else {
                                    showLog("ads_screen: fb no-fill — keep Way2 placeholder");
                                }
                            } else {
                                showLog("ads_screen: fb pool empty — keep Way2 placeholder");
                            }
                        }
                        break;
                    case "ratings":
                        pauseVideo();
                        ratingScreenViewHolder = new RatingScreenViewHolder();
                        convertView = inflater.inflate(R.layout.rating_screen_layout, parent, false);
                        EditText editTextAppReview = convertView.findViewById(R.id.editTextAppReview);
                        Button buttonAppSubmit = convertView.findViewById(R.id.buttonAppSubmit);
                        RatingBar ratingBarApp = convertView.findViewById(R.id.ratingBarApp);
                        TextView textViewRatSubTitle = convertView.findViewById(R.id.textViewRatSubTitle);
                        TextView textViewRatingTitle = convertView.findViewById(R.id.textViewRatingTitle);
                        if (editTextAppReview != null) {
                            editTextAppReview.setVisibility(GONE);
                        }
                        if (buttonAppSubmit != null) {
                            buttonAppSubmit.setVisibility(GONE);
                        }
                        textViewRatSubTitle.setVisibility(GONE);
                        boolean isAppRatingSubmitted = SharedPrefsData.getBool(context, Constants.APP_RATING_SUBMITTED, Constants.PREF_NAME);
                        if (isAppRatingSubmitted) {
                            textViewRatSubTitle.setVisibility(VISIBLE);
                            textViewRatingTitle.setVisibility(GONE);
                            ratingBarApp.setVisibility(VISIBLE);
                            editTextAppReview.setVisibility(GONE);
                            buttonAppSubmit.setVisibility(GONE);
                            ratingBarApp.setIsIndicator(true);
                            float rating = SharedPrefsData.getFloat(context, Constants.APP_RATING, Constants.PREF_NAME);
                            ratingBarApp.setRating(rating);
                        } else {
                            textViewRatingTitle.setVisibility(VISIBLE);
                            ratingBarApp.setVisibility(VISIBLE);
                        }
                        buttonAppSubmit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                RatingSubmitRequest requestModel = new RatingSubmitRequest();
                                requestModel.setDraft_id((long) SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME));
                                requestModel.setComments(editTextAppReview.getText().toString());
                                requestModel.setRating(ratingBarApp.getRating());
                                String jsonString = new com.google.gson.Gson().toJson(requestModel);
                                ratingBarApp.setIsIndicator(true);
                                String encryptData = encrypt(jsonString);
                                showLog("API REQUEST E" + jsonString.toString());
                                sendAppRatingSubmitData(encryptData, 1, new CallBackListener() {
                                    @Override
                                    public void callBackOnResponse() {
                                        Toast.makeText(context, "Rating submitted successfully", Toast.LENGTH_SHORT).show();
                                        SharedPrefsData.putBool(context, Constants.APP_RATING_SUBMITTED, true, Constants.PREF_NAME);
                                        SharedPrefsData.putFloat(context, Constants.APP_RATING, ratingBarApp.getRating(), Constants.PREF_NAME);
                                        textViewRatSubTitle.setVisibility(VISIBLE);
                                        textViewRatingTitle.setVisibility(GONE);
                                        editTextAppReview.setVisibility(GONE);
                                        buttonAppSubmit.setVisibility(GONE);
                                    }

                                    @Override
                                    public void callBackOnError() {
                                    }
                                });

                            }
                        });
                        ratingBarApp.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                            @Override
                            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                                if (ratingBar.getRating() <= 3) {
                                    SharedPrefsData.putBool(context, Constants.APP_RATING_SUBMITTED, false, Constants.PREF_NAME);
                                    editTextAppReview.setVisibility(VISIBLE);
                                    buttonAppSubmit.setVisibility(VISIBLE);


                                } else {
                                    SharedPrefsData.putBool(context, Constants.APP_RATING_SUBMITTED, true, Constants.PREF_NAME);
                                    SharedPrefsData.putFloat(context, Constants.APP_RATING, ratingBar.getRating(), Constants.PREF_NAME);
                                    textViewRatSubTitle.setVisibility(VISIBLE);
                                    textViewRatingTitle.setVisibility(GONE);
                                    editTextAppReview.setVisibility(GONE);
                                    buttonAppSubmit.setVisibility(GONE);
                                    ratingBarApp.setIsIndicator(true);
                                    final String appPackageName = context.getPackageName(); // getPackageName() from Context or Activity object
                                    try {
                                        // context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                    } catch (android.content.ActivityNotFoundException anfe) {
                                        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                    }
                                    RatingSubmitRequest requestModel = new RatingSubmitRequest();
                                    requestModel.setDraft_id((long) SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME));
                                    requestModel.setComments("Thank you..");
                                    requestModel.setRating(ratingBarApp.getRating());
                                    String jsonString = new com.google.gson.Gson().toJson(requestModel);
                                    ratingBarApp.setIsIndicator(true);
                                    String encryptData = encrypt(jsonString);
                                    sendAppRatingSubmitData(encryptData, 1, new CallBackListener() {
                                        @Override
                                        public void callBackOnResponse() {
                                        }

                                        @Override
                                        public void callBackOnError() {
                                        }
                                    });

                                }

                            }
                        });
                        break;
                    case "feed_end":
                        pauseVideo();
                        convertView = inflateFeedEndCard(parent, convertView, isSearchFeedEndHub());
                        customConvertView = convertView;
                        fromWhatsapp = false;
                        break;
                    case "share_app":
                        convertView = inflater.inflate(R.layout.share_app_layout, parent, false);
                        ImageView imageViewShare = convertView.findViewById(R.id.imageViewShare);
                        ImageView imageViewWhatsapp = convertView.findViewById(R.id.imageViewWhatsapp);
                        imageViewShare.setImageResource(R.drawable.ic_app_share_socail);
                        imageViewWhatsapp.setImageResource(R.drawable.ic_whatsapp_home);
                        imageViewWhatsapp.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent sendIntent = new Intent();
                                sendIntent.setAction(Intent.ACTION_SEND);
                                sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                sendIntent.setPackage("com.whatsapp");
                                sendIntent.putExtra(Intent.EXTRA_TEXT,
                                        "Hey check out my app at: https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID);
                                sendIntent.setType("text/plain");
                                context.startActivity(sendIntent);

                            }
                        });
                        break;
                    case "image_gallery":
                        pauseVideo();
                        ArrayList<String> image_gallary = new ArrayList<>();
                        showLog("image_gallery:::");
                        imageGallaryHolder = new ImageGallaryHolder();
                        convertView = inflater.inflate(R.layout.activity_circular_indicatior, parent, false);
                        imageGallaryHolder.galleryLayout = convertView.findViewById(R.id.gallery_layout);
                        imageGallaryHolder.viewpager = convertView.findViewById(R.id.viewpager);
                        ViewPager viewpager = convertView.findViewById(R.id.viewpager);
                        imageGallaryHolder.scrren_icon = convertView.findViewById(R.id.scrren_icon);
                        imageGallaryHolder.image_gallary_share = convertView.findViewById(R.id.image_gallary_share);
                        imageGallaryHolder.dotIndicatior = convertView.findViewById(R.id.dotIndicatior);
                        imageGallaryHolder.image_gallary_scrip = convertView.findViewById(R.id.image_gallary_scrip);
                        imageGallaryHolder.strip_text_name = convertView.findViewById(R.id.strip_text_name);
                        imageGallaryHolder.indicator = convertView.findViewById(R.id.indicator);
                        imageGallaryHolder.relativeLayoutShareOpt = convertView.findViewById(R.id.relativeLayoutShareOpt);
                        imageGallaryHolder.textViewAllPage = convertView.findViewById(R.id.textViewAllPage);
                        imageGallaryHolder.textViewSinglePage = convertView.findViewById(R.id.textViewSinglePage);
                        imageGallaryHolder.imageViewShareClose = convertView.findViewById(R.id.imageViewShareClose);
                        //imageGallaryHolder.galleryLayout.setLayerType(View.LAYER_TYPE_NONE,null);
                        imageGallaryHolder.relativeLayoutShareOpt.setVisibility(GONE);
                        final Animation myAnim = AnimationUtils.loadAnimation(context, R.anim.bounce);
                        double animationDuration = 8 * 1000;
                        myAnim.setDuration((long) animationDuration);
                        // Use custom animation interpolator to achieve the bounce effect
                        MyBounceInterpolator interpolator = new MyBounceInterpolator(0.60, 20.00);
                        myAnim.setInterpolator(interpolator);
                        imageGallaryHolder.image_gallary_share.startAnimation(myAnim);
                        //imageGallaryHolder.indicator.initialize(config);
                        for (int i = 0; i < list.get(position).getImages().size(); i++) {
                            image_gallary.add(list.get(position).getImages().get(i));

                        }
                        viewpager.setAdapter(new SamplePagerAdapter(context, image_gallary, 0, false));
                        // added to increase speed in view pager
                        viewpager.setOffscreenPageLimit(list.get(position).getImages().size() - 1);
                        imageGallaryHolder.indicator.setViewPager(viewpager);
                        imageGallaryHolder.strip_text_name.setText(displayCopy(list.get(position).getStrip_content()));
                        applyFeedStripFont(imageGallaryHolder.strip_text_name);
                        View finalConvertView = convertView;
                        imageGallaryHolder.imageViewShareClose.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                fromWhatsapp = false;
                                imageGallaryHolder.relativeLayoutShareOpt = finalConvertView.findViewById(R.id.relativeLayoutShareOpt);
                                imageGallaryHolder.image_gallary_share = finalConvertView.findViewById(R.id.image_gallary_share);
                                imageGallaryHolder.dotIndicatior = finalConvertView.findViewById(R.id.dotIndicatior);
                                imageGallaryHolder.relativeLayoutShareOpt.setVisibility(GONE);
                                imageGallaryHolder.image_gallary_share.setVisibility(VISIBLE);
                                imageGallaryHolder.dotIndicatior.setVisibility(VISIBLE);

                            }
                        });
                        imageGallaryHolder.textViewAllPage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                callbacks.onRequestedPermission(0);
                                imageGallaryHolder.relativeLayoutShareOpt = finalConvertView.findViewById(R.id.relativeLayoutShareOpt);
                                imageGallaryHolder.image_gallary_share = finalConvertView.findViewById(R.id.image_gallary_share);
                                imageGallaryHolder.dotIndicatior = finalConvertView.findViewById(R.id.dotIndicatior);
                                imageGallaryHolder.relativeLayoutShareOpt.setVisibility(GONE);
                                imageGallaryHolder.image_gallary_share.setVisibility(VISIBLE);
                                imageGallaryHolder.dotIndicatior.setVisibility(VISIBLE);                               // downloadPDFWithProgressBar(list.get(position).getDownloadUrl(),"anyNews+"+list.get(position).getPostId()+".pdf",position);
                            }
                        });
                        View finalConvertView3 = convertView;
                        imageGallaryHolder.textViewSinglePage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                shareGalleryScreenShot(position, 0, finalConvertView3);
                                imageGallaryHolder.relativeLayoutShareOpt = finalConvertView.findViewById(R.id.relativeLayoutShareOpt);
                                imageGallaryHolder.image_gallary_share = finalConvertView.findViewById(R.id.image_gallary_share);
                                imageGallaryHolder.dotIndicatior = finalConvertView.findViewById(R.id.dotIndicatior);
                                imageGallaryHolder.image_gallary_share.setVisibility(VISIBLE);
                                imageGallaryHolder.dotIndicatior.setVisibility(VISIBLE);
                            }
                        });
                        imageGallaryHolder.image_gallary_share.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                fromWhatsapp = false;
                                imageGallaryHolder.relativeLayoutShareOpt = finalConvertView.findViewById(R.id.relativeLayoutShareOpt);
                                imageGallaryHolder.image_gallary_share = finalConvertView.findViewById(R.id.image_gallary_share);
                                imageGallaryHolder.dotIndicatior = finalConvertView.findViewById(R.id.dotIndicatior);
                                imageGallaryHolder.relativeLayoutShareOpt.setVisibility(VISIBLE);
                                imageGallaryHolder.image_gallary_share.setVisibility(GONE);
                                imageGallaryHolder.dotIndicatior.setVisibility(GONE);
//
//                                imageGallaryHolder.image_gallary_scrip.setVisibility(View.GONE);
//
//
//                                String appLink = "https://com.anyNews.anynews/";
//
//                                String mInvitationUrl = list.get(position).getShareLink();
//                                String msg = list.get(position).getTitle() + " "
//                                        + mInvitationUrl;
//
//
//                                countDownTimer = new CountDownTimer(2, 2) {
//
//                                    public void onTick(long millisUntilFinished) {
//
//                                    }
//
//                                    public void onFinish() {
//
//                                        Intent intent = new Intent(Intent.ACTION_SEND);
//                                        intent.setType("text/plain");
//                                        intent.putExtra(Intent.EXTRA_SUBJECT, mInvitationUrl.toString());
//                                        intent.putExtra(Intent.EXTRA_TEXT, msg);
//                                        Bitmap b = ScreenShott.getInstance().takeScreenShotOfView(imageGallaryHolder.scrren_icon);
//
//                                        //  layout_strip_iconnew.setVisibility(View.GONE);
//                                        try {
//                                            File file = new File(context.getExternalCacheDir(), "sample.png");
//                                            FileOutputStream fOut = new FileOutputStream(file);
//                                            b.compress(Bitmap.CompressFormat.PNG, 100, fOut);
//                                            fOut.flush();
//                                            fOut.close();
//                                            file.setReadable(true, true);
//                                            Uri photoURI = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
//                                            intent.putExtra(Intent.EXTRA_STREAM, photoURI);
//                                            List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(Intent.createChooser(intent, "Share Link"), PackageManager.MATCH_DEFAULT_ONLY);
//                                            for (ResolveInfo resolveInfo : resInfoList) {
//                                                String packageName = resolveInfo.activityInfo.packageName;
//                                                context.grantUriPermission(packageName, photoURI, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                                            }
//                                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                                        } catch (Exception e) {
//                                            e.printStackTrace();
//                                        }
//                                        imageGallaryHolder.image_gallary_scrip.setVisibility(View.GONE);
//                                        intent.setType("image/*");
//                                        if (context != null && context.getPackageName().equals(BuildConfig.APPLICATION_ID)) {
//                                            context.startActivity(Intent.createChooser(intent, "Share Link"));
//                                        }
//
//
//                                        Share_Count_Value("share", list.get(position).getPostId());
//                                    }
//                                }.start();
                            }
                        });
                        checking = true;
                        break;
                    case "image_slider":
                        break;
                    case "video_slider":
                        pauseVideo();
                        videoViewHolder = new VideosliderHolder();
                        convertView = inflater.inflate(R.layout.activity_video_slider_detailes, parent, false);
                        break;
                    case "video_gallery":
                        video_galaryHolder = new VideogalaryHolder();
                        convertView = inflater.inflate(R.layout.activity_video_gallery_detailes, parent, false);
                        break;
                    case "web_ads":
                        pauseVideo();
                        webViewHolderAD = new WebViewHolderAD();
                        String eventLink = "" + list.get(position).getSource_link();
                        showLog("LINK:::"+list.get(position).getSource_link());
                        convertView = inflater.inflate(R.layout.activity_webview_ads, parent, false);
                      final WebView webView = convertView.findViewById(R.id.webView);
                        webView.setWebViewClient(new SSLTolerentWebViewClientADs());
                        webView.getSettings().setLoadsImagesAutomatically(true);
                        webView.getSettings().setJavaScriptEnabled(true);
                        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
                        webView.getSettings().setLoadWithOverviewMode(true);
                        webView.getSettings().setUseWideViewPort(true);
                        webView.getSettings().setBuiltInZoomControls(true);
                        webView.getSettings().setDisplayZoomControls(false);
                        webView.loadUrl(eventLink);


                        break;
                    case "web":
                        pauseVideo();
                        webViewHolder = new WebViewHolder();
                      //   eventLink = "" + list.get(position).getSource_link();
                        convertView = inflater.inflate(R.layout.activity_webview_detailes, parent, false);
                        webViewHolder.webView = convertView.findViewById(R.id.webView);
                        webViewHolder.webView.setWebViewClient(new FlipperAdapter.SSLTolerentWebViewClient());
                        webViewHolder.webView.getSettings().setLoadsImagesAutomatically(true);
                        webViewHolder.webView.getSettings().setJavaScriptEnabled(true);
                        webViewHolder.webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
                        webViewHolder.webView.getSettings().setLoadWithOverviewMode(true);
                        webViewHolder.webView.getSettings().setUseWideViewPort(true);
                        webViewHolder.webView.getSettings().setBuiltInZoomControls(true);
                        webViewHolder.webView.getSettings().setDisplayZoomControls(false);
                        webViewHolder.webView.loadUrl(list.get(position).getSource_link());
                        break;

                    case "survey":
                        pauseVideo();
                        surveyViewHolder = new SurveyViewHolder();
                        convertView = inflater.inflate(R.layout.activity_survey_page_flip, parent, false);

                        surveyViewHolder.relativeLayoutSurveyMain = convertView.findViewById(R.id.rl_survay_main);
                        surveyViewHolder.tvSurveyQuestionEnglish = convertView.findViewById(R.id.tv_survey_question_english);
                        surveyViewHolder.neomorphFrameLayoutYes = convertView.findViewById(R.id.neomorphFrameLayoutYes);
                        surveyViewHolder.neomorphFrameLayoutYesSelected = convertView.findViewById(R.id.neomorphFrameLayoutYesSelected);
                        surveyViewHolder.neomorphFrameLayoutNo = convertView.findViewById(R.id.neomorphFrameLayoutNo);
                        surveyViewHolder.neomorphFrameLayoutNoSelected = convertView.findViewById(R.id.neomorphFrameLayoutNoSelected);
                        surveyViewHolder.neomorphFrameLayoutYesPercent = convertView.findViewById(R.id.neomorphFrameLayoutYesPercent);
                        surveyViewHolder.neomorphFrameLayoutNoPercent = convertView.findViewById(R.id.neomorphFrameLayoutNoPercent);
                        surveyViewHolder.tvAnswerYes = convertView.findViewById(R.id.tv_answer_yes);
                        surveyViewHolder.tvAnswerNo = convertView.findViewById(R.id.tv_answer_no);
                        surveyViewHolder.llPercentLayout = convertView.findViewById(R.id.ll_percent_layout);
                        surveyViewHolder.tvYesPercent = convertView.findViewById(R.id.tv_yes_percent);
                        surveyViewHolder.tvNoPercent = convertView.findViewById(R.id.tv_no_percent);

                        try {
                            imageBackgroundURL = list.get(position).getBackgroundImage();
                            Glide.with(getApplicationContext())
                                    .load(imageBackgroundURL)
                                    .into(new CustomTarget<Drawable>() {
                                        @Override
                                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                            surveyViewHolder.relativeLayoutSurveyMain.setBackground(resource);
                                        }

                                        @Override
                                        public void onLoadCleared(@Nullable Drawable placeholder) {

                                        }
                                    });

                            surveyViewHolder.tvSurveyQuestionEnglish.setText("" + list.get(position).getTitle());
                            surveyViewHolder.tvAnswerYes.setText("" + list.get(position).getYes_option());
                            surveyViewHolder.tvAnswerNo.setText("" + list.get(position).getNo_option());


                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                        if (list.get(position).getSurveyStatus() == 0) {

                            surveyViewHolder.llPercentLayout.setVisibility(View.INVISIBLE);


                        } else {

                            surveyViewHolder.llPercentLayout.setVisibility(View.VISIBLE);

                            surveyViewHolder.tvYesPercent.setText(" " + list.get(position).getYes_percent() + "%");
                            surveyViewHolder.tvNoPercent.setText(" " + list.get(position).getNo_percent() + "%");


                            surveyViewHolder.neomorphFrameLayoutYes.setVisibility(View.GONE);
                            surveyViewHolder.neomorphFrameLayoutYesSelected.setVisibility(View.GONE);

                            surveyViewHolder.neomorphFrameLayoutNoSelected.setVisibility(View.GONE);
                            surveyViewHolder.neomorphFrameLayoutNo.setVisibility(View.GONE);
                        }

                        surveyViewHolder.neomorphFrameLayoutYes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                surveyViewHolder.neomorphFrameLayoutYesSelected.setVisibility(View.INVISIBLE);
                                surveyViewHolder.neomorphFrameLayoutYes.setVisibility(View.GONE);
                                surveyViewHolder.neomorphFrameLayoutNoSelected.setVisibility(View.GONE);
                                surveyViewHolder.neomorphFrameLayoutNo.setVisibility(View.INVISIBLE);
                                surveyViewHolder.llPercentLayout.setVisibility(View.VISIBLE);

                                surveyViewHolder.neomorphFrameLayoutYesSelected.setEnabled(false);
                                surveyViewHolder.neomorphFrameLayoutYesSelected.setShadowInner();
                                surveyViewHolder.neomorphFrameLayoutYesSelected.setClickable(false);

                                surveyViewHolder.neomorphFrameLayoutNoSelected.setEnabled(false);
                                surveyViewHolder.neomorphFrameLayoutNoSelected.setClickable(false);
                                surveyViewHolder.neomorphFrameLayoutNoSelected.setShadowInner();

                                surveyViewHolder.neomorphFrameLayoutNo.setEnabled(false);
                                surveyViewHolder.neomorphFrameLayoutYes.setEnabled(false);

                                SurveyResponse("" + list.get(position).getYes_option(), list.get(position).getPostId(), position);

                            }
                        });

                        surveyViewHolder.neomorphFrameLayoutNo.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                surveyViewHolder.neomorphFrameLayoutNoSelected.setVisibility(View.INVISIBLE);
                                surveyViewHolder.neomorphFrameLayoutNo.setVisibility(View.GONE);
                                surveyViewHolder.neomorphFrameLayoutYes.setVisibility(View.GONE);
                                surveyViewHolder.neomorphFrameLayoutNoSelected.setVisibility(View.INVISIBLE);
                                surveyViewHolder.llPercentLayout.setVisibility(View.VISIBLE);

                                surveyViewHolder.neomorphFrameLayoutYesSelected.setEnabled(false);
                                surveyViewHolder.neomorphFrameLayoutYesSelected.setShadowInner();
                                surveyViewHolder.neomorphFrameLayoutYesSelected.setClickable(false);

                                surveyViewHolder.neomorphFrameLayoutNoSelected.setEnabled(false);
                                surveyViewHolder.neomorphFrameLayoutNoSelected.setClickable(false);
                                surveyViewHolder.neomorphFrameLayoutNoSelected.setShadowInner();

                                surveyViewHolder.neomorphFrameLayoutNo.setEnabled(false);
                                surveyViewHolder.neomorphFrameLayoutYes.setEnabled(false);

                                SurveyResponse("" + list.get(position).getNo_option(), list.get(position).getPostId(), position);

                            }
                        });

                        break;
                    case "survey_poll":
                        pauseVideo();
                        pollingViewHolder = new PollingViewHolder();
                        convertView = inflater.inflate(R.layout.activity_polling_page_flip, parent, false);
                        pollingViewHolder.relativeLayoutSurveyMain = convertView.findViewById(R.id.rl_survay_main);
/*



                        pollingViewHolder.llPercentLayout = convertView.findViewById(R.id.ll_percent_layout);




                        pollingViewHolder.llPercentLayout.setVisibility(View.GONE);*/

                        pollingViewHolder.tv_polling_question = convertView.findViewById(R.id.tv_polling_question);
                        pollingViewHolder.polling_half_image = convertView.findViewById(R.id.polling_half_image);


                        pollingViewHolder.tv_answer_A = convertView.findViewById(R.id.tv_answer_A);
                        pollingViewHolder.tv_answer_B = convertView.findViewById(R.id.tv_answer_B);
                        pollingViewHolder.tv_answer_C = convertView.findViewById(R.id.tv_answer_C);
                        pollingViewHolder.tv_answer_D = convertView.findViewById(R.id.tv_answer_D);

                        pollingViewHolder.polling_A = convertView.findViewById(R.id.polling_A);
                        pollingViewHolder.polling_B = convertView.findViewById(R.id.polling_B);
                        pollingViewHolder.polling_C = convertView.findViewById(R.id.polling_C);
                        pollingViewHolder.polling_D = convertView.findViewById(R.id.polling_D);

                        pollingViewHolder.select_tv_answer_A = convertView.findViewById(R.id.select_tv_answer_A);
                        pollingViewHolder.select_tv_answer_B = convertView.findViewById(R.id.select_tv_answer_B);
                        pollingViewHolder.select_tv_answer_C = convertView.findViewById(R.id.select_tv_answer_C);
                        pollingViewHolder.select_tv_answer_D = convertView.findViewById(R.id.select_tv_answer_D);

                        pollingViewHolder.select_polling_A = convertView.findViewById(R.id.select_polling_A);
                        pollingViewHolder.select_polling_B = convertView.findViewById(R.id.select_polling_B);
                        pollingViewHolder.select_polling_C = convertView.findViewById(R.id.select_polling_C);
                        pollingViewHolder.select_polling_D = convertView.findViewById(R.id.select_polling_D);


                        pollingViewHolder.neomorphFrameLayoutA = convertView.findViewById(R.id.neomorphFrameLayoutA);
                        pollingViewHolder.neomorphFrameLayoutB = convertView.findViewById(R.id.neomorphFrameLayoutB);
                        pollingViewHolder.neomorphFrameLayoutC = convertView.findViewById(R.id.neomorphFrameLayoutC);
                        pollingViewHolder.neomorphFrameLayoutD = convertView.findViewById(R.id.neomorphFrameLayoutD);

                        pollingViewHolder.select_neomorphFrameLayoutA = convertView.findViewById(R.id.select_neomorphFrameLayoutA);
                        pollingViewHolder.select_neomorphFrameLayoutB = convertView.findViewById(R.id.select_neomorphFrameLayoutB);
                        pollingViewHolder.select_neomorphFrameLayoutC = convertView.findViewById(R.id.select_neomorphFrameLayoutC);
                        pollingViewHolder.select_neomorphFrameLayoutD = convertView.findViewById(R.id.select_neomorphFrameLayoutD);


                        pollingViewHolder.tv_polling_question.setText(list.get(position).getTitle());
                        if (pollingViewHolder.polling_half_image.getTag() == null ||
                                !pollingViewHolder.polling_half_image.getTag().equals(list.get(position).getBackgroundImage())) {

                            // below method will display image inside our image view..
                            imageLoader.displayImage(list.get(position).getBackgroundImage(), pollingViewHolder.polling_half_image, options, null);

                        }


                        if (list.get(position).getSelected_option() == 1) {
                            pollingViewHolder.neomorphFrameLayoutA.setVisibility(View.GONE);
                            pollingViewHolder.neomorphFrameLayoutB.setVisibility(View.VISIBLE);
                            pollingViewHolder.neomorphFrameLayoutC.setVisibility(View.VISIBLE);
                            pollingViewHolder.neomorphFrameLayoutD.setVisibility(View.VISIBLE);

                            pollingViewHolder.select_neomorphFrameLayoutA.setVisibility(View.VISIBLE);
                            pollingViewHolder.select_neomorphFrameLayoutB.setVisibility(View.GONE);
                            pollingViewHolder.select_neomorphFrameLayoutC.setVisibility(View.GONE);
                            pollingViewHolder.select_neomorphFrameLayoutD.setVisibility(View.GONE);

                            pollingViewHolder.select_polling_A.setVisibility(View.VISIBLE);
                            pollingViewHolder.polling_B.setVisibility(View.VISIBLE);
                            pollingViewHolder.polling_C.setVisibility(View.VISIBLE);
                            pollingViewHolder.polling_D.setVisibility(View.VISIBLE);


                        } else if (list.get(position).getSelected_option() == 2) {
                            pollingViewHolder.neomorphFrameLayoutA.setVisibility(View.VISIBLE);
                            pollingViewHolder.neomorphFrameLayoutB.setVisibility(View.GONE);
                            pollingViewHolder.neomorphFrameLayoutC.setVisibility(View.VISIBLE);
                            pollingViewHolder.neomorphFrameLayoutD.setVisibility(View.VISIBLE);

                            pollingViewHolder.select_neomorphFrameLayoutA.setVisibility(View.GONE);
                            pollingViewHolder.select_neomorphFrameLayoutB.setVisibility(View.VISIBLE);
                            pollingViewHolder.select_neomorphFrameLayoutC.setVisibility(View.GONE);
                            pollingViewHolder.select_neomorphFrameLayoutD.setVisibility(View.GONE);

                            pollingViewHolder.polling_A.setVisibility(View.VISIBLE);
                            pollingViewHolder.select_polling_B.setVisibility(View.VISIBLE);
                            pollingViewHolder.polling_C.setVisibility(View.VISIBLE);
                            pollingViewHolder.polling_D.setVisibility(View.VISIBLE);

                        } else if (list.get(position).getSelected_option() == 3) {

                            pollingViewHolder.neomorphFrameLayoutA.setVisibility(View.VISIBLE);
                            pollingViewHolder.neomorphFrameLayoutB.setVisibility(View.VISIBLE);
                            pollingViewHolder.neomorphFrameLayoutC.setVisibility(View.GONE);
                            pollingViewHolder.neomorphFrameLayoutD.setVisibility(View.VISIBLE);

                            pollingViewHolder.select_neomorphFrameLayoutA.setVisibility(View.GONE);
                            pollingViewHolder.select_neomorphFrameLayoutB.setVisibility(View.GONE);
                            pollingViewHolder.select_neomorphFrameLayoutC.setVisibility(View.VISIBLE);
                            pollingViewHolder.select_neomorphFrameLayoutD.setVisibility(View.GONE);

                            pollingViewHolder.polling_A.setVisibility(View.VISIBLE);
                            pollingViewHolder.polling_B.setVisibility(View.VISIBLE);
                            pollingViewHolder.select_polling_C.setVisibility(View.VISIBLE);
                            pollingViewHolder.polling_D.setVisibility(View.VISIBLE);
                        } else if (list.get(position).getSelected_option() == 4) {
                            pollingViewHolder.neomorphFrameLayoutA.setVisibility(View.VISIBLE);
                            pollingViewHolder.neomorphFrameLayoutB.setVisibility(View.VISIBLE);
                            pollingViewHolder.neomorphFrameLayoutC.setVisibility(View.VISIBLE);
                            pollingViewHolder.neomorphFrameLayoutD.setVisibility(View.GONE);

                            pollingViewHolder.select_neomorphFrameLayoutA.setVisibility(View.GONE);
                            pollingViewHolder.select_neomorphFrameLayoutB.setVisibility(View.GONE);
                            pollingViewHolder.select_neomorphFrameLayoutC.setVisibility(View.GONE);
                            pollingViewHolder.select_neomorphFrameLayoutD.setVisibility(View.VISIBLE);

                            pollingViewHolder.polling_A.setVisibility(View.VISIBLE);
                            pollingViewHolder.polling_B.setVisibility(View.VISIBLE);
                            pollingViewHolder.polling_C.setVisibility(View.VISIBLE);
                            pollingViewHolder.select_polling_D.setVisibility(View.VISIBLE);

                        }


                        if (list.get(position).getSurveyOptions().size() == 2) {
                            pollingViewHolder.select_tv_answer_A.setText(list.get(position).getSurveyOptions().get(0).getSurveyOptionsName());
                            pollingViewHolder.select_tv_answer_B.setText(list.get(position).getSurveyOptions().get(1).getSurveyOptionsName());

                            pollingViewHolder.select_polling_A.setText("" + list.get(position).getSurveyOptions().get(0).getPercent() + "%");
                            pollingViewHolder.select_polling_B.setText("" + list.get(position).getSurveyOptions().get(1).getPercent() + "%");

                            pollingViewHolder.tv_answer_A.setText(list.get(position).getSurveyOptions().get(0).getSurveyOptionsName());
                            pollingViewHolder.tv_answer_B.setText(list.get(position).getSurveyOptions().get(1).getSurveyOptionsName());

                            pollingViewHolder.polling_A.setText("" + list.get(position).getSurveyOptions().get(0).getPercent() + "%");
                            pollingViewHolder.polling_B.setText("" + list.get(position).getSurveyOptions().get(1).getPercent() + "%");

                            pollingViewHolder.select_neomorphFrameLayoutC.setVisibility(View.GONE);
                            pollingViewHolder.select_neomorphFrameLayoutD.setVisibility(View.GONE);
                            pollingViewHolder.neomorphFrameLayoutC.setVisibility(View.GONE);
                            pollingViewHolder.neomorphFrameLayoutD.setVisibility(View.GONE);

                        } else if (list.get(position).getSurveyOptions().size() == 3) {
                            pollingViewHolder.select_tv_answer_A.setText(list.get(position).getSurveyOptions().get(0).getSurveyOptionsName());
                            pollingViewHolder.select_tv_answer_B.setText(list.get(position).getSurveyOptions().get(1).getSurveyOptionsName());
                            pollingViewHolder.select_tv_answer_C.setText(list.get(position).getSurveyOptions().get(2).getSurveyOptionsName());

                            pollingViewHolder.select_polling_A.setText("" + list.get(position).getSurveyOptions().get(0).getPercent() + "%");
                            pollingViewHolder.select_polling_B.setText("" + list.get(position).getSurveyOptions().get(1).getPercent() + "%");
                            pollingViewHolder.select_polling_C.setText("" + list.get(position).getSurveyOptions().get(2).getPercent() + "%");

                            pollingViewHolder.tv_answer_A.setText(list.get(position).getSurveyOptions().get(0).getSurveyOptionsName());
                            pollingViewHolder.tv_answer_B.setText(list.get(position).getSurveyOptions().get(1).getSurveyOptionsName());
                            pollingViewHolder.tv_answer_C.setText(list.get(position).getSurveyOptions().get(2).getSurveyOptionsName());

                            pollingViewHolder.polling_A.setText("" + list.get(position).getSurveyOptions().get(0).getPercent() + "%");
                            pollingViewHolder.polling_B.setText("" + list.get(position).getSurveyOptions().get(1).getPercent() + "%");
                            pollingViewHolder.polling_C.setText("" + list.get(position).getSurveyOptions().get(2).getPercent() + "%");

                            pollingViewHolder.select_neomorphFrameLayoutD.setVisibility(View.GONE);
                            pollingViewHolder.neomorphFrameLayoutD.setVisibility(View.GONE);

                        } else {
                            pollingViewHolder.select_tv_answer_A.setText("" + list.get(position).getSurveyOptions().get(0).getSurveyOptionsName());
                            pollingViewHolder.select_tv_answer_B.setText("" + list.get(position).getSurveyOptions().get(1).getSurveyOptionsName());
                            pollingViewHolder.select_tv_answer_C.setText("" + list.get(position).getSurveyOptions().get(2).getSurveyOptionsName());
                            pollingViewHolder.select_tv_answer_D.setText("" + list.get(position).getSurveyOptions().get(3).getSurveyOptionsName());

                            pollingViewHolder.select_polling_A.setText("" + list.get(position).getSurveyOptions().get(0).getPercent() + "%");
                            pollingViewHolder.select_polling_B.setText("" + list.get(position).getSurveyOptions().get(1).getPercent() + "%");
                            pollingViewHolder.select_polling_C.setText("" + list.get(position).getSurveyOptions().get(2).getPercent() + "%");
                            pollingViewHolder.select_polling_D.setText("" + list.get(position).getSurveyOptions().get(3).getPercent() + "%");

                            pollingViewHolder.tv_answer_A.setText("" + list.get(position).getSurveyOptions().get(0).getSurveyOptionsName());
                            pollingViewHolder.tv_answer_B.setText("" + list.get(position).getSurveyOptions().get(1).getSurveyOptionsName());
                            pollingViewHolder.tv_answer_C.setText("" + list.get(position).getSurveyOptions().get(2).getSurveyOptionsName());
                            pollingViewHolder.tv_answer_D.setText("" + list.get(position).getSurveyOptions().get(3).getSurveyOptionsName());

                            pollingViewHolder.polling_A.setText("" + list.get(position).getSurveyOptions().get(0).getPercent() + "%");
                            pollingViewHolder.polling_B.setText("" + list.get(position).getSurveyOptions().get(1).getPercent() + "%");
                            pollingViewHolder.polling_C.setText("" + list.get(position).getSurveyOptions().get(2).getPercent() + "%");
                            pollingViewHolder.polling_D.setText("" + list.get(position).getSurveyOptions().get(3).getPercent() + "%");

                        }


                        pollingViewHolder.neomorphFrameLayoutA.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {


                                updatearraylist(list.get(position).getSurveyOptions().get(0).getId(), position);
                                showPollingResuilt("A", position, list.get(position).getSurveyOptions().size(), list.get(position).getPostId(), list.get(position).getSurveyOptions().get(0).getId());

                            }
                        });
                        pollingViewHolder.neomorphFrameLayoutB.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                updatearraylist(list.get(position).getSurveyOptions().get(1).getId(), position);
                                showPollingResuilt("B", position, list.get(position).getSurveyOptions().size(), list.get(position).getPostId(), list.get(position).getSurveyOptions().get(1).getId());

                            }
                        });
                        pollingViewHolder.neomorphFrameLayoutC.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                updatearraylist(list.get(position).getSurveyOptions().get(2).getId(), position);
                                showPollingResuilt("C", position, list.get(position).getSurveyOptions().size(), list.get(position).getPostId(), list.get(position).getSurveyOptions().get(2).getId());
                            }
                        });
                        pollingViewHolder.neomorphFrameLayoutD.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                updatearraylist(list.get(position).getSurveyOptions().get(3).getId(), position);
                                showPollingResuilt("D", position, list.get(position).getSurveyOptions().size(), list.get(position).getPostId(), list.get(position).getSurveyOptions().get(3).getId());
                            }
                        });

                        break;

                }
            }

        } catch (NullPointerException e) {
            e.printStackTrace();
        }


        return convertView;

    }

    private void showVastTagAd(View convertView, int position) {
        showLog("showVastTagAd:"+position);
        // Drop any prior VAST session before starting a new one (flip recycle).
        destroyActiveVastSession();
        VideoView videoPlayer;
        MediaController mediaController;
        View playButton;
        VastTagAdVideoAdPlayerAdapter videoAdPlayerAdapter;
        ImaSdkFactory sdkFactory;
        // The AdsLoader instance exposes the requestAds method.
        AdsLoader adsLoader;
        // AdsManager exposes methods to control ad playback and listen to ad events.

        // The saved content position, used to resumed content following an ad break.
        int savedPosition = 0;
        mediaController = new MediaController(context);

        videoPlayer = convertView.findViewById(R.id.videoView);
        activeVastVideoView = videoPlayer;
        mediaController.setAnchorView(videoPlayer);
       // videoPlayer.setMediaController(mediaController);
        showLog("showVastTagAd:2");
        // Create an ad display container that uses a ViewGroup to listen to taps.
        ViewGroup videoPlayerContainer = convertView.findViewById(R.id.videoPlayerContainer);
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        videoAdPlayerAdapter = new VastTagAdVideoAdPlayerAdapter(videoPlayer, audioManager);

        sdkFactory = ImaSdkFactory.getInstance();
        showLog("showVastTagAd:3");
        AdDisplayContainer adDisplayContainer =
                ImaSdkFactory.createAdDisplayContainer(videoPlayerContainer, videoAdPlayerAdapter);

        // Create an AdsLoader.
        ImaSdkSettings settings = sdkFactory.createImaSdkSettings();
        adsLoader = sdkFactory.createAdsLoader(context, settings, adDisplayContainer);
        activeVastAdsLoader = adsLoader;

        // Add listeners for when ads are loaded and for errors.
        adsLoader.addAdErrorListener(
                new AdErrorEvent.AdErrorListener() {
                    /** An event raised when there is an error loading or playing ads. */
                    @Override
                    public void onAdError(AdErrorEvent adErrorEvent) {
                        Log.i("LOGTAG", "Ad Error: " + adErrorEvent.getError().getMessage());
                        //resumeContent();
                    }
                });
        adsLoader.addAdsLoadedListener(
                new AdsLoader.AdsLoadedListener() {
                    @Override
                    public void onAdsManagerLoaded(AdsManagerLoadedEvent adsManagerLoadedEvent) {
                        // Ads were successfully loaded, so get the AdsManager instance. AdsManager has
                        // events for ad playback and errors.
                        final AdsManager[] adsManager = new AdsManager[1];
                        adsManager[0] = adsManagerLoadedEvent.getAdsManager();
                        activeVastAdsManager = adsManager[0];

                        // Attach event and error event listeners.
                        adsManager[0].addAdErrorListener(
                                new AdErrorEvent.AdErrorListener() {
                                    /** An event raised when there is an error loading or playing ads. */
                                    @Override
                                    public void onAdError(AdErrorEvent adErrorEvent) {
                                        Log.e("LOGTAG", "Ad Error: " + adErrorEvent.getError().getMessage());
                                        String universalAdIds =
                                                Arrays.toString(adsManager[0].getCurrentAd().getUniversalAdIds());
                                        Log.i(
                                                "LOGTAG",
                                                "Discarding the current ad break with universal "
                                                        + "ad Ids: "
                                                        + universalAdIds);
                                        adsManager[0].discardAdBreak();
                                    }
                                });
                        adsManager[0].addAdEventListener(
                                new AdEvent.AdEventListener() {

                                    /** Responds to AdEvents. */
                                    @Override
                                    public void onAdEvent(AdEvent adEvent) {
                                        if (adEvent.getType() != AdEvent.AdEventType.AD_PROGRESS) {
                                            Log.i("LOGTAG", "Event: " + adEvent.getType());
                                        }
                                        // These are the suggested event types to handle. For full list of
                                        // all ad event types, see AdEvent.AdEventType documentation.
                                        switch (adEvent.getType()) {
                                            case LOADED:
                                                // AdEventType.LOADED is fired when ads are ready to play.

                                                // This sample app uses the sample tag
                                                // single_preroll_skippable_ad_tag_url that requires calling
                                                // AdsManager.start() to start ad playback.
                                                // If you use a different ad tag URL that returns a VMAP or
                                                // an ad rules playlist, the adsManager.init() function will
                                                // trigger ad playback automatically and the IMA SDK will
                                                // ignore the adsManager.start().
                                                // It is safe to always call adsManager.start() in the
                                                // LOADED event.
                                                adsManager[0].start();
                                                break;
                                            case CONTENT_PAUSE_REQUESTED:
                                                // AdEventType.CONTENT_PAUSE_REQUESTED is fired when you
                                                // should pause your content and start playing an ad.
                                               // pauseContentForAds();
                                                break;
                                            case CONTENT_RESUME_REQUESTED:
                                                // AdEventType.CONTENT_RESUME_REQUESTED is fired when the ad
                                                // you should play your content.
                                                //resumeContent();
                                                break;
                                            case ALL_ADS_COMPLETED:
                                                // Calling adsManager.destroy() triggers the function
                                                // VideoAdPlayer.release().
                                                try {
                                                    adsManager[0].destroy();
                                                } catch (Exception ignored) {
                                                }
                                                if (activeVastAdsManager == adsManager[0]) {
                                                    activeVastAdsManager = null;
                                                }
                                                adsManager[0] = null;
                                                break;
                                            case CLICKED:
                                                // When the user clicks on the Learn More button, the IMA SDK fires
                                                // this event, pauses the ad, and opens the ad's click-through URL.
                                                // When the user returns to the app, the IMA SDK calls the
                                                // VideoAdPlayer.playAd() function automatically.
                                                break;
                                            default:
                                                break;
                                        }
                                    }
                                });
                        AdsRenderingSettings adsRenderingSettings =
                                ImaSdkFactory.getInstance().createAdsRenderingSettings();
                        adsManager[0].init(adsRenderingSettings);
                    }
                });
        showLog("showVastTagAd:4");
        AdsRequest request = sdkFactory.createAdsRequest();
        request.setAdTagUrl(list.get(position).getVasttag_ur());
        request.setContentProgressProvider(
                () -> {
                    if (videoPlayer.getDuration() <= 0) {
                        return VideoProgressUpdate.VIDEO_TIME_NOT_READY;
                    }
                    return new VideoProgressUpdate(
                            videoPlayer.getCurrentPosition(), videoPlayer.getDuration());
                });

        // Request the ad. After the ad is loaded, onAdsManagerLoaded() will be called.
        adsLoader.requestAds(request);
        showLog("showVastTagAd:5");
    }

    private void showIntertitialsAds(InterstitialAd interstitialAd) {
        interstitialAd.show((Activity) context);

    }

    private void sendAppRatingSubmitData(String encrpt_data, int from, CallBackListener callBackListener) {
        EncritonRequestData encritonRequestData = new EncritonRequestData();
        encritonRequestData.setData(encrpt_data);
        JsonObject otp=new Gson().toJsonTree(encritonRequestData).getAsJsonObject();
        APIService apiService = RestApiClient.getClient().create(APIService.class);
        Call<JsonObject> apiResponse = apiService.submitRating("v3", otp);
        apiResponse.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                callBackListener.callBackOnResponse();
            }
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                callBackListener.callBackOnError();
                Toast.makeText(context, "Please try after some time ", Toast.LENGTH_SHORT).show();

            }
        });


    }

    public void showShareScreen(View convertView ){

        ImageView imageViewShare=convertView.findViewById(R.id.imageViewShare);
        ImageView imageViewWhatsapp=convertView.findViewById(R.id.imageViewWhatsapp);
        imageViewShare.setImageResource(R.drawable.ic_app_share_socail);
        imageViewWhatsapp.setImageResource(R.drawable.ic_whatsapp_home);
        imageViewWhatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if(appInstalledOrNot("com.whatsapp")){
                    sendIntent.setPackage("com.whatsapp");
                    sendIntent.putExtra(Intent.EXTRA_TEXT,
                            "Hey check out my app at: https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID);
                    sendIntent.setType("text/plain");
                    context.startActivity(sendIntent);
                }else{
                    Toast.makeText(context, "App is not installed", Toast.LENGTH_SHORT).show();
                }


            }
        });
    }

    public void showMagazineShareOptions(View convertView,int from){
        fromWhatsapp = false;
        final RelativeLayout relativeLayoutShareOpt = convertView.findViewById(R.id.relativeLayoutShareOpt);
        relativeLayoutShareOpt.setVisibility(VISIBLE);
    }

    private boolean appInstalledOrNot(String uri) {
        showLog("appInstalledOrNot:::::::");
        PackageManager pm = context.getPackageManager();
        try {
            return pm.getApplicationInfo(uri, 0).enabled;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return false;
    }



    private void displayFBNativeAd(View convertView, com.facebook.ads.NativeAd nativeAd, RelativeLayout adView, NativeAdLayout nativeAdLayout) {
        showLog("displayFBNativeAd");
        nativeAd.unregisterView();

        // Add the Ad view into the ad container.


        // Add the AdOptionsView
//        LinearLayout adChoicesContainer = convertView.findViewById(R.id.ad_choices_container);
//        AdOptionsView adOptionsView = new AdOptionsView(context, nativeAd, nativeAdLayout);
//        adChoicesContainer.removeAllViews();
//        adChoicesContainer.addView(adOptionsView, 0);

        LinearLayout adChoicesContainer = convertView.findViewById(R.id.ad_choices_container);
        AdOptionsView adOptionsView = new AdOptionsView(context, nativeAd, nativeAdLayout);
        adChoicesContainer.removeAllViews();
        adChoicesContainer.addView(adOptionsView, 0);

        // Create native UI using the ad metadata.
        com.facebook.ads.MediaView nativeAdIcon = adView.findViewById(R.id.native_ad_icon);
        TextView nativeAdTitle = adView.findViewById(R.id.native_ad_title);
        com.facebook.ads.MediaView nativeAdMedia = adView.findViewById(R.id.nativefb_ad_media);
        TextView nativeAdSocialContext = adView.findViewById(R.id.native_ad_social_context);
        TextView nativeAdBody = adView.findViewById(R.id.native_ad_body);
        TextView sponsoredLabel = adView.findViewById(R.id.native_ad_sponsored_label);
        Button nativeAdCallToAction = adView.findViewById(R.id.native_ad_call_to_action);
        nativeAdTitle.setText(nativeAd.getAdvertiserName());
        nativeAdBody.setText(nativeAd.getAdBodyText());
        nativeAdSocialContext.setText(nativeAd.getAdSocialContext());
        nativeAdCallToAction.setText(nativeAd.getAdCallToAction());
        showLog("nativeAd hasCallToAction:: "+nativeAd.hasCallToAction());
        nativeAdCallToAction.setVisibility(nativeAd.hasCallToAction() ? View.VISIBLE : View.INVISIBLE);
        sponsoredLabel.setText(nativeAd.getSponsoredTranslation());

        // Create a list of clickable views
        List<View> clickableViews = new ArrayList<>();
        clickableViews.add(nativeAdTitle);
        clickableViews.add(nativeAdCallToAction);


        // Register the Title and CTA button to listen for clicks.
        nativeAd.registerViewForInteraction(adView, nativeAdMedia, nativeAdIcon, clickableViews);


    }
//    private void displayFBNativeAd2(View convertView,com.facebook.ads.NativeAd nativeAd) {
//        showLog("displayFBNativeAd");
//
//       // if (nativeAd.getAdHeadline() != null && !nativeAd.getAdHeadline().equalsIgnoreCase("")) {
//            NativeAdLayout nativeAdLayout = convertView.findViewById(R.id.native_ad_container);
//            nativeAdLayout.setVisibility(View.VISIBLE);
//            RelativeLayout adViewFB = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.custom_fb_ad, nativeAdLayout, false);
//            nativeAdLayout.addView(adViewFB);
//        showLog("displayFBNativeAd layout added");
//            ImageView ad_choices_container = adViewFB.findViewById(R.id.ad_choices_container);
//            ad_choices_container.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Intent i = new Intent(Intent.ACTION_VIEW);
//                    i.setData(Uri.parse(nativeAd.getAdChoicesLinkUrl()));
//                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    context.startActivity(i);
//                }
//            });
//
//            imageLoader.displayImage(nativeAd.getAdChoicesImageUrl(),
//                    ad_choices_container, optionsAds, animationListener);
//        showLog("displayFBNativeAd adchoice added");
//            TextView nativeAdTitle = adViewFB.findViewById(R.id.native_ad_title);
//            com.facebook.ads.MediaView nativeAdMedia = adViewFB.findViewById(R.id.nativefb_ad_media);
//            TextView nativeAdBody = adViewFB.findViewById(R.id.native_ad_body);
//            TextView sponsoredLabel = adViewFB.findViewById(R.id.text_news_time_facebook);
//            TextView nativeAdCallToAction = adViewFB.findViewById(R.id.button_facebook_add_install);
//            // Set the Text.
//            nativeAdTitle.setText(nativeAd.getAdvertiserName());
//            nativeAdBody.setText(nativeAd.getAdBodyText());
//        showLog("displayFBNativeAd body added");
//            //nativeAdMedia.getLayoutParams().height = (heightImage + ((heightImage * 10) / 100));
//            nativeAdCallToAction.setVisibility(nativeAd.hasCallToAction() ? View.VISIBLE : View.INVISIBLE);
//            nativeAdCallToAction.setText(nativeAd.getAdCallToAction());
//            sponsoredLabel.setText(nativeAd.getSponsoredTranslation());
//        showLog("displayFBNativeAd getAdCallToAction added");
//            // Create a list of clickable views
//            List<View> clickableViews = new ArrayList<>();
//            clickableViews.add(nativeAdTitle);
//            clickableViews.add(nativeAdBody);
//            clickableViews.add(nativeAdMedia);
//            clickableViews.add(nativeAdCallToAction);
//        showLog("displayFBNativeAd click action added");
//            // Register the Title and CTA button to listen for clicks.
//            nativeAd.registerViewForInteraction(
//                    adViewFB,
//                    nativeAdMedia,
//                    clickableViews);
//        //}
//
//
//    }
    /**
     * Ads screen (Way2): Advertise → native (300dp) → Recommended News → 3 flash rows below.
     * Rows stay outside {@code fl_adplaceholder} / NativeAdView (AdMob-safe).
     */
    private void bindAdsScreenCompanion(@Nullable View convertView, int adPosition) {
        if (convertView == null) {
            return;
        }
        try {
            convertView.setTag(R.id.tag_ad_seat_feed_index, adPosition);
            bindAdsScreenTopFlashSlots(convertView);
            View title = convertView.findViewById(R.id.ad_recommended_title);
            TextView advertise = convertView.findViewById(R.id.ad_feed_advertise_link);
            if (advertise != null) {
                advertise.setOnClickListener(
                        v ->
                                ContactUsChooser.openAdvertiseOnAnyNewsWhatsApp(
                                        context,
                                        context.getString(R.string.ad_center_advertise_prefill)));
            }
            LinearLayout slotHost = convertView.findViewById(R.id.ad_recommended_list);
            if (slotHost == null) {
                return;
            }
            slotHost.removeAllViews();
            java.util.ArrayList<ModelClass.Datum> slots = collectAdsScreenFlashSlots(adPosition);
            if (slots.isEmpty()) {
                slotHost.setVisibility(GONE);
                if (title != null) {
                    title.setVisibility(GONE);
                }
                adsScreenOverlayPick.remove(adPosition);
                return;
            }
            if (title != null) {
                title.setVisibility(VISIBLE);
            }
            slotHost.setVisibility(VISIBLE);
            ModelClass.Datum selected = adsScreenOverlayPick.get(adPosition);
            if (!isAdsScreenOpenableCompanion(selected)) {
                selected = null;
                for (ModelClass.Datum probe : slots) {
                    if (isAdsScreenOpenableCompanion(probe)) {
                        selected = probe;
                        adsScreenOverlayPick.put(adPosition, probe);
                        break;
                    }
                }
            }
            LayoutInflater inf = LayoutInflater.from(context);
            for (int i = 0; i < slots.size(); i++) {
                final ModelClass.Datum item = slots.get(i);
                if (item == null || TextUtils.isEmpty(item.getTitle())) {
                    continue;
                }
                View row = inf.inflate(R.layout.item_feed_ad_recommended, slotHost, false);
                row.setTag(R.id.tag_ad_companion_datum, item);
                final boolean rowSelected =
                        selected != null
                                && item.getTitle() != null
                                && selected.getTitle() != null
                                && item.getTitle().equals(selected.getTitle());
                applyAdsScreenCompanionRowSelected(row, rowSelected);
                TextView rowTitle = row.findViewById(R.id.ad_rec_title);
                ImageView thumb = row.findViewById(R.id.ad_rec_thumb);
                if (rowTitle != null) {
                    rowTitle.setText(item.getTitle());
                }
                String thumbUrl = item.getThumbnail();
                if (TextUtils.isEmpty(thumbUrl)
                        && item.getImages() != null
                        && !item.getImages().isEmpty()) {
                    thumbUrl = item.getImages().get(0);
                }
                if (thumb != null) {
                    if (!TextUtils.isEmpty(thumbUrl)) {
                        try {
                            Glide.with(context).load(thumbUrl).centerCrop().into(thumb);
                        } catch (Exception ignored) {
                        }
                    } else {
                        thumb.setImageDrawable(null);
                    }
                }
                row.setOnClickListener(
                        v -> {
                            highlightAdsScreenCompanionRow(slotHost, row);
                            adsScreenOverlayPick.put(adPosition, item);
                            if (callbacks != null) {
                                callbacks.openAdScreenArticleOverlay(item);
                            }
                        });
                slotHost.addView(row);
            }
            if (slotHost.getChildCount() == 0) {
                slotHost.setVisibility(GONE);
            }
        } catch (Exception e) {
            showLog("bindAdsScreenCompanion: " + e.getMessage());
        }
    }

    /** Flash headline strip at top of ad page — reserved when empty. */
    private void bindAdsScreenTopFlashSlots(@Nullable View convertView) {
        if (convertView == null) {
            return;
        }
        LinearLayout top = convertView.findViewById(R.id.ad_top_flash_slots);
        if (top == null) {
            return;
        }
        top.removeAllViews();
        String[] heads = DailyUpdatesConfigHelper.flashHeadlines(context);
        boolean any = false;
        if (heads != null) {
            LayoutInflater inf = LayoutInflater.from(context);
            for (int i = 0; i < heads.length && i < 3; i++) {
                String h = heads[i] != null ? heads[i].trim() : "";
                if (TextUtils.isEmpty(h)) {
                    continue;
                }
                TextView row = new TextView(context);
                row.setText(h);
                row.setTextColor(0xFFE8E8E8);
                row.setTextSize(13f);
                row.setTypeface(row.getTypeface(), Typeface.BOLD);
                row.setMaxLines(2);
                row.setEllipsize(TextUtils.TruncateAt.END);
                LinearLayout.LayoutParams lp =
                        new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT);
                if (i > 0) {
                    lp.topMargin =
                            (int) (6 * context.getResources().getDisplayMetrics().density);
                }
                row.setLayoutParams(lp);
                top.addView(row);
                any = true;
            }
        }
        top.setVisibility(VISIBLE);
        if (!any) {
            top.setMinimumHeight(
                    (int) (56 * context.getResources().getDisplayMetrics().density));
        }
    }

    private static void applyAdsScreenCompanionRowSelected(@NonNull View row, boolean selected) {
        row.setBackgroundColor(selected ? 0xFF353535 : 0xFF252525);
    }

    private static void highlightAdsScreenCompanionRow(
            @NonNull LinearLayout slotHost, @NonNull View selectedRow) {
        for (int i = 0; i < slotHost.getChildCount(); i++) {
            View child = slotHost.getChildAt(i);
            applyAdsScreenCompanionRowSelected(child, child == selectedRow);
        }
    }

    private static boolean isAdsScreenOpenableCompanion(@Nullable ModelClass.Datum item) {
        if (item == null || TextUtils.isEmpty(item.getTitle())) {
            return false;
        }
        Long pid = item.getPostId();
        return pid != null && pid > 0L;
    }

    /**
     * Companion row for ad-seat flip-up / pill tap — selected row or first openable slot.
     * Does not splice or move the main FlipView index.
     */
    @Nullable
    public ModelClass.Datum getAdsScreenOverlayDatum(int adPosition) {
        ModelClass.Datum picked = adsScreenOverlayPick.get(adPosition);
        if (isAdsScreenOpenableCompanion(picked)) {
            return picked;
        }
        java.util.ArrayList<ModelClass.Datum> slots = collectAdsScreenFlashSlots(adPosition);
        for (ModelClass.Datum item : slots) {
            if (isAdsScreenOpenableCompanion(item)) {
                adsScreenOverlayPick.put(adPosition, item);
                return item;
            }
        }
        return null;
    }

    /**
     * Feed index for overlay — reuses the same {@link #getView(int, View, ViewGroup)} path as flip.
     */
    public int resolveFeedIndexForOverlay(@NonNull ModelClass.Datum datum) {
        if (list == null || list.isEmpty()) {
            return -1;
        }
        try {
            Long pid = datum.getPostId();
            if (pid != null && pid > 0L) {
                for (int i = 0; i < list.size(); i++) {
                    ModelClass.Datum d = list.get(i);
                    if (d != null && pid.equals(d.getPostId())) {
                        return i;
                    }
                }
            }
            String title = datum.getTitle();
            if (!TextUtils.isEmpty(title)) {
                ModelClass.Datum match = findInMemoryPostByTitle(title.trim(), -1);
                if (match != null) {
                    for (int i = 0; i < list.size(); i++) {
                        if (list.get(i) == match) {
                            return i;
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return -1;
    }

    /**
     * Full flip card for ad-screen companion tap — not the title-only overlay layout.
     */
    @Nullable
    public View createAdOverlayArticleView(@NonNull ViewGroup parent, @NonNull ModelClass.Datum datum) {
        // Use slide-up overlay fallback only — never rebind FlipView via getView().
        return null;
    }

    /**
     * Prefer splash flash headlines (already in prefs) enriched with in-memory feed
     * thumbs/ids. No extra List_All. Hide when empty.
     */
    @NonNull
    private java.util.ArrayList<ModelClass.Datum> collectAdsScreenFlashSlots(int adPosition) {
        java.util.ArrayList<ModelClass.Datum> out = new java.util.ArrayList<>();
        try {
            java.util.HashSet<Long> visibleIds = collectFeedPostIdsThroughSeat(adPosition);
            java.util.HashSet<Long> pickedIds = new java.util.HashSet<>();
            java.util.HashSet<String> flashTitles = new java.util.HashSet<>();
            String[] heads = DailyUpdatesConfigHelper.flashHeadlines(context);
            if (heads != null) {
                for (String head : heads) {
                    if (!TextUtils.isEmpty(head)) {
                        flashTitles.add(head.trim().toLowerCase(Locale.US));
                    }
                }
            }
            if (heads != null && heads.length > 0) {
                for (int i = 0; i < heads.length && out.size() < 3; i++) {
                    String h = heads[i] != null ? heads[i].trim() : "";
                    if (TextUtils.isEmpty(h)) {
                        continue;
                    }
                    ModelClass.Datum match = findInMemoryPostByTitle(h, adPosition);
                    if (match != null
                            && canUseAdsScreenFlashCompanion(match, visibleIds, pickedIds)) {
                        out.add(match);
                        rememberAdsScreenPickedId(pickedIds, match);
                    } else if (match == null) {
                        ModelClass.Datum stub = new ModelClass.Datum();
                        stub.setTitle(h);
                        out.add(stub);
                    }
                }
                if (!out.isEmpty()) {
                    return out;
                }
            }
            if (list == null || list.isEmpty()) {
                return out;
            }
            collectForwardAdsScreenFlashSlots(
                    adPosition, out, visibleIds, pickedIds, flashTitles, !flashTitles.isEmpty());
            if (out.size() < 3) {
                collectForwardAdsScreenFlashSlots(
                        adPosition, out, visibleIds, pickedIds, flashTitles, false);
            }
        } catch (Exception ignored) {
        }
        return out;
    }

    @NonNull
    private java.util.HashSet<Long> collectFeedPostIdsThroughSeat(int adPosition) {
        java.util.HashSet<Long> ids = new java.util.HashSet<>();
        if (list == null || list.isEmpty() || adPosition < 0) {
            return ids;
        }
        int last = Math.min(adPosition, list.size() - 1);
        for (int i = 0; i <= last; i++) {
            ModelClass.Datum d = list.get(i);
            if (d == null) {
                continue;
            }
            Long pid = d.getPostId();
            if (pid != null && pid > 0L) {
                ids.add(pid);
            }
        }
        return ids;
    }

    private void collectForwardAdsScreenFlashSlots(
            int adPosition,
            @NonNull java.util.ArrayList<ModelClass.Datum> out,
            @NonNull java.util.HashSet<Long> visibleIds,
            @NonNull java.util.HashSet<Long> pickedIds,
            @NonNull java.util.HashSet<String> flashTitles,
            boolean requireFlashTitle) {
        if (list == null) {
            return;
        }
        for (int i = adPosition + 1; i < list.size() && out.size() < 3; i++) {
            ModelClass.Datum d = list.get(i);
            if (!canUseAdsScreenFlashCompanion(d, visibleIds, pickedIds)) {
                continue;
            }
            if (requireFlashTitle && !flashTitles.isEmpty()) {
                String title = d.getTitle();
                if (TextUtils.isEmpty(title)
                        || !flashTitles.contains(title.trim().toLowerCase(Locale.US))) {
                    continue;
                }
            }
            out.add(d);
            rememberAdsScreenPickedId(pickedIds, d);
        }
    }

    private static boolean canUseAdsScreenFlashCompanion(
            @Nullable ModelClass.Datum d,
            @NonNull java.util.HashSet<Long> visibleIds,
            @NonNull java.util.HashSet<Long> pickedIds) {
        if (d == null || isNonContentFlipItem(d) || TextUtils.isEmpty(d.getTitle())) {
            return false;
        }
        Long pid = d.getPostId();
        if (pid != null && pid > 0L) {
            if (visibleIds.contains(pid) || pickedIds.contains(pid)) {
                return false;
            }
        }
        return true;
    }

    private static void rememberAdsScreenPickedId(
            @NonNull java.util.HashSet<Long> pickedIds, @NonNull ModelClass.Datum d) {
        Long pid = d.getPostId();
        if (pid != null && pid > 0L) {
            pickedIds.add(pid);
        }
    }

    @Nullable
    private ModelClass.Datum findInMemoryPostByTitle(@NonNull String title, int adPosition) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        String needle = title.trim();
        if (TextUtils.isEmpty(needle)) {
            return null;
        }
        for (int i = 0; i < list.size(); i++) {
            if (i == adPosition) {
                continue;
            }
            ModelClass.Datum d = list.get(i);
            if (d == null || isNonContentFlipItem(d)) {
                continue;
            }
            String t = d.getTitle();
            if (t != null && needle.equalsIgnoreCase(t.trim())) {
                return d;
            }
        }
        // Soft match: headline prefix / contains (ticker truncations).
        for (int i = 0; i < list.size(); i++) {
            if (i == adPosition) {
                continue;
            }
            ModelClass.Datum d = list.get(i);
            if (d == null || isNonContentFlipItem(d)) {
                continue;
            }
            String t = d.getTitle();
            if (t == null) {
                continue;
            }
            String hay = t.trim();
            if (hay.regionMatches(true, 0, needle, 0, Math.min(needle.length(), hay.length()))
                    || needle.regionMatches(true, 0, hay, 0, Math.min(hay.length(), needle.length()))) {
                return d;
            }
        }
        return null;
    }

    private static boolean isNonContentFlipItem(@Nullable ModelClass.Datum d) {
        if (d == null) {
            return true;
        }
        if (d.isAd_check()) {
            return true;
        }
        String pt = d.getPostType();
        if (pt == null) {
            return false;
        }
        String p = pt.trim();
        return p.equalsIgnoreCase("ad")
                || p.equalsIgnoreCase("ads")
                || p.equalsIgnoreCase("ratings")
                || p.equalsIgnoreCase("share")
                || p.equalsIgnoreCase("survey")
                || p.equalsIgnoreCase("polling");
    }

    private void displayGoogleFullNativeAd(ViewGroup parent, NativeAd ad,View myView,View convertView) {
        {
            // Inflate a layout and add it to the parent ViewGroup.
            if(ad!=null){
                showLog("AD #2");
                NativeAdView adView = (NativeAdView) myView;
                showLog("AD #3");
                MediaView mediaView = (MediaView) adView.findViewById(R.id.ad_media);
                adView.setMediaView(mediaView);
                showLog("AD #4");

                final ImageView iv_ad_view_blur = adView.findViewById(R.id.iv_ad_view_blur);
                iv_ad_view_blur.getLayoutParams().height=screenHeight;
                showLog("AD #8");
                List<NativeAd.Image> images = ad.getImages();
                if (images.size() > 0) {
                    Picasso picasso = new Picasso.Builder(context)
                            .memoryCache(new LruCache(24000))
                            .build();
                    picasso.get()
                            .load(images.get(0).getUri())
                            .transform(new BlurTransformation(context, 150, 1))
                            .into(iv_ad_view_blur);
                }
                showLog("AD #9");
                adView.setNativeAd(ad);
                parent.removeAllViews();
                // Place the AdView into the parent.[;
                parent.setVisibility(VISIBLE);
                parent.addView(adView);
                showLog("AD #10");
            }
        }
    }
    private boolean adHasOnlyStore(NativeAd nativeAd) {
        String store = nativeAd.getStore();
        String advertiser = nativeAd.getAdvertiser();
        return !TextUtils.isEmpty(store) && TextUtils.isEmpty(advertiser);
    }
    private void displayGoogleMediumNativeAd(ViewGroup parent, NativeAd ad,View myView,View convertView) {
        {
            try {
                nativeAd=ad;

                 NativeAdView nativeAdView;

                 TextView primaryView;
                 TextView secondaryView;
                 RatingBar ratingBar;
                 TextView tertiaryView;
                 ImageView iconView,iv_ad_view_blur;
                 MediaView mediaView;
                 Button callToActionView;
                 ConstraintLayout background;

                nativeAdView = (NativeAdView) myView.findViewById(R.id.native_ad_view);
                background = (ConstraintLayout) myView.findViewById(R.id.background);
             ConstraintLayout   background1 = (ConstraintLayout) myView.findViewById(R.id.backgroundBG);
             background1.getLayoutParams().height=screenHeight;

                mediaView = (MediaView) myView.findViewById(R.id.media_view);
                

                iv_ad_view_blur = (ImageView) myView.findViewById(R.id.iv_ad_view_blur);
                    primaryView = (TextView) myView.findViewById(R.id.primary);
                    secondaryView = (TextView) myView.findViewById(R.id.secondary);
                    tertiaryView = (TextView) myView.findViewById(R.id.textView_body_googleads);
                    ratingBar = (RatingBar) myView.findViewById(R.id.rating_bar);
                    ratingBar.setEnabled(false);
                    callToActionView = (Button) myView.findViewById(R.id.cta);
                    iconView = (ImageView) myView.findViewById(R.id.icon);
                    iv_ad_view_blur = (ImageView) myView.findViewById(R.id.iv_ad_view_blur);


                    String store = nativeAd.getStore();
                    Log.e("ADDDD","setNativeAd 1");
                    String advertiser = nativeAd.getAdvertiser();
                    String headline = nativeAd.getHeadline();
                    String body = nativeAd.getBody();
                    String cta = nativeAd.getCallToAction();
                    Double starRating = nativeAd.getStarRating();
                    NativeAd.Image icon = nativeAd.getIcon();
                    Log.e("ADDDD","setNativeAd 2");
                    String secondaryText;
                    nativeAdView.setCallToActionView(callToActionView);
                    nativeAdView.setHeadlineView(primaryView);
                    nativeAdView.setMediaView(mediaView);
                    secondaryView.setVisibility(VISIBLE);
                    Log.e("ADDDD","setNativeAd 3");
                    if (adHasOnlyStore(nativeAd)) {
                        nativeAdView.setStoreView(secondaryView);
                        secondaryText = store;
                    } else if (!TextUtils.isEmpty(advertiser)) {
                        nativeAdView.setAdvertiserView(secondaryView);
                        secondaryText = advertiser;
                    } else {
                        secondaryText = "";
                    }
                    primaryView.setText(headline);
                    callToActionView.setText(cta);
                    //  Set the secondary view to be the star rating if available.
                    if (starRating != null && starRating > 0) {
                        secondaryView.setVisibility(GONE);
                        ratingBar.setVisibility(VISIBLE);
                        ratingBar.setRating(starRating.floatValue());
                        nativeAdView.setStarRatingView(ratingBar);
                        Log.e("ADDDD","setNativeAd 4");
                    } else {
                        secondaryView.setText(secondaryText);
                        secondaryView.setVisibility(VISIBLE);
                        ratingBar.setVisibility(GONE);
                    }
                    if (icon != null) {
                        iconView.setVisibility(VISIBLE);
                        iconView.setImageDrawable(icon.getDrawable());
                        Log.e("ADDDD","setNativeAd 5");
                    } else {
                        iconView.setVisibility(GONE);
                    }
                    if (tertiaryView != null) {
                        tertiaryView.setText(body);
                        nativeAdView.setBodyView(tertiaryView);
                        Log.e("ADDDD","setNativeAd 6");
                    }


                if(nativeAd.getImages()!=null && nativeAd.getImages().size()>0){
                    Log.e("ADDDD","setNativeAd 7");
                    List<NativeAd.Image> images = nativeAd.getImages();
                    if (images.size() > 0) {
                        Picasso picasso = new Picasso.Builder(context)
                                .memoryCache(new LruCache(24000))
                                .build();
                        picasso.get()
                                .load(images.get(0).getUri())
                                .transform(new BlurTransformation(context, 150, 1))
                                .into(iv_ad_view_blur);
                    }
                }
                Log.e("ADDDD","setNativeAd 8");
                nativeAdView.setNativeAd(nativeAd);
                Log.e("ADDDD","setNativeAd 9");

                    // Ensure that the parent view doesn't already contain an ad view.
                    parent.removeAllViews();
                    // Place the AdView into the parent.[;
                    parent.setVisibility(VISIBLE);
                    parent.addView(nativeAdView);
                    showLog("AD #10");


            }catch (Exception e){
                e.printStackTrace();
            }
            // Inflate a layout and add it to the parent ViewGroup.
        }
    }
    private void displayGoogleNativeAd(ViewGroup parent, NativeAd ad,View myView,View convertView) {
        try {
            if(ad!=null){
                showLog("AD #2");
                NativeAdView adView = (NativeAdView) myView;
                showLog("AD #3");
                MediaView mediaView = (MediaView) adView.findViewById(R.id.ad_media);
                adView.setMediaView(mediaView);
                showLog("AD #4");
                // mediaView.setImageScaleType(ImageView.ScaleType.CENTER);

                final ImageView iv_ad_view_blur = adView.findViewById(R.id.iv_ad_view_blur);
                showLog("AD #6");
                // mediaView.getLayoutParams().height = (heightImage + ((heightImage * 10) / 100));
                //  iv_ad_view_blur.getLayoutParams().height = (heightImage + ((heightImage * 10) / 100));

                adView.setHeadlineView(adView.findViewById(R.id.textView_title_googleads));
                adView.setBodyView(adView.findViewById(R.id.textView_body_googleads));
                adView.setCallToActionView(adView.findViewById(R.id.button_googlead));
                adView.setIconView(adView.findViewById(R.id.adImage_log));
                adView.setAdvertiserView(adView.findViewById(R.id.textView17_main));
                adView.setStarRatingView(adView.findViewById(R.id.ratingBar_google));
                showLog("AD #7");

                ((TextView) adView.getHeadlineView()).setText(ad.getHeadline());
                showLog("AD #8");
                ((TextView) adView.getAdvertiserView()).setText(ad.getAdvertiser());
                showLog("AD #9");
                ((TextView) adView.getBodyView()).setText(ad.getBody());
                showLog("AD #10");
                if(ad.getIcon()!=null && ad.getIcon().getDrawable()!=null){
                    ((ImageView) adView.getIconView()).setImageDrawable(ad.getIcon().getDrawable());
                }else{
                    ((ImageView) adView.getIconView()).setVisibility(GONE);
                }

                Double starRating = nativeAd.getStarRating();
                if (starRating != null && starRating > 0) {
                    ((RatingBar)adView.getStarRatingView()).setRating(ad.getStarRating().floatValue());
                    Log.e("ADDDD","setNativeAd 4");
                }
                showLog("AD #8");
                List<NativeAd.Image> images = ad.getImages();
                if (images.size() > 0) {
                    Picasso picasso = new Picasso.Builder(context)
                            .memoryCache(new LruCache(24000))
                            .build();
                    picasso.get()
                            .load(images.get(0).getUri())
                            .transform(new BlurTransformation(context, 150, 1))
                            .into(iv_ad_view_blur);
                }
                showLog("AD #9");
                ((TextView) adView.getCallToActionView()).setText(ad.getCallToAction());
                // Call the NativeAdView's setNativeAd method to register the
                // NativeAdObject.
                adView.setNativeAd(ad);
                // Ensure that the parent view doesn't already contain an ad view.
                parent.removeAllViews();
                // Place the AdView into the parent.[;
                parent.setVisibility(VISIBLE);
                parent.addView(adView);
                showLog("AD #10");
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        // Inflate a layout and add it to the parent ViewGroup.
    }
//    private void displayGoogleNativeMediumAd(int postPosition, NativeAd ad,View convertView) {
//        ColorDrawable cd = new ColorDrawable(0xFFFFF);
//        NativeTemplateStyle styles = new
//                NativeTemplateStyle.Builder().withMainBackgroundColor(cd).build();
//        TemplateView adTemplate = convertView.findViewById(R.id.regularTemplate);
//        if (list.get(postPosition).getAd_view_type().equalsIgnoreCase(Constants.AD_FULL_IMAGE)) {
//            adTemplate = convertView.findViewById(R.id.fullTemplate);
//        } else if (list.get(postPosition).getAd_view_type().equalsIgnoreCase(Constants.AD_MEDIUM)) {
//            adTemplate = convertView.findViewById(R.id.mediumTemplate);
//        } else {
//            adTemplate = convertView.findViewById(R.id.regularTemplate);
//        }
//        adTemplate.setVisibility(VISIBLE);
//        adTemplate.setStyles(styles);
//        adTemplate.setNativeAd(ad);
//    }


    public void closeGalleryShareLayout(View finalConvertView ){
        imageGallaryHolder.relativeLayoutShareOpt = finalConvertView.findViewById(R.id.relativeLayoutShareOpt);
        imageGallaryHolder.image_gallary_share = finalConvertView.findViewById(R.id.image_gallary_share);
        imageGallaryHolder.dotIndicatior = finalConvertView.findViewById(R.id.dotIndicatior);

        imageGallaryHolder.relativeLayoutShareOpt.setVisibility(GONE);
        imageGallaryHolder.image_gallary_share.setVisibility(VISIBLE);
        imageGallaryHolder.dotIndicatior.setVisibility(VISIBLE);
    }

    public void shareGalleryScreenShotUI(View finalConvertView) {
        fromWhatsapp=true;
        imageGallaryHolder.relativeLayoutShareOpt = finalConvertView.findViewById(R.id.relativeLayoutShareOpt);
        imageGallaryHolder.image_gallary_share = finalConvertView.findViewById(R.id.image_gallary_share);
        imageGallaryHolder.dotIndicatior = finalConvertView.findViewById(R.id.dotIndicatior);

        imageGallaryHolder.relativeLayoutShareOpt.setVisibility(VISIBLE);
        imageGallaryHolder.image_gallary_share.setVisibility(GONE);
        imageGallaryHolder.dotIndicatior.setVisibility(GONE);

    }
    public void shareGalleryScreenShot(int position,int from,View customConvertView) {
        RelativeLayout scrren_icon = customConvertView.findViewById(R.id.scrren_icon);
        imageGallaryHolder.relativeLayoutShareOpt.setVisibility(GONE);
        imageGallaryHolder.dotIndicatior.setVisibility(VISIBLE);
        imageGallaryHolder.image_gallary_scrip.setVisibility(View.GONE);
        String appLink = "https://com.anyNews.anynews/";

        String mInvitationUrl = list.get(position).getShareLink();
        String msg = list.get(position).getTitle() + " "
                + mInvitationUrl;
        countDownTimer = new CountDownTimer(2, 2) {

            public void onTick(long millisUntilFinished) {

            }
            public void onFinish() {

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(Intent.EXTRA_SUBJECT, mInvitationUrl.toString());
                intent.putExtra(Intent.EXTRA_TEXT, msg);
                if(from==1){
                    intent.setType("text/plain");
                    intent.setPackage("com.whatsapp");
                }

                Bitmap b = ScreenShott.getInstance().takeScreenShotOfView(scrren_icon);

                //  layout_strip_iconnew.setVisibility(View.GONE);
                try {
                    File file = new File(context.getExternalCacheDir(), "sample.png");
                    FileOutputStream fOut = new FileOutputStream(file);
                    b.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                    fOut.flush();
                    fOut.close();
                    file.setReadable(true, true);
                    Uri photoURI = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
                    showLog("file URI:"+photoURI);
                    intent.putExtra(Intent.EXTRA_STREAM, photoURI);
                    List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(Intent.createChooser(intent, "Share Link"), PackageManager.MATCH_DEFAULT_ONLY);
                    for (ResolveInfo resolveInfo : resInfoList) {
                        String packageName = resolveInfo.activityInfo.packageName;
                        context.grantUriPermission(packageName, photoURI, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                imageGallaryHolder.image_gallary_scrip.setVisibility(View.GONE);
                imageGallaryHolder.image_gallary_share.setVisibility(VISIBLE);
                intent.setType("image/*");
                if (context != null && context.getPackageName().equals(BuildConfig.APPLICATION_ID)) {
                    context.startActivity(Intent.createChooser(intent, "Share Link"));
                }


                Share_Count_Value("share", list.get(position).getPostId());
            }
        }.start();


    }
    public void shareMagazineScreenShot(ImageView imageViewShare, RelativeLayout relativeLayoutShareOpt, int position,RelativeLayout magzine_relative_slider) {

        relativeLayoutShareOpt.setVisibility(GONE);
        image_magazineHolder.magzine_ll_dots.setVisibility(GONE);
        String appLink = "https://com.anyNews.anynews/";
        imageViewShare.setVisibility(GONE);

        String mInvitationUrl = list.get(position).getShareLink();
        String msg = list.get(position).getTitle() + " "
                + mInvitationUrl;
        countDownTimer = new CountDownTimer(2, 2) {

            public void onTick(long millisUntilFinished) {

            }
            public void onFinish() {

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, mInvitationUrl.toString());
                intent.putExtra(Intent.EXTRA_TEXT, msg);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                Bitmap b = ScreenShott.getInstance().takeScreenShotOfView(magzine_relative_slider);

                //  layout_strip_iconnew.setVisibility(View.GONE);
                try {
                    File file = new File(context.getExternalCacheDir(), "sample.png");
                    FileOutputStream fOut = new FileOutputStream(file);
                    b.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                    fOut.flush();
                    fOut.close();
                    file.setReadable(true, true);
                    Uri photoURI = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
                    showLog("file URI:"+photoURI);
                    intent.putExtra(Intent.EXTRA_STREAM, photoURI);
                    List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(Intent.createChooser(intent, "Share Link"), PackageManager.MATCH_DEFAULT_ONLY);
                    for (ResolveInfo resolveInfo : resInfoList) {
                        String packageName = resolveInfo.activityInfo.packageName;
                        context.grantUriPermission(packageName, photoURI, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            //    imageGallaryHolder.image_gallary_scrip.setVisibility(View.GONE);
              //  imageGallaryHolder.image_gallary_share.setVisibility(VISIBLE);
                image_magazineHolder.magzine_ll_dots.setVisibility(VISIBLE);
                imageViewShare.setVisibility(VISIBLE);
                intent.setType("image/*");
                if (context != null && context.getPackageName().equals(BuildConfig.APPLICATION_ID)) {
                    context.startActivity(Intent.createChooser(intent, "Share Link"));
                }
                fromWhatsapp=false;


                Share_Count_Value("share", list.get(position).getPostId());
            }
        }.start();


    }

    private void showFullVideoWithYoutube(int position, View convertView) {
        ImageView newFullImageView1 = convertView.findViewById(R.id.imageViewItem1);
        RelativeLayout layoutMain = convertView.findViewById(R.id.parent_layout);
        layoutMain.getLayoutParams().height=screenHeight;
        fullVideoNewYoutubeViewHolder.full_video_like = convertView.findViewById(R.id.full_video_like_text);
        fullVideoNewYoutubeViewHolder.full_video_dislike = convertView.findViewById(R.id.full_video_dislike_text);
        fullVideoNewYoutubeViewHolder.full_video_share = convertView.findViewById(R.id.full_video_share_text);
        fullVideoNewYoutubeViewHolder.full_video_comment = convertView.findViewById(R.id.full_video_comment_text);
        fullVideoNewYoutubeViewHolder.like_for_video_image = convertView.findViewById(R.id.like_for_video_image);
        fullVideoNewYoutubeViewHolder.dislike_for_video_image = convertView.findViewById(R.id.dislike_for_video_image);
        fullVideoNewYoutubeViewHolder.btnMuteFull = convertView.findViewById(R.id.btnMute);
        fullVideoNewYoutubeViewHolder.user_pic = convertView.findViewById(R.id.user_pic);
        fullVideoNewYoutubeViewHolder.side_menu = convertView.findViewById(R.id.video_side_menu);
        fullVideoNewYoutubeViewHolder.full_image_whatsapp_bg = convertView.findViewById(R.id.full_image_whatsapp_bg);
        final LinearLayout like_for_video_ll = convertView.findViewById(R.id.like_for_video);
        final LinearLayout dislike_for_video_ll = convertView.findViewById(R.id.dislike_for_video);
        final LinearLayout comment_for_video_ll = convertView.findViewById(R.id.comment_for_video);
        final LinearLayout share_for_video_ll = convertView.findViewById(R.id.share_for_video);
        final LinearLayout full_image_whatsapp = convertView.findViewById(R.id.full_image_whatsapp);
        fullVideoNewYoutubeViewHolder.full_image_whatsapp_bg.setImageResource(R.drawable.ic_whatsapp_original1);
        bindFullVideoEngagementCounts(
                fullVideoNewYoutubeViewHolder.full_video_like,
                fullVideoNewYoutubeViewHolder.full_video_comment,
                fullVideoNewYoutubeViewHolder.full_video_share,
                convertView.findViewById(R.id.full_img_whtsapp_text),
                position);
        final ImageView imageViewYoutubePlayVideo = convertView.findViewById(R.id.imageViewPlayVideo);
        RelativeLayout layoutYoutubeFullView = convertView.findViewById(R.id.layoutYoutubeFullView);
        final YouTubePlayerView youTubePlayerView = convertView.findViewById(R.id.youtube_full_video);
        final RelativeLayout parent_layout = convertView.findViewById(R.id.parent_layout);
        final CircleImageView user_pic = convertView.findViewById(R.id.user_pic);
        final ImageView like_for_video_image = convertView.findViewById(R.id.like_for_video_image);
        final ImageView iv_comment_for_video = convertView.findViewById(R.id.iv_comment_for_video);
        user_pic.setImageResource(R.drawable.ic_user_profile);
        like_for_video_image.setImageResource(R.drawable.ic_like_half1);
        iv_comment_for_video.setImageResource(R.drawable.chatbox);

        ((Activity) convertView.getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        newFullImageView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fullVideoNewYoutubeViewHolder.side_menu.isShown() || fullVideoNewYoutubeViewHolder.side_menu.getVisibility() == View.VISIBLE) {
                    fullVideoNewYoutubeViewHolder.side_menu = convertView.findViewById(R.id.video_side_menu);
                    fullVideoNewYoutubeViewHolder.side_menu.setVisibility(View.INVISIBLE);
                    showLog("fullVideoNewYoutubeViewHolder "+ "click gone");
                } else {
                    fullVideoNewYoutubeViewHolder.side_menu = convertView.findViewById(R.id.video_side_menu);
                    fullVideoNewYoutubeViewHolder.side_menu.setVisibility(View.VISIBLE);
                    showLog("fullVideoNewYoutubeViewHolder "+ "click visible");

                }
            }
        });

        fullVideoNewYoutubeViewHolder.user_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openWriterProfileFromFeed(position);

            }
        });
        isFullYoutubeVideoIsInitialized=false;

        if (list.get(position).getTitle() != null)

            if (list.get(position).getVideoUrl() != null && isValidContextForGlide(context)) {
                Glide.with(convertView.getContext())
                        .load(list.get(position).getThumbnail())
                        .into(newFullImageView1);
            }
        if (!isFullYoutubeVideoIsInitialized) {
            newFullImageView1.setVisibility(View.VISIBLE);
            imageViewYoutubePlayVideo.setImageResource(R.drawable.ic_way2_play_circle);
            imageViewYoutubePlayVideo.setVisibility(View.VISIBLE);
        }

        if (list.get(position).getLikeStatus() != null && list.get(position).getLikeStatus() == 1) {
            fullVideoNewYoutubeViewHolder.like_for_video_image.setImageResource(ic_like_half_selected1);
        } else {
            fullVideoNewYoutubeViewHolder.like_for_video_image.setImageResource(ic_like_half1);
        }

        View finalConvertViewFull = convertView;
        imageViewYoutubePlayVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fullVideoNewYoutubeViewHolder.side_menu = finalConvertViewFull.findViewById(R.id.video_side_menu);
                fullVideoNewYoutubeViewHolder.side_menu.setVisibility(View.INVISIBLE);
                if (true) {
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            imageViewYoutubePlayVideo.setVisibility(GONE);
                            newFullImageView1.setVisibility(GONE);
                            isFullYoutubeVideoIsInitialized=true;
                            youTubePlayerView.setVisibility(View.VISIBLE);
                            View v = youTubePlayerView.inflateCustomPlayerUi(R.layout.youtube_custom_player_ui);
                            YouTubePlayerListener listener = new com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener() {
                                @Override
                                public void onReady(@NonNull com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer youTubePlayer) {
                                    fullVideocustomPlayerUiController = new CustomPlayerUiController(context, v, youTubePlayer, youTubePlayerView, layoutHeight, false,new CustomPlayerUiController.ShowFullScreenVideo() {
                                        @Override
                                        public void showFullScreenVideo(boolean b) {
                                                showLog("panel clicked open");
//                                                if (fullVideoNewYoutubeViewHolder.side_menu.isShown() || fullVideoNewYoutubeViewHolder.side_menu.getVisibility() == View.VISIBLE) {
//                                                    fullVideoNewYoutubeViewHolder.side_menu = finalConvertViewFull.findViewById(R.id.video_side_menu);
//                                                    fullVideoNewYoutubeViewHolder.side_menu.setVisibility(View.INVISIBLE);
//                                                    showLog("fullimageadapter"+ "click gone");
//                                                } else {
//                                                    fullVideoNewYoutubeViewHolder.side_menu = finalConvertViewFull.findViewById(R.id.video_side_menu);
//                                                    fullVideoNewYoutubeViewHolder.side_menu.setVisibility(View.VISIBLE);
//                                                    showLog("fullimageadapter"+ "click visible");
//
//                                                }
                                        }

                                        @Override
                                        public void showUIControls(boolean b) {
                                            if(b){
                                                fullVideoNewYoutubeViewHolder.side_menu = finalConvertViewFull.findViewById(R.id.video_side_menu);
                                                fullVideoNewYoutubeViewHolder.side_menu.setVisibility(VISIBLE);
                                            }else{
                                                fullVideoNewYoutubeViewHolder.side_menu = finalConvertViewFull.findViewById(R.id.video_side_menu);
                                                fullVideoNewYoutubeViewHolder.side_menu.setVisibility(View.INVISIBLE);
                                                showLog("fullimageadapter"+ "click visible");

                                            }

                                        }

                                        @Override
                                        public void showProgressOnMainPage(boolean b) {
                                        }
                                    });


                                    youTubePlayer.addListener(fullVideocustomPlayerUiController);
                                    youTubePlayerViewHalf = youTubePlayer;
                                    YouTubePlayerUtils.loadOrCueVideo(
                                            youTubePlayer, lifecycle,
                                            list.get(position).getVideoUrl(), 0f
                                    );

                                }
                            };
                            IFramePlayerOptions options = new IFramePlayerOptions.Builder(context).controls(0).build();
                            youTubePlayerView.initialize(listener, false, options);

                        }
                    }, 100);
                }

            }
        });




        like_for_video_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fullVideoNewYoutubeViewHolder.like_for_video_image = convertView.findViewById(R.id.like_for_video_image);
                int newStatus = applyLikeChange(position, null);
                if (newStatus == 1) fullVideoNewYoutubeViewHolder.like_for_video_image.setImageResource(ic_like_half_selected1);
                else fullVideoNewYoutubeViewHolder.like_for_video_image.setImageResource(ic_like_half1);
            }
        });

        // Dislike removed from app.
        if (dislike_for_video_ll != null) dislike_for_video_ll.setVisibility(View.GONE);

        comment_for_video_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent("COMMENTS_PAGE");
                intent.putExtra("message", "click_event");

                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                try {
                    adapterCallback.OpenComments(list.get(position).getPostId(), list.get(position).getSubPostId());
                } catch (ClassCastException e) {
                    e.printStackTrace();
                    // do something
                }

            }
        });
        full_image_whatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareVideoDirect(position, VideoShareHelper.ShareMode.WHATSAPP_STATUS);

            }
        });
//        share_for_video_ll.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                shareYoutubePlayerFullVideoShare(position,0,newFullImageView1,youTubePlayerView,layoutYoutubeFullView,imageViewYoutubePlayVideo,fullVideoNewYoutubeViewHolder.side_menu);
//            }
//        });

        final RelativeLayout relativeLayoutShareOpt = convertView.findViewById(R.id.relativeLayoutShareOpt);
        final  TextView textViewAllPage = convertView.findViewById(R.id.textViewAllPage);
        final  TextView textViewSinglePage = convertView.findViewById(R.id.textViewSinglePage);
        final ImageView  imageViewShareClose = convertView.findViewById(R.id.imageViewShareClose);

        final ImageView  imageViewShareSingle = convertView.findViewById(R.id.imageViewShareSingle);
        final ImageView  imageViewShareAll = convertView.findViewById(R.id.imageViewShareAll);
        imageViewShareSingle.setImageResource(R.drawable.ic_share_download);
        imageViewShareAll.setImageResource(R.drawable.ic_share_all);

        textViewSinglePage.setText("Share Article");
        textViewAllPage.setText("Share Video");

        //imageGallaryHolder.galleryLayout.setLayerType(View.LAYER_TYPE_NONE,null);


        share_for_video_ll.setOnClickListener(view -> {
            if(relativeLayoutShareOpt.getVisibility()==VISIBLE){
                relativeLayoutShareOpt.setVisibility(GONE);
            }else {
                relativeLayoutShareOpt.setVisibility(VISIBLE);
            }
        });

        relativeLayoutShareOpt.setVisibility(GONE);
        imageViewShareClose.setOnClickListener(view -> relativeLayoutShareOpt.setVisibility(GONE));
        textViewAllPage.setOnClickListener(view -> {
            relativeLayoutShareOpt.setVisibility(GONE);
            shareVideoDirect(position, VideoShareHelper.ShareMode.GENERAL);
        });
        textViewSinglePage.setOnClickListener(view -> {
            relativeLayoutShareOpt.setVisibility(GONE);
            shareYoutubePlayerFullVideoShare(position,0,newFullImageView1,youTubePlayerView,layoutYoutubeFullView,imageViewYoutubePlayVideo,fullVideoNewYoutubeViewHolder.side_menu);
        });




    }

    public void shareYoutubePlayerFullVideoShare(int position, int from, ImageView newFullImageView1, YouTubePlayerView youTubePlayerView, RelativeLayout layoutYoutubeFullView, ImageView imageViewYoutubePlayVideo, LinearLayout side_menu) {

        String mInvitationUrl = list.get(position).getShareLink();
        String msg = list.get(position).getTitle() + " " + mInvitationUrl;
        side_menu.setVisibility(View.INVISIBLE);
        youTubePlayerView.setVisibility(GONE);
        newFullImageView1.setVisibility(VISIBLE);
        imageViewYoutubePlayVideo.setVisibility(VISIBLE);
        countDownTimer = new CountDownTimer(2, 2) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, mInvitationUrl.toString());
                intent.putExtra(Intent.EXTRA_TEXT, msg);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (from == 1) {
                    intent.setType("text/plain");
                    intent.setPackage("com.whatsapp");
                }

                Bitmap b = ScreenShott.getInstance().takeScreenShotOfView(layoutYoutubeFullView);
                //bitmapResize(b);


                try {
                    File file = new File(context.getExternalCacheDir(), "sample.png");

                    FileOutputStream fOut = new FileOutputStream(file);

                    b.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                    fOut.flush();
                    fOut.close();
                    file.setReadable(true, true);
                    Uri photoURI = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
                    intent.putExtra(Intent.EXTRA_STREAM, photoURI);
                    List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(Intent.createChooser(intent, "Share Link"), PackageManager.MATCH_DEFAULT_ONLY);
                    for (ResolveInfo resolveInfo : resInfoList) {
                        String packageName = resolveInfo.activityInfo.packageName;
                        context.grantUriPermission(packageName, photoURI, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                intent.setType("image/*");
                if (context != null && context.getPackageName().equals(BuildConfig.APPLICATION_ID)) {
                    context.startActivity(Intent.createChooser(intent, "Share Link"));
                }


                    if (isFullYoutubeVideoIsInitialized) {
                        youTubePlayerViewHalf.pause();
                        youTubePlayerView.setVisibility(VISIBLE);
                        newFullImageView1.setVisibility(View.GONE);
                        imageViewYoutubePlayVideo.setVisibility(View.GONE);
                    } else {
                        newFullImageView1.setVisibility(VISIBLE);
                        imageViewYoutubePlayVideo.setVisibility(VISIBLE);

                    }

                Share_Count_Value("share", list.get(position).getPostId());

            }
        }.start();

    }

    private void showFullVideoWithExoPlayer(int position, View convertView) {
        RelativeLayout layoutMain = convertView.findViewById(R.id.parent_layout);
        layoutMain.getLayoutParams().height=screenHeight;
        full_videoViewHolder.full_video_like = convertView.findViewById(R.id.full_video_like_text);
        full_videoViewHolder.full_video_dislike = convertView.findViewById(R.id.full_video_dislike_text);
        full_videoViewHolder.full_video_share = convertView.findViewById(R.id.full_video_share_text);
        full_videoViewHolder.full_video_comment = convertView.findViewById(R.id.full_video_comment_text);
        full_videoViewHolder.exo_play_layout = convertView.findViewById(R.id.exo_play_layout);

        PlayerView simpleExoPlayerViewFull  = convertView.findViewById(R.id.normal_video_player);
        full_videoViewHolder.progress_bar_for_video = convertView.findViewById(R.id.progress_bar_for_video);

        full_videoViewHolder.like_for_video_ll = convertView.findViewById(R.id.like_for_video);
        full_videoViewHolder.side_menu = convertView.findViewById(R.id.video_side_menu);
        full_videoViewHolder.dislike_for_video_ll = convertView.findViewById(R.id.dislike_for_video);
        full_videoViewHolder.comment_for_video_ll = convertView.findViewById(R.id.comment_for_video);
        full_videoViewHolder.share_for_video_ll = convertView.findViewById(R.id.share_for_video);
        full_videoViewHolder.full_image_whatsapp_bg = convertView.findViewById(R.id.full_image_whatsapp_bg);
        full_videoViewHolder.user_pic = convertView.findViewById(R.id.user_pic);
        full_videoViewHolder.like_for_video_image = convertView.findViewById(R.id.like_for_video_image);
        bindFullVideoEngagementCounts(
                full_videoViewHolder.full_video_like,
                full_videoViewHolder.full_video_comment,
                full_videoViewHolder.full_video_share,
                convertView.findViewById(R.id.full_img_whtsapp_text),
                position);

        full_videoViewHolder.full_image_whatsapp_bg.setImageResource(R.drawable.ic_whatsapp_original1);

        final ImageView iv_comment_for_video = convertView.findViewById(R.id.iv_comment_for_video);
        final ImageView iv_share_for_video = convertView.findViewById(R.id.iv_share_for_video);
        final ImageView like_for_video_image = convertView.findViewById(R.id.like_for_video_image);
        final CircleImageView user_pic = convertView.findViewById(R.id.user_pic);
        iv_comment_for_video.setImageResource(R.drawable.chatbox);
        iv_share_for_video.setImageResource(R.drawable.ic_share_full);
        like_for_video_image.setImageResource(R.drawable.ic_like_half1);
        user_pic.setImageResource(R.drawable.ic_user_profile);
        final RelativeLayout exo_play_layout = convertView.findViewById(R.id.exo_play_layout);
        final ImageView thaumbnail_for_video = convertView.findViewById(R.id.thaumbnail_for_video);
        final ImageView imageViewExoVideoPlayVideo = convertView.findViewById(R.id.imageViewVideoPlayVideo);

        final ImageButton fullscreen_button = convertView.findViewById(R.id.fullscreen_button);
        final ImageButton mute_button = convertView.findViewById(R.id.mute_button);
        final ProgressBar progress_bar_for_video = convertView.findViewById(R.id.progress_bar_for_video);
        // XML defaults thaumbnail_for_video to GONE — must show poster before first play (fat setFeedExoPosterCover).
        boolean fullExoLive = simpleExoPlayerViewFull.getPlayer() != null
                && simpleExoPlayerViewFull.getPlayer() == exoPlayer
                && exoPlayer != null
                && exoPlayer.getPlayWhenReady()
                && exoPlayer.getPlaybackState() != Player.STATE_ENDED
                && exoPlayer.getPlaybackState() != Player.STATE_IDLE;
        if (fullExoLive) {
            simpleExoPlayerViewFull.setVisibility(View.VISIBLE);
            thaumbnail_for_video.setVisibility(View.GONE);
            imageViewExoVideoPlayVideo.setVisibility(View.GONE);
            updateLeanExoCenterControlIcons(simpleExoPlayerViewFull, true);
        } else {
            if (simpleExoPlayerViewFull.getPlayer() != null && simpleExoPlayerViewFull.getPlayer() != exoPlayer) {
                try {
                    simpleExoPlayerViewFull.getPlayer().release();
                } catch (Exception ignored) {
                }
                simpleExoPlayerViewFull.setPlayer(null);
            }
            simpleExoPlayerViewFull.setVisibility(View.GONE);
            showLeanExoIdlePoster(thaumbnail_for_video, imageViewExoVideoPlayVideo,
                    StatusVideoMedia.resolveThumbnailUrl(list.get(position)));
            if (progress_bar_for_video != null) {
                progress_bar_for_video.setVisibility(View.GONE);
            }
        }
        fullscreen_button.setTag("force_gone");
        fullscreen_button.setVisibility(GONE);
        mute_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(exoPlayer!=null){
                    showLog("exoplayer volume :"+exoPlayer.getVolume());
                    exoPlayer.setVolume(exoPlayer.getVolume()==0f? 1f:0f);
                    if(exoPlayer.getVolume()==0f){
                        mute_button.setImageResource(R.drawable.ic_exo_volume_down);
                    }else{
                        mute_button.setImageResource(R.drawable.ic_volume_on);
                    }
                }
            }
        });
        full_videoViewHolder.side_menu = convertView.findViewById(R.id.video_side_menu);
        full_videoViewHolder.side_menu.setVisibility(View.VISIBLE);
        showLog("fullimageadapter "+ "click visible");
        View finalConvertViewFull = convertView;
        full_videoViewHolder.user_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openWriterProfileFromFeed(position);
            }
        });
        if (list.get(position).getLikeStatus() != null && list.get(position).getLikeStatus() == 1) {
            full_videoViewHolder.like_for_video_image.setImageResource(ic_like_half_selected1);
        } else {
            full_videoViewHolder.like_for_video_image.setImageResource(ic_like_half1);
        }
        if (full_videoViewHolder.dislike_for_video_ll != null) full_videoViewHolder.dislike_for_video_ll.setVisibility(View.GONE);

        imageViewExoVideoPlayVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fullscreen = false;
                mute_button.setImageResource(ic__volume_up);
                String videoUrl = StatusVideoMedia.resolveStreamUrl(list.get(position));
                if (TextUtils.isEmpty(videoUrl)) {
                    videoUrl = list.get(position).getVideoUrl();
                }
                if (TextUtils.isEmpty(videoUrl)) {
                    Toast.makeText(context, "Video is not available", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Resume paused player on this card — do not rebuild (avoids black flash / stuck chrome).
                if (exoPlayer != null
                        && simpleExoPlayerViewFull.getPlayer() == exoPlayer
                        && exoPlayer.getPlaybackState() != Player.STATE_ENDED
                        && exoPlayer.getPlaybackState() != Player.STATE_IDLE) {
                    imageViewExoVideoPlayVideo.setVisibility(View.GONE);
                    thaumbnail_for_video.setVisibility(View.GONE);
                    progress_bar_for_video.setVisibility(View.GONE);
                    simpleExoPlayerViewFull.setVisibility(View.VISIBLE);
                    exoPlayer.setPlayWhenReady(true);
                    try {
                        exoPlayer.play();
                    } catch (Exception ignored) {
                    }
                    updateLeanExoCenterControlIcons(simpleExoPlayerViewFull, true);
                    full_videoViewHolder.side_menu = finalConvertViewFull.findViewById(R.id.video_side_menu);
                    if (full_videoViewHolder.side_menu != null) {
                        full_videoViewHolder.side_menu.setVisibility(View.INVISIBLE);
                    }
                    return;
                }
                imageViewExoVideoPlayVideo.setVisibility(View.GONE);
                thaumbnail_for_video.setVisibility(View.VISIBLE);
                progress_bar_for_video.setVisibility(View.VISIBLE);
                showLeanExoIdlePoster(thaumbnail_for_video, null,
                        StatusVideoMedia.resolveThumbnailUrl(list.get(position)));
                imageViewExoVideoPlayVideo.setVisibility(View.GONE);
                simpleExoPlayerViewFull.setVisibility(View.VISIBLE);
                if (exoPlayer != null) {
                    exoPlayer.setPlayWhenReady(false);
                    exoPlayer.stop();
                    exoPlayer.release();
                    exoPlayer = null;
                }
                exoPlayer = new ExoPlayer.Builder(context).build();
                simpleExoPlayerViewFull.setPlayer(exoPlayer);
                simpleExoPlayerViewFull.setResizeMode(RESIZE_MODE_FIT);
                prepareFeedExoPlayer(exoPlayer, videoUrl);
                exoPlayer.setPlayWhenReady(true);
                full_videoViewHolder.side_menu = finalConvertViewFull.findViewById(R.id.video_side_menu);
                if (full_videoViewHolder.side_menu != null) {
                    full_videoViewHolder.side_menu.setVisibility(View.INVISIBLE);
                }
                wireLeanExoChrome(simpleExoPlayerViewFull, finalConvertViewFull);
                exoPlayer.addListener(new Player.Listener() {
                    @Override
                    public void onPlaybackStateChanged(int playbackState) {
                        if (exoPlayer == null) {
                            return;
                        }
                        boolean playWhenReady = exoPlayer.getPlayWhenReady();
                        showLog("onPlayerStateChanged full::" + playWhenReady + " state=" + playbackState);
                        applyLeanExoOverlayState(
                                playbackState,
                                playWhenReady,
                                thaumbnail_for_video,
                                imageViewExoVideoPlayVideo,
                                progress_bar_for_video,
                                simpleExoPlayerViewFull);
                        LinearLayout sideMenu = finalConvertViewFull.findViewById(R.id.video_side_menu);
                        if (playbackState == Player.STATE_ENDED
                                || (!playWhenReady && playbackState != Player.STATE_BUFFERING)) {
                            if (sideMenu != null) {
                                sideMenu.setVisibility(View.VISIBLE);
                            }
                        } else if (playWhenReady && sideMenu != null) {
                            sideMenu.setVisibility(View.INVISIBLE);
                        }
                    }
                });
            }
        });

        full_videoViewHolder.like_for_video_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                full_videoViewHolder.like_for_video_image = convertView.findViewById(R.id.like_for_video_image);
                int newStatus = applyLikeChange(position, null);
                if (newStatus == 1) full_videoViewHolder.like_for_video_image.setImageResource(ic_like_half_selected1);
                else full_videoViewHolder.like_for_video_image.setImageResource(ic_like_half1);
            }
        });

        full_videoViewHolder.comment_for_video_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent("COMMENTS_PAGE");
                intent.putExtra("message", "click_event");

                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                try {
                    adapterCallback.OpenComments(list.get(position).getPostId(), list.get(position).getSubPostId());
                } catch (ClassCastException e) {
                    e.printStackTrace();
                    // do something
                }

            }
        });

        View finalConvertViewFullYtb = convertView;
        full_videoViewHolder.full_image_whatsapp_bg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareVideoDirect(position, VideoShareHelper.ShareMode.WHATSAPP_STATUS);

            }
        });

//        full_videoViewHolder.share_for_video_ll.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                shareExoPlayerFullVideoShare(position,0,thaumbnail_for_video,exo_play_layout,imageViewExoVideoPlayVideo,simpleExoPlayerViewFull);
//            }
//        });
        //Uri mp4VideoUri = Uri.parse((list.get(position).getVideoUrl())); //ABC NEWS



        final RelativeLayout relativeLayoutShareOpt = convertView.findViewById(R.id.relativeLayoutShareOpt);
        final  TextView textViewAllPage = convertView.findViewById(R.id.textViewAllPage);
        final  TextView textViewSinglePage = convertView.findViewById(R.id.textViewSinglePage);
        final ImageView  imageViewShareClose = convertView.findViewById(R.id.imageViewShareClose);

        final ImageView  imageViewShareSingle = convertView.findViewById(R.id.imageViewShareSingle);
        final ImageView  imageViewShareAll = convertView.findViewById(R.id.imageViewShareAll);
        imageViewShareSingle.setImageResource(R.drawable.ic_share_download);
        imageViewShareAll.setImageResource(R.drawable.ic_share_all);
        textViewSinglePage.setText("Share Article");
        textViewAllPage.setText("Share Video");

        //imageGallaryHolder.galleryLayout.setLayerType(View.LAYER_TYPE_NONE,null);


        full_videoViewHolder.share_for_video_ll.setOnClickListener(view -> {
            if(relativeLayoutShareOpt.getVisibility()==VISIBLE){
                relativeLayoutShareOpt.setVisibility(GONE);
            }else {
                relativeLayoutShareOpt.setVisibility(VISIBLE);
            }
        });

        relativeLayoutShareOpt.setVisibility(GONE);
        imageViewShareClose.setOnClickListener(view -> relativeLayoutShareOpt.setVisibility(GONE));
        textViewAllPage.setOnClickListener(view -> {
            relativeLayoutShareOpt.setVisibility(GONE);
            shareVideoDirect(position, VideoShareHelper.ShareMode.GENERAL);
        });
        textViewSinglePage.setOnClickListener(view -> {
            relativeLayoutShareOpt.setVisibility(GONE);
                shareExoPlayerFullVideoShare(position,0,thaumbnail_for_video,exo_play_layout,imageViewExoVideoPlayVideo,simpleExoPlayerViewFull);
        });


    }

    public void shareExoPlayerFullVideoShare(int position, int from, ImageView thaumbnail_for_video, RelativeLayout exo_play_layout, ImageView imageViewExoVideoPlayVideo, PlayerView simpleExoPlayerViewFull) {
        String mInvitationUrl = list.get(position).getShareLink();
        String msg = list.get(position).getTitle() + " " + mInvitationUrl;
        simpleExoPlayerViewFull.setVisibility(GONE);
        thaumbnail_for_video.setVisibility(VISIBLE);
        imageViewExoVideoPlayVideo.setVisibility(VISIBLE);
        countDownTimer = new CountDownTimer(2, 2) {
            public void onTick(long millisUntilFinished) {
            }
            public void onFinish() {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, mInvitationUrl.toString());
                intent.putExtra(Intent.EXTRA_TEXT, msg);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (from == 1) {
                    intent.setType("text/plain");
                    intent.setPackage("com.whatsapp");
                }
                Bitmap b = ScreenShott.getInstance().takeScreenShotOfView(exo_play_layout);
                //bitmapResize(b);
                try {
                    File file = new File(context.getExternalCacheDir(), "sample.png");
                    FileOutputStream fOut = new FileOutputStream(file);
                    b.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                    fOut.flush();
                    fOut.close();
                    file.setReadable(true, true);
                    Uri photoURI = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
                    intent.putExtra(Intent.EXTRA_STREAM, photoURI);
                    List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(Intent.createChooser(intent, "Share Link"), PackageManager.MATCH_DEFAULT_ONLY);
                    for (ResolveInfo resolveInfo : resInfoList) {
                        String packageName = resolveInfo.activityInfo.packageName;
                        context.grantUriPermission(packageName, photoURI, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (imageViewExoVideoPlayVideo.getVisibility() == VISIBLE) {
                    simpleExoPlayerViewFull.setVisibility(GONE);
                    thaumbnail_for_video.setVisibility(VISIBLE);
                    imageViewExoVideoPlayVideo.setVisibility(View.VISIBLE);

                } else {
                    imageViewExoVideoPlayVideo.setVisibility(View.VISIBLE);
                    thaumbnail_for_video.setVisibility(VISIBLE);
                    if (simpleExoPlayerViewFull.getPlayer() != null) {
                        simpleExoPlayerViewFull.getPlayer().release();
                        simpleExoPlayerViewFull.setPlayer(null);
                    }
                    exoPlayer = null;


                }
                intent.setType("image/*");
                if (context != null && context.getPackageName().equals(BuildConfig.APPLICATION_ID)) {
                    context.startActivity(Intent.createChooser(intent, "Share Link"));
                }
                Share_Count_Value("share", list.get(position).getPostId());
            }
        }.start();

    }

    private void showFullImage(int position, View convertView) {
        showLog("Check Point FULL_IMAGE:: 2");
        applyActionRowWhatsappVisibility(context, convertView);
        ConstraintLayout layout=convertView.findViewById(R.id.parent_layout);
        layout.getLayoutParams().height=screenHeight;
        fullImageActivity.mimageView = convertView.findViewById(R.id.image_view);
        fullImageActivity.side_menu = convertView.findViewById(R.id.side_menu);
        fullImageActivity.layout_strip_icon = convertView.findViewById(R.id.layout_strip_icon);
        fullImageActivity.video_side_menu = convertView.findViewById(R.id.video_side_menu);
        fullImageActivity.layout_strip_iconnew = convertView.findViewById(R.id.layout_strip_iconnew);
        fullImageActivity.user_pic = convertView.findViewById(R.id.user_pic);
        fullImageActivity.strip_text_name = convertView.findViewById(R.id.strip_text_name);
        fullImageActivity.full_image_comment_bg = convertView.findViewById(R.id.full_image_comment_bg);
        fullImageActivity.full_image_whatsapp_bg = convertView.findViewById(R.id.full_image_whatsapp_bg);
        fullImageActivity.full_image_share_bg = convertView.findViewById(R.id.full_image_share_bg);
        fullImageActivity.share_download_app_logo = convertView.findViewById(R.id.share_download_app_logo);
        fullImageActivity.strip_logo = convertView.findViewById(R.id.share_download_app_logo);
        fullImageActivity.varified_btn = convertView.findViewById(R.id.varified_btn);

        bindFullImageAvatarPlus(position);
        fullImageActivity.full_image_comment_bg.setImageResource(R.drawable.chatbox);
        fullImageActivity.full_image_whatsapp_bg.setImageResource(R.drawable.ic_whatsapp_original1);
        fullImageActivity.full_image_share_bg.setImageResource(R.drawable.ic_share_full);
        fullImageActivity.full_image_share_bg.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        fullImageActivity.full_image_share_bg.setBackground(null);
        fullImageActivity.share_download_app_logo.setImageResource(R.drawable.ic_download_now_icon);
        fullImageActivity.strip_logo.setImageResource(R.drawable.ic_logo_strip_icon);
        fullImageActivity.full_image_view_details = convertView.findViewById(R.id.full_image_view_details);
        //fullImageActivity.full_image_view_details.getLayoutParams().width=screenWidth;
        //fullImageActivity.full_image_view_details.getLayoutParams().height=screenHeight;
        if (list.get(position).getImages() != null && !list.get(position).getImages().isEmpty()) {
            String imageUrl = list.get(position).getImages().get(0);
            if (fullImageActivity.mimageView.getTag() == null || !fullImageActivity.mimageView.getTag().equals(imageUrl)) {
                showLog("Check Point FULL_IMAGE:: 3:: " + imageUrl);
                imageLoader.displayImage(imageUrl, fullImageActivity.mimageView, options, animationListener);
                showLog("Check Point FULL_IMAGE:: 4");
            }
        }
        fullImageActivity.user_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openWriterProfileFromFeed(position);

            }
        });


        fullImageActivity.strip_text_name.setText(displayCopy(list.get(position).getStrip_content()));
        applyFeedStripFont(fullImageActivity.strip_text_name);
        fullImageActivity.full_img_like = convertView.findViewById(R.id.full_img_like_text);
        fullImageActivity.full_img_share = convertView.findViewById(R.id.full_image_share_text);
        fullImageActivity.full_img_whtsapp = convertView.findViewById(R.id.full_img_whtsapp_text);
        fullImageActivity.full_img_comment = convertView.findViewById(R.id.full_img_comment_txt);
        bindFullPostEngagementCounts(fullImageActivity, position);

        final LinearLayout full_image_like = convertView.findViewById(R.id.full_image_like);
        fullImageActivity.full_image_whatsapp = convertView.findViewById(R.id.full_image_whatsapp);
        fullImageActivity.full_image_share = convertView.findViewById(R.id.full_image_share);
        fullImageActivity.full_image_comment = convertView.findViewById(R.id.full_image_comment);
        if (fullImageActivity.full_image_share != null) {
            fullImageActivity.full_image_share.setVisibility(View.VISIBLE);
        }


        final ImageView full_image_selected_like = convertView.findViewById(R.id.full_image_selected_like);


        fullImageActivity.layout_strip_iconnew = convertView.findViewById(R.id.layout_strip_iconnew);
        showLog("Check Point FULL_IMAGE:: 7");
        if (list.get(position).getLikeStatus() != null && list.get(position).getLikeStatus() == 1) {
            full_image_selected_like.setImageResource(ic_like_half_selected1);
        } else {
            full_image_selected_like.setImageResource(ic_like_half1);
        }
        // check null
        if (fullImageActivity.strip_text_name.getText().toString().matches("null")) {
            fullImageActivity.strip_text_name.setText("" + " The Best Regional News App ");
        }

        showLog("Check Point FULL_IMAGE:: 8");
        fullImageActivity.full_image_view_details.setOnTouchListener((view, motionEvent) -> false);

        View finalConvertViewFull = convertView;
        fullImageActivity.full_image_view_details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fullImageActivity.side_menu.isShown() || fullImageActivity.side_menu.getVisibility() == View.VISIBLE) {
                    fullImageActivity.side_menu = finalConvertViewFull.findViewById(R.id.side_menu);
                    fullImageActivity.side_menu.setVisibility(View.INVISIBLE);
                    showLog("fullimageadapter"+ "click gone");
                } else {
                    fullImageActivity.side_menu = finalConvertViewFull.findViewById(R.id.side_menu);
                    fullImageActivity.side_menu.setVisibility(View.VISIBLE);
                    showLog("fullimageadapter"+ "click visible");

                }
            }
        });
        showLog("like status:"+list.get(position).getLikeStatus());

        final TextView likeCountViewRef = convertView.findViewById(R.id.full_img_like_text);
        full_image_like.setTag(position);
        full_image_like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = view.getTag() instanceof Integer ? (Integer) view.getTag() : position;
                int newStatus = applyLikeChange(pos, likeCountViewRef);
                if (newStatus == 1) full_image_selected_like.setImageResource(ic_like_half_selected1);
                else full_image_selected_like.setImageResource(ic_like_half1);
            }
        });


        fullImageActivity.full_image_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //fullImageActivity.layout_strip_iconnew.setVisibility(View.GONE);
                Intent intent = new Intent("COMMENTS_PAGE");
                intent.putExtra("message", "click_event");

                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                try {
                    adapterCallback.OpenComments(list.get(position).getPostId(), list.get(position).getSubPostId());
                } catch (ClassCastException e) {
                    e.printStackTrace();
                    // do something
                }

            }
        });

        View myFullView = convertView;
        fullImageActivity.full_image_whatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareFullImageScree(myFullView,position);
            }
        });

        fullImageActivity.full_image_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                fullImageActivity.full_image_share = myFullView.findViewById(R.id.full_image_share);
                fullImageActivity.layout_strip_iconnew = myFullView.findViewById(R.id.layout_strip_iconnew);
                fullImageActivity.full_image_view_details = myFullView.findViewById(R.id.full_image_view_details);

                fullImageActivity.layout_strip_iconnew.setVisibility(View.GONE);


                String appLink = "https://com.anyNews.anynews/";

                //String mInvitationUrl = list.get(position).getShareLink();
                //String msg = list.get(position).getTitle() + " "+ mInvitationUrl;


                countDownTimer = new CountDownTimer(2, 2) {

                    public void onTick(long millisUntilFinished) {

                    }

                    public void onFinish() {

                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        Bitmap b = ScreenShott.getInstance().takeScreenShotOfView(fullImageActivity.full_image_view_details);

                        //  layout_strip_iconnew.setVisibility(View.GONE);

                        /*    data for update perticular article share     */

                        JsonObject otp = set_count_request("share", list.get(position).getPostId());

                        APIService apiService = RestApiClient.getClient().create(APIService.class);
                        Call<EncreptionRequest> apiResponse = apiService.update_post_count("v3", otp);
                        apiResponse.enqueue(new Callback<EncreptionRequest>() {
                            //@RequiresApi(api = Build.VERSION_CODES.O)
                            @Override

                            public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {

                                EncreptionRequest encreptionRequest = response.body();
                                if (encreptionRequest == null || encreptionRequest.getData() == null) return;
                                String encrpt_data = decrypt(encreptionRequest.getData());
                                showLog("update_post_count:"+encrpt_data);

                                Gson gsonObj = new Gson();
                                LikeDownloadRespouse likeDownloadRespouse = gsonObj.fromJson(encrpt_data, LikeDownloadRespouse.class);

                                if (likeDownloadRespouse.getSuccess()) {

                                    try {
                                        String msg = list.get(position).getTitle() + " " + likeDownloadRespouse.getShareLink();
                                        showLog("ShareLinkAdapter"+ likeDownloadRespouse.getShareLink());
                                        intent.putExtra(Intent.EXTRA_SUBJECT, likeDownloadRespouse.getShareLink());
                                        intent.putExtra(Intent.EXTRA_TEXT, msg);

                                        File file = new File(context.getExternalCacheDir(), "sample.png");
                                        FileOutputStream fOut = new FileOutputStream(file);
                                        b.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                                        fOut.flush();
                                        fOut.close();
                                        file.setReadable(true, true);
                                        Uri photoURI = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
                                        intent.putExtra(Intent.EXTRA_STREAM, photoURI);
                                        List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(Intent.createChooser(intent, "Share Link"), PackageManager.MATCH_DEFAULT_ONLY);
                                        for (ResolveInfo resolveInfo : resInfoList) {
                                            String packageName = resolveInfo.activityInfo.packageName;
                                            context.grantUriPermission(packageName, photoURI, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                        }
                                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    fullImageActivity.layout_strip_iconnew.setVisibility(View.GONE);
                                    intent.setType("image/*");
                                    if (context != null && context.getPackageName().equals(BuildConfig.APPLICATION_ID)) {
                                        context.startActivity(Intent.createChooser(intent, "Share Link"));
                                    }
                                }
                            }
                            @Override
                            public void onFailure(Call<EncreptionRequest> call, Throwable t) {
                            }
                        });
                    }
                }.start();

            }
        });
    }

    public void shareFullImageScree(View myFullView, int position) {


        fullImageActivity.full_image_share = myFullView.findViewById(R.id.full_image_share);
        fullImageActivity.layout_strip_iconnew = myFullView.findViewById(R.id.layout_strip_iconnew);
        fullImageActivity.full_image_view_details = myFullView.findViewById(R.id.full_image_view_details);

        fullImageActivity.layout_strip_iconnew.setVisibility(View.GONE);


        String appLink = "https://com.anyNews.anynews/";


        countDownTimer = new CountDownTimer(2, 2) {

            public void onTick(long millisUntilFinished) {


            }

            public void onFinish() {

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setPackage("com.whatsapp");

                Bitmap b = ScreenShott.getInstance().takeScreenShotOfView(fullImageActivity.full_image_view_details);


                JsonObject otp = set_count_request("share", list.get(position).getPostId());

                APIService apiService = RestApiClient.getClient().create(APIService.class);
                Call<EncreptionRequest> apiResponse = apiService.update_post_count("v3", otp);
                apiResponse.enqueue(new Callback<EncreptionRequest>() {
                    //@RequiresApi(api = Build.VERSION_CODES.O)
                    @Override

                    public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {

                        EncreptionRequest encreptionRequest = response.body();
                        if (encreptionRequest == null || encreptionRequest.getData() == null) return;
                        String encrpt_data = decrypt(encreptionRequest.getData());

                        Gson gsonObj = new Gson();
                        LikeDownloadRespouse likeDownloadRespouse = gsonObj.fromJson(encrpt_data, LikeDownloadRespouse.class);

                        if (likeDownloadRespouse.getSuccess()) {
                            //String shareLink = likeDownloadRespouse.getShareLink();
                            //showLog("ShareLink",""+shareLink);
//                            intent.putExtra(Intent.EXTRA_SUBJECT, likeDownloadRespouse.getShareLink());
//                            intent.putExtra(Intent.EXTRA_TEXT, likeDownloadRespouse.getShareLink());

                            String msg = list.get(position).getTitle() + " " + likeDownloadRespouse.getShareLink();
                            showLog("ShareLinkAdapter"+ likeDownloadRespouse.getShareLink());
                            showLog("ShareLinkAdapter"+ msg);
                            intent.putExtra(Intent.EXTRA_SUBJECT, likeDownloadRespouse.getShareLink());
                            intent.putExtra(Intent.EXTRA_TEXT, msg);

                            try {
                                File file = new File(context.getExternalCacheDir(), "sample.png");
                                FileOutputStream fOut = new FileOutputStream(file);
                                b.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                                fOut.flush();
                                fOut.close();
                                file.setReadable(true, true);
                                Uri photoURI = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
                                intent.putExtra(Intent.EXTRA_STREAM, photoURI);
                                List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(Intent.createChooser(intent, "Share Link"), PackageManager.MATCH_DEFAULT_ONLY);
                                for (ResolveInfo resolveInfo : resInfoList) {
                                    String packageName = resolveInfo.activityInfo.packageName;
                                    context.grantUriPermission(packageName, photoURI, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                }
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            fullImageActivity.layout_strip_iconnew.setVisibility(View.GONE);
                            intent.setType("image/*");
                            if (context != null && context.getPackageName().equals(BuildConfig.APPLICATION_ID)) {
                                context.startActivity(Intent.createChooser(intent, "Share Link"));
                            }


                        }


                    }

                    @Override
                    public void onFailure(Call<EncreptionRequest> call, Throwable t) {


                    }
                });


            }
        }.start();


    }

    public void showNormalImage(int position, View convertView) {
        /* Writer Details */
        showLog("showNormalImage:"+position);
        LinearLayout layoutMain = convertView.findViewById(R.id.parent_layout);
        layoutMain.getLayoutParams().height=screenHeight;
        halfImageActivity.writer_time = convertView.findViewById(R.id.writer_time);
        halfImageActivity.writer_name = convertView.findViewById(R.id.writer_name);
        //   halfImageActivity.local_name = convertView.findViewById(R.id.writer_name);
        halfImageActivity.my_feed = convertView.findViewById(R.id.my_feed);
        halfImageActivity.iv_share_and_earn = convertView.findViewById(R.id.iv_share_and_earn);
        halfImageActivity.writer_profile_pic = convertView.findViewById(R.id.writer_profile_pic);
        halfImageActivity.writer_profile_image = convertView.findViewById(R.id.writer_profile_image);
        //Half gallery images...
        halfImageActivity.viewPager = convertView.findViewById(R.id.viewpager);
        halfImageActivity.indicator = convertView.findViewById(R.id.indicator);
        halfImageActivity.galleryImageLayout = convertView.findViewById(R.id.galleryImageLayout);
        halfImageActivity.galleryImageLayout.setVisibility(GONE);
        halfImageActivity.strip_text_name = convertView.findViewById(R.id.strip_text_name);

        /*  like */
        halfImageActivity.tamil_half_content_description = convertView.findViewById(R.id.tamil_half_content_description);
        halfImageActivity.tamil_half_content_title = convertView.findViewById(R.id.tamil_half_content_title);
        halfImageActivity.english_half_content_description = convertView.findViewById(R.id.english_half_content_description);
        halfImageActivity.english_half_content_title = convertView.findViewById(R.id.english_half_content_title);
        /*  test details */
        halfImageActivity.half_content_description = convertView.findViewById(R.id.half_content_description);
        halfImageActivity.half_content_title = convertView.findViewById(R.id.half_content_title);
        halfImageActivity.app_download_logo = convertView.findViewById(R.id.app_download_logo);
        halfImageActivity.with_strips_layout_ic = convertView.findViewById(R.id.with_strips_layout_ic);
        halfImageActivity.new_half_image = convertView.findViewById(R.id.new_half_image);
        final ImageView new_half_image = convertView.findViewById(R.id.new_half_image);
        final RelativeLayout layoutImageHeight = convertView.findViewById(R.id.layoutImageHeight);
        new_half_image.getLayoutParams().height = (layoutHeight);
        layoutImageHeight.getLayoutParams().height = (layoutHeight);
        halfImageActivity.ll_refer_and_earn_half = convertView.findViewById(R.id.ll_refer_and_earn_half);
        halfImageActivity.layoutParentDate = convertView.findViewById(R.id.layoutParentDate);
        TextView textViewPostDate = convertView.findViewById(R.id.textViewPostDate);
        halfImageActivity.ll_footer_layout = convertView.findViewById(R.id.ll_footer_layout);
        showLog("ll_footer_layout:::"+halfImageActivity.ll_footer_layout.getVisibility());
        halfImageActivity.ll_footer_layout.setVisibility(VISIBLE);
        halfImageActivity.parent_layout = convertView.findViewById(R.id.parent_layout);
        AvatarImageHelper.loadUil(
                imageLoader,
                halfImageActivity.writer_profile_pic,
                list.get(position).getWriterProfile());
        halfImageActivity.app_download_logo.setImageResource(R.drawable.ic_download_now_icon);
        halfImageActivity.writer_time = convertView.findViewById(R.id.writer_time);
        halfImageActivity.writer_designation = convertView.findViewById(R.id.writer_designation);
        halfImageActivity.writer_time = convertView.findViewById(R.id.writer_time);
        halfImageActivity.half_comment = convertView.findViewById(R.id.half_comment);
        halfImageActivity.with_strip_layout = convertView.findViewById(R.id.with_strips_layout);
        halfImageActivity.with_out_strip_layout = convertView.findViewById(R.id.with_out_strips_layout);
        halfImageActivity.relative_half_content = convertView.findViewById(R.id.relative_half_contents);
        halfImageActivity.report = convertView.findViewById(R.id.report);
        halfImageActivity.half_image_like = convertView.findViewById(R.id.half_image_like);
        //halfImageActivity.half_image_dislike = convertView.findViewById(R.id.half_image_dislike);
        halfImageActivity.half_image_like_icon = convertView.findViewById(R.id.half_image_like_icon);
        //halfImageActivity.halfimage_dislike_icon = convertView.findViewById(R.id.halfimage_dislike_icon);
        halfImageActivity.writer_follow = convertView.findViewById(R.id.writer_follow);
        halfImageActivity.writer_follower = convertView.findViewById(R.id.writer_follower);
        final TextView test_folloew = convertView.findViewById(R.id.test_folloew);
        final TextView followingLabel = convertView.findViewById(R.id.second_language_name);
        halfImageActivity.writer_time = convertView.findViewById(R.id.writer_time);
        halfImageActivity.writer_designation = convertView.findViewById(R.id.writer_designation);
        halfImageActivity.writer_time = convertView.findViewById(R.id.writer_time);
        /*  click events */
        halfImageActivity.half_image_share = convertView.findViewById(R.id.half_share);
        halfImageActivity.half_comment = convertView.findViewById(R.id.half_comment);
        halfImageActivity.relative_half_content = convertView.findViewById(R.id.relative_half_contents);
        halfImageActivity.report = convertView.findViewById(R.id.report);
        final ImageView half_image_like = convertView.findViewById(R.id.half_image_like);
        //halfImageActivity.half_image_dislike = convertView.findViewById(R.id.half_image_dislike);
        halfImageActivity.half_image_like_icon = convertView.findViewById(R.id.half_image_like_icon);
        //halfImageActivity.halfimage_dislike_icon = convertView.findViewById(R.id.halfimage_dislike_icon);
        halfImageActivity.writer_follow = convertView.findViewById(R.id.writer_follow);
        halfImageActivity.writer_follower = convertView.findViewById(R.id.writer_follower);
        halfImageActivity.half_comment_icon = convertView.findViewById(R.id.half_comment_icon);
        halfImageActivity.half_share_icon = convertView.findViewById(R.id.half_share_icon);
        halfImageActivity.report_icon = convertView.findViewById(R.id.report_icon);
        halfImageActivity.with_strip_layout.setBackgroundResource(R.drawable.ic_half_image_strike_icon);
        TextView textViewWebUrl = convertView.findViewById(R.id.textViewWebUrl);
        TextView textViewStripWebUrl = convertView.findViewById(R.id.textViewStripWebUrl);
        bindFeedWebUrlPill(textViewWebUrl, textViewStripWebUrl, list.get(position), false);

        bindHalfPostEngagementCounts(convertView, position);
        bindArticleTtsSpeaker(convertView, position);

        bindWriterStripIdentity(
                list.get(position),
                halfImageActivity.writer_name,
                halfImageActivity.writer_designation,
                halfImageActivity.writer_time);
        try {
            WriterStripStyleHelper.apply(
                    context,
                    halfImageActivity.writer_name,
                    halfImageActivity.writer_designation,
                    halfImageActivity.writer_time,
                    halfImageActivity.my_feed,
                    halfImageActivity.ll_refer_and_earn_half);
        } catch (Exception ignored) {
        }
        bindLocationPill(
                halfImageActivity.my_feed,
                halfImageActivity.iv_share_and_earn,
                halfImageActivity.ll_refer_and_earn_half,
                list.get(position));
        textViewPostDate.setText(list.get(position).getPost_date_time());

        showLog("Check Point Adapter:: 5");
        showLog("Check Point LanguageId:: "+LanguageId);
        halfImageActivity.english_half_content_description.setTag(position);
        halfImageActivity.english_half_content_title.setTag(position);

        halfImageActivity.half_content_description.setTag(position);
        halfImageActivity.half_content_title.setTag(position);

        halfImageActivity.tamil_half_content_description.setTag(position);
        halfImageActivity.tamil_half_content_title.setTag(position);

        if (LanguageId == 4) {

            halfImageActivity.english_half_content_description.setVisibility(View.GONE);
            halfImageActivity.english_half_content_title.setVisibility(View.GONE);

            halfImageActivity.half_content_description.setVisibility(View.GONE);
            halfImageActivity.half_content_title.setVisibility(View.GONE);


            halfImageActivity.tamil_half_content_description.setVisibility(View.VISIBLE);
            halfImageActivity.tamil_half_content_title.setVisibility(View.VISIBLE);

            halfImageActivity.tamil_half_content_description.setText(displayCopy(list.get(position).getDescription()));
            halfImageActivity.tamil_half_content_title.setText(displayCopy(list.get(position).getTitle()));
            halfImageActivity.strip_text_name.setText(displayCopy(list.get(position).getStrip_content()));
            showLog("Content Description TAMIL: "+halfImageActivity.tamil_half_content_description.getText().toString());
            applyFeedArticleFonts(
                    halfImageActivity.tamil_half_content_title,
                    halfImageActivity.tamil_half_content_description
            );
            applyFeedStripFont(halfImageActivity.strip_text_name);

        } else if (LanguageId == 1) {
            halfImageActivity.tamil_half_content_description.setVisibility(View.GONE);
            halfImageActivity.half_content_title.setVisibility(View.GONE);
            halfImageActivity.half_content_description.setVisibility(View.GONE);
            halfImageActivity.half_content_title.setVisibility(View.GONE);

            halfImageActivity.english_half_content_description.setVisibility(View.VISIBLE);
            halfImageActivity.english_half_content_title.setVisibility(View.VISIBLE);

            halfImageActivity.english_half_content_description.setText(displayCopy(list.get(position).getDescription()));
            halfImageActivity.english_half_content_title.setText(displayCopy(list.get(position).getTitle()));
            halfImageActivity.strip_text_name.setText(displayCopy(list.get(position).getStrip_content()));
            showLog("Content Description ENG : "+halfImageActivity.english_half_content_description.getText().toString());
            applyFeedArticleFonts(
                    halfImageActivity.english_half_content_title,
                    halfImageActivity.english_half_content_description
            );
            applyFeedStripFont(halfImageActivity.strip_text_name);
        } else {

            halfImageActivity.english_half_content_description.setVisibility(View.GONE);
            halfImageActivity.english_half_content_title.setVisibility(View.GONE);
            halfImageActivity.tamil_half_content_description.setVisibility(View.GONE);
            halfImageActivity.half_content_title.setVisibility(View.GONE);
            halfImageActivity.half_content_description.setVisibility(View.VISIBLE);
            halfImageActivity.half_content_title.setVisibility(View.VISIBLE);
            halfImageActivity.half_content_description.setText(displayCopy(list.get(position).getDescription()));
            halfImageActivity.half_content_title.setText(displayCopy(list.get(position).getTitle()));
            halfImageActivity.strip_text_name.setText(displayCopy(list.get(position).getStrip_content()));
            showLog("Content Description HALF: "+halfImageActivity.half_content_description.getText().toString());
            applyFeedArticleFonts(
                    halfImageActivity.half_content_title,
                    halfImageActivity.half_content_description
            );
            applyFeedStripFont(halfImageActivity.strip_text_name);


        }

        showLog("Article Data:"+ "LanguageID" + LanguageId);



        // with below method we are setting display option for our image..
        if (list.get(position).getImages() != null && !list.get(position).getImages().isEmpty()) {
            String halfImageUrl = list.get(position).getImages().get(0);
            if (new_half_image.getTag() == null || !new_half_image.getTag().equals(halfImageUrl)) {
                showLog("Check Point normal image:: " + halfImageUrl);
                imageLoader.displayImage(halfImageUrl, halfImageActivity.new_half_image, options, animationListener);
            }
        }
        showLog("Check Point Adapter:: 4");

        // hide and visible
        halfImageActivity.parent_layout.setOnTouchListener((view, motionEvent) -> false);

        View finalConvertViewHalf = convertView;
        halfImageActivity.parent_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLog("ll_footer_layout:33::"+halfImageActivity.ll_footer_layout.getVisibility());
                if (halfImageActivity.ll_footer_layout.isShown() || halfImageActivity.ll_footer_layout.getVisibility() == View.VISIBLE) {
                    halfImageActivity.ll_footer_layout = finalConvertViewHalf.findViewById(R.id.ll_footer_layout);
                    halfImageActivity.ll_footer_layout.setVisibility(View.GONE);
                    showLog("fullimageadapter"+ "click gone");
                    //  Toast.makeText(HomePage.this, "Gone", Toast.LENGTH_SHORT).show();
                } else {
                    halfImageActivity.ll_footer_layout = finalConvertViewHalf.findViewById(R.id.ll_footer_layout);
                    halfImageActivity.ll_footer_layout.setVisibility(View.VISIBLE);
                    showLog("fullimageadapter"+ "click visible");
                    //  Toast.makeText(HomePage.this, "Visible", Toast.LENGTH_SHORT).show();


                }
            }
        });



        showLog("Executed:"+ "FlipperAdapter");

        bindHalfLikeIcon(half_image_like, list.get(position).getLikeStatus());
        half_image_like.setTag(position);
        final TextView likeCountViewRef = (TextView) convertView.findViewById(R.id.half_like_count);
        final ImageView likeIconRef = half_image_like;
        View.OnClickListener halfLikeClick = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Object tag = half_image_like.getTag();
                if (!(tag instanceof Integer)) return;
                int currPosition = (Integer) tag;
                int newStatus = applyLikeChange(currPosition, likeCountViewRef);
                bindHalfLikeIcon(likeIconRef, newStatus);
            }
        };
        half_image_like.setOnClickListener(halfLikeClick);
        if (halfImageActivity.half_image_like_icon != null) {
            halfImageActivity.half_image_like_icon.setOnClickListener(halfLikeClick);
        }
        showLog("Check Point Adapter:: 6");
        applyWriterFollowState(
                halfImageActivity.writer_follow,
                halfImageActivity.writer_follower,
                test_folloew,
                followingLabel,
                position);
        showLog("Check Point Adapter:: 7");
        // Must run after bindLocationPill so Rewards/Flash never fall through to Local.
        bindDistrictOrRewardsClick(
                halfImageActivity.ll_refer_and_earn_half, list.get(position));
        final View finalConvertView1 = convertView;
        showLog("Check Point Adapter:: 9");
        halfImageActivity.half_image_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takeNormalImageScreenshot(convertView,position,0);
            }
        });

        halfImageActivity.half_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent("COMMENTS_PAGE");
                intent.putExtra("message", "click_event");

                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                try {
                    adapterCallback.OpenComments(list.get(position).getPostId(), list.get(position).getSubPostId());
                } catch (ClassCastException e) {
                    e.printStackTrace();
                    // do something
                }
            }
        });
        halfImageActivity.report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                AlertDialog.Builder builder = new AlertDialog.Builder(context);

                LayoutInflater inflater = LayoutInflater.from(context);
                View dialogView = inflater.inflate(R.layout.custom_dialog_layout, null);

                builder.setView(dialogView);

                RadioButton one = (RadioButton) dialogView.findViewById(R.id.one);
                RadioButton two = (RadioButton) dialogView.findViewById(R.id.two);
                RadioButton three = (RadioButton) dialogView.findViewById(R.id.three);
                RadioButton four = (RadioButton) dialogView.findViewById(R.id.four);

                TextView report = dialogView.findViewById(R.id.report);
                TextView title = dialogView.findViewById(R.id.title);
                EditText full_image_edt_news_headlines = dialogView.findViewById(R.id.full_image_edt_news_headlines);
                Button yes = dialogView.findViewById(R.id.yes);
                ImageView close_dailog = dialogView.findViewById(R.id.close_dailog);


                // Create the alert dialog
                final AlertDialog dialog = builder.create();

                JsonObject updateparams = ReportParams();

                close_dailog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });

                APIService apiService = RestApiClient.getClient().create(APIService.class);
                Call<GetReportIssueResponce> update = apiService.reportissue(updateparams);
                update.enqueue(new Callback<GetReportIssueResponce>() {

                    @Override
                    public void onResponse(Call<GetReportIssueResponce> call, retrofit2.Response<GetReportIssueResponce> response) {

                        GetReportIssueResponce updateResponce = response.body();

                        one.setText("" + updateResponce.getData().getOptions().get(0));
                        two.setText("" + updateResponce.getData().getOptions().get(1));
                        three.setText("" + updateResponce.getData().getOptions().get(2));
                        four.setText("" + updateResponce.getData().getOptions().get(3));
                        report.setText("" + updateResponce.getData().getTitle());
                        title.setText("" + updateResponce.getData().getOtherReason());
                        full_image_edt_news_headlines.setHint("" + updateResponce.getData().getPlaceholder());

                        yes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                String resoin;
                                if (one.isChecked()) {
                                    resoin = one.getText().toString();

                                } else if (two.isChecked()) {
                                    resoin = one.getText().toString();

                                } else if (three.isChecked()) {
                                    resoin = one.getText().toString();

                                } else {

                                    resoin = one.getText().toString();

                                }

                                JsonObject updateparams = DailogRequest(resoin, list.get(position).getPostId(), full_image_edt_news_headlines.getText().toString());


                                APIService apiService = RestApiClient.getClient().create(APIService.class);
                                Call<DailogSendRespouse> update = apiService.dailogSend(updateparams);


                                update.enqueue(new Callback<DailogSendRespouse>() {

                                    @Override
                                    public void onResponse(Call<DailogSendRespouse> call, retrofit2.Response<DailogSendRespouse> response) {

                                        DailogSendRespouse updateResponce = response.body();


                                        if (updateResponce.getSuccess()) {

                                            Toast.makeText(context, "" + updateResponce.getMsg(), Toast.LENGTH_SHORT).show();

                                        } else {
                                            Toast.makeText(context, "" + updateResponce.getMsg(), Toast.LENGTH_SHORT).show();


                                        }
                                        dialog.dismiss();
                                    }

                                    @Override
                                    public void onFailure(Call<DailogSendRespouse> call, Throwable t) {

                                        Toast.makeText(context, "" + call, Toast.LENGTH_SHORT).show();

                                    }
                                });
                            }
                        });


                    }

                    @Override
                    public void onFailure(Call<GetReportIssueResponce> call, Throwable t) {

                        Toast.makeText(context, "" + call, Toast.LENGTH_SHORT).show();

                    }
                });


                dialog.show();


            }
        });

        showLog("Check Point Adapter:: 11");
        if (list.get(position).getIs_source()) {
            descriptionText = "" + list.get(position).getDescription();
            descriptionHash = descriptionText.substring(descriptionText.indexOf("#") + 1);
            showLog("descriptionHash:"+descriptionHash);
            descriptionHash.trim();
            try {
                descriptionDirectionLink = descriptionHash;
                if (TextUtils.isEmpty(descriptionDirectionLink)) {
                    descriptionDirectionLink = descriptionHash;
                } else {
                    descriptionDirectionLink = descriptionHash.substring(0, descriptionHash.indexOf(" "));
                }

            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
            Spannable spannable = new SpannableString(descriptionText);
            String str = spannable.toString();
            int iStart = str.indexOf(descriptionDirectionLink);
            int length = descriptionDirectionLink.length();
            int iEnd = iStart + length;/*10 characters = in-network. */

            SpannableStringBuilder ssText = new SpannableStringBuilder(spannable);
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    showLog(" clickableSpan OnClick");
                    if (("" + list.get(position).getSource_link()).contains("category")) {
                        showLog(" clickableSpan OnClick category");
                        try {
                            //your code at here.
                            Intent viewIntent = new Intent(context,
                                    SearchActivity.class);
                            if (context != null && context.getPackageName().equals(BuildConfig.APPLICATION_ID)) {
                                context.startActivity(viewIntent);
                            }

                        } catch (ActivityNotFoundException e) {
                            e.printStackTrace();
                            Toast.makeText(context, "Unable to open browser", Toast.LENGTH_SHORT).show();
                        }
                    } else if (("" + list.get(position).getSource_link()).contains("event")) {
                        showLog(" clickableSpan OnClick event");
                        try {
                            //your code at here.
                            Intent viewIntent = new Intent(context,
                                    QuizPageActivity.class);
                            if (context != null && context.getPackageName().equals(BuildConfig.APPLICATION_ID)) {
                                context.startActivity(viewIntent);
                            }

                        } catch (ActivityNotFoundException e) {
                            e.printStackTrace();
                            Toast.makeText(context, "Unable to open browser", Toast.LENGTH_SHORT).show();
                        }
                    } else if (("" + list.get(position).getSource_link()).contains("profile")) {
                        try {
                            //your code at here.
                            showLog(" clickableSpan OnClick profile");
                            Intent viewIntent = new Intent(context,
                                    newProfileDetails.class);

                            if (context != null && context.getPackageName().equals(BuildConfig.APPLICATION_ID)) {
                                context.startActivity(viewIntent);
                            }

                        } catch (ActivityNotFoundException e) {
                            e.printStackTrace();
                            Toast.makeText(context, "Unable to open browser", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        showLog(" clickableSpan OnClick ELSE");
                        try {
                            //your code at here.
                            Intent viewIntent = new Intent("android.intent.action.VIEW",
                                    Uri.parse("" + list.get(position).getSource_link()));
                            if (context != null && context.getPackageName().equals(BuildConfig.APPLICATION_ID)) {
                                context.startActivity(viewIntent);
                            }

                        } catch (ActivityNotFoundException e) {
                            e.printStackTrace();
                            Toast.makeText(context, "Unable to open browser", Toast.LENGTH_SHORT).show();
                        }
                    }


                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(false);
                    ds.setColor(0xFF1877F2);
                }
            };
            try {
                ssText.setSpan(clickableSpan, iStart, iEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                int index = str.indexOf(descriptionDirectionLink) - 1;
                ssText = ssText.replace(index, iStart, " ");
            } catch (IndexOutOfBoundsException e    ) {
                e.printStackTrace();
            }

            if (list.get(position).getIs_source()) {

                if (LanguageId == 4) {
                    halfImageActivity.tamil_half_content_description.setText(ssText);
                } else if (LanguageId == 1) {
                    halfImageActivity. english_half_content_description.setText(ssText);
                } else {
                    halfImageActivity.half_content_description.setText(ssText);
                }
            } else {
                halfImageActivity. half_content_description.setText("" + list.get(position).getDescription());
            }
            halfImageActivity.half_content_description.setMovementMethod(new MovementMethodTV());
            halfImageActivity.half_content_description.setHighlightColor(Color.TRANSPARENT);
            halfImageActivity.half_content_description.setEnabled(true);
            halfImageActivity.tamil_half_content_description.setMovementMethod(new MovementMethodTV());
            halfImageActivity.tamil_half_content_description.setHighlightColor(Color.TRANSPARENT);
            halfImageActivity.tamil_half_content_description.setEnabled(true);
            halfImageActivity.english_half_content_description.setMovementMethod(new MovementMethodTV());
            halfImageActivity.english_half_content_description.setHighlightColor(Color.TRANSPARENT);
            halfImageActivity.english_half_content_description.setEnabled(true);
        }

        // halfimage_dislike_icon
        halfImageActivity.writer_follower.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                halfImageActivity.writer_follow = finalConvertView1.findViewById(R.id.writer_follow);
                halfImageActivity.writer_follower = finalConvertView1.findViewById(R.id.writer_follower);
                halfImageActivity.test_folloew = finalConvertView1.findViewById(R.id.test_folloew);

                Intent intent = new Intent("WRITER_FOLLOWER");
                intent.putExtra("message", "click_event");

                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

                if (writerFollowUiState(list.get(position).getIsFollow()) == 0) {

                    halfImageActivity.writer_follow.setVisibility(View.GONE);
                    halfImageActivity.writer_follower.setVisibility(View.VISIBLE);
                    test_folloew.setText("Following");
                    list.get(position).setIsFollow(1);

                    set_follow_un_follow(list.get(position).getWriterId(), 1);


                } else {

                    halfImageActivity.writer_follow.setVisibility(View.VISIBLE);
                    halfImageActivity.writer_follower.setVisibility(View.GONE);
                    test_folloew.setText("Follow");
                    list.get(position).setIsFollow(0);

                    set_follow_un_follow(list.get(position).getWriterId(), 0);

                }

            }
        });

        halfImageActivity.writer_follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                halfImageActivity.writer_follow = finalConvertView1.findViewById(R.id.writer_follow);
                halfImageActivity.writer_follower = finalConvertView1.findViewById(R.id.writer_follower);
                halfImageActivity.test_folloew = finalConvertView1.findViewById(R.id.test_folloew);

                Intent intent = new Intent("WRITER_FOLLOW");
                intent.putExtra("message", "click_event");

                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

                if (writerFollowUiState(list.get(position).getIsFollow()) == 0) {

                    halfImageActivity.writer_follow.setVisibility(View.GONE);
                    halfImageActivity.writer_follower.setVisibility(View.VISIBLE);
                    test_folloew.setText("Following");
                    list.get(position).setIsFollow(1);

                    set_follow_un_follow(list.get(position).getWriterId(), 1);


                } else {

                    halfImageActivity.writer_follow.setVisibility(View.VISIBLE);
                    halfImageActivity.writer_follower.setVisibility(View.GONE);
                    test_folloew.setText("Follow");
                    list.get(position).setIsFollow(0);

                    set_follow_un_follow(list.get(position).getWriterId(), 0);


                }

            }
        });

        halfImageActivity.writer_profile_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWriterProfileFromFeed(position);

            }
        });

        if(list.get(position).isIs_gallery()){
            halfImageActivity.galleryImageLayout.setVisibility(VISIBLE);
            halfImageActivity.new_half_image.setVisibility(GONE);
            new_half_image.setVisibility(GONE);
            WormDotsIndicator indicator = convertView.findViewById(R.id.indicator);

            // half gallery images
            halfImageGallery=new ArrayList<>();
            ArrayList<String> listSample=new ArrayList<>();
            listSample.addAll(list.get(position).getImages());

            // for (int i = 0; i < list.get(position).getImages().size(); i++) {
            for (int i = 0; i < listSample.size(); i++) {
                //halfImageGallery.add(list.get(position).getImages().get(i));
                halfImageGallery.add(listSample.get(i));
            }
            halfImageActivity.viewPager.setAdapter(new SamplePagerAdapter(context, halfImageGallery,layoutHeight,true));

            // added to increase speed in view pager
            //halfImageActivity.viewPager.setOffscreenPageLimit(list.get(position).getImages().size() - 1);
            halfImageActivity.viewPager.setOffscreenPageLimit(listSample.size() - 1);
            indicator.setViewPager(halfImageActivity.viewPager);
            halfImageActivity.ll_footer_layout.setVisibility(VISIBLE);

        }else{
            halfImageActivity.galleryImageLayout.setVisibility(GONE);
            halfImageActivity.new_half_image.setVisibility(VISIBLE);
            new_half_image.setVisibility(VISIBLE);
        }

    }

    public void takeNormalImageScreenshot(View finalConvertView1, int customPosition,int from) {
        {
            showLog("ll_footer_layout::: takeNormalImageScreenshot");
            halfImageActivity.parent_layout = finalConvertView1.findViewById(R.id.parent_layout);
            halfImageActivity.relative_half_content = finalConvertView1.findViewById(R.id.relative_half_contents);
            halfImageActivity.with_strip_layout = finalConvertView1.findViewById(R.id.with_strips_layout);
            halfImageActivity.with_out_strip_layout = finalConvertView1.findViewById(R.id.with_out_strips_layout);
            halfImageActivity.layoutParentDate = finalConvertView1.findViewById(R.id.layoutParentDate);
            halfImageActivity.ll_footer_layout = finalConvertView1.findViewById(R.id.ll_footer_layout);
            halfImageActivity.ll_footer_layout = finalConvertView1.findViewById(R.id.ll_footer_layout);
            TextView textViewWebUrl = finalConvertView1.findViewById(R.id.textViewWebUrl);
            LinearLayout indicator = finalConvertView1.findViewById(R.id.dotIndicatior);
            indicator.setVisibility(GONE);
            halfImageActivity.with_out_strip_layout.setVisibility(View.GONE);
            halfImageActivity.with_strip_layout.setVisibility(View.VISIBLE);
            halfImageActivity.ll_footer_layout.setVisibility(GONE);
            if(list.get(customPosition).isIs_gallery()){
                indicator.setVisibility(GONE);
            }
            textViewWebUrl.setVisibility(GONE);
            halfImageActivity.layoutParentDate.setVisibility(VISIBLE);
            String appLink = "https://com.anyNews.anynews/";
            String mInvitationUrl = list.get(customPosition).getShareLink();
            String msg = list.get(customPosition).getTitle() + " " + mInvitationUrl;
            countDownTimer = new CountDownTimer(2, 2) {
                public void onTick(long millisUntilFinished) {
                }
                public void onFinish() {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    if (from == 1) {
                        intent.setType("text/plain");
                        intent.setPackage("com.whatsapp");
                    }
                    intent.putExtra(Intent.EXTRA_SUBJECT, mInvitationUrl.toString());
                    intent.putExtra(Intent.EXTRA_TEXT, msg);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    Bitmap b = ScreenShott.getInstance().takeScreenShotOfView(halfImageActivity.relative_half_content);
                    //bitmapResize(b);
                    try {
                        File file = new File(context.getExternalCacheDir(), "sample.png");
                        FileOutputStream fOut = new FileOutputStream(file);
                        b.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                        fOut.flush();
                        fOut.close();
                        file.setReadable(true, true);
                        Uri photoURI = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
                        intent.putExtra(Intent.EXTRA_STREAM, photoURI);
                        List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(Intent.createChooser(intent, "Share Link"), PackageManager.MATCH_DEFAULT_ONLY);
                        for (ResolveInfo resolveInfo : resInfoList) {
                            String packageName = resolveInfo.activityInfo.packageName;
                            context.grantUriPermission(packageName, photoURI, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        }
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    halfImageActivity.with_out_strip_layout.setVisibility(View.VISIBLE);
                    halfImageActivity.with_strip_layout.setVisibility(View.GONE);
                    halfImageActivity.layoutParentDate.setVisibility(GONE);
                    halfImageActivity.ll_footer_layout.setVisibility(VISIBLE);
                    textViewWebUrl.setVisibility(VISIBLE);
                    if(list.get(customPosition).isIs_gallery()){
                        indicator.setVisibility(VISIBLE);
                    }
                    intent.setType("image/*");
                    if (context != null && context.getPackageName().equals(BuildConfig.APPLICATION_ID)) {
                        showLog("Halfshare"+ context + "First" + context.getPackageName() + "Second" + BuildConfig.APPLICATION_ID);
                        context.startActivity(Intent.createChooser(intent, "Share Link"));
                    }
                    Share_Count_Value("share", list.get(customPosition).getPostId());
                }
            }.start();


        }

    }
    public void showHalfPostExoPlayerVideo(View convertView, int position) {
        showLog("showNormalVideo:::"+position);
        /* youtube initialize */
        ConstraintLayout layoutParentTop;

        LinearLayout layoutParentTitle;
        LinearLayout layoutParentReportDetails;
        LinearLayout layoutParentMiddle;
        LinearLayout layoutMain = convertView.findViewById(R.id.parent_layout);
        layoutMain.getLayoutParams().height=screenHeight;
        PlayerView  simpleExoPlayerView = convertView.findViewById(R.id.normal_video_player);
        final ImageView thaumbnail_for_video = convertView.findViewById(R.id.thaumbnail_for_video);
        final ImageView imageViewExoVideoPlayVideo = convertView.findViewById(R.id.imageViewVideoPlayVideo);
        final ImageButton fullscreen_button = convertView.findViewById(R.id.fullscreen_button);
        final ImageButton mute_button = convertView.findViewById(R.id.mute_button);
        final ProgressBar progress_bar_for_video = convertView.findViewById(R.id.progress_bar_for_video);
        halfVideoExoPlayerHolder.layoutParentDate = convertView.findViewById(R.id.layoutParentDate);
        TextView textViewPostDate = convertView.findViewById(R.id.textViewPostDate);
        //halfVideoExoPlayerHolder.playButton = convertView.findViewById(R.id.btnPlayYoutubeHalf);

        /* Writer Detailes  */
        halfVideoExoPlayerHolder.writer_time = convertView.findViewById(R.id.writer_time);
        halfVideoExoPlayerHolder.writer_name = convertView.findViewById(R.id.writer_name);
        halfVideoExoPlayerHolder.writer_profile_pic = convertView.findViewById(R.id.writer_profile_pic);
        layoutParentTitle = convertView.findViewById(R.id.layoutParentTitle);
        layoutParentReportDetails = convertView.findViewById(R.id.ll_footer_layout);
        halfVideoExoPlayerHolder.parent_layout = convertView.findViewById(R.id.parent_layout);
        layoutParentMiddle = convertView.findViewById(R.id.layoutParentMiddle);
        halfVideoExoPlayerHolder.strip_text_name = convertView.findViewById(R.id.strip_text_name);
        layoutParentTop = convertView.findViewById(R.id.layoutParentTop);
        halfVideoExoPlayerHolder.layoutParentTop = convertView.findViewById(R.id.layoutParentTop);

        halfVideoExoPlayerHolder.layoutParentTop.getLayoutParams().height = (layoutHeight);


        /*  like */
        final TextView tamil_half_content_description = convertView.findViewById(R.id.tamil_half_content_description__);
        final TextView tamil_half_content_title = convertView.findViewById(R.id.tamil_half_content_title__);
        final TextView english_half_content_description = convertView.findViewById(R.id.english_half_content_description__);
        final TextView english_half_content_title = convertView.findViewById(R.id.english_half_content_title__);
        /*  test detailes */
        final TextView half_content_description = convertView.findViewById(R.id.half_content_description__);
        final TextView half_content_title = convertView.findViewById(R.id.half_content_title__);
        //halfVideoExoPlayerHolder.new_half_youtube_video = convertView.findViewById(R.id.youtube_view_half_video);

        halfVideoExoPlayerHolder.writer_time = convertView.findViewById(R.id.writer_time);
        halfVideoExoPlayerHolder.writer_designation = convertView.findViewById(R.id.writer_designation);
        halfVideoExoPlayerHolder.writer_time = convertView.findViewById(R.id.writer_time);

        /*  click events */
        halfVideoExoPlayerHolder.half_image_share = convertView.findViewById(R.id.half_share__);
        halfVideoExoPlayerHolder.half_comment = convertView.findViewById(R.id.half_comment);


        halfVideoExoPlayerHolder.with_strip_layout = convertView.findViewById(R.id.with_strips_layout);
        halfVideoExoPlayerHolder.with_out_strip_layout = convertView.findViewById(R.id.with_out_strips_layout);

        halfVideoExoPlayerHolder.relative_half_content = convertView.findViewById(R.id.relative_half_contents);
        halfVideoExoPlayerHolder.report = convertView.findViewById(R.id.report);


        halfVideoExoPlayerHolder.half_image_like = convertView.findViewById(R.id.half_image_like);


        halfVideoExoPlayerHolder.half_image_like_icon = convertView.findViewById(R.id.half_image_like_icon);

        halfVideoExoPlayerHolder.half_comment_icon = convertView.findViewById(R.id.half_comment_icon);
        halfVideoExoPlayerHolder.share = convertView.findViewById(R.id.share);
        halfVideoExoPlayerHolder.video_report = convertView.findViewById(R.id.video_report);
        if (halfVideoExoPlayerHolder.share != null) {
            halfVideoExoPlayerHolder.share.setImageResource(R.drawable.ic_action_share);
        }
        if (halfVideoExoPlayerHolder.video_report != null) {
            halfVideoExoPlayerHolder.video_report.setImageResource(R.drawable.ic_action_more_vert);
        }
        bindHalfPostEngagementCounts(convertView, position);
        bindHalfLikeIcon(halfVideoExoPlayerHolder.half_image_like, list.get(position).getLikeStatus());


        halfVideoExoPlayerHolder.writer_follow = convertView.findViewById(R.id.writer_follow);
        halfVideoExoPlayerHolder.writer_follower = convertView.findViewById(R.id.writer_follower);
        final TextView test_folloew = convertView.findViewById(R.id.test_folloew);
        final TextView followingLabel = convertView.findViewById(R.id.second_language_name);
        halfVideoExoPlayerHolder.ll_refer_and_earn_halfy = convertView.findViewById(R.id.ll_refer_and_earn_half);
        halfVideoExoPlayerHolder.iv_share_and_earn = convertView.findViewById(R.id.iv_share_and_earn);
        halfVideoExoPlayerHolder.my_feed = convertView.findViewById(R.id.my_feed);
        halfVideoExoPlayerHolder.parent_layout.setOnClickListener(null);
        hideFeedVideoWriterFooter(convertView);
        TextView textViewWebUrl = convertView.findViewById(R.id.textViewWebUrl);
        TextView textViewStripWebUrl = convertView.findViewById(R.id.textViewStripWebUrl);
        bindFeedWebUrlPill(textViewWebUrl, textViewStripWebUrl, list.get(position), true);
        try {
            WriterStripStyleHelper.apply(
                    context,
                    halfVideoExoPlayerHolder.writer_name,
                    halfVideoExoPlayerHolder.writer_designation,
                    halfVideoExoPlayerHolder.writer_time,
                    halfVideoExoPlayerHolder.my_feed,
                    halfVideoExoPlayerHolder.ll_refer_and_earn_halfy);
        } catch (Exception ignored) {
        }
        bindLocationPill(
                halfVideoExoPlayerHolder.my_feed,
                halfVideoExoPlayerHolder.iv_share_and_earn,
                halfVideoExoPlayerHolder.ll_refer_and_earn_halfy,
                list.get(position));
        textViewPostDate.setText(list.get(position).getPost_date_time());

        if (LanguageId == 4) {

            english_half_content_description.setVisibility(View.GONE);
            english_half_content_title.setVisibility(View.GONE);

            half_content_description.setVisibility(View.GONE);
            half_content_title.setVisibility(View.GONE);

            tamil_half_content_description.setVisibility(View.VISIBLE);
            tamil_half_content_title.setVisibility(View.VISIBLE);

            tamil_half_content_description.setText("" + list.get(position).getDescription());
            tamil_half_content_title.setText("" + list.get(position).getTitle());
            halfVideoExoPlayerHolder.strip_text_name.setText("" + list.get(position).getStrip_content());
            applyFeedArticleFonts(tamil_half_content_title, tamil_half_content_description);
            applyFeedStripFont(halfVideoExoPlayerHolder.strip_text_name);

        } else if (LanguageId == 1) {

            tamil_half_content_description.setVisibility(View.GONE);
            half_content_title.setVisibility(View.GONE);
            half_content_description.setVisibility(View.GONE);
            half_content_title.setVisibility(View.GONE);

            english_half_content_description.setVisibility(View.VISIBLE);
            english_half_content_title.setVisibility(View.VISIBLE);

            english_half_content_description.setText("" + list.get(position).getDescription());
            english_half_content_title.setText("" + list.get(position).getTitle());
            halfVideoExoPlayerHolder.strip_text_name.setText("" + list.get(position).getStrip_content());
            applyFeedArticleFonts(english_half_content_title, english_half_content_description);
            applyFeedStripFont(halfVideoExoPlayerHolder.strip_text_name);

        } else {
            english_half_content_description.setVisibility(View.GONE);
            english_half_content_title.setVisibility(View.GONE);
            tamil_half_content_description.setVisibility(View.GONE);
            half_content_title.setVisibility(View.GONE);
            half_content_description.setVisibility(View.VISIBLE);
            half_content_title.setVisibility(View.VISIBLE);
            half_content_description.setText(""+list.get(position).getDescription());
            half_content_title.setText("" + list.get(position).getTitle());
            halfVideoExoPlayerHolder.strip_text_name.setText("" + list.get(position).getStrip_content());
            applyFeedArticleFonts(half_content_title, half_content_description);
            applyFeedStripFont(halfVideoExoPlayerHolder.strip_text_name);
        }
        bindWriterStripIdentity(
                list.get(position),
                halfVideoExoPlayerHolder.writer_name,
                halfVideoExoPlayerHolder.writer_designation,
                halfVideoExoPlayerHolder.writer_time);
        int width = displayMetrics.widthPixels;
        //halfVideoExoPlayerHolder.imageViewItems.setVisibility(View.VISIBLE);
        //halfVideoExoPlayerHolder.youTubePlayerView.setVisibility(View.GONE);



        // Keep live picture if this card already owns the player; otherwise idle poster + play.
        boolean halfExoLive = simpleExoPlayerView.getPlayer() != null
                && simpleExoPlayerView.getPlayer() == exoPlayer
                && exoPlayer != null
                && exoPlayer.getPlayWhenReady()
                && exoPlayer.getPlaybackState() != Player.STATE_ENDED
                && exoPlayer.getPlaybackState() != Player.STATE_IDLE;
        if (halfExoLive) {
            simpleExoPlayerView.setVisibility(View.VISIBLE);
            thaumbnail_for_video.setVisibility(View.GONE);
            imageViewExoVideoPlayVideo.setVisibility(View.GONE);
            updateLeanExoCenterControlIcons(simpleExoPlayerView, true);
        } else {
            Player attachedHalf = simpleExoPlayerView.getPlayer();
            if (attachedHalf != null && attachedHalf != exoPlayer) {
                try {
                    attachedHalf.release();
                } catch (Exception ignored) {
                }
                simpleExoPlayerView.setPlayer(null);
            } else if (attachedHalf != null && !attachedHalf.getPlayWhenReady()) {
                // Detach idle scrap player so black surface does not cover poster.
                simpleExoPlayerView.setPlayer(null);
            }
            simpleExoPlayerView.setVisibility(View.GONE);
            showLeanExoIdlePoster(thaumbnail_for_video, imageViewExoVideoPlayVideo,
                    StatusVideoMedia.resolveThumbnailUrl(list.get(position)));
        }

        //....................................................Upload video..............................
        layoutParentTitle.setVisibility(View.VISIBLE);
        layoutParentReportDetails.setVisibility(View.VISIBLE);
        layoutParentMiddle.setVisibility(View.VISIBLE);

        layoutParentTop.getLayoutParams().height=layoutHeight;
        // Do not touch resize mode on a live surface — clears TextureView on low-end.
        if (!halfExoLive) {
            simpleExoPlayerView.setResizeMode(RESIZE_MODE_FILL);
        }

        mute_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(exoPlayer!=null){
                    showLog("exoplayer volume :"+exoPlayer.getVolume());
                    exoPlayer.setVolume(exoPlayer.getVolume()==0f? 1f:0f);
                    if(exoPlayer.getVolume()==0f){
                        mute_button.setImageResource(R.drawable.ic_exo_volume_down);
                    }else{
                        mute_button.setImageResource(R.drawable.ic_volume_on);
                    }
                }
            }
        });

        fullscreen_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLog("fullscreen_button::"+fullscreen);
                showExoPlayerInFullScreen(fullscreen,simpleExoPlayerView,layoutParentTop,layoutParentMiddle,layoutParentReportDetails,layoutParentTitle);
            }
        });
        if (!halfExoLive) {
            progress_bar_for_video.setVisibility(View.GONE);
        }
        imageViewExoVideoPlayVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fullscreen = false;
                textViewWebUrl.setVisibility(View.GONE);
                mute_button.setImageResource(ic__volume_up);
                String videoUrl = StatusVideoMedia.resolveStreamUrl(list.get(position));
                if (TextUtils.isEmpty(videoUrl)) {
                    videoUrl = list.get(position).getVideoUrl();
                }
                if (TextUtils.isEmpty(videoUrl)) {
                    Toast.makeText(context, "Video is not available", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (exoPlayer != null
                        && simpleExoPlayerView.getPlayer() == exoPlayer
                        && exoPlayer.getPlaybackState() != Player.STATE_ENDED
                        && exoPlayer.getPlaybackState() != Player.STATE_IDLE) {
                    imageViewExoVideoPlayVideo.setVisibility(View.GONE);
                    thaumbnail_for_video.setVisibility(View.GONE);
                    progress_bar_for_video.setVisibility(View.GONE);
                    simpleExoPlayerView.setVisibility(View.VISIBLE);
                    exoPlayer.setPlayWhenReady(true);
                    try {
                        exoPlayer.play();
                    } catch (Exception ignored) {
                    }
                    updateLeanExoCenterControlIcons(simpleExoPlayerView, true);
                    return;
                }
                imageViewExoVideoPlayVideo.setVisibility(View.GONE);
                thaumbnail_for_video.setVisibility(View.VISIBLE);
                progress_bar_for_video.setVisibility(View.VISIBLE);
                showLeanExoIdlePoster(thaumbnail_for_video, null,
                        StatusVideoMedia.resolveThumbnailUrl(list.get(position)));
                imageViewExoVideoPlayVideo.setVisibility(View.GONE);
                simpleExoPlayerView.setVisibility(View.VISIBLE);
                if (exoPlayer != null) {
                    exoPlayer.setPlayWhenReady(false);
                    exoPlayer.stop();
                    exoPlayer.release();
                    exoPlayer = null;
                }
                exoPlayer = new ExoPlayer.Builder(context).build();
                simpleExoPlayerView.setPlayer(exoPlayer);
                simpleExoPlayerView.setResizeMode(RESIZE_MODE_FIT);
                showLog("Video URL:" + videoUrl);
                prepareFeedExoPlayer(exoPlayer, videoUrl);
                exoPlayer.setPlayWhenReady(true);
                wireLeanExoChrome(simpleExoPlayerView, null);
                exoPlayer.addListener(new Player.Listener() {
                    @Override
                    public void onPlaybackStateChanged(int playbackState) {
                        if (exoPlayer == null) {
                            return;
                        }
                        boolean playWhenReady = exoPlayer.getPlayWhenReady();
                        showLog("onPlayerStateChanged half::" + playWhenReady + " state=" + playbackState);
                        applyLeanExoOverlayState(
                                playbackState,
                                playWhenReady,
                                thaumbnail_for_video,
                                imageViewExoVideoPlayVideo,
                                progress_bar_for_video,
                                simpleExoPlayerView);
                    }
                });
            }
        });




        //.......................................................DONE....................................


        //....................................................Youtube Video...............................
        if(youTubePlayerViewHalf!=null){
            youTubePlayerViewHalf.pause();
            //youTubePlayerViewHalf=null;
        }

        //....................................................DONE........................................

        applyWriterFollowState(
                halfVideoExoPlayerHolder.writer_follow,
                halfVideoExoPlayerHolder.writer_follower,
                test_folloew,
                followingLabel,
                position);
        hideFeedVideoWriterFooter(convertView);

        bindHalfLikeIcon(halfVideoExoPlayerHolder.half_image_like, list.get(position).getLikeStatus());

        // check null
        if (halfVideoExoPlayerHolder.strip_text_name.getText().toString().matches("null")) {
            halfVideoExoPlayerHolder.strip_text_name.setText("" + " The Best Regional News App ");
        }

                           /* final Animation myAnimHalfy = AnimationUtils.loadAnimation(context, R.anim.bounce);
                            double animationDurationHalfy = 8 * 1000;
                            myAnimHalfy.setDuration((long) animationDurationHalfy);

                            // Use custom animation interpolator to achieve the bounce effect
                            MyBounceInterpolator interpolatorHalfy = new MyBounceInterpolator(0.10, 20.00);

                            myAnimHalfy.setInterpolator(interpolatorHalfy);

                            halfVideoExoPlayerHolder.ll_refer_and_earn_halfy.startAnimation(myAnimHalfy);*/


        bindDistrictOrRewardsClick(
                halfVideoExoPlayerHolder.ll_refer_and_earn_halfy, list.get(position));

        //
                        /*Picasso.with(context).load(list.get(position).getWriterProfile()).skipMemoryCache()
                                .placeholder(R.drawable.ic_user_profile).into(halfVideoExoPlayerHolder.writer_profile_pic);*/
        if (halfVideoExoPlayerHolder.writer_profile_pic.getTag() == null ||
                !halfVideoExoPlayerHolder.writer_profile_pic.getTag().equals(list.get(position).getWriterProfile())) {
            // below method will display image inside our image view..
            imageLoader.displayImage(list.get(position).getWriterProfile(), halfVideoExoPlayerHolder.writer_profile_pic, options, null);

        }
        ((Activity) convertView.getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        showLog("IMAGE COVER PAGE: " + list.get(position).getThumbnail() + " , " + list.get(position).getVideoUrl());
        halfVideoExoPlayerHolder.writer_profile_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWriterProfileFromFeed(position);
            }
        });

        // click listener
        halfVideoExoPlayerHolder.report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                AlertDialog.Builder builder = new AlertDialog.Builder(context);

                LayoutInflater inflater = LayoutInflater.from(context);
                View dialogView = inflater.inflate(R.layout.custom_dialog_layout, null);

                builder.setView(dialogView);

                RadioButton one = (RadioButton) dialogView.findViewById(R.id.one);
                RadioButton two = (RadioButton) dialogView.findViewById(R.id.two);
                RadioButton three = (RadioButton) dialogView.findViewById(R.id.three);
                RadioButton four = (RadioButton) dialogView.findViewById(R.id.four);

                TextView report = dialogView.findViewById(R.id.report);
                TextView title = dialogView.findViewById(R.id.title);
                EditText full_image_edt_news_headlines = dialogView.findViewById(R.id.full_image_edt_news_headlines);
                Button yes = dialogView.findViewById(R.id.yes);
                ImageView close_dailog = dialogView.findViewById(R.id.close_dailog);


                // Create the alert dialog
                final AlertDialog dialog = builder.create();

                JsonObject updateparams = ReportParams();

                close_dailog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });

                APIService apiService = RestApiClient.getClient().create(APIService.class);
                Call<GetReportIssueResponce> update = apiService.reportissue(updateparams);
                update.enqueue(new Callback<GetReportIssueResponce>() {

                    @Override
                    public void onResponse(Call<GetReportIssueResponce> call, retrofit2.Response<GetReportIssueResponce> response) {

                        GetReportIssueResponce updateResponce = response.body();

                        one.setText("" + updateResponce.getData().getOptions().get(0));
                        two.setText("" + updateResponce.getData().getOptions().get(1));
                        three.setText("" + updateResponce.getData().getOptions().get(2));
                        four.setText("" + updateResponce.getData().getOptions().get(3));
                        report.setText("" + updateResponce.getData().getTitle());
                        title.setText("" + updateResponce.getData().getOtherReason());
                        full_image_edt_news_headlines.setHint("" + updateResponce.getData().getPlaceholder());

                        yes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                String resoin;
                                if (one.isChecked()) {
                                    resoin = one.getText().toString();

                                } else if (two.isChecked()) {
                                    resoin = one.getText().toString();

                                } else if (three.isChecked()) {
                                    resoin = one.getText().toString();

                                } else {

                                    resoin = one.getText().toString();

                                }

                                JsonObject updateparams = DailogRequest(resoin, list.get(position).getPostId(), full_image_edt_news_headlines.getText().toString());


                                APIService apiService = RestApiClient.getClient().create(APIService.class);
                                Call<DailogSendRespouse> update = apiService.dailogSend(updateparams);


                                update.enqueue(new Callback<DailogSendRespouse>() {

                                    @Override
                                    public void onResponse(Call<DailogSendRespouse> call, retrofit2.Response<DailogSendRespouse> response) {

                                        DailogSendRespouse updateResponce = response.body();


                                        if (updateResponce.getSuccess()) {

                                            Toast.makeText(context, "" + updateResponce.getMsg(), Toast.LENGTH_SHORT).show();

                                        } else {
                                            Toast.makeText(context, "" + updateResponce.getMsg(), Toast.LENGTH_SHORT).show();


                                        }
                                        dialog.dismiss();
                                    }

                                    @Override
                                    public void onFailure(Call<DailogSendRespouse> call, Throwable t) {


                                    }
                                });
                            }
                        });


                    }

                    @Override
                    public void onFailure(Call<GetReportIssueResponce> call, Throwable t) {


                    }
                });


                dialog.show();


            }
        });


        View finalConvertView2 = convertView;


        halfVideoExoPlayerHolder.half_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent("COMMENTS_PAGE");
                intent.putExtra("message", "click_event");

                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                try {
                    adapterCallback.OpenComments(list.get(position).getPostId(), list.get(position).getSubPostId());
                } catch (ClassCastException e) {
                    e.printStackTrace();
                    // do something
                }
            }
        });


        View finalConvertView3 = convertView;
        View.OnClickListener halfExoLikeClick = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                halfVideoExoPlayerHolder.half_image_like = finalConvertView3.findViewById(R.id.half_image_like);
                TextView likeCount = finalConvertView3.findViewById(R.id.half_like_count);
                int newStatus = applyLikeChange(position, likeCount);
                bindHalfLikeIcon(halfVideoExoPlayerHolder.half_image_like, newStatus);
            }
        };
        if (halfVideoExoPlayerHolder.half_image_like_icon != null) {
            halfVideoExoPlayerHolder.half_image_like_icon.setOnClickListener(halfExoLikeClick);
        }
        if (halfVideoExoPlayerHolder.half_image_like != null) {
            halfVideoExoPlayerHolder.half_image_like.setOnClickListener(halfExoLikeClick);
        }

        halfVideoExoPlayerHolder.writer_follower.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent("WRITER_FOLLOWER");
                intent.putExtra("message", "click_event");

                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

                if (writerFollowUiState(list.get(position).getIsFollow()) == 0) {

                    halfVideoExoPlayerHolder.writer_follow.setVisibility(View.GONE);
                    halfVideoExoPlayerHolder.writer_follower.setVisibility(View.VISIBLE);
                    test_folloew.setText("Following");
                    list.get(position).setIsFollow(1);

                    set_follow_un_follow(list.get(position).getWriterId(), 1);


                } else {

                    halfVideoExoPlayerHolder.writer_follow.setVisibility(View.VISIBLE);
                    halfVideoExoPlayerHolder.writer_follower.setVisibility(View.GONE);

                    test_folloew.setText("Follow");
                    list.get(position).setIsFollow(0);

                    set_follow_un_follow(list.get(position).getWriterId(), 0);

                }

            }
        });

        halfVideoExoPlayerHolder.writer_follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                halfVideoExoPlayerHolder.writer_follow = finalConvertView3.findViewById(R.id.writer_follow);
                halfVideoExoPlayerHolder.writer_follower = finalConvertView3.findViewById(R.id.writer_follower);
                halfVideoExoPlayerHolder.test_folloew = finalConvertView3.findViewById(R.id.test_folloew);

                Intent intent = new Intent("WRITER_FOLLOW");
                intent.putExtra("message", "click_event");

                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

                if (writerFollowUiState(list.get(position).getIsFollow()) == 0) {

                    halfVideoExoPlayerHolder.writer_follow.setVisibility(View.GONE);
                    halfVideoExoPlayerHolder.writer_follower.setVisibility(View.VISIBLE);
                    test_folloew.setText("Following");
                    list.get(position).setIsFollow(1);

                    set_follow_un_follow(list.get(position).getWriterId(), 1);

                } else {

                    halfVideoExoPlayerHolder.writer_follow.setVisibility(View.VISIBLE);
                    halfVideoExoPlayerHolder.writer_follower.setVisibility(View.GONE);
                    test_folloew.setText("Follow");
                    list.get(position).setIsFollow(0);

                    set_follow_un_follow(list.get(position).getWriterId(), 0);

                }

            }
        });


        final RelativeLayout relativeLayoutShareOpt = convertView.findViewById(R.id.relativeLayoutShareOpt);
        final  TextView textViewAllPage = convertView.findViewById(R.id.textViewAllPage);
        final  TextView textViewSinglePage = convertView.findViewById(R.id.textViewSinglePage);
        final ImageView  imageViewShareClose = convertView.findViewById(R.id.imageViewShareClose);

        final ImageView  imageViewShareSingle = convertView.findViewById(R.id.imageViewShareSingle);
        final ImageView  imageViewShareAll = convertView.findViewById(R.id.imageViewShareAll);
        imageViewShareSingle.setImageResource(R.drawable.ic_share_download);
        imageViewShareAll.setImageResource(R.drawable.ic_share_all);

        relativeLayoutShareOpt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLog("Hello");
            }
        });

        textViewSinglePage.setText("Share Article");
        textViewAllPage.setText("Share Video");

        //imageGallaryHolder.galleryLayout.setLayerType(View.LAYER_TYPE_NONE,null);

        halfVideoExoPlayerHolder.half_image_share.setOnClickListener(view -> {
            if(relativeLayoutShareOpt.getVisibility()==VISIBLE){
                relativeLayoutShareOpt.setVisibility(GONE);
            }else {
                relativeLayoutShareOpt.setVisibility(VISIBLE);
            }
        });

        relativeLayoutShareOpt.setVisibility(GONE);
        imageViewShareClose.setOnClickListener(view -> relativeLayoutShareOpt.setVisibility(GONE));
        textViewAllPage.setOnClickListener(view -> {
            relativeLayoutShareOpt.setVisibility(GONE);
            shareVideoDirect(position, VideoShareHelper.ShareMode.GENERAL);
        });
        textViewSinglePage.setOnClickListener(view -> {
            relativeLayoutShareOpt.setVisibility(GONE);
            shareExoPlayerVideoScreenShot(finalConvertView2, position, 0, simpleExoPlayerView, thaumbnail_for_video, imageViewExoVideoPlayVideo, progress_bar_for_video);
        });


    }


    public void showHalfPostYoutubeVideo(View convertView, int position) {
        showLog("showNormalVideo:::"+position);
        ConstraintLayout layoutParentTop;
        LinearLayout layoutParentTitle;
        LinearLayout layoutParentMiddle;
        LinearLayout layoutMain = convertView.findViewById(R.id.parent_layout);
        layoutMain.getLayoutParams().height=screenHeight;
       // final ProgressBar progress_bar_for_video = convertView.findViewById(R.id.progress_bar_for_video);
        ImageView imageViewItems = convertView.findViewById(R.id.imageViewItem__);
        half_youTubePlayerHolder.layoutParentDate = convertView.findViewById(R.id.layoutParentDate);
        TextView textViewPostDate = convertView.findViewById(R.id.textViewPostDate);
        //half_youTubePlayerHolder.playButton = convertView.findViewById(R.id.btnPlayYoutubeHalf);
        YouTubePlayerView youTubePlayerView = convertView.findViewById(R.id.youtube_view_half_video);
        /* Writer Detailes  */
        half_youTubePlayerHolder.writer_time = convertView.findViewById(R.id.writer_time);
        half_youTubePlayerHolder.writer_name = convertView.findViewById(R.id.writer_name);
        half_youTubePlayerHolder.writer_profile_pic = convertView.findViewById(R.id.writer_profile_pic);
        half_youTubePlayerHolder.btnMuteHalf = convertView.findViewById(R.id.btnMute);
        layoutParentTitle = convertView.findViewById(R.id.layoutParentTitle);
        final LinearLayout  layoutParentReportDetails = convertView.findViewById(R.id.ll_footer_layout);
        final LinearLayout parent_layout = convertView.findViewById(R.id.parent_layout);
        layoutParentMiddle = convertView.findViewById(R.id.layoutParentMiddle);
        half_youTubePlayerHolder.strip_text_name = convertView.findViewById(R.id.strip_text_name);
        layoutParentTop = convertView.findViewById(R.id.layoutParentTop);
        half_youTubePlayerHolder.layoutParentTop = convertView.findViewById(R.id.layoutParentTop);
        ImageView imageViewYoutubePlayVideo = convertView.findViewById(R.id.imageViewPlayVideo);
        if (half_youTubePlayerHolder.layoutParentTop != null
                && half_youTubePlayerHolder.layoutParentTop.getLayoutParams() != null) {
            half_youTubePlayerHolder.layoutParentTop.getLayoutParams().height = layoutHeight;
        }
        if (imageViewItems != null) {
            imageViewItems.setVisibility(VISIBLE);
        }
        if (youTubePlayerView != null) {
            youTubePlayerView.setVisibility(GONE);
        }

        /*  like */
        final TextView tamil_half_content_description = convertView.findViewById(R.id.tamil_half_content_description__);
        final TextView tamil_half_content_title = convertView.findViewById(R.id.tamil_half_content_title__);
        final TextView english_half_content_description = convertView.findViewById(R.id.english_half_content_description__);
        final TextView english_half_content_title = convertView.findViewById(R.id.english_half_content_title__);
        /*  test detailes */
        final TextView half_content_description = convertView.findViewById(R.id.half_content_description__);
        final TextView half_content_title = convertView.findViewById(R.id.half_content_title__);
        //half_youTubePlayerHolder.new_half_youtube_video = convertView.findViewById(R.id.youtube_view_half_video);

        half_youTubePlayerHolder.writer_time = convertView.findViewById(R.id.writer_time);
        half_youTubePlayerHolder.writer_designation = convertView.findViewById(R.id.writer_designation);
        half_youTubePlayerHolder.writer_time = convertView.findViewById(R.id.writer_time);

        /*  click events */
         half_youTubePlayerHolder.half_image_share = convertView.findViewById(R.id.half_share__);
        half_youTubePlayerHolder.half_comment = convertView.findViewById(R.id.half_comment);


        half_youTubePlayerHolder.with_strip_layout = convertView.findViewById(R.id.with_strips_layout);
        half_youTubePlayerHolder.with_out_strip_layout = convertView.findViewById(R.id.with_out_strips_layout);

        half_youTubePlayerHolder.relative_half_content = convertView.findViewById(R.id.relative_half_contents);
        half_youTubePlayerHolder.report = convertView.findViewById(R.id.report);


        half_youTubePlayerHolder.half_image_like = convertView.findViewById(R.id.half_image_like);


        half_youTubePlayerHolder.half_image_like_icon = convertView.findViewById(R.id.half_image_like_icon);
        bindHalfPostEngagementCounts(convertView, position);
        bindHalfLikeIcon(half_youTubePlayerHolder.half_image_like, list.get(position).getLikeStatus());


        half_youTubePlayerHolder.writer_follow = convertView.findViewById(R.id.writer_follow);
        half_youTubePlayerHolder.writer_follower = convertView.findViewById(R.id.writer_follower);
        final TextView test_folloew = convertView.findViewById(R.id.test_folloew);
        final TextView followingLabel = convertView.findViewById(R.id.second_language_name);
        half_youTubePlayerHolder.ll_refer_and_earn_halfy = convertView.findViewById(R.id.ll_refer_and_earn_half);
        half_youTubePlayerHolder.iv_share_and_earn = convertView.findViewById(R.id.iv_share_and_earn);
        half_youTubePlayerHolder.my_feed = convertView.findViewById(R.id.my_feed);
        parent_layout.setOnClickListener(null);
        hideFeedVideoWriterFooter(convertView);
        showLog("showNormalVideo"+ "after thubnails");
        TextView textViewWebUrl = convertView.findViewById(R.id.textViewWebUrl);
        TextView textViewStripWebUrl = convertView.findViewById(R.id.textViewStripWebUrl);
        bindFeedWebUrlPill(textViewWebUrl, textViewStripWebUrl, list.get(position), true);
        try {
            WriterStripStyleHelper.apply(
                    context,
                    half_youTubePlayerHolder.writer_name,
                    half_youTubePlayerHolder.writer_designation,
                    half_youTubePlayerHolder.writer_time,
                    half_youTubePlayerHolder.my_feed,
                    half_youTubePlayerHolder.ll_refer_and_earn_halfy);
        } catch (Exception ignored) {
        }
        bindLocationPill(
                half_youTubePlayerHolder.my_feed,
                half_youTubePlayerHolder.iv_share_and_earn,
                half_youTubePlayerHolder.ll_refer_and_earn_halfy,
                list.get(position));
        textViewPostDate.setText(list.get(position).getPost_date_time());

        if (LanguageId == 4) {

            english_half_content_description.setVisibility(View.GONE);
            english_half_content_title.setVisibility(View.GONE);

            half_content_description.setVisibility(View.GONE);
            half_content_title.setVisibility(View.GONE);

            tamil_half_content_description.setVisibility(View.VISIBLE);
            tamil_half_content_title.setVisibility(View.VISIBLE);

            tamil_half_content_description.setText("" + list.get(position).getDescription());
            tamil_half_content_title.setText("" + list.get(position).getTitle());
            half_youTubePlayerHolder.strip_text_name.setText("" + list.get(position).getStrip_content());
            applyFeedArticleFonts(tamil_half_content_title, tamil_half_content_description);
            applyFeedStripFont(half_youTubePlayerHolder.strip_text_name);

        } else if (LanguageId == 1) {

            tamil_half_content_description.setVisibility(View.GONE);
            half_content_title.setVisibility(View.GONE);
            half_content_description.setVisibility(View.GONE);
            half_content_title.setVisibility(View.GONE);

            english_half_content_description.setVisibility(View.VISIBLE);
            english_half_content_title.setVisibility(View.VISIBLE);

            english_half_content_description.setText("" + list.get(position).getDescription());
            english_half_content_title.setText("" + list.get(position).getTitle());
            half_youTubePlayerHolder.strip_text_name.setText("" + list.get(position).getStrip_content());
            applyFeedArticleFonts(english_half_content_title, english_half_content_description);
            applyFeedStripFont(half_youTubePlayerHolder.strip_text_name);

        } else {
            english_half_content_description.setVisibility(View.GONE);
            english_half_content_title.setVisibility(View.GONE);
            tamil_half_content_description.setVisibility(View.GONE);
            half_content_title.setVisibility(View.GONE);
            half_content_description.setVisibility(View.VISIBLE);
            half_content_title.setVisibility(View.VISIBLE);
            half_content_description.setText(""+list.get(position).getDescription());
            half_content_title.setText("" + list.get(position).getTitle());
            half_youTubePlayerHolder.strip_text_name.setText("" + list.get(position).getStrip_content());
            applyFeedArticleFonts(half_content_title, half_content_description);
            applyFeedStripFont(half_youTubePlayerHolder.strip_text_name);
        }
        bindWriterStripIdentity(
                list.get(position),
                half_youTubePlayerHolder.writer_name,
                half_youTubePlayerHolder.writer_designation,
                half_youTubePlayerHolder.writer_time);
        int width = displayMetrics.widthPixels;
        //half_youTubePlayerHolder.imageViewItems.setVisibility(View.VISIBLE);
        //half_youTubePlayerHolder.youTubePlayerView.setVisibility(View.GONE);

        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(false);
            exoPlayer.stop();
            exoPlayer.seekTo(0);
            //youTubePlayerViewHalf.cueVideo(videoURL,0);
        }
        showLog("showNormalVideo"+ "after thubnails 2");

        if (list.get(position).getTitle() != null) {
            if (imageViewItems != null
                    && list.get(position).getVideoUrl() != null
                    && isValidContextForGlide(context)) {
                imageLoader.displayImage(
                        list.get(position).getThumbnail(), imageViewItems, options, animationListener);
            }
        }
        if (!isYoutubeVideoIsInitialized && imageViewItems != null) {
            imageViewItems.setVisibility(View.VISIBLE);
        }

        isYoutubeVideoIsInitialized=false;
        imageViewYoutubePlayVideo.setImageResource(R.drawable.ic_way2_play_circle);
        imageViewYoutubePlayVideo.setVisibility(View.VISIBLE);

        //....................................................Upload video..............................
        layoutParentTitle.setVisibility(View.VISIBLE);
        layoutParentReportDetails.setVisibility(View.VISIBLE);
        layoutParentMiddle.setVisibility(View.VISIBLE);

        layoutParentTop.getLayoutParams().height=layoutHeight;


      //  progress_bar_for_video.setVisibility(View.GONE);
        if(youTubePlayerViewHalf!=null){
            youTubePlayerViewHalf.pause();
            //youTubePlayerViewHalf=null;
        }
        showLog("showNormalVideo"+ "after thubnails 3");
        imageViewYoutubePlayVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLog("youTubePlayerView::onClick " );
                if (true) {
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (youTubePlayerView == null) {
                                return;
                            }
                            textViewWebUrl.setVisibility(View.GONE);
                            isYoutubeVideoIsInitialized=true;
                            if (imageViewItems != null) {
                                imageViewItems.setVisibility(View.GONE);
                            }
                            youTubePlayerView.setVisibility(View.VISIBLE);
                            View v = youTubePlayerView.inflateCustomPlayerUi(R.layout.youtube_custom_player_ui);
                            YouTubePlayerListener listener = new com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener() {
                                @Override
                                public void onReady(@NonNull com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer youTubePlayer) {
                                    layoutParentTitle.setVisibility(View.VISIBLE);
                                    layoutParentReportDetails.setVisibility(View.VISIBLE);
                                    layoutParentMiddle.setVisibility(View.VISIBLE);
                                    customPlayerUiController = new CustomPlayerUiController(context, v, youTubePlayer, youTubePlayerView, layoutHeight,true, new CustomPlayerUiController.ShowFullScreenVideo() {
                                        @Override
                                        public void showFullScreenVideo(boolean b) {
                                            showLog("youTubePlayerView::" + youTubePlayerView.getLayoutParams().height);
                                            if (b) {
                                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                                                layoutParentTop.setLayoutParams(params);
                                                //youTubePlayerView.enterFullScreen();
                                                layoutParentTitle.setVisibility(View.GONE);
                                                layoutParentReportDetails.setVisibility(View.GONE);
                                                layoutParentMiddle.setVisibility(View.GONE);

                                            } else {
                                                layoutParentTitle.setVisibility(View.VISIBLE);
                                                layoutParentReportDetails.setVisibility(View.VISIBLE);
                                                layoutParentMiddle.setVisibility(View.VISIBLE);
                                                //youTubePlayerView.exitFullScreen();
                                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, layoutHeight);
                                                ConstraintLayout.LayoutParams params1 = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                                                layoutParentTop.setLayoutParams(params);
                                                youTubePlayerView.setLayoutParams(params1);

                                            }
                                            showLog("youTubePlayerView::" + youTubePlayerView.getLayoutParams().height);
                                        }

                                        @Override
                                        public void showUIControls(boolean b) {
                                        }

                                        @Override
                                        public void showProgressOnMainPage(boolean b) {
                                        }
                                    });
                                    youTubePlayer.addListener(customPlayerUiController);
                                    youTubePlayerViewHalf = youTubePlayer;
                                    YouTubePlayerUtils.loadOrCueVideo(
                                            youTubePlayer, lifecycle,
                                            list.get(position).getVideoUrl(), 0f
                                    );

                                }
                            };
                            IFramePlayerOptions options = new IFramePlayerOptions.Builder(context).controls(0).build();
                            youTubePlayerView.initialize(listener, false, options);

                        }
                    }, 100);
                }

            }
        });
        //....................................................DONE........................................

        applyWriterFollowState(
                half_youTubePlayerHolder.writer_follow,
                half_youTubePlayerHolder.writer_follower,
                test_folloew,
                followingLabel,
                position);
        hideFeedVideoWriterFooter(convertView);

        bindHalfLikeIcon(half_youTubePlayerHolder.half_image_like, list.get(position).getLikeStatus());

        // check null
        if (half_youTubePlayerHolder.strip_text_name.getText().toString().matches("null")) {
            half_youTubePlayerHolder.strip_text_name.setText("" + " The Best Regional News App ");
        }
        bindDistrictOrRewardsClick(
                half_youTubePlayerHolder.ll_refer_and_earn_halfy, list.get(position));

        //
                        /*Picasso.with(context).load(list.get(position).getWriterProfile()).skipMemoryCache()
                                .placeholder(R.drawable.ic_user_profile).into(half_youTubePlayerHolder.writer_profile_pic);*/
        if (half_youTubePlayerHolder.writer_profile_pic.getTag() == null ||
                !half_youTubePlayerHolder.writer_profile_pic.getTag().equals(list.get(position).getWriterProfile())) {

            // below method will display image inside our image view..
            imageLoader.displayImage(list.get(position).getWriterProfile(), half_youTubePlayerHolder.writer_profile_pic, options, null);

        }


        ((Activity) convertView.getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);


        showLog("IMAGE COVER PAGE: " + list.get(position).getThumbnail() + " , " + list.get(position).getVideoUrl());


        // mute
        half_youTubePlayerHolder.btnMuteHalf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLog(" b  -->" + isMutedHalf);
                if (!isMutedHalf) {
                    AudioManager mAudioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
                    half_youTubePlayerHolder.btnMuteHalf.setImageResource(ic_volume_off);
                    isMutedHalf = true;
                } else {
                    AudioManager audio = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                    int maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                    int seventyVolume = (int) (maxVolume * percent);
                    audio.setStreamVolume(AudioManager.STREAM_MUSIC, seventyVolume, 0);
                    half_youTubePlayerHolder.btnMuteHalf.setImageResource(ic__volume_up);
                    isMutedHalf = false;
                }

            }
        });


        half_youTubePlayerHolder.writer_profile_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWriterProfileFromFeed(position);
            }
        });

        // click listener
        half_youTubePlayerHolder.report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                AlertDialog.Builder builder = new AlertDialog.Builder(context);

                LayoutInflater inflater = LayoutInflater.from(context);
                View dialogView = inflater.inflate(R.layout.custom_dialog_layout, null);

                builder.setView(dialogView);

                RadioButton one = (RadioButton) dialogView.findViewById(R.id.one);
                RadioButton two = (RadioButton) dialogView.findViewById(R.id.two);
                RadioButton three = (RadioButton) dialogView.findViewById(R.id.three);
                RadioButton four = (RadioButton) dialogView.findViewById(R.id.four);

                TextView report = dialogView.findViewById(R.id.report);
                TextView title = dialogView.findViewById(R.id.title);
                EditText full_image_edt_news_headlines = dialogView.findViewById(R.id.full_image_edt_news_headlines);
                Button yes = dialogView.findViewById(R.id.yes);
                ImageView close_dailog = dialogView.findViewById(R.id.close_dailog);


                // Create the alert dialog
                final AlertDialog dialog = builder.create();

                JsonObject updateparams = ReportParams();

                close_dailog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });

                APIService apiService = RestApiClient.getClient().create(APIService.class);
                Call<GetReportIssueResponce> update = apiService.reportissue(updateparams);
                update.enqueue(new Callback<GetReportIssueResponce>() {

                    @Override
                    public void onResponse(Call<GetReportIssueResponce> call, retrofit2.Response<GetReportIssueResponce> response) {

                        GetReportIssueResponce updateResponce = response.body();

                        one.setText("" + updateResponce.getData().getOptions().get(0));
                        two.setText("" + updateResponce.getData().getOptions().get(1));
                        three.setText("" + updateResponce.getData().getOptions().get(2));
                        four.setText("" + updateResponce.getData().getOptions().get(3));
                        report.setText("" + updateResponce.getData().getTitle());
                        title.setText("" + updateResponce.getData().getOtherReason());
                        full_image_edt_news_headlines.setHint("" + updateResponce.getData().getPlaceholder());

                        yes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                String resoin;
                                if (one.isChecked()) {
                                    resoin = one.getText().toString();

                                } else if (two.isChecked()) {
                                    resoin = one.getText().toString();

                                } else if (three.isChecked()) {
                                    resoin = one.getText().toString();

                                } else {

                                    resoin = one.getText().toString();

                                }

                                JsonObject updateparams = DailogRequest(resoin, list.get(position).getPostId(), full_image_edt_news_headlines.getText().toString());


                                APIService apiService = RestApiClient.getClient().create(APIService.class);
                                Call<DailogSendRespouse> update = apiService.dailogSend(updateparams);


                                update.enqueue(new Callback<DailogSendRespouse>() {

                                    @Override
                                    public void onResponse(Call<DailogSendRespouse> call, retrofit2.Response<DailogSendRespouse> response) {

                                        DailogSendRespouse updateResponce = response.body();


                                        if (updateResponce.getSuccess()) {

                                            Toast.makeText(context, "" + updateResponce.getMsg(), Toast.LENGTH_SHORT).show();

                                        } else {
                                            Toast.makeText(context, "" + updateResponce.getMsg(), Toast.LENGTH_SHORT).show();


                                        }
                                        dialog.dismiss();
                                    }

                                    @Override
                                    public void onFailure(Call<DailogSendRespouse> call, Throwable t) {


                                    }
                                });
                            }
                        });


                    }

                    @Override
                    public void onFailure(Call<GetReportIssueResponce> call, Throwable t) {


                    }
                });


                dialog.show();


            }
        });


        View finalConvertView2 = convertView;


        half_youTubePlayerHolder.half_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent("COMMENTS_PAGE");
                intent.putExtra("message", "click_event");

                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                try {
                    adapterCallback.OpenComments(list.get(position).getPostId(), list.get(position).getSubPostId());
                } catch (ClassCastException e) {
                    e.printStackTrace();
                    // do something
                }
            }
        });


        View finalConvertView3 = convertView;
        View.OnClickListener halfYtLikeClick = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                half_youTubePlayerHolder.half_image_like = finalConvertView3.findViewById(R.id.half_image_like);
                TextView likeCount = finalConvertView3.findViewById(R.id.half_like_count);
                int newStatus = applyLikeChange(position, likeCount);
                bindHalfLikeIcon(half_youTubePlayerHolder.half_image_like, newStatus);
            }
        };
        if (half_youTubePlayerHolder.half_image_like_icon != null) {
            half_youTubePlayerHolder.half_image_like_icon.setOnClickListener(halfYtLikeClick);
        }
        if (half_youTubePlayerHolder.half_image_like != null) {
            half_youTubePlayerHolder.half_image_like.setOnClickListener(halfYtLikeClick);
        }

        half_youTubePlayerHolder.writer_follower.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent("WRITER_FOLLOWER");
                intent.putExtra("message", "click_event");

                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

                if (writerFollowUiState(list.get(position).getIsFollow()) == 0) {

                    half_youTubePlayerHolder.writer_follow.setVisibility(View.GONE);
                    half_youTubePlayerHolder.writer_follower.setVisibility(View.VISIBLE);
                    test_folloew.setText("Following");
                    list.get(position).setIsFollow(1);

                    set_follow_un_follow(list.get(position).getWriterId(), 1);


                } else {

                    half_youTubePlayerHolder.writer_follow.setVisibility(View.VISIBLE);
                    half_youTubePlayerHolder.writer_follower.setVisibility(View.GONE);

                    test_folloew.setText("Follow");
                    list.get(position).setIsFollow(0);

                    set_follow_un_follow(list.get(position).getWriterId(), 0);

                }

            }
        });

        half_youTubePlayerHolder.writer_follow.setOnClickListener(view -> {

            half_youTubePlayerHolder.writer_follow = finalConvertView3.findViewById(R.id.writer_follow);
            half_youTubePlayerHolder.writer_follower = finalConvertView3.findViewById(R.id.writer_follower);
            half_youTubePlayerHolder.test_folloew = finalConvertView3.findViewById(R.id.test_folloew);

            Intent intent = new Intent("WRITER_FOLLOW");
            intent.putExtra("message", "click_event");

            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

            if (writerFollowUiState(list.get(position).getIsFollow()) == 0) {

                half_youTubePlayerHolder.writer_follow.setVisibility(View.GONE);
                half_youTubePlayerHolder.writer_follower.setVisibility(View.VISIBLE);
                test_folloew.setText("Following");
                list.get(position).setIsFollow(1);

                set_follow_un_follow(list.get(position).getWriterId(), 1);

            } else {

                half_youTubePlayerHolder.writer_follow.setVisibility(View.VISIBLE);
                half_youTubePlayerHolder.writer_follower.setVisibility(View.GONE);
                test_folloew.setText("Follow");
                list.get(position).setIsFollow(0);

                set_follow_un_follow(list.get(position).getWriterId(), 0);

            }

        });

        showLog("showNormalVideo"+ "after thubnails 4");


        final RelativeLayout relativeLayoutShareOpt = convertView.findViewById(R.id.relativeLayoutShareOpt);
        final  TextView textViewAllPage = convertView.findViewById(R.id.textViewAllPage);
        final  TextView textViewSinglePage = convertView.findViewById(R.id.textViewSinglePage);
        final ImageView  imageViewShareClose = convertView.findViewById(R.id.imageViewShareClose);

        final ImageView  imageViewShareSingle = convertView.findViewById(R.id.imageViewShareSingle);
        final ImageView  imageViewShareAll = convertView.findViewById(R.id.imageViewShareAll);
        imageViewShareSingle.setImageResource(R.drawable.ic_share_download);
        imageViewShareAll.setImageResource(R.drawable.ic_share_all);

        textViewSinglePage.setText("Share Article");
        textViewAllPage.setText("Share Video");
        relativeLayoutShareOpt.setVisibility(GONE);
        half_youTubePlayerHolder.half_image_share.setOnClickListener(view -> {
            if(relativeLayoutShareOpt.getVisibility()==VISIBLE){
                relativeLayoutShareOpt.setVisibility(GONE);
            }else {
                relativeLayoutShareOpt.setVisibility(VISIBLE);
            }
        });


        imageViewShareClose.setOnClickListener(view -> relativeLayoutShareOpt.setVisibility(GONE));
        textViewAllPage.setOnClickListener(view -> {
            relativeLayoutShareOpt.setVisibility(GONE);
            shareVideoDirect(position, VideoShareHelper.ShareMode.GENERAL);
        });

        textViewSinglePage.setOnClickListener(view -> shareYoutubeVideoScreenShot(finalConvertView2,position,youTubePlayerView,0,imageViewItems));


    }

    public void showPollingResuilt(String option, int position, int size, long postID, int optionID) {
        if (size == 2) {
            if (option.equals("A")) {

                pollingViewHolder.neomorphFrameLayoutA.setVisibility(View.GONE);
                pollingViewHolder.neomorphFrameLayoutB.setVisibility(View.VISIBLE);

                pollingViewHolder.neomorphFrameLayoutC.setVisibility(View.GONE);
                pollingViewHolder.neomorphFrameLayoutD.setVisibility(View.GONE);

                pollingViewHolder.select_neomorphFrameLayoutA.setVisibility(View.VISIBLE);
                pollingViewHolder.select_neomorphFrameLayoutB.setVisibility(View.GONE);
                pollingViewHolder.select_neomorphFrameLayoutC.setVisibility(View.GONE);
                pollingViewHolder.select_neomorphFrameLayoutD.setVisibility(View.GONE);

                pollingViewHolder.select_polling_A.setVisibility(View.VISIBLE);
                pollingViewHolder.polling_B.setVisibility(View.VISIBLE);

                pollingViewHolder.select_polling_C.setVisibility(View.GONE);
                pollingViewHolder.select_polling_D.setVisibility(View.GONE);

                set_servey_answer_submit(postID, optionID, position);

            } else if (option.equals("B")) {

                pollingViewHolder.neomorphFrameLayoutA.setVisibility(View.VISIBLE);
                pollingViewHolder.neomorphFrameLayoutB.setVisibility(View.GONE);

                pollingViewHolder.neomorphFrameLayoutC.setVisibility(View.GONE);
                pollingViewHolder.neomorphFrameLayoutD.setVisibility(View.GONE);

                pollingViewHolder.select_neomorphFrameLayoutA.setVisibility(View.GONE);
                pollingViewHolder.select_neomorphFrameLayoutB.setVisibility(View.VISIBLE);
                pollingViewHolder.select_neomorphFrameLayoutC.setVisibility(View.GONE);
                pollingViewHolder.select_neomorphFrameLayoutD.setVisibility(View.GONE);

                pollingViewHolder.polling_A.setVisibility(View.VISIBLE);
                pollingViewHolder.select_polling_B.setVisibility(View.VISIBLE);
                pollingViewHolder.select_polling_C.setVisibility(View.GONE);
                pollingViewHolder.select_polling_D.setVisibility(View.GONE);
                set_servey_answer_submit(postID, optionID, position);
            }
        } else if (size == 3) {
            if (option.equals("A")) {

                pollingViewHolder.neomorphFrameLayoutA.setVisibility(View.GONE);
                pollingViewHolder.neomorphFrameLayoutB.setVisibility(View.VISIBLE);
                pollingViewHolder.neomorphFrameLayoutC.setVisibility(View.VISIBLE);

                pollingViewHolder.neomorphFrameLayoutD.setVisibility(View.GONE);

                pollingViewHolder.select_neomorphFrameLayoutA.setVisibility(View.VISIBLE);
                pollingViewHolder.select_neomorphFrameLayoutB.setVisibility(View.GONE);
                pollingViewHolder.select_neomorphFrameLayoutC.setVisibility(View.GONE);
                pollingViewHolder.select_neomorphFrameLayoutD.setVisibility(View.GONE);

                pollingViewHolder.select_polling_A.setVisibility(View.VISIBLE);
                pollingViewHolder.polling_B.setVisibility(View.VISIBLE);
                pollingViewHolder.polling_C.setVisibility(View.VISIBLE);
                pollingViewHolder.select_polling_D.setVisibility(View.GONE);
                set_servey_answer_submit(postID, optionID, position);

            } else if (option.equals("B")) {

                pollingViewHolder.neomorphFrameLayoutA.setVisibility(View.VISIBLE);
                pollingViewHolder.neomorphFrameLayoutB.setVisibility(View.GONE);
                pollingViewHolder.neomorphFrameLayoutC.setVisibility(View.VISIBLE);
                pollingViewHolder.neomorphFrameLayoutD.setVisibility(View.GONE);

                pollingViewHolder.select_neomorphFrameLayoutA.setVisibility(View.GONE);
                pollingViewHolder.select_neomorphFrameLayoutB.setVisibility(View.VISIBLE);
                pollingViewHolder.select_neomorphFrameLayoutC.setVisibility(View.GONE);
                pollingViewHolder.select_neomorphFrameLayoutD.setVisibility(View.GONE);

                pollingViewHolder.polling_A.setVisibility(View.VISIBLE);
                pollingViewHolder.select_polling_B.setVisibility(View.VISIBLE);
                pollingViewHolder.polling_C.setVisibility(View.VISIBLE);
                pollingViewHolder.select_polling_D.setVisibility(View.GONE);
                set_servey_answer_submit(postID, optionID, position);
            } else if (option.equals("C")) {

                pollingViewHolder.neomorphFrameLayoutA.setVisibility(View.VISIBLE);
                pollingViewHolder.neomorphFrameLayoutB.setVisibility(View.VISIBLE);
                pollingViewHolder.neomorphFrameLayoutC.setVisibility(View.GONE);
                pollingViewHolder.neomorphFrameLayoutD.setVisibility(View.GONE);

                pollingViewHolder.select_neomorphFrameLayoutA.setVisibility(View.GONE);
                pollingViewHolder.select_neomorphFrameLayoutB.setVisibility(View.GONE);
                pollingViewHolder.select_neomorphFrameLayoutC.setVisibility(View.VISIBLE);
                pollingViewHolder.select_neomorphFrameLayoutD.setVisibility(View.GONE);

                pollingViewHolder.polling_A.setVisibility(View.VISIBLE);
                pollingViewHolder.polling_B.setVisibility(View.VISIBLE);
                pollingViewHolder.select_polling_C.setVisibility(View.VISIBLE);
                pollingViewHolder.select_polling_D.setVisibility(View.GONE);
                set_servey_answer_submit(postID, optionID, position);
            }
        } else if (size == 4) {

            if (option.equals("A")) {

                pollingViewHolder.neomorphFrameLayoutA.setVisibility(View.GONE);
                pollingViewHolder.neomorphFrameLayoutB.setVisibility(View.VISIBLE);
                pollingViewHolder.neomorphFrameLayoutC.setVisibility(View.VISIBLE);
                pollingViewHolder.neomorphFrameLayoutD.setVisibility(View.VISIBLE);

                pollingViewHolder.select_neomorphFrameLayoutA.setVisibility(View.VISIBLE);
                pollingViewHolder.select_neomorphFrameLayoutB.setVisibility(View.GONE);
                pollingViewHolder.select_neomorphFrameLayoutC.setVisibility(View.GONE);
                pollingViewHolder.select_neomorphFrameLayoutD.setVisibility(View.GONE);

                pollingViewHolder.select_polling_A.setVisibility(View.VISIBLE);
                pollingViewHolder.polling_B.setVisibility(View.VISIBLE);
                pollingViewHolder.polling_C.setVisibility(View.VISIBLE);
                pollingViewHolder.polling_D.setVisibility(View.VISIBLE);
                set_servey_answer_submit(postID, optionID, position);

            } else if (option.equals("B")) {

                pollingViewHolder.neomorphFrameLayoutA.setVisibility(View.VISIBLE);
                pollingViewHolder.neomorphFrameLayoutB.setVisibility(View.GONE);
                pollingViewHolder.neomorphFrameLayoutC.setVisibility(View.VISIBLE);
                pollingViewHolder.neomorphFrameLayoutD.setVisibility(View.VISIBLE);

                pollingViewHolder.select_neomorphFrameLayoutA.setVisibility(View.GONE);
                pollingViewHolder.select_neomorphFrameLayoutB.setVisibility(View.VISIBLE);
                pollingViewHolder.select_neomorphFrameLayoutC.setVisibility(View.GONE);
                pollingViewHolder.select_neomorphFrameLayoutD.setVisibility(View.GONE);

                pollingViewHolder.polling_A.setVisibility(View.VISIBLE);
                pollingViewHolder.select_polling_B.setVisibility(View.VISIBLE);
                pollingViewHolder.polling_C.setVisibility(View.VISIBLE);
                pollingViewHolder.polling_D.setVisibility(View.VISIBLE);
                set_servey_answer_submit(postID, optionID, position);
            } else if (option.equals("C")) {

                pollingViewHolder.neomorphFrameLayoutA.setVisibility(View.VISIBLE);
                pollingViewHolder.neomorphFrameLayoutB.setVisibility(View.VISIBLE);
                pollingViewHolder.neomorphFrameLayoutC.setVisibility(View.GONE);
                pollingViewHolder.neomorphFrameLayoutD.setVisibility(View.VISIBLE);

                pollingViewHolder.select_neomorphFrameLayoutA.setVisibility(View.GONE);
                pollingViewHolder.select_neomorphFrameLayoutB.setVisibility(View.GONE);
                pollingViewHolder.select_neomorphFrameLayoutC.setVisibility(View.VISIBLE);
                pollingViewHolder.select_neomorphFrameLayoutD.setVisibility(View.GONE);

                pollingViewHolder.polling_A.setVisibility(View.VISIBLE);
                pollingViewHolder.polling_B.setVisibility(View.VISIBLE);
                pollingViewHolder.select_polling_C.setVisibility(View.VISIBLE);
                pollingViewHolder.polling_D.setVisibility(View.VISIBLE);
                set_servey_answer_submit(postID, optionID, position);
            } else if (option.equals("D")) {

                pollingViewHolder.neomorphFrameLayoutA.setVisibility(View.VISIBLE);
                pollingViewHolder.neomorphFrameLayoutB.setVisibility(View.VISIBLE);
                pollingViewHolder.neomorphFrameLayoutC.setVisibility(View.VISIBLE);
                pollingViewHolder.neomorphFrameLayoutD.setVisibility(View.GONE);

                pollingViewHolder.select_neomorphFrameLayoutA.setVisibility(View.GONE);
                pollingViewHolder.select_neomorphFrameLayoutB.setVisibility(View.GONE);
                pollingViewHolder.select_neomorphFrameLayoutC.setVisibility(View.GONE);
                pollingViewHolder.select_neomorphFrameLayoutD.setVisibility(View.VISIBLE);

                pollingViewHolder.polling_A.setVisibility(View.VISIBLE);
                pollingViewHolder.polling_B.setVisibility(View.VISIBLE);
                pollingViewHolder.polling_C.setVisibility(View.VISIBLE);
                pollingViewHolder.select_polling_D.setVisibility(View.VISIBLE);
                set_servey_answer_submit(postID, optionID, position);

            }
        }
    }


    private JsonObject get_servey_details(long postID, int optionID) {

        LikeSelectionRequest requestModel = new LikeSelectionRequest();

        requestModel.setDraft_id((long) SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME));
        requestModel.setPost_id((long) postID);
        requestModel.setOption((long) optionID);

        String jsonString = new com.google.gson.Gson().toJson(requestModel);
        String encrpt_data = null;
        encrpt_data = encrypt(jsonString);
        showLog("API REQUEST E" + jsonString.toString());
        EncritonRequestData Encriton_Request = new EncritonRequestData();
        Encriton_Request.setData(encrpt_data);
        return new Gson().toJsonTree(Encriton_Request).getAsJsonObject();
    }

    private void set_servey_answer_submit(long postID, int optionID, int position) {


        JsonObject otp = get_servey_details(postID, optionID);


        APIService apiService = RestApiClient.getClient().create(APIService.class);
        Call<JsonObject> apiResponse = apiService.update_survey_poll("v3", otp);
        apiResponse.enqueue(new Callback<JsonObject>() {
            @Override

            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {


            }
        });

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
            @Override
            public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {


                EncreptionRequest encreptionRequest = response.body();
                if (encreptionRequest == null || encreptionRequest.getData() == null) return;
                String encrpt_data = decrypt(encreptionRequest.getData());

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

    public void Share_Count_Value(String share, long post_id) {

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("draft_id", SharedPrefsData.getInt(context, Constants.DraftID, Constants.PREF_NAME));
        jsonObject.addProperty("post_id", post_id);
        jsonObject.addProperty("property", share);


        showLog("share params:"+ "" + jsonObject);
        APIService apiService = RestApiClient.getClient().create(APIService.class);
        Call<EncreptionRequest> apiResponse = apiService.update_post_count("v3", jsonObject);
        apiResponse.enqueue(new Callback<EncreptionRequest>() {
            @Override

            public void onResponse(Call<EncreptionRequest> call, Response<EncreptionRequest> response) {
                if (response.isSuccessful()) {

                EncreptionRequest encreptionRequest = response.body();
                if (encreptionRequest == null || encreptionRequest.getData() == null) return;
                String encrpt_data = null;
                encrpt_data = decrypt(encreptionRequest.getData());

                    Gson gsonObj = new Gson();
                    LikeDownloadRespouse likeDownloadRespouse = gsonObj.fromJson(encrpt_data, LikeDownloadRespouse.class);

                    if (likeDownloadRespouse.getSuccess()) {


                    }

                } else {

                }

            }

            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {

                // Toast.makeText(context, t.getMessage(), Toast.LENGTH_LONG).show();

            }
        });

    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestored) {

        if (!wasRestored) {
            youTubePlayer.cueVideo(youtubeFullVideoURL);
        }

    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

        if (youTubeInitializationResult.isUserRecoverableError()) {
            youTubeInitializationResult.getErrorDialog((Activity) context, RECOVERY_DIALOG_REQUEST).show();
        } else {
            String errorMessage = String.format("There was an error initializing the YouTubePlayer (%1$s)", youTubeInitializationResult.toString());
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
        }
    }


    public static class ViewHolder {


        TextView half_content_title, half_content_description;
        ImageView new_half_image;

        private TextView textView;

        ViewPager viewpager;


        LinearLayout ll_dots;
        ViewPager mViewPager;
        RelativeLayout relative_slider;


        public ViewHolder() {
        }

    }

    public static class RatingScreenViewHolder {
        RatingScreenViewHolder(){

        }

    }

    public static class AdsScreenViewHolder {
        AdsScreenViewHolder(){

        }

    }
    public static class VastTagyADHolder {



    }
    public static class HalfImageActivityHolder {
        public LinearLayout layoutParentDate;
        public ViewPager viewPager;
        WormDotsIndicator indicator;
        RelativeLayout galleryImageLayout;
        public TextView textViewPostDate;
        TextView half_content_title, half_content_description, test_folloew;
        ImageView new_half_image, half_image_like, half_image_dislike, writer_profile_pic,half_comment_icon,report_icon,half_share_icon,app_download_logo,with_strips_layout_ic;
        LinearLayout half_image_strike_layout, inner_half_image_strike_layout, ll_refer_and_earn_half, ll_image;

        LinearLayout relative_half_content, halfimage_dislike_icon, parent_layout, ll_footer_layout;

        LinearLayout writer_follow, writer_follower, writer_profile_image;

        LinearLayout half_image_share, half_comment, half_image_like_icon, report,
                half_strip_after, half_strip_before, with_strip_layout, with_out_strip_layout;


        TextView writer_time, writer_designation, writer_name, my_feed;
        ImageView iv_share_and_earn;

        TextView half_image_like_text, half_image_dislike_text, half_image_share_text, half_image_comment_text;

        TextView strip_text_name, tamil_half_content_description, tamil_half_content_title, english_half_content_description, english_half_content_title;

        public HalfImageActivityHolder() {


        }

    }

    public static class FullImageActivityHolder {
        ImageView mimageView,full_image_comment_bg,full_image_whatsapp_bg,full_image_share_bg,share_download_app_logo,strip_logo,varified_btn;
        LinearLayout side_menu, layout_strip_icon, video_side_menu, layout_strip_iconnew;
        LinearLayout strip;
        TextView strip_text_name, full_img_like, full_img_share, full_img_whtsapp, full_img_comment;
        CircleImageView user_pic;
        LinearLayout  full_image_whatsapp, full_image_share, full_image_comment;
        LinearLayout full_image_view_details;
        public FullImageActivityHolder() {
        }

    }

    public static class ImageSliderHolder {

        TextView half_content_title, half_content_description;
        ImageView new_half_image;

        private TextView textView;

        ViewPager viewpager;


        LinearLayout ll_dots;
        ViewPager mViewPager;
        RelativeLayout relative_slider;
    }

    public static class ImageGallaryHolder {


        public ImageView imageViewShareClose;
        RelativeLayout relativeLayoutShareOpt;
        TextView textViewAllPage,textViewSinglePage;
        RelativeLayout scrren_icon;
        WormDotsIndicator indicator;
        ViewPager viewpager;
        ImageView image_gallary_share;
        LinearLayout dotIndicatior;
        LinearLayout image_gallary_scrip;
        TextView strip_text_name;
        FrameLayout galleryLayout;

    }

    public static class HalfVideoViewHolder {

        TextView half_content_title, half_content_description;
        ImageView new_half_image;

        private TextView textView;

        ViewPager viewpager;


        LinearLayout ll_dots;
        ViewPager mViewPager;
        RelativeLayout relative_slider;
    }

    public static class FullVideoViewHolder {

        public CircleImageView user_pic;
        public LinearLayout side_menu;
        public RelativeLayout exo_play_layout;
        TextView full_video_like, full_video_dislike, full_video_share, full_video_comment;

        ProgressBar progress_bar_for_video;
        ImageView like_for_video_image,full_image_whatsapp_bg;
        LinearLayout like_for_video_ll, dislike_for_video_ll, comment_for_video_ll, share_for_video_ll;


    }
    // youtube foull video 25 04 2021

    public static class FullVideoNewYoutubeViewHolder {

        public CircleImageView user_pic;
        public LinearLayout side_menu;
       // ImageView newFullImageView1;
        TextView full_video_like, full_video_dislike, full_video_share, full_video_comment;
        ImageView like_for_video_image, dislike_for_video_image, btnMuteFull,full_image_whatsapp_bg;
        //LinearLayout like_for_video_ll, dislike_for_video_ll, comment_for_video_ll, share_for_video_ll;


    }

    public static class FullYouTubePlayerHolder {

        TextView half_content_title, half_content_description;
        ImageView new_half_image;

        private TextView textView;

        ViewPager viewpager;


        LinearLayout ll_dots;
        ViewPager mViewPager;
        RelativeLayout relative_slider;
    }

    public static class HalfVideoExoPlayerHolder {

        public LinearLayout parent_layout;
        public LinearLayout layoutParentDate;
        public TextView textViewPostDate;
        public ImageView video_report,half_comment_icon,share;
        TextView half_content_title, half_content_description, test_folloew;

        YouTubePlayerView youTubePlayerView;
        ConstraintLayout layoutParentTop;

        LinearLayout layoutParentTitle;
        LinearLayout layoutParentReportDetails;
        LinearLayout layoutParentMiddle;
        PlayerView simpleExoPlayerView;


        ImageView new_half_youtube_video, half_image_like, half_image_dislike, writer_profile_pic, playButton;

        LinearLayout half_image_strike_layout, inner_half_image_strike_layout;

        LinearLayout relative_half_content, halfimage_dislike_icon;

        LinearLayout writer_follow, writer_follower, ll_refer_and_earn_halfy;

        LinearLayout half_image_share, half_comment, half_image_like_icon, report, with_strip_layout, with_out_strip_layout;


        TextView writer_time, writer_designation, writer_name, my_feed, local_name;

        TextView half_image_like_text, half_image_dislike_text, half_image_share_text, half_image_comment_text;

        ImageView iv_share_and_earn;


        TextView strip_text_name, tamil_half_content_description, tamil_half_content_title, english_half_content_description, english_half_content_title;
    }
    public static class HalfYouTubePlayerHolder {

        public LinearLayout parent_layout;
        public LinearLayout layoutParentDate;
        public TextView textViewPostDate;
        TextView half_content_title, half_content_description, test_folloew;

        YouTubePlayerView youTubePlayerView;
        ConstraintLayout layoutParentTop;

        LinearLayout layoutParentTitle;
        LinearLayout layoutParentReportDetails;
        LinearLayout layoutParentMiddle;
        PlayerView simpleExoPlayerView;
       ImageView imageViewYoutubePlayVideo;

        ImageView new_half_youtube_video, half_image_like, half_image_dislike, writer_profile_pic, playButton, btnMuteHalf;

        LinearLayout half_image_strike_layout, inner_half_image_strike_layout;

        LinearLayout relative_half_content, halfimage_dislike_icon;

        LinearLayout writer_follow, writer_follower, ll_refer_and_earn_halfy;

        LinearLayout half_image_share, half_comment, half_image_like_icon, report, with_strip_layout, with_out_strip_layout;


        TextView writer_time, writer_designation, writer_name, my_feed, local_name;

        TextView half_image_like_text, half_image_dislike_text, half_image_share_text, half_image_comment_text;

        ImageView iv_share_and_earn;


        TextView strip_text_name, tamil_half_content_description, tamil_half_content_title, english_half_content_description, english_half_content_title;
    }

    public static class ImageMagazineHolder {

        LinearLayout magzine_ll_dots;
        ViewPager magzine_viewPager_data;
    }

    public static class WebViewHolder {
        WebView webView;
    }
    public static class WebViewHolderAD {
        WebView webView;
    }

    public static class SurveyViewHolder {

        LinearLayout llPercentLayout;
        NeomorphFrameLayout neomorphFrameLayoutYes, neomorphFrameLayoutNo, neomorphFrameLayoutYesSelected,
                neomorphFrameLayoutNoSelected, neomorphFrameLayoutYesPercent, neomorphFrameLayoutNoPercent;
        RelativeLayout relativeLayoutSurveyMain;

        private TextView tvSurveyQuestionEnglish, tvAnswerYes, tvAnswerNo, tvYesPercent, tvNoPercent;
    }

    public static class PollingViewHolder {
        LinearLayout llPercentLayout;
        RelativeLayout relativeLayoutSurveyMain;
        TextView tv_polling_question;
        TextView polling_A, polling_B, polling_C, polling_D, tv_answer_A, tv_answer_B, tv_answer_C, tv_answer_D;
        TextView select_polling_A, select_polling_B, select_polling_C, select_polling_D, select_tv_answer_A, select_tv_answer_B, select_tv_answer_C, select_tv_answer_D;
        ImageView polling_half_image;
        NeomorphFrameLayout neomorphFrameLayoutA, neomorphFrameLayoutB, neomorphFrameLayoutC,
                neomorphFrameLayoutD;
        NeomorphFrameLayout select_neomorphFrameLayoutA, select_neomorphFrameLayoutB, select_neomorphFrameLayoutC,
                select_neomorphFrameLayoutD;
    }

    public static class VideosliderHolder {

        TextView half_content_title, half_content_description;
        ImageView new_half_image;

        private TextView textView;

        ViewPager viewpager;


        LinearLayout ll_dots;
        ViewPager mViewPager;
        RelativeLayout relative_slider;
    }

    public static class VideogalaryHolder {

        TextView half_content_title, half_content_description;
        ImageView new_half_image;

        private TextView textView;

        ViewPager viewpager;


        LinearLayout ll_dots;
        ViewPager mViewPager;
        RelativeLayout relative_slider;
    }
    // call survey response api

    private JsonObject set_static_data() {

        SurveyRequestFeedback requestModel = new SurveyRequestFeedback();
        requestModel.setDraftId(SharedPrefsData.getInt(getApplicationContext(), Constants.DraftID, Constants.PREF_NAME));   //
        requestModel.setPostId(postIDSurvey);
        requestModel.setResponse(responseSurvey);

        String jsonString = new com.google.gson.Gson().toJson(requestModel);
        String encrpt_data = null;
        encrpt_data = encrypt(jsonString);
        showLog("API REQUEST E" + jsonString.toString());
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
                    if (encreptionRequest == null || encreptionRequest.getData() == null) return;
                    String encrpt_data = null;
                    encrpt_data = decrypt(encreptionRequest.getData());

                    Gson gsonObj = new Gson();
                    surveyFeedbackResponse = gsonObj.fromJson(encrpt_data, SurveyFeedbackResponse.class);

                    if (surveyFeedbackResponse.getSuccess()) {

                        ModelClass.Datum datum = new ModelClass.Datum();
                        datum.setSurveyStatus(1);

                        datum.setYes_percent(list.get(position).getYes_percent());
                        datum.setNo_percent(list.get(position).getNo_percent());

                        list.set(position, datum);

                        percentageYes = " " + surveyFeedbackResponse.getData().get(0).getPercent();
                        percentageNo = " " + surveyFeedbackResponse.getData().get(1).getPercent();

                        surveyViewHolder.tvYesPercent.setText(" " + surveyFeedbackResponse.getData().get(0).getPercent() + "%");
                        surveyViewHolder.tvNoPercent.setText(" " + surveyFeedbackResponse.getData().get(1).getPercent() + "%");

                    }

                } else {

                }

            }

            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {

            }
        });

    }

    private void updatearraylist(int optionID, int positionx) {
        try {


            ModelClass.Datum datum = new ModelClass.Datum();

            List<ModelClass.SurveyOptions> surveyOptionsArrayList = new ArrayList<>();

            for (int i = 0; i < list.get(positionx).getSurveyOptions().size(); i++) {
                ModelClass.SurveyOptions surveyOptions = new ModelClass.SurveyOptions();
                surveyOptions.setId(list.get(positionx).getSurveyOptions().get(i).getId());
                surveyOptions.setPercent(list.get(positionx).getSurveyOptions().get(i).getPercent());
                surveyOptions.setSurveyOptionsName(list.get(positionx).getSurveyOptions().get(i).getSurveyOptionsName());
                surveyOptions.setCount(list.get(positionx).getSurveyOptions().get(i).getCount());
                surveyOptionsArrayList.add(surveyOptions);

            }
            datum.setSurveyOptions(surveyOptionsArrayList);
            datum.setSelected_option(optionID);
            datum.setLikeStatus(list.get(positionx).getLikeStatus());
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
            datum.setStrip_content(list.get(positionx).getStrip_content());
            datum.setWriterProfile(list.get(positionx).getWriterProfile());


            list.set(positionx, datum);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public class SSLTolerentWebViewClient extends WebViewClient {
        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            if (error.toString() == "piglet")
                handler.cancel();
            else
                handler.proceed(); // Ignore SSL certificate errors
        }
    }
    public class SSLTolerentWebViewClientADs extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            try{
                showLog("  request.getUrl()::"+  request.getUrl());
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(request.getUrl().toString()));
                context.startActivity( intent );
            }catch (Exception e){
                e.printStackTrace();
            }

            return true;
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            if (error.toString() == "piglet")
                handler.cancel();
            else
                handler.proceed(); // Ignore SSL certificate errors
        }
    }

    public static boolean isValidContextForGlide(final Context context) {
        if (context == null) {
            return false;
        }
        if (context instanceof Activity) {
            final Activity activity = (Activity) context;
            if (activity.isDestroyed() || activity.isFinishing()) {
                return false;
            }
        }
        return true;
    }

    public static interface AdapterCallback {

        void OpenComments(Long postId, Long subPostId);
        void shareInWhatsApp(SharePostToWhatsApp justInterface);
        void closeBottomStrip(boolean closeStrip);
    }

    /**
     * Feed cards: hide AnyNews share short-links on video; show external source only on articles.
     */
    private void bindFeedWebUrlPill(
            @Nullable TextView pill,
            @Nullable TextView stripPill,
            @Nullable ModelClass.Datum item,
            boolean onVideoCard) {
        if (pill == null && stripPill == null) {
            return;
        }
        if (onVideoCard || item == null) {
            if (pill != null) {
                pill.setVisibility(GONE);
            }
            if (stripPill != null) {
                stripPill.setVisibility(GONE);
            }
            return;
        }
        String external = resolveExternalSourceUrlForPill(item);
        if (TextUtils.isEmpty(external)) {
            if (pill != null) {
                pill.setVisibility(GONE);
            }
            if (stripPill != null) {
                stripPill.setVisibility(GONE);
            }
            return;
        }
        String label = external.replace("https://", "").replace("http://", "");
        if (pill != null) {
            pill.setVisibility(VISIBLE);
            pill.setText(label);
            pill.setOnClickListener(
                    v -> {
                        try {
                            context.startActivity(
                                    new Intent(Intent.ACTION_VIEW, Uri.parse(external)));
                        } catch (Exception ignored) {
                        }
                    });
        }
        if (stripPill != null) {
            stripPill.setVisibility(VISIBLE);
            stripPill.setText(label);
        }
    }

    @Nullable
    private static String resolveExternalSourceUrlForPill(@NonNull ModelClass.Datum item) {
        String candidate = item.getWeb_url();
        if (TextUtils.isEmpty(candidate)) {
            candidate = item.getSource_link();
        }
        if (TextUtils.isEmpty(candidate)) {
            return null;
        }
        String url = candidate.trim();
        String share = item.getShareLink();
        if (!TextUtils.isEmpty(share) && url.equalsIgnoreCase(share.trim())) {
            return null;
        }
        if (!SharePublicBaseHelper.newsShareOrigin(url).isEmpty()) {
            return null;
        }
        try {
            String host = Uri.parse(url).getHost();
            if (host != null) {
                String h = host.toLowerCase(Locale.US);
                if (h.contains("anws.in")
                        || SharePublicBaseHelper.isShortShareHost(h)
                        || h.contains("anynews.co.in")) {
                    return null;
                }
            }
        } catch (Exception ignored) {
        }
        return url;
    }

    private void prepareFeedExoPlayer(@NonNull ExoPlayer player, @NonNull String videoUrl) {
        try {
            String safe = StatusVideoMedia.ensureHttps(videoUrl.trim());
            player.setMediaItem(MediaItem.fromUri(Uri.parse(safe)));
            player.prepare();
        } catch (Exception e) {
            showLog("prepareFeedExoPlayer: " + e.getMessage());
        }
    }

    /**
     * Idle Exo card: load poster into {@code thaumbnail_for_video} (XML defaults GONE)
     * and show the big Way2 play circle. Fat equivalent: {@code setFeedExoPosterCover(..., true)}.
     */
    private void showLeanExoIdlePoster(
            @Nullable ImageView thumbnail,
            @Nullable ImageView centerPlay,
            @Nullable String thumbUrl) {
        if (thumbnail != null) {
            thumbnail.setVisibility(View.VISIBLE);
            thumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
            if (!TextUtils.isEmpty(thumbUrl)) {
                if (isValidContextForGlide(context)) {
                    try {
                        Glide.with(context).load(thumbUrl).centerCrop().into(thumbnail);
                    } catch (Exception ignored) {
                    }
                }
                try {
                    imageLoader.displayImage(thumbUrl, thumbnail, options, null);
                } catch (Exception ignored) {
                }
            } else {
                thumbnail.setImageDrawable(null);
                thumbnail.setBackgroundColor(Color.parseColor("#1A1A1A"));
            }
        }
        if (centerPlay != null) {
            centerPlay.setImageResource(R.drawable.ic_way2_play_circle);
            centerPlay.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Center overlay + Way2 controller icons from Exo state.
     * Playing: hide poster / big play / exo_pause. Paused or idle: show play (no stuck pause).
     */
    private void applyLeanExoOverlayState(
            int playbackState,
            boolean playWhenReady,
            @Nullable ImageView thumbnail,
            @Nullable ImageView centerPlay,
            @Nullable ProgressBar progress,
            @Nullable PlayerView playerView) {
        if (playbackState == Player.STATE_ENDED
                || (playbackState == Player.STATE_IDLE && !playWhenReady)) {
            if (progress != null) {
                progress.setVisibility(View.GONE);
            }
            if (thumbnail != null) {
                thumbnail.setVisibility(View.VISIBLE);
            }
            if (centerPlay != null) {
                centerPlay.setImageResource(R.drawable.ic_way2_play_circle);
                centerPlay.setVisibility(View.VISIBLE);
            }
            updateLeanExoCenterControlIcons(playerView, false);
            return;
        }
        if (playWhenReady) {
            if (centerPlay != null) {
                centerPlay.setVisibility(View.GONE);
            }
            if (playbackState == Player.STATE_READY) {
                if (thumbnail != null) {
                    thumbnail.setVisibility(View.GONE);
                }
                if (progress != null) {
                    progress.setVisibility(View.GONE);
                }
            } else if (playbackState == Player.STATE_BUFFERING
                    || playbackState == Player.STATE_IDLE) {
                if (progress != null) {
                    progress.setVisibility(View.VISIBLE);
                }
            }
            updateLeanExoCenterControlIcons(playerView, true);
            return;
        }
        // Paused mid-watch: keep last frame, bring play back (no pause glyph).
        if (progress != null) {
            progress.setVisibility(View.GONE);
        }
        if (centerPlay != null) {
            centerPlay.setImageResource(R.drawable.ic_way2_play_circle);
            centerPlay.setVisibility(View.VISIBLE);
        }
        updateLeanExoCenterControlIcons(playerView, false);
    }

    /**
     * Media3 custom controllers still toggle legacy {@code exo_play}/{@code exo_pause};
     * Way2 chrome must force pause GONE and hide play while playing (fat updateExoCenterControlIcons).
     * Classic/Play Store leaves Media3 defaults alone.
     */
    private void updateLeanExoCenterControlIcons(@Nullable PlayerView playerView, boolean playing) {
        if (playerView == null) {
            return;
        }
        if (FeedVideoPlayerConfig.isPlayStoreClassic(context)) {
            return;
        }
        View playBtn = playerView.findViewById(R.id.exo_play);
        View pauseBtn = playerView.findViewById(R.id.exo_pause);
        View muteBtn = playerView.findViewById(R.id.mute_button);
        View fullscreenBtn = playerView.findViewById(R.id.fullscreen_button);
        View progressBar = playerView.findViewById(R.id.exo_progress);
        if (pauseBtn != null) {
            pauseBtn.setVisibility(GONE);
        }
        if (playing) {
            if (playBtn != null) {
                playBtn.setVisibility(GONE);
            }
            if (muteBtn != null) {
                muteBtn.setVisibility(VISIBLE);
            }
            if (fullscreenBtn != null) {
                if ("force_gone".equals(String.valueOf(fullscreenBtn.getTag()))) {
                    fullscreenBtn.setVisibility(GONE);
                } else {
                    fullscreenBtn.setVisibility(VISIBLE);
                }
            }
            if (progressBar != null) {
                progressBar.setVisibility(VISIBLE);
            }
            try {
                playerView.setControllerShowTimeoutMs(0);
                playerView.setControllerHideOnTouch(false);
                playerView.showController();
            } catch (Exception ignored) {
            }
        } else {
            if (playBtn != null) {
                playBtn.setVisibility(VISIBLE);
            }
            if (muteBtn != null) {
                muteBtn.setVisibility(GONE);
            }
            if (fullscreenBtn != null) {
                fullscreenBtn.setVisibility(GONE);
            }
            if (progressBar != null) {
                progressBar.setVisibility(GONE);
            }
            try {
                playerView.showController();
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Wire play/pause + keep Way2 icons in sync. No shared-Exo / settle storms —
     * only listeners on the card's current player instance.
     */
    private void wireLeanExoChrome(@Nullable PlayerView playerView, @Nullable View convertViewForSideMenu) {
        if (playerView == null || exoPlayer == null) {
            return;
        }
        if (FeedVideoPlayerConfig.isPlayStoreClassic(context)) {
            playerView.setControllerShowTimeoutMs(3500);
            playerView.setControllerHideOnTouch(true);
            playerView.setControllerAutoShow(true);
            View pauseBtn = playerView.findViewById(R.id.exo_pause);
            View playBtn = playerView.findViewById(R.id.exo_play);
            if (pauseBtn != null) {
                pauseBtn.setOnClickListener(v -> {
                    if (exoPlayer != null) {
                        exoPlayer.setPlayWhenReady(false);
                        try {
                            exoPlayer.pause();
                        } catch (Exception ignored) {
                        }
                    }
                    pauseBtn.setVisibility(GONE);
                    if (playBtn != null) {
                        playBtn.setVisibility(VISIBLE);
                    }
                });
            }
            if (playBtn != null) {
                playBtn.setOnClickListener(v -> {
                    if (exoPlayer != null) {
                        if (exoPlayer.getPlaybackState() == Player.STATE_ENDED) {
                            exoPlayer.seekTo(0);
                        }
                        exoPlayer.setPlayWhenReady(true);
                        try {
                            exoPlayer.play();
                        } catch (Exception ignored) {
                        }
                    }
                    playBtn.setVisibility(GONE);
                    if (pauseBtn != null) {
                        pauseBtn.setVisibility(VISIBLE);
                    }
                });
            }
            return;
        }
        playerView.setControllerShowTimeoutMs(0);
        playerView.setControllerHideOnTouch(false);
        playerView.setControllerAutoShow(true);
        View playBtn = playerView.findViewById(R.id.exo_play);
        View root = playerView.findViewById(R.id.exo_controller_root);
        if (root == null) {
            root = playerView;
        }
        if (playBtn != null) {
            playBtn.setOnClickListener(v -> {
                if (exoPlayer == null) {
                    return;
                }
                if (exoPlayer.getPlaybackState() == Player.STATE_ENDED) {
                    exoPlayer.seekTo(0);
                }
                exoPlayer.setPlayWhenReady(true);
                try {
                    exoPlayer.play();
                } catch (Exception ignored) {
                }
                updateLeanExoCenterControlIcons(playerView, true);
                View parent = playerView.getParent() instanceof View
                        ? (View) playerView.getParent() : null;
                if (parent != null) {
                    ImageView center = parent.findViewById(R.id.imageViewVideoPlayVideo);
                    if (center != null) {
                        center.setVisibility(View.GONE);
                    }
                }
            });
        }
        root.setOnClickListener(v -> {
            if (exoPlayer == null) {
                return;
            }
            int state = exoPlayer.getPlaybackState();
            boolean playingNow = exoPlayer.getPlayWhenReady()
                    && state != Player.STATE_ENDED
                    && state == Player.STATE_READY;
            if (!playingNow) {
                return;
            }
            exoPlayer.setPlayWhenReady(false);
            try {
                exoPlayer.pause();
            } catch (Exception ignored) {
            }
            updateLeanExoCenterControlIcons(playerView, false);
            View parent = playerView.getParent() instanceof View
                    ? (View) playerView.getParent() : null;
            if (parent != null) {
                ImageView center = parent.findViewById(R.id.imageViewVideoPlayVideo);
                if (center != null) {
                    center.setImageResource(R.drawable.ic_way2_play_circle);
                    center.setVisibility(View.VISIBLE);
                }
            }
            if (convertViewForSideMenu != null) {
                LinearLayout sideMenu = convertViewForSideMenu.findViewById(R.id.video_side_menu);
                if (sideMenu != null) {
                    sideMenu.setVisibility(View.VISIBLE);
                    sideMenu.bringToFront();
                }
            }
        });
        exoPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (exoPlayer == null || playerView.getPlayer() != exoPlayer) {
                    return;
                }
                boolean pwr = exoPlayer.getPlayWhenReady();
                boolean playingNow = pwr
                        && playbackState != Player.STATE_ENDED
                        && (playbackState == Player.STATE_READY
                        || playbackState == Player.STATE_BUFFERING);
                updateLeanExoCenterControlIcons(playerView, playingNow);
            }

            @Override
            public void onPlayWhenReadyChanged(boolean playWhenReady, int reason) {
                if (exoPlayer == null || playerView.getPlayer() != exoPlayer) {
                    return;
                }
                int state = exoPlayer.getPlaybackState();
                boolean playingNow = playWhenReady
                        && state != Player.STATE_ENDED
                        && (state == Player.STATE_READY || state == Player.STATE_BUFFERING);
                updateLeanExoCenterControlIcons(playerView, playingNow);
                if (!playWhenReady && state != Player.STATE_ENDED) {
                    View parent = playerView.getParent() instanceof View
                            ? (View) playerView.getParent() : null;
                    if (parent != null) {
                        ImageView center = parent.findViewById(R.id.imageViewVideoPlayVideo);
                        if (center != null) {
                            center.setImageResource(R.drawable.ic_way2_play_circle);
                            center.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }
        });
        boolean initiallyPlaying = exoPlayer.getPlayWhenReady()
                && exoPlayer.getPlaybackState() != Player.STATE_ENDED
                && exoPlayer.getPlaybackState() != Player.STATE_IDLE;
        updateLeanExoCenterControlIcons(playerView, initiallyPlaying);
    }

    /**
     * Cheap mid-flip / getView pause. Stop audible playback only — no Exo release
     * (that janks 3D flip on low-end). Heavier release belongs in HomePage onPause /
     * settle paths via {@link #releaseActiveExoPlayer()}.
     */
    public static void pauseVideo(){
        try {
            ArticleTtsHelper.stopAll();
        } catch (Exception ignored) {
        }
        try {
            if (youTubePlayerViewHalf != null) {
                youTubePlayerViewHalf.pause();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (exoPlayer != null) {
                exoPlayer.setPlayWhenReady(false);
                try {
                    exoPlayer.pause();
                } catch (Exception ignored) {
                }
                try {
                    exoPlayer.setVolume(0f);
                } catch (Exception ignored) {
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Mute + pause Google native VideoController (Samsung ad-audio leak on leave). */
    public void muteActiveNativeAdAudio() {
        try {
            if (nativeAd == null) return;
            if (nativeAd.getMediaContent() == null) return;
            VideoController vc = nativeAd.getMediaContent().getVideoController();
            if (vc == null) return;
            vc.mute(true);
            vc.pause();
        } catch (Exception e) {
            showLog("muteActiveNativeAdAudio: " + e.getMessage());
        }
    }

    /**
     * Soft leave-ad hook (fat had empty gesture + settle destroy). Lean: mute native
     * + tear down VAST so audio cannot continue after flip-away.
     */
    public void pauseAdMediaForLeaveGesture(int adPage) {
        muteActiveNativeAdAudio();
        destroyActiveVastSession();
    }

    /** After settle: stop TTS for any article that is no longer on screen. */
    public void releaseOffscreenAdMediaAfterSettle(int currentPage) {
        try {
            Long postId = null;
            if (list != null && currentPage >= 0 && currentPage < list.size()
                    && list.get(currentPage) != null) {
                postId = list.get(currentPage).getPostId();
            }
            ArticleTtsHelper.stopIfNotCurrent(postId);
        } catch (Exception ignored) {
        }
        muteActiveNativeAdAudio();
        destroyActiveVastSession();
    }

    private void destroyActiveVastSession() {
        try {
            if (activeVastAdsManager != null) {
                activeVastAdsManager.destroy();
            }
        } catch (Exception ignored) {
        }
        activeVastAdsManager = null;
        // AdsLoader has no safe mid-flip release API across IMA versions — drop ref only.
        activeVastAdsLoader = null;
        try {
            if (activeVastVideoView != null) {
                activeVastVideoView.stopPlayback();
                activeVastVideoView.suspend();
            }
        } catch (Exception ignored) {
        }
        activeVastVideoView = null;
    }

    /**
     * Walk FlipView scrap trees: pause WebView media, VideoView, and any attached
     * PlayerView that is not the shared live Exo (avoid blanking the landing video).
     */
    public void pauseOffscreenMedia(@Nullable ViewGroup flipRoot) {
        if (flipRoot == null) return;
        try {
            for (int i = 0; i < flipRoot.getChildCount(); i++) {
                pauseMediaInViewTree(flipRoot.getChildAt(i));
            }
        } catch (Exception e) {
            showLog("pauseOffscreenMedia: " + e.getMessage());
        }
    }

    private void pauseMediaInViewTree(@Nullable View root) {
        if (root == null) return;
        try {
            if (root instanceof WebView) {
                // onPause only — WebView.pauseTimers() is process-global.
                ((WebView) root).onPause();
                return;
            }
            if (root instanceof VideoView) {
                VideoView vv = (VideoView) root;
                try {
                    vv.pause();
                } catch (Exception ignored) {
                }
                try {
                    vv.stopPlayback();
                } catch (Exception ignored) {
                }
                return;
            }
            if (root instanceof PlayerView) {
                PlayerView pv = (PlayerView) root;
                Player attached = pv.getPlayer();
                if (attached != null && attached != exoPlayer) {
                    try {
                        attached.setPlayWhenReady(false);
                        attached.pause();
                    } catch (Exception ignored) {
                    }
                }
            }
        } catch (Exception ignored) {
        }
        if (root instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) root;
            for (int i = 0; i < group.getChildCount(); i++) {
                pauseMediaInViewTree(group.getChildAt(i));
            }
        }
    }

    /**
     * Top-right speaker on half-image cards when admin {@code tts_speaker_on_image}
     * (+ master {@code tts_enabled}) is ON.
     */
    private void bindArticleTtsSpeaker(View convertView, int position) {
        if (convertView == null) return;
        try {
            ImageView speakerBtn = convertView.findViewById(R.id.article_tts_button);
            View pulseView = convertView.findViewById(R.id.article_tts_pulse);
            if (speakerBtn == null) return;
            Long postId = null;
            CharSequence body = null;
            if (list != null && position >= 0 && position < list.size()) {
                postId = list.get(position).getPostId();
                body = list.get(position).getDescription();
            }
            boolean show = ArticleTtsConfigHelper.shouldShowOnImage(context);
            ArticleTtsHelper.bindButton(
                    context,
                    speakerBtn,
                    pulseView,
                    postId,
                    body,
                    show,
                    ArticleTtsHelper.STYLE_IMAGE);
        } catch (Exception ignored) {
        }
    }

    /**
     * Engagement-row speaker when admin {@code tts_speaker_on_engagement}
     * (+ master) is ON.
     */
    private void bindHalfTtsSpeaker(@Nullable View convertView, int position) {
        if (convertView == null) return;
        try {
            View slot = convertView.findViewById(R.id.half_tts);
            ImageView speakerBtn = convertView.findViewById(R.id.half_tts_icon);
            View pulseView = convertView.findViewById(R.id.half_tts_pulse);
            if (slot == null || speakerBtn == null) return;
            boolean show = ArticleTtsConfigHelper.shouldShowOnEngagement(context);
            slot.setVisibility(show ? View.VISIBLE : View.GONE);
            Long postId = null;
            CharSequence body = null;
            if (list != null && position >= 0 && position < list.size()) {
                postId = list.get(position).getPostId();
                body = list.get(position).getDescription();
            }
            ArticleTtsHelper.bindButton(
                    context,
                    speakerBtn,
                    pulseView,
                    postId,
                    body,
                    show,
                    ArticleTtsHelper.STYLE_ENGAGEMENT);
            if (show) {
                EngagementCountHelper.applyIconTint(context, speakerBtn);
            }
        } catch (Exception ignored) {
        }
    }

    /** Full Exo release — call after flip settles or onPause, not from getView. */
    public static void releaseActiveExoPlayer() {
        try {
            if (exoPlayer != null) {
                exoPlayer.setPlayWhenReady(false);
                try {
                    exoPlayer.pause();
                } catch (Exception ignored) {
                }
                try {
                    exoPlayer.stop();
                } catch (Exception ignored) {
                }
                exoPlayer.release();
                exoPlayer = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean hasActiveVideoPlayer() {
        return youTubePlayerViewHalf != null || exoPlayer != null;
    }

    private void shareVideoDirect(int position, VideoShareHelper.ShareMode mode) {
        try {
            ModelClass.Datum item = list.get(position);
            StatusVideoMedia.ShareUrlPair urls;
            if ("4".equals(item.getMediaType()) || "6".equals(item.getMediaType())) {
                String download = item.getDownloadUrl();
                String play = item.getVideoUrl();
                urls = new StatusVideoMedia.ShareUrlPair(
                        !TextUtils.isEmpty(download) ? download.trim() : null,
                        !TextUtils.isEmpty(play) ? play.trim() : null);
            } else {
                urls = StatusVideoMedia.resolveShareFileUrls(item);
            }
            if (TextUtils.isEmpty(urls.primary) && TextUtils.isEmpty(urls.alternate)) {
                Toast.makeText(context, "Video is not available to share", Toast.LENGTH_SHORT).show();
                return;
            }
            boolean halfExo = item.getPostType() != null
                    && item.getPostType().equalsIgnoreCase("normal_video")
                    && "3".equals(item.getMediaType());
            VideoShareHelper.OnShareCompleteListener listener =
                    () -> Share_Count_Value("share", item.getPostId());
            if (halfExo) {
                VideoShareHelper.shareHalfArticleVideo(context, activity, urls.primary, urls.alternate,
                        item.getTitle(), item.getDescription(), item.getShareLink(), mode, listener);
            } else {
                VideoShareHelper.shareVideo(context, activity, urls.primary, urls.alternate,
                        item.getTitle(), item.getDescription(), item.getShareLink(), mode, listener);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Unable to share video", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("Range")
    public void downloadPDFWithProgressBar(String url, String title,int position,int from) {
        isVideoDownloadCancel=false;

        showLog("URL:"+url);
        showLog("TITLE:"+title);
        String downloadingTitle="Downloading magazine...";
        if(from==0){
             downloadingTitle="Downloading magazine...";
            Toast.makeText(context,"Your magazine is downloading",Toast.LENGTH_LONG).show();
        }else{
             downloadingTitle="Downloading Video...";
            Toast.makeText(context,"Your video is downloading",Toast.LENGTH_LONG).show();
        }

//        ProgressDialog progressDialog = new ProgressDialog(context);
//        progressDialog.setMessage("Downloading...");
//        progressDialog.setIndeterminate(true);
//        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
//        progressDialog.setCancelable(true);
//         DownloadManager.Request request = new DownloadManager.Request(Uri.parse("https://www.africau.edu/images/default/sample.pdf"));
//        DownloadManager.Request request = new DownloadManager.Request(Uri.parse("https://d1rzlnq17mbuq9.cloudfront.net/magazines/exports/943613.pdf"));
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setTitle(title);
        request.setDescription(downloadingTitle);

        //  request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, title);
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        long downloadId = downloadManager.enqueue(request);
        showLog("DOWNLOAD_ID::::::"+downloadId);


        BroadcastReceiver onComplete = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent1) {

                try{

                    context.getContentResolver().registerContentObserver(Uri.parse("content://downloads/my_downloads"),
                            true, new ContentObserver(null) {
                                @Override
                                public void onChange(boolean selfChange, Uri uri) {
                                    super.onChange(selfChange, uri);
                                    if (uri.toString().matches(".*\\d+$")) {
                                        long changedId = Long.parseLong(Objects.requireNonNull(uri.getLastPathSegment()));
                                        if (changedId == downloadId) {
                                            showLog("TAG"+ " :: onChange: " + uri.toString() + " " + changedId + " " + downloadId);

                                            Cursor cursor = null;
                                            try {
                                                cursor = context.getContentResolver().query(uri, null, null, null, null);
                                                if (cursor != null && cursor.moveToFirst()) {
                                                    showLog("TAG"+ "onChange: running");
                                                    isVideoDownloadCancel=false;
                                                } else {
                                                    isVideoDownloadCancel=true;
                                                    showLog("TAG"+ "onChange: cancel");
                                                }
                                            } finally {
                                                if (cursor != null) {
                                                    cursor.close();
                                                }
                                            }
                                        }
                                    }
                                }
                            });

                    showLog("REASON::::::1");

                }catch (Exception e){
                    e.printStackTrace();
                }

                // progressDialog.dismiss();
                showLog("ACTION:"+intent1.getAction());
                showLog("ACTION2:"+intent1.getData());
                //checkDownloadStatus(downloadId,downloadManager);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showPopupDownloadMessage(position,title,from);
                        fromWhatsapp=false;
                    }
                },1000);



                context.unregisterReceiver(this);

            }
        };
        context.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        //progressDialog.show();
    }
    private void showPopupDownloadMessage(int position,String title,int from){
        if(from==0){
            Toast.makeText(context,"Your magazine is downloaded",Toast.LENGTH_LONG).show();
            try {
                String mInvitationUrl = list.get(position).getShareLink();
                showLog("mInvitationUrl:"+mInvitationUrl);
                String msg = list.get(position).getTitle() + " " + mInvitationUrl;
                showLog("msg:"+msg);
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("application/pdf");
                intent.putExtra(Intent.EXTRA_SUBJECT, mInvitationUrl.toString());
                intent.putExtra(Intent.EXTRA_TEXT, msg);
                if(fromWhatsapp){
                    intent.setType("application/pdf");
                    intent.setPackage("com.whatsapp");
                }
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), title);
                showLog("file url:"+file.getAbsolutePath());
                showLog("file url:"+file);
                file.setReadable(true, true);
                // Uri uri = Uri.fromFile(file);
                Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
                showLog("file uri:"+uri.getPath());
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(Intent.createChooser(intent, title), PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo resolveInfo : resInfoList) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    context.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                showLog("FLAG_GRANT_READ_URI_PERMISSION:");
                context.startActivity(Intent.createChooser(intent, "Share Link"));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }else{
            if(isVideoDownloadCancel){
                Toast.makeText(context,"Your video download is cancelled",Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(context,"Your video is downloaded",Toast.LENGTH_LONG).show();
                sendNotification(list.get(position).getPostId(),title);
            }
        }
    }

    private void sendNotification(long post_id,String title) {
        showLog("sendNotification::@1");
        try {
            Intent resultIntent = new Intent(Intent.ACTION_SEND);
 //           resultIntent.setAction(Intent.ACTION_SEND);
            resultIntent.setType("video/*");
            resultIntent.setPackage("com.whatsapp");
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), title);
            file.setReadable(true, true);
            Uri uriForFile = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
            showLog("file uri:"+uriForFile.getPath());
            resultIntent.putExtra(Intent.EXTRA_STREAM, uriForFile);
            List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(Intent.createChooser(resultIntent, title), PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                showLog("packageName:::::::"+packageName);
                context.grantUriPermission(packageName, uriForFile, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            activity.grantUriPermission("com.whatsapp", uriForFile, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            resultIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            showLog("FLAG_GRANT_READ_URI_PERMISSION:");
            showLog("sendNotification::");
            PendingIntent pendingIntent;
            if (Build.VERSION.SDK_INT >= 31) {
                showLog("sendNotification::@2");
                pendingIntent = PendingIntent.getActivity(
                        getApplicationContext(),
                        10,
                        resultIntent,
                        PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT // setting the mutability flag
                );
            } else {
                showLog("sendNotification::@3");
                pendingIntent = PendingIntent.getActivity(context,
                        Integer.parseInt("10"), resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            }
            try {
                String GROUP_KEY_WORK_EMAIL = "com.anyNews.anynews.Services.WORK_EMAIL";
                RemoteViews collapsedView = new RemoteViews(context.getPackageName(), R.layout.download_video_notification);
                showLog("sendNotification::@4");
                final Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
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
                    showLog("sendNotification::@8");
                    builder.setSmallIcon(R.drawable.ic_arman_notificatio);
                    builder.setColor(context.getResources().getColor(R.color.colorPrimary));
                } else {
                    showLog("sendNotification::@7");
                    builder.setSmallIcon(R.drawable.ic_arman_notificatio);
                }
                showLog("sendNotification::@6");
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


    class DownloadFile extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
        }

        @Override
        protected Void doInBackground(String... strings) {
            String fileUrl = strings[0];   // -> http://maven.apache.org/maven-1.x/maven.pdf
            String fileName = strings[1];  // -> maven.pdf
            String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
            File folder = new File(extStorageDirectory, "anyNews");
            folder.mkdir();

            File pdfFile = new File(folder, fileName);

            try{
                pdfFile.createNewFile();
            }catch (IOException e){
                e.printStackTrace();
            }

            try {
                 int MEGABYTE = 1024 * 1024;
                URL url = new URL(fileUrl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                InputStream inputStream = urlConnection.getInputStream();
                FileOutputStream fileOutputStream = new FileOutputStream(pdfFile);
                int totalSize = urlConnection.getContentLength();
                byte[] buffer = new byte[MEGABYTE];
                int bufferLength = 0;
                while ((bufferLength = inputStream.read(buffer)) > 0) {
                    fileOutputStream.write(buffer, 0, bufferLength);
                }
                fileOutputStream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    interface OnRequestPermission{
        void OnRequestPermission();
    }
    public  void showExoPlayerInFullScreen(boolean isFullscreen, PlayerView simpleExoPlayerView, ConstraintLayout layoutParentTop, LinearLayout layoutParentMiddle, LinearLayout layoutParentReportDetails, LinearLayout layoutParentTitle) {
        if(isFullscreen) {
            layoutParentTitle.setVisibility(View.VISIBLE);
            layoutParentReportDetails.setVisibility(View.VISIBLE);
            layoutParentMiddle.setVisibility(View.VISIBLE);
            LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, layoutHeight);
            layoutParentTop.setLayoutParams(params);
            simpleExoPlayerView.setResizeMode(RESIZE_MODE_FIT);

        }else{
            layoutParentTitle.setVisibility(View.GONE);
            layoutParentReportDetails.setVisibility(View.GONE);
            layoutParentMiddle.setVisibility(View.GONE);
            LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
            layoutParentTop.setLayoutParams(params);
            simpleExoPlayerView.setResizeMode(RESIZE_MODE_FIT);
        }
        fullscreen = !fullscreen;
    }

    public void shareYoutubeVideoScreenShot(View finalConvertView2, int position, YouTubePlayerView youTubePlayerView,  int from, ImageView imageViewItems) {
        half_youTubePlayerHolder.relative_half_content = finalConvertView2.findViewById(R.id.relative_half_contents);
        half_youTubePlayerHolder.with_strip_layout = finalConvertView2.findViewById(R.id.with_strips_layout);
        half_youTubePlayerHolder.with_out_strip_layout = finalConvertView2.findViewById(R.id.with_out_strips_layout);
        half_youTubePlayerHolder.layoutParentReportDetails = finalConvertView2.findViewById(R.id.ll_footer_layout);
        half_youTubePlayerHolder.layoutParentDate = finalConvertView2.findViewById(R.id.layoutParentDate);
        TextView textViewWebUrl=finalConvertView2.findViewById(R.id.textViewWebUrl);
        // half_youTubePlayerHolder.imageViewItems = finalConvertView2.findViewById(R.id.imageViewItem);
        youTubePlayerView.setVisibility(View.GONE);
        half_youTubePlayerHolder.layoutParentReportDetails.setVisibility(View.GONE);
        textViewWebUrl.setVisibility(View.GONE);
        half_youTubePlayerHolder.layoutParentDate.setVisibility(VISIBLE);
        if (list.get(position).getMediaType().equalsIgnoreCase("4")) {
            imageViewItems.setVisibility(View.GONE);
        }
        String appLink = "https://com.anyNews.anynews/";
        half_youTubePlayerHolder.with_out_strip_layout.setVisibility(View.GONE);
        half_youTubePlayerHolder.with_strip_layout.setVisibility(View.VISIBLE);
        imageViewItems.setVisibility(View.VISIBLE);
        String mInvitationUrl = list.get(position).getShareLink();
        String msg = list.get(position).getTitle() + " " + mInvitationUrl;
        countDownTimer = new CountDownTimer(2, 2) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                if (from == 1) {
                    intent.setType("text/plain");
                    intent.setPackage("com.whatsapp");
                }
                intent.putExtra(Intent.EXTRA_SUBJECT, mInvitationUrl.toString());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra(Intent.EXTRA_TEXT, msg);
                Bitmap b = ScreenShott.getInstance().takeScreenShotOfView(half_youTubePlayerHolder.relative_half_content);
                //bitmapResize(b);
                try {
                    File file = new File(context.getExternalCacheDir(), "sample.png");
                    FileOutputStream fOut = new FileOutputStream(file);
                    b.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                    fOut.flush();
                    fOut.close();
                    file.setReadable(true, true);
                    Uri photoURI = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
                    intent.putExtra(Intent.EXTRA_STREAM, photoURI);
                    List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(Intent.createChooser(intent, "Share Link"), PackageManager.MATCH_DEFAULT_ONLY);
                    for (ResolveInfo resolveInfo : resInfoList) {
                        String packageName = resolveInfo.activityInfo.packageName;
                        context.grantUriPermission(packageName, photoURI, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                half_youTubePlayerHolder.with_out_strip_layout.setVisibility(View.VISIBLE);
                half_youTubePlayerHolder.with_strip_layout.setVisibility(View.GONE);
                half_youTubePlayerHolder.layoutParentReportDetails.setVisibility(VISIBLE);
                textViewWebUrl.setVisibility(VISIBLE);
                half_youTubePlayerHolder.layoutParentDate.setVisibility(GONE);
                if (list.get(position).getMediaType().equalsIgnoreCase("4")) {
                    if (isYoutubeVideoIsInitialized) {
                        youTubePlayerViewHalf.pause();
                        youTubePlayerView.setVisibility(VISIBLE);
                        imageViewItems.setVisibility(View.GONE);
                    } else {
                        imageViewItems.setVisibility(VISIBLE);
                    }
                }
                intent.setType("image/*");
                if (context != null && context.getPackageName().equals(BuildConfig.APPLICATION_ID)) {
                    context.startActivity(Intent.createChooser(intent, "Share Link"));
                }
                Share_Count_Value("share", list.get(position).getPostId());

            }
        }.start();
    }

    public void shareExoPlayerVideoScreenShot(View finalConvertView2, int position, int from, PlayerView simpleExoPlayerView, ImageView thaumbnail_for_video, ImageView imageViewExoVideoPlayVideo, ProgressBar progress_bar_for_video) {
        halfVideoExoPlayerHolder.relative_half_content = finalConvertView2.findViewById(R.id.relative_half_contents);
        halfVideoExoPlayerHolder.with_strip_layout = finalConvertView2.findViewById(R.id.with_strips_layout);
        halfVideoExoPlayerHolder.with_out_strip_layout = finalConvertView2.findViewById(R.id.with_out_strips_layout);
        halfVideoExoPlayerHolder.layoutParentReportDetails = finalConvertView2.findViewById(R.id.ll_footer_layout);
        halfVideoExoPlayerHolder.layoutParentDate = finalConvertView2.findViewById(R.id.layoutParentDate);
        TextView textViewWebUrl=finalConvertView2.findViewById(R.id.textViewWebUrl);
        // half_youTubePlayerHolder.imageViewItems = finalConvertView2.findViewById(R.id.imageViewItem);
        simpleExoPlayerView.setVisibility(View.GONE);
        textViewWebUrl.setVisibility(View.GONE);
        halfVideoExoPlayerHolder.layoutParentReportDetails.setVisibility(View.GONE);
        halfVideoExoPlayerHolder.layoutParentDate.setVisibility(VISIBLE);
        imageViewExoVideoPlayVideo.setVisibility(VISIBLE);
        thaumbnail_for_video.setVisibility(VISIBLE);
        String appLink = "https://com.anyNews.anynews/";
        halfVideoExoPlayerHolder.with_out_strip_layout.setVisibility(View.GONE);
        halfVideoExoPlayerHolder.with_strip_layout.setVisibility(View.VISIBLE);
        String mInvitationUrl = list.get(position).getShareLink();
        String msg = list.get(position).getTitle() + " " + mInvitationUrl;
        countDownTimer = new CountDownTimer(2, 2) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                if (from == 1) {
                    intent.setType("text/plain");
                    intent.setPackage("com.whatsapp");
                }
                intent.putExtra(Intent.EXTRA_SUBJECT, mInvitationUrl.toString());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(Intent.EXTRA_TEXT, msg);
                Bitmap b = ScreenShott.getInstance().takeScreenShotOfView(halfVideoExoPlayerHolder.relative_half_content);
                //bitmapResize(b);
                try {
                    File file = new File(context.getExternalCacheDir(), "sample.png");
                    FileOutputStream fOut = new FileOutputStream(file);
                    b.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                    fOut.flush();
                    fOut.close();
                    file.setReadable(true, true);
                    Uri photoURI = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
                    intent.putExtra(Intent.EXTRA_STREAM, photoURI);
                    List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(Intent.createChooser(intent, "Share Link"), PackageManager.MATCH_DEFAULT_ONLY);
                    for (ResolveInfo resolveInfo : resInfoList) {
                        String packageName = resolveInfo.activityInfo.packageName;
                        context.grantUriPermission(packageName, photoURI, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                halfVideoExoPlayerHolder.with_out_strip_layout.setVisibility(View.VISIBLE);
                halfVideoExoPlayerHolder.with_strip_layout.setVisibility(View.GONE);
                halfVideoExoPlayerHolder.layoutParentReportDetails.setVisibility(VISIBLE);
                textViewWebUrl.setVisibility(VISIBLE);
                halfVideoExoPlayerHolder.layoutParentDate.setVisibility(GONE);
                if (imageViewExoVideoPlayVideo.getVisibility() == VISIBLE) {
                    simpleExoPlayerView.setVisibility(GONE);
                    thaumbnail_for_video.setVisibility(VISIBLE);
                    imageViewExoVideoPlayVideo.setVisibility(View.VISIBLE);

                } else {
                    imageViewExoVideoPlayVideo.setVisibility(View.VISIBLE);
                    thaumbnail_for_video.setVisibility(VISIBLE);
                    progress_bar_for_video.setVisibility(View.GONE);
                    if (simpleExoPlayerView.getPlayer() != null) {
                        simpleExoPlayerView.getPlayer().release();
                        simpleExoPlayerView.setPlayer(null);
                    }
                    exoPlayer = null;


                }
                intent.setType("image/*");
                if (context != null && context.getPackageName().equals(BuildConfig.APPLICATION_ID)) {
                    context.startActivity(Intent.createChooser(intent, "Share Link"));
                }
                Share_Count_Value("share", list.get(position).getPostId());

            }
        }.start();


    }

    // --- legacy-flip-test (303c32b3) compat for FlipView / StartingActivity callers on 528 ---
    public static final int DEFAULT_ACTION_ICON_GAP_DP = 2;
    public static final int DEFAULT_FULL_SIDE_ACTION_GAP_DP = 6;

    public static int clampActionIconGapDp(int gapDp) {
        return Math.max(0, Math.min(32, gapDp));
    }

    public static int clampFullSideActionGapDp(int gapDp) {
        return Math.max(0, Math.min(32, gapDp));
    }

    /**
     * Half likes-row chip follows the half switch. Full-image / full-video
     * side button follows the full switch. Footer WhatsApp stays untouched.
     *
     * @return true if any icon's visibility changed
     */
    public static boolean applyActionRowWhatsappVisibility(Context context, View convertView) {
        if (convertView == null) {
            return false;
        }
        int halfVis = ActionRowWhatsappConfig.isHalfEnabled(context)
                ? View.VISIBLE : View.GONE;
        int fullVis = ActionRowWhatsappConfig.isFullEnabled(context)
                ? View.VISIBLE : View.GONE;
        boolean changed = setVisibilityDeep(convertView, R.id.half_whatsapp, halfVis);
        changed |= setVisibilityDeep(convertView, R.id.full_image_whatsapp, fullVis);
        return changed;
    }

    /** Sets visibility on every view with {@code id} in this tree, not only the first. */
    private static boolean setVisibilityDeep(View root, int id, int vis) {
        if (root == null) {
            return false;
        }
        boolean changed = false;
        if (root.getId() == id && root.getVisibility() != vis) {
            root.setVisibility(vis);
            changed = true;
        }
        if (root instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) root;
            for (int i = 0; i < group.getChildCount(); i++) {
                changed |= setVisibilityDeep(group.getChildAt(i), id, vis);
            }
        }
        return changed;
    }

    public static void applyHalfActionIconSizes(Context context, View convertView) {
        if (convertView == null) {
            return;
        }
        final float density =
                convertView.getResources().getDisplayMetrics().density;
        int glyphPx = Math.round(AppUiDefaults.HALF_ACTION_ICON_GLYPH_DP * density);
        int circlePx = Math.round(AppUiDefaults.HALF_ACTION_ICON_CIRCLE_DP * density);
        int slotPx = Math.round(AppUiDefaults.HALF_ACTION_ICON_SLOT_DP * density);
        int gapPx = Math.round(AppUiDefaults.HALF_ACTION_ICON_GAP_DP * density);

        int[] slotIds =
                new int[] {
                    R.id.half_image_like_icon,
                    R.id.half_comment,
                    R.id.half_share,
                    R.id.half_tts,
                    R.id.half_search,
                    R.id.report,
                    R.id.half_whatsapp
                };
        for (int slotId : slotIds) {
            View slot = convertView.findViewById(slotId);
            if (slot == null) {
                continue;
            }
            ViewGroup.LayoutParams lp = slot.getLayoutParams();
            if (lp != null) {
                lp.width = slotPx;
                lp.height = slotPx;
                slot.setLayoutParams(lp);
            }
            if (slotId != R.id.report) {
                ViewGroup.MarginLayoutParams mlp =
                        lp instanceof ViewGroup.MarginLayoutParams
                                ? (ViewGroup.MarginLayoutParams) lp
                                : null;
                if (mlp != null) {
                    mlp.setMarginEnd(gapPx);
                    slot.setLayoutParams(mlp);
                }
            }
        }

        int[] iconIds =
                new int[] {
                    R.id.half_image_like,
                    R.id.half_comment_icon,
                    R.id.half_share_icon,
                    R.id.half_tts_icon,
                    R.id.half_search_icon,
                    R.id.report_icon,
                    R.id.half_whatsapp_icon
                };
        for (int iconId : iconIds) {
            View icon = convertView.findViewById(iconId);
            if (icon == null) {
                continue;
            }
            ViewGroup.LayoutParams lp = icon.getLayoutParams();
            if (lp != null) {
                lp.width = circlePx;
                lp.height = circlePx;
                icon.setLayoutParams(lp);
            }
            if (icon instanceof ImageView) {
                ImageView iv = (ImageView) icon;
                int pad = Math.max(0, (circlePx - glyphPx) / 2);
                iv.setPadding(pad, pad, pad, pad);
            }
        }
    }

    public static void applyFullSideActionGap(Context context, View convertView) {
        if (convertView == null) {
            return;
        }
        int gapPx =
                Math.round(
                        clampFullSideActionGapDp(AppUiDefaults.HALF_ACTION_ICON_GAP_DP)
                                * convertView.getResources().getDisplayMetrics().density);
        View fullWa = convertView.findViewById(R.id.full_image_whatsapp);
        View fullShare = convertView.findViewById(R.id.full_image_share);
        applyGapEnd(fullWa, gapPx);
        applyGapEnd(fullShare, gapPx);
    }

    private static void applyGapEnd(@Nullable View view, int gapPx) {
        if (view == null) {
            return;
        }
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (lp instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) lp;
            mlp.setMarginEnd(gapPx);
            view.setLayoutParams(mlp);
        }
    }

    public static int getPinnedLiveExoPosition() {
        return -1;
    }

    public static boolean isPinnedLiveExoHost(View root) {
        return false;
    }
}