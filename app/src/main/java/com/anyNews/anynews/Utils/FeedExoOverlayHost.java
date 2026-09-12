package com.anyNews.anynews.Utils;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerView;

import com.anyNews.anynews.R;
import com.anyNews.anynews.Ui.FeedExoOverlayFrameLayout;

/**
 * Single feed Exo {@link PlayerView} hosted outside FlipView scrap.
 * <p>
 * FlipView recycle / neighbor rebinds never move this surface — that was the mid-play
 * blink when {@code setPlayer} hopped between scrap TextureViews. Chrome on the flip
 * card stays put; this host paints the picture + Way2 controller over
 * {@code exo_play_layout}, and lifts half-card engagement (like/comment/share) onto
 * the video so the overlay does not hide it under a white strip.
 */
@UnstableApi
public final class FeedExoOverlayHost {

    private final FrameLayout container;
    private final PlayerView playerView;
    @Nullable
    private final ProgressBar bufferingSpinner;
    private final int[] containerLoc = new int[2];
    private final int[] anchorLoc = new int[2];
    private final Rect visibleRect = new Rect();

    @Nullable
    private View anchorPlayLayout;
    private boolean hiddenForFlipGesture;
    private boolean sessionActive;

    @Nullable
    private View liftedEngagement;
    @Nullable
    private ViewGroup liftedEngagementParent;
    private int liftedEngagementIndex = -1;
    @Nullable
    private ViewGroup.LayoutParams liftedEngagementLp;
    private int liftedEngagementViewId = View.NO_ID;

    private final Runnable syncBoundsRunnable = this::syncBoundsNow;

    private final View.OnLayoutChangeListener anchorLayoutListener =
            (v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
                if (!sessionActive || hiddenForFlipGesture || v != anchorPlayLayout) {
                    return;
                }
                if (right - left != oldRight - oldLeft
                        || bottom - top != oldTop - oldBottom
                        || left != oldLeft
                        || top != oldTop) {
                    syncBoundsSoon();
                }
            };

    private final ViewTreeObserver.OnScrollChangedListener anchorScrollListener =
            () -> {
                if (sessionActive && !hiddenForFlipGesture && anchorPlayLayout != null) {
                    syncBoundsSoon();
                }
            };

    private FeedExoOverlayHost(
            FrameLayout container, PlayerView playerView, @Nullable ProgressBar bufferingSpinner) {
        this.container = container;
        this.playerView = playerView;
        this.bufferingSpinner = bufferingSpinner;
        harden(playerView);
        container.setVisibility(View.GONE);
        container.setClickable(false);
        container.setFocusable(false);
        if (container instanceof FeedExoOverlayFrameLayout) {
            ((FeedExoOverlayFrameLayout) container).setTouchTarget(playerView);
        }
        if (bufferingSpinner != null) {
            bufferingSpinner.setVisibility(View.GONE);
        }
    }

    @Nullable
    public static FeedExoOverlayHost bind(Activity activity) {
        if (activity == null) {
            return null;
        }
        FrameLayout container = activity.findViewById(R.id.feed_exo_overlay_host);
        PlayerView playerView = activity.findViewById(R.id.feed_exo_overlay_player);
        if (container == null || playerView == null) {
            return null;
        }
        ProgressBar spinner = activity.findViewById(R.id.feed_exo_overlay_spinner);
        return new FeedExoOverlayHost(container, playerView, spinner);
    }

    public PlayerView getPlayerView() {
        return playerView;
    }

    @Nullable
    public ProgressBar getBufferingSpinner() {
        return bufferingSpinner;
    }

    public boolean isSessionActive() {
        return sessionActive;
    }

    /** True when this host is currently aligned to {@code candidate} (or any anchor if null). */
    public boolean isAnchoredTo(@Nullable View candidate) {
        if (!sessionActive || anchorPlayLayout == null) {
            return false;
        }
        return candidate == null || anchorPlayLayout == candidate;
    }

    /**
     * Align + show over the card's {@code exo_play_layout}. Idempotent while the same
     * anchor is live — never tears the TextureView.
     */
    public void showOver(@Nullable View exoPlayLayout) {
        if (exoPlayLayout == null) {
            return;
        }
        sessionActive = true;
        if (anchorPlayLayout != exoPlayLayout) {
            detachAnchorListeners();
            restoreLiftedEngagement();
            anchorPlayLayout = exoPlayLayout;
            attachAnchorListeners();
            liftEngagementForAnchor(exoPlayLayout);
        }
        if (hiddenForFlipGesture) {
            if (container.getVisibility() == View.VISIBLE) {
                container.setVisibility(View.INVISIBLE);
            }
            return;
        }
        syncBoundsNow();
        revealIfReady();
        harden(playerView);
    }

    public void hideVisual() {
        container.removeCallbacks(syncBoundsRunnable);
        if (container.getVisibility() != View.GONE) {
            container.setVisibility(View.INVISIBLE);
        }
        hideBufferingSpinner();
    }

