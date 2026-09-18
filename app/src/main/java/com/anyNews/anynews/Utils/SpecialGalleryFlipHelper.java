package com.anyNews.anynews.Utils;

import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import com.anyNews.anynews.Adapters.GalleryImageFlipAdapter;
import com.anyNews.anynews.R;
import com.anyNews.anynews.flipview.FlipView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Special-tab multi-image posts: nest a horizontal {@link FlipView} (3D book
 * page-turn) instead of a ViewPager slide carousel. Also owns gesture isolation
 * so parent article FlipView / bottom tabs / system-back edge do not steal the
 * horizontal page-turn.
 */
public final class SpecialGalleryFlipHelper {

    /** True while a nested Special gallery FlipView is actively dragging. */
    private static final AtomicBoolean NESTED_GALLERY_DRIVING = new AtomicBoolean(false);

    private static final long HINT_DELAY_MS = 450L;
    private static final int MAX_HINT_PEAKS = 2;

    private SpecialGalleryFlipHelper() {}

    public static boolean isNestedGalleryDriving() {
        return NESTED_GALLERY_DRIVING.get();
    }

    /** Clear sticky drive flag (leave Special / feed swap / missed CANCEL). */
    public static void clearNestedGalleryDriving() {
        NESTED_GALLERY_DRIVING.set(false);
    }

    /**
     * Hide {@code pager}, inflate horizontal FlipView into {@code host}, bind images.
     *
     * @return the nested FlipView, or null if bind failed (caller should keep ViewPager)
     */
    @Nullable
    public static FlipView bind(
            @NonNull ViewGroup host,
            @Nullable ViewPager pager,
            @Nullable List<String> imageUrls,
            @Nullable TextView hintView,
            @Nullable LinearLayout dotsHost,
            @Nullable Runnable onPageChanged) {
        ArrayList<String> urls = new ArrayList<>();
        if (imageUrls != null) {
            for (String u : imageUrls) {
                if (u != null) {
                    urls.add(u);
                }
            }
        }
        if (urls.isEmpty()) {
            urls.add("");
        }
        // New bind must not inherit a stuck drive flag from a prior gesture.
        NESTED_GALLERY_DRIVING.set(false);

        if (pager != null) {
            try {
                GallerySwipeHintHelper.clear(pager);
            } catch (Exception ignored) {
            }
            pager.setVisibility(View.GONE);
            pager.setAdapter(null);
        }

        FlipView flip = host.findViewById(R.id.gallery_horizontal_flip);
        if (flip == null) {
            try {
                View inflated = LayoutInflater.from(host.getContext())
                        .inflate(R.layout.gallery_horizontal_flip, host, false);
                // Insert behind chrome (dots / hint / share) when possible.
                int insertAt = 0;
                if (pager != null) {
                    int pagerIndex = host.indexOfChild(pager);
                    if (pagerIndex >= 0) {
                        insertAt = pagerIndex;
                    }
                }
                host.addView(inflated, insertAt);
                if (inflated instanceof FlipView) {
                    flip = (FlipView) inflated;
                } else {
                    flip = host.findViewById(R.id.gallery_horizontal_flip);
                }
                // RelativeLayout hosts (magazine / gallery scrren_icon): pin edges so
                // MATCH_PARENT is not treated as wrap at (0,0).
                if (flip != null && host instanceof android.widget.RelativeLayout) {
                    try {
                        android.widget.RelativeLayout.LayoutParams lp =
                                new android.widget.RelativeLayout.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT);
                        lp.addRule(android.widget.RelativeLayout.ALIGN_PARENT_LEFT);
                        lp.addRule(android.widget.RelativeLayout.ALIGN_PARENT_TOP);
                        lp.addRule(android.widget.RelativeLayout.ALIGN_PARENT_RIGHT);
                        lp.addRule(android.widget.RelativeLayout.ALIGN_PARENT_BOTTOM);
                        flip.setLayoutParams(lp);
                    } catch (Exception ignored) {
                    }
                }
            } catch (Exception e) {
                if (pager != null) {
                    pager.setVisibility(View.VISIBLE);
                }
                return null;
            }
        }
        if (flip == null) {
            if (pager != null) {
                pager.setVisibility(View.VISIBLE);
            }
            return null;
        }

