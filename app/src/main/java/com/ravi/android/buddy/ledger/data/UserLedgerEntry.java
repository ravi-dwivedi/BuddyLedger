package com.ravi.android.buddy.ledger.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;

/**
 * Created by ravi on 12/2/17.
 */

public class UserLedgerEntry {

    public static final Uri CONTENT_URI =
            DatabaseContract.BASE_CONTENT_URI.buildUpon().appendPath(DatabaseContract.PATH_USER_LEDGER).build();

    public static final String CONTENT_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + DatabaseContract.CONTENT_AUTHORITY + "/" + DatabaseContract.PATH_USER;

    public static final String CONTENT_ITEM_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + DatabaseContract.CONTENT_AUTHORITY + "/" + DatabaseContract.PATH_USER;

    public static final String TABLE_NAME = "user_ledger";
    public static final String COLUMN_LEDGER_ID = "ledger_id";
    public static final String COLUMN_TYPE = "Type";
    public static final String COLUMN_DATE = "Date";
    public static final String COLUMN_DETAILS = "Details";
    public static final String COLUMN_AMOUNT = "Amount";

    /**
     * Matches: /userledge/[_id]/
     */
    public static Uri buildUserLedgerUriWithId(long id) {
        return ContentUris.withAppendedId(CONTENT_URI, id);
    }

    /**
     * Matches: /userledge/
     */
    public static Uri buildUserLedgerUri() {
        return CONTENT_URI.buildUpon().build();
    }

    /**
     *
     */
    public static long getIdFromUri(Uri uri) {
        return ContentUris.parseId(uri);
    }
}
