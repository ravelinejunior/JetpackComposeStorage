package com.raveline.concord.data

import com.raveline.concord.database.entity.Author
import com.raveline.concord.database.entity.MessageEntity

data class MessageWithFile(
    var id: Long = 0L,
    var chatId: Long = 0L,
    var content: String = "",
    var author: Author = Author.OTHER,
    var date: String = "",
    var mediaLink: String = "",
    var idDownloadableFile: Long? = null,
    var downloadableFile: DownloadableFile? = null,
)

fun MessageWithFile.toMessageEntity() = MessageEntity(
    id = id,
    chatId = chatId,
    content = content,
    author = author,
    date = date,
    mediaLink = mediaLink,
    idDownloadableFile = idDownloadableFile,
)
