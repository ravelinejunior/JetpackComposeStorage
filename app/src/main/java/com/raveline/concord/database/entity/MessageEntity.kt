package com.raveline.concord.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.RenameTable
import com.raveline.concord.data.MessageWithFile

@Entity(tableName = "Message")
@RenameTable("Message","Message_Table")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,
    var chatId: Long = 0L,
    var content: String = "",
    var author: Author = Author.OTHER,
    var date: String = "",
    var mediaLink: String = "",
    var idDownloadableFile: Long? = null,
)

fun MessageEntity.toMessageFile() = MessageWithFile(
    id = id,
    chatId = chatId,
    content = content,
    author = author,
    date = date,
    mediaLink = mediaLink,
    idDownloadableFile = idDownloadableFile,
)

enum class Author {
    USER, OTHER
}