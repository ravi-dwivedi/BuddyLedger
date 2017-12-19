package com.ravi.android.buddy.ledger.CustomUiElement;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ravi.android.buddy.ledger.R;

/**
 * Created by ravi on 10/3/17.
 */

public class CustomBar extends LinearLayout {

    private TextView b1TextView;
    private TextView b2TextView;

    private double total = 1;
    private double b1val = 0.6f, b2val = 0.4f;
    private String b1text = "60", b2text = "40";

    public CustomBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        setOrientation(LinearLayout.HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.item_custom_bar, this, true);

        b1TextView = (TextView) findViewById(R.id.horbar_b1);
        b2TextView = (TextView) findViewById(R.id.horbar_b2);

        b1TextView.setBackgroundColor(Color.parseColor("#8bc34a"));
        b2TextView.setBackgroundColor(Color.parseColor("#ED2939"));

    }

    public void setTotal(double total) {
        this.total = total;
    }

    public void updateBar(double b1, double b2) {
        updateBar(b1, b2, String.valueOf(b1), String.valueOf(b2));
    }

    public void updateBar(double b1, double b2, String s1, String s2) {

        b1val = b1;
        b2val = b2;

        float percent1 = (float) (b1val == 0 ? 0 : b1val / total);
        float percent2 = (float) (b2val == 0 ? 0 : b2val / total);

        percent1 = 1 - ((float) ((int) (percent1 * 100))) / 100;
        percent2 = 1 - ((float) ((int) (percent2 * 100))) / 100;

        b1TextView.setLayoutParams(new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, percent1));

        b2TextView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, percent2));

        b1TextView.setText(s1);
        b2TextView.setText(s2);
    }

    public void updateValuesFromBundle(Bundle bundle) {
        if (bundle != null) {
            setTotal(bundle.getDouble("TOTAL_AMOUNT", 0));
            updateBar(bundle.getDouble("LEFT_AMOUNT", 0), bundle.getDouble("RIGHT_AMOUNT", 0),
                    bundle.getString("LEFT_TITLE"), bundle.getString("RIGHT_TITLE"));
        }
    }
}
