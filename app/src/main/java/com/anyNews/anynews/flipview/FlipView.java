package com.anyNews.anynews.flipview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
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

import com.anyNews.anynews.Activities.HomePage;
import com.anyNews.anynews.R;

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
        //session = new SessionManager(mContext);
        final int currentPage = mCurrentPageIndex;
        newPosition = currentPage;
        // if the adapter has stable ids, try to keep the page currently on
        // stable.
        // mPageCount = mAdapter.getCount();
        // ////System.out.println("mPageCount   " + mPageCount);
        // startloading(mPageCount);
        if (mAdapter.hasStableIds() && currentPage != INVALID_PAGE_POSITION) {
            newPosition = getNewPositionOfCurrentPage();
        } else if (currentPage == INVALID_PAGE_POSITION) {
            newPosition = 0;
        }
        // remove all the current views
        try {
//			if (MainActivity.isNightMode == 1) {
//				recycleActiveViews();
//				mRecycler.setViewTypeCount(mAdapter.getViewTypeCount());
//				mRecycler.invalidateScraps();
//				MainActivity.isNightMode = 0;
//			}

            /*if (BuzzActivity.isNightMode == 1) {
                recycleActiveViews();
                mRecycler.setViewTypeCount(mAdapter.getViewTypeCount());
                mRecycler.invalidateScraps();
                BuzzActivity.isNightMode = 0;
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }
        mPageCount = mAdapter.getCount();
        // put the current page within the new adapter range
        newPosition = Math.min(mPageCount - 1,
                newPosition == INVALID_PAGE_POSITION ? 0 : newPosition);
        // ////System.out.println("new position " + newPosition);
        if (newPosition != INVALID_PAGE_POSITION) {
            // TODO pretty confusing
            // this will be correctly set in setFlipDistance method
            mCurrentPageIndex = INVALID_PAGE_POSITION;
            mFlipDistance = INVALID_FLIP_DISTANCE;
            // dataSetChanged often lands back on 0; reset so landing page is tracked.
            mLastDispatchedPageEventIndex = INVALID_PAGE_POSITION;
            flipTo(newPosition);
        } else {
            mFlipDistance = INVALID_FLIP_DISTANCE;
            mPageCount = 0;
            mLastDispatchedPageEventIndex = INVALID_PAGE_POSITION;
            setFlipDistance(0);
        }
        updateEmptyStatus();
    }

    private int getNewPositionOfCurrentPage() {
        // check if id is on same position, this is because it will
        // often be that and this way you do not need to iterate the whole
        // dataset. If it is the same position, you are done.
        if (mCurrentPageId == mAdapter.getItemId(mCurrentPageIndex)) {
            return mCurrentPageIndex;
        }
        // iterate the dataset and look for the correct id. If it
        // exists, set that position as the current position.
        for (int i = 0; i < mAdapter.getCount(); i++) {
            if (mCurrentPageId == mAdapter.getItemId(i)) {
                return i;
            }
        }
        // Id no longer is dataset, keep current page
        return mCurrentPageIndex;
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
        try {
            if (mPageCount < 1) {
                //////System.out.println("FLIP_DISTANCE_PER_PAGE11111111111111   "+FLIP_DISTANCE_PER_PAGE);
                mFlipDistance = 0;
                mCurrentPageIndex = INVALID_PAGE_POSITION;
                mCurrentPageId = -1;
                recycleActiveViews();
                return;
            }

            /*
             * if (flipDistance == mFlipDistance) { return; }
             */
            mFlipDistance = flipDistance;
            // ////System.out.println(mFlipDistance + "   :::mFlipDistance");
            int currentPageIndex = Math.round(mFlipDistance
                    / FLIP_DISTANCE_PER_PAGE);
            // if(mCurrentPageIndex==currentPageIndex){
            // return;
            // }
            if (mCurrentPageIndex != currentPageIndex) {
                mCurrentPageIndex = currentPageIndex;
                mCurrentPageId = mAdapter.getItemId(mCurrentPageIndex);
                // TODO be smarter about this. Dont remove a view that will be added
                // again on the next line.
                // ////System.out.println("before recycleActiveViews  "
                // + //System.currentTimeMillis());
                recycleActiveViews();
                // ////System.out.println("After recycleActiveViews  "
                // + //System.currentTimeMillis());
                // add the new active views
                if (mCurrentPageIndex > 0) {
//                removeView(mPreviousPage.v);
                    fillPageForIndex(mPreviousPage, mCurrentPageIndex - 1,
                            (mCurrentPageIndex - 1) % 3);
//                if(mCurrentPageIndex<3){
//                    removeView(mPreviousPage.v);
//                }
                    addView(mPreviousPage.v);
                }
                if (mCurrentPageIndex >= 0 && mCurrentPageIndex < mPageCount) {
                    fillPageForIndex(mCurrentPage, mCurrentPageIndex, (mCurrentPageIndex) % 3);
//                if(mCurrentPageIndex<3){
//                    removeView(mCurrentPage.v);
//                }
//          
                    addView(mCurrentPage.v);
                }
                if (mCurrentPageIndex < mPageCount - 1) {
//                removeView(mNextPage.v);
                    fillPageForIndex(mNextPage, mCurrentPageIndex + 1,
                            (mCurrentPageIndex + 1) % 3);
//                if(mCurrentPageIndex<3){
//                    removeView(mNextPage.v);
//                }
                    addView(mNextPage.v);
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
        // p.v=bufferedViews.get(i).v;
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
        p.valid = true;
        prvpage.v = p.v;
        prvpage.viewType = p.viewType;
        prvpage.valid = true;
        return p;
    }

    public void recycleActiveViews() {
        // remove and recycle the currently active views
        if (mPreviousPage.valid) {
            removeView(mPreviousPage.v);
            mRecycler.addScrapView(mPreviousPage.v, mPreviousPage.position,
                    mPreviousPage.viewType);
            mPreviousPage.valid = false;
        }
        if (mCurrentPage.valid) {
            removeView(mCurrentPage.v);
            mRecycler.addScrapView(mCurrentPage.v, mCurrentPage.position,
                    mCurrentPage.viewType);
            mCurrentPage.valid = false;
        }
        if (mNextPage.valid) {
            removeView(mNextPage.v);
            mRecycler.addScrapView(mNextPage.v, mNextPage.position,
                    mNextPage.viewType);
            mNextPage.valid = false;
        }
    }

    public void setViewscrap(int index) {
        // get the scrap from the recycler corresponding to the correct view
        // type
        Recycler.Scrap scrap = mRecycler.getScrapView(index, 0);
        // get a view from the adapter if a scrap was not found or it is
        // invalid.
        View v = null;
        if (scrap == null || !scrap.valid) {
            // v = mAdapter.getView(index, scrap == null ? null : scrap.v,
            // this);
            // mAdapter.getView(index+1, scrap == null ? null : scrap.v, this);
            // mAdapter.getView(index+2, scrap == null ? null : scrap.v, this);
            v = mAdapter.getView(index, scrap == null ? null : scrap.v, this);
            mRecycler.addScrapView(v, index, 0);

        }
        // return view
        // return v;
    }

    private View getView(int index, int viewType) {
        // get the scrap from the recycler corresponding to the correct view
        // type
        Recycler.Scrap scrap = mRecycler.getScrapView(index, 0);
        // get a view from the adapter if a scrap was not found or it is
        // invalid.
        View v = null;
        // ////System.out.println("scrapscrap  ::::" + scrap);
        if (scrap == null || !scrap.valid) {
            v = mAdapter.getView(index, scrap == null ? null : scrap.v, this);
            /*
             * mAdapter.getView(index+1, scrap == null ? null : scrap.v, this);
             * mAdapter.getView(index+2, scrap == null ? null : scrap.v, this);
             * mAdapter.getView(index+3, scrap == null ? null : scrap.v, this);
             */
        } else {
            v = scrap.v;
        }
        // return view
        return v;
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
                    } else {
                        mIsFlipping = true;
                        mLastX = x;
                        mLastY = y;
                    }
                } else if ((mIsFlippingVertically && xDiff > mTouchSlop)
                        || (!mIsFlippingVertically && yDiff > mTouchSlop)) {
                    mIsUnableToFlip = true;
                }
        
                break;
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                mLastX = MotionEventCompat.getX(ev, 0);
                mLastY = MotionEventCompat.getY(ev, 0);
                mIsFlipping = !mScroller.isFinished() | mPeakAnim != null;
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
                // start flipping immediately if interrupting some sort of animation
                if (endScroll() || endPeak()) {
                    mIsFlipping = true;
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
                            mIsFlipping = true;
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
                    deltaFlipDistance /= ((isFlippingVertically() ? getHeight()
                            - (getHeight() / 3) : getWidth()) / FLIP_DISTANCE_PER_PAGE);
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
                    smoothFlipTo(getNextPage(velocity));
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
                setDrawWithLayer(mCurrentPage.v, false);
                hideOtherPages(mCurrentPage);
                drawChild(canvas, mCurrentPage.v, 0);
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
            p.v.setVisibility(VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
            Intent i = new Intent(mContext, HomePage.class);
            mContext.startActivity(i);
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
            Intent i = new Intent(mContext, HomePage.class);
            mContext.startActivity(i);

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
        return mCurrentPageIndex;
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
        setFlipDistance(page * FLIP_DISTANCE_PER_PAGE);
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
    }



}

