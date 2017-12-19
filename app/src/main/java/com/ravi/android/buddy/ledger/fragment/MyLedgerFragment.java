package com.ravi.android.buddy.ledger.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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

import com.ravi.android.buddy.ledger.ApplicationGlobals;
import com.ravi.android.buddy.ledger.MyLedgerActivity;
import com.ravi.android.buddy.ledger.R;
import com.ravi.android.buddy.ledger.UserLedgerOperationActivity;
import com.ravi.android.buddy.ledger.adaptor.MyLedgerAdaptor;
import com.ravi.android.buddy.ledger.data.UserLedgerEntry;
import com.ravi.android.buddy.ledger.model.UserLedger;
import com.ravi.android.buddy.ledger.model.UserLedgerCustomType;
import com.ravi.android.buddy.ledger.model.UserLedgerType;
import com.ravi.android.buddy.ledger.utility.DateUtil;
import com.ravi.android.buddy.ledger.utility.PrefManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ravi on 12/2/17.
 */

public class MyLedgerFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        MyLedgerAdaptor.OnItemDeleteCallBack, MyLedgerActivity.OnItemOperationCallBack {

    public static final String DETAIL_MyLedgerFragment = "DETAIL_MyLedgerFragment";

    public static final String LOG_TAG = MyLedgerFragment.class.getSimpleName();
    private Context context;

    private static final int MY_TRANSACTION_LIST__LOADER = 1;

    private MyLedgerAdaptor myLedgerAdaptor;
    private String mCurrency;


    private Double totalIncomeAmount = 0D;
    private Double totalExpenditureAmount = 0D;

    private RecyclerView mRecycleView;

    private PrefManager prefManager;

