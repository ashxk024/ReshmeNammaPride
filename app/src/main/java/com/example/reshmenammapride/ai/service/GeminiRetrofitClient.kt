package com.example.reshmenammapride.ai.service

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

/**
 * Singleton Retrofit client for the Gemini API.
 *
 * Configuration:
 * - 30s connect / 60s read timeout (Gemini can be slow for long prompts)
 * - Logging interceptor in DEBUG builds only
 * - Kotlinx Serialization for JSON (matches our @Serializable DTOs)
 * - ignoreUnknownKeys = true so new Gemini response fields don't crash the app
 */
object GeminiRetrofitClient {

    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient         = true
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        // Only log body in debug builds to avoid leaking API keys in production logs
        level = if (com.example.reshmenammapride.BuildConfig.DEBUG)
            HttpLoggingInterceptor.Level.BODY
        else
            HttpLoggingInterceptor.Level.NONE
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()

    val apiService: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(
                json.asConverterFactory("application/json".toMediaType())
            )
            .build()
            .create(GeminiApiService::class.java)
    }
}