    public void hideVisualIfAnchoredTo(@Nullable View maybeAnchor) {
        if (maybeAnchor != null && anchorPlayLayout != null && maybeAnchor != anchorPlayLayout) {
            return;
        }
        hideVisual();
    }

    public void endSession() {
        sessionActive = false;
        detachAnchorListeners();
        restoreLiftedEngagement();
        anchorPlayLayout = null;
        container.removeCallbacks(syncBoundsRunnable);
        hideBufferingSpinner();
        container.setVisibility(View.GONE);
    }

    public void clearPlayerBinding() {
        try {
            if (playerView.getPlayer() != null) {
                playerView.setPlayer(null);
            }
        } catch (Exception ignored) {
        }
        endSession();
    }

    public void setHiddenForFlipGesture(boolean hidden) {
        hiddenForFlipGesture = hidden;
        if (hidden) {
            hideVisual();
        }
    }

    public void syncBoundsSoon() {
        container.removeCallbacks(syncBoundsRunnable);
        container.post(syncBoundsRunnable);
    }

    public void showBufferingSpinner() {
        if (bufferingSpinner == null) {
            return;
        }
        if (bufferingSpinner.getParent() != container) {
            // Spinner lives in the overlay XML already.
        }
        positionSpinnerOverPlayer();
        bufferingSpinner.setVisibility(View.VISIBLE);
        bufferingSpinner.bringToFront();
    }

    public void hideBufferingSpinner() {
        if (bufferingSpinner != null) {
            bufferingSpinner.setVisibility(View.GONE);
        }
    }

