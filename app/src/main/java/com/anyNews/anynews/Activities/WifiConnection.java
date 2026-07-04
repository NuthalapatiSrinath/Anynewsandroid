package com.anyNews.anynews.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.NetworkChangeReceiver;

public class WifiConnection extends AppCompatActivity {


    static TextView tv_check_connection,no_check_connection;
    static LinearLayout one,two;
    NetworkChangeReceiver mNetworkReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_connection);
        no_check_connection=findViewById(R.id.no_check_connection);
        tv_check_connection=(TextView) findViewById(R.id.tv_check_connection);

        one=findViewById(R.id.one);
        two=findViewById(R.id.two);

        mNetworkReceiver = new NetworkChangeReceiver();
        registerNetworkBroadcastForNougat();

    }


    public static void dialog(boolean value){

        if(value){
            one.setVisibility(View.VISIBLE);
            two.setVisibility(View.GONE);
            tv_check_connection.setText("AKKISETTY NARASIMHA");


            Handler handler = new Handler();
            Runnable delayrunnable = new Runnable() {
                @Override
                public void run() {
                    one.setVisibility(View.VISIBLE);
                }
            };
            handler.postDelayed(delayrunnable, 500);
        }else {
            one.setVisibility(View.GONE);
            two.setVisibility(View.VISIBLE);
            no_check_connection.setText(" NARASIMHA");
        }
    }


    private void registerNetworkBroadcastForNougat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            registerReceiver(mNetworkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            registerReceiver(mNetworkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }

    protected void unregisterNetworkChanges() {
        try {
            unregisterReceiver(mNetworkReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterNetworkChanges();
    }
}