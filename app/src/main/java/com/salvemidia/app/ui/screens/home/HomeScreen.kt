package com.salvemidia.app.ui.screens.home

import android.Manifest
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.salvemidia.app.service.DownloadService
import com.salvemidia.app.ui.components.AppHeader
import com.salvemidia.app.ui.components.NetworkGrid
import com.salvemidia.app.ui.navigation.Screen
import com.salvemidia.app.ui.theme.Background
import com.salvemidia.app.ui.theme.CardBackground
import com.salvemidia.app.ui.theme.PurpleGradientEnd
import com.salvemidia.app.ui.theme.PurpleMain
import com.salvemidia.app.ui.theme.TextSecondary
import com.salvemidia.app.ui.theme.TextWhite

@Composable
fun HomeScreen(navController: NavController, viewModel: HomeViewModel = viewModel()) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Buscar", "Por Link")
    val state by viewModel.state.collectAsState()
    var pending by remember { mutableStateOf<Pair<String, String>?>(null) }

    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33) permLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    pending?.let { (url, title) ->
        DownloadDialog(
            onDismiss = { pending = null },
            onChoose = { audio, quality ->
                val i = Intent(context, DownloadService::class.java)
                    .putExtra("url", url).putExtra("title", title)
                    .putExtra("audio", audio).putExtra("quality", quality)
                ContextCompat.startForegroundService(context, i)
                Toast.makeText(context, "Download iniciado", Toast.LENGTH_SHORT).show()
                pending = null
            }
        )
    }

    Column(Modifier.fillMaxSize().background(Background)) {
        AppHeader(onSettingsClick = { navController.navigate(Screen.Settings.route) })

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Background,
            contentColor = TextWhite,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(Modifier.tabIndicatorOffset(tabPositions[selectedTab]), color = PurpleMain)
            },
            divider = {}
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            color = if (selectedTab == index) TextWhite else TextSecondary,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            item {
                Spacer(Modifier.height(16.dp))
                if (selectedTab == 0) SearchSection(viewModel)
                else LinkSection { link ->
                    if (link.isBlank()) Toast.makeText(context, "Cole um link primeiro", Toast.LENGTH_SHORT).show()
                    else pending = link to link
                }
            }

            if (selectedTab == 0) {
                when (val s = state) {
                    is HomeState.Loading -> item {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = PurpleMain)
                        }
                    }
                    is HomeState.Success -> items(s.results) { r ->
                        SearchResultItem(r) {
                            val u = r.webpage_url ?: r.url
                            if (u != null) pending = u to (r.title ?: "Vídeo")
                        }
                    }
                    is HomeState.Error -> item {
                        Text(s.message, color = Color.Red, modifier = Modifier.padding(16.dp))
                    }
                    else -> {}
                }
            }

            item {
                NetworkGrid()
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun DownloadDialog(onDismiss: () -> Unit, onChoose: (Boolean, Int) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackground,
        title = { Text("Escolha o formato", color = TextWhite) },
        text = {
            Column {
                TextButton(onClick = { onChoose(false, 360) }) { Text("Vídeo MP4 360p", color = PurpleGradientEnd) }
                TextButton(onClick = { onChoose(false, 720) }) { Text("Vídeo MP4 720p", color = PurpleGradientEnd) }
                TextButton(onClick = { onChoose(false, 1080) }) { Text("Vídeo MP4 1080p", color = PurpleGradientEnd) }
                TextButton(onClick = { onChoose(true, 0) }) { Text("Áudio MP3", color = PurpleGradientEnd) }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", color = TextSecondary) } }
    )
}

@Composable
fun SearchSection(viewModel: HomeViewModel) {
    var query by remember { mutableStateOf("") }
    Column {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Digite o nome do vídeo...", color = TextSecondary) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PurpleMain,
                unfocusedBorderColor = CardBackground,
                focusedContainerColor = CardBackground,
                unfocusedContainerColor = CardBackground,
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite
            ),
            singleLine = true
        )
        Spacer(Modifier.height(8.dp))
        GradientButton("Pesquisar") { viewModel.search(query) }
    }
}

@Composable
fun LinkSection(onDownload: (String) -> Unit) {
    var link by remember { mutableStateOf("") }
    Column {
        OutlinedTextField(
            value = link,
            onValueChange = { link = it },
            placeholder = { Text("Cole o link do vídeo aqui...", color = TextSecondary) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PurpleMain,
                unfocusedBorderColor = CardBackground,
                focusedContainerColor = CardBackground,
                unfocusedContainerColor = CardBackground,
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite
            ),
            singleLine = true
        )
        Spacer(Modifier.height(8.dp))
        GradientButton("Baixar por Link") { onDownload(link.trim()) }
    }
}

@Composable
fun GradientButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(PurpleMain, PurpleGradientEnd)), RoundedCornerShape(12.dp))
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text, color = TextWhite, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SearchResultItem(result: SearchResult, onDownload: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = result.thumbnail,
            contentDescription = null,
            modifier = Modifier.size(100.dp, 60.dp).clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(result.title ?: "Sem título", color = TextWhite, fontWeight = FontWeight.Bold, maxLines = 2, fontSize = 14.sp)
            Text(formatDuration(result.duration), color = TextSecondary, fontSize = 12.sp)
        }
        Button(
            onClick = onDownload,
            colors = ButtonDefaults.buttonColors(containerColor = PurpleMain),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 12.dp)
        ) { Text("Baixar", fontSize = 12.sp) }
    }
}

fun formatDuration(seconds: Double?): String {
    if (seconds == null) return "00:00"
    val s = seconds.toInt()
    val m = s / 60
    val h = m / 60
    return if (h > 0) String.format("%d:%02d:%02d", h, m % 60, s % 60)
    else String.format("%02d:%02d", m, s % 60)
}
