package com.ravi.android.buddy.ledger.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import com.ravi.android.buddy.ledger.HomeActivity;
import com.ravi.android.buddy.ledger.R;
import com.ravi.android.buddy.ledger.TransactionOperationsActivity;
import com.ravi.android.buddy.ledger.UserProfileActivity;

/**
 * Created by ravi on 5/3/17.
 */

public class TransactionSummaryWidgetProvider extends AppWidgetProvider {

    public static final String ACTION_UPDATE_WIDGET = "com.ravi.android.buddy.ledger.widget.updateWidget";

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction().equals(ACTION_UPDATE_WIDGET)) {
            ComponentName widget = new ComponentName(context.getPackageName(), TransactionSummaryWidgetProvider.class.getName());
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetManager.getAppWidgetIds(widget), R.id.widget_transactions_list);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.i("Widget", " Loading");
        for (int appWidgetID : appWidgetIds) {

            RemoteViews widget = new RemoteViews(context.getPackageName(), R.layout.widget_transaction_summary);
            /* Set the click on the widget header to open the main activity */
            Intent mainActivity = new Intent(context, HomeActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetID, mainActivity, 0);
            widget.setOnClickPendingIntent(R.id.widget_transactions_heading, pendingIntent);

            Intent addTransaction = new Intent(context, TransactionOperationsActivity.class);
            addTransaction.putExtra("action", "generic");
            PendingIntent addIntent = PendingIntent.getActivity(context, appWidgetID, addTransaction, 0);
            widget.setOnClickPendingIntent(R.id.widget_add_transaction, addIntent);

            Intent remoteViewService = new Intent(context, TransactionSummaryWidgetRemoteViewService.class);
            widget.setRemoteAdapter(R.id.widget_transactions_list, remoteViewService);
            /* Set the intent template to be used by list items in the widget */
            Intent details = new Intent(context, UserProfileActivity.class);
            PendingIntent pIntent = PendingIntent.getActivity(context, 0, details, PendingIntent.FLAG_UPDATE_CURRENT);
            widget.setPendingIntentTemplate(R.id.widget_transactions_list, pIntent);

            appWidgetManager.updateAppWidget(appWidgetID, widget);
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetID, R.id.widget_transactions_list);
        }
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        RemoteViews widget = new RemoteViews(context.getPackageName(), R.layout.widget_transaction_summary);
        Intent remoteViewService = new Intent(context, TransactionSummaryWidgetRemoteViewService.class);
        widget.setRemoteAdapter(R.id.widget_transactions_list, remoteViewService);
        appWidgetManager.updateAppWidget(appWidgetId, widget);
    }

    public static void sendRefreshWidgetBroadcast(Context context) {
        Intent updateWidget = new Intent(context, TransactionSummaryWidgetProvider.class);
        updateWidget.setAction(TransactionSummaryWidgetProvider.ACTION_UPDATE_WIDGET);
        context.sendBroadcast(updateWidget);
    }
}
