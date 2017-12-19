package com.ravi.android.buddy.ledger.fragment;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ravi.android.buddy.ledger.HomeActivity;
import com.ravi.android.buddy.ledger.R;
import com.ravi.android.buddy.ledger.adaptor.SummaryListAdaptor;
import com.ravi.android.buddy.ledger.data.UserEntry;
import com.ravi.android.buddy.ledger.data.UserTransactionEntry;
import com.ravi.android.buddy.ledger.model.User;
import com.ravi.android.buddy.ledger.model.UserLedgerCustomType;
import com.ravi.android.buddy.ledger.model.UserTransaction;
import com.ravi.android.buddy.ledger.model.UserTransactionType;
import com.ravi.android.buddy.ledger.utility.DateUtil;
import com.ravi.android.buddy.ledger.utility.PrefManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ravi on 10/1/17.
 */

public class SummaryFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = SummaryFragment.class.getSimpleName();
    private Context context;
    private SummaryListAdaptor mSummaryListAdaptor;
    private RecyclerView mRecyclerView;
    private static final int SUMMARY_LIST__LOADER = 0;
    private static final int USER_LIST__LOADER = 1;

    private PrefManager prefManager;

    private double totalLentAmount = 0D;
    private double totalBorrowAmount = 0D;

    private List<UserTransaction> userTransactions = new ArrayList<>();

    private Map<Long, User> allUsers = new HashMap<>();
    private List<Map<Long, Double>> perMonthUserTotal = new ArrayList<>();

    private String mCurrency;

    private TextView lentTextView;
    private TextView borrowTextView;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_summary_list, container, false);

        // Get a reference to the RecyclerView, and attach this adapter to it.
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view_summary_list);
        lentTextView = (TextView) rootView.findViewById(R.id.lent_header);
        borrowTextView = (TextView) rootView.findViewById(R.id.borrowed_header);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);
        //mSummaryListAdaptor = new SummaryListAdaptor(getActivity());
        //mRecyclerView.setAdapter(mSummaryListAdaptor);
        prefManager = new PrefManager(context);
        mCurrency = prefManager.getUserCurrency();
        restartSummaryListLoader();

        return rootView;
    }

    public void restartSummaryListLoader() {
        userTransactions.clear();
        allUsers.clear();
        perMonthUserTotal.clear();
        getLoaderManager().restartLoader(SUMMARY_LIST__LOADER, null, this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // We hold for transition here just in-case the activity
        // needs to be re-created. In a standard return transition,
        // this doesn't actually make a difference.
        getActivity().supportPostponeEnterTransition();
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // When tablets rotate, the currently selected list item needs to be saved.
        //mSummaryListAdaptor.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    private static final String[] SUMMARY_COLUMNS = {
            UserTransactionEntry.TABLE_NAME + "." + UserTransactionEntry.COLUMN_TRANSACTION_ID,
            UserTransactionEntry.TABLE_NAME + "." + UserTransactionEntry.COLUMN_USER_ID,
            UserTransactionEntry.TABLE_NAME + "." + UserTransactionEntry.COLUMN_TRANSACTION_AMOUNT,
            UserTransactionEntry.TABLE_NAME + "." + UserTransactionEntry.COLUMN_TRANSACTION_TYPE,
            UserTransactionEntry.TABLE_NAME + "." + UserTransactionEntry.COLUMN_TRANSATION_DESCRIPTION,
            UserTransactionEntry.TABLE_NAME + "." + UserTransactionEntry.COLUMN_TRANSACTION_DATE
    };

    private static final String[] FRIENDS_COLUMNS = {
            UserEntry.TABLE_NAME + "." + UserEntry.COLUMN_USER_ID,
            UserEntry.TABLE_NAME + "." + UserEntry.COLUMN_USER_NAME,
            UserEntry.TABLE_NAME + "." + UserEntry.COLUMN_USER_GENDER,
            UserEntry.TABLE_NAME + "." + UserEntry.COLUMN_USER_IMAGE_LOC
    };

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        switch (i) {
            case SUMMARY_LIST__LOADER:
                return new CursorLoader(getActivity(),
                        UserTransactionEntry.buildTransactionUri(),
                        SUMMARY_COLUMNS,
                        null,
                        null,
                        UserTransactionEntry.COLUMN_TRANSACTION_DATE + " ASC ");
            case USER_LIST__LOADER:
                return new CursorLoader(getActivity(),
                        UserEntry.buildUserUri(),
                        FRIENDS_COLUMNS,
                        null,
                        null,
                        UserEntry.COLUMN_USER_CREATION_DATE + " DESC ");
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, final Cursor data) {
        if (data != null) {
            switch (loader.getId()) {
                case SUMMARY_LIST__LOADER:
                    Thread thread1 = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String prevDate = null;
                            for (int index = 0; index < data.getCount(); index++) {
                                data.moveToPosition(index);

                                if (index == 0 || !DateUtil.isSameMonth(prevDate, data.getString(5))) {
                                    perMonthUserTotal.add(new HashMap<Long, Double>());
                                }

                                if (!perMonthUserTotal.get(perMonthUserTotal.size() - 1).containsKey(data.getLong(1))) {
                                    perMonthUserTotal.get(perMonthUserTotal.size() - 1).put(data.getLong(1), 0D);
                                }
                                switch (UserTransactionType.valueOf(data.getString(3))) {
                                    case BORROW:
                                        perMonthUserTotal.get(perMonthUserTotal.size() - 1).put(data.getLong(1),
                                                perMonthUserTotal.get(perMonthUserTotal.size() - 1).get(data.getLong(1)) - data.getDouble(2));
                                        totalBorrowAmount += data.getDouble(2);
                                        break;
                                    case LENT:
                                        perMonthUserTotal.get(perMonthUserTotal.size() - 1).put(data.getLong(1),
                                                perMonthUserTotal.get(perMonthUserTotal.size() - 1).get(data.getLong(1)) + data.getDouble(2));
                                        totalLentAmount += data.getDouble(2);
                                        break;
                                }

                                UserTransaction userTransaction;

                                if (index != 0 && !DateUtil.isSameMonth(data.getString(5), prevDate)) {
                                    userTransaction = new UserTransaction();
                                    userTransaction.setTransaction_type(UserLedgerCustomType.DATE.name());
                                    userTransaction.setTransaction_date(prevDate);
                                    userTransaction.setMonthTransactionIndex(perMonthUserTotal.size() - 2);
                                    userTransactions.add(0, userTransaction);

                                    userTransaction = new UserTransaction();
                                    userTransaction.setTransaction_type(UserLedgerCustomType.MONTH.name());
                                    userTransaction.setTransaction_date(prevDate);
                                    userTransaction.setMonthTransactionIndex(perMonthUserTotal.size() - 2);
                                    userTransactions.add(0, userTransaction);
                                }

                                if (index != 0 && !DateUtil.isSameDay(data.getString(5), prevDate) &&
                                        DateUtil.isSameMonth(data.getString(5), prevDate)) {
                                    userTransaction = new UserTransaction();
                                    userTransaction.setTransaction_type(UserLedgerCustomType.DATE.name());
                                    userTransaction.setTransaction_date(prevDate);
                                    userTransaction.setMonthTransactionIndex(perMonthUserTotal.size() - 1);
                                    userTransactions.add(0, userTransaction);
                                }

                                userTransaction = new UserTransaction();
                                userTransaction.setId(data.getLong(0));
                                userTransaction.setUserId(data.getLong(1));
                                userTransaction.setAmount(data.getDouble(2));
                                userTransaction.setTransaction_type(data.getString(3));
                                userTransaction.setDescription(data.getString(4));
                                userTransaction.setTransaction_date(data.getString(5));
                                userTransaction.setMonthTransactionIndex(perMonthUserTotal.size() - 1);
                                userTransactions.add(0, userTransaction);

                                if (index == (data.getCount() - 1)) {
                                    userTransaction = new UserTransaction();
                                    userTransaction.setTransaction_type(UserLedgerCustomType.DATE.name());
                                    userTransaction.setTransaction_date(data.getString(5));
                                    userTransaction.setMonthTransactionIndex(perMonthUserTotal.size() - 1);
                                    userTransactions.add(0, userTransaction);
                                }

                                if (index == (data.getCount() - 1)) {
                                    userTransaction = new UserTransaction();
                                    userTransaction.setTransaction_type(UserLedgerCustomType.MONTH.name());
                                    userTransaction.setTransaction_date(data.getString(5));
                                    userTransaction.setMonthTransactionIndex(perMonthUserTotal.size() - 1);
                                    userTransactions.add(0, userTransaction);
                                }
                                prevDate = data.getString(5);
                            }
                            ((HomeActivity) context).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //Set Header Details
                                    lentTextView.setText(getResources().getString(R.string.lent_header) + mCurrency + totalLentAmount);
                                    borrowTextView.setText(getResources().getString(R.string.borrowed_header) + mCurrency + totalBorrowAmount);
                                    getLoaderManager().destroyLoader(SUMMARY_LIST__LOADER);
                                    restartUserListLoader();
                                }
                            });
                        }
                    });
                    thread1.start();
                    break;
                case USER_LIST__LOADER:
                    Thread thread2 = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            for (int index = 0; index < data.getCount(); index++) {
                                data.moveToPosition(index);
                                User u = new User();
                                u.setId(data.getLong(0));
                                u.setName(data.getString(1));
                                u.setGender(data.getString(2));
                                u.setUserImageLocation(data.getString(3));
                                allUsers.put(u.getId(), u);
                            }
                            mSummaryListAdaptor = new SummaryListAdaptor(context, userTransactions, allUsers, perMonthUserTotal, prefManager.getUserCurrency());
                            ((HomeActivity) context).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mRecyclerView.setAdapter(mSummaryListAdaptor);
                                    mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
                                    getLoaderManager().destroyLoader(USER_LIST__LOADER);
                                }
                            });
                        }
                    });
                    thread2.start();
                    break;
            }
        }
    }

    private void restartUserListLoader() {
        getLoaderManager().restartLoader(USER_LIST__LOADER, null, this);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //mSummaryListAdaptor.swapCursor(null);
    }
}
