package com.example.reshmenammapride.ai.service

import com.example.reshmenammapride.ai.dto.GeminiRequest
import com.example.reshmenammapride.ai.dto.GeminiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Retrofit interface for the Gemini 1.5 Flash REST API.
 *
 * Base URL: https://generativelanguage.googleapis.com/
 * Full endpoint: v1beta/models/gemini-1.5-flash:generateContent?key=<API_KEY>
 *
 * We use gemini-1.5-flash because:
 * - It is fast (< 2 seconds typical)
 * - It is cheap (much lower cost per token than Pro)
 * - It is accurate enough for farming advice
 */
interface GeminiApiService {

    @POST("v1beta/models/gemini-2.0-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): Response<GeminiResponse>
}
