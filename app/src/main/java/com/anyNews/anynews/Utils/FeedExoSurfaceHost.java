package com.anyNews.anynews.Utils;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerView;

import com.anyNews.anynews.R;

/**
 * Stable feed Exo <b>picture</b> only — one {@link PlayerView} reparented under the card's
 * {@code exo_play_layout} (index 0) so FlipView scrap recycle cannot create a new TextureView
 * mid-play (that was the blink while audio kept going).
 * <p>
 * Controllers stay on the in-card scrap {@code PlayerView} ({@code surface_type=none}).
 * This host never mounts Way2 chrome / time / spinner — unlike the old overlay experiment.
 */
@UnstableApi
public final class FeedExoSurfaceHost {

    private final FrameLayout park;
    private final PlayerView playerView;

    @Nullable
    private ViewGroup attachedParent;
    @Nullable
    private View cardRoot;
    private boolean sessionActive;

    private FeedExoSurfaceHost(FrameLayout park, PlayerView playerView) {
        this.park = park;
        this.playerView = playerView;
        harden(playerView);
        park.setVisibility(View.GONE);
        playerView.setUseController(false);
        playerView.setControllerAutoShow(false);
    }

    @Nullable
    public static FeedExoSurfaceHost bind(Activity activity) {
        if (activity == null) {
            return null;
        }
        FrameLayout park = activity.findViewById(R.id.feed_exo_surface_park);
        PlayerView playerView = activity.findViewById(R.id.feed_exo_surface_player);
        if (park == null || playerView == null) {
            return null;
        }
        // Ensure parked as child of park for first attach.
        if (playerView.getParent() != park) {
            try {
                if (playerView.getParent() instanceof ViewGroup) {
                    ((ViewGroup) playerView.getParent()).removeView(playerView);
                }
            } catch (Exception ignored) {
            }
            park.addView(playerView, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
        }
        return new FeedExoSurfaceHost(park, playerView);
    }

    public PlayerView getPlayerView() {
        return playerView;
    }

    public boolean isSessionActive() {
        return sessionActive;
    }

    @Nullable
    public View getCardRoot() {
        return cardRoot;
    }

    /** True when this host is painting under {@code candidate} card (or any if null). */
    public boolean isAnchoredToCard(@Nullable View candidate) {
        if (!sessionActive || cardRoot == null) {
            return false;
        }
        return candidate == null || cardRoot == candidate;
    }

    /**
     * Place the stable surface as the bottom child of {@code exoPlayLayout} so in-card
     * chrome (mute / fs / progress / likes siblings) draw above it.
     */
    public void attachUnderChrome(@Nullable ViewGroup exoPlayLayout, @Nullable View hostCardRoot) {
        if (exoPlayLayout == null) {
            return;
        }
        sessionActive = true;
        cardRoot = hostCardRoot;
        if (playerView.getParent() == exoPlayLayout) {
            // Keep at bottom so scrap controller stays on top.
            if (exoPlayLayout.indexOfChild(playerView) != 0) {
                try {
                    exoPlayLayout.removeView(playerView);
                    exoPlayLayout.addView(playerView, 0, matchParentLp());
                } catch (Exception ignored) {
                }
            }
            attachedParent = exoPlayLayout;
            harden(playerView);
            return;
        }
        detachFromParentKeepPlayer();
        try {
            exoPlayLayout.addView(playerView, 0, matchParentLp());
            attachedParent = exoPlayLayout;
        } catch (Exception e) {
            // Fallback: keep in park if the card rejected the child.
            parkIfNeeded();
        }
        harden(playerView);
        if (playerView.getVisibility() != View.VISIBLE) {
            playerView.setVisibility(View.VISIBLE);
        }
    }

    /** Reparent to the activity park without clearing {@code setPlayer}. */
    public void parkSurface() {
        sessionActive = false;
        cardRoot = null;
        parkIfNeeded();
    }

    /**
     * Park only if {@code maybeCard} owns the session — neighbor scrap must not blank
     * a live picture on another seat.
     */
    public void parkIfAnchoredToCard(@Nullable View maybeCard) {
        if (maybeCard != null && cardRoot != null && maybeCard != cardRoot) {
            return;
        }
        parkSurface();
    }

    /** Detach Exo from this host only (caller owns {@code ExoPlayer.release()}). */
    public void clearPlayerBinding() {
        try {
            if (playerView.getPlayer() != null) {
                playerView.setPlayer(null);
            }
        } catch (Exception ignored) {
        }
        parkSurface();
    }

    private void parkIfNeeded() {
        if (playerView.getParent() == park) {
            attachedParent = park;
            return;
        }
        detachFromParentKeepPlayer();
        try {
            park.addView(playerView, matchParentLp());
            attachedParent = park;
        } catch (Exception ignored) {
        }
    }

    private void detachFromParentKeepPlayer() {
        try {
            Object parent = playerView.getParent();
            if (parent instanceof ViewGroup) {
                ((ViewGroup) parent).removeView(playerView);
            }
        } catch (Exception ignored) {
        }
        attachedParent = null;
    }

    private static FrameLayout.LayoutParams matchParentLp() {
        return new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
    }

    @UnstableApi
    private static void harden(PlayerView playerView) {
        if (playerView == null) {
            return;
        }
        try {
            playerView.setUseController(false);
            playerView.setControllerAutoShow(false);
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
