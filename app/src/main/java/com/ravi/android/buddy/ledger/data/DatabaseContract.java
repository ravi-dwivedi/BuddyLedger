package com.ravi.android.buddy.ledger.data;

import android.net.Uri;

/**
 * Created by ravi on 7/1/17.
 */

public class DatabaseContract {

    public static final String CONTENT_AUTHORITY = "com.ravi.android.buddy.ledger";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_USER = "user";

    public static final String PATH_USER_TRANSACTION = "user_transaction";

    public static final String PATH_USER_GROUP = "user_group";

    public static final String PATH_GROUP_MEMBER = "group_member";

    public static final String PATH_USER_LEDGER = "user_ledger";

    public static final String PATH_GROUP_TRANSACTION = "group_transaction";

}
