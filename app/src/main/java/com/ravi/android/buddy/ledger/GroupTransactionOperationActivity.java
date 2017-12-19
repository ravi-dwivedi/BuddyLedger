package com.ravi.android.buddy.ledger;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ravi.android.buddy.ledger.CustomUiElement.SearchableSpinner;
import com.ravi.android.buddy.ledger.data.GroupMemberEntry;
import com.ravi.android.buddy.ledger.data.GroupTransactionEntry;
import com.ravi.android.buddy.ledger.data.UserEntry;
import com.ravi.android.buddy.ledger.data.UserTransactionEntry;
import com.ravi.android.buddy.ledger.model.Gender;
import com.ravi.android.buddy.ledger.model.GroupMemberOperationType;
import com.ravi.android.buddy.ledger.utility.DateUtil;
import com.ravi.android.buddy.ledger.utility.UIToolsUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ravi on 27/2/17.
 */

public class GroupTransactionOperationActivity extends Activity implements
        AdapterView.OnItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor> {

    private TextView form_title;

    private ImageView transaction_buddy_image;

    private SearchableSpinner transaction_buddy_names_spinner;

    private ArrayAdapter spinnerDataAdapter;

    private TextView transactionDescription;

    private TextView transactionAmount;

    private long group_id = -1;

    private final int GROUP_MEMBER_LIST_LOADER = 0;

    private final int USER_DATA_LIST_LOADER = 1;

    private Map<Long, Boolean> addedUserIds = new HashMap<>();

    private List<String> users = new ArrayList<>();

    private int selectedBuddyPosition = -1;

    //private List<Long> user_ids = new ArrayList<>();

    private long transaction_id = -1;

    private long user_id = -1;

    private Boolean dataChanged = false;

    private String group_name = "";

    private Boolean edit = false;

    private Boolean delete = false;

    private Cursor mUserDataCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!UIToolsUtil.isScreenLarge(getApplicationContext())) {
            if (getApplicationContext().getResources().getConfiguration().orientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }
        setContentView(R.layout.activity_group_transaction_operation);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        form_title = (TextView) findViewById(R.id.form_title);
        transaction_buddy_image = (ImageView) findViewById(R.id.transaction_buddy_image);
        transaction_buddy_names_spinner = (SearchableSpinner) findViewById(R.id.transaction_buddy_names_spinner);
        transaction_buddy_names_spinner.setOnItemSelectedListener(this);
        transactionDescription = (TextView) findViewById(R.id.transaction_input_description);
        transactionAmount = (TextView) findViewById(R.id.transaction_amount);
        //transactionCurrencyImage = (ImageView) findViewById(R.id.transaction_currency_image);

        Intent intent = getIntent();
        if (intent != null) {
            group_id = intent.getLongExtra("GROUP_ID", -1);
            group_name = intent.getStringExtra("GROUP_NAME");
            transaction_id = intent.getLongExtra("TRANSACTION_ID", -1);
            user_id = intent.getLongExtra("USER_ID", -1);
            double amount = intent.getDoubleExtra("AMOUNT", 0);
            if (amount > 0) {
                transactionAmount.setText(amount + "");
            }
            transactionDescription.setText(intent.getStringExtra("DESCRIPTION"));
            edit = intent.getBooleanExtra("EDIT", false);
            delete = intent.getBooleanExtra("DELETE", false);
            if (group_id != -1) {
                getLoaderManager().initLoader(GROUP_MEMBER_LIST_LOADER, null, this);
            } else {
                finish();
            }
        }
        form_title.setText(getResources().getString(R.string.group_transaction_operation_transaction_operation_in) + group_name);
    }

    private Boolean validateGroupTransactionFormData() {
        if (transactionAmount.getText().toString() == null || transactionAmount.getText().toString().equalsIgnoreCase("")) {
            return false;
        }
        return true;
    }

    public void addTransaction(View view) {
        if (selectedBuddyPosition != -1) {
            if (!validateGroupTransactionFormData()) {
                Snackbar.make(findViewById(R.id.group_operation_from), "Enter Transaction Amount !!!", Snackbar.LENGTH_SHORT).show();
                return;
            }
            mUserDataCursor.moveToPosition(selectedBuddyPosition);
            ContentValues values = new ContentValues();
            values.put(GroupTransactionEntry.COLUMN_TRANSACTION_AMOUNT, transactionAmount.getText().toString());
            values.put(GroupTransactionEntry.COLUMN_GROUP_ID, group_id);
            values.put(GroupTransactionEntry.COLUMN_USER_ID, mUserDataCursor.getLong(0));
            values.put(GroupTransactionEntry.COLUMN_TRANSACTION_DATE, DateUtil.getCurrentFormattedDateTime());
            values.put(GroupTransactionEntry.COLUMN_TRANSATION_DESCRIPTION, transactionDescription.getText().toString());
            String message = null;
            if (transaction_id == -1) {
                getContentResolver().insert(GroupTransactionEntry.buildGroupTransactionUri(), values);
                message = "Transaction Added";
            } else {
                if (edit) {
                    getContentResolver().update(GroupTransactionEntry.buildGroupTransactionUri(), values,
                            GroupTransactionEntry.COLUMN_TRANSACTION_ID + " = " + transaction_id, null);
                    message = "Transaction Updated";
                } else if (delete) {
                    getContentResolver().delete(GroupTransactionEntry.buildGroupTransactionUri(),
                            GroupTransactionEntry.COLUMN_TRANSACTION_ID + " = " + transaction_id, null);
                    message = "Transaction Deleted";
                }
            }
            dataChanged = true;

            Snackbar.make(findViewById(R.id.group_transaction_operation_form), message, Snackbar.LENGTH_SHORT).show();
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
        } else {
            Snackbar.make(findViewById(R.id.member_operation_form), "Validate Data", Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        selectedBuddyPosition = position;
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
    public void onNothingSelected(AdapterView<?> parent) {
    }

    private final String[] USER_COLUMNS = {
            UserEntry.TABLE_NAME + "." + UserEntry.COLUMN_USER_ID,
            UserEntry.TABLE_NAME + "." + UserEntry.COLUMN_USER_NAME,
            UserEntry.TABLE_NAME + "." + UserEntry.COLUMN_USER_GENDER,
            UserEntry.TABLE_NAME + "." + UserEntry.COLUMN_USER_IMAGE_LOC
    };

    private final String[] GROUP_MEMBERS = {
            GroupMemberEntry.TABLE_NAME + "." + GroupMemberEntry.COLUMN_USER_ID,
            GroupMemberEntry.TABLE_NAME + "." + GroupMemberEntry.COLUMN_GROUP_OPERATION
    };

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case GROUP_MEMBER_LIST_LOADER:
                return new CursorLoader(this,
                        GroupMemberEntry.buildGroupMemberUri(),
                        GROUP_MEMBERS,
                        GroupMemberEntry.COLUMN_GROUP_ID + " = " + group_id,
                        null,
                        GroupMemberEntry.COLUMN_ADDED_DATE + " ASC");
            case USER_DATA_LIST_LOADER:
                StringBuilder condition = new StringBuilder();
                condition.append("( ");
                int index = 0;
                for (long user_id : addedUserIds.keySet()
                        ) {
                    if (index > 0) {
                        condition.append(", ");
                    }
                    condition.append(user_id);
                    ++index;
                }
                condition.append(")");
                return new CursorLoader(this,
                        UserEntry.buildUserUri(),
                        USER_COLUMNS,
                        UserEntry.COLUMN_USER_ID + " in " + condition,
                        null,
                        null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case GROUP_MEMBER_LIST_LOADER:
                for (int index = 0; index < data.getCount(); index++) {
                    data.moveToPosition(index);
                    switch (GroupMemberOperationType.valueOf(data.getString(1))) {
                        case ADD:
                            addedUserIds.put(data.getLong(0), true);
                            break;
                        case REMOVE:
                            addedUserIds.remove(data.getLong(0));
                            break;
                    }
                }
                if (addedUserIds.size() > 0) {
                    getLoaderManager().initLoader(USER_DATA_LIST_LOADER, null, this);
                }
                break;
            case USER_DATA_LIST_LOADER:
                mUserDataCursor = data;
                for (int i = 0; i < data.getCount(); i++) {
                    data.moveToPosition(i);
                    //user_ids.add(data.getLong(0));
                    users.add(data.getString(1));
                    if (transaction_id != -1 && user_id != -1 && user_id == data.getInt(0)) {
                        selectedBuddyPosition = i;
                    }
                }
                spinnerDataAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1,
                        users);
                transaction_buddy_names_spinner.setAdapter(spinnerDataAdapter);
                if (transaction_id != -1 && user_id != -1) {
                    transaction_buddy_names_spinner.setSelection(selectedBuddyPosition);
                }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public void finish() {
        if (dataChanged) {
            Intent data = new Intent();
            data.putExtra("GROUP_TRANSACTION_OPEARATION", true);
            setResult(RESULT_OK, data);
        }
        super.finish();
        overridePendingTransition(0, R.anim.add_activity_exit_animation);
    }
}
