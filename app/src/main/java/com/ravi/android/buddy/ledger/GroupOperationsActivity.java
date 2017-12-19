package com.ravi.android.buddy.ledger;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.ravi.android.buddy.ledger.data.GroupEntry;
import com.ravi.android.buddy.ledger.data.GroupMemberEntry;
import com.ravi.android.buddy.ledger.data.UserEntry;
import com.ravi.android.buddy.ledger.model.GroupMemberOperationType;
import com.ravi.android.buddy.ledger.utility.DateUtil;
import com.ravi.android.buddy.ledger.utility.UIToolsUtil;

/**
 * Created by ravi on 11/1/17.
 */

public class GroupOperationsActivity extends Activity {
    private Spinner groupsSpinner;
    private TextView groupNameTextView;
    private TextView groupDescriptionTextView;

    private Boolean edit = false;
    private long group_id = -1;

    private boolean dataChanged = false;

    private Button addGroupButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!UIToolsUtil.isScreenLarge(getApplicationContext())) {
            if (getApplicationContext().getResources().getConfiguration().orientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }
        setContentView(R.layout.activity_group_operation);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        groupsSpinner = (Spinner) findViewById(R.id.group_types_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.group_types, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        groupsSpinner.setAdapter(adapter);
        attachEventToSpinner();
        groupNameTextView = (TextView) findViewById(R.id.groupName);
        groupDescriptionTextView = (TextView) findViewById(R.id.groupDescription);
        addGroupButton = (Button) findViewById(R.id.addGroupButton);

        Intent intent = getIntent();
        if (intent != null) {
            group_id = intent.getLongExtra("GROUP_ID", -1);
            groupNameTextView.setText(intent.getStringExtra("GROUP_NAME"));
            groupDescriptionTextView.setText(intent.getStringExtra("GROUP_DESCRIPTION"));
            groupsSpinner.setSelection(adapter.getPosition(intent.getStringExtra("GROUP_TYPE")));
        }
        if (group_id != -1) {
            edit = true;
            addGroupButton.setText(getResources().getString(R.string.group_operation_update));
        }
    }

    private Boolean validateGroupFormData() {
        if (groupNameTextView.getText().toString() == null || groupNameTextView.getText().toString().equals("")) {
            return false;
        }
        return true;
    }

    public void addGroup(View view) {
        if (!validateGroupFormData()) {
            Snackbar.make(findViewById(R.id.group_operation_from), "Enter Group Name !!!", Snackbar.LENGTH_SHORT).show();
            return;
        }
        String message;
        ContentValues values = new ContentValues();
        values.put(GroupEntry.COLUMN_GROUP_NAME, groupNameTextView.getText().toString());
        values.put(GroupEntry.COLUMN_GROUP_TYPE, groupsSpinner.getSelectedItem().toString());
        values.put(GroupEntry.COLUMN_GROUP_DESCRIPTION, groupDescriptionTextView.getText().toString());
        if (!edit) {
            message = getResources().getString(R.string.group_operation_added);
            values.put(GroupEntry.COLUMN_GROUP_CREATION_DATE, DateUtil.getCurrentFormattedDateTime());
            group_id = GroupEntry.getGroupIdFromUri(getContentResolver().insert(GroupEntry.buildGroupUri(), values));
            values = new ContentValues();
            values.put(GroupMemberEntry.COLUMN_GROUP_ID, group_id);
            values.put(GroupMemberEntry.COLUMN_USER_ID, ApplicationGlobals.superUserId);
            values.put(GroupMemberEntry.COLUMN_ADDED_DATE, DateUtil.getCurrentFormattedDateTime());
            values.put(GroupMemberEntry.COLUMN_GROUP_OPERATION, GroupMemberOperationType.ADD.name());
            getContentResolver().insert(GroupMemberEntry.buildGroupMemberUri(), values);
        } else {
            message = getResources().getString(R.string.group_operation_updated);
            getContentResolver().update(GroupEntry.buildGroupUri(), values, GroupEntry.COLUMN_GROUP_ID + " = " + group_id, null);
        }
        dataChanged = true;
        message = message + " " + groupNameTextView.getText().toString();
        Snackbar.make(findViewById(R.id.group_operation_from), message, Snackbar.LENGTH_SHORT).show();
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

    private void attachEventToSpinner() {
        groupsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // your code here
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });
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
    public void finish() {
        if (dataChanged) {
            Intent data = new Intent();
            data.putExtra("GROUP_OPEARATION", true);
            if (edit) {
                data.putExtra("GROUP_NAME", groupNameTextView.getText().toString());
                data.putExtra("GROUP_TYPE", groupsSpinner.getSelectedItem().toString());
                data.putExtra("GROUP_DESCRIPTION", groupDescriptionTextView.getText().toString());
            }
            setResult(RESULT_OK, data);
        }
        super.finish();
        overridePendingTransition(0, R.anim.add_activity_exit_animation);
    }
}

