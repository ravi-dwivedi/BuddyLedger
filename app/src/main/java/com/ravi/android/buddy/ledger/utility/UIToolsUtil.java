package com.ravi.android.buddy.ledger.utility;

import android.content.Context;
import android.content.res.Configuration;

/**
 * Created by ravi on 8/2/17.
 */

public class UIToolsUtil {
    private UIToolsUtil() {
    }

    public static int dpToPx(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * scale);
    }

    public static boolean isScreenLarge(Context context) {
        final int screenSize = context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        return screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE ||
                screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }
}