        flip.setVisibility(View.VISIBLE);
        // Match article FlipView travel feel.
        try {
            int travel = Math.max(320, host.getWidth() > 0 ? host.getWidth() : 550);
            flip.setFlipDistanceperpage(travel, host.getResources().getDisplayMetrics().density);
        } catch (Exception ignored) {
        }

        GalleryImageFlipAdapter adapter =
                new GalleryImageFlipAdapter(host.getContext(), urls, true);
        flip.setAdapter(adapter);
        syncDots(dotsHost, 0, urls.size());

        final FlipView flipRef = flip;
        final LinearLayout dotsRef = dotsHost;
        final int pageCount = urls.size();
        flip.setOnFlipListener(new FlipView.OnFlipListener() {
            @Override
            public void onFlippedToPage(FlipView v, int position, long id) {
                syncDots(dotsRef, position, pageCount);
                if (onPageChanged != null) {
                    try {
                        onPageChanged.run();
                    } catch (Exception ignored) {
                    }
                }
            }
        });

        bindSwipeHint(flipRef, hintView, pageCount);
        // Guard last so it owns OnTouchListener (also dismisses hint on contact).
        installNestedSwipeGuard(flipRef, hintView);
        return flipRef;
    }

    /** If nested gallery is past page 0, flip back one page (consume system back). */
    public static boolean tryConsumeBack(@Nullable View root) {
        FlipView flip = findNestedGalleryFlip(root);
        if (flip == null || flip.getPageCount() <= 0) {
            return false;
        }
        int cur = flip.getCurrentPage();
        if (cur <= 0) {
            return false;
        }
        try {
            flip.smoothFlipTo(cur - 1);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Nullable
    public static FlipView findNestedGalleryFlip(@Nullable View root) {
        if (root == null) {
            return null;
        }
        View found = root.findViewById(R.id.gallery_horizontal_flip);
        if (found instanceof FlipView && found.getVisibility() == View.VISIBLE) {
            return (FlipView) found;
        }
        return null;
    }

    private static void syncDots(@Nullable LinearLayout dotsHost, int selected, int count) {
        if (dotsHost == null || count <= 1) {
            if (dotsHost != null) {
                dotsHost.removeAllViews();
            }
            return;
        }
        try {
            dotsHost.removeAllViews();
            for (int i = 0; i < count; i++) {
                TextView dot = new TextView(dotsHost.getContext());
                dot.setText(Html.fromHtml("&#9673;"));
                dot.setTextSize(i == selected ? 28 : 16);
                dot.setTextColor(i == selected ? Color.RED : Color.WHITE);
                dotsHost.addView(dot);
            }
        } catch (Exception ignored) {
        }
    }

    private static void bindSwipeHint(
            @NonNull FlipView flip, @Nullable TextView hintView, int pageCount) {
        Object prev = flip.getTag(R.id.tag_gallery_swipe_hint_session);
        if (prev instanceof Handler) {
            ((Handler) prev).removeCallbacksAndMessages(null);
        }
        if (hintView == null || pageCount < 2) {
            if (hintView != null) {
                hintView.setVisibility(View.GONE);
            }
            return;
        }
        hintView.setText(GallerySwipeHintHelper.resolveHint(flip.getContext()));
        hintView.setVisibility(View.GONE);
        hintView.setAlpha(0f);
        final Handler handler = new Handler(Looper.getMainLooper());
        flip.setTag(R.id.tag_gallery_swipe_hint_session, handler);
        final Runnable[] peakStep = new Runnable[1];
        final int[] peaks = {0};
        peakStep[0] = () -> {
            if (flip.getWindowToken() == null || flip.getCurrentPage() != 0) {
                hintView.setVisibility(View.GONE);
                return;
            }
            if (peaks[0] == 0) {
                hintView.setVisibility(View.VISIBLE);
                hintView.animate().alpha(1f).setDuration(200).start();
            }
            if (peaks[0] >= MAX_HINT_PEAKS) {
                hintView.animate().alpha(0f).setDuration(250)
                        .withEndAction(() -> hintView.setVisibility(View.GONE))
                        .start();
                return;
            }
            peaks[0]++;
            try {
                cancelParentFlipPeak(flip);
                flip.peakNext(true);
            } catch (Exception ignored) {
            }
            handler.postDelayed(peakStep[0], 1400L);
        };
        handler.postDelayed(peakStep[0], HINT_DELAY_MS);
    }

    /**
     * Claim horizontal drags for the nested FlipView; release on vertical so the
     * parent article FlipView can still flip posts. Dismisses swipe hint on contact.
     */
    private static void installNestedSwipeGuard(
            @NonNull FlipView flip, @Nullable TextView hintView) {
        View.OnTouchListener guard = new View.OnTouchListener() {
            private float downX;
            private float downY;
            private boolean disallowSet;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getActionMasked();
                if (action == MotionEvent.ACTION_DOWN
                        || action == MotionEvent.ACTION_MOVE) {
                    dismissHint(flip, hintView);
                }
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        downX = event.getX();
                        downY = event.getY();
                        disallowSet = false;
                        NESTED_GALLERY_DRIVING.set(false);
                        cancelParentFlipPeak(v);
                        break;
                    case MotionEvent.ACTION_MOVE: {
                        float dx = event.getX() - downX;
                        float dy = event.getY() - downY;
                        if (!disallowSet && Math.abs(dx) > Math.abs(dy)
                                && Math.abs(dx) > dp(v, 6)) {
                            disallowSet = true;
                            NESTED_GALLERY_DRIVING.set(true);
                            requestParentsDisallow(v, true);
                            cancelParentFlipPeak(v);
                        } else if (!disallowSet && Math.abs(dy) > Math.abs(dx)
                                && Math.abs(dy) > dp(v, 6)) {
                            NESTED_GALLERY_DRIVING.set(false);
                            requestParentsDisallow(v, false);
                        }
                        break;
                    }
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        NESTED_GALLERY_DRIVING.set(false);
                        requestParentsDisallow(v, false);
                        disallowSet = false;
                        break;
                    default:
                        break;
                }
                return false;
            }
        };
        flip.setTag(R.id.tag_gallery_swipe_guard, guard);
        flip.setOnTouchListener(guard);
    }

    private static void dismissHint(@NonNull FlipView flip, @Nullable TextView hintView) {
        Object tag = flip.getTag(R.id.tag_gallery_swipe_hint_session);
        if (tag instanceof Handler) {
            ((Handler) tag).removeCallbacksAndMessages(null);
            flip.setTag(R.id.tag_gallery_swipe_hint_session, null);
        }
        if (hintView != null) {
            hintView.animate().cancel();
            hintView.setVisibility(View.GONE);
            hintView.setAlpha(0f);
        }
    }

    private static void requestParentsDisallow(@NonNull View v, boolean disallow) {
        ViewParent parent = v.getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(disallow);
        }
    }

    private static void cancelParentFlipPeak(@NonNull View v) {
        ViewParent parent = v.getParent();
        while (parent != null) {
            if (parent instanceof FlipView) {
                try {
                    ((FlipView) parent).cancelIdlePeak();
                } catch (Exception ignored) {
                }
                return;
            }
            parent = parent.getParent();
        }
    }

    private static int dp(@NonNull View v, float value) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                v.getResources().getDisplayMetrics()));
    }
}
