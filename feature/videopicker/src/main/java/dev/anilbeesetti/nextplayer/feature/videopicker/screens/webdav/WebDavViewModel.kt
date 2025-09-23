package dev.anilbeesetti.nextplayer.feature.videopicker.screens.webdav

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.anilbeesetti.nextplayer.core.model.WebDavServer
import dev.anilbeesetti.nextplayer.core.model.WebDavHistory
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.core.net.toUri

data class TestConnectionResult(
    val isSuccess: Boolean,
    val message: String,
)

@HiltViewModel
class WebDavViewModel @Inject constructor(
    private val webDavRepository: dev.anilbeesetti.nextplayer.core.data.repository.WebDavRepository,
) : ViewModel() {

    val servers: StateFlow<List<WebDavServer>> = webDavRepository.getAllServers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _testConnectionResult = MutableStateFlow<TestConnectionResult?>(null)
    val testConnectionResult: StateFlow<TestConnectionResult?> = _testConnectionResult.asStateFlow()

    private val _showHistory = MutableStateFlow(false)
    val showHistory: StateFlow<Boolean> = _showHistory.asStateFlow()

    val allHistory: StateFlow<List<WebDavHistory>> = webDavRepository.getAllHistory()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    val filteredServers = combine(servers, searchQuery) { serverList, query ->
        if (query.isBlank()) {
            serverList
        } else {
            serverList.filter { server ->
                server.name.contains(query, ignoreCase = true) ||
                        server.url.contains(query, ignoreCase = true)
            }
        }
    }

    init {
        loadServers()
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addServer(server: WebDavServer) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                webDavRepository.addServer(server)
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateServer(server: WebDavServer) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                webDavRepository.updateServer(server)
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteServer(serverId: String) {
        viewModelScope.launch {
            try {
                webDavRepository.deleteServer(serverId)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun testConnection(server: WebDavServer) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _testConnectionResult.value = null

                val result = webDavRepository.testConnection(server)
                result.onSuccess { isConnected ->
                    _testConnectionResult.value = TestConnectionResult(
                        isSuccess = isConnected,
                        message = if (isConnected) "Connection successful!" else "Connection failed",
                    )
                    if (isConnected) {
                        // Update server connection status
                        webDavRepository.updateConnectionStatus(
                            server.id,
                            isConnected = true,
                            lastConnected = System.currentTimeMillis(),
                        )
                    }
                }.onFailure { exception ->
                    _testConnectionResult.value = TestConnectionResult(
                        isSuccess = false,
                        message = exception.message ?: "Connection test failed",
                    )
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearTestConnectionResult() {
        _testConnectionResult.value = null
    }

    fun toggleHistory() {
        _showHistory.value = !_showHistory.value
    }

    fun hideHistory() {
        _showHistory.value = false
    }

    fun removeFromHistory(historyId: String) {
        viewModelScope.launch {
            try {
                webDavRepository.deleteHistory(historyId)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun getServerById(serverId: String): WebDavServer? {
        return servers.value.find { it.id == serverId }
    }

    /**
     * Create correct WebDAV URI for history playback, avoiding path duplication
     */
    fun createHistoryPlaybackUri(history: WebDavHistory): android.net.Uri? {
        val server = getServerById(history.serverId) ?: return null
        
        return try {
            val serverUrl = server.url.trimEnd('/')
            val serverUri = java.net.URI(serverUrl)
            val serverBasePath = serverUri.path.trimEnd('/')
            
            // If filePath already contains the server's base path, use it directly
            // Otherwise, append it to the server URL
            val finalUrl = if (serverBasePath.isNotEmpty() && history.filePath.startsWith(serverBasePath)) {
                // filePath already contains base path, construct full URL
                "${serverUri.scheme}://${serverUri.authority}${history.filePath}"
            } else {
                // filePath is relative, append to server URL
                "${serverUrl}${if (history.filePath.startsWith("/")) history.filePath else "/${history.filePath}"}"
            }

            finalUrl.toUri()
        } catch (e: Exception) {
            android.util.Log.e("WebDavViewModel", "Error creating history playback URI", e)
            // Fallback to simple concatenation
            "${server.url.trimEnd('/')}${if (history.filePath.startsWith("/")) history.filePath else "/${history.filePath}"}".toUri()
        }
    }

    private fun loadServers() {
        // No need to manually load servers since we're using StateFlow from repository
    }
}
