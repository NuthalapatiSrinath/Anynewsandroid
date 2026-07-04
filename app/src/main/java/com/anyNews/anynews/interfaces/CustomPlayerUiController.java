package com.anyNews.anynews.interfaces;

import static com.anyNews.anynews.Activities.HomePage.isPlayingVideoInFullScreen;
import static com.anyNews.anynews.R.drawable.ic__volume_up;
import static com.anyNews.anynews.R.drawable.ic_volume_off;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.anyNews.anynews.R;
import com.anyNews.anynews.Utils.AppHelper;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerFullScreenListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.ui.views.YouTubePlayerSeekBar;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.ui.views.YouTubePlayerSeekBarListener;

public class CustomPlayerUiController extends AbstractYouTubePlayerListener implements YouTubePlayerFullScreenListener {

    private final View playerUi;
    private final YouTubePlayerTracker playerTracker;
    int layoutHeight;
    ShowFullScreenVideo showFullScreenVideo;
    boolean isShowFullScreenOption = false;
    ImageButton playPauseButton;
    // private TextView videoCurrentTimeTextView;
    // private TextView videoDurationTextView;
    private Context context;
    private YouTubePlayer youTubePlayer;
    private YouTubePlayerView youTubePlayerView;
    // panel is used to intercept clicks on the WebView, I don't want the user to be able to click the WebView directly.
    private View panel;
    private View progressbar;
    private boolean fullscreen = false;
    private boolean isMutedHalf = false;
    private boolean isClicked = false;
    YouTubePlayerSeekBar youTubePlayerSeekBar;
    ImageButton enterExitFullscreenButton;
    ImageButton mute;
    public CustomPlayerUiController(Context context, View customPlayerUi, YouTubePlayer youTubePlayer, YouTubePlayerView youTubePlayerView, int layoutHeight, boolean isShowFullScreenOption,ShowFullScreenVideo showFullScreenVideo) {
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



    private void initViews(View playerUi) {
        panel = playerUi.findViewById(R.id.panel);
        progressbar = playerUi.findViewById(R.id.progressbar);
        //videoCurrentTimeTextView = playerUi.findViewById(R.id.video_current_time);
        //videoDurationTextView = playerUi.findViewById(R.id.video_duration);
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
                playPauseButton.setImageResource(R.drawable.ic_video_play);
                youTubePlayer.pause();
            } else {
                playPauseButton.setImageResource(R.drawable.ic_custom_video_pause);
                youTubePlayer.play();
            }

        });
        AppHelper.showLog("isShowFullScreenOption:"+isShowFullScreenOption);
        if (!isShowFullScreenOption) {
            AppHelper.showLog("isShowFullScreenOption::::"+isShowFullScreenOption);
            enterExitFullscreenButton.setVisibility(View.GONE);
        }

        enterExitFullscreenButton.setOnClickListener((view) -> {
            if (fullscreen) youTubePlayerView.exitFullScreen();
            else youTubePlayerView.enterFullScreen();
            fullscreen = !fullscreen;
        });
        panel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppHelper.showLog("onclick event"+isClicked);
                if (!isClicked) {
                    AppHelper.showLog("onclick event  !isClicked");
                    playPauseButton.setVisibility(View.GONE);
                    youTubePlayerSeekBar.setVisibility(View.GONE);
                    mute.setVisibility(View.GONE);
                    enterExitFullscreenButton.setVisibility(View.GONE);
                    if(!isShowFullScreenOption){
                        showFullScreenVideo.showFullScreenVideo(true);
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
                    if (!isShowFullScreenOption) {
                        showFullScreenVideo.showFullScreenVideo(false);
                        enterExitFullscreenButton.setVisibility(View.GONE);
                    }else{
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

    @Override
    public void onReady(@NonNull YouTubePlayer youTubePlayer) {
        AppHelper.showLog("onReady:");
        progressbar.setVisibility(View.GONE);
    }

    @Override
    public void onStateChange(@NonNull YouTubePlayer youTubePlayer, @NonNull PlayerConstants.PlayerState state) {
        AppHelper.showLog("onStateChange:" + state);
        if (state == PlayerConstants.PlayerState.PLAYING) {
            AppHelper.showLog("onStateChange: PLAYING");
            panel.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
            progressbar.setVisibility(View.GONE);
            playPauseButton.setImageResource(R.drawable.ic_custom_video_pause);

            AppHelper.showLog("onclick event  !isClicked");
            playPauseButton.setVisibility(View.GONE);
            youTubePlayerSeekBar.setVisibility(View.GONE);
            mute.setVisibility(View.GONE);
            enterExitFullscreenButton.setVisibility(View.GONE);
            if(!isShowFullScreenOption){
                showFullScreenVideo.showFullScreenVideo(true);
            }
            showFullScreenVideo.showUIControls(false);
            isClicked = true;

        } else if (state == PlayerConstants.PlayerState.PAUSED || state == PlayerConstants.PlayerState.VIDEO_CUED) {
            AppHelper.showLog("onStateChange: PAUSED|VIDEO_CUED");
            panel.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
            progressbar.setVisibility(View.GONE);
            playPauseButton.setImageResource(R.drawable.ic_video_play);
        } else {
            AppHelper.showLog("onStateChange: OTHER");
            if (state == PlayerConstants.PlayerState.BUFFERING)
                progressbar.setVisibility(View.VISIBLE);
            panel.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
        }

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onCurrentSecond(@NonNull YouTubePlayer youTubePlayer, float second) {
        //  videoCurrentTimeTextView.setText(second+"");
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onVideoDuration(@NonNull YouTubePlayer youTubePlayer, float duration) {
        // videoDurationTextView.setText(duration+"");
    }

    @Override
    public void onYouTubePlayerEnterFullScreen() {
        AppHelper.showLog("playerUi.getLayoutParams().height:true::" + playerUi.getLayoutParams().height);
        playerUi.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
        playerUi.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
        isPlayingVideoInFullScreen = true;
        showFullScreenVideo.showFullScreenVideo(true);


    }

    @Override
    public void onYouTubePlayerExitFullScreen() {
        AppHelper.showLog("playerUi.getLayoutParams().height:false::" + playerUi.getLayoutParams().height);
        playerUi.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
        playerUi.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
        isPlayingVideoInFullScreen = false;
        showFullScreenVideo.showFullScreenVideo(false);
    }

    public void showUiControlls() {
        isClicked = false;
        playPauseButton.setVisibility(View.VISIBLE);
        youTubePlayerSeekBar.setVisibility(View.VISIBLE);
        mute.setVisibility(View.VISIBLE);
        if (!isShowFullScreenOption) {
            showFullScreenVideo.showFullScreenVideo(false);
            enterExitFullscreenButton.setVisibility(View.GONE);
        }else{
            enterExitFullscreenButton.setVisibility(View.VISIBLE);
        }
    }

    public interface ShowFullScreenVideo {
        public void showFullScreenVideo(boolean b);
        public void showUIControls(boolean b);
    }

}
