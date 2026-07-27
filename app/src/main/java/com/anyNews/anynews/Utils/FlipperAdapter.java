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

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ClipData;
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
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.MimeTypeMap;
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
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.widget.TextViewCompat;
import androidx.fragment.app.FragmentActivity;
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
import com.anyNews.anynews.Pojo.UserProfileRequest;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Services.ShadowTransformer;
import com.anyNews.anynews.interfaces.CallBackListener;
import com.anyNews.anynews.interfaces.CustomPlayerUiController;
import com.anyNews.anynews.interfaces.LoaderCommonOption;
import com.anyNews.anynews.interfaces.ScreenShott;
import com.anyNews.anynews.interfaces.ShareInterfaceListener;
import com.anyNews.anynews.interfaces.SharePostToWhatsApp;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
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
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.ads.interactivemedia.v3.api.ImaSdkSettings;
import com.google.ads.interactivemedia.v3.api.player.VideoProgressUpdate;
import androidx.annotation.OptIn;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerView;
import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.ExplainReasonCallback;
import com.permissionx.guolindev.callback.ForwardToSettingsCallback;
import com.permissionx.guolindev.callback.RequestCallback;
import com.permissionx.guolindev.request.ExplainScope;
import com.permissionx.guolindev.request.ForwardScope;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerCallback;
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
import java.util.Collections;
import java.util.Date;
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
    /** Visible flip page — buffer pages defer heavy native MediaView attach. */
    private int currentFlipPage = 0;
    private final SparseArray<PendingNativeAdBind> pendingNativeAdBinds = new SparseArray<>();

    private static final class PendingNativeAdBind {
        final FrameLayout placeholder;
        final NativeAd ad;
        final View myView;
        final int fromView;
        final View convertView;
        final boolean facebook;
        final com.facebook.ads.NativeAd fbAd;
        final RelativeLayout fbAdView;
        final NativeAdLayout fbNativeAdLayout;

        PendingNativeAdBind(FrameLayout placeholder, NativeAd ad, View myView, int fromView,
                            View convertView) {
            this.placeholder = placeholder;
            this.ad = ad;
            this.myView = myView;
            this.fromView = fromView;
            this.convertView = convertView;
            this.facebook = false;
            this.fbAd = null;
            this.fbAdView = null;
            this.fbNativeAdLayout = null;
        }

        PendingNativeAdBind(View convertView, com.facebook.ads.NativeAd fbAd,
                            RelativeLayout fbAdView, NativeAdLayout fbNativeAdLayout) {
            this.placeholder = null;
            this.ad = null;
            this.myView = null;
            this.fromView = -1;
            this.convertView = convertView;
            this.facebook = true;
            this.fbAd = fbAd;
            this.fbAdView = fbAdView;
            this.fbNativeAdLayout = fbNativeAdLayout;
        }
    }

    public void setCurrentFlipPage(int page) {
        currentFlipPage = page;
    }

    /** Attach deferred native MediaView when user lands on an ad page. */
    public void bindPendingNativeAdMediaForPage(int page) {
        PendingNativeAdBind pending = pendingNativeAdBinds.get(page);
        if (pending == null) {
            return;
        }
        pendingNativeAdBinds.remove(page);
        try {
            if (pending.facebook) {
                if (pending.fbAd != null && pending.convertView != null) {
                    displayFBNativeAd(pending.convertView, pending.fbAd,
                            pending.fbAdView, pending.fbNativeAdLayout);
                }
            } else if (pending.ad != null && pending.placeholder != null) {
                if (pending.fromView == 0) {
                    displayGoogleFullNativeAd(pending.placeholder, pending.ad,
                            pending.myView, pending.convertView);
                } else if (pending.fromView == 1) {
                    displayGoogleMediumNativeAd(pending.placeholder, pending.ad,
                            pending.myView, pending.convertView);
                } else {
                    displayGoogleNativeAd(pending.placeholder, pending.ad,
                            pending.myView, pending.convertView);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Cap Glide decode size/format on flip hot paths (avoids full-res ARGB decode
     * while Camera flip is capturing adjacent pages).
     */
    private RequestOptions flipGlideDecodeOptions() {
        int w = screenWidth > 0 ? screenWidth : 1080;
        int h = screenHeight > 0 ? screenHeight : 1920;
        return new RequestOptions()
                .format(DecodeFormat.PREFER_RGB_565)
                .override(w, h)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);
    }

    com.facebook.ads.NativeAd fbAd=null;
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
    YouTubePlayerListener listener;

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
    MediaSource mediaSource ;
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
        ImageLoaderInit.ensureInitialized(context);
        imageLoader = ImageLoader.getInstance();
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
        tamil_text_dp = FontSettingsHelper.getProgrammaticBodySize(context, 4);
        hindi_text_dp = FontSettingsHelper.getProgrammaticBodySize(context, 2);
        malayalam_text_dp = FontSettingsHelper.getProgrammaticBodySize(context, 1);
        other_font = FontSettingsHelper.getForLanguage(context, LanguageId).getBodyFontSize();
        android.util.Log.d(
                FontSettingsHelper.TAG,
                "FlipperAdapter init lang=" + LanguageId
                        + " tamil=" + tamil_text_dp
                        + " hindi=" + hindi_text_dp
                        + " malayalam=" + malayalam_text_dp
                        + " other=" + other_font
        );
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

    private void applyStripFont(TextView stripView) {
        if (stripView == null) {
            return;
        }
        float stripSize = FontSettingsHelper.getStripSize(context, LanguageId);
        android.util.Log.d(
                FontSettingsHelper.TAG,
                "applyStripFont lang=" + LanguageId + " stripSize=" + stripSize + " view=" + stripView.getId()
        );
        TextViewCompat.setAutoSizeTextTypeWithDefaults(stripView, TextViewCompat.AUTO_SIZE_TEXT_TYPE_NONE);
        int resId = context.getResources().getIdentifier(
                "_" + Math.round(stripSize) + "sdp",
                "dimen",
                context.getPackageName()
        );
        if (resId != 0) {
            stripView.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(resId));
        } else {
            stripView.setTextSize(TypedValue.COMPLEX_UNIT_SP, stripSize);
        }
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
        list = list__;
        normalizeEngagementCounts(list);
        notifyDataSetChanged();
    }

    /** Pagination append: same notify as setData but keeps call site explicit. */
    public void appendData(ArrayList<ModelClass.Datum> list__, int previousSize) {
        list = list__;
        normalizeEngagementCounts(list);
        notifyDataSetChanged();
    }

    private void normalizeEngagementCounts(ArrayList<ModelClass.Datum> items) {
        if (items == null) return;
        for (ModelClass.Datum item : items) {
            if (item.getLikes() == null) item.setLikes(0);
            if (item.getComments() == null) item.setComments(0);
            if (item.getShares() == null) item.setShares(0);
        }
    }

    private void bindActionCount(TextView tv, Integer count) {
        bindFullPostLikeCount(tv, count, null);
    }

    private void bindHalfActionCount(TextView tv, Integer count) {
        if (tv == null) return;
        int c = count == null ? 0 : count;
        if (c <= 0) {
            // INVISIBLE (not GONE) so the action row keeps a fixed height and
            // the content below does not jump when a count appears/disappears.
            tv.setText("");
            tv.setVisibility(View.INVISIBLE);
            return;
        }
        tv.setText(formatCount(c));
        // Count sits below the icon as plain text. Color comes from admin
        // "Engagement Icon Counts → Count color" (engagement_badge_color via
        // splash + articles API); default #FF6300 when unset. Same color also
        // tints the four action icons (see tintHalfActionIcons).
        tv.setBackground(null);
        tv.setTextColor(EngagementCountHelper.getCountColor(context));
        tv.setTypeface(tv.getTypeface(), Typeface.BOLD);
        tv.setVisibility(View.VISIBLE);
    }

    // Half-feed like icon: not-liked = OUTLINE heart; liked = SOLID FILLED heart,
    // each sitting inside the old white circular chip (white_oval). Tint follows
    // admin engagement_badge_color (same as counts). Both heart drawables share
    // 24x24 bounds so toggling never shifts layout.
    private void bindHalfLikeIcon(ImageView icon, Integer status) {
        if (icon == null) return;
        boolean liked = status != null && status == 1;
        icon.setBackgroundResource(R.drawable.white_oval);
        icon.setImageResource(liked ? R.drawable.ic_action_like_filled : R.drawable.ic_action_like);
        EngagementCountHelper.applyIconTint(context, icon);
    }

    /** Apply admin engagement color to like/comment/share/flag on half posts. */
    private void tintHalfActionIcons(View convertView) {
        if (convertView == null) return;
        EngagementCountHelper.applyIconTint(context, convertView.findViewById(R.id.half_image_like));
        EngagementCountHelper.applyIconTint(context, convertView.findViewById(R.id.half_comment_icon));
        EngagementCountHelper.applyIconTint(context, convertView.findViewById(R.id.half_share_icon));
        EngagementCountHelper.applyIconTint(context, convertView.findViewById(R.id.share));
        EngagementCountHelper.applyIconTint(context, convertView.findViewById(R.id.report_icon));
        EngagementCountHelper.applyIconTint(context, convertView.findViewById(R.id.video_report));
    }

    private void bindLikeCountForView(TextView countView, int likes) {
        if (countView == null) return;
        if (countView.getId() == R.id.half_like_count) {
            bindHalfActionCount(countView, likes);
        } else {
            bindActionCount(countView, likes);
        }
    }

    private void bindHalfPostEngagementCounts(View convertView, int position) {
        bindHalfActionCount(convertView.findViewById(R.id.half_like_count), list.get(position).getLikes());
        bindHalfActionCount(convertView.findViewById(R.id.half_comment_count), list.get(position).getComments());
        bindHalfActionCount(convertView.findViewById(R.id.half_share_count), list.get(position).getShares());
        tintHalfActionIcons(convertView);
        applyActionIconRowOffset(convertView);
        applyHalfActionIconSizes(context, convertView);
        applyTitleBodyGap(context, convertView);
        applyContentBlockOffset(context, convertView);
    }

    // Body/description TextViews across the half-image and half-video layouts (plain
    // + "__" video variants + language variants). Missing ids are skipped safely.
    private static final int[] HALF_BODY_TEXT_IDS = new int[] {
            R.id.half_content_description, R.id.tamil_half_content_description,
            R.id.english_half_content_description, R.id.half_content_description__,
            R.id.tamil_half_content_description__, R.id.english_half_content_description__
    };

    private static final int DEFAULT_TITLE_BODY_GAP_DP = 0;
    private static final int TITLE_BODY_GAP_MIN_DP = -16;
    private static final int TITLE_BODY_GAP_MAX_DP = 40;

    /**
     * Admin-controlled gap (dp) between the post title and the body paragraph on
     * half posts. Reads the value delivered via splash/feed config, clamps it,
     * converts dp→px and applies it as the body TextView's top margin at bind time.
     * By-id across layout variants; default 0 reproduces the current look. Runtime
     * only; no layout rebuild.
     */
    public static void applyTitleBodyGap(Context context, View convertView) {
        if (context == null || convertView == null) {
            return;
        }
        int gapDp = DEFAULT_TITLE_BODY_GAP_DP;
        try {
            gapDp = SharedPrefsData.getInt(context, Constants.TITLE_BODY_GAP_DP, Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
        gapDp = Math.max(TITLE_BODY_GAP_MIN_DP, Math.min(TITLE_BODY_GAP_MAX_DP, gapDp));
        int gapPx = Math.round(gapDp * context.getResources().getDisplayMetrics().density);
        for (int id : HALF_BODY_TEXT_IDS) {
            View body = convertView.findViewById(id);
            if (body == null) continue;
            ViewGroup.LayoutParams lp = body.getLayoutParams();
            if (!(lp instanceof ViewGroup.MarginLayoutParams)) continue;
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) lp;
            mlp.topMargin = gapPx;
            body.setLayoutParams(mlp);
        }
    }

    private static final int DEFAULT_CONTENT_BLOCK_OFFSET_DP = 0;
    private static final int CONTENT_BLOCK_OFFSET_MIN_DP = -40;
    private static final int CONTENT_BLOCK_OFFSET_MAX_DP = 40;

    /**
     * Admin-controlled vertical offset (dp) of the whole heading+body content block
     * on half posts. Reads the value delivered via splash/feed config, clamps it,
     * converts dp→px and applies it as the content container's top margin. Negative
     * pulls the block up (closes the gap left when the action icons shrink); positive
     * pushes it down. Applied by id across layout variants; default 0 reproduces the
     * current look. Runtime only; no layout rebuild.
     */
    public static void applyContentBlockOffset(Context context, View convertView) {
        if (context == null || convertView == null) {
            return;
        }
        View container = convertView.findViewById(R.id.layoutParentTitle);
        if (container == null) {
            return;
        }
        ViewGroup.LayoutParams lp = container.getLayoutParams();
        if (!(lp instanceof ViewGroup.MarginLayoutParams)) {
            return;
        }
        int offsetDp = DEFAULT_CONTENT_BLOCK_OFFSET_DP;
        try {
            offsetDp = SharedPrefsData.getInt(context, Constants.CONTENT_BLOCK_OFFSET_DP, Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
        offsetDp = Math.max(CONTENT_BLOCK_OFFSET_MIN_DP, Math.min(CONTENT_BLOCK_OFFSET_MAX_DP, offsetDp));
        int offsetPx = Math.round(offsetDp * context.getResources().getDisplayMetrics().density);
        ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) lp;
        mlp.topMargin = offsetPx;
        container.setLayoutParams(mlp);
    }

    // White-circle chip ImageViews (have the white_oval background) across the
    // half-image and half-video layouts. Missing ids are skipped safely.
    private static final int[] HALF_ACTION_ICON_IDS = new int[] {
            R.id.half_image_like, R.id.half_comment_icon, R.id.half_share_icon,
            R.id.share, R.id.report_icon, R.id.video_report
    };

    // Tappable slot containers that wrap each action icon (image + video layouts).
    private static final int[] HALF_ACTION_SLOT_IDS = new int[] {
            R.id.half_image_like_icon, R.id.half_comment, R.id.half_share,
            R.id.half_share__, R.id.report
    };

    // Defaults + clamps mirror the backend (Root_Settings / Update_Configuration).
    private static final int DEFAULT_ACTION_ICON_SIZE_DP = 21;
    private static final int ACTION_ICON_SIZE_MIN_DP = 10;
    private static final int ACTION_ICON_SIZE_MAX_DP = 40;
    private static final int DEFAULT_LIKE_CIRCLE_SIZE_DP = 35;
    private static final int LIKE_CIRCLE_SIZE_MIN_DP = 20;
    private static final int LIKE_CIRCLE_SIZE_MAX_DP = 52;
    // The tappable slot is the white circle plus this dp padding (42dp vs 35dp
    // default), so the row footprint shrinks/grows with the circle.
    private static final int ACTION_SLOT_EXTRA_DP = 7;

    /**
     * Admin-controlled sizing of the four half-post action icons (like/comment/
     * share/report), their white circle chip, and the tappable slot. Reads the dp
     * values delivered via splash/feed config, clamps them, converts dp→px and
     * updates each view's LayoutParams + icon padding at bind time. Runtime only;
     * no layout rebuild. Defaults reproduce the current 35dp circle / 21dp glyph.
     */
    public static void applyHalfActionIconSizes(Context context, View convertView) {
        if (context == null || convertView == null) {
            return;
        }
        int circleDp = DEFAULT_LIKE_CIRCLE_SIZE_DP;
        int iconDp = DEFAULT_ACTION_ICON_SIZE_DP;
        try {
            int storedCircle = SharedPrefsData.getInt(context, Constants.LIKE_CIRCLE_SIZE_DP, Constants.PREF_NAME);
            if (storedCircle > 0) circleDp = storedCircle;
        } catch (Exception ignored) {
        }
        try {
            int storedIcon = SharedPrefsData.getInt(context, Constants.ACTION_ICON_SIZE_DP, Constants.PREF_NAME);
            if (storedIcon > 0) iconDp = storedIcon;
        } catch (Exception ignored) {
        }
        circleDp = Math.max(LIKE_CIRCLE_SIZE_MIN_DP, Math.min(LIKE_CIRCLE_SIZE_MAX_DP, circleDp));
        iconDp = Math.max(ACTION_ICON_SIZE_MIN_DP, Math.min(ACTION_ICON_SIZE_MAX_DP, iconDp));
        // The glyph can never be larger than the circle it sits inside.
        if (iconDp > circleDp) iconDp = circleDp;

        float density = context.getResources().getDisplayMetrics().density;
        int circlePx = Math.round(circleDp * density);
        int slotPx = Math.round((circleDp + ACTION_SLOT_EXTRA_DP) * density);
        int iconPaddingPx = Math.max(0, Math.round(((circleDp - iconDp) / 2f) * density));

        for (int id : HALF_ACTION_SLOT_IDS) {
            View slot = convertView.findViewById(id);
            if (slot == null) continue;
            ViewGroup.LayoutParams lp = slot.getLayoutParams();
            if (lp == null) continue;
            lp.width = slotPx;
            lp.height = slotPx;
            slot.setLayoutParams(lp);
        }

        for (int id : HALF_ACTION_ICON_IDS) {
            View icon = convertView.findViewById(id);
            if (icon == null) continue;
            ViewGroup.LayoutParams lp = icon.getLayoutParams();
            if (lp == null) continue;
            lp.width = circlePx;
            lp.height = circlePx;
            icon.setLayoutParams(lp);
            icon.setPadding(iconPaddingPx, iconPaddingPx, iconPaddingPx, iconPaddingPx);
        }
    }

    /**
     * Admin-controlled vertical position of the four action icons (like/comment/
     * share/report) on half posts. Reads the dp offset saved from the splash/
     * articles config and shifts the icon row with translationY. Negative = up,
     * positive = down, 0 = unchanged (default). Runtime only; no layout rebuild.
     */
    private void applyActionIconRowOffset(View convertView) {
        if (convertView == null) {
            return;
        }
        View iconRow = convertView.findViewById(R.id.with_out_strips_layout);
        if (iconRow == null) {
            return;
        }
        int offsetDp = 0;
        try {
            offsetDp = SharedPrefsData.getInt(context, Constants.ACTION_ICON_ROW_OFFSET, Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
        // Clamp to the same range the admin/backend enforce.
        if (offsetDp < -40) offsetDp = -40;
        if (offsetDp > 40) offsetDp = 40;
        float px = offsetDp * context.getResources().getDisplayMetrics().density;
        iconRow.setTranslationY(px);
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

    private void bindFullPostLikeCount(TextView tv, Integer count, String likeLabel) {
        if (tv == null) return;
        // Always show the numeric total (including 0) so full_image / full_video posts
        // display like/comment/share counts consistently across every tab (reload,
        // my-feed, categories) just like half/normal posts do. Previously a 0 count fell
        // back to the "Like"/"Comment" text label, which made the reload feed (dominated
        // by full posts) look like it had no engagement counts.
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
        bindFullPostLikeCount(holder.full_img_like, list.get(position).getLikes(), getLikeLabel());
        bindFullPostLikeCount(holder.full_img_comment, list.get(position).getComments(), getCommentLabel());
        bindFullPostLikeCount(holder.full_img_share, list.get(position).getShares(), getShareLabel());
    }

    private void bindFullVideoEngagementCounts(TextView likeView, TextView commentView, TextView shareView, int position) {
        bindFullPostLikeCount(likeView, list.get(position).getLikes(), getLikeLabel());
        bindFullPostLikeCount(commentView, list.get(position).getComments(), getCommentLabel());
        bindFullPostLikeCount(shareView, list.get(position).getShares(), getShareLabel());
    }

    /**
     * MAGAZINE-ONLY share highlight. A true attention BLINK (icon clearly flashes
     * on/off) plus a slight scale bounce, on a subtle dark translucent rounded chip
     * so a clean white share icon reads well over colorful magazine artwork.
     * No red ring, no solid circle. All other post types keep their original look.
     */
    private void startMagazineShareBlink(ImageView shareIcon) {
        if (shareIcon == null) {
            return;
        }
        // Clean white icon on a subtle dark chip for contrast against busy posters.
        shareIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        shareIcon.setColorFilter(Color.WHITE);
        shareIcon.setBackgroundResource(R.drawable.bg_share_blink_chip);

        shareIcon.clearAnimation();
        Animation blink = AnimationUtils.loadAnimation(context, R.anim.blink_share);
        shareIcon.startAnimation(blink);
    }

    private int getVideoLayoutTopHeight() {
        if (Build.VERSION.SDK_INT >= 35) {
            return layoutHeight - 85;
        }
        return layoutHeight;
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
        int newStatus;

        // Like toggle: neutral <-> liked (dislike removed from UI).
        if (oldStatus == 1) {
            newStatus = 0;
            likes = Math.max(0, likes - 1);
        } else {
            newStatus = 1;
            likes++;
        }

        item.setLikeStatus(newStatus);
        item.setLikes(likes);

        showLog("LIKE_DEBUG pos=" + position + " oldStatus=" + oldStatus + " newStatus=" + newStatus
                + " newLikes=" + likes);

        if (countView != null) {
            bindLikeCountForView(countView, likes);
        }
        countforshareanddownload(item.getPostId(), item.getSubPostId(), newStatus);
        return newStatus;
    }

    private int applyDislikeChange(int position, TextView countView) {
        ModelClass.Datum item = list.get(position);
        int oldStatus = item.getLikeStatus() != null ? item.getLikeStatus() : 0;
        int likes = item.getLikes() != null ? item.getLikes() : 0;
        int newStatus = oldStatus == 2 ? 0 : 2;
        if (oldStatus == 1) likes = Math.max(0, likes - 1);
        item.setLikeStatus(newStatus);
        item.setLikes(likes);
        if (countView != null) bindLikeCountForView(countView, likes);
        countforshareanddownload(item.getPostId(), item.getSubPostId(), newStatus);
        return newStatus;
    }

    private void bindThreeStateIcon(ImageView icon, Integer status) {
        if (icon == null) return;
        if (status != null && status == 1) icon.setImageResource(ic_like_half_selected1);
        else icon.setImageResource(ic_like_half1);
    }

    private void hideDislikeView(View dislikeView) {
        if (dislikeView != null) {
            dislikeView.setVisibility(View.GONE);
        }
    }

    private void syncLikeCountFromServer(Long postId, Long subPostId, Integer serverLikes) {
        syncEngagementFromServer(postId, subPostId, serverLikes, null, null);
    }

    private void syncEngagementFromServer(Long postId, Long subPostId, Integer serverLikes,
                                          Integer serverComments, Integer serverShares) {
        if (postId == null || list == null) return;
        for (int i = 0; i < list.size(); i++) {
            ModelClass.Datum item = list.get(i);
            if (item.getPostId() != null && item.getPostId().equals(postId)
                    && (subPostId == null || subPostId.equals(item.getSubPostId()))) {
                if (serverLikes != null) item.setLikes(serverLikes);
                if (serverComments != null) item.setComments(serverComments);
                if (serverShares != null) item.setShares(serverShares);
                notifyDataSetChanged();
                break;
            }
        }
    }

    private int findPostIndex(long postId) {
        if (list == null) return -1;
        for (int i = 0; i < list.size(); i++) {
            ModelClass.Datum item = list.get(i);
            if (item.getPostId() != null && item.getPostId() == postId) {
                return i;
            }
        }
        return -1;
    }

    public void incrementCommentCount(int position) {
        if (list == null || position < 0 || position >= list.size()) return;
        ModelClass.Datum item = list.get(position);
        int comments = item.getComments() != null ? item.getComments() : 0;
        item.setComments(comments + 1);
        notifyDataSetChanged();
    }

    public void syncCommentCountFromServer(Long postId, Long subPostId, Integer serverComments) {
        syncEngagementFromServer(postId, subPostId, null, serverComments, null);
    }

    private void bindDislikeIcon(ImageView icon, Integer status) {
        if (icon == null) return;
        icon.setImageResource(status != null && status == 2
                ? ic_selected_dislike_icon : ic_new_dislike_icon);
    }

    @Override
    public void shareListener() {
        showLog("shareListener:*********");

    }


    public interface Callbacks {
        public void onPageRequested(int page);
        public void onRequestedPermission(int page);
        public void callLocationRequest(int from);
    }

    public void setCallback(Callbacks callbacks) {
        this.callbacks=callbacks;
    }

    @Override
    public boolean hasStableIds() {
        return true;
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
            return (album.getPostId());
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }


    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (list == null || position < 0 || position >= list.size()) {
            if (convertView == null) {
                convertView = new View(parent.getContext());
            }
            return convertView;
        }
        String pagePostType = list.get(position).getPostType();
        if (BuildConfig.DEBUG) {
            showLog("getView pos=" + position + " type=" + pagePostType
                    + " postId=" + list.get(position).getPostId());
        }
        customConvertView=convertView;
        customPosition=position;

        if (pagePostType == null || pagePostType.trim().isEmpty()) {
            // Avoid NPE in switch + empty scrap: unknown/null types used to return a blank page.
            showLog("getView blank page: null/empty post_type at pos=" + position
                    + " postId=" + list.get(position).getPostId());
            convertView = inflater.inflate(R.layout.share_app_layout, parent, false);
            showShareScreen(convertView);
            return convertView;
        }

        try {
            if (!list.isEmpty()) {

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
                        if (hasActiveVideoPlayer()) {
                            pauseVideo();
                        }
                        halfImageActivity = new HalfImageActivityHolder();
                        showLog("Check Point Adapter:: 2");
                        // Reuse scrap when layout type matches — avoid reinflating the large
                        // half-image XML on every flip (biggest flip-lag win). Telugu uses a
                        // different layout, so tag includes that.
                        String normalImageKey = AppHelper.isTeluguLanguageSelected()
                                ? "normal_image_telugu" : "normal_image";
                        if (!canReuseFlipView(convertView, normalImageKey)) {
                            if (AppHelper.isTeluguLanguageSelected()) {
                                convertView = inflater.inflate(R.layout.activity_half_image_telugu, parent, false);
                            } else {
                                convertView = inflater.inflate(R.layout.activity_half_image_new, parent, false);
                            }
                            markFlipViewType(convertView, normalImageKey);
                        }
                        customConvertView = convertView;
                        showLog("Check Point Adapter:: 3_11");
                        fromWhatsapp = false;
                        showNormalImage(position, convertView);
                        showLog("NORMAL_IMAGE Point Adapter:: 3_12");
                        break;
                    case "full_image":
                        if (hasActiveVideoPlayer()) {
                            pauseVideo();
                        }
                        showLog("Check Point FULL_IMAGE:: 1");
                        fullImageActivity = new FullImageActivityHolder();
                        if (!canReuseFlipView(convertView, "full_image")) {
                            convertView = inflater.inflate(R.layout.activity_full_image_new, parent, false);
                            markFlipViewType(convertView, "full_image");
                        }
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
                        String mediaType = list.get(position).getMediaType();
                        if ("5".equals(mediaType)) {
                            full_videoViewHolder = new FullVideoViewHolder();
                            convertView = inflater.inflate(R.layout.activity_full_video, parent, false);
                            showFullVideoWithExoPlayer(position, convertView);
                        }
                        // full youtube video
                        else if ("6".equals(mediaType)) {
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
                        String halfMediaType = list.get(position).getMediaType();
                        if ("3".equals(halfMediaType)) {
                            halfVideoExoPlayerHolder = new HalfVideoExoPlayerHolder();
                            if (AppHelper.isTeluguLanguageSelected()) {
                                convertView = inflater.inflate(R.layout.activity_half_exo_video_detailes_telugu, parent, false);
                            } else {
                                convertView = inflater.inflate(R.layout.activity_half_exo_video_detailes, parent, false);
                            }
                            showHalfPostExoPlayerVideo(convertView, position);
                        } else if ("4".equals(halfMediaType)) {
                            half_youTubePlayerHolder = new HalfYouTubePlayerHolder();
                            if (AppHelper.isTeluguLanguageSelected()) {
                                convertView = inflater.inflate(R.layout.activity_half_youtube_video_detailes_telugu, parent, false);
                            } else {
                                convertView = inflater.inflate(R.layout.activity_half_youtube_video_detailes, parent, false);
                            }
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
                        // Poster-corner share removed: footer glow Share replaces it for magazines.
                        if (imageViewShareMag != null) {
                            imageViewShareMag.clearAnimation();
                            imageViewShareMag.setOnClickListener(null);
                            imageViewShareMag.setVisibility(GONE);
                        }
                        // Ensure footer center "+" morphs to glow Share (HomePage sync).
                        try {
                            if (context instanceof HomePage) {
                                ((HomePage) context).onMagazinePageBound(position);
                            } else if (activity instanceof HomePage) {
                                ((HomePage) activity).onMagazinePageBound(position);
                            }
                        } catch (Exception magBindEx) {
                            android.util.Log.e("MagazineGlow", "onMagazinePageBound failed: "
                                    + magBindEx.getMessage());
                        }
                        final RelativeLayout relativeLayoutShareOpt = convertView.findViewById(R.id.relativeLayoutShareOpt);
                        final TextView textViewAllPage = convertView.findViewById(R.id.textViewAllPage);
                        final TextView textViewSinglePage = convertView.findViewById(R.id.textViewSinglePage);
                        final ImageView imageViewShareClose = convertView.findViewById(R.id.imageViewShareClose);
                        //imageGallaryHolder.galleryLayout.setLayerType(View.LAYER_TYPE_NONE,null);
                        relativeLayoutShareOpt.setVisibility(GONE);
                        imageViewShareClose.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                relativeLayoutShareOpt.setVisibility(GONE);
                                if (imageViewShareMag != null) {
                                    imageViewShareMag.setVisibility(GONE);
                                }
                                fromWhatsapp = false;
                                image_magazineHolder.magzine_ll_dots.setVisibility(VISIBLE);
                            }
                        });
                        textViewAllPage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                relativeLayoutShareOpt.setVisibility(GONE);
                                shareFullMagazine(position);
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
                            Glide.with(context).load(list.get(position).getBackgroundImage())
                                    .apply(flipGlideDecodeOptions())
                                    .into(new SimpleTarget<Drawable>() {
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
                        showLog("AD Point Adapter:: START");
                        pauseVideo();
                        String TAG = "AD DATA";
                        adsScreenViewHolder = new AdsScreenViewHolder();
                        adapterCallback.closeBottomStrip(true);
                        showLog("HomePage getAd_type::" + list.get(position).getAd_type());
                        showLog("HomePage getAd_type::" + list.get(position).getAd_type());
                        if (list.get(position).getAd_type().equalsIgnoreCase("google")) {
                            convertView = inflater.inflate(R.layout.ads_screen, parent, false);
                            final RelativeLayout layoutParent = convertView.findViewById(R.id.layoutParent);
                            layoutParent.getLayoutParams().height = screenHeight;
                            final FrameLayout fl_adplaceholder = convertView.findViewById(R.id.fl_adplaceholder);
                            fl_adplaceholder.setVisibility(VISIBLE);
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
                                    Integer slotPos = list.get(position).getAd_req_position();
                                    if (slotPos != null
                                            && HomePage.nativeGoogleAdList.get(i).getReqPos() == slotPos) {
                                        showLog("HomePage.nativeAdList displaying native ad:");
                                        ad = HomePage.nativeGoogleAdList.get(i).getAd();
                                        nativeAd = HomePage.nativeGoogleAdList.get(i).getAd();
                                        break;
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
                                    // Buffer/preload pages: skip heavy MediaView attach until page is current.
                                    if (position != currentFlipPage) {
                                        showLog("Defer native MediaView bind for buffer ad page=" + position);
                                        pendingNativeAdBinds.put(position, new PendingNativeAdBind(
                                                fl_adplaceholder, finalAd, finalMyView, finalFromView, finalConvertView2));
                                        fl_adplaceholder.removeAllViews();
                                        ProgressBar adPlaceholder = new ProgressBar(context);
                                        fl_adplaceholder.addView(adPlaceholder,
                                                new FrameLayout.LayoutParams(
                                                        ViewGroup.LayoutParams.WRAP_CONTENT,
                                                        ViewGroup.LayoutParams.WRAP_CONTENT,
                                                        android.view.Gravity.CENTER));
                                    } else {
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
                                            }
                                        }, 500);
                                    }

                                } else {
                                    showLog("SHARE #1");
                                    convertView = inflater.inflate(R.layout.share_app_layout, parent, false);
                                    showShareScreen(convertView);
                                }
                            } else {
                                showLog("SHARE #2");
                                convertView = inflater.inflate(R.layout.share_app_layout, parent, false);
                                showShareScreen(convertView);
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
                                    if (position != currentFlipPage) {
                                        showLog("Defer FB native MediaView bind for buffer ad page=" + position);
                                        pendingNativeAdBinds.put(position, new PendingNativeAdBind(
                                                finalConvertView1, fbAd, adView, nativeAdLayout));
                                    } else {
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                displayFBNativeAd(finalConvertView1, fbAd, adView, nativeAdLayout);
                                            }
                                        }, 500);
                                    }
                                } else {
                                    showLog(" fb ad with share:");
                                    convertView = inflater.inflate(R.layout.share_app_layout, parent, false);
                                    showShareScreen(convertView);
                                }
                            } else {
                                showLog(" fb ad with share:");
                                convertView = inflater.inflate(R.layout.share_app_layout, parent, false);
                                showShareScreen(convertView);
                            }
                        }
                        showLog("AD Point Adapter:: END");
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
                        editTextAppReview.setVisibility(GONE);
                        buttonAppSubmit.setVisibility(GONE);
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
                        // Magazines specials (cat 15) get footer glow Share; normal galleries keep corner share.
                        final boolean galleryUsesGlowShare = HomePage.usesMagazineGlowShare(list.get(position));
                        if (galleryUsesGlowShare) {
                            try {
                                if (context instanceof HomePage) {
                                    ((HomePage) context).onMagazinePageBound(position);
                                } else if (activity instanceof HomePage) {
                                    ((HomePage) activity).onMagazinePageBound(position);
                                }
                            } catch (Exception galBindEx) {
                                android.util.Log.e("MagazineGlow", "image_gallery onMagazinePageBound failed: "
                                        + galBindEx.getMessage());
                            }
                        }
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
                        if (imageGallaryHolder.image_gallary_share != null) {
                            // On-image corner Share removed for all galleries — the footer
                            // Share button already covers sharing for image_gallery posts.
                            imageGallaryHolder.image_gallary_share.clearAnimation();
                            imageGallaryHolder.image_gallary_share.setOnClickListener(null);
                            imageGallaryHolder.image_gallary_share.setVisibility(GONE);
                        }
                        //imageGallaryHolder.indicator.initialize(config);
                        List<String> galleryImages = list.get(position).getImages();
                        if (galleryImages != null && !galleryImages.isEmpty()) {
                            image_gallary.addAll(galleryImages);
                        } else {
                            image_gallary.add("");
                        }
                        viewpager.setAdapter(new SamplePagerAdapter(context, image_gallary, 0, false));
                        // added to increase speed in view pager
                        viewpager.setOffscreenPageLimit(Math.max(1, Math.min(2, image_gallary.size() - 1)));
                        if (imageGallaryHolder.indicator != null) {
                            imageGallaryHolder.indicator.setViewPager(viewpager);
                        }
                        if (LanguageId == 4) {
                            imageGallaryHolder.strip_text_name.setText("" + list.get(position).getStrip_content());
                            applyStripFont(imageGallaryHolder.strip_text_name);

                        } else if (LanguageId == 1) {
                            imageGallaryHolder.strip_text_name.setText("" + list.get(position).getStrip_content());
                            applyStripFont(imageGallaryHolder.strip_text_name);

                        } else {
                            imageGallaryHolder.strip_text_name.setText("" + list.get(position).getStrip_content());
                            applyStripFont(imageGallaryHolder.strip_text_name);

                        }
                        View finalConvertView = convertView;
                        imageGallaryHolder.imageViewShareClose.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                fromWhatsapp = false;
                                imageGallaryHolder.relativeLayoutShareOpt = finalConvertView.findViewById(R.id.relativeLayoutShareOpt);
                                imageGallaryHolder.image_gallary_share = finalConvertView.findViewById(R.id.image_gallary_share);
                                imageGallaryHolder.dotIndicatior = finalConvertView.findViewById(R.id.dotIndicatior);
                                imageGallaryHolder.relativeLayoutShareOpt.setVisibility(GONE);
                                if (imageGallaryHolder.image_gallary_share != null) {
                                    imageGallaryHolder.image_gallary_share.setVisibility(GONE);
                                }
                                imageGallaryHolder.dotIndicatior.setVisibility(VISIBLE);

                            }
                        });
                        imageGallaryHolder.textViewAllPage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                shareFullMagazine(position);
                                imageGallaryHolder.relativeLayoutShareOpt = finalConvertView.findViewById(R.id.relativeLayoutShareOpt);
                                imageGallaryHolder.image_gallary_share = finalConvertView.findViewById(R.id.image_gallary_share);
                                imageGallaryHolder.dotIndicatior = finalConvertView.findViewById(R.id.dotIndicatior);
                                imageGallaryHolder.relativeLayoutShareOpt.setVisibility(GONE);
                                if (imageGallaryHolder.image_gallary_share != null) {
                                    imageGallaryHolder.image_gallary_share.setVisibility(GONE);
                                }
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
                                if (imageGallaryHolder.image_gallary_share != null) {
                                    imageGallaryHolder.image_gallary_share.setVisibility(GONE);
                                }
                                imageGallaryHolder.dotIndicatior.setVisibility(VISIBLE);
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
                        bindSurveyPollTwoOption(surveyViewHolder, convertView, position);
                        break;

                    case "survey_poll":
                        pauseVideo();
                        pollingViewHolder = new PollingViewHolder();
                        convertView = inflater.inflate(R.layout.activity_polling_page_flip, parent, false);
                        bindSurveyPollMultiOption(pollingViewHolder, convertView, position);
                        break;

                }
            }

        } catch (Exception e) {
            // Inflate / ClassCast / NPE on survey-poll bind must not kill the feed.
            e.printStackTrace();
            if (convertView == null && parent != null) {
                convertView = new View(parent.getContext());
            }
        }

        return convertView;

    }

    /**
     * FlipView scrap reuse: only keep convertView when it was inflated for the same
     * post layout key. Video/ads/gallery/etc. stay inflate-always (stateful).
     */
    private static boolean canReuseFlipView(View convertView, String typeKey) {
        return convertView != null && typeKey != null && typeKey.equals(convertView.getTag());
    }

    private static void markFlipViewType(View view, String typeKey) {
        if (view != null) {
            view.setTag(typeKey);
        }
    }

    private void showVastTagAd(View convertView, int position) {
        showLog("showVastTagAd:"+position);
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
                                                adsManager[0].destroy();
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
        if (convertView == null) {
            return;
        }
        ImageView imageViewShare=convertView.findViewById(R.id.imageViewShare);
        ImageView imageViewWhatsapp=convertView.findViewById(R.id.imageViewWhatsapp);
        if (imageViewShare == null || imageViewWhatsapp == null) {
            return;
        }
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
        fromWhatsapp=true;
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

                Double starRating = ad.getStarRating();
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
        if (finalConvertView == null || finalConvertView.findViewById(R.id.relativeLayoutShareOpt) == null) {
            return;
        }
        if (imageGallaryHolder == null) {
            imageGallaryHolder = new ImageGallaryHolder();
        }
        imageGallaryHolder.relativeLayoutShareOpt = finalConvertView.findViewById(R.id.relativeLayoutShareOpt);
        imageGallaryHolder.image_gallary_share = finalConvertView.findViewById(R.id.image_gallary_share);
        imageGallaryHolder.dotIndicatior = finalConvertView.findViewById(R.id.dotIndicatior);

        if (imageGallaryHolder.relativeLayoutShareOpt != null) {
            imageGallaryHolder.relativeLayoutShareOpt.setVisibility(GONE);
        }
        if (imageGallaryHolder.image_gallary_share != null) {
            // Keep on-image corner share hidden — footer Share owns gallery sharing.
            imageGallaryHolder.image_gallary_share.clearAnimation();
            imageGallaryHolder.image_gallary_share.setOnClickListener(null);
            imageGallaryHolder.image_gallary_share.setVisibility(GONE);
        }
        if (imageGallaryHolder.dotIndicatior != null) {
            imageGallaryHolder.dotIndicatior.setVisibility(VISIBLE);
        }
    }

    public void shareGalleryScreenShotUI(View finalConvertView) {
        fromWhatsapp=true;
        if (finalConvertView == null || finalConvertView.findViewById(R.id.relativeLayoutShareOpt) == null) {
            return;
        }
        if (imageGallaryHolder == null) {
            imageGallaryHolder = new ImageGallaryHolder();
        }
        imageGallaryHolder.relativeLayoutShareOpt = finalConvertView.findViewById(R.id.relativeLayoutShareOpt);
        imageGallaryHolder.image_gallary_share = finalConvertView.findViewById(R.id.image_gallary_share);
        imageGallaryHolder.dotIndicatior = finalConvertView.findViewById(R.id.dotIndicatior);

        if (imageGallaryHolder.relativeLayoutShareOpt != null) {
            imageGallaryHolder.relativeLayoutShareOpt.setVisibility(VISIBLE);
        }
        if (imageGallaryHolder.image_gallary_share != null) {
            imageGallaryHolder.image_gallary_share.setVisibility(GONE);
        }
        if (imageGallaryHolder.dotIndicatior != null) {
            imageGallaryHolder.dotIndicatior.setVisibility(GONE);
        }

    }
    public void shareGalleryScreenShot(int position,int from,View customConvertView) {
        if (customConvertView == null || customConvertView.findViewById(R.id.relativeLayoutShareOpt) == null) {
            return;
        }
        if (imageGallaryHolder == null) {
            imageGallaryHolder = new ImageGallaryHolder();
        }
        imageGallaryHolder.relativeLayoutShareOpt = customConvertView.findViewById(R.id.relativeLayoutShareOpt);
        imageGallaryHolder.image_gallary_share = customConvertView.findViewById(R.id.image_gallary_share);
        imageGallaryHolder.dotIndicatior = customConvertView.findViewById(R.id.dotIndicatior);
        imageGallaryHolder.image_gallary_scrip = customConvertView.findViewById(R.id.image_gallary_scrip);
        RelativeLayout scrren_icon = customConvertView.findViewById(R.id.scrren_icon);
        if (imageGallaryHolder.relativeLayoutShareOpt != null) {
            imageGallaryHolder.relativeLayoutShareOpt.setVisibility(GONE);
        }
        if (imageGallaryHolder.dotIndicatior != null) {
            imageGallaryHolder.dotIndicatior.setVisibility(VISIBLE);
        }
        if (imageGallaryHolder.image_gallary_scrip != null) {
            imageGallaryHolder.image_gallary_scrip.setVisibility(View.GONE);
        }
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
                if (imageGallaryHolder.image_gallary_scrip != null) {
                    imageGallaryHolder.image_gallary_scrip.setVisibility(View.GONE);
                }
                if (imageGallaryHolder.image_gallary_share != null) {
                    // Do not restore corner share after screenshot share — footer owns it.
                    imageGallaryHolder.image_gallary_share.setVisibility(GONE);
                }
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
                if(fromWhatsapp){
                    intent.setType("text/plain");
                    intent.setPackage("com.whatsapp");
                }

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
                if (imageViewShare != null) {
                    imageViewShare.setVisibility(GONE);
                }
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
        if (languageModelResponse != null && languageModelResponse.getData() != null) {
            fullVideoNewYoutubeViewHolder.full_video_dislike.setText(" " + languageModelResponse.getData().getDislike());
            fullVideoNewYoutubeViewHolder.full_video_share.setText(" " + languageModelResponse.getData().getShare());
            fullVideoNewYoutubeViewHolder.full_video_comment.setText(" " + languageModelResponse.getData().getComment());
        }
        bindFullVideoEngagementCounts(fullVideoNewYoutubeViewHolder.full_video_like,
                fullVideoNewYoutubeViewHolder.full_video_comment,
                fullVideoNewYoutubeViewHolder.full_video_share, position);
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
                Intent intent = new Intent(context, WriterProfileActivity.class);
                intent.putExtra("EXTRA_SESSION_ID", list.get(position).getWriterId().toString());
                if (getApplicationContext() != null && getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)) {
                    context.startActivity(intent);
                }

            }
        });
        isFullYoutubeVideoIsInitialized=false;

        if (list.get(position).getTitle() != null)

            if (list.get(position).getVideoUrl() != null && isValidContextForGlide(context)) {
                Glide.with(convertView.getContext())
                        .load(list.get(position).getThumbnail())
                        .apply(flipGlideDecodeOptions())
                        .into(newFullImageView1);
            }

        bindThreeStateIcon(fullVideoNewYoutubeViewHolder.like_for_video_image, list.get(position).getLikeStatus());
        hideDislikeView(dislike_for_video_ll);

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
                                            LinearLayout sideMenu = finalConvertViewFull.findViewById(R.id.video_side_menu);
                                            fullVideoNewYoutubeViewHolder.side_menu = sideMenu;
                                            if (sideMenu == null) {
                                                return;
                                            }
                                            if (b) {
                                                sideMenu.setVisibility(VISIBLE);
                                                sideMenu.bringToFront();
                                                sideMenu.setClickable(true);
                                            } else {
                                                sideMenu.setVisibility(View.INVISIBLE);
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
                bindFullPostLikeCount(fullVideoNewYoutubeViewHolder.full_video_like, list.get(position).getLikes(), getLikeLabel());
                bindThreeStateIcon(fullVideoNewYoutubeViewHolder.like_for_video_image, newStatus);
            }
        });

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
                showVideoShareChooser(position, VideoShareLayout.FULL_YOUTUBE, convertView);
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

        textViewSinglePage.setText("Share Article");
        textViewAllPage.setText("Download Video");
        boolean downloadable = Boolean.TRUE.equals(list.get(position).getDownloadable());
        imageViewShareAll.setImageResource(downloadable
                ? R.drawable.ic_download_all : R.drawable.ic_disabled_download_all);
        textViewAllPage.setTextColor(downloadable
                ? context.getColor(R.color.black) : Color.parseColor("#D1D1D1"));

        //imageGallaryHolder.galleryLayout.setLayerType(View.LAYER_TYPE_NONE,null);

        share_for_video_ll.setOnClickListener(view ->
                showVideoShareChooser(position, VideoShareLayout.FULL_YOUTUBE, convertView));




    }

    public void callYoutubeInit(ImageView imageViewItems, YouTubePlayerView youTubePlayerView) {
        try {
            imageViewItems.setVisibility(GONE);
            youTubePlayerView.setVisibility(VISIBLE);
        } catch (Exception e) {
            // ignore
        }
    }

    public void callYoutubeReInit(ImageView imageViewItems, LinearLayout halfyoutubecontainer) {
        try {
            imageViewItems.setVisibility(VISIBLE);
            halfyoutubecontainer.setVisibility(GONE);
        } catch (Exception e) {
            // ignore
        }
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
                if (isFullYoutubeVideoIsInitialized) {
                    youTubePlayerViewHalf.pause();
                    youTubePlayerView.setVisibility(VISIBLE);
                    newFullImageView1.setVisibility(View.GONE);
                    imageViewYoutubePlayVideo.setVisibility(View.GONE);
                } else {
                    newFullImageView1.setVisibility(VISIBLE);
                    imageViewYoutubePlayVideo.setVisibility(VISIBLE);
                }
                launchScreenshotShareIntent(intent, from);
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
        full_videoViewHolder.dislike_for_video_image = convertView.findViewById(R.id.dislike_for_video_image);
        if (languageModelResponse != null && languageModelResponse.getData() != null) {
            full_videoViewHolder.full_video_dislike.setText(" " + languageModelResponse.getData().getDislike());
            full_videoViewHolder.full_video_share.setText(" " + languageModelResponse.getData().getShare());
            full_videoViewHolder.full_video_comment.setText(" " + languageModelResponse.getData().getComment());
        }
        bindFullVideoEngagementCounts(full_videoViewHolder.full_video_like,
                full_videoViewHolder.full_video_comment,
                full_videoViewHolder.full_video_share, position);

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
        imageViewExoVideoPlayVideo.setImageResource(R.drawable.ic_video_play);

        final ImageButton fullscreen_button = convertView.findViewById(R.id.fullscreen_button);
        final ImageButton mute_button = convertView.findViewById(R.id.mute_button);
        final ProgressBar progress_bar_for_video = convertView.findViewById(R.id.progress_bar_for_video);
        if (list.get(position).getVideoUrl() != null && isValidContextForGlide(context)) {
            Glide.with(convertView.getContext())
                    .load(list.get(position).getThumbnail())
                    .apply(flipGlideDecodeOptions())
                    .into(thaumbnail_for_video);
        }
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
                showLog("EXTRA_SESSION_ID::::"+list.get(position).getWriterId().toString());
                Intent intent = new Intent(context, WriterProfileActivity.class);
                intent.putExtra("EXTRA_SESSION_ID", list.get(position).getWriterId().toString());
                if (getApplicationContext() != null && getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)) {
                    context.startActivity(intent);
                }

            }
        });
        bindThreeStateIcon(full_videoViewHolder.like_for_video_image, list.get(position).getLikeStatus());
        hideDislikeView(full_videoViewHolder.dislike_for_video_ll);
        wireFullVideoExoPauseControl(simpleExoPlayerViewFull, convertView);
        wireFullVideoExoSideMenuWithController(simpleExoPlayerViewFull, convertView);

        imageViewExoVideoPlayVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fullscreen = false;
                mute_button.setImageResource(ic__volume_up);
                imageViewExoVideoPlayVideo.setVisibility(View.GONE);
                thaumbnail_for_video.setVisibility(View.VISIBLE);
                progress_bar_for_video.setVisibility(View.VISIBLE);
                imageLoader.displayImage(list.get(position).getThumbnail(), thaumbnail_for_video, options, animationListener);
                simpleExoPlayerViewFull.setVisibility(View.VISIBLE);
                if (exoPlayer != null) {
                    exoPlayer.setPlayWhenReady(false);
                    exoPlayer.stop();
                    exoPlayer.release();
                    exoPlayer = null;
                }
                exoPlayer = new ExoPlayer.Builder(context)
                        .setSeekBackIncrementMs(5000)
                        .setSeekForwardIncrementMs(5000).build();
                simpleExoPlayerViewFull.setPlayer(exoPlayer);
                simpleExoPlayerViewFull.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
                MediaItem mediaItem = MediaItem.fromUri(Uri.parse(list.get(position).getVideoUrl()));
                exoPlayer.setMediaItem(mediaItem);
                exoPlayer.prepare();
                exoPlayer.setPlayWhenReady(true);
                wireFullVideoExoPauseControl(simpleExoPlayerViewFull, finalConvertViewFull);
                wireFullVideoExoSideMenuWithController(simpleExoPlayerViewFull, finalConvertViewFull);
                attachExoCenterIconSync(simpleExoPlayerViewFull);
                full_videoViewHolder.side_menu = finalConvertViewFull.findViewById(R.id.video_side_menu);
                // Hide engagement row while playing; tap player (Exo controller) to show again.
                if (full_videoViewHolder.side_menu != null) {
                    full_videoViewHolder.side_menu.setVisibility(View.INVISIBLE);
                }
                exoPlayer.addListener(new Player.Listener() {
                    @Override
                    public void onPlaybackStateChanged(int playbackState) {
                        boolean playWhenReady = exoPlayer.getPlayWhenReady();
                        showLog("onPlayerStateChanged::" + playWhenReady);
                        LinearLayout sideMenu = finalConvertViewFull.findViewById(R.id.video_side_menu);
                        if (playWhenReady) {
                            thaumbnail_for_video.setVisibility(View.GONE);
                            progress_bar_for_video.setVisibility(View.GONE);
                            // Keep side menu hidden while actively playing unless Exo controller is showing
                            // (controller visibility listener restores it on tap).
                        }
                        else if (playbackState == ExoPlayer.STATE_BUFFERING) {
                            showLog("onPlayerStateChanged::" + playWhenReady);
                            progress_bar_for_video.setVisibility(View.VISIBLE);

                        } else if (playbackState == ExoPlayer.STATE_IDLE) {
                            showLog("onPlayerStateChanged::ExoPlayer.STATE_IDLE" );
                            thaumbnail_for_video.setVisibility(View.VISIBLE);
                            imageViewExoVideoPlayVideo.setVisibility(VISIBLE);
                            if (sideMenu != null) {
                                sideMenu.setVisibility(View.VISIBLE);
                            }
                        } else {
                            showLog("onPlayerStateChanged::ExoPlayer.STATE_IDLE111" );
                            progress_bar_for_video.setVisibility(View.GONE);
                            thaumbnail_for_video.setVisibility(View.GONE);
                            if (sideMenu != null) {
                                sideMenu.setVisibility(View.VISIBLE);
                            }
                            showLog("fullimageadapter"+ "click visible");
                        }
                        if (playbackState == ExoPlayer.STATE_ENDED) {
                            showLog("onPlayerStateChanged::ExoPlayer.STATE_ENDED" );
                            thaumbnail_for_video.setVisibility(View.VISIBLE);
                            imageViewExoVideoPlayVideo.setVisibility(View.VISIBLE);
                            if (sideMenu != null) {
                                sideMenu.setVisibility(View.VISIBLE);
                            }
                        }
                    }

                    @Override
                    public void onIsPlayingChanged(boolean isPlaying) {
                        LinearLayout sideMenu = finalConvertViewFull.findViewById(R.id.video_side_menu);
                        if (sideMenu == null) {
                            return;
                        }
                        if (!isPlaying) {
                            sideMenu.setVisibility(View.VISIBLE);
                            sideMenu.bringToFront();
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
                bindFullPostLikeCount(full_videoViewHolder.full_video_like, list.get(position).getLikes(), getLikeLabel());
                bindThreeStateIcon(full_videoViewHolder.like_for_video_image, newStatus);
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
                showVideoShareChooser(position, VideoShareLayout.FULL_EXO, convertView);
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
        textViewSinglePage.setText("Share Article");
        textViewAllPage.setText("Download Video");
        boolean downloadable = Boolean.TRUE.equals(list.get(position).getDownloadable());
        imageViewShareAll.setImageResource(downloadable
                ? R.drawable.ic_download_all : R.drawable.ic_disabled_download_all);
        textViewAllPage.setTextColor(downloadable
                ? context.getColor(R.color.black) : Color.parseColor("#D1D1D1"));

        //imageGallaryHolder.galleryLayout.setLayerType(View.LAYER_TYPE_NONE,null);


        full_videoViewHolder.share_for_video_ll.setOnClickListener(view ->
                showVideoShareChooser(position, VideoShareLayout.FULL_EXO, convertView));


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
                launchScreenshotShareIntent(intent, from);
                Share_Count_Value("share", list.get(position).getPostId());
            }
        }.start();

    }

    private void showFullImage(int position, View convertView) {
        showLog("Check Point FULL_IMAGE:: 2");
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

        fullImageActivity.user_pic.setImageResource(R.drawable.ic_user_profile);
        fullImageActivity.varified_btn.setImageResource(R.drawable.ic_add_profile_icon);
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
                Intent intent = new Intent(context, WriterProfileActivity.class);
                intent.putExtra("EXTRA_SESSION_ID", list.get(position).getWriterId().toString());
                if (getApplicationContext() != null && getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)) {
                    context.startActivity(intent);
                }

            }
        });


        if (LanguageId == 4) {
            fullImageActivity.strip_text_name.setText("" + list.get(position).getStrip_content());
            applyStripFont(fullImageActivity.strip_text_name);

        } else if (LanguageId == 1) {
            fullImageActivity.strip_text_name.setText("" + list.get(position).getStrip_content());
            applyStripFont(fullImageActivity.strip_text_name);

        } else {
            fullImageActivity.strip_text_name.setText("" + list.get(position).getStrip_content());
            applyStripFont(fullImageActivity.strip_text_name);

        }
        fullImageActivity.full_img_like = convertView.findViewById(R.id.full_img_like_text);
        fullImageActivity.full_img_share = convertView.findViewById(R.id.full_image_share_text);
        fullImageActivity.full_img_whtsapp = convertView.findViewById(R.id.full_img_whtsapp_text);
        fullImageActivity.full_img_comment = convertView.findViewById(R.id.full_img_comment_txt);

