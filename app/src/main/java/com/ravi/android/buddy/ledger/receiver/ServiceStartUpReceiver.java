package com.ravi.android.buddy.ledger.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ravi.android.buddy.ledger.service.OfflineNotificationService;

/**
 * Created by ravi on 5/3/17.
 */

public class ServiceStartUpReceiver extends BroadcastReceiver {

    public static final int REQUEST_CODE = 235632;

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent createNotification = new Intent(context, OfflineNotificationService.class);
        context.startService(createNotification);
    }
}
