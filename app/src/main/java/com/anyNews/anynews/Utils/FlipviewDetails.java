package com.anyNews.anynews.Utils;

import static com.anyNews.anynews.Utils.AppHelper.showLog;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.anyNews.anynews.R;
import com.google.android.exoplayer2.util.Util;

import java.util.ArrayList;

public class FlipviewDetails extends AppCompatActivity {
    private FlipViewControllerlatest flipViewControllerlatest;
    private FlipperAdapter adapter;
    ArrayList<ModelClass.Datum> list;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flip_view_controller);
        flipViewControllerlatest = (FlipViewControllerlatest)findViewById(R.id.flip_view);
        list=new ArrayList<>();

    }


    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT < 24) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT >= 24) {
            releasePlayer();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Appconst.pos=0;
        Appconst.swiped=false;
        showLog("ondestory");
        if (Util.SDK_INT >= 24) {
            releasePlayer();
        }
    }

    private void releasePlayer() {
        if (Appconst.simpleExoPlayer != null) {
            Appconst.simpleExoPlayer.release();
            Appconst.simpleExoPlayer = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();


    }
}