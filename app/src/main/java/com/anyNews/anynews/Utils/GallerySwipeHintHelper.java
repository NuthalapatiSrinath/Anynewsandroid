package com.anyNews.anynews.Utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import com.anyNews.anynews.R;
import com.anyNews.anynews.flipview.FlipView;

/**
 * Horizontal swipe coach for multi-image feed pages
 * (image_gallery, magazine, half-image gallery).
 *
 * <p>Shows a small localized “Swipe left” label only. ViewPager fake-drag peek was
 * removed — peeking page-1 before page-0 Glide finished left the cover blank/white
 * until the user swiped away and back (same class of bug as FlipView.peakNext).
 */
public final class GallerySwipeHintHelper {

    /** Wait so the first magazine/gallery frame is clearly visible first. */
    private static final long START_DELAY_MS = 650L;
    /** Total coach shows across the install lifetime (unlimited). */
    private static final int MAX_HINT_SHOWS = 999999;
    private static final String PREF_HINT_SHOW_COUNT = "gallery_swipe_hint_show_count";

    private GallerySwipeHintHelper() {}

    /**
     * Attach (or refresh) the swipe hint for a multi-image pager.
     *
     * @param pager     gallery / magazine ViewPager
     * @param hintView  optional pre-inflated label; created if null
     * @param pageCount number of pages — must be &gt;= 2 to show
     */
    public static void bind(@Nullable ViewPager pager,
            @Nullable TextView hintView,
            int pageCount) {
        if (pager == null) {
            return;
        }
        clear(pager);
        installNestedSwipeGuard(pager);

        if (pageCount < 2) {
            if (hintView != null) {
                hintView.setVisibility(View.GONE);
            }
            return;
        }

        TextView label = hintView;
        if (label == null) {
            label = ensureOverlayLabel(pager);
        }
        if (label == null) {
            return;
        }

        Context ctx = pager.getContext();
        label.setText(resolveHint(ctx));
        label.setVisibility(View.GONE);
        label.setAlpha(0f);

        Session session = new Session(pager, label);
        pager.setTag(R.id.tag_gallery_swipe_hint_session, session);
        label.setTag(R.id.tag_gallery_swipe_hint_session, session);
        session.start();
    }

    /** Cancel pending nudge / hide label for this pager (rebinding / recycle). */
    public static void clear(@Nullable ViewPager pager) {
        if (pager == null) {
            return;
        }
        Object tag = pager.getTag(R.id.tag_gallery_swipe_hint_session);
        if (tag instanceof Session) {
            ((Session) tag).cancel();
        }
        pager.setTag(R.id.tag_gallery_swipe_hint_session, null);
    }

    @NonNull
    public static String resolveHint(@Nullable Context context) {
        int languageId = 0;
        if (context != null) {
            try {
                languageId = SharedPrefsData.getInt(
                        context.getApplicationContext(),
                        Constants.LanguageID,
                        Constants.PREF_NAME);
            } catch (Exception ignored) {
            }
        }
        switch (languageId) {
            case 1: // Malayalam
                return "ഇടത്തേക്ക് സ്വൈപ്പ് ചെയ്യുക";
            case 2: // Hindi
                return "बाएँ स्वाइप करें";
            case 3: // Telugu
                if (context != null) {
                    try {
                        return context.getString(R.string.gallery_swipe_left_hint_te);
                    } catch (Exception ignored) {
                    }
                }
                return "ఎడమవైపు స్వైప్ చేయండి";
            case 4: // Tamil
                return "இடதுபுறம் ஸ்வைப் செய்யவும்";
            case 5: // Kannada
                return "ಎಡಕ್ಕೆ ಸ್ವೈಪ್ ಮಾಡಿ";
            case 6: // Odia
                return "ବାମକୁ ସ୍ୱାଇପ୍ କରନ୍ତୁ";
            default:
                if (context != null) {
                    try {
                        return context.getString(R.string.gallery_swipe_left_hint);
                    } catch (Exception ignored) {
                    }
                }
                return "Swipe left";
        }
    }

