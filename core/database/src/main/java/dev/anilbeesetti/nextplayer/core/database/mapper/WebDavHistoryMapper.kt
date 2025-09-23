package dev.anilbeesetti.nextplayer.core.database.mapper

import dev.anilbeesetti.nextplayer.core.database.entities.WebDavHistoryEntity
import dev.anilbeesetti.nextplayer.core.model.WebDavHistory

fun WebDavHistory.toWebDavHistoryEntity(): WebDavHistoryEntity {
    return WebDavHistoryEntity(
        id = id,
        serverId = serverId,
        serverName = serverName,
        fileName = fileName,
        filePath = filePath,
        fileSize = fileSize,
        lastPlayed = lastPlayed,
        duration = duration,
        position = position,
        mimeType = mimeType,
    )
}

fun WebDavHistoryEntity.toWebDavHistory(): WebDavHistory {
    return WebDavHistory(
        id = id,
        serverId = serverId,
        serverName = serverName,
        fileName = fileName,
        filePath = filePath,
        fileSize = fileSize,
        lastPlayed = lastPlayed,
        duration = duration,
        position = position,
        mimeType = mimeType,
    )
}
