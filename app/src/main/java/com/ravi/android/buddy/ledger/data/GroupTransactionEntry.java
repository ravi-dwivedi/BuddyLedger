package com.ravi.android.buddy.ledger.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;

/**
 * Created by ravi on 27/2/17.
 */

public class GroupTransactionEntry {

    public static final Uri CONTENT_URI =
            DatabaseContract.BASE_CONTENT_URI.buildUpon().appendPath(DatabaseContract.PATH_GROUP_TRANSACTION).build();

    public static final String CONTENT_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + DatabaseContract.CONTENT_AUTHORITY + "/" + DatabaseContract.PATH_GROUP_TRANSACTION;

    public static final String CONTENT_ITEM_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + DatabaseContract.CONTENT_AUTHORITY + "/" + DatabaseContract.PATH_GROUP_TRANSACTION;

    public static final String TABLE_NAME = "group_transaction";

    public static final String COLUMN_TRANSACTION_ID = "transaction_id";

    public static final String COLUMN_USER_ID = "user_id";

    public static final String COLUMN_GROUP_ID = "group_id";

    public static final String COLUMN_TRANSACTION_AMOUNT = "transaction_amount";

    public static final String COLUMN_TRANSATION_DESCRIPTION = "transaction_description";

    public static final String COLUMN_TRANSACTION_DATE = "transaction_date";

    /**
     * Matches: /user/[_id]/
     */
    public static Uri buildGroupTransactionUriWithId(long id) {
        return ContentUris.withAppendedId(CONTENT_URI, id);
    }

    /**
     * Matches: /user_transaction/
     */
    public static Uri buildGroupTransactionUri() {
        return CONTENT_URI.buildUpon().build();
    }
}
