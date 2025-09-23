package dev.anilbeesetti.nextplayer.core.model

import kotlinx.serialization.Serializable

@Serializable
data class WebDavHistory(
    val id: String,
    val serverId: String,
    val serverName: String,
    val fileName: String,
    val filePath: String,
    val fileSize: Long = 0L,
    val lastPlayed: Long = System.currentTimeMillis(),
    val duration: Long = 0L, // Video duration in milliseconds
    val position: Long = 0L, // Last played position in milliseconds
    val mimeType: String? = null,
)

@Serializable
data class WebDavHistoryWithServer(
    val history: WebDavHistory,
    val server: WebDavServer?,
)