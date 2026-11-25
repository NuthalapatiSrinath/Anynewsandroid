package com.anyNews.anynews.Utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.anyNews.anynews.R;
import com.anyNews.anynews.flipview.FlipView;
import com.anyNews.anynews.flipview.OverFlipMode;
import com.bumptech.glide.Glide;

/**
 * Ad-screen recommend tap: temp FlipView surface over the main feed.
 * Does not splice into the main FlipView list — flip up dismisses and the
 * underlying ad/recommend page stays seated.
 */
public final class AdScreenArticleOverlay implements FlipView.OnFlipListener {

    private static final long SLIDE_MS = 280L;
    private static final DecelerateInterpolator DECEL = new DecelerateInterpolator();

    @NonNull private final Context host;
    @NonNull private final FrameLayout hostFrame;
    @Nullable private View root;
    @Nullable private FlipView flipView;
    @Nullable private ModelClass.Datum datum;
    private boolean showing;
    private boolean animating;

    public AdScreenArticleOverlay(@NonNull Context host, @NonNull FrameLayout hostFrame) {
        this.host = host;
        this.hostFrame = hostFrame;
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
        if (root == null || flipView == null) {
            return;
        }
        OverlayFlipAdapter adapter = new OverlayFlipAdapter();
        flipView.setAdapter(adapter);
        flipView.setOnFlipListener(this);
        flipView.setOverFlipMode(OverFlipMode.GLOW);
        try {
            flipView.setFlipDistanceperpage(
                    200, host.getResources().getDisplayMetrics().density);
        } catch (Exception ignored) {
        }
        try {
            flipView.flipTo(0);
        } catch (Exception ignored) {
        }

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
        flipView = root.findViewById(R.id.ad_article_overlay_flip);
    }

    private void cancelAnim() {
        if (root != null) {
            root.animate().cancel();
            root.setLayerType(View.LAYER_TYPE_NONE, null);
        }
    }

    private void hideHostImmediate() {
        cancelAnim();
        if (root != null) {
            root.setTranslationY(0f);
            root.setVisibility(View.GONE);
        }
        hostFrame.setVisibility(View.GONE);
        hostFrame.removeAllViews();
        root = null;
        flipView = null;
        datum = null;
        showing = false;
        animating = false;
    }

    @Override
    public void onFlippedToPage(FlipView v, int position, long id) {
        // Page 1 = dismiss sentinel — article flipped up; ad seat untouched underneath.
        if (position >= 1) {
            dismiss(true);
        }
    }

    private final class OverlayFlipAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (position == 1) {
                View blank = convertView;
                if (blank == null) {
                    blank = new View(parent.getContext());
                    blank.setBackgroundColor(0xFF121212);
                    blank.setLayoutParams(
                            new ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT));
                }
                return blank;
            }
            View page = convertView;
            if (page == null) {
                page =
                        LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.page_ad_overlay_article, parent, false);
                page.setLayoutParams(
                        new ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT));
            }
            bindArticlePage(page);
            return page;
        }
    }

    private void bindArticlePage(@NonNull View page) {
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
