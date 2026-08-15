package com.anyNews.anynews.Utils;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.Nullable;

/**
 * Caches and reads admin cricket surface toggles delivered via splash, session
 * open/heartbeat, and (only while mini-player poll is allowed)
 * {@code /app/v3/cricket_active_match}:
 *
 * <ul>
 *   <li>{@link Constants#CRICKET_MINI_PLAYER_ENABLED} — master floating mini player.
 *       Unset → {@code false} until splash/session delivers admin ON. This is the
 *       <b>poll permission</b> gate (footer alone never starts cricket HTTP).</li>
 *   <li>{@link Constants#CRICKET_FOOTER_TAB_ENABLED} — bottom-footer cricket tab
 *       (display only; opens Match Center when an active mini match exists).</li>
 *   <li>{@link Constants#CRICKET_MINI_PLAYER_POLL_INTERVAL_SECONDS} — live poll
 *       cadence (seconds). Server/admin is sole clamp; app does not invent floors.</li>
 *   <li>{@link Constants#CRICKET_MINI_PLAYER_IDLE_POLL_INTERVAL_SECONDS} — reserved
 *       idle cadence; Home does not idle-poll when no active mini match.</li>
 * </ul>
 *
 * <p>HTTP rule: {@link #isCricketPollAllowed} (mini master ON) plus a real active
 * mini-player match. Footer ON alone → <b>zero</b> {@code cricket_active_match}.
 * No match with Mini Player ON → zero cricket polls.
 *
 * <p>Cadence: if poll prefs are missing, return 0 so callers wait for splash/session
 * rather than inventing a client floor that ignores admin 10s.
 */
public final class CricketSurfaceConfigHelper {

    public static final int HEARTBEAT_MIN_SECONDS = 5;
    public static final int HEARTBEAT_DEFAULT_SECONDS = 0;
    public static final int HEARTBEAT_DEFAULT_WHEN_ON = 30;

    /**
     * Optional absolute client safety max when server never sent a max.
     * Not a floor — only caps absurd values if pref is corrupted.
     */
    public static final int CRICKET_POLL_ABS_MAX_SECONDS = 300;

    /** @deprecated No client floor — admin/server clamps only. Kept for compile refs. */
    @Deprecated
    public static final int CRICKET_POLL_MIN_SECONDS = 0;

    /** @deprecated Prefer wait-for-server (0) over inventing a default poll. */
    @Deprecated
    public static final int CRICKET_POLL_DEFAULT_SECONDS = 0;

    /** @deprecated Idle comes from admin or soft adaptive; no hard 60s floor. */
    @Deprecated
    public static final int CRICKET_POLL_IDLE_SECONDS = 0;

    private CricketSurfaceConfigHelper() {
    }

    public static void saveMiniPlayerEnabled(@Nullable Context context, @Nullable Boolean enabled) {
        if (context == null || enabled == null) return;
        SharedPrefsData.putString(
                context.getApplicationContext(),
                Constants.CRICKET_MINI_PLAYER_ENABLED,
                enabled ? "true" : "false",
                Constants.PREF_NAME);
    }

    public static void saveFooterTabEnabled(@Nullable Context context, @Nullable Boolean enabled) {
        if (context == null || enabled == null) return;
        SharedPrefsData.putString(
                context.getApplicationContext(),
                Constants.CRICKET_FOOTER_TAB_ENABLED,
                enabled ? "true" : "false",
                Constants.PREF_NAME);
    }

    /**
     * Persist server live poll interval as-is (no min floor). Positive only.
     * {@code <=0} clears the cached value (wait for next splash/session).
     */
    public static void savePollIntervalSeconds(@Nullable Context context, int seconds) {
        if (context == null) return;
        if (seconds <= 0) {
            SharedPrefsData.putString(
                    context.getApplicationContext(),
                    Constants.CRICKET_MINI_PLAYER_POLL_INTERVAL_SECONDS,
                    "",
                    Constants.PREF_NAME);
            return;
        }
        int safe = seconds;
        if (safe > CRICKET_POLL_ABS_MAX_SECONDS) {
            safe = CRICKET_POLL_ABS_MAX_SECONDS;
        }
        SharedPrefsData.putString(
                context.getApplicationContext(),
                Constants.CRICKET_MINI_PLAYER_POLL_INTERVAL_SECONDS,
                String.valueOf(safe),
                Constants.PREF_NAME);
    }

