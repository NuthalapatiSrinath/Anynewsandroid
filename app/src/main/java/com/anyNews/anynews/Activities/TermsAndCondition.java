package com.anyNews.anynews.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import com.anyNews.anynews.R;

public class TermsAndCondition extends AppCompatActivity {

    WebView webView;
    ImageView imgBack;
    int Indicator;
    TextView txtTitle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.background_color));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.background_color));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(true);
        } else {
            ViewCompat.setOnApplyWindowInsetsListener(getWindow().getDecorView(), (v, insets) -> {
                v.setPadding(0, insets.getSystemWindowInsetTop(), 0, insets.getSystemWindowInsetBottom());
                return insets;
            });
        }
        setContentView(R.layout.activity_terms_and_conditions);
        imgBack = findViewById(R.id.imgBack);


        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TermsAndCondition.this, Settings_Scrren.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        final ProgressDialog progressDialog = new ProgressDialog(TermsAndCondition.this);
        progressDialog.setMessage("Loading Data...");
        progressDialog.setCancelable(false);
        TermsAndCondition.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        webView = findViewById(R.id.webView);
        txtTitle = findViewById(R.id.txtTitle);
        webView.setWebViewClient(new TermsAndCondition.MyBrowser());

        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);


        String sessionId = getIntent().getStringExtra("EXTRA_SESSION_ID");
        if (sessionId.equals("1")) {
            txtTitle.setText("Terms and Conditions");
            webView.loadUrl("https://anynews.co.in/2019/04/terms-and-conditions.html");
        } else if (sessionId.equals("2")) {
            txtTitle.setText("Privacy Policy");
            webView.loadUrl("https://anynews.co.in/2019/04/privacy-policy.html");
        } else if (sessionId.equals("2")) {
            txtTitle.setText("Privacy Policy");
            webView.loadUrl("https://anynews.co.in/2019/04/terms-and-conditions.html");
        }


        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                if (progress < 100) {
                    progressDialog.show();
                }
                if (progress == 100) {
                    progressDialog.dismiss();
                }
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

}
