package com.ndzl.targetelevator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
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
import java.text.DateFormat;
import java.util.Date;

//adb shell am broadcast -a com.ndzl.DW -c android.intent.category.DEFAULT --es com.symbol.datawedge.data_string spriz  com.ndzl.targetelevator/.IntentsReceiver
public class IntentsReceiver extends BroadcastReceiver implements EMDKManager.EMDKListener, EMDKManager.StatusListener, ProfileManager.DataListener {
    private ProfileManager profileManager = null;
    private EMDKManager emdkManager = null;
    String profileToBeApplied = "SOMESETTING";
    void logToSampleDPS(Context context, String _tbw) throws IOException {
        Context ctxDPS = context.createDeviceProtectedStorageContext();
        String _wout = _tbw ;//+" "+ ctxDPS.getFilesDir().getAbsolutePath();
        FileOutputStream fos = ctxDPS.openFileOutput("sampleDPS.txt", Context.MODE_APPEND);
        fos.write(_wout.getBytes(StandardCharsets.UTF_8));
        fos.close();
    }

    void logToSampleCES(Context context, String _tbw) throws IOException {
        FileOutputStream fos = context.openFileOutput("sampleCES.txt", Context.MODE_APPEND);
        String _wout = _tbw;// +" "+ context.getFilesDir().getAbsolutePath();
        fos.write(_wout.getBytes(StandardCharsets.UTF_8));
        fos.close();
    }

