package com.example.reshmenammapride.ai.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ─── Request hierarchy ────────────────────────────────────────────────────────

@Serializable
data class GeminiRequest(
    val contents: List<GeminiContent>,
    @SerialName("generationConfig")
    val generationConfig: GenerationConfig = GenerationConfig()
)

@Serializable
data class GeminiContent(
    val role: String = "user",
    val parts: List<GeminiPart>
)

@Serializable
data class GeminiPart(
    val text: String
)

@Serializable
data class GenerationConfig(
    val temperature: Double = 0.4,
    @SerialName("maxOutputTokens")
    val maxOutputTokens: Int = 600,
    @SerialName("topP")
    val topP: Double = 0.8,
    @SerialName("topK")
    val topK: Int = 40
)
