package com.anyNews.anynews.Utils;

import static com.anyNews.anynews.Utils.AppHelper.showLog;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.viewpager.widget.ViewPager;

public class VerticalViewPager extends ViewPager {

    public VerticalViewPager(Context context) {
        super(context);
        init();
    }

    public VerticalViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setPageTransformer(true, new VerticalPageTransformerAnimate());
    //    setOverScrollMode(OVER_SCROLL_NEVER);
    }

    private class VerticalPageTransformer implements PageTransformer {
        @Override
        public void transformPage(View view, float position) {

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0);

            } else if (position <= 1) { // [-1,1]
              /*  view.setAlpha(1);

                // Counteract the default slide transition
                view.setTranslationX(view.getWidth() * -position);

                //set Y position to swipe in from top
                float yPosition = position * view.getHeight();
                view.setTranslationY(yPosition);*/

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
//                view.setAlpha(0);
            }

        }
    }

    /**
     * Swaps the X and Y coordinates of your touch event.
     */
    private MotionEvent swapXY(MotionEvent ev) {
        float width = getWidth();
        float height = getHeight();

        float newX = (ev.getY() / height) * width;
        float newY = (ev.getX() / width) * height;

        ev.setLocation(newX, newY);

        return ev;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev){
        boolean intercepted = super.onInterceptTouchEvent(swapXY(ev));
        swapXY(ev);
        return intercepted;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {


        return super.onTouchEvent(swapXY(ev));
    }

    private class VerticalPageTransformerAnimate implements PageTransformer {
        private static final float MIN_SCALE = 0.75f;

        @Override
        public void transformPage(View view, float position) {

            int pageWidth = view.getWidth();
            int pageHeight = view.getHeight();

            Toast.makeText(getContext(), ""+position, Toast.LENGTH_SHORT).show();
            showLog(" AKKISETTY_NARASIMHA "+position);
            float alpha = 0;
            if (0 <= position && position <= 1) {
                alpha = 1 - position;
            }
          /*  else if (-1 < position && position < 0) {
                float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
                float verticalMargin = pageHeight * (1 - scaleFactor) / 2;
                float horizontalMargin = pageWidth * (1 - scaleFactor) / 2;
                if (position < 0) {
                    view.setTranslationX(horizontalMargin - verticalMargin / 2);
                } else {
                    view.setTranslationX(-horizontalMargin + verticalMargin / 2);
                }

                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

                alpha = position + 1;
            }*/

            view.setAlpha(alpha);
            view.setTranslationX(view.getWidth() * -position);
            float yPosition = position * view.getHeight();
            view.setTranslationY(yPosition);

        }
    }


//    @Override
//    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
//// TODO Auto-generated method stub
////Defining Sensitivity
//        float sensitivity = 50;
////Swipe Up Check
//        if (e1.getY() - e2.getY() > sensitivity) {
////Setting Image Resource to Up_Arrow on Swipe Up
//
//            return true;
//        }
//    }
}
