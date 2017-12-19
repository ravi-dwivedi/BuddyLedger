package com.ravi.android.buddy.ledger.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ravi.android.buddy.ledger.model.Gender;
import com.ravi.android.buddy.ledger.utility.DateUtil;
import com.ravi.android.buddy.ledger.utility.PrefManager;

/**
 * Created by ravi on 7/1/17.
 */

public class DatabaseCreator extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    public static final String DATABASE_NAME = "buddyLedger.db";

    public DatabaseCreator(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private String getUserCreateTableQuery() {
        return "CREATE TABLE " + UserEntry.TABLE_NAME + " ("
                + UserEntry.COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + UserEntry.COLUMN_USER_NAME + " TEXT NOT NULL, "
                + UserEntry.COLUMN_USER_EMAIL + " TEXT, "
                + UserEntry.COLUMN_USER_CREATION_DATE + " DATE NOT NULL, "
                + UserEntry.COLUMN_USER_NUMBER + " TEXT, "
                + UserEntry.COLUMN_USER_IMAGE_LOC + " TEXT, "
                + UserEntry.COLUMN_USER_GENDER + " TEXT "
                + " );";
    }

    private String getGroupCreateTableQuery() {
        return "CREATE TABLE " + GroupEntry.TABLE_NAME + " ("
                + GroupEntry.COLUMN_GROUP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + GroupEntry.COLUMN_GROUP_TYPE + " TEXT NOT NULL, "
                + GroupEntry.COLUMN_GROUP_NAME + " TEXT NOT NULL, "
                + GroupEntry.COLUMN_GROUP_CREATION_DATE + " DATE NOT NULL, "
                + GroupEntry.COLUMN_GROUP_DESCRIPTION + " TEXT "
                + " );";
    }

    private String getTransactionCreateTableQuery() {
        return "CREATE TABLE " + UserTransactionEntry.TABLE_NAME + " ("
                + UserTransactionEntry.COLUMN_TRANSACTION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + UserTransactionEntry.COLUMN_USER_ID + " INTEGER NOT NULL, "
                + UserTransactionEntry.COLUMN_TRANSACTION_DATE + " DATE NOT NULL, "
                + UserTransactionEntry.COLUMN_TRANSACTION_TYPE + " TEXT NOT NULL, "
                + UserTransactionEntry.COLUMN_TRANSACTION_AMOUNT + " REAL NOT NULL, "
                + UserTransactionEntry.COLUMN_TRANSATION_DESCRIPTION + " TEXT, "
                + " FOREIGN KEY (" + UserTransactionEntry.COLUMN_USER_ID + ") REFERENCES " +
                UserEntry.TABLE_NAME + " (" + UserEntry.COLUMN_USER_ID + ") "
                + " );";
    }

    private String getGroupTransactionCreateTableQuery() {
        return "CREATE TABLE " + GroupTransactionEntry.TABLE_NAME + " ("
                + GroupTransactionEntry.COLUMN_TRANSACTION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + GroupTransactionEntry.COLUMN_USER_ID + " INTEGER NOT NULL, "
                + GroupTransactionEntry.COLUMN_TRANSACTION_DATE + " DATE NOT NULL, "
                + GroupTransactionEntry.COLUMN_TRANSACTION_AMOUNT + " REAL NOT NULL, "
                + GroupTransactionEntry.COLUMN_TRANSATION_DESCRIPTION + " TEXT, "
                + GroupTransactionEntry.COLUMN_GROUP_ID + " INTEGER NOT NULL, "
                + " FOREIGN KEY (" + GroupTransactionEntry.COLUMN_USER_ID + ") REFERENCES " +
                UserEntry.TABLE_NAME + " (" + UserEntry.COLUMN_USER_ID + "), "
                + " FOREIGN KEY (" + GroupTransactionEntry.COLUMN_GROUP_ID + ") REFERENCES " +
                GroupEntry.TABLE_NAME + " (" + GroupEntry.COLUMN_GROUP_ID + ") "
                + " );";
    }

    private String getGroupMemberCreateTableQuery() {
        return "CREATE TABLE " + GroupMemberEntry.TABLE_NAME + " ("
                + GroupMemberEntry.COLUMN_USER_ID + " INTEGER NOT NULL, "
                + GroupMemberEntry.COLUMN_GROUP_ID + " INTEGER NOT NULL, "
                + GroupMemberEntry.COLUMN_ADDED_DATE + " INTEGER NOT NULL, "
                + GroupMemberEntry.COLUMN_GROUP_OPERATION + " TEXT NOT NULL, "
                + " FOREIGN KEY (" + GroupMemberEntry.COLUMN_USER_ID + ") REFERENCES " +
                UserEntry.TABLE_NAME + " (" + UserEntry.COLUMN_USER_ID + "), "
                + " FOREIGN KEY (" + GroupMemberEntry.COLUMN_GROUP_ID + ") REFERENCES " +
                GroupEntry.TABLE_NAME + " (" + GroupEntry.COLUMN_GROUP_ID + ") "
                //+ " UNIQUE (" + GroupMemberEntry.COLUMN_USER_ID + ", " +
                //GroupMemberEntry.COLUMN_GROUP_ID + ") ON CONFLICT REPLACE "
                + " );";
    }

    private String getUserLedgerTableQuery() {
        return "CREATE TABLE " + UserLedgerEntry.TABLE_NAME + " ("
                + UserLedgerEntry.COLUMN_LEDGER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + UserLedgerEntry.COLUMN_TYPE + " TEXT, "
                + UserLedgerEntry.COLUMN_DETAILS + " TEXT, "
                + UserLedgerEntry.COLUMN_AMOUNT + " REAL, "
                + UserLedgerEntry.COLUMN_DATE + " DATE NOT NULL) ";
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_USER_TABLE = getUserCreateTableQuery();

        final String SQL_CREATE_GROUP_TABLE = getGroupCreateTableQuery();

        final String SQL_CREATE_TRANSACTION_TABLE = getTransactionCreateTableQuery();

        final String SQL_CREATE_GROUP_MEMBER_TABLE = getGroupMemberCreateTableQuery();

        final String SQL_CREATE_USER_LEDGER = getUserLedgerTableQuery();

        final String SQL_CREATE_GROUP_TRANSACTION = getGroupTransactionCreateTableQuery();

        sqLiteDatabase.execSQL(SQL_CREATE_USER_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_GROUP_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_TRANSACTION_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_GROUP_MEMBER_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_USER_LEDGER);
        sqLiteDatabase.execSQL(SQL_CREATE_GROUP_TRANSACTION);
        addSuperUser(sqLiteDatabase);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + UserEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + GroupEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + UserTransactionEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + GroupMemberEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + UserLedgerEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + GroupTransactionEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    private void addSuperUser(SQLiteDatabase sqLiteDatabase) {
        ContentValues values = new ContentValues();
        values.put(UserEntry.COLUMN_USER_NAME, "MySelf");
        values.put(UserEntry.COLUMN_USER_GENDER, Gender.MALE.name());
        values.put(UserEntry.COLUMN_USER_CREATION_DATE, DateUtil.getCurrentFormattedDateTime());
        sqLiteDatabase.insert(UserEntry.TABLE_NAME, null, values);
    }

}
