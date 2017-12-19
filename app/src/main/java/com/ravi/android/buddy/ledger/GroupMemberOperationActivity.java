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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ravi.android.buddy.ledger.CustomUiElement.SearchableSpinner;
import com.ravi.android.buddy.ledger.data.GroupMemberEntry;
import com.ravi.android.buddy.ledger.data.UserEntry;
import com.ravi.android.buddy.ledger.model.Gender;
import com.ravi.android.buddy.ledger.model.GroupMemberOperationType;
import com.ravi.android.buddy.ledger.model.User;
import com.ravi.android.buddy.ledger.utility.DateUtil;
import com.ravi.android.buddy.ledger.utility.UIToolsUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ravi on 25/2/17.
 */

public class GroupMemberOperationActivity extends Activity implements
        AdapterView.OnItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor> {

    private TextView form_title;

    private ImageView member_Image;

    private SearchableSpinner members_list;

    private Cursor mUserDataCursor;
    private final int GROUP_MEMBERS_LOADER = 10;
    private List<String> users = new ArrayList<>();
    //private List<Integer> user_ids = new ArrayList<>();
    private ArrayAdapter spinnerDataAdapter;

    private int selectedBuddyPosition = -1;

    private long group_id;

    private String group_name;

    private boolean delete = false;

    private Button operationButton;

    private Map<Long, Boolean> addedUserIds = new HashMap<>();

    private Boolean dataChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!UIToolsUtil.isScreenLarge(getApplicationContext())) {
            if (getApplicationContext().getResources().getConfiguration().orientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }
        Intent intent = getIntent();
        if (intent == null) {
            finish();
        } else {
            group_id = intent.getLongExtra("GROUP_ID", -1);
            group_name = intent.getStringExtra("GROUP_NAME");
            delete = intent.getBooleanExtra("DELETE", false);
        }
        setContentView(R.layout.activity_group_member_operation);
        operationButton = (Button) findViewById(R.id.memberOperationButton);
        form_title = (TextView) findViewById(R.id.form_title);
        if (!delete) {
            form_title.setText(getResources().getString(R.string.group_member_operation_title_add) + group_name);
            operationButton.setText(getResources().getString(R.string.group_member_operation_add));
        } else {
            form_title.setText(getResources().getString(R.string.group_member_operation_title_remove) + group_name);
            operationButton.setText(getResources().getString(R.string.group_member_operation_remove));
        }
        member_Image = (ImageView) findViewById(R.id.member_image);
        members_list = (SearchableSpinner) findViewById(R.id.member_names_spinner);
        members_list.setOnItemSelectedListener(this);
        getLoaderManager().initLoader(GROUP_MEMBERS_LOADER, null, this);
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
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                GroupMemberEntry.buildGroupMemberUri(),
                GROUP_MEMBERS,
                GroupMemberEntry.COLUMN_GROUP_ID + " = " + group_id,
                null,
                GroupMemberEntry.COLUMN_ADDED_DATE + " ASC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null) {
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

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    StringBuilder condition = new StringBuilder();
                    condition.append("( ");
                    int index = 0;
                    for (long id : addedUserIds.keySet()
                            ) {
                        if (index > 0) {
                            condition.append(", ");
                        }
                        condition.append(id);
                        ++index;
                    }
                    condition.append(")");
                    if (delete) {
                        mUserDataCursor = getContentResolver().query(UserEntry.buildUserUri(), USER_COLUMNS, UserEntry.COLUMN_USER_ID + " in " + condition, null, null);
                    } else {
                        mUserDataCursor = getContentResolver().query(UserEntry.buildUserUri(), USER_COLUMNS, UserEntry.COLUMN_USER_ID + " not in " + condition, null, null);
                    }
                    if (mUserDataCursor != null) {
                        for (int i = 0; i < mUserDataCursor.getCount(); i++) {
                            mUserDataCursor.moveToPosition(i);
                            //user_ids.add(mUserDataCursor.getInt(0));
                            users.add(mUserDataCursor.getString(1));
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setSpinnerAdaptor();
                            }
                        });
                    }
                }
            });
            thread.start();
        }
    }

    private void setSpinnerAdaptor() {
        spinnerDataAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1,
                users);
        members_list.setAdapter(spinnerDataAdapter);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mUserDataCursor = null;
    }

    @Override
    public void finish() {
        if (dataChanged) {
            Intent data = new Intent();
            data.putExtra("GROUP_MEMBER_OPEARATION", true);
            setResult(RESULT_OK, data);
        }
        super.finish();
        overridePendingTransition(0, R.anim.add_activity_exit_animation);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position,
                               long id) {
        selectedBuddyPosition = position;
        mUserDataCursor.moveToPosition(position);
        if (mUserDataCursor.getString(3) != null && !mUserDataCursor.getString(3).equalsIgnoreCase("")) {
            member_Image.setImageURI(Uri.parse(mUserDataCursor.getString(3)));
        } else {
            switch (Gender.valueOf(mUserDataCursor.getString(2))) {
                case FEMALE:
                    member_Image.setImageResource(R.drawable.female_profile_big);
                    break;
                case MALE:
                    member_Image.setImageResource(R.drawable.male_profile_big);
                    break;
            }
        }
        //String label = parent.getItemAtPosition(position).toString();
        //Toast.makeText(parent.getContext(), "Item on position " + position + " : " + spinnerDataAdapter.getItem(position) + " Selected", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }

    public void memberOperation(View view) {
        if (selectedBuddyPosition != -1) {
            mUserDataCursor.moveToPosition(selectedBuddyPosition);
            ContentValues values = new ContentValues();
            values.put(GroupMemberEntry.COLUMN_GROUP_ID, group_id);
            values.put(GroupMemberEntry.COLUMN_USER_ID, mUserDataCursor.getLong(0));
            values.put(GroupMemberEntry.COLUMN_ADDED_DATE, DateUtil.getCurrentFormattedDateTime());
            values.put(GroupMemberEntry.COLUMN_GROUP_OPERATION, !delete ? GroupMemberOperationType.ADD.name() : GroupMemberOperationType.REMOVE.name());
            getContentResolver().insert(GroupMemberEntry.buildGroupMemberUri(), values);
            String message = (delete ? getResources().getString(R.string.group_member_operation_deleted) :
                    getResources().getString(R.string.group_member_operation_added)) + mUserDataCursor.getString(1);
            Snackbar.make(findViewById(R.id.member_operation_form), message, Snackbar.LENGTH_SHORT).show();
            dataChanged = true;
        } else {
            Snackbar.make(findViewById(R.id.member_operation_form), getResources().getString(R.string.group_member_operation_no_member_added_yet), Snackbar.LENGTH_SHORT).show();
        }
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1800);
                    finish();
                } catch (Exception e) {
                    Log.d(this.getClass().getName(), getResources().getString(R.string.thread_sleep_exception));
                }
            }
        });
        thread.start();
    }
}