    private static int hintShowsRemaining(@Nullable Context context) {
        if (context == null) {
            return 0;
        }
        try {
            int shown = SharedPrefsData.getInt(
                    context.getApplicationContext(),
                    PREF_HINT_SHOW_COUNT,
                    Constants.PREF_NAME);
            return Math.max(0, MAX_HINT_SHOWS - shown);
        } catch (Exception e) {
            return 0;
        }
    }

    private static void recordHintShow(@Nullable Context context) {
        if (context == null) {
            return;
        }
        try {
            Context app = context.getApplicationContext();
            int shown = SharedPrefsData.getInt(app, PREF_HINT_SHOW_COUNT, Constants.PREF_NAME);
            if (shown < MAX_HINT_SHOWS) {
                SharedPrefsData.putInt(app, PREF_HINT_SHOW_COUNT, shown + 1, Constants.PREF_NAME);
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Keep parent FlipView from intercepting horizontal gallery drags so pages
     * change instead of the gesture leaking to the footer (Profile tab).
     */
    private static void installNestedSwipeGuard(@NonNull ViewPager pager) {
        Object existing = pager.getTag(R.id.tag_gallery_swipe_guard);
        if (existing instanceof View.OnTouchListener) {
            return;
        }
        View.OnTouchListener guard = new View.OnTouchListener() {
            private float downX;
            private float downY;
            private boolean disallowSet;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        downX = event.getX();
                        downY = event.getY();
                        disallowSet = false;
                        // Stop idle FlipView peak so it cannot claim this gesture.
                        // Do not disallow yet — vertical flips that start on the
                        // gallery must still reach FlipView.
                        cancelParentFlipPeak(v);
                        break;
                    case MotionEvent.ACTION_MOVE: {
                        float dx = event.getX() - downX;
                        float dy = event.getY() - downY;
                        if (!disallowSet && Math.abs(dx) > Math.abs(dy)
                                && Math.abs(dx) > dp(v, 6)) {
                            disallowSet = true;
                            requestParentsDisallow(v, true);
                            cancelParentFlipPeak(v);
                        } else if (!disallowSet && Math.abs(dy) > Math.abs(dx)
                                && Math.abs(dy) > dp(v, 6)) {
                            // Vertical — allow FlipView to flip articles.
                            requestParentsDisallow(v, false);
                        }
                        break;
                    }
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        requestParentsDisallow(v, false);
                        disallowSet = false;
                        break;
                    default:
                        break;
                }
                return false;
            }
        };
        pager.setTag(R.id.tag_gallery_swipe_guard, guard);
        pager.setOnTouchListener(guard);
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

    @Nullable
    private static TextView ensureOverlayLabel(@NonNull ViewPager pager) {
        ViewGroup parent = (ViewGroup) pager.getParent();
        if (parent == null) {
            return null;
        }
        View existing = parent.findViewById(R.id.gallery_swipe_left_hint);
        if (existing instanceof TextView) {
            return (TextView) existing;
        }

        TextView label = new TextView(pager.getContext());
        label.setId(R.id.gallery_swipe_left_hint);
        label.setTextColor(0xFFFFFFFF);
        label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        label.setGravity(Gravity.CENTER);
        label.setPadding(dp(pager, 12), dp(pager, 6), dp(pager, 12), dp(pager, 6));
        label.setBackgroundResource(R.drawable.bg_gallery_dots_pill);
        label.setVisibility(View.GONE);
        label.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);

        if (parent instanceof FrameLayout) {
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
            lp.bottomMargin = dp(pager, 56);
            parent.addView(label, lp);
        } else if (parent instanceof RelativeLayout) {
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
            lp.bottomMargin = dp(pager, 56);
            parent.addView(label, lp);
        } else {
            ViewGroup.MarginLayoutParams lp = new ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.bottomMargin = dp(pager, 56);
            parent.addView(label, lp);
        }
        return label;
    }

    private static int dp(@NonNull View v, float value) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                v.getResources().getDisplayMetrics()));
    }

    private static final class Session implements ViewPager.OnPageChangeListener {
        private final ViewPager pager;
        private final TextView hint;
        private final Handler handler = new Handler(Looper.getMainLooper());
        private boolean dismissed;
        private boolean userSwiped;
        private boolean countedShow;
        @Nullable
        private ViewTreeObserver.OnGlobalLayoutListener layoutListener;

        Session(@NonNull ViewPager pager, @NonNull TextView hint) {
            this.pager = pager;
            this.hint = hint;
        }

        void start() {
            pager.addOnPageChangeListener(this);
            scheduleAfterFirstFrame();
        }

        private void scheduleAfterFirstFrame() {
            if (dismissed) {
                return;
            }
            if (pager.getWidth() > 0 && pager.getHeight() > 0
                    && pager.getChildCount() > 0) {
                handler.postDelayed(this::showLabelOnly, START_DELAY_MS);
                return;
            }
            layoutListener = () -> {
                if (dismissed) {
                    return;
                }
                if (pager.getWidth() > 0 && pager.getHeight() > 0) {
                    removeLayoutListener();
                    handler.postDelayed(this::showLabelOnly, START_DELAY_MS);
                }
            };
            pager.getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);
            // Fallback if layout never fires (recycled / detached).
            handler.postDelayed(() -> {
                if (!dismissed && !countedShow) {
                    removeLayoutListener();
                    showLabelOnly();
                }
            }, START_DELAY_MS + 800L);
        }

        private void removeLayoutListener() {
            if (layoutListener == null) {
                return;
            }
            try {
                pager.getViewTreeObserver().removeOnGlobalLayoutListener(layoutListener);
            } catch (Exception ignored) {
            }
            layoutListener = null;
        }

        void cancel() {
            dismissed = true;
            handler.removeCallbacksAndMessages(null);
            removeLayoutListener();
            try {
                pager.removeOnPageChangeListener(this);
            } catch (Exception ignored) {
            }
            hint.animate().cancel();
            hint.setVisibility(View.GONE);
            hint.setAlpha(0f);
            if (hint.getTag(R.id.tag_gallery_swipe_hint_session) == this) {
                hint.setTag(R.id.tag_gallery_swipe_hint_session, null);
            }
        }

        /** Label coach only — never fakeDrag (that blanked page-0 before Glide finished). */
        private void showLabelOnly() {
            if (dismissed || userSwiped || pager.getAdapter() == null
                    || pager.getAdapter().getCount() < 2
                    || pager.getCurrentItem() != 0) {
                dismiss(true);
                return;
            }
            if (!countedShow) {
                countedShow = true;
                recordHintShow(pager.getContext());
            }
            hint.setVisibility(View.VISIBLE);
            hint.animate()
                    .alpha(1f)
                    .setDuration(220)
                    .start();
            handler.postDelayed(this::finishSoft, 2200L);
        }

        /** Fade label after the nudge loop; tear down listeners for this card bind. */
        private void finishSoft() {
            fadeHintOut();
            dismiss(false);
        }

        private void fadeHintOut() {
            if (hint.getVisibility() != View.VISIBLE) {
                return;
            }
            hint.animate()
                    .alpha(0f)
                    .setDuration(280)
                    .withEndAction(() -> {
                        if (!userSwiped) {
                            hint.setVisibility(View.GONE);
                        }
                    })
                    .start();
        }

        private void dismiss(boolean hide) {
            if (dismissed) {
                if (hide) {
                    hint.animate().cancel();
                    hint.setVisibility(View.GONE);
                    hint.setAlpha(0f);
                }
                return;
            }
            dismissed = true;
            handler.removeCallbacksAndMessages(null);
            removeLayoutListener();
            try {
                pager.removeOnPageChangeListener(this);
            } catch (Exception ignored) {
            }
            if (hide) {
                hint.animate().cancel();
                hint.setVisibility(View.GONE);
                hint.setAlpha(0f);
            }
            if (pager.getTag(R.id.tag_gallery_swipe_hint_session) == this) {
                pager.setTag(R.id.tag_gallery_swipe_hint_session, null);
            }
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            if (position != 0) {
                userSwiped = true;
                dismiss(true);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if (state == ViewPager.SCROLL_STATE_DRAGGING) {
                userSwiped = true;
                dismiss(true);
            }
        }
    }
}
