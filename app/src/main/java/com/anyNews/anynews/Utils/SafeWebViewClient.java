package com.anyNews.anynews.Utils;

import android.os.Build;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;

/**
 * WebViewClient that survives renderer process death instead of taking down the app.
 * Flip cards / legal browsers should extend this (or call {@link #handleRenderProcessGone}).
 */
public class SafeWebViewClient extends WebViewClient {

    @Override
    public boolean onRenderProcessGone(WebView view, RenderProcessGoneDetail detail) {
        return handleRenderProcessGone(view, detail);
    }

    /**
     * @return true if the crash was handled (do not kill the activity / process).
     */
    public static boolean handleRenderProcessGone(
            @Nullable WebView view, @Nullable RenderProcessGoneDetail detail) {
        if (view == null) {
            return true;
        }
        try {
            boolean crashed = true;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && detail != null) {
                crashed = detail.didCrash();
            }
            Logone.w("SafeWebView onRenderProcessGone crashed=" + crashed);
            // Detach and destroy the dead WebView; callers re-inflate on next bind.
            try {
                if (view.getParent() instanceof android.view.ViewGroup) {
                    ((android.view.ViewGroup) view.getParent()).removeView(view);
                }
            } catch (Throwable ignored) {
            }
            try {
                view.destroy();
            } catch (Throwable ignored) {
            }
        } catch (Throwable ignored) {
        }
        return true;
    }
}
