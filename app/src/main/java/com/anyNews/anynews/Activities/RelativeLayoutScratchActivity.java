package com.anyNews.anynews.Activities;

import static com.anyNews.anynews.Utils.AppHelper.showLog;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;

import com.anyNews.anynews.R;
import com.goibibo.libs.views.ScratchRelativeLayoutView;

public class RelativeLayoutScratchActivity extends AppCompatActivity {
    ScratchRelativeLayoutView scratchRelativeLayoutView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE));
        setContentView(R.layout.activity_relative_layout_scratch);

        scratchRelativeLayoutView = findViewById(R.id.scratch_card);
        scratchRelativeLayoutView.setStrokeWidth(20);



        scratchRelativeLayoutView.setScratchView(R.layout.lyt_scratch);


        scratchRelativeLayoutView.setRevealListener(new ScratchRelativeLayoutView.IRevealListener() {
            @Override
            public void onRevealed(ScratchRelativeLayoutView tv) {
                // on reveal
            }

            @Override
            public void onRevealPercentChangedListener(ScratchRelativeLayoutView siv, float percent) {

                showLog("REVEAL"+percent);

                  if(percent>0.3){

                      scratchRelativeLayoutView.reveal();

                  }

            }
        });
    }

}