package dev.anilbeesetti.nextplayer.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import dev.anilbeesetti.nextplayer.core.database.entities.WebDavHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WebDavHistoryDao {
    
    @Query("SELECT * FROM webdav_history ORDER BY lastPlayed DESC LIMIT :limit")
    fun getAllHistory(limit: Int = 50): Flow<List<WebDavHistoryEntity>>
    
    @Query("SELECT * FROM webdav_history WHERE serverId = :serverId ORDER BY lastPlayed DESC LIMIT :limit")
    fun getHistoryByServer(serverId: String, limit: Int = 20): Flow<List<WebDavHistoryEntity>>
    
    @Query("SELECT * FROM webdav_history WHERE serverId = :serverId AND filePath = :filePath LIMIT 1")
    suspend fun getHistoryItem(serverId: String, filePath: String): WebDavHistoryEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: WebDavHistoryEntity)
    
    @Update
    suspend fun updateHistory(history: WebDavHistoryEntity)
    
    @Query("DELETE FROM webdav_history WHERE id = :id")
    suspend fun deleteHistory(id: String)
    
    @Query("DELETE FROM webdav_history WHERE serverId = :serverId")
    suspend fun deleteHistoryByServer(serverId: String)
    
    @Query("DELETE FROM webdav_history WHERE lastPlayed < :timestamp")
    suspend fun deleteOldHistory(timestamp: Long)
    
    @Query("SELECT COUNT(*) FROM webdav_history")
    suspend fun getHistoryCount(): Int
    
    @Query("DELETE FROM webdav_history WHERE id NOT IN (SELECT id FROM webdav_history ORDER BY lastPlayed DESC LIMIT :limit)")
    suspend fun keepRecentHistory(limit: Int = 100)
}