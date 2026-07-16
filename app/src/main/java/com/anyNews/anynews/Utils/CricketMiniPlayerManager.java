package com.anyNews.anynews.Utils;

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
 * Adds NO footer / bottom-nav tab — it purely floats above the reading UI.
 */
public class CricketMiniPlayerManager {

    private static final int POLL_MS = 15000;

    // Dismissed match ids for this app process (session). A brand-new active
    // match id will re-show; UNDO clears the id so it comes back immediately.
    private static final Set<String> dismissedForSession = new HashSet<>();

    private final WeakReference<Activity> activityRef;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private View miniView;
    private ActiveMatch current;
    private boolean attached = false;
    private boolean pollingEnabled = true;

    private final Runnable pollRunnable = new Runnable() {
        @Override
        public void run() {
            refreshFromServer();
            handler.postDelayed(this, POLL_MS);
        }
    };

    public CricketMiniPlayerManager(Activity activity) {
        this.activityRef = new WeakReference<>(activity);
    }

    /** Enable/disable background polling of the dedicated endpoint. */
    public void setPollingEnabled(boolean enabled) {
        this.pollingEnabled = enabled;
    }

    public void onResume() {
        if (pollingEnabled) {
            handler.removeCallbacks(pollRunnable);
            handler.postDelayed(pollRunnable, POLL_MS);
        }
    }

    public void onPause() {
        handler.removeCallbacks(pollRunnable);
    }

    public void onDestroy() {
        handler.removeCallbacks(pollRunnable);
        detach();
    }

    /**
     * Main entry point. Called by the host screen on every feed load with the
     * active_match from the feed config (may be null). Hides the player when
     * there is no active match or when it was dismissed this session.
     */
    public void update(ActiveMatch match) {
        Activity activity = activityRef.get();
        if (activity == null || activity.isFinishing()) return;

        if (match == null || TextUtils.isEmpty(match.getMatch_id())) {
            current = null;
            hide();
            return;
        }

        current = match;

        if (dismissedForSession.contains(match.getMatch_id())) {
            hide();
            return;
        }

        show();
        bind(match);
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
                // Start near bottom, offset up so it floats over the reading area.
                float x = parentW - viewW - margin;
                float y = parentH - viewH - dp(140);
                if (x < margin) x = margin;
                if (y < margin) y = margin;
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

        TextView liveBadge = miniView.findViewById(R.id.cmp_live_badge);
        TextView format = miniView.findViewById(R.id.cmp_format);
        TextView team1 = miniView.findViewById(R.id.cmp_team1);
        TextView team2 = miniView.findViewById(R.id.cmp_team2);
        TextView status = miniView.findViewById(R.id.cmp_status);

        boolean live = m.isLive();
        liveBadge.setText(live ? "● LIVE" : "● " + safeUpper(m.getStatus()));

        format.setText(TextUtils.isEmpty(m.getFormat()) ? "" : m.getFormat());
        format.setVisibility(TextUtils.isEmpty(m.getFormat()) ? View.GONE : View.VISIBLE);

        String line1 = m.scoreLine(m.getTeam1_code(), m.getTeam1_score(), m.getTeam1_overs());
        String line2 = m.scoreLine(m.getTeam2_code(), m.getTeam2_score(), m.getTeam2_overs());
        team1.setText(TextUtils.isEmpty(line1) ? m.getTeam1_code() : line1);
        team2.setText(TextUtils.isEmpty(line2) ? m.getTeam2_code() : line2);

        String st = m.getStatus_text();
        status.setText(TextUtils.isEmpty(st) ? m.getSeries_name() : st);
    }

    private void show() {
        ensureAttached();
        if (miniView != null && miniView.getVisibility() != View.VISIBLE) {
            miniView.setVisibility(View.VISIBLE);
            miniView.setAlpha(0f);
            miniView.animate().alpha(1f).setDuration(200).start();
        }
    }

    private void hide() {
        if (miniView != null) {
            miniView.setVisibility(View.GONE);
        }
    }

    private void detach() {
        if (miniView != null && miniView.getParent() instanceof ViewGroup) {
            ((ViewGroup) miniView.getParent()).removeView(miniView);
        }
        miniView = null;
        attached = false;
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
                if (current != null) {
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
        if (activity == null || current == null) return;
        Intent intent = new Intent(activity, MatchCenterActivity.class);
        intent.putExtra(MatchCenterActivity.EXTRA_MATCH, current);
        activity.startActivity(intent);
    }

    private void refreshFromServer() {
        Activity activity = activityRef.get();
        if (activity == null || activity.isFinishing()) return;
        try {
            APIService api = RestApiClient.getClient().create(APIService.class);
            api.cricketActiveMatch("v3").enqueue(new Callback<CricketActiveMatchResponse>() {
                @Override
                public void onResponse(@NonNull Call<CricketActiveMatchResponse> call,
                                       @NonNull Response<CricketActiveMatchResponse> response) {
                    if (response.body() != null) {
                        update(response.body().getActive_match());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<CricketActiveMatchResponse> call, @NonNull Throwable t) {
                    // Offline / transient — keep last known state, don't crash.
                }
            });
        } catch (Exception ignored) {
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
}