    private TextView fundsText;
    private TextView expenseTextView;
    private TextView incomeTextView;
    private FloatingActionButton fab;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefManager = new PrefManager(context);
        mCurrency = prefManager.getUserCurrency();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_my_summary, container, false);
        mRecycleView = (RecyclerView) rootView.findViewById(R.id.wallet_recycler_view);
        mRecycleView.setLayoutManager(new LinearLayoutManager(context));
        fundsText = (TextView) rootView.findViewById(R.id.wallet_funds_text);
        expenseTextView = (TextView) rootView.findViewById(R.id.wallet_expenses_today);
        incomeTextView = (TextView) rootView.findViewById(R.id.wallet_income_today);
        fundsText.setText(getString(R.string.funds) + mCurrency + (totalIncomeAmount - totalExpenditureAmount));
        expenseTextView.setText(getString(R.string.expense_total) + mCurrency + totalExpenditureAmount);
        incomeTextView.setText(getString(R.string.income_total) + mCurrency + totalIncomeAmount);
        fab = (FloatingActionButton) rootView.findViewById(R.id.fab_add_ledger_item);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onAddFabButtonClick(view);
            }
        });
        fab.show();
        return rootView;
    }

    public void onAddFabButtonClick(View v) {
        Intent intent = new Intent(context, UserLedgerOperationActivity.class);
        ((Activity) context).startActivityForResult(intent, ApplicationGlobals.USER_LEDGER_OPERATION_ACTIVITY_REQUEST_CODE);
        ((Activity) context).overridePendingTransition(R.anim.add_activity_enter_animation, R.anim.no_animation);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(MY_TRANSACTION_LIST__LOADER, null, this);
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
        //mFriendsListAdaptor.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    private static final String[] USER_LEDGER_COLUMNS = {
            UserLedgerEntry.COLUMN_LEDGER_ID,
            UserLedgerEntry.COLUMN_AMOUNT,
            UserLedgerEntry.COLUMN_DATE,
            UserLedgerEntry.COLUMN_DETAILS,
            UserLedgerEntry.COLUMN_TYPE
    };

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(getActivity(),
                UserLedgerEntry.buildUserLedgerUri(),
                USER_LEDGER_COLUMNS,
                null,
                null,
                UserLedgerEntry.COLUMN_DATE + " DESC");

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null) {
            String prevTime = null;
            List<UserLedger> userLedgers = new ArrayList<>();
            for (int index = 0; index < data.getCount(); index++) {
                data.moveToPosition(index);
                switch (UserLedgerType.valueOf(data.getString(4))) {
                    case INCOME:
                        totalIncomeAmount += data.getDouble(1);
                        break;
                    case EXPENSE:
                        totalExpenditureAmount += data.getDouble(1);
                        break;
                }
                if (index == 0 || !DateUtil.isSameDay(data.getString(2), prevTime)) {
                    UserLedger userLedger = new UserLedger();
                    userLedger.setDate(data.getString(2));
                    userLedger.setType(UserLedgerCustomType.DATE.name());
                    userLedgers.add(userLedger);
                }
                UserLedger userLedger = new UserLedger();
                userLedger.setId(data.getInt(0));
                userLedger.setAmount(data.getDouble(1));
                userLedger.setDate(data.getString(2));
                userLedger.setDetails(data.getString(3));
                userLedger.setType(data.getString(4));
                userLedgers.add(userLedger);
                prevTime = userLedger.getDate();
            }
            fundsText.setText(getString(R.string.funds) + mCurrency + (totalIncomeAmount - totalExpenditureAmount));
            expenseTextView.setText(getString(R.string.expense_total) + mCurrency + totalExpenditureAmount);
            incomeTextView.setText(getString(R.string.income_total) + mCurrency + totalIncomeAmount);
            myLedgerAdaptor = new MyLedgerAdaptor(context, mCurrency, userLedgers, this);
            mRecycleView.setAdapter(myLedgerAdaptor);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //myLedgerAdaptor.swapCursor(null);
    }

    @Override
    public void OnItemDeleted(double amount, String ledgerType) {
        if (ledgerType != null && ledgerType != "") {
            if (ledgerType.equalsIgnoreCase(UserLedgerType.INCOME.name())) {
                totalIncomeAmount -= amount;
                incomeTextView.setText(getString(R.string.income_total) + mCurrency + totalIncomeAmount);
                fundsText.setText(getString(R.string.funds) + mCurrency + (totalIncomeAmount - totalExpenditureAmount));
            } else if (ledgerType.equalsIgnoreCase(UserLedgerType.EXPENSE.name())) {
                totalExpenditureAmount -= amount;
                expenseTextView.setText(getString(R.string.expense_total) + mCurrency + totalExpenditureAmount);
                fundsText.setText(getString(R.string.funds) + mCurrency + (totalIncomeAmount - totalExpenditureAmount));
            }
        }
    }

    public void OnItemAdded(long id, String type, Double amount, String details) {

        if (myLedgerAdaptor == null) {
            UserLedger item = new UserLedger();
            item.setId(id);
            item.setType(type);
            item.setDetails(details);
            item.setAmount(amount);
            item.setDate(DateUtil.getCurrentFormattedDateTime());

            List<UserLedger> userLedgers = new ArrayList<>();
            userLedgers.add(item);
            myLedgerAdaptor = new MyLedgerAdaptor(context, mCurrency, new ArrayList<UserLedger>(), this);
            mRecycleView.setAdapter(myLedgerAdaptor);
        } else {
            myLedgerAdaptor.OnItemAdded(id, type, amount, details);
        }

        if (type.equalsIgnoreCase(UserLedgerType.INCOME.name())) {
            totalIncomeAmount += amount;
            incomeTextView.setText(getString(R.string.income_total) + mCurrency + totalIncomeAmount);
        } else {
            totalExpenditureAmount += amount;
            expenseTextView.setText(getString(R.string.expense_total) + mCurrency + totalExpenditureAmount);
        }

        fundsText.setText(getString(R.string.funds) + mCurrency + (totalIncomeAmount - totalExpenditureAmount));
    }

    public void OnItemEdited(long id, String type, Double amount, String details, int position) {
        if (myLedgerAdaptor != null && myLedgerAdaptor.getAdaptorItems() != null &&
                (myLedgerAdaptor.getAdaptorItems().size() >= (position + 1))) {

            if (myLedgerAdaptor.getAdaptorItems().get(position).getType().equalsIgnoreCase(UserLedgerType.INCOME.name())) {
                totalIncomeAmount -= myLedgerAdaptor.getAdaptorItems().get(position).getAmount();
            } else {
                totalExpenditureAmount -= myLedgerAdaptor.getAdaptorItems().get(position).getAmount();
            }

            if (type.equalsIgnoreCase(UserLedgerType.INCOME.name())) {
                totalIncomeAmount += amount;
            } else {
                totalExpenditureAmount += amount;
            }

            incomeTextView.setText(getString(R.string.income_total) + mCurrency + totalIncomeAmount);
            expenseTextView.setText(getString(R.string.expense_total) + mCurrency + totalExpenditureAmount);
            fundsText.setText(getString(R.string.funds) + mCurrency + (totalIncomeAmount - totalExpenditureAmount));

            myLedgerAdaptor.OnItemEdited(id, type, amount, details, position);
        }
    }
}