package com.example.reshmenammapride.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.reshmenammapride.database.entity.BatchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BatchDao {

    /** Insert a new batch. Returns the new row id. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(batch: BatchEntity): Long

    /** Observe all batches, ordered newest first. Updates UI automatically. */
    @Query("SELECT * FROM batches ORDER BY createdAt DESC")
    fun getAllBatches(): Flow<List<BatchEntity>>

    /** Load a single batch by its primary key (used by Dashboard). */
    @Query("SELECT * FROM batches WHERE id = :id")
    suspend fun getBatchById(id: Int): BatchEntity?

    /** Delete a batch (cascade removes its climate entries too). */
    @Query("DELETE FROM batches WHERE id = :id")
    suspend fun deleteBatch(id: Int)
}
