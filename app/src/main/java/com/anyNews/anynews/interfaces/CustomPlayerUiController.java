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

import com.anyNews.anynews.Activities.HomePage;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Utils.AppHelper;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.customui.views.YouTubePlayerSeekBar;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.customui.views.YouTubePlayerSeekBarListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;
import com.pierfrancescosoffritti.youtubeplayer.player.YouTubePlayerFullScreenListener;

/**
 * YouTube chrome. Fullscreen is immersive over the <b>same</b> FlipView card —
 * no forced landscape, no Categories hop, no flip-to-page-0.
 */
public class CustomPlayerUiController extends AbstractYouTubePlayerListener implements YouTubePlayerFullScreenListener {

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
    private int savedSystemUiVisibility = -1;

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
        // Always offer fullscreen for half + full YouTube cards.
        if (enterExitFullscreenButton != null) {
            enterExitFullscreenButton.setVisibility(View.VISIBLE);
            enterExitFullscreenButton.setOnClickListener((view) -> {
                // Consume click so it cannot fall through to bottom-nav Special/Categories.
                view.setClickable(true);
                if (!fullscreen) {
                    enterImmersiveFullscreen();
                } else {
                    exitImmersiveFullscreen();
                }
            });
        }
        panel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppHelper.showLog("onclick event" + isClicked);
                if (!isClicked) {
                    playPauseButton.setVisibility(View.GONE);
                    youTubePlayerSeekBar.setVisibility(View.GONE);
                    mute.setVisibility(View.GONE);
                    if (enterExitFullscreenButton != null && !fullscreen) {
                        enterExitFullscreenButton.setVisibility(View.GONE);
                    }
                    showFullScreenVideo.showUIControls(false);
                    isClicked = true;
                } else {
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

    /**
     * Reparent the player to the activity content root — same orientation, same Flip
     * seat. Never calls {@code setRequestedOrientation} (no forced rotate / recreate hop).
     */
    private void enterImmersiveFullscreen() {
        if (fullscreen || youTubePlayerView == null) {
            return;
        }
        Activity activity = activityOf(context);
        if (activity == null) {
            return;
        }
        // Checkpoint FlipView seat before any layout thrash.
        if (activity instanceof HomePage) {
            try {
                ((HomePage) activity).checkpointFlipPositionForFullscreen();
            } catch (Exception ignored) {
            }
        }
        try {
            Object parent = youTubePlayerView.getParent();
            if (!(parent instanceof ViewGroup)) {
                return;
            }
            originalYtParent = (ViewGroup) parent;
            originalYtIndex = originalYtParent.indexOfChild(youTubePlayerView);
            originalYtLp = youTubePlayerView.getLayoutParams();
            originalYtParent.removeView(youTubePlayerView);

            ViewGroup content = activity.findViewById(android.R.id.content);
            if (content == null) {
                // Put back if content missing.
                originalYtParent.addView(youTubePlayerView, Math.max(0, originalYtIndex), originalYtLp);
                originalYtParent = null;
                return;
            }
            immersiveHost = new FrameLayout(activity);
            immersiveHost.setLayoutParams(new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            immersiveHost.setBackgroundColor(0xFF000000);
            immersiveHost.setClickable(true);
            immersiveHost.setFocusable(true);
            immersiveHost.addView(youTubePlayerView, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            content.addView(immersiveHost);

            applyImmersiveSystemUi(activity, true);
            fullscreen = true;
            isPlayingVideoInFullScreen = true;
            showFullScreenVideo.showFullScreenVideo(true);
            if (enterExitFullscreenButton != null) {
                enterExitFullscreenButton.setVisibility(View.VISIBLE);
                enterExitFullscreenButton.bringToFront();
            }
        } catch (Exception e) {
            AppHelper.showLog("enterImmersiveFullscreen: " + e.getMessage());
            exitImmersiveFullscreen();
        }
    }

    private void exitImmersiveFullscreen() {
        if (!fullscreen && immersiveHost == null) {
            isPlayingVideoInFullScreen = false;
            return;
        }
        Activity activity = activityOf(context);
        try {
            if (immersiveHost != null) {
                immersiveHost.removeView(youTubePlayerView);
                ViewGroup content = activity != null
                        ? (ViewGroup) activity.findViewById(android.R.id.content) : null;
                if (content != null) {
                    content.removeView(immersiveHost);
                }
                immersiveHost = null;
            }
            if (originalYtParent != null && youTubePlayerView != null) {
                if (youTubePlayerView.getParent() instanceof ViewGroup) {
                    ((ViewGroup) youTubePlayerView.getParent()).removeView(youTubePlayerView);
                }
                int idx = originalYtIndex >= 0
                        ? Math.min(originalYtIndex, originalYtParent.getChildCount())
                        : originalYtParent.getChildCount();
                originalYtParent.addView(youTubePlayerView, idx,
                        originalYtLp != null ? originalYtLp
                                : new ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT));
            }
        } catch (Exception e) {
            AppHelper.showLog("exitImmersiveFullscreen: " + e.getMessage());
        }
        originalYtParent = null;
        originalYtIndex = -1;
        originalYtLp = null;
        if (activity != null) {
            applyImmersiveSystemUi(activity, false);
            // Ensure we did not leave a landscape lock from older builds.
            try {
                int cur = activity.getRequestedOrientation();
                if (cur == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                        || cur == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                        || cur == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                        || cur == ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE) {
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
            } catch (Exception ignored) {
            }
            if (activity instanceof HomePage) {
                try {
                    ((HomePage) activity).restoreFlipPositionAfterFullscreen();
                } catch (Exception ignored) {
                }
            }
        }
        fullscreen = false;
        isPlayingVideoInFullScreen = false;
        showFullScreenVideo.showFullScreenVideo(false);
    }

    @SuppressLint("ObsoleteSdkInt")
    private void applyImmersiveSystemUi(Activity activity, boolean enter) {
        try {
            View decor = activity.getWindow().getDecorView();
            if (enter) {
                savedSystemUiVisibility = decor.getSystemUiVisibility();
                activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                decor.setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
            } else {
                activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                if (savedSystemUiVisibility >= 0) {
                    decor.setSystemUiVisibility(savedSystemUiVisibility);
                } else {
                    decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                }
                savedSystemUiVisibility = -1;
            }
        } catch (Exception ignored) {
        }
    }

    private static Activity activityOf(Context context) {
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
            panel.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
            progressbar.setVisibility(View.GONE);
            showFullScreenVideo.showProgressOnMainPage(false);
            playPauseButton.setImageResource(R.drawable.ic_custom_video_pause);
            playPauseButton.setVisibility(View.GONE);
            youTubePlayerSeekBar.setVisibility(View.GONE);
            mute.setVisibility(View.GONE);
            // Keep fullscreen affordance reachable while playing (do not hide it / do not auto-FS).
            if (enterExitFullscreenButton != null && !fullscreen) {
                enterExitFullscreenButton.setVisibility(View.VISIBLE);
            }
            showFullScreenVideo.showUIControls(false);
            isClicked = true;
        } else if (state == PlayerConstants.PlayerState.PAUSED || state == PlayerConstants.PlayerState.VIDEO_CUED) {
            panel.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
            progressbar.setVisibility(View.GONE);
            showFullScreenVideo.showProgressOnMainPage(false);
            // Match Play Store / fat: show play when paused/idle (never leave pause glyph stuck).
            playPauseButton.setImageResource(R.drawable.ic_way2_play_circle);
            playPauseButton.setVisibility(View.VISIBLE);
            isClicked = false;
        } else {
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

    @Override
    public void onYouTubePlayerEnterFullScreen() {
        enterImmersiveFullscreen();
    }

    @Override
    public void onYouTubePlayerExitFullScreen() {
        exitImmersiveFullscreen();
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
        if (fullscreen) {
            exitImmersiveFullscreen();
        }
    }

    public interface ShowFullScreenVideo {
        void showFullScreenVideo(boolean b);
        void showUIControls(boolean b);
        void showProgressOnMainPage(boolean b);
    }
}
