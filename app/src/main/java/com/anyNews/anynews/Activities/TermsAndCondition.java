package com.anyNews.anynews.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.Config;

/**
 * In-app browser for Settings legal pages.
 * EXTRA_SESSION_ID: "1" Terms, "2" Privacy (legacy SettingAdopterData).
 */
public class TermsAndCondition extends AppCompatActivity {

    WebView webView;
    ImageView imgBack;
    TextView txtTitle;
    ProgressDialog progressDialog;
    private String currentUrl;
    private boolean contentBoosted;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.background_color));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.background_color));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(true);
        } else {
            ViewCompat.setOnApplyWindowInsetsListener(
                    getWindow().getDecorView(),
                    (v, insets) -> {
                        v.setPadding(
                                0,
                                insets.getSystemWindowInsetTop(),
                                0,
                                insets.getSystemWindowInsetBottom());
                        return insets;
                    });
        }
        setContentView(R.layout.activity_terms_and_conditions);
        imgBack = findViewById(R.id.imgBack);

        imgBack.setOnClickListener(
                view -> {
                    if (isTaskRoot()) {
                        Intent intent =
                                new Intent(TermsAndCondition.this, Settings_Scrren.class);
                        intent.addFlags(
                                Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                    }
                    finish();
                });

        progressDialog = new ProgressDialog(TermsAndCondition.this);
        progressDialog.setMessage("Loading Data...");
        progressDialog.setCancelable(true);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        webView = findViewById(R.id.webView);
        txtTitle = findViewById(R.id.txtTitle);
        webView.setBackgroundColor(Color.WHITE);

        WebSettings settings = webView.getSettings();
        settings.setLoadsImagesAutomatically(true);
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        }
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webView.setWebViewClient(new LegalBrowser());

        String sessionId = getIntent().getStringExtra("EXTRA_SESSION_ID");
        if (sessionId == null) sessionId = "1";
        if ("1".equals(sessionId)) {
            txtTitle.setText("Terms and Conditions");
            currentUrl = Config.LINK_TERMS_AND_CONDITIONS;
        } else if ("2".equals(sessionId)) {
            txtTitle.setText("Privacy Policy");
            currentUrl = Config.LINK_PRIVACY_POLICY;
        } else {
            // Legacy never had About Us; map any other id to Privacy rather than blank host.
            txtTitle.setText("Privacy Policy");
            currentUrl = Config.LINK_PRIVACY_POLICY;
        }
        webView.loadUrl(currentUrl);

        webView.setWebChromeClient(
                new WebChromeClient() {
                    public void onProgressChanged(WebView view, int progress) {
                        if (isFinishing()) return;
                        try {
                            if (progress < 100) {
                                if (progressDialog != null && !progressDialog.isShowing()) {
                                    progressDialog.show();
                                }
                            } else {
                                dismissProgress();
                            }
                        } catch (Exception ignored) {
                        }
                    }
                });
    }

    private void dismissProgress() {
        try {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        } catch (Exception ignored) {
        }
    }

    private void showLoadError() {
        dismissProgress();
        if (isFinishing()) return;
        Toast.makeText(this, R.string.settings_webview_error, Toast.LENGTH_LONG).show();
        String title =
                txtTitle != null && txtTitle.getText() != null
                        ? txtTitle.getText().toString()
                        : "AnyNews";
        String html =
                "<!DOCTYPE html><html><head><meta name='viewport' content='width=device-width, initial-scale=1'/>"
                        + "<style>body{font-family:sans-serif;padding:16px;color:#222;background:#fff;line-height:1.5}"
                        + "a{color:#E65C00}</style></head><body>"
                        + "<h2>"
                        + escapeHtml(title)
                        + "</h2>"
                        + "<p>Unable to display this page offline. Open in browser:</p>"
                        + "<p><a href='"
                        + escapeHtml(currentUrl)
                        + "'>"
                        + escapeHtml(currentUrl)
                        + "</a></p>"
                        + "</body></html>";
        webView.loadDataWithBaseURL(currentUrl, html, "text/html", "UTF-8", null);
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    /**
     * After a full page load, strip decorative chrome so Terms/Privacy body is visible
     * (theme CSS often leaves white/layout-broken shells in WebView).
     */
    private void boostReadableContent(WebView view) {
        if (contentBoosted || view == null) return;
        contentBoosted = true;
        String js =
                "(function(){"
                        + "try{"
                        + "var s=document.createElement('style');"
                        + "s.innerHTML='html,body{background:#ffffff!important;color:#222222!important;}"
                        + ".post-body,.entry-content,.post-upper,.main-wrapper,.content-wrapper,"
                        + "#Blog1,.blog-posts,.post,.post-body *{display:block!important;"
                        + "visibility:visible!important;opacity:1!important;color:#222!important;"
                        + "max-width:100%!important;width:auto!important;float:none!important;}"
                        + ".post-body p,.post-body li,.post-body span{display:inline!important;}"
                        + ".post-body p,.post-body li{display:block!important;}"
                        + "#header,.header-wrap,.sidebar-wrapper,.footer,#whatsapp,"
                        + "nav,.navbar,.outerpic-wrapper img#Header1_headerimg{display:none!important;}'"
                        + ";document.head.appendChild(s);"
                        + "var b=document.querySelector('.post-body')||document.querySelector('.entry-content')"
                        + "||document.querySelector('[itemprop=articleBody]');"
                        + "var t=document.querySelector('.post-upper .title')||document.querySelector('h1')"
                        + "||document.querySelector('title');"
                        + "if(b&&b.innerText&&b.innerText.trim().length>40){"
                        + "var h=t&&t.innerText?('<h1 style=\"font-size:20px;margin:0 0 12px;color:#111\">'"
                        + "+t.innerText+'</h1>'):'';"
                        + "document.body.innerHTML='<div style=\"padding:16px;font-size:15px;line-height:1.55;"
                        + "color:#222;background:#fff;max-width:100%;overflow-wrap:break-word\">'+h+b.innerHTML"
                        + "+'</div>';"
                        + "document.body.style.background='#fff';"
                        + "}"
                        + "}catch(e){}"
                        + "})();";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            view.evaluateJavascript(js, null);
        } else {
            view.loadUrl("javascript:" + js);
        }
    }

    private class LegalBrowser extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url != null) {
                contentBoosted = false;
                view.loadUrl(url);
            }
            return true;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            if (request != null && request.getUrl() != null) {
                contentBoosted = false;
                view.loadUrl(request.getUrl().toString());
            }
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            dismissProgress();
            boostReadableContent(view);
        }

        @Override
        public void onReceivedError(
                WebView view, WebResourceRequest request, WebResourceError error) {
            if (request != null && request.isForMainFrame()) {
                showLoadError();
            }
        }

        @Override
        @SuppressWarnings("deprecation")
        public void onReceivedError(
                WebView view, int errorCode, String description, String failingUrl) {
            showLoadError();
        }

        @Override
        public boolean onRenderProcessGone(WebView view,
                android.webkit.RenderProcessGoneDetail detail) {
            return com.anyNews.anynews.Utils.SafeWebViewClient
                    .handleRenderProcessGone(view, detail);
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            if (handler != null) {
                handler.cancel();
            }
        }
    }

    @Override
    protected void onDestroy() {
        dismissProgress();
        try {
            if (webView != null) {
                webView.stopLoading();
                webView.destroy();
            }
        } catch (Exception ignored) {
        }
        super.onDestroy();
    }
}
