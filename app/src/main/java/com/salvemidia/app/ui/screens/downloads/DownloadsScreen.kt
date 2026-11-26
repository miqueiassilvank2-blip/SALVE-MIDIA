package com.salvemidia.app.ui.screens.downloads

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
import java.io.File

@Composable
fun DownloadsScreen() {
    val context = LocalContext.current
    val dir = remember { File(context.getExternalFilesDir(null), "SalveMidia").apply { mkdirs() } }
    var files by remember { mutableStateOf(dir.listFiles()?.sortedByDescending { it.lastModified() } ?: emptyList()) }

    Column(Modifier.fillMaxSize().background(Color(0xFF0D0D14)).padding(16.dp)) {
        Text("Downloads", color = Color.White, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        if (files.isEmpty()) {
            Text("Nenhum arquivo baixado ainda.", color = Color(0xFF8A8A99))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(files) { f ->
                    Row(
                        Modifier.fillMaxWidth().background(Color(0xFF16161F), RoundedCornerShape(16.dp)).padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(f.name, color = Color.White, maxLines = 2)
                            Text("%.1f MB".format(f.length() / 1048576.0), color = Color(0xFF8A8A99))
                        }
                        TextButton(onClick = {
                            f.delete()
                            files = dir.listFiles()?.sortedByDescending { it.lastModified() } ?: emptyList()
                        }) { Text("Excluir", color = Color(0xFF7C3AED)) }
                    }
                }
            }
        }
    }
}