//                        Set Text
        fullImageActivity.strip_text_name.setText("" + list.get(position).getStrip_content());
        if (languageModelResponse != null && languageModelResponse.getData() != null) {
            fullImageActivity.full_img_whtsapp.setText(" " + languageModelResponse.getData().getWhatsapp());
            showLog("sree" + languageModelResponse.getData().getWhatsapp());
        }
        bindFullPostEngagementCounts(fullImageActivity, position);


        final LinearLayout full_image_like = convertView.findViewById(R.id.full_image_like);
        fullImageActivity.full_image_whatsapp = convertView.findViewById(R.id.full_image_whatsapp);
        fullImageActivity.full_image_share = convertView.findViewById(R.id.full_image_share);
        fullImageActivity.full_image_comment = convertView.findViewById(R.id.full_image_comment);


        final ImageView full_image_selected_like = convertView.findViewById(R.id.full_image_selected_like);


        fullImageActivity.layout_strip_iconnew = convertView.findViewById(R.id.layout_strip_iconnew);
        showLog("Check Point FULL_IMAGE:: 7");
        bindThreeStateIcon(full_image_selected_like, list.get(position).getLikeStatus());
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
        final ImageView likeIconRef = full_image_selected_like;
        full_image_like.setTag(position);
        full_image_like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = (int) view.getTag();
                int newStatus = applyLikeChange(pos, likeCountViewRef);
                bindFullPostLikeCount(likeCountViewRef, list.get(pos).getLikes(), getLikeLabel());
                bindThreeStateIcon(likeIconRef, newStatus);
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

        if (myFullView == null) {
            Toast.makeText(context, "Unable to share this post right now", Toast.LENGTH_SHORT).show();
            return;
        }
        fullImageActivity.full_image_share = myFullView.findViewById(R.id.full_image_share);
        fullImageActivity.layout_strip_iconnew = myFullView.findViewById(R.id.layout_strip_iconnew);
        fullImageActivity.full_image_view_details = myFullView.findViewById(R.id.full_image_view_details);
        if (fullImageActivity.full_image_view_details == null) {
            Toast.makeText(context, "Unable to share this post right now", Toast.LENGTH_SHORT).show();
            return;
        }

        if (fullImageActivity.layout_strip_iconnew != null) {
            fullImageActivity.layout_strip_iconnew.setVisibility(View.GONE);
        }


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

    public void shareSurveyScreenshot(View flipChild, int position) {
        if (flipChild == null) {
            Toast.makeText(context, "Unable to share this post right now", Toast.LENGTH_SHORT).show();
            return;
        }
        View shareTarget = flipChild.findViewById(R.id.rl_survay_main);
        if (shareTarget == null) {
            shareTarget = flipChild.findViewById(R.id.card_view_survey);
        }
        if (shareTarget == null) {
            Toast.makeText(context, "Unable to share this post right now", Toast.LENGTH_SHORT).show();
            return;
        }
        if (list == null || position < 0 || position >= list.size()) {
            return;
        }
        final View screenshotView = shareTarget;
        String mInvitationUrl = list.get(position).getShareLink();
        String msg = list.get(position).getTitle() + " " + mInvitationUrl;
        countDownTimer = new CountDownTimer(2, 2) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.setPackage("com.whatsapp");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(Intent.EXTRA_SUBJECT, mInvitationUrl);
                intent.putExtra(Intent.EXTRA_TEXT, msg);
                Bitmap b = ScreenShott.getInstance().takeScreenShotOfView(screenshotView);
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
                Share_Count_Value("share", list.get(position).getPostId());
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
        new_half_image.getLayoutParams().height = getVideoLayoutTopHeight();
        layoutImageHeight.getLayoutParams().height = getVideoLayoutTopHeight();
        halfImageActivity.ll_refer_and_earn_half = convertView.findViewById(R.id.ll_refer_and_earn_half);
        halfImageActivity.layoutParentDate = convertView.findViewById(R.id.layoutParentDate);
        TextView textViewPostDate = convertView.findViewById(R.id.textViewPostDate);
        halfImageActivity.ll_footer_layout = convertView.findViewById(R.id.ll_footer_layout);
        showLog("ll_footer_layout:::"+halfImageActivity.ll_footer_layout.getVisibility());
        halfImageActivity.ll_footer_layout.setVisibility(VISIBLE);
        halfImageActivity.parent_layout = convertView.findViewById(R.id.parent_layout);
        halfImageActivity.writer_profile_pic.setImageResource(R.drawable.ic_user_profile);
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
        halfImageActivity.half_comment_icon.setImageResource(R.drawable.ic_action_comment);
        halfImageActivity.half_share_icon.setImageResource(R.drawable.ic_action_share);
        halfImageActivity.report_icon.setImageResource(R.drawable.ic_action_report);
        halfImageActivity.with_strip_layout.setBackgroundResource(R.drawable.ic_half_image_strike_icon);
        TextView textViewWebUrl=convertView.findViewById(R.id.textViewWebUrl);
        TextView textViewStripWebUrl=convertView.findViewById(R.id.textViewStripWebUrl);
        showLog("webURl:"+list.get(position).getWeb_url());
        if(list.get(position).getWeb_url()!=null && !list.get(position).getWeb_url().equalsIgnoreCase("")){
            textViewWebUrl.setText(list.get(position).getWeb_url().replace("https://",""));
            textViewStripWebUrl.setText(list.get(position).getWeb_url().replace("https://",""));

        } else {
            textViewWebUrl.setText("");
            textViewStripWebUrl.setText("");
        }
        textViewWebUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(list.get(position).getWeb_url()==null) return;
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(list.get(position).getWeb_url()));
                context.startActivity(browserIntent);
            }
        });

        bindHalfPostEngagementCounts(convertView, position);

        if (categery_id == 0) {
            halfImageActivity.my_feed.setText(my_feed_name);
            halfImageActivity.iv_share_and_earn.setBackgroundResource(ic_location_icon);

        } else {
            halfImageActivity.my_feed.setText(my_feed_name);
            halfImageActivity.iv_share_and_earn.setBackgroundResource(ic_news_icon);
        }
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

            halfImageActivity.tamil_half_content_description.setText("" + list.get(position).getDescription());
            halfImageActivity.tamil_half_content_title.setText("" + list.get(position).getTitle());
            halfImageActivity.strip_text_name.setText("" + list.get(position).getStrip_content());
            showLog("Content Description TAMIL: "+halfImageActivity.tamil_half_content_description.getText().toString());
            applyStripFont(halfImageActivity.strip_text_name);
            FontSettingsHelper.applyArticleTypography(
                    context,
                    4,
                    halfImageActivity.tamil_half_content_title,
                    halfImageActivity.tamil_half_content_description,
                    "half_image_tamil"
            );

        } else if (LanguageId == 1) {
            halfImageActivity.tamil_half_content_description.setVisibility(View.GONE);
            halfImageActivity.half_content_title.setVisibility(View.GONE);
            halfImageActivity.half_content_description.setVisibility(View.GONE);
            halfImageActivity.half_content_title.setVisibility(View.GONE);

            halfImageActivity.english_half_content_description.setVisibility(View.VISIBLE);
            halfImageActivity.english_half_content_title.setVisibility(View.VISIBLE);

            halfImageActivity.english_half_content_description.setText("" + list.get(position).getDescription());
            halfImageActivity.english_half_content_title.setText("" + list.get(position).getTitle());
            halfImageActivity.strip_text_name.setText("" + list.get(position).getStrip_content());
            showLog("Content Description ENG : "+halfImageActivity.english_half_content_description.getText().toString());
            applyStripFont(halfImageActivity.strip_text_name);
            FontSettingsHelper.applyArticleTypography(
                    context,
                    1,
                    halfImageActivity.english_half_content_title,
                    halfImageActivity.english_half_content_description,
                    "half_image_english"
            );
        } else {

            halfImageActivity.english_half_content_description.setVisibility(View.GONE);
            halfImageActivity.english_half_content_title.setVisibility(View.GONE);
            halfImageActivity.tamil_half_content_description.setVisibility(View.GONE);
            halfImageActivity.half_content_title.setVisibility(View.GONE);
            halfImageActivity.half_content_description.setVisibility(View.VISIBLE);
            halfImageActivity.half_content_title.setVisibility(View.VISIBLE);
            halfImageActivity.half_content_description.setText("" +list.get(position).getDescription());
            halfImageActivity.half_content_title.setText("" + list.get(position).getTitle());
            halfImageActivity.strip_text_name.setText("" + list.get(position).getStrip_content());
            showLog("Content Description HALF: "+halfImageActivity.half_content_description.getText().toString());
            applyStripFont(halfImageActivity.strip_text_name);
            FontSettingsHelper.applyArticleTypography(
                    context,
                    LanguageId,
                    halfImageActivity.half_content_title,
                    halfImageActivity.half_content_description,
                    "half_image_default"
            );

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
        half_image_like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int currPosition = (int) view.getTag();
                int newStatus = applyLikeChange(currPosition, likeCountViewRef);
                bindHalfLikeIcon(likeIconRef, newStatus);
            }
        });
        showLog("Check Point Adapter:: 6");
        halfImageActivity.writer_name.setText("" + list.get(position).getWriterName());

        halfImageActivity.writer_designation.setText("" + list.get(position).getRole());

        halfImageActivity.writer_time.setText("" + commonMethods.getHrsFormatfromDate(list.get(position).getPublishedTime()));

        WriterStripStyleHelper.apply(
                context,
                halfImageActivity.writer_name,
                halfImageActivity.writer_designation,
                halfImageActivity.writer_time,
                halfImageActivity.my_feed,
                halfImageActivity.ll_refer_and_earn_half
        );

        if (list.get(position).getIsFollow() == 0) {

            halfImageActivity.writer_follower.setVisibility(View.GONE);
            halfImageActivity.writer_follow.setVisibility(View.VISIBLE);

            test_folloew.setText("Follow");

        } else {

            halfImageActivity.writer_follower.setVisibility(View.VISIBLE);
            halfImageActivity.writer_follow.setVisibility(View.GONE);

            test_folloew.setText("Following");

        }
        showLog("Check Point Adapter:: 7");
        halfImageActivity.ll_refer_and_earn_half.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                showLog("halfImageActivity ll_refer_and_earn_half");
