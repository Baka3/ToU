package com.example.tou

import android.media.MediaRecorder
import android.os.Build
import java.io.File

class VoiceRecorderHelper(private val context: android.content.Context) {
    private var recorder: MediaRecorder? = null
    private var outputFile: String = ""

    fun startRecording(): String {
        outputFile = File(context.cacheDir, "voice_${System.currentTimeMillis()}.m4a").absolutePath

        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }

        recorder?.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(outputFile)
            prepare()
            start()
        }
        return outputFile
    }

    fun stopRecording(): String {
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
        return outputFile
    }
}