package com.example.reshmenammapride.ai.preprocessing


/**
 * Fully aggregated climate report produced by [ClimatePreprocessor].
 * This is what gets sent to Gemini — NOT the raw ClimateEntryEntity list.
 */
data class ClimateReport(

    // ── Basic stats ───────────────────────────────────────────────────────────
    val entryCount: Int,
    val avgTemperature: Float,
    val minTemperature: Float,
    val maxTemperature: Float,
    val avgHumidity: Float,
    val minHumidity: Float,
    val maxHumidity: Float,

    // ── Fluctuation analysis ──────────────────────────────────────────────────
    val tempFluctuation: Float,          // max − min
    val humidityFluctuation: Float,
    val tempStdDev: Float,               // standard deviation — true instability
    val humidityStdDev: Float,

    // ── Trend direction ───────────────────────────────────────────────────────
    val tempTrend: TrendDirection,       // RISING / FALLING / STABLE
    val humidityTrend: TrendDirection,

    // ── Recent window (last 5 readings) ──────────────────────────────────────
    val recentAvgTemp: Float,
    val recentAvgHumidity: Float,

    // ── Danger flags ──────────────────────────────────────────────────────────
    val hasDangerousTemperature: Boolean,  // any reading < 18 or > 34
    val hasDangerousHumidity: Boolean,     // any reading < 55 or > 95
    val hasHighInstability: Boolean,       // stdDev > 2 for temp or > 8 for humidity

    // ── Batch context ─────────────────────────────────────────────────────────
    val batchName: String,
    val breedType: String,
    val batchAgeDays: Int,
    val instarStageName: String,           // "Early Stage" / "Mid Growth" / "Mature Stage"
    val daysUntilHarvest: Int,

    // ── Climate consistency score (0–100) ─────────────────────────────────────
    val consistencyScore: Int
)

enum class TrendDirection { RISING, FALLING, STABLE }
