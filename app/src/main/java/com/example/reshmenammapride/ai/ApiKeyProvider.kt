package com.example.reshmenammapride.ai

import com.example.reshmenammapride.BuildConfig

/**
 * Central access point for the Gemini API key.
 *
 * HOW TO SET YOUR KEY (never paste it in source code):
 * ─────────────────────────────────────────────────────
 * 1. Open (or create) the file  <project-root>/local.properties
 * 2. Add this line:
 *        GEMINI_API_KEY=AIza...your_actual_key...
 * 3. Sync Gradle. The key is injected into BuildConfig at compile time.
 * 4. local.properties is already in .gitignore — it is NEVER committed.
 *
 * For CI/CD: pass it as a Gradle property via environment variable:
 *   ./gradlew assembleRelease -PGEMINI_API_KEY=${{ secrets.GEMINI_API_KEY }}
 */
object ApiKeyProvider {

    /**
     * Returns the Gemini API key.
     * Throws [IllegalStateException] if the key was not configured.
     */
    fun getGeminiApiKey(): String {
        val key = BuildConfig.GEMINI_API_KEY
        check(key.isNotBlank()) {
            "Gemini API key is missing. Add GEMINI_API_KEY=<your_key> to local.properties."
        }
        return key
    }

    /** Returns true if the key is present — used to show/hide the AI button in UI. */
    fun isConfigured(): Boolean = BuildConfig.GEMINI_API_KEY.isNotBlank()
}
