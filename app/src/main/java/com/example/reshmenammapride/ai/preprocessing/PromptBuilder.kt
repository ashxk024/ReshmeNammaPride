package com.example.reshmenammapride.ai.preprocessing

/**
 * Converts a [ClimateReport] into a structured Gemini prompt.
 *
 * Design principles:
 * • Role prompt sets Gemini as a silkworm farming advisor — not a generic chatbot.
 * • All numbers are pre-computed by [ClimatePreprocessor] before reaching here.
 * • The response format is explicitly specified so output is predictable and parseable.
 * • Language is kept simple: the audience is village farmers, not scientists.
 */
object PromptBuilder {

    fun build(report: ClimateReport): String = buildString {

        // ── Role + audience ───────────────────────────────────────────────────
        appendLine("""
            You are an expert silkworm farming advisor helping small-scale farmers in India.
            Your advice must be:
            - Simple and practical (farmers may have low literacy)
            - Actionable (tell exactly what to do)
            - Prioritized (most urgent issue first)
            - In short paragraphs, not bullet lists
            - Free of scientific jargon
            
            Analyze the following climate data for a silkworm batch and provide farming advice.
        """.trimIndent())

        appendLine()

        // ── Batch context ─────────────────────────────────────────────────────
        appendLine("=== BATCH INFORMATION ===")
        appendLine("Batch Name   : ${report.batchName}")
        appendLine("Breed Type   : ${report.breedType}")
        appendLine("Age          : ${report.batchAgeDays} days")
        appendLine("Growth Stage : ${report.instarStageName}")
        appendLine("Days to Harvest: ${report.daysUntilHarvest} days remaining")
        appendLine()

        // ── Overall climate health ────────────────────────────────────────────
        appendLine("=== OVERALL CLIMATE HEALTH ===")
        appendLine("Consistency Score: ${report.consistencyScore}/100 " +
                "(100 = perfectly stable, 0 = very unstable)")
        appendLine("Total readings analyzed: ${report.entryCount}")
        appendLine()

        // ── Temperature analysis ──────────────────────────────────────────────
        appendLine("=== TEMPERATURE ANALYSIS ===")
        appendLine("Average   : ${report.avgTemperature.f1()}°C  (ideal: 24–28°C)")
        appendLine("Minimum   : ${report.minTemperature.f1()}°C")
        appendLine("Maximum   : ${report.maxTemperature.f1()}°C")
        appendLine("Fluctuation (range): ${report.tempFluctuation.f1()}°C")
        appendLine("Stability (std dev) : ${report.tempStdDev.f1()}°C  (safe: <2°C)")
        appendLine("Trend     : ${report.tempTrend.label()}")
        appendLine("Recent avg (last 5 readings): ${report.recentAvgTemp.f1()}°C")
        if (report.hasDangerousTemperature)
            appendLine("⚠ ALERT: One or more readings were outside safe range (18–34°C)!")
        appendLine()

        // ── Humidity analysis ─────────────────────────────────────────────────
        appendLine("=== HUMIDITY ANALYSIS ===")
        appendLine("Average   : ${report.avgHumidity.f1()}%   (ideal: 70–85%)")
        appendLine("Minimum   : ${report.minHumidity.f1()}%")
        appendLine("Maximum   : ${report.maxHumidity.f1()}%")
        appendLine("Fluctuation (range): ${report.humidityFluctuation.f1()}%")
        appendLine("Stability (std dev) : ${report.humidityStdDev.f1()}%  (safe: <8%)")
        appendLine("Trend     : ${report.humidityTrend.label()}")
        appendLine("Recent avg (last 5 readings): ${report.recentAvgHumidity.f1()}%")
        if (report.hasDangerousHumidity)
            appendLine("⚠ ALERT: One or more readings were outside safe range (55–95%)!")
        appendLine()

        // ── Combined risk flags ───────────────────────────────────────────────
        if (report.hasHighInstability) {
            appendLine("=== INSTABILITY WARNING ===")
            appendLine("Climate conditions have been highly unstable. " +
                    "This can stress silkworms and increase disease risk.")
            appendLine()
        }

        // ── Response format instruction ───────────────────────────────────────
        appendLine("=== YOUR RESPONSE FORMAT ===")
        appendLine("Respond in exactly 4 sections using these headings:")
        appendLine()
        appendLine("CURRENT STATUS")
        appendLine("(1–2 sentences: overall assessment of climate health for this batch)")
        appendLine()
        appendLine("MAIN CONCERN")
        appendLine("(1–2 sentences: the single most important issue to fix right now)")
        appendLine()
        appendLine("WHAT TO DO")
        appendLine("(2–4 practical actions the farmer should take today or this week)")
        appendLine()
        appendLine("SILK QUALITY OUTLOOK")
        appendLine("(1–2 sentences: expected cocoon/silk quality based on current conditions)")
        appendLine()
        appendLine("Keep your entire response under 250 words. Use simple language.")
    }

    // ── Formatting helpers ────────────────────────────────────────────────────

    private fun Float.f1() = "%.1f".format(this)

    private fun TrendDirection.label() = when (this) {
        TrendDirection.RISING  -> "RISING ↑ (getting warmer/wetter)"
        TrendDirection.FALLING -> "FALLING ↓ (getting cooler/drier)"
        TrendDirection.STABLE  -> "STABLE → (consistent)"
    }
}
