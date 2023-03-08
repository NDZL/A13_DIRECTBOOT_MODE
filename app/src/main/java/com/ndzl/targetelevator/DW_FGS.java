package com.ndzl.targetelevator;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

//adb shell am startservice -a com.ndzl.DW -c android.intent.category.DEFAULT  com.ndzl.targetelevator/.DW_FGS
// --es SSS spriz
//with the activity in foreground

//Applications that target Android 12 or higher can’t start foreground services while running in the background.
//If an application attempts to start a foreground service while running in the background, the 🛑 ForegroundServiceStartNotAllowedException 🛑exception occurs.

// so DW delivery by calling startforegroundservice is no more an option??


public class DW_FGS extends IntentService {

    public static final String CHANNEL_ID = "ForegroundServiceChannel";

    public DW_FGS() {
        super("DW_FGS");
    }

    public void showToast(String message) {
        final String msg = message;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    protected void onHandleIntent(Intent intent) {

        if (intent != null /*&& intent.getAction().equalsIgnoreCase("kKK")*/) {
            String scanData = intent.getStringExtra("com.symbol.datawedge.data_string");

            //  Note that the notification is unlikely to be seen since the scan is processed so quickly but this step is necessary on O+
            createNotificationChannel();
            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_MUTABLE );
            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("DataWedge Background Scanning")
                    .setContentText("barcode processing")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentIntent(pendingIntent)
                    .build();
            startForeground(1, notification);

            showToast("DW_FGS barcode: " + scanData);

        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "DataWedge Background Scanning",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

}