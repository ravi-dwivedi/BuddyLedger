package com.ravi.android.buddy.ledger;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ravi.android.buddy.ledger.data.UserLedgerEntry;
import com.ravi.android.buddy.ledger.model.UserLedgerType;
import com.ravi.android.buddy.ledger.utility.DateUtil;
import com.ravi.android.buddy.ledger.utility.PrefManager;
import com.ravi.android.buddy.ledger.utility.UIToolsUtil;

/**
 * Created by ravi on 12/2/17.
 */

public class UserLedgerOperationActivity extends Activity {
    private String type = UserLedgerType.EXPENSE.name();
    private long id = -1;
    private String details;
    private double amount = 0;
    private Context context;
    private String mCurrency;
    private PrefManager prefManager;
    private boolean edit = false;
    private int position = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (UIToolsUtil.isScreenLarge(getApplicationContext())) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        }
        setContentView(R.layout.activity_user_ledger_operation);
        context = getApplicationContext();
        getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, convertDPToPx(context, 235)); // 335px - 220dp
        TextView textView = (TextView) findViewById(R.id.add_wallet_item_detail_name);
        textView.setText(getString(R.string.expense));
        textView.setTextColor(Color.RED);
        prefManager = new PrefManager(context);
        mCurrency = prefManager.getUserCurrency();
        final Button amountView = (Button) findViewById(R.id.add_wallet_item_amount_wrapper);
        edit = getIntent().getBooleanExtra("EDIT", false);
        if (edit) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    id = getIntent().getLongExtra("ID", 0);
                    amount = getIntent().getDoubleExtra("AMOUNT", 0);
                    details = getIntent().getStringExtra("DETAILS");
                    type = getIntent().getStringExtra("TYPE");
                    position = getIntent().getIntExtra("POSITION", -1);
                    RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.add_wallet_item_details);
                    amountView.setText(getString(R.string.amount_colon) + mCurrency + Double.toString(amount));
                    TextView detailsText = (TextView) findViewById(R.id.add_wallet_item_detail);
                    detailsText.setText(details);
                    TextView textView = (TextView) relativeLayout.findViewById(R.id.add_wallet_item_detail_balance);
                    textView.setText(getString(R.string.amount_colon) + mCurrency + amount);
                    textView = (TextView) findViewById(R.id.add_wallet_item_detail_name);
                    textView.setText(type);
                    RadioGroup radioGroup = (RadioGroup) findViewById(R.id.type_radio_options);
                    if (type.equals(UserLedgerType.INCOME.name())) {
                        radioGroup.check(R.id.radio_button_income);
                        textView.setTextColor(setColorGreen());
                    } else
                        radioGroup.check(R.id.radio_button_expenditure);
                    Button doneButton = (Button) findViewById(R.id.done_button_add_wallet_item);
                    doneButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onEditWalletItem();
                        }
                    });
                    doneButton.setText(getString(R.string.edit));
                    amountView.setTextColor(Color.BLACK);
                    getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, convertDPToPx(context, 330)); // 445px - 300dp
                    relativeLayout.setVisibility(View.VISIBLE);
                }
            });
            thread.run();
        }
    }

    @Override
    public void finish() {
        Intent data = new Intent();
        data.putExtra("ID", id);
        data.putExtra("AMOUNT", amount);
        data.putExtra("TYPE", type);
        data.putExtra("DETAILS", details);
        data.putExtra("EDIT", edit);
        data.putExtra("POSITION", position);
        setResult(RESULT_OK, data);
        super.finish();
        overridePendingTransition(0, R.anim.add_activity_exit_animation);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 200) {
            if (resultCode == RESULT_OK) {
                Double result = data.getDoubleExtra("RESULT", 0);
                Button amountView = (Button) findViewById(R.id.add_wallet_item_amount_wrapper);
                RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.add_wallet_item_details);
                if (result == 0) {
                    amount = 0;
                    relativeLayout.setVisibility(View.GONE);
                    getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, convertDPToPx(context, 235)); // 335px - 220dp
                    amountView.setText(getString(R.string.enter_amount));
                } else {
                    amount = result;
                    if (amount < 0) {
                        type = UserLedgerType.EXPENSE.name();
                        amount = -1 * amount;
                        RadioGroup group = (RadioGroup) findViewById(R.id.type_radio_options);
                        group.check(R.id.radio_button_expenditure);
                    }
                    TextView textView = (TextView) relativeLayout.findViewById(R.id.add_wallet_item_detail_balance);
                    textView.setText(getString(R.string.amount_colon) + mCurrency + amount);
                    textView = (TextView) findViewById(R.id.add_wallet_item_detail_name);
                    textView.setText(type);
                    amountView.setText(getString(R.string.amount_colon) + mCurrency + amount);
                    amountView.setTextColor(Color.BLACK);
                    if (type.equals(UserLedgerType.INCOME.name())) {
                        textView.setTextColor(setColorGreen());
                    } else {
                        textView.setTextColor(setColorRed());
                    }
                    getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, convertDPToPx(context, 330)); // 445px - 300dp
                    relativeLayout.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    public void onTypeRadioButtonSelected(View v) {
        TextView textView = (TextView) findViewById(R.id.add_wallet_item_detail_name);
        switch (v.getId()) {
            case (R.id.radio_button_expenditure):
                textView.setText(getString(R.string.expense));
                textView.setTextColor(setColorRed());
                type = UserLedgerType.EXPENSE.name();
                break;
            case R.id.radio_button_income:
                textView.setText(getString(R.string.income));
                textView.setTextColor(setColorGreen());
                type = UserLedgerType.INCOME.name();
        }
    }

    public void onAddWalletItem(View v) {
        Button amountLayout = (Button) findViewById(R.id.add_wallet_item_amount_wrapper);
        if (amount == 0) {
            amountLayout.setTextColor(Color.RED);
            Toast.makeText(this, getString(R.string.enter_amount), Toast.LENGTH_SHORT).show();
            return;
        }
        TextView detailsText = (TextView) findViewById(R.id.add_wallet_item_detail);
        details = detailsText.getText().toString();
        Snackbar.make(findViewById(R.id.new_wallet_transaction), "Added Transaction: " + type + ": " + mCurrency
                + amount, Snackbar.LENGTH_SHORT).show();
        saveItemToDatabase(type, details, amount);
        amountLayout.setText(getString(R.string.enter_amount));
        amountLayout.setTextColor(Color.BLACK);
        detailsText.setText(null);
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.add_wallet_item_details);
        relativeLayout.setVisibility(View.GONE);
        getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, convertDPToPx(context, 235));
    }

    public void onEditWalletItem() {
        Button amountLayout = (Button) findViewById(R.id.add_wallet_item_amount_wrapper);
        if (amount == 0) {
            amountLayout.setTextColor(Color.RED);
            Toast.makeText(this, getString(R.string.enter_amount), Toast.LENGTH_SHORT).show();
            return;
        }
        TextView detailsText = (TextView) findViewById(R.id.add_wallet_item_detail);
        details = detailsText.getText().toString();
        //Snackbar.make(findViewById(R.id.new_wallet_transaction), "Added Transaction: " + type + ": " + amount, Snackbar.LENGTH_SHORT).show();
        onEditTransactionDetails(id, type, details, amount);
    }

    public int convertDPToPx(Context context, float dp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics);
    }

    public void startCalCActivity(View v) {
        Intent calcActivity = new Intent(this, CalculatorActivity.class);
        startActivityForResult(calcActivity, 200);
    }

    public int setColorRed() {
        return Color.parseColor("#ffc94c4c");
    }

    public int setColorGreen() {
        return Color.parseColor("#ff509f4c");
    }

    private void saveItemToDatabase(String type, String details, Double amount) {
        ContentValues values = new ContentValues();
        values.put(UserLedgerEntry.COLUMN_AMOUNT, amount);
        values.put(UserLedgerEntry.COLUMN_DATE, DateUtil.getCurrentFormattedDateTime());
        values.put(UserLedgerEntry.COLUMN_DETAILS, details);
        values.put(UserLedgerEntry.COLUMN_TYPE, type);
        id = UserLedgerEntry.getIdFromUri(getContentResolver().insert(UserLedgerEntry.buildUserLedgerUri(), values));
    }

    private void onEditTransactionDetails(long id, String type, String details, Double amount) {
        ContentValues values = new ContentValues();
        values.put(UserLedgerEntry.COLUMN_AMOUNT, amount);
        values.put(UserLedgerEntry.COLUMN_DETAILS, details);
        values.put(UserLedgerEntry.COLUMN_TYPE, type);
        getContentResolver().update(UserLedgerEntry.buildUserLedgerUri(), values, UserLedgerEntry.COLUMN_LEDGER_ID + " = " + id, null);
    }

}
