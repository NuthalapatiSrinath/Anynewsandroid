package com.anyNews.anynews.Utils;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.anyNews.anynews.Pojo.PollSurveyColorsConfig;
import com.anyNews.anynews.R;
import com.google.gson.Gson;

/**
 * Dark polished poll/survey card styling (reference UI).
 * Fixed green / red / blue / orange accents — admin poll_survey_colors are stored
 * but do not override the result-rank / option-index color scheme.
 * <p>
 * Never shows vote counts — callers must bind percentage text only.
 */
public final class PollOptionStyleHelper {

    private static final String TAG = "AnyNewsPollStyle";
    private static final Gson GSON = new Gson();

    /** Option / result accent index: 0=green, 1=red, 2=blue, 3=orange (cycles). */
    public static final int ACCENT_GREEN = 0;
    public static final int ACCENT_RED = 1;
    public static final int ACCENT_BLUE = 2;
    public static final int ACCENT_ORANGE = 3;

    public enum FillMode {
        HIDDEN,
        PERCENT,
        FULL_SELECTED
    }

    private PollOptionStyleHelper() {
    }

    public static void save(Context context, @Nullable PollSurveyColorsConfig config) {
        if (context == null || config == null) {
            return;
        }
        SharedPrefsData.putString(
                context,
                Constants.POLL_SURVEY_COLORS,
                GSON.toJson(config),
                Constants.PREF_NAME
        );
    }

    @Nullable
    public static PollSurveyColorsConfig load(Context context) {
        if (context == null) {
            return null;
        }
        String json = SharedPrefsData.getString(context, Constants.POLL_SURVEY_COLORS, Constants.PREF_NAME);
        if (TextUtils.isEmpty(json)) {
            return null;
        }
        try {
            return GSON.fromJson(json, PollSurveyColorsConfig.class);
        } catch (Exception e) {
            Log.e(TAG, "load parse failed", e);
            return null;
        }
    }

    public static int accentForIndex(int zeroBasedIndex) {
        int i = zeroBasedIndex % 4;
        if (i < 0) {
            i += 4;
        }
        return i;
    }

    /**
     * Poll (2-option) after-vote: highest % → green, lowest % → red.
     * Ties: first option green, second red.
     *
     * @return int[2] accent indices for option 0 and 1
     */
    public static int[] pollRankAccents(float pct0, float pct1) {
        if (pct0 >= pct1) {
            return new int[]{ACCENT_GREEN, ACCENT_RED};
        }
        return new int[]{ACCENT_RED, ACCENT_GREEN};
    }

    @DrawableRes
    public static int optionBg(int accent) {
        switch (accent) {
            case ACCENT_RED:
                return R.drawable.bg_poll_option_red;
            case ACCENT_BLUE:
                return R.drawable.bg_poll_option_blue;
            case ACCENT_ORANGE:
                return R.drawable.bg_poll_option_orange;
            case ACCENT_GREEN:
            default:
                return R.drawable.bg_poll_option_green;
        }
    }

    @DrawableRes
    public static int resultBg(int accent) {
        switch (accent) {
            case ACCENT_RED:
                return R.drawable.bg_poll_result_red;
            case ACCENT_BLUE:
                return R.drawable.bg_poll_result_blue;
            case ACCENT_ORANGE:
                return R.drawable.bg_poll_result_orange;
            case ACCENT_GREEN:
            default:
                return R.drawable.bg_poll_result_green;
        }
    }

    @DrawableRes
    public static int radioEmpty(int accent) {
        switch (accent) {
            case ACCENT_RED:
                return R.drawable.bg_poll_radio_red;
            case ACCENT_BLUE:
                return R.drawable.bg_poll_radio_blue;
            case ACCENT_ORANGE:
                return R.drawable.bg_poll_radio_orange;
            case ACCENT_GREEN:
            default:
                return R.drawable.bg_poll_radio_green;
        }
    }

