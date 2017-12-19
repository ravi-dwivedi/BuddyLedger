package com.ravi.android.buddy.ledger.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;

/**
 * Created by ravi on 7/1/17.
 */

public class GroupMemberEntry {

    public static final Uri CONTENT_URI =
            DatabaseContract.BASE_CONTENT_URI.buildUpon().appendPath(DatabaseContract.PATH_GROUP_MEMBER).build();

    public static final String CONTENT_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + DatabaseContract.CONTENT_AUTHORITY + "/" + DatabaseContract.PATH_GROUP_MEMBER;

    public static final String CONTENT_ITEM_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + DatabaseContract.CONTENT_AUTHORITY + "/" + DatabaseContract.PATH_GROUP_MEMBER;

    public static final String TABLE_NAME = "group_member_mapping";

    public static final String COLUMN_USER_ID = "user_id";

    public static final String COLUMN_GROUP_ID = "group_id";

    public static final String COLUMN_GROUP_OPERATION = "operation";

    public static final String COLUMN_ADDED_DATE = "added_date";


    /**
     * Matches: /group_member/[_id]/
     */
    public static Uri buildGroupMemberWithId(long id) {
        return ContentUris.withAppendedId(CONTENT_URI, id);
    }

    /**
     * Matches: /group_member/
     */
    public static Uri buildGroupMemberUri() {
        return CONTENT_URI.buildUpon().build();
    }

}
