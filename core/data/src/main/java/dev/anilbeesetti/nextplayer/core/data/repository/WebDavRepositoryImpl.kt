package dev.anilbeesetti.nextplayer.core.data.repository

import dev.anilbeesetti.nextplayer.core.data.webdav.SardineWebDavClient
import dev.anilbeesetti.nextplayer.core.database.dao.WebDavServerDao
import dev.anilbeesetti.nextplayer.core.database.dao.WebDavHistoryDao
import dev.anilbeesetti.nextplayer.core.database.mapper.toWebDavServer
import dev.anilbeesetti.nextplayer.core.database.mapper.toWebDavServerEntity
import dev.anilbeesetti.nextplayer.core.database.mapper.toWebDavHistory
import dev.anilbeesetti.nextplayer.core.database.mapper.toWebDavHistoryEntity
import dev.anilbeesetti.nextplayer.core.model.WebDavFile
import dev.anilbeesetti.nextplayer.core.model.WebDavServer
import dev.anilbeesetti.nextplayer.core.model.WebDavHistory
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber

@Singleton
class WebDavRepositoryImpl @Inject constructor(
    private val webDavServerDao: WebDavServerDao,
    private val webDavHistoryDao: WebDavHistoryDao,
    private val webDavClient: SardineWebDavClient,
) : WebDavRepository {

    override fun getAllServers(): Flow<List<WebDavServer>> {
        return webDavServerDao.getAllServers().map { entities ->
            entities.map { it.toWebDavServer() }
        }
    }

    override suspend fun getServerById(id: String): WebDavServer? {
        return webDavServerDao.getServerById(id)?.toWebDavServer()
    }

    override suspend fun addServer(server: WebDavServer) {
        webDavServerDao.insertServer(server.toWebDavServerEntity())
    }

    override suspend fun updateServer(server: WebDavServer) {
        webDavServerDao.updateServer(server.toWebDavServerEntity())
    }

    override suspend fun deleteServer(id: String) {
        webDavServerDao.deleteServerById(id)
    }

    override suspend fun updateConnectionStatus(id: String, isConnected: Boolean, lastConnected: Long) {
        webDavServerDao.updateConnectionStatus(id, isConnected, lastConnected)
    }

    override suspend fun getServerFiles(serverId: String, path: String): Result<List<WebDavFile>> {
        return try {
            val server = getServerById(serverId)
                ?: return Result.failure(IllegalArgumentException("Server not found"))

            Timber.d("Listing WebDAV files at: ${server.url}$path")

            val result = webDavClient.listFiles(server, path)
            if (result.isSuccess) {
                Timber.d("Found ${result.getOrNull()?.size ?: 0} files/directories")
            }
            result
        } catch (e: Exception) {
            Timber.e(e, "Failed to list WebDAV files")
            Result.failure(e)
        }
    }

    override suspend fun testConnection(server: WebDavServer): Result<Boolean> {
        return try {
            Timber.d("Testing WebDAV connection to: ${server.url}")

            val result = webDavClient.testConnection(server)
            if (result.isSuccess) {
                Timber.d("WebDAV connection test successful")
            }
            result
        } catch (e: Exception) {
            Timber.e(e, "WebDAV connection test failed")
            Result.failure(e)
        }
    }

    // History management implementation
    override fun getAllHistory(): Flow<List<WebDavHistory>> {
        return webDavHistoryDao.getAllHistory().map { entities ->
            entities.map { it.toWebDavHistory() }
        }
    }

    override fun getHistoryByServer(serverId: String): Flow<List<WebDavHistory>> {
        return webDavHistoryDao.getHistoryByServer(serverId).map { entities ->
            entities.map { it.toWebDavHistory() }
        }
    }

    override suspend fun addHistory(history: WebDavHistory) {
        try {
            // Check if history item already exists for this server and file path
            val existingHistory = webDavHistoryDao.getHistoryItem(history.serverId, history.filePath)
            
            if (existingHistory != null) {
                // Update existing history with new play time and position
                val updatedHistory = history.copy(
                    id = existingHistory.id,
                    lastPlayed = System.currentTimeMillis()
                )
                webDavHistoryDao.updateHistory(updatedHistory.toWebDavHistoryEntity())
            } else {
                // Insert new history item
                webDavHistoryDao.insertHistory(history.toWebDavHistoryEntity())
            }
            
            // Clean up old history to keep database size manageable
            cleanOldHistory()
        } catch (e: Exception) {
            Timber.e(e, "Failed to add WebDAV history")
        }
    }

    override suspend fun updateHistory(history: WebDavHistory) {
        try {
            webDavHistoryDao.updateHistory(history.toWebDavHistoryEntity())
        } catch (e: Exception) {
            Timber.e(e, "Failed to update WebDAV history")
        }
    }

    override suspend fun deleteHistory(id: String) {
        try {
            webDavHistoryDao.deleteHistory(id)
        } catch (e: Exception) {
            Timber.e(e, "Failed to delete WebDAV history")
        }
    }

    override suspend fun deleteHistoryByServer(serverId: String) {
        try {
            webDavHistoryDao.deleteHistoryByServer(serverId)
        } catch (e: Exception) {
            Timber.e(e, "Failed to delete WebDAV history for server: $serverId")
        }
    }

    override suspend fun cleanOldHistory(maxItems: Int) {
        try {
            val count = webDavHistoryDao.getHistoryCount()
            if (count > maxItems) {
                webDavHistoryDao.keepRecentHistory(maxItems)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to clean old WebDAV history")
        }
    }
}
