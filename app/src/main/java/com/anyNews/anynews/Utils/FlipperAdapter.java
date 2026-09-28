package com.anyNews.anynews.Utils;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
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
import android.graphics.drawable.ColorDrawable;
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
import android.os.SystemClock;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
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
import com.anyNews.anynews.flipview.FlipView;
import com.anyNews.anynews.BuildConfig;
import com.anyNews.anynews.Pojo.CardItem;
import com.anyNews.anynews.Pojo.DailogSendRespouse;
import com.anyNews.anynews.Pojo.DialogSendRequest;
import com.anyNews.anynews.Pojo.DilogeResest;
import com.anyNews.anynews.Pojo.EncreptionRequest;
import com.anyNews.anynews.Pojo.EncritonRequestData;
import com.anyNews.anynews.Pojo.FlipGoogleNativeAds;
import com.anyNews.anynews.Pojo.GetReportIssueResponce;
import com.anyNews.anynews.Pojo.LanguageModelResponse;
import com.anyNews.anynews.Pojo.LikeDownloadRespouse;
import com.anyNews.anynews.Pojo.LikeSelectionRequest;
import com.anyNews.anynews.Pojo.SurveyFeedbackResponse;
import com.anyNews.anynews.Pojo.SurveyRequestFeedback;
import com.anyNews.anynews.Pojo.UserProfileRequest;
import com.anyNews.anynews.Pojo.WriterStripStyleConfig;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.anyNews.anynews.Services.ShadowTransformer;
import com.anyNews.anynews.interfaces.CustomPlayerUiController;
import com.anyNews.anynews.interfaces.LoaderCommonOption;
import com.anyNews.anynews.interfaces.ScreenShott;
import com.anyNews.anynews.interfaces.ShareInterfaceListener;
import com.anyNews.anynews.interfaces.SharePostToWhatsApp;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.RequestBuilder;
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
import androidx.media3.common.C;
import androidx.media3.common.VideoSize;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.DefaultLoadControl;
import androidx.media3.exoplayer.DefaultRenderersFactory;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.common.util.Util;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerView;
import androidx.media3.ui.TimeBar;
import com.google.android.gms.ads.MediaContent;
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
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
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
import java.util.Formatter;
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
    /**
     * Blocks double-taps from opening multiple system share sheets. While a launch
     * is in flight, or within {@link #SHARE_LAUNCH_GUARD_MS} of the last accept,
     * further share taps are ignored.
     */
    private static final long SHARE_LAUNCH_GUARD_MS = 900L;
    private long lastShareLaunchAtMs = 0L;
    private boolean shareLaunchInFlight = false;
    /** Debounce for video options dialog / bottom sheet open. */
    private long lastVideoShareUiAtMs = 0L;
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
    /** Feed position currently owning shared {@link #exoPlayer} (half or full). */
    private static int activeExoPosition = -1;
    private static String activeExoVideoUrl;
    /**
     * Set by footer Reload. Mid-watch position lives on the shared player
     * ({@link #exoPlayer} current position + {@link #activeExoPosition}/{@link #activeExoVideoUrl}),
     * not on disk. Until the next explicit start, rebind must not restore that timestamp.
     * Pause/resume without Reload does not set this.
     */
    private static boolean discardSavedPlaybackUntilNextStart = false;
    /**
     * True after the shared feed player has pushed a frame.
     * While this is set, nothing opaque may cover the picture (poster, shutter, spinner)
     * until pause-leave, end, or error. A brief rebuffer keeps the last TextureView frame.
     */
    private static boolean feedExoFrameRendered = false;
    /** PlayerView currently showing {@link #exoPlayer}. Used to pin the last frame. */
    private static java.lang.ref.WeakReference<PlayerView> feedExoPlayerViewRef;
    /**
     * Stable TextureView host reparented under {@code exo_play_layout} (picture only).
     * Chrome stays on the scrap PlayerView ({@code surface_type=none}).
     */
    @Nullable
    private static FeedExoSurfaceHost feedExoSurfaceHost;
    /** Flip card root that currently owns the stable surface (for FlipView pin). */
    private static java.lang.ref.WeakReference<View> feedExoHostRootRef;
    /** Debounce the white buffering ring so a one-frame rebuffer does not strobe it. */
    private static final long EXO_SPINNER_DEBOUNCE_MS = 600L;
    private static final Handler EXO_OVERLAY_HANDLER = new Handler(Looper.getMainLooper());
    private static ProgressBar pendingExoSpinner;
    private static boolean feedExoSpinnerArmed;
    private static ImageView feedExoThumbnailOverlay;
    private static ImageView feedExoCenterPlayOverlay;
    private static final Runnable SHOW_EXO_SPINNER = new Runnable() {
        @Override
        public void run() {
            if (feedExoSpinnerArmed && pendingExoSpinner != null) {
                pendingExoSpinner.setVisibility(View.VISIBLE);
            }
        }
    };
    /**
     * User explicitly paused (Exo/YouTube controls). Blocks auto-restart until the next
     * flip settle clears this via {@link #clearUserPausedVideoAutoplay()}.
     */
    private boolean userPausedVideoAutoplay = false;
    /** Guards double flip-next requests from multiple ended listeners. */
    private int lastAutoplayEndedNotifyPos = -1;
    private long lastAutoplayEndedNotifyAtMs = 0L;
    static String videoURL;
    public static boolean fullscreen=false;
    public View customConvertView;
    public int customPosition;
    Callbacks callbacks;
    public NativeAd nativeAd=null;
    /**
     * Prevents unbounded getView recursion from search bind-fail retry (ANR).
     */
    private int searchBindRetryDepth = 0;
    /**
     * Response id of the last Google native painted into a feed seat. Used only to
     * prefer a different ready creative for the next seat when the pool has one —
     * never as a fallback to re-show a prior seat's ad (that was the same-ad bug).
     */
    @Nullable
    private String lastPaintedGoogleResponseId;
    /** Visible flip page — used to decide whether a buffer ad seat needs rebind. */
    private int currentFlipPage = 0;
    private final SparseArray<PendingNativeAdBind> pendingNativeAdBinds = new SparseArray<>();
    /**
     * Flip indices whose ad seat was bound without a creative (default ad placeholder).
     * When a fill arrives later, {@link #setCurrentFlipPage} / notify rebinds so the
     * user never stays on the house-ad placeholder after AdMob succeeds.
     */
    private final SparseBooleanArray adShellAwaitingCreative = new SparseBooleanArray();
    /**
     * Live Google ad shells awaiting creative — paint natives in place on fill so we
     * avoid FlipView {@code dataSetChanged()} (full recycle) mid-gesture.
     * Visible like-count TextViews keyed by postId so Update_Like can refresh the
     * on-screen number without waiting for FlipView to rebind (notify alone often
     * left the current page stuck at 0 / optimistic 1 until a later unlike).
     */
    private final android.util.LongSparseArray<java.lang.ref.WeakReference<TextView>> likeCountViewsByPostId =
            new android.util.LongSparseArray<>();
    /** Same FlipView rebind gap as likes — remember comment count TextViews by postId. */
    private final android.util.LongSparseArray<java.lang.ref.WeakReference<TextView>> commentCountViewsByPostId =
            new android.util.LongSparseArray<>();
    /** Share count TextViews by postId — never FlipView-notify just to bump shares. */
    private final android.util.LongSparseArray<java.lang.ref.WeakReference<TextView>> shareCountViewsByPostId =
            new android.util.LongSparseArray<>();
    /** WhatsApp share count TextViews (full posts) by postId. */
    private final android.util.LongSparseArray<java.lang.ref.WeakReference<TextView>> whatsappShareCountViewsByPostId =
            new android.util.LongSparseArray<>();

    private final SparseArray<java.lang.ref.WeakReference<FrameLayout>> adShellPlaceholders =
            new SparseArray<>();
    private final SparseArray<Integer> adShellFromViewTypes = new SparseArray<>();
    /**
     * Ad shell without creative: show Share-with-friends default immediately
     * (never blank white). Seat stays {@code ad} so a late fill can overlay the
     * placeholder in place. Soft timeout only nudges a retry — never demotes
     * the list row to permanent {@code share_app} (layout thrash / flip lag).
     */
    private static final long AD_SHELL_FILL_TIMEOUT_MS = 8_000L;
    /**
     * July/Sep-7 parked-seat fill: poll while the user waits on Share default
     * so a late AdMob / AdPreloader creative paints in place. First tick
     * matches SETTLE_GRACE (160ms); later ticks ~450ms. Never calls
     * {@code notifyDataSetChanged} on every scroll — only seat-gated recycle
     * after repeated in-place misses.
     */
    private static final long PARKED_AD_FILL_FIRST_MS = 200L;
    private static final long PARKED_AD_FILL_POLL_MS = 800L;
    /** ~6s of polls then long cool-down — avoid request floods while parked on Share. */
    private static final int PARKED_AD_FILL_MAX_TICKS = 8;
    private static final long PARKED_AD_FILL_COOLDOWN_MS = 20_000L;
    /** Decorative ad blur: smaller radius + downscale; skip on buffer pages. */
    private static final int AD_BLUR_RADIUS = 24;
    private static final int AD_BLUR_SAMPLING = 4;
    private final Handler adShellHandler = new Handler(Looper.getMainLooper());
    private final SparseArray<Runnable> adShellTimeoutRunnables = new SparseArray<>();
    @Nullable private Runnable parkedAdFillWatchdog;
    private int parkedAdFillPage = -1;
    private int parkedAdFillTicks = 0;
    private boolean pendingSafeAdNotify = false;
    private boolean pendingFlipDatasetNotify = false;
            private final Runnable deferredSafeAdNotify = new Runnable() {
        @Override
        public void run() {
            pendingSafeAdNotify = false;
            try {
                if (shouldDeferAdWork() || isHomeFlipAnimating() || isUserDrivingFlip()) {
                    scheduleSafeAdNotify(Math.max(64L, FlipAdWorkGate.millisUntilAdWorkSafe()));
                    return;
                }
                // Prefer in-place paint. Unconditional notifyDataSetChanged while
                // seated on an ad recycled onto black ads_screen → creative → loop.
                if (tryPaintReadyGoogleNativesInPlace()) {
                    return;
                }
                if (needsAdSeatDatasetRebind()) {
                    showLog("ANYNEWS_ADS seat-only deferred notifyDataSetChanged");
                    notifyWhenFlipSettled();
                }
            } catch (Exception e) {
                showLog("ANYNEWS_ADS rebind skipped: " + e.getMessage());
            }
        }
    };
    /**
     * One coalesced FlipView recycle after the user stops flipping. Running
     * {@code notifyDataSetChanged} mid-swipe cleared the page index; the next
     * notify then treated the seat as unknown and snapped to the first article.
     */
    private final Runnable deferredFlipDatasetNotify = new Runnable() {
        @Override
        public void run() {
            if (isUserDrivingFlip()) {
                adShellHandler.postDelayed(this, 32L);
                return;
            }
            pendingFlipDatasetNotify = false;
            try {
                notifyDataSetChanged();
            } catch (Exception ignored) {
            }
        }
    };

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
        // Mid-gesture only: onFlippedToPage fires from dispatchDraw while scroller runs.
        // Index update is enough; bind / watchdog / AdLoader / paint wait for land.
        // Do NOT gate on settle-grace (shouldDeferAdWork) — settle still needs to arm
        // the landed seat; MediaView paint is deferred inside bind/scheduleSettledAdWork.
        if (isHomeFlipAnimating()) {
            return;
        }
        // Land: attach deferred buffer MediaView; if still on house shell, arm
        // parked in-place fill (no FlipView recycle / black loop).
        try {
            cancelParkedAdFillWatchdog();
            if (list == null || page < 0 || page >= list.size()) {
                return;
            }
            ModelClass.Datum slot = list.get(page);
            if (slot == null || slot.getPostType() == null
                    || !slot.getPostType().equalsIgnoreCase("ad")) {
                cancelAdShellFillTimeout(page);
                return;
            }
            cancelAdShellFillTimeout(page);
            if (pendingNativeAdBinds.get(page) != null && !shouldDeferAdWork()) {
                adShellAwaitingCreative.delete(page);
                bindPendingNativeAdMediaForPage(page);
                return;
            }
            // Flip-back often keeps the scrap NativeAdView (headline/CTA) while the
            // MediaView video surface is dead, or after flip-away destroy left a
            // zombie chrome. Do not treat "NativeAdView present" as healthy.
            if (isCurrentAdSeatShowingNativeCreative()) {
                if (ensureLandedAdMediaAlive(page)) {
                    adShellAwaitingCreative.delete(page);
                    return;
                }
                // Dead / destroyed creative stripped — fall through to house + reload.
            }
            // House / empty shell — arm parked fill; HomePage.scheduleSettledAdWork
            // owns the reliability AdLoader (avoid double-stack on land).
            adShellAwaitingCreative.put(page, true);
            scheduleAdShellFillTimeout(page);
            armParkedAdFillWatchdog(page);
            if (!shouldDeferAdWork()) {
                tryPaintGoogleNativeInPlace(page);
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Flip-back restore for an ad seat that still shows NativeAdView chrome.
     * @return true when the seat is healthy (video resumed or non-video creative OK);
     *         false when dead chrome was stripped and a fresh fill is required.
     */
    private boolean ensureLandedAdMediaAlive(int page) {
        try {
            if (list == null || page < 0 || page >= list.size()) {
                return false;
            }
            ModelClass.Datum slot = list.get(page);
            if (slot == null) {
                return false;
            }
            Integer needReqPos = slot.getAd_req_position();
            String adType = slot.getAd_type();
            boolean fbSeat = adType != null
                    && !adType.equalsIgnoreCase("google")
                    && !adType.equalsIgnoreCase("admanager");
            boolean livePacked = fbSeat
                    ? hasLiveFbNativePackedForReqPos(needReqPos)
                    : hasLiveGoogleNativePackedForReqPos(needReqPos);
            if (!livePacked) {
                // Flip-away destroy (or recycle) left zombie headline/CTA + black media.
                stripDeadNativeCreativeFromCurrentPage();
                showLog("AD flip-back: stripped dead creative, need fresh fill page=" + page
                        + " reqPos=" + needReqPos);
                return false;
            }
            if (fbSeat) {
                return true;
            }
            NativeAd live = findGoogleNativeForSlot(slot);
            if (live == null) {
                stripDeadNativeCreativeFromCurrentPage();
                return false;
            }
            // Seat-sticky keep: MediaView often loses its surface off-screen.
            // Resume video and re-register media so flip-back matches first show.
            return resumeGoogleNativeVideoOnCurrentPage(live);
        } catch (Exception e) {
            showLog("ensureLandedAdMediaAlive: " + e.getMessage());
            return false;
        }
    }

    /** Remove zombie NativeAdView / FB unit from the visible ad seat. */
    private void stripDeadNativeCreativeFromCurrentPage() {
        try {
            if (!(context instanceof HomePage)) {
                return;
            }
            View page = ((HomePage) context).getCurrentFlipPageView();
            if (page == null) {
                return;
            }
            View fl = page.findViewById(R.id.fl_adplaceholder);
            if (fl instanceof ViewGroup) {
                ViewGroup group = (ViewGroup) fl;
                for (int i = group.getChildCount() - 1; i >= 0; i--) {
                    View child = group.getChildAt(i);
                    if (child instanceof NativeAdView
                            || (child.getTag() != null
                            && "ad_dark_underlay".equals(String.valueOf(child.getTag())))) {
                        // Keep underlay; strip only NativeAdView. Re-add underlay after.
                        if (child instanceof NativeAdView) {
                            group.removeViewAt(i);
                        }
                    }
                }
                // Also drop nested native_ad_view shells.
                View nested = group.findViewById(R.id.native_ad_view);
                if (nested != null && nested.getParent() instanceof ViewGroup) {
                    ((ViewGroup) nested.getParent()).removeView(nested);
                }
                ensureDarkAdUnderlay(group);
                tagAdSeatReqPos(page, null);
            }
            NativeAdLayout fb = page.findViewById(R.id.native_ad_container);
            if (fb != null) {
                try {
                    View unit = fb.findViewById(R.id.ad_unit);
                    if (unit != null && unit.getParent() instanceof ViewGroup) {
                        ((ViewGroup) unit.getParent()).removeView(unit);
                    }
                } catch (Exception ignored) {
                }
                ensureDarkAdUnderlay(fb);
                tagAdSeatReqPos(page, null);
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Re-attach MediaView + play VideoController so flip-back video matches first show.
     * @return true if chrome is usable; false if rebind failed (caller should reload).
     */
    private boolean resumeGoogleNativeVideoOnCurrentPage(@NonNull NativeAd ad) {
        try {
            if (!(context instanceof HomePage)) {
                return true;
            }
            View page = ((HomePage) context).getCurrentFlipPageView();
            if (page == null) {
                return true;
            }
            NativeAdView adView = null;
            View fl = page.findViewById(R.id.fl_adplaceholder);
            if (fl instanceof ViewGroup) {
                ViewGroup group = (ViewGroup) fl;
                for (int i = 0; i < group.getChildCount(); i++) {
                    if (group.getChildAt(i) instanceof NativeAdView) {
                        adView = (NativeAdView) group.getChildAt(i);
                        break;
                    }
                }
            }
            if (adView == null) {
                View nav = page.findViewById(R.id.native_ad_view);
                if (nav instanceof NativeAdView) {
                    adView = (NativeAdView) nav;
                }
            }
            if (adView == null) {
                return false;
            }
            MediaView mediaView = adView.findViewById(R.id.ad_media);
            if (mediaView == null) {
                mediaView = adView.findViewById(R.id.media_view);
            }
            if (mediaView != null) {
                try {
                    mediaView.setVisibility(VISIBLE);
                    adView.setMediaView(mediaView);
                } catch (Exception ignored) {
                }
            }
            // Re-register the same NativeAd so MediaView rebuilds its surface.
            try {
                adView.setNativeAd(ad);
            } catch (Exception e) {
                showLog("resume setNativeAd failed: " + e.getMessage());
                return false;
            }
            try {
                MediaContent mediaContent = ad.getMediaContent();
                if (mediaContent != null && mediaContent.hasVideoContent()) {
                    VideoController vc = mediaContent.getVideoController();
                    if (vc != null) {
                        vc.play();
                        showLog("AD flip-back: VideoController.play()");
                    }
                }
            } catch (Exception ignored) {
            }
            return true;
        } catch (Exception e) {
            showLog("resumeGoogleNativeVideoOnCurrentPage: " + e.getMessage());
            return false;
        }
    }

    /**
     * Soft-hide lived only on the overlay-era adapter. July / Play Store path:
     * settle destroys claimed/impressed natives for seats the user left when
     * admin destroy toggle is ON so flip-back requests a fresh AdMob fill.
     */
    public void pauseAdMediaForLeaveGesture(int adPage) {
        // intentionally empty during gesture — orphan prune happens on settle
    }

    /**
     * After settle: destroy claimed/impressed creatives for seats that are not
     * the current page when admin destroy toggle is ON. OFF = seat-sticky keep.
     */
    public void releaseOffscreenAdMediaAfterSettle(int currentPage) {
        try {
            if (context instanceof HomePage) {
                ((HomePage) context).destroyOffscreenImpressedNatives(currentPage);
            }
        } catch (Exception ignored) {
        }
        // Late safety if settle runs after a flip. Primary cancel is
        // HomePage.onFlippedToPage (immediate, all card types, no network).
        try {
            Long postId = null;
            if (list != null && currentPage >= 0 && currentPage < list.size()
                    && list.get(currentPage) != null) {
                postId = list.get(currentPage).getPostId();
            }
            ArticleTtsHelper.stopIfNotCurrent(postId);
        } catch (Exception ignored) {
        }
    }

    /**
     * Top-right speaker on half-image cards when admin {@code tts_speaker_on_image}
     * (+ master {@code tts_enabled}) is ON. Speaks article body; pulse while speaking.
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
     * (+ master) is ON. Same TTS engine as image placement.
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

    private boolean isUserDrivingFlip() {
        try {
            if (context instanceof HomePage) {
                return ((HomePage) context).isNewsUserDrivingFlip();
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    /**
     * Dataset change that must not run while the user is swiping. Coalesced to
     * one recycle after the fling settles, so fast flips never snap to page 0.
     */
    public void notifyWhenFlipSettled() {
        try {
            if (isUserDrivingFlip()) {
                if (!pendingFlipDatasetNotify) {
                    pendingFlipDatasetNotify = true;
                    adShellHandler.postDelayed(deferredFlipDatasetNotify, 32L);
                }
                return;
            }
            pendingFlipDatasetNotify = false;
            adShellHandler.removeCallbacks(deferredFlipDatasetNotify);
            notifyDataSetChangedNow();
        } catch (Exception ignored) {
            try {
                notifyDataSetChangedNow();
            } catch (Exception ignored2) {
            }
        }
    }

    private boolean notifyDatasetBypass = false;

    /**
     * Soft-merge, ads, sticky, and likes used to call this mid-gesture. That
     * recycled FlipView and the next notify treated the seat as unknown (page 0).
     * While the user is driving, coalesce to {@link #notifyWhenFlipSettled()}.
     */
    @Override
    public void notifyDataSetChanged() {
        if (!notifyDatasetBypass && isUserDrivingFlip()) {
            notifyWhenFlipSettled();
            return;
        }
        super.notifyDataSetChanged();
    }

    private void notifyDataSetChangedNow() {
        notifyDatasetBypass = true;
        try {
            notifyDataSetChanged();
        } finally {
            notifyDatasetBypass = false;
        }
    }

    private boolean isHomeFlipAnimating() {
        try {
            if (context instanceof HomePage) {
                return ((HomePage) context).isNewsFlipAnimating();
            }
        } catch (Exception ignored) {
        }
        return FlipAdWorkGate.isFlipAnimating();
    }

    private boolean shouldDeferAdWork() {
        try {
            if (context instanceof HomePage) {
                return ((HomePage) context).shouldDeferAdWork();
            }
        } catch (Exception ignored) {
        }
        return FlipAdWorkGate.shouldDeferAdWork();
    }

    /** Admin destroy-after-impression (default OFF). Cached via FeedAdConfig. */
    private boolean isDestroyImpressedEnabled() {
        try {
            return FeedAdConfig.isDestroyAfterImpression(context);
        } catch (Exception ignored) {
            return FeedAdConfig.DEFAULT_DESTROY_AFTER_IMPRESSION;
        }
    }

    /**
     * Coalesce ad-fill rebinds and never run FlipView {@code dataSetChanged} while
     * the 3D flip is animating (that recycles pages mid-gesture → hitch).
     */
    public void scheduleSafeAdNotify(long delayMs) {
        try {
            adShellHandler.removeCallbacks(deferredSafeAdNotify);
            pendingSafeAdNotify = true;
            long wait = Math.max(0L, delayMs);
            if (shouldDeferAdWork()) {
                wait = Math.max(wait, FlipAdWorkGate.millisUntilAdWorkSafe());
                if (wait < 64L) {
                    wait = 64L;
                }
            }
            adShellHandler.postDelayed(deferredSafeAdNotify, wait);
        } catch (Exception ignored) {
            notifyWhenFlipSettled();
        }
    }

    /**
     * Paint filled Google natives into live empty shells without a full adapter
     * recycle. Returns true if at least one shell was painted.
     */
    public boolean tryPaintReadyGoogleNativesInPlace() {
        boolean painted = false;
        try {
            for (int i = adShellAwaitingCreative.size() - 1; i >= 0; i--) {
                int page = adShellAwaitingCreative.keyAt(i);
                if (tryPaintGoogleNativeInPlace(page)) {
                    painted = true;
                }
            }
            // Landed house placeholder even if awaiting was cleared by a prior miss.
            if (!painted && currentFlipPage >= 0
                    && isDefaultAdPlaceholderShowing(currentFlipPage)) {
                if (tryPaintGoogleNativeInPlace(currentFlipPage)) {
                    painted = true;
                }
            }
        } catch (Exception ignored) {
        }
        return painted;
    }

    /**
     * True when the <b>visible</b> Flip page is an ad seat that still needs a
     * creative paint (Share default / house, full share_app scrap, or empty
     * black {@code ads_screen}) while a native is ready. Only then may we allow
     * one FlipView settle recycle. Already-painted seats must return false so
     * AdPreloader / pool refill cannot black↔ad loop.
     */
    public boolean needsAdSeatDatasetRebind() {
        try {
            if (list == null || list.isEmpty()) {
                return false;
            }
            if (currentFlipPage < 0 || currentFlipPage >= list.size()) {
                return false;
            }
            ModelClass.Datum slot = list.get(currentFlipPage);
            if (slot == null || slot.getPostType() == null
                    || !slot.getPostType().equalsIgnoreCase("ad")) {
                return false;
            }
            if (!hasNativeCreativeForSlot(slot)) {
                return false;
            }
            if (isCurrentAdSeatShowingNativeCreative()) {
                return false;
            }
            return adShellAwaitingCreative.get(currentFlipPage, false)
                    || isDefaultAdPlaceholderShowing(currentFlipPage)
                    || isCurrentAdSeatShowingShareFallback()
                    || isCurrentAdSeatEmptyBlackShell();
        } catch (Exception ignored) {
        }
        return false;
    }

    /** Visible page already has a bound Google NativeAdView — do not recycle. */
    private boolean isCurrentAdSeatShowingNativeCreative() {
        try {
            if (!(context instanceof HomePage)) {
                return false;
            }
            View page = ((HomePage) context).getCurrentFlipPageView();
            if (page == null) {
                return false;
            }
            View fl = page.findViewById(R.id.fl_adplaceholder);
            if (!(fl instanceof ViewGroup)) {
                return false;
            }
            ViewGroup group = (ViewGroup) fl;
            for (int i = 0; i < group.getChildCount(); i++) {
                if (group.getChildAt(i) instanceof NativeAdView) {
                    return true;
                }
            }
            return page.findViewById(R.id.native_ad_view) instanceof NativeAdView
                    && ((NativeAdView) page.findViewById(R.id.native_ad_view))
                    .getParent() != null;
        } catch (Exception ignored) {
        }
        return false;
    }

    /**
     * Legacy scrap: seat type is {@code ad} but the visible page was inflated as
     * full {@code share_app_layout} (no fl_adplaceholder). Share-inside-ads_screen
     * (ad_default_placeholder) is the intentional empty-seat default — not this.
     */
    private boolean isCurrentAdSeatShowingShareFallback() {
        try {
            if (!(context instanceof HomePage)) {
                return false;
            }
            View page = ((HomePage) context).getCurrentFlipPageView();
            if (page == null) {
                return false;
            }
            return page.findViewById(R.id.imageViewWhatsapp) != null
                    && page.findViewById(R.id.fl_adplaceholder) == null;
        } catch (Exception ignored) {
        }
        return false;
    }

    /**
     * Remove any rotating ProgressBar from an ad container immediately.
     * Ad seats never show a spinner — Share default or static dark house only.
     */
    private void stripAdSeatSpinners(@Nullable ViewGroup container) {
        if (container == null) {
            return;
        }
        try {
            for (int i = container.getChildCount() - 1; i >= 0; i--) {
                View child = container.getChildAt(i);
                if (child instanceof ProgressBar) {
                    container.removeViewAt(i);
                }
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Inflated {@code ads_screen} with empty placeholder (legacy ProgressBar-only
     * or zero children) — house shell should already be mounted; treat as needs paint.
     */
    private boolean isCurrentAdSeatEmptyBlackShell() {
        try {
            if (!(context instanceof HomePage)) {
                return false;
            }
            View page = ((HomePage) context).getCurrentFlipPageView();
            if (page == null) {
                return false;
            }
            View fl = page.findViewById(R.id.fl_adplaceholder);
            if (!(fl instanceof ViewGroup)) {
                return false;
            }
            ViewGroup group = (ViewGroup) fl;
            stripAdSeatSpinners(group);
            return group.getChildCount() == 0;
        } catch (Exception ignored) {
        }
        return false;
    }

    /** Visible Flip page is an ad still on house/share default (fill pending). */
    private boolean isParkedOnUnfilledAdSeat() {
        try {
            if (list == null || list.isEmpty()) {
                return false;
            }
            if (currentFlipPage < 0 || currentFlipPage >= list.size()) {
                return false;
            }
            ModelClass.Datum slot = list.get(currentFlipPage);
            if (slot == null || slot.getPostType() == null
                    || !slot.getPostType().equalsIgnoreCase("ad")) {
                return false;
            }
            return adShellAwaitingCreative.get(currentFlipPage, false)
                    || isDefaultAdPlaceholderShowing(currentFlipPage);
        } catch (Exception ignored) {
        }
        return false;
    }

    private boolean isDefaultAdPlaceholderShowing(int position) {
        try {
            FrameLayout fl = resolveAdShellFrame(position);
            if (fl == null) {
                return adShellAwaitingCreative.get(position, false);
            }
            return fl.findViewById(R.id.ad_default_placeholder_root) != null;
        } catch (Exception ignored) {
            return adShellAwaitingCreative.get(position, false);
        }
    }

    @Nullable
    private FrameLayout resolveAdShellFrame(int position) {
        try {
            java.lang.ref.WeakReference<FrameLayout> ref = adShellPlaceholders.get(position);
            FrameLayout fl = ref != null ? ref.get() : null;
            if (fl != null && fl.isAttachedToWindow()) {
                return fl;
            }
            // Weak ref lost — re-find live fl_adplaceholder without full notify.
            if (position == currentFlipPage && context instanceof HomePage) {
                View page = ((HomePage) context).getCurrentFlipPageView();
                if (page != null) {
                    View found = page.findViewById(R.id.fl_adplaceholder);
                    if (found instanceof FrameLayout && found.isAttachedToWindow()) {
                        Integer fromBoxed = adShellFromViewTypes.get(position);
                        int fromView = fromBoxed != null ? fromBoxed : 2;
                        rememberAdShellPlaceholder(position, (FrameLayout) found, fromView);
                        // Do NOT re-arm awaiting when MediaView chrome is already live —
                        // that caused a second setNativeAd and mute/pause blinked once.
                        if (!placeholderHasNativeCreative((FrameLayout) found)) {
                            adShellAwaitingCreative.put(position, true);
                        }
                        return (FrameLayout) found;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private boolean tryPaintGoogleNativeInPlace(int position) {
        try {
            if (list == null || position < 0 || position >= list.size()) {
                return false;
            }
            ModelClass.Datum slot = list.get(position);
            if (slot == null || slot.getPostType() == null
                    || !slot.getPostType().equalsIgnoreCase("ad")) {
                return false;
            }
            FrameLayout fl = resolveAdShellFrame(position);
            if (fl == null || !fl.isAttachedToWindow()) {
                return false;
            }
            View convertView = fl;
            try {
                if (fl.getParent() instanceof View) {
                    convertView = (View) fl.getParent();
                    while (convertView.getParent() instanceof View
                            && convertView.findViewById(R.id.fl_adplaceholder) == null) {
                        convertView = (View) convertView.getParent();
                    }
                    if (convertView.findViewById(R.id.fl_adplaceholder) == null) {
                        convertView = fl;
                    }
                }
            } catch (Exception ignored) {
                convertView = fl;
            }
            Integer needReqPos = slot.getAd_req_position();
            // Keep live MediaView only while a packed creative still exists for this
            // seat. After flip-away destroy, scrap may still hold a dead NativeAdView —
            // do not treat that as filled (July fresh-fill on return).
            if (placeholderHasNativeCreative(fl)
                    && isPaintedAdSeatForReqPos(convertView, needReqPos)
                    && hasLiveGoogleNativePackedForReqPos(needReqPos)) {
                // Flip-back: resume video / re-register MediaView so black surface recovers.
                if (position == currentFlipPage) {
                    NativeAd live = findGoogleNativeForSlot(slot);
                    if (live != null) {
                        resumeGoogleNativeVideoOnCurrentPage(live);
                    }
                }
                adShellAwaitingCreative.delete(position);
                cancelAdShellFillTimeout(position);
                if (position == parkedAdFillPage) {
                    cancelParkedAdFillWatchdog();
                }
                return true;
            }
            // Zombie chrome without a live packed ad — strip so house + reload can paint.
            if (placeholderHasNativeCreative(fl)
                    && !hasLiveGoogleNativePackedForReqPos(needReqPos)
                    && position == currentFlipPage) {
                stripDeadNativeCreativeFromCurrentPage();
            }
            NativeAd ad = findGoogleNativeForSlot(slot);
            if (ad == null) {
                return false;
            }
            Integer fromBoxed = adShellFromViewTypes.get(position);
            int fromView = fromBoxed != null ? fromBoxed : 2;
            View myView;
            if (fromView == 0) {
                myView = LayoutInflater.from(context).inflate(
                        R.layout.anynews_ad_full_template_view, fl, false);
            } else if (fromView == 1) {
                myView = LayoutInflater.from(context).inflate(
                        R.layout.anynews_ad_medium_template_view, fl, false);
            } else {
                myView = LayoutInflater.from(context).inflate(
                        R.layout.custom_ad_native, fl, false);
            }
            paintGoogleNativeIntoSeat(fl, ad, myView, fromView, convertView, position);
            return true;
        } catch (Exception e) {
            showLog("tryPaintGoogleNativeInPlace: " + e.getMessage());
            return false;
        }
    }

    private void rememberAdShellPlaceholder(int position, @Nullable FrameLayout fl, int fromView) {
        try {
            if (fl == null) {
                adShellPlaceholders.remove(position);
                adShellFromViewTypes.remove(position);
                return;
            }
            adShellPlaceholders.put(position, new java.lang.ref.WeakReference<>(fl));
            adShellFromViewTypes.put(position, fromView);
        } catch (Exception ignored) {
        }
    }

    private void clearAdShellPlaceholder(int position) {
        try {
            adShellPlaceholders.remove(position);
            adShellFromViewTypes.remove(position);
        } catch (Exception ignored) {
        }
    }

    private void cancelAdShellFillTimeout(int position) {
        try {
            Runnable r = adShellTimeoutRunnables.get(position);
            if (r != null) {
                adShellHandler.removeCallbacks(r);
                adShellTimeoutRunnables.remove(position);
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Drop deferred native MediaView attach for a page when the seat is no
     * longer an ad shell (removed / replaced) so a late bind cannot paint wrong UI.
     */
    public void clearPendingNativeAdBind(int page) {
        try {
            pendingNativeAdBinds.remove(page);
            adShellAwaitingCreative.delete(page);
            clearAdShellPlaceholder(page);
            cancelAdShellFillTimeout(page);
        } catch (Exception ignored) {
        }
    }

    private void cancelAllAdShellFillTimeouts() {
        try {
            for (int i = 0; i < adShellTimeoutRunnables.size(); i++) {
                Runnable r = adShellTimeoutRunnables.valueAt(i);
                if (r != null) {
                    adShellHandler.removeCallbacks(r);
                }
            }
            adShellTimeoutRunnables.clear();
        } catch (Exception ignored) {
        }
    }

    /**
     * After {@link #AD_SHELL_FILL_TIMEOUT_MS} without a native creative on the
     * <b>currently visible</b> flip index, ask HomePage to keep the house placeholder
     * (and optionally retry load). Buffer / peek pages must NOT arm this — FlipView
     * binds next ad early. Never demote to permanent {@code share_app}.
     */
    private void scheduleAdShellFillTimeout(int position) {
        try {
            // Only the landed page may time out. Buffer binds keep default placeholder.
            if (position != currentFlipPage) {
                cancelAdShellFillTimeout(position);
                showLog("AD shell timeout skip (buffer) pos=" + position
                        + " currentFlipPage=" + currentFlipPage);
                return;
            }
            cancelAdShellFillTimeout(position);
            final int pos = position;
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    try {
                        adShellTimeoutRunnables.remove(pos);
                        // User may have flipped away — do not touch an off-screen buffer seat.
                        if (pos != currentFlipPage) {
                            showLog("AD shell timeout abort (left page) pos=" + pos
                                    + " currentFlipPage=" + currentFlipPage);
                            return;
                        }
                        // Creative may have arrived after deadline arming — or a ready
                        // pool ad can be claimed for this seat.
                        if (list != null && pos >= 0 && pos < list.size()) {
                            ModelClass.Datum slot = list.get(pos);
                            if (slot != null
                                    && slot.getPostType() != null
                                    && slot.getPostType().equalsIgnoreCase("ad")
                                    && hasNativeCreativeForSlot(slot)) {
                                cancelAdShellFillTimeout(pos);
                                if (tryPaintGoogleNativeInPlace(pos)) {
                                    showLog("AD shell timeout averted via pool/exact pos=" + pos);
                                    if (context instanceof HomePage) {
                                        try {
                                            ((HomePage) context).refillNativeAdPoolAfterConsume();
                                        } catch (Exception ignored) {
                                        }
                                    }
                                    return;
                                }
                                scheduleSafeAdNotify(0L);
                                return;
                            }
                            if (slot == null
                                    || slot.getPostType() == null
                                    || !slot.getPostType().equalsIgnoreCase("ad")) {
                                return;
                            }
                        }
                        showLog("AD shell fill timeout → keep Share default + retry pos=" + pos
                                + " currentFlipPage=" + currentFlipPage);
                        NativeAdCacheCoordinator.get().onHouseFallback(pos);
                        if (context instanceof HomePage) {
                            ((HomePage) context).onUnfilledAdShellTimedOut(pos);
                        }
                        // Re-arm soft timeout so fill can still arrive later in-session.
                        if (pos == currentFlipPage
                                && list != null && pos >= 0 && pos < list.size()) {
                            ModelClass.Datum still = list.get(pos);
                            if (still != null && still.getPostType() != null
                                    && still.getPostType().equalsIgnoreCase("ad")
                                    && (adShellAwaitingCreative.get(pos, false)
                                    || isDefaultAdPlaceholderShowing(pos))) {
                                scheduleAdShellFillTimeout(pos);
                                armParkedAdFillWatchdog(pos);
                            }
                        }
                    } catch (Exception e) {
                        showLog("ad shell timeout: " + e.getMessage());
                    }
                }
            };
            adShellTimeoutRunnables.put(pos, r);
            adShellHandler.postDelayed(r, AD_SHELL_FILL_TIMEOUT_MS);
        } catch (Exception ignored) {
        }
    }

    /**
     * Share-with-friends default while native is still loading or failed.
     * Do not mutate the list seat to {@code share_app} — that caused Share→ad
     * flash and blocked late fills. Seat stays {@code ad}; soft timeout keeps
     * Share chrome and retries load — never permanent demotion.
     * Always paints the Share default so emulator/no-fill never leaves blank white.
     */
    private void mountAdPendingEmptyShell(@Nullable FrameLayout fl, int position) {
        mountAdPendingEmptyShell(fl, position, 2);
    }

    private void mountAdPendingEmptyShell(@Nullable FrameLayout fl, int position, int fromView) {
        if (fl == null) {
            return;
        }
        try {
            ViewGroup.LayoutParams flLp = fl.getLayoutParams();
            if (flLp != null) {
                flLp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                flLp.height = ViewGroup.LayoutParams.MATCH_PARENT;
                fl.setLayoutParams(flLp);
            }
            fl.setVisibility(VISIBLE);
            mountDefaultAdPlaceholder(fl);
            adShellAwaitingCreative.put(position, true);
            rememberAdShellPlaceholder(position, fl, fromView);
            // Arm retry / parked fill only when this seat is on-screen.
            if (position == currentFlipPage) {
                scheduleAdShellFillTimeout(position);
                armParkedAdFillWatchdog(position);
            } else {
                cancelAdShellFillTimeout(position);
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * While parked on a house/default ad seat, keep trying in-place paint as
     * creatives arrive (July/Sep-7 wait-on-seat behavior) without blinking news.
     */
    private void armParkedAdFillWatchdog(int position) {
        try {
            cancelParkedAdFillWatchdog();
            if (position != currentFlipPage) {
                return;
            }
            parkedAdFillPage = position;
            parkedAdFillTicks = 0;
            parkedAdFillWatchdog =
                    new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (parkedAdFillPage != currentFlipPage
                                        || parkedAdFillPage < 0) {
                                    cancelParkedAdFillWatchdog();
                                    return;
                                }
                                int page = parkedAdFillPage;
                                if (list == null || page < 0 || page >= list.size()) {
                                    cancelParkedAdFillWatchdog();
                                    return;
                                }
                                ModelClass.Datum slot = list.get(page);
                                if (slot == null
                                        || slot.getPostType() == null
                                        || !slot.getPostType().equalsIgnoreCase("ad")) {
                                    cancelParkedAdFillWatchdog();
                                    return;
                                }
                                if (!adShellAwaitingCreative.get(page, false)
                                        && !isDefaultAdPlaceholderShowing(page)) {
                                    cancelParkedAdFillWatchdog();
                                    return;
                                }
                                if (shouldDeferAdWork() || isHomeFlipAnimating()) {
                                    adShellHandler.postDelayed(this, PARKED_AD_FILL_POLL_MS);
                                    return;
                                }
                                // Always re-ensure while parked — orphan in-flight
                                // markers / mid-flip warm caps used to leave house forever.
                                requestNativeAdLoadForSlot(slot);
                                if (hasNativeCreativeForSlot(slot)) {
                                    if (tryPaintGoogleNativeInPlace(page)) {
                                        showLog("ANYNEWS_ADS parked watchdog painted pos=" + page);
                                        cancelParkedAdFillWatchdog();
                                        return;
                                    }
                                }
                                parkedAdFillTicks++;
                                if (parkedAdFillTicks >= PARKED_AD_FILL_MAX_TICKS) {
                                    // Cool down then keep polling — never stop for the session.
                                    parkedAdFillTicks = 0;
                                    showLog("ANYNEWS_ADS parked watchdog recover cool-down pos="
                                            + page);
                                    adShellHandler.postDelayed(this, PARKED_AD_FILL_COOLDOWN_MS);
                                    return;
                                }
                                adShellHandler.postDelayed(this, PARKED_AD_FILL_POLL_MS);
                            } catch (Exception e) {
                                cancelParkedAdFillWatchdog();
                            }
                        }
                    };
            adShellHandler.postDelayed(parkedAdFillWatchdog, PARKED_AD_FILL_FIRST_MS);
        } catch (Exception ignored) {
        }
    }

    private void cancelParkedAdFillWatchdog() {
        try {
            if (parkedAdFillWatchdog != null) {
                adShellHandler.removeCallbacks(parkedAdFillWatchdog);
            }
        } catch (Exception ignored) {
        }
        parkedAdFillWatchdog = null;
        parkedAdFillPage = -1;
        parkedAdFillTicks = 0;
    }

    /**
     * Paint Share-with-friends default into any ad container (Google FrameLayout
     * or FB NativeAdLayout). Overlay-without-flash: never {@code removeAllViews()}
     * first (that left an empty black void for 1–2 frames). Dark underlay stays
     * until Share is on top; prior children stripped on the next frame. When a
     * native fills, creative overlays Share without a black flash.
     * Never mounts a ProgressBar / rotating spinner on ad seats.
     */
    private void mountDefaultAdPlaceholder(@Nullable ViewGroup container) {
        if (container == null) {
            return;
        }
        try {
            stripAdSeatSpinners(container);
            container.setBackgroundColor(0xFF121212);
            container.setVisibility(VISIBLE);
            View existing = container.findViewById(R.id.ad_default_placeholder_root);
            if (existing != null) {
                showShareScreen(existing);
                try {
                    existing.bringToFront();
                } catch (Exception ignored) {
                }
                return;
            }
            ensureDarkAdUnderlay(container);
            View placeholder = LayoutInflater.from(context).inflate(
                    R.layout.ad_default_placeholder, container, false);
            ViewGroup.LayoutParams lp = placeholder.getLayoutParams();
            if (lp == null) {
                lp = new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
                placeholder.setLayoutParams(lp);
            } else {
                lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
                placeholder.setLayoutParams(lp);
            }
            container.addView(placeholder);
            showShareScreen(placeholder);
            final View keep = placeholder;
            container.post(() -> {
                try {
                    for (int i = container.getChildCount() - 1; i >= 0; i--) {
                        View child = container.getChildAt(i);
                        if (child != keep) {
                            container.removeViewAt(i);
                        }
                    }
                } catch (Exception ignored) {
                }
            });
        } catch (Exception e) {
            showLog("mountDefaultAdPlaceholder: " + e.getMessage());
            try {
                // Absolute last resort: solid dark plate (never white / empty black).
                container.setBackgroundColor(0xFF121212);
            } catch (Exception ignored) {
            }
        }
    }

    /** FB / generic container path: Share default + timeout / parked-fill bookkeeping. */
    private void mountFbAdPendingDefaultShell(
            @Nullable NativeAdLayout nativeAdLayout, int position) {
        if (nativeAdLayout == null) {
            return;
        }
        try {
            mountDefaultAdPlaceholder(nativeAdLayout);
            adShellAwaitingCreative.put(position, true);
            if (position == currentFlipPage) {
                scheduleAdShellFillTimeout(position);
                armParkedAdFillWatchdog(position);
            } else {
                cancelAdShellFillTimeout(position);
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Bind Google native (or dark house) into an existing {@code fl_adplaceholder}
     * without reinflating {@code ads_screen}. Used by scrap reuse and fresh inflate.
     */
    private void bindGoogleAdIntoPlaceholder(
            @NonNull FrameLayout fl_adplaceholder,
            @NonNull View convertView,
            int position) {
        ModelClass.Datum googleSlot = list.get(position);
        Integer needReqPos = googleSlot != null ? googleSlot.getAd_req_position() : null;
        // Keep live MediaView on the landed seat only while the packed creative is
        // still in the pool — avoids AdMob mute blink. After flip-away destroy there
        // is no live packed ad → fall through to house + fresh request (July path).
        if (placeholderHasNativeCreative(fl_adplaceholder)
                && isPaintedAdSeatForReqPos(convertView, needReqPos)
                && position == currentFlipPage
                && hasLiveGoogleNativePackedForReqPos(needReqPos)) {
            NativeAd liveKeep = findGoogleNativeForSlot(googleSlot);
            if (liveKeep != null) {
                resumeGoogleNativeVideoOnCurrentPage(liveKeep);
            }
            rememberAdShellPlaceholder(position, fl_adplaceholder, 2);
            adShellAwaitingCreative.delete(position);
            cancelAdShellFillTimeout(position);
            return;
        }
        // Destroy-after-impression / recycle: scrap still has headline+CTA but no
        // live packed NativeAd — strip zombie so house shell + fresh AdLoader run.
        if (placeholderHasNativeCreative(fl_adplaceholder)
                && position == currentFlipPage
                && !hasLiveGoogleNativePackedForReqPos(needReqPos)) {
            try {
                for (int i = fl_adplaceholder.getChildCount() - 1; i >= 0; i--) {
                    View child = fl_adplaceholder.getChildAt(i);
                    if (child instanceof NativeAdView) {
                        fl_adplaceholder.removeViewAt(i);
                    }
                }
                View nested = fl_adplaceholder.findViewById(R.id.native_ad_view);
                if (nested != null && nested.getParent() instanceof ViewGroup) {
                    ((ViewGroup) nested.getParent()).removeView(nested);
                }
                ensureDarkAdUnderlay(fl_adplaceholder);
                tagAdSeatReqPos(convertView, null);
            } catch (Exception ignored) {
            }
        }
        int fromView = 2;
        View myView;
        String adViewType = googleSlot.getAd_view_type();
        if (adViewType != null
                && adViewType.equalsIgnoreCase(Constants.AD_FULL_IMAGE)) {
            fromView = 0;
            myView = LayoutInflater.from(context).inflate(
                    R.layout.anynews_ad_full_template_view, null);
        } else if (adViewType != null
                && adViewType.equalsIgnoreCase(Constants.AD_MEDIUM)) {
            fromView = 1;
            myView = LayoutInflater.from(context).inflate(
                    R.layout.anynews_ad_medium_template_view, null);
        } else {
            fromView = 2;
            myView = LayoutInflater.from(context).inflate(
                    R.layout.custom_ad_native, null);
        }
        // Buffer binds: exact match only — do not steal AdPreloader /
        // ahead pool from the seat the user is about to land on.
        NativeAd ad = (position == currentFlipPage)
                ? findGoogleNativeForSlot(googleSlot)
                : peekExactGoogleNativeForSlot(googleSlot);
        // Only the landed seat may start a network load from getView.
        // Ahead buffer binds used to request without ever impressing.
        // Mid-gesture / settle-grace: never AdLoader (HomePage settle owns it).
        if (position == currentFlipPage) {
            requestNativeAdLoadForSlot(googleSlot);
        }

        if (ad != null) {
            // Never attach NativeAd / MediaView mid-flip — hitch + white blink.
            // Buffer pages and in-flight land both wait for settle bind.
            if (position != currentFlipPage
                    || isHomeFlipAnimating()
                    || shouldDeferAdWork()) {
                showLog("Defer native MediaView bind for ad page=" + position
                        + (position == currentFlipPage ? " (land-wait)" : " (buffer)"));
                pendingNativeAdBinds.put(position, new PendingNativeAdBind(
                        fl_adplaceholder, ad, myView, fromView, convertView));
                // Keep live paint if already showing this seat; else house until settle.
                if (!(placeholderHasNativeCreative(fl_adplaceholder)
                        && isPaintedAdSeatForReqPos(convertView, needReqPos)
                        && hasLiveGoogleNativePackedForReqPos(needReqPos))) {
                    mountAdPendingEmptyShell(fl_adplaceholder, position, fromView);
                }
            } else {
                showLog("HomePage.nativeAdList displaying native ad (landed):");
                showLog("fromView:;" + fromView);
                ensureDarkAdUnderlay(fl_adplaceholder);
                paintGoogleNativeIntoSeat(
                        fl_adplaceholder, ad, myView, fromView, convertView, position);
            }
        } else {
            showLog("AD seat pending fill (house shell) pos=" + position
                    + " reqPos=" + googleSlot.getAd_req_position());
            tagAdSeatReqPos(convertView, null);
            mountAdPendingEmptyShell(fl_adplaceholder, position, fromView);
        }
    }

    /**
     * Bind FB native (or dark house) into an existing NativeAdLayout.
     * Never {@code addView} the creative shell before house/underlay —
     * overlay attach only when filling.
     */
    private void bindFacebookAdIntoLayout(
            @NonNull NativeAdLayout nativeAdLayout,
            @NonNull View convertView,
            int position) {
        nativeAdLayout.setBackgroundColor(0xFF121212);
        ModelClass.Datum fbSlot = list.get(position);
        Integer fbNeedReq = fbSlot != null ? fbSlot.getAd_req_position() : null;
        // Keep painted FB chrome only while a live packed creative exists for
        // THIS seat reqPos. Scrap recycle without the paint tag used to keep
        // seat A's NativeAdLayout on seat B (unpaid / wrong creative).
        try {
            View unit = nativeAdLayout.findViewById(R.id.ad_unit);
            if (unit != null
                    && nativeAdLayout.findViewById(R.id.ad_default_placeholder_root) == null
                    && isPaintedAdSeatForReqPos(convertView, fbNeedReq)
                    && position == currentFlipPage
                    && hasLiveFbNativePackedForReqPos(fbNeedReq)) {
                adShellAwaitingCreative.delete(position);
                return;
            }
        } catch (Exception ignored) {
        }
        LayoutInflater fbInflater = LayoutInflater.from(context);
        RelativeLayout adView = (RelativeLayout) fbInflater.inflate(
                R.layout.custom_fb_ad, nativeAdLayout, false);
        adView.setBackgroundColor(0xFF121212);
        fbAd = findFbNativeForSlot(fbSlot);
        if (position == currentFlipPage) {
            requestNativeAdLoadForSlot(fbSlot);
        }
        if (fbAd != null) {
            showLog(" fb ad :");
            if (position != currentFlipPage
                    || isHomeFlipAnimating()
                    || shouldDeferAdWork()) {
                showLog("Defer FB native MediaView bind for ad page=" + position
                        + (position == currentFlipPage ? " (land-wait)" : " (buffer)"));
                pendingNativeAdBinds.put(position, new PendingNativeAdBind(
                        convertView, fbAd, adView, nativeAdLayout));
                try {
                    View unit = nativeAdLayout.findViewById(R.id.ad_unit);
                    boolean live = unit != null
                            && nativeAdLayout.findViewById(R.id.ad_default_placeholder_root)
                            == null
                            && isPaintedAdSeatForReqPos(convertView, fbNeedReq)
                            && hasLiveFbNativePackedForReqPos(fbNeedReq);
                    if (!live) {
                        tagAdSeatReqPos(convertView, null);
                        mountFbAdPendingDefaultShell(nativeAdLayout, position);
                    }
                } catch (Exception ignored) {
                    tagAdSeatReqPos(convertView, null);
                    mountFbAdPendingDefaultShell(nativeAdLayout, position);
                }
            } else {
                ensureDarkAdUnderlay(nativeAdLayout);
                tagAdSeatReqPos(convertView, fbNeedReq);
                attachNativeAdViewWithoutFlash(nativeAdLayout, adView);
                displayFBNativeAd(convertView, fbAd, adView, nativeAdLayout);
                markFbNativeClaimed(fbAd);
                adShellAwaitingCreative.delete(position);
            }
        } else {
            showLog(" fb ad pending fill (house shell) pos=" + position);
            tagAdSeatReqPos(convertView, null);
            mountFbAdPendingDefaultShell(nativeAdLayout, position);
        }
    }

    /**
     * Paint a loaded Google native into the seat (current or FlipView buffer page).
     * Buffer pages must show the creative before the flip gesture so the animation
     * already has the ad texture — not share_app.
     */
    private void paintGoogleNativeIntoSeat(
            @NonNull FrameLayout fl_adplaceholder,
            @NonNull NativeAd ad,
            @NonNull View myView,
            int fromView,
            @NonNull View convertView,
            int position) {
        cancelAdShellFillTimeout(position);
        adShellAwaitingCreative.delete(position);
        pendingNativeAdBinds.remove(position);
        clearAdShellPlaceholder(position);
        // Safety: never attach MediaView mid-flip even if a caller raced settle.
        if (isHomeFlipAnimating() || shouldDeferAdWork()) {
            pendingNativeAdBinds.put(position, new PendingNativeAdBind(
                    fl_adplaceholder, ad, myView, fromView, convertView));
            return;
        }
        if (position == parkedAdFillPage) {
            cancelParkedAdFillWatchdog();
        }
        markGoogleNativeClaimed(ad);
        notePaintedGoogleNative(ad);
        try {
            Integer paintedReq = null;
            try {
                if (list != null && position >= 0 && position < list.size()
                        && list.get(position) != null) {
                    paintedReq = list.get(position).getAd_req_position();
                }
            } catch (Exception ignored) {
            }
            tagAdSeatReqPos(convertView, paintedReq);
            if (fromView == 0) {
                displayGoogleFullNativeAd(fl_adplaceholder, ad, myView, convertView, position);
            } else if (fromView == 1) {
                displayGoogleMediumNativeAd(fl_adplaceholder, ad, myView, convertView, position);
            } else {
                displayGoogleNativeAd(fl_adplaceholder, ad, myView, convertView, position);
            }
            // Never leave last creative on the field — July unpaid same-ad fallback path.
            nativeAd = null;
        } catch (Exception displayEx) {
            showLog("native ad display failed → default placeholder: " + displayEx.getMessage());
            mountAdPendingEmptyShell(fl_adplaceholder, position, fromView);
        }
    }

    /**
     * Opaque dark plate so {@code ads_screen}/{@code fl_adplaceholder} is never an
     * empty black void before house or creative paints. Strips any ProgressBar
     * first — spinner is never an acceptable wait UI on ad seats.
     */
    private void ensureDarkAdUnderlay(@Nullable ViewGroup container) {
        if (container == null) {
            return;
        }
        try {
            stripAdSeatSpinners(container);
            container.setBackgroundColor(0xFF121212);
            container.setVisibility(VISIBLE);
            try {
                ViewGroup.LayoutParams flLp = container.getLayoutParams();
                if (flLp != null) {
                    flLp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    flLp.height = ViewGroup.LayoutParams.MATCH_PARENT;
                    container.setLayoutParams(flLp);
                }
            } catch (Exception ignored) {
            }
            if (container.getChildCount() > 0) {
                return;
            }
            View plate = new View(context);
            plate.setBackgroundColor(0xFF121212);
            plate.setTag("ad_dark_underlay");
            container.addView(plate, new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
        } catch (Exception ignored) {
        }
    }

    /**
     * Attach a prepared NativeAdView without blanking the seat.
     * July {@code removeAllViews()} then {@code addView()} left
     * {@code ads_screen}/{@code fl_adplaceholder} empty (black) for 1–2 frames
     * while MediaView initialized — the on-land flash. Keep house/dark underlay
     * underneath, add creative on top, then strip underlay after the next frame.
     */
    private void attachNativeAdViewWithoutFlash(@Nullable ViewGroup parent, @Nullable View adView) {
        if (parent == null || adView == null) {
            return;
        }
        try {
            ensureDarkAdUnderlay(parent);
            if (adView.getParent() instanceof ViewGroup) {
                ((ViewGroup) adView.getParent()).removeView(adView);
            }
            // July chrome: keep NativeAdView / title-footer surfaces from XML.
            // Only darken MediaView / blur plate so texture init isn't a white/gray flash.
            try {
                View media = adView.findViewById(R.id.ad_media);
                if (media == null) {
                    media = adView.findViewById(R.id.media_view);
                }
                if (media != null) {
                    media.setBackgroundColor(0xFF121212);
                }
                View blur = adView.findViewById(R.id.iv_ad_view_blur);
                if (blur != null) {
                    blur.setBackgroundColor(0xFF121212);
                    if (blur instanceof ImageView
                            && ((ImageView) blur).getDrawable() == null) {
                        ((ImageView) blur).setImageDrawable(
                                new android.graphics.drawable.ColorDrawable(0xFF121212));
                    }
                }
                // Full-bleed templates (no rl_btm / medium primary): dark root is fine.
                if (adView.findViewById(R.id.rl_btm) == null
                        && adView.findViewById(R.id.primary) == null) {
                    adView.setBackgroundColor(0xFF121212);
                }
            } catch (Exception ignored) {
            }
            ViewGroup.LayoutParams lp = adView.getLayoutParams();
            if (lp == null) {
                lp = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
            } else {
                lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
            }
            parent.addView(adView, lp);
            final View keep = adView;
            parent.post(() -> {
                try {
                    for (int i = parent.getChildCount() - 1; i >= 0; i--) {
                        View child = parent.getChildAt(i);
                        if (child != keep) {
                            parent.removeViewAt(i);
                        }
                    }
                } catch (Exception ignored) {
                }
            });
        } catch (Exception e) {
            showLog("attachNativeAdViewWithoutFlash: " + e.getMessage());
            try {
                // Still prefer overlay if possible; never leave a void frame.
                parent.setBackgroundColor(0xFF121212);
                ensureDarkAdUnderlay(parent);
                if (adView.getParent() instanceof ViewGroup) {
                    ((ViewGroup) adView.getParent()).removeView(adView);
                }
                parent.addView(adView);
            } catch (Exception ignored) {
            }
        }
    }

    /** True when fl_adplaceholder already hosts a bound Google NativeAdView. */
    private static boolean placeholderHasNativeCreative(@Nullable ViewGroup fl) {
        if (fl == null) {
            return false;
        }
        try {
            for (int i = 0; i < fl.getChildCount(); i++) {
                if (fl.getChildAt(i) instanceof NativeAdView) {
                    return true;
                }
            }
            View nav = fl.findViewById(R.id.native_ad_view);
            return nav instanceof NativeAdView && nav.getParent() != null;
        } catch (Exception ignored) {
        }
        return false;
    }

    /** Record which feed ad_req_position was painted into this Flip scrap. */
    private void tagAdSeatReqPos(@Nullable View root, @Nullable Integer reqPos) {
        if (root == null) {
            return;
        }
        try {
            root.setTag(R.id.tag_ad_seat_req_pos, reqPos);
        } catch (Exception ignored) {
        }
    }

    /**
     * Scrap paint tag is only valid for the same seat reqPos. FlipView recycling used
     * to keep seat A's NativeAdView on seat B (= same creative everywhere, unpaid reuse).
     */
    private boolean isPaintedAdSeatForReqPos(@Nullable View root, @Nullable Integer needReqPos) {
        if (root == null || needReqPos == null) {
            return false;
        }
        try {
            Object tagged = root.getTag(R.id.tag_ad_seat_req_pos);
            return tagged instanceof Integer && needReqPos.equals(tagged);
        } catch (Exception ignored) {
        }
        return false;
    }

    /**
     * True when HomePage still holds a live (non-destroyed) Google {@link NativeAd}
     * packed for this reqPos. After flip-away destroy the scrap may still show a
     * detached NativeAdView — that must not count as a filled seat.
     */
    private boolean hasLiveGoogleNativePackedForReqPos(@Nullable Integer reqPos) {
        if (reqPos == null || HomePage.nativeGoogleAdList == null) {
            return false;
        }
        try {
            for (int i = 0; i < HomePage.nativeGoogleAdList.size(); i++) {
                FlipGoogleNativeAds packed = HomePage.nativeGoogleAdList.get(i);
                if (packed != null && packed.getAd() != null
                        && packed.getReqPos() == reqPos) {
                    return true;
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    /** Live FB native still packed for this reqPos (not destroyed on flip-away). */
    private boolean hasLiveFbNativePackedForReqPos(@Nullable Integer reqPos) {
        if (reqPos == null || HomePage.nativeFBAdList == null) {
            return false;
        }
        try {
            for (int i = 0; i < HomePage.nativeFBAdList.size(); i++) {
                FlipGoogleNativeAds packed = HomePage.nativeFBAdList.get(i);
                if (packed != null && packed.getFbAd() != null
                        && packed.getReqPos() == reqPos) {
                    return true;
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    /** Attach deferred native MediaView when user lands on an ad page. */
    public void bindPendingNativeAdMediaForPage(int page) {
        PendingNativeAdBind pending = pendingNativeAdBinds.get(page);
        if (pending == null) {
            return;
        }
        pendingNativeAdBinds.remove(page);
        cancelAdShellFillTimeout(page);
        adShellAwaitingCreative.delete(page);
        try {
            if (pending.facebook) {
                if (pending.fbAd != null && pending.convertView != null
                        && pending.fbAdView != null && pending.fbNativeAdLayout != null) {
                    // Overlay FB ad on house shell — do not blank the container first.
                    try {
                        pending.fbNativeAdLayout.setBackgroundColor(0xFF121212);
                        if (pending.fbAdView.getParent() instanceof ViewGroup) {
                            ((ViewGroup) pending.fbAdView.getParent())
                                    .removeView(pending.fbAdView);
                        }
                        attachNativeAdViewWithoutFlash(
                                pending.fbNativeAdLayout, pending.fbAdView);
                    } catch (Exception ignored) {
                    }
                    Integer paintedFbReq = null;
                    try {
                        if (list != null && page >= 0 && page < list.size()
                                && list.get(page) != null) {
                            paintedFbReq = list.get(page).getAd_req_position();
                        }
                    } catch (Exception ignored) {
                    }
                    tagAdSeatReqPos(pending.convertView, paintedFbReq);
                    displayFBNativeAd(pending.convertView, pending.fbAd,
                            pending.fbAdView, pending.fbNativeAdLayout);
                    markFbNativeClaimed(pending.fbAd);
                }
            } else if (pending.ad != null && pending.placeholder != null
                    && pending.myView != null && pending.convertView != null) {
                paintGoogleNativeIntoSeat(
                        pending.placeholder,
                        pending.ad,
                        pending.myView,
                        pending.fromView,
                        pending.convertView,
                        page);
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

    /**
     * Half/full YouTube card poster before iframe play.
     * Always uses {@code i.ytimg.com/vi/{id}/maxresdefault.jpg} (hq fallback) —
     * never CMS orange "Click Here", app_splash_logo, or a black plate when an id exists.
     * Keeps the prior drawable while loading so recycle/bind does not flash black.
     * Autoplay still starts via {@link #maybeAutoplayVisibleVideo} → center play click.
     */
    private void bindYoutubeFeedPoster(
            @Nullable ImageView target, @Nullable ModelClass.Datum item) {
        if (target == null || refusePosterOverLiveVideo(target)) {
            return;
        }
        String ytId = null;
        if (item != null) {
            ytId = YouTubeIdHelper.extractFromItem(
                    item.getVideoUrl(),
                    item.getThumbnail(),
                    item.getBackgroundImage(),
                    item.getVideos(),
                    item.getImages());
        }
        String poster = YouTubeIdHelper.feedPosterUrlForId(ytId);
        String hqFallback = YouTubeIdHelper.hqDefaultThumbnailUrlForId(ytId);
        try {
            if (imageLoader != null) {
                imageLoader.cancelDisplayTask(target);
            }
        } catch (Exception ignored) {
        }
        // No id → leave whatever is there; never paint black/splash/CMS art.
        if (TextUtils.isEmpty(poster)) {
            target.setTag(null);
            try {
                target.setBackground(null);
            } catch (Exception ignored) {
            }
            return;
        }
        final String contentKey = "ytposter:" + ytId;
        Object prev = target.getTag();
        if (prev != null
                && contentKey.equals(String.valueOf(prev))
                && target.getDrawable() != null) {
            return;
        }
        // Keep previous thumb visible while the new YT URL loads (no black flash).
        Drawable keepWhileLoading = target.getDrawable();
        target.setTag(contentKey);
        try {
            target.setBackground(null);
        } catch (Exception ignored) {
        }
        if (!isValidContextForGlide(context)) {
            return;
        }
        try {
            RequestBuilder<Drawable> errorBuilder = Glide.with(context)
                    .load(hqFallback)
                    .apply(flipGlideDecodeOptions().dontAnimate());
            if (keepWhileLoading != null) {
                errorBuilder = errorBuilder.placeholder(keepWhileLoading);
            }
            RequestBuilder<Drawable> primary = Glide.with(context)
                    .load(poster)
                    .apply(flipGlideDecodeOptions().dontAnimate())
                    .error(errorBuilder);
            if (keepWhileLoading != null) {
                primary = primary.placeholder(keepWhileLoading);
            }
            primary.into(target);
        } catch (Exception e) {
            showLog("bindYoutubeFeedPoster: " + e.getMessage());
            // Last resort: still try hq URL — never black/splash.
            try {
                if (!TextUtils.isEmpty(hqFallback) && isValidContextForGlide(context)) {
                    Glide.with(context)
                            .load(hqFallback)
                            .apply(flipGlideDecodeOptions().dontAnimate())
                            .into(target);
                }
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Decorative native-ad blur. Async Picasso arrival used to flash the ad
     * background for the first ~1–3s after land. Keep a stable dark plate, skip
     * blur for video natives (MediaView owns the surface), and never clear to
     * transparent while loading.
     */
    private void loadNativeAdBlurBackground(@Nullable ImageView blurView,
                                            @Nullable NativeAd ad,
                                            int listPosition) {
        if (blurView == null) {
            return;
        }
        // Must stay dark (#121212) to match MediaView / ads_screen — white plate
        // (717240d "placeholder theme") flashed the creative↔Install seam.
        final int plate = 0xFF121212;
        try {
            blurView.setBackgroundColor(plate);
            boolean allowHeavy = listPosition == currentFlipPage && !isHomeFlipAnimating();
            if (!allowHeavy) {
                // Keep solid plate — do not null the drawable (causes blink).
                if (blurView.getDrawable() == null) {
                    blurView.setImageDrawable(new android.graphics.drawable.ColorDrawable(plate));
                }
                return;
            }
            // Video creatives: MediaView texture is the visual — blur swap blinks.
            try {
                if (ad != null && ad.getMediaContent() != null
                        && ad.getMediaContent().hasVideoContent()) {
                    blurView.setImageDrawable(new android.graphics.drawable.ColorDrawable(plate));
                    return;
                }
            } catch (Exception ignored) {
            }
            if (ad == null || ad.getImages() == null || ad.getImages().isEmpty()
                    || ad.getImages().get(0) == null
                    || ad.getImages().get(0).getUri() == null) {
                blurView.setImageDrawable(new android.graphics.drawable.ColorDrawable(plate));
                return;
            }
            final Uri blurUri = ad.getImages().get(0).getUri();
            // Stable plate until blur arrives — no transparent→image flash.
            if (blurView.getDrawable() == null) {
                blurView.setImageDrawable(new android.graphics.drawable.ColorDrawable(plate));
            }
            blurView.post(() -> {
                try {
                    if (!blurView.isAttachedToWindow()) {
                        return;
                    }
                    Picasso.get()
                            .load(blurUri)
                            .resize(240, 426)
                            .centerCrop()
                            .noFade()
                            .placeholder(new android.graphics.drawable.ColorDrawable(plate))
                            .error(new android.graphics.drawable.ColorDrawable(plate))
                            .transform(new BlurTransformation(
                                    context, AD_BLUR_RADIUS, AD_BLUR_SAMPLING))
                            .into(blurView);
                } catch (Exception e) {
                    try {
                        blurView.setImageDrawable(
                                new android.graphics.drawable.ColorDrawable(plate));
                    } catch (Exception ignored) {
                    }
                }
            });
        } catch (Exception ignored) {
            try {
                blurView.setImageDrawable(new android.graphics.drawable.ColorDrawable(plate));
            } catch (Exception ignored2) {
            }
        }
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
        resetAdBindState();
        stripHalfPostsIfSpecialMode(list__);
        list = list__;
        normalizeEngagementCounts(list);
        notifyWhenFlipSettled();
    }

    /**
     * Keyword search / mode switch: apply the new deck immediately.
     * Deferred {@link #notifyWhenFlipSettled()} left FlipView on a stale pageCount
     * and let {@code flipTo} / clamp land on Continue-reading while hits existed.
     */
    public void setDataImmediate(ArrayList<ModelClass.Datum> list__) {
        resetAdBindState();
        stripHalfPostsIfSpecialMode(list__);
        list = list__;
        normalizeEngagementCounts(list);
        try {
            notifyDataSetChangedNow();
        } catch (Exception e) {
            try {
                notifyWhenFlipSettled();
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Pagination append: mutate the list and grow FlipView page count without
     * {@code notifyDataSetChanged} / force-rebinding the visible card (blink).
     */
    public void appendData(ArrayList<ModelClass.Datum> list__, int previousSize) {
        // Keep pending binds for unchanged indices; drop timeouts so new ad seats re-arm.
        cancelAllAdShellFillTimeouts();
        stripHalfPostsIfSpecialMode(list__);
        list = list__;
        normalizeEngagementCounts(list);
        notifyPageCountGrewQuietly();
    }

    /**
     * Soft-merge / held-page apply when HomePage already mutated the shared list.
     * Grows FlipView quietly — never force-rebinds the current index.
     */
    public void notifyFeedAppendedQuietly() {
        notifyPageCountGrewQuietly();
    }

    private void notifyPageCountGrewQuietly() {
        try {
            if (context instanceof HomePage) {
                ((HomePage) context).notifyFlipPageCountGrewQuietly();
                return;
            }
        } catch (Exception ignored) {
        }
        // No FlipView host — last resort (may blink). Prefer quiet path above.
        try {
            notifyWhenFlipSettled();
        } catch (Exception ignored) {
        }
    }

    /**
     * Drop deferred binds, fill timeouts, and last-bound creatives. Call on feed
     * mode switches / pool clears so tab return cannot reuse a destroyed NativeAd
     * or leave an empty/spinner shell (Share/house remounts on next getView).
     */
    public void resetAdBindState() {
        try {
            cancelAllAdShellFillTimeouts();
            cancelParkedAdFillWatchdog();
            adShellHandler.removeCallbacks(deferredSafeAdNotify);
            adShellHandler.removeCallbacks(deferredFlipDatasetNotify);
            pendingSafeAdNotify = false;
            pendingFlipDatasetNotify = false;
            pendingNativeAdBinds.clear();
            adShellAwaitingCreative.clear();
            adShellPlaceholders.clear();
            adShellFromViewTypes.clear();
            nativeAd = null;
            lastPaintedGoogleResponseId = null;
            fbAd = null;
        } catch (Exception ignored) {
        }
    }

    /**
     * Synchronous hard clear for News→Special (and other mode switches).
     * Empties the adapter-owned list immediately so FlipView cannot paint stale News
     * half cards on the first Special frame.
     */
    public void clearAllDataImmediate() {
        try {
            resetAdBindState();
            if (list == null) {
                list = new ArrayList<>();
            } else {
                list.clear();
            }
            notifyDataSetChanged();
        } catch (Exception ignored) {
            try {
                list = new ArrayList<>();
                notifyDataSetChanged();
            } catch (Exception ignored2) {
            }
        }
    }

    /**
     * Special tab must never render half News cards ({@code normal_image} /
     * {@code normal_video}), even if a cache/merge path or server glitch includes them.
     */
    private void stripHalfPostsIfSpecialMode(@Nullable ArrayList<ModelClass.Datum> items) {
        if (items == null || items.isEmpty() || context == null) {
            return;
        }
        try {
            if (!isSpecialModeNow()) {
                return;
            }
            for (int i = items.size() - 1; i >= 0; i--) {
                ModelClass.Datum d = items.get(i);
                if (d == null) {
                    items.remove(i);
                    continue;
                }
                String pt = d.getPostType();
                if (pt != null
                        && ("normal_image".equalsIgnoreCase(pt)
                        || "normal_video".equalsIgnoreCase(pt))) {
                    items.remove(i);
                }
            }
        } catch (Exception ignored) {
        }
    }

    /** Prefer HomePage in-memory Special flag (avoids SharedPreferences apply race). */
    private boolean isSpecialModeNow() {
        try {
            HomePage hp = resolveHomePageHost(context);
            if (hp != null) {
                return hp.isSpecialFeedModeActive();
            }
        } catch (Exception ignored) {
        }
        try {
            boolean special = SharedPrefsData.getBool(
                    context, Constants.isUnreadPostsSelected, Constants.PREF_NAME);
            boolean video = SharedPrefsData.getBool(
                    context, Constants.isVideoPostsSelected, Constants.PREF_NAME);
            return special && !video;
        } catch (Exception ignored) {
            return false;
        }
    }

    /**
     * Reject half News types in Special at paint time (defense if list was mutated
     * after setData / without strip).
     */
    private boolean isHalfNewsTypeForbiddenInSpecial(@Nullable String postType) {
        if (!isSpecialModeNow() || postType == null) return false;
        return "normal_image".equalsIgnoreCase(postType)
                || "normal_video".equalsIgnoreCase(postType);
    }

    private void normalizeEngagementCounts(ArrayList<ModelClass.Datum> items) {
        if (items == null) return;
        for (ModelClass.Datum item : items) {
            if (item.getLikes() == null) item.setLikes(0);
            if (item.getComments() == null) item.setComments(0);
            if (item.getShares() == null) item.setShares(0);
            if (item.getWhatsappShares() == null) item.setWhatsappShares(0);
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
        EngagementCountHelper.applyIconTint(context, convertView.findViewById(R.id.half_search_icon));
        EngagementCountHelper.applyIconTint(context, convertView.findViewById(R.id.half_tts_icon));
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

    private void rememberLikeCountView(Long postId, TextView countView) {
        if (postId == null || countView == null) return;
        likeCountViewsByPostId.put(postId, new java.lang.ref.WeakReference<>(countView));
    }

    private TextView findRememberedLikeCountView(Long postId) {
        if (postId == null) return null;
        java.lang.ref.WeakReference<TextView> ref = likeCountViewsByPostId.get(postId);
        TextView tv = ref != null ? ref.get() : null;
        if (tv != null && tv.isAttachedToWindow()) {
            return tv;
        }
        return null;
    }

    private void rememberCommentCountView(Long postId, TextView countView) {
        if (postId == null || countView == null) return;
        commentCountViewsByPostId.put(postId, new java.lang.ref.WeakReference<>(countView));
    }

    private TextView findRememberedCommentCountView(Long postId) {
        if (postId == null) return null;
        java.lang.ref.WeakReference<TextView> ref = commentCountViewsByPostId.get(postId);
        TextView tv = ref != null ? ref.get() : null;
        if (tv != null && tv.isAttachedToWindow()) {
            return tv;
        }
        return null;
    }

    private void rememberShareCountView(Long postId, TextView countView) {
        if (postId == null || countView == null) return;
        shareCountViewsByPostId.put(postId, new java.lang.ref.WeakReference<>(countView));
    }

    private void rememberWhatsappShareCountView(Long postId, TextView countView) {
        if (postId == null || countView == null) return;
        whatsappShareCountViewsByPostId.put(postId, new java.lang.ref.WeakReference<>(countView));
    }

    private TextView findRememberedShareCountView(Long postId) {
        if (postId == null) return null;
        java.lang.ref.WeakReference<TextView> ref = shareCountViewsByPostId.get(postId);
        TextView tv = ref != null ? ref.get() : null;
        if (tv != null && tv.isAttachedToWindow()) {
            return tv;
        }
        return null;
    }

    private TextView findRememberedWhatsappShareCountView(Long postId) {
        if (postId == null) return null;
        java.lang.ref.WeakReference<TextView> ref = whatsappShareCountViewsByPostId.get(postId);
        TextView tv = ref != null ? ref.get() : null;
        if (tv != null && tv.isAttachedToWindow()) {
            return tv;
        }
        return null;
    }

    private void bindShareCountForView(@Nullable TextView countView, int shares) {
        if (countView == null) return;
        if (countView.getId() == R.id.half_share_count) {
            bindHalfActionCount(countView, shares);
        } else {
            bindFullPostLikeCount(countView, shares, getShareLabel());
        }
    }

    /** @return true when a live comment-count TextView was updated in place. */
    private boolean applyCommentCountToRememberedView(Long postId, Integer comments) {
        if (postId == null || comments == null) return false;
        TextView countView = findRememberedCommentCountView(postId);
        if (countView == null) {
            countView = findLiveEngagementTextView(postId, R.id.half_comment_count,
                    R.id.full_img_comment_txt, R.id.full_video_comment_text);
            if (countView != null) {
                rememberCommentCountView(postId, countView);
            }
        }
        if (countView == null) return false;
        if (countView.getId() == R.id.half_comment_count) {
            bindHalfActionCount(countView, comments);
        } else {
            bindFullPostLikeCount(countView, comments, getCommentLabel());
        }
        return true;
    }

    private boolean applyShareCountToRememberedView(Long postId, Integer shares) {
        if (postId == null || shares == null) return false;
        TextView countView = findRememberedShareCountView(postId);
        if (countView == null) {
            countView = findLiveEngagementTextView(postId, R.id.half_share_count,
                    R.id.full_image_share_text, R.id.full_video_share_text);
            if (countView != null) {
                rememberShareCountView(postId, countView);
            }
        }
        if (countView == null) return false;
        bindShareCountForView(countView, shares);
        return true;
    }

    private void bindWhatsappCountForView(@Nullable TextView countView, Integer count) {
        if (countView == null) return;
        if (countView.getId() == R.id.half_whatsapp_count) {
            int c = count == null ? 0 : count;
            // Always show the number, including 0, the way full-post WhatsApp does.
            // Color stays the half-row admin count color so it stays readable on the card.
            countView.setText(formatCount(c));
            countView.setBackground(null);
            countView.setTextColor(EngagementCountHelper.getCountColor(context));
            countView.setTypeface(countView.getTypeface(), Typeface.BOLD);
            countView.setVisibility(View.VISIBLE);
            return;
        }
        bindFullPostLikeCount(countView, count, null);
    }

    private boolean applyWhatsappShareCountToRememberedView(Long postId, Integer waShares) {
        if (postId == null || waShares == null) return false;
        TextView countView = findLiveEngagementTextView(postId, R.id.half_whatsapp_count,
                R.id.full_img_whtsapp_text, R.id.full_img_whtsapp_text);
        if (countView == null) {
            countView = findRememberedWhatsappShareCountView(postId);
        } else {
            rememberWhatsappShareCountView(postId, countView);
        }
        boolean painted = false;
        if (countView != null) {
            bindWhatsappCountForView(countView, waShares);
            painted = true;
        }
        // Footer middle WhatsApp uses this same total. One Share_Count_Value call
        // paints both; this does not increment again.
        if (context instanceof HomePage) {
            ((HomePage) context).onWhatsappShareCountUpdated(postId, waShares);
        }
        return painted;
    }

    /**
     * Resolve an engagement TextView on the visible Flip page when the weak
     * remember-map missed (scrap recycle). Avoids notifyDataSetChanged flash.
     */
    @Nullable
    private TextView findLiveEngagementTextView(Long postId, int halfId, int fullImageId,
                                                int fullVideoId) {
        try {
            if (!(context instanceof HomePage) || postId == null) {
                return null;
            }
            View page = ((HomePage) context).getCurrentFlipPageView();
            if (page == null) {
                return null;
            }
            // Only paint if this page is the same post (avoid updating neighbor scrap).
            if (list != null && currentFlipPage >= 0 && currentFlipPage < list.size()) {
                ModelClass.Datum slot = list.get(currentFlipPage);
                if (slot == null || slot.getPostId() == null
                        || !slot.getPostId().equals(postId)) {
                    return null;
                }
            }
            View half = page.findViewById(halfId);
            if (half instanceof TextView && half.isAttachedToWindow()) {
                return (TextView) half;
            }
            View fullImg = page.findViewById(fullImageId);
            if (fullImg instanceof TextView && fullImg.isAttachedToWindow()) {
                return (TextView) fullImg;
            }
            View fullVid = page.findViewById(fullVideoId);
            if (fullVid instanceof TextView && fullVid.isAttachedToWindow()) {
                return (TextView) fullVid;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * Half articles have no right-rail WhatsApp. Insert the same chip full posts
     * use (icon + whatsapp_shares) before Share. Tap shares once via the footer path.
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
        slot.setVisibility(View.VISIBLE);
        ImageView icon = slot.findViewById(R.id.half_whatsapp_icon);
        if (icon != null) {
            icon.setImageResource(R.drawable.ic_whatsapp_original1);
            icon.clearColorFilter();
            androidx.core.widget.ImageViewCompat.setImageTintList(icon, null);
        }
        TextView countView = slot.findViewById(R.id.half_whatsapp_count);
        bindWhatsappCountForView(countView, list.get(position).getWhatsappShares());
        rememberWhatsappShareCountView(list.get(position).getPostId(), countView);
        final int sharePosition = position;
        slot.setOnClickListener(v -> {
            if (context instanceof HomePage) {
                ((HomePage) context).onArticleWhatsappClicked(sharePosition);
            }
        });
    }

    private void bindHalfPostEngagementCounts(View convertView, int position) {
        ensureHalfWhatsappAction(convertView, position);
        TextView likeCount = convertView.findViewById(R.id.half_like_count);
        TextView commentCount = convertView.findViewById(R.id.half_comment_count);
        TextView shareCount = convertView.findViewById(R.id.half_share_count);
        bindHalfActionCount(likeCount, list.get(position).getLikes());
        bindHalfActionCount(commentCount, list.get(position).getComments());
        bindHalfActionCount(shareCount, list.get(position).getShares());
        rememberLikeCountView(list.get(position).getPostId(), likeCount);
        rememberCommentCountView(list.get(position).getPostId(), commentCount);
        rememberShareCountView(list.get(position).getPostId(), shareCount);
        tintHalfActionIcons(convertView);
        applyActionIconRowOffset(convertView);
        applyHalfActionIconSizes(context, convertView);
        bindHalfSearchIcon(convertView);
        bindHalfTtsSpeaker(convertView, position);
        applyTitleBodyGap(context, convertView);
        applyContentBlockOffset(context, convertView);
        bindHalfBodyCollapseToggle(convertView);
    }

    /**
     * Admin {@code search_icon_on_engagement}: search between share and ⋮.
     * Opens {@link com.anyNews.anynews.Activities.InAppSearchActivity} with circular reveal.
     */
    private void bindHalfSearchIcon(@Nullable View convertView) {
        if (convertView == null) return;
        View slot = convertView.findViewById(R.id.half_search);
        if (slot == null) return;
        boolean show = SearchIconConfig.isOnEngagement(context);
        slot.setVisibility(show ? View.VISIBLE : View.GONE);
        if (!show) return;
        // Keep outline magnifier here — toggle bar uses ic_search_toggle_rich.
        View icon = convertView.findViewById(R.id.half_search_icon);
        if (icon instanceof ImageView) {
            ((ImageView) icon).setImageResource(R.drawable.ic_action_search);
        }
        View.OnClickListener open =
                v -> {
                    try {
                        android.app.Activity act = null;
                        if (context instanceof android.app.Activity) {
                            act = (android.app.Activity) context;
                        } else if (activity instanceof android.app.Activity) {
                            act = (android.app.Activity) activity;
                        }
                        if (act != null) {
                            SearchRevealHelper.openFrom(act, v);
                        }
                    } catch (Exception ignored) {
                    }
                };
        slot.setOnClickListener(open);
        if (icon != null) icon.setOnClickListener(open);
    }

    /**
     * Content-area single tap → mode-aware News|Read chrome and/or Classic hide-bars.
     * Used for half posts (body/title) and re-bound mid-session go-offline.
     * Does not overwrite parent_layout / full_image side-menu listeners — those call
     * {@link FlipperAdapter#dispatchNewsContentTap} first.
     */
    public static void installOfflineChromeTaps(@Nullable View convertView) {
        if (convertView == null) return;
        final HomePage chromeHost = resolveHomePageHost(convertView.getContext());
        if (chromeHost == null) return;

        final View footer = convertView.findViewById(R.id.ll_footer_layout);
        final View sideMenu = convertView.findViewById(R.id.side_menu);
        final boolean fullPost = footer == null && sideMenu != null;
        final View classicChrome = footer != null ? footer : sideMenu;

        // Half: keep engagement row in sync with writers-row visibility after bind/recycle.
        if (!fullPost) {
            syncHalfEngagementWithWritersRow(convertView.getContext(), convertView);
        }

        // Half-article body / title regions (not footer-toggle host views).
        final View titleBlock = convertView.findViewById(R.id.layoutParentTitle);
        View relativeBottom = convertView.findViewById(R.id.relativeBottom);
        installNewsModeTap(relativeBottom, convertView, classicChrome, fullPost, chromeHost);
        installNewsModeTap(titleBlock, convertView, classicChrome, fullPost, chromeHost);
        for (int id : HALF_BODY_TEXT_IDS) {
            installNewsModeTap(
                    convertView.findViewById(id), convertView, classicChrome, fullPost, chromeHost);
        }
        int[] titleIds = new int[] {
                R.id.half_content_title, R.id.tamil_half_content_title,
                R.id.english_half_content_title,
                R.id.half_content_title__, R.id.tamil_half_content_title__,
                R.id.english_half_content_title__
        };
        for (int id : titleIds) {
            installNewsModeTap(
                    convertView.findViewById(id), convertView, classicChrome, fullPost, chromeHost);
        }
        installNewsModeTap(
                convertView.findViewById(R.id.layoutParentTop),
                convertView, classicChrome, fullPost, chromeHost);
        installNewsModeTap(
                convertView.findViewById(R.id.layoutImageHeight),
                convertView, classicChrome, fullPost, chromeHost);
        installNewsModeTap(
                convertView.findViewById(R.id.relative_half_contents),
                convertView, classicChrome, fullPost, chromeHost);
        // Full-post body region when present.
        installNewsModeTap(
                convertView.findViewById(R.id.full_image_view_details),
                convertView, classicChrome, true, chromeHost);
    }

    /**
     * Keep half-article body expanded for reading. Wire single taps on the content
     * area to mode-aware chrome / Classic hide (not body collapse).
     * Vertical flings stay with FlipView (listener returns false / doesn't consume).
     */
    public static void bindHalfBodyCollapseToggle(View convertView) {
        if (convertView == null) return;
        final View titleBlock = convertView.findViewById(R.id.layoutParentTitle);
        if (titleBlock == null) {
            // Full posts: still install chrome taps if chrome surfaces exist.
            installOfflineChromeTaps(convertView);
            return;
        }

        View media = convertView.findViewById(R.id.layoutImageHeight);
        if (media == null) {
            media = convertView.findViewById(R.id.layoutParentTop);
        }
        final View mediaBlock = media;

        // Fresh bind: end any running anim and force expanded for readable article body.
        cancelHalfBodyCollapseAnimator(titleBlock);
        boolean needRestore =
                Boolean.TRUE.equals(titleBlock.getTag(R.id.tag_half_body_collapsed))
                        || titleBlock.getAlpha() < 0.99f;
        if (!needRestore) {
            ViewGroup.LayoutParams tlp = titleBlock.getLayoutParams();
            if (tlp != null && tlp.height == 0) {
                float w = 0f;
                if (tlp instanceof android.widget.LinearLayout.LayoutParams) {
                    w = ((android.widget.LinearLayout.LayoutParams) tlp).weight;
                }
                // height 0 with no weight = collapsed empty body
                needRestore = w <= 0f;
            }
        }
        if (needRestore) {
            restoreHalfBodyExpandedImmediate(titleBlock, mediaBlock);
        }
        titleBlock.setTag(R.id.tag_half_body_collapsed, Boolean.FALSE);
        titleBlock.setAlpha(1f);

        // Content-area single tap → mode-aware News|Read / Classic hide.
        installOfflineChromeTaps(convertView);
    }

    @Nullable
    private static HomePage resolveHomePageHost(@Nullable Context ctx) {
        Context c = ctx;
        for (int i = 0; i < 6 && c != null; i++) {
            if (c instanceof HomePage) {
                return (HomePage) c;
            }
            if (c instanceof android.content.ContextWrapper) {
                c = ((android.content.ContextWrapper) c).getBaseContext();
            } else {
                break;
            }
        }
        return null;
    }

    /**
     * Content tap with Classic / Reader / Hybrid routing on main News FlipView.
     * Long-press toggles News|Read chrome when active — except Classic, where
     * News|Read never appears (likes/writers stay content-tap Classic hide).
     */
    private static void installNewsModeTap(
            @Nullable View target,
            @Nullable final View bandRoot,
            @Nullable final View classicChrome,
            final boolean fullPost,
            @Nullable final HomePage homePage) {
        if (target == null) return;
        final float[] lastXY = new float[] { Float.NaN, Float.NaN };
        target.setClickable(true);
        target.setOnTouchListener((v, motionEvent) -> {
            try {
                int a = motionEvent.getActionMasked();
                if (a == android.view.MotionEvent.ACTION_DOWN
                        || a == android.view.MotionEvent.ACTION_UP) {
                    lastXY[0] = motionEvent.getRawX();
                    lastXY[1] = motionEvent.getRawY();
                }
            } catch (Exception ignored) {
            }
            return false;
        });
        target.setOnLongClickListener(v -> {
            try {
                HomePage hp = homePage != null ? homePage : resolveHomePageHost(v.getContext());
                if (hp == null || !hp.isHomeModeChromeActive()) return false;
                // Classic: do not surface News|Read via long-press either.
                if (NewsTapModeConfig.isClassic(v.getContext())
                        && hp.isOnlineMainNewsChromeActive()) {
                    return true;
                }
                hp.toggleHomeModeChrome();
                return true;
            } catch (Exception ignored) {
            }
            return false;
        });
        target.setOnClickListener(v -> {
            float x = lastXY[0];
            float y = lastXY[1];
            if (Float.isNaN(x) || Float.isNaN(y)) {
                int[] loc = new int[2];
                try {
                    v.getLocationOnScreen(loc);
                    x = loc[0] + v.getWidth() / 2f;
                    y = loc[1] + v.getHeight() / 2f;
                } catch (Exception ignored) {
                    x = 0f;
                    y = 0f;
                }
            }
            dispatchNewsContentTap(
                    v.getContext(),
                    bandRoot != null ? bandRoot : v,
                    x,
                    y,
                    classicChrome,
                    fullPost);
        });
    }

    /**
     * Resolve Classic hide vs News|Read chrome for a content tap.
     *
     * @return true if handled (caller should not run a second classic toggle)
     */
    private static boolean dispatchNewsContentTap(
            @Nullable Context ctx,
            @Nullable View bandRoot,
            float rawX,
            float rawY,
            @Nullable View classicChrome,
            boolean fullPost) {
        try {
            HomePage hp = resolveHomePageHost(ctx);
            if (hp != null && hp.isLocalFeedChromeActive()) {
                // Local: toggle writers/likes (Classic bars) AND Local|District pill.
                // Previously only the pill toggled, so force-hidden article bars
                // never came back on tap.
                toggleClassicChrome(ctx, bandRoot, classicChrome, fullPost);
                hp.toggleHomeModeChrome();
                return true;
            }

            final boolean onlineMain =
                    hp != null && hp.isOnlineMainNewsChromeActive();
            final boolean chromeActive =
                    hp != null && hp.isHomeModeChromeActive();

            // Offline News|Bookmarks / Special / Video: keep prior chrome-first behavior.
            if (!onlineMain) {
                if (chromeActive) {
                    hp.toggleHomeModeChrome();
                    return true;
                }
                toggleClassicChrome(ctx, bandRoot, classicChrome, fullPost);
                return true;
            }

            String mode = NewsTapModeConfig.getMode(ctx);
            if (NewsTapModeConfig.MODE_READER.equals(mode)) {
                if (chromeActive) {
                    hp.toggleHomeModeChrome();
                    return true;
                }
                toggleClassicChrome(ctx, bandRoot, classicChrome, fullPost);
                return true;
            }

            if (NewsTapModeConfig.MODE_CLASSIC.equals(mode)) {
                toggleClassicChrome(ctx, bandRoot, classicChrome, fullPost);
                return true;
            }

            // Hybrid: upper band → Reader; lower / right-rail → Classic.
            float yPct = 50f;
            float xFromRightPct = 100f;
            View root = bandRoot;
            if (root != null && root.getHeight() > 0 && root.getWidth() > 0) {
                int[] loc = new int[2];
                root.getLocationOnScreen(loc);
                float relY = rawY - loc[1];
                float relX = rawX - loc[0];
                yPct = (relY / (float) root.getHeight()) * 100f;
                xFromRightPct =
                        ((root.getWidth() - relX) / (float) root.getWidth()) * 100f;
            }

            int upperPct =
                    fullPost
                            ? NewsTapModeConfig.getFullUpperPct(ctx)
                            : NewsTapModeConfig.getHalfUpperPct(ctx);
            int rightRailPct = NewsTapModeConfig.getFullRightRailPct(ctx);

            boolean classicZone = false;
            if (fullPost && xFromRightPct <= rightRailPct) {
                classicZone = true;
            } else if (yPct >= upperPct) {
                classicZone = true;
            }

            if (classicZone) {
                toggleClassicChrome(ctx, bandRoot, classicChrome, fullPost);
                return true;
            }
            if (chromeActive) {
                hp.toggleHomeModeChrome();
                return true;
            }
            toggleClassicChrome(ctx, bandRoot, classicChrome, fullPost);
            return true;
        } catch (Exception ignored) {
            toggleClassicChrome(ctx, bandRoot, classicChrome, fullPost);
            return true;
        }
    }

    /**
     * Classic hide: half → writers/reporter footer; full → likes/comments side rail.
     * When admin {@code news_tap_half_hide_engagement} is ON, half also toggles the
     * four action icons (like / comment / share / flag) with the writers row.
     * Half uses {@link View#INVISIBLE} (not {@link View#GONE}) so article text below
     * does not jump; the red share branding strip stays out of this path entirely.
     */
    private static void toggleClassicChrome(
            @Nullable Context ctx,
            @Nullable View cardRoot,
            @Nullable View classicChrome,
            boolean fullPost) {
        if (classicChrome == null) return;
        try {
            // Always keep share branding strip out of tap chrome (Share click shows it).
            hideHalfShareBrandingStrip(cardRoot);
            if (classicChrome.isShown()
                    || classicChrome.getVisibility() == View.VISIBLE) {
                classicChrome.setVisibility(View.INVISIBLE);
            } else {
                classicChrome.setVisibility(View.VISIBLE);
            }
            if (!fullPost) {
                syncHalfEngagementWithWritersRow(ctx, cardRoot);
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Force-hide Classic chrome on a card (same targets as tap Classic hide).
     * Used for cold-start / first-paint auto-hide when {@code news_tap_mode=classic}.
     */
    public static void hideClassicChromeOnCard(
            @Nullable Context ctx, @Nullable View cardRoot) {
        if (cardRoot == null) return;
        try {
            hideHalfShareBrandingStrip(cardRoot);
            View footer = cardRoot.findViewById(R.id.ll_footer_layout);
            View sideMenu = cardRoot.findViewById(R.id.side_menu);
            boolean fullPost = footer == null && sideMenu != null;
            View classicChrome = footer != null ? footer : sideMenu;
            if (classicChrome != null) {
                classicChrome.setVisibility(View.INVISIBLE);
            }
            if (!fullPost) {
                syncHalfEngagementWithWritersRow(ctx, cardRoot);
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Red ANYNEWS / slogan strip under media is for share-screenshot capture only.
     * Never reveal it from content-tap chrome toggles.
     */
    private static void hideHalfShareBrandingStrip(@Nullable View cardRoot) {
        if (cardRoot == null) return;
        try {
            View strip = cardRoot.findViewById(R.id.with_strips_layout);
            if (strip != null) {
                strip.setVisibility(View.GONE);
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Half articles: optionally mirror writers-row visibility onto the engagement
     * icon row ({@code with_out_strips_layout}). When the admin toggle is OFF,
     * always keep engagement visible (writers row may still hide alone).
     * Uses {@link View#INVISIBLE} when hiding so layout height is reserved and
     * the sibling share branding strip cannot expand into the freed space.
     */
    private static void syncHalfEngagementWithWritersRow(
            @Nullable Context ctx, @Nullable View cardRoot) {
        if (cardRoot == null) return;
        try {
            // Share strip must stay GONE unless an actual Share flow shows it.
            hideHalfShareBrandingStrip(cardRoot);
            View engagement = cardRoot.findViewById(R.id.with_out_strips_layout);
            if (engagement == null) return;
            if (!NewsTapModeConfig.halfHideEngagementWithWriters(ctx)) {
                engagement.setVisibility(View.VISIBLE);
                return;
            }
            View footer = cardRoot.findViewById(R.id.ll_footer_layout);
            if (footer == null) {
                engagement.setVisibility(View.VISIBLE);
                return;
            }
            boolean writersVisible = footer.getVisibility() == View.VISIBLE;
            engagement.setVisibility(
                    writersVisible ? View.VISIBLE : View.INVISIBLE);
        } catch (Exception ignored) {
        }
    }

    private static void cancelHalfBodyCollapseAnimator(View titleBlock) {
        if (titleBlock == null) return;
        Object a = titleBlock.getTag(R.id.tag_half_body_animator);
        if (a instanceof android.animation.ValueAnimator) {
            try {
                ((android.animation.ValueAnimator) a).cancel();
            } catch (Exception ignored) {
            }
        }
        titleBlock.setTag(R.id.tag_half_body_animator, null);
    }

    private static void restoreHalfBodyExpandedImmediate(View titleBlock, View mediaBlock) {
        if (titleBlock == null) return;
        Integer titleH = (Integer) titleBlock.getTag(R.id.tag_half_body_title_h);
        Float weight = (Float) titleBlock.getTag(R.id.tag_half_body_title_weight);
        ViewGroup.LayoutParams tlp = titleBlock.getLayoutParams();
        if (tlp != null) {
            if (weight != null && weight > 0f && tlp instanceof android.widget.LinearLayout.LayoutParams) {
                android.widget.LinearLayout.LayoutParams llp =
                        (android.widget.LinearLayout.LayoutParams) tlp;
                llp.height = 0;
                llp.weight = weight;
                titleBlock.setLayoutParams(llp);
            } else if (titleH != null && titleH > 0) {
                tlp.height = titleH;
                if (tlp instanceof android.widget.LinearLayout.LayoutParams) {
                    ((android.widget.LinearLayout.LayoutParams) tlp).weight = 0f;
                }
                titleBlock.setLayoutParams(tlp);
            }
        }
        titleBlock.setAlpha(1f);
        if (mediaBlock != null) {
            Integer mediaH = (Integer) mediaBlock.getTag(R.id.tag_half_body_media_h);
            if (mediaH != null && mediaH > 0) {
                ViewGroup.LayoutParams mlp = mediaBlock.getLayoutParams();
                if (mlp != null) {
                    mlp.height = mediaH;
                    mediaBlock.setLayoutParams(mlp);
                }
            }
        }
    }

    private static void animateHalfBodyCollapse(
            final View titleBlock, final View mediaBlock, final boolean collapse) {
        if (titleBlock == null) return;
        cancelHalfBodyCollapseAnimator(titleBlock);

        titleBlock.measure(
                View.MeasureSpec.makeMeasureSpec(titleBlock.getWidth() > 0
                                ? titleBlock.getWidth()
                                : ((View) titleBlock.getParent()).getWidth(),
                        View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        int measuredTitle = titleBlock.getHeight() > 0
                ? titleBlock.getHeight()
                : titleBlock.getMeasuredHeight();
        int measuredMedia = mediaBlock != null
                ? (mediaBlock.getHeight() > 0 ? mediaBlock.getHeight() : mediaBlock.getMeasuredHeight())
                : 0;

        Integer storedTitle = (Integer) titleBlock.getTag(R.id.tag_half_body_title_h);
        Integer storedMedia = mediaBlock != null
                ? (Integer) mediaBlock.getTag(R.id.tag_half_body_media_h)
                : null;

        if (storedTitle == null || storedTitle <= 0) {
            storedTitle = Math.max(measuredTitle, 1);
            titleBlock.setTag(R.id.tag_half_body_title_h, storedTitle);
            ViewGroup.LayoutParams tlp0 = titleBlock.getLayoutParams();
            if (tlp0 instanceof android.widget.LinearLayout.LayoutParams) {
                titleBlock.setTag(
                        R.id.tag_half_body_title_weight,
                        ((android.widget.LinearLayout.LayoutParams) tlp0).weight);
            }
        }
        if (mediaBlock != null && (storedMedia == null || storedMedia <= 0) && measuredMedia > 0) {
            storedMedia = measuredMedia;
            mediaBlock.setTag(R.id.tag_half_body_media_h, storedMedia);
        }

        final int expandTitleH = storedTitle;
        final int expandMediaH =
                storedMedia != null && storedMedia > 0 ? storedMedia : measuredMedia;
        final int fromTitle;
        final int toTitle;
        final int fromMedia;
        final int toMedia;
        if (collapse) {
            fromTitle = Math.max(titleBlock.getHeight() > 0 ? titleBlock.getHeight() : expandTitleH, 0);
            toTitle = 0;
            fromMedia = mediaBlock != null
                    ? (mediaBlock.getHeight() > 0 ? mediaBlock.getHeight() : expandMediaH)
                    : 0;
            toMedia = expandMediaH + expandTitleH;
        } else {
            fromTitle = Math.max(titleBlock.getHeight(), 0);
            toTitle = expandTitleH;
            fromMedia = mediaBlock != null
                    ? (mediaBlock.getHeight() > 0 ? mediaBlock.getHeight() : expandMediaH)
                    : 0;
            toMedia = expandMediaH;
        }

        // Fixed heights (weight 0) for smooth pixel animation.
        forceFixedHeight(titleBlock, fromTitle);
        if (mediaBlock != null) {
            forceFixedHeight(mediaBlock, fromMedia);
        }

        android.animation.ValueAnimator anim = android.animation.ValueAnimator.ofFloat(0f, 1f);
        anim.setDuration(280);
        anim.setInterpolator(new androidx.interpolator.view.animation.FastOutSlowInInterpolator());
        final int fFromTitle = fromTitle;
        final int fToTitle = toTitle;
        final int fFromMedia = fromMedia;
        final int fToMedia = toMedia;
        anim.addUpdateListener(animation -> {
            float f = (float) animation.getAnimatedValue();
            int th = Math.round(fFromTitle + (fToTitle - fFromTitle) * f);
            forceFixedHeight(titleBlock, Math.max(th, 0));
            titleBlock.setAlpha(collapse ? (1f - f) : f);
            if (mediaBlock != null && fToMedia > 0) {
                int mh = Math.round(fFromMedia + (fToMedia - fFromMedia) * f);
                forceFixedHeight(mediaBlock, Math.max(mh, 1));
            }
        });
        anim.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                titleBlock.setTag(R.id.tag_half_body_collapsed, collapse);
                titleBlock.setTag(R.id.tag_half_body_animator, null);
                if (!collapse) {
                    titleBlock.setAlpha(1f);
                    // Prefer weight restore for residual layout flexibility.
                    Float w = (Float) titleBlock.getTag(R.id.tag_half_body_title_weight);
                    ViewGroup.LayoutParams tlp = titleBlock.getLayoutParams();
                    if (w != null && w > 0f && tlp instanceof android.widget.LinearLayout.LayoutParams) {
                        android.widget.LinearLayout.LayoutParams llp =
                                (android.widget.LinearLayout.LayoutParams) tlp;
                        llp.height = 0;
                        llp.weight = w;
                        titleBlock.setLayoutParams(llp);
                    } else {
                        forceFixedHeight(titleBlock, expandTitleH);
                    }
                    if (mediaBlock != null) {
                        forceFixedHeight(mediaBlock, expandMediaH);
                    }
                } else {
                    forceFixedHeight(titleBlock, 0);
                    titleBlock.setAlpha(0f);
                    if (mediaBlock != null) {
                        forceFixedHeight(mediaBlock, expandMediaH + expandTitleH);
                    }
                }
            }
        });
        titleBlock.setTag(R.id.tag_half_body_animator, anim);
        anim.start();
    }

    private static void forceFixedHeight(View v, int heightPx) {
        if (v == null) return;
        ViewGroup.LayoutParams lp = v.getLayoutParams();
        if (lp == null) return;
        lp.height = heightPx;
        if (lp instanceof android.widget.LinearLayout.LayoutParams) {
            ((android.widget.LinearLayout.LayoutParams) lp).weight = 0f;
        }
        v.setLayoutParams(lp);
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

    /** Drop an in-feed ratings card after Play Store CTA (never leave thank-you). */
    private void removeRatingsCardAt(int position) {
        try {
            if (list == null || position < 0 || position >= list.size()) {
                return;
            }
            ModelClass.Datum item = list.get(position);
            if (item == null || item.getPostType() == null
                    || !item.getPostType().equalsIgnoreCase("ratings")) {
                // Position may have shifted — scan once.
                for (int i = 0; i < list.size(); i++) {
                    ModelClass.Datum d = list.get(i);
                    if (d != null && d.getPostType() != null
                            && d.getPostType().equalsIgnoreCase("ratings")) {
                        list.remove(i);
                        notifyWhenFlipSettled();
                        return;
                    }
                }
                return;
            }
            list.remove(position);
            notifyWhenFlipSettled();
        } catch (Exception e) {
            showLog("removeRatingsCardAt: " + e.getMessage());
        }
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

    /**
     * Writer strip name + designation + relative time for half image/video layouts.
     * Desk Auto News: clean admin name (no "Auto News" suffix) and role Reporter.
     * Never paints the literal word {@code null} (missing role / published_time).
     */
    private void bindWriterStripIdentity(
            ModelClass.Datum item,
            TextView nameView,
            TextView designationView,
            TextView timeView) {
        if (item == null) {
            return;
        }
        if (nameView != null) {
            nameView.setText(safeBylineText(
                    AutoNewsBylineHelper.displayWriterName(item.getWriterName())));
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
            // Always clear first — recycled FlipView scrap can keep literal "null".
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

    /** Drop empty / literal {@code null} strings before painting writer strip. */
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

    /**
     * Title / body / strip. {@code "" + null} and a JSON {@code "null"} both paint
     * the word null under the headline — treat those as empty.
     */
    @NonNull
    private static String displayCopy(@Nullable String raw) {
        return safeBylineText(raw);
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
            R.id.half_image_like, R.id.half_comment_icon, R.id.half_whatsapp_icon, R.id.half_share_icon,
            R.id.half_tts_icon, R.id.half_search_icon, R.id.share, R.id.report_icon, R.id.video_report
    };

    // Tappable slot containers that wrap each action icon (image + video layouts).
    private static final int[] HALF_ACTION_SLOT_IDS = new int[] {
            R.id.half_image_like_icon, R.id.half_comment, R.id.half_whatsapp, R.id.half_share,
            R.id.half_tts, R.id.half_search, R.id.half_share__, R.id.report
    };

    // Defaults + clamps mirror the backend (Root_Settings / Update_Configuration).
    private static final int DEFAULT_ACTION_ICON_SIZE_DP = 21;
    private static final int ACTION_ICON_SIZE_MIN_DP = 10;
    private static final int ACTION_ICON_SIZE_MAX_DP = 40;
    /** Tighter than the previous 8dp slot margin that spread the row to the share link. */
    public static final int DEFAULT_ACTION_ICON_GAP_DP = 2;
    private static final int ACTION_ICON_GAP_MIN_DP = 0;
    private static final int ACTION_ICON_GAP_MAX_DP = 32;
    /**
     * Vertical gap between right-rail actions on full image / full video.
     * Tighter than the old 20dp full-image margins (share was 17dp; full video was 10dp).
     */
    public static final int DEFAULT_FULL_SIDE_ACTION_GAP_DP = 6;
    private static final int FULL_SIDE_ACTION_GAP_MIN_DP = 0;
    private static final int FULL_SIDE_ACTION_GAP_MAX_DP = 32;
    private static final int[] FULL_SIDE_ACTION_IDS = new int[] {
            R.id.full_image_like, R.id.full_image_comment, R.id.full_image_whatsapp,
            R.id.full_image_share, R.id.like_for_video, R.id.comment_for_video,
            R.id.dislike_for_video, R.id.share_for_video
    };
    /** Trailing inset on the last icon so the cluster does not touch the card edge. */
    private static final int ACTION_ICON_END_INSET_DP = 4;
    /** Extra space past the share-link overlay before the first icon. */
    private static final int SHARE_LINK_CLEARANCE_DP = 8;
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
        // Engagement search uses the same circle/glyph sizes as like/share/report
        // (Configuration ACTION_ICON / LIKE_CIRCLE) — do not boost larger.
        ensureHalfTtsPulseVisible(convertView, slotPx);
        applyActionIconHorizontalGap(context, convertView, density);
    }

    public static int clampActionIconGapDp(int gapDp) {
        return Math.max(ACTION_ICON_GAP_MIN_DP, Math.min(ACTION_ICON_GAP_MAX_DP, gapDp));
    }

    private static int readActionIconGapDp(Context context) {
        int gapDp = DEFAULT_ACTION_ICON_GAP_DP;
        try {
            android.content.SharedPreferences sp =
                    context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
            if (sp.contains(Constants.ACTION_ICON_GAP_DP)) {
                gapDp = sp.getInt(Constants.ACTION_ICON_GAP_DP, DEFAULT_ACTION_ICON_GAP_DP);
            }
        } catch (Exception ignored) {
        }
        return clampActionIconGapDp(gapDp);
    }

    public static int clampFullSideActionGapDp(int gapDp) {
        return Math.max(FULL_SIDE_ACTION_GAP_MIN_DP, Math.min(FULL_SIDE_ACTION_GAP_MAX_DP, gapDp));
    }

    private static int readFullSideActionGapDp(Context context) {
        int gapDp = DEFAULT_FULL_SIDE_ACTION_GAP_DP;
        try {
            android.content.SharedPreferences sp =
                    context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
            if (sp.contains(Constants.FULL_SIDE_ACTION_GAP_DP)) {
                gapDp = sp.getInt(Constants.FULL_SIDE_ACTION_GAP_DP, DEFAULT_FULL_SIDE_ACTION_GAP_DP);
            }
        } catch (Exception ignored) {
        }
        return clampFullSideActionGapDp(gapDp);
    }

    /**
     * Space between like / comment / WhatsApp / share on the full-image and
     * full-video right rail. The writer avatar keeps its own margins so the
     * photo and + badge do not collide with the first button. Each count stays
     * inside its button. The rail uses wrap height so a larger gap does not clip.
     * Half-article rows do not have these ids, so this is a no-op there.
     */
    public static void applyFullSideActionGap(Context context, View convertView) {
        if (context == null || convertView == null) {
            return;
        }
        int gapPx = Math.round(readFullSideActionGapDp(context)
                * context.getResources().getDisplayMetrics().density);
        boolean touched = false;
        for (int id : FULL_SIDE_ACTION_IDS) {
            View action = convertView.findViewById(id);
            if (action == null) {
                continue;
            }
            ViewGroup.LayoutParams lp = action.getLayoutParams();
            if (!(lp instanceof ViewGroup.MarginLayoutParams)) {
                continue;
            }
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) lp;
            if (mlp.topMargin != gapPx) {
                mlp.topMargin = gapPx;
                action.setLayoutParams(mlp);
            }
            touched = true;
        }
        if (!touched) {
            return;
        }
        View rail = convertView.findViewById(R.id.video_side_menu);
        if (rail == null) {
            return;
        }
        ViewGroup.LayoutParams railLp = rail.getLayoutParams();
        if (railLp != null && railLp.height != ViewGroup.LayoutParams.WRAP_CONTENT) {
            railLp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            rail.setLayoutParams(railLp);
        }
    }

    /**
     * Packs like / comment / share / speaker / search / more to the end of the
     * row with the admin horizontal gap. The row already has a weighted spacer,
     * so a smaller gap pulls the first icon off the share-link overlay instead
     * of stretching the icons edge to edge. A post-layout pass shrinks the gap
     * further (down to 0) if the cluster would still cover that link.
     */
    private static void applyActionIconHorizontalGap(Context context, View convertView, float density) {
        if (context == null || convertView == null) {
            return;
        }
        int gapPx = Math.round(readActionIconGapDp(context) * density);
        int endInsetPx = Math.round(ACTION_ICON_END_INSET_DP * density);
        View lastVisible = null;
        for (int id : HALF_ACTION_SLOT_IDS) {
            View slot = convertView.findViewById(id);
            if (slot == null) continue;
            ViewGroup.LayoutParams lp = slot.getLayoutParams();
            if (!(lp instanceof ViewGroup.MarginLayoutParams)) continue;
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) lp;
            mlp.setMarginEnd(gapPx);
            slot.setLayoutParams(mlp);
            if (slot.getVisibility() != View.GONE) {
                lastVisible = slot;
            }
        }
        if (lastVisible != null && lastVisible.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) lastVisible.getLayoutParams();
            mlp.setMarginEnd(endInsetPx);
            lastVisible.setLayoutParams(mlp);
        }
        keepActionIconsClearOfShareLink(convertView, density, gapPx);
    }

    /** Shrink inter-icon gaps so the first icon stays to the right of the share link. */
    private static void keepActionIconsClearOfShareLink(View convertView, float density, int gapPx) {
        if (convertView == null || gapPx <= 0) {
            return;
        }
        final View link = convertView.findViewById(R.id.textViewWebUrl);
        final View row = convertView.findViewById(R.id.with_out_strips_layout);
        if (link == null || row == null || link.getVisibility() != View.VISIBLE) {
            return;
        }
        row.post(() -> {
            if (!link.isAttachedToWindow() || !row.isAttachedToWindow()) return;
            if (link.getWidth() <= 0 || row.getWidth() <= 0) return;
            View first = null;
            View last = null;
            int visible = 0;
            for (int id : HALF_ACTION_SLOT_IDS) {
                View slot = row.findViewById(id);
                if (slot == null) {
                    slot = convertView.findViewById(id);
                }
                if (slot == null || slot.getVisibility() == View.GONE || slot.getWidth() <= 0) {
                    continue;
                }
                if (first == null) first = slot;
                last = slot;
                visible++;
            }
            if (first == null || last == null || visible < 2) return;
            int[] linkLoc = new int[2];
            int[] firstLoc = new int[2];
            link.getLocationOnScreen(linkLoc);
            first.getLocationOnScreen(firstLoc);
            int minLeft = linkLoc[0] + link.getWidth() + Math.round(SHARE_LINK_CLEARANCE_DP * density);
            int overlap = minLeft - firstLoc[0];
            if (overlap <= 0) return;
            int gapCount = visible - 1;
            int reducePer = (int) Math.ceil(overlap / (float) gapCount);
            int newGap = Math.max(0, gapPx - reducePer);
            if (newGap >= gapPx) return;
            for (int id : HALF_ACTION_SLOT_IDS) {
                View slot = convertView.findViewById(id);
                if (slot == null || slot.getVisibility() == View.GONE || slot == last) continue;
                ViewGroup.LayoutParams lp = slot.getLayoutParams();
                if (!(lp instanceof ViewGroup.MarginLayoutParams)) continue;
                ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) lp;
                if (mlp.getMarginEnd() == newGap) continue;
                mlp.setMarginEnd(newGap);
                slot.setLayoutParams(mlp);
            }
        });
    }

    /**
     * Speaker ring scales to {@link ArticleTtsHelper#PULSE_MAX_SCALE}. Size it to the
     * icon slot and keep enough padding that the full circle stays out of the headline.
     * Ancestors above a photo must not clip the top of that ring.
     */
    private static void ensureHalfTtsPulseVisible(View convertView, int slotPx) {
        View pulse = convertView.findViewById(R.id.half_tts_pulse);
        if (pulse == null || slotPx <= 0) {
            return;
        }
        ViewGroup.LayoutParams pulseLp = pulse.getLayoutParams();
        if (pulseLp != null && (pulseLp.width != slotPx || pulseLp.height != slotPx)) {
            pulseLp.width = slotPx;
            pulseLp.height = slotPx;
            pulse.setLayoutParams(pulseLp);
        }
        float density = convertView.getResources().getDisplayMetrics().density;
        int overflow = Math.round(slotPx * (ArticleTtsHelper.PULSE_MAX_SCALE - 1f) / 2f)
                + Math.round(2f * density);
        View row = convertView.findViewById(R.id.with_out_strips_layout);
        if (row != null) {
            // Photo cards: top of the ring draws over the image, so only the bottom
            // needs padding (keeps the headline clear). Poll rows pad both sides.
            boolean underPhoto = convertView.findViewById(R.id.layoutImageHeight) != null;
            int padTop = underPhoto ? row.getPaddingTop() : Math.max(row.getPaddingTop(), overflow);
            int padBottom = Math.max(row.getPaddingBottom(), overflow);
            if (padTop != row.getPaddingTop() || padBottom != row.getPaddingBottom()) {
                row.setPadding(row.getPaddingLeft(), padTop, row.getPaddingRight(), padBottom);
            }
            clearClipForTtsPulse(row);
        } else {
            clearClipForTtsPulse(pulse);
        }
    }

    /** Let the scaled ring paint over the photo; stop at the card root or poll card. */
    private static void clearClipForTtsPulse(View start) {
        View current = start;
        for (int i = 0; i < 8 && current != null; i++) {
            if (current instanceof ViewGroup) {
                ViewGroup group = (ViewGroup) current;
                group.setClipChildren(false);
                group.setClipToPadding(false);
            }
            ViewParent parent = current.getParent();
            if (!(parent instanceof ViewGroup)) {
                break;
            }
            ViewGroup group = (ViewGroup) parent;
            int id = group.getId();
            if (id == R.id.rl_question || id == R.id.rl_survay_main) {
                break;
            }
            group.setClipChildren(false);
            group.setClipToPadding(false);
            if (id == R.id.relative_half_contents
                    || id == R.id.layoutMain
                    || id == R.id.parent_layout) {
                break;
            }
            current = group;
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

    /** Same compact total the full-post WhatsApp caption uses. */
    public static String formatEngagementCount(int c) {
        return formatCount(c);
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
        bindFullPostLikeCount(holder.full_img_whtsapp, list.get(position).getWhatsappShares(), null);
        rememberLikeCountView(list.get(position).getPostId(), holder.full_img_like);
        rememberCommentCountView(list.get(position).getPostId(), holder.full_img_comment);
        rememberShareCountView(list.get(position).getPostId(), holder.full_img_share);
        rememberWhatsappShareCountView(list.get(position).getPostId(), holder.full_img_whtsapp);
    }

    private void bindFullVideoEngagementCounts(TextView likeView, TextView commentView, TextView shareView, int position) {
        bindFullVideoEngagementCounts(likeView, commentView, shareView, null, position);
    }

    private void bindFullVideoEngagementCounts(TextView likeView, TextView commentView, TextView shareView,
                                               TextView whatsappView, int position) {
        bindFullPostLikeCount(likeView, list.get(position).getLikes(), getLikeLabel());
        bindFullPostLikeCount(commentView, list.get(position).getComments(), getCommentLabel());
        bindFullPostLikeCount(shareView, list.get(position).getShares(), getShareLabel());
        bindFullPostLikeCount(whatsappView, list.get(position).getWhatsappShares(), null);
        rememberLikeCountView(list.get(position).getPostId(), likeView);
        rememberCommentCountView(list.get(position).getPostId(), commentView);
        rememberShareCountView(list.get(position).getPostId(), shareView);
        rememberWhatsappShareCountView(list.get(position).getPostId(), whatsappView);
    }

    /** Hide on-post magazine/Special Share — Play Store uses footer middle Share. */
    private void hideOnPostMagazineShare(ImageView shareIcon) {
        if (shareIcon == null) {
            return;
        }
        // Clear blink first — infinite scale/alpha can keep painting over Close if
        // visibility is flipped while the animation is still attached.
        shareIcon.clearAnimation();
        shareIcon.setOnClickListener(null);
        shareIcon.setClickable(false);
        shareIcon.setFocusable(false);
        shareIcon.setVisibility(GONE);
    }

    /**
     * Show All/Single share sheet and keep on-post Share hidden (footer Share owns it).
     */
    private void openOnPostShareSheet(@Nullable RelativeLayout shareSheet,
                                      @Nullable ImageView... shareIcons) {
        if (shareIcons != null) {
            for (ImageView icon : shareIcons) {
                hideOnPostMagazineShare(icon);
            }
        }
        if (shareSheet == null) {
            return;
        }
        shareSheet.setVisibility(VISIBLE);
        shareSheet.setClickable(true);
        try {
            float density = context.getResources().getDisplayMetrics().density;
            shareSheet.setElevation(16f * density);
            shareSheet.bringToFront();
            android.view.ViewParent parent = shareSheet.getParent();
            if (parent instanceof ViewGroup) {
                ((ViewGroup) parent).bringChildToFront(shareSheet);
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Posts that use Play Store footer-middle Share: true magazines, Magazines-cat
     * specials, and image_gallery pages while the Special tab is active.
     */
    private boolean shouldUseMagazineFooterShare(@Nullable ModelClass.Datum item) {
        if (item == null) {
            return false;
        }
        if (HomePage.usesMagazineGlowShare(item)) {
            return true;
        }
        if (!isSpecialModeNow()) {
            return false;
        }
        String type = item.getPostType();
        if (type == null) {
            return false;
        }
        type = type.trim();
        return type.equalsIgnoreCase("image_gallery")
                || type.equalsIgnoreCase("magazine")
                || type.equalsIgnoreCase("magazines");
    }

    /** After All/Single sheet closes, keep on-post Share hidden (footer owns it). */
    private void restoreOnPostMagazineShare(ImageView shareIcon, ModelClass.Datum item,
                                            View.OnClickListener clickListener) {
        hideOnPostMagazineShare(shareIcon);
    }

    private int getVideoLayoutTopHeight() {
        if (Build.VERSION.SDK_INT >= 35) {
            return layoutHeight - 85;
        }
        return layoutHeight;
    }

    /**
     * Optimistically update like icon + delta on the last known feed total, then
     * fire Update_Like. The API response always overwrites the count via
     * {@link #syncLikeCountFromServer} so a stale feed total cannot stick after
     * the round-trip (prefer server total for like and unlike).
     * Returns the new likeStatus so the caller can update icon immediately.
     * type: 1=like, 2=dislike, 0=remove
     */
    private int applyLikeChange(int position, TextView countView) {
        if (list == null || position < 0 || position >= list.size()) return 0;
        ModelClass.Datum item = list.get(position);
        int oldStatus = item.getLikeStatus() != null ? item.getLikeStatus() : 0;
        // Base on last known good from feed / prior server sync — never invent
        // a count from likeStatus alone.
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

        rememberLikeCountView(item.getPostId(), countView);
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
        syncEngagementFromServer(postId, subPostId, serverLikes, serverComments, serverShares, null);
    }

    private void syncEngagementFromServer(Long postId, Long subPostId, Integer serverLikes,
                                          Integer serverComments, Integer serverShares,
                                          Integer serverWhatsappShares) {
        if (postId == null || list == null) return;
        boolean matched = false;
        for (int i = 0; i < list.size(); i++) {
            ModelClass.Datum item = list.get(i);
            if (item.getPostId() == null || !item.getPostId().equals(postId)) continue;
            // Prefer exact sub_post match; if client/server omit/zero sub_post_id, still update.
            if (subPostId != null && item.getSubPostId() != null
                    && subPostId != 0L && item.getSubPostId() != 0L
                    && !subPostId.equals(item.getSubPostId())) {
                continue;
            }
            if (serverLikes != null) item.setLikes(serverLikes);
            if (serverComments != null) item.setComments(serverComments);
            if (serverShares != null) item.setShares(serverShares);
            if (serverWhatsappShares != null) item.setWhatsappShares(serverWhatsappShares);
            matched = true;
            // Always in-place TextView paint — FlipView notify recycles and flashes
            // the visible card (video / ads / black ads_screen). Never settle-notify
            // for engagement-only updates; model is already correct for next bind.
            if (serverLikes != null) {
                TextView countView = findRememberedLikeCountView(postId);
                if (countView == null) {
                    countView = findLiveEngagementTextView(postId, R.id.half_like_count,
                            R.id.full_img_like_text, R.id.full_video_like_text);
                    if (countView != null) {
                        rememberLikeCountView(postId, countView);
                    }
                }
                if (countView != null) {
                    bindLikeCountForView(countView, serverLikes);
                }
            }
            if (serverComments != null) {
                applyCommentCountToRememberedView(postId, serverComments);
            }
            if (serverShares != null) {
                applyShareCountToRememberedView(postId, serverShares);
            }
            if (serverWhatsappShares != null) {
                applyWhatsappShareCountToRememberedView(postId, serverWhatsappShares);
            }
            break;
        }
        if (!matched) {
            showLog("syncEngagementFromServer: no list match postId=" + postId);
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
        applyCommentCountToRememberedView(item.getPostId(), item.getComments());
    }

    /** Bump comment count by post ids (preferred when Flip index may be stale). */
    public void incrementCommentCountForPost(Long postId, Long subPostId) {
        if (postId == null || list == null) return;
        for (int i = 0; i < list.size(); i++) {
            ModelClass.Datum item = list.get(i);
            if (item.getPostId() == null || !item.getPostId().equals(postId)) continue;
            if (subPostId != null && item.getSubPostId() != null
                    && subPostId != 0L && item.getSubPostId() != 0L
                    && !subPostId.equals(item.getSubPostId())) {
                continue;
            }
            int comments = item.getComments() != null ? item.getComments() : 0;
            item.setComments(comments + 1);
            applyCommentCountToRememberedView(postId, item.getComments());
            return;
        }
    }

    public void syncCommentCountFromServer(Long postId, Long subPostId, Integer serverComments) {
        syncCommentCountFromServer(postId, subPostId, serverComments, false);
    }

    /**
     * Update the in-memory comment badge from a list/add response.
     * Never wipe a positive local count down to 0 unless {@code allowZero} —
     * empty/failed sheet loads previously cleared the engagement row after close.
     */
    public void syncCommentCountFromServer(Long postId, Long subPostId,
                                           Integer serverComments, boolean allowZero) {
        if (serverComments == null) return;
        if (!allowZero && serverComments <= 0) {
            if (postId != null && list != null) {
                for (int i = 0; i < list.size(); i++) {
                    ModelClass.Datum item = list.get(i);
                    if (item.getPostId() == null || !item.getPostId().equals(postId)) continue;
                    Integer existing = item.getComments();
                    if (existing != null && existing > 0) {
                        applyCommentCountToRememberedView(postId, existing);
                        return;
                    }
                    break;
                }
            }
        }
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
        /**
         * Main-feed Flash News chip: open the same Redis Flash_News flip as
         * Daily Updates Flash.
         */
        void openFlashNewsFromFeedChip();
        /**
         * Feed video autoplay: current video finished at {@code position}.
         * HomePage should smooth-flip to next (no wrap) after a short delay.
         */
        void onVideoAutoplayEndedRequestNext(int position);
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
            if (album == null) {
                return position + 1L;
            }
            if (album.getPostId() != null && album.getPostId() > 0) {
                return album.getPostId();
            }
            // Ad / share shells often share null/0 postId — synthesize a stable id so
            // FlipView dataSetChanged keeps the current page (avoids jump-to-0 hitch).
            Integer adPos = album.getAd_req_position();
            if (adPos != null) {
                return 0x4A000000L ^ (adPos.longValue() & 0xffffffffL);
            }
            String type = album.getPostType();
            if (type != null && !type.isEmpty()) {
                return 0x50000000L ^ (((long) type.hashCode()) << 16) ^ (position + 1L);
            }
            return position + 1L;
        } catch (Exception e) {
            e.printStackTrace();
            return position + 1L;
        }
    }


    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (list == null || position < 0 || position >= list.size()) {
            // Past adapter range: never invent a "no more news" page in search —
            // reuse the Continue-reading hub (or a gone scrap if hub already last).
            if (isSearchFeedEndHub()) {
                return inflateFeedEndCard(parent, convertView, true);
            }
            if (list != null && FeedEndHelper.hasAtEnd(list)) {
                View empty = new View(context);
                empty.setVisibility(View.GONE);
                return empty;
            }
            // Never return a blank white scrap past the adapter — show end card.
            return inflateFeedEndCard(parent, convertView, false);
        }
        ModelClass.Datum pageDatum = list.get(position);
        if (pageDatum == null) {
            showLog("getView null datum at pos=" + position);
            if (isSearchFeedEndHub()) {
                return inflateFeedEndCard(parent, convertView, true);
            }
            return inflateFeedEndCard(parent, convertView, false);
        }
        String pagePostType = pageDatum.getPostType();
        if (BuildConfig.DEBUG) {
            showLog("getView pos=" + position + " type=" + pagePostType
                    + " postId=" + pageDatum.getPostId());
        }
        customConvertView=convertView;
        customPosition=position;

        // Special defense-in-depth: never bind half News cards in Special mode.
        if (isHalfNewsTypeForbiddenInSpecial(pagePostType)) {
            showLog("getView rejected half type in Special pos=" + position
                    + " type=" + pagePostType);
            return inflateFeedEndCard(parent, convertView, false);
        }

        // Search Continue-reading hub is ONLY for the feed_end sentinel —
        // never for blank/unknown real hits (that painted hub on every page).
        if (FeedEndHelper.isFeedEndType(pagePostType)
                || FeedEndHelper.isFeedEnd(pageDatum)) {
            return inflateFeedEndCard(parent, convertView, true);
        }

        // Keyword search: never bind mid-list native / vast ad shells.
        if (isSearchFeedEndHub()
                && ReadArticleStore.isAdFeedSlot(pageDatum)) {
            showLog("getView search strip ad shell pos=" + position
                    + " type=" + pagePostType);
            try {
                list.remove(position);
                if (position < list.size()) {
                    return getView(position, null, parent);
                }
            } catch (Exception ignored) {
            }
            return inflateFeedEndCard(parent, convertView, true);
        }

        if (pagePostType == null || pagePostType.trim().isEmpty()) {
            pagePostType = inferPostTypeForFlip(list.get(position));
            if (pagePostType != null) {
                try {
                    list.get(position).setPostType(pagePostType);
                } catch (Exception ignored) {
                }
            }
            showLog("getView inferred post_type=" + pagePostType
                    + " at pos=" + position
                    + " postId=" + list.get(position).getPostId());
        }
        if (pagePostType == null || pagePostType.trim().isEmpty()) {
            // Still unknown — never paint Continue-reading for a real post id.
            Long pid = list.get(position).getPostId();
            if (pid != null && pid > 0 && !FeedEndHelper.isFeedEnd(list.get(position))) {
                try {
                    list.get(position).setPostType("normal_image");
                } catch (Exception ignored) {
                }
                return getView(position, null, parent);
            }
            if (isSearchFeedEndHub()) {
                showLog("getView search blank → hub pos=" + position);
                return inflateFeedEndCard(parent, convertView, true);
            }
            showLog("getView blank page: null/empty post_type at pos=" + position
                    + " postId=" + list.get(position).getPostId());
            return inflateFeedEndCard(parent, convertView, false);
        }
        // Greetings / aliases List_All morphs server-side; Search_Stories may not.
        if ("greetings".equalsIgnoreCase(pagePostType)
                || "greeting".equalsIgnoreCase(pagePostType)) {
            pagePostType = "full_image";
            try {
                list.get(position).setPostType(pagePostType);
            } catch (Exception ignored) {
            }
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
                        // Do not stop a shared player already preparing/playing this same
                        // card — notifyDataSetChanged rebinds scrap and used to seek/stop,
                        // leaving spinner then the center play button again.
                        if (!isSharedExoActiveFor(position)) {
                            pauseVideo();
                        }
                        fullscreen = false;
                        //media_type: 5 => full normal video
                        //media_type: 6 => full youtube video
                        fromWhatsapp = false;
                        String mediaType = list.get(position).getMediaType();
                        String fullVideoUrl = list.get(position).getVideoUrl();
                        boolean fullLooksYoutube = YouTubeIdHelper.isYouTubeish(fullVideoUrl)
                                || YouTubeIdHelper.extract(fullVideoUrl) != null;
                        if ("5".equals(mediaType) && !fullLooksYoutube) {
                            full_videoViewHolder = new FullVideoViewHolder();
                            // Reuse scrap while the shared ExoPlayer is live on this card —
                            // inflate-always destroyed the video surface and flashed white/spinner.
                            String fullExoKey =
                                    "full_video_exo_" + FeedVideoPlayerConfig.layoutModeKey(context);
                            if (!canReuseFlipView(convertView, fullExoKey)) {
                                convertView = inflater.inflate(
                                        FeedVideoPlayerConfig.fullVideoLayout(context), parent, false);
                                markFlipViewType(convertView, fullExoKey);
                            }
                            showFullVideoWithExoPlayer(position, convertView);
                        }
                        // full youtube video (media_type 6, or 4 wrongly stored, or YouTube URL)
                        else if ("6".equals(mediaType) || "4".equals(mediaType) || fullLooksYoutube) {
                            fullVideoNewYoutubeViewHolder = new FullVideoNewYoutubeViewHolder();
                            convertView = inflater.inflate(R.layout.youtube_full_videos_new, parent, false);
                            showFullVideoWithYoutube(position, convertView);

                        }
                        // Fallback: treat unknown media as YouTube full when URL is empty —
                        // prefer exo layout only when we clearly have a file stream.
                        else {
                            full_videoViewHolder = new FullVideoViewHolder();
                            String fullExoKeyFallback =
                                    "full_video_exo_" + FeedVideoPlayerConfig.layoutModeKey(context);
                            if (!canReuseFlipView(convertView, fullExoKeyFallback)) {
                                convertView = inflater.inflate(
                                        FeedVideoPlayerConfig.fullVideoLayout(context), parent, false);
                                markFlipViewType(convertView, fullExoKeyFallback);
                            }
                            showFullVideoWithExoPlayer(position, convertView);
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
                            boolean teluguHalf = AppHelper.isTeluguLanguageSelected();
                            String halfExoKey = (teluguHalf
                                    ? "normal_video_exo_telugu_"
                                    : "normal_video_exo_")
                                    + FeedVideoPlayerConfig.layoutModeKey(context);
                            // Reuse scrap while shared Exo is active — new inflate tears the
                            // video surface and flashes a white clear / buffering ring.
                            if (!canReuseFlipView(convertView, halfExoKey)) {
                                convertView = inflater.inflate(
                                        FeedVideoPlayerConfig.halfExoLayout(context, teluguHalf),
                                        parent,
                                        false);
                                markFlipViewType(convertView, halfExoKey);
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
                    case "magazines":
                        pauseVideo();
                        image_magazineHolder = new ImageMagazineHolder();
                        convertView = inflater.inflate(R.layout.activity_magazine_detailes, parent, false);
                        image_magazineHolder.magzine_viewPager_data = convertView.findViewById(R.id.magzine_viewPager_data);
                        image_magazineHolder.magzine_ll_dots = convertView.findViewById(R.id.magzine_ll_dots);
                        image_magazineHolder.magzine_indicator = convertView.findViewById(R.id.magzine_indicator);
                        final RelativeLayout magzine_relative_slider = convertView.findViewById(R.id.magzine_relative_slider);
                        ImageView imageViewShareMag = convertView.findViewById(R.id.imageViewShare);
                        // Play Store: footer-middle blinking Share; hide on-post corner Share.
                        if (imageViewShareMag != null) {
                            hideOnPostMagazineShare(imageViewShareMag);
                        }
                        final RelativeLayout relativeLayoutShareOpt = convertView.findViewById(R.id.relativeLayoutShareOpt);
                        final TextView textViewAllPage = convertView.findViewById(R.id.textViewAllPage);
                        final TextView textViewSinglePage = convertView.findViewById(R.id.textViewSinglePage);
                        final ImageView imageViewShareClose = convertView.findViewById(R.id.imageViewShareClose);
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
                        //imageGallaryHolder.galleryLayout.setLayerType(View.LAYER_TYPE_NONE,null);
                        relativeLayoutShareOpt.setVisibility(GONE);
                        imageViewShareClose.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                relativeLayoutShareOpt.setVisibility(GONE);
                                hideOnPostMagazineShare(imageViewShareMag);
                                fromWhatsapp = false;
                                image_magazineHolder.magzine_ll_dots.setVisibility(VISIBLE);
                            }
                        });
                        textViewAllPage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                relativeLayoutShareOpt.setVisibility(GONE);
                                hideOnPostMagazineShare(imageViewShareMag);
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
                            Object magBg =
                                    offlineAwareGlideModel(
                                            list.get(position),
                                            list.get(position).getBackgroundImage());
                            if (magBg != null) {
                                // LOW so CardPager page-0 IMMEDIATE cover wins the Glide pool.
                                Glide.with(context).load(magBg)
                                        .apply(flipGlideDecodeOptions())
                                        .priority(Priority.LOW)
                                        .into(new SimpleTarget<Drawable>() {
                                    @Override
                                    public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                                        magzine_relative_slider.setBackground(resource);
                                    }
                                });
                            }
                            ArrayList<String> magImages =
                                    GalleryImageUrls.sanitize(
                                            OfflineImageStore.resolveGalleryUris(
                                                    context,
                                                    list.get(position).getPostId(),
                                                    list.get(position).getImages()));
                            // Empty gallery → ViewPager getCount=0 + ShadowTransformer
                            // getCardViewAt(0) IndexOOB → process death on share open /
                            // magazine bind (looks like “automatically back”).
                            if (magImages.isEmpty()) {
                                magImages.add("");
                                android.util.Log.e("MagazineCrash",
                                        "magazine empty images → placeholder postId="
                                                + list.get(position).getPostId());
                            }
                            TextView magSwipeHint =
                                    convertView.findViewById(R.id.gallery_swipe_left_hint);
                            // Magazines / post_type magazine: always CardPager carousel (live /
                            // pre-e23bbb9). SpecialGalleryFlipHelper FIT_CENTER letterboxed
                            // posters into a black bar with dots below the image.
                            mCardAdapter = new CardPagerAdapter(context, list.get(position).getStrip_content(), new CardPagerAdapter.shareMagazine() {
                                @Override
                                public void onClickShareMagzine(CardView cardView, ImageView imageView1) {
                                    fromWhatsapp = false;
                                    adapterCardView = cardView;
                                    openOnPostShareSheet(relativeLayoutShareOpt, imageViewShareMag);
                                }
                            });
                            for (int i = 0; i < magImages.size(); i++) {
                                mCardAdapter.addCardItem(new CardItem("" + magImages.get(i), "" + "" + magImages.get(i), list.get(position).getShareLink(), displayCopy(list.get(position).getTitle()), list.get(position).getPostId()));
                            }
                            mCardShadowTransformer = new ShadowTransformer(image_magazineHolder.magzine_viewPager_data, mCardAdapter);
                            mCardShadowTransformer.enableScaling(true);
                            image_magazineHolder.magzine_viewPager_data.setVisibility(VISIBLE);
                            image_magazineHolder.magzine_viewPager_data.setAdapter(mCardAdapter);
                            // enableScaling() runs before instantiateItem, so apply the live
                            // 1.1 scale after pages exist — card grows into bottom padding and
                            // dots overlay the poster (not a black gap below it).
                            final CardPagerAdapter magPagerAdapter = mCardAdapter;
                            final ViewPager magPager =
                                    image_magazineHolder.magzine_viewPager_data;
                            magPager.post(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        int cur = magPager.getCurrentItem();
                                        CardView card = magPagerAdapter.getCardViewAt(cur);
                                        if (card != null) {
                                            card.setScaleX(1.1f);
                                            card.setScaleY(1.1f);
                                        }
                                    } catch (Exception ignored) {
                                    }
                                }
                            });
                            // Cover Glide can lose the first-open race (transparent stub);
                            // neighbors often already in memory — re-kick page-0 after layout.
                            CardPagerAdapter.schedulePage0Ensure(magPager, magPagerAdapter);
                            // Share deep-link ?page=N → land magazine ViewPager on that slide.
                            // Peek only; settle runs AFTER setViewPager so a second bind
                            // or the indicator cannot leave the pager on slide 0.
                            int magLand = -1;
                            try {
                                magLand = applyPendingShareGalleryPage(
                                        list.get(position).getPostId(),
                                        convertView,
                                        magPager,
                                        null);
                            } catch (Exception ignored) {
                            }
                            // Same Play Store WormDots + gallery_strip as image_gallery / half gallery.
                            if (image_magazineHolder.magzine_ll_dots != null) {
                                image_magazineHolder.magzine_ll_dots.setVisibility(
                                        mCardAdapter.getCount() > 1 ? VISIBLE : GONE);
                            }
                            if (image_magazineHolder.magzine_indicator != null) {
                                try {
                                    image_magazineHolder.magzine_indicator.setViewPager(
                                            image_magazineHolder.magzine_viewPager_data);
                                    image_magazineHolder.magzine_indicator.setVisibility(
                                            mCardAdapter.getCount() > 1 ? VISIBLE : GONE);
                                } catch (Exception e) {
                                    android.util.Log.e("MagazineCrash",
                                            "magzine WormDots setViewPager: " + e.getMessage());
                                }
                            }
                            if (magLand >= 0) {
                                final int landSafe = Math.max(0, Math.min(
                                        magLand, Math.max(0, mCardAdapter.getCount() - 1)));
                                try {
                                    magPager.setCurrentItem(landSafe, false);
                                } catch (Exception ignored) {
                                }
                                scheduleShareGallerySettle(
                                        list.get(position).getPostId(), landSafe, magPager);
                            }
                            GallerySwipeHintHelper.bind(
                                    image_magazineHolder.magzine_viewPager_data,
                                    magSwipeHint,
                                    mCardAdapter != null ? mCardAdapter.getCount() : 0);

                        } catch (Exception e) {
                        }
                        break;
                    case "ad":
                        // Keyword search FlipView: never paint / fill mid-list natives.
                        if (isSearchFeedEndHub()) {
                            showLog("AD Point Adapter:: SKIP search mode pos=" + position);
                            try {
                                if (list != null && position >= 0 && position < list.size()
                                        && ReadArticleStore.isAdFeedSlot(list.get(position))) {
                                    list.remove(position);
                                    if (position < list.size()) {
                                        return getView(position, null, parent);
                                    }
                                }
                            } catch (Exception ignored) {
                            }
                            return inflateFeedEndCard(parent, convertView, true);
                        }
                        // Keep ads_screen / fb shell so late fills paint in place.
                        // Never swap the whole page to share_app_layout on a miss —
                        // that caused Share lag and forced FlipView recycle (blink risk).
                        // Pool: exact reqPos → shared AdPreloader → idle orphan/ahead
                        // (distinct creative only). Never reuse last seat via nativeAd field
                        // or same responseId. No ProgressBar — house shell until ready.
                        showLog("AD Point Adapter:: START");
                        if (hasActiveVideoPlayer()) {
                            pauseVideo();
                        }
                        adsScreenViewHolder = new AdsScreenViewHolder();
                        // HomePage keeps bottom nav on native in-feed ads; only hides
                        // for share/ratings or true interstitial format.
                        adapterCallback.closeBottomStrip(true);
                        String adTypeAtPos = list.get(position).getAd_type();
                        if (adTypeAtPos == null || adTypeAtPos.trim().isEmpty()) {
                            adTypeAtPos = "google";
                        }
                        showLog("HomePage getAd_type::" + adTypeAtPos);
                        cancelAdShellFillTimeout(position);
                        // Do not cancel parked fill watchdog here — buffer scrap getView
                        // used to abort the visible seat's poll and leave house forever.
                        // Scrap reuse: keep ads_screen shell, but never seat-sticky the
                        // same impressed creative on flip-back (July / Play Store path).
                        String adReuseKey = "ad_" + adTypeAtPos.toLowerCase(java.util.Locale.US);
                        if (adTypeAtPos.equalsIgnoreCase("google")
                                || adTypeAtPos.equalsIgnoreCase("admanager")) {
                            if (canReuseFlipView(convertView, adReuseKey)) {
                                FrameLayout existingFl =
                                        convertView.findViewById(R.id.fl_adplaceholder);
                                if (existingFl != null) {
                                    customConvertView = convertView;
                                    try {
                                        View lpRoot = convertView.findViewById(R.id.layoutParent);
                                        if (lpRoot != null && lpRoot.getLayoutParams() != null
                                                && lpRoot.getLayoutParams().height != screenHeight) {
                                            lpRoot.getLayoutParams().height = screenHeight;
                                        }
                                    } catch (Exception ignored) {
                                    }
                                    if (existingFl.getVisibility() != VISIBLE) {
                                        existingFl.setVisibility(VISIBLE);
                                    }
                                    stripAdSeatSpinners(existingFl);
                                    // Rebind every opportunity — bindGoogle keeps live
                                    // MediaView only while a packed creative still exists.
                                    bindGoogleAdIntoPlaceholder(
                                            existingFl, convertView, position);
                                    showLog("AD Point Adapter:: END seat-reuse");
                                    break;
                                }
                            }
                            convertView = inflater.inflate(R.layout.ads_screen, parent, false);
                            markFlipViewType(convertView, adReuseKey);
                            customConvertView = convertView;
                            final RelativeLayout layoutParent =
                                    convertView.findViewById(R.id.layoutParent);
                            layoutParent.getLayoutParams().height = screenHeight;
                            // July ads_screen uses form_surface_elevated — keep theme chrome
                            // so title/CTA footer stay readable (do not force #121212 on host).
                            final FrameLayout fl_adplaceholder =
                                    convertView.findViewById(R.id.fl_adplaceholder);
                            fl_adplaceholder.setVisibility(VISIBLE);
                            // Dark underlay only inside the placeholder until creative/Share.
                            ensureDarkAdUnderlay(fl_adplaceholder);
                            bindGoogleAdIntoPlaceholder(
                                    fl_adplaceholder, convertView, position);
                        } else {
                            if (canReuseFlipView(convertView, adReuseKey)) {
                                NativeAdLayout existingFb =
                                        convertView.findViewById(R.id.native_ad_container);
                                if (existingFb != null) {
                                    customConvertView = convertView;
                                    convertView.setBackgroundColor(0xFF121212);
                                    existingFb.setBackgroundColor(0xFF121212);
                                    bindFacebookAdIntoLayout(
                                            existingFb, convertView, position);
                                    showLog("AD Point Adapter:: END fb scrap-reuse");
                                    break;
                                }
                            }
                            convertView = inflater.inflate(R.layout.fb_ad_post, parent, false);
                            markFlipViewType(convertView, adReuseKey);
                            customConvertView = convertView;
                            convertView.setBackgroundColor(0xFF121212);
                            NativeAdLayout nativeAdLayout =
                                    convertView.findViewById(R.id.native_ad_container);
                            nativeAdLayout.setBackgroundColor(0xFF121212);
                            ensureDarkAdUnderlay(nativeAdLayout);
                            bindFacebookAdIntoLayout(
                                    nativeAdLayout, convertView, position);
                        }
                        showLog("AD Point Adapter:: END");
                        break;
                    case "ratings":
                        // Copy + CTA only — no in-app stars (Play forbids pre-selecting them).
                        pauseVideo();
                        ratingScreenViewHolder = new RatingScreenViewHolder();
                        convertView = inflater.inflate(R.layout.rating_screen_layout, parent, false);
                        Button buttonRateOnPlay = convertView.findViewById(R.id.buttonAppSubmit);
                        TextView textViewRatSubTitle = convertView.findViewById(R.id.textViewRatSubTitle);
                        TextView textViewRatingTitle = convertView.findViewById(R.id.textViewRatingTitle);
                        // Never park on "Thank you for your feedback" for this CTA.
                        if (textViewRatSubTitle != null) {
                            textViewRatSubTitle.setVisibility(GONE);
                        }
                        boolean isAppRatingSubmitted = SharedPrefsData.getBool(
                                context, Constants.APP_RATING_SUBMITTED, Constants.PREF_NAME);
                        if (isAppRatingSubmitted) {
                            // Already acted — drop the card so the user is not stuck on thank-you.
                            if (textViewRatingTitle != null) {
                                textViewRatingTitle.setVisibility(GONE);
                            }
                            if (buttonRateOnPlay != null) {
                                buttonRateOnPlay.setVisibility(GONE);
                            }
                            final int ratingsPos = position;
                            convertView.post(() -> removeRatingsCardAt(ratingsPos));
                        } else {
                            if (textViewRatingTitle != null) {
                                textViewRatingTitle.setVisibility(VISIBLE);
                            }
                            if (buttonRateOnPlay != null) {
                                buttonRateOnPlay.setVisibility(VISIBLE);
                                buttonRateOnPlay.setOnClickListener(view -> {
                                    SharedPrefsData.putBool(
                                            context, Constants.APP_RATING_SUBMITTED, true, Constants.PREF_NAME);
                                    ReadArticleStore.markRatingsPromptSeen(context);

                                    Activity host = activity;
                                    if (host == null && context instanceof Activity) {
                                        host = (Activity) context;
                                    }
                                    // Primary: open Play Store listing. Do not swap UI to thank-you.
                                    boolean openedStore = false;
                                    if (host != null && !host.isFinishing()) {
                                        openedStore = PlayInAppReviewHelper.openPlayStoreListing(host);
                                        if (!openedStore) {
                                            PlayInAppReviewHelper.launch(host, null);
                                        }
                                    } else {
                                        try {
                                            final String appPackageName = context.getPackageName();
                                            context.startActivity(new Intent(Intent.ACTION_VIEW,
                                                    Uri.parse("market://details?id=" + appPackageName)));
                                            openedStore = true;
                                        } catch (android.content.ActivityNotFoundException anfe) {
                                            try {
                                                context.startActivity(new Intent(Intent.ACTION_VIEW,
                                                        Uri.parse("https://play.google.com/store/apps/details?id="
                                                                + context.getPackageName())));
                                                openedStore = true;
                                            } catch (Exception ignored) {
                                            }
                                        }
                                    }
                                    // Leave the prompt; remove so return from Play is not thank-you.
                                    removeRatingsCardAt(position);
                                    showLog("rate_on_play openedStore=" + openedStore);
                                });
                            }
                        }
                        break;
                    case "share_app":
                        convertView = inflater.inflate(R.layout.share_app_layout, parent, false);
                        showShareScreen(convertView);
                        break;
                    case "image_gallery":
                        pauseVideo();
                        ArrayList<String> image_gallary = new ArrayList<>();
                        showLog("image_gallery:::");
                        // Magazines specials + Special-tab galleries → footer blinking Share.
                        final boolean galleryUsesFooterShare =
                                shouldUseMagazineFooterShare(list.get(position));
                        if (galleryUsesFooterShare) {
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
                        // On-image corner Share removed — footer Share covers Magazines/Special.
                        hideOnPostMagazineShare(imageGallaryHolder.image_gallary_share);
                        final View finalConvertView = convertView;
                        //imageGallaryHolder.indicator.initialize(config);
                        List<String> galleryImages = list.get(position).getImages();
                        if (galleryImages != null && !galleryImages.isEmpty()) {
                            image_gallary.addAll(
                                    GalleryImageUrls.sanitize(
                                            OfflineImageStore.resolveGalleryUris(
                                                    context,
                                                    list.get(position).getPostId(),
                                                    galleryImages)));
                            try {
                                OfflineImageStore.enqueueArticle(context, list.get(position));
                            } catch (Exception ignored) {
                            }
                        }
                        if (image_gallary.isEmpty()) {
                            // Transparent seat only — never a splash-logo fake page.
                            image_gallary.add("");
                        }
                        TextView gallerySwipeHint =
                                convertView.findViewById(R.id.gallery_swipe_left_hint);
                        // Special: FlipView book page-turn instead of ViewPager slide.
                        // Host must be scrren_icon (ViewPager parent) so single-page share
                        // screenshots include the flipped image — not the FrameLayout root
                        // behind a transparent overlay.
                        //
                        // Bind FlipView BEFORE attaching SamplePagerAdapter. Previously we
                        // setAdapter(UIL) then SpecialGalleryFlipHelper.setAdapter(null),
                        // which cancelled in-flight page-0 loads while page 1 often already
                        // sat in memory cache → first image blank/slow, next already there;
                        // leave+return looked fine once page 0 finished into cache.
                        boolean usedSpecialGalleryFlip = false;
                        ViewGroup galleryFlipHost = null;
                        if (viewpager.getParent() instanceof ViewGroup) {
                            galleryFlipHost = (ViewGroup) viewpager.getParent();
                        } else if (imageGallaryHolder.galleryLayout != null) {
                            galleryFlipHost = imageGallaryHolder.galleryLayout;
                        }
                        int shareLandPage = -1;
                        try {
                            HomePage shareHome = shareHome();
                            if (shareHome != null) {
                                shareLandPage = shareHome.peekShareGalleryPage(
                                        list.get(position).getPostId());
                            }
                        } catch (Exception ignored) {
                        }
                        // -1 means no share pending. Do not Math.max that into 0 and
                        // then treat it as an explicit land (that consumed the page).
                        final int galleryInitialPage = shareLandPage < 0 ? 0 : shareLandPage;
                        final boolean shareGalleryLanding = shareLandPage >= 0;
                        FlipView galFlip = null;
                        if (isSpecialModeNow() && galleryFlipHost != null) {
                            // Keep Play Store WormDotsIndicator visible; Flip helper
                            // drives it via BaseDotsIndicator.Pager (no TextView dots).
                            if (imageGallaryHolder.indicator != null) {
                                imageGallaryHolder.indicator.setVisibility(VISIBLE);
                            }
                            galFlip = SpecialGalleryFlipHelper.bind(
                                    galleryFlipHost,
                                    viewpager,
                                    image_gallary,
                                    gallerySwipeHint,
                                    imageGallaryHolder.indicator,
                                    null,
                                    galleryInitialPage);
                            if (galFlip != null) {
                                usedSpecialGalleryFlip = true;
                            }
                        }
                        if (!usedSpecialGalleryFlip) {
                            SamplePagerAdapter galleryPagerAdapter =
                                    new SamplePagerAdapter(context, image_gallary, 0, false);
                            viewpager.setAdapter(galleryPagerAdapter);
                            // Keep 1–2 neighbors warm (default offscreen is also 1).
                            viewpager.setOffscreenPageLimit(
                                    Math.max(1, Math.min(2, image_gallary.size() - 1)));
                            if (imageGallaryHolder.indicator != null) {
                                imageGallaryHolder.indicator.setViewPager(viewpager);
                            }
                            SamplePagerAdapter.schedulePage0Ensure(viewpager, galleryPagerAdapter);
                            if (galleryInitialPage > 0) {
                                try {
                                    int safe = Math.max(0,
                                            Math.min(galleryInitialPage, image_gallary.size() - 1));
                                    viewpager.setCurrentItem(safe, false);
                                    final int landSafe = safe;
                                    viewpager.post(() -> {
                                        try {
                                            viewpager.setCurrentItem(landSafe, false);
                                        } catch (Exception ignored) {
                                        }
                                    });
                                } catch (Exception ignored) {
                                }
                            }
                            GallerySwipeHintHelper.bind(
                                    viewpager, gallerySwipeHint, image_gallary.size());
                        }
                        if (shareGalleryLanding) {
                            View settleAnchor = usedSpecialGalleryFlip ? galFlip : viewpager;
                            scheduleShareGallerySettle(
                                    list.get(position).getPostId(),
                                    galleryInitialPage,
                                    settleAnchor);
                        }
                        if (LanguageId == 4) {
                            imageGallaryHolder.strip_text_name.setText(displayCopy(list.get(position).getStrip_content()));
                            applyStripFont(imageGallaryHolder.strip_text_name);

                        } else if (LanguageId == 1) {
                            imageGallaryHolder.strip_text_name.setText(displayCopy(list.get(position).getStrip_content()));
                            applyStripFont(imageGallaryHolder.strip_text_name);

                        } else {
                            imageGallaryHolder.strip_text_name.setText(displayCopy(list.get(position).getStrip_content()));
                            applyStripFont(imageGallaryHolder.strip_text_name);

                        }
                        imageGallaryHolder.imageViewShareClose.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                fromWhatsapp = false;
                                imageGallaryHolder.relativeLayoutShareOpt = finalConvertView.findViewById(R.id.relativeLayoutShareOpt);
                                imageGallaryHolder.image_gallary_share = finalConvertView.findViewById(R.id.image_gallary_share);
                                imageGallaryHolder.dotIndicatior = finalConvertView.findViewById(R.id.dotIndicatior);
                                imageGallaryHolder.relativeLayoutShareOpt.setVisibility(GONE);
                                hideOnPostMagazineShare(imageGallaryHolder.image_gallary_share);
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
                                hideOnPostMagazineShare(imageGallaryHolder.image_gallary_share);
                                imageGallaryHolder.dotIndicatior.setVisibility(VISIBLE);

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
                                // Screenshot path restores share when done; keep hidden during capture.
                                hideOnPostMagazineShare(imageGallaryHolder.image_gallary_share);
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

                    case "feed_end":
                        convertView = inflateFeedEndCard(parent, convertView, true);
                        break;

                    default: {
                        // Prefer a real article layout over Continue-reading hub when the
                        // row is not explicitly a feed_end sentinel.
                        if (!FeedEndHelper.isFeedEnd(list.get(position))) {
                            String fallback = "normal_image";
                            String media = list.get(position).getMediaType();
                            if ("5".equals(media) || "6".equals(media)) {
                                fallback = "full_video";
                            } else if ("3".equals(media) || "4".equals(media)) {
                                fallback = "normal_video";
                            } else if ("greetings".equalsIgnoreCase(pagePostType)
                                    || "greeting".equalsIgnoreCase(pagePostType)) {
                                fallback = "full_image";
                            }
                            if (!fallback.equalsIgnoreCase(pagePostType)) {
                                showLog("getView unknown post_type=" + pagePostType
                                        + " pos=" + position + " → retry as " + fallback);
                                try {
                                    list.get(position).setPostType(fallback);
                                } catch (Exception ignored) {
                                }
                                return getView(position, null, parent);
                            }
                            // Last resort for non-end item: inflate normal_image
                            convertView = inflater.inflate(R.layout.activity_half_image_new, parent, false);
                            break;
                        }
                        showLog("getView feed_end card pos=" + position);
                        convertView = inflateFeedEndCard(parent, convertView, true);
                        break;
                    }

                }
            }

        } catch (Exception e) {
            // Inflate / ClassCast / NPE on survey-poll bind must not kill the feed.
            e.printStackTrace();
            if (parent != null) {
                // Search: never paint Continue-reading for a real post_id hit.
                // One retry only — unbounded getView recursion caused ANR when
                // getIs_source() was null (Boolean unbox NPE).
                try {
                    if (searchBindRetryDepth == 0
                            && list != null
                            && position >= 0
                            && position < list.size()) {
                        ModelClass.Datum row = list.get(position);
                        if (row != null
                                && !FeedEndHelper.isFeedEnd(row)
                                && row.getPostId() != null
                                && row.getPostId() > 0) {
                            String fallback = inferPostTypeForFlip(row);
                            if (fallback == null || fallback.trim().isEmpty()
                                    || FeedEndHelper.isFeedEndType(fallback)) {
                                fallback = "normal_image";
                            }
                            try {
                                row.setPostType(fallback);
                            } catch (Exception ignored) {
                            }
                            showLog("getView search bind fail → retry once as " + fallback
                                    + " pos=" + position);
                            searchBindRetryDepth = 1;
                            try {
                                return getView(position, null, parent);
                            } finally {
                                searchBindRetryDepth = 0;
                            }
                        }
                    }
                } catch (Exception ignored) {
                }
                convertView = inflateFeedEndCard(parent, convertView, isSearchFeedEndHub());
            }
        }

        return convertView;

    }

    /**
     * Map sparse / Search_Stories rows onto a FlipView layout key so we never
     * paint the Continue-reading hub for a real article.
     */
    @Nullable
    private static String inferPostTypeForFlip(@Nullable ModelClass.Datum d) {
        if (d == null) return null;
        if (FeedEndHelper.isFeedEnd(d)) return FeedEndHelper.POST_TYPE;
        String existing = d.getPostType();
        if (existing != null && !existing.trim().isEmpty()) {
            String t = existing.trim();
            if ("greetings".equalsIgnoreCase(t) || "greeting".equalsIgnoreCase(t)) {
                return "full_image";
            }
            if ("polling".equalsIgnoreCase(t)) {
                return "survey_poll";
            }
            return t;
        }
        Long id = d.getPostId();
        if (id == null || id <= 0) {
            return null;
        }
        String media = d.getMediaType();
        if (media != null) {
            media = media.trim();
            if ("5".equals(media) || "6".equals(media)) {
                return "full_video";
            }
            if ("3".equals(media) || "4".equals(media)) {
                return "normal_video";
            }
        }
        if (d.getVideoUrl() != null && !d.getVideoUrl().trim().isEmpty()) {
            return "normal_video";
        }
        return "normal_image";
    }

    /**
     * Dedicated end-of-feed FlipView page — never a blank white View.
     * In keyword search, ALWAYS paint the Continue-reading hub — never the
     * "no more news" / ఇక చదవడానికి empty card (that was a double-sentinel bug).
     * {@code allowSearchHub} is kept for call-site clarity; search mode overrides it.
     */
    @NonNull
    private View inflateFeedEndCard(@Nullable ViewGroup parent, @Nullable View convertView) {
        return inflateFeedEndCard(parent, convertView, false);
    }

    @NonNull
    private View inflateFeedEndCard(@Nullable ViewGroup parent, @Nullable View convertView,
            boolean allowSearchHub) {
        // Search: hub only. Non-search: classic feed_end_card. Never mix both.
        final boolean searchHub = isSearchFeedEndHub();
        if (searchHub && !allowSearchHub && BuildConfig.DEBUG) {
            showLog("inflateFeedEndCard: search forces hub (skip no-more-news card)");
        }
        final String typeKey = searchHub ? "search_hub" : FeedEndHelper.POST_TYPE;
        View root;
        if (canReuseFlipView(convertView, typeKey)) {
            root = convertView;
        } else if (searchHub) {
            root = inflater.inflate(R.layout.search_feed_hub_card,
                    parent != null ? parent : null, false);
            markFlipViewType(root, typeKey);
        } else {
            root = inflater.inflate(R.layout.feed_end_card,
                    parent != null ? parent : null, false);
            markFlipViewType(root, typeKey);
        }
        if (searchHub) {
            bindSearchFeedHub(root);
        } else {
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
        try {
            installOfflineChromeTaps(root);
        } catch (Exception ignored) {
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

    private void bindSearchFeedHub(@NonNull View root) {
        try {
            final HomePage hp = resolveHomePageHost(root.getContext()) != null
                    ? resolveHomePageHost(root.getContext())
                    : (activity instanceof HomePage ? (HomePage) activity : null);
            View main = root.findViewById(R.id.search_hub_main);
            View local = root.findViewById(R.id.search_hub_local);
            View flash = root.findViewById(R.id.search_hub_flash);
            View daily = root.findViewById(R.id.search_hub_daily);
            View mainDiv = root.findViewById(R.id.search_hub_div_main);
            View localDiv = root.findViewById(R.id.search_hub_div_local);
            View flashDiv = root.findViewById(R.id.search_hub_div_flash);
            boolean showDaily = hp == null || hp.isSearchHubDailyUpdatesAvailable();
            boolean showFlash = hp == null || hp.isSearchHubFlashAvailable();
            boolean showLocal = hp == null || hp.isSearchHubLocalAvailable();
            if (main != null) {
                main.setVisibility(View.VISIBLE);
                main.setOnClickListener(v -> {
                    if (hp != null) hp.openMainNewsFromSearchHub();
                });
            }
            if (mainDiv != null) {
                mainDiv.setVisibility(showLocal || showFlash || showDaily
                        ? View.VISIBLE : View.GONE);
            }
            if (local != null) {
                local.setVisibility(showLocal ? View.VISIBLE : View.GONE);
                local.setOnClickListener(v -> {
                    if (hp != null) hp.openLocalNewsFromSearchHub();
                });
            }
            if (localDiv != null) {
                localDiv.setVisibility(showLocal && (showFlash || showDaily)
                        ? View.VISIBLE : View.GONE);
            }
            if (flash != null) {
                flash.setVisibility(showFlash ? View.VISIBLE : View.GONE);
                flash.setOnClickListener(v -> {
                    if (hp != null) hp.openFlashNewsFromSearchHub();
                });
            }
            if (flashDiv != null) {
                flashDiv.setVisibility(showFlash && showDaily ? View.VISIBLE : View.GONE);
            }
            if (daily != null) {
                daily.setVisibility(showDaily ? View.VISIBLE : View.GONE);
                daily.setOnClickListener(v -> {
                    if (hp != null) hp.openDailyUpdatesFromSearchHub();
                });
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * FlipView scrap reuse: only keep convertView when it was inflated for the same
     * post layout key. Gallery/YouTube stay inflate-always (heavier stateful WebViews).
     * Half/full Exo and ad seats reuse scrap so painted creatives / video surfaces
     * are not torn into empty black on notify.
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

    /**
     * Exact pool match only — never consumes {@link AdPreloader}. Used when getView
     * will defer MediaView paint (mid-flip / settle grace) so a buffer shell cannot
     * strand the warm creative on an ahead reqPos. When destroy-after-impression
     * is OFF, impressed exact matches are seat-sticky (peek OK). When ON, skip
     * impressed so flip-back requests a fresh fill.
     */
    @Nullable
    private NativeAd peekExactGoogleNativeForSlot(@Nullable ModelClass.Datum slot) {
        Integer slotPos = slot != null ? slot.getAd_req_position() : null;
        if (slotPos == null || HomePage.nativeGoogleAdList == null) {
            return null;
        }
        try {
            for (int i = 0; i < HomePage.nativeGoogleAdList.size(); i++) {
                FlipGoogleNativeAds packed = HomePage.nativeGoogleAdList.get(i);
                if (packed == null || packed.getAd() == null) continue;
                if (packed.getReqPos() == slotPos) {
                    if (packed.isImpressed() && isDestroyImpressedEnabled()) {
                        return null;
                    }
                    return packed.getAd();
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * Exact {@code ad_req_position} match first, then ready-pool transfer: orphan
     * (reqPos not on a live ad seat) or an <b>unclaimed</b> creative from a seat
     * further ahead (idle only). Claimed/impressed creatives are never handed to
     * another seat (money-safe unpaid-reuse guard).
     * <p>
     * Destroy toggle ON: impressed exact matches are released so flip-back
     * requests a fresh AdMob fill. OFF (default): seat-sticky re-show on same
     * reqPos without destroy.
     */
    @Nullable
    private NativeAd findGoogleNativeForSlot(@Nullable ModelClass.Datum slot) {
        Integer slotPos = slot != null ? slot.getAd_req_position() : null;
        if (slotPos == null) {
            return null;
        }
        if (HomePage.nativeGoogleAdList != null && !HomePage.nativeGoogleAdList.isEmpty()) {
            for (int i = 0; i < HomePage.nativeGoogleAdList.size(); i++) {
                FlipGoogleNativeAds packed = HomePage.nativeGoogleAdList.get(i);
                if (packed == null || packed.getAd() == null) continue;
                if (packed.getReqPos() != slotPos) continue;
                if (packed.isImpressed()) {
                    if (isDestroyImpressedEnabled()) {
                        // Flip-back / re-land: drop paid creative and request anew.
                        showLog("HomePage.nativeAdList release impressed for fresh fill reqPos="
                                + slotPos);
                        releaseImpressedGooglePackedAt(i);
                        break;
                    }
                    // OFF: seat-sticky same reqPos — re-show same creative.
                    showLog("HomePage.nativeAdList seat-sticky impressed reqPos=" + slotPos);
                    return packed.getAd();
                }
                showLog("HomePage.nativeAdList exact pos match reqPos=" + slotPos
                        + (packed.isClaimed() ? " (claimed same-seat)" : ""));
                return packed.getAd();
            }
        }
        // Early-warmed shared-pool creative: inject into the seat pool so settle
        // paint / exact match can show immediately without waiting for AdLoader.
        NativeAd preloaded = takeAdPreloaderIntoPool(slotPos);
        if (preloaded != null) {
            return preloaded;
        }
        // Mid-flip: exact miss only — pool reclaim walks the feed and can hitch.
        if (isHomeFlipAnimating()) {
            return null;
        }
        if (HomePage.nativeGoogleAdList == null || HomePage.nativeGoogleAdList.isEmpty()) {
            return null;
        }
        // Idle reclaim: orphan (dead reqPos) first, then nearest unclaimed ahead.
        // Prefer different responseId; allow same-responseId new object if needed.
        java.util.HashSet<Integer> liveAdPositions = new java.util.HashSet<>();
        try {
            if (list != null) {
                for (int i = 0; i < list.size(); i++) {
                    ModelClass.Datum d = list.get(i);
                    if (d == null || d.getPostType() == null
                            || !d.getPostType().equalsIgnoreCase("ad")) {
                        continue;
                    }
                    Integer p = d.getAd_req_position();
                    if (p != null) {
                        liveAdPositions.add(p);
                    }
                }
            }
        } catch (Exception ignored) {
        }
        FlipGoogleNativeAds orphanPreferred = null;
        FlipGoogleNativeAds orphanAny = null;
        FlipGoogleNativeAds aheadPreferred = null;
        FlipGoogleNativeAds aheadAny = null;
        for (int i = 0; i < HomePage.nativeGoogleAdList.size(); i++) {
            FlipGoogleNativeAds packed = HomePage.nativeGoogleAdList.get(i);
            if (packed == null || packed.getAd() == null
                    || packed.isClaimed() || packed.isImpressed()) {
                continue;
            }
            boolean sameCreative = isSamePaintedGoogleCreative(packed.getAd());
            int packedPos = packed.getReqPos();
            if (!liveAdPositions.contains(packedPos)) {
                if (orphanAny == null) {
                    orphanAny = packed;
                }
                if (!sameCreative && orphanPreferred == null) {
                    orphanPreferred = packed;
                }
                continue;
            }
            if (packedPos > slotPos) {
                if (aheadAny == null) {
                    aheadAny = packed;
                }
                if (!sameCreative && aheadPreferred == null) {
                    aheadPreferred = packed;
                }
            }
        }
        FlipGoogleNativeAds donor = orphanPreferred != null ? orphanPreferred
                : (orphanAny != null ? orphanAny
                : (aheadPreferred != null ? aheadPreferred : aheadAny));
        if (donor == null) {
            return null;
        }
        int fromPos = donor.getReqPos();
        showLog("HomePage.nativeAdList pool claim " + fromPos + " → " + slotPos
                + (!liveAdPositions.contains(fromPos) ? " (orphan)" : " (ahead-idle)")
                + (isSamePaintedGoogleCreative(donor.getAd())
                ? " (same-respId ok — new object)" : ""));
        reassignGoogleNativeReqPos(donor, slotPos);
        return donor.getAd();
    }

    /**
     * Destroy an impressed Google packed entry so flip-back can request a fresh
     * fill. Never transfers the creative to another seat.
     */
    private void releaseImpressedGooglePackedAt(int index) {
        if (HomePage.nativeGoogleAdList == null
                || index < 0
                || index >= HomePage.nativeGoogleAdList.size()) {
            return;
        }
        try {
            FlipGoogleNativeAds packed = HomePage.nativeGoogleAdList.get(index);
            if (packed == null) {
                HomePage.nativeGoogleAdList.remove(index);
                return;
            }
            int oldPos = packed.getReqPos();
            try {
                if (packed.getAd() != null) {
                    packed.getAd().destroy();
                }
            } catch (Exception ignored) {
            }
            packed.setAd(null);
            packed.setClaimed(false);
            packed.setImpressed(false);
            HomePage.nativeGoogleAdList.remove(index);
            try {
                if (HomePage.nativeGoogleAdArray != null) {
                    HomePage.nativeGoogleAdArray.remove(Integer.valueOf(oldPos));
                }
            } catch (Exception ignored) {
            }
            showLog("released impressed google for fresh fill reqPos=" + oldPos);
            try {
                String coordCode = packed.getAdCode();
                if (coordCode == null || coordCode.trim().isEmpty()) {
                    coordCode = FeedAdConfig.getUnitId(context);
                }
                NativeAdCacheCoordinator.get().onConsumed("google", coordCode, oldPos);
                NativeAdCacheCoordinator.get().allowSettleRetry("google", coordCode, oldPos);
            } catch (Exception ignored) {
            }
            if (context instanceof HomePage) {
                try {
                    ((HomePage) context).refillNativeAdPoolAfterConsume();
                } catch (Exception ignored) {
                }
            }
        } catch (Exception e) {
            showLog("releaseImpressedGooglePackedAt: " + e.getMessage());
        }
    }

    /** Destroy impressed FB packed entry for fresh fill on flip-back. */
    private void releaseImpressedFbPackedAt(int index) {
        if (HomePage.nativeFBAdList == null
                || index < 0
                || index >= HomePage.nativeFBAdList.size()) {
            return;
        }
        try {
            FlipGoogleNativeAds packed = HomePage.nativeFBAdList.get(index);
            if (packed == null) {
                HomePage.nativeFBAdList.remove(index);
                return;
            }
            int oldPos = packed.getReqPos();
            try {
                if (packed.getFbAd() != null) {
                    packed.getFbAd().destroy();
                }
            } catch (Exception ignored) {
            }
            packed.setFbAd(null);
            packed.setClaimed(false);
            packed.setImpressed(false);
            HomePage.nativeFBAdList.remove(index);
            try {
                if (HomePage.nativeFBAdArray != null) {
                    HomePage.nativeFBAdArray.remove(Integer.valueOf(oldPos));
                }
            } catch (Exception ignored) {
            }
            showLog("released impressed fb for fresh fill reqPos=" + oldPos);
            if (context instanceof HomePage) {
                try {
                    ((HomePage) context).refillNativeAdPoolAfterConsume();
                } catch (Exception ignored) {
                }
            }
        } catch (Exception e) {
            showLog("releaseImpressedFbPackedAt: " + e.getMessage());
        }
    }

    /** AdMob response id for back-to-back creative preference (best-effort). */
    @Nullable
    private static String googleNativeResponseId(@Nullable NativeAd ad) {
        if (ad == null) {
            return null;
        }
        try {
            if (ad.getResponseInfo() != null) {
                return ad.getResponseInfo().getResponseId();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private boolean isSamePaintedGoogleCreative(@Nullable NativeAd ad) {
        if (ad == null || lastPaintedGoogleResponseId == null
                || lastPaintedGoogleResponseId.isEmpty()) {
            return false;
        }
        String rid = googleNativeResponseId(ad);
        return rid != null && rid.equals(lastPaintedGoogleResponseId);
    }

    private void notePaintedGoogleNative(@Nullable NativeAd ad) {
        // Track responseId only — never reuse this field as a claimable NativeAd fallback.
        nativeAd = null;
        String rid = googleNativeResponseId(ad);
        if (rid != null && !rid.isEmpty()) {
            lastPaintedGoogleResponseId = rid;
        }
    }

    /**
     * Move a ready {@link AdPreloader} native into {@link HomePage#nativeGoogleAdList}
     * for {@code slotPos}. Prefer a different responseId than the last painted seat;
     * same responseId on a new unimpressed object is still claimable (new impression).
     * Immediately replenishes the shared warm pool after take.
     */
    @Nullable
    private NativeAd takeAdPreloaderIntoPool(int slotPos) {
        try {
            // Search mode: never seat AdPreloader creatives into FlipView.
            if (isSearchFeedEndHub()) {
                return null;
            }
            if (!AdPreloader.get().isReady()) {
                return null;
            }
            if (HomePage.nativeGoogleAdList == null) {
                return null;
            }
            String code = AdPreloader.get().peekAdCode();
            NativeAd ad = AdPreloader.get().takeReadyAndReplenish(
                    context, lastPaintedGoogleResponseId);
            if (ad == null) {
                try {
                    AdPreloader.get().prefetch(context);
                } catch (Exception ignored) {
                }
                return null;
            }
            FlipGoogleNativeAds packed = new FlipGoogleNativeAds();
            packed.setAd(ad);
            packed.setAdCode(code != null ? code : "");
            packed.setReqPos(slotPos);
            packed.setAd_type("google");
            packed.setClaimed(false);
            packed.setImpressed(false);
            HomePage.nativeGoogleAdList.add(packed);
            try {
                if (HomePage.nativeGoogleAdArray != null
                        && !HomePage.nativeGoogleAdArray.contains(slotPos)) {
                    HomePage.nativeGoogleAdArray.add(slotPos);
                }
            } catch (Exception ignored) {
            }
            showLog("AdPreloader → seat reqPos=" + slotPos
                    + " poolLeft=" + AdPreloader.get().readyCount());
            return ad;
        } catch (Exception e) {
            showLog("takeAdPreloaderIntoPool: " + e.getMessage());
            return null;
        }
    }

    /** Move a ready creative onto {@code newPos} and free the donor seat for refill. */
    private void reassignGoogleNativeReqPos(
            @NonNull FlipGoogleNativeAds packed, int newPos) {
        int oldPos = packed.getReqPos();
        try {
            packed.setReqPos(newPos);
        } catch (Exception ignored) {
        }
        if (oldPos == newPos) {
            return;
        }
        try {
            if (HomePage.nativeGoogleAdArray != null) {
                HomePage.nativeGoogleAdArray.remove(Integer.valueOf(oldPos));
                if (!HomePage.nativeGoogleAdArray.contains(newPos)) {
                    HomePage.nativeGoogleAdArray.add(newPos);
                }
            }
        } catch (Exception ignored) {
        }
        if (context instanceof HomePage) {
            try {
                ((HomePage) context).onNativeAdPoolReassigned(oldPos, newPos);
            } catch (Exception ignored) {
            }
        }
    }

    /** Mark the packed Google native claimed once painted into a seat. */
    private void markGoogleNativeClaimed(@Nullable NativeAd ad) {
        if (ad == null || HomePage.nativeGoogleAdList == null) {
            return;
        }
        try {
            for (int i = 0; i < HomePage.nativeGoogleAdList.size(); i++) {
                FlipGoogleNativeAds packed = HomePage.nativeGoogleAdList.get(i);
                if (packed != null && packed.getAd() == ad) {
                    packed.setClaimed(true);
                    NativeAdCacheCoordinator.get().onConsumed(
                            "google", packed.getAdCode(), packed.getReqPos());
                    if (context instanceof HomePage) {
                        ((HomePage) context).refillNativeAdPoolAfterConsume();
                    }
                    return;
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void markFbNativeClaimed(@Nullable com.facebook.ads.NativeAd fbAd) {
        if (fbAd == null || HomePage.nativeFBAdList == null) {
            return;
        }
        try {
            for (int i = 0; i < HomePage.nativeFBAdList.size(); i++) {
                FlipGoogleNativeAds packed = HomePage.nativeFBAdList.get(i);
                if (packed != null && packed.getFbAd() == fbAd) {
                    packed.setClaimed(true);
                    NativeAdCacheCoordinator.get().onConsumed(
                            "facebook", packed.getAdCode(), packed.getReqPos());
                    if (context instanceof HomePage) {
                        ((HomePage) context).refillNativeAdPoolAfterConsume();
                    }
                    return;
                }
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * FB: exact match only (ahead-steal disabled for flip lag). Impressed exact
     * matches are released for fresh fill on flip-back; never transferred to
     * another seat.
     */
    @Nullable
    private com.facebook.ads.NativeAd findFbNativeForSlot(@Nullable ModelClass.Datum slot) {
        if (HomePage.nativeFBAdList == null || HomePage.nativeFBAdList.isEmpty()) {
            return null;
        }
        Integer slotPos = slot != null ? slot.getAd_req_position() : null;
        if (slotPos == null) {
            return null;
        }
        for (int i = 0; i < HomePage.nativeFBAdList.size(); i++) {
            FlipGoogleNativeAds packed = HomePage.nativeFBAdList.get(i);
            if (packed == null || packed.getFbAd() == null) continue;
            if (packed.getReqPos() == slotPos) {
                if (packed.isImpressed()) {
                    if (isDestroyImpressedEnabled()) {
                        showLog("fb release impressed for fresh fill reqPos=" + slotPos);
                        releaseImpressedFbPackedAt(i);
                        return null;
                    }
                    showLog("fb seat-sticky impressed reqPos=" + slotPos);
                    return packed.getFbAd();
                }
                return packed.getFbAd();
            }
        }
        return null;
    }

    /**
     * True when Google/FB has an exact claimable creative <b>or</b> an
     * unclaimed ready-pool ad that can be transferred (orphan / ahead). Peek only
     * — does not mutate. Impressed exact seats count as ready when destroy toggle
     * is OFF (seat-sticky); when ON they need a fresh fill.
     * <p>
     * {@link AdPreloader} being ready is <b>not</b> enough: that warm creative may
     * never claim onto this seat (primary_only / rebind miss) and used to skip
     * per-seat {@code AdLoader} → Share forever. Play Store always fires seat loads.
     */
    private boolean hasNativeCreativeForSlot(@Nullable ModelClass.Datum slot) {
        if (slot == null) {
            return false;
        }
        Integer slotPos = slot.getAd_req_position();
        if (slotPos == null) {
            return false;
        }
        try {
            if (HomePage.nativeGoogleAdList != null) {
                java.util.HashSet<Integer> liveAdPositions = new java.util.HashSet<>();
                if (list != null) {
                    for (int i = 0; i < list.size(); i++) {
                        ModelClass.Datum d = list.get(i);
                        if (d == null || d.getPostType() == null
                                || !d.getPostType().equalsIgnoreCase("ad")) {
                            continue;
                        }
                        Integer p = d.getAd_req_position();
                        if (p != null) {
                            liveAdPositions.add(p);
                        }
                    }
                }
                for (int i = 0; i < HomePage.nativeGoogleAdList.size(); i++) {
                    FlipGoogleNativeAds packed = HomePage.nativeGoogleAdList.get(i);
                    if (packed == null || packed.getAd() == null) continue;
                    if (packed.getReqPos() == slotPos) {
                        if (!packed.isImpressed() || !isDestroyImpressedEnabled()) {
                            return true;
                        }
                        continue;
                    }
                    if (!packed.isClaimed() && !packed.isImpressed()
                            && !isHomeFlipAnimating()) {
                        int packedPos = packed.getReqPos();
                        if (!liveAdPositions.contains(packedPos) || packedPos > slotPos) {
                            return true;
                        }
                    }
                }
            }
            if (HomePage.nativeFBAdList != null) {
                for (int i = 0; i < HomePage.nativeFBAdList.size(); i++) {
                    FlipGoogleNativeAds packed = HomePage.nativeFBAdList.get(i);
                    if (packed == null || packed.getFbAd() == null) continue;
                    if (packed.getReqPos() == slotPos) {
                        if (!packed.isImpressed() || !isDestroyImpressedEnabled()) {
                            return true;
                        }
                        continue;
                    }
                    if (!packed.isClaimed() && !packed.isImpressed()
                            && !isHomeFlipAnimating()
                            && packed.getReqPos() > slotPos) {
                        return true;
                    }
                }
            }
            // Do NOT treat AdPreloader.isReady() as a seat creative — that skipped
            // July on-seat AdLoader when the warm object never painted this reqPos.
        } catch (Exception ignored) {
        }
        return false;
    }

    /**
     * Ask HomePage to (re)load this slot if no creative is ready yet.
     * No-op when context is not HomePage (bookmark reader, etc.).
     * Always fires per-seat load when the seat lacks a claimable creative —
     * matching Play Store. Warm pool may still paint via rebind/claim.
     */
    private void requestNativeAdLoadForSlot(@Nullable ModelClass.Datum slot) {
        if (slot == null || !(context instanceof HomePage)) return;
        try {
            // Never start AdLoader mid-gesture — hitch on Camera flip frames.
            // HomePage.scheduleSettledAdWork owns the reliability path on land.
            if (isHomeFlipAnimating() || shouldDeferAdWork()) {
                return;
            }
            // Prefer painting an already-matched / transferable seat creative.
            if (hasNativeCreativeForSlot(slot)) {
                return;
            }
            // Best-effort claim warm inventory into the seat pool for faster paint,
            // but never skip ensureFeedNativeAdLoaded just because AdPreloader is ready.
            try {
                Integer warmPos = slot.getAd_req_position();
                if (warmPos != null && AdPreloader.get().isReady()) {
                    takeAdPreloaderIntoPool(warmPos);
                    if (hasNativeCreativeForSlot(slot)) {
                        return;
                    }
                }
            } catch (Exception ignored) {
            }
            // Best-effort warm (may run mid-flip). Cap/backoff never permanently blocks —
            // HomePage settle path unlocks and retries on land.
            String code = slot.getAd_code();
            if (code == null || code.trim().isEmpty()) {
                code = slot.getNext_req_ad_code();
            }
            Integer pos = slot.getAd_req_position();
            if (pos == null) {
                pos = slot.getNext_ad_req_position();
            }
            String type = slot.getAd_type();
            if (type == null || type.trim().isEmpty()) {
                type = "google";
            }
            // Google seats use HomePage's Play Store production native unit, so absent
            // server metadata is not a reason to abandon a paid request. Other networks
            // still need their own placement id.
            boolean googleSeat = "google".equalsIgnoreCase(type)
                    || "admanager".equalsIgnoreCase(type);
            if (!googleSeat && (code == null || code.trim().isEmpty())) {
                showLog("requestNativeAdLoadForSlot: empty ad_code — arm fill timeout");
                int listPos = list != null ? list.indexOf(slot) : -1;
                if (listPos < 0 && pos != null && list != null) {
                    for (int i = 0; i < list.size(); i++) {
                        ModelClass.Datum d = list.get(i);
                        if (d != null && pos.equals(d.getAd_req_position())) {
                            listPos = i;
                            break;
                        }
                    }
                }
                if (listPos >= 0) {
                    scheduleAdShellFillTimeout(listPos);
                }
                return;
            }
            ((HomePage) context).ensureFeedNativeAdLoaded(code, pos, type);
        } catch (Exception e) {
            showLog("requestNativeAdLoadForSlot: " + e.getMessage());
        }
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
        final String shareText =
                "Hey check out my app at: https://play.google.com/store/apps/details?id="
                        + BuildConfig.APPLICATION_ID;
        imageViewShare.setImageResource(R.drawable.ic_app_share_socail);
        imageViewWhatsapp.setImageResource(R.drawable.ic_whatsapp_home);
        imageViewShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent sendIntent = new Intent(Intent.ACTION_SEND);
                    sendIntent.setType("text/plain");
                    sendIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                    context.startActivity(Intent.createChooser(sendIntent, "Share AnyNews"));
                } catch (Exception e) {
                    showLog("share_app system share: " + e.getMessage());
                }
            }
        });
        imageViewWhatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if(appInstalledOrNot("com.whatsapp")){
                    sendIntent.setPackage("com.whatsapp");
                    sendIntent.putExtra(Intent.EXTRA_TEXT, shareText);
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
        if (convertView == null) {
            return;
        }
        final RelativeLayout relativeLayoutShareOpt = convertView.findViewById(R.id.relativeLayoutShareOpt);
        ImageView magazineShare = convertView.findViewById(R.id.imageViewShare);
        ImageView galleryShare = convertView.findViewById(R.id.image_gallary_share);
        openOnPostShareSheet(relativeLayoutShareOpt, magazineShare, galleryShare);
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
        try {
            AdImpressionReporter.get().recordFbDisplay(nativeAd);
        } catch (Exception ignored) {
        }


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
        displayGoogleFullNativeAd(parent, ad, myView, convertView, currentFlipPage);
    }

    private void displayGoogleFullNativeAd(ViewGroup parent, NativeAd ad, View myView,
                                           View convertView, int listPosition) {
        {
            // Overlay creative on dark/house underlay — never removeAllViews first
            // (that painted empty black ads_screen for 1–2 frames = land flash).
            if(ad!=null){
                showLog("AD #2");
                NativeAdView adView = (NativeAdView) myView;
                showLog("AD #3");
                MediaView mediaView = (MediaView) adView.findViewById(R.id.ad_media);
                adView.setMediaView(mediaView);
                showLog("AD #4");

                final ImageView iv_ad_view_blur = adView.findViewById(R.id.iv_ad_view_blur);
                if (iv_ad_view_blur != null && iv_ad_view_blur.getLayoutParams() != null) {
                    iv_ad_view_blur.getLayoutParams().height=screenHeight;
                }
                showLog("AD #8");
                loadNativeAdBlurBackground(iv_ad_view_blur, ad, listPosition);
                showLog("AD #9");
                adView.setNativeAd(ad);
                attachNativeAdViewWithoutFlash(parent, adView);
                showLog("AD #10");
                try {
                    AdImpressionReporter.get().recordGoogleDisplay(ad);
                } catch (Exception ignored) {
                }
            }
        }
    }
    private boolean adHasOnlyStore(NativeAd nativeAd) {
        String store = nativeAd.getStore();
        String advertiser = nativeAd.getAdvertiser();
        return !TextUtils.isEmpty(store) && TextUtils.isEmpty(advertiser);
    }
    private void displayGoogleMediumNativeAd(ViewGroup parent, NativeAd ad,View myView,View convertView) {
        displayGoogleMediumNativeAd(parent, ad, myView, convertView, currentFlipPage);
    }

    private void displayGoogleMediumNativeAd(ViewGroup parent, NativeAd ad, View myView,
                                             View convertView, int listPosition) {
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


                Log.e("ADDDD","setNativeAd 7");
                loadNativeAdBlurBackground(iv_ad_view_blur, nativeAd, listPosition);
                Log.e("ADDDD","setNativeAd 8");
                nativeAdView.setNativeAd(nativeAd);
                Log.e("ADDDD","setNativeAd 9");

                    // Overlay on house/dark underlay — never blank the seat first.
                    attachNativeAdViewWithoutFlash(parent, nativeAdView);
                    showLog("AD #10");
                    try {
                        AdImpressionReporter.get().recordGoogleDisplay(nativeAd);
                    } catch (Exception ignored) {
                    }


            }catch (Exception e){
                e.printStackTrace();
            }
            // Inflate a layout and add it to the parent ViewGroup.
        }
    }
    private void displayGoogleNativeAd(ViewGroup parent, NativeAd ad,View myView,View convertView) {
        displayGoogleNativeAd(parent, ad, myView, convertView, currentFlipPage);
    }

    private void displayGoogleNativeAd(ViewGroup parent, NativeAd ad, View myView,
                                       View convertView, int listPosition) {
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
                loadNativeAdBlurBackground(iv_ad_view_blur, ad, listPosition);
                showLog("AD #9");
                ((TextView) adView.getCallToActionView()).setText(ad.getCallToAction());
                // Call the NativeAdView's setNativeAd method to register the
                // NativeAdObject.
                adView.setNativeAd(ad);
                // Overlay on house/dark underlay — never blank the seat first.
                attachNativeAdViewWithoutFlash(parent, adView);
                showLog("AD #10");
                try {
                    AdImpressionReporter.get().recordGoogleDisplay(ad);
                } catch (Exception ignored) {
                }
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
        ModelClass.Datum visibleItem = null;
        try {
            if (list != null && !list.isEmpty()) {
                int idx = 0;
                if (context instanceof HomePage) {
                    idx = ((HomePage) context).getVisiblePostIndexForShare();
                }
                if (idx >= 0 && idx < list.size()) {
                    visibleItem = list.get(idx);
                }
            }
        } catch (Exception ignored) {
        }
        final ModelClass.Datum restoreItem = visibleItem;
        final View restoreRoot = finalConvertView;
        restoreOnPostMagazineShare(imageGallaryHolder.image_gallary_share, restoreItem, v -> {
            fromWhatsapp = false;
            shareGalleryScreenShotUI(restoreRoot);
        });
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

        openOnPostShareSheet(imageGallaryHolder.relativeLayoutShareOpt,
                imageGallaryHolder.image_gallary_share);
        if (imageGallaryHolder.dotIndicatior != null) {
            imageGallaryHolder.dotIndicatior.setVisibility(GONE);
        }

    }
    public void shareGalleryScreenShot(int position,int from,View customConvertView) {
        if (!tryAcquireShareLaunch()) {
            return;
        }
        if (customConvertView == null || customConvertView.findViewById(R.id.relativeLayoutShareOpt) == null) {
            releaseShareLaunch();
            return;
        }
        if (imageGallaryHolder == null) {
            imageGallaryHolder = new ImageGallaryHolder();
        }
        imageGallaryHolder.relativeLayoutShareOpt = customConvertView.findViewById(R.id.relativeLayoutShareOpt);
        imageGallaryHolder.image_gallary_share = customConvertView.findViewById(R.id.image_gallary_share);
        imageGallaryHolder.dotIndicatior = customConvertView.findViewById(R.id.dotIndicatior);
        imageGallaryHolder.image_gallary_scrip = customConvertView.findViewById(R.id.image_gallary_scrip);
        final RelativeLayout scrren_icon = customConvertView.findViewById(R.id.scrren_icon);
        // Hide All/Single sheet immediately so the click feels instant; heavy capture runs after.
        if (imageGallaryHolder.relativeLayoutShareOpt != null) {
            imageGallaryHolder.relativeLayoutShareOpt.setVisibility(GONE);
        }
        if (imageGallaryHolder.dotIndicatior != null) {
            imageGallaryHolder.dotIndicatior.setVisibility(VISIBLE);
        }
        if (imageGallaryHolder.image_gallary_scrip != null) {
            imageGallaryHolder.image_gallary_scrip.setVisibility(View.GONE);
        }

        final String mInvitationUrl = shareLinkForGalleryPage(position, customConvertView);
        final String msg = ShareOutboundHelper.formatCaption(list.get(position).getTitle(), mInvitationUrl, list.get(position).getPostId());
        final boolean galleryWhatsapp = from == 1 || fromWhatsapp;
        // Defer past the click frame so the sheet dismiss paints first, then capture.
        customConvertView.post(() -> {
            Bitmap b = null;
            try {
                if (scrren_icon != null) {
                    b = ScreenShott.getInstance().takeScreenShotOfView(scrren_icon);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            final Bitmap shot = b;
            new Thread(() -> {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("image/*");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(Intent.EXTRA_SUBJECT, mInvitationUrl);
                intent.putExtra(Intent.EXTRA_TEXT, msg);
                if (galleryWhatsapp) {
                    intent.setPackage("com.whatsapp");
                }
                try {
                    if (shot != null) {
                        writeScreenshotPngAndAttach(intent, shot);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                new Handler(Looper.getMainLooper()).post(() -> {
                    try {
                        if (imageGallaryHolder != null
                                && imageGallaryHolder.image_gallary_scrip != null) {
                            imageGallaryHolder.image_gallary_scrip.setVisibility(View.GONE);
                        }
                        if (imageGallaryHolder != null
                                && imageGallaryHolder.image_gallary_share != null) {
                            restoreOnPostMagazineShare(
                                    imageGallaryHolder.image_gallary_share,
                                    list.get(position),
                                    v -> {
                                        fromWhatsapp = false;
                                        shareGalleryScreenShotUI(customConvertView);
                                    });
                        }
                        if (context != null
                                && context.getPackageName().equals(BuildConfig.APPLICATION_ID)) {
                            final long galleryPostId = list.get(position).getPostId();
                            final boolean countAsWhatsapp = galleryWhatsapp;
                            // Count only when user picks a target — not on sheet open/dismiss.
                            ShareOutboundHelper.launchChooserOnTargetChosen(
                                    context,
                                    intent,
                                    "Share Link",
                                    () -> Share_Count_Value(
                                            shareCountProperty(countAsWhatsapp), galleryPostId));
                        }
                        fromWhatsapp = false;
                    } finally {
                        releaseShareLaunch();
                    }
                });
            }, "gallery-share-shot").start();
        });
    }

    public void shareMagazineScreenShot(ImageView imageViewShare, RelativeLayout relativeLayoutShareOpt, int position,RelativeLayout magzine_relative_slider) {
        if (!tryAcquireShareLaunch()) {
            return;
        }

        relativeLayoutShareOpt.setVisibility(GONE);
        image_magazineHolder.magzine_ll_dots.setVisibility(GONE);
        hideOnPostMagazineShare(imageViewShare);

        String mInvitationUrl = shareLinkForGalleryPage(position, magzine_relative_slider);
        String msg = ShareOutboundHelper.formatCaption(list.get(position).getTitle(), mInvitationUrl, list.get(position).getPostId());
        final boolean magazineWasWhatsapp = fromWhatsapp;
        fromWhatsapp = false;

        Intent base = new Intent();
        base.putExtra(Intent.EXTRA_SUBJECT, mInvitationUrl);
        base.putExtra(Intent.EXTRA_TEXT, msg);
        prepareAndLaunchScreenshotShare(
                magzine_relative_slider,
                base,
                magazineWasWhatsapp,
                () -> {
                    image_magazineHolder.magzine_ll_dots.setVisibility(VISIBLE);
                    if (imageViewShare != null) {
                        restoreOnPostMagazineShare(imageViewShare, list.get(position), v -> {
                            fromWhatsapp = false;
                            openOnPostShareSheet(relativeLayoutShareOpt, imageViewShare);
                        });
                    }
                },
                () -> Share_Count_Value(
                        shareCountProperty(magazineWasWhatsapp), list.get(position).getPostId()));
    }

    private void showFullVideoWithYoutube(int position, View convertView) {
        applyFullSideActionGap(context, convertView);
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
                fullVideoNewYoutubeViewHolder.full_video_share,
                convertView.findViewById(R.id.full_img_whtsapp_text), position);
        final ImageView imageViewYoutubePlayVideo = convertView.findViewById(R.id.imageViewPlayVideo);
        if (imageViewYoutubePlayVideo != null) {
            imageViewYoutubePlayVideo.setImageResource(R.drawable.ic_way2_play_circle);
        }
        RelativeLayout layoutYoutubeFullView = convertView.findViewById(R.id.layoutYoutubeFullView);
        final YouTubePlayerView youTubePlayerView = convertView.findViewById(R.id.youtube_full_video);
        final RelativeLayout parent_layout = convertView.findViewById(R.id.parent_layout);
        final CircleImageView user_pic = convertView.findViewById(R.id.user_pic);
        final ImageView like_for_video_image = convertView.findViewById(R.id.like_for_video_image);
        final ImageView iv_comment_for_video = convertView.findViewById(R.id.iv_comment_for_video);
        AvatarImageHelper.loadUil(imageLoader, user_pic, list.get(position).getWriterProfile());
        like_for_video_image.setImageResource(R.drawable.ic_like_half1);
        iv_comment_for_video.setImageResource(R.drawable.chatbox);

        ((Activity) convertView.getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        // Keep iframe hidden until play — its blank WebView is black and must not
        // show behind / instead of the YouTube poster thumb.
        try {
            youTubePlayerView.setVisibility(View.GONE);
        } catch (Exception ignored) {
        }
        if (newFullImageView1 != null) {
            newFullImageView1.setVisibility(View.VISIBLE);
            newFullImageView1.setClickable(true);
            newFullImageView1.setFocusable(true);
        }
        if (imageViewYoutubePlayVideo != null) {
            imageViewYoutubePlayVideo.setVisibility(View.VISIBLE);
        }

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

        // Real YouTube thumb only — never CMS orange / splash / black plate.
        bindYoutubeFeedPoster(newFullImageView1, list.get(position));

        bindThreeStateIcon(fullVideoNewYoutubeViewHolder.like_for_video_image, list.get(position).getLikeStatus());
        hideDislikeView(dislike_for_video_ll);

        View finalConvertViewFull = convertView;
        imageViewYoutubePlayVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fullVideoNewYoutubeViewHolder.side_menu = finalConvertViewFull.findViewById(R.id.video_side_menu);
                if (fullVideoNewYoutubeViewHolder.side_menu != null) {
                    fullVideoNewYoutubeViewHolder.side_menu.setVisibility(View.INVISIBLE);
                }
                if (true) {
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // Keep YT poster visible until iframe is ready — do not flash black.
                            imageViewYoutubePlayVideo.setVisibility(GONE);
                            isFullYoutubeVideoIsInitialized=true;
                            youTubePlayerView.setVisibility(View.VISIBLE);
                            View v = youTubePlayerView.inflateCustomPlayerUi(R.layout.youtube_custom_player_ui);
                            YouTubePlayerListener listener = new com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener() {
                                @Override
                                public void onReady(@NonNull com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer youTubePlayer) {
                                    try {
                                        newFullImageView1.setVisibility(GONE);
                                    } catch (Exception ignored) {
                                    }
                                    fullVideocustomPlayerUiController = new CustomPlayerUiController(context, v, youTubePlayer, youTubePlayerView, layoutHeight, true,new CustomPlayerUiController.ShowFullScreenVideo() {
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
                                    fullVideocustomPlayerUiController.setUserPlaybackCallbacks(
                                            () -> markUserPausedVideoAutoplay(),
                                            () -> clearUserPausedVideoAutoplay());
                                    attachYoutubeEndedAutoplayListener(youTubePlayer, position);
                                    ModelClass.Datum fullItem = list.get(position);
                                    String fullYtId = YouTubeIdHelper.extractFromItem(
                                            fullItem.getVideoUrl(),
                                            fullItem.getThumbnail(),
                                            fullItem.getBackgroundImage(),
                                            fullItem.getVideos(),
                                            fullItem.getImages());
                                    if (TextUtils.isEmpty(fullYtId)) {
                                        fullYtId = YouTubeIdHelper.extract(fullItem.getVideoUrl());
                                    }
                                    if (!TextUtils.isEmpty(fullYtId)) {
                                        YouTubePlayerUtils.loadOrCueVideo(
                                                youTubePlayer, lifecycle,
                                                fullYtId, 0f
                                        );
                                    }

                                }
                            };
                            IFramePlayerOptions options = new IFramePlayerOptions.Builder(context).controls(0).build();
                            youTubePlayerView.initialize(listener, false, options);

                        }
                    }, 100);
                }

            }
        });

        // Poster tap = play YouTube immediately (same path as center play).
        if (newFullImageView1 != null) {
            newFullImageView1.setOnClickListener(v -> {
                if (imageViewYoutubePlayVideo != null
                        && imageViewYoutubePlayVideo.getVisibility() == View.VISIBLE) {
                    imageViewYoutubePlayVideo.performClick();
                }
            });
        }




        like_for_video_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fullVideoNewYoutubeViewHolder.like_for_video_image = convertView.findViewById(R.id.like_for_video_image);
                int newStatus = applyLikeChange(position, fullVideoNewYoutubeViewHolder.full_video_like);
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
        if (!tryAcquireShareLaunch()) {
            return;
        }

        String mInvitationUrl = list.get(position).getShareLink();
        String msg = ShareOutboundHelper.formatCaption(list.get(position).getTitle(), mInvitationUrl, list.get(position).getPostId());
        side_menu.setVisibility(View.INVISIBLE);
        youTubePlayerView.setVisibility(GONE);
        newFullImageView1.setVisibility(VISIBLE);
        imageViewYoutubePlayVideo.setVisibility(VISIBLE);
        countDownTimer = new CountDownTimer(2, 2) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_SUBJECT, mInvitationUrl);
                intent.putExtra(Intent.EXTRA_TEXT, msg);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Bitmap b = ScreenShott.getInstance().takeScreenShotOfView(layoutYoutubeFullView);
                try {
                    writeScreenshotPngAndAttach(intent, b);
                } catch (Exception e) {
                    e.printStackTrace();
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
                launchScreenshotShareIntent(intent, from,
                        () -> Share_Count_Value(
                                shareCountProperty(from == 1 || from == 2),
                                list.get(position).getPostId()));

            }
        }.start();

    }

    private void showFullVideoWithExoPlayer(int position, View convertView) {
        applyFullSideActionGap(context, convertView);
        RelativeLayout layoutMain = convertView.findViewById(R.id.parent_layout);
        PlayerView simpleExoPlayerViewFull  = convertView.findViewById(R.id.normal_video_player);
        if (!isFeedExoSurfaceLive(simpleExoPlayerViewFull)
                && layoutMain.getLayoutParams().height != screenHeight) {
            layoutMain.getLayoutParams().height = screenHeight;
        }
        full_videoViewHolder.full_video_like = convertView.findViewById(R.id.full_video_like_text);
        full_videoViewHolder.full_video_dislike = convertView.findViewById(R.id.full_video_dislike_text);
        full_videoViewHolder.full_video_share = convertView.findViewById(R.id.full_video_share_text);
        full_videoViewHolder.full_video_comment = convertView.findViewById(R.id.full_video_comment_text);
        full_videoViewHolder.exo_play_layout = convertView.findViewById(R.id.exo_play_layout);

        hardenFeedPlayerSurface(simpleExoPlayerViewFull);
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
                full_videoViewHolder.full_video_share,
                convertView.findViewById(R.id.full_img_whtsapp_text), position);

        full_videoViewHolder.full_image_whatsapp_bg.setImageResource(R.drawable.ic_whatsapp_original1);

        final ImageView iv_comment_for_video = convertView.findViewById(R.id.iv_comment_for_video);
        final ImageView iv_share_for_video = convertView.findViewById(R.id.iv_share_for_video);
        final ImageView like_for_video_image = convertView.findViewById(R.id.like_for_video_image);
        final CircleImageView user_pic = convertView.findViewById(R.id.user_pic);
        iv_comment_for_video.setImageResource(R.drawable.chatbox);
        iv_share_for_video.setImageResource(R.drawable.ic_share_full);
        like_for_video_image.setImageResource(R.drawable.ic_like_half1);
        AvatarImageHelper.loadUil(imageLoader, user_pic, list.get(position).getWriterProfile());
        final RelativeLayout exo_play_layout = convertView.findViewById(R.id.exo_play_layout);
        final ImageView thaumbnail_for_video = convertView.findViewById(R.id.thaumbnail_for_video);
        final ImageView imageViewExoVideoPlayVideo = convertView.findViewById(R.id.imageViewVideoPlayVideo);
        imageViewExoVideoPlayVideo.setImageResource(R.drawable.ic_way2_play_circle);

        final ImageButton fullscreen_button = convertView.findViewById(R.id.fullscreen_button);
        final ImageButton mute_button = convertView.findViewById(R.id.mute_button);
        final ProgressBar progress_bar_for_video = convertView.findViewById(R.id.progress_bar_for_video);
        if (isFeedExoSurfaceLive(simpleExoPlayerViewFull)) {
            setFeedExoPosterCover(thaumbnail_for_video, simpleExoPlayerViewFull, false);
            hideExoSpinnerNow(progress_bar_for_video);
        } else if (list.get(position).getVideoUrl() != null
                && isValidContextForGlide(context)
                && !isSharedExoActiveFor(position)
                && !isLiveExoOnView(simpleExoPlayerViewFull, position)) {
            loadArticleImageGlide(
                    convertView.getContext(),
                    thaumbnail_for_video,
                    list.get(position),
                    list.get(position).getThumbnail());
            setFeedExoPosterCover(thaumbnail_for_video, simpleExoPlayerViewFull, true);
        } else if (!isSharedExoActiveFor(position)
                && !isLiveExoOnView(simpleExoPlayerViewFull, position)) {
            setFeedExoPosterCover(thaumbnail_for_video, simpleExoPlayerViewFull, true);
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

        // Re-bind while the same card is still owning the shared player: re-attach, do not reset to play.
        if (isSharedExoActiveFor(position) || isFeedExoSurfaceLive(simpleExoPlayerViewFull)) {
            // Never detach→reattach the same player — that clears the picture
            // and flashes a white hole / poster over an already-playing frame.
            hardenFeedPlayerSurface(simpleExoPlayerViewFull);
            boolean reboundSurface = attachSharedExoToPlayerView(simpleExoPlayerViewFull);
            if (simpleExoPlayerViewFull.getVisibility() != View.VISIBLE) {
                simpleExoPlayerViewFull.setVisibility(View.VISIBLE);
            }
            if (simpleExoPlayerViewFull.getResizeMode() != AspectRatioFrameLayout.RESIZE_MODE_FIT) {
                simpleExoPlayerViewFull.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            }
            wireFullVideoExoPauseControl(simpleExoPlayerViewFull, convertView);
            wireFullVideoExoSideMenuWithController(simpleExoPlayerViewFull, convertView);
            applyExoOverlayState(
                    exoPlayer.getPlaybackState(),
                    exoPlayer.getPlayWhenReady(),
                    thaumbnail_for_video,
                    imageViewExoVideoPlayVideo,
                    progress_bar_for_video,
                    simpleExoPlayerViewFull);
            if (full_videoViewHolder.side_menu != null && exoPlayer.getPlayWhenReady()
                    && exoPlayer.getPlaybackState() != Player.STATE_ENDED) {
                full_videoViewHolder.side_menu.setVisibility(View.INVISIBLE);
            }
            // New PlayerView (surface moved) needs listeners bound to these UI refs.
            // Scrap reuse with player already attached keeps prior listeners.
            if (reboundSurface) {
                attachExoCenterIconSync(simpleExoPlayerViewFull);
                final PlayerView resumeFullPlayerView = simpleExoPlayerViewFull;
                exoPlayer.addListener(new Player.Listener() {
                    @Override
                    public void onPlaybackStateChanged(int playbackState) {
                        if (exoPlayer == null || resumeFullPlayerView.getPlayer() != exoPlayer) {
                            return;
                        }
                        applyExoOverlayState(
                                playbackState,
                                exoPlayer.getPlayWhenReady(),
                                thaumbnail_for_video,
                                imageViewExoVideoPlayVideo,
                                progress_bar_for_video,
                                resumeFullPlayerView);
                        updateExoCenterControlIcons(resumeFullPlayerView);
                        if (playbackState == Player.STATE_ENDED) {
                            notifyVideoEndedForAutoplay(position);
                        }
                    }
                });
            } else {
                updateExoCenterControlIcons(simpleExoPlayerViewFull);
            }
            // Stay playing if user already started this card.
            if (exoPlayer.getPlayWhenReady()) {
                forceSharedExoPlay();
            }
        }

        imageViewExoVideoPlayVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fullscreen = false;
                mute_button.setImageResource(ic__volume_up);
                String videoUrl = list.get(position).getVideoUrl();
                if (TextUtils.isEmpty(videoUrl)) {
                    Toast.makeText(context, "Video is not available", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (isLiveExoOnView(simpleExoPlayerViewFull, position)) {
                    // Same card already playing — do not release/recreate the surface.
                    forceSharedExoPlay();
                    imageViewExoVideoPlayVideo.setVisibility(View.GONE);
                    setFeedExoPosterCover(thaumbnail_for_video, simpleExoPlayerViewFull, false);
                    hideExoSpinnerNow(progress_bar_for_video);
                    return;
                }
                imageViewExoVideoPlayVideo.setVisibility(View.GONE);
                thaumbnail_for_video.setVisibility(View.VISIBLE);
                progress_bar_for_video.setVisibility(View.VISIBLE);
                displayArticleImage(
                        thaumbnail_for_video,
                        list.get(position),
                        list.get(position).getThumbnail());
                hardenFeedPlayerSurface(simpleExoPlayerViewFull);
                simpleExoPlayerViewFull.setVisibility(View.VISIBLE);
                releaseSharedExoPlayerQuietly();
                exoPlayer = createFeedExoPlayer();
                attachSharedExoToPlayerView(simpleExoPlayerViewFull);
                if (simpleExoPlayerViewFull.getResizeMode() != AspectRatioFrameLayout.RESIZE_MODE_FIT) {
                    simpleExoPlayerViewFull.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
                }
                MediaItem mediaItem = MediaItem.fromUri(Uri.parse(videoUrl));
                // Start at 0. Pause/resume uses the live player and does not come through here.
                exoPlayer.setMediaItem(mediaItem);
                exoPlayer.seekTo(0);
                exoPlayer.prepare();
                // Explicit center-play tap must always start playback (not settings autoplay).
                forceSharedExoPlay();
                markSharedExoActive(position, videoUrl);
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
                        if (exoPlayer == null) {
                            return;
                        }
                        boolean playWhenReady = exoPlayer.getPlayWhenReady();
                        showLog("onPlayerStateChanged full::" + playWhenReady + " state=" + playbackState);
                        applyExoOverlayState(
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
                        }
                        if (playbackState == Player.STATE_ENDED) {
                            notifyVideoEndedForAutoplay(position);
                        }
                        updateExoCenterControlIcons(simpleExoPlayerViewFull);
                    }

                    @Override
                    public void onPlayerError(@NonNull PlaybackException error) {
                        showLog("full exo error::" + error.getErrorCodeName());
                        feedExoFrameRendered = false;
                        progress_bar_for_video.setVisibility(View.GONE);
                        setFeedExoPosterCover(thaumbnail_for_video, simpleExoPlayerViewFull, true);
                        imageViewExoVideoPlayVideo.setVisibility(View.VISIBLE);
                        LinearLayout sideMenu = finalConvertViewFull.findViewById(R.id.video_side_menu);
                        if (sideMenu != null) {
                            sideMenu.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onIsPlayingChanged(boolean isPlaying) {
                        LinearLayout sideMenu = finalConvertViewFull.findViewById(R.id.video_side_menu);
                        // Rebuffer sets isPlaying=false while playWhenReady stays true.
                        // Don't pop engagement chrome over the picture on that blip.
                        boolean userPaused = exoPlayer == null || !exoPlayer.getPlayWhenReady();
                        if (sideMenu != null && !isPlaying && userPaused) {
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
                int newStatus = applyLikeChange(position, full_videoViewHolder.full_video_like);
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
        if (!tryAcquireShareLaunch()) {
            return;
        }
        String mInvitationUrl = list.get(position).getShareLink();
        String msg = ShareOutboundHelper.formatCaption(list.get(position).getTitle(), mInvitationUrl, list.get(position).getPostId());
        simpleExoPlayerViewFull.setVisibility(GONE);
        thaumbnail_for_video.setVisibility(VISIBLE);
        imageViewExoVideoPlayVideo.setVisibility(VISIBLE);
        countDownTimer = new CountDownTimer(2, 2) {
            public void onTick(long millisUntilFinished) {
            }
            public void onFinish() {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_SUBJECT, mInvitationUrl);
                intent.putExtra(Intent.EXTRA_TEXT, msg);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Bitmap b = ScreenShott.getInstance().takeScreenShotOfView(exo_play_layout);
                try {
                    writeScreenshotPngAndAttach(intent, b);
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
                launchScreenshotShareIntent(intent, from,
                        () -> Share_Count_Value(
                                shareCountProperty(from == 1 || from == 2),
                                list.get(position).getPostId()));
            }
        }.start();

    }

    private void showFullImage(int position, View convertView) {
        applyFullSideActionGap(context, convertView);
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

        AvatarImageHelper.loadUil(imageLoader, fullImageActivity.user_pic, list.get(position).getWriterProfile());
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
            showLog("Check Point FULL_IMAGE:: 3:: " + imageUrl);
            displayArticleImage(fullImageActivity.mimageView, list.get(position), imageUrl);
            showLog("Check Point FULL_IMAGE:: 4");
        } else {
            String alt = OfflineImageStore.primaryImageUrl(list.get(position));
            if (!TextUtils.isEmpty(alt)) {
                displayArticleImage(fullImageActivity.mimageView, list.get(position), alt);
            } else {
                // Empty images[] must not leave a scrap FlipView's prior birthday/hero bitmap.
                clearArticleHeroImage(fullImageActivity.mimageView);
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
            fullImageActivity.strip_text_name.setText(displayCopy(list.get(position).getStrip_content()));
            applyStripFont(fullImageActivity.strip_text_name);

        } else if (LanguageId == 1) {
            fullImageActivity.strip_text_name.setText(displayCopy(list.get(position).getStrip_content()));
            applyStripFont(fullImageActivity.strip_text_name);

        } else {
            fullImageActivity.strip_text_name.setText(displayCopy(list.get(position).getStrip_content()));
            applyStripFont(fullImageActivity.strip_text_name);

        }
        fullImageActivity.full_img_like = convertView.findViewById(R.id.full_img_like_text);
        fullImageActivity.full_img_share = convertView.findViewById(R.id.full_image_share_text);
        fullImageActivity.full_img_whtsapp = convertView.findViewById(R.id.full_img_whtsapp_text);
        fullImageActivity.full_img_comment = convertView.findViewById(R.id.full_img_comment_txt);

//                        Set Text
        fullImageActivity.strip_text_name.setText(displayCopy(list.get(position).getStrip_content()));
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

        // Mode-aware Classic / Reader / Hybrid (also re-bound in installOfflineChromeTaps).
        installNewsModeTap(
                fullImageActivity.full_image_view_details,
                convertView,
                fullImageActivity.side_menu,
                true,
                resolveHomePageHost(context));
        installOfflineChromeTaps(convertView);
        showLog("like status:"+list.get(position).getLikeStatus());

        final TextView likeCountViewRef = convertView.findViewById(R.id.full_img_like_text);
        final ImageView likeIconRef = full_image_selected_like;
        rememberLikeCountView(list.get(position).getPostId(), likeCountViewRef);
        full_image_like.setTag(position);
        full_image_like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = (int) view.getTag();
                int newStatus = applyLikeChange(pos, likeCountViewRef);
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
                if (!tryAcquireShareLaunch()) {
                    return;
                }

                fullImageActivity.full_image_share = myFullView.findViewById(R.id.full_image_share);
                fullImageActivity.layout_strip_iconnew = myFullView.findViewById(R.id.layout_strip_iconnew);
                fullImageActivity.full_image_view_details = myFullView.findViewById(R.id.full_image_view_details);
                if (fullImageActivity.full_image_view_details == null) {
                    releaseShareLaunch();
                    Toast.makeText(context, "Unable to share this post right now", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (fullImageActivity.layout_strip_iconnew != null) {
                    fullImageActivity.layout_strip_iconnew.setVisibility(View.GONE);
                }

                final String mInvitationUrl = list.get(position).getShareLink();
                final String msg = ShareOutboundHelper.formatCaption(list.get(position).getTitle(), mInvitationUrl, list.get(position).getPostId());
                Intent base = new Intent();
                base.putExtra(Intent.EXTRA_SUBJECT, mInvitationUrl);
                base.putExtra(Intent.EXTRA_TEXT, msg);
                prepareAndLaunchScreenshotShare(
                        fullImageActivity.full_image_view_details,
                        base,
                        false,
                        () -> {
                            if (fullImageActivity.layout_strip_iconnew != null) {
                                fullImageActivity.layout_strip_iconnew.setVisibility(View.GONE);
                            }
                        },
                        () -> Share_Count_Value("share", list.get(position).getPostId()));
            }
        });
        bindSponsoredChrome(convertView, position);
    }

    public void shareFullImageScree(View myFullView, int position) {

        if (!tryAcquireShareLaunch()) {
            return;
        }
        if (myFullView == null) {
            releaseShareLaunch();
            Toast.makeText(context, "Unable to share this post right now", Toast.LENGTH_SHORT).show();
            return;
        }
        fullImageActivity.full_image_share = myFullView.findViewById(R.id.full_image_share);
        fullImageActivity.layout_strip_iconnew = myFullView.findViewById(R.id.layout_strip_iconnew);
        fullImageActivity.full_image_view_details = myFullView.findViewById(R.id.full_image_view_details);
        if (fullImageActivity.full_image_view_details == null) {
            releaseShareLaunch();
            Toast.makeText(context, "Unable to share this post right now", Toast.LENGTH_SHORT).show();
            return;
        }

        if (fullImageActivity.layout_strip_iconnew != null) {
            fullImageActivity.layout_strip_iconnew.setVisibility(View.GONE);
        }

        final String mInvitationUrl = list.get(position).getShareLink();
        final String msg = ShareOutboundHelper.formatCaption(list.get(position).getTitle(), mInvitationUrl, list.get(position).getPostId());

        countDownTimer = new CountDownTimer(2, 2) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                try {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("image/*");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setPackage("com.whatsapp");
                    intent.putExtra(Intent.EXTRA_SUBJECT, mInvitationUrl);
                    intent.putExtra(Intent.EXTRA_TEXT, msg);

                    Bitmap b = ScreenShott.getInstance()
                            .takeScreenShotOfView(fullImageActivity.full_image_view_details);
                    try {
                        writeScreenshotPngAndAttach(intent, b);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (fullImageActivity.layout_strip_iconnew != null) {
                        fullImageActivity.layout_strip_iconnew.setVisibility(View.GONE);
                    }
                    // Same signal as normal Share: count only after a chooser target is picked.
                    if (context != null && context.getPackageName().equals(BuildConfig.APPLICATION_ID)) {
                        final long waPostId = list.get(position).getPostId();
                        ShareOutboundHelper.launchChooserOnTargetChosen(
                                context,
                                intent,
                                "Share",
                                () -> Share_Count_Value("whatsapp_share", waPostId));
                    }
                } finally {
                    releaseShareLaunch();
                }
            }
        }.start();
    }

    public void shareSurveyScreenshot(View flipChild, int position) {
        if (!tryAcquireShareLaunch()) {
            return;
        }
        if (flipChild == null) {
            releaseShareLaunch();
            Toast.makeText(context, "Unable to share this post right now", Toast.LENGTH_SHORT).show();
            return;
        }
        View shareTarget = flipChild.findViewById(R.id.rl_survay_main);
        if (shareTarget == null) {
            shareTarget = flipChild.findViewById(R.id.card_view_survey);
        }
        if (shareTarget == null) {
            releaseShareLaunch();
            Toast.makeText(context, "Unable to share this post right now", Toast.LENGTH_SHORT).show();
            return;
        }
        if (list == null || position < 0 || position >= list.size()) {
            releaseShareLaunch();
            return;
        }
        final View screenshotView = shareTarget;
        String mInvitationUrl = list.get(position).getShareLink();
        String msg = ShareOutboundHelper.formatCaption(list.get(position).getTitle(), mInvitationUrl, list.get(position).getPostId());
        countDownTimer = new CountDownTimer(2, 2) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                try {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("image/*");
                    intent.setPackage("com.whatsapp");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(Intent.EXTRA_SUBJECT, mInvitationUrl);
                    intent.putExtra(Intent.EXTRA_TEXT, msg);
                    Bitmap b = ScreenShott.getInstance().takeScreenShotOfView(screenshotView);
                    try {
                        writeScreenshotPngAndAttach(intent, b);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (context != null && context.getPackageName().equals(BuildConfig.APPLICATION_ID)) {
                        final long surveyPostId = list.get(position).getPostId();
                        ShareOutboundHelper.launchChooserOnTargetChosen(
                                context,
                                intent,
                                "Share",
                                () -> Share_Count_Value("whatsapp_share", surveyPostId));
                    }
                } finally {
                    releaseShareLaunch();
                }
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
        if (halfImageActivity.ll_footer_layout != null) {
            showLog("ll_footer_layout:::" + halfImageActivity.ll_footer_layout.getVisibility());
            // Always show writers/reporters on bind; Classic hide is tap-only.
            halfImageActivity.ll_footer_layout.setVisibility(VISIBLE);
        }
        halfImageActivity.parent_layout = convertView.findViewById(R.id.parent_layout);
        AvatarImageHelper.loadUil(imageLoader, halfImageActivity.writer_profile_pic, list.get(position).getWriterProfile());
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
        halfImageActivity.report_icon.setImageResource(R.drawable.ic_action_more_vert);
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
        // In-feed URL chip: share like the share icon — do not ACTION_VIEW web_url
        // (App Links would reopen this same article). External WhatsApp/browser opens unchanged.
        textViewWebUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takeNormalImageScreenshot(convertView, position, 0);
            }
        });

        bindHalfPostEngagementCounts(convertView, position);
        bindSponsoredChrome(convertView, position);

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

            halfImageActivity.english_half_content_description.setText(displayCopy(list.get(position).getDescription()));
            halfImageActivity.english_half_content_title.setText(displayCopy(list.get(position).getTitle()));
            halfImageActivity.strip_text_name.setText(displayCopy(list.get(position).getStrip_content()));
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
            halfImageActivity.half_content_description.setText(displayCopy(list.get(position).getDescription()));
            halfImageActivity.half_content_title.setText(displayCopy(list.get(position).getTitle()));
            halfImageActivity.strip_text_name.setText(displayCopy(list.get(position).getStrip_content()));
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
            showLog("Check Point normal image:: " + halfImageUrl);
            applyHalfFeedImageScale(halfImageActivity.new_half_image, list.get(position));
            displayArticleImage(halfImageActivity.new_half_image, list.get(position), halfImageUrl);
        } else if (list.get(position) != null) {
            // Fallback: thumbnail / background when images[] empty
            String alt = OfflineImageStore.primaryImageUrl(list.get(position));
            if (!TextUtils.isEmpty(alt)) {
                applyHalfFeedImageScale(halfImageActivity.new_half_image, list.get(position));
                displayArticleImage(halfImageActivity.new_half_image, list.get(position), alt);
            } else {
                clearArticleHeroImage(halfImageActivity.new_half_image);
            }
        } else {
            clearArticleHeroImage(halfImageActivity.new_half_image);
        }
        showLog("Check Point Adapter:: 4");

        // hide and visible — mode-aware Classic / Reader / Hybrid
        installNewsModeTap(
                halfImageActivity.parent_layout,
                convertView,
                halfImageActivity.ll_footer_layout,
                false,
                resolveHomePageHost(context));
        // Body text must keep chrome hide after parent listener is set.
        installOfflineChromeTaps(convertView);



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
        bindWriterStripIdentity(
                list.get(position),
                halfImageActivity.writer_name,
                halfImageActivity.writer_designation,
                halfImageActivity.writer_time);

        WriterStripStyleHelper.apply(
                context,
                halfImageActivity.writer_name,
                halfImageActivity.writer_designation,
                halfImageActivity.writer_time,
                halfImageActivity.my_feed,
                halfImageActivity.ll_refer_and_earn_half
        );
        // Style may recolor the pill label; re-bind text/visibility so district never vanishes.
        bindLocationPill(
                halfImageActivity.my_feed,
                halfImageActivity.iv_share_and_earn,
                halfImageActivity.ll_refer_and_earn_half,
                list.get(position));

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
        // Must run after bindLocationPill so Rewards chip never falls through to Local.
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
                showArticleMenu(position);
            }
        });

        showLog("Check Point Adapter:: 11");
        if (Boolean.TRUE.equals(list.get(position).getIs_source())) {
            descriptionText = displayCopy(list.get(position).getDescription());
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

            if (Boolean.TRUE.equals(list.get(position).getIs_source())) {

                if (LanguageId == 4) {
                    halfImageActivity.tamil_half_content_description.setText(ssText);
                } else if (LanguageId == 1) {
                    halfImageActivity. english_half_content_description.setText(ssText);
                } else {
                    halfImageActivity.half_content_description.setText(ssText);
                }
            } else {
                halfImageActivity. half_content_description.setText(displayCopy(list.get(position).getDescription()));
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

        bindArticleTtsSpeaker(convertView, position);

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

                // half gallery images — prefer offline files when pack warmed
                halfImageGallery = new ArrayList<>();
                ArrayList<String> listSample =
                        GalleryImageUrls.sanitize(
                                OfflineImageStore.resolveGalleryUris(
                                        context, list.get(position).getPostId(), halfGalleryImages));
                halfImageGallery.addAll(listSample);
                try {
                    OfflineImageStore.enqueueArticle(context, list.get(position));
                } catch (Exception ignored) {
                }
                SamplePagerAdapter halfGalleryAdapter = new SamplePagerAdapter(
                        context,
                        halfImageGallery,
                        layoutHeight,
                        true,
                        list.get(position).usesContainFeedImage());
                halfImageActivity.viewPager.setAdapter(halfGalleryAdapter);

                // added to increase speed in view pager
                halfImageActivity.viewPager.setOffscreenPageLimit(Math.max(1, Math.min(2, listSample.size() - 1)));
                if (indicator != null) {
                    indicator.setViewPager(halfImageActivity.viewPager);
                }
                SamplePagerAdapter.schedulePage0Ensure(
                        halfImageActivity.viewPager, halfGalleryAdapter);
                TextView halfSwipeHint =
                        convertView.findViewById(R.id.gallery_swipe_left_hint);
                GallerySwipeHintHelper.bind(
                        halfImageActivity.viewPager, halfSwipeHint, listSample.size());
                if (halfImageActivity.ll_footer_layout != null) {
                    halfImageActivity.ll_footer_layout.setVisibility(VISIBLE);
                }
            } else {
                halfImageActivity.galleryImageLayout.setVisibility(GONE);
                halfImageActivity.new_half_image.setVisibility(VISIBLE);
                new_half_image.setVisibility(VISIBLE);
                GallerySwipeHintHelper.clear(halfImageActivity.viewPager);
            }

        }else{
            halfImageActivity.galleryImageLayout.setVisibility(GONE);
            halfImageActivity.new_half_image.setVisibility(VISIBLE);
            new_half_image.setVisibility(VISIBLE);
            if (halfImageActivity.viewPager != null) {
                GallerySwipeHintHelper.clear(halfImageActivity.viewPager);
            }
        }

    }

    public void takeNormalImageScreenshot(View finalConvertView1, int customPosition,int from) {
        {
            showLog("ll_footer_layout::: takeNormalImageScreenshot");
            if (!tryAcquireShareLaunch()) {
                return;
            }
            if (finalConvertView1 == null) {
                releaseShareLaunch();
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
                releaseShareLaunch();
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
                releaseShareLaunch();
                return;
            }
            String mInvitationUrl = list.get(customPosition).getShareLink();
            String msg = ShareOutboundHelper.formatCaption(list.get(customPosition).getTitle(), mInvitationUrl, list.get(customPosition).getPostId());
            countDownTimer = new CountDownTimer(2, 2) {
                public void onTick(long millisUntilFinished) {
                }
                public void onFinish() {
                    try {
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("image/*");
                        if (from == 1) {
                            intent.setPackage("com.whatsapp");
                        }
                        intent.putExtra(Intent.EXTRA_SUBJECT, mInvitationUrl);
                        intent.putExtra(Intent.EXTRA_TEXT, msg);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        Bitmap b = ScreenShott.getInstance().takeScreenShotOfView(halfImageActivity.relative_half_content);
                        try {
                            writeScreenshotPngAndAttach(intent, b);
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
                        if (context != null && context.getPackageName().equals(BuildConfig.APPLICATION_ID)) {
                            showLog("Halfshare"+ context + "First" + context.getPackageName() + "Second" + BuildConfig.APPLICATION_ID);
                            final long halfPostId = list.get(customPosition).getPostId();
                            final boolean halfWhatsapp = from == 1;
                            // Count only when user picks a target — not on sheet open/dismiss.
                            ShareOutboundHelper.launchChooserOnTargetChosen(
                                    context,
                                    intent,
                                    "Share Link",
                                    () -> Share_Count_Value(
                                            shareCountProperty(halfWhatsapp), halfPostId));
                        }
                    } finally {
                        releaseShareLaunch();
                    }
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
        PlayerView  simpleExoPlayerView = convertView.findViewById(R.id.normal_video_player);
        if (!isFeedExoSurfaceLive(simpleExoPlayerView)
                && layoutMain.getLayoutParams().height != screenHeight) {
            layoutMain.getLayoutParams().height = screenHeight;
        }
        hardenFeedPlayerSurface(simpleExoPlayerView);
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

        if (!isFeedExoSurfaceLive(simpleExoPlayerView)) {
            halfVideoExoPlayerHolder.layoutParentTop.getLayoutParams().height = getVideoLayoutTopHeight();
        }


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
        bindSponsoredChrome(convertView, position);

        bindHalfLikeIcon(halfVideoExoPlayerHolder.half_image_like, list.get(position).getLikeStatus());

        halfVideoExoPlayerHolder.half_comment_icon = convertView.findViewById(R.id.half_comment_icon);
        halfVideoExoPlayerHolder.share = convertView.findViewById(R.id.share);
        halfVideoExoPlayerHolder.video_report = convertView.findViewById(R.id.video_report);
        halfVideoExoPlayerHolder.half_comment_icon.setImageResource(R.drawable.ic_action_comment);
        halfVideoExoPlayerHolder.share.setImageResource(R.drawable.ic_action_share);
        halfVideoExoPlayerHolder.video_report.setImageResource(R.drawable.ic_action_more_vert);
        tintHalfActionIcons(convertView);


        halfVideoExoPlayerHolder.writer_follow = convertView.findViewById(R.id.writer_follow);
        halfVideoExoPlayerHolder.writer_follower = convertView.findViewById(R.id.writer_follower);
        final TextView test_folloew = convertView.findViewById(R.id.test_folloew);
        halfVideoExoPlayerHolder.ll_refer_and_earn_halfy = convertView.findViewById(R.id.ll_refer_and_earn_half);
        halfVideoExoPlayerHolder.iv_share_and_earn = convertView.findViewById(R.id.iv_share_and_earn);
        halfVideoExoPlayerHolder.my_feed = convertView.findViewById(R.id.my_feed);
        halfVideoExoPlayerHolder.parent_layout.setOnClickListener(null);
        installNewsModeTap(
                halfVideoExoPlayerHolder.parent_layout,
                convertView,
                layoutParentReportDetails,
                false,
                resolveHomePageHost(context));
        installOfflineChromeTaps(convertView);
        TextView textViewWebUrl=convertView.findViewById(R.id.textViewWebUrl);
        TextView textViewStripWebUrl=convertView.findViewById(R.id.textViewStripWebUrl);
        if(list.get(position).getWeb_url()!=null && !list.get(position).getWeb_url().equalsIgnoreCase("")){
            textViewWebUrl.setText(list.get(position).getWeb_url().replace("https://",""));
            textViewStripWebUrl.setText(list.get(position).getWeb_url().replace("https://",""));
        } else {
            textViewWebUrl.setText("");
            textViewStripWebUrl.setText("");
        }
        // In-feed URL chip: same share flow as half_image_share (not ACTION_VIEW / App Link).
        textViewWebUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showVideoShareChooser(position, VideoShareLayout.HALF_EXO, convertView);
            }
        });
        if (categery_id == 0) {
            bindLocationPill(
                    halfVideoExoPlayerHolder.my_feed,
                    halfVideoExoPlayerHolder.iv_share_and_earn,
                    halfVideoExoPlayerHolder.ll_refer_and_earn_halfy,
                list.get(position));

        } else {
            bindLocationPill(
                    halfVideoExoPlayerHolder.my_feed,
                    halfVideoExoPlayerHolder.iv_share_and_earn,
                    halfVideoExoPlayerHolder.ll_refer_and_earn_halfy,
                list.get(position));

        }
        textViewPostDate.setText(list.get(position).getPost_date_time());

        if (LanguageId == 4) {

            english_half_content_description.setVisibility(View.GONE);
            english_half_content_title.setVisibility(View.GONE);

            half_content_description.setVisibility(View.GONE);
            half_content_title.setVisibility(View.GONE);

            tamil_half_content_description.setVisibility(View.VISIBLE);
            tamil_half_content_title.setVisibility(View.VISIBLE);

            tamil_half_content_description.setText(displayCopy(list.get(position).getDescription()));
            tamil_half_content_title.setText(displayCopy(list.get(position).getTitle()));
            halfVideoExoPlayerHolder.strip_text_name.setText(displayCopy(list.get(position).getStrip_content()));
            applyStripFont(halfVideoExoPlayerHolder.strip_text_name);
            FontSettingsHelper.applyArticleTypography(context, 4, tamil_half_content_title, tamil_half_content_description, "half_video_tamil");

        } else if (LanguageId == 1) {

            tamil_half_content_description.setVisibility(View.GONE);
            half_content_title.setVisibility(View.GONE);
            half_content_description.setVisibility(View.GONE);
            half_content_title.setVisibility(View.GONE);

            english_half_content_description.setVisibility(View.VISIBLE);
            english_half_content_title.setVisibility(View.VISIBLE);

            english_half_content_description.setText(displayCopy(list.get(position).getDescription()));
            english_half_content_title.setText(displayCopy(list.get(position).getTitle()));
            halfVideoExoPlayerHolder.strip_text_name.setText(displayCopy(list.get(position).getStrip_content()));
            applyStripFont(halfVideoExoPlayerHolder.strip_text_name);
            FontSettingsHelper.applyArticleTypography(context, 1, english_half_content_title, english_half_content_description, "half_video_english");

        } else {
            english_half_content_description.setVisibility(View.GONE);
            english_half_content_title.setVisibility(View.GONE);
            tamil_half_content_description.setVisibility(View.GONE);
            half_content_title.setVisibility(View.GONE);
            half_content_description.setVisibility(View.VISIBLE);
            half_content_title.setVisibility(View.VISIBLE);
            half_content_description.setText(displayCopy(list.get(position).getDescription()));
            half_content_title.setText(displayCopy(list.get(position).getTitle()));
            halfVideoExoPlayerHolder.strip_text_name.setText(displayCopy(list.get(position).getStrip_content()));
            applyStripFont(halfVideoExoPlayerHolder.strip_text_name);
            FontSettingsHelper.applyArticleTypography(context, LanguageId, half_content_title, half_content_description, "half_video_default");
        }
        bindWriterStripIdentity(
                list.get(position),
                halfVideoExoPlayerHolder.writer_name,
                halfVideoExoPlayerHolder.writer_designation,
                halfVideoExoPlayerHolder.writer_time);
        WriterStripStyleHelper.apply(
                context,
                halfVideoExoPlayerHolder.writer_name,
                halfVideoExoPlayerHolder.writer_designation,
                halfVideoExoPlayerHolder.writer_time,
                halfVideoExoPlayerHolder.my_feed,
                halfVideoExoPlayerHolder.ll_refer_and_earn_halfy
        );
        bindLocationPill(
                halfVideoExoPlayerHolder.my_feed,
                halfVideoExoPlayerHolder.iv_share_and_earn,
                halfVideoExoPlayerHolder.ll_refer_and_earn_halfy,
                list.get(position));
        int width = displayMetrics.widthPixels;
        //halfVideoExoPlayerHolder.imageViewItems.setVisibility(View.VISIBLE);
        //halfVideoExoPlayerHolder.youTubePlayerView.setVisibility(View.GONE);



        if (isFeedExoSurfaceLive(simpleExoPlayerView)) {
            setFeedExoPosterCover(thaumbnail_for_video, simpleExoPlayerView, false);
            hideExoSpinnerNow(progress_bar_for_video);
        } else if (list.get(position).getTitle() != null){
            if (list.get(position).getVideoUrl() != null
                    && isValidContextForGlide(context)
                    && !isSharedExoActiveFor(position)
                    && !isLiveExoOnView(simpleExoPlayerView, position)) {
                displayArticleImage(
                        thaumbnail_for_video,
                        list.get(position),
                        list.get(position).getThumbnail());
                setFeedExoPosterCover(thaumbnail_for_video, simpleExoPlayerView, true);
            }
        }

//        if (exoPlayer != null) {
//            exoPlayer.setPlayWhenReady(false);
//            exoPlayer.stop();
//            exoPlayer.seekTo(0);
//            //youTubePlayerViewHalf.cueVideo(videoURL,0);
//        }
//        exoPlayer=null;
        boolean resumeSharedExo = isSharedExoActiveFor(position);
        boolean keepLiveHalfSurface = resumeSharedExo || isFeedExoSurfaceLive(simpleExoPlayerView);
        Player attachedHalfPlayer = simpleExoPlayerView.getPlayer();
        if (attachedHalfPlayer != null && attachedHalfPlayer != exoPlayer) {
            try {
                attachedHalfPlayer.release();
            } catch (Exception ignored) {
            }
        }
        if (resumeSharedExo || isFeedExoSurfaceLive(simpleExoPlayerView)) {
            hardenFeedPlayerSurface(simpleExoPlayerView);
            boolean reboundSurface = attachSharedExoToPlayerView(simpleExoPlayerView);
            if (simpleExoPlayerView.getVisibility() != View.VISIBLE) {
                simpleExoPlayerView.setVisibility(View.VISIBLE);
            }
            if (simpleExoPlayerView.getResizeMode() != AspectRatioFrameLayout.RESIZE_MODE_FIT) {
                simpleExoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            }
            imageViewExoVideoPlayVideo.setVisibility(View.GONE);
            applyExoOverlayState(
                    exoPlayer.getPlaybackState(),
                    exoPlayer.getPlayWhenReady(),
                    thaumbnail_for_video,
                    imageViewExoVideoPlayVideo,
                    progress_bar_for_video,
                    simpleExoPlayerView);
            wireHalfVideoExoPauseControl(simpleExoPlayerView);
            if (reboundSurface) {
                attachExoCenterIconSync(simpleExoPlayerView);
                final PlayerView resumeHalfPlayerView = simpleExoPlayerView;
                exoPlayer.addListener(new Player.Listener() {
                    @Override
                    public void onPlaybackStateChanged(int playbackState) {
                        if (exoPlayer == null || resumeHalfPlayerView.getPlayer() != exoPlayer) {
                            return;
                        }
                        applyExoOverlayState(
                                playbackState,
                                exoPlayer.getPlayWhenReady(),
                                thaumbnail_for_video,
                                imageViewExoVideoPlayVideo,
                                progress_bar_for_video,
                                resumeHalfPlayerView);
                        updateExoCenterControlIcons(resumeHalfPlayerView);
                        if (playbackState == Player.STATE_ENDED) {
                            notifyVideoEndedForAutoplay(position);
                        }
                    }
                });
            } else {
                updateExoCenterControlIcons(simpleExoPlayerView);
            }
            if (exoPlayer.getPlayWhenReady()) {
                forceSharedExoPlay();
            }
        } else if (isLiveExoOnView(simpleExoPlayerView, position)) {
            // Same video still rendering on this card — do not setPlayer(null) mid-play.
            keepLiveHalfSurface = true;
            hardenFeedPlayerSurface(simpleExoPlayerView);
            attachSharedExoToPlayerView(simpleExoPlayerView);
            if (simpleExoPlayerView.getVisibility() != View.VISIBLE) {
                simpleExoPlayerView.setVisibility(View.VISIBLE);
            }
            imageViewExoVideoPlayVideo.setVisibility(View.GONE);
            applyExoOverlayState(
                    exoPlayer.getPlaybackState(),
                    exoPlayer.getPlayWhenReady(),
                    thaumbnail_for_video,
                    imageViewExoVideoPlayVideo,
                    progress_bar_for_video,
                    simpleExoPlayerView);
            updateExoCenterControlIcons(simpleExoPlayerView);
        } else {
            // Detach only when leaving the card — never release the shared static player
            // from a recycled scrap view, and never clear a live picture.
            if (!isFeedExoSurfaceLive(simpleExoPlayerView)) {
                if (simpleExoPlayerView.getPlayer() != null
                        && simpleExoPlayerView.getPlayer() != exoPlayer) {
                    simpleExoPlayerView.setPlayer(null);
                } else if (simpleExoPlayerView.getPlayer() != null && !isSharedExoActiveFor(position)) {
                    simpleExoPlayerView.setPlayer(null);
                }
            }
            simpleExoPlayerView.setVisibility(View.GONE);
            if ("4".equalsIgnoreCase(list.get(position).getMediaType())) {
                // Youtube
            } else {
                // uploaded video — idle UX until user taps center play
                imageViewExoVideoPlayVideo.setVisibility(View.VISIBLE);
                setFeedExoPosterCover(thaumbnail_for_video, simpleExoPlayerView, true);
            }
        }

        //....................................................Upload video..............................
        // Do not resize the live picture on likes/progress rebind — that clears TextureView.
        if (!isFeedExoSurfaceLive(simpleExoPlayerView)) {
            layoutParentTitle.setVisibility(View.VISIBLE);
            layoutParentReportDetails.setVisibility(View.VISIBLE);
            layoutParentMiddle.setVisibility(View.VISIBLE);
            layoutParentTop.getLayoutParams().height = getVideoLayoutTopHeight();
        }
        if (!keepLiveHalfSurface && !isFeedExoSurfaceLive(simpleExoPlayerView)) {
            simpleExoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
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
        if (!keepLiveHalfSurface) {
            progress_bar_for_video.setVisibility(View.GONE);
        }
        imageViewExoVideoPlayVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fullscreen = false;
                textViewWebUrl.setVisibility(View.GONE);
                mute_button.setImageResource(ic__volume_up);
                String videoUrl = list.get(position).getVideoUrl();
                if (TextUtils.isEmpty(videoUrl)) {
                    Toast.makeText(context, "Video is not available", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (isLiveExoOnView(simpleExoPlayerView, position)
                        || isFeedExoSurfaceLive(simpleExoPlayerView)) {
                    // Same card already playing — do not release/recreate the surface.
                    forceSharedExoPlay();
                    imageViewExoVideoPlayVideo.setVisibility(View.GONE);
                    setFeedExoPosterCover(thaumbnail_for_video, simpleExoPlayerView, false);
                    hideExoSpinnerNow(progress_bar_for_video);
                    return;
                }
                imageViewExoVideoPlayVideo.setVisibility(View.GONE);
                thaumbnail_for_video.setVisibility(View.VISIBLE);
                progress_bar_for_video.setVisibility(View.VISIBLE);
                displayArticleImage(
                        thaumbnail_for_video,
                        list.get(position),
                        list.get(position).getThumbnail());
                hardenFeedPlayerSurface(simpleExoPlayerView);
                simpleExoPlayerView.setVisibility(View.VISIBLE);
                releaseSharedExoPlayerQuietly();
                exoPlayer = createFeedExoPlayer();
                attachSharedExoToPlayerView(simpleExoPlayerView);
                if (simpleExoPlayerView.getResizeMode() != AspectRatioFrameLayout.RESIZE_MODE_FIT) {
                    simpleExoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
                }
                showLog("Video URL:"+videoUrl);
                MediaItem mediaItem = MediaItem.fromUri(Uri.parse(videoUrl));
                // Start at 0. Pause/resume uses the live player and does not come through here.
                exoPlayer.setMediaItem(mediaItem);
                exoPlayer.seekTo(0);
                exoPlayer.prepare();
                // Explicit center-play tap must always start playback (not settings autoplay).
                forceSharedExoPlay();
                markSharedExoActive(position, videoUrl);
                // Media3 does not manage the legacy exo_play/exo_pause buttons in the custom
                // controller, so wire them here to make tapping toggle play/pause and to keep
                // exactly one center icon visible per player state.
                wireHalfVideoExoPauseControl(simpleExoPlayerView);
                attachExoCenterIconSync(simpleExoPlayerView);
                exoPlayer.addListener(new Player.Listener() {
                    @Override
                    public void onPlaybackStateChanged(int playbackState) {
                        if (exoPlayer == null) {
                            return;
                        }
                        boolean playWhenReady = exoPlayer.getPlayWhenReady();
                        showLog("onPlayerStateChanged half::" + playWhenReady + " state=" + playbackState);
                        applyExoOverlayState(
                                playbackState,
                                playWhenReady,
                                thaumbnail_for_video,
                                imageViewExoVideoPlayVideo,
                                progress_bar_for_video,
                                simpleExoPlayerView);
                        updateExoCenterControlIcons(simpleExoPlayerView);
                        if (playbackState == Player.STATE_ENDED) {
                            notifyVideoEndedForAutoplay(position);
                        }
                    }

                    @Override
                    public void onPlayerError(@NonNull PlaybackException error) {
                        showLog("half exo error::" + error.getErrorCodeName());
                        feedExoFrameRendered = false;
                        progress_bar_for_video.setVisibility(View.GONE);
                        setFeedExoPosterCover(thaumbnail_for_video, simpleExoPlayerView, true);
                        imageViewExoVideoPlayVideo.setVisibility(View.VISIBLE);
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


        bindDistrictOrRewardsClick(
                halfVideoExoPlayerHolder.ll_refer_and_earn_halfy, list.get(position));

        //
                        /*Picasso.with(context).load(list.get(position).getWriterProfile()).skipMemoryCache()
                                .placeholder(R.drawable.ic_user_profile).into(halfVideoExoPlayerHolder.writer_profile_pic);*/
        AvatarImageHelper.loadUil(imageLoader, halfVideoExoPlayerHolder.writer_profile_pic,
                list.get(position).getWriterProfile());
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
                showArticleMenu(position);
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
                TextView likeCount = finalConvertView3.findViewById(R.id.half_like_count);
                int newStatus = applyLikeChange(position, likeCount);
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
        if (imageViewYoutubePlayVideo != null) {
            imageViewYoutubePlayVideo.setImageResource(R.drawable.ic_way2_play_circle);
        }
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
        bindSponsoredChrome(convertView, position);


        half_youTubePlayerHolder.writer_follow = convertView.findViewById(R.id.writer_follow);
        half_youTubePlayerHolder.writer_follower = convertView.findViewById(R.id.writer_follower);
        final TextView test_folloew = convertView.findViewById(R.id.test_folloew);
        half_youTubePlayerHolder.ll_refer_and_earn_halfy = convertView.findViewById(R.id.ll_refer_and_earn_half);
        half_youTubePlayerHolder.iv_share_and_earn = convertView.findViewById(R.id.iv_share_and_earn);
        half_youTubePlayerHolder.my_feed = convertView.findViewById(R.id.my_feed);
        layoutParentReportDetails.setVisibility(View.VISIBLE);
        parent_layout.setOnClickListener(null);
        installNewsModeTap(
                parent_layout,
                convertView,
                layoutParentReportDetails,
                false,
                resolveHomePageHost(context));
        installOfflineChromeTaps(convertView);
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
        // In-feed URL chip: same share flow as half_image_share (not ACTION_VIEW / App Link).
        textViewWebUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showVideoShareChooser(position, VideoShareLayout.HALF_YOUTUBE, convertView);
            }
        });
        if (categery_id == 0) {
            bindLocationPill(
                    half_youTubePlayerHolder.my_feed,
                    half_youTubePlayerHolder.iv_share_and_earn,
                    half_youTubePlayerHolder.ll_refer_and_earn_halfy,
                list.get(position));

        } else {
            bindLocationPill(
                    half_youTubePlayerHolder.my_feed,
                    half_youTubePlayerHolder.iv_share_and_earn,
                    half_youTubePlayerHolder.ll_refer_and_earn_halfy,
                list.get(position));

        }
        textViewPostDate.setText(list.get(position).getPost_date_time());

        if (LanguageId == 4) {

            english_half_content_description.setVisibility(View.GONE);
            english_half_content_title.setVisibility(View.GONE);

            half_content_description.setVisibility(View.GONE);
            half_content_title.setVisibility(View.GONE);

            tamil_half_content_description.setVisibility(View.VISIBLE);
            tamil_half_content_title.setVisibility(View.VISIBLE);

            tamil_half_content_description.setText(displayCopy(list.get(position).getDescription()));
            tamil_half_content_title.setText(displayCopy(list.get(position).getTitle()));
            half_youTubePlayerHolder.strip_text_name.setText(displayCopy(list.get(position).getStrip_content()));
            applyStripFont(half_youTubePlayerHolder.strip_text_name);
            FontSettingsHelper.applyArticleTypography(context, 4, tamil_half_content_title, tamil_half_content_description, "half_video_tamil");

        } else if (LanguageId == 1) {

            tamil_half_content_description.setVisibility(View.GONE);
            half_content_title.setVisibility(View.GONE);
            half_content_description.setVisibility(View.GONE);
            half_content_title.setVisibility(View.GONE);

            english_half_content_description.setVisibility(View.VISIBLE);
            english_half_content_title.setVisibility(View.VISIBLE);

            english_half_content_description.setText(displayCopy(list.get(position).getDescription()));
            english_half_content_title.setText(displayCopy(list.get(position).getTitle()));
            half_youTubePlayerHolder.strip_text_name.setText(displayCopy(list.get(position).getStrip_content()));
            applyStripFont(half_youTubePlayerHolder.strip_text_name);
            FontSettingsHelper.applyArticleTypography(context, 1, english_half_content_title, english_half_content_description, "half_video_english");

        } else {
            english_half_content_description.setVisibility(View.GONE);
            english_half_content_title.setVisibility(View.GONE);
            tamil_half_content_description.setVisibility(View.GONE);
            half_content_title.setVisibility(View.GONE);
            half_content_description.setVisibility(View.VISIBLE);
            half_content_title.setVisibility(View.VISIBLE);
            half_content_description.setText(displayCopy(list.get(position).getDescription()));
            half_content_title.setText(displayCopy(list.get(position).getTitle()));
            half_youTubePlayerHolder.strip_text_name.setText(displayCopy(list.get(position).getStrip_content()));
            applyStripFont(half_youTubePlayerHolder.strip_text_name);
            FontSettingsHelper.applyArticleTypography(context, LanguageId, half_content_title, half_content_description, "half_video_default");
        }
        bindWriterStripIdentity(
                list.get(position),
                half_youTubePlayerHolder.writer_name,
                half_youTubePlayerHolder.writer_designation,
                half_youTubePlayerHolder.writer_time);
        WriterStripStyleHelper.apply(
                context,
                half_youTubePlayerHolder.writer_name,
                half_youTubePlayerHolder.writer_designation,
                half_youTubePlayerHolder.writer_time,
                half_youTubePlayerHolder.my_feed,
                half_youTubePlayerHolder.ll_refer_and_earn_halfy
        );
        bindLocationPill(
                half_youTubePlayerHolder.my_feed,
                half_youTubePlayerHolder.iv_share_and_earn,
                half_youTubePlayerHolder.ll_refer_and_earn_halfy,
                list.get(position));
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

        // Real YouTube thumb only — never CMS orange / splash / black plate.
        bindYoutubeFeedPoster(imageViewItems, list.get(position));

        isYoutubeVideoIsInitialized=false;
        imageViewYoutubePlayVideo.setVisibility(View.VISIBLE);
        if (imageViewItems != null) {
            imageViewItems.setVisibility(View.VISIBLE);
            imageViewItems.setClickable(true);
            imageViewItems.setFocusable(true);
        }
        try {
            // Half player host stays empty until tap — avoids black iframe plate.
            halfyoutubecontainer.setVisibility(View.GONE);
            halfyoutubecontainer.removeAllViews();
        } catch (Exception ignored) {
        }
        try {
            // Video area must not sit on a black plate under the YT thumb.
            layoutParentTop.setBackgroundColor(Color.TRANSPARENT);
        } catch (Exception ignored) {
        }

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
                            // Keep YT poster under the player until onReady — no black plate.
                            youtubeprogressbar.setVisibility(GONE);
                            textViewWebUrl.setVisibility(View.GONE);
                            imageViewYoutubePlayVideo.setVisibility(View.GONE);
                            imageViewItems.setVisibility(View.VISIBLE);
                            youTubePlayerView.setVisibility(View.VISIBLE);
                            View v = youTubePlayerView.inflateCustomPlayerUi(R.layout.youtube_custom_player_ui);
                            listener = new AbstractYouTubePlayerListener() {
                                @Override
                                public void onReady(@NonNull com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer youTubePlayer) {
                                    try {
                                        imageViewItems.setVisibility(View.GONE);
                                        imageViewYoutubePlayVideo.setVisibility(View.GONE);
                                    } catch (Exception ignored) {
                                    }
                                    layoutParentTitle.setVisibility(View.VISIBLE);
                                    layoutParentReportDetails.setVisibility(View.VISIBLE);
                                    layoutParentMiddle.setVisibility(View.VISIBLE);
                                    customPlayerUiController = new CustomPlayerUiController(context, v, youTubePlayer, youTubePlayerView, layoutHeight,true, new CustomPlayerUiController.ShowFullScreenVideo() {
                                        @Override
                                        public void showFullScreenVideo(boolean b) {
                                            // Immersive host owns the picture; only toggle in-card chrome.
                                            // Do not resize FlipView parents — that used to snap to page 0.
                                            showLog("youTubePlayerView FS chrome hide=" + b);
                                            if (b) {
                                                layoutParentTitle.setVisibility(GONE);
                                                layoutParentReportDetails.setVisibility(GONE);
                                                layoutParentMiddle.setVisibility(GONE);
                                            } else {
                                                layoutParentTitle.setVisibility(View.VISIBLE);
                                                layoutParentReportDetails.setVisibility(View.VISIBLE);
                                                layoutParentMiddle.setVisibility(View.VISIBLE);
                                            }
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
                                    customPlayerUiController.setUserPlaybackCallbacks(
                                            () -> markUserPausedVideoAutoplay(),
                                            () -> clearUserPausedVideoAutoplay());
                                    attachYoutubeEndedAutoplayListener(youTubePlayer, position);
                                    ModelClass.Datum halfItem = list.get(position);
                                    String halfYtId = YouTubeIdHelper.extractFromItem(
                                            halfItem.getVideoUrl(),
                                            halfItem.getThumbnail(),
                                            halfItem.getBackgroundImage(),
                                            halfItem.getVideos(),
                                            halfItem.getImages());
                                    if (TextUtils.isEmpty(halfYtId)) {
                                        halfYtId = YouTubeIdHelper.extract(halfItem.getVideoUrl());
                                    }
                                    if (!TextUtils.isEmpty(halfYtId)) {
                                        YouTubePlayerUtils.loadOrCueVideo(
                                                youTubePlayer, lifecycle,
                                                halfYtId, 0f
                                        );
                                    }
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
        // Poster tap = play YouTube immediately (same path as center play).
        if (imageViewItems != null) {
            imageViewItems.setOnClickListener(v -> {
                if (imageViewYoutubePlayVideo != null
                        && imageViewYoutubePlayVideo.getVisibility() == View.VISIBLE) {
                    imageViewYoutubePlayVideo.performClick();
                }
            });
        }
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
        bindDistrictOrRewardsClick(
                half_youTubePlayerHolder.ll_refer_and_earn_halfy, list.get(position));

        //
                        /*Picasso.with(context).load(list.get(position).getWriterProfile()).skipMemoryCache()
                                .placeholder(R.drawable.ic_user_profile).into(half_youTubePlayerHolder.writer_profile_pic);*/
        AvatarImageHelper.loadUil(imageLoader, half_youTubePlayerHolder.writer_profile_pic,
                list.get(position).getWriterProfile());


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
                showArticleMenu(position);
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
                TextView likeCount = finalConvertView3.findViewById(R.id.half_like_count);
                int newStatus = applyLikeChange(position, likeCount);
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
        if (writer_id == null || SelfFollowGuard.isSelf(getApplicationContext(), writer_id)) {
            return;
        }

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
                    try {
                        com.anyNews.anynews.rewards.RewardsEngagementBatcher.get()
                                .trackFollow(context);
                    } catch (Exception ignored) {
                    }
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

    /**
     * Article overflow menu (report-style bottom sheet): Bookmark + Report.
     * Sponsored posts open {@link #showAdCenter} instead (Way2 Ad Center).
     */
    private void showArticleMenu(final int position) {
        if (list == null || position < 0 || position >= list.size()) return;
        final ModelClass.Datum item = list.get(position);
        if (item == null || item.getPostId() == null) return;
        if (item.isSponsoredAd()) {
            showAdCenter(position);
            return;
        }

        try {
            final BottomSheetDialog sheet = new BottomSheetDialog(context);
            View dialogView =
                    LayoutInflater.from(context).inflate(R.layout.bottom_sheet_article_menu, null);
            TextView bookmarkLabel = dialogView.findViewById(R.id.option_bookmark_label);
            ImageView bookmarkIcon = dialogView.findViewById(R.id.option_bookmark_icon);

            boolean saved = BookmarkStore.isBookmarked(context, item.getPostId());
            if (saved || (item.getBookmarkStatus() != null && item.getBookmarkStatus() == 1)) {
                bookmarkLabel.setText(R.string.remove_bookmark);
                bookmarkIcon.setImageResource(R.drawable.ic_action_bookmark);
            } else {
                bookmarkLabel.setText(R.string.bookmark);
                bookmarkIcon.setImageResource(R.drawable.ic_action_bookmark_outline);
            }

            dialogView
                    .findViewById(R.id.option_bookmark)
                    .setOnClickListener(
                            v -> {
                                sheet.dismiss();
                                BookmarkStore.toggle(
                                        context,
                                        item,
                                        (bookmarked, toastMsg) -> {
                                            item.setBookmarkStatus(bookmarked ? 1 : 0);
                                            Toast.makeText(
                                                            context,
                                                            bookmarked
                                                                    ? context.getString(
                                                                            R.string
                                                                                    .bookmark_saved)
                                                                    : context.getString(
                                                                            R.string
                                                                                    .bookmark_removed),
                                                            Toast.LENGTH_SHORT)
                                                    .show();
                                        });
                            });

            dialogView
                    .findViewById(R.id.option_report)
                    .setOnClickListener(
                            v -> {
                                sheet.dismiss();
                                openReportIssueDialog(position);
                            });

            sheet.setContentView(dialogView);
            sheet.show();
        } catch (Exception e) {
            // Fallback: open report dialog if sheet fails
            openReportIssueDialog(position);
        }
    }

    /**
     * Way2News-style Ad Center for {@code is_sponsored} posts.
     * Advertise → Configuration {@code full_jatakam_whatsapp}.
     */
    private void showAdCenter(final int position) {
        if (list == null || position < 0 || position >= list.size()) return;
        final ModelClass.Datum item = list.get(position);
        if (item == null || item.getPostId() == null) return;

        try {
            final BottomSheetDialog sheet = new BottomSheetDialog(context);
            View dialogView =
                    LayoutInflater.from(context).inflate(R.layout.bottom_sheet_ad_center, null);

            TextView saveLabel = dialogView.findViewById(R.id.ad_center_save_label);
            ImageView saveIcon = dialogView.findViewById(R.id.ad_center_save_icon);
            boolean saved = BookmarkStore.isBookmarked(context, item.getPostId());
            if (saved || (item.getBookmarkStatus() != null && item.getBookmarkStatus() == 1)) {
                if (saveLabel != null) saveLabel.setText(R.string.ad_center_saved);
                if (saveIcon != null) {
                    saveIcon.setImageResource(R.drawable.ic_action_bookmark);
                }
            }

            View close = dialogView.findViewById(R.id.ad_center_close);
            if (close != null) close.setOnClickListener(v -> sheet.dismiss());

            dialogView
                    .findViewById(R.id.ad_center_share)
                    .setOnClickListener(
                            v -> {
                                sheet.dismiss();
                                shareSponsoredAd(position);
                            });

            dialogView
                    .findViewById(R.id.ad_center_report)
                    .setOnClickListener(
                            v -> {
                                sheet.dismiss();
                                openReportIssueDialog(position);
                            });

            dialogView
                    .findViewById(R.id.ad_center_save)
                    .setOnClickListener(
                            v -> {
                                sheet.dismiss();
                                BookmarkStore.toggle(
                                        context,
                                        item,
                                        (bookmarked, toastMsg) -> {
                                            item.setBookmarkStatus(bookmarked ? 1 : 0);
                                            Toast.makeText(
                                                            context,
                                                            bookmarked
                                                                    ? context.getString(
                                                                            R.string
                                                                                    .bookmark_saved)
                                                                    : context.getString(
                                                                            R.string
                                                                                    .bookmark_removed),
                                                            Toast.LENGTH_SHORT)
                                                    .show();
                                        });
                            });

            dialogView
                    .findViewById(R.id.ad_center_advertise)
                    .setOnClickListener(
                            v -> {
                                sheet.dismiss();
                                openAdvertiseOnAnyNewsWhatsApp();
                            });

            sheet.setContentView(dialogView);
            sheet.show();
        } catch (Exception e) {
            showLog("showAdCenter: " + e.getMessage());
            openReportIssueDialog(position);
        }
    }

    private void shareSponsoredAd(int position) {
        if (list == null || position < 0 || position >= list.size()) return;
        ModelClass.Datum item = list.get(position);
        if (item == null) return;
        String link = item.getShareLink();
        if (TextUtils.isEmpty(link)) {
            link = item.getTitle() != null ? item.getTitle() : "";
        }
        try {
            Intent send = new Intent(Intent.ACTION_SEND);
            send.setType("text/plain");
            String body = ShareOutboundHelper.formatCaption(item.getTitle(), link, item.getPostId());
            send.putExtra(Intent.EXTRA_TEXT, body);
            context.startActivity(Intent.createChooser(send, context.getString(R.string.ad_center_share)));
        } catch (Exception e) {
            showLog("shareSponsoredAd: " + e.getMessage());
        }
    }

    /** Opens WhatsApp using Root_Settings {@code full_jatakam_whatsapp} from splash prefs. */
    private void openAdvertiseOnAnyNewsWhatsApp() {
        String number = ProfileHubConfigHelper.getFullJatakamWhatsapp(context);
        if (TextUtils.isEmpty(number)) {
            number = SharedPrefsData.getString(
                    context, Constants.FULL_JATAKAM_WHATSAPP, Constants.PREF_NAME);
        }
        if (TextUtils.isEmpty(number)) {
            Toast.makeText(context, R.string.ad_center_whatsapp_missing, Toast.LENGTH_LONG).show();
            return;
        }
        String digits = number.replaceAll("[^0-9]", "");
        if (digits.length() < 10) {
            Toast.makeText(context, R.string.ad_center_whatsapp_missing, Toast.LENGTH_LONG).show();
            return;
        }
        String text = context.getString(R.string.ad_center_advertise_prefill);
        try {
            Uri uri = Uri.parse("https://wa.me/" + digits + "?text=" + Uri.encode(text));
            Intent i = new Intent(Intent.ACTION_VIEW, uri);
            context.startActivity(i);
        } catch (Exception e) {
            Toast.makeText(context, R.string.ad_center_whatsapp_missing, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Sponsored chrome:
     * - Full ads: teal "AD" badge top-right → Advertise on AnyNews WhatsApp;
     *   side engagement chrome hidden so the creative reads full-bleed.
     * - Half ads: "SPONSORED AD" label + info icon → Ad Center; body URLs blue/tappable.
     */
    private void bindSponsoredChrome(View convertView, int position) {
        if (convertView == null || list == null || position < 0 || position >= list.size()) {
            return;
        }
        ModelClass.Datum item = list.get(position);
        boolean sponsored = item != null && item.isSponsoredAd();
        boolean fullCreative =
                sponsored
                        && item.getPostType() != null
                        && item.getPostType().toLowerCase().contains("full");

        // Full-bleed AD badge (top-right)
        TextView adBadge = convertView.findViewById(R.id.sponsored_ad_badge);
        ViewGroup root =
                convertView instanceof ViewGroup
                        ? (ViewGroup) convertView
                        : null;
        if (adBadge == null && sponsored && fullCreative && root != null) {
            adBadge = new TextView(context);
            adBadge.setId(R.id.sponsored_ad_badge);
            adBadge.setText(R.string.sponsored_ad_badge);
            adBadge.setTextColor(Color.WHITE);
            adBadge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
            adBadge.setTypeface(adBadge.getTypeface(), Typeface.BOLD);
            adBadge.setAllCaps(true);
            adBadge.setBackgroundResource(R.drawable.bg_sponsored_ad_badge);
            float density = context.getResources().getDisplayMetrics().density;
            int padH = (int) (8 * density);
            int padV = (int) (3 * density);
            adBadge.setPadding(padH, padV, padH, padV);
            if (root instanceof androidx.constraintlayout.widget.ConstraintLayout) {
                androidx.constraintlayout.widget.ConstraintLayout.LayoutParams lp =
                        new androidx.constraintlayout.widget.ConstraintLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.topToTop = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
                lp.endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
                lp.setMargins(0, (int) (12 * density), (int) (12 * density), 0);
                root.addView(adBadge, lp);
            } else if (root instanceof FrameLayout) {
                FrameLayout.LayoutParams lp =
                        new FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.gravity = Gravity.TOP | Gravity.END;
                lp.setMargins(0, (int) (12 * density), (int) (12 * density), 0);
                root.addView(adBadge, lp);
            } else {
                ViewGroup.LayoutParams rlp =
                        new ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT);
                root.addView(adBadge, rlp);
            }
        }
        if (adBadge != null) {
            adBadge.setVisibility(sponsored && fullCreative ? VISIBLE : GONE);
            if (sponsored && fullCreative) {
                adBadge.setOnClickListener(v -> openAdvertiseOnAnyNewsWhatsApp());
                adBadge.bringToFront();
            } else {
                adBadge.setOnClickListener(null);
            }
        }

        // Full-image layout only: hide engagement chrome for sponsored full creatives;
        // restore on recycle when binding a normal full post.
        View sideMenu = convertView.findViewById(R.id.side_menu);
        if (sideMenu != null && convertView.findViewById(R.id.full_image_view_details) != null) {
            sideMenu.setVisibility(sponsored && fullCreative ? GONE : VISIBLE);
        }

        TextView label = convertView.findViewById(R.id.sponsored_ad_label);
        if (label == null && sponsored && !fullCreative) {
            ViewGroup row = convertView.findViewById(R.id.with_out_strips_layout);
            if (row instanceof LinearLayout) {
                LinearLayout ll = (LinearLayout) row;
                label = new TextView(context);
                label.setId(R.id.sponsored_ad_label);
                label.setText(R.string.sponsored_ad_label);
                label.setTextColor(Color.parseColor("#5B9BD5"));
                label.setTextSize(11f);
                label.setTypeface(label.getTypeface(), Typeface.BOLD);
                label.setAllCaps(true);
                LinearLayout.LayoutParams lp =
                        new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.gravity = android.view.Gravity.CENTER_VERTICAL;
                float density = context.getResources().getDisplayMetrics().density;
                lp.setMarginStart((int) (12 * density));
                lp.setMarginEnd((int) (8 * density));
                int insertAt = 0;
                for (int i = 0; i < ll.getChildCount(); i++) {
                    View child = ll.getChildAt(i);
                    ViewGroup.LayoutParams clp = child.getLayoutParams();
                    if (clp instanceof LinearLayout.LayoutParams
                            && ((LinearLayout.LayoutParams) clp).weight > 0f) {
                        insertAt = i;
                        break;
                    }
                }
                ll.addView(label, insertAt, lp);
            }
        }
        if (label != null) {
            label.setVisibility(sponsored && !fullCreative ? VISIBLE : GONE);
            if (sponsored && !fullCreative) {
                label.setText(R.string.sponsored_ad_label);
            }
        }

        ImageView reportIcon = convertView.findViewById(R.id.report_icon);
        if (reportIcon == null) {
            reportIcon = convertView.findViewById(R.id.video_report);
        }
        if (reportIcon != null && !(sponsored && fullCreative)) {
            reportIcon.setImageResource(
                    sponsored ? R.drawable.ic_action_info : R.drawable.ic_action_more_vert);
        }

        if (sponsored && !fullCreative) {
            applySponsoredHalfBodyLinks(convertView, item);
        }
    }

    /**
     * Half sponsored body: auto-link http(s) URLs in blue and make them tappable.
     * Also linkifies optional source_link when present as plain text.
     */
    private void applySponsoredHalfBodyLinks(View convertView, ModelClass.Datum item) {
        if (convertView == null || item == null) return;
        String raw = item.getDescription() != null ? item.getDescription() : "";
        String source = item.getSource_link() != null ? item.getSource_link() : "";
        if (!TextUtils.isEmpty(source)
                && !raw.contains(source)
                && (source.startsWith("http://") || source.startsWith("https://"))) {
            raw = TextUtils.isEmpty(raw) ? source : (raw + "\n" + source);
        }
        for (int id : HALF_BODY_TEXT_IDS) {
            TextView tv = convertView.findViewById(id);
            if (tv == null || tv.getVisibility() != VISIBLE) continue;
            SpannableString spannable = new SpannableString(raw);
            Linkify.addLinks(spannable, Linkify.WEB_URLS);
            URLSpan[] urls = spannable.getSpans(0, spannable.length(), URLSpan.class);
            if (urls != null) {
                for (URLSpan span : urls) {
                    int start = spannable.getSpanStart(span);
                    int end = spannable.getSpanEnd(span);
                    final String url = span.getURL();
                    spannable.removeSpan(span);
                    spannable.setSpan(
                            new ClickableSpan() {
                                @Override
                                public void onClick(View widget) {
                                    try {
                                        if (context != null
                                                && context
                                                        .getPackageName()
                                                        .equals(BuildConfig.APPLICATION_ID)) {
                                            // Owned AnyNews https → in-app only (no Chrome).
                                            OwnedHttpsOpener.open(context, url);
                                        }
                                    } catch (Exception e) {
                                        showLog("sponsored link: " + e.getMessage());
                                    }
                                }

                                @Override
                                public void updateDrawState(TextPaint ds) {
                                    super.updateDrawState(ds);
                                    ds.setUnderlineText(true);
                                    ds.setColor(Color.parseColor("#1565C0"));
                                }
                            },
                            start,
                            end,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
            tv.setText(spannable);
            tv.setMovementMethod(LinkMovementMethod.getInstance());
            tv.setHighlightColor(Color.TRANSPARENT);
        }
    }

    /** Existing report-content flow (options API + submit). */
    private void openReportIssueDialog(final int position) {
        if (list == null || position < 0 || position >= list.size()) return;
        try {
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
            EditText full_image_edt_news_headlines =
                    dialogView.findViewById(R.id.full_image_edt_news_headlines);
            Button yes = dialogView.findViewById(R.id.yes);
            ImageView close_dailog = dialogView.findViewById(R.id.close_dailog);

            final AlertDialog dialog = builder.create();
            JsonObject updateparams = ReportParams();

            close_dailog.setOnClickListener(view -> dialog.dismiss());

            APIService apiService = RestApiClient.getClient().create(APIService.class);
            Call<GetReportIssueResponce> update = apiService.reportissue(updateparams);
            update.enqueue(
                    new Callback<GetReportIssueResponce>() {
                        @Override
                        public void onResponse(
                                Call<GetReportIssueResponce> call,
                                retrofit2.Response<GetReportIssueResponce> response) {
                            GetReportIssueResponce updateResponce = response.body();
                            if (updateResponce == null
                                    || updateResponce.getData() == null
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
                            full_image_edt_news_headlines.setHint(
                                    "" + updateResponce.getData().getPlaceholder());

                            yes.setOnClickListener(
                                    view -> {
                                        String resoin;
                                        if (one.isChecked()) {
                                            resoin = one.getText().toString();
                                        } else if (two.isChecked()) {
                                            resoin = two.getText().toString();
                                        } else if (three.isChecked()) {
                                            resoin = three.getText().toString();
                                        } else {
                                            resoin = four.getText().toString();
                                        }

                                        JsonObject params =
                                                DailogRequest(
                                                        resoin,
                                                        list.get(position).getPostId(),
                                                        full_image_edt_news_headlines
                                                                .getText()
                                                                .toString());

                                        APIService api =
                                                RestApiClient.getClient().create(APIService.class);
                                        api.dailogSend(params)
                                                .enqueue(
                                                        new Callback<DailogSendRespouse>() {
                                                            @Override
                                                            public void onResponse(
                                                                    Call<DailogSendRespouse> call,
                                                                    retrofit2.Response<
                                                                                    DailogSendRespouse>
                                                                            response) {
                                                                DailogSendRespouse body =
                                                                        response.body();
                                                                if (body != null) {
                                                                    Toast.makeText(
                                                                                    context,
                                                                                    ""
                                                                                            + body
                                                                                                    .getMsg(),
                                                                                    Toast
                                                                                            .LENGTH_SHORT)
                                                                            .show();
                                                                }
                                                                dialog.dismiss();
                                                            }

                                                            @Override
                                                            public void onFailure(
                                                                    Call<DailogSendRespouse> call,
                                                                    Throwable t) {
                                                                Toast.makeText(
                                                                                context,
                                                                                "" + call,
                                                                                Toast
                                                                                        .LENGTH_SHORT)
                                                                        .show();
                                                            }
                                                        });
                                    });
                        }

                        @Override
                        public void onFailure(
                                Call<GetReportIssueResponce> call, Throwable t) {
                            Toast.makeText(context, "" + call, Toast.LENGTH_SHORT).show();
                        }
                    });

            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
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


                // Prefer authoritative Mongo total from Update_Like — overwrites any
                // optimistic +/- 1 so the side menu never stays on a stale 0/1.
                if (likeDownloadRespouse != null
                        && Boolean.TRUE.equals(likeDownloadRespouse.getSuccess())
                        && likeDownloadRespouse.getLikes() != null) {
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

        String property = share == null ? "share" : share.trim();
        boolean isWhatsapp =
                "whatsapp_share".equalsIgnoreCase(property)
                        || "whatsapp".equalsIgnoreCase(property);
        if (isWhatsapp) {
            property = "whatsapp_share";
        }

        int postIndex = findPostIndex(post_id);
        // Normal share bumps shares; WhatsApp button bumps whatsapp_shares only.
        // Defer FlipView rebind so the system share sheet can open on this frame first.
        if (postIndex >= 0) {
            ModelClass.Datum item = list.get(postIndex);
            boolean bumped = false;
            if ("share".equalsIgnoreCase(property)) {
                int shares = item.getShares() != null ? item.getShares() : 0;
                item.setShares(shares + 1);
                bumped = true;
            } else if (isWhatsapp) {
                int wa = item.getWhatsappShares() != null ? item.getWhatsappShares() : 0;
                item.setWhatsappShares(wa + 1);
                bumped = true;
            }
            if (bumped) {
                // In-place count paint only — never FlipView recycle (black/video flash).
                try {
                    if ("share".equalsIgnoreCase(property)) {
                        applyShareCountToRememberedView(post_id, item.getShares());
                    } else if (isWhatsapp) {
                        applyWhatsappShareCountToRememberedView(
                                post_id, item.getWhatsappShares());
                    }
                } catch (Exception ignored) {
                }
            }
        }

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("draft_id", SharedPrefsData.getInt(context, Constants.DraftID, Constants.PREF_NAME));
        jsonObject.addProperty("post_id", post_id);
        jsonObject.addProperty("property", property);


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
                        if (isWhatsapp) {
                            syncEngagementFromServer(post_id, null, null, null, null,
                                    likeDownloadRespouse.getWhatsappShares());
                        } else {
                            syncEngagementFromServer(post_id, null, null, null, likeDownloadRespouse.getShares());
                        }
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

    /** Property for Update_Post_Counts — WhatsApp button vs normal share sheet. */
    private String shareCountProperty(boolean whatsapp) {
        return whatsapp ? "whatsapp_share" : "share";
    }

    /**
     * Current nested gallery/magazine page (0-based) inside a post card root.
     * Prefers Special FlipView, then gallery ViewPager, then magazine ViewPager.
     */
    private int resolveNestedGalleryPage(@Nullable View root) {
        if (root == null) {
            return 0;
        }
        try {
            FlipView flip = SpecialGalleryFlipHelper.findNestedGalleryFlip(root);
            if (flip != null && flip.getVisibility() == VISIBLE) {
                return Math.max(0, flip.getCurrentPage());
            }
        } catch (Exception ignored) {
        }
        try {
            ViewPager galleryPager = root.findViewById(R.id.viewpager);
            if (galleryPager != null
                    && galleryPager.getVisibility() == VISIBLE
                    && galleryPager.getAdapter() != null) {
                return Math.max(0, galleryPager.getCurrentItem());
            }
        } catch (Exception ignored) {
        }
        try {
            ViewPager magPager = root.findViewById(R.id.magzine_viewPager_data);
            if (magPager != null && magPager.getAdapter() != null) {
                return Math.max(0, magPager.getCurrentItem());
            }
        } catch (Exception ignored) {
        }
        if (image_magazineHolder != null
                && image_magazineHolder.magzine_viewPager_data != null
                && image_magazineHolder.magzine_viewPager_data.getAdapter() != null) {
            try {
                return Math.max(0,
                        image_magazineHolder.magzine_viewPager_data.getCurrentItem());
            } catch (Exception ignored) {
            }
        }
        if (imageGallaryHolder != null && imageGallaryHolder.viewpager != null
                && imageGallaryHolder.viewpager.getAdapter() != null
                && imageGallaryHolder.viewpager.getVisibility() == VISIBLE) {
            try {
                return Math.max(0, imageGallaryHolder.viewpager.getCurrentItem());
            } catch (Exception ignored) {
            }
        }
        return 0;
    }

    /**
     * Post share_link with current nested page encoded as {@code ?page=N} (0-based).
     * Page 0 omits the query so legacy short URLs stay unchanged.
     */
    @NonNull
    private String shareLinkForGalleryPage(int position, @Nullable View root) {
        String base = "";
        try {
            if (list != null && position >= 0 && position < list.size()) {
                base = list.get(position).getShareLink();
            }
        } catch (Exception ignored) {
        }
        return ShareOutboundHelper.appendGalleryPage(base, resolveNestedGalleryPage(root));
    }

    /**
     * Apply share {@code ?page=} to a nested gallery FlipView / ViewPager.
     * Peeks only — the caller settles (and then consumes) after layout so a
     * later {@code setAdapter} cannot snap the slide back to 0.
     * Returns the page used ({@code >= 0}), or {@code -1} if none pending.
     */
    private int applyPendingShareGalleryPage(long postId, @Nullable View root,
                                             @Nullable ViewPager pager,
                                             @Nullable FlipView flip) {
        HomePage home = shareHome();
        int page = -1;
        if (home != null) {
            try {
                page = home.peekShareGalleryPage(postId);
            } catch (Exception ignored) {
            }
        }
        if (page < 0) {
            return -1;
        }
        final int land = page;
        if (flip != null) {
            try {
                int count = flip.getPageCount();
                if (count <= 0) {
                    return -1;
                }
                int safe = Math.max(0, Math.min(land, count - 1));
                flip.rebindSettledPage(safe);
                return flipIsOnSharePage(flip, safe) ? safe : -1;
            } catch (Exception ignored) {
            }
        }
        if (pager != null && pager.getAdapter() != null) {
            try {
                int count = pager.getAdapter().getCount();
                if (count <= 0) {
                    return -1;
                }
                int safe = Math.max(0, Math.min(land, count - 1));
                movePagerToSharePage(pager, safe, false);
                return safe;
            } catch (Exception ignored) {
            }
        }
        if (root != null) {
            FlipView nested = SpecialGalleryFlipHelper.findNestedGalleryFlip(root);
            if (nested != null) {
                try {
                    int count = nested.getPageCount();
                    if (count <= 0) {
                        return -1;
                    }
                    int safe = Math.max(0, Math.min(land, count - 1));
                    nested.rebindSettledPage(safe);
                    return flipIsOnSharePage(nested, safe) ? safe : -1;
                } catch (Exception ignored) {
                }
            }
        }
        return land;
    }

    @Nullable
    private HomePage shareHome() {
        if (context instanceof HomePage) {
            return (HomePage) context;
        }
        if (activity instanceof HomePage) {
            return (HomePage) activity;
        }
        return null;
    }

    /**
     * Re-apply {@code page} after layout / a second bind, then consume only if
     * no newer jump replaced this one.
     */
    private void scheduleShareGallerySettle(long postId, int page, @Nullable View anchor) {
        HomePage home = shareHome();
        if (home == null || page < 0) {
            return;
        }
        final int gen = home.bumpShareGalleryApplyGen();
        if (gen < 0) {
            return;
        }
        final int safe = page;
        Runnable confirm = new Runnable() {
            @Override
            public void run() {
                reapplyShareGalleryPage(anchor, safe);
                Runnable done = new Runnable() {
                    @Override
                    public void run() {
                        reapplyShareGalleryPage(anchor, safe);
                        // Leave the pending slide in place. Home consumes it only
                        // after the visible pager is still on that index, so the
                        // feed rebind that follows a share pin can apply it again.
                    }
                };
                if (anchor != null) {
                    anchor.post(done);
                } else {
                    done.run();
                }
            }
        };
        if (anchor != null) {
            anchor.post(confirm);
        } else {
            confirm.run();
        }
    }

    private void reapplyShareGalleryPage(@Nullable View anchor, int page) {
        if (page < 0) {
            return;
        }
        try {
            if (anchor instanceof ViewPager) {
                movePagerToSharePage((ViewPager) anchor, page, false);
                return;
            }
            if (anchor instanceof FlipView) {
                FlipView flip = (FlipView) anchor;
                int count = flip.getPageCount();
                if (count <= 0) {
                    return;
                }
                flip.rebindSettledPage(Math.max(0, Math.min(page, count - 1)));
                return;
            }
            moveNestedGalleryToPage(anchor, page);
        } catch (Exception ignored) {
        }
    }

    /**
     * Already-open magazine: the feed card is on screen, so getView will not
     * run again. Move the nested pager/flip to the pending share slide.
     *
     * @return true when a visible pager/flip was moved
     */
    public boolean landVisibleShareGalleryPage(long postId, @Nullable View pageRoot) {
        HomePage home = shareHome();
        if (home == null) {
            return false;
        }
        int page;
        try {
            page = home.peekShareGalleryPage(postId);
        } catch (Exception e) {
            return false;
        }
        if (page < 0) {
            return false;
        }
        boolean moved = moveNestedGalleryToPage(pageRoot, page);
        if (!moved) {
            moved = moveShownHolderToSharePage(page);
        }
        if (!moved || !visibleShareGalleryIsOnPage(postId, page, pageRoot)) {
            return false;
        }
        View anchor = pageRoot;
        if (anchor == null && image_magazineHolder != null) {
            anchor = image_magazineHolder.magzine_viewPager_data;
        }
        scheduleShareGallerySettle(postId, page, anchor);
        return true;
    }

    private boolean moveShownHolderToSharePage(int page) {
        boolean moved = false;
        try {
            if (image_magazineHolder != null
                    && image_magazineHolder.magzine_viewPager_data != null
                    && image_magazineHolder.magzine_viewPager_data.isShown()) {
                moved = movePagerToSharePage(
                        image_magazineHolder.magzine_viewPager_data, page, false) || moved;
            }
        } catch (Exception ignored) {
        }
        try {
            if (imageGallaryHolder != null
                    && imageGallaryHolder.viewpager != null
                    && imageGallaryHolder.viewpager.isShown()) {
                moved = movePagerToSharePage(imageGallaryHolder.viewpager, page, true) || moved;
            }
        } catch (Exception ignored) {
        }
        try {
            View root = null;
            if (imageGallaryHolder != null && imageGallaryHolder.galleryLayout != null) {
                root = imageGallaryHolder.galleryLayout;
            }
            FlipView flip = SpecialGalleryFlipHelper.findNestedGalleryFlip(root);
            if (flip != null && flip.isShown()) {
                int count = flip.getPageCount();
                if (count > 0) {
                    flip.rebindSettledPage(Math.max(0, Math.min(page, count - 1)));
                    moved = true;
                }
            }
        } catch (Exception ignored) {
        }
        return moved;
    }

    private boolean moveNestedGalleryToPage(@Nullable View root, int page) {
        if (root == null || page < 0) {
            return false;
        }
        boolean moved = false;
        try {
            ViewPager mag = root.findViewById(R.id.magzine_viewPager_data);
            if (movePagerToSharePage(mag, page, false)) {
                moved = true;
            }
        } catch (Exception ignored) {
        }
        try {
            ViewPager gallery = root.findViewById(R.id.viewpager);
            if (movePagerToSharePage(gallery, page, true)) {
                moved = true;
            }
        } catch (Exception ignored) {
        }
        try {
            FlipView flip = SpecialGalleryFlipHelper.findNestedGalleryFlip(root);
            if (flip != null) {
                int count = flip.getPageCount();
                if (count > 0) {
                    flip.rebindSettledPage(Math.max(0, Math.min(page, count - 1)));
                    moved = true;
                }
            }
        } catch (Exception ignored) {
        }
        return moved;
    }

    private boolean movePagerToSharePage(@Nullable ViewPager pager, int page, boolean requireVisible) {
        if (pager == null || pager.getAdapter() == null) {
            return false;
        }
        if (requireVisible && pager.getVisibility() != VISIBLE) {
            return false;
        }
        int count = pager.getAdapter().getCount();
        if (count <= 0) {
            return false;
        }
        int safe = Math.max(0, Math.min(page, count - 1));
        pager.setCurrentItem(safe, false);
        if (pager.getCurrentItem() != safe) {
            final int landSafe = safe;
            pager.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (pager.getAdapter() != null && pager.getCurrentItem() != landSafe) {
                            pager.setCurrentItem(landSafe, false);
                        }
                    } catch (Exception ignored) {
                    }
                }
            });
        }
        return pager.getCurrentItem() == safe;
    }

    /**
     * True when the on-screen magazine ViewPager or gallery FlipView is already
     * on the share slide. Used to decide when {@code ?page=} can be consumed.
     */
    public boolean visibleShareGalleryIsOnPage(long postId, int page, @Nullable View pageRoot) {
        if (page < 0) {
            return false;
        }
        if (nestedControlIsOnSharePage(pageRoot, page)) {
            return true;
        }
        try {
            if (image_magazineHolder != null
                    && image_magazineHolder.magzine_viewPager_data != null
                    && image_magazineHolder.magzine_viewPager_data.isShown()
                    && pagerIsOnSharePage(image_magazineHolder.magzine_viewPager_data, page)) {
                return true;
            }
        } catch (Exception ignored) {
        }
        try {
            if (imageGallaryHolder != null
                    && imageGallaryHolder.viewpager != null
                    && imageGallaryHolder.viewpager.isShown()
                    && pagerIsOnSharePage(imageGallaryHolder.viewpager, page)) {
                return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    private boolean nestedControlIsOnSharePage(@Nullable View anchor, int page) {
        if (anchor == null || page < 0) {
            return false;
        }
        try {
            if (anchor instanceof ViewPager) {
                return pagerIsOnSharePage((ViewPager) anchor, page);
            }
            if (anchor instanceof FlipView) {
                return flipIsOnSharePage((FlipView) anchor, page);
            }
            ViewPager mag = anchor.findViewById(R.id.magzine_viewPager_data);
            if (pagerIsOnSharePage(mag, page)) {
                return true;
            }
            ViewPager gallery = anchor.findViewById(R.id.viewpager);
            if (gallery != null && gallery.getVisibility() == VISIBLE
                    && pagerIsOnSharePage(gallery, page)) {
                return true;
            }
            FlipView flip = SpecialGalleryFlipHelper.findNestedGalleryFlip(anchor);
            if (flipIsOnSharePage(flip, page)) {
                return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    private static boolean pagerIsOnSharePage(@Nullable ViewPager pager, int page) {
        if (pager == null || pager.getAdapter() == null) {
            return false;
        }
        int count = pager.getAdapter().getCount();
        if (count <= 0) {
            return false;
        }
        int want = Math.max(0, Math.min(page, count - 1));
        return pager.getCurrentItem() == want;
    }

    private static boolean flipIsOnSharePage(@Nullable FlipView flip, int page) {
        if (flip == null) {
            return false;
        }
        int count;
        try {
            count = flip.getPageCount();
        } catch (Exception e) {
            return false;
        }
        if (count <= 0) {
            return false;
        }
        int want = Math.max(0, Math.min(page, count - 1));
        try {
            return flip.getCurrentPage() == want;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Single-launch guard for all share entry points. Returns false if a share is
     * already in flight or was accepted within {@link #SHARE_LAUNCH_GUARD_MS}.
     */
    private boolean tryAcquireShareLaunch() {
        long now = SystemClock.elapsedRealtime();
        if (shareLaunchInFlight || (now - lastShareLaunchAtMs) < SHARE_LAUNCH_GUARD_MS) {
            showLog("share launch ignored (guard)");
            return false;
        }
        shareLaunchInFlight = true;
        lastShareLaunchAtMs = now;
        return true;
    }

    private void releaseShareLaunch() {
        shareLaunchInFlight = false;
    }

    /**
     * Attach a FileProvider image URI for sharing. ClipData + FLAG_GRANT_READ_URI_PERMISSION
     * is enough for the system chooser — avoid querying every ResolveInfo (that blocked the UI).
     */
    private void putShareImageStream(Intent intent, Uri photoURI) {
        intent.putExtra(Intent.EXTRA_STREAM, photoURI);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            intent.setClipData(ClipData.newUri(context.getContentResolver(), "share", photoURI));
        } catch (Exception ignored) {
        }
    }

    /**
     * Fast share attach: JPEG (not PNG@100) written off the critical path when possible.
     * Kept name for call sites; format is JPEG for speed.
     */
    private void writeScreenshotPngAndAttach(Intent intent, Bitmap bitmap) throws Exception {
        if (bitmap == null || bitmap.isRecycled()) {
            throw new IllegalArgumentException("empty screenshot");
        }
        File cacheDir = context.getExternalCacheDir();
        if (cacheDir == null) {
            cacheDir = context.getCacheDir();
        }
        File file = new File(cacheDir, "share_" + System.currentTimeMillis() + ".jpg");
        FileOutputStream fOut = new FileOutputStream(file);
        try {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
            fOut.flush();
        } finally {
            fOut.close();
        }
        file.setReadable(true, false);
        Uri photoURI = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
        intent.setType("image/jpeg");
        putShareImageStream(intent, photoURI);
    }

    /**
     * Capture on UI, compress/attach on a worker, then open the system share sheet.
     * Keeps Single Page / screenshot shares feeling instant (no PNG@100 on the main thread).
     * {@code onShareConfirmed} runs only when the user picks a chooser target
     * — including a WhatsApp-targeted intent. Never merely because the sheet opened.
     */
    private void prepareAndLaunchScreenshotShare(
            @Nullable View captureView,
            @NonNull Intent baseIntent,
            boolean whatsappDirect,
            @Nullable Runnable restoreUi,
            @Nullable Runnable onShareConfirmed) {
        final Handler main = new Handler(Looper.getMainLooper());
        final Runnable finishUi = () -> {
            if (restoreUi != null) {
                try {
                    restoreUi.run();
                } catch (Exception ignored) {
                }
            }
            releaseShareLaunch();
        };
        if (captureView == null) {
            finishUi.run();
            return;
        }
        // Yield so the in-app share sheet / chrome hide paints before capture.
        captureView.post(() -> {
            Bitmap bitmap = null;
            try {
                bitmap = ScreenShott.getInstance().takeScreenShotOfView(captureView);
            } catch (Exception e) {
                e.printStackTrace();
            }
            final Bitmap shot = bitmap;
            new Thread(() -> {
                try {
                    if (shot == null || shot.isRecycled()) {
                        main.post(finishUi);
                        return;
                    }
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("image/jpeg");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    if (baseIntent.getStringExtra(Intent.EXTRA_SUBJECT) != null) {
                        intent.putExtra(Intent.EXTRA_SUBJECT,
                                baseIntent.getStringExtra(Intent.EXTRA_SUBJECT));
                    }
                    if (baseIntent.getStringExtra(Intent.EXTRA_TEXT) != null) {
                        intent.putExtra(Intent.EXTRA_TEXT,
                                baseIntent.getStringExtra(Intent.EXTRA_TEXT));
                    }
                    if (whatsappDirect) {
                        intent.setPackage("com.whatsapp");
                    }
                    writeScreenshotPngAndAttach(intent, shot);
                    try {
                        if (!shot.isRecycled()) {
                            shot.recycle();
                        }
                    } catch (Exception ignored) {
                    }
                    final Intent launchIntent = intent;
                    main.post(() -> {
                        try {
                            if (context != null
                                    && context.getPackageName().equals(BuildConfig.APPLICATION_ID)) {
                                // WhatsApp (package set) and normal Share both wait for a target pick.
                                ShareOutboundHelper.launchChooserOnTargetChosen(
                                        context,
                                        launchIntent,
                                        "Share Link",
                                        onShareConfirmed);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            finishUi.run();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    main.post(finishUi);
                }
            }, "share-screenshot").start();
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
        WormDotsIndicator magzine_indicator;
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
                loadArticleImageGlide(
                        getApplicationContext(),
                        holder.survey_half_image,
                        datum,
                        imageBackgroundURL);
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
                displayArticleImage(holder.polling_half_image, datum, bg);
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
        ensureHalfWhatsappAction(convertView, position);
        TextView likeCountTv = convertView.findViewById(R.id.half_like_count);
        TextView commentCountTv = convertView.findViewById(R.id.half_comment_count);
        TextView shareCountTv = convertView.findViewById(R.id.half_share_count);
        bindHalfActionCount(likeCountTv, list.get(position).getLikes());
        bindHalfActionCount(commentCountTv, list.get(position).getComments());
        bindHalfActionCount(shareCountTv, list.get(position).getShares());
        rememberLikeCountView(list.get(position).getPostId(), likeCountTv);
        rememberCommentCountView(list.get(position).getPostId(), commentCountTv);
        rememberShareCountView(list.get(position).getPostId(), shareCountTv);
        tintHalfActionIcons(convertView);
        applyHalfActionIconSizes(context, convertView);
        bindHalfSearchIcon(convertView);
        bindHalfTtsSpeaker(convertView, position);

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
        // Search slot is admin-gated via bindHalfSearchIcon (do not force VISIBLE here).
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
            reportIcon.setImageResource(R.drawable.ic_action_more_vert);
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

        View.OnClickListener reportClick = v -> showArticleMenu(position);
        if (reportSlot != null) {
            reportSlot.setOnClickListener(reportClick);
        } else if (reportIcon != null) {
            reportIcon.setOnClickListener(reportClick);
        }
        bindSponsoredChrome(convertView, position);
    }

    /** Flag/report dialog used by survey/poll engagement row (same flow as half posts). */
    private void showHalfReportDialog(final int position) {
        openReportIssueDialog(position);
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
            datum.setWhatsappShares(list.get(positionx).getWhatsappShares());
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
                // Owned share/API hosts stay in-app; other ad clicks still use external VIEW.
                OwnedHttpsOpener.open(context, request.getUrl());
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

    /**
     * Upload Original → FIT_CENTER (full frame, letterbox OK).
     * Upload Cropped / default → FIT_XY (fill Normal Post slot).
     */
    private void applyHalfFeedImageScale(
            @Nullable ImageView target, @Nullable ModelClass.Datum item) {
        if (target == null) return;
        boolean contain = item != null && item.usesContainFeedImage();
        ImageView.ScaleType next =
                contain ? ImageView.ScaleType.FIT_CENTER : ImageView.ScaleType.FIT_XY;
        // Clear UIL tag so a recycled view with the same URL rebinds after scale change.
        if (target.getScaleType() != next) {
            target.setTag(null);
        }
        target.setScaleType(next);
        if (contain) {
            target.setBackgroundColor(Color.BLACK);
        } else {
            target.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    /**
     * Bind article hero/thumbnail via UIL. Prefers explicit offline file keyed by post_id
     * + remote URL; on network fail retries that file. Online pack capture also queues a
     * thrifty download.
     *
     * <p>Content identity includes the remote URL so a URL change (or scrap FlipView reuse)
     * cannot keep a prior birthday poster on an unrelated article. {@code loadUri} may flip
     * between http and file:// after offline download — that alone must not flash-reload.
     */
    private void displayArticleImage(
            @Nullable ImageView target,
            @Nullable ModelClass.Datum item,
            @Nullable String remoteUrl) {
        displayArticleImage(target, item, 0, remoteUrl);
    }

    private void displayArticleImage(
            @Nullable final ImageView target,
            @Nullable final ModelClass.Datum item,
            final int imageIndex,
            @Nullable String remoteUrl) {
        if (target == null || refusePosterOverLiveVideo(target)) return;
        String remote = remoteUrl;
        if (TextUtils.isEmpty(remote) && item != null) {
            remote = OfflineImageStore.primaryImageUrl(item);
        }
        final Long postId = item != null ? item.getPostId() : null;
        if (item != null) {
            try {
                OfflineImageStore.enqueueArticle(context, item);
            } catch (Exception ignored) {
            }
        }
        final String loadUri =
                OfflineImageStore.resolveDisplayUri(context, postId, imageIndex, remote);
        if (TextUtils.isEmpty(loadUri)) {
            clearArticleHeroImage(target);
            return;
        }
        // Identity = post + slot + remote URL (not file:// loadUri). Same article with the
        // same remote may rebind http→file after offline save without flashing.
        // Use "id:" / "i:" prefixes so post 10 cannot prefix-match post 100's tag.
        final String remoteKey = TextUtils.isEmpty(remote) ? "" : remote.trim();
        final String contentKey;
        if (postId != null && postId > 0) {
            contentKey = "id:" + postId + "|i:" + imageIndex + "|u:" + remoteKey;
        } else if (!TextUtils.isEmpty(remoteKey)) {
            contentKey = "u:" + remoteKey + "|i:" + imageIndex;
        } else {
            contentKey = "uri:" + loadUri;
        }
        final String tagKey = contentKey + "|" + loadUri;
        Object prev = target.getTag();
        if (prev != null) {
            String prevTag = String.valueOf(prev);
            if (prevTag.equals(tagKey) || prevTag.equals(contentKey)) {
                return;
            }
            // Same article + same remote already showing — skip offline file rebind flash.
            // Prefix must include the remote URL so postId|index alone cannot match a
            // different URL (birthday vs rain) on a recycled ImageView.
            if (prevTag.startsWith(contentKey + "|") && target.getDrawable() != null) {
                target.setTag(contentKey);
                return;
            }
            // Different post or URL: drop the scrap bitmap immediately before async load.
            if (!prevTag.equals(contentKey) && !prevTag.startsWith(contentKey + "|")) {
                try {
                    if (imageLoader != null) {
                        imageLoader.cancelDisplayTask(target);
                    }
                } catch (Exception ignored) {
                }
                target.setImageDrawable(null);
                target.setTag(null);
            }
        }
        target.setTag(tagKey);
        if (imageLoader == null) {
            ImageLoaderInit.ensureInitialized(context);
            imageLoader = ImageLoader.getInstance();
        }
        final int idx = imageIndex;
        final String expectedContentKey = contentKey;
        imageLoader.displayImage(
                loadUri,
                target,
                options,
                new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingFailed(
                            String imageUri, View view, FailReason failReason) {
                        if (!(view instanceof ImageView)) return;
                        Object t = view.getTag();
                        if (t != null) {
                            String ts = String.valueOf(t);
                            if (!ts.equals(expectedContentKey)
                                    && !ts.equals(tagKey)
                                    && !ts.startsWith(expectedContentKey + "|")) {
                                return; // view rebound to another article
                            }
                        }
                        File local =
                                OfflineImageStore.fileIfReady(context, postId, idx, remoteKey);
                        if (local == null && idx > 0) {
                            local = OfflineImageStore.fileIfReady(context, postId, 0, remoteKey);
                        }
                        if (local != null) {
                            String fileUri = "file://" + local.getAbsolutePath();
                            if (!fileUri.equals(imageUri)) {
                                imageLoader.displayImage(
                                        fileUri,
                                        (ImageView) view,
                                        options,
                                        animationListener);
                                return;
                            }
                        }
                        ((ImageView) view).setImageResource(IMAGE_FAIL_PLACEHOLDER);
                    }

                    @Override
                    public void onLoadingComplete(
                            String imageUri, View view, Bitmap loadedImage) {
                        if (!(view instanceof ImageView)) return;
                        Object t = view.getTag();
                        if (t != null) {
                            String ts = String.valueOf(t);
                            // Reject late UIL callbacks after the ImageView was rebound.
                            if (!ts.equals(expectedContentKey)
                                    && !ts.equals(tagKey)
                                    && !ts.startsWith(expectedContentKey + "|")) {
                                return;
                            }
                        }
                        view.setTag(expectedContentKey);
                        if (refusePosterOverLiveVideo((ImageView) view)) {
                            return;
                        }
                        if (animationListener != null) {
                            animationListener.onLoadingComplete(imageUri, view, loadedImage);
                        }
                    }
                });
    }

    /** Cancel in-flight UIL work and drop any scrap hero bitmap. */
    private void clearArticleHeroImage(@Nullable ImageView target) {
        if (target == null) return;
        try {
            if (imageLoader == null) {
                ImageLoaderInit.ensureInitialized(context);
                imageLoader = ImageLoader.getInstance();
            }
            if (imageLoader != null) {
                imageLoader.cancelDisplayTask(target);
            }
        } catch (Exception ignored) {
        }
        try {
            target.setImageDrawable(null);
            target.setImageResource(IMAGE_FAIL_PLACEHOLDER);
            target.setTag(null);
        } catch (Exception ignored) {
        }
    }

    /** True when the ImageView already holds a decoded article bitmap (not color placeholder). */
    private static boolean hasSettledArticleBitmap(@Nullable ImageView target) {
        if (target == null) {
            return false;
        }
        try {
            android.graphics.drawable.Drawable d = target.getDrawable();
            if (!(d instanceof android.graphics.drawable.BitmapDrawable)) {
                return false;
            }
            Bitmap b = ((android.graphics.drawable.BitmapDrawable) d).getBitmap();
            return b != null && !b.isRecycled() && b.getWidth() > 2 && b.getHeight() > 2;
        } catch (Exception ignored) {
            return false;
        }
    }

    /** Glide bind for video thumbs / survey BG with offline file resolve. */
    private void loadArticleImageGlide(
            @Nullable Context glideContext,
            @Nullable ImageView target,
            @Nullable ModelClass.Datum item,
            @Nullable String remoteUrl) {
        if (target == null || refusePosterOverLiveVideo(target)) return;
        Context gc = glideContext != null ? glideContext : context;
        if (!isValidContextForGlide(gc)) return;
        if (item != null) {
            try {
                OfflineImageStore.enqueueArticle(context, item);
            } catch (Exception ignored) {
            }
        }
        String remote = remoteUrl;
        if (TextUtils.isEmpty(remote) && item != null) {
            remote = OfflineImageStore.primaryImageUrl(item);
        }
        String loadUri =
                OfflineImageStore.resolveDisplayUri(
                        context, item != null ? item.getPostId() : null, remote);
        if (TextUtils.isEmpty(loadUri)) {
            clearArticleHeroImage(target);
            return;
        }
        final Long postId = item != null ? item.getPostId() : null;
        final String remoteKey = TextUtils.isEmpty(remote) ? "" : remote.trim();
        final String contentKey;
        if (postId != null && postId > 0) {
            contentKey = "id:" + postId + "|i:0|u:" + remoteKey;
        } else if (!TextUtils.isEmpty(remoteKey)) {
            contentKey = "u:" + remoteKey;
        } else {
            contentKey = "uri:" + loadUri;
        }
        Object prevTag = target.getTag();
        if (prevTag != null && target.getDrawable() != null) {
            String pStr = String.valueOf(prevTag);
            if (pStr.equals(contentKey)
                    || pStr.equals(loadUri)
                    || pStr.startsWith(contentKey + "|")) {
                target.setTag(contentKey);
                return;
            }
        }
        Object model = offlineAwareGlideModel(item, remote);
        if (model == null) {
            clearArticleHeroImage(target);
            return;
        }
        // Different post/URL: clear scrap bitmap before Glide paints.
        if (prevTag != null) {
            String pStr = String.valueOf(prevTag);
            if (!pStr.equals(contentKey) && !pStr.startsWith(contentKey + "|")) {
                try {
                    target.setImageDrawable(null);
                } catch (Exception ignored) {
                }
            }
        }
        target.setTag(contentKey);
        Glide.with(gc).load(model).apply(flipGlideDecodeOptions()).into(target);
    }

    @Nullable
    private Object offlineAwareGlideModel(
            @Nullable ModelClass.Datum item, @Nullable String remoteUrl) {
        if (item != null) {
            try {
                OfflineImageStore.enqueueArticle(context, item);
            } catch (Exception ignored) {
            }
        }
        String remote = remoteUrl;
        if (TextUtils.isEmpty(remote) && item != null) {
            remote = OfflineImageStore.primaryImageUrl(item);
        }
        String loadUri =
                OfflineImageStore.resolveDisplayUri(
                        context, item != null ? item.getPostId() : null, remote);
        if (TextUtils.isEmpty(loadUri)) return null;
        if (loadUri.startsWith("file://")) {
            try {
                String path = Uri.parse(loadUri).getPath();
                if (path != null) return new File(path);
            } catch (Exception ignored) {
            }
        }
        return loadUri;
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
        try {
            ArticleTtsHelper.stopAll();
        } catch (Exception ignored) {
        }
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
        // Not released — clear active-card mark so rebind does not pretend playback is live.
        activeExoPosition = -1;
        activeExoVideoUrl = null;
    }

    /**
     * Footer Reload (half Exo / full Exo / YouTube): drop the mid-watch timestamp so the
     * next play — overlay tap or autoplay restart — starts at 0.
     * Does not run on pause, flip, or mid-watch resume.
     */
    public static void resetPlaybackOnFeedReload() {
        discardSavedPlaybackUntilNextStart = true;
        try {
            if (exoPlayer != null) {
                exoPlayer.setPlayWhenReady(false);
                try {
                    exoPlayer.pause();
                } catch (Exception ignored) {
                }
                try {
                    if (exoPlayer.getPlaybackState() != Player.STATE_IDLE) {
                        exoPlayer.seekTo(0);
                    }
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
        }
        releaseActiveExoPlayer();
        // release clears tracking but must not re-enable position restore.
        discardSavedPlaybackUntilNextStart = true;
        try {
            if (youTubePlayerViewHalf != null) {
                youTubePlayerViewHalf.pause();
                youTubePlayerViewHalf.seekTo(0f);
            }
        } catch (Exception ignored) {
        }
        isYoutubeVideoIsInitialized = false;
        isFullYoutubeVideoIsInitialized = false;
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
        activeExoPosition = -1;
        activeExoVideoUrl = null;
        resetFeedExoSurfaceGuards();
    }

    public static boolean hasActiveVideoPlayer() {
        return youTubePlayerViewHalf != null || exoPlayer != null;
    }

    private static void clearSharedExoTracking() {
        activeExoPosition = -1;
        activeExoVideoUrl = null;
    }

    private void markSharedExoActive(int position, String videoUrl) {
        activeExoPosition = position;
        activeExoVideoUrl = videoUrl;
        // Fresh media item (play click) — mid-watch resume is allowed again until the next Reload.
        discardSavedPlaybackUntilNextStart = false;
        // Warm WhatsApp share cache while the user watches (downloadUrl + play URL).
        try {
            VideoShareHelper.prefetchVideoCandidates(context,
                    resolveVideoShareUrl(position), videoUrl);
        } catch (Exception ignored) {
        }
    }

    /**
     * True when the shared feed ExoPlayer is still bound to this flip index and URL
     * (preparing, buffering, or playing) — rebind must re-attach, not reset to idle play.
     */
    private boolean isSharedExoActiveFor(int position) {
        // Reload already dropped the saved timestamp — do not reattach the old player.
        if (discardSavedPlaybackUntilNextStart) {
            return false;
        }
        if (exoPlayer == null || list == null || position < 0 || position >= list.size()) {
            return false;
        }
        if (activeExoPosition != position) {
            return false;
        }
        try {
            String url = list.get(position).getVideoUrl();
            if (TextUtils.isEmpty(url) || !url.equals(activeExoVideoUrl)) {
                return false;
            }
            int state = exoPlayer.getPlaybackState();
            if (state == Player.STATE_ENDED) {
                return false;
            }
            // Fully stopped idle without play intent — treat as inactive.
            if (state == Player.STATE_IDLE && !exoPlayer.getPlayWhenReady()) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    private ExoPlayer createFeedExoPlayer() {
        feedExoFrameRendered = false;
        // Rebuffer longer before another stall so low-end decoders don't strobe.
        // Frame-rate strategy OFF: "seamless" mode switches flash white on some videos.
        DefaultLoadControl loadControl = new DefaultLoadControl.Builder()
                .setBufferDurationsMs(
                        /* minBufferMs */ 30_000,
                        /* maxBufferMs */ 60_000,
                        /* bufferForPlaybackMs */ 2_500,
                        /* bufferForPlaybackAfterRebufferMs */ 8_000)
                .setPrioritizeTimeOverSizeThresholds(true)
                .setBackBuffer(15_000, true)
                .build();
        DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(context)
                .setEnableDecoderFallback(true);
        ExoPlayer player = new ExoPlayer.Builder(context)
                .setRenderersFactory(renderersFactory)
                .setLoadControl(loadControl)
                .setVideoChangeFrameRateStrategy(C.VIDEO_CHANGE_FRAME_RATE_STRATEGY_OFF)
                .setSeekBackIncrementMs(5000)
                .setSeekForwardIncrementMs(5000)
                .build();
        try {
            player.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT);
        } catch (Exception ignored) {
        }
        player.addListener(new Player.Listener() {
            @Override
            public void onRenderedFirstFrame() {
                onFeedExoFirstFrame();
            }

            @Override
            public void onVideoSizeChanged(VideoSize videoSize) {
                // Adaptive / VFR size changes used to lift the shutter over the picture.
                if (feedExoFrameRendered) {
                    pinFeedExoLastFrame(currentFeedExoPlayerView());
                    hideFeedExoCoverOverlays();
                }
            }

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (exoPlayer == null) {
                    return;
                }
                if (playbackState == Player.STATE_ENDED) {
                    return;
                }
                if (feedExoFrameRendered
                        && (playbackState == Player.STATE_READY
                        || playbackState == Player.STATE_BUFFERING
                        || (playbackState == Player.STATE_IDLE && exoPlayer.getPlayWhenReady()))) {
                    pinFeedExoLastFrame(currentFeedExoPlayerView());
                    hideFeedExoCoverOverlays();
                }
            }
        });
        return player;
    }

    /**
     * Shared player is already rendering this card. Play/autoplay must not
     * release and recreate it (that clears the surface mid-watch).
     */
    private boolean isLiveExoOnView(PlayerView playerView, int position) {
        if (playerView == null || exoPlayer == null || playerView.getPlayer() != exoPlayer) {
            return false;
        }
        if (discardSavedPlaybackUntilNextStart) {
            return false;
        }
        try {
            int state = exoPlayer.getPlaybackState();
            if (state == Player.STATE_ENDED || state == Player.STATE_IDLE) {
                return false;
            }
            if (!exoPlayer.getPlayWhenReady() && state != Player.STATE_BUFFERING) {
                return false;
            }
            if (isSharedExoActiveFor(position)) {
                return true;
            }
            if (list == null || position < 0 || position >= list.size()) {
                return false;
            }
            String url = list.get(position).getVideoUrl();
            return !TextUtils.isEmpty(url) && url.equals(activeExoVideoUrl);
        } catch (Exception e) {
            return false;
        }
    }

    private static void resetFeedExoSurfaceGuards() {
        feedExoFrameRendered = false;
        feedExoPlayerViewRef = null;
        feedExoThumbnailOverlay = null;
        feedExoCenterPlayOverlay = null;
        hideExoSpinnerNow(pendingExoSpinner);
    }

    private static PlayerView currentFeedExoPlayerView() {
        return feedExoPlayerViewRef != null ? feedExoPlayerViewRef.get() : null;
    }

    private static void rememberFeedExoPlayerView(PlayerView playerView) {
        if (playerView != null) {
            feedExoPlayerViewRef = new java.lang.ref.WeakReference<>(playerView);
        }
    }

    /**
     * Picture is already on this PlayerView. Poster, shutter, and spinner must stay off
     * through ready, rebuffer, and a brief idle while playWhenReady is still true.
     */
    private static boolean isFeedExoSurfaceLive(PlayerView playerView) {
        if (!feedExoFrameRendered || exoPlayer == null || playerView == null) {
            return false;
        }
        if (playerView.getPlayer() != exoPlayer) {
            return false;
        }
        try {
            int state = exoPlayer.getPlaybackState();
            if (state == Player.STATE_ENDED) {
                return false;
            }
            return state == Player.STATE_READY
                    || state == Player.STATE_BUFFERING
                    || (state == Player.STATE_IDLE && exoPlayer.getPlayWhenReady());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Half-video rebinds (likes poll, progress) must not reload the poster over a live picture.
     * @return true when the ImageView sits on a PlayerView that is already showing frames
     */
    private static boolean refusePosterOverLiveVideo(ImageView target) {
        if (target == null || !feedExoFrameRendered) {
            return false;
        }
        View parent = target.getParent() instanceof View ? (View) target.getParent() : null;
        if (parent == null) {
            return false;
        }
        View player = parent.findViewById(R.id.normal_video_player);
        if (!(player instanceof PlayerView) || !isFeedExoSurfaceLive((PlayerView) player)) {
            return false;
        }
        target.setVisibility(View.GONE);
        return true;
    }

    private static void setFeedExoPosterCover(ImageView thumbnail, PlayerView playerView, boolean cover) {
        if (thumbnail == null) {
            return;
        }
        if (cover && isFeedExoSurfaceLive(playerView)) {
            thumbnail.setVisibility(View.GONE);
            return;
        }
        thumbnail.setVisibility(cover ? View.VISIBLE : View.GONE);
    }

    private static void hideFeedExoCoverOverlays() {
        hideExoSpinnerNow(pendingExoSpinner);
        if (feedExoThumbnailOverlay != null) {
            feedExoThumbnailOverlay.setVisibility(View.GONE);
        }
        if (feedExoCenterPlayOverlay != null) {
            feedExoCenterPlayOverlay.setVisibility(View.GONE);
        }
        PlayerView playerView = currentFeedExoPlayerView();
        if (playerView == null || playerView.getPlayer() != exoPlayer
                || !(playerView.getParent() instanceof View)) {
            return;
        }
        View parent = (View) playerView.getParent();
        View thumb = parent.findViewById(R.id.thaumbnail_for_video);
        if (thumb != null) {
            thumb.setVisibility(View.GONE);
        }
        View centerPlay = parent.findViewById(R.id.imageViewVideoPlayVideo);
        if (centerPlay != null) {
            centerPlay.setVisibility(View.GONE);
        }
        View progress = parent.findViewById(R.id.progress_bar_for_video);
        if (progress != null) {
            progress.setVisibility(View.GONE);
        }
    }

    private static void onFeedExoFirstFrame() {
        feedExoFrameRendered = true;
        pinFeedExoLastFrame(currentFeedExoPlayerView());
        hideFeedExoCoverOverlays();
    }

    /** If the first-frame callback was missed, a progressed position still means the picture is up. */
    private static void noteFeedExoFrameIfProgressed() {
        if (feedExoFrameRendered || exoPlayer == null) {
            return;
        }
        try {
            if (exoPlayer.getCurrentPosition() > 0L
                    && exoPlayer.getPlaybackState() != Player.STATE_ENDED) {
                onFeedExoFirstFrame();
            }
        } catch (Exception ignored) {
        }
    }

    private static void hideExoSpinnerNow(ProgressBar progress) {
        feedExoSpinnerArmed = false;
        EXO_OVERLAY_HANDLER.removeCallbacks(SHOW_EXO_SPINNER);
        if (progress != null) {
            progress.setVisibility(View.GONE);
        }
        if (pendingExoSpinner != null && pendingExoSpinner != progress) {
            pendingExoSpinner.setVisibility(View.GONE);
        }
        pendingExoSpinner = null;
    }

    /**
     * Buffering ring only before the first frame. After that a stall must not paint
     * the white ring (or the poster) over the picture — the last frame stays.
     */
    private static void scheduleExoSpinner(ProgressBar progress, boolean startup) {
        EXO_OVERLAY_HANDLER.removeCallbacks(SHOW_EXO_SPINNER);
        if (feedExoFrameRendered) {
            feedExoSpinnerArmed = false;
            if (progress != null) {
                progress.setVisibility(View.GONE);
            }
            pendingExoSpinner = null;
            return;
        }
        pendingExoSpinner = progress;
        feedExoSpinnerArmed = true;
        if (progress == null) {
            return;
        }
        if (startup) {
            progress.setVisibility(View.VISIBLE);
            return;
        }
        progress.setVisibility(View.GONE);
        EXO_OVERLAY_HANDLER.postDelayed(SHOW_EXO_SPINNER, EXO_SPINNER_DEBOUNCE_MS);
    }

    /** Explicit user play: keep playWhenReady true until the user pauses. */
    private void forceSharedExoPlay() {
        if (exoPlayer == null) {
            return;
        }
        try {
            userPausedVideoAutoplay = false;
            // Ended, or Reload just cleared the saved timestamp — never resume mid-watch.
            if (discardSavedPlaybackUntilNextStart
                    || exoPlayer.getPlaybackState() == Player.STATE_ENDED) {
                exoPlayer.seekTo(0);
                discardSavedPlaybackUntilNextStart = false;
            } else if (exoPlayer.getPlayWhenReady()
                    && (exoPlayer.isPlaying()
                    || exoPlayer.getPlaybackState() == Player.STATE_READY
                    || exoPlayer.getPlaybackState() == Player.STATE_BUFFERING)) {
                // Likes poll / progress rebind must not call play() again — that resets the renderer.
                return;
            }
            exoPlayer.setPlayWhenReady(true);
            exoPlayer.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Settings → Video Autoplay user pref. Unset is not "off" — see
     * {@link VideoAutoplayConfig#isUserOptOut(Context)}.
     */
    public static boolean isVideoAutoplayUserPrefEnabled(Context ctx) {
        return !VideoAutoplayConfig.isUserOptOut(ctx);
    }

    /**
     * Legacy admin global {@code video_autoplay_enabled}. Split flags fall back
     * to this when they have never been stored.
     */
    public static boolean isAdminVideoAutoplayEnabled(Context ctx) {
        return VideoAutoplayConfig.isLegacyEnabled(ctx);
    }

    /**
     * True when either card type may autoplay and the user has not opted out.
     * Start/chain should use {@link #shouldAutoplayPostType(Context, String)}.
     */
    public static boolean isVideoAutoplayEnabled(Context ctx) {
        if (VideoAutoplayConfig.isUserOptOut(ctx)) {
            return false;
        }
        return VideoAutoplayConfig.isFullEnabled(ctx)
                || VideoAutoplayConfig.isHalfEnabled(ctx);
    }

    /** Admin toggle for this card type, minus an explicit user opt-out. */
    public static boolean shouldAutoplayPostType(Context ctx, String postType) {
        return VideoAutoplayConfig.shouldAutoplay(ctx, postType);
    }

    /** Footer Video / Flash (Breaking) tab is selected. */
    public boolean isVideoFooterTabSelected() {
        return context != null
                && SharedPrefsData.getBool(context, Constants.isVideoPostsSelected, Constants.PREF_NAME);
    }

    /**
     * Autoplay this card when its admin toggle is ON, the user has not explicitly
     * opted out, and they have not paused on this page.
     */
    public boolean shouldAutoplayFeedVideo(String postType) {
        return shouldAutoplayPostType(context, postType)
                && !userPausedVideoAutoplay;
    }

    public void clearUserPausedVideoAutoplay() {
        userPausedVideoAutoplay = false;
    }

    public void markUserPausedVideoAutoplay() {
        userPausedVideoAutoplay = true;
    }

    /**
     * Notify HomePage to chain to the next flip page when a video ends under autoplay rules.
     */
    private void notifyVideoEndedForAutoplay(int position) {
        if (context == null || list == null || position < 0 || position >= list.size()
                || list.get(position) == null) {
            return;
        }
        if (!shouldAutoplayPostType(context, list.get(position).getPostType())) {
            return;
        }
        if (userPausedVideoAutoplay) {
            return;
        }
        // Debounce duplicate ended events from stacked listeners.
        long now = System.currentTimeMillis();
        if (position == lastAutoplayEndedNotifyPos && (now - lastAutoplayEndedNotifyAtMs) < 800L) {
            return;
        }
        lastAutoplayEndedNotifyPos = position;
        lastAutoplayEndedNotifyAtMs = now;
        try {
            if (callbacks != null) {
                callbacks.onVideoAutoplayEndedRequestNext(position);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * After flip settles with autoplay ON: start (or resume) the visible card.
     * Does not force-restart if the user paused on this page.
     */
    public void maybeAutoplayVisibleVideo(View convertView, int position) {
        if (convertView == null || list == null) {
            return;
        }
        if (position < 0 || position >= list.size() || list.get(position) == null) {
            return;
        }
        String postType = list.get(position).getPostType();
        if (!shouldAutoplayFeedVideo(postType)) {
            return;
        }
        // Already playing this card — leave alone. After Reload, do not resume the
        // previous timestamp; fall through to the overlay play click (starts at 0).
        try {
            if (!discardSavedPlaybackUntilNextStart
                    && exoPlayer != null && activeExoPosition == position) {
                int state = exoPlayer.getPlaybackState();
                if (exoPlayer.getPlayWhenReady()
                        && state != Player.STATE_ENDED
                        && state != Player.STATE_IDLE) {
                    return;
                }
                // Light-pause from mid-flip or idle prepare — resume without rebuild.
                if (state != Player.STATE_ENDED && !userPausedVideoAutoplay) {
                    forceSharedExoPlay();
                    return;
                }
            }
        } catch (Exception ignored) {
        }
        try {
            if (clickAutoplayControl(convertView.findViewById(R.id.imageViewVideoPlayVideo))) {
                return;
            }
            clickAutoplayControl(convertView.findViewById(R.id.imageViewPlayVideo));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Center-play overlay starts Exo or YouTube. Visibility used to skip the click
     * when the poster button was not yet VISIBLE, so a settled card never started.
     */
    private boolean clickAutoplayControl(ImageView button) {
        if (button == null) {
            return false;
        }
        try {
            if (!button.isEnabled()) {
                button.setEnabled(true);
            }
            if (button.getVisibility() != View.VISIBLE) {
                button.setVisibility(View.VISIBLE);
            }
            return button.performClick();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Extra YouTube ended listener for Video-tab autoplay chain. */
    private void attachYoutubeEndedAutoplayListener(
            com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer youTubePlayer,
            int position) {
        if (youTubePlayer == null) {
            return;
        }
        youTubePlayer.addListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onStateChange(
                    @NonNull com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer player,
                    @NonNull com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState state) {
                if (state == com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState.ENDED) {
                    notifyVideoEndedForAutoplay(position);
                }
            }
        });
    }

    private void releaseSharedExoPlayerQuietly() {
        if (exoPlayer != null) {
            try {
                exoPlayer.setPlayWhenReady(false);
                exoPlayer.stop();
                exoPlayer.release();
            } catch (Exception ignored) {
            }
            exoPlayer = null;
        }
        clearSharedExoTracking();
        resetFeedExoSurfaceGuards();
        try {
            if (feedExoSurfaceHost != null) {
                feedExoSurfaceHost.clearPlayerBinding();
            }
        } catch (Exception ignored) {
        }
        feedExoHostRootRef = null;
    }

    private void detachPlayerViewSafely(PlayerView playerView) {
        if (playerView == null) {
            return;
        }
        try {
            Player attached = playerView.getPlayer();
            if (attached != null && attached != exoPlayer) {
                try {
                    attached.release();
                } catch (Exception ignored) {
                }
            }
            if (playerView.getPlayer() != null) {
                playerView.setPlayer(null);
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Opaque TextureView so a stall keeps the last frame instead of punching a hole
     * through to the white page (SurfaceView). Shutter stays transparent so it cannot
     * paint over that frame. Do not set a background on the TextureView — that color
     * flashes between frames on low-end GPUs.
     */
    @OptIn(markerClass = UnstableApi.class)
    private void hardenFeedPlayerSurface(PlayerView playerView) {
        if (playerView == null) {
            return;
        }
        try {
            playerView.setKeepContentOnPlayerReset(true);
            playerView.setUseArtwork(false);
            playerView.setDefaultArtwork(null);
            playerView.setShutterBackgroundColor(android.graphics.Color.TRANSPARENT);
            playerView.setBackgroundColor(android.graphics.Color.BLACK);
            playerView.setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER);
            View surface = playerView.getVideoSurfaceView();
            if (surface instanceof TextureView) {
                ((TextureView) surface).setOpaque(true);
            }
            if (feedExoFrameRendered) {
                pinFeedExoLastFrame(playerView);
            } else {
                hideFeedExoShutter(playerView);
            }
        } catch (Exception ignored) {
        }
    }

    /** Shutter and artwork sit above the video. After the first frame they must stay off. */
    private static void hideFeedExoShutter(PlayerView playerView) {
        if (playerView == null) {
            return;
        }
        try {
            View shutter = playerView.findViewById(androidx.media3.ui.R.id.exo_shutter);
            if (shutter != null) {
                shutter.setBackgroundColor(android.graphics.Color.TRANSPARENT);
                shutter.setAlpha(0f);
                if (shutter.getVisibility() != View.GONE) {
                    shutter.setVisibility(View.GONE);
                }
            }
            View artwork = playerView.findViewById(androidx.media3.ui.R.id.exo_artwork);
            if (artwork != null && artwork.getVisibility() != View.GONE) {
                artwork.setVisibility(View.GONE);
            }
            View buffering = playerView.findViewById(androidx.media3.ui.R.id.exo_buffering);
            if (buffering != null && buffering.getVisibility() != View.GONE) {
                buffering.setVisibility(View.GONE);
            }
        } catch (Exception ignored) {
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    private static void pinFeedExoLastFrame(PlayerView playerView) {
        if (playerView == null) {
            return;
        }
        try {
            View surface = playerView.getVideoSurfaceView();
            if (surface instanceof TextureView) {
                ((TextureView) surface).setOpaque(true);
            }
            playerView.setShutterBackgroundColor(android.graphics.Color.TRANSPARENT);
            playerView.setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER);
            hideFeedExoShutter(playerView);
        } catch (Exception ignored) {
        }
    }

    /**
     * Bind shared Exo to the in-card PlayerView (TextureView). FeedExoSurfaceHost is
     * disabled for playback — reparent + surface_type=none left a black picture with
     * chrome only. FlipView pin still avoids scrap thrash mid-play.
     */
    private boolean attachSharedExoToPlayerView(PlayerView scrapPlayerView) {
        View cardRoot = findExoCardRoot(scrapPlayerView);
        return attachSharedExoToPlayerView(scrapPlayerView, cardRoot);
    }

    private boolean attachSharedExoToPlayerView(PlayerView scrapPlayerView, View cardRoot) {
        if (exoPlayer == null || scrapPlayerView == null) {
            return false;
        }
        // Park any leftover surface-host view so it cannot sit under/over the card blank.
        try {
            if (feedExoSurfaceHost != null) {
                feedExoSurfaceHost.parkSurface();
            }
        } catch (Exception ignored) {
        }
        if (cardRoot != null) {
            rememberFeedExoHost(cardRoot);
        }
        rememberFeedExoPlayerView(scrapPlayerView);
        boolean changed = false;
        try {
            if (scrapPlayerView.getVisibility() != View.VISIBLE) {
                scrapPlayerView.setVisibility(View.VISIBLE);
            }
            hardenFeedPlayerSurface(scrapPlayerView);
            Player attached = scrapPlayerView.getPlayer();
            if (attached == exoPlayer) {
                if (feedExoFrameRendered) {
                    pinFeedExoLastFrame(scrapPlayerView);
                }
            } else {
                if (attached != null && attached != exoPlayer) {
                    try {
                        attached.release();
                    } catch (Exception ignored) {
                    }
                }
                scrapPlayerView.setPlayer(exoPlayer);
                hardenFeedPlayerSurface(scrapPlayerView);
                changed = true;
            }
            return changed;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** HomePage binds the stable surface PlayerView once after setContentView. */
    public static void setFeedExoSurfaceHost(@Nullable FeedExoSurfaceHost host) {
        feedExoSurfaceHost = host;
    }

    @Nullable
    private static PlayerView stableFeedSurfacePlayerView() {
        return feedExoSurfaceHost != null ? feedExoSurfaceHost.getPlayerView() : null;
    }

    private static void rememberFeedExoHost(@Nullable View root) {
        feedExoHostRootRef = root != null ? new java.lang.ref.WeakReference<>(root) : null;
    }

    /**
     * Seat index currently owning the shared feed ExoPlayer, or -1.
     * Used by FlipView to keep that card's view tree across notify rebinds.
     */
    public static int getPinnedLiveExoPosition() {
        if (discardSavedPlaybackUntilNextStart || exoPlayer == null) {
            return -1;
        }
        return activeExoPosition;
    }

    /**
     * True when {@code root} is the Flip card chrome host for the active Exo seat.
     * TextureView lives on {@link FeedExoSurfaceHost}; FlipView still pins this tree so
     * likes/progress rebinds do not thrash the card mid-play.
     */
    public static boolean isPinnedLiveExoHost(View root) {
        if (root == null || exoPlayer == null || activeExoPosition < 0) {
            return false;
        }
        if (discardSavedPlaybackUntilNextStart) {
            return false;
        }
        try {
            if (exoPlayer.getPlaybackState() == Player.STATE_ENDED) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        View pinnedRoot = feedExoHostRootRef != null ? feedExoHostRootRef.get() : null;
        return pinnedRoot != null && pinnedRoot == root;
    }

    @Nullable
    private static View findExoCardRoot(@Nullable View from) {
        View cur = from;
        while (cur != null) {
            if (cur.findViewById(R.id.exo_play_layout) != null
                    && cur.findViewById(R.id.normal_video_player) != null
                    && cur != from) {
                return cur;
            }
            // convertView often IS the root that contains exo_play_layout
            if (cur.findViewById(R.id.exo_play_layout) != null
                    && cur.findViewById(R.id.side_menu) != null) {
                return cur;
            }
            Object parent = cur.getParent();
            if (!(parent instanceof View)) {
                break;
            }
            cur = (View) parent;
        }
        return from;
    }

    /** Scrap PlayerView: chrome only — transparent so the stable surface underneath shows. */
    private static void prepareScrapAsChromeOnly(@Nullable PlayerView scrap) {
        if (scrap == null) {
            return;
        }
        try {
            scrap.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            scrap.setShutterBackgroundColor(android.graphics.Color.TRANSPARENT);
            scrap.setUseArtwork(false);
            scrap.setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER);
            View shutter = scrap.findViewById(androidx.media3.ui.R.id.exo_shutter);
            if (shutter != null) {
                shutter.setVisibility(View.GONE);
            }
            if (scrap.getVisibility() != View.VISIBLE) {
                scrap.setVisibility(View.VISIBLE);
            }
        } catch (Exception ignored) {
        }
    }

    private void bindScrapChromePlayer(@Nullable PlayerView scrap) {
        if (scrap == null || exoPlayer == null) {
            return;
        }
        prepareScrapAsChromeOnly(scrap);
        try {
            if (scrap.getPlayer() != exoPlayer) {
                scrap.setPlayer(exoPlayer);
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Center overlay (thumbnail / big play / spinner) from Exo state.
     * Transient IDLE while preparing with playWhenReady=true must NOT put the play button back.
     * Once a frame is on screen, do not paint poster, shutter, or spinner over it until
     * the user pauses and leaves, the clip ends, or playback errors.
     */
    private void applyExoOverlayState(
            int playbackState,
            boolean playWhenReady,
            ImageView thumbnail,
            ImageView centerPlay,
            ProgressBar progress,
            PlayerView playerView) {
        feedExoThumbnailOverlay = thumbnail;
        feedExoCenterPlayOverlay = centerPlay;
        if (playerView != null) {
            rememberFeedExoPlayerView(playerView);
            hardenFeedPlayerSurface(playerView);
        }
        noteFeedExoFrameIfProgressed();
        boolean holdingFrame = isFeedExoSurfaceLive(playerView)
                || (feedExoFrameRendered
                && exoPlayer != null
                && playbackState != Player.STATE_ENDED
                && (playbackState == Player.STATE_READY
                || playbackState == Player.STATE_BUFFERING
                || (playbackState == Player.STATE_IDLE && playWhenReady)));
        if (holdingFrame) {
            pinFeedExoLastFrame(playerView);
            setFeedExoPosterCover(thumbnail, playerView, false);
            if (centerPlay != null) {
                centerPlay.setVisibility(View.GONE);
            }
            hideExoSpinnerNow(progress);
            return;
        }
        if (playbackState == Player.STATE_BUFFERING) {
            if (centerPlay != null) {
                centerPlay.setVisibility(View.GONE);
            }
            setFeedExoPosterCover(thumbnail, playerView, true);
            scheduleExoSpinner(progress, true);
        } else if (playbackState == Player.STATE_READY) {
            hideExoSpinnerNow(progress);
            if (playWhenReady) {
                setFeedExoPosterCover(thumbnail, playerView, !feedExoFrameRendered);
                if (centerPlay != null) {
                    centerPlay.setVisibility(View.GONE);
                }
            } else if (centerPlay != null) {
                centerPlay.setVisibility(View.GONE);
            }
        } else if (playbackState == Player.STATE_ENDED) {
            hideExoSpinnerNow(progress);
            feedExoFrameRendered = false;
            setFeedExoPosterCover(thumbnail, playerView, true);
            if (centerPlay != null) {
                centerPlay.setVisibility(View.VISIBLE);
            }
        } else if (playbackState == Player.STATE_IDLE) {
            if (playWhenReady) {
                if (centerPlay != null) {
                    centerPlay.setVisibility(View.GONE);
                }
                setFeedExoPosterCover(thumbnail, playerView, true);
                scheduleExoSpinner(progress, true);
            } else {
                hideExoSpinnerNow(progress);
                setFeedExoPosterCover(thumbnail, playerView, true);
                if (centerPlay != null) {
                    centerPlay.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    /** Back-compat overload when PlayerView is unavailable. */
    private void applyExoOverlayState(
            int playbackState,
            boolean playWhenReady,
            ImageView thumbnail,
            ImageView centerPlay,
            ProgressBar progress) {
        applyExoOverlayState(playbackState, playWhenReady, thumbnail, centerPlay, progress, null);
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
     * Way2News chrome: play circle when idle/paused; mute + fullscreen + thin red line when
     * playing; no center pause, no ±10, no timestamp. Media3 1.1.0 does not drive legacy
     * {@code exo_play}/{@code exo_pause}, so we manage visibility from player state.
     * Play Store classic uses Media3 default controller visibility instead.
     */
    private void updateExoCenterControlIcons(PlayerView playerView) {
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
        noteFeedExoFrameIfProgressed();

        int state = exoPlayer != null ? exoPlayer.getPlaybackState() : Player.STATE_IDLE;
        boolean ended = state == Player.STATE_ENDED;
        // After the first frame, a brief BUFFERING blip is not a stall — keep mute /
        // fullscreen. The white ring is scheduled separately and only if it lasts.
        boolean buffering = !feedExoFrameRendered
                && (state == Player.STATE_BUFFERING
                || (state == Player.STATE_IDLE && exoPlayer != null && exoPlayer.getPlayWhenReady()));
        boolean playing = exoPlayer != null
                && exoPlayer.getPlayWhenReady()
                && !ended
                && (state == Player.STATE_READY
                || (feedExoFrameRendered && state == Player.STATE_BUFFERING));

        if (pauseBtn != null) {
            pauseBtn.setVisibility(GONE);
        }

        if (buffering) {
            // Image 3: spinner (outside controller) replaces play; hide playing chrome briefly.
            if (playBtn != null) {
                playBtn.setVisibility(GONE);
            }
            setWay2PlayingChromeVisible(muteBtn, fullscreenBtn, progressBar, false);
        } else if (playing) {
            // Image 4: mute + fullscreen + thin red line only.
            // Do not re-show the controller on every tick — that fades an overlay over the picture.
            if (playBtn != null) {
                playBtn.setVisibility(GONE);
            }
            setWay2PlayingChromeVisible(muteBtn, fullscreenBtn, progressBar, true);
            pinFeedExoLastFrame(playerView);
            if (muteBtn == null || muteBtn.getVisibility() != VISIBLE) {
                try {
                    playerView.showController();
                } catch (Exception ignored) {
                }
            }
        } else if (ended) {
            // Replay is the overlay center play (imageViewVideoPlayVideo).
            if (playBtn != null) {
                playBtn.setVisibility(GONE);
            }
            setWay2PlayingChromeVisible(muteBtn, fullscreenBtn, progressBar, false);
        } else {
            // Image 2: idle / paused — large play circle only.
            if (playBtn != null) {
                playBtn.setVisibility(VISIBLE);
            }
            setWay2PlayingChromeVisible(muteBtn, fullscreenBtn, progressBar, false);
            try {
                playerView.showController();
            } catch (Exception ignored) {
            }
        }
    }

    private void setWay2PlayingChromeVisible(View muteBtn, View fullscreenBtn, View progressBar,
                                             boolean visible) {
        int vis = visible ? VISIBLE : GONE;
        if (muteBtn != null) {
            muteBtn.setVisibility(vis);
        }
        if (fullscreenBtn != null) {
            // Full-bleed article video hides fullscreen in existing product logic.
            if (visible && fullscreenBtn.getTag() != null
                    && "force_gone".equals(String.valueOf(fullscreenBtn.getTag()))) {
                fullscreenBtn.setVisibility(GONE);
            } else {
                fullscreenBtn.setVisibility(vis);
            }
        }
        if (progressBar != null) {
            progressBar.setVisibility(vis);
            // Scrub readout only while dragging; never leave it up when chrome hides.
            View scrubTime = null;
            if (progressBar.getParent() instanceof View) {
                View parent = (View) progressBar.getParent();
                scrubTime = parent.findViewById(R.id.exo_scrub_time);
            }
            if (scrubTime == null && progressBar.getRootView() != null) {
                scrubTime = progressBar.getRootView().findViewById(R.id.exo_scrub_time);
            }
            if (!visible && scrubTime != null) {
                scrubTime.setVisibility(GONE);
            }
        }
    }

    /** Play Store classic: auto-hide controller after idle (Media3 default UX). */
    private void applyClassicControllerPolicy(PlayerView playerView) {
        if (playerView == null) {
            return;
        }
        playerView.setControllerShowTimeoutMs(3500);
        playerView.setControllerHideOnTouch(true);
        playerView.setControllerAutoShow(true);
        try {
            playerView.showController();
        } catch (Exception ignored) {
        }
    }

    /** Keep Way2 chrome always available (no auto-hide strip / dark overlay flash). */
    private void applyWay2ControllerPolicy(PlayerView playerView) {
        if (playerView == null) {
            return;
        }
        playerView.setControllerShowTimeoutMs(0);
        playerView.setControllerHideOnTouch(false);
        playerView.setControllerAutoShow(true);
        View muteBtn = playerView.findViewById(R.id.mute_button);
        if (muteBtn == null || muteBtn.getVisibility() != VISIBLE) {
            try {
                playerView.showController();
            } catch (Exception ignored) {
            }
        }
        wireWay2ScrubTime(playerView);
    }

    private void applyFeedPlayerControllerPolicy(PlayerView playerView) {
        if (FeedVideoPlayerConfig.isPlayStoreClassic(context)) {
            applyClassicControllerPolicy(playerView);
        } else {
            applyWay2ControllerPolicy(playerView);
        }
    }

    /**
     * While the user drags the red progress line, show seek position (e.g. {@code 0:50}).
     * Hidden at all other times. Seeking itself is already handled by Media3.
     */
    @OptIn(markerClass = UnstableApi.class)
    private void wireWay2ScrubTime(PlayerView playerView) {
        if (playerView == null) {
            return;
        }
        View progress = playerView.findViewById(R.id.exo_progress);
        final TextView scrubTime = playerView.findViewById(R.id.exo_scrub_time);
        if (!(progress instanceof TimeBar) || scrubTime == null) {
            return;
        }
        // Avoid stacking duplicate listeners when the same PlayerView is re-wired.
        Object wired = scrubTime.getTag(R.id.exo_scrub_time);
        if (Boolean.TRUE.equals(wired)) {
            return;
        }
        scrubTime.setTag(R.id.exo_scrub_time, Boolean.TRUE);
        scrubTime.setVisibility(GONE);

        final StringBuilder formatBuilder = new StringBuilder();
        final Formatter formatter = new Formatter(formatBuilder, Locale.getDefault());
        TimeBar timeBar = (TimeBar) progress;
        timeBar.addListener(new TimeBar.OnScrubListener() {
            @Override
            public void onScrubStart(TimeBar timeBar, long position) {
                scrubTime.setText(Util.getStringForTime(formatBuilder, formatter, position));
                scrubTime.setVisibility(VISIBLE);
                scrubTime.bringToFront();
            }

            @Override
            public void onScrubMove(TimeBar timeBar, long position) {
                scrubTime.setText(Util.getStringForTime(formatBuilder, formatter, position));
                if (scrubTime.getVisibility() != VISIBLE) {
                    scrubTime.setVisibility(VISIBLE);
                }
            }

            @Override
            public void onScrubStop(TimeBar timeBar, long position, boolean canceled) {
                scrubTime.setVisibility(GONE);
            }
        });
    }

    /**
     * Attaches a listener so the center icons re-sync whenever the player state changes
     * (auto-play start, buffering, pause, end). Used by both half and full video.
     * Classic/Play Store path relies on Media3 controller; skip Way2 icon sync.
     */
    private void attachExoCenterIconSync(PlayerView playerView) {
        if (playerView == null || exoPlayer == null) {
            return;
        }
        applyFeedPlayerControllerPolicy(playerView);
        if (FeedVideoPlayerConfig.isPlayStoreClassic(context)) {
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
     * Wires half (inline feed) chrome for the selected feed player mode.
     */
    private void wireHalfVideoExoPauseControl(PlayerView playerView) {
        if (playerView == null) {
            return;
        }
        applyFeedPlayerControllerPolicy(playerView);
        if (FeedVideoPlayerConfig.isPlayStoreClassic(context)) {
            wireClassicPlayPauseControls(playerView, null);
            return;
        }
        wireWay2PlayPauseControls(playerView, null);
        updateExoCenterControlIcons(playerView);
    }

    private void wireFullVideoExoPauseControl(PlayerView playerView, View convertView) {
        if (playerView == null || convertView == null) {
            return;
        }
        applyFeedPlayerControllerPolicy(playerView);
        // Full-card layout historically hides the fullscreen control (already full bleed).
        View fullscreenBtn = playerView.findViewById(R.id.fullscreen_button);
        if (fullscreenBtn != null) {
            fullscreenBtn.setTag("force_gone");
            fullscreenBtn.setVisibility(GONE);
        }
        if (FeedVideoPlayerConfig.isPlayStoreClassic(context)) {
            wireClassicPlayPauseControls(playerView, convertView);
            return;
        }
        wireWay2PlayPauseControls(playerView, convertView);
        updateExoCenterControlIcons(playerView);
    }

    /** Play Store classic: explicit play/pause buttons (±10 handled by Media3). */
    private void wireClassicPlayPauseControls(PlayerView playerView, View convertView) {
        View pauseBtn = playerView.findViewById(R.id.exo_pause);
        View playBtn = playerView.findViewById(R.id.exo_play);
        if (pauseBtn != null) {
            pauseBtn.setOnClickListener(v -> {
                if (exoPlayer != null) {
                    markUserPausedVideoAutoplay();
                    exoPlayer.setPlayWhenReady(false);
                    exoPlayer.pause();
                }
                pauseBtn.setVisibility(GONE);
                if (playBtn != null) {
                    playBtn.setVisibility(VISIBLE);
                }
                if (convertView != null) {
                    LinearLayout sideMenu = convertView.findViewById(R.id.video_side_menu);
                    if (sideMenu != null) {
                        sideMenu.setVisibility(View.VISIBLE);
                        sideMenu.bringToFront();
                    }
                }
            });
        }
        if (playBtn != null) {
            playBtn.setOnClickListener(v -> {
                if (exoPlayer != null) {
                    if (exoPlayer.getPlaybackState() == Player.STATE_ENDED) {
                        exoPlayer.seekTo(0);
                    }
                    forceSharedExoPlay();
                }
                playBtn.setVisibility(GONE);
                if (pauseBtn != null) {
                    pauseBtn.setVisibility(VISIBLE);
                }
            });
        }
    }

    private void wireWay2PlayPauseControls(PlayerView playerView, View convertView) {
        View playBtn = playerView.findViewById(R.id.exo_play);
        View root = playerView.findViewById(R.id.exo_controller_root);
        if (root == null) {
            root = playerView;
        }
        if (playBtn != null) {
            playBtn.setOnClickListener(v -> {
                if (exoPlayer != null) {
                    if (exoPlayer.getPlaybackState() == Player.STATE_ENDED) {
                        exoPlayer.seekTo(0);
                    }
                    forceSharedExoPlay();
                }
                updateExoCenterControlIcons(playerView);
            });
        }
        // Tap empty area while playing → pause (Image 2 play circle returns).
        root.setOnClickListener(v -> {
            if (exoPlayer == null) {
                return;
            }
            int state = exoPlayer.getPlaybackState();
            boolean ended = state == Player.STATE_ENDED;
            boolean playing = exoPlayer.getPlayWhenReady() && !ended && state == Player.STATE_READY;
            if (playing) {
                markUserPausedVideoAutoplay();
                exoPlayer.setPlayWhenReady(false);
                exoPlayer.pause();
                updateExoCenterControlIcons(playerView);
                if (convertView != null) {
                    LinearLayout sideMenu = convertView.findViewById(R.id.video_side_menu);
                    if (sideMenu != null) {
                        sideMenu.setVisibility(View.VISIBLE);
                        sideMenu.bringToFront();
                    }
                }
            }
        });
    }

    /**
     * Sync Like/Comment/Share (video_side_menu) with ExoPlayer controller visibility.
     * Current/Way2 keeps mute/fs/progress always on while playing; classic shows engagement
     * with the auto-hiding controller (Play Store behavior).
     */
    private void wireFullVideoExoSideMenuWithController(PlayerView playerView, View convertView) {
        if (playerView == null || convertView == null) {
            return;
        }
        final LinearLayout sideMenu = convertView.findViewById(R.id.video_side_menu);
        final boolean classic = FeedVideoPlayerConfig.isPlayStoreClassic(context);
        applyFeedPlayerControllerPolicy(playerView);
        // Explicit type — Media3 PlayerView overloads VisibilityListener + ControllerVisibilityListener
        playerView.setControllerVisibilityListener(
                (PlayerView.ControllerVisibilityListener) visibility -> {
            if (sideMenu == null) {
                return;
            }
            if (classic) {
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
                return;
            }
            if (visibility == View.VISIBLE) {
                boolean playing = exoPlayer != null && exoPlayer.isPlaying();
                // While playing, Way2 hides engagement chrome; show again when paused.
                sideMenu.setVisibility(playing ? View.INVISIBLE : View.VISIBLE);
                if (!playing) {
                    sideMenu.bringToFront();
                    sideMenu.setClickable(true);
                }
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
        if (!tryAcquireShareLaunch()) {
            return;
        }
        try {
            ModelClass.Datum item = list.get(position);
            String videoUrl = resolveVideoShareUrl(position);
            if (videoUrl == null) {
                releaseShareLaunch();
                Toast.makeText(context, "Video is not available to share", Toast.LENGTH_SHORT).show();
                return;
            }
            final boolean waShare = mode == VideoShareHelper.ShareMode.WHATSAPP_CHAT
                    || mode == VideoShareHelper.ShareMode.WHATSAPP_STATUS;
            final boolean halfArticle = "normal_video".equalsIgnoreCase(item.getPostType());
            VideoShareHelper.OnShareCompleteListener shareListener =
                    new VideoShareHelper.OnShareCompleteListener() {
                        @Override
                        public void onShareStarted() {
                            releaseShareLaunch();
                        }

                        @Override
                        public void onShareConfirmed() {
                            Share_Count_Value(shareCountProperty(waShare), item.getPostId());
                        }
                    };
            if (halfArticle) {
                VideoShareHelper.shareHalfArticleVideo(context, activity,
                        resolveVideoShareUrl(position),
                        item.getVideoUrl(),
                        item.getTitle(),
                        item.getDescription(), item.getShareLink(), mode,
                        shareListener);
            } else {
                VideoShareHelper.shareVideo(context, activity,
                        resolveVideoShareUrl(position),
                        item.getVideoUrl(),
                        item.getTitle(),
                        item.getDescription(), item.getShareLink(), mode,
                        shareListener);
            }
        } catch (Exception e) {
            e.printStackTrace();
            releaseShareLaunch();
            Toast.makeText(context, "Unable to share video", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Half/full video share caption: title + article body + link (not title/link only).
     */
    private String buildVideoArticleShareText(int position) {
        if (list == null || position < 0 || position >= list.size()) {
            return "";
        }
        return VideoShareHelper.buildShareMessage(
                list.get(position).getTitle(),
                list.get(position).getDescription(),
                list.get(position).getShareLink(),
                list.get(position).getPostId());
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
        long now = SystemClock.elapsedRealtime();
        // Short debounce only — do not reuse the 900ms share-launch guard or the sheet feels dead.
        if ((now - lastVideoShareUiAtMs) < 280L) {
            return;
        }
        lastVideoShareUiAtMs = now;
        if (layout == VideoShareLayout.HALF_EXO || layout == VideoShareLayout.HALF_YOUTUBE) {
            String primary = resolveVideoShareUrl(position);
            String alternate = list.get(position) != null ? list.get(position).getVideoUrl() : null;
            VideoShareHelper.prefetchHalfArticleVideo(context, primary, alternate);
        }
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

        View whatsappChatOption = sheetView.findViewById(R.id.option_whatsapp_chat);

        final boolean[] sheetActionTaken = {false};
        View.OnClickListener dismissAndShare = actionView -> {
            if (sheetActionTaken[0]) {
                return;
            }
            sheetActionTaken[0] = true;
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

        // Show the sheet first — style / prefetch after paint so Share feels instant.
        bottomSheet.show();
        sheetView.post(() -> {
            if (whatsappChatOption != null) {
                FullVideoShareCardHelper.apply(context, whatsappChatOption);
            }
            if (canShareVideoFile(position)) {
                if (layout == VideoShareLayout.HALF_EXO || layout == VideoShareLayout.HALF_YOUTUBE) {
                    String alternate = list.get(position) != null ? list.get(position).getVideoUrl() : null;
                    VideoShareHelper.prefetchHalfArticleVideo(context, resolveVideoShareUrl(position), alternate);
                } else {
                    VideoShareHelper.prefetchVideo(context, resolveVideoShareUrl(position));
                }
            }
        });
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

    private boolean isHalfVideoPost(int position) {
        return list != null && position >= 0 && position < list.size()
                && list.get(position) != null
                && "normal_video".equalsIgnoreCase(list.get(position).getPostType());
    }

    private String halfVideoDownloadFileName(int position) {
        return "anyNews_" + System.currentTimeMillis() + "_"
                + list.get(position).getPostId() + ".mp4";
    }

    /**
     * Half-article download from the legacy permission callback. Full-screen
     * videos stay on DownloadManager.
     */
    public void downloadHalfArticleVideo(int position) {
        downloadFullVideo(position);
    }

    private void runWhenVideoStorageAllowed(Runnable action) {
        if (action == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                || ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            action.run();
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
                                action.run();
                            }
                        }
                    });
        } else if (activity != null) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 234);
        }
    }

    /**
     * "Download Video" for half and full posts. Half-article files are stamped
     * with the ANYNEWS share logo before they are saved. Full-screen videos
     * stay on DownloadManager, unchanged.
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
        if (isHalfVideoPost(position)) {
            runWhenVideoStorageAllowed(() -> VideoShareHelper.saveHalfArticleVideo(
                    context, activity, videoUrl,
                    list.get(position).getVideoUrl(),
                    halfVideoDownloadFileName(position)));
            return;
        }
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

    private void launchScreenshotShareIntent(
            Intent intent, int whatsappMode, @Nullable Runnable onShareConfirmed) {
        try {
            if (whatsappMode == 1) {
                if (!appInstalledOrNot("com.whatsapp")) {
                    Toast.makeText(context, "WhatsApp is not installed", Toast.LENGTH_SHORT).show();
                    return;
                }
                intent.putExtra("jid", "status@broadcast");
                intent.setPackage("com.whatsapp");
                ShareOutboundHelper.launchChooserOnTargetChosen(
                        context, intent, "Share Link", onShareConfirmed);
            } else if (whatsappMode == 2) {
                if (!appInstalledOrNot("com.whatsapp")) {
                    Toast.makeText(context, "WhatsApp is not installed", Toast.LENGTH_SHORT).show();
                    return;
                }
                intent.setPackage("com.whatsapp");
                ShareOutboundHelper.launchChooserOnTargetChosen(
                        context, intent, "Share Link", onShareConfirmed);
            } else if (context != null && context.getPackageName().equals(BuildConfig.APPLICATION_ID)) {
                ShareOutboundHelper.launchChooserOnTargetChosen(
                        context, intent, "Share Link", onShareConfirmed);
            }
        } finally {
            releaseShareLaunch();
        }
    }

    public void shareFullMagazine(int position) {
        if (list == null || position < 0 || position >= list.size()) {
            return;
        }
        if (!tryAcquireShareLaunch()) {
            return;
        }
        prepareAndShareFullMagazine(position);
    }

    public void shareMagazineImagesFallback(int position) {
        if (!tryAcquireShareLaunch()) {
            return;
        }
        prepareAndShareFullMagazine(position);
    }

    private void prepareAndShareFullMagazine(int position) {
        ModelClass.Datum item = list.get(position);
        String downloadUrl = item.getDownloadUrl();
        List<String> images = item.getImages();
        boolean hasPdf = downloadUrl != null && !downloadUrl.trim().isEmpty();
        boolean hasImages = images != null && !images.isEmpty();

        if (!hasPdf && !hasImages) {
            releaseShareLaunch();
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
                        postToMain(() -> {
                            launchMagazinePdfShareIntent(position, pdfUri);
                            releaseShareLaunch();
                        });
                        return;
                    }
                }
                if (hasImages) {
                    ArrayList<Uri> uris = downloadMagazineImagesToCache(position, images);
                    if (!uris.isEmpty()) {
                        postToMain(() -> {
                            launchMagazineImagesShareIntent(position, uris);
                            releaseShareLaunch();
                        });
                        return;
                    }
                }
                postToMain(() -> {
                    releaseShareLaunch();
                    Toast.makeText(context, "Unable to share magazine", Toast.LENGTH_LONG).show();
                });
            } catch (Exception e) {
                e.printStackTrace();
                postToMain(() -> {
                    releaseShareLaunch();
                    Toast.makeText(context, "Unable to share magazine", Toast.LENGTH_LONG).show();
                });
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
        String mInvitationUrl = shareLinkForGalleryPage(position, null);
        String msg = ShareOutboundHelper.formatCaption(list.get(position).getTitle(), mInvitationUrl, list.get(position).getPostId());
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_STREAM, pdfUri);
        intent.putExtra(Intent.EXTRA_SUBJECT, mInvitationUrl);
        intent.putExtra(Intent.EXTRA_TEXT, msg);
        boolean pdfWasWhatsapp = fromWhatsapp;
        applyWhatsappTarget(intent);
        grantShareUriPermissions(intent, Collections.singletonList(pdfUri));
        final long pdfPostId = list.get(position).getPostId();
        startShareActivity(intent, "Share Magazine",
                () -> Share_Count_Value(shareCountProperty(pdfWasWhatsapp), pdfPostId));
        fromWhatsapp = false;
    }

    private void launchMagazineImagesShareIntent(int position, ArrayList<Uri> uris) {
        String mInvitationUrl = shareLinkForGalleryPage(position, null);
        String msg = ShareOutboundHelper.formatCaption(list.get(position).getTitle(), mInvitationUrl, list.get(position).getPostId());
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
        boolean imagesWasWhatsapp = fromWhatsapp;
        applyWhatsappTarget(intent);
        grantShareUriPermissions(intent, uris);
        final long imagesPostId = list.get(position).getPostId();
        startShareActivity(intent, "Share Magazine",
                () -> Share_Count_Value(shareCountProperty(imagesWasWhatsapp), imagesPostId));
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

    private void startShareActivity(
            Intent intent, String chooserTitle, @Nullable Runnable onShareConfirmed) {
        try {
            // Packaged WhatsApp and the open share sheet both count only on target pick.
            Context launchCtx = (activity != null && !activity.isFinishing()) ? activity : context;
            if (launchCtx == null) {
                return;
            }
            ShareOutboundHelper.launchChooserOnTargetChosen(
                    launchCtx, intent, chooserTitle, onShareConfirmed);
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
        // Android 13+: need POST_NOTIFICATIONS for our tap-to-open complete notification.
        if (from != 0 && activity != null) {
            VideoDownloadConfirmHelper.ensureNotificationPermission(activity);
        }
        String downloadingTitle="Downloading magazine...";
        if(from==0){
             downloadingTitle="Downloading magazine...";
            Toast.makeText(context,"Your magazine is downloading",Toast.LENGTH_LONG).show();
        }else{
             downloadingTitle="Downloading Video...";
            Toast.makeText(context,"Your video is downloading",Toast.LENGTH_LONG).show();
        }

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setTitle(title);
        request.setDescription(downloadingTitle);
        // Keep DownloadManager's own status-bar entry visible after complete as a backup.
        request.setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, title);
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        long downloadId = downloadManager.enqueue(request);
        showLog("DOWNLOAD_ID::::::"+downloadId);


        BroadcastReceiver onComplete = new BroadcastReceiver() {
            public void onReceive(Context receiverContext, Intent intent1) {
                long completedId = intent1 != null
                        ? intent1.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
                        : -1L;
                if (completedId != downloadId) {
                    // Another download finished — keep listening for ours.
                    return;
                }
                try {
                    receiverContext.unregisterReceiver(this);
                } catch (Exception ignored) {
                }
                showLog("ACTION:"+ (intent1 != null ? intent1.getAction() : "null"));
                // Confirm success via DownloadManager (do not trust flaky my_downloads ContentObserver).
                new Handler(Looper.getMainLooper()).postDelayed(() ->
                        showPopupDownloadMessage(position, title, from, downloadId), 400);
            }
        };
        IntentFilter downloadFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(onComplete, downloadFilter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            context.registerReceiver(onComplete, downloadFilter);
        }
    }

    private boolean isDownloadManagerSuccessful(long downloadId) {
        if (downloadId <= 0) {
            return false;
        }
        DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        if (dm == null) {
            return false;
        }
        Cursor cursor = null;
        try {
            cursor = dm.query(new DownloadManager.Query().setFilterById(downloadId));
            if (cursor != null && cursor.moveToFirst()) {
                int status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
                int reason = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON));
                showLog("DM status=" + status + " reason=" + reason + " id=" + downloadId);
                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    isVideoDownloadCancel = false;
                    return true;
                }
                if (status == DownloadManager.STATUS_FAILED
                        || status == DownloadManager.STATUS_PAUSED) {
                    isVideoDownloadCancel = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return false;
    }

    private void showPopupDownloadMessage(int position, String title, int from) {
        showPopupDownloadMessage(position, title, from, -1L);
    }

    private void showPopupDownloadMessage(int position, String title, int from, long downloadId) {
        if (from == 0) {
            Toast.makeText(context, "Your magazine is downloaded", Toast.LENGTH_LONG).show();
            try {
                String mInvitationUrl = list.get(position).getShareLink();
                showLog("mInvitationUrl:" + mInvitationUrl);
                String msg = ShareOutboundHelper.formatCaption(list.get(position).getTitle(), mInvitationUrl, list.get(position).getPostId());
                showLog("msg:" + msg);
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), title);
                showLog("file url:" + file.getAbsolutePath());
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
                startShareActivity(intent, "Share Magazine", null);
                fromWhatsapp = false;
            } catch (Exception e) {
                e.printStackTrace();
                shareFullMagazine(position);
            }
            return;
        }

        boolean success = downloadId > 0
                ? isDownloadManagerSuccessful(downloadId)
                : !isVideoDownloadCancel;
        if (!success) {
            // Fallback: file already on disk (observer race / STATUS lag).
            File file = new File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    title);
            success = file.exists() && file.length() > 0;
        }
        if (!success) {
            Toast.makeText(context, "Your video download is cancelled", Toast.LENGTH_LONG).show();
            return;
        }
        // Dialog + NotificationCompat ACTION_VIEW (tap opens the saved video).
        VideoDownloadConfirmHelper.showDownloaded(context, downloadId, title);
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
            if (simpleExoPlayerView.getResizeMode() != AspectRatioFrameLayout.RESIZE_MODE_FIT) {
                simpleExoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            }

        }else{
            layoutParentTitle.setVisibility(View.GONE);
            layoutParentReportDetails.setVisibility(View.GONE);
            layoutParentMiddle.setVisibility(View.GONE);
            LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
            layoutParentTop.setLayoutParams(params);
            if (simpleExoPlayerView.getResizeMode() != AspectRatioFrameLayout.RESIZE_MODE_FIT) {
                simpleExoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            }
        }
        fullscreen = !fullscreen;
    }

    public void shareYoutubeVideoScreenShot(View finalConvertView2, int position, LinearLayout container, int from, ImageView imageViewItems) {
        if (!tryAcquireShareLaunch()) {
            return;
        }
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
        half_youTubePlayerHolder.with_out_strip_layout.setVisibility(View.GONE);
        half_youTubePlayerHolder.with_strip_layout.setVisibility(View.VISIBLE);
        imageViewItems.setVisibility(View.VISIBLE);
        String mInvitationUrl = list.get(position).getShareLink();
        String msg = buildVideoArticleShareText(position);
        countDownTimer = new CountDownTimer(2, 2) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_SUBJECT, mInvitationUrl);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(Intent.EXTRA_TEXT, msg);
                Bitmap b = ScreenShott.getInstance().takeScreenShotOfView(half_youTubePlayerHolder.relative_half_content);
                try {
                    writeScreenshotPngAndAttach(intent, b);
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
                launchScreenshotShareIntent(intent, from,
                        () -> Share_Count_Value(
                                shareCountProperty(from == 1 || from == 2),
                                list.get(position).getPostId()));

            }
        }.start();
    }

    public void shareExoPlayerVideoScreenShot(View finalConvertView2, int position, int from, PlayerView simpleExoPlayerView, ImageView thaumbnail_for_video, ImageView imageViewExoVideoPlayVideo, ProgressBar progress_bar_for_video) {
        if (!tryAcquireShareLaunch()) {
            return;
        }
        halfVideoExoPlayerHolder.relative_half_content = finalConvertView2.findViewById(R.id.relative_half_contents);
        halfVideoExoPlayerHolder.with_strip_layout = finalConvertView2.findViewById(R.id.with_strips_layout);
        halfVideoExoPlayerHolder.with_out_strip_layout = finalConvertView2.findViewById(R.id.with_out_strips_layout);
        halfVideoExoPlayerHolder.layoutParentReportDetails = finalConvertView2.findViewById(R.id.ll_footer_layout);
        halfVideoExoPlayerHolder.layoutParentDate = finalConvertView2.findViewById(R.id.layoutParentDate);
        TextView textViewWebUrl=finalConvertView2.findViewById(R.id.textViewWebUrl);
        simpleExoPlayerView.setVisibility(View.GONE);
        textViewWebUrl.setVisibility(View.GONE);
        halfVideoExoPlayerHolder.layoutParentReportDetails.setVisibility(View.GONE);
        halfVideoExoPlayerHolder.layoutParentDate.setVisibility(VISIBLE);
        imageViewExoVideoPlayVideo.setVisibility(VISIBLE);
        thaumbnail_for_video.setVisibility(VISIBLE);
        halfVideoExoPlayerHolder.with_out_strip_layout.setVisibility(View.GONE);
        halfVideoExoPlayerHolder.with_strip_layout.setVisibility(View.VISIBLE);
        String mInvitationUrl = list.get(position).getShareLink();
        String msg = buildVideoArticleShareText(position);
        countDownTimer = new CountDownTimer(2, 2) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_SUBJECT, mInvitationUrl);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(Intent.EXTRA_TEXT, msg);
                Bitmap b = ScreenShott.getInstance().takeScreenShotOfView(halfVideoExoPlayerHolder.relative_half_content);
                try {
                    writeScreenshotPngAndAttach(intent, b);
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
                launchScreenshotShareIntent(intent, from,
                        () -> Share_Count_Value(
                                shareCountProperty(from == 1 || from == 2),
                                list.get(position).getPostId()));

            }
        }.start();


    }

}