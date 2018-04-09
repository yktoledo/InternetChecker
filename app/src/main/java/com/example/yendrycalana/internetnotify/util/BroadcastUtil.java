package com.example.yendrycalana.internetnotify.util;

import android.content.Context;
import android.content.Intent;

import com.example.yendrycalana.internetnotify.services.BroadcastWidget;

import static com.example.yendrycalana.internetnotify.util.Constants.ACTION_BROADCAST_WIDGET;
import static com.example.yendrycalana.internetnotify.util.Constants.WIDGET_NOTIFICATION;

/**
 * Created by yendry.calana on 3/28/18.
 */

public class BroadcastUtil {
   public static void sendWidgetBroadcast(Context context, String extra){
       Intent intent = new Intent(context, BroadcastWidget.class);
       intent.putExtra(WIDGET_NOTIFICATION, extra);
       intent.setAction(ACTION_BROADCAST_WIDGET);
       context.sendBroadcast(intent);
   }
}
