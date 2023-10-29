package com.raveline.concord.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.raveline.concord.data.DownloadableFile

@Entity(tableName = "DownloadableFile_Table")
data class DownloadableFileEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,
    var name: String = "",
    var url: String = "",
    var size: Long = 0L,
)

fun DownloadableFileEntity.toDownloadableFile() = DownloadableFile(
    id = id,
    name = name,
    url = url,
    size = size
)