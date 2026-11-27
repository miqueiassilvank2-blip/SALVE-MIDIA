package com.salvemidia.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.ContentValues
import android.net.Uri
import android.content.pm.ServiceInfo
import android.os.Environment
import android.provider.MediaStore
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
import java.io.FileInputStream
import java.io.FileOutputStream

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
            var keepFinalNotification = false
            try {
                val dir = File(getExternalFilesDir(null), "tmp").apply { mkdirs() }
                val clients = listOf("android_vr", "tv", "ios", "web_safari", "mweb")
                var lastError: Exception? = null
                var downloaded = false

                fun makeRequest(client: String?, includeFormat: Boolean): YoutubeDLRequest {
                    val req = YoutubeDLRequest(url)
                    if (client != null) {
                        req.addOption("--extractor-args", "youtube:player_client=$client")
                    }
                    req.addOption("--no-check-certificates")
                    req.addOption("--retries", "3")
                    req.addOption("-o", dir.absolutePath + "/%(title).80s.%(ext)s")
                    req.addOption("--no-playlist")
                    if (audio) {
                        if (includeFormat) req.addOption("-f", "ba/bestaudio/best")
                        req.addOption("-x")
                        req.addOption("--audio-format", "mp3")
                        req.addOption("--embed-thumbnail")
                        req.addOption("--convert-thumbnails", "jpg")
                        req.addOption("--add-metadata")
                    } else {
                        if (includeFormat) {
                            req.addOption("-f", "bv*[height<=$quality]+ba/b[height<=$quality]/bv*+ba/b/best")
                        }
                        req.addOption("--merge-output-format", "mp4")
                    }
                    return req
                }

                for (client in clients) {
                    try {
                        val req = makeRequest(client, true)
                        YoutubeDL.getInstance().execute(req, "dl$startId") { progress, _, _ ->
                            nm.notify(notifId, buildNotification(title, progress.toInt().coerceIn(0, 100), "${progress.toInt()}%"))
                        }
                        downloaded = true
                        break
                    } catch (e: Exception) {
                        lastError = e
                    }
                }

                if (!downloaded) {
                    try {
                        val req = makeRequest(null, false)
                        YoutubeDL.getInstance().execute(req, "dl$startId") { progress, _, _ ->
                            nm.notify(notifId, buildNotification(title, progress.toInt().coerceIn(0, 100), "${progress.toInt()}%"))
                        }
                        downloaded = true
                    } catch (e: Exception) {
                        lastError = e
                    }
                }

                if (!downloaded) {
                    val errorMessage = lastError?.toString() ?: "Falha no download"
                    File(getExternalFilesDir(null), "erro_download.txt").writeText(errorMessage)
                    val lastUsefulLine = errorMessage.lineSequence()
                        .map { it.trim() }
                        .lastOrNull { it.isNotEmpty() && !it.startsWith("WARNING") }
                        ?: "Falha no download"
                    toast("Erro: ${lastUsefulLine.take(150)}")
                    return@launch
                }

                val output = dir.listFiles()
                    ?.filter { file ->
                        file.isFile && !file.name.endsWith(".part", true) &&
                            !file.name.endsWith(".ytdl", true) &&
                            !file.name.endsWith(".jpg", true) &&
                            !file.name.endsWith(".webp", true)
                    }
                    ?.maxByOrNull { it.lastModified() }
                if (output == null) {
                    toast("Baixou, mas não foi possível salvar na galeria")
                    return@launch
                }

                val savedUri = try {
                    saveToPublicFolder(output, audio)
                } catch (_: Throwable) {
                    null
                }
                if (savedUri == null) {
                    toast("Baixou, mas não foi possível salvar na galeria")
                } else {
                    output.delete()
                    toast(if (audio) "Salvo em Músicas/SalveMidia" else "Salvo em Filmes/SalveMidia")
                }

                try {
                    AppDatabase.getDatabase(applicationContext).historyDao()
                        .insert(HistoryItem(title = title, platform = detectPlatform(url), url = url))
                } catch (_: Throwable) {
                }
                val finalNotification = buildFinalNotification(title, savedUri, audio)
                stopForeground(STOP_FOREGROUND_DETACH)
                nm.notify(notifId, finalNotification)
                keepFinalNotification = true
            } catch (e: Exception) {
                toast("Erro: ${e.message?.take(150) ?: "falha no download"}")
            } finally {
                if (!keepFinalNotification) stopForeground(STOP_FOREGROUND_REMOVE)
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
            val ch = NotificationChannel("downloads", "Downloads", NotificationManager.IMPORTANCE_DEFAULT)
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

    private fun buildFinalNotification(title: String, uri: Uri?, audio: Boolean): Notification {
        val builder = NotificationCompat.Builder(this, "downloads")
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("Download concluído: $title")
            .setContentText(if (uri != null) "Toque para abrir" else "Arquivo salvo na pasta privada")
            .setProgress(0, 0, false)
            .setOngoing(false)
            .setAutoCancel(true)
        if (uri != null) {
            val openIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, if (audio) contentResolver.getType(uri) ?: "audio/*" else "video/*")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val pendingIntent = PendingIntent.getActivity(
                this, uri.hashCode(), openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.setContentIntent(pendingIntent)
        }
        return builder.build()
    }

    private fun saveToPublicFolder(source: File, audio: Boolean): Uri? {
        val relativeDirectory = if (audio) Environment.DIRECTORY_MUSIC else Environment.DIRECTORY_MOVIES
        val folderName = "$relativeDirectory/SalveMidia"
        if (Build.VERSION.SDK_INT >= 29) {
            val extension = source.extension.lowercase()
            val mimeType = when (extension) {
                "mp3" -> "audio/mpeg"
                "m4a" -> "audio/mp4"
                "mp4" -> "video/mp4"
                else -> if (audio) "audio/*" else "video/*"
            }
            val collection = if (audio) MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                else MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, source.name)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, folderName)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
            val uri = contentResolver.insert(collection, values) ?: return null
            try {
                contentResolver.openOutputStream(uri)?.use { output ->
                    FileInputStream(source).use { input -> input.copyTo(output) }
                } ?: throw IllegalStateException("Não foi possível abrir destino")
                val ready = ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }
                contentResolver.update(uri, ready, null, null)
                return uri
            } catch (error: Throwable) {
                contentResolver.delete(uri, null, null)
                throw error
            }
        }
        val publicDirectory = File(
            Environment.getExternalStoragePublicDirectory(relativeDirectory), "SalveMidia"
        ).apply { mkdirs() }
        val destination = File(publicDirectory, source.name)
        FileInputStream(source).use { input ->
            FileOutputStream(destination).use { output -> input.copyTo(output) }
        }
        val mimeType = if (audio) {
            if (source.extension.equals("m4a", true)) "audio/mp4" else "audio/mpeg"
        } else "video/mp4"
        val collection = if (audio) MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            else MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DATA, destination.absolutePath)
            put(MediaStore.MediaColumns.DISPLAY_NAME, destination.name)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
        }
        return contentResolver.insert(collection, values)
            ?: throw IllegalStateException("Não foi possível registrar o arquivo")
    }

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
