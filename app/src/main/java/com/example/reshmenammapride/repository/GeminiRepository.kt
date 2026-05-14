package com.example.reshmenammapride.repository

import android.util.Log
import com.example.reshmenammapride.ai.AiResult
import com.example.reshmenammapride.ai.ApiKeyProvider
import com.example.reshmenammapride.ai.dto.GeminiContent
import com.example.reshmenammapride.ai.dto.GeminiPart
import com.example.reshmenammapride.ai.dto.GeminiRequest
import com.example.reshmenammapride.ai.dto.extractText
import com.example.reshmenammapride.ai.preprocessing.ClimatePreprocessor
import com.example.reshmenammapride.ai.preprocessing.PromptBuilder
import com.example.reshmenammapride.ai.service.GeminiApiService
import com.example.reshmenammapride.database.entity.ClimateEntryEntity
import com.example.reshmenammapride.model.InstarStage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Handles the full AI pipeline:
 *
 * Climate entries
 *   → preprocessing
 *   → prompt building
 *   → Gemini API
 *   → AI result
 */
class GeminiRepository(
    private val apiService: GeminiApiService
) {

    companion object {
        private const val MIN_ENTRIES_FOR_ANALYSIS = 3
    }

    // Prevents multiple simultaneous API requests
    private var isRequestRunning = false

    /**
     * Main analysis entry point.
     */
    suspend fun analyzeClimate(
        entries: List<ClimateEntryEntity>,
        batchName: String,
        breedType: String,
        batchAgeDays: Int,
        instarStage: InstarStage
    ): AiResult = withContext(Dispatchers.IO) {

        // ─────────────────────────────────────────────────────────────
        // API key check
        // ─────────────────────────────────────────────────────────────
        if (!ApiKeyProvider.isConfigured()) {
            return@withContext AiResult.ApiKeyMissing
        }

        Log.d("AI_FLOW", "ENTRY COUNT: ${entries.size}")

        // ─────────────────────────────────────────────────────────────
        // Minimum data check
        // ─────────────────────────────────────────────────────────────
        if (entries.size < MIN_ENTRIES_FOR_ANALYSIS) {
            return@withContext AiResult.InsufficientData
        }

        // ─────────────────────────────────────────────────────────────
        // Prevent duplicate requests
        // ─────────────────────────────────────────────────────────────
        if (isRequestRunning) {
            return@withContext AiResult.Error(
                "Analysis already running. Please wait."
            )
        }

        isRequestRunning = true

        try {

            // ─────────────────────────────────────────────────────────
            // Preprocess climate data
            // ─────────────────────────────────────────────────────────
            val report = try {

                ClimatePreprocessor.process(
                    entries = entries,
                    batchName = batchName,
                    breedType = breedType,
                    batchAgeDays = batchAgeDays,
                    instarStage = instarStage
                )

            } catch (e: Exception) {

                return@withContext AiResult.Error(
                    "Failed to process climate data: ${e.message}"
                )
            }

            Log.d("AI_FLOW", "Preprocessing completed")

            // ─────────────────────────────────────────────────────────
            // Build Gemini prompt
            // ─────────────────────────────────────────────────────────
            val prompt = PromptBuilder.build(report)

            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(
                            GeminiPart(text = prompt)
                        )
                    )
                )
            )

            // ─────────────────────────────────────────────────────────
            // API key
            // ─────────────────────────────────────────────────────────
            val apiKey = ApiKeyProvider.getGeminiApiKey()

            Log.d("AI_FLOW", "Calling Gemini API")

            // ─────────────────────────────────────────────────────────
            // API request
            // ─────────────────────────────────────────────────────────
            val response = apiService.generateContent(
                apiKey = apiKey,
                request = request
            )

            Log.d("AI_FLOW", "HTTP CODE = ${response.code()}")

            // ─────────────────────────────────────────────────────────
            // Success
            // ─────────────────────────────────────────────────────────
            if (response.isSuccessful) {

                val body = response.body()

                val blockReason = body?.promptFeedback?.blockReason

                if (!blockReason.isNullOrBlank()) {

                    return@withContext AiResult.Error(
                        "Request blocked: $blockReason"
                    )
                }

                val text = body?.extractText()

                return@withContext if (text.isNullOrBlank()) {

                    AiResult.Error(
                        "Gemini returned an empty response."
                    )

                } else {

                    AiResult.Success(
                        advice = text.trim()
                    )
                }
            }

            // ─────────────────────────────────────────────────────────
            // Rate limit
            // ─────────────────────────────────────────────────────────
            if (response.code() == 429) {

                return@withContext AiResult.Error(
                    "Gemini free quota temporarily exhausted. Please wait 1–2 minutes and try again."
                )
            }

            // ─────────────────────────────────────────────────────────
            // Invalid API key
            // ─────────────────────────────────────────────────────────
            if (response.code() == 401 || response.code() == 403) {

                return@withContext AiResult.Error(
                    "Invalid API key or insufficient permissions."
                )
            }

            // ─────────────────────────────────────────────────────────
            // Other API errors
            // ─────────────────────────────────────────────────────────
            val errorBody =
                response.errorBody()?.string() ?: "Unknown error"

            return@withContext AiResult.Error(
                "Request failed (${response.code()}): $errorBody"
            )

        } catch (e: IOException) {

            Log.e("AI_FLOW", "Network error", e)

            return@withContext AiResult.Error(
                "No internet connection."
            )

        } catch (e: Exception) {

            Log.e("AI_FLOW", "Unexpected Gemini error", e)

            return@withContext AiResult.Error(
                e.message ?: "Unknown error"
            )

        } finally {

            isRequestRunning = false
        }
    }
}