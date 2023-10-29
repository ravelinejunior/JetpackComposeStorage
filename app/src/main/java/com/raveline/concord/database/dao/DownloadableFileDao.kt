package com.raveline.concord.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import com.raveline.concord.database.entity.DownloadableFileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadableFileDao {

    @Insert(onConflict = REPLACE)
    suspend fun insert(downloadableFileEntity: DownloadableFileEntity)

    @Query("SELECT * FROM DOWNLOADABLEFILE_TABLE WHERE ID = :id")
    fun getFileById(id: Long): Flow<DownloadableFileEntity>

}