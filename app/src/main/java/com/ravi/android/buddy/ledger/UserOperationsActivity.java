package com.ravi.android.buddy.ledger;

import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FilterQueryProvider;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.ravi.android.buddy.ledger.data.UserEntry;
import com.ravi.android.buddy.ledger.model.Gender;
import com.ravi.android.buddy.ledger.utility.DateUtil;
import com.ravi.android.buddy.ledger.utility.UIToolsUtil;

/**
 * Created by ravi on 11/1/17.
 */

public class UserOperationsActivity extends Activity {

    private AutoCompleteTextView searchableBuddyName;
    private TextView buddyEmailTextView;
    private TextView buddyContactNumberTextView;
    private RadioGroup buddyGenderRadioGroup;
    private ImageButton buddyImageButton;
    private SimpleCursorAdapter simpleCursorAdapter;
    private String name;
    private String image_uri;
    private Boolean dataChanged = false;

    private Button userOperationButton;

    private long user_id = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!UIToolsUtil.isScreenLarge(getApplicationContext())) {
            if (getApplicationContext().getResources().getConfiguration().orientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }

        setContentView(R.layout.activity_friend_operation);
        // getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, convertDPToPx(this.getApplicationContext(), 500));
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        buddyEmailTextView = (TextView) findViewById(R.id.buddyEmail);
        buddyContactNumberTextView = (TextView) findViewById(R.id.buddyContactNumber);
        buddyGenderRadioGroup = (RadioGroup) findViewById(R.id.buddyGender);
        buddyImageButton = (ImageButton) findViewById(R.id.buddy_profile_photo);
        userOperationButton = (Button) findViewById(R.id.user_operation_button);
        buddyGenderRadioGroup.check(R.id.radio_male);
        setupSearchView();
        Intent intent = getIntent();
        if (intent != null) {
            user_id = intent.getLongExtra("USER_ID", -1);
            if (user_id != -1) {
                userOperationButton.setText(getResources().getString(R.string.buddy_operation_update_user));
                name = intent.getStringExtra("NAME");
                searchableBuddyName.setText(name);
                buddyEmailTextView.setText(intent.getStringExtra("EMAIL"));
                buddyContactNumberTextView.setText(intent.getStringExtra("PHONE_NUM"));
                String gender = intent.getStringExtra("GENDER");
                switch (Gender.valueOf(gender)) {
                    case FEMALE:
                        buddyGenderRadioGroup.check(R.id.radio_female);
                        changeProfilePicture(findViewById(R.id.radio_female));
                        break;
                    case MALE:
                        buddyGenderRadioGroup.check(R.id.radio_male);
                        changeProfilePicture(findViewById(R.id.radio_male));
                        break;
                }
                image_uri = intent.getStringExtra("IMG_LOC");
                if (image_uri != null && !image_uri.equalsIgnoreCase("")) {
                    buddyImageButton.setImageURI(Uri.parse(image_uri));
                }
            }
        }
        requestReadContactPermissions();
    }

    private Boolean validateUserFormData() {
        if (searchableBuddyName.getText().toString() == null || searchableBuddyName.getText().toString().equals("")) {
            return false;
        }
        return true;
    }

    public void addUser(View view) {
        if (!validateUserFormData()) {
            Snackbar.make(findViewById(R.id.friend_operation_form), "Enter User Name !!!", Snackbar.LENGTH_SHORT).show();
            return;
        }
        String message = null;
        ContentValues values = new ContentValues();
        values.put(UserEntry.COLUMN_USER_NAME, searchableBuddyName.getText().toString());
        values.put(UserEntry.COLUMN_USER_EMAIL, buddyEmailTextView.getText().toString());
        values.put(UserEntry.COLUMN_USER_NUMBER, buddyContactNumberTextView.getText().toString());
        values.put(UserEntry.COLUMN_USER_IMAGE_LOC, image_uri);

        switch (buddyGenderRadioGroup.getCheckedRadioButtonId()) {
            case R.id.radio_male:
                values.put(UserEntry.COLUMN_USER_GENDER, Gender.MALE.name());
                break;
            case R.id.radio_female:
                values.put(UserEntry.COLUMN_USER_GENDER, Gender.FEMALE.name());
                break;
            default:
                values.put(UserEntry.COLUMN_USER_GENDER, Gender.MALE.name());
        }
        if (user_id == -1) {
            values.put(UserEntry.COLUMN_USER_CREATION_DATE, DateUtil.getCurrentFormattedDateTime());
            getContentResolver().insert(UserEntry.buildUserUri(), values);
            message = getResources().getString(R.string.buddy_operation_added) + " " + searchableBuddyName.getText().toString();
        } else {
            getContentResolver().update(UserEntry.buildUserUri(), values, UserEntry.COLUMN_USER_ID + " = " + user_id, null);
            message = getResources().getString(R.string.buddy_operation_updated) + " " + searchableBuddyName.getText().toString();
        }
        dataChanged = true;
        Snackbar.make(findViewById(R.id.friend_operation_form), message, Snackbar.LENGTH_SHORT).show();
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

    public void changeProfilePicture(View view) {
        if (image_uri == null || image_uri.equalsIgnoreCase("")) {
            switch (view.getId()) {
                case R.id.radio_male:
                    buddyImageButton.setImageResource(R.drawable.male_profile_big);
                    break;
                case R.id.radio_female:
                    buddyImageButton.setImageResource(R.drawable.female_profile_big);
                    break;
                default:
                    buddyImageButton.setImageResource(R.drawable.male_profile_big);
            }
        }
        //RotateAnimation ranim = (RotateAnimation) AnimationUtils.loadAnimation(this.getApplicationContext(), R.anim.rotation);
        //buddyImageButton.setAnimation(ranim);
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


    /**
     * Implementation for edit text that works as search view for searching contacts
     */
    private void setupSearchView() {
        searchableBuddyName = (AutoCompleteTextView) findViewById(R.id.search_view);
        searchableBuddyName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (simpleCursorAdapter != null) {
                    simpleCursorAdapter.getFilter().filter(charSequence);
                    searchableBuddyName.setAdapter(simpleCursorAdapter);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        searchableBuddyName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                name = ((TextView) view.findViewById(R.id.contact_name)).getText().toString();
                image_uri = null;
                searchableBuddyName.setText("");
                getContactImageAndNumbers();

                searchableBuddyName.setText(name);
                if (image_uri != null)
                    buddyImageButton.setImageURI(Uri.parse(image_uri));
            }
        });
        String from[] = {ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.PHOTO_URI,
                ContactsContract.Contacts._ID, ContactsContract.Contacts.HAS_PHONE_NUMBER};
        int to[] = {R.id.contact_name, R.id.contact_image};
        simpleCursorAdapter = new SimpleCursorAdapter(this, R.layout.search_contacts_list_item, null, from, to, SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        simpleCursorAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence constraint) {
                String selection = ContactsContract.Contacts.DISPLAY_NAME + " LIKE '%" + constraint + "%'";
                return getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, selection, null, null);
            }
        });
    }

    private void getContactImageAndNumbers() {
        Cursor phoneCursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null,
                "DISPLAY_NAME = '" + name + "'", null, null);
        if (phoneCursor.moveToFirst()) {
            name = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            image_uri = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));
            buddyEmailTextView.setText(getEmail(phoneCursor));
            buddyContactNumberTextView.setText(getPhone(phoneCursor));
        }
        phoneCursor.close();
    }

    private String getPhone(Cursor cursor) {
        int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)));
        if (hasPhoneNumber > 0) {
            Cursor phoneCursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                    new String[]{cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))}, null);
            phoneCursor.moveToFirst();
            String number = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            phoneCursor.close();
            return number;
        }
        return null;
    }

    private String getEmail(Cursor cursor) {
        String email = null;
        Cursor emailCursor = getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                new String[]{cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))}, null);
        if (emailCursor != null) {
            if (emailCursor.moveToFirst())
                email = emailCursor.getString(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
            emailCursor.close();
        }
        return email;
    }

    @Override
    public void finish() {
        Intent data = new Intent();
        if (dataChanged) {
            data.putExtra("USER_OPEARATION", true);
            if (user_id != -1) {
                data.putExtra("USER_NAME", searchableBuddyName.getText().toString());
                data.putExtra("IMG_LOC", image_uri);
                data.putExtra("EMAIL", buddyEmailTextView.getText().toString());
                data.putExtra("NUMBER", buddyContactNumberTextView.getText().toString());
                switch (buddyGenderRadioGroup.getCheckedRadioButtonId()) {
                    case R.id.radio_male:
                        data.putExtra("GENDER", Gender.MALE.name());
                        break;
                    case R.id.radio_female:
                        data.putExtra("GENDER", Gender.FEMALE.name());
                        break;
                }
            }
            setResult(RESULT_OK, data);
        }
        super.finish();
        overridePendingTransition(0, R.anim.add_activity_exit_animation);
    }

    /**
     * Requests read contacts permission from user. Only for android v23 and up
     */
    public void requestReadContactPermissions() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_CONTACTS)) {
                android.support.v7.app.AlertDialog.Builder dialogBuilder = new android.support.v7.app.AlertDialog.Builder(this);
                dialogBuilder.setMessage(getString(R.string.request_permissions_contacts))
                        .setPositiveButton(getString(R.string.dlg_ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                ActivityCompat.requestPermissions(UserOperationsActivity.this, new String[]{android.Manifest.permission.READ_CONTACTS}, 2);
                            }
                        });
                dialogBuilder.show();
            } else
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_CONTACTS}, 2);
        }
    }
}