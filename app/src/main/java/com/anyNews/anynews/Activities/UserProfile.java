package com.anyNews.anynews.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.anyNews.anynews.R;

public class UserProfile extends AppCompatActivity {

    LinearLayout next_payment,total_earning;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
       /* next_payment=findViewById(R.id.next_payment);
        total_earning=findViewById(R.id.total_earning);
        total_earning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(UserProfile.this,PaymentHistory.class);
                startActivity(intent);
            }
        });
        next_payment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(UserProfile.this,RedeemPageActivity.class);
                startActivity(intent);
            }
        });*/
    }
}