    /** Persist idle poll interval from admin. {@code <=0} clears. */
    public static void saveIdlePollIntervalSeconds(@Nullable Context context, int seconds) {
        if (context == null) return;
        if (seconds <= 0) {
            SharedPrefsData.putString(
                    context.getApplicationContext(),
                    Constants.CRICKET_MINI_PLAYER_IDLE_POLL_INTERVAL_SECONDS,
                    "",
                    Constants.PREF_NAME);
            return;
        }
        int safe = Math.min(CRICKET_POLL_ABS_MAX_SECONDS, seconds);
        SharedPrefsData.putString(
                context.getApplicationContext(),
                Constants.CRICKET_MINI_PLAYER_IDLE_POLL_INTERVAL_SECONDS,
                String.valueOf(safe),
                Constants.PREF_NAME);
    }

    public static void saveLiveEnabled(@Nullable Context context, @Nullable Boolean enabled) {
        if (context == null || enabled == null) return;
        SharedPrefsData.putString(
                context.getApplicationContext(),
                Constants.SESSION_LIVE_ENABLED,
                enabled ? "true" : "false",
                Constants.PREF_NAME);
        if (!enabled) {
            saveHeartbeatIntervalSeconds(context, 0);
        }
    }

    public static void saveLiveWindowSeconds(@Nullable Context context, int seconds) {
        if (context == null) return;
        int safe = Math.max(0, seconds);
        SharedPrefsData.putString(
                context.getApplicationContext(),
                Constants.SESSION_LIVE_WINDOW_SECONDS,
                String.valueOf(safe),
                Constants.PREF_NAME);
    }

    public static void saveHeartbeatIntervalSeconds(@Nullable Context context, int seconds) {
        if (context == null) return;
        int safe;
        if (seconds <= 0) {
            safe = 0;
        } else {
            safe = Math.max(HEARTBEAT_MIN_SECONDS, seconds);
        }
        SharedPrefsData.putString(
                context.getApplicationContext(),
                Constants.SESSION_HEARTBEAT_INTERVAL_SECONDS,
                String.valueOf(safe),
                Constants.PREF_NAME);
    }

    public static void applyPresenceFromServer(
            @Nullable Context context,
            @Nullable Boolean liveEnabled,
            @Nullable Integer heartbeatSec,
            @Nullable Integer windowSec) {
        if (context == null) return;

        Boolean effectiveEnabled = liveEnabled;
        if (effectiveEnabled == null && heartbeatSec != null) {
            effectiveEnabled = heartbeatSec > 0;
        }

        if (effectiveEnabled != null && !effectiveEnabled) {
            saveLiveEnabled(context, false);
            saveLiveWindowSeconds(context, 0);
            return;
        }

        if (effectiveEnabled != null) {
            SharedPrefsData.putString(
                    context.getApplicationContext(),
                    Constants.SESSION_LIVE_ENABLED,
                    "true",
                    Constants.PREF_NAME);
        }

        if (heartbeatSec != null) {
            saveHeartbeatIntervalSeconds(context, Math.max(0, heartbeatSec));
        }
        if (windowSec != null) {
            saveLiveWindowSeconds(context, Math.max(0, windowSec));
        } else if (heartbeatSec != null && heartbeatSec > 0) {
            saveLiveWindowSeconds(context, Math.max(HEARTBEAT_MIN_SECONDS, heartbeatSec) * 2);
        }
    }

    public static boolean isMiniPlayerEnabled(@Nullable Context context) {
        return readBool(context, Constants.CRICKET_MINI_PLAYER_ENABLED, false);
    }

    public static boolean isFooterTabEnabled(@Nullable Context context) {
        return readBool(context, Constants.CRICKET_FOOTER_TAB_ENABLED, false);
    }

