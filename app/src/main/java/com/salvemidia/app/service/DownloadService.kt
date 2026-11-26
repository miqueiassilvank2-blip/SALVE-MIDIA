package com.salvemidia.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.salvemidia.app.data.AppDatabase
import com.salvemidia.app.data.HistoryItem
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File

class DownloadService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val url = intent?.getStringExtra("url") ?: return START_NOT_STICKY
        val title = intent.getStringExtra("title") ?: "Download"
        val audio = intent.getBooleanExtra("audio", false)
        val quality = intent.getIntExtra("quality", 720)
        val notifId = 1000 + startId

        createChannel()
        val first = buildNotification(title, 0, "Iniciando...")
        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(notifId, first, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(notifId, first)
        }

        scope.launch {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            try {
                val dir = File(getExternalFilesDir(null), "SalveMidia").apply { mkdirs() }
                val req = YoutubeDLRequest(url)
                req.addOption("--extractor-args", "youtube:player_client=android_vr,web_safari")
                req.addOption("-o", dir.absolutePath + "/%(title).80s.%(ext)s")
                req.addOption("--no-playlist")
                if (audio) {
                    req.addOption("-x")
                    req.addOption("--audio-format", "mp3")
                    req.addOption("--embed-thumbnail")
                    req.addOption("--convert-thumbnails", "jpg")
                    req.addOption("--add-metadata")
                } else {
                    req.addOption("-f", "bestvideo[height<=$quality]+bestaudio/best[height<=$quality]/best")
                    req.addOption("--merge-output-format", "mp4")
                }
                YoutubeDL.getInstance().execute(req, "dl$startId") { progress, _, _ ->
                    nm.notify(notifId, buildNotification(title, progress.toInt().coerceIn(0, 100), "${progress.toInt()}%"))
                }
                AppDatabase.getDatabase(applicationContext).historyDao()
                    .insert(HistoryItem(title = title, platform = detectPlatform(url), url = url))
                toast("Download concluído: $title")
            } catch (e: Exception) {
                toast("Erro: ${e.message?.take(120) ?: "falha no download"}")
            } finally {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf(startId)
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun toast(msg: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(applicationContext, msg, Toast.LENGTH_LONG).show()
        }
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val ch = NotificationChannel("downloads", "Downloads", NotificationManager.IMPORTANCE_LOW)
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(ch)
        }
    }

    private fun buildNotification(title: String, progress: Int, text: String): Notification =
        NotificationCompat.Builder(this, "downloads")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle(title)
            .setContentText(text)
            .setProgress(100, progress, progress == 0)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()

    companion object {
        fun detectPlatform(url: String): String = when {
            "youtu" in url -> "YouTube"
            "tiktok" in url -> "TikTok"
            "instagram" in url -> "Instagram"
            "facebook" in url || "fb.watch" in url -> "Facebook"
            "twitter" in url || "x.com" in url -> "X (Twitter)"
            "kwai" in url -> "Kwai"
            "vimeo" in url -> "Vimeo"
            else -> "Outro"
        }
    }
}
