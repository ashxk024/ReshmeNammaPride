package com.example.reshmenammapride.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Persisted silkworm batch record.
 *
 * [id]             Auto-generated primary key.
 * [batchName]      Human-readable batch label.
 * [breedType]      Silkworm breed (e.g. PM×CSR2).
 * [createdEpochDay] LocalDate.toEpochDay() — avoids needing a TypeConverter for dates.
 * [createdAt]      System.currentTimeMillis() — used for display ordering.
 */
@Entity(tableName = "batches")
data class BatchEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val batchName: String,
    val breedType: String,
    val createdEpochDay: Long,
    val createdAt: Long = System.currentTimeMillis()
)