    /**
     * True when any cricket UI surface is admin-enabled (mini and/or footer).
     * Used for Match Center / tab visibility — <b>not</b> for background polls.
     */
    public static boolean isAnyCricketSurfaceEnabled(@Nullable Context context) {
        return isMiniPlayerEnabled(context) || isFooterTabEnabled(context);
    }

    /**
     * True when the app may run {@code cricket_active_match} background polls.
     * Master mini player only — footer tab is display-only and must not poll.
     * Callers must also require a real active mini-player match (stop on null).
     */
    public static boolean isCricketPollAllowed(@Nullable Context context) {
        return isMiniPlayerEnabled(context);
    }

    /**
     * True when splash/session has delivered a live poll interval (&gt; 0).
     * Until then, cricket polling must wait (no invented client cadence).
     */
    public static boolean hasPollCadence(@Nullable Context context) {
        return getPollIntervalSeconds(context) > 0;
    }

    /**
     * Live-match poll interval in seconds. {@code 0} if not yet delivered by
     * server — callers must not invent a default floor.
     */
    public static int getPollIntervalSeconds(@Nullable Context context) {
        int raw = readIntOrZero(context, Constants.CRICKET_MINI_PLAYER_POLL_INTERVAL_SECONDS);
        if (raw <= 0) return 0;
        return Math.min(CRICKET_POLL_ABS_MAX_SECONDS, raw);
    }

    /**
     * Idle poll (no active match). Prefer admin idle key; else soft adaptive
     * ({@code live * 3}) without a hard 60s floor; {@code 0} if live also unknown.
     */
    public static int getIdlePollIntervalSeconds(@Nullable Context context) {
        int idle = readIntOrZero(context, Constants.CRICKET_MINI_PLAYER_IDLE_POLL_INTERVAL_SECONDS);
        if (idle > 0) {
            return Math.min(CRICKET_POLL_ABS_MAX_SECONDS, idle);
        }
        int live = getPollIntervalSeconds(context);
        if (live <= 0) return 0;
        long soft = (long) live * 3L;
        if (soft > CRICKET_POLL_ABS_MAX_SECONDS) soft = CRICKET_POLL_ABS_MAX_SECONDS;
        return (int) soft;
    }

    public static boolean isLiveEnabled(@Nullable Context context) {
        return readBool(context, Constants.SESSION_LIVE_ENABLED, false);
    }

    public static int getLiveWindowSeconds(@Nullable Context context) {
        return Math.max(0, readInt(context, Constants.SESSION_LIVE_WINDOW_SECONDS, 0));
    }

    public static int getHeartbeatIntervalSeconds(@Nullable Context context) {
        if (!isLiveEnabled(context)) return 0;
        int raw = readInt(context, Constants.SESSION_HEARTBEAT_INTERVAL_SECONDS, HEARTBEAT_DEFAULT_SECONDS);
        if (raw <= 0) return 0;
        return Math.max(HEARTBEAT_MIN_SECONDS, raw);
    }

    public static boolean isHeartbeatEnabled(@Nullable Context context) {
        return getHeartbeatIntervalSeconds(context) > 0;
    }

    private static boolean readBool(@Nullable Context context, String key, boolean fallback) {
        if (context == null) return fallback;
        try {
            String v = SharedPrefsData.getString(context.getApplicationContext(), key, Constants.PREF_NAME);
            if (TextUtils.isEmpty(v)) return fallback;
            return "true".equalsIgnoreCase(v.trim());
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static int readInt(@Nullable Context context, String key, int fallback) {
        if (context == null) return fallback;
        try {
            String v = SharedPrefsData.getString(context.getApplicationContext(), key, Constants.PREF_NAME);
            if (TextUtils.isEmpty(v)) return fallback;
            return Integer.parseInt(v.trim());
        } catch (Exception ignored) {
            return fallback;
        }
    }

    /** Empty/missing pref → 0 (not a silent invented default). */
    private static int readIntOrZero(@Nullable Context context, String key) {
        if (context == null) return 0;
        try {
            String v = SharedPrefsData.getString(context.getApplicationContext(), key, Constants.PREF_NAME);
            if (TextUtils.isEmpty(v)) return 0;
            return Integer.parseInt(v.trim());
        } catch (Exception ignored) {
            return 0;
        }
    }
}
