package com.example.yendrycalana.internetnotify;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;


import com.example.yendrycalana.internetnotify.evenbus.ServiceMessage;
import com.example.yendrycalana.internetnotify.services.BroadcastWidget;
import com.example.yendrycalana.internetnotify.services.NetworkService;

import org.greenrobot.eventbus.EventBus;

import static com.example.yendrycalana.internetnotify.util.Constants.DEFAULT_NOTIFICATION;
import static com.example.yendrycalana.internetnotify.util.Constants.MUSIC_NOTIFICATION;
import static com.example.yendrycalana.internetnotify.util.Constants.PERIOD;
import static com.example.yendrycalana.internetnotify.util.Constants.SHPF;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "kaka";
    private SeekBar seek;
    SharedPreferences pref;
    Switch service_switch, default_notification_switch, music_notification_switch;
    TextView interval_text;
    private boolean readyToSwitch = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pref = getApplicationContext().getSharedPreferences(SHPF, Context.MODE_PRIVATE);
        interval_text = findViewById(R.id.interval_text_id);
        service_switch = findViewById(R.id.service_switch);
        default_notification_switch = findViewById(R.id.default_switch);
        music_notification_switch = findViewById(R.id.ring_switch);

        interval_text.setText(getString(R.string.check_every)+pref.getInt(PERIOD, 5)+getString(R.string.seconds));

        service_switch.setOnCheckedChangeListener((compoundButton, b) -> {
            Log.d(TAG, "setOnCheckedChangeListener: ");
            if(readyToSwitch) {
                if (b) {
                    startService(new Intent(this, NetworkService.class));
                } else {
                    stopService(new Intent(this, NetworkService.class));
                }
            }
        });
        default_notification_switch.setChecked(pref.getBoolean(DEFAULT_NOTIFICATION, false));
        default_notification_switch.setOnCheckedChangeListener((compoundButton, b) -> {
            pref.edit().putBoolean(DEFAULT_NOTIFICATION, b).apply();
            EventBus.getDefault().post(new ServiceMessage());

        });
        music_notification_switch.setChecked(pref.getBoolean(MUSIC_NOTIFICATION, false));
        music_notification_switch.setOnCheckedChangeListener((compoundButton, b) -> {
            pref.edit().putBoolean(MUSIC_NOTIFICATION, b).apply();
            EventBus.getDefault().post(new ServiceMessage());
        });


        seek = findViewById(R.id.seekBar);
        seek.setProgress(getInterval());
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                Log.d(TAG, "onProgressChanged: "+i);
                ServiceMessage event = new ServiceMessage();
                switch (i) {
                    case 0:
                        pref.edit().putInt(PERIOD, 5).apply();
                        event.setInterval(5);
                        break;
                    case 1:
                        pref.edit().putInt(PERIOD, 20).apply();
                        event.setInterval(20);
                        break;
                    case 2:
                        pref.edit().putInt(PERIOD, 40).apply();
                        event.setInterval(40);
                        break;
                    case 3:
                        pref.edit().putInt(PERIOD, 60).apply();
                        event.setInterval(60);
                        break;
                }
                interval_text.setText(getString(R.string.check_every)+pref.getInt(PERIOD, 5)+getString(R.string.seconds));
                Log.d(TAG, "onProgressChanged: "+event.getInterval());
                EventBus.getDefault().post(event);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onStop() {
        readyToSwitch = false;
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        service_switch.setChecked(isMyServiceRunning(NetworkService.class));
        readyToSwitch = true;
    }


    private int getInterval() {
        int anInt = pref.getInt(PERIOD, 5);
        if (anInt == 5){
            return 0;
        }else if (anInt == 20){
            return 1;
        }else if (anInt == 40){
            return 2;
        }else if (anInt == 60){
            return 3;
        }else {
            return -1;
        }
    }


    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        assert manager != null;
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
