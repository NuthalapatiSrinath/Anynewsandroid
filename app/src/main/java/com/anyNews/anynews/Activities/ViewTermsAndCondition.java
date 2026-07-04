package com.anyNews.anynews.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import com.anyNews.anynews.R;
import com.anyNews.anynews.Utils.Logone;

public class ViewTermsAndCondition extends AppCompatActivity {

    private static final String TAG = "ViewTermsAndCondition";
    WebView webView;
    ImageView imgBack;
    int Indicator;
    TextView txtTitle;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_terms_and_condition);
        imgBack = findViewById(R.id.imgBack);

        progressDialog = new ProgressDialog(ViewTermsAndCondition.this);
        progressDialog.setMessage("Loading Data...");
        progressDialog.setCancelable(false);
        ViewTermsAndCondition.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        webView = findViewById(R.id.webView);
        txtTitle = findViewById(R.id.txtTitle);
        webView.setWebViewClient(new ViewTermsAndCondition.MyBrowser());

        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        txtTitle.setText("Terms and Conditions");
        webView.loadUrl("https://anynews.co.in/2019/04/terms-and-conditions.html");



        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                if (progress < 100) {
                    progressDialog.show();
                }
                if (progress == 100 && progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }
        });

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private class MyBrowser extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

    private void closeDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        Logone.d(TAG, "called onDestroy");
        closeDialog();
        super.onDestroy();
    }


}
