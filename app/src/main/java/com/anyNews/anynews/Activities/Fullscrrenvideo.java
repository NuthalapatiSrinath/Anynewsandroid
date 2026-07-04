package com.anyNews.anynews.Activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.anyNews.anynews.R;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.TextView;

import com.anyNews.anynews.Utils.MediaUrl;
import com.anyNews.anynews.Utils.Logone;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.source.LoopingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoRendererEventListener;


        import android.os.Bundle;
        import android.util.Log;
        import android.view.Surface;
        import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.ExoPlaybackException;
        import com.google.android.exoplayer2.ExoPlayer;
        import com.google.android.exoplayer2.Format;
        import com.google.android.exoplayer2.PlaybackParameters;
        import com.google.android.exoplayer2.SimpleExoPlayer;
        import com.google.android.exoplayer2.Timeline;
        import com.google.android.exoplayer2.decoder.DecoderCounters;
        import com.google.android.exoplayer2.source.LoopingMediaSource;
        import com.google.android.exoplayer2.source.MediaSource;
        import com.google.android.exoplayer2.source.TrackGroupArray;
        import com.google.android.exoplayer2.source.hls.HlsMediaSource;
        import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
        import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
        import com.google.android.exoplayer2.trackselection.TrackSelection;
        import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
        import com.google.android.exoplayer2.trackselection.TrackSelector;
        import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
        import com.google.android.exoplayer2.ui.PlayerView;
        import com.google.android.exoplayer2.upstream.DataSource;
        import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
        import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
        import com.google.android.exoplayer2.util.Util;
        import com.google.android.exoplayer2.video.VideoRendererEventListener;




public class Fullscrrenvideo extends AppCompatActivity {


    private static final String TAG = "MainActivity";
    private PlayerView simpleExoPlayerView;
    private SimpleExoPlayer player;
    private TextView resolutionTextView;


    boolean value=true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscrrenvideo);

        String videoUrl = getIntent() != null ? getIntent().getStringExtra("video_url") : null;
        if (videoUrl == null || videoUrl.isEmpty()) {
            videoUrl = MediaUrl.OCI_BASE + "1587134017576/master.m3u8";
        } else {
            videoUrl = MediaUrl.resolve(videoUrl);
        }
        Uri mp4VideoUri = Uri.parse(videoUrl);

        int h = simpleExoPlayerView.getResources().getConfiguration().screenHeightDp;
        int w = simpleExoPlayerView.getResources().getConfiguration().screenWidthDp;
        Logone.e(TAG, "height : " + h +  " weight: " + w);
        ////Set media controller
        //SimpleExoPlayer player = new SimpleExoPlayer.Builder(getApplicationContext()).build();

        simpleExoPlayerView.setPlayer(player);
        simpleExoPlayerView.setUseController(false);
        simpleExoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);

        // Build the media item.


        // Prepare the player with the source.
        //player.prepare();
        player.setPlayWhenReady(true);
        player.setVolume(0);


     //   player.setVolume(0);
        simpleExoPlayerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                player.seekTo(0);
                player.setPlayWhenReady(true);


                player.retry();
           /*  //   player.getContentPosition();

                if(value){

                    Toast.makeText(Fullscrrenvideo.this, ""+player.getContentPosition()+" ,  "+player.getDuration(), Toast.LENGTH_SHORT).show();
                    player.setPlayWhenReady(false);
                    value=false;

                }else {

                    player.setPlayWhenReady(true);

                    value=true;
                }*/
            }
        });


    }



//-------------------------------------------------------ANDROID LIFECYCLE---------------------------------------------------------------------------------------------

    @Override
    protected void onStop() {
        super.onStop();
        Logone.v(TAG, "onStop()...");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Logone.v(TAG, "onStart()...");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Logone.v(TAG, "onResume()...");
    }

    @Override
    protected void onPause() {
        super.onPause();

        Logone.v(TAG, "onPause()...");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logone.v(TAG, "onDestroy()...");
        player.release();
    }
}