//                Intent intent = new Intent("CURRENT_LOCATION");
//                intent.putExtra("message", "click_event");
//                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                callbacks.callLocationRequest(0);



            }
        });
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
                        if (updateResponce == null || updateResponce.getData() == null
                                || updateResponce.getData().getOptions() == null
                                || updateResponce.getData().getOptions().size() < 4) {
                            return;
                        }

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
                    ds.setColor(context.getResources().getColor(R.color.colorPrimary));
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

                if (list.get(position).getIsFollow() == 0) {

                    halfImageActivity.writer_follow.setVisibility(View.GONE);
                    halfImageActivity.writer_follower.setVisibility(View.VISIBLE);
                    test_folloew.setText("Following");

                    set_follow_un_follow(list.get(position).getWriterId(), 1);


                } else {

                    halfImageActivity.writer_follow.setVisibility(View.VISIBLE);
                    halfImageActivity.writer_follower.setVisibility(View.GONE);
                    test_folloew.setText("Follow");

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

                if (list.get(position).getIsFollow() == 0) {

                    halfImageActivity.writer_follow.setVisibility(View.GONE);
                    halfImageActivity.writer_follower.setVisibility(View.VISIBLE);
                    test_folloew.setText("Following");

                    set_follow_un_follow(list.get(position).getWriterId(), 1);


                } else {

                    halfImageActivity.writer_follow.setVisibility(View.VISIBLE);
                    halfImageActivity.writer_follower.setVisibility(View.GONE);
                    test_folloew.setText("Follow");

                    set_follow_un_follow(list.get(position).getWriterId(), 0);


                }

            }
        });

        halfImageActivity.writer_profile_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), WriterProfileActivity.class);
                intent.putExtra("EXTRA_SESSION_ID", list.get(position).getWriterId().toString());
                if (context != null && context.getPackageName().equals(BuildConfig.APPLICATION_ID)) {
                    context.startActivity(intent);
                }

            }
        });

        if(list.get(position).isIs_gallery()){
            List<String> halfGalleryImages = list.get(position).getImages();
            if (halfGalleryImages != null && !halfGalleryImages.isEmpty()) {
                halfImageActivity.galleryImageLayout.setVisibility(VISIBLE);
                halfImageActivity.new_half_image.setVisibility(GONE);
                new_half_image.setVisibility(GONE);
                WormDotsIndicator indicator = convertView.findViewById(R.id.indicator);

                // half gallery images
                halfImageGallery = new ArrayList<>();
                ArrayList<String> listSample = new ArrayList<>(halfGalleryImages);
                halfImageGallery.addAll(listSample);
                halfImageActivity.viewPager.setAdapter(new SamplePagerAdapter(context, halfImageGallery, layoutHeight, true));

                // added to increase speed in view pager
                halfImageActivity.viewPager.setOffscreenPageLimit(Math.max(1, Math.min(2, listSample.size() - 1)));
                if (indicator != null) {
                    indicator.setViewPager(halfImageActivity.viewPager);
                }
                halfImageActivity.ll_footer_layout.setVisibility(VISIBLE);
            } else {
                halfImageActivity.galleryImageLayout.setVisibility(GONE);
                halfImageActivity.new_half_image.setVisibility(VISIBLE);
                new_half_image.setVisibility(VISIBLE);
            }

        }else{
            halfImageActivity.galleryImageLayout.setVisibility(GONE);
            halfImageActivity.new_half_image.setVisibility(VISIBLE);
            new_half_image.setVisibility(VISIBLE);
        }

    }

    public void takeNormalImageScreenshot(View finalConvertView1, int customPosition,int from) {
        {
            showLog("ll_footer_layout::: takeNormalImageScreenshot");
            if (finalConvertView1 == null) {
                Toast.makeText(context, "Unable to share this post right now", Toast.LENGTH_SHORT).show();
                return;
            }
            halfImageActivity.parent_layout = finalConvertView1.findViewById(R.id.parent_layout);
            halfImageActivity.relative_half_content = finalConvertView1.findViewById(R.id.relative_half_contents);
            halfImageActivity.with_strip_layout = finalConvertView1.findViewById(R.id.with_strips_layout);
            halfImageActivity.with_out_strip_layout = finalConvertView1.findViewById(R.id.with_out_strips_layout);
            halfImageActivity.layoutParentDate = finalConvertView1.findViewById(R.id.layoutParentDate);
            halfImageActivity.ll_footer_layout = finalConvertView1.findViewById(R.id.ll_footer_layout);
            TextView textViewWebUrl = finalConvertView1.findViewById(R.id.textViewWebUrl);
            LinearLayout indicator = finalConvertView1.findViewById(R.id.dotIndicatior);
            if (halfImageActivity.relative_half_content == null
                    || halfImageActivity.with_strip_layout == null
                    || halfImageActivity.with_out_strip_layout == null
                    || halfImageActivity.ll_footer_layout == null
                    || halfImageActivity.layoutParentDate == null) {
                Toast.makeText(context, "Unable to share this post right now", Toast.LENGTH_SHORT).show();
                return;
            }
            if (indicator != null) {
                indicator.setVisibility(GONE);
            }
            halfImageActivity.with_out_strip_layout.setVisibility(View.GONE);
            halfImageActivity.with_strip_layout.setVisibility(View.VISIBLE);
            halfImageActivity.ll_footer_layout.setVisibility(GONE);
            if (list != null && customPosition >= 0 && customPosition < list.size()
                    && list.get(customPosition).isIs_gallery() && indicator != null) {
                indicator.setVisibility(GONE);
            }
            if (textViewWebUrl != null) {
                textViewWebUrl.setVisibility(GONE);
            }
            halfImageActivity.layoutParentDate.setVisibility(VISIBLE);
            if (list == null || customPosition < 0 || customPosition >= list.size()) {
                return;
            }
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
                    if (textViewWebUrl != null) {
                        textViewWebUrl.setVisibility(VISIBLE);
                    }
                    if (list != null && customPosition >= 0 && customPosition < list.size()
                            && list.get(customPosition).isIs_gallery() && indicator != null) {
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

        halfVideoExoPlayerHolder.layoutParentTop.getLayoutParams().height = getVideoLayoutTopHeight();


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
        bindHalfPostEngagementCounts(convertView, position);

        bindHalfLikeIcon(halfVideoExoPlayerHolder.half_image_like, list.get(position).getLikeStatus());

        halfVideoExoPlayerHolder.half_comment_icon = convertView.findViewById(R.id.half_comment_icon);
        halfVideoExoPlayerHolder.share = convertView.findViewById(R.id.share);
        halfVideoExoPlayerHolder.video_report = convertView.findViewById(R.id.video_report);
        halfVideoExoPlayerHolder.half_comment_icon.setImageResource(R.drawable.ic_action_comment);
        halfVideoExoPlayerHolder.share.setImageResource(R.drawable.ic_action_share);
        halfVideoExoPlayerHolder.video_report.setImageResource(R.drawable.ic_action_report);
        tintHalfActionIcons(convertView);


        halfVideoExoPlayerHolder.writer_follow = convertView.findViewById(R.id.writer_follow);
        halfVideoExoPlayerHolder.writer_follower = convertView.findViewById(R.id.writer_follower);
        final TextView test_folloew = convertView.findViewById(R.id.test_folloew);
        halfVideoExoPlayerHolder.ll_refer_and_earn_halfy = convertView.findViewById(R.id.ll_refer_and_earn_half);
        halfVideoExoPlayerHolder.iv_share_and_earn = convertView.findViewById(R.id.iv_share_and_earn);
        halfVideoExoPlayerHolder.my_feed = convertView.findViewById(R.id.my_feed);
        halfVideoExoPlayerHolder.parent_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (layoutParentReportDetails.isShown() || layoutParentReportDetails.getVisibility() == View.VISIBLE) {

                    layoutParentReportDetails.setVisibility(View.GONE);
                    Logone.e("fullimageadapter", "click gone");
                    //  Toast.makeText(HomePage.this, "Gone", Toast.LENGTH_SHORT).show();
                } else {
                    layoutParentReportDetails.setVisibility(View.VISIBLE);
                    Logone.e("fullimageadapter", "click visible");
                    //  Toast.makeText(HomePage.this, "Visible", Toast.LENGTH_SHORT).show();
                }
            }
        });
        TextView textViewWebUrl=convertView.findViewById(R.id.textViewWebUrl);
        TextView textViewStripWebUrl=convertView.findViewById(R.id.textViewStripWebUrl);
        if(list.get(position).getWeb_url()!=null && !list.get(position).getWeb_url().equalsIgnoreCase("")){
            textViewWebUrl.setText(list.get(position).getWeb_url().replace("https://",""));
            textViewStripWebUrl.setText(list.get(position).getWeb_url().replace("https://",""));
        } else {
            textViewWebUrl.setText("");
            textViewStripWebUrl.setText("");
        }
        textViewWebUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(list.get(position).getWeb_url()==null) return;
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(list.get(position).getWeb_url()));
                context.startActivity(browserIntent);
            }
        });
        if (categery_id == 0) {
            halfVideoExoPlayerHolder.my_feed.setText(my_feed_name);
            halfVideoExoPlayerHolder.iv_share_and_earn.setBackgroundResource(ic_location_icon);

        } else {
            halfVideoExoPlayerHolder.my_feed.setText(my_feed_name);
            halfVideoExoPlayerHolder.iv_share_and_earn.setBackgroundResource(ic_news_icon);

        }
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
            applyStripFont(halfVideoExoPlayerHolder.strip_text_name);
            FontSettingsHelper.applyArticleTypography(context, 4, tamil_half_content_title, tamil_half_content_description, "half_video_tamil");

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
            applyStripFont(halfVideoExoPlayerHolder.strip_text_name);
            FontSettingsHelper.applyArticleTypography(context, 1, english_half_content_title, english_half_content_description, "half_video_english");

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
            applyStripFont(halfVideoExoPlayerHolder.strip_text_name);
            FontSettingsHelper.applyArticleTypography(context, LanguageId, half_content_title, half_content_description, "half_video_default");
        }
        halfVideoExoPlayerHolder.writer_name.setText("" + list.get(position).getWriterName());
        halfVideoExoPlayerHolder.writer_designation.setText("" + list.get(position).getRole());
        halfVideoExoPlayerHolder.writer_time.setText("" + commonMethods.getHrsFormatfromDate(list.get(position).getPublishedTime()));
        WriterStripStyleHelper.apply(
                context,
                halfVideoExoPlayerHolder.writer_name,
                halfVideoExoPlayerHolder.writer_designation,
                halfVideoExoPlayerHolder.writer_time,
                halfVideoExoPlayerHolder.my_feed,
                halfVideoExoPlayerHolder.ll_refer_and_earn_halfy
        );
        int width = displayMetrics.widthPixels;
        //halfVideoExoPlayerHolder.imageViewItems.setVisibility(View.VISIBLE);
        //halfVideoExoPlayerHolder.youTubePlayerView.setVisibility(View.GONE);



        if (list.get(position).getTitle() != null){
            if (list.get(position).getVideoUrl() != null && isValidContextForGlide(context)) {
                imageLoader.displayImage(list.get(position).getThumbnail(), thaumbnail_for_video, options, animationListener);
            }
        }

