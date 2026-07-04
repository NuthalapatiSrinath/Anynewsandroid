package com.anyNews.anynews.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.anyNews.anynews.R;
import com.goibibo.libs.views.ScratchRelativeLayoutView;

import static com.anyNews.anynews.App.getContext;

public class DailogBox extends AppCompatActivity {
    ScratchRelativeLayoutView scratchRelativeLayoutView;
    ImageView back_button;
    Button button;
    ViewGroup viewGroup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dailog_box);

        button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewGroup = findViewById(android.R.id.content);
                 AlertDialog.Builder builder = new AlertDialog.Builder(DailogBox.this);
                View dialogView = LayoutInflater.from(DailogBox.this).inflate(R.layout.custom_dialog, viewGroup, false);
                builder.setView(dialogView);
                final AlertDialog alertDialog = builder.create();
                alertDialog.show();
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                scratchRelativeLayoutView = dialogView.findViewById(R.id.scratch_card);
                back_button=dialogView.findViewById(R.id.back_button);
                scratchRelativeLayoutView.setStrokeWidth(10);



                scratchRelativeLayoutView.setScratchView(R.layout.lyt_scratch);

                back_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.dismiss();
                    }
                });

                scratchRelativeLayoutView.setRevealListener(new ScratchRelativeLayoutView.IRevealListener() {
                    @Override
                    public void onRevealed(ScratchRelativeLayoutView tv) {
                        // on reveal
                    }

                    @Override
                    public void onRevealPercentChangedListener(ScratchRelativeLayoutView siv, float percent) {

                        if(percent>0.3){

                            scratchRelativeLayoutView.reveal();

                        }

                    }
                });
                alertDialog.show();
            }
        });

    }
}