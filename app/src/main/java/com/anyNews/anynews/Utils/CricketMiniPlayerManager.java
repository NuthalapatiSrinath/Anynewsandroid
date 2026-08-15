package com.anyNews.anynews.Utils;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.anyNews.anynews.Activities.MatchCenterActivity;
import com.anyNews.anynews.Pojo.ActiveMatch;
import com.anyNews.anynews.Pojo.CricketActiveMatchResponse;
import com.anyNews.anynews.Pojo.CricketMiniPlayerPosition;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Services.APIService;
import com.anyNews.anynews.Services.RestApiClient;
import com.google.android.material.snackbar.Snackbar;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Cricket Live Mini Player controller.
 *
 * Attaches a draggable floating card to an Activity's content root. The card is
 * shown ONLY when the backend reports a single active match (see the data
 * contract in ActiveMatch / helpers/crex/activeMatch.js). Tapping it opens the
 * Match Center; the close button hides it and shows a "Live match closed"
 * snackbar with an orange UNDO to restore it for the session.
 *
 * <p><b>Poll gate (strict):</b> Home polls {@code /app/v3/cricket_active_match}
 * only while master mini player is admin-ON <b>and</b> a specific match has
 * Mini Player ON (non-null {@code active_match}). Footer tab ON alone must
 * <b>never</b> start interval polls. No active mini match / splash mini OFF /
 * API returns null → <b>zero</b> cricket HTTP. Footer may still paint from a
 * feed-seeded active match without polling.
 */
public class CricketMiniPlayerManager {

    private static int opensSinceLastSync = 0;
    private static boolean globalIsVisible = false;
    private static boolean globalIsViewing = false;

    // Dismissed match ids for this app process (session). Cleared when the
    // admin turns the mini player OFF (null active_match) so turning the same
    // match back ON shows the player again. UNDO also clears the id.
    private static final Set<String> dismissedForSession = new HashSet<>();

    // Last non-null active match seen this app process. Shown INSTANTLY when a
    // new host screen opens so the card never flashes blank while the first poll
    // is in flight; the poll then refreshes it in place.
    private static ActiveMatch lastKnownActive;

    private final WeakReference<Activity> activityRef;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private final android.content.BroadcastReceiver stateReceiver = new android.content.BroadcastReceiver() {
        @Override
        public void onReceive(android.content.Context context, Intent intent) {
            if (SessionAnalyticsTracker.ACTION_CRICKET_STATE_CHANGED.equals(intent.getAction())) {
                Activity a = activityRef.get();
                if (a != null) {
                    applySurfaceGateFromPrefs(a);
                }
            }
        }
    };

    private View miniView;
    private ActiveMatch current;
    /** Last non-null active match id from the server (for dismiss / OFF cleanup). */
    private String lastServerActiveId;
    private boolean attached = false;
    private boolean pollingEnabled = true;
    private boolean pollInFlight = false;
    /** True while a poll tick is scheduled or expected to reschedule. */
    private boolean pollLoopArmed = false;
    /**
     * Whether the last successful poll (or feed seed) had a live active_match.
     * Required together with mini master ON before any cricket_active_match HTTP.
     */
    private boolean lastPollHadActiveMatch = false;
    /** Whole LIVE-pill opacity blink while the active match is live. */
    private ObjectAnimator liveBadgeBlink;
    /** Last applied placement JSON — avoid resetting the card on every poll. */
    private String lastAppliedPositionJson;

    /**
     * Admin master toggle for the floating mini player. Default false until
     * prefs say ON (splash/session). Required for background polls; when false
     * the floating card stays hidden (footer may still show a seeded match).
     */
    private boolean miniPlayerEnabled = false;
    /** Admin toggle for the bottom-footer cricket tab (display only — no polls). */
    private boolean footerTabEnabled = false;
    /** True while HomePage is mid 3D flip — overlay hidden for gesture only. */
    private boolean hiddenForFlipGesture = false;
    /** Whether the card was visible when flip gesture started (for restore). */
    private boolean visibleBeforeFlipGesture = false;

