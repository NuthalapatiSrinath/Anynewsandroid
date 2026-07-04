package com.anyNews.anynews.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.NetworkChangeReceiver;
import com.anyNews.anynews.Utils.Logone;

public class Wifireconnection extends AppCompatActivity {


    private static final String LOG_TAG = "CheckNetworkStatus";
    private NetworkChangeReceiver receiver;
    private boolean isConnected = false;
    private TextView networkStatus;

    LinearLayout one,two;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifireconnection);
        one=findViewById(R.id.one);
        two=findViewById(R.id.two);

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        receiver = new NetworkChangeReceiver();
        registerReceiver(receiver, filter);

        networkStatus = (TextView) findViewById(R.id.networkStatus);
    }


    @Override
    protected void onDestroy() {
        Logone.v(LOG_TAG, "onDestory");
        super.onDestroy();

        unregisterReceiver(receiver);

    }


    public class NetworkChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, final Intent intent) {

            Logone.v(LOG_TAG, "Receieved notification about network status");
            isNetworkAvailable(context);

        }


        private boolean isNetworkAvailable(Context context) {
            ConnectivityManager connectivity = (ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity != null) {
                NetworkInfo[] info = connectivity.getAllNetworkInfo();
                if (info != null) {
                    for (int i = 0; i < info.length; i++) {
                        if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                            if (!isConnected) {
                                Logone.v(LOG_TAG, "Now you are connected to Internet!");
                            //    networkStatus.setText("Now you are connected to Internet!");
                                one.setVisibility(View.GONE);
                                two.setVisibility(View.VISIBLE);
                                isConnected = true;
                                //do your processing here ---
                                //if you need to post any data to the server or get status
                                //update from the server
                            }
                            return true;
                        }
                    }
                }
            }
            Logone.v(LOG_TAG, "You are not connected to Internet!");
            one.setVisibility(View.VISIBLE);
            two.setVisibility(View.GONE);
         //   one.setText("You are not connected to Internet!");
            isConnected = false;
            return false;
        }
    }
}