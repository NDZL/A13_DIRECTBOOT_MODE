package com.ndzl.targetelevator;

import static android.app.PendingIntent.FLAG_IMMUTABLE;

import static androidx.core.app.ActivityCompat.startActivityForResult;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;


//ANR in com.ndzl.targetelevator - nice catch!
//Reason: A foreground service of FOREGROUND_SERVICE_TYPE_SHORT_SERVICE did not stop within a timeout: ComponentInfo{com.ndzl.targetelevator/com.ndzl.targetelevator.BA_FGS}


public class BA_FGS extends Service { //BOOT-AWARE FGS
    private static final String CHANNEL_ID = "ForegroundServiceChannel";
    String TAG = "com.ndzl.targetelevator/BA_FGS";

    public void showToast(String message) {
        final String msg = message;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Notificaton Name",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    void logToSampleDPS( String _tbw) throws IOException {

        Context ctxDPS = getApplicationContext().createDeviceProtectedStorageContext();
        String _wout = _tbw ;//+" "+ ctxDPS.getFilesDir().getAbsolutePath();
        FileOutputStream fos = ctxDPS.openFileOutput("sampleDPS.txt", Context.MODE_APPEND);
        fos.write(_wout.getBytes(StandardCharsets.UTF_8));
        fos.close();
    }


    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        try {logToSampleDPS("\nBA_FGS created!");} catch (IOException e) {}

        //SSM
        Context ssmContext = getApplicationContext().createDeviceProtectedStorageContext();
        String _tbw = "\nSSM file content in FGS run after LOCKED_BOOT_COMPLETED: "+ ssmQueryFile(ssmContext, false);
        try {logToSampleDPS(_tbw);} catch (IOException e) {}

        //IntentsReceiver REGISTERED BOTH IN MAINACTIVITY AND HERE
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.ndzl.DW");
        filter.addAction("com.symbol.datawedge.api.NOTIFICATION_ACTION");  //NDZL JAN.2025, test to see if this is received in direct boot mode
        filter.addAction("TURN_OFF_AUDIO_CAPTURE"); //not in the right place here - move to a specific receiver
        filter.addCategory("android.intent.category.DEFAULT");
        registerReceiver(new IntentsReceiver(), filter, Context.RECEIVER_EXPORTED);

        registerForDWnotifications();

    }
    
    void registerForDWnotifications() {
        Bundle b = new Bundle();
        b.putString("com.symbol.datawedge.api.APPLICATION_NAME","com.ndzl.targetelevator");
        b.putString("com.symbol.datawedge.api.NOTIFICATION_TYPE","SCANNER_STATUS");
        Intent i = new Intent();
        i.setAction("com.symbol.datawedge.api.ACTION");
        i.putExtra("com.symbol.datawedge.api.REGISTER_FOR_NOTIFICATION", b);
        this.sendBroadcast(i);
    }   

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "onStartCommand");


        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_MUTABLE );
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Direct Boot Exerciser")
                .setContentText("testing FGS")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);

        addOverlayView();


        return super.onStartCommand(intent, flags, startId);
    }

    private WindowManager wm;
    private Button button;
    private void addOverlayView() {

        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        button = new Button(this);
        //button.setBackgroundResource(R.drawable.ic_button1);
        button.setText("ALERT MESSAGE HERE!");
        button.setAlpha(1);
        button.setBackgroundColor(Color.BLUE);
        //button.setOnClickListener(this);

        int type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;

/*        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.START | Gravity.TOP;
        params.x = 0;
        params.y = 0;
        */


        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);


       // wm.addView(button, params);

