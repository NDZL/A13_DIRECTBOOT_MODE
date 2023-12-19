package com.ndzl.targetelevator

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent


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

    override fun onKeyEvent(event: KeyEvent?): Boolean {
        Log.d(TAG, "accessib service key pressed "+event!!.keyCode)
        return super.onKeyEvent(event)
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