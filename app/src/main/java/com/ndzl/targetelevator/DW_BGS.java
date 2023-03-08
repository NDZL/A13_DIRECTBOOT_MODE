package com.ndzl.targetelevator;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

//USE THIS SERVICE IN COMBINATION WITH DW INTENT OUTPUT SET TO "Intent delivery: startService"
//in the manifest keep just *one* service declared when filtering intents!
//output: in A13 no intent is sent -  Unable to start service Intent { act=com.ndzl.DW cat=[android.intent.category.DEFAULT] cmp=com.ndzl.targetelevator/.DW_BGS (has extras) } U=0: not found
//in A11 this works smoothly

//adb shell am startservice -a com.ndzl.DW -c android.intent.category.DEFAULT --es com.symbol.datawedge.data_string spriz  com.ndzl.targetelevator/.DW_BGS

public class DW_BGS extends IntentService {

    public static final String CHANNEL_ID = "BackgroundServiceChannel";

    public DW_BGS() {
        super("DW_bGS");
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

            //Intent notificationIntent = new Intent(this, MainActivity.class);
            //PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_MUTABLE );


            showToast("DW_bGS barcode: <" + scanData+">");

        }
    }


}