/*
        //OVERLAY EXERCISE - ALSO FOR DIRECT BOOT - usecase: to display an alert sent from a server to the user
        //20.12.2023: YES THIS WORKSin direct boot mode. however when this activity is displayed, the accessibility service does not capture pressed keys. to be tweaked
        startActivity(new Intent().setClassName("com.ndzl.targetelevator", "com.ndzl.targetelevator.EmergencyOverlayActivity")
                .addCategory("android.intent.category.DEFAULT")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
*/

    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private final String AUTHORITY = "content://com.zebra.securestoragemanager.securecontentprovider/data";
    private final String AUTHORITY_FILE = "content://com.zebra.securestoragemanager.securecontentprovider/files/";
    private final String RETRIEVE_AUTHORITY = "content://com.zebra.securestoragemanager.securecontentprovider/file/*";
    private final String COLUMN_DATA_NAME = "data_name";
    private final String COLUMN_DATA_VALUE = "data_value";
    private final String COLUMN_DATA_TYPE = "data_type";
    private final String COLUMN_DATA_PERSIST_REQUIRED = "data_persist_required";
    private final String COLUMN_TARGET_APP_PACKAGE = "target_app_package";
    private final String signature = "";
    String ssmQueryFile(Context context, boolean isReadFromWorkProfile) {
        Uri uriFile = Uri.parse(RETRIEVE_AUTHORITY);  //original - usually works
        //Uri uriFile = Uri.parse(AUTHORITY_FILE);//test for direct boot
        String selection = "target_app_package='com.ndzl.targetelevator'"; //GETS *ALL FILES* FOR THE PACKAGE NO PERSISTANCE FILTER
        Log.i(TAG, "File selection " + selection);
        Log.i(TAG, "File cpUriQuery " + uriFile.toString());

        String res = "N/A";
        Cursor cursor = null;
        try {
            Log.i(TAG, "Before calling query API Time");
            cursor = context.getContentResolver().query(uriFile, null, selection, null, null);
            Log.i(TAG, "After query API called TIme");
        } catch (Exception e) {
            Log.d(TAG, "Error: " + e.getMessage());
        }
        try {
            if (cursor != null && cursor.moveToFirst()) {
                StringBuilder strBuild = new StringBuilder();
                String uriString;
                strBuild.append("FILES FOUND: "+cursor.getCount()+"\n");
                while (!cursor.isAfterLast()) {

                    uriString = cursor.getString(cursor.getColumnIndex("secure_file_uri"));
                    if(isReadFromWorkProfile)
                        uriString = uriString.replace("/0/", "/10/"); //ATTEMPT TO  ACCESS WORK PROFILE SSM FROM MAIN USER => Permission Denial: reading com.zebra.securestoragemanager.SecureFileProvider uri content://com.zebra.securestoragemanager.SecureFileProvider/user_de/data/user_de/10/com.zebra.securestoragemanager/files/com.ndzl.sst_companionapp/enterprise.txt from pid=19235, uid=10216 requires the provider be exported, or grantUriPermission()

                    String fileName = cursor.getString(cursor.getColumnIndex("secure_file_name"));
                    String isDir = cursor.getString(cursor.getColumnIndex("secure_is_dir"));
                    String crc = cursor.getString(cursor.getColumnIndex("secure_file_crc"));
                    strBuild.append("\n");
                    strBuild.append("URI - " + uriString).append("\n").append("FileName - " + fileName).append("\n").append("IS Directory - " + isDir)
                            .append("\n").append("CRC - " + crc).append("\n").append("FileContent - ").append(readFile(context, uriString));
                    Log.i(TAG, "File cursor " + strBuild);
                    strBuild.append("\n ----------------------").append("\n");

                    cursor.moveToNext();
                }
                Log.d(TAG, "Query File: " + strBuild);
                Log.d("Client - Query", "Set test to view =  " + System.currentTimeMillis());
                res =strBuild.toString();
            } else {
                res="No files to query for local package "+context.getPackageName();
            }
        } catch (Exception e) {
            Log.d(TAG, "Files query data error: " + e.getMessage());
            res="EXCP-"+e.getMessage();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return res;
    }

    private String readFile(Context context, String uriString) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(Uri.parse(uriString));
        InputStreamReader isr = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            sb.append(line);
        }
        Log.d(TAG, "full content = " + sb);
        return sb.toString();
    }

}