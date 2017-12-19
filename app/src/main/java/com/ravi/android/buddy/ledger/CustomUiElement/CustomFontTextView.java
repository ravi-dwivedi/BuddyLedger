package com.ravi.android.buddy.ledger.CustomUiElement;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import com.ravi.android.buddy.ledger.R;

/**
 * Created by ravi on 29/1/17.
 */

public class CustomFontTextView extends AppCompatTextView {
    private String fontName;

    public CustomFontTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public CustomFontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CustomFontTextView(Context context) {
        super(context);
        init(context, null);
    }

    public CustomFontTextView(Context context, String fontName) {
        super(context);
        this.fontName = fontName;
        init(context, null);
    }

    private void init(Context context, AttributeSet attrs) {
        if (!this.isInEditMode()) {
            if (attrs != null) {
                TypedArray a = getContext().obtainStyledAttributes(attrs,
                        R.styleable.CustomFontTV);
                fontName = a.getString(R.styleable.CustomFontTV_fontName);
                if (fontName != null) {
                    createFromAsset(context);
                }
                a.recycle();
            } else if (fontName != null) {
                createFromAsset(context);
            }
        }
    }

    private void createFromAsset(Context context) {
        if (fontName != null) {
            setTypeface(TypeFaces.get(context, fontName));
        }
    }
}

