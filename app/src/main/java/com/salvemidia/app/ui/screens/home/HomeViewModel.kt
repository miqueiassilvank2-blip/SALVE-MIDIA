package com.salvemidia.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