    @DrawableRes
    public static int radioSelected(int accent) {
        switch (accent) {
            case ACCENT_RED:
                return R.drawable.bg_poll_radio_red_selected;
            case ACCENT_BLUE:
                return R.drawable.bg_poll_radio_blue_selected;
            case ACCENT_ORANGE:
                return R.drawable.bg_poll_radio_orange_selected;
            case ACCENT_GREEN:
            default:
                return R.drawable.bg_poll_radio_green_selected;
        }
    }

    @DrawableRes
    public static int badgeBg(int accent) {
        switch (accent) {
            case ACCENT_RED:
                return R.drawable.bg_poll_badge_red;
            case ACCENT_BLUE:
                return R.drawable.bg_poll_badge_blue;
            case ACCENT_ORANGE:
                return R.drawable.bg_poll_badge_orange;
            case ACCENT_GREEN:
            default:
                return R.drawable.bg_poll_badge_green;
        }
    }

    @DrawableRes
    public static int progressFill(int accent) {
        switch (accent) {
            case ACCENT_RED:
                return R.drawable.bg_poll_progress_red;
            case ACCENT_BLUE:
                return R.drawable.bg_poll_progress_blue;
            case ACCENT_ORANGE:
                return R.drawable.bg_poll_progress_orange;
            case ACCENT_GREEN:
            default:
                return R.drawable.bg_poll_progress_green;
        }
    }

    public static void applyRadio(View radio, int accent, boolean selected) {
        if (radio == null) {
            return;
        }
        radio.setBackgroundResource(selected ? radioSelected(accent) : radioEmpty(accent));
    }

    public static void applyResultRow(
            @Nullable View row,
            @Nullable TextView badge,
            @Nullable TextView label,
            @Nullable TextView percentTv,
            @Nullable View progressFill,
            int accent,
            int badgeNumber,
            @Nullable CharSequence labelText,
            float percent
    ) {
        if (row != null) {
            row.setBackgroundResource(resultBg(accent));
            row.setVisibility(View.VISIBLE);
        }
        if (badge != null) {
            badge.setBackgroundResource(badgeBg(accent));
            badge.setText(String.valueOf(badgeNumber));
            badge.setTextColor(Color.WHITE);
        }
        if (label != null && labelText != null) {
            label.setText(labelText);
            label.setTextColor(Color.WHITE);
        }
        if (percentTv != null) {
            int rounded = Math.round(Math.max(0f, Math.min(100f, percent)));
            percentTv.setText(rounded + "%");
            percentTv.setTextColor(Color.WHITE);
        }
        if (progressFill != null) {
            progressFill.setBackgroundResource(progressFill(accent));
            setProgressWidth(progressFill, percent);
        }
    }

    /**
     * Size the colored fill as {@code percent%} of its track parent.
     * <p>
     * Critical: result rows start as {@code visibility=gone}, so the track parent
     * often has width 0 on the first bind (Special tab already-voted polls). A single
     * {@code post()} that bails when width==0 left bars empty forever while % text
     * still showed. Wait for a real layout measurement before applying width.
     */
    public static void setProgressWidth(@Nullable View fill, float percent) {
        if (fill == null) {
            return;
        }
        final float clamped = Math.max(0f, Math.min(100f, percent));
        final View parent = fill.getParent() instanceof View ? (View) fill.getParent() : null;
        if (parent == null) {
            return;
        }
        // Defer one frame so GONE→VISIBLE has a chance to measure first.
        parent.post(() -> applyProgressWidthWhenReady(fill, parent, clamped, 0));
    }

