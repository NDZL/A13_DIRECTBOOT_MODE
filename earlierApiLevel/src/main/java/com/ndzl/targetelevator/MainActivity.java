package com.ndzl.targetelevator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tvOut = findViewById(R.id.tvout);

        //REGISTER  RECEIVER
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.ndzl.DW");
        filter.addCategory("android.intent.category.DEFAULT");

        //TARGET API 31 & API 33: COMPILES AND RUNS ON 14 SMOOTHLY
        //TARGET API 34: LINT HIGHLIGHTS AN ISSUE, CODE COMPILES, FAILS TO RUN ON A14: com.ndzl.earliertarget: One of RECEIVER_EXPORTED or RECEIVER_NOT_EXPORTED should be specified when a receiver isn't being registered exclusively for system broadcasts                                                                                                  Caused by: java.lang.SecurityException: com.ndzl.earliertarget: One of RECEIVER_EXPORTED or RECEIVER_NOT_EXPORTED should be specified when a receiver isn't being registered exclusively for system broadcasts
        registerReceiver(new IntentsReceiver(), filter);






    }


}