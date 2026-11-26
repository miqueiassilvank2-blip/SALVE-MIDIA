package com.salvemidia.app

import android.app.Application
import android.util.Log
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import java.io.File

class SalveMidiaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val old = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            try {
                File(filesDir, "crash.txt").writeText(Log.getStackTraceString(e))
            } catch (_: Throwable) {}
            old?.uncaughtException(t, e)
        }
        try {
            YoutubeDL.getInstance().init(this)
            FFmpeg.getInstance().init(this)
            Thread {
                try {
                    YoutubeDL.getInstance().updateYoutubeDL(this, YoutubeDL.UpdateChannel.STABLE)
                } catch (e: Throwable) {
                    Log.e("SalveMidiaApp", "falha ao atualizar youtubedl", e)
                }
            }.start()
        } catch (e: Throwable) {
            Log.e("SalveMidiaApp", "falha ao iniciar youtubedl/ffmpeg", e)
        }
    }
}
