package com.anyNews.anynews.Utils;

import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import com.anyNews.anynews.Adapters.GalleryImageFlipAdapter;
import com.anyNews.anynews.R;
import com.anyNews.anynews.flipview.FlipView;
import com.tbuonomo.viewpagerdotsindicator.BaseDotsIndicator;
import com.tbuonomo.viewpagerdotsindicator.OnPageChangeListenerHelper;
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Special-tab multi-image posts: nest a horizontal {@link FlipView} (3D book
 * page-turn) instead of a ViewPager slide carousel. Also owns gesture isolation
 * so parent article FlipView / bottom tabs / system-back edge do not steal the
 * horizontal page-turn.
 *
 * <p>Dots: Play Store {@link WormDotsIndicator} (gallery_strip) driven via a
 * custom {@link BaseDotsIndicator.Pager} — same widget/animation as July main,
 * not invent-a-TextView dots.
 */
public final class SpecialGalleryFlipHelper {

    /** True while a nested Special gallery FlipView is actively dragging. */
    private static final AtomicBoolean NESTED_GALLERY_DRIVING = new AtomicBoolean(false);

    private static final long HINT_DELAY_MS = 650L;

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
            @Nullable WormDotsIndicator wormDots,
            @Nullable Runnable onPageChanged) {
        ArrayList<String> urls = GalleryImageUrls.sanitize(imageUrls);
        if (urls.isEmpty()) {
            // One transparent seat — never invent a splash-logo "page".
            urls.add("");
        }
        final String fingerprint = GalleryImageUrls.fingerprint(urls);

        // New bind must not inherit a stuck drive flag from a prior gesture —
        // unless the user is mid-swipe on this same nested FlipView (rebind would
        // reset adapter mid-flip → NPE / IndexOOB on left swipe).
        FlipView existing = host.findViewById(R.id.gallery_horizontal_flip);
        if (NESTED_GALLERY_DRIVING.get()
                && existing != null
                && existing.getVisibility() == View.VISIBLE
                && existing.getWindowToken() != null) {
            return existing;
        }
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
        // Opaque black underlay so letterbox / unload never flashes white.
        try {
            flip.setBackgroundColor(0xFF000000);
        } catch (Exception ignored) {
        }
        // Match article FlipView travel feel.
        try {
            int travel = Math.max(320, host.getWidth() > 0 ? host.getWidth() : 550);
            flip.setFlipDistanceperpage(travel, host.getResources().getDisplayMetrics().density);
        } catch (Exception ignored) {
        }

        final FlipView flipRef = flip;
        Object priorFp = flipRef.getTag(R.id.tag_gallery_flip_urls);
        final boolean sameUrls = fingerprint.equals(priorFp)
                && flipRef.getAdapter() instanceof GalleryImageFlipAdapter
                && flipRef.getPageCount() == urls.size();

        if (!sameUrls) {
            // Cancel any in-flight peak / hint before swapping adapter — mid-peak
            // capture of an unloaded page-0 caused blank / half-book black.
            cancelPeakAndHint(flipRef, hintView);
            try {
                flipRef.cancelIdlePeak();
            } catch (Exception ignored) {
            }
            // FIT_XY (useContainFit=false): fill the page like live CardPager fitXY —
            // FIT_CENTER letterboxed posters into a black bar with dots below content.
            GalleryImageFlipAdapter adapter =
                    new GalleryImageFlipAdapter(host.getContext(), urls, false);
            try {
                flipRef.setAdapter(adapter);
                flipRef.setTag(R.id.tag_gallery_flip_urls, fingerprint);
            } catch (Exception e) {
                android.util.Log.e("MagazineCrash", "nested FlipView setAdapter failed: "
                        + e.getMessage());
                if (pager != null) {
                    pager.setVisibility(View.VISIBLE);
                }
                flipRef.setVisibility(View.GONE);
                return null;
            }
            // Land on page 0 after adapter swap — never leave a mid-peak seat.
            try {
                flipRef.flipTo(0);
            } catch (Exception ignored) {
            }
            // First bind often runs before FlipView has non-zero size; Glide wait-for-layout
            // then loses to scrap/detach and page-0 stays on transparent stub until the user
            // leaves and returns. Re-kick cover once laid out and force FlipView redraw.
            schedulePage0Ensure(flipRef, adapter);
        } else if (flipRef.getAdapter() instanceof GalleryImageFlipAdapter) {
            // Same URLs skipped setAdapter — but cover may still be the transparent stub
            // from a cancelled first open. Re-kick until Glide actually paints.
            GalleryImageFlipAdapter priorAdapter =
                    (GalleryImageFlipAdapter) flipRef.getAdapter();
            if (!priorAdapter.isPage0Loaded()) {
                schedulePage0Ensure(flipRef, priorAdapter);
            }
        }

        final int pageCount = urls.size();
        final FlipViewDotsPager dotsPager = bindWormDots(wormDots, flipRef, pageCount);

        flipRef.setOnFlipListener(new FlipView.OnFlipListener() {
            @Override
            public void onFlippedToPage(FlipView v, int position, long id) {
                int safe = position;
                if (safe < 0) safe = 0;
                if (safe >= pageCount) safe = Math.max(0, pageCount - 1);
                if (dotsPager != null) {
                    dotsPager.onFlippedTo(safe);
                }
                if (onPageChanged != null) {
                    try {
                        onPageChanged.run();
                    } catch (Exception ignored) {
                    }
                }
            }
        });

        // Text coach only — never peakNext/book-bob on open (that painted blank /
        // half-black before page-0 Glide finished, and flashed 1–2 times).
        bindSwipeHintLabelOnly(flipRef, hintView, pageCount);
        // Guard last so it owns OnTouchListener (also dismisses hint on contact).
        installNestedSwipeGuard(flipRef, hintView);
        return flipRef;
    }

    /**
     * Drive Play Store {@link WormDotsIndicator} from FlipView page index
     * (same widget as ViewPager {@code setViewPager} path on News).
     */
    @Nullable
    private static FlipViewDotsPager bindWormDots(
            @Nullable WormDotsIndicator wormDots,
            @NonNull FlipView flip,
            int pageCount) {
        if (wormDots == null) {
            return null;
        }
        try {
            wormDots.setVisibility(pageCount > 1 ? View.VISIBLE : View.GONE);
            if (pageCount <= 1) {
                wormDots.setPager(null);
                return null;
            }
            View strip = wormDots.getParent() instanceof View
                    ? (View) wormDots.getParent() : null;
            if (strip != null) {
                strip.setVisibility(View.VISIBLE);
            }
            FlipViewDotsPager pager = new FlipViewDotsPager(flip, pageCount);
            wormDots.setPager(pager);
            wormDots.refreshDots();
            return pager;
        } catch (Exception e) {
            android.util.Log.e("MagazineCrash", "bindWormDots: " + e.getMessage());
            return null;
        }
    }

    /** If nested gallery is past page 0, flip back one page (consume system back). */
    public static boolean tryConsumeBack(@Nullable View root) {
        FlipView flip = findNestedGalleryFlip(root);
        if (flip == null) {
            return false;
        }
        int pageCount;
        try {
            pageCount = flip.getPageCount();
        } catch (Exception e) {
            android.util.Log.e("MagazineCrash", "tryConsumeBack pageCount: " + e.getMessage());
            return false;
        }
        if (pageCount <= 0) {
            return false;
        }
        int cur;
        try {
            cur = flip.getCurrentPage();
        } catch (Exception e) {
            return false;
        }
        if (cur <= 0) {
            return false;
        }
        try {
            flip.smoothFlipTo(Math.max(0, Math.min(cur - 1, pageCount - 1)));
            return true;
        } catch (Exception e) {
            android.util.Log.e("MagazineCrash", "tryConsumeBack smoothFlip: " + e.getMessage());
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

    /**
     * Bridges FlipView → Play Store WormDotsIndicator (library {@link BaseDotsIndicator.Pager}).
     */
    private static final class FlipViewDotsPager implements BaseDotsIndicator.Pager {
        private final FlipView flip;
        private final int count;
        private int current;
        @Nullable
        private OnPageChangeListenerHelper helper;

        FlipViewDotsPager(@NonNull FlipView flip, int count) {
            this.flip = flip;
            this.count = Math.max(0, count);
            int cur = 0;
            try {
                cur = flip.getCurrentPage();
            } catch (Exception ignored) {
            }
            this.current = Math.max(0, Math.min(cur, Math.max(0, count - 1)));
        }

        void onFlippedTo(int position) {
            current = Math.max(0, Math.min(position, Math.max(0, count - 1)));
            if (helper != null) {
                try {
                    helper.onPageScrolled(current, 0f);
                } catch (Exception ignored) {
                }
            }
        }

        @Override
        public boolean isNotEmpty() {
            return count > 0;
        }

        @Override
        public int getCurrentItem() {
            return current;
        }

        @Override
        public boolean isEmpty() {
            return count <= 0;
        }

        @Override
        public int getCount() {
            return count;
        }

        @Override
        public void setCurrentItem(int item, boolean smoothScroll) {
            int safe = Math.max(0, Math.min(item, Math.max(0, count - 1)));
            current = safe;
            try {
                if (smoothScroll) {
                    flip.smoothFlipTo(safe);
                } else {
                    flip.flipTo(safe);
                }
            } catch (Exception e) {
                try {
                    flip.flipTo(safe);
                } catch (Exception ignored) {
                }
            }
        }

        @Override
        public void removeOnPageChangeListener() {
            helper = null;
        }

        @Override
        public void addOnPageChangeListener(
                @NonNull OnPageChangeListenerHelper onPageChangeListenerHelper) {
            helper = onPageChangeListenerHelper;
            try {
                helper.onPageScrolled(current, 0f);
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * After nested FlipView layout, ensure page-0 Glide actually painted and invalidate
     * FlipView (custom dispatchDraw does not always refresh from ImageView alone).
     *
     * <p>Prior double-{@code post} no-op'd when {@code windowToken} was still null (common
     * on first Special open) and never retried — cover stayed on the transparent stub
     * while pages 1+ loaded after size existed. Retry until loaded or attempts exhausted.
     */
    private static void schedulePage0Ensure(
            @NonNull FlipView flip, @NonNull GalleryImageFlipAdapter adapter) {
        final Handler handler = new Handler(Looper.getMainLooper());
        final int[] attempts = {0};
        final int maxAttempts = 6;
        final Runnable[] ensureHolder = new Runnable[1];
        ensureHolder[0] = () -> {
            if (adapter.isPage0Loaded()) {
                try {
                    flip.invalidate();
                } catch (Exception ignored) {
                }
                return;
            }
            if (flip.getWindowToken() == null) {
                if (attempts[0] < maxAttempts) {
                    attempts[0]++;
                    handler.postDelayed(ensureHolder[0], 50L);
                }
                return;
            }
            try {
                adapter.ensurePage0Bound(flip);
                flip.invalidate();
            } catch (Exception e) {
                android.util.Log.e("MagazineCrash", "ensurePage0Bound: " + e.getMessage());
            }
            if (!adapter.isPage0Loaded() && attempts[0] < maxAttempts) {
                attempts[0]++;
                long delay = attempts[0] <= 2 ? 50L : (attempts[0] <= 4 ? 200L : 400L);
                handler.postDelayed(ensureHolder[0], delay);
            }
        };
        final Runnable ensure = ensureHolder[0];
        if (flip.getWidth() > 0 && flip.getHeight() > 0) {
            flip.post(ensure);
            return;
        }
        final ViewTreeObserver.OnGlobalLayoutListener[] layoutListener =
                new ViewTreeObserver.OnGlobalLayoutListener[1];
        layoutListener[0] = () -> {
            if (flip.getWidth() <= 0 || flip.getHeight() <= 0) {
                return;
            }
            try {
                flip.getViewTreeObserver().removeOnGlobalLayoutListener(layoutListener[0]);
            } catch (Exception ignored) {
            }
            ensure.run();
        };
        try {
            flip.getViewTreeObserver().addOnGlobalLayoutListener(layoutListener[0]);
        } catch (Exception e) {
            handler.postDelayed(ensure, 100L);
            return;
        }
        // Fallback if layout never fires (recycled / detached host).
        handler.postDelayed(() -> {
            try {
                flip.getViewTreeObserver().removeOnGlobalLayoutListener(layoutListener[0]);
            } catch (Exception ignored) {
            }
            if (!adapter.isPage0Loaded()) {
                ensure.run();
            }
        }, 300L);
    }

    /** Label-only swipe coach — no FlipView.peakNext (avoids blank/half-black open). */
    private static void bindSwipeHintLabelOnly(
            @NonNull FlipView flip, @Nullable TextView hintView, int pageCount) {
        cancelPeakAndHint(flip, hintView);
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
        handler.postDelayed(() -> {
            if (flip.getWindowToken() == null || flip.getCurrentPage() != 0) {
                return;
            }
            hintView.setVisibility(View.VISIBLE);
            hintView.animate().alpha(1f).setDuration(200).start();
            handler.postDelayed(() -> {
                if (hintView.getWindowToken() == null) {
                    return;
                }
                hintView.animate().alpha(0f).setDuration(250)
                        .withEndAction(() -> hintView.setVisibility(View.GONE))
                        .start();
            }, 2200L);
        }, HINT_DELAY_MS);
    }

    private static void cancelPeakAndHint(@NonNull FlipView flip, @Nullable TextView hintView) {
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
        try {
            flip.cancelIdlePeak();
        } catch (Exception ignored) {
        }
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
        cancelPeakAndHint(flip, hintView);
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
