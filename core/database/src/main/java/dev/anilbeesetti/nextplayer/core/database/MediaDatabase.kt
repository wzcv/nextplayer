package dev.anilbeesetti.nextplayer.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import dev.anilbeesetti.nextplayer.core.database.dao.DirectoryDao
import dev.anilbeesetti.nextplayer.core.database.dao.MediumDao
import dev.anilbeesetti.nextplayer.core.database.dao.WebDavHistoryDao
import dev.anilbeesetti.nextplayer.core.database.dao.WebDavServerDao
import dev.anilbeesetti.nextplayer.core.database.entities.AudioStreamInfoEntity
import dev.anilbeesetti.nextplayer.core.database.entities.DirectoryEntity
import dev.anilbeesetti.nextplayer.core.database.entities.MediumEntity
import dev.anilbeesetti.nextplayer.core.database.entities.SubtitleStreamInfoEntity
import dev.anilbeesetti.nextplayer.core.database.entities.VideoStreamInfoEntity
import dev.anilbeesetti.nextplayer.core.database.entities.WebDavHistoryEntity
import dev.anilbeesetti.nextplayer.core.database.entities.WebDavServerEntity

@Database(
    entities = [
        DirectoryEntity::class,
        MediumEntity::class,
        VideoStreamInfoEntity::class,
        AudioStreamInfoEntity::class,
        SubtitleStreamInfoEntity::class,
        WebDavServerEntity::class,
        WebDavHistoryEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
abstract class MediaDatabase : RoomDatabase() {

    abstract fun mediumDao(): MediumDao

    abstract fun directoryDao(): DirectoryDao

    abstract fun webDavServerDao(): WebDavServerDao

    abstract fun webDavHistoryDao(): WebDavHistoryDao

    companion object {
        const val DATABASE_NAME = "media_db"
    }
}
