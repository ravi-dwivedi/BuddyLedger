package com.ravi.android.buddy.ledger;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by ravi on 12/2/17.
 */

public class CalculatorActivity extends Activity {

    private TextView txtTop; // Reference to EditText of result
    private TextView txtMiddle;
    private TextView txtBottom;
    private int selectedTexBox = 2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);

        // Retrieve a reference to the EditText field for displaying the result.
        txtTop = (TextView) findViewById(R.id.txtTop);
        txtTop.setText("");
        txtMiddle = (TextView) findViewById(R.id.txtMiddle);
        txtMiddle.setText("0");
        txtBottom = (TextView) findViewById(R.id.txtBottom);
        txtBottom.setText("");

        // Register listener (this class) for all the buttons
        BtnListener listener = new BtnListener();
        findViewById(R.id.btnNum0Id).setOnClickListener(listener);
        findViewById(R.id.btnNum1Id).setOnClickListener(listener);
        findViewById(R.id.btnNum2Id).setOnClickListener(listener);
        findViewById(R.id.btnNum3Id).setOnClickListener(listener);
        findViewById(R.id.btnNum4Id).setOnClickListener(listener);
        findViewById(R.id.btnNum5Id).setOnClickListener(listener);
        findViewById(R.id.btnNum6Id).setOnClickListener(listener);
        findViewById(R.id.btnNum7Id).setOnClickListener(listener);
        findViewById(R.id.btnNum8Id).setOnClickListener(listener);
        findViewById(R.id.btnNum9Id).setOnClickListener(listener);
        findViewById(R.id.btnAddId).setOnClickListener(listener);
        findViewById(R.id.btnSubId).setOnClickListener(listener);
        findViewById(R.id.btnMulId).setOnClickListener(listener);
        findViewById(R.id.btnDivId).setOnClickListener(listener);
        findViewById(R.id.btnClearId).setOnClickListener(listener);
        findViewById(R.id.btnEqualId).setOnClickListener(listener);
        findViewById(R.id.btnDecimal).setOnClickListener(listener);
        findViewById(R.id.btnDelId).setOnClickListener(listener);
    }

    private class BtnListener implements View.OnClickListener {
        // On-click event handler for all the buttons
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                // Number buttons: '0' to '9'
                case R.id.btnNum0Id:
                case R.id.btnNum1Id:
                case R.id.btnNum2Id:
                case R.id.btnNum3Id:
                case R.id.btnNum4Id:
                case R.id.btnNum5Id:
                case R.id.btnNum6Id:
                case R.id.btnNum7Id:
                case R.id.btnNum8Id:
                case R.id.btnNum9Id:
                    String inDigit = ((Button) view).getText().toString();
                    Double val = 0D;
                    switch (selectedTexBox) {
                        case 2:
                            val = Double.parseDouble(txtMiddle.getText().toString());
                            if (val != 0) {
                                txtMiddle.setText(txtMiddle.getText().toString() + inDigit);
                            } else if (txtMiddle.getText().toString().contains(".")) {
                                txtMiddle.setText(txtMiddle.getText().toString() + inDigit);
                            } else {
                                txtMiddle.setText(inDigit);
                            }
                            break;
                        case 3:
                            if (txtBottom.getText().toString() != null && !txtBottom.getText().toString().equalsIgnoreCase("")) {
                                val = Double.parseDouble(txtBottom.getText().toString());
                                if (val != 0) {
                                    txtBottom.setText(txtBottom.getText().toString() + inDigit);
                                } else if (txtBottom.getText().toString().contains(".")) {
                                    txtBottom.setText(txtBottom.getText().toString() + inDigit);
                                } else {
                                    txtBottom.setText(inDigit);
                                }
                            } else {
                                txtBottom.setText(inDigit);
                            }
                            break;
                    }
                    break;
                case R.id.btnDecimal:
                    switch (selectedTexBox) {
                        case 2:
                            if (!txtMiddle.getText().toString().contains(".")) {
                                txtMiddle.setText(txtMiddle.getText().toString() + ".");
                            }
                            break;
                        case 3:
                            if (!txtBottom.getText().toString().contains(".")) {
                                txtBottom.setText(txtBottom.getText().toString() + ".");
                            }
                            break;
                    }
                    break;
                case R.id.btnAddId:
                case R.id.btnSubId:
                case R.id.btnMulId:
                case R.id.btnDivId:
                    if (selectedTexBox != 3 && txtMiddle.getText().toString().length() > 0) {
                        selectedTexBox = 3;
                        txtTop.setText(txtMiddle.getText().toString());
                        txtMiddle.setText(((Button) view).getText().toString());
                        txtBottom.setText("0");
                    } else if (selectedTexBox == 3 && txtBottom.getText().toString().length() > 0) {
                        String top = txtTop.getText().toString();
                        String middle = txtMiddle.getText().toString();
                        String bottom = txtBottom.getText().toString();
                        if (top != null && middle != null && bottom != null &&
                                !top.equalsIgnoreCase("") && !middle.equalsIgnoreCase("") && !bottom.equalsIgnoreCase("")) {
                            Double topInt = Double.parseDouble(top);
                            Double bottomInt = Double.parseDouble(bottom);
                            Double result = 0D;
                            switch (middle) {
                                case "+":
                                    result = topInt + bottomInt;
                                    break;
                                case "-":
                                    result = topInt - bottomInt;
                                    break;
                                case "*":
                                    result = topInt * bottomInt;
                                    break;
                                case "/":
                                    if (bottomInt != 0) {
                                        result = topInt / bottomInt;
                                    } else {
                                        result = topInt;
                                    }
                                    break;
                            }
                            if (result == Double.POSITIVE_INFINITY || result == Double.NEGATIVE_INFINITY) {
                                result = 0D;
                                infinityNotAllowed();
                            }
                            txtTop.setText(result + "");
                            txtMiddle.setText(((Button) view).getText().toString());
                            txtBottom.setText("0");
                            selectedTexBox = 3;
                        }
                    }
                    break;
                case R.id.btnEqualId:
                    if (selectedTexBox == 3) {
                        String top = txtTop.getText().toString();
                        String middle = txtMiddle.getText().toString();
                        String bottom = txtBottom.getText().toString();
                        if (top != null && middle != null && bottom != null &&
                                !top.equalsIgnoreCase("") && !middle.equalsIgnoreCase("") && !bottom.equalsIgnoreCase("")) {
                            Double topInt = Double.parseDouble(top);
                            Double bottomInt = Double.parseDouble(bottom);
                            Double result = 0D;
                            switch (middle) {
                                case "+":
                                    result = topInt + bottomInt;
                                    break;
                                case "-":
                                    result = topInt - bottomInt;
                                    break;
                                case "*":
                                    result = topInt * bottomInt;
                                    break;
                                case "/":
                                    if (bottomInt != 0) {
                                        result = topInt / bottomInt;
                                    } else {
                                        result = topInt;
                                    }
                                    break;
                            }
                            txtTop.setText("");
                            if (result == Double.POSITIVE_INFINITY || result == Double.NEGATIVE_INFINITY) {
                                result = 0D;
                                infinityNotAllowed();
                            }
                            txtMiddle.setText(result + "");
                            txtBottom.setText("");
                            selectedTexBox = 2;
                        }
                    }
                    break;
                case R.id.btnClearId:
                    txtTop.setText("");
                    txtMiddle.setText("0");
                    txtBottom.setText("");
                    selectedTexBox = 2;
                    break;
                case R.id.btnDelId:
                    switch (selectedTexBox) {
                        case 2:
                            String txt = txtMiddle.getText().toString();
                            if (txt.length() > 1) {
                                txtMiddle.setText(txt.substring(0, txt.length() - 1));
                            } else if (txt.length() == 1) {
                                txtMiddle.setText("0");
                            }
                            break;
                        case 3:
                            txt = txtBottom.getText().toString();
                            if (txt.length() > 0) {
                                txtBottom.setText(txt.substring(0, txt.length() - 1));
                            } else if (txt.length() == 0) {
                                selectedTexBox = 2;
                                txtBottom.setText("");
                                txtMiddle.setText(txtTop.getText().toString());
                                txtTop.setText("");
                            }
                            break;
                        case R.id.button2:
                            onDone(view);
                    }
                    break;
            }
        }
    }

    public void onDone(View v) {
        Double result = 0D;
        switch (selectedTexBox) {
            case 2:
                if (txtMiddle.getText().length() > 0) {
                    result = Double.parseDouble(txtMiddle.getText().toString());
                }
                break;
            case 3:
                Double num1 = Double.parseDouble(txtTop.getText().toString());
                Double num2 = txtBottom.getText().length() > 0 ? Double.parseDouble(txtBottom.getText().toString()) : 0D;
                String operation = txtMiddle.getText().toString();
                switch (operation) {
                    case "+":
                        result = num1 + num2;
                        break;
                    case "-":
                        result = num1 - num2;
                        break;
                    case "*":
                        result = num1 * num2;
                        break;
                    case "/":
                        if (num2 != 0) {
                            result = num1 / num2;
                        } else {
                            result = num1;
                        }
                        break;
                }
                break;
        }
        Intent data = new Intent();
        if (result == Double.POSITIVE_INFINITY || result == Double.NEGATIVE_INFINITY) {
            infinityNotAllowed();
            result = 0D;
        }
        data.putExtra("RESULT", result);
        setResult(RESULT_OK, data);
        finish();
    }

    public void onCancel(View v) {
        setResult(RESULT_CANCELED);
        finish();
    }

    private void infinityNotAllowed() {
        Toast.makeText(this, getResources().getString(R.string.calculator_toast_message_infinity_not_allowed), Toast.LENGTH_SHORT).show();
    }
}
