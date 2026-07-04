package com.anyNews.anynews.Utils;


import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.MediaController;

import com.anyNews.anynews.R;

public class CustomMediaController extends MediaController
{
    Context _context;

    public CustomMediaController(Context context, boolean useFastForward)
    {
        super(context, useFastForward);
        _context = context;
    }

    public CustomMediaController(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        _context = context;
    }

    public CustomMediaController(Context context)
    {
        super(context);
        _context = context;
    }

    @Override
    public void setAnchorView(View view)
    {
        super.setAnchorView(view);



        //  removeAllViews();
        /**
         * 0 : 컨트롤러 전체
         * 1 : 컨트롤 버튼
         * 2 : 탐색 바
         */
        LinearLayout layout = (LinearLayout) getChildAt(0);
        LinearLayout button = (LinearLayout)layout.getChildAt(1);
        LinearLayout seek = (LinearLayout)layout.getChildAt(2);

        LayoutInflater inf = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout linear = (LinearLayout) inf.inflate(R.layout.preview, null);
        //   layout.addView(linear, 2);
    }
}

