package com.ravi.android.buddy.ledger;

import android.content.Context;

import com.ravi.android.buddy.ledger.model.User;

/**
 * Created by ravi on 7/1/17.
 */

public class ApplicationGlobals {

    private static ApplicationGlobals applicationGlobals;
    private static User loggedInUser;
    private Context context;

    public static final String storedDBPath = "BuddyLedgerData";

    public static final int superUserId = 1;

    public static final int HOME_ACTIVITY_REQUEST_CODE = 100;

    public static final int USER_PROFILE_ACTIVITY_REQUEST_CODE = 200;

    public static final int GROUP_INFO_ACTIVITY_REQUEST_CODE = 300;

    public static final int MY_LEDGER_ACTIVITY_REQUEST_CODE = 400;

    public static final int USER_OPERATION_ACTIVITY_REQUEST_CODE = 500;

    public static final int USER_OPERATION_ACTIVITY_FOR_SUPER_USER_REQUEST_CODE = 501;

    public static final int USER_TRANSACTION_OPERATION__ACTIVITY_REQUEST_CODE = 600;

    public static final int GROUP_OPERATION_ACTIVITY_REQUEST_CODE = 700;

    public static final int GROUP_TRANSACTION_OPERATION_ACTIVITY_REQUEST_CODE = 800;

    public static final int GROUP_MEMBER_OPERATION_ACTIVITY_REQUEST_CODE = 900;

    public static final int USER_LEDGER_OPERATION_ACTIVITY_REQUEST_CODE = 1000;

    // Firebase instance variables
    /*
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mMessagesDatabaseReference;
    private ChildEventListener mChildEventListener;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mChatPhotosStorageReference;
    */

    private ApplicationGlobals(Context context) {
        this.context = context;
    }

    public static ApplicationGlobals get(Context context) {
        if (applicationGlobals == null) {
            applicationGlobals = new ApplicationGlobals(context);
        }
        return applicationGlobals;
    }
}
