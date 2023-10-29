package com.raveline.concord.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.raveline.concord.database.entity.Chat
import com.raveline.concord.database.dao.ChatDao
import com.raveline.concord.database.dao.DownloadableFileDao
import com.raveline.concord.database.dao.MessageDao
import com.raveline.concord.database.entity.DownloadableFileEntity
import com.raveline.concord.database.entity.MessageEntity

@Database(
    entities = [Chat::class, MessageEntity::class, DownloadableFileEntity::class],
    version = 2,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(
            from = 1, to = 2
        )
    ]
)

abstract class ConcordDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun messageDao(): MessageDao
    abstract fun downloadableFileDao(): DownloadableFileDao
}
