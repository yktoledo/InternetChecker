package com.example.yendrycalana.internetnotify.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.yendrycalana.internetnotify.R;
import com.example.yendrycalana.internetnotify.evenbus.CancelRing;
import com.example.yendrycalana.internetnotify.evenbus.ServiceMessage;
import com.example.yendrycalana.internetnotify.util.BroadcastUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

import static com.example.yendrycalana.internetnotify.util.Constants.CHANGE_WIFI_ICON_NOTIFICATION_OFF;
import static com.example.yendrycalana.internetnotify.util.Constants.CHANGE_WIFI_ICON_NOTIFICATION_ON;
import static com.example.yendrycalana.internetnotify.util.Constants.CHANGE_WIFI_ICON_SERVER_START;
import static com.example.yendrycalana.internetnotify.util.Constants.CHANGE_WIFI_ICON_SERVER_STOP;
import static com.example.yendrycalana.internetnotify.util.Constants.DEFAULT_NOTIFICATION;
import static com.example.yendrycalana.internetnotify.util.Constants.FIRST_TIME;
import static com.example.yendrycalana.internetnotify.util.Constants.MUSIC_NOTIFICATION;
import static com.example.yendrycalana.internetnotify.util.Constants.PERIOD;
import static com.example.yendrycalana.internetnotify.util.Constants.SHPF;

/**
 * Created by yendry.calana on 3/23/18.
 */

public class NetworkService extends Service {

    private static final String TAG = "kaka";
    private Disposable interval;
    private int period = 5;
    SharedPreferences pref;
    private NotificationCompat.Builder notificationBuilder;
    private NotificationManager notificationManager;
    private MediaPlayer player;
    private boolean ring = false;
    private boolean default_notification = false;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        pref = getApplicationContext().getSharedPreferences(SHPF, Context.MODE_PRIVATE);
        period = pref.getInt(PERIOD, 5);
        default_notification = pref.getBoolean(DEFAULT_NOTIFICATION, false);
        ring = pref.getBoolean(MUSIC_NOTIFICATION, false);
        this.interval = getSubscribe();
        BroadcastUtil.sendWidgetBroadcast(this, CHANGE_WIFI_ICON_SERVER_START);
        return START_STICKY;
    }

    private void ringsTone() {
        player = MediaPlayer.create(this, R.raw.alan_walker_fade);
        player.start();
    }

    private void showNotification() {



        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationBuilder = new NotificationCompat.Builder(this);


        Intent yepIntent = new Intent(this, MyIntentService.class);
        yepIntent.setAction("test");
        yepIntent.putExtra("foo", true);
        yepIntent.putExtra("bar", "more info");
        PendingIntent yepPendingIntent = PendingIntent.getService(this, 0, yepIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        notificationBuilder.setContentIntent(yepPendingIntent);

        notificationBuilder.setSmallIcon(R.drawable.ic_wifi_black_24dp)
                .setAutoCancel(true)
                .setColor(getResources().getColor(R.color.notification_background))
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(getString(R.string.notification_content))
                .setPriority((Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) ? NotificationManager.IMPORTANCE_HIGH : NotificationCompat.PRIORITY_HIGH);
        if (default_notification) {
            notificationBuilder.setDefaults(Notification.DEFAULT_ALL);
        }


        notificationManager.notify(0, notificationBuilder.build());
    }


    @NonNull
    private Disposable getSubscribe() {
        return Observable.interval(period, TimeUnit.SECONDS)
                .subscribe(aLong -> {
                    Log.d(TAG, "getSubscribe: "+pref.getBoolean(FIRST_TIME, true));
                    Log.d(TAG, "getSubscribe: "+period);
                    if (isInternetAvailable()) {
                        BroadcastUtil.sendWidgetBroadcast(this, CHANGE_WIFI_ICON_NOTIFICATION_ON);
                        if (pref.getBoolean(FIRST_TIME, true)) {
                            showNotification();
                            if (ring) {
                                ringsTone();
                            }
                            pref.edit().putBoolean(FIRST_TIME, false).apply();
                        }
                    } else {
                        BroadcastUtil.sendWidgetBroadcast(this, CHANGE_WIFI_ICON_NOTIFICATION_OFF);
                        pref.edit().putBoolean(FIRST_TIME, true).apply();
                    }
                });
    }

    @Override
    public void onDestroy() {
        BroadcastUtil.sendWidgetBroadcast(this, CHANGE_WIFI_ICON_SERVER_STOP);
        if (player != null) {
            player.stop();
        }
        if (!interval.isDisposed()) {
            interval.dispose();
        }
        if (notificationManager != null){
            notificationManager.cancel(0);
        }

        pref.edit().putBoolean(FIRST_TIME, true).apply();
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Subscribe()
    public void onMessageEvent(ServiceMessage event) {
        Log.d(TAG, "onMessageEvent: "+event.getInterval());
        ring = pref.getBoolean(MUSIC_NOTIFICATION, false);
        default_notification = pref.getBoolean(DEFAULT_NOTIFICATION, false);

        if (event.getInterval()>0){
            period = event.getInterval();
            if (!interval.isDisposed()) {
                interval.dispose();
            }
            interval = getSubscribe();
        }


    }



    public boolean isInternetAvailable() {
        try {
            Socket sock = new Socket();
            SocketAddress sockaddr = new InetSocketAddress("8.8.8.8", 53);

            sock.connect(sockaddr, 1000); // This will block no more than timeoutMs
            sock.close();

            return true;

        } catch (IOException e) {
            return false;
        }
    }

    @Subscribe()
    public void onMessageEvent(CancelRing event) {
        if (player != null) {
            player.stop();
        }
    }

}
