package com.anyNews.anynews.myflip.utils;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class NumberButtonArman extends Button {

    private int number;

    public NumberButtonArman(Context context, final int n) {
        super(context, null, android.R.attr.buttonStyleInset);
        setNumber(n);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "Clicked button " + number, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
        setText(Integer.toString(number));
    }

    @Override
    public String toString() {
        return "NumberButton: " + number;
    }
}