package com.ndzl.targetelevator;

import static android.content.Intent.ACTION_INSTALL_PACKAGE;
import static android.content.Intent.ACTION_SEND;
import static android.content.Intent.ACTION_VIEW;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.symbol.emdk.EMDKBase;
import com.symbol.emdk.EMDKException;
import com.symbol.emdk.EMDKManager;
import com.symbol.emdk.EMDKResults;
import com.symbol.emdk.ProfileManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/*README
https://developer.android.com/training/articles/direct-boot
* TO RUN AND DEBUG IN WORK PROFILE
* - INSTALLING: EDIT ANDROID STUDIO RUN CONFIGURATION AND SET "INSTALL FOR ALL USERS" FLAG
* - DEBUGGING: MANUALLY START THE BADGED-WORK PROFILE APP, THEN ATTACH DEBUGGER FROM ANDROID STUDIO
* - TO APPLY CHANGES / UPDATING THE APP: ISSUE COMMANDLINE adb uninstall com.ndzl.targetelevator, THEN REINSTALL with android studio
* */


public class MainActivity extends AppCompatActivity implements EMDKManager.EMDKListener, EMDKManager.StatusListener, ProfileManager.DataListener {

    static private void copy(InputStream in, File dst) throws IOException {
        FileOutputStream out=new FileOutputStream(dst);
        byte[] buf=new byte[1024];
        int len;

        while ((len=in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }

        in.close();
        out.close();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tvOut = findViewById(R.id.tvout);

        //REGISTER  RECEIVER
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.LOCKED_BOOT_COMPLETED");

        filter.addAction("com.ndzl.DW");
        filter.addCategory("android.intent.category.DEFAULT");

        registerReceiver(new IntentsReceiver(), filter);

        IntentFilter userUnlockedFilter = new IntentFilter();

        //userUnlockedFilter.addAction("android.intent.action.USER_UNLOCKED");
        //registerReceiver(new UserUnlockedIntentReceiver(), userUnlockedFilter);
        //Log.d("com.ndzl.targetelevator", "==REGISTERING RECEIVER! 000");

        //EMDKResults results = EMDKManager.getEMDKManager(getApplicationContext(), MainActivity.this);

        // creating and reading a file in the Device Encrypted Storage
        String fileNameDPS = "sampleDPS.txt";
        Context ctxDPS = createDeviceProtectedStorageContext();
        String pathDPS = ctxDPS.getFilesDir().getAbsolutePath();
        String pathAndFileDPS= pathDPS+"/"+fileNameDPS;

        String dps_fileContent="N/A";

        try {
            FileOutputStream fos = ctxDPS.openFileOutput(fileNameDPS, MODE_APPEND);  //DO NOT use a fullpath, rather just the filename // in /data/user_de/0/com.ndzl.targetelevator/files or /data/user_de/10/com.ndzl.targetelevator/files
            String _tbw = "\n"+DateFormat.getDateTimeInstance().format(new Date(System.currentTimeMillis()))+" MainActivity/OnCreate/DPS Context "+ UUID.randomUUID()+"\n";
            fos.write(_tbw.getBytes(StandardCharsets.UTF_8));
            fos.close();
        } catch (FileNotFoundException e) {
            dps_fileContent = "FNF EXCP:"+e.getMessage();
        } catch (IOException e) {
            dps_fileContent = "IO WRITE EXCP:"+e.getMessage();
        }


        try {
            dps_fileContent = readFile(ctxDPS, fileNameDPS);
        } catch (IOException e) {
            dps_fileContent = "IO READ EXCP:"+e.getMessage();
        }

       // DevicePolicyManager dmp = new DevicePolicyManager();
        // int ses = dmp.getStorageEncryptionStatus();


        //Toast.makeText(getApplicationContext(), ""+ pathAndFileDPS, Toast.LENGTH_LONG).show();

        //--then creating and reading a file in the Credential Encrypted Storage
        String fileNameCES = "sampleCES.txt";
        Context ctxCES = this;
        String pathCES = ctxCES.getFilesDir().getAbsolutePath();
        String pathAndFileCES= pathCES+"/"+fileNameCES;

        String ces_fileContent="N/A";

        try {
            FileOutputStream fos = ctxCES.openFileOutput(fileNameCES, MODE_APPEND);
            String _tbw = "\n"+DateFormat.getDateTimeInstance().format(new Date(System.currentTimeMillis()))+" MainActivity/OnCreate/CES Context "+ UUID.randomUUID()+"\n";
            fos.write(_tbw.getBytes(StandardCharsets.UTF_8));
            fos.close();
        } catch (FileNotFoundException e) {
            ces_fileContent = "FNF EXCP:"+e.getMessage();
        } catch (IOException e) {
            ces_fileContent = "IO WRITE EXCP:"+e.getMessage();
        }




        try {
            ces_fileContent = readFile(ctxCES, fileNameCES);
        } catch (IOException e) {
            ces_fileContent = "IO READ EXCP:"+e.getMessage();
        }



        tvOut.setText("DEVICE PROTECTED STORAGE\nPrinted at "+DateFormat.getDateTimeInstance().format(new Date(System.currentTimeMillis()))+"\nFILE:\n"+pathAndFileDPS+"\nCONTENT:\n"+dps_fileContent+"\n\nCREDENTIAL ENCRYPTED STORAGE\nFILE:\n"+pathAndFileCES+"\nCONTENT:\n"+ces_fileContent+"\n");

        //testing calling services
//        Intent fgsi = new Intent(this, EMDK_FGS.class);
//        startForegroundService(fgsi);

        //to test emdk instanced in a service - this works well
        //Intent bgsi = new Intent(this, DW_BGS.class);
        //startService( bgsi );

/*//EMDK TEST VIA WORKMANAGER WORKMANAGER NOT WORKING IN DIRECT BOOT!
        try{
            schedulePeriodicJob();
        }
        catch(Exception e){
            e.printStackTrace();
        }*/
    }


    private String readFile(Context context, String uriString) throws IOException {
        InputStream inputStream =   context.openFileInput(uriString);
        InputStreamReader isr = new InputStreamReader(inputStream);

        BufferedReader bufferedReader = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            sb.append(line+"\n");
        }
        Log.d("com.ndzl.targetelevator", "full content = " + sb);
        return sb.toString();
    }


