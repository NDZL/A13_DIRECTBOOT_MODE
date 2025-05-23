package com.ndzl.targetelevator

import android.content.Context
import android.media.MediaRecorder
import android.util.Log
import java.io.IOException

// AudioManager provides access to volume and ringer mode control.
// The AudioRecord class manages the audio resources for Java applications to record audio from the audio input hardware of the platform.
// MediaRecorder Used to record audio and video.
// https://stackoverflow.com/questions/5886872/android-audiorecord-vs-mediarecorder-for-recording-audio

private const val LOG_TAG = "MicManager"

class MicManager(/*context: Context*/) {

    //private var tempRecFile: String = "/enterprise/usr/persist/mic-audio-temp.3gp"
    private var fileName: String = "/enterprise/usr/persist/mic-audio.3gp"

    private var recorder: MediaRecorder? = null

    private var isRecOn: Boolean = false
    public fun isRecording(): Boolean {
        return isRecOn
    }

    //private val ctx = context



    public fun startRecording() {
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC )
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(fileName)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            chmodFile(fileName)

            try {
                prepare()
            } catch (e: IOException) {
                Log.e(LOG_TAG, "prepare() failed")
            }

            start()
            Log.i(LOG_TAG, "AUDIO RECORDING STARTED")
            isRecOn = true
        }
    }

    public fun stopRecording() {
        try{
            recorder?.apply {
                stop()
                release()
                isRecOn = false
                chmodFile(fileName)
                //renameFile(tempRecFile, fileName)
                Log.i(LOG_TAG, "AUDIO RECORDING STOPPED")
        }
        } catch (e: Exception){
            Log.e(LOG_TAG, "stop() failed")
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

    private fun renameFile(sourcePath: String, destPath:String){
        try {
            val _p =
                Runtime.getRuntime().exec("mv $sourcePath $destPath")
            _p.waitFor()
        } catch (e: IOException) {
        } catch (e: InterruptedException) {
        }
    }
}