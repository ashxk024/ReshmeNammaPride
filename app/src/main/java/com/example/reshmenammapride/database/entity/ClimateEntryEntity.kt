package com.example.reshmenammapride.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Persisted climate reading linked to a parent [BatchEntity].
 *
 * Foreign key ensures entries are deleted automatically when their batch is deleted.
 * Index on [batchId] speeds up queries like "all entries for batch X".
 */
@Entity(
    tableName = "climate_entries",
    foreignKeys = [
        ForeignKey(
            entity        = BatchEntity::class,
            parentColumns = ["id"],
            childColumns  = ["batchId"],
            onDelete      = ForeignKey.CASCADE  // deletes entries when batch is deleted
        )
    ],
    indices = [Index(value = ["batchId"])]
)
data class ClimateEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val batchId: Int,
    val temperature: Float,
    val humidity: Float,
    val recordedAt: Long = System.currentTimeMillis()  // epoch millis — no TypeConverter needed
)
