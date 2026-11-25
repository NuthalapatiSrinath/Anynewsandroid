package com.anyNews.anynews.Utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.anyNews.anynews.R;
import com.bumptech.glide.Glide;

/**
 * Ad-screen recommend tap: temp surface over the main feed.
 * Article page reuses {@link FlipperAdapter#createAdOverlayArticleView} when possible.
 */
public final class AdScreenArticleOverlay {

    private static final long SLIDE_MS = 280L;
    private static final DecelerateInterpolator DECEL = new DecelerateInterpolator();

    @NonNull private final Context host;
    @NonNull private final FrameLayout hostFrame;
    @Nullable private FlipperAdapter feedAdapter;
    @Nullable private View root;
    @Nullable private FrameLayout contentHost;
    @Nullable private ModelClass.Datum datum;
    private boolean showing;
    private boolean animating;

    public AdScreenArticleOverlay(@NonNull Context host, @NonNull FrameLayout hostFrame) {
        this.host = host;
        this.hostFrame = hostFrame;
    }

    public void setFeedAdapter(@Nullable FlipperAdapter adapter) {
        this.feedAdapter = adapter;
    }

    public boolean isShowing() {
        return showing;
    }

    public void show(@Nullable ModelClass.Datum article) {
        if (article == null || TextUtils.isEmpty(article.getTitle()) || animating) {
            return;
        }
        if (showing) {
            dismiss(false);
        }
        datum = article;
        ensureInflated();
        if (root == null || contentHost == null) {
            return;
        }
        bindArticleSurface();
        attachFlipUpDismiss(root);

        showing = true;
        animating = true;
        hostFrame.setVisibility(View.VISIBLE);
        root.setVisibility(View.VISIBLE);
        root.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        final int h = Math.max(hostFrame.getHeight(), root.getHeight());
        final float startY = h > 0 ? h : host.getResources().getDisplayMetrics().heightPixels;
        root.setTranslationY(startY);
        root.animate()
                .translationY(0f)
                .setDuration(SLIDE_MS)
                .setInterpolator(DECEL)
                .setListener(
                        new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                animating = false;
                                if (root != null) {
                                    root.setLayerType(View.LAYER_TYPE_NONE, null);
                                }
                            }
                        })
                .start();
    }

    public boolean dismiss(boolean animate) {
        if (!showing || root == null) {
            hideHostImmediate();
            return false;
        }
        if (animating && animate) {
            return true;
        }
        showing = false;
        if (!animate) {
            cancelAnim();
            hideHostImmediate();
            return true;
        }
        animating = true;
        root.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        final int h = Math.max(hostFrame.getHeight(), root.getHeight());
        final float endY = -(h > 0 ? h : host.getResources().getDisplayMetrics().heightPixels);
        root.animate()
                .translationY(endY)
                .setDuration(SLIDE_MS)
                .setInterpolator(DECEL)
                .setListener(
                        new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                animating = false;
                                hideHostImmediate();
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {
                                animating = false;
                                hideHostImmediate();
                            }
                        })
                .start();
        return true;
    }

    private void ensureInflated() {
        if (root != null) {
            return;
        }
        root =
                LayoutInflater.from(host)
                        .inflate(R.layout.layout_ad_article_overlay, hostFrame, false);
        hostFrame.removeAllViews();
        hostFrame.addView(
                root,
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
        contentHost = root.findViewById(R.id.ad_article_overlay_content);
    }

    private void bindArticleSurface() {
        if (contentHost == null) {
            return;
        }
        contentHost.removeAllViews();
        ModelClass.Datum d = datum;
        if (d != null && feedAdapter != null) {
            View feedPage = feedAdapter.createAdOverlayArticleView(contentHost, d);
            if (feedPage != null) {
                if (feedPage.getParent() instanceof ViewGroup) {
                    ((ViewGroup) feedPage.getParent()).removeView(feedPage);
                }
                contentHost.addView(
                        feedPage,
                        new FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT));
                attachFlipUpDismiss(feedPage);
                return;
            }
        }
        View page =
                LayoutInflater.from(host)
                        .inflate(R.layout.page_ad_overlay_article, contentHost, false);
        contentHost.addView(
                page,
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
        bindArticleFallback(page);
        attachFlipUpDismiss(page);
    }

    private void attachFlipUpDismiss(@NonNull View target) {
        final float[] downY = new float[1];
        final float[] downX = new float[1];
        target.setOnTouchListener(
                (v, event) -> {
                    switch (event.getActionMasked()) {
                        case MotionEvent.ACTION_DOWN:
                            downY[0] = event.getY();
                            downX[0] = event.getX();
                            return false;
                        case MotionEvent.ACTION_UP:
                            float dy = downY[0] - event.getY();
                            float dx = Math.abs(event.getX() - downX[0]);
                            if (dy > Math.max(72f, v.getHeight() * 0.1f) && dy > dx * 1.2f) {
                                dismiss(true);
                                return true;
                            }
                            return false;
                        default:
                            return false;
                    }
                });
    }

    private void cancelAnim() {
        if (root != null) {
            root.animate().cancel();
            root.setLayerType(View.LAYER_TYPE_NONE, null);
        }
    }

    private void hideHostImmediate() {
        cancelAnim();
        if (contentHost != null) {
            contentHost.removeAllViews();
        }
        if (root != null) {
            root.setTranslationY(0f);
            root.setVisibility(View.GONE);
        }
        hostFrame.setVisibility(View.GONE);
        hostFrame.removeAllViews();
        root = null;
        contentHost = null;
        datum = null;
        showing = false;
        animating = false;
    }

    private void bindArticleFallback(@NonNull View page) {
        TextView title = page.findViewById(R.id.ad_overlay_title);
        TextView body = page.findViewById(R.id.ad_overlay_body);
        ImageView image = page.findViewById(R.id.ad_overlay_image);
        ModelClass.Datum d = datum;
        if (title != null) {
            title.setText(d != null && d.getTitle() != null ? d.getTitle() : "");
        }
        if (body != null) {
            String desc = d != null ? d.getDescription() : null;
            if (TextUtils.isEmpty(desc)) {
                body.setVisibility(View.GONE);
            } else {
                body.setVisibility(View.VISIBLE);
                body.setText(desc);
            }
        }
        if (image == null || d == null) {
            return;
        }
        String url = d.getThumbnail();
        if (TextUtils.isEmpty(url) && d.getImages() != null && !d.getImages().isEmpty()) {
            url = d.getImages().get(0);
        }
        if (TextUtils.isEmpty(url)) {
            image.setImageDrawable(null);
            return;
        }
        try {
            Glide.with(image.getContext()).load(url).centerCrop().into(image);
        } catch (Exception ignored) {
        }
    }
}