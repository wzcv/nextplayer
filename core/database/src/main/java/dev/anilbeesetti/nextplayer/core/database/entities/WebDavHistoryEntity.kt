package dev.anilbeesetti.nextplayer.core.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "webdav_history",
    foreignKeys = [
        ForeignKey(
            entity = WebDavServerEntity::class,
            parentColumns = ["id"],
            childColumns = ["serverId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["serverId"]),
        Index(value = ["lastPlayed"]),
        Index(value = ["serverId", "filePath"], unique = true)
    ]
)
data class WebDavHistoryEntity(
    @PrimaryKey
    val id: String,
    val serverId: String,
    val serverName: String,
    val fileName: String,
    val filePath: String,
    val fileSize: Long = 0L,
    val lastPlayed: Long = System.currentTimeMillis(),
    val duration: Long = 0L,
    val position: Long = 0L,
    val mimeType: String? = null,
)