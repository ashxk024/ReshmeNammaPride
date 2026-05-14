package com.example.reshmenammapride.model

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * UI-layer model for a single climate reading.
 * Converted from [ClimateEntryEntity] by the repository/ViewModel.
 */
data class ClimateEntry(
    val id: Int = 0,
    val temperature: Float,
    val humidity: Float,
    val recordedAt: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        /** Convert epoch-millis stored in Room back to LocalDateTime for display. */
        fun fromEpochMillis(millis: Long): LocalDateTime =
            LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault())
    }
}