//        if (exoPlayer != null) {
//            exoPlayer.setPlayWhenReady(false);
//            exoPlayer.stop();
//            exoPlayer.seekTo(0);
//            //youTubePlayerViewHalf.cueVideo(videoURL,0);
//        }
//        exoPlayer=null;
        if(simpleExoPlayerView.getPlayer()!=null){
            simpleExoPlayerView.getPlayer().release();
            simpleExoPlayerView.setPlayer(null);
        }
        simpleExoPlayerView.setVisibility(View.GONE);
        if("4".equalsIgnoreCase(list.get(position).getMediaType())){
            //Youtube

        }else{
            //uploaded video
            imageViewExoVideoPlayVideo.setVisibility(View.VISIBLE);
        }

        //....................................................Upload video..............................
        layoutParentTitle.setVisibility(View.VISIBLE);
        layoutParentReportDetails.setVisibility(View.VISIBLE);
        layoutParentMiddle.setVisibility(View.VISIBLE);

        layoutParentTop.getLayoutParams().height = getVideoLayoutTopHeight();
        simpleExoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);

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
        progress_bar_for_video.setVisibility(View.GONE);
        imageViewExoVideoPlayVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fullscreen = false;
                textViewWebUrl.setVisibility(View.GONE);
                mute_button.setImageResource(ic__volume_up);
                imageViewExoVideoPlayVideo.setVisibility(View.GONE);
                thaumbnail_for_video.setVisibility(View.VISIBLE);
                progress_bar_for_video.setVisibility(View.VISIBLE);
                imageLoader.displayImage(list.get(position).getThumbnail(), thaumbnail_for_video, options, animationListener);
                simpleExoPlayerView.setVisibility(View.VISIBLE);
                if (exoPlayer != null) {
                    exoPlayer.setPlayWhenReady(false);
                    exoPlayer.stop();
                    exoPlayer.release();
                    exoPlayer = null;
                }
                exoPlayer = new ExoPlayer.Builder(context)
                        .setSeekBackIncrementMs(5000)
                        .setSeekForwardIncrementMs(5000).build();
                simpleExoPlayerView.setPlayer(exoPlayer);
                simpleExoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
                showLog("Video URL:"+list.get(position).getVideoUrl());
                MediaItem mediaItem = MediaItem.fromUri(Uri.parse(list.get(position).getVideoUrl()));
                exoPlayer.setMediaItem(mediaItem);
                exoPlayer.prepare();
                exoPlayer.setPlayWhenReady(true);
                // Media3 does not manage the legacy exo_play/exo_pause buttons in the custom
                // controller, so wire them here to make tapping toggle play/pause and to keep
                // exactly one center icon visible per player state.
                wireHalfVideoExoPauseControl(simpleExoPlayerView);
                attachExoCenterIconSync(simpleExoPlayerView);
                exoPlayer.addListener(new Player.Listener() {
                    @Override
                    public void onPlaybackStateChanged(int playbackState) {
                        boolean playWhenReady = exoPlayer.getPlayWhenReady();
                        showLog("onPlayerStateChanged::" + playWhenReady);
                        if (playWhenReady) {
                            thaumbnail_for_video.setVisibility(View.GONE);
                            progress_bar_for_video.setVisibility(View.GONE);
                        }
                        else if (playbackState == ExoPlayer.STATE_BUFFERING) {
                            showLog("STATE_BUFFERING::" + playWhenReady);
//                            thaumbnail_for_video.setVisibility(View.VISIBLE);
                            progress_bar_for_video.setVisibility(View.VISIBLE);

                        } else if (playbackState == ExoPlayer.STATE_IDLE) {

                            thaumbnail_for_video.setVisibility(View.VISIBLE);
                            imageViewExoVideoPlayVideo.setVisibility(VISIBLE);
                            //progress_bar_for_video.setVisibility(View.VISIBLE);


                        } else {
                            progress_bar_for_video.setVisibility(View.GONE);
                            thaumbnail_for_video.setVisibility(View.GONE);
                        }
                        if (playbackState == ExoPlayer.STATE_ENDED) {
                            //  exoPlayer.prepare(mediaSource);
                            // exoPlayer.setPlayWhenReady(true);
                            thaumbnail_for_video.setVisibility(View.VISIBLE);
                            imageViewExoVideoPlayVideo.setVisibility(View.VISIBLE);
                        }
                        updateExoCenterControlIcons(simpleExoPlayerView);
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

        if (list.get(position).getIsFollow() == 0) {
            halfVideoExoPlayerHolder.writer_follower.setVisibility(View.GONE);
            halfVideoExoPlayerHolder.writer_follow.setVisibility(View.VISIBLE);
            test_folloew.setText("Follow");
        } else {
            halfVideoExoPlayerHolder.writer_follower.setVisibility(View.VISIBLE);
            halfVideoExoPlayerHolder.writer_follow.setVisibility(View.GONE);
            test_folloew.setText("Following");
        }

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


        halfVideoExoPlayerHolder.ll_refer_and_earn_halfy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//
//
//                Intent intent = new Intent("CURRENT_LOCATION");
//                intent.putExtra("message", "click_event");
//
//                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

                callbacks.callLocationRequest(0);
            }
        });

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
                Intent intent = new Intent(getApplicationContext(), WriterProfileActivity.class);
                intent.putExtra("EXTRA_SESSION_ID", list.get(position).getWriterId().toString());
                if (context != null && context.getPackageName().equals(BuildConfig.APPLICATION_ID)) {
                    context.startActivity(intent);
                }
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
                        if (updateResponce == null || updateResponce.getData() == null
                                || updateResponce.getData().getOptions() == null
                                || updateResponce.getData().getOptions().size() < 4) {
                            return;
                        }

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
        halfVideoExoPlayerHolder.half_image_like_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                halfVideoExoPlayerHolder.half_image_like = finalConvertView3.findViewById(R.id.half_image_like);
                int newStatus = applyLikeChange(position, null);
                bindHalfActionCount(finalConvertView3.findViewById(R.id.half_like_count), list.get(position).getLikes());
                bindHalfLikeIcon(halfVideoExoPlayerHolder.half_image_like, newStatus);
            }
        });

        halfVideoExoPlayerHolder.writer_follower.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent("WRITER_FOLLOWER");
                intent.putExtra("message", "click_event");

                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

                if (list.get(position).getIsFollow() == 0) {

                    halfVideoExoPlayerHolder.writer_follow.setVisibility(View.GONE);
                    halfVideoExoPlayerHolder.writer_follower.setVisibility(View.VISIBLE);
                    test_folloew.setText("Following");

                    set_follow_un_follow(list.get(position).getWriterId(), 1);


                } else {

                    halfVideoExoPlayerHolder.writer_follow.setVisibility(View.VISIBLE);
                    halfVideoExoPlayerHolder.writer_follower.setVisibility(View.GONE);

                    test_folloew.setText("Follow");

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

                if (list.get(position).getIsFollow() == 0) {

                    halfVideoExoPlayerHolder.writer_follow.setVisibility(View.GONE);
                    halfVideoExoPlayerHolder.writer_follower.setVisibility(View.VISIBLE);
                    test_folloew.setText("Following");

                    set_follow_un_follow(list.get(position).getWriterId(), 1);

                } else {

                    halfVideoExoPlayerHolder.writer_follow.setVisibility(View.VISIBLE);
                    halfVideoExoPlayerHolder.writer_follower.setVisibility(View.GONE);
                    test_folloew.setText("Follow");

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
        boolean downloadable = Boolean.TRUE.equals(list.get(position).getDownloadable());
        imageViewShareAll.setImageResource(downloadable
                ? R.drawable.ic_download_all : R.drawable.ic_disabled_download_all);
        textViewAllPage.setTextColor(downloadable
                ? context.getColor(R.color.black) : Color.parseColor("#D1D1D1"));

        relativeLayoutShareOpt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLog("Hello");
            }
        });

        textViewSinglePage.setText("Share Article");
        textViewAllPage.setText("Download Video");

        //imageGallaryHolder.galleryLayout.setLayerType(View.LAYER_TYPE_NONE,null);

        halfVideoExoPlayerHolder.half_image_share.setOnClickListener(view ->
                showVideoShareChooser(position, VideoShareLayout.HALF_EXO, convertView));


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
        /* Writer Detailes  */
        half_youTubePlayerHolder.writer_time = convertView.findViewById(R.id.writer_time);
        half_youTubePlayerHolder.writer_name = convertView.findViewById(R.id.writer_name);
        half_youTubePlayerHolder.writer_profile_pic = convertView.findViewById(R.id.writer_profile_pic);
        half_youTubePlayerHolder.btnMuteHalf = convertView.findViewById(R.id.btnMute);
        layoutParentTitle = convertView.findViewById(R.id.layoutParentTitle);
        final LinearLayout  layoutParentReportDetails = convertView.findViewById(R.id.ll_footer_layout);
        final LinearLayout parent_layout = convertView.findViewById(R.id.parent_layout);
        final LinearLayout halfyoutubecontainer = convertView.findViewById(R.id.halfyoutubecontainer);
        layoutParentMiddle = convertView.findViewById(R.id.layoutParentMiddle);
        half_youTubePlayerHolder.strip_text_name = convertView.findViewById(R.id.strip_text_name);
        layoutParentTop = convertView.findViewById(R.id.layoutParentTop);
        half_youTubePlayerHolder.layoutParentTop = convertView.findViewById(R.id.layoutParentTop);
        ImageView imageViewYoutubePlayVideo = convertView.findViewById(R.id.imageViewPlayVideo);
        half_youTubePlayerHolder.layoutParentTop.getLayoutParams().height = getVideoLayoutTopHeight();
        imageViewItems.setVisibility(VISIBLE);
        halfyoutubecontainer.setVisibility(GONE);

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


        half_youTubePlayerHolder.writer_follow = convertView.findViewById(R.id.writer_follow);
        half_youTubePlayerHolder.writer_follower = convertView.findViewById(R.id.writer_follower);
        final TextView test_folloew = convertView.findViewById(R.id.test_folloew);
        half_youTubePlayerHolder.ll_refer_and_earn_halfy = convertView.findViewById(R.id.ll_refer_and_earn_half);
        half_youTubePlayerHolder.iv_share_and_earn = convertView.findViewById(R.id.iv_share_and_earn);
        half_youTubePlayerHolder.my_feed = convertView.findViewById(R.id.my_feed);
        layoutParentReportDetails.setVisibility(View.VISIBLE);
        parent_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                  // layoutParentReportDetails=convertView.findViewById(R.id.layoutParentReportDetails);
                if (layoutParentReportDetails.isShown() || layoutParentReportDetails.getVisibility() == View.VISIBLE) {
                    layoutParentReportDetails.setVisibility(View.GONE);
                    showLog("showNormalVideo"+ "click gone");
                    //  Toast.makeText(HomePage.this, "Gone", Toast.LENGTH_SHORT).show();
                } else {
                    layoutParentReportDetails.setVisibility(View.VISIBLE);
                    showLog("showNormalVideo"+ "click visible");
                    //  Toast.makeText(HomePage.this, "Visible", Toast.LENGTH_SHORT).show();
                }
            }
        });
        showLog("showNormalVideo"+ "after thubnails");
        TextView textViewWebUrl=convertView.findViewById(R.id.textViewWebUrl);
        TextView textViewStripWebUrl=convertView.findViewById(R.id.textViewStripWebUrl);
        if(list.get(position).getWeb_url()!=null && !list.get(position).getWeb_url().equalsIgnoreCase("")){
            textViewWebUrl.setText(list.get(position).getWeb_url().replace("https://",""));
            textViewStripWebUrl.setText(list.get(position).getWeb_url().replace("https://",""));
        } else {
            textViewWebUrl.setText("");
            textViewStripWebUrl.setText("");
        }
        textViewWebUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(list.get(position).getWeb_url()==null) return;
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(list.get(position).getWeb_url()));
                context.startActivity(browserIntent);
            }
        });
        if (categery_id == 0) {
            half_youTubePlayerHolder.my_feed.setText(my_feed_name);
            half_youTubePlayerHolder.iv_share_and_earn.setBackgroundResource(ic_location_icon);

        } else {
            half_youTubePlayerHolder.my_feed.setText(my_feed_name);
            half_youTubePlayerHolder.iv_share_and_earn.setBackgroundResource(ic_news_icon);

        }
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
            applyStripFont(half_youTubePlayerHolder.strip_text_name);
            FontSettingsHelper.applyArticleTypography(context, 4, tamil_half_content_title, tamil_half_content_description, "half_video_tamil");

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
            applyStripFont(half_youTubePlayerHolder.strip_text_name);
            FontSettingsHelper.applyArticleTypography(context, 1, english_half_content_title, english_half_content_description, "half_video_english");

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
            applyStripFont(half_youTubePlayerHolder.strip_text_name);
            FontSettingsHelper.applyArticleTypography(context, LanguageId, half_content_title, half_content_description, "half_video_default");
        }
        half_youTubePlayerHolder.writer_name.setText("" + list.get(position).getWriterName());
        half_youTubePlayerHolder.writer_designation.setText("" + list.get(position).getRole());
        half_youTubePlayerHolder.writer_time.setText("" + commonMethods.getHrsFormatfromDate(list.get(position).getPublishedTime()));
        WriterStripStyleHelper.apply(
                context,
                half_youTubePlayerHolder.writer_name,
                half_youTubePlayerHolder.writer_designation,
                half_youTubePlayerHolder.writer_time,
                half_youTubePlayerHolder.my_feed,
                half_youTubePlayerHolder.ll_refer_and_earn_halfy
        );
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

        if (list.get(position).getTitle() != null){
            if (list.get(position).getVideoUrl() != null && isValidContextForGlide(context)) {
                 imageLoader.displayImage(list.get(position).getThumbnail(), imageViewItems, options, animationListener);
            }
        }

        isYoutubeVideoIsInitialized=false;
        imageViewYoutubePlayVideo.setVisibility(View.VISIBLE);

        //....................................................Upload video..............................
        layoutParentTitle.setVisibility(View.VISIBLE);
        layoutParentReportDetails.setVisibility(View.VISIBLE);
        layoutParentMiddle.setVisibility(View.VISIBLE);

        layoutParentTop.getLayoutParams().height = getVideoLayoutTopHeight();


      //  progress_bar_for_video.setVisibility(View.GONE);
        if(youTubePlayerViewHalf!=null){
            youTubePlayerViewHalf.pause();
            //youTubePlayerViewHalf=null;
        }
        ProgressBar youtubeprogressbar = convertView.findViewById(R.id.youtubeprogressbar);
        showLog("showNormalVideo"+ "after thubnails 3");
        imageViewYoutubePlayVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLog("youTubePlayerView::onClick " );
                if (true) {
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            YouTubePlayerView youTubePlayerView = new YouTubePlayerView(context);
                            youTubePlayerView.setEnableAutomaticInitialization(false);
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                            youTubePlayerView.setLayoutParams(params);

                            halfyoutubecontainer.setVisibility(VISIBLE);
                            lifecycle.removeObserver(youTubePlayerView);
                            halfyoutubecontainer.removeAllViews();
                            halfyoutubecontainer.addView(youTubePlayerView);

                            showLog("Data::"+isYoutubeVideoIsInitialized);
                            youtubeprogressbar.setVisibility(VISIBLE);
                            textViewWebUrl.setVisibility(View.GONE);
                            imageViewItems.setVisibility(View.GONE);
                            youTubePlayerView.setVisibility(View.VISIBLE);
                            View v = youTubePlayerView.inflateCustomPlayerUi(R.layout.youtube_custom_player_ui);
                            listener = new AbstractYouTubePlayerListener() {
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
                                                layoutParentTitle.setVisibility(GONE);
                                                layoutParentReportDetails.setVisibility(GONE);
                                                layoutParentMiddle.setVisibility(GONE);

                                            } else {
                                                layoutParentTitle.setVisibility(View.VISIBLE);
                                                layoutParentReportDetails.setVisibility(View.VISIBLE);
                                                layoutParentMiddle.setVisibility(View.VISIBLE);
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
                                            youtubeprogressbar.setVisibility(GONE);
                                            youtubeprogressbar.bringToFront();
                                            showLog("Progressbar shows on Main Page:"+b);

                                        }
                                    });
                                    youTubePlayer.addListener(customPlayerUiController);
                                    youTubePlayerViewHalf = youTubePlayer;
                                    YouTubePlayerUtils.loadOrCueVideo(
                                            youTubePlayer, lifecycle,
                                            list.get(position).getVideoUrl(), 0f
                                    );
                                    isYoutubeVideoIsInitialized = true;

                                }
                            };
                            youTubePlayerView.getYouTubePlayerWhenReady(new YouTubePlayerCallback() {
                                @Override
                                public void onYouTubePlayer(@NonNull com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer youTubePlayer) {
                                    showLog("getYouTubePlayerWhenReady::");
                                }
                            });

                            IFramePlayerOptions options = new IFramePlayerOptions.Builder(context).controls(0).build();
                            youTubePlayerView.initialize(listener, false, options);

                        }
                    }, 100);
                }

            }
        });
        //....................................................DONE........................................

        if (list.get(position).getIsFollow() == 0) {
            half_youTubePlayerHolder.writer_follower.setVisibility(View.GONE);
            half_youTubePlayerHolder.writer_follow.setVisibility(View.VISIBLE);
            test_folloew.setText("Follow");
        } else {
            half_youTubePlayerHolder.writer_follower.setVisibility(View.VISIBLE);
            half_youTubePlayerHolder.writer_follow.setVisibility(View.GONE);
            test_folloew.setText("Following");
        }

        bindHalfLikeIcon(half_youTubePlayerHolder.half_image_like, list.get(position).getLikeStatus());

        // check null
        if (half_youTubePlayerHolder.strip_text_name.getText().toString().matches("null")) {
            half_youTubePlayerHolder.strip_text_name.setText("" + " The Best Regional News App ");
        }
        half_youTubePlayerHolder.ll_refer_and_earn_halfy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