    /**
     * Optional host callback fired on each poll (and on the cached onResume paint)
     * with the current active match + footer flag, so a host screen can show or
     * hide its own footer cricket tab. The mini player and footer tab are
     * independent surfaces of the same active match.
     */
    public interface CricketPollListener {
        void onCricketPoll(ActiveMatch match, boolean footerTabEnabled);
    }

    private CricketPollListener pollListener;

    private final Runnable pollRunnable = new Runnable() {
        @Override
        public void run() {
            if (!shouldPollCricket()) {
                // Hard gate: mini off or no active mini match → zero cricket HTTP.
                return;
            }
            Activity a = activityRef.get();
            if (a == null || a.isFinishing()) {
                return;
            }
            // Wait for splash/session cadence — do not invent client floors / 1s spam.
            if (!CricketSurfaceConfigHelper.hasPollCadence(a)) {
                handler.postDelayed(this, 1500L);
                return;
            }
            refreshFromServer();
            scheduleNextPoll();
        }
    };

    /**
     * True only when master mini is ON and we already know an active mini-player
     * match exists. Footer never qualifies. No active match → no polls.
     */
    private boolean shouldPollCricket() {
        return miniPlayerEnabled && lastPollHadActiveMatch;
    }

    /**
     * Schedules next cricket_active_match poll from admin live interval only.
     * Never schedules when mini is OFF or there is no active mini match.
     */
    private void scheduleNextPoll() {
        if (!pollingEnabled || !shouldPollCricket()) {
            return;
        }
        Activity a = activityRef.get();
        if (a == null || a.isFinishing()) {
            return;
        }
        int sec = CricketSurfaceConfigHelper.getPollIntervalSeconds(a);
        if (sec <= 0) {
            // Cadence not delivered yet — recheck without HTTP.
            handler.postDelayed(pollRunnable, 1500L);
            return;
        }
        handler.postDelayed(pollRunnable, sec * 1000L);
    }

    /** Start the poll loop once when the strict gate first allows it. */
    private void ensurePollingLoop() {
        if (!pollingEnabled || !shouldPollCricket()) {
            return;
        }
        if (pollLoopArmed) {
            return;
        }
        pollLoopArmed = true;
        handler.removeCallbacks(pollRunnable);
        handler.post(pollRunnable);
    }

    public CricketMiniPlayerManager(Activity activity) {
        this.activityRef = new WeakReference<>(activity);
        // Seed from cached splash/session flags so open respects admin toggles
        // before any network (and skips HTTP until mini + active match).
        this.miniPlayerEnabled = CricketSurfaceConfigHelper.isMiniPlayerEnabled(activity);
        this.footerTabEnabled = CricketSurfaceConfigHelper.isFooterTabEnabled(activity);
    }

    /** Enable/disable background polling of the dedicated endpoint. */
    public void setPollingEnabled(boolean enabled) {
        this.pollingEnabled = enabled;
        if (!enabled) {
            stopPollingCompletely();
        }
    }

    /** Register a host callback for footer-tab driving (see {@link CricketPollListener}). */
    public void setPollListener(CricketPollListener listener) {
        this.pollListener = listener;
    }

    /** Last active match seen this process (used by a host footer tab tap). */
    public static ActiveMatch getLastKnownActive() {
        return lastKnownActive;
    }

    public static boolean isCricketVisible() {
        return globalIsVisible;
    }

    public static boolean isCricketViewing() {
        return globalIsViewing;
    }

    public static String getActiveMatchId() {
        return lastKnownActive != null ? lastKnownActive.getMatch_id() : null;
    }

    public static int getAndResetOpensSinceLastSync() {
        int o = opensSinceLastSync;
        opensSinceLastSync = 0;
        return o;
    }

    public static void incrementOpens() {
        opensSinceLastSync++;
    }

    public static void setGlobalIsViewing(boolean viewing) {
        globalIsViewing = viewing;
    }

