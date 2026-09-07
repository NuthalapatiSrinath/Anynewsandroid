package com.anyNews.anynews.Utils;

import android.os.SystemClock;

/**
 * Process-wide gate so AdMob load/bind/pool/impression flush never pressure the
 * FlipView gesture path. HomePage notes each flip; loaders and reporters defer
 * until the scroller has been idle for {@link #SETTLE_GRACE_MS}.
 *
 * <p>Deferral is not cancellation: a deferred preload remains VALID across flips
 * unless the owning feed generation is replaced or the Activity is destroyed.
 */
public final class FlipAdWorkGate {

    /** Wait after last flip interaction before heavy bind/paint ad work. */
    public static final long SETTLE_GRACE_MS = 160L;

    /**
     * During an active fling / grace window, allow at most this many concurrent
     * native loads (even if admin max_in_flight is higher).
     */
    public static final int MAX_IN_FLIGHT_DURING_FLIP = 1;

    private static volatile boolean flipAnimating;
    private static volatile long lastInteractionUptimeMs;

    private FlipAdWorkGate() {}

    /** Call from onFlippedToPage / OnFlipCompleted entry (and similar). */
    public static void noteFlipInteraction() {
        lastInteractionUptimeMs = SystemClock.uptimeMillis();
    }

    /** Keep in sync with {@link com.anyNews.anynews.flipview.FlipView#isFlipAnimating()}. */
    public static void setFlipAnimating(boolean animating) {
        flipAnimating = animating;
        if (animating) {
            lastInteractionUptimeMs = SystemClock.uptimeMillis();
        }
    }

    public static boolean isFlipAnimating() {
        return flipAnimating;
    }

    /**
     * True while FlipView is mid-gesture/scroller OR within settle grace after the
     * last flip. Heavy AdMob / pool / flush work must wait.
     */
    public static boolean shouldDeferAdWork() {
        if (flipAnimating) {
            return true;
        }
        long last = lastInteractionUptimeMs;
        if (last <= 0L) {
            return false;
        }
        return (SystemClock.uptimeMillis() - last) < SETTLE_GRACE_MS;
    }

    /** Milliseconds to wait before the next safe ad tick (0 = run now). */
    public static long millisUntilAdWorkSafe() {
        if (flipAnimating) {
            return SETTLE_GRACE_MS;
        }
        long last = lastInteractionUptimeMs;
        if (last <= 0L) {
            return 0L;
        }
        long elapsed = SystemClock.uptimeMillis() - last;
        if (elapsed >= SETTLE_GRACE_MS) {
            return 0L;
        }
        return SETTLE_GRACE_MS - elapsed;
    }
}
