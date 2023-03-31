package com.ndzl.targetelevator;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.symbol.emdk.EMDKBase;
import com.symbol.emdk.EMDKException;
import com.symbol.emdk.EMDKManager;
import com.symbol.emdk.EMDKResults;
import com.symbol.emdk.ProfileManager;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

//USE THIS SERVICE IN COMBINATION WITH DW INTENT OUTPUT SET TO "Intent delivery: startForegroundService"
//in the manifest keep just one service declared when filtering intents!
//output: in A13 no intent is sent, Unable to start service Intent { act=com.ndzl.DW cat=[android.intent.category.DEFAULT] cmp=com.ndzl.targetelevator/.EMDK_FGS (has extras) } U=0: not found
//in A11 this works smoothly



//adb shell am startservice -a com.ndzl.DW -c android.intent.category.DEFAULT  com.ndzl.targetelevator/.EMDK_FGS
// --es SSS spriz
//with the activity in foreground

//Applications that target Android 12 or higher can’t start foreground services while running in the background.
//If an application attempts to start a foreground service while running in the background, the 🛑 ForegroundServiceStartNotAllowedException 🛑exception occurs.

// so DW delivery by calling startforegroundservice is no more an option??

//Exemptions from background start restrictions:
// After the device reboots and receives the ACTION_BOOT_COMPLETED, ACTION_LOCKED_BOOT_COMPLETED, or ACTION_MY_PACKAGE_REPLACED intent action in a broadcast receiver.
//Constant Value: "android.intent.action.LOCKED_BOOT_COMPLETED"

public class EMDK_FGS extends Service implements EMDKManager.EMDKListener, EMDKManager.StatusListener, ProfileManager.DataListener  {
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    String TAG = "com.ndzl.targetelevator/EMDK_FGS";

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
                    "DataWedge Background Scanning",
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
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        try {logToSampleDPS("\nEMDK_FGS created!");} catch (IOException e) {}

        //EMDK TEST
        EMDKResults results = EMDKManager.getEMDKManager(getApplicationContext(), EMDK_FGS.this); //DEPENDACY_COMPONENT_FAILURE

        //SSM
        Context ssmContext = getApplicationContext().createDeviceProtectedStorageContext();
        String _tbw = "\nSSM file content in FGS run after LOCKED_BOOT_COMPLETED: "+ ssmQueryFile(ssmContext, false);
        try {logToSampleDPS(_tbw);} catch (IOException e) {}
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "onStartCommand");
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_MUTABLE );
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("EMDK in Direct Boot")
                .setContentText("testing FGS")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);


        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //TESTING ZEBRA EMDK
    private ProfileManager profileManager = null;
    private EMDKManager emdkManager = null;
    String profileToBeApplied = "SOMESETTING";
    @Override
    public void onOpened(EMDKManager emdkManager) {
        emdkManager = emdkManager;

        Log.d(TAG,"onOpened before getInstanceAsync");

        try {
            emdkManager.getInstanceAsync(EMDKManager.FEATURE_TYPE.PROFILE, EMDK_FGS.this);
        } catch (EMDKException e) {
            e.printStackTrace();
            Log.d(TAG,"onOpened exception 1");

        }
        catch(Exception ex){
            Log.d(TAG,"onOpened exception 2");

        }
        Log.d(TAG,"onOpened after getInstanceAsync");

    }

    @Override
    public void onClosed() {

    }

    @Override
    public void onStatus(EMDKManager.StatusData statusData, EMDKBase emdkBase) {
        if(statusData.getResult() == EMDKResults.STATUS_CODE.SUCCESS) {
            if(statusData.getFeatureType() == EMDKManager.FEATURE_TYPE.PROFILE)
            {
                profileManager = (ProfileManager)emdkBase;
                profileManager.addDataListener(this);
                ApplyEMDKprofile();
                //finish();
                //System.exit(0);
            }
        }
    }

    private void ApplyEMDKprofile(){
        if (profileManager != null) {
            String[] modifyData = new String[1];

            final EMDKResults results = profileManager.processProfileAsync(profileToBeApplied,
                    ProfileManager.PROFILE_FLAG.SET, modifyData);

            String sty = results.statusCode.toString();
        }
    }

    @Override
    public void onData(ProfileManager.ResultData resultData) {
        EMDKResults result = resultData.getResult();
        if(result.statusCode == EMDKResults.STATUS_CODE.CHECK_XML) {
            String responseXML = result.getExtendedStatusMessage();
            //Toast.makeText(MainActivity.this, "RESPONSE="+responseXML, Toast.LENGTH_LONG).show();
        } else if(result.statusCode != EMDKResults.STATUS_CODE.SUCCESS) {
            //Toast.makeText(MainActivity.this, "ERROR IN PROFILE APPLICATION", Toast.LENGTH_LONG).show();
        }
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