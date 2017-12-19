package com.ravi.android.buddy.ledger.CustomUiElement;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

/**
 * Created by ravi on 29/1/17.
 */

public class DynamicSizeImageView extends AppCompatImageView {

    private float mAspectRatio = 1.5f;

    public DynamicSizeImageView(Context context) {
        super(context);
    }

    public DynamicSizeImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DynamicSizeImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setAspectRatio(float aspectRatio) {
        mAspectRatio = aspectRatio;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measuredWidth = getMeasuredWidth();
        setMeasuredDimension(measuredWidth, (int) (measuredWidth / mAspectRatio));
    }

}
