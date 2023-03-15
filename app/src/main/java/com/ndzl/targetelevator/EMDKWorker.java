package com.ndzl.targetelevator;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.symbol.emdk.EMDKBase;
import com.symbol.emdk.EMDKException;
import com.symbol.emdk.EMDKManager;
import com.symbol.emdk.EMDKResults;
import com.symbol.emdk.ProfileManager;

//WASTED TIME
//https://issuetracker.google.com/issues/236861097?pli=1
//WORKMANAGER NOT WORKING IN DIRECT BOOT!
public class EMDKWorker extends Worker implements EMDKManager.EMDKListener, EMDKManager.StatusListener, ProfileManager.DataListener  {

    String TAG = "com.ndzl.targetelevator/EMDKWorker";
    Context applicationContext;
    public EMDKWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

        applicationContext = context;

    }

    @NonNull
    @Override
    public Result doWork() {

        EMDKResults results = EMDKManager.getEMDKManager(getApplicationContext(), EMDKWorker.this);

        return Result.success();
    }

    @Override
    public void onStopped() {
        super.onStopped();

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
            emdkManager.getInstanceAsync(EMDKManager.FEATURE_TYPE.PROFILE, EMDKWorker.this);
        } catch (EMDKException e) {
            e.printStackTrace();
            Log.d(TAG,"onOpened exception 1");

        }
        catch(Exception ex){
            Log.d(TAG,"onOpened exception 1");

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
}
