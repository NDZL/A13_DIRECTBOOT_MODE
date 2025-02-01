package com.ndzl.targetelevator


import android.accessibilityservice.AccessibilityService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.CountDownTimer
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import androidx.core.app.NotificationCompat

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.net.URL
import java.util.Timer
import kotlin.concurrent.timerTask

//memo: an accessibility service automatically starts at boot *before* boot completed is received
//best doc on shared audio capturing: https://developer.android.com/guide/topics/media/platform/sharing-audio-input
//A14 porting: audio capturing is not working in A14 as microphone service type, the rest of the code is working (as shortService type)

//GEMINI: in a AccessibilityService,  what is the callback sequence?
//ACCESSIBILITY SERVICE CALLBACK FLOW
//+-------------------+     +---------------------------------+     +-----------------+     +-----------------+
//| onServiceConnected()| --> | onAccessibilityEvent(event) x N | --> | onInterrupt()   | --> | onDestroy()     |
//+-------------------+     +---------------------------------+     +-----------------+     +-----------------+
//       ^                                                                  |
//       |                                                                  |
//       +------------------------------------------------------------------+
//         (Service enabled)                                     (Service disabled)

//AccessibilityService is a special type of service that does not follow the typical lifecycle of a regular service.

//You're right to point out the apparent contradiction! It seems like a catch-22: you need to call startForeground() to make a service a foreground service (FGS), but startForeground() can only be called on a started service, and AccessibilityService components aren't started in the same way as regular services.

class EmergencyAccessibilityService : AccessibilityService() {

    val TAG = "EmergencyAccessibilityService"

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val workerDispatcher = Dispatchers.IO

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
//        serviceScope.launch(workerDispatcher) {
//            // This code runs on a worker thread (Dispatchers.IO)
//            Log.d(TAG, "onAccessibilityEvent : Thread = ${Thread.currentThread().name}")
//
//        }
    }

    override fun onInterrupt() {
    }

    companion object{
        val mm = MicManager( /*this as Context*/)  //EXCP HERE Companion cannot be cast to android.content.Context
        var isA11yConnected = false
        var isA11yStartedAsFGS = false
    }

    override fun onServiceConnected() {
        super.onServiceConnected()

        Log.d(TAG, "accessib service is connected")

        if(!isA11yConnected) {
            isA11yConnected = true
            Log.d(TAG, "accessib service is running")
            val intent = Intent(this, EmergencyAccessibilityService::class.java)
            startService(intent)
        }
    }

    var isAlarmOn = false
    val tg = ToneGenerator(AudioManager.STREAM_ALARM, 100)
    val timerLongPress = object: CountDownTimer(1500, 1000) {
        override fun onTick(millisUntilFinished: Long) {}

        override fun onFinish() {
            isAlarmOn = !isAlarmOn
            Log.d(TAG, "timerLongPress finished")
            triggerEmergencyAlarm()
        }
    }
    override fun onKeyEvent(event: KeyEvent?): Boolean {
        Log.d(TAG, "accessib service key pressed "+event!!.keyCode)
        if( (event!!.keyCode==104 || event!!.keyCode==26 || event!!.keyCode==10036  ) &&  event!!.action==KeyEvent.ACTION_DOWN){
            try {
                timerLongPress.start()
            } catch (e: Exception) { }
        }
        else if(  (event!!.keyCode==104 || event!!.keyCode==26 || event!!.keyCode==10036  ) && event!!.action==KeyEvent.ACTION_UP){
            try {
                timerLongPress.cancel()
            } catch (e: Exception) { }
        }

        return super.onKeyEvent(event)
    }

    lateinit var toneTimer:Timer
    private fun triggerEmergencyAlarm() {

        if(isAlarmOn) {
            Log.d(TAG, "triggerEmergencyAlarm starting Tone")
            try {
                toneTimer = Timer()
                toneTimer.schedule(timerTask {
                    tg.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 200);
                }, 0, 1000)
            } catch (e: Exception) { }
        }
        else{
            Log.d(TAG, "triggerEmergencyAlarm cancel Tone")
            try{
                toneTimer.cancel()
            } catch (e: Exception) { }
        }
    }


    private fun handleKeyEvent(event: KeyEvent): Boolean {
        val action = event.action
        val keyCode = event.keyCode
        if (action == KeyEvent.ACTION_DOWN) {
            when (keyCode) {
                KeyEvent.KEYCODE_VOLUME_DOWN ->                         //do something
                    return true

                KeyEvent.KEYCODE_VOLUME_UP -> {

                    //do something
                    return true
                }
            }
        }
        return false
    }

    //BOOT-AWARE FGS
    private val CHANNEL_ID = "AccessibilityForegroundServiceChannel"

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "NotificatonAccessiblity Name",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(
                NotificationManager::class.java
            )
            manager.createNotificationChannel(serviceChannel)
        }
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        Log.d(TAG, "accessib onStartCommand begin")
        if(!isA11yStartedAsFGS) {
            isA11yStartedAsFGS = true
            Log.d(TAG, "accessib onStartCommand turning A11y service to FGS")
            //TRYING CAPTURING AUDIO WHILE CALL IN UNDERWAY
            getSystemService(WINDOW_SERVICE);

            val toggleWifenceIntent = Intent()
            toggleWifenceIntent.setAction("TURN_OFF_AUDIO_CAPTURE");
            val toggleMICPendingIntent = PendingIntent.getBroadcast(
                applicationContext,
                4004,
                toggleWifenceIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            );

            createNotificationChannel()
            val notificationIntent = Intent(this, MainActivity::class.java)
            val pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_MUTABLE)
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Accessibility Exerciser")
                .setContentText("Accessibility FSG")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .addAction(
                    R.drawable.ic_launcher_background,
                    "AUDIO REC OFF",
                    toggleMICPendingIntent
                )
                .build()
            startForeground(2002, notification, FOREGROUND_SERVICE_TYPE_MICROPHONE)
            Log.d(TAG, "accessib startForeground just called")

            try {
                Log.d(TAG, "trying startRecording")
                mm.startRecording()
            } catch (e: Exception) {
                Log.d(TAG, "startRecording excp: " + e.message)
            }
        }
        else
            Log.d(TAG, "accessib onStartCommand skipping turning A11y service to FGS")

        try {
            Timer("NDZL-TIMER")
                .schedule(timerTask {

                        Log.i(TAG, "timertask started")
                        val cxnt48Emergency = URL("https://cxnt48.com/emergency?get").readText()
                        Log.i(TAG, "cxnt48Emergency: " + cxnt48Emergency)

                        //NEXT START ACTIVITY *NOT WORKING* IN DBM IN A14 tc22
                        if (!cxnt48Emergency.contains("NO_EMERGENCY"))
                            startActivity(
                                Intent().setClassName(
                                    "com.ndzl.targetelevator",
                                    "com.ndzl.targetelevator.EmergencyOverlayActivity"
                                )
                                    .addCategory(Intent.CATEGORY_DEFAULT)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )


                }, 10000, 2000)
            Log.d(TAG, "timer set for emergency check")
        } catch (e: Exception) {
            Log.d(TAG, "timer set error, already existing")
        }

            //return super.onStartCommand(intent, flags, startId)
        return START_STICKY

    }
}