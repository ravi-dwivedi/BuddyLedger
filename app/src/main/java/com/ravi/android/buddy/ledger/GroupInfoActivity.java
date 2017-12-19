package com.ravi.android.buddy.ledger;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.ravi.android.buddy.ledger.fragment.GroupInfoFragment;

/**
 * Created by ravi on 5/2/17.
 */

public class GroupInfoActivity extends AppCompatActivity {

    private GroupInfoFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);

        if (savedInstanceState == null) {
            Bundle arguments = new Bundle();
            long userId = getIntent().getLongExtra("GROUP_ID", -1);
            arguments.putLong("GROUP_ID", userId);

            fragment = new GroupInfoFragment();
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.group_info_fragment, fragment, GroupInfoFragment.DETAIL_GroupInfoFragment)
                    .commit();
        } else {
            fragment = (GroupInfoFragment) getSupportFragmentManager().findFragmentByTag(GroupInfoFragment.DETAIL_GroupInfoFragment);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_user_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.add_transaction) {
            // startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == ApplicationGlobals.GROUP_OPERATION_ACTIVITY_REQUEST_CODE) {
            if (data.getBooleanExtra("GROUP_OPEARATION", false)) {
                fragment.setGroupInfoUpdated(true);
                fragment.updateGroupInfo(data.getStringExtra("GROUP_NAME"),
                        data.getStringExtra("GROUP_TYPE"), data.getStringExtra("GROUP_DESCRIPTION"));
            }
        } else if (resultCode == RESULT_OK &&
                (requestCode == ApplicationGlobals.GROUP_MEMBER_OPERATION_ACTIVITY_REQUEST_CODE ||
                        requestCode == ApplicationGlobals.GROUP_MEMBER_OPERATION_ACTIVITY_REQUEST_CODE)) {
            if (data.getBooleanExtra("GROUP_MEMBER_OPEARATION", false)) {
                fragment.restartGroupMemberListLoader();
            }
        } else if (resultCode == RESULT_OK &&
                requestCode == ApplicationGlobals.GROUP_TRANSACTION_OPERATION_ACTIVITY_REQUEST_CODE) {
            if (data.getBooleanExtra("GROUP_TRANSACTION_OPEARATION", false)) {
                fragment.restartGroupTransactionLoader();
            }
        }
    }

    @Override
    public void finish() {
        if (fragment != null) {
            Intent data = new Intent();
            if (fragment.getGroupInfoUpdated()) {
                data.putExtra("GROUP_OPEARATION", true);
            }
            setResult(RESULT_OK, data);
        }
        super.finish();
    }
}