    //https://developer.android.com/reference/androidx/work/PeriodicWorkRequest?hl=en
    public void schedulePeriodicJob() {
        //MIN PERIOD 15'  Interval duration lesser than minimum allowed value; Changed to 900000
        PeriodicWorkRequest pingWorkRequest =
                new PeriodicWorkRequest.Builder(EMDKWorker.class, 15, TimeUnit.MINUTES, 5, TimeUnit.MINUTES)
                        .addTag("PERIODIC_EMDK_TASK")
                        //.setConstraints(anyNetworkConstraint)
                        .build();

        //OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(EMDKWorker.class)
        //        .build();

        WorkManager
                .getInstance(this)
                .enqueueUniquePeriodicWork("uniqueWorkEMDK",
                        ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                        pingWorkRequest);
                //.enqueue( oneTimeWorkRequest );


    }

    private ProfileManager profileManager = null;
    private EMDKManager emdkManager = null;
    String profileToBeApplied = "SOMESETTING";
    @Override
    public void onOpened(EMDKManager emdkManager) {
        try {
            emdkManager.getInstanceAsync(EMDKManager.FEATURE_TYPE.PROFILE, MainActivity.this);
        } catch (EMDKException e) {
            e.printStackTrace();
        }
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
                //ApplyEMDKprofile();
                //finish();
                //System.exit(0);
            }
        }
    }

    @Override
    public void onData(ProfileManager.ResultData resultData) {

    }
}

//2022-12-22 15:41:55.043  1803-1843  ActivityTaskManager     pid-1803                             W  Showing SDK deprecation warning for package com.ndzl.earlytarget
//        // DO NOT MODIFY. Used by CTS to verify the dialog is displayed.
//        window.getAttributes().setTitle("DeprecatedTargetSdkVersionDialog");

//        try {
//
//                InputStream in = getResources().openRawResource(R.raw.earlytarget_22);
//                File out=new File(getCacheDir(), "earlytarget_22.apk");
//                // File out=new File(getCacheDir(), "earlytarget_33.apk");
//                //File out=new File(getCacheDir(), "zebra_xamarin_v6.pdf");
//                copy(in, out);
//                long outlen = out.length();
//
//
//                String authority = getApplicationContext().getPackageName() + ".provider";
//
//                Uri apkURI = FileProvider.getUriForFile(this, authority, out);
//                /*URI examples, on TC21 A11 -
//                 * PDF: content://com.ndzl.targetelevator.provider/cache_files/zebra_xamarin_v6.pdf  <==WORKING!
//                 * APK: it works fine, app installer is invoked */
//
//                Intent intent = new Intent();
//                intent.setDataAndType(apkURI, "application/vnd.android.package-archive");
//                //pdf//intent.setDataAndType(apkURI, "application/pdf");
//
//                intent.setAction(ACTION_VIEW);
//                //-old-//intent.setFlags(IntePERMISSION_REQUEST_CODEnt.FLAG_ACTIVITY_NEW_TASK);
//
//                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//
//                startActivity(intent);
//
//                } catch (IOException e) {
//
//                int x=0;
//                }