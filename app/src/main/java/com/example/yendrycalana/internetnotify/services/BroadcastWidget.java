package com.example.yendrycalana.internetnotify.services;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.yendrycalana.internetnotify.R;
import com.example.yendrycalana.internetnotify.util.SwitchSubstring;

import org.greenrobot.eventbus.EventBus;

import static com.example.yendrycalana.internetnotify.util.Constants.ACTION_BROADCAST_WIDGET;
import static com.example.yendrycalana.internetnotify.util.Constants.CHANGE_WIFI_ICON_NOTIFICATION_OFF;
import static com.example.yendrycalana.internetnotify.util.Constants.CHANGE_WIFI_ICON_NOTIFICATION_ON;
import static com.example.yendrycalana.internetnotify.util.Constants.CHANGE_WIFI_ICON_SERVER_START;
import static com.example.yendrycalana.internetnotify.util.Constants.CHANGE_WIFI_ICON_SERVER_STOP;
import static com.example.yendrycalana.internetnotify.util.Constants.START_ON_TOUCH;
import static com.example.yendrycalana.internetnotify.util.Constants.WIDGET_NOTIFICATION;

/**
 * Implementation of App Widget functionality.
 */
public class BroadcastWidget extends AppWidgetProvider {
    private static int mCounter = 0;

    void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.broadcast_widget);
        // Construct an Intent which is pointing this class.
        Intent intent = new Intent(context, BroadcastWidget.class);
        intent.putExtra(WIDGET_NOTIFICATION, START_ON_TOUCH);
        intent.setAction(ACTION_BROADCAST_WIDGET);
        // And this time we are sending a broadcast with getBroadcast
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        views.setOnClickPendingIntent(R.id.widget_content, pendingIntent);
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String notification ="";
        if (intent.getExtras() != null) {
            notification = intent.getExtras().getString(WIDGET_NOTIFICATION, "");
        }

        if (ACTION_BROADCAST_WIDGET.equals(intent.getAction())) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.broadcast_widget);

            if (!TextUtils.isEmpty(notification)) {
                SwitchSubstring.of(notification)
                        .when(CHANGE_WIFI_ICON_NOTIFICATION_ON, () -> views.setImageViewResource(R.id.image_in_widget, R.drawable.wifi_on))
                        .when(CHANGE_WIFI_ICON_NOTIFICATION_OFF, () -> views.setImageViewResource(R.id.image_in_widget, R.drawable.wifi_off))
                        .when(CHANGE_WIFI_ICON_SERVER_START, () -> views.setInt(R.id.indicator_id, "setBackgroundResource", R.drawable.background_green))
                        .when(CHANGE_WIFI_ICON_SERVER_STOP, () -> {
                            views.setInt(R.id.indicator_id, "setBackgroundResource", R.drawable.background_red);
                            views.setImageViewResource(R.id.image_in_widget, R.drawable.stop_icon);
                        })
                        .when(START_ON_TOUCH, () -> startService(context));
            }


            // This time we dont have widgetId. Reaching our widget with that way.
            ComponentName appWidget = new ComponentName(context, BroadcastWidget.class);
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidget, views);

        }
    }

    private void startService(Context context) {
        if (!isMyServiceRunning(NetworkService.class, context)){
            context.startService(new Intent(context, NetworkService.class));
        }else {
            context.stopService(new Intent(context, NetworkService.class));
        }

    }

    private boolean isMyServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        assert manager != null;
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}