//                Intent intent = new Intent("CURRENT_LOCATION");
//                intent.putExtra("message", "click_event");
//
//                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                callbacks.callLocationRequest(0);
            }
        });

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
        if (half_youTubePlayerHolder.btnMuteHalf != null) {
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
        }


        half_youTubePlayerHolder.writer_profile_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), WriterProfileActivity.class);
                intent.putExtra("EXTRA_SESSION_ID", list.get(position).getWriterId().toString());
                if (context != null && context.getPackageName().equals(BuildConfig.APPLICATION_ID)) {
                    context.startActivity(intent);
                }
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
                        if (updateResponce == null || updateResponce.getData() == null
                                || updateResponce.getData().getOptions() == null
                                || updateResponce.getData().getOptions().size() < 4) {
                            return;
                        }

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
        half_youTubePlayerHolder.half_image_like_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                half_youTubePlayerHolder.half_image_like = finalConvertView3.findViewById(R.id.half_image_like);
                int newStatus = applyLikeChange(position, null);
                bindHalfActionCount(finalConvertView3.findViewById(R.id.half_like_count), list.get(position).getLikes());
                bindHalfLikeIcon(half_youTubePlayerHolder.half_image_like, newStatus);
            }
        });

        half_youTubePlayerHolder.writer_follower.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent("WRITER_FOLLOWER");
                intent.putExtra("message", "click_event");

                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

                if (list.get(position).getIsFollow() == 0) {

                    half_youTubePlayerHolder.writer_follow.setVisibility(View.GONE);
                    half_youTubePlayerHolder.writer_follower.setVisibility(View.VISIBLE);
                    test_folloew.setText("Following");

                    set_follow_un_follow(list.get(position).getWriterId(), 1);


                } else {

                    half_youTubePlayerHolder.writer_follow.setVisibility(View.VISIBLE);
                    half_youTubePlayerHolder.writer_follower.setVisibility(View.GONE);

                    test_folloew.setText("Follow");

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

            if (list.get(position).getIsFollow() == 0) {

                half_youTubePlayerHolder.writer_follow.setVisibility(View.GONE);
                half_youTubePlayerHolder.writer_follower.setVisibility(View.VISIBLE);
                test_folloew.setText("Following");

                set_follow_un_follow(list.get(position).getWriterId(), 1);

            } else {

                half_youTubePlayerHolder.writer_follow.setVisibility(View.VISIBLE);
                half_youTubePlayerHolder.writer_follower.setVisibility(View.GONE);
                test_folloew.setText("Follow");

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
        boolean downloadable = Boolean.TRUE.equals(list.get(position).getDownloadable());
        imageViewShareAll.setImageResource(downloadable
                ? R.drawable.ic_download_all : R.drawable.ic_disabled_download_all);
        textViewAllPage.setTextColor(downloadable
                ? context.getColor(R.color.black) : Color.parseColor("#D1D1D1"));

        textViewSinglePage.setText("Share Article");
        textViewAllPage.setText("Download Video");
        relativeLayoutShareOpt.setVisibility(GONE);
        half_youTubePlayerHolder.half_image_share.setOnClickListener(view ->
                showVideoShareChooser(position, VideoShareLayout.HALF_YOUTUBE, convertView));


    }

    public void showPollingResuilt(String option, int position, int size, long postID, int optionID) {
        // Legacy entry point — submit vote and show multi-option results UI.
        if (pollingViewHolder != null && position >= 0 && position < list.size()) {
            updatearraylist(optionID, position);
            showPollingResultsUi(pollingViewHolder, list.get(position));
        }
        set_servey_answer_submit(postID, optionID, position);
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
                if (!response.isSuccessful() || response.body() == null) {
                    return;
                }
                try {
                    JsonObject body = response.body();
                    if (!body.has("data") || body.get("data") == null || !body.get("data").isJsonArray()) {
                        return;
                    }
                    JsonArray data = body.getAsJsonArray("data");
                    if (position < 0 || position >= list.size()) {
                        return;
                    }
                    ModelClass.Datum datum = list.get(position);
                    if (datum.getSurveyOptions() == null) {
                        return;
                    }
                    for (int i = 0; i < data.size() && i < datum.getSurveyOptions().size(); i++) {
                        JsonElement el = data.get(i);
                        if (el == null || !el.isJsonObject()) continue;
                        JsonObject opt = el.getAsJsonObject();
                        ModelClass.SurveyOptions local = datum.getSurveyOptions().get(i);
                        if (local == null) continue;
                        if (opt.has("percent") && !opt.get("percent").isJsonNull()) {
                            local.setPercent((int) Math.round(opt.get("percent").getAsDouble()));
                        }
                        if (opt.has("count") && !opt.get("count").isJsonNull()) {
                            local.setCount((int) Math.round(opt.get("count").getAsDouble()));
                        }
                    }
                    if (body.has("selected_option") && !body.get("selected_option").isJsonNull()) {
                        datum.setSelected_option(body.get("selected_option").getAsInt());
                    } else {
                        datum.setSelected_option(optionID);
                    }
                    list.set(position, datum);
                    if (pollingViewHolder != null) {
                        showPollingResultsUi(pollingViewHolder, datum);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
                    syncLikeCountFromServer(post_id, sub_post_id, likeDownloadRespouse.getLikes());
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

        int postIndex = findPostIndex(post_id);
        if (postIndex >= 0 && "share".equalsIgnoreCase(share)) {
            ModelClass.Datum item = list.get(postIndex);
            int shares = item.getShares() != null ? item.getShares() : 0;
            item.setShares(shares + 1);
            notifyDataSetChanged();
        }

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
                        syncEngagementFromServer(post_id, null, null, null, likeDownloadRespouse.getShares());
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
        ImageView like_for_video_image, dislike_for_video_image, full_image_whatsapp_bg;
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
        LinearLayout llOptionsLayout;
        LinearLayout optionRowYes, optionRowNo, resultRowYes, resultRowNo;
        RelativeLayout relativeLayoutSurveyMain;
        ImageView survey_half_image;
        View yesPercentFill, noPercentFill, radioYes, radioNo;
        TextView btnVoteNow;
        TextView badgeYes, badgeNo, tvYesLabel, tvNoLabel;
        TextView tvSurveyQuestionEnglish, tvAnswerYes, tvAnswerNo, tvYesPercent, tvNoPercent;
        /** Pending selection before Vote Now: 0=yes, 1=no, -1=none. */
        int pendingChoice = -1;
    }

    public static class PollingViewHolder {
        LinearLayout llPercentLayout;
        LinearLayout llOptionsLayout;
        LinearLayout optionRowA, optionRowB, optionRowC, optionRowD;
        LinearLayout resultRowA, resultRowB, resultRowC, resultRowD;
        RelativeLayout relativeLayoutSurveyMain;
        TextView tv_polling_question;
        TextView polling_A, polling_B, polling_C, polling_D;
        TextView tv_answer_A, tv_answer_B, tv_answer_C, tv_answer_D;
        TextView tv_result_label_A, tv_result_label_B, tv_result_label_C, tv_result_label_D;
        TextView badge_A, badge_B, badge_C, badge_D;
        TextView btnVoteNow;
        ImageView polling_half_image;
        View radio_A, radio_B, radio_C, radio_D;
        View optionFillA, optionFillB, optionFillC, optionFillD;
        /** Pending selection index 0–3 before Vote Now; -1 = none. */
        int pendingIndex = -1;
        int pendingOptionId = 0;
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

                    if (surveyFeedbackResponse.getSuccess()
                            && surveyFeedbackResponse.getData() != null
                            && surveyFeedbackResponse.getData().size() >= 2) {

                        ModelClass.Datum datum = list.get(position);
                        datum.setSurveyStatus(1);
                        datum.setYes_percent(surveyFeedbackResponse.getData().get(0).getPercent());
                        datum.setNo_percent(surveyFeedbackResponse.getData().get(1).getPercent());

                        list.set(position, datum);

                        percentageYes = " " + surveyFeedbackResponse.getData().get(0).getPercent();
                        percentageNo = " " + surveyFeedbackResponse.getData().get(1).getPercent();

                        if (surveyViewHolder != null) {
                            showSurveyResultsUi(
                                    surveyViewHolder,
                                    datum,
                                    parsePercent(surveyFeedbackResponse.getData().get(0).getPercent()),
                                    parsePercent(surveyFeedbackResponse.getData().get(1).getPercent())
                            );
                        }

                    }

                } else {

                }

            }

            @Override
            public void onFailure(Call<EncreptionRequest> call, Throwable t) {

            }
        });

    }

    private static float parsePercent(Object raw) {
        return PollOptionStyleHelper.parsePercent(raw);
    }

    /** True when payload has 3+ choices — use multi-option survey UI even if post_type is "survey". */
    private static boolean shouldUseMultiOptionSurveyUi(ModelClass.Datum datum) {
        if (datum == null || datum.getSurveyOptions() == null) {
            return false;
        }
        int named = 0;
        for (ModelClass.SurveyOptions opt : datum.getSurveyOptions()) {
            if (opt == null) continue;
            String n = opt.getSurveyOptionsName();
            if (n != null && !n.trim().isEmpty()) {
                named++;
            }
        }
        return named > 2;
    }

    /** Ensure each option has a stable 1-based id for survey_poll votes. */
    private static void ensureSurveyOptionIds(ModelClass.Datum datum) {
        if (datum == null || datum.getSurveyOptions() == null) {
            return;
        }
        for (int i = 0; i < datum.getSurveyOptions().size(); i++) {
            ModelClass.SurveyOptions opt = datum.getSurveyOptions().get(i);
            if (opt != null && opt.getId() == 0) {
                opt.setId(i + 1);
            }
        }
    }

    private static String surveyOptionLabel(ModelClass.Datum datum, int index, String fallback) {
        if (datum != null && datum.getSurveyOptions() != null
                && index >= 0 && index < datum.getSurveyOptions().size()) {
            ModelClass.SurveyOptions opt = datum.getSurveyOptions().get(index);
            if (opt != null && opt.getSurveyOptionsName() != null
                    && !opt.getSurveyOptionsName().trim().isEmpty()) {
                return opt.getSurveyOptionsName().trim();
            }
        }
        return fallback != null ? fallback : "";
    }

    /** Polling = post_type "survey" (Yes/No). Image on top, dark card below. */
    private void bindSurveyPollTwoOption(SurveyViewHolder holder, View convertView, final int position) {
        if (holder == null || convertView == null || list == null
                || position < 0 || position >= list.size()) {
            return;
        }
        holder.relativeLayoutSurveyMain = convertView.findViewById(R.id.rl_survay_main);
        holder.survey_half_image = convertView.findViewById(R.id.survey_half_image);
        holder.tvSurveyQuestionEnglish = convertView.findViewById(R.id.tv_survey_question_english);
        holder.llOptionsLayout = convertView.findViewById(R.id.ll_options_layout);
        holder.llPercentLayout = convertView.findViewById(R.id.ll_percent_layout);
        holder.optionRowYes = convertView.findViewById(R.id.option_row_yes);
        holder.optionRowNo = convertView.findViewById(R.id.option_row_no);
        holder.resultRowYes = convertView.findViewById(R.id.result_row_yes);
        holder.resultRowNo = convertView.findViewById(R.id.result_row_no);
        holder.radioYes = convertView.findViewById(R.id.radio_yes);
        holder.radioNo = convertView.findViewById(R.id.radio_no);
        holder.tvAnswerYes = convertView.findViewById(R.id.tv_answer_yes);
        holder.tvAnswerNo = convertView.findViewById(R.id.tv_answer_no);
        holder.tvYesLabel = convertView.findViewById(R.id.tv_yes_label);
        holder.tvNoLabel = convertView.findViewById(R.id.tv_no_label);
        holder.tvYesPercent = convertView.findViewById(R.id.tv_yes_percent);
        holder.tvNoPercent = convertView.findViewById(R.id.tv_no_percent);
        holder.yesPercentFill = convertView.findViewById(R.id.yes_percent_fill);
        holder.noPercentFill = convertView.findViewById(R.id.no_percent_fill);
        holder.badgeYes = convertView.findViewById(R.id.badge_yes);
        holder.badgeNo = convertView.findViewById(R.id.badge_no);
        holder.btnVoteNow = convertView.findViewById(R.id.btn_vote_now_survey);
        holder.pendingChoice = -1;

        ModelClass.Datum datum = list.get(position);
        try {
            imageBackgroundURL = datum.getBackgroundImage();
            if (holder.survey_half_image != null) {
                Glide.with(getApplicationContext())
                        .load(imageBackgroundURL)
                        .apply(flipGlideDecodeOptions())
                        .into(holder.survey_half_image);
            }
            if (holder.tvSurveyQuestionEnglish != null) {
                holder.tvSurveyQuestionEnglish.setText(datum.getTitle() != null ? datum.getTitle() : "");
            }
            String yesLabel = surveyOptionLabel(datum, 0, datum.getYes_option());
            String noLabel = surveyOptionLabel(datum, 1, datum.getNo_option());
            if (yesLabel == null || yesLabel.isEmpty() || "null".equalsIgnoreCase(yesLabel)) {
                yesLabel = "Yes";
            }
            if (noLabel == null || noLabel.isEmpty() || "null".equalsIgnoreCase(noLabel)) {
                noLabel = "No";
            }
            if (holder.tvAnswerYes != null) holder.tvAnswerYes.setText(yesLabel);
            if (holder.tvAnswerNo != null) holder.tvAnswerNo.setText(noLabel);
            if (holder.tvYesLabel != null) holder.tvYesLabel.setText(yesLabel);
            if (holder.tvNoLabel != null) holder.tvNoLabel.setText(noLabel);
        } catch (Exception e) {
            e.printStackTrace();
        }

        boolean voted = datum.getSurveyStatus() != null && datum.getSurveyStatus() == 1;
        if (voted) {
            showSurveyResultsUi(
                    holder,
                    datum,
                    parsePercent(datum.getYes_percent()),
                    parsePercent(datum.getNo_percent())
            );
        } else {
            showSurveyOptionsUi(holder);
            updateSurveyRadios(holder, -1);
            if (holder.optionRowYes != null) {
                holder.optionRowYes.setOnClickListener(v -> {
                    holder.pendingChoice = 0;
                    updateSurveyRadios(holder, 0);
                });
            }
            if (holder.optionRowNo != null) {
                holder.optionRowNo.setOnClickListener(v -> {
                    holder.pendingChoice = 1;
                    updateSurveyRadios(holder, 1);
                });
            }
            if (holder.btnVoteNow != null) {
                final String yesVote = surveyOptionLabel(datum, 0, datum.getYes_option());
                final String noVote = surveyOptionLabel(datum, 1, datum.getNo_option());
                holder.btnVoteNow.setOnClickListener(v -> {
                    if (holder.pendingChoice < 0) {
                        Toast.makeText(context, R.string.poll_select_option, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String response = holder.pendingChoice == 0 ? yesVote : noVote;
                    // Keep options visible until API returns real percents (avoid flashing 0%).
                    SurveyResponse(response, list.get(position).getPostId(), position);
                });
            }
        }
        bindSurveyPollEngagementActions(convertView, position);
    }

    private void showSurveyOptionsUi(SurveyViewHolder holder) {
        if (holder.llOptionsLayout != null) holder.llOptionsLayout.setVisibility(View.VISIBLE);
        if (holder.llPercentLayout != null) holder.llPercentLayout.setVisibility(View.GONE);
    }

    private void updateSurveyRadios(SurveyViewHolder holder, int selected) {
        PollOptionStyleHelper.applyRadio(holder.radioYes, PollOptionStyleHelper.ACCENT_GREEN, selected == 0);
        PollOptionStyleHelper.applyRadio(holder.radioNo, PollOptionStyleHelper.ACCENT_RED, selected == 1);
    }

    /** Percent-only results. Highest % = green, lowest % = red. No vote counts. */
    private void showSurveyResultsUi(SurveyViewHolder holder, ModelClass.Datum datum, float yesPct, float noPct) {
        if (holder == null) return;
        if (holder.llOptionsLayout != null) holder.llOptionsLayout.setVisibility(View.GONE);
        if (holder.llPercentLayout != null) {
            holder.llPercentLayout.setVisibility(View.VISIBLE);
            holder.llPercentLayout.requestLayout();
        }

        String yesLabel = surveyOptionLabel(datum, 0, datum != null ? datum.getYes_option() : null);
        String noLabel = surveyOptionLabel(datum, 1, datum != null ? datum.getNo_option() : null);
        if (yesLabel == null || yesLabel.isEmpty() || "null".equalsIgnoreCase(yesLabel)) yesLabel = "Yes";
        if (noLabel == null || noLabel.isEmpty() || "null".equalsIgnoreCase(noLabel)) noLabel = "No";
        int[] accents = PollOptionStyleHelper.pollRankAccents(yesPct, noPct);

        PollOptionStyleHelper.applyResultRow(
                holder.resultRowYes, holder.badgeYes, holder.tvYesLabel, holder.tvYesPercent,
                holder.yesPercentFill, accents[0], 1, yesLabel, yesPct);
        PollOptionStyleHelper.applyResultRow(
                holder.resultRowNo, holder.badgeNo, holder.tvNoLabel, holder.tvNoPercent,
                holder.noPercentFill, accents[1], 2, noLabel, noPct);

        // Re-apply after measure — same GONE→VISIBLE width race as multi-option surveys.
        if (holder.llPercentLayout != null) {
            final float y = yesPct;
            final float n = noPct;
            holder.llPercentLayout.post(() -> {
                PollOptionStyleHelper.setProgressWidth(holder.yesPercentFill, y);
                PollOptionStyleHelper.setProgressWidth(holder.noPercentFill, n);
            });
        }
    }

    /** Survey = post_type "survey_poll" (multi). Image on top, dark card below. */
    private void bindSurveyPollMultiOption(PollingViewHolder holder, View convertView, final int position) {
        if (holder == null || convertView == null || list == null
                || position < 0 || position >= list.size()) {
            return;
        }
        try {
            holder.relativeLayoutSurveyMain = convertView.findViewById(R.id.rl_survay_main);
            holder.tv_polling_question = convertView.findViewById(R.id.tv_polling_question);
            holder.polling_half_image = convertView.findViewById(R.id.polling_half_image);
            holder.llOptionsLayout = convertView.findViewById(R.id.ll_options_layout);
            holder.llPercentLayout = convertView.findViewById(R.id.ll_percent_layout);
            holder.btnVoteNow = convertView.findViewById(R.id.btn_vote_now_polling);

            holder.optionRowA = convertView.findViewById(R.id.option_row_A);
            holder.optionRowB = convertView.findViewById(R.id.option_row_B);
            holder.optionRowC = convertView.findViewById(R.id.option_row_C);
            holder.optionRowD = convertView.findViewById(R.id.option_row_D);
            holder.resultRowA = convertView.findViewById(R.id.result_row_A);
            holder.resultRowB = convertView.findViewById(R.id.result_row_B);
            holder.resultRowC = convertView.findViewById(R.id.result_row_C);
            holder.resultRowD = convertView.findViewById(R.id.result_row_D);

            holder.radio_A = convertView.findViewById(R.id.radio_A);
            holder.radio_B = convertView.findViewById(R.id.radio_B);
            holder.radio_C = convertView.findViewById(R.id.radio_C);
            holder.radio_D = convertView.findViewById(R.id.radio_D);

            holder.tv_answer_A = convertView.findViewById(R.id.tv_answer_A);
            holder.tv_answer_B = convertView.findViewById(R.id.tv_answer_B);
            holder.tv_answer_C = convertView.findViewById(R.id.tv_answer_C);
            holder.tv_answer_D = convertView.findViewById(R.id.tv_answer_D);

            holder.tv_result_label_A = convertView.findViewById(R.id.tv_result_label_A);
            holder.tv_result_label_B = convertView.findViewById(R.id.tv_result_label_B);
            holder.tv_result_label_C = convertView.findViewById(R.id.tv_result_label_C);
            holder.tv_result_label_D = convertView.findViewById(R.id.tv_result_label_D);

            holder.polling_A = convertView.findViewById(R.id.polling_A);
            holder.polling_B = convertView.findViewById(R.id.polling_B);
            holder.polling_C = convertView.findViewById(R.id.polling_C);
            holder.polling_D = convertView.findViewById(R.id.polling_D);

            holder.optionFillA = convertView.findViewById(R.id.option_fill_A);
            holder.optionFillB = convertView.findViewById(R.id.option_fill_B);
            holder.optionFillC = convertView.findViewById(R.id.option_fill_C);
            holder.optionFillD = convertView.findViewById(R.id.option_fill_D);

            holder.badge_A = convertView.findViewById(R.id.badge_A);
            holder.badge_B = convertView.findViewById(R.id.badge_B);
            holder.badge_C = convertView.findViewById(R.id.badge_C);
            holder.badge_D = convertView.findViewById(R.id.badge_D);

            holder.pendingIndex = -1;
            holder.pendingOptionId = 0;

            ModelClass.Datum datum = list.get(position);
            ensureSurveyOptionIds(datum);
            if (holder.tv_polling_question != null) {
                holder.tv_polling_question.setText(datum.getTitle() != null ? datum.getTitle() : "");
            }
            if (holder.polling_half_image != null) {
                String bg = datum.getBackgroundImage();
                Object tag = holder.polling_half_image.getTag();
                if (tag == null || bg == null || !tag.equals(bg)) {
                    if (imageLoader != null) {
                        imageLoader.displayImage(bg, holder.polling_half_image, options, null);
                    }
                    holder.polling_half_image.setTag(bg);
                }
            }

            if (datum.getSurveyOptions() == null || datum.getSurveyOptions().isEmpty()) {
                bindSurveyPollEngagementActions(convertView, position);
                return;
            }

            int n = Math.min(4, datum.getSurveyOptions().size());
            LinearLayout[] optionRows = {holder.optionRowA, holder.optionRowB, holder.optionRowC, holder.optionRowD};
            LinearLayout[] resultRows = {holder.resultRowA, holder.resultRowB, holder.resultRowC, holder.resultRowD};
            View[] radios = {holder.radio_A, holder.radio_B, holder.radio_C, holder.radio_D};
            TextView[] answers = {holder.tv_answer_A, holder.tv_answer_B, holder.tv_answer_C, holder.tv_answer_D};
            TextView[] resultLabels = {
                    holder.tv_result_label_A, holder.tv_result_label_B,
                    holder.tv_result_label_C, holder.tv_result_label_D
            };

            for (int i = 0; i < 4; i++) {
                boolean on = i < n;
                if (optionRows[i] != null) optionRows[i].setVisibility(on ? View.VISIBLE : View.GONE);
                if (resultRows[i] != null) resultRows[i].setVisibility(on ? View.VISIBLE : View.GONE);
                if (on) {
                    ModelClass.SurveyOptions opt = datum.getSurveyOptions().get(i);
                    String name = opt != null ? opt.getSurveyOptionsName() : "";
                    if (answers[i] != null) answers[i].setText(name != null ? name : "");
                    if (resultLabels[i] != null) resultLabels[i].setText(name != null ? name : "");
                    int accent = PollOptionStyleHelper.accentForIndex(i);
                    if (optionRows[i] != null) {
                        optionRows[i].setBackgroundResource(PollOptionStyleHelper.optionBg(accent));
                    }
                    PollOptionStyleHelper.applyRadio(radios[i], accent, false);
                }
            }

            boolean voted = datum.getSelected_option() != 0;
            if (voted) {
                showPollingResultsUi(holder, datum);
            } else {
                if (holder.llOptionsLayout != null) holder.llOptionsLayout.setVisibility(View.VISIBLE);
                if (holder.llPercentLayout != null) holder.llPercentLayout.setVisibility(View.GONE);
                updatePollingRadios(holder, -1, n);

                for (int i = 0; i < n; i++) {
                    final int idx = i;
                    ModelClass.SurveyOptions opt = datum.getSurveyOptions().get(i);
                    final int optionId = opt != null ? opt.getId() : (i + 1);
                    if (optionRows[i] != null) {
                        optionRows[i].setOnClickListener(v -> {
                            holder.pendingIndex = idx;
                            holder.pendingOptionId = optionId;
                            updatePollingRadios(holder, idx, n);
                        });
                    }
                }

                if (holder.btnVoteNow != null) {
                    holder.btnVoteNow.setOnClickListener(v -> {
                        if (holder.pendingIndex < 0 || holder.pendingOptionId == 0) {
                            Toast.makeText(context, R.string.poll_select_option, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        updatearraylist(holder.pendingOptionId, position);
                        showPollingResultsUi(holder, list.get(position));
                        set_servey_answer_submit(
                                list.get(position).getPostId(),
                                holder.pendingOptionId,
                                position
                        );
                    });
                }
            }
            bindSurveyPollEngagementActions(convertView, position);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                bindSurveyPollEngagementActions(convertView, position);
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Wire like / comment / share / flag on survey + survey_poll flip pages.
     * Uses the same view ids as half-image posts ({@code include_half_engagement_row}).
     * <p>
     * Row lives under the poll options (dark card), not overlaid on the hero image.
     * Overlay placement was clipped by the image FrameLayout and looked like "missing"
     * icons in the empty space under options.
     */
    private void bindSurveyPollEngagementActions(View convertView, final int position) {
        if (convertView == null || list == null || position < 0 || position >= list.size()) {
            return;
        }
        final View engagementRow = convertView.findViewById(R.id.with_out_strips_layout);
        if (convertView.findViewById(R.id.half_image_like) == null && engagementRow == null) {
            return;
        }

        // Counts + circle/glyph sizing. Skip half-post row offset — survey row is under
        // options and must stay put (offset was designed for half-image content strip).
        bindHalfActionCount(convertView.findViewById(R.id.half_like_count), list.get(position).getLikes());
        bindHalfActionCount(convertView.findViewById(R.id.half_comment_count), list.get(position).getComments());
        bindHalfActionCount(convertView.findViewById(R.id.half_share_count), list.get(position).getShares());
        tintHalfActionIcons(convertView);
        applyHalfActionIconSizes(context, convertView);

        if (engagementRow != null) {
            engagementRow.setVisibility(View.VISIBLE);
            engagementRow.setTranslationY(0f);
            engagementRow.setAlpha(1f);
            engagementRow.setElevation(8f * context.getResources().getDisplayMetrics().density);
            engagementRow.bringToFront();
        }

        final ImageView likeIcon = convertView.findViewById(R.id.half_image_like);
        final ImageView commentIcon = convertView.findViewById(R.id.half_comment_icon);
        final ImageView shareIcon = convertView.findViewById(R.id.half_share_icon);
        final ImageView reportIcon = convertView.findViewById(R.id.report_icon);
        final LinearLayout likeSlot = convertView.findViewById(R.id.half_image_like_icon);
        final LinearLayout commentSlot = convertView.findViewById(R.id.half_comment);
        final LinearLayout shareSlot = convertView.findViewById(R.id.half_share);
        final LinearLayout reportSlot = convertView.findViewById(R.id.report);
        final TextView likeCountView = convertView.findViewById(R.id.half_like_count);

        // Force every chip visible — recycled flip pages / prior GONE must not stick.
        View[] slots = {likeSlot, commentSlot, shareSlot, reportSlot, likeIcon, commentIcon, shareIcon, reportIcon};
        for (View slot : slots) {
            if (slot != null) {
                slot.setVisibility(View.VISIBLE);
                slot.setAlpha(1f);
            }
        }

        if (commentIcon != null) {
            commentIcon.setImageResource(R.drawable.ic_action_comment);
            commentIcon.setBackgroundResource(R.drawable.white_oval);
        }
        if (shareIcon != null) {
            shareIcon.setImageResource(R.drawable.ic_action_share);
            shareIcon.setBackgroundResource(R.drawable.white_oval);
        }
        if (reportIcon != null) {
            reportIcon.setImageResource(R.drawable.ic_action_report);
            reportIcon.setBackgroundResource(R.drawable.white_oval);
        }

        bindHalfLikeIcon(likeIcon, list.get(position).getLikeStatus());
        tintHalfActionIcons(convertView);
        if (likeIcon != null) {
            likeIcon.setTag(position);
            likeIcon.setOnClickListener(view -> {
                Object tag = view.getTag();
                if (!(tag instanceof Integer)) return;
                int currPosition = (Integer) tag;
                int newStatus = applyLikeChange(currPosition, likeCountView);
                bindHalfLikeIcon(likeIcon, newStatus);
            });
        }
        if (likeSlot != null) {
            likeSlot.setOnClickListener(v -> {
                if (likeIcon != null) likeIcon.performClick();
            });
        }

        View.OnClickListener openComments = v -> {
            Intent intent = new Intent("COMMENTS_PAGE");
            intent.putExtra("message", "click_event");
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            try {
                adapterCallback.OpenComments(
                        list.get(position).getPostId(),
                        list.get(position).getSubPostId()
                );
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
        };
        if (commentSlot != null) {
            commentSlot.setOnClickListener(openComments);
        } else if (commentIcon != null) {
            commentIcon.setOnClickListener(openComments);
        }

        View.OnClickListener shareClick = v -> shareSurveyScreenshot(convertView, position);
        if (shareSlot != null) {
            shareSlot.setOnClickListener(shareClick);
        } else if (shareIcon != null) {
            shareIcon.setOnClickListener(shareClick);
        }

        View.OnClickListener reportClick = v -> showHalfReportDialog(position);
        if (reportSlot != null) {
            reportSlot.setOnClickListener(reportClick);
        } else if (reportIcon != null) {
            reportIcon.setOnClickListener(reportClick);
        }
    }

    /** Flag/report dialog used by survey/poll engagement row (same flow as half posts). */
    private void showHalfReportDialog(final int position) {
        if (list == null || position < 0 || position >= list.size()) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.custom_dialog_layout, null);
        builder.setView(dialogView);

        final RadioButton one = dialogView.findViewById(R.id.one);
        final RadioButton two = dialogView.findViewById(R.id.two);
        final RadioButton three = dialogView.findViewById(R.id.three);
        final RadioButton four = dialogView.findViewById(R.id.four);
        final TextView report = dialogView.findViewById(R.id.report);
        final TextView title = dialogView.findViewById(R.id.title);
        final EditText full_image_edt_news_headlines =
                dialogView.findViewById(R.id.full_image_edt_news_headlines);
        final Button yes = dialogView.findViewById(R.id.yes);
        final ImageView close_dailog = dialogView.findViewById(R.id.close_dailog);
        final AlertDialog dialog = builder.create();

        if (close_dailog != null) {
            close_dailog.setOnClickListener(v -> dialog.dismiss());
        }

        APIService apiService = RestApiClient.getClient().create(APIService.class);
        apiService.reportissue(ReportParams()).enqueue(new Callback<GetReportIssueResponce>() {
            @Override
            public void onResponse(Call<GetReportIssueResponce> call,
                                   retrofit2.Response<GetReportIssueResponce> response) {
                GetReportIssueResponce updateResponce = response.body();
                if (updateResponce == null || updateResponce.getData() == null
                        || updateResponce.getData().getOptions() == null
                        || updateResponce.getData().getOptions().size() < 4) {
                    return;
                }
                if (one != null) one.setText("" + updateResponce.getData().getOptions().get(0));
                if (two != null) two.setText("" + updateResponce.getData().getOptions().get(1));
                if (three != null) three.setText("" + updateResponce.getData().getOptions().get(2));
                if (four != null) four.setText("" + updateResponce.getData().getOptions().get(3));
                if (report != null) report.setText("" + updateResponce.getData().getTitle());
                if (title != null) title.setText("" + updateResponce.getData().getOtherReason());
                if (full_image_edt_news_headlines != null) {
                    full_image_edt_news_headlines.setHint(
                            "" + updateResponce.getData().getPlaceholder());
                }
                if (yes != null) {
                    yes.setOnClickListener(view -> {
                        String reason = "";
                        if (one != null && one.isChecked()) {
                            reason = one.getText().toString();
                        } else if (two != null && two.isChecked()) {
                            reason = two.getText().toString();
                        } else if (three != null && three.isChecked()) {
                            reason = three.getText().toString();
                        } else if (four != null && four.isChecked()) {
                            reason = four.getText().toString();
                        }
                        String other = full_image_edt_news_headlines != null
                                ? full_image_edt_news_headlines.getText().toString()
                                : "";
                        JsonObject updateparams = DailogRequest(
                                reason, list.get(position).getPostId(), other);
                        APIService submitApi = RestApiClient.getClient().create(APIService.class);
                        submitApi.dailogSend(updateparams).enqueue(new Callback<DailogSendRespouse>() {
                            @Override
                            public void onResponse(Call<DailogSendRespouse> call,
                                                   retrofit2.Response<DailogSendRespouse> response) {
                                DailogSendRespouse body = response.body();
                                if (body != null) {
                                    Toast.makeText(context, "" + body.getMsg(), Toast.LENGTH_SHORT).show();
                                }
                                dialog.dismiss();
                            }

                            @Override
                            public void onFailure(Call<DailogSendRespouse> call, Throwable t) {
                                Toast.makeText(context, "" + call, Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
                }
            }

            @Override
            public void onFailure(Call<GetReportIssueResponce> call, Throwable t) {
                Toast.makeText(context, "" + call, Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show();
    }

    private void updatePollingRadios(PollingViewHolder holder, int selected, int n) {
        View[] radios = {holder.radio_A, holder.radio_B, holder.radio_C, holder.radio_D};
        for (int i = 0; i < n && i < radios.length; i++) {
            PollOptionStyleHelper.applyRadio(
                    radios[i],
                    PollOptionStyleHelper.accentForIndex(i),
                    selected == i
            );
        }
    }

    /**
     * Survey multi-option results: fixed colors by index (green/red/blue/orange).
     * Percent only — never vote counts.
     */
    private void showPollingResultsUi(PollingViewHolder holder, ModelClass.Datum datum) {
        if (holder == null || datum == null || datum.getSurveyOptions() == null) {
            return;
        }
        if (holder.llOptionsLayout != null) holder.llOptionsLayout.setVisibility(View.GONE);
        if (holder.llPercentLayout != null) {
            holder.llPercentLayout.setVisibility(View.VISIBLE);
            // Force a layout pass so progress tracks get a real width before fill sizing
            // (Special already-voted bind used to run while tracks were still 0px wide).
            holder.llPercentLayout.requestLayout();
        }

        int n = Math.min(4, datum.getSurveyOptions().size());
        LinearLayout[] rows = {holder.resultRowA, holder.resultRowB, holder.resultRowC, holder.resultRowD};
        TextView[] badges = {holder.badge_A, holder.badge_B, holder.badge_C, holder.badge_D};
        TextView[] labels = {
                holder.tv_result_label_A, holder.tv_result_label_B,
                holder.tv_result_label_C, holder.tv_result_label_D
        };
        TextView[] percents = {holder.polling_A, holder.polling_B, holder.polling_C, holder.polling_D};
        View[] fills = {holder.optionFillA, holder.optionFillB, holder.optionFillC, holder.optionFillD};

        for (int i = 0; i < 4; i++) {
            if (i >= n) {
                if (rows[i] != null) rows[i].setVisibility(View.GONE);
                continue;
            }
            float pct = parsePercent(datum.getSurveyOptions().get(i).getPercent());
            String name = datum.getSurveyOptions().get(i).getSurveyOptionsName();
            int accent = PollOptionStyleHelper.accentForIndex(i);
            PollOptionStyleHelper.applyResultRow(
                    rows[i], badges[i], labels[i], percents[i], fills[i],
                    accent, i + 1, name, pct
            );
        }

        // Re-apply fills after the results panel has been measured (Special first paint).
        if (holder.llPercentLayout != null) {
            final int optionCount = n;
            final ModelClass.Datum resultDatum = datum;
            final View[] fillViews = fills;
            holder.llPercentLayout.post(() -> {
                for (int i = 0; i < optionCount; i++) {
                    if (fillViews[i] == null || resultDatum.getSurveyOptions() == null
                            || i >= resultDatum.getSurveyOptions().size()) {
                        continue;
                    }
                    float pct = parsePercent(resultDatum.getSurveyOptions().get(i).getPercent());
                    PollOptionStyleHelper.setProgressWidth(fillViews[i], pct);
                }
            });
        }
    }


    private void updatearraylist(int optionID, int positionx) {
        try {
            if (list.get(positionx).getSurveyOptions() == null
                    || list.get(positionx).getSurveyOptions().isEmpty()) {
                return;
            }

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

            // Optimistic local tally: bump voted option, recompute % so results UI is not all 0%.
            boolean matched = false;
            int totalVotes = 0;
            for (int i = 0; i < surveyOptionsArrayList.size(); i++) {
                ModelClass.SurveyOptions o = surveyOptionsArrayList.get(i);
                if (o == null) continue;
                if (o.getId() == optionID) {
                    o.setCount(Math.max(0, o.getCount()) + 1);
                    matched = true;
                }
                totalVotes += Math.max(0, o.getCount());
            }
            if (!matched && optionID >= 1 && optionID <= surveyOptionsArrayList.size()) {
                ModelClass.SurveyOptions o = surveyOptionsArrayList.get(optionID - 1);
                if (o != null) {
                    o.setCount(Math.max(0, o.getCount()) + 1);
                    totalVotes += 1;
                }
            }
            if (totalVotes <= 0) {
                totalVotes = 1;
            }
            for (ModelClass.SurveyOptions o : surveyOptionsArrayList) {
                if (o == null) continue;
                o.setPercent((int) Math.floor((Math.max(0, o.getCount()) * 100.0) / totalVotes));
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

    private JsonObject set_writer_profile_data() {


        UserProfileRequest requestModel = new UserProfileRequest();
        requestModel.setDraftId(SharedPrefsData.getInt(context, Constants.DraftID, Constants.PREF_NAME));
        String jsonString = new com.google.gson.Gson().toJson(requestModel);
        String encrpt_data = null;
        /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            encrpt_data = encrypt(jsonString);
        }*/
        encrpt_data = encrypt(jsonString);
        showLog("API REQUEST E" + jsonString.toString());
        EncritonRequestData Encriton_Request = new EncritonRequestData();
        Encriton_Request.setData(encrpt_data);
        return new Gson().toJsonTree(Encriton_Request).getAsJsonObject();
    }
    /**
     * Called from getView during page turns. Must stay cheap: production 3.40.45
     * had this as a no-op. Releasing Exo / walking children here janks the 3D flip
     * on low-end (Narzo / Go). Full teardown belongs in HomePage after settle.
     */
    public static void pauseVideo(){
        try{
            if (youTubePlayerViewHalf != null) {
                youTubePlayerViewHalf.pause();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        try{
            if (exoPlayer != null) {
                exoPlayer.setPlayWhenReady(false);
                try {
                    exoPlayer.pause();
                } catch (Exception ignored) {
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /** Full Exo release — call only after flip settles, not from getView. */
    public static void releaseActiveExoPlayer() {
        try{
            if (exoPlayer != null) {
                exoPlayer.setPlayWhenReady(false);
                exoPlayer.pause();
                exoPlayer.stop();
                exoPlayer.release();
                exoPlayer = null;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static boolean hasActiveVideoPlayer() {
        return youTubePlayerViewHalf != null || exoPlayer != null;
    }

    public void resetAllFlipChildrenVideoPlayers(ViewGroup flipView) {
        if (flipView == null) {
            return;
        }
        for (int i = 0; i < flipView.getChildCount(); i++) {
            resetFullExoVideoView(flipView.getChildAt(i));
            resetFullYoutubeVideoView(flipView.getChildAt(i));
        }
    }

    public void resetFullExoVideoView(View convertView) {
        if (convertView == null) {
            return;
        }
        try {
            showLog("resetFullExoVideoView: stopping full exo video");
            PlayerView playerView = convertView.findViewById(R.id.normal_video_player);
            if (playerView != null) {
                Player attachedPlayer = playerView.getPlayer();
                if (attachedPlayer != null) {
                    attachedPlayer.setPlayWhenReady(false);
                    attachedPlayer.pause();
                    attachedPlayer.stop();
                    if (attachedPlayer != exoPlayer) {
                        attachedPlayer.release();
                    }
                }
                playerView.setPlayer(null);
                playerView.setVisibility(View.GONE);
            }
            // Do not releaseActiveExoPlayer() here — resetAllFlipChildrenVideoPlayers
            // walks every flip child; releasing per child blanks the active video page.

            ImageView thumbnail = convertView.findViewById(R.id.thaumbnail_for_video);
            ImageView playButton = convertView.findViewById(R.id.imageViewVideoPlayVideo);
            ProgressBar progress = convertView.findViewById(R.id.progress_bar_for_video);
            LinearLayout sideMenu = convertView.findViewById(R.id.video_side_menu);
            if (thumbnail != null) {
                thumbnail.setVisibility(View.VISIBLE);
            }
            if (playButton != null) {
                playButton.setVisibility(View.VISIBLE);
            }
            if (progress != null) {
                progress.setVisibility(View.GONE);
            }
            if (sideMenu != null) {
                sideMenu.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetFullYoutubeVideoView(View convertView) {
        if (convertView == null) {
            return;
        }
        try {
            // Global YouTube player is released by HomePage.teardownFlipVideoPlayers /
            // releaseActiveExoPlayer path once — do not null it per child walk.
            isFullYoutubeVideoIsInitialized = false;

            YouTubePlayerView youtubeView = convertView.findViewById(R.id.youtube_full_video);
            ImageView thumbnail = convertView.findViewById(R.id.imageViewItem1);
            ImageView playButton = convertView.findViewById(R.id.imageViewPlayVideo);
            LinearLayout sideMenu = convertView.findViewById(R.id.video_side_menu);
            if (youtubeView != null) {
                youtubeView.setVisibility(View.GONE);
            }
            if (thumbnail != null) {
                thumbnail.setVisibility(View.VISIBLE);
            }
            if (playButton != null) {
                playButton.setVisibility(View.VISIBLE);
            }
            if (sideMenu != null) {
                sideMenu.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Keeps the custom controller's center icons in a single correct state.
     *
     * The custom controller layout ({@code player_custom_ui.xml}) uses the legacy separate
     * {@code exo_play}/{@code exo_pause} buttons, but Media3 1.1.0's PlayerControlView only
     * manages a single {@code exo_play_pause} button. As a result Media3 never toggles these
     * buttons and both stay visible ("visible" in XML) at the same time. We drive their
     * visibility manually from the player state so exactly one icon shows:
     * Playing -> pause only, Paused -> play only, Ended -> neither (the big center play button
     * over the thumbnail acts as replay).
     */
    private void updateExoCenterControlIcons(PlayerView playerView) {
        if (playerView == null) {
            return;
        }
        View playBtn = playerView.findViewById(R.id.exo_play);
        View pauseBtn = playerView.findViewById(R.id.exo_pause);
        if (playBtn == null || pauseBtn == null) {
            return;
        }
        int state = exoPlayer != null ? exoPlayer.getPlaybackState() : Player.STATE_IDLE;
        boolean ended = state == Player.STATE_ENDED;
        boolean playing = exoPlayer != null && exoPlayer.getPlayWhenReady() && !ended;
        if (ended) {
            // Replay affordance is the big center play button (imageViewVideoPlayVideo) shown
            // over the thumbnail; hide both controller icons to avoid stacked icons.
            playBtn.setVisibility(GONE);
            pauseBtn.setVisibility(GONE);
        } else if (playing) {
            pauseBtn.setVisibility(VISIBLE);
            playBtn.setVisibility(GONE);
        } else {
            playBtn.setVisibility(VISIBLE);
            pauseBtn.setVisibility(GONE);
        }
    }

    /**
     * Attaches a listener so the center icons re-sync whenever the player state changes
     * (auto-play start, buffering, pause, end). Used by both half and full video.
     */
    private void attachExoCenterIconSync(PlayerView playerView) {
        if (playerView == null || exoPlayer == null) {
            return;
        }
        exoPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                updateExoCenterControlIcons(playerView);
            }

            @Override
            public void onPlayWhenReadyChanged(boolean playWhenReady, int reason) {
                updateExoCenterControlIcons(playerView);
            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                updateExoCenterControlIcons(playerView);
            }
        });
        updateExoCenterControlIcons(playerView);
    }

    /**
     * Wires the half (inline feed) video controller's play/pause buttons so tapping them
     * actually toggles playback. Media3 does not manage these legacy buttons, so without this
     * the half video could not be paused by tapping. Also keeps the center icon state correct.
     */
    private void wireHalfVideoExoPauseControl(PlayerView playerView) {
        if (playerView == null) {
            return;
        }
        View pauseBtn = playerView.findViewById(R.id.exo_pause);
        View playBtn = playerView.findViewById(R.id.exo_play);
        if (pauseBtn != null) {
            pauseBtn.setOnClickListener(v -> {
                if (exoPlayer != null) {
                    exoPlayer.setPlayWhenReady(false);
                    exoPlayer.pause();
                }
                updateExoCenterControlIcons(playerView);
            });
        }
        if (playBtn != null) {
            playBtn.setOnClickListener(v -> {
                if (exoPlayer != null) {
                    if (exoPlayer.getPlaybackState() == Player.STATE_ENDED) {
                        exoPlayer.seekTo(0);
                    }
                    exoPlayer.setPlayWhenReady(true);
                    exoPlayer.play();
                }
                updateExoCenterControlIcons(playerView);
            });
        }
        updateExoCenterControlIcons(playerView);
    }

    private void wireFullVideoExoPauseControl(PlayerView playerView, View convertView) {
        if (playerView == null || convertView == null) {
            return;
        }
        View pauseBtn = playerView.findViewById(R.id.exo_pause);
        View playBtn = playerView.findViewById(R.id.exo_play);
        if (pauseBtn != null) {
            pauseBtn.setOnClickListener(v -> {
                if (exoPlayer != null) {
                    exoPlayer.setPlayWhenReady(false);
                    exoPlayer.pause();
                }
                updateExoCenterControlIcons(playerView);
                LinearLayout sideMenu = convertView.findViewById(R.id.video_side_menu);
                if (sideMenu != null) {
                    sideMenu.setVisibility(View.VISIBLE);
                    sideMenu.bringToFront();
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
                    exoPlayer.play();
                }
                updateExoCenterControlIcons(playerView);
            });
        }
        updateExoCenterControlIcons(playerView);
    }

    /**
     * Sync Like/Comment/Share (video_side_menu) with ExoPlayer controller visibility.
     * Tap the playing video to show controls + engagement row; they auto-hide after the
     * controller timeout. Fixes share being unreachable until the user left and returned.
     */
    private void wireFullVideoExoSideMenuWithController(PlayerView playerView, View convertView) {
        if (playerView == null || convertView == null) {
            return;
        }
        final LinearLayout sideMenu = convertView.findViewById(R.id.video_side_menu);
        playerView.setControllerShowTimeoutMs(3500);
        playerView.setControllerHideOnTouch(true);
        playerView.setControllerAutoShow(true);
        // Explicit type — Media3 PlayerView overloads VisibilityListener + ControllerVisibilityListener
        playerView.setControllerVisibilityListener(
                (PlayerView.ControllerVisibilityListener) visibility -> {
            if (sideMenu == null) {
                return;
            }
            if (visibility == View.VISIBLE) {
                sideMenu.setVisibility(View.VISIBLE);
                sideMenu.bringToFront();
                sideMenu.setClickable(true);
            } else {
                boolean playing = exoPlayer != null && exoPlayer.isPlaying();
                sideMenu.setVisibility(playing ? View.INVISIBLE : View.VISIBLE);
                if (!playing) {
                    sideMenu.bringToFront();
                }
            }
        });
    }

    public void shareVideoDirect(int position, VideoShareHelper.ShareMode mode) {
        try {
            ModelClass.Datum item = list.get(position);
            String videoUrl = resolveVideoShareUrl(position);
            if (videoUrl == null) {
                Toast.makeText(context, "Video is not available to share", Toast.LENGTH_SHORT).show();
                return;
            }
            VideoShareHelper.shareVideo(context, activity, videoUrl, item.getTitle(), item.getShareLink(), mode,
                    () -> Share_Count_Value("share", item.getPostId()));
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Unable to share video", Toast.LENGTH_SHORT).show();
        }
    }

    private enum VideoShareLayout {
        HALF_EXO,
        HALF_YOUTUBE,
        FULL_EXO,
        FULL_YOUTUBE
    }

    private enum VideoShareAction {
        WHATSAPP_STATUS,
        WHATSAPP_CHAT,
        SHARE_ARTICLE,
        OTHER_APPS
    }

    public void showVideoShareChooserForPost(int position, View convertView) {
        if (list == null || position < 0 || position >= list.size() || convertView == null) {
            return;
        }
        ModelClass.Datum item = list.get(position);
        VideoShareLayout layout;
        if ("normal_video".equalsIgnoreCase(item.getPostType())) {
            layout = "3".equals(item.getMediaType()) ? VideoShareLayout.HALF_EXO : VideoShareLayout.HALF_YOUTUBE;
        } else if ("full_video".equalsIgnoreCase(item.getPostType())) {
            layout = "5".equals(item.getMediaType()) ? VideoShareLayout.FULL_EXO : VideoShareLayout.FULL_YOUTUBE;
        } else {
            return;
        }
        showVideoShareChooser(position, layout, convertView);
    }

    /**
     * Half and full videos: Download / Share / Dismiss first, then the share sheet.
     */
    private void showVideoShareChooser(int position, VideoShareLayout layout, View convertView) {
        FullVideoOptionsDialog.show(context, new FullVideoOptionsDialog.Listener() {
            @Override
            public void onDownloadVideo() {
                downloadFullVideo(position);
            }

            @Override
            public void onShare() {
                showVideoShareSheet(position, layout, convertView);
            }
        });
    }

    private void showVideoShareSheet(int position, VideoShareLayout layout, View convertView) {
        BottomSheetDialog bottomSheet = new BottomSheetDialog(context);
        View sheetView = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_video_share, null);
        bottomSheet.setContentView(sheetView);

        // Half + full: Contact / Other apps only — hide Status and Share Article.
        View shareArticleOption = sheetView.findViewById(R.id.option_share_article);
        View whatsappStatusOption = sheetView.findViewById(R.id.option_whatsapp_status);
        if (shareArticleOption != null) {
            shareArticleOption.setVisibility(View.GONE);
        }
        if (whatsappStatusOption != null) {
            whatsappStatusOption.setVisibility(View.GONE);
        }

        // Warm share cache while the sheet is open so Contact feels instant.
        if (canShareVideoFile(position)) {
            VideoShareHelper.prefetchVideo(context, resolveVideoShareUrl(position));
        }

        View whatsappChatOption = sheetView.findViewById(R.id.option_whatsapp_chat);
        // Primary Contact card: admin title/subtitle/colors/icon/bg (3.40.92+), half + full.
        if (whatsappChatOption != null) {
            FullVideoShareCardHelper.apply(context, whatsappChatOption);
        }

        View.OnClickListener dismissAndShare = actionView -> {
            bottomSheet.dismiss();
            VideoShareAction action;
            int viewId = actionView.getId();
            if (viewId == R.id.option_whatsapp_status) {
                action = VideoShareAction.WHATSAPP_STATUS;
            } else if (viewId == R.id.option_whatsapp_chat) {
                // Admin action_url (if set) replaces Send-to-Contact.
                if (FullVideoShareCardHelper.openActionUrlIfPresent(context)) {
                    return;
                }
                action = VideoShareAction.WHATSAPP_CHAT;
            } else if (viewId == R.id.option_share_article) {
                action = VideoShareAction.SHARE_ARTICLE;
            } else if (viewId == R.id.option_other_apps) {
                action = VideoShareAction.OTHER_APPS;
            } else {
                return;
            }
            executeVideoShare(position, layout, convertView, action);
        };

        if (whatsappStatusOption != null && whatsappStatusOption.getVisibility() == View.VISIBLE) {
            whatsappStatusOption.setOnClickListener(dismissAndShare);
        }
        if (whatsappChatOption != null) {
            whatsappChatOption.setOnClickListener(dismissAndShare);
        }
        if (shareArticleOption != null && shareArticleOption.getVisibility() == View.VISIBLE) {
            shareArticleOption.setOnClickListener(dismissAndShare);
        }
        sheetView.findViewById(R.id.option_other_apps).setOnClickListener(dismissAndShare);
        bottomSheet.show();
    }

    private void executeVideoShare(int position, VideoShareLayout layout, View convertView, VideoShareAction action) {
        switch (action) {
            case WHATSAPP_STATUS:
                if (canShareVideoFile(position)) {
                    shareVideoDirect(position, VideoShareHelper.ShareMode.WHATSAPP_STATUS);
                } else {
                    Toast.makeText(context, "Video file is not available to share", Toast.LENGTH_SHORT).show();
                }
                break;
            case WHATSAPP_CHAT:
                if (canShareVideoFile(position)) {
                    shareVideoDirect(position, VideoShareHelper.ShareMode.WHATSAPP_CHAT);
                } else {
                    Toast.makeText(context, "Video file is not available to share", Toast.LENGTH_SHORT).show();
                }
                break;
            case SHARE_ARTICLE:
                shareVideoScreenshot(position, layout, convertView, 0);
                break;
            case OTHER_APPS:
                if (canShareVideoFile(position)) {
                    shareVideoDirect(position, VideoShareHelper.ShareMode.GENERAL);
                } else {
                    Toast.makeText(context, "Video file is not available to share", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private String resolveVideoShareUrl(int position) {
        ModelClass.Datum item = list.get(position);
        if (item == null) {
            return null;
        }
        String downloadUrl = item.getDownloadUrl();
        if (downloadUrl != null && !downloadUrl.trim().isEmpty()) {
            return downloadUrl.trim();
        }
        String videoUrl = item.getVideoUrl();
        if (videoUrl != null && !videoUrl.trim().isEmpty()) {
            return videoUrl.trim();
        }
        return null;
    }

    private boolean canShareVideoFile(int position) {
        return resolveVideoShareUrl(position) != null;
    }

    /**
     * Full-video "Download Video" option. Saves through the same DownloadManager
     * helper the magazine/video downloads already use, so progress, the
     * "downloaded" toast and the share notification stay identical. Android 10+
     * writes to public Downloads without a permission; older builds ask for
     * WRITE_EXTERNAL_STORAGE first.
     */
    private void downloadFullVideo(int position) {
        if (list == null || position < 0 || position >= list.size()) {
            return;
        }
        String videoUrl = resolveVideoShareUrl(position);
        if (videoUrl == null) {
            Toast.makeText(context, "Video is not available to download", Toast.LENGTH_SHORT).show();
            return;
        }
        String fileName = buildVideoDownloadFileName(position, videoUrl);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                || ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            downloadPDFWithProgressBar(videoUrl, fileName, position, 1);
            return;
        }
        if (activity instanceof FragmentActivity) {
            PermissionX.init((FragmentActivity) activity)
                    .permissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .onExplainRequestReason(new ExplainReasonCallback() {
                        @Override
                        public void onExplainReason(@NonNull ExplainScope scope, @NonNull List<String> deniedList) {
                            scope.showRequestReasonDialog(deniedList,
                                    "Please allow the below necessary permissions to download videos", "OK", "Cancel");
                        }
                    })
                    .onForwardToSettings(new ForwardToSettingsCallback() {
                        @Override
                        public void onForwardToSettings(@NonNull ForwardScope scope, @NonNull List<String> deniedList) {
                            scope.showForwardToSettingsDialog(deniedList,
                                    "Please allow below permission manually from settings page to download videos", "OK", "Cancel");
                        }
                    })
                    .request(new RequestCallback() {
                        @Override
                        public void onResult(boolean allGranted, @NonNull List<String> grantedList,
                                             @NonNull List<String> deniedList) {
                            if (allGranted) {
                                downloadPDFWithProgressBar(videoUrl, fileName, position, 1);
                            }
                        }
                    });
        } else if (activity != null) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 234);
        }
    }

    private String buildVideoDownloadFileName(int position, String videoUrl) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(videoUrl);
        if (extension == null || extension.trim().isEmpty()) {
            extension = "mp4";
        }
        return "anyNews_" + System.currentTimeMillis() + "_"
                + list.get(position).getPostId() + "." + extension;
    }

    private void shareVideoScreenshot(int position, VideoShareLayout layout, View convertView, int whatsappMode) {
        switch (layout) {
            case HALF_EXO:
                shareExoPlayerVideoScreenShot(convertView, position, whatsappMode,
                        convertView.findViewById(R.id.normal_video_player),
                        convertView.findViewById(R.id.thaumbnail_for_video),
                        convertView.findViewById(R.id.imageViewVideoPlayVideo),
                        convertView.findViewById(R.id.progress_bar_for_video));
                break;
            case HALF_YOUTUBE:
                shareYoutubeVideoScreenShot(convertView, position,
                        convertView.findViewById(R.id.halfyoutubecontainer), whatsappMode,
                        convertView.findViewById(R.id.imageViewItem__));
                break;
            case FULL_EXO:
                shareExoPlayerFullVideoShare(position, whatsappMode,
                        convertView.findViewById(R.id.thaumbnail_for_video),
                        convertView.findViewById(R.id.exo_play_layout),
                        convertView.findViewById(R.id.imageViewVideoPlayVideo),
                        convertView.findViewById(R.id.normal_video_player));
                break;
            case FULL_YOUTUBE:
                shareYoutubePlayerFullVideoShare(position, whatsappMode,
                        convertView.findViewById(R.id.imageViewItem1),
                        convertView.findViewById(R.id.youtube_full_video),
                        convertView.findViewById(R.id.layoutYoutubeFullView),
                        convertView.findViewById(R.id.imageViewPlayVideo),
                        convertView.findViewById(R.id.video_side_menu));
                break;
        }
    }

    private void launchScreenshotShareIntent(Intent intent, int whatsappMode) {
        if (whatsappMode == 1) {
            if (!appInstalledOrNot("com.whatsapp")) {
                Toast.makeText(context, "WhatsApp is not installed", Toast.LENGTH_SHORT).show();
                return;
            }
            intent.putExtra("jid", "status@broadcast");
            intent.setPackage("com.whatsapp");
            context.startActivity(intent);
        } else if (whatsappMode == 2) {
            if (!appInstalledOrNot("com.whatsapp")) {
                Toast.makeText(context, "WhatsApp is not installed", Toast.LENGTH_SHORT).show();
                return;
            }
            intent.setPackage("com.whatsapp");
            context.startActivity(intent);
        } else if (context != null && context.getPackageName().equals(BuildConfig.APPLICATION_ID)) {
            context.startActivity(Intent.createChooser(intent, "Share Link"));
        }
    }

    public void shareFullMagazine(int position) {
        if (list == null || position < 0 || position >= list.size()) {
            return;
        }
        prepareAndShareFullMagazine(position);
    }

    public void shareMagazineImagesFallback(int position) {
        prepareAndShareFullMagazine(position);
    }

    private void prepareAndShareFullMagazine(int position) {
        ModelClass.Datum item = list.get(position);
        String downloadUrl = item.getDownloadUrl();
        List<String> images = item.getImages();
        boolean hasPdf = downloadUrl != null && !downloadUrl.trim().isEmpty();
        boolean hasImages = images != null && !images.isEmpty();

        if (!hasPdf && !hasImages) {
            Toast.makeText(context, "Magazine pages are not available", Toast.LENGTH_LONG).show();
            return;
        }

        Toast.makeText(context, "Your magazine is downloading", Toast.LENGTH_LONG).show();
        new Thread(() -> {
            try {
                if (hasPdf) {
                    File pdfFile = downloadMagazinePdfToCache(position, downloadUrl.trim());
                    if (pdfFile != null && pdfFile.exists() && pdfFile.length() > 0) {
                        Uri pdfUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", pdfFile);
                        postToMain(() -> launchMagazinePdfShareIntent(position, pdfUri));
                        return;
                    }
                }
                if (hasImages) {
                    ArrayList<Uri> uris = downloadMagazineImagesToCache(position, images);
                    if (!uris.isEmpty()) {
                        postToMain(() -> launchMagazineImagesShareIntent(position, uris));
                        return;
                    }
                }
                postToMain(() -> Toast.makeText(context, "Unable to share magazine", Toast.LENGTH_LONG).show());
            } catch (Exception e) {
                e.printStackTrace();
                postToMain(() -> Toast.makeText(context, "Unable to share magazine", Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void postToMain(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    private File getMagazineShareCacheDir(int position) {
        File cacheDir = new File(context.getExternalCacheDir(),
                "magazine_share_" + list.get(position).getPostId());
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        return cacheDir;
    }

    private File downloadMagazinePdfToCache(int position, String pdfUrl) {
        try {
            File cacheDir = getMagazineShareCacheDir(position);
            File outFile = new File(cacheDir, "magazine.pdf");
            downloadRemoteFile(pdfUrl, outFile);
            return outFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private ArrayList<Uri> downloadMagazineImagesToCache(int position, List<String> images) {
        ArrayList<Uri> uris = new ArrayList<>();
        File cacheDir = getMagazineShareCacheDir(position);
        try {
            for (int i = 0; i < images.size(); i++) {
                String imageUrl = images.get(i);
                if (imageUrl == null || imageUrl.trim().isEmpty()) {
                    continue;
                }
                String extension = getFileExtensionFromUrl(imageUrl.trim());
                File outFile = new File(cacheDir, "page_" + i + "." + extension);
                downloadRemoteFile(imageUrl.trim(), outFile);
                if (outFile.exists() && outFile.length() > 0) {
                    Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", outFile);
                    uris.add(uri);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return uris;
    }

    private String getFileExtensionFromUrl(String url) {
        String path = url;
        int queryIndex = path.indexOf('?');
        if (queryIndex > 0) {
            path = path.substring(0, queryIndex);
        }
        int dotIndex = path.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < path.length() - 1) {
            String ext = path.substring(dotIndex + 1).toLowerCase(Locale.getDefault());
            if (ext.matches("[a-z0-9]{2,5}")) {
                return ext;
            }
        }
        return "jpg";
    }

    private void downloadRemoteFile(String urlStr, File outFile) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setConnectTimeout(20000);
        conn.setReadTimeout(60000);
        conn.setInstanceFollowRedirects(true);
        conn.setRequestProperty("User-Agent", "anyNews-Android");
        conn.connect();
        int responseCode = conn.getResponseCode();
        if (responseCode >= 400) {
            conn.disconnect();
            throw new IOException("HTTP " + responseCode + " for " + urlStr);
        }
        try (InputStream in = conn.getInputStream(); FileOutputStream out = new FileOutputStream(outFile)) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        } finally {
            conn.disconnect();
        }
    }

    private void launchMagazinePdfShareIntent(int position, Uri pdfUri) {
        String mInvitationUrl = list.get(position).getShareLink();
        String msg = list.get(position).getTitle() + " " + mInvitationUrl;
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_STREAM, pdfUri);
        intent.putExtra(Intent.EXTRA_SUBJECT, mInvitationUrl);
        intent.putExtra(Intent.EXTRA_TEXT, msg);
        applyWhatsappTarget(intent);
        grantShareUriPermissions(intent, Collections.singletonList(pdfUri));
        startShareActivity(intent, "Share Magazine");
        Share_Count_Value("share", list.get(position).getPostId());
        fromWhatsapp = false;
    }

    private void launchMagazineImagesShareIntent(int position, ArrayList<Uri> uris) {
        String mInvitationUrl = list.get(position).getShareLink();
        String msg = list.get(position).getTitle() + " " + mInvitationUrl;
        Intent intent = new Intent();
        if (uris.size() == 1) {
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_STREAM, uris.get(0));
        } else {
            intent.setAction(Intent.ACTION_SEND_MULTIPLE);
            intent.setType("image/*");
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        }
        intent.putExtra(Intent.EXTRA_SUBJECT, mInvitationUrl);
        intent.putExtra(Intent.EXTRA_TEXT, msg);
        applyWhatsappTarget(intent);
        grantShareUriPermissions(intent, uris);
        startShareActivity(intent, "Share Magazine");
        Share_Count_Value("share", list.get(position).getPostId());
        fromWhatsapp = false;
    }

    private void applyWhatsappTarget(Intent intent) {
        if (!fromWhatsapp) {
            return;
        }
        if (!appInstalledOrNot("com.whatsapp")) {
            Toast.makeText(context, "WhatsApp is not installed", Toast.LENGTH_SHORT).show();
            fromWhatsapp = false;
            return;
        }
        intent.setPackage("com.whatsapp");
    }

    private void startShareActivity(Intent intent, String chooserTitle) {
        try {
            Intent chooser = Intent.createChooser(intent, chooserTitle);
            if (activity != null && !activity.isFinishing()) {
                activity.startActivity(chooser);
            } else if (context != null) {
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(chooser);
            }
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, "No app available to share", Toast.LENGTH_SHORT).show();
        }
    }

    private void grantShareUriPermissions(Intent intent, List<Uri> uris) {
        if (uris == null || uris.isEmpty()) {
            return;
        }
        ClipData clipData = ClipData.newUri(context.getContentResolver(), "share", uris.get(0));
        for (int i = 1; i < uris.size(); i++) {
            clipData.addItem(new ClipData.Item(uris.get(i)));
        }
        intent.setClipData(clipData);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        List<ResolveInfo> resInfoList = context.getPackageManager()
                .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            for (Uri uri : uris) {
                context.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
        }
        if (fromWhatsapp && appInstalledOrNot("com.whatsapp")) {
            for (Uri uri : uris) {
                context.grantUriPermission("com.whatsapp", uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
        }
    }

    @SuppressLint("Range")
    public void downloadPDFWithProgressBar(String url, String title,int position,int from) {
        isVideoDownloadCancel=false;

        showLog("URL:"+url);
        showLog("TITLE:"+title);
        if (url == null || url.trim().isEmpty()) {
            if (from == 0) {
                shareFullMagazine(position);
            } else {
                Toast.makeText(context, "Download file is not available", Toast.LENGTH_LONG).show();
            }
            return;
        }
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
                    }
                },1000);



                context.unregisterReceiver(this);

            }
        };
        IntentFilter downloadFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(onComplete, downloadFilter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            context.registerReceiver(onComplete, downloadFilter);
        }
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
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), title);
                showLog("file url:"+file.getAbsolutePath());
                if (!file.exists() || file.length() == 0) {
                    shareFullMagazine(position);
                    return;
                }
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("application/pdf");
                intent.putExtra(Intent.EXTRA_SUBJECT, mInvitationUrl);
                intent.putExtra(Intent.EXTRA_TEXT, msg);
                Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                applyWhatsappTarget(intent);
                grantShareUriPermissions(intent, Collections.singletonList(uri));
                startShareActivity(intent, "Share Magazine");
                fromWhatsapp = false;
            } catch (Exception e) {
                e.printStackTrace();
                shareFullMagazine(position);
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
            simpleExoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);

        }else{
            layoutParentTitle.setVisibility(View.GONE);
            layoutParentReportDetails.setVisibility(View.GONE);
            layoutParentMiddle.setVisibility(View.GONE);
            LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
            layoutParentTop.setLayoutParams(params);
            simpleExoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
        }
        fullscreen = !fullscreen;
    }

    public void shareYoutubeVideoScreenShot(View finalConvertView2, int position, LinearLayout container, int from, ImageView imageViewItems) {
        half_youTubePlayerHolder.relative_half_content = finalConvertView2.findViewById(R.id.relative_half_contents);
        half_youTubePlayerHolder.with_strip_layout = finalConvertView2.findViewById(R.id.with_strips_layout);
        half_youTubePlayerHolder.with_out_strip_layout = finalConvertView2.findViewById(R.id.with_out_strips_layout);
        half_youTubePlayerHolder.layoutParentReportDetails = finalConvertView2.findViewById(R.id.ll_footer_layout);
        half_youTubePlayerHolder.layoutParentDate = finalConvertView2.findViewById(R.id.layoutParentDate);
        TextView textViewWebUrl=finalConvertView2.findViewById(R.id.textViewWebUrl);
        container.setVisibility(View.GONE);
        half_youTubePlayerHolder.layoutParentReportDetails.setVisibility(View.GONE);
        textViewWebUrl.setVisibility(View.GONE);
        half_youTubePlayerHolder.layoutParentDate.setVisibility(VISIBLE);
        if ("4".equalsIgnoreCase(list.get(position).getMediaType())) {
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
                if ("4".equalsIgnoreCase(list.get(position).getMediaType())) {
                    if (isYoutubeVideoIsInitialized) {
                        youTubePlayerViewHalf.pause();
                        container.setVisibility(VISIBLE);
                        imageViewItems.setVisibility(View.GONE);
                    } else {
                        imageViewItems.setVisibility(View.VISIBLE);
                    }
                }
                intent.setType("image/*");
                launchScreenshotShareIntent(intent, from);
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
                launchScreenshotShareIntent(intent, from);
                Share_Count_Value("share", list.get(position).getPostId());

            }
        }.start();


    }
}