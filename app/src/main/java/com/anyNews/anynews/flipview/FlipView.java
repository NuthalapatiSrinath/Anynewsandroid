package com.anyNews.anynews.flipview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.Scroller;

import androidx.core.view.MotionEventCompat;
import androidx.core.view.VelocityTrackerCompat;

import com.anyNews.anynews.R;
import com.anyNews.anynews.Utils.FlipperAdapter;

import java.util.ArrayList;
import java.util.List;


public class FlipView extends FrameLayout {
    // this will be the postion when there is not data
    private static final int INVALID_PAGE_POSITION = -1;
    // "null" flip distance
    private static final int INVALID_FLIP_DISTANCE = -1;
    private static final int PEAK_ANIM_DURATION = 600;// in ms
    private static final int MAX_SINGLE_PAGE_FLIP_ANIM_DURATION = 300;// in ms
    private static final int MAX_SHADOW_ALPHA = 180;// out of 255
    private static final int MAX_SHADE_ALPHA = 130;// out of 255
    private static final int MAX_SHINE_ALPHA = 100;// out of 255
    // value for no pointer
    private static final int INVALID_POINTER = -1;
    // constant used by the attributes
    private static final int VERTICAL_FLIP = 50;
    // constant used by the attributes
    @SuppressWarnings("unused")
    private static final int HORIZONTAL_FLIP = 1;
    public static ValueAnimator mPeakAnim;
    private final int sideBufferSize = 3;
    private final Interpolator flipInterpolator = new DecelerateInterpolator();
    public boolean mIsFlipping;
    public boolean farword = false;
    //SessionManager session;
    Context mContext;
    /** Android Go / low-RAM: skip full-screen shadow/shade paints during 3D flip. */
    private boolean mLightFlipEffects;
    List<Page> st45 = new ArrayList<Page>();
    int flipdegreeforward = 80, flipdegreebackward = 100;
    boolean st = false;
    boolean st1 = false;
    boolean st2 = false;
    boolean nextflip = true;
    Page p = null;
    boolean str = false;    private final DataSetObserver dataSetObserver = new DataSetObserver() {

        @Override
        public void onChanged() {
            dataSetChanged();
        }

        @Override
        public void onInvalidated() {
            dataSetInvalidated();
        }

    };
    boolean str1 = false;
    // boolean flipnext = true;
    float lt = 0;
    int velocity1 = 0;
    int adaptercount = 0;
    Boolean flipto = false;
    private boolean completeCheck = true;
    private int newPosition;
    private int MAX_SINGLE_PAGE_FLIP_ANIM_DURATION1 = 300;
    // for normalizing width/height
    private int FLIP_DISTANCE_PER_PAGE = 200;
    private Scroller mScroller;
    private final TimeInterpolator mPeakInterpolator = new AccelerateDecelerateInterpolator();
    private boolean mIsFlippingVertically = true;
    private boolean mIsUnableToFlip;
    private boolean mIsFlippingEnabled = true;
    private boolean mLastTouchAllowed = true;
    private int mTouchSlop;
    private boolean mIsOverFlipping;
    // keep track of pointer
    private float mLastX = -1;
    private float mLastY = -1;
    private int mActivePointerId = INVALID_POINTER;
    // velocity stuff
    private VelocityTracker mVelocityTracker;
    private int mMinimumVelocity;
    private int mMaximumVelocity;
    // views get recycled after they have been pushed out of the active queue
    private Recycler mRecycler = new Recycler();
    private ListAdapter mAdapter;
    private int mPageCount = 0;
    private final Page mPreviousPage = new Page();
    private final Page mCurrentPage = new Page();
    private final Page mNextPage = new Page();
    private final Page prvpage = new Page();
    private final Page nrPage = new Page();
    private View mEmptyView;
    private OnFlipListener mOnFlipListener;
    private OnFlipCompleteListener mOnFlipCompleteListener;
    private OnFlipAppeared mOnFlipAppeared;
    private OnOverFlipListener mOnOverFlipListener;
    private float mFlipDistance = INVALID_FLIP_DISTANCE;
    private int mCurrentPageIndex = INVALID_PAGE_POSITION;
    // Must NOT start at 0: page 0 is the first feed/push card. Starting at 0 made
    // `mLastDispatched == mCurrent` on first draw, so onFlippedToPage never fired for
    // the landing article — likes worked, Users/Impressions stayed 0.
    private int mLastDispatchedPageEventIndex = INVALID_PAGE_POSITION;
    private long mCurrentPageId = 0;
    /**
     * Last page the user was actually on. Survives {@link #dataSetChanged()} so a
     * chained notify cannot treat the seat as unknown and snap to the first card.
     * Never overwritten with 0 unless the user is really on page 0.
     */
    private int mAnchorPageIndex = INVALID_PAGE_POSITION;
    private long mAnchorPageId = 0;
    private float mAnchorFlipDistance = INVALID_FLIP_DISTANCE;
    /**
     * Settled seat (index + post_id). Independent of a mid-gesture distance that
     * can explode to 0 on a short FlipView. Never written as 0 unless the user
     * actually landed on page 0.
     */
    private int mCommittedPageIndex = INVALID_PAGE_POSITION;
    private long mCommittedPageId = 0;
    /** Page the current finger-down started on. One gesture may move at most one page. */
    private int mGestureAnchorPage = INVALID_PAGE_POSITION;
    /**
     * Bumped on every dataset change. Scrap may be reused only when this matches
     * the generation the view was painted in — otherwise buffer pages keep the
     * previous article (sticky twin / overlay).
     */
    private int mBindGeneration = 0;
    /** Re-entrant notify from getView during restore must not resolve a wiped index. */
    private int mDataSetChangeDepth = 0;
    private Runnable mRestoreAfterFlip;
    private OverFlipMode mOverFlipMode;
    private OverFlipper mOverFlipper;
    //
    // clipping rects
    private final Rect mTopRect = new Rect();
    private final Rect mBottomRect = new Rect();
    private final Rect mRightRect = new Rect();
    private final Rect mLeftRect = new Rect();
    // used for transforming the canvas
    private final Camera mCamera = new Camera();
    private final Matrix mMatrix = new Matrix();
    // paints drawn above views when flipping
    private final Paint mShadowPaint = new Paint();
    private final Paint mShadePaint = new Paint();
    private final Paint mShinePaint = new Paint();

    public FlipView(Context context) {
        this(context, null);
        mContext = context;
    }

