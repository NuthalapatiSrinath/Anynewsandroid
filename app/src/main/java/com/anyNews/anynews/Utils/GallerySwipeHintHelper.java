package com.anyNews.anynews.Utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import com.anyNews.anynews.R;

/**
 * One-shot horizontal swipe coach for multi-image feed pages
 * (image_gallery, magazine, half-image gallery).
 *
 * <p>Shows a small localized “Swipe left” label and a brief ViewPager
 * fake-drag peek toward the next image. Runs once per card bind until the
 * user starts a horizontal drag (or the hint finishes its short loop).
 * Single-image pagers are left untouched.
 */
public final class GallerySwipeHintHelper {

    private static final long START_DELAY_MS = 650L;
    private static final long NUDGE_DURATION_MS = 560L;
    private static final long NUDGE_GAP_MS = 1200L;
    private static final int MAX_NUDGES = 2;
    private static final float PEEK_DP = 42f;

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
        label.setVisibility(View.VISIBLE);
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
        private final float peekPx;
        private ValueAnimator nudgeAnimator;
        private int nudgeCount;
        private boolean dismissed;
        private boolean userSwiped;
        private float lastFakeDrag;

        Session(@NonNull ViewPager pager, @NonNull TextView hint) {
            this.pager = pager;
            this.hint = hint;
            this.peekPx = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    PEEK_DP,
                    pager.getResources().getDisplayMetrics());
        }

        void start() {
            pager.addOnPageChangeListener(this);
            handler.postDelayed(this::showAndNudge, START_DELAY_MS);
        }

        void cancel() {
            dismissed = true;
            handler.removeCallbacksAndMessages(null);
            stopNudge(false);
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

        private void showAndNudge() {
            if (dismissed || userSwiped || pager.getAdapter() == null
                    || pager.getAdapter().getCount() < 2
                    || pager.getCurrentItem() != 0) {
                dismiss(true);
                return;
            }
            hint.setVisibility(View.VISIBLE);
            hint.animate()
                    .alpha(1f)
                    .setDuration(220)
                    .start();
            runNudge();
        }

        private void runNudge() {
            if (dismissed || userSwiped || nudgeCount >= MAX_NUDGES) {
                if (!userSwiped && !dismissed) {
                    finishSoft();
                }
                return;
            }
            if (pager.getCurrentItem() != 0) {
                dismiss(true);
                return;
            }
            stopNudge(false);
            lastFakeDrag = 0f;
            try {
                if (!pager.isFakeDragging()) {
                    pager.beginFakeDrag();
                }
            } catch (Exception e) {
                finishSoft();
                return;
            }
            nudgeCount++;
            // Peek next (negative = next page in LTR), then spring back to page 0.
            nudgeAnimator = ValueAnimator.ofFloat(0f, -peekPx, 0f);
            nudgeAnimator.setDuration(NUDGE_DURATION_MS);
            nudgeAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            nudgeAnimator.addUpdateListener(animation -> {
                if (dismissed || userSwiped) {
                    return;
                }
                float value = (Float) animation.getAnimatedValue();
                float delta = value - lastFakeDrag;
                lastFakeDrag = value;
                try {
                    if (pager.isFakeDragging()) {
                        pager.fakeDragBy(delta);
                    }
                } catch (Exception ignored) {
                }
            });
            nudgeAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    endFakeDragQuietly();
                    if (dismissed || userSwiped) {
                        return;
                    }
                    if (nudgeCount < MAX_NUDGES) {
                        handler.postDelayed(Session.this::runNudge, NUDGE_GAP_MS);
                    } else {
                        finishSoft();
                    }
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    endFakeDragQuietly();
                }
            });
            nudgeAnimator.start();
        }

        /** Fade label after the nudge loop; tear down listeners for this card bind. */
        private void finishSoft() {
            fadeHintOut();
            dismiss(false);
        }

        private void endFakeDragQuietly() {
            try {
                if (pager.isFakeDragging()) {
                    pager.endFakeDrag();
                }
            } catch (Exception ignored) {
            }
        }

        private void stopNudge(boolean endDrag) {
            if (nudgeAnimator != null) {
                nudgeAnimator.cancel();
                nudgeAnimator = null;
            }
            if (endDrag) {
                endFakeDragQuietly();
            }
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
            stopNudge(true);
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
            if (state == ViewPager.SCROLL_STATE_DRAGGING && !pager.isFakeDragging()) {
                userSwiped = true;
                dismiss(true);
            }
        }
    }
}
