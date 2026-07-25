package com.anyNews.anynews.Utils;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.anyNews.anynews.Pojo.PollSurveyColorsConfig;
import com.anyNews.anynews.R;
import com.anyNews.anynews.Ui.NeomorphFrameLayout;
import com.google.gson.Gson;

/**
 * Applies admin poll/survey option colors and percentage fills.
 * Fill is clipped to the rounded option bounds (neumorph corner radius) and
 * sized to {@code percent} of the option width — never spills outside the card.
 */
public final class PollOptionStyleHelper {

    private static final String TAG = "AnyNewsPollStyle";
    private static final Gson GSON = new Gson();

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

    public static int resolveFillColor(Context context) {
        int fallback = ContextCompat.getColor(context, R.color.poll_option_selected_bg);
        PollSurveyColorsConfig cfg = load(context);
        if (cfg == null) {
            return fallback;
        }
        boolean night = isNightMode(context);
        return parseColor(night ? cfg.getDarkFill() : cfg.getLightFill(), fallback);
    }

    public static int resolveTextColor(Context context) {
        int fallback = ContextCompat.getColor(context, R.color.poll_option_selected_text);
        PollSurveyColorsConfig cfg = load(context);
        if (cfg == null) {
            return fallback;
        }
        boolean night = isNightMode(context);
        return parseColor(night ? cfg.getDarkText() : cfg.getLightText(), fallback);
    }

    /**
     * Shows a left-aligned percentage fill inside {@code clip}, clipped to the
     * neumorph corner radius. Pass {@code percent} 0–100. When {@code show} is
     * false the fill is hidden (pre-vote / unselected).
     */
    public static void applyPercentFill(
            Context context,
            @Nullable View clip,
            @Nullable View fill,
            float percent,
            boolean show,
            @Nullable TextView... labels
    ) {
        if (context == null) {
            return;
        }
        int fillColor = resolveFillColor(context);
        int textColor = resolveTextColor(context);

        if (labels != null) {
            for (TextView tv : labels) {
                if (tv != null) {
                    tv.setTextColor(textColor);
                }
            }
        }

        if (fill == null) {
            return;
        }
        if (clip == null && fill.getParent() instanceof View) {
            clip = (View) fill.getParent();
        }
        if (clip == null) {
            return;
        }

        ensureRoundedClip(context, clip);

        if (!show) {
            fill.setVisibility(View.GONE);
            ViewGroupLayoutParamsZeroWidth(fill);
            return;
        }

        final float clamped = Math.max(0f, Math.min(100f, percent));
        fill.setVisibility(View.VISIBLE);
        applyFillColor(fill, fillColor);

        final View clipView = clip;
        clipView.post(() -> {
            int parentW = clipView.getWidth();
            if (parentW <= 0) {
                return;
            }
            int targetW = Math.round(parentW * (clamped / 100f));
            android.view.ViewGroup.LayoutParams lp = fill.getLayoutParams();
            if (lp == null) {
                return;
            }
            lp.width = targetW;
            fill.setLayoutParams(lp);
        });
    }

    /** Surface + normal neumorph chrome so green only comes from the fill view. */
    public static void styleResultNeomorph(Context context, @Nullable NeomorphFrameLayout frame) {
        if (context == null || frame == null) {
            return;
        }
        int surface = ContextCompat.getColor(context, R.color.poll_option_surface);
        int shadow = ContextCompat.getColor(context, R.color.neomorph_shadow_color);
        frame.setBackgroundColor(Color.TRANSPARENT);
        frame.setNeomorphBackgroundColor(surface);
        frame.setNeomorphShadowColor(shadow);
    }

    private static void ensureRoundedClip(Context context, View clip) {
        final float radius = context.getResources().getDimension(R.dimen.neomorph_view_corner_radius);
        clip.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radius);
            }
        });
        clip.setClipToOutline(true);
    }

    private static void applyFillColor(View fill, int color) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(color);
        gd.setShape(GradientDrawable.RECTANGLE);
        fill.setBackground(gd);
    }

    private static void ViewGroupLayoutParamsZeroWidth(View fill) {
        android.view.ViewGroup.LayoutParams lp = fill.getLayoutParams();
        if (lp != null) {
            lp.width = 0;
            fill.setLayoutParams(lp);
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
