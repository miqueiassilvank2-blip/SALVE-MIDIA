package com.salvemidia.app.ui.screens.downloads

import android.content.ContentUris
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private data class PublicDownload(val uri: Uri, val name: String, val size: Long, val mimeType: String?, val dateAdded: Long)

@Composable
fun DownloadsScreen() {
    val context = LocalContext.current
    var files by remember { mutableStateOf(emptyList<PublicDownload>()) }
    val refreshFiles = {
        val resolver = context.contentResolver
        val result = mutableListOf<PublicDownload>()
        listOf(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI to true,
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI to false
        ).forEach { (collection, video) ->
            val projection = if (Build.VERSION.SDK_INT >= 29) arrayOf(
                MediaStore.MediaColumns._ID,
                MediaStore.MediaColumns.DISPLAY_NAME,
                MediaStore.MediaColumns.SIZE,
                MediaStore.MediaColumns.MIME_TYPE,
                MediaStore.MediaColumns.DATE_ADDED,
                MediaStore.MediaColumns.RELATIVE_PATH
            ) else arrayOf(
                MediaStore.MediaColumns._ID,
                MediaStore.MediaColumns.DISPLAY_NAME,
                MediaStore.MediaColumns.SIZE,
                MediaStore.MediaColumns.MIME_TYPE,
                MediaStore.MediaColumns.DATE_ADDED,
                MediaStore.MediaColumns.DATA
            )
            val pathColumn = if (Build.VERSION.SDK_INT >= 29) MediaStore.MediaColumns.RELATIVE_PATH else MediaStore.MediaColumns.DATA
            val selection = "$pathColumn LIKE ?"
            val pathPattern = if (Build.VERSION.SDK_INT >= 29) "%SalveMidia%" else "%/SalveMidia/%"
            try {
                resolver.query(collection, projection, selection, arrayOf(pathPattern), "${MediaStore.MediaColumns.DATE_ADDED} DESC")?.use { cursor ->
                    val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                    val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                    val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
                    val mimeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)
                    val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED)
                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idColumn)
                        val itemUri = ContentUris.withAppendedId(collection, id)
                        result += PublicDownload(
                            itemUri,
                            cursor.getString(nameColumn) ?: "Download",
                            cursor.getLong(sizeColumn),
                            if (cursor.isNull(mimeColumn)) if (video) "video/*" else "audio/*" else cursor.getString(mimeColumn),
                            cursor.getLong(dateColumn)
                        )
                    }
                }
            } catch (_: Exception) {
            }
        }
        result.sortedByDescending { it.dateAdded }
    }
    LaunchedEffect(Unit) { files = refreshFiles() }

    Column(Modifier.fillMaxSize().background(Color(0xFF0D0D14)).padding(16.dp)) {
        Text("Downloads", color = Color.White, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        if (files.isEmpty()) {
            Text("Nenhum arquivo baixado ainda.", color = Color(0xFF8A8A99))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(files, key = { it.uri.toString() }) { file ->
                    Row(
                        Modifier.fillMaxWidth().background(Color(0xFF16161F), RoundedCornerShape(16.dp)).padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(file.name, color = Color.White, maxLines = 2)
                            Text("%.1f MB".format(file.size / 1048576.0), color = Color(0xFF8A8A99))
                        }
                        TextButton(onClick = {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(file.uri, file.mimeType ?: "*/*")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            runCatching { context.startActivity(intent) }
                        }) { Text("Abrir", color = Color(0xFF7C3AED)) }
                        TextButton(onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = file.mimeType ?: "*/*"
                                putExtra(Intent.EXTRA_STREAM, file.uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, "Compartilhar arquivo"))
                        }) { Text("Compartilhar", color = Color(0xFF7C3AED)) }
                        TextButton(onClick = {
                            runCatching { context.contentResolver.delete(file.uri, null, null) }
                            files = refreshFiles()
                        }) { Text("Excluir", color = Color(0xFF7C3AED)) }
                    }
                }
            }
        }
    }
}
