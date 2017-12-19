package com.ravi.android.buddy.ledger;

import android.app.Activity;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ravi.android.buddy.ledger.CustomUiElement.SearchableSpinner;
import com.ravi.android.buddy.ledger.data.UserEntry;
import com.ravi.android.buddy.ledger.data.UserTransactionEntry;
import com.ravi.android.buddy.ledger.model.Gender;
import com.ravi.android.buddy.ledger.model.UserTransactionType;
import com.ravi.android.buddy.ledger.utility.DateUtil;
import com.ravi.android.buddy.ledger.utility.PrefManager;
import com.ravi.android.buddy.ledger.utility.UIToolsUtil;
import com.ravi.android.buddy.ledger.widget.TransactionSummaryWidgetProvider;

import java.util.ArrayList;
import java.util.List;

import android.app.LoaderManager;

/**
 * Created by ravi on 11/1/17.
 */

public class TransactionOperationsActivity extends Activity implements
        AdapterView.OnItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor> {

    private final int FRIENDS_LIST__LOADER = 10;

    private SearchableSpinner transaction_buddy_names_spinner;
    private List<String> users = new ArrayList<>();
    //private List<Long> user_ids = new ArrayList<>();
    private ArrayAdapter spinnerDataAdapter;

    private TextView transactionDescription;
    private ImageView transactionCurrencyImage;
    private TextView transactionAmount;
    private Button mButtonLent;
    private Button mButtonBorrow;
    private UserTransactionType userTransactionType = UserTransactionType.LENT;

    private int selectedBuddyPosition = -1;

    private long user_id = -1;

    private long transaction_id = -1;

    private Boolean dataChanged = false;

    private Cursor mUserDataCursor;

    private ImageView transaction_buddy_image;

    private PrefManager prefManager;

    private String mCurrency;

    //private SearchableListAdaptor mSearchableListAdaptor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!UIToolsUtil.isScreenLarge(getApplicationContext())) {
            if (getApplicationContext().getResources().getConfiguration().orientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }
        setContentView(R.layout.activity_transaction_operation);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        transaction_buddy_names_spinner = (SearchableSpinner) findViewById(R.id.transaction_buddy_names_spinner);
        transaction_buddy_names_spinner.setOnItemSelectedListener(this);
        transactionDescription = (TextView) findViewById(R.id.transaction_input_description);
        transactionAmount = (TextView) findViewById(R.id.transaction_amount);
        //transactionCurrencyImage = (ImageView) findViewById(R.id.transaction_currency_image);
        mButtonLent = (Button) findViewById(R.id.lentButton);
        mButtonBorrow = (Button) findViewById(R.id.borrowButton);
        transaction_buddy_image = (ImageView) findViewById(R.id.transaction_buddy_image);
        switchTransactionTypeButtonColors();

        Intent intent = getIntent();
        if (intent != null) {
            updateUIIfDataPresent(intent);
        }

        getLoaderManager().initLoader(FRIENDS_LIST__LOADER, null, this);
        prefManager = new PrefManager(getApplicationContext());
        mCurrency = prefManager.getUserCurrency();
    }

    private void updateUIIfDataPresent(Intent intent) {
        user_id = intent.getLongExtra("USER_ID", -1);
        transaction_id = intent.getLongExtra("TRANSACTION_ID", -1);
        if (transaction_id != -1) {
            transactionAmount.setText(intent.getDoubleExtra("AMOUNT", 0) + "");
            transactionDescription.setText(intent.getStringExtra("DESCRIPTION"));
            String type = intent.getStringExtra("TYPE");
            if (type != null && !type.equalsIgnoreCase("")) {
                switch (UserTransactionType.valueOf(intent.getStringExtra("TYPE"))) {
                    case LENT:
                        userTransactionType = UserTransactionType.LENT;
                        break;
                    case BORROW:
                        userTransactionType = UserTransactionType.BORROW;
                        break;
                }
                switchTransactionTypeButtonColors();
            }
        }
    }

    private Boolean validateTransaction() {
        if (transactionAmount.getText().toString() == null || transactionAmount.getText().toString().equals("") ||
                new Double(transactionAmount.getText().toString()).compareTo(0D) == 0) {
            return false;
        }
        return true;
    }

    public void addTransaction(View view) {
        if (!validateTransaction()) {
            Snackbar.make(findViewById(R.id.user_transaction_form), "Amount must be greater than 0", Snackbar.LENGTH_SHORT).show();
            return;
        }
        String message = "No User To add Transaction";
        if (selectedBuddyPosition != -1) {
            mUserDataCursor.moveToPosition(selectedBuddyPosition);
            ContentValues values = new ContentValues();
            values.put(UserTransactionEntry.COLUMN_TRANSACTION_AMOUNT, transactionAmount.getText().toString());
            values.put(UserTransactionEntry.COLUMN_USER_ID, mUserDataCursor.getLong(0));
            values.put(UserTransactionEntry.COLUMN_TRANSATION_DESCRIPTION, transactionDescription.getText().toString());
            values.put(UserTransactionEntry.COLUMN_TRANSACTION_DATE, DateUtil.getCurrentFormattedDateTime());
            values.put(UserTransactionEntry.COLUMN_TRANSACTION_TYPE, userTransactionType.name());
            if (transaction_id == -1) {
                getContentResolver().insert(UserTransactionEntry.buildTransactionUri(), values);
                message = getResources().getString(R.string.transaction_operation_added) + " : " + userTransactionType.name() + " " + transactionAmount.getText().toString() + " " + mCurrency;
            } else {
                getContentResolver().update(UserTransactionEntry.buildTransactionUri(), values, UserTransactionEntry.COLUMN_TRANSACTION_ID + " = " + transaction_id, null);
                message = getResources().getString(R.string.transaction_operation_updated) + " : " + userTransactionType.name() + " " + transactionAmount.getText().toString() + " " + mCurrency;
            }
            dataChanged = true;
            TransactionSummaryWidgetProvider.sendRefreshWidgetBroadcast(getApplicationContext());
        }
        Snackbar.make(findViewById(R.id.user_transaction_form), message, Snackbar.LENGTH_SHORT).show();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1800);
                    finish();
                } catch (Exception e) {
                    Log.d(this.getClass().getName(), "Exception while Thread.sleep call");
                }
            }
        });
        thread.start();
    }

    public void changeTransactionType(View view) {
        switch (view.getId()) {
            case R.id.lentButton:
                userTransactionType = UserTransactionType.LENT;
                break;
            case R.id.borrowButton:
                userTransactionType = UserTransactionType.BORROW;
        }
        switchTransactionTypeButtonColors();
    }


    private int[][] states = new int[][]{new int[]{android.R.attr.state_enabled},
            new int[]{android.R.attr.state_pressed},
            new int[]{android.R.attr.state_focused},
            new int[]{android.R.attr.state_focused, android.R.attr.state_pressed}
    };

    private int[] lentUpColors = new int[]{Color.GREEN, Color.YELLOW, Color.YELLOW, Color.YELLOW};
    private int[] lentDownColors = new int[]{Color.GRAY, Color.YELLOW, Color.YELLOW, Color.YELLOW};
    private int[] borrowUpColors = new int[]{Color.RED, Color.YELLOW, Color.YELLOW, Color.YELLOW};
    private int[] borrowDownColors = new int[]{Color.GRAY, Color.YELLOW, Color.YELLOW, Color.YELLOW};

    public void switchTransactionTypeButtonColors() {
        switch (userTransactionType) {
            case BORROW:
                mButtonLent.setBackgroundTintList(new ColorStateList(states, lentDownColors));
                mButtonBorrow.setBackgroundTintList(new ColorStateList(states, borrowUpColors));
                break;
            case LENT:
                mButtonLent.setBackgroundTintList(new ColorStateList(states, lentUpColors));
                mButtonBorrow.setBackgroundTintList(new ColorStateList(states, borrowDownColors));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            // finish the activity
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position,
                               long id) {
        //String label = parent.getItemAtPosition(position).toString();
        selectedBuddyPosition = position;
        //Toast.makeText(parent.getContext(), "Item on position " + position + " : " + spinnerDataAdapter.getItem(position) + " Selected", Toast.LENGTH_SHORT).show();
        mUserDataCursor.moveToPosition(position);
        if (mUserDataCursor.getString(3) != null && !mUserDataCursor.getString(3).equalsIgnoreCase("")) {
            transaction_buddy_image.setImageURI(Uri.parse(mUserDataCursor.getString(3)));
        } else {
            switch (Gender.valueOf(mUserDataCursor.getString(2))) {
                case FEMALE:
                    transaction_buddy_image.setImageResource(R.drawable.female_profile_big);
                    break;
                case MALE:
                    transaction_buddy_image.setImageResource(R.drawable.male_profile_big);
                    break;
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
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
        super.onSaveInstanceState(outState);
    }

    private static final String[] FRIENDS_COLUMNS = {
            UserEntry.TABLE_NAME + "." + UserEntry.COLUMN_USER_ID,
            UserEntry.TABLE_NAME + "." + UserEntry.COLUMN_USER_NAME,
            UserEntry.TABLE_NAME + "." + UserEntry.COLUMN_USER_GENDER,
            UserEntry.TABLE_NAME + "." + UserEntry.COLUMN_USER_IMAGE_LOC
    };

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                UserEntry.buildUserUri(),
                FRIENDS_COLUMNS,
                UserEntry.COLUMN_USER_ID + " != " + ApplicationGlobals.superUserId,
                null,
                UserEntry.COLUMN_USER_CREATION_DATE);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null) {
            mUserDataCursor = data;
            for (int index = 0; index < data.getCount(); index++) {
                data.moveToPosition(index);
                //user_ids.add(data.getLong(0));
                users.add(data.getString(1));
                if (user_id != -1 && user_id == data.getInt(0)) {
                    selectedBuddyPosition = index;
                }
            }
            //mSearchableListAdaptor = new SearchableListAdaptor(this, (ArrayList<String>) users);
            //transaction_buddy_names_spinner.setAdapter(mSearchableListAdaptor);
            spinnerDataAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1,
                    users);
            transaction_buddy_names_spinner.setAdapter(spinnerDataAdapter);

            if (user_id != -1) {
                transaction_buddy_names_spinner.setSelection(selectedBuddyPosition);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //user_ids.clear();
        users.clear();
    }

    @Override
    public void finish() {
        if (dataChanged) {
            Intent data = new Intent();
            data.putExtra("TRANSACTION_OPEARATION", true);
            setResult(RESULT_OK, data);
        }
        super.finish();
        overridePendingTransition(0, R.anim.add_activity_exit_animation);
    }
}
