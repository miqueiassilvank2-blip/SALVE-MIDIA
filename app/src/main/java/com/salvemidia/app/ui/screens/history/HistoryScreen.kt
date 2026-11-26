package com.salvemidia.app.ui.screens.history

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
import com.salvemidia.app.data.AppDatabase
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen() {
    val context = LocalContext.current
    val dao = remember { AppDatabase.getDatabase(context).historyDao() }
    val items by dao.getAll().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    val fmt = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    Column(Modifier.fillMaxSize().background(Color(0xFF0D0D14)).padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Histórico", color = Color.White, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
            if (items.isNotEmpty()) {
                TextButton(onClick = { scope.launch { dao.deleteAll() } }) {
                    Text("Limpar", color = Color(0xFF7C3AED))
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        if (items.isEmpty()) {
            Text("Nenhum download no histórico.", color = Color(0xFF8A8A99))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(items) { h ->
                    Column(
                        Modifier.fillMaxWidth()
                            .background(Color(0xFF16161F), RoundedCornerShape(16.dp))
                            .padding(12.dp)
                    ) {
                        Text(h.title, color = Color.White, maxLines = 2)
                        Text("${h.platform} • ${fmt.format(Date(h.date))}", color = Color(0xFF8A8A99))
                    }
                }
            }
        }
    }
}
