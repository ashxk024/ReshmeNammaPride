package com.example.reshmenammapride.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.reshmenammapride.database.entity.ClimateEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClimateEntryDao {

    /** Insert a new climate reading. Returns the new row id. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: ClimateEntryEntity): Long

    /**
     * Observe all climate entries for a given batch, newest first.
     * Flow means DashboardScreen updates automatically when new readings arrive.
     */
    @Query("SELECT * FROM climate_entries WHERE batchId = :batchId ORDER BY recordedAt DESC")
    fun getEntriesForBatch(batchId: Int): Flow<List<ClimateEntryEntity>>

    /** One-shot fetch (useful for the AI analysis trigger). */
    @Query("SELECT * FROM climate_entries WHERE batchId = :batchId ORDER BY recordedAt DESC")
    suspend fun getEntriesForBatchOnce(batchId: Int): List<ClimateEntryEntity>
}
