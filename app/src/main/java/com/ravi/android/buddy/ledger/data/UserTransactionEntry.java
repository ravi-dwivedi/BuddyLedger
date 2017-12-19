package com.ravi.android.buddy.ledger.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;

/**
 * Created by ravi on 8/1/17.
 */

public class UserTransactionEntry {

    public static final Uri CONTENT_URI =
            DatabaseContract.BASE_CONTENT_URI.buildUpon().appendPath(DatabaseContract.PATH_USER_TRANSACTION).build();

    public static final String CONTENT_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + DatabaseContract.CONTENT_AUTHORITY + "/" + DatabaseContract.PATH_USER_TRANSACTION;

    public static final String CONTENT_ITEM_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + DatabaseContract.CONTENT_AUTHORITY + "/" + DatabaseContract.PATH_USER_TRANSACTION;

    public static final String TABLE_NAME = "user_transaction";

    public static final String COLUMN_TRANSACTION_ID = "transaction_id";

    public static final String COLUMN_USER_ID = "user_id";

    public static final String COLUMN_TRANSACTION_DATE = "transaction_date";

    public static final String COLUMN_TRANSACTION_TYPE = "transaction_type";

    public static final String COLUMN_TRANSACTION_AMOUNT = "transaction_amount";

    public static final String COLUMN_TRANSATION_DESCRIPTION = "transaction_description";

    /**
     * Matches: /user/[_id]/
     */
    public static Uri buildTransactionUriWithId(long id) {
        return ContentUris.withAppendedId(CONTENT_URI, id);
    }

    /**
     * Matches: /user_transaction/
     */
    public static Uri buildTransactionUri() {
        return CONTENT_URI.buildUpon().build();
    }
}