    public void syncBoundsNow() {
        View anchor = anchorPlayLayout;
        if (anchor == null || !sessionActive || hiddenForFlipGesture) {
            return;
        }
        if (!anchor.isAttachedToWindow()) {
            hideVisual();
            return;
        }
        if (anchor.getWidth() <= 0 || anchor.getHeight() <= 0) {
            syncBoundsSoon();
            return;
        }
        if (!anchor.getGlobalVisibleRect(visibleRect) || visibleRect.isEmpty()) {
            hideVisual();
            return;
        }
        if (container.getVisibility() == View.GONE) {
            container.setVisibility(View.INVISIBLE);
        }
        container.getLocationOnScreen(containerLoc);
        anchor.getLocationOnScreen(anchorLoc);
        int left = anchorLoc[0] - containerLoc[0];
        int top = anchorLoc[1] - containerLoc[1];
        int width = anchor.getWidth();
        int height = anchor.getHeight();

        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) playerView.getLayoutParams();
        if (lp == null) {
            lp = new FrameLayout.LayoutParams(width, height);
        }
        boolean changed = lp.width != width
                || lp.height != height
                || lp.leftMargin != left
                || lp.topMargin != top;
        if (changed) {
            lp.width = width;
            lp.height = height;
            lp.leftMargin = left;
            lp.topMargin = top;
            playerView.setLayoutParams(lp);
        }
        positionLiftedEngagement(left, top, width, height);
        positionSpinnerOverPlayer();
        if (!hiddenForFlipGesture) {
            revealIfReady();
        }
    }

    private void revealIfReady() {
        if (!sessionActive || hiddenForFlipGesture || anchorPlayLayout == null) {
            return;
        }
        if (container.getVisibility() != View.VISIBLE) {
            container.setVisibility(View.VISIBLE);
        }
        if (playerView.getVisibility() != View.VISIBLE) {
            playerView.setVisibility(View.VISIBLE);
        }
        if (liftedEngagement != null) {
            liftedEngagement.setVisibility(View.VISIBLE);
            liftedEngagement.bringToFront();
        }
    }

    private void liftEngagementForAnchor(@Nullable View exoPlayLayout) {
        View middle = findEngagementRow(exoPlayLayout);
        if (middle == null || !(middle.getParent() instanceof ViewGroup)) {
            return;
        }
        // Full-video side menu stays on the card; only half-card middle row is lifted.
        if (middle.getId() == R.id.video_side_menu) {
            return;
        }
        liftedEngagementParent = (ViewGroup) middle.getParent();
        liftedEngagementIndex = liftedEngagementParent.indexOfChild(middle);
        liftedEngagementLp = middle.getLayoutParams();
        liftedEngagementViewId = middle.getId();
        try {
            liftedEngagementParent.removeView(middle);
        } catch (Exception e) {
            liftedEngagementParent = null;
            return;
        }
        // Transparent so icons sit on the video, not a white strip.
        try {
            middle.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        } catch (Exception ignored) {
        }
        FrameLayout.LayoutParams liftLp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        container.addView(middle, liftLp);
        liftedEngagement = middle;
        middle.bringToFront();
    }

    private void positionLiftedEngagement(int playerLeft, int playerTop, int playerW, int playerH) {
        View middle = liftedEngagement;
        if (middle == null) {
            return;
        }
        middle.measure(
                View.MeasureSpec.makeMeasureSpec(Math.max(1, playerW), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        int h = middle.getMeasuredHeight();
        if (h <= 0) {
            h = (int) (48f * middle.getResources().getDisplayMetrics().density + 0.5f);
        }
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) middle.getLayoutParams();
        if (lp == null) {
            lp = new FrameLayout.LayoutParams(playerW, h);
        }
        // Sit ON the video, but leave a thin strip at the bottom for the red progress line.
        int progressPad = (int) (22f * middle.getResources().getDisplayMetrics().density + 0.5f);
        lp.width = playerW;
        lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        lp.leftMargin = playerLeft;
        lp.topMargin = playerTop + Math.max(0, playerH - progressPad - h);
        middle.setLayoutParams(lp);
        middle.bringToFront();
    }

    private void positionSpinnerOverPlayer() {
        if (bufferingSpinner == null) {
            return;
        }
        FrameLayout.LayoutParams plp = (FrameLayout.LayoutParams) playerView.getLayoutParams();
        if (plp == null || plp.width <= 0 || plp.height <= 0) {
            return;
        }
        FrameLayout.LayoutParams slp = (FrameLayout.LayoutParams) bufferingSpinner.getLayoutParams();
        if (slp == null) {
            slp = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        int size = bufferingSpinner.getLayoutParams() != null
                ? Math.max(bufferingSpinner.getLayoutParams().width, 56)
                : 56;
        if (size <= 0) {
            size = (int) (56f * bufferingSpinner.getResources().getDisplayMetrics().density);
        }
        slp.width = size;
        slp.height = size;
        slp.leftMargin = plp.leftMargin + (plp.width - size) / 2;
        slp.topMargin = plp.topMargin + (plp.height - size) / 2;
        bufferingSpinner.setLayoutParams(slp);
    }

    private void restoreLiftedEngagement() {
        View middle = liftedEngagement;
        ViewGroup parent = liftedEngagementParent;
        if (middle == null) {
            return;
        }
        try {
            if (middle.getParent() instanceof ViewGroup) {
                ((ViewGroup) middle.getParent()).removeView(middle);
            }
        } catch (Exception ignored) {
        }
        if (parent != null && liftedEngagementLp != null) {
            try {
                int index = liftedEngagementIndex;
                if (index < 0 || index > parent.getChildCount()) {
                    parent.addView(middle, liftedEngagementLp);
                } else {
                    parent.addView(middle, index, liftedEngagementLp);
                }
            } catch (Exception e) {
                try {
                    parent.addView(middle, liftedEngagementLp);
                } catch (Exception ignored) {
                }
            }
        }
        liftedEngagement = null;
        liftedEngagementParent = null;
        liftedEngagementLp = null;
        liftedEngagementIndex = -1;
        liftedEngagementViewId = View.NO_ID;
    }

    @Nullable
    private static View findEngagementRow(@Nullable View exoPlayLayout) {
        if (exoPlayLayout == null) {
            return null;
        }
        // Walk up to the half-card root and find layoutParentMiddle.
        View root = exoPlayLayout;
        for (int i = 0; i < 8 && root != null; i++) {
            View middle = root.findViewById(R.id.layoutParentMiddle);
            if (middle != null) {
                return middle;
            }
            ViewParent p = root.getParent();
            root = p instanceof View ? (View) p : null;
        }
        return null;
    }

    private void attachAnchorListeners() {
        View anchor = anchorPlayLayout;
        if (anchor == null) {
            return;
        }
        anchor.addOnLayoutChangeListener(anchorLayoutListener);
        try {
            ViewTreeObserver observer = anchor.getViewTreeObserver();
            if (observer.isAlive()) {
                observer.addOnScrollChangedListener(anchorScrollListener);
            }
        } catch (Exception ignored) {
        }
    }

    private void detachAnchorListeners() {
        View anchor = anchorPlayLayout;
        if (anchor == null) {
            return;
        }
        try {
            anchor.removeOnLayoutChangeListener(anchorLayoutListener);
        } catch (Exception ignored) {
        }
        try {
            ViewTreeObserver observer = anchor.getViewTreeObserver();
            if (observer.isAlive()) {
                observer.removeOnScrollChangedListener(anchorScrollListener);
            }
        } catch (Exception ignored) {
        }
    }

    @UnstableApi
    private static void harden(PlayerView playerView) {
        if (playerView == null) {
            return;
        }
        try {
            playerView.setKeepContentOnPlayerReset(true);
            playerView.setUseArtwork(false);
            playerView.setDefaultArtwork(null);
            playerView.setShutterBackgroundColor(android.graphics.Color.TRANSPARENT);
            playerView.setBackgroundColor(android.graphics.Color.BLACK);
            playerView.setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER);
            if (playerView.getResizeMode() != AspectRatioFrameLayout.RESIZE_MODE_FIT) {
                playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            }
            View surface = playerView.getVideoSurfaceView();
            if (surface instanceof android.view.TextureView) {
                ((android.view.TextureView) surface).setOpaque(true);
            }
            View shutter = playerView.findViewById(androidx.media3.ui.R.id.exo_shutter);
            if (shutter != null) {
                shutter.setBackgroundColor(android.graphics.Color.TRANSPARENT);
                shutter.setAlpha(0f);
                shutter.setVisibility(View.GONE);
            }
        } catch (Exception ignored) {
        }
    }
}
