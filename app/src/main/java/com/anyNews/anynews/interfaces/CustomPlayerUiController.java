package com.anyNews.anynews.interfaces;

import static com.anyNews.anynews.Activities.HomePage.isPlayingVideoInFullScreen;
import static com.anyNews.anynews.R.drawable.ic__volume_up;
import static com.anyNews.anynews.R.drawable.ic_volume_off;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.anyNews.anynews.R;
import com.anyNews.anynews.Utils.AppHelper;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.customui.views.YouTubePlayerSeekBar;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.customui.views.YouTubePlayerSeekBarListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.FullscreenListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;

/**
 * Custom YouTube chrome. Fullscreen must open real immersive playback — never
 * bottom-nav Special ({@code navigation_catagiries} / Categories).
 */
public class CustomPlayerUiController extends AbstractYouTubePlayerListener {

    private final View playerUi;
    private final YouTubePlayerTracker playerTracker;
    int layoutHeight;
    ShowFullScreenVideo showFullScreenVideo;
    boolean isShowFullScreenOption = false;
    ImageButton playPauseButton;
    private Context context;
    private YouTubePlayer youTubePlayer;
    private YouTubePlayerView youTubePlayerView;
    private View panel;
    private View progressbar;
    private boolean fullscreen = false;
    private boolean isMutedHalf = false;
    private boolean isClicked = false;
    YouTubePlayerSeekBar youTubePlayerSeekBar;
    ImageButton enterExitFullscreenButton;
    ImageButton mute;

    private ViewGroup originalYtParent;
    private int originalYtIndex = -1;
    private ViewGroup.LayoutParams originalYtLp;
    private FrameLayout immersiveHost;
    private Function0<Unit> exitFullscreenCallback;
    private int savedSystemUiVisibility = -1;
    private int savedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;

    public CustomPlayerUiController(Context context, View customPlayerUi, YouTubePlayer youTubePlayer, YouTubePlayerView youTubePlayerView, int layoutHeight, boolean isShowFullScreenOption, ShowFullScreenVideo showFullScreenVideo) {
        this.playerUi = customPlayerUi;
        this.context = context;
        this.youTubePlayer = youTubePlayer;
        this.youTubePlayerView = youTubePlayerView;
        playerTracker = new YouTubePlayerTracker();
        youTubePlayer.addListener(playerTracker);
        this.layoutHeight = layoutHeight;
        this.isShowFullScreenOption = isShowFullScreenOption;
        this.showFullScreenVideo = showFullScreenVideo;
        initViews(customPlayerUi);
        wireLibraryFullscreenListener();
    }

    private Runnable onUserPausePlayback;
    private Runnable onUserResumePlayback;

    public void setUserPlaybackCallbacks(Runnable onUserPause, Runnable onUserResume) {
        this.onUserPausePlayback = onUserPause;
        this.onUserResumePlayback = onUserResume;
    }

