package com.ravi.android.buddy.ledger.utility;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by ravi on 14/1/17.
 */

public class PrefManager {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;

    // shared pref mode
    int PRIVATE_MODE = 0;

    // Shared preferences file name
    private static final String PREF_NAME = "buddy-ledger";

    private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";

    private static final String IS_CURRENCY_DIALOG_SHOWN = "IsCurrencyDialogShown";

    private static final String USER_CURRENCY = "userCurrency";

    public PrefManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime);
        editor.commit();
    }

    public boolean isFirstTimeLaunch() {
        return pref.getBoolean(IS_FIRST_TIME_LAUNCH, true);
    }

    public Boolean isCurrencyDialogShown() {
        return pref.getBoolean(IS_CURRENCY_DIALOG_SHOWN, false);
    }

    public void setCurrencyDialogShown(boolean isFirstTime) {
        editor.putBoolean(IS_CURRENCY_DIALOG_SHOWN, isFirstTime);
        editor.commit();
    }

    public String getUserCurrency() {
        return pref.getString(USER_CURRENCY, "₹");
    }

    public void setUserCurrency(String value) {
        editor.putString(USER_CURRENCY, value);
        editor.commit();
    }
}
