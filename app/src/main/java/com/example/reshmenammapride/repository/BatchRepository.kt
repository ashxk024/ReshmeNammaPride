package com.example.reshmenammapride.repository

import com.example.reshmenammapride.database.dao.BatchDao
import com.example.reshmenammapride.database.dao.ClimateEntryDao
import com.example.reshmenammapride.database.entity.BatchEntity
import com.example.reshmenammapride.database.entity.ClimateEntryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for batch and climate data.
 *
 * ViewModels talk to the repository; the repository talks to the DAOs.
 * This keeps ViewModels free of database import dependencies.
 */
class BatchRepository(
    private val batchDao: BatchDao,
    private val climateEntryDao: ClimateEntryDao
) {

    // ── Batches ──────────────────────────────────────────────────────────────

    /**
     * Insert a batch and return its generated id.
     * Must be called from a coroutine (suspend).
     */
    suspend fun insertBatch(
        batchName: String,
        breedType: String,
        createdEpochDay: Long
    ): Int {
        val entity = BatchEntity(
            batchName       = batchName,
            breedType       = breedType,
            createdEpochDay = createdEpochDay
        )
        return batchDao.insertBatch(entity).toInt()
    }

    /**
     * Flow of all batches ordered newest first.
     * Collect in HomeViewModel to drive the batch list.
     */
    fun getAllBatches(): Flow<List<BatchEntity>> = batchDao.getAllBatches()

    suspend fun getBatchById(id: Int): BatchEntity? = batchDao.getBatchById(id)

    suspend fun deleteBatch(id: Int) = batchDao.deleteBatch(id)

    // ── Climate entries ──────────────────────────────────────────────────────

    /**
     * Insert a climate entry for the given batch.
     * Returns the generated entry id.
     */
    suspend fun insertClimateEntry(
        batchId: Int,
        temperature: Float,
        humidity: Float
    ): Int {
        val entity = ClimateEntryEntity(
            batchId     = batchId,
            temperature = temperature,
            humidity    = humidity
        )
        return climateEntryDao.insertEntry(entity).toInt()
    }

    /**
     * Flow of climate entries for one batch, newest first.
     * Collect in DashboardViewModel to drive the history list + dial.
     */
    fun getClimateEntries(batchId: Int): Flow<List<ClimateEntryEntity>> =
        climateEntryDao.getEntriesForBatch(batchId)

    /** One-shot fetch for AI analysis. */
    suspend fun getClimateEntriesOnce(batchId: Int): List<ClimateEntryEntity> =
        climateEntryDao.getEntriesForBatchOnce(batchId)
}