    @Override
    public void onReceive(final Context context, Intent intent) {

        //Log.d("com.ndzl.targetelevator", "## event received ");

        //https://developer.android.com/guide/components/broadcast-exceptions
        if (intent != null && intent.getAction().equals("android.intent.action.LOCKED_BOOT_COMPLETED")) {

            //TRY ACCESSING THE DPS WHILE IN DIRECT BOOT
            try {
                String _tbw = "\n"+ DateFormat.getDateTimeInstance().format(new Date(System.currentTimeMillis()))+"\n==LOCKED_BOOT_COMPLETED== received! onReceive/DPS context\n";
                logToSampleDPS(context, _tbw);

                //TESTING SSM
                _tbw = "\nSSM data records after boot: "+ ssm_notpersisted_countRecords(context);
                logToSampleDPS(context, _tbw);

                _tbw = "\nSSM file content after boot: "+ ssmQueryFile(context, false);
                logToSampleDPS(context, _tbw);
            } catch (IOException e) {
                Toast.makeText(context.getApplicationContext(), "\nIOException-logToSampleDPS", Toast.LENGTH_LONG).show();
                //throw new RuntimeException(e);
            }


            //TRY ACCESSING THE CES WHILE IN DIRECT BOOT
            try {
                String _tbw = "\n"+ DateFormat.getDateTimeInstance().format(new Date(System.currentTimeMillis()))+"\n==LOCKED_BOOT_COMPLETED== received! onReceive/CES context\n";
                logToSampleCES(context, _tbw); //causing fatal exc
            } catch (IOException e) {
                try {
                    logToSampleDPS(context, "\nIOException 111 while logToSampleCES()"); //trying logging in the DPS when a exception occurs - it should be always accessible!
                } catch (IOException ex) {
                    //throw new RuntimeException(ex);
                }
                //throw new RuntimeException(e);
            }

            //TRY WORKING EMDK - not working here
             //   EMDKResults results = EMDKManager.getEMDKManager(context, IntentsReceiver.this);

            //starting a FGS after LOCKED_BOOT_COMPLETED receive is a valid exception!
            //After the device reboots and receives the ACTION_BOOT_COMPLETED, ACTION_LOCKED_BOOT_COMPLETED, or ACTION_MY_PACKAGE_REPLACED intent action in a broadcast receiver.
            // Constant Value: "android.intent.action.LOCKED_BOOT_COMPLETED"
            Intent fgsi = new Intent(context.getApplicationContext(), EMDK_FGS.class);
            try {
                context.getApplicationContext().startForegroundService(fgsi);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
/*
            //to test emdk in direct boot mode - cannot start  a service from receiver
            Intent bgsi = new Intent(context, DW_BGS.class);
            context.startService( bgsi );

            Log.d("com.ndzl.targetelevator", "==launching mainactivity ! 789");
            //Context mainctxDPS = context.createDeviceProtectedStorageContext();
            Intent runMain = new Intent(context, MainActivity.class);
            runMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(runMain); //no, you cannot launch an activity while in direct boot mode
            //Toast.makeText(context.getApplicationContext(), "LOCKED_BOOT_COMPLETED\n"+ ctxDPS.getFilesDir().getAbsolutePath(), Toast.LENGTH_LONG).show();
*/



        }

        //https://developer.android.com/training/articles/direct-boot#notification
        if (intent != null && intent.getAction().equals("android.intent.action.USER_UNLOCKED")) {
            Log.d("com.ndzl.targetelevator", "==RECEIVED USER_UNLOCKED==! 456");

            Context ctxDPS = context.createDeviceProtectedStorageContext();
            try {
                String _tbw = "\n"+ DateFormat.getDateTimeInstance().format(new Date(System.currentTimeMillis()))+"\n==USER_UNLOCKED== received!\n";
                logToSampleDPS(context, _tbw);
                logToSampleCES(context, _tbw);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Toast.makeText(context.getApplicationContext(), "USER_UNLOCKED", Toast.LENGTH_LONG).show();
        }

        //BOOT_COMPLETED IS MANIFEST-DECLARED
        if (intent != null && intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Log.d("com.ndzl.targetelevator", "==RECEIVED BOOT_COMPLETED==! 927");

            try {
                String _tbw = "\n"+ DateFormat.getDateTimeInstance().format(new Date(System.currentTimeMillis()))+"\n==BOOT COMPLETED== received!\n";
                logToSampleDPS(context, _tbw);
                logToSampleCES(context, _tbw);
            } catch (IOException e) {
                try {
                    logToSampleDPS(context, "\nIOException 222 while logToSampleCES()");
                } catch (IOException ex) {}
            }

            Toast.makeText(context.getApplicationContext(), "BOOT COMPLETED!", Toast.LENGTH_LONG).show();
            //BOOT-COMPLETED INTENT RECEIVED ON TC21-A11 40 SECONDS AFTER UNLOCKING WITH PIN (AND SECURE STARTUP ENABLED), BOTH DPS AND CES AVAILABLE
            //A13, RECEIVED AFTER 40 SECS MORE OR LESS. TOO BUT CES NOT AVAILABLE IN THAT MOMENT
        }

        if (intent != null && intent.getAction().equals("com.ndzl.DW")){
            String _tbw = "\n"+ DateFormat.getDateTimeInstance().format(new Date(System.currentTimeMillis()))+" - com.ndzl.DW received via BROADCAST intent";
            try {
                logToSampleCES(context, _tbw);
                //TESTING SSM
                _tbw = "\nSSM data records after DW scan: "+ssm_notpersisted_countRecords(context);
                logToSampleDPS(context, _tbw);

                _tbw = "\nSSM file content after DW scan: "+ ssmQueryFile(context, false);
                logToSampleDPS(context, _tbw);




            } catch (IOException e) {

            }
            //FOR TESTING ONLY, REMOVE THEN
            //EMDKResults results = EMDKManager.getEMDKManager(context.getApplicationContext(), IntentsReceiver.this);
            /*
            Intent runMain = new Intent(context, MainActivity.class);
            runMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(runMain);
            */

        }



    }

    //TESTING ZEBRA SSM
    private final String AUTHORITY = "content://com.zebra.securestoragemanager.securecontentprovider/data";
    private final String AUTHORITY_FILE = "content://com.zebra.securestoragemanager.securecontentprovider/files/";
    private final String RETRIEVE_AUTHORITY = "content://com.zebra.securestoragemanager.securecontentprovider/file/*";
    private final String COLUMN_DATA_NAME = "data_name";
    private final String COLUMN_DATA_VALUE = "data_value";
    private final String COLUMN_DATA_TYPE = "data_type";
    private final String COLUMN_DATA_PERSIST_REQUIRED = "data_persist_required";
    private final String COLUMN_TARGET_APP_PACKAGE = "target_app_package";
    private static final String TAG = "com.ndzl.targetelevator";
    String ssm_notpersisted_countRecords(Context context) {
        Uri cpUriQuery = Uri.parse(AUTHORITY + "/[" + context.getPackageName() + "]");
        String selection = COLUMN_TARGET_APP_PACKAGE + " = '" + context.getPackageName() + "'" + " AND " + COLUMN_DATA_PERSIST_REQUIRED + " = 'false'" + " AND " + COLUMN_DATA_TYPE + " = '" + "1" + "'";

        int _count=0;
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(cpUriQuery, null, selection, null, null);
            _count = cursor.getCount();
        } catch (Exception e) {
            Log.d(TAG, "Error: " + e.getMessage());
        }
        finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return  _count+"\n SSM Data Uri="+cpUriQuery.toString()+" ";
    }

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



    //TESTING ZEBRA EMDK
    @Override
    public void onOpened(EMDKManager emdkManager) {
        this.emdkManager = emdkManager;

        Log.d("com.ndzl.targetelevator","onOpened before getInstanceAsync");

        try {
            emdkManager.getInstanceAsync(EMDKManager.FEATURE_TYPE.PROFILE, IntentsReceiver.this);
        } catch (EMDKException e) {
            e.printStackTrace();
            Log.d("com.ndzl.targetelevator","onOpened exception 1");

        }
        catch(Exception ex){
            Log.d("com.ndzl.targetelevator","onOpened exception 1");

        }
        Log.d("com.ndzl.targetelevator","onOpened after getInstanceAsync");

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