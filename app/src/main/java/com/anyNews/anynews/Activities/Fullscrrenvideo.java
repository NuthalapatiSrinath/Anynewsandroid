package com.anyNews.anynews.Activities;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerView;

import com.anyNews.anynews.R;
import com.anyNews.anynews.Utils.Logone;
import com.anyNews.anynews.Utils.MediaUrl;

public class Fullscrrenvideo extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private PlayerView simpleExoPlayerView;
    private ExoPlayer player;
    boolean value = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscrrenvideo);
        simpleExoPlayerView = findViewById(R.id.player_view);

        String videoUrl = getIntent() != null ? getIntent().getStringExtra("video_url") : null;
        if (videoUrl == null || videoUrl.isEmpty()) {
            videoUrl = MediaUrl.OCI_BASE + "1587134017576/master.m3u8";
        } else {
            videoUrl = MediaUrl.resolve(videoUrl);
        }

        int h = simpleExoPlayerView.getResources().getConfiguration().screenHeightDp;
        int w = simpleExoPlayerView.getResources().getConfiguration().screenWidthDp;
        Logone.e(TAG, "height : " + h + " weight: " + w);

        player = new ExoPlayer.Builder(getApplicationContext()).build();
        simpleExoPlayerView.setPlayer(player);
        simpleExoPlayerView.setUseController(false);
        simpleExoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
        player.setMediaItem(MediaItem.fromUri(Uri.parse(videoUrl)));
        player.prepare();
        player.setPlayWhenReady(true);
        player.setVolume(0);

        simpleExoPlayerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                player.seekTo(0);
                player.setPlayWhenReady(true);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logone.v(TAG, "onDestroy()...");
        if (player != null) {
            player.release();
            player = null;
        }
    }
}
