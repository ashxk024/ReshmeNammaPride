package com.example.reshmenammapride.ai.preprocessing

import com.example.reshmenammapride.database.entity.ClimateEntryEntity
import com.example.reshmenammapride.model.InstarStage
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Converts raw [ClimateEntryEntity] rows into a structured [ClimateReport].
 *
 * All preprocessing happens on the device — Gemini only sees aggregated numbers,
 * keeping prompt size small and API costs low.
 */
object ClimatePreprocessor {

    // ── Safe range constants for silkworms ────────────────────────────────────
    private const val TEMP_SAFE_MIN   = 18f
    private const val TEMP_SAFE_MAX   = 34f
    private const val HUM_SAFE_MIN    = 55f
    private const val HUM_SAFE_MAX    = 95f
    private const val TEMP_INSTABILITY_THRESHOLD = 2.0f   // °C std dev
    private const val HUM_INSTABILITY_THRESHOLD  = 8.0f   // % std dev
    private const val RECENT_WINDOW   = 5

    /**
     * Main entry point.
     *
     * @param entries     Raw entries from Room, ordered newest-first.
     * @param batchName   Display name of the batch.
     * @param breedType   Breed string.
     * @param batchAgeDays Days since batch was created.
     * @param instarStage Current growth stage enum.
     * @return [ClimateReport] ready to be forwarded to [PromptBuilder].
     * @throws IllegalArgumentException if [entries] is empty.
     */
    fun process(
        entries: List<ClimateEntryEntity>,
        batchName: String,
        breedType: String,
        batchAgeDays: Int,
        instarStage: InstarStage
    ): ClimateReport {
        require(entries.isNotEmpty()) { "Cannot build a climate report from empty entries." }

        val temps = entries.map { it.temperature }
        val hums  = entries.map { it.humidity }

        // ── Basic statistics ──────────────────────────────────────────────────
        val avgTemp  = temps.average().toFloat()
        val minTemp  = temps.min()
        val maxTemp  = temps.max()
        val avgHum   = hums.average().toFloat()
        val minHum   = hums.min()
        val maxHum   = hums.max()

        // ── Standard deviation ────────────────────────────────────────────────
        val tempStdDev = stdDev(temps)
        val humStdDev  = stdDev(hums)

        // ── Trend (linear regression on time-ordered data) ────────────────────
        // Entries are newest-first → reverse for chronological order
        val chronoTemps = temps.reversed()
        val chronoHums  = hums.reversed()
        val tempTrend   = computeTrend(chronoTemps)
        val humTrend    = computeTrend(chronoHums)

        // ── Recent window ─────────────────────────────────────────────────────
        val recentTemps = temps.take(RECENT_WINDOW)
        val recentHums  = hums.take(RECENT_WINDOW)
        val recentAvgTemp = recentTemps.average().toFloat()
        val recentAvgHum  = recentHums.average().toFloat()

        // ── Danger flags ──────────────────────────────────────────────────────
        val dangerousTemp = temps.any { it < TEMP_SAFE_MIN || it > TEMP_SAFE_MAX }
        val dangerousHum  = hums.any  { it < HUM_SAFE_MIN  || it > HUM_SAFE_MAX  }
        val highInstability = tempStdDev > TEMP_INSTABILITY_THRESHOLD ||
                humStdDev  > HUM_INSTABILITY_THRESHOLD

        // ── Consistency score (0–100) ─────────────────────────────────────────
        val consistencyScore = computeConsistencyScore(
            avgTemp, avgHum, tempStdDev, humStdDev, dangerousTemp, dangerousHum
        )

        // ── Days until harvest ────────────────────────────────────────────────
        val harvestDay = 28
        val daysLeft   = (harvestDay - batchAgeDays).coerceAtLeast(0)

        return ClimateReport(
            entryCount              = entries.size,
            avgTemperature          = avgTemp,
            minTemperature          = minTemp,
            maxTemperature          = maxTemp,
            avgHumidity             = avgHum,
            minHumidity             = minHum,
            maxHumidity             = maxHum,
            tempFluctuation         = maxTemp - minTemp,
            humidityFluctuation     = maxHum - minHum,
            tempStdDev              = tempStdDev,
            humidityStdDev          = humStdDev,
            tempTrend               = tempTrend,
            humidityTrend           = humTrend,
            recentAvgTemp           = recentAvgTemp,
            recentAvgHumidity       = recentAvgHum,
            hasDangerousTemperature = dangerousTemp,
            hasDangerousHumidity    = dangerousHum,
            hasHighInstability      = highInstability,
            batchName               = batchName,
            breedType               = breedType,
            batchAgeDays            = batchAgeDays,
            instarStageName         = instarStage.displayName(),
            daysUntilHarvest        = daysLeft,
            consistencyScore        = consistencyScore
        )
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private fun stdDev(values: List<Float>): Float {
        if (values.size < 2) return 0f
        val mean = values.average()
        val variance = values.sumOf { (it - mean).pow(2).toDouble() } / values.size
        return sqrt(variance).toFloat()
    }

    /**
     * Simple linear trend: compare the mean of the last third vs the first third.
     * Stable if change < 1°C / 3%.
     */
    private fun computeTrend(chronologicalValues: List<Float>): TrendDirection {
        if (chronologicalValues.size < 3) return TrendDirection.STABLE
        val third = (chronologicalValues.size / 3).coerceAtLeast(1)
        val earlyMean = chronologicalValues.take(third).average().toFloat()
        val lateMean  = chronologicalValues.takeLast(third).average().toFloat()
        val delta     = lateMean - earlyMean
        return when {
            delta >  1.0f -> TrendDirection.RISING
            delta < -1.0f -> TrendDirection.FALLING
            else          -> TrendDirection.STABLE
        }
    }

    /**
     * Scores how consistent the climate has been.
     * 100 = perfect; deductions for being out of ideal range, high std dev, dangerous readings.
     */
    private fun computeConsistencyScore(
        avgTemp: Float, avgHum: Float,
        tempStd: Float, humStd: Float,
        dangerTemp: Boolean, dangerHum: Boolean
    ): Int {
        var score = 100

        // Ideal ranges: temp 24–28, hum 70–85
        if (avgTemp < 24f || avgTemp > 28f) score -= 20
        if (avgHum  < 70f || avgHum  > 85f) score -= 20
        score -= (tempStd * 5).toInt().coerceAtMost(20)
        score -= (humStd  * 1).toInt().coerceAtMost(20)
        if (dangerTemp) score -= 15
        if (dangerHum)  score -= 15

        return score.coerceIn(0, 100)
    }

    private fun InstarStage.displayName() = when (this) {
        InstarStage.EARLY  -> "Early Stage (Instars 1–2)"
        InstarStage.MID    -> "Mid Growth (Instars 3–4)"
        InstarStage.MATURE -> "Mature Stage (Instar 5 / spinning)"
    }
}
