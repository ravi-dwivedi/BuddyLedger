package com.ravi.android.buddy.ledger.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;

import com.ravi.android.buddy.ledger.ApplicationGlobals;
import com.ravi.android.buddy.ledger.HomeActivity;
import com.ravi.android.buddy.ledger.R;
import com.ravi.android.buddy.ledger.data.UserEntry;
import com.ravi.android.buddy.ledger.data.UserTransactionEntry;
import com.ravi.android.buddy.ledger.model.UserTransactionType;
import com.ravi.android.buddy.ledger.utility.PrefManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ravi on 5/3/17.
 */

public class OfflineNotificationService extends IntentService {

    private PrefManager prefManager;

    public OfflineNotificationService() {
        super(OfflineNotificationService.class.getName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        reloadData();
        double lentAmount = 0;
        double borrowAmount = 0;
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        prefManager = new PrefManager(getApplicationContext());
        String mCurrency = prefManager.getUserCurrency();
        for (Map.Entry<Long, Double> entry :
                userIdToBalanceMap.entrySet()) {
            if (entry.getValue() != 0) {
                if (entry.getValue() < 0) {
                    lentAmount += entry.getValue();
                    String line = "Lent :: " + userNames.get(entry.getKey());
                    line += " : " + mCurrency;
                    line += -1 * entry.getValue();
                    inboxStyle.addLine(line);
                } else {
                    borrowAmount += entry.getValue();
                    String line = "Borrowed :: " + userNames.get(entry.getKey());
                    line += " :: " + mCurrency;
                    line += entry.getValue();
                    inboxStyle.addLine(line);
                }
            }
        }

        if (lentAmount != 0 || borrowAmount != 0) {
            Intent startApplication = new Intent(this, HomeActivity.class);
            inboxStyle.setSummaryText(getString(R.string.lent_colon) + mCurrency + -1 * lentAmount + "         " +
                    getString(R.string.borrowed_colon) + mCurrency + borrowAmount);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
            notificationBuilder.setAutoCancel(true)
                    .setStyle(inboxStyle)
                    .setContentTitle(getString(R.string.noti_pending_transaction))
                    .setContentText(getString(R.string.lent_colon) + mCurrency + -1 * lentAmount + "         "
                            + getString(R.string.borrowed_colon) + mCurrency + borrowAmount)
                    .setContentIntent(PendingIntent.getActivity(this, 0, startApplication, 0))
                    .setSmallIcon(R.drawable.money_icon)
                    .setDefaults(Notification.DEFAULT_ALL);
            notificationManager.notify(23, notificationBuilder.build());
        }
    }

    Map<Long, String> userNames = new HashMap<>();

    private Map<Long, Double> userIdToBalanceMap = new HashMap<>();

    private final String[] USER_TRANSACTIONS_COLUMNS = {
            UserTransactionEntry.COLUMN_TRANSACTION_TYPE,
            UserTransactionEntry.COLUMN_USER_ID,
            UserTransactionEntry.COLUMN_TRANSACTION_AMOUNT
    };

    private final String[] USER_COLUMNS = {
            UserEntry.TABLE_NAME + "." + UserEntry.COLUMN_USER_ID,
            UserEntry.TABLE_NAME + "." + UserEntry.COLUMN_USER_NAME
    };

    private void reloadData() {
        userNames.clear();
        userIdToBalanceMap.clear();
        Cursor transactionCursor = getApplicationContext().getContentResolver().query(UserTransactionEntry.buildTransactionUri(),
                USER_TRANSACTIONS_COLUMNS, UserTransactionEntry.COLUMN_USER_ID + " != " + ApplicationGlobals.superUserId, null,
                UserTransactionEntry.COLUMN_TRANSACTION_DATE + " DESC ");
        StringBuilder condition = new StringBuilder();
        condition.append("( ");
        if (transactionCursor != null) {
            for (int index = 0; index < transactionCursor.getCount(); index++) {
                transactionCursor.moveToPosition(index);
                if (!userIdToBalanceMap.containsKey(transactionCursor.getLong(1))) {
                    if (userIdToBalanceMap.size() > 0) {
                        condition.append(", ");
                    }
                    condition.append(transactionCursor.getLong(1));
                    userIdToBalanceMap.put(transactionCursor.getLong(1), 0D);
                }
                switch (UserTransactionType.valueOf(transactionCursor.getString(0))) {
                    case LENT:
                        userIdToBalanceMap.put(transactionCursor.getLong(1),
                                userIdToBalanceMap.get(transactionCursor.getLong(1)) - transactionCursor.getDouble(2));
                        break;
                    case BORROW:
                        userIdToBalanceMap.put(transactionCursor.getLong(1),
                                userIdToBalanceMap.get(transactionCursor.getLong(1)) + transactionCursor.getDouble(2));
                        break;
                }
            }
            transactionCursor.close();
        }
        condition.append(" )");
        Cursor userDataCursor = getApplicationContext().getContentResolver().query(UserEntry.buildUserUri(),
                USER_COLUMNS, UserEntry.COLUMN_USER_ID + " in " + condition, null,
                null);
        if (userDataCursor != null) {
            for (int index = 0; index < userDataCursor.getCount(); index++) {
                userDataCursor.moveToPosition(index);
                userNames.put(userDataCursor.getLong(0), userDataCursor.getString(1));
            }
            userDataCursor.close();
        }
    }
}