    public FlipView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        mContext = context;
    }

    public FlipView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.FlipView);
        // 0 is vertical, 1 is horizontal
        mIsFlippingVertically = a.getInt(R.styleable.FlipView_orientation,
                VERTICAL_FLIP) == VERTICAL_FLIP;
        setOverFlipMode(OverFlipMode.values()[a.getInt(
                R.styleable.FlipView_overFlipMode, 0)]);
        // a.recycle();
        // DisplayMetrics dm = new DisplayMetrics();
        // ////System.out.println( dm.heightPixels);
        init();
    }

    public int getNewPosition() {
        return newPosition;
    }

    public void setNewPosition(int newPosition) {
        this.newPosition = newPosition;
    }

    public boolean ismIsFlippingEnabled() {
        return mIsFlippingEnabled;
    }

    public void setmIsFlippingEnabled(boolean mIsFlippingEnabled) {
        this.mIsFlippingEnabled = mIsFlippingEnabled;
    }

    public void setFlipDistanceperpage(int distance, float densityofdevice) {
        try {
            if (false) {
                if (densityofdevice == 1.5f && distance > 500) {
                    FLIP_DISTANCE_PER_PAGE = 225;
                } else if (densityofdevice <= 2f && distance > 500) {
                    FLIP_DISTANCE_PER_PAGE = 225;
                    MAX_SINGLE_PAGE_FLIP_ANIM_DURATION1 = 375;
                } else if (densityofdevice <= 3f && distance > 500) {
                    FLIP_DISTANCE_PER_PAGE = 225;
                    MAX_SINGLE_PAGE_FLIP_ANIM_DURATION1 = 375;
                } else if (densityofdevice <= 4f && distance > 500) {
                    FLIP_DISTANCE_PER_PAGE = 225;
                    MAX_SINGLE_PAGE_FLIP_ANIM_DURATION1 = 375;
                } else {
                    FLIP_DISTANCE_PER_PAGE = distance;
                }
            } else {
                if (distance > 600) {
                    FLIP_DISTANCE_PER_PAGE = 550;
                } else {
                    FLIP_DISTANCE_PER_PAGE = distance;
                }


            }
//        if (densityofdevice == 1.5f) {
//            MAX_SINGLE_PAGE_FLIP_ANIM_DURATION1 = 400;
//            flipdegreeforward = 70;
//            flipdegreebackward = 110;
//        }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init() {
        final Context context = getContext();
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mScroller = new Scroller(context, flipInterpolator);
        mTouchSlop = configuration.getScaledTouchSlop();
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        mShadowPaint.setColor(Color.BLACK);
        mShadowPaint.setStyle(Style.FILL);
        mShadePaint.setColor(Color.BLACK);
        mShadePaint.setStyle(Style.FILL);
        mShinePaint.setColor(Color.BLACK);
        mShinePaint.setStyle(Style.FILL);
        mLightFlipEffects = shouldUseLightFlipEffects(context);
    }

    private static boolean shouldUseLightFlipEffects(Context context) {
        try {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (am != null && am.isLowRamDevice()) {
                return true;
            }
            // Extra guard for Go-class phones that report ~2–3GB usable RAM.
            ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
            if (am != null) {
                am.getMemoryInfo(mi);
                if (mi.totalMem > 0 && mi.totalMem < 3L * 1024L * 1024L * 1024L) {
                    return true;
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    public void dataSetChanged() {
        if (mAdapter == null) {
            return;
        }
        // getView during restore can notify again. The outer call already captured
        // the user's seat — a nested pass that sees INVALID index used to snap home.
        if (mDataSetChangeDepth > 0) {
            try {
                mPageCount = mAdapter.getCount();
            } catch (Exception ignored) {
            }
            return;
        }
        mDataSetChangeDepth++;
        try {
            rememberFlipAnchor();
            // Any notify invalidates painted scrap. Same-position scrap must not
            // be returned as-is when the post_id at that index changed.
            mBindGeneration++;
            if (mRecycler != null) {
                mRecycler.invalidateScraps();
            }
            final boolean driving = isUserDrivingFlip();
            int restore = resolvePageAfterDataChange();
            newPosition = restore;
            mPageCount = mAdapter.getCount();
            if (mPageCount < 1) {
                // Intentional clear (tab switch) may reset. A mid-flip empty must
                // not wipe the page id — the next append would then land on card 0.
                if (!driving) {
                    clearFlipAnchor();
                    clearCommittedSeat();
                    mFlipDistance = INVALID_FLIP_DISTANCE;
                    mCurrentPageIndex = INVALID_PAGE_POSITION;
                    mCurrentPageId = -1;
                    mLastDispatchedPageEventIndex = INVALID_PAGE_POSITION;
                    setFlipDistance(0);
                }
                updateEmptyStatus();
                return;
            }
            if (restore == INVALID_PAGE_POSITION) {
                restore = fallbackPageNeverHomeUnlessOnZero();
            } else if (restore > mPageCount - 1) {
                restore = mPageCount - 1;
            } else if (restore < 0) {
                restore = 0;
            }
            newPosition = restore;
            // Never recycle or snap while the user is still swiping. A recycle here
            // aborted the gesture and the next notify treated the seat as unknown.
            if (driving) {
                rememberFlipAnchor();
                scheduleRestoreAfterFlip();
                invalidate();
                updateEmptyStatus();
                return;
            }
            // Apply the restored page BEFORE layout. Do not clear index, flip
            // distance, or page id first — that window was the snap-to-first bug.
            // Same page: keep last-dispatched so we do not re-fire onFlippedToPage(0).
            setFlipDistance(restore * (float) FLIP_DISTANCE_PER_PAGE, true);
            updateEmptyStatus();
        } finally {
            mDataSetChangeDepth--;
        }
    }

    /**
     * Feed append / soft-merge grow only. Updates {@link #mPageCount} and may bind
     * a newly available off-screen next page — never recycles the current visible
     * card. {@link #dataSetChanged()} → {@code setFlipDistance(..., forceRebind=true)}
     * is the blink path; callers must use this for normal pagination append.
     */
    public void pageCountGrewQuietly() {
        if (mAdapter == null) {
            return;
        }
        final int oldCount = mPageCount;
        int newCount;
        try {
            newCount = mAdapter.getCount();
        } catch (Exception e) {
            return;
        }
        if (newCount < 0) {
            newCount = 0;
        }
        mPageCount = newCount;
        rememberFlipAnchor();
        if (isUserDrivingFlip()) {
            // Count is already live for the next settle; do not recycle mid-gesture.
            invalidate();
            updateEmptyStatus();
            return;
        }
        if (newCount <= oldCount) {
            // In-place soft-merge refresh with no new pages — keep painted card.
            invalidate();
            updateEmptyStatus();
            return;
        }
        try {
            final int cur = bestKnownPageIndex();
            if (cur >= 0 && cur < mPageCount - 1) {
                // First time a next page exists after being on the former last card,
                // or next buffer was never filled — bind off-screen only.
                if (!mNextPage.valid
                        || mNextPage.position != cur + 1) {
                    if (!isSamePaintedArticle(cur + 1, cur)) {
                        if (mNextPage.valid && mNextPage.v != null) {
                            try {
                                removeView(mNextPage.v);
                                mRecycler.addScrapView(mNextPage.v, mNextPage.position,
                                        mNextPage.viewType, mNextPage.boundItemId,
                                        mNextPage.boundGeneration);
                            } catch (Exception ignored) {
                            }
                            mNextPage.valid = false;
                        }
                        fillPageForIndex(mNextPage, cur + 1, (cur + 1) % 3);
                        addFlipChild(mNextPage.v);
                    }
                }
            }
        } catch (Exception ignored) {
        }
        invalidate();
        updateEmptyStatus();
    }

    /**
     * Page to show after a dataset change. Never defaults to 0 when the user was
     * further into the feed. Duplicate post_ids (sticky + chrono) use the match
     * nearest the flip seat, not the first list occurrence.
     */
    private int resolvePageAfterDataChange() {
        final int count;
        try {
            count = mAdapter.getCount();
        } catch (Exception e) {
            return INVALID_PAGE_POSITION;
        }
        if (count < 1) {
            return INVALID_PAGE_POSITION;
        }
        final int reference = bestKnownPageIndex();
        final long pageId = bestKnownPageId();
        if (reference != INVALID_PAGE_POSITION) {
            final int clamped = Math.max(0, Math.min(count - 1, reference));
            if (pageId == 0 || pageId == -1) {
                return clamped;
            }
            try {
                if (mAdapter.getItemId(clamped) == pageId) {
                    return clamped;
                }
            } catch (Exception ignored) {
                return clamped;
            }
            int nearest = nearestIndexForPageId(pageId, reference, count);
            if (nearest != INVALID_PAGE_POSITION) {
                return nearest;
            }
            return clamped;
        }
        if (pageId != 0 && pageId != -1) {
            int nearest = nearestIndexForPageId(pageId, INVALID_PAGE_POSITION, count);
            if (nearest != INVALID_PAGE_POSITION) {
                return nearest;
            }
        }
        return fallbackPageNeverHomeUnlessOnZero();
    }

    /**
     * When the seat is unknown, stay on the last committed page. Page 0 is only
     * legal if the user actually landed there (or has never landed anywhere).
     */
    private int fallbackPageNeverHomeUnlessOnZero() {
        if (mCommittedPageIndex > 0) {
            return mCommittedPageIndex;
        }
        if (mAnchorPageIndex > 0) {
            return mAnchorPageIndex;
        }
        if (mLastDispatchedPageEventIndex > 0) {
            return mLastDispatchedPageEventIndex;
        }
        if (isActuallyOnFirstPage()) {
            return 0;
        }
        return 0;
    }

    private int nearestIndexForPageId(long pageId, int origin, int count) {
        int best = INVALID_PAGE_POSITION;
        int bestDist = Integer.MAX_VALUE;
        for (int i = 0; i < count; i++) {
            long id;
            try {
                id = mAdapter.getItemId(i);
            } catch (Exception e) {
                continue;
            }
            if (id != pageId) {
                continue;
            }
            // No flip seat: do not prefer index 0 (sticky twin). Prefer the later
            // copy so a lost index cannot send the user home.
            final int dist = origin == INVALID_PAGE_POSITION
                    ? (count - i)
                    : Math.abs(i - origin);
            if (dist < bestDist) {
                bestDist = dist;
                best = i;
            }
        }
        return best;
    }

    private int bestKnownPageIndex() {
        // A wiped or exploded distance of 0 must not beat a committed mid-feed seat.
        if (mCurrentPageIndex > 0) {
            return mCurrentPageIndex;
        }
        if (mCurrentPageIndex == 0 && isActuallyOnFirstPage()) {
            return 0;
        }
        int fromDistance = pageIndexFromDistance(mFlipDistance);
        if (fromDistance > 0) {
            return fromDistance;
        }
        if (mCommittedPageIndex > 0) {
            return mCommittedPageIndex;
        }
        if (mAnchorPageIndex > 0) {
            return mAnchorPageIndex;
        }
        int fromAnchor = pageIndexFromDistance(mAnchorFlipDistance);
        if (fromAnchor > 0) {
            return fromAnchor;
        }
        if (mLastDispatchedPageEventIndex > 0) {
            return mLastDispatchedPageEventIndex;
        }
        if (mCurrentPageIndex == 0 || fromDistance == 0 || mCommittedPageIndex == 0
                || mAnchorPageIndex == 0) {
            return 0;
        }
        return INVALID_PAGE_POSITION;
    }

    private int pageIndexFromDistance(float distance) {
        if (distance == INVALID_FLIP_DISTANCE || distance < 0 || FLIP_DISTANCE_PER_PAGE < 1) {
            return INVALID_PAGE_POSITION;
        }
        return Math.round(distance / (float) FLIP_DISTANCE_PER_PAGE);
    }

    /**
     * True only when flip distance and the settled seat both say page 0.
     * A 0 index left behind by a short-screen fling is not "on the first card".
     */
    private boolean isActuallyOnFirstPage() {
        if (mFlipDistance != INVALID_FLIP_DISTANCE
                && mFlipDistance >= FLIP_DISTANCE_PER_PAGE * 0.5f) {
            return false;
        }
        if (mCurrentPageIndex > 0) {
            return false;
        }
        // A mid-feed seat must not be treated as page 0 just because distance exploded.
        if (mCommittedPageIndex > 1 && mCurrentPageIndex == 0) {
            return false;
        }
        return mCurrentPageIndex == 0
                || mCommittedPageIndex == 0
                || (mCurrentPageIndex == INVALID_PAGE_POSITION
                && mCommittedPageIndex <= 0
                && (mFlipDistance == INVALID_FLIP_DISTANCE || mFlipDistance < 1f));
    }

    private long bestKnownPageId() {
        if (mCurrentPageId != 0 && mCurrentPageId != -1) {
            return mCurrentPageId;
        }
        if (mCommittedPageId != 0 && mCommittedPageId != -1) {
            return mCommittedPageId;
        }
        return mAnchorPageId;
    }

    private void rememberFlipAnchor() {
        int page = INVALID_PAGE_POSITION;
        if (mCurrentPageIndex != INVALID_PAGE_POSITION && mCurrentPageIndex >= 0) {
            page = mCurrentPageIndex;
        } else if (mFlipDistance != INVALID_FLIP_DISTANCE && mFlipDistance >= 0) {
            page = Math.round(mFlipDistance / (float) FLIP_DISTANCE_PER_PAGE);
        }
        if (page > 0) {
            mAnchorPageIndex = page;
            mCommittedPageIndex = page;
        } else if (page == 0 && isActuallyOnFirstPage()) {
            mAnchorPageIndex = 0;
            mCommittedPageIndex = 0;
        }
        if (mCurrentPageId != 0 && mCurrentPageId != -1) {
            mAnchorPageId = mCurrentPageId;
            if (mCommittedPageIndex == page || mCommittedPageIndex < 0) {
                mCommittedPageId = mCurrentPageId;
            }
        }
        if (mFlipDistance != INVALID_FLIP_DISTANCE && mFlipDistance >= 0
                && (mFlipDistance > 0 || page == 0)) {
            mAnchorFlipDistance = mFlipDistance;
        }
    }

    private void clearCommittedSeat() {
        mCommittedPageIndex = INVALID_PAGE_POSITION;
        mCommittedPageId = 0;
        mGestureAnchorPage = INVALID_PAGE_POSITION;
    }

    private void clearFlipAnchor() {
        mAnchorPageIndex = INVALID_PAGE_POSITION;
        mAnchorPageId = 0;
        mAnchorFlipDistance = INVALID_FLIP_DISTANCE;
        if (mRestoreAfterFlip != null) {
            removeCallbacks(mRestoreAfterFlip);
            mRestoreAfterFlip = null;
        }
    }

    /** Re-apply a dataset change once the swipe/fling has stopped. */
    private void scheduleRestoreAfterFlip() {
        if (mRestoreAfterFlip != null) {
            return;
        }
        mRestoreAfterFlip = new Runnable() {
            @Override
            public void run() {
                if (isUserDrivingFlip()) {
                    postDelayed(this, 32L);
                    return;
                }
                mRestoreAfterFlip = null;
                dataSetChanged();
            }
        };
        postDelayed(mRestoreAfterFlip, 32L);
    }

    /**
     * Page to keep after a recycle. {@code -1} only when the user has never landed.
     */
    public int getAnchoredPage() {
        if (mCurrentPageIndex > 0) {
            return mCurrentPageIndex;
        }
        if (mCurrentPageIndex == 0 && isActuallyOnFirstPage()) {
            return 0;
        }
        return bestKnownPageIndex();
    }

    private void dataSetInvalidated() {
        if (mAdapter != null) {
            mAdapter.unregisterDataSetObserver(dataSetObserver);
            mAdapter = null;
        }
        mRecycler = new Recycler();
        removeAllViews();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(0, widthMeasureSpec);
        int height = getDefaultSize(0, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void measureChildren(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(0, widthMeasureSpec);
        int height = getDefaultSize(0, heightMeasureSpec);
        int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(width,
                MeasureSpec.EXACTLY);
        int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height,
                MeasureSpec.EXACTLY);
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            measureChild(child, childWidthMeasureSpec, childHeightMeasureSpec);
        }
    }

    @Override
    protected void measureChild(View child, int parentWidthMeasureSpec,
                                int parentHeightMeasureSpec) {
        child.measure(parentWidthMeasureSpec, parentHeightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        layoutChildren();
        mTopRect.top = 0;
        mTopRect.left = 0;
        mTopRect.right = getWidth();
        mTopRect.bottom = getHeight() / 2;
        mBottomRect.top = getHeight() / 2;
        mBottomRect.left = 0;
        mBottomRect.right = getWidth();
        mBottomRect.bottom = getHeight();
        mLeftRect.top = 0;
        mLeftRect.left = 0;
        mLeftRect.right = getWidth() / 2;
        mLeftRect.bottom = getHeight();
        mRightRect.top = 0;
        mRightRect.left = getWidth() / 2;
        mRightRect.right = getWidth();
        mRightRect.bottom = getHeight();
    }

    private void layoutChildren() {
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            layoutChild(child);
        }
    }

    private void layoutChild(View child) {
        child.layout(0, 0, getWidth(), getHeight());
    }

    private void setFlipDistance(float flipDistance) {
        setFlipDistance(flipDistance, false);
    }

    /**
     * @param forceRebind recycle even when the page index is unchanged (dataset
     *                    refresh). Never used to pass through an invalid index —
     *                    callers must set the target page first.
     */
    private void setFlipDistance(float flipDistance, boolean forceRebind) {
        try {
            if (mPageCount < 1) {
                rememberFlipAnchor();
                if (!isUserDrivingFlip()) {
                    clearFlipAnchor();
                    mFlipDistance = 0;
                    mCurrentPageIndex = INVALID_PAGE_POSITION;
                    mCurrentPageId = -1;
                }
                recycleActiveViews();
                return;
            }

            if (Float.isNaN(flipDistance) || Float.isInfinite(flipDistance)) {
                int keep = bestKnownPageIndex();
                if (keep < 0) {
                    keep = 0;
                }
                flipDistance = keep * (float) Math.max(1, FLIP_DISTANCE_PER_PAGE);
            }
            /*
             * if (flipDistance == mFlipDistance) { return; }
             */
            mFlipDistance = flipDistance;
            // ////System.out.println(mFlipDistance + "   :::mFlipDistance");
            int currentPageIndex = Math.round(mFlipDistance
                    / (float) Math.max(1, FLIP_DISTANCE_PER_PAGE));
            if (currentPageIndex < 0) {
                currentPageIndex = 0;
            } else if (currentPageIndex > mPageCount - 1) {
                currentPageIndex = mPageCount - 1;
            }
            int unclampedPage = currentPageIndex;
            currentPageIndex = clampPageAgainstExplodedDistance(currentPageIndex);
            if (currentPageIndex != unclampedPage && currentPageIndex >= 0) {
                mFlipDistance = currentPageIndex * (float) Math.max(1, FLIP_DISTANCE_PER_PAGE);
            }
            // if(mCurrentPageIndex==currentPageIndex){
            // return;
            // }
            final boolean sameIndexForceRebind = forceRebind
                    && mCurrentPageIndex == currentPageIndex;
            View pinLiveExoHost = null;
            if (sameIndexForceRebind && mCurrentPage.valid
                    && FlipperAdapter.isPinnedLiveExoHost(mCurrentPage.v)) {
                // Likes/progress notify must not recycle the live video card into scrap —
                // neighbor fills steal that scrap and setPlayer moves the TextureView (blink).
                pinLiveExoHost = mCurrentPage.v;
            }
            if (forceRebind || mCurrentPageIndex != currentPageIndex) {
                mCurrentPageIndex = currentPageIndex;
                try {
                    long id = mAdapter.getItemId(mCurrentPageIndex);
                    if (id != 0 && id != -1) {
                        mCurrentPageId = id;
                    }
                } catch (Exception ignored) {
                }
                rememberFlipAnchor();
                // TODO be smarter about this. Dont remove a view that will be added
                // again on the next line.
                // ////System.out.println("before recycleActiveViews  "
                // + //System.currentTimeMillis());
                recycleActiveViews(pinLiveExoHost);
                // ////System.out.println("After recycleActiveViews  "
                // + //System.currentTimeMillis());
                // add the new active views
                if (mCurrentPageIndex > 0
                        && !isSamePaintedArticle(mCurrentPageIndex - 1, mCurrentPageIndex)) {
                    fillPageForIndex(mPreviousPage, mCurrentPageIndex - 1,
                            (mCurrentPageIndex - 1) % 3);
                    addFlipChild(mPreviousPage.v);
                }
                if (mCurrentPageIndex >= 0 && mCurrentPageIndex < mPageCount) {
                    if (pinLiveExoHost != null) {
                        // Chrome-only rebind on the same tree — PlayerView stays attached.
                        mCurrentPage.position = mCurrentPageIndex;
                        try {
                            mCurrentPage.viewType = mAdapter.getItemViewType(mCurrentPageIndex);
                        } catch (Exception ignored) {
                            mCurrentPage.viewType = 0;
                        }
                        View rebound = null;
                        try {
                            rebound = mAdapter.getView(mCurrentPageIndex, pinLiveExoHost, this);
                        } catch (Exception ignored) {
                        }
                        mCurrentPage.v = rebound != null ? rebound : pinLiveExoHost;
                        stampBoundIdentity(mCurrentPage);
                        mCurrentPage.valid = true;
                    } else {
                        fillPageForIndex(mCurrentPage, mCurrentPageIndex, (mCurrentPageIndex) % 3);
                    }
                    addFlipChild(mCurrentPage.v);
                }
                if (mCurrentPageIndex < mPageCount - 1
                        && !isSamePaintedArticle(mCurrentPageIndex + 1, mCurrentPageIndex)) {
                    fillPageForIndex(mNextPage, mCurrentPageIndex + 1,
                            (mCurrentPageIndex + 1) % 3);
                    addFlipChild(mNextPage.v);
                    // ////System.out.println("aaaaa adview  "
                    // + //System.currentTimeMillis());
                    /*
                     * setViewscrap(mCurrentPageIndex + 2);
                     * setViewscrap(mCurrentPageIndex + 3);
                     * setViewscrap(mCurrentPageIndex + 4);
                     * setViewscrap(mCurrentPageIndex + 5);
                     */
                }

            } else {
                rememberFlipAnchor();
                invalidate();
                if (-mFlipDistance < 140) {
                    if (mOnOverFlipListener != null) {
                        mOnOverFlipListener.flipfinsh();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // invalidate();
    }

    private void fillPageForIndex(Page p, int i, int l) {
        // if (st45.get(i) != null) {
        p.position = i;
        // p.viewType = mAdapter.getItemViewType(st45.get(i).position);
        // p.v = st45.get(i).v;
        // p.valid = true;
        // }
       
        // p.position = i;
        // if(l==0){
       
        //
        // ////System.out.println(st45.get(0).v);
        // p.viewType = mAdapter.getItemViewType(st45.get(0).position);
        // p.v = st45.get(0).v;
        // }else if(l==1){
        p.viewType = mAdapter.getItemViewType(p.position);
        p.v = getView(p.position, p.viewType);
        stampBoundIdentity(p);
        //
        // }
        // else {
        
        // p.viewType = mAdapter.getItemViewType(st45.get(1).position);
        // p.v = st45.get(1).v;
        
        // }
        // else{
        // p.viewType = mAdapter.getItemViewType(st45.get(1).position);
        
        // ////System.out.println(st45.get(1).v);
        // p.v = st45.get(1).v;
        // }
        // ////System.out.println("sssssss  :::" + l + "   "
        // + //System.currentTimeMillis());
        p.valid = true;
        // } else {
        // //
        // p.position = i;
        // p.viewType = st45.get(i).viewType;
        // p.v = st45.get(i).v;
        // p.valid = true;
        // }
    }

    private Page fillPageForIndex(Page p, int i) {
        p.position = i;
        p.viewType = mAdapter.getItemViewType(p.position);
        p.v = getView(p.position, p.viewType);
        stampBoundIdentity(p);
        p.valid = true;
        prvpage.v = p.v;
        prvpage.viewType = p.viewType;
        prvpage.valid = true;
        return p;
    }

    public void recycleActiveViews() {
        recycleActiveViews(null);
    }

    /**
     * @param pinKeep when non-null, leave this view attached (do not scrap). Used to
     *                keep a live ExoPlayer TextureView across same-seat notify rebinds.
     */
    public void recycleActiveViews(View pinKeep) {
        // remove and recycle the currently active views
        if (mPreviousPage.valid) {
            if (mPreviousPage.v != pinKeep) {
                removeView(mPreviousPage.v);
                mRecycler.addScrapView(mPreviousPage.v, mPreviousPage.position,
                        mPreviousPage.viewType, mPreviousPage.boundItemId,
                        mPreviousPage.boundGeneration);
            }
            mPreviousPage.valid = false;
        }
        if (mCurrentPage.valid) {
            if (mCurrentPage.v != pinKeep) {
                removeView(mCurrentPage.v);
                mRecycler.addScrapView(mCurrentPage.v, mCurrentPage.position,
                        mCurrentPage.viewType, mCurrentPage.boundItemId,
                        mCurrentPage.boundGeneration);
            }
            mCurrentPage.valid = false;
        }
        if (mNextPage.valid) {
            if (mNextPage.v != pinKeep) {
                removeView(mNextPage.v);
                mRecycler.addScrapView(mNextPage.v, mNextPage.position,
                        mNextPage.viewType, mNextPage.boundItemId,
                        mNextPage.boundGeneration);
            }
            mNextPage.valid = false;
        }
    }

    public void setViewscrap(int index) {
        // Unused prefetch. Always rebind — a valid scrap is not a painted page.
        getView(index, 0);
    }

    /**
     * Buffer pages must not keep a previously painted article.
     * Same-position scrap is a convertView only when its bound post_id and
     * bind generation still match. Otherwise {@link ListAdapter#getView} rebinds.
     */
    private View getView(int index, int viewType) {
        int type = viewType;
        if (mAdapter != null) {
            try {
                type = mAdapter.getItemViewType(index);
            } catch (Exception ignored) {
                type = viewType;
            }
        }
        Recycler.Scrap scrap = mRecycler.getScrapView(index, type);
        final long want = itemIdAt(index);
        // Never hand the live Exo host to a neighbor seat — that moves setPlayer mid-play.
        if (scrap != null && scrap.v != null
                && FlipperAdapter.isPinnedLiveExoHost(scrap.v)
                && FlipperAdapter.getPinnedLiveExoPosition() != index) {
            mRecycler.addScrapView(scrap.v, FlipperAdapter.getPinnedLiveExoPosition(), type,
                    scrap.boundItemId, scrap.boundGeneration);
            scrap = null;
        }
        if (canReturnScrapWithoutRebind(scrap, want) && !isActivePageView(scrap.v)) {
            return scrap.v;
        }
        View convert = scrap == null ? null : scrap.v;
        if (convert != null && isActivePageView(convert)) {
            convert = null;
        }
        if (convert != null && FlipperAdapter.isPinnedLiveExoHost(convert)
                && FlipperAdapter.getPinnedLiveExoPosition() != index) {
            mRecycler.addScrapView(convert, FlipperAdapter.getPinnedLiveExoPosition(), type,
                    scrap != null ? scrap.boundItemId : 0L,
                    scrap != null ? scrap.boundGeneration : 0);
            convert = null;
        }
        View v = null;
        try {
            if (mAdapter != null) {
                v = mAdapter.getView(index, convert, this);
            }
        } catch (Exception ignored) {
        }
        if (v == null) {
            v = convert;
        }
        if (v != null && isActivePageView(v)) {
            try {
                v = mAdapter.getView(index, null, this);
            } catch (Exception ignored) {
            }
        }
        return v;
    }

    private boolean canReturnScrapWithoutRebind(Recycler.Scrap scrap, long want) {
        if (scrap == null || !scrap.valid || scrap.v == null) {
            return false;
        }
        if (scrap.boundGeneration != mBindGeneration) {
            return false;
        }
        if (want == 0L || want == -1L) {
            return false;
        }
        return scrap.boundItemId == want;
    }

    private boolean isActivePageView(View v) {
        if (v == null) return false;
        return (mPreviousPage.valid && mPreviousPage.v == v)
                || (mCurrentPage.valid && mCurrentPage.v == v)
                || (mNextPage.valid && mNextPage.v == v);
    }

    private void stampBoundIdentity(Page p) {
        if (p == null) return;
        p.boundItemId = itemIdAt(p.position);
        p.boundGeneration = mBindGeneration;
    }

    /** Sticky + timeline twins share a post_id. Do not paint both buffer pages. */
    private boolean isSamePaintedArticle(int index, int other) {
        long a = itemIdAt(index);
        long b = itemIdAt(other);
        if (a == 0L || a == -1L || b == 0L || b == -1L) {
            return false;
        }
        return a == b;
    }

    private long itemIdAt(int index) {
        if (mAdapter == null || index < 0) return 0L;
        try {
            return mAdapter.getItemId(index);
        } catch (Exception ignored) {
            return 0L;
        }
    }

    /** Never attach a view twice — that painted the same card on two buffer pages. */
    private void addFlipChild(View v) {
        if (v == null) return;
        android.view.ViewParent parent = v.getParent();
        if (parent == this) {
            return;
        }
        if (parent instanceof android.view.ViewGroup) {
            ((android.view.ViewGroup) parent).removeView(v);
        }
        addView(v);
    }

    private View getView1(int index, int viewType) {
        // get the scrap from the recycler corresponding to the correct view
        // type
        // get a view from the adapter if a scrap was not found or it is
        // invalid.
        View v = null;
        v = mAdapter.getView(index, null, this);
        /*
         * mAdapter.getView(index+1, scrap == null ? null : scrap.v, this);
         * mAdapter.getView(index+2, scrap == null ? null : scrap.v, this);
         * mAdapter.getView(index+3, scrap == null ? null : scrap.v, this);
         */
        // return view
        return v;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        MainActivity.youTubeView.setVisibility(View.INVISIBLE);
        if (!mIsFlippingEnabled) {
            return false;
        }
        if (mPageCount < 1) {
            return false;
        }
        final int action = ev.getAction() & MotionEvent.ACTION_MASK;
        if (action == MotionEvent.ACTION_CANCEL
                || action == MotionEvent.ACTION_UP) {
            mIsFlipping = false;
            mIsUnableToFlip = false;
            mActivePointerId = INVALID_POINTER;
            if (mVelocityTracker != null) {
                mVelocityTracker.recycle();
                mVelocityTracker = null;
            }
            return false;
        }
        if (action != MotionEvent.ACTION_DOWN) {
            if (mIsFlipping) {
                return true;
            } else if (mIsUnableToFlip) {
                return false;
            }
        }
        switch (action) {
            case MotionEvent.ACTION_MOVE:
                final int activePointerId = mActivePointerId;
                if (activePointerId == INVALID_POINTER) {
                    break;
                }
                final int pointerIndex = MotionEventCompat.findPointerIndex(ev,
                        activePointerId);
        
                if (pointerIndex == -1) {
                    mActivePointerId = INVALID_POINTER;
                    break;
                }
                final float x = MotionEventCompat.getX(ev, pointerIndex);
                final float dx = x - mLastX;
                final float xDiff = Math.abs(dx);
                final float y = MotionEventCompat.getY(ev, pointerIndex);
                final float dy = y - mLastY;
                final float yDiff = Math.abs(dy);
                if ((mIsFlippingVertically && yDiff > mTouchSlop && yDiff > xDiff)
                        || (!mIsFlippingVertically && xDiff > mTouchSlop && xDiff > yDiff)) {
                    // Let NestedScrollView / ScrollView consume vertical drag when
                    // content can still scroll in that direction (raasi lucky panel).
                    if (mIsFlippingVertically && canCurrentPageScrollVertically(dy < 0 ? 1 : -1)) {
                        mIsUnableToFlip = true;
                        endPeak();
                    } else {
                        endPeak();
                        mIsFlipping = true;
                        rememberGestureAnchor();
                        mLastX = x;
                        mLastY = y;
                    }
                } else if ((mIsFlippingVertically && xDiff > mTouchSlop)
                        || (!mIsFlippingVertically && yDiff > mTouchSlop)) {
                    // Horizontal drag on a vertical FlipView: yield to nested gallery
                    // ViewPagers (magazine / image_gallery). Idle peakNext must not keep
                    // claiming the gesture — that stole swipes and misfired footer Profile.
                    mIsUnableToFlip = true;
                    endPeak();
                }
        
                break;
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                mLastX = MotionEventCompat.getX(ev, 0);
                mLastY = MotionEventCompat.getY(ev, 0);
                // Idle peakNext() must NOT claim DOWN — magazine ViewPagers need the
                // gesture. Only an in-flight scroller means we are already flipping.
                mIsFlipping = !mScroller.isFinished();
                mIsUnableToFlip = false;
                mLastTouchAllowed = true;
        
                break;
            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;
        }
        if (!mIsFlipping) {
            trackVelocity(ev);
        }
        return mIsFlipping;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!mIsFlippingEnabled) {
            return false;
        }
        if (mPageCount < 1) {
            return false;
        }
        if (!mIsFlipping && !mLastTouchAllowed) {
            return false;
        }
        final int action = ev.getAction();
		mLastTouchAllowed = action != MotionEvent.ACTION_UP
				&& action != MotionEvent.ACTION_CANCEL
				&& action != MotionEvent.ACTION_OUTSIDE;
        trackVelocity(ev);
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                // Interrupt in-flight flip scroll, but do not treat canceling idle
                // peakNext as "start flipping" — that blocked horizontal gallery swipes.
                clearGestureAnchor();
                boolean endedScroll = endScroll();
                endPeak();
                if (endedScroll) {
                    mIsFlipping = true;
                    rememberGestureAnchor();
                }
                // Remember where the motion event started
                mLastX = ev.getX();
                mLastY = ev.getY();
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                break;
            case MotionEvent.ACTION_MOVE:
                float yDiff = 0f;
                str = false;
                str1 = false;
                if (!mIsFlipping) {
                    final int pointerIndex = MotionEventCompat.findPointerIndex(ev,
                            mActivePointerId);
                    if (pointerIndex == -1) {
                        mActivePointerId = INVALID_POINTER;
                        break;
                    }
                    final float x = MotionEventCompat.getX(ev, pointerIndex);
                    final float xDiff = Math.abs(x - mLastX);
                    final float y = MotionEventCompat.getY(ev, pointerIndex);
                    yDiff = Math.abs(y - mLastY);
                    if ((mIsFlippingVertically && yDiff > mTouchSlop && yDiff > xDiff)
                            || (!mIsFlippingVertically && xDiff > mTouchSlop && xDiff > yDiff)) {
                        float moveDy = y - mLastY;
                        if (mIsFlippingVertically
                                && canCurrentPageScrollVertically(moveDy < 0 ? 1 : -1)) {
                            // Child still scrolling — do not start page flip.
                        } else {
                            endPeak();
                            mIsFlipping = true;
                            rememberGestureAnchor();
                            mLastX = x;
                            mLastY = y;
                        }
                    }
                }
                // if (yDiff > 300) {
                if (mIsFlipping) {
                    // flipnext = false;
                    yDiff = 0;
                    // Scroll to follow the motion event
                    final int activePointerIndex = MotionEventCompat
                            .findPointerIndex(ev, mActivePointerId);
                    if (activePointerIndex == -1) {
                        mActivePointerId = INVALID_POINTER;
                        break;
                    }
                    final float x = MotionEventCompat.getX(ev, activePointerIndex);
                    final float deltaX = mLastX - x;
                    final float y = MotionEventCompat.getY(ev, activePointerIndex);
                    final float deltaY = mLastY - y;
                    mLastX = x;
                    mLastY = y;
                    float deltaFlipDistance = 0;
                    if (mIsFlippingVertically) {
                        deltaFlipDistance = deltaY;
                    } else {
                        deltaFlipDistance = deltaX;
                    }
                    // if (mFlipDistance <= -30) {
                    // mFlipDistance = 0;
                    // }
                    final int maxFlipDistance = (mPageCount - 1)
                            * FLIP_DISTANCE_PER_PAGE;
                    if (mFlipDistance > (maxFlipDistance + 30)) {
                        mFlipDistance = maxFlipDistance;
                    }
//                    //System.out.println("maxFlipDistance  " + maxFlipDistance);
//                    //System.out.println("MFLIPDISTANCE LIBRARY " + mFlipDistance);
                    /*
                     * if(mFlipDistance<=-50){ mFlipDistance=0; }
                     * if(deltaFlipDistance<=0){ deltaFlipDistance=(float) 0.22; }
                     */
                    /*
                     * if(mFlipDistance>=120){ mFlipDistance=0; }
                     */
//                    //System.out.println(getHeight());
//                    //System.out.println(FLIP_DISTANCE_PER_PAGE);
//                    //System.out.println(getHeight() / FLIP_DISTANCE_PER_PAGE);
//                    //System.out.println("xxxxxx        :: " + ((isFlippingVertically()
//                            ? getHeight()
//                            : getWidth()) / FLIP_DISTANCE_PER_PAGE));
//                    //System.out.println("mLastX        :: " + mLastX);
                    deltaFlipDistance /= pageTravelScale();
                    // One motion event cannot skip the feed or slam distance to 0/end.
                    // Small phones used to divide by 0 here (short FlipView) and explode.
                    deltaFlipDistance = clampFlipDelta(deltaFlipDistance);
//                    //System.out.println("mFlipDistance   ::" + mFlipDistance);
//                    //System.out.println("deltaFlipDistance   ::" + deltaFlipDistance);
					farword = deltaFlipDistance < 0;
//                    //System.out.println("before setFlipdistance  "
//                            + //System.currentTimeMillis());
//                    //System.out.println("total  "
//                            + (mFlipDistance % FLIP_DISTANCE_PER_PAGE) + "   "
//                            + deltaFlipDistance);
//                    //System.out.println("aaaaaaaaaa     "
//                            + (mFlipDistance + deltaFlipDistance)
//                            % FLIP_DISTANCE_PER_PAGE);
                    // if((mFlipDistance +
                    // deltaFlipDistance)%FLIP_DISTANCE_PER_PAGE)
                    // flipnext
                    // if(flipnext){
                    setFlipDistance(mFlipDistance + deltaFlipDistance);
                    deltaFlipDistance = deltaFlipDistance;
                    // }
                    ///nullpointer exception   java.lang.IllegalStateException: The specified child already has a parent. You must call removeView() on the child's parent first.
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(400, mMaximumVelocity);
                    if (isFlippingVertically()) {
                        if (mOnFlipAppeared != null) {
                            mOnFlipAppeared.onFlippedToAppear(FlipView.this);
                        }
                      
                        velocity1 = (int) VelocityTrackerCompat.getYVelocity(
                                velocityTracker, mActivePointerId);
                    } else {
                      
                        velocity1 = (int) VelocityTrackerCompat.getXVelocity(
                                velocityTracker, mActivePointerId);
                    }
                    final int minFlipDistance = 0;
                    // mIsFlipping=false;
                    /*
                     * final int maxFlipDistance = (mPageCount - 1)
                     * FLIP_DISTANCE_PER_PAGE;
                     */
                    final boolean isOverFlipping = mFlipDistance < minFlipDistance
                            || mFlipDistance > maxFlipDistance;
//                    //System.out.println("mFlipDistance ::" + mFlipDistance);
//                    //System.out.println("minFlipDistance ::" + minFlipDistance);
//                    //System.out.println("maxFlipDistance ::" + maxFlipDistance);
                    if (isOverFlipping) {
                        mIsOverFlipping = true;
                        setFlipDistance(mOverFlipper.calculate(mFlipDistance,
                                minFlipDistance, maxFlipDistance));
                        if (mOnOverFlipListener != null) {
                           // //System.out.println("mOnOverFlipListener      ");
                            float overFlip = mOverFlipper.getTotalOverFlip();
                            // if (overFlip >= 70) {
                            // overFlip = 0;
                            // }
                     //       //System.out.println("OVERFLIP IN LIBRARY  " + overFlip);
                            /*
                             * // * else{ overFlip=-29; }
                             */
                            mOnOverFlipListener.onOverFlip(this, mOverFlipMode,
                                    overFlip < 0, Math.abs(overFlip),
                                    FLIP_DISTANCE_PER_PAGE);
                            //System.out.println("FLIP_DISTANCE_PER_PAGE         " + FLIP_DISTANCE_PER_PAGE);
                            //System.out.println("overFlip  " + overFlip);
                        }
                    } else if (mIsOverFlipping) {
                        mIsOverFlipping = false;
                        if (mOnOverFlipListener != null) {
                            // TODO in the future should only notify flip
                            // distance 0
                            // on the correct edge (previous/next)
                            //System.out.println("mIsOverFlipping      ");
                            mOnOverFlipListener.onOverFlip(this, mOverFlipMode,
                                    false, 0, FLIP_DISTANCE_PER_PAGE);
                            mOnOverFlipListener.onOverFlip(this, mOverFlipMode,
                                    true, 0, FLIP_DISTANCE_PER_PAGE);
                        }
                    }
                }
                // }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                // str=true;
                if (mIsFlipping) {
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                    int velocity = 0;
                    if (isFlippingVertically()) {
                        velocity = (int) VelocityTrackerCompat.getYVelocity(
                                velocityTracker, mActivePointerId);
                    } else {
                        velocity = (int) VelocityTrackerCompat.getXVelocity(
                                velocityTracker, mActivePointerId);
                    }
                    //System.out.println("velocity   :::" + velocity);
                    //System.out.println("farword   :::" + farword);
                    if (velocity < 0) {
                        str = true;
                    } else {
                        str1 = true;
                    }
                    smoothFlipTo(clampLandingPage(getNextPage(velocity)));
                    clearGestureAnchor();
                    mActivePointerId = INVALID_POINTER;
                    endFlip();
                    mOverFlipper.overFlipEnded();

                }
                completeCheck = true;
                break;
            case MotionEventCompat.ACTION_POINTER_DOWN: {
                final int index = MotionEventCompat.getActionIndex(ev);
                final float x = MotionEventCompat.getX(ev, index);
                final float y = MotionEventCompat.getY(ev, index);
                mLastX = x;
                mLastY = y;
                mActivePointerId = MotionEventCompat.getPointerId(ev, index);
                break;
            }
            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                final int index = MotionEventCompat.findPointerIndex(ev,
                        mActivePointerId);
                final float x = MotionEventCompat.getX(ev, index);
                final float y = MotionEventCompat.getY(ev, index);
                mLastX = x;
                mLastY = y;
                break;
        }
        if (mActivePointerId == INVALID_POINTER) {
            mLastTouchAllowed = false;
        }
        return true;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        try {
            if (mPageCount < 1) {
                return;
            }
            if (!mScroller.isFinished() && mScroller.computeScrollOffset()) {
                //Log.e("TRACKING","SINGLE TRACKING dispatchDraw 1 ");
                setFlipDistance(mScroller.getCurrY());

            }
            if (mIsFlipping || !mScroller.isFinished() || mPeakAnim != null) {
                showAllPages();
                drawPreviousHalf(canvas);
                drawNextHalf(canvas);
                drawFlippingHalf(canvas);
            } else {
                if (mOnFlipCompleteListener != null && completeCheck) {
                    completeCheck = false;
                    mOnFlipCompleteListener.OnFlipCompletedListener(FlipView.this);
                }
                endScroll();
                commitSettledPage();
                setDrawWithLayer(mCurrentPage.v, false);
                hideOtherPages(mCurrentPage);
                if (mCurrentPage.v != null) {
                    drawChild(canvas, mCurrentPage.v, 0);
                }
                // dispatch listener event now that we have "landed" on a page.
                // TODO not the prettiest to have this with the drawing logic,
                // should change.
                /*if (mLastDispatchedPageEventIndex != mCurrentPageIndex) {
                    mLastDispatchedPageEventIndex = mCurrentPageIndex;
                    //Log.e("TRACKING","SINGLE TRACKING dispatchDraw 3 "+mCurrentPageIndex);
                    postFlippedToPage(mCurrentPageIndex);
                }else{
                    //Log.e("TRACKING","SINGLE TRACKING dispatchDraw 4 "+mCurrentPageIndex);
                }*/
            }
            if (mLastDispatchedPageEventIndex != mCurrentPageIndex) {
                mLastDispatchedPageEventIndex = mCurrentPageIndex;
//                Log.e("TRACKING","SINGLE TRACKING dispatchDraw 3 "+mCurrentPageIndex);
                postFlippedToPage(mCurrentPageIndex);

            }
            // if overflip is GLOW mode and the edge effects needed drawing, make
            // sure to invalidate
            if (mOverFlipper.draw(canvas)) {
                // always invalidate whole screen as it is needed 99% of the time.
                // This is because of the shadows and shines put on the non-flipping
                // pages
                invalidate();
            }
        } catch (Exception e) {
            e.printStackTrace();

        }

    }

    private void hideOtherPages(Page p) {
        try {
            if (mPreviousPage.v != null) {
                if (mPreviousPage != p && mPreviousPage.valid
                        && mPreviousPage.v.getVisibility() != GONE) {
                    mPreviousPage.v.setVisibility(GONE);
                }
            }
            if (mCurrentPage.v != null) {
                if (mCurrentPage != p && mCurrentPage.valid
                        && mCurrentPage.v.getVisibility() != GONE) {
                    mCurrentPage.v.setVisibility(GONE);
                }
            }
            if (mNextPage.v != null) {
                if (mNextPage != p && mNextPage.valid
                        && mNextPage.v.getVisibility() != GONE) {
                    mNextPage.v.setVisibility(GONE);
                }
            }
            if (p != null && p.v != null) {
                p.v.setVisibility(VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAllPages() {
        try {
            if (mPreviousPage.v != null) {
                if (mPreviousPage.valid && mPreviousPage.v.getVisibility() != VISIBLE) {
                    mPreviousPage.v.setVisibility(VISIBLE);
                }
            }
            if (mCurrentPage.v != null) {
                if (mCurrentPage.valid && mCurrentPage.v.getVisibility() != VISIBLE) {
                    mCurrentPage.v.setVisibility(VISIBLE);
                }
            }
            if (mNextPage.v != null) {
                if (mNextPage.valid && mNextPage.v.getVisibility() != VISIBLE) {
                    mNextPage.v.setVisibility(VISIBLE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * draw top/left half
     *
     * @param canvas
     */
    private void drawPreviousHalf(Canvas canvas) {
        canvas.save();
        canvas.clipRect(isFlippingVertically() ? mTopRect : mLeftRect);
        final float degreesFlipped = getDegreesFlipped();
        final Page p = degreesFlipped > 90 ? mPreviousPage : mCurrentPage;
        // if the view does not exist, skip drawing it
        if (p.valid) {
            // ////System.out.println("drawPreviousHalf   ");
            setDrawWithLayer(p.v, true);
            drawChild(canvas, p.v, 0);
        }
        drawPreviousShadow(canvas);
        canvas.restore();
    }

    /**
     * draw top/left half shadow
     *
     * @param canvas
     */
    private void drawPreviousShadow(Canvas canvas) {
        if (mLightFlipEffects) {
            return;
        }
        final float degreesFlipped = getDegreesFlipped();
        if (degreesFlipped > 90) {
            final int alpha = (int) (((degreesFlipped - 90) / 90f) * MAX_SHADOW_ALPHA);
            mShadowPaint.setAlpha(alpha);
            canvas.drawPaint(mShadowPaint);
        }
    }

    /**
     * draw bottom/right half
     *
     * @param canvas
     */
    private void drawNextHalf(Canvas canvas) {
        canvas.save();
        canvas.clipRect(isFlippingVertically() ? mBottomRect : mRightRect);
        final float degreesFlipped = getDegreesFlipped();
        final Page p = degreesFlipped > 90 ? mCurrentPage : mNextPage;
        // ////System.out.println("degreesFlipped11 degreesFlipped11  :::: "
        // + degreesFlipped);
        // if the view does not exist, skip drawing it
        if (p.valid) {
            // ////System.out.println("drawNextHalf");
            setDrawWithLayer(p.v, true);
            drawChild(canvas, p.v, 0);
        }
        drawNextShadow(canvas);
        canvas.restore();
    }

    /**
     * draw bottom/right half shadow
     *
     * @param canvas
     */
    private void drawNextShadow(Canvas canvas) {
        if (mLightFlipEffects) {
            return;
        }
        final float degreesFlipped = getDegreesFlipped();
        if (degreesFlipped <= 90) {
            final int alpha = (int) ((Math.abs(degreesFlipped - 90) / 90f) * MAX_SHADOW_ALPHA);
            mShadowPaint.setAlpha(alpha);
            canvas.drawPaint(mShadowPaint);
        }
    }

    private void drawFlippingHalf(Canvas canvas) {
        canvas.save();
        mCamera.save();
        final float degreesFlipped = getDegreesFlipped();
        // lt = degreesFlipped;
        // if (degreesFlipped > 178 && degreesFlipped < 179) {
        // flipnext = true;
        // }
//        //Log.e("TRACKING","SINGLE TRACKING drawFlippingHalf ");
        // ////System.out.println("degreesFlipped55        "+degreesFlipped);
        if (degreesFlipped > 90) {
            canvas.clipRect(isFlippingVertically() ? mTopRect : mLeftRect);
            if (mIsFlippingVertically) {
                if (!farword) {
                    if (str == true) {
                        if (degreesFlipped < 100) {
                            mCamera.rotateX(-90);
                            // mCamera.rotateX(degreesFlipped - 180);
                            // ////System.out.println("degreesFlipped 1   " +
                            // degreesFlipped);
                            // str=false;
                        } else {
                            mCamera.rotateX(degreesFlipped - 180);
                        }

                    } else {
                        mCamera.rotateX(degreesFlipped - 180);
                    }
                } else {
                    // ////System.out.println("degreesFlipped    ::" +
                    // degreesFlipped
                    // + "   ::::  " + str);
                    // if(str1==true){
                    // if(degreesFlipped>110){
                    // mCamera.rotateX(degreesFlipped-110);
                    // }
                    // }
                    // else{
                    // ////System.out.println("degreesFlipped 2    " +
                    // degreesFlipped);
                    // mCamera.rotateX(degreesFlipped - 180);
                    if (str1 == true) {
                        if (degreesFlipped < 100) {
                            mCamera.rotateX(-90);
                        } else {
                            mCamera.rotateX(degreesFlipped - 180);
                        }
                    } else {
                        //////System.out.println("stat   :::" + velocity1);
                        if (degreesFlipped < 100) {
                            if (velocity1 > 650) {
                                mCamera.rotateX(90);
                            } else {
                                mCamera.rotateX(degreesFlipped - 180);
                            }
                        } else {
                            mCamera.rotateX(degreesFlipped - 180);
                        }
                    }
                    // }
                }

            } else {
                mCamera.rotateY(180 - degreesFlipped);

            }
        } else {
            // ////System.out.println("drawFlippingHalf  " + degreesFlipped);
            canvas.clipRect(isFlippingVertically() ? mBottomRect : mRightRect);
            // ////System.out.println("farwordfarwordfarword1    ::" + farword);
            if (mIsFlippingVertically) {
                if (!farword) {
                    if (str == true) {
                        if (degreesFlipped > 80) {
                            mCamera.rotateX(-90);
                            // mCamera.rotateX(degreesFlipped);
                            // ////System.out.println("degreesFlipped 1   " +
                            // degreesFlipped);
                            // str=false;
                        } else {
                            mCamera.rotateX(degreesFlipped);
                        }

                    } else {
                        if (degreesFlipped > 80) {
                            // ////System.out.println("stat   :::"+velocity1);
                            if (-velocity1 > 650) {
                                mCamera.rotateX(-90);
                            } else {
                                mCamera.rotateX(degreesFlipped);
                            }
                            // ////System.out.println("degreesFlipped 1   " +
                            // degreesFlipped);
                            // str=false;
                        } else {
                            mCamera.rotateX(degreesFlipped);
                        }
                        // mCamera.rotateX(degreesFlipped);
                    }
                } else {
                    if (str1 == true) {
                        if (degreesFlipped > 80) {
                            mCamera.rotateX(90);
                        } else {
                            mCamera.rotateX(degreesFlipped);
                        }
                    } else {
                        mCamera.rotateX(degreesFlipped);
                    }
                }
            } else {
                mCamera.rotateY(-degreesFlipped);
            }
        }
        mCamera.getMatrix(mMatrix);
        positionMatrix();
        canvas.concat(mMatrix);
        setDrawWithLayer(mCurrentPage.v, true);
        drawChild(canvas, mCurrentPage.v, 0);
        drawFlippingShadeShine(canvas);
        mCamera.restore();
        canvas.restore();

    }

    /**
     * will draw a shade if flipping on the previous(top/left) half and a shine
     * if flipping on the next(bottom/right) half
     *
     * @param canvas
     */
    private void drawFlippingShadeShine(Canvas canvas) {
        if (mLightFlipEffects) {
            return;
        }
        final float degreesFlipped = getDegreesFlipped();
        if (degreesFlipped < 90) {
            final int alpha = (int) ((degreesFlipped / 90f) * MAX_SHINE_ALPHA);
            mShinePaint.setAlpha(alpha);
            canvas.drawRect(isFlippingVertically() ? mBottomRect : mRightRect,
                    mShinePaint);
        } else {
            final int alpha = (int) ((Math.abs(degreesFlipped - 180) / 90f) * MAX_SHADE_ALPHA);
            mShadePaint.setAlpha(alpha);
            canvas.drawRect(isFlippingVertically() ? mTopRect : mLeftRect,
                    mShadePaint);
        }
    }

    /**
     * Enable a hardware layer for the view.
     *
     * @param v
     * @param drawWithLayer
     */
    @SuppressLint("NewApi")
    private void setDrawWithLayer(View v, boolean drawWithLayer) {
        if (isHardwareAccelerated()) {
            if (v != null) {
                if (v.getLayerType() != LAYER_TYPE_HARDWARE && drawWithLayer) {
                    v.setLayerType(LAYER_TYPE_HARDWARE, null);
                } else if (v.getLayerType() != LAYER_TYPE_NONE && !drawWithLayer) {
                    v.setLayerType(LAYER_TYPE_NONE, null);
                }
            }
        }
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN_MR2) {
            v.setLayerType(LAYER_TYPE_NONE, null);
        }
    }

    private void positionMatrix() {
        mMatrix.preScale(0.10f, 0.10f);
        mMatrix.postScale(10.0f, 10.0f);
        mMatrix.preTranslate(-getWidth() / 2, -getHeight() / 2);
        mMatrix.postTranslate(getWidth() / 2, getHeight() / 2);
    }

    private float getDegreesFlipped() {
        float localFlipDistance = mFlipDistance % FLIP_DISTANCE_PER_PAGE;
        // fix for negative modulo. always want a positive flip degree
        if (localFlipDistance < 0) {
            localFlipDistance += FLIP_DISTANCE_PER_PAGE;
        }
        return (localFlipDistance / FLIP_DISTANCE_PER_PAGE) * 180;
    }

    private void postFlippedToPage(final int page) {
//        post(new Runnable() {
//
//            @Override
//            public void run() {
//        //Log.e("TRACKING","SINGLE TRACKING view check null "+page);
        if (mOnFlipListener != null) {
            mOnFlipListener.onFlippedToPage(FlipView.this, page,
                    mAdapter.getItemId(page));
//                    //Log.e("TRACKING","SINGLE TRACKING view !=null "+page);
            // ////System.out.println("adaptercount   :::" + adaptercount);
            // ////System.out.println("adaptercount11   :::"
            // + mAdapter.getCount());
                    /*if (flipto) {
                        flipTo(0);
                        flipto = false;
                    }*/
            // bufferedViews.clear();
            // if(page>mAdapter.getCount()){
            // bufferedViews.add(fillPageForIndex1(new Page(), page-1));
            // bufferedViews.add(fillPageForIndex1(new Page(), page));
            // }
            // else{
            // bufferedViews.add(fillPageForIndex1(new Page(), page-1));
            // bufferedViews.add(fillPageForIndex1(new Page(), page));
            // bufferedViews.add(fillPageForIndex1(new Page(), page+1));
            // }
            // bufferedViews.clear();
            // bufferedViews.add(fillPageForIndex1(new Page(), page));
            // bufferedViews.addFirst(fillPageForIndex1(new Page(),
            // page-1));
            // if(page+1>mAdapter.getCount()){
            // bufferedViews.addLast(null);
            // }
            // else{
            // bufferedViews.addLast(fillPageForIndex1(new Page(),
            // page+1));
            // }
            // if (page <= 2) {
            // startloading(mAdapter.getCount(), 0);
            // } else if (page > 2) {
            // startloading(mAdapter.getCount(), page - 2);
            // }
        }
//            }
//        });
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        // ////System.out.println("onSecondaryPointerUp  "+ev);
        final int pointerIndex = MotionEventCompat.getActionIndex(ev);
        final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mLastX = MotionEventCompat.getX(ev, newPointerIndex);
            mActivePointerId = MotionEventCompat.getPointerId(ev,
                    newPointerIndex);
            if (mVelocityTracker != null) {
                mVelocityTracker.clear();
            }
        }
    }

    /**
     * @param deltaFlipDistance The distance to flip.
     * @return The duration for a flip, bigger deltaFlipDistance = longer
     * duration. The increase if duration gets smaller for bigger values
     * of deltaFlipDistance.
     */
    private int getFlipDuration(int deltaFlipDistance) {
        float distance = Math.abs(deltaFlipDistance);
        int i = (int) (MAX_SINGLE_PAGE_FLIP_ANIM_DURATION * Math.sqrt(distance
                / FLIP_DISTANCE_PER_PAGE));
        /*
         * if(i>1000){ i=i+1000; } if(i>2000){ i=i+2000; } else if(i>3000){
         * i=i+3000; }
         */
        return (int) (MAX_SINGLE_PAGE_FLIP_ANIM_DURATION * Math.sqrt(distance
                / FLIP_DISTANCE_PER_PAGE));
    }

    /*private void positionMatrix1() {
        mMatrix.preScale(0.25f, 0.25f);
        mMatrix.postScale(4.0f, 4.0f);
        mMatrix.preTranslate(-getWidth() / 2, -getHeight() / 2);
        mMatrix.postTranslate(getWidth() / 2, getHeight() / 2);
    }*/

    private int getFlipDuration1(int deltaFlipDistance) {
        float distance = Math.abs(deltaFlipDistance);
        int i = (int) (MAX_SINGLE_PAGE_FLIP_ANIM_DURATION1 * Math
                .sqrt(distance / 200));
        /*
         * if(i>1000){ i=i+1000; } if(i>2000){ i=i+2000; } else if(i>3000){
         * i=i+3000; }
         */
        return (int) (MAX_SINGLE_PAGE_FLIP_ANIM_DURATION1 * Math
                .sqrt(distance / 100));
    }

    /**
     * @param velocity
     * @return the page you should "land" on
     */
    /**
     * Finger travel → flip distance. Never 0: a short FlipView (small phones,
     * gesture bars) used integer division and turned one swipe into Infinity,
     * which rounded to page 0 or the last card.
     */
    private float pageTravelScale() {
        int span = isFlippingVertically()
                ? Math.max(1, getHeight() - (getHeight() / 3))
                : Math.max(1, getWidth());
        int per = Math.max(1, FLIP_DISTANCE_PER_PAGE);
        float div = span / (float) per;
        if (div < 1f) {
            div = 1f;
        }
        return div;
    }

    /** At most one page per motion event so a fling walks N, N+1, N+2. */
    private float clampFlipDelta(float delta) {
        float maxStep = Math.max(1, FLIP_DISTANCE_PER_PAGE);
        if (delta > maxStep) {
            return maxStep;
        }
        if (delta < -maxStep) {
            return -maxStep;
        }
        return delta;
    }

    private void rememberGestureAnchor() {
        if (mGestureAnchorPage >= 0) {
            return;
        }
        int page = mCurrentPageIndex;
        if (page < 0) {
            page = bestKnownPageIndex();
        }
        if (page < 0 && mCommittedPageIndex >= 0) {
            page = mCommittedPageIndex;
        }
        mGestureAnchorPage = page;
    }

    private void clearGestureAnchor() {
        mGestureAnchorPage = INVALID_PAGE_POSITION;
    }

    /**
     * Finger-up must not snap to page 0 unless the gesture actually reached the
     * start. A sequential drag from a later page may still land on 0.
     */
    private int clampLandingPage(int nextPage) {
        if (nextPage != 0) {
            return Math.max(0, nextPage);
        }
        int origin = mGestureAnchorPage >= 0 ? mGestureAnchorPage : mCommittedPageIndex;
        if (origin <= 1) {
            return 0;
        }
        int dragged = getCurrentPageRound();
        if (dragged <= 1) {
            return Math.max(0, dragged);
        }
        return dragged;
    }

    /**
     * A leap from mid-feed to page 0 is the short-screen distance explosion.
     * Adjacent steps (2 → 1 → 0) stay legal so a real drag can still reach the start.
     */
    private int clampPageAgainstExplodedDistance(int page) {
        int previous = mCurrentPageIndex >= 0 ? mCurrentPageIndex : mCommittedPageIndex;
        if (previous <= 1 || page != 0) {
            return page;
        }
        return previous;
    }

    private int getNextPage(int velocity) {
        int nextPage;
        if (velocity > mMinimumVelocity) {
            nextPage = getCurrentPageFloor();
        } else if (velocity < -mMinimumVelocity) {
            nextPage = getCurrentPageCeil();
        } else {
            nextPage = getCurrentPageRound();
        }
        return Math.min(Math.max(nextPage, 0), mPageCount - 1);
    }

    private int getCurrentPageRound() {
        return Math.round(mFlipDistance / FLIP_DISTANCE_PER_PAGE);
    }

    private int getCurrentPageFloor() {
        return (int) Math.floor(mFlipDistance / FLIP_DISTANCE_PER_PAGE);
    }

    private int getCurrentPageCeil() {
        return (int) Math.ceil(mFlipDistance / FLIP_DISTANCE_PER_PAGE);
    }

    /**
     * @return true if ended a flip
     */
    private boolean endFlip() {
        final boolean wasflipping = mIsFlipping;
        mIsFlipping = false;
        mIsUnableToFlip = false;
        mLastTouchAllowed = false;
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
        return wasflipping;
    }

    /**
     * @return true if ended a scroll
     */
    private boolean endScroll() {
        final boolean wasScrolling = !mScroller.isFinished();
        mScroller.abortAnimation();
        return wasScrolling;
    }

    /**
     * Cancel idle {@link #peakNext}/{@link #peakPrevious} bob without starting a
     * flip. Used when a nested horizontal gallery takes the gesture.
     */
    public void cancelIdlePeak() {
        endPeak();
    }

    /**
     * @return true if ended a peak
     */
    @SuppressLint("NewApi")
    private boolean endPeak() {
        final boolean wasPeaking = mPeakAnim != null;
        if (mPeakAnim != null) {
            mPeakAnim.cancel();
            mPeakAnim = null;
        }
        return wasPeaking;
    }

    @SuppressLint("NewApi")
    private void peak(boolean next, boolean once) {
        final float baseFlipDistance = mCurrentPageIndex
                * FLIP_DISTANCE_PER_PAGE;
        if (next) {
            mPeakAnim = ValueAnimator.ofFloat(baseFlipDistance,
                    baseFlipDistance + FLIP_DISTANCE_PER_PAGE / 6);
        } else {
            mPeakAnim = ValueAnimator.ofFloat(baseFlipDistance,
                    baseFlipDistance - FLIP_DISTANCE_PER_PAGE / 6);
        }
        mPeakAnim.setInterpolator(mPeakInterpolator);
        mPeakAnim.addUpdateListener(new AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setFlipDistance((Float) animation.getAnimatedValue());
            }
        });
        mPeakAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                endPeak();
            }
        });
        mPeakAnim.setDuration(PEAK_ANIM_DURATION);
        mPeakAnim.setRepeatMode(ValueAnimator.REVERSE);
        mPeakAnim.setRepeatCount(once ? 1 : ValueAnimator.INFINITE);
        mPeakAnim.start();
    }

    private void trackVelocity(MotionEvent ev) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
    }

    private void updateEmptyStatus() {
        boolean empty = mAdapter == null || mPageCount == 0;
        if (empty) {
            if (mEmptyView != null) {
                mEmptyView.setVisibility(View.VISIBLE);
                // setVisibility(View.GONE);
            } else {
                setVisibility(View.VISIBLE);
            }

        } else {
            if (mEmptyView != null) {
                mEmptyView.setVisibility(View.GONE);
            }
            setVisibility(View.VISIBLE);
        }
    }

    public ListAdapter getAdapter() {
        return mAdapter;
    }

    /**
     * @param adapter a regular ListAdapter, not all methods if the list adapter are
     *                used by the flipview
     */
    public void setAdapter(ListAdapter adapter) {
        if (mAdapter != null) {
            mAdapter.unregisterDataSetObserver(dataSetObserver);
        }
        // remove all the current views
        removeAllViews();
        mAdapter = adapter;
        mPageCount = adapter == null ? 0 : mAdapter.getCount();
        if (adapter != null) {
            mAdapter.registerDataSetObserver(dataSetObserver);
            mRecycler.setViewTypeCount(mAdapter.getViewTypeCount());
            mRecycler.invalidateScraps();
            // startloading(mAdapter.getCount(), 0);
            // fillPageForIndex1(prvpage,0);
            // fillPageForIndex1(nrPage,2);
            // bufferedViews.addFirst(null);
            // bufferedViews.add(fillPageForIndex1(new Page(), 3));
            // bufferedViews.addLast(fillPageForIndex1(new Page(), 3));
            // for (int i = 0; i < 20; i++) {
            // st45.add(fillPageForIndex1(new Page(), i));
            // }
        }
        // TODO pretty confusing
        // this will be correctly set in setFlipDistance method
        clearFlipAnchor();
        clearCommittedSeat();
        mCurrentPageIndex = INVALID_PAGE_POSITION;
        mFlipDistance = INVALID_FLIP_DISTANCE;
        // Force first-page (index 0) to dispatch onFlippedToPage after setAdapter.
        mLastDispatchedPageEventIndex = INVALID_PAGE_POSITION;
        setFlipDistance(0);
        updateEmptyStatus();
    }

    public int getPageCount() {
        return mPageCount;
    }

    public int getCurrentPage() {
        if (mCurrentPageIndex > 0) {
            return mCurrentPageIndex;
        }
        if (mCurrentPageIndex == 0 && isActuallyOnFirstPage()) {
            return 0;
        }
        // During a dataset restore the index is briefly unknown. Callers that
        // treat -1 as "go to first article" must not see that window.
        int known = getAnchoredPage();
        if (known == 0 && mCommittedPageIndex > 0 && !isActuallyOnFirstPage()) {
            return mCommittedPageIndex;
        }
        return known < 0 ? 0 : known;
    }

    /** Last page the user actually landed on. 0 only if that seat is page 0. */
    public long getCommittedPageId() {
        if (mCommittedPageId != 0 && mCommittedPageId != -1) {
            return mCommittedPageId;
        }
        return bestKnownPageId();
    }

    private void commitSettledPage() {
        if (mIsFlipping || (mScroller != null && !mScroller.isFinished())) {
            return;
        }
        if (mCurrentPageIndex > 0) {
            mCommittedPageIndex = mCurrentPageIndex;
            if (mCurrentPageId != 0 && mCurrentPageId != -1) {
                mCommittedPageId = mCurrentPageId;
            }
            return;
        }
        if (mCurrentPageIndex == 0 && isActuallyOnFirstPage()) {
            mCommittedPageIndex = 0;
            if (mCurrentPageId != 0 && mCurrentPageId != -1) {
                mCommittedPageId = mCurrentPageId;
            }
        }
    }

    /**
     * True while the user is dragging or the fling scroller is running.
     * Does not include the idle peak-next hint — that must not delay first paint.
     * Dataset notifies must wait for this to clear so the feed cannot snap home.
     */
    public boolean isUserDrivingFlip() {
        try {
            return mIsFlipping || (mScroller != null && !mScroller.isFinished());
        } catch (Exception e) {
            return mIsFlipping;
        }
    }

    /**
     * The active page View (not {@link #getChildAt} by absolute feed index —
     * FlipView only keeps prev/current/next children).
     */
    @androidx.annotation.Nullable
    public View getCurrentPageView() {
        try {
            if (mCurrentPage != null && mCurrentPage.valid) {
                return mCurrentPage.v;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * True while a flip gesture/animation is in progress (touch, scroller, or peak).
     * Callers must not {@code notifyDataSetChanged} / recycle pages during this window.
     */
    public boolean isFlipAnimating() {
        try {
            return mIsFlipping
                    || (mScroller != null && !mScroller.isFinished())
                    || mPeakAnim != null;
        } catch (Exception e) {
            return mIsFlipping;
        }
    }

    /**
     * True if the current page has a real vertical {@link ScrollView} /
     * {@link NestedScrollView} that can still scroll in {@code direction}.
     * Used so calendar/raasi nested pages can scroll without starting a FlipView
     * fold.
     *
     * <p><b>Must not</b> treat arbitrary {@link View#canScrollVertically} as a
     * flip blocker — WebView (YouTube), PlayerView, ads, and many article
     * descendants report true and froze News/Special/Video flipping after the
     * calendar nested-scroll fix.
     */
    private boolean canCurrentPageScrollVertically(int direction) {
        View page = mCurrentPage != null && mCurrentPage.valid ? mCurrentPage.v : null;
        return page != null && canDedicatedVerticalScrollContainerScroll(page, direction);
    }

    private static boolean canDedicatedVerticalScrollContainerScroll(
            View view, int direction) {
        if (view == null) return false;
        if (isDedicatedVerticalScrollContainer(view)
                && view.canScrollVertically(direction)) {
            return true;
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                if (canDedicatedVerticalScrollContainerScroll(
                        group.getChildAt(i), direction)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isDedicatedVerticalScrollContainer(View view) {
        return view instanceof android.widget.ScrollView
                || view instanceof androidx.core.widget.NestedScrollView;
    }

    public void flipTo(int page) {
        if (page < 0 || page > mPageCount - 1) {
            throw new IllegalArgumentException("That page does not exist");
        }
//        //Log.e("TRACKING","flipTo "+page);
        endFlip();
        clearGestureAnchor();
        // Explicit navigation (deep link, restore) is a real land — including page 0.
        mCommittedPageIndex = page;
        setFlipDistance(page * FLIP_DISTANCE_PER_PAGE);
        commitExplicitPage(page);
    }

    /**
     * Keyword search / fresh deck: wipe restore anchors so the next
     * {@code dataSetChanged} cannot clamp onto a prior News index or the
     * Continue-reading hub at the end of the new list.
     */
    public void resetSeatForNewFeed() {
        try {
            endFlip();
        } catch (Exception ignored) {
        }
        clearGestureAnchor();
        clearFlipAnchor();
        clearCommittedSeat();
        mCurrentPageIndex = 0;
        mCurrentPageId = -1;
        mLastDispatchedPageEventIndex = INVALID_PAGE_POSITION;
        mFlipDistance = 0;
        try {
            if (mPageCount > 0) {
                setFlipDistance(0, true);
                commitExplicitPage(0);
            }
        } catch (Exception ignored) {
        }
    }

    private void commitExplicitPage(int page) {
        if (page < 0) return;
        mCommittedPageIndex = page;
        mCurrentPageIndex = page;
        long id = itemIdAt(page);
        if (id != 0L && id != -1L) {
            mCommittedPageId = id;
            mCurrentPageId = id;
            mAnchorPageId = id;
        }
        mAnchorPageIndex = page;
    }
    // public void startloading(int count, int currentpos) {
    //
    // // if(adaptercount<mAdapter.getCount()){
    // Page p = null;
    // // ////System.out.println("araaylist    ::" + st45.size());
    // // ////System.out.println("adaptercount    ::" + adaptercount);
    // // st45 = new ArrayList<Page>();
    // int k = 0;
    // for (int l = 0; l < count; l++) {
    // // setViewscrap(l);
    // p = new Page();
    // // p=fillPageForIndex1(p, l);
    // if (l < currentpos) {
    // st45.add(null);
    // } else {
    // if (k <= 5) {
    // st45.add(fillPageForIndex1(p, l));
    //
    // } else {
    // st45.add(null);
    // }
    // k = k + 1;
    // }
    // }
    //
    // // adaptercount = adaptercount + 5;
    // // }
    //
    // }
    //
    // public void startloading1(int count) {
    //
    // // if(adaptercount<mAdapter.getCount()){
    // if (count > 2) {
    // st45 = new ArrayList<Page>();
    // for (int l = count - 2; l < 5; l++) {
    // // setViewscrap(l);
    // p = new Page();
    // // p=fillPageForIndex1(p, l);
    // st45.add(fillPageForIndex1(p, l));
    // adaptercount = adaptercount + 1;
    // }
    // }
    //
    // }

    /* ---------- API ---------- */

    public void flipBy(int delta) {
        flipTo(mCurrentPageIndex + delta);
    }

    public void smoothFlipTo(int page1) {
        if (page1 < 0 || page1 > mPageCount - 1) {
            throw new IllegalArgumentException("That page does not exist");
        }
//        //Log.e("TRACKING","smoothFlipTo "+page1);
        final int start = (int) mFlipDistance;
        final int delta = page1 * FLIP_DISTANCE_PER_PAGE - start;
        endFlip();
        mScroller.startScroll(0, start, 0, delta, getFlipDuration1(delta));
        invalidate();
    }

    public void smoothFlipTo1(int page1) {
        if (page1 < 0 || page1 > mPageCount - 1) {
            throw new IllegalArgumentException("That page does not exist");
        }
        // 06-26 18:01:53.721: I///System.out(6298):
        // getFlipDuration(delta)111111111111111111111111111
        // 1714---delta----1800----start--1800---mFlipDistance---1800.0
        final int start = (int) mFlipDistance;
        if (mCurrentPageIndex <= 10) {
            page1 = 0;
        } else {
            page1 = mCurrentPageIndex - 10;
            flipto = true;
        }
        final int delta = page1 * FLIP_DISTANCE_PER_PAGE - start;
        // ////System.out.println("getFlipDuration(delta)111111111111111111111111111  "+getFlipDuration(delta)+"---delta---"+delta+"----start--"+start+"---mFlipDistance---"+mFlipDistance);
        endFlip();
        mScroller.startScroll(0, start, 0, delta, getFlipDuration1(delta));
        // mScroller.startScroll(1800, , 1800, 0, 1714);
        // ////System.out.println("getFlipDuration(delta)2222222222222222222  "
        // + getFlipDuration(delta) + "---delta---" + delta
        // + "----start--" + start);
        // setFlipDistance(0 * FLIP_DISTANCE_PER_PAGE);
        // if(flipto){
        // flipTo(0);
        // flipto=false;
        // }
        invalidate();
        // /////System.out.println("getFlipDuration(delta)  "+getFlipDuration(delta));
    }

    public void smoothFlipBy(int delta) {
        smoothFlipTo(mCurrentPageIndex + delta);
    }

    /**
     * Hint that there is a next page will do nothing if there is no next page
     *
     * @param once if true, only peak once. else peak until user interacts with
     *             view
     */
    public void peakNext(boolean once) {
        if (mCurrentPageIndex < mPageCount - 1) {
            peak(true, once);
        }
    }

    /**
     * Hint that there is a previous page will do nothing if there is no
     * previous page
     *
     * @param once if true, only peak once. else peak until user interacts with
     *             view
     */
    public void peakPrevious(boolean once) {
        if (mCurrentPageIndex > 0) {
            peak(false, once);
        }
    }

    /**
     * @return true if the view is flipping vertically, can only be set via xml
     * attribute "orientation"
     */
    public boolean isFlippingVertically() {
        return mIsFlippingVertically;
    }

    /**
     * The OnFlipListener will notify you when a page has been fully turned.
     *
     * @param onFlipListener
     */
    public void setOnFlipListener(OnFlipListener onFlipListener) {
        mOnFlipListener = onFlipListener;
        // setAdapter may draw before the listener is attached; reset so the next
        // dispatchDraw notifies page 0 (push/share landing card) once listener exists.
        mLastDispatchedPageEventIndex = INVALID_PAGE_POSITION;
        invalidate();
    }

    public void setOnFlipCompleteListener(OnFlipCompleteListener onFlipListener) {
        mOnFlipCompleteListener = onFlipListener;
    }

    public void setOnFlipAppear(OnFlipAppeared onFlipListener) {
        mOnFlipAppeared = onFlipListener;
    }

    /**
     * The OnOverFlipListener will notify of over flipping. This is a great
     * listener to have when implementing pull-to-refresh
     *
     * @param onOverFlipListener
     */
    public void setOnOverFlipListener(OnOverFlipListener onOverFlipListener) {
        this.mOnOverFlipListener = onOverFlipListener;
    }

    /**
     * @return the overflip mode of this flipview. Default is GLOW
     */
    public OverFlipMode getOverFlipMode() {
        return mOverFlipMode;
    }

    /**
     * Set the overflip mode of the flipview. GLOW is the standard seen in all
     * andriod lists. RUBBER_BAND is more like iOS lists which list you flip
     * past the first/last page but adding friction, like a rubber band.
     *
     * @param overFlipMode
     */
    public void setOverFlipMode(OverFlipMode overFlipMode) {
        this.mOverFlipMode = overFlipMode;
        mOverFlipper = OverFlipperFactory.create(this, mOverFlipMode);
    }

    /**
     * @param emptyView The view to show when either no adapter is set or the adapter
     *                  has no items. This should be a view already in the view
     *                  hierarchy which the FlipView will set the visibility of.
     */
    public void setEmptyView(View emptyView) {
        mEmptyView = emptyView;
        updateEmptyStatus();
    }

    public interface OnFlipListener {
        void onFlippedToPage(FlipView v, int position, long id);
//        public void onCurrentToTrack(int position);
    }

    public interface OnFlipCompleteListener {
        void OnFlipCompletedListener(FlipView v);
//        public void onCurrentToTrack(int position);
    }

    public interface OnFlipAppeared {
        void onFlippedToAppear(FlipView v);
//        public void onCurrentToTrack(int position);
    }

    public interface OnOverFlipListener {
        void onOverFlip(FlipView v, OverFlipMode mode,
						boolean overFlippingPrevious, float overFlipDistance,
						float flipDistancePerPage);

        void flipfinsh();


    }

    static class Page {
        View v;
        int position;
        int viewType;
        boolean valid;
        long boundItemId;
        int boundGeneration;
    }



}

