package com.anyNews.anynews.Ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.anyNews.anynews.R;

/**
 * Fuel price card. The station photo and scrim paint behind the prices and
 * do not add height, so the city list sits directly under the cards.
 *
 * A match_parent child inside this wrap_content frame is measured with
 * AT_MOST equal to the viewport. A plain View then reports that full size,
 * which opened a tall photo gap and came back on every state change.
 */
public class FuelHeroFrame extends FrameLayout {

    public FuelHeroFrame(Context context) {
        super(context);
    }

    public FuelHeroFrame(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FuelHeroFrame(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private static boolean isBackdrop(View child) {
        int id = child.getId();
        return id == R.id.fuel_hero_image || id == R.id.fuel_hero_scrim;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int maxContentWidth = 0;
        int maxContentHeight = 0;
        int childState = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE || isBackdrop(child)) continue;
            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
            maxContentWidth = Math.max(maxContentWidth,
                    child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin);
            maxContentHeight = Math.max(maxContentHeight,
                    child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);
            childState = combineMeasuredStates(childState, child.getMeasuredState());
        }
        maxContentWidth += getPaddingLeft() + getPaddingRight();
        maxContentHeight += getPaddingTop() + getPaddingBottom();

        int measuredW = resolveSizeAndState(maxContentWidth, widthMeasureSpec, childState);
        int measuredH = resolveSizeAndState(maxContentHeight, heightMeasureSpec,
                childState << MEASURED_HEIGHT_STATE_SHIFT);
        setMeasuredDimension(measuredW, measuredH);

        int innerW = Math.max(0, measuredW - getPaddingLeft() - getPaddingRight());
        int innerH = Math.max(0, measuredH - getPaddingTop() - getPaddingBottom());
        int backdropW = MeasureSpec.makeMeasureSpec(innerW, MeasureSpec.EXACTLY);
        int backdropH = MeasureSpec.makeMeasureSpec(innerH, MeasureSpec.EXACTLY);
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE || !isBackdrop(child)) continue;
            child.measure(backdropW, backdropH);
        }
    }
}
