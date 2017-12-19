package com.ravi.android.buddy.ledger.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;

/**
 * Created by ravi on 7/1/17.
 */

public class UserEntry {

    public static final Uri CONTENT_URI =
            DatabaseContract.BASE_CONTENT_URI.buildUpon().appendPath(DatabaseContract.PATH_USER).build();

    public static final String CONTENT_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + DatabaseContract.CONTENT_AUTHORITY + "/" + DatabaseContract.PATH_USER;

    public static final String CONTENT_ITEM_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + DatabaseContract.CONTENT_AUTHORITY + "/" + DatabaseContract.PATH_USER;

    public static final String TABLE_NAME = "user";

    public static final String COLUMN_USER_ID = "user_id";

    public static final String COLUMN_USER_NAME = "user_name";

    public static final String COLUMN_USER_EMAIL = "user_email";

    public static final String COLUMN_USER_CREATION_DATE = "created";

    public static final String COLUMN_USER_IMAGE_LOC = "user_image_loc";

    public static final String COLUMN_USER_NUMBER = "user_number";

    public static final String COLUMN_USER_GENDER = "user_gender";

    /**
     * Matches: /user/[_id]/
     */
    public static Uri buildUserUriWithId(long id) {
        return ContentUris.withAppendedId(CONTENT_URI, id);
    }

    /**
     * Matches: /user/
     */
    public static Uri buildUserUri() {
        return CONTENT_URI.buildUpon().build();
    }
}
