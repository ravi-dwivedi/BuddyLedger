package com.ravi.android.buddy.ledger.widget;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.RemoteViews;

import com.ravi.android.buddy.ledger.HomeActivity;
import com.ravi.android.buddy.ledger.R;
import com.ravi.android.buddy.ledger.data.UserLedgerEntry;
import com.ravi.android.buddy.ledger.data.UserTransactionEntry;
import com.ravi.android.buddy.ledger.model.UserLedgerType;
import com.ravi.android.buddy.ledger.model.UserTransactionType;
import com.ravi.android.buddy.ledger.utility.DateUtil;

/**
 * Created by ravi on 9/3/17.
 */

public class AllTransactionSummaryWidgetIntentService extends IntentService {

    public AllTransactionSummaryWidgetIntentService() {
        super("AllTransactionSummaryWidgetIntentService");
    }

    private final String[] USER_TRANSACTIONS_COLUMNS = {
            UserTransactionEntry.COLUMN_TRANSACTION_TYPE,
            UserTransactionEntry.COLUMN_TRANSACTION_AMOUNT,
            UserTransactionEntry.COLUMN_TRANSACTION_DATE
    };

    private static final String[] USER_LEDGER_COLUMNS = {
            UserLedgerEntry.COLUMN_AMOUNT,
            UserLedgerEntry.COLUMN_DATE,
            UserLedgerEntry.COLUMN_TYPE
    };

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        // Retrieve all of the Today widget ids: these are the widgets we need to update
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                AllTransactionSummaryWidgetProvider.class));


        double totalLentAmount = 0D;
        double totalBorrowAmount = 0D;

        Cursor userTransactionCursor = getContentResolver().query(UserTransactionEntry.buildTransactionUri(), USER_TRANSACTIONS_COLUMNS, null,
                null, null);
        if (userTransactionCursor != null) {
            for (int index = 0; index < userTransactionCursor.getCount(); index++) {
                userTransactionCursor.moveToPosition(index);
                switch (UserTransactionType.valueOf(userTransactionCursor.getString(0))) {
                    case LENT:
                        totalLentAmount += userTransactionCursor.getDouble(1);
                        break;
                    case BORROW:
                        totalBorrowAmount += userTransactionCursor.getDouble(1);
                        break;
                }
            }
            userTransactionCursor.close();
        }

        double totalIncomeAmount = 0D;
        double totalExpenditureAmount = 0D;
        double currentMonthIncome = 0D;
        double currentMonthExpense = 0D;

        Cursor userLedgerCursor = getContentResolver().query(UserLedgerEntry.buildUserLedgerUri(), USER_LEDGER_COLUMNS, null, null, null);
        if (userLedgerCursor != null) {
            String curDate = DateUtil.getCurrentFormattedDateTime();
            for (int index = 0; index < userLedgerCursor.getCount(); index++) {
                userLedgerCursor.moveToPosition(index);
                switch (UserLedgerType.valueOf(userLedgerCursor.getString(2))) {
                    case INCOME:
                        totalIncomeAmount += userLedgerCursor.getDouble(0);
                        if (DateUtil.isDateIsOfCurrentMonth(userLedgerCursor.getString(1), curDate)) {
                            currentMonthIncome += userLedgerCursor.getDouble(0);
                        }
                        break;
                    case EXPENSE:
                        totalExpenditureAmount += userLedgerCursor.getDouble(0);
                        if (DateUtil.isDateIsOfCurrentMonth(userLedgerCursor.getString(1), curDate)) {
                            currentMonthExpense += userLedgerCursor.getDouble(0);
                        }
                        break;
                }
            }
            userLedgerCursor.close();
        }

        // Perform this loop procedure for each Today widget
        for (int appWidgetId : appWidgetIds) {
            // Find the correct layout based on the widget's width
            int widgetWidth = getWidgetWidth(appWidgetManager, appWidgetId);
            int defaultWidth = getResources().getDimensionPixelSize(R.dimen.all_transaction_summary_default_width);
            int largeWidth = getResources().getDimensionPixelSize(R.dimen.all_transaction_summary_large_width);
            int layoutId;
            if (widgetWidth >= largeWidth) {
                layoutId = R.layout.widget_all_transaction_summary;
            } else if (widgetWidth >= defaultWidth) {
                layoutId = R.layout.widget_all_transaction_summary;
            } else {
                layoutId = R.layout.widget_all_transaction_summary;
            }
            RemoteViews views = new RemoteViews(getPackageName(), layoutId);

            views.setTextViewText(R.id.lent_amount, totalLentAmount + "");
            views.setTextViewText(R.id.borrow_amount, totalBorrowAmount + "");
            views.setTextViewText(R.id.monthly_income_amount, currentMonthIncome + "");
            views.setTextViewText(R.id.monthly_expense_amount, currentMonthExpense + "");
            views.setTextViewText(R.id.total_income_amount, totalIncomeAmount + "");
            views.setTextViewText(R.id.total_borrow_amount, totalExpenditureAmount + "");
            // Create an Intent to launch MainActivity
            Intent launchIntent = new Intent(this, HomeActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
            views.setOnClickPendingIntent(R.id.all_transactions_summary, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }


    }

    private int getWidgetWidth(AppWidgetManager appWidgetManager, int appWidgetId) {
        // Prior to Jelly Bean, widgets were always their default size
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return getResources().getDimensionPixelSize(R.dimen.all_transaction_summary_default_width);
        }
        // For Jelly Bean and higher devices, widgets can be resized - the current size can be
        // retrieved from the newly added App Widget Options
        return getWidgetWidthFromOptions(appWidgetManager, appWidgetId);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private int getWidgetWidthFromOptions(AppWidgetManager appWidgetManager, int appWidgetId) {
        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
        if (options.containsKey(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)) {
            int minWidthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
            // The width returned is in dp, but we'll convert it to pixels to match the other widths
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, minWidthDp,
                    displayMetrics);
        }
        return getResources().getDimensionPixelSize(R.dimen.all_transaction_summary_default_width);
    }
}
