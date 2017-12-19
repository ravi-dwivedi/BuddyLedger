package com.ravi.android.buddy.ledger;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.transition.Slide;
import android.transition.TransitionInflater;
import android.view.Menu;
import android.view.MenuItem;

import com.ravi.android.buddy.ledger.fragment.UserProfileFragment;
import com.ravi.android.buddy.ledger.model.Gender;

/**
 * Created by ravi on 5/2/17.
 */

public class UserProfileActivity extends AppCompatActivity {

    private UserProfileFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        if (savedInstanceState == null) {
            Bundle arguments = new Bundle();
            long userId = getIntent().getLongExtra("USER_ID", -1);
            arguments.putLong("USER_ID", userId);
            fragment = new UserProfileFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.user_profile_fragment, fragment, UserProfileFragment.DETAIL_UserProfileFragment)
                    .commit();
        } else {
            fragment = (UserProfileFragment) getSupportFragmentManager().findFragmentByTag(UserProfileFragment.DETAIL_UserProfileFragment);
        }

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setEnterTransition(new Slide().setDuration(500));
            getWindow().setReturnTransition(TransitionInflater.from(this).inflateTransition(R.transition.user_profile_activity_exit));
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
        if (resultCode == RESULT_OK && fragment != null) {
            if (requestCode == ApplicationGlobals.USER_TRANSACTION_OPERATION__ACTIVITY_REQUEST_CODE
                    && data.getBooleanExtra("TRANSACTION_OPEARATION", false)) {
                fragment.setTransactionUpdated(true);
                if (fragment.isAdded())
                    fragment.restartTransactionLoader();
            } else if (requestCode == ApplicationGlobals.USER_OPERATION_ACTIVITY_REQUEST_CODE
                    && data.getBooleanExtra("USER_OPEARATION", false)) {
                fragment.setUserInfoUpdated(true);
                if (fragment.isAdded())
                    fragment.updateUserData(data.getStringExtra("USER_NAME"), data.getStringExtra("IMG_LOC"),
                            data.getStringExtra("GENDER"), data.getStringExtra("EMAIL"), data.getStringExtra("NUMBER"));
            }
        }
    }

    @Override
    public void finish() {
        if (fragment != null) {
            Intent data = new Intent();
            if (fragment.isUserInfoUpdated()) {
                data.putExtra("USER_OPEARATION", true);
            }
            if (fragment.isTransactionUpdated()) {
                data.putExtra("TRANSACTION_OPEARATION", true);
            }
            setResult(RESULT_OK, data);
        }
        super.finish();
    }
}