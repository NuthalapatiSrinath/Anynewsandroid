package com.anyNews.anynews.Utils;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerView;

import com.anyNews.anynews.R;

/**
 * Single feed Exo {@link PlayerView} hosted outside FlipView scrap.
 * <p>
 * FlipView recycle / neighbor rebinds never move this surface — that was the mid-play
 * blink when {@code setPlayer} hopped between scrap TextureViews. Chrome on the flip
 * card (poster, center play, side menu) stays put; this host only paints the picture +
 * Way2 controller (mute / fullscreen / progress) over {@code exo_play_layout}.
 */
@UnstableApi
public final class FeedExoOverlayHost {

    private final FrameLayout container;
    private final PlayerView playerView;
    private final int[] containerLoc = new int[2];
    private final int[] anchorLoc = new int[2];
    private final Rect visibleRect = new Rect();

    @Nullable
    private View anchorPlayLayout;
    private boolean hiddenForFlipGesture;
    private boolean sessionActive;

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

    private FeedExoOverlayHost(FrameLayout container, PlayerView playerView) {
        this.container = container;
        this.playerView = playerView;
        harden(playerView);
        container.setVisibility(View.GONE);
        // Pass-through when hidden; when shown, PlayerView consumes video taps.
        container.setClickable(false);
        container.setFocusable(false);
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
        return new FeedExoOverlayHost(container, playerView);
    }

    public PlayerView getPlayerView() {
        return playerView;
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
            anchorPlayLayout = exoPlayLayout;
            attachAnchorListeners();
        }
        if (hiddenForFlipGesture) {
            // Keep layout so restore after settle has valid screen coords.
            if (container.getVisibility() == View.VISIBLE) {
                container.setVisibility(View.INVISIBLE);
            }
            return;
        }
        syncBoundsNow();
        revealIfReady();
        harden(playerView);
    }

    /**
     * Hide for flip gesture / share — keep player↔surface binding.
     * Uses {@link View#INVISIBLE} so bounds stay measurable for restore.
     */
    public void hideVisual() {
        container.removeCallbacks(syncBoundsRunnable);
        if (container.getVisibility() != View.GONE) {
            container.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Hide only if {@code maybeAnchor} owns the current session (or session has no
     * anchor). Neighbor scrap rebinds must not blank a live video on another seat.
     */
    public void hideVisualIfAnchoredTo(@Nullable View maybeAnchor) {
        if (maybeAnchor != null && anchorPlayLayout != null && maybeAnchor != anchorPlayLayout) {
            return;
        }
        hideVisual();
    }

    /** End the feed video session (release path). Does not call {@code setPlayer(null)}. */
    public void endSession() {
        sessionActive = false;
        detachAnchorListeners();
        anchorPlayLayout = null;
        container.removeCallbacks(syncBoundsRunnable);
        container.setVisibility(View.GONE);
    }

    /** Detach Exo from this host only (caller owns {@code ExoPlayer.release()}). */
    public void clearPlayerBinding() {
        try {
            if (playerView.getPlayer() != null) {
                playerView.setPlayer(null);
            }
        } catch (Exception ignored) {
        }
        endSession();
    }

    /**
     * Hide during 3D flip so a flat overlay does not sit on a rotating card.
     * Restore is via {@link #showOver} after settle / play — not automatic (avoids
     * flashing the picture over the wrong card when landing on a non-video).
     */
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
        // Temporarily ensure container participates in layout so location is valid.
        int priorVisibility = container.getVisibility();
        if (priorVisibility == View.GONE) {
            container.setVisibility(View.INVISIBLE);
        }
        container.getLocationOnScreen(containerLoc);
        anchor.getLocationOnScreen(anchorLoc);
        int left = anchorLoc[0] - containerLoc[0];
        int top = anchorLoc[1] - containerLoc[1];
        int width = anchor.getWidth();
        int height = anchor.getHeight();
        // Clamp to visible portion when the card is partially off-screen.
        if (visibleRect.width() > 0 && visibleRect.height() > 0) {
            // Prefer full card slot size; only hide if completely invisible (handled above).
        }
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
