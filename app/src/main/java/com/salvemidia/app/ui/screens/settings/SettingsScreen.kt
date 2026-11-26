package com.salvemidia.app.ui.screens.settings

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("settings", Context.MODE_PRIVATE) }
    var quality by remember { mutableStateOf(prefs.getString("quality", "720p") ?: "720p") }
    var format by remember { mutableStateOf(prefs.getString("format", "MP4") ?: "MP4") }

    Column(Modifier.fillMaxSize().background(Color(0xFF0D0D14)).padding(16.dp)) {
        Text("Configurações", color = Color.White, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        Text("Qualidade padrão", color = Color(0xFF8A8A99))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("360p", "720p", "1080p").forEach { q ->
                FilterChip(selected = quality == q, onClick = {
                    quality = q; prefs.edit().putString("quality", q).apply()
                }, label = { Text(q) })
            }
        }
        Spacer(Modifier.height(16.dp))
        Text("Formato padrão", color = Color(0xFF8A8A99))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("MP4", "MP3").forEach { f ->
                FilterChip(selected = format == f, onClick = {
                    format = f; prefs.edit().putString("format", f).apply()
                }, label = { Text(f) })
            }
        }
    }
}
