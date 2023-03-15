package com.ndzl.targetelevator;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.symbol.emdk.EMDKBase;
import com.symbol.emdk.EMDKException;
import com.symbol.emdk.EMDKManager;
import com.symbol.emdk.EMDKResults;
import com.symbol.emdk.ProfileManager;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

//USE THIS SERVICE IN COMBINATION WITH DW INTENT OUTPUT SET TO "Intent delivery: startService"
//in the manifest keep just *one* service declared when filtering intents!
//output: in A13 no intent is sent -  Unable to start service Intent { act=com.ndzl.DW cat=[android.intent.category.DEFAULT] cmp=com.ndzl.targetelevator/.DW_BGS (has extras) } U=0: not found
//in A11 this works smoothly

//adb shell am startservice -a com.ndzl.DW -c android.intent.category.DEFAULT --es com.symbol.datawedge.data_string spriz  com.ndzl.targetelevator/.DW_BGS

public class DW_BGS extends IntentService implements EMDKManager.EMDKListener, EMDKManager.StatusListener, ProfileManager.DataListener {

    //public static final String CHANNEL_ID = "BackgroundServiceChannel";

    public DW_BGS() {
        super("DW_bGS");
    }

    void logToSampleDPS( String _tbw)  {
        try {
            Context ctxDPS = createDeviceProtectedStorageContext();
            String _wout = "\n"+_tbw ;
            FileOutputStream fos = ctxDPS.openFileOutput("sampleDPS.txt", Context.MODE_APPEND);
            fos.write(_wout.getBytes(StandardCharsets.UTF_8));
            fos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void showToast(String message) {
        final String msg = message;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                try {
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                }

                Toast.makeText(getApplicationContext(), "SHUTTING DOWN", Toast.LENGTH_SHORT).show();
                emdkManager.release();
            }
        });
    }


    private ProfileManager profileManager = null;
    private EMDKManager emdkManager = null;
    String profileToBeApplied = "SOMESETTING";
    @Override
    protected void onHandleIntent(Intent intent) {

        //Context ctxDPS = createDeviceProtectedStorageContext();
        EMDKResults results = EMDKManager.getEMDKManager(getApplicationContext(), DW_BGS.this);
        logToSampleDPS("onHandleIntent after getEMDKManager");

        if (intent != null /*&& intent.getAction().equalsIgnoreCase("kKK")*/) {
            String scanData = intent.getStringExtra("com.symbol.datawedge.data_string");



            //Intent notificationIntent = new Intent(this, MainActivity.class);
            //PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_MUTABLE );


            showToast("DW_bGS barcode: <" + scanData+">");

        }
    }


    @Override
    public void onOpened(EMDKManager emdkManager) {
        this.emdkManager = emdkManager;

        logToSampleDPS("onOpened before getInstanceAsync");

        try {
            emdkManager.getInstanceAsync(EMDKManager.FEATURE_TYPE.PROFILE, DW_BGS.this);
        } catch (EMDKException e) {
            e.printStackTrace();
            logToSampleDPS("onOpened exception 1");

        }
        catch(Exception ex){
            logToSampleDPS("onOpened exception 1");

        }
        logToSampleDPS("onOpened after getInstanceAsync");

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
}