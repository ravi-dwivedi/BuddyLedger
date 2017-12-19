package com.ravi.android.buddy.ledger;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.ravi.android.buddy.ledger.fragment.MyLedgerFragment;

/**
 * Created by ravi on 12/2/17.
 */

public class MyLedgerActivity extends AppCompatActivity {

    private MyLedgerFragment myLedgerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_summary);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("My Summary");
        if (savedInstanceState == null) {
            myLedgerFragment = new MyLedgerFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.my_ledger_summary_fragment, myLedgerFragment, MyLedgerFragment.DETAIL_MyLedgerFragment)
                    .commit();
        } else {
            myLedgerFragment = (MyLedgerFragment) getSupportFragmentManager().findFragmentByTag(MyLedgerFragment.DETAIL_MyLedgerFragment);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ApplicationGlobals.USER_LEDGER_OPERATION_ACTIVITY_REQUEST_CODE
                && resultCode == RESULT_OK) {
            long id = data.getLongExtra("ID", -1);
            String type = data.getStringExtra("TYPE");
            Double amount = data.getDoubleExtra("AMOUNT", 0D);
            String details = data.getStringExtra("DETAILS");
            Boolean edit = data.getBooleanExtra("EDIT", false);
            int position = data.getIntExtra("POSITION", -1);
            if (id >= 0) {
                if (edit) {
                    myLedgerFragment.OnItemEdited(id, type, amount, details, position);
                } else {
                    myLedgerFragment.OnItemAdded(id, type, amount, details);
                }
            }
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

    public interface OnItemOperationCallBack {
        void OnItemAdded(long id, String type, Double amount, String details);

        void OnItemEdited(long id, String type, Double amount, String details, int position);
    }
}
