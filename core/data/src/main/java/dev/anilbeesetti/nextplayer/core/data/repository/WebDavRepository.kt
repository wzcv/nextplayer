package dev.anilbeesetti.nextplayer.core.data.repository

import dev.anilbeesetti.nextplayer.core.model.WebDavFile
import dev.anilbeesetti.nextplayer.core.model.WebDavServer
import dev.anilbeesetti.nextplayer.core.model.WebDavHistory
import kotlinx.coroutines.flow.Flow

interface WebDavRepository {
    fun getAllServers(): Flow<List<WebDavServer>>
    suspend fun getServerById(id: String): WebDavServer?
    suspend fun addServer(server: WebDavServer)
    suspend fun updateServer(server: WebDavServer)
    suspend fun deleteServer(id: String)
    suspend fun updateConnectionStatus(id: String, isConnected: Boolean, lastConnected: Long = System.currentTimeMillis())
    suspend fun getServerFiles(serverId: String, path: String = "/"): Result<List<WebDavFile>>
    suspend fun testConnection(server: WebDavServer): Result<Boolean>
    
    // History management
    fun getAllHistory(): Flow<List<WebDavHistory>>
    fun getHistoryByServer(serverId: String): Flow<List<WebDavHistory>>
    suspend fun addHistory(history: WebDavHistory)
    suspend fun updateHistory(history: WebDavHistory)
    suspend fun deleteHistory(id: String)
    suspend fun deleteHistoryByServer(serverId: String)
    suspend fun cleanOldHistory(maxItems: Int = 100)
}