    private void notifyPollListener(ActiveMatch match) {
        if (pollListener != null) {
            try {
                // Footer is independent of mini: pass match whenever footer is ON.
                ActiveMatch forFooter = footerTabEnabled ? match : null;
                pollListener.onCricketPoll(forFooter, footerTabEnabled);
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Re-read admin mini/footer prefs and start/stop poll accordingly.
     * Called from session open response broadcast and onResume.
     * Footer alone never starts polls; mini ON without a known active match
     * waits for feed seed / a later non-null active_match.
     */
    private void applySurfaceGateFromPrefs(Activity activity) {
        boolean wasPolling = shouldPollCricket();
        miniPlayerEnabled = CricketSurfaceConfigHelper.isMiniPlayerEnabled(activity);
        footerTabEnabled = CricketSurfaceConfigHelper.isFooterTabEnabled(activity);
        if (!miniPlayerEnabled) {
            stopPollingCompletely();
            hide();
            // Keep lastKnown for footer paint if footer still ON; else clear.
            if (!footerTabEnabled) {
                current = null;
                lastPollHadActiveMatch = false;
                notifyPollListener(null);
            } else {
                lastPollHadActiveMatch = false;
                notifyPollListener(lastKnownActive);
            }
            return;
        }
        // Mini ON: only poll when we already have an active mini match.
        if (lastKnownActive != null
                && !TextUtils.isEmpty(lastKnownActive.getMatch_id())) {
            lastPollHadActiveMatch = true;
            current = lastKnownActive;
            lastServerActiveId = lastKnownActive.getMatch_id();
            if (!dismissedForSession.contains(lastKnownActive.getMatch_id())) {
                show();
                bind(lastKnownActive);
            }
            notifyPollListener(lastKnownActive);
            if (!wasPolling) {
                ensurePollingLoop();
            }
        } else {
            lastPollHadActiveMatch = false;
            stopPollingCompletely();
            hide();
            notifyPollListener(null);
        }
    }

    /** Cancel poll loop; no further cricket_active_match HTTP until gate allows. */
    private void stopPollingCompletely() {
        handler.removeCallbacks(pollRunnable);
        pollInFlight = false;
        pollLoopArmed = false;
    }

    /**
     * Host became visible. Polls only when mini is ON and an active mini match
     * is already known. Footer alone: zero cricket HTTP.
     */
    public void onResume() {
        Activity activity = activityRef.get();
        if (activity != null) {
            try {
                androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(activity)
                        .registerReceiver(stateReceiver,
                                new android.content.IntentFilter(
                                        SessionAnalyticsTracker.ACTION_CRICKET_STATE_CHANGED));
            } catch (Exception ignored) {
            }
            applySurfaceGateFromPrefs(activity);
        }

        if (!pollingEnabled || !shouldPollCricket()) {
            // Still paint footer from cache when display-only.
            if (footerTabEnabled) {
                notifyPollListener(lastKnownActive);
            }
            return;
        }

        // Paint the last-known active match immediately (cached, no network).
        if (lastKnownActive != null
                && !TextUtils.isEmpty(lastKnownActive.getMatch_id())
                && !dismissedForSession.contains(lastKnownActive.getMatch_id())) {
            current = lastKnownActive;
            lastServerActiveId = lastKnownActive.getMatch_id();
            lastPollHadActiveMatch = true;
            show();
            bind(lastKnownActive);
        }
        notifyPollListener(lastKnownActive);
        // onResume may have disarmed via onPause; re-arm if gate still open.
        pollLoopArmed = false;
        ensurePollingLoop();
    }

    public void onPause() {
        handler.removeCallbacks(pollRunnable);
        pollLoopArmed = false;
        Activity activity = activityRef.get();
        if (activity != null) {
            try {
                androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(activity)
                        .unregisterReceiver(stateReceiver);
            } catch (Exception ignored) {
            }
        }
    }

    public void onDestroy() {
        stopPollingCompletely();
        Activity activity = activityRef.get();
        if (activity != null) {
            try {
                androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(activity)
                        .unregisterReceiver(stateReceiver);
            } catch (Exception ignored) {
            }
        }
        detach();
    }

    /**
     * Immediate one-shot refresh. No-ops unless mini is ON and an active match
     * is already known (never probes just because footer is ON).
     */
    public void refreshNow() {
        if (!shouldPollCricket()) return;
        refreshFromServer();
    }

    /**
     * Main entry point. Called by the host screen on every feed load with the
     * active_match from the feed config (may be null). Does not issue cricket
     * HTTP by itself. Seeds mini/footer UI. Starts background polls only when
     * mini master is ON and {@code match} is a real active mini-player match.
     * Null match → stop all cricket polls (zero HTTP until a match is seeded).
     */
    public void update(ActiveMatch match) {
        Activity activity = activityRef.get();
        if (activity == null || activity.isFinishing()) return;

        // Re-sync flags in case splash/session flipped them without resume.
        miniPlayerEnabled = CricketSurfaceConfigHelper.isMiniPlayerEnabled(activity);
        footerTabEnabled = CricketSurfaceConfigHelper.isFooterTabEnabled(activity);

        if (!miniPlayerEnabled && !footerTabEnabled) {
            stopPollingCompletely();
            if (!TextUtils.isEmpty(lastServerActiveId)) {
                dismissedForSession.remove(lastServerActiveId);
                lastServerActiveId = null;
            }
            current = null;
            lastKnownActive = null;
            lastPollHadActiveMatch = false;
            hide();
            notifyPollListener(null);
            return;
        }

        if (match == null || TextUtils.isEmpty(match.getMatch_id())) {
            // No LIVE mini player active → zero cricket HTTP.
            stopPollingCompletely();
            if (!TextUtils.isEmpty(lastServerActiveId)) {
                dismissedForSession.remove(lastServerActiveId);
                lastServerActiveId = null;
            }
            current = null;
            lastKnownActive = null;
            lastPollHadActiveMatch = false;
            hide();
            notifyPollListener(null);
            return;
        }

        String matchId = match.getMatch_id();
        lastPollHadActiveMatch = true;

        // Switched active match A→B: drop A's dismiss and show B.
        if (!TextUtils.isEmpty(lastServerActiveId) && !lastServerActiveId.equals(matchId)) {
            dismissedForSession.remove(lastServerActiveId);
        }
        lastServerActiveId = matchId;
        current = match;
        lastKnownActive = match;

        // Footer-only: track match for the tab, never show the floating card,
        // never start cricket_active_match polls.
        if (!miniPlayerEnabled) {
            stopPollingCompletely();
            hide();
            notifyPollListener(match);
            return;
        }

        if (dismissedForSession.contains(matchId)) {
            hide();
            notifyPollListener(match);
            ensurePollingLoop();
            return;
        }

        show();
        bind(match);
        notifyPollListener(match);
        ensurePollingLoop();
    }

    private void ensureAttached() {
        if (attached && miniView != null) return;
        Activity activity = activityRef.get();
        if (activity == null) return;

        ViewGroup root = activity.findViewById(android.R.id.content);
        if (root == null) return;

        miniView = LayoutInflater.from(activity).inflate(R.layout.view_cricket_mini_player, root, false);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        miniView.setLayoutParams(lp);
        miniView.setVisibility(View.GONE);
        root.addView(miniView);
        attached = true;

        setupInteractions(activity, root);
        positionInitial(root);
    }

    private void positionInitial(final ViewGroup root) {
        miniView.post(new Runnable() {
            @Override
            public void run() {
                if (miniView == null) return;
                int parentW = root.getWidth();
                int parentH = root.getHeight();
                int viewW = miniView.getWidth();
                int viewH = miniView.getHeight();
                float margin = dp(12);
                float x;
                float y;

                Activity activity = activityRef.get();
                CricketMiniPlayerPosition custom = CricketMiniPlayerPositionHelper.loadCustom(activity);
                if (custom != null) {
                    // Custom: x/y % of the movable range inside a 12dp margin.
                    float maxX = Math.max(0f, parentW - viewW - 2f * margin);
                    float maxY = Math.max(0f, parentH - viewH - 2f * margin);
                    x = margin + maxX * (float) (custom.getXPercent() / 100d);
                    y = margin + maxY * (float) (custom.getYPercent() / 100d);
                } else {
                    // Default: bottom-right, 12dp from right, 140dp up from bottom.
                    x = parentW - viewW - margin;
                    y = parentH - viewH - dp(140);
                }
                if (x < margin) x = margin;
                if (y < margin) y = margin;
                float maxXClamp = Math.max(margin, parentW - viewW - margin);
                float maxYClamp = Math.max(margin, parentH - viewH - margin);
                x = clamp(x, margin, maxXClamp);
                y = clamp(y, margin, maxYClamp);
                miniView.setX(x);
                miniView.setY(y);
            }
        });
    }

    private void setupInteractions(final Activity activity, final ViewGroup root) {
        final ImageView close = miniView.findViewById(R.id.cmp_close);
        final ImageView expand = miniView.findViewById(R.id.cmp_expand);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissWithUndo();
            }
        });

        View.OnClickListener openListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMatchCenter();
            }
        };
        expand.setOnClickListener(openListener);

        // Drag anywhere; distinguish a tap (open) from a drag using touch slop.
        final int touchSlop = ViewConfiguration.get(activity).getScaledTouchSlop();
        miniView.setOnTouchListener(new View.OnTouchListener() {
            float downRawX, downRawY, downX, downY;
            boolean dragging;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        downRawX = event.getRawX();
                        downRawY = event.getRawY();
                        downX = miniView.getX();
                        downY = miniView.getY();
                        dragging = false;
                        return true;
                    case MotionEvent.ACTION_MOVE: {
                        float dx = event.getRawX() - downRawX;
                        float dy = event.getRawY() - downRawY;
                        if (!dragging && Math.hypot(dx, dy) > touchSlop) {
                            dragging = true;
                        }
                        if (dragging) {
                            float newX = downX + dx;
                            float newY = downY + dy;
                            float maxX = root.getWidth() - miniView.getWidth();
                            float maxY = root.getHeight() - miniView.getHeight();
                            miniView.setX(clamp(newX, 0, maxX));
                            miniView.setY(clamp(newY, 0, maxY));
                        }
                        return true;
                    }
                    case MotionEvent.ACTION_UP:
                        if (!dragging) {
                            v.performClick();
                            openMatchCenter();
                        } else {
                            snapToEdge(root);
                        }
                        return true;
                    default:
                        return false;
                }
            }
        });
    }

    private void snapToEdge(ViewGroup root) {
        if (miniView == null) return;
        float margin = dp(8);
        float center = miniView.getX() + miniView.getWidth() / 2f;
        float target = center < root.getWidth() / 2f
                ? margin
                : root.getWidth() - miniView.getWidth() - margin;
        miniView.animate().x(target).setDuration(160).start();
    }

    private void bind(ActiveMatch m) {
        if (miniView == null) return;

        View liveBadge = miniView.findViewById(R.id.cmp_live_badge);
        View liveDot = miniView.findViewById(R.id.cmp_live_dot);
        TextView liveLabel = miniView.findViewById(R.id.cmp_live_label);
        TextView format = miniView.findViewById(R.id.cmp_format);
        TextView team1 = miniView.findViewById(R.id.cmp_team1);
        TextView team2 = miniView.findViewById(R.id.cmp_team2);
        TextView status = miniView.findViewById(R.id.cmp_status);

        boolean live = m.isLive();
        if (live) {
            liveLabel.setText("LIVE");
            liveLabel.setLetterSpacing(0.1f);
            if (liveDot != null) liveDot.setVisibility(View.VISIBLE);
            liveBadgeBlink = LiveBadgeBlinkHelper.start(liveBadge, liveBadgeBlink);
        } else {
            liveBadgeBlink = LiveBadgeBlinkHelper.stop(liveBadge, liveBadgeBlink);
            liveLabel.setLetterSpacing(0f);
            liveLabel.setText(safeUpper(m.getStatus()));
            if (liveDot != null) liveDot.setVisibility(View.VISIBLE);
        }

        format.setText(TextUtils.isEmpty(m.getFormat()) ? "" : m.getFormat());
        format.setVisibility(TextUtils.isEmpty(m.getFormat()) ? View.GONE : View.VISIBLE);

        // Prefer full team names (e.g. "Washington Freedom"), fall back to code.
        String t1 = teamLabel(m.getTeam1_name(), m.getTeam1_code());
        String t2 = teamLabel(m.getTeam2_name(), m.getTeam2_code());
        String line1 = m.scoreLine(t1, m.getTeam1_score(), m.getTeam1_overs());
        String line2 = m.scoreLine(t2, m.getTeam2_score(), m.getTeam2_overs());
        team1.setText(TextUtils.isEmpty(line1) ? t1 : line1);
        team2.setText(TextUtils.isEmpty(line2) ? t2 : line2);

        String st = m.getStatus_text();
        status.setText(TextUtils.isEmpty(st) ? m.getSeries_name() : st);
    }

    private void show() {
        // Don't fight flip-gesture hide — restore path will show after settle.
        if (hiddenForFlipGesture) {
            return;
        }
        if (!miniPlayerEnabled) {
            hide();
            return;
        }
        ensureAttached();
        if (miniView != null && miniView.getVisibility() != View.VISIBLE) {
            miniView.setVisibility(View.VISIBLE);
            miniView.setAlpha(0f);
            miniView.animate().alpha(1f).setDuration(200).start();
        }
        globalIsVisible = true;
    }

    private void hide() {
        stopLiveBadgeBlink();
        if (miniView != null) {
            miniView.setVisibility(View.GONE);
        }
        globalIsVisible = false;
    }

    /**
     * Hide the floating mini-player only for the flip gesture duration.
     * Feature is not removed — restores after {@code false} if it was showing.
     * Idempotent across repeated onFlippedToAppear move callbacks.
     */
    public void setHiddenForFlipGesture(boolean hidden) {
        if (hidden) {
            if (hiddenForFlipGesture) {
                return;
            }
            hiddenForFlipGesture = true;
            visibleBeforeFlipGesture = miniView != null
                    && miniView.getVisibility() == View.VISIBLE;
            if (visibleBeforeFlipGesture && miniView != null) {
                miniView.animate().cancel();
                miniView.setAlpha(1f);
                miniView.setVisibility(View.GONE);
            }
            return;
        }
        if (!hiddenForFlipGesture) {
            return;
        }
        hiddenForFlipGesture = false;
        boolean shouldRestore = visibleBeforeFlipGesture;
        visibleBeforeFlipGesture = false;
        if (shouldRestore
                && miniPlayerEnabled
                && current != null
                && !TextUtils.isEmpty(current.getMatch_id())
                && !dismissedForSession.contains(current.getMatch_id())) {
            show();
        }
    }

    private void detach() {
        stopLiveBadgeBlink();
        if (miniView != null && miniView.getParent() instanceof ViewGroup) {
            ((ViewGroup) miniView.getParent()).removeView(miniView);
        }
        miniView = null;
        attached = false;
    }

    private void stopLiveBadgeBlink() {
        View badge = miniView != null ? miniView.findViewById(R.id.cmp_live_badge) : null;
        liveBadgeBlink = LiveBadgeBlinkHelper.stop(badge, liveBadgeBlink);
    }

    private void dismissWithUndo() {
        final Activity activity = activityRef.get();
        if (current != null) {
            dismissedForSession.add(current.getMatch_id());
        }
        hide();

        if (activity == null) return;
        View anchor = activity.findViewById(android.R.id.content);
        if (anchor == null) return;

        Snackbar bar = Snackbar.make(anchor, "Live match closed", Snackbar.LENGTH_LONG);
        bar.setAction("UNDO", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (current != null && miniPlayerEnabled) {
                    dismissedForSession.remove(current.getMatch_id());
                    show();
                    bind(current);
                }
            }
        });
        try {
            bar.setActionTextColor(activity.getColor(R.color.cmp_brand_orange));
        } catch (Exception ignored) {
        }
        bar.show();
    }

    private void openMatchCenter() {
        Activity activity = activityRef.get();
        if (activity == null || current == null || !miniPlayerEnabled) return;
        incrementOpens();
        Intent intent = new Intent(activity, MatchCenterActivity.class);
        intent.putExtra(MatchCenterActivity.EXTRA_MATCH, current);
        activity.startActivity(intent);
    }

    /**
     * Only Home path that hits {@code cricket_active_match}.
     * Hard-gated by mini master ON + known active mini match (not footer).
     */
    private void refreshFromServer() {
        if (!shouldPollCricket()) return;
        Activity activity = activityRef.get();
        if (activity == null || activity.isFinishing()) return;
        if (pollInFlight) return;
        pollInFlight = true;
        try {
            APIService api = RestApiClient.getClient().create(APIService.class);
            NetworkCallDeduper.enqueueUnique("cricket_active_match_v3", api.cricketActiveMatch("v3"),
                    new Callback<CricketActiveMatchResponse>() {
                @Override
                public void onResponse(@NonNull Call<CricketActiveMatchResponse> call,
                                       @NonNull Response<CricketActiveMatchResponse> response) {
                    pollInFlight = false;
                    if (response.body() != null) {
                        CricketActiveMatchResponse body = response.body();
                        if (body.getMini_player_position() != null) {
                            Activity host = activityRef.get();
                            CricketMiniPlayerPosition pos = body.getMini_player_position();
                            String posJson = String.valueOf(pos.isUseDefault())
                                    + "|" + pos.getXPercent() + "|" + pos.getYPercent();
                            CricketMiniPlayerPositionHelper.save(host, pos);
                            if (attached && miniView != null && host != null
                                    && !posJson.equals(lastAppliedPositionJson)) {
                                lastAppliedPositionJson = posJson;
                                ViewGroup root = host.findViewById(android.R.id.content);
                                if (root != null) positionInitial(root);
                            } else if (lastAppliedPositionJson == null) {
                                lastAppliedPositionJson = posJson;
                            }
                        }
                        // Cache + apply the two independent surface toggles live.
                        Activity host = activityRef.get();
                        if (body.getCricket_mini_player_enabled() != null) {
                            miniPlayerEnabled = body.isMiniPlayerEnabled();
                            CricketSurfaceConfigHelper.saveMiniPlayerEnabled(host, miniPlayerEnabled);
                        }
                        if (body.getCricket_footer_tab_enabled() != null) {
                            footerTabEnabled = body.isFooterTabEnabled();
                            CricketSurfaceConfigHelper.saveFooterTabEnabled(host, footerTabEnabled);
                        }
                        ActiveMatch am = body.getActive_match();
                        boolean hadMatch = am != null && !TextUtils.isEmpty(am.getMatch_id());
                        // Mini OFF or no active mini match → stop all cricket HTTP.
                        if (!miniPlayerEnabled || !hadMatch) {
                            lastPollHadActiveMatch = false;
                            stopPollingCompletely();
                            update(am); // clears UI / footer when null
                            return;
                        }
                        boolean wasActive = lastPollHadActiveMatch;
                        lastPollHadActiveMatch = true;
                        update(am);
                        if (wasActive != lastPollHadActiveMatch
                                && pollingEnabled
                                && shouldPollCricket()) {
                            handler.removeCallbacks(pollRunnable);
                            scheduleNextPoll();
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<CricketActiveMatchResponse> call, @NonNull Throwable t) {
                    pollInFlight = false;
                    // Offline / transient — keep last known state, don't crash.
                    // Stay on current adaptive cadence (do not accelerate).
                }
            });
        } catch (Exception ignored) {
            pollInFlight = false;
        }
    }

    private float dp(float v) {
        Activity a = activityRef.get();
        float density = a != null ? a.getResources().getDisplayMetrics().density : 2.5f;
        return v * density;
    }

    private static float clamp(float v, float min, float max) {
        if (max < min) return min;
        return Math.max(min, Math.min(max, v));
    }

    private static String safeUpper(String s) {
        return s == null ? "" : s.toUpperCase();
    }

    /** Prefer the full team name; fall back to the short code. */
    private static String teamLabel(String name, String code) {
        if (!TextUtils.isEmpty(name) && !name.equalsIgnoreCase(code)) return name;
        return code == null ? "" : code;
    }
}
