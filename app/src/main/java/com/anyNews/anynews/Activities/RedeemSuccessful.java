package com.anyNews.anynews.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.anyNews.anynews.BuildConfig;
import com.anyNews.anynews.R;

public class RedeemSuccessful extends AppCompatActivity {

    ImageView redeem_suceess_done;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_redeem_successful);
        redeem_suceess_done=findViewById(R.id.redeem_suceess_done);
        redeem_suceess_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(RedeemSuccessful.this,newProfileDetails.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                if (getApplicationContext() != null && getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)){
                    startActivity(intent);
                }
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
    //    super.onBackPressed();
        Intent intent=new Intent(RedeemSuccessful.this,newProfileDetails.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        if (getApplicationContext() != null && getApplicationContext().getPackageName().equals(BuildConfig.APPLICATION_ID)){
            startActivity(intent);
        }
        startActivity(intent);
        finish();
    }
}