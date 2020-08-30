package com.example.led_control;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import static com.example.led_control.App.CHANNEL_ID;
import static com.example.led_control.ledControl.btSocket;

public class Service extends android.app.Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean bl_status = intent.getBooleanExtra("blStatus", false);

        Intent notificationIntent = new Intent(this, ledControl.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        String content = "WTf";


        if (btSocket != null) {
            content = "it is connected!";

        } else {
            content = "nooo";

        }

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Rainmaker")
                .setContentText(content)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);


        return START_NOT_STICKY;


    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
