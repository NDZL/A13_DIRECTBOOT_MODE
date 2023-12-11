package com.ndzl.targetelevator

import android.app.Application
import android.content.Context
import android.media.MediaRecorder
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import java.io.IOException

private const val LOG_TAG = "MicManager"
class MicManager {

    private var fileName: String = "/enterprise/usr/persist/mic-audio.3gp"

    private var recorder: MediaRecorder? = null

    fun showToast(message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(
                requireContext().applicationContext,
                message,
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    public fun startRecording() {
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(fileName)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            chmodFile(fileName)
            Log.i(LOG_TAG, "AUDIO RECORDING STARTED")
            try {
                prepare()
            } catch (e: IOException) {
                Log.e(LOG_TAG, "prepare() failed")
            }

            start()
        }
    }

    public fun stopRecording() {
        recorder?.apply {
            stop()
            release()
            Log.i(LOG_TAG, "AUDIO RECORDING STOPPED")
        }
        recorder = null
    }

    private fun chmodFile(sourcePath: String){
        try {
            val _p =
                Runtime.getRuntime().exec("chmod 666 $sourcePath") //chmod needed for /enterprise
            _p.waitFor()
        } catch (e: IOException) {
        } catch (e: InterruptedException) {
        }
    }
}