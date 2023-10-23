package com.raveline.concord.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.raveline.concord.data.Chat
import com.raveline.concord.data.Message

@Database(
    entities = [Chat::class, Message::class],
    version = 1,
    exportSchema = true,
)

abstract class ConcordDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun messageDao(): MessageDao
}
