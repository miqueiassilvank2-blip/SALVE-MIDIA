package com.salvemidia.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import org.json.JSONObject
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class SearchResult(
    val id: String? = null,
    val title: String? = null,
    val thumbnail: String? = null,
    val duration: Double? = null,
    val url: String? = null,
    val webpage_url: String? = null
)

sealed class HomeState {
    object Idle : HomeState()
    object Loading : HomeState()
    data class Success(val results: List<SearchResult>) : HomeState()
    data class Error(val message: String) : HomeState()
}

class HomeViewModel : ViewModel() {
    private val _state = MutableStateFlow<HomeState>(HomeState.Idle)
    val state: StateFlow<HomeState> = _state

    data class LinkInfo(val title: String?, val thumbnail: String?, val duration: Double?, val uploader: String?)
    sealed class LinkState {
        object Idle : LinkState()
        object Loading : LinkState()
        data class Success(val info: LinkInfo) : LinkState()
        object Error : LinkState()
    }
    private val _linkState = MutableStateFlow<LinkState>(LinkState.Idle)
    val linkState: StateFlow<LinkState> = _linkState
    private var linkInfoJob: Job? = null

    fun loadLinkInfo(url: String) {
        linkInfoJob?.cancel()
        linkInfoJob = viewModelScope.launch {
            _linkState.value = LinkState.Loading
            try {
                val info = withContext(Dispatchers.IO) {
                    val isYoutube = runCatching { android.net.Uri.parse(url).host.orEmpty().lowercase() }
                        .getOrDefault("").let { it == "youtube.com" || it.endsWith(".youtube.com") || it == "youtu.be" || it.endsWith(".youtu.be") }
                    val clients = if (isYoutube) listOf<String?>(null, "android_vr", "tv", "ios", "web_safari") else listOf(null)
                    var lastError: Throwable? = null
                    for (client in clients) {
                        currentCoroutineContext().ensureActive()
                        try {
                            val request = YoutubeDLRequest(url)
                            request.addOption("--dump-single-json")
                            request.addOption("--no-playlist")
                            request.addOption("--skip-download")
                            if (client != null) request.addOption("--extractor-args", "youtube:player_client=$client")
                            val jsonObject = JSONObject(YoutubeDL.getInstance().execute(request).out)
                            return@withContext LinkInfo(
                                jsonObject.optString("title").takeIf { it.isNotBlank() },
                                jsonObject.optString("thumbnail").takeIf { it.isNotBlank() },
                                if (jsonObject.has("duration") && !jsonObject.isNull("duration")) jsonObject.optDouble("duration").takeIf { it.isFinite() } else null,
                                (jsonObject.optString("uploader").takeIf { it.isNotBlank() }
                                    ?: jsonObject.optString("channel").takeIf { it.isNotBlank() })
                            )
                        } catch (error: Throwable) {
                            if (error is CancellationException) throw error
                            lastError = error
                        }
                    }
                    throw lastError ?: IllegalStateException("Falha ao ler detalhes")
                }
                _linkState.value = LinkState.Success(info)
            } catch (error: Throwable) {
                if (error is CancellationException) throw error
                _linkState.value = LinkState.Error
            }
        }
    }

    fun clearLinkInfo() {
        linkInfoJob?.cancel()
        linkInfoJob = null
        _linkState.value = LinkState.Idle
    }

    private val json = Json { ignoreUnknownKeys = true }

    fun search(query: String) {
        if (query.isBlank()) return
        
        viewModelScope.launch {
            _state.value = HomeState.Loading
            try {
                val results = withContext(Dispatchers.IO) {
                    val request = YoutubeDLRequest("ytsearch10:$query")
                    request.addOption("--dump-json")
                    request.addOption("--flat-playlist")
                    val response = YoutubeDL.getInstance().execute(request)
                    response.out.lines()
                        .filter { it.isNotBlank() }
                        .map { json.decodeFromString<SearchResult>(it) }
                }
                _state.value = HomeState.Success(results)
            } catch (e: Exception) {
                _state.value = HomeState.Error(e.message ?: "Erro desconhecido")
            }
        }
    }

    fun getStreamUrl(url: String, onResult: (String?, String?) -> Unit) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                var lastError: String? = null
                val playerClients = listOf("android_vr,web_safari", "tv", "ios")
                for (playerClient in playerClients) {
                    try {
                        val request = YoutubeDLRequest(url)
                        request.addOption("-g")
                        request.addOption("-f", "best[height<=360][ext=mp4][protocol^=https]/best[height<=360][ext=mp4]/best[height<=360]/best")
                        request.addOption("--no-playlist")
                        request.addOption("--extractor-args", "youtube:player_client=$playerClient")
                        val streamUrl = YoutubeDL.getInstance().execute(request).out.lines()
                            .firstOrNull { it.isNotBlank() }
                        if (streamUrl != null) return@withContext streamUrl to null
                        lastError = "Nenhuma URL de transmissão foi retornada."
                    } catch (e: Exception) {
                        lastError = e.message.orEmpty()
                    }
                }
                val message = lastError.orEmpty().lineSequence()
                    .filterNot { it.startsWith("WARNING") }
                    .joinToString("\n")
                    .takeLast(200)
                null to message.ifBlank { "Erro desconhecido" }
            }
            onResult(result.first, result.second)
        }
    }
}
