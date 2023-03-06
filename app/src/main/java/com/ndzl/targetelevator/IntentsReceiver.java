package com.ndzl.targetelevator;

import static android.content.Context.MODE_PRIVATE;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.Date;
import java.util.UUID;

public class IntentsReceiver extends BroadcastReceiver  {

    void logToSampleDPS(Context context, String _tbw) throws IOException {
        Context ctxDPS = context.createDeviceProtectedStorageContext();
        String _wout = _tbw +" "+ ctxDPS.getFilesDir().getAbsolutePath();
        FileOutputStream fos = ctxDPS.openFileOutput("sampleDPS.txt", Context.MODE_APPEND);
        fos.write(_wout.getBytes(StandardCharsets.UTF_8));
        fos.close();
    }

    void logToSampleCES(Context context, String _tbw) throws IOException {
        FileOutputStream fos = context.openFileOutput("sampleCES.txt", Context.MODE_APPEND);
        String _wout = _tbw +" "+ context.getFilesDir().getAbsolutePath();
        fos.write(_wout.getBytes(StandardCharsets.UTF_8));
        fos.close();
    }

    @Override
    public void onReceive(final Context context, Intent intent) {

        Log.d("com.ndzl.targetelevator", "## event received ");
        //https://developer.android.com/guide/components/broadcast-exceptions
        if (intent != null && intent.getAction().equals("android.intent.action.LOCKED_BOOT_COMPLETED")) {

            //TRY ACCESSING THE DPS WHILE IN DIRECT BOOT
            try {
                String _tbw = "\n"+ DateFormat.getDateTimeInstance().format(new Date(System.currentTimeMillis()))+"\n==LOCKED_BOOT_COMPLETED== received! onReceive/DPS context\n";
                logToSampleDPS(context, _tbw);
            } catch (IOException e) {
                Toast.makeText(context.getApplicationContext(), "\nIOException-logToSampleDPS", Toast.LENGTH_LONG).show();
                //throw new RuntimeException(e);
            }


            //TRY ACCESSING THE CES WHILE IN DIRECT BOOT
            /*
            try {
                String _tbw = "\n"+ DateFormat.getDateTimeInstance().format(new Date(System.currentTimeMillis()))+"\n==LOCKED_BOOT_COMPLETED== received! onReceive/CES context\n";
                logToSampleCES(context, _tbw); //causing fatal exc
            } catch (IOException e) {
                try {
                    logToSampleDPS(context, "\nIOException while logToSampleCES()"); //trying logging in the DPS when a exception occurs - it should be always accessible!
                } catch (IOException ex) {
                    //throw new RuntimeException(ex);
                }
                //throw new RuntimeException(e);
            }

             */


            Log.d("com.ndzl.targetelevator", "==launching mainactivity ! 789");
            //Context mainctxDPS = context.createDeviceProtectedStorageContext();
            Intent runMain = new Intent(context.getApplicationContext(), MainActivity.class);
            runMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.getApplicationContext().startActivity(runMain);
            //Toast.makeText(context.getApplicationContext(), "LOCKED_BOOT_COMPLETED\n"+ ctxDPS.getFilesDir().getAbsolutePath(), Toast.LENGTH_LONG).show();


        }

        //https://developer.android.com/training/articles/direct-boot#notification
        if (intent != null && intent.getAction().equals("android.intent.action.USER_UNLOCKED")) {
            Log.d("com.ndzl.targetelevator", "==RECEIVED USER_UNLOCKED==! 456");

            Context ctxDPS = context.createDeviceProtectedStorageContext();
            try {
                FileOutputStream fos = ctxDPS.openFileOutput("sampleDPS.txt", Context.MODE_APPEND);
                String _tbw = "\n"+ DateFormat.getDateTimeInstance().format(new Date(System.currentTimeMillis()))+"\n==USER_UNLOCKED== received!\n";
                fos.write(_tbw.getBytes(StandardCharsets.UTF_8));
                fos.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Toast.makeText(context.getApplicationContext(), "USER_UNLOCKED", Toast.LENGTH_LONG).show();
        }

        //BOOT_COMPLETED IS MANIFEST-DECLARED
        if (intent != null && intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Log.d("com.ndzl.targetelevator", "==RECEIVED BOOT_COMPLETED==! 927");

            Context ctxDPS = context.createDeviceProtectedStorageContext();
            try {
                FileOutputStream fos = ctxDPS.openFileOutput("sampleDPS.txt", Context.MODE_APPEND);
                String _tbw = "\n"+ DateFormat.getDateTimeInstance().format(new Date(System.currentTimeMillis()))+"\n==BOOT COMPLETED== received!\n";
                fos.write(_tbw.getBytes(StandardCharsets.UTF_8));
                fos.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            Toast.makeText(context.getApplicationContext(), "BOOT COMPLETED!", Toast.LENGTH_LONG).show();
        }

        if (intent != null && intent.getAction().equals("com.ndzl.DW")){
            String _tbw = "\n"+ DateFormat.getDateTimeInstance().format(new Date(System.currentTimeMillis()))+" - com.ndzl.DW received via BROADCAST intent";
            try {
                logToSampleCES(context, _tbw);
            } catch (IOException e) {

            }
        }



    }
}