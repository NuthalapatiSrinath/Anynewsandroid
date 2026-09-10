package com.anyNews.anynews.Utils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.Nullable;

/**
 * Admin-controlled size + margins for the blue circular on-image Share
 * ({@code R.drawable.ic_share_full}) used on Magazines, Special-tab galleries,
 * and Telugu Calendar Panchangam.
 *
 * <p>Anchor is <b>bottom-end</b> (bottom-right in LTR). Defaults match
 * {@code activity_circular_indicatior.xml}: 48dp size, end≈20, bottom 64,
 * start/top 0. Values arrive via splash + feed poll into SharedPreferences.
 */
public final class MagazineShareIconHelper {

    public static final int DEFAULT_SIZE_DP = 48;
    public static final int DEFAULT_MARGIN_END_DP = 20;
    public static final int DEFAULT_MARGIN_BOTTOM_DP = 64;
    public static final int DEFAULT_MARGIN_START_DP = 0;
    public static final int DEFAULT_MARGIN_TOP_DP = 0;

    private static final int SIZE_MIN_DP = 28;
    private static final int SIZE_MAX_DP = 72;
    private static final int MARGIN_SIDE_MAX_DP = 80;
    private static final int MARGIN_BOTTOM_MAX_DP = 160;

    private MagazineShareIconHelper() {
    }

    /** Persist splash / poll values (already clamped on server; clamp again for safety). */
    public static void save(
            @Nullable Context context,
            @Nullable Integer sizeDp,
            @Nullable Integer marginEndDp,
            @Nullable Integer marginBottomDp,
            @Nullable Integer marginStartDp,
            @Nullable Integer marginTopDp
    ) {
        if (context == null) {
            return;
        }
        if (sizeDp != null) {
            SharedPrefsData.putInt(
                    context,
                    Constants.MAGAZINE_SHARE_ICON_SIZE_DP,
                    clampSize(sizeDp),
                    Constants.PREF_NAME
            );
        }
        if (marginEndDp != null) {
            SharedPrefsData.putInt(
                    context,
                    Constants.MAGAZINE_SHARE_MARGIN_END_DP,
                    clampSideMargin(marginEndDp),
                    Constants.PREF_NAME
            );
        }
        if (marginBottomDp != null) {
            SharedPrefsData.putInt(
                    context,
                    Constants.MAGAZINE_SHARE_MARGIN_BOTTOM_DP,
                    clampBottomMargin(marginBottomDp),
                    Constants.PREF_NAME
            );
        }
        if (marginStartDp != null) {
            SharedPrefsData.putInt(
                    context,
                    Constants.MAGAZINE_SHARE_MARGIN_START_DP,
                    clampSideMargin(marginStartDp),
                    Constants.PREF_NAME
            );
        }
        if (marginTopDp != null) {
            SharedPrefsData.putInt(
                    context,
                    Constants.MAGAZINE_SHARE_MARGIN_TOP_DP,
                    clampSideMargin(marginTopDp),
                    Constants.PREF_NAME
            );
        }
    }

    /**
     * Apply size + margins to an on-post Share ImageView. Safe no-op if null.
     * Until splash/poll writes prefs, leaves XML layout untouched so Magazines /
     * Calendar keep their existing margins.
     *
     * @param extraBottomDp additional bottom inset in dp (unused for now; API kept)
     */
    public static void apply(
            @Nullable Context context,
            @Nullable ImageView shareIcon,
            int extraBottomDp
    ) {
        if (context == null || shareIcon == null) {
            return;
        }

        // Size key is always written after splash (never 0). Until then, keep XML.
        boolean configured = false;
        int sizeDp = DEFAULT_SIZE_DP;
        try {
            int storedSize = SharedPrefsData.getInt(
                    context, Constants.MAGAZINE_SHARE_ICON_SIZE_DP, Constants.PREF_NAME);
            if (storedSize > 0) {
                sizeDp = storedSize;
                configured = true;
            }
        } catch (Exception ignored) {
        }
        if (!configured) {
            return;
        }

        int endDp = DEFAULT_MARGIN_END_DP;
        int bottomDp = DEFAULT_MARGIN_BOTTOM_DP;
        int startDp = DEFAULT_MARGIN_START_DP;
        int topDp = DEFAULT_MARGIN_TOP_DP;
        try {
            endDp = SharedPrefsData.getInt(
                    context, Constants.MAGAZINE_SHARE_MARGIN_END_DP, Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
        try {
            bottomDp = SharedPrefsData.getInt(
                    context, Constants.MAGAZINE_SHARE_MARGIN_BOTTOM_DP, Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
        try {
            startDp = SharedPrefsData.getInt(
                    context, Constants.MAGAZINE_SHARE_MARGIN_START_DP, Constants.PREF_NAME);
        } catch (Exception ignored) {
        }
        try {
            topDp = SharedPrefsData.getInt(
                    context, Constants.MAGAZINE_SHARE_MARGIN_TOP_DP, Constants.PREF_NAME);
        } catch (Exception ignored) {
        }

        sizeDp = clampSize(sizeDp);
        endDp = clampSideMargin(endDp);
        bottomDp = clampBottomMargin(bottomDp);
        startDp = clampSideMargin(startDp);
        topDp = clampSideMargin(topDp);
        if (extraBottomDp != 0) {
            bottomDp = clampBottomMargin(bottomDp + extraBottomDp);
        }

        float density = context.getResources().getDisplayMetrics().density;
        int sizePx = Math.round(sizeDp * density);
        // Keep glyph padding proportional to the current 12dp on 48dp icon.
        int padPx = Math.max(0, Math.round((sizeDp / 4f) * density));

        ViewGroup.LayoutParams lp = shareIcon.getLayoutParams();
        if (lp == null) {
            lp = new ViewGroup.MarginLayoutParams(sizePx, sizePx);
        } else {
            lp.width = sizePx;
            lp.height = sizePx;
        }
        if (lp instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) lp;
            mlp.setMarginStart(Math.round(startDp * density));
            mlp.setMarginEnd(Math.round(endDp * density));
            mlp.topMargin = Math.round(topDp * density);
            mlp.bottomMargin = Math.round(bottomDp * density);
            shareIcon.setLayoutParams(mlp);
        } else {
            shareIcon.setLayoutParams(lp);
        }
        shareIcon.setPadding(padPx, padPx, padPx, padPx);
    }

    public static void apply(@Nullable Context context, @Nullable ImageView shareIcon) {
        apply(context, shareIcon, 0);
    }

    /** Re-apply to known overlay ids under a root (live poll refresh). */
    public static void applyUnder(@Nullable Context context, @Nullable View root) {
        if (context == null || root == null) {
            return;
        }
        int[] ids = new int[] {
                R.id.image_gallary_share,
                R.id.imageViewShare,
                R.id.telugu_calendar_blink_share
        };
        for (int id : ids) {
            View v = root.findViewById(id);
            if (v instanceof ImageView) {
                apply(context, (ImageView) v);
            }
        }
    }

    private static int clampSize(int dp) {
        return Math.max(SIZE_MIN_DP, Math.min(SIZE_MAX_DP, dp));
    }

    private static int clampSideMargin(int dp) {
        return Math.max(0, Math.min(MARGIN_SIDE_MAX_DP, dp));
    }

    private static int clampBottomMargin(int dp) {
        return Math.max(0, Math.min(MARGIN_BOTTOM_MAX_DP, dp));
    }
}
