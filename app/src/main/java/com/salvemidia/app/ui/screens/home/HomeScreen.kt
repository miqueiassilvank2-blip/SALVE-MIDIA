package com.salvemidia.app.ui.screens.home

import android.Manifest
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
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
    val searchFocus = remember { FocusRequester() }
    val linkFocus = remember { FocusRequester() }
    val state by viewModel.state.collectAsState()
    var pending by remember { mutableStateOf<Pair<String, String>?>(null) }
    var link by remember { mutableStateOf("") }
    var lastAutoPastedLink by remember { mutableStateOf<String?>(null) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val readClipboardFn: () -> Unit = {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clipText = clipboard?.primaryClip?.getItemAt(0)?.coerceToText(context)?.toString()?.trim()
        if (
            link.isEmpty() &&
            clipText.isValidHttpLink() &&
            clipText != lastAutoPastedLink
        ) {
            link = clipText
            lastAutoPastedLink = clipText
            selectedTab = 1
            Toast.makeText(context, "Link colado automaticamente", Toast.LENGTH_SHORT).show()
        }
    }

    val readClipboard by rememberUpdatedState(readClipboardFn)

    DisposableEffect(lifecycleOwner) {
        readClipboard()
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) readClipboard()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33) permLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(500)
        readClipboard()
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

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp)).background(CardBackground).padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("Buscar", "Por Link").forEachIndexed { index, label ->
                val active = selectedTab == index
                Row(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(16.dp))
                        .background(if (active) PurpleMain else Color.Transparent)
                        .clickable {
                            selectedTab = index
                            if (index == 0) searchFocus.requestFocus() else linkFocus.requestFocus()
                        }.padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(if (index == 0) Icons.Default.Search else Icons.Default.Link, null, tint = TextWhite, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(label, color = TextWhite, fontWeight = FontWeight.Bold)
                }
            }
        }

        LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            item {
                Spacer(Modifier.height(24.dp))
                SearchSection(viewModel, searchFocus)
                Row(Modifier.fillMaxWidth().padding(vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    HorizontalDivider(Modifier.weight(1f), color = TextSecondary.copy(alpha = 0.35f))
                    Text("Ou", color = TextSecondary, modifier = Modifier.padding(horizontal = 12.dp))
                    HorizontalDivider(Modifier.weight(1f), color = TextSecondary.copy(alpha = 0.35f))
                }
                LinkSection(linkFocus, link, onLinkChange = { link = it }, onDownload = { enteredLink ->
                    if (enteredLink.isBlank()) Toast.makeText(context, "Cole um link primeiro", Toast.LENGTH_SHORT).show()
                    else pending = enteredLink to enteredLink
                })
            }
            if (selectedTab == 0) {
                when (val s = state) {
                    is HomeState.Loading -> item {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = PurpleMain)
                        }
                    }
                    is HomeState.Success -> items(s.results) { r ->
                        val u = r.webpage_url ?: r.url
                        SearchResultItem(r,
                            onDownload = { if (u != null) pending = u to (r.title ?: "Vídeo") }
                        )
                    }
                    is HomeState.Error -> item { Text(s.message, color = Color.Red, modifier = Modifier.padding(16.dp)) }
                    else -> {}
                }
            }
            item { NetworkGrid(); Spacer(Modifier.height(16.dp)) }
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
fun SearchSection(viewModel: HomeViewModel, focusRequester: FocusRequester) {
    var query by remember { mutableStateOf("") }
    Column {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Digite o nome do vídeo...", color = TextSecondary) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PurpleMain, unfocusedBorderColor = TextSecondary.copy(alpha = 0.2f), focusedContainerColor = CardBackground, unfocusedContainerColor = CardBackground, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
            singleLine = true
        )
        Spacer(Modifier.height(8.dp))
        GradientButton("Pesquisar", Icons.Default.Search) { viewModel.search(query) }
    }
}

@Composable
fun LinkSection(
    focusRequester: FocusRequester,
    link: String,
    onLinkChange: (String) -> Unit,
    onDownload: (String) -> Unit
) {
    Column {
        OutlinedTextField(
            value = link,
            onValueChange = onLinkChange,
            placeholder = { Text("Cole o link do vídeo aqui...", color = TextSecondary) },
            leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, tint = TextSecondary) },
            trailingIcon = {
                if (link.isNotEmpty()) {
                    IconButton(onClick = { onLinkChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Limpar link", tint = TextSecondary)
                    }
                } else {
                    val context = LocalContext.current
                    TextButton(onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                        val copied = clipboard?.primaryClip?.takeIf { it.itemCount > 0 }
                            ?.getItemAt(0)?.coerceToText(context)?.toString()?.trim()
                        val validLink = copied?.takeIf { it.isValidHttpLink() }
                        if (validLink != null) onLinkChange(validLink) else
                            Toast.makeText(context, "Nenhum link copiado", Toast.LENGTH_SHORT).show()
                    }) { Text("Colar", color = PurpleGradientEnd) }
                }
            },
            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PurpleMain, unfocusedBorderColor = TextSecondary.copy(alpha = 0.2f), focusedContainerColor = CardBackground, unfocusedContainerColor = CardBackground, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite),
            singleLine = true
        )
        Spacer(Modifier.height(8.dp))
        GradientButton("Baixar por Link", Icons.Default.Download) { onDownload(link.trim()) }
    }
}

@Composable
fun GradientButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent), contentPadding = PaddingValues()) {
        Row(modifier = Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(PurpleMain, PurpleGradientEnd)), RoundedCornerShape(16.dp)), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = TextWhite, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
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
            model = result.thumbnail ?: result.id?.let { "https://i.ytimg.com/vi/$it/hqdefault.jpg" },
            contentDescription = null,
            modifier = Modifier.size(120.dp, 68.dp).clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(result.title ?: "Sem título", color = TextWhite, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis, fontSize = 14.sp, modifier = Modifier.weight(1f))
            Text(formatDuration(result.duration), color = TextSecondary, fontSize = 12.sp)
        }
        Spacer(Modifier.width(8.dp))
        Button(onClick = onDownload, modifier = Modifier.height(36.dp), colors = ButtonDefaults.buttonColors(containerColor = PurpleMain), shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(horizontal = 10.dp)) {
            Text("Baixar", fontSize = 13.sp)
        }
    }
}

private fun String?.isValidHttpLink(): Boolean {
    if (this.isNullOrBlank()) return false
    val uri = android.net.Uri.parse(this)
    return (uri.scheme.equals("http", ignoreCase = true) || uri.scheme.equals("https", ignoreCase = true)) &&
        !uri.host.isNullOrBlank()
}

fun formatDuration(seconds: Double?): String {
    if (seconds == null) return "00:00"
    val s = seconds.toInt()
    val m = s / 60
    val h = m / 60
    return if (h > 0) String.format("%d:%02d:%02d", h, m % 60, s % 60)
    else String.format("%02d:%02d", m, s % 60)
}
