package com.ndzl.targetelevator;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.Date;

//adb shell am broadcast -a com.ndzl.DW -c android.intent.category.DEFAULT --es com.symbol.datawedge.data_string spriz  com.ndzl.targetelevator/.IntentsReceiver
public class IntentsReceiver extends BroadcastReceiver{


    @Override
    public void onReceive(final Context context, Intent intent) {


        if (intent != null && intent.getAction().equals("com.ndzl.DW")){
            String barcode_value = intent.getStringExtra("com.symbol.datawedge.data_string");
            String _tbw = "\n"+ DateFormat.getDateTimeInstance().format(new Date(System.currentTimeMillis()))+" - com.ndzl.DW received via BROADCAST intent <"+barcode_value+">";
            Log.i("IntentsReceiver", "DW SENT: " + barcode_value);
        }


    }

    public void showContexedToast(Context context, String message) {
        final String msg = message;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText( context, msg, Toast.LENGTH_LONG).show();
            }
        });
    }



}