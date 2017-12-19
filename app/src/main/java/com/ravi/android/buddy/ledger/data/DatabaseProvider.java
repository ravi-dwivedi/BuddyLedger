package com.ravi.android.buddy.ledger.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Created by ravi on 8/1/17.
 */

public class DatabaseProvider extends ContentProvider {

    private DatabaseCreator dbCreator;
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private final static int USERS = 100;
    private final static int USER_WITH_ID = 101;
    private final static int GROUPS = 200;
    private final static int GROUP_WITH_ID = 201;
    private final static int TRANSACTIONS = 300;
    private final static int TRANSACTION_WITH_ID = 301;
    private final static int TANSACTIONS_WITH_USER_ID = 302;
    private final static int GROUP_MEMBER_MAPPINGS = 400;
    private final static int USERS_BY_GROUP_ID = 501;
    private final static int GROUPS_BY_USER_ID = 502;
    private final static int USER_LEDGER = 600;
    private final static int USER_LEDGER_BY_ID = 601;
    private final static int GROUP_TRANSACTION = 700;
    private final static int GROUP_TRANSACTION_BY_ID = 701;

    /*
    private static final SQLiteQueryBuilder sUsersByGroupId;
    private static final SQLiteQueryBuilder sGroupsByUserId;

    static {
        sUsersByGroupId = new SQLiteQueryBuilder();

        sUsersByGroupId.setTables(
                UserEntry.TABLE_NAME + " INNER JOIN " +
                        GroupMemberEntry.TABLE_NAME +
                        " ON " + GroupMemberEntry.TABLE_NAME +
                        "." + GroupMemberEntry.COLUMN_USER_ID +
                        " = " + UserEntry.TABLE_NAME +
                        "." + UserEntry.COLUMN_USER_ID);

        sGroupsByUserId = new SQLiteQueryBuilder();

        sGroupsByUserId.setTables(
                GroupEntry.TABLE_NAME + " INNER JOIN " +
                        GroupMemberEntry.TABLE_NAME +
                        " ON " + GroupMemberEntry.TABLE_NAME +
                        "." + GroupMemberEntry.COLUMN_GROUP_ID +
                        " = " + GroupEntry.TABLE_NAME +
                        "." + GroupEntry.COLUMN_GROUP_ID);
    }
    */

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = DatabaseContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, DatabaseContract.PATH_USER, USERS);
        matcher.addURI(authority, DatabaseContract.PATH_USER + "/#", USER_WITH_ID);
        matcher.addURI(authority, DatabaseContract.PATH_USER_GROUP, GROUPS);
        matcher.addURI(authority, DatabaseContract.PATH_USER_GROUP + "/#", GROUP_WITH_ID);
        matcher.addURI(authority, DatabaseContract.PATH_USER_TRANSACTION, TRANSACTIONS);
        matcher.addURI(authority, DatabaseContract.PATH_USER_TRANSACTION + "/#", TRANSACTION_WITH_ID);
        matcher.addURI(authority, DatabaseContract.PATH_USER_TRANSACTION + "/" + UserTransactionEntry.COLUMN_USER_ID + "/#", TANSACTIONS_WITH_USER_ID);
        matcher.addURI(authority, DatabaseContract.PATH_GROUP_MEMBER, GROUP_MEMBER_MAPPINGS);
        matcher.addURI(authority, DatabaseContract.PATH_USER + "/" + GroupMemberEntry.COLUMN_USER_ID + "/#", USERS_BY_GROUP_ID);
        matcher.addURI(authority, DatabaseContract.PATH_USER_GROUP + "/" + GroupMemberEntry.COLUMN_GROUP_ID + "/#", GROUPS_BY_USER_ID);
        matcher.addURI(authority, DatabaseContract.PATH_USER_LEDGER, USER_LEDGER);
        matcher.addURI(authority, DatabaseContract.PATH_USER_LEDGER + "/#", USER_LEDGER_BY_ID);
        matcher.addURI(authority, DatabaseContract.PATH_GROUP_TRANSACTION, GROUP_TRANSACTION);
        matcher.addURI(authority, DatabaseContract.PATH_GROUP_TRANSACTION + "/#", GROUP_TRANSACTION_BY_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        dbCreator = new DatabaseCreator(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor = null;
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case USERS:
                retCursor = dbCreator.getReadableDatabase().query(
                        UserEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case USER_WITH_ID:
                retCursor = dbCreator.getReadableDatabase().query(
                        UserEntry.TABLE_NAME,
                        projection,
                        UserEntry.COLUMN_USER_ID + " = ?",
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case GROUPS:
                retCursor = dbCreator.getReadableDatabase().query(
                        GroupEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case GROUP_WITH_ID:
                retCursor = dbCreator.getReadableDatabase().query(
                        GroupEntry.TABLE_NAME,
                        projection,
                        GroupEntry.COLUMN_GROUP_ID + " = ?",
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case TRANSACTIONS:
                retCursor = dbCreator.getReadableDatabase().query(
                        UserTransactionEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case TRANSACTION_WITH_ID:
                retCursor = dbCreator.getReadableDatabase().query(
                        UserTransactionEntry.TABLE_NAME,
                        projection,
                        UserTransactionEntry.COLUMN_TRANSACTION_ID + " = ?",
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case TANSACTIONS_WITH_USER_ID:
                retCursor = dbCreator.getReadableDatabase().query(
                        UserTransactionEntry.TABLE_NAME,
                        projection,
                        UserTransactionEntry.COLUMN_USER_ID + " = ?",
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case GROUP_MEMBER_MAPPINGS:
                retCursor = dbCreator.getReadableDatabase().query(
                        GroupMemberEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case USER_LEDGER:
                retCursor = dbCreator.getReadableDatabase().query(
                        UserLedgerEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case USER_LEDGER_BY_ID:
                retCursor = dbCreator.getReadableDatabase().query(
                        UserLedgerEntry.TABLE_NAME,
                        projection,
                        UserLedgerEntry.COLUMN_LEDGER_ID + " = ?",
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case GROUP_TRANSACTION:
                retCursor = dbCreator.getReadableDatabase().query(
                        GroupTransactionEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case GROUP_TRANSACTION_BY_ID:
                retCursor = dbCreator.getReadableDatabase().query(
                        GroupTransactionEntry.TABLE_NAME,
                        projection,
                        GroupTransactionEntry.COLUMN_TRANSACTION_ID + " = ?",
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
        }
        return retCursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case USERS:
                return UserEntry.CONTENT_TYPE;
            case USER_WITH_ID:
                return UserEntry.CONTENT_ITEM_TYPE;
            case GROUPS:
                return GroupEntry.CONTENT_TYPE;
            case GROUP_WITH_ID:
                return GroupEntry.CONTENT_ITEM_TYPE;
            case TRANSACTIONS:
                return UserTransactionEntry.CONTENT_TYPE;
            case TRANSACTION_WITH_ID:
                return UserTransactionEntry.CONTENT_ITEM_TYPE;
            case TANSACTIONS_WITH_USER_ID:
                return UserTransactionEntry.CONTENT_TYPE;
            case GROUP_MEMBER_MAPPINGS:
                return GroupMemberEntry.CONTENT_TYPE;
            case USERS_BY_GROUP_ID:
                return UserEntry.CONTENT_TYPE;
            case GROUPS_BY_USER_ID:
                return GroupEntry.CONTENT_TYPE;
            case USER_LEDGER:
                return UserLedgerEntry.CONTENT_TYPE;
            case USER_LEDGER_BY_ID:
                return UserLedgerEntry.CONTENT_ITEM_TYPE;
            case GROUP_TRANSACTION:
                return GroupTransactionEntry.CONTENT_TYPE;
            case GROUP_TRANSACTION_BY_ID:
                return GroupTransactionEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = dbCreator.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;
        switch (match) {
            case USERS:
                long user_id = db.insert(UserEntry.TABLE_NAME, null, values);
                if (user_id > 0)
                    returnUri = UserEntry.buildUserUriWithId(user_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            case GROUPS:
                long group_id = db.insert(GroupEntry.TABLE_NAME, null, values);
                if (group_id > 0)
                    returnUri = GroupEntry.buildGroupUriWithId(group_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            case TRANSACTIONS:
                long transaction_id = db.insert(UserTransactionEntry.TABLE_NAME, null, values);
                if (transaction_id > 0)
                    returnUri = UserTransactionEntry.buildTransactionUriWithId(transaction_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            case GROUP_MEMBER_MAPPINGS:
                db.insert(GroupMemberEntry.TABLE_NAME, null, values);
                returnUri = uri;
                break;
            case USER_LEDGER:
                long user_ledger_id = db.insert(UserLedgerEntry.TABLE_NAME, null, values);
                if (user_ledger_id > 0)
                    returnUri = UserLedgerEntry.buildUserLedgerUriWithId(user_ledger_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            case GROUP_TRANSACTION:
                long group_transaction_id = db.insert(GroupTransactionEntry.TABLE_NAME, null, values);
                if (group_transaction_id > 0)
                    returnUri = GroupTransactionEntry.buildGroupTransactionUriWithId(group_transaction_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = dbCreator.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        if (null == selection) selection = "1";
        switch (match) {
            case USERS:
                rowsDeleted = db.delete(UserTransactionEntry.TABLE_NAME, selection, selectionArgs);
                rowsDeleted += db.delete(GroupMemberEntry.TABLE_NAME, selection, selectionArgs);
                rowsDeleted += db.delete(UserEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case GROUPS:
                rowsDeleted = db.delete(UserTransactionEntry.TABLE_NAME, selection, selectionArgs);
                rowsDeleted += db.delete(GroupMemberEntry.TABLE_NAME, selection, selectionArgs);
                rowsDeleted += db.delete(GroupEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case TRANSACTIONS:
                rowsDeleted = db.delete(UserTransactionEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case USER_LEDGER:
                rowsDeleted = db.delete(UserLedgerEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case GROUP_TRANSACTION:
                rowsDeleted = db.delete(GroupTransactionEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = dbCreator.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;
        switch (match) {
            case USERS:
                rowsUpdated = db.update(UserEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case GROUPS:
                rowsUpdated = db.update(GroupEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case TRANSACTIONS:
                rowsUpdated = db.update(UserTransactionEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case USER_LEDGER:
                rowsUpdated = db.update(UserLedgerEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case GROUP_TRANSACTION:
                rowsUpdated = db.update(GroupTransactionEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}
