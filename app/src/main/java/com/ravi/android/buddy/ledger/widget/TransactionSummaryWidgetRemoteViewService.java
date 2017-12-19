package com.ravi.android.buddy.ledger.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.ravi.android.buddy.ledger.ApplicationGlobals;
import com.ravi.android.buddy.ledger.R;
import com.ravi.android.buddy.ledger.UserProfileActivity;
import com.ravi.android.buddy.ledger.data.UserEntry;
import com.ravi.android.buddy.ledger.data.UserTransactionEntry;
import com.ravi.android.buddy.ledger.model.User;
import com.ravi.android.buddy.ledger.model.UserTransactionType;
import com.ravi.android.buddy.ledger.utility.PrefManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ravi on 5/3/17.
 */

public class TransactionSummaryWidgetRemoteViewService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new TransactionSummaryRemoteViewFactory(this);
    }

    class TransactionSummaryRemoteViewFactory implements RemoteViewsFactory {
        private Context mContext;
        private String mCurrency;
        private PrefManager prefManager;

        private Map<Long, Double> userIdToBalanceMap = new HashMap<>();

        private final String[] USER_TRANSACTIONS_COLUMNS = {
                UserTransactionEntry.COLUMN_TRANSACTION_TYPE,
                UserTransactionEntry.COLUMN_USER_ID,
                UserTransactionEntry.COLUMN_TRANSACTION_AMOUNT
        };

        private final String[] USER_COLUMNS = {
                UserEntry.TABLE_NAME + "." + UserEntry.COLUMN_USER_ID,
                UserEntry.TABLE_NAME + "." + UserEntry.COLUMN_USER_NAME,
                UserEntry.TABLE_NAME + "." + UserEntry.COLUMN_USER_IMAGE_LOC,
                UserEntry.TABLE_NAME + "." + UserEntry.COLUMN_USER_GENDER
        };

        List<User> users = new ArrayList<>();

        TransactionSummaryRemoteViewFactory(Context context) {
            super();
            this.mContext = context;
        }

        @Override
        public void onCreate() {
            prefManager = new PrefManager(mContext);
            mCurrency = prefManager.getUserCurrency();
            reloadData();
        }

        @Override
        public void onDataSetChanged() {
            reloadData();
        }

        @Override
        public void onDestroy() {
        }

        @Override
        public int getCount() {
            return users.size() - 1;
        }

        @Override
        public RemoteViews getViewAt(int i) {
            RemoteViews holder = new RemoteViews(mContext.getPackageName(), R.layout.item_transaction_summary_widget);
            holder.setTextViewText(R.id.user_name, users.get(i).getName());
            if (userIdToBalanceMap.get(users.get(i).getId()) < 0) {
                holder.setTextColor(R.id.transaction_amount_text, Color.parseColor("#ffc94c4c"));
                holder.setTextColor(R.id.transaction_amount_type, Color.parseColor("#ffc94c4c"));
                holder.setTextViewText(R.id.transaction_amount_type, "Owes : ");
                holder.setTextViewText(R.id.transaction_amount_text, (userIdToBalanceMap.get(users.get(i).getId()) * -1) + mCurrency);
            } else if (userIdToBalanceMap.get(users.get(i).getId()) > 0) {
                holder.setTextColor(R.id.transaction_amount_text, Color.parseColor("#ff509f4c"));
                holder.setTextColor(R.id.transaction_amount_type, Color.parseColor("#ff509f4c"));
                holder.setTextViewText(R.id.transaction_amount_type, "You Owes : ");
                holder.setTextViewText(R.id.transaction_amount_text, userIdToBalanceMap.get(users.get(i).getId()) + mCurrency);
            } else {
                holder.setTextViewText(R.id.transaction_amount_type, "Balanced");
                holder.setTextViewText(R.id.transaction_amount_text, "");
            }
            if (users.get(i).getUserImageLocation() != null && !users.get(i).getUserImageLocation().equalsIgnoreCase("")) {
                holder.setImageViewUri(R.id.user_prifile_icon, Uri.parse(users.get(i).getUserImageLocation()));
            } else {
                switch (users.get(i).getGender()) {
                    case "MALE":
                        holder.setImageViewResource(R.id.user_prifile_icon, R.drawable.male_profile_big);
                        break;
                    case "FEMALE":
                        holder.setImageViewResource(R.id.user_prifile_icon, R.drawable.female_profile_big);
                        break;
                }
            }
            Intent intent = new Intent(mContext, UserProfileActivity.class);
            intent.putExtra("USER_ID", users.get(i).getId());
            holder.setOnClickFillInIntent(R.id.item_transaction_summary_widget, intent);
            return holder;
        }

        @Override
        public RemoteViews getLoadingView() {
            return new RemoteViews(getPackageName(), R.layout.item_transaction_summary_widget);
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int i) {
            return users.get(i).getId();
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        private void reloadData() {
            users.clear();
            userIdToBalanceMap.clear();
            Cursor transactionCursor = mContext.getContentResolver().query(UserTransactionEntry.buildTransactionUri(),
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
            Cursor userDataCursor = mContext.getContentResolver().query(UserEntry.buildUserUri(),
                    USER_COLUMNS, UserEntry.COLUMN_USER_ID + " in " + condition, null,
                    null);
            if (userDataCursor != null) {
                for (int index = 0; index < userDataCursor.getCount(); index++) {
                    userDataCursor.moveToPosition(index);
                    User u = new User();
                    u.setId(userDataCursor.getLong(0));
                    u.setName(userDataCursor.getString(1));
                    u.setUserImageLocation(userDataCursor.getString(2));
                    u.setGender(userDataCursor.getString(3));
                    users.add(u);
                }
                userDataCursor.close();
            }
        }
    }
}
