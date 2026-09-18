package com.anyNews.anynews.Utils;

import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Small process-local coordinator for native request dedupe / backoff / counters.
 * It deliberately has no network or backend dependency: HomePage owns GMA/Facebook
 * SDK calls and views; {@link AdPreloader} holds the shared warm unimpressed pool.
 *
 * <p>A slot key is valid while its configured unit/type remains useful. Flip changes do
 * not invalidate it. Activity destroy (or a hard pool wipe) calls {@link #clearAll()}.
 * Ordinary News/Local/Flash/Special switches should prefer pruning impressed creatives
 * only — clearing this map alone is fine; do not destroy warm {@link AdPreloader} ads.
 *
 * <p>Failures never permanently disable a seat for the process lifetime: after a short
 * burst of fast retries the slot enters a longer recover backoff, then tries again.
 */
public final class NativeAdCacheCoordinator {
    private static final String TAG = "ANYNEWS_ADS";
    /**
     * Seat-level ready markers (not the AdPreloader object pool). Keep near warm
     * depth so ahead-of-seat prefetch cannot stockpile unused matched creatives.
     */
    private static final int MAX_READY = 3;
    /** Fast retries before entering long recover backoff (never session-kill). */
    private static final int FAST_RETRY_BURST = 1;
    private static final long RETRY_BASE_MS = 2_000L;
    private static final long NO_FILL_RETRY_MS = 15_000L;
    /** After a no-fill / fail streak, wait this long then try again (always). */
    private static final long RECOVER_BACKOFF_MS = 30_000L;
    private static final long MAX_BACKOFF_MS = 60_000L;

    public enum StartDecision { START, ALREADY_READY, IN_FLIGHT, BACKOFF, CACHE_FULL }

    private static final class Entry {
        final String key;
        int failures;
        boolean inFlight;
        boolean ready;
        long retryAfterUptimeMs;

        Entry(String key) {
            this.key = key;
        }
    }

    private final Map<String, Entry> entries = new HashMap<>();
    private int eligibleOpportunities;
    private int paidLoaded;
    private int paidShown;
    private int houseFallbacks;
    private int failures;
    private int noFills;
    private int cacheHits;
    private int cacheMisses;

    private NativeAdCacheCoordinator() {}

    private static final class Holder {
        private static final NativeAdCacheCoordinator INSTANCE = new NativeAdCacheCoordinator();
    }

    public static NativeAdCacheCoordinator get() {
        return Holder.INSTANCE;
    }

    public static String key(String adType, String adCode, int requestPosition) {
        return (TextUtils.isEmpty(adType) ? "google" : adType.toLowerCase())
                + "|" + (adCode == null ? "" : adCode.trim()) + "|" + requestPosition;
    }

    public synchronized StartDecision requestStart(
            String adType, String adCode, int requestPosition, int maxInFlight) {
        if (TextUtils.isEmpty(adCode)) {
            log("REQUEST rejected empty configured unit");
            return StartDecision.BACKOFF;
        }
        eligibleOpportunities++;
        String key = key(adType, adCode, requestPosition);
        Entry entry = entries.get(key);
        if (entry == null) {
            entry = new Entry(key);
            entries.put(key, entry);
        }
        if (entry.ready) {
            cacheHits++;
            log("CACHE HIT " + key);
            return StartDecision.ALREADY_READY;
        }
        cacheMisses++;
        long now = SystemClock.uptimeMillis();
        if (entry.inFlight) {
            log("REQUEST dedupe in-flight " + key);
            return StartDecision.IN_FLIGHT;
        }
        if (entry.retryAfterUptimeMs > now) {
            log("REQUEST backoff " + key + " remaining="
                    + (entry.retryAfterUptimeMs - now));
            return StartDecision.BACKOFF;
        }
        if (readyCountLocked() >= MAX_READY || inFlightCountLocked() >= Math.max(1, maxInFlight)) {
            log("REQUEST cache/cap full " + key);
            return StartDecision.CACHE_FULL;
        }
        entry.inFlight = true;
        log("REQUEST / LOAD START " + key);
        return StartDecision.START;
    }

    public synchronized void onLoaded(String adType, String adCode, int requestPosition,
                                      String adapterName) {
        Entry entry = getOrCreate(adType, adCode, requestPosition);
        entry.inFlight = false;
        entry.ready = true;
        entry.failures = 0;
        entry.retryAfterUptimeMs = 0L;
        paidLoaded++;
        log("LOAD SUCCESS / PRELOAD " + entry.key + " adapter=" + safe(adapterName));
        trimReadyLocked();
    }

    /**
     * Returns a retry delay in ms. Never returns {@code -1} — seats must keep
     * recovering for the whole session whenever inventory returns.
     */
    public synchronized long onFailed(String adType, String adCode, int requestPosition,
                                      boolean noFill, String detail) {
        Entry entry = getOrCreate(adType, adCode, requestPosition);
        entry.inFlight = false;
        entry.ready = false;
        failures++;
        if (noFill) noFills++;
        entry.failures++;
        long delay;
        if (entry.failures <= FAST_RETRY_BURST) {
            delay = noFill ? NO_FILL_RETRY_MS
                    : RETRY_BASE_MS * (1L << (entry.failures - 1));
            log((noFill ? "NO FILL" : "LOAD FAILURE") + " / RETRY " + entry.key
                    + " delay=" + delay + " detail=" + safe(detail));
        } else {
            // Soft-exhaust: long recover, then reset burst counter so fill can resume.
            delay = Math.min(
                    noFill ? RECOVER_BACKOFF_MS
                            : Math.max(RECOVER_BACKOFF_MS,
                                    RETRY_BASE_MS * (1L << Math.min(entry.failures - 1, 5))),
                    MAX_BACKOFF_MS);
            entry.failures = 0;
            log((noFill ? "NO FILL" : "LOAD FAILURE")
                    + " / RECOVER (never session-kill) " + entry.key
                    + " delay=" + delay + " detail=" + safe(detail));
        }
        entry.retryAfterUptimeMs = SystemClock.uptimeMillis() + delay;
        return delay;
    }

    public synchronized void onConsumed(String adType, String adCode, int requestPosition) {
        Entry entry = entries.get(key(adType, adCode, requestPosition));
        if (entry != null) {
            entry.ready = false;
        }
        log("CONSUMED / replacement preload eligible "
                + key(adType, adCode, requestPosition));
    }

    public synchronized void resetSlotFailure(String adType, String adCode, int requestPosition) {
        Entry entry = entries.get(key(adType, adCode, requestPosition));
        if (entry != null) {
            entry.failures = 0;
            entry.retryAfterUptimeMs = 0L;
        }
    }

    /** Called only from a network SDK's real impression callback. */
    public synchronized void onPaidShown(String adType, String adCode, int requestPosition) {
        paidShown++;
        log("SHOWN " + key(adType, adCode, requestPosition));
    }

    public synchronized void onHouseFallback(int requestPosition) {
        houseFallbacks++;
        log("HOUSE FALLBACK pos=" + requestPosition);
    }

    public synchronized void logMediationAdapter(String adapterName) {
        log("MEDIATION ADAPTER " + safe(adapterName));
    }

    public synchronized void clearAll() {
        entries.clear();
        log("CACHE disposed");
    }

    /**
     * Settle / land path: clear backoff + failure counters so on-land load
     * always gets another chance. Also clears a stale {@code ready} marker so
     * ALREADY_READY cannot block forever after a destroyed creative. Never
     * cancels a true in-flight SDK request.
     */
    public synchronized void allowSettleRetry(String adType, String adCode, int requestPosition) {
        Entry entry = entries.get(key(adType, adCode, requestPosition));
        if (entry == null) {
            return;
        }
        if (entry.inFlight) {
            return;
        }
        entry.failures = 0;
        entry.retryAfterUptimeMs = 0L;
        // Stale ready with no creative left would otherwise skip loads forever.
        entry.ready = false;
        log("SETTLE RETRY unlocked " + entry.key);
    }

    /** True when a seat already has an SDK request in flight (do not double-start). */
    public synchronized boolean isInFlight(String adType, String adCode, int requestPosition) {
        Entry entry = entries.get(key(adType, adCode, requestPosition));
        return entry != null && entry.inFlight;
    }

    /** Remaining backoff ms, or 0 when start may proceed. */
    public synchronized long millisUntilRetryAllowed(
            String adType, String adCode, int requestPosition) {
        Entry entry = entries.get(key(adType, adCode, requestPosition));
        if (entry == null || entry.retryAfterUptimeMs <= 0L) {
            return 0L;
        }
        long remaining = entry.retryAfterUptimeMs - SystemClock.uptimeMillis();
        return Math.max(0L, remaining);
    }

    private Entry getOrCreate(String type, String code, int position) {
        String key = key(type, code, position);
        Entry entry = entries.get(key);
        if (entry == null) {
            entry = new Entry(key);
            entries.put(key, entry);
        }
        return entry;
    }

    private int inFlightCountLocked() {
        int count = 0;
        for (Entry entry : entries.values()) if (entry.inFlight) count++;
        return count;
    }

    private int readyCountLocked() {
        int count = 0;
        for (Entry entry : entries.values()) if (entry.ready) count++;
        return count;
    }

    private void trimReadyLocked() {
        int ready = readyCountLocked();
        if (ready <= MAX_READY) return;
        Iterator<Entry> iterator = entries.values().iterator();
        while (iterator.hasNext() && ready > MAX_READY) {
            Entry entry = iterator.next();
            if (entry.ready && !entry.inFlight) {
                iterator.remove();
                ready--;
            }
        }
    }

    @NonNull
    private static String safe(String value) {
        return value == null ? "unknown" : value;
    }

    private void log(String message) {
        Log.d(TAG, message + " counters{eligible=" + eligibleOpportunities
                + ",loaded=" + paidLoaded + ",shown=" + paidShown + ",house=" + houseFallbacks
                + ",fail=" + failures + ",noFill=" + noFills + ",hit=" + cacheHits
                + ",miss=" + cacheMisses + "}");
    }
}
