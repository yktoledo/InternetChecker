package com.example.yendrycalana.internetnotify.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.yendrycalana.internetnotify.evenbus.CancelRing;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by yendry.calana on 3/27/18.
 */

public class MyIntentService extends IntentService {

    private NotificationManager notificationManager;
    public MyIntentService() {
        super("mi");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("myapp", "I got this awesome intent and will now do stuff in the background!");
        // .... do what you like

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert notificationManager != null;
        notificationManager.cancel(0);
        EventBus.getDefault().post(new CancelRing());
    }
}