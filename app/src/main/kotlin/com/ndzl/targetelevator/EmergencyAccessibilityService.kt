package com.ndzl.targetelevator

import android.accessibilityservice.AccessibilityService
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.CountDownTimer
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import java.util.Timer
import kotlin.concurrent.timerTask


class EmergencyAccessibilityService : AccessibilityService() {

    val TAG = "EmergencyAccessibilityService"
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        Log.d(TAG, "onAccessibiltyEvent" + event.toString())
    }

    override fun onInterrupt() {
    }

    override fun onServiceConnected() {
        super.onServiceConnected()

        Log.d(TAG, "accessib service is connected")
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
}