    private static void applyProgressWidthWhenReady(
            final View fill,
            final View parent,
            final float clamped,
            final int attempt
    ) {
        int parentW = parent.getWidth();
        if (parentW > 0) {
            ViewGroup.LayoutParams lp = fill.getLayoutParams();
            if (lp == null) {
                return;
            }
            int target = Math.round(parentW * (clamped / 100f));
            if (lp.width != target) {
                lp.width = target;
                fill.setLayoutParams(lp);
            }
            fill.setVisibility(View.VISIBLE);
            return;
        }
        if (attempt >= 12) {
            return;
        }
        // Results panel just became VISIBLE — wait for the next layout pass.
        ViewTreeObserver observer = parent.getViewTreeObserver();
        if (observer.isAlive()) {
            observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    ViewTreeObserver obs = parent.getViewTreeObserver();
                    if (obs.isAlive()) {
                        obs.removeOnGlobalLayoutListener(this);
                    }
                    applyProgressWidthWhenReady(fill, parent, clamped, attempt + 1);
                }
            });
        }
        parent.requestLayout();
        // Fallback if global-layout never fires (detached FlipView page).
        parent.postDelayed(
                () -> applyProgressWidthWhenReady(fill, parent, clamped, attempt + 1),
                32L
        );
    }

    /** Format percent for display — never includes vote counts. */
    public static String formatPercent(Object raw) {
        return Math.round(parsePercent(raw)) + "%";
    }

    public static float parsePercent(Object raw) {
        if (raw == null) {
            return 0f;
        }
        try {
            String s = String.valueOf(raw).replace("%", "").trim();
            if (s.isEmpty()) {
                return 0f;
            }
            return Float.parseFloat(s);
        } catch (Exception e) {
            return 0f;
        }
    }

    // --- legacy API kept so older call sites compile; no-ops / thin wrappers ---

    public static int resolveFillColor(Context context) {
        int fallback = ContextCompat.getColor(context, R.color.poll_accent_green);
        PollSurveyColorsConfig cfg = load(context);
        if (cfg == null) {
            return fallback;
        }
        boolean night = isNightMode(context);
        return parseColor(night ? cfg.getDarkFill() : cfg.getLightFill(), fallback);
    }

    public static int resolveTextColor(Context context) {
        return Color.WHITE;
    }

    public static int resolveSurfaceTextColor(Context context) {
        return Color.WHITE;
    }

    public static int resolvePartialFillTextColor(Context context) {
        return Color.WHITE;
    }

    public static void applyPercentFill(
            Context context,
            @Nullable View clip,
            @Nullable View fill,
            float percent,
            boolean show,
            @Nullable TextView... labels
    ) {
        if (fill == null) {
            return;
        }
        if (!show) {
            fill.setVisibility(View.GONE);
            return;
        }
        fill.setVisibility(View.VISIBLE);
        setProgressWidth(fill, percent);
        if (labels != null) {
            for (TextView tv : labels) {
                if (tv != null) {
                    tv.setTextColor(Color.WHITE);
                }
            }
        }
    }

    public static void applyOptionFill(
            Context context,
            @Nullable View clip,
            @Nullable View fill,
            float percent,
            FillMode mode,
            @Nullable TextView... labels
    ) {
        if (mode == FillMode.HIDDEN) {
            if (fill != null) {
                fill.setVisibility(View.GONE);
            }
            return;
        }
        applyPercentFill(context, clip, fill, percent, true, labels);
    }

    public static void styleResultNeomorph(Context context, @Nullable View frame) {
        // No-op: dark card UI no longer uses neumorph frames.
    }

    public static void applySurfaceLabelColors(Context context, @Nullable TextView... labels) {
        if (labels == null) {
            return;
        }
        for (TextView tv : labels) {
            if (tv != null) {
                tv.setTextColor(Color.WHITE);
            }
        }
    }

    private static boolean isNightMode(Context context) {
        int night = context.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        return night == Configuration.UI_MODE_NIGHT_YES;
    }

    private static int parseColor(String raw, int fallback) {
        if (TextUtils.isEmpty(raw)) {
            return fallback;
        }
        try {
            String trimmed = raw.trim();
            if (!trimmed.startsWith("#")) {
                trimmed = "#" + trimmed;
            }
            return Color.parseColor(trimmed);
        } catch (Exception e) {
            return fallback;
        }
    }
}