    private void initViews(View playerUi) {
        panel = playerUi.findViewById(R.id.panel);
        progressbar = playerUi.findViewById(R.id.progressbar);
        playPauseButton = playerUi.findViewById(R.id.play_pause_button);
        enterExitFullscreenButton = playerUi.findViewById(R.id.enter_exit_fullscreen_button);
        mute = playerUi.findViewById(R.id.mute);
        youTubePlayerSeekBar = playerUi.findViewById(R.id.youtube_player_seekbar);
        youTubePlayer.addListener(youTubePlayerSeekBar);
        youTubePlayerSeekBar.setYoutubePlayerSeekBarListener(new YouTubePlayerSeekBarListener() {
            @Override
            public void seekTo(float v) {
                youTubePlayer.seekTo(v);
            }
        });
        playPauseButton.setVisibility(View.GONE);
        playPauseButton.setOnClickListener((view) -> {
            if (playerTracker.getState() == PlayerConstants.PlayerState.PLAYING) {
                playPauseButton.setImageResource(R.drawable.ic_way2_play_circle);
                youTubePlayer.pause();
                if (onUserPausePlayback != null) {
                    onUserPausePlayback.run();
                }
            } else {
                playPauseButton.setImageResource(R.drawable.ic_custom_video_pause);
                youTubePlayer.play();
                if (onUserResumePlayback != null) {
                    onUserResumePlayback.run();
                }
            }
        });
        AppHelper.showLog("isShowFullScreenOption:" + isShowFullScreenOption);
        // Always offer fullscreen for half + full YouTube cards.
        if (enterExitFullscreenButton != null) {
            enterExitFullscreenButton.setVisibility(View.VISIBLE);
            enterExitFullscreenButton.setOnClickListener((view) -> {
                // Consume click so it cannot fall through to bottom-nav Special
                // (legacy id navigation_catagiries / "Categories").
                view.setClickable(true);
                if (!fullscreen) {
                    enterImmersiveFullscreen();
                } else {
                    exitImmersiveFullscreen();
                }
            });
        }
        if (!isShowFullScreenOption && enterExitFullscreenButton != null) {
            // Full-bleed full-video cards still get a working fullscreen control.
            enterExitFullscreenButton.setVisibility(View.VISIBLE);
        }
        panel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppHelper.showLog("onclick event" + isClicked);
                if (!isClicked) {
                    AppHelper.showLog("onclick event  !isClicked");
                    playPauseButton.setVisibility(View.GONE);
                    youTubePlayerSeekBar.setVisibility(View.GONE);
                    mute.setVisibility(View.GONE);
                    if (enterExitFullscreenButton != null) {
                        enterExitFullscreenButton.setVisibility(View.GONE);
                    }
                    showFullScreenVideo.showUIControls(false);
                    isClicked = true;
                } else {
                    AppHelper.showLog("onclick event  isClicked");
                    isClicked = false;
                    playPauseButton.setVisibility(View.VISIBLE);
                    youTubePlayerSeekBar.setVisibility(View.VISIBLE);
                    showFullScreenVideo.showUIControls(true);
                    mute.setVisibility(View.VISIBLE);
                    if (enterExitFullscreenButton != null) {
                        enterExitFullscreenButton.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
        mute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isMutedHalf) {
                    mute.setImageResource(ic_volume_off);
                    youTubePlayer.mute();
                    isMutedHalf = true;
                } else {
                    mute.setImageResource(ic__volume_up);
                    youTubePlayer.unMute();
                    isMutedHalf = false;
                }
            }
        });
    }

    private void wireLibraryFullscreenListener() {
        if (youTubePlayerView == null) {
            return;
        }
        try {
            youTubePlayerView.addFullscreenListener(new FullscreenListener() {
                @Override
                public void onEnterFullscreen(@NonNull View fullscreenView,
                                              @NonNull Function0<Unit> exitFullscreen) {
                    exitFullscreenCallback = exitFullscreen;
                    attachImmersiveView(fullscreenView);
                    fullscreen = true;
                    isPlayingVideoInFullScreen = true;
                    if (showFullScreenVideo != null) {
                        showFullScreenVideo.showFullScreenVideo(true);
                    }
                }

                @Override
                public void onExitFullscreen() {
                    detachImmersiveView();
                    exitFullscreenCallback = null;
                    fullscreen = false;
                    isPlayingVideoInFullScreen = false;
                    if (showFullScreenVideo != null) {
                        showFullScreenVideo.showFullScreenVideo(false);
                    }
                }
            });
        } catch (Exception e) {
            AppHelper.showLog("addFullscreenListener failed: " + e.getMessage());
        }
    }

    private void enterImmersiveFullscreen() {
        if (fullscreen || youTubePlayerView == null) {
            return;
        }
        try {
            youTubePlayerView.matchParent();
        } catch (Exception ignored) {
        }
        attachImmersiveView(youTubePlayerView);
        fullscreen = true;
        isPlayingVideoInFullScreen = true;
        if (showFullScreenVideo != null) {
            showFullScreenVideo.showFullScreenVideo(true);
        }
        if (enterExitFullscreenButton != null) {
            enterExitFullscreenButton.setImageResource(R.drawable.ic_fullscreen_24dp);
        }
    }

    private void exitImmersiveFullscreen() {
        if (!fullscreen) {
            return;
        }
        if (exitFullscreenCallback != null) {
            try {
                exitFullscreenCallback.invoke();
            } catch (Exception ignored) {
            }
            exitFullscreenCallback = null;
        }
        detachImmersiveView();
        try {
            if (youTubePlayerView != null) {
                youTubePlayerView.wrapContent();
            }
        } catch (Exception ignored) {
        }
        fullscreen = false;
        isPlayingVideoInFullScreen = false;
        if (showFullScreenVideo != null) {
            showFullScreenVideo.showFullScreenVideo(false);
        }
    }

    /** Public for HomePage back-press exit. */
    public void onYouTubePlayerExitFullScreen() {
        exitImmersiveFullscreen();
    }

    public void onYouTubePlayerEnterFullScreen() {
        enterImmersiveFullscreen();
    }

    private void attachImmersiveView(@NonNull View target) {
        Activity activity = activityFrom(context);
        if (activity == null) {
            return;
        }
        ViewGroup decor = (ViewGroup) activity.getWindow().getDecorView();
        ViewGroup content = decor.findViewById(android.R.id.content);
        if (content == null) {
            return;
        }
        // Remember original parent so we can restore the YouTubePlayerView.
        if (target == youTubePlayerView && originalYtParent == null
                && youTubePlayerView.getParent() instanceof ViewGroup) {
            originalYtParent = (ViewGroup) youTubePlayerView.getParent();
            originalYtIndex = originalYtParent.indexOfChild(youTubePlayerView);
            originalYtLp = youTubePlayerView.getLayoutParams();
        }
        if (immersiveHost == null) {
            immersiveHost = new FrameLayout(activity);
            immersiveHost.setLayoutParams(new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            immersiveHost.setBackgroundColor(0xFF000000);
            immersiveHost.setClickable(true);
            immersiveHost.setFocusable(true);
            // Block touches from reaching bottom-nav Special / Categories underneath.
            immersiveHost.setOnClickListener(v -> { /* consume */ });
        }
        if (target.getParent() instanceof ViewGroup) {
            ((ViewGroup) target.getParent()).removeView(target);
        }
        immersiveHost.removeAllViews();
        immersiveHost.addView(target, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        if (immersiveHost.getParent() == null) {
            content.addView(immersiveHost);
        }
        hideHomeChrome(activity, true);
        try {
            savedOrientation = activity.getRequestedOrientation();
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        } catch (Exception ignored) {
        }
        try {
            savedSystemUiVisibility = activity.getWindow().getDecorView().getSystemUiVisibility();
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            activity.getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        } catch (Exception ignored) {
        }
    }

    private void detachImmersiveView() {
        Activity activity = activityFrom(context);
        if (immersiveHost != null && immersiveHost.getParent() instanceof ViewGroup) {
            ((ViewGroup) immersiveHost.getParent()).removeView(immersiveHost);
        }
        if (youTubePlayerView != null && originalYtParent != null) {
            if (youTubePlayerView.getParent() instanceof ViewGroup) {
                ((ViewGroup) youTubePlayerView.getParent()).removeView(youTubePlayerView);
            }
            ViewGroup.LayoutParams lp = originalYtLp != null
                    ? originalYtLp
                    : new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            int index = originalYtIndex >= 0 ? originalYtIndex : -1;
            if (index >= 0 && index <= originalYtParent.getChildCount()) {
                originalYtParent.addView(youTubePlayerView, index, lp);
            } else {
                originalYtParent.addView(youTubePlayerView, lp);
            }
        }
        originalYtParent = null;
        originalYtIndex = -1;
        originalYtLp = null;
        if (activity != null) {
            hideHomeChrome(activity, false);
            try {
                if (savedOrientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
                    activity.setRequestedOrientation(savedOrientation);
                } else {
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
            } catch (Exception ignored) {
            }
            try {
                activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                if (savedSystemUiVisibility >= 0) {
                    activity.getWindow().getDecorView()
                            .setSystemUiVisibility(savedSystemUiVisibility);
                }
            } catch (Exception ignored) {
            }
        }
        savedSystemUiVisibility = -1;
        immersiveHost = null;
    }

    private static void hideHomeChrome(@NonNull Activity activity, boolean hide) {
        try {
            View nav = activity.findViewById(R.id.nav_view);
            if (nav != null) {
                nav.setVisibility(hide ? View.GONE : View.VISIBLE);
            }
            View fab = activity.findViewById(R.id.fab);
            if (fab != null) {
                fab.setVisibility(hide ? View.GONE : View.VISIBLE);
            }
        } catch (Exception ignored) {
        }
    }

    private static Activity activityFrom(Context context) {
        if (context instanceof Activity) {
            return (Activity) context;
        }
        if (context instanceof android.content.ContextWrapper) {
            Context base = ((android.content.ContextWrapper) context).getBaseContext();
            if (base instanceof Activity) {
                return (Activity) base;
            }
        }
        return null;
    }

    @Override
    public void onReady(@NonNull YouTubePlayer youTubePlayer) {
        AppHelper.showLog("onReady:");
        showFullScreenVideo.showProgressOnMainPage(false);
        progressbar.setVisibility(View.GONE);
    }

    @Override
    public void onStateChange(@NonNull YouTubePlayer youTubePlayer, @NonNull PlayerConstants.PlayerState state) {
        showFullScreenVideo.showProgressOnMainPage(true);
        AppHelper.showLog("onStateChange:" + state);
        if (state == PlayerConstants.PlayerState.PLAYING) {
            AppHelper.showLog("onStateChange: PLAYING");
            panel.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
            progressbar.setVisibility(View.GONE);
            showFullScreenVideo.showProgressOnMainPage(false);
            playPauseButton.setImageResource(R.drawable.ic_custom_video_pause);
            playPauseButton.setVisibility(View.GONE);
            youTubePlayerSeekBar.setVisibility(View.GONE);
            mute.setVisibility(View.GONE);
            // Keep fullscreen affordance reachable while playing (do not hide it).
            if (enterExitFullscreenButton != null && !fullscreen) {
                enterExitFullscreenButton.setVisibility(View.VISIBLE);
            }
            showFullScreenVideo.showUIControls(false);
            isClicked = true;
        } else if (state == PlayerConstants.PlayerState.PAUSED || state == PlayerConstants.PlayerState.VIDEO_CUED) {
            AppHelper.showLog("onStateChange: PAUSED|VIDEO_CUED");
            panel.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
            progressbar.setVisibility(View.GONE);
            showFullScreenVideo.showProgressOnMainPage(false);
            playPauseButton.setImageResource(R.drawable.ic_way2_play_circle);
        } else {
            AppHelper.showLog("onStateChange: OTHER $$");
            if (state == PlayerConstants.PlayerState.BUFFERING) {
                progressbar.setVisibility(View.VISIBLE);
            }
            showFullScreenVideo.showProgressOnMainPage(false);
            panel.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onCurrentSecond(@NonNull YouTubePlayer youTubePlayer, float second) {
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onVideoDuration(@NonNull YouTubePlayer youTubePlayer, float duration) {
    }

    public void showUiControlls() {
        isClicked = false;
        playPauseButton.setVisibility(View.VISIBLE);
        youTubePlayerSeekBar.setVisibility(View.VISIBLE);
        youTubePlayer.seekTo(0);
        mute.setVisibility(View.VISIBLE);
        if (enterExitFullscreenButton != null) {
            enterExitFullscreenButton.setVisibility(View.VISIBLE);
        }
    }

    public interface ShowFullScreenVideo {
        void showFullScreenVideo(boolean b);
        void showUIControls(boolean b);
        void showProgressOnMainPage(boolean b);
    }
}
