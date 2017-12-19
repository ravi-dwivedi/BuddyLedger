package com.ravi.android.buddy.ledger.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by ravi on 7/1/17.
 */

public class GroupEntry {

    public static final Uri CONTENT_URI =
            DatabaseContract.BASE_CONTENT_URI.buildUpon().appendPath(DatabaseContract.PATH_USER_GROUP).build();

    public static final String CONTENT_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + DatabaseContract.CONTENT_AUTHORITY + "/" + DatabaseContract.PATH_USER_GROUP;

    public static final String CONTENT_ITEM_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + DatabaseContract.CONTENT_AUTHORITY + "/" + DatabaseContract.PATH_USER_GROUP;

    public static final String TABLE_NAME = "user_group";

    public static final String COLUMN_GROUP_ID = "group_id";

    public static final String COLUMN_GROUP_TYPE = "group_type";

    public static final String COLUMN_GROUP_NAME = "group_name";

    public static final String COLUMN_GROUP_CREATION_DATE = "group_creation_date";

    public static final String COLUMN_GROUP_DESCRIPTION = "group_description";

    //public static final String COLUMN_GROUP_IMAGE_LOC = "group_image_loc";

    //public static final String COLUMN_CREATED_BY = "group_created_by";

    /**
     * Matches: /user_group/[_id]/
     */
    public static Uri buildGroupUriWithId(long id) {
        return ContentUris.withAppendedId(CONTENT_URI, id);
    }

    /**
     * Matches: /user_group/
     */
    public static Uri buildGroupUri() {
        return CONTENT_URI.buildUpon().build();
    }

    public static long getGroupIdFromUri(Uri uri) {
        return Long.parseLong(uri.getPathSegments().get(1));
    }

}
