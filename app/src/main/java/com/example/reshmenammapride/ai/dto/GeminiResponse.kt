package com.example.reshmenammapride.ai.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ─── Response hierarchy ───────────────────────────────────────────────────────

@Serializable
data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null,
    val error: GeminiError? = null,
    @SerialName("promptFeedback")
    val promptFeedback: PromptFeedback? = null
)

@Serializable
data class GeminiCandidate(
    val content: GeminiContent? = null,
    @SerialName("finishReason")
    val finishReason: String? = null,
    @SerialName("safetyRatings")
    val safetyRatings: List<SafetyRating>? = null
)

@Serializable
data class SafetyRating(
    val category: String = "",
    val probability: String = ""
)

@Serializable
data class PromptFeedback(
    @SerialName("blockReason")
    val blockReason: String? = null
)

@Serializable
data class GeminiError(
    val code: Int = 0,
    val message: String = "",
    val status: String = ""
)

// ─── Helper extension ─────────────────────────────────────────────────────────

/** Extracts the plain text from the first candidate, or null if unavailable. */
fun GeminiResponse.extractText(): String? =
    candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
