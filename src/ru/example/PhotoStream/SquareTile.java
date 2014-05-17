package ru.example.PhotoStream;

import android.content.Context;
import android.util.AttributeSet;

public class SquareTile extends SmartImage {
    public SquareTile(Context context) {
        super(context);
    }

    public SquareTile(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public SquareTile(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth());
    }
}
